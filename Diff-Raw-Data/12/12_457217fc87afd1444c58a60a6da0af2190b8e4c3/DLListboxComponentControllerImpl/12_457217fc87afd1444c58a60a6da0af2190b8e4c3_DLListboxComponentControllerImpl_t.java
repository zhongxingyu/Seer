 package cz.datalite.zk.components.list.controller.impl;
 
 import cz.datalite.dao.DLSortType;
 import cz.datalite.helpers.ZKBinderHelper;
 import cz.datalite.zk.components.list.controller.DLListboxComponentController;
 import cz.datalite.zk.components.list.controller.DLListboxExtController;
 import cz.datalite.zk.components.list.model.DLColumnModel;
 import cz.datalite.zk.components.list.model.DLColumnUnitModel;
 import cz.datalite.zk.components.list.view.DLListbox;
 import cz.datalite.zk.components.list.view.DLListheader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.ComponentNotFoundException;
 import org.zkoss.zk.ui.UiException;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.util.Composer;
 import org.zkoss.zul.Listcell;
 import org.zkoss.zul.Listhead;
 import org.zkoss.zul.Listitem;
 
 /**
  *  Implementation of the listbox controller.
  * @param <T> master entity type
  * @author Karel Čemus <cemus@datalite.cz>
  */
 public class DLListboxComponentControllerImpl<T> implements DLListboxComponentController<T> {
 
     // master controller
     protected final DLListboxExtController masterController;
     // model
     /** data model */
     protected List<T> listboxModel;
     /** listheader models */
     protected final DLColumnModel columnModel;
     /** listheader - listheader model map */
     protected final Map<DLListheader, DLColumnUnitModel> columnMap = new HashMap<DLListheader, DLColumnUnitModel>();
     /** renderer template for changing when order of the column is changed */
     protected Listitem renderTemplate;
     /** map model - cell in the renderer because of breaking and creating relations */
     protected final Map<DLColumnUnitModel, Listcell> rendererCellTemplates = new HashMap<DLColumnUnitModel, Listcell>();
     /** map listheader - model for changing cols order model */
     protected final Map<DLColumnUnitModel, DLListheader> listheaderTemplates = new HashMap<DLColumnUnitModel, DLListheader>();
     /** selected item */
     protected T selectedItem;
     // default model
     protected final List<DLListheader> defaultHeaders = new LinkedList<DLListheader>();
     protected final List<Listcell> defaultRendererCellTemplates = new LinkedList<Listcell>();
     // view
     protected final DLListbox listbox;
 
     @SuppressWarnings( "unchecked" )
     public DLListboxComponentControllerImpl( final DLListboxExtController masterController, final DLColumnModel columnModel, final DLListbox listbox, final boolean autoinit ) {
         this.masterController = masterController;
         this.columnModel = columnModel;
         this.listbox = listbox;
         listbox.setController( this );
 
         // on select updates model
         listbox.addEventListener( Events.ON_SELECT, new org.zkoss.zk.ui.event.EventListener() {
 
             public void onEvent( final org.zkoss.zk.ui.event.Event event ) throws Exception {
                 if ( listbox.getSelectedIndex() == -1 ) {
                     selectedItem = null;
                 } else {
                     selectedItem = listboxModel.get( listbox.getSelectedIndex() );
                 }
                 masterController.onSelect();
             }
         } );
 
         // updates filter if onEasyFilter is captured
         listbox.addEventListener( "onEasyFilter", new org.zkoss.zk.ui.event.EventListener() {
 
             public void onEvent( final org.zkoss.zk.ui.event.Event event ) {
                 masterController.getEasyFilterController().onEasyFilter();
             }
         } );
 
         // updates filter if onEasyFilterClear is captured
         listbox.addEventListener( "onEasyFilterClear", new org.zkoss.zk.ui.event.EventListener() {
 
             public void onEvent( final org.zkoss.zk.ui.event.Event event ) {
                 masterController.getEasyFilterController().onClearEasyFilter( true );
             }
         } );
 
         defaultHeaders.addAll( listbox.getListheaders() );
         initListheaderModels( listbox.getListheaders(), autoinit );
     }
 
     public void onSort( final DLListheader listheader ) {
         assert listheader != null : "Listheader cannot be null. Invalid argument for sort.";
 
         final DLColumnUnitModel model = columnMap.get( listheader );
 
         if ( !model.isSortable() ) {
             return;
         }
 
         // if this column isn't sorted then remove all sorts from other columns
         // it allows only one sorted column
         //------
         // if someone would like allow multiple soirted column generated from user click
         // you have to comment this rows
         //------
         // this model is cleared everytime when the sorttype is changed
         final DLSortType sortType = model.getSortType();
         columnModel.clearSortAll();
 
         // if it is database sort there are 3 kinds of the sorts
         // else it is zk sort so there is only 2 types
         if ( model.isSortZk() ) {
             model.setSortType( sortType.nextZK() );
         } else {
             model.setSortType( sortType.next() );
         }
 
         onSort( model.isSortZk(), model.getSortType(), listheader );
         fireChanges();
     }
 
     /**
      * Perform sort
      * @param zkSort is it zk sort or not
      * @param sortType type of sorting - ascending, descending, natural
      * @param listheader sorted column
      */
     protected void onSort( final boolean zkSort, final DLSortType sortType, final DLListheader listheader ) {
         if ( masterController.isLocked() ) {
             return;
         }
 
         if ( zkSort ) {
             final DLColumnUnitModel model = columnMap.get( listheader );
             model.setSortType( sortType.nextZK() );
             switch ( sortType ) {
                 case ASCENDING:
                     listheader.sort( true );
                     break;
                 case DESCENDING:
                     listheader.sort( false );
                     break;
                 default:
                     throw new UnsupportedOperationException( "Unknown sortType." );
             }
             model.setSortType( model.getSortType().nextZK() );
         } else {
             masterController.onSortChange();
         }
     }
 
     public void onCreate() {
         masterController.onCreate();
     }
 
     public void fireChanges() {
         for ( DLListheader header : listbox.getListheaders() ) {
             header.fireChanges();
         }
     }
 
     public void fireDataChanges() {
         if ( listboxModel != null ) {
             listbox.setListModel( listboxModel );
 
             // if selected item is null or not in new list and we should select first row
             if ( listbox.isSelectFirstRow() && !listboxModel.contains( getSelectedItem() ) && listboxModel.size() > 0 ) {
                 setSelectedItem( listboxModel.get( 0 ) );
             }
         }
 
     }
 
     public void fireColumnModelChanges() {
         initListheaderModels( defaultHeaders, true );
         initRendererTemplate( defaultHeaders, defaultRendererCellTemplates );
     }
 
     protected void initListheaderModels( final List<DLListheader> headers, final boolean autoinit ) {
         columnMap.clear();
         // loading listheades
         int i = 0;
         for ( DLListheader header : headers ) {
             columnMap.put( header, columnModel.getColumnModel( ++i ) );
             header.setModel( columnMap.get( header ) );
             header.setController( this );
             if ( autoinit ) {
                 header.initModel();
             }
         }
     }
 
     public void refreshBindingAll() {
         ZKBinderHelper.getBinder( listbox ).loadAll();
     }
 
     @SuppressWarnings( "unchecked" )
     public void fireOrderChanges() {
 
         final Listhead listhead = listbox.getListhead();
 
         if ( renderTemplate == null ) {
             throw new UiException( "Template is not set. Is there data binding set on the component correctly?" );
         }
 
         // remove all children
         renderTemplate.getChildren().clear();
 
         // remove all children
         listhead.getChildren().clear();
 
         // add children to template and listhead according the model
         List<DLColumnUnitModel> orderedModel = new LinkedList(columnModel.getColumnModels());
         Collections.sort(orderedModel);
 
         for ( DLColumnUnitModel unit : orderedModel ) {
             if ( unit.isVisible() ) {
                renderTemplate.appendChild( rendererCellTemplates.get( unit ) ); // FIXME JB REV MP BUG 1 different column order
                 listhead.appendChild( listheaderTemplates.get( unit ) );
             }
         }
 
         // clear rendered items
         listbox.getItems().clear();
         listbox.invalidate(); // FIXME check if really required
 
         fireChanges();
 
     }
 
     public void setListboxModel( final List<T> model ) {
         this.listboxModel = model;
     }
 
     @SuppressWarnings( "unchecked" )
     public void setRendererTemplate( final Listitem item ) {
         renderTemplate = listbox.getItemAtIndex( 0 );
         defaultRendererCellTemplates.addAll( renderTemplate.getChildren() );
         initRendererTemplate( listbox.getListheaders(), defaultRendererCellTemplates );
     }
 
     protected void initRendererTemplate( final List<DLListheader> headers, final List<Listcell> listcells ) {
         // loading renderer templates and column name from the binding.
         // ---------------
         // if column name was already loaded will be now skipped.
         // ---------------
         // if column isn't loaded from the listheader nor from binding
         // this column will be very handicapped
         rendererCellTemplates.clear();
         listheaderTemplates.clear();
         int i = 0;
         for ( Listcell cell : listcells ) {
             final DLListheader header = headers.get( i++ );
             rendererCellTemplates.put( columnMap.get( header ), cell );
             listheaderTemplates.put( columnMap.get( header ), header );
             final String bindingText = ZKBinderHelper.getDefaultAnnotation( cell, "label", "value" );
             final String converter = ZKBinderHelper.getDefaultAnnotation( cell, "label", "converter" );
             if ( columnMap.get( header ).getColumn() == null && bindingText != null ) // set column from binding
             {
                 if ( bindingText.indexOf( '.' ) == -1 ) { // if there is whole entity without any property
                     columnMap.get( header ).setColumn( null );
                 } else { // if there is define some property
                     columnMap.get( header ).setColumn( bindingText.substring( bindingText.indexOf( '.' ) + 1 ) );
                 }
             }
             if ( !columnMap.get( header ).isConverter() && converter != null ) // set converter from binding
             {
                 columnMap.get( header ).setConverter( converter, header );
             }
             if ( columnMap.get( header ).getColumnType() == null && columnMap.get( header ).isColumn() ) // set type if it is not explicitly setted
             {
                 columnMap.get( header ).setColumnType( getFieldType( masterController.getEntityClass(), columnMap.get( header ).getColumn() ) );
             }
         }
     }
 
     /**
      * Returns data type of the fild according to the main entity and address
      * @param cls class with entity
      * @param key address
      * @return type
      */
     protected static Class getFieldType( final Class cls, final String key ) {
         if ( key.length() == 0 ) {
             return cls;
         }
         final int index = key.indexOf( '.' );
         final String name = index == -1 ? key : key.substring( 0, index );
         final String newKey = index == -1 ? "" : key.substring( index + 1 );
         for ( java.lang.reflect.Field field : cls.getDeclaredFields() ) {
             if ( field.getName().equals( name ) ) {
                 return getFieldType( field.getType(), newKey );
             }
         }
         return null;
     }
 
     public T getSelectedItem() {
         return selectedItem;
     }
 
     public void setSelectedItem( final T selectedItem ) {
         if ( masterController.isLocked() ) {
             return;
         }
         this.selectedItem = selectedItem;
         listbox.setSelectedIndex( getSelectedIndex(), false );
     }
 
     public int getSelectedIndex() {
         return listboxModel.indexOf( selectedItem );
     }
 
     public void setSelectedIndex( final int selectedIndex ) {
         if ( listboxModel == null ) {
             return;
         }
         if ( listboxModel.size() <= selectedIndex || selectedIndex < 0 ) {
             setSelectedItem( null );
         } else {
             setSelectedItem( listboxModel.get( selectedIndex ) );
         }
     }
 
     public Component getFellow( final String compId ) {
         if ( listbox.getFellow( compId ) == null ) {
             throw new ComponentNotFoundException( "Component " + compId + " not found" );
         }
         return listbox.getFellow( compId );
     }
 
     public void addForward( final String originEvent, final String targetComponent, final String targetEvent ) {
         addForward( originEvent, getFellow( targetComponent ), targetEvent );
     }
 
     public void addForward( final String originalEvent, final Component target, final String targetEvent ) {
         target.addForward( originalEvent, listbox, targetEvent );
     }
 
     public Composer getWindowCtl() {
         return ( Composer ) listbox.getVariable( "ctl", true );
     }
 
     public void focus() {
         listbox.focus();
     }
 
     public boolean isLoadDataOnCreate() {
         return listbox.isLoadDataOnCreate();
     }
 
     public DLListbox getListbox() {
         return listbox;
     }
 
     public String getUuidsForTest() {
         final StringBuilder builder = new StringBuilder( 40 );
         builder.append( listbox.getUuid() ).append( ',' );
         builder.append( masterController.getPagingUuid() );
         builder.append( ',' );
         builder.append( masterController.getQuickFilterUuid() );
         return builder.toString();
     }
 }
