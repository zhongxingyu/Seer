 /*******************************************************************************
  * Caleydo - Visualization for Molecular Biology - http://caleydo.org
  * Copyright (c) The Caleydo Team. All rights reserved.
  * Licensed under the new BSD license, available at http://caleydo.org/license
  ******************************************************************************/
 package org.caleydo.vis.lineup.model;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.AbstractList;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Deque;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Objects;
 
 import org.caleydo.core.util.function.DoubleSizedIterables;
 import org.caleydo.core.util.function.IDoubleSizedIterable;
 import org.caleydo.core.util.function.IDoubleSizedIterator;
 import org.caleydo.vis.lineup.config.IRankTableConfig;
 import org.caleydo.vis.lineup.data.IDoubleFunction;
 import org.caleydo.vis.lineup.model.mixin.ICompressColumnMixin;
 import org.caleydo.vis.lineup.model.mixin.IFilterColumnMixin;
 import org.caleydo.vis.lineup.model.mixin.IMappedColumnMixin;
 import org.caleydo.vis.lineup.model.mixin.IRankColumnModel;
 import org.caleydo.vis.lineup.model.mixin.IRankableColumnMixin;
 
 import com.google.common.collect.Iterables;
 
 /**
  * basic model abstraction of a ranked list
  *
  * @author Samuel Gratzl
  *
  */
 public final class RankTableModel implements IRankColumnParent, Cloneable {
 	public static final String PROP_SELECTED_ROW = "selectedRow";
 	public static final String PROP_COLUMNS = "columns";
 	public static final String PROP_POOL = "pool";
 	public static final String PROP_DESTROYED = "destroyed";
 	public static final String PROP_DATA = "data";
 	public static final String PROP_DATA_MASK = "datamask";
 	public static final String PROP_FILTER_INVALID = "invalidFilter";
 
 	private final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);
 
 	/**
 	 * current visible columns
 	 */
 	private List<ARankColumnModel> columns = new ArrayList<>();
 	/**
 	 * current hidden columns
 	 */
 	private List<ARankColumnModel> pool = new ArrayList<>(2);
 
 	private final PropertyChangeListener resort = new PropertyChangeListener() {
 		@Override
 		public void propertyChange(PropertyChangeEvent evt) {
 			findCorrespondingRanker((IRankColumnModel) evt.getSource()).dirtyOrder();
 		}
 	};
 	private final PropertyChangeListener refilter = new PropertyChangeListener() {
 		@Override
 		public void propertyChange(PropertyChangeEvent evt) {
 			refilter((IRankColumnModel) evt.getSource());
 		}
 	};
 
 	/**
 	 * settings
 	 */
 	private final IRankTableConfig config;
 	/**
 	 * the data of this table, not data can only be ADDED not removed, if you want to disable the use the
 	 * {@link #dataMask}
 	 */
 	private final List<IRow> data = new ArrayList<>();
 	/**
 	 * mask selecting a subset of the data
 	 */
 	private BitSet dataMask;
 
 	private IRow selectedRow = null;
 
 	/**
 	 * ranker used by default
 	 */
 	private final ColumnRanker defaultRanker;
 	/**
 	 *
 	 */
 	public RankTableModel(IRankTableConfig config) {
 		this.config = config;
 		this.defaultRanker = new ColumnRanker(this);
 	}
 
 
 	public RankTableModel(RankTableModel copy) {
 		this.config = copy.config;
 		this.selectedRow = copy.selectedRow;
 		this.dataMask = copy.dataMask;
 		this.data.addAll(copy.data);
 		this.defaultRanker = copy.defaultRanker.clone(this);
 		for(ARankColumnModel c : copy.pool)
 			this.pool.add(c.clone());
 		for(ARankColumnModel c : copy.columns)
 			this.columns.add(c.clone());
 	}
 
 	@Override
 	public RankTableModel clone() {
 		return new RankTableModel(this);
 	}
 
 	/**
 	 * removes all columns and clears the data
 	 */
 	public void reset() {
 		for (ARankColumnModel c : this.columns)
 			takeDown(c);
 		this.columns.clear();
 		for (ARankColumnModel c : this.pool)
 			takeDown(c);
 		this.pool.clear();
 		this.dataMask = null;
 		this.data.clear();
 		this.selectedRow = null;
 		// this.defaultRanker.reset();
 	}
 
 	/**
 	 * adds a collection of new data items to this table
 	 *
 	 * @param rows
 	 */
 	public void addData(Collection<? extends IRow> rows) {
 		if (rows == null || rows.isEmpty())
 			return;
 		int s = this.data.size();
 		for (IRow r : rows)
 			r.setIndex(s++);
 		this.data.addAll(rows);
 		propertySupport.fireIndexedPropertyChange(PROP_DATA, s, null, rows);
 		for (ColumnRanker order : findAllColumnRankers()) {
 			order.dirtyFilter();
 		}
 		//defaultFilter.dirtyFilter();
 	}
 
 	/**
 	 *
 	 */
 	void fireFilterInvalid() {
 		for (ARankColumnModel col : columns) {
 			col.onRankingInvalid();
 		}
 		for (ARankColumnModel m : this.pool)
 			m.onRankingInvalid();
 		propertySupport.firePropertyChange(PROP_FILTER_INVALID, false, true);
 		for (ColumnRanker order : findAllColumnRankers()) {
 			order.dirtyOrder();
 			order.getOrder();
 		}
 	}
 
 	public IRow getSelectedRow() {
 		return selectedRow;
 	}
 
 	/**
 	 * @param selectedRow
 	 *            setter, see {@link selectedRow}
 	 */
 	public void setSelectedRow(IRow selectedRow) {
 		propertySupport.firePropertyChange(PROP_SELECTED_ROW, this.selectedRow, this.selectedRow = selectedRow);
 	}
 
 	public void selectNextRow() {
 		System.out.println("select next");
 		if (selectedRow == null)
 			setSelectedRow(defaultRanker.selectFirst());
 		else
 			setSelectedRow(defaultRanker.selectNext(selectedRow));
 	}
 
 	public void selectPreviousRow() {
 		if (selectedRow == null)
 			return;
 		setSelectedRow(defaultRanker.selectPrevious(selectedRow));
 	}
 
 	/**
 	 * sets the data mask to filter the {@link #data}
 	 *
 	 * @param dataMask
 	 */
 	public void setDataMask(BitSet dataMask) {
 		if (Objects.equals(dataMask, this.dataMask))
 			return;
 		boolean change = true;
 		if (this.dataMask != null && dataMask != null) {
 			this.dataMask.xor(dataMask);
 			if (getDataSize() < this.dataMask.size())
 				this.dataMask.clear(getDataSize(), this.dataMask.size());
 			change = !this.dataMask.isEmpty(); // same data subset
 		}
 		propertySupport.firePropertyChange(PROP_DATA_MASK, dataMask, this.dataMask = (BitSet) dataMask.clone());
 		if (change) {
 			for (ColumnRanker r : findAllColumnRankers())
 				r.dirtyFilter();
 		}
 	}
 
 	/**
 	 * @return the dataMask, see {@link #dataMask}
 	 */
 	public BitSet getDataMask() {
 		return dataMask;
 	}
 
 	public void add(ARankColumnModel col) {
 		add(columns.size(), col);
 	}
 
 	public void add(int index, ARankColumnModel col) {
 		col.init(this);
 		setup(col);
 		this.columns.add(index, col); // intelligent positioning
 		propertySupport.fireIndexedPropertyChange(PROP_COLUMNS, index, null, col);
 		findCorrespondingRanker(index).checkOrderChanges(null, col);
 	}
 
 	@Override
 	public final void move(ARankColumnModel model, int to, boolean clone) {
 		int from = this.columns.indexOf(model);
 		if (!clone && model.getParent() == this && from >= 0) { // move within the same parent
 			if (from == to)
 				return;
 			ColumnRanker rOld = findCorrespondingRanker(from);
 			ColumnRanker rNew = findCorrespondingRanker(to);
 			columns.add(to, model);
 			columns.remove(from < to ? from : from + 1);
 			propertySupport.fireIndexedPropertyChange(PROP_COLUMNS, to, from, model);
 			if (rOld != rNew) {
 				rOld.checkOrderChanges(model, null);
 				rNew.checkOrderChanges(null, model);
 			} else
 				rOld.checkOrderChanges(null, model);
 		} else if (clone) {
 			add(to, model.clone());
 		} else {
 			model.getParent().remove(model);
 			add(to, model);
 		}
 	}
 
 	@Override
 	public boolean isMoveAble(ARankColumnModel model, int index, boolean clone) {
 		return config.isMoveAble(model, clone) && (clone || model.getParent().isHideAble(model))
 				&& !((model instanceof OrderColumn) && index == 0);
 	}
 
 	@Override
 	public void replace(ARankColumnModel from, ARankColumnModel to) {
 		int i = this.columns.indexOf(from);
 		columns.set(i, to);
 		to.init(this);
 		setup(to);
 		propertySupport.fireIndexedPropertyChange(PROP_COLUMNS, i, from, to);
 		from.takeDown();
 		takeDown(from);
 		findCorrespondingRanker(i).checkOrderChanges(from, to);
 	}
 
 	@Override
 	public int indexOf(ARankColumnModel model) {
 		return this.columns.indexOf(model);
 	}
 
 	public boolean isCombineAble(ARankColumnModel model, ARankColumnModel with, boolean clone, int combineMode) {
 		if (model == with)
 			return false;
 		if (model.getParent() == with || with.getParent() == model) // already children
 			return false;
 		if (!clone && !with.getParent().isHideAble(with)) // b must be hide able
 			return false;
 		return config.isCombineAble(model, with, clone, combineMode);
 	}
 
 	private void setup(ARankColumnModel col) {
 		col.addPropertyChangeListener(ARankColumnModel.PROP_WIDTH, resort);
 		col.addPropertyChangeListener(StackedRankColumnModel.PROP_WEIGHTS, resort);
 		col.addPropertyChangeListener(IMappedColumnMixin.PROP_MAPPING, refilter);
 		col.addPropertyChangeListener(IFilterColumnMixin.PROP_FILTER, refilter);
 	}
 
 	private void takeDown(ARankColumnModel col) {
 		col.removePropertyChangeListener(ARankColumnModel.PROP_WIDTH, resort);
 		col.removePropertyChangeListener(StackedRankColumnModel.PROP_WEIGHTS, resort);
 		col.removePropertyChangeListener(IMappedColumnMixin.PROP_MAPPING, refilter);
 		col.removePropertyChangeListener(IFilterColumnMixin.PROP_FILTER, refilter);
 	}
 
 	@Override
 	public void remove(ARankColumnModel model) {
 		int index = columns.indexOf(model);
 		if (index < 0) { // maybe in the pool
 			removeFromPool(model, false);
 			return;
 		}
 		ColumnRanker r = findCorrespondingRanker(index);
 		columns.remove(model);
 		propertySupport.fireIndexedPropertyChange(PROP_COLUMNS, index, model, null);
 		model.takeDown();
 		r.checkOrderChanges(model, null);
 	}
 
 	/**
 	 * @param model
 	 */
 	void addToPool(ARankColumnModel model) {
 		int bak = pool.size();
 		this.pool.add(model);
 		model.init(this);
 		model.setCollapsed(false);
 		propertySupport.fireIndexedPropertyChange(PROP_POOL, bak, null, model);
 	}
 
 	public void removeFromPool(ARankColumnModel model) {
 		removeFromPool(model, true);
 	}
 
 	/**
 	 * @param model
 	 * @param b
 	 */
 	private void removeFromPool(ARankColumnModel model, boolean destroy) {
 		int index = pool.indexOf(model);
 		if (index < 0)
 			return;
 		pool.remove(index);
 		propertySupport.fireIndexedPropertyChange(PROP_POOL, index, model, null);
 		model.takeDown();
 		if (destroy) {
 			propertySupport.firePropertyChange(PROP_DESTROYED, model, null);
 		}
 	}
 
 
 	@Override
 	public boolean hide(ARankColumnModel model) {
 		remove(model);
 		if (!config.isDestroyOnHide(model))
 			addToPool(model);
 		else {
 			propertySupport.firePropertyChange(PROP_DESTROYED, model, null);
 		}
 		return true;
 	}
 
 	@Override
 	public boolean isDestroyAble(ARankColumnModel model) {
 		return pool.contains(model); // just elements in the pool
 	}
 
 	@Override
 	public final boolean isCollapseAble(ARankColumnModel model) {
 		return config.isDefaultCollapseAble();
 	}
 
 	@Override
 	public boolean isHideAble(ARankColumnModel model) {
 		return config.isDefaultHideAble();
 	}
 
 	@Override
 	public boolean isHidden(ARankColumnModel model) {
 		return pool.contains(model);
 	}
 
 	@Override
 	public RankTableModel getTable() {
 		return this;
 	}
 
 	/**
 	 * explodes the given composite model into its components, i.e children
 	 */
 	@Override
 	public void explode(ACompositeRankColumnModel model) {
 		int index = this.columns.indexOf(model);
 		List<ARankColumnModel> children = model.getChildren();
		float w = (model instanceof ICompressColumnMixin || model instanceof GroupRankColumnModel) ? 100 : model
				.getWidth();
 		for (ARankColumnModel child : children) {
 			child.init(this);
 			child.setWidth(w); // reset width
 		}
 		this.columns.set(index, children.get(0));
 		propertySupport.fireIndexedPropertyChange(PROP_COLUMNS, index, model, children.get(0));
 		if (children.size() > 1) {
 			this.columns.addAll(index + 1, children.subList(1, children.size()));
 			propertySupport.fireIndexedPropertyChange(PROP_COLUMNS, index + 1, null,
 					children.subList(1, children.size()));
 		}
 		//if (!defaultFilter.checkFilterChanges(model, children.get(0)))
 		//findCorrespondingRanker(index).checkOrderChanges(model, children.get(0));
 		findCorrespondingRanker(index).checkOrderChanges(model, children.get(0));
 	}
 
 	/**
 	 * @return the columns, see {@link #columns}
 	 */
 	public List<ARankColumnModel> getColumns() {
 		return Collections.unmodifiableList(columns);
 	}
 
 	public List<ARankColumnModel> getFlatColumns() {
 		List<ARankColumnModel> r = new ArrayList<>(columns.size());
 		Deque<ARankColumnModel> cols = new LinkedList<>(columns);
 
 		while (!cols.isEmpty()) {
 			ARankColumnModel model = cols.pollFirst();
 			r.add(model);
 			if (model instanceof ACompositeRankColumnModel) {
 				cols.addAll(((ACompositeRankColumnModel) model).getChildren());
 			}
 		}
 		return r;
 	}
 
 	/**
 	 * @return the pool, see {@link #pool}
 	 */
 	public List<ARankColumnModel> getPool() {
 		return Collections.unmodifiableList(pool);
 	}
 
 	public final void addPropertyChangeListener(PropertyChangeListener listener) {
 		propertySupport.addPropertyChangeListener(listener);
 	}
 
 	public final void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
 		propertySupport.addPropertyChangeListener(propertyName, listener);
 	}
 
 	public final void removePropertyChangeListener(PropertyChangeListener listener) {
 		propertySupport.removePropertyChangeListener(listener);
 	}
 
 	public final void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
 		propertySupport.removePropertyChangeListener(propertyName, listener);
 	}
 
 	public List<IRow> getData() {
 		return Collections.unmodifiableList(this.data);
 	}
 
 	public List<IRow> getDataModifiable() {
 		return this.data;
 	}
 
 	public List<IRow> getMaskedData() {
 		if (dataMask == null || dataMask.cardinality() == data.size())
 			return getData();
 		final int[] lookup = new int[dataMask.cardinality()];
 		int j = 0;
 		for (int i = dataMask.nextSetBit(0); i >= 0; i = dataMask.nextSetBit(i + 1)) {
 			lookup[j++] = i;
 		}
 		return new AbstractList<IRow>() {
 			@Override
 			public IRow get(int index) {
 				return lookup == null ? data.get(index) : data.get(lookup[index]);
 			}
 
 			@Override
 			public int size() {
 				return lookup == null ? data.size() : lookup.length;
 			}
 		};
 	}
 
 	/**
 	 * return a view on the filtered data mapped to doubles
 	 *
 	 * @param f
 	 * @return
 	 */
 	public IDoubleSizedIterable getFilteredMappedData(final IDoubleFunction<IRow> f) {
 		if (dataMask == null || dataMask.cardinality() == data.size())
 			return map(getData(), f);
 
 		return new IDoubleSizedIterable() {
 			@Override
 			public int size() {
 				return dataMask.cardinality();
 			}
 
 			@Override
 			public IDoubleSizedIterable map(org.caleydo.core.util.function.IDoubleFunction f) {
 				return DoubleSizedIterables.map(this, f);
 			}
 
 			@Override
 			public IDoubleSizedIterator iterator() {
 				return new IDoubleSizedIterator() {
 					int i = dataMask.nextSetBit(0);
 
 					@Override
 					public void remove() {
 						throw new UnsupportedOperationException();
 					}
 
 					@Override
 					public Double next() {
 						return nextPrimitive();
 					}
 
 					@Override
 					public boolean hasNext() {
 						return i >= 0;
 					}
 
 					@Override
 					public double nextPrimitive() {
 						int act = i;
 						i = dataMask.nextSetBit(i + 1);
 						return f.applyPrimitive(data.get(act));
 					}
 
 					@Override
 					public int size() {
 						return dataMask.cardinality();
 					}
 				};
 			}
 		};
 	}
 
 	/**
 	 * @param data2
 	 * @param f
 	 * @return
 	 */
 	private static IDoubleSizedIterable map(final List<IRow> data2, final IDoubleFunction<IRow> f) {
 		return new IDoubleSizedIterable() {
 
 			@Override
 			public int size() {
 				return data2.size();
 			}
 
 			@Override
 			public IDoubleSizedIterable map(org.caleydo.core.util.function.IDoubleFunction f) {
 				return DoubleSizedIterables.map(this, f);
 			}
 			@Override
 			public IDoubleSizedIterator iterator() {
 				return new IDoubleSizedIterator() {
 					private final Iterator<IRow> it = data2.iterator();
 
 					@Override
 					public void remove() {
 						throw new UnsupportedOperationException();
 					}
 
 					@Override
 					public Double next() {
 						return nextPrimitive();
 					}
 
 					@Override
 					public boolean hasNext() {
 						return it.hasNext();
 					}
 
 					@Override
 					public double nextPrimitive() {
 						return f.applyPrimitive(it.next());
 					}
 
 					@Override
 					public int size() {
 						return data2.size();
 					}
 				};
 			}
 		};
 	}
 
 	protected void refilter(IRankColumnModel source) {
 		for (ColumnRanker r : findAllColumnRankers())
 			r.dirtyFilter();
 		// findCorrespondingRanker((IRankColumnModel) evt.getSource()).dirtyFilter();// TODO Auto-generated method stub
 	}
 
 	public int getDataSize() {
 		return this.data.size();
 	}
 
 
 
 	private ColumnRanker findCorrespondingRanker(IRankColumnModel model) {
 		if (model == null)
 			return findCorrespondingRanker(-1);
 		while (model.getParent() != this)
 			model = model.getParent();
 		return findCorrespondingRanker(columns.indexOf(model));
 	}
 
 	public ColumnRanker getPreviousRanker(ColumnRanker ranker) {
 		if (ranker == defaultRanker)
 			return null;
 		ColumnRanker previous = defaultRanker;
 		for (ARankColumnModel col : columns) {
 			if (col instanceof OrderColumn) {
 				ColumnRanker r = ((OrderColumn) col).getRanker();
 				if (r == ranker)
 					return previous;
 				previous = r;
 			}
 		}
 		return previous;
 	}
 
 	private ColumnRanker findCorrespondingRanker(int index) {
 		if (index <= 0)
 			return defaultRanker;
 		for (ListIterator<ARankColumnModel> it = columns.listIterator(index); it.hasPrevious();) {
 			ARankColumnModel m = it.previous();
 			if (m instanceof OrderColumn)
 				return ((OrderColumn) m).getRanker();
 		}
 		return defaultRanker;
 	}
 
 	private Iterable<ColumnRanker> findAllColumnRankers() {
 		List<ColumnRanker> r = new ArrayList<>();
 		for (ARankColumnModel col : columns)
 			if (col instanceof OrderColumn)
 				r.add(((OrderColumn) col).getRanker());
 		return Iterables.concat(Collections.singleton(defaultRanker), r);
 	}
 
 	@Override
 	public ColumnRanker getMyRanker(IRankColumnModel model) {
 		return findCorrespondingRanker(model);
 	}
 
 	/**
 	 * @return the config, see {@link #config}
 	 */
 	public IRankTableConfig getConfig() {
 		return config;
 	}
 
 	@Override
 	public IRankColumnParent getParent() {
 		return null;
 	}
 
 	@Override
 	public String getTitle() {
 		return "RankTable";
 	}
 
 	/**
 	 * @param columnRanker
 	 */
 	void fireRankingInvalidOf(ColumnRanker ranker) {
 		int start = getStartIndex(ranker);
 		for (ListIterator<ARankColumnModel> it = columns.listIterator(start); it.hasNext();) {
 			ARankColumnModel m = it.next();
 			if (m instanceof OrderColumn)
 				break;
 			m.onRankingInvalid();
 		}
 		if (ranker == defaultRanker) {
 			for (ARankColumnModel m : this.pool)
 				m.onRankingInvalid();
 		}
 	}
 
 	public Iterator<ARankColumnModel> getColumnsOf(ColumnRanker ranker) {
 		int start = getStartIndex(ranker);
 		if (start >= columns.size())
 			return Collections.emptyIterator();
 
 		List<ARankColumnModel> r = new ArrayList<>(columns.size() - start);
 		for (ListIterator<ARankColumnModel> it = columns.listIterator(start); it.hasNext();) {
 			ARankColumnModel m = it.next();
 			if (m instanceof OrderColumn)
 				break;
 			r.add(m);
 		}
 		return r.iterator();
 	}
 
 	public Iterator<IFilterColumnMixin> findAllMyFilteredColumns(ColumnRanker ranker) {
 		int start = getStartIndex(ranker);
 		List<IFilterColumnMixin> r = new ArrayList<>();
 
 		int i = -1;
 		boolean ended = false;
 		for (ARankColumnModel col : columns) {
 			i++;
 			boolean within = (i >= start && !ended);
 			if (col instanceof IFilterColumnMixin) {
 				IFilterColumnMixin f = (IFilterColumnMixin)col;
 				if (f.isGlobalFilter() || within)
 					r.add(f);
 			}
 			if (col instanceof ACompositeRankColumnModel) {
 				findAllFlatFilteredColumns((ACompositeRankColumnModel) col, within, r);
 			}
 			if (col instanceof OrderColumn && within) {
 				ended = true;
 			}
 		}
 
 		return r.iterator();
 	}
 
 	private void findAllFlatFilteredColumns(ACompositeRankColumnModel composite, boolean within,
 			List<IFilterColumnMixin> r) {
 		for (ARankColumnModel col : composite) {
 			if (col instanceof IFilterColumnMixin) {
 				IFilterColumnMixin f = (IFilterColumnMixin) col;
 				if (f.isGlobalFilter() || within)
 					r.add(f);
 			}
 			if (col instanceof ACompositeRankColumnModel) {
 				findAllFlatFilteredColumns((ACompositeRankColumnModel) col, within, r);
 			}
 		}
 	}
 
 	private int getStartIndex(ColumnRanker ranker) {
 		int start = 0;
 		if (ranker != defaultRanker) { // find the start
 			for (ARankColumnModel col : columns) {
 				start++;
 				if ((col instanceof OrderColumn) && ((OrderColumn) col).getRanker() == ranker)
 					break;
 			}
 		}
 		return start;
 	}
 
 	/**
 	 * @return the defaultRanker, see {@link #defaultRanker}
 	 */
 	public ColumnRanker getDefaultRanker() {
 		return defaultRanker;
 	}
 
 	public IRow getDataItem(int index) {
 		return data.get(index);
 	}
 
 	@Override
 	public void orderBy(IRankableColumnMixin model) {
 		getMyRanker(model).orderBy(model);
 	}
 
 	/**
 	 * adds a snapshot of the given column including a separator and a rank column
 	 *
 	 * @param model
 	 */
 	public void addSnapshot(ARankColumnModel model) {
 		add(new OrderColumn());
 		for (ARankColumnModel col : config.createAutoSnapshotColumns(this, model))
 			add(col);
 		if (model != null) {
 			ARankColumnModel clone = model.clone();
 			add(clone);
 			if (clone instanceof IRankableColumnMixin)
 				((IRankableColumnMixin) clone).orderByMe();
 		}
 	}
 
 	/**
 	 * whether the table contains any snapshots
 	 *
 	 * @return
 	 */
 	public boolean hasSnapshots() {
 		return getFirstSnapshot() < columns.size();
 	}
 
 	/**
 	 * returns the first index of an order columns
 	 *
 	 * @return
 	 */
 	public int getFirstSnapshot() {
 		for (int i = 0; i < columns.size(); ++i)
 			if (columns.get(i) instanceof OrderColumn)
 				return i;
 		return columns.size();
 	}
 
 	public void dirtyAllOrders() {
 		for (ColumnRanker ranker : findAllColumnRankers()) {
 			ranker.dirtyOrder();
 			ranker.order();
 		}
 
 	}
 
 	static class FlatIterator implements Iterator<ARankColumnModel> {
 		private Deque<Iterator<ARankColumnModel>> stack = new ArrayDeque<>(3);
 
 		public FlatIterator(Iterator<ARankColumnModel> it) {
 			this.stack.push(it);
 		}
 
 		@Override
 		public boolean hasNext() {
 			while (!stack.isEmpty() && !stack.peekLast().hasNext())
 				stack.pollLast();
 			return !stack.isEmpty();
 		}
 
 		@Override
 		public ARankColumnModel next() {
 			ARankColumnModel m = stack.peekLast().next();
 			if (m instanceof ACompositeRankColumnModel) {
 				ACompositeRankColumnModel c = (ACompositeRankColumnModel) m;
 				stack.push(c.iterator());
 			}
 			return m;
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 
 	}
 
 }
 
