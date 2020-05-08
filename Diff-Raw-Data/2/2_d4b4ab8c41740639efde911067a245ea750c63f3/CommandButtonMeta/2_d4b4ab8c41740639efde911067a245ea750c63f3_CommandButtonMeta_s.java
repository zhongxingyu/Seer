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
 
 package org.icefaces.mobi.component.button;
 
 import org.icefaces.ace.meta.annotation.*;
 import org.icefaces.ace.meta.baseMeta.UICommandMeta;
 import org.icefaces.ace.meta.annotation.ClientBehaviorHolder;
 import org.icefaces.ace.meta.annotation.ClientEvent;
 import org.icefaces.ace.meta.annotation.Property;
import org.icefaces.ace.api.IceClientBehaviorHolder;
 import org.icefaces.mobi.utils.TLDConstants;
 
 import javax.faces.application.ResourceDependencies;
 import javax.faces.application.ResourceDependency;
 import javax.faces.component.UIParameter;
 
 import java.util.List;
 
 @Component(
         tagName = "commandButton",
         componentClass = "org.icefaces.mobi.component.button.CommandButton",
         rendererClass = "org.icefaces.mobi.component.button.CommandButtonRenderer",
         generatedClass = "org.icefaces.mobi.component.button.CommandButtonBase",
         extendsClass = "javax.faces.component.UICommand",
         componentType = "org.icefaces.component.CommandButton",
         rendererType = "org.icefaces.component.CommandButtonRenderer",
         componentFamily = "org.icefaces.CommandButton",
         tlddoc = "Renders an HTML \"input\" element. The component can be styled for the " +
         		"supported mobile device platforms, as well as be used in a group of buttons. " 
 )
 
 @ResourceDependencies({
         @ResourceDependency(library = "org.icefaces.component.util", name = "component.js")
 })
 @ClientBehaviorHolder(events = {
 	@ClientEvent(name="click", javadoc="Fired when a command button is clicked. ",
             tlddoc="Fired when commandButton is clicked. ", defaultRender="@all",
             defaultExecute="@all")}, defaultEvent="click")
 public class CommandButtonMeta extends UICommandMeta {
 
     @Property(defaultValue = "false", tlddoc =TLDConstants.SINGLESUBMIT)
     private boolean singleSubmit;
 
     @Property(defaultValue = "false", tlddoc = TLDConstants.DISABLED)
     private boolean disabled;
 
     @Property(tlddoc = TLDConstants.TABINDEX)
     private Integer tabindex;
 
     @Property(tlddoc = TLDConstants.STYLECLASS)
     private String styleClass;
 
     @Property(tlddoc = TLDConstants.STYLE)
     private String style;
 
     @Property(tlddoc = "Determines the button style. Four styles of buttons are allowed: \"important\"," +
     		" \"back\" and \"attention\", \"unimportant\", and, if empty or null, \"default\". ",
             required = Required.no)
     private String buttonType;
     
     @Property(defaultValue="button", tlddoc = "The value of the \"type\" attribute for the input element, \"button\", or \"submit\"" +
     		 "The default is \"button\". ",
             required = Required.no)
     private String type;
 
     @Property(defaultValue = "false", tlddoc = "Flag indicating that, if this component is activated by the " +
     		"user, notifications should be delivered to interested listeners and actions immediately (that is," +
     		" during Apply Request Values phase) rather than waiting until Invoke Application phase.")
     private boolean immediate;
 
     @Property(tlddoc=" The id of panelConfirmation component to be associated with the commandButton. ")
     private String panelConfirmation;
 
     @Property(tlddoc="The id of blocking submitNotification panel, which will block UI access " +
     		"to the page until the response has been received. ")
     private String submitNotification;
 
     @Property(defaultValue = "false", tlddoc = "The selected state of button. This is normally activated " +
     		"when the commandButton is part of a commandButtonGroup.")
     // TODO move selected state out into CommandButtonGroup model. Would be nice if this was transparent.
     private boolean selected;
     
     @Property(tlddoc="The id of a contentPane in a contentStack that will be displayed when selecting the commandButton. ")
     private String openContentPane;
     
     @Property(tlddoc="If true, default false, the commandButton will disable itself when offline ")
     private boolean disableOffline;
 
     @Field
     private String groupId;
 
     @Field(defaultValue="false")
     private Boolean selectedButton;
 
     @Field(defaultValue="")
     private String params;
 
     @Field
     private String behaviors;
 
     @Field
     private String submitNotificationId;
 
     @Field
     private String panelConfirmationId;
 
     @Field
     private StringBuilder jsCall;
 
     @Field
     private String name;
     
     @Property(tlddoc = "JavaScript to be evaluated when the browser goes online. Two arguments are passed through to the context: the online or offline 'event' argument, and the 'elem' argument for the root element of the component. ")
     private String ononline;
     
     @Property(tlddoc = "JavaScript to be evaluated when the browser goes offline. Two arguments are passed through to the context: the online or offline 'event' argument, and the 'elem' argument for the root element of the component.")
     private String onoffline;
 
 }
