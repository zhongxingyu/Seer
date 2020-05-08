 /**
  * 
  */
 package pl.dmcs.whatsupdoc.client.fields;
 
 import java.util.List;
 
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * 17-11-2012
  * @author Jakub Jeleński, jjelenski90@gmail.com
  * 
  * 
  */
 public class ListItemField extends Field {
 	
 	/**
 	 * @param l - list of widgets
 	 * @param css - list of css
 	 */
 	public ListItemField(List<Widget> l, List<String> css){
 		super(l.size(), css);
 		
 		for(int i=0;i<l.size();i++){
			System.out.println(subDiv.get(i).getStyleName());
 			subDiv.get(i).add(l.get(i));
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see pl.dmcs.whatsupdoc.client.fields.Field#getValue()
 	 */
 	@Override
 	public Object getValue() {
 		//Not implemented
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see pl.dmcs.whatsupdoc.client.fields.Field#clear()
 	 */
 	@Override
 	public void clear() {
 	}
 
 }
