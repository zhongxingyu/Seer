 package org.romaframework.aspect.view.html.transformer.freemarker;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Set;
 
 import org.romaframework.aspect.i18n.I18NType;
 import org.romaframework.aspect.view.ViewConstants;
 import org.romaframework.aspect.view.feature.ViewActionFeatures;
 import org.romaframework.aspect.view.feature.ViewFieldFeatures;
 import org.romaframework.core.Roma;
 import org.romaframework.core.schema.SchemaAction;
 import org.romaframework.core.schema.SchemaClass;
 import org.romaframework.core.schema.SchemaClassDefinition;
 import org.romaframework.core.schema.SchemaClassElement;
 import org.romaframework.core.schema.SchemaElement;
 import org.romaframework.core.schema.SchemaField;
 
 public class TableDriver {
 
 	// LEVEL ONE
	private Set<SchemaClass>					runtimeClasses		= new LinkedHashSet<SchemaClass>();
 	private Set<SchemaField>					fields						= new LinkedHashSet<SchemaField>();
 	private Set<SchemaAction>					actions						= new LinkedHashSet<SchemaAction>();
 
 	// LEVEL TWO
 	private List<SchemaClassElement>	expandedElements	= new ArrayList<SchemaClassElement>();
 
 	public TableDriver(SchemaClass baseClass, final Collection<Object> implementedObjects) {
 		initClasses(baseClass, implementedObjects);
 		initFields();
 		initActions();
 		initExpandedElements();
 	}
 
 	private void initClasses(SchemaClass baseClass, final Collection<Object> implementedObjects) {
 		if (baseClass != null)
 			runtimeClasses.add(baseClass);
 		for (Object element : implementedObjects) {
			SchemaClass implementedClass = Roma.schema().getSchemaClass(element);
 			if (implementedClass != null)
 				runtimeClasses.add(implementedClass);
 		}
 	}
 
 	private void initFields() {
		for (SchemaClass clazz : runtimeClasses) {
 			Iterator<SchemaField> fieldIterator = clazz.getFieldIterator();
 			while (fieldIterator.hasNext()) {
 				SchemaField field = fieldIterator.next();
 				if (isNewField(field)) {
 					fields.add(field);
 				}
 			}
 		}
 	}
 
 	private boolean isNewField(SchemaField target) {
 		for (SchemaField field : fields) {
 			if (field.getName().equals(target.getName()))
 				return false;
 		}
 		return true;
 	}
 
 	private void initActions() {
		for (SchemaClass clazz : runtimeClasses) {
 			Iterator<SchemaAction> actionIterator = clazz.getActionIterator();
 			while (actionIterator.hasNext()) {
 				SchemaAction action = actionIterator.next();
 				if (isNewAction(action)) {
 					actions.add(action);
 				}
 			}
 		}
 	}
 
 	private boolean isNewAction(SchemaAction target) {
 		for (SchemaAction action : actions) {
 			if (action.getName().equals(target.getName()))
 				return false;
 		}
 		return true;
 	}
 
 	private void initExpandedElements() {
 		for (SchemaField field : fields) {
 			if (LayoutExpandHelper.isVisible(field))
 				expandedElements.add(field);
 		}
 		for (SchemaAction action : actions) {
 			if (LayoutExpandHelper.isVisible(action))
 				expandedElements.add(action);
 		}
 		while (!isExpanded())
 			;
 	}
 
 	private boolean isExpanded() {
 		boolean allexpanded = true;
 		ListIterator<SchemaClassElement> iterator = expandedElements.listIterator();
 
 		while (iterator.hasNext()) {
 			SchemaClassElement element = iterator.next();
 			if (LayoutExpandHelper.isLayoutExpand(element)) {
 				allexpanded = false;
 				iterator.remove();
 				List<SchemaClassElement> elements = LayoutExpandHelper.expand((SchemaField) element);
 				for (SchemaClassElement subelement : elements) {
 					if (isAddable(subelement)) {
 						iterator.add(subelement);
 					}
 				}
 			}
 		}
 
 		return allexpanded;
 	}
 
 	private boolean isAddable(SchemaClassElement target) {
 		if (LayoutExpandHelper.isVisible(target)) {
 			if (target instanceof SchemaField) {
 				return isNewFieldInElements((SchemaField) target);
 			}
 			if (target instanceof SchemaAction) {
 				return isNewActionInElements((SchemaAction) target);
 			}
 		}
 		return false;
 	}
 
 	private boolean isNewFieldInElements(SchemaField target) {
 		for (SchemaElement element : expandedElements) {
 			if (element instanceof SchemaField) {
 				if (element.getName().equals(target.getName()))
 					return false;
 			}
 		}
 		return true;
 	}
 
 	private boolean isNewActionInElements(SchemaAction target) {
 		for (SchemaElement element : expandedElements) {
 			if (element instanceof SchemaField) {
 				if (element.getName().equals(target.getName()))
 					return false;
 			}
 		}
 		return true;
 	}
 
	protected Set<SchemaClass> getRuntimeClasses() {
 		return runtimeClasses;
 	}
 
 	protected Set<SchemaField> getFields() {
 		return fields;
 	}
 
 	protected Set<SchemaAction> getActions() {
 		return actions;
 	}
 
 	public List<SchemaClassElement> getExpandedElements() {
 		return expandedElements;
 	}
 
 	public List<String> getLabels() {
 		List<String> labels = new ArrayList<String>();
 		for (SchemaClassElement element : expandedElements) {
 			if (element instanceof SchemaField) {
 				labels.add(Roma.i18n().get(element, I18NType.LABEL, ViewFieldFeatures.LABEL));
 			} else if (element instanceof SchemaAction) {
 				labels.add(Roma.i18n().get(element, I18NType.LABEL, ViewActionFeatures.LABEL));
 			}
 		}
 		return labels;
 	}
 
 	public List<String> getRawName() {
 		List<String> labels = new ArrayList<String>();
 		for (SchemaClassElement element : expandedElements) {
 			labels.add(element.getName());
 		}
 		return labels;
 	}
 
 	public List<String> getFieldNames() {
 		List<String> fieldNames = new ArrayList<String>();
 		for (SchemaField field : fields) {
 			addFieldName(fieldNames, field);
 		}
 		return fieldNames;
 	}
 
 	public List<String> getActionNames() {
 		List<String> actionNames = new ArrayList<String>();
 		for (SchemaAction action : actions) {
 			addActionName(actionNames, action);
 		}
 		for (SchemaField field : fields) {
 			addActionName(actionNames, field);
 		}
 		return actionNames;
 	}
 
 	private void addFieldName(List<String> fieldNames, SchemaField field) {
 		Object visible = field.getFeature(ViewFieldFeatures.VISIBLE);
 		Object layout = field.getFeature(ViewFieldFeatures.POSITION);
 		if (Boolean.TRUE.equals(visible)) {
 			if (ViewConstants.LAYOUT_EXPAND.equals(layout)) {
 				fieldNames.addAll(getFieldNames(field));
 			} else {
 				fieldNames.add(field.getName());
 			}
 		}
 	}
 
 	private void addActionName(List<String> actionNames, SchemaAction action) {
 		Object visible = action.getFeature(ViewActionFeatures.VISIBLE);
 		if (Boolean.TRUE.equals(visible))
 			actionNames.add(action.getName());
 	}
 
 	private void addActionName(List<String> actionNames, SchemaField field) {
 		Object visible = field.getFeature(ViewFieldFeatures.VISIBLE);
 		Object layout = field.getFeature(ViewFieldFeatures.POSITION);
 		if (Boolean.TRUE.equals(visible)) {
 			if (ViewConstants.LAYOUT_EXPAND.equals(layout)) {
 				actionNames.addAll(getActionNames(field));
 			}
 		}
 	}
 
 	private List<String> getFieldNames(SchemaField layoutExpandField) {
 		List<String> fieldNames = new ArrayList<String>();
 		SchemaClassDefinition entityType = layoutExpandField.getType();
 		Iterator<SchemaField> fieldIterator = entityType.getFieldIterator();
 		while (fieldIterator.hasNext()) {
 			SchemaField field = fieldIterator.next();
 			addFieldName(fieldNames, field);
 		}
 		return fieldNames;
 	}
 
 	private List<String> getActionNames(SchemaField layoutExpandField) {
 		List<String> actionNames = new ArrayList<String>();
 		SchemaClassDefinition entityType = layoutExpandField.getType();
 		Iterator<SchemaAction> actionIterator = entityType.getActionIterator();
 		Iterator<SchemaField> fieldsIterator = entityType.getFieldIterator();
 		while (actionIterator.hasNext()) {
 			SchemaAction action = actionIterator.next();
 			addActionName(actionNames, action);
 		}
 		while (fieldsIterator.hasNext()) {
 			SchemaField field = fieldsIterator.next();
 			addActionName(actionNames, field);
 		}
 		return actionNames;
 	}
 
 }
