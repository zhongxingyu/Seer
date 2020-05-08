 package com.sdc.kotlin;
 
 import KotlinPrinter.KotlinPrinter;
 import com.sdc.abstractLanguage.AbstractFrame;
 import pretty.PrettyPackage;
 
 import com.sdc.abstractLanguage.AbstractClass;
 import com.sdc.abstractLanguage.AbstractMethod;
 
 import java.util.List;
 
 public class KotlinMethod extends AbstractMethod {
     private boolean hasReceiverParameter = false;
 
     public KotlinMethod(final String modifier, final String returnType, final String name, final String signature, final String[] exceptions,
                       final AbstractClass abstractClass,
                       final List<String> genericTypes, final List<String> genericIdentifiers,
                       final int textWidth, final int nestSize)
     {
         super(modifier, returnType, name, signature, exceptions, abstractClass, genericTypes, genericIdentifiers, textWidth, nestSize);
     }
 
     @Override
     protected String getInheritanceIdentifier() {
         return KotlinClass.INHERITANCE_IDENTIFIER;
     }
 
     @Override
     protected int getParametersStartIndex() {
         return isNormalClassMethod() || hasReceiverParameter ? 1 : 0;
     }
 
     @Override
     public AbstractFrame createFrame() {
         return new KotlinFrame();
     }
 
     public void dragReceiverFromMethodParameters() {
         hasReceiverParameter = true;
         declareThisVariable();
         myName = getCurrentFrame().getVariable(0).getType() + "." + myName;
     }
 
     public boolean isNormalClassMethod() {
         return myAbstractClass.isNormalClass();
     }
 
     @Override
     public String toString() {
         return PrettyPackage.pretty(myTextWidth, (new KotlinPrinter()).printMethod(this));
     }
 }
