 package de.flower.common.validation.unique;
 
 
 import de.flower.common.jpa.IColumnResolver;
 import de.flower.common.logging.Slf4jUtil;
 import de.flower.common.model.BaseEntity;
 import de.flower.common.util.ReflectionUtil;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import javax.validation.ConstraintValidator;
 import javax.validation.ConstraintValidatorContext;
 import java.util.List;
 
 /**
  * @author oblume
  */
 public class UniqueValidator implements ConstraintValidator<Unique, BaseEntity> {
 
     private final static Logger log = Slf4jUtil.getLogger();
 
     private List<UniqueConstraintDef> constraints;
 
     private EntityManager em;
 
     /**
      * Need to create own EntityManager. Otherwise when validating during a save/update the database query
      * will force a flush of the unsaved entity -> null id error from hibernate.
      * So we need a separate session. However this opens the risk for phantom reads.
      *
      * @see http://community.jboss.org/wiki/AccessingTheHibernateSessionWithinAConstraintValidator
      */
     @Autowired
     private EntityManagerFactory emFactory;
 
     @Autowired
     private IColumnResolver columnResolver;
 
     private Class<?> entityClass;
 
     private boolean initialized = false;
 
     public void initialize(Unique constraintAnnotation) {
         this.constraints = UniqueConstraintDetector.convert(constraintAnnotation.constraints());
     }
 
     /**
      * Initializatin is delayed until the first validation should be made.
      * Reason: Only then we have access to the entityclass.
      *
      * @param entityClass
      */
     public void lazyInitialize(Class<?> entityClass) {
         if (this.entityClass == null) {
             this.entityClass = entityClass;
         }
         if (!initialized) {
 
             constraints.addAll(UniqueConstraintDetector.detectConstraints(entityClass));
             if (constraints.isEmpty()) {
                 log.warn("No uniqueness constraints defined in class[{}].", entityClass.getSimpleName());
             }
         }
         initialized = true;
     }
 
     @Override
     public boolean isValid(BaseEntity entity, ConstraintValidatorContext context) {
         lazyInitialize(entity.getClass());
         if (constraints.isEmpty()) {
             return true; // warnings of misconfiguration have been logged. just keep quiet and let bean validate.
         }
         try {
             boolean valid = true;
             em = emFactory.createEntityManager();
             ValidatorImpl validator = new ValidatorImpl(em, columnResolver);
             for (UniqueConstraintDef constraint : constraints) {
                 valid = valid && validator.isValid(entity, context, constraint);
             }
             return valid;
         } finally {
             em.close();
         }
     }
 
     public static class ValidatorImpl {
 
         private IColumnResolver columnResolver;
 
         private EntityManager em;
 
         public ValidatorImpl(EntityManager em, IColumnResolver columnResolver) {
             this.em = em;
             this.columnResolver = columnResolver;
         }
 
         public boolean isValid(BaseEntity entity, ConstraintValidatorContext context, UniqueConstraintDef constraint) {
             boolean valid = rowCount(entity, columnResolver.map2FieldNames(entity.getClass(), constraint.columnNames)).intValue() == 0;
             if (!valid) {
                 context.buildConstraintViolationWithTemplate("{de.flower.validation.constraints.unique.message." + constraint.name + "}")
                        .addNode(constraint.name).addConstraintViolation();
             }
             return valid;
         }
 
         private Long rowCount(BaseEntity entity, String[] fields) {
             Class<? extends BaseEntity> entityClass = entity.getClass();
             CriteriaBuilder cb = em.getCriteriaBuilder();
             CriteriaQuery<Long> query = cb.createQuery(Long.class);
             Root<? extends BaseEntity> root = query.from(entityClass);
             query.select(cb.count(root));
             Predicate condition = null;
             for (String field : fields) {
                 Predicate tmp = cb.equal(root.<Object>get(field), ReflectionUtil.getProperty(entity, field));
                 condition = (condition == null) ? tmp : cb.and(condition, tmp);
             }
             // update on existing element?
             if (!entity.isTransient()) {
                 condition = cb.and(condition, cb.notEqual(root.<Long>get("id"), "" + entity.getId()));
             }
             query.where(condition);
             return em.createQuery(query).getSingleResult();
         }
     }
 
 
 }
