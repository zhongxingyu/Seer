 package wicket.contrib.dojo.autoupdate;
 
 import wicket.Application;
 import wicket.AttributeModifier;
 import wicket.Component;
 import wicket.RequestCycle;
 import wicket.Response;
 import wicket.WicketRuntimeException;
 import wicket.contrib.dojo.DojoAjaxHandler;
 import wicket.markup.html.PackageResourceReference;
 import wicket.markup.html.internal.HtmlHeaderContainer;
 import wicket.model.Model;
 import wicket.response.StringResponse;
 import wicket.util.resource.IResourceStream;
 import wicket.util.resource.StringBufferResourceStream;
 
 /**
  * Dojo Ajax auto update handler. <br/>
  * Bind this handler to any component implementing IUpdatable.<br/>
  * Every <i>interval</i> the bound component's update() method will be called<br/>
  * followed by a rerender of the bound component.<br/>
  * 
  * @author Ruud Booltink
  * @author Marco van de Haar
  *
  */
 public class DojoAutoUpdateHandler extends DojoAjaxHandler
 {
 	private final int interval;
 	private String HtmlId;
 	
 	/**
 	 * 
 	 * @param interval The amount of milliseconds between updates and renders.
 	 */
 	public DojoAutoUpdateHandler(int interval)
 	{
 		this.interval = interval;
 	    
 
 	}
 
     
 	/**
 	 * Internal render method. This handler currently only supports 1 component.<br/>
 	 * The array is for future usage.
 	 * 
 	 * @param components The components to be rerendered.
 	 * @return Rerendered component's resource stream.
 	 */
 	protected IResourceStream render(final Component[] components) 
 	{
 		
 		
 		StringBufferResourceStream response = new StringBufferResourceStream("text/plain");
 		//response.append("<?xml version='1.0' encoding='utf-8'?>");
 		//response.append("<components>");
 		
 		if (components != null)
 		{
 			
 			Response resp = new StringResponse(); 
 			RequestCycle requestCycle = RequestCycle.get();
 			Response origResponse = requestCycle.getResponse();
 			try
 			{
 				requestCycle.setResponse(resp);
 				for (int i=0; i < components.length; i++)
 				{
 					//	resp.write("<component cssid=''><![CDATA[");
 					
 					Component component = components[i];
 					boolean renderBodyOnly = component.getRenderBodyOnly();
 					try
 					{
 						component.setRenderBodyOnly(true);
 
 						component.doRender();
 					}
 					catch (Exception ex)
 					{
 						resp.write(ex.toString());
 					}
 					finally
 					{
 						component.setRenderBodyOnly(renderBodyOnly);
 					}
 				//	resp.write("]]<component>");
 				}
 			}
 			finally
 			{
 				requestCycle.setResponse(origResponse);
 			}
 			response.append(resp.toString());
 		}
 		//response.append("</components>");
 		//System.out.println("response: " + response.asString());
 		return response;
 		
 	}
 	/** 
 	 * adds reference to autoupdate.js.
 	 * @see wicket.AjaxHandler#renderHeadContribution(wicket.markup.html.HtmlHeaderContainer)
 	 */
 	protected void renderHeadContribution(HtmlHeaderContainer container)
 	{
 		addJsReference(container, new PackageResourceReference(Application.get(),
 				DojoAutoUpdateHandler.class, "autoupdate.js"));
 		
 	}
 	
 	/* (non-Javadoc)
 	 * @see wicket.AjaxHandler#getResponse()
 	 */
 	protected IResourceStream getResponse() 
 	{
 		Component[] components = new Component[1];
 		components[0] = getComponent();
 		
 		((IUpdatable)components[0]).update();
 						
 		return render(components);
 	}
 	
 	/**
 	 * adds the onload contribution: calls checkUpdate() <br/>
 	 * and calls checkUpdate() in a set interval.
 	 * @see wicket.AjaxHandler#getBodyOnloadContribution()
 	 */
 	protected String getBodyOnloadContribution()
 	{
		return "checkUpdate('" + getCallbackUrl() + "','text/plain', '" + getComponent().getId() + "');intervalCheck("+ this.interval + ", '" + getCallbackUrl() + "', 'text/html','" + HtmlId + "');";
 		
 	}
 	
 	
 	
 	/**
 	 * Checks if bound component is Updatable and adds HTMLID
 	 * @see wicket.AjaxHandler#onBind()
 	 */
 	protected void onBind()
 	{
 		Component c = getComponent();
 		if (!(c instanceof IUpdatable))
 		{
 			throw new WicketRuntimeException("This handler must be bound to Updatable Components.");
 		}
 		// create a unique HTML for the explode component
 		HtmlId = c.getId() + "_" + c.getPath();
 		// Add ID to component, and bind effect to trigger
 		c.add(new AttributeModifier("id", true, new Model(HtmlId)));
 
 	}
     
 }
