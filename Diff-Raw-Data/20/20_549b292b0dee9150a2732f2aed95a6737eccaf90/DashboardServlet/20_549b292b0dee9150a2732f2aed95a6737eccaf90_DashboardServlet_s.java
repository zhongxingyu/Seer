 /*
  * #%L
  * Bitrepository Webclient
  * %%
  * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.dashboard;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.bitrepository.common.webobjects.StatisticsDataSize;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class DashboardServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private final Logger log = LoggerFactory.getLogger(getClass());
 	public static final String GRAPH_TYPE_ATTRIBUTE = "GRAPH_TYPE_ATTRIBUTE";
 	public static final String DATA_SIZE_HISTORY_ATTRIBUTE = "DATA_SIZE_HISTORY_ATTRIBUTE";
 	public static final String DATA_SIZE_HISTORY_NAMES_ATTRIBUTE = "DATA_SIZE_HISTORY_NAMES_ATTRIBUTE";
 
 	/*
 	 * Called first time page is accessed or refreshed. Set some checkbox to pre-selected
 	 */
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("text/html; charset=UTF-8");
 		request.setCharacterEncoding("UTF-8");
 
 		try {
 			ArrayList<String> collectionIdsSelected = new ArrayList<String>();
 			ArrayList<ArrayList<StatisticsDataSize>> dataSet = new ArrayList<ArrayList<StatisticsDataSize>>();
 			ArrayList<ArrayList<StatisticsDataSize>> deltaDataSet = new ArrayList<ArrayList<StatisticsDataSize>>();
 			ArrayList<String> dataSetNames = new ArrayList<String>();
 			for (String id : DashboardDataCache.getCollectionId2NameMap().keySet()) {
 
 				ArrayList<StatisticsDataSize> data = DashboardDataCache.getStatisticsIdDataSizeMap().get(id);
 				ArrayList<StatisticsDataSize> deltaData = DashboardDataCache.getStatisticsIdDataGrowthMap().get(id);
 				dataSet.add(data);
 				deltaDataSet.add(deltaData);
 				dataSetNames.add(DashboardDataCache.getCollectionId2NameMap().get(id));
 				collectionIdsSelected.add(id);
 				request.setAttribute(id, "on");
 			}
 			request.setAttribute(DATA_SIZE_HISTORY_ATTRIBUTE, dataSet);
 			request.setAttribute(DATA_SIZE_HISTORY_NAMES_ATTRIBUTE, dataSetNames);
 			returnFormPage(request, response);
 			return;
 
 		} catch (Exception e) {// various server errors
 			log.error("unexpected error", e);
 			returnFormPage(request, response);
 			return;
 		}
 
 	}
 
 	/*
 	 * Handle Post request - that is all the form data
 	 */
 	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		response.setContentType("text/html; charset=UTF-8");
 		request.setCharacterEncoding("UTF-8");
 
 		try {
 			ArrayList<String> collectionIdsSelected = new ArrayList<String>();
 			ArrayList<ArrayList<StatisticsDataSize>> dataSet = new ArrayList<ArrayList<StatisticsDataSize>>();
 			ArrayList<ArrayList<StatisticsDataSize>> dataDeltaSet = new ArrayList<ArrayList<StatisticsDataSize>>();
 			ArrayList<String> dataSetNames = new ArrayList<String>();
 			String graphType = request.getParameter("graphType");
 
 			for (String id : DashboardDataCache.getCollectionId2NameMap().keySet()) {
 				if (request.getParameter(id) != null) {
 					ArrayList<StatisticsDataSize> data = DashboardDataCache.getStatisticsIdDataSizeMap().get(id);
 					ArrayList<StatisticsDataSize> dataDelta = DashboardDataCache.getStatisticsIdDataGrowthMap().get(id);
 					dataSet.add(data);
 					dataDeltaSet.add(dataDelta);
 					dataSetNames.add(DashboardDataCache.getCollectionId2NameMap().get(id));
 					collectionIdsSelected.add(id);
 					request.setAttribute(id, "on");
 				}
 			}
 			if ("graph_tilvaekst".equals(graphType)) {
 				request.setAttribute(DATA_SIZE_HISTORY_ATTRIBUTE, dataSet);
 				request.setAttribute(GRAPH_TYPE_ATTRIBUTE, "graph_tilvaekst");
 			} else {
 				request.setAttribute(DATA_SIZE_HISTORY_ATTRIBUTE, dataDeltaSet);
 				request.setAttribute(GRAPH_TYPE_ATTRIBUTE, "graph_delta");
 			}
 			request.setAttribute(DATA_SIZE_HISTORY_NAMES_ATTRIBUTE, dataSetNames);
 			returnFormPage(request, response);
 			return;
 
 		} catch (Exception e) {// various server errors
 			log.error("unexpected error", e);
 			returnFormPage(request, response);
 			return;
 		}
 
 	}
 
 	private void returnFormPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		RequestDispatcher dispatcher = request.getRequestDispatcher("dashboard.jsp");
 		dispatcher.forward(request, response);
 		return;
 	}
 
 	public static int countCheckSumErrors(ArrayList<GetIntegrityStatus> status) {
 
 		int checkSumErrors = 0;
 		for (GetIntegrityStatus current : status) {
 			checkSumErrors += current.getChecksumErrorCount();
 		}
 		return checkSumErrors;
 	}
 
 	public static int countMissingFiles(ArrayList<GetIntegrityStatus> status) {
 		int missingFiles = 0;
 		for (GetIntegrityStatus current : status) {
 			missingFiles += current.getMissingFilesCount();
 		}
 		return missingFiles;
 	}
 
 	public static long getMaximumByteSize(ArrayList<ArrayList<StatisticsDataSize>> data) {
 		long max = 0;
 
 		for (ArrayList<StatisticsDataSize> list : data) {
 			for (StatisticsDataSize currentDataSet : list) {
 				if (currentDataSet.getDataSize() > max) {
 					max = currentDataSet.getDataSize();
 				}
 			}
 		}
 		return max;
 	}
 
 }
