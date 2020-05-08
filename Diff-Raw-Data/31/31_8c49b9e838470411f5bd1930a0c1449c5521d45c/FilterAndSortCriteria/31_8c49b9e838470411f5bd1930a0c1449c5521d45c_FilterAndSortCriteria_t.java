 package com.anasoft.os.daofusion.cto.client;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Basic client-side persistent entity criteria
  * representation for a single property of the
  * target persistent entity.
  * 
  * <p>
  * 
  * This class is essentially an analogy of server-side
  * property criteria regarding the <em>filtering</em>
  * and <em>sorting</em> functionality.
  * 
  * <p>
  * 
  * Back on the server side, persistent entity property
  * mappings are used to resolve:
  * 
  * <ul>
  * 	<li>symbolic persistent entity property identifier
  * 		(<tt>propertyId</tt>) into the corresponding
  * 		<tt>propertyPath</tt>
  * 	<li>list of string-based filter values
  * 		(<tt>filterValues</tt>) into appropriate
  * 		typed object representations
  * </ul>
  * 
  * Note that the <tt>propertyId</tt> of each single
  * {@link FilterAndSortCriteria} instance must be
  * unique within the {@link CriteriaTransferObject}.
  * 
  * @see CriteriaTransferObject
  * 
  * @author vojtech.szocs
  */
 public class FilterAndSortCriteria implements Serializable {
 
    private static final long serialVersionUID = -593864722147943119L;
     
     private String propertyId;
 	
 	private List<String> filterValues = new ArrayList<String>();
 	
 	private Boolean sortAscending;
     private Boolean ignoreCase;
     
    /**
     * Creates a new persistent entity criteria
     * without specifying the property identifier.
     * 
     * <p>
     * 
     * This constructor serves for deserialization
     * purposes only and should NOT be used explicitly.
     */
    protected FilterAndSortCriteria() {
        // nothing to do here
    }
    
 	/**
 	 * Creates a new persistent entity criteria.
 	 * 
 	 * @param propertyId Symbolic persistent entity property
 	 * identifier.
 	 */
 	public FilterAndSortCriteria(String propertyId) {
 		this.propertyId = propertyId;
 	}
 	
 	/**
 	 * @return Symbolic persistent entity property identifier.
 	 */
 	public String getPropertyId() {
 		return propertyId;
 	}
 	
 	/**
 	 * @return Array of string-based filter values.
 	 */
 	public String[] getFilterValues() {
 		return filterValues.toArray(new String[0]);
 	}
 	
 	/**
	 * Clears any filter value(s) set previously
	 * to this criteria.
	 */
	public void clearFilterValues() {
        filterValues.clear();
	}
	
	/**
 	 * Sets a single filter value to this criteria,
 	 * replacing any filter value(s) set previously.
 	 * 
 	 * @param value String-based filter value.
 	 */
 	public void setFilterValue(String value) {
	    clearFilterValues();
 	    filterValues.add(value);
 	}
 	
 	/**
 	 * Sets multiple filter values to this criteria,
      * replacing any filter value(s) set previously.
      * 
 	 * @param values String-based filter values.
 	 */
 	public void setFilterValues(String... values) {
	    clearFilterValues();
 	    filterValues.addAll(Arrays.asList(values));
 	}
 	
 	/**
 	 * @return <tt>true</tt> for ascending, <tt>false</tt>
 	 * for descending sort order or <tt>null</tt> to disable
 	 * the sorting functionality.
 	 */
 	public Boolean getSortAscending() {
 		return sortAscending;
 	}
 	
 	/**
 	 * @param sortAscending <tt>true</tt> for ascending,
 	 * <tt>false</tt> for descending sort order or <tt>null</tt>
 	 * to disable the sorting functionality.
 	 */
 	public void setSortAscending(Boolean sortAscending) {
 		this.sortAscending = sortAscending;
 	}
 	
 	/**
 	 * @return <tt>true</tt> for case-insensitive sorting,
 	 * <tt>false</tt> for case-sensitive sorting (applicable
 	 * only when <tt>sortAscending</tt> is not <tt>null</tt>).
 	 */
 	public Boolean getIgnoreCase() {
 		return ignoreCase;
 	}
 	
 	/**
 	 * @param ignoreCase <tt>true</tt> for case-insensitive
 	 * sorting, <tt>false</tt> for case-sensitive sorting
 	 * (applicable only when <tt>sortAscending</tt> is not
 	 * <tt>null</tt>).
 	 */
 	public void setIgnoreCase(Boolean ignoreCase) {
 		this.ignoreCase = ignoreCase;
 	}
 	
 }
