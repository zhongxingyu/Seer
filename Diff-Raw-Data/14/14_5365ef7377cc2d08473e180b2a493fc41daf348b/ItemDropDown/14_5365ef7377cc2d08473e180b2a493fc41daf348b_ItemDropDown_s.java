 package david.robson.wistrap.base.nav;
 
 import java.util.List;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.CompoundPropertyModel;
 
 
 public class ItemDropDown extends Panel
 {
 
 	public ItemDropDown(String id, final MenuItem item) 
 	{
 		super(id);
 		
 		final MenuItemAsDropDown itemAsDropDown = (MenuItemAsDropDown) item;
 		
 		List<MenuItem> childItems = itemAsDropDown.getChildItems();
 		setRenderBodyOnly(true);
 		
 		
 		Label label = new Label("dropdownLabel", itemAsDropDown.getTitle());
 		label.setRenderBodyOnly(true);
 		add(label);
 		
 		
 		ListView<MenuItem> dropdownChildren = new ListView<MenuItem>("dropdownChildren", childItems)
 		{
 			protected void populateItem(final ListItem<MenuItem> childItem)
			{
				ItemAction component = (ItemAction) childItem.getModelObject().getComponent("childItem", childItem.getModelObject());
				
				
				childItem.add(component);
 			}
 		};
 		add(dropdownChildren);
 		
 	}
 
 }
