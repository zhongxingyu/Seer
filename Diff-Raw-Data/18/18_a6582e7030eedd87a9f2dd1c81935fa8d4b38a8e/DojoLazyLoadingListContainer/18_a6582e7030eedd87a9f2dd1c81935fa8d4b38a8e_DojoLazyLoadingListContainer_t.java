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
 package org.wicketstuff.dojo.markup.html.list.lazy;
 
 
 import org.apache.wicket.Component;
 import org.apache.wicket.WicketRuntimeException;
 import org.wicketstuff.dojo.DojoIdConstants;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.model.IModel;
 
 /**
  * A lazy loading Repeating view
  * <b>UNDER DEVELOPMENT</b>
  * @author vincent demay
  *
  */
 @SuppressWarnings("serial")
 public class DojoLazyLoadingListContainer extends  WebMarkupContainer
 {
 	
 	private int max;
 
 	public DojoLazyLoadingListContainer(String id, IModel model, int maxItem)
 	{
 		super(id, model);
 		add(new DojoLazyLoadingListContainerHandler());
 		max = maxItem;
 		setOutputMarkupId(true);
 	}
 
 	public DojoLazyLoadingListContainer(String id, int maxItem)
 	{
 		super(id);
 		add(new DojoLazyLoadingListContainerHandler());
 		max = maxItem;
 		setOutputMarkupId(true);
 	}
 	
 	protected void onComponentTag(ComponentTag tag)
 	{
 		super.onComponentTag(tag);
 		tag.put(DojoIdConstants.DOJO_TYPE, DojoIdConstants.DOJO_TYPE_LAZYTABLE);
 		tag.put("maxKnewItem","" + max);
		tag.put("first", ((DojoLazyLoadingRefreshingView)getChild()).getFirst());
 	}
 	
 	/**
 	 * Find the list view in children
 	 * if none or more than one throw an exception!
 	 * 
 	 * @return the child ListView of this container
 	 */
 	public Component getChild()
 	{
 		ChildFinder visitor = new ChildFinder();
 		visitChildren(visitor);
 		return visitor.getChild();
 	}
 	
 	
 	/**
 	 * Check for child - retreive a 
 	 *   * DojoLazyLoadingRefreshingView
 	 * @author Vincent Demay
 	 *
 	 */
 	private class ChildFinder implements IVisitor{
 		private Component component = null;
 		private int componentNumber = 0;
 		
 		public Object component(Component component)
 		{
 			if (component instanceof DojoLazyLoadingRefreshingView){
 				this.component = component;
 				componentNumber ++;
 			}
 			return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
 		}
 		
 		public Component getChild(){
 			if (componentNumber != 1 ){
 				throw new WicketRuntimeException("A DojoLazyLoadingListContainer should contain exactly one DojoLazyLoadingRefreshingView as directly child");
 			}
 			return component;
 		}
 	}
 }
