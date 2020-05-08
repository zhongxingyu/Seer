 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.javascript.jdt.integration;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.internal.javascript.reference.resolvers.IReferenceResolver;
 import org.eclipse.dltk.internal.javascript.reference.resolvers.IResolvableReference;
 import org.eclipse.dltk.internal.javascript.reference.resolvers.ReferenceResolverContext;
 import org.eclipse.dltk.internal.javascript.typeinference.AbstractCallResultReference;
 import org.eclipse.dltk.internal.javascript.typeinference.CallResultReference;
import org.eclipse.dltk.internal.javascript.typeinference.IClassReference;
 import org.eclipse.dltk.internal.javascript.typeinference.IReference;
 import org.eclipse.dltk.internal.javascript.typeinference.NewReference;
 import org.eclipse.dltk.internal.javascript.typeinference.UncknownReference;
 import org.eclipse.jdt.core.CompletionProposal;
 import org.eclipse.jdt.core.CompletionRequestor;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.eval.IEvaluationContext;
 import org.eclipse.jdt.core.search.IJavaSearchConstants;
 import org.eclipse.jdt.core.search.IJavaSearchScope;
 import org.eclipse.jdt.core.search.SearchEngine;
 import org.eclipse.jdt.core.search.SearchMatch;
 import org.eclipse.jdt.core.search.SearchPattern;
 import org.eclipse.jdt.core.search.SearchRequestor;
 
 public class JdtReferenceResolver implements IExecutableExtension,
 		IReferenceResolver {
 
	private static final class ClassRef extends UncknownReference implements
			IClassReference {
		private ClassRef(String paramOrVarName, boolean childIsh) {
			super(paramOrVarName, childIsh);
		}
	}

 	private static final String PACKAGES = "Packages.";
 	ArrayList imports = new ArrayList();
 
 	public void evaluate(IProject pr) {
 		JavaCore.create(pr);
 	}
 
 	public void setInitializationData(IConfigurationElement config,
 			String propertyName, Object data) throws CoreException {
 
 	}
 
 	public boolean canResolve(ISourceModule module) {
 		IProject pr = module.getResource().getProject();
 		return JavaCore.create(pr).exists();
 	}
 
	public Set getChilds(final IResolvableReference ref) {
 		if (ref instanceof AbstractCallResultReference) {
 			final AbstractCallResultReference cm = (AbstractCallResultReference) ref;
 			if (cm instanceof NewReference) {
 				String preId = cm.getId();
 				final HashSet result = new HashSet();
 				if (preId.startsWith(PACKAGES))
 					preId = preId.substring(PACKAGES.length());
 				IJavaSearchScope createJavaSearchScope = SearchEngine
 						.createJavaSearchScope(new IJavaElement[] { create });
 				SearchPattern createPattern = SearchPattern.createPattern(
 						"jsFunction*", IJavaSearchConstants.METHOD, 100,
 						SearchPattern.R_PATTERN_MATCH);
 				try {
 
 					engine
 							.search(
 									createPattern,
 									new org.eclipse.jdt.core.search.SearchParticipant[] { engine
 											.getDefaultSearchParticipant() },
 									createJavaSearchScope,
 									new SearchRequestor() {
 
 										public void acceptSearchMatch(
 												SearchMatch match)
 												throws CoreException {
 											Object element = match.getElement();
 
 											if (element instanceof IMethod) {
 												IMethod m = (IMethod) element;
												IType declaringType = m
														.getDeclaringType();
												if (!declaringType
														.getElementName()
														.equals(cm.getId()))
													return;
												IMethod[] ts = declaringType
 														.getMethods();
 												for (int a = 0; a < ts.length; a++) {
 													String string = "jsFunction_";
 													String stringget = "jsGet_";
 													String stringset = "jsSet_";
 													IMethod method = ts[a];
 													if (method.getElementName()
 															.startsWith(string)) {
 														UncknownReference r = new UncknownReference(
 																method
 																		.getElementName()
 																		.substring(
 																				string
 																						.length()),
 																true);
 
 														result.add(r);
 														r.setFunctionRef();
 													} else if (method
 															.getElementName()
 															.startsWith(
 																	stringget)) {
 														IReference r = new UncknownReference(
 																method
 																		.getElementName()
 																		.substring(
 																				stringget
 																						.length()),
 																true);
 														result.add(r);
 													} else if (method
 															.getElementName()
 															.startsWith(
 																	stringset)) {
 														IReference r = new UncknownReference(
 																method
 																		.getElementName()
 																		.substring(
 																				stringset
 																						.length()),
 																true);
 														result.add(r);
 													}
 												}
 											}
 										}
 
 									}, null);
 				} catch (CoreException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				final String cmid = preId;
 				if (!result.isEmpty())
 					return result;
 				String string = cmid + " z=new " + cmid + ";z.";
 				try {
 
 					context.codeComplete(string, string.length(),
 							new CompletionRequestor() {
 								public void accept(CompletionProposal proposal) {
 									IReference r = new JavaProposalReference(
 											context, proposal, owner, create,
 											cmid);
 									result.add(r);
 
 								}
 							});
 					return result;
 				} catch (JavaModelException e) {
 					return null;
 				}
 			}
 			if (cm instanceof CallResultReference) {
 				CallResultReference call = (CallResultReference) cm;
 				IReference rm = call.getRoot();
 				if (rm != null) {
 					int lastIndexOf = call.getId().lastIndexOf('.');
 					String substring = call.getId().substring(lastIndexOf + 1);
 					IReference child = rm.getChild(substring, true);
 					if (child == null)
 						return null;
 					return child.getChilds(true);
 				}
 			}
 		}
 		return null;
 	}
 
 	public Set resolveGlobals(String id) {
 
 		final HashSet result = new HashSet();
 		if (id.startsWith(PACKAGES))
 			id = id.substring(PACKAGES.length());
 		final String id2 = id;
 
 		String sm = id;
 
 		int indexOf = id.indexOf('.');
 
 		if (indexOf == -1) {
 			sm = "import " + sm;
 			IJavaSearchScope createJavaSearchScope = SearchEngine
 					.createJavaSearchScope(new IJavaElement[] { create });
 			SearchPattern createPattern = SearchPattern.createPattern(
 					"jsFunction*", IJavaSearchConstants.METHOD, 100,
 					SearchPattern.R_PATTERN_MATCH);
 			try {
 				engine
 						.search(
 								createPattern,
 								new org.eclipse.jdt.core.search.SearchParticipant[] { engine
 										.getDefaultSearchParticipant() },
 								createJavaSearchScope, new SearchRequestor() {
 
 									public void acceptSearchMatch(
 											SearchMatch match)
 											throws CoreException {
 										Object element = match.getElement();
 
 										if (element instanceof IMethod) {
 											IMethod m = (IMethod) element;
 											String elementName = m
 													.getDeclaringType()
 													.getElementName();
											IReference r = new ClassRef(
 													elementName, true);
 											result.add(r);
 										}
 									}
 
 								}, null);
 			} catch (CoreException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
			if (result.size() > 0)
				return result;
 			try {
 				context.codeComplete(sm, sm.length(),
 						new CompletionRequestor() {
 
 							public void accept(CompletionProposal proposal) {
 								if (proposal.getName() != null) {
 									IReference r = new JavaProposalReference(
 											context, proposal, owner, create,
 											"");
 									result.add(r);
 								} else {
 									char[] completion = proposal
 											.getCompletion();
 									String sm = new String(completion);
 									IReference r = new JavaProposalReference(
 											context, sm, proposal, owner,
 											create, "");
 									result.add(r);
 								}
 							}
 
 						});
 			} catch (JavaModelException e) {
 
 			}
 		} else {
 			try {
 				context.codeComplete(sm, sm.length(),
 						new CompletionRequestor() {
 
 							public void accept(CompletionProposal proposal) {
 								if (proposal.getName() != null) {
 									IReference r = new JavaProposalReference(
 											context, proposal, owner, create,
 											"");
 									result.add(r);
 								} else {
 
 									char[] completion = proposal
 											.getCompletion();
 									String sm = new String(completion);
 									String pName = sm;
 									if (proposal.getKind() == CompletionProposal.PACKAGE_REF) {
 										sm = sm.substring(id2.length());
 									} else {
 										if (sm.startsWith(id2)
 												&& sm.length() != id2.length())
 											sm = sm.substring(id2.length())
 													.trim();
 
 									}
 									IReference r = new JavaProposalReference(
 											context, sm, proposal, owner,
 											create, pName);
 									result.add(r);
 								}
 							}
 
 						});
 			} catch (JavaModelException e) {
 
 			}
 		}
 		return result;
 	}
 
 	public void processCall(String call, String objId) {
 		if (call.equals("importPackage"))
 			imports.add(objId);
 	}
 
 	ReferenceResolverContext owner;
 	IEvaluationContext context;
 	IJavaProject create;
 	SearchEngine engine = new SearchEngine();
 
 	public void init(ReferenceResolverContext owner) {
 		ISourceModule module = owner.getModule();
 		IProject pr = module.getResource().getProject();
 		create = JavaCore.create(pr);
 		context = create.newEvaluationContext();
 		String[] imps = new String[imports.size()];
 		for (int a = 0; a < imps.length; a++) {
 			imps[a] = imports.get(a).toString() + ".*";
 		}
 		context.setImports(imps);
 		this.owner = owner;
 	}
 
 }
