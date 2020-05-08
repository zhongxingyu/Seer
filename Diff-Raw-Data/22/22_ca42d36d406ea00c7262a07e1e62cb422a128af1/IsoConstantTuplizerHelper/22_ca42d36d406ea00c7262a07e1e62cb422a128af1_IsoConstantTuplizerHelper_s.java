 package gov.nih.nci.iso21090.hibernate.tuple;
 
 import gov.nih.nci.iso21090.Any;
 import gov.nih.nci.iso21090.NullFlavor;
 import gov.nih.nci.iso21090.hibernate.node.ComplexNode;
 import gov.nih.nci.iso21090.hibernate.node.ConstantNode;
 import gov.nih.nci.iso21090.hibernate.node.Node;
 
 import java.lang.reflect.Field;
 import java.math.BigDecimal;
 import java.net.URI;
 import java.util.List;
 import java.util.Set;
 
 import org.hibernate.HibernateException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  * Helper class which reads the IsoConstants.xml file and sets the constant values in the 
  * classes loaded by Hibernate.
  * 
  * @author patelsat
  *
  */
 @SuppressWarnings("PMD.CyclomaticComplexity")
 public class IsoConstantTuplizerHelper {
 
     private static final String NULL_FLAVOR_ATTRIBUTE = "nullFlavor";
     private static ApplicationContext ctx = new ClassPathXmlApplicationContext("IsoConstants.xml");
 
     
     
     /**
      * Sets the nullFlavor in entity of propertyValue is null otherwise sets the constant values in the 
      * propertyValue. entityName and propertyName combination is used to locate the entry in the 
      * IsoConstants.xml file
      * @param entity Parent object in which nullflavor should be set if propertyValue is null
      * @param propertyValue object in which constants are to be set
      * @param entityName name of the entity
      * @param propertyName name of the property
      */
     public void setConstantValues(Object entity, Object propertyValue, String entityName, String propertyName) {
         ComplexNode complexNode = getComplexNodeBean(entityName, propertyName);
         setConstantValues(entity, propertyValue, complexNode);
     }
     
     @SuppressWarnings("PMD.CyclomaticComplexity")
     private void setConstantValues(Object parent, Object property, ComplexNode complexNode) {
         
         if (complexNode == null) {
             return;
         }
         
         if (property == null) {
             setNullFlavor(parent, complexNode);
         } else if (Any.class.isAssignableFrom(property.getClass()) && ((Any) property).getNullFlavor() == null) {
             return;
         } else {
             
             for (Node node : complexNode.getInnerNodes()) {
                 if ((node instanceof ConstantNode) && !(NULL_FLAVOR_ATTRIBUTE.equals(node.getName()))) {
                     setValue(property, node.getName(), ((ConstantNode) node).getInstance());
                 }
             }
             
             for (Node node : complexNode.getInnerNodes()) {
                 
                 if (node instanceof ComplexNode) {
                     
                     if (!node.getName().startsWith("part_")) {
                         Object existingObject = getPropertyObject(property, node.getName());
                         if (Any.class.isAssignableFrom(existingObject.getClass())) {
                             setConstantValues(property, existingObject, (ComplexNode) node);
                         } else if (Set.class.isAssignableFrom(existingObject.getClass())) {
                             for (Object obj : (Set) existingObject) {
                                 setConstantValues(property, obj, (ComplexNode) node);
                             }
                         }
                     } else  {
                         List partList = (List) getPropertyObject(property, "part");
                         Integer index = Integer.parseInt(node.getName().substring("part_".length()));
 
                         if (partList.size() < index) {
                             throw new HibernateException("Can not set constant as part.size()<part[index]");
                         }
                         setConstantValues(property, partList.get(index), (ComplexNode) node);
                     }
                 }
             }
         }
     }
 
 
 
     private Object getPropertyObject(Object parent, String propertyName) {
         try {
             
             Field propertyField = findFieldInClass(parent.getClass(), propertyName);
             propertyField.setAccessible(true);
             return propertyField.get(parent);
             
         } catch (SecurityException e) {
             throw new HibernateException(e);
         } catch (IllegalAccessException e) {
             throw new HibernateException(e);
         }
     }
 
     private void setValue(Object parent, String propertyName, Object instance) {
         try {
             
             Field propertyField = findFieldInClass(parent.getClass(), propertyName);        
             propertyField.setAccessible(true);
             Object converteValue = convertValue(instance, propertyField.getType()); 
             propertyField.set(parent, converteValue);
             
         } catch (SecurityException e) {
             throw new HibernateException(e);
         } catch (IllegalAccessException e) {
             throw new HibernateException(e);
         } catch (IllegalArgumentException e) {
             throw new HibernateException(e);
         }
     }
     
     private Object convertValue(Object value, Class targetType) {
         if (value == null) {
             return null;
         }
         if (targetType.isAssignableFrom(value.getClass())) {
             return value;
         }
         
         try {
             
             if (value.getClass() == String.class) {
                 if (Boolean.class == targetType) {
                     return Boolean.valueOf((String) value);
                 } else if (Integer.class == targetType) {
                     return Integer.valueOf((String) value);
                 } else if (BigDecimal.class == targetType) {
                     return BigDecimal.valueOf(Double.valueOf((String) value));
                 } else if (URI.class == targetType) {
                     return new URI((String) value);
                 }
             }
         } catch (Exception e) {
             throw new HibernateException("Error in converting String tyoe to " 
                     + targetType.getName()  + " for " + value, e);
         }
         throw new HibernateException("Can not convert Non-String type to target type of " 
                 + targetType.getName());
     }
 
     @SuppressWarnings("PMD.EmptyCatchBlock")
    private Field findFieldInClass(Class klass, String fieldName) {
         Class tempKlass = klass;
         while (tempKlass != null && Object.class != tempKlass) {
             try {
                 Field field = tempKlass.getDeclaredField(fieldName);
                 if (field != null) {
                     return field;
                 }
             } catch (SecurityException e) {
                 //No operation
             } catch (NoSuchFieldException e) {
                 //No operation                
             }
             
             tempKlass = tempKlass.getSuperclass();
         }
         throw new HibernateException("No field found in class " + klass.getName() + " for " + fieldName);
     }
     
     private Object intantiatePropertyObject(Object parent, String propertyName) {
         
         try {
             
            Field propertyField = parent.getClass().getDeclaredField(propertyName);
             Class propertyTypeClass = propertyField.getType();
             return propertyTypeClass.newInstance();
             
         } catch (SecurityException e) {
             throw new HibernateException(e);
        } catch (NoSuchFieldException e) {
            throw new HibernateException(e);
         } catch (IllegalAccessException e) {
             throw new HibernateException(e);
         } catch (InstantiationException e) {
             throw new HibernateException(e);
         }
     }    
 
     private void setNullFlavor(Object parent, ComplexNode rootNode) {
         NullFlavor nullFlavor = NullFlavor.MSK;
         for (Node node : rootNode.getInnerNodes()) {
             if (NULL_FLAVOR_ATTRIBUTE.equals(node.getName())) {
                 ConstantNode nullFlavorNode = (ConstantNode) node;
                 nullFlavor = (NullFlavor) nullFlavorNode.getInstance();
                 break;
             }
         }
 
         Object nullValueObject = intantiatePropertyObject(parent, rootNode.getName());
 
         if (!Any.class.isAssignableFrom(nullValueObject.getClass())) {
             return;
         }
         
         setValue(nullValueObject, NULL_FLAVOR_ATTRIBUTE, nullFlavor);
         setValue(parent, rootNode.getName(), nullValueObject);
     }
 
     /**
      * Returns the RootNode by reading the Spring configuration file.
      * 
      * @param dataTypeObject key in the file
      * @return RootNode instance or null
      */
     private ComplexNode getComplexNodeBean(String entityName, String propertyName) {
            String configLookupKey = entityName + "." + propertyName;
            return getComplexNodeBean(configLookupKey);
     }
 
     @SuppressWarnings("PMD.EmptyCatchBlock")
     private ComplexNode getComplexNodeBean(String configLookupKey) {
         
         try {
             return (ComplexNode) ctx.getBean(configLookupKey);
         } catch (Exception e) {
             //Do Nothing
         }
         return null;
     }
     
     /**
      * Sets the constant values for AD and EN part collection.
      * 
      * @param component object in which the null flavor will be set if the value is null
      * @param value object in which the constant values are to be stored
      * @param roleName name of the component used to locate constant entry in config file
      * @param propertyName name of the value property for which constants are to be set
      */
     @SuppressWarnings("PMD.CyclomaticComplexity")
     public void setConstantPartValues(Object component, Object value, String roleName, String propertyName)
     {
         ComplexNode complexNode = getComplexNodeBean(roleName);
         if (complexNode == null) {
             return;
         } else if (NULL_FLAVOR_ATTRIBUTE.equals(propertyName)) {
             setNullFlavor(component, complexNode);
         } else {
             if (value == null) {
                 return;
             }
             for (Node node : complexNode.getInnerNodes()) {
                 if (propertyName.equals(node.getName()) && node instanceof ComplexNode) {
                     setConstantValues(null, value, (ComplexNode) node);
                     return;
                 }
             }
         }
     }
 }
