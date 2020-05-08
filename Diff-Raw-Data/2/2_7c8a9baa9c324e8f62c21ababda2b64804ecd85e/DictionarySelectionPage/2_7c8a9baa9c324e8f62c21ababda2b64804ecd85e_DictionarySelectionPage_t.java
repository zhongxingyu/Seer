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
 package org.lh.dmlj.schema.editor.dictionary.tools._import.page;
 
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.lh.dmlj.schema.editor.dictionary.tools.Plugin;
 import org.lh.dmlj.schema.editor.dictionary.tools._import.common.ContextAttributeKeys;
 import org.lh.dmlj.schema.editor.dictionary.tools.model.Dictionary;
 import org.lh.dmlj.schema.editor.importtool.AbstractDataEntryPage;
 
 public class DictionarySelectionPage extends AbstractDataEntryPage {
 
 	private Table table;
 	
 	private List<Dictionary> dictionaries;
 	
 	public DictionarySelectionPage() {
 		super();
 	}
 
 	@Override
 	public void aboutToShow() {
 		validatePage();
 	}
 
 	/**
 	 * @wbp.parser.entryPoint
 	 */
 	@Override
 	public Control createControl(Composite parent) {
 		Composite container = new Composite(parent, SWT.NONE);
 		container.setLayout(new GridLayout(1, false));
 		
 		table = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
 		table.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				validatePage();
 			}
 		});
 		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		
 		TableColumn tblclmnId = new TableColumn(table, SWT.NONE);
 		tblclmnId.setWidth(125);
 		tblclmnId.setText("Id");
 		
 		TableColumn tblclmnHostname = new TableColumn(table, SWT.NONE);
 		tblclmnHostname.setWidth(125);
 		tblclmnHostname.setText("Hostname");
 		
 		TableColumn tblclmnPort = new TableColumn(table, SWT.RIGHT);
 		tblclmnPort.setWidth(50);
 		tblclmnPort.setText("Port");
 		
 		TableColumn tblclmnDictname = new TableColumn(table, SWT.NONE);
 		tblclmnDictname.setWidth(75);
 		tblclmnDictname.setText("Dictname");
 		
 		TableColumn tblclmnSchema = new TableColumn(table, SWT.NONE);
 		tblclmnSchema.setWidth(75);
 		tblclmnSchema.setText("Schema");
 		
 		getDictionaries();
 		
 		return container;
 	}
 	
 	private void getDictionaries() {
 		try {
 			dictionaries = Dictionary.list(Plugin.getDefault().getDictionaryFolder());
 			for (Dictionary dictionary : dictionaries) {
 				TableItem tableItem = new TableItem(table, SWT.NONE);
 				tableItem.setText(0, dictionary.getId());
 				tableItem.setText(1, dictionary.getHostname());
 				tableItem.setText(2, String.valueOf(dictionary.getPort()));
 				tableItem.setText(3, dictionary.getDictname());
				tableItem.setText(4, dictionary.getSchemaWithDefault(Plugin.getDefault()));
 			}
 		} catch (Throwable t) {
 			throw new RuntimeException(t);
 		}
 	}	
 	
 	private void validatePage() {
 	
 		getContext().clearAttribute(ContextAttributeKeys.DICTIONARY);
 		
 		getController().setPageComplete(false);
 		getController().setErrorMessage(null);
 		
 		if (!Plugin.getDefault().isDriverInstalled()) {
 			getController().setErrorMessage("CA IDMS JDBC driver NOT installed");
 			return;
 		}
 		
 		if (table.getSelectionIndex() > -1) {
 			getContext().setAttribute(ContextAttributeKeys.DICTIONARY, 
 									  dictionaries.get(table.getSelectionIndex()));
 			getController().setPageComplete(true);
 		}
 		
 	}
 
 }
