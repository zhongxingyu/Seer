 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.project.version;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import org.eclipse.osgi.util.NLS;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import org.jboss.tools.common.model.ServiceDialog;
 import org.jboss.tools.common.model.options.PreferenceModelUtilities;
 import org.jboss.tools.common.model.util.XMLUtil;
 import org.jboss.tools.common.util.FileUtil;
 import org.jboss.tools.common.xml.XMLUtilities;
 import org.jboss.tools.jst.web.messages.xpl.WebUIMessages;
 import org.jboss.tools.jst.web.project.handlers.AddLibraryToVersionSupport;
 import org.jboss.tools.jst.web.project.helpers.LibrarySet;
 import org.jboss.tools.jst.web.project.helpers.LibrarySets;
 
 public class ProjectVersion {
 	ProjectVersions versions;
 	Element element;
 	String name;
 	LibraryReference[] libraries = new LibraryReference[0];
 	String projectTemplatesLocation;
 	String[] order = new String[0];
 	
 	String preferredServletVersion = null;
	String minimalServletVersion = null;
 	
 	ProjectVersion(ProjectVersions versions) {
 		this.versions = versions;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public String[] getLibraryNames() {
 		String[] r = new String[libraries.length];
 		for (int i = 0; i < libraries.length; i++) r[i] = libraries[i].name;
 		return r;
 	}
 	
 	public String[] getLibraryLocations() {
 		String[] r = new String[libraries.length];
 		for (int i = 0; i < libraries.length; i++) r[i] = libraries[i].location;
 		return r;
 	}
 	
 	public String getProjectTemplatesLocation() {
 		return projectTemplatesLocation;
 	}
 	
 	public String getPreferredServletVersion() {
 		return preferredServletVersion;
 	}
 	
	public String getMinimalServletVersion() {
		return minimalServletVersion;
	}
	
 	public String getErrorMessage() {
 		LibraryReference core = null;
 		LibraryReference common = null;
 		for (int i = 0; i < libraries.length; i++) {
 			if("core".equals(libraries[i].type)) core = libraries[i]; //$NON-NLS-1$
 			if("common".equals(libraries[i].type)) common = libraries[i]; //$NON-NLS-1$
 		}
 
 		if(core == null || core.location.length() == 0) {
 			return NLS.bind(WebUIMessages.CORE_LIBRARY_LOCATION_ISNOT_SET_FOR_VERSION,name); 
 		} else if(!new File(core.location).isDirectory()) {
 			return NLS.bind(WebUIMessages.CORE_LIBRARY_LOCATION_FOR_VERSION_ISNOT_CORRECT,name);
 		}
 		if(common != null && !new File(common.location).isDirectory()) {
 			return NLS.bind(WebUIMessages.COMMON_LIBRARY_LOCATION_FOR_VERSION_ISNOT_CORRECT,name);
 		}
 		if(projectTemplatesLocation == null || projectTemplatesLocation.length() == 0) {
 			return NLS.bind(WebUIMessages.TEMPLATES_LOCATION_ISNOT_SET_FOR_VERSION, name);
 		} else if(!new File(projectTemplatesLocation).isDirectory()) {
 			return NLS.bind(WebUIMessages.TEMPLATES_LOCATION_FOR_VERSION_ISNOT_CORRECT,name);
 		}
 		return null;
 	}
 	
 	class LibraryReference {
 		String name;
 		String location;
 		String type;
 		Element e;
 	}
 	
 	void load(Element e) {
 		element = e;
 		name = e.getAttribute("displayName"); //$NON-NLS-1$
 		String sv = e.getAttribute("servlet-version"); //$NON-NLS-1$
 		if(sv != null && sv.trim().length() > 0) {
 			preferredServletVersion = sv.trim();
 		}
		sv = e.getAttribute("min-servlet-version"); //$NON-NLS-1$
		if(sv != null && sv.trim().length() > 0) {
			minimalServletVersion = sv.trim();
		}
 		ArrayList<LibraryReference> ls = new ArrayList<LibraryReference>();
 		NodeList nl = e.getChildNodes();
 		for (int i = 0; i < nl.getLength(); i++) {
 			Node n = nl.item(i);
 			if(n.getNodeType() != Node.ELEMENT_NODE) continue;
 			Element ne = (Element)n;
 			String location = ne.getAttribute("location"); //$NON-NLS-1$
 			if(location == null || location.length() == 0) continue;
 			location = versions.getAbsoluteLocation(location);
 			if("lib".equals(ne.getNodeName())) { //$NON-NLS-1$
 				int c = location.lastIndexOf('/');
 				String name = location.substring(c + 1);
 				LibraryReference l = getLibrary(name);
 				if(l == null) {
 					l = new LibraryReference();
 					l.name = name;
 				}
 				l.type = ne.getAttribute("type"); //$NON-NLS-1$
 				l.location = location;
 				l.e = ne;
 				ls.add(l);				
 			} else if("projectTempl".equals(ne.getNodeName())) { //$NON-NLS-1$
 				projectTemplatesLocation = location;
 				Element[] es = XMLUtilities.getChildren(ne, "project"); //$NON-NLS-1$
 				order = new String[es.length];
 				for (int j = 0; j < order.length; j++) order[j] = es[j].getAttribute("name"); //$NON-NLS-1$
 			}
 		}
 		libraries = ls.toArray(new LibraryReference[0]);
 	}
 	
 	//modification
 	
 	public String addLibrary() {
 		return AddLibraryToVersionSupport.run(this);
 	}
 	
 	public void addLibrary(String name) {
 		if(element == null) return;
 		addLibrary(name, "user"); //$NON-NLS-1$
 		versions.save();
 		load(element);
 	}
 	
 	void addLibrary(String name, String type) {
 		LibrarySet set = LibrarySets.getInstance().getLibrarySet(name);
 		if(set == null) return;
 		String location = LibrarySets.getInstance().getLibrarySetsPath() + "/" + name; //$NON-NLS-1$
 		Element lib = XMLUtil.createElement(element, "lib"); //$NON-NLS-1$
 		lib.setAttribute("location", getRelativePath(versions.path, location)); //$NON-NLS-1$
 		lib.setAttribute("type", type); //$NON-NLS-1$
 	}
 	
 	static String getRelativePath(String root, String location) {
 		String loc = FileUtil.getRelativePath(root, location);
 		if(loc == null) return location;
 		if(loc.startsWith("/..")) return loc.substring(1); //$NON-NLS-1$
 		if(loc.startsWith("/")) return "." + loc; //$NON-NLS-1$ //$NON-NLS-2$
 		return loc;
 	}
 	
 	public void removeLibrary(String name) {
 		LibraryReference l = getLibrary(name);
 		if(l == null || l.e == null) return;
 		if(!confirm(NLS.bind(WebUIMessages.YOU_WANT_TO_DELETE_LIBRARY, name))) return;
 		l.e.getParentNode().removeChild(l.e);
 		versions.save();
 		load(element);
 	}
 	
 	LibraryReference getLibrary(String name) {
 		for (int i = 0; i < libraries.length; i++) {
 			if(libraries[i].name.equals(name)) return libraries[i];
 		}
 		return null;
 	}
 
 	static boolean confirm(String message) {
 		ServiceDialog d = PreferenceModelUtilities.getPreferenceModel().getService();
 		return d.showDialog(WebUIMessages.CONFIRMATION, message, new String[]{WebUIMessages.OK, WebUIMessages.CANCEL}, null, ServiceDialog.QUESTION) == 0;
 	}
 	
 	public void setOrder(String[] order) {
 		this.order = order;
 		if(element == null) return;
 		Element e = XMLUtilities.getUniqueChild(element, "projectTempl"); //$NON-NLS-1$
 		if(e == null) return;
 		Element[] es = XMLUtilities.getChildren(e, "project"); //$NON-NLS-1$
 		for (int i = 0; i < es.length; i++) e.removeChild(es[i]);
 		for (int i = 0; i < order.length; i++) {
 			Element c = XMLUtilities.createElement(e, "project"); //$NON-NLS-1$
 			c.setAttribute("name", order[i]); //$NON-NLS-1$
 		}
 		versions.save();
 	}
 	
 	public String[] getOrder() {
 		return order;
 	}
 
 }
