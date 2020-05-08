 package wicket.contrib.dojo.html.list;
 
 import java.util.Collections;
 import java.util.List;
 
 import wicket.MarkupContainer;
 import wicket.ajax.AjaxRequestTarget;
 import wicket.contrib.dojo.dojodnd.DojoDragContainer;
 import wicket.contrib.dojo.dojodnd.DojoDropContainer;
 import wicket.markup.html.list.ListItem;
 
 
 /**
  * 
  * @author vdemay
  * TODO refactor me 
  */
 public class DojoOrderableListViewContainer extends DojoDropContainer
 {
 
 	public DojoOrderableListViewContainer(MarkupContainer parent, String id)
 	{
 		super(parent, id);
		setDropPattern(this.getMarkupId());
 	}
 
 
 	public void onDrop(DojoOrderableListView container,  int oldPosition, int newPosition, AjaxRequestTarget target)
 	{
 		if (oldPosition != newPosition){
 			List list = container.getList();
 			Object drag = list.remove(oldPosition);
 			list.add(newPosition, drag);
 			container.setList(list);
 			target.appendJavascript(getChangeIDScript());
 		}
 		onDrop(null, newPosition);
 	}
 	
 	private String getChangeIDScript(){
 		String containerId = getMarkupId();
 		String changeId = "";
 		changeId += "var children = document.getElementById('" + containerId + "').getElementsByTagName('div');\n";
 		changeId += "for(var i=0; children.length > i ; i++){\n";
 		changeId += "	children[i].id = '" + containerId + "_" + "list" + "_'+i\n";   //FIXME : replace List it is not very Generic
 		changeId += "}";
 		return changeId;
 		
 	}
 	
 	/**
 	 * Called after the model is updated. Use this method to e.g. update the
 	 * persistent model. Does nothing by default.
 	 */
 	protected void onAjaxModelUpdated(AjaxRequestTarget target)
 	{
 		String dragSource = getRequest().getParameter("dragSource");
 		int newPosition = Integer.parseInt(getRequest().getParameter("position"));
 		MarkupContainer container = getPage(); 
 		String[] ids = dragSource.split("_");
 		for (int i=0; i < ids.length - 1; i++){
 			container = (MarkupContainer)container.get(ids[i]);
 		}
 		int oldPosition = Integer.parseInt(ids[ids.length - 1]);
 		onDrop((DojoOrderableListView) container, oldPosition, newPosition, target);  
 	}
 
 	@Override
 	public void onDrop(DojoDragContainer container, int newPosition){}
 
 }
