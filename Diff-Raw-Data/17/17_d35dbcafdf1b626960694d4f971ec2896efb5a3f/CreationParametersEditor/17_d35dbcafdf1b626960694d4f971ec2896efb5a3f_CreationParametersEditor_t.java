 /*===========================================================================
   Copyright (C) 2010-2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.steps.rainbowkit.ui;
 
 import java.util.ArrayList;
 
 import net.sf.okapi.common.EditorFor;
 import net.sf.okapi.common.IContext;
 import net.sf.okapi.common.IHelp;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IParametersEditor;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.ui.Dialogs;
 import net.sf.okapi.common.ui.ISWTEmbeddableParametersEditor;
 import net.sf.okapi.common.ui.OKCancelPanel;
 import net.sf.okapi.common.ui.TextAndBrowsePanel;
 import net.sf.okapi.common.ui.UIUtil;
 import net.sf.okapi.common.ui.genericeditor.GenericEditor;
 import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
 import net.sf.okapi.steps.rainbowkit.creation.Parameters;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Text;
 
 @EditorFor(Parameters.class)
 public class CreationParametersEditor implements IParametersEditor, ISWTEmbeddableParametersEditor {
 	
 	private Shell shell;
 	private boolean result = false;
 	private OKCancelPanel pnlActions;
 	private Parameters params;
 	private IHelp help;
 	private TabFolder tabs;
 	private Text edPackageName;
 	private TextAndBrowsePanel pnlPackageDir;
 	private Composite mainComposite;
 	private List lbTypes;
 	private Button btOptions;
 	private Button btHelp;
 	private Text edDescription;
 	private GenericEditor gedit;
 	private ArrayList<String> optEditors;
 	private ArrayList<String> writers;
 	private ArrayList<IParameters> optStrings;
 	private ArrayList<String> optMoreInfo;
 	private IContext context;
 	
 	public boolean edit (IParameters params,
 		boolean readOnly,
 		IContext context)
 	{
 		boolean bRes = false;
 		try {
 			shell = null;
 			this.context = context;
 			help = (IHelp)context.getObject("help");
 			this.params = (Parameters)params;
 			
 			shell = new Shell((Shell)context.getObject("shell"), SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
 			create((Shell)context.getObject("shell"), readOnly);
 			return showDialog();
 		}
 		catch ( Exception e ) {
 			Dialogs.showError(shell, e.getLocalizedMessage(), null);
 			bRes = false;
 		}
 		finally {
 			// Dispose of the shell, but not of the display
 			if ( shell != null ) shell.dispose();
 		}
 		return bRes;
 	}
 	
 	public IParameters createParameters () {
 		return new Parameters();
 	}
 	
 	@Override
 	public Composite getComposite () {
 		return mainComposite;
 	}
 
 	@Override
 	public void initializeEmbeddableEditor (Composite parent,
 		IParameters paramsObject,
 		IContext context)
 	{
 		shell = (Shell)context.getObject("shell");
 		help = (IHelp)context.getObject("help");
 		this.context = context;
 		params = (Parameters)paramsObject; 
 
 		createComposite(parent);
 		setData();
 	}
 
 	@Override
 	public String validateAndSaveParameters () {
 		if ( !saveData() ) return null;
 		return params.toString();
 	}
 	
 	private void create (Shell parent,
 		boolean readOnly)
 	{
 		shell.setText("Translation Kit Creation");
 		if ( parent != null ) UIUtil.inheritIcon(shell, parent);
 		GridLayout layTmp = new GridLayout();
 		layTmp.marginBottom = 0;
 		layTmp.verticalSpacing = 0;
 		shell.setLayout(layTmp);
 
 		createComposite(shell);
 
 		//--- Dialog-level buttons
 
 		SelectionAdapter OKCancelActions = new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				result = false;
 				if ( e.widget.getData().equals("h") ) {
 					if ( help != null ) help.showWiki("Translation Kit Creation Step");
 					return;
 				}
 				if ( e.widget.getData().equals("o") ) saveData();
 				shell.close();
 			};
 		};
 		pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
 		pnlActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		pnlActions.btOK.setEnabled(!readOnly);
 		if ( !readOnly ) {
 			shell.setDefaultButton(pnlActions.btOK);
 		}
 
 		setData();
 		shell.pack();
 		shell.setMinimumSize(shell.getSize());
 		Dialogs.centerWindow(shell, parent);
 	}
 	
 	private void createComposite (Composite parent) {
 		optEditors = new ArrayList<String>();
 		optStrings = new ArrayList<IParameters>();
 		optMoreInfo = new ArrayList<String>();
 		writers = new ArrayList<String>();
 		// XLIFF options
 		optEditors.add("net.sf.okapi.steps.rainbowkit.xliff.Options");
 		optStrings.add(createParameters(optEditors.get(optEditors.size()-1)));
 		optMoreInfo.add("Rainbow TKit - Generic XLIFF"); // wiki page
 		writers.add("net.sf.okapi.steps.rainbowkit.xliff.XLIFFPackageWriter");
 		// PO options
 		optEditors.add(null);
 		optStrings.add(null);
 		optMoreInfo.add("Rainbow TKit - PO Package"); // wiki page
 		writers.add("net.sf.okapi.steps.rainbowkit.po.POPackageWriter");
 		// RTF options
 		optEditors.add(null);
 		optStrings.add(null);
 		optMoreInfo.add("Rainbow TKit - Original with RTF"); // wiki page
 		writers.add("net.sf.okapi.steps.rainbowkit.rtf.RTFPackageWriter");
 		// OmegaT options
 		optEditors.add(null);
 		optStrings.add(null);
 		optMoreInfo.add("Rainbow TKit - OmegaT Project"); // wiki page
 		writers.add("net.sf.okapi.steps.rainbowkit.omegat.OmegaTPackageWriter");
 		// Transifex options
 		optEditors.add("net.sf.okapi.steps.rainbowkit.transifex.Parameters");
 		optStrings.add(createParameters(optEditors.get(optEditors.size()-1)));
 		optMoreInfo.add("Rainbow TKit - Transifex Project"); // wiki page
 		writers.add("net.sf.okapi.steps.rainbowkit.transifex.TransifexPackageWriter");
 
 		mainComposite = new Composite(parent, SWT.BORDER);
 		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		GridLayout layout = new GridLayout();
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		mainComposite.setLayout(layout);
 		
 		tabs = new TabFolder(mainComposite, SWT.NONE);
 		tabs.setLayout(new GridLayout());
 		GridData gdTmp = new GridData(GridData.FILL_BOTH);
 //		// Auto-size is too high, we need to fix it manually
 //		gdTmp.heightHint = 430;
 		tabs.setLayoutData(gdTmp);
 
 		//--- Output format tab
 		
 		Composite cmpTmp = new Composite(tabs, SWT.NONE);
 		cmpTmp.setLayout(new GridLayout(2, false));
 		TabItem tiTmp = new TabItem(tabs, SWT.NONE);
 		tiTmp.setText("Package Format");
 		tiTmp.setControl(cmpTmp);
 
 		Label label = new Label(cmpTmp, SWT.NONE);
 		label.setText("Type of package to create:");
 		gdTmp = new GridData();
 		gdTmp.horizontalSpan = 2;
 		label.setLayoutData(gdTmp);
 		
 		lbTypes = new List(cmpTmp, SWT.BORDER);
 		lbTypes.add("Generic XLIFF");
 		lbTypes.setData("0", "net.sf.okapi.steps.rainbowkit.xliff.XLIFFPackageWriter");
 		
 		lbTypes.add("PO Package");
 		lbTypes.setData("1", "net.sf.okapi.steps.rainbowkit.po.POPackageWriter");
 
 		lbTypes.add("Original with RTF");
 		lbTypes.setData("2", "net.sf.okapi.steps.rainbowkit.rtf.RTFPackageWriter");
 
 		lbTypes.add("OmegaT Project");
 		lbTypes.setData("3", "net.sf.okapi.steps.rainbowkit.omegat.OmegaTPackageWriter");
 
 		lbTypes.add("Transifex Project");
 		lbTypes.setData("4", "net.sf.okapi.steps.rainbowkit.transifex.TransifexPackageWriter");
 
 		gdTmp = new GridData(GridData.FILL_BOTH);
 		gdTmp.heightHint = 70;
 		gdTmp.horizontalSpan = 2;
 		lbTypes.setLayoutData(gdTmp);
 		lbTypes.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				updatePackageType();
 			}
 		});
 		
 		btOptions = new Button(cmpTmp, SWT.PUSH);
 		btOptions.setText("&Options...");
 		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
 		btOptions.setLayoutData(gdTmp);
 		UIUtil.ensureWidth(btOptions, 80);
 		btOptions.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				editOptions();
 			}
 		});
 		
 		edDescription = new Text(cmpTmp, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
 		edDescription.setEditable(false);
 		gdTmp = new GridData(GridData.FILL_BOTH);
 		gdTmp.heightHint = 60;
 		gdTmp.verticalSpan = 2;
 		edDescription.setLayoutData(gdTmp);
 
 		btHelp = new Button(cmpTmp, SWT.PUSH);
 		btHelp.setText("&More Info");
 		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
 		btHelp.setLayoutData(gdTmp);
 		UIUtil.ensureWidth(btHelp, 80);
 		btHelp.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				callMoreInfo();
 			}
 		});
 		
 		//--- Location tab
 		
 		cmpTmp = new Composite(tabs, SWT.NONE);
 		cmpTmp.setLayout(new GridLayout());
 		tiTmp = new TabItem(tabs, SWT.NONE);
 		tiTmp.setText("Output Location");
 		tiTmp.setControl(cmpTmp);
 
 		label = new Label(cmpTmp, SWT.NONE);
 		label.setText("Root of the output directory:");
 		pnlPackageDir = new TextAndBrowsePanel(cmpTmp, SWT.NONE, true);
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		pnlPackageDir.setLayoutData(gdTmp);
 
 		label = new Label(cmpTmp, SWT.NONE);
 		label.setText("Name of the package:");
 		edPackageName = new Text(cmpTmp, SWT.BORDER);
 		edPackageName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 	}
 	
 	private void updatePackageType () {
 		int n = lbTypes.getSelectionIndex();
 		if ( n == -1 ) {
 			btOptions.setEnabled(false);
 			edDescription.setText("");
 			return;
 		}
 		switch ( n ) {
 		case 0: // XLIFF
 			btOptions.setEnabled(optEditors.get(n)!=null);
 			edDescription.setText("Simple package where all files to translate are extracted into XLIFF documents.\n"
				+ "You can translate this package with any XLIFF editor and many XML-enabled tools.");
 			break;
 		case 1: // PO
 			btOptions.setEnabled(false);
 			edDescription.setText("Simple package where all files to translate are extracted into PO files.\n"
 				+ "You can translate this package with any PO editor.");
 			break;
 		case 2: // Original with RTF
 			btOptions.setEnabled(false);
 			edDescription.setText("Package where all the files to translate are converted into an RTF file with Trados-compatible styles.\n"
 				+ "You can translate this package with Trados Translator's Workbench or any compatible tool.");
 			break;
 		case 3: // OmegaT
 			btOptions.setEnabled(false);
 			edDescription.setText("OmegaT project with all its files and directory structure in place.\n"
 				+ "You can translate this package with OmegaT.");
 			break;
 		case 4: // Transifex
 			btOptions.setEnabled(optEditors.get(n)!=null);
 			edDescription.setText("Package where translatable files are uploaded to an online Transifex project.\n"
 				+ "You can translate this package with the online Transifex editor or locally with PO editors.");
 			break;
 		}
 	}
 
 	private IParameters createParameters (String className) {
 		IParameters p = null;
 		try {
 			p = (IParameters)Class.forName(className).newInstance();
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getLocalizedMessage(), null);
 		}
 		return p;
 	}
 	
 	private void editOptions () {
 		try {
 			int n = lbTypes.getSelectionIndex();
 			if ( n == -1 ) return;
 			// Create the editor description
 			IEditorDescriptionProvider descProv = (IEditorDescriptionProvider)Class.forName(
 				optEditors.get(n)).newInstance();
 			// Create the generic editor if needed
 			if ( gedit == null ) {
 				gedit = new GenericEditor();
 			}
 			// Get the parameters
 			IParameters p = optStrings.get(n);
 			if ( !gedit.edit(p, descProv, false, context) ) {
 				return; // Cancel
 			}
 			// Else: Save the data
 			optStrings.set(n, p);
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private void callMoreInfo () {
 		try {
 			int n = lbTypes.getSelectionIndex();
 			if ( n == -1 ) return;
 			Util.openWikiTopic(optMoreInfo.get(n));
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private boolean showDialog () {
 		shell.open();
 		while ( !shell.isDisposed() ) {
 			if ( !shell.getDisplay().readAndDispatch() )
 				shell.getDisplay().sleep();
 		}
 		return result;
 	}
 
 	private void setData () {
 		pnlPackageDir.setText(params.getPackageDirectory());
 		edPackageName.setText(params.getPackageName());
 
 		String current = params.getWriterClass();
 		int n = 0;
 		for ( String str : writers ) {
 			if ( str.equals(current) ) break; // Found it
 			else n++;
 		}
 		lbTypes.setSelection(n);
 		IParameters p = optStrings.get(n);
 		if ( p != null ) {
 			p.fromString(params.getWriterOptions());
 		}
 		
 		updatePackageType();
 	}
 
 	private boolean saveData () {
 		result = false;
 		if ( pnlPackageDir.getText().trim().length() == 0 ) {
 			//TODO: error box
 			return result;
 		}
 		if ( edPackageName.getText().trim().length() == 0 ) {
 			//TODO: error box
 			return result;
 		}
 		params.setPackageDirectory(pnlPackageDir.getText().trim());
 		params.setPackageName(edPackageName.getText().trim());
 		
 		int n = lbTypes.getSelectionIndex();
 		// Writer type/class
 		params.setWriterClass((String)lbTypes.getData(String.valueOf(n)));
 		// Writer options
 		IParameters p = optStrings.get(n);
 		if ( p != null ) {
 			params.setWriterOptions(p.toString());
 		}
 		else {
 			params.setWriterOptions(null);
 		}
 		result = true;
 		return result;
 	}
 	
 }
