 package uk.ac.starlink.vo;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import uk.ac.starlink.util.gui.ArrayTableColumn;
 import uk.ac.starlink.util.gui.ArrayTableModel;
 
 /**
  * TableModel in which each row represents a {@link RegResource}.
  *
  * @author   Mark Taylor
  * @since    18 Dec 2008
  */
 public class ResourceTableModel extends ArrayTableModel {
 
     /**
      * Constructs a ResourceTableModel with no AccessRef column.
      */
     public ResourceTableModel() {
         this( false );
     }
 
     /**
      * Constructs a ResourceTableModel with an optional AccessRef column.
      * This is a bit problematic - there is not a formal 1:1 relationship
      * between RegResources, which is what are displayed per-row in
      * this table, and RegCapabilityInterfaces, which are what host
      * AccessRefs (a.k.a. Service URLs).  In many cases however, the
      * relationship is in fact 1:1.
      * If <code>includeAcref</code> is set true, a column is added for
      * this information, and it's populated only in the cases where
      * a 1:1 relationship does actually hold.
      *
      * @param   includeAcref  true if the access ref column is to be included
      */
     public ResourceTableModel( boolean includeAcref ) {
         super();
         List colList = new ArrayList( Arrays.asList( new ArrayTableColumn[] {
             new ArrayTableColumn( "shortName", String.class ) {
                 public Object getValue( Object item ) {
                     return getResource( item ).getShortName();
                 }
             },
             new ArrayTableColumn( "title", String.class ) {
                 public Object getValue( Object item ) {
                     return getResource( item ).getTitle();
                 }
             },
             new ArrayTableColumn( "identifier", String.class ) {
                 public Object getValue( Object item ) {
                     return getResource( item ).getIdentifier();
                 }
             },
             new ArrayTableColumn( "publisher", String.class ) {
                 public Object getValue( Object item ) {
                    return getResource( item ).getIdentifier();
                 }
             },
             new ArrayTableColumn( "contact", String.class ) {
                 public Object getValue( Object item ) {
                     return getResource( item ).getContact();
                 }
             },
             new ArrayTableColumn( "refURL", String.class ) {
                 public Object getValue( Object item ) {
                     return getResource( item ).getReferenceUrl();
                 }
             },
         } ) );
         if ( includeAcref ) {
             colList.add( new ArrayTableColumn( "soleAccessURL", String.class ) {
                 public Object getValue( Object item ) {
                     RegCapabilityInterface[] caps =
                         getResource( item ).getCapabilities();
                     return ( caps != null && caps.length == 1 )
                          ? caps[ 0 ].getAccessUrl()
                          : null;
                 }
             } );
         }
         setColumns( (ArrayTableColumn[])
                     colList.toArray( new ArrayTableColumn[ 0 ] ) );
         setItems( new RegResource[ 0 ] );
     }
 
     /**
      * Sets the data for this table.
      *
      * @param  resources   resource array
      */
     public void setResources( RegResource[] resources ) {
         super.setItems( resources );
     }
 
     /**
      * Returns the data array for this table.
      *
      * @return  resource array
      */
     public RegResource[] getResources() {
         return (RegResource[]) super.getItems();
     }
 
     /**
      * Returns the RegResource object corresponding to a given row item.
      *
      * @param  row data object
      * @return  resource
      */
     private RegResource getResource( Object item ) {
         return (RegResource) item;
     }
 }
