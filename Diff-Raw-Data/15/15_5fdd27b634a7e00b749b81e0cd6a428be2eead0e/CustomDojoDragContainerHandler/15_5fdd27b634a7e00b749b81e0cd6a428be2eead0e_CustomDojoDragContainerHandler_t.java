 package org.wicketstuff.dojo.examples.dnd;
 
 import org.apache.wicket.IRequestTarget;
 import org.apache.wicket.RequestCycle;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.wicketstuff.dojo.dojodnd.AbstractDojoDragContainerHandler;
 
 /**
  * Custom implementation of a drag container.
  * @author B. Molenkamp
  * @version SVN: $Id$
  */
 public class CustomDojoDragContainerHandler extends AbstractDojoDragContainerHandler
 {
 	
 	/* (non-Javadoc)
 	 * @see wicket.contrib.dojo.DojoAjaxHandler#renderHead(wicket.markup.html.IHeaderResponse)
 	 */
 	public void renderHead(IHeaderResponse response)
 	{
 		super.renderHead(response);
 
 		CustomDojoDragContainer container = (CustomDojoDragContainer) getDojoDragContainer();
 		String dragClass = container.getDragClass();
 		
 		//DojoOnLoad only if not AjaxRequest
 		IRequestTarget target = RequestCycle.get().getRequestTarget();
 		if(!(target instanceof AjaxRequestTarget)){
 			response.renderJavascript("dojo.event.connect(dojo, \"loaded\", function() {" + 
					"new wicketstuff.examples.dnd.CustomDojoDragContainer('" + container.getMarkupId() + "','" + container.getDragPattern() + "', '" + dragClass + "').initializeDragContainer()});\n", 
 					container.getMarkupId() + "onLoad" );
 		}
 		//else will be done by onComponentReRendered
 	}
 	
 	public void onComponentReRendered(AjaxRequestTarget ajaxTarget)
 	{
 		super.onComponentReRendered(ajaxTarget);
 		
 		CustomDojoDragContainer container = (CustomDojoDragContainer) getDojoDragContainer();
 		String dragClass = container.getDragClass();
 
		ajaxTarget.appendJavascript("new wicketstuff.examples.dnd.CustomDojoDragContainer('" + container.getMarkupId() + "','" + container.getDragPattern() + "', '" + dragClass + "').initializeDragContainer();\n");
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.wicketstuff.dojo.dojodnd.AbstractDojoDragContainerHandler#setRequire(org.wicketstuff.dojo.AbstractRequireDojoBehavior.RequireDojoLibs)
 	 */
 	@Override
 	public void setRequire(RequireDojoLibs libs) {
 		super.setRequire(libs);
 		
 		libs.add("wicketstuff.examples.dnd.CustomDojoDragContainer");
 	}
 }
