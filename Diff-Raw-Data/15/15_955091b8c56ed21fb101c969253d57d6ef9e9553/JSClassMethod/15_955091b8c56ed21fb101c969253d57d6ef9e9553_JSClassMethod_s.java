 package com.sdc.js;
 
import JSClassPrinter.JSClassPrinterPackage;
 import pretty.PrettyPackage;
 
 import com.sdc.abstractLanguage.AbstractClass;
 import com.sdc.abstractLanguage.AbstractMethod;
 
 import java.util.List;
 
 public class JSClassMethod extends AbstractMethod {
     public JSClassMethod(final String modifier, final String returnType, final String name, final String[] exceptions,
                       final AbstractClass abstractClass,
                       final List<String> genericTypes, final List<String> genericIdentifiers,
                       final int textWidth, final int nestSize)
     {
         super(modifier, returnType, name, exceptions, abstractClass, genericTypes, genericIdentifiers, textWidth, nestSize);
         this.myRootAbstractFrame = new JSFrame();
         this.myCurrentAbstractFrame = myRootAbstractFrame;
     }
 
     @Override
     protected String getInheritanceIdentifier() {
         return JSClass.INHERITANCE_IDENTIFIER;
     }
 
     @Override
     protected int getParametersStartIndex() {
         return 1;
     }
 
     @Override
     public String toString() {
          return PrettyPackage.pretty(myTextWidth, JSClassPrinterPackage.printClassMethod(this));
     }
 }
