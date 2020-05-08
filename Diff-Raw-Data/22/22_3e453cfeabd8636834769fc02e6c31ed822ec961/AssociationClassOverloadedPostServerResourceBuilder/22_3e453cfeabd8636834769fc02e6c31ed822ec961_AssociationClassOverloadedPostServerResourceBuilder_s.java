 package org.umlg.restlet.visitor.clazz;
 
 import org.eclipse.uml2.uml.AssociationClass;
 import org.eclipse.uml2.uml.Classifier;
 import org.eclipse.uml2.uml.Property;
 import org.umlg.framework.Visitor;
 import org.umlg.generation.Workspace;
 import org.umlg.java.metamodel.*;
 import org.umlg.java.metamodel.annotation.*;
 import org.umlg.javageneration.util.PropertyWrapper;
 import org.umlg.javageneration.util.TinkerGenerationUtil;
 import org.umlg.javageneration.util.TumlClassOperations;
 import org.umlg.restlet.generation.RestletVisitors;
 import org.umlg.restlet.util.TumlRestletGenerationUtil;
 import org.umlg.restlet.visitor.model.QueryExecuteResourceBuilder;
 
 import java.util.List;
 import java.util.Set;
 
 public class AssociationClassOverloadedPostServerResourceBuilder extends BaseServerResourceBuilder implements Visitor<AssociationClass> {
 
     public AssociationClassOverloadedPostServerResourceBuilder(Workspace workspace, String sourceDir) {
         super(workspace, sourceDir);
     }
 
     @Override
     public void visitBefore(AssociationClass ac) {
 
         //This is to navigate from the property to the association class itself
         List<Property> memberEnds = ac.getMemberEnds();
         for (Property memberEnd : memberEnds) {
 
             PropertyWrapper pWrap = new PropertyWrapper(memberEnd);
 
             OJAnnotatedClass owner = findOJClass(pWrap.getType());
 
             OJAnnotatedInterface annotatedInf = new OJAnnotatedInterface(TumlClassOperations.getPathName(pWrap.getOwningType()).getLast() + "_"
                     + pWrap.getOtherEnd().getName() + "_" + ac.getName() + "_ServerResource");
             OJPackage ojPackage = new OJPackage(owner.getMyPackage().toString() + ".restlet");
             annotatedInf.setMyPackage(ojPackage);
             addToSource(annotatedInf);
 
             OJAnnotatedClass annotatedClass = new OJAnnotatedClass(TumlClassOperations.getPathName(pWrap.getOwningType()).getLast() + "_"
                     + pWrap.getOtherEnd().getName() + "_" + ac.getName() + "_ServerResourceImpl");
             annotatedClass.setSuperclass(TumlRestletGenerationUtil.ServerResource);
             annotatedClass.addToImplementedInterfaces(annotatedInf.getPathName());
             annotatedClass.setMyPackage(ojPackage);
             addToSource(annotatedClass);
             addDefaultConstructor(annotatedClass);
 
             addCompositeParentIdField(pWrap, annotatedClass);
             addGetObjectRepresentation(pWrap, annotatedInf, annotatedClass);
 
             addPostObjectRepresentation(pWrap, annotatedInf, annotatedClass);
             addServerResourceToRouterEnum(pWrap, annotatedClass);
         }
 
         //And for navigating from the association class to its member ends
         for (Property memberEnd : memberEnds) {
             PropertyWrapper pWrap = new PropertyWrapper(memberEnd);
             PropertyWrapper otherEndWrap = new PropertyWrapper(pWrap.getOtherEnd());
 
             OJAnnotatedClass owner = findOJClass(pWrap.getType());
 
             OJAnnotatedInterface annotatedInf = new OJAnnotatedInterface(pWrap.getAssociationClassPathName().getLast()
                     + "_" + pWrap.getName() + "_ServerResource");
             OJPackage ojPackage = new OJPackage(owner.getMyPackage().toString() + ".restlet");
             annotatedInf.setMyPackage(ojPackage);
             addToSource(annotatedInf);
 
             OJAnnotatedClass annotatedClass = new OJAnnotatedClass(pWrap.getAssociationClassPathName().getLast()
                     + "_" + pWrap.getName() + "_ServerResourceImpl");
             annotatedClass.setSuperclass(TumlRestletGenerationUtil.ServerResource);
             annotatedClass.addToImplementedInterfaces(annotatedInf.getPathName());
             annotatedClass.setMyPackage(ojPackage);
             addToSource(annotatedClass);
             addDefaultConstructor(annotatedClass);
 
             addCompositeParentIdField(pWrap, annotatedClass, true);
             addGetObjectRepresentation(pWrap, annotatedInf, annotatedClass, true);
 
             addPostObjectRepresentation(otherEndWrap, annotatedInf, annotatedClass, true);
             addAssociationClassServerResourceToRouterEnum(pWrap, annotatedClass);
         }
     }
 
     @Override
     public void visitAfter(AssociationClass ac) {
     }
 
     private void addGetObjectRepresentation(PropertyWrapper pWrap, OJAnnotatedInterface annotatedInf, OJAnnotatedClass annotatedClass) {
         addGetObjectRepresentation(pWrap, annotatedInf, annotatedClass, false);
     }
 
     private void addGetObjectRepresentation(PropertyWrapper pWrap, OJAnnotatedInterface annotatedInf, OJAnnotatedClass annotatedClass, boolean asAssociationClass) {
 
         OJAnnotatedOperation get = new OJAnnotatedOperation("get", TumlRestletGenerationUtil.Representation);
         get.addToThrows(TumlRestletGenerationUtil.ResourceException);
         annotatedClass.addToImports(TumlRestletGenerationUtil.ResourceException);
 
         OJTryStatement tryStatement = new OJTryStatement();
         OJPathName parentPathName;
         if (!asAssociationClass) {
             parentPathName = TumlClassOperations.getPathName(pWrap.getOtherEnd().getType());
         } else {
             parentPathName = pWrap.getAssociationClassPathName();
         }
         tryStatement.getTryPart().addToStatements(
                "this." + parentPathName.getLast().toLowerCase() + "Id = (String)getRequestAttributes().get(\""
                         + parentPathName.getLast().toLowerCase() + "Id\")");
         tryStatement.getTryPart().addToStatements(
                 parentPathName.getLast() + " parentResource = GraphDb.getDb().instantiateClassifier(" + parentPathName.getLast().toLowerCase() + "Id" + ")");
         annotatedClass.addToImports(parentPathName);
         buildToJson(pWrap, annotatedClass, tryStatement.getTryPart(), asAssociationClass);
         get.getBody().addToStatements(tryStatement);
         tryStatement.getFinallyPart().addToStatements(TinkerGenerationUtil.graphDbAccess + ".rollback()");
         tryStatement.setCatchPart(null);
         annotatedClass.addToImports(TinkerGenerationUtil.graphDbPathName);
         annotatedClass.addToImports(TumlRestletGenerationUtil.JsonRepresentation);
         annotatedClass.addToOperations(get);
     }
 
     private void addDeleteResource(PropertyWrapper pWrap, OJAnnotatedClass annotatedClass, OJPathName parentPathName) {
         OJAnnotatedOperation delete = new OJAnnotatedOperation("delete");
         delete.setVisibility(OJVisibilityKind.PRIVATE);
         annotatedClass.addToOperations(delete);
         annotatedClass.addToImports(pWrap.javaBaseTypePath());
         if (pWrap.isComposite()) {
             delete.addToParameters(new OJParameter("propertyMap", new OJPathName("java.util.Map").addToGenerics("String").addToGenerics("Object")));
             delete.getBody().addToStatements("Object id = propertyMap.get(\"id\")");
             delete.getBody().addToStatements(pWrap.javaBaseTypePath().getLast() + " childResource = GraphDb.getDb().instantiateClassifier(id)");
             delete.getBody().addToStatements("childResource.delete()");
         } else {
             delete.addToParameters(new OJParameter("parentResource", parentPathName));
             delete.addToParameters(new OJParameter("propertyMap", new OJPathName("java.util.Map").addToGenerics("String").addToGenerics("Object")));
             delete.getBody().addToStatements("Object id = propertyMap.get(\"id\")");
             delete.getBody().addToStatements(pWrap.javaBaseTypePath().getLast() + " childResource = GraphDb.getDb().instantiateClassifier(id)");
             delete.getBody().addToStatements("parentResource." + pWrap.remover() + "(childResource)");
         }
 
     }
 
     private void addPostObjectRepresentation(PropertyWrapper pWrap, OJAnnotatedInterface annotatedInf, OJAnnotatedClass annotatedClass) {
         addPostObjectRepresentation(pWrap, annotatedInf, annotatedClass, false);
     }
 
     private void addPostObjectRepresentation(PropertyWrapper pWrap, OJAnnotatedInterface annotatedInf, OJAnnotatedClass annotatedClass, boolean asAssociationClass) {
         OJAnnotatedOperation postInf = new OJAnnotatedOperation("post", TumlRestletGenerationUtil.Representation);
         postInf.addToParameters(new OJParameter("entity", TumlRestletGenerationUtil.Representation));
         annotatedInf.addToOperations(postInf);
         postInf.addAnnotationIfNew(new OJAnnotationValue(TumlRestletGenerationUtil.Post, "json"));
 
         OJAnnotatedOperation post = new OJAnnotatedOperation("post", TumlRestletGenerationUtil.Representation);
         post.addToParameters(new OJParameter("entity", TumlRestletGenerationUtil.Representation));
         post.addToThrows(TumlRestletGenerationUtil.ResourceException);
         annotatedClass.addToImports(TumlRestletGenerationUtil.ResourceException);
         TinkerGenerationUtil.addOverrideAnnotation(post);
         TinkerGenerationUtil.addSuppressWarning(post);
 
         PropertyWrapper otherEndPWrap = new PropertyWrapper(pWrap.getOtherEnd());
 
         OJPathName parentPathName;
         if (!asAssociationClass) {
             parentPathName = otherEndPWrap.javaBaseTypePath();
         } else {
             parentPathName = pWrap.getAssociationClassPathName();
         }
 
         OJTryStatement ojTryStatement = new OJTryStatement();
         OJField mapper = new OJField("mapper", TinkerGenerationUtil.ObjectMapper);
         mapper.setInitExp("new ObjectMapper()");
         ojTryStatement.getTryPart().addToLocals(mapper);
 
         OJPathName pathName = new OJPathName("java.util.Map").addToGenerics("String").addToGenerics("Object");
         OJAnnotatedField entityText = new OJAnnotatedField("entityText", "String");
         entityText.setInitExp("entity.getText()");
         ojTryStatement.getTryPart().addToLocals(entityText);
 
         OJField resultMap = new OJField("resultMap", new OJPathName("java.util.Map").addToGenerics(new OJPathName("Class<? extends " + pWrap.getAssociationClassPathName().getLast() + ">")).addToGenerics("java.lang.StringBuilder"));
         resultMap.setInitExp("new HashMap<Class<? extends " + pWrap.getAssociationClassPathName().getLast() + ">, StringBuilder>()");
         annotatedClass.addToImports("java.util.HashMap");
         ojTryStatement.getTryPart().addToLocals(resultMap);
 
         ojTryStatement.getTryPart().addToStatements(pathName.getLast() + " overloaded = mapper.readValue(" + entityText.getName() + ", Map.class)");
 
         //Insert
         ojTryStatement.getTryPart().addToStatements("Object o = overloaded.get(\"insert\")");
         OJIfStatement ifInsert = new OJIfStatement("o != null");
         OJIfStatement ifArrayForInsert = new OJIfStatement("o instanceof ArrayList");
         OJIfStatement ifEmpty = new OJIfStatement("!((List)o).isEmpty()");
         ifArrayForInsert.addToThenPart(ifEmpty);
         annotatedClass.addToImports("java.util.List");
         ifEmpty.addToThenPart("throw new IllegalStateException(\"Adding an association class happens from the association member end!\")");
         ifArrayForInsert.addToElsePart("throw new IllegalStateException(\"Adding an association class happens from the association member end!\")");
         ifInsert.addToThenPart(ifArrayForInsert);
         ojTryStatement.getTryPart().addToStatements(ifInsert);
 
         //Delete
         ojTryStatement.getTryPart().addToStatements("o = overloaded.get(\"delete\")");
         OJIfStatement ifDelete = new OJIfStatement("o != null");
         OJIfStatement ifArrayForDelete = new OJIfStatement("o instanceof ArrayList");
         ifEmpty = new OJIfStatement("!((List)o).isEmpty()");
         ifArrayForDelete.addToThenPart(ifEmpty);
         ifEmpty.addToThenPart("throw new IllegalStateException(\"Deleting an association class happens from the association member end!\")");
         ifArrayForDelete.addToElsePart("throw new IllegalStateException(\"Deleting an association class happens from the association member end!\")");
         ifDelete.addToThenPart(ifArrayForDelete);
         ojTryStatement.getTryPart().addToStatements(ifDelete);
 
         //Update
         ojTryStatement.getTryPart().addToStatements("o = overloaded.get(\"update\")");
         OJIfStatement ifUpdate = new OJIfStatement("o != null");
         OJIfStatement ifArrayForUpdate = new OJIfStatement("o instanceof ArrayList");
         OJPathName genericsForArray = new OJPathName("java.util.Map").addToGenerics("String").addToGenerics("Object");
         OJField array = new OJField("array", new OJPathName("java.util.ArrayList").addToGenerics(genericsForArray));
         array.setInitExp("(ArrayList<Map<String, Object>>)o");
         ifArrayForUpdate.getThenPart().addToLocals(array);
         ifUpdate.addToThenPart(ifArrayForUpdate);
         ojTryStatement.getTryPart().addToStatements(ifUpdate);
 
         OJForStatement forArray = new OJForStatement("map", new OJPathName("java.util.Map").addToGenerics(new OJPathName("String")).addToGenerics(
                 new OJPathName("Object")), "array");
         ifArrayForUpdate.addToThenPart(forArray);
         forArray.getBody().addToStatements("put(resultMap, map)");
         OJField map = new OJField("map", new OJPathName("java.util.Map").addToGenerics("String").addToGenerics("Object"));
         map.setInitExp("(Map<String, Object>) o");
         ifArrayForUpdate.setElsePart(new OJBlock());
         ifArrayForUpdate.getElsePart().addToLocals(map);
         ifArrayForUpdate.getElsePart().addToStatements("put(resultMap, map)");
 
         addPutResource(pWrap, annotatedClass, parentPathName);
 
         //Check if transaction needs commiting
         commitOrRollback(ojTryStatement.getTryPart());
 
         OJBlock jsonResultBlock = new OJBlock();
         ojTryStatement.getTryPart().addToStatements(jsonResultBlock);
 
         OJField result = new OJField("result", "java.lang.StringBuilder");
         result.setInitExp("new StringBuilder(\"[\")");
         jsonResultBlock.addToLocals(result);
         OJField count = new OJField("count", "int");
         count.setInitExp("1");
         jsonResultBlock.addToLocals(count);
 
         OJForStatement forConcreteClassifiers = new OJForStatement("baseClass", new OJPathName("Class").addToGenerics("? extends " + pWrap.getAssociationClassPathName().getLast()), "resultMap.keySet()");
         jsonResultBlock.addToStatements(forConcreteClassifiers);
         if (pWrap.isOne()) {
             forConcreteClassifiers.getBody().addToStatements("result.append(\"{\\\"data\\\": \")");
         } else {
             forConcreteClassifiers.getBody().addToStatements("result.append(\"{\\\"data\\\": [\")");
         }
         forConcreteClassifiers.getBody().addToStatements("result.append(resultMap.get(baseClass))");
         if (pWrap.isOne()) {
             forConcreteClassifiers.getBody().addToStatements("result.append(\",\")");
         } else {
             forConcreteClassifiers.getBody().addToStatements("result.append(\"],\")");
         }
         forConcreteClassifiers.getBody().addToStatements("result.append(\" \\\"meta\\\" : {\")");
         forConcreteClassifiers.getBody().addToStatements("result.append(\"\\\"qualifiedName\\\": \\\"" + pWrap.getQualifiedName() + "AC\\\"\")");
         forConcreteClassifiers.getBody().addToStatements("result.append(\", \\\"to\\\": \")");
 
         OJIfStatement ifClassInstanceOf = new OJIfStatement();
         forConcreteClassifiers.getBody().addToStatements(ifClassInstanceOf);
         Set<Classifier> concreteImplementations = TumlClassOperations.getConcreteImplementations(pWrap.getAssociationClass());
         boolean first = true;
         for (Classifier concreteImplementation : concreteImplementations) {
             OJBlock ojIfBlock;
             if (first) {
                 first = false;
                 ifClassInstanceOf.setCondition("baseClass.equals(" + TumlClassOperations.getPathName(concreteImplementation).getLast() + ".class)");
                 ojIfBlock = ifClassInstanceOf.getThenPart();
             } else {
                 ojIfBlock = ifClassInstanceOf.addToElseIfCondition("baseClass.equals(" + TumlClassOperations.getPathName(concreteImplementation).getLast() + ".class)", "");
             }
             ojIfBlock.addToStatements("result.append(" + TumlClassOperations.propertyEnumName(concreteImplementation) + ".asJson())");
             ojIfBlock.addToStatements("result.append(\", \\\"from\\\": \")");
             Classifier owningType = (Classifier) pWrap.getOwningType();
             ojIfBlock.addToStatements("result.append(" + TumlClassOperations.propertyEnumName(owningType) + ".asJson())");
             ojIfBlock.addToStatements("result.append(\"}\")");
 
         }
         OJIfStatement ifLast = new OJIfStatement("count++ == resultMap.size()");
         ifLast.addToThenPart("result.append(\"}\")");
         ifLast.addToElsePart("result.append(\"},\")");
         forConcreteClassifiers.getBody().addToStatements(ifLast);
 
         ifClassInstanceOf.addToElsePart("throw new IllegalStateException(\"Unknown type \" + baseClass.getName())");
         jsonResultBlock.addToStatements("result.append(\"]\")");
         jsonResultBlock.addToStatements("return new " + TumlRestletGenerationUtil.JsonRepresentation.getLast() + "(result.toString())");
 
         ojTryStatement.setCatchParam(new OJParameter("e", new OJPathName("java.lang.Exception")));
         ojTryStatement.getCatchPart().addToStatements("GraphDb.getDb().rollback()");
 
         ojTryStatement.getCatchPart().addToStatements("throw " + TumlRestletGenerationUtil.UmlgExceptionUtilFactory.getLast() + ".getTumlExceptionUtil().handle(e)");
         annotatedClass.addToImports(TumlRestletGenerationUtil.UmlgExceptionUtilFactory);
 
         post.getBody().addToStatements(ojTryStatement);
 
         annotatedClass.addToImports(parentPathName);
 
         annotatedClass.addToImports(TinkerGenerationUtil.graphDbPathName);
         annotatedClass.addToImports(TumlRestletGenerationUtil.JsonRepresentation);
         annotatedClass.addToOperations(post);
     }
 
     private void addPutResource(PropertyWrapper pWrap, OJAnnotatedClass annotatedClass, OJPathName parentPathName) {
         OJAnnotatedOperation put = new OJAnnotatedOperation("put");
         put.setVisibility(OJVisibilityKind.PRIVATE);
         put.addToParameters(new OJParameter("resultMap", new OJPathName("java.util.Map").addToGenerics(new OJPathName("Class").addToGenerics("? extends " + pWrap.getAssociationClassPathName().getLast())).addToGenerics("java.lang.StringBuilder")));
         put.addToParameters(new OJParameter("propertyMap", new OJPathName("java.util.Map").addToGenerics("String").addToGenerics("Object")));
         annotatedClass.addToOperations(put);
 
         OJBlock firstBlock = new OJBlock();
         firstBlock.addToStatements("Object id = propertyMap.get(\"id\")");
         firstBlock.addToStatements(pWrap.getAssociationClassPathName().getLast() + " childResource = GraphDb.getDb().instantiateClassifier(id)");
         annotatedClass.addToImports(pWrap.javaBaseTypePath());
         firstBlock.addToStatements("childResource.fromJson(propertyMap)");
         put.getBody().addToStatements(firstBlock);
 
         OJBlock secondBlock = new OJBlock();
 
         OJField baseTumlClass = new OJField("baseTumlClass", new OJPathName("Class").addToGenerics("? extends " + pWrap.getAssociationClassPathName().getLast()));
         baseTumlClass.setInitExp("childResource.getClass()");
         secondBlock.addToLocals(baseTumlClass);
 
         OJField sb = new OJField("sb", "StringBuilder");
         secondBlock.addToLocals(sb);
 
         OJIfStatement ifSbExist = new OJIfStatement("!resultMap.containsKey(baseTumlClass)");
         ifSbExist.addToThenPart("sb = new StringBuilder()");
         ifSbExist.addToThenPart("resultMap.put(baseTumlClass, sb)");
         ifSbExist.addToElsePart("sb = resultMap.get(baseTumlClass)");
         ifSbExist.addToElsePart("sb.append(\",\")");
         secondBlock.addToStatements(ifSbExist);
 
         secondBlock.addToStatements("sb.append(childResource.toJsonWithoutCompositeParent())");
         put.getBody().addToStatements(secondBlock);
 
     }
 
     private void addPostResource(PropertyWrapper pWrap, OJAnnotatedClass annotatedClass, OJPathName parentPathName) {
         addPostResource(pWrap, annotatedClass, parentPathName, false);
     }
 
     private void addPostResource(PropertyWrapper pWrap, OJAnnotatedClass annotatedClass, OJPathName parentPathName, boolean asAssociationClass) {
         OJAnnotatedOperation add = new OJAnnotatedOperation("add");
         add.setVisibility(OJVisibilityKind.PRIVATE);
 
         add.addToParameters(new OJParameter("resultMap", new OJPathName("java.util.Map").addToGenerics(new OJPathName("Class").addToGenerics("? extends " + pWrap.javaBaseTypePath().getLast())).addToGenerics("java.lang.StringBuilder")));
         add.addToParameters(new OJParameter("parentResource", parentPathName));
         add.addToParameters(new OJParameter("propertyMap", new OJPathName("java.util.Map").addToGenerics("String").addToGenerics("Object")));
         annotatedClass.addToOperations(add);
 
         OJField qualifiedName = new OJField("qualifiedName", "String");
         qualifiedName.setInitExp("(String)propertyMap.get(\"qualifiedName\")");
         add.getBody().addToLocals(qualifiedName);
 
         OJField baseTumlClass = new OJField("baseTumlClass", new OJPathName("Class").addToGenerics(pWrap.javaBaseTypePath()));
         baseTumlClass.setInitExp(TinkerGenerationUtil.UmlgSchemaFactory.getLast() + ".getUmlgSchemaMap().get(qualifiedName)");
         annotatedClass.addToImports(TinkerGenerationUtil.UmlgSchemaFactory);
         add.getBody().addToLocals(baseTumlClass);
 
         OJField sb = new OJField("sb", "StringBuilder");
         add.getBody().addToLocals(sb);
 
         OJIfStatement ifSbExist = new OJIfStatement("!resultMap.containsKey(baseTumlClass)");
         ifSbExist.addToThenPart("sb = new StringBuilder()");
         ifSbExist.addToThenPart("resultMap.put(baseTumlClass, sb)");
         ifSbExist.addToElsePart("sb = resultMap.get(baseTumlClass)");
         ifSbExist.addToElsePart("sb.append(\",\")");
         add.getBody().addToStatements(ifSbExist);
 
         OJTryStatement tryInstantiate = new OJTryStatement();
         add.getBody().addToStatements(tryInstantiate);
 
         if (pWrap.isComposite()) {
             PropertyWrapper otherEndPWrap = new PropertyWrapper(pWrap.getOtherEnd());
             OJField constructor = new OJField("constructor", new OJPathName("java.lang.reflect.Constructor").addToGenerics(pWrap.javaBaseTypePath()));
             constructor.setInitExp("baseTumlClass.getConstructor(" + otherEndPWrap.javaBaseTypePath().getLast() + ".class)");
             tryInstantiate.getTryPart().addToLocals(constructor);
             tryInstantiate.getTryPart().addToStatements(pWrap.javaBaseTypePath().getLast() + " childResource = constructor.newInstance(parentResource)");
             tryInstantiate.getTryPart().addToStatements("childResource.fromJson(propertyMap)");
         } else {
             tryInstantiate.getTryPart().addToStatements("Object id = propertyMap.get(\"id\")");
             tryInstantiate.getTryPart().addToStatements(pWrap.javaBaseTypePath().getLast() + " childResource = GraphDb.getDb().instantiateClassifier(id)");
             if (!pWrap.isMemberOfAssociationClass() || asAssociationClass) {
                 tryInstantiate.getTryPart().addToStatements("parentResource." + pWrap.adder() + "(childResource)");
             } else {
                 tryInstantiate.getTryPart().addToStatements("parentResource." + pWrap.adder() + "(childResource, null)");
             }
         }
         tryInstantiate.getTryPart().addToStatements("String jsonResult = childResource.toJsonWithoutCompositeParent(true)");
         annotatedClass.addToImports(pWrap.javaBaseTypePath());
         if (pWrap.isOrdered()) {
             //TODO
         } else {
             //TODO
         }
         tryInstantiate.getTryPart().addToStatements("sb.append(jsonResult)");
         tryInstantiate.setCatchParam(new OJParameter("e", "Exception"));
         tryInstantiate.getCatchPart().addToStatements("throw new RuntimeException(e)");
     }
 
     private void buildToJson(PropertyWrapper pWrap, OJAnnotatedClass annotatedClass, OJBlock block, boolean asAssociationClass) {
         Set<Classifier> concreteImplementations;
         Set<Classifier> concreteImplementationsFrom;
         if (!asAssociationClass) {
             concreteImplementations = TumlClassOperations.getConcreteImplementations(pWrap.getAssociationClass());
             concreteImplementationsFrom = TumlClassOperations.getConcreteImplementations((Classifier) pWrap.getOwningType());
         } else {
             concreteImplementations = TumlClassOperations.getConcreteImplementations((Classifier)pWrap.getType());
             concreteImplementationsFrom = TumlClassOperations.getConcreteImplementations(pWrap.getAssociationClass());
         }
         if (!concreteImplementationsFrom.isEmpty()) {
             annotatedClass.addToImports(TinkerGenerationUtil.ToJsonUtil);
             block.addToStatements("StringBuilder json = new StringBuilder()");
             block.addToStatements("json.append(\"[\")");
 
             int count = 1;
             // For meta data, put where one is navigating to first, then where on is
             // navigating from
             // This is consistent with navigating to a entity with a vertex where
             // there is no navigating from.
             // i.e. the first meta data in the array is the entity navigating to.
             for (Classifier concreteClassifierTo : concreteImplementations) {
                 annotatedClass.addToImports(TumlClassOperations.getPathName(concreteClassifierTo));
                 if (pWrap.isOne() || asAssociationClass) {
                     block.addToStatements("json.append(\"{\\\"data\\\": \")");
                 } else {
                     block.addToStatements("json.append(\"{\\\"data\\\": [\")");
                 }
                 if (pWrap.isOne() || asAssociationClass) {
 
                     if (asAssociationClass) {
                         OJIfStatement ifOneInstanceOf = new OJIfStatement("parentResource." + pWrap.getter() + "() instanceof "
                                 + TumlClassOperations.getPathName(concreteClassifierTo).getLast());
                         ifOneInstanceOf.addToThenPart("json.append(" + TinkerGenerationUtil.ToJsonUtil.getLast() + ".toJsonWithoutCompositeParent(parentResource." + pWrap.getter() + "()))");
                         ifOneInstanceOf.addToElsePart("json.append(\"null\")");
                         block.addToStatements(ifOneInstanceOf);
                     } else {
                         OJIfStatement ifOneInstanceOf = new OJIfStatement("parentResource." + pWrap.associationClassGetter() + "() instanceof "
                                 + TumlClassOperations.getPathName(concreteClassifierTo).getLast());
                         ifOneInstanceOf.addToThenPart("json.append(" + TinkerGenerationUtil.ToJsonUtil.getLast() + ".toJsonWithoutCompositeParent(parentResource." + pWrap.associationClassGetter() + "()))");
                         ifOneInstanceOf.addToElsePart("json.append(\"null\")");
                         block.addToStatements(ifOneInstanceOf);
                     }
                 } else {
                     block.addToStatements("json.append(" + TinkerGenerationUtil.ToJsonUtil.getLast() + ".toJsonWithoutCompositeParent(parentResource.get" + pWrap.getAssociationClassPathName().getLast() + "().select(new "
                             + TinkerGenerationUtil.BooleanExpressionEvaluator.getCopy().addToGenerics(pWrap.getAssociationClassPathName()).getLast()
                             + "() {\n			@Override\n			public Boolean evaluate(" + pWrap.getAssociationClassPathName().getLast()
                             + " e) {\n				return e instanceof " + TumlClassOperations.getPathName(concreteClassifierTo).getLast() + ";\n			}\n		})))");
                     annotatedClass.addToImports(TinkerGenerationUtil.BooleanExpressionEvaluator);
                 }
                 annotatedClass.addToImports(TumlClassOperations.getPathName(pWrap.getType()));
                 if (pWrap.isOne() || asAssociationClass) {
                     block.addToStatements("json.append(\",\")");
                 } else {
                     block.addToStatements("json.append(\"],\")");
                 }
 
                 block.addToStatements("json.append(\" \\\"meta\\\" : {\")");
                 // The execute ocl query resource is only required if the below
                 // visitor is available
                 if (RestletVisitors.containsVisitorForClass(QueryExecuteResourceBuilder.class)
                         && (pWrap.getType().getQualifiedName().equals(TumlRestletGenerationUtil.instanceQueryQualifiedName) || pWrap.getType().getQualifiedName().equals(TumlRestletGenerationUtil.classQueryQualifiedName))) {
                     block.addToStatements("json.append(\"\\\"oclExecuteUri\\\": \\\"/" + pWrap.getModel().getName() + "/{contextId}/oclExecuteQuery\\\", \")");
                 }
                 if (!asAssociationClass) {
                     block.addToStatements("json.append(\"\\\"qualifiedName\\\": \\\"" + pWrap.getQualifiedName() + "AC\\\"\")");
                 } else {
                     block.addToStatements("json.append(\"\\\"qualifiedName\\\": \\\"" + pWrap.getQualifiedName() + "\\\"\")");
                 }
                 block.addToStatements("json.append(\", \\\"to\\\": \")");
                 int countFrom = 1;
                 OJIfStatement ifStatementFrom = new OJIfStatement();
                 for (Classifier concreteClassifierFrom : concreteImplementationsFrom) {
                     OJBlock conditionBlockFrom = new OJBlock();
                     annotatedClass.addToImports(TumlClassOperations.getPathName(concreteClassifierFrom));
                     String condition = "parentResource instanceof " + TumlClassOperations.getPathName(concreteClassifierFrom).getLast();
                     if (countFrom == 1) {
                         ifStatementFrom.setCondition(condition);
                         ifStatementFrom.setThenPart(conditionBlockFrom);
                     } else if (countFrom == concreteImplementationsFrom.size()) {
                         ifStatementFrom.setElsePart(conditionBlockFrom);
                     } else {
                         conditionBlockFrom = ifStatementFrom.addToElseIfCondition(condition, "");
                     }
                     conditionBlockFrom.addToStatements("json.append(" + TumlClassOperations.propertyEnumName(concreteClassifierTo) + ".asJson())");
                     conditionBlockFrom.addToStatements("json.append(\", \\\"from\\\": \")");
                     conditionBlockFrom.addToStatements("json.append(" + TumlClassOperations.propertyEnumName(concreteClassifierFrom) + ".asJson())");
                     annotatedClass.addToImports(TumlClassOperations.getPathName(concreteClassifierFrom).append(
                             TumlClassOperations.propertyEnumName(concreteClassifierFrom)));
                     countFrom++;
                 }
                 block.addToStatements(ifStatementFrom);
 
                 annotatedClass.addToImports(TumlClassOperations.getPathName(pWrap.getOwningType()).append(
                         TumlClassOperations.propertyEnumName(pWrap.getOwningType())));
                 annotatedClass.addToImports(TumlClassOperations.getPathName(concreteClassifierTo)
                         .append(TumlClassOperations.propertyEnumName(concreteClassifierTo)));
                 block.addToStatements("json.append(\"}\")");
                 if (concreteImplementations.size() != 1 && count != concreteImplementations.size()) {
                     block.addToStatements("json.append(\"}, \")");
                 }
                 count++;
             }
             block.addToStatements("json.append(\"}]\")");
             block.addToStatements("return new " + TumlRestletGenerationUtil.JsonRepresentation.getLast() + "(json.toString())");
         } else {
             //TODO not thought through
             block.addToStatements("return null");
         }
     }
 
     private void addServerResourceToRouterEnum(PropertyWrapper pWrap, OJAnnotatedClass annotatedClass) {
         OJEnum routerEnum = (OJEnum) this.workspace.findOJClass("restlet.RestletRouterEnum");
 
         OJEnumLiteral ojLiteral = new OJEnumLiteral(TumlClassOperations.getPathName(pWrap.getOwningType()).getLast() + "_" + pWrap.getAssociationClassFakePropertyName());
 
         OJField uri = new OJField();
         uri.setType(new OJPathName("String"));
         uri.setInitExp("\"/" + TumlClassOperations.getPathName(pWrap.getOwningType()).getLast().toLowerCase() + "s/{"
                 + TumlClassOperations.getPathName(pWrap.getOwningType()).getLast().toLowerCase() + "Id}/" + pWrap.getAssociationClassFakePropertyName() + "\"");
         ojLiteral.addToAttributeValues(uri);
 
         OJField serverResourceClassField = new OJField();
         serverResourceClassField.setType(new OJPathName("java.lang.Class"));
         serverResourceClassField.setInitExp(annotatedClass.getName() + ".class");
         ojLiteral.addToAttributeValues(serverResourceClassField);
         routerEnum.addToImports(annotatedClass.getPathName());
         routerEnum.addToImports(TumlRestletGenerationUtil.ServerResource);
 
         routerEnum.addToLiterals(ojLiteral);
 
         OJAnnotatedOperation attachAll = routerEnum.findOperation("attachAll", TumlRestletGenerationUtil.Router);
         attachAll.getBody().addToStatements(routerEnum.getName() + "." + ojLiteral.getName() + ".attach(router)");
     }
 
     private void addAssociationClassServerResourceToRouterEnum(PropertyWrapper pWrap, OJAnnotatedClass annotatedClass) {
         OJEnum routerEnum = (OJEnum) this.workspace.findOJClass("restlet.RestletRouterEnum");
 
         OJEnumLiteral ojLiteral = new OJEnumLiteral(pWrap.getAssociationClassPathName().getLast() + "_" + pWrap.fieldname());
 
         OJField uri = new OJField();
         uri.setType(new OJPathName("String"));
         uri.setInitExp("\"/" + pWrap.getAssociationClassPathName().getLast().toLowerCase() + "s/{"
                 + pWrap.getAssociationClassPathName().getLast().toLowerCase() + "Id}/" + pWrap.fieldname() + "\"");
         ojLiteral.addToAttributeValues(uri);
 
         OJField serverResourceClassField = new OJField();
         serverResourceClassField.setType(new OJPathName("java.lang.Class"));
         serverResourceClassField.setInitExp(annotatedClass.getName() + ".class");
         ojLiteral.addToAttributeValues(serverResourceClassField);
         routerEnum.addToImports(annotatedClass.getPathName());
         routerEnum.addToImports(TumlRestletGenerationUtil.ServerResource);
 
         routerEnum.addToLiterals(ojLiteral);
 
         OJAnnotatedOperation attachAll = routerEnum.findOperation("attachAll", TumlRestletGenerationUtil.Router);
         attachAll.getBody().addToStatements(routerEnum.getName() + "." + ojLiteral.getName() + ".attach(router)");
     }
 
     private void addCompositeParentIdField(PropertyWrapper pWrap, OJAnnotatedClass annotatedClass) {
         addCompositeParentIdField(pWrap, annotatedClass, false);
     }
 
     private void addCompositeParentIdField(PropertyWrapper pWrap, OJAnnotatedClass annotatedClass, boolean asAssociationClass) {
         OJField compositeParentFieldId;
         if (!asAssociationClass) {
             compositeParentFieldId = new OJField(TumlClassOperations.getPathName(pWrap.getOtherEnd().getType()).getLast().toLowerCase() + "Id",
                 new OJPathName("Object"));
         } else {
             compositeParentFieldId = new OJField(pWrap.getAssociationClassPathName().getLast().toLowerCase() + "Id",
                     new OJPathName("Object"));
 
         }
         compositeParentFieldId.setVisibility(OJVisibilityKind.PRIVATE);
         annotatedClass.addToFields(compositeParentFieldId);
     }
 
 }
