 /*===========================================================================
   Copyright (C) 2010-2012 by the Okapi Framework contributors
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
 
 import java.io.File;
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
 
 	private static String SEPARATOR = "  -->  ";
 	
 	private Shell shell;
 	private boolean result = false;
 	private OKCancelPanel pnlActions;
 	private Parameters params;
 	private IHelp help;
 	private TabFolder tabs;
 	private Text edPackageName;
 	private TextAndBrowsePanel pnlPackageDir;
 	private Button btCreateZip;
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
 	private Button chkSendOutput;
 	private Text edAncilOrigin;
 	private Text edAncilDestination;
 	private Button btAddAncilFile;
 	private Button btRemoveAncilFile;
 	private List lbAncilList;
 	private boolean supportFileMode;
 	
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
 		if ( supportFileMode ) {
 			return null;
 		}
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
 				if ( supportFileMode ) {
 					e.doit = false;
 					return;
 				}
 				if ( e.widget.getData().equals("o") ) {
 					saveData();
 				}
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
 		// XLIFF-RTF options
 		optEditors.add(null);
 		optStrings.add(null);
 		optMoreInfo.add("Rainbow TKit - XLIFF with RTF"); // wiki page
 		writers.add("net.sf.okapi.steps.rainbowkit.xliffrtf.XLIFFRTFPackageWriter");
 		// OmegaT options
 		optEditors.add("net.sf.okapi.steps.rainbowkit.omegat.Options");
 		optStrings.add(createParameters(optEditors.get(optEditors.size()-1)));
 		optMoreInfo.add("Rainbow TKit - OmegaT Project"); // wiki page
 		writers.add("net.sf.okapi.steps.rainbowkit.omegat.OmegaTPackageWriter");
 		// Transifex options
 		optEditors.add("net.sf.okapi.steps.rainbowkit.transifex.Parameters");
 		optStrings.add(createParameters(optEditors.get(optEditors.size()-1)));
 		optMoreInfo.add("Rainbow TKit - Transifex Project"); // wiki page
 		writers.add("net.sf.okapi.steps.rainbowkit.transifex.TransifexPackageWriter");
 		// ONTRAM options
 		optEditors.add(null);
 		optStrings.add(null);
 		optMoreInfo.add("Rainbow TKit - ONTRAM XINI"); // wiki page
 		writers.add("net.sf.okapi.steps.rainbowkit.ontram.OntramPackageWriter");
 		// XLIFF 2
 		optEditors.add("net.sf.okapi.steps.rainbowkit.xliff.XLIFF2Options");
 		optStrings.add(createParameters(optEditors.get(optEditors.size()-1)));
 		optMoreInfo.add("Rainbow TKit - XLIFF 2.0"); // wiki page
 		writers.add("net.sf.okapi.steps.rainbowkit.xliff.XLIFF2PackageWriter");
 		// Versified RTF options
 		optEditors.add(null);
 		optStrings.add(null);
 		optMoreInfo.add("Rainbow TKit - Versified with RTF"); // wiki page
 		writers.add("net.sf.okapi.steps.rainbowkit.versified.VersifiedRtfPackageWriter");
 		// Table options
 		optEditors.add("net.sf.okapi.filters.transtable.Parameters");
 		optStrings.add(createParameters(optEditors.get(optEditors.size()-1)));
 		optMoreInfo.add("Rainbow TKit - Translation Table"); // wiki page
 		writers.add("net.sf.okapi.steps.rainbowkit.table.TablePackageWriter");
 
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
 		
 		lbTypes = new List(cmpTmp, SWT.BORDER | SWT.V_SCROLL);
 		lbTypes.add("Generic XLIFF");
 		lbTypes.setData("0", "net.sf.okapi.steps.rainbowkit.xliff.XLIFFPackageWriter");
 		
 		lbTypes.add("PO Package");
 		lbTypes.setData("1", "net.sf.okapi.steps.rainbowkit.po.POPackageWriter");
 
 		lbTypes.add("Original with RTF");
 		lbTypes.setData("2", "net.sf.okapi.steps.rainbowkit.rtf.RTFPackageWriter");
 
 		lbTypes.add("XLIFF with RTF");
 		lbTypes.setData("3", "net.sf.okapi.steps.rainbowkit.xliffrtf.XLIFFRTFPackageWriter");
 
 		lbTypes.add("OmegaT Project");
 		lbTypes.setData("4", "net.sf.okapi.steps.rainbowkit.omegat.OmegaTPackageWriter");
 
 		lbTypes.add("Transifex Project");
 		lbTypes.setData("5", "net.sf.okapi.steps.rainbowkit.transifex.TransifexPackageWriter");
 
 		lbTypes.add("ONTRAM XINI");
 		lbTypes.setData("6", "net.sf.okapi.steps.rainbowkit.ontram.OntramPackageWriter");
 
 		lbTypes.add("XLIFF 2.0 (Experimental - output only)");
 		lbTypes.setData("7", "net.sf.okapi.steps.rainbowkit.xliff.XLIFF2PackageWriter");
 		
 		lbTypes.add("Versified with RTF (Beta)");
 		lbTypes.setData("8", "net.sf.okapi.steps.rainbowkit.versified.VersifiedRtfPackageWriter");
 
 		lbTypes.add("Translation Table (Beta)");
 		lbTypes.setData("9", "net.sf.okapi.steps.rainbowkit.table.TablePackageWriter");
 
 		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.heightHint = 70;
 		gdTmp.verticalSpan = 2;
 		lbTypes.setLayoutData(gdTmp);
 		lbTypes.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				updatePackageType();
 			}
 		});
 		
 		final int btnWidth = 95;
 		btOptions = new Button(cmpTmp, SWT.PUSH);
 		btOptions.setText("&Options...");
 		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
 		btOptions.setLayoutData(gdTmp);
 		UIUtil.ensureWidth(btOptions, btnWidth);
 		btOptions.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				editOptions();
 			}
 		});
 		
 		btHelp = new Button(cmpTmp, SWT.PUSH);
 		btHelp.setText("&More Info");
 		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
 		btHelp.setLayoutData(gdTmp);
 		UIUtil.ensureWidth(btHelp, btnWidth);
 		btHelp.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				callMoreInfo();
 			}
 		});
 		
 		edDescription = new Text(cmpTmp, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
 		edDescription.setEditable(false);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.heightHint = 50;
 		gdTmp.horizontalSpan = 2;		
 		edDescription.setLayoutData(gdTmp);
 		
 		chkSendOutput = new Button(cmpTmp, SWT.CHECK);
 		chkSendOutput.setText("Send the prepared files to the next step");
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		gdTmp.horizontalSpan = 2;
 		chkSendOutput.setLayoutData(gdTmp);
 
 		//--- Location tab
 		
 		cmpTmp = new Composite(tabs, SWT.NONE);
 		cmpTmp.setLayout(new GridLayout(2, false));
 		tiTmp = new TabItem(tabs, SWT.NONE);
 		tiTmp.setText("Output Location");
 		tiTmp.setControl(cmpTmp);
 
 		label = new Label(cmpTmp, SWT.NONE);
 		label.setText("Root of the output directory:");
 		new Label(cmpTmp, SWT.NONE);
 		
 		pnlPackageDir = new TextAndBrowsePanel(cmpTmp, SWT.NONE, true);
 		gdTmp = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
 		pnlPackageDir.setLayoutData(gdTmp);
 
 		label = new Label(cmpTmp, SWT.NONE);
 		label.setText("Name of the package:");
 		new Label(cmpTmp, SWT.NONE);
 		
 		edPackageName = new Text(cmpTmp, SWT.BORDER);
 		edPackageName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
 		
 		btCreateZip = new Button(cmpTmp, SWT.CHECK);
 		btCreateZip.setText("Create a ZIP file for the package");
 
 		//--- Support Material tab
 		
 		cmpTmp = new Composite(tabs, SWT.NONE);
 		cmpTmp.setLayout(new GridLayout(1, false));
 		tiTmp = new TabItem(tabs, SWT.NONE);
 		tiTmp.setText("Support Material");
 		tiTmp.setControl(cmpTmp);
 		
 		label = new Label(cmpTmp, SWT.NONE);
 		label.setText("List of the files to include in the package");
 
 		lbAncilList = new List(cmpTmp, SWT.BORDER | SWT.V_SCROLL);
		lbAncilList.setLayoutData(new GridData(GridData.FILL_BOTH));
 		
 		Composite cmpTmp2 = new Composite(cmpTmp, SWT.NONE);
 		cmpTmp2.setLayout(new GridLayout(3, false));
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.verticalAlignment = GridData.VERTICAL_ALIGN_END;
 		cmpTmp2.setLayoutData(gdTmp);
 		
 		btAddAncilFile = UIUtil.createGridButton(cmpTmp2, SWT.PUSH, "Add...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
 		btAddAncilFile.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if ( supportFileMode ) endSupportFileMode(true);
 				else startSupportFileMode();
 			}
 		});
 
 		label = new Label(cmpTmp2, SWT.NONE);
 		label.setText("File(s) path");
 		
 		edAncilOrigin = new Text(cmpTmp2, SWT.BORDER);
 		edAncilOrigin.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		btRemoveAncilFile = UIUtil.createGridButton(cmpTmp2, SWT.PUSH, "Remove", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
 		btRemoveAncilFile.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if ( supportFileMode ) endSupportFileMode(false);
 				else removeAncillaryFile();
 			}
 		});
 		
 		label = new Label(cmpTmp2, SWT.NONE);
 		label.setText("Destination");
 		
 		edAncilDestination = new Text(cmpTmp2, SWT.BORDER);
 		edAncilDestination.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		endSupportFileMode(false);
 	}
 	
 	private void removeAncillaryFile () {
 		try {
 			int n = lbAncilList.getSelectionIndex();
 			if ( n == -1 ) return;
 			lbAncilList.remove(n);
 			if ( n >= lbAncilList.getItemCount() ) n = lbAncilList.getItemCount()-1;
 			lbAncilList.select(n);
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private void startSupportFileMode () {
 		try {
 			supportFileMode = true;
 			btAddAncilFile.setText("Accept");
 			btRemoveAncilFile.setText("Discard");
 			edAncilOrigin.setEnabled(true);
 			edAncilDestination.setEnabled(true);
 			edAncilDestination.setText(File.separator+Parameters.SUPPORTFILE_SAMENAME);
 			edAncilOrigin.setFocus();
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private boolean endSupportFileMode (boolean saveData) {
 		try {
 			if ( saveData ) {
 				String origin = edAncilOrigin.getText().trim();
 				if ( origin.isEmpty() ) {
 					Dialogs.showError(shell, "You must specify a file or a pattern.", null);
 					edAncilOrigin.setFocus();
 					return false;
 				}
 				String destination = edAncilDestination.getText().trim();
 				if ( destination.isEmpty() ) {
 					Dialogs.showError(shell, "You must specify a destination.", null);
 					edAncilDestination.setFocus();
 					return false;
 				}
 				lbAncilList.add(origin+SEPARATOR+destination);
 				lbAncilList.select(lbAncilList.getItemCount()-1);
 			}
 			btAddAncilFile.setText("Add...");
 			btRemoveAncilFile.setText("Remove");
 			edAncilOrigin.setEnabled(false);
 			edAncilOrigin.setText("");
 			edAncilDestination.setEnabled(false);
 			edAncilDestination.setText("");
 			supportFileMode = false;
 			return true;
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 			return false;
 		}
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
 			edDescription.setText("Simple package where translatable files are extracted into XLIFF documents.\n"
 				+ "You can translate this package with any XLIFF editor and many XML-enabled tools.");
 			break;
 		case 1: // PO
 			btOptions.setEnabled(false);
 			edDescription.setText("Simple package where translatable files are extracted into PO files.\n"
 				+ "You can translate this package with any PO editor.");
 			break;
 		case 2: // Original with RTF
 			btOptions.setEnabled(false);
 			edDescription.setText("Package where the files to translate are converted into an RTF file with Trados-compatible styles.\n"
 				+ "You can translate this package with Trados Translator's Workbench or any compatible tool.");
 			break;
 		case 3: // XLIFF with RTF
 			btOptions.setEnabled(false);
 			edDescription.setText("Package where the files are extracted to XLIFF then converted into an Trados-compatible RTF file.\n"
 				+ "You can translate this package with Trados Translator's Workbench or any compatible tool.");
 			break;
 		case 4: // OmegaT
 			btOptions.setEnabled(optEditors.get(n)!=null);
 			edDescription.setText("OmegaT project with all its files and directory structure in place.\n"
 				+ "You can translate this package with OmegaT.");
 			break;
 		case 5: // Transifex
 			btOptions.setEnabled(optEditors.get(n)!=null);
 			edDescription.setText("Package where translatable files are uploaded to an online Transifex project.\n"
 				+ "You can translate this package with the online Transifex editor or locally with PO editors. "
 				+ "You can also use OmegaT to access the remote project directly.");
 			break;
 		case 6: // ONTRAM
 			btOptions.setEnabled(false);
 			edDescription.setText("Simple package where translatable files are extracted into a XINI document.\n"
 				+ "You can translate this package with ONTRAM.");
 			break;
 		case 7: // XLIFF 2
 			btOptions.setEnabled(optEditors.get(n)!=null);
 			edDescription.setText("Simple package with XLIFF 2.0 experimental files.\n"
 				+ "Not for real projects. The XLIFF 2.0 format is defined yet. See XLIFF TC for more information or to provide feedback.");
 			break;
 		case 8: // Versified Text with RTF
 			btOptions.setEnabled(false);
 			edDescription.setText("Package where the files to translate are converted into a Versified RTF file with Trados-compatible styles.\n"
 				+ "You can translate this package with Trados Translator's Workbench, WordFast or any RTF compatible editor.");
 			break;
 		case 9: // Table
 			btOptions.setEnabled(optEditors.get(n)!=null);
 			edDescription.setText("Package where the files to translate are converted into tab-delimited tables.\n"
 				+ "You can translate this package with a spreadsheet application.");
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
 		btCreateZip.setSelection(params.getCreateZip());
 
 		chkSendOutput.setSelection(params.getSendOutput());
 		String current = params.getWriterClass();
 		int n = 0;
 		for ( String str : writers ) {
 			if ( str.equals(current) ) break; // Found it
 			else n++;
 		}
 		lbTypes.select(n);
 		IParameters p = optStrings.get(n);
 		if ( p != null ) {
 			p.fromString(params.getWriterOptions());
 		}
 		
 		// Support material
 		java.util.List<String> list = params.convertSupportFilesToList(params.getSupportFiles());
 		for ( String item : list ) {
 			lbAncilList.add(item.replace(Parameters.SUPPORTFILEDEST_SEP, SEPARATOR));
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
 		params.setCreateZip(btCreateZip.getSelection());
 		
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
 		
 		// Support material
 		ArrayList<String> list = new ArrayList<String>();
 		for ( String item : lbAncilList.getItems() ) {
 			list.add(item.replace(SEPARATOR, Parameters.SUPPORTFILEDEST_SEP));
 		}
 		String tmp = "";
 		if ( !list.isEmpty() ) {
 			tmp = params.convertSupportFilesToString(list);
 		}
 		params.setSupportFiles(tmp);
 
 		params.setSendOutput(chkSendOutput.getSelection());
 		
 		result = true;
 		return result;
 	}
 	
 }
