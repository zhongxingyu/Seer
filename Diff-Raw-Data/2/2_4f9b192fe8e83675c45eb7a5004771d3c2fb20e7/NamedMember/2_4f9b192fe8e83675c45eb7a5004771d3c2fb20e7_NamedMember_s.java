 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core;
 
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IMember;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 
 public abstract class NamedMember extends Member {
 	/*
 	 * This element's name, or an empty <code>String</code> if this element
 	 * does not have a name.
 	 */
 	protected String name;
 
 	public NamedMember(ModelElement parent, String name) {
 		super(parent);
 		this.name = name;
 	}
 
 	public String getElementName() {
 		return this.name;
 	}
 
 	protected IScriptFolder getScriptFolder() {
 		return null;
 	}
 
 	public String getFullyQualifiedName(String enclosingTypeSeparator, boolean showParameters) throws ModelException {
 		String packageName = getScriptFolder().getElementName();
 		if (packageName.equals(IScriptFolder.DEFAULT_FOLDER_NAME)) {
 			return getTypeQualifiedName(enclosingTypeSeparator, showParameters);
 		}
 		return packageName + IScriptFolder.PACKAGE_DELIMETER_STR + getTypeQualifiedName(enclosingTypeSeparator, showParameters);
 	}
 
 	public String getTypeQualifiedName(String typeSeparator, boolean showParameters) throws ModelException {
 		NamedMember declaringType;
 		String thisName = this.name;
 		if (thisName.startsWith(typeSeparator))
 			return thisName;
 		switch (this.parent.getElementType()) {
 			case IModelElement.SOURCE_MODULE:
				thisName = typeSeparator + thisName;
 				if (showParameters) {					
 					StringBuffer buffer = new StringBuffer(thisName);
 					// appendTypeParameters(buffer);
 					if (DLTKCore.DEBUG) {
 						System.err.println("TODO: NamedMember: add type parameters");
 					}
 					return buffer.toString();
 				}
 				return thisName;
 			case IModelElement.TYPE:
 				declaringType = (NamedMember) this.parent;
 				break;
 			case IModelElement.FIELD:
 			case IModelElement.METHOD:
 				declaringType = (NamedMember) ((IMember) this.parent).getDeclaringType();
 				break;
 			default:
 				return null;
 		}
 		String declTName = "";
 		if( declaringType != null ) {
 			declTName = declaringType.getTypeQualifiedName(typeSeparator, showParameters);
 		}
 		StringBuffer buffer = new StringBuffer(declTName);
 		buffer.append(typeSeparator);
 		String simpleName = this.name.length() == 0 ? Integer.toString(this.occurrenceCount) : this.name;
 		buffer.append(simpleName);
 		if (showParameters) {
 			// appendTypeParameters(buffer);
 			if (DLTKCore.DEBUG) {
 				System.err.println("TODO: NamedMember: Add type parameters.");
 			}
 		}
 		return buffer.toString();
 	}
 		
 	protected String getKey(IField field, boolean forceOpen) throws ModelException {
 		StringBuffer key = new StringBuffer();
 		
 		// declaring class 
 		String declaringKey = getKey((IType) field.getParent(), forceOpen);
 		key.append(declaringKey);
 		
 		// field name
 		key.append('.');
 		key.append(field.getElementName());
 
 		return key.toString();
 	}
 	
 	protected String getKey(IMethod method, boolean forceOpen) throws ModelException {
 		StringBuffer key = new StringBuffer();
 		
 		// declaring class 
 		String declaringKey = getKey((IType) method.getParent(), forceOpen);
 		key.append(declaringKey);
 		
 		// selector
 		key.append('.');
 		String selector = method.getElementName();
 		key.append(selector);
 		
 		// type parameters
 		
 		
 		// parameters
 		key.append('(');
 		String[] parameters = method.getParameters();
 		for (int i = 0, length = parameters.length; i < length; i++)
 			key.append(parameters[i].replace('.', '/'));
 		key.append(')');
 		
 		// return type		
 		key.append('V');
 		
 		return key.toString();
 	}
 	
 	protected String getKey(IType type, boolean forceOpen) throws ModelException {
 		StringBuffer key = new StringBuffer();
 		key.append('L');
 		String packageName = type.getScriptFolder().getElementName();
 		key.append(packageName.replace('.', '/'));
 		if (packageName.length() > 0)
 			key.append('/');
 		String typeQualifiedName = type.getTypeQualifiedName("$");
 		ISourceModule cu = (ISourceModule) type.getAncestor(IModelElement.SOURCE_MODULE);
 		if (cu != null) {
 			String cuName = cu.getElementName();
 			String mainTypeName = cuName.substring(0, cuName.lastIndexOf('.'));
 			int end = typeQualifiedName.indexOf('$');
 			if (end == -1)
 				end = typeQualifiedName.length();
 			String topLevelTypeName = typeQualifiedName.substring(0, end);
 			if (!mainTypeName.equals(topLevelTypeName)) {
 				key.append(mainTypeName);
 				key.append('~');
 			}
 		}
 		key.append(typeQualifiedName);
 		key.append(';');
 		return key.toString();
 	}
 }
