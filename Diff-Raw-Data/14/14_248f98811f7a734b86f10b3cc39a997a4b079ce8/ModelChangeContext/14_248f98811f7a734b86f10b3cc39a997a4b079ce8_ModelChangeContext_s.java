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
 package org.lh.dmlj.schema.editor.command.infrastructure;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.lh.dmlj.schema.ConnectionLabel;
 import org.lh.dmlj.schema.ConnectionPart;
 import org.lh.dmlj.schema.Connector;
 import org.lh.dmlj.schema.DiagramData;
 import org.lh.dmlj.schema.DiagramLabel;
 import org.lh.dmlj.schema.DiagramNode;
 import org.lh.dmlj.schema.Guide;
 import org.lh.dmlj.schema.MemberRole;
 import org.lh.dmlj.schema.Ruler;
 import org.lh.dmlj.schema.Schema;
 import org.lh.dmlj.schema.SchemaArea;
 import org.lh.dmlj.schema.SchemaRecord;
 import org.lh.dmlj.schema.Set;
 import org.lh.dmlj.schema.SystemOwner;
 
 public class ModelChangeContext {
 
 	private CommandExecutionMode commandExecutionMode;
 	private Map<String, String> contextData = new HashMap<>();
 	private Object listenerData;
 	private ModelChangeType modelChangeType;
 	private Schema schema;
 	
 	public static String getQualifiedFeatureName(EStructuralFeature feature) {
 		return feature.getContainerClass().getSimpleName() + "." + feature.getName();
 	}
 	
 	public ModelChangeContext(ModelChangeType modelChangeType) {
 		super();
 		this.modelChangeType = modelChangeType;
 	}
 
 	public boolean appliesTo(ConnectionPart connectionPart) {
 		checkAppliesTo(connectionPart);
 		MemberRole memberRole = connectionPart.getMemberRole();
 		Set set = memberRole.getSet();
 		SchemaRecord record = memberRole.getRecord();
 		int connectionPartIndex = memberRole.getConnectionParts().indexOf(connectionPart);
 		return contextData.size() == 3 && 
 			   set.getName().equals(contextData.get(IContextDataKeys.SET_NAME)) && 
 			   record.getName().equals(contextData.get(IContextDataKeys.RECORD_NAME)) &&
 			   contextData.containsKey(IContextDataKeys.CONNECTION_PART_INDEX) &&
 			   connectionPartIndex == Integer.valueOf(contextData.get(IContextDataKeys.CONNECTION_PART_INDEX)) ||				   
 			   contextData.size() == 4 &&
 			   contextData.containsKey(IContextDataKeys.PROPERTY_NAME) &&
 			   set.getName().equals(contextData.get(IContextDataKeys.SET_NAME)) &&
 			   record.getName().equals(contextData.get(IContextDataKeys.RECORD_NAME)) &&
 			   contextData.containsKey(IContextDataKeys.CONNECTION_PART_INDEX) &&
 			   connectionPartIndex == Integer.valueOf(contextData.get(IContextDataKeys.CONNECTION_PART_INDEX));		
 	}
 	
 	public boolean appliesTo(Connector connector) {
 		checkAppliesTo(connector);
 		MemberRole memberRole = connector.getConnectionPart().getMemberRole();
 		Set set = memberRole.getSet();
 		SchemaRecord record = memberRole.getRecord();
 		int connectionPartIndex = 
 			memberRole.getConnectionParts().indexOf(connector.getConnectionPart());
 		return contextData.size() == 3 && 
 			   set.getName().equals(contextData.get(IContextDataKeys.SET_NAME)) && 
 			   record.getName().equals(contextData.get(IContextDataKeys.RECORD_NAME)) &&
 			   contextData.containsKey(IContextDataKeys.CONNECTION_PART_INDEX) &&
 			   connectionPartIndex == Integer.valueOf(contextData.get(IContextDataKeys.CONNECTION_PART_INDEX)) ||				   
 			   contextData.size() == 4 &&
 			   contextData.containsKey(IContextDataKeys.PROPERTY_NAME) &&
 			   set.getName().equals(contextData.get(IContextDataKeys.SET_NAME)) &&
 			   record.getName().equals(contextData.get(IContextDataKeys.RECORD_NAME)) &&
 			   contextData.containsKey(IContextDataKeys.CONNECTION_PART_INDEX) &&
 			   connectionPartIndex == Integer.valueOf(contextData.get(IContextDataKeys.CONNECTION_PART_INDEX));				
 	}
 	
 	public boolean appliesTo(Guide guide) {
 		checkAppliesTo(guide);
 		Ruler ruler = guide.getRuler();
 		int rulerIndex = ruler.getDiagramData().getRulers().indexOf(ruler);
 		int guideIndex = ruler.getGuides().indexOf(guide);
 		return contextData.size() == 2 &&
 			   contextData.containsKey(IContextDataKeys.RULER_INDEX) &&
 			   rulerIndex == Integer.valueOf(contextData.get(IContextDataKeys.RULER_INDEX)) &&
 			   contextData.containsKey(IContextDataKeys.GUIDE_INDEX) &&
 			   guideIndex == Integer.valueOf(contextData.get(IContextDataKeys.GUIDE_INDEX)) ||
 			   contextData.size() == 3 &&
 			   contextData.containsKey(IContextDataKeys.PROPERTY_NAME) &&
 			   contextData.containsKey(IContextDataKeys.RULER_INDEX) &&
 			   rulerIndex == Integer.valueOf(contextData.get(IContextDataKeys.RULER_INDEX)) &&
 			   contextData.containsKey(IContextDataKeys.GUIDE_INDEX) &&
 			   guideIndex == Integer.valueOf(contextData.get(IContextDataKeys.GUIDE_INDEX));		
 	}
 	
 	public boolean appliesTo(MemberRole memberRole) {
 		checkAppliesTo(memberRole);
 		Set set = memberRole.getSet();
 		SchemaRecord record = memberRole.getRecord();
 		return contextData.size() == 2 && 
 			   set.getName().equals(contextData.get(IContextDataKeys.SET_NAME)) && 
 			   record.getName().equals(contextData.get(IContextDataKeys.RECORD_NAME)) ||
 			   contextData.size() == 3 &&
 			   contextData.containsKey(IContextDataKeys.PROPERTY_NAME) &&
 			   set.getName().equals(contextData.get(IContextDataKeys.SET_NAME)) &&
 			   record.getName().equals(contextData.get(IContextDataKeys.RECORD_NAME));		
 	}
 	
 	public boolean appliesTo(Ruler ruler) {
 		checkAppliesTo(ruler);
 		int rulerIndex = ruler.getDiagramData().getRulers().indexOf(ruler);
 		return contextData.size() == 1 &&
 			   contextData.containsKey(IContextDataKeys.RULER_INDEX) &&
 			   rulerIndex == Integer.valueOf(contextData.get(IContextDataKeys.RULER_INDEX)) ||
 			   contextData.size() == 2 &&
 			   contextData.containsKey(IContextDataKeys.PROPERTY_NAME) &&
 			   contextData.containsKey(IContextDataKeys.RULER_INDEX) &&
 			   rulerIndex == Integer.valueOf(contextData.get(IContextDataKeys.RULER_INDEX));		
 	}
 	
 	public boolean appliesTo(SchemaArea area) {
 		checkAppliesTo(area);
 		return contextData.size() == 1 && 
 			   area.getName().equals(contextData.get(IContextDataKeys.AREA_NAME)) ||
 			   contextData.size() == 2 &&
 			   contextData.containsKey(IContextDataKeys.PROPERTY_NAME) &&
 			   area.getName().equals(contextData.get(IContextDataKeys.AREA_NAME));		
 	}
 	
 	public boolean appliesTo(SchemaRecord record) {
 		checkAppliesTo(record);
 		return contextData.size() == 1 && 
 			   record.getName().equals(contextData.get(IContextDataKeys.RECORD_NAME)) ||
 			   contextData.size() == 2 &&
 			   contextData.containsKey(IContextDataKeys.PROPERTY_NAME) &&
 			   record.getName().equals(contextData.get(IContextDataKeys.RECORD_NAME));		
 	}
 	
 	public boolean appliesTo(Set set) {
 		checkAppliesTo(set);
 		return contextData.size() == 1 && 
 			   set.getName().equals(contextData.get(IContextDataKeys.SET_NAME)) ||
 			   contextData.size() == 2 &&
 			   contextData.containsKey(IContextDataKeys.PROPERTY_NAME) &&
 			   set.getName().equals(contextData.get(IContextDataKeys.SET_NAME));		
 	}
 	
 	private void checkAppliesTo(EObject model) {
 		if (model == null) {
 			throw new IllegalArgumentException("Invalid model: null");
 		}
 		if (modelChangeType == ModelChangeType.SET_PROPERTY &&
 			!contextData.containsKey(IContextDataKeys.PROPERTY_NAME)) {
 			
 			throw new IllegalStateException("Feature name NOT found in context data");
 		}
 		if (contextData.containsKey(IContextDataKeys.PROPERTY_NAME) &&
 		    modelChangeType != ModelChangeType.SET_PROPERTY) {
 			
 			throw new IllegalStateException("Feature name should NOT be present in context data: " +
 											modelChangeType);
 		}		
 	}
 	
 	public ModelChangeContext copy() {
 		ModelChangeContext copy = new ModelChangeContext(modelChangeType);
 		copy.setSchema(schema);
 		for (String key : contextData.keySet()) {
 			copy.getContextData().put(key, contextData.get(key));
 		}
 		// don't copy the listener data and command execution mode
 		return copy;
 	}
 
 	public CommandExecutionMode getCommandExecutionMode() {
 		return commandExecutionMode;
 	}
 
 	public Map<String, String> getContextData() {
 		return contextData;
 	}
 
 	public Object getListenerData() {
 		return listenerData;
 	}
 
 	public ModelChangeType getModelChangeType() {
 		return modelChangeType;
 	}
 
 	public Schema getSchema() {
 		return schema;
 	}
 
 	public boolean isPropertySet(EStructuralFeature... features) {
 		if (modelChangeType != ModelChangeType.SET_PROPERTY) {
 			throw new IllegalStateException("Context has wrong model change type: " + 
 											modelChangeType + " (expected: " + 
 											ModelChangeType.SET_PROPERTY + ")");
 		}
 		String featureName = contextData.get(IContextDataKeys.PROPERTY_NAME);
 		if (featureName == null) {
 			throw new IllegalStateException("Context has no feature in its context data");
 		}
 		for (EStructuralFeature feature : features) {
 			if (featureName.equals(getQualifiedFeatureName(feature))) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void putContextData(ConnectionPart connectionPart) {
 		MemberRole memberRole = connectionPart.getMemberRole(); 
 		String setName = memberRole.getSet().getName();
 		String recordName = memberRole.getRecord().getName();
 		int connectionPartIndex = memberRole.getConnectionParts().indexOf(connectionPart);
 		contextData.put(IContextDataKeys.SET_NAME, setName);
 		contextData.put(IContextDataKeys.RECORD_NAME, recordName);
 		contextData.put(IContextDataKeys.CONNECTION_PART_INDEX, String.valueOf(connectionPartIndex));
 	}
 	
 	public void putContextData(Connector connector) {
 		MemberRole memberRole = connector.getConnectionPart().getMemberRole();
 		String setName = memberRole.getSet().getName();
 		String recordName = memberRole.getRecord().getName();
 		int connectionPartIndex = 
 			memberRole.getConnectionParts().indexOf(connector.getConnectionPart());
 		contextData.put(IContextDataKeys.SET_NAME, setName);
 		contextData.put(IContextDataKeys.RECORD_NAME, recordName);
 		contextData.put(IContextDataKeys.CONNECTION_PART_INDEX, String.valueOf(connectionPartIndex));
 	}
 	
 	public void putContextData(DiagramNode diagramNode) {
 		if (diagramNode instanceof ConnectionLabel) {
 			ConnectionLabel connectionLabel = (ConnectionLabel) diagramNode;
 			putContextData(connectionLabel.getMemberRole());
 		} else if (diagramNode instanceof Connector) {
 			Connector connector = (Connector) diagramNode;
 			putContextData(connector);
 		} else if (diagramNode instanceof DiagramLabel) {
 			// nothing to set
 		} else if (diagramNode instanceof SchemaRecord) {
 			SchemaRecord record = (SchemaRecord) diagramNode;
 			putContextData(record);
 		} else if (diagramNode instanceof SystemOwner) {
 			SystemOwner systemOwner = (SystemOwner) diagramNode;
 			putContextData(systemOwner.getSet());
 		} else if (diagramNode != null) {
 			throw new IllegalArgumentException("DiagramNode type invalid: " + 
 											   diagramNode.getClass().getName());
 		} else {
 			throw new IllegalArgumentException("DiagramNode type invalid: null");
 		}
 	}
 
 	public void putContextData(Guide guide) {
 		Ruler ruler = guide.getRuler();
 		DiagramData diagramData = ruler.getDiagramData();
 		int rulerIndex = diagramData.getRulers().indexOf(ruler);
 		int guideIndex = ruler.getGuides().indexOf(guide);
 		contextData.put(IContextDataKeys.RULER_INDEX, String.valueOf(rulerIndex));
 		contextData.put(IContextDataKeys.GUIDE_INDEX, String.valueOf(guideIndex));
 	}
 	
 	public void putContextData(MemberRole memberRole) {
 		String setName = memberRole.getSet().getName();
 		String recordName = memberRole.getRecord().getName();
 		contextData.put(IContextDataKeys.SET_NAME, setName);
 		contextData.put(IContextDataKeys.RECORD_NAME, recordName);
 	}
 	
 	public void putContextData(Ruler ruler) {
 		DiagramData diagramData = ruler.getDiagramData();
 		int rulerIndex = diagramData.getRulers().indexOf(ruler);
 		contextData.put(IContextDataKeys.RULER_INDEX, String.valueOf(rulerIndex));
 	}
 	
 	public void putContextData(SchemaArea area) {
 		String areaName = area.getName();
 		contextData.put(IContextDataKeys.AREA_NAME, areaName);
 	}
 	
 	public void putContextData(SchemaRecord record) {
 		contextData.put(IContextDataKeys.RECORD_NAME, record.getName());
 	}
 	
 	public void putContextData(Set set) {
 		contextData.put(IContextDataKeys.SET_NAME, set.getName());
 	}
 	
 	public void putContextData(EObject model, EStructuralFeature feature) {		
 		
 		if (modelChangeType != ModelChangeType.SET_PROPERTY) {
 			throw new IllegalStateException("Invalid model change type: " + modelChangeType);
 		}		
 		if (feature == null) {
 			throw new IllegalArgumentException("Invalid feature: null");
 		}
 		
 		String featureName = ModelChangeContext.getQualifiedFeatureName(feature);
 		contextData.put(IContextDataKeys.PROPERTY_NAME, featureName);
 		
		// the following is based on the types of defined attributes based properties sections
 		if (model instanceof SchemaArea) {
 			putContextData((SchemaArea) model); 
 		} else if (model instanceof DiagramLabel) {
 			// nothing to set
 		} else if (model instanceof DiagramData) {
 			// nothing to set
 		} else if (model instanceof SchemaRecord) {
 			putContextData((SchemaRecord) model);
 		} else if (model instanceof Schema) {
 			// nothing to set
 		} else if (model instanceof Set) {
 			putContextData((Set) model);
 		} else if (model == null){
 			throw new IllegalArgumentException("Model type invalid: null");
 		} else {
 			throw new IllegalArgumentException("Model type invalid: " + model.getClass().getName());
 		}
 	}
 
 	/**
 	 * This attribute should only be set by the model change dispatcher and NOT by the component 
 	 * that creates the initial context before handling a model change command to the command stack.
 	 * @param commandExecutionMode the command execution mode during which the context is passed to 
 	 *        model change listeners
 	 */
 	public void setCommandExecutionMode(CommandExecutionMode commandExecutionMode) {
 		this.commandExecutionMode = commandExecutionMode;
 	}
 
 	/**
 	 * This attribute should NOT be set by the component that creates the initial context before 
 	 * handling a model change command to the command stack.  It is intended <i>only</i> for model  
 	 * change listeners, that can add a 'listener object' to the context <i>before</i> the model 
 	 * change is carried out, which can then be retrieved <i>after</i> the model has been changed.
 	 * @param listenerData an object containing listener specific data 
 	 */
 	public void setListenerData(Object listenerData) {
 		this.listenerData = listenerData;
 	}
 	
 	public void setSchema(Schema schema) {
 		this.schema = schema;
 	}
 
 	public String toString() {
 		return "ModelChangeContext [modelChangeType=" + modelChangeType + 
 			   ", commandExecutionMode=" + commandExecutionMode + 
 			   ", contextData=" + contextData + 
 			   ", listenerData=" + listenerData + "]";
 	}
 	
 }
