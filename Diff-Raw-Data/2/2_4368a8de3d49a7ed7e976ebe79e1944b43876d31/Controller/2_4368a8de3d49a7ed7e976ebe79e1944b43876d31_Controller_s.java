 package keytool.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JFileChooser;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import keytool.model.Model;
 import keytool.model.ModelException;
 import keytool.view.MainWindow;
 import keytool.view.View;
 
 /*
 @startuml
 
 [*] --> StateWait
 StateWait --> StateSaving : menuSave
 StateSaving --> StateWait : FOopen
 StateSaving --> StateWait : FOcancel
 
 StateWait --> StateOpening : menuOpen
 StateOpening --> StateWait : FOopen
 StateOpening --> StateWait : FOcancel
 
 StateWait --> StateExporting : itemExport
 StateExporting --> StateWait : FOopen
 StateExporting --> StateWait : FOcancel
 
 StateWait --> StateImporting : itemImport
 StateImporting --> StateWait : FOopen
 StateImporting --> StateWait : FOcancel
 
 @enduml
  */
 public class Controller {
 	private View view;
 	private Model model;
 	private enum State {StateWAIT, StateSAVING, StateOPENING, StateIMPORTING, StateEXPORTING, StateCREATINGKEY};
 	private State state;
 
 	public Controller(Model model, View view){
 		this.view = view;
 		this.model = model;
 		this.state = State.StateWAIT;
 
 		initMainWindowListener();
 		refreshKeysList();
 		refreshCertificateList();
 
 		initFOWindowListener();
 		initCreateKeyWindowListener();
 	}
 
 	private void initMainWindowListener() {
 		MainWindow mw = this.view.getMainWindow();
 		mw.addItemNewKeyStoreListener(new ItemNewListener());
 		mw.addItemOpenListener(new ItemOpenListener());
 		mw.addItemSaveListener(new ItemSaveListener());
 		mw.addItemSaveAsListener(new ItemSaveAsListener());
 		mw.addItemQuitListener(new ItemQuitListener());
 		mw.addBtnImportListener(new BtnImportListener());
 		mw.addBtnExportListener(new BtnExportListener());
 		mw.addBtnNewKeyListener(new BtnNewKeyListener());
 		mw.addKeyListListener(new ListKeysListener());
 		mw.addCertificatesListListener(new ListCertificatesListener());
 	}
 
 	private void refreshLists() {
 		this.refreshKeysList();
 		this.refreshCertificateList();
 	}
 	
 	private void refreshKeysList() {
 		try {
 			DefaultListModel list = this.model.getKeys();
 			this.view.getMainWindow().setKeysList(list);
 		} catch (ModelException e) {
 			this.view.createKeyErrorWindow(e.getMessage());
 		}
 	}
 
 	private void refreshCertificateList() {
 		try {
 			DefaultListModel list = this.model.getCertificates();
 			this.view.getMainWindow().setCertificatesList(list);
 		} catch (ModelException e) {
 			this.view.createKeyErrorWindow(e.getMessage());
 		}
 	}
 
 	class ItemNewListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			try {
 				model.newKeyStore();
 			} catch (ModelException e1) {
 				view.createKeyErrorWindow(e1.getMessage());
 			}
 			refreshLists();
 		}
 	}
 	
 	class ItemOpenListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			view.getFileOpenWindow().setOpenDialog();
 			view.showFileOpenWindow();
 			state = State.StateOPENING;
 		}
 	}
 
 	class ItemQuitListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			view.disposeAll();
 		}
 	}
 
 	class ItemSaveListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			try {
 				model.save();
 			} catch (ModelException e1) {
 				view.createKeyErrorWindow(e1.getMessage());
 			}
 		}
 	}
 
 	class ItemSaveAsListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			view.getFileOpenWindow().setSaveDialog();
 			view.showFileOpenWindow();
 			state = State.StateSAVING;
 		}
 	}
 
 	class BtnImportListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			view.showFileOpenWindow();
 			state = State.StateIMPORTING;
 		}
 	}
 
 	class BtnExportListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			view.showFileOpenWindow();
 			state = State.StateEXPORTING;
 		}
 	}
 	
 	class BtnNewKeyListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			System.out.println("création clé");
 			//view.showCreateKeyWindow();
 			state = State.StateCREATINGKEY;
 		}
 	}
 	
 	class ListKeysListener implements ListSelectionListener {
 		public void valueChanged(ListSelectionEvent arg0) {
 			String selectedKey = view.getMainWindow().getSelectedKey();
 			try {
 				view.getMainWindow().setDetails(model.getKey(selectedKey).getDetails());
 			} catch (ModelException e) {
 				view.createKeyErrorWindow(e.getMessage());
 			}
 		}
 	}
 
 	class ListCertificatesListener implements ListSelectionListener {
 		public void valueChanged(ListSelectionEvent arg0) {
 			String selectedCertificates = view.getMainWindow().getSelectedCertificate();
 			try {
				view.getMainWindow().setDetails(model.getKey(selectedCertificates).getDetails());
 			} catch (ModelException e) {
 				view.createKeyErrorWindow(e.getMessage());
 			}
 		}
 	}
 	
 	private void initFOWindowListener() {
 		this.view.getFileOpenWindow().addFCActionListener(new FOActionListener());
 		this.view.getFileOpenWindow().addFCWindowListener(new FOWindowListener());
 	}
 
 	class FOActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if (JFileChooser.APPROVE_SELECTION.equals(e.getActionCommand())) {
 				switch(state) {
 				case StateOPENING:
 					try {
 						view.hideFileOpenWindow();
 						model.openKeyStore(view.getFileOpenWindow().getPath());
 					} catch (ModelException e2) {
 						view.createKeyErrorWindow(e2.getMessage());
 					}
 					break;
 				case StateSAVING:
 					try {
 						view.hideFileOpenWindow();
 						model.saveTo(view.getFileOpenWindow().getPath());
 					} catch (ModelException e1) {
 						view.createKeyErrorWindow(e1.getMessage());
 					}
 					break;
 				case StateEXPORTING:
 					try {
 						if(view.getMainWindow().isKeysTabSelected()) {
 							String alias = view.getMainWindow().getSelectedKey();
 							if(! alias.equals(""))
 								model.getKey(alias).exportTo(view.getFileOpenWindow().getPath());
 								
 						} else if(view.getMainWindow().isCertificatesTabSelected()) {
 							String alias = view.getMainWindow().getSelectedCertificate();
 							if(! alias.equals(""))
 								model.getCertificate(alias).exportTo(view.getFileOpenWindow().getPath());
 							
 						}
 					} catch (ModelException e1) {
 						view.createKeyErrorWindow(e1.getMessage());
 					}
 					view.hideFileOpenWindow();
 					break;
 				case StateIMPORTING:
 					break;
 				}
 			} else if (JFileChooser.CANCEL_SELECTION.equals(e.getActionCommand())) {
 				view.hideFileOpenWindow();
 			}
 			refreshLists();
 			state = State.StateWAIT;
 		}
 	}
 
 	class FOWindowListener extends WindowAdapter {
 		public void windowClosing(WindowEvent e) {
 			view.hideFileOpenWindow();
 			state = State.StateWAIT;
 		}
 	}
 	
 	private void initCreateKeyWindowListener() {
 		this.view.getCreateKeyWindow().addBtnCancelListener(new CKWBtnCancelListener());
 		this.view.getCreateKeyWindow().addBtnValidateListener(new CKWBtnValidateListener());
 		this.view.getCreateKeyWindow().addCWWindowListener(new CKWWindowListener());
 	}
 	
 	class CKWBtnCancelListener implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 			
 		}
 	}
 	
 	class CKWBtnValidateListener implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 			
 		}
 	}
 	
 	class CKWWindowListener extends WindowAdapter {
 		public void windowClosing(WindowEvent e) {
 
 		}
 	}
 
 }
