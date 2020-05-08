 /*
  *    Project: The EDNA Kernel
  *             http://www.edna-site.org
  *
  *    File: "$Id:$"
  *
  *    Copyright (C) 2008-2009 European Synchrotron Radiation Facility
  *                            Grenoble, France
  *
  *    Principal authors: Marie-Francoise Incardona (incardon@esrf.fr)
  *                       Olof Svensson (svensson@esrf.fr)
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU Lesser General Public License as published
  *    by the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU Lesser General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    and the GNU Lesser General Public License  along with this program.
  *    If not, see <http://www.gnu.org/licenses/>.
  */
 package org.edna.datamodel.transformations.workflow;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.plugin.EcorePlugin;
 import org.eclipse.xtext.mwe.UriFilter;
 
 /**
  * This filter is used to filter out the target model file when reading
  * .edna_datamodel files. This is necessary since the Index should not
  * contain the target elements. Otherwise this will lead to serialization
  * problems since there are ambigious elements when resolving cross
  * references.
  *
  * @author Karsten Thoms
  *
  */
 public class TargetUriFilter implements UriFilter {
 	private URI uri;
 
 	public boolean matches(URI uri) {
 		final String uriAsString = uri.toString();
 
 		if (!uriAsString.contains("datamodel"))
 			return false;
 		if (uriAsString.contains("/.svn/"))
 			return false;
 		if (!uri.lastSegment().endsWith(".edna_datamodel"))
 			return false;
 
 		// When used within Eclipse determine if the project containing the
 		// resource is not open.
 		if (EcorePlugin.IS_ECLIPSE_RUNNING) {
 			if (uri.isPlatformResource()) {
 				if (!EcorePlugin.getWorkspaceRoot().getProject(uri.segment(1)).isOpen()) {
 					return false;
 				}
 			} else if (uri.isFile()) {
 				// For File URIs locate the respective project from the platform resource map
 				for (String projectName : EcorePlugin.getPlatformResourceMap().keySet()) {
 					URI projectUri = EcorePlugin.getPlatformResourceMap().get(projectName);
 					if (uriAsString.startsWith(projectUri.toString())) {
						if (!EcorePlugin.getWorkspaceRoot().getProject(projectName).isOpen()) {
							return false;
 						}
 					}
 				}

 			}
 		}
 		final String sourceFileName = this.uri.lastSegment().substring(0, this.uri.lastSegment().lastIndexOf('.'));
 		final String currentFileName = uri.lastSegment().substring(0, uri.lastSegment().lastIndexOf('.'));
 
 		final boolean result = !sourceFileName.equals(currentFileName) && "XSDataCommon".equals(currentFileName);
 		return result;
 	}
 
 	public void setUri(String uri) {
 		this.uri = URI.createURI(uri);
 	}
 
 
 }
