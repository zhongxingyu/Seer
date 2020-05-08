 package org.certificatesmanager.listeners;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.certificatesmanager.Components;
 import org.certificatesmanager.guicomponents.PasswordDialog;
 import org.certificatesmanager.security.KeyManager;
 import org.certificatesmanager.security.KeyObject;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 public class MenuItemListener extends SelectionAdapter {
 	
 	private Shell shell;
 	private Display display;
 	private Components components;	
 	
 	public MenuItemListener(Shell shell){
 		this.shell = shell;
 		display = Display.getCurrent();
 	}
 
 	public MenuItemListener(Shell shlCertificatesmanager, Components components) {
 		this(shlCertificatesmanager);
 		this.components = components;
 	}
 
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		super.widgetSelected(e);
 		String selectedItem = ((MenuItem) e.widget).getText();
 		switch(selectedItem){
 		case "Open Certificate":			
 			showFileDialog();
 			break;
 		case "About":
 			System.out.println("About code goes Here..");
 			if(this.components!=null){
 				System.out.println(components.getMenu().getItemCount());
 			}
 			showDialog();
 			break;
 		case "Exit":
 			shell.close();
 			break;
 		}
 	}
 	
 	private void showDialog(){
 		MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION |SWT.OK);
 	    messageBox.setMessage("Certificate Manager ver 1.0");
 	    int rc = messageBox.open();
 	}
 	
 	private void showFileDialog(){		
 		FileDialog dialog = new FileDialog(shell);
 		dialog.setText("Choose a certificate");
 		String platform = SWT.getPlatform();
 		dialog.setFilterPath (platform.equals("win32") || platform.equals("wpf") ? "c:\\" : "/");
 		String result = dialog.open();
 		System.out.println ("RESULT=" +result);
		if(result==null) return;
 		PasswordDialog pwdialog = new PasswordDialog(shell);
 	    pwdialog.open();
 		KeyManager key = new KeyManager(result, pwdialog.getPassword());		
 		try{
 			key.load();
 			List<KeyObject> list = (ArrayList<KeyObject>) key.getKeyList();
 			Iterator<KeyObject> keyIt = list.iterator();
 			while(keyIt.hasNext()){
 				KeyObject entry = keyIt.next();
 				TableItem tableItem= new TableItem(components.getAliasTable(), SWT.NONE);
 				tableItem.setText(new String[] {entry.alias, entry.startDate.toString(), entry.endDate.toString(), entry.issuerName, entry.algorithm});			
 			}
 			for (TableColumn tc : components.getAliasTable().getColumns()){
 		       	tc.pack();
 			}
 			shell.pack();
 			while (!shell.isDisposed()) {
 				if (!display.readAndDispatch ()) display.sleep ();
 			}
 			display.dispose ();
 		} catch (Exception e){
 			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR |SWT.OK);			
 		    messageBox.setMessage(e.getMessage());
 		    messageBox.open();
 		}
 	}
 
 }
