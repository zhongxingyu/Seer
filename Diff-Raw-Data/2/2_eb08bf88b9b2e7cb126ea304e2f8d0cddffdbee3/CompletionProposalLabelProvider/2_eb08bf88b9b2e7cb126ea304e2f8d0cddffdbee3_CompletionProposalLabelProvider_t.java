 /*******************************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.ui.text.completion;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.core.CompletionContext;
 import org.eclipse.dltk.core.CompletionProposal;
 import org.eclipse.dltk.core.Flags;
 import org.eclipse.dltk.core.Signature;
 import org.eclipse.dltk.internal.corext.template.completion.SignatureUtil;
 import org.eclipse.dltk.ui.DLTKPluginImages;
 import org.eclipse.dltk.ui.ScriptElementImageDescriptor;
 import org.eclipse.dltk.ui.ScriptElementImageProvider;
 import org.eclipse.dltk.ui.ScriptElementLabels;
 import org.eclipse.jface.resource.ImageDescriptor;
 
 /**
  * Provides labels forscriptcontent assist proposals. The functionality is
  * similar to the one provided by {@link org.eclipse.dltk.ui.ModelElementLabels},
  * but based on signatures and {@link CompletionProposal}s.
  * 
  * @see Signature
  * 
  */
 public abstract class CompletionProposalLabelProvider {
 
 	/**
 	 * The completion context.
 	 */
 	private CompletionContext fContext;
 
 	/**
 	 * Creates a new label provider.
 	 */
 	public CompletionProposalLabelProvider() {
 	}
 
 	/**
 	 * Creates and returns a parameter list of the given method proposal
 	 * suitable for display. The list does not include parentheses. The lower
 	 * bound of parameter types is returned.
 	 * <p>
 	 * Examples:
 	 * 
 	 * <pre>
 	 *    &quot;void method(int i, Strings)&quot; -&gt; &quot;int i, String s&quot;
 	 *    &quot;? extends Number method(java.lang.String s, ? super Number n)&quot; -&gt; &quot;String s, Number n&quot;
 	 * </pre>
 	 * 
 	 * </p>
 	 * 
 	 * @param methodProposal
 	 *            the method proposal to create the parameter list for. Must be
 	 *            of kind {@link CompletionProposal#METHOD_REF}.
 	 * @return the list of comma-separated parameters suitable for display
 	 */
 	public String createParameterList(CompletionProposal methodProposal) {
 		Assert
 				.isTrue(methodProposal.getKind() == CompletionProposal.METHOD_REF);
 		return appendUnboundedParameterList(new StringBuffer(), methodProposal)
 				.toString();
 	}
 
 	/**
 	 * Appends the parameter list to <code>buffer</code>. See
 	 * <code>createUnboundedParameterList</code> for details.
 	 * 
 	 * @param buffer
 	 *            the buffer to append to
 	 * @param methodProposal
 	 *            the method proposal
 	 * @return the modified <code>buffer</code>
 	 */
 	protected StringBuffer appendUnboundedParameterList(StringBuffer buffer,
 			CompletionProposal methodProposal) {
 		// TODO remove once https://bugs.eclipse.org/bugs/show_bug.cgi?id=85293
 		// gets fixed.
 		char[][] parameterNames = methodProposal.findParameterNames(null);
 		char[][] parameterTypes = null;
 		// for (int i= 0; i < parameterTypes.length; i++) {
 		// parameterTypes[i]=
 		// createTypeDisplayName(SignatureUtil.getLowerBound(parameterTypes[i]));
 		// }
 		return appendParameterSignature(buffer, parameterTypes, parameterNames);
 	}
 
 	/**
 	 * Returns the display string for ascripttype signature.
 	 * 
 	 * @param typeSignature
 	 *            the type signature to create a display name for
 	 * @return the display name for <code>typeSignature</code>
 	 * @throws IllegalArgumentException
 	 *             if <code>typeSignature</code> is not a valid signature
 	 * @see Signature#toCharArray(char[])
 	 * @see Signature#getSimpleName(char[])
 	 */
 	private char[] createTypeDisplayName(char[] typeSignature)
 			throws IllegalArgumentException {
 		// char[] displayName=
 		// Signature.getSimpleName(Signature.toCharArray(typeSignature));
 
 		// return displayName;
 		return null;
 	}
 
 	/**
 	 * Creates a display string of a parameter list (without the parentheses)
 	 * for the given parameter types and names.
 	 * 
 	 * @param parameterTypes
 	 *            the parameter types
 	 * @param parameterNames
 	 *            the parameter names
 	 * @return the display string of the parameter list defined by the passed
 	 *         arguments
 	 */
 	protected StringBuffer appendParameterSignature(StringBuffer buffer,
 			char[][] parameterTypes, char[][] parameterNames) {		
 		if (parameterNames != null) {
 			for (int i = 0; i < parameterNames.length; i++) {
 				if (i > 0) {
 					buffer.append(',');
 					buffer.append(' ');
 				}
 				buffer.append(parameterNames[i]);
 			}
 		}
 		return buffer;
 	}
 
 	/**
 	 * Creates a display label for the given method proposal. The display label
 	 * consists of:
 	 * <ul>
 	 * <li>the method name</li>
 	 * <li>the parameter list (see
 	 * {@link #createParameterList(CompletionProposal)})</li>
 	 * <li>the upper bound of the return type (see
 	 * {@link SignatureUtil#getUpperBound(String)})</li>
 	 * <li>the raw simple name of the declaring type</li>
 	 * </ul>
 	 * <p>
 	 * Examples: For the <code>get(int)</code> method of a variable of type
 	 * <code>List<? extends Number></code>, the following display name is
 	 * returned: <code>get(int index)  Number - List</code>.<br>
 	 * For the <code>add(E)</code> method of a variable of type
 	 * returned: <code>add(Number o)  void - List</code>.<br>
 	 * <code>List<? super Number></code>, the following display name is
 	 * </p>
 	 * 
 	 * @param methodProposal
 	 *            the method proposal to display
 	 * @return the display label for the given method proposal
 	 */
 	protected String createMethodProposalLabel(CompletionProposal methodProposal) {
 		StringBuffer nameBuffer = new StringBuffer();
 
 		// method name
 		nameBuffer.append(methodProposal.getName());
 
 		// parameters
 		nameBuffer.append('(');
 		appendUnboundedParameterList(nameBuffer, methodProposal);
 		nameBuffer.append(')');
 
 		// return type
 		if (!methodProposal.isConstructor()) {
 			// TODO remove SignatureUtil.fix83600 call when bugs are fixed
 			// char[] returnType=
 			// createTypeDisplayName(methodProposal.getSignature());
 			// nameBuffer.append(" "); //$NON-NLS-1$
 			// nameBuffer.append(returnType);
 		}
 
 		// declaring type
 		// nameBuffer.append(" - "); //$NON-NLS-1$
 		// String declaringType= extractDeclaringTypeFQN(methodProposal);
 		// declaringType= Signature.getSimpleName(declaringType);
 		// nameBuffer.append(declaringType);
 
 		return nameBuffer.toString();
 	}
 
 	protected String createOverrideMethodProposalLabel(
 			CompletionProposal methodProposal) {
 		StringBuffer nameBuffer = new StringBuffer();
 
 		// method name
 		nameBuffer.append(methodProposal.getName());
 
 		// parameters
 		nameBuffer.append('(');
 		appendUnboundedParameterList(nameBuffer, methodProposal);
 		nameBuffer.append(")  "); //$NON-NLS-1$
 
 		// return type
 		// TODO remove SignatureUtil.fix83600 call when bugs are fixed
 		// char[] returnType=
 		// createTypeDisplayName(methodProposal.getSignature());
 		// nameBuffer.append(returnType);
 
 		// declaring type
 		// nameBuffer.append(" - "); //$NON-NLS-1$
 
 		return nameBuffer.toString();
 	}
 
 	/**
 	 * Creates a display label for a given type proposal. The display label
 	 * consists of:
 	 * <ul>
 	 * <li>the simple type name (erased when the context is in javadoc)</li>
 	 * <li>the package name</li>
 	 * </ul>
 	 * <p>
 	 * Examples: A proposal for the generic type
 	 * <code>java.util.List&lt;E&gt;</code>, the display label is:
 	 * <code>List<E> - java.util</code>.
 	 * </p>
 	 * 
 	 * @param typeProposal
 	 *            the method proposal to display
 	 * @return the display label for the given type proposal
 	 */
 	protected String createTypeProposalLabel(CompletionProposal typeProposal) {
 		char[] signature;
 		signature = typeProposal.getSignature();
 		char[] fullName = Signature.toCharArray(signature);
 		return createTypeProposalLabel(fullName);
 	}
 
 	String createScriptdocSimpleProposalLabel(CompletionProposal proposal) {
 		// TODO get rid of this
 		return createSimpleLabel(proposal);
 	}
 
 	String createTypeProposalLabel(char[] fullName) {
 		// only display innermost type name as type name, using any
 		// enclosing types as qualification
 		int qIndex = findSimpleNameStart(fullName);
 
 		StringBuffer buf = new StringBuffer();
 		buf.append(fullName, qIndex, fullName.length - qIndex);
 		if (qIndex > 0) {
 			buf.append(ScriptElementLabels.CONCAT_STRING);
 			buf.append(fullName, 0, qIndex - 1);
 		}
 		return buf.toString();
 	}
 
 	private int findSimpleNameStart(char[] array) {
 		int lastDot = 0;
 		for (int i = 0, len = array.length; i < len; i++) {
 			char ch = array[i];
 			if (ch == '<') {
 				return lastDot;
 			} else if (ch == '.') {
 				lastDot = i + 1;
 			}
 		}
 		return lastDot;
 	}
 
 	String createSimpleLabelWithType(CompletionProposal proposal) {
 		StringBuffer buf = new StringBuffer();
 		buf.append(proposal.getCompletion());
 		char[] typeName = Signature.getSignatureSimpleName(proposal
 				.getSignature());
 		if (typeName.length > 0) {
 			buf.append("    "); //$NON-NLS-1$
 			buf.append(typeName);
 		}
 		return buf.toString();
 	}
 
 	String createLabelWithTypeAndDeclaration(CompletionProposal proposal) {
 		StringBuffer buf = new StringBuffer();
 		buf.append(proposal.getCompletion());
 		char[] typeName = Signature.getSignatureSimpleName(proposal
 				.getSignature());
 		if (typeName.length > 0) {
 			buf.append("    "); //$NON-NLS-1$
 			buf.append(typeName);
 		}
 		char[] declaration = proposal.getDeclarationSignature();
 		if (declaration != null) {
 			declaration = Signature.getSignatureSimpleName(declaration);
 			if (declaration.length > 0) {
 				buf.append(" - "); //$NON-NLS-1$
 				buf.append(declaration);
 			}
 		}
 
 		return buf.toString();
 	}
 
 	public String createSimpleLabel(CompletionProposal proposal) {
 		return String.valueOf(proposal.getCompletion());
 	}
 
 	/**
 	 * Creates the display label for a given <code>CompletionProposal</code>.
 	 * 
 	 * @param proposal
 	 *            the completion proposal to create the display label for
 	 * @return the display label for <code>proposal</code>
 	 */
 	public String createLabel(CompletionProposal proposal) {
 		switch (proposal.getKind()) {
 		case CompletionProposal.METHOD_NAME_REFERENCE:
 		case CompletionProposal.METHOD_REF:
 		case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
 			return createMethodProposalLabel(proposal);
 		case CompletionProposal.METHOD_DECLARATION:
 			return createOverrideMethodProposalLabel(proposal);
 		case CompletionProposal.TYPE_REF:
 			return createTypeProposalLabel(proposal);
 			// case CompletionProposal.JAVADOC_TYPE_REF:
 			// return createJavadocTypeProposalLabel(proposal);
 			// case CompletionProposal.JAVADOC_FIELD_REF:
 			// case CompletionProposal.JAVADOC_VALUE_REF:
 			// case CompletionProposal.JAVADOC_BLOCK_TAG:
 			// case CompletionProposal.JAVADOC_INLINE_TAG:
 			// case CompletionProposal.JAVADOC_PARAM_REF:
 			// return createJavadocSimpleProposalLabel(proposal);
 			// case CompletionProposal.JAVADOC_METHOD_REF:
 			// return createJavadocMethodProposalLabel(proposal);
 		case CompletionProposal.ANNOTATION_ATTRIBUTE_REF:
 		case CompletionProposal.FIELD_REF:
 			return createLabelWithTypeAndDeclaration(proposal);
 		case CompletionProposal.LOCAL_VARIABLE_REF:
 		case CompletionProposal.VARIABLE_DECLARATION:
 			return createSimpleLabelWithType(proposal);
 		case CompletionProposal.KEYWORD:
 		case CompletionProposal.PACKAGE_REF:
 		case CompletionProposal.LABEL_REF:
 			return createSimpleLabel(proposal);
 		default:
 			Assert.isTrue(false);
 			return null;
 		}
 	}
 
 	/**
 	 * Creates and returns a decorated image descriptor for a completion
 	 * proposal.
 	 * 
 	 * @param proposal
 	 *            the proposal for which to create an image descriptor
 	 * @return the created image descriptor, or <code>null</code> if no image
 	 *         is available
 	 */
 	public ImageDescriptor createImageDescriptor(CompletionProposal proposal) {
 		final int flags = proposal.getFlags();
 
 		ImageDescriptor descriptor;
 		switch (proposal.getKind()) {
 		case CompletionProposal.METHOD_DECLARATION:
 		case CompletionProposal.METHOD_NAME_REFERENCE:
 		case CompletionProposal.METHOD_REF:
 		case CompletionProposal.ANNOTATION_ATTRIBUTE_REF:
 		case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
			descriptor = ScriptElementImageProvider.getMethodImageDescriptor(proposal.getFlags());
 			break;
 		case CompletionProposal.TYPE_REF:
 			descriptor = DLTKPluginImages.DESC_OBJS_CLASSALT;
 			break;
 		case CompletionProposal.FIELD_REF:
 			descriptor = DLTKPluginImages.DESC_FIELD_DEFAULT;
 			break;
 		case CompletionProposal.LOCAL_VARIABLE_REF:
 		case CompletionProposal.VARIABLE_DECLARATION:
 			descriptor = DLTKPluginImages.DESC_OBJS_LOCAL_VARIABLE;
 			break;
 		case CompletionProposal.PACKAGE_REF:
 			descriptor = DLTKPluginImages.DESC_OBJS_PACKAGE;
 			break;
 		case CompletionProposal.KEYWORD:
 		case CompletionProposal.LABEL_REF:
 			descriptor = null;
 			break;
 		// case CompletionProposal.JAVADOC_METHOD_REF:
 		// case CompletionProposal.JAVADOC_TYPE_REF:
 		// case CompletionProposal.JAVADOC_FIELD_REF:
 		// case CompletionProposal.JAVADOC_VALUE_REF:
 		// case CompletionProposal.JAVADOC_BLOCK_TAG:
 		// case CompletionProposal.JAVADOC_INLINE_TAG:
 		// case CompletionProposal.JAVADOC_PARAM_REF:
 		// descriptor = JavaPluginImages.DESC_OBJS_JAVADOCTAG;
 		// break;
 		default:
 			descriptor = null;
 			Assert.isTrue(false);
 		}
 
 		if (descriptor == null)
 			return null;
 		return decorateImageDescriptor(descriptor, proposal);
 	}
 
 	public ImageDescriptor createMethodImageDescriptor(
 			CompletionProposal proposal) {
 		final int flags = proposal.getFlags();
 		return decorateImageDescriptor(ScriptElementImageProvider
 				.getMethodImageDescriptor(flags), proposal);
 	}
 
 	ImageDescriptor createTypeImageDescriptor(CompletionProposal proposal) {
 		final int flags = proposal.getFlags();
 		// boolean isInterfaceOrAnnotation= Flags.isInterface(flags) ||
 		// Flags.isAnnotation(flags);
 		return decorateImageDescriptor(ScriptElementImageProvider
 				.getTypeImageDescriptor(flags, false), proposal);
 	}
 
 	ImageDescriptor createFieldImageDescriptor(CompletionProposal proposal) {
 		final int flags = proposal.getFlags();
 		return decorateImageDescriptor(ScriptElementImageProvider
 				.getFieldImageDescriptor(flags), proposal);
 	}
 
 	ImageDescriptor createLocalImageDescriptor(CompletionProposal proposal) {
 		return decorateImageDescriptor(
 				DLTKPluginImages.DESC_OBJS_LOCAL_VARIABLE, proposal);
 	}
 
 	ImageDescriptor createPackageImageDescriptor(CompletionProposal proposal) {
 		return decorateImageDescriptor(DLTKPluginImages.DESC_OBJS_PACKAGE,
 				proposal);
 	}
 
 	/**
 	 * Returns a version of <code>descriptor</code> decorated according to the
 	 * passed <code>modifier</code> flags.
 	 * 
 	 * @param descriptor
 	 *            the image descriptor to decorate
 	 * @param proposal
 	 *            the proposal
 	 * @return an image descriptor for a method proposal
 	 * @see Flags
 	 */
 	private ImageDescriptor decorateImageDescriptor(ImageDescriptor descriptor,
 			CompletionProposal proposal) {
 		int adornments = 0;
 		int flags = proposal.getFlags();
 		int kind = proposal.getKind();
 
 		// if (Flags.isDeprecated(flags))
 		// adornments |= ModelElementImageDescriptor.DEPRECATED;
 
 		// if (kind == CompletionProposal.FIELD_REF || kind ==
 		// CompletionProposal.METHOD_DECLARATION || kind ==
 		// CompletionProposal.METHOD_DECLARATION || kind ==
 		// CompletionProposal.METHOD_NAME_REFERENCE || kind ==
 		// CompletionProposal.METHOD_REF)
 		// if (Flags.isStatic(flags))
 		// adornments |= ModelElementImageDescriptor.STATIC;
 
 		// if (kind == CompletionProposal.METHOD_DECLARATION || kind ==
 		// CompletionProposal.METHOD_DECLARATION || kind ==
 		// CompletionProposal.METHOD_NAME_REFERENCE || kind ==
 		// CompletionProposal.METHOD_REF)
 		// if (Flags.isSynchronized(flags))
 		// adornments |= ModelElementImageDescriptor.SYNCHRONIZED;
 
 		// if (kind == CompletionProposal.TYPE_REF && Flags.isAbstract(flags) &&
 		// !Flags.isInterface(flags))
 		// adornments |= ModelElementImageDescriptor.ABSTRACT;
 
 		return new ScriptElementImageDescriptor(descriptor, adornments,
 				ScriptElementImageProvider.SMALL_SIZE);
 	}
 
 	/**
 	 * Sets the completion context.
 	 * 
 	 * @param context
 	 *            the completion context
 	 * 
 	 */
 	void setContext(CompletionContext context) {
 		fContext = context;
 	}
 
 }
