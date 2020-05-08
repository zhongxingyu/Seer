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
 package org.wicketstuff.dojo.markup.html.list;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.behavior.AttributeAppender;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.MarkupStream;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.RefreshingView;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 
 /**
  * This {@link DojoOrderableRepeatingView} should be added only in {@link DojoOrderableListContainer}
  * This container and it child allow top make a DragAndDropableList. When action on the list will be done
  * <code>moveItem</code> and <code>removeItem</code> will be triggered. You should implement these methods 
  * to update your model.
  * <p>
  * 	This componant should be associated to a <b>div</b> element in the markup
  * </p>
  * 
  * @author Vincent Demay
  *
  */
 public abstract class DojoOrderableRepeatingView extends RefreshingView
 {
 
 	int pos = 0; 
 
 	/**
 	 * Construct
 	 * @param id
 	 * @param model
 	 */
 	public DojoOrderableRepeatingView(String id, IModel model)
 	{
 		super(id, model);
 		setOutputMarkupId(true);
 	}
 
 	/**
 	 * Construct
 	 * @param id
 	 */
 	public DojoOrderableRepeatingView(String id)
 	{
 		super(id);
 		setOutputMarkupId(true);
 	}
 	
 	protected void renderChild(Component child)
 	{
 		String posString = Integer.toString(pos++);
 		child.add(new AttributeAppender("pos", true, new Model(posString),""));
 		super.renderChild(child);
 	}
 	
 	protected void onComponentTag(ComponentTag tag)
 	{
 		checkComponentTag(tag, "div");
 		super.onComponentTag(tag);
 	}
 	
	protected void onRender(MarkupStream markupStream)
	{
		super.onRender(markupStream);
 		pos = 0;
 	}
 
 	/**
 	 * Triggered when an item is dragged and dropped. SubClass has to implement this
 	 * method to update its model according to the from, to position.
 	 * You can also access to the {@link AjaxRequestTarget} to add some javascript during the drop
 	 * @param from where the item was
 	 * @param to where the item has been moved
 	 * @param target ajaxtarget
 	 */
 	public abstract void moveItem(int from, int to, AjaxRequestTarget target);
 
 	/**
 	 * Triggered when an item is removed. SubClass should inplement this 
 	 * method to remove item from the model
 	 * You can also access to the {@link AjaxRequestTarget} to add some javascript during the drop
 	 * @param item item that will be removed
 	 * @param target ajaxtarget
 	 */
 	public abstract void removeItem(Item item, AjaxRequestTarget target);
 	
 	
 
 }
