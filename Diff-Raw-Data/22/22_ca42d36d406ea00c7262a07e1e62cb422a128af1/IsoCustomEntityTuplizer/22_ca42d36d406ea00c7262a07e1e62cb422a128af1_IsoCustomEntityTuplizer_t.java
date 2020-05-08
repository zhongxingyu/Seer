 package gov.nih.nci.iso21090.hibernate.tuple;
 
 import gov.nih.nci.iso21090.Any;
 
 import org.hibernate.HibernateException;
 import org.hibernate.mapping.PersistentClass;
 import org.hibernate.tuple.entity.EntityMetamodel;
 import org.hibernate.tuple.entity.PojoEntityTuplizer;
 
 /**
  * Tuplizer responsible for setting nullFlavor and other constants at the class/entity level.
  * 
  * @author patelsat
  *
  */
 public class IsoCustomEntityTuplizer extends PojoEntityTuplizer {
 
     /**
      * Default constructor.
      * @param entityModel entityMetaModel
      * @param persistentClass persistentClass
      */
     public IsoCustomEntityTuplizer(EntityMetamodel entityModel, PersistentClass persistentClass) {
         super(entityModel, persistentClass);
     }
 
     /**
      * Sets the values of the properties. In our case the nullFlavor.
      * 
      * @param entity Name of the entity.
      * @param values values to be set.
      */
     @SuppressWarnings("PMD.CyclomaticComplexity")
     public void setPropertyValues(final Object entity, final Object[] values) {
         
         super.setPropertyValues(entity, values);
         
         try {
             String[] propertyNames = this.getEntityMetamodel().getPropertyNames();
             String entityName = this.getEntityMetamodel().getName();
             String identifierName = this.getEntityMetamodel().getIdentifierProperty().getName();
 
             Object identifier = this.getIdentifier(entity);
             
             IsoConstantTuplizerHelper helper = new IsoConstantTuplizerHelper();
             
             if (Any.class.isAssignableFrom(identifier.getClass())) {
                 helper.setConstantValues(entity, identifier, entityName, identifierName);
             }
             
             for (int i = 0; i < values.length; i++) {
 
                Class propertyTypeClass = helper.findFieldInClass(this.getMappedClass(), propertyNames[i]).getType();
 
                 if (Any.class.isAssignableFrom(propertyTypeClass)) {
                     helper.setConstantValues(entity, values[i], entityName, propertyNames[i]);
                 }
             }
             
         } catch (Exception e) {
             throw new HibernateException(e);
         }
     }
     
 
 }
