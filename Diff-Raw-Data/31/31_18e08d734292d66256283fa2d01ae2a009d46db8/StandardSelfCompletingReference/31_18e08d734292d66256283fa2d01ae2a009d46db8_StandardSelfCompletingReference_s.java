 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.javascript.typeinference;
 
 import java.net.URL;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.dltk.core.CompletionProposal;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.internal.core.SourceMethod;
 import org.eclipse.dltk.internal.javascript.reference.resolvers.SelfCompletingReference;
 
 /**
  * @since 2.0
  */
 public class StandardSelfCompletingReference implements IReference,
 		SelfCompletingReference {
 
 	private String name;
 	private final boolean childIsh;
 	boolean fRef;
 	boolean local;
 
 	protected ModelElement parent;
 	private int offset;
 	private int length;
 
 	private Map childs;
 	private char[][] parameterNames;
 	private String proposalInfo;
 	protected StandardSelfCompletingReference parentRef;
 	private URL imageUrl;
 	private String returnType;
 	private int parameterIndex = -1;
 
 	public int getOffset() {
 		return offset;
 	}
 
 	public int getLength() {
 		return length;
 	}
 
 	public StandardSelfCompletingReference(String paramOrVarName,
 			boolean childIsh) {
 		this.name = paramOrVarName;
 		this.childIsh = childIsh;
 
 	}
 
 	public void setLocationInformation(ModelElement parent, int offset,
 			int length) {
 		this.parent = parent;
 		this.length = length;
 		this.offset = offset;
 	}
 
 	public Set getChilds(boolean resolveLocals) {
 		if (childs == null) {
 			childs = new HashMap(3);
 			createChilds();
 		}
 		return new HashSet(childs.values());
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String toString() {
 		return name;
 	}
 
 	public String getParentName() {
 		return name;
 	}
 
 	public IReference getChild(String key, boolean resolveLocals) {
 		if (childs == null) {
 			childs = new HashMap(3);
 			createChilds();
 		}
 		IReference r = (IReference) childs.get(key);
 		return r;
 	}
 
 	protected void createChilds() {
 	}
 
 	public void setChild(String key, IReference ref) {
 		if (childs == null) {
 			childs = new HashMap(3);
 			createChilds();
 		}
 		ref = testRecursion(ref);
 		childs.put(key, ref);
 	}
 
 	/**
 	 * @param ref
 	 * @return
 	 */
 	private IReference testRecursion(IReference ref) {
 		if (ref instanceof StandardSelfCompletingReference) {
 			StandardSelfCompletingReference ssr = (StandardSelfCompletingReference) ref;
 			ssr.parentRef = this;
 			if (parentRef != null) {
 				HashSet set = new HashSet();
 				set.add(parentRef);
 				IReference child = parentRef.findEqualParent(ref, set);
 				if (child != null) {
 					ref = child;
 				}
 			}
 		}
 		return ref;
 	}
 
 	/**
 	 * @param ref
 	 * @return
 	 */
 	private IReference findEqualParent(IReference ref, Set parents) {
 		if (this.equals(ref))
 			return this;
 		if (parentRef != null && !parents.contains(parentRef)) {
 			parents.add(parentRef);
 			return parentRef.findEqualParent(ref, parents);
 		}
 		return null;
 	}
 
 	public boolean isChildishReference() {
 		return childIsh;
 	}
 
 	public void recordDelete(String fieldId) {
 		if (childs == null)
 			return;
 		childs.remove(fieldId);
 	}
 
 	public IReference getPrototype(boolean resolveLocals) {
 		return getChild("prototype", true);
 	}
 
 	public void setPrototype(IReference ref) {
 		this.setChild("prototype", ref);
 	}
 
 	public void addModelElements(Collection toAdd) {
 		if (parent != null) {
 			if (isFunctionRef()) {
 				String[] params = null;
 				char[][] parameterNames = getParameterNames();
 				if (parameterNames != null) {
 					params = new String[parameterNames.length];
 					for (int i = 0; i < parameterNames.length; i++) {
 						params[i] = new String(parameterNames[i]);
 					}
 				}
 				MethodReference method = new MethodReference(parent, name,
 						params, getProposalInfo(), getReturnType());
 				toAdd.add(method);
 			} else {
 				FakeField fakeField = new FakeField(parent, name, offset,
						length);
 				fakeField.setProposalInfo(getProposalInfo());
 				toAdd.add(fakeField);
 			}
 		}
 	}
 
 	public boolean isFunctionRef() {
 		return fRef;
 	}
 
 	public StandardSelfCompletingReference setFunctionRef() {
 		fRef = true;
 		return this;
 	}
 
 	public char[][] getParameterNames() {
 		return parameterNames;
 	}
 
 	/**
 	 * @param parameterNames
 	 *            the parameterNames to set
 	 */
 	public void setParameterNames(char[][] parameterNames) {
 		this.parameterNames = parameterNames;
 	}
 
 	public String getProposalInfo() {
 		return proposalInfo;
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.reference.resolvers.SelfCompletingReference#getImageURL()
 	 */
 	public URL getImageURL() {
 		return imageUrl;
 	}
 
 	/**
 	 * @param imageUrl
 	 *            the imageUrl to set
 	 */
 	public void setImageUrl(URL imageUrl) {
 		this.imageUrl = imageUrl;
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.reference.resolvers.SelfCompletingReference#getReturnType()
 	 */
 	public String getReturnType() {
 		return returnType;
 	}
 
 	/**
 	 * @param returnType
 	 *            the returntype of this reference
 	 */
 	public void setReturnType(String returnType) {
 		this.returnType = returnType;
 	}
 
 	/**
 	 * @param proposalInfo
 	 *            the proposalInfo to set
 	 */
 	public void setProposalInfo(String proposalInfo) {
 		this.proposalInfo = proposalInfo;
 	}
 
 	public boolean isLocal() {
 		return local;
 	}
 
 	public void setLocal(boolean local) {
 		this.local = local;
 	}
 
 	/**
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	public boolean equals(Object obj) {
 		if (obj instanceof StandardSelfCompletingReference) {
 			StandardSelfCompletingReference ur = (StandardSelfCompletingReference) obj;
 			return ur.name.equals(name) && fRef == ur.fRef
 					&& childIsh == ur.childIsh && fRef == ur.fRef
 					&& local == ur.local && ur.offset == offset
 					&& length == ur.length;
 		}
 		return false;
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.reference.resolvers.SelfCompletingReference#getKind()
 	 */
 	public int getKind() {
 		return isFunctionRef() ? CompletionProposal.METHOD_REF
 				: CompletionProposal.LOCAL_VARIABLE_REF;
 	}
 
 	private static class MethodReference extends SourceMethod implements
 			IProposalHolder {
 
 		private final String[] parameters;
 		private final String proposalInfo;
 		private final String type;
 
 		/**
 		 * @param parent
 		 * @param name
 		 * @param type
 		 */
 		public MethodReference(ModelElement parent, String name,
 				String[] parameters, String proposalInfo, String type) {
 			super(parent, name);
 			this.parameters = parameters;
 			this.proposalInfo = proposalInfo;
 			this.type = type;
 		}
 
 		/**
 		 * @see org.eclipse.dltk.internal.core.SourceMethod#getParameters()
 		 */
 		public String[] getParameters() throws ModelException {
 			if (parameters == null)
 				return new String[0];
 			return parameters;
 		}
 
 		public String getProposalInfo() {
 			return proposalInfo;
 		}
 
 		/**
 		 * @see org.eclipse.dltk.internal.core.SourceMethod#getParameterInitializers()
 		 */
 		public String[] getParameterInitializers() throws ModelException {
 			return null;
 		}
 
 		/**
 		 * @see org.eclipse.dltk.internal.core.SourceMethod#getType()
 		 */
 		public String getType() throws ModelException {
 			return type;
 		}
 
 		/**
 		 * @see org.eclipse.dltk.internal.core.ModelElement#exists()
 		 */
 		public boolean exists() {
 			return true;
 		}
 
 		/**
 		 * @see org.eclipse.dltk.internal.core.SourceMethod#isConstructor()
 		 */
 		public boolean isConstructor() throws ModelException {
 			return false;
 		}
 	}
 
 	/**
 	 * @param b
 	 */
 	public void setParameterIndex(int index) {
 		parameterIndex = index;
 	}
 
 	/**
 	 * @return the parameter
 	 */
 	public int getParameterIndex() {
 		return parameterIndex;
 	}
 }
