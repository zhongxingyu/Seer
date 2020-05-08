 package org.umlg.restlet.visitor.clazz;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.uml2.uml.*;
 import org.eclipse.uml2.uml.Class;
 import org.umlg.framework.VisitSubclasses;
 import org.umlg.framework.Visitor;
 import org.umlg.generation.Workspace;
 import org.umlg.java.metamodel.*;
 import org.umlg.java.metamodel.annotation.*;
 import org.umlg.java.metamodel.generated.OJVisibilityKindGEN;
 import org.umlg.javageneration.util.Namer;
 import org.umlg.javageneration.util.PropertyWrapper;
 import org.umlg.javageneration.util.TinkerGenerationUtil;
 import org.umlg.javageneration.util.TumlClassOperations;
 import org.umlg.restlet.util.TumlRestletGenerationUtil;
 
 import java.util.SortedSet;
 
 public class EntityServerResourceBuilder extends BaseServerResourceBuilder implements Visitor<Classifier> {
 
     public EntityServerResourceBuilder(Workspace workspace, String sourceDir) {
         super(workspace, sourceDir);
     }
 
     @Override
     @VisitSubclasses({Interface.class, Class.class, AssociationClass.class})
     public void visitBefore(Classifier classifier) {
         if (classifier instanceof Interface || classifier instanceof org.eclipse.uml2.uml.Class) {
             OJPackage ojPackage = new OJPackage(Namer.name(classifier.getNearestPackage()) + ".restlet");
             OJAnnotatedClass annotatedClass = new OJAnnotatedClass(getServerResourceImplName(classifier));
             annotatedClass.setSuperclass(TumlRestletGenerationUtil.ServerResource);
             annotatedClass.setMyPackage(ojPackage);
             annotatedClass.setVisibility(TumlClassOperations.getVisibility(classifier.getVisibility()));
             addToSource(annotatedClass);
             addDefaultConstructor(annotatedClass);
             //Interfaces and abstract classes can not have a get put or delete
             if (!(classifier instanceof Interface) && !classifier.isAbstract()) {
                 addPrivateIdVariable(classifier, annotatedClass);
                 addGetRepresentation((Class)classifier, annotatedClass);
                 addPutRepresentation((Class)classifier, annotatedClass);
                 addDeleteRepresentation((Class)classifier, annotatedClass);
             }
             //OPTIONS apply to abstract classes and interfaces
             addOptionsRepresentation(classifier, annotatedClass);
             addToRouterEnum(classifier, annotatedClass);
         }
     }
 
     @Override
     public void visitAfter(Classifier classifier) {
     }
 
     private void addDeleteRepresentation(Class clazz, OJAnnotatedClass annotatedClass) {
         OJAnnotatedOperation delete = new OJAnnotatedOperation("delete", TumlRestletGenerationUtil.Representation);
         delete.addToThrows(TumlRestletGenerationUtil.ResourceException);
         annotatedClass.addToImports(TumlRestletGenerationUtil.ResourceException);
         TinkerGenerationUtil.addOverrideAnnotation(delete);
 
         delete.getBody().addToStatements(
                "this." + getIdFieldName(clazz) + "= (String)getRequestAttributes().get(\"" + getIdFieldName(clazz) + "\")");
         delete.getBody().addToStatements(
                 TumlClassOperations.className(clazz) + " c = new " + TumlClassOperations.className(clazz) + "(GraphDb.getDb().getVertex(this."
                         + getIdFieldName(clazz) + "))");
         annotatedClass.addToImports(TumlClassOperations.getPathName(clazz));
 
         OJPathName parentPathName = TumlClassOperations.getOtherEndToCompositePathName(clazz);
         if (parentPathName != null) {
             annotatedClass.addToImports(parentPathName);
             Property parentProperty = TumlClassOperations.getOtherEndToComposite(clazz);
             PropertyWrapper parentWrap = new PropertyWrapper(parentProperty);
             delete.getBody().addToStatements(parentPathName.getLast() + " " + parentWrap.fieldname() + " = c." + parentWrap.getter() + "()");
         }
 
         OJTryStatement ojTry = new OJTryStatement();
         ojTry.getTryPart().addToStatements("c.delete()");
         ojTry.getTryPart().addToStatements("GraphDb.getDb().commit()");
 
         ojTry.setCatchParam(new OJParameter("e", new OJPathName("java.lang.Exception")));
 
         ojTry.getCatchPart().addToStatements("GraphDb.getDb().rollback()");
         ojTry.getCatchPart().addToStatements("throw " + TumlRestletGenerationUtil.UmlgExceptionUtilFactory.getLast() + ".getTumlExceptionUtil().handle(e)");
         annotatedClass.addToImports(TumlRestletGenerationUtil.UmlgExceptionUtilFactory);
         delete.getBody().addToStatements(ojTry);
 
         if (parentPathName != null) {
             Property parentProperty = TumlClassOperations.getOtherEndToComposite(clazz);
             PropertyWrapper parentWrap = new PropertyWrapper(parentProperty);
             addGetParentRepresentation((Class) parentProperty.getType(), annotatedClass);
             delete.getBody().addToStatements("return getParent(" + parentWrap.fieldname() + ")");
         } else {
             delete.getBody().addToStatements("return new JsonRepresentation(\"\")");
         }
 
         annotatedClass.addToImports(TinkerGenerationUtil.graphDbPathName);
         annotatedClass.addToImports(TumlRestletGenerationUtil.JsonRepresentation);
         annotatedClass.addToOperations(delete);
     }
 
     private void addPutRepresentation(Class clazz, OJAnnotatedClass annotatedClass) {
         OJAnnotatedOperation put = new OJAnnotatedOperation("put", TumlRestletGenerationUtil.Representation);
         put.addParam("entity", TumlRestletGenerationUtil.Representation);
         put.addToThrows(TumlRestletGenerationUtil.ResourceException);
         TinkerGenerationUtil.addOverrideAnnotation(put);
 
         annotatedClass.addToImports(TumlRestletGenerationUtil.ResourceException);
 
         put.getBody().addToStatements(
                "this." + getIdFieldName(clazz) + "= (String)getRequestAttributes().get(\"" + getIdFieldName(clazz) + "\")");
         put.getBody().addToStatements(
                 TumlClassOperations.className(clazz) + " c = new " + TumlClassOperations.className(clazz) + "(GraphDb.getDb().getVertex(this."
                         + getIdFieldName(clazz) + "))");
         annotatedClass.addToImports(TumlClassOperations.getPathName(clazz));
         OJTryStatement ojTry = new OJTryStatement();
 
         OJAnnotatedField entityText = new OJAnnotatedField("entityText", "String");
         entityText.setInitExp("entity.getText()");
         ojTry.getTryPart().addToLocals(entityText);
 
         ojTry.getTryPart().addToStatements("c.fromJson(" + entityText.getName() + ")");
 
         ojTry.getTryPart().addToStatements("GraphDb.getDb().commit()");
 
         //Now build the json to return, including meta data
         ojTry.getTryPart().addToStatements("StringBuilder json = new StringBuilder()");
         ojTry.getTryPart().addToStatements("json.append(\"[{\\\"data\\\": \")");
         ojTry.getTryPart().addToStatements("json.append(" + "c.toJsonWithoutCompositeParent())");
         ojTry.getTryPart().addToStatements("meta", "json.append(\", \\\"meta\\\" : {\")");
         ojTry.getTryPart().addToStatements("json.append(\"\\\"qualifiedName\\\": \\\"" + clazz.getQualifiedName() + "\\\"\")");
         ojTry.getTryPart().addToStatements("json.append(\", \\\"to\\\": \")");
         ojTry.getTryPart().addToStatements("json.append(" + TumlClassOperations.propertyEnumName(clazz) + ".asJson())");
         annotatedClass.addToImports(TumlClassOperations.getPathName(clazz).append(TumlClassOperations.propertyEnumName(clazz)));
         ojTry.getTryPart().addToStatements("json.append(\"}}]\")");
         ojTry.getTryPart().addToStatements("return new " + TumlRestletGenerationUtil.JsonRepresentation.getLast() + "(json.toString())");
 
 
         ojTry.setCatchParam(new OJParameter("e", new OJPathName("java.lang.Exception")));
         ojTry.getCatchPart().addToStatements("GraphDb.getDb().rollback()");
         OJIfStatement ifRuntime = new OJIfStatement("e instanceof RuntimeException");
         ifRuntime.addToThenPart("throw (RuntimeException)e");
         ojTry.getCatchPart().addToStatements(ifRuntime);
         ojTry.getCatchPart().addToStatements("throw new RuntimeException(e)");
         put.getBody().addToStatements(ojTry);
         ojTry.getFinallyPart().addToStatements("GraphDb.getDb().rollback()");
 
         annotatedClass.addToImports(TinkerGenerationUtil.graphDbPathName);
         annotatedClass.addToImports(TumlRestletGenerationUtil.JsonRepresentation);
         annotatedClass.addToOperations(put);
     }
 
     private void addGetRepresentation(Class clazz, OJAnnotatedClass annotatedClass) {
         OJAnnotatedOperation get = new OJAnnotatedOperation("get", TumlRestletGenerationUtil.Representation);
         get.addToThrows(TumlRestletGenerationUtil.ResourceException);
         annotatedClass.addToImports(TumlRestletGenerationUtil.ResourceException);
         TinkerGenerationUtil.addOverrideAnnotation(get);
 
         OJTryStatement tryStatement = new OJTryStatement();
         tryStatement.getTryPart().addToStatements("StringBuilder json = new StringBuilder()");
         tryStatement.getTryPart().addToStatements(
                "this." + getIdFieldName(clazz) + "= (String)getRequestAttributes().get(\"" + getIdFieldName(clazz) + "\")");
         tryStatement.getTryPart().addToStatements(
                 TumlClassOperations.className(clazz) + " c = new " + TumlClassOperations.className(clazz) + "(GraphDb.getDb().getVertex(this."
                         + getIdFieldName(clazz) + "))");
         annotatedClass.addToImports(TumlClassOperations.getPathName(clazz));
         tryStatement.getTryPart().addToStatements("json.append(\"[{\\\"data\\\": \")");
         tryStatement.getTryPart().addToStatements("json.append(" + "c.toJsonWithoutCompositeParent())");
 
         tryStatement.getTryPart().addToStatements("meta", "json.append(\", \\\"meta\\\" : {\")");
 
         tryStatement.getTryPart().addToStatements("json.append(\"\\\"qualifiedName\\\": \\\"" + clazz.getQualifiedName() + "\\\"\")");
         tryStatement.getTryPart().addToStatements("json.append(\"}}]\")");
         tryStatement.getTryPart().addToStatements("return new " + TumlRestletGenerationUtil.JsonRepresentation.getLast() + "(json.toString())");
 
         get.getBody().addToStatements(tryStatement);
         tryStatement.setCatchPart(null);
         tryStatement.getFinallyPart().addToStatements(TinkerGenerationUtil.graphDbAccess + ".rollback()");
 
         annotatedClass.addToImports(TinkerGenerationUtil.graphDbPathName);
         annotatedClass.addToImports(TumlRestletGenerationUtil.JsonRepresentation);
         annotatedClass.addToOperations(get);
     }
 
     private void addOptionsRepresentation(Classifier clazz, OJAnnotatedClass annotatedClass) {
 
         OJAnnotatedOperation options = new OJAnnotatedOperation("options", TumlRestletGenerationUtil.Representation);
         options.addToThrows(TumlRestletGenerationUtil.ResourceException);
         annotatedClass.addToImports(TumlRestletGenerationUtil.ResourceException);
         TinkerGenerationUtil.addOverrideAnnotation(options);
 
         OJTryStatement tryStatement = new OJTryStatement();
         SortedSet<Classifier> sortedConcreteImplementations = TumlClassOperations.getConcreteImplementations(clazz);
         OJBlock block = tryStatement.getTryPart();
         block.addToStatements("StringBuilder json = new StringBuilder()");
         block.addToStatements("json.append(\"[\")");
 
         int count = 1;
         for (Classifier classifier : sortedConcreteImplementations) {
             block.addToStatements("json.append(\"{\\\"data\\\": null,\")");
             block.addToStatements("json.append(\" \\\"meta\\\" : {\")");
 
             block.addToStatements("json.append(\"\\\"qualifiedName\\\": \\\"" + clazz.getQualifiedName() + "\\\"\")");
             block.addToStatements("json.append(\", \\\"to\\\": \")");
             block.addToStatements("json.append(" + TumlClassOperations.propertyEnumName(classifier) + ".asJson())");
 //            block.addToStatements("json.append(\", \\\"from\\\": \")");
 //            block.addToStatements("json.append(" + TumlClassOperations.propertyEnumName(clazz) + ".asJson())");
             block.addToStatements("json.append(\"} \")");
             annotatedClass.addToImports(TumlClassOperations.getPathName(classifier).append(TumlClassOperations.propertyEnumName(classifier)));
             if (count++ != sortedConcreteImplementations.size()) {
                 block.addToStatements("json.append(\"}, \")");
             }
         }
         annotatedClass.addToImports(TumlClassOperations.getPathName(clazz).append(TumlClassOperations.propertyEnumName(clazz)));
         block.addToStatements("json.append(\"}]\")");
 
         annotatedClass.addToImports(TumlClassOperations.getPathName(clazz));
         tryStatement.getTryPart().addToStatements("return new " + TumlRestletGenerationUtil.JsonRepresentation.getLast() + "(json.toString())");
 
         options.getBody().addToStatements(tryStatement);
         tryStatement.setCatchPart(null);
         tryStatement.getFinallyPart().addToStatements(TinkerGenerationUtil.graphDbAccess + ".rollback()");
 
         annotatedClass.addToImports(TinkerGenerationUtil.graphDbPathName);
         annotatedClass.addToImports(TumlRestletGenerationUtil.JsonRepresentation);
         annotatedClass.addToOperations(options);
     }
 
     private void addGetParentRepresentation(Class clazz, OJAnnotatedClass annotatedClass) {
         OJAnnotatedOperation getParent = new OJAnnotatedOperation("getParent", TumlRestletGenerationUtil.Representation);
         getParent.addParam("parent", TumlClassOperations.getPathName(clazz));
         getParent.setVisibility(OJVisibilityKindGEN.PRIVATE);
         getParent.addToThrows(TumlRestletGenerationUtil.ResourceException);
         annotatedClass.addToImports(TumlRestletGenerationUtil.ResourceException);
 
         getParent.getBody().addToStatements("StringBuilder json = new StringBuilder()");
         annotatedClass.addToImports(TumlClassOperations.getPathName(clazz));
         getParent.getBody().addToStatements("json.append(\"[{\\\"data\\\": \")");
         getParent.getBody().addToStatements("json.append(" + "parent.toJsonWithoutCompositeParent())");
 
         getParent.getBody().addToStatements("meta", "json.append(\", \\\"meta\\\" : {\")");
 
         getParent.getBody().addToStatements("json.append(\"\\\"qualifiedName\\\": \\\"" + clazz.getQualifiedName() + "\\\"\")");
         getParent.getBody().addToStatements("json.append(\", \\\"to\\\": \")");
         getParent.getBody().addToStatements("json.append(" + TumlClassOperations.propertyEnumName(clazz) + ".asJson())");
         annotatedClass.addToImports(TumlClassOperations.getPathName(clazz).append(TumlClassOperations.propertyEnumName(clazz)));
         getParent.getBody().addToStatements("json.append(\"}}]\")");
         getParent.getBody().addToStatements("return new " + TumlRestletGenerationUtil.JsonRepresentation.getLast() + "(json.toString())");
 
         annotatedClass.addToImports(TinkerGenerationUtil.graphDbPathName);
         annotatedClass.addToImports(TumlRestletGenerationUtil.JsonRepresentation);
         annotatedClass.addToOperations(getParent);
     }
 
     private void addToRouterEnum(Classifier clazz, OJAnnotatedClass annotatedClass) {
         OJEnum routerEnum = (OJEnum) this.workspace.findOJClass("restlet.RestletRouterEnum");
         OJEnumLiteral ojLiteral = new OJEnumLiteral(TumlClassOperations.className(clazz).toUpperCase());
 
         OJField uri = new OJField();
         uri.setType(new OJPathName("String"));
         uri.setInitExp("\"/" + TumlClassOperations.className(clazz).toLowerCase() + "s/{" + TumlClassOperations.className(clazz).toLowerCase() + "Id}\"");
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
 
         //Add meta data property url
         OJEnumLiteral ojLiteralMeta = new OJEnumLiteral(TumlClassOperations.className(clazz).toUpperCase() + "_META");
         OJField uriMeta = new OJField();
         uriMeta.setType(new OJPathName("String"));
         uriMeta.setInitExp("\"/" + StringUtils.uncapitalize(TumlClassOperations.className(clazz)) + "MetaData\"");
         ojLiteralMeta.addToAttributeValues(uriMeta);
         ojLiteralMeta.addToAttributeValues(serverResourceClassField);
 
         routerEnum.addToImports(annotatedClass.getPathName());
         routerEnum.addToImports(TumlRestletGenerationUtil.ServerResource);
         routerEnum.addToLiterals(ojLiteralMeta);
         attachAll.getBody().addToStatements(routerEnum.getName() + "." + ojLiteralMeta.getName() + ".attach(router)");
     }
 
 }
