 /*
 Copyright (C) 2012 by CapCaval.org
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
 package org.capcaval.c3.componentmanager._impl;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Proxy;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.capcaval.c3.component.Component;
 import org.capcaval.c3.component.ComponentEvent;
 import org.capcaval.c3.component.ComponentEventSubscribe;
 import org.capcaval.c3.component.ComponentService;
 import org.capcaval.c3.component.ComponentState;
 import org.capcaval.c3.componentmanager.ComponentManager;
 import org.capcaval.c3.componentmanager.ComponentManagerController;
 import org.capcaval.c3.componentmanager._impl.tools.ComponentDescriptionContainer;
 import org.capcaval.c3.componentmanager._impl.tools.ComponentEventProxy;
 import org.capcaval.c3.componentmanager._impl.tools.ComponentEventSubscribeImpl;
 import org.capcaval.c3.componentmanager._impl.tools.ComponentItemDescription;
 import org.capcaval.c3.componentmanager._impl.tools.SubComponentDescription;
 import org.capcaval.c3.componentmanager._impl.tools.UsedEventSubscribeDescription;
 import org.capcaval.c3.componentmanager._impl.tools.UsedServicesDescription;
 import org.capcaval.c3.componentmanager.tools.ComponentAnalyserTool;
 import org.capcaval.c3.componentmanager.tools.ComponentDescription;
 import org.capcaval.c3.componentmanager.tools.ComponentDescription.ComponentDescriptionFactory;
 import org.capcaval.tools.pair.Pair;
 
 public class ComponentManagerImpl implements ComponentManager, ComponentManagerController {
 	// List<Component> componentList = new ArrayList<Component>();
 	List<Class<? extends Component>> componentImplTypeList = new ArrayList<Class<? extends Component>>();
 
 	ComponentDescriptionContainer cdc = null;
 
 	@Override
 	public void registerComponent(
 			Class<? extends Component>... componentTypeList) {
 		for (Class<? extends Component> componentType : componentTypeList) {
 			this.componentImplTypeList.add(componentType);
 		}
 	}
 
 	@Override
 	public String startApplication(Class<? extends Component>... componentType) {
 		// first register component
 		this.registerComponent(componentType);
 
 		// secondly let's start
 		String desc = this.startApplication();
 		
 		return desc;
 	}
 
 	@Override
 	public <T extends ComponentService> T getComponentService(
 			Class<T> componentServiceType) {
 
 		return (T) this.cdc.getServiceInstance(componentServiceType);
 	}
 
 	@Override
 	public String startApplication() {
 		// discover all the component
 		ComponentDescription[] cdList = this.discoverAllComponents();
 
 		// allocate one instance of each component and keep in ref the overall description
 		this.cdc = this.allocateAllComponents(cdList);
 
 		// set all the automatic links
 		this.assembleWireToAllComponents(cdc, cdList);
 
 		// activate them all
 		this.startComponents(cdList);
 		
 		return this.cdc.toString();
 	}
 
 	@Override
 	public void startComponents(ComponentDescription[] cdList) {
 		// perform the init for each component
 		for (ComponentDescription componentDesc : cdList) {
 			// retrieve the component state
 			ComponentState cs = componentDesc.getComponentStateInstance();
 
 			if (cs != null) {
 				cs.componentInitiated();
 			}
 
 		}
 
 		// start each component
 		for (ComponentDescription componentDesc : cdList) {
 			// retrieve the component state
 			ComponentState cs = componentDesc.getComponentStateInstance();
 
 			if (cs != null) {
 				cs.componentStarted();
 			}
 		}
 
 	}
 
 	@Override
 	public void assembleWireToAllComponents(ComponentDescriptionContainer cdc,
 			ComponentDescription[] cdList) throws RuntimeException {
 		// create a component description seeker
 
 		// for all components get the used service and wire it
 		for (ComponentDescription cd : cdList) {
 			// assemble wire one component by one component
 			this.assembleWire(cdc, cd);
 			//UsedServicesDescription[] usedServiceList = cd.getUsedServiceList();
 		}
 	}
 
 	private void assembleWire(ComponentDescriptionContainer cdc,
 			ComponentDescription cd) throws RuntimeException {
 		
 			// assemble all used services
 			UsedServicesDescription[] usedServiceList = cd.getUsedServiceList();
 			this.assembleAllUsedServices(cdc, usedServiceList);
 		
 			// assemble all consumed events
 			Method[] consumedEventList = cd.getConsumedEventMethodList();
 			this.assembleAllConsumedEvents(cdc, consumedEventList);
 
 			// assemble all the used event subscribe
 			UsedEventSubscribeDescription[] UsedEventSubscribeList =  cd.getUsedEventSubscribeList();
 			this.assembleAllEventSubscribes(cdc, UsedEventSubscribeList);
 
 			// assemble all the sub component
 			SubComponentDescription[] subComponentList = cd.getSubComponetList();
 			this.assembleAllSubComponent(cdc, subComponentList);
 			
 			// assemble all the item component
 			ComponentItemDescription[] componentItemList =  cd.getComponentItemList();
 			this.assembleAllItemComponent(cdc, componentItemList);
 
 	}
 
 	private void assembleAllItemComponent(ComponentDescriptionContainer cdc,
 			ComponentItemDescription[] componentItemList) {
 		for(ComponentItemDescription desc : componentItemList){
 			// get the component instance to be set
 			Component componentInstance = cdc.getComponentInstance(desc.getComponentType());
 			
 			// get the item instance
 			Object itemInstance = cdc.getItemInstance(desc.getItemType());
 			
 			// get the field to be set
 			Field field = desc.getField();
 			
 			// set the instance to the root component of the item
 			try {
 				field.setAccessible(true);
 				field.set(componentInstance, itemInstance);
 			} catch (Exception e) {
 				e.printStackTrace();
 			} 
 		}
 		
 	}
 
 	private void assembleAllSubComponent(ComponentDescriptionContainer cdc,
 			SubComponentDescription[] subComponentDescList) {
 		for(SubComponentDescription subDesc : subComponentDescList){
 			// get the field to be set
 			Field field = subDesc.getField();
 			
 			Component subInstance = cdc.getComponentInstance(field.getType());
 			Component parentInstance = cdc.getComponentInstance(subDesc.getImplementationType());
 			
 			// set the ref of the sub instance inside the correct field of the parent instance
 			try {
 				field.setAccessible(true);
 				field.set(parentInstance, subInstance);
 			} catch (Exception e) {
 				e.printStackTrace();
 			} 
 		}
 		
 	}
 
 	private void assembleAllEventSubscribes(ComponentDescriptionContainer cdc,
 			UsedEventSubscribeDescription[] usedEventSubscribeList) throws RuntimeException {
 		
 		for(UsedEventSubscribeDescription desc :usedEventSubscribeList){
 			// get the type of eventSubscribe
 			Field eventSubscribeField = desc.getField();
 			
 			ParameterizedType pType = (ParameterizedType)eventSubscribeField.getGenericType();
 			Class<? extends ComponentEvent> type = (Class<? extends ComponentEvent>)pType.getActualTypeArguments()[0];
 			//Class<? extends ComponentEvent> type = (Class<? extends ComponentEvent>)eventSubscribeField.getGenericType();
 			
 			// get instance of the eventSubscribe which will be set as a value
 			ComponentEventSubscribe<?> value = cdc.getEventSubscribeInstance(type);
 			
 			// get the instance to be set
 			Component componentInstance = cdc.getComponentInstance(desc.getImplementationType());
 			
 			// set it to the field
 			try {
 				eventSubscribeField.setAccessible(true);
 				eventSubscribeField.set(componentInstance, value);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 			if(value == null){
 				// build the error message
 				StringBuffer errorMessage = new StringBuffer();
 				errorMessage.append("C³ ERROR : Event producer instance cannot be found, in order to subscribe to it\n");
 				errorMessage.append("   -->  The component implementation (" + componentInstance.getClass() +  " requested C³ the following event subscription "+ type + " \n");
 				errorMessage.append("   -->  Implementation  : (" + componentInstance.getClass().getSimpleName() + ".java:0)\n");
 				errorMessage.append("   -->  The Following attribute annoted @ConsumedEvent is not subscribed \n"); 
 				errorMessage.append("   -->  The " + type.getSimpleName() + " is not published, produce event it inside a deployed component \n");
 				errorMessage.append("   -->  The available events are : \n");
 				if(cdc.getEventInstanceMap().keySet().size() == 0){
 					errorMessage.append("        - There is no Event published to C³" );
 				}
 				for(Class<? extends ComponentEvent> eventSubscribeType : cdc.getEventSubscribeMap().keySet()){
 					errorMessage.append("        - " +  eventSubscribeType.getGenericInterfaces()[0].getClass());
 				}
 
 				
 				throw new RuntimeException(errorMessage.toString());
 			}
 		}
 		
 	}
 
 	private void assembleAllConsumedEvents(ComponentDescriptionContainer cdc,
 			Method[] consumedEventList) {
 		for (Method eventMethod : consumedEventList) {
 			// get the type of service
 			Class<? extends ComponentEvent> eventType = (Class<? extends ComponentEvent>)eventMethod.getReturnType();
 			
 			// get instance of the service which will be set as a value
 			ComponentEventSubscribe<?> eventSubscribe = cdc.getEventSubscribeInstance(eventType);
 			
 			if(eventSubscribe == null){
 				// build the error message
 				StringBuffer errorMessage = new StringBuffer();
 				errorMessage.append("C³ ERROR : Event producer instance cannot be found, in order to subscribe to it\n");
 				errorMessage.append("   -->  The component implementation (" + eventMethod.getDeclaringClass() +  " requested C³ the following event subscription "+ eventMethod.getReturnType() + " \n");
 				errorMessage.append("   -->  Implementation  : (" + eventMethod.getDeclaringClass().getSimpleName() + ".java:0)\n");
 				errorMessage.append("   -->  The Following attribute annoted @ConsumedEvent is not subscribed \n"); 
 				errorMessage.append("   -->  The " + eventMethod.getDeclaringClass().getSimpleName() + " is not published, produce event it inside a deployed component \n");
 				errorMessage.append("   -->  The available events are : \n");
 				if(cdc.getEventSubscribeMap().size() == 0){
 					errorMessage.append("        - There is no Event published to C³" );
 				}
 				for(Class<? extends ComponentEvent> eventSubscribeType : cdc.getEventSubscribeMap().keySet()){
 					errorMessage.append("        - " +  eventSubscribeType.getGenericInterfaces()[0].getClass());
 				}
 				
 				
 				throw new RuntimeException(errorMessage.toString());
 			}
 			
 			
 			// get the event instance
 			ComponentEvent eventInstance = cdc.getEventInstance(eventMethod);
 			
 			// subscribe the event
 			// MLB event
			//eventSubscribe.subscribe(eventInstance);  // (? extends ComponentEvent)
 		}
 		
 	}
 
 	private void assembleAllUsedServices(ComponentDescriptionContainer cdc,
 			UsedServicesDescription[] usedServiceList) {
 		for (UsedServicesDescription service : usedServiceList) {
 			// get the type of service
 			Field serviceField = service.getField();
 			Class<? extends ComponentService> type = (Class<? extends ComponentService>)serviceField.getType();
 			
 			// get instance of the service which will be set as a value
 			ComponentService value = cdc.getServiceInstance(type);
 			
 			// get the component instance of the used service
 			Component cmpInstance = cdc.getComponentInstance(service.getImplementationType());
 			
 			// set it to the field
 			try {
 				serviceField.setAccessible(true);
 				serviceField.set(cmpInstance, value);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 			if(value == null){
 				// build the error message
 				StringBuffer errorMessage = new StringBuffer();
 				errorMessage.append("C³ ERROR : Service instance cannot be found, in order to be injected\n");
 				errorMessage.append("   -->  The component implementation (" + serviceField.getDeclaringClass() +  " requested C³ the following service "+ serviceField.getType() + " \n");
 				errorMessage.append("   -->  Implementation  : (" + serviceField.getDeclaringClass().getSimpleName() + ".java:0)\n");
 				errorMessage.append("   -->  The Following attribute annoted @UsedService is set to null : \"" + serviceField.getType().getSimpleName() + " " + serviceField.getName() + "\"\n"); 
 				errorMessage.append("   -->  The " + type + " is not published, implement it inside a deployed component \n");
 				errorMessage.append("   -->  The available services are : \n");
 				if(cdc.getServicesMap().keySet().size() == 0){
 					errorMessage.append("        - There is no service published to C³" );
 				}
 				for(Class<? extends ComponentService> serviceType : cdc.getServicesMap().keySet()){
 					errorMessage.append("        - " +  serviceType);
 				}
 				
 				
 				throw new RuntimeException(errorMessage.toString());
 			}
 		}
 		
 	}
 
 	@Override
 	public ComponentDescription[] discoverAllComponents() {
 
 		ComponentDescriptionFactory fdf = ComponentDescriptionFactory.factory;
 
 		List<ComponentDescription> cdList = new ArrayList<ComponentDescription>();
 
 		for (Class<?> cmpnClass : this.componentImplTypeList) {
 			
 			this.discoverComponentImpl(cmpnClass,cdList, fdf, 0);
 			
 		}
 
 		return cdList.toArray(new ComponentDescription[0]);
 	}
 
 	private void discoverComponentImpl(Class<?> cmpnClass,
 			List<ComponentDescription> cdList, ComponentDescriptionFactory fdf, int componentLevel) {
 		// allocate a ComponentDescription
 		ComponentDescription cd = fdf.createComponentDescription();
 
 		// add one to the level
 		componentLevel = componentLevel +1;
 		
 		cd.setComponentLevel(componentLevel);
 		
 		// seek any sub component
 		SubComponentDescription[] subComponentList = ComponentAnalyserTool.getSubComponentList(cmpnClass);
 		
 		for(SubComponentDescription scDesc:subComponentList){
 			// recursive discover
 			this.discoverComponentImpl(scDesc.getField().getType(), cdList, fdf, componentLevel);
 			cd.addSubComponent(scDesc);
 		}
 		
 		Class<?> cmpType = ComponentAnalyserTool
 				.getComponentType(cmpnClass);
 
 		cd.setComponentType(cmpType);
 		cd.setComponentImplementationType(cmpnClass);
 		cd.setComponentName(cmpType.getName());
 
 		// get all the services event etc..
 		this.seekComponentAbstractions(cmpnClass, cd);
 		
 		
 		// seek all component items
 		ComponentItemDescription[] cmpItemList = ComponentAnalyserTool.getComponentItemList(cmpnClass);
 		for(ComponentItemDescription itemDesc : cmpItemList){
 			cd.addItem(itemDesc);
 			// get all the abstraction for the items
 			this.seekComponentAbstractions(itemDesc.getItemType(), cd);
 		}
 	
 		// add description to the list
 		cdList.add(cd);
 		
 	}
 
 	protected void seekComponentAbstractions(final Class<?> cmpnClass, final ComponentDescription cd){
 		// seek provided services
 		Class<? extends ComponentService>[] serviceList = ComponentAnalyserTool
 				.getProvidedServiceList(cmpnClass);
 		for(Class<? extends ComponentService> serviceType : serviceList){
 			// create a new pair
 			//Pair<Class<? extends ComponentService>, Class<?>> serviceDesc = (Pair<Class<? extends ComponentService>, Class<?>>)PairFactory.factory.newPair(serviceType, cmpnClass);
 			//Pair<?, ?> serviceDesc = PairFactory.factory.newPair(serviceType, cmpnClass);
 			cd.addProvidedServices(serviceType, cmpnClass);}
 
 		// seek produced event
 		Class<? extends ComponentEvent>[] eventList = ComponentAnalyserTool
 				.getProvidedEventList(cmpnClass);
 		cd.addProvidedEventList(eventList);
 
 		// seek any used services
 		UsedServicesDescription[] usedServiceFieldList = ComponentAnalyserTool
 				.getUsedServiceFieldList(cmpnClass);
 		cd.addUsedComponentServiceFieldList(usedServiceFieldList);
 
 		// seek any consumed event subscribe
 		UsedEventSubscribeDescription[] cmpEventSubscribeList = ComponentAnalyserTool
 				.getUsedEventSubscribeList(cmpnClass);
 		cd.addUsedComponentEventSubscribeFieldList(cmpEventSubscribeList);
 
 		// seek any consumed events from implementation
 		Class<?>[] consumedEventlist = ComponentAnalyserTool.getInterfaceList(cmpnClass, ComponentEvent.class);
 		
 		// seek any consumed events from factory
 		Method[] consumeEventList = ComponentAnalyserTool
 				.getConsumeEventList(cmpnClass);
 		cd.addConsumedEventMethodList(consumeEventList);
 		cd.addConsumedEventList(consumedEventlist);
 	}
 	
 	@Override
 	public ComponentDescriptionContainer allocateAllComponents(
 			ComponentDescription[] cdList) {
 		// allocate a new instance for all component descriptions
		ComponentDescriptionContainer cdc = new ComponentDescriptionContainer(cdList);
 		
 		// create a hashmap to store all service and events
 		Map<Class<?>, Object> cmpInstanceList = new HashMap<Class<?>, Object>();
 
 		for (ComponentDescription cDesc : cdList) {
 			try {
 				// get the type of implementation
 				Class<?> componentType = cDesc.getComponentImplementationType();
 				
 				// allocate the instance of the component
 				Component instance = (Component) componentType.newInstance();
 				// register it 
 				cdc.registerImplementation(componentType, instance);
 				
 				cmpInstanceList.put(componentType, instance);
 				
 				// allocate all the items
 				for(ComponentItemDescription itemDesc : cDesc.getComponentItemList()){
 					// allocate the item
 					Object itemInstance = itemDesc.getItemType().newInstance();
 					// register them as implementation
 					cdc.registerItemImplementation(itemDesc.getItemType(), itemInstance);
 					cmpInstanceList.put(itemDesc.getItemType(), itemInstance);
 				}
 				
 				
 				// register the instance for all provided services
 				for (Pair<?,?> serviceDesc : cDesc.getProvidedServiceList()) {
 					Class<? extends ComponentService> serviceType = (Class<? extends ComponentService>) serviceDesc.firstItem();
 					ComponentService serviceInstance = (ComponentService)cmpInstanceList.get(serviceDesc.secondItem());
 					
 					cdc.registerService(
 							serviceType, 
 							serviceInstance);
 				}
 				// register the instance for all provided events
 				for (Class<? extends ComponentEvent> eventType : cDesc.getProvidedEventList()) {
 					// inject the subscriber to the component
					ComponentEventSubscribe<?> ces = this.injectEventSubscribe(instance, eventType);
 					
 					// register it to componentManager
 					cdc.registerEventSubscribe(eventType, ces);
 				}
 				
 				// seek consumed event to be instantiated from the method
 				for(Method createEventMethod : cDesc.getConsumedEventMethodList()){
 					// set the method accessible
 					createEventMethod.setAccessible(true);
 					
 					// call the method to create the instance
 					ComponentEvent event = (ComponentEvent)createEventMethod.invoke(instance);
 					
 					// keep it for subscrition later
 					cdc.registerEventInstance(createEventMethod, event);
 				}
 
 				// seek consumed event to be instantiated
 				for(Class<?> eventType : cDesc.getConsumedEventList()){
 					// retrieve the instance
 					ComponentEvent event = (ComponentEvent)cDesc.getComponentInstance();
 					
 					// keep it for subscrition later
 					//cdc.registerEventInstance(createEventMethod, event);
 					// MLB Event
 				}
 
 				
 				// keep a ref on the instance
 				cDesc.setComponentInstance(instance);
 
 				// seek for component state implementation
 				if (ComponentState.class.isAssignableFrom(cDesc
 						.getComponentImplementationType())) {
 					cDesc.setComponentStateInstance((ComponentState) instance);
 				}
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		return cdc;
 
 	}
 
 	private <T extends ComponentEvent> ComponentEventSubscribe<T> injectEventSubscribe(
			Component instance, Class<T> eventType) {
 
 		Field field = this.getEventField(instance, eventType);
 
 		// get the event subscribe, create one if none already existing
		//ComponentEventSubscribeImpl<T> ces = this.retrieveComponentEventSubscribe(eventType);
		
		// allocate one subscribe per event
		ComponentEventSubscribeImpl<T>ces = new ComponentEventSubscribeImpl<T>();
 		
 		// allocate the proxy
 		ComponentEventSubscribe<T> cesProxy = (ComponentEventSubscribe<T>) Proxy
 				.newProxyInstance(eventType.getClassLoader(), new Class[] {
 						eventType, ComponentEventSubscribe.class },
 						new ComponentEventProxy<T>(ces));
 
 		// set its value
 		try {
 			// make sure that the event ref can be set
 			field.setAccessible(true);
 			field.set(instance, cesProxy);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return ces;
 
 	}
 
	// TODO to be removed
 	private <T extends ComponentEvent> ComponentEventSubscribeImpl<T> retrieveComponentEventSubscribe(
			Class<T> eventType) {
 		
 		ComponentEventSubscribeImpl<T> ces = null;
 		
		if(this.cdc != null){
 			// TODO Cast à revoir !!!!!!!!!!!!!
			ces = (ComponentEventSubscribeImpl<T>) this.cdc.getEventSubscribeInstance(eventType);}
 
 		// Lazy pattern on ComponentEventSubscribe
 		if (ces == null) {
 			// if null create one
 			ces = new ComponentEventSubscribeImpl<T>();
 		}
 
 		return ces;
 	}
 
 	protected Field getEventField(Component instance,
 			Class<? extends ComponentEvent> eventType) {
 		// retrieve the event attribute
 		Field[] fieldList = instance.getClass().getDeclaredFields();
 
 		Field field = null;
 		boolean isOver = false;
 		int index = 0;
 		while (isOver == false) {
 			field = fieldList[index++];
 			Class<?> type = field.getType();
 			if ( type == eventType) {
 				isOver = true;
 			}
 			else if (index >= fieldList.length) {
 				isOver = true;
 				field = null;
 			}
 		}
 		
 		if(field == null){
 			System.out.println("field not found");
 		}
 			
 		
 		return field;
 	}
 
 	@Override
 	public void stopApplication() {
 		ComponentDescription[] cdList = this.cdc.getComponentDescriptionList();
 		
 		// start each component
 		for (ComponentDescription cd : cdList) {
 			// retrieve the component state
 			ComponentState cs = cd.getComponentStateInstance();
 
 			if (cs != null) {
 				cs.componentStopped();
 			}
 		}
 
 	}
 
 	@Override
 	public <T extends ComponentEvent> ComponentEventSubscribe<T> getComponentEventSubscribe(
 			Class<T> eventType) {
 		return (ComponentEventSubscribe) this.cdc
 				.getEventSubscribeInstance(eventType);
 	}
 
 	@Override
 	public ComponentManagerController getController() {
 		// return itself
 		return this;
 	}
 
 	@Override
 	public void setComponentDescriptionContainer(
 			ComponentDescriptionContainer cdc) {
 		this.cdc = cdc;
 	}
 
 }
