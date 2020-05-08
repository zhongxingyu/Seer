 package wicket.contrib.dojo.html.list.lazy;
 
 
 import static wicket.contrib.dojo.DojoIdConstants.DOJO_TYPE;
 import static wicket.contrib.dojo.DojoIdConstants.DOJO_TYPE_LAZYTABLE;
 import wicket.Component;
 import wicket.MarkupContainer;
 import wicket.ResourceReference;
 import wicket.WicketRuntimeException;
 import wicket.markup.ComponentTag;
 import wicket.markup.html.WebMarkupContainer;
 import wicket.model.IModel;
 
 /**
  * A lazy loading Repeating view
  * @author vincent demay
  *
  */
 public class DojoLazyLoadingListContainer extends  WebMarkupContainer
 {
	
	private int max;
 
	public DojoLazyLoadingListContainer(MarkupContainer parent, String id, IModel model, int maxItem)
 	{
 		super(parent, id, model);
 		add(new DojoLazyLoadingListContainerHandler());
		max = maxItem;
 	}
 
	public DojoLazyLoadingListContainer(MarkupContainer parent, String id, int maxItem)
 	{
 		super(parent, id);
 		add(new DojoLazyLoadingListContainerHandler());
		max = maxItem;
 	}
 	
 	@Override
 	protected void onComponentTag(ComponentTag tag)
 	{
 		super.onComponentTag(tag);
 		tag.put(DOJO_TYPE, DOJO_TYPE_LAZYTABLE);
 		tag.put("templatePath", urlFor(new ResourceReference(DojoLazyLoadingListContainerHandler.class, "LazyTable.htm")));
		tag.put("maxKnewItem","" + max);
 	}
 	
 	/**
 	 * Find the list view in children
 	 * if none or more than one throw an exception!
 	 * 
 	 * @return the child ListView of this container
 	 */
 	public Component getChild()
 	{
 		ChildFinder visitor = new ChildFinder();
 		visitChildren(visitor);
 		return visitor.getChild();
 	}
 	
 	
 	/**
 	 * Check for child - retreive a 
 	 *   * DojoLazyLoadingRefreshingView
 	 * @author Vincent Demay
 	 *
 	 */
 	private class ChildFinder implements IVisitor{
 		private Component component = null;
 		private int componentNumber = 0;
 		
 		public Object component(Component component)
 		{
 			if (component instanceof DojoLazyLoadingRefreshingView){
 				this.component = component;
 				componentNumber ++;
 			}
 			return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
 		}
 		
 		public Component getChild(){
 			if (componentNumber != 1 ){
 				throw new WicketRuntimeException("A DojoLazyLoadingListContainer should contain exactly one DojoLazyLoadingRefreshingView as directly child");
 			}
 			return component;
 		}
 	}
 	
 	
 
 
 
 	
 }
