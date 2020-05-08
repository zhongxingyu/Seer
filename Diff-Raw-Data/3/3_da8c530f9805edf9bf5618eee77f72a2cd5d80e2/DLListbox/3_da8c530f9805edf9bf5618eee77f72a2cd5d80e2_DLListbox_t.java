 package cz.datalite.zk.components.list.view;
 
 import cz.datalite.helpers.EqualsHelper;
 import cz.datalite.zk.bind.ZKBinderHelper;
 import cz.datalite.zk.components.list.DLListboxEvents;
 import cz.datalite.zk.components.list.controller.DLListboxComponentController;
 import java.util.Collections;
 import java.util.List;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zkoss.zk.ui.Execution;
 import org.zkoss.zk.ui.Executions;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zul.ListModel;
 import org.zkoss.zul.Listbox;
 import org.zkoss.zul.Listitem;
 import org.zkoss.zul.ListitemRenderer;
 import org.zkoss.zul.ext.Selectable;
 
 /**
  * This component is the extension for the ZK listbox. This component and
  * other utils allows user to use sorting, paging, filtering, exporting
  * and some other tools. This component supports MVC architecture.
  * @author Jiri Bubnik
  * @author Karel Čemus <cemus@datalite.cz>
  */
 public class DLListbox extends Listbox {
     
     /** request for direct export to MS Excel */
     public static final String ON_DIRECT_EXPORT = "onDirectExport";
     
     /** logger */
     protected static final Logger LOGGER = LoggerFactory.getLogger( DLListbox.class );
     
     protected DLListboxComponentController controller;
 
     @SuppressWarnings( "ResultOfObjectAllocationIgnored" )
     public DLListbox() {
         super();
         // init self-attaching onCreate event listener
         new OnCreateListener();
      }
 
 
     @Deprecated
     public void setListModel( final List model ) {
         setModel( new org.zkoss.zkplus.databind.BindingListModelList( model, true ) );
     }
 
     @Override
     public void setModel( final ListModel model ) {
 
         final Execution exec = Executions.getCurrent();
         exec.removeAttribute( "zkoss.Listbox.deferInitModel_" + getUuid() ); // TODO problems with ROD - Listbox#2482
 
         super.setModel( model );
 
          if ( getSelectedItem() == null ) {
             Events.postEvent( "onDeselect", this, null );
         }
     }
 
     @Override
     public void setSelectedIndex( int jsel ) {
         if ( !EqualsHelper.isEqualsNull( jsel, getSelectedIndex() ) && getModel().getSize() > jsel ) {
             super.setSelectedIndex( jsel );
             Events.postEvent( DLListboxEvents.ON_SELECTED_SHOW, this, null );
         }
     }
 
     /**
      * Sets selected item by value
      *
      * @param value hodnota itemu, podle ktere se vyhledava
      * @param select if is selected
      */
     public void selectItemByValue( final Object value, final boolean select ) {
         for ( Listitem item : ( List<Listitem> ) getItems() ) {
             if ( item.getValue() == null && value == null
                     || (value != null && value.equals( item.getValue() )) ) {
                 if ( select ) {
                     this.addItemToSelection( item );
                 } else {
                     this.removeItemFromSelection( item );
                 }
             }
         }
 
         // ZK bug - doesn't reload automatically
         if ( "select".equals( getMold() ) ) {
             invalidate();
         }
     }
 
     /**
      * Returns value of the selected item - equivalent to getSelectedItem().getValue()
      * @return value of the selected item or null if no item is selected
      */
     public Object getSelectedValue() {
         Listitem selected = getSelectedItem();
         if ( selected != null ) {
             return selected.getValue();
         }
         return null;
     }
 
     /**
      * Selects first for in the list if some exists
      * 
      * @return true if the first is selected, false if list is empty
      */
     public boolean selectFirstRow() {
         if ( getModel().getSize() > 0 ) {
             setSelectedIndex( 0 );
             Events.postEvent( Events.ON_SELECT, this, Collections.singleton( getModel().getElementAt( getSelectedIndex() ) ) );
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * should be automatically selected first row
      */
     private boolean selectFirstRow = true;
 
     /**
      * Should be selected first row automatically after insertion new model
      * @return the selectFirstRow
      */
     public boolean isSelectFirstRow() {
         return selectFirstRow;
     }
 
     /**
      * <p>Should be selected first row automatically after insertion new model</p>
      *
      * <p>In the classical M-V-C architecture this component is the View.
      * if user want to select something then he clicks on this component
      * and it emits the event.</p>
      *
      * <p>So if the model and controller thinks that there is selected
      * different value than which is in real then the event should be
      * emitted to update model.</p>
      *
      * <p>If there is no selectedItem and new model is set
      * then the first row is automaticly selected if it is enabled.
      * Because model and ctl expect that here is null which is not
      * then the onSelectEvent is emitted. Doesn't matter who did the
      * change if user or view component itself.</p>
      *
      * @param selectFirstRow the selectFirstRow to set
      */
     public void setSelectFirstRow( final boolean selectFirstRow ) {
         this.selectFirstRow = selectFirstRow;
     }
 
     public void setController( final DLListboxComponentController controller ) {
         assert controller != null : "Controller cannot be null.";
         this.controller = controller;
 
         // Hack of load on save only if used with controller (it needs to be set before binding init)
         addEventListener( "onLoadOnSave", new EventListener() {
 
             public void onEvent( final Event event ) {
                 //
                 // HACK!!!!
                 //
                 // @since 13.2.2011
                 // @author Jiri Bubnik
                 //
                 // ZK databinder registers own listener is no other is found.
                 // Load on Save is very buggy feature, we dont want to use it.
                 // There is on library property to disable it, so we disable it this way
             }
         } );
 
     }
 
     public DLListboxComponentController getController() {
         return controller;
     }
 
     @SuppressWarnings( "unchecked" )
     public List<DLListheader> getListheaders() {
         return ( List<DLListheader> ) ( List ) getListhead().getChildren();
     }
 
     @SuppressWarnings( "unchecked" )
     @Override
     public void setItemRenderer( final ListitemRenderer renderer ) {
         super.setItemRenderer( renderer );
         if ( controller != null ) {
             // it contains the one listheader and one listitem which is the template
             if ( ZKBinderHelper.version( this ) == 1 && getChildren().size() == 2 )
                 controller.setRendererTemplate( getItemAtIndex( 0 ) );
         }
     }
     
     /** flag determining wheather or not the execution is in the middle of onInitRender */
     private boolean onInitRender = false; 
 
     @Override
     public void onInitRender() {
         super.onInitRender();
 
         // controller doesn't support multiple select item. Setup selected index only if controller has selected item
         if ( controller != null && getModel().getSize() > 0 )
             if ( getModel() instanceof Selectable ) {
                 final Selectable model = ( Selectable ) getModel();
                 model.clearSelection();
                 for ( Object selected : controller.getSelectedItems() ) {
                     model.addToSelection( selected );
                 }
                 onInitRender = true;
                 Events.sendEvent( Events.ON_SELECT, this, controller.getSelectedItems() );
                 onInitRender = false;
             } else
                 LOGGER.warn( "Model wasn't recognized, the first row was not selected." );
         else
             Events.postEvent( DLListboxEvents.ON_SELECTED_HIDE, this, null );
     }
 
     public boolean isOnInitRender() {
         return onInitRender;
     }
 
     /** Should the page start with listbox filled with data */
     boolean loadDataOnCreate = true;
 
     /**
      * Should the page start with listbox filled with data .
      * @param set true/false (default is true)
      */
     public void setLoadDataOnCreate( boolean set ) {
         loadDataOnCreate = set;
     }
 
     /**
      * Should the page start with listbox filled with data.
      * @return true/false (default is true)
      */
     public boolean isLoadDataOnCreate() {
         return loadDataOnCreate;
     }
     
     /** package */ void updateListItem( Listitem item ) {
        if ( controller != null)
            controller.updateListItem( item );
     }
 
     class OnCreateListener implements EventListener<Event> {
 
         public OnCreateListener() {
             // register self as a on create listener
             DLListbox.this.addEventListener( Events.ON_CREATE, OnCreateListener.this );
         }
 
         public void onEvent( Event event ) throws Exception {
 
             // if the controller is defined, init it
             if ( controller != null ) controller.onCreate();
             
             // new data binding uses the special renderer
             if (ZKBinderHelper.version(DLListbox.this) == 2)
                 setItemRenderer( new DLListitemRenderer() );
         }
     }
 
 }
