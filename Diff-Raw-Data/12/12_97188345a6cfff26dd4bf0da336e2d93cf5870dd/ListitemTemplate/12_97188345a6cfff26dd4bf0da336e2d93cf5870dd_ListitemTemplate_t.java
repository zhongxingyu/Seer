 package cz.datalite.zk.components.lovbox;
 
 import cz.datalite.zk.bind.ZKBinderHelper;
 import java.util.HashMap;
 import java.util.Map;
 import org.zkoss.xel.VariableResolver;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.util.Composer;
 import org.zkoss.zk.ui.util.Template;
 import org.zkoss.zul.Listcell;
 import org.zkoss.zul.Listitem;
 
 /**
  * Simple template for lovboxes without specified inner listbox.
  * 
  * @author Karel Cemus <cemus@datalite.cz>
  */
 public class ListitemTemplate implements Template {
 
     private static final String PREFIX = "item.";
 
     private final String[] labels;
 
     private final String description;
 
     public ListitemTemplate( String[] labels ) {
         this( labels, null );
     }
 
     public ListitemTemplate( String[] labels, String description ) {
         this.labels = labels;
         this.description = description == null ? null : description;
     }
 
     public Component[] create( Component parent, Component insertBefore, VariableResolver resolver, Composer composer ) {
 
         //create template components & add binding expressions
         Listitem listitem = new Listitem();
         // append child
         if ( insertBefore == null ) parent.appendChild( listitem );
         else parent.insertBefore( listitem, insertBefore );
 
         // for each label column define the cell
         for ( final String property : labels ) {
            createCell( listitem, property );
         }
 
        if ( description != null ) createCell( listitem, description );
 
         return new Component[]{ listitem };
     }
 
     private void createCell( Component parent, String binding ) {
         final Listcell cell = new Listcell();
         parent.appendChild( cell );
         ZKBinderHelper.registerAnnotation( cell, "label", "load", PREFIX + binding );
         assert !cell.getAnnotations( "label" ).isEmpty() : "Annotation was not registered successfully";
     }
 
     public Map<String, Object> getParameters() {
 
         Map<String, Object> parameters = new HashMap<String, Object>();
         //set binding variable
         parameters.put( "var", "item" );
 
         return parameters;
     }
 }
