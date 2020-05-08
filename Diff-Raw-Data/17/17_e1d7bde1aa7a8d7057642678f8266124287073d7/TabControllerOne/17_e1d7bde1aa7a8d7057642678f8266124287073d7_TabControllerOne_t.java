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
 package org.sopeco.webui.client.layout.center.execute.tabOne;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.sopeco.webui.client.event.EventControl;
 import org.sopeco.webui.client.event.MEControllerEvent;
 import org.sopeco.webui.client.event.handler.MEControllerEventHandler;
 import org.sopeco.webui.client.layout.center.execute.ExecuteController;
 import org.sopeco.webui.client.layout.center.execute.ExecuteTabPanel;
 import org.sopeco.webui.client.layout.center.execute.TabController;
 import org.sopeco.webui.client.layout.popups.Message;
 import org.sopeco.webui.client.manager.Manager;
 import org.sopeco.webui.client.manager.ScenarioManager;
 import org.sopeco.webui.shared.entities.FrontendScheduledExperiment;
 import org.sopeco.webui.shared.rpc.RPC;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.FlowPanel;
 
 /**
  * 
  * @author Marius Oehler
  * 
  */
 public class TabControllerOne extends TabController implements ClickHandler, MEControllerEventHandler {
 
 	private static final Logger LOGGER = Logger.getLogger(TabControllerOne.class.getName());
 
 	private ExecuteTab view;
 	private DateTimeFormat dtf;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param parentController
 	 */
 	public TabControllerOne(ExecuteController parentController) {
 		super(parentController);
 		initialize();
 	}
 
 	/**
 	 * Initializes the view.
 	 */
 	private void initialize() {
 		view = new ExecuteTab();
 		view.getBtnExecute().addClickHandler(this);
 
 		EventControl.get().addHandler(MEControllerEvent.TYPE, this);
 	}
 
 	@Override
 	public FlowPanel getView() {
 		return view;
 	}
 
 	public void onNewMEControllerEvent(org.sopeco.webui.client.event.MEControllerEvent event) {
 		updateControllerURL();
 	};
 
 	@Override
 	public void onSelection() {
 		updateControllerURL();
 
 		if (dtf == null) {
 			dtf = DateTimeFormat.getFormat("yyyy-MM-dd hh:mm aa");
 		}
 		view.getEditLabel().setValue("ExperimentRun " + dtf.format(new Date()));
 	}
 
 	public void updateControllerURL() {
 		view.getEditController().setValue(Manager.get().getControllerUrl());
 	}
 
 	@Override
 	public void onClick(ClickEvent event) {
 		scheduleExperiment();
 	}
 
 	private boolean isRepeating() {
 		return view.getScheduleConfTable().getCbRepeat().getValue()
 				&& !view.getRepeatTable().getScheduleDays().isEmpty();
 	}
 
 	/**
 	 * Returns the time, when the experiment will be executed. If it's executed
 	 * immediately, this method returns the current time.
 	 * 
 	 * @return stating timestamp
 	 */
 	private long getStartTime() {
 		if (view.isExecutingImmediately()) {
 			return System.currentTimeMillis();
 		} else {
 			String[] splitTime = view.getScheduleConfTable().getEditStartTime().getValue().split(":");
 			String[] splitDate = view.getScheduleConfTable().getEditStartDate().getValue().split("\\.");
 
 			Date date = new Date();
 			date.setHours(Integer.parseInt(splitTime[0]));
 			date.setMinutes(Integer.parseInt(splitTime[1]));
 			date.setSeconds(0);
 			date.setDate(Integer.parseInt(splitDate[0]));
 			date.setMonth(Integer.parseInt(splitDate[1]) - 1);
 			date.setYear(Integer.parseInt(splitDate[2]) - 1900);
 
 			return date.getTime();
 		}
 	}
 
 	private List<String> createExperimentSelection() {
 		List<String> selectedExperimentSeries = new ArrayList<String>();
 
 		for (String spec : view.getSelectionPanel().getTreeItems().keySet()) {
 			for (String esd : view.getSelectionPanel().getTreeItems().get(spec).keySet()) {
				if (view.getSelectionPanel().getTreeItems().get(spec).get(esd).getCheckBox().getValue()) {
 					selectedExperimentSeries.add(spec+"."+esd);
 				}
 			}
 		}
 
 		return selectedExperimentSeries;
 	}
 
 	private void scheduleExperiment() {
 		FrontendScheduledExperiment scheduledExperiment = new FrontendScheduledExperiment();
 		// scheduledExperiment.setAccount(Manager.get().getAccountDetails().getAccountName());
 		scheduledExperiment.setAccount(Manager.get().getAccountDetails().getId());
 		scheduledExperiment.setLabel(view.getEditLabel().getValue());
 		scheduledExperiment.setStartTime(getStartTime());
 		scheduledExperiment.setControllerUrl(Manager.get().getControllerUrl());
 		scheduledExperiment.setScenarioDefinition(ScenarioManager.get().getCurrentScenarioDefinition());
 		scheduledExperiment.setSelectedExperiments(createExperimentSelection());
 
 		if (!view.isExecutingImmediately() && isRepeating()) {
 			scheduledExperiment.setRepeating(true);
 			scheduledExperiment.setRepeatDays(view.getRepeatTable().getScheduleDays());
 			scheduledExperiment.setRepeatHours(view.getRepeatTable().getScheduleHours());
 			scheduledExperiment.setRepeatMinutes(view.getRepeatTable().getScheduleMinutes());
 		} else {
 			scheduledExperiment.setRepeating(false);
 		}
 
 		RPC.getExecuteRPC().scheduleExperiment(scheduledExperiment, new AsyncCallback<Void>() {
 			@Override
 			public void onSuccess(Void result) {
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Message.error(caught.getMessage());
 			}
 		});
 
 		if (view.isExecutingImmediately()) {
 			LOGGER.fine("Execute NOW");
 			((ExecuteTabPanel) getParentController().getView()).selectTab(2);
 			getParentController().getTabControllerThree().startingMessage();
 		}
 
 		getParentController().getTabControllerTwo().loadScheduledExperiments();
 	}
 }
