 /*******************************************************************************
  * Copyright (c) 2001, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jem.internal.adapters.jdom;
 /*
  *  $RCSfile: JDOMAdaptor.java,v $
 *  $Revision: 1.9 $  $Date: 2007/01/30 14:39:13 $ 
  */
 
 import java.io.File;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 import org.eclipse.jdt.core.*;
 import org.eclipse.jem.java.*;
 import org.eclipse.jem.internal.java.adapters.JavaReflectionAdaptor;
 import org.eclipse.jem.internal.java.adapters.nls.ResourceHandler;
 import org.eclipse.jem.java.internal.impl.JavaRefFactoryImpl;
 /**
  * Insert the type's description here.
  * Creation date: (6/6/2000 4:42:50 PM)
  * @author: Administrator
  */
 public abstract class JDOMAdaptor extends JavaReflectionAdaptor {
 
 	protected final static JavaRefPackage JAVA_PACK = JavaRefFactoryImpl.getPackage();
 	protected IJavaProject sourceProject;
 	final public static int INVALID_LINENO = -1;
 	final protected Integer fLINENOLock = new Integer(INVALID_LINENO);
 	// This object is not static, as it is used as synchronization element.
 	private int fResolvedLineNo = INVALID_LINENO; // Line offset in source file
 	private int fResolvedColNo = INVALID_LINENO; // Column offset in source file
 	public JDOMAdaptor(Notifier target, IJavaProject workingProject) {
 		super(target);
 		setSourceProject(workingProject);
 	}
 	protected void clearSource() {
 		// To be overidden if needed.
 	}
 	
 	/**
 	 * Called by subclasses in canReflect(). If the target is not in a resource, or the
 	 * resource is not loaded, then it can't reflect. Mustn't reflect if the target
 	 * has been unloaded.
 	 * 
 	 * @return
 	 */
 	protected boolean isResourceLoaded() {
 		Resource res = ((EObject) getTarget()).eResource();
 		return res != null && res.isLoaded();
 	}
 	
 	/**
 	 * Scan for CRs and LFs within a character buffer
 	 * Creation date: (8/17/2001 2:14:13 PM)
 	 * @return int  LineNo at charOffset
 	 * @param charOffset int
 	 * @param buffer org.eclipse.jdt.core.IBuffer
 	 */
 	private void computeLineOffset(int charOffset, IBuffer buffer) {
 
 		fResolvedColNo = fResolvedLineNo = INVALID_LINENO;
 
 		if (buffer == null)
 			return;
 
 		char[] charBuff = buffer.getCharacters();
 
 		if (charBuff == null)
 			return;
 
 		int LineCount = 0;
 		int ColNo = 0;
 		for (int i = 0; i <= charOffset; i++) {
 			ColNo++;
 			if (charBuff[i] == '\r') {
 				LineCount++;
 				ColNo = 0;
 				if (charBuff[i + 1] == '\n')
 					i++; // skip LineFeed followed  a CR
 			} else if (charBuff[i] == '\n') {
 				LineCount++;
 				ColNo = 0;
 			}
 
 		}
 		fResolvedColNo = ColNo;
 		fResolvedLineNo = LineCount;
 	}
 	/**
 	 * computeMethodID - generate the unique ID to be used to identify a method.
 	 * Similar to a Signature, but hopefully more readable.
 	 * The name format will be:
 	 * 	simpleTypeName.methodName(my.package.Parm_Type1,parmType2
 	 * Note: This implementation is tightly coupled with ReflectionAdapter.getTypeNamesFromMethodID().
 	 */
 	public static String computeMethodID(IMethod jdomMethod) {
 		return computeMethodID(jdomMethod, jdomMethod.getDeclaringType(), null);
 	}
 	/**
 	 * computeMethodID - generate the unique ID to be used to identify a method.
 	 * Similar to a Signature, but hopefully more readable.
 	 * The name format will be:
 	 * 	simpleTypeName.methodName(my.package.Parm_Type1,parmType2
 	 * Note: This implementation is tightly coupled with ReflectionAdapter.getTypeNamesFromMethodID().
 	 */
 	public static String computeMethodID(IMethod jdomMethod, IType type, Map typeCache) {
 		StringBuffer out = new StringBuffer();
 		out.append(type.getTypeQualifiedName());
 		out.append(C_CLASS_MEMBER_DELIMITER);
 		out.append(jdomMethod.getElementName());
 		out.append(C_METHOD_PARM_DELIMITER);
 		String[] parmTypeNames = jdomMethod.getParameterTypes();
 		String parmName;
 		for (int i = 0; i < parmTypeNames.length; i++) {
 			parmName = convertJDOMtypeName(parmTypeNames[i]);
 			parmName = JDOMSearchHelper.getResolvedTypeName(parmName, type, typeCache);
 			out.append(parmName);
 			if (i < (parmTypeNames.length - 1))
 				out.append(C_PARM_PARM_DELIMITER);
 		}
 		try {
 			if (jdomMethod.isConstructor())
 				out.append(S_CONSTRUCTOR_TOKEN);
 		} catch (JavaModelException e) {
 		}
 		return out.toString();
 	}
 	/**
 	 * computeMethodName - generate the name to be used to identify a method.
 	 * For the moment, names are simple, and UUID's are complex.
 	 */
 	public static String computeMethodName(IMethod jdomMethod) {
 		return jdomMethod.getElementName();
 	}
 	/**
 	 * Java content has changed, but no structural changes that require
 	 * to reflectValues();  e.g., the body of a method has changed.
 	 * Creation date: (8/17/2001 10:47:58 AM)
 	 */
 	public void contentChanged() {
 		synchronized (fLINENOLock) {
 			fResolvedLineNo = INVALID_LINENO;
 			fResolvedColNo = INVALID_LINENO;
 		}
 	}
 	/**
 	 * computeMethodID - generate the unique ID to be used to identify a method.
 	 * Similar to a Signature, but hopefully more readable.
 	 * The name format will be:
 	 * 	methodName_parmType1_parmType2
 	 */
 	public static String convertJDOMtypeName(String jdomTypeName) {
 		return signatureToString(jdomTypeName);
 	}
 	/**
 	 * createJavaField - instantiate a Java Field based on the passed Java Model IField
 	 * We are deferring field contents assuming that its adaptor will reflect its details.
 	 */
 	public Field createJavaField(IField jdomField, XMIResource resource) {
 		String name = jdomField.getElementName();
 		Field newField = getJavaFactory().createField();
 		newField.setName(name);
 		resource.setID(newField, ((JavaClass) getTarget()).getName() + C_CLASS_MEMBER_DELIMITER + name);
 		return newField;
 	}
 	/**
 	 * createJavaMethod - instantiate a Java Method based on the passed Java Model IMethod
 	 * We are deferring method contents assuming that its adaptor will reflect its details.
 	 * We need to store enough info in the empty Method to find its Java source.
 	 * The UUID will eventually hold enough info to identify the source, so we use it.
 	 */
 	public Method createJavaMethod(IMethod jdomMethod, XMIResource resource) {
 		Method newMethod = getJavaFactory().createMethod();
 		// We use a simple name, but a complex ID 
 		newMethod.setName(computeMethodName(jdomMethod));
 		resource.setID(newMethod, computeMethodID(jdomMethod, getType(), getTypeResolutionCache()));
 		return newMethod;
 	}
 	protected IPath getBinaryPathFromQualifiedName(String qualifiedName) {
 		return new Path(qualifiedName.replace('.', File.separatorChar) + ".class"); //$NON-NLS-1$
 	}
 	public IType getBinaryType(String qualifiedName) {
 		try {
 			if (getSourceProject() != null) {
 				IJavaElement found = getSourceProject().findElement(getBinaryPathFromQualifiedName(qualifiedName));
 				if (found != null)
 					return ((IClassFile) found).getType();
 			}
 		} catch (JavaModelException jme) {
 			System.out.println(ResourceHandler.getString("Error_Looking_Up_Type_ERROR_", (new Object[] { qualifiedName, jme.getMessage()}))); //$NON-NLS-1$ = "Error looking up type: "
 		}
 		return null;
 	}
 	/**
 	 * Compute a column number from the ISourceRange offset
 	 * Cache the line number thereafter.  Source change will
 	 * Invoke the contentChanged() method.
 	 * Creation date: (8/17/2001 11:16:51 AM)
 	 * @return int
 	 */
 	public int getColNo() {
 
 		synchronized (fLINENOLock) {
 			if (fResolvedColNo == INVALID_LINENO)
 				resolveLineColNo();
 		}
 		return fResolvedColNo;
 	}
 	/**
 	 * Compute a line number from the ISourceRange offset
 	 * Cache the line number thereafter.  Source change will
 	 * Invoke the contentChanged() method.
 	 * Creation date: (8/17/2001 11:16:51 AM)
 	 * @return int
 	 */
 	public int getLineNo() {
 
 		synchronized (fLINENOLock) {
 			if (fResolvedLineNo == INVALID_LINENO)
 				resolveLineColNo();
 		}
 		return fResolvedLineNo;
 	}
 	/**
 	 * Insert the method's description here.
 	 * Creation date: (8/17/2001 1:18:29 PM)
 	 */
 	public abstract Object getReflectionSource();
 	/* 
 	 * Resolve a type name in the context of a Type.
 	 * (Borrowed from org.eclipse.jdt.ui.codemanipulation.StubUtility.getResolvedTypeName())
 	 * The input is a simple or qualified name, NOT a signature
 	 * The output will be a qualified name, NOT a signature
 	 */
 	public static String getResolvedTypeName(String typeName, IType declaringType) {
 		String name = typeName;
 		try {
 			name = JDOMSearchHelper.resolveSimpleTypeName(declaringType, typeName);
 		} catch (JavaModelException e) {
 			// ignore
 		}
 		return name;
 	}
 
 	protected IJavaProject getSourceProject() {
 		return sourceProject;
 	}
 	protected abstract IType getType();
 	protected abstract Map getTypeResolutionCache();
 
 	public void releaseSourceType() {
 		flushReflectedValuesIfNecessary(true); // induce clients to get Notified.
 	}
 
 	public Notification releaseSourceTypeNoNotification() {
 		return flushReflectedValuesIfNecessaryNoNotification(true); // induce clients to get Notified.
 	}
 	/**
 	 * Insert the method's description here.
 	 * Creation date: (8/21/2001 8:09:34 AM)
 	 */
 	private void resolveLineColNo() {
 
 		IMember rs = (IMember) getReflectionSource();
 		if (rs != null) {
 			int offset = INVALID_LINENO;
 			try {
 				ISourceRange sr = rs.getNameRange();
 				if (sr.getLength() <= 0)
 					return;
 				offset = sr.getOffset();
 			} catch (JavaModelException je) {
 				return;
 			}
 			ICompilationUnit cu = rs.getCompilationUnit();
 			if (cu != null) {
 				try {
 					IBuffer buffer = cu.getBuffer();
 					computeLineOffset(offset, buffer);
 				} catch (JavaModelException je) {
 				}
 			}
 		}
 	}
 	protected void setSourceProject(IJavaProject workingProject) {
 		sourceProject = workingProject;
 	}
 	/**
 	 * Converts a type signature to a readable string.
 	 *
 	 * Uses Signature.toString(), then tries to undo bad replacement for inner classes.
	 * 
	 * Bug: 166226 [https://bugs.eclipse.org/bugs/show_bug.cgi?id=166226]
	 * Update to use the erasure type from the signature in order to 
	 * tolerate JDK 5 generics.
 	 *
 	 */
 	public static String signatureToString(String signature) throws IllegalArgumentException {
 		boolean hasDollar = (signature.indexOf(Signature.C_DOLLAR) != -1);
		//begin 166226 fix
		String result = Signature.getTypeErasure(signature);
		result = Signature.toString(result);
		//end 166226 fix
 		if (hasDollar) {
 			int newPos = result.lastIndexOf("."); //$NON-NLS-1$
 			if (newPos != -1) {
 				result = result.substring(0, newPos) + "$" + result.substring(newPos + 1); //$NON-NLS-1$
 			}
 		}
 		return result;
 	}
 	/**
 	 * setType - set our type here
 	 */
 	protected String typeNameFromSignature(String sig) {
 		return typeNameFromSignature(sig, getType());
 	}
 	/**
 	 * setType - set our type here
 	 */
 	protected String typeNameFromSignature(String sig, IType parent) {
 		return typeNameFromSignature(sig, parent, getTypeResolutionCache());
 	}
 	/**
 	 * setType - set our type here
 	 */
 	public static String typeNameFromSignature(String sig, IType parent, Map typeCache) {
 		String result;
 		String componentSignature = Signature.getElementType(sig);
 		int arrayDimensions = Signature.getArrayCount(sig);
 		result = JDOMSearchHelper.getResolvedTypeName(signatureToString(componentSignature), parent, typeCache);
 		for (int i = 0; i < arrayDimensions; i++) {
 			result = result + "[]"; //$NON-NLS-1$
 		}
 		return result;
 	}
 	/**
 	 * @deprecated
 	 * @see org.eclipse.jem.internal.adapters.jdom.JDOMSearchHelper#findType(String, boolean, IJavaProject, JDOMAdaptor)
 	 */
 	public IType getType(String qualifiedName) {
 		return JDOMSearchHelper.findType(qualifiedName, false, getSourceProject(), this);
 	}
 }
