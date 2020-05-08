 package org.nebulostore.appcore;
 
 import java.lang.reflect.Method;
 
 import org.nebulostore.appcore.exceptions.NebuloException;
 import org.nebulostore.appcore.exceptions.UnsupportedMessageException;
 
 /**
  * Generic Message visitor class. Subclasses should define "visit" methods with one parameter that
  * is a subclass of Message. Visitor will go up the class hierarchy and call the first suitable
  * method it finds.
  * @param <R>
  *          return type.
  */
 public abstract class MessageVisitor<R> {
   private static final String METHOD_NAME = "visit";
 
/* Common action for all messages that are not handled. */
   protected R visitDefault(Message message) throws NebuloException {
     throw new UnsupportedMessageException(message.getClass().getName());
   }
 
   public R visit(Object message) throws NebuloException {
     if (message instanceof Message) {
       Class<?> currClass = message.getClass();
       while (currClass != Object.class) {
         try {
          Method method = this.getClass().getMethod(METHOD_NAME, currClass);
          return (R) method.invoke(this, message);
        } catch (Exception e) {
           currClass = currClass.getSuperclass();
         }
       }
       return visitDefault((Message) message);
     } else {
       throw new UnsupportedMessageException(message.getClass().getName());
     }
   }
 }
