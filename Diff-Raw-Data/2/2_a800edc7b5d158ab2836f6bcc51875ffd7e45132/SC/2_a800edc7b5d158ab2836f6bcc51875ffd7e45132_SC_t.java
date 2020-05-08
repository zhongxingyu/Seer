 /*
  * SmartGWT (GWT for SmartClient)
  * Copyright 2008 and beyond, Isomorphic Software, Inc.
  *
  * SmartGWT is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License version 3
  * as published by the Free Software Foundation.  SmartGWT is also
  * available under typical commercial license terms - see
  * http://smartclient.com/license
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  */
 package org.vaadin.smartgwt.server.util;
 
 import java.util.Map;
 import java.util.Stack;
 
 import org.vaadin.rpc.server.ServerSideHandler;
 import org.vaadin.rpc.server.ServerSideProxy;
 import org.vaadin.smartgwt.server.Canvas;
 
 import com.vaadin.terminal.PaintException;
 import com.vaadin.terminal.PaintTarget;
 
 @com.vaadin.ui.ClientWidget(org.vaadin.smartgwt.client.util.VSC.class)
 public class SC extends Canvas {
 	private ServerSideProxy client = new ServerSideProxy(new ServerSideHandlerImpl());
 	private Stack callBacks = new Stack();
 
 	public void say(String message) {
 		client.call("sayNoCallback", message);
 	}
 
 	public void say(String message, BooleanCallback bcb) {
 		client.call("sayWithCallback", message);
 		callBacks.push(bcb);
 	}
 
 	public void say(String title, String message) {
 		client.call("sayNoCallback", title, message);
 	}
 
 	public void say(String title, String message, BooleanCallback bcb) {
 		client.call("sayWithCallback", title, message);
 		callBacks.push(bcb);
 	}
 
 	@Override
 	public void changeVariables(final Object source, final Map variables) {
 		client.changeVariables(source, variables);
 
 		if (callBacks.size() > 0) {
 			BooleanCallback bcp = (BooleanCallback) callBacks.pop();
 			bcp.execute((Boolean) variables.get("callback"));
 		}
 	}
 
 	@Override
 	public void paintContent(PaintTarget target) throws PaintException {
 		super.paintContent(target);
 		client.paintContent(target);
 	}
 
 	private class ServerSideHandlerImpl implements ServerSideHandler {
 		@Override
 		public Object[] initRequestFromClient() {
			return new Object[0];
 		}
 
 		@Override
 		public void callFromClient(String method, Object[] params) {
 
 		}
 
 		@Override
 		public void requestRepaint() {
 			SC.this.requestRepaint();
 		}
 	}
 }
