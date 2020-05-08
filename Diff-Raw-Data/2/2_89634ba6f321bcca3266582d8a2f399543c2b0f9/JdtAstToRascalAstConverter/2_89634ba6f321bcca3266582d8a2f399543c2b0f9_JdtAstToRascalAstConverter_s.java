 /*******************************************************************************
  * Copyright (c) 2009-2012 CWI
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   * Anastasia Izmaylova - A.Izmaylova@cwi.nl (CWI)
 *******************************************************************************/
 package prototype.org.rascalmpl.eclipse.library.lang.java.jdt.refactorings.internal;
 
 import java.util.Stack;
 
 import org.eclipse.imp.pdb.facts.IValueFactory;
 import org.eclipse.imp.pdb.facts.type.TypeStore;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 
 /*
  * Extends the JdtAstToRascalAstConverter to annotate a node with the scope information (refactoring-specific)
  */
 public class JdtAstToRascalAstConverter extends org.rascalmpl.eclipse.library.lang.java.jdt.internal.JdtAstToRascalAstConverter {
 	
 	private final BindingConverter bindingConverter;
 	
 	public JdtAstToRascalAstConverter(final IValueFactory values, final TypeStore typeStore, final BindingConverter bindingConverter) {
		super(values, typeStore, bindingConverter);
 		this.bindingConverter = bindingConverter;
 	}
 	
 	public static final String ANNOTATION_SCOPES = "scopes";
 	
 	private Stack<ITypeBinding> scopes;
 	
 	public void preVisit(ASTNode node) {
 		super.preVisit(node);
 		scopes = this.getBindingsImporter().getEnclosingTypes();
 	}
 		
 	public void postVisit(ASTNode node) {
 		super.postVisit(node);
 		if(!scopes.isEmpty())
 			this.setRascalAstNodeAnnotation(ANNOTATION_SCOPES, 
 					bindingConverter.getEntities(scopes.toArray(new ITypeBinding[] {})));
 	}
 }
