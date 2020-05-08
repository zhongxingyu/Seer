 /*******************************************************************************
  * Copyright (c) 27 jun. 2013 NetXForge.
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details. You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Christophe Bouhier - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.server.logic.reporting;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.util.CellRangeAddress;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.eclipse.core.runtime.NullProgressMonitor;
 
 import com.google.inject.Inject;
 import com.netxforge.netxstudio.common.model.CompositeSummary;
 import com.netxforge.netxstudio.common.model.IMonitoringSummary;
 import com.netxforge.netxstudio.common.model.IMonitoringSummary.RAG;
 import com.netxforge.netxstudio.common.model.MonitoringStateModel;
 import com.netxforge.netxstudio.common.model.RFSServiceSummary;
 import com.netxforge.netxstudio.generics.DateTimeRange;
 import com.netxforge.netxstudio.scheduling.ComponentWorkFlowRun;
 import com.netxforge.netxstudio.scheduling.Failure;
 import com.netxforge.netxstudio.services.RFSService;
 import com.netxforge.netxstudio.services.Service;
 
 public class RFSServiceSummaryReportingLogic extends OperatorReportingLogic {
 
 	private static final int CONTENT_ROW = 7;
 	private static final int HEADER_ROW = 8;
 	private static final int SERVICES_ROW = 9;
 	private static final int NODES_ROW = 10;
 	private static final int RESOURCES_ROW = 11;
 
 	@Inject
 	private MonitoringStateModel monitoringStateModel;
 
 	private CompositeSummary opSummary = new CompositeSummary();
 
 	@Override
 	protected void writeHeader(Sheet sheet, DateTimeRange dtr) {
 		super.createHeaderStructure(sheet);
 
 		super.typeCell.setCellValue("Service Monitoring");
 		super.titleCell.setCellValue("Management Sheet");
 
 		if (dtr != null) {
 			super.periodCell.setCellValue(this.getModelUtils().date(
 					getModelUtils().fromXMLDate(dtr.getBegin()))
 					+ "-"
 					+ this.getModelUtils().date(
 							getModelUtils().fromXMLDate(dtr.getEnd())));
 		}
 	}
 
 	protected String calculateFileName() {
 		String baseName = super.calculateFileName();
 		return REPORT_PREFIX + "_" + REPORT_PREFIX_SM_EXEC + "_" + baseName;
 	}
 
 	protected void doRun() {
 
 		// start a transaction
 		this.getDataProvider().getTransaction();
 
 		final List<Service> allServices = this.getServicesToExecuteFor();
 		// final List<NodeType> allNodes = getNodeTypesToExecuteFor();
 
 		getJobMonitor().setTotalWork(allServices.size());
 		getJobMonitor().setTask("Performing reporting");
 
 		// EXCEL WRITE
 		setWorkBook(new XSSFWorkbook());
 		Sheet sheet = this.getSheet("Summary");
 		this.writeHeader(sheet, this.getPeriod());
 
 		// Execute the tolerance expressions, which returns a summary for each
 		// service.
 		for (Service service : allServices) {
 			if (service instanceof RFSService) {
 				RFSServiceSummary summary = this.processService(service);
 				opSummary.addSummary(summary);
 			}
 		}
 
 		writeSummary(sheet);
 
 		if (!getFailures().isEmpty()) {
 			final ComponentWorkFlowRun run = (ComponentWorkFlowRun) this
 					.getDataProvider().getTransaction()
 					.getObject(this.getJobMonitor().getWorkFlowRunId());
 
 			for (Failure f : this.getFailures()) {
 				run.getFailureRefs().add(f);
 			}
 		}
 
 		try {
 			this.getWorkBook().write(this.getStream());
 		} catch (IOException e) {
 			e.printStackTrace();
 			// TODO, Perhaps add another failure?
 		}
 
 		this.getDataProvider().commitTransaction();
 		this.getDataProvider().closeSession();
 	}
 
 	private void writeSummary(Sheet sheet) {
 
 		// Title
 		Row summaryRow = sheet.createRow(CONTENT_ROW);
 		Cell summaryCell = summaryRow.createCell(2);
 		summaryCell.setCellValue("Executive Summary");
 		sheet.addMergedRegion(new CellRangeAddress(CONTENT_ROW, CONTENT_ROW, 2,
 				4));
 
 		// Table
 		CellStyle borderStyle = this.getWorkBook().createCellStyle();
 		borderStyle.setBorderTop(CellStyle.BORDER_THIN);
 		borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
 		borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
 		borderStyle.setBorderRight(CellStyle.BORDER_THIN);
 		borderStyle.setAlignment(CellStyle.ALIGN_CENTER);
 		borderStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
 
 		Row headerRow = sheet.createRow(HEADER_ROW);
 		{
 			Cell c1 = headerRow.createCell(4);
 			c1.setCellValue("Quantity");
 			c1.setCellStyle(borderStyle);
 		}
 
 		{
 			Cell c1 = headerRow.createCell(5);
 			c1.setCellValue("RED");
 			c1.setCellStyle(borderStyle);
 		}
 
 		{
 			Cell c1 = headerRow.createCell(6);
 			c1.setCellValue("AMBER");
 			c1.setCellStyle(borderStyle);
 		}
 
 		{
 			Cell c1 = headerRow.createCell(7);
 			c1.setCellValue("GREEN");
 			c1.setCellStyle(borderStyle);
 		}
 
 		writeServicesSummary(sheet, borderStyle);
 		writeNodesSummary(sheet, borderStyle);
 		writeResourcesSummary(sheet, borderStyle);
 
 	}
 
 	private void writeServicesSummary(Sheet sheet, CellStyle borderStyle) {
 		Row servicesRow = sheet.createRow(SERVICES_ROW);
 
 		{
 			Cell c1 = servicesRow.createCell(2);
 			c1.setCellValue("#Services");
 
 		}
 		{ // QUANTITY
 			Cell c1 = servicesRow.createCell(4);
 			c1.setCellStyle(borderStyle);
 			c1.setCellValue(opSummary.totalServices());
 			this.getServices().size();
 		}
 
 		{ // RED
 			Cell c1 = servicesRow.createCell(5);
 			c1.setCellStyle(borderStyle);
 			c1.setCellValue(opSummary.totalRag(RAG.RED));
 		}
 
 		{ // AMBER
 			Cell c1 = servicesRow.createCell(6);
 			c1.setCellStyle(borderStyle);
 			c1.setCellValue(opSummary.totalRag(RAG.AMBER));
 		}
 
 		{ // GREEN
 			Cell c1 = servicesRow.createCell(7);
 			c1.setCellStyle(borderStyle);
 			c1.setCellValue(opSummary.totalRag(RAG.GREEN));
 		}
 	}
 
 	private void writeNodesSummary(Sheet sheet, CellStyle borderStyle) {
 		Row nodesRow = sheet.createRow(NODES_ROW);
 
 		{
 			Cell c1 = nodesRow.createCell(2);
 			c1.setCellValue("#Nodes");
 		}
 		{
 			Cell c1 = nodesRow.createCell(4);
 			c1.setCellStyle(borderStyle);
 			c1.setCellValue(opSummary.totalNodes());
 		}
 
 		{
 			Cell c1 = nodesRow.createCell(5);
 			c1.setCellStyle(borderStyle);
 			c1.setCellValue(opSummary.totalNodeRag(RAG.RED));
 		}
 
 		{
 			Cell c1 = nodesRow.createCell(6);
 			c1.setCellStyle(borderStyle);
 			c1.setCellValue(opSummary.totalNodeRag(RAG.AMBER));
 		}
 
 		{
 			Cell c1 = nodesRow.createCell(7);
 			c1.setCellStyle(borderStyle);
 			c1.setCellValue(opSummary.totalNodeRag(RAG.GREEN));
 		}
 	}
 
 	private void writeResourcesSummary(Sheet sheet, CellStyle borderStyle) {
 		Row resourcesRow = sheet.createRow(RESOURCES_ROW);
 
 		{
 			Cell c1 = resourcesRow.createCell(2);
 			c1.setCellValue("#Resources");
 		}
 		{
 			Cell c1 = resourcesRow.createCell(4);
 			c1.setCellStyle(borderStyle);
 			c1.setCellValue(opSummary.totalResources());
 		}
 
 		{
 			Cell c1 = resourcesRow.createCell(5);
 			c1.setCellStyle(borderStyle);
 			c1.setCellValue(opSummary.totalNetXResourceRag(RAG.RED));
 		}
 
 		{
 			Cell c1 = resourcesRow.createCell(6);
 			c1.setCellStyle(borderStyle);
 			c1.setCellValue(opSummary.totalNetXResourceRag(RAG.AMBER));
 		}
 
 		{
 			Cell c1 = resourcesRow.createCell(7);
 			c1.setCellStyle(borderStyle);
 			c1.setCellValue(opSummary.totalNetXResourceRag(RAG.GREEN));
 		}
 	}
 
 	private RFSServiceSummary processService(Service service) {
 
 		// Get a summary without background computation and progress monitor.
 		IMonitoringSummary serviceSummary = monitoringStateModel
 				.summary(service);
		serviceSummary.addContextObject(getPeriod());
 		serviceSummary.compute(new NullProgressMonitor());
 
 		if (serviceSummary instanceof RFSServiceSummary) {
 
 			final ReportingEngine engine = (ReportingEngine) getEngine();
 			engine.setService(service);
 			engine.setServiceSummary((RFSServiceSummary) serviceSummary);
 			engine.setJobMonitor(getJobMonitor());
 			engine.setDataProvider(this.getDataProvider());
 			engine.setPeriod(getPeriod());
 			engine.execute();
 			if (engine.getFailures().size() > 0) {
 				// Don't add anmy
 				this.getFailures().addAll(engine.getFailures());
 			}
 			return (RFSServiceSummary) serviceSummary;
 		}
 		return null;
 	}
 
 }
