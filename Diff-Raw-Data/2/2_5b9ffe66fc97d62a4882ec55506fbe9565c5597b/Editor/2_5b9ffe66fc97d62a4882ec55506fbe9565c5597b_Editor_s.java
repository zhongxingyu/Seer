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
 
 package net.sf.okapi.filters.regex.ui;
 
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 
 import net.sf.okapi.common.IContext;
 import net.sf.okapi.common.IHelp;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IParametersEditor;
 import net.sf.okapi.common.ui.Dialogs;
 import net.sf.okapi.common.ui.InputDialog;
 import net.sf.okapi.common.ui.OKCancelPanel;
 import net.sf.okapi.common.ui.UIUtil;
 import net.sf.okapi.common.ui.filters.InlineCodeFinderDialog;
 import net.sf.okapi.common.ui.filters.LDPanel;
 import net.sf.okapi.filters.regex.Parameters;
 import net.sf.okapi.filters.regex.Rule;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Text;
 
 public class Editor implements IParametersEditor {
 	
 	private Shell shell;
 	private boolean result = false;
 	private Text edExpression;
 	private Button chkExtractOuterStrings;
 	private Text edStartString;
 	private Text edEndString;
 	private List lbRules;
 	private Button btAdd;
 	private Button btEdit;
 	private Button btRename;
 	private Button btRemove;
 	private Button btMoveUp;
 	private Button btMoveDown;
 	private LDPanel pnlLD;
 	private Parameters params;
 	private ArrayList<Rule> rules;
 	private int ruleIndex = -1;
 	private Text edRuleType;
 	private Button chkPreserveWS;
 	private Button chkUseCodeFinder;
 	private Button btEditFinderRules;
 	private Button chkIgnoreCase;
 	private Button chkDotAll;
 	private Button chkMultiline;
 	private Text edMimeType;
 	private IHelp help;
 	private Button chkOneLevelGroups;
 	
 	public boolean edit (IParameters p_Options,
 		boolean readOnly,
 		IContext context)
 	{
 		boolean bRes = false;
 		shell = null;
 		help = (IHelp)context.getObject("help");
 		
 		params = (Parameters)p_Options;
 		// Make a work copy (in case of escape)
 		rules = new ArrayList<Rule>();
 		for ( Rule rule : params.getRules() ) {
 			rules.add(new Rule(rule));
 		}
 		
 		try {
 			shell = new Shell((Shell)context.getObject("shell"), SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
 			create((Shell)context.getObject("shell"), readOnly);
 			return showDialog();
 		}
 		catch ( Exception E ) {
 			Dialogs.showError(shell, E.getLocalizedMessage(), null);
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
 	
 	private void create (Shell p_Parent,
 		boolean readOnly)
 	{
 		shell.setText(Res.getString("Editor.caption")); //$NON-NLS-1$
 		if ( p_Parent != null ) shell.setImage(p_Parent.getImage());
 		GridLayout layTmp = new GridLayout();
 		layTmp.marginBottom = 0;
 		layTmp.verticalSpacing = 0;
 		shell.setLayout(layTmp);
 
 		TabFolder tfTmp = new TabFolder(shell, SWT.NONE);
 		GridData gdTmp = new GridData(GridData.FILL_BOTH);
 		tfTmp.setLayoutData(gdTmp);
 
 		//--- Rules tab
 		
 		Composite cmpTmp = new Composite(tfTmp, SWT.NONE);
 		layTmp = new GridLayout(2, false);
 		cmpTmp.setLayout(layTmp);
 		
 		lbRules = new List(cmpTmp, SWT.BORDER | SWT.H_SCROLL);
 		gdTmp = new GridData(GridData.FILL_BOTH);
 		gdTmp.horizontalSpan = 1;
 		gdTmp.grabExcessHorizontalSpace = true;
 		gdTmp.verticalSpan = 3;
 		lbRules.setLayoutData(gdTmp);
 		lbRules.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				updateRule();
 				updateMoveButtons();
 			};
 		});
 		lbRules.addMouseListener(new MouseListener() {
 			public void mouseDoubleClick(MouseEvent e) {
 				editRule(false);
 			}
 			public void mouseDown(MouseEvent e) {}
 			public void mouseUp(MouseEvent e) {}
 		});
 		
 		//--- Rule properties
 		
 		Group propGroup = new Group(cmpTmp, SWT.NONE);
 		layTmp = new GridLayout();
 		propGroup.setLayout(layTmp);
 		propGroup.setText(Res.getString("Editor.ruleProperties")); //$NON-NLS-1$
 		gdTmp = new GridData(GridData.FILL_BOTH);
 		gdTmp.verticalSpan = 3;
 		propGroup.setLayoutData(gdTmp);
 		
 		edExpression = new Text(propGroup, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
 		edExpression.setEditable(false);
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		gdTmp.heightHint = 50;
 		edExpression.setLayoutData(gdTmp);
 		
 		edRuleType = new Text(propGroup, SWT.BORDER);
 		edRuleType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		edRuleType.setEditable(false);
 		
 		chkPreserveWS = new Button(propGroup, SWT.CHECK);
 		chkPreserveWS.setText(Res.getString("Editor.preserveWS")); //$NON-NLS-1$
 		gdTmp = new GridData();
 		chkPreserveWS.setLayoutData(gdTmp);
 		
 		chkUseCodeFinder = new Button(propGroup, SWT.CHECK);
 		chkUseCodeFinder.setText(Res.getString("Editor.hasInlines")); //$NON-NLS-1$
 		gdTmp = new GridData();
 		chkUseCodeFinder.setLayoutData(gdTmp);
 		chkUseCodeFinder.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				updateEditFinderRulesButton();
 			};
 		});
 		
 		btEditFinderRules = new Button(propGroup, SWT.PUSH);
 		btEditFinderRules.setText(Res.getString("Editor.editInlines")); //$NON-NLS-1$
 		gdTmp = new GridData();
 		gdTmp.horizontalIndent = 16;
 		btEditFinderRules.setLayoutData(gdTmp);
 		btEditFinderRules.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				editFinderRules();
 			};
 		});
 
 		//--- Buttons
 		
 		Composite cmpButtons = new Composite(cmpTmp, SWT.NONE);
 		layTmp = new GridLayout(2, true);
 		layTmp.marginWidth = 0;
 		cmpButtons.setLayout(layTmp);
 		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
 		gdTmp.verticalSpan = 2;
 		cmpButtons.setLayoutData(gdTmp);
 		
 		int buttonWidth = 90;
 		
 		btAdd = new Button(cmpButtons, SWT.PUSH);
 		btAdd.setText(Res.getString("Editor.add")); //$NON-NLS-1$
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		btAdd.setLayoutData(gdTmp);
 		UIUtil.ensureWidth(btAdd, buttonWidth);
 		btAdd.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				editRule(true);
 			};
 		});
 		
 		btEdit = new Button(cmpButtons, SWT.PUSH);
 		btEdit.setText(Res.getString("Editor.edit")); //$NON-NLS-1$
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		btEdit.setLayoutData(gdTmp);
 		btEdit.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				editRule(false);
 			};
 		});
 		
 		btRename = new Button(cmpButtons, SWT.PUSH);
 		btRename.setText(Res.getString("Editor.rename")); //$NON-NLS-1$
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		btRename.setLayoutData(gdTmp);
 		btRename.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				renameRule();
 			};
 		});
 		
 		btMoveUp = new Button(cmpButtons, SWT.PUSH);
 		btMoveUp.setText(Res.getString("Editor.moveUp")); //$NON-NLS-1$
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		btMoveUp.setLayoutData(gdTmp);
 		btMoveUp.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				moveUpRule();
 			};
 		});
 		
 		btRemove = new Button(cmpButtons, SWT.PUSH);
 		btRemove.setText(Res.getString("Editor.remove")); //$NON-NLS-1$
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		gdTmp.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
 		btRemove.setLayoutData(gdTmp);
 		btRemove.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				removeRule();
 			};
 		});
 		
 		btMoveDown = new Button(cmpButtons, SWT.PUSH);
 		btMoveDown.setText(Res.getString("Editor.moveDown")); //$NON-NLS-1$
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		gdTmp.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
 		btMoveDown.setLayoutData(gdTmp);
 		btMoveDown.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				moveDownRule();
 			};
 		});
 
 		//--- Options
 		
 		chkOneLevelGroups = new Button(cmpTmp, SWT.CHECK);
		chkOneLevelGroups.setText("Auto-close previous group when a new one starts");
 		
 		Group optionsGroup = new Group(cmpTmp, SWT.NONE);
 		layTmp = new GridLayout(2, false);
 		optionsGroup.setLayout(layTmp);
 		optionsGroup.setText(Res.getString("Editor.regexOptions")); //$NON-NLS-1$
 		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
 		optionsGroup.setLayoutData(gdTmp);
 		
 		chkDotAll = new Button(optionsGroup, SWT.CHECK);
 		chkDotAll.setText(Res.getString("Editor.dotMatchesLF")); //$NON-NLS-1$
 
 		chkMultiline = new Button(optionsGroup, SWT.CHECK);
 		chkMultiline.setText(Res.getString("Editor.multiline")); //$NON-NLS-1$
 		
 		chkIgnoreCase = new Button(optionsGroup, SWT.CHECK);
 		chkIgnoreCase.setText(Res.getString("Editor.ignoreCases")); //$NON-NLS-1$
 		
 		//--- end Options
 
 		TabItem tiTmp = new TabItem(tfTmp, SWT.NONE);
 		tiTmp.setText(Res.getString("Editor.rules")); //$NON-NLS-1$
 		tiTmp.setControl(cmpTmp);
 		
 		
 		//--- Options tab
 		
 		cmpTmp = new Composite(tfTmp, SWT.NONE);
 		layTmp = new GridLayout();
 		cmpTmp.setLayout(layTmp);
 		
 		// Localization directives
 		Group grpTmp = new Group(cmpTmp, SWT.NONE);
 		layTmp = new GridLayout();
 		grpTmp.setLayout(layTmp);
 		grpTmp.setText(Res.getString("Editor.locDir")); //$NON-NLS-1$
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		grpTmp.setLayoutData(gdTmp);
 		pnlLD = new LDPanel(grpTmp, SWT.NONE);
 
 		// Strings
 		grpTmp = new Group(cmpTmp, SWT.NONE);
 		layTmp = new GridLayout(2, false);
 		grpTmp.setLayout(layTmp);
 		grpTmp.setText(Res.getString("Editor.strings")); //$NON-NLS-1$
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		grpTmp.setLayoutData(gdTmp);
 		
 		chkExtractOuterStrings = new Button(grpTmp, SWT.CHECK);
 		chkExtractOuterStrings.setText(Res.getString("Editor.extractStringsOutside")); //$NON-NLS-1$
 		gdTmp = new GridData();
 		gdTmp.horizontalSpan = 2;
 		chkExtractOuterStrings.setLayoutData(gdTmp);
 //TODO: implement chkExtractOuterStrings		
 chkExtractOuterStrings.setEnabled(false); // NOT WORKING YET		
 
 		Label label = new Label(grpTmp, SWT.NONE);
 		label.setText(Res.getString("Editor.startOfString")); //$NON-NLS-1$
 		edStartString = new Text(grpTmp, SWT.BORDER);
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		edStartString.setLayoutData(gdTmp);
 
 		label = new Label(grpTmp, SWT.NONE);
 		label.setText(Res.getString("Editor.endOfString")); //$NON-NLS-1$
 		edEndString = new Text(grpTmp, SWT.BORDER);
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		edEndString.setLayoutData(gdTmp);
 
 		// Content type
 		grpTmp = new Group(cmpTmp, SWT.NONE);
 		layTmp = new GridLayout(2, false);
 		grpTmp.setLayout(layTmp);
 		grpTmp.setText(Res.getString("Editor.contentType")); //$NON-NLS-1$
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		grpTmp.setLayoutData(gdTmp);
 
 		label = new Label(grpTmp, SWT.NONE);
 		label.setText(Res.getString("Editor.mimeType")); //$NON-NLS-1$
 		edMimeType = new Text(grpTmp, SWT.BORDER);
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		edMimeType.setLayoutData(gdTmp);
 		
 		tiTmp = new TabItem(tfTmp, SWT.NONE);
 		tiTmp.setText(Res.getString("Editor.options")); //$NON-NLS-1$
 		tiTmp.setControl(cmpTmp);
 		
 		//--- Dialog-level buttons
 
 		SelectionAdapter okCancelActions = new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if ( e.widget.getData().equals("h") ) { //$NON-NLS-1$
 					if ( help != null ) help.showTopic(this, "index");
 					return;
 				}
 				if ( e.widget.getData().equals("o") ) saveData(); //$NON-NLS-1$
 				shell.close();
 			};
 		};
 		OKCancelPanel pnlActions = new OKCancelPanel(shell, SWT.NONE, okCancelActions, true);
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		pnlActions.setLayoutData(gdTmp);
 		pnlActions.btOK.setEnabled(!readOnly);
 		if ( !readOnly ) {
 			shell.setDefaultButton(pnlActions.btOK);
 		}
 
 		shell.pack();
 		shell.setMinimumSize(shell.getSize());
 		Point startSize = shell.getMinimumSize();
 		if ( startSize.x < 600 ) startSize.x = 600; 
 		if ( startSize.y < 450 ) startSize.y = 450; 
 		shell.setSize(startSize);
 		Dialogs.centerWindow(shell, p_Parent);
 		setData();
 	}
 	
 	private boolean showDialog () {
 		shell.open();
 		while ( !shell.isDisposed() ) {
 			if ( !shell.getDisplay().readAndDispatch() )
 				shell.getDisplay().sleep();
 		}
 		return result;
 	}
 	
 	private void updateRule () {
 		saveRuleData(ruleIndex);
 		
 		int newRuleIndex = lbRules.getSelectionIndex();
 		boolean enabled = (newRuleIndex > -1 );
 		edRuleType.setEnabled(enabled);
 		chkPreserveWS.setEnabled(enabled);
 		chkUseCodeFinder.setEnabled(enabled);
 
 		ruleIndex = newRuleIndex;
 		if ( ruleIndex < 0 ) {
 			edExpression.setText(""); //$NON-NLS-1$
 			edRuleType.setText(""); //$NON-NLS-1$
 			chkPreserveWS.setSelection(false);
 			chkUseCodeFinder.setSelection(false);
 			btEditFinderRules.setEnabled(false);
 			return;
 		}
 		Rule rule = rules.get(ruleIndex);
 		edExpression.setText(rule.getExpression());
 		switch ( rule.getRuleType() ) {
 		case Rule.RULETYPE_STRING:
 			edRuleType.setText(Res.getString("Editor.extractStringsInside")); //$NON-NLS-1$
 			break;
 		case Rule.RULETYPE_CONTENT:
 			edRuleType.setText(Res.getString("Editor.extractContent")); //$NON-NLS-1$
 			break;
 		case Rule.RULETYPE_COMMENT:
 			edRuleType.setText(Res.getString("Editor.treatAsComment")); //$NON-NLS-1$
 			break;
 		case Rule.RULETYPE_NOTRANS:
 			edRuleType.setText(Res.getString("Editor.doNotExtract")); //$NON-NLS-1$
 			break;
 		case Rule.RULETYPE_OPENGROUP:
 			edRuleType.setText(Res.getString("Editor.startGroup")); //$NON-NLS-1$
 			break;
 		case Rule.RULETYPE_CLOSEGROUP:
 			edRuleType.setText(Res.getString("Editor.endGroup")); //$NON-NLS-1$
 			break;
 		default:
 			edRuleType.setText(""); //$NON-NLS-1$
 		}
 		
 		chkPreserveWS.setSelection(rule.preserveWS());
 		chkUseCodeFinder.setSelection(rule.useCodeFinder());
 		updateEditFinderRulesButton();
 	}
 
 	private void updateEditFinderRulesButton () {
 		btEditFinderRules.setEnabled(chkUseCodeFinder.getSelection());
 	}
 	
 	private void editFinderRules () {
 		try {
 			Rule rule = rules.get(ruleIndex);
 			InlineCodeFinderDialog dlg = 
 				new InlineCodeFinderDialog(shell, Res.getString("Editor.inlinesPatterns"), null); //$NON-NLS-1$
 			dlg.setData(rule.getCodeFinderRules());
 			String tmp = dlg.showDialog();
 			if ( tmp == null ) return;
 			rule.setCodeFinderRules(tmp);
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private void saveRuleData (int index) {
 		if ( index < 0 ) return;
 		Rule rule = rules.get(index);
 		rule.setPreserveWS(chkPreserveWS.getSelection());
 		rule.setUseCodeFinder(chkUseCodeFinder.getSelection());
 	}
 
 	private void updateMoveButtons () {
 		int n = lbRules.getSelectionIndex();
 		btMoveUp.setEnabled(n > 0);
 		btMoveDown.setEnabled((n != -1) && ( n < lbRules.getItemCount()-1 ));
 	}
 
 	private void updateRuleButtons () {
 		int n = lbRules.getSelectionIndex();
 		btRemove.setEnabled(n != -1);
 		btEdit.setEnabled(n != -1);
 		btRename.setEnabled(n != -1);
 		updateMoveButtons();
 	}
 	
 	private void renameRule () {
 		try {
 			int n = lbRules.getSelectionIndex();
 			if ( n == -1 ) return;
 			Rule rule = rules.get(n);
 			String name = rule.getRuleName();
 			InputDialog dlg = new InputDialog(shell, Res.getString("Editor.renameRule"), //$NON-NLS-1$
 				Res.getString("Editor.newRuleName"), name, null, 0, -1, -1); //$NON-NLS-1$
 			if ( (name = dlg.showDialog()) == null ) return;
 			rule.setRuleName(name);
 			lbRules.setItem(n, name);
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private void editRule (boolean newRule) {
 		try {
 			Rule rule;
 			if ( newRule ) {
 				// Get the name
 				String name = Res.getString("Editor.defaultRuleName"); //$NON-NLS-1$
 				InputDialog dlg = new InputDialog(shell, Res.getString("Editor.newRuleCaption"), //$NON-NLS-1$
 					Res.getString("Editor.newRuleLabel"), name, null, 0, -1, -1); //$NON-NLS-1$
 				if ( (name = dlg.showDialog()) == null ) return;
 				rule = new Rule();
 				rule.setRuleName(name);
 			}
 			else {
 				int n = lbRules.getSelectionIndex();
 				if ( n == -1 ) return;
 				rule = rules.get(n);
 			}
 			
 			RuleDialog dlg = new RuleDialog(shell, help, rule, getRegexOptions());
 			if ( !dlg.showDialog() ) return;
 			rule = dlg.getRule();
 			
 			if ( newRule ) {
 				rules.add(rule);
 				lbRules.add(rule.getRuleName());
 				lbRules.select(lbRules.getItemCount()-1);
 			}
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 		finally {
 			updateRule();
 			updateRuleButtons();
 		}
 	}
 	
 	private void removeRule () {
 		int n = lbRules.getSelectionIndex();
 		if ( n == -1 ) return;
 		ruleIndex = -1;
 		rules.remove(n);
 		lbRules.remove(n);
 		if ( n > lbRules.getItemCount()-1  ) n = lbRules.getItemCount()-1;
 		lbRules.select(n);
 		updateRule();
 		updateRuleButtons();
 	}
 	
 	private void moveUpRule () {
 		int n = lbRules.getSelectionIndex();
 		if ( n < 1 ) return;
 		saveRuleData(ruleIndex);
 		ruleIndex = -1;
 		// Move in the rules array
 		Rule tmp = rules.get(n);
 		rules.set(n, rules.get(n-1));
 		rules.set(n-1, tmp);
 		// Refresh the list box
 		lbRules.setItem(n, rules.get(n).getRuleName());
 		lbRules.setItem(n-1, rules.get(n-1).getRuleName());
 		lbRules.select(n-1);
 		updateRule();
 		updateRuleButtons();
 	}
 	
 	private void moveDownRule () {
 		int n = lbRules.getSelectionIndex();
 		if ( n < 0 ) return;
 		saveRuleData(ruleIndex);
 		ruleIndex = -1;
 		// Move in the rules array
 		Rule tmp = rules.get(n);
 		rules.set(n, rules.get(n+1));
 		rules.set(n+1, tmp);
 		// Refresh the list box
 		lbRules.setItem(n, rules.get(n).getRuleName());
 		lbRules.setItem(n+1, rules.get(n+1).getRuleName());
 		lbRules.select(n+1);
 		updateRule();
 		updateRuleButtons();
 	}
 	
 	private void setData () {
 		pnlLD.setOptions(params.locDir.useLD(), params.locDir.localizeOutside());
 		chkExtractOuterStrings.setSelection(params.extractOuterStrings);
 		edStartString.setText(params.startString);
 		edEndString.setText(params.endString);
 		edMimeType.setText(params.mimeType);
 		
 		for ( Rule rule : rules ) {
 			lbRules.add(rule.getRuleName());
 		}
 		chkOneLevelGroups.setSelection(params.oneLevelGroups);
 
 		int tmp = params.regexOptions;
 		chkDotAll.setSelection((tmp & Pattern.DOTALL)==Pattern.DOTALL);
 		chkIgnoreCase.setSelection((tmp & Pattern.CASE_INSENSITIVE)==Pattern.CASE_INSENSITIVE);
 		chkMultiline.setSelection((tmp & Pattern.MULTILINE)==Pattern.MULTILINE);
 		pnlLD.updateDisplay();
 		if ( lbRules.getItemCount() > 0 ) lbRules.select(0);
 		updateRule();
 		updateRuleButtons();
 	}
 	
 	private void saveData () {
 		saveRuleData(ruleIndex);
 		//TODO: validation
 		params.locDir.setOptions(pnlLD.getUseLD(), pnlLD.getLocalizeOutside());
 		params.extractOuterStrings = chkExtractOuterStrings.getSelection();
 		params.startString = edStartString.getText();
 		params.endString = edEndString.getText();
 		params.mimeType = edMimeType.getText();
 		
 		ArrayList<Rule> paramRules = params.getRules();
 		paramRules.clear();
 		for ( Rule rule : rules ) {
 			paramRules.add(rule);
 		}
 		params.oneLevelGroups = chkOneLevelGroups.getSelection();
 		params.regexOptions = getRegexOptions();
 		result = true;
 	}
 
 	private int getRegexOptions () {
 		int tmp = 0;
 		if ( chkDotAll.getSelection() ) tmp |= Pattern.DOTALL;
 		if ( chkIgnoreCase.getSelection() ) tmp |= Pattern.CASE_INSENSITIVE;
 		if ( chkMultiline.getSelection() ) tmp |= Pattern.MULTILINE;
 		return tmp;
 	}
 
 }
