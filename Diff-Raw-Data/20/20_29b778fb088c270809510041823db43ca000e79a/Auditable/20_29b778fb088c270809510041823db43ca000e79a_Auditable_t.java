 package com.ctp.javaone.archiver.persistence;
 
 import java.lang.annotation.Retention;
 import java.lang.annotation.Target;
 import static java.lang.annotation.ElementType.*;
 import static java.lang.annotation.RetentionPolicy.*;
 
 import javax.interceptor.InterceptorBinding;
 
 @InterceptorBinding
 @Target({METHOD, TYPE})
 @Retention(RUNTIME)
 public @interface Auditable {
 }
