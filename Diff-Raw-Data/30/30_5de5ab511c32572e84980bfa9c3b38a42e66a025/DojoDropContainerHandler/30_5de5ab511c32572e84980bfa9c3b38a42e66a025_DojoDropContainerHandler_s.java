 package wicket.contrib.dojo.dojodnd;
 
 import java.util.HashMap;
 
 import wicket.IRequestTarget;
 import wicket.RequestCycle;
 import wicket.ajax.AjaxRequestTarget;
 import wicket.contrib.dojo.AbstractRequireDojoBehavior;
 import wicket.contrib.dojo.templates.DojoPackagedTextTemplate;
 import wicket.markup.html.IHeaderResponse;
 
 /**
  * Handler for a {@link DojoDropContainer}
  * @author <a href="http://www.demay-fr.net/blog">Vincent Demay</a>
  *
  */
 class DojoDropContainerHandler extends AbstractRequireDojoBehavior
 {
 	/** container handler is attached to. */
 	private DojoDropContainer container;
 
 
 	/**
 	 * @see wicket.AjaxHandler#onBind()
 	 */
 	protected void onBind()
 	{
 		this.container = (DojoDropContainer)getComponent();
 	}
 	
 	/**
 	 * 
 	 */
 	public void renderHead(IHeaderResponse response)
 	{
 		super.renderHead(response);
 		super.renderHead(response);
 
 		DojoPackagedTextTemplate template = new DojoPackagedTextTemplate(this.getClass(), "DojoDropContainerHandlerTemplate.js");
		
		HashMap map = new HashMap();
		map.put("MarkupId", container.getMarkupId());
		map.put("DropId", container.getDropPattern());
		map.put("CallbackUrl", getCallbackUrl());
		map.put("Id", container.getId());
		response.renderJavascript(template.asString(map), template.getWidgetUniqueKey(this.getComponent()));
 	
 		IRequestTarget target = RequestCycle.get().getRequestTarget();
 		if(!(target instanceof AjaxRequestTarget)){
			response.renderJavascript("dojo.event.connect(dojo, \"loaded\", \"initDrop" + container.getMarkupId() + "\");\n", container.getMarkupId() + "onLoad");
 		}
 	}
 	
 	public void onComponentReRendered(AjaxRequestTarget ajaxTarget)
 	{
 		super.onComponentReRendered(ajaxTarget);
		ajaxTarget.appendJavascript("initDrop" + container.getMarkupId() + "();\n");
 	}
 	
 	protected void respond(AjaxRequestTarget target)
 	{
 		container.onAjaxModelUpdated(target);
 	}
 
 	public void setRequire(RequireDojoLibs libs)
 	{
 		libs.add("dojo.dnd.*");
 		libs.add("dojo.event.*");
 		libs.add("dojo.io.*");
 	}
 	
 
 
 }
