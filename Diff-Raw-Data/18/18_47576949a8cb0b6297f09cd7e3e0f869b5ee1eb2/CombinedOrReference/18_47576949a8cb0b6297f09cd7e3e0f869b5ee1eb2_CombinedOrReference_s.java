 /**
  * 
  */
 package org.eclipse.dltk.internal.javascript.typeinference;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.internal.javascript.reference.resolvers.SelfCompletingReference;
 
 /**
  * @author jcompagner
  * 
  */
 public class CombinedOrReference implements IReference, SelfCompletingReference {
 
 	private final List lstReadonly;
 	private final List lstReferences;
 	private boolean recursiveCheck;
 
 	/**
 	 * 
 	 */
 	public CombinedOrReference() {
 		lstReferences = new ArrayList();
 		lstReadonly = new ArrayList();
 	}
 
 	private CombinedOrReference(List references, List readonly) {
 		this();
 		for (int i = 0; i < readonly.size(); i++) {
 			IReference element = (IReference) readonly.get(i);
 			addReadonly(element);
 		}
 		for (int i = 0; i < references.size(); i++) {
 			IReference element = (IReference) references.get(i);
 			addReference(element);
 		}
 	}
 
 	/**
 	 * @param rm
 	 * @param root
 	 */
 	public CombinedOrReference(IReference rm, IReference root) {
 		this();
 		addReadonly(rm);
 		addReference(root);
 	}
 
 	public void addReference(IReference reference) {
 		if (reference instanceof CombinedOrReference) {
 			addCombinedReference(reference);
 		} else if (!testContains(reference)) {
 			lstReferences.add(reference);
 		}
 	}
 
 	/**
 	 * @param reference
 	 * @return
 	 */
	private boolean testContains(IReference reference) {
 		if (reference == this)
 			return true;
 
 		for (int i = 0; i < lstReferences.size(); i++) {
 			Object element = lstReferences.get(i);
 			if (element.equals(reference))
 				return true;
 			if (testTransparentRef(reference, element))
 				return true;
 		}
 
 		for (int i = 0; i < lstReadonly.size(); i++) {
 			Object element = lstReadonly.get(i);
 			if (element.equals(reference))
 				return true;
 			if (testTransparentRef(reference, element))
 				return true;
 		}
 
 		if (testTransparentRef(this, reference))
 			return true;
 		return false;
 	}
 
 	/**
 	 * @param reference
 	 * @param element
 	 */
 	private boolean testTransparentRef(IReference reference, Object element) {
 		if (element instanceof TransparentRef) {
 			TransparentRef transparentRef = ((TransparentRef) element);
 			if (transparentRef.evaluateReference == reference)
 				return true;
 			if (transparentRef.evaluateReference instanceof CombinedOrReference) {
 				CombinedOrReference cor = (CombinedOrReference) transparentRef.evaluateReference;
 				if (cor.testContains(this)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @param reference
 	 */
 	private void addCombinedReference(IReference reference) {
 		CombinedOrReference cor = (CombinedOrReference) reference;
 		for (int i = 0; i < cor.lstReferences.size(); i++) {
 			addReference((IReference) cor.lstReferences.get(i));
 		}
 		for (int i = 0; i < cor.lstReadonly.size(); i++) {
 			addReadonly((IReference) cor.lstReadonly.get(i));
 		}
 	}
 
 	public void addReadonly(IReference reference) {
 		if (reference instanceof CombinedOrReference) {
 			addCombinedReference(reference);
 		} else if (!testContains(reference)) {
 			lstReadonly.add(reference);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.typeinference.IReference#addModelElements(java.util.Collection)
 	 */
 	public void addModelElements(Collection toAdd) {
 		for (int i = 0; i < lstReferences.size(); i++) {
 			IReference element = (IReference) lstReferences.get(i);
 			element.addModelElements(toAdd);
 		}
 		for (int i = 0; i < lstReadonly.size(); i++) {
 			IReference element = (IReference) lstReadonly.get(i);
 			element.addModelElements(toAdd);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.typeinference.IReference#getChild(java.lang.String,
 	 *      boolean)
 	 */
 	public IReference getChild(String key, boolean resolveLocals) {
 		ArrayList alReferences = new ArrayList();
 		for (int i = 0; i < lstReferences.size(); i++) {
 			IReference element = (IReference) lstReferences.get(i);
 			IReference child = element.getChild(key, resolveLocals);
 			if (child != null && !alReferences.contains(child))
 				alReferences.add(child);
 		}
 		ArrayList alReadonly = new ArrayList();
 		for (int i = 0; i < lstReadonly.size(); i++) {
 			IReference element = (IReference) lstReadonly.get(i);
 			IReference child = element.getChild(key, resolveLocals);
 			if (child != null && !alReadonly.contains(child))
 				alReadonly.add(child);
 		}
 		if (alReferences.size() == 0 && alReadonly.size() == 0)
 			return null;
 
 		if (alReferences.size() == 1 && alReadonly.size() == 0) {
 			return (IReference) alReferences.get(0);
 		}
 		if (alReferences.size() == 0 && alReadonly.size() == 1) {
 			return (IReference) alReadonly.get(0);
 		}
 		return new CombinedOrReference(alReferences, alReadonly).compressed();
 	}
 
 	/**
 	 * @return
 	 */
 	private IReference compressed() {
 		if (lstReferences.size() == 1 && lstReadonly.size() == 0) {
 			return (IReference) lstReferences.get(0);
 		}
 		if (lstReferences.size() == 0 && lstReadonly.size() == 1) {
 			return (IReference) lstReadonly.get(0);
 		}
 		return this;
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.typeinference.IReference#getChilds(boolean)
 	 */
 	public Set getChilds(boolean resolveLocals) {
 		HashSet set = new HashSet();
 		if (recursiveCheck)
 			return set;
 		try {
 			recursiveCheck = true;
 			for (int i = 0; i < lstReferences.size(); i++) {
 				IReference element = (IReference) lstReferences.get(i);
 				set.addAll(element.getChilds(resolveLocals));
 			}
 			for (int i = 0; i < lstReadonly.size(); i++) {
 				IReference element = (IReference) lstReadonly.get(i);
 				set.addAll(element.getChilds(resolveLocals));
 			}
 		} finally {
 			recursiveCheck = false;
 		}
 		return set;
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.typeinference.IReference#getName()
 	 */
 	public String getName() {
 		if (lstReferences.size() > 0)
 			return ((IReference) lstReferences.get(0)).getName();
 		if (lstReadonly.size() > 0)
 			return ((IReference) lstReadonly.get(0)).getName();
 		return null;
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.typeinference.IReference#getPrototype(boolean)
 	 */
 	public IReference getPrototype(boolean resolveLocals) {
 		ArrayList alReferences = new ArrayList();
 		for (int i = 0; i < lstReferences.size(); i++) {
 			IReference element = (IReference) lstReferences.get(i);
 			IReference prototype = element.getPrototype(resolveLocals);
 			if (prototype != null && !alReferences.contains(prototype)) {
 				alReferences.add(prototype);
 			}
 		}
 		ArrayList alReadonly = new ArrayList();
 		for (int i = 0; i < lstReadonly.size(); i++) {
 			IReference element = (IReference) lstReadonly.get(i);
 			IReference prototype = element.getPrototype(resolveLocals);
 			if (prototype != null && !alReadonly.contains(prototype)) {
 				alReadonly.add(prototype);
 			}
 		}
 		if (alReferences.size() == 0 && alReadonly.size() == 0)
 			return null;
 
 		if (alReferences.size() == 1 && alReadonly.size() == 0) {
 			return (IReference) alReferences.get(0);
 		}
 		if (alReferences.size() == 0 && alReadonly.size() == 1) {
 			return (IReference) alReadonly.get(0);
 		}
 		return new CombinedOrReference(alReferences, alReadonly).compressed();
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.typeinference.IReference#isChildishReference()
 	 */
 	public boolean isChildishReference() {
 		for (int i = 0; i < lstReferences.size(); i++) {
 			IReference element = (IReference) lstReferences.get(i);
 			if (element.isChildishReference())
 				return true;
 		}
 		for (int i = 0; i < lstReadonly.size(); i++) {
 			IReference element = (IReference) lstReadonly.get(i);
 			if (element.isChildishReference())
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.typeinference.IReference#isFunctionRef()
 	 */
 	public boolean isFunctionRef() {
 		for (int i = 0; i < lstReferences.size(); i++) {
 			IReference element = (IReference) lstReferences.get(i);
 			if (element.isFunctionRef())
 				return true;
 		}
 		for (int i = 0; i < lstReadonly.size(); i++) {
 			IReference element = (IReference) lstReadonly.get(i);
 			if (element.isFunctionRef())
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.typeinference.IReference#isLocal()
 	 */
 	public boolean isLocal() {
 		for (int i = 0; i < lstReferences.size(); i++) {
 			IReference element = (IReference) lstReferences.get(i);
 			if (element.isLocal())
 				return true;
 		}
 		for (int i = 0; i < lstReadonly.size(); i++) {
 			IReference element = (IReference) lstReadonly.get(i);
 			if (element.isLocal())
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.typeinference.IReference#recordDelete(java.lang.String)
 	 */
 	public void recordDelete(String fieldId) {
 		for (int i = 0; i < lstReferences.size(); i++) {
 			IReference element = (IReference) lstReferences.get(i);
 			element.recordDelete(fieldId);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.typeinference.IReference#setChild(java.lang.String,
 	 *      org.eclipse.dltk.internal.javascript.typeinference.IReference)
 	 */
 	public void setChild(String key, IReference ref) {
 		for (int i = 0; i < lstReferences.size(); i++) {
 			IReference element = (IReference) lstReferences.get(i);
 			element.setChild(key, ref);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.typeinference.IReference#setLocal(boolean)
 	 */
 	public void setLocal(boolean local) {
 		for (int i = 0; i < lstReadonly.size(); i++) {
 			IReference element = (IReference) lstReadonly.get(i);
 			element.setLocal(local);
 		}
 		for (int i = 0; i < lstReferences.size(); i++) {
 			IReference element = (IReference) lstReferences.get(i);
 			element.setLocal(local);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.typeinference.IReference#setLocationInformation(org.eclipse.dltk.internal.core.ModelElement,
 	 *      int, int)
 	 */
 	public void setLocationInformation(ModelElement mo, int position, int length) {
 		for (int i = 0; i < lstReferences.size(); i++) {
 			IReference element = (IReference) lstReferences.get(i);
 			element.setLocationInformation(mo, position, length);
 		}
 		for (int i = 0; i < lstReadonly.size(); i++) {
 			IReference element = (IReference) lstReadonly.get(i);
 			element.setLocationInformation(mo, position, length);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.typeinference.IReference#setPrototype(org.eclipse.dltk.internal.javascript.typeinference.IReference)
 	 */
 	public void setPrototype(IReference ref) {
 		for (int i = 0; i < lstReferences.size(); i++) {
 			IReference element = (IReference) lstReferences.get(i);
 			element.setPrototype(ref);
 		}
 	}
 
 	/**
 	 * @param ref
 	 * @return
 	 */
 	public boolean contains(IReference ref) {
 		return lstReferences.contains(ref) || lstReadonly.contains(ref);
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.reference.resolvers.SelfCompletingReference#getImageURL()
 	 */
 	public URL getImageURL() {
 		for (int i = 0; i < lstReferences.size(); i++) {
 			IReference element = (IReference) lstReferences.get(i);
 			if (element instanceof SelfCompletingReference
 					&& ((SelfCompletingReference) element).getImageURL() != null) {
 				return ((SelfCompletingReference) element).getImageURL();
 			}
 		}
 		for (int i = 0; i < lstReadonly.size(); i++) {
 			IReference element = (IReference) lstReadonly.get(i);
 			if (element instanceof SelfCompletingReference
 					&& ((SelfCompletingReference) element).getImageURL() != null) {
 				return ((SelfCompletingReference) element).getImageURL();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.reference.resolvers.SelfCompletingReference#getKind()
 	 */
 	public int getKind() {
 		for (int i = 0; i < lstReferences.size(); i++) {
 			IReference element = (IReference) lstReferences.get(i);
 			if (element instanceof SelfCompletingReference
 					&& ((SelfCompletingReference) element).getKind() != 0) {
 				return ((SelfCompletingReference) element).getKind();
 			}
 		}
 		for (int i = 0; i < lstReadonly.size(); i++) {
 			IReference element = (IReference) lstReadonly.get(i);
 			if (element instanceof SelfCompletingReference
 					&& ((SelfCompletingReference) element).getKind() != 0) {
 				return ((SelfCompletingReference) element).getKind();
 			}
 		}
 		return 0;
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.reference.resolvers.SelfCompletingReference#getParameterNames()
 	 */
 	public char[][] getParameterNames() {
 		for (int i = 0; i < lstReferences.size(); i++) {
 			IReference element = (IReference) lstReferences.get(i);
 			if (element instanceof SelfCompletingReference
 					&& ((SelfCompletingReference) element).getParameterNames() != null) {
 				return ((SelfCompletingReference) element).getParameterNames();
 			}
 		}
 		for (int i = 0; i < lstReadonly.size(); i++) {
 			IReference element = (IReference) lstReadonly.get(i);
 			if (element instanceof SelfCompletingReference
 					&& ((SelfCompletingReference) element).getParameterNames() != null) {
 				return ((SelfCompletingReference) element).getParameterNames();
 			}
 		}
 		return null;
 	}
 
 	public String getReturnType() {
 		for (int i = 0; i < lstReferences.size(); i++) {
 			IReference element = (IReference) lstReferences.get(i);
 			if (element instanceof SelfCompletingReference
 					&& ((SelfCompletingReference) element).getReturnType() != null) {
 				return ((SelfCompletingReference) element).getReturnType();
 			}
 		}
 		for (int i = 0; i < lstReadonly.size(); i++) {
 			IReference element = (IReference) lstReadonly.get(i);
 			if (element instanceof SelfCompletingReference
 					&& ((SelfCompletingReference) element).getReturnType() != null) {
 				return ((SelfCompletingReference) element).getReturnType();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.reference.resolvers.SelfCompletingReference#getProposalInfo()
 	 */
 	public String getProposalInfo() {
 		for (int i = 0; i < lstReferences.size(); i++) {
 			IReference element = (IReference) lstReferences.get(i);
 			if (element instanceof SelfCompletingReference
 					&& ((SelfCompletingReference) element).getProposalInfo() != null) {
 				return ((SelfCompletingReference) element).getProposalInfo();
 			}
 		}
 		for (int i = 0; i < lstReadonly.size(); i++) {
 			IReference element = (IReference) lstReadonly.get(i);
 			if (element instanceof SelfCompletingReference
 					&& ((SelfCompletingReference) element).getProposalInfo() != null) {
 				return ((SelfCompletingReference) element).getProposalInfo();
 			}
 		}
 		return null;
 	}
 
 }
