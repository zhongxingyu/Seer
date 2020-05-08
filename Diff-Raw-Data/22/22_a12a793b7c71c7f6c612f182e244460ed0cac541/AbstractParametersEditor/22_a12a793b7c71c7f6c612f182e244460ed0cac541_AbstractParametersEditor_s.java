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
 
 package net.sf.okapi.ui.common.editors;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.okapi.common.IContext;
 import net.sf.okapi.common.IHelp;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IParametersEditor;
 import net.sf.okapi.common.ui.Dialogs;
 import net.sf.okapi.common.ui.OKCancelPanel;
 import net.sf.okapi.common.framework.CompoundParameters;
 import net.sf.okapi.common.framework.INotifiable;
 import net.sf.okapi.ui.common.dialogs.AbstractBaseDialog;
 import net.sf.okapi.ui.common.dialogs.IDialogPage;
 import net.sf.okapi.ui.common.utils.SWTUtils;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Widget;
 
 /**
  * Abstract base class for parameters editors 
  * 
  * @version 0.1, 12.06.2009
  */
 
 public abstract class AbstractParametersEditor implements IParametersEditor, Listener, INotifiable {
 
 	private Shell shell;
 //	private Shell parent;
 	private boolean result = true;
 	private OKCancelPanel pnlActions;
 	private IParameters params;
 	private IHelp help;
 	private TabFolder pageContainer;
 	private List<IDialogPage> pages = null;
 	boolean readOnly = false;
 	
 	public boolean edit(IParameters paramsObject, boolean readOnly, IContext context) {
 	
 		result = true;
 		if (context == null) return false;
 		//if (paramsObject == null) return false;
 		
 		this.readOnly = readOnly;
 		
 		Shell parent = (Shell)context.getObject("shell"); 
 
 		try {			
 			if (pages == null) 
 				pages = new ArrayList<IDialogPage>();
 			else
 				pages.clear();
 							
 			help = (IHelp)context.getObject("help");
 	
 			shell = null;
 			params = paramsObject;
 													
 			shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE /*| SWT.MAX */ | SWT.APPLICATION_MODAL);
 //			shell.addListener(SWT.Traverse, new Listener() {
 //				public void handleEvent(Event event) {
 //					if (event.detail == SWT.TRAVERSE_ESCAPE) {
 //						event.doit = true;
 //						result = false;
 //					}
 //				}});
 			create(parent);			
 			if (!result) return  false;
 			
 			showDialog();			
 			if (!result) return  false;			
 		}
 //		catch ( Exception E ) {
 //			Dialogs.showError(parent, E.getLocalizedMessage(), null);
 //			result = false;
 //		}
 		finally {
 			// Dispose of the shell, but not of the display
 			if (shell != null) shell.dispose();
 		}
 		return result;
 	}
 
 	private void create (Shell p_Parent) {
 		
 		shell.setText(getCaption());
 		shell.setData("owner", this);
 		
 		if ( p_Parent != null ) shell.setImage(p_Parent.getImage());
 
 ////--------------------------		
 //		
 //		TabFolder tfTmp = new TabFolder(shell, SWT.NONE);
 //		GridData gdTmp = new GridData(GridData.FILL_BOTH);
 //		tfTmp.setLayoutData(gdTmp);
 //		
 //		Composite cmpTmp = new Composite(tfTmp, SWT.NONE);
 //		GridLayout layTmp = new GridLayout();
 //		cmpTmp.setLayout(layTmp);
 //		
 ////		Button chkUseCodeFinder = new Button(cmpTmp, SWT.CHECK);
 ////		chkUseCodeFinder.setText("Has inline codes as defined below:");
 ////		chkUseCodeFinder.addSelectionListener(new SelectionAdapter() {
 ////			public void widgetSelected(SelectionEvent e) {
 ////				//updateInlineCodes();
 ////			};
 ////		});
 //		
 //		InlineCodeFinderPanel pnlCodeFinder = new InlineCodeFinderPanel(cmpTmp, SWT.NONE);
 //		pnlCodeFinder.setLayoutData(new GridData(GridData.FILL_BOTH));
 //		
 //		TabItem tiTmp = new TabItem(tfTmp, SWT.NONE);
 //		tiTmp.setText("Inline Codes");
 //		tiTmp.setControl(cmpTmp);
 //
 //		
 ////--------------------------		
 		GridLayout layTmp = new GridLayout();		
 		layTmp.marginBottom = 0;
 		layTmp.verticalSpacing = 0;
 		shell.setLayout(layTmp);
 
 		pageContainer = new TabFolder(shell, SWT.NONE);
 		//pageContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		GridData gdTmp = new GridData(GridData.FILL_BOTH);
 		pageContainer.setLayoutData(gdTmp);
 		
 		createPages(pageContainer);
 		if (!result) return;
 		
 		//loadParameters();
 		// result = loadParameters();
 //		if (!result) return;
 		
 		//--- Dialog-level buttons
 
 		SelectionAdapter OKCancelActions = new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				
 				if ( e.widget.getData().equals("h") ) {  // Help
 					if ( help != null ) help.showTopic(this, "index");
 					return;
 				} 
 				else if ( e.widget.getData().equals("o") ) { // OK
 					
 					if (!checkCanClose(true)) return;
 					result = saveParameters();
 				}
 				else {  // Cancel
 					result = false;
 					if (!checkCanClose(false)) return;
 				}
 				
 				shell.close();
 			};
 		};
 		pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		pnlActions.setLayoutData(gdTmp);
 		shell.setDefaultButton(pnlActions.btOK);
 						
 		shell.pack();
 		Rectangle minRect = shell.getBounds();
 		Rectangle startRect = shell.getBounds();
 				
 		if ( minRect.width > 500 ) minRect.width = 500;
 		if ( minRect.height > 400 ) minRect.height = 400;
 		
 		shell.setMinimumSize(minRect.width, minRect.height);
 		shell.setSize(startRect.width, startRect.height);		
 		
 		Dialogs.centerWindow(shell, p_Parent);
 		
 		loadParameters();  //!!! Here to have the dialog be minimal size 
 	}
 
 //	private void create (Shell p_Parent)
 //		{
 //			shell.setText("EditorCaption");
 //			if ( p_Parent != null ) shell.setImage(p_Parent.getImage());
 //			GridLayout layTmp = new GridLayout();
 //			layTmp.marginBottom = 0;
 //			layTmp.verticalSpacing = 0;
 //			shell.setLayout(layTmp);
 //
 //			TabFolder tfTmp = new TabFolder(shell, SWT.NONE);
 //			GridData gdTmp = new GridData(GridData.FILL_BOTH);
 //			tfTmp.setLayoutData(gdTmp);
 //
 //			//--- Options tab
 //			
 //			Composite cmpTmp = new Composite(tfTmp, SWT.NONE);
 //			layTmp = new GridLayout();
 //			cmpTmp.setLayout(layTmp);
 //			
 //			Group grpTmp = new Group(cmpTmp, SWT.NONE);
 //			layTmp = new GridLayout();
 //			grpTmp.setLayout(layTmp);
 //			grpTmp.setText("LodDirTitle");
 //			gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 //			grpTmp.setLayoutData(gdTmp);
 ////			pnlLD = new LDPanel(grpTmp, SWT.NONE);
 //			
 //			grpTmp = new Group(cmpTmp, SWT.NONE);
 //			layTmp = new GridLayout();
 //			grpTmp.setLayout(layTmp);
 //			grpTmp.setText("KeyCondTitle");
 //			gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 //			grpTmp.setLayoutData(gdTmp);
 //			
 //			Button chkUseKeyFilter = new Button(grpTmp, SWT.CHECK);
 //			chkUseKeyFilter.setText("chkUseKeyFilter");
 //			chkUseKeyFilter.addSelectionListener(new SelectionAdapter() {
 ////				public void widgetSelected(SelectionEvent e) {
 ////					updateKeyFilter();
 ////				};
 //			});
 //
 //			Button rdExtractOnlyMatchingKey = new Button(grpTmp, SWT.RADIO);
 //			rdExtractOnlyMatchingKey.setText("rdExtractOnlyMatchingKey");
 //			gdTmp = new GridData();
 //			gdTmp.horizontalIndent = 16;
 //			rdExtractOnlyMatchingKey.setLayoutData(gdTmp);
 //
 //			Button rdExcludeMatchingKey = new Button(grpTmp, SWT.RADIO);
 //			rdExcludeMatchingKey.setText("rdExcludeMatchingKey");
 //			rdExcludeMatchingKey.setLayoutData(gdTmp);
 //
 //			Text edKeyCondition = new Text(grpTmp, SWT.BORDER);
 //			gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 //			gdTmp.horizontalIndent = 16;
 //			edKeyCondition.setLayoutData(gdTmp);
 //			
 //			Label label = new Label(grpTmp, SWT.WRAP);
 //			label.setText("KeyCondNote");
 //			gdTmp = new GridData(GridData.FILL_BOTH);
 //			gdTmp.horizontalIndent = 16;
 //			gdTmp.widthHint = 300;
 //			label.setLayoutData(gdTmp);
 //			
 //			Button chkExtraComments = new Button(cmpTmp, SWT.CHECK);
 //			chkExtraComments.setText("chkExtraComments");
 //
 //			Button chkCommentsAreNotes = new Button(cmpTmp, SWT.CHECK);
 //			chkCommentsAreNotes.setText("chkCommentsAreNotes");
 //			
 //			TabItem tiTmp = new TabItem(tfTmp, SWT.NONE);
 //			tiTmp.setText("tabOptions");
 //			tiTmp.setControl(cmpTmp);
 //			
 //			//--- Inline tab
 //			
 //			cmpTmp = new Composite(tfTmp, SWT.NONE);
 //			layTmp = new GridLayout();
 //			cmpTmp.setLayout(layTmp);
 //			
 //			Button chkUseCodeFinder = new Button(cmpTmp, SWT.CHECK);
 //			chkUseCodeFinder.setText("Has inline codes as defined below:");
 //			chkUseCodeFinder.addSelectionListener(new SelectionAdapter() {
 //				public void widgetSelected(SelectionEvent e) {
 ////					updateInlineCodes();
 //				};
 //			});
 //			
 //			InlineCodeFinderPanel pnlCodeFinder = new InlineCodeFinderPanel(cmpTmp, SWT.NONE);
 //			pnlCodeFinder.setLayoutData(new GridData(GridData.FILL_BOTH));
 //			
 //			tiTmp = new TabItem(tfTmp, SWT.NONE);
 //			tiTmp.setText("Inline Codes");
 //			tiTmp.setControl(cmpTmp);
 //
 //			
 //			InlineCodeFinder codeFinder = new InlineCodeFinder();
 //			// Default in-line codes: special escaped-chars and printf-style variable
 //			codeFinder.reset();
 //			
 //			// Default in-line codes: special escaped-chars and printf-style variable
 //			codeFinder.addRule("%(([-0+#]?)[-0+#]?)((\\d\\$)?)(([\\d\\*]*)(\\.[\\d\\*]*)?)[dioxXucsfeEgGpn]");
 //			codeFinder.addRule("(\\\\r\\\\n)|\\\\a|\\\\b|\\\\f|\\\\n|\\\\r|\\\\t|\\\\v");
 //
 //			pnlCodeFinder.setData(codeFinder.toString());
 //			
 ////			pnlCodeFinder.setData(
 ////			"#v1\n" +
 ////			"count.i=3\n" +
 ////			"rule0=%(([-0+#]?)[-0+#]?)((\\d\\$)?)(([\\d\\*]*)(\\.[\\d\\*]*)?)[dioxXucsfeEgGpn]\n" +
 ////			"rule1=(\\r\\n)|\\a|\\b|\\f|\\n|\\r|\\t|\\v\n" +
 ////			"rule2=\\{\\d.*?\\}\n" +
 ////			"sample=%s, %d, {1}, \\n, \\r, \\t, etc.\n" +
 ////			"useAllRulesWhenTesting.b=true");
 //			
 //			//updateInlineCodes();
 //			pnlCodeFinder.updateDisplay();
 //
 //			//--- Output tab
 //			
 //			cmpTmp = new Composite(tfTmp, SWT.NONE);
 //			layTmp = new GridLayout();
 //			cmpTmp.setLayout(layTmp);
 //			
 //			grpTmp = new Group(cmpTmp, SWT.NONE);
 //			layTmp = new GridLayout();
 //			grpTmp.setLayout(layTmp);
 //			grpTmp.setText("grpExtendedChars");
 //			gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 //			grpTmp.setLayoutData(gdTmp);
 //
 //			Button chkEscapeExtendedChars = new Button(grpTmp, SWT.CHECK);
 //			chkEscapeExtendedChars.setText("chkEscapeExtendedChars");
 //			
 //			tiTmp = new TabItem(tfTmp, SWT.NONE);
 //			tiTmp.setText("tabOutput");
 //			tiTmp.setControl(cmpTmp);
 //			
 //			
 //			//--- Dialog-level buttons
 //
 //			SelectionAdapter OKCancelActions = new SelectionAdapter() {
 //				public void widgetSelected(SelectionEvent e) {
 //					result = false;
 //					if ( e.widget.getData().equals("h") ) {
 //						if ( help != null ) help.showTopic(this, "index");
 //						return;
 //					}
 //					if ( e.widget.getData().equals("o") ) {
 //						//if ( !saveData() ) return;
 //						result = true;
 //					}
 //					shell.close();
 //				};
 //			};
 //			pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
 //			gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 //			pnlActions.setLayoutData(gdTmp);
 //			pnlActions.btOK.setEnabled(!readOnly);
 //			if ( !readOnly ) {
 //				shell.setDefaultButton(pnlActions.btOK);
 //			}
 //
 //			shell.pack();
 //			Rectangle Rect = shell.getBounds();
 //			shell.setMinimumSize(Rect.width, Rect.height);
 //			Dialogs.centerWindow(shell, p_Parent);
 ////			setData();
 //		}
 
 	
 	private void showDialog () {
 		
 		if (!result) return;
 		
 		for (IDialogPage page : pages) {			
 			
 			if (page == null) continue;
 			
 			page.interop(null);
 		}
 		
 		interop(null);
 		
 		result = false; // To react to OK only
 		shell.open();
 		while ( !shell.isDisposed() ) {
 			
 			try {
 				if ( !shell.getDisplay().readAndDispatch() )
 					shell.getDisplay().sleep();
 			}
 			catch ( Exception E ) {
 				Dialogs.showError(shell, E.getLocalizedMessage(), null);
 			}
 		}				
 	}
 
 	private boolean loadParameters() {
 		
 		// Iterate through parameters of sub-filters and pages to load default values into the pages
 		
 		if (params instanceof CompoundParameters) {
 			
 			List<IParameters> list = ((CompoundParameters) params).getParameters();
 			
 			for (IParameters parameters : list)
 				for (IDialogPage page : pages)					
 					page.load(parameters);
 		}
 		 
 		
 		// Iterate through pages, load parameters		
 		
 		for (IDialogPage page : pages) {			
 			
 			if (page == null) return false;
 			
 			if (!page.load(params)) {
 								
 				Dialogs.showError(shell, String.format("Error loading parameters to the %s page.", getCaption(page)), null);
 				return false; // The page unable to load params is invalid
 			}
 		}
 		
 		if (params instanceof CompoundParameters) {
 			
 			IParameters activeParams = ((CompoundParameters) params).getActiveParameters(); 
 			
 			for (IDialogPage page : pages) {			
 				
 				if (page == null) return false;
 				
 				if (!page.load(activeParams)) {
 									
 					Dialogs.showError(shell, String.format("Error loading parameters to the %s page.", getCaption(page)), null);
 					return false; // The page unable to load params is invalid
 				}
 			}
 		}
 						
 		for (IDialogPage page : pages) {			
 			
 			if (page == null) return false;
 			
 			page.interop(null);			
 		}
 		
 		interop(null);
 		
 		return true;		
 	}
 	
 	private boolean saveParameters() {
 		// Iterate through pages, store parameters
 		
 		if (readOnly) {
 			
 			Dialogs.showWarning(shell, "Editor in read-only mode, parameters are not saved.", null);
 			return false;
 		}
 		
 		for (IDialogPage page : pages) {			
 			
 			if (page == null) return false;
 			
 			page.interop(null);
 		}
 		
 		interop(null);
 		
 		for (IDialogPage page : pages) {			
 			
 			if (page == null) return false;
 			if (!page.save(params)) { // Fills in parametersClass
 				
 				Dialogs.showError(shell, String.format("Error saving parameters from the %s page.", getCaption(page)), null);
 				return false;
 			}
 		}
 		
 		if (params instanceof CompoundParameters) {
 			
 			IParameters activeParams = ((CompoundParameters) params).getActiveParameters(); 
 			
 			for (IDialogPage page : pages) {			
 				
 				if (page == null) return false;
 				
 				if (!page.save(activeParams)) {
 			
 					Dialogs.showError(shell, String.format("Error saving parameters from the %s page.", getCaption(page)), null);
 					return false; 
 				}
 			}
 		}
 		
 		return true;
 	}
 	
 	private boolean checkCanClose(boolean isOK) {		
 		// Iterate through pages, ask if the editor can be closed
 		
 		for (IDialogPage page : pages) {			
 			
 			if (page == null) return false;
 			if (!page.canClose(isOK)) {
 				
 				pageContainer.setSelection(findTab(page));
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	protected void addSpeaker(Control control) {
 		
 		addSpeaker(control, SWT.Selection);
 		// addSpeaker(widget, SWT.DefaultSelection);
 	}
 	
 	protected void addSpeaker(Control control, int eventType) {
 		
 		if (control == null) return;
 		
 		control.addListener(eventType, this);
 	}
 	
 	protected void addSpeaker(Class<?> pageClass, String controlName) {
 		
 		addSpeaker(SWTUtils.findControl(findPage(pageClass), controlName));		
 	}
 	
 	protected void addSpeaker(Class<?> pageClass, String controlName, int eventType) {
 		
 		addSpeaker(SWTUtils.findControl(findPage(pageClass), controlName), eventType);
 	}
 	
 	protected Control findControl(Class<?> pageClass, String controlName) {
 		
 		return SWTUtils.findControl(findPage(pageClass), controlName);
 	}
 	
 	protected Control findControl(Composite page, String controlName) {
 		
 		return SWTUtils.findControl(page, controlName);
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected Composite addPage(String caption, Class<?> pageClass) {
 			
 		if (!Composite.class.isAssignableFrom(pageClass)) return null;
 		
 		try {
 			Constructor<Composite> cc = (Constructor<Composite>) pageClass.getConstructor(new Class[] {Composite.class, int.class});
 			if (cc == null) return null;
 			
 			Composite page = cc.newInstance(new Object[] {pageContainer, SWT.NONE});
 			return addPage(caption, page);
 			
 		} catch (InstantiationException e) {
 			
 			result = false;
 			e.printStackTrace();
 			return null;
 			
 		} catch (IllegalAccessException e) {
 			
 			result = false;
 			e.printStackTrace();
 			return null;
 			
 		} catch (SecurityException e) {
 			
 			result = false;
 			e.printStackTrace();
 			return null;
 			
 		} catch (NoSuchMethodException e) {
 			
 			result = false;
 			e.printStackTrace();
 			return null;
 			
 		} catch (IllegalArgumentException e) {
 			
 			result = false;
 			e.printStackTrace();
 			return null;
 			
 		} catch (InvocationTargetException e) {
 			
 			result = false;
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	protected Composite addPage(String caption, Composite page) {
 		
 		TabItem tabItem = new TabItem(pageContainer, SWT.NONE);
 		tabItem.setText(caption);
 		
 		tabItem.setControl(page);
 		
 		if (page instanceof IDialogPage) {
 
 			IDialogPage ppg = (IDialogPage) page;  
 			if (pages != null) pages.add(ppg);			
 		}
 		
 		return page;
 	}
 	
 	protected TabFolder getPageContainer() {
 		
 		return pageContainer;
 	}
 	
 	protected Composite findPage(Class<?> pageClass) {
 		
 		for (IDialogPage page : pages) {
 			
 			if (page == null) continue;
 			
 			if (page.getClass() == pageClass && page instanceof Composite) 
 				return (Composite) page;
 		}
 		
 		return null;		
 	}
 
 	protected Composite findPageInTabs(Class<?> pageClass) {
 		
 		for (TabItem tabItem : pageContainer.getItems()) {
 			
 			Composite page = (Composite) tabItem.getControl();
 			if (page.getClass() == pageClass) return page;
 		}
 		
 		return null;		
 	}
 
 	
 	protected Composite findPageInTabs(String caption) {
 		
 		for (TabItem tabItem : pageContainer.getItems()) {
 			
 			if (tabItem.getText().equalsIgnoreCase(caption)) {
 				
 				Composite page = (Composite) tabItem.getControl();
 				return page;
 			}			
 		}
 		
 		return null;		
 	}
 		
 	protected TabItem findTab(IDialogPage page) {
 		
 		if (page instanceof Composite) 
 			return findTab((Composite) page);
 		
 		return null;
 	}
 	
 	protected TabItem findTab(Composite page) {
 		
 		for (TabItem tabItem : pageContainer.getItems()) {
 			
 			Composite p = (Composite) tabItem.getControl();
 			
 			if (p == page) return tabItem;
 		}			
 
 		return null;		
 	}
 	
 	protected String getCaption(IDialogPage page) {
 		
 		if (!(page instanceof Composite)) return "";
 		
 		TabItem tab = findTab(page);
 		if (tab == null) return "";
 		
 		return tab.getText();
 	}
 	
 	abstract public IParameters createParameters();
 	abstract protected String getCaption();
 	abstract protected void createPages(TabFolder pageContainer);
 	abstract protected void interop(Widget speaker);
 	
 	public void handleEvent (Event event) {
 		
 		interop(event.widget);
 	}
 	
 	
 	protected void pageInterop(Class<?> pageClass, Widget speaker) {
 		
 		Composite page = findPage(pageClass);
 		
 		if (page instanceof IDialogPage)
 			((IDialogPage) page).interop(speaker);
 	}
 
 	public boolean exec(Object sender, String command, Object info) {
 		
 		if (command.equalsIgnoreCase(AbstractBaseDialog.REGISTER_DIALOG_PAGE)) {
 
 			if (info instanceof IDialogPage) {
 					
 				boolean res = pages.add((IDialogPage) info);
 				
 				if (res) {
 					((IDialogPage) info).interop(null);
 					interop(null);
 				}
 				
 				return res;
 			}				
 		}
 		else if (command.equalsIgnoreCase(AbstractBaseDialog.UNREGISTER_DIALOG_PAGE)) {
 
 			if (info instanceof IDialogPage) {
 					
 				boolean res = pages.remove(info);
 				if (res) interop(null);
 				
 				return res;
 			}				
 		}
 					
 		return false;
 		
 	}
 	
 	protected Shell getShell() {
 		
 		return shell;
 	}
 }
