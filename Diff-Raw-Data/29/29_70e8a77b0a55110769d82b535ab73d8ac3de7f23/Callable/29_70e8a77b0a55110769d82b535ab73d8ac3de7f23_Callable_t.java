 package org.babblelang.engine.impl;
 
 import org.babblelang.parser.BabbleParser;
 
 import java.util.LinkedHashMap;
 
 public interface Callable {
     Namespace bindParameters(Interpreter interpreter, BabbleParser.CallContext callSite, Namespace parent, Parameters parameters);
 
     Object call(Interpreter interpreter, BabbleParser.CallContext callSite, Scope scope);
 
     class Parameters extends LinkedHashMap<String, Object> {
         public void bind(Interpreter interpreter, BabbleParser.ParametersDeclarationContext parametersDeclarationContext, Scope scope) {
             int count = 0;
             for (BabbleParser.ParameterDeclarationContext parameter : parametersDeclarationContext.parameterDeclaration()) {
                 String pos = "$" + (count++);
                 String name = parameter.ID().getText();
                 Object value;
                 if (this.containsKey(name)) {
                     value = this.get(name);
                 } else if (this.containsKey(pos)) {
                     value = this.get(pos);
                 } else if (parameter.defaultValue != null) {
                     value = interpreter.visit(parameter.defaultValue);
                 } else {
                    throw new IllegalArgumentException("Missing parameter : " + name);
                 }
                 scope.define(name, false).set(value);
             }
         }
 
         public Object[] valuesArray() {
             Object[] result = new Object[size()];
             int i = 0;
             for (Object o : values()) {
                 result[i++] = o;
             }
             return result;
         }
 
         public Class[] typesArray() {
             Class[] result = new Class[size()];
             int i = 0;
             for (Object o : values()) {
                 result[i++] = o.getClass();
             }
             return result;
 
         }
     }
 }
