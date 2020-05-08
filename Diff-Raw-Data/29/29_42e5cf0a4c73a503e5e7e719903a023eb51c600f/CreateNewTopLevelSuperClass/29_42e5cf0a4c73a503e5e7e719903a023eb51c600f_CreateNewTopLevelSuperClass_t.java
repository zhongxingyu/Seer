 /**
  * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 
 package edu.illinois.compositerefactorings.steps;
 
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.Collection;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.BodyDeclaration;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jdt.ui.text.java.IInvocationContext;
 import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
 
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
		descriptors.add(CreateNewSuperclassCommandHandler.createRefactoringDescriptor(Arrays.asList(type)));
 		return descriptors;
 	}
 
 }
