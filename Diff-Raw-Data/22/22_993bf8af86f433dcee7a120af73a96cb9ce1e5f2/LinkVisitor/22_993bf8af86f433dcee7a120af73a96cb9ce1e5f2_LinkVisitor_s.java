/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 
 package iniconfigurationmanager;
 
import iniconfigurationmanager.items.ConfigItem;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  *
  * @author Ondrej Klejch <ondrej.klejch@gmail.com>
  */
 public class LinkVisitor<T> {
 
    private T type;

    private HashSet< ConfigItem > enteredConfigItems;
 
     private List< T > values;
 
    public LinkVisitor( T type ) {
        this.type = type;
         this.values = new LinkedList< T >();
     }
 
 
    public void enter( ConfigItem item ) {
         if( enteredConfigItems.contains( item ) ) {
             //@TODO throw new exception
         }
 
         enteredConfigItems.add( item );
     }
 
     
     public void visit( Object value ) {
         values.add( (T) value);
     }
 
     
    public void leave( ConfigItem item ) {
         enteredConfigItems.remove( item );
     }
 
 
     public List<T> getValues() {
         return values;
     }
 
 }
