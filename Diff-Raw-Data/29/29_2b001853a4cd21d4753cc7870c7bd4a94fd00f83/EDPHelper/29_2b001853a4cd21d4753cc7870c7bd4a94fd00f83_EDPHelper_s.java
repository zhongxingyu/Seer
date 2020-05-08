 /*******************************************************************************
  * Copyright (c) 2008 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.engine.edp.nonosgi;
 
 import java.util.List;
 
 import org.eclipse.wazaabi.engine.edp.Identifiable;
 import org.eclipse.wazaabi.engine.edp.Registry;
 import org.eclipse.wazaabi.engine.edp.events.EDPEventAdapterFactory;
 import org.eclipse.wazaabi.engine.edp.events.EDPEventHandlerAdapterFactory;
 import org.eclipse.wazaabi.engine.edp.events.EventAdapterFactory;
 import org.eclipse.wazaabi.engine.edp.events.EventHandlerAdapterFactory;
 import org.eclipse.wazaabi.engine.edp.executables.EDPExecutableAdapterFactory;
 import org.eclipse.wazaabi.engine.edp.executables.ExecutableAdapterFactory;
 import org.eclipse.wazaabi.mm.edp.EdpPackage;
 
 public class EDPHelper {
 
 	public static synchronized void init(Registry registry) {
 		EdpPackage.eINSTANCE.eClass();
 
 		addService(registry, EventHandlerAdapterFactory.class,
 				new EDPEventHandlerAdapterFactory());
 		addService(registry, EventAdapterFactory.class,
 				new EDPEventAdapterFactory());
 		addService(registry, ExecutableAdapterFactory.class,
 				new EDPExecutableAdapterFactory());
 	}
 
 	public static void addService(Registry registry, Class<?> interfaze,
 			Identifiable f) {
 		if (registry == null || f == null || interfaze == null)
 			return;
 		List<Object> services = registry.getServices(interfaze);
 		services.add(f);
 		registry.setServices(interfaze, services, true);
 	}
 }
