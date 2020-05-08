 /*****************************************************************************
  * This source file is part of SBS (Screen Build System),                    *
  * which is a component of Screen Framework                                  *
  *                                                                           *
  * Copyright (c) 2008-2013 Ratouit Thomas                                    *
  *                                                                           *
  * This program is free software; you can redistribute it and/or modify it   *
  * under the terms of the GNU Lesser General Public License as published by  *
  * the Free Software Foundation; either version 3 of the License, or (at     *
  * your option) any later version.                                           *
  *                                                                           *
  * This program is distributed in the hope that it will be useful, but       *
  * WITHOUT ANY WARRANTY; without even the implied warranty of                *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser   *
  * General Public License for more details.                                  *
  *                                                                           *
  * You should have received a copy of the GNU Lesser General Public License  *
  * along with this program; if not, write to the Free Software Foundation,   *
  * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA, or go to   *
  * http://www.gnu.org/copyleft/lesser.txt.                                   *
  *****************************************************************************/
 
 package com.thoratou.exact.processors;
 
 import com.thoratou.exact.annotations.ExactExtension;
 import com.thoratou.exact.annotations.ExactNode;
 import com.thoratou.exact.annotations.ExactPath;
 import com.thoratou.exact.exception.ExactException;
 import com.thoratou.exact.exception.ExactXPathNotSupportedException;
 import com.thoratou.exact.xpath.XPathParser;
 import com.thoratou.exact.xpath.ast.XPathPathExpr;
 import com.thoratou.exact.xpath.ast.XPathStep;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.functors.EqualPredicate;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 import org.apache.velocity.runtime.RuntimeConstants;
 
 import javax.annotation.processing.*;
 import javax.lang.model.SourceVersion;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.Name;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.util.ElementFilter;
 import javax.tools.JavaFileObject;
 import java.io.*;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 @SupportedAnnotationTypes("com.thoratou.exact.annotations.ExactNode")
 @SupportedSourceVersion(SourceVersion.RELEASE_7)
 public class ExactProcessor extends AbstractProcessor{
 
     private static Logger logger = CustomLogger.getLogger();
     private ProcessingEnvironment processingEnv;
     public static HashMap<String, TypeElement> classMap = new HashMap<String, TypeElement>();
     public static HashMap<Pair<String,String>, ExecutableElement> methodMap = new HashMap<Pair<String, String>, ExecutableElement>();
 
     @Override
     public synchronized void init(ProcessingEnvironment processingEnv) {
         this.processingEnv = processingEnv;
         super.init(processingEnv);
     }
 
     @Override
     public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
         if(!roundEnv.processingOver()){
 
             try {
                 HashMap<String, ArrayList<Item> > itemMap = new HashMap<String, ArrayList<Item>>();
                 HashMap<String, ArrayList<ExtensionItem> > extItemMap = new HashMap<String, ArrayList<ExtensionItem>>();
                 readAnnotations(roundEnv, itemMap, extItemMap);
 
                 HashMap<String, List<PathStep> > mergedMap
                         = new HashMap<String, List<PathStep>>();
                 mergeClassPaths(itemMap,extItemMap,mergedMap);
 
                 writeSources(mergedMap);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return false;
     }
 
     private void readAnnotations(RoundEnvironment roundEnv, HashMap<String, ArrayList<Item>> itemMap, HashMap<String, ArrayList<ExtensionItem>> extItemMap)
             throws Exception {
         //retrieve all classes with @ExactNode annotation
         for(TypeElement typeElement : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(ExactNode.class))){
             Name qualifiedName = typeElement.getQualifiedName();
             String className = qualifiedName.toString();
             logger.info("Exact class : "+className);
             classMap.put(className, typeElement);
 
             for(ExecutableElement methodElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())){
                 {
                     ExactPath annotation = methodElement.getAnnotation(ExactPath.class);
                     if(annotation != null){
                         String methodName = methodElement.getSimpleName().toString();
                         String returnType = methodElement.getReturnType().toString();
                         String xPathString = annotation.value();
 
                         logger.info("Exact method : "+methodName+" , "+annotation.value()+" , "+returnType);
 
                         XPathParser parser = new XPathParser(xPathString);
                         XPathPathExpr xPathPathExpr = parser.parse();
 
                         logger.info("XPath value = "+xPathPathExpr.toString());
 
                         Item item = new Item();
                         item.setxPathPathExpr(xPathPathExpr);
                         item.setMethodName(methodName);
                         item.setReturnType(returnType);
 
                         if(itemMap.containsKey(className)){
                             ArrayList<Item> items = itemMap.get(className);
                             items.add(item);
                         }
                         else{
                             ArrayList<Item> items = new ArrayList<Item>();
                             items.add(item);
                             itemMap.put(className,items);
                         }
 
                         methodMap.put(new Pair<String, String>(className, methodName), methodElement);
                     }
                 }
 
                 {
                     ExactExtension annotation = methodElement.getAnnotation(ExactExtension.class);
                     if(annotation!=null){
                         String methodName = methodElement.getSimpleName().toString();
                         String returnType = methodElement.getReturnType().toString();
                         String name = annotation.name();
                         String element = annotation.element();
                         String filter = annotation.filter();
 
                         logger.info("Exact extension : "+methodName+" , "+returnType+" , "+name+" , "+element+" , "+filter);
 
                         XPathParser elementParser = new XPathParser(element);
                         XPathPathExpr elementXPathPathExpr = elementParser.parse();
                         logger.info("XPath element = "+elementXPathPathExpr.toString());
 
                         XPathParser filterParser = new XPathParser(filter);
                         XPathPathExpr filterXPathPathExpr = filterParser.parse();
                         logger.info("XPath filter = "+filterXPathPathExpr.toString());
 
                         ExtensionItem item = new ExtensionItem();
                         item.setName(name);
                         item.setElement(elementXPathPathExpr);
                         item.setFilter(filterXPathPathExpr);
                         item.setMethodName(methodName);
                         item.setReturnType(returnType);
 
                         if(extItemMap.containsKey(className)){
                             ArrayList<ExtensionItem> items = extItemMap.get(className);
                             items.add(item);
                         }
                         else{
                             ArrayList<ExtensionItem> items = new ArrayList<ExtensionItem>();
                             items.add(item);
                             extItemMap.put(className,items);
                         }
                     }
                 }
             }
         }
     }
 
     private void mergeClassPaths(HashMap<String, ArrayList<Item>> itemMap,
                                  HashMap<String, ArrayList<ExtensionItem>> extItemMap,
                                  HashMap<String, List<PathStep>> mergedMap) throws ExactXPathNotSupportedException, ClassNotFoundException {
         /*
         convert Xpath path map into step-by-step merged representation
         example :
 
         input :
             Boms :
             { 'com.thoratou.example.SimpleBom' ->
                  [
                     {'toto/titi/text()', 'getTiti()', 'FieldString'},
                     {'toto/tata/@value', 'getTata()', 'FieldString'}
                     {'toto/tata/child', 'getChild()', 'com.thoratou.example.ChildBom'}
                  ]
             }
             Extensions
             { 'com.thoratou.example.SimpleBom' ->
                  [
                     {'ext', 'toto/tata/ext', '@filter', 'getExtension()', 'com.thoratou.example.ExtensionBom'}
                  ]
             }
 
         output :
             { 'com.thoratou.example.SimpleBom', ->
                  [
                     {CHILD_ELEMENT, 'toto'} ->
                     [
                         {CHILD_ELEMENT, 'titi'} ->
                         [
                             {TEXT, 'getTiti()', 'FieldString'}
                         ],
                         {CHILD_ELEMENT, 'tata'} ->
                         [
                             {ATTRIBUTE, 'value',  'getTata()', 'FieldString'}
                             {CHILD_ELEMENT, 'child'} ->
                             [
                                 {BOM, 'getChild()', 'com.thoratou.example.ChildBom'}
                             ]
                             {CHILD_ELEMENT, 'ext'} ->
                             [
                                 {EXTENSION, 'getExtension()', 'com.thoratou.example.ExtensionBom'} ->
                                 [
                                     {ATTRIBUTE, 'filter', '', 'FieldString'}
                                 ]
                             ]
                         ]
                     ]
                  ]
             }
          */
 
         for(Map.Entry<String, ArrayList<Item>> itemEntry : itemMap.entrySet()){
             //retrieve bom basic data
             String baseClassName = itemEntry.getKey();
             ArrayList<Item> itemList = itemEntry.getValue();
 
             //initialize new representation for this bom
            List<PathStep> newPathStepList = new ArrayList<PathStep>();
            mergedMap.put(baseClassName, newPathStepList);
             for(Item item : itemList){
                 XPathPathExpr xPathPathExpr = item.getxPathPathExpr();
                 String methodName = item.getMethodName();
                 String returnType = item.getReturnType();
                 convertXPathExpr(xPathPathExpr,
                         methodName,
                         returnType,
                         newPathStepList,
                         false);
             }
         }
 
         for(Map.Entry<String, ArrayList<ExtensionItem>> extItemEntry : extItemMap.entrySet()){
             //retrieve bom basic data
             String baseClassName = extItemEntry.getKey();
             ArrayList<ExtensionItem> extItemList = extItemEntry.getValue();
 
             //initialize new representation for this bom
            List<PathStep> newPathStepList = new ArrayList<PathStep>();
            mergedMap.put(baseClassName, newPathStepList);
             for(ExtensionItem extItem : extItemList){
                 XPathPathExpr xPathPathExpr = extItem.getElement();
                 String methodName = extItem.getMethodName();
                 String returnType = extItem.getReturnType();
                 PathStep lastPathStep = convertXPathExpr(xPathPathExpr,
                         methodName,
                         returnType,
                         newPathStepList,
                         true);
                 lastPathStep.setExtensionName(extItem.getName());
                 //add filter steps
                 convertXPathSteps(extItem.getFilter().steps,
                        "", //mthod name (value doesn't matter for filters)
                         "com.thoratou.exact.fields.FieldString", //TODO : add other fields and bom support
                         lastPathStep.getChildSteps(),
                         false);
             }
         }
     }
 
     private PathStep convertXPathExpr(XPathPathExpr xPathPathExpr,
                                       String methodName,
                                       String returnType,
                                       List<PathStep> newPathStepList,
                                       boolean isExtension) throws ExactXPathNotSupportedException, ClassNotFoundException {
         PathStep rootStep = new PathStep();
         rootStep.setStepKind(PathStep.Kind.START);
         switch (xPathPathExpr.context) {
             case ABS:
                 //rootStep.setStartKind(PathStep.StartKind.ABSOLUTE);
                 throw new ExactXPathNotSupportedException(xPathPathExpr);
             case REL:
                 rootStep.setStartKind(PathStep.StartKind.RELATIVE);
                 break;
         }
         PathStep targetStep = addOrMergePathStep(rootStep, newPathStepList);
         return convertXPathSteps(xPathPathExpr.steps, methodName, returnType, targetStep.getChildSteps(), isExtension);
     }
 
     private PathStep convertXPathSteps(XPathStep[] steps,
                                        String methodName,
                                        String returnType,
                                        List<PathStep> newPathStepList,
                                        boolean extension) throws ExactXPathNotSupportedException, ClassNotFoundException {
         List<PathStep> currentList = newPathStepList;
         PathStep lastPathStep = null;
         for(XPathStep step : steps){
             PathStep pathStep = new PathStep();
             switch (step.axis) {
                 case CHILD:
                     switch (step.test) {
                         case NAME:
                             pathStep.setStepKind(PathStep.Kind.CHILD_ELEMENT);
                             pathStep.setStepValue(step.testStr());
                             //logger.info("step : child, "+pathStep.getStepValue());
                             break;
                         case TYPE_TEXT:
                             pathStep.setStepKind(PathStep.Kind.TEXT);
                             pathStep.setMethodName(methodName);
                             pathStep.setReturnType(returnType);
                             //logger.info("step : text, "+pathStep.getStepValue());
                             break;
                         case NAME_WILDCARD:
                         case NAMESPACE_WILDCARD:
                         case TYPE_NODE:
                         case TYPE_COMMENT:
                         case TYPE_PROCESSING_INSTRUCTION:
                             throw new ExactXPathNotSupportedException(step);
                     }
                     break;
                 case ATTRIBUTE:
                     pathStep.setStepKind(PathStep.Kind.ATTRIBUTE);
                     pathStep.setStepValue(step.testStr());
                     pathStep.setMethodName(methodName);
                     pathStep.setReturnType(returnType);
                     //logger.info("step : attribute, "+pathStep.getStepValue());
                     break;
                 case DESCENDANT:
                 case PARENT:
                 case ANCESTOR:
                 case FOLLOWING_SIBLING:
                 case PRECEDING_SIBLING:
                 case FOLLOWING:
                 case PRECEDING:
                 case NAMESPACE:
                 case SELF:
                 case DESCENDANT_OR_SELF:
                 case ANCESTOR_OR_SELF:
                     throw new ExactXPathNotSupportedException(step);
             }
 
             PathStep targetStep = addOrMergePathStep(pathStep, currentList);
 
             //save last target step
             lastPathStep = targetStep;
 
             //move to sub list
             currentList = targetStep.getChildSteps();
         }
 
         PathStep bomStep = new PathStep();
         bomStep.setMethodName(methodName);
         bomStep.setReturnType(returnType);
         PathStep.TypeKind returnTypeKind = bomStep.getReturnTypeKind();
         switch (returnTypeKind) {
             case UNKNOWN:
                 break;
             case FIELD:
                 break;
             case LIST:
                 PathStep.ListTypeKind returnListTypeKind = bomStep.getReturnListTypeKind();
                 switch (returnListTypeKind) {
                     case UNKNOWN:
                         break;
                     case FIELD:
                         break;
                     case BOM:
                         //add a specific bom step
                         bomStep.setStepKind(extension ? PathStep.Kind.EXTENSION : PathStep.Kind.BOM);
                         lastPathStep.getChildSteps().add(bomStep);
                         lastPathStep = bomStep;
                         break;
                 }
                 break;
             case BOM:
                 //add a specific bom step
                 bomStep.setStepKind(extension ? PathStep.Kind.EXTENSION : PathStep.Kind.BOM);
                 lastPathStep.getChildSteps().add(bomStep);
                 lastPathStep = bomStep;
                 break;
         }
 
         return lastPathStep;
     }
 
     private PathStep addOrMergePathStep(PathStep pathStep, Collection currentList){
         PathStep returnStep = null;
         /*final PathStep finalPathStep = pathStep;
         @SuppressWarnings("unchecked")
         Collection<PathStep> select = CollectionUtils.select(currentList, new Predicate(){
             @Override
             public boolean evaluate(Object o) {
                 PathStep ps = (PathStep) o;
                 logger.info("compare : "+ps+" * "+finalPathStep);
                 return finalPathStep.equals(ps);  //To change body of implemented methods use File | Settings | File Templates.
             }
         });*/
         @SuppressWarnings("unchecked")
         Collection<PathStep> select = CollectionUtils.select(currentList, new EqualPredicate(pathStep));
         if(select.isEmpty()){
             //logger.info("new path step : "+pathStep);
             //new step => add new one in tree
             currentList.add(pathStep);
             returnStep = pathStep;
         }
         else if(select.size() == 1){
             //existing step => merge with existing one
             returnStep = select.iterator().next();
             //logger.info("existing path step : "+pathStep);
         }
         else{
             logger.log(Level.SEVERE, "internal error, more than one data for the same path step");
             //TODO : exception handling
         }
         return returnStep;
     }
 
     private void writeSources(HashMap<String, List<PathStep>> mergedMap)
             throws IOException, ClassNotFoundException, ExactException {
         //use all annotation data to generate parsing files
         for(Map.Entry<String, List<PathStep>> entryList : mergedMap.entrySet()) {
             VelocityEngine engine = new VelocityEngine();
             //needed it avoid global instead of local variable modification
             engine.setProperty(RuntimeConstants.VM_CONTEXT_LOCALSCOPE, true);
 
             //read file into JAR
             InputStream configStream = getClass().getResourceAsStream("/xmlreader.vm");
             BufferedReader configReader = new BufferedReader(new InputStreamReader(configStream, "UTF-8"));
 
             String className = entryList.getKey();
             List<PathStep> steps = entryList.getValue();
 
             VelocityContext context = new VelocityContext();
             context.put("class", className);
             //logger.info("class : "+className);
 
             //ugly temp code
             String[] split = className.split("\\.");
             StringBuffer packageBuffer = new StringBuffer();
             for(int i = 0; i<split.length -1; i++){
                 packageBuffer.append(split[i]);
                 if(i != split.length - 2){
                     packageBuffer.append(".");
                 }
             }
             String packageName = packageBuffer.toString();
             //logger.info("package : "+packageName);
             context.put("package", packageName);
             String simpleName = split[split.length-1];
             //logger.info("simpleclass : "+simpleName);
             context.put("simpleclass", simpleName);
 
 
             context.put("steps",steps);
             context.put("kindmap", PathStep.ReverseKindMap);
             context.put("startmap", PathStep.ReverseStartMap);
             context.put("typemap", PathStep.ReverseTypeMap);
             context.put("listtypemap", PathStep.ReverseListTypeMap);
             context.put("indentutil", new IndentUtil());
             context.put("processingutil", new ProcessingUtil());
 
             Set<String> bomList = new HashSet<String>();
             registerBomListFromSteps(steps, bomList);
             context.put("bomlist", bomList);
 
             Set<String> extensionList = new HashSet<String>();
             Map<String, ExtensionVelocityData> extensionMap = new HashMap<String, ExtensionVelocityData>();
             registerExtensionListFromSteps(steps, extensionList, extensionMap);
             context.put("extensionlist", extensionList);
             context.put("extensionmap", extensionMap);
 
             logger.info("input velocity data : "+className+ " , "+steps.toString());
 
             //StringWriter writer = new StringWriter();
             //String packagePath = packageName.replace(".","/");
             //String fullFile = packagePath+"/"+simpleName+"XmlReader.java";
             //logger.info(fullFile);
 
             Filer filer = processingEnv.getFiler();
             JavaFileObject sourceFile = filer.createSourceFile(className+"XmlReader");
             Writer sourceWriter = sourceFile.openWriter();
 
             engine.evaluate(context, sourceWriter, "", configReader);
 
             sourceWriter.close();
             sourceFile.delete();
 
             //logger.info("final velocity data : "+writer.getBuffer().toString());
         }
     }
 
     private void registerBomListFromSteps(List<PathStep> steps, Set<String> bomList) throws ClassNotFoundException {
         for(PathStep step : steps){
             if(step.getStepKind() == PathStep.Kind.BOM){
                 PathStep.TypeKind returnTypeKind = step.getReturnTypeKind();
                 switch (returnTypeKind) {
                     case UNKNOWN:
                         break;
                     case FIELD:
                         break;
                     case LIST:
                         PathStep.ListTypeKind returnListTypeKind = step.getReturnListTypeKind();
                         switch (returnListTypeKind) {
                             case UNKNOWN:
                                 break;
                             case FIELD:
                                 break;
                             case BOM:
                                 String returnType = step.getReturnType();
                                 List<String> typeParameterList = PathStep.getTypeParameterList(returnType);
                                 String bomName = typeParameterList.get(0);
                                 bomList.add(bomName);
                                 break;
                         }
                         break;
                     case BOM:
                         String bomName = step.getReturnType();
                         bomList.add(bomName);
                         break;
                 }
             }
 
             registerBomListFromSteps(step.getChildSteps(), bomList);
         }
     }
 
     private void registerExtensionListFromSteps(List<PathStep> steps,
                                                 Set<String> extensionList,
                                                 Map<String, ExtensionVelocityData> extensionMap) throws ClassNotFoundException, ExactException {
         for(PathStep step : steps){
             if(step.getStepKind() == PathStep.Kind.EXTENSION){
                 ExtensionVelocityData extensionVelocityData = new ExtensionVelocityData();
                 extensionVelocityData.setExtensionStep(step);
                 extensionVelocityData.setFilterKind(retrieveExtensionFilterKind(step));
                 extensionMap.put(step.getExtensionName(), extensionVelocityData);
                 extensionList.add(step.getExtensionName());
             }
             registerExtensionListFromSteps(step.getChildSteps(), extensionList, extensionMap);
         }
     }
 
     private PathStep.Kind retrieveExtensionFilterKind(PathStep step) throws ExactException {
         for(PathStep child : step.getChildSteps()) {
             switch (child.getStepKind()) {
                 case UNKNOWN:
                     throw new ExactException("");
                 case CHILD_ELEMENT:
                     return retrieveExtensionFilterKind(child);
                 case TEXT:
                 case ATTRIBUTE:
                     return child.getStepKind();
                 case BOM:
                     throw new ExactException("");
                 case EXTENSION:
                     throw new ExactException("");
                 case START:
                     throw new ExactException("");
             }
         }
         return PathStep.Kind.UNKNOWN;
     }
 }
