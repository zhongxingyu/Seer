 package org.dyndns.schuschu.xmms2client.action.backend;
 
import java.lang.reflect.Array;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 
 import org.dyndns.schuschu.xmms2client.backend.FooBackendMedia;
 import org.dyndns.schuschu.xmms2client.interfaces.FooInterfaceClickable;
 
 import se.fnord.xmms2.client.Client;
 
 public class FooActionBackendMediaOrder extends FooActionBackendMedia {
 
 	public FooActionBackendMediaOrder(FooInterfaceClickable clickable,
 			FooBackendMedia backend, Client client) {
 		super(clickable, backend, client);
 	}
 
 	@Override
 	public void clicked() {
 		List<String> listCurrent = getBackend().getOrderBy();
 		StringBuffer buffer = new StringBuffer();
		for(String s : listCurrent){
			buffer.append(s+" ");
 		}
		buffer.deleteCharAt(buffer.length()-1);
 		String current = buffer.toString();
		
		String input = JOptionPane.showInputDialog("Please enter new Order:\n(i.e.: artist album title",current);
		
 		List<String> newOrder = Arrays.asList(input.split(" "));
						
 		if (input != null) {
 			getBackend().setOrderBy(newOrder);
			
			getBackend().getView().setSelection(new int[]{-1});
 			getBackend().refresh();
 			getBackend().generateFilteredContent();
 		}
 	}
 }
