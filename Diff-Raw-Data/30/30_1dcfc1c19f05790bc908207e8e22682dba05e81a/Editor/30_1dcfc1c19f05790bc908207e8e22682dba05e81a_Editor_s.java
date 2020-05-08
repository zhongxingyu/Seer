 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
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
 
 package net.sf.okapi.applications.rainbow.utilities.encodingconversion;
 
 import net.sf.okapi.common.IContext;
 import net.sf.okapi.common.IHelp;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IParametersEditor;
 import net.sf.okapi.common.ui.Dialogs;
 import net.sf.okapi.common.ui.OKCancelPanel;
 import net.sf.okapi.common.ui.UIUtil;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Text;
 
 public class Editor implements IParametersEditor {
 	
 	private Shell shell;
 	private boolean result = false;
 	private OKCancelPanel pnlActions;
 	private Parameters params;
 	private Button chkUnescapeNCR;
 	private Button chkUnescapeCER;     
 	private Button chkUnescapeJava;
 	private Button rdEscapeToNCRHexaU;
 	private Button rdEscapeToNCRHexaL;
 	private Button rdEscapeToNCRDeci;
 	private Button rdEscapeToCER;
 	private Button rdEscapeToJavaU;
 	private Button rdEscapeToJavaL;
 	private Button rdEscapeToUserFormat;
 	private Text edUserFormat;
 	private Button rdEscapeUnsupported;
 	private Button rdEscapeAll;
 	private String formattedOutput;
 	private Button chkUseBytes;
 	private Button chkBOMonUTF8;
 	private Button chkReportUnsupported;
 	private IHelp help;
 
 	public boolean edit (IParameters params,
 		boolean readOnly,
 		IContext context)
 	{
 		boolean bRes = false;
 		try {
 			shell = null;
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
 	
 	private void create (Shell parent,
 		boolean readOnly)
 	{
 		shell.setText("Encoding Conversion");
 		if ( parent != null ) UIUtil.inheritIcon(shell, parent);
 		GridLayout layTmp = new GridLayout();
 		layTmp.marginBottom = 0;
 		layTmp.verticalSpacing = 0;
 		shell.setLayout(layTmp);
 
 		TabFolder tfTmp = new TabFolder(shell, SWT.NONE);
 		tfTmp.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		//--- Input tab
 
 		Composite cmpTmp = new Composite(tfTmp, SWT.NONE);
 		cmpTmp.setLayout(new GridLayout());
 		TabItem tiTmp = new TabItem(tfTmp, SWT.NONE);
 		tiTmp.setText("Input");
 		tiTmp.setControl(cmpTmp);
 
 		Group group = new Group(cmpTmp, SWT.NONE);
 		group.setLayout(new GridLayout());
 		group.setText("Un-escape the following notations");
 		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		chkUnescapeNCR = new Button(group, SWT.CHECK);
 		chkUnescapeNCR.setText("Numeric character references (&&#225; or &&#xE1; or &&&xe1; --> \u00e1)");
 		
 		chkUnescapeCER = new Button(group, SWT.CHECK);
 		chkUnescapeCER.setText("Character entity references (&&aacute; --> \u00e1)");
 		
 		chkUnescapeJava = new Button(group, SWT.CHECK);
 		chkUnescapeJava.setText("Java-style escape notation (\\u00E1 or \\u00e1 --> \u00e1)");
 		
 		//--- Output tab
 
 		cmpTmp = new Composite(tfTmp, SWT.NONE);
 		cmpTmp.setLayout(new GridLayout());
 		tiTmp = new TabItem(tfTmp, SWT.NONE);
 		tiTmp.setText("Output");
 		tiTmp.setControl(cmpTmp);
 
 		group = new Group(cmpTmp, SWT.NONE);
 		group.setLayout(new GridLayout());
 		group.setText("Escape notation to use");
 		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		rdEscapeToNCRHexaU = new Button(group, SWT.RADIO);
 		rdEscapeToNCRHexaU.setText("Uppercase hexadecimal numeric character reference (\u00e1 --> &&#xE1;)");
 		
 		rdEscapeToNCRHexaL = new Button(group, SWT.RADIO);
 		rdEscapeToNCRHexaL.setText("Lowercase hexadecimal numeric character reference (\u00e1 --> &&#xe1;)");
 		
 		rdEscapeToNCRDeci = new Button(group, SWT.RADIO);
 		rdEscapeToNCRDeci.setText("Decimal numeric character reference (\u00e1 --> &&#224;)");
 		
 		rdEscapeToCER = new Button(group, SWT.RADIO);
 		rdEscapeToCER.setText("Character entity reference (\u00e1 --> &&aacute;)");
 		
 		rdEscapeToJavaU = new Button(group, SWT.RADIO);
 		rdEscapeToJavaU.setText("Uppercase Java-style notation (\u00e1 --> \\u00E1)");
 		
 		rdEscapeToJavaL = new Button(group, SWT.RADIO);
 		rdEscapeToJavaL.setText("Lowrcase Java-style notation (\u00e1 --> \\u00e1)");
 
 		formattedOutput = "User-defined notation (\u00e1 --> %s)";
 		rdEscapeToUserFormat = new Button(group, SWT.RADIO);
 		rdEscapeToUserFormat.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		rdEscapeToUserFormat.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				edUserFormat.setEnabled(rdEscapeToUserFormat.getSelection());
 				chkUseBytes.setEnabled(rdEscapeToUserFormat.getSelection());
 			}
 		});
 		
 		edUserFormat = new Text(group, SWT.BORDER);
 		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		gdTmp.horizontalIndent = 16;
 		edUserFormat.setLayoutData(gdTmp);
 		edUserFormat.addModifyListener(new ModifyListener () {
 			public void modifyText(ModifyEvent e) {
 				updateUserOutput();
 			}
 		});
 		
 		chkUseBytes = new Button(group, SWT.CHECK);
 		chkUseBytes.setText("Use the byte values");
 		gdTmp = new GridData();
 		gdTmp.horizontalIndent = 16;
 		chkUseBytes.setLayoutData(gdTmp);
 		
 		group = new Group(cmpTmp, SWT.NONE);
 		group.setLayout(new GridLayout());
 		group.setText("What characters should be escaped");
 		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		rdEscapeUnsupported = new Button(group, SWT.RADIO);
 		rdEscapeUnsupported.setText("Only the characters un-supported by the output encoding");
 		
 		rdEscapeAll = new Button(group, SWT.RADIO);
 		rdEscapeAll.setText("All extended characters");
 
 		group = new Group(cmpTmp, SWT.NONE);
 		group.setLayout(new GridLayout());
 		group.setText("Miscellaneous");
 		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		
 		chkBOMonUTF8 = new Button(group, SWT.CHECK);
 		chkBOMonUTF8.setText("Use Byte-Order-Mark for UTF-8 output");
 		
 		chkReportUnsupported = new Button(group, SWT.CHECK);
 		chkReportUnsupported.setText("List characters not supported by the output encoding");
 
 		//--- Dialog-level buttons
 
 		SelectionAdapter OKCancelActions = new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				result = false;
 				if ( e.widget.getData().equals("h") ) {
					if ( help != null ) help.showTopic(this, "index");
 					return;
 				}
 				if ( e.widget.getData().equals("o") ) saveData();
 				shell.close();
 			};
 		};
 		pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true, "Execute");
 		pnlActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		pnlActions.btOK.setEnabled(!readOnly);
 		if ( !readOnly ) {
 			shell.setDefaultButton(pnlActions.btOK);
 		}
 
 		setData();
 		
 		updateUserOutput();
 		edUserFormat.setEnabled(rdEscapeToUserFormat.getSelection());
 		chkUseBytes.setEnabled(rdEscapeToUserFormat.getSelection());
 		
 		shell.pack();
 		shell.setMinimumSize(shell.getSize());
 		Dialogs.centerWindow(shell, parent);
 	}
 
 	private void updateUserOutput () {
 		String tmp = edUserFormat.getText();
 		if ( tmp.length() == 0 ) tmp = "?";
 		else {
 			try {
 				tmp = String.format(tmp, (int)0x00e1);
 			}
 			catch ( Exception e ) {
 				tmp = "<!ERROR!>";
 			}
 		}
 		rdEscapeToUserFormat.setText(String.format(formattedOutput, tmp));
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
 		chkUnescapeNCR.setSelection(params.unescapeNCR);
 		chkUnescapeCER.setSelection(params.unescapeCER);
 		chkUnescapeJava.setSelection(params.unescapeJava);
 
 		switch ( params.escapeNotation ) {
 		case Parameters.ESCAPE_NCRDECI:
 			rdEscapeToNCRDeci.setSelection(true);
 			break;
 		case Parameters.ESCAPE_CER:
 			rdEscapeToCER.setSelection(true);
 			break;
 		case Parameters.ESCAPE_JAVAU:
 			rdEscapeToJavaU.setSelection(true);
 			break;
 		case Parameters.ESCAPE_JAVAL:
 			rdEscapeToJavaL.setSelection(true);
 			break;
 		case Parameters.ESCAPE_USERFORMAT:
 			rdEscapeToUserFormat.setSelection(true);
 			break;
 		case Parameters.ESCAPE_NCRHEXAL:
 			rdEscapeToNCRHexaL.setSelection(true);
 			break;
 		case Parameters.ESCAPE_NCRHEXAU:
 		default:
 			rdEscapeToNCRHexaU.setSelection(true);
 			break;
 		}
 		edUserFormat.setText(params.userFormat);
 		chkUseBytes.setSelection(params.useBytes);
 		rdEscapeAll.setSelection(params.escapeAll);
 		rdEscapeUnsupported.setSelection(!params.escapeAll);
 		chkBOMonUTF8.setSelection(params.BOMonUTF8);
 		chkReportUnsupported.setSelection(params.reportUnsupported);
 	}
 
 	private boolean saveData () {
 		params.unescapeNCR = chkUnescapeNCR.getSelection();
 		params.unescapeCER = chkUnescapeCER.getSelection();
 		params.unescapeJava = chkUnescapeJava.getSelection();
 		params.escapeAll = rdEscapeAll.getSelection();
 		params.escapeNotation = getEscapeNotation();
 		String tmp = edUserFormat.getText();
 		//TODO: check format
 		params.userFormat= tmp;
 		params.useBytes = chkUseBytes.getSelection();
 		params.BOMonUTF8 = chkBOMonUTF8.getSelection();
 		params.reportUnsupported = chkReportUnsupported.getSelection();
 		result = true;
 		return result;
 	}
 	
 	int getEscapeNotation () {
 		if ( rdEscapeToNCRHexaL.getSelection() )
 			return Parameters.ESCAPE_NCRHEXAL;
 		if ( rdEscapeToCER.getSelection() )
 			return Parameters.ESCAPE_CER;
 		if ( rdEscapeToJavaL.getSelection() )
 			return Parameters.ESCAPE_JAVAL;
 		if ( rdEscapeToJavaU.getSelection() )
 			return Parameters.ESCAPE_JAVAU;
 		if ( rdEscapeToNCRDeci.getSelection() )
 			return Parameters.ESCAPE_NCRDECI;
 		if ( rdEscapeToUserFormat.getSelection() )
 			return Parameters.ESCAPE_USERFORMAT;
 		// Else and if ( rdEscapeToNCRHexaU.getSelection() )
 		return Parameters.ESCAPE_NCRHEXAU;
 	}
 	
 }
