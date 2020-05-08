 package com.sdc.js;
 
import JSPrinter.JSPrinter;
 import pretty.PrettyPackage;
 
 import com.sdc.abstractLanguage.AbstractClass;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class JSClass extends AbstractClass {
     public final static String INHERITANCE_IDENTIFIER = "extends";
 
     public JSClass(final String modifier, final String type, final String name, final String packageName,
                      final List<String> implementedInterfaces, final String superClass,
                      final List<String> genericTypes, final List<String> genericIdentifiers,
                      final int textWidth, final int nestSize)
     {
         super(modifier, type, name, packageName, implementedInterfaces, superClass, genericTypes, genericIdentifiers, textWidth, nestSize);
         this.myDefaultPackages = Arrays.asList(myPackage);
     }
 
     @Override
     protected String getInheritanceIdentifier() {
         return INHERITANCE_IDENTIFIER;
     }
 
     @Override
     public String toString() {
        return PrettyPackage.pretty(myTextWidth, (new JSPrinter()).printClass(this));
     }
 }
