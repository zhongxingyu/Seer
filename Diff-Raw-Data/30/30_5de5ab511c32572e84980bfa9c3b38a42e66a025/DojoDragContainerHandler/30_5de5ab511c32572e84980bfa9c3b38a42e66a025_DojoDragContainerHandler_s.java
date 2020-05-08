 package wicket.contrib.dojo.dojodnd;
 
 import java.util.HashMap;
 
 import wicket.IRequestTarget;
 import wicket.RequestCycle;
 import wicket.ajax.AjaxRequestTarget;
 import wicket.contrib.dojo.AbstractRequireDojoBehavior;
 import wicket.contrib.dojo.templates.DojoPackagedTextTemplate;
 import wicket.markup.html.IHeaderResponse;
 
 /**
  * <p>
  * A Handler associated to {@link DojoDragContainer}
  * </p>
  * 
  * @author Vincent Demay
  *
  */
 public class DojoDragContainerHandler extends AbstractRequireDojoBehavior
 {
 	/** container handler is attached to. */
 	private DojoDragContainer container;
 	
 	/**
 	 * @see wicket.AjaxHandler#onBind()
 	 */
 	protected void onBind()
 	{
 		this.container = (DojoDragContainer)getComponent();
 	}
 	
 	
 	protected void respond(AjaxRequestTarget target)
 	{
 		//DO NOTHING
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see wicket.contrib.dojo.DojoAjaxHandler#renderHead(wicket.markup.html.IHeaderResponse)
 	 */
 	public void renderHead(IHeaderResponse response)
 	{
 		super.renderHead(response);
 		
 		DojoPackagedTextTemplate template = new DojoPackagedTextTemplate(this.getClass(), "DojoDragContainerHandlerTemplate.js");
 		HashMap map = new HashMap();
		map.put("MarkupId", container.getMarkupId());
		map.put("DragId", container.getDragPattern());
		response.renderJavascript(template.asString(map), template.getWidgetUniqueKey(this.getComponent()));
 		
 		//DojoOnLoad only if not AjaxRequest
 		IRequestTarget target = RequestCycle.get().getRequestTarget();
 		if(!(target instanceof AjaxRequestTarget)){
			response.renderJavascript("dojo.event.connect(dojo, \"loaded\", \"initDrag" + container.getMarkupId() + "\");\n", container.getMarkupId() + "onLoad" );
 		}
 		//else will be done by onComponentReRendered
 	}
 	
 	public void onComponentReRendered(AjaxRequestTarget ajaxTarget)
 	{
 		super.onComponentReRendered(ajaxTarget);
		ajaxTarget.appendJavascript("initDrag" + container.getMarkupId() + "()\n");
 	}
 
 
 	public void setRequire(RequireDojoLibs libs)
 	{
 		libs.add("dojo.dnd.*");
 		libs.add("dojo.event.*");
 		libs.add("dojo.io.*");
 	}
 
 }
