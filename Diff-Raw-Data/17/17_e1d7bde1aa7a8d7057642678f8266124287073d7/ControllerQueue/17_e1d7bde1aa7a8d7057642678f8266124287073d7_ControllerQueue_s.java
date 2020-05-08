 /**
  * Copyright (c) 2013 SAP
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the SAP nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SAP BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.sopeco.webui.server.execute;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.logging.Logger;
 
 import org.sopeco.config.Configuration;
 import org.sopeco.config.IConfiguration;
 import org.sopeco.engine.status.ErrorInfo;
 import org.sopeco.engine.status.EventType;
 import org.sopeco.engine.status.IStatusListener;
 import org.sopeco.engine.status.ProgressInfo;
 import org.sopeco.engine.status.StatusBroker;
 import org.sopeco.engine.status.StatusMessage;
 import org.sopeco.runner.SoPeCoRunner;
 import org.sopeco.webui.server.persistence.UiPersistence;
 import org.sopeco.webui.server.persistence.entities.ScheduledExperiment;
 import org.sopeco.webui.server.rpc.PushRPCImpl;
 import org.sopeco.webui.server.user.User;
 import org.sopeco.webui.server.user.UserManager;
 import org.sopeco.webui.shared.entities.ExecutedExperimentDetails;
 import org.sopeco.webui.shared.entities.FrontendScheduledExperiment;
 import org.sopeco.webui.shared.entities.MECLog;
 import org.sopeco.webui.shared.entities.RunningControllerStatus;
 import org.sopeco.webui.shared.entities.account.Account;
 import org.sopeco.webui.shared.push.PushDomain;
import org.sopeco.webui.shared.push.PushSerializable;
 import org.sopeco.webui.shared.push.packages.PushControllerStatus;
 import org.sopeco.webui.shared.push.packages.PushScheduledExperiments;
 
 /**
  * 
  * @author Marius Oehler
  * 
  */
 public class ControllerQueue implements IStatusListener {
 
 	private static final Logger LOGGER = Logger.getLogger(ControllerQueue.class.getName());
 	private static ExecutorService threadPool;
 
 	/** Queue of waiting experiments. */
 	private List<QueuedExperiment> experimentQueue;
 
 	/** The experiment which is performed at the moment. */
 	private QueuedExperiment runningExperiment;
 
 	private Future<?> executeStatus;
 
 	private String controllerURL;
 
 	private String generatedSessionId;
 
 	/**
 	 * Creates an ThreadPool, which is responsible for the SoPeCo Runners.
 	 * 
 	 * @return ExecutorService
 	 */
 	private static ExecutorService getThreadPool() {
 		if (threadPool == null) {
 			threadPool = Executors.newCachedThreadPool();
 		}
 		return threadPool;
 	}
 
 	/**
 	 * Constructor.
 	 */
 	public ControllerQueue(String pControllerURL) {
 		experimentQueue = new ArrayList<QueuedExperiment>();
 		controllerURL = pControllerURL;
 
 		StatusBroker.getManager(pControllerURL).addStatusListener(this);
 	}
 
 	/**
 	 * Returns whether the active experiment is executed by a thread.
 	 * 
 	 * @return true if a runner is running
 	 */
 	public boolean isExecuting() {
 		if (executeStatus != null) {
 			synchronized (executeStatus) {
 				if (executeStatus.isDone() || executeStatus.isCancelled()) {
 					return false;
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns whether an experiment is loaded, and perhaps is executed.
 	 * 
 	 * @return true if an experiment is active
 	 */
 	public boolean experimentIsLoaded() {
 		if (runningExperiment != null) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Adds an experiment to this ControllerQueue. If no experiment is executed
 	 * yet, this will be executed.
 	 * 
 	 * @param experiment
 	 */
 	public void addExperiment(QueuedExperiment experiment) {
 		LOGGER.info("Adding experiment id:" + experiment.getScheduledExperiment().getId() + " to queue.");
 		experiment.setTimeQueued(System.currentTimeMillis());
 		experimentQueue.add(experiment);
 		checkQueue();
 	}
 
 	public void abortExperiment() {
 		if (isExecuting()) {
 			Configuration.getSessionSingleton(generatedSessionId).setProperty(IConfiguration.EXPERIMENT_RUN_ABORT,
 					new Boolean(true));
 		}
 
 	}
 
 	/**
 	 * Checks if the controller is ready and a experiment is waiting in the
 	 * queue. If so, the next experiment is started.
 	 */
 	private void checkQueue() {
 		synchronized (experimentQueue) {
 			LOGGER.info("Looking for waiting experiment..");
 			if (isExecuting()) {
 				LOGGER.info("Controller is running.");
 				pushCurrentControllerQueue();
 				return;
 			} else if (experimentIsLoaded()) {
 				LOGGER.info("Experiment is already loaded.");
 			} else if (experimentQueue.isEmpty()) {
 				LOGGER.info("Queue is empty.");
 			} else {
 				runningExperiment = experimentQueue.get(0);
 				experimentQueue.remove(0);
 				pushCurrentControllerQueue();
 				execute();
 			}
 		}
 	}
 
 	/**
 	 * Start the execution of the given experiment.
 	 * 
 	 * @param experiment
 	 */
 	private void execute() {
 		LOGGER.info("Start experiment id:" + runningExperiment.getScheduledExperiment().getId() + " on: "
 				+ runningExperiment.getScheduledExperiment().getControllerUrl());
 
 		Map<String, Object> executionProperties = new HashMap<String, Object>();
 		try {
 			executionProperties.putAll(runningExperiment.getScheduledExperiment().getProperties());
 
 			executionProperties.put(IConfiguration.CONF_MEASUREMENT_CONTROLLER_URI, new URI(runningExperiment
 					.getScheduledExperiment().getControllerUrl()));
 
 			executionProperties.put(IConfiguration.CONF_SCENARIO_DESCRIPTION, runningExperiment
 					.getScheduledExperiment().getScenarioDefinition());
			executionProperties.put(IConfiguration.CONF_EXPERIMENT_EXECUTION_SELECTION, runningExperiment
					.getScheduledExperiment().getSelectedExperiments());
 		} catch (URISyntaxException e) {
 			throw new RuntimeException(e);
 		}
 
 		generatedSessionId = "RANDOMID" + (long) (Long.MAX_VALUE * Math.random());
		SoPeCoRunner runner = new SoPeCoRunner(generatedSessionId, executionProperties);
 		executeStatus = getThreadPool().submit(runner);
 
 		runningExperiment.setTimeStarted(System.currentTimeMillis());
 
 		notifyAccount();
 		// notifyStatusChange(EStatus.START_MEASUREMENT.toString());
 	}
 
 	/**
 	 * Stores the duration of this execution in the list, which is stored in the
 	 * ScheduledExperiment.
 	 */
 	private void saveDurationInExperiment() {
 		ScheduledExperiment exp = UiPersistence.getUiProvider().loadScheduledExperiment(
 				runningExperiment.getScheduledExperiment().getId());
 		if (exp != null) {
 			if (exp.getDurations() == null) {
 				exp.setDurations(new ArrayList<Long>());
 			}
 			long duration = runningExperiment.getTimeEnded() - runningExperiment.getTimeStarted();
 			exp.getDurations().add(duration);
 			UiPersistence.getUiProvider().storeScheduledExperiment(exp);
 		}
 	}
 
 	/**
 	 * Returns the experiment which is loaded at the moment.
 	 * 
 	 * @return QueuedExperiment
 	 */
 	public QueuedExperiment getCurrentlyRunning() {
 		return runningExperiment;
 	}
 
 	/**
 	 * Pushes the next status of the running experiment.
 	 * 
 	 * @param statusMessage
 	 */
 	public void nextStatusMessage(StatusMessage statusMessage) {
 		if (runningExperiment == null) {
 			return;
 		}
 		runningExperiment.getStatusMessageList().add(statusMessage);
 		if (statusMessage.getStatusInfo() != null && statusMessage.getStatusInfo() instanceof ProgressInfo) {
 			runningExperiment.setLastProgressInfo((ProgressInfo) statusMessage.getStatusInfo());
 		}
 
 		pushRunningExperimentStatus();
 	}
 
 	/**
 	 * Pushes the status information of the current running experiment to all
 	 * users, which use this MEController.
 	 */
 	private void pushRunningExperimentStatus() {
 		PushControllerStatus pcs = new PushControllerStatus(PushDomain.TAB_CONTROLLER_THREE);
 		pcs.setCcExperiment(createControllerStatusPackage());
 
 		PushRPCImpl.pushToAllOnController(runningExperiment.getScheduledExperiment().getControllerUrl(), pcs);
 	}
 
 	/**
 	 * Pushes a list with the QueuedExperiments, which are waiting in the
 	 * controller queue.
 	 */
 	private void pushCurrentControllerQueue() {
 		if (runningExperiment == null) {
 			return;
 		}
 
 		ArrayList<FrontendScheduledExperiment> list = new ArrayList<FrontendScheduledExperiment>();
 		for (QueuedExperiment experiment : experimentQueue) {
 			list.add(experiment.getScheduledExperiment().createFrontendScheduledExperiment());
 		}
 
 		PushScheduledExperiments pse = new PushScheduledExperiments(PushDomain.TAB_CONTROLLER_THREE);
 		pse.setList(list);
 
 		PushRPCImpl.pushToAllOnController(runningExperiment.getScheduledExperiment().getControllerUrl(), pse);
 	}
 
 	//
 	// #################################### \/ \/ \/ checken \/ \/ \/ \/ \/ \/
 	//
 
 	public void check() {
 		if (!isExecuting() && runningExperiment != null) {
 			LOGGER.fine("Thread finished but experiment was not completed. Adding MEASUREMENT_FINISHED event.");
 			StatusMessage sm = new StatusMessage();
 			sm.setEventType(EventType.MEASUREMENT_FINISHED);
 			onNewStatus(sm);
 		}
 	}
 
 	@Override
 	public void onNewStatus(StatusMessage statusMessage) {
 		LOGGER.info("New Status on '" + this.controllerURL + "': " + statusMessage.getEventType());
 		nextStatusMessage(statusMessage);
 
 		if (statusMessage.getEventType() == EventType.EXECUTION_FAILED) {
 			System.out.println();
 		}
 
 		if (statusMessage.getEventType() == EventType.MEASUREMENT_FINISHED) {
 			finished();
 		}
 	}
 
 	/**
 	 * Ends the execution of the current experiment and stores information about
 	 * it.
 	 */
 	private synchronized void finished() {
 		LOGGER.info("Experiment id:" + runningExperiment.getScheduledExperiment().getId() + " finished on: "
 				+ runningExperiment.getScheduledExperiment().getControllerUrl());
 
 		runningExperiment.setTimeEnded(System.currentTimeMillis());
 		saveDurationInExperiment();
 
 		saveLogInHistory();
 
 		notifyAccount();
 
 		Configuration.removeConfiguration(generatedSessionId);
 
 		executeStatus = null;
 		runningExperiment = null;
 		checkQueue();
 	}
 
 	private void saveLogInHistory() {
 		boolean hasError = false;
 		for (StatusMessage sm : runningExperiment.getStatusMessageList()) {
 			if (sm.getStatusInfo() != null && sm.getStatusInfo() instanceof ErrorInfo) {
 				hasError = true;
 				break;
 			}
 		}
 
 		ExecutedExperimentDetails eed = new ExecutedExperimentDetails();
 		// eed.setEventLog(runningExperiment.getEventLogLiteList());
 
 		eed.setSuccessful(!hasError);
 		eed.setTimeFinished(runningExperiment.getTimeEnded());
 		eed.setTimeStarted(runningExperiment.getTimeStarted());
 		eed.setName(runningExperiment.getScheduledExperiment().getLabel());
 		eed.setControllerURL(runningExperiment.getScheduledExperiment().getControllerUrl());
 
 		eed.setAccountId(runningExperiment.getScheduledExperiment().getAccountId());
 		eed.setScenarioName(runningExperiment.getScheduledExperiment().getScenarioDefinition().getScenarioName());
 
 		long generatedId = UiPersistence.getUiProvider().storeExecutedExperimentDetails(eed);
 
 		MECLog log = new MECLog();
 		log.setId(generatedId);
 		log.setEntries(runningExperiment.getEventLogLiteList());
 
 		UiPersistence.getUiProvider().storeMECLog(log);
 	}
 
 	/**
 	 * Creates the CurrentControllerExperiment object, that contains all
 	 * necessary information about the current controller state.
 	 * */
 	public RunningControllerStatus createControllerStatusPackage() {
 		if (runningExperiment == null) {
 			return null;
 		}
 		RunningControllerStatus cce = new RunningControllerStatus();
 
 		cce.setAccount(runningExperiment.getScheduledExperiment().getAccountId());
 		cce.setScenario(runningExperiment.getScheduledExperiment().getScenarioDefinition().getScenarioName());
 		cce.setTimeStart(runningExperiment.getTimeStarted());
 		cce.setLabel(runningExperiment.getScheduledExperiment().getLabel());
 		cce.setEventLogList(runningExperiment.getEventLogLiteList());
 
 		if (runningExperiment.getStatusMessageList().get(runningExperiment.getStatusMessageList().size() - 1)
 				.getEventType() == EventType.MEASUREMENT_FINISHED) {
 			cce.setHasFinished(true);
 		} else {
 			cce.setHasFinished(false);
 		}
 
 		if (runningExperiment.getLastProgressInfo() != null) {
 			ProgressInfo info = runningExperiment.getLastProgressInfo();
 			float progress = 100F / info.getNumberOfRepetition() * info.getRepetition();
 			cce.setProgress(progress);
 		} else {
 			cce.setProgress(-1);
 		}
 
 		if (runningExperiment.getScheduledExperiment().getDurations().size() > 2) {
 			long sum = 0;
 			for (long l : runningExperiment.getScheduledExperiment().getDurations()) {
 				sum += l;
 			}
 			long estiamtedDuration = sum / runningExperiment.getScheduledExperiment().getDurations().size();
 			cce.setTimeRemaining(estiamtedDuration);
 		}
 
 		return cce;
 	}
 
 	/**
 	 * Sends all schedule exps.
 	 */
 	private void notifyAccount() {
 		List<ScheduledExperiment> resultList = UiPersistence.getUiProvider().loadScheduledExperimentsByAccount(
 				runningExperiment.getScheduledExperiment().getAccountId());
 
 		ArrayList<FrontendScheduledExperiment> fseList = new ArrayList<FrontendScheduledExperiment>();
 		for (ScheduledExperiment experiment : resultList) {
 			fseList.add(experiment.createFrontendScheduledExperiment());
 		}
 
 		PushScheduledExperiments pse = new PushScheduledExperiments(PushDomain.TAB_CONTROLLER_TWO);
 		pse.setList(fseList);
 
 		for (User user : UserManager.instance().getAllUsers()) {
 			Account account = user.getCurrentAccount();
 			if (account != null && account.getId() == runningExperiment.getScheduledExperiment().getAccountId()) {
 				PushRPCImpl.push(user.getSessionId(), pse);
 			}
 		}
 	}
 
 }
