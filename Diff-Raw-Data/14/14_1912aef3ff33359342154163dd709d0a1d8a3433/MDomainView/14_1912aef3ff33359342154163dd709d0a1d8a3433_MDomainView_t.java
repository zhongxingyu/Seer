 package net.jumperz.app.MDozens.view;
 
 import java.util.*;
 import java.util.List;
 
 import net.jumperz.app.MDozens.MContext;
 import net.jumperz.app.MDozens.api.MSession;
 import net.jumperz.app.MDozens.dialog.MLoginDialog;
 
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.jface.action.*;
 import org.eclipse.jface.dialogs.DialogMessageArea;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.custom.TableEditor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.swt.*;
 import org.eclipse.swt.layout.*;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.*;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import net.jumperz.util.*;
 
 public class MDomainView
 extends MAbstractView
 implements MObserver1
 {
 private Action loginAction;
 private MContext context = MContext.getInstance();
 
 public static MDomainView instance;
 //--------------------------------------------------------------------------------
 public MDomainView()
 {
 instance = this;
 context.register1( instance );
 }
 //--------------------------------------------------------------------------------
 public void init2()
 {
 parent.setLayout( new FormLayout() );
 
table = new Table( parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI );
 table.addMouseListener(new MouseAdapter() {
 	@Override
 	public void mouseDown(MouseEvent e) {
 	onMouseDown( e );
 	}
 });
 table.setHeaderVisible( true );
 table.setLinesVisible( true );
 
 table.addListener( SWT.Selection, this );
 
 FormData d1 = new FormData();
 d1.top = new FormAttachment(0);
 d1.left = new FormAttachment( 0, 1 );
 d1.right = new FormAttachment( 100, -1 );
 d1.bottom = new FormAttachment( 100, -1 );
 table.setLayoutData( d1 );
 
 menuManager = new MenuManager();
 Menu contextMenu = menuManager.createContextMenu( table );
 table.setMenu( contextMenu );
 
 loginAction = new Action(){ public void run() {
 login();
 }};
 loginAction.setText( "Login" );
 initAction( loginAction, "server_lightning.png", menuManager );
 }
 //--------------------------------------------------------------------------------
 private void login()
 {
 final Map dialogData = new HashMap();
 MLoginDialog dialog = new MLoginDialog( shell, dialogData );
 dialog.open();
 
 if( dialogData.size() > 0 )
 	{
 	(new Thread(){ public void run()
 	{
 	context.login( ( String )dialogData.get( "user" ), ( String )dialogData.get( "apiKey" ) );
 	}}).start();
 	}
 }
 //--------------------------------------------------------------------------------
 private void onMouseDown( MouseEvent e )
 {
 
 }
 //--------------------------------------------------------------------------------
 protected void onTableSelect()
 {
 TableItem[] items = table.getSelection();
 if( items.length == 1 )
 	{
 	int selectedIndex = table.getSelectionIndex();
 	Map domainData = ( Map )items[ 0 ].getData();
 	context.onDomainSelect( domainData );
 	}
 }
 //--------------------------------------------------------------------------------
 public void setFocus()
 {
 }
 //--------------------------------------------------------------------------------
 private void onZone( final Map zoneMap )
 {
 shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//-----
 
 clearTableSwt();
 
 List domainList = ( List )zoneMap.get( "domain" );
 if( domainList.size() > 0 )
 	{
 		//draw table column
 	TableColumn column = new TableColumn( table, SWT.NONE );
 	column.setText( "id" );
 	column.setWidth( 100 );
 	
 	column = new TableColumn( table, SWT.NONE );
 	column.setText( "name" );
 	column.setWidth( 100 );
 	
 	for( int i = 0; i < domainList.size(); ++i )
 		{
 		Map domainData = ( Map )domainList.get( i );
 		TableItem item = new TableItem( table, SWT.NONE );
 		item.setText( 0, ( String )domainData.get( "id" ) );
 		item.setText( 1, ( String )domainData.get( "name" ) );
 		item.setData( domainData );
 		}
 	}
 
 }});//-----
 }
 //--------------------------------------------------------------------------------
 public void update()
 {
 int state = context.getState();
 if( state == STATE_ZONE_SUCCESS )
 	{
 	onZone( context.getZoneMap() );
 	}
 }
 //--------------------------------------------------------------------------------
 }
