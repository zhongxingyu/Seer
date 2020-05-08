 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.ace.generator.artifacts;
 
 import org.icefaces.ace.generator.behavior.Behavior;
 import org.icefaces.ace.generator.context.ComponentContext;
 import org.icefaces.ace.generator.context.GeneratorContext;
 import org.icefaces.ace.generator.utils.FileWriter;
 import org.icefaces.ace.generator.utils.PropertyValues;
 import org.icefaces.ace.generator.utils.Utility;
 import org.icefaces.ace.meta.annotation.Component;
 import org.icefaces.ace.meta.annotation.Facet;
 import org.icefaces.resources.ICEBrowserDependency;
 import org.icefaces.resources.ICEResourceDependencies;
 import org.icefaces.resources.ICEResourceDependency;
 import org.icefaces.resources.ICEResourceLibrary;
 
 import javax.faces.application.ResourceDependencies;
 import javax.faces.application.ResourceDependency;
 
 import java.lang.reflect.Field;
 import java.util.*;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 public class ComponentArtifact extends Artifact{
     private static final Logger logger = Logger.getLogger(ComponentArtifact.class.getName());
     private StringBuilder writer = new StringBuilder();
     private ComponentContext componentContext;
 
 
     public ComponentArtifact(ComponentContext componentContext) {
         super(componentContext);
         this.componentContext = componentContext;
     }
 
     private ComponentContext getComponentContext() {
         return componentContext;
     }
 
     private void startComponentClass(ComponentContext compCtx, Class clazz, Component component) {
         String generatedClassName = Utility.getGeneratedClassName(component);
         /*logger.info("START COMPONENTCLASS for class ="+generatedClassName);*/
         writer.append("package ");
         writer.append(Utility.getPackageNameOfClass(generatedClassName));
         writer.append(";\n\n");
         writer.append("import java.io.IOException;\n");
         writer.append("import java.util.List;\n");
         writer.append("import java.util.ArrayList;\n");
         writer.append("import java.util.Map;\n");
         writer.append("import java.util.HashMap;\n");
         writer.append("import java.util.Arrays;\n\n");
         writer.append("import java.util.EnumSet;\n\n");
         writer.append("import java.util.Iterator;\n\n");
         writer.append("import javax.faces.context.FacesContext;\n");
         writer.append("import javax.el.MethodExpression;\n");
         writer.append("import javax.el.ValueExpression;\n\n");
         writer.append("import javax.faces.component.StateHelper;\n\n");
         writer.append("import javax.faces.component.UIComponent;\n");
         writer.append("import javax.faces.render.Renderer;\n");
         writer.append("import javax.faces.component.NamingContainer;\n");
         writer.append("import javax.faces.component.UINamingContainer;\n");
         writer.append("import javax.faces.component.UniqueIdVendor;\n");
         writer.append("import javax.faces.component.UIViewRoot;\n\n");
 
         if (clazz.isAnnotationPresent(ICEResourceDependencies.class) ||
             clazz.isAnnotationPresent(ICEResourceDependency.class)) {
             writer.append("import org.icefaces.resources.ICEResourceDependencies;\n");
             writer.append("import org.icefaces.resources.ICEResourceDependency;\n\n");
             writer.append("import org.icefaces.resources.ICEBrowserDependency;\n\n");
             writer.append("import org.icefaces.resources.BrowserType;\n\n");
         }
         if (clazz.isAnnotationPresent(ICEResourceLibrary.class)) {
             writer.append("import org.icefaces.resources.ICEResourceLibrary;\n\n");
         }
         if (clazz.isAnnotationPresent(ResourceDependencies.class) ||
             clazz.isAnnotationPresent(ResourceDependency.class)) {
             writer.append("import javax.faces.application.ResourceDependencies;\n");
             writer.append("import javax.faces.application.ResourceDependency;\n\n");
         }
 
         String interfaceClassName = Utility.getGeneratedInterfaceClassName(clazz);
         if (interfaceClassName != null && interfaceClassName.length() > 0) {
             writer.append("import ").append(interfaceClassName).append(";\n");
         }
 
         for (Behavior behavior: compCtx.getBehaviors()) {
             behavior.addImportsToComponent(writer);
         }
         writer.append("/*\n * ******* GENERATED CODE - DO NOT EDIT *******\n */\n");
         writer.append(Utility.getJavaDocComment(component.javadoc(), component.tlddoc()));
 
         // copy @ResourceDependency annotations
         if (clazz.isAnnotationPresent(ICEResourceLibrary.class)) {
             ICEResourceLibrary lib = (ICEResourceLibrary)clazz.getAnnotation(ICEResourceLibrary.class);
 
             writer.append("@ICEResourceLibrary(\"");
             writer.append(lib.value());
             writer.append("\")\n");
         }
 
         if (clazz.isAnnotationPresent(ICEResourceDependencies.class)) {
             writer.append("@ICEResourceDependencies({\n");
 
             ICEResourceDependencies rd = (ICEResourceDependencies) clazz.getAnnotation(ICEResourceDependencies.class);
             ICEResourceDependency[] rds = rd.value();
             int rdsLength = rds.length;
             for (int i = 0; i < rdsLength; i++) {
                 String overrideString = getOverrideString(rds[i]);
 
                 writer.append(
                         "\t@ICEResourceDependency(name=\"" + rds[i].name() + "\"," +
                         "library=\"" + rds[i].library() +  "\"," +
                         "target=\"" + rds[i].target() + "\"," +
                         "browser=BrowserType." + rds[i].browser().toString() + "," +
                         "browserOverride=" + overrideString + ")");
                 if (i < (rdsLength-1)) {
                     writer.append(",");
                 }
                 writer.append("\n");
             }
 
             writer.append("})\n");
         } else if (clazz.isAnnotationPresent(ICEResourceDependency.class)) {
             ICEResourceDependency rd = (ICEResourceDependency) clazz.getAnnotation(ICEResourceDependency.class);
             writer.append("@ICEResourceDependency(name=\"" + rd.name() + "\",library=\"" + rd.library() + "\",target=\"" + rd.target() + "\")\n");
         }
 
         // copy @ResourceDependency annotations
         if (clazz.isAnnotationPresent(ResourceDependencies.class)) {
             writer.append("\n");
             writer.append("@ResourceDependencies({\n");
 
             ResourceDependencies rd = (ResourceDependencies) clazz.getAnnotation(ResourceDependencies.class);
             ResourceDependency[] rds = rd.value();
             int rdsLength = rds.length;
             for (int i = 0; i < rdsLength; i++) {
                 writer.append("\t@ResourceDependency(name=\"" + rds[i].name() + "\",library=\"" + rds[i].library() + "\",target=\"" + rds[i].target() + "\")");
                 if (i < (rdsLength-1)) {
                     writer.append(",");
                 }
                 writer.append("\n");
             }
 
             writer.append("})");
             writer.append("\n\n");
         } else if (clazz.isAnnotationPresent(ResourceDependency.class)) {
             ResourceDependency rd = (ResourceDependency) clazz.getAnnotation(ResourceDependency.class);
             writer.append("@ResourceDependency(name=\"" + rd.name() + "\",library=\"" + rd.library() + "\",target=\"" + rd.target() + "\")\n\n");
         }
         
         component.componentClass();
         writer.append("public ");
         if (!component.componentClass().equals(generatedClassName)) {
             writer.append("abstract ");
         }
         writer.append("class ");
         String generatedSimpleClassName = Utility.getSimpleNameOfClass(generatedClassName);
         writer.append(generatedSimpleClassName);
         writer.append(" extends ");
         writer.append(component.extendsClass());
         StringBuilder interfaceNames = new StringBuilder();
         if (interfaceClassName != null && interfaceClassName.length() > 0) {
             interfaceNames.append(Utility.getSimpleNameOfClass(interfaceClassName));
         }
         for (Behavior behavior: compCtx.getBehaviors()) {
             if (interfaceNames.length() > 0) interfaceNames.append(',');
             interfaceNames.append(behavior.getInterfaceName());
         }
         if (interfaceNames.length() > 0) {
             writer.append(" implements ");
             writer.append(interfaceNames.toString());
         }
 
         writer.append("{\n");
 
         writer.append("\n\tpublic static final String COMPONENT_TYPE = \""+ component.componentType() + "\";");
         String rendererType = null;
         if (!"null".equals(component.rendererType()) && !"".equals(component.rendererType())) {
             rendererType = "\""+ component.rendererType() + "\"";
         }
 
         writer.append("\n\tpublic static final String RENDERER_TYPE = "+ rendererType + ";\n");
         writer.append("\n\tprivate EnumSet<PropertyKeys> setOfUsedProperties = EnumSet.noneOf(PropertyKeys.class);\n") ;
 
         writer.append("\n\tpublic ");
         writer.append(generatedSimpleClassName);
         writer.append("() {\n\t\tsuper();\n\t\tsetRendererType(RENDERER_TYPE);\n\t}\n");
 
         writer.append("\n\tpublic String getFamily() {\n\t\treturn \"");
         writer.append(Utility.getFamily(component));
         writer.append("\";\n\t}\n\n");
     }
 
     private String getOverrideString(ICEResourceDependency rd) {
         String ret = "{";
         ICEBrowserDependency[] overrides = rd.browserOverride();
 
         if (overrides.length > 0) {
             ICEBrowserDependency bd;
             for (int i = 0; i < overrides.length; i++) {
                 bd = overrides[i];
                 ret += "\t@ICEBrowserDependency(name=\"" + bd.name() + "\"," +
                             "library=\"" + bd.library() +  "\"," +
                             "target=\"" + bd.target() + "\"," +
                             "browser=BrowserType." + bd.browser().toString() + ")";
                 if (i == overrides.length - 1)
                     ret += "\n";
                 else
                     ret += ",\n";
             }
             ret += "}";
         } else ret = "{}";
 
         return ret;
     }
 
 
     private void endComponentClass(ComponentContext compCtx) {
         for (Behavior behavior: compCtx.getBehaviors()) {
             behavior.addCodeToComponent(writer);
         }
         writer.append("\n}");
         createJavaFile();
 
     }
 
     private void createJavaFile() {
         System.out.println("____________________________Creating component class_________________________");
         Component component = (Component) getMetaContext().getActiveClass().getAnnotation(Component.class);
         String componentClass =Utility.getGeneratedClassName(component);
         String fileName = Utility.getSimpleNameOfClass(componentClass) + ".java";
         System.out.println("____FileName "+ fileName);
         String pack = Utility.getPackageNameOfClass(componentClass);
         System.out.println("____package "+ pack);
         String path = Utility.getPackagePathOfClass(componentClass);
         System.out.println("____path "+ path);
         FileWriter.write("/generated/base/", path, fileName, writer);
         System.out.println("____________________________Creating component class ends_________________________");
     }
 
 
     private void addProperties(ComponentContext compCtx, ArrayList<PropertyValues> generatedProperties) {
         /*logger.info(".....addProperties and Getter and setters to componentContext");*/
         addPropertyEnum(compCtx, generatedProperties);
         addGetterSetter(compCtx, generatedProperties);
     }
 
     private void addPropertyEnum(ComponentContext compCtx, ArrayList<PropertyValues> generatedProperties) {
 
         writer.append("\n\tprotected enum PropertyKeys {\n");
         for (Behavior behavior: compCtx.getBehaviors()) {
             behavior.addPropertiesEnumToComponent(writer);
         }
         for(PropertyValues propertyValues : generatedProperties) {
             String propertyName = propertyValues.resolvePropertyName();
             if (logger.isLoggable(Level.FINE)) {
                 logger.info("Processing property " + propertyName );
             }
             if (!propertyValues.isDelegatingProperty()) {
                 String varName = propertyValues.getJavaVariableName();
                 String propKey;
                 if (!propertyName.equals(varName))  {
                     propKey = varName + "(\"" + propertyName + "\")";
                 } else {
                     propKey = propertyName;
                 }
       /*          logger.info(" \t\t varName="+varName+" propKey="+propKey);*/
                 writer.append("\t\t");
                 writer.append( propKey );
                 writer.append(",\n");
             }
         }
         for (Field field : compCtx.getInternalFieldsSorted()) {
             writer.append("\t\t");
             writer.append( field.getName() );
             writer.append(",\n");
         }
         writer.append("\t\t;\n");
         writer.append("\t\tString toString;\n");
         writer.append("\t\tPropertyKeys(String toString) { this.toString = toString; }\n");
         writer.append("\t\tPropertyKeys() { }\n");
         writer.append("\t\tpublic String toString() {\n");
         writer.append("\t\t\treturn ((toString != null) ? toString : name());\n");
         writer.append("\t\t}\n\t}\n");
     }
 
     private void addGetterSetter(ComponentContext compCtx, ArrayList<PropertyValues> generatedProperties) {
         for (Behavior behavior: compCtx.getBehaviors()) {
             behavior.addGetterSetter(this, writer);
         }
         for (PropertyValues prop : generatedProperties) {
             addGetterSetter(prop);
         }
     }
 
     /**
      * Add a getter/setter for the property. This method writes the
      * getters to return a Wrapper class for most primitive types, the exceptions being
      * if the method name has a signature from EditableValueHolder that can't be changed.
      * @param prop
      */
     public void addGetterSetter(PropertyValues prop) {
         // primitive properties are supported (ones with values
         // even if no default is specified in the Meta). Wrapper properties
         // (null default and settable values) are also supported
         // There are four property names which must be forced to be primitive                                L
         // or else the generated signature clashes with the property names in
         // EditableValueHolder
         String propertyName = prop.resolvePropertyName();
         String varName = prop.getJavaVariableName();
         boolean isPrimitive = prop.field.getType().isPrimitive() ||
                               GeneratorContext.SpecialReturnSignatures.containsKey(propertyName);
 
         String returnAndArgumentType = Utility.getGeneratedType(prop);
 
         boolean isBoolean = prop.field.getType().equals(Boolean.class) ||
                             prop.field.getType().equals(Boolean.TYPE);
 
         // The publicly exposed property name. Will differ from the field name
         // if the field name is a java keyword
         String camlCaseMethodName = propertyName.substring(0,1).toUpperCase() + propertyName.substring(1);
         String setMethodName = "set" + camlCaseMethodName;
         String getMethodName = (isBoolean ? "is" : "get") + camlCaseMethodName;
 
         //-------------------------------------
         // writing Setter
         //-------------------------------------
 
         addJavaDoc(propertyName, true, prop.javadocSet);
         writer.append("\tpublic void ");
         writer.append(setMethodName);
 
         // Allow java autoconversion to deal with most of the conversion between
         // primitive types and Wrapper classes
         writer.append("(");
         writer.append( returnAndArgumentType );
         writer.append(" ");
         writer.append(varName);
         writer.append(") {");
         writer.append("\n\t\tPropertyKeys propToUse = PropertyKeys.").append(varName).append(";");
 /*        writer.append("\nSystem.out.println(\"SETTER propToUse is\"+").append("propToUse").append(");");*/
         writer.append("\n\t\taddToSetOfUsedProperties(propToUse);");
  /*       writer.append("System.out.println("+varName+"+\" SETTER FOR PROP\");\n");*/
         if (!prop.isDelegatingProperty()) {
             writer.append("\n\t\tValueExpression ve = getValueExpression(PropertyKeys.");
             writer.append(varName);
             writer.append(".toString() );");
 //			writer.append("\n\t\tMap clientValues = null;");
             writer.append("\n\t\tif (ve != null) {");
             writer.append("\n\t\t\t// map of style values per clientId");
             writer.append("\n\t\t\tve.setValue(getFacesContext().getELContext(), ");
             writer.append(varName);
             writer.append(" );");
 
             writer.append("\n\t\t} else { ");
 
 //		    writer.append("\n\t\t\tMap clientValues = (Map) sh.get(valuesKey); ");
 //
 //			writer.append("\n\t\t\tif (clientValues == null) {" );
 //            writer.append("\n\t\t\t\tclientValues = new HashMap(); ");
 ////            writer.append("\n\t\t\t\tmarkInitialState();");
 //            writer.append("\n\t\t\t\tsh.put(PropertyKeys.");
 //			writer.append(field.getName());
 //			writer.append(", clientValues ); ");
 //			writer.append("\n\t\t\t}");
             writer.append("\n\t\t\tStateHelper sh = getStateHelper(); ");
 
             writer.append("\n\t\t\tif (isDisconnected(this))  {");
             // Here,
 
             writer.append("\n\t\t\t\tString defaultKey = PropertyKeys.").append(varName).
                     append(".toString() + \"_defaultValues\";" );
             writer.append("\n\t\t\t\tMap clientDefaults = (Map) sh.get(defaultKey);");
 
             writer.append("\n\t\t\t\tif (clientDefaults == null");
             if (!isPrimitive) {
                 writer.append(" && ").append(varName).append(" != null");
             }
             writer.append(") { ");
             writer.append("\n\t\t\t\t\tclientDefaults = new HashMap(); ");
             writer.append("\n\t\t\t\t\tclientDefaults.put(\"defValue\"," ).append(varName).append(");");
             writer.append("\n\t\t\t\t\tsh.put(defaultKey, clientDefaults); ");
             writer.append("\n\t\t\t\t} ");
 
 
             writer.append("\n\t\t\t} else {");
             writer.append("\n\t\t\t\tString clientId = getClientId();");
             writer.append("\n\t\t\t\tString valuesKey = PropertyKeys.").append(varName).
                     append(".toString() + \"_rowValues\"; ");
             writer.append("\n\t\t\t\tMap clientValues = (Map) sh.get(valuesKey); ");
             writer.append("\n\t\t\t\tif (clientValues == null) {");
             writer.append("\n\t\t\t\t\tclientValues = new HashMap(); ");
             writer.append("\n\t\t\t\t}");
             if (isPrimitive) {
                 writer.append("\n\t\t\t\tclientValues.put(clientId, " ).append(varName).append(");");
             } else {
                 writer.append("\n\t\t\t\tif (").append(varName).append(" == null) {");
                 writer.append("\n\t\t\t\t\tclientValues.remove(clientId);");
                 writer.append("\n\t\t\t\t} else {");
                 writer.append("\n\t\t\t\t\tclientValues.put(clientId, " ).append(varName).append(");");
                 writer.append("\n\t\t\t\t}");
             }
 
             writer.append("\n\t\t\t\t//Always re-add the delta values to the map. JSF merges the values into the main map" );
             writer.append("\n\t\t\t\t//and values are not state saved unless they're in the delta map. " );
 
             writer.append("\n\t\t\t\tsh.put(valuesKey, clientValues);" );
 
 
             writer.append("\n\t\t\t}" );
             writer.append("\n\t\t}" );
         } else {
             writer.append("\n\t\tsuper." + setMethodName + "(" + varName + ");");
         }
         writer.append("\n\t}\n" );
 
         //-----------------
         //getter
         //-----------------
 
         addJavaDoc(propertyName, false, prop.javadocGet);
 
         // Internal value representation is always Wrapper type
         String internalType = returnAndArgumentType;
         if (GeneratorContext.InvWrapperTypes.containsKey( returnAndArgumentType ) ) {
             internalType = GeneratorContext.InvWrapperTypes.get( returnAndArgumentType );
         }
 
         writer.append("\tpublic ");
         writer.append( returnAndArgumentType );
         writer.append(" ");
 
         writer.append(getMethodName);
         writer.append("() {");
 
         if (!prop.isDelegatingProperty()) {
             // start of the code
             writer.append("\n\t\t").append(internalType).append(" retVal = ");
 
             // No defined default value is returned as the string "null". This has to
             // be handled for various cases. primitives must have a default of some kind
             // and Strings have to return null (not "null") to work.
             String defaultValue = prop.defaultValue;
             logger.info("Evaluating field name: " + varName + ", isPRIMITIVE " +
                     isPrimitive + ", defaultValue:[" + defaultValue + "], isNull:" + (defaultValue == null));
 
             if (isPrimitive && (defaultValue == null || defaultValue.equals("") || defaultValue.equals("null"))) {
                 defaultValue = GeneratorContext.PrimitiveDefaults.get( prop.field.getType().toString().trim() );
             }
 
             boolean needsQuotes = ((internalType.indexOf("String") > -1) || (internalType.indexOf("Object") > -1));
             if ( needsQuotes && (defaultValue != null ) && (!"null".equals(defaultValue)))  {
                 writer.append("\"");
             }
             writer.append( defaultValue );
             if ( needsQuotes && (defaultValue != null ) && (!"null".equals(defaultValue)))  {
                 writer.append("\"");
             }
             writer.append(";");
 
             // Start of Value Expression code
             writer.append("\n\t\tValueExpression ve = getValueExpression( PropertyKeys.");
             writer.append(varName);
             writer.append(".toString() );");
 
             writer.append("\n\t\tif (ve != null) {" );
             // For primitives, don't overwrite a default value with a null value obtained from
             // the value expression. For the stateHelper, we're the ones putting those values
             // into the map, hence a value wont be found there.
             if (isPrimitive) {
                 writer.append("\n\t\t\tObject o = ve.getValue( getFacesContext().getELContext() );");
                 writer.append("\n\t\t\tif (o != null) { " );
                 writer.append("\n\t\t\t\tretVal = (").append( internalType ).append
                         (") o; ");
                 writer.append("\n\t\t\t}");
             } else {
                 writer.append("\n\t\t\t\tretVal = (").append( internalType ).append
                         (") ve.getValue( getFacesContext().getELContext() ); ");
             }
             writer.append("\n\t\t} else {");
             writer.append("\n\t\t\tStateHelper sh = getStateHelper(); ");
             writer.append("\n\t\t\tString valuesKey = PropertyKeys.").append(varName).append(".toString() + \"_rowValues\";");
             writer.append("\n\t\t\tMap clientValues = (Map) sh.get(valuesKey);");
             writer.append("\n\t\t\tboolean mapNoValue = false;");
             // differentiate between the case where the map has clientId and it's value is null
             // verses it not existing in the map at all.
             writer.append("\n\t\t\tif (clientValues != null) { ");
 
             writer.append("\n\t\t\t\tString clientId = getClientId();");
             writer.append("\n\t\t\t\tif (clientValues.containsKey( clientId ) ) { ");
             writer.append("\n\t\t\t\t\tretVal = (").append(internalType).append(") clientValues.get(clientId); ");
             writer.append("\n\t\t\t\t} else { ");
             writer.append("\n\t\t\t\t\tmapNoValue=true;");
             writer.append("\n\t\t\t\t}");
             writer.append("\n\t\t\t}");
 
 
             writer.append("\n\t\t\tif (mapNoValue || clientValues == null ) { ");
             writer.append("\n\t\t\t\tString defaultKey = PropertyKeys.").append(varName).append(".toString() + \"_defaultValues\";");
             writer.append("\n\t\t\t\tMap defaultValues = (Map) sh.get(defaultKey); ");
             writer.append("\n\t\t\t\tif (defaultValues != null) { ");
             writer.append("\n\t\t\t\t\tif (defaultValues.containsKey(\"defValue\" )) {");
             writer.append("\n\t\t\t\t\t\tretVal = (").append(internalType).append(") defaultValues.get(\"defValue\"); ");
             writer.append("\n\t\t\t\t\t}");
             writer.append("\n\t\t\t\t}");
             writer.append("\n\t\t\t}");
             writer.append("\n\t\t}");                       
             writer.append("\n\t\treturn retVal;");
         } else {
             writer.append("\n\t\treturn super.");
             writer.append(getMethodName + "();");
         }
         writer.append("\n\t}\n");
     }
 
     private void addJavaDoc(String name, boolean isSetter, String doc) {
         writer.append(Utility.getJavaDocComment(name, isSetter, doc));
     }
 
     private void addFacet(ComponentContext compCtx, Class clazz, Component component) {
         Iterator<Field> iterator = compCtx.getFieldsForFacet().values().iterator();
         while (iterator.hasNext()) {
             Field field = iterator.next();
             Facet facet = (Facet)field.getAnnotation(Facet.class);
             String facetName = field.getName();
             addJavaDoc(field.getName() + " facet", true, facet.javadocSet());
             writer.append("\tpublic void set");
             writer.append(field.getName().substring(0,1).toUpperCase());
             writer.append(field.getName().substring(1));
             writer.append("Facet");
 
             writer.append("(");
             writer.append(field.getType().getName());
             writer.append(" ");
             writer.append(field.getName());
             writer.append(") {\n\t\tgetFacets().put(\"");
             writer.append(facetName);
             writer.append("\", ");
             writer.append(field.getName());
             writer.append(");\n");
             writer.append("\t}\n");
 
 
             //getter
             addJavaDoc(field.getName() + " facet", false, facet.javadocGet());
             writer.append("\tpublic ");
             writer.append(field.getType().getName());
             writer.append(" ");
             writer.append("get");
             writer.append(field.getName().substring(0,1).toUpperCase());
             writer.append(field.getName().substring(1));
             writer.append("Facet");
             writer.append("() {\n");
             writer.append("\t\t return getFacet(\"");
             writer.append(facetName);
             writer.append("\");\n\t}\n");
 
         }
 
     }
 
     public void addFieldGetterSetter(Field field, org.icefaces.ace.meta.annotation.Field fieldAnnotation) {
         // primitive properties are supported (ones with values
         // even if no default is specified in the Meta). Wrapper properties
         // (null defaul`t and settable values) are also supported
         // There are four property names which must be forced to be primitive                                L
         // or else the generated signiture clashes with the property names in
         // EditableValueHolder
         String propertyName = field.getName();
         String varName = propertyName;
         boolean isPrimitive = field.getType().isPrimitive() ||
                               GeneratorContext.SpecialReturnSignatures.containsKey(propertyName);
 
         String returnAndArgumentType = Utility.getGeneratedType(propertyName, field);
 
         boolean isBoolean = field.getType().equals(Boolean.class) ||
                             field.getType().equals(Boolean.TYPE);
 
         // The publicly exposed property name. Will differ from the field name
         // if the field name is a java keyword
         String camlCaseMethodName = propertyName.substring(0,1).toUpperCase() + propertyName.substring(1);
         String setMethodName = "set" + camlCaseMethodName;
         String getMethodName = (isBoolean ? "is" : "get") + camlCaseMethodName;
 
         //-------------------------------------
         // writing Setter
         //-------------------------------------
 
         addJavaDoc(propertyName, true, fieldAnnotation.javadoc());
         writer.append("\tpublic void ");
         writer.append(setMethodName);
 
         // Allow java autoconversion to deal with most of the conversion between
         // primitive types and Wrapper classes
         writer.append("(");
         writer.append( returnAndArgumentType );
         writer.append(" ");
         writer.append(field.getName());
         writer.append(") {");
      /*   StringBuilder sb = new StringBuilder("\nSystem.out.println(\"").append("setMethodname=").append(setMethodName).append(" value=\"+").append(varName).append(");\n");
         writer.append(sb);*/
         writer.append("\n\t\tStateHelper sh = getStateHelper(); ");
         writer.append("\n\t\tString clientId = getClientId();");
         writer.append("\n\t\tString valuesKey = PropertyKeys.").append(varName).
                 append(".toString() + \"_rowValues\"; ");
         writer.append("\n\t\tMap clientValues = (Map) sh.get(valuesKey); ");
         writer.append("\n\t\tif (clientValues == null) {");
         writer.append("\n\t\t\tclientValues = new HashMap(); ");
         writer.append("\n\t\t}");
         writer.append("\n\t\tif (" + varName + " != null) clientValues.put(clientId, " + varName + ");");
         writer.append("\n\t\telse clientValues.remove(clientId);");
 
         writer.append("\n\t\t//Always re-add the delta values to the map. JSF merges the values into the main map" );
         writer.append("\n\t\t//and values are not state saved unless they're in the delta map. " );
 
         writer.append("\n\t\tsh.put(valuesKey, clientValues);" );
 
         writer.append("\n\t}\n" );
 
         //-----------------
         //getter
         //-----------------
 
         addJavaDoc(propertyName, false, fieldAnnotation.javadoc());
         // Internal value representation is always Wrapper type
         String internalType = returnAndArgumentType;
         if (GeneratorContext.InvWrapperTypes.containsKey( returnAndArgumentType ) ) {
             internalType = GeneratorContext.InvWrapperTypes.get( returnAndArgumentType );
         }
 
         writer.append("\tpublic ");
         writer.append( returnAndArgumentType );
         writer.append(" ");
 
         writer.append(getMethodName);
         writer.append("() {");
 
         // start of the code
         writer.append("\n\t\t").append(internalType).append(" retVal = ");
 
         // No defined default value is returned as the string "null". This has to
         // be handled for various cases. primitives must have a default of some kind
         // and Strings have to return null (not "null") to work.
         String defaultValue = fieldAnnotation.defaultValue();
         logger.fine("Evaluating field name: " + field.getName().trim() + ", isPRIMITIVE " +
                 isPrimitive + ", defaultValue:[" + defaultValue + "], isNull:" + (defaultValue == null));
 
         if (isPrimitive && (defaultValue == null || defaultValue.equals("") || defaultValue.equals("null"))) {
             defaultValue = GeneratorContext.PrimitiveDefaults.get( field.getType().toString().trim() );
         }
 
         boolean needsQuotes = ((internalType.indexOf("String") > -1) || (internalType.indexOf("Object") > -1));
         if ( needsQuotes && (defaultValue != null ) && (!"null".equals(defaultValue)))  {
             writer.append("\"");
         }
         writer.append( defaultValue );
         if ( needsQuotes && (defaultValue != null ) && (!"null".equals(defaultValue)))  {
             writer.append("\"");
         }
         writer.append(";");
 
         writer.append("\n\t\tStateHelper sh = getStateHelper(); ");
         writer.append("\n\t\tString valuesKey = PropertyKeys.").append(varName).append(".toString() + \"_rowValues\";");
         writer.append("\n\t\tMap clientValues = (Map) sh.get(valuesKey);");
         writer.append("\n\t\tif (clientValues != null) { ");
 
         writer.append("\n\t\t\tString clientId = getClientId();");
         writer.append("\n\t\t\tif (clientValues.containsKey( clientId ) ) { ");
         writer.append("\n\t\t\t\tretVal = (").append(internalType).append(") clientValues.get(clientId); ");
         writer.append("\n\t\t\t}");
         writer.append("\n\t\t}");
 
         writer.append("\n\t\treturn retVal;");
 
         writer.append("\n\t}\n");
     }
 
 
     private void addInternalFields(ComponentContext compCtx) {
         for (Field field : compCtx.getInternalFieldsSorted()) {
             org.icefaces.ace.meta.annotation.Field fieldAnnotation =
                 (org.icefaces.ace.meta.annotation.Field)
                     field.getAnnotation(org.icefaces.ace.meta.annotation.Field.class);
             addFieldGetterSetter(field, fieldAnnotation);
         }
     }
 
     private void isDisconnected() {
         writer.append("\n\tprivate static boolean isDisconnected(UIComponent component) {\n");
         writer.append("\t\tUIComponent parent = component.getParent();\n");
         writer.append("\t\tif (parent != null && parent instanceof UIViewRoot) {\n");
         writer.append("\t\t\treturn false;\n");
         writer.append("\t\t} else if (parent != null) {\n");
         writer.append("\t\t\treturn isDisconnected(parent);\n");
         writer.append("\t\t} else {\n");
         writer.append("\t\t\treturn true;\n");
         writer.append("\t\t}\n");
         writer.append("\t}\n");
     }
     private void isInEnumSet() {
        writer.append("\n\tprotected boolean isInEnumSet(String finder){\n");
         writer.append("\t\tif (setOfUsedProperties.isEmpty()){\n");
/*        writer.append("\t\t  System.out.println(\"     empty enum set\");\n");*/
         writer.append("\t\t\treturn false;\n");
         writer.append("\t\t}\n");
 /*        writer.append("\t\tSystem.out.println(\" size of enumset=\"+ setOfUsedProperties.size());\n");*/
        writer.append("\t\tif (null != enumTypeContains( finder)){\n");
/*       writer.append("\t\tSystem.out.println(\" have value   \");\n");*/
        writer.append("\t\t\treturn true;\n");
         writer.append("\t\t}\n");
/*      writer.append("\t\t  System.out.println(\"     NOT  found so return FALSE \");\n");*/
         writer.append("\t\treturn false;\n");
         writer.append("\t}\n");
     }
     private void addToSetOfUsedProperties(){
         writer.append("\n\t private void addToSetOfUsedProperties(PropertyKeys propertyKey){\n");
         writer.append("\t\tthis.setOfUsedProperties.add(propertyKey);\n");
  /*       writer.append("\t\tSystem.out.println(\" added prop to enumSet size=\"+setOfUsedProperties.size());\n");
         writer.append("\t\tIterator<PropertyKeys> ite = setOfUsedProperties.iterator();\n");
         writer.append("\t\tSystem.out.println(\" component type=\"+ this.getClass().getName());");*/
  /*       writer.append("\t\twhile(ite.hasNext()){\n");
         writer.append("\t\t\t\tSystem.out.println(\":- in set is =\"+ite.next());\n");
         writer.append("\t\t}\n");*/
         writer.append("\t}\n");
     }
     private void setValueExpression(){
         writer.append("\n @Override\n");
         writer.append("\tpublic void setValueExpression(String name, ValueExpression binding){\n");
         writer.append("\t\tPropertyKeys pkey = enumTypeContains( name);\n");
         writer.append("\t\tif (null != enumTypeContains( name)){\n");
         writer.append("\t\t\taddToSetOfUsedProperties(pkey);\n");
         writer.append("\t\t}\n");
         writer.append("\t\tsuper.setValueExpression(name, binding);\n");
         writer.append("\t}\n");
     }
     private void enumTypeContains(){
         writer.append("\n\tprivate PropertyKeys enumTypeContains( String name){\n");
 /*        writer.append("\t\tSystem.out.println(\" looking for label =\"+name);\n");*/
         writer.append("\t\tfor (PropertyKeys pkey: PropertyKeys.values()){\n");
 /*        writer.append("\t\tSystem.out.println(\" pkey =\"+pkey.toString());\n");*/
         writer.append("\t\t\tif (pkey.toString().equals(name)){ \n");
 /*        writer.append("\t\t\t\tSystem.out.println(\" enum contains name=\"+name); \n");*/
         writer.append("\t\t\t\treturn pkey; \n");
         writer.append("\t\t\t} \n");
         writer.append("\t\t} \n");
 /*        writer.append("\t\tSystem.out.println(\" enum DOES NOT contain name=\"+name);\n");*/
         writer.append("\t\treturn null;\n");
         writer.append("\t}\n");
     }
         private void checkEnumSetSize(){
         writer.append("\n\tprotected int checkEnumSetSize( ){\n");
         writer.append("\t\treturn setOfUsedProperties.size();\n");
         writer.append("\t}\n");
     }
 /*
     private void handleAttribute() {
         writer.append("\n\tprivate void handleAttribute(String name, Object value) {\n");
         writer.append("\t\tList<String> setAttributes = (List<String>) this.getAttributes().get(\"javax.faces.component.UIComponentBase.attributesThatAreSet\");\n");
         writer.append("\t\tif (setAttributes == null) {\n");
         writer.append("\t\t\tString cname = this.getClass().getName();\n");
         writer.append("\t\t\tif (cname != null) {\n");
         writer.append("\t\t\t\tsetAttributes = new ArrayList<String>(6);\n");
         writer.append("\t\t\t\tthis.getAttributes().put(\"javax.faces.component.UIComponentBase.attributesThatAreSet\", setAttributes);\n");
         writer.append("\t\t\t}\n\t\t}\n");
         writer.append("\t\tif (setAttributes != null) {\n");
         writer.append("\t\t\tif (value == null) {\n");
         writer.append("\t\t\t\tValueExpression ve = getValueExpression(name);\n");
         writer.append("\t\t\t\tif (ve == null) {\n");
         writer.append("\t\t\t\t\tsetAttributes.remove(name);\n");
         writer.append("\t\t\t\t}\n");
         writer.append("\t\t\t} else if (!setAttributes.contains(name)) {\n");
         writer.append("\t\t\t\tsetAttributes.add(name);\n");
         writer.append("\t\t\t}\n");
         writer.append("\t\t}\n");
         writer.append("\t\tfor(int i=0; i< setAttributes.size(); i++){\n");
         writer.append("\t\t\t System.out.println(\"Attribute set = \"+setAttributes.get(i));\n");
         writer.append("\t\t}\n") ;
         writer.append("if (setAttributes.isEmpty()){\n System.out.println(\"EMPTY LIST\");}\n");
         writer.append("\t}\n");
     }
 */
 
     public void build() {
         ComponentContext compCtx = getComponentContext();
 
         Component component = (Component) getMetaContext().getActiveClass().getAnnotation(Component.class);
         startComponentClass(compCtx, getMetaContext().getActiveClass(), component);
         addProperties(compCtx, getMetaContext().getGeneratingPropertyValuesSorted());
         addFacet(compCtx, getMetaContext().getActiveClass(), component);
         addInternalFields(compCtx);
         isDisconnected();
         addToSetOfUsedProperties();
         isInEnumSet();
         setValueExpression();
        checkEnumSetSize();
         enumTypeContains();
       /* System.out.println("BEFORE handleAttribute");*/
        /* handleAttribute();*/
      /*  System.out.println("AFTER handleAttribute");*/
         endComponentClass(compCtx);
 
         // add entry to faces-config
         GeneratorContext.getInstance().getFacesConfigBuilder().addEntry(getMetaContext().getActiveClass(), component);
 
         GeneratorContext.getInstance().getJsfTldBuilder().addTagInfo(
             getMetaContext().getActiveClass(), component);
 
         GeneratorContext.getInstance().getFaceletTagLibBuilder().addTagInfo(
             getMetaContext().getActiveClass(), component, compCtx.isGenerateHandler());
         //since generatedComponentProperties doesn't include inherited properties,
         //here all of the PropertyValues are used. This part may need re-factor-ed later.
         for (PropertyValues prop : getMetaContext().getPropertyValuesSorted()) {
             GeneratorContext.getInstance().getJsfTldBuilder().addAttributeInfo(prop);
             GeneratorContext.getInstance().getFaceletTagLibBuilder().addAttributeInfo(prop);
         }
     }
 }
 
