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
 package descent.ui.text.java;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.text.Assert;
 
 import descent.core.CompletionContext;
 import descent.core.CompletionProposal;
 import descent.core.Flags;
 import descent.core.IMethod;
 import descent.core.Signature;
 import descent.core.compiler.CharOperation;
 import descent.internal.corext.template.java.SignatureUtil;
 import descent.internal.corext.util.Messages;
 import descent.internal.ui.JavaPluginImages;
 import descent.internal.ui.viewsupport.JavaElementImageProvider;
 import descent.ui.JavaElementImageDescriptor;
 import descent.ui.JavaElementLabels;
 
 
 /**
  * Provides labels for java content assist proposals. The functionality is
  * similar to the one provided by {@link descent.ui.JavaElementLabels},
  * but based on signatures and {@link CompletionProposal}s.
  *
  * @see Signature
  * @since 3.1
  */
 public class CompletionProposalLabelProvider {
 
 	/**
 	 * The completion context.
 	 * 
 	 * @since 3.2
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
 	 * <pre>
 	 *   &quot;void method(int i, Strings)&quot; -&gt; &quot;int i, String s&quot;
 	 *   &quot;? extends Number method(java.lang.String s, ? super Number n)&quot; -&gt; &quot;String s, Number n&quot;
 	 * </pre>
 	 * </p>
 	 *
 	 * @param methodProposal the method proposal to create the parameter list
 	 *        for. Must be of kind {@link CompletionProposal#METHOD_REF}.
 	 * @return the list of comma-separated parameters suitable for display
 	 */
 	public String createParameterList(CompletionProposal methodProposal) {
 		Assert.isTrue(methodProposal.getKind() == CompletionProposal.METHOD_REF ||
 				methodProposal.getKind() == CompletionProposal.OP_CALL ||
 				methodProposal.getKind() == CompletionProposal.FUNCTION_CALL ||
 				methodProposal.getKind() == CompletionProposal.TEMPLATE_REF ||
 				methodProposal.getKind() == CompletionProposal.TEMPLATED_AGGREGATE_REF ||
 				methodProposal.getKind() == CompletionProposal.TEMPLATED_FUNCTION_REF);
 		
 		StringBuffer sb1 = new StringBuffer();
 		
 		appendTemplateParameterList(sb1, methodProposal);
 		
 		StringBuffer sb2 = new StringBuffer();
 		if (methodProposal.getKind() == CompletionProposal.METHOD_REF ||
 				methodProposal.getKind() == CompletionProposal.OP_CALL ||
 				methodProposal.getKind() == CompletionProposal.FUNCTION_CALL ||
 				methodProposal.getKind() == CompletionProposal.TEMPLATED_FUNCTION_REF) {
 			appendUnboundedParameterList(sb2, methodProposal);
 			appendVarargs(sb2, methodProposal);
 		}
 		
 		if (sb1.length() > 0 && sb2.length() > 0) {
 			sb1.append(',');
 			sb1.append(' ');
 		}
 		
 		sb1.append(sb2);
 		
 		return sb1.toString();
 	}
 
 	/**
 	 * Appends the parameter list to <code>buffer</code>. See
 	 * <code>createUnboundedParameterList</code> for details.
 	 *
 	 * @param buffer the buffer to append to
 	 * @param methodProposal the method proposal
 	 * @return the modified <code>buffer</code>
 	 */
 	private StringBuffer appendUnboundedParameterList(StringBuffer buffer, CompletionProposal methodProposal) {
 		// TODO remove once https://bugs.eclipse.org/bugs/show_bug.cgi?id=85293
 		// gets fixed.
 		//char[] signature= SignatureUtil.fix83600(methodProposal.getSignature());
 		char[] signature= methodProposal.getTypeSignature();
 		if (signature == null) {
 			return buffer;
 		}
 		char[][] parameterNames= methodProposal.findParameterNames(null);
 		char[][] parameterDefaultValues = methodProposal.findParameterDefaultValues(null);
 		char[][] parameterTypes= Signature.getParameterTypes(signature);
 		for (int i= 0; i < parameterTypes.length; i++) {
 			parameterTypes[i]= createTypeDisplayName(parameterTypes[i]);
 		}
 		return appendParameterSignature(buffer, parameterTypes, parameterNames, parameterDefaultValues);
 	}
 	
 	private StringBuffer appendVarargs(StringBuffer buffer, CompletionProposal methodProposal) {
 		int varargs = Signature.getVariadic(methodProposal.getTypeSignature());
 		if (varargs != IMethod.VARARGS_NO) {
 			if (varargs == IMethod.VARARGS_UNDEFINED_TYPES) {
 				if (buffer.charAt(buffer.length() - 1) != '(') {
 					buffer.append(',');
 					buffer.append(' ');
 				}
 			}
 			buffer.append('.');
 			buffer.append('.');
 			buffer.append('.');
 		}
 		return buffer;
 	}
 	
 	private StringBuffer appendTemplateParameterList(StringBuffer buffer, CompletionProposal tempProposal) {
 		// TODO remove once https://bugs.eclipse.org/bugs/show_bug.cgi?id=85293
 		// gets fixed.
 		//char[] signature= SignatureUtil.fix83600(tempProposal.getSignature());
 		char[][] parameterNames= tempProposal.findTemplateParameterNames(null);
 		char[][] defaultValues = tempProposal.findTemplateParameterDefaultValues(null);
 		
 		for (int i = 0; i < parameterNames.length; i++) {
 			if (i != 0) {
 				buffer.append(',');
 				buffer.append(' ');
 			}
 			
 			buffer.append(parameterNames[i]);
 			
 			if (defaultValues != null && i < defaultValues.length && defaultValues[i] != null) {
 				buffer.append(' ');
 				buffer.append('=');
 				buffer.append(' ');
 				buffer.append(defaultValues[i]);
 			}
 		}
 		
 		return buffer;
 		
 //		char[][] parameterTypes= Signature.getParameterTypes(signature);
 //		for (int i= 0; i < parameterTypes.length; i++) {
 //			parameterTypes[i]= createTypeDisplayName(SignatureUtil.getLowerBound(parameterTypes[i]));
 //		}
 //		return appendParameterSignature(buffer, parameterTypes, parameterNames);
 	}
 
 	/**
 	 * Returns the display string for a java type signature.
 	 *
 	 * @param typeSignature the type signature to create a display name for
 	 * @return the display name for <code>typeSignature</code>
 	 * @throws IllegalArgumentException if <code>typeSignature</code> is not a
 	 *         valid signature
 	 * @see Signature#toCharArray(char[])
 	 * @see Signature#getSimpleName(char[])
 	 */
 	private char[] createTypeDisplayName(char[] typeSignature) throws IllegalArgumentException {
 		char[] prefix = null;
 		
 		if (typeSignature.length > 0) {
 			char c = typeSignature[0];
 			switch(c) {
 			case 'J':
 				prefix = new char[] { 'o', 'u', 't' };
 				typeSignature = CharOperation.subarray(typeSignature, 1, typeSignature.length);
 				break;
 			case 'K':
 				prefix = new char[] { 'l', 'a', 'z', 'y' };
 				typeSignature = CharOperation.subarray(typeSignature, 1, typeSignature.length);
 				break;
 			case 'L':
 				prefix = new char[] { 'r', 'e', 'f' };
 				typeSignature = CharOperation.subarray(typeSignature, 1, typeSignature.length);
 				break;
 			}
 		}
 		
 		char[] displayName= Signature.toCharArray(typeSignature, false /* don't fully qualify names */);
 		if (prefix != null) {
 			displayName = CharOperation.concat(prefix, displayName, ' ');
 		}
 		
 		return displayName;
 	}
 
 	/**
 	 * Creates a display string of a parameter list (without the parentheses)
 	 * for the given parameter types and names.
 	 *
 	 * @param parameterTypes the parameter types
 	 * @param parameterNames the parameter names
 	 * @return the display string of the parameter list defined by the passed
 	 *         arguments
 	 */
 	private final StringBuffer appendParameterSignature(StringBuffer buffer, char[][] parameterTypes, char[][] parameterNames, char[][] parameterDefaultValues) {
 		if (parameterTypes != null) {
 			for (int i = 0; i < parameterTypes.length; i++) {
 				if (i > 0) {
 					buffer.append(',');
 					buffer.append(' ');
 				}
 				buffer.append(parameterTypes[i]);
 				
 				// parameterNames[i] may not exist, since void is valid parameter, which has no name
 				if (parameterNames != null && i < parameterNames.length && parameterNames[i] != null) {
 					buffer.append(' ');
 					buffer.append(parameterNames[i]);
 					
 					if (parameterDefaultValues != null && i < parameterDefaultValues.length && parameterDefaultValues[i] != null) {
 						buffer.append(' ');
 						buffer.append('=');
 						buffer.append(' ');
 						buffer.append(parameterDefaultValues[i]);
 					}
 						
 				}
 			}
 		}
 		return buffer;
 	}
 
 	/**
 	 * Creates a display label for the given method proposal. The display label
 	 * consists of:
 	 * <ul>
 	 *   <li>the method name</li>
 	 *   <li>the parameter list (see {@link #createParameterList(CompletionProposal)})</li>
 	 *   <li>the upper bound of the return type (see {@link SignatureUtil#getUpperBound(String)})</li>
 	 *   <li>the raw simple name of the declaring type</li>
 	 * </ul>
 	 * <p>
 	 * Examples:
 	 * For the <code>get(int)</code> method of a variable of type <code>List<? extends Number></code>, the following
 	 * display name is returned: <code>get(int index)  Number - List</code>.<br>
 	 * For the <code>add(E)</code> method of a variable of type <code>List<? super Number></code>, the following
 	 * display name is returned: <code>add(Number o)  void - List</code>.<br>
 	 * </p>
 	 *
 	 * @param methodProposal the method proposal to display
 	 * @return the display label for the given method proposal
 	 */
 	String createMethodProposalLabel(CompletionProposal methodProposal) {
 		StringBuffer nameBuffer= new StringBuffer();
 		nameBuffer.append(methodProposal.getName());
 		
 		if (Signature.getTypeSignatureKind(methodProposal.getSignature()) == Signature.TEMPLATED_FUNCTION_SIGNATURE) {
 			nameBuffer.append('!');
 			nameBuffer.append('(');
 			appendTemplateParameterList(nameBuffer, methodProposal);
 			nameBuffer.append(')');
 		}
 		
 		nameBuffer.append('(');
 		appendUnboundedParameterList(nameBuffer, methodProposal);
 		appendVarargs(nameBuffer, methodProposal);
 		nameBuffer.append(')');
 
 		// return type
 		if (!methodProposal.isConstructor()) {
 			// TODO remove SignatureUtil.fix83600 call when bugs are fixed
 			char[] returnType= createTypeDisplayName(SignatureUtil.getUpperBound(Signature.getReturnType(SignatureUtil.fix83600(methodProposal.getTypeSignature()))));
 			nameBuffer.append("  "); //$NON-NLS-1$
 			nameBuffer.append(returnType);
 		}
 
 		appendDeclaration(methodProposal, nameBuffer);
 
 		return nameBuffer.toString();
 	}
 	
 	String createTemplateProposalLabel(CompletionProposal tempProposal) {
 		StringBuffer nameBuffer= new StringBuffer();
 		nameBuffer.append(tempProposal.getName());
 		nameBuffer.append('!');
 		nameBuffer.append('(');
 		appendTemplateParameterList(nameBuffer, tempProposal);
 		nameBuffer.append(')');
 		
 		appendDeclaration(tempProposal, nameBuffer);
 		
 		return nameBuffer.toString();
 	}
 	
 	String createTemplatedFunctionProposalLabel(CompletionProposal tempProposal) {
 		StringBuffer nameBuffer= new StringBuffer();
 		nameBuffer.append(tempProposal.getName());
 		nameBuffer.append('!');
 		nameBuffer.append('(');
 		appendTemplateParameterList(nameBuffer, tempProposal);
 		nameBuffer.append(')');
 		nameBuffer.append('(');
 		appendUnboundedParameterList(nameBuffer, tempProposal);
 		nameBuffer.append(')');
 		
 		appendDeclaration(tempProposal, nameBuffer);
 		
 		return nameBuffer.toString();
 	}
 
 	private void appendDeclaration(CompletionProposal proposal, StringBuffer nameBuffer) {
 		// declaring type
 		String declaringType= extractDeclaringTypeFQN(proposal);
 		if (declaringType != null) {
 			nameBuffer.append(" - "); //$NON-NLS-1$
 			nameBuffer.append(declaringType);
 		} else {
 			// module name
 			char[] fullName= Signature.toCharArray(proposal.getSignature(),
 					false /* don't fully qualify names */);
 			int qIndex= findSimpleNameStart(fullName);
 			if (qIndex > 0) {
 				nameBuffer.append(JavaElementLabels.CONCAT_STRING);
 				nameBuffer.append(fullName, 0, qIndex - 1);
 			}
 		}
 	}
 	
 	/**
 	 * Creates a display label for the given method proposal. The display label consists of:
 	 * <ul>
 	 * <li>the method name</li>
 	 * <li>the raw simple name of the declaring type</li>
 	 * </ul>
 	 * <p>
 	 * Examples: For the <code>get(int)</code> method of a variable of type
 	 * <code>List<? extends Number></code>, the following display name is returned <code>get(int) - List</code>.<br>
 	 * For the <code>add(E)</code> method of a variable of type <code>List</code>, the
 	 * following display name is returned:
 	 * <code>add(Object) - List</code>.<br>
 	 * </p>
 	 * 
 	 * @param methodProposal the method proposal to display
 	 * @return the display label for the given method proposal
 	 * @since 3.2
 	 */
 	String createJavadocMethodProposalLabel(CompletionProposal methodProposal) {
 		StringBuffer nameBuffer= new StringBuffer();
 		
 		// method name
 		nameBuffer.append(methodProposal.getCompletion());
 		
 		// declaring type
 		nameBuffer.append(" - "); //$NON-NLS-1$
 		String declaringType= extractDeclaringTypeFQN(methodProposal);
 		// TODO JDT signature
 //		declaringType= Signature.getSimpleName(declaringType);
 		nameBuffer.append(declaringType);
 		
 		return nameBuffer.toString();
 	}
 	
 	String createOverrideMethodProposalLabel(CompletionProposal methodProposal) {
 		StringBuffer nameBuffer= new StringBuffer();
 
 		// method name
 		nameBuffer.append(methodProposal.getName());
 
 		// parameters
 		nameBuffer.append('(');
 		appendUnboundedParameterList(nameBuffer, methodProposal);
 		nameBuffer.append(")  "); //$NON-NLS-1$
 
 		// return type
 		// TODO remove SignatureUtil.fix83600 call when bugs are fixed
 		char[] returnType= createTypeDisplayName(SignatureUtil.getUpperBound(Signature.getReturnType(SignatureUtil.fix83600(methodProposal.getTypeSignature()))));
 		nameBuffer.append(returnType);
 
 		// declaring type
 		nameBuffer.append(" - "); //$NON-NLS-1$
 
 		String declaringType= extractDeclaringTypeFQN(methodProposal);
 		
 		// TODO JDT signature
 //		declaringType= Signature.getSimpleName(declaringType);
 		nameBuffer.append(Messages.format(JavaTextMessages.ResultCollector_overridingmethod, new String(declaringType)));
 
 		return nameBuffer.toString();
 	}
 
 	/**
 	 * Extracts the fully qualified name of the declaring type of a method
 	 * reference.
 	 *
 	 * @param methodProposal a proposed method
 	 * @return the qualified name of the declaring type
 	 */
 	private String extractDeclaringTypeFQN(CompletionProposal methodProposal) {
 		char[] declaringTypeSignature= methodProposal.getDeclarationSignature();
 		// special methods may not have a declaring type: methods defined on arrays etc.
 		// TODO remove when bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=84690 gets fixed
 		if (declaringTypeSignature == null)
 			return null;
 		
 		return new String(createTypeDisplayName(declaringTypeSignature));
 //		return SignatureUtil.stripSignatureToFQN(String.valueOf(declaringTypeSignature));
 	}
 
 	/**
 	 * Creates a display label for a given type proposal. The display label
 	 * consists of:
 	 * <ul>
 	 *   <li>the simple type name (erased when the context is in javadoc)</li>
 	 *   <li>the package name</li>
 	 * </ul>
 	 * <p>
 	 * Examples:
 	 * A proposal for the generic type <code>java.util.List&lt;E&gt;</code>, the display label
 	 * is: <code>List<E> - java.util</code>.
 	 * </p>
 	 *
 	 * @param typeProposal the method proposal to display
 	 * @return the display label for the given type proposal
 	 */
 	String createTypeProposalLabel(CompletionProposal typeProposal) {
 		StringBuffer nameBuffer = new StringBuffer();
 		
 		if (typeProposal.isAlias()) {
 			nameBuffer.append(typeProposal.getName());
 		} else {
 			char[] signature;
 			if (fContext != null && fContext.isInJavadoc())
 				signature= typeProposal.getSignature();
 			else
 				signature= typeProposal.getSignature();
 			nameBuffer.append(Signature.toCharArray(signature,
 					false /* don't fully qualify names */));
 		}
 		
 		appendDeclaration(typeProposal, nameBuffer);
 
 		return nameBuffer.toString();
 	}
 	
 	String createJavadocTypeProposalLabel(CompletionProposal typeProposal) {
 		char[] fullName= Signature.toCharArray(typeProposal.getSignature(),
 				false /* don't fully qualify names */);
 		return createJavadocTypeProposalLabel(fullName);
 	}
 	
 	String createJavadocSimpleProposalLabel(CompletionProposal proposal) {
 		// TODO get rid of this
 		return createSimpleLabel(proposal);
 	}
 	
 	String createTypeProposalLabel(char[] fullName) {
 		// only display innermost type name as type name, using any
 		// enclosing types as qualification
 		int qIndex= findSimpleNameStart(fullName);
 
 		StringBuffer buf= new StringBuffer();
 		buf.append(fullName, qIndex, fullName.length - qIndex);
 		if (qIndex > 0) {
 			buf.append(JavaElementLabels.CONCAT_STRING);
 			buf.append(fullName, 0, qIndex - 1);
 		}
 		return buf.toString();
 	}
 	
 	String createJavadocTypeProposalLabel(char[] fullName) {
 		// only display innermost type name as type name, using any
 		// enclosing types as qualification
 		int qIndex= findSimpleNameStart(fullName);
 		
 		StringBuffer buf= new StringBuffer("{@link "); //$NON-NLS-1$
 		buf.append(fullName, qIndex, fullName.length - qIndex);
 		buf.append('}');
 		if (qIndex > 0) {
 			buf.append(JavaElementLabels.CONCAT_STRING);
 			buf.append(fullName, 0, qIndex - 1);
 		}
 		return buf.toString();
 	}
 	
 	private int findSimpleNameStart(char[] array) {
 		int lastDot= 0;
 		for (int i= 0, len= array.length; i < len; i++) {
 			char ch= array[i];
 			if (ch == '<') {
 				return lastDot;
 			} else if (ch == '.') {
 				lastDot= i + 1;
 			}
 		}
 		return lastDot;
 	}
 
 	String createSimpleLabelWithType(CompletionProposal proposal) {
 		StringBuffer buf= new StringBuffer();
 		buf.append(proposal.getCompletion());
 		
 		char[] typeName = proposal.getTypeSignature();
 		if (typeName != null && typeName.length != 0) {
 			buf.append("    "); //$NON-NLS-1$
 			buf.append(Signature.getSimpleName(Signature.toCharArray(typeName,
 					false /* don't fully qualify names */)));
 		}
 		
 //		char[] typeName= Signature.getSignatureSimpleName(proposal.getTypeName());
 //		if (typeName.length > 0) {
 //			buf.append("    "); //$NON-NLS-1$
 //			buf.append(typeName);
 //		}
 		return buf.toString();
 	}
 
 	String createLabelWithTypeAndDeclaration(CompletionProposal proposal) {
 		StringBuffer buf= new StringBuffer();
 		buf.append(proposal.getName());
 		
 		char[] typeSignature = proposal.getTypeSignature();
 		if (typeSignature != null && typeSignature.length != 0) {
 			buf.append("    "); //$NON-NLS-1$
 			buf.append(Signature.toCharArray(typeSignature,
 					false /* don't fully qualify names */));
 		}
 		
 		char[] declSignature = proposal.getDeclarationSignature();
 		if (declSignature != null && declSignature.length != 0) {
 			buf.append(JavaElementLabels.CONCAT_STRING);
 			
 			buf.append(Signature.toCharArray(declSignature,
 					false /* don't fully qualify names */));	
 		}
 		
 //		char[] typeName= Signature.getSignatureSimpleName(proposal.getTypeName());
 //		if (typeName.length > 0) {
 //			buf.append("    "); //$NON-NLS-1$
 //			buf.append(typeName);
 //		}
 //		
 //		if (proposal.getSignature() != null) {
 //			char[] fullName= Signature.toCharArray(proposal.getSignature());
 //			
 //			// only display innermost type name as type name, using any
 //			// enclosing types as qualification
 //			int qIndex= findSimpleNameStart(fullName);
 //			if (qIndex > 0) {
 //				buf.append(JavaElementLabels.CONCAT_STRING);
 //				buf.append(fullName, 0, qIndex - 1);
 //			}
 //			return buf.toString();
 //		}
 		return buf.toString();
 	}
 
 	String createPackageProposalLabel(CompletionProposal proposal) {
 		Assert.isTrue(proposal.getKind() == CompletionProposal.COMPILATION_UNIT_REF);
 		return String.valueOf(proposal.getDeclarationSignature());
 	}
 
 	String createSimpleLabel(CompletionProposal proposal) {
 		return String.valueOf(proposal.getCompletion());
 	}
 
 	String createAnonymousTypeLabel(CompletionProposal proposal) {
 		char[] declaringTypeSignature= proposal.getDeclarationSignature();
 
 		StringBuffer buffer= new StringBuffer();
 		
 		// TODO JDT signature
 //		buffer.append(Signature.getSignatureSimpleName(declaringTypeSignature));
 		buffer.append('(');
 		appendUnboundedParameterList(buffer, proposal);
 		buffer.append(')');
 		buffer.append("  "); //$NON-NLS-1$
 		buffer.append(JavaTextMessages.ResultCollector_anonymous_type);
 
 		return buffer.toString();
 	}
 
 	/**
 	 * Creates the display label for a given <code>CompletionProposal</code>.
 	 *
 	 * @param proposal the completion proposal to create the display label for
 	 * @return the display label for <code>proposal</code>
 	 */
 	public String createLabel(CompletionProposal proposal) {
 		switch (proposal.getKind()) {
 			case CompletionProposal.METHOD_NAME_REFERENCE:
 			case CompletionProposal.METHOD_REF:
 			case CompletionProposal.OP_CALL:
 			case CompletionProposal.FUNCTION_CALL:
 			case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
 				if (fContext != null && fContext.isInJavadoc())
 					return createJavadocMethodProposalLabel(proposal);
 				return createMethodProposalLabel(proposal);
 			case CompletionProposal.TEMPLATE_REF:
 			case CompletionProposal.TEMPLATED_AGGREGATE_REF:
 				return createTemplateProposalLabel(proposal);
 			case CompletionProposal.TEMPLATED_FUNCTION_REF:
 				return createTemplatedFunctionProposalLabel(proposal);
 			case CompletionProposal.METHOD_DECLARATION:
 				return createOverrideMethodProposalLabel(proposal);
 			case CompletionProposal.ANONYMOUS_CLASS_DECLARATION:
 				return createAnonymousTypeLabel(proposal);
 			case CompletionProposal.TYPE_REF:
 				return createTypeProposalLabel(proposal);
 			case CompletionProposal.JAVADOC_TYPE_REF:
 				return createJavadocTypeProposalLabel(proposal);
 			case CompletionProposal.JAVADOC_FIELD_REF:
 			case CompletionProposal.JAVADOC_VALUE_REF:
 			case CompletionProposal.JAVADOC_BLOCK_TAG:
 			case CompletionProposal.JAVADOC_INLINE_TAG:
 			case CompletionProposal.JAVADOC_PARAM_REF:
 				return createJavadocSimpleProposalLabel(proposal);
 			case CompletionProposal.JAVADOC_METHOD_REF:
 				return createJavadocMethodProposalLabel(proposal);
 			case CompletionProposal.COMPILATION_UNIT_REF:
 				return createPackageProposalLabel(proposal);
 			case CompletionProposal.ANNOTATION_ATTRIBUTE_REF:
 			case CompletionProposal.FIELD_REF:
 			case CompletionProposal.ENUM_MEMBER:
 				return createLabelWithTypeAndDeclaration(proposal);
 			case CompletionProposal.LOCAL_VARIABLE_REF:
 			case CompletionProposal.VARIABLE_DECLARATION:
 				return createSimpleLabelWithType(proposal);
 			case CompletionProposal.KEYWORD:
 			case CompletionProposal.LABEL_REF:
 				return createSimpleLabel(proposal);
 			case CompletionProposal.DDOC_MACRO:
 				return createSimpleLabel(proposal);
 			default:
 				Assert.isTrue(false);
 				return null;
 		}
 	}
 
 	/**
 	 * Creates and returns a decorated image descriptor for a completion proposal.
 	 *
 	 * @param proposal the proposal for which to create an image descriptor
 	 * @return the created image descriptor, or <code>null</code> if no image is available
 	 */
 	public ImageDescriptor createImageDescriptor(CompletionProposal proposal) {
 		final long flags= proposal.getFlags();
 
 		ImageDescriptor descriptor;
 		switch (proposal.getKind()) {
 			case CompletionProposal.METHOD_DECLARATION:
 			case CompletionProposal.METHOD_NAME_REFERENCE:
 			case CompletionProposal.METHOD_REF:
 			case CompletionProposal.TEMPLATED_FUNCTION_REF:
 			case CompletionProposal.ANNOTATION_ATTRIBUTE_REF:
 			case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
 				descriptor= JavaElementImageProvider.getMethodImageDescriptor(false, flags);
 				break;
 			case CompletionProposal.FUNCTION_CALL:
 			case CompletionProposal.OP_CALL:
 				descriptor= JavaElementImageProvider.getFunctionCallImageDescriptor();
 				break;
 			case CompletionProposal.ANONYMOUS_CLASS_DECLARATION:
 			case CompletionProposal.TYPE_REF:
 				// TODO JDT signature
 //				switch (Signature.getTypeSignatureKind(proposal.getSignature())) {
 //					case Signature.CLASS_TYPE_SIGNATURE:
 //						descriptor= JavaElementImageProvider.getTypeImageDescriptor(false, false, flags, false);
 //						break;
 //					case Signature.INTERFACE_TYPE_SIGNATURE:
 //						descriptor= JavaElementImageProvider.getTypeImageDescriptor(false, false, flags | Flags.AccInterface, false);
 //						break;
 //					case Signature.STRUCT_TYPE_SIGNATURE:
 //						descriptor= JavaElementImageProvider.getTypeImageDescriptor(false, false, flags | Flags.AccStruct, false);
 //						break;
 //					case Signature.UNION_TYPE_SIGNATURE:
 //						descriptor= JavaElementImageProvider.getTypeImageDescriptor(false, false, flags | Flags.AccUnion, false);
 //						break;
 //					case Signature.ENUM_TYPE_SIGNATURE:
 //						descriptor= JavaElementImageProvider.getTypeImageDescriptor(false, false, flags | Flags.AccEnum, false);
 //						break;
 //					case Signature.TYPE_VARIABLE_SIGNATURE:
 //						descriptor= JavaPluginImages.DESC_OBJS_TYPEVARIABLE;
 //						break;
 //					default:
 //						descriptor= null;
 //				}
 				descriptor= JavaElementImageProvider.getTypeImageDescriptor(false, false, flags, false);
 				break;
 			case CompletionProposal.TEMPLATE_REF:
 				descriptor = JavaElementImageProvider.getTypeImageDescriptor(false, false, flags | Flags.AccTemplate, false);
 				break;
 			case CompletionProposal.TEMPLATED_AGGREGATE_REF:
 				// TODO JDT signature
 //				switch (Signature.getTypeSignatureKind(proposal.getSignature())) {
 //				case Signature.TEMPLATED_CLASS_TYPE_SIGNATURE:
 //					descriptor= JavaElementImageProvider.getTypeImageDescriptor(false, false, flags, false);
 //					break;
 //				case Signature.TEMPLATED_INTERFACE_TYPE_SIGNATURE:
 //					descriptor= JavaElementImageProvider.getTypeImageDescriptor(false, false, flags | Flags.AccInterface, false);
 //					break;
 //				case Signature.TEMPLATED_STRUCT_TYPE_SIGNATURE:
 //					descriptor= JavaElementImageProvider.getTypeImageDescriptor(false, false, flags | Flags.AccStruct, false);
 //					break;
 //				case Signature.TEMPLATED_UNION_TYPE_SIGNATURE:
 //					descriptor= JavaElementImageProvider.getTypeImageDescriptor(false, false, flags | Flags.AccUnion, false);
 //					break;
 //				default:
 //					descriptor= null;
 //				}
 				descriptor= JavaElementImageProvider.getTypeImageDescriptor(false, false, flags, false);
 				break;
 			case CompletionProposal.FIELD_REF:
 			case CompletionProposal.ENUM_MEMBER:
 				if ((flags & Flags.AccAlias) != 0) {
 					descriptor= JavaElementImageProvider.getAliasImageDescriptor(flags);
 				} else if ((flags & Flags.AccTypedef) != 0) {
 					descriptor= JavaElementImageProvider.getTypedefImageDescriptor(flags);
 				} else {
 					descriptor= JavaElementImageProvider.getFieldImageDescriptor(false, flags);
 				}
 				break;
 			case CompletionProposal.LOCAL_VARIABLE_REF:
 			case CompletionProposal.VARIABLE_DECLARATION:
 				descriptor= JavaPluginImages.DESC_OBJS_LOCAL_VARIABLE;
 				break;
 			case CompletionProposal.COMPILATION_UNIT_REF:
 				descriptor= JavaPluginImages.DESC_OBJS_PACKAGE;
 				break;
 			case CompletionProposal.KEYWORD:
 			case CompletionProposal.LABEL_REF:
 				descriptor= null;
 				break;
 			case CompletionProposal.JAVADOC_METHOD_REF:
 			case CompletionProposal.JAVADOC_TYPE_REF:
 			case CompletionProposal.JAVADOC_FIELD_REF:
 			case CompletionProposal.JAVADOC_VALUE_REF:
 			case CompletionProposal.JAVADOC_BLOCK_TAG:
 			case CompletionProposal.JAVADOC_INLINE_TAG:
 			case CompletionProposal.JAVADOC_PARAM_REF:
 				descriptor = JavaPluginImages.DESC_OBJS_JAVADOCTAG;
 				break;
 			case CompletionProposal.VERSION_REF:
 				descriptor= JavaPluginImages.DESC_OBJS_VERSION_DECLARATION;
 			case CompletionProposal.DEBUG_REF:
 				descriptor= JavaPluginImages.DESC_OBJS_DEBUG_DECLARATION;
 			case CompletionProposal.DDOC_MACRO:
 				descriptor= JavaPluginImages.DESC_OBJS_DDOC_MACRO;
 				break;
 			default:
 				descriptor= null;
 				Assert.isTrue(false);
 		}
 
 		if (descriptor == null)
 			return null;
 		return decorateImageDescriptor(descriptor, proposal);
 	}
 
 	ImageDescriptor createMethodImageDescriptor(CompletionProposal proposal) {
 		final long flags= proposal.getFlags();
 		return decorateImageDescriptor(JavaElementImageProvider.getMethodImageDescriptor(false, flags), proposal);
 	}
 
 	ImageDescriptor createTypeImageDescriptor(CompletionProposal proposal) {
 		final long flags= proposal.getFlags();
 		boolean isInterfaceOrAnnotation= Flags.isInterface(flags);
 		return decorateImageDescriptor(JavaElementImageProvider.getTypeImageDescriptor(true /* in order to get all visibility decorations */, isInterfaceOrAnnotation, flags, false), proposal);
 	}
 
 	ImageDescriptor createFieldImageDescriptor(CompletionProposal proposal) {
 		final long flags= proposal.getFlags();
 		return decorateImageDescriptor(JavaElementImageProvider.getFieldImageDescriptor(false, flags), proposal);
 	}
 
 	ImageDescriptor createLocalImageDescriptor(CompletionProposal proposal) {
 		return decorateImageDescriptor(JavaPluginImages.DESC_OBJS_LOCAL_VARIABLE, proposal);
 	}
 
 	ImageDescriptor createPackageImageDescriptor(CompletionProposal proposal) {
 		return decorateImageDescriptor(JavaPluginImages.DESC_OBJS_MODULE, proposal);
 	}
 	
 	ImageDescriptor createVersionImageDescriptor(CompletionProposal proposal) {
 		return decorateImageDescriptor(JavaPluginImages.DESC_OBJS_VERSION_DECLARATION, proposal);
 	}
 	
 	ImageDescriptor createDebugImageDescriptor(CompletionProposal proposal) {
 		return decorateImageDescriptor(JavaPluginImages.DESC_OBJS_DEBUG_DECLARATION, proposal);
 	}
 
 	/**
 	 * Returns a version of <code>descriptor</code> decorated according to
 	 * the passed <code>modifier</code> flags.
 	 *
 	 * @param descriptor the image descriptor to decorate
 	 * @param proposal the proposal
 	 * @return an image descriptor for a method proposal
 	 * @see Flags
 	 */
 	private ImageDescriptor decorateImageDescriptor(ImageDescriptor descriptor, CompletionProposal proposal) {
 		int adornments= 0;
 		long flags= proposal.getFlags();
 		int kind= proposal.getKind();
 
 		if (Flags.isDeprecated(flags))
 			adornments |= JavaElementImageDescriptor.DEPRECATED;
 
 		if (kind == CompletionProposal.FIELD_REF || kind == CompletionProposal.METHOD_DECLARATION || kind == CompletionProposal.METHOD_DECLARATION || kind == CompletionProposal.METHOD_NAME_REFERENCE || kind == CompletionProposal.METHOD_REF || kind == CompletionProposal.OP_CALL)
 			if (Flags.isStatic(flags))
 				adornments |= JavaElementImageDescriptor.STATIC;
 
 		if (kind == CompletionProposal.METHOD_DECLARATION || kind == CompletionProposal.METHOD_DECLARATION || kind == CompletionProposal.METHOD_NAME_REFERENCE || kind == CompletionProposal.METHOD_REF)
 			if (Flags.isSynchronized(flags))
 				adornments |= JavaElementImageDescriptor.SYNCHRONIZED;
 
 		if (kind == CompletionProposal.TYPE_REF && Flags.isAbstract(flags) && !Flags.isInterface(flags))
 			adornments |= JavaElementImageDescriptor.ABSTRACT;
 
 		return new JavaElementImageDescriptor(descriptor, adornments, JavaElementImageProvider.SMALL_SIZE);
 	}
 
 	/**
 	 * Sets the completion context.
 	 * 
 	 * @param context the completion context
 	 * @since 3.2
 	 */
 	void setContext(CompletionContext context) {
 		fContext= context;
 	}
 
 }
