 package org.umlg.restlet.visitor.clazz;
 
 import org.eclipse.uml2.uml.AssociationClass;
 import org.eclipse.uml2.uml.Class;
 import org.umlg.framework.VisitSubclasses;
 import org.umlg.framework.Visitor;
 import org.umlg.generation.Workspace;
 import org.umlg.java.metamodel.*;
 import org.umlg.java.metamodel.annotation.*;
 import org.umlg.javageneration.util.Namer;
 import org.umlg.javageneration.util.TinkerGenerationUtil;
 import org.umlg.javageneration.util.TumlClassOperations;
 import org.umlg.restlet.util.TumlRestletGenerationUtil;
 
 public class EntityForLookupServerResourceBuilder extends BaseServerResourceBuilder implements Visitor<Class> {
 
     public EntityForLookupServerResourceBuilder(Workspace workspace, String sourceDir) {
         super(workspace, sourceDir);
     }
 
     @Override
     @VisitSubclasses({Class.class, AssociationClass.class})
     public void visitBefore(Class clazz) {
         if (!clazz.isAbstract()) {
 //            OJAnnotatedInterface annotatedInf = new OJAnnotatedInterface(getLookupServerResourceName(clazz));
             OJPackage ojPackage = new OJPackage(Namer.name(clazz.getNearestPackage()) + ".restlet");
 //            annotatedInf.setMyPackage(ojPackage);
 //            addToSource(annotatedInf);
             OJAnnotatedClass annotatedClass = new OJAnnotatedClass(getLookupServerResourceImplName(clazz));
             annotatedClass.setSuperclass(TumlRestletGenerationUtil.ServerResource);
 //            annotatedClass.addToImplementedInterfaces(annotatedInf.getPathName());
             annotatedClass.setMyPackage(ojPackage);
             annotatedClass.setVisibility(TumlClassOperations.getVisibility(clazz.getVisibility()));
             addToSource(annotatedClass);
             addPrivateIdVariable(clazz, annotatedClass);
             addDefaultConstructor(annotatedClass);
             if (!clazz.isAbstract()) {
                 addPutRepresentation(clazz/*, annotatedInf*/, annotatedClass);
             }
             addToRouterEnum(clazz, annotatedClass);
         }
     }
 
     @Override
     public void visitAfter(Class clazz) {
     }
 
     private void addPutRepresentation(Class clazz, OJAnnotatedClass annotatedClass) {
 
 //        OJAnnotatedOperation putInf = new OJAnnotatedOperation("put", TumlRestletGenerationUtil.Representation);
 //        putInf.addParam("entity", TumlRestletGenerationUtil.Representation);
 //        annotatedInf.addToOperations(putInf);
 //        putInf.addAnnotationIfNew(new OJAnnotationValue(TumlRestletGenerationUtil.Put, "json"));
 
         OJAnnotatedOperation put = new OJAnnotatedOperation("put", TumlRestletGenerationUtil.Representation);
         put.addParam("entity", TumlRestletGenerationUtil.Representation);
         put.addToThrows(TumlRestletGenerationUtil.ResourceException);
         annotatedClass.addToImports(TumlRestletGenerationUtil.ResourceException);
         TinkerGenerationUtil.addOverrideAnnotation(put);
 
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
 
         //get the lookup uri
         ojTry.getTryPart().addToStatements("String lookupUri = getQueryValue(\"lookupUri\")");
         ojTry.getTryPart().addToStatements("lookupUri = \"riap://host\" + lookupUri");
         ojTry.getTryPart().addToStatements("int fakeIdIndex = lookupUri.indexOf(\"fake\")");
         OJIfStatement ifFakeId = new OJIfStatement("fakeIdIndex != -1");
         ifFakeId.addToThenPart("int indexOfForwardSlash = lookupUri.indexOf(\"/\", fakeIdIndex)");
         ifFakeId.addToThenPart("String fakeId = lookupUri.substring(fakeIdIndex, indexOfForwardSlash)");
         ifFakeId.addToThenPart("Object id = " + TinkerGenerationUtil.UmlgTmpIdManager.getLast() + ".INSTANCE.get(fakeId)");
         ifFakeId.addToThenPart("lookupUri = lookupUri.replace(fakeId, id.toString())");
         ojTry.getTryPart().addToStatements(ifFakeId);
         annotatedClass.addToImports(TinkerGenerationUtil.UmlgTmpIdManager);
 
         ojTry.getTryPart().addToStatements(TumlRestletGenerationUtil.ClientResource.getLast() + " cr = new ClientResource(lookupUri)");
         annotatedClass.addToImports(TumlRestletGenerationUtil.ClientResource);
         ojTry.getTryPart().addToStatements(TumlRestletGenerationUtil.Representation.getLast() + " result = cr.get()");
         ojTry.getTryPart().addToStatements("return result");
 
         ojTry.getFinallyPart().addToStatements(TinkerGenerationUtil.UmlgTmpIdManager.getLast() + ".INSTANCE.remove()");
         ojTry.getFinallyPart().addToStatements(TinkerGenerationUtil.graphDbAccess + ".rollback()");
 
         ojTry.setCatchParam(new OJParameter("e", new OJPathName("java.lang.Exception")));
 
         OJIfStatement ifRuntime = new OJIfStatement("e instanceof RuntimeException");
         ifRuntime.addToThenPart("throw (RuntimeException)e");
         ojTry.getCatchPart().addToStatements(ifRuntime);
         ojTry.getCatchPart().addToStatements("throw new RuntimeException(e)");
         put.getBody().addToStatements(ojTry);
 
 
         annotatedClass.addToImports(TinkerGenerationUtil.graphDbPathName);
         annotatedClass.addToOperations(put);
     }
 
     private void addToRouterEnum(Class clazz, OJAnnotatedClass annotatedClass) {
         OJEnum routerEnum = (OJEnum) this.workspace.findOJClass("restlet.RestletRouterEnum");
         OJEnumLiteral ojLiteral = new OJEnumLiteral(TumlClassOperations.className(clazz).toUpperCase() + "_forwardToLookup");
 
         OJField uri = new OJField();
         uri.setType(new OJPathName("String"));
         uri.setInitExp("\"/" + TumlClassOperations.className(clazz).toLowerCase() + "s/{" + TumlClassOperations.className(clazz).toLowerCase() + "Id}/forwardToLookup\"");
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
 
 }
