 /*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.server.core.internal;
 
 import java.io.StringReader;
 import java.io.StringWriter;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.core.runtime.preferences.PreferenceModifyListener;
 import org.osgi.service.prefs.Preferences;
 import org.w3c.dom.*;
 import org.xml.sax.InputSource;
 
 public class ServerPreferenceModifyListener extends PreferenceModifyListener {
 
 	private static final String LOCKED_ATTRIBUTE_NAME = "locked"; //$NON-NLS-1$
 	private static final String RUNTIMES_PREFERENCE_NAME = "runtimes"; //$NON-NLS-1$
 	private static final String RUNTIME_NODE_NAME = "runtime"; //$NON-NLS-1$
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.runtime.preferences.PreferenceModifyListener#preApply(org.eclipse.core.runtime.preferences.IEclipsePreferences)
 	 */
 	public IEclipsePreferences preApply(IEclipsePreferences node) {
 		Preferences instance = node.node(InstanceScope.SCOPE);
 		Preferences runtimes = instance.node(ServerPlugin.PLUGIN_ID);
 		if (runtimes != null)
 			removeLockedServerRuntimePreference(runtimes); //$NON-NLS-1$
 		return super.preApply(node);
 	}
 
 	private void removeLockedServerRuntimePreference(Preferences preferences) {
 		try {
 			Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(new InputSource(new StringReader(preferences.get(RUNTIMES_PREFERENCE_NAME, "")))); //$NON-NLS-1$
 			NodeList nodeList = doc.getElementsByTagName(RUNTIME_NODE_NAME);
 			for (int s = 0; s < nodeList.getLength(); s++) {
 				Node node = nodeList.item(s);
 				if (node.getNodeType() == Node.ELEMENT_NODE) {
 					NamedNodeMap attributes = node.getAttributes();
 					Node locked = attributes.getNamedItem(LOCKED_ATTRIBUTE_NAME);
 					if (locked != null && Boolean.parseBoolean(locked.getNodeValue()))
 						node.getParentNode().removeChild(node);
 				}
 			}
 			StringWriter runtimes = new StringWriter();
             TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(runtimes));        
             preferences.remove(RUNTIMES_PREFERENCE_NAME);
             preferences.put(RUNTIMES_PREFERENCE_NAME, runtimes.toString());
 		  } catch (Exception e) {
 		    //there is no defined runtime environment 
 			  return;
 		  }
 		  
 	}
 
 }
