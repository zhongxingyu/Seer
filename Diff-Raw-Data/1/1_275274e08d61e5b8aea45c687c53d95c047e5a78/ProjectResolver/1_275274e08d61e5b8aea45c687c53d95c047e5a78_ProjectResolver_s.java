 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jst.jsf.common.ui.IFileFolderConstants;
 import org.eclipse.jst.jsf.common.ui.internal.logging.Logger;
 import org.eclipse.jst.jsf.common.ui.internal.utils.ResourceUtils;
 import org.eclipse.jst.jsf.common.ui.internal.utils.WebrootUtil;
 import org.eclipse.jst.pagedesigner.PDPlugin;
 import org.eclipse.jst.pagedesigner.css2.property.ICSSPropertyID;
 import org.eclipse.wst.common.uriresolver.internal.util.URIHelper;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
 import org.eclipse.wst.sse.core.internal.util.URIResolver;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * A URIResolver implementation
  *
  */
 public class ProjectResolver implements URIResolver {
 	private static final String TLD_TAG_URI = "uri";
 
 	private static final String URI_PREFIX_HTTP = "http";
 
 	private static final String FILE_PROTOCOL = "file";
 
 	/** Create the logger for this class */
 	private static Logger _log = PDPlugin.getLogger(ProjectResolver.class);
 
 	private IProject _project = null;
 
 	private String _fileBaseLocation = null;
 
 	private static Map _uriMap = null;
 
 	/**
 	 * It is strongly recommended that clients use
 	 * project.getAdapter(URIResolver.class) to obtain a URIResolver aware of
 	 * the Project's special requirements. Note that a URIResolver may not be
 	 * returned at all so manually creating this object may still be required.
 	 * @param project
 	 */
 	public ProjectResolver(IProject project) {
 		super();
 		_project = project;
 	}
 
 	/**
 	 * @param path
 	 */
 	public void seekTld(IFolder path) {
 		if (path == null) {
 			return;
 		}
 		if (_uriMap == null) {
 			_uriMap = new HashMap();
 		}
 
 		try {
 			IResource[] res = path.members();
 			if (null == res) {
 				return;
 			}
 			for (int i = 0; i < res.length; i++) {
 				if (res[i] instanceof IFolder) {
 					seekTld((IFolder) res[i]);
 				}
 				String ext = res[i].getFileExtension();
 				if (IFileFolderConstants.EXT_TAGLIB.equalsIgnoreCase(ext)) {
 					IFile tldFile = (IFile) res[i];
 					String uri = getURIfromTLD(tldFile);
 					String locate = tldFile.getLocation().toOSString();
 					if (uri != null && _uriMap.get(uri) == null) {
 						_uriMap.put(uri, locate);
 					}
 				}
 			}
 		} catch (CoreException e) {
 			_log.error("Error.ProjectResolver.GetlocationByURI.0", e);
 		}
 	}
 
 	/**
 	 * @param path
 	 */
 	public void seekTld(File path) {
 		if (path == null || !path.isDirectory()) {
 			return;
 		}
 		if (_uriMap == null) {
 			_uriMap = new HashMap();
 		}
 
 		try {
 			File[] res = path.listFiles();
 			if (null == res) {
 				return;
 			}
 			for (int i = 0; i < res.length; i++) {
 				if (res[i] instanceof IFolder) {
 					seekTld(res[i]);
 				}
 
 				if (res[i].getName().endsWith(
 						IFileFolderConstants.DOT
 								+ IFileFolderConstants.EXT_TAGLIB)) {
 					String uri = getURIfromTLD(res[i]);
 					String locate;
 
 					locate = res[i].getCanonicalPath();
 
 					if (uri != null && _uriMap.get(uri) == null) {
 						_uriMap.put(uri, locate);
 					}
 				}
 			}
 		} catch (IOException e1) {
 			_log.error("Error.ProjectResolver.GetlocationByURI.0", e1);
 		}
 	}
 
 	/**
 	 * @param tldFile
 	 * @return the uri for the tld in tldFile or null
 	 */
 	public String getURIfromTLD(File tldFile) {
 
 		if (tldFile == null) {
 			return null;
 		}
 		IDOMModel tldModel = null;
 
 		InputStream in = null;
 		try {
 			in = new FileInputStream(tldFile);
 		} catch (FileNotFoundException e) {
 			_log.error("RenderingTraverser.Error.FileNotFound", e);
 		}
 //		IDOMModel xmlModel = null;
 
 		try {
 			tldModel = (IDOMModel) StructuredModelManager.getModelManager().getModelForRead(
 					tldFile.getAbsolutePath(), in, null);
 			NodeList uriList = tldModel.getDocument().getElementsByTagName(
 					TLD_TAG_URI);
 			for (int i = 0, n = uriList.getLength(); i < n; i++) {
 				Node uri = uriList.item(i);
 				return uri.getChildNodes().item(0).getNodeValue();
 			}
 		} catch (UnsupportedEncodingException e1) {
 			_log.error("RenderingTraverser.Error.UnsupportedEncoding", e1);
 		} catch (IOException e1) {
 			_log.error("RenderingTraverser.Error.IO", e1);
 		} finally {
 			ResourceUtils.ensureClosed(in);
 			
 			if (tldModel != null)
 			{
 			    tldModel.releaseFromRead();
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * @param tldFile
 	 * @return the URI for the TLD in tldFile or null
 	 */
 	public String getURIfromTLD(IFile tldFile) {
 		if (tldFile == null) {
 			return null;
 		}
 		IDOMModel tldModel;
 
 		try {
 			tldModel = (IDOMModel) getModelManager().getModelForRead(tldFile);
 			NodeList uriList = tldModel.getDocument().getElementsByTagName(
 					TLD_TAG_URI);
 			for (int i = 0, n = uriList.getLength(); i < n; i++) {
 				Node uri = uriList.item(i);
 				return uri.getChildNodes().item(0).getNodeValue();
 			}
 		} catch (IOException e) {
 			// Error in taglib locating.
 			_log.error("Error.ProjectResolver.GetlocationByURI.0", e); //$NON-NLS-1$
 		} catch (CoreException e1) {
 			_log.error("Error.ProjectResolver.GetlocationByURI.0", e1);
 		}
 		return null;
 	}
 
 	/**
 	 * initialize the map of tlds
 	 */
 	public void initTldMap() {
 		if (_uriMap == null) {
 			_uriMap = new HashMap();
 		}
 		if (_project == null) {
 			return;
 		}
 		if (WebrootUtil.getWebContentFolder(_project) == null) {
 			return;
 		}
 		IFolder webinf = WebrootUtil.getWebContentFolder(_project).getFolder(
 				IFileFolderConstants.FOLDER_WEBINF);
 		if (webinf != null && webinf.exists()) {
 			seekTld(webinf);
 		}
 
 		String locate = PDPlugin.getInstallLocation().append("/jsf-tld")
 				.toString();
 		File jsfDir = new File(locate);
 		seekTld(jsfDir);
 
 	}
 
 	public java.lang.String getFileBaseLocation() {
 		return _fileBaseLocation;
 	}
 
 	public java.lang.String getLocationByURI(String uri) {
 		// System.out.println(getLocationByURI(uri, getFileBaseLocation()));
 		return getLocationByURI(uri, getFileBaseLocation());
 	}
 
 	private IModelManager getModelManager() {
 		return StructuredModelManager.getModelManager();
 	}
 
 	private String getLocationFromWEBXML(String uri, String baseReference) {
 		if (uri == null) {
 			return null;
 		}
 		try {
 			// if (_project.hasNature(ICommonConstants.NATURE_WEBAPP))
 			// {
 			if (uri.startsWith(IFileFolderConstants.PATH_SEPARATOR)) {
 				uri = _project.getProject().getLocation().toString()
 						+ IFileFolderConstants.PATH_SEPARATOR
 						+ WebrootUtil.getWebContentFolderName(_project) + uri;
 			}
 			if (uri.startsWith(URI_PREFIX_HTTP)) {
 				IFile webxml = WebrootUtil.getWebContentFolder(_project)
 						.getFolder(IFileFolderConstants.FOLDER_WEBINF).getFile(
 								IFileFolderConstants.FILE_WEB_XML);
 				IDOMModel xmlModel;
 
 				if (webxml.exists()) {
 					try {
 						xmlModel = (IDOMModel) getModelManager()
 								.getModelForRead(webxml);
 
 						NodeList taglibNodeList = xmlModel
 								.getDocument()
 								.getElementsByTagName(ICSSPropertyID.TAG_TAGLIB);
 
 						for (int i = 0, size = taglibNodeList.getLength(); i < size; i++) {
 							Node taglibNode = taglibNodeList.item(i);
 
 							NodeList childList = taglibNode.getChildNodes();
 							String taguri = "";
 							String taglocation = "";
 							for (int j = 0, childSize = childList.getLength(); j < childSize; j++) {
 								Node childTaglibNode = childList.item(j);
 								if (ICSSPropertyID.ATTR_TAGLIB_URI
 										.equalsIgnoreCase(childTaglibNode
 												.getNodeName())) {
 									taguri = childTaglibNode.getChildNodes()
 											.item(0).getNodeValue();
 								}
 								if (ICSSPropertyID.ATTR_TAGLIB_LOCATION
 										.equalsIgnoreCase(childTaglibNode
 												.getNodeName())) {
 									taglocation = childTaglibNode
 											.getChildNodes().item(0)
 											.getNodeValue();
 								}
 
 							}
 							if (uri.equalsIgnoreCase(taguri))
 								uri = _project.getProject().getLocation()
 										.toString()
 										+ IFileFolderConstants.PATH_SEPARATOR
 										+ WebrootUtil
 												.getWebContentFolderName(_project)
 										+ taglocation;
 						}
 						xmlModel.releaseFromRead();
 					} catch (IOException e) {
 
 						// Error in taglib locating.
 						_log.error(
 								"Error.ProjectResolver.GetlocationByURI.0", e); //$NON-NLS-1$
 					} catch (CoreException e1) {
 						e1.printStackTrace();
 						_log.error("Error.ProjectResolver.GetlocationByURI.0",
 								e1);
 					}
 
 				}
 			}
 			// }
 		} catch (DOMException e1) {
 			// Error in taglib locating.
 			_log.error("Error.ProjectResolver.GetlocationByURI.0", e1); //$NON-NLS-1$
 		}
 		// catch (CoreException e1)
 		// {
 		//
 		// _log.error("Error.ProjectResolver.GetlocationByURI.0", e1);
 		// }
 
 		if (isFileURL(uri)) {
 			try {
 				URL url = new URL(uri);
 				return getPath(url);
 			} catch (MalformedURLException e) {
 				_log.error("Error.ProjectResolver.GetlocationByURI.0", e);
 			}
 		}
 		// defect 244817 end
 		return URIHelper.normalize(uri, baseReference, getRootLocationString());
 
 	}
 
 	public String getLocationByURI(String uri, String baseReference) {
 		// DataWindow may generate URL like "d:\somefile" (dos path). We may
 		// need some
 		// special support. (lium)
 		int columnIndex = uri.indexOf(":");
 		int slashIndex = uri.indexOf("/");
 		if (columnIndex != -1 && (slashIndex == -1 || columnIndex < slashIndex)) {
 			return uri;
 		}
 
 		String result = getLocationFromWEBXML(uri, baseReference);
 		if (result != null && !result.equals(uri)) {
 			return result;
 		}
 		if (_uriMap == null) {
 			initTldMap();
 		}
 		if (_uriMap != null) {
 			return (String) _uriMap.get(uri);
 		}
 		return null;
 	}
 
 	// defect 244817 start
 	/**
 	 * @param passedSpec
 	 * @return boolean
 	 */
 	private boolean isFileURL(String passedSpec) {
 		if (passedSpec == null) {
 			return false;
 		}
 		final String spec = passedSpec.trim();
 		if (spec.length() == 0) {
 			return false;
 		}
 		String newProtocol = null;
 		for (int index = 0, limit = spec.length(); index < limit; index++) {
 			final char p = spec.charAt(index);
 			if (p == '/') {
 				//$NON-NLS-1$
 				break;
 			}
 			if (p == ':') {
 				//$NON-NLS-1$
 				newProtocol = spec.substring(0, index);
 				break;
 			}
 		}
 		return (newProtocol != null && newProtocol
 				.compareToIgnoreCase(FILE_PROTOCOL) == 0); //$NON-NLS-1$
 	}
 
 	/**
 	 * @param url
 	 * @return String
 	 */
 	private String getPath(URL url) {
 		String ref = url.getRef() == null ? "" : "#" + url.getRef(); //$NON-NLS-1$ //$NON-NLS-2$
 		String strPath = url.getFile() + ref;
 		IPath path;
 		if (strPath.length() == 0) {
 			path = Path.ROOT;
 		} else {
 			path = new Path(strPath);
 			String query = null;
 			StringTokenizer parser = new StringTokenizer(strPath, "?"); //$NON-NLS-1$
 			int tokenCount = parser.countTokens();
 			if (tokenCount == 2) {
 				path = new Path((String) parser.nextElement());
 				query = (String) parser.nextElement();
 			}
 			if (query == null) {
 				parser = new StringTokenizer(path.toString(), "#"); //$NON-NLS-1$
 				tokenCount = parser.countTokens();
 				if (tokenCount == 2) {
 					path = new Path((String) parser.nextElement());
 				}
 			}
 		}
 		return getPath(path, url.getHost());
 	}
 
 	/**
 	 * @param path
 	 * @param host
 	 * @return String
 	 */
 	private String getPath(IPath path, String host) {
 		IPath newPath = path;
 		// They are potentially for only Windows operating system.
 		// a.) if path has a device, and if it begins with IPath.SEPARATOR,
 		// remove it
 		final String device = path.getDevice();
 		if (device != null && device.length() > 0) {
 			if (device.charAt(0) == IPath.SEPARATOR) {
 				final String newDevice = device.substring(1);
 				newPath = path.setDevice(newDevice);
 			}
 		}
 		// b.) if it has a hostname, it is UNC name... Any java or eclipse api
 		// helps it ??
 		if (newPath != null && host != null && host.length() != 0) {
 			IPath uncPath = new Path(host);
 			uncPath = uncPath.append(path);
 			newPath = uncPath.makeUNC(true);
 		}
 		
 		if (newPath != null)
 		{
 		    return newPath.toString();
 		}
 		return path.toString();
 	}
 
 	/**
 	 * Resolve the (possibly relative) URI acording to RFC1808 using the default
 	 * file base location. Resolves resource references into absolute resource
 	 * locations without ensuring that the resource actually exists. Note:
 	 * currently resolveCrossProjectLinks is ignored in this implementation.
 	 */
 	public java.lang.String getLocationByURI(String uri,
 			boolean resolveCrossProjectLinks) {
 		return getLocationByURI(uri, getFileBaseLocation(),
 				resolveCrossProjectLinks);
 	}
 
 	/**
 	 * Perform the getLocationByURI action using the baseReference as the point
 	 * of reference instead of the default for this resolver Note: currently
 	 * resolveCrossProjectLinks is ignored in this implementation.
 	 */
 	public java.lang.String getLocationByURI(String uri, String baseReference,
 			boolean resolveCrossProjectLinks) {
 		return getLocationByURI(uri, baseReference);
 	}
 
 	public org.eclipse.core.resources.IProject getProject() {
 		return _project;
 	}
 
 	public org.eclipse.core.resources.IContainer getRootLocation() {
 		return _project;
 	}
 
 	protected String getRootLocationString() {
 		return null;
 	}
 
 	public void setFileBaseLocation(java.lang.String newFileBaseLocation) {
 		_fileBaseLocation = newFileBaseLocation;
 	}
 
 	public void setProject(org.eclipse.core.resources.IProject newProject) {
 		_project = newProject;
 	}
 
 	public InputStream getURIStream(String uri) {
 		return null;
 	}
 }
