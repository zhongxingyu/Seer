 package org.tuml.restlet.visitor.clazz;
 
 import java.util.Collections;
 
 import org.eclipse.uml2.uml.Class;
 import org.opaeum.java.metamodel.OJPathName;
 import org.opaeum.java.metamodel.annotation.OJAnnotatedClass;
 import org.opaeum.java.metamodel.annotation.OJAnnotatedOperation;
 import org.opaeum.java.metamodel.annotation.OJEnum;
 import org.tuml.framework.Visitor;
 import org.tuml.generation.Workspace;
 import org.tuml.javageneration.util.TumlClassOperations;
 import org.tuml.javageneration.validation.Validation;
 import org.tuml.javageneration.visitor.BaseVisitor;
 import org.tuml.javageneration.visitor.clazz.RuntimePropertyImplementor;
 
 public class AddIdLiteralsToRuntimeEnum extends BaseVisitor implements Visitor<Class> {
 
 	public AddIdLiteralsToRuntimeEnum(Workspace workspace) {
 		super(workspace);
 	}
 
 	@Override
 	public void visitBefore(Class clazz) {
 		OJAnnotatedClass annotatedClass = findOJClass(clazz);
 		OJEnum ojEnum = annotatedClass.findEnum(TumlClassOperations.propertyEnumName(clazz));
 		addField(annotatedClass, ojEnum, "id");
 	}
 
 	@Override
 	public void visitAfter(Class element) {
 	}
 
 	private void addField(OJAnnotatedClass annotatedClass, OJEnum ojEnum, String fieldName) {
 		OJAnnotatedOperation fromLabel = ojEnum.findOperation("fromLabel", new OJPathName("String"));
 		OJAnnotatedOperation fromQualifiedName = ojEnum.findOperation("fromQualifiedName", new OJPathName("String"));
 		OJAnnotatedOperation fromInverseQualifiedName = ojEnum.findOperation("fromInverseQualifiedName", new OJPathName("String"));
		RuntimePropertyImplementor.addEnumLiteral(ojEnum, fromLabel, fromQualifiedName, fromInverseQualifiedName, fieldName, "not_applicable", "inverseOf::not_applicable", true, null,
 				Collections.<Validation> emptyList(), false, false, false, false, false, false, true, false, false, 1, 1, false, false, false, false, true, "");
 	}
 
 }
