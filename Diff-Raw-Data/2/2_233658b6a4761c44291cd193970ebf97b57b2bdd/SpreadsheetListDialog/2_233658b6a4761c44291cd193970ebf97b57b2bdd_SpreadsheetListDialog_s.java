 package com.uwusoft.timesheet.googlestorage;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.dialogs.ListDialog;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 import com.google.gdata.client.docs.DocsService;
 import com.google.gdata.data.docs.DocumentListEntry;
 import com.google.gdata.data.docs.DocumentListFeed;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 import com.uwusoft.timesheet.Activator;
 import com.uwusoft.timesheet.util.MessageBox;
 import com.uwusoft.timesheet.util.SecurePreferencesManager;
 
 public class SpreadsheetListDialog extends ListDialog {
 	private DocsService docsService;
 	private Map<String, String> spreadsheets = new HashMap<String, String>();
 	private String spreadsheet;
 
 	class SpreadsheetLabelProvider extends LabelProvider implements ITableLabelProvider {
 		public String getColumnText(Object obj, int index) {
 			return getText(obj);
 		}
 
 		public Image getColumnImage(Object obj, int index) {
 			return getImage(obj);
 		}
 
 		public Image getImage(Object obj) {
 			return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet.googlestorage", "/icons/spread_16.png").createImage();
 		}
 	}
 	
 	public SpreadsheetListDialog(Shell parent) {
 		super(parent);
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		SecurePreferencesManager secureProps = new SecurePreferencesManager("Google");
     	String userName = preferenceStore.getString(GoogleStorageService.PREFIX + GoogleStorageService.USERNAME);
     	String password = secureProps.getProperty(GoogleStorageService.PREFIX + GoogleStorageService.PASSWORD);
        	docsService = new DocsService("Timesheet");
         docsService.useSsl();
         try {
 			docsService.setUserCredentials(userName, password);
 			DocumentListFeed resultFeed = docsService.getFeed(new URL("http://docs.google.com/feeds/default/private/full/"),
 					DocumentListFeed.class);
 			for(DocumentListEntry entry : resultFeed.getEntries()) {
				if (entry.getType().equals("spreadsheet"))
 					spreadsheets.put(entry.getTitle().getPlainText(), entry.getDocId());
 			}
 		} catch (AuthenticationException e) {
 			MessageBox.setError("Spreadsheet", e.getMessage());
 		} catch (MalformedURLException e) {
 			MessageBox.setError("Spreadsheet", e.getMessage());
 		} catch (IOException e) {
 			MessageBox.setError("Spreadsheet", e.getMessage());
 		} catch (ServiceException e) {
 			MessageBox.setError("Spreadsheet", e.getResponseBody());
 		}
 		setContentProvider(ArrayContentProvider.getInstance());
 		setLabelProvider(new SpreadsheetLabelProvider());
 		setTitle("Spreadsheets");
 		setMessage("Select spreadsheet");
 		setWidthInChars(70);
 	}
     @Override
     protected Control createDialogArea(Composite composite) {
         Composite parent = (Composite) super.createDialogArea(composite);
 		getTableViewer().setInput(spreadsheets.keySet());
 		getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
 			@Override
 			public void selectionChanged(SelectionChangedEvent event) {
 				ISelection rawSelection = getTableViewer().getSelection();
 				if (rawSelection != null
 						&& rawSelection instanceof IStructuredSelection) {
 					IStructuredSelection selection = (IStructuredSelection) rawSelection;
 					if (selection.size() == 1) {
 						spreadsheet = (String) selection.getFirstElement();
 					}
 				}
 			}			
 		});
         return parent;
     }
 	
     public String getSpreadsheetKey() {
 		return spreadsheets.get(spreadsheet);
 	}
 }
