 package search.exp.searchV32cc;
 
 import search.search32k.Hash;
 import search.search32k.TTEntry;
 
 public final class ZMap3 implements Hash{
 	private final TTEntry[] alwaysReplace;
 	private final TTEntry[] depthReplace;
 	private final int size;
 	
 	/**
 	 * creates a new ZMap
 	 * @param size hash map size, in powers of 2
 	 */
 	public ZMap3(int size){
 		this.size = size;
 		alwaysReplace = new TTEntry[1<<size];
 		depthReplace = new TTEntry[1<<size];
 		for(int i = 0; i < 1<<size; i++){
 			alwaysReplace[i] = new TTEntry();
 			depthReplace[i] = new TTEntry();
 		}
 	}
 	
 	public TTEntry get(long zkey){
 		final int index = (int)(zkey >>> (64-size));
 		TTEntry e = alwaysReplace[index];
 		if(e.zkey == zkey){
 			return e;
 		} else{
 			TTEntry d = depthReplace[index];
 			if(d.zkey == zkey){
 				return d;
 			}
 		}
 		return null;
 	}
 	
 	public void put(final long zkey, final TTEntry e){
 		final int index = (int)(zkey >>> (64-size));
 		final int seq = e.seq;
 		final TTEntry d = depthReplace[index];
 		if(d.seq != seq || e.depth < d.depth){
 			d.fill(e);
 		} else{
 			alwaysReplace[index].fill(e);
 		}
 	}
	
	public void clear(){assert false;}
 }
