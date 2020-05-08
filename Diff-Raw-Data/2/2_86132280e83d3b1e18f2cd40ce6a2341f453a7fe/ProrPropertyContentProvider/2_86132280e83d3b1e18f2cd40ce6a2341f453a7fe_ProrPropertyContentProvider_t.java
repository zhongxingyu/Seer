 /*******************************************************************************
  * Copyright (c) 2014 Formal Mind GmbH, University of Duesseldorf and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Michael Jastram - initial API and implementation
  *******************************************************************************/
 package org.eclipse.rmf.reqif10.pror.editor.propertiesview;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.agilemore.agilegrid.AbstractContentProvider;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor.PropertyValueWrapper;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 import org.eclipse.rmf.reqif10.AttributeDefinition;
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.rmf.reqif10.ReqIF10Package;
 import org.eclipse.rmf.reqif10.SpecElementWithAttributes;
 import org.eclipse.rmf.reqif10.SpecHierarchy;
 import org.eclipse.rmf.reqif10.common.util.ReqIF10Util;
 import org.eclipse.rmf.reqif10.pror.util.ProrUtil;
 
 /**
  * The agile grid content provider for the properties view. Internally, it
  * manages a List of PropertyRows. These represent Categories and Descriptors
  * (for AttributeValues and EMF Values)
  * 
  * @author Lukas Ladenberger
  * @author Michael Jastram
  * 
  */
 public class ProrPropertyContentProvider extends AbstractContentProvider {
 
 	// Special categories that should be ordered differently
 	public static String SPEC_HIERARCHY_NAME = "Spec Hierarchy";
 	public static String SPEC_OBJECT_NAME = "Spec Object";
 	public static String SPEC_RELATION_NAME = "Spec Relation";
 	public static String SPECIFICATION_NAME = "Specification";
 	public static String RELATION_GROUP_NAME = "Relation Group";
 
 	// Only access via getRows(), to ensure it's not null.
 	private List<PropertyRow> rows;
 
 	// The Object whose properties are shown.
 	private Object content;
 
 	// Whether to show all properties or not
 	private boolean showAllProps;
 
 	private AdapterFactory adapterFactory;
 
 	public ProrPropertyContentProvider(AdapterFactory adapterFactory,
 			boolean showAllProps) {
 		this.adapterFactory = adapterFactory;
 		this.showAllProps = showAllProps;
 	}
 
 	@Override
 	public Object doGetContentAt(int row, int col) {
 		return getRows().get(row).getContent(col);
 	}
 
 	/**
 	 * This is the object for which this contentProvider manges the properties.
 	 * It is either a {@link SpecElementWithAttributes} or a
 	 * {@link SpecHierarchy}.
 	 */
 	Object getElement() {
 		return content;
 	}
 
 	@Override
 	public void doSetContentAt(int row, int col, Object value) {
 		// We don't need this method, cause an automatic refresh of the agile
 		// grid after editing a cell
 		// Fix of 378041:
 		// needed to inform ProrPropertyControl of property change.
 		// firePropertyChange is never called from
 		// AbstractContentProvider.setContentAt(..)
 		// because oldValue is equal to newValue (value already updated
 		// somewhere else?)
 		super.firePropertyChange("", null, null);
 	}
 
 	public PropertyRow getRowContent(int row) {
 		if (row >= getRowCount())
 			return null;
 		return getRows().get(row);
 	}
 
 	public IItemLabelProvider getItemLabelProvider(int row) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void setContent(Object content) {
 		this.content = content;
 		rows = null;
 	}
 
 	public int getRowCount() {
 		return getRows().size();
 	}
 
 	/**
 	 * Lazily builds a list of {@link PropertyRow}s, representing either
 	 * {@link Category}s or {@link PropertyRow}s. These are properly ordered.
 	 * 
 	 * @return
 	 */
 	private List<PropertyRow> getRows() {
 		// Use cached version if it exists.
 		if (rows != null)
 			return rows;
 
 		// We get the provider, which may be null.
 		rows = new ArrayList<PropertyRow>();
 		ItemProviderAdapter provider = ProrUtil.getItemProvider(adapterFactory,
 				content);
 		if (provider == null)
 			return rows;
 
 		// To ensure ordering by category and then alphabetically, we build a
 		// map of sets.
 		// As Descriptors are Comparable, they are properly ordered.
 		TreeMap<String, TreeSet<Descriptor>> categoryMap = new TreeMap<String, TreeSet<Descriptor>>();
 		for (IItemPropertyDescriptor prop : provider
 				.getPropertyDescriptors(content)) {
 
 			if (!showAllProps && isAdvancedProperty(prop))
 				continue;
 
 			String categoryName = prop.getCategory(content);
 			if (categoryName == null) {
 				categoryName = "Misc";
 			}
 			TreeSet<Descriptor> categorySet = categoryMap.get(categoryName);
 			if (categorySet == null) {
 				categorySet = new TreeSet<Descriptor>();
 				categoryMap.put(categoryName, categorySet);
 			}
 			categorySet.add(new Descriptor(prop));
 		}
 
 		// To ensure user-relevant ordering, we add the following categories in
 		// this order...
 		addCategoryAndRemoveFromMap(categoryMap, SPEC_OBJECT_NAME);
 		addCategoryAndRemoveFromMap(categoryMap, SPEC_RELATION_NAME);
 		addCategoryAndRemoveFromMap(categoryMap, SPEC_HIERARCHY_NAME);
 		addCategoryAndRemoveFromMap(categoryMap, SPECIFICATION_NAME);
 		addCategoryAndRemoveFromMap(categoryMap, RELATION_GROUP_NAME);
 
 		// ... and insert all other categories before.
 		for (String categoryName : categoryMap.keySet()) {
 			rows.add(0, new Category(categoryName));
 			rows.addAll(1, categoryMap.get(categoryName));
 		}
 
 		return rows;
 	}
 
 	private void addCategoryAndRemoveFromMap(
 			TreeMap<String, TreeSet<Descriptor>> categoryMap,
 			String categoryName) {
 		if (categoryMap.containsKey(categoryName)) {
 			rows.add(new Category(categoryName));
 			rows.addAll(categoryMap.get(categoryName));
 			categoryMap.remove(categoryName);
 		}
 	}
 
 	/**
 	 * Return true if this is one of the special properties that should only be
 	 * shown on the advanced property panel.
 	 * 
 	 * @param prop
 	 */
 	private boolean isAdvancedProperty(IItemPropertyDescriptor prop) {
 		String name = prop.getId(content);
 		String category = prop.getCategory(content);
 
 		// Special case: Datatype Dialog
 		if (category == null && "identifier".equals(name)
 				|| "desc".equals(name) || "lastChange".equals(name)) {
 			return true;
 		}
 
 		// Only hide properties that belong to the standard categories
 		if (!(SPEC_HIERARCHY_NAME.equals(category)
 				|| SPEC_RELATION_NAME.equals(category)
 				|| SPEC_OBJECT_NAME.equals(category)
 				|| SPECIFICATION_NAME.equals(category) || RELATION_GROUP_NAME
 					.equals(category))) {
 			return false;
 		}
 
 		// These are the attributes that shall be hidden.
 		if ("identifier".equals(name) || "desc".equals(name)
 				|| "lastChange".equals(name) || "editable".equals(name)
 				|| "longName".equals(name) || "tableInternal".equals(name)
 				|| "object".equals(name) || "editableAtts".equals(name)) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Three implementations of this interface are provided to represent the
 	 * rows of the Property View.
 	 */
 	interface PropertyRow extends Comparable<PropertyRow> {
 		Object getContent(int column);
 	}
 
 	/**
 	 * Rows representing a Category
 	 */
 	class Category implements PropertyRow {
 		String name;
 
 		public Category(String name) {
 			this.name = name;
 		}
 
 		public Object getContent(int column) {
 			return column == 0 ? name : null;
 		}
 
 		public int compareTo(PropertyRow that) {
 			return name.compareTo(((Category) that).name);
 		}
 
 		@Override
 		public boolean equals(Object that) {
 			if (!(that instanceof Category))
 				return false;
 			return compareTo((PropertyRow) that) == 0;
 		}
 
 		@Override
 		public int hashCode() {
 			return name.hashCode();
 		}
 	}
 
 	/**
 	 * Rows representing an {@link IItemPropertyDescriptor}. This can one that
 	 * is RMF-Specific or EMF-Specific.
 	 */
 	class Descriptor implements PropertyRow {
 		IItemPropertyDescriptor descriptor;
 		AttributeValue attributeValue;
 
 		public Descriptor(IItemPropertyDescriptor descriptor) {
 			this.descriptor = descriptor;
 		}
 
 		public IItemPropertyDescriptor getItemPropertyDescriptor() {
 			return descriptor;
 		}
 
 		public boolean isRMFSpecific() {
 			Object feature = descriptor.getFeature(content);
 			return feature == ReqIF10Package.Literals.SPEC_ELEMENT_WITH_ATTRIBUTES__VALUES
 					|| descriptor.getFeature(content) == ReqIF10Package.Literals.ATTRIBUTE_VALUE_BOOLEAN__THE_VALUE
 					|| descriptor.getFeature(content) == ReqIF10Package.Literals.ATTRIBUTE_VALUE_DATE__THE_VALUE
 					|| descriptor.getFeature(content) == ReqIF10Package.Literals.ATTRIBUTE_VALUE_ENUMERATION__VALUES
 					|| descriptor.getFeature(content) == ReqIF10Package.Literals.ATTRIBUTE_VALUE_INTEGER__THE_VALUE
 					|| descriptor.getFeature(content) == ReqIF10Package.Literals.ATTRIBUTE_VALUE_REAL__THE_VALUE
 					|| descriptor.getFeature(content) == ReqIF10Package.Literals.ATTRIBUTE_VALUE_STRING__THE_VALUE
 					|| descriptor.getFeature(content) == ReqIF10Package.Literals.ATTRIBUTE_VALUE_XHTML__THE_VALUE;
 		}
 
 		public Object getContent(int column) {
 			return column == 0 ? descriptor.getDisplayName(content)
 					: descriptor.getPropertyValue(content);
 		}
 
 		public int compareTo(PropertyRow that) {
 			return descriptor.getDisplayName(content).compareTo(
 					((Descriptor) that).descriptor.getDisplayName(content));
 		}
 
 		/**
 		 * Returns the string content from the descriptor.  However, we have to extract
 		 * the string differently for {@link GregorianCalendar}.
 		 */
 		@Override
 		public String toString() {
 			PropertyValueWrapper propertyValueWrapper = (PropertyValueWrapper) descriptor
 					.getPropertyValue(content);
 			if (propertyValueWrapper != null) {
 				Object editableValue = propertyValueWrapper.getEditableValue(content);
 				if (editableValue instanceof GregorianCalendar) {
 					GregorianCalendar cal = (GregorianCalendar) editableValue;
 					Date date = cal.getTime();
 					return DateFormat.getDateTimeInstance().format(date);
 				} else {
 					return propertyValueWrapper
 							.getText(content);
 				}
 			}
 			return "";
 		}
 
 		@Override
 		public boolean equals(Object that) {
 			if (!(that instanceof Descriptor))
 				return false;
 			return compareTo((Descriptor) that) == 0;
 		}
 
 		@Override
 		public int hashCode() {
 			return descriptor.hashCode();
 		}
 
 		/**
 		 * It is quite possible that the AttributeValue does not exist yet. In
 		 * that case, a new AttributeValue is created and returned, but not
 		 * connected to the parent object (i.e. EObject#eContainer() returns
 		 * null).
 		 */
 		public AttributeValue getAttributeValue() {
 			if (!isRMFSpecific())
 				return null;
 
 			// Find the SpecElement
 			SpecElementWithAttributes specElement = null;
 			if (content instanceof SpecElementWithAttributes) {
 				specElement = (SpecElementWithAttributes) content;
 			} else if (content instanceof SpecHierarchy) {
 				specElement = ((SpecHierarchy) content).getObject();
			} else if (content instanceof AttributeValue) {
				return (AttributeValue) content;
 			}
 			
 			// Could also be a default value
 			if (specElement == null) {
 				if (content instanceof AttributeDefinition) {
 					AttributeDefinition ad = (AttributeDefinition) content;
 					EStructuralFeature defaultValueFeature = ReqIF10Util.getDefaultValueFeature(ad);
 					return (AttributeValue) ((AttributeDefinition) content).eGet(defaultValueFeature);
 				}
 			}
 
 			return ReqIF10Util.getAttributeValueForLabel(specElement,
 					descriptor.getDisplayName(specElement));
 		}
 	}
 }
