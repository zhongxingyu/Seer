 package ru.rulex.conclusion.dagger;
 
 public enum Generefier implements ValueSuppler<Object>
 {
   INSTANCE
   {
     @Override
     public Object supply( Object value )
     {
       return value;
     }
   };
 
   @SuppressWarnings("unchecked") // these Object ValueSuppler work for any T
  <T> ValueSuppler<T> generify()
   {
     return (ValueSuppler<T>) this;
   }
 }
