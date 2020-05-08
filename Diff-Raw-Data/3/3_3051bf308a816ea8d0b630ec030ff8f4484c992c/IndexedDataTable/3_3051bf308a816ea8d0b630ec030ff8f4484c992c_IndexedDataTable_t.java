 /**
  * 
  */
 package de.xwic.cube.impl;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectOutput;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import de.xwic.cube.ICell;
 import de.xwic.cube.IDimensionElement;
 import de.xwic.cube.IKeyProvider;
 import de.xwic.cube.Key;
 
 /**
  * Stores cube leaf cells within a sorted index for optimized performance
  * @author lippisch
  *
  */
 public class IndexedDataTable implements ICellStore, Serializable {
 
 	protected final static int EOL = -1; // end of list
 	
 	protected transient Log log = LogFactory.getLog(getClass());
 	
 	protected List<IndexedData> indexData = new ArrayList<IndexedData>();
 	protected Map<Key, ICell> hashData = new HashMap<Key, ICell>();
 	
 	protected boolean indexDirty = true;
 	protected final int dimensionCount;
 
 	protected final int measureCount;
 	
 	protected int maxDepth = 0; 
 
 	protected class SearchContext {
 		Key key = null;
 		int measureIndex = 0;
 		Cell cell = null;
 		int rowIdx = 0;
 		int maxRow = 0;
 		int currIdx = 0;
 		IndexedData currId = null;
 		int ibScan = 0;
 		boolean wasMatch = false;
 		
 		long readCount = 0;
 		long seekCount = 0;
 		long bufferStart = 0;
 		int bufferPos = 0;
 		int bufferMax = 0;
 		byte[] buffer = null;
 		ByteArrayInputStream bufferIn = null;
 		DataInputStream objIn = null;
 
 	}
 	
 	/**
 	 * Constructor.
 	 * @param dimensionCount
 	 */
 	public IndexedDataTable(int dimensionCount, int measureCount) {
 		this.dimensionCount = dimensionCount;
 		this.measureCount = measureCount;
 		
 	}
 	
 	/**
 	 * Add a cell to the table.
 	 * @param key
 	 * @param cell
 	 */
 	public void put(Key key, ICell cell) {
 		ICell old = hashData.put(key, cell);
 		if (old != null) {
 			// the element did exist before. Find it in the index.
 			IndexedData id = findIndexedData(key);
 			if (id == null) {
 				throw new IllegalStateException("The indexData is out of sync with the hashData!");
 			}
 			id.setCell(cell); // just replace the cell.
 		} else {
 			indexData.add(new IndexedData(key, cell));
 			indexDirty = true;
 		}
 	}
 	
 	/**
 	 * @param key
 	 * @return
 	 */
 	private IndexedData findIndexedData(Key key) {
 		
 		for (IndexedData id : indexData) {
 			if (id.getKey().equals(key)) {
 				return id;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the element from the Data Table.
 	 * @param key
 	 */
 	public ICell get(Key key) {
 		return hashData.get(key);
 	}
 	
 	/**
 	 * Returns the value of the given search key.
 	 * @param key
 	 * @param measureIdx
 	 * @return
 	 */
 	public ICell calcCell(Key key) {
 
 		if (getIndexDataSize() == 0) {
 			return null; // exit if no elements..
 		}
 
 		if (key.isLeaf()) { // leafs can be "looked up" through the map
 			return getLeafValue(key);
 		}
 		
 		if (indexDirty) {
 			buildIndex(); // rebuild index
 		}
 
 		SearchContext ctx = new SearchContext();
 		ctx.key = key;
 		ctx.maxRow = getIndexDataSize();
 		ctx.rowIdx = 0;
 		ctx.cell = null;
 		ctx.currId = getStartIndexData(); // start with the first one
 		ctx.currIdx = ctx.currId == null ? -1 : 0;
 
 		onBeginScan(ctx);
 		// search the elements.
 		scanElements(ctx, 0, ctx.maxRow);
 		
 		onFinishedScan(ctx);
 		
 		//System.out.println("Entries touched: " + ctx.ibScan + " out of " + indexData.size());
 		
 		return ctx.cell;
 	}
 	
 	/**
 	 * @param ctx 
 	 * 
 	 */
 	protected void onFinishedScan(SearchContext ctx) {
 		
 	}
 
 	/**
 	 * @return
 	 */
 	protected IndexedData getStartIndexData() {
 		return indexData.get(0);
 	}
 
 	/**
 	 * @return
 	 */
 	protected int getIndexDataSize() {
 		return indexData.size();
 	}
 
 	/**
 	 * Sub-implemtations may prepare the scan process. 
 	 */
 	protected void onBeginScan(SearchContext ctx) {
 	
 	}
 
 	/**
 	 * @param key
 	 * @return
 	 */
 	protected ICell getLeafValue(Key key) {
 		
 		return hashData.get(key);
 
 	}
 
 	/**
 	 * @param ctx
 	 * @param i
 	 */
 	protected void scanElements(SearchContext ctx, int dimIdx, int max) {
 
 		IDimensionElement elm = ctx.key.getDimensionElement(dimIdx);
 		String[] searchPath = elm.getPathArray();
 
 		boolean hadMatch = false;
 		while (ctx.rowIdx != -1 && ctx.rowIdx < max) {
 
 			if (ctx.currIdx != ctx.rowIdx) { // only get IndexedData again if pointers have changed
 				ctx.currId = onScanElement(ctx);
 				ctx.currIdx = ctx.rowIdx;
 			}
 			Key otherKey = ctx.currId.getKey();
 			int[][] nextPnt = ctx.currId.getNextEntry();
 			
 			String[] idPath = otherKey.getDimensionElement(dimIdx).getPathArray();
 			// compare the keys to see if it is a match
 			boolean match = true;
 			
 			for (int ed = 0; ed < searchPath.length; ed++) {
 				if (idPath.length <= ed || !searchPath[ed].equals(idPath[ed])) { // can not match, as it is
 					if (hadMatch) {
 						// we have already had a match. This means that we now no longer
 						// have a match and can exit from that dimension depth
 						return; // HARD EXIT
 					} else {
 						// continue search on that level
 						match = false;
 						int newIdx = -1;
 						for (int reEd = Math.min(ed, idPath.length - 1); newIdx == -1 && reEd >= 0; reEd--) {
 							newIdx = nextPnt[dimIdx][reEd];
 						}
 						ctx.rowIdx = newIdx;
 						break;
 					}
 				}
 			}
 			ctx.wasMatch = match;
 			if (match) {
 				hadMatch = true;
 				if (dimIdx + 1 == dimensionCount) { // we are at the lowest level
 					//System.out.println("Match found for: " + otherKey);
 					if (ctx.cell == null) {
 						ctx.cell = new Cell(measureCount);
 					}
 					aggregateCells(ctx.cell, ctx.currId.getCell());
 					ctx.rowIdx++; // just read next line if we have had a match
 				} else {
 					// scan 
 					int subMax = searchPath.length > 0 ? nextPnt[dimIdx][searchPath.length - 1] : max; 
 					if (subMax == -1) {
 						subMax = max;
 					}
 					scanElements(ctx, dimIdx + 1, subMax);
 				}
			
 			}
 			
 		}
 		
 		
 	}
 
 
 	
 	/**
 	 * @param ctx
 	 * @return
 	 */
 	protected IndexedData onScanElement(SearchContext ctx) {
 		ctx.ibScan++;
 		return indexData.get(ctx.rowIdx);
 	}
 
 	/**
 	 * Aggregates the valueCell to the cell.
 	 * @param cell
 	 * @param valueCell
 	 */
 	protected void aggregateCells(ICell cell, ICell valueCell) {
 		for (int m = 0; m < measureCount; m++) {
 			Double value = valueCell.getValue(m); 
 			if (value != null) {
 				Double aggrValue = cell.getValue(m);  
 				if (aggrValue == null) {
 					cell.setValue(m, value);
 				} else {
 					cell.setValue(m, aggrValue + value);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Rebuild the index for fast non-leaf-search.
 	 */
 	public void buildIndex() {
 
 		System.out.println("BuildIndex");
 		maxDepth = 0;
 		
 		if (indexData.size() == 0) {
 			return; // nothing to do
 		}
 		
 		Collections.sort(indexData);
 		
 		int[][] pointers = new int[dimensionCount][50]; // max depth of 50 elements per dimension
 		int[] lastDepth = new int[dimensionCount];
 		String[][] lastKeys = new String[dimensionCount][50];
 		
 		// prefill pointers with -1, which means "end"
 		for (int di = 0; di < dimensionCount; di++) {
 			for (int elI = 0; elI < 50; elI++) {
 				pointers[di][elI] = EOL;
 				lastKeys[di][elI] = null;
 			}
 		}
 		int max = indexData.size();
 
 		Key lastKey = indexData.get(max - 1).getKey();
 		// populate lastKeys
 		for (int di = 0; di < dimensionCount; di++) {
 			IDimensionElement elm = lastKey.getDimensionElement(di);
 			lastDepth[di] = elm.getDepth();
 			int elI = lastDepth[di] - 1;
 			while (elI >= 0) {
 				String sKey = elm.getKey();
 				lastKeys[di][elI] = sKey;
 				elm = elm.getParent();
 				elI--;
 			}
 		}
 		
 		// walk through the list and build pointers to the "next" element switch
 		for (int idx = max - 1; idx >=0; idx--) {
 			
 			IndexedData id = indexData.get(idx);
 			Key key = id.getKey();
 			// compare all elements
 			int[][] nextEntry = new int[dimensionCount][0];
 			
 			boolean changed = false;
 			for (int di = 0; di < dimensionCount; di++) {
 				IDimensionElement elm = key.getDimensionElement(di);
 				int depth = elm.getDepth();
 				if (depth > maxDepth) {
 					maxDepth = depth;
 				}
 				
 				
 				String[] path = new String[depth];
 				for (int ed = depth - 1; ed >= 0; ed--) {
 					path[ed] = elm.getKey();
 					elm = elm.getParent();
 				}
 				// now walk forward. This is needed to properly recognize
 				// child key changes (i.e. 2010/Q1 vs. 2011/Q1)
 
 				// now copy the pointers
 				nextEntry[di] = new int[depth];
 				for (int ed = 0; ed < depth; ed++) {
 					if (changed || !path[ed].equals(lastKeys[di][ed])) {
 						// there is a change!
 						//if (changed) {
 						//	pointers[di][ed] = -1; // set subsequent elements to -1
 						//} else {
 							pointers[di][ed] = idx + 1; // set to previous record
 						//}
 						lastKeys[di][ed] = path[ed];
 						changed = true;
 					}
 					nextEntry[di][ed] = pointers[di][ed];
 				}
 				
 				
 			}
 			id.setNextEntry(nextEntry);
 			
 		}
 		indexDirty = false; // no longer dirty
 	}
 	
 	/**
 	 * This method is for debugging purposes and might be removed later on.
 	 */
 	public void dumpElements() {
 		
 		
 		int idx = 0;
 		for (IndexedData id : indexData) {
 			System.out.print((idx++) + " :");
 			Key key = id.getKey();
 			int[][] nextEntry = id.getNextEntry();
 			for (int di = 0; di < dimensionCount; di++) {
 				IDimensionElement elm = key.getDimensionElement(di);
 				int depth = elm.getDepth();
 				System.out.print(elm.getID());
 				if (nextEntry.length >= dimensionCount) {
 					System.out.print("(");
 					for (int ed = 0; ed < depth; ed++) {
 						if (ed > 0) {
 							System.out.print(",");
 						}
 						if (nextEntry[di].length >= ed) {
 							System.out.print(nextEntry[di][ed]);
 						} else {
 							System.out.print("?");
 						}
 					}
 					System.out.print(")|");
 				}
 			}
 			System.out.println("");
 		}
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.impl.ICellStore#clear()
 	 */
 	@Override
 	public void clear() {
 		indexData.clear();
 		hashData.clear();
 	}
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.impl.ICellStore#getKeyIterator()
 	 */
 	@Override
 	public Iterator<Key> getKeyIterator() {
 		return hashData.keySet().iterator();
 	}
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.impl.ICellStore#remove(de.xwic.cube.Key)
 	 */
 	@Override
 	public void remove(Key key) {
 		hashData.remove(key);
 
 		// TODO faster method (using fast-find methodology)
 		for (Iterator<IndexedData> it = indexData.iterator(); it.hasNext(); ) {
 			IndexedData id = it.next();
 			if (id.getKey().equals(key)) {
 				it.remove();
 				break;
 			}
 		}
 		indexDirty = true;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.impl.ICellStore#restore(java.io.ObjectInput, de.xwic.cube.IKeyProvider)
 	 */
 	@Override
 	public void restore(ObjectInput in, IKeyProvider keyProvider) throws IOException, ClassNotFoundException {
 
 		log = LogFactory.getLog(getClass());
 		
 		indexDirty = in.readBoolean();
 		int size = in.readInt();
 		hashData = new HashMap<Key, ICell>(size);
 		indexData = new ArrayList<IndexedData>(size);
 		for (int i = 0; i < size; i++) {
 			IndexedData id = new IndexedData();
 			id.restore(in, keyProvider, dimensionCount);
 			indexData.add(id);
 			hashData.put(id.getKey(), id.getCell());
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.impl.ICellStore#serialize(java.io.ObjectOutput)
 	 */
 	@Override
 	public void serialize(ObjectOutput out) throws IOException {
 		
 		if (hashData.size() != indexData.size()) {
 			throw new IllegalStateException("hashData and indexData out of sync!");
 		}
 		
 		out.writeBoolean(indexDirty);
 		out.writeInt(indexData.size());
 		
 		for (IndexedData id : indexData) {
 			id.serialize(out);
 		}
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.impl.ICellStore#size()
 	 */
 	@Override
 	public int size() {
 		return hashData.size();
 	}
 	
 }
