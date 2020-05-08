 package com.anasoft.os.daofusion.criteria;
 
 import org.hibernate.criterion.CriteriaSpecification;
 
 /**
  * Enumeration of possible database join types
  * applicable to {@link AssociationPathElement}
  * instances.
  * 
  * @see AssociationPathElement
  * @see NestedPropertyCriterion
  * 
  * @author vojtech.szocs
  */
 public enum NestedPropertyJoinType {
 
     DEFAULT(CriteriaSpecification.INNER_JOIN),
     INNER_JOIN(CriteriaSpecification.INNER_JOIN),
     FULL_JOIN(CriteriaSpecification.FULL_JOIN),
     LEFT_JOIN(CriteriaSpecification.LEFT_JOIN);
     
     private final int hibernateJoinType;
     
     NestedPropertyJoinType(int hibernateJoinType) {
         this.hibernateJoinType = hibernateJoinType;
     }
     
     public int getHibernateJoinType() {
         return hibernateJoinType;
     }
     
    public static NestedPropertyJoinType valueOf(int hibernateJoinType) {
        NestedPropertyJoinType[] values = NestedPropertyJoinType.values();
        
        for (NestedPropertyJoinType joinType : values) {
            if (joinType.getHibernateJoinType() == hibernateJoinType) {
                return joinType;
            }
        }
        
        throw new IllegalArgumentException("No enum value for hibernateJoinType " + hibernateJoinType);
    }
    
 }
