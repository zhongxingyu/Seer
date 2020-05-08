 package herbstJennrichLehmannRitter.ui.GUI;
 
 import herbstJennrichLehmannRitter.engine.Globals;
 import herbstJennrichLehmannRitter.server.NetworkServer;
 import herbstJennrichLehmannRitter.server.impl.NetworkServerWrapper;
 import herbstJennrichLehmannRitter.ui.RMIUsertInterface;
 import herbstJennrichLehmannRitter.ui.impl.ClientUserInterface;
 import herbstJennrichLehmannRitter.ui.impl.RMIUserInterfaceImpl;
 
 import java.rmi.RemoteException;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 
 /**	Description of ClientMenuGUI Class
  * Implementation of the Game as Client with the possibility to enter an IP-Adress
  */
 
 public class ClientMenuGUI extends AbstractMagicGUIElement {
 
 	private Text ipTextField;
 	private Label ipTextLabel;
 	private Button connectButton;
 	private Button backButton;
 	
 	private MainMenuGUI mainMenuGUI;
 	private Timer timer;
 	private PlayGameGUI playGameGUI;
 	
 	private RMIUsertInterface rmi;
 	
 	public ClientMenuGUI(Display parent, MainMenuGUI mainMenuGUI) {
 		super(parent);
 		this.mainMenuGUI = mainMenuGUI;
 		initGUI();
 	}
 	
 	@Override
 	protected void onInitGUI() {
 		initIpTextLabel();
 		initIpTextField();
 		initConnectButton();
 		initBackButton();
 	}
 	
 	@Override
 	protected void onInitShell() {
 		getShell().setText("Starte Spiel als Client");
 		getShell().setLayout(new GridLayout(2, false));
 	}
 	
 	@Override
 	protected Listener getOnCloseListener() {
 		return new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				if (ClientMenuGUI.this.rmi != null) {
					try {
						ClientMenuGUI.this.rmi.unexport();
					} catch (RemoteException e) {
						// nothing should happen here
					}
 				}
 			}
 		};
 	}
 	
 	private void initIpTextLabel() {
 	    GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalSpan = 3;
 		
 		this.ipTextLabel = new Label(getShell(), SWT.FILL);
 		this.ipTextLabel.setText("Bitte geben Sie die IP vom Server an:");
 		this.ipTextLabel.setBackground(getShell().getBackground());
 		this.ipTextLabel.setLayoutData(gridData);
 	}	
 	
 	private void initIpTextField() {
 	    GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalSpan = 3;
 		
 		this.ipTextField = new Text(getShell(), SWT.FILL);
 		this.ipTextField.setLayoutData(gridData);
 	}
 
 	private void initBackButton() {
 		this.backButton = new Button(getShell(), SWT.NONE);
 		this.backButton.setText("Zurück");
 		this.backButton.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
 		
 		this.backButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				ClientMenuGUI.this.getShell().close();
 			}
 		});
 	}
 
 
 	private void initConnectButton() {
 		this.connectButton = new Button(getShell(), SWT.NONE);
 		this.connectButton.setText("Verbinden");
 		this.connectButton.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
 		
 		this.connectButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				final NetworkServer gameServer = Globals.getRemoteServer
 						(ClientMenuGUI.this.ipTextField.getText(),
 						ClientMenuGUI.this.getShell());
 				ClientMenuGUI.this.timer = new Timer();
 				
 				ClientUserInterface clientUserInterface = new ClientUserInterface();
 				try {
 					ClientMenuGUI.this.rmi = new RMIUserInterfaceImpl(clientUserInterface);
 					ClientMenuGUI.this.timer.schedule(new TimerTask() {
 						@Override
 						public void run() {
 							getDisplay().asyncExec(new Runnable() {
 								@Override
 								public void run() {
 									try {
 										gameServer.unregister();
 									} catch (RemoteException e) {
 										System.out.println
 										(e.getLocalizedMessage());
 									}
 								}
 							});
 						}
 					}, 60000);
 					ClientMenuGUI.this.mainMenuGUI.setGameServer(new NetworkServerWrapper(gameServer));
 					
 					PlayGameGUI playGameGUI = new PlayGameGUI(getDisplay(), 
 							clientUserInterface, new NetworkServerWrapper(gameServer));
 					
 					ClientMenuGUI.this.playGameGUI = playGameGUI;
 					
 					clientUserInterface.setMainMenuGUI(ClientMenuGUI.this.mainMenuGUI);
 					clientUserInterface.setClientMenuGUI(ClientMenuGUI.this);
 					clientUserInterface.setPlayGameGUI(ClientMenuGUI.this.playGameGUI);
 					
 					gameServer.register(ClientMenuGUI.this.rmi);
 				} catch (RemoteException e1) {
 				}
 			}
 		});
 	}
 
 	public void cancelTimerAndOpenPlayGameGUI() {
 		this.timer.cancel();
 		this.playGameGUI.setPlayerName(ClientMenuGUI.this.mainMenuGUI.getPlayerName());
 		this.playGameGUI.setEnemyName("Gegner");
 		this.playGameGUI.open();
 		this.playGameGUI.nextTurnEnemy();
 	}
 }
