 /*
  * Copyright Adele Team LIG
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package fr.liglab.adele.cilia.model.impl;
 
 import java.util.Dictionary;
 
 import fr.liglab.adele.cilia.model.Binding;
 import fr.liglab.adele.cilia.model.Chain;
 import fr.liglab.adele.cilia.model.Component;
 import fr.liglab.adele.cilia.model.MediatorComponent;
 import fr.liglab.adele.cilia.model.Port;
 
 /**
  * This class represent the relation between two mediators in the Cilia Model.
  * 
  * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
  *
  */
 public class BindingImpl extends ComponentImpl implements Binding{
     /**
      * Reference to the port of the source mediator where this bind is done.
      */
     private Port targetPort = null;
     /**
      * Reference to the port of the target mediator where this bind is done.
      */
     private Port sourcePort = null;
 
     private volatile static long bindingIds = 0;
     
     private volatile SenderImpl sender;
     
     private volatile CollectorImpl collector;
     
     private final Object lockObject = new Object();
     /**
      * Constructor.
      */
     public BindingImpl() {
         this(null, null);
     }
     /**
      * 
      * @param id BindingImpl identificator.
      * @param type BindingImpl type.
      * @param classname //Not used. 
      * @param properties Properties that will be mapped to sender/collector properties.
      */
     public BindingImpl(String type,
             Dictionary properties) {
         super(new Long(bindingIds++).toString(), type, null, properties);
     }
     /**
      * Get the parent chain.
      * @return the parent chain.
      */
     public Chain getChain() {
     	return getSourceMediator().getChain();
     }
     /**
      * Set the source mediator model.
      * @param source the mediator model.
      */
     public void setSourcePort(Port source) {
     	MediatorComponentImpl med;
     	synchronized (lockObject) {
     		this.sourcePort = source;
     		med = (MediatorComponentImpl)this.sourcePort.getMediator(); 
 		}
     	med.addOutBinding(this);
     }
     /**
      * Get the source mediator port in this binding.  
      * @return the source mediator port.
      */
     public Port getSourcePort() {
     	synchronized (lockObject) {
     		return this.sourcePort;	
 		}
         
     }
     
     /**
      * Set the target mediator model.
      * @param target
      */
     public void setTargetPort(Port target) {
     	MediatorComponentImpl med;
     	synchronized (lockObject) {
     		this.targetPort = target;
     		med = (MediatorComponentImpl)this.targetPort.getMediator();
 		}
     	med.addInBinding(this);
     }
     /**
      * Get the target mediator port asociated to this binding. 
      * @return the target mediator port.
      */
     public Port getTargetPort() {
     	synchronized (lockObject) {
     		return this.targetPort;	
 		}
     }
     /**
      * Get the source mediator model.
      * @return the source mediator model.
      */
     public MediatorComponent getSourceMediator() {
     	MediatorComponent med = null;
     	synchronized (lockObject) {
     		if (sourcePort != null) {
     			med = sourcePort.getMediator();
     		}
 		}
         return med;
     }
     /**
      * Get the target mediator model.
      * @return the target mediator model.
      */
     public MediatorComponent getTargetMediator() {
     	MediatorComponent med = null;
     	synchronized (lockObject) {
     		if (targetPort != null) {
     			med = targetPort.getMediator();
     		}
     	}
     	return med;
     }
 
     
 
 	/**
 	 * Add a sender to the current mediator representation model.
 	 * @param sender Sender representation model to add to the current mediator.
 	 * @return true if was successfully added, false if not.
 	 */
 	public boolean addSender(SenderImpl sender) {
 		boolean result = false;
 		try {
 			synchronized (lockObject) {
 				this.sender = sender; 
 				result = true;
 			}
 		}catch (Exception ex){
 			result = false;
 		}
 
 		return result;
 	}
 	/**
 	 * Get the sender added to tue current mediator wich contains the given identificator.
 	 * @param senderId sender identificator.
 	 * @return the sender which contains the identificator, null if there is any sender with the given identificator.
 	 */
 	public SenderImpl getSender() {
 		synchronized (lockObject) {
 			return  sender;
 		}
 	}
 
 	
 	
 	/**
 	 * Add a collector to the mediator representation model.
 	 * @param collector Collector model to add.
 	 * @return true if collector was successfully added, false if not.
 	 */
 	public boolean addCollector(CollectorImpl collector) {
 		boolean result = false;
 		try {
 			synchronized (lockObject) {
 				this.collector = collector; 
 				result = true;
 			}
 		}catch (Exception ex){
 			result = false;
 		}
 		return result;
 	}
 
 
 	/**
 	 * Get the collector representation model which has the given identificator.
 	 * @param collectorId collector identificator.
 	 * @return the reference collector, null if any collector correspond to that identificator.
 	 */
 	public CollectorImpl getCollector() {
 		synchronized (lockObject) {
 			return collector;
 		}
 	}
 	
 	public String toString(){
		StringBuffer toShow = new StringBuffer();
 		if (getSourcePort() != null) {
			toShow.append("FROM ");
 			toShow.append(getSourceMediator().getId());
 			toShow.append(":");
 			toShow.append(getSourcePort().getName());
 		}
		if (getSourcePort() != null) {
			toShow.append(" TO ");
 			toShow.append(getTargetMediator().getId());
 			toShow.append(":");
 			toShow.append(getTargetPort().getName());
 		}
 		return toShow.toString();
 	}
     
 }
