 package net.chrisrichardson.asyncpojos.futures;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.springframework.util.Assert;
 
 public class Outcome<T> {
   public Throwable failure;
   public T success;
 
   @Override
   public String toString() {
     return ToStringBuilder.reflectionToString(this);
   }
   
   public boolean isSuccessful() {
     return failure == null;
   }
 
   static Outcome<?> makeFailedOutcome(Throwable e) {
     Assert.notNull(e);
     Outcome<?> o = new Outcome<Object>();
     o.failure = e;
     return o;
   }
 
  static <T> Outcome<T> makeSuccessfulOutcome(T result) {
     Outcome<T> o = new Outcome<T>();
     o.success = result;
     return o;
   }
 }
