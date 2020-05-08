 package n3phele.service.lifecycle;
 /**
  * (C) Copyright 2010-2013. Nigel Cook. All rights reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  * 
  * Licensed under the terms described in LICENSE file that accompanied this code, (the "License"); you may not use this file
  * except in compliance with the License. 
  * 
  *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on 
  *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
  *  specific language governing permissions and limitations under the License.
  */
 import static com.googlecode.objectify.ObjectifyService.ofy;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.logging.Level;
 
 import n3phele.service.core.NotFoundException;
 import n3phele.service.model.Action;
 import n3phele.service.model.ActionState;
 import n3phele.service.model.CloudProcess;
 import n3phele.service.model.SignalKind;
 import n3phele.service.model.core.Helpers;
 import n3phele.service.model.core.User;
 import n3phele.service.rest.impl.ActionResource;
 import n3phele.service.rest.impl.CloudProcessResource;
 import n3phele.service.rest.impl.UserResource;
 
 import com.google.appengine.api.taskqueue.DeferredTask;
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.appengine.api.taskqueue.TaskOptions;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.VoidWork;
 import com.googlecode.objectify.Work;
 
 public class ProcessLifecycle {
 	final private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(ProcessLifecycle.class.getName()); 
 
 	protected ProcessLifecycle() {}
 	
 	public CloudProcess createProcess(User user, String name, n3phele.service.model.Context context, List<URI> dependency, CloudProcess parent, boolean topLevel, Class<? extends Action> clazz) throws IllegalArgumentException {
 		Action action;
 		try {
 			if(Helpers.isBlankOrNull(name))
 				name = user.getLastName();
 			action = clazz.newInstance().create(user.getUri(), name, context);
 		} catch (InstantiationException e) {
 			log.log(Level.SEVERE, "Class "+clazz, e);
 			throw new IllegalArgumentException(e);
 		} catch (IllegalAccessException e) {
 			log.log(Level.SEVERE, "Class "+clazz, e);
 			throw new IllegalArgumentException(e);
 		}
 		ActionResource.dao.add(action);	
 		CloudProcess process = new CloudProcess(user.getUri(), name, parent, topLevel, action);
 		CloudProcessResource.dao.add(process);
 		action.setProcess(process.getUri());
 		String contextName = action.getContext().getValue("name");
 		if(Helpers.isBlankOrNull(contextName)) {
 			action.getContext().putValue("name", name);
 		}
 		ActionResource.dao.update(action);
 		setDependentOn(process.getUri(), dependency);
 		return process;
 	}
 	
 	/*
 	 * Process execution management
 	 */
 	
 	/** Refresh the status of active actions
 	 * @return Map of states of active cloud processes, and count of processes in those states.
 	 */
 	public Map<String, Long> periodicScheduler() {
 		Map<String, Long> counter = new HashMap<String, Long>();
 		
 		for(CloudProcess process : CloudProcessResource.dao.getNonfinalized()) {
 			ActionState processState = process.getState();
 			String active = process.getRunning()!=null?"_Running":"";
 			String wait = process.getWaitTimeout()!=null?"_Wait":"";
 			Long count = 0L;
 			if(counter.containsKey(processState.toString()+active+wait))
 				count = counter.get(processState.toString()+active+wait);
 			count = count + 1;
 			counter.put(processState.toString()+active+wait, count);
 			log.info("Process "+process.getUri()+" "+processState+" "+process.getRunning());
 			if(process.getRunning() == null && processState != ActionState.NEWBORN) {
 				if(process.hasPending() || processState == ActionState.RUNABLE)
 					schedule(process, false);
 			} 
 		} 
 		
 		return counter;
 	}
 	
 	/** Places a task on to the run queue
 	 * @param process
 	 * @return TRUE if process queued
 	 */
 	public boolean schedule(CloudProcess process, final boolean forceWrite) {
 		final URI processURI = process.getUri();
 		final Long processId = process.getId();
 		final Key<CloudProcess> processRoot = process.getRoot();
 		return CloudProcessResource.dao.transact(new Work<Boolean>() {
 
 			@Override
 			public Boolean run() {
 				log.info(">>>>>>>>>>Schedule "+processURI);
 				boolean result = false;
 				boolean dirty = forceWrite;
 				CloudProcess process = CloudProcessResource.dao.load(processRoot, processId);
 				if(process.getRunning() == null && !process.isFinalized()) {
 					if(process.getWaitTimeout() != null) {
 						Date now = new Date();
 						if(process.hasPendingAssertions() || now.after(process.getWaitTimeout())) {
 							process.setWaitTimeout(null);
 							dirty = true;
 						}
 					}
 					if(process.getState().equals(ActionState.RUNABLE) && !process.isPendingCall() 
 							&& !(process.isPendingCancel() || process.isPendingDump())
 							&& process.hasPendingAssertions() || process.getWaitTimeout() == null ) {
 						dirty = true;
 						log.info("===========>Pending call of "+processURI);
 						process.setPendingCall(true);
 					}
 					if(process.hasPending()) {
 						Date stamp = new Date();
 						log.info("Queued "+processURI+" "+stamp);
 						process.setRunning(stamp);
 						QueueFactory.getDefaultQueue().add(ofy().getTxn(),
 								TaskOptions.Builder.withPayload(new Schedule(processURI, process.getRunning())));
 						result = true;
 						dirty = true;
 					}
 				}
 				if(dirty)
 					CloudProcessResource.dao.update(process);
 				log.info("<<<<<<<<Schedule "+processURI);
 				return result;
 			}});
 		
 	}
 	private static class Schedule implements DeferredTask {
 		private static final long serialVersionUID = 1L;
 		final private URI process;
 		final private Date stamp;
 		public Schedule(URI process, Date stamp) {
 			this.process = process;
 			this.stamp = stamp;
 		}
 
 		@Override
 		public void run(){
 			try {
 				boolean redispatch = true;
 				while(redispatch) {
 					CloudProcessResource.dao.clear();
 					redispatch = mgr().dispatch(process, stamp);
 				}
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "Dispatch exception", e);
 				mgr().dump(process);
 			}
 		}
 	}
 	
 	
 	
 	/** Dispatches execution to a task's entry points
 	 * @param processId
 	 * @return true if additional dispatch requested
 	 */
 	private boolean dispatch(final URI processId, final Date stamp) {
 		
 		NextStep dispatchCode = CloudProcessResource.dao.transact(new Work<NextStep>() {
 		    public NextStep run() {
 		    	log.info(">>>>>>>>>Dispatch "+processId);
 		        CloudProcess process = CloudProcessResource.dao.load(processId);
 				if(process.isFinalized()) {
 					log.warning("Processing called on process "+processId+" finalized="+process.isFinalized()+" state="+process.getState());
 					log.info("<<<<<<<<Dispatch "+processId);
 					return new NextStep(DoNext.nothing);
 				}
 				
 				if(!process.getRunning().equals(stamp)) {
 					log.severe("Processing stamp is "+process.getRunning()+" expected "+stamp);
 					log.info("<<<<<<<<Dispatch "+processId);
 					return new NextStep(DoNext.nothing);
 				}
 				
 				if(process.isPendingCancel() || process.isPendingDump()) { 
 					process.setState(ActionState.CANCELLED);
 			       CloudProcessResource.dao.update(process);
 			        if(process.isPendingCancel()) { 
 			        	process.setPendingCancel(false);
 			        	process.setPendingDump(false);
 			        	log.info("<<<<<<<<Dispatch "+processId);
 			        	return new NextStep(DoNext.cancel);
 			        } else {
 			        	process.setPendingCancel(false);
 			        	process.setPendingDump(false);
 			        	log.info("<<<<<<<<Dispatch "+processId);
 			        	return new NextStep(DoNext.dump);
 			        } 
 				} else if(process.isPendingInit()) {
 					process.setPendingInit(false);
 					process.setStart(new Date());
 			        CloudProcessResource.dao.update(process);
 			        log.info("<<<<<<<<Dispatch "+processId);
 			        return new NextStep(DoNext.init);
 				} else if(process.hasPendingAssertions()) {
 						ArrayList<String> assertions = new ArrayList<String>(process.getPendingAssertion().size());
 						assertions.addAll(process.getPendingAssertion());
 						process.getPendingAssertion().clear();
 						CloudProcessResource.dao.update(process);
 						log.info("<<<<<<<<Dispatch "+processId);
 						return new NextStep(DoNext.assertion, assertions);
 				} else if(process.isPendingCall()) {
 						process.setPendingCall(false);
 						CloudProcessResource.dao.update(process);
 						log.info("<<<<<<<<Dispatch "+processId);
 						return new NextStep(DoNext.call);	
 				}
 				log.info("<<<<<<<<Dispatch "+processId);
 		        return new NextStep(DoNext.nothing);
 		} });
 		CloudProcess process = CloudProcessResource.dao.load(processId);
 		Action task = null;
 		try {
 			task = ActionResource.dao.load(process.getAction());
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "Failed to access task "+process.getAction(), e);
 			toFailed(process);
 			return false;
 		}
 		log.info("Dispatch "+dispatchCode+": process "+process.getName()+" "+process.getUri()+" task "+task.getUri());
 		
 		boolean error = false;
 		switch(dispatchCode.todo){
 		case assertion:
 				for(String assertion : dispatchCode.assertion) {
 					try {
 						int index = assertion.indexOf(":");
 						SignalKind kind = SignalKind.valueOf(assertion.substring(0,index));
 						task.signal(kind, assertion.substring(index+1));
 					} catch (Exception e) {
 						log.log(Level.WARNING, "Assertion "+ assertion + " exception for process "+process.getUri()+" task "+task.getUri(), e);
 					}
 				}
 			writeOnChange(task);
 			return endOfTimeSlice(process);
 
 		case init:
 			try {
 				task.init();
 			} catch (Exception e) {
 				log.log(Level.WARNING, "Init exception for process "+process.getUri()+" task "+task.getUri(), e);
 				error = true;
 			}
 			writeOnChange(task);
 			if(error) {
 				toFailed(process);
 			} else {
 				return endOfTimeSlice(process);
 			}
 			break;
 		case call:
 			boolean complete = false;
 			try {
 				complete = task.call();
 			} catch (WaitForSignalRequest e) {
 				toWait(process, e.getTimeout());
 			} catch (Exception e) {
 				log.log(Level.WARNING, "Call exception for process "+process.getUri()+" task "+task.getUri(), e);
 				error = true;
 			}
 			writeOnChange(task);
 			if(error) {
 				toFailed(process);
 				break;
 			} else if(complete) {
 				toComplete(process);
 			} else {
 				return endOfTimeSlice(process);
 			}
 			break;
 		case cancel:
 			try {
 				task.cancel();
 			} catch (Exception e) {
 				log.log(Level.WARNING, "Cancel exception for process "+process.getUri()+" task "+task.getUri(), e);
 			}
 			writeOnChange(task);
 			toCancelled(process);
 			break;
 		case dump:
 			try {
 				task.dump();
 			} catch (Exception e) {
 				log.log(Level.WARNING, "Dump exception for process "+process.getUri()+" task "+task.getUri(), e);
 			}
 			writeOnChange(task);
 			toCancelled(process);
 			break;
 		case nothing:
 		default:
 			log.severe("******Nothing to do for "+process.getName()+":"+process.toString());
 			// Likely concurrency bug. Process gets dispatched twice.
 			break;
 		}
 		return false;
 		
 	}
 	
 	/* -------------------------------------------------------------------------------------------------
 	 * The following routines manage the lifecycle changes associated with the transaction of a process
 	 * to a particular state.
 	 * ------------------------------------------------------------------------------------------------
 	 */
 	
 	/** Moves a process to the "complete" state, signifying error free completion of processing
 	 * @param process
 	 */
 	private void toComplete(final CloudProcess process) {
 		final Long processId = process.getId();
 		final Key<CloudProcess> processRoot = process.getRoot();
 		log.info("Complete "+process.getName()+":"+process.getUri());
 		giveChildrenToGrandparent(process);
 		final List<String> dependents = CloudProcessResource.dao.transact(new Work<List<String>>() {
 			@Override
 			public List<String> run() {
 				log.info(">>>>>>>>>toComplete "+processId);
 				CloudProcess targetProcess = CloudProcessResource.dao.load(processRoot, processId);
 				logExecutionTime(targetProcess);
 				targetProcess.setState(ActionState.COMPLETE);
 				targetProcess.setComplete(new Date());
 				targetProcess.setRunning(null);
 				targetProcess.setFinalized(true);
 				List<String> result = new ArrayList<String>();
 				result.add(Helpers.URItoString(targetProcess.getParent()));
 				if(targetProcess.getDependencyFor() != null) {
 					result.addAll(targetProcess.getDependencyFor());
 				}
 				CloudProcessResource.dao.update(targetProcess);
 				log.info("<<<<<<<<toComplete "+processId);
 				return result;
 			}
 		});
 		URI parentURI = Helpers.stringToURI(dependents.get(0));
 		if(parentURI != null) {
 			CloudProcess parent;
 			try {
 				parent = CloudProcessResource.dao.load(parentURI);
 				signal(parent, SignalKind.Ok, process.getUri().toString());
 			} catch (NotFoundException e) {
 				log.severe("Unknown parent "+ process);
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "Signal failure to "+parentURI+" "+process, e);
 			}
 		}
 		if(dependents != null && dependents.size() > 1) {
 			for(String dependent : dependents.subList(1, dependents.size())) {
 				signalDependentProcessIsComplete(process, URI.create(dependent));
 			}
 		}
 	}
 	
 	/** Signals to a process that its dependency has now successfully completed
 	 * @param process that completed successfully
 	 * @param depender process awaiting dependency completion
 	 */
 	private void signalDependentProcessIsComplete(CloudProcess process, final URI depender ) {
 		final URI processUri = process.getUri();
 		CloudProcessResource.dao.transact(new VoidWork(){
 			@Override
 			public void vrun() {
 			log.info(">>>>>>>>>signalDependentProcessIsComplete "+processUri);
 			String hasFinalized = processUri.toString();
 			CloudProcess dprocess = CloudProcessResource.dao.load(depender);
 			if(dprocess.getDependentOn() != null && dprocess.getDependentOn().size() > 0) {
 				boolean found = dprocess.getDependentOn().remove(hasFinalized);
 				if(found) {
 					if(dprocess.getDependentOn().size() == 0 && dprocess.getState() != ActionState.NEWBORN && !dprocess.isFinalized()) {
 						if(dprocess.getState() == ActionState.INIT) {
 								dprocess.setPendingInit(true);
 						} 
 						dprocess.setState(ActionState.RUNABLE);
 						schedule(dprocess, true);
 					}
 				} else {
 					if(dprocess.getState() == ActionState.NEWBORN){
 						log.warning("**HANDLED RACE CONDITION** Dependency "+hasFinalized+" not found in "+dprocess.getUri());
 					} else {
 						log.severe("Dependency "+hasFinalized+" not found in "+dprocess.getUri());
 					}
 					
 				}
 			}
 			log.info("<<<<<<<<signalDependentProcessIsComplete "+processUri);
 		}});
 		
 	}
 	
 	/** Blocks process execution block until successful completion of dependent process
 	 * @param process the process to block awaiting successful completion
 	 * @param dependent the process that when complete unblocks execution of process 
 	 * @return true if the dependency causes the process to block
 	 * @throws IllegalArgumentException
 	 */
 	public boolean addDependentOn(final CloudProcess process, CloudProcess dependent) throws IllegalArgumentException {
 		return setDependentOn(process.getUri(), Arrays.asList(dependent.getUri()));
 	}
 	
 	/** Adds a set of dependency such that process runs dependent on the successful execution of dependentOnList members
 	 * @param process 
 	 * @param dependentOnList
 	 * @return TRUE if dependency causes process execution to block
 	 * @throws IllegalArgumentException if the dependency is non-existent or has terminated abnormally
 	 */
 	public boolean setDependentOn(final URI process, List<URI> dependentOnList) {
 		if(dependentOnList == null || dependentOnList.isEmpty()) return false;
 		boolean result = false;
 		int chunkSize = 4;
 		for(int i = 0; i < dependentOnList.size(); i += chunkSize) {
 			int lim = (i + chunkSize) < dependentOnList.size()? i+ chunkSize : dependentOnList.size();
 			boolean chunkResult = setListOfDependentOn(process, dependentOnList.subList(i, lim));
 			result = result || chunkResult;
 		}
 
 		if(result)
 			CloudProcessResource.dao.transact(new VoidWork(){
 	
 				@Override
 				public void vrun() {
 					CloudProcess targetProcess = CloudProcessResource.dao.load(process);
 					if(!targetProcess.isFinalized()) {
 						if(targetProcess.getDependentOn() != null && targetProcess.getDependentOn().size() > 0 &&
 								targetProcess.getRunning()==null &&
 								targetProcess.getState().equals(ActionState.RUNABLE)) {
 							targetProcess.setState(ActionState.BLOCKED);
 							targetProcess.setWaitTimeout(null);
 							CloudProcessResource.dao.update(targetProcess);
 						}
 					} 
 				}
 			});
 
 		return result;
 
 	}
 	
 	/** Adds a set of dependency such that process runs dependent on the successful execution of dependentOnList members
 	 * @param processUri 
 	 * @param dependentOnList
 	 * @return TRUE if dependency causes process execution to block
 	 * @throws IllegalArgumentException if the dependency is non-existent or has terminated abnormally
 	 */
 	private boolean setListOfDependentOn(final URI processUri, final List<URI> dependentOnList) {
 		
 		return CloudProcessResource.dao.transact(new Work<Boolean>(){
 
 			@Override
 			public Boolean run() {
 				log.info(">>>>>>>>>setListOfDependentOn "+processUri);
 				boolean willBlock = false;
 				CloudProcess depender = CloudProcessResource.dao.load(processUri);
 				if(depender.isFinalized()) {
 					log.warning("Cannot add dependencies to finalized process "+processUri+" state "+depender.getState());
 					throw new IllegalArgumentException("Cannot add dependencies to finalized process "+processUri+" state "+depender.getState());
 				}
 				for(URI dependentOn : dependentOnList) {
 					CloudProcess dependency;
 					try {
 						dependency = CloudProcessResource.dao.load(dependentOn);
 					} catch (NotFoundException e) {
 						throw new IllegalArgumentException("Dependency does exist "+dependentOn,e);
 					}
 					if(dependency.isFinalized()) {
 						if(dependency.getState().equals(ActionState.COMPLETE)) {
 							log.warning(dependentOn+" already finalized, removing dependency constraint from "+processUri);
 						} else {
 							throw new IllegalArgumentException("Process "+processUri+" has a dependency on "+dependentOn+" which is "+dependency.getState());
 						}
 					} else {
 						if(!depender.getDependentOn().contains(dependency.getUri().toString())) {
 							dependency.getDependencyFor().add(depender.getUri().toString());
 							depender.getDependentOn().add(dependency.getUri().toString());
 							CloudProcessResource.dao.update(dependency);
 							willBlock = true;
 						} else {
 							log.severe(dependentOn+" already in list for "+processUri);
 						}
 						
 					}
 				}
 				CloudProcessResource.dao.update(depender);
 				log.info("<<<<<<<<setListOfDependentsOn "+processUri);
 				return willBlock;
 			}});
 	}
 		
 	/** Set process to having terminated with processing failure. The process parent is notified of
 	 * the failure.
 	 * @param process
 	 */
 	private void toFailed(CloudProcess process) {
 		final Long processId = process.getId();
 		final Key<CloudProcess> processRoot = process.getRoot();
 		giveChildrenToGrandparent(process);
 		List<String> dependents = CloudProcessResource.dao.transact(new Work<List<String>>() {
 
 			@Override
 			public List<String> run() {
 				log.info(">>>>>>>>>toFailed "+processId);
 				CloudProcess targetProcess = CloudProcessResource.dao.load(processRoot, processId);
 				List<String> result = null;
 				if(!targetProcess.isFinalized()) {
 					logExecutionTime(targetProcess);
 					targetProcess.setState(ActionState.FAILED);
 					targetProcess.setComplete(new Date());
 					targetProcess.setRunning(null);
 					targetProcess.setFinalized(true);
 					result = new ArrayList<String>();
 					result.add(Helpers.URItoString(targetProcess.getParent()));
 					if(targetProcess.getDependencyFor() != null) {
 						result.addAll(targetProcess.getDependencyFor());
 					}
 					CloudProcessResource.dao.update(targetProcess);
 				} else {
 					log.warning("Failed process "+targetProcess.getUri()+" is finalized");
 				}
 				log.info("<<<<<<<<toFailed "+processId);
 				return result;
 			}});
 		URI parentURI = Helpers.stringToURI(dependents.get(0));
 		if(parentURI != null) {
 			CloudProcess parent;
 			try {
 				parent = CloudProcessResource.dao.load(parentURI);
 				signal(parent, SignalKind.Failed, process.getUri().toString());
 			} catch (NotFoundException e) {
 				log.severe("Unknown parent "+ process);
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "Signal failure to "+parentURI+" "+process, e);
 			}
 		}
 		if(dependents != null && dependents.size() > 1) {
 			for(String dependent : dependents.subList(1, dependents.size())) {
 				CloudProcess dprocess = CloudProcessResource.dao.load(URI.create(dependent));
 				if(!dprocess.isFinalized()) {
 					toCancelled(dprocess);
 				}
 			}
 		}
 	}
 	
 	private void giveChildrenToGrandparent(final CloudProcess process) {
 		URI uri = process.getParent();
 		CloudProcess grandParent=null;
 		while(uri != null) {
 			grandParent = CloudProcessResource.dao.load(uri);
 			if(!grandParent.isFinalized()) break;
 			uri = grandParent.getParent();
 		}
 		final URI grandParentURI = uri;
 		/*
 		 * 
 		 */
 		boolean moreToDo = true;
 		while(moreToDo) {
 			moreToDo = CloudProcessResource.dao.transact(new Work<Boolean>(){
 			@Override
 			public Boolean run() {
 				log.info(">>>>>>>>>giveChildToGrandparent "+grandParentURI);
 				boolean goAgain = false;
 				CloudProcess grandParent = null;
 				if(grandParentURI != null) {
 					grandParent = CloudProcessResource.dao.load(grandParentURI);
 					if(grandParent.isFinalized()) {
 						log.warning("Grandparent "+grandParent.getUri()+" is finalized");
 						throw new IllegalArgumentException("Grandparent "+grandParent.getUri()+" is finalized");
 					}
 				}
 				List<CloudProcess> children = getNonfinalizedChildren(process);
 				boolean parentUpdate = false;
 				int chunk = 4;
 				for(CloudProcess childProcess : children) {
 					if(chunk-- < 1) {
 						goAgain = true;
 						break;
 					}
 					if(grandParent != null) {
 						childProcess.setParent(grandParentURI);
 						CloudProcessResource.dao.update(childProcess);
 						final String typedAssertion = SignalKind.Adoption+":"+childProcess.getUri().toString();
 						if(!grandParent.getPendingAssertion().contains(typedAssertion)) {
 							grandParent.getPendingAssertion().add(typedAssertion);
 							parentUpdate = true;
 						}
 					} else {
 						childProcess.setPendingCancel(true);
 						childProcess.setParent(null);
 						schedule(childProcess, true);
 					}
 				}
 				if(parentUpdate) {
 					CloudProcessResource.dao.update(grandParent);
 				}
 				log.info("<<<<<<<<giveChildToGrandparent "+grandParentURI);
 				return goAgain;
 			}});
 		}
 		if(grandParentURI != null)
 			schedule(grandParent, false);
 		
 	}
 	
 	
 	/** Set process to the wait state
 	 * 
 	 * @param process
 	 */
 	private void toWait(CloudProcess process, final Date timeout) {
 		final Long processId = process.getId();
 		final Key<CloudProcess> processRoot = process.getRoot();
 		CloudProcessResource.dao.transact(new VoidWork() {
 
 			@Override
 			public void vrun() {
 				log.info(">>>>>>>>>toWait "+processId);
 				CloudProcess targetProcess = CloudProcessResource.dao.load(processRoot, processId);
 				if(!targetProcess.isFinalized()) {
 					targetProcess.setWaitTimeout(timeout);
 					CloudProcessResource.dao.update(targetProcess);
 				} else {
 					log.warning("Wait process "+targetProcess.getUri()+" is finalized");
 				}
 				log.info("<<<<<<<<toWait "+processId);
 			}});
 
 	}
 	
 	/** Re-evaluate process state at normal end of execution. 
 	 * @param process
 	 * @return true if redispatch requested
 	 */
 	private boolean endOfTimeSlice(CloudProcess process) {
 		final Long processId = process.getId();
 		final Key<CloudProcess> processRoot = process.getRoot();
 		boolean redispatch = CloudProcessResource.dao.transact(new Work<Boolean>() {
 
 			@Override
 			public Boolean run() {
 				log.info(">>>>>>>>>endOfTimeSlice "+processId);
 				boolean redispatch = false;
 				CloudProcess targetProcess = CloudProcessResource.dao.load(processRoot, processId);
 				if(!targetProcess.isFinalized()) {
 					if(targetProcess.getState() == ActionState.RUNABLE && targetProcess.hasPending()) {
 						targetProcess.setWaitTimeout(null);
 						long now = new Date().getTime(); 
 						if(targetProcess.getRunning().getTime()+(60*1000) < now) {
 							// process has run too long .. re queue
 							logExecutionTime(targetProcess);
 							targetProcess.setRunning(null);
 							log.warning("Re-queue process "+targetProcess.getId());
 							schedule(targetProcess, true);
 						} else {
 							log.info("Re-dispatch process "+targetProcess);
 							redispatch = true;
 						}
 					} else {
 						logExecutionTime(targetProcess);
 						targetProcess.setRunning(null);
 						if(targetProcess.getDependentOn() != null && !targetProcess.getDependentOn().isEmpty()) {
 							targetProcess.setState(ActionState.BLOCKED);
 							targetProcess.setWaitTimeout(null);
 						} 
 						CloudProcessResource.dao.update(targetProcess);
 					}
 				} 
 				log.info("<<<<<<<<endOfTimeSlice "+processId+(redispatch?" -- redispatch":""));
 				return redispatch;
 			}});
 			return redispatch;
 	}
 	
 	
 	/** Move process to cancelled state.
 	 * The process parent is notified of the cancellation.
 	 * @param process
 	 */
 	private void toCancelled(CloudProcess process) {
 		final Map<URI, Set<String>>notifications = new HashMap<URI, Set<String>>();
 		final Set<String> priorDependents = new HashSet<String>();
 		canceller(process, notifications, priorDependents);
 		if(!notifications.isEmpty()) {
 			for(Entry<URI, Set<String>> notification : notifications.entrySet()) {
 				addSignalList(notification.getKey(), SignalKind.Cancel, notification.getValue());
 			}
 		}
 	}
 	
 	private void canceller(CloudProcess process, final Map<URI, Set<String>>notifications, final Set<String> priorDependents) {
 		final Long processId = process.getId();
 		final Key<CloudProcess> processRoot = process.getRoot();
 		Set<String> dependents = CloudProcessResource.dao.transact(new Work<Set<String>>() {
 
 			@Override
 			public Set<String> run() {
 				log.info(">>>>>>>>>toCancelled "+processId);
 				CloudProcess targetProcess = CloudProcessResource.dao.load(processRoot, processId);
 				Set<String> result = null;
 				if(!targetProcess.isFinalized()) {					
 					if(targetProcess.getState().equals(ActionState.BLOCKED)){
 						targetProcess.setPendingCancel(true);
 						schedule(targetProcess, true);
 					} else {
 						logExecutionTime(targetProcess);
 						targetProcess.setState(ActionState.CANCELLED);
 						targetProcess.setComplete(new Date());
 						if(targetProcess.getStart() == null) {
 							targetProcess.setStart(targetProcess.getComplete());
 						}
 						targetProcess.setRunning(null);
 						targetProcess.setFinalized(true);
 						if(targetProcess.getParent() != null) {
 							try {
 								Set<String> notifyList = notifications.get(targetProcess.getParent());
 								if(notifyList == null) {
 									notifyList = new HashSet<String>();
 									notifications.put(targetProcess.getParent(), notifyList);
 								}
 								notifyList.add(targetProcess.getUri().toString());
 
 							} catch (Exception e) {
 								log.log(Level.SEVERE, "Signal failure "+targetProcess, e);
 							}
 						}
 						if(targetProcess.getDependencyFor() != null) {
 							result = new HashSet<String>();
 							for(String target : targetProcess.getDependencyFor()) {
 								if(!priorDependents.contains(target)) {
 									result.add(target);
 								}
 							}
 						}
 						CloudProcessResource.dao.update(targetProcess);
 					}
 					
 				} else {
 					log.warning("Cancelled process "+targetProcess.getUri()+" is finalized");
 				}
 				log.info("<<<<<<<<toCancelled "+processId);
 				return result;
 			}});
 		if(dependents != null && !dependents.isEmpty()) {
 			for(String dependent : dependents) {
 				CloudProcess dprocess;
 				try {
 					dprocess = CloudProcessResource.dao.load(URI.create(dependent));
 					if(!dprocess.isFinalized()) {
 						canceller(dprocess, notifications, priorDependents);
 					}
 				} catch (NotFoundException e) {
 					log.severe("Process "+dependent+" not found");
 				}
 			}
 		}
 	}
 	
 	public CloudProcess spawn(URI owner, String name, n3phele.service.model.Context context, 
 								     List<URI> dependency, URI parentURI, String className) throws IllegalArgumentException, NotFoundException, ClassNotFoundException {
 
 		String canonicalClassName = "n3phele.service.actions."+className+"Action";
 
 		User user;
 		CloudProcess parent = null;
 		try {
 			user = UserResource.dao.load(owner);
 		} catch (NotFoundException e) {
 			log.warning("Cant find owner "+owner);
 			throw e;
 		}
 		
 		try {
 			if(parentURI != null)
 				parent = CloudProcessResource.dao.load(parentURI);
 		} catch (NotFoundException e) {
 			log.warning("Cant find parent "+parentURI);
 			throw e;
 		}
 		
 		CloudProcess process = this.createProcess(user, 
 				name, context, dependency, parent, false,
 				Class.forName(canonicalClassName).asSubclass(Action.class));
 		return process;
 	}
 
 	
 	/** cancel running an existing process.
 	 * Causes the existing process to stop current processing and to close and free any resources that the
 	 * process is currently using.
 	 * 
 	 */
 	public void cancel(CloudProcess process) {
 		log.info("cancel "+process.getUri());
 		final Long processId = process.getId();
 		final Key<CloudProcess> groupId = process.getRoot();
 		CloudProcessResource.dao.transact(new VoidWork() {
 			@Override
 			public void vrun() {
 				log.info(">>>>>>>>>cancel "+processId);
 				CloudProcess targetProcess = CloudProcessResource.dao.load(groupId, processId);
 				if(!targetProcess.isFinalized()) {
 					targetProcess.setPendingCancel(true);
 					schedule(targetProcess, true);
 				} else {
 					log.severe("Cancel on finalized process "+targetProcess.getUri());
 				}
 
 				log.info("<<<<<<<<cancel "+processId);
 			}});
 	}
 	
 	/** cancel running an existing process.
 	 * Causes the existing process to stop current processing and to close and free any resources that the
 	 * process is currently using.
 	 * 
 	 */
 	public void cancel(URI processId) throws NotFoundException {
 		CloudProcess process = CloudProcessResource.dao.load(processId);
 		cancel(process);
 	}
 
 	public void init(CloudProcess process) {
 		log.info("init "+process.getUri()+" "+process.getName());
 		final Long processId = process.getId();
 		final Key<CloudProcess> processRoot = process.getRoot();
 		CloudProcessResource.dao.transact(new VoidWork() {
 			@Override
 			public void vrun() {
 				log.info(">>>>>>>>>init "+processId);
 				CloudProcess targetProcess = CloudProcessResource.dao.load(processRoot, processId);
 				if(!targetProcess.isFinalized() && targetProcess.getState() == ActionState.NEWBORN) {
 					if(targetProcess.getDependentOn() != null && targetProcess.getDependentOn().size() != 0) {
 						targetProcess.setState(ActionState.INIT);
 						CloudProcessResource.dao.update(targetProcess);
 					} else {
 						targetProcess.setState(ActionState.RUNABLE);
 						targetProcess.setPendingInit(true);
 						targetProcess.setPendingCall(true);
 						/*
 						 * NB: There is an assumption that:
 						 * 1. The objectify get operation will fetch the modified process object
 						 *    in the schedule transaction
 						 * 2. The pendingInit will cause schedule to write the CloudProcess object
 						 */
 						schedule(targetProcess, true); 
 					}
 				} else {
 					log.severe("Init on finalized or non-newborn process "+targetProcess.getUri()+" "+targetProcess.getState());
 				}
 				log.info("<<<<<<<<init "+processId);
 			}});
 	}
 	
 	/** Dump a running process, which causes the process to be stopped current processing and 
 	 * to save diagnostic information that
 	 * can be later reviews.
 	 * 
 	 */
 	public CloudProcess dump(CloudProcess process) {
 		final Long processId = process.getId();
 		final Key<CloudProcess> processRoot = process.getRoot();
 		log.info("dump "+process.getUri());
 		return CloudProcessResource.dao.transact(new Work<CloudProcess>() {
 			@Override
 			public CloudProcess run() {
 				log.info(">>>>>>>>>dump "+processId);
 				CloudProcess targetProcess = CloudProcessResource.dao.load(processRoot, processId);
 				if(!targetProcess.isFinalized()) {
 					targetProcess.setPendingDump(true);
 					schedule(targetProcess, true);
 				} else {
 					log.severe("Dump on finalized process "+targetProcess.getUri());
 				}
 
 				log.info("<<<<<<<<dump "+processId);
 				return targetProcess;
 			}});
 		
 	}
 	
 	/** Dump a running process, which causes the process to be stopped current processing and 
 	 * to save diagnostic information that
 	 * can be later reviews.
 	 * 
 	 */
 	public CloudProcess dump(URI processId) throws NotFoundException {
 
 		CloudProcess process = CloudProcessResource.dao.load(processId);
 		return dump(process);
 		
 	}
 	
 	/** 
 	 * @param jobAction
 	 * @throws WaitForSignalRequest 
 	 */
 	public void waitForSignal() throws WaitForSignalRequest {
 		throw new WaitForSignalRequest();
 	}
 	
 	public static class WaitForSignalRequest extends Exception {
 		private static final long serialVersionUID = 1L;
 		private final Date timeout;
 		
 		public WaitForSignalRequest() {
 			this(Calendar.HOUR, 1);
 		}
 		
 		public WaitForSignalRequest(int unit, int count) {
 			Calendar alarm = Calendar.getInstance();
 			alarm.add(unit, count);
 			timeout = alarm.getTime();
 		}
 		
 		public Date getTimeout() {
 			return this.timeout;
 		}
 	}
 	
 
 	/** Signals a process with an assertion.
 	 * @param process
 	 * @param kind
 	 * @param assertion
 	 */
 	public void signal(CloudProcess process, SignalKind kind, String assertion) {
 		this.signal(process.getUri(), kind, assertion);
 	}
 	
 	/** Signals a process with an assertion.
 	 * @param cloudProcessURI
 	 * @param kind
 	 * @param assertion
 	 */
 	public void signal(final URI cloudProcessURI, final SignalKind kind, final String assertion) {
 		addSignalList(cloudProcessURI, kind, Arrays.asList(assertion));
 	}
 	
 	/** Signals a process with an assertion.
 	 * @param cloudProcessURI
 	 * @param kind
 	 * @param assertion
 	 */
 	public void addSignalList(final URI cloudProcessURI, final SignalKind kind, final Collection<String> assertions) {
 		log.info("signal <"+kind+":"+assertions+"> to "+cloudProcessURI);
 		CloudProcessResource.dao.transact(new VoidWork() {
 			@Override
 			public void vrun() {
 				log.info(">>>>>>>>>signalList "+cloudProcessURI);
 				CloudProcess p = CloudProcessResource.dao.load(cloudProcessURI);
 				if(!p.isFinalized()) {
 					boolean added = false;
 					if(!assertions.isEmpty()) {
 						for(String assertion : assertions) {
 							if(!p.getPendingAssertion().contains(kind+":"+assertion)) {
 								p.getPendingAssertion().add(kind+":"+assertion);
 								added = true;
 							}
 						}
 						if(added) {
 							if(p.getState() == ActionState.RUNABLE) {
 								schedule(p, true);
 							} else {
 								CloudProcessResource.dao.update(p);
 							}
 						}	
 					}
 				} else {
 					log.severe("Signal <"+kind+":"+assertions+"> on finalized process "+p.getUri());
 				}
 				log.info("<<<<<<<<signalList "+cloudProcessURI);
 
 			}});
 	}
 	
 	/** Signals a process parent with an assertion.
 	 * @param cloudProcessURI
 	 * @param kind
 	 * @param assertion
 	 */
 	public void signalParent(final URI childProcessURI, final SignalKind kind, final String assertion) {
 		log.info("signal <"+kind+":"+assertion+"> to parent of "+childProcessURI);
 		CloudProcessResource.dao.transact(new VoidWork() {
 			@Override
 			public void vrun() {
 				log.info(">>>>>>>>>signalParent "+childProcessURI);
 				CloudProcess child = CloudProcessResource.dao.load(childProcessURI);
 				if(child.getParent() != null) {
 					log.info("signal <"+kind+":"+assertion+"> to "+child.getParent());
 					CloudProcess p = CloudProcessResource.dao.load(child.getParent());
 					if(!p.isFinalized()) {
 						if(!p.getPendingAssertion().contains(kind+":"+assertion)) {
 							p.getPendingAssertion().add(kind+":"+assertion);
 							if(p.getState() == ActionState.RUNABLE) {
 								schedule(p, true);
 							} else {
 								CloudProcessResource.dao.update(p);
 							}
 						}
 					} else {
 						log.warning("Signal <"+kind+":"+assertion+"> on finalized process "+p.getUri());
 					}
 				} else {
 					log.info("signal <"+kind+":"+assertion+"> "+childProcessURI+" has no parent");
 				}
 				log.info("<<<<<<<<signalParent "+childProcessURI);
 			}});
 	}
 	
 	/*
 	 * Helpers
 	 * =======
 	 */
 	private enum DoNext {
 		nothing,
 		cancel,
 		dump,
 		init,
 		assertion,
 		call
 	}
 	
 	private static class NextStep {
 		public List<String> assertion;
 		public DoNext todo;
 		public NextStep(DoNext todo, List<String> assertion) {
 			this.todo = todo;
 			this.assertion = assertion;
 		}
 		
 		public NextStep(DoNext todo) {
 			this.todo = todo;
 		}
 		
 		public String toString() {
 			if(todo == DoNext.assertion) {
 				return todo+"<"+assertion+">";
 			} else {
 				return ""+todo;
 			}
 		}
 	}
 	
 	
 	private void logExecutionTime(CloudProcess process) {
 		Date started = process.getRunning();
 		if(started != null) {
 			Date now = new Date();
 			Long duration = now.getTime() - started.getTime();
 			log.info(process.getName()+" "+process.getUri()+" executed "+duration+" milliseconds");
 		}
 	}
 	
 	private boolean writeOnChange(final Action action) {
 		boolean result = ActionResource.dao.transact(new Work<Boolean>() {
 			@Override
 			public Boolean run() {
 				log.info(">>>>>>>>>writeOnChange "+action.getUri());
 				Action db = ActionResource.dao.load(action.getUri());
 				if(!db.equals(action)) {
 					ActionResource.dao.update(action);
 					return true;
 				}
 				return false;
 			}});
 		log.info("Action "+action.getName()+" "+action.getUri()+" write "+result);
 		log.info("<<<<<<<<writeOnChange "+action.getUri());
 		return result;
 	}
 	
 	private List<CloudProcess> getNonfinalizedChildren(CloudProcess process) {
 		Key<CloudProcess> root = process.getRoot();
 		if(root == null) {
 			root = Key.create(process);
 		}
 		return ofy().load().type(CloudProcess.class).ancestor(root)
 				.filter("parent", process.getUri().toString())
 				.filter("finalized", false).list();
 	}
 	
 	
 	private final static ProcessLifecycle processLifecycle = new ProcessLifecycle();
 	public static ProcessLifecycle mgr() {
 		return processLifecycle;
 	}
 
 }
