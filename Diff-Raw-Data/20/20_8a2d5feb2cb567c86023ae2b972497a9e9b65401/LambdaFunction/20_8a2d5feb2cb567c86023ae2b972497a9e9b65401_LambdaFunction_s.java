 package com.sdc.ast.expressions.nestedclasses;
 
 import com.sdc.abstractLanguage.AbstractClass;
 import com.sdc.abstractLanguage.AbstractMethod;
import com.sdc.kotlin.KotlinClass;
import com.sdc.kotlin.KotlinClassField;
import com.sdc.kotlin.KotlinMethod;
 
 import java.util.List;
 
 public class LambdaFunction extends NestedClass {
     public LambdaFunction(final AbstractClass nestedClass) {
         super(nestedClass);
     }
 
    public KotlinMethod getFunction() {
        List<KotlinMethod> methods = ((KotlinClass) myNestedClass).getMethods();
        for (final KotlinMethod method : methods) {
             if (!method.getModifier().contains("bridge") && method.getName().equals("invoke")) {
                 return method;
             }
         }
         return null;
     }
 
     public boolean isKotlinLambda() {
        final String superClass = ((KotlinClass) myNestedClass).getSuperClass();
         return superClass.startsWith("FunctionImpl");
     }
 }
