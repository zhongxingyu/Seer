 package com.eucalyptus.webui.client.activity;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.eucalyptus.webui.client.ClientFactory;
 import com.eucalyptus.webui.client.place.SearchPlace;
 import com.eucalyptus.webui.client.service.SearchRange;
 import com.eucalyptus.webui.client.service.SearchResult;
 import com.eucalyptus.webui.client.service.SearchResultRow;
 import com.eucalyptus.webui.client.view.ConfirmationView;
 import com.eucalyptus.webui.client.view.DetailView;
 import com.eucalyptus.webui.client.view.FooterView;
 import com.eucalyptus.webui.client.view.ImageView;
 import com.eucalyptus.webui.client.view.HasValueWidget;
 import com.eucalyptus.webui.client.view.InputField;
 import com.eucalyptus.webui.client.view.InputView;
 import com.eucalyptus.webui.client.view.SecurityGroupView;
 import com.eucalyptus.webui.client.view.FooterView.StatusType;
 import com.eucalyptus.webui.client.view.InputField.ValueType;
 import com.eucalyptus.webui.client.view.LogView.LogType;
 import com.eucalyptus.webui.client.view.SecurityGroupViewImpl;
 import com.eucalyptus.webui.shared.checker.ValueChecker;
 import com.google.common.collect.Lists;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class SecurityGroupActivity extends AbstractSearchActivity implements SecurityGroupView.Presenter, DetailView.Presenter, InputView.Presenter, ConfirmationView.Presenter {
   
   public static final String TITLE = "安全组管理";
   public static final String[] CREATE_SEGROUP_CAPTION = {"Create a new security group", "新建一个安全组"};
   public static final String[] CREATE_SEGROUP_SUBJECT = {"Enter information to create a new security group:", "输入新安全组的信息:"};
   public static final String[] DELETE_SEGROUP_CAPTION = {"Delete selected security group", "删除选中安全组"};
   public static final String[] DELETE_SEGROUP_SUBJECT = {"Are you sure to delete a new security group?", "确定要删除选中的安全组吗？"};
   public static final String[] SEGROUP_NAME_INPUT_TITLE = {"Security group name", "安全组名称"};
   public static final String[] SEGROUP_DESC_INPUT_TITLE = {"Security group description", "安全组描述"};
 
   private static final Logger LOG = Logger.getLogger( SecurityGroupActivity.class.getName( ) );
   
   
   
   private Set<SearchResultRow> currentSelected;
   
   public SecurityGroupActivity( SearchPlace place, ClientFactory clientFactory ) {
     super( place, clientFactory );
   }
 
   @Override
   protected void doSearch( String query, SearchRange range ) {
     this.clientFactory.getBackendAwsService().lookupSecurityGroup( this.clientFactory.getLocalSession( ).getSession( ), search, range,
                                                            new AsyncCallback<SearchResult>( ) {
       
       @Override
       public void onFailure( Throwable caught ) {
         ActivityUtil.logoutForInvalidSession( clientFactory, caught );
         LOG.log( Level.WARNING, "Search failed: " + caught );
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
       //this.clientFactory.getShellView( ).showDetail( DETAIL_PANE_SIZE );
       //showSingleSelectedDetails( selection.toArray( new SearchResultRow[0] )[0] );
     }
   }
 
   @Override
   public void saveValue( ArrayList<String> keys, ArrayList<HasValueWidget> values ) {
     // Nothing to do for now.
   }
 
   @Override
   protected String getTitle( ) {
     return TITLE;
   }
 
   @Override
   protected void showView( SearchResult result ) {
     if ( this.view == null ) {
       this.view = this.clientFactory.getSecurityGroupView( );
       ( ( SecurityGroupView ) this.view ).setPresenter( this );
       container.setWidget( this.view );
       ( ( SecurityGroupView ) this.view ).clear( );
     }
     ( ( SecurityGroupView ) this.view ).showSearchResult( result );    
   }
 
   private void doCreateSecurityGroup(String name, String desc) {
     this.clientFactory.getShellView().getFooterView().showStatus(StatusType.LOADING, "新建安全组...", 0);
     this.clientFactory.getBackendAwsService().createSecurityGroup(this.clientFactory.getLocalSession().getSession(), name, desc, 
         new AsyncCallback<String>( ) {
           @Override
           public void onFailure( Throwable caught ) {
           ActivityUtil.logoutForInvalidSession( clientFactory, caught );
           clientFactory
           .getShellView()
           .getFooterView()
           .showStatus(StatusType.ERROR, "创建安全组失败", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
           clientFactory
           .getShellView()
           .getLogView()
           .log(LogType.ERROR, "创建安全组失败" + ": " + caught.getMessage());
           }
           
           @Override
           public void onSuccess( String result ) {
             clientFactory
             .getShellView()
             .getFooterView()
             .showStatus(StatusType.NONE, "创建安全组" + result + "成功", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
             reloadCurrentRange();
           }
         } );    
   }
   private void doDeleteSecurityGroups() {
     if ( currentSelected == null || currentSelected.size( ) < 1 ) {
       return;
     }
     
     final ArrayList<String> ids = Lists.newArrayList( ); 
     for ( SearchResultRow row : currentSelected ) {
       ids.add( row.getField( 0 ) );
     }
     
     clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "删除安全组中...", 0 );
     
     clientFactory.getBackendAwsService( ).deleteSecurityGroups( clientFactory.getLocalSession( ).getSession( ), ids, new AsyncCallback<Void>( ) {
 
       @Override
       public void onFailure( Throwable caught ) {
         ActivityUtil.logoutForInvalidSession( clientFactory, caught );
         clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "删除安全组失败", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
         clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "删除安全组失败:" + caught.getMessage( ) );
       }
 
       @Override
       public void onSuccess( Void arg0 ) {
         clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "删除安全组成功", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
         clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "删除安全组成功" );
         reloadCurrentRange( );
         currentSelected = null;
       }
     });
 
   }
 
   @Override
   public void onCreateSecurityGroup() {
     InputView dialog = this.clientFactory.getInputView( );
     dialog.setPresenter( this );
     dialog.display( CREATE_SEGROUP_CAPTION[1], CREATE_SEGROUP_SUBJECT[1], new ArrayList<InputField>( Arrays.asList(
       new InputField( ) {
         @Override
         public String getTitle( ) {
           return SEGROUP_NAME_INPUT_TITLE[1];
         }
   
         @Override
         public ValueType getType( ) {
           return ValueType.TEXT;
         }
   
         @Override
         public ValueChecker getChecker( ) {
           return null;
         }
       }, new InputField() {
       @Override
       public String getTitle() {
         return SEGROUP_DESC_INPUT_TITLE[1];
       }
 
       @Override
       public ValueType getType() {
         return ValueType.TEXTAREA;
       }
 
       @Override
       public ValueChecker getChecker() {
         return null;
       }
     })));
 
   }
 
   @Override
   public void onDeleteSecurityGroup() {
     if ( currentSelected == null || currentSelected.size( ) < 1 ) {
       clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "请选择要删除的安全组", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
         return;
     }
     System.out.println("size: " + currentSelected.size());
     
     ConfirmationView dialog = this.clientFactory.getConfirmationView( );
     dialog.setPresenter( this );
     dialog.display( DELETE_SEGROUP_CAPTION[1], DELETE_SEGROUP_SUBJECT[1], currentSelected, new ArrayList<Integer>( Arrays.asList( 0, 1 ) ));    
   }
 
   @Override
   public void onAddSecurityRule() {
     if ( currentSelected == null || currentSelected.size( ) != 1 ) {
       clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "请选择一个安全组", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
         return;
     }
     for (SearchResultRow r: currentSelected) {
       Window.Location.replace(r.getField(2));
     }
   }
 
   @Override
   public void process(String subject, ArrayList<String> values) {
     if (CREATE_SEGROUP_SUBJECT[1].equals(subject)) {
       doCreateSecurityGroup(values.get(0), values.get(1));
     }
     
   }
 
   @Override
   public void confirm(String subject) {
     if (DELETE_SEGROUP_SUBJECT[1].equals(subject)) {
       doDeleteSecurityGroups();
     }  
   }
   
 }
