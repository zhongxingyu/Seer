 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.common.ui.filters;
 
 import net.sf.okapi.common.BaseContext;
 import net.sf.okapi.common.IContext;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IParametersEditor;
 import net.sf.okapi.common.exceptions.OkapiEditorCreationException;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filters.IFilterConfigurationMapper;
 import net.sf.okapi.common.ui.Dialogs;
 import net.sf.okapi.common.ui.InputDialog;
 import net.sf.okapi.common.ui.UIUtil;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * Default panel for the creation, edit and selection of filter configurations.
  */
 public class FilterConfigurationsPanel extends Composite {
 
 	private Table table;
 	private FilterConfigurationsTableModel model;
 	private Text edFilter;
 	private Text edDescription;
 	private Button btEdit;
 	private Button btCreate;
 	private Button btDelete;
 	private IFilterConfigurationMapper mapper;
 	private IFilter cachedFilter;
 	private IContext context;
 	private String configEditorClass;
 
 	/**
 	 * Creates a FilterConfigurationsPanel object for a given parent with a given style.
 	 * @param parent the parent of the panel to create.
 	 * @param style the style of the panel.
 	 * @param filterConfigInfoDialogClass the calls name of the
 	 * {@link IFilterConfigurationInfoEditor} to use, or null to use the default.
 	 * @param mapper the {@link IFilterConfigurationMapper} mapper object to edit.
 	 */
 	public FilterConfigurationsPanel (Composite parent,
 		int style,
 		String filterConfigInfoDialogClass,
 		IFilterConfigurationMapper mapper)
 	{
 		super(parent, style);
 		createContent();
 		configEditorClass = filterConfigInfoDialogClass;
 
 		this.mapper = mapper;
 		model.setMapper(mapper);
 		
 		context = new BaseContext();
 		context.setObject("shell", getShell());
 	}
 
 	/**
 	 * Sets the implementation of {@link IFilterConfigurationMapper} for this panel. 
 	 * @param mapper the mapper to use with this panel.
 	 * @param configId the optional configuration identifier to select,
 	 * or null to select the first configuration in the list.
 	 */
 	public void setConfiguration (String configId)
 	{
 		// Update the list and the selection
 		model.updateTable(0, configId);
 		updateInfo();
 	}
 	
 	/**
 	 * Updates the list of the configurations. Tries to keep the current
 	 * selected configuration.
 	 */
 	public void updateData () {
 		// Get the current selection
 		int n = table.getSelectionIndex();
 		String configId = null;
 		if ( n > -1 ) {
 			configId = table.getItem(n).getText(FilterConfigurationsTableModel.ID_COLINDEX);
 		}
 		// Update the list and the selection
 		model.updateTable(0, configId);
 		updateInfo();
 	}
 	
 	/**
 	 * Gets the identifier of the configuration currently selected.
 	 * @return the configuration identifier of the current selection,
 	 * or null if there no configuration is selected.  
 	 */
 	public String getConfigurationId () {
 		int n = table.getSelectionIndex();
 		if ( n == -1 ) return null;
 		return table.getItem(n).getText(FilterConfigurationsTableModel.ID_COLINDEX);
 	}
 	
 	private void createContent () {
 		GridLayout layTmp = new GridLayout(3, false);
 		layTmp.marginHeight = 0;
 		layTmp.marginWidth = 0;
 		setLayout(layTmp);
 
 		table = new Table(this, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		GridData gdTmp = new GridData(GridData.FILL_BOTH);
 		gdTmp.horizontalSpan = 3;
 		table.setLayoutData(gdTmp);
 		table.addControlListener(new ControlAdapter() {
 		    public void controlResized(ControlEvent e) {
 		    	Rectangle rect = table.getClientArea();
 		    	int whidthCol3 = 80;
 				int nPart = (int)((rect.width-whidthCol3) / 100);
 				table.getColumn(0).setWidth(26*nPart);
 				table.getColumn(1).setWidth(40*nPart);
 				table.getColumn(2).setWidth(34*nPart);
 				table.getColumn(3).setWidth(whidthCol3);
 		    }
 		});
 		table.addMouseListener(new MouseListener() {
 			public void mouseDoubleClick(MouseEvent e) {
 				editParameters();
 			}
 			public void mouseDown(MouseEvent e) {}
 			public void mouseUp(MouseEvent e) {}
 		});
 		table.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				updateInfo();
 			};
 		});
 		
 		model = new FilterConfigurationsTableModel();
 		model.linkTable(table);
 		
 		edFilter = new Text(this, SWT.BORDER);
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		gdTmp.horizontalSpan = 3;
 		edFilter.setLayoutData(gdTmp);
 		edFilter.setEditable(false);
 	
 		edDescription = new Text(this, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL);
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		gdTmp.horizontalSpan = 3;
 		gdTmp.heightHint = 40;
 		edDescription.setLayoutData(gdTmp);
 		edDescription.setEditable(false);
 		
 		btEdit = new Button(this, SWT.PUSH);
 		btEdit.setText("Edit...");
 		btEdit.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				editParameters();
 			};
 		});
 		
 		btCreate = new Button(this, SWT.PUSH);
 		btCreate.setText("Create...");
 		btCreate.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				createConfiguration();
 			};
 		});
 		
 		btDelete = new Button(this, SWT.PUSH);
 		btDelete.setText("Delete...");
 		btDelete.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				deleteConfiguration();
 			};
 		});
 		
 		int nWidth = UIUtil.getMinimumWidth(80, btEdit, "&View...");
 		UIUtil.setSameWidth(nWidth, btEdit, btCreate, btDelete);
 	}
 
 	private void updateInfo () {
 		int n = table.getSelectionIndex();
 		if ( n > -1 ) {
 			FilterConfiguration config = mapper.getConfiguration(
 				table.getItem(n).getText(FilterConfigurationsTableModel.ID_COLINDEX));
 			if ( config != null ) {
 				edFilter.setText(config.filterClass);
 				edDescription.setText(config.description);
 				btEdit.setEnabled(true);
 				if ( config.custom ) btEdit.setText("&Edit...");
 				else btEdit.setText("&View...");
 				btDelete.setEnabled(config.custom);
 				btCreate.setEnabled(true);
 				return;
 			}
 		}
 		// Otherwise:
 		edFilter.setText("");
 		edDescription.setText("");
 		btEdit.setEnabled(false);
 		btCreate.setEnabled(false);
 		btDelete.setEnabled(false);
 	}
 	
 	private void editParameters () {
 		try {
 			int n = table.getSelectionIndex();
 			if ( n == -1 ) return;
 			FilterConfiguration config = mapper.getConfiguration(
 				table.getItem(n).getText(FilterConfigurationsTableModel.ID_COLINDEX));
 			if ( config == null ) return;
 			cachedFilter = mapper.createFilter(config.configId, cachedFilter);
 			IParametersEditor editor = mapper.createParametersEditor(config.configId, cachedFilter);
 			IParameters params = mapper.getParameters(config, cachedFilter);
 	
 			// Call the editor
 			if ( editor == null ) {
 				// Properties-like editing
 				InputDialog dlg  = new InputDialog(getShell(),
 					"Filters Parameters ("+config.configId+")",
 					"Parameters:",
 					params.toString(), null, 0, 200, 600);
 				dlg.setReadOnly(!config.custom); // Pre-defined configurations should be read-only
 				String data = dlg.showDialog();
 				if ( data == null ) return;
 				if ( !config.custom ) return; // Don't save pre-defined parameters
 				data = data.replace("\r\n", "\n");
 				params.fromString(data.replace("\r", "\n"));
 			}
 			else {
 				if ( !editor.edit(params, !config.custom, context) ) return;
 			}
 			// Don't try to save pre-defined parameters
 			if ( !config.custom ) return;
 			// Else save the modified parameters to custom storage
 			mapper.saveCustomParameters(config, params);
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(getShell(), e.getMessage(), null);
 		}
 	}
 
 	private void deleteConfiguration () {
 		try {
 			int n = table.getSelectionIndex();
 			if ( n == -1 ) return;
 			String id = table.getItem(n).getText(FilterConfigurationsTableModel.ID_COLINDEX);
 			FilterConfiguration config = mapper.getConfiguration(id);
 			if ( !config.custom ) return; // Cannot delete pre-defined configurations
 			
 			// Ask confirmation
 			MessageBox dlg = new MessageBox(getParent().getShell(),
 				SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
 			dlg.setMessage(String.format("This command will delete permanently the configuration '%s'.\n"
 				+"Do you want to proceed with the deletion?", id));
 			dlg.setText("Rainbow");
 			switch  ( dlg.open() ) {
 			case SWT.NO:
 			case SWT.CANCEL:
 				return;
 			}
 
 			// Else: Do delete the item
 			mapper.deleteCustomParameters(config);
 			mapper.removeConfiguration(id);
 			model.updateTable(n, null);
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(getShell(), e.getMessage(), null);
 		}
 	}
 	
 	private void createConfiguration () {
 		try {
 			int n = table.getSelectionIndex();
 			if ( n == -1 ) return;
 			FilterConfiguration baseConfig = mapper.getConfiguration(
 				table.getItem(n).getText(FilterConfigurationsTableModel.ID_COLINDEX));
 			if ( baseConfig == null ) return;
 
 			FilterConfiguration newConfig = mapper.createCustomConfiguration(baseConfig);
 			if ( newConfig == null ) {
 				throw new Exception(String.format("Could not create new configuration based on '%s'",
 					baseConfig.configId));
 			}
 			
 			// Edit the configuration info
 			if ( !editConfigurationInfo(newConfig) ) return; // Canceled
 			
 			// Set the new parameters with the base ones
 			IParameters newParams = mapper.getParameters(baseConfig);
 			// Save the new configuration
 			mapper.saveCustomParameters(newConfig, newParams);
 			
 			// Add the new configuration
 			mapper.addConfiguration(newConfig);
 			// Update the list and the selection
 			model.updateTable(0, newConfig.configId);
 			updateInfo();
 
 			// And continue by editing the parameters for that configuration
 			editParameters();
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(getShell(), e.getMessage(), null);
 		}
 	}
 	
 	private boolean editConfigurationInfo (FilterConfiguration config) {
 		// Create the configuration info editor
 		IFilterConfigurationInfoEditor editor;
 		if ( configEditorClass == null ) {
 			// Use default, if none is provided
 			editor = new FilterConfigurationInfoEditor();
 		}
 		else {
 			try {
 				editor = (IFilterConfigurationInfoEditor)Class.forName(configEditorClass).newInstance();
 			}
 			catch ( InstantiationException e ) {
 				throw new OkapiEditorCreationException(String.format(
 					"Cannot create editor '%s'", configEditorClass), e);
 			}
 			catch ( IllegalAccessException e ) {
 				throw new OkapiEditorCreationException(String.format(
 					"Cannot create editor '%s'", configEditorClass), e);
 			}
 			catch ( ClassNotFoundException e ) {
 				throw new OkapiEditorCreationException(String.format(
 					"Cannot create editor '%s'", configEditorClass), e);
 			}
 		}
 		
 		// Create and call the dialog
 		editor.create(getShell());
 		return editor.showDialog(config, mapper);
 	}
 }
