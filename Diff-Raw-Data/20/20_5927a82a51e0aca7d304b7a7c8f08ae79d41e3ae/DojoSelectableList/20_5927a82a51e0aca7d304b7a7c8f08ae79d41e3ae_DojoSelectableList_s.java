 package wicket.contrib.dojo.html.list.table;
 
 
 import java.util.List;
 
 import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.list.ListItem;
 import wicket.markup.html.list.ListView;
 import wicket.model.IModel;
 
 public abstract class DojoSelectableList extends  ListView
 {

 	DojoSelectableListContainer container;
 	
 	public DojoSelectableList(String id, IModel model, DojoSelectableListContainer container)
 	{
 		super(id, model);
 		//we add here behavior to the parent
 		container.add(new DojoSelectableListContainerHandler(this));
 		this.container = container;
 	}
 
 	public DojoSelectableList(String id, List list, DojoSelectableListContainer container)
 	{
 		super(id, list);
 		//we add here behavior to the parent
 		container.add(new DojoSelectableListContainerHandler(this));
 		this.container = container;
 	}
 
 	public DojoSelectableList(String id, DojoSelectableListContainer container)
 	{
 		super(id);
 		//we add here behavior to the parent
 		container.add(new DojoSelectableListContainerHandler(this));
 		this.container = container;
 	}
 	
 	protected void onBeforeRender()
 	{
 		super.onBeforeRender();
 		if (!"tr".equals(getMarkupStream().getTag().getName())){
 			throw new WicketRuntimeException("Tag name for a DojoSelectableList should be 'tr'");
 		}
 	}
 	
 	/**
 	 * Will be used to add Behavior to the parent (see onBeforeRender)
 	 * @return the parent container
 	 */
 	private DojoSelectableListContainer getParentContainer(){
 		return this.container;
 	}

	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
	}


	protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		// TODO Auto-generated method stub
		super.onComponentTagBody(markupStream, openTag);
	}
 
 
 
 }
