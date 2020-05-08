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
 package org.wicketstuff.dojo.markup.html.container;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.Component.IVisitor;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.wicketstuff.dojo.AbstractRequireDojoBehavior;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 
 /**
  * An {@link AbstractRequireDojoBehavior} allowing to find a {@link IDojoContainer} by its
  * markup id.
  * Handler to use with  {@link AbstractDojoChangeContainer}
  * 
  * @author Vincent Demay
  *
  */
 public abstract class AbstractDojoChangeContainerHandler extends AbstractRequireDojoBehavior {
 
 
 	/**
 	 * Find a direct {@link IDojoContainer} child 
 	 * @param markupId component MarkupId to find 
 	 * @return the {@link IDojoContainer} if found or null otherwise
 	 */
 	protected IDojoContainer getChildByMarkupId(String markupId){
 		ChildFinder finder = new ChildFinder(markupId);
 		if (getComponent() instanceof WebMarkupContainer){
 			((WebMarkupContainer)getComponent()).visitChildren(finder);
 		}
 		return finder.getChild();
 	}
 	
 	/**
 	 * render js to connect handler on each child
 	 * see {@link RenderHeadCreator}
 	 */
 	public void renderHead(IHeaderResponse response)
 	{
 		super.renderHead(response);
 
 		//add onShow event on each child in the Container
 		AbstractDojoChangeContainer container = (AbstractDojoChangeContainer)getComponent();
 		RenderHeadCreator head = new RenderHeadCreator(container);
 		container.visitChildren(head);
 		
 		response.renderJavascript(head.getHead(), getComponent().getMarkupId() + "script");
 	}
 	
 
 	@Override
 	public void onComponentReRendered(AjaxRequestTarget ajaxTarget) {
 		super.onComponentReRendered(ajaxTarget);
 		//if a tab is selected keepit on the refresh of the widget
 		AbstractDojoChangeContainer container = (AbstractDojoChangeContainer) getComponent();
 		ajaxTarget.appendJavascript("" +
    			"var selected = dojo.widget.byId('" + container.getSelectedChildId() + "');\n" + 
     			"dojo.widget.byId('" + container.getMarkupId() + "').selectChild(selected)");
 	}
 	
 	protected final void respond(AjaxRequestTarget target)
 	{
 		AbstractDojoContainer container = (AbstractDojoContainer) getComponent();
 		String childId = container.getRequest().getParameter("childId");
 		IDojoContainer child = getChildByMarkupId(childId);
 		((AbstractDojoChangeContainer)getComponent()).setSelected(child);
 		((AbstractDojoChangeContainer)getComponent()).onSelectionChange(child, target);
 	}
 	
 	/**
 	 * @return javascript that will generate an ajax GET request to this
 	 *         behavior *
 	 * @param recordPageVersion
 	 *            if true the url will be encoded to execute on the current page
 	 *            version, otherwise url will be encoded to execute on the
 	 *            latest page version
 	 */
 	protected CharSequence getCallbackScript(String id)
 	{
 		return generateCallbackScript("wicketAjaxGet('" + getCallbackUrl() + "&childId=" + id + "'");
 	}
 
 	/******************************************************/
 	
 
 	/**
 	 * This visitor lookup a {@link IDojoContainer} by its markupid
 	 * @author Vincent Demay
 	 *
 	 */
 	private class ChildFinder implements IVisitor{
 
 		private String markupId;
 		private IDojoContainer child;
 		
 		public ChildFinder(String markupId) {
 			super();
 			this.markupId = markupId;
 		}
 
 		public Object component(Component component) {
 			if (component instanceof IDojoContainer && markupId.equals(component.getMarkupId())){
 				child = (IDojoContainer) component;
 			}
 			return IVisitor.CONTINUE_TRAVERSAL;
 		}
 		
 		public IDojoContainer getChild(){
 			return child;
 		}
 		
 	}
 	
 	/**
 	 * Create the head contribution to connect onchange on childs
 	 * @author Vincent demay
 	 */
 	private class RenderHeadCreator implements IVisitor{
 
 		private String toReturn;
 		private AbstractDojoChangeContainer container;
 		
 		public RenderHeadCreator(AbstractDojoChangeContainer container)
 		{
 			toReturn = "";
 			toReturn += "function initContainerChild" + container.getMarkupId() + "(){\n";
 			
 			this.container = container;
 			
 		}
 
 		public Object component(Component component)
 		{
 			String id = component.getMarkupId();
 			toReturn += "	var widget = dojo.widget.byId('" + id + "')\n";
 			toReturn += "	dojo.event.connect(widget,'onShow', function(){" + getCallbackScript(id) + "})\n";
 			
 			return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
 		}
 		
 		public String getHead(){
 			toReturn += "}\n";
 			toReturn += "dojo.event.connect(dojo, \"loaded\", \"initContainerChild" + container.getMarkupId() + "\");\n";
 			return toReturn;
 		}
 		
 	}
 }
