 /*******************************************************************************
  * Copyright (c) May 16, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdklib;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.zend.sdklib.internal.library.AbstractLibrary;
 import org.zend.sdklib.internal.project.template.TemplateWriter;
 
 /**
  * Sample library class
  * 
  * @author Roy, 2011
  * 
  */
 public class ZendProject extends AbstractLibrary {
 
 	protected String name;
 	protected boolean withScripts;
 	protected String destination;
 
 	public ZendProject(String name, boolean withScripts, String destination) {
 		this.name = name;
 		this.withScripts = withScripts;
 		this.destination = destination;
 	}
 
 	/**
 	 * Writes project to file system.
 	 * 
 	 * @return true on success, false otherwise.
 	 */
 	public boolean create() {
 		TemplateWriter tw = new TemplateWriter();
 		
 		File dest = destination == null ? new File(".") : new File(destination);
		if (! dest.exists()) {
			dest.mkdir();
		}
 		
 		try {
 			tw.writeTemplate(name, withScripts, dest);
 		} catch (IOException e) {
 			log.error(e.getMessage());
 			return false;
 		}
 		
 		return true;
 	}
 
 }
