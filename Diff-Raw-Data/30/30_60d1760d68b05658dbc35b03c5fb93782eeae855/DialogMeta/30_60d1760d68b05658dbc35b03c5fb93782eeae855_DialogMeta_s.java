 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.ace.component.dialog;
 
 import org.icefaces.ace.resources.ACEResourceNames;
 import org.icefaces.resources.ICEResourceDependencies;
 import org.icefaces.resources.ICEResourceDependency;
 
 import org.icefaces.ace.meta.annotation.Component;
 import org.icefaces.ace.meta.annotation.Property;
 import org.icefaces.ace.meta.baseMeta.UIPanelMeta;
 import org.icefaces.ace.meta.annotation.ClientBehaviorHolder;
 import org.icefaces.ace.meta.annotation.ClientEvent;
 import org.icefaces.ace.api.IceClientBehaviorHolder;
 import org.icefaces.resources.ICEResourceLibrary;
 
 @Component(
         tagName         = "dialog",
         componentClass  = "org.icefaces.ace.component.dialog.Dialog",
         rendererClass   = "org.icefaces.ace.component.dialog.DialogRenderer",
         generatedClass  = "org.icefaces.ace.component.dialog.DialogBase",
         extendsClass    = "javax.faces.component.UIPanel",
         componentType   = "org.icefaces.ace.component.Dialog",
         rendererType    = "org.icefaces.ace.component.DialogRenderer",
 		componentFamily = "org.icefaces.ace.Dialog",
 		tlddoc = "The Dialog is a container component that can overlay other elements on page. Dialog has several customization options such as modal, resize, width, height, position." +
                 " If position of dialog is out of place, try putting it as last child of body." +
                 "<p>For more information, see the <a href=\"http://wiki.icefaces.org/display/ICE/Dialog\">Dialog Wiki Documentation</a>."
         )
 @ICEResourceLibrary(ACEResourceNames.ACE_LIBRARY)
 @ICEResourceDependencies({
 	@ICEResourceDependency(name=ACEResourceNames.COMBINED_CSS),
 	@ICEResourceDependency(name=ACEResourceNames.JQUERY_JS),
 	@ICEResourceDependency(name=ACEResourceNames.COMPONENTS_JS)
 })
 @ClientBehaviorHolder(events = {
 	@ClientEvent(name="close", javadoc="Fired when the panel is closed (default event).", tlddoc="Fired when the panel is closed (default event).", defaultRender="@all", defaultExecute="@this", argumentClass="org.icefaces.ace.event.CloseEvent")
 }, defaultEvent="close")
 
 public class DialogMeta extends UIPanelMeta {
 
 	@Property(tlddoc="Name of the widget to access client side api")
 	private String widgetVar;
 	
 	@Property(tlddoc="Header text")
 	private String header;
 	
 	@Property(tlddoc="Boolean value whether to allow the user to drag the Panel using its header", defaultValue="true")
 	private boolean draggable;
 	
 	@Property(tlddoc="Makes the dialog resizable. Should be false if width or height is auto, or resizing may hehave erratically.", defaultValue="true")
 	private boolean resizable;
 	
 	@Property(tlddoc="Boolean value that specifies whether the document should be shielded with a partially transparent mask to require the user to close the Panel before being able to activate any elements in the document", defaultValue="false")
 	private boolean modal;
 	
 	@Property(tlddoc="visible can be used to toggle visibility on the server, rendered should not be used that way, setting rendered=false on a visible modal dialog will not remove the modality layer, visible=false must be set first (or client-side JS function called)", defaultValue="false")
 	private boolean visible;
 
 	@Property(tlddoc="Width of the element in pixels. Default (not specified or value <= 0) is auto. If auto, resizable should be false, or resizing may hehave erratically. If auto, IE7 may not size or position properly.", defaultValue="Integer.MIN_VALUE")
 	private int width;
 	
 	@Property(tlddoc="Height of the element in pixels. Default (not specified or value <= 0) is auto. If auto, resizable should be false, or resizing may hehave erratically. If auto, IE7 may not size or position properly.", defaultValue="Integer.MIN_VALUE")
 	private int height;
 
 	@Property(tlddoc="zindex property to control overlapping with other elements", defaultValue="1000")
 	private int zindex;
 	
 	@Property(tlddoc="Minimum width of a resizable dialog", defaultValue="150")
 	private int minWidth;
 	
 	@Property(tlddoc="Minimum height of resizable dialog", defaultValue="0")
 	private int minHeight;
 	
 	@Property(tlddoc="Style class of the main container of dialog")
 	private String styleClass;
 	
 	@Property(tlddoc="Effect to use when showing the dialog. Possible values are 'blind', 'clip', 'drop', 'explode, 'fade', 'fold', 'puff', 'slide', 'scale', 'bounce', 'highlight', 'pulsate', and 'shake' (Some effects are not supported in IE7, see wiki page for more information).")
 	private String showEffect;
 	
 	@Property(tlddoc="Effect to use when hiding the dialog. Possible values are 'blind', 'clip', 'drop', 'explode, 'fade', 'fold', 'puff', 'slide', 'scale', 'bounce', 'highlight', and 'shake' (Some effects are not supported in IE browsers, see wiki page for more information).")
 	private String hideEffect;
 	
 	@Property(tlddoc="Specifies where the dialog should be displayed relative to the viewport. Possible values: \n1) a single string representing position within viewport: 'center', 'left', 'right', 'top', 'bottom'.\n2) an array containing an x,y coordinate pair in pixel offset from left, top corner of viewport (e.g. [350,100])\n3) an array containing x,y position string values (e.g. ['right','top'] for top right corner).")
 	private String position;
 	
 	@Property(tlddoc="Boolean value that Specifies whether the dialog should close when it has focus and the user presses the escape (ESC) key.", defaultValue="true")
 	private boolean closeOnEscape;
 	
 	@Property(tlddoc="Boolean value that specifies whether the dialog should have a close button in the header.", defaultValue="true")
 	private boolean closable;
 	
 	@Property(tlddoc="Javascript code to be executed when showing the dialog")
 	private String onShow;
 	
 	@Property(tlddoc="Javascript code to be executed when hiding the dialog")
 	private String onHide;
 	
 	@Property(tlddoc="Boolean value that specifies whether the dialog should have a header (default true)", defaultValue="true")
 	private boolean showHeader;
 
 	@Property(tlddoc="Id of the component to position the dialog against. Setting this id will override the 'position' attribute, using 'dialogPosition' and 'relativePosition' instead.")
 	private String relativeTo;
 	
 	@Property(tlddoc="The side of the dialog to position in relation to the target component. The format is \"<horizontal value> <vertical value>\". Possible horizontal values are \"left\", \"right\" and \"center\". Possible vertical values are \"top\", \"center\", and \"bottom\". Example: \"left top\" or \"center center\".", defaultValue="center")
 	private String dialogPosition;
 	
 	@Property(tlddoc="The side of the target component to position the dialog against. The format is \"<horizontal value> <vertical value>\". Possible horizontal values are \"left\", \"right\" and \"center\". Possible vertical values are \"top\", \"center\", and \"bottom\". Example: \"left top\" or \"center center\".", defaultValue="center")
 	private String relativePosition;
	
	@Property(tlddoc="Specifies the jQuery selector(s) of the elements inside the dialog that will be used as the drag handle. When no handle is specified, the dialog header will be the handle by default. When using multiple selectors, separate them by commas.")
	private String handle;
 }
