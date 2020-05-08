 package wicket.contrib.dojo.markup.html.container.tab;
 
import wicket.MarkupContainer;
 import wicket.contrib.dojo.DojoIdConstants;
 import wicket.contrib.dojo.markup.html.container.AbstractDojoContainer;
 import wicket.markup.ComponentTag;
 
 /**
  * AbstractDojoContainer should be added 
  * @author Vincent Demay
  *
  */
 public class DojoTabContainer extends AbstractDojoContainer
 {
 	private AbstractDojoContainer selected;
 	
 	/**
 	 * Construct a DojoTabContainer
 	 * @param parent parent where the container will be added
 	 * @param id container id
 	 * @param title container title
 	 */
 	public DojoTabContainer(String id, String title)
 	{
 		super(id, title);
 		add(new DojoTabHandler());
 	}
 
 	/**
 	 * Construct a DojoTabContainer
 	 * @param parent parent where the container will be added
 	 * @param id container id
 	 */
 	public DojoTabContainer(String id)
 	{
 		this(id, null);
 	}
 
 	protected void onComponentTag(ComponentTag tag)
 	{
 		super.onComponentTag(tag);
 		tag.put(DojoIdConstants.DOJO_TYPE, DojoIdConstants.DOJO_TYPE_TABCONTAINER);
 		tag.put("label", getTitle());
 		tag.put("selectedChild", getSelectedTabId());
 	}
 
 	/**
 	 * Tab select by default
 	 * @param tab Tab select by default
 	 */
 	public void setSelectedTab(AbstractDojoContainer tab){
 		selected = tab;
 	}
 	
 	/**
 	 * return the current selected tab id
 	 * @return the current selected tab id
 	 */
 	public String getSelectedTabId(){
 		if (selected != null){
 			return selected.getMarkupId();
 		}
 		else return "";
 	}
 	
 	/**
 	 * return the current container selected in the tab container
 	 * @return the selected container in the tab container
 	 */
 	public AbstractDojoContainer getSelectedTab(){
 		return selected;
 	}
 
 	/**
 	 * Ovewrite this methos to handle clicks on tab
 	 * @param tab new tab selected
 	 */
 	public void onSelectTab(AbstractDojoContainer tab)
 	{
 				
 	}
 
 
 }
