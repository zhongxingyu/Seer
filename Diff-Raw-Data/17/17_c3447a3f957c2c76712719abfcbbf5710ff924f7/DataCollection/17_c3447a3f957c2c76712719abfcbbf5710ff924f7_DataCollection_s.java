 package com.megginson.sloop.model;
 
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A collection of data records (i.e. internal representation of a CSV file).
  * 
  * This represents tabular data: a list of lists, where the child lists are all
  * the same length. From a Java Collections point of view, this is a read-only
  * {@link List} of {@link DataRecord} objects. The list of headers is set at
  * creation time, and can't be changed afterwards. To add a row of new values,
  * use the {@link addRecord} objects.
  * 
  * The collection can filter itself to show only some of its members (or none at
  * all) using a {@link ValueFilter}. For example, to show only records where the
  * <code>foo</code> column has the value <code>bar</code>, you could use the
  * following code:
  * 
  * <pre>
  * dataCollection.putFilter("foo", new ValueFilter() {
  *   public boolean isMatch (value) { return "bar".equals(value); }
  * });
  * 
  * dataCollection.setFiltered(true);
  * </pre>
  * 
  * @author David Megginson
  * @see DataRecord
  */
 public class DataCollection extends AbstractList<DataRecord> {
 
 	private String mHeaders[];
 
 	private ValueFilter mFilters[];
 
 	private List<String[]> mRecords = new ArrayList<String[]>();
 
 	private List<Integer> mFilteredIndices = new ArrayList<Integer>();
 
 	private boolean mIsFiltered = false;
 
 	private boolean mIsCacheDirty = true;
 
 	/**
 	 * Default constructor.
 	 * 
 	 * Creates an empty collection with no filters.
 	 */
 	public DataCollection(String headers[]) {
 		super();
 		mHeaders = headers;
 		mFilters = new ValueFilter[headers.length];
 	}
 
 	/**
 	 * Indicate whether the collection is currently filtered.
 	 * 
 	 * @return true if the collection is filtered; false if it is unfiltered.
 	 * @see #setFiltered(boolean)
 	 * @see #putFilter(String, ValueFilter)
 	 */
 	public boolean isFiltered() {
 		return mIsFiltered;
 	}
 
 	/**
 	 * Toggle whether the collection is currently filtered.
 	 * 
 	 * Simply adding a filter with {@link #putFilter(String, ValueFilter)} does
 	 * not toggle filtering; it's necessary to call this method explicitly to
 	 * turn filtering on and off.
 	 * 
 	 * @param isFiltered
 	 *            true to enable filtering; false otherwise.
 	 * @see #isFiltered()
 	 * @see #putFilter(String, ValueFilter)
 	 */
 	public void setFiltered(boolean isFiltered) {
 		mIsFiltered = isFiltered;
 	}
	
 	/**
 	 * Indicate whether any filters are currently assigned.
 	 * 
 	 * @return true if there is at least one filter assigned.
 	 */
	public boolean hasFilters () {
 		for (ValueFilter filter : mFilters) {
 			if (filter != null) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Add a filter for one of the columns.
 	 * 
 	 * This method does not actually enable filtering; it's necessary to invoke
 	 * the {@link #setFiltered(boolean)} method explicitly to turn filtering on
 	 * and off.
 	 * 
 	 * @param name
 	 *            the column name to filter.
 	 * @param filter
 	 *            the filter object.
 	 * @see #getFilter(String)
 	 * @see #isFiltered()
 	 */
 	public void putFilter(String name, ValueFilter filter) {
 		mFilters[indexOf(name)] = filter;
 		mIsCacheDirty = true;
 	}
 
 	/**
 	 * Get the current filter for a column.
 	 * 
 	 * @param name
 	 *            the column name.
 	 * @return the filter if it exists, or null otherwise.
 	 * @see #putFilter(String, ValueFilter)
 	 */
 	public ValueFilter getFilter(String name) {
 		return mFilters[indexOf(name)];
 	}
 
 	/**
 	 * Add a row to the collection as a string array.
 	 * 
 	 * The class will convert this to a {@link DataRecord} for display as
 	 * required.
 	 * 
 	 * @param record
 	 *            an array of strings representing a row.
 	 */
 	public void addRecord(String record[]) {
 		mRecords.add(record.clone());
 		mIsCacheDirty = true;
 	}
 
 	/**
 	 * Very simple search function.
 	 * 
 	 * @param pattern
 	 *            A substring to search for (case-sensitive for now).
 	 * @param startingPosition
 	 *            The record for starting the search.
 	 * @return The number of the first record containing the pattern, or -1 if
 	 *         none was found.
 	 */
 	public int search(String pattern, int startingPosition) {
 		for (int i = startingPosition; i < mRecords.size(); i++) {
 			String record[] = mRecords.get(i);
 			for (String value : record) {
 				if (value.contains(pattern)) {
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 
 	@Override
 	public DataRecord get(int location) {
 		int rawLocation;
 		if (mIsFiltered) {
 			if (mIsCacheDirty) {
 				rebuildCache();
 			}
 			rawLocation = mFilteredIndices.get(location);
 		} else {
 			rawLocation = location;
 		}
 		return new DataRecord(mHeaders, mRecords.get(rawLocation), mFilters);
 	}
 
 	@Override
 	public int size() {
 		if (mIsFiltered) {
 			if (mIsCacheDirty) {
 				rebuildCache();
 			}
 			return mFilteredIndices.size();
 		} else {
 			return mRecords.size();
 		}
 	}
 
 	/**
 	 * Get the unfiltered size of the collection.
 	 * 
	 * This method will always return the total number of records
	 * in the collection, even if filtering is in force.
 	 * 
 	 * @return the total number of items in the (unfiltered) collection.
 	 * @see #size()
 	 */
 	public int sizeUnfiltered() {
 		return mRecords.size();
 	}
 
 	/**
 	 * Get the index of a header (first occurrence only).
 	 * 
 	 * @param header
 	 *            the header string.
 	 * @return the zero-based index, or -1 if not found.
 	 */
 	private int indexOf(String header) {
 		for (int i = 0; i < mHeaders.length; i++) {
 			if (header.equals(mHeaders[i])) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * Make a cached list of indices for a filtered view.
 	 */
 	private void rebuildCache() {
 		mFilteredIndices.clear();
 		for (int i = 0; i < mRecords.size(); i++) {
 			if (doFilterRecord(mRecords.get(i))) {
 				mFilteredIndices.add(i);
 			}
 		}
 		mIsCacheDirty = false;
 	}
 
 	/**
 	 * Run all appropriate filters against a single record.
 	 * 
 	 * @param record
 	 *            the record to filter (in its String array form).
 	 * @return true if the record matches the filter.
 	 */
 	private boolean doFilterRecord(String record[]) {
 		for (int i = 0; i < mHeaders.length; i++) {
			if (i < mFilters.length && i < record.length && mFilters[i] != null && !mFilters[i].isMatch(record[i])) {
				return false;
 			}
 		}
 		return true;
 	}
 
 }
