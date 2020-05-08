 /*******************************************************************************
  * Copyright (c) 2011 University of Illinois at Urbana-Champaign
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *  
  * Contributors: 
  * University of Illinois at Urbana-Champaign - initial API and implementation
  *******************************************************************************/
 package edu.illinois.parallelism.openmp.refactoring.padscalarvariables;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
 import org.eclipse.cdt.core.dom.ast.ASTVisitor;
 import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
 import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
 import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
 import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
 import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
 import org.eclipse.cdt.core.dom.ast.IASTName;
 import org.eclipse.cdt.core.dom.ast.IASTNode;
 import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
 import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
 import org.eclipse.cdt.core.dom.ast.IBinding;
 import org.eclipse.cdt.core.dom.ast.INodeFactory;
 import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
 import org.eclipse.cdt.core.model.ICElement;
 import org.eclipse.cdt.core.model.ICProject;
 import org.eclipse.cdt.internal.core.dom.parser.c.CVariable;
 import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
 import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
 import org.eclipse.ltk.core.refactoring.RefactoringStatus;
 import org.eclipse.ptp.pldt.openmp.analysis.OpenMPAnalysisManager;
 import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTOMPPragma;
 import org.eclipse.text.edits.TextEditGroup;
 
 /**
  * 
  * @author nchen
  * 
  */
 @SuppressWarnings("restriction")
 public class PadScalarVariablesRefactoring extends CRefactoring {
 
 	private OpenMPAnalysisManager ompManager;
 	private IASTNode nodeToRefactor;
 	private int bytesToPad = 8;
 	private List<VariableToPadTuple> variablesToPad;
 
 	public List<VariableToPadTuple> getVariablesToPad() {
 		return variablesToPad;
 	}
 
 	public void setBytesToPad(int bytesToPad) {
 		this.bytesToPad = bytesToPad;
 	}
 
 	public int getBytesToPad() {
 		return bytesToPad;
 	}
 
 	public PadScalarVariablesRefactoring(IFile file, ISelection selection, ICElement element, ICProject proj) {
 		// Parameter element is always null for this refactoring since it
 		// depends on the position of cursor not the selected element
 		super(file, selection, element, proj);
 		name = Messages.PadScalarVariableRefactoring_humanName;
 		setRegionToCursorPositionOnly();
 	}
 
 	public boolean isWithinProperOMPRegion() {
 		loadTranslationUnit(initStatus, new NullProgressMonitor());
 		ompManager = new OpenMPAnalysisManager(unit, file);
 
 		nodeToRefactor = locateMinimumBoundingPragmaRegion();
 		if (nodeToRefactor != null) {
 			System.err.println(nodeToRefactor);
 			System.err.println("Start line :" + nodeToRefactor.getFileLocation().getStartingLineNumber());
 			System.err.println("End line :" + nodeToRefactor.getFileLocation().getEndingLineNumber());
 		}
 
 		return (nodeToRefactor != null);
 	}
 
 	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
 		SubMonitor sm = SubMonitor.convert(pm, 10);
 
 		RefactoringStatus status = super.checkInitialConditions(sm.newChild(6));
 		if (status.hasError())
 			return status;
 
 		Set<IASTName> potentialVariablesToPad = initRefactoring();
 		System.err.println("Potential variables to pad: " + potentialVariablesToPad);
 		potentialVariablesToPad = filterVariablesToPad(potentialVariablesToPad);
 		System.err.println("Filtered variables to pad: " + potentialVariablesToPad);
 		variablesToPad = createVariableTuple(potentialVariablesToPad);
 
 		return status;
 	}
 
 	@Override
 	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector) throws CoreException, OperationCanceledException {
 		try {
 			lockIndex();
 			try {
 				TextEditGroup editGroup = new TextEditGroup("Variables to Pad");
 				INodeFactory factory = unit.getASTNodeFactory();
 
 				for (VariableToPadTuple variable : variablesToPad) {
 					if (variable.shouldPad) {
 						String paddingVariableName = new String(variable.getName().toString() + "_padding");
 						IASTName name = factory.newName(paddingVariableName.toCharArray());
 						IASTArrayModifier arrayModifier = factory.newArrayModifier(factory.newLiteralExpression(IASTLiteralExpression.lk_integer_constant, new Integer(bytesToPad).toString()));
 						IASTArrayDeclarator newArrayDeclarator = factory.newArrayDeclarator(name);
 						newArrayDeclarator.addArrayModifier(arrayModifier);
 						IASTSimpleDeclSpecifier newSimpleDeclSpecifier = factory.newSimpleDeclSpecifier();
 						newSimpleDeclSpecifier.setType(Kind.eChar);
 						IASTSimpleDeclaration newSimpleDeclaration = factory.newSimpleDeclaration(newSimpleDeclSpecifier);
 						newSimpleDeclaration.addDeclarator(newArrayDeclarator);
 						IASTDeclarationStatement newDeclarationStatement = factory.newDeclarationStatement(newSimpleDeclaration);
 
 						ASTRewrite rewriter = collector.rewriterForTranslationUnit(unit);
 						IASTName variableToPadNode = variable.getName();
 						IASTNode parent = getVariableDeclaringBlock(variableToPadNode);
 						IASTNode insertionPoint = promoteUntilSubchildLevel(variableToPadNode, parent);
 
 						rewriter.insertBefore(parent, insertionPoint, newDeclarationStatement, editGroup);
 					}
 				}
 
 			} finally {
 				unlockIndex();
 			}
 		} catch (InterruptedException e) {
 			Thread.currentThread().interrupt();
 		}
 	}
 
 	@Override
 	protected RefactoringDescriptor getRefactoringDescriptor() {
 		// TODO Create a refactoring descriptor for this refactoring
 		return null;
 	}
 
 	// TODO: We might be able get rid of this if we store
 	// IASTDeclarationStatement instead of IASTName
 	// Traverses up the hierarchy of parent nodes for "node" until the "parent"
 	// is the parent
 	private IASTNode promoteUntilSubchildLevel(IASTName node, IASTNode parent) {
 		IASTNode candidate = node;
 		while (!(candidate.getParent() == parent)) {
 			candidate = candidate.getParent();
 			if (candidate == null)
 				break;
 		}
 		return candidate;
 	}
 
 	private IASTNode getVariableDeclaringBlock(IASTName variableToPadNode) {
 		IASTNode candidate = variableToPadNode;
 		while (!(candidate instanceof IASTCompoundStatement)) {
 			candidate = candidate.getParent();
 			if (candidate == null)
 				break;
 		}
 		return candidate;
 	}
 
 	// TODO: What happens in the following cases?
 	// * The variable is extern
 	// * The variable is a local variable
 	// Is there a reliable way to use IASTName[] declarationsInAST =
 	// unit.getDeclarationsInAST(binding);
 	@SuppressWarnings("unchecked")
 	private Set<IASTName> initRefactoring() {
 		Set<IASTName> possibleCandidatesWithDuplicates = locateVariableReferencesInRegion();
 		Set<IASTName> candidatesWithoutDuplicates = new HashSet<IASTName>();
 
 		for (IASTName name : possibleCandidatesWithDuplicates) {
 			IBinding binding = name.resolveBinding();
 			if (binding instanceof CVariable) {
 				CVariable cVariable = (CVariable) binding;
 				IASTNode[] declarations = cVariable.getDeclarations();
 				System.err.println("The set of IASTNode[]s for " + binding);
 				System.err.println(declarations);
 				candidatesWithoutDuplicates.addAll((Collection<? extends IASTName>) Arrays.asList(declarations));
 			}
 		}
 
 		return candidatesWithoutDuplicates;
 	}
 
 	private Set<IASTName> locateVariableReferencesInRegion() {
 		final Set<IASTName> variablesUsed = new HashSet<IASTName>();
 
 		nodeToRefactor.accept(new ASTVisitor() {
 
 			{
 				shouldVisitNames = true;
 			}
 
 			@Override
 			public int visit(IASTName name) {
 				variablesUsed.add(name);
 				return super.visit(name);
 			}
 
 		});
 
 		return variablesUsed;
 	}
 
 	// We are only interested in the cursor position and not the selection
 	private void setRegionToCursorPositionOnly() {
 		region = new Region(region.getOffset(), 0);
 	}
 
 	private IASTNode locateMinimumBoundingPragmaRegion() {
 		PASTOMPPragma[] ompPragmas = ompManager.getOMPPragmas();
 		PASTOMPPragma candidate = null;
 		for (PASTOMPPragma ompPragma : ompPragmas) {
 			if (isRefactoringSelectionWithinRegion(ompPragma)) {
 				if (candidate == null) // First candidate
 					candidate = ompPragma;
 				else if (hasTighterBoundThan(ompPragma, candidate)) {
 					candidate = ompPragma;
 				}
 			}
 		}
 		if (candidate != null)
 			return candidate.getRegion();
 		return null;
 	}
 
 	// Evaluates if newCandidate has a tighter bound on the region than
 	// currentCandidate. All ompPragma regions are nested must be PROPER SUBSETS
 	// of one another if there are nested so this is the only case we consider
 	// here.
 	private boolean hasTighterBoundThan(PASTOMPPragma newCandidate, PASTOMPPragma currendCandidate) {
 		return (newCandidate.getRegionOffset() > currendCandidate.getRegionOffset());
 	}
 
 	private boolean isRefactoringSelectionWithinRegion(PASTOMPPragma ompPragma) {
 		return (selectionIsAfterOMPPragmaStart(ompPragma)) && (selectionIsBeforeOMPPragmaEnd(ompPragma));
 	}
 
 	private boolean selectionIsBeforeOMPPragmaEnd(PASTOMPPragma ompPragma) {
 		return region.getOffset() <= (ompPragma.getRegionOffset() + ompPragma.getRegionLength());
 	}
 
 	private boolean selectionIsAfterOMPPragmaStart(PASTOMPPragma ompPragma) {
 		return region.getOffset() >= ompPragma.getRegionOffset();
 	}
 
 	private List<VariableToPadTuple> createVariableTuple(Set<IASTName> variablesToPad) {
 		List<VariableToPadTuple> variables = new ArrayList<VariableToPadTuple>();
 		for (IASTName name : variablesToPad) {
 			variables.add(new VariableToPadTuple(name));
 		}
 		return variables;
 	}
 
 	private Set<IASTName> filterVariablesToPad(Set<IASTName> variablesToPad) {
 		Set<IASTName> filteredVariables = new HashSet<IASTName>(variablesToPad);
 		for (IASTNode variable : variablesToPad) {
 			if (!isValidTypeToPad(variable))
 				filteredVariables.remove(variable);
 		}
 		return filteredVariables;
 	}
 
 	// TODO: Change this to be more robust
 	// What other types can we support? int, float, etc? How to determine the
 	// size?
 	private boolean isValidTypeToPad(IASTNode variable) {
 		String nodeType = ASTTypeUtil.getNodeType(variable);
 		return nodeType.matches("int|char");
 	}
 
 	class VariableToPadTuple {
 		IASTName name;
 		boolean shouldPad;
 
 		public VariableToPadTuple(IASTName _name) {
 			name = _name;
 			shouldPad = false;
 		}
 
 		public IASTName getName() {
 			return name;
 		}
 
 		public void setShouldPad(boolean value) {
 			shouldPad = value;
 		}
 
 		public String toString() {
 			return new String(name.getSimpleID());
 		}
 
 	}
 
 }
