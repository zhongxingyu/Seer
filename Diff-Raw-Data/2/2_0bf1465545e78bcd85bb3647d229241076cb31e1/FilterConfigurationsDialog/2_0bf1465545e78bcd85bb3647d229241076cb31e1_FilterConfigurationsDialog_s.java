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
 
 import java.io.File;
 
 import net.sf.okapi.common.IHelp;
 import net.sf.okapi.common.filters.FilterConfigurationMapper;
 import net.sf.okapi.common.ui.ClosePanel;
 import net.sf.okapi.common.ui.Dialogs;
 import net.sf.okapi.common.ui.InputDialog;
 import net.sf.okapi.common.ui.OKCancelPanel;
 import net.sf.okapi.common.ui.UIUtil;
 import net.sf.okapi.common.ui.filters.FilterConfigurationsPanel;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * Default dialog box for maintaining an {@link FilterConfigurationMapper} object.
  */
 public class FilterConfigurationsDialog {
 
 	private Shell shell;
 	private FilterConfigurationsPanel pnlConfigs;
 	private FilterConfigurationMapper mapper;
 	private String result = null;
 	private IHelp help;
 	private Text edParamsFolder;
 	private Button btGetParamsFolder;
 
 	/**
 	 * Creates a new FilterConfigurationsDialog object.
 	 * @param parent the parent shell for this dialog box (can be null).
 	 * @param selectionMode true to have a 'Select' button instead of 'Close'.
 	 * @param mapper filter configuration mapper object to edit.
 	 * @param helpParam help context (can be null).
 	 */
 	public FilterConfigurationsDialog (Shell parent,
 		boolean selectionMode,
 		FilterConfigurationMapper mapper,
 		IHelp helpParam)
 	{
 		this.mapper = mapper;
 		help = helpParam;
 		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
 		shell.setText(Res.getString("FilterConfigurationsDialog.caption")); //$NON-NLS-1$
 		UIUtil.inheritIcon(shell, parent);
 		shell.setLayout(new GridLayout());
 		
 		pnlConfigs = new FilterConfigurationsPanel(shell, SWT.NONE,
 			"net.sf.okapi.common.ui.filters.FilterConfigurationIdentifierEditor", mapper); //$NON-NLS-1$
 		GridData gdTmp = new GridData(GridData.FILL_BOTH);
 		pnlConfigs.setLayoutData(gdTmp);
 		
 		Group group = new Group(shell, SWT.NONE);
 		group.setLayout(new GridLayout(3, false));
 		group.setText(Res.getString("FilterConfigurationsDialog.customConfigCaption")); //$NON-NLS-1$
 		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		Label label = new Label(group, SWT.NONE);
 		label.setText(Res.getString("FilterConfigurationsDialog.folder")); //$NON-NLS-1$
 
 		edParamsFolder = new Text(group, SWT.BORDER);
 		edParamsFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		edParamsFolder.setEditable(false);
 		
 		btGetParamsFolder = new Button(group, SWT.PUSH);
 		btGetParamsFolder.setText("..."); //$NON-NLS-1$
 		btGetParamsFolder.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				editParamsFolder();
             }
 		});
 		
 		edParamsFolder.setText(mapper.getCustomConfigurationsDirectory());
 		
 		// Dialog-level buttons
 		SelectionAdapter Actions = new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				result = null;
 				if ( e.widget.getData().equals("h") ) { //$NON-NLS-1$
 					if ( help != null ) {
						help.showTopic(this, "../index", "lib/filterConfigurations.html"); //$NON-NLS-1$ //$NON-NLS-2$
 					}
 					return;
 				}
 				if ( e.widget.getData().equals("o") ) { //$NON-NLS-1$
 					result = pnlConfigs.getConfigurationId(); 
 				}
 				shell.close();
 			};
 		};
 		
 		if ( selectionMode ) {
 			OKCancelPanel pnlActions = new OKCancelPanel(shell, SWT.NONE, Actions, (help!=null), Res.getString("FilterConfigurationsDialog.select")); //$NON-NLS-1$
 			pnlActions.btCancel.setText(Res.getString("FilterConfigurationsDialog.close")); //$NON-NLS-1$
 			gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 			gdTmp.horizontalSpan = 2;
 			pnlActions.setLayoutData(gdTmp);
 			shell.setDefaultButton(pnlActions.btOK);
 		}
 		else {
 			ClosePanel pnlActions = new ClosePanel(shell, SWT.NONE, Actions, (help!=null));
 			gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 			gdTmp.horizontalSpan = 2;
 			pnlActions.setLayoutData(gdTmp);
 			shell.setDefaultButton(pnlActions.btClose);
 		}
 		
 		shell.pack();
 		Rectangle Rect = shell.getBounds();
 		shell.setMinimumSize(Rect.width, Rect.height);
 		Point startSize = shell.getMinimumSize();
 		if ( startSize.x < 760 ) startSize.x = 760;
 		if ( startSize.y < 550 ) startSize.y = 550;
 		shell.setSize(startSize);
 		Dialogs.centerWindow(shell, parent);
 	}
 	
 	private void editParamsFolder () {
 		try {
 			InputDialog dlg = new InputDialog(shell,
 				Res.getString("FilterConfigurationsDialog.customConfigCaption"), //$NON-NLS-1$
 				Res.getString("FilterConfigurationsDialog.customConfigCaptionLabel"), //$NON-NLS-1$
 				edParamsFolder.getText(),
 				null, 1, -1, -1);
 			dlg.setAllowEmptyValue(false);
 			String newDir = dlg.showDialog();
 			if ( newDir == null ) return; // Canceled
 			if ( !newDir.endsWith(File.separator) ) {
 				newDir = newDir + File.separator;
 			}
 			// Set the new directory
 			edParamsFolder.setText(newDir);
 			// Set the mapper and reload the custom configurations
 			updateCustomConfigurations();
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 
 	/**
 	 * Opens this dialog box.
 	 * @param configId the identifier of the configuration to select (can be null).
 	 * @return the identifier of the selected configuration, or null if none is selected.
 	 */
 	public String showDialog (String configId) {
 		pnlConfigs.setConfiguration(configId);
 		shell.open();
 		while ( !shell.isDisposed() ) {
 			if ( !shell.getDisplay().readAndDispatch() )
 				shell.getDisplay().sleep();
 		}
 		return result;
 	}
 
 	/**
 	 * Updates the custom configurations directory and its custom configurations list.
 	 */
 	public void updateCustomConfigurations () {
 		// Re-load custom configurations
 		mapper.setCustomConfigurationsDirectory(edParamsFolder.getText());
 		mapper.updateCustomConfigurations();
 		// Update the display list and the selection
 		pnlConfigs.updateData();
 	}
 
 }
