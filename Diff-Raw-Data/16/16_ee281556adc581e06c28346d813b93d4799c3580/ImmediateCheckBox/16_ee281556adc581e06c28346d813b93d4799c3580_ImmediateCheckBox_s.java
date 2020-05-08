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
 import org.apache.wicket.markup.html.form.CheckBox;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.util.string.AppendingStringBuffer;
 import org.wicketstuff.dojo.AbstractDefaultDojoBehavior;
 
 /**
  * Checkbox that updates the server side model using AJAX whenever it is
  * clicked. After updating, method onAjaxModelUpdated is called to allow users
  * to do custom handling like persisting the change to a database.
  * <p>
  * An example:
  * 
  * <pre>
  *            	addTicketOptionForm.add(new ListView(&quot;ticketOptionsList&quot;,
  *            			new PropertyModel(activityModel, &quot;ticketOptions&quot;)) {
  *            
  *            		protected void populateItem(ListItem item) {
  *            			final TicketOption ticketOption = (TicketOption) item
  *            					.getModelObject();
  *            			...
  *            			item.add(new ImmediateCheckBox(&quot;available&quot;) {
  *            				@Override
  *            				protected void onAjaxModelUpdated() {
  *            					Activity activity = (Activity)ActivityDetailsPage.this.getModelObject();
  *            					getActivityDao().update(activity);
  *            				}
  *            			});
  *            		...
  * </pre>
  * 
  * </p>
  * 
  * @author Eelco Hillenius
  * @author Igor Vaynberg (ivaynberg)
  */
 @SuppressWarnings("serial")
 public class ImmediateCheckBox extends CheckBox
 {
 	/**
 	 * Ajax handler that immediately updates the attached component when the
 	 * onclick event happens.
 	 */
 	@SuppressWarnings("serial")
 	public static class ImmediateUpdateAjaxHandler extends AbstractDefaultDojoBehavior
 	{
 		/** checkbox this handler is attached to. */
 		private ImmediateCheckBox checkBox;
 
 		/**
 		 * Construct.
 		 */
 		public ImmediateUpdateAjaxHandler()
 		{
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
 					"javascript:immediateCheckBox('").append(getCallbackUrl()).append("', '")
 					.append(checkBox.getInputName()).append("', this.checked);");
 			tag.put("onclick", attributeValue);
 		}
 
 		/**
 		 * @see wicket.contrib.dojo.AbstractDefaultDojoBehavior#renderHead(wicket.markup.html.IHeaderResponse)
 		 */
 		public void renderHead(IHeaderResponse response)
 		{
 			super.renderHead(response);
 
 			response.renderJavascriptReference(new ResourceReference(ImmediateCheckBox.class,
 					"ImmediateCheckBox.js"));
 		}
 
 		/**
 		 * Gets the resource to render to the requester.
 		 * @param target {@link AjaxRequestTarget}
 		 */
 		protected final void respond(AjaxRequestTarget target)
 		{
 			// let the form component update its model
			checkBox.convert();
 			checkBox.updateModel();
 			checkBox.onAjaxModelUpdated(target);
 		}
 
 		/**
 		 * @see wicket.AjaxHandler#onBind()
 		 */
 		protected void onBind()
 		{
 			this.checkBox = (ImmediateCheckBox)getComponent();
 		}
 	}
 
 	/**
 	 * Construct.
 	 * 
 	 * @param parent
 	 * 
 	 * @param id
 	 */
 	public ImmediateCheckBox(String id)
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
 	public ImmediateCheckBox(String id, IModel model)
 	{
 		super(id, model);
 		add(new ImmediateUpdateAjaxHandler());
 	}
 
 	/**
 	 * Returns the name of the javascript method that will be invoked when the
 	 * processing of the ajax callback is complete. The method must have the
 	 * following signature: <code>function(type, data, evt)</code> where the
 	 * data argument will be the value of the resouce stream provided by
 	 * <code>getResponseResourceStream</code> method.
 	 * 
 	 * For example if we want to echo the value returned by
 	 * getResponseResourceStream stream we can implement it as follows: <code>
 	 * <pre>
 	 *       
 	 *       getJsCallbackFunctionName() {return(&quot;handleit&quot;);}
 	 *       
 	 *       in javascript:
 	 *       
 	 *       function handleit(type, data, evt) { alert(data); } 
 	 * </pre>
 	 * </code>
 	 * 
 	 * @see ImmediateCheckBox#getResponseResourceStream()
 	 * @return name of the client-side javascript callback handler
 	 */
 	protected String getJSCallbackFunctionName()
 	{
 		return null;
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
 
 }
