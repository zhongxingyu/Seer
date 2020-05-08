 
 package ApplicationLogic;
 
 import static ApplicationLogic.ItemWrapper.*;
 
 import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.Set;
 
 import DatabaseFrontend.Item;
 import DatabaseFrontend.Permission;
 import DatabaseFrontend.User;
 
 public class ItemControl {
 	
 	public final Control p;
 	
 	
 	public ItemControl(Control p) {
 		this.p = p;
 		
 	}
 
 	public ItemWrapper newItem(long buyNowPriceInCents, String description) {
 		try{
 		UserWrapper _wuser = p.accountControl.getLoggedInUser();
 		if(!_wuser.isAllowed(Permission.MakeItem))
 			return null;
		User _user = _wuser.getUser();
 		Item retitem = Item.makeItem(buyNowPriceInCents, 0, _user, description);
 		if (retitem == null)
 			return null;
 		return (new ItemWrapper(retitem,p));
 		}
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 		
 		
 	}
 
 	public Set<ReadonlyItemWrapper> wrapItemsReadOnly(Set<Item> items) {
 		Set<ReadonlyItemWrapper> w = new HashSet();
 		for (Item i : items)
 			w.add(wrapReadOnly(i));
 		return w;
 	}
 
 	ReadonlyItemWrapper wrapReadOnly(Item i) {
 		return new ReadonlyItemWrapper(i, this.p);
 	}
 }
