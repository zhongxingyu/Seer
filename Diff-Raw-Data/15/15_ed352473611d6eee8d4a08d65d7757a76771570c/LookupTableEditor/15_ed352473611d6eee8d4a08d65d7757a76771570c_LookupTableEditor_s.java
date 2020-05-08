 /*-
  * Copyright © 2012 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.lookuptable.editor;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import org.eclipse.core.commands.operations.IUndoContext;
 import org.eclipse.core.commands.operations.ObjectUndoContext;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ColumnViewer;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.operations.RedoActionHandler;
 import org.eclipse.ui.operations.UndoActionHandler;
 import org.eclipse.ui.part.EditorPart;
 import org.eclipse.ui.part.FileEditorInput;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.gda.lookuptable.editor.LookupTableMultiPageEditor.ColumnLabelProviderCustom;
 
 public class LookupTableEditor extends EditorPart {
 	private static final Logger logger = LoggerFactory.getLogger(LookupTableEditor.class);
 	private IEditorInput input;
 	private IEditorSite site;
 	private TableViewer tv;
 	private IUndoContext undoContext;
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 
 	}
 
 	@Override
 	public void doSaveAs() {
 
 	}
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
 		this.site = site;
 		this.input = input;
 	}
 
 	@Override
 	public IWorkbenchPartSite getSite() {
 		return site;
 	}
 
 	@Override
 	public boolean isDirty() {
 		return false;
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 
 		Composite composite = new Composite(parent, SWT.NONE);
 		GridLayout glayout = new GridLayout();
 		glayout.marginWidth = 0;
 		glayout.marginHeight = 0;
 		glayout.horizontalSpacing = 0;
 		glayout.verticalSpacing = 0;
 		composite.setLayout(glayout);
 
 //		Composite buttonsCmp = new Composite(composite, SWT.None);
 //		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
 //		layoutData.heightHint = 40;
 //		buttonsCmp.setLayoutData(layoutData);
 //		buttonsCmp.setLayout(new FillLayout());
 //
 //		Button btnAddRow = new Button(buttonsCmp, SWT.PUSH);
 //		btnAddRow.setText("Add Row");
 //		btnAddRow.setImage(Activator.getDefault().getImageRegistry().get(ImageConstants.IMG_ADD));
 //
 //		Button btnDelRow = new Button(buttonsCmp, SWT.PUSH);
 //		btnDelRow.setText("Delete Row");
 //		btnDelRow.setImage(Activator.getDefault().getImageRegistry().get(ImageConstants.IMG_DEL));
 
 		Composite tvCmp = new Composite(composite, SWT.None);
 		tvCmp.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		FillLayout layout = new FillLayout();
 		tvCmp.setLayout(layout);
 
 		tv = new TableViewer(tvCmp, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
 
 		final Table table = tv.getTable();
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 
 		tv.setContentProvider(new ArrayContentProvider());
 
 		InputStream contents = null;
 		ArrayList<String> lines = new ArrayList<String>();
 		try {
 			IFile file = ((FileEditorInput) input).getFile();
 			file.refreshLocal(IResource.DEPTH_ONE, null);
 			contents = file.getContents();
 
 			BufferedReader br = new BufferedReader(new InputStreamReader(contents));
 			String nextLine;
 			boolean headerFound = false;
 
 			while (((nextLine = br.readLine()) != null) && (nextLine.length() > 0)) {
 				if (nextLine.startsWith("#")) {
 					continue;
 				}
 				if (nextLine.startsWith("ScannableNames")) {
 					String[] names = nextLine.split("[, \t][, \t]*");
 					int colCount = 0;
 					for (int count = 1; count < names.length; count++) {
 						final TableViewerColumn col = new TableViewerColumn(tv, SWT.NONE, colCount);
 						col.setLabelProvider(new ColumnLabelProviderCustom(colCount) {
 							@Override
 							public String getText(Object element) {
 								if (element instanceof String) {
 									String str = (String) element;
 									return str.split("[, \t][, \t]*")[getIndex()];
 								}
 								return super.getText(element);
 							}
 						});
 						TableColumn tableColumn = col.getColumn();
 						tableColumn.setAlignment(SWT.CENTER);
 						col.getColumn().setText(names[count]);
 						col.getColumn().setWidth(100);
 						col.setEditingSupport(new LookupTableColEditingSupport(tv, colCount));
 						colCount++;
 					}
 					headerFound = true;
 				} else if (nextLine.startsWith("ScannableUnits")) {
 					String[] names = nextLine.split("[, \t][, \t]*");
 					if (headerFound) {
 						TableColumn[] columns = tv.getTable().getColumns();
 						int col = 0;
 						for (int i = 1; i < names.length; i++) {
 							TableColumn tableColumn = columns[col];
 							String tcText = tableColumn.getText() + " (" + names[i] + ")";
 							tableColumn.setText(tcText);
 							tableColumn.setToolTipText(tcText);
 							col++;
 						}
 					}
 					continue;
 				} else if (headerFound) {
 					lines.add(nextLine);
 				}
 			}
 
 		} catch (CoreException e) {
			logger.error("TODO put description of error here", e);
 		} catch (IOException e) {
			logger.error("TODO put description of error here", e);
 		}
 		tv.setInput(lines.toArray());
 
 		site.setSelectionProvider(tv);
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(resChgList);
 	}
 
 	@Override
 	public void setFocus() {
 
 	}
 
 	public IAction getAction(String id) {
 		if (ActionFactory.UNDO.getId().equals(id)) {
 			return new UndoActionHandler(getSite(), getUndoContext());
 		} else if (ActionFactory.REDO.getId().equals(id)) {
 			return new RedoActionHandler(getSite(), getUndoContext());
 		}
 		return null;
 	}
 
 	private IUndoContext getUndoContext() {
 		if (undoContext == null) {
 			undoContext = new ObjectUndoContext(this, getPartName());
 		}
 		return undoContext;
 	}
 
 	@Override
 	public String getPartName() {
 		return "Table view";
 	}
 
 	private IResourceChangeListener resChgList = new IResourceChangeListener() {
 
 		private IResourceDeltaVisitor _visitor = new DeltaVisitor();
 
 		@Override
 		public void resourceChanged(IResourceChangeEvent event) {
 			try {
 				event.getDelta().accept(_visitor);
 			} catch (CoreException e) {
				logger.error("TODO put description of error here", e);
 			}
 		}
 	};
 
 	private class DeltaVisitor implements IResourceDeltaVisitor {
 
 		@Override
 		public boolean visit(IResourceDelta delta) throws CoreException {
 			boolean result = true;
 			IResource resource = delta.getResource();
 			if (resource instanceof IFile && resource.equals(((FileEditorInput) input).getFile())) {
 				result = false;
 				if (delta.getKind() == IResourceDelta.CHANGED) {
 					tv.refresh(true);
 				}
 			}
 			return result;
 		}
 	}
 
 	@Override
 	public void dispose() {
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resChgList);
 
 	}
 
 	public void refresh(InputStream is) {
 
 		BufferedReader br = new BufferedReader(new InputStreamReader(is));
 		String nextLine;
 		boolean headerFound = false;
 
 		ArrayList<String> lines = new ArrayList<String>();
 		try {
 			while (((nextLine = br.readLine()) != null) && (nextLine.length() > 0)) {
 				if (nextLine.startsWith("#")) {
 					continue;
 				}
 				if (nextLine.startsWith("ScannableNames")) {
 					headerFound = true;
 					continue;
 				} else if (nextLine.startsWith("ScannableUnits")) {
 					continue;
 				} else if (headerFound) {
 					lines.add(nextLine);
 				}
 				tv.setInput(lines.toArray());
 			}
 		} catch (IOException e) {
			logger.error("TODO put description of error here", e);
 		}
 	}
 
 	static class LookupTableColEditingSupport extends EditingSupport {
 
 		private final int index;
 
 		public LookupTableColEditingSupport(ColumnViewer colViewer, int index) {
 			super(colViewer);
 			this.index = index;
 		}
 
 		@Override
 		protected CellEditor getCellEditor(Object element) {
 			Table table = ((TableViewer) getViewer()).getTable();
 			return new TextCellEditor(table);
 		}
 
 		@Override
 		protected boolean canEdit(Object element) {
 			//return true;
 			return false;
 		}
 
 		@Override
 		protected Object getValue(Object element) {
 			return ((String) element).split("[, \t][, \t]*")[index];
 		}
 
 		@Override
 		protected void setValue(Object element, Object value) {
 
 		}
 
 	}
 
 }
