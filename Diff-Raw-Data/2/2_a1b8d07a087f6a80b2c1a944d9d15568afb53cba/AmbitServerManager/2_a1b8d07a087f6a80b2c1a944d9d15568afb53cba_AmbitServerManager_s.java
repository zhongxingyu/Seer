 /*******************************************************************************
  * Copyright (c) 2012  Egon Willighagen <egon.willighagen@gmail.com>
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contact: http://www.bioclipse.net/
  ******************************************************************************/
 package net.bioclipse.ambit.server.business;
 
 import net.bioclipse.core.business.BioclipseException;
 import net.bioclipse.managers.business.IBioclipseManager;
 
 import org.apache.log4j.Logger;
 import org.restlet.Component;
 
 import ambit2.rest.AmbitApplication;
 
 public class AmbitServerManager implements IBioclipseManager {
 
     private static final Logger logger = Logger.getLogger(AmbitServerManager.class);
 
     private Component component = null;
 
     /**
      * Gives a short one word name of the manager used as variable name when
      * scripting.
      */
     public String getManagerName() {
         return "ambitserver";
     }
 
     public void boot(String username, String password, int port) throws BioclipseException {
     	if (component != null)
     		throw new BioclipseException("The local AMBIT2 server is already booted.");
 
     	logger.debug("Booting an AMBIT server...");
         // Create a component
     	try {
 			component = AmbitApplication.boot(username, password, port);
 		} catch (Exception e) {
 			throw new BioclipseException("Error while booting Ambit: " + e.getMessage(), e);
 		}
     }
 
     public void shutdown() throws BioclipseException {
    	if (component != null)
     		throw new BioclipseException("There is no local AMBIT2 server running.");
 
     	try {
 			AmbitApplication.shutdown(component);
 		} catch (Exception e) {
 			throw new BioclipseException("Error while shutting down Ambit: " + e.getMessage(), e);
 		}
     }
 }
