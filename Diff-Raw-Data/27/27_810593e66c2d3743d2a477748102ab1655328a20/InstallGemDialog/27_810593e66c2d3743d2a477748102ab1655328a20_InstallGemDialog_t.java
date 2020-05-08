 package com.aptana.rdt.ui.gems;
 
 import java.util.SortedSet;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Text;
 import org.rubypeople.rdt.ui.TableViewerSorter;
 
 import com.aptana.rdt.AptanaRDTPlugin;
 import com.aptana.rdt.core.gems.Gem;
 import com.aptana.rdt.core.gems.LogicalGem;
 
 public class InstallGemDialog extends Dialog {
 
 	private Text nameText;
 	private Combo versionCombo;
 	
 	private String name;
 	private String version;
 	
 	private boolean filterByText = true;
 
 	private TableViewer gemViewer;
 	private GemContentProvider contentProvider;	
 
 	public InstallGemDialog(Shell parentShell) {
 		super(parentShell);
 		contentProvider = new GemContentProvider();
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		getShell().setText(GemsMessages.InstallGemDialog_dialog_title);
 		Composite control = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		control.setLayout(layout);
 
 		Label nameLabel = new Label(control, SWT.LEFT);
 		nameLabel.setText(GemsMessages.InstallGemDialog_name_label);
 
 		nameText = new Text(control, SWT.BORDER);
 		GridData nameTextData = new GridData();
 		nameTextData.widthHint = 150;
 		nameText.setLayoutData(nameTextData);
		final MyViewerFilter filter = new MyViewerFilter();
 		nameText.addModifyListener(new ModifyListener() {
 		
 			public void modifyText(ModifyEvent e) {
 				if (filterByText) {
					getShell().getDisplay().asyncExec(new Runnable() {
 
 						public void run() {
							filter.setText(nameText.getText());
 							gemViewer.refresh();
 						}
 					});
 				}
 				filterByText = true;
 			}
 		
 		});
 		
 		Label versionLabel = new Label(control, SWT.LEFT);
 		versionLabel.setText(GemsMessages.InstallGemDialog_version_label);
 
 		versionCombo = new Combo(control, SWT.DROP_DOWN);
 		GridData versionComboData = new GridData();
 		versionComboData.widthHint = 100;
 		versionCombo.setLayoutData(versionComboData);
 
 		gemViewer = new TableViewer(parent, SWT.SINGLE | SWT.FULL_SELECTION);
 		Table serverTable = gemViewer.getTable();
 		serverTable.setHeaderVisible(true);
 		serverTable.setLinesVisible(false);
 		GridData data = new GridData(GridData.FILL_HORIZONTAL);
 		data.heightHint = 400;
 		serverTable.setLayoutData(data);
 
 		TableColumn nameColumn = new TableColumn(serverTable, SWT.LEFT);
 		nameColumn.setText(GemsMessages.GemsView_NameColumn_label);
 		nameColumn.setWidth(150);
 
 		TableColumn versionColumn = new TableColumn(serverTable, SWT.LEFT);
 		versionColumn.setText(GemsMessages.GemsView_VersionColumn_label);
 		versionColumn.setWidth(75);
 
 		TableColumn descriptionColumn = new TableColumn(serverTable, SWT.LEFT);
 		descriptionColumn
 				.setText(GemsMessages.GemsView_DescriptionColumn_label);
 		descriptionColumn.setWidth(275);
 
 		gemViewer.setLabelProvider(new GemLabelProvider());
 		gemViewer.setContentProvider(contentProvider);
 		TableViewerSorter.bind(gemViewer);
 		gemViewer.setInput(AptanaRDTPlugin.getDefault().getGemManager().getRemoteGems());
		gemViewer.addFilter(filter);
 
 		gemViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 		
 			public void selectionChanged(SelectionChangedEvent event) {
 				ISelection selection = event.getSelection();
 				if (selection instanceof IStructuredSelection) {
 					IStructuredSelection structured = (IStructuredSelection) selection;
 					LogicalGem gem = (LogicalGem) structured.getFirstElement();
 					SortedSet<String> versions = gem.getVersions();
 					filterByText = false; // don't filter list when I programmatically set text
 					nameText.setText(gem.getName());
 					versionCombo.removeAll();
 					String lastVersion = null;
 					for (String version : versions) {
 						versionCombo.add(version);
 						lastVersion = version;
 					}
 					versionCombo.setText(lastVersion);
 				}
 			}
 		
 		});
 		
 		return control;
 	}
 
 	/**
 	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
 	 */
 	public void buttonPressed(int buttonId) {
 		if (buttonId == IDialogConstants.OK_ID) {
 			name = nameText.getText();
 			version = versionCombo.getText();
 			okPressed();
 		} else if (buttonId == IDialogConstants.CANCEL_ID) {
 			cancelPressed();
 		}
 	}
 
 	public Gem getGem() {
 		return new Gem(name, version, "");
 	}
 	
 	private static class MyViewerFilter extends ViewerFilter {
 		
		private String value;
 
		public void setText(String value) {
			if (value == null) {
				this.value = null;
			} else {
				this.value = value.toLowerCase();			
			}
 		}
 
 		@Override
 		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (value == null || value.trim().length() == 0) return true;
 			Gem gem = (Gem) element;
			return gem.getName().toLowerCase().startsWith(value);
 		}
 		
 	}
 
 }
