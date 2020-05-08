 /**
  * Copyright (C) 2011 / cube-team <https://cube.forge.osor.eu>
  *
  * Licensed under the Apache License, Version 2.0 (the "License").
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package ch.admin.vbs.cube.core.vm;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import ch.admin.vbs.cube.common.CubeException;
 
 /**
  * The VM model maintains a list of VM for a given session.
  */
 public class VmModel {
 	private ArrayList<Vm> vmList = new ArrayList<Vm>();
 	private HashMap<String, Vm> vmListIndex = new HashMap<String, Vm>();
 	private ArrayList<IVmModelChangeListener> modelListeners = new ArrayList<IVmModelChangeListener>(2);
 	private ArrayList<IVmStateChangeListener> stateListeners = new ArrayList<IVmStateChangeListener>(2);
 
 	public void addVm(Vm vm) throws CubeException {
 		synchronized (vmList) {
 			if (vmListIndex.containsKey(vm.getId())) {
 				throw new CubeException("VM [" + vm.getId() + "] already in the model");
 			} else {
 				vmList.add(vm);
 				vmListIndex.put(vm.getId(), vm);
 				fireModelUpdatedEvent();
 			}
 		}
 	}
 
 	public void removeVm(Vm vm) throws CubeException {
 		synchronized (vmList) {
 			if (vmListIndex.containsKey(vm.getId())) {
 				vmListIndex.remove(vm.getId());
 				vmList.remove(vm);
 				fireModelUpdatedEvent();
 			} else {
 				throw new CubeException("VM [" + vm.getId() + "] not in the model");
 			}
 		}
 	}
 
 	public List<Vm> getVmList() {
 		synchronized (vmList) {
 			return new ArrayList<Vm>(vmList);
 		}
 	}
 
 	public Vm findByInstanceUid(String instanceUid) {
 		synchronized (vmList) {
 			return vmListIndex.get(instanceUid);
 		}
 	}
 
 	public void addModelChangeListener(IVmModelChangeListener l) {
 		synchronized (modelListeners) {
 			modelListeners.add(l);
 		}
 	}
 
 	public void removeModelChangeListener(IVmModelChangeListener l) {
 		synchronized (modelListeners) {
 			modelListeners.remove(l);
 		}
 	}
 
 	public void fireModelUpdatedEvent() {
 		synchronized (modelListeners) {
 			for (IVmModelChangeListener l : modelListeners) {
 				l.listUpdated();
 			}
 		}
 	}
 
 	public void fireVmUpdatedEvent(Vm vm) {
 		synchronized (modelListeners) {
 			for (IVmModelChangeListener l : modelListeners) {
 				l.vmUpdated(vm);
 			}
 		}
 	}
 
 	public void addStateChangeListener(IVmStateChangeListener l) {
 		synchronized (stateListeners) {
 			stateListeners.add(l);
 		}
 	}
 
 	public void removeStateChangeListener(IVmStateChangeListener l) {
 		synchronized (stateListeners) {
 			stateListeners.remove(l);
 		}
 	}
 
 	public void fireVmStateUpdatedEvent(Vm vm) {
 		synchronized (stateListeners) {
 			for (IVmStateChangeListener l : stateListeners) {
				l.vmStateUpdated(vm);
 			}
 		}
 	}
 }
