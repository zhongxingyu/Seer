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
 package org.jsonddl.generator;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.ServiceLoader;
 import java.util.TreeMap;
 
 import org.jsonddl.model.EnumValue;
 import org.jsonddl.model.Kind;
 import org.jsonddl.model.Model;
 import org.jsonddl.model.Property;
 import org.jsonddl.model.Schema;
 import org.jsonddl.model.Type;
 import org.mozilla.javascript.CompilerEnvirons;
 import org.mozilla.javascript.Node;
 import org.mozilla.javascript.Parser;
 import org.mozilla.javascript.RhinoException;
 import org.mozilla.javascript.ast.ArrayLiteral;
 import org.mozilla.javascript.ast.AstNode;
 import org.mozilla.javascript.ast.AstRoot;
 import org.mozilla.javascript.ast.KeywordLiteral;
 import org.mozilla.javascript.ast.Name;
 import org.mozilla.javascript.ast.NumberLiteral;
 import org.mozilla.javascript.ast.ObjectLiteral;
 import org.mozilla.javascript.ast.ObjectProperty;
 import org.mozilla.javascript.ast.StringLiteral;
 import org.mozilla.javascript.ast.VariableDeclaration;
 
 import com.google.gson.Gson;
 
 public class Generator {
   public static void main(String[] args) throws IOException {
     final File outputRoot = new File(args[2]);
     Options options = new Options.Builder().withPackageName(args[1]).build();
     new Generator().generate(new FileInputStream(new File(args[0])), options,
         new Dialect.Collector() {
           @Override
           public void println(String message) {
             System.out.println(message);
           }
 
           @Override
           public void println(String format, Object... args) {
             System.out.println(String.format(format, args));
           }
 
           @Override
           public OutputStream writeJavaSource(String packageName, String simpleName)
               throws IOException {
             File f = new File(outputRoot, packageName.replace('.', File.separatorChar));
             f.mkdirs();
             f = new File(f, simpleName + ".java");
             return new FileOutputStream(f);
           }
 
           @Override
           public OutputStream writeResource(String path) throws IOException {
             File file = new File(outputRoot, path);
             file.getParentFile().mkdirs();
             return new FileOutputStream(file);
           }
         });
   }
 
   public boolean generate(InputStream schema, Options options, Dialect.Collector output)
       throws IOException {
     Schema s;
    if (Boolean.TRUE.equals(options.getNormalizedInput())) {
       s = parseNormalized(schema, output);
     } else {
       s = parseIdiomatic(options, schema, output);
     }
     if (s == null) {
       return false;
     }
     ServiceLoader<Dialect> loader = ServiceLoader.load(Dialect.class);
     if (!loader.iterator().hasNext()) {
       /*
        * Fallback for the case where the generator jar isn't on the actual classpath, but has been
        * loaded as a plugin. This is normally what happens when running as an annotation processor.
        */
       loader = ServiceLoader.load(Dialect.class, getClass().getClassLoader());
     }
 
     List<String> dialects = options.getDialects();
     for (Dialect dialect : loader) {
       if (dialects == null || dialects.contains(dialect.getName())) {
         dialect.generate(options, output, s);
       }
     }
     return true;
   }
 
   Schema parseIdiomatic(Options options, InputStream schema, Dialect.Collector output)
       throws IOException {
     CompilerEnvirons env = new CompilerEnvirons();
     env.setRecordingComments(true);
     env.setRecordingLocalJsDocComments(true);
     Parser parser = new Parser(env);
     AstRoot root;
     try {
       InputStreamReader sourceReader = new InputStreamReader(schema);
       root = parser.parse(sourceReader, options.getPackageName(), 0);
       sourceReader.close();
     } catch (RhinoException e) {
       output.println("Could not parse input file: %s", e.getMessage());
       return null;
     }
     Schema.Builder schemaBuilder = new Schema.Builder();
     Node first = root.getFirstChild();
     if (first instanceof VariableDeclaration) {
       VariableDeclaration decl = (VariableDeclaration) first;
       schemaBuilder.setComment(decl.getJsDoc());
       first = decl.getVariables().get(0).getInitializer();
     }
     ObjectLiteral obj;
     if (first instanceof ObjectLiteral) {
       obj = (ObjectLiteral) first;
     } else {
       output.println("Expecting an object literal or variable initializer as the first node");
       return null;
     }
 
     Map<String, Model> models = new TreeMap<String, Model>();
     for (ObjectProperty prop : obj.getElements()) {
       Model.Builder builder = new Model.Builder()
           .withComment(prop.getLeft().getJsDoc())
           .withName(extractName(prop));
 
       ArrayLiteral enumDeclarations = castOrNull(ArrayLiteral.class, prop.getRight());
       ObjectLiteral propertyDeclarations = castOrNull(ObjectLiteral.class, prop.getRight());
 
       if (enumDeclarations != null) {
         List<EnumValue> enumValues = new ArrayList<EnumValue>();
         for (AstNode node : enumDeclarations.getElements()) {
           StringLiteral string = castOrNull(StringLiteral.class, node);
           if (string == null) {
             throw new UnexpectedNodeException(node, "Expecting a string");
           }
           enumValues.add(new EnumValue.Builder()
               .withComment(node.getJsDoc())
               .withName(string.getValue())
               .build());
         }
         builder.withEnumValues(enumValues);
       } else if (propertyDeclarations != null) {
         List<Property> properties = new ArrayList<Property>();
         for (ObjectProperty propertyDeclaration : propertyDeclarations.getElements()) {
           properties.add(extractProperty(propertyDeclaration));
         }
         builder.withProperties(properties);
       } else {
         throw new UnexpectedNodeException(prop.getRight(),
             "Expecting property declaration object or enum declaration array");
       }
       models.put(extractName(prop), builder.build());
     }
     Schema s = schemaBuilder.withModels(models).accept(new DdlTypeReplacer()).build();
     return s;
   }
 
   Schema parseNormalized(InputStream schema, Dialect.Collector output) throws IOException {
     @SuppressWarnings("unchecked")
     Map<String, Object> map = new Gson().fromJson(new InputStreamReader(schema, "UTF8"), Map.class);
     return new Schema.Builder().from(map).build();
   }
 
   private <T extends AstNode> T castOrNull(Class<T> clazz, AstNode node) {
     if (clazz.isInstance(node)) {
       return clazz.cast(node);
     }
     return null;
   }
 
   /**
    * Extracts the name of the given object property.
    */
   private String extractName(ObjectProperty prop) {
     String typeName;
     StringLiteral typeNameAsLit = castOrNull(StringLiteral.class, prop.getLeft());
     Name typeNameAsName = castOrNull(Name.class, prop.getLeft());
     if (typeNameAsLit != null) {
       typeName = typeNameAsLit.getValue();
     } else if (typeNameAsName != null) {
       typeName = typeNameAsName.getIdentifier();
     } else {
       throw new RuntimeException("Unexpected node type "
         + prop.getLeft().getClass().getSimpleName());
     }
     return typeName;
   }
 
   private Property extractProperty(ObjectProperty prop) {
     return new Property.Builder()
         .withComment(prop.getLeft().getJsDoc())
         .withName(extractName(prop))
         .withType(typeName(prop.getRight(), false))
         .build();
   }
 
   /**
    * Convert an AST node to a Java type reference.
    */
   private Type typeName(AstNode node, boolean forceBoxed) {
     StringLiteral string = castOrNull(StringLiteral.class, node);
     if (string != null) {
       String value = string.getValue();
       if (value.isEmpty()) {
         return new Type.Builder()
             .withKind(Kind.STRING)
             .build();
       }
       return new Type.Builder().withKind(Kind.EXTERNAL).withName(value).build();
     }
     Name name = castOrNull(Name.class, node);
     if (name != null) {
       String id = name.getIdentifier();
       return new Type.Builder()
           .withKind(Kind.EXTERNAL)
           .withName(id)
           .build();
     }
 
     ArrayLiteral array = castOrNull(ArrayLiteral.class, node);
     if (array != null) {
       if (array.getSize() != 1) {
         throw new UnexpectedNodeException(array, "Expecting exactly one entry");
       }
       return new Type.Builder()
           .withKind(Kind.LIST)
           .withListElement(typeName(array.getElement(0), true))
           .build();
     }
 
     KeywordLiteral keyword = castOrNull(KeywordLiteral.class, node);
     if (keyword != null && keyword.isBooleanLiteral()) {
       return new Type.Builder()
           .withKind(Kind.BOOLEAN)
           .build();
     }
 
     NumberLiteral num = castOrNull(NumberLiteral.class, node);
     if (num != null) {
       double d = num.getNumber();
       if (Math.round(d) == d) {
         return new Type.Builder()
             .withKind(Kind.INTEGER)
             .build();
       } else {
         return new Type.Builder()
             .withKind(Kind.DOUBLE)
             .build();
       }
     }
     ObjectLiteral obj = castOrNull(ObjectLiteral.class, node);
     if (obj != null) {
       if (obj.getElements().size() != 1) {
         throw new UnexpectedNodeException(obj, "Expecting exactly one property");
       }
       ObjectProperty prop = obj.getElements().get(0);
       return new Type.Builder()
           .withKind(Kind.MAP)
           .withMapKey(typeName(prop.getLeft(), true))
           .withMapValue(typeName(prop.getRight(), true))
           .build();
     }
 
     throw new UnexpectedNodeException(node);
   }
 
 }
