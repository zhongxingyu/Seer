/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
