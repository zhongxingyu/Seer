 /**
  * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 
 package edu.illinois.compositerefactorings.refactorings;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.ToolFactory;
 import org.eclipse.jdt.core.compiler.IScanner;
 import org.eclipse.jdt.core.compiler.ITerminalSymbols;
 import org.eclipse.jdt.core.compiler.InvalidInputException;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.Block;
 import org.eclipse.jdt.core.dom.BodyDeclaration;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.Javadoc;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
 import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
 import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
 import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
 import org.eclipse.jdt.internal.corext.dom.TokenScanner;
 import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
 import org.eclipse.jdt.internal.corext.refactoring.changes.CreateCompilationUnitChange;
 import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
 import org.eclipse.jdt.internal.corext.refactoring.util.ResourceUtil;
 import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
 import org.eclipse.jdt.internal.ui.JavaPlugin;
 import org.eclipse.jdt.ui.CodeGeneration;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.ltk.core.refactoring.resource.ResourceChange;
 import org.eclipse.text.edits.TextEdit;
 
 @SuppressWarnings({ "restriction", "unchecked" })
 public class NewClassCreator {
 
 	private String fClassName;
 
 	private IPackageFragment fPackageFragment;
 
	public NewClassCreator(String fClassName, IPackageFragment fPackageFragment) {
 		this.fClassName= fClassName;
 		this.fPackageFragment= fPackageFragment;
 	}
 
 	public String getClassName() {
 		return fClassName;
 	}
 
 	public IPackageFragment getPackageFragment() {
 		return fPackageFragment;
 	}
 
 	public List<ResourceChange> createTopLevelParameterObject() throws CoreException {
 		List<ResourceChange> changes= new ArrayList<ResourceChange>();
 		ICompilationUnit unit= getPackageFragment().getCompilationUnit(getClassName() + JavaModelUtil.DEFAULT_CU_SUFFIX);
 		Assert.isTrue(!unit.exists());
 		IJavaProject javaProject= unit.getJavaProject();
 		ICompilationUnit workingCopy= unit.getWorkingCopy(null);
 
 		try {
 			// create stub with comments and dummy type
 			String lineDelimiter= StubUtility.getLineDelimiterUsed(javaProject);
 			String fileComment= getFileComment(workingCopy, lineDelimiter);
 			String typeComment= getTypeComment(workingCopy, lineDelimiter);
 			String content= CodeGeneration.getCompilationUnitContent(workingCopy, fileComment, typeComment, "class " + getClassName() + "{}", lineDelimiter); //$NON-NLS-1$ //$NON-NLS-2$
 			workingCopy.getBuffer().setContents(content);
 
 			CompilationUnitRewrite cuRewrite= new CompilationUnitRewrite(workingCopy);
 			ASTRewrite rewriter= cuRewrite.getASTRewrite();
 			CompilationUnit root= cuRewrite.getRoot();
 			AST ast= cuRewrite.getAST();
 			ImportRewrite importRewrite= cuRewrite.getImportRewrite();
 
 			// retrieve&replace dummy type with real class
 			ListRewrite types= rewriter.getListRewrite(root, CompilationUnit.TYPES_PROPERTY);
 			ASTNode dummyType= (ASTNode)types.getOriginalList().get(0);
 			TypeDeclaration classDeclaration= createClassDeclaration(getClassName(), cuRewrite);
 			classDeclaration.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
 			Javadoc javadoc= (Javadoc)dummyType.getStructuralProperty(TypeDeclaration.JAVADOC_PROPERTY);
 			rewriter.set(classDeclaration, TypeDeclaration.JAVADOC_PROPERTY, javadoc, null);
 			types.replace(dummyType, classDeclaration, null);
 
 			// Apply rewrites and discard workingcopy
 			// Using CompilationUnitRewrite.createChange() leads to strange
 			// results
 			String charset= ResourceUtil.getFile(unit).getCharset(false);
 			Document document= new Document(content);
 			try {
 				rewriter.rewriteAST().apply(document);
 				TextEdit rewriteImports= importRewrite.rewriteImports(null);
 				rewriteImports.apply(document);
 			} catch (BadLocationException e) {
 				throw new CoreException(new Status(IStatus.ERROR, JavaPlugin.getPluginId(), RefactoringCoreMessages.IntroduceParameterObjectRefactoring_parameter_object_creation_error, e));
 			}
 			String docContent= document.get();
 			CreateCompilationUnitChange compilationUnitChange= new CreateCompilationUnitChange(unit, docContent, charset);
 			changes.add(compilationUnitChange);
 		} finally {
 			workingCopy.discardWorkingCopy();
 		}
 		return changes;
 	}
 
 	public TypeDeclaration createClassDeclaration(String declaringType, CompilationUnitRewrite cuRewrite) throws CoreException {
 		AST ast= cuRewrite.getAST();
 		TypeDeclaration typeDeclaration= ast.newTypeDeclaration();
 		typeDeclaration.setName(ast.newSimpleName(fClassName));
 		List<BodyDeclaration> body= typeDeclaration.bodyDeclarations();
 		MethodDeclaration constructor= createConstructor(declaringType, cuRewrite);
 		body.add(constructor);
 		return typeDeclaration;
 	}
 
 	private MethodDeclaration createConstructor(String declaringTypeName, CompilationUnitRewrite cuRewrite) throws CoreException {
 		AST ast= cuRewrite.getAST();
 		ICompilationUnit unit= cuRewrite.getCu();
 		IJavaProject project= unit.getJavaProject();
 
 		MethodDeclaration methodDeclaration= ast.newMethodDeclaration();
 		methodDeclaration.setName(ast.newSimpleName(fClassName));
 		methodDeclaration.setConstructor(true);
 		methodDeclaration.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
 		String lineDelimiter= StubUtility.getLineDelimiterUsed(unit);
 		if (createComments(project)) {
 			String comment= CodeGeneration.getMethodComment(unit, declaringTypeName, methodDeclaration, null, lineDelimiter);
 			if (comment != null) {
 				Javadoc doc= (Javadoc)cuRewrite.getASTRewrite().createStringPlaceholder(comment, ASTNode.JAVADOC);
 				methodDeclaration.setJavadoc(doc);
 			}
 		}
 		Block block= ast.newBlock();
 		methodDeclaration.setBody(block);
 		return methodDeclaration;
 	}
 
 	protected String getFileComment(ICompilationUnit parentCU, String lineDelimiter) throws CoreException {
 		if (StubUtility.doAddComments(parentCU.getJavaProject())) {
 			return CodeGeneration.getFileComment(parentCU, lineDelimiter);
 		}
 		return null;
 	}
 
 	protected String getTypeComment(ICompilationUnit parentCU, String lineDelimiter) throws CoreException {
 		if (StubUtility.doAddComments(parentCU.getJavaProject())) {
 			StringBuffer typeName= new StringBuffer();
 			typeName.append(getClassName());
 			String[] typeParamNames= new String[0];
 			String comment= CodeGeneration.getTypeComment(parentCU, typeName.toString(), typeParamNames, lineDelimiter);
 			if (comment != null && isValidComment(comment)) {
 				return comment;
 			}
 		}
 		return null;
 	}
 
 	private boolean isValidComment(String template) {
 		IScanner scanner= ToolFactory.createScanner(true, false, false, false);
 		scanner.setSource(template.toCharArray());
 		try {
 			int next= scanner.getNextToken();
 			while (TokenScanner.isComment(next)) {
 				next= scanner.getNextToken();
 			}
 			return next == ITerminalSymbols.TokenNameEOF;
 		} catch (InvalidInputException e) {
 		}
 		return false;
 	}
 
 	private boolean createComments(IJavaProject project) {
 		return StubUtility.doAddComments(project);
 	}
 
 }
