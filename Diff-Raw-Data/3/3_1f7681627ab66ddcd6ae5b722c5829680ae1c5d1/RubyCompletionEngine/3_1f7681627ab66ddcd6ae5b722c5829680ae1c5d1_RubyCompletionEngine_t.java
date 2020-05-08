 package org.eclipse.dltk.ruby.internal.core.codeassist;
 
 import java.util.Collection;
 import java.util.Map;
 
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.expressions.CallExpression;
 import org.eclipse.dltk.ast.references.ConstantReference;
 import org.eclipse.dltk.ast.references.SimpleReference;
 import org.eclipse.dltk.ast.statements.Statement;
 import org.eclipse.dltk.codeassist.CompletionEngine;
 import org.eclipse.dltk.codeassist.IAssistParser;
 import org.eclipse.dltk.codeassist.RelevanceConstants;
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.compiler.env.ISourceModule;
 import org.eclipse.dltk.core.CompletionProposal;
 import org.eclipse.dltk.core.CompletionRequestor;
 import org.eclipse.dltk.core.IDLTKProject;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.ISearchableEnvironment;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.ddp.BasicContext;
 import org.eclipse.dltk.ddp.ExpressionGoal;
 import org.eclipse.dltk.ddp.TypeInferencer;
 import org.eclipse.dltk.evaluation.types.IEvaluatedType;
 import org.eclipse.dltk.evaluation.types.SimpleType;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.ruby.ast.ColonExpression;
 import org.eclipse.dltk.ruby.core.model.FakeMethod;
 import org.eclipse.dltk.ruby.core.utils.RubySyntaxUtils;
 import org.eclipse.dltk.ruby.internal.parser.JRubySourceParser;
 import org.eclipse.dltk.ruby.internal.parsers.jruby.ASTUtils;
 import org.eclipse.dltk.ruby.typeinference.BuiltinMethods;
 import org.eclipse.dltk.ruby.typeinference.RubyClassType;
 import org.eclipse.dltk.ruby.typeinference.RubyEvaluatorFactory;
 import org.eclipse.dltk.ruby.typeinference.RubyMetaClassType;
 import org.eclipse.dltk.ruby.typeinference.RubyModelUtils;
 import org.eclipse.dltk.ruby.typeinference.RubyTypeInferencingUtils;
 import org.eclipse.dltk.ruby.typeinference.BuiltinMethods.BuiltinClass;
 
 public class RubyCompletionEngine extends CompletionEngine {
 
 	private TypeInferencer inferencer;
 
 	public RubyCompletionEngine(ISearchableEnvironment nameEnvironment,
 			CompletionRequestor requestor, Map settings,
 			IDLTKProject dltkProject) {
 		super(nameEnvironment, requestor, settings, dltkProject);
 		inferencer = new TypeInferencer(new RubyEvaluatorFactory());
 	}
 
 	private JRubySourceParser parser = new JRubySourceParser(null);
 
 	protected int getEndOfEmptyToken() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	protected String processMethodName(IMethod method, String token) {
 		return null;
 	}
 
 	protected String processTypeName(IType method, String token) {
 		return null;
 	}
 
 	public IAssistParser getParser() {
 		return null;
 	}
 
 	private boolean afterColons(String content, int position) {
 		if (position < 2)
 			return false;
 		if (content.charAt(position - 1) == ':'
 				&& content.charAt(position - 2) == ':')
 			return true;
 		return false;
 	}
 
 	private boolean afterDot(String content, int position) {
 		if (position < 1)
 			return false;
 		if (content.charAt(position - 1) == '.')
 			return true;
 		return false;
 	}
 
 	private static String cut(String content, int position, int length) {
 		content = content.substring(0, position)
 				+ content.substring(position + length);
 		return content;
 	}
 
 	private static boolean widowDot(String content, int position) {
 		while (position < content.length()
 				&& (content.charAt(position) == ' ' || content.charAt(position) == '\t'))
 			position++;
 		if (position >= content.length())
 			return true;
 		if (!RubySyntaxUtils.isNameChar(content.charAt(position)))
 			return true;
 		return false;
 	}
 
 	public void complete(ISourceModule module, int position, int i) {
 		this.actualCompletionPosition = position;
 		this.requestor.beginReporting();
 		org.eclipse.dltk.core.ISourceModule modelModule = (org.eclipse.dltk.core.ISourceModule) module;
 		try {
 			String content = module.getSourceContents();
 			if (afterDot(content, position)) {
 				this.setSourceRange(position, position);
 				if (widowDot(content, position)) {
 					content = cut(content, position - 1, 1);
 					position--;
 				}
 				ModuleDeclaration moduleDeclaration = parser.parse(content);
 				ASTNode node = ASTUtils.findMaximalNodeEndingAt(
 						moduleDeclaration, position - 1);
 				if (node instanceof Statement) {
 					IMethod[] allMethods = getMethodsForReceiver(modelModule, moduleDeclaration, node);
 					if (allMethods != null) {
 						for (int j = 0; j < allMethods.length; j++) {
 							reportMethod("".toCharArray(), 0, allMethods[j], allMethods.length - j);
 						}
 					}
 				}
 			} else if (afterColons(content, position)) {
 				this.setSourceRange(position, position);
 				if (widowDot(content, position)) {
 					content = cut(content, position - 2, 2);
 					position -= 2;
 				}
 				ModuleDeclaration moduleDeclaration = parser.parse(content);
 				ASTNode node = ASTUtils.findMaximalNodeEndingAt(
 						moduleDeclaration, position - 1);
 				if (node instanceof ColonExpression
 						|| node instanceof ConstantReference) {
 					ExpressionGoal goal = new ExpressionGoal(new BasicContext(
 							modelModule, moduleDeclaration), (Statement) node);
 					IEvaluatedType type = inferencer.evaluateGoal(goal, 0);
 					if (type instanceof RubyMetaClassType)
 						type = ((RubyMetaClassType) type).getInstanceType();
 					if (type instanceof RubyClassType) {
 						IType[] subtypes = RubyTypeInferencingUtils
 								.findSubtypes(modelModule.getScriptProject(),
 										(RubyClassType) type);
 						for (int j = 0; j < subtypes.length; j++) {
 							reportType("".toCharArray(), 0, subtypes[j],
 									RubyTypeInferencingUtils.compileFQN(
 											((RubyClassType) type).getFQN(),
 											true).length() + 2);
 						}
 					}
 				}
 			} else {
 				ModuleDeclaration moduleDeclaration = parser.parse(content);
 				ASTNode minimalNode = ASTUtils.findMinimalNode(
 						moduleDeclaration, position - 1, position - 1);
 				if (minimalNode != null) {
 					if (minimalNode instanceof CallExpression) {
 						completeCall(modelModule, moduleDeclaration,
 								(CallExpression) minimalNode, position);
 					} else if (minimalNode instanceof ConstantReference) {
 						completeConstant(modelModule, moduleDeclaration,
 								(ConstantReference) minimalNode, position);
 					} else if (minimalNode instanceof ColonExpression) {
 						completeColonExpression(modelModule, moduleDeclaration,
 								(ColonExpression) minimalNode, position);
 					} else if (minimalNode instanceof SimpleReference) {
 						completeSimpleRef(modelModule, moduleDeclaration,
 								(SimpleReference) minimalNode, position);
 					} else {
 						System.out.println("Node "
 								+ minimalNode.getClass().getName()
 								+ " is unsuppored by now");
 					}
 				}
 
 			}
 
 		} finally {
 			this.requestor.endReporting();
 		}
 	}
 
 	private IMethod[] getMethodsForReceiver(org.eclipse.dltk.core.ISourceModule modelModule, ModuleDeclaration moduleDeclaration, ASTNode receiver) {
 		ExpressionGoal goal = new ExpressionGoal(new BasicContext(
 				modelModule, moduleDeclaration), (Statement) receiver);
 		IEvaluatedType type = inferencer.evaluateGoal(goal, 0);
 		if (type instanceof RubyClassType) {
 			RubyClassType rubyClassType = (RubyClassType) type;
 			rubyClassType = RubyTypeInferencingUtils
 					.resolveMethods(modelModule.getScriptProject(),
 							rubyClassType);
 			return rubyClassType.getAllMethods();			
 		} else if (type instanceof RubyMetaClassType) {
 			RubyMetaClassType metaType = (RubyMetaClassType) type;
 			metaType = RubyTypeInferencingUtils.resolveMethods(modelModule.getScriptProject(), metaType);
 			return metaType.getMethods();
 		} else if (type instanceof SimpleType) {
 			SimpleType simpleType = (SimpleType) type;
 			IMethod[] meth = null;
 			switch (simpleType.getType()) {
 				case SimpleType.TYPE_NUMBER:
 					meth = RubyModelUtils.getFakeMethods((ModelElement) modelModule, "Fixnum");
 					break;
 				case SimpleType.TYPE_STRING:
 					meth = RubyModelUtils.getFakeMethods((ModelElement) modelModule, "String");
 					break;
 			}
 			return meth;
 		}
 		return null;
 	}
 
 	private void completeSimpleRef(org.eclipse.dltk.core.ISourceModule module,
 			ModuleDeclaration moduleDeclaration, SimpleReference node,
 			int position) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void completeColonExpression(
 			org.eclipse.dltk.core.ISourceModule module,
 			ModuleDeclaration moduleDeclaration, ColonExpression node,
 			int position) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void completeConstant(org.eclipse.dltk.core.ISourceModule module,
 			ModuleDeclaration moduleDeclaration, ConstantReference node,
 			int position) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void completeCall(org.eclipse.dltk.core.ISourceModule module,
 			ModuleDeclaration moduleDeclaration, CallExpression node,
 			int position) {
 		Statement receiver = node.getReceiver();
 
 		String content;
 		try {
 			content = module.getSource();
 		} catch (ModelException e) {
 			return;
 		}
 		
		
 		int pos = node.getReceiver().sourceEnd() + 1;
 		
 		String starting = content.substring(pos, position);
 
		this.setSourceRange(position - starting.length(), position);
 		if (receiver != null) {
 			IMethod[] allMethods = getMethodsForReceiver(module, moduleDeclaration, receiver);
 			if (allMethods != null) {
 				for (int j = 0; j < allMethods.length; j++) {
 					if (allMethods[j].getElementName().startsWith(starting))
 						reportMethod("".toCharArray(), 0, allMethods[j], allMethods.length - j);
 				}
 			}
 		} else {
 			// complete word
 		}
 	}
 
 	protected String processFieldName(IField field, String token) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private void reportMethod(char[] token, int length, IMethod method, int order) {
 				
 		char[] name = method.getElementName().toCharArray();
 		if (length <= name.length
 				&& CharOperation.prefixEquals(token, name, false)) {
 			int relevance = RelevanceConstants.R_INTERESTING;
 			relevance += computeRelevanceForCaseMatching(token, name);
 			relevance += RelevanceConstants.R_NON_RESTRICTED;
 			relevance += order; 
 			
 			// accept result
 			noProposal = false;
 			if (!requestor.isIgnored(CompletionProposal.METHOD_DECLARATION)) {
 				CompletionProposal proposal = createProposal(
 						CompletionProposal.METHOD_DECLARATION,
 						actualCompletionPosition);
 				// proposal.setSignature(getSignature(typeBinding));
 				// proposal.setPackageName(q);
 				// proposal.setTypeName(displayName);
 				// ArgumentDescriptor[] arguments = method.getArguments();
 				// if(arguments.length > 0 ) {
 				// char[][] args = new char[arguments.length][];
 				// for( int j = 0; j < arguments.length; ++j ) {
 				// args[j] = arguments[j].getName().toCharArray();
 				// }
 				// proposal.setParameterNames(args);
 				// }
 
 				String[] params = null;
 
 				if (!(method instanceof FakeMethod)) {
 					try {
 						params = method.getParameters();
 					} catch (ModelException e) {
 						e.printStackTrace();
 					}
 				}
 
 				if (params != null && params.length > 0) {
 					char[][] args = new char[params.length][];
 					for (int i = 0; i < params.length; ++i) {
 						args[i] = params[i].toCharArray();
 					}
 					proposal.setParameterNames(args);
 				}
 
 				proposal.setModelElement(method);
 				proposal.setName(name);
 				proposal.setCompletion(name);
 				// proposal.setFlags(Flags.AccDefault);
 				proposal.setReplaceRange(this.startPosition - this.offset,
 						this.endPosition - this.offset);
 				proposal.setRelevance(relevance);
 				this.requestor.accept(proposal);
 				if (DEBUG) {
 					this.printDebug(proposal);
 				}
 			}
 		}
 	}
 
 	private void reportType(char[] token, int length, IType type, int from) {
 		char[] name = type.getTypeQualifiedName("::").toCharArray();
 		if (length <= name.length
 				&& CharOperation.prefixEquals(token, name, false)) {
 			int relevance = RelevanceConstants.R_INTERESTING;
 			relevance += computeRelevanceForCaseMatching(token, name);
 			relevance += RelevanceConstants.R_NON_RESTRICTED;
 			
 			// accept result
 			noProposal = false;
 			if (!requestor.isIgnored(CompletionProposal.TYPE_REF)) {
 				CompletionProposal proposal = createProposal(
 						CompletionProposal.TYPE_REF, actualCompletionPosition);
 				// proposal.setSignature(getSignature(typeBinding));
 				// proposal.setPackageName(q);
 				// proposal.setTypeName(displayName);
 
 				proposal.setModelElement(type);
 				proposal.setName(name);
 				proposal.setCompletion(type.getTypeQualifiedName("::")
 						.substring(from).toCharArray());
 				// proposal.setFlags(Flags.AccDefault);
 				proposal.setReplaceRange(this.startPosition - this.offset,
 						this.endPosition - this.offset);
 				proposal.setRelevance(relevance);
 				this.requestor.accept(proposal);
 				if (DEBUG) {
 					this.printDebug(proposal);
 				}
 			}
 		}
 	}
 
 }
