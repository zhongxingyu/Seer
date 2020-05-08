 package com.eucalyptus.webui.client.activity;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import com.eucalyptus.webui.client.ClientFactory;
 import com.eucalyptus.webui.client.place.InstancePlace;
 import com.eucalyptus.webui.client.service.SearchRange;
 import com.eucalyptus.webui.client.service.SearchResultRow;
 import com.eucalyptus.webui.client.service.SearchResult;
 import com.eucalyptus.webui.client.view.ConfirmationView;
 import com.eucalyptus.webui.client.view.DetailView;
 import com.eucalyptus.webui.client.view.FooterView;
 import com.eucalyptus.webui.client.view.InputField;
 import com.eucalyptus.webui.client.view.InputView;
 import com.eucalyptus.webui.client.view.FooterView.StatusType;
 import com.eucalyptus.webui.client.view.InputField.ValueType;
 import com.eucalyptus.webui.client.view.HasValueWidget;
 import com.eucalyptus.webui.client.view.InstanceView;
 import com.eucalyptus.webui.client.view.LogView.LogType;
 import com.eucalyptus.webui.shared.checker.ValueChecker;
 import com.eucalyptus.webui.shared.checker.ValueCheckerFactory;
 import com.google.common.collect.Lists;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class InstanceActivity extends AbstractSearchActivity
     implements InstanceView.Presenter, ConfirmationView.Presenter, InputView.Presenter {
   
   public static final String TITLE = "虚拟机管理";
   private static final Logger LOG = Logger.getLogger( InstanceActivity.class.getName( ) );
 
   private Set<SearchResultRow> currentSelected;
     
   public InstanceActivity( InstancePlace place, ClientFactory clientFactory ) {
     super( place, clientFactory );
   }
 
   protected void doSearch( final String search, SearchRange range ) {
 	
     this.clientFactory.getBackendAwsService( ).lookupInstance( this.clientFactory.getLocalSession( ).getSession( ), search, range,
                                                            new AsyncCallback<SearchResult>( ) {
       
       @Override
       public void onFailure( Throwable caught ) {
         ActivityUtil.logoutForInvalidSession( clientFactory, caught );
         LOG.log( Level.WARNING, "Search failed: " + caught );
         clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Instance search " + search + " failed: " + caught.getMessage( ) );
         displayData( null );
       }
       
       @Override
       public void onSuccess( SearchResult result ) {
         LOG.log( Level.INFO, "Search success:" + result );
         displayData( result );
       }
       
     } );
   
   }
 
   @Override
   public void onSelectionChange( Set<SearchResultRow> selection ) {
     this.currentSelected = selection;
     if ( selection == null || selection.size( ) != 1 ) {
       LOG.log( Level.INFO, "Not a single selection" );      
       this.clientFactory.getShellView( ).hideDetail( );
     } else {
       LOG.log( Level.INFO, "Selection changed to " + selection );
       // this.clientFactory.getShellView( ).showDetail( DETAIL_PANE_SIZE );
       // showSingleSelectedDetails( selection.toArray( new SearchResultRow[0] )[0] );
     }
   }
 
   @Override
   protected String getTitle( ) {
     return TITLE;
   }
 
   @Override
   protected void showView( SearchResult result ) {
     if ( this.view == null ) {
       this.view = this.clientFactory.getInstanceView( );
       ( ( InstanceView ) this.view ).setPresenter( this );
       container.setWidget( this.view );
       ( ( InstanceView ) this.view ).clear( );
     }
     
     ( ( InstanceView ) this.view ).showSearchResult( result );    
   }
 
   @Override
   public void process(String subject, ArrayList<String> values) {
 	  // TODO Auto-generated method stub
 
   }
 
   @Override
   public void confirm(String subject) {
 	  // TODO Auto-generated method stub
 
   }
 
   @Override
   public void saveValue(ArrayList<String> keys, ArrayList<HasValueWidget> values) {
 	  // TODO Auto-generated method stub
 
   }
 
 @Override
 public void onStartInstances() {
 	final ArrayList<String> ids = Lists.newArrayList();
     for ( SearchResultRow row : currentSelected ) {
         ids.add(row.getField(0));
     }
    
 	this.clientFactory.getBackendAwsService().startInstances(clientFactory.getLocalSession( ).getSession( ), ids, new AsyncCallback<ArrayList<String>>( ) {
 	      @Override
 	      public void onFailure( Throwable caught ) {
 	        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
 	        // Log
 	      }
 	      @Override
 	      public void onSuccess( ArrayList<String> arg ) {
 	    	  clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Instances stopped", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
 	    	// Log
 	      }
 	});
 	
 }
 
 @Override
 public void onStopInstances() {
 	final ArrayList<String> ids = Lists.newArrayList();
     for ( SearchResultRow row : currentSelected ) {
         ids.add(row.getField(0));
     }
 	this.clientFactory.getBackendAwsService().stopInstances(clientFactory.getLocalSession( ).getSession( ), ids, new AsyncCallback<ArrayList<String>>( ) {
 	      @Override
 	      public void onFailure( Throwable caught ) {
 	        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
 	        // Log
 	      }
 	      @Override
 	      public void onSuccess( ArrayList<String> arg ) {
 	    	  clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Instances stopped", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
 	    	// Log
 	      }
 	});
 	
 }
 
 @Override
 public void onTerminateInstances() {
 	final ArrayList<String> ids = Lists.newArrayList();
     for ( SearchResultRow row : currentSelected ) {
         ids.add(row.getField(0));
     }
 	this.clientFactory.getBackendAwsService().terminateInstances(clientFactory.getLocalSession( ).getSession( ), ids, new AsyncCallback<ArrayList<String>>( ) {
 	      @Override
 	      public void onFailure( Throwable caught ) {
 	        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
 	        // Log
 	      }
 	      @Override
 	      public void onSuccess( ArrayList<String> arg ) {
 	    	  clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Instances stopped", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
 	    	// Log
 	      }
 	});
 	
 }
 }
