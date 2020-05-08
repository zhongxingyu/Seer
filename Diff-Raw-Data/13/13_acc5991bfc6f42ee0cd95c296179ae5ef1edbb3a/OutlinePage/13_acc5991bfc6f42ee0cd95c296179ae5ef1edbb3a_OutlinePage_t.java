 /**
  * Copyright (C) 2014  Luc Hermans
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program.  If
  * not, see <http://www.gnu.org/licenses/>.
  * 
  * Contact information: kozzeluc@gmail.com.
  */
 package org.lh.dmlj.schema.editor.outline;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gef.DefaultEditDomain;
 import org.eclipse.gef.EditDomain;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPartViewer;
 import org.eclipse.gef.ui.actions.ActionRegistry;
 import org.eclipse.gef.ui.parts.ContentOutlinePage;
 import org.eclipse.gef.ui.parts.SelectionSynchronizer;
 import org.eclipse.gef.ui.parts.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.part.IPageSite;
 import org.eclipse.ui.part.PageBook;
 import org.lh.dmlj.schema.ConnectionLabel;
 import org.lh.dmlj.schema.ConnectionPart;
 import org.lh.dmlj.schema.Connector;
 import org.lh.dmlj.schema.Schema;
 import org.lh.dmlj.schema.Set;
 import org.lh.dmlj.schema.editor.SchemaEditor;
 import org.lh.dmlj.schema.editor.outline.part.SchemaTreeEditPartFactory;
 import org.lh.dmlj.schema.editor.property.IGraphicalEditorProvider;
 
 public class OutlinePage 
 	extends ContentOutlinePage implements IGraphicalEditorProvider<SchemaEditor> {
 	
 	private SchemaEditor editor;
 	private PageBook 	 pageBook;
 	
 	public OutlinePage(SchemaEditor editor) {
 		super(new TreeViewer());
 		this.editor = editor;
 	}
 
 	public boolean canConvertEditPart(EditPartViewer viewer, EditPart part) {
		return true;
 	}	
 	
 	private void configureOutlineViewer() {
 		getViewer().setEditDomain((EditDomain) editor.getAdapter(DefaultEditDomain.class));
 		getViewer().setEditPartFactory(new SchemaTreeEditPartFactory(editor));
 	}
 	
 	public EditPart convert(EditPartViewer viewer, EditPart part) {
 		EditPart editPart = (EditPart) viewer.getEditPartRegistry().get(part.getModel());
 		if (editPart != null) {
 			return editPart;
 		}
 		Object model = part.getModel();
 		if (model instanceof Connector) {
 			Connector connector = (Connector) model;
 			EObject target = getTarget(connector.getConnectionPart().getMemberRole().getSet());
 			return (EditPart) viewer.getEditPartRegistry().get(target);
 		} else if (model instanceof ConnectionLabel) {
 			ConnectionLabel connectionLabel = (ConnectionLabel) model;
 			EObject target = getTarget(connectionLabel.getMemberRole().getSet());
 			return (EditPart) viewer.getEditPartRegistry().get(target);
 		} else if (model instanceof ConnectionPart) {
 			ConnectionPart connectionPart = (ConnectionPart) model;
 			EObject target = getTarget(connectionPart.getMemberRole().getSet());
 			return (EditPart) viewer.getEditPartRegistry().get(target);
 		} else if (model instanceof Set) {
 			Set set = (Set) model;
 			EObject target = set.getMembers().get(0).getConnectionParts().get(0);
			return (EditPart) viewer.getEditPartRegistry().get(target);
 		}
 		return null;
 	}
 
 	public void createControl(Composite parent) {
 		pageBook = new PageBook(parent, SWT.NONE);
 		pageBook.showPage(getViewer().createControl(pageBook));
 		configureOutlineViewer();
 		hookOutlineViewer();
 		initializeOutlineViewer();
 	}	
 	
 	public void dispose() {
 		unhookOutlineViewer();
 		super.dispose();
 	}
 	
 	public Control getControl() {
 		return pageBook;
 	}
 	
 	@Override
 	public SchemaEditor getEditor() {
 		// we need this for the tabbed property sections (Properties view)
 		return editor;
 	}
 	
 	private EObject getTarget(Set set) {
 		if (set.getSystemOwner() != null) {
 			return set.getSystemOwner();
 		} else {
 			return set;
 		}
 	}
 
 	private void hookOutlineViewer() {
 		SelectionSynchronizer selectionSynchronizer = 
 			(SelectionSynchronizer) editor.getAdapter(SelectionSynchronizer.class);
 		selectionSynchronizer.addViewer(getViewer());
 	}
 	
 	public void init(IPageSite pageSite) {
 		super.init(pageSite);
 		ActionRegistry registry = (ActionRegistry) editor.getAdapter(ActionRegistry.class);
 		IActionBars bars = pageSite.getActionBars();
 		String id = ActionFactory.UNDO.getId();
 		bars.setGlobalActionHandler(id, registry.getAction(id));
 		id = ActionFactory.REDO.getId();
 		bars.setGlobalActionHandler(id, registry.getAction(id));
 		id = ActionFactory.DELETE.getId();
 		bars.setGlobalActionHandler(id, registry.getAction(id));
 		bars.updateActionBars();
 	}	
 	
 	private void initializeOutlineViewer() {
 		setSchema(editor.getSchema());
 	}
 	
 	public void setSchema(Schema schema) {
 		getViewer().setContents(schema);
 	}
 	
 	private void unhookOutlineViewer() {
 		SelectionSynchronizer selectionSynchronizer = 
 			(SelectionSynchronizer) editor.getAdapter(SelectionSynchronizer.class);
 		selectionSynchronizer.removeViewer(getViewer());
 	}
 		
 }
