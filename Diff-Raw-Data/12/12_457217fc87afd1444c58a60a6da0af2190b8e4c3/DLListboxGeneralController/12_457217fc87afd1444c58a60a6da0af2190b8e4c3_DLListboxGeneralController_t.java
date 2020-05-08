 package cz.datalite.zk.components.list;
 
 import cz.datalite.dao.DLResponse;
 import cz.datalite.dao.DLSort;
 import cz.datalite.zk.components.list.controller.impl.*;
 import cz.datalite.dao.DLSortType;
 import cz.datalite.helpers.ReflectionHelper;
 import cz.datalite.utils.HashMapAutoCreate;
 import cz.datalite.zk.components.list.controller.*;
 import cz.datalite.zk.components.list.enums.DLFilterOperator;
 import cz.datalite.zk.components.list.enums.DLFiterType;
 import cz.datalite.zk.components.list.filter.NormalFilterModel;
 import cz.datalite.zk.components.list.filter.NormalFilterUnitModel;
 import cz.datalite.zk.components.list.model.*;
 import cz.datalite.zk.components.list.view.DLListControl;
 import cz.datalite.zk.components.list.view.DLListbox;
 import cz.datalite.zk.components.list.view.DLListboxManager;
 import cz.datalite.zk.components.list.view.DLQuickFilter;
 import cz.datalite.zk.components.paging.DLPaging;
 import cz.datalite.zk.components.paging.DLPagingController;
 import cz.datalite.zk.components.paging.DLPagingModel;
 import java.util.Collections;
 import java.util.EnumMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.Executions;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.event.SelectEvent;
 import org.zkoss.zk.ui.util.Composer;
 import org.zkoss.zul.Paging;
 
 /**
  * Main controller for the extended listbox component. This controller is the
  * master for other controllers like listboxComponent, paging, quickFilter, etc.
  * This supervisor reacts on the events which are propagated from the parts.
  * This implementation is independent on the used technology like hibernate,
  * toplink or simply list. To specify implementation you must use on of special
  * implementation.
  * @param <T> main entity
  * @author Karel Čemus <cemus@datalite.cz>
  */
 public abstract class DLListboxGeneralController<T> implements DLListboxExtController<T> {
 
     /** Complete listbox, filter and paging model. This is storable to the session. */
     protected DLMasterModel model = new DLMasterModel();
     /** Controller for the listbox component */
     protected DLListboxComponentController<T> listboxController;
     /** Controller for the listbox manager  */
     protected DLManagerController managerController = new DLDefaultManagerControllerImpl();
     /** Controller for the paging component */
     protected DLPagingController pagingController = new DLDefaultPagingControllerImpl();
     /** Controller for the easy filter components */
     protected DLEasyFilterController easyFilterController = new DLDefaultEasyFilterControllerImpl();
     /** Controller for the quick filter comopnent */
     protected DLQuickFilterController quickFilterController = new DLDefaultQuickFilterControllerImpl();
     /** main entity class */
     protected final Class<T> entityClass;
     /** automatically save model */
     protected boolean autosave = true;
     /** component identifier */
     protected final String identifier;
     /** is necessary to call initialization methods - isn't when model is loaded from session */
     protected boolean autoinit = true;
     /** list of the listeners */
     protected Map<String, EventListeners> listeners = new HashMapAutoCreate<String, EventListeners>( EventListeners.class );
     /** model lock - if model is locked it cannot be changed */
     protected boolean lock = false;
 
     /**
      * Create instance of the general controller
      * @param identifier identifier which is used to saving model to the session - it must be unique in the session
      */
     @SuppressWarnings( "unchecked" )
     public DLListboxGeneralController( final String identifier, final Class<T> clazz ) {
         if ( clazz == null ) {
             entityClass = ( Class<T> ) ReflectionHelper.getTypeArguments( DLListboxGeneralController.class, getClass() ).get( 0 );
         } else {
             this.entityClass = clazz;
         }
         this.identifier = identifier;
         if ( autosave ) {
             autoinit ^= loadModel();
         }
         easyFilterController = new DLEasyFilterControllerImpl( this, model.getFilterModel().getEasy() );
     }
 
     public DLListboxGeneralController( final String identifier ) {
         this( identifier, null );
     }
 
     public void doAfterCompose( final Component comp ) {
         if ( comp instanceof DLListbox ) {
             initListbox( ( DLListbox ) comp );
         } else if ( comp instanceof DLPaging ) {
             initPaging( ( DLPaging ) comp );
         } else if ( comp instanceof DLListboxManager ) {
             initManager( ( DLListboxManager ) comp );
         } else if ( comp instanceof DLQuickFilter ) {
             initQuickFilter( ( DLQuickFilter ) comp );
         } else if ( comp instanceof DLListControl ) { // řídící komponenta
             final DLListControl listControl = ( DLListControl ) comp;
             listControl.init();
             if ( listControl.isQfilter() ) {
                 initQuickFilter( listControl.getQFilterComponent() );
             }
             if ( listControl.isManager() ) {
                 initManager( listControl.getManagerComponent() );
             }
             if ( listControl.isPaging() ) {
                 initPaging( listControl.getPagingComponent() );
             }
         } else if ( comp instanceof Paging ) {
             throw new IllegalArgumentException( "If you want to use paging, you have to use DLPaging component. In ZUL file it is called \"dlpaging\"." );
         }
     }
 
     /**
      * Initialize paging component
      * @param comp paging component
      */
     protected void initPaging( final DLPaging comp ) {
         pagingController = new DLPagingControllerImpl( this, model.getPagingModel(), comp );
     }
 
     /**
      * Initialize listbox component
      * @param comp listbox component
      */
     protected void initListbox( final DLListbox comp ) {
         listboxController = new DLListboxComponentControllerImpl<T>( this, model.getColumnModel(), comp, autoinit );
     }
 
     /**
      * Initialize manager component
      * @param comp manager component
      */
     protected void initManager( final DLListboxManager comp ) {
         managerController = new DLManagerControllerImpl<T>( this, comp );
     }
 
     /**
      * Initialize quick filter compnent
      * @param comp quick filter component
      */
     protected void initQuickFilter( final DLQuickFilter comp ) {
         quickFilterController = new DLQuickFilterControllerImpl( this, model.getFilterModel().getQuick(), comp );
     }
 
     public void onCreate() {
         getListboxController().fireOrderChanges();
         quickFilterController.fireChanges();
         managerController.fireChanges();
 
         if ( listboxController.isLoadDataOnCreate() ) {
             refreshDataModel();
         }
     }
 
     public void onPagingModelChange() {
         if ( lock ) {
             return;
         }
         refreshDataModel();
         pagingController.fireChanges();
         autosaveModel();
     }
 
     public void onFilterChange( final String filterType ) {
         if ( lock ) {
             return;
         }
         model.getPagingModel().clear();
         managerController.fireChanges();
         refreshDataModel();
         autosaveModel();
 
         final boolean easy = model.getFilterModel().getEasy().isEmpty();
         final boolean quick = model.getFilterModel().getQuick().isEmpty();
         final boolean normal = model.getFilterModel().getNormal().isEmpty();
 
         // There are defined active filters - true means that filter is active
         final Map<DLFiterType, Boolean> filters = new EnumMap<DLFiterType, Boolean>( DLFiterType.class );
         filters.put( DLFiterType.EASY, !easy );
         filters.put( DLFiterType.QUICK, !quick );
         filters.put( DLFiterType.NORMAL, !normal );
         filters.put( DLFiterType.ALL, quick || easy || normal );
 
         callListeners( new Event( filterType, getListbox(), filters ) );
         callListeners( new Event( DLListboxEvents.ON_FILTER_CHANGE, getListbox(), filters ) );
     }
 
     public void onSortChange() {
         if ( lock ) {
             return;
         }
         refreshDataModel();
         getListboxController().fireChanges();
         autosaveModel();
     }
 
     public void confirmEasyFilter() {
         if ( lock ) {
             return;
         }
         getEasyFilterController().onEasyFilter();
     }
 
     public void clearEasyFilter() {
         clearEasyFilter( true );
     }
 
     public void clearEasyFilter( final boolean refresh ) {
         if ( lock ) {
             return;
         }
         getEasyFilterController().onClearEasyFilter( refresh );
     }
 
     public DLColumnModel getColumnModel() {
         return model.getColumnModel();
     }
 
     public NormalFilterModel getNormalFilterModel() {
         return model.getFilterModel().getNormal();
     }
 
     public void onSortManagerOk( final List<Map<String, Object>> data ) {
         if ( lock ) {
             return;
         }
         // reset sorting settings
         for ( DLColumnUnitModel unit : model.getColumnModel().getColumnModels() ) {
             unit.setSortType( DLSortType.NATURAL );
         }
 
         // updating the models
         for ( Map<String, Object> map : data ) {
             // if natural is setted nothing has to be done
             if ( DLSortType.NATURAL.equals( map.get( "sortType" ) ) ) {
                 continue;
             }
             // search coresponing model and update
             @SuppressWarnings( "unchecked" )
             final java.util.Map.Entry<Integer, String> column = (( java.util.Map.Entry<Integer, String> ) map.get( "column" ));
             model.getColumnModel().getColumnModels().get( column.getKey() ).setSortType( ( DLSortType ) map.get( "sortType" ) );
         }
 
         onSortChange();
     }
 
     public void onColumnManagerOk( final List<Map<String, Object>> data ) {
         if ( lock ) {
             return;
         }
         // reset visibility settings
         for ( DLColumnUnitModel unit : model.getColumnModel().getColumnModels() ) {
             unit.setVisible( false );
         }
 
         // set visible value on true where user wanted
         for ( Map<String, Object> map : data ) {
             DLColumnUnitModel column = model.getColumnModel().getColumnModels().get( ( Integer ) map.get( "index" ) );
             column.setVisible( true );
            column.setOrder( ( Integer ) map.get("order")); // FIXME JB REV MP BUG 1 here is set column order
         }
 
        getListboxController().fireOrderChanges(); // FIXME JB REV MP BUG 1 here column order is different
         autosaveModel();
 
         refreshDataModel(); // JB if data for hidden columns are not available, need to reload data (not only set model)
     }
 
     public void onFilterManagerOk( final NormalFilterModel data ) {
         if ( lock ) {
             return;
         }
         model.getFilterModel().getNormal().clear();
         model.getFilterModel().getNormal().addAll( data );
 
         onFilterChange( DLListboxEvents.ON_NORMAL_FILTER_CHANGE );
     }
 
     public void onExportManagerOk( final org.zkoss.util.media.AMedia data ) {
         org.zkoss.zul.Filedownload.save( data );
     }
 
     protected DLColumnUnitModel getColumnUnitModel( final Integer order ) {
         for ( DLColumnUnitModel unit : model.getColumnModel().getColumnModels() ) {
             if ( order.equals( unit.getOrder() ) ) {
                 return unit;
             }
         }
         return null;
     }
 
     protected DLColumnUnitModel getColumnUnitModel( final String column ) {
         for ( DLColumnUnitModel unit : model.getColumnModel().getColumnModels() ) {
             if ( column.equals( unit.getColumn() ) ) {
                 return unit;
             }
         }
         return null;
     }
 
     public void refreshDataModel() {
         refreshDataModel( false );
     }
 
     public void refreshDataModel( final boolean clearPaging ) {
         if ( lock ) {
             return;
         }
 
         if ( clearPaging ) {
             model.getPagingModel().clear();
         }
 
         final cz.datalite.dao.DLResponse<T> response =
                 loadData( model.getFilterModel().toNormal( model.getColumnModel() ),
                 model.getPagingModel().getPageSize() * model.getPagingModel().getActualPage(),
                 model.getPagingModel().getPageSize() == 0 ? 0 : model.getPagingModel().getPageSize() + 1,
                 model.getColumnModel().getSorts() );
 
         if ( model.getPagingModel().getPageSize() == DLPagingModel.NOT_PAGING ) {
             getListboxController().setListboxModel( response.getData() );
         } else {
             getListboxController().setListboxModel(
                     response.getData().size() > 1 ? response.getData().subList(
                     0,
                     Math.max( 0, Math.min( model.getPagingModel().getPageSize(), response.getData().size() ) ) ) : response.getData() );
         }
 
         model.getPagingModel().setTotalSize( response.getRows(), response.getData().size() );
 
         getListboxController().fireDataChanges();
         pagingController.fireChanges();
 
         callListeners( new Event( DLListboxEvents.ON_MODEL_CHANGE, getListbox() ) );
     }
 
     public DLResponse<T> loadData( final int rowLimit ) {
         return loadData( model.getFilterModelInNormal(), 0, rowLimit, model.getColumnModel().getSorts() );
     }
 
     /**
      * Returns data list coresponding to search criterias.
      * @param filter filter criterias
      * @param firstRow index of the first rows begins at 0
      * @param rowCount row count in the result
      * @param sorts sort settings
      * @return coresponing data list
      */
     protected abstract cz.datalite.dao.DLResponse<T> loadData( final List<NormalFilterUnitModel> filter, final int firstRow, final int rowCount, final List<cz.datalite.dao.DLSort> sorts );
 
     /**
      * Returns distinct values in this column. Rows have to corespond to
      * filter criterias.
      * @deprecated since 2.9.2010 - for distinct components there are {@link cz.datalite.zk.components.list.filter.components.DataLoader}
      * @param column column name with distinct
      * @param filter criterias
      * @return coresponding model
      */
     protected abstract DLResponse<String> loadDistinctColumnValues( final String column, final List<NormalFilterUnitModel> filter, final int firstRow, final int rowCount, final List<cz.datalite.dao.DLSort> sorts );
 
     public DLResponse<String> loadDistinctValues( final String column, final String qFilterValue, final int firstRow, final int rowCount ) {
 //        final DLFilterModel filterModel = new DLFilterModel() {
 //
 //            {
 //                _easy.putAll( model.getFilterModel().getEasy() );
 //                _quick.setKey( model.getFilterModel().getQuick().getKey() );
 //                _quick.setValue( model.getFilterModel().getQuick().getValue() );
 //                _default.addAll( model.getFilterModel().getDefault() );
 //                _normal.addAll( normalFilter );
 //            }
 //        };
 
         List<NormalFilterUnitModel> filterModel;
         if ( qFilterValue == null ) {
             filterModel = Collections.emptyList();
         } else {
             final NormalFilterUnitModel filterUnit = new NormalFilterUnitModel( model.getColumnModel().getByName( column ) );
             filterUnit.setValue( 1, qFilterValue );
             filterUnit.setOperator( filterUnit.getQuickFilterOperator() );
             filterModel = Collections.singletonList( filterUnit );
         }
 
         final DLSort sort = new DLSort( column, DLSortType.ASCENDING );
 
         return loadDistinctColumnValues( column, filterModel, firstRow, rowCount, Collections.singletonList( sort ) );
     }
 
     public Class getEntityClass() {
         return entityClass;
     }
 
     public void registerEasyFilterOnFilter( final String compId ) {
         registerEasyFilterOnFilter( getListboxController().getFellow( compId ) );
     }
 
     public void registerEasyFilterOnFilter( final Component comp ) {
         registerEasyFilterOnFilter( comp, "onClick" );
     }
 
     public void registerEasyFilterOnFilter( final String compId, final String event ) {
         registerEasyFilterOnFilter( getListboxController().getFellow( compId ), event );
     }
 
     public void registerEasyFilterOnFilter( final Component comp, final String event ) {
         getListboxController().addForward( event, comp, "onEasyFilter" );
 
     }
 
     public void registerEasyFilterVariable( final String compId, final String variableName ) {
         registerEasyFilterVariable( getListboxController().getFellow( compId ), variableName );
     }
 
     public void registerEasyFilterVariable( final Component comp, final String variableName ) {
         comp.setVariable( variableName, easyFilterController.getBindingModel(), true );
     }
 
     public void registerEasyFilterOnClear( final String compId ) {
         registerEasyFilterOnClear( getListboxController().getFellow( compId ) );
     }
 
     public void registerEasyFilterOnClear( final Component comp ) {
         registerEasyFilterOnClear( comp, "onClick" );
     }
 
     public void registerEasyFilterOnClear( final String compId, final String event ) {
         registerEasyFilterOnClear( getListboxController().getFellow( compId ), event );
     }
 
     public void registerEasyFilterOnClear( final Component comp, final String event ) {
         getListboxController().addForward( event, comp, "onEasyFilterClear" );
     }
 
     public boolean loadModel() {
         final DLMasterModel loaded = ( DLMasterModel ) org.zkoss.zk.ui.Sessions.getCurrent().getAttribute( getSessionName() + "cache" );
         if ( loaded == null ) {
             return false;
         } else {
             model = loaded;
             model.getFilterModel().getDefault().clear();
             return true;
         }
     }
 
     /**
      * If autoset model then save model.
      */
     protected void autosaveModel() {
         if ( autosave ) {
             saveModel();
         }
     }
 
     public void saveModel() {
         Executions.getCurrent().getDesktop().getSession().setAttribute( getSessionName() + "cache", model );
     }
 
     public void setAutoSaveModel( final boolean autosave ) {
         this.autosave = autosave;
     }
 
     /**
      * Returns session name for save/load model
      * @return session name
      */
     protected String getSessionName() {
         return getClass().getName() + "#" + org.zkoss.zk.ui.Executions.getCurrent().getDesktop().getRequestPath() + "$" + identifier;
     }
 
     public void refreshBinding() {
         getListboxController().refreshBindingAll();
     }
 
     public void clearAllModel() {
         model.clear();
         autosaveModel();
 
         getListboxController().fireColumnModelChanges();
         getListboxController().fireOrderChanges();
         getListboxController().fireChanges();
         managerController.fireChanges();
         refreshDataModel();
         quickFilterController.fireChanges();
         easyFilterController.fireChanges();
     }
 
     public void clearFilterModel() {
         model.getFilterModel().clear();
         refreshDataModel();
         quickFilterController.fireChanges();
         easyFilterController.fireChanges();
         managerController.fireChanges();
     }
 
     public void addDefaultFilter( final String property, final DLFilterOperator operator, final Object value1, final Object value2 ) {
         final NormalFilterUnitModel unit = new NormalFilterUnitModel( property );
         unit.setOperator( operator );
         unit.setValue( 1, value1 );
         unit.setValue( 2, value2 );
         model.getFilterModel().getDefault().add( unit );
     }
 
     public T getSelectedItem() {
         return getListboxController().getSelectedItem();
     }
 
     public void setSelectedItem( final T selecteItem ) {
         getListboxController().setSelectedItem( selecteItem );
     }
 
     public void setSelectedIndex( final int index ) {
         getListboxController().setSelectedIndex( index );
     }
 
     public DLEasyFilterController getEasyFilterController() {
         return easyFilterController;
     }
 
     public DLMasterModel getModel() {
         return model;
     }
 
     public Composer getWindowCtl() {
         return getListboxController().getWindowCtl();
     }
 
     public void onSelect() {
         if ( lock ) {
             return;
         }
         callListeners( new SelectEvent( DLListboxEvents.ON_DLSELECT, getListbox(), Collections.singleton( getListboxController().getSelectedItem() ) ) );
     }
 
     public void addListener( final String event, final EventListener listener ) {
         listeners.get( event ).add( listener );
     }
 
     public boolean removeListener( final String event, final EventListener listener ) {
         return listeners.get( event ).remove( listener );
     }
 
     public boolean isLocked() {
         return lock;
     }
 
     public void lockModel() {
         this.lock = true;
     }
 
     public void unlockModel() {
         this.lock = false;
     }
 
     public void focus() {
         getListboxController().focus();
     }
 
     protected DLListboxComponentController<T> getListboxController() {
         if ( listboxController == null ) {
             throw new IllegalStateException( "Listbox controller is mising! You are using component without controller (old version) for extended functions." );
         }
         return listboxController;
     }
 
     public DLListbox getListbox() {
         return listboxController.getListbox();
     }
 
     protected void callListeners( final Event event ) {
         Events.postEvent( event );
         for ( EventListener listener : listeners.get( event.getName() ) ) {
             try {
                 listener.onEvent( event );
             } catch ( Exception ex ) {
                 Logger.getLogger( DLListboxGeneralController.class.getName() ).error( "Event couldn't be send. Error has occured in DLListboxGeneralController.", ex );
             }
         }
     }
 
     public String getListboxUuid() {
         return listboxController.getListbox().getUuid();
     }
 
     public String getPagingUuid() {
         return pagingController.getUuid();
     }
 
     public String getQuickFilterUuid() {
         return quickFilterController.getUuid();
     }
 
     public static class EventListeners extends LinkedList<EventListener> {
     }
 }
