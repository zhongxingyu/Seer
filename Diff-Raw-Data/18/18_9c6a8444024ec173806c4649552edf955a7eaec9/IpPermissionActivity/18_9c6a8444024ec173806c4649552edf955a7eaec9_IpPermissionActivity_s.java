 package com.eucalyptus.webui.client.activity;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
import sun.awt.SunHints.Value;

 import com.eucalyptus.webui.client.ClientFactory;
 import com.eucalyptus.webui.client.place.SearchPlace;
 import com.eucalyptus.webui.client.service.EucalyptusServiceException;
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
 import com.eucalyptus.webui.client.view.IpPermissionView;
 import com.eucalyptus.webui.client.view.WalrusCtrlView;
 import com.eucalyptus.webui.client.view.FooterView.StatusType;
 import com.eucalyptus.webui.client.view.InputField.ValueType;
 import com.eucalyptus.webui.client.view.LogView.LogType;
 import com.eucalyptus.webui.shared.checker.ValueChecker;
 import com.eucalyptus.webui.shared.query.QueryParser;
 import com.eucalyptus.webui.shared.query.QueryParsingException;
 import com.eucalyptus.webui.shared.query.QueryType;
 import com.eucalyptus.webui.shared.query.SearchQuery;
 import com.google.common.collect.Lists;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class IpPermissionActivity extends AbstractSearchActivity implements IpPermissionView.Presenter, DetailView.Presenter, InputView.Presenter, ConfirmationView.Presenter {
   
   public static final String TITLE = "规则管理";
   public static final String[] CREATE_SERULE_CAPTION = {"Create a new security rule", "新建一个安全规则"};
   public static final String[] CREATE_SERULE_SUBJECT = {"Enter information to create a new security rule:", "输入新安全规则的信息:"};
   public static final String[] DELETE_SERULE_CAPTION = {"Delete selected security rule", "删除选中安全规则"};
   public static final String[] DELETE_SERULE_SUBJECT = {"Are you sure to delete a new security rule?", "确定要删除选中的安全规则吗？"};
   public static final String[] SERULE_SEGROUP_INPUT_TITLE = {"Security Group", "安全组"};
   public static final String[] SERULE_FROM_PORT_INPUT_TITLE = {"From port", "源端口"};
   public static final String[] SERULE_TO_PORT_INPUT_TITLE = {"To Port", "目的端口"};
   public static final String[] SERULE_PROTO_INPUT_TITLE = {"Protocol", "协议"};
   public static final String[] SERULE_IP_RANGE_INPUT_TITLE = {"IP range", "IP段"};
 
   
   private static final Logger LOG = Logger.getLogger( IpPermissionActivity.class.getName( ) );
   private static SearchQuery parseQuery( QueryType type, String query ) {
     SearchQuery sq = null;
     try {
       sq = QueryParser.get( ).parse( type.name( ), query );
     } catch ( QueryParsingException e ) {
     }
     return sq;
   }
 
   
   private Set<SearchResultRow> currentSelected;
   
   public IpPermissionActivity( SearchPlace place, ClientFactory clientFactory ) {
     super( place, clientFactory );
   }
 
   @Override
   protected void doSearch( String query, SearchRange range ) {
     this.clientFactory.getBackendAwsService( ).lookupSecurityRule( this.clientFactory.getLocalSession( ).getSession( ), search, range,
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
       //this.clientFactory.getShellView( ).hideDetail( );
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
       this.view = this.clientFactory.getIpPermissionView( );
       ( ( IpPermissionView ) this.view ).setPresenter( this );
       container.setWidget( this.view );
       ( ( IpPermissionView ) this.view ).clear( );
     }
     ( ( IpPermissionView ) this.view ).showSearchResult( result );    
   }
 
   @Override
   public void onAddRule() {
     InputView dialog = this.clientFactory.getInputView( );
     dialog.setPresenter( this );
     dialog.display( CREATE_SERULE_CAPTION[1], CREATE_SERULE_SUBJECT[1], new ArrayList<InputField>( Arrays.asList(
       new InputField( ) {
         @Override
         public String getTitle( ) {
           return SERULE_SEGROUP_INPUT_TITLE[1];
         }
   
         @Override
         public ValueType getType( ) {
           return ValueType.TEXT;
         }
   
         @Override
         public ValueChecker getChecker( ) {
           return null;
         }
       }, new InputField( ) {
         @Override
         public String getTitle( ) {
           return SERULE_FROM_PORT_INPUT_TITLE[1];
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
         return SERULE_TO_PORT_INPUT_TITLE[1];
       }
 
       @Override
       public ValueType getType() {
         return ValueType.TEXT;
       }
 
       @Override
       public ValueChecker getChecker() {
         return null;
       }
     }, new InputField() {
       @Override
       public String getTitle() {
         return SERULE_PROTO_INPUT_TITLE[1];
       }
 
       @Override
       public ValueType getType() {
         return ValueType.TEXT;
       }
 
       @Override
       public ValueChecker getChecker() {
         return null;
       }  
     }, new InputField() {
       @Override
       public String getTitle() {
         return SERULE_IP_RANGE_INPUT_TITLE[1];
       }
 
       @Override
       public ValueType getType() {
         return ValueType.TEXT;
       }
 
       @Override
       public ValueChecker getChecker() {
         return null;
       }      
     })));
     
     SearchQuery q = parseQuery(QueryType.ipPermission, search);
     if (q != null && q.hasOnlySingle("segroup")) {
       String group = q.getSingle("segroup").getValue();
       dialog.setState(0, group, false);
     }
   }
 
   @Override
   public void onDeleteRule() {
     if ( currentSelected == null || currentSelected.size( ) < 1 ) {
       clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "请选择要删除的安全规则", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
         return;
     }
     System.out.println("size: " + currentSelected.size());
     
     ConfirmationView dialog = this.clientFactory.getConfirmationView( );
     dialog.setPresenter( this );
     dialog.display( DELETE_SERULE_CAPTION[1], DELETE_SERULE_SUBJECT[1], currentSelected, new ArrayList<Integer>( Arrays.asList(0, 1, 2, 3, 4, 5 ) ));    
     
   }
 
   @Override
   public void confirm(String subject) {
     if (DELETE_SERULE_SUBJECT[1].equals(subject)) {
       doDeleteSecurityRules();
     }
     
   }
 
   private void doDeleteSecurityRules() {
     if ( currentSelected == null || currentSelected.size( ) < 1 ) {
       return;
     }
     
     final ArrayList<String> groups = Lists.newArrayList( );
     final ArrayList<String> fromPorts = Lists.newArrayList( );
     final ArrayList<String> toPorts = Lists.newArrayList( );
     final ArrayList<String> protos = Lists.newArrayList( );
     final ArrayList<String> ipRanges = Lists.newArrayList( );
     for ( SearchResultRow row : currentSelected ) {
       groups.add( row.getField( 1 ) );
       fromPorts.add( row.getField( 2 ) );
       toPorts.add( row.getField( 3 ) );
       protos.add( row.getField( 4 ) );
       ipRanges.add( row.getField( 5 ) );
     }
     
     clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "删除安全规则中...", 0 );
     
     clientFactory.getBackendAwsService( ).delSecurityRules( clientFactory.getLocalSession( ).getSession( ), groups, fromPorts, toPorts, protos, ipRanges, new AsyncCallback<Void>( ) {
 
       @Override
       public void onFailure( Throwable caught ) {
         ActivityUtil.logoutForInvalidSession( clientFactory, caught );
         clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "删除安全规则失败", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
         clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "删除安全规则失败:" + caught.getMessage( ) );
       }
 
       @Override
       public void onSuccess( Void arg0 ) {
         clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "删除安全规则成功", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
         clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "删除安全规则成功" );
         reloadCurrentRange( );
         currentSelected = null;
       }
     });
 
     
   }
 
   @Override
   public void process(String subject, ArrayList<String> values) {
     if (CREATE_SERULE_SUBJECT[1].equals(subject)) {
       doCreateSecurityRule(values.get(0), values.get(1), values.get(2), values.get(3), values.get(4));
     }
     
   }
 
   private void doCreateSecurityRule(String group, String fromPort,
       String toPort, String proto, String ipRange) {
     this.clientFactory.getShellView().getFooterView().showStatus(StatusType.LOADING, "新建安全规则...", 0);
     this.clientFactory.getBackendAwsService().addSecurityRule(this.clientFactory.getLocalSession().getSession(), group, fromPort, toPort, proto, ipRange,  
         new AsyncCallback<Void>( ) {
           @Override
           public void onFailure( Throwable caught ) {
           ActivityUtil.logoutForInvalidSession( clientFactory, caught );
           clientFactory
           .getShellView()
           .getFooterView()
           .showStatus(StatusType.ERROR, "创建安全规则失败", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
           clientFactory
           .getShellView()
           .getLogView()
           .log(LogType.ERROR, "创建安全规则失败" + ": " + caught.getMessage());
           }
           
           @Override
           public void onSuccess( Void result ) {
             clientFactory
             .getShellView()
             .getFooterView()
             .showStatus(StatusType.NONE, "创建安全规则成功", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
             reloadCurrentRange();
           }
         } );    
 
     
   }
   
   
 }
