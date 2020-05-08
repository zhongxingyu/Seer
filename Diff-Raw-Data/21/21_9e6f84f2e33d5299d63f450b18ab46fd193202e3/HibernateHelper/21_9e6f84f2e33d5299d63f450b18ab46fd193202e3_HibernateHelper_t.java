 package com.anasoft.os.daofusion.util;
 
 import java.util.Iterator;
 import java.util.StringTokenizer;
 
 import org.hibernate.Criteria;
 import org.hibernate.impl.CriteriaImpl;
 import org.hibernate.impl.CriteriaImpl.Subcriteria;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.anasoft.os.daofusion.criteria.NestedPropertyJoinType;
 
 /**
  * Helper class for Hibernate related stuff.
  * 
  * @author vojtech.szocs
  * @author janci.pastor
  */
 public final class HibernateHelper {
 
     private static final Logger LOG = LoggerFactory.getLogger(HibernateHelper.class);
     
     private HibernateHelper() {}
     
     /**
      * Attempts to find a subcriteria for the given
      * <tt>associationPath</tt> within the <tt>targetCriteria</tt>.
      * 
      * <p>
      * 
      * If not found, the method creates a new subcriteria
      * rooted under the <tt>targetCriteria</tt> (according
      * to the <tt>associationPath</tt>) and returns that
      * subcriteria instance.
      * 
      * <p>
      * 
      * Use this method to modify existing {@link Criteria}
      * instances in a safe way to avoid the
      * <a href="http://opensource.atlassian.com/projects/hibernate/browse/HHH-879">
      * duplicate association path</a> Hibernate Criteria API issue.
      * 
     * <p>
     * 
     * Note that the method returns <tt>null</tt> in case
     * <tt>targetCriteria</tt> is not an instance of
     * {@link CriteriaImpl} or {@link Subcriteria}
     * (iterating through {@link Subcriteria} is not
     * possible in that case).
     * 
      * @param targetCriteria Root {@link Criteria} instance.
      * @param associationPath Association path of the subcriteria.
      * @param hibernateJoinType Hibernate join type to use
      * for nested subcriteria instances.
      * @return Subcriteria instance for the given
      * <tt>associationPath</tt> rooted under the <tt>targetCriteria</tt>.
      */
     @SuppressWarnings("unchecked")
     public static Criteria findSubCriteria(Criteria targetCriteria, String associationPath, int hibernateJoinType) {
         final StringTokenizer associationPathTokenizer = new StringTokenizer(associationPath, ".");
         Criteria currentCriteria = targetCriteria;
         StringBuilder partialAssociationPath = new StringBuilder();
         
        Iterator<Subcriteria> subCriteriaIterator;
        if (targetCriteria instanceof CriteriaImpl) {
            subCriteriaIterator = ((CriteriaImpl) targetCriteria).iterateSubcriteria();
        } else if (targetCriteria instanceof Subcriteria) {
            subCriteriaIterator = ((CriteriaImpl) ((Subcriteria) targetCriteria).getParent()).iterateSubcriteria();
        } else {
            LOG.error("targetCriteria expected to be either CriteriaImpl or CriteriaImpl.Subcriteria instance, but was {}", targetCriteria.getClass().getName());
            return null;
        }
        
         while (associationPathTokenizer.hasMoreTokens()) {
             final String associationPathElement = associationPathTokenizer.nextToken();
             boolean subCriteriaFound = false;
             
             if (partialAssociationPath.length() > 0) {
                 partialAssociationPath.append(".");
             }
             partialAssociationPath.append(associationPathElement);
             
             // check if there is a subcriteria for associationPathElement within the targetCriteria
             while (subCriteriaIterator.hasNext()) {
                 final Subcriteria subCriteriaImpl = subCriteriaIterator.next();
                 
                 if (associationPathElement.equals(subCriteriaImpl.getPath())
                         && partialAssociationPath.toString().equals(getSubCriteriaPath(subCriteriaImpl))) {
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
      * Attempts to find a subcriteria for the given
      * <tt>associationPath</tt> within the <tt>targetCriteria</tt>
      * using the {@link NestedPropertyJoinType#DEFAULT default join type}.
      * 
      * @param targetCriteria Root {@link Criteria} instance.
      * @param associationPath Association path of the subcriteria.
      * @return Subcriteria instance for the given
      * <tt>associationPath</tt> rooted under the <tt>targetCriteria</tt>.
      * 
      * @see #findSubCriteria(Criteria, String, int)
      */
     public static Criteria findSubCriteria(Criteria targetCriteria, String associationPath) {
         return findSubCriteria(targetCriteria, associationPath, NestedPropertyJoinType.DEFAULT.getHibernateJoinType());
     }
     
     /**
      * Returns a property path for the given <tt>subCriteria</tt>,
      * starting from its root {@link Criteria} instance.
      * 
      * @param subCriteria {@link Subcriteria} instance representing
      * the target property.
      * @return Dot-separated logical path to the target property
      * for the given <tt>subCriteria</tt>, starting from its root
      * {@link Criteria} instance.
      */
     public static String getSubCriteriaPath(Subcriteria subCriteria){
         StringBuilder path = new StringBuilder(subCriteria.getPath());
         
         while (subCriteria.getParent() != null && subCriteria.getParent() instanceof Subcriteria) {
             subCriteria = (Subcriteria) subCriteria.getParent();
             path.insert(0, ".").insert(0, subCriteria.getPath());
         }
         
         return path.toString();
     }
     
 }
