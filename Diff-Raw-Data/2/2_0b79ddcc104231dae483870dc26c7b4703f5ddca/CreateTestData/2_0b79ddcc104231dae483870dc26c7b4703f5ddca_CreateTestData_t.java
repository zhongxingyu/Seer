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
 import java.util.Date;
 
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.eclipse.emf.cdo.CDOObject;
 import org.eclipse.emf.cdo.util.CommitException;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 
 import com.google.inject.Inject;
 import com.netxforge.netxstudio.common.model.ModelUtils;
 import com.netxforge.netxstudio.data.IDataProvider;
 import com.netxforge.netxstudio.generics.GenericsPackage;
 import com.netxforge.netxstudio.geo.GeoPackage;
 import com.netxforge.netxstudio.library.Equipment;
 import com.netxforge.netxstudio.library.Function;
 import com.netxforge.netxstudio.library.LibraryFactory;
 import com.netxforge.netxstudio.library.LibraryPackage;
 import com.netxforge.netxstudio.library.NodeType;
 import com.netxforge.netxstudio.library.Unit;
 import com.netxforge.netxstudio.metrics.IdentifierDataKind;
 import com.netxforge.netxstudio.metrics.MappingXLS;
 import com.netxforge.netxstudio.metrics.MappingXLSColumn;
 import com.netxforge.netxstudio.metrics.Metric;
 import com.netxforge.netxstudio.metrics.MetricSource;
 import com.netxforge.netxstudio.metrics.MetricsFactory;
 import com.netxforge.netxstudio.metrics.MetricsPackage;
 import com.netxforge.netxstudio.metrics.ObjectKindType;
 import com.netxforge.netxstudio.metrics.ValueDataKind;
 import com.netxforge.netxstudio.metrics.ValueKindType;
 import com.netxforge.netxstudio.operators.Network;
 import com.netxforge.netxstudio.operators.Node;
 import com.netxforge.netxstudio.operators.OperatorsFactory;
 import com.netxforge.netxstudio.operators.OperatorsPackage;
 import com.netxforge.netxstudio.protocols.ProtocolsPackage;
 import com.netxforge.netxstudio.scheduling.JobState;
 import com.netxforge.netxstudio.scheduling.MetricSourceJob;
 import com.netxforge.netxstudio.scheduling.RFSServiceJob;
 import com.netxforge.netxstudio.scheduling.SchedulingFactory;
 import com.netxforge.netxstudio.scheduling.SchedulingPackage;
 import com.netxforge.netxstudio.server.dataimport.MasterDataImporter;
 import com.netxforge.netxstudio.server.test.dataprovider.AbstractDataProviderTest;
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
 public class CreateTestData extends AbstractDataProviderTest {
 
 	private static final int HIERARCHY_DEPTH = 3;
 	private static final int HIERARCHY_BREADTH = 3;
 	private static final String RFS_NAME = "Speech";
 	private static final String MS_NAME = "SGSN_Attached_Users";
 	private static final int MINUTE = 60000;
 
 	@Inject
 	@NonStatic
 	private IDataProvider dataProvider;
 	@Inject
 	private ModelUtils modelUtils;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		super.getInjector().injectMembers(this);
 		dataProvider.setDoGetResourceFromOwnTransaction(false);
 		dataProvider.openSession("admin", "admin");
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		dataProvider.closeSession();
 	}
 
 	public void testCreateTestData() throws Exception {
 		// clear data does not always work, get timeout
 		// etc.
 		// start with an empty database
 		// clearData();
 
 		dataProvider.getTransaction();
 		importMetrics();
 		createRFSService();
 		createMetricSource();
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
 		network.getNodes().add(rfsService.getNodes().get(0));
 		network.getNodes().add(rfsService.getNodes().get(1));
 
 		final RFSServiceJob job = SchedulingFactory.eINSTANCE
 				.createRFSServiceJob();
 		job.setRFSService(rfsService);
 		job.setJobState(JobState.ACTIVE);
 		job.setStartTime(modelUtils.toXMLDate(new Date(System
 				.currentTimeMillis() + 2 * MINUTE)));
 		job.setInterval(600);
 		job.setName(rfsService.getServiceName());
 
 		// add to the job resource, that one is watched by the jobhandler
 		dataProvider.getResource(SchedulingPackage.Literals.JOB).getContents()
 				.add(job);
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
 
 	private MetricSource createMetricSource() throws CommitException {
 		// create the Metricsource
 		final MetricSource metricSource = MetricsFactory.eINSTANCE
 				.createMetricSource();
 		metricSource.setName(MS_NAME);
 		metricSource
 				.setMetricLocation("/com/netxforge/nextstudio/server/test/metrics/actions/"
 						+ MS_NAME + ".xls");
 
 		final MappingXLS mappingXLS = MetricsFactory.eINSTANCE
 				.createMappingXLS();
 		metricSource.setMetricMapping(mappingXLS);
 
 		setMSName1Mapping(mappingXLS);
 
 		addToResource(metricSource);
 
 		// create a metric source job
 		final MetricSourceJob msJob = SchedulingFactory.eINSTANCE
 				.createMetricSourceJob();
 		msJob.setInterval(600);
 		msJob.setJobState(JobState.ACTIVE);
 		msJob.setName(MS_NAME);
 		msJob.setStartTime(modelUtils.toXMLDate(new Date(System
 				.currentTimeMillis() + 2 * MINUTE)));
 		msJob.setMetricSource(metricSource);
 
 		// add to the job resource, that one is watched by the jobhandler
 		dataProvider.getResource(SchedulingPackage.Literals.JOB).getContents()
 				.add(msJob);
 		return metricSource;
 	}
 
 	private void setMSName1Mapping(MappingXLS mappingXLS) {
 		mappingXLS.setFirstDataRow(11);
 		mappingXLS.setHeaderRow(10);
 		mappingXLS.setSheetNumber(0);
 
 		mappingXLS.getMappingColumns().add(
 				createValueColumn("Start Time", 0, ValueKindType.DATETIME));
 		mappingXLS.getMappingColumns().add(
 				createValueColumn("Period", 1, ValueKindType.PERIOD));
 		mappingXLS.getMappingColumns().add(
 				createValueColumn("Gb mode max attached users(number)", 4,
 						ValueKindType.METRIC));
 		mappingXLS.getMappingColumns().add(
 				createValueColumn("Iu mode max attached users(number)", 5,
 						ValueKindType.METRIC));
 		mappingXLS.getMappingColumns().add(
 				createIdentifierColumn(2, 10, ObjectKindType.NODE,
 						OperatorsPackage.eINSTANCE.getNode_NodeID().getName()));
 		mappingXLS
 				.getMappingColumns()
 				.add(createIdentifierColumn(3, 10, ObjectKindType.FUNCTION,
 						LibraryPackage.eINSTANCE.getComponent_Name().getName()));
 	}
 
 	private MappingXLSColumn createIdentifierColumn(int columnNo,
 			int headerRow, ObjectKindType objectKind, String objectProperty) {
 		final MappingXLSColumn column = MetricsFactory.eINSTANCE
 				.createMappingXLSColumn();
 		column.setColumn(columnNo);
 		final IdentifierDataKind kind = MetricsFactory.eINSTANCE
 				.createIdentifierDataKind();
 		kind.setObjectKind(objectKind);
 		kind.setObjectProperty(objectProperty);
 		column.setDataType(kind);
 		return column;
 	}
 
 	private MappingXLSColumn createValueColumn(String metricName, int columnNo,
 			ValueKindType valueKindType) {
 		final MappingXLSColumn column = MetricsFactory.eINSTANCE
 				.createMappingXLSColumn();
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
 		node.setOriginalNodeTypeRef(nodeType);
 		return node;
 	}
 
 	private java.util.List<Equipment> createEquipments(String id, int level) {
 		final java.util.List<Equipment> equipments = new ArrayList<Equipment>();
 		for (int i = 0; i < HIERARCHY_BREADTH; i++) {
 			final Equipment equipment = LibraryFactory.eINSTANCE
 					.createEquipment();
 			equipments.add(equipment);
 			if (level == 0 && i == 0) {
 				equipment.setName(id);
 			} else {
 				equipment.setName(id + "_" + level + "_" + i);
 			}
 			equipment.setEquipmentCode(equipment.getName());
 			if (level <= HIERARCHY_DEPTH) {
 				equipment.getEquipments().addAll(
 						createEquipments(id, level + 1));
 			}
 		}
 		return equipments;
 	}
 
 	private java.util.List<Function> createFunctions(String id, int level) {
 		final java.util.List<Function> functions = new ArrayList<Function>();
 		for (int i = 0; i < HIERARCHY_BREADTH; i++) {
 			final Function function = LibraryFactory.eINSTANCE.createFunction();
 			functions.add(function);
 			if (level == 0 && i == 0) {
 				function.setName(id);
 			} else {
 				function.setName(id + "_" + level + "_" + i);
 			}
 			if (level <= HIERARCHY_DEPTH) {
 				function.getFunctions().addAll(createFunctions(id, level + 1));
 			}
 		}
 		return functions;
 	}
 }
