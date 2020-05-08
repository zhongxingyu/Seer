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
  
 package org.icefaces.ace.component.ajax;
 
 import org.icefaces.ace.meta.annotation.TagHandler;
 import org.icefaces.ace.meta.annotation.Property;
 import org.icefaces.ace.meta.annotation.TagHandlerType;
 import org.icefaces.ace.meta.annotation.Required;
 
 import javax.el.MethodExpression;
 
 @TagHandler(
     tagName = "ajax",
     tagHandlerType = TagHandlerType.TAG_HANDLER,
     tagHandlerClass = "org.icefaces.ace.component.ajax.AjaxBehaviorHandler",
     generatedClass = "org.icefaces.ace.component.ajax.AjaxBehaviorHandlerBase",
     extendsClass = "javax.faces.view.facelets.TagHandler",
 	behaviorId = "org.icefaces.ace.component.AjaxBehavior",
 	behaviorClass = "org.icefaces.ace.component.ajax.AjaxBehavior",
     tlddoc = "Applied on components that support client behaviors similar to the standard f:ajax behavior." +
                  "<p>For more information, see the " +
                  "<a href=\"http://wiki.icefaces.org/display/ICE/Ajax\">Ajax Wiki Documentation</a>."
 )
 public class AjaxBehaviorHandlerMeta {
 
 	@Property(required=Required.no, tlddoc="Method to process in partial request.")
 	private MethodExpression listener;
 	
 	@Property(required=Required.no, tlddoc="Boolean value that determines the phaseId, when true actions are processed at apply_request_values, when false at invoke_application phase. If not specified, the component's immediate value is applied.")
 	private boolean immediate;
 	
 	@Property(required=Required.no, tlddoc="Component(s) to execute in the ajax request. The format is the same as that of the f:ajax tag.")
 	private String execute;
 	
 	@Property(required=Required.no, tlddoc="Component(s) to render in the ajax rquest. The format is the same as that of the f:ajax tag.")
 	private String render;
 	
	@Property(required=Required.no, tlddoc="Javascript handler to execute before ajax request is begins. The function has to return 'true' to continue with the ajax request; if 'false' or nothing is returned, the ajax request will be aborted.")
 	private String onStart;
 	
	@Property(required=Required.no, tlddoc="Javascript handler to execute when ajax request is completed.")
 	private String onComplete;
 	
	@Property(required=Required.no, tlddoc="Javascript handler to execute when ajax request succeeds.")
 	private String onSuccess;
 	
	@Property(required=Required.no, tlddoc="Javascript handler to execute when ajax request fails.")
 	private String onError;
 	
 	@Property(required=Required.no, tlddoc="Disables ajax behavior.")
 	private boolean disabled;
 	
 	@Property(required=Required.no, tlddoc="Name of the event that will trigger the ajax request.")
 	private String event;
 }
