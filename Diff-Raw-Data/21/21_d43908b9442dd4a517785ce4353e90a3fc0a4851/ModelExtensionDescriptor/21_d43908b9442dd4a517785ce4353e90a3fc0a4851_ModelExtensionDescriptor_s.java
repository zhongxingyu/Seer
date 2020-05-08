 /*******************************************************************************
  * Copyright (c) 2011, 2012 Red Hat, Inc.
  *  All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Red Hat, Inc. - initial API and implementation
  *
  * @author Bob Brodt
  ******************************************************************************/
 
 package org.eclipse.bpmn2.modeler.core.runtime;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.bpmn2.Bpmn2Package;
 import org.eclipse.bpmn2.ExtensionAttributeValue;
 import org.eclipse.bpmn2.modeler.core.adapters.AdapterUtil;
 import org.eclipse.bpmn2.modeler.core.adapters.ExtendedPropertiesAdapter;
 import org.eclipse.bpmn2.modeler.core.model.Bpmn2ModelerFactory;
 import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl.SimpleFeatureMapEntry;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.FeatureMap;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * @author Bob Brodt
  *
  */
 public class ModelExtensionDescriptor extends BaseRuntimeDescriptor {
 
 	// Container class for property values
 	public static class Value {
 		
 		static int ID = 0;
 		String id;
 		public List<Object>values;
 		
 		public Value() {
 			setDefaultId();
 		}
 		
 		public Value(String id) {
 			if (id==null || id.isEmpty())
 				setDefaultId();
 			else
 				this.id = id;
 		}
 		
 		public List<Object> getValues() {
 			if (values==null) {
 				values = new ArrayList<Object>();
 			}
 			return values;
 		}
 		
 		private void setDefaultId() {
 			id = "V-" + ID++; //$NON-NLS-1$
 		}
 	}
 	
 	// name/value pairs constructed from Custom Task extension point
 	public static class Property {
 		public String name;
 		public String description;
 		public List<Object>values;
 		public String ref;
 		public String type;
 		
 		public Property() {
 			this.name = "unknown"; //$NON-NLS-1$
 		}
 		
 		public Property(String name, String description) {
 			super();
 			this.name = name;
 			this.description = description;
 		}
 		
 		public List<Object> getValues() {
 			if (values==null) {
 				values = new ArrayList<Object>();
 			}
 			return values;
 		}
 
 		public String getFirstStringValue() {
 
 			if (!this.getValues().isEmpty()) {
 				// simple attribute - find a String value for it
 				for (Object propValue : this.getValues()) {
 					if (propValue instanceof String) {
 						return (String)propValue;
 					}
 					else if (propValue instanceof Property) {
 						String s = ((Property)propValue).getFirstStringValue();
 						if (s!=null)
 							return s;
 					}
 				}
 			}
 			return null;
 		}
 	}
 	
 	public class ModelExtensionAdapter extends AdapterImpl {
 
 		ModelExtensionDescriptor descriptor;
 		
 		public ModelExtensionAdapter(ModelExtensionDescriptor descriptor) {
 			super();
 			this.descriptor = descriptor;
 		}
 		
 		public Property getProperty(String name) {
 			return descriptor.getProperty(name);
 		}
 		
 		public List<Property> getProperties(String path) {
 			return descriptor.getProperties(path);
 		}
 		
 		public ModelExtensionDescriptor getDescriptor() {
 			return descriptor;
 		}
 	}
 	
 	protected String id;
 	protected String name;
 	protected String type;
 	protected String description;
 	protected List<Property> properties = new ArrayList<Property>();
 	protected Resource containingResource;
 	protected EObject modelObject;
 
 	public ModelExtensionDescriptor(String id, String name) {
 		this.id = id;
 		this.name = name;
 	}
 	
 	public String getId() {
 		return id;
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	public String getType() {
 		return type;
 	}
 	
 	public String getDescription() {
 		return description;
 	}
 	
 	public List<Property> getProperties() {
 		return properties;
 	}
 
 	
 	/**
 	 * Creates a custom Task object from a definition in the currently selected
 	 * Target Runtime plugin's "modelExtension" extension point.
 	 * 
 	 * @param container - the EObject which will eventually contain the new Task.
 	 *                    No changes are made to this object, it is only used to
 	 *                    locate the EMF Resource which will eventually contain
 	 *                    the new Task object.
 	 * @return an initialized Task object
 	 */
 	public EObject createObject(EObject container) {
 		if (container!=null)
 			containingResource = container.eResource();
 		modelObject = createObject(getType());
 		populateObject(modelObject, true);
 		return modelObject;
 	}
 	
 	/**
 	 * Create and initialize an object of the given EClass name.
 	 * The runtime's EPackage is searched first for the given ECLass; if not
 	 * found, the Bpmn2Package is searched.
 	 * 
 	 * @param className
 	 * @return an initialized EObject or null if the EClass name was not found
 	 */
 	private EObject createObject(String className) {
 		// look in the extension model package for the class name first
 		EClass eClass = (EClass) getEPackage().getEClassifier(className);
 		if (eClass==null) {
 			// if not found, look in BPMN2 package
 			eClass = (EClass) Bpmn2Package.eINSTANCE.getEClassifier(className);
 		}
 		if (eClass!=null)
 			return createObject(eClass);
 		return null;
 	}
 
 	/**
 	 * Create and initialize an object of the given EClass. Initialization consists
 	 * of assigning an ID and setting a default name if the EClass has those features.
 	 * 
 	 * @param eClass - type of object to create
 	 * @return an initialized EObject
 	 */
 	private EObject createObject(EClass eClass) {
 		EObject eObject = null;
 		EPackage pkg = eClass.getEPackage();
 		if (pkg==Bpmn2Package.eINSTANCE) {
 			eObject = Bpmn2ModelerFactory.create(containingResource, eClass);
 		}
 		else {
 			eObject = pkg.getEFactoryInstance().create(eClass);
 		}
 		
 		// if the object has an "id", assign it now.
 		String id = ModelUtil.setID(eObject,containingResource);
 		// also set a default name
 		EStructuralFeature feature = eObject.eClass().getEStructuralFeature("name"); //$NON-NLS-1$
 		if (feature!=null) {
 			if (id!=null)
 				eObject.eSet(feature, ModelUtil.toDisplayName(id));
 			else
 				eObject.eSet(feature, NLS.bind(Messages.ModelExtensionDescriptor_Create,
 					ModelUtil.toDisplayName(eObject.eClass().getName())));
 		}
 
 		return eObject;
 	}
 	
 	/**
 	 * Search the Target Runtime's EPackage for a structural feature with the specified name.
 	 * If the feature is not found in the runtime package, search the Bpmn2Package.
 	 * 
 	 * @param type - the feature type, either EAttribute or EReference
 	 * @param name - name of the feature
 	 * @return an EStructuralFeature or null if not found
 	 */
 	private EStructuralFeature getFeature(Class type, String name) {
 		EStructuralFeature feature = getFeature(getEPackage(), type, name);
 		if (feature==null) {
 			// try the bpmn2 package
 			feature = getFeature(Bpmn2Package.eINSTANCE, type, name);
 		}
 		return feature;
 	}
 	
 	/**
 	 * Search the given EPackage for a structural feature with the specified name.
 	 * 
 	 * @param pkg - the EPackage to search
 	 * @param type - the feature type, either EAttribute or EReference
 	 * @param name - name of the feature
 	 * @return an EStructuralFeature or null if not found
 	 */
 	private EStructuralFeature getFeature(EPackage pkg, Class type, String name) {
 		TreeIterator<EObject> it = pkg.eAllContents();
 		while (it.hasNext()) {
 			EObject o = it.next();
 			if (type.isInstance(o)) {
 				EStructuralFeature fName = o.eClass().getEStructuralFeature("name"); //$NON-NLS-1$
 				if (fName!=null && o.eGet(fName)!=null && o.eGet(fName).equals(name)) {
 					return (EStructuralFeature)o;
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Populate the given EObject with a list of values which must be Property objects.
 	 * 
 	 * @param object - the object to initialize
 	 * @param values - list of Property values
 	 */
 	public void populateObjectFromValues(EObject object, List<Object> values, boolean all) {
 		
 		for (Object value : values) {
 			if (value instanceof Property)
 				populateObject(object,(Property)value, all);
 		}
 	}
 	
 	public EObject populateObject(EObject object, Resource resource, boolean all) {
 		containingResource = resource;
 		modelObject = object;
 		populateObject(modelObject, all);
 		return modelObject;
 	}
 
 	public void populateObject(EObject object, boolean all) {
 		modelObject = object;
 		populateObject(object, getProperties(), all);
 		adaptObject(object);
 	}
 	
 	/**
 	 * Populate the given EObject with a list of Property objects.
 	 * 
 	 * @param object - the object to initialize
 	 * @param values - list of Property objects
 	 */
 	public void populateObject(EObject object, List<Property> properties, boolean all) {
 		
 		for (Property prop : properties) {
 			populateObject(object,prop, all);
 		}
 	}
 	
 	/**
 	 * Set the value of the structural feature. If the feature is a list,
 	 * the value is added to the list.
 	 * 
 	 * @param object
 	 * @param feature
 	 * @param value
 	 */
 	private void setValue(EObject object, EStructuralFeature feature, Object value, boolean force, Property property) {
 		this.setValueFromString(object, feature, value, force);
 	}
 	
 	/**
 	 * Return the value of the specified feature from the given EObject.
 	 * If the feature is a list, return the indexed value.
 	 *  
 	 * @param object
 	 * @param feature
 	 * @param index
 	 * @return the feature's value
 	 */
 	private Object getValue(EObject object, EStructuralFeature feature, int index) {
 		if (feature.isMany()) {
 			return ((EList)object.eGet(feature)).get(index<0 ? 0 : index);
 		}
 		return object.eGet(feature);
 	}
 	
 	public EStructuralFeature getFeature(EClass eclass, Property property) {
 		EStructuralFeature feature = eclass.getEStructuralFeature(property.name);
 
 		Object firstValue = property.getValues().isEmpty() ? null : property.getValues().get(0);
 
 		if (feature==null) {
 			EPackage pkg = getEPackage();
 			// if the Property has a "ref" or if its value is a Property
 			// then this must be an EReference
 			if (property.ref!=null || firstValue instanceof Property) {
 				feature = ModelUtil.createDynamicReference(pkg, eclass, property.name, property.type);
 			}
 			else
 				feature = ModelUtil.createDynamicAttribute(pkg, eclass, property.name, property.type);
 		}
 		return feature;
 	}
 
 	/**
 	 * Populate the given EObject from the Property tree defined in this runtime
 	 * plugin's "modelObject" extension point.
 	 * 
 	 * @param object
 	 * @param property
 	 */
 	public EStructuralFeature populateObject(EObject object, Property property, boolean all) {
 
 		EObject childObject = null;
 		EStructuralFeature childFeature = null;
 		EStructuralFeature feature = object.eClass().getEStructuralFeature(property.name);
 
 		Object firstValue = property.getValues().isEmpty() ? null : property.getValues().get(0);
 
 		if (feature==null) {
 			EPackage pkg = getEPackage();
			if (pkg==Bpmn2Package.eINSTANCE) {
				feature = ModelUtil.getAnyAttribute(object, property.name);
				if (feature==null) {
					feature = ModelUtil.addAnyAttribute(object, "http://org.eclipse.bpmn2/extensions", property.name, property.type, "");
				}
			}
			else {
				// if the Property has a "ref" or if its value is a Property
				// then this must be an EReference
				if (property.ref!=null || firstValue instanceof Property) {
					feature = ModelUtil.createDynamicReference(pkg, object, property.name, property.type);
				}
				else
					feature = ModelUtil.createDynamicAttribute(pkg, object, property.name, property.type);
 			}
 		}
 		
 		if (feature instanceof EAttribute) {
 			if ( all && feature.getEType().getInstanceClass() == FeatureMap.Entry.class ) {
 				// special handling for FeatureMaps
 				for (Object value : property.getValues()) {
 					if (value instanceof Property) {
 						Property p = (Property)value;
 						childFeature = getFeature(p.name);
 						if (childFeature instanceof EAttribute) {
 							childObject = createObject(((EReference) childFeature));
 							setValue(childObject, childFeature, firstValue, true, property);
 						}
 						else if (childFeature instanceof EReference) {
 							childObject = createObject(((EReference) childFeature).getEReferenceType());
 							FeatureMap.Entry entry = new SimpleFeatureMapEntry((EStructuralFeature.Internal)childFeature, childObject);
 							setValue(object, feature, entry, true, property);
 							populateObjectFromValues(childObject,p.getValues(), true);
 						}
 					}
 				}
 			}
 			else
 				setValue(object, feature, firstValue, all, property);
 		}
 		else if (feature instanceof EReference) {
 			if (all) {
 				EReference ref = (EReference)feature;
 				if (property.ref!=null) {
 					// navigate down the newly created custom task to find the object reference
 					childObject = modelObject;
 					String[] segments = property.ref.split("/"); //$NON-NLS-1$
 					for (String s : segments) {
 						// is the feature an Elist?
 						int index = s.indexOf('#');
 						if (index>0) {
 							index = Integer.parseInt(s.substring(index+1));
 							s = s.split("#")[0]; //$NON-NLS-1$
 						}
 						childFeature = childObject.eClass().getEStructuralFeature(s);
 						childObject = (EObject)getValue(childObject, childFeature, index);
 					}
 					setValue(object, feature, childObject, true, property);
 				}
 				else if (firstValue instanceof Property)
 				{
                     EClassifier reftype = property.type == null || property.type.length() == 0 ? null : ModelUtil
                             .getEClassifierFromString(getEPackage(), property.type);
                     if (reftype == null || !(reftype instanceof EClass)) {
                         reftype = ref.getEReferenceType();
                     }
                     childObject = createObject((EClass) reftype);
                     setValue(object, feature, childObject, true, property);
                     populateObjectFromValues(childObject, property.getValues(), all);
 				}
 			}
 		}
 		return feature;
 	}
 
 	/**
 	 * Return the value of the given root Property name.
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public Object getPropertyValue(String name) {
 
 		for (Property prop : getProperties()) {
 			if (prop.name.equals(name)) {
 				if (!prop.getValues().isEmpty()) {
 					return prop.getValues().get(0);
 				}
 			}
 		}
 		return null;
 	}
 	
 	public List<Property> getProperties(String path) {
 		List<Property> result = new ArrayList<Property>();
 		List<Property> props = new ArrayList<Property>();
 		props.addAll(getProperties());
 		String names[] = path.split("/"); //$NON-NLS-1$
 		getProperties(props,names,0,result);
 		return result;
 	}
 	
 	private void getProperties(List<Property>props, String names[], int index, List<Property>result) {
 		String name = names[index];
 		for (Property p : props) {
 			if (p.name.equals(name)) {
 				if (index==names.length-1)
 					result.add(p);
 				else {
 					List<Property>childProps = new ArrayList<Property>();
 					for (Object v : p.values) {
 						if (v instanceof Property) {
 							childProps.add((Property)v);
 						}
 					}
 					getProperties(childProps, names, index+1, result);
 				}
 			}
 		}
 	}
 	
 	public Property getProperty(String name) {
 		for (Property prop : getProperties()) {
 			if (prop.name.equals(name)) {
 				return prop;
 			}
 		}
 		return null;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public void setProperties(List<Property> properties) {
 		this.properties = properties;
 	}
 	
 	public static ModelExtensionAdapter getModelExtensionAdapter(EObject object) {
 		for (Adapter a : object.eAdapters()) {
 			if (a instanceof ModelExtensionAdapter) {
 				return (ModelExtensionAdapter)a;
 			}
 		}
 		return null;
 	}
 
 	public void adaptObject(EObject object) {
 		addModelExtensionAdapter(object);
 		if (description!=null && !description.isEmpty()) {
 			ExtendedPropertiesAdapter adapter = ExtendedPropertiesAdapter.adapt(object);
 			if (adapter!=null) {
 				adapter.setProperty(ExtendedPropertiesAdapter.CUSTOM_DESCRIPTION, description);
 			}
 		}
 	}
 	
 	private void addModelExtensionAdapter(EObject object) {
 		if (!object.eAdapters().contains(this))
 			object.eAdapters().add( new ModelExtensionAdapter(this) );
 	}
 }
