 /*******************************************************************************
  * Copyright (c) 2012 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.engine.edp;
 
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.wazaabi.engine.edp.adapters.EventHandlerAdapter;
 import org.eclipse.wazaabi.engine.edp.exceptions.OperationAborted;
 import org.eclipse.wazaabi.mm.edp.EventDispatcher;
 import org.eclipse.wazaabi.mm.edp.events.Event;
 import org.eclipse.wazaabi.mm.edp.handlers.EventHandler;
 
 public class EDPUtils {
 
 	/**
 	 * This method throws a given event to the dispatcher.
 	 * 
 	 * @param dispatcher
 	 * @param event
 	 */
 	public static void throwEvent(EventDispatcher dispatcher, Event event) {
 		if (dispatcher == null || event == null || "".equals(event.getId())) //$NON-NLS-1$
 			return;
 		for (EventHandler eventHandler : dispatcher.getHandlers())
 			for (Event listenedEvent : eventHandler.getEvents())
 				if (event.getId().equals(listenedEvent.getId()))
 					for (Adapter adapter : eventHandler.eAdapters())
 						if (adapter instanceof EventHandlerAdapter)
 							try {
 								((EventHandlerAdapter) adapter).trigger(event);
 							} catch (OperationAborted e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 								System.err.println(e.getErrorMessage());
 							}
 
 	}
 
	/**
	 * Returns the (smart) concatenation of the prefix and the relativePath.
	 * 
	 * @param prefix
	 * @param relativePath
	 * @return A normalized URI or null
	 */
 	public static String normalizeURI(String prefix, String relativePath) {
 		if (prefix == null)
 			return relativePath;
 		if (relativePath == null)
 			return prefix;

 		if (!relativePath.startsWith("//")) {
 			if (relativePath.startsWith("/"))
 				relativePath = relativePath.substring(1);
 			if (prefix.endsWith("/"))
 				return prefix + relativePath;
			else if (prefix.startsWith("urn:")) //$NON-NLS-1$
				return prefix + relativePath;
 			else
 				return prefix + '/' + relativePath;
 		}
 		return null;
 	}
 }
