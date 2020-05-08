 package com.anasoft.os.daofusion.criteria;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.Criteria;
 import org.hibernate.impl.CriteriaImpl;
 import org.hibernate.impl.CriteriaImpl.Subcriteria;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Register of {@link AssociationPath} instances and corresponding
  * Hibernate {@link Criteria} to be reused when modifying the root
  * {@link Criteria} instance.
  * 
  * <p>
  * 
  * This class is used by {@link NestedPropertyCriteria} to initialize
  * {@link Subcriteria} mappings in a safe way, avoiding the
  * <a href="http://opensource.atlassian.com/projects/hibernate/browse/HHH-879">
  * duplicate association path</a> Hibernate Criteria API issue.
  * 
  * <p>
  * 
  * {@link AssociationPathRegister} is a thin wrapper around the given
  * {@link Criteria} instance that gets initialized with existing
  * {@link Subcriteria} mappings at construction time. It is therefore
  * safe to create multiple {@link AssociationPathRegister} instances
  * operating on the same {@link Criteria}.
  * 
  * <p>
  * 
  * You can use this class to modify {@link Criteria} instances in a safe
  * way on your own as well (always prefer {@link #get(AssociationPath)} in
  * favor of calling the {@link Criteria#createCriteria(String, String, int)
  * createCriteria} method directly).
  * 
  * @see AssociationPath
  * @see NestedPropertyCriteria
  * 
  * @author michal.jemala
  * @author vojtech.szocs
  */
 public class AssociationPathRegister {
 
     private static final Logger LOG = LoggerFactory.getLogger(AssociationPathRegister.class);
     
 	private final Map<AssociationPath, Criteria> pathToCriteriaMap = new HashMap<AssociationPath, Criteria>();
 	
 	/**
 	 * Creates a new association path register, rooted
 	 * at the given {@link Criteria} instance.
 	 * 
 	 * <p>
 	 * 
 	 * Note that the register is initialized with Hibernate
 	 * {@link Criteria} mappings according to the current
 	 * state of <tt>rootCriteria</tt>.
 	 * 
 	 * @param rootCriteria Root {@link Criteria} instance.
 	 */
 	@SuppressWarnings("unchecked")
     public AssociationPathRegister(Criteria rootCriteria) {
 	    if (CriteriaImpl.class.isAssignableFrom(rootCriteria.getClass())) {
 	        Iterator<Subcriteria> subCriteriaIterator = CriteriaImpl.class.cast(rootCriteria).iterateSubcriteria();
 	        
 	        while (subCriteriaIterator.hasNext()) {
                 Subcriteria subCriteria = subCriteriaIterator.next();
                 AssociationPath associationPath = getSubCriteriaAssociationPath(subCriteria);
                 
                 pathToCriteriaMap.put(associationPath, subCriteria);
             }
 	    } else {
 	        LOG.warn("rootCriteria is not a Hibernate CriteriaImpl but {}", rootCriteria.getClass().getName());
 	    }
 	    
 		pathToCriteriaMap.put(new AssociationPath(), rootCriteria);
 	}
 	
 	/**
      * Returns an association path for the given <tt>subCriteria</tt>
      * by traversing its {@link Subcriteria} parents.
      * 
      * @param subCriteria {@link Subcriteria} instance to check.
      * @return {@link AssociationPath} for the given <tt>subCriteria</tt>.
      */
     private AssociationPath getSubCriteriaAssociationPath(Subcriteria subCriteria){
         List<AssociationPathElement> elementList = new ArrayList<AssociationPathElement>();
        elementList.add(new AssociationPathElement(subCriteria.getPath()));
         
         Subcriteria currentSubCriteria = subCriteria;
         
         while (currentSubCriteria.getParent() != null
                 && Subcriteria.class.isAssignableFrom(currentSubCriteria.getParent().getClass())) {
             currentSubCriteria = Subcriteria.class.cast(currentSubCriteria.getParent());
            elementList.add(0, new AssociationPathElement(currentSubCriteria.getPath()));
         }
         
         return new AssociationPath(elementList.toArray(new AssociationPathElement[0]));
     }
 	
 	/**
 	 * Returns a {@link Criteria} instance for the given
 	 * {@link AssociationPath}.
 	 * 
 	 * <p>
 	 * 
 	 * This method ensures that Hibernate {@link Criteria}
 	 * mappings are lazily initialized (with existing criteria
 	 * instances being reused) prior to returning the target
 	 * {@link Criteria}.
 	 * 
 	 * <p>
 	 * 
 	 * Resulting {@link Criteria} instances have unique aliases
 	 * (based on their association paths) so that they can be
 	 * referenced in complex Hibernate queries like this:
 	 * 
 	 * <pre>
 	 * Criteria criteria1 = apRegister.get(associationPath1);
 	 * String alias1 = criteria1.getAlias();
 	 * 
 	 * Criteria criteria2 = apRegister.get(associationPath2);
 	 * String alias2 = criteria2.getAlias();
 	 * 
 	 * rootCriteria.add(
 	 *     Restrictions.or(
 	 *         Restrictions.eq(alias1 + "." + targetPropertyName1, value1),
 	 *         Restrictions.eq(alias2 + "." + targetPropertyName2, value2)
 	 *     ));
 	 * </pre>
 	 * 
 	 * You can safely call this method multiple times with
      * same association path argument. Note that unused
      * association path criteria instances might break your
      * query behavior when using certain join types (e.g.
      * {@link NestedPropertyJoinType#INNER_JOIN inner join}).
      * 
 	 * @param associationPath Association path for which
 	 * to obtain the {@link Criteria} instance.
 	 * @return {@link Criteria} instance for the given
      * {@link AssociationPath}.
 	 */
 	public Criteria get(AssociationPath associationPath) {
 	    if (!pathToCriteriaMap.containsKey(associationPath)) {
     	    for (AssociationPath partialPath : associationPath) {
                 if (!pathToCriteriaMap.containsKey(partialPath)) {
                     AssociationPath superPath = partialPath.getSuperPath();
                     AssociationPathElement lastElement = partialPath.getLastElement();
                     
                     Criteria parentCriteria = pathToCriteriaMap.get(superPath);
                     Criteria criteria = parentCriteria.createCriteria(
                             lastElement.getValue(), partialPath.getAlias(),
                             lastElement.getJoinType().getHibernateJoinType());
                     
                     pathToCriteriaMap.put(partialPath, criteria);
                 }
             }
 	    }
 	    
 		return pathToCriteriaMap.get(associationPath);
 	}
 	
 }
