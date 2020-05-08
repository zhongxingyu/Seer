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
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.BodyDeclaration;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
 import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
 import org.eclipse.jdt.internal.ui.JavaPluginImages;
 import org.eclipse.jdt.ui.text.java.IInvocationContext;
 import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
 import org.eclipse.swt.graphics.Image;
 
import edu.illinois.compositerefactorings.messages.CompositeRefactoringsMessages;

 @SuppressWarnings("restriction")
 public class CreateNewSuperclass extends CompositeRefactoringStep {
 
 	public CreateNewSuperclass(IInvocationContext context, ASTNode coveringNode, boolean problemsAtLocation) {
 		super(context, coveringNode, problemsAtLocation);
 	}
 
 	@Override
 	protected Collection<? extends IJavaElement> getInputs() {
 		Collection<IType> inputs= new ArrayList<IType>();
 		TypeDeclaration typeDeclaration= null;
 
 		if (context.getCoveredNode() instanceof TypeDeclaration) {
 			typeDeclaration= (TypeDeclaration)context.getCoveredNode();
 		} else if (coveringNode instanceof TypeDeclaration) {
 			typeDeclaration= (TypeDeclaration)coveringNode;
 		} else if (!(coveringNode instanceof BodyDeclaration) && coveringNode.getParent() != null && coveringNode.getParent() instanceof TypeDeclaration) {
 			typeDeclaration= (TypeDeclaration)coveringNode.getParent();
 		}
 
 		if (typeDeclaration != null) {
 			IType type= (IType)typeDeclaration.resolveBinding().getJavaElement();
 			inputs.add(type);
 		}
 
 		return inputs;
 	}
 
 	@Override
 	public Collection<ASTRewriteCorrectionProposal> getProposals(IJavaElement input) throws CoreException {
 		IType type= (IType)input;
 		TypeDeclaration typeDeclaration= (TypeDeclaration)((CompilationUnit)coveringNode.getRoot()).findDeclaringNode(type.getKey());
 		ASTNode node= typeDeclaration.getParent();
 		AST ast= node.getAST();
 		ASTRewrite rewrite= ASTRewrite.create(ast);
		String label= MessageFormat.format(CompositeRefactoringsMessages.CreateNewSuperclass_description, typeDeclaration.getName(), getCompilationUnit().getElementName());
 		Image image= JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
 		ASTRewriteCorrectionProposal proposal= new ASTRewriteCorrectionProposal(label, getCompilationUnit(), rewrite, 0, image);
 		TypeDeclaration newSuperclass= ast.newTypeDeclaration();
 		newSuperclass.setName(ast.newSimpleName("Super" + type.getElementName()));
 		ListRewrite listRewrite= rewrite.getListRewrite(node, CompilationUnit.TYPES_PROPERTY);
 		listRewrite.insertLast(newSuperclass, null);
 		rewrite.set(newSuperclass, TypeDeclaration.SUPERCLASS_TYPE_PROPERTY, typeDeclaration.getStructuralProperty(TypeDeclaration.SUPERCLASS_TYPE_PROPERTY), null);
 		rewrite.set(typeDeclaration, TypeDeclaration.SUPERCLASS_TYPE_PROPERTY, newSuperclass.getName(), null);
 		Collection<ASTRewriteCorrectionProposal> proposals= new ArrayList<ASTRewriteCorrectionProposal>();
 		proposals.add(proposal);
 		return proposals;
 	}
 
 }
