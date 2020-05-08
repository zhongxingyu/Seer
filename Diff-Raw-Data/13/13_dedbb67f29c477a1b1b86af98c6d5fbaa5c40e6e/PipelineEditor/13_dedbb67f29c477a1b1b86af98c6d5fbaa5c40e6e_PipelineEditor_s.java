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
 
 package net.sf.okapi.applications.rainbow.pipeline;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 import net.sf.okapi.common.IHelp;
 import net.sf.okapi.common.ui.Dialogs;
 import net.sf.okapi.common.ui.InputDialog;
 import net.sf.okapi.common.ui.OKCancelPanel;
 import net.sf.okapi.common.ui.UIUtil;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 public class PipelineEditor {
 
 	private Shell shell;
 	private boolean result;
 	private IHelp help;
 //	private String projectDir;
 	private PipelineWrapper wrapper;
 	private Map<String, Step> availableSteps;
 	private ArrayList<Step> workSteps;
 	private Text edPath;
 	private List lbSteps;
 	private Button btLoad;
 	private Button btSave;
 	private Button btSaveAs;
 	private Button btAddStep;
 	private Button btEditStep;
 	private Button btRemoveStep;
 	private OKCancelPanel pnlActions;
 	
 	public boolean edit (Shell parent,
 		Map<String, Step> availableSteps,
 		PipelineWrapper wrapper,
 		IHelp helpParam,
 		String projectDir,
 		boolean executeMode)
 	{
 		boolean result = false;
 		try {
 			this.availableSteps = availableSteps;
 			workSteps = new ArrayList<Step>();
 			for ( Step step : wrapper.getSteps() ) {
 				workSteps.add(step.clone());
 			}
 			this.wrapper = wrapper;
 			this.help = helpParam;
 //			this.projectDir = projectDir;
 			create(parent, executeMode);
 			populate(0);
 			result = showDialog();
 		}
 		catch ( Exception e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 			result = false;
 		}
 		finally {
 			// Dispose of the shell, but not of the display
 			if ( shell != null ) shell.dispose();
 		}
 		return result;
 	}
 
 	private boolean showDialog () {
 		shell.open();
 		while ( !shell.isDisposed() ) {
 			if ( !shell.getDisplay().readAndDispatch() )
 				shell.getDisplay().sleep();
 		}
 		return result;
 	}
 
 	private void create (Shell parent,
 		boolean executeMode)
 	{
 		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
 		if ( executeMode ) shell.setText("Execute Pipeline");
 		else shell.setText("Edit Pipeline");
 		if ( parent != null ) UIUtil.inheritIcon(shell, parent);
 		GridLayout layTmp = new GridLayout(2, false);
 		shell.setLayout(layTmp);
 		
 		edPath = new Text(shell, SWT.BORDER);
 		edPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		edPath.setEditable(false);
 
 		btLoad = new Button(shell, SWT.PUSH);
 		btLoad.setText("Load...");
 		btLoad.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				load(null);
 			}
 		});
 		
 		lbSteps = new List(shell, SWT.BORDER);
 		GridData gdTmp = new GridData(GridData.FILL_BOTH);
 		gdTmp.verticalSpan = 5;
 		lbSteps.setLayoutData(gdTmp);
 		
 		int width = 100;
 		btSave = new Button(shell, SWT.PUSH);
 		btSave.setText("Save");
 		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
 		gdTmp.widthHint = width;
 		btSave.setLayoutData(gdTmp);
 		btSave.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				save(edPath.getText());
 			}
 		});
 		
 		btSaveAs = new Button(shell, SWT.PUSH);
 		btSaveAs.setText("Save As...");
 		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
 		gdTmp.widthHint = width;
 		btSaveAs.setLayoutData(gdTmp);
 		btSaveAs.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				save(null);
 			}
 		});
 		
 		btAddStep = new Button(shell, SWT.PUSH);
 		btAddStep.setText("Add Step...");
 		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
 		gdTmp.widthHint = width;
 		btAddStep.setLayoutData(gdTmp);
 		btAddStep.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				addStep();
 			}
 		});
 		
 		btRemoveStep = new Button(shell, SWT.PUSH);
 		btRemoveStep.setText("Remove Step...");
 		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
 		gdTmp.widthHint = width;
 		btRemoveStep.setLayoutData(gdTmp);
 		btRemoveStep.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				removeStep();
 			}
 		});
 		
 		btEditStep = new Button(shell, SWT.PUSH);
 		btEditStep.setText("Edit Options...");
 		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
 		gdTmp.widthHint = width;
 		btEditStep.setLayoutData(gdTmp);
 		btEditStep.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				editStep();
 			}
 		});
 
 		// Dialog-level buttons
 		SelectionAdapter OKCancelActions = new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				result = false;
 				if ( e.widget.getData().equals("h") ) { //$NON-NLS-1$
 					//if ( help != null ) help.showTopic(this, "index", "inputDocProp.html"); //$NON-NLS-1$ //$NON-NLS-2$
 					return;
 				}
 				if ( e.widget.getData().equals("o") ) { //$NON-NLS-1$
 					if ( !saveData() ) return;
 					result = true;
 				}
 				shell.close();
 			};
 		};
 		
 		
 		if ( executeMode ) {
 			pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true, "Execute");
 		}
 		else {
 			pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
 		}
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		gdTmp.horizontalSpan = 2;
 		pnlActions.setLayoutData(gdTmp);
 		shell.setDefaultButton(pnlActions.btOK);
 
 		shell.pack();
 		Rectangle Rect = shell.getBounds();
 		shell.setMinimumSize(Rect.width, Rect.height);
 		Dialogs.centerWindow(shell, parent);
 	}
 
 	private String getId (String listEntry) {
 		int pos = listEntry.indexOf('[');
 		if ( pos == -1 ) return listEntry;
 		return listEntry.substring(pos+1, listEntry.length()-1);
 	}
 
 	private void populate (int index) {
 		edPath.setText(wrapper.getPath()==null ? "" : wrapper.getPath());
 		lbSteps.removeAll();
 		for ( Step step : workSteps ) {
 			lbSteps.add(String.format("%s  [%s]", step.name, step.id));
 		}
 		if ( index != -1 ) {
 			if (( index < 0 ) || ( index > lbSteps.getItemCount() )) {
 				index = -1;
 			}
 		}
 		if ( index == -1 ) {
 			index = 0;
 		}
 		lbSteps.select(index);
 	}
 	
 	private void addStep () {
 		try {
 			UtilityPicker dlg = new UtilityPicker(shell, availableSteps, help);
 			String display = dlg.showDialog();
 			if ( display == null ) return;
 			String id = getId(display);
 			workSteps.add(availableSteps.get(id).clone());
 			lbSteps.add(display);
 			lbSteps.select(lbSteps.getItemCount()-1);
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private void editStep () {
 		try {
 			int n = lbSteps.getSelectionIndex();
 			if ( n < 0 ) return;
 			Step step = workSteps.get(n);
 			if ( step.paramsData == null ) {
 				// No parameters for this step
 				return;
 			}
 			
 			if ( step.editorClass != null ) {
 //				params.fromString(step.paramsData);
 //				IParametersEditor editor = (IParametersEditor)Class.forName(step.editorClass).newInstance();
 //				editor.edit(paramsObject, uiContext, helpParam, projectDir)
 			}
 			else {
 				InputDialog dlg  = new InputDialog(shell, "Step Parameters", "Parameters:",
 					step.paramsData, null, 0, 200);
 				String data = dlg.showDialog();
 				if ( data == null ) return;
				step.paramsData = data;
 			}
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private void removeStep () {
 		try {
 			Dialogs.showError(shell, "Not implemented yet.", null);
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private boolean saveData () {
 		// Copy the work steps to the real object
 		wrapper.clear();
 		for ( Step step : workSteps ) {
 			wrapper.addStep(step);
 		}
 		return true;
 	}
 	
 	private void save (String path) {
 		Dialogs.showError(shell, "Not implemented yet.", null);
 /*		try {
 			if (( path == null ) || ( path.length() == 0 )) {
 				path = Dialogs.browseFilenamesForSave(shell, "Save Pipeline As", null, null, null);
 				if ( path == null ) return;
 			}
 			if ( saveData() ) return;
 			
 			// Save it
 			wrapper.save(path);
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}*/
 	}
 
 	private void load (String path) {
 		Dialogs.showError(shell, "Not implemented yet.", null);
 /*		try {
 			if (( path == null ) || ( path.length() == 0 )) {
 				String[] paths = Dialogs.browseFilenames(shell, "Load Pipeline", false, null, null, null);
 				if ( paths == null ) return;
 				else path = paths[0];
 			}
 			wrapper.load(path);
 			populate(-1);
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}*/
 	}
 
 }
