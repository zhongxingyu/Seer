 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package cc.warlock.rcp.prefs;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jface.bindings.keys.KeyStroke;
 import org.eclipse.jface.bindings.keys.SWTKeySupport;
 import org.eclipse.jface.fieldassist.IContentProposal;
 import org.eclipse.jface.fieldassist.IContentProposalProvider;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkbenchPropertyPage;
 import org.eclipse.ui.part.PageBook;
 
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.client.settings.internal.ClientSettings;
 import cc.warlock.core.client.settings.macro.CommandMacroHandler;
 import cc.warlock.core.client.settings.macro.IMacro;
 import cc.warlock.core.client.settings.macro.IMacroCommand;
 import cc.warlock.core.client.settings.macro.IMacroHandler;
 import cc.warlock.core.client.settings.macro.IMacroProvider;
 import cc.warlock.core.client.settings.macro.internal.Macro;
 import cc.warlock.rcp.ui.ContentAssistCellEditor;
 import cc.warlock.rcp.ui.KeyStrokeCellEditor;
 import cc.warlock.rcp.ui.KeyStrokeText;
 import cc.warlock.rcp.ui.WarlockSharedImages;
 import cc.warlock.rcp.ui.KeyStrokeText.KeyStrokeLockListener;
 
 /**
  *
  * @author Marshall Culpepper
  */
 public class MacrosPreferencePage extends WarlockPreferencePage implements
 		IWorkbenchPropertyPage {
 
 	public static final String PAGE_ID = "cc.warlock.rcp.prefs.macros";
 	
 	protected static String COLUMN_COMMAND = "command";
 	protected static String COLUMN_KEY = "key";
 	
 	protected TableViewer macroTable;
 	protected IWarlockClient client;
 	protected ClientSettings settings;
 	
 	protected ArrayList<Macro> macros = new ArrayList<Macro>();
 
 	protected Button addMacroButton;
 	protected Button removeMacroButton;
 	
 	protected Macro selectedMacro;
 	protected PageBook filterBook;
 
 	protected Text commandText;
 	protected KeyStrokeText keyComboText;
 	protected Menu filterMenu;
 	protected boolean filterByCommand = true;
 	
 	@Override
 	protected Control createContents(Composite parent) {
 		Composite main = new Composite(parent, SWT.NONE);
 		main.setLayout(new GridLayout(2, false));
 		
 		Composite filterComposite = new Composite(main, SWT.NONE);
 		GridLayout layout = new GridLayout(3, false);
 		layout.marginWidth = layout.marginHeight = 0;
 		filterComposite.setLayout(layout);
 		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
 //		data.horizontalSpan = 2;
 		filterComposite.setLayoutData(data);
 		
 		new Label(filterComposite, SWT.NONE).setText("Filter: ");
 	
 		filterBook = new PageBook(filterComposite, SWT.NONE);
 		filterBook.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		
 		commandText = new Text(filterBook, SWT.BORDER);
 		commandText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				macroTable.refresh();
 			}
 		});
 		
 		keyComboText = new KeyStrokeText(filterBook, SWT.BORDER);
 		keyComboText.addKeyStrokeLockListener(new KeyStrokeLockListener() {
 			public void keyStrokeLocked() {
 				macroTable.refresh();
 			}
 			public void keyStrokeUnlocked() {
 				macroTable.refresh();
 			}
 		});
 		
 		filterBook.showPage(commandText);
 
 		Button filterButton = new Button(filterComposite, SWT.ARROW | SWT.DOWN);
 		filterMenu = new Menu(filterButton);
 		filterButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				filterMenu.setVisible(true);
 			}
 		});
 		
 		MenuItem filterByCommand = new MenuItem(filterMenu, SWT.RADIO);
 		filterByCommand.setText("Filter by command");
 		filterByCommand.setSelection(true);
 		filterByCommand.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				MacrosPreferencePage.this.filterByCommand = true;
 				filterBook.showPage(commandText);
 				macroTable.refresh();
 			}
 		});
 		
 		MenuItem filterByKeyCombo= new MenuItem(filterMenu, SWT.RADIO);
 		filterByKeyCombo.setText("Filter by key combo");
 		filterByKeyCombo.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				MacrosPreferencePage.this.filterByCommand = false;
 				filterBook.showPage(keyComboText.getText());
 				macroTable.refresh();
 			}
 		});
 		filterButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false));
 		
 		new Label(main, SWT.NONE);
 		macroTable = new TableViewer(main, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
 		TableViewerColumn commandColumn = new TableViewerColumn(macroTable, SWT.NONE, 0);
 		commandColumn.getColumn().setText("Command");
 		commandColumn.getColumn().setWidth(225);
 		commandColumn.setEditingSupport(new CommandEditingSupport(macroTable));
 		
 		TableViewerColumn keyColumn = new TableViewerColumn(macroTable, SWT.NONE, 1);
 		keyColumn.getColumn().setText("Key Combination");
 		keyColumn.getColumn().setWidth(125);
 		keyColumn.setEditingSupport(new KeyStrokeEditingSupport(macroTable));
 		
 		macroTable.setUseHashlookup(true);
 		macroTable.setColumnProperties(new String[] {COLUMN_COMMAND, COLUMN_KEY});
 		macroTable.setContentProvider(new ArrayContentProvider());
 		macroTable.setLabelProvider(new LabelProvider());
 		macroTable.addFilter(new ViewerFilter() {
 			public boolean select(Viewer viewer, Object parentElement, Object element) {
 				IMacro macro = (IMacro) element;
 				Collection<IMacroHandler> handlers = macro.getHandlers();
 				
 				if (handlers.size() == 1) {
 					IMacroHandler handler = (IMacroHandler) handlers.toArray()[0];
 					if (handler instanceof CommandMacroHandler) {
 						return true;
 					}
 				}
 				return false;
 			}	
 		});
 		macroTable.addFilter(new MacroFilter());
 		
 		macroTable.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				if (macroTable.getSelection().isEmpty()) {
 					removeMacroButton.setEnabled(false);
 				} else {
 					selectedMacro = (Macro) ((IStructuredSelection)macroTable.getSelection()).getFirstElement();
 					removeMacroButton.setEnabled(true);
 				}
 			}
 		});
 		
 		for (IMacro macro : settings.getAllMacros()) {
 			if (macro instanceof Macro) {
 				macros.add(new Macro((Macro)macro));
 			}
 		}
 		
 		macroTable.setInput(macros);
 		macroTable.getTable().setHeaderVisible(true);
 		int listHeight = macroTable.getTable().getItemHeight() * 8;
 		Rectangle trim = macroTable.getTable().computeTrim(0, 0, 0, listHeight);
 		data = new GridData(GridData.FILL, GridData.FILL, true, true);
 		data.heightHint = trim.height;
 		
 		macroTable.getTable().setLayoutData(data);
 		
 		Composite macroButtons = new Composite(main, SWT.NONE);
 		macroButtons.setLayout(new GridLayout(1, true));
 		macroButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 		
 		addMacroButton = new Button(macroButtons, SWT.PUSH);
 		addMacroButton.setText("Add Macro");
 		addMacroButton.setImage(WarlockSharedImages.getImage(WarlockSharedImages.IMG_ADD));
 		addMacroButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				addMacroSelected();
 			}
 		});
 		addMacroButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		
 		removeMacroButton = new Button(macroButtons, SWT.PUSH);
 		removeMacroButton.setText("Remove Macro");
 		removeMacroButton.setImage(WarlockSharedImages.getImage(WarlockSharedImages.IMG_REMOVE));
 		removeMacroButton.setEnabled(false);
 		removeMacroButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				removeMacroSelected();
 			}
 		});
 		removeMacroButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		
 		return main;
 	}
 	
 	protected class MacroFilter extends ViewerFilter {
 		public boolean select(Viewer viewer, Object parentElement, Object element) {
 			IMacro macro = (IMacro)element;
 			
 			if (filterByCommand) {
 				String command = getCommandMacroHandler(macro).getCommand();
 				
 				if (command.equals("")) {
 					return true;
 				}
 				
 				return command.toLowerCase().contains(commandText.getText().toLowerCase());
 			} else {
 				KeyStroke stroke = keyComboText.getKeyStroke();
 				if (stroke != null && stroke.getNaturalKey() != KeyStroke.NO_KEY) {
 					return (stroke.getModifierKeys() == macro.getModifiers() && stroke.getNaturalKey() == macro.getKeyCode());
 				}
 				return true;
 			}
 		}
 	}
 	
 	protected ArrayList<Macro> addedMacros = new ArrayList<Macro>();
 	protected void addMacroSelected ()
 	{
 		Macro macro = new Macro(settings.getMacroConfigurationProvider(), 0);
 		macro.addHandler(new CommandMacroHandler(""));
 		
 		addedMacros.add(macro);
 		macros.add(macro);
 		macroTable.add(macro);
 		
 		macroTable.editElement(macro, 0);
 	}
 	
 	protected ArrayList<Macro> removedMacros = new ArrayList<Macro>();
 
 	protected void removeMacroSelected ()
 	{
 		if (addedMacros.contains(selectedMacro)) {
 			addedMacros.remove(selectedMacro);
 		}
 		else if (macros.contains(selectedMacro)) {
			macros.remove(selectedMacro);
 			removedMacros.add(selectedMacro);
 		}
 		
 		macroTable.remove(selectedMacro);
 	}
 	
 	protected CommandMacroHandler getCommandMacroHandler (IMacro macro) {
 		return (CommandMacroHandler)macro.getHandlers().toArray()[0];
 	}
 	
 	protected class LabelProvider implements ITableLabelProvider
 	{
 		public void addListener(ILabelProviderListener listener) {}
 		public void dispose() {}
 		public Image getColumnImage(Object element, int columnIndex) {
 			return null;
 		}
 		public String getColumnText(Object element, int columnIndex) {
 			IMacro macro = (IMacro) element;
 			if (columnIndex == 0) {
 				return getCommandMacroHandler(macro).getCommand();
 			} else {
 				
 				return SWTKeySupport.getKeyFormatterForPlatform().format(KeyStroke.getInstance(macro.getModifiers(), macro.getKeyCode()));
 			}
 		}
 		public boolean isLabelProperty(Object element, String property) {
 			return true;
 		}
 		public void removeListener(ILabelProviderListener listener) {}
 	}
 	
 	protected class CommandEditingSupport extends EditingSupport implements IContentProposalProvider
 	{
 		protected ContentAssistCellEditor editor;
 		
 		public CommandEditingSupport (TableViewer viewer) {
 			super(viewer);
 			
 			editor = new ContentAssistCellEditor(macroTable.getTable(), SWT.SINGLE, new char[] { '{', '$', '\\' }, this);
 		}
 		
 		protected boolean canEdit(Object element) {
 			return true;
 		}
 		
 		protected CellEditor getCellEditor(Object element) { 
 			return editor;
 		}
 		
 		protected Object getValue(Object element) {
 			return getCommandMacroHandler((IMacro)element).getCommand();
 		}
 		
 		protected void setValue(Object element, Object value) {
 			getCommandMacroHandler((IMacro)element).setCommand((String)value);
 			((Macro)element).setNeedsUpdate(true);
 			
 			macroTable.update(element, null);
 		}
 		
 		protected class MacroCommandContentProposal implements IContentProposal
 		{
 			protected IMacroCommand command;
 			protected String contents;
 			protected int position;
 		
 			public MacroCommandContentProposal (IMacroCommand command, String contents, int position)
 			{
 				this.command = command;
 				this.contents = contents;
 				this.position = position;
 			}
 			
 			public String getContent() {
 				String content = "{" + this.command.getIdentifier() + "}";
 				
 				int leftBracketIndex = contents.substring(0, position).lastIndexOf('{');
 				String text = contents.substring(0, leftBracketIndex) + content + contents.substring(position);
 				
 				return text;
 			}
 			
 			public int getCursorPosition() {
 				return getContent().length();
 			}
 			
 			public String getDescription() {
 				return command.getDescription();
 			}
 			
 			public String getLabel() {
 				return "{"+command.getIdentifier()+"}";
 			}
 		}
 		
 		public IContentProposal[] getProposals(String contents, int position) {
 			ArrayList<IContentProposal> proposals = new ArrayList<IContentProposal>();
 			
 			int lastRightBracket = contents.substring(0, position).lastIndexOf('{');
 			
 			if (lastRightBracket >= 0) {
 				String commandSubstr = contents.substring(lastRightBracket+1, position).toLowerCase();
 				
 				for (IMacroCommand command : settings.getAllMacroCommands()) {
 					if (command.getIdentifier().toLowerCase().startsWith(commandSubstr)) {
 						proposals.add(new MacroCommandContentProposal(
 							command, contents.substring(0, contents.length()-1), position-1));
 					}
 				}
 			}
 			
 			return proposals.toArray(new IContentProposal[proposals.size()]);
 		}
 	}
 	
 	protected class KeyStrokeEditingSupport extends EditingSupport
 	{
 		protected KeyStrokeCellEditor editor;
 		
 		public KeyStrokeEditingSupport (TableViewer viewer)
 		{
 			super(viewer);
 			
 			this.editor = new KeyStrokeCellEditor(macroTable.getTable(), SWT.SINGLE);
 		}
 		
 		protected boolean canEdit(Object element) {
 			return true;
 		}
 
 		protected CellEditor getCellEditor(Object element) {
 			return editor;
 		}
 
 		protected Object getValue(Object element) {
 			Macro macro = (Macro) element;
 			return KeyStroke.getInstance(macro.getModifiers(), macro.getKeyCode());
 		}
 
 		protected void setValue(Object element, Object value) {
 			Macro macro = (Macro) element;
 			KeyStroke stroke = (KeyStroke) value;
 			
 			macro.setModifiers(stroke.getModifierKeys());
 			macro.setKeyCode(stroke.getNaturalKey());
 			
 			macroTable.update(macro, null);
 		}	
 	}
 
 	@Override
 	public void setElement(IAdaptable element) {
 		client = (IWarlockClient)element.getAdapter(IWarlockClient.class);
 		settings = (ClientSettings)client.getClientSettings();
 	}
 	
 	@Override
 	public boolean performOk() {
 		for (Macro macro : macros) {
 			if (macro.needsUpdate() && !addedMacros.contains(macro)) {
 				IMacroProvider provider = (IMacroProvider) macro.getProvider();
 				provider.replaceMacro(macro.getOriginalMacro(), macro);
 			}
 		}
 		
 		for (Macro macro : removedMacros) {
 			IMacroProvider provider = (IMacroProvider) macro.getProvider();
 			provider.removeMacro(macro.getOriginalMacro());
 		}
 		
 		for (Macro macro : addedMacros) {
 			settings.getMacroConfigurationProvider().addMacro(macro);
 		}
 		
 		return true;
 	}
 }
