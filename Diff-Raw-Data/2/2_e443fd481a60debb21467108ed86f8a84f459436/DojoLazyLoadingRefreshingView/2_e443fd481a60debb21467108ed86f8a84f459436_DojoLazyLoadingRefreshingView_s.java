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
 package wicket.contrib.dojo.html.list.lazy;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import wicket.markup.repeater.RefreshingView;
 import wicket.markup.repeater.data.IDataProvider;
 import wicket.model.IModel;
 import wicket.model.Model;
 
 /**
  * <p>
  * This widget is a repeating view which will be lazy loaded : Element will be asked to the server
  * in real time when user scroll the list. This Widget should be added in a {@link DojoLazyLoadingListContainer}.
  * </p>
  * <p>
  * 	<b>Sample</b>
  *  <pre>
  *public class LazyTableSample extends WebPage {
  *	
  *	public LazyTableSample(PageParameters parameters){
  *		DojoLazyLoadingListContainer container = new DojoLazyLoadingListContainer(this, "container", 3000)
  *		DojoLazyLoadingRefreshingView list = new DojoLazyLoadingRefreshingView(container, "table"){ 
  *
  *			public Iterator iterator(int first, int count) {
  *				ArrayList&ltString> list = new ArrayList&ltString>();
  * 				int i = 0;
  *				while(i < count){
  *					list.add("foo" + (first + i++));
  *				}
  *				
  *				return list.iterator();
  *			} 
  *
  *			protected void populateItem(Item item) {
  *				new Label(item, "label",item.getModel());			
  *			}
  *			
  *		};
  *	}
  *}
  *
  *  </pre>
  * </p>
  * @author Vincent demay
  *
  */
 public abstract class DojoLazyLoadingRefreshingView extends RefreshingView implements IDataProvider
 {
 	private int first = 0;
 	private int count = 30;
 	
 	private DojoLazyLoadingListContainer parent;
 	
 	/**
 	 * {@link DojoLazyLoadingRefreshingView} constructor
 	 * @param parent parent where the widget will be added
 	 * @param id widget id
 	 * @param model model associated to the widget
 	 */
 	public DojoLazyLoadingRefreshingView(DojoLazyLoadingListContainer parent, String id, IModel model) {
 		super(parent, id, model);
 		this.parent = parent;
 		setOutputMarkupId(true);
 	}
 
 	/**
 	 * {@link DojoLazyLoadingRefreshingView} constructor
 	 * @param parent parent where the widget will be added
 	 * @param id widget id
 	 */
 	public DojoLazyLoadingRefreshingView(DojoLazyLoadingListContainer parent, String id) {
 		this(parent, id, null);
 	}
 
 
 	/**
 	 * Return an iterator used to display element on client side
 	 * This method is called when users scrolled the list and items
 	 * are not yet available
 	 * @param first First element to send on client-side
 	 * @param count Number of element to Send
 	 * @return A repeater containing count element starting from first
 	 */
 	public abstract Iterator iterator(int first, int count);
 	
 	
 	public IModel model(Object object){
 		return new Model(object);
 	}
 
	public void detach(){}

 	/**
 	 * return count
 	 * @return count
 	 */
 	public int getCount()
 	{
 		return count;
 	}
 
 	/**
 	 * set count
 	 * @param count
 	 */
 	public void setCount(int count)
 	{
 		this.count = count;
 	}
 
 	/**
 	 * return first
 	 * @return first
 	 */
 	public int getFirst()
 	{
 		return first;
 	}
 
 	/**
 	 * set first
 	 * @param first
 	 */
 	public void setFirst(int first)
 	{
 		this.first = first;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	protected final Iterator getItemModels(){
 		
 		final List toReturn = new ArrayList();
 		
 		Iterator it = iterator(first, count);
 		while (it.hasNext())
 		{
 			toReturn.add(model(it.next()));
 		}
 
 		return toReturn.iterator();
 	}
 }
