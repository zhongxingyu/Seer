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
 
 		response.renderJavascript(template.asString(), template.getStaticKey());
 	
 		IRequestTarget target = RequestCycle.get().getRequestTarget();
 		if(!(target instanceof AjaxRequestTarget)){
			response.renderJavascript("dojo.event.connect(dojo, \"loaded\", function() {initDrop('" + container.getMarkupId() + "', '" + container.getDropPattern() + "', '" + getCallbackUrl() + "'); });" , container.getMarkupId() + "onLoad");
 		}
 	}
 	
 	public void onComponentReRendered(AjaxRequestTarget ajaxTarget)
 	{
 		super.onComponentReRendered(ajaxTarget);
 		ajaxTarget.appendJavascript("initDrop('" + container.getMarkupId() + "', '" + container.getDropPattern() + "', '" + getCallbackUrl() + "');\n");
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
