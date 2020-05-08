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
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Dictionary;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import fr.liglab.adele.cilia.exceptions.CiliaException;
 import fr.liglab.adele.cilia.model.Adapter;
 import fr.liglab.adele.cilia.model.Binding;
 import fr.liglab.adele.cilia.model.Chain;
 import fr.liglab.adele.cilia.model.Mediator;
 import fr.liglab.adele.cilia.model.MediatorComponent;
 import fr.liglab.adele.cilia.model.Port;
 
 /**
  * 
  * This class represent the chain model.
  * 
  * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
  *         Team</a>
  * 
  */
 public class ChainImpl extends ComponentImpl implements Chain {
 	/**
 	 * List of mediators contained in the chain model.
 	 */
 	private Hashtable /* <MediatorImpl> */mediators = new Hashtable();
 
 	/**
 	 * List of adapters contained in the chain model.
 	 */
 	private Hashtable /* <Adapters> */adapters = new Hashtable();
 	/**
 	 * List of bindings contained in the chain model.
 	 */
 	private Set /* <BindingImpl> */bindings = new HashSet();
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param id
 	 *            chain identificator.
 	 * @param type
 	 *            chain type.
 	 * @param nspace
 	 *            //not used.
 	 * @param properties
 	 *            chain properties.
 	 */
 	public ChainImpl(String id, String type, String nspace,
 			Dictionary properties) {
 		super(id, type, nspace, properties);
 
 	}
 
 	/**
 	 * Add a mediator to the chain model.
 	 * 
 	 * @param mediator
 	 *            mediator model to add.
 	 * @return true if mediator is added or to the chain or if mediator was
 	 *         already in the chain. return false if the mediator given as a
 	 *         parameter is null.
 	 */
 	public boolean add(Mediator mediator) throws CiliaException {
 		boolean inserted = false;
 		boolean exist = true;
 		String mediatorId = mediator.getId();
 		try {
 			mutex.readLock().acquire();
 		} catch (InterruptedException e) {
 		}
 		if (mediator != null && !mediators.containsKey(mediatorId)
 				&& (!adapters.containsKey(mediatorId))) {
 			exist = false;
 		}
 		mutex.readLock().release();
 
 		if (!exist) {
 			try {
 				mutex.writeLock().acquire();
 			} catch (InterruptedException e) {
 			}
 			mediators.put(mediatorId, mediator);
 			mutex.writeLock().release();
 			inserted = true;
 		} else {
 			throw new CiliaException("MediatorImpl identifier must be unique: "
 					+ mediator.getId());
 		}
 		if (inserted) {
 			((MediatorComponentImpl) mediator).setChain(this);
 			setChanged();
 			notifyObservers(new UpdateEvent(UpdateActions.ADD_MEDIATOR,
 					mediator));
 
 		}
 		return inserted;
 	}
 
 	/**
 	 * Remove mediator from the chain model, also it will remove the bindings
 	 * asociated to the given mediator
 	 * 
 	 * @param mediator
 	 *            mediator to remove.
 	 * @return true if it was removed, false if mediator was not in chain.
 	 */
 	public boolean removeMediator(Mediator mediator) {
 		if (mediator != null) {
 			return removeMediator(mediator.getId());
 		}
 		return false;
 	}
 
 	/**
 	 * Remove a mediator from the chain model, also it will remove the bindings
 	 * asociated to the given mediator
 	 * 
 	 * @param mediatorId
 	 *            mediator identificator to remove.
 	 * @return true if it was removed, false if mediator was not in chain.
 	 */
 	public boolean removeMediator(String mediatorId) {
 		// get MediatorImpl and bindings.
 		MediatorImpl mediatorToRemove = null;
 		Binding[] inbindings = null;
 		Binding[] outbindings = null;
 		boolean result = false;
 		try {
 			mutex.writeLock().acquire();
 		} catch (InterruptedException e) {
 		}
 		mediatorToRemove = (MediatorImpl) mediators.remove(mediatorId);
 		mutex.writeLock().release();
 		if (mediatorToRemove != null) {
 			inbindings = mediatorToRemove.getInBindings();
 			outbindings = mediatorToRemove.getOutBindings();
 			unlinkBindingsFromChain(inbindings);
 			unlinkBindingsFromChain(outbindings);
 			mediatorToRemove.setChain(null);
 			result = true;
 		} else {
 			result = false;
 		}
 		// bindings are removed from the chain, we need to remove bindings from
 		// the mediator.
 		if (result) {
 			unbind(inbindings);
 			unbind(outbindings);
 			setChanged();
 			notifyObservers(new UpdateEvent(UpdateActions.REMOVE_MEDIATOR,
 					mediatorToRemove));
 		}
 		return result;
 	}
 
 	/**
 	 * Obtain the mediator model which has the given identificator.
 	 * 
 	 * @param mediatorId
 	 *            MediatorImpl identificator.
 	 * @return the mediator which has the given identificator.
 	 */
 	public Mediator getMediator(String mediatorId) {
 		Mediator med = null;
 		try {
 			mutex.readLock().acquire();
 		} catch (InterruptedException e) {
 		}
 		med = (Mediator) mediators.get(mediatorId);
 		mutex.readLock().release();
 		return med;
 	}
 
 	/**
 	 * Add an adapter model instance to the chain.
 	 * 
 	 * @param adapter
 	 *            The adapter model reference to add.
 	 * @return true if is correctely added, false if adapter is null or if there
 	 *         is an adapter with the same id in the chain.
 	 */
 	public boolean add(Adapter adapter) throws CiliaException {
 		boolean inserted = false;
 		boolean exist = true;
 		try {
 			mutex.readLock().acquire();
 		} catch (InterruptedException e) {
 		}
 		if (adapter != null && !adapters.containsKey(adapter.getId())
 				&& (!mediators.containsKey(adapter.getId()))) {
 			exist = false;
 		}
 		mutex.readLock().release();
 		if (!exist) {
 			try {
 				mutex.writeLock().acquire();
 			} catch (InterruptedException e) {
 			}
 			adapters.put(adapter.getId(), adapter);
 			mutex.writeLock().release();
 			inserted = true;
 		} else {
 			throw new CiliaException("AdapterImpl identifier must be unique: "
 					+ adapter.getId());
 		}
 		if (inserted) {
 			((AdapterImpl) adapter).setChain(this);
 			setChanged();
 			notifyObservers(new UpdateEvent(UpdateActions.ADD_ADAPTER, adapter));
 		}
 		return inserted;
 	}
 
 	/**
 	 * Get all the mediators models added to the chain model.
 	 * 
 	 * @return
 	 */
 	public Set getMediators() {
 		try {
 			mutex.readLock().acquire();
 		} catch (InterruptedException e) {
 		}
 		Set ns = new HashSet(mediators.values());
 		mutex.readLock().release();
 		return ns;
 	}
 
 	/**
 	 * Obtain the adaptor model which has the given identificator.
 	 * 
 	 * @param mediatorId
 	 *            MediatorImpl identificator.
 	 * @return the mediator which has the given identificator.
 	 */
 	public Adapter getAdapter(String adapterId) {
 		Adapter adap = null;
 		try {
 			mutex.readLock().acquire();
 		} catch (InterruptedException e) {
 		}
 		adap = (Adapter) adapters.get(adapterId);
 		mutex.readLock().release();
 		return adap;
 	}
 
 	/**
 	 * Get all the mediators models added to the chain model.
 	 * 
 	 * @return
 	 */
 	public Set getAdapters() {
 		try {
 			mutex.readLock().acquire();
 		} catch (InterruptedException e) {
 		}
 		Set ns = new HashSet(adapters.values());
 		mutex.readLock().release();
 		return ns;
 	}
 
 	/**
 	 * Remove a adapter from the chain model, also it will remove the bindings
 	 * asociated to the given adapter
 	 * 
 	 * @param adapterId
 	 *            adapter identificator to remove.
 	 * @return true if it was removed, false if adapter was not in chain.
 	 */
 	public boolean removeAdapter(String adapterId) {
 		// get AdapterImpl and bindings.
 		AdapterImpl adapterToRemove = null;
 		Binding[] inbindings = null;
 		Binding[] outbindings = null;
 		boolean result = false;
 		try {
 			mutex.writeLock().acquire();
 		} catch (InterruptedException e) {
 		}
 		adapterToRemove = (AdapterImpl) adapters.remove(adapterId);
 		mutex.writeLock().release();
 		if (adapterToRemove != null) {
 			inbindings = adapterToRemove.getInBindings();
 			outbindings = adapterToRemove.getOutBindings();
 			unlinkBindingsFromChain(inbindings);
 			unlinkBindingsFromChain(outbindings);
 			adapterToRemove.setChain(null);
 			result = true;
 		} else {
 			result = false;
 		}
 		// bindings are removed from the chain, we need to remove bindings from
 		// the adapter.
 		if (result) {
 			unbind(inbindings);
 			unbind(outbindings);
 
 			setChanged();
 			notifyObservers(new UpdateEvent(UpdateActions.REMOVE_ADAPTER,
 					adapterToRemove));
 		}
 		return result;
 	}
 
 	/**
 	 * Get all the bindings added to the chain model.
 	 * 
 	 * @return the added bindings.
 	 */
 	public Set getBindings() {
 		try {
 			mutex.readLock().acquire();
 		} catch (InterruptedException e) {
 		}
 		Set ns = new HashSet(bindings);
 		mutex.readLock().release();
 		return ns;
 	}
 
 	/**
 	 * Bind two mediators using their inPort and outPorts.
 	 * 
 	 * @param sourcePort
 	 *            MediatorImpl's delivery port.
 	 * @param targetPort
 	 *            MediatorImpl's entry port.
 	 * @return The BindingImpl which associes two mediators, null when it can't
 	 *         associate them.
 	 */
 	public Binding bind(Port sourcePort, Port targetPort) {
 		Binding bind = new BindingImpl();
 		return bind(sourcePort, targetPort, bind);
 	}
 
 	/**
 	 * 
 	 * @param sourcePort
 	 * @param targetPort
 	 * @param binding
 	 * @return The BindingImpl which associes two mediators, null when it can't
 	 *         associate them because the binding has already done with others
 	 *         mediators.
 	 */
 	public Binding bind(Port sourcePort, Port targetPort, Binding binding) {
 		boolean result = false;
 		// if binding has associated others ports, it could'nt be created.
 		if (null != binding.getSourcePort() || null != binding.getTargetPort()) {
 			System.err.println("BindingImpl is assigned to other components");
 			return null;
 		}
 		if (sourcePort.getMediator().getChain() != this
 				|| targetPort.getMediator().getChain() != this) {
 			System.err
 					.println("Mediators in bind doesn't belong to the same chain "
 							+ this + " " + sourcePort.getMediator().getChain());
 			return null;
 		}
 		if (!sourcePort.getPortType().equals(PortType.OUTPUT)) {
 			throw new RuntimeException(
 					"PortImpl type not compatible, it must be PortType.OUTPUT");
 		}
 		if (!targetPort.getPortType().equals(PortType.INPUT)) {
 			throw new RuntimeException(
 					"PortImpl type not compatible, it must be PortType.INPUT");
 		}
 		// set the ports.
 		((BindingImpl) binding).setSourcePort(sourcePort);
 		((BindingImpl) binding).setTargetPort(targetPort);
 		try {
 			mutex.writeLock().acquire();
 		} catch (InterruptedException e) {
 		}
 		result = bindings.add(binding);
 		mutex.writeLock().release();
 		if (result) {
 			setChanged();
 			notifyObservers(new UpdateEvent(UpdateActions.ADD_BINDING, binding));
 		}
 		return binding;
 	}
 
 	public Binding bind(Port inoutPort, Binding binding) {
 		if (inoutPort.getPortType().equals(PortType.INPUT)) {
 			return inputBind(inoutPort, binding);
 		} else {
 			return outputBind(inoutPort, binding);
 		}
 	}
 
 	public Binding inputBind(Port inPort, Binding binding) {
 		boolean result = false;
 		if (!inPort.getPortType().equals(PortType.INPUT)) {
 			throw new RuntimeException(
 					"PortImpl type not compatible, it must be PortType.INPUT");
 		}
 		((BindingImpl) binding).setTargetPort(inPort);
 		try {
 			mutex.writeLock().acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		result = bindings.add(binding);
 		mutex.writeLock().release();
 		if (result == true) {
 			setChanged();
 			notifyObservers(new UpdateEvent(UpdateActions.ADD_BINDING, binding));
 		}
 		return binding;
 	}
 
 	public Binding outputBind(Port outPort, Binding binding) {
 		boolean result = false;
 		if (!outPort.getPortType().equals(PortType.OUTPUT)) {
 			throw new RuntimeException(
 					"PortImpl type not compatible, it must be PortType.OUTPUT");
 		}
 
 		((BindingImpl) binding).setSourcePort(outPort);
 		try {
 			mutex.writeLock().acquire();
 		} catch (InterruptedException e) {
 		}
 		result = bindings.add(binding);
 		mutex.writeLock().release();
 		if (result == true) {
 			setChanged();
 			notifyObservers(new UpdateEvent(UpdateActions.ADD_BINDING, binding));
 		}
 		return binding;
 	}
 
 	private void unbind(Binding[] bs) {
 		if (bs != null) {
 			for (int i = 0; i < bs.length; i++) {
 				unbind(bs[i]);
 			}
 		}
 	}
 
 	private void unlinkBindingsFromChain(Binding[] bindingsToUnlink) { // called
 																		// in a
 																		// synchronized
 																		// block.
 		if (bindingsToUnlink != null) {
 			try {
 				mutex.writeLock().acquire();
 			} catch (InterruptedException e) {
 			}
 			for (int i = 0; i < bindingsToUnlink.length; i++) {
 				bindings.remove(bindingsToUnlink[i]);
 			}
 			mutex.writeLock().release();
 		}
 	}
 
 	/**
 	 * Remove the binding added to the given mediators.
 	 * 
 	 * @param sourceMediator
 	 *            the source mediator.
 	 * @param targetMediator
 	 *            the target mediator.
 	 * @return true if the binding was removed, false if there was any binding
 	 *         to remove.
 	 */
 	public boolean unbind(MediatorComponent sourceMediator,
 			MediatorComponent targetMediator) {
 		Binding bindingsToRemove[] = null;
 		boolean result = false;
 		bindingsToRemove = getBindings(sourceMediator, targetMediator);
 		unlinkBindingsFromChain(bindingsToRemove);
 		if (bindingsToRemove != null) {// Notify to the mediators to remove
 										// bindings.
 			unbind(bindingsToRemove);
 			result = true;
 		}
 		return result;
 	}
 
 	/**
 	 * Remove the binding added to the given mediators.
 	 * 
 	 * @param sourceMediatorId
 	 *            the source mediator identificator.
 	 * @param targetMediatorId
 	 *            the target mediator identificator.
 	 * @return true if the binding was removed, false if there was any binding
 	 *         to remove.
 	 */
 	public boolean unbind(String sourceMediatorId, String targetMediatorId) {
 		return unbind(getMediator(sourceMediatorId),
 				getMediator(targetMediatorId));
 	}
 
 	/**
 	 * Remove the given binding.
 	 * 
 	 * @param binding
 	 *            to remove.
 	 * @return true if the binding was removed, false if there was any binding
 	 *         to remove.
 	 */
 	public boolean unbind(Binding binding) {
 		if (binding != null) {
 			try {
 				mutex.writeLock().acquire();
 			} catch (InterruptedException e) {
 			}
 			bindings.remove(binding);
 			mutex.writeLock().release();
 			MediatorComponentImpl outM = (MediatorComponentImpl) binding
 					.getSourceMediator();
 			MediatorComponentImpl inM = (MediatorComponentImpl) binding
 					.getTargetMediator();
 			inM.removeInBinding(binding);
 			outM.removeOutBinding(binding);
 			setChanged();
 			notifyObservers(new UpdateEvent(UpdateActions.REMOVE_BINDING,
 					binding));
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Obtain an array of all the bindings asociated to the given mediators.
 	 * 
 	 * @param source
 	 *            source mediator which contains the searched bindings.
 	 * @param target
 	 *            target mediator which contains the searched bindings.
 	 * @return the array of bindings.
 	 */
 	public Binding[] getBindings(MediatorComponent source,
 			MediatorComponent target) {
 		List result = new ArrayList();
 		List bindings = new ArrayList();
 		Binding[] sourceB = null;
 		Binding[] targetB = null;
 		Binding[] allBindings = null;
 		if (source != null) {
 			sourceB = source.getOutBindings();
 		}
 		if (target != null) {
 			targetB = target.getInBindings();
 		}
 
 		if (sourceB != null && targetB != null) {
			bindings.addAll(Arrays.asList(sourceB));
 			bindings.addAll(Arrays.asList(targetB));
 			// See if the bindings are connected (the same binding to both
 			// mediators)
 			if (bindings != null) {
 				Iterator it = bindings.iterator();
 				while (it.hasNext()) {
 					BindingImpl c = (BindingImpl) it.next();
 					if ((c.getSourceMediator().getId()
 							.compareTo(source.getId()) == 0)
 							&& (c.getTargetMediator().getId()
 									.compareTo(target.getId()) == 0)) {
 						result.add(c);
 					}
 				}
 			}
 			allBindings = (BindingImpl[]) result.toArray(new BindingImpl[result
 					.size()]);
 			// } else {
 
 			// if (sourceB !=null && targetB == null) {
 			// allBindings = sourceB;
 			// } else if (targetB != null && sourceB == null) {
 			// allBindings = targetB;
 		}
 		return allBindings;
 	}
 
 	public void dispose() {
 		super.dispose();
 		try {
 			mutex.writeLock().acquire();
 		} catch (InterruptedException e) {
 		}
 		this.adapters.clear();
 		this.adapters = null;
 		this.mediators.clear();
 		this.mediators = null;
 		this.bindings.clear();
 		this.bindings = null;
 		mutex.writeLock().release();
 	}
 
 	public String toString() {
 		StringBuffer toShow = new StringBuffer("{ ").append("\n");
 		toShow.append("Chain : ").append(getId()).append(",").append("\n");
 		Set mediators = getMediators();
 		Set adapters = getAdapters();
 		Set bindings = getBindings();
 		Iterator itm = mediators.iterator();
 		Iterator ita = adapters.iterator();
 		Iterator itb = bindings.iterator();
 		// Add the mediator id's to the StringBuffer.
 		if (itm.hasNext()) {
 			toShow.append("Mediators : [\n");
 			while (itm.hasNext()) {
 				MediatorImpl mediator = (MediatorImpl) itm.next();
 				toShow.append(mediator.getId());
 				toShow.append(",\n");
 			}
 			toShow.delete(toShow.length() - 2, toShow.length() - 1);
 			toShow.append("],\n");
 		}
 		// Add the AdapterImpl id's to the StringBuffer.
 		if (ita.hasNext()) {
 			toShow.append("Adapters : {\n");
 			List<String> inonly = new ArrayList<String>();
 			List<String> outonly = new ArrayList<String>();
 			List<String> inout = new ArrayList<String>();
 			List<String> unassigned = new ArrayList<String>();
 			while (ita.hasNext()) {
 				AdapterImpl adapter = (AdapterImpl) ita.next();
 				if (adapter.getPattern() == PatternType.IN_ONLY) {
 					inonly.add(adapter.getId());
 				} else if (adapter.getPattern() == PatternType.OUT_ONLY) {
 					outonly.add(adapter.getId());
 				} else if (adapter.getPattern() == PatternType.IN_OUT) {
 					inout.add(adapter.getId());
 				} else {
 					unassigned.add(adapter.getId());
 				}
 			}
 
 			toShow.append("\"in-only\" : ").append(inonly).append(",\n");
 
 			toShow.append("\"out-only\" : ").append(outonly).append(",\n");
 
 			toShow.append("\"in-out\" : ").append(inout).append(",\n");
 
 			if (!unassigned.isEmpty()) {
 				toShow.append("\"unknown\" : ").append(unassigned)
 						.append(",\n");
 			}
 			toShow.delete(toShow.length() - 2, toShow.length() - 1);
 			toShow.append("},\n");
 		}
 		if (itb.hasNext()) {
 			toShow.append("Bindings : [\n");
 			while (itb.hasNext()) {
 				BindingImpl binding = (BindingImpl) itb.next();
 				toShow.append(binding);
 				toShow.append(",\n");
 			}
 			toShow.delete(toShow.length() - 2, toShow.length() - 1);
 			toShow.append("],\n");
 		}
 		toShow.delete(toShow.length() - 2, toShow.length() - 1);
 		toShow.append("}");
 		return toShow.toString();
 	}
 
 }
