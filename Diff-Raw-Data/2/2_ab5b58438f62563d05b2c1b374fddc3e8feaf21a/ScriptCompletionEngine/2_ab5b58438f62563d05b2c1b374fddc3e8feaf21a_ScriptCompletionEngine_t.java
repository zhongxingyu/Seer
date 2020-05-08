 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.codeassist;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.dltk.ast.declarations.Argument;
 import org.eclipse.dltk.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.compiler.problem.CategorizedProblem;
 import org.eclipse.dltk.core.CompletionProposal;
 import org.eclipse.dltk.core.CompletionRequestor;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IAccessRule;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISearchableEnvironment;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.codeassist.impl.Engine;
 import org.eclipse.dltk.internal.compiler.lookup.LookupEnvironment;
 
 public abstract class ScriptCompletionEngine extends Engine implements
 		ICompletionEngine {
 	protected static boolean DEBUG = DLTKCore.DEBUG_COMPLETION;
 
 	protected IScriptProject scriptProject;
 
	// Accepts completion proposals
 	protected CompletionRequestor requestor;
 
 	protected int startPosition;
 	protected int actualCompletionPosition;
 	protected int endPosition;
 	protected int offset;
 
 	protected char[] fileName = null;
 
 	protected boolean noProposal = true;
 
 	protected CategorizedProblem problem = null;
 
 	protected char[] source;
 
 	public ScriptCompletionEngine(/*
 									 * ISearchableEnvironment nameEnvironment,
 									 * CompletionRequestor requestor, Map
 									 * settings, IScriptProject scriptProject
 									 */) {
 		super(null);
 
 		// this.scriptProject = scriptProject;
 		// this.requestor = requestor;
 		// this.nameEnvironment = nameEnvironment;
 		// this.lookupEnvironment = new LookupEnvironment(this,
 		// nameEnvironment);
 	}
 
 	protected CompletionProposal createProposal(int kind, int completionOffset) {
 		CompletionProposal proposal = CompletionProposal.create(kind,
 				completionOffset - this.offset);
 
 		return proposal;
 	}
 
 	// print
 	protected void printDebug(CategorizedProblem error) {
 		if (ScriptCompletionEngine.DEBUG) {
 			System.out.print("COMPLETION - completionFailure("); //$NON-NLS-1$
 			System.out.print(error);
 			System.out.println(")"); //$NON-NLS-1$
 		}
 	}
 
 	protected void printDebug(CompletionProposal proposal) {
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("COMPLETION - "); //$NON-NLS-1$
 		switch (proposal.getKind()) {
 		case CompletionProposal.FIELD_REF:
 			buffer.append("FIELD_REF"); //$NON-NLS-1$
 			break;
 		case CompletionProposal.KEYWORD:
 			buffer.append("KEYWORD"); //$NON-NLS-1$
 			break;
 		case CompletionProposal.LABEL_REF:
 			buffer.append("LABEL_REF"); //$NON-NLS-1$
 			break;
 		case CompletionProposal.LOCAL_VARIABLE_REF:
 			buffer.append("LOCAL_VARIABLE_REF"); //$NON-NLS-1$
 			break;
 		case CompletionProposal.METHOD_DECLARATION:
 			buffer.append("METHOD_DECLARATION"); //$NON-NLS-1$
 			break;
 		case CompletionProposal.METHOD_REF:
 			buffer.append("METHOD_REF"); //$NON-NLS-1$
 			break;
 		case CompletionProposal.PACKAGE_REF:
 			buffer.append("PACKAGE_REF"); //$NON-NLS-1$
 			break;
 		case CompletionProposal.TYPE_REF:
 			buffer.append("TYPE_REF"); //$NON-NLS-1$
 			break;
 		case CompletionProposal.VARIABLE_DECLARATION:
 			buffer.append("VARIABLE_DECLARATION"); //$NON-NLS-1$
 			break;
 		case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
 			buffer.append("POTENTIAL_METHOD_DECLARATION"); //$NON-NLS-1$
 			break;
 		case CompletionProposal.METHOD_NAME_REFERENCE:
 			buffer.append("METHOD_NAME_REFERENCE"); //$NON-NLS-1$
 			break;
 		default:
 			buffer.append("PROPOSAL"); //$NON-NLS-1$
 			break;
 
 		}
 
 		buffer.append("{\n");//$NON-NLS-1$
 		buffer
 				.append("\tCompletion[").append(proposal.getCompletion() == null ? "null".toCharArray() : proposal.getCompletion()).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		buffer
 				.append("\tDeclarationKey[").append(proposal.getDeclarationKey() == null ? "null".toCharArray() : proposal.getDeclarationKey()).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		buffer
 				.append("\tKey[").append(proposal.getKey() == null ? "null".toCharArray() : proposal.getKey()).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 
 		buffer
 				.append("\tName[").append(proposal.getName() == null ? "null".toCharArray() : proposal.getName()).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 
 		buffer
 				.append("\tCompletionLocation[").append(proposal.getCompletionLocation()).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		int start = proposal.getReplaceStart();
 		int end = proposal.getReplaceEnd();
 		buffer.append("\tReplaceStart[").append(start).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
 		buffer.append("-ReplaceEnd[").append(end).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		if (this.source != null)
 			buffer
 					.append("\tReplacedText[").append(this.source, start, end - start).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		buffer
 				.append("\tTokenStart[").append(proposal.getTokenStart()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
 		buffer
 				.append("-TokenEnd[").append(proposal.getTokenEnd()).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		buffer
 				.append("\tRelevance[").append(proposal.getRelevance()).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$
 
 		buffer.append("}\n");//$NON-NLS-1$
 		System.out.println(buffer.toString());
 	}
 
 	// Source range
 	protected void setSourceRange(int start, int end) {
 		this.setSourceRange(start, end, true);
 	}
 
 	protected void setSourceRange(int start, int end,
 			boolean emptyTokenAdjstment) {
 		this.startPosition = start;
 		if (emptyTokenAdjstment) {
 			int endOfEmptyToken = getEndOfEmptyToken();
 			this.endPosition = endOfEmptyToken > end ? endOfEmptyToken : end;
 		} else {
 			this.endPosition = end;
 		}
 	}
 
 	protected abstract int getEndOfEmptyToken();
 
 	protected abstract String processMethodName(IMethod method, String token);
 
 	protected abstract String processTypeName(IType method, String token);
 
 	protected abstract String processFieldName(IField field, String token);
 
 	// what about onDemand types? Ignore them since it does not happen!
 	// import p1.p2.A.*;
 	public void findKeywords(char[] keyword, char[][] choices,
 			boolean canCompleteEmptyToken) {
 		if (choices == null || choices.length == 0)
 			return;
 
 		int length = keyword.length;
 		if (canCompleteEmptyToken || length > 0) {
 			for (int i = 0; i < choices.length; i++) {
 				if (length <= choices[i].length
 						&& CharOperation.prefixEquals(keyword, choices[i],
 								false)) {
 					int relevance = computeBaseRelevance();
 
 					relevance += computeRelevanceForInterestingProposal();
 					relevance += computeRelevanceForCaseMatching(keyword,
 							choices[i]);
 					relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no
 					/*
 					 * access restriction for keywors
 					 */
 
 					// if (CharOperation.equals(choices[i], Keywords.TRUE)
 					// || CharOperation.equals(choices[i], Keywords.FALSE)) {
 					// relevance +=
 					// computeRelevanceForExpectingType(TypeBinding.BOOLEAN);
 					// relevance += computeRelevanceForQualification(false);
 					// }
 					this.noProposal = false;
 					if (!this.requestor.isIgnored(CompletionProposal.KEYWORD)) {
 						CompletionProposal proposal = this.createProposal(
 								CompletionProposal.KEYWORD,
 								this.actualCompletionPosition);
 						proposal.setName(choices[i]);
 						proposal.setCompletion(choices[i]);
 						proposal.setReplaceRange(this.startPosition
 								- this.offset, this.endPosition - this.offset);
 						proposal.setRelevance(relevance);
 						this.requestor.accept(proposal);
 						if (DEBUG) {
 							this.printDebug(proposal);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	protected void findLocalVariables(char[] token, char[][] choices,
 			boolean canCompleteEmptyToken, boolean provideDollar) {
 		int kind = CompletionProposal.LOCAL_VARIABLE_REF;
 		findElements(token, choices, canCompleteEmptyToken, provideDollar,
 				kind, Collections.EMPTY_MAP, Collections.EMPTY_MAP);
 	}
 
 	protected void findElements(char[] token, char[][] choices,
 			boolean canCompleteEmptyToken, boolean provideDollar, int kind,
 			Map parameterNames, Map proposalInfos) {
 		if (choices == null || choices.length == 0)
 			return;
 
 		int length = token.length;
 		if (canCompleteEmptyToken || length > 0) {
 			for (int i = 0; i < choices.length; i++) {
 				char[] co = choices[i];
 				if (!provideDollar && co.length > 1 && co[0] == '$') {
 					char co2[] = new char[co.length - 1];
 					System.arraycopy(co, 1, co2, 0, co2.length);
 					co = co2;
 				}
 				if (length <= choices[i].length
 						&& CharOperation.prefixEquals(token, co, false)) {
 					int relevance = computeBaseRelevance();
 					relevance += computeRelevanceForInterestingProposal();
 					relevance += computeRelevanceForCaseMatching(token, co);
 					relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no
 
 					// accept result
 					ScriptCompletionEngine.this.noProposal = false;
 
 					if (!ScriptCompletionEngine.this.requestor.isIgnored(kind)) {
 						CompletionProposal proposal = ScriptCompletionEngine.this
 								.createProposal(
 										kind,
 										ScriptCompletionEngine.this.actualCompletionPosition);
 						// proposal.setSignature(getSignature(typeBinding));
 						// proposal.setPackageName(q);
 						// proposal.setTypeName(displayName);
 						proposal.setName(co);
 						proposal.setCompletion(co);
 						proposal.setParameterNames((char[][]) parameterNames
 								.get(choices[i]));
 						Object proposalInfo = proposalInfos.get(choices[i]);
 						if (proposalInfo != null) {
 							proposal.extraInfo = proposalInfo;
 						}
 
 						// proposal.setFlags(Flags.AccDefault);
 						proposal.setReplaceRange(this.startPosition
 								- this.offset, this.endPosition - this.offset);
 						proposal.setRelevance(relevance);
 						this.requestor.accept(proposal);
 						if (DEBUG) {
 							this.printDebug(proposal);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void findMethods(char[] token, boolean canCompleteEmptyToken,
 			List methods, List methodNames) {
 		if (methods == null || methods.size() == 0)
 			return;
 
 		int length = token.length;
 		String tok = new String(token);
 		if (canCompleteEmptyToken || length > 0) {
 			for (int i = 0; i < methods.size(); i++) {
 				IMethod method = (IMethod) methods.get(i);
 				String qname = (String) methodNames.get(i);// processMethodName(method,
 				// tok);
 				char[] name = qname.toCharArray();
 				if (DLTKCore.DEBUG_COMPLETION) {
 					System.out.println("Completion:" + qname); //$NON-NLS-1$
 				}
 				if (length <= name.length
 						&& CharOperation.prefixEquals(token, name, false)) {
 					int relevance = computeBaseRelevance();
 					relevance += computeRelevanceForInterestingProposal();
 					relevance += computeRelevanceForCaseMatching(token, name);
 					relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no
 
 					// accept result
 					ScriptCompletionEngine.this.noProposal = false;
 					if (!ScriptCompletionEngine.this.requestor
 							.isIgnored(CompletionProposal.METHOD_DECLARATION)) {
 						CompletionProposal proposal = ScriptCompletionEngine.this
 								.createProposal(
 										CompletionProposal.METHOD_DECLARATION,
 										ScriptCompletionEngine.this.actualCompletionPosition);
 						// proposal.setSignature(getSignature(typeBinding));
 						// proposal.setPackageName(q);
 						// proposal.setTypeName(displayName);
 						proposal.setModelElement(method);
 						if (method != null) {
 							try {
 								proposal.setFlags(method.getFlags());
 							} catch (ModelException e) {
 								if (DLTKCore.DEBUG) {
 									e.printStackTrace();
 								}
 							}
 						}
 						String[] arguments = null;
 
 						try {
 							arguments = method.getParameters();
 						} catch (ModelException e) {
 							if (DLTKCore.DEBUG) {
 								e.printStackTrace();
 							}
 						}
 						if (arguments != null && arguments.length > 0) {
 							char[][] args = new char[arguments.length][];
 							for (int j = 0; j < arguments.length; ++j) {
 								args[j] = arguments[j].toCharArray();
 							}
 							proposal.setParameterNames(args);
 						}
 
 						proposal.setName(name);
 						proposal.setCompletion(name);
 						// proposal.setFlags(Flags.AccDefault);
 						proposal.setReplaceRange(this.startPosition
 								- this.offset, this.endPosition - this.offset);
 						proposal.setRelevance(relevance);
 						this.requestor.accept(proposal);
 						if (DEBUG) {
 							this.printDebug(proposal);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	protected void findLocalMethods(char[] token,
 			boolean canCompleteEmptyToken, List methods, List methodNames) {
 		if (methods == null || methods.size() == 0)
 			return;
 
 		int length = token.length;
 		if (canCompleteEmptyToken || length > 0) {
 			for (int i = 0; i < methods.size(); i++) {
 				MethodDeclaration method = (MethodDeclaration) methods.get(i);
 				char[] name = ((String) (methodNames.get(i))).toCharArray();
 				if (length <= name.length
 						&& CharOperation.prefixEquals(token, name, false)) {
 					int relevance = computeBaseRelevance();
 					relevance += computeRelevanceForInterestingProposal();
 					relevance += computeRelevanceForCaseMatching(token, name);
 					relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no
 
 					// accept result
 					ScriptCompletionEngine.this.noProposal = false;
 					if (!ScriptCompletionEngine.this.requestor
 							.isIgnored(CompletionProposal.METHOD_DECLARATION)) {
 						CompletionProposal proposal = ScriptCompletionEngine.this
 								.createProposal(
 										CompletionProposal.METHOD_DECLARATION,
 										ScriptCompletionEngine.this.actualCompletionPosition);
 						// proposal.setSignature(getSignature(typeBinding));
 						// proposal.setPackageName(q);
 						// proposal.setTypeName(displayName);
 						List arguments = method.getArguments();
 						if (arguments != null && arguments.size() > 0) {
 							char[][] args = new char[arguments.size()][];
 							for (int j = 0; j < arguments.size(); ++j) {
 								args[j] = ((Argument) arguments.get(j))
 										.getName().toCharArray();
 							}
 							proposal.setParameterNames(args);
 						}
 
 						proposal.setName(name);
 						proposal.setCompletion(name);
 						// proposal.setFlags(Flags.AccDefault);
 						proposal.setReplaceRange(this.startPosition
 								- this.offset, this.endPosition - this.offset);
 						proposal.setRelevance(relevance);
 						this.requestor.accept(proposal);
 						if (DEBUG) {
 							this.printDebug(proposal);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	protected void findMethods(char[] token, boolean canCompleteEmptyToken,
 			List methods) {
 		findMethods(token, canCompleteEmptyToken, methods,
 				CompletionProposal.METHOD_DECLARATION);
 	}
 
 	public void findFields(char[] token, boolean canCompleteEmptyToken,
 			List methods, String prefix) {
 		findFields(token, canCompleteEmptyToken, methods,
 				CompletionProposal.FIELD_REF, prefix);
 	}
 
 	protected void findMethods(char[] token, boolean canCompleteEmptyToken,
 			List methods, int kind) {
 		if (methods == null || methods.size() == 0)
 			return;
 
 		int length = token.length;
 		String tok = new String(token);
 		if (canCompleteEmptyToken || length > 0) {
 			for (int i = 0; i < methods.size(); i++) {
 				IMethod method = (IMethod) methods.get(i);
 				String qname = processMethodName(method, tok);
 				char[] name = qname.toCharArray();
 				if (DLTKCore.DEBUG_COMPLETION) {
 					System.out.println("Completion:" + qname); //$NON-NLS-1$
 				}
 				if (length <= name.length
 						&& CharOperation.prefixEquals(token, name, false)) {
 					int relevance = computeBaseRelevance();
 					relevance += computeRelevanceForInterestingProposal();
 					relevance += computeRelevanceForCaseMatching(token, name);
 					relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no
 
 					// accept result
 					ScriptCompletionEngine.this.noProposal = false;
 					if (!ScriptCompletionEngine.this.requestor.isIgnored(kind)) {
 						CompletionProposal proposal = ScriptCompletionEngine.this
 								.createProposal(
 										kind,
 										ScriptCompletionEngine.this.actualCompletionPosition);
 						// proposal.setSignature(getSignature(typeBinding));
 						// proposal.setPackageName(q);
 						// proposal.setTypeName(displayName);
 						proposal.setModelElement(method);
 						try {
 							proposal.setFlags(method.getFlags());
 						} catch (ModelException e1) {
 							if (DLTKCore.DEBUG) {
 								e1.printStackTrace();
 							}
 						}
 						String[] arguments = null;
 
 						try {
 							arguments = method.getParameters();
 						} catch (ModelException e) {
 							if (DLTKCore.DEBUG) {
 								e.printStackTrace();
 							}
 						}
 						if (arguments != null && arguments.length > 0) {
 							char[][] args = new char[arguments.length][];
 							for (int j = 0; j < arguments.length; ++j) {
 								args[j] = arguments[j].toCharArray();
 							}
 							proposal.setParameterNames(args);
 						}
 
 						proposal.setName(name);
 						proposal.setCompletion(name);
 						// proposal.setFlags(Flags.AccDefault);
 						proposal.setReplaceRange(this.startPosition
 								- this.offset, this.endPosition - this.offset);
 						proposal.setRelevance(relevance);
 						this.requestor.accept(proposal);
 						if (DEBUG) {
 							this.printDebug(proposal);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	protected void findFields(char[] token, boolean canCompleteEmptyToken,
 			List fields, int kind, String prefix) {
 		if (fields == null || fields.size() == 0)
 			return;
 
 		int length = token.length;
 		String tok = new String(token);
 		if (canCompleteEmptyToken || length > 0) {
 			for (int i = 0; i < fields.size(); i++) {
 				IField field = (IField) fields.get(i);
 				String qname = processFieldName(field, tok);
 				char[] name = qname.toCharArray();
 				if (DLTKCore.DEBUG_COMPLETION) {
 					System.out.println("Completion:" + qname); //$NON-NLS-1$
 				}
 				if (length <= name.length
 						&& CharOperation.prefixEquals(token, name, false)) {
 					int relevance = computeBaseRelevance();
 					relevance += computeRelevanceForInterestingProposal();
 					relevance += computeRelevanceForCaseMatching(token, name);
 					relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no
 
 					// accept result
 					ScriptCompletionEngine.this.noProposal = false;
 					if (!ScriptCompletionEngine.this.requestor.isIgnored(kind)) {
 						CompletionProposal proposal = ScriptCompletionEngine.this
 								.createProposal(
 										kind,
 										ScriptCompletionEngine.this.actualCompletionPosition);
 						// proposal.setSignature(getSignature(typeBinding));
 						// proposal.setPackageName(q);
 						// proposal.setTypeName(displayName);
 						proposal.setModelElement(field);
 						proposal.setName(name);
 						proposal.setCompletion((prefix + qname).toCharArray());
 						// proposal.setFlags(Flags.AccDefault);
 						proposal.setReplaceRange(this.startPosition
 								- this.offset, this.endPosition - this.offset);
 						proposal.setRelevance(relevance);
 						this.requestor.accept(proposal);
 						if (DEBUG) {
 							this.printDebug(proposal);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void findTypes(char[] token, boolean canCompleteEmptyToken,
 			List types) {
 		if (types == null || types.size() == 0)
 			return;
 
 		int length = token.length;
 		String tok = new String(token);
 		if (canCompleteEmptyToken || length > 0) {
 			for (int i = 0; i < types.size(); i++) {
 				IType type = (IType) types.get(i);
 				String qname = processTypeName(type, tok);
 				char[] name = qname.toCharArray();
 				if (DLTKCore.DEBUG_COMPLETION) {
 					System.out.println("Completion:" + qname); //$NON-NLS-1$
 				}
 				if (length <= name.length
 						&& CharOperation.prefixEquals(token, name, false)) {
 					int relevance = computeBaseRelevance();
 					relevance += computeRelevanceForInterestingProposal();
 					relevance += computeRelevanceForCaseMatching(token, name);
 					relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no
 
 					// accept result
 					ScriptCompletionEngine.this.noProposal = false;
 					if (!ScriptCompletionEngine.this.requestor
 							.isIgnored(CompletionProposal.TYPE_REF)) {
 
 						CompletionProposal proposal = ScriptCompletionEngine.this
 								.createProposal(
 										CompletionProposal.TYPE_REF,
 										ScriptCompletionEngine.this.actualCompletionPosition);
 						// proposal.setSignature(getSignature(typeBinding));
 						// proposal.setPackageName(q);
 						// proposal.setTypeName(displayName);
 						proposal.setModelElement(type);
 						proposal.setName(name);
 						proposal.setCompletion(name);
 						// proposal.setFlags(Flags.AccDefault);
 						proposal.setReplaceRange(this.startPosition
 								- this.offset, this.endPosition - this.offset);
 						proposal.setRelevance(relevance);
 						this.requestor.accept(proposal);
 						if (DEBUG) {
 							this.printDebug(proposal);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	// Relevance
 	private int computeBaseRelevance() {
 		return RelevanceConstants.R_DEFAULT;
 	}
 
 	private int computeRelevanceForInterestingProposal() {
 		return RelevanceConstants.R_INTERESTING;
 	}
 
 	protected int computeRelevanceForCaseMatching(char[] token,
 			char[] proposalName) {
 		if (this.options.camelCaseMatch) {
 			if (CharOperation.equals(token, proposalName, true)) {
 				return RelevanceConstants.R_CASE
 						+ RelevanceConstants.R_EXACT_NAME;
 			} else if (CharOperation.prefixEquals(token, proposalName, true)) {
 				return RelevanceConstants.R_CASE;
 			} else if (CharOperation.camelCaseMatch(token, proposalName)) {
 				return RelevanceConstants.R_CAMEL_CASE;
 			} else if (CharOperation.equals(token, proposalName, false)) {
 				return RelevanceConstants.R_EXACT_NAME;
 			}
 		} else if (CharOperation.prefixEquals(token, proposalName, true)) {
 			if (CharOperation.equals(token, proposalName, true)) {
 				return RelevanceConstants.R_CASE
 						+ RelevanceConstants.R_EXACT_NAME;
 			} else {
 				return RelevanceConstants.R_CASE;
 			}
 		} else if (CharOperation.equals(token, proposalName, false)) {
 			return RelevanceConstants.R_EXACT_NAME;
 		}
 		return 0;
 	}
 
 	protected int computeRelevanceForRestrictions(int accessRuleKind) {
 		if (accessRuleKind == IAccessRule.K_ACCESSIBLE) {
 			return RelevanceConstants.R_NON_RESTRICTED;
 		}
 		return 0;
 	}
 
 	public void setEnvironment(ISearchableEnvironment environment) {
 		this.nameEnvironment = environment;
 		this.lookupEnvironment = new LookupEnvironment(this, nameEnvironment);
 	}
 
 	public void setOptions(Map options) {
 	}
 
 	public void setProject(IScriptProject project) {
 		this.scriptProject = project;
 	}
 
 	public void setRequestor(CompletionRequestor requestor) {
 		this.requestor = requestor;
 	}
 }
