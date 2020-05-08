 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.core.mixin;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.dltk.compiler.ISourceElementRequestor;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IParent;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 
 public class MixinSourceElementRequestor implements ISourceElementRequestor {
 	private List<String> path = new ArrayList<String>();
 	private IMixinRequestor requestor;
 	private boolean signature = false;
 	private ISourceModule module;
 
 	public MixinSourceElementRequestor(IMixinRequestor requestor,
 			boolean signature, ISourceModule module) {
 		this.requestor = requestor;
 		this.signature = signature;
 		this.module = module;
 	}
 
 	protected void enterElement(String path) {
 		this.path.add(path);
 	}
 
 	private IModelElement getElement(IModelElement parent, int index) {
 		if (path.size() == index) {
 			return parent;
 		}
 		if (parent instanceof IParent) {
 			IParent par = (IParent) parent;
 			IModelElement[] children = null;
 			try {
 				children = par.getChildren();
 			} catch (ModelException e) {
 				e.printStackTrace();
 				throw new RuntimeException(e);
 			}
 			for (int i = 0; i < children.length; ++i) {
 				if (children[i].getElementName().equals(path.get(index))) {
 					IModelElement el = getElement(children[i], index + 1);
 					if (el != null) {
 						return el;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	protected IModelElement getModelElement() {
 		if (!signature) {
 			return null;
 		}
 		if (this.path.size() == 0) {
 			return this.module;
 		}
 		return getElement(this.module, 0);
 	}
 
 	protected void exitElement() {
 		if (path.size() > 0) {
 			this.path.remove(this.path.size() - 1);
 		}
 	}
 
 	protected String getKey() {
 		StringBuffer buffer = new StringBuffer();
 		for (int i = 0; i < path.size(); ++i) {
 			buffer.append(path.get(i));
 			if (i != path.size() - 1) {
 				buffer.append(IMixinRequestor.MIXIN_NAME_SEPARATOR);
 			}
 		}
 		return buffer.toString();
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public void acceptFieldReference(String fieldName, int sourcePosition) {
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public void acceptMethodReference(String methodName, int argCount,
 			int sourcePosition, int sourceEndPosition) {
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public void acceptPackage(int declarationStart, int declarationEnd,
 			String name) {
 	}
 
 	public void acceptTypeReference(String typeName, int sourcePosition) {
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public boolean enterFieldCheckDuplicates(FieldInfo info) {
 		return false;
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public void enterMethodRemoveSame(MethodInfo info) {
 	}
 
 	public void enterModule() {
 	}
 
 	public void enterModuleRoot() {
 	}
 
 	public boolean enterTypeAppend(String fullName, String delimiter) {
 		return false;
 	}
 
 	public void exitModule(int declarationEnd) {
 	}
 
 	public void exitModuleRoot() {
 	}
 
 	public void exitField(int declarationEnd) {
 		exitElement();
 	}
 
 	public void exitMethod(int declarationEnd) {
 		exitElement();
 	}
 
 	public void exitType(int declarationEnd) {
 		exitElement();
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public void enterField(FieldInfo info) {
 		enterElement(info.name);
 		IMixinRequestor.ElementInfo elInfo = new IMixinRequestor.ElementInfo();
 		elInfo.key = getKey();
 		if (signature) {
 			elInfo.object = getModelElement();
 		}
 		requestor.reportElement(elInfo);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public void enterMethod(MethodInfo info) {
 		enterElement(info.name);
 		IMixinRequestor.ElementInfo elInfo = new IMixinRequestor.ElementInfo();
 		elInfo.key = getKey();
 		if (signature) {
 			elInfo.object = getModelElement();
 		}
 		requestor.reportElement(elInfo);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public void enterType(TypeInfo info) {
 		enterElement(info.name);
 		IMixinRequestor.ElementInfo elInfo = new IMixinRequestor.ElementInfo();
 		elInfo.key = getKey();
 		if (signature) {
 			elInfo.object = getModelElement();
 		}
 		requestor.reportElement(elInfo);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public void acceptImport(ImportInfo importInfo) {
 	}
 }
