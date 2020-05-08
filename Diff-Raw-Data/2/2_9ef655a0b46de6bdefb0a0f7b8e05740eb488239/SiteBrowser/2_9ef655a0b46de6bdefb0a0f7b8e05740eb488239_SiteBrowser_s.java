 package com.antimatterstudios.esftp.ui.widgets;
 
 import org.eclipse.swt.widgets.*;
 
 import com.antimatterstudios.esftp.Activator;
 import com.antimatterstudios.esftp.Transfer;
 import com.antimatterstudios.esftp.TransferDetails;
 import com.antimatterstudios.esftp.ui.UserInterface;
 import com.antimatterstudios.esftp.ui.widgets.Directory;
 import com.antimatterstudios.esftp.ui.widgets.SiteBrowserLabelProvider;
 
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.swt.SWT;
 
 import java.util.Iterator;
 
 public class SiteBrowser extends Composite
 {
 	protected String m_siteRoot;
 	
 	protected String m_selectedSiteRoot;
 	
 	protected TreeViewer m_treeViewer;
 	
 	protected Directory m_root;
 	
 	protected SiteBrowserContentProvider m_contentProvider;
 	
 	protected SiteBrowserLabelProvider m_labelProvider;
 	
 	protected UserInterface m_userInterface;
 	
 	protected Transfer m_transfer;
 	
 	protected TransferDetails m_details;
 	
 	protected Button m_connect;
 	
 	protected Button m_disconnect;
 	
 	protected Button m_selectDirectory;
 	
 	protected Button m_cancel;
 	
 	protected Listener m_connectCallback;
 	
 	protected Listener m_disconnectCallback;
 	
 	protected Listener m_selectCallback;
 	
 	protected Listener m_cancelCallback;
 	
 	protected Text m_progress;
 	
 	protected int m_totalFiles;
 	
 	protected int m_totalFolders;
 	
 	protected void createCallbacks()
 	{
 		m_connectCallback = new Listener(){
 			public void handleEvent(Event e){
 				updateProgress("QUERYING....",-1,-1);
 				m_userInterface.updateStore();
 				m_transfer = Activator.getDefault().getTransfer(m_details);
 				m_transfer.init(m_details);
 				if(m_transfer.open()){
 					m_root = new Directory("/");
 					m_treeViewer.setInput(m_root);		
 					m_root.expand(m_details.getSiteRoot(), m_treeViewer);
 					updateProgress("COMPLETE!",0,0);
 				}
 			}
 		};
 		
 		m_disconnectCallback = new Listener(){
 			public void handleEvent(Event e){
 				setDisconnected();
 			}
 		};
 		
 		m_selectCallback = new Listener(){
 			public void handleEvent(Event e){
 				m_userInterface.showTestInterface();
 			}
 		};
 		
 		m_cancelCallback = new Listener(){
 			public void handleEvent(Event e){
 				setDisconnected();
 				m_userInterface.showTestInterface();
 			}
 		};
 	}
 	
 	protected void createButtons()
 	{
 		createCallbacks();
 		
 		m_connect = new Button(this, SWT.NONE);
 		m_connect.setText("Connect");
 		m_connect.addListener(SWT.Selection, m_connectCallback);
 		m_connect.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
 		
 		m_disconnect = new Button(this, SWT.NONE);
 		m_disconnect.setText("Disconnect");
 		m_disconnect.addListener(SWT.Selection, m_disconnectCallback);
 		m_disconnect.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
 		
 		m_selectDirectory = new Button(this, SWT.NONE);
 		m_selectDirectory.setText("Select Directory");
 		m_selectDirectory.addListener(SWT.Selection, m_selectCallback);
 		m_selectDirectory.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
 		
 		m_cancel = new Button(this, SWT.NONE);
 		m_cancel.setText("Cancel");
 		m_cancel.addListener(SWT.Selection, m_cancelCallback);
 		m_cancel.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
 	}
 	
 	public SiteBrowser(Composite parent, int style, UserInterface ui)
 	{
 		super(parent,style);
 		
 		m_userInterface = ui;
 		m_details = new TransferDetails(m_userInterface.getPreferences());
 		m_transfer = null;
 		
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 5;
 		
 		setLayout(layout);
 		setLayoutData(new GridData(GridData.FILL_BOTH));
 		
 		m_contentProvider = new SiteBrowserContentProvider(this);
 		m_labelProvider = new SiteBrowserLabelProvider();
 		
 		new Label(this,SWT.NONE);
 		
 		m_treeViewer = new TreeViewer(this, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
 		m_treeViewer.setContentProvider(m_contentProvider);
 		m_treeViewer.setLabelProvider(m_labelProvider);
 		m_treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
 		
 		setDisconnected();
 		
 		System.out.println("ADDING THE PROGRESS TEXT ENTRY");
 		new Label(this,SWT.NONE).setText("Progress: ");
 		m_progress = new Text(this, SWT.BORDER | SWT.READ_ONLY);
 		m_progress.setLayoutData(new GridData(SWT.FILL,SWT.NONE,true,false,4,1));
 		
 		new Label(this,SWT.NONE);
 		
 		createButtons();
 		
 		m_treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				// if the selection is empty clear the label
 				if(event.getSelection().isEmpty()) {
 					System.out.println("Clearing selection");
 					return;
 				}
 				if(event.getSelection() instanceof IStructuredSelection) {
 					IStructuredSelection selection = (IStructuredSelection)event.getSelection();
 
 					for (Iterator i = selection.iterator(); i.hasNext();) {
 						Model item = (Model) i.next();
 						String selected = m_labelProvider.getText(item);
 					
 						if(selected != "<Disconnected>"){
							selected = m_details.getSiteRoot()+item.getFullName();
 							
 							m_userInterface.setSiteRoot(selected);
 						}
 						System.out.println("Selected treeview item = "+selected);				
 						return;
 					}
 				}
 			}
 		});
 	}
 	
 	public void setVisible(boolean visible)
 	{
 		super.setVisible(visible);
 		
 		m_siteRoot = m_userInterface.getSiteRoot();
 	}
 	
 	public Directory setDisconnected()
 	{
 		m_root = new Directory();
 		
 		m_root.addFolder(new Directory("<Disconnected>"));
 		
 		if(m_transfer != null) m_transfer.close();
 		m_treeViewer.setInput(m_root);
 
 		return m_root;
 	}
 	
 	public Transfer getTransfer()
 	{
 		return m_transfer;
 	}
 	
 	public void updateProgress(String item, int file, int folder)
 	{
 		m_totalFiles = (file != -1) ? m_totalFiles+file : 0;
 		m_totalFolders = (file != -1) ? m_totalFolders+folder : 0;
 		
 		if(m_progress != null){
 			m_progress.setText("Processing: "+item+"    files ("+m_totalFiles+"), folders (" +m_totalFolders+")");
 			m_progress.update();
 		}
 	}
 }
