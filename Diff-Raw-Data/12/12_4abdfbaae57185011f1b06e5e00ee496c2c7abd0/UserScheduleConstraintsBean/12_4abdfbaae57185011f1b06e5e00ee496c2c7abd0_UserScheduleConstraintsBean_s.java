 package org.ffplanner.bean;
 
 import org.ffplanner.bean.constraints.AnyConstraintToggler;
 import org.ffplanner.bean.constraints.SpecificConstraintToggler;
 import org.ffplanner.entity.*;
 
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import java.io.Serializable;
 
 /**
  * @author Bogdan Dumitriu
  */
 @Stateless
 @LocalBean
 public class UserScheduleConstraintsBean extends ConnectorEntityBean<UserScheduleConstraint> implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     @Override
     public UserScheduleConstraint find(Long showingId, Long userScheduleId) {
         return super.find(showingId, userScheduleId);
     }
 
     @Override
     protected Class<UserScheduleConstraint> getEntityClass() {
         return UserScheduleConstraint.class;
     }
 
     @Override
     protected Predicate getLeftCondition(Long id, CriteriaBuilder criteriaBuilder, Root<UserScheduleConstraint> root) {
        return criteriaBuilder.equal(root.get(UserScheduleConstraints_.showing).get(Showing_.id), id);
     }
 
     @Override
     protected Predicate getRightCondition(
             Long id, CriteriaBuilder criteriaBuilder, Root<UserScheduleConstraint> root) {
        return criteriaBuilder.equal(root.get(UserScheduleConstraints_.userSchedule).get(UserSchedule_.id), id);
     }
 
     /**
      * If {@code constraintType} is set, it is removed. If no constraint is set or any other constraint is set,
      * {@code constraintType} becomes the new constraint.
      */
     public void toggleConstraint(Showing showing, UserSchedule userSchedule, ScheduleConstraintType constraintType) {
         new SpecificConstraintToggler(entityManager, showing, userSchedule).toggle(constraintType);
     }
 
     /**
      * If any constraint is set, it is removed. If no constraint is set, {@code constraintType} becomes the new
      * constraint.
      */
     public void toggleAnyConstraint(Showing showing, UserSchedule userSchedule, ScheduleConstraintType constraintType) {
         new AnyConstraintToggler(entityManager, showing, userSchedule).toggle(constraintType);
     }
 
     public boolean hasConstraint(Showing showing, UserSchedule userSchedule, ScheduleConstraintType constraintType) {
         final UserScheduleConstraint constraints = entityManager.find(
                 UserScheduleConstraint.class, new UserScheduleConstraintId(userSchedule.getId(), showing.getId()));
         return constraints != null && constraints.getConstraintType() == constraintType;
     }
 }
