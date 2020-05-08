 /*
  * Project: xdccBee
  * Copyright (C) 2009 snert@snert-lab.de,
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.snertlab.xdccBee.ui;
 
 import java.text.MessageFormat;
 import java.util.List;
 
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Tree;
 
 import de.snertlab.xdccBee.irc.DccDownload;
 import de.snertlab.xdccBee.irc.DccPacket;
 import de.snertlab.xdccBee.irc.IrcChannel;
 import de.snertlab.xdccBee.irc.IrcServer;
 import de.snertlab.xdccBee.irc.ServerList;
 import de.snertlab.xdccBee.irc.listener.INotifyDccDownload;
 import de.snertlab.xdccBee.irc.listener.INotifyIrcServerEditNew;
 import de.snertlab.xdccBee.irc.listener.NotifyManagerDccDownload;
 import de.snertlab.xdccBee.irc.listener.NotifyManagerIrcServerEditNew;
 import de.snertlab.xdccBee.messages.XdccBeeMessages;
 import de.snertlab.xdccBee.tools.AutoResizeTableLayout;
 import de.snertlab.xdccBee.tools.MyMessageDialog;
 import de.snertlab.xdccBee.ui.actions.ActionConnectIrcServer;
 import de.snertlab.xdccBee.ui.actions.ActionDisconnectIrcServer;
 import de.snertlab.xdccBee.ui.actions.ActionEditIrcChannel;
 import de.snertlab.xdccBee.ui.actions.ActionEditIrcServer;
 import de.snertlab.xdccBee.ui.actions.ActionNewIrcChannelContextMenu;
 import de.snertlab.xdccBee.ui.actions.ActionNewIrcServer;
 import de.snertlab.xdccBee.ui.actions.ActionRemoveIrcChannelContextMenu;
 import de.snertlab.xdccBee.ui.actions.ActionRemoveIrcServerContextMenu;
 
 
 /**
  * @author snert
  *
  */
 public class ViewMain implements INotifyIrcServerEditNew, INotifyDccDownload {
 	
 	private IrcTreeViewer ircTreeViewer;
 	private FileViewer packetViewer;
 	private InfoTabFolder infoTabFolder;
 	private TableViewer tblViewerDownloadQueue;
 		
 	public void createContents(Composite parent){
 		createContentsView(parent);
 		makeTreeMenu();
 		NotifyManagerIrcServerEditNew.getNotifyManager().register(this);
 		NotifyManagerDccDownload.getNotifyManager().register(this);
 		connectAllAutoconnectServer();
 	}
 
 	public void createContentsView(Composite parent) {
 		
 		Composite comp = new Composite(parent, SWT.NONE);
 		FormLayout layout = new FormLayout();
 		layout.marginWidth = 5;
 		layout.marginHeight = 5;
 		comp.setLayout( layout );
 
 		FormData data = new FormData();
 		data.top = new FormAttachment(0, 0);
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0);
 		data.bottom = new FormAttachment(100, 0);
 
 		comp.setLayoutData(data);
 
 		FormData data2 = new FormData();
 		data2.top = new FormAttachment(0, 0);
 		data2.left = new FormAttachment(0, 0);
 		data2.right = new FormAttachment(25, 0);
 		data2.bottom = new FormAttachment(60, 0);
 		
 		ircTreeViewer = new IrcTreeViewer(comp, SWT.BORDER);
 		Tree treeIrcServer = ircTreeViewer.getTree();
 		treeIrcServer.setLayoutData(data2);
 		//ircTreeViewer.addSelectionChangedListener(new IrcTreeViewerSelectionChangedListener()); 
 		
 		FormData data3 = new FormData();
 		data3.top = new FormAttachment(0, 31);
 		data3.left = new FormAttachment(treeIrcServer, 5);
 		data3.right = new FormAttachment(100, 0);
 		data3.bottom = new FormAttachment(60, 0);
 
 		packetViewer = new FileViewer(comp, SWT.BORDER | SWT.FULL_SELECTION);
 		packetViewer.getTable().setLayoutData(data3);
 		
 		FormData data3b = new FormData();
 		data3b.top = new FormAttachment(0, 0);
 		data3b.left = new FormAttachment(treeIrcServer, 5);
 		data3b.right = new FormAttachment(100, 0);
 		data3b.bottom = new FormAttachment(packetViewer.getTable(), 5);
 
 		//TODO Sollte ohne Übergabe von PacketViewwer funktionieren
 		new FileFilterComposite(comp, data3b, packetViewer);
 		
 		FormData data4 = new FormData();
 		data4.top = new FormAttachment(treeIrcServer,5);
 		data4.left = new FormAttachment(0, 0);
 		data4.right = new FormAttachment(100, 0);
 		data4.bottom = new FormAttachment(100, 0);
 
 		infoTabFolder = new InfoTabFolder(comp, SWT.BORDER);
 		infoTabFolder.setLayoutData(data4);
 		
 		CTabItem tabItemQueue = new CTabItem(infoTabFolder, SWT.NONE);
 		tabItemQueue.setText(XdccBeeMessages.getString("ViewMain_TAB_DOWNLOADS")); //$NON-NLS-1$
 		Composite compQueue = new Composite(infoTabFolder, SWT.NONE);
 		compQueue.setLayout(new GridLayout());
 		compQueue.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true) );
 		tblViewerDownloadQueue = new TableViewer(compQueue, SWT.BORDER);
 		Table tblDownloadQueue = tblViewerDownloadQueue.getTable();
 		AutoResizeTableLayout autoTableLayout = new AutoResizeTableLayout(tblDownloadQueue);
 		autoTableLayout.addColumnData(new ColumnWeightData(30));
 		autoTableLayout.addColumnData(new ColumnWeightData(20));
 		autoTableLayout.addColumnData(new ColumnWeightData(30));
 		autoTableLayout.addColumnData(new ColumnWeightData(10));
 		autoTableLayout.addColumnData(new ColumnWeightData(10));
 		TableColumn column1 = new TableColumn(tblDownloadQueue, SWT.NONE);
 		TableColumn column2 = new TableColumn(tblDownloadQueue, SWT.NONE);
 		TableColumn column3 = new TableColumn(tblDownloadQueue, SWT.NONE);
 		TableColumn column4 = new TableColumn(tblDownloadQueue, SWT.NONE);
 		TableColumn column5 = new TableColumn(tblDownloadQueue, SWT.NONE);
 		column1.setText(XdccBeeMessages.getString("ViewMain_TABLE_DOWNLOADS_COL_PACKET")); //$NON-NLS-1$
 		column2.setText(XdccBeeMessages.getString("ViewMain_TABLE_DOWNLOADS_COL_PROGRESS")); //$NON-NLS-1$
 		column3.setText("Rate");
		column4.setText("Dauer");
 		column5.setText("Status");
 		tblDownloadQueue.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true) );
 		tblDownloadQueue.setHeaderVisible(true);
 		tblDownloadQueue.setLinesVisible(true);
 
 		
 		tabItemQueue.setControl(compQueue);
 		if(infoTabFolder.getItemCount()!=0) infoTabFolder.setSelection(0);
 		
 	}
 	
 //	private class IrcTreeViewerSelectionChangedListener implements ISelectionChangedListener{
 //		@Override
 //		public void selectionChanged(SelectionChangedEvent arg0) {			
 //			TreeSelection selection = (TreeSelection) arg0.getSelection();
 //			if(selection.getFirstElement() instanceof IrcServer) {
 //				//TODO Über ContenLabelProvider realisieren
 //				selectedIrcServer = (IrcServer) selection.getFirstElement();
 //				packetViewer.setInput(selectedIrcServer.getListDccPackets());
 //				// TODO Logic Channelfilter stimmt noch nicht
 //				packetViewer.disableChannelFilter();
 //			} else if(selection.getFirstElement() instanceof IrcChannel) {
 //				packetViewer.enableChannelFilter();
 //			}
 //		}
 //	}
 
 	
 	public Object getSelectedTreeItem() {
 		IStructuredSelection selectedItem = (IStructuredSelection) ircTreeViewer.getSelection();
 		return selectedItem.getFirstElement();
 	}
 
 	public DccPacket getSelectedDccPacket() {
 		return packetViewer.getSelectedDccPacket();
 	}
 
 	private void makeTreeMenu(){
 		
 		Menu mnuIrcServer = new Menu(ircTreeViewer.getTree());
 		ircTreeViewer.getTree().setMenu(mnuIrcServer);
 
 		final MenuItem mntmAddNewItems = new MenuItem(mnuIrcServer, SWT.CASCADE);
 		mntmAddNewItems.setText(XdccBeeMessages.getString("ViewMain_MENUITEM_ADD")); //$NON-NLS-1$
 		
 		final Menu menu_1 = new Menu(mntmAddNewItems);
 		mntmAddNewItems.setMenu(menu_1);
 		
 		final MenuItem mntmAddServer = new MenuItem(menu_1, SWT.NONE);
 		mntmAddServer.setText(XdccBeeMessages.getString("ViewMain_MENUITEM_SERVER")); //$NON-NLS-1$
 		mntmAddServer.addSelectionListener( new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				addNewIrcServer();
 			}
 		});
 		
 		final MenuItem mntmAddChannel = new MenuItem(menu_1, SWT.NONE);
 		mntmAddChannel.setText(XdccBeeMessages.getString("ViewMain_MENUITEM_CHANNEL")); //$NON-NLS-1$
 		mntmAddChannel.addSelectionListener( new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				addNewIrcChannel();
 			}
 		});
 		
 		final MenuItem mntmEdit = new MenuItem(mnuIrcServer, SWT.NONE);
 		mntmEdit.setText(XdccBeeMessages.getString("ViewMain_MENUITEM_EDIT")); //$NON-NLS-1$
 		mntmEdit.addSelectionListener( new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if(getSelectedTreeItem() instanceof IrcServer){
 					editIrcServer();
 				}else if(getSelectedTreeItem() instanceof IrcChannel){
 					editIrcChannel();
 				}
 			}
 		});
 		
 		final MenuItem mntmRemove = new MenuItem(mnuIrcServer, SWT.NONE);
 		mntmRemove.setText(XdccBeeMessages.getString("ViewMain_MENUITEM_REMOVE")); //$NON-NLS-1$
 		mntmRemove.addSelectionListener( new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if(getSelectedTreeItem() instanceof IrcServer){
 					removeIrcServer();
 				}else if(getSelectedTreeItem() instanceof IrcChannel){
 					removeIrcChannel();
 				}
 			}
 		});
 		
 		new MenuItem(mnuIrcServer, SWT.SEPARATOR);
 
 		final MenuItem mntmVerbinden = new MenuItem(mnuIrcServer, SWT.NONE);
 		mntmVerbinden.setText(XdccBeeMessages.getString("ViewMain_MENUITEM_CONNECT")); //$NON-NLS-1$
 		mntmVerbinden.addSelectionListener( new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if(getSelectedTreeItem() instanceof IrcServer){
 					connectIrcServer();
 				}else if(getSelectedTreeItem() instanceof IrcChannel){
 					IrcChannel ircChannel = (IrcChannel)getSelectedTreeItem();
 					ircChannel.connect();
 				}
 			}
 		});
 		
 		final MenuItem mntmTrennen = new MenuItem(mnuIrcServer, SWT.NONE);
 		mntmTrennen.setText(XdccBeeMessages.getString("ViewMain_MENUITEM_DISCONNECT")); //$NON-NLS-1$
 		mntmTrennen.addSelectionListener( new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if(getSelectedTreeItem() instanceof IrcServer){
 					disconnectIrcServer();
 				}else if(getSelectedTreeItem() instanceof IrcChannel){
 					IrcChannel ircChannel = (IrcChannel)getSelectedTreeItem();
 					ircChannel.disconnect();
 				}
 			}
 		});
 		
 		new MenuItem(mnuIrcServer, SWT.SEPARATOR);
 		
 		final MenuItem mntmShowAllPackets = new MenuItem(mnuIrcServer, SWT.NONE);
 		mntmShowAllPackets.setText(XdccBeeMessages.getString("ViewMain_MENUITEM_SHOW_ALL_PACKETS")); //$NON-NLS-1$
 		mntmShowAllPackets.addSelectionListener( new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				packetViewer.setFilterAll();
 			}
 		});
 
 		final MenuItem mntmShowServerPackets = new MenuItem(mnuIrcServer, SWT.NONE);
 		mntmShowServerPackets.setText(XdccBeeMessages.getString("ViewMain_MENUITEM_SHOW_ONLY_CHANNEL_PACKETS")); //$NON-NLS-1$
 		mntmShowServerPackets.addSelectionListener( new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if(getSelectedTreeItem() instanceof IrcServer){
 					packetViewer.setFilterIrcServer((IrcServer)getSelectedTreeItem());
 				}else if(getSelectedTreeItem() instanceof IrcChannel){
 					packetViewer.setFilterIrcServer(((IrcChannel)getSelectedTreeItem()).getIrcServer());
 				}
 			}
 		});
 		
 		final MenuItem mntmShowChannelPackets = new MenuItem(mnuIrcServer, SWT.NONE);
 		mntmShowChannelPackets.setText(XdccBeeMessages.getString("ViewMain_MENUITEM_SHOW_CHANNEL_PACKETS")); //$NON-NLS-1$
 		mntmShowChannelPackets.addSelectionListener( new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if(getSelectedTreeItem() instanceof IrcChannel){
 					packetViewer.setFilterIrcChannel((IrcChannel)getSelectedTreeItem());
 				}
 			}
 		});
 
 		mnuIrcServer.addListener(SWT.Show, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				if( getSelectedTreeItem() == null){
 					mntmAddChannel.setEnabled(false);
 					mntmEdit.setEnabled(false);
 					mntmRemove.setEnabled(false);
 					mntmTrennen.setEnabled(false);
 					mntmVerbinden.setEnabled(false);
 					mntmShowAllPackets.setEnabled(ServerList.isAnyServerConnected());
 					mntmShowServerPackets.setEnabled(false);
 					mntmShowChannelPackets.setEnabled(false);
 				}else if( getSelectedTreeItem() instanceof IrcServer){
 					IrcServer ircServer = (IrcServer)getSelectedTreeItem();
 					mntmAddChannel.setEnabled(true);
 					mntmEdit.setEnabled(true);
 					mntmRemove.setEnabled(true);
 					mntmShowAllPackets.setEnabled(ServerList.isAnyServerConnected());
 					mntmShowChannelPackets.setEnabled(false);
 					if( ircServer.isConnected() || ircServer.isConnecting() ){
 						mntmVerbinden.setEnabled(false);
 					}else{
 						mntmVerbinden.setEnabled(true);
 					}
 					if( ircServer.isConnected() ){
 						mntmTrennen.setEnabled(true);
 						mntmShowServerPackets.setEnabled(true);
 					}else{						
 						mntmTrennen.setEnabled(false);
 						mntmShowServerPackets.setEnabled(false);
 					}
 				}else if( getSelectedTreeItem() instanceof IrcChannel){
 					IrcChannel ircChannel = (IrcChannel)getSelectedTreeItem();
 					mntmAddChannel.setEnabled(true);
 					mntmEdit.setEnabled(true);
 					mntmRemove.setEnabled(true);
 					mntmShowAllPackets.setEnabled(ServerList.isAnyServerConnected());
 					mntmShowServerPackets.setEnabled( ircChannel.getIrcServer().isConnected() );
 					if( ircChannel.isConnected() || ! ircChannel.getIrcServer().isConnected() ){
 						mntmVerbinden.setEnabled(false);
 					}else{
 						mntmVerbinden.setEnabled(true);
 					}
 					if( ircChannel.isConnected() ){
 						mntmShowChannelPackets.setEnabled(true);
 						mntmTrennen.setEnabled(true);
 					}else{
 						mntmShowChannelPackets.setEnabled(false);
 						mntmTrennen.setEnabled(false);
 					}
 
 				}else{
 					throw new RuntimeException("Undefined state for getSelectedTreeItem()"); //$NON-NLS-1$
 				}
 			}
 		});
 	}
 	private void removeIrcServer() {
 		IrcServer ircServer = (IrcServer) getSelectedTreeItem();
 		boolean isOk = MyMessageDialog.openConfirm(Display.getCurrent().getActiveShell(), XdccBeeMessages.getString("ViewMain_CONFIRM_TITLE_REMOVE_SERVER"), MessageFormat.format(XdccBeeMessages.getString("ViewMain_CONFIRM_MESSAGE_REMOVE_SERVER"), new Object[]{ircServer.getHostname()})); //$NON-NLS-1$ //$NON-NLS-2$
 		if(isOk){
 			new ActionRemoveIrcServerContextMenu(ircServer).run();
 		}
 	}
 
 	private void removeIrcChannel() {
 		IrcChannel ircChannel = (IrcChannel) getSelectedTreeItem();
 		boolean isOk = MyMessageDialog.openConfirm(Display.getCurrent().getActiveShell(), XdccBeeMessages.getString("ViewMain_CONFIRM_TITLE_REMOVE_CHANNEL"), MessageFormat.format(XdccBeeMessages.getString("ViewMain_CONFIRM_MESSAGE_REMOVE_CHANNEL"), new Object[]{ircChannel.getChannelName()}));
 		if(isOk){
 			new ActionRemoveIrcChannelContextMenu(ircChannel).run();
 		}
 	}
 
 	private void editIrcServer() {
 		new ActionEditIrcServer(Display.getCurrent().getActiveShell(), (IrcServer)getSelectedTreeItem()).run();
 	}
 	
 	private void editIrcChannel() {
 		new ActionEditIrcChannel(Display.getCurrent().getActiveShell(), (IrcChannel)getSelectedTreeItem()).run();
 	}
 
 	private void addNewIrcServer() {
 		new ActionNewIrcServer(Display.getCurrent().getActiveShell()).run();
 	}
 	
 	private void addNewIrcChannel() {
 		IrcServer ircServer = null;
 		if(getSelectedTreeItem() instanceof IrcServer){
 			ircServer = (IrcServer) getSelectedTreeItem();
 		}else{
 			ircServer = ((IrcChannel) getSelectedTreeItem()).getIrcServer();
 		}
 		new ActionNewIrcChannelContextMenu(Display.getCurrent().getActiveShell(), ircServer).run();
 	}
 	
 	private void connectIrcServer() {
 		new ActionConnectIrcServer((IrcServer)getSelectedTreeItem()).run();
 	}
 	
 	private void disconnectIrcServer() {
 		new ActionDisconnectIrcServer((IrcServer)getSelectedTreeItem()).run();		
 	}
 	
 	private void connectAllAutoconnectServer(){
 		List<IrcServer> listIrcServer = Application.getServerSettings().getListServer();
 		for (IrcServer ircServer : listIrcServer) {
 			if(ircServer.isAutoconnect()) ircServer.connect();
 		}
 	}
 	
 	@Override
 	public void notifyIrcServerEditNew(IrcServer ircServer) {
 		Object[] expandedElements = ircTreeViewer.getExpandedElements();
 		ircTreeViewer.setInput(Application.getServerSettings().getListServer());
 		ircTreeViewer.setExpandedElements(expandedElements);
 	}
 
 	@Override
 	public void notifyNewDccDownload(final DccDownload dccDownload) {
 		TableItemDownloadThread tableItemDownloadThread = new TableItemDownloadThread(tblViewerDownloadQueue.getTable(), dccDownload);
 		tableItemDownloadThread.start();
 	}
 }
