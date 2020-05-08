 package org.eclipse.dltk.ruby.internal.core.codeassist;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.declarations.TypeDeclaration;
 import org.eclipse.dltk.ast.expressions.CallExpression;
 import org.eclipse.dltk.ast.expressions.Expression;
 import org.eclipse.dltk.ast.references.ConstantReference;
 import org.eclipse.dltk.ast.references.SimpleReference;
 import org.eclipse.dltk.ast.statements.Block;
 import org.eclipse.dltk.ast.statements.Statement;
 import org.eclipse.dltk.codeassist.IAssistParser;
 import org.eclipse.dltk.codeassist.ISelectionEngine;
 import org.eclipse.dltk.compiler.env.ISourceModule;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKModelUtil;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISearchableEnvironment;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.internal.codeassist.impl.Engine;
 import org.eclipse.dltk.internal.compiler.lookup.LookupEnvironment;
 import org.eclipse.dltk.ruby.ast.ColonExpression;
 import org.eclipse.dltk.ruby.core.ParentshipBuildingVisitor;
 import org.eclipse.dltk.ruby.core.utils.RubySyntaxUtils;
 import org.eclipse.dltk.ruby.typeinference.RubyTypeUtils;
 import org.eclipse.dltk.ruby.typeinference.internal.IContext;
 import org.eclipse.dltk.ruby.typeinference.internal.RubyTypeModel;
 import org.eclipse.dltk.ruby.typemodel.classes.RubyMetaTypeDescriptor;
 import org.eclipse.dltk.typeinference.ASTCaching;
 import org.eclipse.dltk.typeinference.AnyTypeDescriptor;
 import org.eclipse.dltk.typeinference.IClassLikeFragment;
 import org.eclipse.dltk.typeinference.IKnownTypeDescriptor;
 import org.eclipse.dltk.typeinference.IMethodDescriptor;
 import org.eclipse.dltk.typeinference.IScope;
 import org.eclipse.dltk.typeinference.ITypeDescriptor;
 import org.eclipse.dltk.typeinference.IUnit;
 import org.eclipse.dltk.typeinference.IVariableDescriptor;
 import org.eclipse.dltk.typeinference.UserMethodDescriptor;
 import org.eclipse.dltk.typeinference.UserTypeDescriptor;
 
 
 public class RubySelectionEngine extends Engine implements ISelectionEngine {
 	public static boolean DEBUG = DLTKCore.DEBUG_SELECTION;
 
 	protected int actualSelectionStart;
 
 	protected int actualSelectionEnd;
 
 	private List selectionElements = new ArrayList();
 		
 	private RubySelectionParser parser = new RubySelectionParser();
 	
 	private RubyTypeModel calculator = null;//new RubyTypeModel();
 
 	private ISourceModule sourceModule;
 
 	private IDLTKLanguageToolkit toolkit;
 
 	public RubySelectionEngine(ISearchableEnvironment environment, Map options,
 			IDLTKLanguageToolkit toolkit) {
 		super(options);
 		this.toolkit = toolkit;
 		this.nameEnvironment = environment;
 		this.lookupEnvironment = new LookupEnvironment(this, nameEnvironment);
 	}
 
 	public IAssistParser getParser() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	private String getTypeFQN (TypeDeclaration decl) {
 		String fqn = "";
 		while (decl != null) {
 			String curName = decl.getName();
 			if (fqn.length() > 0)
 				fqn = "::" + fqn;
 			fqn = curName + fqn;
 			if (curName.startsWith("::")) {
 				break;
 			}
 			decl = this.getEnclosingType2(decl);
 		} 
 		if (!fqn.startsWith("::"))
 			fqn = "::" + fqn;
 		return fqn;
 	}
 	
 	public IModelElement[] select(ISourceModule sourceUnit,
 			int selectionSourceStart, int selectionSourceEnd) {
 		sourceModule = (ISourceModule) sourceUnit
 				.getModelElement();
 		String source = sourceUnit.getSourceContents();
 		if (DEBUG) {
 			System.out.print("SELECTION IN "); //$NON-NLS-1$
 			System.out.print(sourceUnit.getFileName());
 			System.out.print(" FROM "); //$NON-NLS-1$
 			System.out.print(selectionSourceStart);
 			System.out.print(" TO "); //$NON-NLS-1$
 			System.out.println(selectionSourceEnd);
 			System.out.println("SELECTION - Source :"); //$NON-NLS-1$
 			System.out.println(source);
 		}
 		if (!checkSelection(source, selectionSourceStart, selectionSourceEnd)) {
 			return new IModelElement[0];
 		}		
 		actualSelectionEnd--; //inclusion fix
 		if (DEBUG) {
 			System.out.print("SELECTION - Checked : \""); //$NON-NLS-1$
 			System.out.print(source.substring(actualSelectionStart,
 					actualSelectionEnd + 1));
 			System.out.println('"');
 		}
 		
 		try {
 			ModuleDeclaration parsedUnit =	this.parser.parse(sourceUnit);
 
 			if (parsedUnit != null) {
 				if(DEBUG) {
 					System.out.println("SELECTION - AST :"); //$NON-NLS-1$
 					System.out.println(parsedUnit.toString());
 				}
 				
 				this.buildParentNodesMap(parsedUnit);
				try {
//					IUnit unit = calculator.getUnit((org.eclipse.dltk.core.ISourceModule) sourceModule);
//					unit.getASTNode(ASTCaching.REPARSE);
//					calculator.recalculate(unit);
				} catch (Throwable e) {
					if (DEBUG) {
						e.printStackTrace();
					}
					return null;
				}
				
 				ASTNode node = findMinimalNode(parsedUnit, actualSelectionStart, actualSelectionEnd);
 				
 				if (node == null)
 					return new IModelElement[0];
 				
 				org.eclipse.dltk.core.ISourceModule modelModule = (org.eclipse.dltk.core.ISourceModule) sourceModule
 						.getModelElement();
 				if (node instanceof TypeDeclaration) {
 					TypeDeclaration typeDeclaration = (TypeDeclaration) node;
 					selectionOnTypeDeclaration (parsedUnit, typeDeclaration);
 				} else if (node instanceof MethodDeclaration) {
 					MethodDeclaration methodDeclaration = (MethodDeclaration) node;
 					selectionOnMethodDeclaration(parsedUnit, methodDeclaration);
 				} else if (node instanceof ConstantReference) {
 					ConstantReference reference = (ConstantReference) node;
 					selectionOnConstant(modelModule, parsedUnit, reference);
 				} else if (node instanceof ColonExpression) {
 					ColonExpression colonExpression = (ColonExpression) node;
 					selectionOnType(modelModule, parsedUnit, colonExpression);
 				} else {
					CallExpression parentCall = this.getEnclosingCallNode(node);
						
 					if (parentCall != null) {
 						selectOnMethod(modelModule, parsedUnit, parentCall);
 					} else { // parentCall == null
 						if (node instanceof SimpleReference) {
 							selectionOnVariable(modelModule, parsedUnit, (SimpleReference)node);
 						}
 					}					
 				}
 			}
 		} catch (IndexOutOfBoundsException e) { // work-around internal failure 		
 			if(DEBUG) {
 				System.out.println("Exception caught by RubySelectionEngine:"); //$NON-NLS-1$
 				e.printStackTrace(System.out);
 			}
 		}  finally {
 			reset();
 		}		
 		
 
 		return (IModelElement[]) selectionElements
 				.toArray(new IModelElement[selectionElements.size()]);
 	}
 			
 	/**
 	 * Finds minimal ast node, that covers given position
 	 * @param unit
 	 * @param position
 	 * @return
 	 */
 	protected ASTNode findMinimalNode(ModuleDeclaration unit, int start, int end) {
 			
 		class Visitor extends ASTVisitor {
 			ASTNode result = null;
 			int start, end;
 			
 			public Visitor(int start, int end) {
 				this.start = start;
 				this.end = end;
 			}
 			
 			public ASTNode getResult () {
 				return result;
 			}			
 			
 			public boolean visitGeneral(ASTNode s) throws Exception {				
 				int realStart = s.sourceStart();
 				int realEnd = s.sourceEnd();
 				if (s instanceof Block) {
 					realStart = realEnd = -42; //never select on blocks
 				} else if (s instanceof TypeDeclaration) {
 					TypeDeclaration declaration = (TypeDeclaration) s;
 					realStart = declaration.getNameStart();
 					realEnd = declaration.getNameEnd();
 				} else if (s instanceof MethodDeclaration) {
 					MethodDeclaration declaration = (MethodDeclaration) s;
 					realStart = declaration.getNameStart();
 					realEnd = declaration.getNameEnd();
 				}
 				if (realStart <= start && realEnd >= end) {
 					result = s;
 					if (DLTKCore.DEBUG_SELECTION)
 						System.out.println("Found " + s.getClass().getName());
 				}
 				return true;
 			}
 
 			
 		}
 		
 		Visitor visitor = new Visitor(start, end);		
 		
 		try {
 			unit.traverse(visitor);
 		} catch (Exception e) {
 			if (DEBUG)
 				e.printStackTrace();
 		}
 		
 		return visitor.getResult();
 	}
 		
 	
 
 	/**
 	 * Checks, whether giver selection is correct selection, or can be expanded 
 	 * to correct selection region. As result will set this.actualSelection(Start|End)
 	 * properly. In case of incorrect selection, will return false.
 	 * @param source
 	 * @param start
 	 * @param end
 	 * @return
 	 */
 	protected boolean checkSelection(String source, int start, int end) {
 
 		if (start - 1 == end) {
 			ISourceRange range, range2;			
 			range = RubySyntaxUtils.getEnclosingName(source, start);
 			if (range != null) {
 				this.actualSelectionStart = range.getOffset();
 				this.actualSelectionEnd = this.actualSelectionStart
 						+ range.getLength();
 				//return true;
 			}
 			range2 = RubySyntaxUtils.insideMethodOperator(source, start);
 			if (range != null && (range2 == null || range2.getLength() < range.getLength()))
 				return true;
 			if (range2 != null) {
 				this.actualSelectionStart = range2.getOffset();
 				this.actualSelectionEnd = this.actualSelectionStart
 						+ range2.getLength();
 				return true;
 			}
 		} else {
 			String str = source.substring(start, end + 1);
 			if (RubySyntaxUtils.isRubyName(str)) {
 				this.actualSelectionStart = start;
 				this.actualSelectionEnd = end + 1;
 				return true;
 			}
 		}
 
 		return false;
 	}
 	
 	private static String getTypeNameFromColon (ColonExpression e) {
 		String name = "";
 		Expression expr = e;
 		while (expr instanceof ColonExpression) {
 			ColonExpression colonExpression = (ColonExpression) expr;
 			if (name.length() > 0) 
 				name = "::" + name;
 			name = colonExpression.getName() + name;
 			if (colonExpression.isFull()) {
 				name = "::" + name;
 				break;
 			} else
 				expr = colonExpression.getLeft();
 		} 
 		if (expr instanceof ConstantReference) {
 			ConstantReference constantReference = (ConstantReference) expr;
 			if (name.length() > 0) 
 				name = "::" + name;
 			name = constantReference.getName() + name;
 		}		
 		return name;
 	}
 	
 	private void selectTypes (String name) {
 		//TODO:  need full search
 		IType type = (IType) DLTKModelUtil.findType((org.eclipse.dltk.core.ISourceModule) sourceModule, name, "::");
 		if (type != null) {
 			selectionElements.add(type);
 		}
 	}
 	
 	private void selectionOnType (org.eclipse.dltk.core.ISourceModule modelModule, ModuleDeclaration parsedUnit, ColonExpression e) {
 		IUnit unit = calculator.getUnit(modelModule);
 		ITypeDescriptor node = calculator.calculateType(unit, e);
 		goToType(parsedUnit, node);
 //		//String name = getTypeNameFromColon(e);
 //		selectTypes(name);
 	}
 
 	private void goToType(ModuleDeclaration parsedUnit, ITypeDescriptor node) {
 		if (node instanceof RubyMetaTypeDescriptor) //XXX: little hack 
 			node = ((RubyMetaTypeDescriptor)node).getInstanceType();
 		if (node instanceof UserTypeDescriptor) {
 			UserTypeDescriptor typeDescriptor = (UserTypeDescriptor) node;
 			IClassLikeFragment[] fragments = typeDescriptor.getFragments();
 			if (fragments.length > 0) {
 				TypeDeclaration typeDeclaration = (TypeDeclaration) fragments[0].getASTNode(ASTCaching.ALLOW_ANY);
 				selectionOnTypeDeclaration(parsedUnit, typeDeclaration);
 			}
 		}
 	}
 	
 	private void selectionOnConstant (org.eclipse.dltk.core.ISourceModule modelModule, ModuleDeclaration parsedUnit, ConstantReference e) {
 		IUnit unit = calculator.getUnit(modelModule);
 		ITypeDescriptor node = calculator.calculateType(unit, e);
 		goToType(parsedUnit, node);
 		//selectTypes(constant.getName());
 		//TODO: find constants in currect scope
 	}
 	
 	private void selectionOnVariable (org.eclipse.dltk.core.ISourceModule modelModule, ModuleDeclaration parsedUnit, SimpleReference e) {
 		IUnit unit = calculator.getUnit(modelModule);
 		IScope scope = unit.getInnermostModelElement(e).getScope();
 		if (scope != null) {
 			IVariableDescriptor variable = scope.lookupVariable(e.getName(), false);
 			if (variable != null) {
 				//todo
 			}
 		}
 		//TODO
 		// does SimpleRef fits Inst, Local, Class vars?
 	}
 	
 	private void selectionOnTypeDeclaration(ModuleDeclaration parsedUnit,
 			TypeDeclaration typeDeclaration) {
 		String typeFQN = getTypeFQN(typeDeclaration);
 		//TODO: find all classes and methods with such name
 		IType el = (IType)DLTKModelUtil.findType(sourceModule.getModelElement(),
 				typeFQN, "::"); 		
 		Assert.isNotNull(el);
 		selectionElements.add(el);
 	}
 	
 	private void selectionOnMethodDeclaration (ModuleDeclaration parsedUnit, MethodDeclaration methodDeclaration) {		
 		TypeDeclaration typeDeclaration = this.getEnclosingType(methodDeclaration);		
 		if (typeDeclaration == null) {
 			IMethod method = ((org.eclipse.dltk.core.ISourceModule) sourceModule).getMethod(methodDeclaration.getName()); 								
 			selectionElements.add(method);
 		} else {
 			IType el = (IType)DLTKModelUtil.findType(sourceModule.getModelElement(),
 					getTypeFQN(typeDeclaration), "::");
 			Assert.isNotNull(el);
 			IMethod method = el.getMethod(methodDeclaration.getName());
 			selectionElements.add(method);
 		}
 	}
 	
 	private void selectOnMethod (org.eclipse.dltk.core.ISourceModule modelModule, ModuleDeclaration parsedUnit, CallExpression parentCall) {
 		String methodName = ((CallExpression) parentCall).getName();
 		IUnit unit = calculator.getUnit(modelModule);
 		IScope scope = unit.getInnermostModelElement(parentCall).getScope();
 		Statement receiver = parentCall.getReceiver();
 		
 		ITypeDescriptor type;
 		if (receiver == null) {
 			type = scope.getEnclosingType();
 		} else {
 			type = calculator.calculateType(unit, receiver);
 		}
 		
 		if (type != null && type instanceof IKnownTypeDescriptor) {
 			IKnownTypeDescriptor knownTypeDescriptor = (IKnownTypeDescriptor) type;
 			IMethodDescriptor methodDescriptor = type.getMethodByName(methodName);
 			if (methodDescriptor instanceof UserMethodDescriptor) {
 				UserMethodDescriptor userMethodDescriptor = (UserMethodDescriptor) methodDescriptor;
 				selectionOnMethodDeclaration(parsedUnit, userMethodDescriptor.getNode());								
 			}
 		}
 		
 	
 	}
 
 	/**
 	 * @deprecated
 	 */
 	private TypeDeclaration getEnclosingType(ASTNode node) {
 		while (node != null && !(node instanceof TypeDeclaration)) {
 			node = getParentNode(node);
 		}
 		return (TypeDeclaration) node;
 	}
 
 	/**
 	 * @deprecated
 	 */
 	private CallExpression getEnclosingCallNode(ASTNode node) {
 		while (node != null && !(node instanceof CallExpression)) {
 			node = getParentNode(node);
 		}
 		return (CallExpression) node;
 	}
 	
 	/**
 	 * @deprecated
 	 */
 	private TypeDeclaration getEnclosingType2(ASTNode node) {
 		node = getParentNode(node);
 		while (node != null && !(node instanceof TypeDeclaration)) {
 			node = getParentNode(node);
 		}
 		return (TypeDeclaration) node;
 	}
 	
 	private Map parentNodesMap;
 
 	/**
 	 * @deprecated
 	 */
 	private ASTNode getParentNode(ASTNode node) {
 		return (ASTNode) parentNodesMap.get(node);
 	}
 	
 	/**
 	 * @deprecated
 	 */
 	private void buildParentNodesMap(ModuleDeclaration rootNode) {
 		ParentshipBuildingVisitor visitor = new ParentshipBuildingVisitor();
 		try {
 			rootNode.traverse(visitor);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		parentNodesMap = visitor.getParents();
 	}
 
 }
