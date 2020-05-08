 package wicket.contrib.dojo.html.list.table;
 
 
 import java.util.List;
 
 import wicket.WicketRuntimeException;
 import wicket.markup.html.list.ListView;
 import wicket.model.IModel;
 
 /**
  * A selectable list with listener on click and dbl click to add to the parent see {@link DojoSelectableListContainer}
  * @author vincent demay
  *
  */
public abstract class DojoSelectableList extends ListView
 {
 	
 	DojoSelectableListContainer container;
 	
 	/**
 	 * Construct the list
 	 * @param id list id
 	 * @param model model associated with the list
 	 */
 	public DojoSelectableList(String id, IModel model)
 	{
 		super(id, model);
 	}
 
 	/**
 	 * Construct the list
 	 * @param id list id
 	 * @param list model associated with the list
 	 */
 	public DojoSelectableList(String id, List list)
 	{
 		super(id, list);
 	}
 
 	/**
 	 * Construct the list
 	 * @param id list id
 	 */
 	public DojoSelectableList(String id)
 	{
 		super(id);
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
 
 	protected void onAttach()
 	{
 		super.onAttach();
 		//we add here behavior to the parent
 		this.container = (DojoSelectableListContainer)getParent();
 		this.container.setListView(this);
 		container.add(new DojoSelectableListContainerHandler(this));
 	}
 }
