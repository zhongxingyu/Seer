 package org.emftext.sdk.quickfixes;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.emftext.sdk.concretesyntax.resource.cs.ICsQuickFix;
 import org.emftext.sdk.concretesyntax.resource.cs.mopp.CsQuickFix;
 
 /**
  * A quick fix that removes an option form a concrete syntax specification.
  * This fix can be used for duplicate options.
  */
 public class RemoveElementQuickFix extends CsQuickFix implements ICsQuickFix {
 
 	private EObject objectToRemove;
 
 	public RemoveElementQuickFix(String message, EObject objectToRemove) {
		super(message, "IMG_ELCL_REMOVE", objectToRemove);
 		this.objectToRemove = objectToRemove;
 	}
 
 	@Override
 	public void applyChanges() {
 		EcoreUtil.delete(objectToRemove);
 	}
 }
