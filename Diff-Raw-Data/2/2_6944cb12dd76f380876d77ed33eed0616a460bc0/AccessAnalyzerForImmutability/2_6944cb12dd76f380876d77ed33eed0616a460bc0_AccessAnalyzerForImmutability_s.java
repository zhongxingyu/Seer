 package edu.uiuc.immutability;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.Flags;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IField;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.CharacterLiteral;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.eclipse.jdt.core.dom.PrimitiveType;
 import org.eclipse.jdt.core.dom.SimpleType;
 import org.eclipse.jdt.core.dom.Type;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
 import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
 import org.eclipse.jdt.core.search.IJavaSearchConstants;
 import org.eclipse.jdt.core.search.IJavaSearchScope;
 import org.eclipse.jdt.core.search.SearchEngine;
 import org.eclipse.jdt.core.search.SearchMatch;
 import org.eclipse.jdt.core.search.SearchParticipant;
 import org.eclipse.jdt.core.search.SearchPattern;
 import org.eclipse.jdt.core.search.SearchRequestor;
 import org.eclipse.jdt.internal.corext.dom.ASTNodes;
 import org.eclipse.jdt.internal.corext.dom.Bindings;
 import org.eclipse.jdt.internal.corext.dom.ModifierRewrite;
 import org.eclipse.ltk.core.refactoring.RefactoringStatus;
 import org.eclipse.text.edits.TextEditGroup;
 
 @SuppressWarnings("restriction")
 public class AccessAnalyzerForImmutability extends ASTVisitor {
 
 	private RefactoringStatus status;
 	private List<TextEditGroup> groupDescriptions;
 	private final MakeImmutableRefactoring refactoring;
 	private final ASTRewrite rewriter;
 	private final AST astRoot;
 	private final ICompilationUnit unit;
 	
 	public AccessAnalyzerForImmutability(
 			MakeImmutableRefactoring makeImmutableRefactoring,
 			ICompilationUnit unit, ASTRewrite rewriter) {
 		this.refactoring = makeImmutableRefactoring;
 		this.rewriter = rewriter;
 		this.astRoot = rewriter.getAST();
 		this.unit = unit;
 		status = new RefactoringStatus();
 		groupDescriptions = new ArrayList<TextEditGroup>();
 	}
 	
 	@Override
 	public boolean visit(FieldDeclaration fieldDecl) {
 		if (doesParentBindToTargetClass(fieldDecl)) {
 			
 			// Change modifier to final
 			if (!Flags.isFinal(fieldDecl.getModifiers())) {
 				int finalModifiers = fieldDecl.getModifiers() | ModifierKeyword.FINAL_KEYWORD.toFlagValue();
 				TextEditGroup gd = new TextEditGroup("change to final");
 				ModifierRewrite.create(rewriter, fieldDecl).setModifiers(finalModifiers, gd);
 				groupDescriptions.add(gd);
 			}
 			
 			// Add initializers to the fragments that do not have them as they are required for final variables
 			Type fieldDeclType = fieldDecl.getType(); 
 			List fragments = fieldDecl.fragments();
 			for (Object obj : fragments) {
 				VariableDeclarationFragment frag = (VariableDeclarationFragment)obj;
 				if (frag != null && frag.getInitializer() == null) {
 					VariableDeclarationFragment newFrag =
 							(VariableDeclarationFragment)ASTNode.copySubtree(frag.getAST(), frag);
 					
 					// Check whether the field is already initialized in a constructor in which case we can't
 					// initialize it a second time at the declaration point
 					if ( !isFieldInitializedInConstructor(frag) ) {
 
 						// Add initializer
 						Expression initializer = null;
 						if (fieldDeclType instanceof PrimitiveType) {
 							PrimitiveType primType = (PrimitiveType)fieldDeclType;
 							
 							if (primType.getPrimitiveTypeCode() == PrimitiveType.BOOLEAN) {
 								initializer = newFrag.getAST().newBooleanLiteral(false);
 							}
 							else if (primType.getPrimitiveTypeCode() == PrimitiveType.CHAR) {
 								CharacterLiteral charLit = newFrag.getAST().newCharacterLiteral(); 
 								charLit.setCharValue('\u0000');
 									
 								initializer = charLit;
 							}
 							else if (primType.getPrimitiveTypeCode() == PrimitiveType.FLOAT) {
 								initializer = newFrag.getAST().newNumberLiteral("0.0f");
 							}
 							else if (primType.getPrimitiveTypeCode() == PrimitiveType.DOUBLE) {
 								initializer = newFrag.getAST().newNumberLiteral("0.0d");
 							}
 							else if (   primType.getPrimitiveTypeCode() == PrimitiveType.INT
 							         || primType.getPrimitiveTypeCode() == PrimitiveType.SHORT
 							         || primType.getPrimitiveTypeCode() == PrimitiveType.BYTE) {
 						 		initializer = newFrag.getAST().newNumberLiteral("0");
 							}
 							else if (primType.getPrimitiveTypeCode() == PrimitiveType.LONG) {
 								initializer = newFrag.getAST().newNumberLiteral("0L");
 							}
 							else {
 								continue; // TODO add assertion as this should not happen
 							}
 						}
 						else if (fieldDeclType instanceof SimpleType) {
 							SimpleType simpType = (SimpleType)fieldDeclType;
 							assert simpType != null;
 							
 							// NOTE Not sure whether it is a good idea to handle strings unlike other objects
 							initializer = (simpType.getName().toString().equals("String")) 
 							            ? newFrag.getAST().newStringLiteral()
 							            : newFrag.getAST().newNullLiteral();
 						}
 						else {
 							continue; // TODO: not supported failure
 						}
 						
 						assert initializer != null;
 						
 						newFrag.setInitializer(initializer);
 					
 						TextEditGroup gd = new TextEditGroup("add initializer");
 						rewriter.replace(frag, newFrag, gd);
 						groupDescriptions.add(gd);
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private boolean isFieldInitializedInConstructor(VariableDeclarationFragment frag) {
 		
 		// Get the class of the variable (The grandparent of a field/fragment is always its class)
 		TypeDeclaration parentClass = (TypeDeclaration)frag.getParent().getParent();
 		assert parentClass.isInterface() == false; //Interfaces can't have non-static fields
 		
 		// Go through all the constructors and check whether they initialize the variable
 		IType classTypeId = unit.getType(parentClass.getName().toString());
 		IField field = classTypeId.getField(frag.getName().toString());
 		
 		IMethod[] methods;
 		try {
 			methods = classTypeId.getMethods();						
 			for (IMethod method : methods) {
 				if (method.isConstructor()) {
 					SearchPattern pattern = SearchPattern.createPattern(field, IJavaSearchConstants.WRITE_ACCESSES);
 					SearchEngine engine = new SearchEngine();
 					SearchParticipant[] participants = new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() };
 					IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { method });
 					
 					final List matches = new ArrayList();
 					SearchRequestor requestor = new SearchRequestor() {
 						public void acceptSearchMatch(SearchMatch match) {
 							matches.add(match);
 						}
 					};
 					
 					engine.search(pattern, participants, scope, requestor, null);
 					if (!matches.isEmpty()) {
 						// Skip over the add initializer step as this variable is already initialized
 						// in a constructor
 						return true;
 					}
 				}
 			}
 		} catch (JavaModelException e) {
 			// TODO: Trigger error
 		}
 		catch (CoreException e) {
 			// TODO: Trigger error
 		}
 		
 		return false;
 	}
 	
 	private boolean doesParentBindToTargetClass(FieldDeclaration fieldDecl) {
 		ASTNode parent = ASTNodes.getParent(fieldDecl, TypeDeclaration.class);
 		if (parent != null) {
 			TypeDeclaration typeDecl = (TypeDeclaration) parent;
 			return Bindings.equals(typeDecl.resolveBinding(), refactoring.getTargetBinding());
 		}
 		return false;
 	}
 	
 
 	public RefactoringStatus getStatus() {
 		// TODO Auto-generated method stub
 		return status;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public Collection getGroupDescriptions() {
 		return groupDescriptions;
 	}
 
 }
 
 class A {
 	int i = 10, j = 12;
 }
