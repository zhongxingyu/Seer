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
 package org.sopeco.webui.server.rpc;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.ws.rs.client.WebTarget;
 import javax.ws.rs.core.GenericType;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.sopeco.persistence.dataset.DataSetAggregated;
 import org.sopeco.persistence.dataset.ParameterValue;
 import org.sopeco.persistence.dataset.SimpleDataSet;
 import org.sopeco.persistence.dataset.SimpleDataSetColumn;
 import org.sopeco.persistence.dataset.SimpleDataSetRow;
 import org.sopeco.persistence.entities.ExperimentSeries;
 import org.sopeco.persistence.entities.ExperimentSeriesRun;
 import org.sopeco.persistence.entities.ScenarioInstance;
 import org.sopeco.persistence.exceptions.DataNotFoundException;
 import org.sopeco.service.configuration.ServiceConfiguration;
 import org.sopeco.webui.server.rest.ClientFactory;
 import org.sopeco.webui.server.rpc.servlet.SPCRemoteServlet;
 import org.sopeco.webui.server.user.TokenManager;
 import org.sopeco.webui.shared.definitions.result.SharedExperimentRuns;
 import org.sopeco.webui.shared.definitions.result.SharedExperimentSeries;
 import org.sopeco.webui.shared.definitions.result.SharedScenarioInstance;
 import org.sopeco.webui.shared.rpc.ResultRPC;
 
 /**
  * 
  * @author Marius Oehler
  * 
  */
 public class ResultRPCImpl extends SPCRemoteServlet implements ResultRPC {
 
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	public List<SharedScenarioInstance> getInstances(String scenarioName) {
 		requiredLoggedIn();
 		
 		WebTarget wt = ClientFactory.getInstance().getClient(ServiceConfiguration.SVC_SCENARIO,
 						 									 ServiceConfiguration.SVC_SCENARIO_INSTANCES);
 		
 		wt = wt.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, scenarioName);
 		wt = wt.queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, getToken());
 		
 		Response r = wt.request(MediaType.APPLICATION_JSON).get();
		
		r.readEntity(new GenericType<List<ScenarioInstance>>() { });
 			
 		// now convert the scenario instances to the appropriate type
 		List<ScenarioInstance> scenarioList = r.readEntity(new GenericType<List<ScenarioInstance>>() { });
 
 		List<SharedScenarioInstance> retList = new ArrayList<SharedScenarioInstance>();
 		
 		if (scenarioList != null) {
 			
 			for (ScenarioInstance instance : scenarioList) {
 				retList.add(convertInstance(instance));
 			}
 			
 		}
 		
 		return retList;
 	}
 
 	@Override
 	public String getResultAsR(String scenario, String exoerimentSeries, String url, long timestamp) {
 		requiredLoggedIn();
 		
 		try {
 			ScenarioInstance instance = getScenarioInstance(getSessionId(), scenario, url);
 			ExperimentSeries series = getSeries(instance, exoerimentSeries);
 			ExperimentSeriesRun run = getRun(series, timestamp);
 
 			DataSetAggregated dataset = run.getSuccessfulResultDataSet();
 			SimpleDataSet simpleDataset = dataset.convertToSimpleDataSet();
 
 			StringBuffer rValue = new StringBuffer();
 
 			int rowCount = simpleDataset.getRowList().size();
 			int colCount = 0;
 			if (rowCount > 0)
 				colCount = simpleDataset.getRow(0).getRowValues().size();
 
 			rValue.append("myframe <- data.frame( matrix( nrow = " + rowCount + ", ncol = " + colCount + " ) )\n");
 
 			int i = 1;
 			for (Iterator<SimpleDataSetRow> rowIter = simpleDataset.getRowList().iterator(); rowIter.hasNext(); i++) {
 				rValue.append("myframe[");
 				rValue.append(i);
 				rValue.append(",] <- c(");
 
 				for (Iterator<ParameterValue> colIter = rowIter.next().getRowValues().iterator(); colIter.hasNext();) {
 					Object val = colIter.next().getValue();
 					if (val instanceof String) {
 						rValue.append("\"");
 						rValue.append(val.toString());
 						rValue.append("\"");
 					} else if (val instanceof Boolean) {
 						rValue.append(val.toString().toUpperCase());
 					} else {
 						rValue.append(val.toString());
 					}
 					if (colIter.hasNext()) {
 						rValue.append(", ");
 					}
 				}
 
 				rValue.append(")\n");
 			}
 
 			rValue.append("colnames(myframe) <- c(");
 
 			for (Iterator<SimpleDataSetColumn> iter = simpleDataset.getColumns().iterator(); iter.hasNext();) {
 				rValue.append("\"");
 				rValue.append(iter.next().getParameter().getFullName());
 				rValue.append("\"");
 				if (iter.hasNext()) {
 					rValue.append(", ");
 				}
 			}
 			rValue.append(")");
 
 			return rValue.toString();
 		} catch (DataNotFoundException e) {
 			return "No Data Found";
 		}
 	}
 
 	
 	
 	//////////////////////////////////////////////////////////////////////////////////////////////////
 	///////////////////////////////////////// HELPER /////////////////////////////////////////////////
 	//////////////////////////////////////////////////////////////////////////////////////////////////	
 
 	/**
 	 * Creates a SharedScenarioInstance out of a ScenarioInstance which can send
 	 * to the FrontEnd.
 	 * 
 	 * @param instance
 	 * @return
 	 */
 	private SharedScenarioInstance convertInstance(ScenarioInstance instance) {
 		requiredLoggedIn();
 		
 		SharedScenarioInstance retInstance = new SharedScenarioInstance();
 
 		retInstance.setScenarioName(instance.getName());
 		retInstance.setControllerUrl(instance.getMeasurementEnvironmentUrl());
 
 		for (ExperimentSeries series : instance.getExperimentSeriesList()) {
 			SharedExperimentSeries sharedSeries = new SharedExperimentSeries();
 			sharedSeries.setExperimentName(series.getName());
 
 			for (ExperimentSeriesRun run : series.getExperimentSeriesRuns()) {
 				SharedExperimentRuns sharedRun = new SharedExperimentRuns();
 				sharedRun.setTimestamp(run.getTimestamp());
 				sharedRun.setLabel(run.getLabel());
 
 				sharedSeries.addExperimentRun(sharedRun);
 			}
 
 			retInstance.addExperimentSeries(sharedSeries);
 		}
 
 		return retInstance;
 	}
 	
 	/**
 	 *
 	 */
 	private ExperimentSeriesRun getRun(ExperimentSeries series, Long timestamp) throws DataNotFoundException {
 		requiredLoggedIn();
 		
 		for (ExperimentSeriesRun run : series.getExperimentSeriesRuns()) {
 			System.out.println(run.getTimestamp() + " " + timestamp);
 			if (timestamp.equals(run.getTimestamp())) {
 				return run;
 			}
 		}
 
 		throw new DataNotFoundException("No ExperimentSeriesRun with timestamp '" + timestamp + "' found..");
 	}
 
 	/**
 	 * 
 	 */
 	private ExperimentSeries getSeries(ScenarioInstance instance, String name) throws DataNotFoundException {
 		requiredLoggedIn();
 		
 		for (ExperimentSeries series : instance.getExperimentSeriesList()) {
 			if (series.getName().equals(name)) {
 				return series;
 			}
 		}
 
 		throw new DataNotFoundException("No ExperimentSeries '" + name + "' found..");
 	}
 
 	/**
 	 *
 	 */
 	private ScenarioInstance getScenarioInstance(String sId, String scenarioName, String url)
 			throws DataNotFoundException {
 		requiredLoggedIn();
 		
 		WebTarget wt = ClientFactory.getInstance().getClient(ServiceConfiguration.SVC_SCENARIO,
 					 										 ServiceConfiguration.SVC_SCENARIO_INSTANCE);
 		
 		wt = wt.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, TokenManager.instance().getToken(sId));
 		wt = wt.queryParam(ServiceConfiguration.SVCP_SCENARIO_NAME, scenarioName);
 		wt = wt.queryParam(ServiceConfiguration.SVCP_SCENARIO_URL, url);
 		
 		Response r = wt.request(MediaType.APPLICATION_JSON).get();
 		
 		return r.readEntity(ScenarioInstance.class);
 	}
 }
