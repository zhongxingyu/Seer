 package org.jboss.forge.addon.gradle.parser;
 
 /**
  * Represents assignment to string variable.
  * 
  * @author Adam Wyłuda
  */
 public class VariableAssignment extends SourceCodeElement
 {
    private final String variable;
    private final String value;
 
    public VariableAssignment(String variable, String value, 
             int lineNumber, int columnNumber, int lastLineNumber, int lastColumnNumber)
    {
       super(lineNumber, columnNumber, lastLineNumber, lastColumnNumber);
       
       this.variable = variable;
       this.value = value;
    }
 
    public String getVariable()
    {
       return variable;
    }
 
    public String getValue()
    {
       return value;
    }
 }
