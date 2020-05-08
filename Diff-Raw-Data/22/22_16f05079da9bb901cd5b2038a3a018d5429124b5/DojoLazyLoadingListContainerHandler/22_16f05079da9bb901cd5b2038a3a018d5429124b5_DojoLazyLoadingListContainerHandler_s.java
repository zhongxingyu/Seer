 package wicket.contrib.dojo.html.list.lazy;
 
 import wicket.ResourceReference;
 import wicket.ajax.AjaxRequestTarget;
 import wicket.contrib.dojo.AbstractDefaultDojoBehavior;
 import wicket.contrib.dojo.AbstractRequireDojoBehavior;
 import wicket.markup.ComponentTag;
 import wicket.markup.html.IHeaderResponse;
 
 /**
  * @author Vincent Demay
  *
  */
 public class DojoLazyLoadingListContainerHandler extends AbstractRequireDojoBehavior
 {
 	/**
 	 * 
 	 */
 	public void setRequire(RequireDojoLibs libs)
 	{
 		//DO Nothing, the Widget is in the package
 	}
 
 	@Override
 	protected final void respond(AjaxRequestTarget target)
 	{
 		int first = Integer.parseInt(getComponent().getRequest().getParameter("start"));
 		int end = Integer.parseInt(getComponent().getRequest().getParameter("end"));
 		int count = end - first;
 		
 		DojoLazyLoadingRefreshingView child = ((DojoLazyLoadingRefreshingView)((DojoLazyLoadingListContainer)getComponent()).getChild());
 		child.setFirst(first);
 		child.setCount(count);
 		target.addComponent(child);
 		
		int i = 0;
		while (i < 1000000000){i++;}
		
 		target.prependJavascript("dojo.widget.byId(\"" + getComponent().getMarkupId() + "\").contentTable.getElementsByTagName('tbody')[0].id='" + child.getMarkupId() + "'");
 		target.appendJavascript("dojo.widget.byId('" + getComponent().getMarkupId() + "').postUpdate()");
 	}
 
 	@Override
 	public void renderHead(IHeaderResponse response)
 	{
 		super.renderHead(response);
 		response.renderJavascriptReference(new ResourceReference(DojoLazyLoadingListContainerHandler.class, "LazyTable.js"));
 		response.renderCSSReference(new ResourceReference(DojoLazyLoadingListContainerHandler.class, "LazyLoadingTable.css"));
 	}
 
 	@Override
 	protected void onComponentTag(ComponentTag tag)
 	{
 		super.onComponentTag(tag);
 		tag.put("onReload", "" + getPreCallbackScript() + getCallbackScript());
 	}
 	
 	protected CharSequence getPreCallbackScript(){
 		return "";//"this.contentTable.innerHTML='<tbody id=" + getComponent().getMarkupId() + "_contentItems><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr><tr><td></td></tr></tbody>';";
 	}
 	
 	
 	/**
 	 * @return javascript that will generate an ajax GET request to this
 	 *         behavior *
 	 * @param recordPageVersion
 	 *            if true the url will be encoded to execute on the current page
 	 *            version, otherwise url will be encoded to execute on the
 	 *            latest page version
 	 */
 	protected CharSequence getCallbackScript(boolean recordPageVersion)
 	{
 		return getCallbackScript("wicketAjaxGet('" + getCallbackUrl(recordPageVersion) + "&start=' + arguments[0] + '&end=' +  arguments[1]", null,
 				null);
 	}
 }
