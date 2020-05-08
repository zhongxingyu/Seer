 /*******************************************************************************
  * Copyright (c) 2007 IBM Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 
  *******************************************************************************/
 
 package org.eclipse.imp.pdb.browser;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.imp.pdb.analysis.AnalysisException;
 import org.eclipse.imp.pdb.facts.IRelation;
 import org.eclipse.imp.pdb.facts.ISet;
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.pdb.facts.db.FactBase;
 import org.eclipse.imp.pdb.facts.db.IFactBaseListener;
 import org.eclipse.imp.pdb.facts.db.IFactKey;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleManager;
 import org.eclipse.ui.console.MessageConsole;
 import org.eclipse.ui.console.MessageConsoleStream;
 import org.eclipse.ui.part.ViewPart;
 
 public class FactBrowserView extends ViewPart implements IFactBaseListener {
 	public static final String ID = "org.eclipse.imp.pdb.factBrowser";
 	
 	private static final String TYPE_PROPERTY = "type";
 	private static final String BASE_TYPE_PROPERTY = "basetype";
 	private static final String CONTEXT_PROPERTY = "context";
 
 	private static final String DETAILS_CONSOLE = "Fact Details";
 
 	private FactBase factBase = FactBase.getInstance();
 	private TableViewer tableViewer;
 	private Table table;
 
 	private MessageConsoleStream fMsgStream;
 
 	public FactBrowserView() {
 		factBase.addListener(this);
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		parent.setLayout(new FillLayout(SWT.VERTICAL));
 
 		table = new Table(parent, SWT.FULL_SELECTION);
 		table.setLinesVisible(true);
 		table.setHeaderVisible(true);
 
 		TableColumn type = new TableColumn(table, SWT.NONE);
 		type.setText("Type");
 		type.setResizable(true);
 
 		TableColumn baseType = new TableColumn(table, SWT.NONE);
 		baseType.setText("Base Type");
 		baseType.setResizable(true);
 
 		TableColumn context = new TableColumn(table, SWT.NONE);
 		context.setText("Context");
 		context.setResizable(true);
 
 		TableColumn count = new TableColumn(table, SWT.NONE);
 		count.setText("#Elements");
 		count.setResizable(true);
 
 		tableViewer = new TableViewer(table);
 		tableViewer.setColumnProperties(new String[] { TYPE_PROPERTY,
 				BASE_TYPE_PROPERTY, CONTEXT_PROPERTY });
 		tableViewer.setLabelProvider(new LabelProvider());
 
 		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 
 				ISelection sel = tableViewer.getSelection();
 				if (sel instanceof IStructuredSelection) {
 					IFactKey row = (IFactKey) ((IStructuredSelection) sel)
 							.getFirstElement();
 					echoKeyValueToConsole(row);
 				}
 			}
 		});
 
 		fillTable();
 
 		addAnalyzePulldownMenu();
 		addProjectSelectionBox();
 	}
 
 	private void addAnalyzePulldownMenu() {
 		IToolBarManager toolbar = getViewSite().getActionBars()
 				.getToolBarManager();
 		TriggerAnalysisMenuAction menuAction = new TriggerAnalysisMenuAction();
 		toolbar.add(menuAction);
 	}
 
 	private void addProjectSelectionBox() {
 		IToolBarManager toolbar = getViewSite().getActionBars()
 				.getToolBarManager();
 		SelectContextMenuAction menuAction = SelectContextMenuAction
 				.getInstance();
 		toolbar.add(menuAction);
 	}
 
 	private void fillTable() {
 		for (IFactKey key : factBase.getAllKeys()) {
 			addFactRow(key);
 		}
 	}
 
 	protected MessageConsole findConsole(String consoleName) {
 		MessageConsole myConsole = null;
 		final IConsoleManager consoleManager = ConsolePlugin.getDefault()
 				.getConsoleManager();
 		IConsole[] consoles = consoleManager.getConsoles();
 		for (int i = 0; i < consoles.length; i++) {
 			IConsole console = consoles[i];
 			if (console.getName().equals(consoleName))
 				myConsole = (MessageConsole) console;
 		}
 		if (myConsole == null) {
 			myConsole = new MessageConsole(consoleName, null);
 			consoleManager.addConsoles(new IConsole[] { myConsole });
 		}
 		consoleManager.showConsoleView(myConsole);
 		return myConsole;
 	}
 
 	public MessageConsoleStream getConsoleStream() {
 		if (fMsgStream == null) {
 			fMsgStream = findConsole(DETAILS_CONSOLE).newMessageStream();
 		}
 		return fMsgStream;
 	}
 
 	private void addFactRow(IFactKey key) {
 		tableViewer.add(key);
 		repackTable();
 	}
 
 	private void repackTable() {
 		for (TableColumn column : table.getColumns()) {
 			column.pack();
 		}
 	}
 
 	private void removeFact(IFactKey key) {
 		tableViewer.remove(key);
 	}
 
 	private void updateFact(IFactKey key) {
 		// TODO find something useful to do here
 	}
 
 	@Override
 	public void setFocus() {
 		// TODO Auto-generated method stub
 	}
 
 	private class LabelProvider implements ITableLabelProvider {
 		private static final int TYPE_COLUMN = 0;
 		private static final int BASE_TYPE_COLUMN = 1;
 		private static final int CONTEXT_COLUMN = 2;
 		private static final int COUNT_COLUMN = 3;
 
 		public Image getColumnImage(Object element, int columnIndex) {
 			return null;
 		}
 
 		public String getColumnText(Object element, int columnIndex) {
 			IFactKey row = (IFactKey) element;
 
 			switch (columnIndex) {
 			case TYPE_COLUMN:
 				return row.getType().toString();
 			case BASE_TYPE_COLUMN:
				return row.getType().toString();
 			case CONTEXT_COLUMN:
 				return row.getContext().toString();
 			case COUNT_COLUMN:
 				try {
 					return countValue(FactBase.getInstance().getFact(row));
 				} catch (AnalysisException e) {
 					return "<unavailable>";
 				}
 			}
 
 			return "<invalid>";
 		}
 
 		public void addListener(ILabelProviderListener listener) {
 		}
 
 		public void dispose() {
 		}
 
 		public boolean isLabelProperty(Object element, String property) {
 			return false;
 		}
 
 		public void removeListener(ILabelProviderListener listener) {
 		}
 	}
 
 	public void factChanged(final IFactKey key, IValue newValue, final Reason reason) {
 		run(new Runnable() {
 			public void run() {
 				switch (reason) {
 				case FACT_DEFINED:
 					addFactRow(key);
 					break;
 				case FACT_REMOVED:
 					removeFact(key);
 				case FACT_UPDATED:
 					updateFact(key);
 				}
 			}
 		});
 
 	}
 	
 	private void run(Runnable r) {
 		PlatformUI.getWorkbench().getDisplay().asyncExec(r);
 	}
 	
 	public String countValue(IValue fact) {
 		if (fact instanceof ISet) {
 			return Integer.toString(((ISet) fact).size());
 		} else if (fact instanceof IRelation) {
 			return Integer.toString(((IRelation) fact).size());
 		} else {
 			return "1";
 		}
 	}
 
 	public void factBaseCleared() {
 		run(new Runnable() {
 			public void run() {
 				tableViewer.refresh();
 			}
 		});
 	}
 
 	public void removeCurrentSelection() {
 		ISelection s = tableViewer.getSelection();
 		if (s instanceof StructuredSelection) {
 			StructuredSelection selection = (StructuredSelection) s;
 			Iterator<?> iter = selection.iterator();
 
 			while (iter.hasNext()) {
 				IFactKey key = (IFactKey) iter.next();
 				factBase.removeFact(key);
 			}
 		}
 	}
 
 	public List<IFactKey> getCurrentSelection() {
 		ISelection s = tableViewer.getSelection();
 		List<IFactKey> result = new ArrayList<IFactKey>();
 		if (s instanceof StructuredSelection) {
 			StructuredSelection selection = (StructuredSelection) s;
 			Iterator<?> iter = selection.iterator();
 
 			while (iter.hasNext()) {
 				IFactKey key = (IFactKey) iter.next();
 				result.add(key);
 			}
 		}
 		return result;
 	}
 
 	private void echoKeyValueToConsole(IFactKey row) {
 		MessageConsoleStream ms = getConsoleStream();
 		try {
 			ms.println(row.toString() + " = ");
 			ms.println(factBase.getFact(row).toString());
 		} catch (AnalysisException e) {
 			ms.println("Error retrieving fact value from factbase: "
 					+ e.getMessage());
 		}
 	}
 }
