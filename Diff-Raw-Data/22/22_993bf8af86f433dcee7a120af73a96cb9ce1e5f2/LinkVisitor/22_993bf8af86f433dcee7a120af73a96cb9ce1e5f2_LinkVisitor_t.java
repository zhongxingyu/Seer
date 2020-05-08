 
 package iniconfigurationmanager;
 
import iniconfigurationmanager.schema.ConfigItemData;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  *
  * @author Ondrej Klejch <ondrej.klejch@gmail.com>
  */
 public class LinkVisitor<T> {
 
    private HashSet< ConfigItemData > enteredConfigItems;
 
     private List< T > values;
 
    public LinkVisitor() {
         this.values = new LinkedList< T >();
     }
 
 
    public void enter( ConfigItemData item ) {
         if( enteredConfigItems.contains( item ) ) {
             //@TODO throw new exception
         }
 
         enteredConfigItems.add( item );
     }
 
     
     public void visit( Object value ) {
         values.add( (T) value);
     }
 
     
    public void leave( ConfigItemData item ) {
         enteredConfigItems.remove( item );
     }
 
 
     public List<T> getValues() {
         return values;
     }
 
 }
