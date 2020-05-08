 /******************************************************************************* 
  * Copyright (c) 2011 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.jst.web.validation;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.osgi.util.NLS;
 import org.jboss.tools.common.log.LogHelper;
 import org.jboss.tools.common.meta.constraint.impl.XAttributeConstraintQClassName;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.util.EclipseJavaUtil;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.common.validation.IProjectValidationContext;
 import org.jboss.tools.common.validation.ValidationErrorManager;
 import org.jboss.tools.jst.web.WebModelPlugin;
 
 /**
  * @author Viacheslav Kabanovich
  */
 public class CheckClass extends Check {
 	XAttributeConstraintQClassName constraint = new XAttributeConstraintQClassName();
 	boolean allowsPrimitive = false;
 	String implementsType = null;
 	String extendsType = null;
 
 	public CheckClass(ValidationErrorManager manager, String preference, String attr) {
 		super(manager, preference, attr);
 	}
 
 	public CheckClass(ValidationErrorManager manager, String preference, String attr, boolean allowsPrimitive, String implementsType, String extendsType) {
 		super(manager, preference, attr);
 		this.allowsPrimitive = allowsPrimitive;
 		this.implementsType = implementsType;
 		this.extendsType = extendsType;
 	}
 
 	public void check(XModelObject object) {
 		if(attr == null) return;
 		String value = object.getAttributeValue(attr);
 		if(value == null) {
			System.out.println("Entity=" + object.getModelEntity().getName());
			System.out.println("Attr=" + attr);
 			return;
 		}
 		if(value.length() == 0 || isJavaLang(value)) return;
 		if(isPrimitive(value)) {
 			if(!allowsPrimitive) {
 				fireNotExist(object, preference, attr, value);
 			}
 			return;
 		}
 		if(!checkQualifiedName(value)) {
 			fireInvalid(object, attr, attr, value);
 			return;
 		}
 		IType type = getValidType(value, object);
 
 		IFile f = (IFile)object.getAdapter(IFile.class);
 		if(f != null) {
 			IProjectValidationContext context = manager.getValidationContext();
 			if(context != null) {
 				context.addLinkedCoreResource(WebXMLCoreValidator.SHORT_ID, value, f.getFullPath(), true);
 				if(type != null && type.getResource() != null && type.exists()) {
 					context.addLinkedCoreResource(WebXMLCoreValidator.SHORT_ID, type.getResource().getFullPath().toOSString(), f.getFullPath(), true);
 				}
 			}
 		}
 		
 		if(type != null) {
 			String mustImpl = null;
 			try { mustImpl = checkImplements(object, type); } catch (Exception e) {
 	        	LogHelper.logError("org.jboss.tools.jst.web.verification", e); //$NON-NLS-1$
 			}
 			if(mustImpl != null) {
 				fireImplements(object, preference, attr, value, mustImpl);
 			}
 			String mustExtend = null;
 			try { mustExtend = checkExtends(object, type); } catch (Exception e) {
 	        	LogHelper.logError("org.jboss.tools.jst.web.verification", e); //$NON-NLS-1$
 			}
 			if(mustExtend != null) {
 				fireExtends(object, preference, attr, value, mustExtend);
 			}
 			return;
 		}
 		fireNotExist(object, preference, attr, value);
 	}
 	
 	private boolean checkQualifiedName(String value) {
 		return constraint.accepts(value);
 	}
 	
 	private String checkImplements(XModelObject object, IType type) throws Exception {
 		if("java.lang.Class".equals(type.getFullyQualifiedName())) return null; //$NON-NLS-1$
 		String impl = implementsType;
 		if(impl == null || impl.length() == 0) return null;
 		String[] is = type.getSuperInterfaceNames();
 		for (int i = 0; i < is.length; i++) {
 			String f = EclipseJavaUtil.resolveType(type, is[i]);
 			if(f != null && f.equals(impl)) return null; 
 		}
 		if(type.isInterface()) return impl;
 		String f = type.getSuperclassName();
 		if(f == null || f.length() == 0 || "java.lang.Object".equals(f)) return impl; //$NON-NLS-1$
 		f = EclipseJavaUtil.resolveType(type, f);
 		if(f == null || f.length() == 0 || "java.lang.Object".equals(f)) return impl; //$NON-NLS-1$
 		type = getValidType(f, object);
 		if(type == null) return impl;
 		return checkImplements(object, type);
 	}
 	
 	private String checkExtends(XModelObject object, IType type) throws Exception {
 		if(type.isInterface()) return null;
 		if("java.lang.Class".equals(type.getFullyQualifiedName())) return null; //$NON-NLS-1$
 		String ext = extendsType;
 		if(ext == null || ext.length() == 0 || ext.equals(type.getFullyQualifiedName())) return null;
 		String f = type.getSuperclassName();
 		if(f == null || f.length() == 0 || "java.lang.Object".equals(f)) return ext; //$NON-NLS-1$
 		if(f.equals(ext)) return null;
 		f = EclipseJavaUtil.resolveType(type, f);
 		if(f == null || f.length() == 0 || "java.lang.Object".equals(f)) return ext; //$NON-NLS-1$
 		if(f.equals(ext)) return null;
 		type = getValidType(f, object);
 		if(type == null) return ext;
 		return checkExtends(object, type);
 	}
 	
 	private boolean isPrimitive(String value) {
 		return ".int.boolean.char.byte.double.float.long.short.".indexOf("." + value + ".") >= 0; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 	}
 
 	private boolean isJavaLang(String value) {
 		if(value.indexOf('.') < 0) {
 			return ".String.Integer.Boolean.Character.Byte.Double.Float.Long.Short.".indexOf("." + value + ".") >= 0; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		} else if(value.startsWith("java.lang.")) { //$NON-NLS-1$
 			return isJavaLang(value.substring(10));
 		} else {
 			return false;
 		}
 	}
 
 	protected void fireImplements(XModelObject object, String id, String attr, String value, String interfaceName) {
 		fireMessage(object, NLS.bind(WebXMLValidatorMessages.CLASS_NOT_IMPLEMENTS, new Object[] {attr, value, interfaceName}));
 	}
 
 	protected void fireExtends(XModelObject object, String id, String attr, String value, String superName) {
 		fireMessage(object, NLS.bind(WebXMLValidatorMessages.CLASS_NOT_EXTENDS, new Object[] {attr, value, superName}));
 	}
 	protected void fireInvalid(XModelObject object, String id, String attr, String value) {
 		fireMessage(object, NLS.bind(WebXMLValidatorMessages.CLASS_NOT_VALID, attr, value));
 	}
 	protected void fireNotExist(XModelObject object, String id, String attr, String value) {
 		fireMessage(object, NLS.bind(WebXMLValidatorMessages.CLASS_NOT_EXISTS, attr, value));
 	}
 
 	public IType getValidType(String className, XModelObject o) {
 		IProject project = EclipseResourceUtil.getProject(o);
 		if(project == null) return null;
 		IType type = EclipseResourceUtil.getValidType(project, className);
 		if(type != null) return type;
 		IJavaProject javaProject = EclipseResourceUtil.getJavaProject(project);
 		if(javaProject != null) {
 			try {
 				type = EclipseJavaUtil.findType(javaProject, className);
 			} catch (JavaModelException e) {
 				WebModelPlugin.getDefault().logError(e);
 			}
 			if(type != null) return type;
 		}
 		if(EclipseResourceUtil.isContainedInOutput(project, className)) {
 			// Eclipse does not have type in this case, 
 			// so we return something instead of null 
 			// This is ok while result is only compared to null
 			return EclipseResourceUtil.getValidType(project, "java.lang.Class"); //$NON-NLS-1$
 		}
 		return null; 
 	}
     
 }
 
