 package nl.spelberg.viewbean;
 
 import java.lang.reflect.Field;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.Assert;
 
 public class ViewBeanModel<T> implements ViewBeanField.UpdateListener<Object> {
 
     private static final Logger log = LoggerFactory.getLogger(ViewBeanModel.class);
 
     private final T bean;
     private final Map<String, ViewBeanField> fields;
 
     public ViewBeanModel(T bean) {
         Assert.notNull(bean, "bean is null");
         this.bean = bean;
         this.fields = findFields(bean);
     }
 
     private Map<String, ViewBeanField> findFields(Object bean) {
         Map<String, ViewBeanField> beanEditFields = new LinkedHashMap<String, ViewBeanField>();
 
         Class<?> beanClass = bean.getClass();
         List<Field> fields = ViewBeanUtils.allDeclaredFields(beanClass);
         for (Field field : fields) {
             ViewField annotation = field.getAnnotation(ViewField.class);
             if (annotation != null) {
                 ViewBeanField viewBeanField = new ViewBeanField(bean, field);
                 beanEditFields.put(viewBeanField.id(), viewBeanField);
             }
         }
 
         return beanEditFields;
     }
 
     public T bean() {
         return bean;
     }
 
     public Collection<ViewBeanField> fields() {
         return fields.values();
     }
 
     public ViewBeanField fieldWithName(String name) {
         ViewBeanField attributeView = fields.get(name);
         if (attributeView == null) {
             throw new IllegalStateException("Field with name '" + name + "' does not exist in ViewBeanModel " + toString());
         } else {
             return attributeView;
         }
     }
 
    @SuppressWarnings({"unchecked"})
     public <Y> ViewBeanField<Y> fieldWithName(String name, Class<Y> fieldClass) {
         return fieldWithName(name);
     }
 
     @Override
     public String toString() {
         return "ViewBeanModel{beanClass=" + bean.getClass().getName() + ", beanEditFields=" + fields.entrySet() + '}';
     }
 
     public void beforeUpdate(ViewBeanField<Object> viewBeanField) {
         log.info("Before update: " + viewBeanField);
     }
 
     public void afterUpdate(ViewBeanField<Object> viewBeanField) {
         log.info("After update: " + viewBeanField);
     }
 
     @SuppressWarnings({"unchecked"})
     public Class<T> getBeanClass() {
         return (Class<T>) bean.getClass();
     }
 
     public int fieldIndexForName(String attributeName) {
         int index = 0;
         for (ViewBeanField viewBeanField : fields.values()) {
             if (viewBeanField.fieldName().equals(attributeName)) {
                 return index;
             }
             index++;
         }
         throw new IllegalStateException("No attribute with name '" + attributeName + "'");
     }
 }
