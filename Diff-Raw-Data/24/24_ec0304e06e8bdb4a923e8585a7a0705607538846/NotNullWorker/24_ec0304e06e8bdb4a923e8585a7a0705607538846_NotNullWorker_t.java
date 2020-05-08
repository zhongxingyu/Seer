 package examples.plastic.transformer;
 
 import examples.plastic.annotations.NotNull;
import org.apache.tapestry5.plastic.*;
 
 public class NotNullWorker implements PlasticClassTransformer {
 
   private static final class NullCheckingConduit implements
          FieldConduit<Object> {
 
     private final String className;
 
     private final String fieldName;
 
     private Object fieldValue;
 
     private NullCheckingConduit(String className, String fieldName) {
       this.className = className;
       this.fieldName = fieldName;
     }
 
     public Object get(Object instance, InstanceContext context) {
 
       return fieldValue;
     }
 
     public void set(Object instance, InstanceContext context, Object newValue) {
 
      if (newValue == null) {
         throw new IllegalArgumentException(String.format(
                "Field %s of class %s may not be assigned null.",
                fieldName, className));
      }
 
       fieldValue = newValue;
     }
   }
 
   public void transform(PlasticClass plasticClass) {
 
     for (PlasticField field : plasticClass
            .getFieldsWithAnnotation(NotNull.class)) {
 
       final String className = plasticClass.getClassName();
       final String fieldName = field.getName();
 
       field.setComputedConduit(new ComputedValue<FieldConduit<?>>() {
 
         public FieldConduit<Object> get(InstanceContext context) {
           return new NullCheckingConduit(className, fieldName);
         }
       });
     }
   }
 }
