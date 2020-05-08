 package herbstJennrichRitterLehmann.ui.GUI;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 public class GameMenuGUI {
 
 	/**
 	 * Implementation Menü Spielauswahl
 	 */
 	
 	private Shell shell;
 	private static Display display;
 	private Button btnStartHost;
 	private Button btnStartClient;
 	private Button btnExit;
 	
 	public GameMenuGUI() {
 		initShell();
 		initBtnStartHost();
 		initBtnStartClient();
 		initBtnExit();
 		
 		shell.open();
 		 while (!shell.isDisposed()) {
 	          if (!display.readAndDispatch()) {
 	            display.sleep();
 	          }
 	      }
 		
 	}
 
 
 	private void initBtnExit() {
 		this.btnExit = new Button(shell, SWT.NONE);
 		this.btnExit.setText("Zurück");
 		this.btnExit.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
 				true, false));
 		this.btnExit.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				shell.dispose();
 			}
 		});
 		
 		
 	}
 
 
 	private void initBtnStartClient() {
 		this.btnStartClient= new Button(shell, SWT.NONE);
 		this.btnStartClient.setText("Starte als Client");
 		this.btnStartClient.setToolTipText("An einem Spiel teilnehmen");
 		this.btnStartClient.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
 				true, false));
 		
 	}
 
 
 	private void initBtnStartHost() {
 		this.btnStartHost = new Button(shell, SWT.NONE);
 		this.btnStartHost.setText("Starte als Host");
 		this.btnStartHost.setToolTipText("Ein Spiel als Server starten");
 		this.btnStartHost.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
 				true, false));
 		this.btnStartHost.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 			}
 		});
 		
 	}
 
 
 	private void initShell() {
 		this.shell = new Shell(SWT.TITLE | SWT.CLOSE);
 		this.shell.setText("Spielauswahl");
 		this.shell.setLayout(new GridLayout(1, false));
 		this.shell.setSize(220, 145);
 	};
 
 
 
 
 	public static void main(String[] args) {
 		display = new Display();
 		new GameMenuGUI();
 	}
 }
