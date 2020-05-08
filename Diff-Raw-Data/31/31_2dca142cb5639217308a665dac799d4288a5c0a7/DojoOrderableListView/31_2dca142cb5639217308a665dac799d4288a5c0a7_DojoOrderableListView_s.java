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
 
 import java.util.List;
 
 import org.apache.wicket.behavior.AttributeAppender;
 import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 
 /**
  * DojoOrderableListView should be added on a {@link DojoOrderableListContainer}
  * This container and it child allow top make a DragAndDropableList. Model associated with the {@link DojoOrderableListView}
  * will be automaticaly updated during dnd
  * <p>
  * 	This componant should be associated to a <b>div</b> element in the markup
  * </p>
  * <p>
  * 
  * <b>Sample</b>
  * <pre>
  * public class OrderableList extends WebPage {
  * 
  * 	static final List objList  = new  ArrayList();
  * 
  * 	
  * 	public OrderableList() {
  * 		super();
  * 		if (objList.size() == 0){
  * 			objList.add("foo1");
  * 			objList.add("bar1");
  * 			objList.add("foo2");
  * 			objList.add("bar2");
  * 			objList.add("foo3");
  * 			objList.add("bar3");
  * 			objList.add("foo4");
  * 			objList.add("bar4");
  * 			objList.add("foo5");
  * 			objList.add("bar5");
  * 			objList.add("foo6");
  * 			objList.add("bar6");
  * 		}
  * 		DojoOrderableListViewContainer container = new DojoOrderableListViewContainer("container");
  * 		add(container);
  * 		DojoOrderableListView list = new DojoOrderableListView("list", objList){
  * 
  * 			protected void populateItem(ListItem item) {
  * 				item.add(new Label("label",(String)item.getModelObject()));
  * 				item.add(new DojoOrderableListRemover("remover", item));
  * 				
  * 			}
  * 			
  * 		};
  * 		container.add(list);
  * 	}
  * 
  * }
  * </pre>
  * </p>
  * @author Vincent Demay
  */
 public abstract class DojoOrderableListView extends ListView
 {
 	int pos = 0; 
 
 	/**
 	 * Construct
 	 * @param id id
 	 * @param model model
 	 */
 	public DojoOrderableListView(String id, IModel model)
 	{
 		super(id, model);
 	}
 
 	/**
 	 * Construct
 	 * @param id id 
 	 * @param list list
 	 */
 	public DojoOrderableListView(String id, List list)
 	{
 		super(id, list);
 	}
 
 	/**
 	 * Construct
 	 * @param id id
 	 */
 	public DojoOrderableListView(String id)
 	{
 		super(id);
 	}
 	
 	protected void onComponentTag(ComponentTag tag)
 	{
 		checkComponentTag(tag, "div");
 		super.onComponentTag(tag);
 	}
 
 	protected void renderItem(ListItem item)
 	{
 		String posString = Integer.toString(pos++);
 		item.add(new AttributeAppender("pos", true, new Model(posString),""));
 		super.renderItem(item);
 	}

	protected void onRender(MarkupStream markupStream)
	{
		super.onRender(markupStream);
 		//position computing should be restart : 
 		pos = 0;
 	}
 
 }
