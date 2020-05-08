 /* 
  * 
  * PROJECT
  *     Name
  *         bean-annotation-processor
  *     
  *     Code Version
  *         1.0
  *     
  *     Description
  *         This provides some variants of annotation processors producing JavaBeans.
  *         
  * COPYRIGHTS
  *     Copyright (C) 2013 by Natusoft AB All rights reserved.
  *     
  * LICENSE
  *     Apache 2.0 (Open Source)
  *     
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *     
  *       http://www.apache.org/licenses/LICENSE-2.0
  *     
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  *     
  * AUTHORS
  *     tommy ()
  *         Changes:
  *         2013-07-15: Created!
  *         
  */
 package se.natusoft.annotation.beanannotationprocessor;
 
 import se.natusoft.annotation.beanannotationprocessor.annotations.Bean;
 import se.natusoft.annotation.processor.simplified.SimplifiedAnnotationProcessor;
 import se.natusoft.annotation.processor.simplified.annotations.*;
 import se.natusoft.annotation.processor.simplified.annotations.Process;
 import se.natusoft.annotation.processor.simplified.codegen.GenerationSupport;
 import se.natusoft.annotation.processor.simplified.codegen.JavaSourceOutputStream;
 import se.natusoft.annotation.processor.simplified.model.SAPAnnotation;
 import se.natusoft.annotation.processor.simplified.model.SAPType;
 
 import javax.annotation.processing.SupportedSourceVersion;
 import javax.lang.model.SourceVersion;
 import javax.lang.model.element.*;
 import javax.lang.model.type.PrimitiveType;
 import javax.lang.model.type.TypeMirror;
 import javax.tools.FileObject;
 import javax.tools.StandardLocation;
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * This generates the class that the annotated file extends.
  */
 @AutoDiscovery
 @ProcessedAnnotations({Bean.class})
 @SupportedSourceVersion(SourceVersion.RELEASE_7)
 public class BeanProcessor extends SimplifiedAnnotationProcessor {
     //
     // Private Members
     //
 
     private static final boolean verbose = true;
 
     private List<Element> toGenerate = null;
 
 
     //
     // Constructors
     //
 
     public BeanProcessor() {
         super(verbose);
     }
 
     //
     // Methods
     //
 
     @NewRound
     public void newRound() {
         this.toGenerate = new LinkedList<Element>();
     }
 
     @Process(Bean.class)
     public void processBean(Set<? extends Element> annotatedElements) {
         for (Element element : annotatedElements) {
             SAPType type = new SAPType(element);
 
            if (getClass().getClassLoader().getClass().equals(java.net.URLClassLoader.class)) {
                // This is very annoying!: When sources have already been generated and no clean have been
                // done, then ((TypeElement)element).getSuperClass() will return the super class including
                // package name. When no generated sources are available then this will return super class
                // without package. type.getExtends() always returns the superclass as is, while
                // type.getExtendsSimpleName() always removes an eventual package part.
                //
                // The real problem here is that if previously generated sources are available (no clean
                // has been done since last build) then if I generate the sources again (overwrites the old)
                // the compiler will complain about duplicate classes and fail!
                if (type.getExtendsSimpleName().equals(type.getExtends())) {
                    this.toGenerate.add(element);
                }
                else {
                    System.err.println("[WARNING] " +
                            "A previously generated version of '" + type.getQualifiedName() + "' already " +
                            "exists! Overwriting it will cause a duplicate class exception! This means that any " +
                            "changes in annotations will not be reflected in generated code unless you do " +
                            "clean and build again! I suspect this is a maven problem where maven is compiling " +
                            "the previously generated classes before annotation processing is triggered.");
                }
            }
            else {
                this.toGenerate.add(element);
            }
         }
     }
 
     @GenerateSource
     public void generate(GenerationSupport generationSupport) {
         try {
             for (Element annotatedElement : this.toGenerate) {
                 SAPType type = new SAPType((TypeElement)annotatedElement);
 
                 String genQName = type.getPackage() + "." + type.getExtendsSimpleName();
                 JavaSourceOutputStream jos = generationSupport.getToBeCompiledJavaSourceOutputStream(genQName, annotatedElement);
                 jos.packageLine(type.getPackage());
                 jos.generatedAnnotation(getClass().getName(), "");
                 jos.begClass("public", "abstract", type.getExtendsSimpleName());
                 jos.begMethod("protected", "", "", type.getExtendsSimpleName());
                 jos.endMethod();
                 jos.emptyLine();
                 {
                     SAPAnnotation beanAnnotation = type.getAnnotationByClass(Bean.class);
 
                     AnnotationValue annVal = beanAnnotation.getAnnotationValueFor("value");
                     @SuppressWarnings("unchecked")
                     List<AnnotationMirror> props = (List<AnnotationMirror>)annVal.getValue();
 
                     List<String> nonNullProps = new LinkedList<String>();
 
                     for (AnnotationMirror propMirror : props) {
                         SAPAnnotation propAnn = new SAPAnnotation(propMirror);
                         String propName = propAnn.getValueFor("name").toString();
                         String propType = propAnn.getValueFor("type").toString();
                         @SuppressWarnings("unchecked")
                         List<AnnotationValue> propTypeGenerics = (List<AnnotationValue>)propAnn.getAnnotationValueFor("generics").getValue();
                         String propDef = propAnn.getValueFor("init").toString();
                         boolean required = propAnn.getValueFor("required").toBoolean();
 
                         if (required) {
                             nonNullProps.add(propName);
                         }
 
                         verbose("Generating property: " + propName);
                         String defValue = propDef.trim().length() > 0 ? propDef : null;
                         String fieldType = propType;
                         if (propTypeGenerics.size() > 0) {
                             String comma="";
                             fieldType = fieldType + "<";
                             for (AnnotationValue genType : propTypeGenerics) {
                                 fieldType = fieldType + comma + genType.getValue().toString();
                                 comma = ",";
                             }
                             fieldType = fieldType + ">";
                         }
                         jos.field("private", fieldType, propName, defValue);
 
                         jos.begMethod("public", "", type.getSimpleName(), setterName(propName));
                         {
                             jos.methodArg(fieldType, "value");
                             jos.println("        this." + propName + " = value;");
                             jos.println("        return (" + type.getSimpleName() + ")this;");
                         }
                         jos.endMethod();
 
                         jos.begMethod("public", "", fieldType, getterName(propName));
                         {
                             jos.println("        return this." + propName + ";");
                         }
                         jos.endMethod();
                         jos.emptyLine();
                     }
 
                     if (!nonNullProps.isEmpty()) {
                         jos.begMethod("public", "", "void", "validate");
                         jos.println("        StringBuilder sb = new StringBuilder();");
                         jos.println("        String space = \"\";");
                         for (String propName : nonNullProps) {
                             jos.println("        if (this." + propName + " == null) {");
                             jos.println("            sb.append(space);");
                             jos.println("            space = \" \";");
                             jos.println("            sb.append(\"'" + propName + "' cannot be null!\");");
                             jos.println("        }");
                         }
                         jos.println("        if (sb.length() > 0) throw new IllegalStateException(sb.toString());");
                         jos.endMethod();
                     }
                 }
                 jos.endClass();
                 jos.close();
             }
         }
         catch (IOException ioe) {
             failCompile("@Bean processor failed to generate bean!", ioe);
         }
     }
 
     private String setterName(String propName) {
         return "set" + propName.substring(0, 1).toUpperCase() + propName.substring(1);
     }
 
     private String getterName(String propName) {
         return "get" + propName.substring(0, 1).toUpperCase() + propName.substring(1);
     }
 
     @AllProcessed
     public void done() {
     }
 
 }
