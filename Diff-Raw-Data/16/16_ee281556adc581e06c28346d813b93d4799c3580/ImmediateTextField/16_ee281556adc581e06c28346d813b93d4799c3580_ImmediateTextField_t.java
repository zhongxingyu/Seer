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
 package org.wicketstuff.dojo.markup.html.form;
 
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.util.string.AppendingStringBuffer;
 import org.wicketstuff.dojo.AbstractDefaultDojoBehavior;
 
 
 /**
  * Simple Dojo textfield folowing the example of ImmediateCheckBox
  * 
  * Textfield that updates the server side model using AJAX whenever the user
  * focusses on another formcomponent (onblur). After updating, the method
  * onAjaxModelUpdated is called to allow users to do custom handling like
  * persisting the change to a database.
  * 
  * Example:
  * 
  * add(textfield = new ImmediateTextField("textfield", new
  * PropertyModel(Index.this, "inputText")) { protected void onAjaxModelUpdated() {
  * //to see if model has been updated System.out.println("model: " +
  * (Index.this.toString())); } } private static final long serialVersionUID =
  * 1L;} );
  * 
  * @author Eelco Hillenius
  * @author Marco van de Haar
  * @author Ruud Booltink
  */
 @SuppressWarnings("serial")
 public class ImmediateTextField extends TextField
 {
 	/**
 	 * Construct.
 	 * 
 	 * @param parent
 	 * @param id
 	 *            component id
 	 */
 	public ImmediateTextField(String id)
 	{
 		super(id);
 		add(new ImmediateUpdateAjaxHandler());
 	}
 
 	/**
 	 * Construct.
 	 * 
 	 * @param parent
 	 * 
 	 * @param id
 	 * @param model
 	 */
 	public ImmediateTextField(String id, IModel model)
 	{
 		super(id, model);
 		add(new ImmediateUpdateAjaxHandler());
 	}
 
 	/**
 	 * @param id
 	 * @param type
 	 */
 	public ImmediateTextField(String id, Class type)
 	{
 		super(id, type);
 		add(new ImmediateUpdateAjaxHandler());
 	}
 
 	/**
 	 * @param id
 	 * @param model
 	 * @param type
 	 */
 	public ImmediateTextField(String id, IModel model, Class type)
 	{
 		super(id, model, type);
 		add(new ImmediateUpdateAjaxHandler());
 	}
 
 	/**
 	 * Called after the model is updated. Use this method to e.g. update the
 	 * persistent model. Does nothing by default.
 	 * 
      * @param target {@link AjaxRequestTarget}
 	 */
 	protected void onAjaxModelUpdated(AjaxRequestTarget target)
 	{
 	}
 
 	/**
 	 * Ajax handler that immediately updates the attached component when the
 	 * onblur event happens.
 	 */
 	private static class ImmediateUpdateAjaxHandler extends AbstractDefaultDojoBehavior
 	{
 		/** checkbox this handler is attached to. */
 		private ImmediateTextField textField;
 
 		/**
 		 * Construct.
 		 */
 		public ImmediateUpdateAjaxHandler()
 		{
 		}
 
 		/**
 		 * @see wicket.contrib.dojo.AbstractDefaultDojoBehavior#renderHead(wicket.markup.html.IHeaderResponse)
 		 */
 		public void renderHead(IHeaderResponse response)
 		{
 			super.renderHead(response);
 
 			response.renderJavascriptReference(new ResourceReference(ImmediateTextField.class,
 					"ImmediateTextField.js"));
 		}
 
 		/**
 		 * Attaches the event handler for the given component to the given tag.
 		 * 
 		 * @param tag
 		 *            The tag to attach
 		 */
 		public final void onComponentTag(final ComponentTag tag)
 		{
 			final AppendingStringBuffer attributeValue = new AppendingStringBuffer(
 					"javascript:immediateTextField('").append(getCallbackUrl()).append("', '")
 					.append(textField.getInputName()).append("', this.value);");
 			tag.put("onblur", attributeValue);
 		}
 
 		/**
 		 * @see wicket.AjaxHandler#onBind()
 		 */
 		protected void onBind()
 		{
 			this.textField = (ImmediateTextField)getComponent();
 		}
 		
 		/**
 		 * Gets the resource to render to the requester.
 		 * @param target {@link AjaxRequestTarget}
 		 */
 		protected final void respond(AjaxRequestTarget target)
 		{
 			// let the form component update its model
			textField.convertInput();
 			textField.updateModel();
 			textField.onAjaxModelUpdated(target);
 		}
 	}
 
 }
