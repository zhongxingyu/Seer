 /**
  * Copyright (C) 2013  Luc Hermans
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
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.gef.EditPart;
 import org.lh.dmlj.schema.AreaSpecification;
 import org.lh.dmlj.schema.INodeTextProvider;
 import org.lh.dmlj.schema.SchemaArea;
 import org.lh.dmlj.schema.SchemaPackage;
 import org.lh.dmlj.schema.SchemaRecord;
 import org.lh.dmlj.schema.Set;
 import org.lh.dmlj.schema.SystemOwner;
 import org.lh.dmlj.schema.editor.command.infrastructure.IModelChangeProvider;
 
 public class AreaTreeEditPart extends AbstractSchemaTreeEditPart<SchemaArea> {
 	
 	public AreaTreeEditPart(SchemaArea area, IModelChangeProvider modelChangeProvider) {		
 		super(area, modelChangeProvider);
 	}
 	
 	@Override
 	public void afterAddItem(EObject owner, EReference reference, Object item) {
 		if (owner == getModel().getSchema() && 
 			reference == SchemaPackage.eINSTANCE.getSchema_Sets()) {
 			
 			// a set is added; if it is a system owned indexed set, make sure we create a child 
 			// edit part for it if the system owner is stored in the model area
 			Set set = (Set) item;
 			if (set.getSystemOwner() != null) {
 				SchemaArea area = set.getSystemOwner().getAreaSpecification().getArea();
 				if (area == getModel()) {					
 					EditPart child = SchemaTreeEditPartFactory.createEditPart(set.getSystemOwner(), 
 																			  modelChangeProvider);					
 					int index = getInsertionIndex(getChildren(), set, getChildNodeTextProviderOrder());					
 					addChild(child, index);					
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void afterMoveItem(EObject oldOwner, EReference reference, Object item, 
 							  EObject newOwner) {
 		
 		if (oldOwner instanceof SchemaArea &&
 			reference == SchemaPackage.eINSTANCE.getSchemaArea_AreaSpecifications()) {
 			
 			// a record or system owner was moved to another area...
 			AreaSpecification areaSpecification = (AreaSpecification) item;
 			if (oldOwner == getModel()) {
 				// ...and the source area matches this edit part's area; a record or system owner 
 				// has been moved away from this area, cleanup the corresponding child edit part
 				Object model;
 				if (areaSpecification.getRecord() != null) {
 					model = areaSpecification.getRecord();
 				} else {
 					model = areaSpecification.getSystemOwner();
 				}
 				// we cannot use the edit part registry to find the child because we're not at the
 				// top (schema) level
 				EditPart child = childFor(model); 
 				removeChild(child);													
 			} else if (newOwner == getModel()) {
 				// ...and the target area matches this edit part's area; a record or system owner 
 				// has been moved towards this area, create and insert the appropriate child edit  
 				// part at the right position
 				Object model;
 				INodeTextProvider<?> nodeTextProvider;
 				if (areaSpecification.getRecord() != null) {
 					model = areaSpecification.getRecord();
 					nodeTextProvider = (INodeTextProvider<?>) model;
 				} else {
 					model = areaSpecification.getSystemOwner();
 					nodeTextProvider = ((SystemOwner) model).getSet();
 				}
 				int i = getInsertionIndex(getChildren(), nodeTextProvider, getChildNodeTextProviderOrder());
 				EditPart child = 
 					SchemaTreeEditPartFactory.createEditPart(model, modelChangeProvider);
 				addChild(child, i);
 			}
 		}		
 		
 	}
 
 	@Override
 	public void afterRemoveItem(EObject owner, EReference reference, Object item) {
 		if (owner == getModel().getSchema() && 
 			reference == SchemaPackage.eINSTANCE.getSchema_Sets()) {
 			
 			// a set was removed; if the set is system owned and the model area contains the system
 			// owner of that set, we need to remove the child edit part belonging to the removed 
 			// system owner; we can use the item because it still has a reference to the obsolete
 			// system owner
 			Set set = (Set) item;
 			if (set.getSystemOwner() != null) {
 				try {
 					// see if a child edit part exists for the system owner
 					EditPart child = childFor(set.getSystemOwner());
 					// yes: the system owner IS contained in the model area; remove the child edit
 					// part
 					removeChild(child);
 				} catch (IllegalArgumentException e) {
 					// no: the system owner is not contained in the model area
 				}
 			}			
 			
 		}
 	}
 
 	@Override
 	public void afterSetFeatures(EObject owner, EStructuralFeature[] features) {
 		if (owner == getNodeTextProvider() && 
 			isFeatureSet(features, SchemaPackage.eINSTANCE.getSchemaArea_Name())) {
 			
 			// the area name has changed... the order of the parent edit part might become 
 			// disrupted, so we have to inform that edit part of this fact
 			nodeTextChanged();						
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
