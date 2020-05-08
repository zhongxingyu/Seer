 package com.feup.contribution.aida.ui;
 
 import java.util.LinkedList;
 
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 import com.feup.contribution.aida.AidaPlugin;
 import com.feup.contribution.aida.project.AidaPackage;
 import com.feup.contribution.aida.project.AidaProject;
 
 public class AidaRunTestDialog extends TitleAreaDialog{
 
 	private Table table;
 	private AidaProject project;
 	
 	public AidaRunTestDialog(Shell parentShell) {
 		super(parentShell);
 	}
 
 	@Override
 	public void create() {
 		super.create();
 		setTitle("Run Aida Tests");
		setMessage("Select the packages you want to test and click Run Tests");
 	}
 	
 	protected Control createDialogArea(Composite parent) {
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 1;
 		parent.setLayout(layout);
 
 	    GridData gd = new GridData(GridData.FILL_BOTH);
 		table = new Table(parent, SWT.CHECK);
 		table.setLayoutData(gd);
 	    
 		TableColumn packageColumn = new TableColumn(table, SWT.LEFT | SWT.BORDER);
 		packageColumn.setWidth(300);
 		packageColumn.setText("Packages");
 
 		LinkedList<AidaPackage> packages = project.getPackages();
 		for (AidaPackage aidaPackage : packages) {
 			TableItem item = new TableItem(table, SWT.NONE);
 			item.setText(aidaPackage.getName());			
 		}
 		
  		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 
 	    table.addListener(SWT.Selection, new Listener() {
 	        public void handleEvent(Event event) {
 	          if (event.item instanceof TableItem) {
 	        	  TableItem item = (TableItem) event.item;
 		          if (event.detail == SWT.CHECK) {
 		        	  AidaPackage p = project.getPackage(item.getText());
 		        	  LinkedList<AidaPackage> referenced = p.getReferencedPackages();
 		        	  for (AidaPackage aidaPackage : referenced) {
		        		  TableItem[] items = table.getItems();
 		        		  for (int i = 0; i < items.length; i++) {
 							if (items[i].getText().equals(aidaPackage.getName()))
 									items[i].setChecked(true);
 						}
 		        	  }
 		          } else {
 		        	  AidaPackage p = project.getPackage(item.getText());
 		        	  LinkedList<AidaPackage> referencedBy = p.getReferencedByPackages();
 		        	  for (AidaPackage aidaPackage : referencedBy) {
		        		  TableItem[] items = table.getItems();
 		        		  for (int i = 0; i < items.length; i++) {
 							if (items[i].getText().equals(aidaPackage.getName()))
 									items[i].setChecked(false);
 						}
 		        	  }		        	  
 		          }
 		      }
 	        }
 	    });
 		
 		return parent;
 	}
 
 	public void setProject(AidaProject project) {
 		this.project = project;
 		
 	}
 }
