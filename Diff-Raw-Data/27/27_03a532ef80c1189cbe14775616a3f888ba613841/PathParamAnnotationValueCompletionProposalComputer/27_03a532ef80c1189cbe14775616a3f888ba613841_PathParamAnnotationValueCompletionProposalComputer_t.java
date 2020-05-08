 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Xavier Coulon - Initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.ws.jaxrs.ui.contentassist;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IMember;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.IAnnotationBinding;
 import org.eclipse.jdt.ui.text.IJavaPartitions;
 import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
 import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
 import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentExtension3;
 import org.eclipse.jface.text.IDocumentPartitioner;
 import org.eclipse.jface.text.ITypedRegion;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.jface.viewers.StyledString;
 import org.eclipse.swt.graphics.Image;
 import org.jboss.tools.ws.jaxrs.core.utils.JdtUtils;
 import org.jboss.tools.ws.jaxrs.ui.JBossJaxrsUIPlugin;
 import org.jboss.tools.ws.jaxrs.ui.internal.utils.Logger;
 
 /**
  * Computes proposals for <code>java.ws.rs.PathParam</code> annotation values in
  * the compilation unit context.
  * 
  * @author xcoulon
  * 
  */
 public class PathParamAnnotationValueCompletionProposalComputer implements IJavaCompletionProposalComputer {
 
 	/** Icon for completion proposals. */
	private final Image icon = JBossJaxrsUIPlugin.getDefault().createImage("url_mapping.gif");
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void sessionStarted() {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void sessionEnded() {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public final List<ICompletionProposal> computeCompletionProposals(final ContentAssistInvocationContext context,
 			final IProgressMonitor monitor) {
 		JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
 		try {
 			CompilationUnit compilationUnit = resolveContextualCompilationUnit(monitor, javaContext);
 			IJavaElement invocationElement = javaContext.getCompilationUnit().getElementAt(
 					context.getInvocationOffset());
 			if (invocationElement.getElementType() == IJavaElement.METHOD) {
 				IAnnotationBinding annotationBinding = resolveContextualAnnotationBinding(javaContext, compilationUnit);
 				// completion proposal on @PathParam method annotation
 				if (annotationBinding != null
 						&& PathParam.class.getName().equals(
 								JdtUtils.resolveAnnotationFullyQualifiedName(annotationBinding))) {
 					return internalComputePathParamProposals(javaContext, annotationBinding,
 							(IMethod) invocationElement, compilationUnit);
 				}
 			}
 		} catch (Exception e) {
 			Logger.error("Failed to compute completion proposal", e);
 		}
 		return Collections.emptyList();
 	}
 
 	/**
 	 * Computes the valid proposals for the <code>javax.ws.rs.PathParam</code>
 	 * annotation value. The proposals are based on:
 	 * <ul>
 	 * <li>The values of the <code>javax.ws.rs.Path</code> annotations, both at
 	 * the method and at the type level (inclusion),</li>
 	 * <li>The values of the sibling <code>javax.ws.rs.PathParam</code>
 	 * annotations (exclusion).
 	 * </ul>
 	 * 
 	 * @param javaContext
 	 *            the invocation context
 	 * @param pathParamAnnotationBinding
 	 *            the annotation bindings
 	 * @param method
 	 *            the enclosing java method
 	 * @param compilationUnit
 	 *            the compilation unit (AST3)
 	 * @return the list of computed completion proposals
 	 * @throws CoreException
 	 *             in case of underlying exception
 	 * @throws BadLocationException
	 * @throws org.eclipse.jface.text.BadLocationException
 	 */
 	private List<ICompletionProposal> internalComputePathParamProposals(JavaContentAssistInvocationContext javaContext,
 			IAnnotationBinding pathParamAnnotationBinding, IMethod method, CompilationUnit compilationUnit)
 			throws CoreException, BadLocationException {
 		ITypedRegion region = getRegion(javaContext);
 		String matchValue = javaContext.getDocument().get(region.getOffset(),
 				javaContext.getInvocationOffset() - region.getOffset());
 		// int cursorPosition = javaContext.getInvocationOffset();
 		List<ICompletionProposal> completionProposals = new ArrayList<ICompletionProposal>();
 		// compute proposals from @Path annotations on the method
 		completionProposals.addAll(generateCompletionProposal(method, compilationUnit, region, matchValue));
 		// compute proposals from @Path annotations on the type
 		completionProposals.addAll(generateCompletionProposal(method.getDeclaringType(), compilationUnit, region,
 				matchValue));
 		return completionProposals;
 	}
 
	private List<ICompletionProposal> generateCompletionProposal(IMember member, CompilationUnit compilationUnit,
			ITypedRegion region, String matchValue) throws CoreException {
 		List<ICompletionProposal> completionProposals = new ArrayList<ICompletionProposal>();
		String pathAnnotationValue = (String) JdtUtils.resolveAnnotationAttributeValue(member, compilationUnit,
				Path.class, "value");
 		if (pathAnnotationValue != null && pathAnnotationValue.contains("{") && pathAnnotationValue.contains("}")) {
 			List<String> uriParams = extractParamsFromUriTemplateFragment(pathAnnotationValue);
			for (String uriParam : uriParams) {
 				String replacementValue = "\"" + uriParam + "\"";
 				if (replacementValue.startsWith(matchValue)) {
 					String displayString = uriParam + " - JAX-RS Mapping";
 					StyledString displayStyledString = new StyledString(displayString);
					displayStyledString.setStyle(uriParam.length(), displayString.length() - uriParam.length(),
							StyledString.QUALIFIER_STYLER);
 					completionProposals.add(new AnnotationCompletionProposal(replacementValue, displayStyledString,
 							region, icon, member, compilationUnit));
 				}
 			}
 		}
 		return completionProposals;
 	}

 	private static List<String> extractParamsFromUriTemplateFragment(String fragment) {
 		List<String> params = new ArrayList<String>();
 		int beginIndex = -1;
 		while ((beginIndex = fragment.indexOf("{", beginIndex + 1)) != -1) {
 			int semicolonIndex = fragment.indexOf(":", beginIndex);
 			int closingCurlyBraketIndex = fragment.indexOf("}", beginIndex);
 			int endIndex = semicolonIndex != -1 ? semicolonIndex : closingCurlyBraketIndex;
 			params.add(fragment.substring(beginIndex + 1, endIndex).trim());
 		}
 		return params;
 	}
 
 	/**
 	 * @param javaContext
 	 * @param unit
 	 * @return
 	 */
 	private IAnnotationBinding resolveContextualAnnotationBinding(JavaContentAssistInvocationContext javaContext,
 			CompilationUnit unit) {
 		TypedRegionVisitor visitor = new TypedRegionVisitor(javaContext.getInvocationOffset());
 		unit.accept(visitor);
 		return visitor.getBinding();
 	}
 
 	/**
 	 * @param monitor
 	 * @param javaContext
 	 * @return
 	 * @throws JavaModelException
 	 */
 	private CompilationUnit resolveContextualCompilationUnit(final IProgressMonitor monitor,
 			JavaContentAssistInvocationContext javaContext) throws JavaModelException {
 		IType type = getEnclosingType(javaContext.getCompilationUnit().getElementAt(javaContext.getInvocationOffset()));
 		CompilationUnit unit = JdtUtils.parse(type, monitor);
 		return unit;
 	}
 
 	/**
 	 * Resolves the typed region for the given java content assist invocation
 	 * context.
 	 * 
 	 * @param javaContext
 	 *            the java content assist invocation context
 	 * @return the typed region
 	 */
 	private ITypedRegion getRegion(final JavaContentAssistInvocationContext javaContext) {
 		IDocument document = javaContext.getDocument();
 		IDocumentPartitioner documentPartitioner = ((IDocumentExtension3) document)
 				.getDocumentPartitioner(IJavaPartitions.JAVA_PARTITIONING);
 		return documentPartitioner.getPartition(javaContext.getInvocationOffset());
 	}
 
 	/**
 	 * Returns the enclosing java type for the given java element.
 	 * 
 	 * @param element
 	 *            the element
 	 * @return the enclosing type, or null if the given element is neither a
 	 *         method nor the type itself
 	 * @throws JavaModelException
 	 *             in case of underlying exception
 	 */
 	private IType getEnclosingType(final IJavaElement element) throws JavaModelException {
 		switch (element.getElementType()) {
 		case IJavaElement.TYPE:
 			return (IType) element;
 		case IJavaElement.METHOD:
 			return (IType) element.getParent();
 		default:
 			break;
 		}
 		Logger.error("Unexpected element type for " + element.getElementName() + ": " + element.getElementType());
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public final List<IContextInformation> computeContextInformation(final ContentAssistInvocationContext context,
 			final IProgressMonitor monitor) {
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public final String getErrorMessage() {
 		return null;
 	}
 
 }
