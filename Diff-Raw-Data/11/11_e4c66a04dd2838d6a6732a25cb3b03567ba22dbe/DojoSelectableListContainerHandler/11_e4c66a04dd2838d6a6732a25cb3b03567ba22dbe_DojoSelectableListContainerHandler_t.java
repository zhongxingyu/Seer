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
 package wicket.contrib.dojo.markup.html.list.table;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import wicket.ajax.AjaxRequestTarget;
 import wicket.ajax.IAjaxCallDecorator;
 import wicket.contrib.dojo.AbstractRequireDojoBehavior;
 import wicket.contrib.dojo.templates.DojoPackagedTextTemplate;
 import wicket.markup.ComponentTag;
 import wicket.markup.html.IHeaderResponse;
 import wicket.markup.html.WebMarkupContainer;
 import wicket.markup.html.link.ILinkListener;
 import wicket.markup.html.list.ListView;
 import wicket.markup.repeater.Item;
 import wicket.markup.repeater.RepeatingView;
 
 /**
  * Handler associated with {@link DojoSelectableListContainer}
  * @author Vincent Demay
  *
  */
 public class DojoSelectableListContainerHandler extends AbstractRequireDojoBehavior
 {
 	/**
 	 * Allow to lock indicator on a request
 	 */
 	private boolean lockIndicator = false;
 	
 	public void onComponentReRendered(AjaxRequestTarget ajaxTarget)
 	{
 		String selectedIndex[] = ((DojoSelectableListContainer)getComponent()).getSelectedIndex();
 		super.onComponentReRendered(ajaxTarget);
 		if (selectedIndex != null){
 			String selected = "";
 			for (int i=0; i < selectedIndex.length; i++){
 				int pos = Integer.parseInt(selectedIndex[i]);
 				if (i==0){
 					selected += "[";
 				}
 				if (i == selectedIndex.length -1){
 					selected += pos + "]";
 				}else {
 					selected += pos + ",";
 				}
 			}
 			ajaxTarget.appendJavascript("dojo.widget.byId('" + getComponent().getMarkupId() + "').selectIndexes(" + selected + ")");
 		}
 	}
 
 	//child of this container
 	private WebMarkupContainer child;
 
 	/**
 	 * 
 	 * @param listView
 	 */
 	public DojoSelectableListContainerHandler()
 	{
 		super();
 	}
 
 	/**
 	 * @see wicket.contrib.dojo.AbstractRequireDojoBehavior#setRequire(wicket.contrib.dojo.AbstractRequireDojoBehavior.RequireDojoLibs)
 	 */
 	public void setRequire(RequireDojoLibs libs)
 	{
 		libs.add("dojoWicket.widget.SelectableTable");
 	}
 
 	protected final void respond(AjaxRequestTarget target)
 	{
 		List selected = ((DojoSelectableListContainer)getComponent()).getSelected();
 
 		String indexList[] = getComponent().getRequest().getParameters("select");
 
 		if (indexList == null)
 		{
			if (selected.size() > 0)
				// No parameters by the name "select", so double-click has occured,
				// call the onChoose() method
				((DojoSelectableListContainer)getComponent()).onChoose(target, selected.get(0));
 		}
 		else
 		{
 			// A new selection has been made
 			
 			// Clear current selection
 			selected.clear();
 
 			// Compute new selection 
 			if (child instanceof ListView){
 				ListView listView = (ListView) child;
 				List all = listView.getList();
 				int pos;
 				for (int i=0; i < indexList.length; i++){
 					pos = Integer.parseInt(indexList[i]);
 					selected.add(all.get(pos));
 				}
 			}else if (child instanceof RepeatingView){
 				RepeatingView repeatingView = (RepeatingView) child;
 				Iterator ite = repeatingView.iterator();
 				List selectedIndexes = Arrays.asList(indexList);
 				int pos = 0;
 				while (ite.hasNext()){
 					Object element = ite.next();
 					if (selectedIndexes.contains(Integer.toString(pos))){
 						selected.add(((Item)element).getModelObject());
 					}
 					pos++;
 				}
 			}
 			//store selected indexes
 			((DojoSelectableListContainer)getComponent()).setSelectedIndex(indexList);
 			// Call the onSelection() method
 			((DojoSelectableListContainer)getComponent()).onSelection(target, selected);
 		}
 	}
 	
 	/**
 	 * @return javascript that will generate an ajax GET request to this
 	 *         behavior *
 	 * @param recordPageVersion
 	 *            if true the url will be encoded to execute on the current page
 	 *            version, otherwise url will be encoded to execute on the
 	 *            latest page version
 	 */
 	protected final CharSequence getCallbackScript()
 	{
 		lockIndicator = ((DojoSelectableListContainer)getComponent()).isLockIndicatorOnClick();
 		return getCallbackScript("wicketAjaxGet('" + super.getCallbackUrl() + "' + getSelectableTableSelection('"+getComponent().getMarkupId()+"')", null,
 				null);
 	}
 	
 	/**
 	 * return javascript that will be used to respond to Double click
 	 * @return javascript that will be used to respond to Double click
 	 */
 	protected final CharSequence getDoubleClickCallbackScripts(){
 		if (((DojoSelectableListContainer) getComponent()).isAjaxModeOnChoose()){
 			return getCallbackScript("wicketAjaxGet('" + super.getCallbackUrl(true, true) + "'", null,null);
 		}else{
 			CharSequence url = ((DojoSelectableListContainer) getComponent()).urlFor(ILinkListener.INTERFACE);
 			return "window.location.href='" + url + "' + getSelectableTableSelection('"+getComponent().getMarkupId()+"') ";
 		}
 	}
 	
 	protected IAjaxCallDecorator getAjaxCallDecorator()
 	{
 		if(lockIndicator){
 			lockIndicator = false;
 			return null;
 		}else{
 			return super.getAjaxCallDecorator();
 		}
 	}
 
 	/**
 	 * Add onSelect and on choose event listener
 	 */
 	protected void onComponentTag(ComponentTag tag)
 	{
 		super.onComponentTag(tag);
 		//TODO : nothing better to do?
 		tag.put("onSelect", "currentSelectableTable = '" + getComponent().getMarkupId() + "';" + getCallbackScript().toString().replace("&", "&amp;"));
 		tag.put("onChoose", getDoubleClickCallbackScripts().toString().replace("&", "&amp;"));
 	}
 
 	public WebMarkupContainer getChild()
 	{
 		return child;
 	}
 
 	public void setChild(WebMarkupContainer child)
 	{
 		this.child = child;
 	}
 
 	public void renderHead(IHeaderResponse response)
 	{
 		super.renderHead(response);
 		DojoPackagedTextTemplate template = new DojoPackagedTextTemplate(DojoSelectableListContainer.class, "DojoSelectableTableContainerTemplate.js");
 		response.renderJavascript(template.asString(), template.getStaticKey());
 	}
 
 	
 
 }
