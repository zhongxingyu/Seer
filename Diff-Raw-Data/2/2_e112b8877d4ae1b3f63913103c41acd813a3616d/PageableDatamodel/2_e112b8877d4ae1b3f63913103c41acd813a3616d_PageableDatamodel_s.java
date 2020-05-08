 package org.luca.medialib.jsf.model.rf;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.context.FacesContext;
 
 import org.ajax4jsf.model.DataVisitor;
 import org.ajax4jsf.model.ExtendedDataModel;
 import org.ajax4jsf.model.Range;
 import org.ajax4jsf.model.SequenceRange;
 import org.luca.medialib.domain.Identifiable;
 
 
 /**
  * Abstract pageable and data provider unaware datamodel.
  * Based on: http://gochev.blogspot.com/2009/08/richfaces-server-side-paging-with.html.
  * 
  * @author luc4
  */
 abstract class PageableDatamodel<T extends Identifiable> extends ExtendedDataModel<T> implements
 		Serializable
 {
 	private Long currentId;
 
 	private Integer rowCount;
 
 	private Range currentRange;
 
 	// TODO luc4: try to avoid two caches!
 	// needed because we need the cached data in guaranteed order.
 	private List<T> cache;
 
 	// needed because we want have a random accessible cache.
 	private Map<Long, T> mapCache = new HashMap<Long, T>();
 
 
 	/**
 	 * Returns a data page.
 	 * 
 	 * @param start
 	 * @param size
 	 * @return
 	 */
 	protected abstract List<T> getItemsRanged( int start, int size );
 
 
 	/**
 	 * Returns the total count for items.
 	 * 
 	 * @return
 	 */
 	protected abstract int getItemCount();
 
 
 	/**
 	 * Refresh the datamodel for the current range.
 	 */
 	public void refresh()
 	{
 		rowCount = getItemCount();
 		cache = getItemsRanged( currentRange );
 		mapCache.clear();
 		for ( T item : cache )
 		{
 			mapCache.put( item.getId(), item );
 		}
 	}
 
 
 	/**
 	 * This method never called from framework. (non-Javadoc)
 	 * 
 	 * @see org.ajax4jsf.model.ExtendedDataModel#getRowKey()
 	 */
 	@Override
 	public Object getRowKey()
 	{
 		return currentId;
 	}
 
 
 	/**
 	 * This method normally called by Visitor before request Data Row.
 	 */
 	@Override
 	public void setRowKey( Object key )
 	{
 		this.currentId = (Long) key;
 	}
 
 
 	/**
 	 * This is main part of Visitor pattern. Method called by framework many times during request
 	 * processing.
 	 */
 	@Override
 	public void walk( FacesContext context, DataVisitor visitor, Range range, Object argument )
 	{
 		if ( hasRangeChanged( currentRange, range ) )
 		{
 			cache = getItemsRanged( range );
 			mapCache.clear();
 			currentRange = range;
 		}
 
 		for ( T item : cache )
 		{
 			mapCache.put( item.getId(), item );
 			visitor.process( context, item.getId(), argument );
 		}
 	}
 
 
 	@Override
 	public int getRowCount()
 	{
 		if ( null == rowCount )
 		{
 			rowCount = getItemCount();
 		}
 		return rowCount;
 	}
 
 
 	/**
 	 * This is main way to obtain data row. It is intensively used by framework.
 	 * We strongly recommend use of local cache in that method.
 	 */
 	@Override
 	public T getRowData()
 	{
 		return mapCache.get( currentId );
 	}
 
 
 	/**
 	 * Never called by framework.
 	 */
 	@Override
 	public boolean isRowAvailable()
 	{
 		if ( null == currentId )
 		{
 			return false;
 		}
 		else
 		{
 			return (null != mapCache.get( currentId ));
 		}
 	}
 
 
 	/**
 	 * Unused rudiment from old JSF staff.
 	 */
 	@Override
 	public int getRowIndex()
 	{
		return 0;
 	}
 
 
 	/**
 	 * Unused rudiment from old JSF staff.
 	 */
 	@Override
 	public void setRowIndex( int rowIndex )
 	{
 		throw new UnsupportedOperationException();
 	}
 
 
 	/**
 	 * Unused rudiment from old JSF staff.
 	 */
 	@Override
 	public Object getWrappedData()
 	{
 		throw new UnsupportedOperationException();
 	}
 
 
 	/**
 	 * Unused rudiment from old JSF staff.
 	 */
 	@Override
 	public void setWrappedData( Object data )
 	{
 		throw new UnsupportedOperationException();
 	}
 
 
 	private List<T> getItemsRanged( Range currentRange )
 	{
 		int start = ((SequenceRange) currentRange).getFirstRow();
 		int size = ((SequenceRange) currentRange).getRows();
 		return getItemsRanged( start, size );
 	}
 
 
 	private boolean hasRangeChanged( Range currentRange, Range newRange )
 	{
 		if ( (null == currentRange && null != newRange)
 				|| (null != currentRange && null == newRange) )
 		{
 			return true;
 		}
 		else
 		{
 			SequenceRange s1 = (SequenceRange) currentRange;
 			SequenceRange s2 = (SequenceRange) newRange;
 			return s1.getFirstRow() != s2.getFirstRow() || s1.getRows() != s2.getRows();
 		}
 	}
 
 }
