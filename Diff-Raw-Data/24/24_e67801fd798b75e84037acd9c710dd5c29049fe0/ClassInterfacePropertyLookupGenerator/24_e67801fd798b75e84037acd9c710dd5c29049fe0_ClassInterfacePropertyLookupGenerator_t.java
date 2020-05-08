 package org.umlg.javageneration.visitor.clazz;
 
 import org.eclipse.uml2.uml.*;
 import org.eclipse.uml2.uml.Class;
 import org.umlg.framework.VisitSubclasses;
 import org.umlg.framework.Visitor;
 import org.umlg.generation.Workspace;
 import org.umlg.java.metamodel.OJField;
 import org.umlg.java.metamodel.annotation.OJAnnotatedClass;
 import org.umlg.java.metamodel.annotation.OJAnnotatedOperation;
 import org.umlg.javageneration.util.PropertyWrapper;
 import org.umlg.javageneration.util.TinkerGenerationUtil;
 import org.umlg.javageneration.util.TumlClassOperations;
 import org.umlg.javageneration.visitor.BaseVisitor;
 
 import java.util.Set;
 
 public class ClassInterfacePropertyLookupGenerator extends BaseVisitor implements Visitor<Class> {
 
     public ClassInterfacePropertyLookupGenerator(Workspace workspace) {
         super(workspace);
     }
 
     @Override
     @VisitSubclasses({Class.class, AssociationClass.class})
     public void visitBefore(Class clazz) {
         OJAnnotatedClass annotatedClass = findOJClass(clazz);
         addLookupPropertiesFromInterfaces(annotatedClass, clazz);
     }
 
     @Override
     public void visitAfter(Class element) {
     }
 
     private void addLookupPropertiesFromInterfaces(OJAnnotatedClass annotatedClass, Class clazz) {
         Set<Property> properties = TumlClassOperations.getPropertiesOnRealizedInterfaces(clazz);
         for (Property p : properties) {
             PropertyWrapper propertyWrapper = new PropertyWrapper(p);
             if (propertyWrapper.needsLookup()) {
 
                 OJAnnotatedOperation lookupOnParent = new OJAnnotatedOperation(propertyWrapper.lookup());
                 String pathName = TumlClassOperations.getPathName(propertyWrapper.getType()).getLast();
                 lookupOnParent.setReturnType(TinkerGenerationUtil.tinkerSet.getCopy().addToGenerics(pathName));
 
                 OJField result = new OJField("result", TinkerGenerationUtil.tinkerSet.getCopy().addToGenerics(pathName));
                 result.setInitExp("new " + TinkerGenerationUtil.tumlMemorySet.getCopy().addToGenerics(TumlClassOperations.getPathName(propertyWrapper.getType()).getLast()).getLast() + "()");
                 lookupOnParent.getBody().addToLocals(result);
                 Set<Classifier> concreteImplementations = TumlClassOperations.getConcreteRealization((Interface) propertyWrapper.getType());
                int cnt = 1;
                 for (Classifier c : concreteImplementations) {
                     annotatedClass.addToImports(TumlClassOperations.getPathName(c));

                    if (propertyWrapper.isUnique()) {
                        PropertyWrapper otherEnd = new PropertyWrapper(propertyWrapper.getOtherEnd());
                        if (propertyWrapper.isOneToOne() || otherEnd.isOne()) {
                            lookupOnParent.getBody().addToStatements("Filter<" + propertyWrapper.javaBaseTypePath().getLast() + "> filter" + cnt + " = new Filter<" +
                                    propertyWrapper.javaBaseTypePath().getLast() + ">() {\n    @Override\n    public boolean filter(" +
                                    propertyWrapper.javaBaseTypePath().getLast() + " entity){\n        return entity." +
                                    otherEnd.getter() + "() == null;\n    }\n}");
                        } else {
                            lookupOnParent.getBody().addToStatements("Filter<" + propertyWrapper.javaBaseTypePath().getLast() + "> filter" + cnt + " = new Filter<" +
                                    propertyWrapper.javaBaseTypePath().getLast() + ">() {\n    @Override\n    public boolean filter(" +
                                    propertyWrapper.javaBaseTypePath().getLast() + " entity){\n        return !entity." +
                                    otherEnd.getter() + "().contains(" + otherEnd.javaBaseTypePath().getLast() + ".this);\n    }\n}");
                        }
                        lookupOnParent.getBody().addToStatements("result.addAll(" + TumlClassOperations.getMetaClassName(c) + ".getInstance().getAllInstances(filter" + cnt++ + "))");

                    } else {
                        lookupOnParent.getBody().addToStatements("result.addAll(" + TumlClassOperations.getMetaClassName(c) + ".getInstance().getAllInstances())");
                    }

                     annotatedClass.addToImports(TumlClassOperations.getMetaClassPathName(c));
                 }
                 lookupOnParent.getBody().addToStatements("return result");
                 annotatedClass.addToOperations(lookupOnParent);
             }
         }
     }
 
 
 }
