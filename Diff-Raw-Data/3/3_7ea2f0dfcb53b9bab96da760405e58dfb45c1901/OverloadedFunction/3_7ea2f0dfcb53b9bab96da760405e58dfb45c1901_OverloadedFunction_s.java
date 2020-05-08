 package org.libj.xquery.namespace;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class OverloadedFunction implements Function {
     private String className;
     private String functionName;
     private List<JavaFunction> functions;
 
     public OverloadedFunction(String className, String functionName, List<JavaFunction> functions) {
         this.className = className.replace('.', '/');
         this.functionName = functionName;
         this.functions = functions;
     }
 
     public JavaFunction resolveFunction(Class... argumentTypes) {
         List<JavaFunction> numberMatched = new ArrayList<JavaFunction>();
         for (JavaFunction f: functions) {
             if (f.getParameterTypes().length == argumentTypes.length - (f.isMethod() ? 1 : 0)) {
                 numberMatched.add(f);
             }
         }
         if (numberMatched.isEmpty()) {
             throw new RuntimeException("No matching method found: "+functions.get(0).getClassName()+'/'+functions.get(0).getFunctionName());
         }
         if (numberMatched.size() == 1) {
             return numberMatched.get(0);
         }
         List<JavaFunction> exactlyMatched = new ArrayList<JavaFunction>();
         for (JavaFunction f: functions) {
             if(isExactlyMatched(f, argumentTypes)) {
                 exactlyMatched.add(f);
             }
         }
         if (exactlyMatched.isEmpty()) {
             throw new RuntimeException("No matching method found: "+functions.get(0).getClassName()+'/'+functions.get(0).getFunctionName());
         }
         if (exactlyMatched.size() == 1) {
             return exactlyMatched.get(0);
         }
         throw new RuntimeException("Not Implemented!");
     }
 
     private boolean isExactlyMatched(JavaFunction f, Class[] argumentTypes) {
         Class[] parameterTypes = f.getParameterTypes();
         for (int i = 0; i < parameterTypes.length; i++) {
             if (parameterTypes[i] != argumentTypes[i + (f.isMethod() ? 1 : 0)]) {
                 return false;
             }
         }
         return true;
     }
 
     public String getClassName() {
         return className;
     }
 
     public String getFunctionName() {
         return functionName;
     }
 }
