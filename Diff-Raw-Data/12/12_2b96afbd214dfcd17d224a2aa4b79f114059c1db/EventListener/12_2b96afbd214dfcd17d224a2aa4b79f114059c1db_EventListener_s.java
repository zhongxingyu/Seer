 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.ui.views.scriptpad.events;
 
 import java.util.EventObject;
 
 import org.eclipse.tcf.te.tcf.core.scripting.events.ScriptEvent;
 import org.eclipse.tcf.te.tcf.ui.views.scriptpad.console.Console;
 import org.eclipse.tcf.te.tcf.ui.views.scriptpad.console.Factory;
 import org.eclipse.tcf.te.ui.events.AbstractEventListener;
 
 /**
  * Script Pad console event listener
  */
 public class EventListener extends AbstractEventListener {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.interfaces.events.IEventListener#eventFired(java.util.EventObject)
 	 */
 	@Override
 	public void eventFired(EventObject event) {
 		if (event instanceof ScriptEvent) {
 			ScriptEvent scriptEvent = (ScriptEvent)event;
 
 			// Get the event type
 			ScriptEvent.Type type = scriptEvent.getType();
 
 			switch (type) {
 				case START:
 					Factory.showConsole();
 					break;
 				case OUTPUT:
 					Console console = Factory.getConsole();
 					if (console != null) {
 						ScriptEvent.Message message = scriptEvent.getMessage();
 						if (message != null) console.appendMessage(message.type, message.text);
 					}
 					break;
 				case STOP:
 					break;
 			}
 		}
 	}
 
 }
