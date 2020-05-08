 /*
  * Copyright 2011 John Ahlroos
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fi.jasoft.qrcode.client.ui;
 
 import com.vaadin.terminal.gwt.client.ApplicationConnection;
 import com.vaadin.terminal.gwt.client.Paintable;
 import com.vaadin.terminal.gwt.client.UIDL;
 import com.vaadin.terminal.gwt.client.VConsole;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.ImageElement;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Client side widget which communicates with the server. Messages from the
  * server are shown as HTML and mouse clicks are sent to the server.
  */
 public class VQRCode extends SimplePanel implements Paintable {
 
 	/** Set the CSS class name to allow styling. */
 	public static final String CLASSNAME = "v-qrcode";
 
 	/** The client side widget identifier */
 	protected String paintableId;
 
 	/** Reference to the server connection object. */
 	protected ApplicationConnection client;
 	
 	/** The qr encoded image */
 	private Image qrcode;
 	
 	/**
 	 * The constructor should first call super() to initialize the component and
 	 * then handle any initialization relevant to Vaadin.
 	 */
 	public VQRCode() {
 		
 		// This method call of the Paintable interface sets the component
 		// style name in DOM tree
 		setStyleName(CLASSNAME);
 		
 		qrcode = new Image();
 		qrcode.setStyleName(CLASSNAME+"-img");
 		qrcode.setAltText("qrcode");
 		setWidget(qrcode);
 	}
 	
 	private int pixelWidth = 100;
 	@Override
 	public void setWidth(String width) {
 		super.setWidth(width);
 		qrcode.setWidth(width);
 		qrcode.setHeight(pixelHeight+"px");
 		if(width.contains("px")){
 			pixelWidth = Integer.parseInt(width.replaceAll("px", ""));
 		}
 	}
 	
 	private int pixelHeight = 100;
 	@Override
 	public void setHeight(String height) {
 		super.setHeight(height);
 		qrcode.setHeight(height);
 		qrcode.setWidth(pixelWidth+"px");
 		if(height.contains("px")){
 			pixelHeight = Integer.parseInt(height.replaceAll("px", ""));
 		}
 	}
 	
     /**
      * Called whenever an update is received from the server 
      */
 	private boolean initDone = false;
 	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
 		// This call should be made first. 
 		// It handles sizes, captions, tooltips, etc. automatically.
 		if (client.updateComponent(this, uidl, true)) {
 		    // If client.updateComponent returns true there has been no changes and we
 		    // do not need to update anything.
 			return;
 		}
 
 		// Save reference to server connection object to be able to send
 		// user interaction later
 		this.client = client;
 
 		// Save the client side identifier (paintable id) for the widget
 		paintableId = uidl.getId();
 		
 		if(!initDone){
 			initDone = true;
 			client.updateVariable(paintableId, "pixelWidth", pixelWidth, false);
 			client.updateVariable(paintableId, "pixelHeight", pixelHeight, true);
 			client.updateVariable(paintableId, "load", true, true);
 		}
 		
 		if(uidl.hasAttribute("qrcode")){
			String resUrl = client.translateVaadinUri(uidl.getStringAttribute("qrcode"));
 			qrcode.setUrl(resUrl);
 		}
 	}
 }
