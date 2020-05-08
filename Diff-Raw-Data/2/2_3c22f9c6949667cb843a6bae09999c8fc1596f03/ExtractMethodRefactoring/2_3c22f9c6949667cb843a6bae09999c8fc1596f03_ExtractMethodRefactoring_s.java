 /*******************************************************************************
  * Copyright (c) 2000, 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Benjamin Muskalla <bmuskalla@eclipsesource.com> - [extract method] Does not replace similar code in parent class of anonymous class - https://bugs.eclipse.org/bugs/show_bug.cgi?id=160853
  *     Benjamin Muskalla <bmuskalla@eclipsesource.com> - [extract method] Extract method and continue https://bugs.eclipse.org/bugs/show_bug.cgi?id=48056
  *     Benjamin Muskalla <bmuskalla@eclipsesource.com> - [extract method] should declare method static if extracted from anonymous in static method - https://bugs.eclipse.org/bugs/show_bug.cgi?id=152004
  *******************************************************************************/
 package org.eclipse.dltk.internal.javascript.corext.refactoring.code;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.manipulation.RefactoringChecks;
 import org.eclipse.dltk.core.manipulation.SourceModuleChange;
 import org.eclipse.dltk.internal.corext.refactoring.ScriptRefactoringDescriptor;
 import org.eclipse.dltk.internal.corext.refactoring.util.ResourceUtil;
 import org.eclipse.dltk.internal.javascript.core.manipulation.Messages;
 import org.eclipse.dltk.internal.javascript.corext.refactoring.Checks;
 import org.eclipse.dltk.internal.javascript.corext.refactoring.ParameterInfo;
 import org.eclipse.dltk.internal.javascript.corext.refactoring.RefactoringCoreMessages;
 import org.eclipse.dltk.internal.javascript.corext.refactoring.code.flow.VariableBinding;
 import org.eclipse.dltk.internal.javascript.corext.refactoring.util.Selection;
 import org.eclipse.dltk.javascript.core.dom.BinaryExpression;
 import org.eclipse.dltk.javascript.core.dom.BinaryOperator;
 import org.eclipse.dltk.javascript.core.dom.BlockStatement;
 import org.eclipse.dltk.javascript.core.dom.CallExpression;
 import org.eclipse.dltk.javascript.core.dom.DomFactory;
 import org.eclipse.dltk.javascript.core.dom.DomPackage;
 import org.eclipse.dltk.javascript.core.dom.Expression;
 import org.eclipse.dltk.javascript.core.dom.ExpressionStatement;
 import org.eclipse.dltk.javascript.core.dom.FunctionExpression;
 import org.eclipse.dltk.javascript.core.dom.Identifier;
 import org.eclipse.dltk.javascript.core.dom.Node;
 import org.eclipse.dltk.javascript.core.dom.Parameter;
 import org.eclipse.dltk.javascript.core.dom.ReturnStatement;
 import org.eclipse.dltk.javascript.core.dom.Source;
 import org.eclipse.dltk.javascript.core.dom.Statement;
 import org.eclipse.dltk.javascript.core.dom.Type;
 import org.eclipse.dltk.javascript.core.dom.VariableDeclaration;
 import org.eclipse.dltk.javascript.core.dom.VariableReference;
 import org.eclipse.dltk.javascript.core.dom.VariableStatement;
 import org.eclipse.dltk.javascript.core.dom.rewrite.ASTConverter;
 import org.eclipse.dltk.javascript.core.dom.rewrite.Generator;
 import org.eclipse.dltk.javascript.core.dom.rewrite.RewriteAnalyzer;
 import org.eclipse.dltk.javascript.core.dom.rewrite.VariableLookup;
 import org.eclipse.dltk.javascript.core.refactoring.descriptors.ExtractMethodDescriptor;
 import org.eclipse.dltk.javascript.parser.JavaScriptParserUtil;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.change.ChangeDescription;
 import org.eclipse.emf.ecore.change.util.ChangeRecorder;
 import org.eclipse.ltk.core.refactoring.Change;
 import org.eclipse.ltk.core.refactoring.Refactoring;
 import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
 import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
 import org.eclipse.ltk.core.refactoring.RefactoringStatus;
 import org.eclipse.ltk.core.refactoring.TextFileChange;
 import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
 
 /**
  * Extracts a method in a compilation unit based on a text selection range.
  */
 public class ExtractMethodRefactoring extends Refactoring {
 
 	//private static final String ATTRIBUTE_VISIBILITY= "visibility"; //$NON-NLS-1$
 	private static final String ATTRIBUTE_DESTINATION= "destination"; //$NON-NLS-1$
 	/*private static final String ATTRIBUTE_COMMENTS= "comments"; //$NON-NLS-1$
 	private static final String ATTRIBUTE_REPLACE= "replace"; //$NON-NLS-1$
 	private static final String ATTRIBUTE_EXCEPTIONS= "exceptions"; //$NON-NLS-1$*/
 
 	private ISourceModule fCUnit;
 	private String fSource;
 	private Source fRoot;
 	//private ImportRewrite fImportRewriter;
 	private int fSelectionStart;
 	private int fSelectionLength;
 	/*private AST fAST;
 	private ASTRewrite fRewriter;*/
 	private ExtractMethodAnalyzer fAnalyzer;
 	//private int fVisibility;
 	private String fMethodName;
 	//private boolean fThrowRuntimeExceptions;
 	private List<ParameterInfo> fParameterInfos;
 	private Set<String> fUsedNames;
 	/*private boolean fGenerateScriptdoc;
 	private boolean fReplaceDuplicates;
 	private SnippetFinder.Match[] fDuplicates;*/
 	private int fDestinationIndex= 0;
 	private Node fDestination;
 	private Node[] fDestinations;
 	//private LinkedProposalModel fLinkedProposalModel;
 
 	private static final String EMPTY= ""; //$NON-NLS-1$
 
 	/*private static final String KEY_TYPE= "type"; //$NON-NLS-1$
 	private static final String KEY_NAME= "name"; //$NON-NLS-1$
 
 	private static class UsedNamesCollector extends ASTVisitor {
 		private Set result= new HashSet();
 		private Set fIgnore= new HashSet();
 		public static Set perform(ASTNode[] nodes) {
 			UsedNamesCollector collector= new UsedNamesCollector();
 			for (int i= 0; i < nodes.length; i++) {
 				nodes[i].accept(collector);
 			}
 			return collector.result;
 		}
 		public boolean visit(FieldAccess node) {
 			Expression exp= node.getExpression();
 			if (exp != null)
 				fIgnore.add(node.getName());
 			return true;
 		}
 		public void endVisit(FieldAccess node) {
 			fIgnore.remove(node.getName());
 		}
 		public boolean visit(MethodInvocation node) {
 			Expression exp= node.getExpression();
 			if (exp != null)
 				fIgnore.add(node.getName());
 			return true;
 		}
 		public void endVisit(MethodInvocation node) {
 			fIgnore.remove(node.getName());
 		}
 		public boolean visit(QualifiedName node) {
 			fIgnore.add(node.getName());
 			return true;
 		}
 		public void endVisit(QualifiedName node) {
 			fIgnore.remove(node.getName());
 		}
 		public boolean visit(SimpleName node) {
 			if (!fIgnore.contains(node))
 				result.add(node.getIdentifier());
 			return true;
 		}
 		public boolean visit(TypeDeclaration node) {
 			return visitType(node);
 		}
 		public boolean visit(AnnotationTypeDeclaration node) {
 			return visitType(node);
 		}
 		public boolean visit(EnumDeclaration node) {
 			return visitType(node);
 		}
 		private boolean visitType(AbstractTypeDeclaration node) {
 			result.add(node.getName().getIdentifier());
 			// don't dive into type declaration since they open a new
 			// context.
 			return false;
 		}
 	}
 
 	/**
 	 * Creates a new extract method refactoring
 	 * @param unit the compilation unit, or <code>null</code> if invoked by scripting
 	 * @param selectionStart selection start
 	 * @param selectionLength selection end
 	 */
 	public ExtractMethodRefactoring(ISourceModule unit, int selectionStart, int selectionLength) {
 		fCUnit= unit;
 		fRoot= null;
 		fMethodName= "extracted"; //$NON-NLS-1$
 		fSelectionStart= selectionStart;
 		fSelectionLength= selectionLength;
 		//fVisibility= -1;
 	}
 
     /*public ExtractMethodRefactoring(ScriptRefactoringArguments arguments, RefactoringStatus status) {
    		this((ISourceModule) null, 0, 0);
    		RefactoringStatus initializeStatus= initialize(arguments);
    		status.merge(initializeStatus);
     }
 
 	/**
 	 * Creates a new extract method refactoring
 	 * @param astRoot the AST root of an AST created from a compilation unit
 	 * @param selectionStart start
 	 * @param selectionLength length
 	 *
 	public ExtractMethodRefactoring(Source astRoot, int selectionStart, int selectionLength) {
 		this((ISourceModule) astRoot.getTypeRoot(), selectionStart, selectionLength);
 		fRoot= astRoot;
 	}
 
 	public void setLinkedProposalModel(LinkedProposalModel linkedProposalModel) {
 		fLinkedProposalModel= linkedProposalModel;
 	}*/
 
 	 public String getName() {
 	 	return RefactoringCoreMessages.ExtractMethodRefactoring_name;
 	 }
 
 	/**
 	 * Checks if the refactoring can be activated. Activation typically means, if a
 	 * corresponding menu entry can be added to the UI.
 	 *
 	 * @param pm a progress monitor to report progress during activation checking.
 	 * @return the refactoring status describing the result of the activation check.
 	 * @throws CoreException if checking fails
 	 */
 	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
 		RefactoringStatus result= new RefactoringStatus();
 		pm.beginTask("", 100); //$NON-NLS-1$
 
 		if (fSelectionStart < 0 || fSelectionLength == 0) {
 			result.addFatalError(RefactoringCoreMessages.ExtractMethodRefactoring_no_set_of_statements);
 			return result;
 		}
 
 		IFile[] changedFiles= ResourceUtil.getFiles(new ISourceModule[]{fCUnit});
 		result.merge(RefactoringChecks.validateModifiesFiles(changedFiles, getValidationContext()));
 		if (result.hasFatalError())
 			return result;
 		result.merge(ResourceChangeChecker.checkFilesToBeChanged(changedFiles, new SubProgressMonitor(pm, 1)));
 
 		if (fRoot == null) {
 			fRoot = (Source)ASTConverter.convert(JavaScriptParserUtil.parse(fCUnit));
 		}
 		//fImportRewriter= StubUtility.createImportRewrite(fRoot, true);
 
 		//fAST= fRoot.getAST();
 		fAnalyzer= new ExtractMethodAnalyzer(fCUnit, Selection.createFromStartLength(fSelectionStart, fSelectionLength));
 		//fRoot.accept(fAnalyzer);
 
 		result.merge(fAnalyzer.checkInitialConditions(fRoot));
 		if (result.hasFatalError())
 			return result;
 		
 		fSource = fCUnit.getSource();
 		Node[] nodes = fAnalyzer.getSelectedNodes();
 		boolean badSelection = false;
 		for(int i=fSelectionStart;i<nodes[0].getBegin();i++)
 			if (!Character.isWhitespace(fSource.charAt(i))) badSelection = true; 
 		for(int i=nodes[nodes.length-1].getEnd();i<fSelectionStart+fSelectionLength;i++)
 			if (!Character.isWhitespace(fSource.charAt(i))) badSelection = true;
 		if (badSelection) {
 			result.addFatalError(RefactoringCoreMessages.StatementAnalyzer_doesNotCover);
 			return result;
 		}
 		/*if (fVisibility == -1) {
 			setVisibility(Modifier.PRIVATE);
 		}*/
 		initializeParameterInfos();
 		initializeUsedNames();
 		//initializeDuplicates();
 		initializeDestinations();
 		return result;
 	}
 
 	/**
 	 * Sets the method name to be used for the extracted method.
 	 *
 	 * @param name the new method name.
 	 */
 	public void setMethodName(String name) {
 		fMethodName= name;
 	}
 
 	/**
 	 * Returns the method name to be used for the extracted method.
 	 * @return the method name to be used for the extracted method.
 	 *
 	public String getMethodName() {
 		return fMethodName;
 	}
 
 	/**
 	 * Sets the visibility of the new method.
 	 *
 	 * @param visibility the visibility of the new method. Valid values are
 	 *  "public", "protected", "", and "private"
 	 *
 	public void setVisibility(int visibility) {
 		fVisibility= visibility;
 	}
 
 	/**
 	 * Returns the visibility of the new method.
 	 *
 	 * @return the visibility of the new method
 	 *
 	public int getVisibility() {
 		return fVisibility;
 	}
 
 	/**
 	 * Returns the parameter infos.
 	 * @return a list of parameter infos.
 	 */
 	public List<ParameterInfo> getParameterInfos() {
 		return fParameterInfos;
 	}
 
 	/**
 	 * Sets whether the new method signature throws runtime exceptions.
 	 *
 	 * @param throwRuntimeExceptions flag indicating if the new method
 	 * 	throws runtime exceptions
 	 *
 	public void setThrowRuntimeExceptions(boolean throwRuntimeExceptions) {
 		fThrowRuntimeExceptions= throwRuntimeExceptions;
 	}
 
 	/**
 	 * Checks if the new method name is a valid method name. This method doesn't
 	 * check if a method with the same name already exists in the hierarchy. This
 	 * check is done in <code>checkInput</code> since it is expensive.
 	 * @return validation status
 	 */
 	public RefactoringStatus checkMethodName() {
 		return Checks.validateIdentifier(fMethodName);
 	}
 
 	public Node[] getDestinations() {
 		return fDestinations;
 	}
 
 	public void setDestination(int index) {
 		fDestination= fDestinations[index];
 		fDestinationIndex= index;
 	}
 
 	/**
 	 * Checks if the parameter names are valid.
 	 * @return validation status
 	 */
 	public RefactoringStatus checkParameterNames() {
 		RefactoringStatus result= new RefactoringStatus();
 		for (ParameterInfo parameter : fParameterInfos) {
 			result.merge(Checks.validateIdentifier(parameter.getNewName()));
 			for (ParameterInfo other : fParameterInfos) {
 				if (parameter != other && other.getNewName().equals(parameter.getNewName())) {
 					result.addError(Messages.format(
 						RefactoringCoreMessages.ExtractMethodRefactoring_error_sameParameter,
 						//BasicElementLabels.getScriptElementName(other.getNewName())
 						other.getNewName()));
 					return result;
 				}
 			}
 			if (parameter.isRenamed() && fUsedNames.contains(parameter.getNewName())) {
 				result.addError(Messages.format(
 					RefactoringCoreMessages.ExtractMethodRefactoring_error_nameInUse,
 					//BasicElementLabels.getScriptElementName(parameter.getNewName())
 					parameter.getNewName()));
 				return result;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Checks if varargs are ordered correctly.
 	 * @return validation status
 	 *
 	public RefactoringStatus checkVarargOrder() {
 		for (Iterator iter= fParameterInfos.iterator(); iter.hasNext();) {
 			ParameterInfo info= (ParameterInfo)iter.next();
 			if (info.isOldVarargs() && iter.hasNext()) {
 				return RefactoringStatus.createFatalErrorStatus(Messages.format(
 					 RefactoringCoreMessages.ExtractMethodRefactoring_error_vararg_ordering,
 					 BasicElementLabels.getScriptElementName(info.getOldName())));
 			}
 		}
 		return new RefactoringStatus();
 	}
 
 	/**
 	 * Returns the names already in use in the selected statements/expressions.
 	 *
 	 * @return names already in use.
 	 *
 	public Set getUsedNames() {
 		return fUsedNames;
 	}
 
 	/* (non-Scriptdoc)
 	 * Method declared in Refactoring
 	 */
 	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException {
 		pm.beginTask(RefactoringCoreMessages.ExtractMethodRefactoring_checking_new_name, 1);
 		pm.subTask(EMPTY);
 
 		RefactoringStatus result= checkMethodName();
 		result.merge(checkParameterNames());
 		/*result.merge(checkVarargOrder());
 		pm.worked(1);
 		if (pm.isCanceled())
 			throw new OperationCanceledException();
 
 		Node node= fAnalyzer.getEnclosingNode();
 		if (node != null) {
 			fAnalyzer.checkInput(result, fMethodName);
 			pm.worked(1);
 		}*/
 		pm.done();
 		return result;
 	}
 
 	/* (non-Scriptdoc)
 	 * Method declared in IRefactoring
 	 */
 	public Change createChange(IProgressMonitor pm) throws CoreException {
 		if (fMethodName == null)
 			return null;
 		pm.beginTask("", 2); //$NON-NLS-1$
 		try {
 			ChangeRecorder cr = new ChangeRecorder(fRoot);
 			Node[] nodes = fAnalyzer.getSelectedNodes();
 			boolean isExpr = fAnalyzer.isExpressionSelected();
 			// replace method parameter names
 			Map<String,String> renamings = new HashMap<String,String>();
 			for (ParameterInfo parameter : fParameterInfos)
 				if (parameter.isRenamed())
 					renamings.put(parameter.getOldName(),parameter.getNewName());
 			for (Node node : nodes) {
 				List<Identifier> oldNames = VariableLookup.findReferences(node, renamings.keySet());
 				for (Identifier ref : oldNames) {
 					ref.setName(renamings.get(ref.getName()));
 				}
 			}			
 			// create call
 			CallExpression invocation= DomFactory.eINSTANCE.createCallExpression();
 			{
 				VariableReference ref = DomFactory.eINSTANCE.createVariableReference();
 				Identifier id = DomFactory.eINSTANCE.createIdentifier();
 				id.setName(fMethodName);
 				ref.setVariable(id);
 				invocation.setApplicant(ref);
 			}
 			for (ParameterInfo parameter : fParameterInfos) {
 				Identifier name = DomFactory.eINSTANCE.createIdentifier();
 				name.setName(parameter.getOldName());
 				VariableReference local = DomFactory.eINSTANCE.createVariableReference();
 				local.setVariable(name);
 				invocation.getArguments().add(local);
 			}
 			if (isExpr) {
 				// replace with call
 				EReference ref = nodes[0].eContainmentFeature();
 				if (ref.isMany()) {
 					EList<Expression> exprList = (EList<Expression>)nodes[0].eContainer().eGet(ref);
 					exprList.set(exprList.lastIndexOf(nodes[0]), (Expression)invocation);
 				} else {
 					nodes[0].eContainer().eSet(ref, invocation);
 				}
 			} else {
 				// create replacement
 				Node call;
 				int returnKind= fAnalyzer.getReturnKind();
 				switch (returnKind) {
 				case ExtractMethodAnalyzer.ACCESS_TO_LOCAL:
 					VariableBinding binding= fAnalyzer.getReturnLocal();
 					if (binding != null) {
 						call = createDeclaration(binding, invocation);
 					} else {
 						BinaryExpression assignment= DomFactory.eINSTANCE.createBinaryExpression();
 						assignment.setOperation(BinaryOperator.ASSIGN);
 						VariableReference retVar = DomFactory.eINSTANCE.createVariableReference();
 						Identifier id = DomFactory.eINSTANCE.createIdentifier();
 						id.setName(fAnalyzer.getReturnValue().getName());
 						retVar.setVariable(id);
 						assignment.setLeft(retVar);
 						assignment.setRight(invocation);
 						call = assignment;
 					}
 					break;
 				case ExtractMethodAnalyzer.RETURN_STATEMENT_VALUE:
 					ReturnStatement rs=DomFactory.eINSTANCE.createReturnStatement();
 					rs.setExpression(invocation);
 					call = rs;
 					break;
 				default:
 					call = invocation;
 				}
 				if (call instanceof Expression) {
 					ExpressionStatement stmt = DomFactory.eINSTANCE.createExpressionStatement();
 					stmt.setExpression((Expression)call);
 					call = stmt;
 				}
 				List<Statement> callNodes= new ArrayList<Statement>(2);
 				callNodes.add((Statement)call);
 				if (returnKind == ExtractMethodAnalyzer.RETURN_STATEMENT_VOID && !fAnalyzer.isLastStatementSelected()) {
 					callNodes.add(DomFactory.eINSTANCE.createReturnStatement());
 				}
 				for (VariableBinding local : fAnalyzer.getCallerLocals())
 					callNodes.add(createDeclaration(local, null));
 				// replace with call
 				EReference ref = nodes[0].eContainmentFeature();
 				if (ref.isMany()) {
 					EList<Statement> list = (EList<Statement>)nodes[0].eContainer().eGet(ref);
 					int index = list.lastIndexOf(nodes[0]);
 					for(int i=nodes.length-1;i>=0;i--)
 						list.remove(index+i);
 					list.addAll(index, callNodes);
 				} else if (callNodes.size() == 1) {
 					nodes[0].eContainer().eSet(ref, callNodes.get(0));
 				} else {
 					BlockStatement block = DomFactory.eINSTANCE.createBlockStatement();
 					block.getStatements().addAll(callNodes);
 					nodes[0].eContainer().eSet(ref, block);
 				}				
 			}
 			// create declaration
 			FunctionExpression mm = createNewMethodDeclaration();
 			List<Statement> statements = mm.getBody().getStatements();
 			VariableBinding[] methodLocals= fAnalyzer.getMethodLocals();
 			for (int i= 0; i < methodLocals.length; i++) {
 				if (methodLocals[i] != null)
 					statements.add(createDeclaration(methodLocals[i],null));
 			}
 			if (isExpr) {
 				ReturnStatement rs = DomFactory.eINSTANCE.createReturnStatement();
 				rs.setExpression((Expression)nodes[0]);
 				statements.add(rs);
 			} else {
 				for(Node node : nodes) 
 					statements.add((Statement)node);
 				VariableBinding returnValue= fAnalyzer.getReturnValue();
 				if (returnValue != null) {
 					ReturnStatement rs = DomFactory.eINSTANCE.createReturnStatement();
 					VariableReference ret = DomFactory.eINSTANCE.createVariableReference();
 					Identifier rid = DomFactory.eINSTANCE.createIdentifier();
 					String name = returnValue.getName();
 					if (renamings.containsKey(name))
 						name = renamings.get(name);
 					rid.setName(name);
 					ret.setVariable(rid);
 					rs.setExpression(ret);
 					statements.add(rs);
 				}
 			}
 			// add declaration
 			ExpressionStatement stmt = DomFactory.eINSTANCE.createExpressionStatement();
 			stmt.setExpression(mm);
 			Node enclosing = fDestination;
 			EReference ref = enclosing.eContainmentFeature();
 			assert ref.isMany();
 			EList<Statement> list = (EList<Statement>)enclosing.eContainer().eGet(ref);
			list.add(list.lastIndexOf(enclosing),stmt);
 			// TODO replace branches
 			ChangeDescription cd = cr.endRecording();
 			RewriteAnalyzer ra = new RewriteAnalyzer(cd, fSource);
 			SourceModuleChange result= new SourceModuleChange(RefactoringCoreMessages.ExtractMethodRefactoring_change_name, fCUnit);
 			ra.rewrite(fRoot);
 			result.setSaveMode(TextFileChange.KEEP_SAVE_STATE);
 			result.setDescriptor(new RefactoringChangeDescriptor(getRefactoringDescriptor()));
 			result.setEdit(ra.getEdit());
 			cd.apply();
 			return result;
 		} finally {
 			pm.done();
 		}
 	}
 
 	/*private void replaceBranches(final SourceModuleChange result) {
 		ASTNode[] selectedNodes= fAnalyzer.getSelectedNodes();
 		for (int i= 0; i < selectedNodes.length; i++) {
 			ASTNode astNode= selectedNodes[i];
 			astNode.accept(new ASTVisitor() {
 				private LinkedList fOpenLoopLabels= new LinkedList();
 
 				private void registerLoopLabel(Statement node) {
 					String identifier;
 					if (node.getParent() instanceof LabeledStatement) {
 						LabeledStatement labeledStatement= (LabeledStatement)node.getParent();
 						identifier= labeledStatement.getLabel().getIdentifier();
 					} else {
 						identifier= null;
 					}
 					fOpenLoopLabels.add(identifier);
 				}
 				
 				public boolean visit(ForStatement node) {
 					registerLoopLabel(node);
 					return super.visit(node);
 				}
 
 				public void endVisit(ForStatement node) {
 					fOpenLoopLabels.removeLast();
 				}
 
 				public boolean visit(WhileStatement node) {
 					registerLoopLabel(node);
 					return super.visit(node);
 				}
 
 				public void endVisit(WhileStatement node) {
 					fOpenLoopLabels.removeLast();
 				}
 
 				public boolean visit(EnhancedForStatement node) {
 					registerLoopLabel(node);
 					return super.visit(node);
 				}
 
 				public void endVisit(EnhancedForStatement node) {
 					fOpenLoopLabels.removeLast();
 				}
 
 				public boolean visit(DoStatement node) {
 					registerLoopLabel(node);
 					return super.visit(node);
 				}
 
 				public void endVisit(DoStatement node) {
 					fOpenLoopLabels.removeLast();
 				}
 
 				public void endVisit(ContinueStatement node) {
 					final SimpleName label= node.getLabel();
 					if (fOpenLoopLabels.isEmpty() || (label != null && !fOpenLoopLabels.contains(label.getIdentifier()))) {
 						TextEditGroup description= new TextEditGroup(RefactoringCoreMessages.ExtractMethodRefactoring_replace_continue);
 						result.addTextEditGroup(description);
 
 						ReturnStatement rs= fAST.newReturnStatement();
 						IVariableBinding returnValue= fAnalyzer.getReturnValue();
 						if (returnValue != null) {
 							rs.setExpression(fAST.newSimpleName(getName(returnValue)));
 						}
 
 						fRewriter.replace(node, rs, description);
 					}
 				}
 			});
 		}
 	}*/
 
 	private ExtractMethodDescriptor getRefactoringDescriptor() {
 		final Map arguments= new HashMap();
 		String project= null;
 		IScriptProject ScriptProject= fCUnit.getScriptProject();
 		if (ScriptProject != null)
 			project= ScriptProject.getElementName();
 		/*ITypeBinding type= null;
 		if (fDestination instanceof AbstractTypeDeclaration) {
 			final AbstractTypeDeclaration decl= (AbstractTypeDeclaration) fDestination;
 			type= decl.resolveBinding();
 		} else if (fDestination instanceof AnonymousClassDeclaration) {
 			final AnonymousClassDeclaration decl= (AnonymousClassDeclaration) fDestination;
 			type= decl.resolveBinding();
 		}
 		IMethodBinding method= null;
 		final Node enclosing= fAnalyzer.getEnclosingNode();
 		if (enclosing instanceof MethodDeclaration) {
 			final MethodDeclaration node= (MethodDeclaration) enclosing;
 			method= node.resolveBinding();
 		}*/
 		final int flags= RefactoringDescriptor.STRUCTURAL_CHANGE | ScriptRefactoringDescriptor.ARCHIVE_REFACTORABLE | ScriptRefactoringDescriptor.ARCHIVE_IMPORTABLE;
 		final String description= Messages.format(RefactoringCoreMessages.ExtractMethodRefactoring_descriptor_description_short, fMethodName);
 		//final String label= method != null ? BindingLabelProvider.getBindingLabel(method, ScriptElementLabels.ALL_FULLY_QUALIFIED) : '{' + ScriptElementLabels.ELLIPSIS_STRING + '}';
 		//final String header= Messages.format(RefactoringCoreMessages.ExtractMethodRefactoring_descriptor_description, new String[] { BasicElementLabels.getScriptElementName(getSignature()), label, BindingLabelProvider.getBindingLabel(type, ScriptElementLabels.ALL_FULLY_QUALIFIED)});
 		//final ScriptRefactoringDescriptorComment comment= new ScriptRefactoringDescriptorComment(project, this, header);
 		//comment.addSetting(Messages.format(RefactoringCoreMessages.ExtractMethodRefactoring_name_pattern, BasicElementLabels.getScriptElementName(fMethodName)));
 		//comment.addSetting(Messages.format(RefactoringCoreMessages.ExtractMethodRefactoring_destination_pattern, BindingLabelProvider.getBindingLabel(type, ScriptElementLabels.ALL_FULLY_QUALIFIED)));
 		//String visibility= JdtFlags.getVisibilityString(fVisibility);
 		//if ("".equals(visibility)) //$NON-NLS-1$
 		//	visibility= RefactoringCoreMessages.ExtractMethodRefactoring_default_visibility;
 		//comment.addSetting(Messages.format(RefactoringCoreMessages.ExtractMethodRefactoring_visibility_pattern, visibility));
 		//if (fThrowRuntimeExceptions)
 		//	comment.addSetting(RefactoringCoreMessages.ExtractMethodRefactoring_declare_thrown_exceptions);
 		//if (fReplaceDuplicates)
 		//	comment.addSetting(RefactoringCoreMessages.ExtractMethodRefactoring_replace_occurrences);
 		//if (fGenerateScriptdoc)
 		//	comment.addSetting(RefactoringCoreMessages.ExtractMethodRefactoring_generate_comment);
 		final ExtractMethodDescriptor descriptor= new ExtractMethodDescriptor(project, description, ""/*comment.asString()*/, arguments, flags);
 		arguments.put(ScriptRefactoringDescriptor.ATTRIBUTE_INPUT, ScriptRefactoringDescriptor.elementToHandle(project, fCUnit));
 		arguments.put(ScriptRefactoringDescriptor.ATTRIBUTE_NAME, fMethodName);
 		arguments.put(ScriptRefactoringDescriptor.ATTRIBUTE_SELECTION, new Integer(fSelectionStart).toString() + " " + new Integer(fSelectionLength).toString()); //$NON-NLS-1$
 		//arguments.put(ATTRIBUTE_VISIBILITY, new Integer(fVisibility).toString());
 		arguments.put(ATTRIBUTE_DESTINATION, new Integer(fDestinationIndex).toString());
 		//arguments.put(ATTRIBUTE_EXCEPTIONS, Boolean.valueOf(fThrowRuntimeExceptions).toString());
 		//arguments.put(ATTRIBUTE_COMMENTS, Boolean.valueOf(fGenerateScriptdoc).toString());
 		//arguments.put(ATTRIBUTE_REPLACE, Boolean.valueOf(fReplaceDuplicates).toString());
 		return descriptor;
 	}
 
 	/**
 	 * Returns the signature of the new method.
 	 *
 	 * @return the signature of the extracted method
 	 */
 	public String getSignature() {
 		return getSignature(fMethodName);
 	}
 
 	/**
 	 * Returns the signature of the new method.
 	 *
 	 * @param methodName the method name used for the new method
 	 * @return the signature of the extracted method
 	 */
 	public String getSignature(String methodName) {
 		FunctionExpression methodDecl= createNewMethodDeclaration();
 		String str = new Generator(null,"",0,"").generate(methodDecl).toString();
 		return str.substring(0, str.indexOf('{')-1);
 	}
 
 	/*
 	 * Returns the number of duplicate code snippets found.
 	 *
 	 * @return the number of duplicate code fragments
 	 *
 	public int getNumberOfDuplicates() {
 		if (fDuplicates == null)
 			return 0;
 		int result=0;
 		for (int i= 0; i < fDuplicates.length; i++) {
 			if (!fDuplicates[i].isMethodBody())
 				result++;
 		}
 		return result;
 	}
 
 	public boolean getReplaceDuplicates() {
 		return fReplaceDuplicates;
 	}
 
 	public void setReplaceDuplicates(boolean replace) {
 		fReplaceDuplicates= replace;
 	}
 
 	public void setGenerateScriptdoc(boolean generate) {
 		fGenerateScriptdoc= generate;
 	}
 
 	public boolean getGenerateScriptdoc() {
 		return fGenerateScriptdoc;
 	}*/
 
 	//---- Helper methods ------------------------------------------------------------------------
 
 	private void initializeParameterInfos() {
 		VariableBinding[] arguments= fAnalyzer.getArguments();
 		fParameterInfos= new ArrayList<ParameterInfo>(arguments.length);
 		for (int i= 0; i < arguments.length; i++) {
 			String argument = arguments[i].getName();
 			String type = arguments[i].getTypeName();
 			if (type == null)
 				type = "";
 			fParameterInfos.add(new ParameterInfo(type, argument, i));
 		}
 	}
 
 	private void initializeUsedNames() {
 		// TODO might need more thinking
 		fUsedNames = VariableLookup.getVisibleNames(fAnalyzer.getSelectedNodes()[0]);
 		//fUsedNames= UsedNamesCollector.perform(fAnalyzer.getSelectedNodes());
 		for (ParameterInfo parameter : fParameterInfos) {
 			fUsedNames.remove(parameter.getOldName());
 		}
 	}
 
 	/*private void initializeDuplicates() {
 		ASTNode start= fAnalyzer.getEnclosingBodyDeclaration();
 		while (!(start instanceof AbstractTypeDeclaration)) {
 			start= start.getParent();
 		}
 
 		fDuplicates= SnippetFinder.perform(start, fAnalyzer.getSelectedNodes());
 		fReplaceDuplicates= fDuplicates.length > 0 && ! fAnalyzer.isLiteralNodeSelected();
 	}*/
 
 	private void initializeDestinations() {
 		List<Node> result= new ArrayList<Node>();
 		Node node= fAnalyzer.getEnclosingNode();
 		Node parent = node == null ? null : (Node)node.eContainer();
 		Node grandparent = parent == null ? null : (Node)parent.eContainer();
 		if (node instanceof Source) {
 			Node cur = fAnalyzer.getSelectedNodes()[0];
 			while (cur.eContainer() != node)
 				cur = (Node)cur.eContainer();
 			result.add(cur);
 		} else {
 			while (node != null) {
 				if (parent instanceof Source) break;
 				boolean stop = false;
 				switch(grandparent.eClass().getClassifierID()) {
 				case DomPackage.GETTER_ASSIGNMENT:
 				case DomPackage.SETTER_ASSIGNMENT:
 				case DomPackage.FUNCTION_EXPRESSION:
 					stop = true;
 				}
 				if (stop) break;
 				node = parent;
 				parent = grandparent;
 				grandparent = parent == null ? null : (Node)parent.eContainer();
 			}
 			if (node != null) result.add(node);
 		}
 		// TODO understand what we need here or we can just move manually after refactoring
 		/*if (decl instanceof FunctionExpression) {
 			//ITypeBinding binding= ASTNodes.getEnclosingType(current);
 			Node next= getNextParent(current);
 			while (next != null && binding != null && binding.isNested()) {
 				result.add(next);
 				current= next;
 				binding= ASTNodes.getEnclosingType(current);
 				next= getNextParent(next);
 			}
 		}*/
 		fDestinations= (Node[])result.toArray(new Node[result.size()]);
 		fDestination= fDestinations[fDestinationIndex];
 	}
 
 	private FunctionExpression createNewMethodDeclaration() {
 		FunctionExpression result= DomFactory.eINSTANCE.createFunctionExpression();
 
 		//int modifiers= fVisibility;
 		/*Node enclosingBodyDeclaration= fAnalyzer.getEnclosingNode();
 		while (enclosingBodyDeclaration != null && enclosingBodyDeclaration.eContainer() != fDestination) {
 			enclosingBodyDeclaration= (Node)enclosingBodyDeclaration.eContainer();
 		}
 		if (enclosingBodyDeclaration instanceof BodyDeclaration) { // should always be the case
 			int enclosingModifiers= ((BodyDeclaration)enclosingBodyDeclaration).getModifiers();
 			boolean shouldBeStatic= Modifier.isStatic(enclosingModifiers) || fAnalyzer.getForceStatic();
 			if (shouldBeStatic) {
 				modifiers|= Modifier.STATIC;
 			}
 		}
 
 		ITypeBinding[] typeVariables= computeLocalTypeVariables();
 		List typeParameters= result.typeParameters();
 		for (int i= 0; i < typeVariables.length; i++) {
 			TypeParameter parameter= fAST.newTypeParameter();
 			parameter.setName(fAST.newSimpleName(typeVariables[i].getName()));
 			typeParameters.add(parameter);
 		}
 
 		result.modifiers().addAll(ASTNodeFactory.newModifiers(fAST, modifiers));
 		result.setReturnType2((Type)ASTNode.copySubtree(fAST, fAnalyzer.getReturnType()));*/
 		if (fAnalyzer.getReturnTypeName() != null) {
 			Type type = DomFactory.eINSTANCE.createType();
 			type.setName(fAnalyzer.getReturnTypeName());
 			result.setReturnType(type);
 		}
 		Identifier id = DomFactory.eINSTANCE.createIdentifier();
 		id.setName(fMethodName);
 		result.setIdentifier(id);
 
 		List<Parameter> parameters= result.getParameters();
 		for (int i= 0; i < fParameterInfos.size(); i++) {
 			ParameterInfo info= (ParameterInfo)fParameterInfos.get(i);
 			Parameter parameter= DomFactory.eINSTANCE.createParameter();
 			Identifier prmName = DomFactory.eINSTANCE.createIdentifier();
 			prmName.setName(info.getNewName());
 			parameter.setName(prmName);
 			if (!"".equals(info.getNewTypeName())) { //$NON-NLS-1
 				Type type = DomFactory.eINSTANCE.createType();
 				type.setName(info.getNewTypeName());
 				parameter.setType(type);
 			}
 			parameters.add(parameter);
 		}
 
 		/*List exceptions= result.thrownExceptions();
 		ITypeBinding[] exceptionTypes= fAnalyzer.getExceptions(fThrowRuntimeExceptions);
 		ImportRewriteContext context= new ContextSensitiveImportRewriteContext(enclosingBodyDeclaration, fImportRewriter);
 		for (int i= 0; i < exceptionTypes.length; i++) {
 			ITypeBinding exceptionType= exceptionTypes[i];
 			exceptions.add(ASTNodeFactory.newName(fAST, fImportRewriter.addImport(exceptionType, context)));
 		}*/
 		result.setBody(DomFactory.eINSTANCE.createBlockStatement());
 		return result;
 	}
 
 	/*private ITypeBinding[] computeLocalTypeVariables() {
 		List result= new ArrayList(Arrays.asList(fAnalyzer.getTypeVariables()));
 		for (int i= 0; i < fParameterInfos.size(); i++) {
 			ParameterInfo info= (ParameterInfo)fParameterInfos.get(i);
 			processVariable(result, info.getOldBinding());
 		}
 		IVariableBinding[] methodLocals= fAnalyzer.getMethodLocals();
 		for (int i= 0; i < methodLocals.length; i++) {
 			processVariable(result, methodLocals[i]);
 		}
 		return (ITypeBinding[])result.toArray(new ITypeBinding[result.size()]);
 	}
 
 	private void processVariable(List result, IVariableBinding variable) {
 		if (variable == null)
 			return;
 		ITypeBinding binding= variable.getType();
 		if (binding != null && binding.isParameterizedType()) {
 			ITypeBinding[] typeArgs= binding.getTypeArguments();
 			for (int args= 0; args < typeArgs.length; args++) {
 				ITypeBinding arg= typeArgs[args];
 				if (arg.isTypeVariable() && !result.contains(arg)) {
 					ASTNode decl= fRoot.findDeclaringNode(arg);
 					if (decl != null && decl.getParent() instanceof MethodDeclaration) {
 						result.add(arg);
 					}
 				}
 			}
 		}
 	}*/
 
 	/*private String getName(IVariableBinding binding) {
 		for (Iterator iter= fParameterInfos.iterator(); iter.hasNext();) {
 			ParameterInfo info= (ParameterInfo)iter.next();
 			if (Bindings.equals(binding, info.getOldBinding())) {
 				return info.getNewName();
 			}
 		}
 		return binding.getName();
 	}
 
 	private VariableDeclaration getVariableDeclaration(ParameterInfo parameter) {
 		return ASTNodes.findVariableDeclaration(parameter.getOldBinding(), fAnalyzer.getEnclosingBodyDeclaration());
 	}*/
 
 	private VariableStatement createDeclaration(VariableBinding name, Expression initializer) {
 		Identifier id = DomFactory.eINSTANCE.createIdentifier();
 		id.setName(name.getName());
 		VariableDeclaration decl = DomFactory.eINSTANCE.createVariableDeclaration();
 		decl.setIdentifier(id);
 		if (name.getTypeName() != null) {
 			Type type = DomFactory.eINSTANCE.createType();
 			type.setName(name.getTypeName());
 		}
 		if (initializer != null)
 			decl.setInitializer(initializer);
 		VariableStatement result = DomFactory.eINSTANCE.createVariableStatement();
 		result.getDeclarations().add(decl);
 		return result;
 	}
 
 	/*public ISourceModule getSource() {
 		return fCUnit;
 	}
 
 	private RefactoringStatus initialize(ScriptRefactoringArguments arguments) {
 		final String selection= arguments.getAttribute(ScriptRefactoringDescriptorUtil.ATTRIBUTE_SELECTION);
 		if (selection == null)
 			return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, ScriptRefactoringDescriptorUtil.ATTRIBUTE_SELECTION));
 
 		int offset= -1;
 		int length= -1;
 		final StringTokenizer tokenizer= new StringTokenizer(selection);
 		if (tokenizer.hasMoreTokens())
 			offset= Integer.valueOf(tokenizer.nextToken()).intValue();
 		if (tokenizer.hasMoreTokens())
 			length= Integer.valueOf(tokenizer.nextToken()).intValue();
 		if (offset < 0 || length < 0)
 			return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.InitializableRefactoring_illegal_argument, new Object[] { selection, ScriptRefactoringDescriptorUtil.ATTRIBUTE_SELECTION}));
 
 		fSelectionStart= offset;
 		fSelectionLength= length;
 
 		final String handle= arguments.getAttribute(ScriptRefactoringDescriptorUtil.ATTRIBUTE_INPUT);
 		if (handle == null)
 			return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, ScriptRefactoringDescriptorUtil.ATTRIBUTE_INPUT));
 
 		IScriptElement element= ScriptRefactoringDescriptorUtil.handleToElement(arguments.getProject(), handle, false);
 		if (element == null || !element.exists() || element.getElementType() != IScriptElement.COMPILATION_UNIT)
 			return ScriptRefactoringDescriptorUtil.createInputFatalStatus(element, getName(), IScriptRefactorings.EXTRACT_METHOD);
 
 		fCUnit= (ISourceModule) element;
 		final String visibility= arguments.getAttribute(ATTRIBUTE_VISIBILITY);
 		if (visibility != null && visibility.length() != 0) {
 			int flag= 0;
 			try {
 				flag= Integer.parseInt(visibility);
 			} catch (NumberFormatException exception) {
 				return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, ATTRIBUTE_VISIBILITY));
 			}
 			fVisibility= flag;
 		}
 		final String name= arguments.getAttribute(ScriptRefactoringDescriptorUtil.ATTRIBUTE_NAME);
 		if (name == null || name.length() == 0)
 			return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, ScriptRefactoringDescriptorUtil.ATTRIBUTE_NAME));
 
 		fMethodName= name;
 
 		final String destination= arguments.getAttribute(ATTRIBUTE_DESTINATION);
 		if (destination != null && destination.length() == 0) {
 			int index= 0;
 			try {
 				index= Integer.parseInt(destination);
 			} catch (NumberFormatException exception) {
 				return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, ATTRIBUTE_DESTINATION));
 			}
 			fDestinationIndex= index;
 		}
 		final String replace= arguments.getAttribute(ATTRIBUTE_REPLACE);
 		if (replace == null)
 			return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, ATTRIBUTE_REPLACE));
 
 		fReplaceDuplicates= Boolean.valueOf(replace).booleanValue();
 
 		final String comments= arguments.getAttribute(ATTRIBUTE_COMMENTS);
 		if (comments == null)
 			return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, ATTRIBUTE_COMMENTS));
 
 		fGenerateScriptdoc= Boolean.valueOf(comments).booleanValue();
 
 		final String exceptions= arguments.getAttribute(ATTRIBUTE_EXCEPTIONS);
 		if (exceptions == null)
 			return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, ATTRIBUTE_EXCEPTIONS));
 
 		fThrowRuntimeExceptions= Boolean.valueOf(exceptions).booleanValue();
 
 		return new RefactoringStatus();
 	}*/
 }
