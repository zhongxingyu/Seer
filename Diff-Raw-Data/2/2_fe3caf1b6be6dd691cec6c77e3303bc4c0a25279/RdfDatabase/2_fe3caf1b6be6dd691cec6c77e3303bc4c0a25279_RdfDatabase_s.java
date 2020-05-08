 /**
  * 
  */
 package sim.server;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.UUID;
 
 import org.ontoware.rdf2go.model.Model;
 import org.ontoware.rdf2go.model.QueryResultTable;
 import org.ontoware.rdf2go.model.Statement;
 import org.ontoware.rdf2go.model.node.DatatypeLiteral;
 import org.ontoware.rdf2go.model.node.Node;
 import org.ontoware.rdf2go.model.node.PlainLiteral;
 import org.ontoware.rdf2go.model.node.URI;
 import org.ontoware.rdf2go.util.RDFTool;
 import org.openrdf.rdf2go.RepositoryModel;
 import org.openrdf.repository.http.HTTPRepository;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sim.data.ApplicationId;
 import sim.data.Context;
 import sim.data.MethodMetrics;
 import sim.data.MetricsVisitor;
 import sim.data.PlatformMetrics;
 import sim.data.SystemId;
 import sim.data.SystemMetrics;
 import sim.server.data.CompoundMetric;
 import sim.server.data.Metric;
 import sim.server.util.SPARQLQueryContentAnalyzer;
 
 
 /**
  * @author valer
  *
  */
 public class RdfDatabase implements MetricsVisitor {
 
 	private static final Logger logger = LoggerFactory.getLogger(RdfDatabase.class);
 	
 	public static final String QUERY_CONTENT = "QueryContent";
 	public static final String NUMBER_OF_PLUGINS = "NumberOfPlugin";
 	
 	private Model model;
 
 	private String simNS;
 	private String rdfNS;
 	private String rdfsNS;	
 	private String xsdNS;
 	
 	private URI typePredicateURI;
 	private URI rdfLiURI;
 	private URI hasSystemMetricURI;
 	private URI hasNameURI;
 	private URI hasDataValueURI;
 	private URI hasTimeStampURI;
 	
 	private URI hasMethodMetricURI;
 	private URI hasMethodNameURI;
 	private URI hasClassNameURI;
 	private URI hasExceptionURI;
 	private URI hasEndedWithErrorURI;
 	private URI hasBeginExecutionTimeURI;
 	private URI hasEndExecutionTimeURI;
 	
 	private URI hasParentContextURI;
 	private URI hasContextURI;
 	private URI hasMetricsURI;
 	
 	private URI longDatatypeURI;
 	private URI doubleDatatypeURI;
 	private URI dateTimeDatatypeURI;
 	private URI booleanDatatypeURI;
 	private URI integerDatatypeURI;
 
 	private URI hasMethodExecutionURI;
 	private URI isMethodExecutionOfURI;
 	private URI hasMeasurementURI;
 	private URI isMeasurementOfURI;
 	private URI includesURI;
 	
 	private URI methodTypeURI;
 	private URI methodExecutionTypeURI;
 	
 	private URI systemTypeURI;
 	private URI applicationTypeURI;
 	
 	private URI hasPlatformMetricURI;
 	
 	private URI bagTypeURI;
 	
 	private static final HashMap<String, URI> methodURICache = new HashMap<String, URI>();
 	private static final HashMap<String, URI> systemURICache = new HashMap<String, URI>();
 	private static final HashMap<String, URI> applicationURICache = new HashMap<String, URI>();
 		
 	public RdfDatabase() {
 	}
 	
 	public void open() {
 		this.model = new RepositoryModel(new HTTPRepository("http://" + Main.storage_server_domain + ":" + Main.storage_server_port + "/openrdf-sesame", Main.storage_repository_id));
 		this.model.open();
 		this.model.setAutocommit(false);
 		
 		simNS = model.getNamespace("sim");
 		if (simNS == null) {
 			model.setNamespace("sim", "http://www.larkc.eu/ontologies/IMOntology.rdf#");
 			model.commit();
 			simNS = model.getNamespace("sim");
 			if (simNS == null) {
 				simNS = "http://www.larkc.eu/ontologies/IMOntology.rdf#"; //this is for OWLIM, the get returned null check http://www.mail-archive.com/owlim-discussion@ontotext.com/msg00772.html
 			}
 		}
 		rdfNS = model.getNamespace("rdf");
 		if (rdfNS == null) {
 			model.setNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
 			model.commit();
 			rdfNS = model.getNamespace("rdf");
 		}
 		rdfsNS = model.getNamespace("rdfs");
 		if (rdfsNS == null) {
 			model.setNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
 			model.commit();
 			rdfsNS = model.getNamespace("rdfs");
 		}
 
 		xsdNS = model.getNamespace("xsd");
 		if (xsdNS == null) {
 			model.setNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
 			model.commit();
 			xsdNS = model.getNamespace("xsd");
 		}
 		
 		typePredicateURI = model.createURI(rdfNS + "type");
 		rdfLiURI = model.createURI(rdfNS + "li");
 		hasSystemMetricURI = model.createURI(simNS + "hasSystemMetric");
 		hasNameURI = model.createURI(simNS + "hasName");
 		hasDataValueURI = model.createURI(simNS + "hasDataValue");
 		hasTimeStampURI = model.createURI(simNS + "hasTimeStamp");
 		
 		hasMethodMetricURI = model.createURI(simNS + "hasMethodMetric");
 		hasMethodNameURI = model.createURI(simNS + "hasMethodName");
 		hasClassNameURI = model.createURI(simNS + "hasClassName");
 		hasExceptionURI = model.createURI(simNS + "hasException");
 		hasEndedWithErrorURI = model.createURI(simNS + "hasEndedWithError");
 		hasBeginExecutionTimeURI = model.createURI(simNS + "hasBeginExecutionTime");
 		hasEndExecutionTimeURI = model.createURI(simNS + "hasEndExecutionTime");
 		
 		hasParentContextURI = model.createURI(simNS + "hasParentContext");
 		hasContextURI = model.createURI(simNS + "hasContext");
 		hasMetricsURI = model.createURI(simNS + "hasMetrics");
 		
 		longDatatypeURI = model.createURI(xsdNS + "long");
 		doubleDatatypeURI = model.createURI(xsdNS + "double");
 		dateTimeDatatypeURI = model.createURI(xsdNS + "dateTime");
 		booleanDatatypeURI = model.createURI(xsdNS + "boolean");
 		integerDatatypeURI = model.createURI(xsdNS + "int");
 		
 		hasMethodExecutionURI = model.createURI(simNS + "hasMethodExecution");
 		isMethodExecutionOfURI = model.createURI(simNS + "isMethodExecutionOf");
 		hasMeasurementURI = model.createURI(simNS + "hasMeasurement");
 		isMeasurementOfURI = model.createURI(simNS + "isMeasurementOf");
 		includesURI = model.createURI(simNS + "includes");
 		
 		methodTypeURI = model.createURI(simNS + "Method");
 		methodExecutionTypeURI = model.createURI(simNS + "MethodExecution");
 		
 		systemTypeURI = model.createURI(simNS + "System");
 		applicationTypeURI = model.createURI(simNS + "Application");
 		
		hasMethodMetricURI = model.createURI(simNS + "hasPlatformMetric");
 		
 		bagTypeURI = model.createURI(simNS + "Bag");
 	}
 	
 	public void close() {
 		this.model.close();
 	}
 
 	private PlainLiteral getStringTypeURI(String value) {
 		return model.createPlainLiteral(value);
 	}
 
 	private DatatypeLiteral getLongTypeURI(long value) {
 		return model.createDatatypeLiteral(String.valueOf(value), longDatatypeURI);
 	}
 
 	private DatatypeLiteral getDoubleTypeURI(double value) {
 		return model.createDatatypeLiteral(String.valueOf(value), doubleDatatypeURI);
 	}
 
 	public long getDateTimeLong(DatatypeLiteral datatypeLiteral){
 		long result = 0;
 		try {
 			result = RDFTool.string2Date(datatypeLiteral.getValue()).getTime();
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}		
 		return result;
 	}
 	
 	private DatatypeLiteral getDateTimeTypeURI(long dateTimeLong) {
 		Date dateTime = new Date(dateTimeLong);
 		return model.createDatatypeLiteral(RDFTool.dateTime2String(dateTime), dateTimeDatatypeURI);
 	}
 
 	private DatatypeLiteral getBooleanTypeURI(boolean value) {
 		return model.createDatatypeLiteral(String.valueOf(value), booleanDatatypeURI);
 	}
 
 	private DatatypeLiteral getIntegerTypeURI(int value) {
 		return model.createDatatypeLiteral(String.valueOf(value), integerDatatypeURI);
 	}
 
 	@Override
 	public void visit(MethodMetrics methodMetrics) {
 		List<Statement> statements = new ArrayList<Statement>();
 		
 		DatatypeLiteral dateTimeLiteral = getDateTimeTypeURI(methodMetrics.getCreationTime());
 		
 		URI idSystemURI = addSystem(methodMetrics.getSystemId(), statements);
 		URI idApplicationURI = addApplication(methodMetrics.getMethod().getApplicationId(), statements);
 
 		String methodID = methodMetrics.getMethod().getClassName() + "." + methodMetrics.getMethod().getMethodName().replace("<init>", "new");
 		
 		URI idMethodURI = methodURICache.get(methodID);
 		if(idMethodURI == null){		
 			idMethodURI = model.createURI(simNS + methodID);			
 			statements.add(model.createStatement(idMethodURI, typePredicateURI, methodTypeURI));
 			statements.add(model.createStatement(idMethodURI, hasMethodNameURI, model.createPlainLiteral(methodMetrics.getMethod().getMethodName())));
 			statements.add(model.createStatement(idMethodURI, hasClassNameURI, model.createPlainLiteral(methodMetrics.getMethod().getClassName())));
 			methodURICache.put(methodID, idMethodURI);
 		}
 		
 		URI idMethodMetricsURI = generateURI();
 		statements.add(model.createStatement(idMethodMetricsURI, typePredicateURI, methodExecutionTypeURI));
 		statements.add(model.createStatement(idMethodMetricsURI, isMethodExecutionOfURI, idMethodURI));
 		statements.add(model.createStatement(idMethodMetricsURI, hasBeginExecutionTimeURI, getLongTypeURI(methodMetrics.getBeginExecutionTime())));
 		statements.add(model.createStatement(idMethodMetricsURI, hasEndExecutionTimeURI, getLongTypeURI(methodMetrics.getEndExecutionTime())));
 		statements.add(model.createStatement(idMethodMetricsURI, hasEndedWithErrorURI, getBooleanTypeURI(methodMetrics.endedWithError())));
 		if (methodMetrics.getException() != null) {
 			statements.add(model.createStatement(idMethodMetricsURI, hasExceptionURI, model.createPlainLiteral(methodMetrics.getException())));
 		}
 		
 		URI idContextURI;
 		if (methodMetrics.getContextId() == null)
 			idContextURI = null;
 		else
 			idContextURI = model.createURI(simNS + methodMetrics.getContextId());
 
 		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "WallClockTime", getLongTypeURI(methodMetrics.getWallClockTime())));
 		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadUserCPUTime", getLongTypeURI(methodMetrics.getThreadUserCpuTime())));
 		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadSystemCPUTime", getLongTypeURI(methodMetrics.getThreadSystemCpuTime())));
 		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadTotalCPUTime", getLongTypeURI(methodMetrics.getThreadTotalCpuTime())));
 		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadCount", getLongTypeURI(methodMetrics.getThreadCount())));
 		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadBlockCount", getLongTypeURI(methodMetrics.getThreadBlockCount())));
 		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadBlockTime", getLongTypeURI(methodMetrics.getThreadBlockTime())));
 		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadWaitCount", getLongTypeURI(methodMetrics.getThreadWaitCount())));
 		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadWaitTime", getLongTypeURI(methodMetrics.getThreadWaitTime())));
 		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadGccCount", getLongTypeURI(methodMetrics.getThreadGccCount())));
 		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadGccTime", getLongTypeURI(methodMetrics.getThreadGccTime())));
 		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ProcessTotalCPUTime", getLongTypeURI(methodMetrics.getProcessTotalCpuTime())));
 		
 		
 		/**
 		 * if the method execution is the method execution of specific methods that correspond to
 		 * atomic metrics than the corresponding atomic metrics are written in the RDF storage
 		 */
 		List<Statement> atomicMetricsStatements = processMetric(methodMetrics,idContextURI,dateTimeLiteral);
 		if(atomicMetricsStatements.size()>0)		
 			statements.addAll(atomicMetricsStatements);
 
 		model.addAll(statements.iterator());
 		model.commit();
 	}
 	
 	@Override
 	public void visit(SystemMetrics systemMetrics) {
 		List<Statement> statements = new ArrayList<Statement>();
 		
 		DatatypeLiteral dateTimeLiteral = getDateTimeTypeURI(systemMetrics.getCreationTime());
 		
 		//System metric
 		URI idSystemURI = addSystem(systemMetrics.getSystemId(), statements);
 		
 		//SystemLoadAverage
 		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "SystemLoadAverage", getDoubleTypeURI(systemMetrics.getSystemLoadAverage())));
 		
 		//TotalSystemFreeMemory
 		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "TotalSystemFreeMemory", getLongTypeURI(systemMetrics.getTotalSystemFreeMemory())));
 		
 		//TotalSystemUsedMemory
 		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "TotalSystemUsedMemory", getLongTypeURI(systemMetrics.getTotalSystemUsedMemory())));
 		
 		//TotalSystemUsedSwap
 		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "TotalSystemUsedSwap", getLongTypeURI(systemMetrics.getTotalSystemUsedSwap())));
 
 		//SystemOpenFileDescriptors
 		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "SystemOpenFileDescriptorCount", getLongTypeURI(systemMetrics.getSystemOpenFileDescriptors())));
 		
 		//SwapIn
 		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "SwapIn", getLongTypeURI(systemMetrics.getSwapIn())));
 
 		//SwapOut
 		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "SwapOut", getLongTypeURI(systemMetrics.getSwapOut())));
 
 		//IORead
 		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "IORead", getLongTypeURI(systemMetrics.getIORead())));
 
 		//IOWrite
 		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "IOWrite", getLongTypeURI(systemMetrics.getIOWrite())));
 
 		//UserPerc
 		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "UserCPULoad", getDoubleTypeURI(systemMetrics.getUserPerc())));
 
 		//SysPerc
 		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "SystemCPULoad", getDoubleTypeURI(systemMetrics.getSysPerc())));
 
 		//IdlePerc //TODO
 		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "IdleCPULoad", getDoubleTypeURI(systemMetrics.getIdlePerc())));
 
 		//WaitPerc //TODO
 		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "WaitCPULoad", getDoubleTypeURI(systemMetrics.getWaitPerc())));
 		
 		//IrqPerc //TODO
 		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "IrqCPULoad", getDoubleTypeURI(systemMetrics.getIrqPerc())));
 		
 		//User
 		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "UserCPUTime", getDoubleTypeURI(systemMetrics.getUser())));
 		
 		//Sys
 		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "SystemCPUTime", getDoubleTypeURI(systemMetrics.getSys())));
 		
 		//Idle
 		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "IdleCPUTime", getDoubleTypeURI(systemMetrics.getIdle())));
 		
 		//Wait
 		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "WaitCPUTime", getDoubleTypeURI(systemMetrics.getWait())));
 		
 		//Irq
 		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "IrqCPUTime", getDoubleTypeURI(systemMetrics.getIrq())));
 		
 		model.addAll(statements.iterator());
 		model.commit();
 	}
 
 	private List<Statement> createSystemMetricStatements(URI idSystemURI, DatatypeLiteral dateTimeLiteral, String type, Node value) {
 		List<Statement> statements = new ArrayList<Statement>();
 		URI idURI = generateURI();
 		statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + type)));
 		statements.add(model.createStatement(idURI, hasDataValueURI, value));
 		statements.add(model.createStatement(idURI, hasTimeStampURI, dateTimeLiteral));
 		statements.add(model.createStatement(idSystemURI, hasSystemMetricURI, idURI));
 		
 		return statements;
 	}
 
 	private List<Statement> createMethodMetricStatements(URI idSystemURI, URI idApplicationURI, URI idContextURI, URI idMethodURI, DatatypeLiteral dateTimeLiteral, String type, Node value) {
 		List<Statement> statements = new ArrayList<Statement>();
 		URI idURI = generateURI();
 		statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + type)));
 		statements.add(model.createStatement(idURI, hasDataValueURI, value));
 		statements.add(model.createStatement(idURI, hasTimeStampURI, dateTimeLiteral));
 		if (idContextURI != null) {
 			statements.add(model.createStatement(idURI, hasContextURI, idContextURI));
 		}
 		statements.add(model.createStatement(idSystemURI, hasMethodMetricURI, idURI));
 		statements.add(model.createStatement(idApplicationURI, hasMethodMetricURI, idURI));
 		statements.add(model.createStatement(idMethodURI, hasMethodMetricURI, idURI));
 		
 		return statements;
 	}
 
 	private List<Statement> createAtomicMetricStatements(URI idContextURI, DatatypeLiteral dateTimeLiteral, String type, Node value) {
 		List<Statement> statements = new ArrayList<Statement>();
 		URI idURI = generateURI();
 		statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + type)));
 		statements.add(model.createStatement(idURI, hasDataValueURI, value));
 		statements.add(model.createStatement(idURI, hasTimeStampURI, dateTimeLiteral));
 		if (idContextURI != null) {
 			statements.add(model.createStatement(idURI, hasContextURI, idContextURI));
 		}
 		return statements;
 	}
 
 
 	private List<Statement> createPlatformMetricStatements(URI idSystemURI, URI idApplicationURI, DatatypeLiteral dateTimeLiteral, String type, Node value) {
 		List<Statement> statements = new ArrayList<Statement>();
 		URI idURI = generateURI();
 		statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + type)));
 		statements.add(model.createStatement(idURI, hasDataValueURI, value));
 		statements.add(model.createStatement(idURI, hasTimeStampURI, dateTimeLiteral));
 		statements.add(model.createStatement(idSystemURI, hasPlatformMetricURI, idURI));
 		statements.add(model.createStatement(idApplicationURI, hasPlatformMetricURI, idURI));
 		
 		return statements;
 	}
 
 	private URI addSystem(SystemId systemId, List<Statement> statements) {
 		//System metric
 		URI idSystemURI = systemURICache.get(systemId.getId());
 		if (idSystemURI == null) {
 			idSystemURI = model.createURI(simNS + systemId.getId());
 			statements.add(model.createStatement(idSystemURI, typePredicateURI, systemTypeURI));
 			statements.add(model.createStatement(idSystemURI, hasNameURI, getStringTypeURI(systemId.getName())));
 		}
 		return idSystemURI;
 	}
 
 	private URI addApplication(ApplicationId applicationId, List<Statement> statements) {
 		URI idApplicationURI = applicationURICache.get(applicationId.getId());
 		if (idApplicationURI == null) {
 			idApplicationURI = model.createURI(simNS + applicationId.getId());
 			statements.add(model.createStatement(idApplicationURI, typePredicateURI, applicationTypeURI));
 			statements.add(model.createStatement(idApplicationURI, hasNameURI, getStringTypeURI(applicationId.getName())));
 		}
 		return idApplicationURI;
 	}
 
 	private URI addContext(Context context, List<Statement> statements) {
 		if (context == null) {
 			return null;
 		}
 		URI idContextURI = model.createURI(simNS + context.getId());
 		statements.add(model.createStatement(idContextURI, typePredicateURI, model.createURI(simNS + context.getName())));
 		if (context.getParentContextId() != null) {
 			URI idParentContextURI = model.createURI(simNS + context.getParentContextId());
 			statements.add(model.createStatement(idContextURI, hasParentContextURI, idParentContextURI));
 		}
 		URI idBagURI = model.createURI(simNS + context.getId() + "-metrics");
 		statements.add(model.createStatement(idBagURI, typePredicateURI, bagTypeURI));
 		statements.add(model.createStatement(idContextURI, hasMetricsURI, idBagURI));
 
 		for (Entry<String, Object> entry : context.entrySet()) {
 			URI idBagValueURI = generateURI();
 			statements.add(model.createStatement(idBagValueURI, typePredicateURI, model.createURI(simNS + entry.getKey())));
 			statements.add(model.createStatement(idBagValueURI, hasDataValueURI, model.createPlainLiteral(entry.getValue().toString())));
 			statements.add(model.createStatement(idBagURI, rdfLiURI, idBagValueURI));
 		}
 		
 
 		/*	check if the context contains information about SPARQL query;
 		if that's the case the SPARQL query is parsed and query atomic metrics
 		are created for this query
 		 */
 		if(context.containsKey(QUERY_CONTENT)){
 			String query = context.get(QUERY_CONTENT).toString();
 			SPARQLQueryContentAnalyzer sqa = new SPARQLQueryContentAnalyzer(query);
 			if (sqa.parseQuery()) {
 	
 				URI idURI = generateURI();
 				statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + "QueryDataSetSourcesNb")));
 				statements.add(model.createStatement(idURI, hasDataValueURI, getIntegerTypeURI(sqa.getQueryDataSetSourcesNb())));
 				statements.add(model.createStatement(idURI, hasTimeStampURI, getDateTimeTypeURI(context.getCreationTime())));
 				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
 	
 				idURI = generateURI();
 				statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + "QueryNamespaceNb")));
 				statements.add(model.createStatement(idURI, hasDataValueURI, getIntegerTypeURI(sqa.getQueryNamespaceNb())));
 				statements.add(model.createStatement(idURI, hasTimeStampURI, getDateTimeTypeURI(context.getCreationTime())));
 				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
 	
 				idURI = generateURI();
 				statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + "QueryOperatorsNb")));
 				statements.add(model.createStatement(idURI, hasDataValueURI, getIntegerTypeURI(sqa.getQueryOperatorsNb())));
 				statements.add(model.createStatement(idURI, hasTimeStampURI, getDateTimeTypeURI(context.getCreationTime())));
 				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
 	
 				idURI = generateURI();
 				statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + "QueryResultLimitNb")));
 				statements.add(model.createStatement(idURI, hasDataValueURI, getIntegerTypeURI(sqa.getQueryResultLimitNb())));
 				statements.add(model.createStatement(idURI, hasTimeStampURI, getDateTimeTypeURI(context.getCreationTime())));
 				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
 			
 				idURI = generateURI();
 				statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + "QueryResultOffsetNb")));
 				statements.add(model.createStatement(idURI, hasDataValueURI, getIntegerTypeURI(sqa.getQueryResultOffsetNb())));
 				statements.add(model.createStatement(idURI, hasTimeStampURI, getDateTimeTypeURI(context.getCreationTime())));
 				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
 			
 				idURI = generateURI();
 				statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + "QueryResultOrderingNb")));
 				statements.add(model.createStatement(idURI, hasDataValueURI, getIntegerTypeURI(sqa.getQueryResultOrderingNb())));
 				statements.add(model.createStatement(idURI, hasTimeStampURI, getDateTimeTypeURI(context.getCreationTime())));
 				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
 										
 				idURI = generateURI();
 				statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + "QuerySizeInCharacters")));
 				statements.add(model.createStatement(idURI, hasDataValueURI, getIntegerTypeURI(sqa.getQuerySizeInCharacters())));
 				statements.add(model.createStatement(idURI, hasTimeStampURI, getDateTimeTypeURI(context.getCreationTime())));
 				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
 	
 				idURI = generateURI();
 				statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + "QueryVariablesNb")));
 				statements.add(model.createStatement(idURI, hasDataValueURI, getIntegerTypeURI(sqa.getQueryVariablesNb())));
 				statements.add(model.createStatement(idURI, hasTimeStampURI, getDateTimeTypeURI(context.getCreationTime())));
 				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
 			}
 		}
 		
 		/*	check if the context contains information about number of plugins
 		    in a workflow and if that's the case create workflow number of plugins 
 		    atomic metric
 		 */
 		if(context.containsKey(NUMBER_OF_PLUGINS)){
 			int numberOfPlugins = new Integer(context.get(NUMBER_OF_PLUGINS).toString()).intValue();
 
 			URI idURI = generateURI();
 			statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + "WorkflowNumerOfPlugins")));
 			statements.add(model.createStatement(idURI, hasDataValueURI, getIntegerTypeURI(numberOfPlugins)));
 			statements.add(model.createStatement(idURI, hasTimeStampURI, getDateTimeTypeURI(context.getCreationTime())));
 			statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));			
 		}
 
 		return idContextURI;
 	}
 
 	@Override
 	public void visit(Context context) {
 		List<Statement> statements = new ArrayList<Statement>();	
 		addContext(context, statements);
 		model.addAll(statements.iterator());
 		model.commit();			
 	}
 
 	public URI generateURI(){
 		return model.createURI(simNS + UUID.randomUUID().toString());
 	}
 	
 	public QueryResultTable sparqlSelect(String queryString){
 		return model.sparqlSelect(queryString);
 	}
 
 	public URI createURI(String uriString){
 		return model.createURI(simNS + uriString);
 	}
 
 	public String getSimNS() {
 		return simNS;
 	}
 
 	public String getRdfNS() {
 		return rdfNS;
 	}
 
 	public String getRdfsNS() {
 		return rdfsNS;
 	}
 
 	public String getXsdNS() {
 		return xsdNS;
 	}
 	
 	public void visit(CompoundMetric compoundMetric) {
 		List<Statement> statements = new ArrayList<Statement>();
 				
 		URI idCompoundMetricURI = generateURI();
 		compoundMetric.setId(idCompoundMetricURI);
 		
 		statements.add(model.createStatement(idCompoundMetricURI, typePredicateURI, compoundMetric.getType()));
 		for(Metric m : compoundMetric.getConstituentMetrics()){
 			statements.add(model.createStatement(idCompoundMetricURI, includesURI, m.getId()));
 		}		
 		statements.add(model.createStatement(idCompoundMetricURI, hasDataValueURI, getDoubleTypeURI(new Double(compoundMetric.getValue()).doubleValue())));
 		
 		model.addAll(statements.iterator());
 		model.commit();
 	}
 	
 	@Override
 	public void visit(PlatformMetrics pm) {
 		List<Statement> statements = new ArrayList<Statement>();
 
 		DatatypeLiteral dateTimeLiteral = getDateTimeTypeURI(pm.getCreationTime());
 		
 		URI idSystemURI = addSystem(pm.getSystemId(), statements);
 		URI idApplicationURI = addApplication(pm.getApplicationId(), statements);
 		
 		pm.getAvgCpuUsage();
 		pm.getCpuTime();
 		pm.getCpuUsage();
 		pm.getGccCount();
 		pm.getGccTime();
 		pm.getUptime();
 		pm.getUsedMemory();
 
 		statements.addAll(createPlatformMetricStatements(idSystemURI, idApplicationURI, dateTimeLiteral, "AvgCPUUsage", getDoubleTypeURI(pm.getAvgCpuUsage())));
 		statements.addAll(createPlatformMetricStatements(idSystemURI, idApplicationURI, dateTimeLiteral, "CPUTime", getLongTypeURI(pm.getCpuTime())));
 		statements.addAll(createPlatformMetricStatements(idSystemURI, idApplicationURI, dateTimeLiteral, "CPUUsage", getDoubleTypeURI(pm.getCpuUsage())));
 		statements.addAll(createPlatformMetricStatements(idSystemURI, idApplicationURI, dateTimeLiteral, "GccCount", getLongTypeURI(pm.getCpuTime())));
 		statements.addAll(createPlatformMetricStatements(idSystemURI, idApplicationURI, dateTimeLiteral, "GccTime", getLongTypeURI(pm.getCpuTime())));
 		statements.addAll(createPlatformMetricStatements(idSystemURI, idApplicationURI, dateTimeLiteral, "Uptime", getLongTypeURI(pm.getCpuTime())));
 		statements.addAll(createPlatformMetricStatements(idSystemURI, idApplicationURI, dateTimeLiteral, "UsedMemory", getLongTypeURI(pm.getCpuTime())));
 	}
 
 	
 	private List<Statement> processMetric(MethodMetrics methodMetrics, URI idContextURI, DatatypeLiteral dateTimeLiteral){
 		
 		List<Statement> statements = new ArrayList<Statement>();
 		String methodID = methodMetrics.getMethod().getClassName() + "." + methodMetrics.getMethod().getMethodName().replace("<init>", "new");
 
 		/**
 		 * if the method execution is the method execution of 
 		 * eu.larkc.core.endpoint.sparql.SparqlHandler.handle
 		 * we write query related atomic metrics
 		 */		
 		if(methodID.equals("eu.larkc.core.endpoint.sparql.SparqlHandler.handle")){					
 			//write an instance of QueryBeginExecutionTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "QueryBeginExecutionTime", getLongTypeURI(methodMetrics.getBeginExecutionTime()));						
 			//write an instance of QueryEndExecutionTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "QueryEndExecutionTime", getLongTypeURI(methodMetrics.getEndExecutionTime()));
 			//write an instance of QueryErrorStatus
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "QueryErrorStatus", getBooleanTypeURI(methodMetrics.endedWithError()));
 			//write an instance of QueryTotalResponseTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "QueryTotalResponseTime", getLongTypeURI(methodMetrics.getWallClockTime()));			
 			//write an instance of QueryThreadUserCPUTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "QueryThreadUserCPUTime", getLongTypeURI(methodMetrics.getThreadUserCpuTime()));			
 			//write an instance of QueryThreadSystemCPUTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "QueryThreadSystemCPUTime", getLongTypeURI(methodMetrics.getThreadSystemCpuTime()));			
 			//write an instance of QueryThreadTotalCPUTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "QueryThreadTotalCPUTime", getLongTypeURI(methodMetrics.getThreadTotalCpuTime()));			
 			//write an instance of QueryThreadCount
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "QueryThreadCount", getLongTypeURI(methodMetrics.getThreadCount()));			
 			//write an instance of QueryThreadBlockCount
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "QueryThreadBlockCount", getLongTypeURI(methodMetrics.getThreadBlockCount()));			
 			//write an instance of QueryThreadBlockTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "QueryThreadBlockTime", getLongTypeURI(methodMetrics.getThreadBlockTime()));			
 			//write an instance of QueryThreadWaitCount
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "QueryThreadWaitCount", getLongTypeURI(methodMetrics.getThreadWaitCount()));			
 			//write an instance of QueryThreadWaitTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "QueryThreadWaitTime", getLongTypeURI(methodMetrics.getThreadWaitTime()));			
 			//write an instance of QueryThreadGccCount
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "QueryThreadGccCount", getLongTypeURI(methodMetrics.getThreadGccCount()));			
 			//write an instance of QueryThreadGccTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "QueryThreadGccTime", getLongTypeURI(methodMetrics.getThreadGccTime()));			
 			//write an instance of QueryProcessTotalCPUTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "QueryProcessTotalCPUTime", getLongTypeURI(methodMetrics.getProcessTotalCpuTime()));			
 		}
 		
 		/**
 		 * if the method execution is the method execution of 
 		 * eu.larkc.core.executor.Executor.execute
 		 * we write workflow related atomic metrics
 		 */		
 		if(methodID.equals("eu.larkc.core.executor.Executor.execute") || methodID.equals("eu.larkc.core.executor.Executor.getNextResults")){	
 			//write an instance of WorkflowTotalResponseTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "WorkflowTotalResponseTime", getLongTypeURI(methodMetrics.getWallClockTime()));			
 			//write an instance of WorkflowThreadUserCPUTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "WorkflowThreadUserCPUTime", getLongTypeURI(methodMetrics.getThreadUserCpuTime()));						
 			//write an instance of WorkflowThreadSystemCPUTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "WorkflowThreadSystemCPUTime", getLongTypeURI(methodMetrics.getThreadSystemCpuTime()));			
 			//write an instance of WorkflowThreadTotalCPUTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "WorkflowThreadTotalCPUTime", getLongTypeURI(methodMetrics.getThreadTotalCpuTime()));			
 			//write an instance of WorkflowThreadCount
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "WorkflowThreadCount", getLongTypeURI(methodMetrics.getThreadCount()));			
 			//write an instance of WorkflowThreadBlockCount
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "WorkflowThreadBlockCount", getLongTypeURI(methodMetrics.getThreadBlockCount()));			
 			//write an instance of WorkflowThreadBlockTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "WorkflowThreadBlockTime", getLongTypeURI(methodMetrics.getThreadBlockTime()));			
 			//write an instance of WorkflowThreadWaitCount
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "WorkflowThreadWaitCount", getLongTypeURI(methodMetrics.getThreadWaitCount()));			
 			//write an instance of WorkflowThreadWaitTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "WorkflowThreadWaitTime", getLongTypeURI(methodMetrics.getThreadWaitTime()));			
 			//write an instance of WorkflowThreadGccCount
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "WorkflowThreadGccCount", getLongTypeURI(methodMetrics.getThreadGccCount()));			
 			//write an instance of WorkflowThreadGccTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "WorkflowThreadGccTime", getLongTypeURI(methodMetrics.getThreadGccTime()));			
 			//write an instance of WorkflowProcessTotalCPUTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "WorkflowProcessTotalCPUTime", getLongTypeURI(methodMetrics.getProcessTotalCpuTime()));			
 		}
 		
 		/**
 		 * if the method execution is the method execution of 
 		 * eu.larkc.plugin.Plugin.invoke
 		 * we write plugin related atomic metrics
 		 */		
 		if(methodID.equals("eu.larkc.plugin.Plugin.invoke")){		
 			//write an instance of PluginBeginExecutionTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "PluginBeginExecutionTime", getLongTypeURI(methodMetrics.getBeginExecutionTime()));			
 			//write an instance of PluingEndExecutionTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "PluingEndExecutionTime", getLongTypeURI(methodMetrics.getEndExecutionTime()));			
 			//write an instance of PluingErrorStatus
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "PluingErrorStatus", getBooleanTypeURI(methodMetrics.endedWithError()));			
 			//write an instance of PluingTotalResponseTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "PluingTotalResponseTime", getLongTypeURI(methodMetrics.getWallClockTime()));						
 			//write an instance of PluingThreadUserCPUTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "PluingThreadUserCPUTime", getLongTypeURI(methodMetrics.getThreadUserCpuTime()));						
 			//write an instance of PluingThreadSystemCPUTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "PluingThreadSystemCPUTime", getLongTypeURI(methodMetrics.getThreadSystemCpuTime()));						
 			//write an instance of PluingThreadTotalCPUTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "PluingThreadTotalCPUTime", getLongTypeURI(methodMetrics.getThreadTotalCpuTime()));						
 			//write an instance of PluingThreadCount
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "PluingThreadCount", getLongTypeURI(methodMetrics.getThreadCount()));						
 			//write an instance of PluingThreadBlockCount
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "PluingThreadBlockCount", getLongTypeURI(methodMetrics.getThreadBlockCount()));						
 			//write an instance of PluingThreadBlockTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "PluingThreadBlockTime", getLongTypeURI(methodMetrics.getThreadBlockTime()));						
 			//write an instance of PluingThreadWaitCount
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "PluingThreadWaitCount", getLongTypeURI(methodMetrics.getThreadWaitCount()));						
 			//write an instance of PluingThreadWaitTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "PluingThreadWaitTime", getLongTypeURI(methodMetrics.getThreadWaitTime()));						
 			//write an instance of PluingThreadGccCount
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "PluingThreadGccCount", getLongTypeURI(methodMetrics.getThreadGccCount()));						
 			//write an instance of PluingThreadGccTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "PluingThreadGccTime", getLongTypeURI(methodMetrics.getThreadGccTime()));						
 			//write an instance of PluingProcessTotalCPUTime
 			createAtomicMetricStatements(idContextURI, dateTimeLiteral, "PluingProcessTotalCPUTime", getLongTypeURI(methodMetrics.getProcessTotalCpuTime()));						
 		}
 		return statements;
 	}
 
 	public Model getModel() {
 		return model;
 	}
 
 }
