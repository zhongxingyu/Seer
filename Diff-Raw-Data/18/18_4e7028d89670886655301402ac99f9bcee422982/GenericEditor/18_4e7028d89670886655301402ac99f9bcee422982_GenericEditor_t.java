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
 
 package net.sf.okapi.common.ui.genericeditor;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Hashtable;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import net.sf.okapi.common.IContext;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.ParametersDescription;
 import net.sf.okapi.common.uidescription.AbstractPart;
 import net.sf.okapi.common.uidescription.CheckboxPart;
 import net.sf.okapi.common.uidescription.EditorDescription;
 import net.sf.okapi.common.uidescription.IContainerPart;
 import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
 import net.sf.okapi.common.uidescription.ListSelectionPart;
 import net.sf.okapi.common.uidescription.PathInputPart;
 import net.sf.okapi.common.uidescription.TextInputPart;
 import net.sf.okapi.common.ui.Dialogs;
 import net.sf.okapi.common.ui.OKCancelPanel;
 import net.sf.okapi.common.ui.TextAndBrowsePanel;
 import net.sf.okapi.common.ui.UIUtil;
 import net.sf.okapi.common.exceptions.OkapiEditorCreationException;
 
 public class GenericEditor {
 
 	private Shell shell;
 	private boolean result = false;
 	private IParameters params;
 	private EditorDescription description;
 	private Hashtable<String, Control> controls;
 	private Composite mainCmp;
 
 	public boolean edit (IParameters paramsObject,
 		IEditorDescriptionProvider descProvider,
 		boolean readOnly,
 		IContext context)
 	{
 		boolean bRes = false;
 		shell = null;
 		try {
 			params = paramsObject; 
 			shell = new Shell((Shell)context.getObject("shell"), SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
 			create((Shell)context.getObject("shell"), descProvider, readOnly);
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
 	
 	private void create (Shell parent,
 		IEditorDescriptionProvider descProv,
 		boolean readOnly)
 	{
 		if ( parent != null ) UIUtil.inheritIcon(shell, parent);
 		GridLayout layTmp = new GridLayout(1, false);
 		layTmp.marginBottom = 0;
 		layTmp.verticalSpacing = 0;
 		shell.setLayout(layTmp);
 
 		// Get the UI description
 		ParametersDescription pd = params.getParametersDescription();
 		if ( pd == null ) {
 			throw new OkapiEditorCreationException(
 				"This configuration cannot be edited with the generic editor because it does not provide a description of its parameters.");
 		}
 		description = descProv.createEditorDescription(pd);
 		if ( description == null ) {
 			throw new OkapiEditorCreationException(
 				"This configuration cannot be edited with the generic editor because the UI description could not be created.");
 		}
 		controls = new Hashtable<String, Control>();
 		
 		// Set caption is it is provided
 		if ( description.getCaption() != null ) {
 			shell.setText(description.getCaption());
 		}
 		else { // Default caption
 			shell.setText("Configuration Editor");
 		}
 		
 		// Set a frame if there is no tab(s)
 		boolean hasTab = false;
 //		for ( IUIDescriptor desc : description.getDescriptors().values() ) {
 //			if ( desc instanceof TabDescriptor ) {
 //				hasTab = true;
 //				break;
 //			}
 //		}
 		if ( hasTab ) {
 			mainCmp = shell;
 		}
 		else {
 			mainCmp = new Composite(shell, SWT.BORDER);
 			mainCmp.setLayoutData(new GridData(GridData.FILL_BOTH));
 			layTmp = new GridLayout(2, false);
 			mainCmp.setLayout(layTmp);
 		}
 
 		// Create the UI parts
 		boolean hasPathInput = false;
 		Composite cmp;
 		int horizAlignFlag = 0;
 		if ( description.alignLabels() ) {
 			horizAlignFlag = GridData.HORIZONTAL_ALIGN_END;
 		}
 		
 		for ( AbstractPart part : description.getDescriptors().values() ) {
 			if ( part instanceof TextInputPart ) {
 				TextInputPart d = (TextInputPart)part;
 				cmp = lookupParent(d.getContainer());
 				setLabel(cmp, d, horizAlignFlag);
 				Text text = new Text(cmp, SWT.BORDER);
 				controls.put(d.getName(), text);
 				text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 				text.setEditable(d.getWriteMethod()!=null);
 				if ( d.isPassword() ) text.setEchoChar('*');
 			}
 			else if ( part instanceof CheckboxPart ) {
 				CheckboxPart d = (CheckboxPart)part;
 				cmp = lookupParent(d.getContainer());
 				new Label(cmp, SWT.NONE);
 				Button button = new Button(cmp, SWT.CHECK);
 				button.setToolTipText(d.getShortDescription());
 				controls.put(d.getName(), button);
 				button.setText(d.getDisplayName());
 				button.setEnabled(d.getWriteMethod()!=null);
 			}
 			else if ( part instanceof PathInputPart ) {
 				PathInputPart d = (PathInputPart)part;
 				cmp = lookupParent(d.getContainer());
 				setLabel(cmp, d, horizAlignFlag);
 				TextAndBrowsePanel ctrl = new TextAndBrowsePanel(cmp, SWT.NONE, false);
 				ctrl.setSaveAs(d.isForSaveAs());
 				ctrl.setTitle(d.getBrowseTitle());
 				ctrl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 				controls.put(d.getName(), ctrl);
 				ctrl.setEditable(d.getWriteMethod()!=null);
 				hasPathInput = true;
 			}
 			else if ( part instanceof ListSelectionPart ) {
 				ListSelectionPart d = (ListSelectionPart)part;
 				cmp = lookupParent(d.getContainer());
 				if ( d.isDropDown() ) {
 					setLabel(cmp, d, horizAlignFlag);
 					Combo combo = new Combo(cmp, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
 					controls.put(d.getName(), combo);
 					combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 					combo.setEnabled(d.getWriteMethod()!=null);
 				}
 				else {
 					setLabel(cmp, d, horizAlignFlag|GridData.VERTICAL_ALIGN_BEGINNING);
 					List list = new List(cmp, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
 					controls.put(d.getName(), list);
 					list.setLayoutData(new GridData(GridData.FILL_BOTH));
 					list.setEnabled(d.getWriteMethod()!=null);
 				}
 			}
 		}
 		
 		//--- Dialog-level buttons
 
 		SelectionAdapter okCancelActions = new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if ( e.widget.getData().equals("h") ) { //$NON-NLS-1$
 					//if ( help != null ) help.showTopic(this, "index");
 					return;
 				}
 				if ( e.widget.getData().equals("o") ) { //$NON-NLS-1$
 					if ( !saveData() ) return;
 					result = true;
 				}
 				shell.close();
 			};
 		};
 		OKCancelPanel pnlActions = new OKCancelPanel(shell, SWT.NONE, okCancelActions, true);
 		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		//gdTmp.horizontalSpan = 2;
 		pnlActions.setLayoutData(gdTmp);
 		pnlActions.btOK.setEnabled(!readOnly);
 		if ( !readOnly ) {
 			shell.setDefaultButton(pnlActions.btOK);
 		}
 
 		shell.pack();
 		shell.setMinimumSize(shell.getSize());
 		if ( hasPathInput ) {
 			Point startSize = shell.getMinimumSize();
 			if ( startSize.x < 600 ) startSize.x = 600; 
 //			if ( startSize.y < 450 ) startSize.y = 450; 
 			shell.setSize(startSize);
 		}
 		Dialogs.centerWindow(shell, parent);
 		setData();
 	}
 
 	private void setLabel (Composite parent,
 		AbstractPart part,
 		int flag)
 	{
 		Label label = new Label(parent, SWT.NONE);
 		label.setText(part.getDisplayName());
 		label.setToolTipText(part.getShortDescription());
 		label.setLayoutData(new GridData(flag));
 	}
 	
 	private void setData () {
 		for ( AbstractPart part : description.getDescriptors().values() ) {
 			if ( part instanceof TextInputPart ) {
 				TextInputPart d = (TextInputPart)part;
 				setInputControl((Text)controls.get(d.getName()), d);
 			}
 			else if ( part instanceof CheckboxPart ) {
 				CheckboxPart d = (CheckboxPart)part;
 				setCheckboxControl((Button)controls.get(d.getName()), d);
 			}
 			else if ( part instanceof PathInputPart ) {
 				PathInputPart d = (PathInputPart)part;
 				setPathControl((TextAndBrowsePanel)controls.get(d.getName()), d);
 			}
 			else if ( part instanceof ListSelectionPart ) {
 				ListSelectionPart d = (ListSelectionPart)part;
 				if ( d.isDropDown() ) {
 					setComboControl((Combo)controls.get(d.getName()), d);
 				}
 				else {
 					setListControl((List)controls.get(d.getName()), d);
 				}
 			}
 		}
 	}
 	
 	private boolean saveData () {
 		Control ctrl;
 		for ( String name : controls.keySet() ) {
 			ctrl = controls.get(name);
 			if ( ctrl instanceof Text ) {
 				if ( !saveInputControl((Text)ctrl, (TextInputPart)description.getDescriptor(name)) ) {
 					return false;
 				}
 			}
 			else if ( ctrl instanceof Button ) {
 				if (( ctrl.getStyle() & SWT.CHECK) == SWT.CHECK ) {
 					if ( !saveCheckboxControl((Button)ctrl, (CheckboxPart)description.getDescriptor(name)) ) {
 						return false;
 					}
 				}
 			}
 			else if ( ctrl instanceof TextAndBrowsePanel ) {
 				if ( description.getDescriptor(name) instanceof PathInputPart ) {
 					if ( !saveTextAndBrowseControl((TextAndBrowsePanel)ctrl, (PathInputPart)description.getDescriptor(name)) ) {
 						return false;
 					}
 				}
 			}
 			else if ( ctrl instanceof List ) {
 				if ( description.getDescriptor(name) instanceof ListSelectionPart ) {
					if ( !saveListControl((List)ctrl, (ListSelectionPart)description.getDescriptor(name)) ) {
						return false;
 					}
				}
			}
			else if ( ctrl instanceof Combo ) {
				if ( description.getDescriptor(name) instanceof ListSelectionPart ) {
					if ( !saveComboControl((Combo)ctrl, (ListSelectionPart)description.getDescriptor(name)) ) {
						return false;
 					}
 				}
 			}
 		}
 		return true;
 	}
 	
 	private boolean saveInputControl (Text text, TextInputPart desc) {
 		try {
 			if ( !desc.isAllowEmpty() ) {
 				if ( text.getText().length() == 0 ) {
 					Dialogs.showError(shell, "Empty entry not allowed.", null);
 					text.setFocus();
 					return false;
 				}
 			}
 			if ( desc.getType().equals(String.class) ) {
 				desc.getWriteMethod().invoke(desc.getParent(), text.getText());
 			}
 			else if ( desc.getType().equals(int.class) ) {
 				try {
 					int n = 0;
 					if ( text.getText().length() > 0 ) { 
 						n = Integer.valueOf(text.getText());
 					}
 					desc.getWriteMethod().invoke(desc.getParent(), n);
 				}
 				catch ( NumberFormatException e ) {
 					Dialogs.showError(shell, "Invalid integer value. "+e.getMessage(), null);
 					text.setFocus();
 					text.selectAll();
 					return false;
 				}
 			}
 			else {
 				throw new RuntimeException(String.format(
 					"Invalid type for the parameter '%s'.", desc.getName()));
 			}
 		}
 		catch ( IllegalArgumentException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 			return false;
 		}
 		catch ( IllegalAccessException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 			return false;
 		}
 		catch ( InvocationTargetException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 			return false;
 		}
 		return true;
 	}
 	
 	private boolean saveCheckboxControl (Button button, CheckboxPart desc) {
 		try {
 			if ( desc.getType().equals(boolean.class) ) {
 				desc.getWriteMethod().invoke(desc.getParent(), button.getSelection());
 			}
 			else if ( desc.getType().equals(String.class) ) {
 				desc.getWriteMethod().invoke(desc.getParent(), (button.getSelection() ? "1" : "0"));
 			}
 			else if ( desc.getType().equals(int.class) ) {
 				desc.getWriteMethod().invoke(desc.getParent(), (button.getSelection() ? 1 : 0));
 			}
 			else {
 				throw new RuntimeException(String.format(
 					"Invalid type for the parameter '%s'.", desc.getName()));
 			}
 		}
 		catch ( IllegalArgumentException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 			return false;
 		}
 		catch ( IllegalAccessException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 			return false;
 		}
 		catch ( InvocationTargetException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 			return false;
 		}
 		return true;
 	}
 	
 	private boolean saveTextAndBrowseControl (TextAndBrowsePanel ctrl, PathInputPart desc) {
 		try {
 			if ( desc.getType().equals(String.class) ) {
 				if ( ctrl.getText().length() == 0 ) {
 					Dialogs.showError(shell, "You must specify a path.", null);
 					ctrl.setFocus();
 					return false;
 				}
 				desc.getWriteMethod().invoke(desc.getParent(), ctrl.getText());
 			}
 			else {
 				throw new RuntimeException(String.format(
 					"Invalid type for the parameter '%s'.", desc.getName()));
 			}
 		}
 		catch ( IllegalArgumentException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 			return false;
 		}
 		catch ( IllegalAccessException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 			return false;
 		}
 		catch ( InvocationTargetException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 			return false;
 		}
 		return true;
 	}
 	
 	
 	private boolean saveListControl (List list, ListSelectionPart desc) {
 		try {
 			int n = list.getSelectionIndex();
 			if ( n > -1 ) {
 				desc.getWriteMethod().invoke(desc.getParent(), list.getItem(n));
 			}
 		}
 		catch ( IllegalArgumentException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 			return false;
 		}
 		catch ( IllegalAccessException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 			return false;
 		}
 		catch ( InvocationTargetException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 			return false;
 		}
 		return true;
 	}
 
 	private boolean saveComboControl (Combo combo, ListSelectionPart desc) {
 		try {
 			int n = combo.getSelectionIndex();
 			if ( n > -1 ) {
 				desc.getWriteMethod().invoke(desc.getParent(), combo.getItem(n));
 			}
 		}
 		catch ( IllegalArgumentException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 			return false;
 		}
 		catch ( IllegalAccessException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 			return false;
 		}
 		catch ( InvocationTargetException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 			return false;
 		}
 		return true;
 	}
 	
 	private void setInputControl (Text text, TextInputPart desc) {
 		try {
 			String tmp = "";
 			if ( desc.getType().equals(String.class) ) {
 				tmp = (String)desc.getReadMethod().invoke(desc.getParent());
 			}
 			else if ( desc.getType().equals(int.class) ) {
 				tmp = ((Integer)desc.getReadMethod().invoke(desc.getParent())).toString();
 			}
 			else {
 				throw new RuntimeException(String.format(
 					"Invalid type for the parameter '%s'.", desc.getName()));
 			}
 			if ( tmp == null ) text.setText("");
 			else text.setText(tmp);
 		}
 		catch ( IllegalArgumentException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 		catch ( IllegalAccessException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 		catch ( InvocationTargetException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private void setListControl (List list, ListSelectionPart desc) {
 		try {
 			if ( desc.getType().equals(String.class) ) {
 				String current = (String)desc.getReadMethod().invoke(desc.getParent());
 				if ( current == null ) current = "";
 				int found = -1;
 				int n = 0;
 				for ( String item : desc.getChoices() ) {
 					list.add(item);
 					if ( item.equals(current) ) found = n;
 					n++;
 				}
 				if ( found > -1 ) {
 					list.select(found);
 				}
 			}
 			else {
 				throw new RuntimeException(String.format(
 					"Invalid type for the parameter '%s'.", desc.getName()));
 			}
 		}
 		catch ( IllegalArgumentException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 		catch ( IllegalAccessException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 		catch ( InvocationTargetException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private void setComboControl (Combo combo, ListSelectionPart desc) {
 		try {
 			if ( desc.getType().equals(String.class) ) {
 				String current = (String)desc.getReadMethod().invoke(desc.getParent());
 				if ( current == null ) current = "";
 				int found = -1;
 				int n = 0;
 				for ( String item : desc.getChoices() ) {
 					combo.add(item);
 					if ( item.equals(current) ) found = n;
 					n++;
 				}
 				if ( found > -1 ) {
 					combo.select(found);
 				}
 			}
 			else {
 				throw new RuntimeException(String.format(
 					"Invalid type for the parameter '%s'.", desc.getName()));
 			}
 		}
 		catch ( IllegalArgumentException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 		catch ( IllegalAccessException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 		catch ( InvocationTargetException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private void setCheckboxControl (Button button, CheckboxPart desc) {
 		try {
 			if ( desc.getType().equals(boolean.class) ) {
 				button.setSelection((Boolean)desc.getReadMethod().invoke(desc.getParent()));
 			}
 			else if ( desc.getType().equals(int.class) ) {
 				int n = (Integer)desc.getReadMethod().invoke(desc.getParent());
 				button.setSelection(n!=0);
 			}
 			else if ( desc.getType().equals(String.class) ) {
 				String tmp = (String)desc.getReadMethod().invoke(desc.getParent());
 				button.setSelection((tmp!=null) && !tmp.equals("0"));
 			}
 			else {
 				throw new RuntimeException(String.format(
 					"Invalid type for the parameter '%s'.", desc.getName()));
 			}
 		}
 		catch ( IllegalArgumentException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 		catch ( IllegalAccessException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 		catch ( InvocationTargetException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private void setPathControl (TextAndBrowsePanel ctrl, PathInputPart desc) {
 		try {
 			if ( desc.getType().equals(String.class) ) {
 				String tmp = (String)desc.getReadMethod().invoke(desc.getParent());
 				ctrl.setText((tmp==null) ? "" : tmp);
 			}
 			else {
 				throw new RuntimeException(String.format(
 					"Invalid type for the parameter '%s'.", desc.getName()));
 			}
 		}
 		catch ( IllegalArgumentException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 		catch ( IllegalAccessException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 		catch ( InvocationTargetException e ) {
 			Dialogs.showError(shell, e.getMessage(), null);
 		}
 	}
 	
 	private Composite lookupParent (IContainerPart desc) {
 		if ( desc == null ) return mainCmp;
 		//TODO
 		return mainCmp;
 	}
 	
 	private boolean showDialog () {
 		shell.open();
 		while ( !shell.isDisposed() ) {
 			if ( !shell.getDisplay().readAndDispatch() )
 				shell.getDisplay().sleep();
 		}
 		return result;
 	}
 
 }
