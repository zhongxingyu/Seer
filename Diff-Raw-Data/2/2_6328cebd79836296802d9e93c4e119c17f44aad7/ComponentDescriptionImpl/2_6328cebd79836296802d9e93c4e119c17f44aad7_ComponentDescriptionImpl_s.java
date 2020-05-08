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
 package org.capcaval.c3.componentmanager._impl.tools;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.capcaval.c3.component.Component;
 import org.capcaval.c3.component.ComponentEvent;
 import org.capcaval.c3.component.ComponentService;
 import org.capcaval.c3.component.ComponentState;
 import org.capcaval.c3.componentmanager.tools.ComponentDescription;
 import org.capcaval.tools.pair.Pair;
 import org.capcaval.tools.pair.PairFactory;
 import org.capcaval.tools.pair.PairImpl;
 
 
 public class ComponentDescriptionImpl implements ComponentDescription {
 
 	protected String componentName;
 	protected int componentLevel;
 	protected Class<?> componentType;
 	protected Class<?> componentImplementationType;
 	protected Class<?>[] serviceList;
 	private boolean isComponentStateImplemented = false;
 	private Component componentInstance;
 	private ComponentState componentStateInstance;
 	// provided event and service
 	protected List<Pair<Class<? extends ComponentService>, Class<?>>> providedServiceslist = new ArrayList<Pair<Class<? extends ComponentService>, Class<?>>>();
 	protected List<Class<? extends ComponentEvent>> providedEventsList = new ArrayList<Class<? extends ComponentEvent>>();
 	// consumed event and used service
 	protected List<UsedEventSubscribeDescription> usedEventSubscribeList = new ArrayList<UsedEventSubscribeDescription>();
 	protected List<UsedServicesDescription> usedServicesList = new ArrayList<UsedServicesDescription>();
 	protected List<Method> consumedEventMethodList = new ArrayList<Method>();
 	protected List<SubComponentDescription> subComponentList = new ArrayList<SubComponentDescription>();
 	protected List<ComponentItemDescription> componentItemList = new ArrayList<ComponentItemDescription>();
 	protected List<Class<?>> consumedEventList = new ArrayList<Class<?>>();
 
 	
 	@Override
 	public void addProvidedServices(
 			Pair<Class<? extends ComponentService>, Class<?>> serviceDesc) {
 		providedServiceslist.add(serviceDesc);
 	}
 
 	//TODO à enlever pas utilisé
 	@SuppressWarnings("unchecked")
 	@Override
 	public Class<? extends ComponentService>[] getProvidedServices() {
 		return this.providedServiceslist.toArray(new Class[0]);
 	}
 
 	@Override
 	public void setComponentName(String name) {
 		this.componentName = name;
 	}
 
 	@Override
 	public void setComponentType(Class<?> cmpnClass) {
 		this.componentType = cmpnClass;
 	}
 
 	@Override
 	public void addProvidedServiceList(Pair<Class<? extends ComponentService>, Class<?>>[] serviceDescList) {
 		for(Pair<Class<? extends ComponentService>, Class<?>> serviceDesc : serviceDescList){
 			this.addProvidedServices(serviceDesc);
 		}
 	}
 
 	@Override
 	public void addUsedServicesField( UsedServicesDescription[] usedServiceList) {
 		for(UsedServicesDescription usedService : usedServiceList)
 			this.usedServicesList.add(usedService);
 		
 	}
 
 	@Override
 	public Pair[] getProvidedServiceList() {
 		return this.providedServiceslist.toArray(new PairImpl[0]);
 	}
 
 
 	@Override
 	public Class<?> getComponentImplementationType() {
 		return this.componentImplementationType;
 	}
 
 	@Override
 	public void setComponentImplementationType(Class<?> type) {
 		this.componentImplementationType = type;
 		
 	}
 
 	@Override
 	public void addProvidedEventList(Class<? extends ComponentEvent>[] eventList) {
 		for(Class<? extends ComponentEvent> eventType : eventList){
 			this.providedEventsList.add(eventType);
 		}
 		
 	}
 
 	@Override
 	public Class<? extends ComponentEvent>[] getProvidedEventList() {
 		return this.providedEventsList.toArray(new Class[0]);
 	}
 
 	@Override
 	public Component getComponentInstance() {
 		return this.componentInstance;
 	}
 
 	@Override
 	public void setComponentInstance(Component instance) {
 		this.componentInstance = instance;
 	}
 
 	@Override
 	public ComponentState getComponentStateInstance() {
 		return this.componentStateInstance;
 	}
 
 	@Override
 	public void setComponentStateInstance(ComponentState instance) {
 		this.componentStateInstance = instance;
 	}
 
 
 	@Override
 	public void addUsedComponentEventSubscribeFieldList(
 			UsedEventSubscribeDescription[] cmpEventSubscribeList) {
 		for(UsedEventSubscribeDescription eventSubscribe : cmpEventSubscribeList){
 			this.usedEventSubscribeList.add(eventSubscribe);}
 		
 	}
 
 	@Override
 	public void addUsedComponentServiceFieldList(UsedServicesDescription[] cmpServiceList) {
 		for(UsedServicesDescription serviceDesc : cmpServiceList){
 			this.usedServicesList.add(serviceDesc);}
 	}
 
 	@Override
 	public void addConsumedEventMethodList( Method[] consumeEventMethodList) {
 		for(Method consumeEventMethod : consumeEventMethodList){
 			this.consumedEventMethodList.add(consumeEventMethod);}
 		
 	}
 
 	@Override
 	public void addConsumedEventList(Class<?>[] consumedEventlist) {
 		for(Class<?>eventType : consumedEventlist){
 			this.consumedEventList.add(eventType);
 			}
 	}
 	
 	@Override
 	public Method[] getConsumedEventMethodList() {
 		return this.consumedEventMethodList.toArray(new Method[0]);
 	}
 
 	@Override
 	public UsedEventSubscribeDescription[] getUsedEventSubscribeList() {
 		return this.usedEventSubscribeList.toArray(new UsedEventSubscribeDescription[0]);
 	}
 
 	@Override
 	public UsedServicesDescription[] getUsedServiceList() {
 		
 //		Field[] fieldList = new Field[this.usedServicesList.size()];
 //		for(int i=0; i<this.usedServicesList.size(); i++){
 //			fieldList[i] = this.usedServicesList.get(i).field;
 //		}
 		
 		return this.usedServicesList.toArray(new UsedServicesDescription[0]);
 	}
 	
 	@Override
 	public String toString(){
 		StringBuffer str = new StringBuffer(); 
 		str.append("===================================================================================================\n");
 		str.append("COMPONENT NAME : " + this.componentName + "\t\t level : " + this.componentLevel + "\n");
 		str.append("--------------------------------------------------------------------------------------------\n");
 		str.append("IMPLEMENTATION : " + this.componentImplementationType+ "\n");
 		str.append("STATE : " + this.isComponentStateImplemented+ "\n");
 		str.append("\n");
 		str.append("SERVICE PROVIDED: \n");
 		for(Pair<Class<? extends ComponentService>,Class<?>> pair : this.providedServiceslist){
 			str.append("\t "+pair.firstItem()+"\n");
 		}
 		str.append("EVENT PROVIDED: \n");
 		for(Class<?> eventType : this.providedEventsList){
 			str.append("\t "+eventType+"\n");
 		}
 		str.append("USED SERVICES : \n");
 		for(UsedServicesDescription desc : this.usedServicesList){
 			str.append("\t "+desc.field.getType()+"\n");
 		}
 		str.append("CONSUMED EVENT: \n");
 		for(Method method : this.consumedEventMethodList){
			str.append("\t "+method.getDeclaringClass()+"\n");
 		}
 		str.append("USED EVENT SUBSCRIBE: \n");
 		for(UsedEventSubscribeDescription desc : this.usedEventSubscribeList){
 			str.append("\t "+desc.field.getGenericType()+"\n");
 		}
 		str.append("SUB COMPONENT: \n");
 		for(SubComponentDescription desc : this.subComponentList){
 			str.append("\t "+desc.getField().getType().getSimpleName()+"\n");
 		}
 		str.append("COMPONENT ITEM: \n");
 		for(ComponentItemDescription desc : this.componentItemList){
 			str.append("\t "+desc.getField().getType().getSimpleName()+"\n");
 		}
 		
 		str.append("===================================================================================================\n\n");
 		
 		return str.toString();
 	}
 
 	@Override
 	public void addSubComponent(SubComponentDescription desc) {
 		this.subComponentList.add(desc);
 
 	}
 
 	@Override
 	public void setComponentLevel(int componentLevel) {
 		this.componentLevel = componentLevel;
 		
 	}
 
 	@Override
 	public SubComponentDescription[] getSubComponetList() {
 		return this.subComponentList.toArray(new SubComponentDescription[0]);
 	}
 
 	@Override
 	public void addItem(ComponentItemDescription itemDesc) {
 		this.componentItemList.add(itemDesc);
 	}
 
 	@Override
 	public ComponentItemDescription[] getComponentItemList() {
 		return this.componentItemList.toArray(new ComponentItemDescription[0]);
 	}
 
 	@Override
 	public void addProvidedServices(
 			Class<? extends ComponentService> serviceType, Class<?> cmpnClass) {
 		 Pair<Class<? extends ComponentService>, Class<?>> item =
 			 new PairImpl<Class<? extends ComponentService>, Class<?>>(serviceType, cmpnClass);
 			 //PairFactory.factory.newPair((Class<? extends ComponentService>)serviceType, (Class<?>)cmpnClass);
 		 providedServiceslist.add(item);
 		
 	}
 
 	@Override
 	public Class<?>[] getConsumedEventList() {
 		// TODO Auto-generated method stub
 		return this.consumedEventList.toArray(new Class<?>[0]);
 	}
 
 
 
 }
