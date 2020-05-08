 /*===========================================================================
   Copyright (C) 2010 by the Okapi Framework contributors
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
 
 package net.sf.okapi.applications.rainbow.lib;
 
 import net.sf.okapi.common.IHelp;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.ui.ClosePanel;
 import net.sf.okapi.common.ui.Dialogs;
 import net.sf.okapi.common.ui.UIUtil;
 import net.sf.okapi.common.ui.filters.InlineCodeFinderPanel;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.dnd.TransferData;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Shell;
 
 public class CodeFinderEditor {
 	
 	private Shell shell;
 	private IHelp help;
 	private Button btCopyToClipboard;
 	private Button chkXMLAware;
 	private Button chkYAMLAware;
 	private InlineCodeFinderPanel pnlCodeFinder;
 
 	public CodeFinderEditor (Shell p_Parent,
 		IHelp helpParam)
 	{
 		help = helpParam;
 		shell = new Shell(p_Parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
 		shell.setText("Code Finder Editor");
 		UIUtil.inheritIcon(shell, p_Parent);
 		shell.setLayout(new GridLayout(4, false));
 		
 		pnlCodeFinder = new InlineCodeFinderPanel(shell, SWT.NONE);
 		GridData gdTmp = new GridData(GridData.FILL_BOTH);
 		gdTmp.horizontalSpan = 4;
 		pnlCodeFinder.setLayoutData(gdTmp);
 		
 		chkXMLAware = new Button(shell, SWT.CHECK);
 		chkXMLAware.setText("XML aware");
 		chkXMLAware.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL));
 		
 		chkYAMLAware = new Button(shell, SWT.CHECK);
 		chkYAMLAware.setText("YAML aware");
 		chkYAMLAware.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
 		
 		final int width = 170; 
 		btCopyToClipboard = new Button(shell, SWT.PUSH);
 		btCopyToClipboard.setText("Paste From Clipboard");
 		gdTmp = new GridData(GridData.HORIZONTAL_ALIGN_END);
 		gdTmp.widthHint = width;
 		btCopyToClipboard.setLayoutData(gdTmp);
 		btCopyToClipboard.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				pasteFromClipboard();
 			};
 		});
 
 		btCopyToClipboard = new Button(shell, SWT.PUSH);
 		btCopyToClipboard.setText("Copy To Clipboard");
 		gdTmp = new GridData(GridData.HORIZONTAL_ALIGN_END);
 		gdTmp.widthHint = width;
 		btCopyToClipboard.setLayoutData(gdTmp);
 		btCopyToClipboard.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				copyToClipboard();
 			};
 		});
 
 		SelectionAdapter CloseActions = new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if ( e.widget.getData().equals("h") ) { //$NON-NLS-1$
 					if ( help != null ) help.showWiki("Rainbow - Code Finder Editor");
 					return;
 				}
 				shell.close();
 			};
 		};
 		ClosePanel pnlActions = new ClosePanel(shell, SWT.NONE, CloseActions, (help!=null));
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		gdTmp.horizontalSpan = 4;
 		pnlActions.setLayoutData(gdTmp);
 		shell.setDefaultButton(pnlActions.btClose);
 		
 		shell.pack();
 		Rectangle rect = shell.getBounds();
 		shell.setMinimumSize(rect.width, rect.height);
 		if ( rect.width < 700 ) rect.width = 700;
 		if ( rect.height < 450 ) rect.height = 450;
 		shell.setSize(rect.width, rect.height);
 		Dialogs.centerWindow(shell, p_Parent);
 
 		setInitialRules();
 	}
 	
 	public void showDialog () {
 		shell.open();
 		while ( !shell.isDisposed() ) {
 			if ( !shell.getDisplay().readAndDispatch() )
 				shell.getDisplay().sleep();
 		}
 	}
 	
 	private void setInitialRules () {
 		chkXMLAware.setSelection(true);
 		pnlCodeFinder.setRules("#v1\ncount.i=1\nrule0=(\\A[^<]*?>)|(<[\\w!?/].*?(>|\\Z))\n"
 			+ "sample=Text <b>bold</b> <a name=\"abc\">\nuseAllRulesWhenTesting.b=false");
 		pnlCodeFinder.updateDisplay();
 	}
 	
 	private void copyToClipboard () {
 		Clipboard clipboard = null;
 		try {
 			String text = pnlCodeFinder.getRules();
 			if ( text == null ) {
 				Dialogs.showError(shell, "Error in expression.\nNo data copied.", null);
 			}
 			// Else: proceed to copy
 			// Escapes to XML and/or YAML is requested
 			if ( chkXMLAware.getSelection() ) {
 				text = Util.escapeToXML(text, 1, false, null);
 			}
 			if ( chkYAMLAware.getSelection() ) {
 				text = escapeToYAML(text);
 			}
 			
 			// Copy them to the Clipboard
 			clipboard = new Clipboard(shell.getDisplay());
 			TextTransfer textTransfer = TextTransfer.getInstance();
 			clipboard.setContents(new String[]{text}, new Transfer[]{textTransfer});
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getLocalizedMessage(), null);
 		}
 		finally {
 			if ( clipboard != null ) clipboard.dispose();
 		}
 	}
 
 	private void pasteFromClipboard () {
 		Clipboard clipboard = null;
 		try {
 			// Get the data types available in the Clipboard
 			clipboard = new Clipboard(shell.getDisplay());
 		    TransferData[] transferDatas = clipboard.getAvailableTypes();
 		    boolean found = false;
 		    for(int i=0; i<transferDatas.length; i++) {
 		    	if ( TextTransfer.getInstance().isSupportedType(transferDatas[i]) ) {
 		    		found = true;
 		    		break;
 		    	}
 		    }
 		    // Do nothing if there is no simple text available
 		    if ( !found ) return;
 
 		    // Load the file from the text in the Clipboard
 		    String text = (String)clipboard.getContents(TextTransfer.getInstance());
 		    if ( chkXMLAware.getSelection() ) {
 		    	text = unescapeFromXML(text);
 		    }
 		    if ( chkYAMLAware.getSelection() ) {
 		    	text = unescapeFromYAML(text);
 		    }
 		    pnlCodeFinder.setRules(text);
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getLocalizedMessage(), null);
 		}
 		finally {
 			if ( clipboard != null ) clipboard.dispose();
 			pnlCodeFinder.updateDisplay();
 		}
 	}
 	
 	private String unescapeFromXML (String text) {
 		text = text.replace("&lt;", "<");
 		text = text.replace("&gt;", ">");
 		text = text.replace("&quot;", "\"");
 		text = text.replace("&apos;", "'");
 		// Do ampersand last to make sure we do not double escape
 		return text.replace("&amp;", "&");
 	}
 
 	private String escapeToYAML (String text) {
 		text = text.replace("\\", "\\\\");
 		return text.replace("\n", "\\n");
 	}
 	
 	private String unescapeFromYAML (String text) {
 		text = text.replaceAll("([^\\\\])\\\\n", "$1\n");
 		return text.replace("\\\\", "\\");
 	}
 	
 }
