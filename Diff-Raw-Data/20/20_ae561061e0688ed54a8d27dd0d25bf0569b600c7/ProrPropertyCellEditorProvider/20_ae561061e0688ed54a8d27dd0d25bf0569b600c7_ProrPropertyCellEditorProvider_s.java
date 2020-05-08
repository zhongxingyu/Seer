 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.pror.reqif10.editor.propertiesview;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.CellEditor;
 import org.agilemore.agilegrid.editors.ComboBoxCellEditor;
 import org.agilemore.agilegrid.editors.TextCellEditor;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CommandWrapper;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.rmf.pror.reqif10.configuration.ProrPresentationConfiguration;
 import org.eclipse.rmf.pror.reqif10.editor.agilegrid.AbstractProrCellEditorProvider;
 import org.eclipse.rmf.pror.reqif10.editor.presentation.service.PresentationEditorManager;
 import org.eclipse.rmf.pror.reqif10.editor.presentation.service.PresentationService;
 import org.eclipse.rmf.pror.reqif10.util.ConfigurationUtil;
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.rmf.reqif10.SpecHierarchy;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 
 /**
  * The cell editor provider for the properties view.
  * 
  * @author Lukas Ladenberger
  * @author Michael Jastram
  */
 public class ProrPropertyCellEditorProvider extends AbstractProrCellEditorProvider {
 
 	private final ProrPropertyContentProvider contentProvider;
 	
 	public static String SPEC_HIERARCHY_NAME = "Spec Hierarchy";
 	public static String SPEC_OBJECT_NAME = "Spec Object";
 	public static String SPEC_RELATION_NAME = "Spec Relation";
 	
 	public ProrPropertyCellEditorProvider(AgileGrid agileGrid,
 			AdapterFactory adapterFactory, EditingDomain editingDomain) {
 		super(agileGrid, adapterFactory, editingDomain);
 		contentProvider = (ProrPropertyContentProvider) agileGrid
 				.getContentProvider();
 	}
 
 	@Override
 	protected AttributeValue getAttributeValue(int row, int col) {
 		return contentProvider.getAttributeValue(row);
 	}
 	
 	@Override
 	public boolean canEdit(int row, int col) {
 
 		AttributeValue attrValue = getAttributeValue(row, col);
 		if (attrValue != null) {
 			PresentationService service = PresentationEditorManager
 					.getPresentationService(attrValue, editingDomain);
 			return service == null ? true : service.canEdit();
 		} else {
 			IItemPropertyDescriptor descriptor = this.contentProvider
 					.getItemPropertyDescriptor(row).getItemPropertyDescriptor();
 			if (descriptor != null)
 				return descriptor.canSetProperty(this.contentProvider
 						.getElement());
 		}
 
 		return false;
 
 	}
 
 	@Override
 	public CellEditor getCellEditor(int row, int col, Object hint) {
 
 		// Get the correct celleditor
 		AttributeValue attrValue = getAttributeValue(row, col);
 
 		CellEditor cellEditor = null;
 
 		// If the attribute is a reqif attribute (an attribute value exists),
 		// when try to get the presentation service
 		if (attrValue != null) {
 
 			ProrPresentationConfiguration config = ConfigurationUtil
 					.getPresentationConfig(attrValue, editingDomain);
 			if (config != null) {
 				PresentationService service = PresentationEditorManager.getPresentationService(config);
 				if (service != null)
 					cellEditor = service.getCellEditor(agileGrid,
 						editingDomain, attrValue);
 			}
 
 			if (cellEditor == null)
 				cellEditor = getDefaultCellEditor(attrValue,
 						getAffectedElement(row, col));
 
 		} else { // If the attribute is an EMF attribute (no attribute value
 					// exists) return a default celleditor
 
 			final IItemPropertyDescriptor descriptor = this.contentProvider
 					.getItemPropertyDescriptor(row).getItemPropertyDescriptor();
 			
 			String categoryName = descriptor.getCategory(this.contentProvider.getElement());
 			Object selectedElement = this.contentProvider.getElement();
 			
 			if (categoryName != null && categoryName.equals(SPEC_OBJECT_NAME)) {
 				if (this.contentProvider.getElement() instanceof SpecHierarchy)
 					selectedElement = ((SpecHierarchy) this.contentProvider.getElement())
 							.getObject();
 			}
 			
 			cellEditor = getNonAttributeCellEditor(selectedElement, descriptor);
 				
 		}
 		return cellEditor;
 		
 	}
 	
 	private CellEditor getNonAttributeCellEditor(
 			final Object selectedElement,
 			final IItemPropertyDescriptor descriptor) {
 
 		Collection<?> collection = descriptor
 				.getChoiceOfValues(selectedElement);
 
 		CellEditor cellEditor;
 
 		// Multivalue --> combobox celleditor
 		if (collection != null) {
 			final ArrayList<String> strList = new ArrayList<String>();
 			final ArrayList<Object> objList = new ArrayList<Object>();
 			// Add a null entry. This is particularly fatal when no entries
 			// exist, i.e. the user opens a dropdown that has no entries.
 			strList.add("");
 			objList.add(null);
 			for (Object o : collection) {
 				if (o != null) {
 					String str = descriptor.getLabelProvider(selectedElement)
 							.getText(o);
 					objList.add(o);
 					strList.add(str);
 				}
 			}
 
 			cellEditor = new ComboBoxCellEditor(agileGrid,
 					strList.toArray(new String[strList.size()]), SWT.NONE) {
 
 				@Override
 				protected Object doGetValue() {
 					CCombo combo = (CCombo) getControl();
 					int selectionIndex = combo.getSelectionIndex();
 
 					Command setCmd = SetCommand.create(editingDomain,
 							selectedElement,
 							descriptor.getFeature(selectedElement),
 							objList.get(selectionIndex));
 					Command affectedObjectCommand = getAffectedObjectCommand(
 							selectedElement, setCmd);
 					editingDomain.getCommandStack().execute(
 							affectedObjectCommand);
 					// fixAffectedObjectsOfLastcommand();
 					return strList.get(selectionIndex);
 				}
 
 			};
 
 		} else { // Singlevalue --> text celleditor
 			cellEditor = new TextCellEditor(agileGrid) {
 
 				@Override
 				protected Object doGetValue() {
 					Object value = super.doGetValue();
 					// descriptor.setPropertyValue(selectedElement, value);
 					Command setCmd = SetCommand.create(editingDomain,
 							selectedElement,
 							descriptor.getFeature(selectedElement), value);
 					Command affectedObjectCommand = getAffectedObjectCommand(
 							selectedElement, setCmd);
 					editingDomain.getCommandStack().execute(
 							affectedObjectCommand);
 					// fixAffectedObjectsOfLastcommand();
 					return value;
 				}
 
 			};
 		}
 
 		return cellEditor;
 	}
 	
 //	/**
 //	 * This method undos the last command, wrapps it to change the affected
 //	 * objects, and executes it again.
 //	 * <p>
 //	 * This is a workaround, as we modify properties via
 //	 * {@link IItemPropertyDescriptor#setPropertyValue(Object, Object)}. That
 //	 * method builds the appropriate command and executes it. However, the
 //	 * affected objects are incorrect, as this is typically the
 //	 * {@link SpecElementWithAttributes} (or {@link SpecHierarchy}), but the
 //	 * property belongs to {@link AttributeValue}, which is therefore reported
 //	 * as the affected element.
 //	 */
 //	private void fixAffectedObjectsOfLastcommand() {
 //		Command lastCmd = editingDomain.getCommandStack().getMostRecentCommand();
 //		if (lastCmd == null) return;
 //		editingDomain.getCommandStack().undo();
 //		CommandWrapper wrappedCmd = new CommandWrapper(lastCmd) {
 //			public java.util.Collection<?> getAffectedObjects() {
 //				List<Object> list = new ArrayList<Object>();
 //				list.add(contentProvider.getIdentifiable());
 //				return list;
 //			}
 //		};
 //		editingDomain.getCommandStack().execute(wrappedCmd);
 //	}
 	
 	private Command getAffectedObjectCommand(final Object element, Command cmd) {
 		return new CommandWrapper(cmd) {
 			public java.util.Collection<?> getAffectedObjects() {
 				List<Object> list = new ArrayList<Object>();
 				list.add(element);
 				return list;
 			}
 		};
 	}
 
 	@Override
 	public Object getAffectedElement(int row, int col) {
 		if (this.contentProvider != null)
 				return this.contentProvider.getElement();
 		return null;
 	}
 
 }
