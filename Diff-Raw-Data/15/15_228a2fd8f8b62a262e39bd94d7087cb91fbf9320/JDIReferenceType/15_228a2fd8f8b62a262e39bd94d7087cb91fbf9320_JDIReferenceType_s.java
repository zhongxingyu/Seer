 /*******************************************************************************
  * Copyright (c) 2000, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jdt.internal.debug.core.model;
 
 import java.text.MessageFormat;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.jdt.core.Signature;
 import org.eclipse.jdt.debug.core.IJavaClassObject;
 import org.eclipse.jdt.debug.core.IJavaFieldVariable;
 import org.eclipse.jdt.debug.core.IJavaObject;
 import org.eclipse.jdt.debug.core.IJavaReferenceType;
 import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
 
 import com.sun.jdi.AbsentInformationException;
 import com.sun.jdi.ArrayType;
 import com.sun.jdi.ClassNotLoadedException;
 import com.sun.jdi.Field;
 import com.sun.jdi.ReferenceType;
 import com.sun.jdi.Type;
 
 /**
  * References a class, interface, or array type.
  */
 public abstract class JDIReferenceType extends JDIType implements IJavaReferenceType {
 	
 	// field names declared in this type
 	private String[] fDeclaredFields = null;
 	// field names declared in this type, super types, implemented interaces and superinterfaces
 	private String[] fAllFields = null;
 
 	/**
 	 * Constructs a new reference type in the given target.
 	 * 
 	 * @param target associated vm
 	 * @param type reference type
 	 */
 	public JDIReferenceType(JDIDebugTarget target, Type type) {
 		super(target, type);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getAvailableStrata()
 	 */
 	public String[] getAvailableStrata() {
 		List strata = getReferenceType().availableStrata();
 		return (String[])strata.toArray(new String[strata.size()]);
 	}
 
 	/**
 	 * Returns the underlying reference type.
 	 * 
 	 * @return the underlying reference type
 	 */
 	protected ReferenceType getReferenceType() {
 		return (ReferenceType)getUnderlyingType();
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getDefaultStratum()
 	 */
 	public String getDefaultStratum() {
 		return getReferenceType().defaultStratum();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getField(java.lang.String)
 	 */
 	public IJavaFieldVariable getField(String name) throws DebugException {
 		try {
 			ReferenceType type = (ReferenceType)getUnderlyingType();
 			Field field = type.fieldByName(name);
 			if (field != null && field.isStatic()) {
 				return new JDIFieldVariable(getDebugTarget(), field, type);
 			}			
 		} catch (RuntimeException e) {
 			getDebugTarget().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIClassType.exception_while_retrieving_field"), new String[] {e.toString(), name}), e); //$NON-NLS-1$
 		}
 		// it is possible to return null		
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getClassObject()
 	 */
 	public IJavaClassObject getClassObject() throws DebugException {
 		try {
 			ReferenceType type= (ReferenceType)getUnderlyingType();
 			return (IJavaClassObject)JDIValue.createValue(getDebugTarget(), type.classObject());
 		} catch (RuntimeException e) {
 			getDebugTarget().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIClassType.exception_while_retrieving_class_object"), new String[] {e.toString()}), e); //$NON-NLS-1$
 		}
 		// execution will not fall through to here,
 		// as #requestFailed will throw an exception
 		return null;
 	}	
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getAllFieldNames()
 	 */
 	public String[] getAllFieldNames() throws DebugException {
 		if (fAllFields == null) {
 			try {
 				List fields = ((ReferenceType)getUnderlyingType()).allFields();
 				fAllFields = new String[fields.size()];
 				Iterator iterator = fields.iterator();
 				int i = 0;
 				while (iterator.hasNext()) {
 					Field field = (Field)iterator.next();
 					fAllFields[i] = field.name();
 					i++;
 				}
 			} catch (RuntimeException e) {
 				getDebugTarget().targetRequestFailed(JDIDebugModelMessages.getString("JDIReferenceType.2"), e); //$NON-NLS-1$
 			}			
 		}			
 		return fAllFields;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getDeclaredFieldNames()
 	 */
 	public String[] getDeclaredFieldNames() throws DebugException {
 		if (fDeclaredFields == null) {
 			try {
 				List fields = ((ReferenceType)getUnderlyingType()).fields();
 				fDeclaredFields = new String[fields.size()];
 				Iterator iterator = fields.iterator();
 				int i = 0;
 				while (iterator.hasNext()) {
 					Field field = (Field)iterator.next();
 					fDeclaredFields[i] = field.name();
 					i++;
 				}
 			} catch (RuntimeException e) {
 				getDebugTarget().targetRequestFailed(JDIDebugModelMessages.getString("JDIReferenceType.3"), e); //$NON-NLS-1$
 			}			
 		}
 		return fDeclaredFields;
 	}
 	
 	/**
 	 * Return the source paths for the given stratum.
 	 */
 	public String[] getSourcePaths(String stratum) {
 		try {
 			List sourcePaths= getReferenceType().sourcePaths(stratum);
 			return (String[]) sourcePaths.toArray(new String[sourcePaths.size()]);
 		} catch (AbsentInformationException e) {
 			return new String[0];
 		}
 	}
 
     /* (non-Javadoc)
      * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getClassLoaderObject()
      */
     public IJavaObject getClassLoaderObject() throws DebugException {
         try {
             ReferenceType type= (ReferenceType)getUnderlyingType();
             return (IJavaObject)JDIValue.createValue(getDebugTarget(), type.classLoader());
         } catch (RuntimeException e) {
             getDebugTarget().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIReferenceType.0"), new String[] {e.toString()}), e); //$NON-NLS-1$
         }
         // execution will not fall through to here,
         // as #requestFailed will throw an exception
         return null;
     }
     
 
 	static public String getGenericName(ReferenceType type) throws DebugException {
 		if (type instanceof ArrayType) {
 			try {
 				Type componentType;
 					componentType= ((ArrayType)type).componentType();
 				if (componentType instanceof ReferenceType) {
 					return getGenericName((ReferenceType)componentType) + "[]"; //$NON-NLS-1$
 				}
 				return type.name();
 			} catch (ClassNotLoadedException e) {
				throw new DebugException(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), JDIDebugPlugin.INTERNAL_ERROR, null, e));
 			}
 		}
 		StringBuffer res= new StringBuffer(Signature.toString(type.signature()).replace('/', '.'));
 		String genericSignature= type.genericSignature();
 		if (genericSignature != null) {
 			String[] typeParameters= Signature.getTypeParameters(genericSignature);
 			if (typeParameters.length > 0) {
 				res.append('<').append(Signature.getTypeVariable(typeParameters[0]));
 				for (int i= 1; i < typeParameters.length; i++) {
 					res.append(',').append(Signature.getTypeVariable(typeParameters[i]));
 				}
 				res.append('>');
 			}
 		}
 		return res.toString();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getGenericSignature()
 	 */
 	public String getGenericSignature() throws DebugException {
 		return getReferenceType().genericSignature();
 	}
 
 }
