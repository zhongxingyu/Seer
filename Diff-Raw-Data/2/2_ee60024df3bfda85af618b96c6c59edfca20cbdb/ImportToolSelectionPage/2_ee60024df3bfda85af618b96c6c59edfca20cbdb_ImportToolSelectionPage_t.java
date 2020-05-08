 /**
  * Copyright (C) 2014  Luc Hermans
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program.  If
  * not, see <http://www.gnu.org/licenses/>.
  * 
  * Contact information: kozzeluc@gmail.com.
  */
 package org.lh.dmlj.schema.editor.wizard._import.elements;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.lh.dmlj.schema.SchemaRecord;
 import org.lh.dmlj.schema.editor.extension.RecordElementsImportToolExtensionElement;
 import org.lh.dmlj.schema.editor.template.RecordTemplate;
 import org.eclipse.wb.swt.SWTResourceManager;
 
 public class ImportToolSelectionPage extends WizardPage {
 	
 	private static final String CONFIRM_BUTTON_MESSAGE = 
 		"After selecting a data source, press the 'Select' button so that you can proceed with " +
 		"the next page; you will NOT be able to change your choice once you have pressed the " +
 		"'Select' button.  You will NOT be able to return to this page once you press the 'Next' " +
 		"button.";
 	
 	private Button btnSelect;
 	private Combo combo;
 	private RecordElementsImportToolExtensionElement extensionElement;
 	private List<RecordElementsImportToolExtensionElement> extensionElements;		
 	private Text textDescription;
 	private Text textCurrentRecordStructure;
 	private SchemaRecord record;
 	
 	public ImportToolSelectionPage(List<RecordElementsImportToolExtensionElement> extensionElements,
 								   SchemaRecord record) {
 		
 		super("_importToolSelectionPage", "Record Elements", null);
 		// there will be at least 1 import tool
 		this.extensionElements = extensionElements;
 		this.record = record;
 		setMessage("Select the (data) source; the current record structure will be COMPLETELY " +
 				   "replaced");
 	}
 
 	@Override
 	public void createControl(Composite parent) {		
 		Composite container = new Composite(parent, SWT.NONE);
 		setControl(container);				
 		container.setLayout(new GridLayout(3, false));
 		
 		Label lblInstalledImportTools = new Label(container, SWT.NONE);
 		lblInstalledImportTools.setText("Source:");
 		
 		combo = new Combo(container, SWT.READ_ONLY);
 		int i = extensionElements.size() > 1 ? 1 : 2;		
 		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, i, 1));		
 		combo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {				
 				selectImportTool();
 			}
 		});
 		
 		// the 'Select' button is only relevant if more than 1 import tool is defined; we use this 
 		// button to enable the 'Next (page)' button - once this button is enabled, the next pages 
 		// for the wizard will be added so this is a one time operation with no way back
 		if (extensionElements.size() > 1) {
 			btnSelect = new Button(container, SWT.NONE);
 			btnSelect.setEnabled(false);
 			btnSelect.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					combo.setEnabled(false);
 					btnSelect.setEnabled(false);
 					setPageComplete(true);
 				}
 			});
 			btnSelect.setText("Select");
 		}
 		
 		Label lblNewLabel_1 = new Label(container, SWT.NONE);
 		lblNewLabel_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
 		
 		Label lblDescription = new Label(container, SWT.NONE);
 		lblDescription.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
 		lblDescription.setText("Description:");
 		
 		textDescription = new Text(container, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
 		GridData gd_text = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
 		gd_text.heightHint = 75;
 		gd_text.widthHint = 300;
 		textDescription.setLayoutData(gd_text);
 			
 		if (extensionElements.size() > 1) {
 			combo.add("[select a data source and press the 'Select' button]");
 		}		
 		for (RecordElementsImportToolExtensionElement extensionElement : extensionElements) {
 			combo.add(extensionElement.getSource());
 		}
 		combo.select(0);
 		
 		Label lblNewLabel = new Label(container, SWT.NONE);
 		lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
 		
 		Label lblCurrentRecordStructure = new Label(container, SWT.WRAP);
 		GridData gd_lblCurrentRecordStructure = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
 		gd_lblCurrentRecordStructure.widthHint = 75;
 		lblCurrentRecordStructure.setLayoutData(gd_lblCurrentRecordStructure);
 		lblCurrentRecordStructure.setText("Current Record Structure:");
 		
 		textCurrentRecordStructure = new Text(container, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
 		textCurrentRecordStructure.setFont(SWTResourceManager.getFont("Courier New", 8, SWT.NORMAL));
 		GridData gd_textCurrentRecordStructure = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
 		gd_textCurrentRecordStructure.heightHint = 200;
 		gd_textCurrentRecordStructure.widthHint = 200;
 		textCurrentRecordStructure.setLayoutData(gd_textCurrentRecordStructure);
 		
 		if (extensionElements.size() > 1) {
 			// if there is more than 1 import tool available, pressing the 'Select' button will 
 			// mark the page as complete			
 			textDescription.setText(CONFIRM_BUTTON_MESSAGE);
 			setPageComplete(false);
 		} else {
 			// if there is only 1 import tool, there is no point in requiring the 'Select' button to 
 			// be pressed								
 			extensionElement = extensionElements.get(0);
 			textDescription.setText(extensionElement.getDescription());			
 			setPageComplete(true);		
 		}
 		
 		fillRecordStructure();
 			
 	}	
 
 	private void fillRecordStructure() {
 		String ddl = new RecordTemplate().generate(Arrays.asList(new Object[] {record, false}));
		int i = ddl.indexOf(" .");
 		String structure = i > -1 ? ddl.substring(i + 2).trim() : "";
 		textCurrentRecordStructure.setText(structure);
 	}
 
 	public RecordElementsImportToolExtensionElement getExtensionElement() {
 		return extensionElement;
 	}	
 
 	private void selectImportTool() {		
 		
 		int i = combo.getSelectionIndex();
 		if (extensionElements.size() == 1) {
 			extensionElement = extensionElements.get(0);
 		} else if (i > 0) {
 			extensionElement = extensionElements.get(i - 1);
 			btnSelect.setEnabled(true);
 			textDescription.setText(extensionElement.getDescription());
 		} else {
 			extensionElement = null;
 			textDescription.setText(CONFIRM_BUTTON_MESSAGE);
 			btnSelect.setEnabled(false);
 		}		
 		
 	}	
 	
 }
