 package com.massfords.jaxb;
 
 import java.util.Set;
 
 import com.sun.codemodel.JDefinedClass;
 import com.sun.codemodel.JMod;
 import com.sun.codemodel.JPackage;
 import com.sun.codemodel.JTypeVar;
 import com.sun.tools.xjc.outline.ClassOutline;
 import com.sun.tools.xjc.outline.Outline;
 
 /**
  * Creates a no-op implementation of the Transformer interface. After creating the class
  * a visit method is added for each of the beans that were generated.
  * 
  * @author utard
  */
 public class CreateBaseTransformerClass extends CodeCreator {
 
     private JDefinedClass transformer;
 
     public CreateBaseTransformerClass(JDefinedClass transformer, Outline outline, JPackage jPackage) {
         super(outline, jPackage);
         this.transformer = transformer;
     }
 
     @Override
     protected void run(Set<ClassOutline> classes) {
         setOutput(getOutline().getClassFactory().createClass(getPackage(), "BaseTransformer", null));
         JTypeVar genericType = getOutput().generify("T");
         getOutput()._implements(transformer.narrow(genericType));
         for (ClassOutline classOutline : classes) {
             if (!classOutline.target.isAbstract()) {
                 // add the method to the base vizzy
                getOutput().method(JMod.ABSTRACT | JMod.PUBLIC, genericType, "transform").param(classOutline.implClass, "aBean");
             }
         }
     }
 }
