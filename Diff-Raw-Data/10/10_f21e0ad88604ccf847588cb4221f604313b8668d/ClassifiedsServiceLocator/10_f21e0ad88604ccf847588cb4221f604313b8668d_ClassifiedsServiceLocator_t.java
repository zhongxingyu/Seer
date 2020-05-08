 /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  Copyright (C) 2008 Felipe Gaúcho
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
  
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  
  This file is part of the CEJUG-CLASSIFIEDS Project - an  open source classifieds system
  originally used by CEJUG - Ceará Java Users Group.
  The project is hosted https://cejug-classifieds.dev.java.net/
  
  You can contact us through the mail dev@cejug-classifieds.dev.java.net
  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
 package net.java.dev.cejug.classifieds.server;
 
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import net.java.dev.cejug.classifieds.server.config.ConfigWrapper;
 import net.java.dev.cejug.classifieds.server.generated.contract.ClassifiedsServiceInterface;
 import net.java.dev.cejug.classifieds.server.reference.ClassifiedsReferenceImplementation;
 
 /**
  * Cejug-Classifieds service locator.
  * 
  * @see <a
  *      href='http://java.sun.com/blueprints/corej2eepatterns/Patterns/BusinessDelegate.html'>Core
  *      J2EE Patterns - Service Locator</a>
  * @author $Author: felipegaucho $
  * @version $Rev: 355 $ ($Date: 2007-12-12 21:30:02 +0100 (Wed, 12 Dec 2007) $)
  */
 public abstract class ClassifiedsServiceLocator {
 	/**
 	 * the global log manager, used to allow third party services to override
 	 * the defult logger.
 	 */
 	private static LogManager logManager = null;
 
 	/**
 	 * If the property SERVICE_IMPLEMENTATION is set in the system
 	 * configuration, tries to instantiate it by reflection. Otherwise, returns
 	 * an instance of the cejug-classifieds-server reference implementation.
 	 * 
 	 * @return an instance of the service implementation.
 	 * @throws NoClassDefFoundError
 	 */
 	static ClassifiedsServiceInterface getServiceImplementation()
 			throws NoClassDefFoundError {
 		return new ClassifiedsReferenceImplementation();
 
 		// load properties
 		// instantiate implementation
 		// return
 	}
 
 	static Logger getLogger(String name) {
		/*if (logManager == null) {
 			logManager = LogManager.getLogManager();
		}*/
		return Logger.getLogger(name);
 
		//return logManager.getLogger(name);
 	}
 }
