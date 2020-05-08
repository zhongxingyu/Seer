 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package wicket.contrib.dojo.markup.html;
 
 import wicket.MarkupContainer;
 import wicket.ajax.AjaxRequestTarget;
 import wicket.ajax.markup.html.AjaxLink;
 import wicket.contrib.dojo.AbstractDefaultDojoBehavior;
 import wicket.markup.ComponentTag;
 import wicket.model.IModel;
 
 /**
  * A component that allows a trigger request to be triggered via html anchor tag
  * 
  * 
  * @author Vincent Demay
  * 
  */
 public abstract class DojoLink extends AjaxLink
 {
 
 	/**
 	 * Construct.
 	 * 
 	 * @param id
 	 */
 	public DojoLink(final String id)
 	{
 		this(id, null);
 	}
 
 	/**
 	 * Construct.
 	 * 
 	 * @param id
 	 * @param model
 	 */
 	public DojoLink(final String id, final IModel model)
 	{
 		super(id, model);
 
 		add(new AbstractDefaultDojoBehavior()
 		{
 			private static final long serialVersionUID = 1L;
 
 			protected void onComponentTag(ComponentTag tag)
 			{
 				// return false to end event processing in case the DojoLink is bound to a <button> contained in a form
				tag.put("onclick", getCallbackScript() + "; return false;");
 			}
 
 			protected void respond(AjaxRequestTarget target)
 			{
 				((DojoLink)getComponent()).onClick(target);
 			}
 
 		});
 	}
 
 }
