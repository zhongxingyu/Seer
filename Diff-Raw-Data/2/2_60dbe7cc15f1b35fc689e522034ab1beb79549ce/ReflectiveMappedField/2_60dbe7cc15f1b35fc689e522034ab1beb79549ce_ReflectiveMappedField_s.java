 package net.jps.jx.mapping.reflection;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import net.jps.jx.annotation.JsonField;
 import net.jps.jx.mapping.MappedField;
 import net.jps.jx.jaxb.JaxbConstants;
 import net.jps.jx.util.reflection.ReflectionException;
 
 /**
  *
  * @author zinic
  */
 public class ReflectiveMappedField implements MappedField {
 
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private final XmlAttribute xmlAttributeAnnotation;
    private final XmlElement xmlElementAnnotation;
    private final JsonField jsonFieldAnnotation;
    private final Class instanceClass;
    private final Object instanceRef;
    private final Field fieldRef;
    private final Method getterRef, setterRef;
 
    public ReflectiveMappedField(Field fieldRef, Object instanceRef) {
       this.fieldRef = fieldRef;
       this.instanceRef = instanceRef;
 
       instanceClass = instanceRef.getClass();
 
       xmlElementAnnotation = (XmlElement) fieldRef.getAnnotation(XmlElement.class);
       xmlAttributeAnnotation = (XmlAttribute) fieldRef.getAnnotation(XmlAttribute.class);
       jsonFieldAnnotation = (JsonField) fieldRef.getAnnotation(JsonField.class);
 
       getterRef = findGetter();
       setterRef = findSetter();
    }
 
    @Override
    public Class getFieldType() {
       return fieldRef.getType();
    }
 
    @Override
    public boolean hasGetter() {
       return getterRef != null;
    }
 
    @Override
    public boolean hasSetter() {
       return setterRef != null;
    }
 
    protected Field getFieldRef() {
       return fieldRef;
    }
 
    protected Class getInstanceClass() {
       return instanceClass;
    }
 
    protected Object getInstanceRef() {
       return instanceRef;
    }
 
    @Override
    public String getName() {
       return jsonFieldAnnotation != null ? jsonFieldAnnotation.value() : getJaxbNameForField();
    }
 
    @Override
    public boolean canSet() {
       return setterRef != null;
    }
 
    private String getJaxbNameForField() {
       if (xmlAttributeAnnotation != null) {
          return JaxbConstants.JAXB_DEFAULT_NAME.equals(xmlAttributeAnnotation.name()) ? fieldRef.getName() : xmlAttributeAnnotation.name();
       }
 
       if (xmlElementAnnotation != null) {
          return JaxbConstants.JAXB_DEFAULT_NAME.equals(xmlElementAnnotation.name()) ? fieldRef.getName() : xmlElementAnnotation.name();
       }
 
       return fieldRef.getName();
    }
 
    private Method findGetter() {
      final String getterMethodName = formatGetterMethodName(fieldRef.getName(), instanceClass);
 
       for (Method method : instanceClass.getMethods()) {
          if (method.getName().equals(getterMethodName)) {
             return method;
          }
       }
 
       throw new ReflectionException("Unable to find getter method: " + getterMethodName + "() for field: " + fieldRef.getName() + " with class: " + instanceClass.getName());
    }
 
    private Method findSetter() {
       final String setterMethodName = formatSetterMethodName(fieldRef.getName());
 
       for (Method method : instanceClass.getMethods()) {
          if (method.getName().equals(setterMethodName)) {
             return method;
          }
       }
 
       return null;
    }
 
    private String formatGetterMethodName(String name, Class fieldType) {
       final StringBuilder methodNameBuilder = formatFieldName(name);
 
       final String getterPrefix = fieldType.equals(boolean.class) || fieldType.equals(Boolean.class) ? "is" : "get";
       methodNameBuilder.insert(0, getterPrefix);
 
       return methodNameBuilder.toString();
    }
 
    private String formatSetterMethodName(String name) {
       final StringBuilder methodNameBuilder = formatFieldName(name);
       methodNameBuilder.insert(0, "set");
 
       return methodNameBuilder.toString();
    }
 
    private StringBuilder formatFieldName(String name) {
       final StringBuilder nameBuilder = new StringBuilder(name);
 
       // JAXB espcaes field names that clash with reserved keywords with a '_' character
       if (nameBuilder.charAt(0) == '_') {
          nameBuilder.deleteCharAt(0);
       }
 
       final char fieldNameFirstChar = nameBuilder.charAt(0);
 
       if (Character.isLowerCase(fieldNameFirstChar)) {
          nameBuilder.setCharAt(0, Character.toUpperCase(fieldNameFirstChar));
       }
 
       return nameBuilder;
    }
 
    @Override
    public Object get() {
       return invoke(instanceRef, getterRef);
    }
 
    @Override
    public void set(Object value) {
       if (setterRef == null) {
          throw new IllegalStateException("No setter found for field: " + fieldRef.getName() + "::" + fieldRef.getType().getName() + " on class: " + instanceClass.getName());
       }
 
       invoke(instanceRef, setterRef, value);
    }
 
    protected Object invoke(Object target, Method m) {
       return invoke(target, m, EMPTY_OBJECT_ARRAY);
    }
 
    protected Object invoke(Object target, Method m, Object... args) {
       try {
          return m.invoke(target, args);
       } catch (IllegalAccessException iae) {
          throw new ReflectionException("Unable to access method for invocation. Target method: " + m.getName(), iae);
       } catch (IllegalArgumentException iae) {
          throw new ReflectionException("Illegal argument caught by underlying method during reflective call. Target method: " + m.getName(), iae);
       } catch (InvocationTargetException ite) {
          throw new ReflectionException("Method invocation failed. Target method: " + m.getName(), ite);
       }
    }
 }
