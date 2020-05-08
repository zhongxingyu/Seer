 package com.sdc.ast.expressions.nestedclasses;
 
 import com.sdc.abstractLanguage.AbstractClass;
 import com.sdc.abstractLanguage.AbstractMethod;
 
 import java.util.List;
 
 public class LambdaFunction extends NestedClass {
     public LambdaFunction(final AbstractClass nestedClass) {
         super(nestedClass);
     }
 
    public AbstractMethod getFunction() {
        List<AbstractMethod> methods = myNestedClass.getMethods();
        for (final AbstractMethod method : methods) {
             if (!method.getModifier().contains("bridge") && method.getName().equals("invoke")) {
                 return method;
             }
         }
         return null;
     }
 
     public boolean isKotlinLambda() {
        final String superClass = myNestedClass.getSuperClass();
         return superClass.startsWith("FunctionImpl");
     }
 }
