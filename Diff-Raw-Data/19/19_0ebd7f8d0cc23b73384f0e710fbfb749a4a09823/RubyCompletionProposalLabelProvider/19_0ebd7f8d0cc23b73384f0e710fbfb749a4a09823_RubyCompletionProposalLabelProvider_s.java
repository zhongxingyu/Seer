 package org.eclipse.dltk.ruby.internal.ui.text.completion;
 
 import org.eclipse.dltk.core.CompletionProposal;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.ruby.core.model.FakeMethod;
 import org.eclipse.dltk.ui.text.completion.CompletionProposalLabelProvider;
 
 public class RubyCompletionProposalLabelProvider extends
 		CompletionProposalLabelProvider {
 	protected String createMethodProposalLabel(CompletionProposal methodProposal) {
 		StringBuffer nameBuffer = new StringBuffer();
 
 		// method name
 		nameBuffer.append(methodProposal.getName());
 
 		// parameters
 		nameBuffer.append('(');
 		appendUnboundedParameterList(nameBuffer, methodProposal);
 		nameBuffer.append(')');
 		
 		
 		IMethod method = (IMethod) methodProposal.getModelElement();
 		nameBuffer.append(" - ");
 		if (method instanceof FakeMethod) {
 			nameBuffer.append(((FakeMethod)method).getReceiver());
 		} else {
 			IModelElement parent = method.getParent();
 			if (parent instanceof IType) {
 				IType type = (IType) parent;
 				nameBuffer.append(type.getTypeQualifiedName("::"));
 			} else {
 				nameBuffer.append(parent.getElementName());
 			}
 		}
 
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
 		nameBuffer.append(")"); //$NON-NLS-1$
 		
 		IMethod method = (IMethod) methodProposal.getModelElement();
 		nameBuffer.append(" - ");
 		if (method instanceof FakeMethod) {
 			nameBuffer.append(((FakeMethod)method).getReceiver());
 		} else {
 			IModelElement parent = method.getParent();
 			if (parent instanceof IType) {
 				IType type = (IType) parent;
 				nameBuffer.append(type.getTypeQualifiedName("::"));
 			} else {
 				nameBuffer.append(parent.getElementName());
 			}
 		}
 
 		return nameBuffer.toString();
 	}
 
 	protected final StringBuffer appendParameterSignature(StringBuffer buffer,
 			char[][] parameterTypes, char[][] parameterNames) {
 		if (parameterNames != null) {
 			for (int i = 0; i < parameterNames.length; i++) {
 				if (i > 0) {
 					buffer.append(',');
 				}
 				buffer.append(parameterNames[i]);
 			}
 		}
 		return buffer;
 	}
 	
 	protected String createTypeProposalLabel(CompletionProposal typeProposal) {
 		StringBuffer nameBuffer = new StringBuffer();
 
 		nameBuffer.append(typeProposal.getName());
 
 		
 		IType type = (IType) typeProposal.getModelElement();
 		nameBuffer.append(" - ");
 		IModelElement parent = type.getParent();
 		if (parent instanceof IType) {
 			IType type2 = (IType) parent;
			nameBuffer.append(type2.getTypeQualifiedName("::"));
 		} else {
 			nameBuffer.append(parent.getElementName());
 		}
 		
 		return nameBuffer.toString();
 	}
 	
 	
 }
