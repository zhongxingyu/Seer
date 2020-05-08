 package org.andrewhitchcock.duwamish.util;
 
 import java.io.Closeable;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.util.Iterator;
 
 import com.google.protobuf.Message.Builder;
 
 public abstract class ProtocolBufferReader<T> implements Iterator<T>, Closeable {
   
   protected boolean initialized = false;
   protected boolean success;
   protected T nextValue;
   
   protected abstract void getNext();
 
   @Override
   public boolean hasNext() {
     initialize();
     return success;
   }
 
   @Override
   public T next() {
     initialize();
     T result = nextValue;
     getNext();
     return result;
   }
 
   @Override
   public void remove() {
     throw new RuntimeException("Not implemented");
   }
   
   public T peak() {
     initialize();
     return nextValue;
   }
   
   private void initialize() {
     if (!initialized) {
       getNext();
       initialized = true;
     }
   }
   
   
   
   public static <A> ProtocolBufferReader<A> newReader(Class<A> clazz, final InputStream inputSteam) {
     Method bm;
     try {
       bm = clazz.getMethod("newBuilder");
     } catch (Exception e) {
       throw new RuntimeException();
     }
     final Method finalBm = bm;
     
     return new ProtocolBufferReader<A>() {
       private InputStream is = inputSteam;
       private Method builderMethod = finalBm;
       
       @SuppressWarnings("unchecked")
       @Override
       protected void getNext() {
         try {
           Builder builder = (Builder)(builderMethod.invoke(null));
           success = builder.mergeDelimitedFrom(is);
           if (success) {
             nextValue = (A) builder.build();
           } else {
             nextValue = null;
           }
         } catch (Exception e) {
           throw new RuntimeException(e);
         }
       }
 
       @Override
       public void close() {
         FileUtil.closeAll(is);
       }
     };
   }
   
   public static <A> ProtocolBufferReader<A> newReader(final Object[] values) {
     return new ProtocolBufferReader<A>() {
       private Object[] objects = values;
       private int index = 0;
       
       @SuppressWarnings("unchecked")
       @Override
       protected void getNext() {
        if (index < objects.length) {
           nextValue = (A) objects[index];
           success = true;
           index++;
         } else {
           nextValue = null;
           success = false;
         }
       }
       
       @Override
       public void close() {
         // no op
       }
     };
   }
 }
