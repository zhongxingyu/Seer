 /*
  * Copyright (C) 2008 Universidade Federal de Campina Grande
  *  
  * This file is part of Commune. 
  *
  * Commune is free software: you can redistribute it and/or modify it under the
  * terms of the GNU Lesser General Public License as published by the Free 
  * Software Foundation, either version 3 of the License, or (at your option) 
  * any later version. 
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT 
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  * for more details. 
  * 
  * You should have received a copy of the GNU Lesser General Public License 
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package br.edu.ufcg.lsd.commune.processor.interest;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import sun.security.provider.certpath.X509CertPath;
 import br.edu.ufcg.lsd.commune.Module;
 import br.edu.ufcg.lsd.commune.api.FailureNotification;
 import br.edu.ufcg.lsd.commune.api.RecoveryNotification;
 import br.edu.ufcg.lsd.commune.container.InvalidMonitoringException;
 import br.edu.ufcg.lsd.commune.container.ObjectDeployment;
 import br.edu.ufcg.lsd.commune.identification.CommuneAddress;
 import br.edu.ufcg.lsd.commune.identification.ContainerID;
 import br.edu.ufcg.lsd.commune.identification.DeploymentID;
 import br.edu.ufcg.lsd.commune.identification.ServiceID;
 import br.edu.ufcg.lsd.commune.message.Message;
 import br.edu.ufcg.lsd.commune.message.StubParameter;
 
 @SuppressWarnings("restriction")
 public class InterestManager {
 
 	private static final int DEFAULT_INTEREST_DELAY = 500;
 	private static final int THREAD_POOL_SIZE = 10;
 	private static final long DEFAULT_TIMEOUT = 1000;
 
 	
 	private final InterestProcessor interestProcessor;
 	private ScheduledExecutorService executor;
 
 	private Lock interestLock = new ReentrantLock();
 	private Map<MonitoredParameter, Monitor> parameter2Monitor = new HashMap<MonitoredParameter, Monitor>();
 	private Map<ServiceID, Interest> interests = new HashMap<ServiceID, Interest>();
 	
 	private List<TimeoutListener> timeoutListeners = new ArrayList<TimeoutListener>();
 	
 	
 	public InterestManager(InterestProcessor interestProcessor) {
 		this.executor = createExecutor();
 		this.interestProcessor = interestProcessor;
 	}
 
 	
 	public void addListener(TimeoutListener listener) {
 		timeoutListeners.add(listener);
 	}
 	
 	protected ScheduledExecutorService createExecutor() {
 		return Executors.newScheduledThreadPool( THREAD_POOL_SIZE, new InterestThreadFactory());
 	}
 
 	public void addMonitoredParameter(ObjectDeployment deployment, Method method, int parameterIndex, 
 			Class<?> parameterType, String monitorServiceName, InterestRequirements requirements) {
 		
 		Monitor monitor = createMonitor(parameterType, monitorServiceName);
 		MonitoredParameter monitoredParameter = 
 			new MonitoredParameter(deployment.getObject(), method, parameterIndex, parameterType, requirements); 
 		
 		try {
 			interestLock.lock();
 			parameter2Monitor.put(monitoredParameter, monitor);
 
 		} finally {
 			interestLock.unlock();
 		}
 	}
 
 	private Monitor createMonitor(Class<?> monitorableType, String monitorServiceName) {
 		ObjectDeployment monitorDeployment = this.interestProcessor.getMonitorDeployment(monitorServiceName);
 		Method recoveryNotification = getRecoveryNotification(monitorDeployment, monitorableType);
 		Method failureNotification = getFailureNotification(monitorDeployment, monitorableType);
 		return new Monitor(monitorDeployment, recoveryNotification, failureNotification);
 	}
 	
 	private Method getRecoveryNotification(ObjectDeployment monitorDeployment, Class<?> monitorableType) {
 		return getNotificationMethod(monitorDeployment, monitorableType, RecoveryNotification.class, "recovery");
 	}
 
 	private Method getFailureNotification(ObjectDeployment monitorDeployment, Class<?> monitorableType) {
 		return getNotificationMethod(monitorDeployment, monitorableType, FailureNotification.class, "failure");
 	}
 	
 	private Method getNotificationMethod(ObjectDeployment monitorDeployment, Class<?> monitorableType, 
 			Class<? extends Annotation> annotationType, String name) {
 		
 		Class<?> monitorType = monitorDeployment.getObject().getClass();
 		Method notificationMethod = null;
 		
 		for (Method method : monitorType.getMethods()) {
 			
 			if (method.getAnnotation(annotationType) != null) {
 				Class<?>[] parameterTypes = method.getParameterTypes();
 				
 				if (parameterTypes[0].isAssignableFrom(monitorableType)) {
 					notificationMethod = method;
 				}
 			}
 		}
 
 		if (notificationMethod == null) {
 			throw new InvalidMonitoringException("There is not " + name + " notification method for '" + 
 					monitorableType + "' in the monitor '" + monitorDeployment.getDeploymentID().getServiceName() + 
 					"'");
 		}
 		
 		return notificationMethod;
 	}
 
 	public void registerInterest(String monitorName, Class<?> monitorableType, ServiceID stubServiceID, 
 			InterestRequirements requirements) {
 		
 		Monitor monitor = createMonitor(monitorableType, monitorName);
 		registerInterest(stubServiceID, monitor, requirements, false);
 	}
 
 	private Interest registerInterest(ServiceID stubServiceID, Monitor monitor, InterestRequirements requirements, 
 			boolean parameter) {
 		
 		try {
 			interestLock.lock();
 			
 			Interest interest = new Interest(monitor, stubServiceID, requirements);
 			Interest origInterest = interests.get(stubServiceID);
 			
 			if (origInterest == null || origInterest.getInterested().getDeploymentID() == null) {
 				setNewInterest(interest, parameter);
  			
 			} else {
 				DeploymentID origInterestedID = origInterest.getInterested().getDeploymentID();
 				DeploymentID newInterestedID = interest.getInterested().getDeploymentID();
 
 				boolean isNewMonitor = !origInterestedID.equals(newInterestedID);
 				
 				if (isNewMonitor || parameter) {
 					origInterest.cancelScheduledExecution();
 					setNewInterest(interest, parameter);
 				}
  			}
 			return interest;
 			
 		} finally {
 			interestLock.unlock();
 		}
 	}
 
 	private void setNewInterest(Interest interest, boolean replaced) {
 		interests.put(interest.getStubServiceID(), interest);
 		scheduleHBRequest(interest, replaced);
 	}
 	
 	protected void scheduleHBRequest(final Interest interest, boolean parameter) {
 		Runnable hbRequest = createRunnable(interest);
 		
 		if (parameter) {
 			interest.setLastHeartbeat();
 			hbRequest.run(); //Send now the first heartbeat
 		}
 		
 		ScheduledFuture<?> future = 
 			executor.scheduleAtFixedRate(hbRequest, DEFAULT_INTEREST_DELAY, interest.getHeartbeatDelay(), 
 				TimeUnit.MILLISECONDS);
 		interest.setScheduledExecution(future);
 	}
 
 	protected Runnable createRunnable(final Interest interest) {
 		Runnable hbRequest = new Runnable() {
 			public void run() {
 				process(interest);
 			}
 		};
 		return hbRequest;
 	}
 
 	void process(Interest interest) {
 		processTimeout(interest);
 		sendInterestMessage(interest);
 	}
 	
 	private void sendInterestMessage(Interest interest) {
 		
 		Message message = null;
 		
 		try {
 			interestLock.lock();
 			
 			if (isStubDown(interest)) {
 				message = interest.createIsItAliveMessage();
 			} else {
 				
 				if (interest.isTimedOut()) {
 					message = createNotifyFailureMessage(interest);
 				} else {
 					message = interest.createIsItAliveMessage();
 				}
 			}
 			
 		} finally {
 			interestLock.unlock();
 		}
 		
 		
 		if (message != null) {
 			interestProcessor.sendMessage(message);
 		}
 	}
 
 
 	private void processTimeout(Interest interest) {
 		ServiceID stubServiceID = null; 
 		
 		try {
 			interestLock.lock();
 			if (interest.isTimedOut()) {
 				stubServiceID = interest.getStubServiceID();
 			}
 		} finally {
 			interestLock.unlock();
 		}
 		
 		if (stubServiceID != null) {
 			fireTimeout(stubServiceID);
 		}
 		
 	}
 	
 	private void fireTimeout(ServiceID stubServiceID) {
 		for (TimeoutListener listener : timeoutListeners) {
 			listener.timeout(stubServiceID);
 		}		
 	}
 
 	public void sendNotifyFailureMessage(ServiceID key) {
 		Message interestMessage = null;
 		
 		try {
 			interestLock.lock();
 
 			Interest interest = interests.get(key);
 			interestMessage = createNotifyFailureMessage(interest);
 		
 		} finally {
 			interestLock.unlock();
 		}
 		
 		if (interestMessage != null) {
 			interestProcessor.sendMessage(interestMessage);
 		}
 	}
 	
 	private Message createNotifyFailureMessage(Interest interest) {
 		ObjectDeployment interested = interest.getInterested();
 		Message message = 
 			new Message(interested.getModule().getContainerID(), interested.getDeploymentID(), 
 					interest.getFailureNotificationMethod().getName());
 		
 		DeploymentID stubDeploymentID = 
 			interestProcessor.getModule().getStubDeploymentID(interest.getStubServiceID());
 		Class<?> stubType = interest.getFailureNotificationMethod().getParameterTypes()[0];
 		
 		StubParameter stubParameter = new StubParameter(stubDeploymentID);
 		message.addParameter(stubType, stubParameter);
 		
 		if (interest.hasFailureNotificationMethodDeploymentID()) {
 			message.addParameter(DeploymentID.class, stubDeploymentID);
 		}
 
 		if (interest.hasFailureNotificationMethodCertificate()) {
 			message.addParameter(X509CertPath.class, interest.getInterestCertPath());
 		}
 		
 		return message;
 	}
 
 	public void isItAlive(ServiceID monitoredID, DeploymentID sourceID) {
 		if (monitoredID == null) {
 			return;
 		}
 		
 		Module myContainer = this.interestProcessor.getModule();
 		ContainerID monitoredContainerID = monitoredID.getContainerID();
 		String serviceName = monitoredID.getServiceName();
 		
 		if (monitoredContainerID == null || !monitoredContainerID.equals(myContainer.getContainerID()) ||
 				serviceName == null) {
 			return;
 		}
 		
 		ObjectDeployment monitoredDeployment = myContainer.getObjectRepository().get(serviceName);
 		
 		
 		Message message = null; 
 		
 		if ( monitoredDeployment == null ) {
 			ServiceID validMonitoredID = new ServiceID(myContainer.getContainerID(), monitoredID.getServiceName());
 			message = createUpdateStatusMessage(sourceID, validMonitoredID);
 			message.addParameter(MonitorableStatus.class, MonitorableStatus.UNAVAILABLE);
 		} else {
 			message = createUpdateStatusMessage(sourceID, monitoredDeployment.getDeploymentID());
 			message.addParameter(MonitorableStatus.class, MonitorableStatus.AVAILABLE);
 		}
 		
 		this.interestProcessor.sendMessage(message);
 	}
 	
 	private Message createUpdateStatusMessage(DeploymentID sourceID, CommuneAddress monitoredID) {
 		String processorType = InterestProcessor.class.getName();
 		return new Message(monitoredID, sourceID, InterestProcessor.UPDATE_STATUS_MESSAGE, processorType);
 	}
 
 	void updateStatus(CommuneAddress targetID, MonitorableStatus status, X509CertPath certPath) {
 		if (MonitorableStatus.AVAILABLE.equals(status)) {
 			updateStatusAvailable(targetID, certPath);
 		} else {
 			updateStatusUnavailable(targetID, certPath);
 		}
 	}

 	private void updateStatusAvailable(CommuneAddress targetID, X509CertPath certPath) {
 		DeploymentID targetDeploymentID = (DeploymentID) targetID; 
 		
 		try {
 			interestLock.lock();
			//TODO validate
 			Interest interest = interests.get(targetDeploymentID.getServiceID());
 			
			
 			if (interest == null) {
				return;//TODO
 			}
 
 			interest.setLastHeartbeat();
			
 			interest.setInterestCertPath(certPath);
 			
 			if (isStubDown(interest)) {
 				
 				interestProcessor.getModule().setStubDeploymentID(targetDeploymentID);
				sendNotifyRecoveryMessage(interest);
 
 			} 
			
			
			
 		} finally {
 			interestLock.unlock();
 		}
 	}
 
 	private boolean isStubDown(Interest interest) {
 		return !interestProcessor.getModule().isStubUp(interest.getStubServiceID());
 	}
 	
	private void sendNotifyRecoveryMessage(Interest interest) {
 		ObjectDeployment interested = interest.getInterested();
 		Message message = 
 			new Message(interested.getModule().getContainerID(), interested.getDeploymentID(), 
 					interest.getRecoveryNotificationMethod().getName());
 		
 		DeploymentID stubDeploymentID = 
 			interestProcessor.getModule().getStubDeploymentID(interest.getStubServiceID());
 		Class<?> stubType = interest.getRecoveryNotificationMethod().getParameterTypes()[0]; 
 
 		message.addStubParameter(stubType, stubDeploymentID);
 		
 		if (interest.hasRecoveryNotificationMethodDeploymentID()) {
 			message.addParameter(DeploymentID.class, stubDeploymentID);
 		}
 		
 		if (interest.hasRecoveryNotificationMethodCertificate()) {
 			message.addParameter(X509CertPath.class, interest.getInterestCertPath());
 		}
 		
		this.interestProcessor.sendMessage(message);
 	}
 	
 	private void updateStatusUnavailable(CommuneAddress targetID, X509CertPath certPath) {
 		
 		Message interestMessage = null;
 		
 		try {
 			interestLock.lock();
 			ServiceID targetServiceID = (ServiceID) targetID; 
 			//TODO validate
 			Interest interest = interests.get(targetServiceID);
 			
 			if (interest != null) {
 				interest.setInterestCertPath(certPath);
 				if (interestProcessor.getModule().isStubUp(interest.getStubServiceID())) {
 					interestMessage = createNotifyFailureMessage(interest);
 				}
 			}
 			
 		} finally {
 			interestLock.unlock();
 		}
 		
 		if (interestMessage != null) {
 			interestProcessor.sendMessage(interestMessage);
 		}
 	}
 	
 	public void shutdown() {
 		this.executor.shutdownNow();
 		try {
 			this.executor.awaitTermination( DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS );
 		} catch ( InterruptedException e ) {
 			// TODO
 		}
 	}
 
 	
 	private class InterestThreadFactory implements ThreadFactory {
 
 		public Thread newThread(Runnable r) {
 			Thread thread = new Thread(r);
 			thread.setName("EXECUTOR-Interest" + thread.getId());
 			return thread;
 		}
 	}
 
 	
 	public void registerParameterInterest(ObjectDeployment objectDeployment, Method method, int parameterIndex, 
 			Class<?> parameterType, ServiceID stubServiceID) {
 
 		try {
 			interestLock.lock();
 			MonitoredParameter monitoredParameter = 
 				findMonitoredParameter(objectDeployment.getObject(), method, parameterIndex);
 			Monitor monitor = parameter2Monitor.get(monitoredParameter);
 			
 			if (monitor != null) {
 				Interest interest = registerInterest(stubServiceID, monitor, monitoredParameter.getRequirements(), true);
 				interest.setLastHeartbeat();
 			}
 			
 		} finally {
 			interestLock.unlock();
 		}
 	}
 	
 	private MonitoredParameter findMonitoredParameter(Object deployedObject, Method method, int parameterIndex) {
 		Set<MonitoredParameter> parameters = parameter2Monitor.keySet();
 		
 		for (MonitoredParameter monitoredParameter : parameters) {
 			
 			if (monitoredParameter.getDeployedObject().equals(deployedObject) && 
 				monitoredParameter.getMethod().equals(method) && 
 				monitoredParameter.getParameterIndex() == parameterIndex) {
 				
 				return monitoredParameter;
 			}
 		}
 		
 		return null;
 	}
 
 	public void removeInterest(Object stub) {
 		try {
 			interestLock.lock();
 
 			ServiceID stubServiceID = interestProcessor.getModule().getStubServiceID(stub);
 			Interest removedInterest = interests.remove(stubServiceID);
 			if(removedInterest != null){
 				removedInterest.cancelScheduledExecution();
 			}
 			
 			
 		} finally {
 			interestLock.unlock();
 		}
 	}
 	
 	public void removeInterest(ServiceID stubServiceID) {
 		try {
 			interestLock.lock();
 
 			Interest removedInterest = interests.remove(stubServiceID);
 			if(removedInterest != null){
 				removedInterest.cancelScheduledExecution();
 			}
 			
 			
 		} finally {
 			interestLock.unlock();
 		}
 		
 	}
 
 	public boolean isInterested(DeploymentID interestedID, ServiceID monitorableID) {
 		try {
 			interestLock.lock();
 
 			Interest interest = interests.get(monitorableID);
 			
 			if (interest == null) {
 				return false;
 			}
 			
 			return interestedID.equals(interest.getInterested().getDeploymentID());
 			
 		} finally {
 			interestLock.unlock();
 		}
 	}
 	
 	public Interest getInterest(ServiceID monitorableID) {
 		try {
 			interestLock.lock();
 
 			return interests.get(monitorableID);
 			
 		} finally {
 			interestLock.unlock();
 		}
 	}
 
 }
