 package org.iucn.sis.shared.api.schemes;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
 import org.iucn.sis.client.api.caches.AssessmentCache;
 import org.iucn.sis.client.api.caches.ReferenceCache;
 import org.iucn.sis.shared.api.acl.InsufficientRightsException;
 import org.iucn.sis.shared.api.citations.Referenceable;
 import org.iucn.sis.shared.api.data.TreeDataRow;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.Reference;
 import org.iucn.sis.shared.api.structures.ClassificationInfo;
 import org.iucn.sis.shared.api.structures.DisplayStructure;
 
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.extjs.client.WindowUtils;
 
 public class ClassificationSchemeModelData implements ModelData, Referenceable {
 	
 	private static final long serialVersionUID = 1L;
 	
 	protected final DisplayStructure structure;
 	protected final Map<String, Object> map;
 	
 	protected Set<Reference> references;
 	protected Field field;
 	
 	protected TreeDataRow selectedRow;
 	
 	public ClassificationSchemeModelData(DisplayStructure structure) {
 		this(structure, null);
 	}
 	
 	public ClassificationSchemeModelData(DisplayStructure structure, Field field) {
 		super();
 		this.field = field;
 		this.structure = structure;
 		this.map = new HashMap<String, Object>();
 		this.references = new HashSet<Reference>();
 		
 		if (field != null) {
 			if (field.getReference() != null)
 				references = field.getReference();
			
			if (structure.isPrimitive())
				structure.setData(field.getPrimitiveField(structure.getId()));
			else {
				if (structure.hasId())
					structure.setData(field.getField(structure.getId()));
				else
					structure.setData(field);
			}
 		}
 		
 		updateDisplayableData();
 	}
 	
 	@Override
 	public <X> X get(String property) {
 		return (X) map.get(property);
 	}
 	
 	@Override
 	public Map<String, Object> getProperties() {
 		return map;
 	}
 	
 	@Override
 	public Collection<String> getPropertyNames() {
 		return map.keySet();
 	}
 	
 	@Override
 	public <X> X remove(String property) {
 		return (X) map.remove(property);
 	}
 	
 	public <X extends Object> X set(String property, X value) {
 		return (X) map.put(property, value);
 	};
 	
 	public TreeDataRow getSelectedRow() {
 		return selectedRow;
 	}
 	
 	public void setSelectedRow(TreeDataRow selectedRow) {
 		this.selectedRow = selectedRow;
 		if (selectedRow != null)
 			set("text", selectedRow.getDescription());
 	}
 	
 	public void updateDisplayableData() {
 		if (structure != null) {
 			final ArrayList<String> raw = new ArrayList<String>(), 
 				pretty = new ArrayList<String>(), 
 				descs = new ArrayList<String>();
 			
 			for (Object obj : structure.getClassificationInfo()) {
 				ClassificationInfo info = (ClassificationInfo)obj;
 				set(info.getDescription(), info.getData());
 				raw.add(info.getData());
 				descs.add(info.getDescription());
 			}
 			
 			try {
 				structure.getDisplayableData(raw, pretty, 0);
 			} catch (Exception e) {
 				return;
 			}
 			
 			for (int i = 0; i < descs.size(); i++)
 				set(descs.get(i), pretty.get(i));
 		}
 	}
 	
 	public Widget getDetailsWidget(boolean isViewOnly) {
 		SimplePanel container = new SimplePanel();
 		if (!isViewOnly) {
 			Widget structWidget = structure.generate();
 			structWidget.addStyleName("leftMargin15");
 			container.setWidget(structWidget);
 		} else {
 			Widget structWidget = structure.generateViewOnly();
 			structWidget.addStyleName("leftMargin15");
 			container.setWidget(structWidget);
 		}
 		return container;
 	}
 	
 	public void save(Field parent, Field field) {
 		if (structure != null) {
 			if (structure.isPrimitive())
 				structure.save(field, field.getPrimitiveField(structure.getId()));
 			else {
 				if (structure.hasId())
 					structure.save(field, field.getField(structure.getId()));
 				else
 					structure.save(parent, field);
 			}
 			field.setReference(references);
 		}
 	}
 	
 	public Field getField() {
 		return field;
 	}
 	
 	public void setField(Field field) {
 		this.field = field;
 	}
 	
 	@Override
 	public void addReferences(ArrayList<Reference> references,
 			GenericCallback<Object> callback) {
 		this.references.addAll(references);
 		ReferenceCache.getInstance().addReferences(references);
 		if (field != null) {
 			ReferenceCache.getInstance().addReferencesToAssessmentAndSave(references, field, callback);
 		}
 		else
 			callback.onSuccess(null);
 	}
 	
 	@Override
 	public Set<Reference> getReferencesAsList() {
 		return references;
 	}
 	
 	@Override
 	public void removeReferences(ArrayList<Reference> references,
 			GenericCallback<Object> listener) {
 		this.references.removeAll(references);
 		if (field != null) {
 			try {
 				AssessmentClientSaveUtils.saveAssessment(listener);
 			} catch (InsufficientRightsException e) {
 				WindowUtils.errorAlert("Insufficient permissions", 
 					"You do not have permission to modify this " +
 					"assessment. The changes you made will not " +
 					"be saved."	
 				);
 			}
 		}
 		else
 			listener.onSuccess(null);
 	}
 	
 	@Override
 	public void onReferenceChanged(GenericCallback<Object> callback) {
 		/*
 		 * I don't think I care, I just want the ID, and 
 		 * that hasn't changed...
 		 */
 	}
 
 }
