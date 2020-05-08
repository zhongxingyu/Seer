 /**
  * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package fr.imag.adele.apam.apform.impl.handlers;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Dictionary;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.felix.ipojo.ConfigurationException;
 import org.apache.felix.ipojo.FieldInterceptor;
 import org.apache.felix.ipojo.architecture.ComponentTypeDescription;
 import org.apache.felix.ipojo.architecture.HandlerDescription;
 import org.apache.felix.ipojo.metadata.Attribute;
 import org.apache.felix.ipojo.metadata.Element;
 import org.apache.felix.ipojo.parser.FieldMetadata;
 import org.apache.felix.ipojo.parser.MethodMetadata;
 
 import fr.imag.adele.apam.Component;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.apform.impl.ApamAtomicComponentFactory;
 import fr.imag.adele.apam.apform.impl.ApamComponentFactory;
 import fr.imag.adele.apam.apform.impl.ApamInstanceManager;
 import fr.imag.adele.apam.apform.impl.PropertyCallback;
 import fr.imag.adele.apam.declarations.AtomicImplementationDeclaration;
 import fr.imag.adele.apam.declarations.ImplementationDeclaration;
 import fr.imag.adele.apam.declarations.InjectedPropertyPolicy;
 import fr.imag.adele.apam.declarations.PropertyDefinition;
 import fr.imag.adele.apam.impl.InstanceImpl;
 
 public class PropertyInjectionHandler extends ApformHandler implements	FieldInterceptor {
 
 	/**
 	 * The registered name of this iPojo handler
 	 */
 	public static final String NAME = "properties";
 
 	/**
 	 * Whether this handler is required for the specified configuration
 	 */
 	public static boolean isRequired(AtomicImplementationDeclaration componentDeclaration) {
 		
 		for (PropertyDefinition definition : componentDeclaration.getPropertyDefinitions()) {
 
 			if (definition.getField() != null)
 				return true;
 
 			if (definition.getCallback() != null)
 				return true;
 		}
 		
     	return false;
 	}
 	
 	@Override
 	public void initializeComponentFactory(ComponentTypeDescription typeDesc, Element metadata) throws ConfigurationException {
 
 		if (!(getFactory() instanceof ApamAtomicComponentFactory))
 			return;
 
 		ApamAtomicComponentFactory implementation = (ApamAtomicComponentFactory) getFactory();
 		ImplementationDeclaration declaration = implementation.getDeclaration();
 
 		if (!(declaration instanceof AtomicImplementationDeclaration))
 			return;
 
 		AtomicImplementationDeclaration primitive = (AtomicImplementationDeclaration) declaration;
 		for (PropertyDefinition definition : primitive.getPropertyDefinitions()) {
 
 			if (definition.getField() != null) {
 				FieldMetadata field = getPojoMetadata().getField(
 						definition.getField());
 				if (field == null)
 					throw new ConfigurationException("Invalid property definition "	+ definition.getName()+ ": the specified field does not exist");
 
 			}
 
 			if (definition.getCallback() != null) {
 				MethodMetadata method = getPojoMetadata().getMethod(definition.getCallback());
 				if (method == null)
 					throw new ConfigurationException("Invalid property definition "	+ definition.getName() + ": the specified method does not exist");
 			}
 		}
 	}
 
 	@Override
 	public void configure(Element metadata,	@SuppressWarnings("rawtypes") Dictionary configuration)	throws ConfigurationException {
 		/*
 		 * Add interceptors to delegate property injection
 		 * 
 		 * NOTE All validations were already performed when validating the
 		 * factory @see initializeComponentFactory, including initializing
 		 * unspecified properties with appropriate default values. Here we just
 		 * assume metadata is correct.
 		 */
 
 		if (!(getFactory() instanceof ApamAtomicComponentFactory))
 			return;
 
 		ApamAtomicComponentFactory implementation = (ApamAtomicComponentFactory) getFactory();
 		ImplementationDeclaration declaration = implementation.getDeclaration();
 
 		if (!(declaration instanceof AtomicImplementationDeclaration))
 			return;
 
 		AtomicImplementationDeclaration primitive = (AtomicImplementationDeclaration) declaration;
 		for (PropertyDefinition definition : primitive.getPropertyDefinitions()) {
 
 			if (definition.getField() != null) {
 				FieldMetadata field = getPojoMetadata().getField(definition.getField());
 				getInstanceManager().register(field,this);
 			}
 
 			if (definition.getCallback() != null) {
 				getInstanceManager().addCallback(new PropertyCallback(getInstanceManager(), definition));
 			}
 		}
 	}
 	
 	public void setApamComponent(Component component) {
 		
 		if (component == null)
 			return;
 		
 		ApamAtomicComponentFactory implementation = (ApamAtomicComponentFactory) getFactory();
 		ImplementationDeclaration declaration = implementation.getDeclaration();
 
 		if (!(declaration instanceof AtomicImplementationDeclaration))
 			return;
 
 		AtomicImplementationDeclaration primitive = (AtomicImplementationDeclaration) declaration;
 		for (PropertyDefinition definition : primitive.getPropertyDefinitions()) {
 
 			if (definition.getInjected() == InjectedPropertyPolicy.EXTERNAL)
 				continue;
 			
 			if (definition.getField() != null) {
 				FieldMetadata field = getPojoMetadata().getField(definition.getField());
 				
 				Object fieldValue	= getInstanceManager().getFieldValue(field.getFieldName());
 				
 				if (definition.getInjected() == InjectedPropertyPolicy.INTERNAL)
 					onSet(getInstanceManager().getPojoObject(), field.getFieldName(), fieldValue);
 				
 				if (definition.getInjected() == InjectedPropertyPolicy.BOTH) {
 					Object apamValue	= component.getPropertyObject(definition.getName());
 					
 					if (apamValue == null)
 						onSet(getInstanceManager().getPojoObject(), field.getFieldName(), fieldValue);
 				}
 
 			}
 
 		}		
 	}
 
 	@Override
 	public Object onGet(Object pojo, String fieldName, Object currentValue) {
 
 		if (getInstanceManager().getApamComponent() == null) {
 			return currentValue;
 		}
 		
     	if (!(getFactory() instanceof ApamAtomicComponentFactory))
     		return currentValue;
 
     	ApamAtomicComponentFactory implementation	= (ApamAtomicComponentFactory) getFactory();
     	ImplementationDeclaration declaration		= implementation.getDeclaration();
     	
     	if (! (declaration instanceof AtomicImplementationDeclaration))
     		return currentValue;
     	
     	AtomicImplementationDeclaration primitive	= (AtomicImplementationDeclaration) declaration;
     	for (PropertyDefinition definition : primitive.getPropertyDefinitions()) {
     		
     		if (definition.getField() != null && definition.getField().equals(fieldName) ) {
     			
     			String property		= definition.getName();
     			Instance instance 	= getInstanceManager().getApamComponent();
  
     			/*
     			 * For primitive property fields, always return the APAM value that is already of the
     			 * correct type and is always synchronized
     			 */
     			if (! definition.isSet())
     				return instance.getPropertyObject(property);
     			
     			/*
     			 * For multi-valued property fields, convert the APAM value to a collection that can
     			 * be injected into the field, and that track modifications 
     			 */
     			if (definition.isSet() ) {
 
     				@SuppressWarnings("unchecked")
 					Set<String> newValue 		= (Set<String>) instance.getPropertyObject(property);
         			BoundSet<?> injectedValue	= null;
         			
     				if (definition.getBaseType().equals("int") && newValue != null) {
     					
     					injectedValue = new BoundSet<Integer>(newValue,instance,definition) {
     					
 							protected final Integer cast(String element) {
 								return Integer.valueOf(element);
 							}
 
 							protected final String uncast(Integer value) {
 								return value.toString();
 							}
     					
     					};
     				}
     				else if (definition.getBaseType().equals("boolean") && newValue != null) {
     					
     					injectedValue = new BoundSet<Boolean>(newValue,instance,definition) {
     					
 							protected final Boolean cast(String element) {
 								return Boolean.valueOf(element);
 							}
 
 							protected final String uncast(Boolean value) {
 								return value.toString();
 							}
     					
     					};
        				}
     				else if (newValue != null) {
     					
     					injectedValue = new BoundSet<String>(newValue,instance,definition) {
     					
 							protected final String cast(String element) {
 								return element;
 							}
 
 							protected final String uncast(String value) {
 								return value;
 							}
     					
     					};
        				}
      				
     				if (injectedValue == currentValue)
         				continue;
 
         			return injectedValue;
 
     			}
      			
     			
     		}
     		
     	}
     	
     	return currentValue;
 	}
 
 	@Override
 	public void onSet(Object pojo, String fieldName, Object newValue) {
 
 		if (getInstanceManager().getApamComponent() == null) {
 			return;
 		}
 
     	if (!(getFactory() instanceof ApamAtomicComponentFactory))
     		return;
 
     	ApamAtomicComponentFactory implementation	= (ApamAtomicComponentFactory) getFactory();
     	ImplementationDeclaration declaration	= implementation.getDeclaration();
     	
     	if (! (declaration instanceof AtomicImplementationDeclaration))
     		return;
     	
     	AtomicImplementationDeclaration primitive	= (AtomicImplementationDeclaration) declaration;
     	for (PropertyDefinition definition : primitive.getPropertyDefinitions()) {
     		
     		if (definition.getField() != null && definition.getField().equals(fieldName)) {
     			
     			String property		= definition.getName();
     			Instance instance 	= getInstanceManager().getApamComponent();
 
  
     			/*
    			 * If the field has been modified, and the injection type is not EXTERNAL update the Apam value
     			 */
  
         			if (newValue != null && newValue instanceof BoundSet<?>)
         				newValue = ((BoundSet<?>) newValue).unwrap();
 
         			Object currentValue = instance.getPropertyObject(property);
 
         			if (newValue != null && currentValue != null && currentValue.equals(newValue))
         				continue;
 
         			if (newValue == null && currentValue == null)
         				continue;
 
            			if (definition.getInjected() != InjectedPropertyPolicy.EXTERNAL) {
 
 	        			if (newValue == null)
 	        				((InstanceImpl)instance).removeProperty(property,true);
 	        			
 	        			if (newValue != null)
 	        				((InstanceImpl)instance).setProperty(property, newValue, true);
         			}
         			else {
            				System.out.println("ERROR modification of external property "+definition.getName()+" by field"+definition.getField());
     			}
     			
     		} 
     		
     	}
     	
     	return;
 		
 	}
 
 	/**
 	 * The description of this handler instance
 	 * 
 	 */
 	private static class Description extends HandlerDescription {
 
 		private final PropertyInjectionHandler propertyHandler;
 
 		public Description(PropertyInjectionHandler propertyHandler) {
 			super(propertyHandler);
 			this.propertyHandler = propertyHandler;
 		}
 
 		@Override
 		public Element getHandlerInfo() {
 			Element root = super.getHandlerInfo();
 
 			if (propertyHandler.getInstanceManager() instanceof ApamInstanceManager) {
 				ApamInstanceManager instance = (ApamInstanceManager) propertyHandler.getInstanceManager();
 				for (PropertyDefinition definition : instance.getFactory().getDeclaration().getPropertyDefinitions()) {
 
 					/*
 					 * Ignore non injected properties
 					 */
 					if (definition.getField() == null && definition.getCallback() == null)
 						continue;
 
 					String name = definition.getName();
 					String field = definition.getField();
 					String method = definition.getCallback();
 					String value = instance.getApamComponent() != null ? instance.getApamComponent().getProperty(name) : null;
 
 					Element property = new Element("property", ApamComponentFactory.APAM_NAMESPACE);
 					
 					property.addAttribute(new Attribute("name",	ApamComponentFactory.APAM_NAMESPACE,  name));
 					property.addAttribute(new Attribute("field", ApamComponentFactory.APAM_NAMESPACE, field != null ? field : ""));
 					property.addAttribute(new Attribute("method",ApamComponentFactory.APAM_NAMESPACE, method != null ? method : ""));
 					property.addAttribute(new Attribute("value", ApamComponentFactory.APAM_NAMESPACE, value != null ? value : ""));
 
 					root.addElement(property);
 				}
 			}
 			return root;
 		}
 
 	}
 
 	@Override
 	public HandlerDescription getDescription() {
 		return new Description(this);
 	}
 
 	@Override
 	public void start() {
 	}
 
 	@Override
 	public void stop() {
 	}
 
 	@Override
 	public String toString() {
 		return "APAM property manager for "	+ getInstanceManager().getInstanceName();
 	}
 	
 	
 	/**
 	 * This class handles a typed set that is dynamically bound to a property of an APAM instance.
 	 * Changes to the set are automatically propagated to the property
 	 */
 	private static abstract class BoundSet<E> implements Set<E> {
 		
 		private final Instance 				instance;
 		private final PropertyDefinition	property;
 		private final Set<String>			backing;
 		
 		public BoundSet(Set<String> backing, Instance instance, PropertyDefinition property) {
 			
 			this.backing 	= backing;
 			this.instance	= instance;
 			this.property	= property;
 		}
 
 		private Set<String> unwrap() {
 			return backing;
 		}
 
 		/**
 		 * Updates the associated property when the backing collection is changed
 		 */
 		private final void propagate() {
 			((InstanceImpl)instance).setProperty(property.getName(),this.unwrap(),true);
 		}
 		
 		/**
 		 * Verifies that non-internal properties can only be modified through the APAM API
 		 */
 		private final void checkModifiable() throws UnsupportedOperationException {
 			if (property.getInjected()!=InjectedPropertyPolicy.INTERNAL)
 				throw new UnsupportedOperationException("Field "+ property.getField()+
 						" is associated to non-internal property "+property.getName()+
 						" of component "+property.getComponent().getName()+
 						" and its value can not be modified");
 		}
 		
 		/**
 		 * Converts the string element of the backing collection to the type of the view
 		 */
 		protected abstract E cast(String element);
 		
 		/**
 		 * Converts a value of the type of the view to a string that can be stored in the
 		 * backing collection
 		 */
 		protected abstract String uncast(E value);
 
 		/*
 		 * The Object contract
 		 */
 		public boolean equals(Object o) {
 			return o == this || backing.equals(o);
 		}
 		
 		public int hashCode() {
 			return backing.hashCode();
 		}
 		
 		public String toString() {
 			return backing.toString();
 		}
 
 		/*
 		 * The Collection contract
 		 * 
 		 */
 		public int size() {
 			return backing.size();
 		}
 
 		public boolean isEmpty() {
 			return backing.isEmpty();
 		}
 
 		@SuppressWarnings("unchecked")
 		public boolean contains(Object o) {
 			/*
 			 * WARNING This implementation restricts the parameter to be a value of the type of
 			 * the view. This is more restrictive that the contract of this method, but is needed
 			 * in order to ensure the automatic conversion. 
 			 */
 			return backing.contains(uncast((E)o));
 		}
 
 		@Override
 		@SuppressWarnings("unchecked")
 		public boolean containsAll(Collection<?> collection) {
 			/*
 			 * WARNING This implementation restricts the parameter to be a value of the type of
 			 * the view. This is more restrictive that the contract of this method, but is needed
 			 * in order to ensure the automatic conversion. 
 			 */
 			List<String> elements = new ArrayList<String>();
 			for(Object element : collection) {
 				elements.add(uncast((E)element));
 			}
 			
 			return backing.containsAll(elements);
 		}
 
 		@Override
 		public Iterator<E> iterator() {
 		    return new Iterator<E>() {
 				private final Iterator<String> backing = BoundSet.this.backing.iterator();
 				
 				public boolean hasNext() {
 					return backing.hasNext();
 				}
 				public E next() {
 					return BoundSet.this.cast(backing.next());
 				}
 				
 				public void remove() {
 					BoundSet.this.checkModifiable();
 					backing.remove();
 					BoundSet.this.propagate();
 				}
 			};
 		}
 
 		@Override
 		public Object[] toArray() {
 			List<E> elements = new ArrayList<E>();
 			for(String element : backing) {
 				elements.add(cast(element));
 			}
 			
 			return elements.toArray();
 		}
 		
 		public <T extends Object> T[] toArray(T[] a) {
 			List<E> elements = new ArrayList<E>();
 			for(String element : backing) {
 				elements.add(cast(element));
 			}
 			
 			return elements.toArray(a);
 		}
 
 		@Override
 		public boolean add(E e) {
 			checkModifiable();
 			boolean added = backing.add(uncast(e));
 			if (added)
 				propagate();
 			
 			return added;
 		}
 
 		@Override
 		public boolean addAll(Collection<? extends E> collection) {
 			List<String> elements = new ArrayList<String>();
 			for(E element : collection) {
 				elements.add(uncast(element));
 			}
 			
 			checkModifiable();
 			boolean added = backing.addAll(elements);
 			if (added)
 				propagate();
 			
 			return added;
 		}
 		
 		@Override
 		@SuppressWarnings("unchecked")
 		public boolean remove(Object o) {
 			/*
 			 * WARNING This implementation restricts the parameter to be a value of the type of
 			 * the view. This is more restrictive that the contract of this method, but is needed
 			 * in order to ensure the automatic conversion. 
 			 */
 			checkModifiable();
 			boolean removed = backing.remove(uncast((E)o));
 			if (removed)
 				propagate();
 			
 			return removed;
 		}
 
 		@Override
 		@SuppressWarnings("unchecked")
 		public boolean removeAll(Collection<?> collection) {
 			/*
 			 * WARNING This implementation restricts the parameter to be a value of the type of
 			 * the view. This is more restrictive that the contract of this method, but is needed
 			 * in order to ensure the automatic conversion. 
 			 */
 			List<String> elements = new ArrayList<String>();
 			for(Object element : collection) {
 				elements.add(uncast((E)element));
 			}
 			
 			checkModifiable();
 			boolean removed = backing.removeAll(elements);
 			if (removed)
 				propagate();
 			return removed;
 		}
 
 		@Override
 		@SuppressWarnings("unchecked")
 		public boolean retainAll(Collection<?> collection) {
 			/*
 			 * WARNING This implementation restricts the parameter to be a value of the type of
 			 * the view. This is more restrictive that the contract of this method, but is needed
 			 * in order to ensure the automatic conversion. 
 			 */
 			List<String> elements = new ArrayList<String>();
 			for(Object element : collection) {
 				elements.add(uncast((E)element));
 			}
 			
 			checkModifiable();
 			boolean removed = backing.retainAll(elements);
 			if (removed)
 				propagate();
 			
 			return removed;
 		}
 		
 		@Override
 		public void clear() {
 			checkModifiable();
 			backing.clear();
 			propagate();
 		}
 	}
 
 }
