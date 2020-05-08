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
 
 package net.sf.okapi.applications.rainbow.utilities.merging;
 
 import net.sf.okapi.applications.rainbow.packages.Manifest;
 import net.sf.okapi.common.IHelp;
 import net.sf.okapi.common.ui.Dialogs;
 import net.sf.okapi.common.ui.OKCancelPanel;
 import net.sf.okapi.common.ui.UIUtil;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 
 public class ManifestDialog {
 	
 	private Shell shell;
 	private IHelp help;
 	private Manifest manifest;
 	private ManifestTableModel tableMod;
 	private SelectionAdapter CloseActions;
 	private boolean result;
 	private Text edPkgType;
 	private Text edPkgID;
 	private Text edSource;
 	private Text edTarget;
 	private Text edDate;
 	private Text edMergeInputRoot;
 	private Button chkUseApprovedOnly;
 	private Button chkUpdateApprovedFlag;
 
 	public ManifestDialog (Shell parent,
 		IHelp helpParam)
 	{
 		result = false;
 		help = helpParam;
 		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
 		shell.setText("Translation Package Manifest");
 		UIUtil.inheritIcon(shell, parent);
 		shell.setLayout(new GridLayout());
 		
 		TabFolder tfTmp = new TabFolder(shell, SWT.NONE);
 		tfTmp.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		//--- Documents tab
 		
 		Composite cmpTmp = new Composite(tfTmp, SWT.NONE);
 		cmpTmp.setLayout(new GridLayout());
 		TabItem tiTmp = new TabItem(tfTmp, SWT.NONE);
 		tiTmp.setText("Documents");
 		tiTmp.setControl(cmpTmp);
 		
 		final Table tableDocs = new Table(cmpTmp, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL);
 		GridData gdTmp = new GridData(GridData.FILL_BOTH);
 		gdTmp.minimumHeight = 300;
 		gdTmp.minimumWidth = 550;
 		tableDocs.setLayoutData(gdTmp);
 		tableDocs.setHeaderVisible(true);
 		tableDocs.setLinesVisible(true);
 		tableMod = new ManifestTableModel();
 		tableMod.linkTable(tableDocs);
 
 		edMergeInputRoot = new Text(cmpTmp, SWT.BORDER);
 		edMergeInputRoot.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		edMergeInputRoot.setEditable(false);
 		
 		//--- Options tab
 		
 		cmpTmp = new Composite(tfTmp, SWT.NONE);
 		cmpTmp.setLayout(new GridLayout());
 		tiTmp = new TabItem(tfTmp, SWT.NONE);
 		tiTmp.setText("Options");
 		tiTmp.setControl(cmpTmp);
 		
 		chkUseApprovedOnly = new Button(cmpTmp, SWT.CHECK);
 		chkUseApprovedOnly.setText("Merge the translation only if it is approved");
 		
 		chkUpdateApprovedFlag = new Button(cmpTmp, SWT.CHECK);
 		chkUpdateApprovedFlag.setText("Set the 'approved' flag on the merged translations");
 
 		//--- Information tab
 		
 		cmpTmp = new Composite(tfTmp, SWT.NONE);
 		cmpTmp.setLayout(new GridLayout(2, false));
 		tiTmp = new TabItem(tfTmp, SWT.NONE);
 		tiTmp.setText("Information");
 		tiTmp.setControl(cmpTmp);
 
 		Label stTmp = new Label(cmpTmp, SWT.NONE);
 		stTmp.setText("Package type:");
 		stTmp.setLayoutData(new GridData());
 		edPkgType = new Text(cmpTmp, SWT.BORDER);
 		edPkgType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		edPkgType.setEditable(false);
 		
 		stTmp = new Label(cmpTmp, SWT.NONE);
 		stTmp.setText("Package ID:");
 		stTmp.setLayoutData(new GridData());
 		edPkgID = new Text(cmpTmp, SWT.BORDER);
 		edPkgID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		edPkgID.setEditable(false);
 		
 		stTmp = new Label(cmpTmp, SWT.NONE);
 		stTmp.setText("Source language:");
 		stTmp.setLayoutData(new GridData());
 		edSource = new Text(cmpTmp, SWT.BORDER);
 		edSource.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		edSource.setEditable(false);
 		
 		stTmp = new Label(cmpTmp, SWT.NONE);
 		stTmp.setText("Target language:");
 		stTmp.setLayoutData(new GridData());
 		edTarget = new Text(cmpTmp, SWT.BORDER);
 		edTarget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		edTarget.setEditable(false);
 		
 		stTmp = new Label(cmpTmp, SWT.NONE);
 		stTmp.setText("Creation date:");
 		stTmp.setLayoutData(new GridData());
 		edDate = new Text(cmpTmp, SWT.BORDER);
 		edDate.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		edDate.setEditable(false);
 				
 		//--- Dialog-level buttons
 
 		CloseActions = new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if ( e.widget.getData().equals("h") ) { //$NON-NLS-1$
					if ( help != null ) help.showWiki("Rainbow - Translation Package Manifest");
 					return;
 				}
 				if ( e.widget.getData().equals("o") ) {
 					tableMod.saveData();
 					manifest.setUseApprovedOnly(chkUseApprovedOnly.getSelection());
 					manifest.setUpdateApprovedFlag(chkUpdateApprovedFlag.getSelection());
 					result = true;
 				}
 				shell.close();
 			};
 		};
 		OKCancelPanel pnlActions = new OKCancelPanel(shell, SWT.NONE, CloseActions,
 			(help!=null), "Execute");
 		pnlActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		shell.setDefaultButton(pnlActions.btOK);
 
 		shell.pack();
 		shell.setMinimumSize(shell.getSize());
 		Point startSize = shell.getMinimumSize();
 		if ( startSize.x < 450 ) startSize.x = 450;
 		shell.setSize(startSize);
 
     	Rectangle rect = tableDocs.getClientArea();
 		int nPart = (int)(rect.width / 100);
 		tableDocs.getColumn(0).setWidth(45*nPart);
 		tableDocs.getColumn(1).setWidth(15*nPart);
 		tableDocs.getColumn(2).setWidth(rect.width-(60*nPart));
 		
 		Dialogs.centerWindow(shell, parent);
 	}
 	
 	private void setData (Manifest manifest) {
 		this.manifest = manifest;
 		edMergeInputRoot.setText(manifest.getMergeInputRoot());
 		edPkgType.setText(manifest.getPackageType());
 		edPkgID.setText(manifest.getPackageID());
 		edSource.setText(manifest.getSourceLanguage().toString());
 		edTarget.setText(manifest.getTargetLanguage().toString());
 		edDate.setText(manifest.getDate());
 		chkUseApprovedOnly.setSelection(manifest.useApprovedOnly());
 		chkUpdateApprovedFlag.setSelection(manifest.updateApprovedFlag());
 	}
 	
 	public boolean showDialog (Manifest manifest) {
 		tableMod.setManifest(manifest);
 		setData(manifest);
 		shell.open();
 		while ( !shell.isDisposed() ) {
 			if ( !shell.getDisplay().readAndDispatch() )
 				shell.getDisplay().sleep();
 		}
 		return result;
 	}
 
 }
