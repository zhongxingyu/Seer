 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.internal.core.codeassist;
 
import java.util.ArrayList;
 import java.util.HashSet;
import java.util.List;
 import java.util.Set;
 
 import org.eclipse.dltk.codeassist.ScriptCompletionEngine;
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.compiler.env.IModuleSource;
 import org.eclipse.dltk.core.CompletionProposal;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IAccessRule;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.internal.javascript.ti.IReferenceAttributes;
 import org.eclipse.dltk.internal.javascript.ti.ITypeInferenceContext;
 import org.eclipse.dltk.internal.javascript.ti.IValueProvider;
 import org.eclipse.dltk.internal.javascript.ti.PositionReachedException;
 import org.eclipse.dltk.internal.javascript.ti.TypeInferencer2;
 import org.eclipse.dltk.internal.javascript.typeinference.CompletionPath;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.ast.StringLiteral;
 import org.eclipse.dltk.javascript.core.JavaScriptKeywords;
 import org.eclipse.dltk.javascript.parser.JavaScriptParserUtil;
 import org.eclipse.dltk.javascript.typeinference.IValueCollection;
 import org.eclipse.dltk.javascript.typeinference.IValueParent;
 import org.eclipse.dltk.javascript.typeinference.IValueReference;
 import org.eclipse.dltk.javascript.typeinference.ReferenceKind;
 import org.eclipse.dltk.javascript.typeinfo.IRClassType;
 import org.eclipse.dltk.javascript.typeinfo.IRMethod;
 import org.eclipse.dltk.javascript.typeinfo.IRParameter;
 import org.eclipse.dltk.javascript.typeinfo.IRRecordMember;
 import org.eclipse.dltk.javascript.typeinfo.IRRecordType;
 import org.eclipse.dltk.javascript.typeinfo.IRSimpleType;
 import org.eclipse.dltk.javascript.typeinfo.IRType;
 import org.eclipse.dltk.javascript.typeinfo.IRVariable;
 import org.eclipse.dltk.javascript.typeinfo.ITypeNames;
 import org.eclipse.dltk.javascript.typeinfo.JSTypeSet;
 import org.eclipse.dltk.javascript.typeinfo.MemberPredicate;
 import org.eclipse.dltk.javascript.typeinfo.TypeMemberQuery;
 import org.eclipse.dltk.javascript.typeinfo.TypeMode;
 import org.eclipse.dltk.javascript.typeinfo.TypeUtil;
 import org.eclipse.dltk.javascript.typeinfo.model.Element;
 import org.eclipse.dltk.javascript.typeinfo.model.Member;
 import org.eclipse.dltk.javascript.typeinfo.model.Method;
 import org.eclipse.dltk.javascript.typeinfo.model.Parameter;
 import org.eclipse.dltk.javascript.typeinfo.model.ParameterKind;
 import org.eclipse.dltk.javascript.typeinfo.model.Type;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeKind;
 
 public class JavaScriptCompletionEngine2 extends ScriptCompletionEngine
 		implements JSCompletionEngine {
 
 	private boolean useEngine = true;
 
 	public boolean isUseEngine() {
 		return useEngine;
 	}
 
 	public void setUseEngine(boolean useEngine) {
 		this.useEngine = useEngine;
 	}
 
 	public void complete(final IModuleSource cu, final int position, int i) {
 		this.requestor.beginReporting();
 		String content = cu.getSourceContents();
 		if (position < 0 || position > content.length()) {
 			return;
 		}
 
 		final TypeInferencer2 inferencer2 = new TypeInferencer2();
 		inferencer2.setModelElement(cu.getModelElement());
 
 		final Script script = JavaScriptParserUtil.parse(cu, null);
 		final NodeFinder nodeFinder = new NodeFinder(position, position);
 		nodeFinder.locate(script);
 		if (nodeFinder.getNode() instanceof StringLiteral) {
 			// don't complete inside string literals
 			return;
 		}
 		final PositionCalculator calculator = new PositionCalculator(content,
 				position, false);
 		final CompletionVisitor visitor = new CompletionVisitor(inferencer2,
 				position);
 		inferencer2.setVisitor(visitor);
 		if (cu instanceof org.eclipse.dltk.core.ISourceModule) {
 			inferencer2
 					.setModelElement((org.eclipse.dltk.core.ISourceModule) cu);
 		}
 		try {
 			inferencer2.doInferencing(script);
 		} catch (PositionReachedException e) {
 			// e.printStackTrace();
 		}
 		TypeInferencer2.withTypeSystem(inferencer2, new Runnable() {
 			public void run() {
 				final CompletionPath path = new CompletionPath(calculator
 						.getCompletion());
 				final Reporter reporter = new Reporter(path.lastSegment(),
 						position);
 				if (calculator.isMember() && !path.isEmpty()
 						&& path.lastSegment() != null) {
 					doCompletionOnMember(inferencer2, visitor.getCollection(),
 							path, reporter);
 				} else {
 					doGlobalCompletion(inferencer2, visitor.getCollection(),
 							reporter);
 				}
 			}
 		});
 		this.requestor.endReporting();
 	}
 
 	public void completeTypes(ISourceModule module, TypeMode mode,
 			String prefix, int offset) {
 		final TypeInferencer2 inferencer2 = new TypeInferencer2();
 		inferencer2.setModelElement(module);
 		doCompletionOnType(inferencer2, mode, new Reporter(prefix, offset));
 	}
 
 	private void doCompletionOnType(ITypeInferenceContext context,
 			TypeMode mode, Reporter reporter) {
 		Set<String> typeNames = context.listTypes(mode, reporter.getPrefix());
 		for (String typeName : typeNames) {
 			final Type type = context.getType(typeName);
 			if (type != null && type.isVisible()) {
 				reporter.reportTypeRef(type);
 			}
 		}
 	}
 
 	private static boolean exists(IValueParent item) {
 		if (item instanceof IValueReference) {
 			return ((IValueReference) item).exists();
 		} else {
 			return true;
 		}
 	}
 
 	/**
 	 * @param context
 	 * @param collection
 	 * @param startPart
 	 */
 	private void doCompletionOnMember(ITypeInferenceContext context,
 			IValueCollection collection, CompletionPath path, Reporter reporter) {
 		IValueParent item = collection;
 		for (int i = 0; i < path.segmentCount() - 1; ++i) {
 			if (path.isName(i)) {
 				final String segment = path.segment(i);
 				if ("this".equals(segment) && item instanceof IValueCollection) {
 					item = ((IValueCollection) item).getThis();
 				} else {
 					item = item.getChild(segment);
 				}
 				if (!exists(item))
 					break;
 			} else if (path.isFunction(i)) {
 				item = item.getChild(IValueReference.FUNCTION_OP);
 				if (!exists(item))
 					break;
 			} else if (path.isObject(i)) {
 				item = item.getChild(ITypeNames.OBJECT).getChild(
 						IValueReference.FUNCTION_OP);
 				break;
 			} else {
 				assert path.isArray(i);
 				item = item.getChild(IValueReference.ARRAY_OP);
 				if (!exists(item))
 					break;
 			}
 		}
 
 		if (item != null && exists(item)) {
 			reportItems(context, reporter, item, true);
 		}
 	}
 
 	protected void reportItems(ITypeInferenceContext context,
 			Reporter reporter, IValueParent item, boolean testPrivate) {
 		reporter.report(context, item, testPrivate);
 		if (item instanceof IValueCollection) {
 			IValueCollection coll = (IValueCollection) item;
 			for (;;) {
 				coll = coll.getParent();
 				if (coll == null)
 					break;
 				reporter.report(context, coll, testPrivate);
 			}
 		} else if (item instanceof IValueReference) {
 			reporter.reportValueTypeMembers(context, (IValueReference) item);
 		}
 	}
 
 	protected void reportGlobals(ITypeInferenceContext context,
 			Reporter reporter) {
 		final Set<String> globals = context.listGlobals(reporter.getPrefix());
 		for (String global : globals) {
 			if (reporter.canReport(global)) {
 				Member element = context.resolve(global);
 				if (element != null && element.isVisible()) {
 					reporter.report(global, element);
 				}
 			} else if (global.lastIndexOf('.') != -1) {
 				Type type = context.getType(global);
 				if (type != null && type.isVisible()
 						&& type.getKind() != TypeKind.UNKNOWN) {
 					reporter.reportTypeRef(type);
 				}
 
 			}
 		}
 	}
 
 	private class Reporter {
 
 		final char[] prefix;
 		private final String prefixStr;
 		final int position;
 		final Set<String> processed = new HashSet<String>();
 		final boolean camelCase = DLTKCore.ENABLED.equals(DLTKCore
 				.getOption(DLTKCore.CODEASSIST_CAMEL_CASE_MATCH));
 
 		public Reporter(String prefix, int position) {
 			this.prefixStr = prefix != null ? prefix : "";
 			this.prefix = prefixStr.toCharArray();
 			this.position = position;
 			setSourceRange(position - this.prefix.length, position);
 		}
 
 		public void ignore(String generatedIdentifier) {
 			processed.add(generatedIdentifier);
 		}
 
 		public String getPrefix() {
 			return prefixStr;
 		}
 
 		public int getPosition() {
 			return position;
 		}
 
 		public void report(String name, Element element) {
 			if (element instanceof Member && processed.add(name)) {
 				reportMember((Member) element, name, false);
 			}
 		}
 
 		public boolean canReport(String name) {
 			return matches(name) && !processed.contains(name);
 		}
 
 		private boolean matches(String name) {
 			return CharOperation.prefixEquals(prefix, name, false) || camelCase
 					&& CharOperation.camelCaseMatch(prefix, name.toCharArray());
 		}
 
 		public void report(ITypeInferenceContext context, IValueParent item,
 				boolean testPrivate) {
 			boolean superScope = false;
 			if (item instanceof IValueProvider) {
 				superScope = ((IValueProvider) item).getValue().getAttribute(
 						IReferenceAttributes.SUPER_SCOPE, false) == Boolean.TRUE;
 			}
 			final Set<String> deleted = item.getDeletedChildren();
 			for (String childName : item.getDirectChildren()) {
 				if (childName.equals(IValueReference.FUNCTION_OP))
 					continue;
 				if (!deleted.contains(childName) && matches(childName)
 						&& processed.add(childName)) {
 					IValueReference child = item.getChild(childName);
 					if (child.exists()) {
 						if (testPrivate) {
 							IRMethod method = (IRMethod) child
 									.getAttribute(IReferenceAttributes.R_METHOD);
 							IRVariable variable = (IRVariable) child
 									.getAttribute(IReferenceAttributes.R_VARIABLE);
 							if ((method != null && (method.isPrivate() || (method
 									.isProtected() && !superScope)))
 									|| (variable != null && (variable
 											.isPrivate() || (variable
 											.isProtected() && !superScope)))) {
 								continue;
 							}
 						} else if (child
 								.getAttribute(IReferenceAttributes.PRIVATE) == Boolean.TRUE)
 							continue;
 						reportReference(child);
 					}
 				}
 			}
 		}
 
 		public void reportValueTypeMembers(ITypeInferenceContext context,
 				IValueReference valueRef) {
 			final TypeMemberQuery typeQuery = new TypeMemberQuery();
			final List<Member> members = new ArrayList<Member>();
 			collectTypes(context, valueRef.getDeclaredTypes(), typeQuery,
 					members);
 			collectTypes(context, valueRef.getTypes(), typeQuery, members);
 			for (Member member : members) {
 				reportMember(member, member.getName(), true);
 			}
 			for (Member member : typeQuery.ignoreDuplicates(processed)) {
 				if (member.isVisible()
 						&& CharOperation.prefixEquals(prefix, member.getName(),
 								false)) {
 					reportMember(member, member.getName(),
 							typeQuery.contains(member.getDeclaringType()));
 				}
 			}
 		}
 
 		protected void collectTypes(final ITypeInferenceContext context,
 				final JSTypeSet types, final TypeMemberQuery typeQuery,
				final List<Member> members) {
 			for (IRType type : types) {
 				if (type instanceof IRClassType) {
 					final Type t = ((IRClassType) type).getTarget();
 					if (t != null) {
 						typeQuery.add(t, t.memberPredicateFor(type,
 								MemberPredicate.STATIC));
 					}
 				} else if (type instanceof IRSimpleType) {
 					final Type t = ((IRSimpleType) type).getTarget();
 					if (t != null) {
 						typeQuery.add(t, t.memberPredicateFor(type,
 								MemberPredicate.NON_STATIC));
 					}
 				} else if (type instanceof IRRecordType) {
 					for (IRRecordMember member : ((IRRecordType) type)
 							.getMembers()) {
 						members.add(member.getMember());
 					}
 				} else {
 					final Type t = TypeUtil.extractType(context, type);
 					if (t != null) {
 						typeQuery.add(t, MemberPredicate.NON_STATIC);
 					}
 				}
 			}
 		}
 
 		/**
 		 * @param member
 		 */
 		private void reportMember(Member member, String memberName,
 				boolean important) {
 			boolean isFunction = member instanceof Method;
 			CompletionProposal proposal = CompletionProposal.create(
 					isFunction ? CompletionProposal.METHOD_REF
 							: CompletionProposal.FIELD_REF, position);
 
 			int relevance = computeBaseRelevance();
 			// if (important) {
 			// relevance += RelevanceConstants.R_NON_INHERITED;
 			// }
 			relevance += computeRelevanceForInterestingProposal();
 			relevance += computeRelevanceForCaseMatching(prefix, memberName);
 			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
 			proposal.setRelevance(relevance);
 
 			proposal.setCompletion(memberName);
 			proposal.setName(memberName);
 			proposal.setExtraInfo(member);
 			proposal.setReplaceRange(startPosition - offset, endPosition
 					- offset);
 			if (isFunction) {
 				Method method = (Method) member;
 				int paramCount = method.getParameters().size();
 				if (paramCount > 0) {
 					final String[] params = new String[paramCount];
 					for (int i = 0; i < paramCount; ++i) {
 						Parameter parameter = method.getParameters().get(i);
 						if (parameter.getKind() == ParameterKind.OPTIONAL) {
 							params[i] = '[' + parameter.getName() + ']';
 						} else {
 							params[i] = parameter.getName();
 						}
 					}
 					proposal.setParameterNames(params);
 				}
 			}
 			requestor.accept(proposal);
 		}
 
 		/**
 		 * @param reference
 		 */
 		private void reportReference(IValueReference reference) {
 			int proposalKind = CompletionProposal.FIELD_REF;
 			final ReferenceKind kind = reference.getKind();
 			if (reference.getAttribute(IReferenceAttributes.PHANTOM, true) == null
 					&& (kind == ReferenceKind.FUNCTION || reference
 							.hasChild(IValueReference.FUNCTION_OP))) {
 				proposalKind = CompletionProposal.METHOD_REF;
 			} else if (kind == ReferenceKind.LOCAL) {
 				proposalKind = CompletionProposal.LOCAL_VARIABLE_REF;
 			}
 			CompletionProposal proposal = CompletionProposal.create(
 					proposalKind, position);
 
 			int relevance = computeBaseRelevance();
 			relevance += computeRelevanceForInterestingProposal();
 			relevance += computeRelevanceForCaseMatching(prefix,
 					reference.getName());
 			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
 			proposal.setRelevance(relevance);
 
 			proposal.setCompletion(reference.getName());
 			proposal.setName(reference.getName());
 			proposal.setExtraInfo(reference);
 			proposal.setReplaceRange(startPosition - offset, endPosition
 					- offset);
 			if (proposalKind == CompletionProposal.METHOD_REF) {
 				final IRMethod method = (IRMethod) reference.getAttribute(
 						IReferenceAttributes.R_METHOD, true);
 				if (method != null) {
 					int paramCount = method.getParameterCount();
 					if (paramCount > 0) {
 						final String[] params = new String[paramCount];
 						for (int i = 0; i < paramCount; ++i) {
 							IRParameter parameter = method.getParameters().get(
 									i);
 							if (parameter.isOptional()) {
 								params[i] = '[' + parameter.getName() + ']';
 							} else {
 								params[i] = parameter.getName();
 							}
 						}
 						proposal.setParameterNames(params);
 					}
 				}
 			}
 			requestor.accept(proposal);
 		}
 
 		public void reportTypeRef(Type type) {
 			if (!processed.add(type.getName())) {
 				return;
 			}
 			CompletionProposal proposal = CompletionProposal.create(
 					CompletionProposal.TYPE_REF, position);
 			int relevance = computeBaseRelevance();
 			relevance += computeRelevanceForInterestingProposal();
 			relevance += computeRelevanceForCaseMatching(prefix, type.getName());
 			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
 			proposal.setRelevance(relevance);
 			proposal.setCompletion(type.getName());
 			proposal.setName(type.getName());
 			proposal.setExtraInfo(type);
 			proposal.setReplaceRange(startPosition - offset, endPosition
 					- offset);
 			requestor.accept(proposal);
 		}
 
 	}
 
 	/**
 	 * @param context
 	 * @param collection
 	 * @param reporter
 	 */
 	private void doGlobalCompletion(ITypeInferenceContext context,
 			IValueCollection collection, Reporter reporter) {
 		reportItems(context, reporter, collection, false);
 		if (useEngine) {
 			doCompletionOnType(context, TypeMode.CODE, reporter);
 			doCompletionOnKeyword(reporter.getPrefix(), reporter.getPosition());
 			reportGlobals(context, reporter);
 		}
 	}
 
 	private void doCompletionOnKeyword(String startPart, int position) {
 		setSourceRange(position - startPart.length(), position);
 		String[] keywords = JavaScriptKeywords.getJavaScriptKeywords();
 		findKeywords(startPart.toCharArray(), keywords, true);
 	}
 
 }
