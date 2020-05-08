 package iTests.framework.testng.annotations;
 
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 
 @Retention(RetentionPolicy.RUNTIME)
 public @interface TestConfiguration {
 
     public enum VM {
         UNIX,
         WINDOWS,
         MAC,
         ALL
     }
 
    VM[] os() default VM.ALL;
 
 }
