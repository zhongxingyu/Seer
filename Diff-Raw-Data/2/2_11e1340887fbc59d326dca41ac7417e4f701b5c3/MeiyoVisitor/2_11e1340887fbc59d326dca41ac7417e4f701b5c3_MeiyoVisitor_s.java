 package org.nnsoft.commons.meiyo.classvisitor;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 public final class MeiyoVisitor {
 
     private MeiyoVisitor() {
         // do nothing
     }
 
     public static ClassVisitor createVisitor(Module...modules) {
         if (modules == null || modules.length == 0) {
            throw new IllegalArgumentException("Modules cannot be null or empty");
         }
         return createVisitor(Arrays.asList(modules));
     }
 
     public static ClassVisitor createVisitor(Collection<Module> modules) {
         if (modules == null || modules.isEmpty()) {
             throw new IllegalArgumentException("Modules cannot be null or empty");
         }
 
         BinderImpl binderImpl = new BinderImpl();
         for (Module module : modules) {
             module.configure(binderImpl);
         }
 
         return new ClassVisitorImpl(binderImpl);
     }
 
 }
