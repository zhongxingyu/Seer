 package wicket.contrib.dojo.html.list.table;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import wicket.ResourceReference;
 import wicket.ajax.AjaxRequestTarget;
 import wicket.contrib.dojo.AbstractRequireDojoBehavior;
 import wicket.markup.ComponentTag;
 import wicket.markup.html.IHeaderResponse;
 import wicket.markup.html.link.ILinkListener;
 
 /**
  * @author Vincent Demay
  *
  */
 public class DojoSelectableListContainerHandler extends AbstractRequireDojoBehavior
 {
 	
 	//child of this container
 	private DojoSelectableList listView;
 
 	/**
 	 * 
 	 * @param selectableList
 	 */
 	public DojoSelectableListContainerHandler(DojoSelectableList selectableList)
 	{
 		super();
 		listView = selectableList;
 	}
 
 	/**
 	 * 
 	 */
 	public void setRequire(RequireDojoLibs libs)
 	{
 		//DO Nothing, the Widget is in the package
 	}
 
 	/**
 	 * 
 	 */
 	protected final void respond(AjaxRequestTarget target)
 	{
 		ArrayList selected = new ArrayList();
 		String indexList[] = getComponent().getRequest().getParameters("select");
 		if (indexList == null){
 			if (((DojoSelectableListContainer)getComponent()).getSelected() != null){
 				((DojoSelectableListContainer)getComponent()).onChoose(target, ((DojoSelectableListContainer)getComponent()).getSelected().get(0));
 			}
 			else
 			{
 				((DojoSelectableListContainer)getComponent()).onChoose(target, null);
 			}
 		}else{
 			List all = listView.getList();
 			int pos;
 			for (int i=0; i < indexList.length; i++){
 				pos = Integer.parseInt(indexList[i]);
 				selected.add(all.get(all.size()- pos - 1));
 			}
 			
 			((DojoSelectableListContainer)getComponent()).setSelected(selected);
 			
 			((DojoSelectableListContainer)getComponent()).onSelection(target, selected);
 		}
 	}
 
 	/**
 	 * TODO find an other way to Render an as big javascript
 	 * TODO put it in js file
 	 */
 	public void renderHead(IHeaderResponse response)
 	{
 		super.renderHead(response);
 		response.renderCSSReference(new ResourceReference(DojoSelectableListContainer.class, "DojoSelectableListContainer.css"));
 		response.renderJavascriptReference(new ResourceReference(DojoSelectableListContainer.class, "SelectableTable.js"));
 		if (((DojoSelectableListContainer)getComponent()).getOverwriteCss() != null){
 			response.renderCSSReference(((DojoSelectableListContainer)getComponent()).getOverwriteCss());
 		}
 		
 		String toReturn="";
 		toReturn += "<script language=\"JavaScript\" type=\"text/javascript\">\n";
 		toReturn += "function getSelection(id){\n";
 		toReturn += "	var container = document.getElementById(id);\n";
 		toReturn += "	var body = container.getElementsByTagName('tbody')[0];\n";
 		toReturn += "	var rows=body.getElementsByTagName('tr')\n";
 		toReturn += "	var selection = '';\n";
 		toReturn += "	var index = 0;\n";
 		toReturn += "	for(var i=0; i<rows.length; i++){\n";
 		toReturn += "		if(rows[i].parentNode==body){\n";
 		toReturn += "			if(dojo.html.getAttribute(rows[i],'selected')=='true'){\n";
 		toReturn += "				selection += '&select=' + index;\n";
 		toReturn += "			}\n";
 		toReturn += "			index++;\n";
 		toReturn += "		}\n";
 		toReturn += "	}\n";
 		toReturn += "	return selection;\n";
 		toReturn += "};\n";
 		toReturn += "</script>\n";
 		
 		response.renderString(toReturn);
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
 		return getCallbackScript("wicketAjaxGet('" + super.getCallbackUrl(false, true) + "' + getSelection('"+getComponent().getMarkupId()+"')", null,
 				null);
 	}
 	
 	/**
 	 * return javascript that will be used to respond to Double click
 	 * @return javascript that will be used to respond to Double click
 	 */
 	protected final CharSequence getDoubleClickCallbackScripts(){
 		if (((DojoSelectableListContainer) getComponent()).isAjaxModeOnChoose()){
 			return getCallbackScript("wicketAjaxGet('" + super.getCallbackUrl(false, true) + "'", null,null);
 		}else{
 			CharSequence url = ((DojoSelectableListContainer) getComponent()).urlFor(ILinkListener.INTERFACE);
			return "window.location.href='" + url + "' + getSelection('"+getComponent().getMarkupId()+"')";
 		}
 	}
 
 	/**
 	 * Add onSelect and on choose event listener
 	 */
 	protected void onComponentTag(ComponentTag tag)
 	{
 		super.onComponentTag(tag);
 		tag.put("onSelect", getCallbackScript());
 		tag.put("onChoose", getDoubleClickCallbackScripts());
 	}
 
 	
 
 }
