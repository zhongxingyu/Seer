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
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.ComponentTag;
 import org.wicketstuff.dojo.AbstractRequireDojoBehavior;
 
 /**
  * <b>UNDER DEVELOPMENT</b>
  * @author Vincent Demay
  *
  */
 @SuppressWarnings("serial")
 public class DojoLazyLoadingListContainerHandler extends AbstractRequireDojoBehavior
 {
 	/**
 	 * 
 	 */
 	public void setRequire(RequireDojoLibs libs)
 	{
 		libs.add("dojoWicket.widget.LazyTable");
 	}
 
 	protected final void respond(AjaxRequestTarget target)
 	{
 		int first = Integer.parseInt(getComponent().getRequest().getParameter("start"));
 		int end = Integer.parseInt(getComponent().getRequest().getParameter("end"));
 		int count = end - first;
 		
 		DojoLazyLoadingRefreshingView child = ((DojoLazyLoadingRefreshingView)((DojoLazyLoadingListContainer)getComponent()).getChild());
 		child.setFirst(first);
 		child.setCount(count);
		target.addComponent(getComponent());
 		
 		target.prependJavascript("dojo.widget.byId(\"" + getComponent().getMarkupId() + "\").contentTable.getElementsByTagName('tbody')[0].id='" + child.getMarkupId() + "'");
		//target.appendJavascript("dojo.widget.byId('" + getComponent().getMarkupId() + "').postUpdate()");
 	}
 
 
 	protected void onComponentTag(ComponentTag tag)
 	{
 		super.onComponentTag(tag);
 		tag.put("onReload", "" + getPreCallbackScript() + getCallbackScript());
 	}
 	
 	protected CharSequence getPreCallbackScript(){
 		return "";//"this.contentTable.innerHTML='<tbody id=" + getComponent().getMarkupId() + "_contentItems><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr></tbody>';";
 	}
 	
 
 	protected CharSequence getCallbackScript()
 	{
 		return getCallbackScript("wicketAjaxGet('" + getCallbackUrl() + "&start=' + arguments[0] + '&end=' +  arguments[1]", null,
 				null);
 	}
 }
