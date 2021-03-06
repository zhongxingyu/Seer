 /*
  * Copyright 2009 by OpenGamma Inc and other contributors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.fudgemsg.proto.java;
 
 import java.io.IOException;
 
 import org.fudgemsg.proto.IndentWriter;
 import org.fudgemsg.proto.MessageDefinition;
 import org.fudgemsg.proto.EnumDefinition;
 
 /**
  * Helper class for JavaClassCode for dealing with repeated code constructs and
  * the local variable stack, which
  * seemed like a good idea at the time but isn't really necessary.
  */
 /* package */class JavaWriter {
 
   private final JavaWriter _parent;
 
   private final IndentWriter _writer;
 
   private int _scope;
 
   private boolean _anonValue;
 
   /* package */JavaWriter(final IndentWriter writer) {
     _writer = writer;
     _parent = null;
     _scope = 1;
    _anonValue = true;
   }
 
   /* package */JavaWriter(final JavaWriter parent) {
     _parent = parent;
     _writer = parent._writer;
     _scope = parent._scope;
     _anonValue = parent._anonValue;
   }
 
   /* package */void namedLocalVariable(final String type, final String name,
       final String value) throws IOException {
     if (value != null) {
       _writer.write("final ");
     }
     _writer.write(type);
     _writer.write(' ');
     _writer.write(name);
     if (value != null) {
       _writer.write(" = ");
       _writer.write(value);
     }
   }
 
   /* package */String localVariable(final String type, final boolean isFinal)
       throws IOException {
     final StringBuilder sb = new StringBuilder("fudge").append(_scope++);
     if (isFinal)
       _writer.write("final ");
     _writer.write(type);
     _writer.write(' ');
     _writer.write(sb.toString());
     return sb.toString();
   }
 
   /* package */String localVariable(final String type, final boolean isFinal,
       final String value) throws IOException {
     final String lv = localVariable(type, isFinal);
     _writer.write(" = ");
     _writer.write(value);
     return lv;
   }
 
   /* package */String forEach(final String clazz, final String collection)
       throws IOException {
     _writer.write("for (");
     final String field = localVariable(clazz, false);
     _writer.write(" : ");
     _writer.write(collection);
     _writer.write(')');
     return field;
   }
 
   /* package */String forEachIndex(final String array, final String limit)
       throws IOException {
     _writer.write("for (");
     final String index = localVariable("int", false, "0");
     _writer.write("; ");
     _writer.write(index);
     _writer.write(" < ");
     _writer.write(array);
     _writer.write('.');
     _writer.write(limit);
     _writer.write("; ");
     _writer.write(index);
     _writer.write("++)");
     return index;
   }
 
   /* package */void javadoc(final String text) throws IOException {
     _writer.write("/**");
     _writer.newLine();
     final String[] lines = text.split("\\n");
     int shortestWhitespace = Integer.MAX_VALUE;
     for (String line : lines) {
       for (int i = 0; i < line.length(); i++) {
         if (!Character.isWhitespace(line.charAt(i))) {
           if (i < shortestWhitespace)
             shortestWhitespace = i;
           break;
         }
       }
     }
     if (shortestWhitespace == Integer.MAX_VALUE)
       shortestWhitespace = 0;
     for (String line : lines) {
       _writer.write(" * ");
       _writer.write(line.substring(shortestWhitespace));
       _writer.newLine();
     }
     _writer.write(" */");
     _writer.newLine();
   }
 
   /* package */void throwInvalidFudgeFieldException(
       final MessageDefinition message, final String fieldRef,
       final String expected, final String cause) throws IOException {
     _writer
         .write("throw new IllegalArgumentException (\"Fudge message is not a ");
     _writer.write(message.getName());
     _writer.write(" - field '");
     _writer.write(fieldRef);
     _writer.write("' is not ");
     _writer.write(expected);
     _writer.write('\"');
     if (cause != null) {
       _writer.write(", ");
       _writer.write(cause);
     }
     _writer.write(')');
   }
 
   /* package */void throwAssertionError(final String message)
       throws IOException {
     _writer.write("throw new AssertionError (\"");
     _writer.write(message);
     _writer.write("\")");
   }
 
   /* package */void throwNullParameterException(final String variable)
       throws IOException {
     _writer.write("throw new NullPointerException (\"");
     _writer.write(variable);
     _writer.write(" must not be null\")");
   }
 
   /* package */void throwEmptyListException(final String variable)
       throws IOException {
     _writer.write("throw new IllegalArgumentException (\"");
     _writer.write(variable);
     _writer.write(" must not be an empty list\")");
   }
   
   /* package */ void throwWrongSizedArrayException (final String variable, final int size) throws IOException {
     _writer.write ("throw new IllegalArgumentException (\"");
     _writer.write (variable);
     _writer.write (" is not the expected length (");
     _writer.write (Integer.toString (size));
     _writer.write (")\")");
   }
 
   /* package */void defaultThrowInvalidFudgeEnumException(
       final EnumDefinition enumDefinition, final String encodedValueExpr)
       throws IOException {
     _writer
         .write("default : throw new IllegalArgumentException (\"Field is not a ");
     _writer.write(enumDefinition.getName());
     _writer.write(" - invalid value '\" + ");
     _writer.write(encodedValueExpr);
     _writer.write(" + \"'\")");
   }
 
   /* package */void elseThrowInvalidFudgeFieldException(
       final MessageDefinition message, final String fieldRef,
       final String expected, final String cause) throws IOException {
     _writer.write("else ");
     throwInvalidFudgeFieldException(message, fieldRef, expected, cause);
   }
 
   /* package */void ifNull(final String test) throws IOException {
     _writer.write("if (");
     _writer.write(test);
     _writer.write(" == null) ");
   }
 
   /* package */void ifNotNull(final String test) throws IOException {
     _writer.write("if (");
     _writer.write(test);
     _writer.write(" != null) ");
   }
 
   /* package */void ifZero(final String test) throws IOException {
     _writer.write("if (");
     _writer.write(test);
     _writer.write(" == 0) ");
   }
 
   /* package */void ifEmptyList(final String test) throws IOException {
     _writer.write("if (");
     _writer.write(test);
     _writer.write(".size () == 0) ");
   }
   
   /* package */ void ifSizeNot (final String object, final String method, final int value) throws IOException {
     _writer.write ("if (");
     // object needs extra parens if it has a cast at the front
     if (object.charAt (0) == '(') _writer.write ('(');
     _writer.write (object);
     if (object.charAt (0) == '(') _writer.write (')');
     _writer.write ('.');
     _writer.write (method);
     _writer.write (" != ");
     _writer.write (Integer.toString (value));
     _writer.write (") ");
   }
 
   /* package */void ifGtZero(final String test) throws IOException {
     _writer.write("if (");
     _writer.write(test);
     _writer.write(" > 0) ");
   }
 
   /* package */void ifBool(final String test) throws IOException {
     _writer.write("if (");
     _writer.write(test);
     _writer.write(") ");
   }
 
   /* package */void ifNotBool(final String test) throws IOException {
     _writer.write("if (!");
     _writer.write(test);
     _writer.write(") ");
   }
 
   /* package */void assignment(final String variable, final String value)
       throws IOException {
     _writer.write(variable);
     _writer.write(" = ");
     _writer.write(value);
   }
 
   /* package */void assignmentConstruct(final String variable,
       final String clazz, final String params) throws IOException {
     _writer.write(variable);
     _writer.write(" = new ");
     _writer.write(clazz);
     _writer.write(" (");
     if (params != null)
       _writer.write(params);
     _writer.write(')');
   }
 
   /* package */void anonGetValue(final String source) throws IOException {
     if (!_anonValue) {
       _writer.write("Object ");
       _anonValue = true;
     }
     _writer.write("fudge0 = ");
     _writer.write(source);
     _writer.write(".getValue ()");
   }
 
   /* package */void ifNotInstanceOf(final String test, final String clazz)
       throws IOException {
     _writer.write("if (!(");
     _writer.write(test);
     _writer.write(" instanceof ");
     _writer.write(clazz);
     _writer.write(")) ");
   }
 
   /* package */void anonIfInstanceOf(final String clazz) throws IOException {
     _writer.write("if (fudge0 instanceof ");
     _writer.write(clazz);
     _writer.write(") ");
   }
 
   /* package */void anonElseIfInstanceOf(final String clazz) throws IOException {
     _writer.write("else if (fudge0 instanceof ");
     _writer.write(clazz);
     _writer.write(") ");
   }
 
   /* package */void anonIfNotInstanceOf(final String clazz) throws IOException {
     _writer.write("if (!(fudge0 instanceof ");
     _writer.write(clazz);
     _writer.write(")) ");
   }
 
   /* package */void anonAssignment(final String target, final String type)
       throws IOException {
     _writer.write(target);
     _writer.write(" = (");
     _writer.write(type);
     _writer.write(")fudge0");
   }
 
   /* package */void invoke(final String object, final String method,
       final String params) throws IOException {
     _writer.write(object);
     _writer.write('.');
     invoke(method, params);
   }
 
   /* package */void invoke(final String target, final String params)
       throws IOException {
     _writer.write(target);
     _writer.write(" (");
     if (params != null)
       _writer.write(params);
     _writer.write(')');
   }
 
   /* package */void guard() throws IOException {
     _writer.write("try");
   }
 
   /* package */void catchIllegalArgumentException() throws IOException {
     _writer.write("catch (IllegalArgumentException e)");
   }
 
   /* package */void catchCloneNotSupportedException() throws IOException {
     _writer.write("catch (CloneNotSupportedException e)");
   }
 
   /* package */void otherwise() throws IOException {
     _writer.write("else");
   }
 
   /* package */void method(final boolean isStatic, final String returnType,
       final String name, final String params) throws IOException {
     _writer.write("public ");
     if (isStatic)
       _writer.write("static ");
     _writer.write(returnType);
     _writer.write(' ');
     _writer.write(name);
     _writer.write(" (");
     if (params != null)
       _writer.write(params);
     _writer.write(')');
   }
 
   /* package */void constructor(final String visibility, final String clazz,
       final String params) throws IOException {
     _writer.write(visibility);
     _writer.write (' ');
     _writer.write(clazz);
     _writer.write(" (");
     if (params != null)
       _writer.write(params);
     _writer.write(')');
   }
 
   /* package */void attribute(final boolean isFinal, final String type,
       final String name) throws IOException {
     _writer.write("private ");
     if (isFinal)
       _writer.write("final ");
     _writer.write(type);
     _writer.write(' ');
     _writer.write(name);
   }
 
   /* package */void publicStaticFinal(final String type, final String name,
       final String value) throws IOException {
     _writer.write("public static final ");
     _writer.write(type);
     _writer.write(' ');
     _writer.write(name);
     _writer.write(" = ");
     _writer.write(value);
   }
 
   /* package */void returnInvoke(final String target, final String params,
       final String cast) throws IOException {
     _writer.write("return ");
     if (cast != null) {
       _writer.write('(');
       _writer.write(cast);
       _writer.write(')');
     }
     invoke(target, params);
   }
 
   /* package */void returnVariable(final String variable) throws IOException {
     _writer.write("return ");
     _writer.write(variable);
   }
 
   /* package */void returnIfNull(final String test, final String notNull,
       final String ifNull) throws IOException {
     _writer.write("return (");
     _writer.write(test);
     _writer.write(" != null) ? ");
     _writer.write(notNull);
     _writer.write(" : ");
     _writer.write(ifNull);
   }
 
   /* package */void returnThis() throws IOException {
     _writer.write("return this");
   }
 
   /* package */void returnTrue() throws IOException {
     _writer.write("return true");
   }
 
   /* package */void returnFalse() throws IOException {
     _writer.write("return false");
   }
 
   /* package */void returnConstruct(final String clazz, final String params)
       throws IOException {
     _writer.write("return new ");
     _writer.write(clazz);
     _writer.write(" (");
     if (params != null)
       _writer.write(params);
     _writer.write(')');
   }
 
   /* package */void returnNullIfZero(final String test) throws IOException {
     _writer.write("if (");
     _writer.write(test);
     _writer.write(" == 0) return null");
   }
 
   /* package */void throwIndexOutOfBoundsException(final String variable)
       throws IOException {
     _writer.write("throw new IndexOutOfBoundsException (\"");
     _writer.write(variable);
     _writer.write("=\" + ");
     _writer.write(variable);
     _writer.write(')');
   }
 
   /* package */void select(final String variable) throws IOException {
     _writer.write("switch (");
     _writer.write(variable);
     _writer.write(')');
   }
 
   /* package */void selectCaseReturn(final String caseValue,
       final String retValue) throws IOException {
     _writer.write("case ");
     _writer.write(caseValue);
     _writer.write(" : return ");
     _writer.write(retValue);
   }
 
   /* package */void classDef(final boolean isStatic, final String clazz,
       final String extendsClass, final String interfaceClass)
       throws IOException {
     _writer.write("public ");
     if (isStatic)
       _writer.write("static ");
     _writer.write("class ");
     _writer.write(clazz);
     if (extendsClass != null) {
       _writer.write(" extends ");
       _writer.write(extendsClass);
     }
     if (interfaceClass != null) {
       _writer.write(" implements ");
       _writer.write(interfaceClass);
     }
   }
 
   /* package */void elseReturnFalse() throws IOException {
     _writer.write("else return false");
   }
 
   /* package */void elseIfNotNull(final String test) throws IOException {
     _writer.write("else if (");
     _writer.write(test);
     _writer.write(" != null) ");
   }
 
   /* package */void packageDef(final String namespace) throws IOException {
     _writer.write("package ");
     _writer.write(namespace);
   }
   
   /* package */ void importLib (final String namespace) throws IOException {
     _writer.write ("import ");
     _writer.write (namespace);
   }
 
   /* package */void enumDef(final String clazz) throws IOException {
     _writer.write("public enum ");
     _writer.write(clazz);
   }
 
   /* package */void enumElementSeparator() throws IOException {
     _writer.write(',');
     _writer.newLine();
   }
 
   /* package */void enumElement(final String id, final String params)
       throws IOException {
     _writer.write(id);
     if (params != null) {
       _writer.write(" (");
       _writer.write(params);
       _writer.write(')');
     }
   }
 
   /* package */IndentWriter getWriter() {
     return _writer;
   }
 
   /* package */JavaWriter getParent() {
     return _parent;
   }
 
 }
