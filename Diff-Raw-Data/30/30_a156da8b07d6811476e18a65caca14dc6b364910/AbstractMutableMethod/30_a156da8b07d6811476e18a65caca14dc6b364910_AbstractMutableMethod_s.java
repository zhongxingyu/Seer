 package net.bodz.bas.potato.element;
 
 public abstract class AbstractMutableMethod
         extends MutablePotatoElement
         implements IMethod {
 
    private static final long serialVersionUID = 1L;

     private Class<?> returnType;
     private Class<?>[] parameterTypes;
 
     /** ⇱ Implementation Of {@link IMethod}. */
     /* _____________________________ */static section.iface __METHOD__;
 
     @Override
     public Class<?> getReturnType() {
         return returnType;
     }
 
     @Override
     public Class<?>[] getParameterTypes() {
         return parameterTypes;
     }
 
     /** ⇱ Implementation Of {@link IMutableMethod}. */
     /* _____________________________ */static section.iface __MUTABLE__;
 
     public void setReturnType(Class<?> returnType) {
         this.returnType = returnType;
     }
 
     public void setParameterTypes(Class<?>[] parameterTypes) {
         this.parameterTypes = parameterTypes;
     }
 
 }
