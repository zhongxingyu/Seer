 /*******************************************************************************
  * Copyright (c) May 20, 2011 NetXForge.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  You should have received a copy of the GNU Lesser General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: 
  * 	Martin Taal - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.server.test.actions;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.eclipse.emf.cdo.CDOObject;
 import org.eclipse.emf.cdo.util.CommitException;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
 import com.google.common.collect.Lists;
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.netxforge.netxstudio.common.model.ModelUtils;
 import com.netxforge.netxstudio.data.IDataProvider;
 import com.netxforge.netxstudio.generics.GenericsPackage;
 import com.netxforge.netxstudio.geo.GeoPackage;
 import com.netxforge.netxstudio.library.Equipment;
 import com.netxforge.netxstudio.library.Expression;
 import com.netxforge.netxstudio.library.Function;
 import com.netxforge.netxstudio.library.LevelType;
 import com.netxforge.netxstudio.library.LibraryFactory;
 import com.netxforge.netxstudio.library.LibraryPackage;
 import com.netxforge.netxstudio.library.NodeType;
 import com.netxforge.netxstudio.library.Tolerance;
 import com.netxforge.netxstudio.library.Unit;
 import com.netxforge.netxstudio.metrics.DatabaseTypeType;
 import com.netxforge.netxstudio.metrics.IdentifierDataKind;
 import com.netxforge.netxstudio.metrics.MappingCSV;
 import com.netxforge.netxstudio.metrics.MappingColumn;
 import com.netxforge.netxstudio.metrics.MappingRDBMS;
 import com.netxforge.netxstudio.metrics.MappingXLS;
 import com.netxforge.netxstudio.metrics.Metric;
 import com.netxforge.netxstudio.metrics.MetricSource;
 import com.netxforge.netxstudio.metrics.MetricsFactory;
 import com.netxforge.netxstudio.metrics.MetricsPackage;
 import com.netxforge.netxstudio.metrics.ObjectKindType;
 import com.netxforge.netxstudio.metrics.ValueDataKind;
 import com.netxforge.netxstudio.metrics.ValueKindType;
 import com.netxforge.netxstudio.operators.FunctionRelationship;
 import com.netxforge.netxstudio.operators.Network;
 import com.netxforge.netxstudio.operators.Node;
 import com.netxforge.netxstudio.operators.OperatorsFactory;
 import com.netxforge.netxstudio.operators.OperatorsPackage;
 import com.netxforge.netxstudio.protocols.ProtocolsPackage;
 import com.netxforge.netxstudio.scheduling.JobState;
 import com.netxforge.netxstudio.scheduling.MetricSourceJob;
 import com.netxforge.netxstudio.scheduling.RFSServiceJob;
 import com.netxforge.netxstudio.scheduling.RFSServiceRetentionJob;
 import com.netxforge.netxstudio.scheduling.SchedulingFactory;
 import com.netxforge.netxstudio.scheduling.SchedulingPackage;
 import com.netxforge.netxstudio.server.dataimport.MasterDataImporter;
 import com.netxforge.netxstudio.server.service.NetxForgeService;
 import com.netxforge.netxstudio.server.test.base.TestModule;
 import com.netxforge.netxstudio.server.test.dataprovider.NonStatic;
 import com.netxforge.netxstudio.services.RFSService;
 import com.netxforge.netxstudio.services.ServicesFactory;
 import com.netxforge.netxstudio.services.ServicesPackage;
 
 /**
  * Creates test data. Steps:
  * <ol>
  * <li>Start with an empty database</li>
  * <li>Start the server</li>
  * <li>Run this testcase</li>
  * </ol>
  * 
  * @author Martin Taal
  */
 public class TestDataCreator implements NetxForgeService {
 
 	private static final int HIERARCHY_DEPTH = 2;
 	private static final int HIERARCHY_BREADTH = 2;
 	private static final String RFS_NAME = "Speech";
 	private static final String MS_XLS_NAME = "SGSN_Attached_Users";
 	private static final String MS_CSV_NAME = "TEKELEC";
 	private static final String MS_DB_PG_NAME = "DB_PG";
 	private static final String MS_DB_ORACLE_NAME = "DB_ORACLE";
 	private static final int MINUTE = 60000;
 
 	@Inject
 	@NonStatic
 	private IDataProvider dataProvider;
 
 	@Inject
 	private ModelUtils modelUtils;
 
 	private List<Tolerance> tl = Lists.newArrayList();
 	private Expression utilizationExpression = null;
 	private Expression retentionExpression = null;
 	private Expression capacityExpression = null;
 
 	private List<FunctionRelationship> functionRelationships = new ArrayList<FunctionRelationship>();
 
 	public Object run(Map<String, String> parameters) {
 		try {
 			Guice.createInjector(TestModule.getModule()).injectMembers(this);
 
 			dataProvider.setDoGetResourceFromOwnTransaction(false);
 			dataProvider.openSession("admin", "admin");
 
 			create();
 
 			return "done";
 		} catch (final Exception e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	public void create() throws Exception {
 		// clear data does not always work, get timeout
 		// etc.
 		// start with an empty database
 		// clearData();
 
 		dataProvider.getTransaction();
 		importMetrics();
 		createRFSService();
 		createXLSMetricSource();
 		createCVSMetricSource();
 		createDBOracleMetricSource();
 		createDBPGMetricSource();
 		dataProvider.commitTransaction();
 	}
 
 	private void clearData() {
 		dataProvider.getTransaction();
 		// note order of clearing is important because of
 		// dependencies
 		clearResourcesForEPackage(SchedulingPackage.eINSTANCE);
 		clearResourcesForEPackage(ServicesPackage.eINSTANCE);
 		clearResourcesForEPackage(ProtocolsPackage.eINSTANCE);
 		clearResourcesForEPackage(OperatorsPackage.eINSTANCE);
 		clearResourcesForEPackage(MetricsPackage.eINSTANCE);
 		clearResourcesForEPackage(LibraryPackage.eINSTANCE);
 		clearResourcesForEPackage(GeoPackage.eINSTANCE);
 		clearResourcesForEPackage(GenericsPackage.eINSTANCE);
 		dataProvider.commitTransaction();
 	}
 
 	private void clearResourcesForEPackage(
 			org.eclipse.emf.ecore.EPackage ePackage) {
 		for (final EClassifier eClassifier : ePackage.getEClassifiers()) {
 			if (eClassifier instanceof EClass) {
 				final EClass eClass = (EClass) eClassifier;
 				dataProvider.getResource(eClass).getContents().clear();
 			}
 		}
 	}
 
 	private void createRFSService() {
 
 		final Network network = OperatorsFactory.eINSTANCE.createNetwork();
 		network.setName("t-mobile");
 		addToResource(network);
 
 		final RFSService rfsService = ServicesFactory.eINSTANCE
 				.createRFSService();
 		rfsService.setServiceName(RFS_NAME);
 		addToResource(rfsService);
 		rfsService.getNodes().add(createNode("YPSGSN3"));
 		rfsService.getNodes().add(createNode("RTSGSN3"));
 		rfsService.getNodes().add(createARNSTPNode());
 		network.getNodes().addAll(rfsService.getNodes());
 		network.getFunctionRelationships().addAll(functionRelationships);
 
 		final RFSServiceJob job = SchedulingFactory.eINSTANCE
 				.createRFSServiceJob();
 		job.setRFSService(rfsService);
 		job.setJobState(JobState.IN_ACTIVE);
 		job.setStartTime(modelUtils.toXMLDate(new Date(System
 				.currentTimeMillis() + 2 * MINUTE)));
 		job.setInterval(600);
 		job.setName(rfsService.getServiceName());
 
 		// add to the job resource, that one is watched by the jobhandler
 		dataProvider.getResource(SchedulingPackage.Literals.JOB).getContents()
 				.add(job);
 
 		final RFSServiceRetentionJob retentionJob = SchedulingFactory.eINSTANCE
 				.createRFSServiceRetentionJob();
 		retentionJob.setRFSService(rfsService);
 		retentionJob.setJobState(JobState.ACTIVE);
 		retentionJob.setStartTime(modelUtils.toXMLDate(new Date(System
 				.currentTimeMillis() + 2 * MINUTE)));
 		retentionJob.setInterval(600);
 		retentionJob.setName(rfsService.getServiceName() + "Retention");
 
 		// add to the job resource, that one is watched by the jobhandler
 		dataProvider.getResource(SchedulingPackage.Literals.JOB).getContents()
 				.add(retentionJob);
 	}
 
 	private void addToResource(CDOObject cdoObject) {
 		dataProvider.getResource(cdoObject.eClass()).getContents()
 				.add(cdoObject);
 	}
 
 	public void importMetrics() throws Exception {
 		// first create a set of standard units
 		getUnit("[%]");
 		getUnit("[#]");
 		getUnit("KS");
 		getUnit("KB");
 		getUnit("[Erlangs]");
 		getUnit("[mE]");
 		getUnit("[cell/s]");
 		getUnit("[byte]");
 		getUnit("[kB]");
 		getUnit("[kbit/s]");
 		getUnit("[mErlangs]");
 		getUnit("[min]");
 		getUnit("#");
 		getUnit("Octets");
 
 		final InputStream is = this.getClass().getResourceAsStream(
 				"data/metrics16052011.xls");
 		final MasterDataImporter masterDataImporter = new MasterDataImporter();
 		masterDataImporter.setDataProvider(dataProvider);
 		masterDataImporter.setEClassToImport(MetricsPackage.eINSTANCE
 				.getMetric());
 		masterDataImporter.process(new HSSFWorkbook(is));
 	}
 
 	private MetricSource createCVSMetricSource() throws CommitException {
 		// create the Metricsource
 		final MetricSource metricSource = MetricsFactory.eINSTANCE
 				.createMetricSource();
 		metricSource.setName(MS_CSV_NAME);
 		metricSource.setMetricLocation("TEKELEC");
 
 		final MappingCSV mappingCSV = MetricsFactory.eINSTANCE
 				.createMappingCSV();
 		metricSource.setMetricMapping(mappingCSV);
 
 		setCSVMapping(mappingCSV);
 
 		addToResource(metricSource);
 
 		// create a metric source job
 		final MetricSourceJob msJob = SchedulingFactory.eINSTANCE
 				.createMetricSourceJob();
 		msJob.setInterval(600);
 		msJob.setJobState(JobState.IN_ACTIVE);
 		msJob.setName(MS_CSV_NAME);
 		msJob.setStartTime(modelUtils.toXMLDate(new Date(System
 				.currentTimeMillis() + 2 * MINUTE)));
 		msJob.setMetricSource(metricSource);
 
 		// add to the job resource, that one is watched by the jobhandler
 		dataProvider.getResource(SchedulingPackage.Literals.JOB).getContents()
 				.add(msJob);
 
 		return metricSource;
 	}
 
 	private MetricSource createDBPGMetricSource() throws CommitException {
 		// create the Metricsource
 		final MetricSource metricSource = MetricsFactory.eINSTANCE
 				.createMetricSource();
 		metricSource.setName(MS_DB_PG_NAME);
 		metricSource
 				.setMetricLocation("jdbc:postgresql://localhost:5432/DB_TM");
 
 		final MappingRDBMS mapping = MetricsFactory.eINSTANCE
 				.createMappingRDBMS();
 		metricSource.setMetricMapping(mapping);
 		mapping.setHeaderRow(0);
 		mapping.setFirstDataRow(0);
 		mapping.setUser("TM");
 		mapping.setPassword("TM");
 		mapping.setDatabaseType(DatabaseTypeType.POSTGRES);
 
 		setDBMapping(mapping);
 
 		addToResource(metricSource);
 
 		// create a metric source job
 		final MetricSourceJob msJob = SchedulingFactory.eINSTANCE
 				.createMetricSourceJob();
 		msJob.setInterval(600);
 		msJob.setJobState(JobState.IN_ACTIVE);
 		msJob.setName(MS_DB_PG_NAME);
 		msJob.setStartTime(modelUtils.toXMLDate(new Date(System
 				.currentTimeMillis() + 2 * MINUTE)));
 		msJob.setMetricSource(metricSource);
 
 		// add to the job resource, that one is watched by the jobhandler
 		dataProvider.getResource(SchedulingPackage.Literals.JOB).getContents()
 				.add(msJob);
 
 		return metricSource;
 	}
 
 	private MetricSource createDBOracleMetricSource() throws CommitException {
 		// create the Metricsource
 		final MetricSource metricSource = MetricsFactory.eINSTANCE
 				.createMetricSource();
 		metricSource.setName(MS_DB_ORACLE_NAME);
 		metricSource
 				.setMetricLocation("jdbc:oracle:thin:@192.168.22.33:1521:oss");
 
 		final MappingRDBMS mapping = MetricsFactory.eINSTANCE
 				.createMappingRDBMS();
 		metricSource.setMetricMapping(mapping);
 		mapping.setHeaderRow(0);
 		mapping.setFirstDataRow(0);
 		mapping.setUser("capman1");
 		mapping.setPassword("HadronC0llider");
 		mapping.setDatabaseType(DatabaseTypeType.ORACLE);
 
 		setDBMapping(mapping);
 
 		addToResource(metricSource);
 
 		// create a metric source job
 		final MetricSourceJob msJob = SchedulingFactory.eINSTANCE
 				.createMetricSourceJob();
 		msJob.setInterval(600);
 		msJob.setJobState(JobState.IN_ACTIVE);
 		msJob.setName(MS_DB_ORACLE_NAME);
 		msJob.setStartTime(modelUtils.toXMLDate(new Date(System
 				.currentTimeMillis() + 2 * MINUTE)));
 		msJob.setMetricSource(metricSource);
 
 		// add to the job resource, that one is watched by the jobhandler
 		dataProvider.getResource(SchedulingPackage.Literals.JOB).getContents()
 				.add(msJob);
 
 		return metricSource;
 	}
 
 	private MetricSource createXLSMetricSource() throws CommitException {
 		// create the Metricsource
 		final MetricSource metricSource = MetricsFactory.eINSTANCE
 				.createMetricSource();
 		metricSource.setName(MS_XLS_NAME);
 		metricSource.setMetricLocation("SGSN"); // /" + MS_NAME + ".xls
 
 		final MappingXLS mappingXLS = MetricsFactory.eINSTANCE
 				.createMappingXLS();
 		metricSource.setMetricMapping(mappingXLS);
 
 		setXLSMapping(mappingXLS);
 
 		addToResource(metricSource);
 
 		// create a metric source job
 		final MetricSourceJob msJob = SchedulingFactory.eINSTANCE
 				.createMetricSourceJob();
 		msJob.setInterval(600);
 		msJob.setJobState(JobState.IN_ACTIVE);
 		msJob.setName(MS_XLS_NAME);
 		msJob.setStartTime(modelUtils.toXMLDate(new Date(System
 				.currentTimeMillis() + 2 * MINUTE)));
 		msJob.setMetricSource(metricSource);
 
 		// add to the job resource, that one is watched by the jobhandler
 		dataProvider.getResource(SchedulingPackage.Literals.JOB).getContents()
 				.add(msJob);
 		return metricSource;
 	}
 
 	private Expression createOrGetUtilizationExpression() {
 
 		if (utilizationExpression != null) {
 			return utilizationExpression;
 		}
 
 		// Utilization expression.
 		utilizationExpression = LibraryFactory.eINSTANCE.createExpression();
 		utilizationExpression.setName("Utilization Expression");
 
 		// 1st Context is a NetXResource
 		// 2nd Context is the Resource monitoring period.
 		// This returns an expressionresult with a list of values dividing the
 		// metrics values by the capacity values.
 		// The Timestamp of the result is not set at the moment.
 		final String eAsString = "this UTILIZATION 60 = this METRIC 60 / this CAP 60;";
 		utilizationExpression.getExpressionLines().addAll(
 				getExpressionLines(eAsString));
 		addToResource(utilizationExpression);
 		return utilizationExpression;
 	}
 
 	private Expression createOrGetRetentionExpression() {
 
 		if (retentionExpression != null) {
 			return retentionExpression;
 		}
 
 		// Utilization expression.
 		retentionExpression = LibraryFactory.eINSTANCE.createExpression();
 		retentionExpression.setName("Retention Expression");
 
 		// 1st Context is a NetXResource
 		// 2nd Context is the Resource monitoring period.
 		// This returns an expressionresult with a list of values dividing the
 		// metrics values by the capacity values.
 		// The Timestamp of the result is not set at the moment.
 		final String eAsString = "this METRIC 1440 = this METRIC 60.max();";
 		retentionExpression.getExpressionLines().addAll(
 				getExpressionLines(eAsString));
 		addToResource(retentionExpression);
 		return retentionExpression;
 	}
 
 	private Expression createOrGetCapacityExpression() {
 		if (capacityExpression != null) {
 			return capacityExpression;
 		}
 		capacityExpression = LibraryFactory.eINSTANCE.createExpression();
 		capacityExpression.setName("Capacity Expression");
 		// For this expression:
 		// The context should be a NODE which has:
 		// -Equipments with equipment code = BOARD
 		// -Function named SGSN which has a resource expression name
 		// ="Gb_mode_max_attached_users(number)"
 		// Note I: The grammar can't deal with spaces in the reference to the
 		// Resource expression name, so
 		// when you create the resource, you should add underscores in the
 		// expression name of the resource.
 		// Note II: The expression result, will return a single value, which
 		// should be populated accross the whole period context.
 		final String eAsString = "this.FUNCTION YPSGSN3 -> RESOURCE Gb_mode_max_attached_users_number_ CAP 60 = this.EQUIPMENT BOARD.count() * 5;";
 		capacityExpression.getExpressionLines().addAll(
 				getExpressionLines(eAsString));
 		addToResource(capacityExpression);
 		return capacityExpression;
 	}
 
 	private Collection<String> getExpressionLines(String Expression) {
 		final String[] splitByNewLine = Expression.split("\n");
 		final Collection<String> collection = Lists
 				.newArrayList(splitByNewLine);
 		return collection;
 	}
 
 	private void setXLSMapping(MappingXLS mappingXLS) {
 		mappingXLS.setFirstDataRow(11);
 		mappingXLS.setHeaderRow(0);
 		mappingXLS.setSheetNumber(0);
 
 		final MappingColumn dateColumn = createValueColumn("Start Time", 0,
 				ValueKindType.DATETIME);
 		mappingXLS.getMappingColumns().add(dateColumn);
 		((ValueDataKind) dateColumn.getDataType())
 				.setFormat("MM/dd/yyyy hh:mm:ss");
 
 		mappingXLS.getMappingColumns().add(
 				createValueColumn("Period", 1, ValueKindType.PERIOD));
 		mappingXLS.getMappingColumns().add(
 				createValueColumn("Gb mode max attached users(number)", 4,
 						ValueKindType.METRIC));
 		mappingXLS.getMappingColumns().add(
 				createValueColumn("Iu mode max attached users(number)", 5,
 						ValueKindType.METRIC));
 		mappingXLS.getMappingColumns().add(
 				createIdentifierColumn(2, ObjectKindType.NODE,
 						OperatorsPackage.eINSTANCE.getNode_NodeID().getName()));
 		mappingXLS
 				.getMappingColumns()
 				.add(createIdentifierColumn(3, ObjectKindType.FUNCTION,
 						LibraryPackage.eINSTANCE.getComponent_Name().getName()));
 	}
 
 	private void setCSVMapping(MappingCSV mappingCSV) {
 		mappingCSV.setHeaderRow(1);
 		mappingCSV.setFirstDataRow(4);
 		mappingCSV.setDelimiter(",");
 		mappingCSV.setPeriodHint(60);
 
 		final MappingColumn dateColumn = createValueColumn("Start Date", 7,
 				ValueKindType.DATE);
 		mappingCSV.getHeaderMappingColumns().add(dateColumn);
 		// MM/dd/yyyy hh:mm:ss
 		((ValueDataKind) dateColumn.getDataType()).setFormat("yyyy-MM-dd");
 		final MappingColumn timeColumn = createValueColumn("Start Time", 8,
 				ValueKindType.TIME);
 		((ValueDataKind) timeColumn.getDataType()).setFormat("HH:mm:ss");
 		mappingCSV.getHeaderMappingColumns().add(timeColumn);
 		mappingCSV.getHeaderMappingColumns().add(
 				createIdentifierColumn(0, ObjectKindType.NODE,
 						OperatorsPackage.eINSTANCE.getNode_NodeID().getName()));
 
 		mappingCSV.getMappingColumns().add(
 				createValueColumn("MSGSRCVD", 6, ValueKindType.METRIC));
 		mappingCSV.getMappingColumns().add(
 				createValueColumn("MSURETRN", 7, ValueKindType.METRIC));
 		mappingCSV.getMappingColumns().add(
 				createValueColumn("MOCTTRAN", 8, ValueKindType.METRIC));
 		mappingCSV.getMappingColumns().add(
 				createIdentifierColumn(1, ObjectKindType.RELATIONSHIP,
 						OperatorsPackage.eINSTANCE.getRelationship_Name()
 								.getName()));
 		mappingCSV
 				.getMappingColumns()
 				.add(createIdentifierColumn(3, ObjectKindType.FUNCTION,
 						LibraryPackage.eINSTANCE.getComponent_Name().getName()));
 	}
 
 	private void setDBMapping(MappingRDBMS mapping) {
 		mapping.setDateFormat("dd.MM.yyyy");
 		mapping.setTimeFormat("HH:mm");
 		final String qry = "select UTP_MO.CO_NAME AS NAME,to_char(PERIOD_START_TIME,'dd.mm.yyyy hh24:mi') "
 				+ "as PERIOD_START_TIME_h, RNS_PS_MLANEMBT_TRA1_RAW.* from RNS_PS_MLANEMBT_TRA1_RAW,UTP_MO "
 				+ "where RNS_PS_MLANEMBT_TRA1_RAW.MSC_ID=UTP_MO.CO_GID "
 				+ "and to_char(PERIOD_START_TIME,'dd.mm.yyyy hh24:mi')>=to_char('${startDate}') ||' ${endTime}' "
 				+ "and to_char(PERIOD_START_TIME,'dd.mm.yyyy hh24:mi')<=to_char('${endDate}') ||' ${endTime}' ";
 		mapping.setQuery(qry);
 
 		mapping.getMappingColumns().add(
 				createIdentifierColumn(0, ObjectKindType.NODE,
 						OperatorsPackage.eINSTANCE.getNode_NodeID().getName()));
 
 		final MappingColumn dateColumn = createValueColumn("Start Date", 1,
 				ValueKindType.DATETIME);
 		((ValueDataKind) dateColumn.getDataType())
 				.setFormat("MM.dd.yyyy HH:mm");
 		mapping.getMappingColumns().add(dateColumn);
 
 		mapping.getMappingColumns()
 				.add(createIdentifierColumn(3, ObjectKindType.FUNCTION,
 						LibraryPackage.eINSTANCE.getComponent_Name().getName()));
 		mapping.getMappingColumns().add(
 				createIdentifierColumn(3, ObjectKindType.EQUIPMENT,
 						LibraryPackage.eINSTANCE.getEquipment_EquipmentCode()
 								.getName()));
 		mapping.getMappingColumns().add(
 				createIdentifierColumn(4, ObjectKindType.EQUIPMENT,
 						LibraryPackage.eINSTANCE.getEquipment_EquipmentCode()
 								.getName()));
 
 		mapping.getMappingColumns().add(
 				createValueColumn("LANEMB_LOAD_RATE", 9, ValueKindType.METRIC));
 		mapping.getMappingColumns()
 				.add(createValueColumn("LANEMB_PEAK_RATE", 10,
 						ValueKindType.METRIC));
 		mapping.getMappingColumns().add(
 				createValueColumn("LANEMB_PEAK_TIME_IN_SEC", 11,
 						ValueKindType.METRIC));
 	}
 
 	private MappingColumn createIdentifierColumn(int columnNo,
 			ObjectKindType objectKind, String objectProperty) {
 		final MappingColumn column = MetricsFactory.eINSTANCE
 				.createMappingColumn();
 		column.setColumn(columnNo);
 		final IdentifierDataKind kind = MetricsFactory.eINSTANCE
 				.createIdentifierDataKind();
 		kind.setObjectKind(objectKind);
 		kind.setObjectProperty(objectProperty);
 		column.setDataType(kind);
 		return column;
 	}
 
 	private MappingColumn createValueColumn(String metricName, int columnNo,
 			ValueKindType valueKindType) {
 		final MappingColumn column = MetricsFactory.eINSTANCE
 				.createMappingColumn();
 		column.setColumn(columnNo);
 		final ValueDataKind valueDataKind = MetricsFactory.eINSTANCE
 				.createValueDataKind();
 		valueDataKind.setValueKind(valueKindType);
 		column.setDataType(valueDataKind);
 		if (valueDataKind.getValueKind() == ValueKindType.METRIC) {
 			valueDataKind.setMetricRef(getMetric(metricName));
 		}
 		return column;
 	}
 
 	private Metric getMetric(String name) {
 		final Resource resource = dataProvider
 				.getResource(MetricsPackage.eINSTANCE.getMetric());
 		for (final EObject eObject : resource.getContents()) {
 			if (eObject instanceof Metric) {
 				final Metric metric = (Metric) eObject;
 				if (metric.getName() != null && metric.getName().equals(name)) {
 					return metric;
 				}
 			}
 		}
 
 		// create one
 		final Metric metric = MetricsFactory.eINSTANCE.createMetric();
 		metric.setDescription("name");
 		metric.setName(name);
 		metric.setUnitRef(getUnit("#"));
 		addToResource(metric);
 		return metric;
 	}
 
 	private Unit getUnit(String unitCode) {
 		final Resource resource = dataProvider
 				.getResource(LibraryPackage.eINSTANCE.getUnit());
 		for (final EObject eObject : resource.getContents()) {
 			if (eObject instanceof Unit) {
 				final Unit unit = (Unit) eObject;
 				if (unit.getCode().equals(unitCode)) {
 					return unit;
 				}
 			}
 		}
 		final Unit unit = LibraryFactory.eINSTANCE.createUnit();
 		if (unitCode.length() > 3) {
 			unit.setCode(unitCode.substring(0, 3));
 		} else {
 			unit.setCode(unitCode);
 		}
 		unit.setDescription(unitCode);
 		unit.setName(unitCode);
 		resource.getContents().add(unit);
 		return unit;
 	}
 
 	private Node createNode(String id) {
 		final NodeType nodeType = LibraryFactory.eINSTANCE.createNodeType();
 		nodeType.getFunctions().addAll(createFunctions(id, 0));
 		nodeType.getEquipments().addAll(createEquipments(id, 0));
 
 		final Node node = OperatorsFactory.eINSTANCE.createNode();
 		node.setNodeID(id);
 		node.setNodeType(nodeType);
 		final NodeType originalNodeType = EcoreUtil.copy(nodeType);
 		addToResource(originalNodeType);
 		node.setOriginalNodeTypeRef(originalNodeType);
 		return node;
 	}
 
 	private java.util.List<Equipment> createEquipments(String id, int level) {
 		final java.util.List<Equipment> equipments = new ArrayList<Equipment>();
 		final List<Tolerance> tls = createOrGetTolerances();
 		for (int i = 0; i < HIERARCHY_BREADTH; i++) {
 			final Equipment equipment = LibraryFactory.eINSTANCE
 					.createEquipment();
 			equipments.add(equipment);
 			if (level == HIERARCHY_DEPTH && i == (HIERARCHY_BREADTH - 1)) {
 				equipment.setName(id);
 				equipment.getMetricRefs().add(
 						getMetric("Gb mode max attached users(number)"));
 				equipment.getMetricRefs().add(
 						getMetric("Iu mode max attached users(number)"));
 				{ // Load the utilization expression.
 					final Expression e = this
 							.createOrGetUtilizationExpression();
 					equipment.setUtilizationExpressionRef(e);
 				}
 				{// Load the cap expression.
 					final Expression e = this.createOrGetCapacityExpression();
 					equipment.setCapacityExpressionRef(e);
 				}
 				{// Add various tolerance refs.
 					equipment.getToleranceRefs().addAll(tls);
 				}
 				{// Add various retention refs.
 					final Expression e = createOrGetRetentionExpression();
 					equipment.setRetentionExpressionRef(e);
 				}
 			} else {
 				equipment.setName(id + "_" + level + "_" + i);
 			}
 
 			// equipment.setEquipmentCode(equipment.getName());
 			equipment.setEquipmentCode("BOARD");
 			if (level <= HIERARCHY_DEPTH) {
 				equipment.getEquipments().addAll(
 						createEquipments(id, level + 1));
 			}
 		}
 		return equipments;
 	}
 
 	private java.util.List<Function> createFunctions(String id, int level) {
 		final java.util.List<Function> functions = new ArrayList<Function>();
 		final List<Tolerance> tls = createOrGetTolerances();
 		for (int i = 0; i < HIERARCHY_BREADTH; i++) {
 			final Function function = LibraryFactory.eINSTANCE.createFunction();
 			functions.add(function);
 			if (level == HIERARCHY_DEPTH && i == (HIERARCHY_BREADTH - 1)) {
 				function.setName(id);
 				function.getMetricRefs().add(
 						getMetric("Gb mode max attached users(number)"));
 				function.getMetricRefs().add(
 						getMetric("Iu mode max attached users(number)"));
 				{ // Load the utilization expression.
 					final Expression e = this
 							.createOrGetUtilizationExpression();
 					function.setUtilizationExpressionRef(e);
 				}
 				{// Load the cap expression.
 					final Expression e = this.createOrGetCapacityExpression();
 					function.setCapacityExpressionRef(e);
 				}
 				{// Add various tolerance refs.
 					function.getToleranceRefs().addAll(tls);
 				}
 				{// Add various tolerance refs.
 					final Expression e = this.createOrGetRetentionExpression();
 					function.setRetentionExpressionRef(e);
 				}
 
 			} else {
 				function.setName(id + "_" + level + "_" + i);
 			}
 			if (level <= HIERARCHY_DEPTH) {
 				function.getFunctions().addAll(createFunctions(id, level + 1));
 			}
 		}
 		return functions;
 	}
 
 	/**
 	 * Create various tolerance levels, with each an own expression.
 	 * 
 	 * 
 	 * 
 	 * @return
 	 */
 	private List<Tolerance> createOrGetTolerances() {
 
 		if (tl.size() != 0) {
 			return tl;
 		}
 
 		{
 			final Tolerance t = LibraryFactory.eINSTANCE.createTolerance();
 			t.setLevel(LevelType.RED);
 			t.setName("Tolerance Red");
 
 			final Expression te = LibraryFactory.eINSTANCE.createExpression();
 			te.setName("Tolerance Red");
 
 			// Context is a NetXResource
 			// Takes the capacity range and multiplies it by a factor someting.
 			// No expression result will be created, as we don't assign the
 			// result to a range.
 			// you could use the result set of the last scope (this is what the
 			// evaluation returns).
 
 			final String eAsString = "this TOLERANCE 60 = this CAP 60 * 0.9;";
 			te.getExpressionLines().addAll(getExpressionLines(eAsString));
 			addToResource(te);
 			t.setExpressionRef(te);
 
 			// Context is a Node
 			tl.add(t);
 			this.addToResource(t);
 		}
 		{
 			final Tolerance t = LibraryFactory.eINSTANCE.createTolerance();
 			t.setLevel(LevelType.AMBER);
 			t.setName("Tolerance Amber");
 
 			final Expression te = LibraryFactory.eINSTANCE.createExpression();
 			te.setName("Tolerance Amber");
 			// Context is a Node
 			final String eAsString = "this TOLERANCE 60 = this CAP * 0.7;";
 			te.getExpressionLines().addAll(getExpressionLines(eAsString));
 			addToResource(te);
 			t.setExpressionRef(te);
 
 			tl.add(t);
 			this.addToResource(t);
 		}
 		{
 			final Tolerance t = LibraryFactory.eINSTANCE.createTolerance();
 			t.setLevel(LevelType.GREEN);
 			t.setName("Tolerance Green");
 
 			final Expression te = LibraryFactory.eINSTANCE.createExpression();
 			te.setName("Tolerance Green");
 			// Context is a Node
 			final String eAsString = "this TOLERANCE 60 = this CAP * 0.5;";
 			te.getExpressionLines().addAll(getExpressionLines(eAsString));
 
 			addToResource(te);
 			t.setExpressionRef(te);
 
 			tl.add(t);
 			this.addToResource(t);
 		}
 		{
 			final Tolerance t = LibraryFactory.eINSTANCE.createTolerance();
 			t.setLevel(LevelType.GREEN);
 			t.setName("Tolerance Yellow");
 
 			final Expression te = LibraryFactory.eINSTANCE.createExpression();
 			te.setName("Tolerance Yellow");
 			// Context is a Node
 			final String eAsString = "this TOLERANCE 60 = this CAP * 0.3;";
 			te.getExpressionLines().addAll(getExpressionLines(eAsString));
 			this.addToResource(te);
 
 			t.setExpressionRef(te);
 
 			tl.add(t);
 			this.addToResource(t);
 		}
 		return tl;
 	}
 
 	private Node createARNSTPNode() {
 		final String[] relationShips = new String[] { "tmscnlna0",
 				"sgrroamna0", "tmscnlna0s", "acr01na0s", "thlrnlna0",
 				"sgc01na0", "thlrnlna0s", "sgrstpin0", "sgrstpin0", "lsm3ua2",
 				"tbmscukin0", "arm04na0", "arm04na0", "acr01na0s", "acr01na0s",
 				"lsm3ua", "lsm3ua2", "drin01na0", "drin01na0", "kpnamin0",
 				"kpnrtin0", "kpnrtna1", "btfrastp", "kpnutna1", "sgrstpin0",
 				"sgrstpin0", "lsm3ua", "kpnrtin0", "lsm3ua", "lsm3ua2",
 				"kpnrtna1", "lsm3ua", "lsm3ua2", "tmscnlna0", "tmscnlna0s",
 				"acr01na0s", "thlrnlna0", "sgc01na0", "thlrnlna0s",
 				"sgrstpna0", "sgrstpna0", "tbmscukin0", "arm04na0", "arm04na0",
 				"acr01na0s", "acr01na0s", "drin01na0", "drin01na0", "kpnamin0",
 				"kpnrtin0", "tele2rosgw", "tele2rosgw", "sgrstpna0",
 				"sgrstpna0", "acr01na0s", "acr01na0s", "sgm01na0", "sgm01na0",
 				"kpnamin0", "kpnrtin0", "sgrstpna0s", "sgrstpna0s",
 				"tmscukna1", "arm04na0", "arm04na0", "kpnamin0", "kpnrtin0",
 				"drin01na0", "drin01na0", "kpnamin0", "kpnrtin0", "onlinemgw3",
 				"sgrstpna0s", "sgrstpna0s", "sgc01na0", "sgc01na0",
 				"sgc01na0s", "sgc01na0s", "tbmscnlin0", "tbmscnlin0",
 				"sgm01na0", "sgm01na0", "kpnamin0", "kpnrtin0", "sgrstpna1",
 				"sgrstpna1", "arm04na0", "arm04na0", "kpnamin0", "kpnrtin0",
				"drin01na0", "drin01na0", "etes01", "acr01na0", "bruhin0",
 				"bruiin0", "tele2amstp", "sgrstpna1", "sgrstpna1", "sgc01na0",
 				"sgc01na0", "sgc01na0s", "sgc01na0s", "drin01na0", "drin01na0",
 				"sgm01na0", "sgm01na0", "bruhin0", "bruiin0", "arm04na0",
 				"arm04na0", "kpnamin0", "kpnrtin0", "acr01na0", "acr01na0",
 				"bruhin0", "bruiin0", "tele2rosgw", "tele2rosgw", "tele2amstp",
 				"tele2rostp", "sgc01na0", "sgc01na0", "sgc01na0s", "sgc01na0s",
 				"drin01na0", "drin01na0", "acr01na0", "acr01na0", "sgm01na0",
 				"sgm01na0", "bruhin0", "bruiin0", "arm04na0", "arm04na0",
 				"kpnamin0", "kpnrtin0", "acr01na0", "bruhin0", "bruiin0",
 				"sgc01na0", "sgc01na0", "sgc01na0s", "sgc01na0s", "drin01na0",
 				"drin01na0", "arm04na0", "arm04na0", "bruhin0", "bruiin0",
 				"acr01na0s", "acr01na0s", "tele2rosgw", "tele2rosgw",
 				"drin01na0", "drin01na0", "acr01na0", "acr01na0", "tmdblfin0",
 				"sgc01na0s", "sgc01na0s", "arm04na0", "arm04na0", "bruhin0",
 				"bruiin0", "acr01na0s", "acr01na0s", "kpnamin0", "kpnrtin0",
 				"sgc01na0s", "sgc01na0s", "kpnamin0", "kpnrtin0", "bruhin0",
 				"bruiin0", "acr01na0", "acr01na0", "dtdusin0", "kpnamin0",
 				"kpnrtin0", "sgc01na0s", "sgc01na0s", "kpnamin0", "kpnrtin0",
 				"bruhin0", "bruiin0", "acr01na0", "acr01na0", "etmsc01na1",
 				"etams01", "bruhin0", "bruiin0", "sgc01na0s", "sgc01na0s",
 				"kpnamin0", "kpnrtin0", "vfamsgw2", "vfamsgw2", "sgc01na0",
 				"sgc01na0", "kpnutna1", "kpnrtna1", "kpnutna1", "kpnrtna1",
 				"bruhin0", "bruiin0", "kpnutna1", "vfrmsgw2", "vfrmsgw2",
 				"sgm01na0", "sgm01na0", "sgc01na0", "sgc01na0", "bruhin0",
 				"bruiin0", "acr01na0", "acr01na0", "kpnamin0", "onlinemgw2",
 				"kpnm1na1", "sgm01na0", "sgm01na0", "sgc01na0", "sgc01na0",
 				"bruhin0", "bruiin0", "bruhin0", "bruiin0", "acr01na0",
 				"acr01na0", "btfrastp", "bruhin0", "bruiin0", "kpnm1na1",
 				"tele2rostp", "sgm01na0", "sgm01na0", "kpnamin0", "kpnrtin0",
 				"acr01na0s", "acr01na0s", "kpnm2na1", "kpnm2na1", "dtfrain0",
 				"sgm01na0", "sgm01na0", "bruhin0", "bruiin0", "tmdblfin0",
 				"acr01na0s", "acr01na0s", "tele2rosgw", "tele2rosgw" };
 
 		final List<Integer> incrementIndex = new ArrayList<Integer>();
 		incrementIndex.add(33);
 		incrementIndex.add(54);
 		incrementIndex.add(78);
 		incrementIndex.add(103);
 		incrementIndex.add(125);
 		incrementIndex.add(144);
 		incrementIndex.add(154);
 		incrementIndex.add(167);
 		incrementIndex.add(178);
 		incrementIndex.add(189);
 		incrementIndex.add(198);
 		incrementIndex.add(209);
 		incrementIndex.add(220);
 		incrementIndex.add(235);
 		incrementIndex.add(244);
 
 		final Node node = OperatorsFactory.eINSTANCE.createNode();
 
 		// create the node/function
 		final NodeType nodeType = LibraryFactory.eINSTANCE.createNodeType();
 		int index = 0;
 		int i = 0;
 		for (final String relationShipName : relationShips) {
 			if (incrementIndex.contains(index)) {
 				i++;
 			}
 			nodeType.getFunctions().add(
 					createFunction("A" + (i > 0 ? i : ""), node,
 							relationShipName));
 			nodeType.getFunctions().add(
 					createFunction("B" + (i > 0 ? i : ""), node,
 							relationShipName));
 			index++;
 		}
 		node.setNodeID("arnstp01");
 		node.setNodeType(nodeType);
 		final NodeType originalNodeType = EcoreUtil.copy(nodeType);
 		addToResource(originalNodeType);
 		node.setOriginalNodeTypeRef(originalNodeType);
 		return node;
 	}
 
 	private Function createFunction(String name, Node node,
 			String relationShipName) {
 
 		final Function f = LibraryFactory.eINSTANCE.createFunction();
 		f.setName(name);
 
 		f.getMetricRefs().add(getMetric("MSGSRCVD"));
 		f.getMetricRefs().add(getMetric("MSURETRN"));
 		f.getMetricRefs().add(getMetric("MOCTTRAN"));
 
 		// f.getToleranceRefs().addAll(createOrGetTolerances());
 		// f.setCapacityExpressionRef(createOrGetCapacityExpression());
 		// f.setUtilizationExpressionRef(createOrGetUtilizationExpression());
 
 		final FunctionRelationship r = OperatorsFactory.eINSTANCE
 				.createFunctionRelationship();
 		r.setName(relationShipName);
 		r.setFunction1Ref(f);
 		r.setFunction2Ref(f);
 		r.setNodeID1Ref(node);
 		r.setNodeID2Ref(node);
 		functionRelationships.add(r);
 
 		f.getFunctionRelationshipRefs().add(r);
 
 		return f;
 	}
 
 	public IDataProvider getDataProvider() {
 		return dataProvider;
 	}
 
 	public void setDataProvider(IDataProvider dataProvider) {
 		this.dataProvider = dataProvider;
 	}
 
 	public void setModelUtils(ModelUtils modelUtils) {
 		this.modelUtils = modelUtils;
 	}
 
 }
