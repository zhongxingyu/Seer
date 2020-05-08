 package cz.datalite.zk.components.list.view;
 
 import cz.datalite.dao.DLSortType;
 import cz.datalite.helpers.StringHelper;
 import cz.datalite.zk.components.list.controller.DLListboxComponentController;
 import cz.datalite.zk.components.list.enums.DLFilterOperator;
 import cz.datalite.zk.components.list.filter.compilers.FilterCompiler;
 import cz.datalite.zk.components.list.filter.components.CloneFilterComponentFactory;
 import cz.datalite.zk.components.list.filter.components.CloneableFilterComponent;
 import cz.datalite.zk.components.list.filter.components.FilterComponentFactory;
 import cz.datalite.zk.components.list.filter.components.InstanceFilterComponentFactory;
 import cz.datalite.zk.components.list.model.DLColumnUnitModel;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import org.zkoss.lang.Strings;
 import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.SortEvent;
 import org.zkoss.zul.Listheader;
 import org.zkoss.zul.ListitemComparator;
 
 /**
  * ZK component DLListheader is extended component from standard Listheader
  *
  * @author Jiri Bubnik
  * @author Karel Cemus
  */
 public class DLListheader extends Listheader {
 
     // should be this column sorted in the default
     protected DLSortType defaultSort = DLSortType.NATURAL;
     // column name in the database which coresponds to this col - for sorting
     protected String sortColumn = "";
     // will be used default zk sort od database sort
     protected boolean sortZk;
     // is column sortable - false disables sorting
     protected boolean sortable = false;
     // column name in the database which coresponds to this col
     protected String column;
     // data type of this column
     protected Class columnType;
     // is enabled for quick filter
     protected boolean quickFilter = true;
     /** defines operators used in normal filter for this column */
     protected List<DLFilterOperator> operators;
     /** defines class which generates default component for filter value in normal filter */
     protected FilterComponentFactory filterComponentFactory;
     /** defines own filter compiler for this column */
     protected FilterCompiler filterCompiler;
     // is enabled for all filters
     protected boolean filter = true;
     // converter
     protected String converter;
     // model
     protected DLColumnUnitModel model;
     // controller
     protected DLListboxComponentController controller;
 
     public void initModel() {
         model.setLabel( getLabel() );
         model.setSortable( sortable );
         model.setSortZk( sortZk );
         model.setSortType( defaultSort );
         model.setSortColumn( sortColumn );
         model.setVisible( isVisible() );
         model.setColumn( column );
         model.setColumnType( columnType );
         model.setConverter( converter, this, Collections.<String,String>emptyMap() );
         model.setQuickFilter( quickFilter );
         model.setFilter( filter );
         model.setFilterOperators( operators );
         model.setFilterComponentFactory( filterComponentFactory );
         model.setFilterCompiler( filterCompiler );
     }
 
     /**
      * Sets default sort on this column
      * 
      * @param defaultSort the defaultSort to set
      */
     public void setDefaultSort( final String defaultSort ) {
         if ( "asc".equals( defaultSort ) ) {
             this.defaultSort = DLSortType.ASCENDING;
         } else if ( "desc".equals( defaultSort ) ) {
             this.defaultSort = DLSortType.DESCENDING;
         } else if ( "natural".equals( defaultSort ) ) {
             this.defaultSort = DLSortType.NATURAL;
         } else {
             throw new UnsupportedOperationException( "Unknown sortType." );
         }
     }
     /**
      * Identifier for searching help in the database
      */
     private String helpId;
     /**
      * Text displayed in the help
      */
     private String helpText;
     /**
      * Identifier for the internationalization (i18n)
      */
     private String languageId;
 
     /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
     public void setHelpId( final String helpId ) {
         this.helpId = helpId;
     }
 
     public String getHelpId() {
         return helpId;
     }
 
     public void getHelpText( final String helpText ) {
         this.helpText = helpText;
     }
 
     public String getHelpText() {
         return helpText;
     }
 
     public void setLanguageId( final String languageId ) {
         this.languageId = languageId;
     }
 
     public String getLanguageId() {
         return languageId;
     }
 
     /**
      * If header doesn't containt tooltiptext, label is default tooltip.
      * Usefull for long header names.
      * 
      * @return toolitp text
      */
     @Override
     public String getTooltiptext() {
         final String tooltip = super.getTooltiptext();
 
         return StringHelper.isNull( tooltip ) ? getLabel() : tooltip;
     }
 
     @Override
     public void setSort( final String type ) {
         if ( type != null ) {
             if ( type.startsWith( "auto" ) ) {
                 if ( type.startsWith( "auto(" ) && type.endsWith( ")" ) ) {
                     sortColumn = type.substring( 5, type.length() - 1 );
                 }
                 sortable = true;
                 sortZk = true;
                 super.setSort( type );
             } else if ( "none".equals( type ) ) {
                 sortable = false;
                 super.setSort( type );
             } else if ( type.startsWith( "db(" ) && type.endsWith( ")" ) ) {
                 sortColumn = type.substring( 3, type.length() - 1 );
                 sortZk = false;
                 sortable = true;
                 setSortAscending( new ListitemComparator( this, true, true ) );
                 setSortDescending( new ListitemComparator( this, false, true ) );
             } else {
                 throw new UnsupportedOperationException( "Unknown sortType." );
             }
         }
     }
    
    @Override
    public void onSort(SortEvent event) {
        if ( isController() )
            getController().onSort( this );
        else
            super.onSort( event );
    }
 
    /** @deprecated since ZK 6.5.0 */
     @Override
    @Deprecated
     public void onSort() {
         if ( isController() ) {
             getController().onSort( this );
         } else {
             super.onSort();
         }
     }
 
     @Override
     public DLListbox getListbox() {
         return ( DLListbox ) super.getListbox();
     }
 
     /**
      * Sets graphical symbol for sorting
      */
     public void setSortStyle() {
         super.setSortDirection( getSortDirection() );
     }
 
     @Override
     public String getSortDirection() {
         if ( isController() ) {
             return getModel().getSortType().getStringValue();
         } else {
             return super.getSortDirection();
         }
     }
 
     /**
      * if is controller attached, this method is empty
      * @param sortDir sort direction
      * @throws WrongValueException
      */
     @Override
     public void setSortDirection( final String sortDir ) throws WrongValueException {
         if ( !isController() ) {
             super.setSortDirection( sortDir );
         }
     }
 
     protected DLListboxComponentController getController() {
         return getListbox().getController();
     }
 
     protected boolean isController() {
         return controller != null;
     }
 
     public void setModel( final DLColumnUnitModel model ) {
         this.model = model;
     }
 
     public void fireChanges() {
         if ( isVisible() != getModel().isVisible() ) {
             setVisible( getModel().isVisible() );
         }
         setSortStyle();
     }
 
     public void setController( final DLListboxComponentController controller ) {
         this.controller = controller;
     }
 
     public DLColumnUnitModel getModel() {
         if ( model == null ) {
             throw new IllegalArgumentException( "Model is required. Please set the controller." );
         } else {
             return model;
         }
     }
 
     public void setColumn( final String column ) {
         this.column = column;
     }
 
     public void setColumnType( final String columnType ) throws ClassNotFoundException {
         this.columnType = Class.forName( columnType );
     }
 
     public void setConverter( final String converter ) {
         this.converter = converter;
     }
 
     public void setQuickFilter( final boolean quickFilter ) {
         this.quickFilter = quickFilter;
     }
 
     public void setFilter( final boolean filter ) {
         this.filter = filter;
     }
 
     public void setFilterOperators( final String ops ) {
         operators = new ArrayList<DLFilterOperator>();
         if ( Strings.isEmpty( ops ) ) {
             return;
         }
         for ( String op : ops.split( "," ) ) {
             if ( "none".equals( op ) ) { // none operator can be used
                 operators.clear(); // remove all already define operators
                 return;
             }
             operators.add( DLFilterOperator.strToEnum( op ) );
         }
     }
 
     public void setFilterComponent( final CloneableFilterComponent filterComponent ) {
         if ( this.filterComponentFactory == null ) {
             this.filterComponentFactory = new CloneFilterComponentFactory( filterComponent );
         }
     }
 
     public void setFilterComponent( final String filterComponentClass ) {
         if ( this.filterComponentFactory == null ) {
             try {
                 filterComponentFactory = new InstanceFilterComponentFactory( filterComponentClass );
             } catch ( RuntimeException ex ) {
                 throw new IllegalArgumentException( "FilterComponent wasn't created. Class '"
                         + filterComponentClass + "' wasn't found.", ex );
             }
         }
     }
 
     public void setFilterCompiler( final FilterCompiler filterCompiler ) {
         if ( this.filterCompiler == null ) {
             this.filterCompiler = filterCompiler;
         }
     }
 
     public void setFilterCompiler( final String filterCompilerClass ) {
         if ( this.filterCompiler == null ) {
             try {
                 filterCompiler = ( FilterCompiler ) Class.forName( filterCompilerClass ).newInstance();
             } catch ( InstantiationException ex ) {
                 throw new IllegalArgumentException( "FilterCompiler wasn't created. For class '"
                         + filterCompilerClass + "' couldn't be created an instance.", ex );
             } catch ( IllegalAccessException ex ) {
                 throw new IllegalArgumentException( "FilterCompiler wasn't created. Class '"
                         + filterCompilerClass + "' has no public constructor.", ex );
             } catch ( ClassNotFoundException ex ) {
                 throw new IllegalArgumentException( "FilterCompiler wasn't created. Class '"
                         + filterCompilerClass + "' wasn't found.", ex );
             }
         }
     }
 
     public void setFilterGeneralComponent( final String className ) {
         if ( this.filterCompiler == null && this.filterComponentFactory == null ) {
             try {
                 final Object component = Class.forName( className ).newInstance();
                 setFilterGeneralComponent( component );
             } catch ( InstantiationException ex ) {
                 throw new IllegalArgumentException( "GeneralFilterComponent wasn't created. For class '"
                         + className + "' couldn't be created an instance.", ex );
             } catch ( IllegalAccessException ex ) {
                 throw new IllegalArgumentException( "GeneralFilterComponent wasn't created. Class '"
                         + className + "' has no public constructor.", ex );
             } catch ( ClassNotFoundException ex ) {
                 throw new IllegalArgumentException( "GeneralFilterComponent wasn't created. Class '"
                         + className + "' wasn't found.", ex );
             }
         }
     }
 
     public void setFilterGeneralComponent( final Object component ) {
         if ( !FilterCompiler.class.isAssignableFrom( component.getClass() ) ) {
             throw new IllegalArgumentException( "FilterGeneralComponent requires to implement " + FilterCompiler.class.getCanonicalName()
                     + ". To define own FilterComponent serves attribute \"filterComponent\"." );
         }
         if ( !CloneableFilterComponent.class.isAssignableFrom( component.getClass() ) ) {
             throw new IllegalArgumentException( "FilterGeneralComponent requires to implement " + CloneableFilterComponent.class.getCanonicalName()
                     + ". To define own FilterCompiler serves attribute \"filterCompiler\"." );
         }
 
         setFilterComponent( ( CloneableFilterComponent ) component );
         setFilterCompiler( ( FilterCompiler ) component );
     }
 }
