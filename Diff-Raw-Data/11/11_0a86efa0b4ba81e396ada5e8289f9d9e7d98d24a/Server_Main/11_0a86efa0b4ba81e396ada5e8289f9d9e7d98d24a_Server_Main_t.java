 package kickflick.gui;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import kickflick.device.device;
 import kickflick.utility.server;
 
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Menu;
 
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.MenuItem;
 
 public class Server_Main {
 
 	private server Server;
 	private Combo combo_port;
 
     final private Display display = new Display();
     final private Shell shlKickflickServer = new Shell( display );
 
 	private Runnable timer_;
     private final int time = 1500; //TODO make configurable
 	private Table DeviceTable;
 
 	/**
 	 * @wbp.parser.entryPoint
 	 */
 	public void open() {
 		//display = Display.getDefault();
 		shlKickflickServer.setMinimumSize(new Point(1, 23));
 		shlKickflickServer.setSize(575, 354);
 		shlKickflickServer.setLocation(750, 250);
 		shlKickflickServer.setText("KickFlick Server");
 		GridLayout gl_shlKickflickServer = new GridLayout(1, false);
 		gl_shlKickflickServer.marginHeight = 0;
 		gl_shlKickflickServer.marginWidth = 0;
 		shlKickflickServer.setLayout(gl_shlKickflickServer);
 
         shlKickflickServer.addShellListener(new ShellAdapter()
         {
             public void shellClosed(ShellEvent e) {
                 close_server();
             }
         });
 
 		Group grpConnection = new Group(shlKickflickServer, SWT.NONE);
 		grpConnection.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		grpConnection.setLayout(new GridLayout(4, false));
 		grpConnection.setText("Connection");
 
 		combo_port = new Combo(grpConnection, SWT.READ_ONLY);
 		combo_port.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 
 		final Combo combo_baut = new Combo(grpConnection, SWT.READ_ONLY);
 		combo_baut.setItems(new String[] {"300", "600", "1200", "2400", "4800", "9600", "14400", "19200", "28800", "38400", "57600", "115200"});
 		combo_baut.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		combo_baut.select(10);
 
 		Button btnRefresh = new Button(grpConnection, SWT.NONE);
 		btnRefresh.addMouseListener(new MouseAdapter() {
 			public void mouseDown(MouseEvent e) {
 				set_up();
 			}
 		});
 		btnRefresh.setText("Refresh");
 
 		final Button btnConnect = new Button(grpConnection, SWT.NONE);
 		GridData gd_btnConnect = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
 		gd_btnConnect.widthHint = 100;
 		gd_btnConnect.minimumWidth = 200;
 		btnConnect.setLayoutData(gd_btnConnect);
 		btnConnect.addMouseListener(new MouseAdapter() {
 			public void mouseDown(MouseEvent e) {
 				if ( Server != null && !Server.get_SerialCom().is_connected())
 				{
 					Server.connect_panstamp(combo_port.getItem(combo_port.getSelectionIndex()),
 										Integer.parseInt(combo_baut.getItem(combo_baut.getSelectionIndex())));
 					btnConnect.setText("Disconnect");
 				}
 				else
 				{
                     Server.get_SerialCom().exit();
 					btnConnect.setText("Connect");
 				}
 			}
 		});
 		btnConnect.setText("Connect");
 
 		Menu menu = new Menu(shlKickflickServer, SWT.BAR);
 		shlKickflickServer.setMenuBar(menu);
 		
 		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
 		mntmFile.setText("File");
 		
 		Menu menu_1 = new Menu(mntmFile);
 		mntmFile.setMenu(menu_1);
 		
 		MenuItem mntmLoad = new MenuItem(menu_1, SWT.NONE);
 		mntmLoad.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				try {
 					Server.loadDevicesFromFile("test");
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				} catch (ClassNotFoundException e2) {
                     e2.printStackTrace();
                 }
 			}
 		});
 		mntmLoad.setText("Load");
 		
 		MenuItem mntmSave = new MenuItem(menu_1, SWT.NONE);
 		mntmSave.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				try {
 					Server.saveDevicesToFile("test");
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		});
 		mntmSave.setText("Save");
 		
 		MenuItem mntmExit = new MenuItem(menu_1, SWT.NONE);
 		mntmExit.setText("Exit");
 
         mntmExit.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
                 close_server();
                shlKickflickServer.close();
             }
         });
 
 
 		Group grpDevices = new Group(shlKickflickServer, SWT.NONE);
 		grpDevices.setText("Devices");
 		grpDevices.setLayout(new GridLayout(1, false));
 		grpDevices.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
 		
         DeviceTable = new Table( grpDevices, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION );
         DeviceTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
         DeviceTable.setLinesVisible(true);
         DeviceTable.setHeaderVisible(true);
 
 				
         TableColumn tblclmnNewColumn = new TableColumn(DeviceTable, SWT.NONE);
         tblclmnNewColumn.setWidth(248);
         tblclmnNewColumn.setText("Personality");
 						
         TableColumn tblclmnNewColumn_1 = new TableColumn(DeviceTable, SWT.CENTER);
         tblclmnNewColumn_1.setWidth(100);
         tblclmnNewColumn_1.setText("State");
 								
         TableColumn tblclmnNewColumn_2 = new TableColumn(DeviceTable, SWT.CENTER);
         tblclmnNewColumn_2.setWidth(100);
         tblclmnNewColumn_2.setText("last seen");
 
         TableColumn tblclmnNewColumn_3 = new TableColumn(DeviceTable, SWT.CENTER);
         tblclmnNewColumn_3.setWidth(100);
         tblclmnNewColumn_3.setText("Battery");
 
         DeviceTable.pack();
 
 		if ( this.Server.get_devices().size() > 0 )
 		{
 			for ( int i = 0; i < this.Server.get_devices().size() ; ++i)
 			{
 				TableItem tableItem = new TableItem(DeviceTable, SWT.NONE,i);
 				tableItem.setText(new String[] {
 													this.Server.get_device(0).get_Personality().get_Name(),
 													this.Server.get_device(0).get_Personality().get_state_name()
 												});
 			}
 		}
 		
 		Composite composite = new Composite(grpDevices, SWT.NONE);
 		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
 		composite.setLayout(new GridLayout(4, true));
 		
 		Button btnDeleteAll = new Button(composite, SWT.NONE);
 		btnDeleteAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		btnDeleteAll.setText("Delete All");
 		btnDeleteAll.addMouseListener(new MouseAdapter() {
 			public void mouseDown(MouseEvent e) {
 				Server.get_devices().clear();
 			}
 		});
 		
 		Button btnDelete = new Button(composite, SWT.NONE);
 		btnDelete.addMouseListener(new MouseAdapter() {
 			public void mouseDown(MouseEvent e) {
 				if ( DeviceTable.getSelectionIndex() >= 0)
 					Server.get_devices().remove(DeviceTable.getSelectionIndex());
 			}
 		});
 		btnDelete.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		btnDelete.setText("Delete");
 		
 		
 		
         Button Config_btn = new Button(composite, SWT.NONE);
         Config_btn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
         Config_btn.setText("Config");
         Config_btn.addMouseListener(new MouseAdapter()
         {
             public void mouseDown(MouseEvent e) {
 
                 if ( DeviceTable.getSelectionIndex() >= 0)
                 {
                     int index = DeviceTable.getSelectionIndex();
                     Device_Config_Dialog tmp = new Device_Config_Dialog(
                             shlKickflickServer,
                             SWT.APPLICATION_MODAL,
                             Server.get_device(index),
                             Server.get_PersonalitiesCount()
                     );
                     device tmp_dev =  (device) tmp.open();
                     Server.set_device(index,tmp_dev);
                     Server.send_device(index);
                 }
             }
         });
 				
 				
         Button newDevice = new Button(composite, SWT.NONE);
         newDevice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
         newDevice.setText("New Device");
         newDevice.addMouseListener(new MouseAdapter() {
             public void mouseDown(MouseEvent e) {
                 Server.get_devices().add(new kickflick.device.device());
             }
         });
 
 		set_up();
 
         init_timer();
 
         display.timerExec(0,this.timer_);
 
 		shlKickflickServer.open();
 		shlKickflickServer.layout();
 		while (!shlKickflickServer.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	private void set_up()
 	{
 		java.util.List<String> port_list  = Server.get_SerialCom().get_port_names();
 		getCombo_port().setItems(port_list.toArray(new String[port_list.size()]));
 		getCombo_port().select(0);
 	}
 
     private void init_timer()
     {
         this.timer_ = new Runnable() {
             public void run()
             {
                 //DeviceTable.clearAll();
                 int selection = DeviceTable.getSelectionIndex();
                 List<device> list = Server.get_devices();
                 DeviceTable.removeAll();
 
 
                 int i = 0;
                 for ( device d : list)
                 {
                     TableItem tableItem = new TableItem(DeviceTable, SWT.NONE,i);   //TODO evtl. tabelle vorher löschen
                     tableItem.setText(new String[] {
                             d.get_Personality().get_Name(),
                             d.get_Personality().get_state_name(),
                             new SimpleDateFormat("HH:mm:ss").format(d.get_timestamp_last_heard_of()),
                             d.get_battery_state()
                     });
                     if( d.is_battery_low() ) {
                         MessageBox alert = new MessageBox( shlKickflickServer, SWT.RESIZE | SWT.ICON_WARNING | SWT.OK );
                         alert.setMessage("Battery low on device "+d.get_Personality().get_Name());
                         alert.open();
                     }
                     if( d.get_Personality().get_State() == -1 ) {
                         MessageBox alert = new MessageBox( shlKickflickServer, SWT.RESIZE | SWT.ICON_WARNING | SWT.OK );
                         alert.setMessage("Didn't receive signal from "+d.get_Personality().get_Name()+" for 2 minutes" );
                         alert.open();
                     }
                     ++i;
                 }
                 try{
                     DeviceTable.setSelection(selection);
                 }
                 finally{}
 
                 display.timerExec(time, this);
             }
         };
     }
 
 	public Combo getCombo_port() {
 		return combo_port;
 	}
 
 	protected Table getDeviceTable() {
 		return DeviceTable;
 	}
 
     private server get_server()
     {
         return this.Server;
     }
 
     public void set_Server(kickflick.utility.server serv)
     {
         this.Server = serv;
     }
 
     private void close_server() {
         if ( Server.get_SerialCom().is_connected())
             Server.disconnect_pannstamp();
         display.timerExec(-1,timer_); //stops timer
         Server.stop_timer();
     }
 }
 
 
