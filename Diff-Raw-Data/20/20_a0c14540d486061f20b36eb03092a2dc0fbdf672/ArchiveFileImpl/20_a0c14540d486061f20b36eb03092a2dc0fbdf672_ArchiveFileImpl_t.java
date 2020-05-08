 /*******************************************************************************
  * Copyright (c) 2005 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Ian Trimble - initial API and implementation
  *******************************************************************************/ 
 package org.eclipse.jst.jsf.core.internal.jsflibraryregistry.impl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.internal.Messages;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.ArchiveFile;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibrary;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibraryRegistryPackage;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.PluginProvidedJSFLibrary;
 import org.eclipse.osgi.util.NLS;
 import org.osgi.framework.Bundle;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Archive File</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.jst.jsf.core.internal.jsflibraryregistry.impl.ArchiveFileImpl#isRelativeToWorkspace <em>Relative To Workspace</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.core.internal.jsflibraryregistry.impl.ArchiveFileImpl#getSourceLocation <em>Source Location</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.core.internal.jsflibraryregistry.impl.ArchiveFileImpl#getRelativeDestLocation <em>Relative Dest Location</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.core.internal.jsflibraryregistry.impl.ArchiveFileImpl#getJSFLibrary <em>JSF Library</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class ArchiveFileImpl extends EObjectImpl implements ArchiveFile {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2005 Oracle Corporation";
 
 	/**
 	 * The default value of the '{@link #isRelativeToWorkspace() <em>Relative To Workspace</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isRelativeToWorkspace()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean RELATIVE_TO_WORKSPACE_EDEFAULT = true;
 
 	/**
 	 * The cached value of the '{@link #isRelativeToWorkspace() <em>Relative To Workspace</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isRelativeToWorkspace()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean relativeToWorkspace = RELATIVE_TO_WORKSPACE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getSourceLocation() <em>Source Location</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSourceLocation()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String SOURCE_LOCATION_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getSourceLocation() <em>Source Location</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSourceLocation()
 	 * @generated
 	 * @ordered
 	 */
 	protected String sourceLocation = SOURCE_LOCATION_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getRelativeDestLocation() <em>Relative Dest Location</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getRelativeDestLocation()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String RELATIVE_DEST_LOCATION_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getRelativeDestLocation() <em>Relative Dest Location</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getRelativeDestLocation()
 	 * @generated
 	 * @ordered
 	 */
 	protected String relativeDestLocation = RELATIVE_DEST_LOCATION_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected ArchiveFileImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected EClass eStaticClass() {
 		return JSFLibraryRegistryPackage.eINSTANCE.getArchiveFile();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getSourceLocation() {
 		return sourceLocation;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Enhanced to not only set the location but also to attempt to determine
 	 * if the location passed is relative to the current workspace. If so, only
 	 * the relative location is stored. If not, the full location as passed is
 	 * stored and the relativeToWorkspace property is set to false. To override
 	 * this behaviour and set the absolute location as passed regardless, call
 	 * setRelativeToWorkspace(false) BEFORE calling this method.
 	 *  
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void setSourceLocation(String newSourceLocation) {
 		String oldSourceLocation = sourceLocation;
 		if (relativeToWorkspace) {
 			IWorkspaceRoot workspaceRoot = getWorkspaceRoot();
 			if (workspaceRoot != null) {
 				IPath wsRootPath = workspaceRoot.getLocation();
 				IPath srcPath = new Path(newSourceLocation);
 				if (workspaceRoot.findMember(srcPath) != null) {
 					sourceLocation = newSourceLocation;
 				} else if (wsRootPath.isPrefixOf(srcPath)) {
 					int segmentsMatched = wsRootPath.matchingFirstSegments(srcPath);
 					srcPath = srcPath.removeFirstSegments(segmentsMatched).setDevice(null);
 					sourceLocation = srcPath.toOSString();
 				} else {
 					sourceLocation = newSourceLocation;
 					relativeToWorkspace = false;
 				}
 			}
 		} else {
 			sourceLocation = newSourceLocation;
 		}
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JSFLibraryRegistryPackage.ARCHIVE_FILE__SOURCE_LOCATION, oldSourceLocation, sourceLocation));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isRelativeToWorkspace() {
 		return relativeToWorkspace;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * To override workspace-relative recognition behaviour, be sure to call
 	 * this method with a false value BEFORE calling setLocation(String).
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRelativeToWorkspace(boolean newRelativeToWorkspace) {
 		boolean oldRelativeToWorkspace = relativeToWorkspace;
 		relativeToWorkspace = newRelativeToWorkspace;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JSFLibraryRegistryPackage.ARCHIVE_FILE__RELATIVE_TO_WORKSPACE, oldRelativeToWorkspace, relativeToWorkspace));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getRelativeDestLocation() {
 		return relativeDestLocation;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRelativeDestLocation(String newRelativeDestLocation) {
 		String oldRelativeDestLocation = relativeDestLocation;
 		relativeDestLocation = newRelativeDestLocation;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JSFLibraryRegistryPackage.ARCHIVE_FILE__RELATIVE_DEST_LOCATION, oldRelativeDestLocation, relativeDestLocation));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public JSFLibrary getJSFLibrary() {
 		if (eContainerFeatureID != JSFLibraryRegistryPackage.ARCHIVE_FILE__JSF_LIBRARY) return null;
 		return (JSFLibrary)eContainer;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setJSFLibrary(JSFLibrary newJSFLibrary) {
 		if (newJSFLibrary != eContainer || (eContainerFeatureID != JSFLibraryRegistryPackage.ARCHIVE_FILE__JSF_LIBRARY && newJSFLibrary != null)) {
 			if (EcoreUtil.isAncestor(this, newJSFLibrary))
 				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
 			NotificationChain msgs = null;
 			if (eContainer != null)
 				msgs = eBasicRemoveFromContainer(msgs);
 			if (newJSFLibrary != null)
 				msgs = ((InternalEObject)newJSFLibrary).eInverseAdd(this, JSFLibraryRegistryPackage.JSF_LIBRARY__ARCHIVE_FILES, JSFLibrary.class, msgs);
 			msgs = eBasicSetContainer((InternalEObject)newJSFLibrary, JSFLibraryRegistryPackage.ARCHIVE_FILE__JSF_LIBRARY, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JSFLibraryRegistryPackage.ARCHIVE_FILE__JSF_LIBRARY, newJSFLibrary, newJSFLibrary));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getPath() {
 		String path = null;
 		String resolvedSourceLocation = getResolvedSourceLocation();
 		if (resolvedSourceLocation != null) {
 			int iPos = resolvedSourceLocation.lastIndexOf('/');
 			if (iPos < 0) {
 				iPos = resolvedSourceLocation.lastIndexOf('\\');
 			}
 			if (iPos < 1) {
 				path = ""; //$NON-NLS-1$
 			} else {
 				path = resolvedSourceLocation.substring(0, iPos);
 			}
 		}
 		return path;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getName() {
 		String name = null;
 		String resolvedSourceLocation = getResolvedSourceLocation();
 		if (resolvedSourceLocation != null) {
 			int iPos = resolvedSourceLocation.lastIndexOf('/');
 			if (iPos < 0) {
 				iPos = resolvedSourceLocation.lastIndexOf('\\');
 			}
 			if (iPos < 0 || iPos == resolvedSourceLocation.length() - 1) {
 				name = resolvedSourceLocation;
 			} else {
 				name = resolvedSourceLocation.substring(iPos + 1);
 			}
 		}
 		return name;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean exists() {
 		boolean exists = false;
 		if (getJSFLibrary() instanceof PluginProvidedJSFLibrary) {
 			Bundle bundle = getBundle();
 			if (bundle != null) {
 				exists = bundle.getEntry(sourceLocation) != null;
 			}
 		} else {
 			String resolvedSourceLocation = getResolvedSourceLocation();
 			if (resolvedSourceLocation != null) {
 				exists = new File(resolvedSourceLocation).exists();
 			}
 		}
 		return exists;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean equals(Object object) {
 		boolean equal = false;
 		if (object != null && object instanceof ArchiveFile) {
 			String resolvedSourceLocation = getResolvedSourceLocation();
 			String objResolvedSourceLocation = ((ArchiveFile)object).getResolvedSourceLocation();
 			if (resolvedSourceLocation == null && objResolvedSourceLocation == null) {
 				equal = true;
 			} else if (resolvedSourceLocation != null) {
 				equal = resolvedSourceLocation.equals(objResolvedSourceLocation);
 			}
 		}
 		return equal;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public int hashCode() {
 		return getResolvedSourceLocation() != null ? getResolvedSourceLocation().hashCode() : 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean copyTo(String baseDestLocation) {
 		boolean copied = false;
 		InputStream in = null;
 		OutputStream out = null;
 		IPath outPath = new Path(baseDestLocation);
 		if (relativeDestLocation != null) {
 			outPath = outPath.append(relativeDestLocation);
 		}
 		outPath = outPath.append(getName());
 		try {
 			if (!outPath.toFile().exists()) {
 				out = new FileOutputStream(outPath.toOSString());
 			} else {
 				return copied;
 			}
 			if (getJSFLibrary() instanceof PluginProvidedJSFLibrary) {
 				Bundle bundle = getBundle();
 				URL inURL = bundle.getEntry(sourceLocation);
 				if (inURL != null) {
 					in = inURL.openStream();
 				} else {
 					JSFCorePlugin.log(
 							IStatus.ERROR,
 							NLS.bind(Messages.ArchiveFileImpl_CannotLocatePluginRelativeFile,
 									sourceLocation));
 					return copied;
 				}
 			} else {
 				in = new FileInputStream(getResolvedSourceLocation());
 			}
 			byte[] buf = new byte[1024];
 			int len = 0;
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 			copied = true;
 		} catch(FileNotFoundException fnfe) {
 			JSFCorePlugin.log(IStatus.ERROR, Messages.ArchiveFileImpl_CannotCopyFile, fnfe);
 		} catch(IOException ioe) {
 			JSFCorePlugin.log(IStatus.ERROR, Messages.ArchiveFileImpl_CannotCopyFile, ioe);
 		} finally {
 			try {
 				if (in != null) {
 					in.close();
 				}
 				if (out != null) {
 					out.close();
 				}
 			} catch(IOException ioe) {
 				JSFCorePlugin.log(IStatus.WARNING, Messages.ArchiveFileImpl_CannotCloseFile, ioe);
 			}
 		}
 		return copied;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getResolvedSourceLocation() {
 		String resolvedSourceLocation = null;
		/**
		 * The implementation to support PluginProvidedJSFLibrary assume the following.
		 * 1. Plugin provided JSF library needs to be distributed as an expanded folder.
		 * 2. JARs for plugin provided JSF library need to reside inside the folder mentioned in item 1 above.
		 * 3. Each JAR needs to specify a relative path for the folder mentioned in item 1 above.
		 * 
		 * Fix for bug 144954.
		 */
 		if (getJSFLibrary() instanceof PluginProvidedJSFLibrary) {
 			Bundle bundle = getBundle();
 			if (bundle != null) {
				//resolvedSourceLocation = appendSeparator(bundleLocation) + sourceLocation;
				try {
					Path srcPath = new Path(sourceLocation);
					URL fileURL = Platform.find(bundle, srcPath);
					URL url = Platform.resolve(fileURL);
					resolvedSourceLocation = url.getPath();
				} catch (IOException e) {
 					resolvedSourceLocation = sourceLocation;
 				}
 			} else {
 				resolvedSourceLocation = sourceLocation;
 			}
 		} else {
 			if (isRelativeToWorkspace()) {
 				IWorkspaceRoot workspaceRoot = getWorkspaceRoot();
 				if (workspaceRoot != null) {
 					String workspaceLocation = workspaceRoot.getLocation().toOSString();
 					if (workspaceLocation != null) {
 						resolvedSourceLocation = appendSeparator(workspaceLocation) + sourceLocation;
 					} else {
 						resolvedSourceLocation = sourceLocation;
 					}
 				} else {
 					resolvedSourceLocation = sourceLocation;
 				}
 			} else {
 				resolvedSourceLocation = sourceLocation;
 			}
 		}
 		return resolvedSourceLocation;
 	}
 
 	/**
 	 * Gets the Bundle instance by parent PluginProvidedJSFLibrary instance's
 	 * pluginID property, if and only if the parent JSFLibrary instance IS a
 	 * PluginProvidedJSFLibrary, else returns null.
 	 * @return Bundle instance, or null if not located or applicable
 	 */
 	protected Bundle getBundle() {
 		Bundle bundle = null;
 		if (getJSFLibrary() instanceof PluginProvidedJSFLibrary) {
 			String pluginID = ((PluginProvidedJSFLibrary)getJSFLibrary()).getPluginID();
 			if (pluginID != null) {
 				bundle = Platform.getBundle(pluginID);
 			}
 		}
 		return bundle;
 	}
 
 	/**
 	 * Gets the IWorkspaceRoot instance. 
 	 * @return IWorkspaceRoot instance
 	 */
 	protected IWorkspaceRoot getWorkspaceRoot() {
 		IWorkspaceRoot workspaceRoot = null;
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		if (workspace != null) {
 			workspaceRoot = workspace.getRoot();
 		}
 		return workspaceRoot;
 	}
 
 	protected String appendSeparator(String path) {
 		String newPath = null;
 		if (!path.endsWith("\\") && !path.endsWith("/")) { //$NON-NLS-1$ //$NON-NLS-2$
 			newPath = path + System.getProperty("file.separator"); //$NON-NLS-1$
 		} else {
 			newPath = path;
 		}
 		return newPath;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
 		if (featureID >= 0) {
 			switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
 				case JSFLibraryRegistryPackage.ARCHIVE_FILE__JSF_LIBRARY:
 					if (eContainer != null)
 						msgs = eBasicRemoveFromContainer(msgs);
 					return eBasicSetContainer(otherEnd, JSFLibraryRegistryPackage.ARCHIVE_FILE__JSF_LIBRARY, msgs);
 				default:
 					return eDynamicInverseAdd(otherEnd, featureID, baseClass, msgs);
 			}
 		}
 		if (eContainer != null)
 			msgs = eBasicRemoveFromContainer(msgs);
 		return eBasicSetContainer(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
 		if (featureID >= 0) {
 			switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
 				case JSFLibraryRegistryPackage.ARCHIVE_FILE__JSF_LIBRARY:
 					return eBasicSetContainer(null, JSFLibraryRegistryPackage.ARCHIVE_FILE__JSF_LIBRARY, msgs);
 				default:
 					return eDynamicInverseRemove(otherEnd, featureID, baseClass, msgs);
 			}
 		}
 		return eBasicSetContainer(null, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eBasicRemoveFromContainer(NotificationChain msgs) {
 		if (eContainerFeatureID >= 0) {
 			switch (eContainerFeatureID) {
 				case JSFLibraryRegistryPackage.ARCHIVE_FILE__JSF_LIBRARY:
 					return eContainer.eInverseRemove(this, JSFLibraryRegistryPackage.JSF_LIBRARY__ARCHIVE_FILES, JSFLibrary.class, msgs);
 				default:
 					return eDynamicBasicRemoveFromContainer(msgs);
 			}
 		}
 		return eContainer.eInverseRemove(this, EOPPOSITE_FEATURE_BASE - eContainerFeatureID, null, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Object eGet(EStructuralFeature eFeature, boolean resolve) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case JSFLibraryRegistryPackage.ARCHIVE_FILE__RELATIVE_TO_WORKSPACE:
 				return isRelativeToWorkspace() ? Boolean.TRUE : Boolean.FALSE;
 			case JSFLibraryRegistryPackage.ARCHIVE_FILE__SOURCE_LOCATION:
 				return getSourceLocation();
 			case JSFLibraryRegistryPackage.ARCHIVE_FILE__RELATIVE_DEST_LOCATION:
 				return getRelativeDestLocation();
 			case JSFLibraryRegistryPackage.ARCHIVE_FILE__JSF_LIBRARY:
 				return getJSFLibrary();
 		}
 		return eDynamicGet(eFeature, resolve);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void eSet(EStructuralFeature eFeature, Object newValue) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case JSFLibraryRegistryPackage.ARCHIVE_FILE__RELATIVE_TO_WORKSPACE:
 				setRelativeToWorkspace(((Boolean)newValue).booleanValue());
 				return;
 			case JSFLibraryRegistryPackage.ARCHIVE_FILE__SOURCE_LOCATION:
 				setSourceLocation((String)newValue);
 				return;
 			case JSFLibraryRegistryPackage.ARCHIVE_FILE__RELATIVE_DEST_LOCATION:
 				setRelativeDestLocation((String)newValue);
 				return;
 			case JSFLibraryRegistryPackage.ARCHIVE_FILE__JSF_LIBRARY:
 				setJSFLibrary((JSFLibrary)newValue);
 				return;
 		}
 		eDynamicSet(eFeature, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void eUnset(EStructuralFeature eFeature) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case JSFLibraryRegistryPackage.ARCHIVE_FILE__RELATIVE_TO_WORKSPACE:
 				setRelativeToWorkspace(RELATIVE_TO_WORKSPACE_EDEFAULT);
 				return;
 			case JSFLibraryRegistryPackage.ARCHIVE_FILE__SOURCE_LOCATION:
 				setSourceLocation(SOURCE_LOCATION_EDEFAULT);
 				return;
 			case JSFLibraryRegistryPackage.ARCHIVE_FILE__RELATIVE_DEST_LOCATION:
 				setRelativeDestLocation(RELATIVE_DEST_LOCATION_EDEFAULT);
 				return;
 			case JSFLibraryRegistryPackage.ARCHIVE_FILE__JSF_LIBRARY:
 				setJSFLibrary((JSFLibrary)null);
 				return;
 		}
 		eDynamicUnset(eFeature);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean eIsSet(EStructuralFeature eFeature) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case JSFLibraryRegistryPackage.ARCHIVE_FILE__RELATIVE_TO_WORKSPACE:
 				return relativeToWorkspace != RELATIVE_TO_WORKSPACE_EDEFAULT;
 			case JSFLibraryRegistryPackage.ARCHIVE_FILE__SOURCE_LOCATION:
 				return SOURCE_LOCATION_EDEFAULT == null ? sourceLocation != null : !SOURCE_LOCATION_EDEFAULT.equals(sourceLocation);
 			case JSFLibraryRegistryPackage.ARCHIVE_FILE__RELATIVE_DEST_LOCATION:
 				return RELATIVE_DEST_LOCATION_EDEFAULT == null ? relativeDestLocation != null : !RELATIVE_DEST_LOCATION_EDEFAULT.equals(relativeDestLocation);
 			case JSFLibraryRegistryPackage.ARCHIVE_FILE__JSF_LIBRARY:
 				return getJSFLibrary() != null;
 		}
 		return eDynamicIsSet(eFeature);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (RelativeToWorkspace: ");
 		result.append(relativeToWorkspace);
 		result.append(", SourceLocation: ");
 		result.append(sourceLocation);
 		result.append(", RelativeDestLocation: ");
 		result.append(relativeDestLocation);
 		result.append(')');
 		return result.toString();
 	}
 
 } //ArchiveFileImpl
