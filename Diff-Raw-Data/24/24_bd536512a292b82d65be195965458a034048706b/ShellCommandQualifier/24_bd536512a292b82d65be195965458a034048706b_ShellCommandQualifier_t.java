 package com.ctp.javaone.archiver.command;
 
 import javax.enterprise.util.AnnotationLiteral;
 
 @SuppressWarnings("all")
public class ShellCommandQualifier extends AnnotationLiteral<ShellCommand> 
		implements ShellCommand {
 
     private final String value;
 
     public ShellCommandQualifier(String value) {
         this.value = value;
     }
 
     @Override
     public String value() {
         return value;
     }
 
 }
