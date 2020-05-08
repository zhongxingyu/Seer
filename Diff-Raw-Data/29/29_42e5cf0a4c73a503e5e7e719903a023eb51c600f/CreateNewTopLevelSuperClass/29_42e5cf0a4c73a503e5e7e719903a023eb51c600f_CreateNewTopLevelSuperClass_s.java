 /**
  * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 
 package edu.illinois.compositerefactorings.steps;
 
import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.BodyDeclaration;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
 import org.eclipse.jdt.ui.text.java.IInvocationContext;
 import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
 
import edu.illinois.compositerefactorings.messages.CompositeRefactoringsMessages;
import edu.illinois.compositerefactorings.refactorings.createnewtoplevelsuperclass.CreateNewTopLevelSuperClassDescriptor;

@SuppressWarnings("restriction")
 public class CreateNewTopLevelSuperClass extends RefactoringBasedStep {
 
 	public CreateNewTopLevelSuperClass(IInvocationContext context, ASTNode coveringNode, boolean problemsAtLocation) {
 		super(context, coveringNode, problemsAtLocation);
 	}
 
 	@Override
 	protected Collection<? extends TypeDeclaration> getInputs() {
 		Collection<TypeDeclaration> inputs= new ArrayList<TypeDeclaration>();
 		TypeDeclaration typeDeclaration= null;
 
 		if (context.getCoveredNode() instanceof TypeDeclaration) {
 			typeDeclaration= (TypeDeclaration)context.getCoveredNode();
 		} else if (coveringNode instanceof TypeDeclaration) {
 			typeDeclaration= (TypeDeclaration)coveringNode;
 		} else if (!(coveringNode instanceof BodyDeclaration) && coveringNode.getParent() != null && coveringNode.getParent() instanceof TypeDeclaration) {
 			typeDeclaration= (TypeDeclaration)coveringNode.getParent();
 		}
 
 		if (typeDeclaration != null) {
 			inputs.add(typeDeclaration);
 		}
 
 		return inputs;
 	}
 
 	@Override
 	protected Collection<RefactoringDescriptor> getDescriptors(Object input) throws CoreException {
 		TypeDeclaration typeDeclaration= (TypeDeclaration)input;
 		ITypeBinding typeBinding= typeDeclaration.resolveBinding();
 		IType type= (IType)typeBinding.getJavaElement();
 		Collection<RefactoringDescriptor> descriptors= new ArrayList<RefactoringDescriptor>();
		String description= MessageFormat.format(CompositeRefactoringsMessages.CreateNewTopLevelSuperClass_description, type.getElementName());
		Map<String, String> arguments= new HashMap<String, String>();
		arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT, JavaRefactoringDescriptorUtil.elementToHandle(getJavaProject().getElementName(), type));
		arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME, "Super" + type.getElementName());
		CreateNewTopLevelSuperClassDescriptor descriptor= new CreateNewTopLevelSuperClassDescriptor(getJavaProject().getElementName(), description, null, arguments,
				RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		descriptors.add(descriptor);
 		return descriptors;
 	}
 
 }
