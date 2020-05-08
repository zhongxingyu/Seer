 package com.anasoft.os.daofusion.criteria;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.Map.Entry;
 
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Order;
 import org.hibernate.impl.CriteriaImpl;
 import org.hibernate.impl.CriteriaImpl.Subcriteria;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.anasoft.os.daofusion.util.ReflectionHelper;
 import com.anasoft.os.daofusion.util.SimpleListContainer;
 
 /**
  * General purpose persistent entity criteria implementation
  * acting as container for {@link NestedPropertyCriterion}
  * instances.
  * 
  * <p>
  * 
  * This class implements the query constraint application
  * logic regarding specific {@link NestedPropertyCriterion}
  * subclasses via a default {@link NestedPropertyCriterionVisitor}
  * implementation. The {@link PersistentEntityCriteria#apply(Criteria)}
  * method implementation contains code that visits all
  * {@link NestedPropertyCriterion} instances, updating a single
  * {@link Criteria} instance within the visitor class.
  * 
  * <p>
  * 
  * Additionally, a nested persistent entity property criteria
  * contains features that are common to either all or certain
  * {@link NestedPropertyCriterion} instances:
  * 
  * <ul>
  * 	<li>paging criteria definition (<tt>firstResult</tt> and
  * 		<tt>maxResults</tt>)
  * 	<li>filter object which can be used as the source for filter
  * 		values (see {@link FilterCriterion} for more information
  * 		about the filter object and direct filter value concepts)
  * 	<li>the ability to preprocess a {@link Criteria} instance
  * 		regarding possible nested subcriteria in a generic way
  * 		to avoid the "duplicate association path" Hibernate Criteria
  * 		API issue
  * </ul>
  * 
  * @see NestedPropertyCriterion
  * @see NestedPropertyCriterionVisitor
  * @see PersistentEntityCriteria
  * 
  * @author vojtech.szocs
  */
 public class NestedPropertyCriteria extends SimpleListContainer<NestedPropertyCriterion> implements PersistentEntityCriteria {
 
 	private static final Logger LOG = LoggerFactory.getLogger(NestedPropertyCriteria.class);
 	
 	private Integer firstResult;
     private Integer maxResults;
 	
 	private Object filterObject;
 	
 	/**
 	 * @return Index of the starting element or <tt>null</tt>
 	 * representing no constraints on this paging parameter.
 	 */
 	public Integer getFirstResult() {
 		return firstResult;
 	}
 	
 	/**
 	 * @param firstResult Index of the starting element or
 	 * <tt>null</tt> representing no constraints on this
 	 * paging parameter.
 	 */
 	public void setFirstResult(Integer firstResult) {
 		this.firstResult = firstResult;
 	}
 	
 	/**
 	 * @return Maximum number of elements to return or
 	 * <tt>null</tt> representing no constraints on this
 	 * paging parameter.
 	 */
 	public Integer getMaxResults() {
 		return maxResults;
 	}
 	
 	/**
 	 * @param maxResults Maximum number of elements to return
 	 * or <tt>null</tt> representing no constraints on this
 	 * paging parameter.
 	 */
 	public void setMaxResults(Integer maxResults) {
 		this.maxResults = maxResults;
 	}
 	
 	/**
 	 * @return Filter object (can be <tt>null</tt>).
 	 */
 	public Object getFilterObject() {
 		return filterObject;
 	}
 	
 	/**
 	 * @param filterObject Filter object (can be <tt>null</tt>).
 	 */
 	public void setFilterObject(Object filterObject) {
 		this.filterObject = filterObject;
 	}
 	
 	/**
 	 * {@link Criteria} instance preprocessing regarding possible
 	 * nested subcriteria.
 	 * 
 	 * <p>
 	 * 
 	 * This kind of preprocessing is necessary in order to avoid
 	 * the "duplicate association path" Hibernate Criteria API
 	 * issue which leads to an exception in case of multiple
 	 * subcriteria with same association paths.
 	 * 
 	 * <p>
 	 * 
 	 * More information on this issue can be found at the following
	 * location: {@linkplain http://opensource.atlassian.com/projects/hibernate/browse/HHH-879}
 	 * 
 	 * @param targetCriteria {@link Criteria} instance to preprocess.
 	 * @return Map of association paths with corresponding subcriteria
 	 * instances created from the <tt>targetCriteria</tt>.
 	 */
 	protected Map<String, Criteria> preprocessSubCriteria(Criteria targetCriteria) {
 		final Map<String, Criteria> subCriteriaMap = new HashMap<String, Criteria>();
 		final Map<String, Integer> associationPathMap = new LinkedHashMap<String, Integer>();
 		
 		// extract association paths and corresponding join types to associationPathMap
 		final List<NestedPropertyCriterion> propertyCriterionList = getObjectList();
 		for (NestedPropertyCriterion propertyCriterion : propertyCriterionList) {
 			final String associationPath = propertyCriterion.getAssociationPath();
 			
 			if (associationPath != null) {
 				associationPathMap.put(associationPath, propertyCriterion.getAssociationJoinType().getHibernateJoinType());
 			}
 		}
 		
 		// populate subCriteriaMap according to associationPathMap
 		final Iterator<Entry<String, Integer>> associationPathEntryIterator = associationPathMap.entrySet().iterator();
 		while (associationPathEntryIterator.hasNext()) {
 			final Entry<String, Integer> associationPathEntry = associationPathEntryIterator.next();
 			final String associationPath = associationPathEntry.getKey();
 			
 			final Criteria subCriteria = findSubCriteria(associationPath, targetCriteria, associationPathEntry.getValue());
 			subCriteriaMap.put(associationPath, subCriteria);
 		}
 		
 		return subCriteriaMap;
 	}
 	
 	/**
 	 * Attempts to find a subcriteria with the given
 	 * <tt>associationPath</tt> within the <tt>targetCriteria</tt>.
 	 * 
 	 * <p>
 	 * 
 	 * If not found, the method creates a new subcriteria
 	 * rooted under the <tt>targetCriteria</tt> and returns
 	 * that subcriteria instance.
 	 * 
 	 * @param associationPath Association path of the subcriteria.
 	 * @param targetCriteria Root {@link Criteria} instance.
 	 * @param hibernateJoinType Hibernate join type to use
 	 * for nested subcriteria instances.
 	 * @return Subcriteria instance with the given
 	 * <tt>associationPath</tt> rooted under the <tt>targetCriteria</tt>.
 	 */
 	@SuppressWarnings("unchecked")
 	private Criteria findSubCriteria(String associationPath, Criteria targetCriteria, int hibernateJoinType) {
 		final StringTokenizer associationPathTokenizer = new StringTokenizer(associationPath, ".");
 		Criteria currentCriteria = targetCriteria;
 		
 		while (associationPathTokenizer.hasMoreTokens()) {
 			final String associationPathElement = associationPathTokenizer.nextToken();
 			boolean subCriteriaFound = false;
 			
 			// check if there is a subcriteria for associationPathElement within the targetCriteria
 			final Iterator<Subcriteria> subCriteriaIterator = ((CriteriaImpl) targetCriteria).iterateSubcriteria();
 			while (subCriteriaIterator.hasNext()) {
 				final Subcriteria subCriteriaImpl = subCriteriaIterator.next();
 				
 				if (associationPathElement.equals(subCriteriaImpl.getPath())) {
 					LOG.info("Found existing subcriteria with associationPath element '{}' within the targetCriteria - reusing this subcriteria", associationPathElement);
 					currentCriteria = subCriteriaImpl;
 					subCriteriaFound = true;
 					break;
 				}
 			}
 			
 			// if not found, create a new subcriteria rooted under the currentCriteria
 			if (!subCriteriaFound) {
 				LOG.info("Creating new subcriteria with associationPath element '{}' within the targetCriteria", associationPathElement);
 				currentCriteria = currentCriteria.createCriteria(associationPathElement, hibernateJoinType);
 			}
 		}
 		
 		return currentCriteria;
 	}
 	
 	/**
 	 * Returns a {@link NestedPropertyCriterionVisitor} instance
 	 * to be used within the {@link #apply(Criteria)} method.
 	 * 
 	 * @param targetCriteria {@link Criteria} instance to update.
 	 * @param subCriteriaMap Map of association paths with corresponding
 	 * subcriteria instances created from the <tt>targetCriteria</tt>.
 	 * @param filterObject Filter object (can be <tt>null</tt>).
 	 * @return {@link NestedPropertyCriterionVisitor} instance operating
 	 * on the <tt>targetCriteria</tt>.
 	 */
 	protected NestedPropertyCriterionVisitor getCriterionVisitor(Criteria targetCriteria, Map<String, Criteria> subCriteriaMap, Object filterObject) {
 	    return new DefaultNestedPropertyCriterionVisitor(targetCriteria, subCriteriaMap, filterObject);
 	}
 	
 	/**
 	 * @see com.anasoft.os.daofusion.criteria.PersistentEntityCriteria#apply(org.hibernate.Criteria)
 	 */
 	public final void apply(Criteria targetCriteria) {
 		final List<NestedPropertyCriterion> criterionList = getObjectList();
 		
 		final NestedPropertyCriterionVisitor visitor = getCriterionVisitor(
 				targetCriteria,
 				preprocessSubCriteria(targetCriteria),
 				filterObject);
 		
 		for (NestedPropertyCriterion criterion : criterionList) {
 			criterion.accept(visitor);
 		}
 		
 		applyPagingCriteria(targetCriteria);
 	}
 	
 	/**
 	 * Applies paging criteria to the <tt>targetCriteria</tt>.
 	 * 
 	 * @param targetCriteria {@link Criteria} instance to update.
 	 */
 	private void applyPagingCriteria(Criteria targetCriteria) {
 		if (firstResult != null) {
 			targetCriteria.setFirstResult(firstResult);
 		}
 		
 		if (maxResults != null) {
 			targetCriteria.setMaxResults(maxResults);
 		}
 	}
 	
 	/**
 	 * Default {@link NestedPropertyCriterionVisitor} implementation
 	 * which operates on a single {@link Criteria} instance.
 	 * 
 	 * @see NestedPropertyCriterionVisitor
 	 * 
 	 * @author vojtech.szocs
 	 */
 	public static class DefaultNestedPropertyCriterionVisitor implements NestedPropertyCriterionVisitor {
 
 	    protected final Criteria targetCriteria;
 	    protected final Map<String, Criteria> subCriteriaMap;
 	    
 	    protected final Object filterObject;
 	    
 	    /**
 	     * Creates a new nested property criterion visitor.
 	     * 
 	     * @param targetCriteria {@link Criteria} instance to update.
 	     * @param subCriteriaMap Map of association paths with corresponding
 	     * subcriteria instances created from the <tt>targetCriteria</tt>.
 	     * @param filterObject Filter object (can be <tt>null</tt>).
 	     */
 	    public DefaultNestedPropertyCriterionVisitor(Criteria targetCriteria, Map<String, Criteria> subCriteriaMap, Object filterObject) {
 	        this.targetCriteria = targetCriteria;
 	        this.subCriteriaMap = subCriteriaMap;
 	        this.filterObject = filterObject;
 	    }
 	    
 	    /**
 	     * @see com.anasoft.os.daofusion.criteria.NestedPropertyCriterionVisitor#visit(com.anasoft.os.daofusion.criteria.FilterCriterion)
 	     */
 	    public void visit(FilterCriterion criterion) {
 	        
 	        // resolve filterObjectValuePaths against the filterObject
 	        Object[] filterObjectValues = null;
 	        
 	        if (filterObject != null) {
 	            final String[] filterObjectValuePaths = criterion.getFilterObjectValuePaths();
 	            
 	            if (filterObjectValuePaths != null) {
 	                filterObjectValues = new Object[filterObjectValuePaths.length];
 	                
 	                for (int i = 0; i < filterObjectValuePaths.length; i++) {
 	                    filterObjectValues[i] = ReflectionHelper.resolvePropertyPath(filterObject, filterObjectValuePaths[i]);
 	                }
 	            }
 	        }
 	        
 	        // apply query constraints to the Criteria instance
 	        final PropertyFilterCriterionProvider criterionProvider = criterion.getFilterCriterionProvider();
             final Object[] directValues = criterion.getDirectValues();
             
 	        if (criterionProvider.enabled(filterObjectValues, directValues)) {
 	            final Criterion propertyFilterCriterion = criterionProvider.getCriterion(
 	                    criterion.getTargetPropertyName(),
 	                    filterObjectValues,
 	                    directValues);
 	            
 	            final String associationPath = criterion.getAssociationPath();
 	            
 	            if (associationPath != null) {
 	                subCriteriaMap.get(associationPath).add(propertyFilterCriterion);
 	            } else {
 	                targetCriteria.add(propertyFilterCriterion);
 	            }
 	        } else {
 	            LOG.info("Skipping FilterCriterion instance processing for propertyPath '{}' - associated filter criterion provider reports to be disabled", criterion.getPropertyPath());
 	        }
 	    }
 	    
 	    /**
 	     * @see com.anasoft.os.daofusion.criteria.NestedPropertyCriterionVisitor#visit(com.anasoft.os.daofusion.criteria.SortCriterion)
 	     */
 	    public void visit(SortCriterion criterion) {
 	        final String targetPropertyName = criterion.getTargetPropertyName();
 	        
 	        final Order order = criterion.isSortAscending() ? Order.asc(targetPropertyName) : Order.desc(targetPropertyName);
 	        
 	        if (criterion.isIgnoreCase()) {
 	            order.ignoreCase();
 	        }
 	        
 	        final String associationPath = criterion.getAssociationPath();
 	        
 	        if (associationPath != null) {
 	            subCriteriaMap.get(associationPath).addOrder(order);
 	        } else {
 	            targetCriteria.addOrder(order);
 	        }
 	    }
 	    
 	}
 	
 }
