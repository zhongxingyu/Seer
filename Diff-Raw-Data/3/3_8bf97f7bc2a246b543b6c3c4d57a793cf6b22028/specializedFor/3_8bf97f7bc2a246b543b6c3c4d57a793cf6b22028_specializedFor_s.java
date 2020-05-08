 package miscellaneous.serialization;
 
 import java.lang.annotation.*;
 
 @Target({ElementType.METHOD, ElementType.FIELD})
 @Retention(RetentionPolicy.RUNTIME)
 public @interface specializedFor {
 
  Class<?>[] value();
 }
