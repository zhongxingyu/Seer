 /**
  * Copyright (C) 2015  Luc Hermans
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
 package org.lh.dmlj.schema.editor.outline.part;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.lh.dmlj.schema.AreaSpecification;
 import org.lh.dmlj.schema.INodeTextProvider;
 import org.lh.dmlj.schema.Schema;
 import org.lh.dmlj.schema.SchemaArea;
 import org.lh.dmlj.schema.SchemaPackage;
 import org.lh.dmlj.schema.SchemaRecord;
 import org.lh.dmlj.schema.Set;
 import org.lh.dmlj.schema.SystemOwner;
 import org.lh.dmlj.schema.editor.command.infrastructure.CommandExecutionMode;
 import org.lh.dmlj.schema.editor.command.infrastructure.IContextDataKeys;
 import org.lh.dmlj.schema.editor.command.infrastructure.IModelChangeProvider;
 import org.lh.dmlj.schema.editor.command.infrastructure.ModelChangeContext;
 import org.lh.dmlj.schema.editor.command.infrastructure.ModelChangeType;
 
 public class AreaTreeEditPart extends AbstractSchemaTreeEditPart<SchemaArea> {
 	
 	public AreaTreeEditPart(SchemaArea area, IModelChangeProvider modelChangeProvider) {		
 		super(area, modelChangeProvider);
 	}
 	
 	@Override
 	public void afterModelChange(ModelChangeContext context) {
 		if (context.getModelChangeType() == ModelChangeType.ADD_RECORD &&
 			context.getCommandExecutionMode() != CommandExecutionMode.UNDO) {
 			
 			// a record is added (execute/redo); create a child edit part for it if the record is 
 			// stored in the model area
 			Schema schema = getModel().getSchema();
 			SchemaRecord record = schema.getRecords().get(schema.getRecords().size() - 1);			
 			SchemaArea area = record.getAreaSpecification().getArea();
 			if (area == getModel()) {					
 				createAndAddChild(record);
 			}
 		} else if (context.getModelChangeType() == ModelChangeType.ADD_RECORD &&
 				   context.getCommandExecutionMode() == CommandExecutionMode.UNDO &&
 				   context.getListenerData() instanceof SchemaRecord) {
 					
 			// an add record operation was undone; remove the record's child edit part if it is 
 			// stored in the model area (in which case we've put the record in the context's 
 			// listener data when processing the before model change event)
 			SchemaRecord record = (SchemaRecord) context.getListenerData();	
 			findAndRemoveChild(record, false);
 		} else if (context.getModelChangeType() == ModelChangeType.CHANGE_AREA_SPECIFICATION) {
 			// area specification change
 			if (context.getContextData().containsKey(IContextDataKeys.RECORD_NAME)) {
 				// the context applies to a record; if the record is moved away from or towards the
 				// model area, take the appropriate action
 				String recordName = context.getContextData().get(IContextDataKeys.RECORD_NAME);
 				SchemaRecord record = getModel().getSchema().getRecord(recordName);
 				SchemaArea area = record.getAreaSpecification().getArea();
 				if (area == getModel() && context.getListenerData() != record) {					
 					// the record is moved towards the model area; create and insert the appropriate 
 					// child edit part at the right position
 					createAndAddChild(record);
 				} else if (area != getModel() && context.getListenerData() == record) {					
 					// the record is moved away to an area other than the model area; cleanup the 
 					// child edit part (we cannot use the edit part registry to find the child 
 					// because we're not at the top [schema] level)
 					findAndRemoveChild(record, false);
 				} else if (area == getModel()) {
 					// the area name might have changed; calling getText() and checking if the area
 					// name really changed won't work because in the model, the area name has 
 					// changed and getText() ALWAYS does a call to the model, so we might do an
 					// unnecessary refresh and child reordering here (the change area specification 
 					// dialog should be blamed for allowing an area rename)
 					nodeTextChanged();
 				}
 			} else {
 				// the context applies to a system owned indexed set
 				String setName = context.getContextData().get(IContextDataKeys.SET_NAME);
 				Set set = getModel().getSchema().getSet(setName);
 				SchemaArea area = set.getSystemOwner().getAreaSpecification().getArea();
 				if (area == getModel() && context.getListenerData() != set.getSystemOwner()) {					
 					// the system owner is moved towards the model area; create and insert the 
 					// appropriate child edit part at the right position
 					createAndAddChild(set.getSystemOwner(), set);
 				} else if (area != getModel() && context.getListenerData() == set.getSystemOwner()) {					
 					// the system owner is moved away to an area other than the model area; cleanup  
 					// the child edit part (we cannot use the edit part registry to find the child 
 					// because we're not at the top [schema] level)
 					findAndRemoveChild(set.getSystemOwner(), false);
 				} else if (area == getModel()) {
 					// the area name might have changed; calling getText() and checking if the area
 					// name really changed won't work because in the model, the area name has 
 					// changed and getText() ALWAYS does a call to the model, so we might do an
 					// unnecessary refresh and child reordering here (the change area specification 
 					// dialog should be blamed for allowing an area rename)
 					nodeTextChanged();
 				}
 			}
 		} else if (context.getModelChangeType() == ModelChangeType.DELETE_SYSTEM_OWNED_SET &&
 				   context.getCommandExecutionMode() != CommandExecutionMode.UNDO &&
 				   context.getListenerData() instanceof SystemOwner) {
 			
 			// a system owned indexed set, of which the owner is stored in the model area, was 
 			// removed (execute/redo); find the child edit part for the system owner (which we've
 			// put in the context's listener data in the before model change callback) and remove it
 			SystemOwner systemOwner = (SystemOwner) context.getListenerData();
 			findAndRemoveChild(systemOwner, false);
 		} else if (context.getModelChangeType() == ModelChangeType.DELETE_SYSTEM_OWNED_SET &&
 				   context.getCommandExecutionMode() == CommandExecutionMode.UNDO) {					
 			
 			// a delete system owned indexed set operation was undone; make sure we create a child 
 			// edit part for the system owner again if it is stored in the model area
 			String setName = context.getContextData().get(IContextDataKeys.SET_NAME);
 			Set set = getModel().getSchema().getSet(setName);
 			SchemaArea area = set.getSystemOwner().getAreaSpecification().getArea();
 			if (area == getModel()) {					
 				createAndAddChild(set.getSystemOwner(), set);
 			}
 		} else if (context.getModelChangeType() == ModelChangeType.SET_PROPERTY && 
 				   context.isPropertySet(SchemaPackage.eINSTANCE.getSchemaArea_Name()) &&
 				   appliesToModelArea(context)) {
 			
 			// the area name has changed (execute/undo/redo)... the order of the parent edit part 
 			// might become disrupted, so we have to inform that edit part of this fact
 			nodeTextChanged();						
 		}
 		// Note that we don't do anything for new system owned indexed sets; that is because a new
 		// index will ALWAYS be stored in a new area (for which an edit part will be created; the
 		// new index will appear as a child of that new edit part).
 	}
 	
 	private boolean appliesToModelArea(ModelChangeContext context) {
 		if (Boolean.TRUE.equals(context.getListenerData())) {
 			return true;
 		} else {
 			String areaName = context.getContextData().get(IContextDataKeys.AREA_NAME);
 			return getModel().getName().equals(areaName);
 		}
 	}
 
 	@Override
 	public void beforeModelChange(ModelChangeContext context) {
 		if (context.getModelChangeType() == ModelChangeType.ADD_RECORD &&
 			context.getCommandExecutionMode() == CommandExecutionMode.UNDO) {
 				
 			// an add record operation is being undone; store the record in the context's listener 
 			// data if it is stored in the model area
 			Schema schema = getModel().getSchema();
 			SchemaRecord record = schema.getRecords().get(schema.getRecords().size() - 1);			
 			SchemaArea area = record.getAreaSpecification().getArea();
 			if (area == getModel()) {					
 				context.setListenerData(record);
 			}
 		} else if (context.getModelChangeType() == ModelChangeType.CHANGE_AREA_SPECIFICATION) {
 			// area specification change; if a record or system owner moves to another area, we need
 			// to know, so if the item involved is stored in the model area, put it in the context's
 			// listener data
 			if (context.getContextData().containsKey(IContextDataKeys.RECORD_NAME)) {
 				// the context applies to a record
 				String recordName = context.getContextData().get(IContextDataKeys.RECORD_NAME);
 				SchemaRecord record = getModel().getSchema().getRecord(recordName);
 				if (record.getAreaSpecification().getArea() == getModel()) {
 					context.setListenerData(record);
 				}
 			} else {
 				// the context applies to a system owned indexed set
 				String setName = context.getContextData().get(IContextDataKeys.SET_NAME);
 				Set set = getModel().getSchema().getSet(setName);
 				if (set.getSystemOwner().getAreaSpecification().getArea() == getModel()) {
 					context.setListenerData(set.getSystemOwner());
 				}
 			}
 		} else if (context.getModelChangeType() == ModelChangeType.DELETE_SYSTEM_OWNED_SET &&
 			context.getCommandExecutionMode() != CommandExecutionMode.UNDO) {			
 			
 			// a system owned indexed set is being removed (execute/redo); if the model area 
 			// contains the system owner of that set, we need to remove the child edit part 
 			// belonging to the removed system owner - put the system owner in the context's
 			// listener data so that we can find and remove the child edit part in the after model
 			// change callback
 			String setName = context.getContextData().get(IContextDataKeys.SET_NAME);
 			Set set = getModel().getSchema().getSet(setName);
 			if (set.getSystemOwner().getAreaSpecification().getArea() == getModel()) {
 				context.setListenerData(set.getSystemOwner());
 			}
 		} else if (context.getModelChangeType() == ModelChangeType.SET_PROPERTY && 
 				   context.isPropertySet(SchemaPackage.eINSTANCE.getSchemaArea_Name()) &&
 				   context.getCommandExecutionMode() != CommandExecutionMode.UNDO &&
 				   context.appliesTo(getModel())) {
 						
 			// the model area's name is changing (execute/redo); put Boolean.TRUE in the context's 
 			// listener's data so that we can respond to this when processing the after model change 
 			// event
 			context.setListenerData(Boolean.TRUE);
 		}
 	}
 
 	@Override
 	protected Class<?>[] getChildNodeTextProviderOrder() {
 		return new Class<?>[] {SchemaRecord.class, Set.class};
 	}
 	
 	@Override
 	protected String getImagePath() {
 		return "icons/data_source.gif";
 	}
 
 	@Override
 	protected List<?> getModelChildren() {
 		
 		List<Object> children = new ArrayList<>();
 		
 		// add the area's records in alphabetical order
 		List<SchemaRecord> records = new ArrayList<>();
 		for (AreaSpecification areaSpecification : getModel().getAreaSpecifications()) {
 			if (areaSpecification.getRecord() != null) {
 				records.add(areaSpecification.getRecord());
 			}
 		}
 		Collections.sort(records);
 		children.addAll(records);
 		
 		// add the area's system owned indexed set owners in alphabetical order
 		List<SystemOwner> systemOwners = new ArrayList<>();
 		for (AreaSpecification areaSpecification : getModel().getAreaSpecifications()) {
 			if (areaSpecification.getSystemOwner() != null) {
 				systemOwners.add(areaSpecification.getSystemOwner());
 			}
 		}
 		Collections.sort(systemOwners);
 		children.addAll(systemOwners);
 		
 		return children;
 	}
 
 	@Override
 	protected INodeTextProvider<SchemaArea> getNodeTextProvider() {
 		return getModel();
 	}	
 
 }
