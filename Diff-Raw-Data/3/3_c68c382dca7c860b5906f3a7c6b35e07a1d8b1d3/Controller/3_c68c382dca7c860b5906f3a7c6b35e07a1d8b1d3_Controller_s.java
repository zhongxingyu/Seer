 package main;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 
 import file.File;
 import folder.FolderTree;
 import gui.GuiWindower;
 import node.ClientP2Pnode;
 
 /**
  * Main Controller
  * 
  * @author Marek Niesiobędzki
  * 
  */
 public class Controller {
 
 	private GuiWindower gui;
 	private FolderTree folderTree;
 	private ClientP2Pnode clientP2Pnode;
 	private File file;
 
 	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
 			this);
 
 	public Controller(GuiWindower gui, FolderTree folderTree, File file,
 			ClientP2Pnode clientP2Pnode) {
 		this.gui = gui;
 		this.folderTree = folderTree;
 		this.file = file;
 		this.clientP2Pnode = clientP2Pnode;
 		this.gui.addButtonActionListener(guziki); // podpięcie guzików
 		this.addPropertyChangeListener(this.gui); // podpięcie zmian
		this.folderTree = new FolderTree(this.gui.getFolderPath());
 	}
 
 	private void addPropertyChangeListener(PropertyChangeListener listener) {
 		propertyChangeSupport.addPropertyChangeListener(listener);
 	}
 
 	/* Action Listener */
 	private ActionListener guziki = new ActionListener() {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			System.out.println("Akcja " + e.getActionCommand());
 			if (e.getActionCommand().equals("Rozpocznij")) {
 				// TODO: coś
 				startSync();
 				File.runFolderListener(gui.getFolderPath());
 			}
 
 		}
 	};
 	private int serverPort;
 	private int nodePort;
 	private String clientName;
 	private String serverAddress;
 
 	/**
 	 * Pobiera adres serwera z GUI i sprawdza czy pasuje do wzorców adresu IP lub hostu
 	 */
 	public void getServerAddress() {
 		String addressOrIP = this.gui.getServerAddress();
 		if (isStringIPorHostName(addressOrIP)) {
 			serverAddress = addressOrIP;
 		} else {
 			this.gui.displayError("Zły adres IP lub host");
 		}
 	}
 
 	public void getServerPort() {
 		String port = this.gui.getServerPort();
 		if (isStringPortNumber(port)) {
 			serverPort = Integer.parseInt(port);
 		} else {
 			this.gui.displayError("Zły numer portu serwera");
 		}
 	}
 
 	public void getClientPort() {
 		String port = this.gui.getClientPort();
 		if (isStringPortNumber(port)) {
 			nodePort = Integer.parseInt(port);
 		} else {
 			this.gui.displayError("Zły numer portu klienta");
 		}
 	}
 
 	public void getClientName() {
 		clientName = this.gui.getClientName();
 	}
 
 	public void setFolderTreePath(String path) {
 		// TODO: uruchomienie nasłuchu zmian
 		this.gui.getFolderPath();
 	}
 
 	public boolean startSync() {
 		return true;
 	}
 
 	public boolean stopSync() {
 		return true;
 	}
 
 	private boolean isStringPortNumber(String port) {
 		return port
 				.matches("(^[0-9]$)|(^[0-9][0-9]$)|(^[0-9][0-9][0-9]$)|(^[0-9][0-9][0-9][0-9]$)|((^[0-5][0-9][0-9][0-9][0-9]$)|(^6[0-4][0-9][0-9][0-9]$)|(^65[0-4][0-9][0-9]$)|(^655[0-2][0-9]$)|(^6553[0-5]$))");
 	}
 
 	private boolean isStringIPorHostName(String hostOrIP) {
 		String ValidIpAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
 		String ValidHostnameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";
 		return (hostOrIP.matches(ValidIpAddressRegex) || hostOrIP
 				.matches(ValidHostnameRegex));
 	}
 
 	public static void main(String[] args) {
 		String ValidIpAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
 
 		String ValidHostnameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";
 		System.out.println(new String("1299serek.pjwstk.wp.pl.dupa")
 				.matches(ValidHostnameRegex));
 	}
 
 }
