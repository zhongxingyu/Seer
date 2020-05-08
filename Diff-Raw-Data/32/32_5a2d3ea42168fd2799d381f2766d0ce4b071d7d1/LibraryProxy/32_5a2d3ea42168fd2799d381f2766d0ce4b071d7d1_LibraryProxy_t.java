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
 package org.jboss.tools.jst.web.kb.internal.scanner;
 
 import java.util.Properties;
 
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.jst.web.kb.internal.KbObject;
 import org.jboss.tools.jst.web.kb.internal.taglib.AbstractTagLib;
 import org.w3c.dom.Element;
 
 /**
  * Stores/loads first-level link between KB library object and underlying XModel object.
  * 
  * @author Viacheslav Kabanovich
  *
  */
 public class LibraryProxy extends KbObject {
 	AbstractTagLib library;
 	
 	public LibraryProxy(AbstractTagLib library) {
 		this.library = library;
 		setSourcePath(library.getSourcePath());
 		setId(library.getId());
 	}
 
 	public AbstractTagLib getLibrary() {
 		return library;
 	}
 
 	public String getXMLName() {
 		return library.getXMLName();
 	}
 	
 	public String getXMLClass() {
 		return library.getXMLClass();
 	}
 
 	public void loadXML(Element element, Properties context) {
 		super.loadXML(element, context);
		if(getId() instanceof XModelObject) {
			XModelObject o = (XModelObject)getId();
			LoadedDeclarations ds = new XMLScanner().parse(o, getSourcePath(), library.getKbProject());
			if(ds != null && !ds.getLibraries().isEmpty()) {
				library = (AbstractTagLib)ds.getLibraries().get(0);
			} else {
				library = null;
			}
 		} else {
			library.loadXML(element, context);
 		}
 	}
 }
