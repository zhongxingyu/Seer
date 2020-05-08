 package wicket.contrib.dojo.html.list;
 
 import java.util.List;
 
 import wicket.MarkupContainer;
 import wicket.ajax.AjaxRequestTarget;
 import wicket.behavior.AttributeAppender;
 import wicket.contrib.dojo.AbstractRequireDojoBehavior;
 import wicket.contrib.dojo.dojodnd.DojoDragContainerHandler;
 import wicket.markup.ComponentTag;
 import wicket.markup.html.IHeaderResponse;
 import wicket.markup.html.list.ListItem;
 import wicket.markup.html.list.ListView;
 import wicket.model.IModel;
 import wicket.model.Model;
 
 public abstract class DojoOrderableListView extends ListView
 {
 	DojoOrderableListViewContainer container;
 	int pos = 0; 
 	private String dragId;
 
 	public DojoOrderableListView(DojoOrderableListViewContainer parent, String id, IModel model)
 	{
 		super(parent, id, model);
 		container = parent;
 	}
 
 	public DojoOrderableListView(DojoOrderableListViewContainer parent, String id, List list)
 	{
 		super(parent, id, list);
 		container = parent;
 	}
 
 	public DojoOrderableListView(DojoOrderableListViewContainer parent, String id)
 	{
 		super(parent, id);
 		container = parent;
 	}
 	
 	public String generateId(){
 		return container.getMarkupId()+ "_list_" + (pos++);
 		
 	}
 
 	@Override
 	protected void onComponentTag(ComponentTag tag)
 	{
 		super.onComponentTag(tag);
 	}
 
 	@Override
 	protected void renderItem(ListItem item)
 	{
 		String id = generateId();
 		item.add(new AttributeAppender("id", true, new Model<String>(id),""));
		dragId = "*";
 		super.renderItem(item);
 	}
 
 	@Override
 	protected void populateItem(ListItem item)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void renderHead(IHeaderResponse response)
 	{
 		super.renderHead(response);
 		response.renderString(generateDragDefinition(container.getMarkupId()));
 
 	}
 	
 	private String generateDragDefinition(String id){
 		String toReturn = "";
 		toReturn += "<script language=\"JavaScript\" type=\"text/javascript\">\n";
 		toReturn += "function initDrag" + id + "(){\n";
 		toReturn += "	var children = document.getElementById('" + container.getMarkupId() + "').getElementsByTagName('div');\n";
 		toReturn += "	for(var i=0;  children.length > i ; i++){\n";
		toReturn += "		var drag = new dojo.dnd.HtmlDragSource(children[i], children[i].id);\n";
 		toReturn += "	}\n";
 		toReturn += "}\n";
 		toReturn += "dojo.event.connect(dojo, \"loaded\", \"initDrag" + id + "\");\n";
 		toReturn += "</script>\n";
 		return toReturn;
 	}
 
 }
