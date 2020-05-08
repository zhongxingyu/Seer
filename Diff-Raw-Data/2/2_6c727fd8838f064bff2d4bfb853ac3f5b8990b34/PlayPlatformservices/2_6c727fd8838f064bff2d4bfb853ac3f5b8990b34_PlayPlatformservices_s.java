 package eu.play_project.play_platformservices;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.jws.WebService;
 import javax.xml.ws.Endpoint;
 
 import org.objectweb.fractal.api.NoSuchInterfaceException;
 import org.objectweb.fractal.api.control.BindingController;
 import org.objectweb.fractal.api.control.IllegalBindingException;
 import org.objectweb.fractal.api.control.IllegalLifeCycleException;
 import org.objectweb.proactive.Body;
 import org.objectweb.proactive.core.component.body.ComponentEndActive;
 import org.objectweb.proactive.core.component.body.ComponentInitActive;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.Syntax;
 import com.hp.hpl.jena.sparql.serializer.PlaySerializer;
 
 import eu.play_project.dcep.api.DcepManagmentApi;
 import eu.play_project.play_commons.constants.Constants;
 import eu.play_project.play_platformservices.api.EpSparqlQuery;
 import eu.play_project.play_platformservices.api.QueryDetails;
 import eu.play_project.play_platformservices.api.QueryDispatchApi;
 import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
 import eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.historic.QueryTemplateGenerator;
 import eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime.EleGeneratorForConstructQuery;
 import eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime.StreamIdCollector;
 
 @WebService(
 		serviceName = "QueryDispatchApi",
 		portName = "QueryDispatchApiPort",
 		endpointInterface = "eu.play_project.play_platformservices.api.QueryDispatchApi")
 public class PlayPlatformservices implements QueryDispatchApi,
 		ComponentInitActive, ComponentEndActive, BindingController,
 		Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	private EleGenerator eleGenerator;
 	private DcepManagmentApi dcepManagmentApi;
 	private boolean init = false;
 
 	private Logger logger;
 
 	private Endpoint soapEndpoint;
 
 	@Override
 	public String[] listFc() {
 		return new String[] { "DcepManagmentApi" };
 	}
 
 	@Override
 	public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
 		if ("DcepManagmentApi".equals(clientItfName)) {
 			return dcepManagmentApi;
 		} else {
 			throw new NoSuchInterfaceException("DcepManagmentApi");
 		}
 	}
 
 	@Override
 	public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
 		if (clientItfName.equals("DcepManagmentApi")) {
 			dcepManagmentApi = (DcepManagmentApi) serverItf;
 		}
 	}
 
 	@Override
 	public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
 
 	}
 
 	@Override
 	public void initComponentActivity(Body body) {
 		if (!init) {
 			
 			this.logger = LoggerFactory.getLogger(this.getClass());
 		
 			logger.info("Initialising {} component.", this.getClass().getSimpleName());
 	
 			eleGenerator = new EleGeneratorForConstructQuery();
 	
 			// Provide PublishApi as Webservice
 			try {
 				soapEndpoint = Endpoint.publish(Constants.getProperties().getProperty("platfomservices.querydispatchapi.endpoint"), this); 
 			} catch (Exception e) {
 				logger.error("Exception while publishing QueryDispatch Web Service endpoint", e);
 			}
 	
 			this.init = true;
 		}
 	}
 	
 	@Override
 	public void endComponentActivity(Body arg0) {
 		logger.info("Terminating {} component.", this.getClass().getSimpleName());
 		
 		if (this.soapEndpoint != null) {
 			this.soapEndpoint.stop();
 		}
 		this.init = false;
 	}
 	
 
 	@Override
 	public synchronized String registerQuery(String queryId, String query) {
 		if (!init) {
 			throw new IllegalStateException("Component not initialized: "
 					+ this.getClass().getSimpleName());
 		}
 
 		// Parse query
 		Query q = QueryFactory.create(query, Syntax.syntaxEPSPARQL_20);
 
 		// Generate CEP-language
 		eleGenerator.setPatternId("'" + queryId + "'"); // TODO sobermeier: Remove in the future, ETALIS will do this
 		eleGenerator.generateQuery(q);
 
 		logger.info("Registering query " + q);
 
 		// Add queryDetails
 		QueryDetails qd = this.createQueryDetails(queryId, q);
 
 		EpSparqlQuery epQuery = new EpSparqlQuery(qd, eleGenerator.getEle());
 		
 		//Generate historical query.
 		epQuery.sethistoricalQueries(PlaySerializer.serializeToMultipleSelectQueries(q));
 		
 		epQuery.setConstructTemplate((new QueryTemplateGenerator()).createQueryTemplate(q));
 		
 		try {
 			dcepManagmentApi.registerEventPattern(epQuery);
 		} catch (Exception e) {
 			logger.error("Error while registering query: " + queryId, e);
 		}
 		return queryId;
 	}
 
 
 	@Override
 	public void unregisterQuery(String queryId) {
 		if (!init) {
 			throw new IllegalStateException("Component not initialized: "
 					+ this.getClass().getSimpleName());
 		}
 		
 		logger.info("Unregistering query " + queryId);
 
 		this.dcepManagmentApi.unregisterEventPattern(queryId);
 	}
 
 	@Override
 	public QueryDetails analyseQuery(String queryId, String query) {
 		if (!init) {
 			throw new IllegalStateException("Component not initialized: "
 					+ this.getClass().getSimpleName());
 		}
 		
 		// Parse query
 		Query q = QueryFactory.create(query, com.hp.hpl.jena.query.Syntax.syntaxEPSPARQL_20);
 
 		return createQueryDetails(queryId, q);
 	}
 
 	private QueryDetails createQueryDetails(String queryId, Query query) {
 		if (!init) {
 			throw new IllegalStateException("Component not initialized: "
 					+ this.getClass().getSimpleName());
 		}
 		
 		logger.info("Analysing query with ID " + queryId);
 		
 		QueryDetails qd = new QueryDetails();
 		qd.setQueryId(queryId);
 
 		qd.setWindowTime(query.getWindowTime());
 		
 		if (dcepManagmentApi.getRegisteredEventPatterns().containsKey(queryId)) {
 			throw new IllegalArgumentException("Query ID is alread used: " + queryId);
 		}
 
 		StreamIdCollector streamIdCollector = new StreamIdCollector();
 		streamIdCollector.getStreamIds(query, qd);
 
 		return qd;
 	}
 	
 	@Override
 	public String getRegisteredQuery(String queryId) {
 		if (!init) {
 			throw new IllegalStateException("Component not initialized: "
 					+ this.getClass().getSimpleName());
 		}
 		
 		return this.dcepManagmentApi.getRegisteredEventPattern(queryId).getEpSparqlQuery();
 	}
 
 	@Override
 	public List<eu.play_project.play_platformservices.jaxb.Query> getRegisteredQueries() {
 		if (!init) {
 			throw new IllegalStateException("Component not initialized: "
 					+ this.getClass().getSimpleName());
 		}
 			
 		List<eu.play_project.play_platformservices.jaxb.Query> results = new ArrayList<eu.play_project.play_platformservices.jaxb.Query>();
 		
 		Map<String, EpSparqlQuery> queries = dcepManagmentApi.getRegisteredEventPatterns();
 		
 		for (String queryId : queries.keySet()) {
 			eu.play_project.play_platformservices.jaxb.Query query = new eu.play_project.play_platformservices.jaxb.Query();
 			query.id = queryId;
			query.content = queries.get(queries).getEpSparqlQuery();
 			
 			results.add(query);
 		} 
 		
 		return results;
 	}
 
 }
