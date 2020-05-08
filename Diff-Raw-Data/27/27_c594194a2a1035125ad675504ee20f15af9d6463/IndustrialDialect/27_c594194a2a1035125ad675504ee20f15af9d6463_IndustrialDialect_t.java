 /*
  * Copyright 2011 Robert W. Vawter III <bob@vawter.org>
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.jsonddl.generator.industrial;
 
 import static org.jsonddl.generator.TypeAnswers.getParameterizedQualifiedSourceName;
 
 import org.jsonddl.JsonDdlObject;
 import org.jsonddl.JsonDdlVisitor;
 import org.jsonddl.generator.Dialect;
 import org.jsonddl.generator.IndentedWriter;
 import org.jsonddl.generator.Options;
 import org.jsonddl.generator.TypeAnswers;
 import org.jsonddl.impl.ContextImpl;
 import org.jsonddl.impl.DigestVisitor;
 import org.jsonddl.impl.Digested;
 import org.jsonddl.impl.JsonMapVisitor;
 import org.jsonddl.impl.Protected;
 import org.jsonddl.impl.Traversable;
 import org.jsonddl.model.EnumValue;
 import org.jsonddl.model.Kind;
 import org.jsonddl.model.Model;
 import org.jsonddl.model.Property;
 import org.jsonddl.model.Schema;
 import org.jsonddl.model.Type;
 
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Generated;
 
 public class IndustrialDialect implements Dialect {
 
   public static String generatedAnnotation(Class<? extends Dialect> clazz, Date now) {
     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
     String generated = "@" + Generated.class.getCanonicalName() + "(value=\""
       + clazz.getCanonicalName() + "\", date=\"" + sdf.format(now) + "\")";
     return generated;
   }
 
   public static String getterName(String propertyName) {
     return Character.toUpperCase(propertyName.charAt(0))
       + (propertyName.length() > 1 ? propertyName.substring(1) : "");
   }
 
   @Override
   public void generate(Options options, Dialect.Collector output, Schema s) throws IOException {
     Date now = new Date();
     String packageName = options.getPackageName();
     for (Model model : s.getModels().values()) {
       Map<String, String> dialectProperties = model.getDialectProperties() == null ? null :
           model.getDialectProperties().get(getName());
       if (dialectProperties == null) {
         // Avoid the need for defensive coding below
         dialectProperties = Collections.emptyMap();
       }
 
       String simpleName = model.getName();
       String builderName = simpleName + ".Builder";
       String implName = simpleName + "Impl";
 
       PrintWriter intf = new PrintWriter(new OutputStreamWriter(output.writeJavaSource(
           packageName, simpleName)));
 
       intf.println("package " + packageName + ";");
       if (model.getComment() != null) {
         intf.println(model.getComment());
       }
       intf.println(generatedAnnotation(getClass(), now));
 
       if (model.getEnumValues() != null) {
         intf.println("public enum " + simpleName + " {");
         for (EnumValue enumValue : model.getEnumValues()) {
           if (enumValue.getComment() != null) {
             intf.println(enumValue.getComment());
           }
           intf.println(enumValue.getName() + ",");
         }
         intf.println("}");
         intf.close();
         continue;
       }
 
       intf.print("public interface " + simpleName);
       intf.print(" extends ");
       if (dialectProperties.containsKey("implements")) {
         intf.print(dialectProperties.get("implements"));
         intf.print(", ");
       }
       intf.print(JsonDdlObject.class.getCanonicalName() + "<" + simpleName + ">");
       intf.println(" {");
 
       StringWriter builderContents = new StringWriter();
       PrintWriter builder = new PrintWriter(builderContents);
       {
         builder.print("public static class Builder ");
         if (dialectProperties.containsKey("extends")) {
           builder.print(" extends " + dialectProperties.get("extends"));
         }
         builder.println(" implements "
           + JsonDdlObject.Builder.class.getCanonicalName() + "<" + simpleName + ">, "
           + Traversable.class.getCanonicalName() + "<" + simpleName + ">, "
           + Digested.class.getCanonicalName() + ", "
           + simpleName + " {");
         builder.println("private " + implName + " obj;");
         builder.println("public Builder() {this(new " + implName + "());}");
         builder.println("Builder(" + implName + " instance) {this.obj = instance;}");
         builder.println("public " + builderName + " builder() { return this; }");
         builder.println("public Class<" + simpleName + "> getDdlObjectType() { return "
           + simpleName + ".class;}");
         builder.println("public " + builderName + " newInstance() { return new " + builderName
           + "(); }");
         builder.println("public " + Map.class.getCanonicalName()
           + "<String, Object> toJsonObject() { return obj.toJsonObject(); }");
 
         writeObjectMethods(builder, false);
       }
 
       PrintWriter impl = new PrintWriter(new OutputStreamWriter(output.writeJavaSource(
           packageName, implName)));
       {
         impl.println("package " + packageName + ";");
         impl.println(generatedAnnotation(getClass(), now));
         impl.print("class " + implName);
         if (dialectProperties.containsKey("extends")) {
           impl.print(" extends " + dialectProperties.get("extends"));
         }
         impl.println(" implements "
           + Traversable.class.getCanonicalName() + "<" + simpleName + ">, "
           + Digested.class.getCanonicalName() + ", "
           + simpleName + " {");
         impl.println("protected " + implName + "() {}");
         impl.println("public Class<" + simpleName + "> getDdlObjectType() { return "
           + simpleName + ".class;}");
 
         writeObjectMethods(impl, true);
       }
 
       StringWriter buildContents = new StringWriter();
       PrintWriter build = new PrintWriter(buildContents);
       StringWriter fromContents = new StringWriter();
       PrintWriter from = new PrintWriter(fromContents);
       StringWriter traverseContents = new StringWriter();
       PrintWriter traverse = new PrintWriter(traverseContents);
       StringWriter traverseMutableContents = new StringWriter();
       PrintWriter traverseMutable = new PrintWriter(traverseMutableContents);
       for (Property property : model.getProperties()) {
         Type type = property.getType();
         String getterName = getterName(property.getName());
 
         String qsn = getParameterizedQualifiedSourceName(type);
         impl.println(qsn + " " + getterName + ";");
         impl.println("public " + qsn + " get" + getterName + "() {return "
             + getterName + ";}");
 
         if (property.getComment() != null) {
           intf.println(property.getComment());
         }
         intf.println(qsn + " get" + getterName + "();");
 
         if (TypeAnswers.shouldProtect(type)) {
           build.println("toReturn." + getterName + " = " + Protected.class.getCanonicalName()
             + ".object(toReturn." + getterName + ");");
         }
 
         // Getters, for DDL types and for plain types
         if (Kind.DDL.equals(type.getKind())) {
           // public Foo.Builder getFoo() { Foo.Builder toReturn = obj.foo.builder();
           builder.println("public " + qsn + ".Builder"
             + " get" + getterName + "() {");
           builder.println(qsn + ".Builder toReturn = obj." + getterName + ".builder();");
           builder.println("obj." + getterName + " = toReturn;");
           builder.println("return toReturn;");
           builder.println("}");
         } else {
           builder.println("public " + qsn + " get" + getterName + "() { return obj." + getterName
             + "; }");
         }
 
         // Setter
         builder.println("public void set" + getterName + "(" + qsn + " value) { with" + getterName
           + "(value);}");
         // Literate setter
         builder.print(
             "public " + builderName + " with" + getterName + "(" + qsn + " value) { ");
         builder.print("obj." + getterName + " = value;");
         builder.println("return this;}");
         if (Kind.LIST.equals(type.getKind())) {
           // Literate list accumulator
           builder.println("public " + builderName + " add" + getterName + "("
             + getParameterizedQualifiedSourceName(type.getListElement()) + " element) {");
           builder.println("if (obj." + getterName + " == null) { obj." + getterName + " = new "
             + ArrayList.class.getCanonicalName() + "(); }");
           builder.println("obj." + getterName + ".add(element);");
           builder.println("return this;");
           builder.println("}");
         } else if (Kind.MAP.equals(type.getKind())) {
           // Literate map accumulator
           builder.println("public " + builderName + " put" + getterName + "("
               + getParameterizedQualifiedSourceName(type.getMapKey()) + " key,"
             + getParameterizedQualifiedSourceName(type.getMapValue()) + " value) {");
           builder.println("if (obj." + getterName + " == null) { obj." + getterName + " = new "
             + LinkedHashMap.class.getCanonicalName() + "(); }");
           builder.println("obj." + getterName + ".put(key, value);");
           builder.println("return this;");
           builder.println("}");
         }
 
         // Property copy
         from.println("with" + getterName + "(from.get" + getterName + "());");
 
         // Traversal
         writeTraversalForProperty(traverse, property.getName(), getterName, type, false);
         writeTraversalForProperty(traverseMutable, property.getName(), getterName, type, true);
       }
       builder.println("public " + builderName + " accept("
         + JsonDdlVisitor.class.getCanonicalName()
         + " visitor) {");
       builder.println("obj = new " + ContextImpl.ObjectContext.Builder.class.getCanonicalName()
         + "<" + simpleName + ">().withValue(this).withKind("
         + Kind.class.getCanonicalName()
         + "."
         + Kind.DDL.name() + ").withMutability(true).build().traverse(visitor).builder().obj;");
       builder.println("return this;");
       builder.println("}");
 
       builder.println("public " + simpleName + " build() {");
       builder.println(implName + " toReturn = obj;");
       builder.println("obj = null;");
       builder.append(buildContents.getBuffer().toString());
       builder.println("return toReturn;");
       builder.println("}");
 
       builder.println("public " + builderName + " from(" + simpleName + " from) {");
       builder.append(fromContents.getBuffer());
       builder.append("return this;");
       builder.println("}");
 
       builder.println("public " + builderName + " from(" + Map.class.getCanonicalName()
         + "<String, Object> map){");
       builder.println("accept(" + JsonMapVisitor.class.getCanonicalName()
         + ".fromJsonMap(map));");
       builder.println("return this;");
       builder.println("}");
 
       builder.println("public " + builderName + " traverse("
           + JsonDdlVisitor.class.getCanonicalName()
           + " visitor) {");
       builder.append(traverseMutableContents.getBuffer().toString());
       builder.println("return this;");
       builder.println("}");
       builder.println("}");
       // END BUILDER
       intf.append(builderContents.getBuffer().toString());
 
       impl.println("public " + simpleName + " accept(" + JsonDdlVisitor.class.getCanonicalName()
         + " visitor) {");
       impl.println("return new " + ContextImpl.ObjectContext.Builder.class.getCanonicalName() + "<"
         + simpleName + ">().withValue(this).withKind(" + Kind.class.getCanonicalName() + "."
         + Kind.DDL.name()
         + ").build().traverse(visitor);");
       impl.println("}");
 
       impl.println("public " + builderName + " builder() { return newInstance().from(this); }");
       impl.println("public " + builderName + " newInstance() { return new " + builderName + "(); }");
 
       impl.println("public " + Map.class.getCanonicalName()
         + "<String,Object> toJsonObject() { return " + JsonMapVisitor.class.getCanonicalName()
         + ".toJsonObject(this); }");
 
       impl.println("public " + simpleName + " traverse(" + JsonDdlVisitor.class.getCanonicalName()
         + " visitor) {");
       impl.append(traverseContents.getBuffer().toString());
       impl.println("return this;");
       impl.println("}");
       impl.println("}");
       // END IMPL
 
       intf.println("Builder builder();");
       intf.println("Builder newInstance();");
       intf.println("}");
 
       intf.close();
       impl.close();
     }
     writePackageVisitor(options, s, output, now);
   }
 
   @Override
   public String getName() {
     return "industrial";
   }
 
   private String kindReference(Kind type) {
     if (type == null) {
       return "null";
     }
     return Kind.class.getCanonicalName() + "." + type.name();
   }
 
   private void writeObjectMethods(PrintWriter out, boolean assumeImmutable) {
     // Stash the digest for immutable objects
     if (assumeImmutable) {
       out.println("private byte[] digest;");
     }
    out.println("public byte[] computeDigest() {");
     if (assumeImmutable) {
       out.println("if (digest == null) {");
     } else {
       out.println("byte[] digest;");
     }
     // DigestVisitor v =
     out.println(DigestVisitor.class.getCanonicalName() + " v = new "
       + DigestVisitor.class.getCanonicalName() + "(); accept(v); digest = v.getDigest();");
     if (assumeImmutable) {
       out.println("}");
     }
     out.println("return digest;");
     out.println("}");
 
     // hashCode()
     out.println("public int hashCode() {");
     if (!assumeImmutable) {
       out.print("byte[] digest = ");
     }
    out.println("computeDigest();");
     out.println("return (int)((digest[0] << 3) | (digest[1] << 2) | (digest[18] << 1) | digest[19]);");
     out.println("}");
 
     // equals()
     out.println("public boolean equals(Object o) {");
     out.println("if (o == this) { return true; }");
     out.println("if (!(o instanceof " + Digested.class.getCanonicalName()
       + ")) { return false; }");
    out.println("byte[] d1 = computeDigest(); byte[] d2 = ((" + Digested.class.getCanonicalName()
      + ")o).computeDigest();");
     out.println("for (int i = 0, j = d1.length; i<j; i++) { if (d1[i] != d2[i]) return false ;}");
     out.println("return true;");
     out.println("}");
   }
 
   /**
    * Create a convenience base type that pre-defines all method signatures that a visitor for models
    * in the package would want to define.
    */
   private void writePackageVisitor(Options options, Schema s, Collector collector, final Date now)
       throws IOException {
     final String packageName = options.getPackageName();
     final StringBuilder visitorName = new StringBuilder(packageName.substring(packageName
         .lastIndexOf('.') + 1)).append("Visitor");
     visitorName.setCharAt(0, Character.toUpperCase(visitorName.charAt(0)));
     final IndentedWriter out = new IndentedWriter(new OutputStreamWriter(collector.writeJavaSource(
         packageName, visitorName.toString())));
     s.accept(new JsonDdlVisitor() {
       public void endVisit(Schema s) {
         out.outdent();
         out.println("}");
       }
 
       public boolean visit(Model m) {
         out.println("public void endVisit(%s x, %s<%s> ctx) throws Exception {}", m.getName(),
             Context.class.getCanonicalName(), m.getName());
         out.println("public boolean visit(%s x, %s<%s> ctx) throws Exception { return true; }",
             m.getName(), Context.class.getCanonicalName(), m.getName());
         return false;
       }
 
       public boolean visit(Schema s) {
         out.println("package %s;", packageName);
         out.println(generatedAnnotation(IndustrialDialect.class, now));
         out.println("/** A convenience base type tha defines visit methods for " +
             "all model types in the {@code %s} package.*/", packageName);
         out.println("public class %s implements %s {", visitorName,
             JsonDdlVisitor.class.getCanonicalName());
         out.indent();
         return true;
       }
 
     });
     out.close();
   }
 
   private void writeTraversalForProperty(PrintWriter pw, String propertyName, String getterName,
       Type type, boolean mutable) {
     if (mutable) {
       pw.println("with" + getterName + "(");
     }
 
     final List<Kind> kindReferencs = new ArrayList<Kind>();
     type.accept(new JsonDdlVisitor() {
       @SuppressWarnings("unused")
       public boolean visit(Type t, Context<Type> ctx) {
         kindReferencs.add(t.getKind());
         return true;
       }
     });
 
     pw.println("new " + TypeAnswers.getContextBuilderDeclaration(type) + "()");
     pw.println(".withKind(" + kindReference(kindReferencs.remove(0)) + ")");
     if (!kindReferencs.isEmpty()) {
       pw.print(".withNestedKinds(" + Arrays.class.getCanonicalName() + ".asList(");
       boolean needsComma = false;
       for (Kind kind : kindReferencs) {
         if (needsComma) {
           pw.print(", ");
         }
         needsComma = true;
         pw.print(kindReference(kind));
       }
       pw.println("))");
     }
     pw.println(".withLeafType(" + TypeAnswers.getQualifiedLeafTypeName(type) + ".class)");
     pw.println(".withMutability(" + mutable + ")");
     pw.println(".withProperty(\"" + propertyName + "\")");
     String propertyRef = (mutable ? "obj" : "this") + "." + getterName;
     pw.println(".withValue(" + propertyRef + ")");
     pw.print(".build().traverse(visitor)");
     if (mutable) {
       pw.print(")");
     }
     pw.println(";");
   }
 }
