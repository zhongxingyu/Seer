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
 import com.hp.hpl.jena.query.QueryException;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.Syntax;
 import com.hp.hpl.jena.sparql.serializer.PlaySerializer;
 
 import eu.play_platform.platformservices.bdpl.syntax.windows.visitor.ElementWindowVisitor;
 import eu.play_project.dcep.api.DcepManagementException;
 import eu.play_project.dcep.api.DcepManagmentApi;
 import eu.play_project.play_commons.constants.Constants;
 import eu.play_project.play_platformservices.api.BdplQuery;
 import eu.play_project.play_platformservices.api.QueryDetails;
 import eu.play_project.play_platformservices.api.QueryDispatchApi;
 import eu.play_project.play_platformservices.api.QueryDispatchException;
 import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
 import eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime.EleGeneratorForConstructQuery;
 import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.historic.QueryTemplateGenerator;
 import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.ComplexTypeFinder;
 import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.StreamIdCollector;
 import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.WindowVisitor;
 
 /**
  * The PLAY SOAP Web Service to manage event patterns. See
  * {@linkplain PlayPlatformservicesRest} for the corresponding RESTful service.
  * 
  * @author Roland Stühmer
  */
 @WebService(
 		serviceName = "QueryDispatchApi",
 		portName = "QueryDispatchApiPort",
 		endpointInterface = "eu.play_project.play_platformservices.api.QueryDispatchApi")
 public class PlayPlatformservices implements QueryDispatchApi,
 		ComponentInitActive, ComponentEndActive, BindingController,
 		Serializable {
 
 	private static final long serialVersionUID = 100L;
 	private EleGenerator eleGenerator;
 	private DcepManagmentApi dcepManagmentApi;
 	private boolean init = false;
 	private final Logger logger = LoggerFactory.getLogger(PlayPlatformservices.class);
 	private Endpoint soapServer;
 	private PlayPlatformservicesRest restServer;
 
 	@Override
 	public String[] listFc() {
 		return new String[] { DcepManagmentApi.class.getSimpleName() };
 	}
 
 	@Override
 	public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
 		if (DcepManagmentApi.class.getSimpleName().equals(clientItfName)) {
 			return dcepManagmentApi;
 		} else {
 			throw new NoSuchInterfaceException(DcepManagmentApi.class.getSimpleName());
 		}
 	}
 
 	@Override
 	public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
 		if (clientItfName.equals(DcepManagmentApi.class.getSimpleName())) {
 			dcepManagmentApi = (DcepManagmentApi) serverItf;
 		}
 		else {
 			throw new NoSuchInterfaceException(String.format("Interface '%s' not available at '%s'.", clientItfName, this.getClass().getSimpleName()));
 		}
 	}
 
 	@Override
 	public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
 		if (clientItfName.equals(DcepManagmentApi.class.getSimpleName())) {
 			// do nothing, currently
 		}
 		else {
 			throw new NoSuchInterfaceException(String.format("Interface '%s' not available at '%s'.", clientItfName, this.getClass().getSimpleName()));
 		}
 	}
 
 	@Override
 	public synchronized void initComponentActivity(Body body) {
 		if (!init) {
 			
 			logger.info("Initialising {} component.", this.getClass().getSimpleName());
 	
 			eleGenerator = new EleGeneratorForConstructQuery();
 	
 			/*
 			 * Provide QueryDispatchApi as SOAP Webservice
 			 */
 			try {
 				String address = Constants.getProperties().getProperty("platfomservices.querydispatchapi.endpoint");
 				soapServer = Endpoint.publish(address, this);
 				logger.info("QueryDispatch SOAP service started at {}.", address);
 			} catch (Exception e) {
 				logger.error("Exception while publishing QueryDispatch SOAP Service", e);
 			}
 	
 			/*
 			 * Provide QueryDispatchApi as REST Webservice
 			 */
 			try {
 				restServer = new PlayPlatformservicesRest(this);
 	        	logger.info(String.format("QueryDispatch REST service started at %s with WADL remotely available at "
 	        			+ "%s/application.wadl\n", PlayPlatformservicesRest.BASE_URI, Constants.getProperties().getProperty("platfomservices.querydispatchapi.rest")));
 			} catch (Exception e) {
 				logger.error("Exception while publishing QueryDispatch REST Service", e);
 			}
 			
 			this.init = true;
 		}
 	}
 	
 	@Override
 	public synchronized void endComponentActivity(Body arg0) {
 		logger.info("Terminating {} component.", this.getClass().getSimpleName());
 		this.init = false;
 		
 		if (this.soapServer != null) {
 			this.soapServer.stop();
 		}
 		
 		if (this.restServer != null) {
 			this.restServer.destroy();
 		}
 	}
 	
 
 	@Override
 	public synchronized String registerQuery(String queryId, String query) throws QueryDispatchException {
 		if (!init) {
 			throw new IllegalStateException("Component not initialized: "
 					+ this.getClass().getSimpleName());
 		}
 
 		logger.info("Registering query with ID '{}'", queryId);
 
 		BdplQuery epQuery = createCepQuery(queryId, query);
 		
 		try {
 			dcepManagmentApi.registerEventPattern(epQuery);
 		} catch (Exception e) {
 			String msg = String.format("Error while registering query '%s': %s: %s", queryId, e.getClass().getSimpleName(), e.getMessage());
 			logger.error(msg);
 			throw new QueryDispatchException(msg);
 		}
 		return queryId;
 	}
 
 	private BdplQuery createCepQuery(String queryId, String query)
 			throws QueryDispatchException {
 		// Parse query
 		Query q;
 		try {
 			q = QueryFactory.create(query, Syntax.syntaxBDPL);
 		} catch (com.hp.hpl.jena.query.QueryException e) {
 			throw new QueryDispatchException(e.getMessage());
 		}
 
 		// Generate CEP-language
 		eleGenerator.setPatternId(queryId);
 		eleGenerator.generateQuery(q);
 
 		// Add queryDetails
 		QueryDetails qd = this.createQueryDetails(queryId, q);
 		qd.setRdfDbQueries(eleGenerator.getRdfDbQueries());
 		
 		BdplQuery bdpl = BdplQuery.builder()
 				.details(qd)
 				.ele(eleGenerator.getEle())
 				.historicalQueries(PlaySerializer.serializeToMultipleSelectQueries(q))
 				.constructTemplate(new QueryTemplateGenerator().createQueryTemplate(q))
 				.bdpl(query)
 				.build();
 
 		return bdpl;
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
 	public QueryDetails analyseQuery(String queryId, String query) throws QueryDispatchException {
 		if (!init) {
 			throw new IllegalStateException("Component not initialized: "
 					+ this.getClass().getSimpleName());
 		}
 		
 		// Parse query
 		try {
 			Query q = QueryFactory.create(query, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
 			return createQueryDetails(queryId, q);
 		}
 		catch (QueryException e) {
 			throw new QueryDispatchException(e.getMessage());
 		}
 		
 	}
 
 	private QueryDetails createQueryDetails(String queryId, Query query) throws QueryDispatchException {
 		if (!init) {
 			throw new IllegalStateException("Component not initialized: " + this.getClass().getSimpleName());
 		}
 		
 		logger.info("Analysing query with ID " + queryId);
 		
 		QueryDetails qd = new QueryDetails(queryId);
 
 		// Set properties for windows in QueryDetails
 		ElementWindowVisitor windowVisitor = new WindowVisitor(qd);
 		query.getWindow().accept(windowVisitor);
 		
 		// Check if id is alredy used.
 		if (dcepManagmentApi != null && dcepManagmentApi.getRegisteredEventPatterns().containsKey(queryId)) {
			throw new QueryDispatchException("Query ID is already used: " + queryId);
 		}
 
 		// Set stream ids in QueryDetails.
 		StreamIdCollector streamIdCollector = new StreamIdCollector();
 		streamIdCollector.getStreamIds(query, qd);
 		
 		// Set complex event type.
 		qd.setComplexType((new ComplexTypeFinder()).visit(query.getConstructTemplate()));
 
 		return qd;
 	}
 	
 	@Override
 	public eu.play_project.play_platformservices.jaxb.Query getRegisteredQuery(String queryId)
 			throws QueryDispatchException {
 		if (!init) {
 			throw new IllegalStateException("Component not initialized: "
 					+ this.getClass().getSimpleName());
 		}
 		try {
 			return new eu.play_project.play_platformservices.jaxb.Query(
 					this.dcepManagmentApi.getRegisteredEventPattern(queryId));
 		} catch (DcepManagementException e) {
 			throw new QueryDispatchException(e.getMessage());
 		}
 	}
 
 	@Override
 	public List<eu.play_project.play_platformservices.jaxb.Query> getRegisteredQueries() {
 		if (!init) {
 			throw new IllegalStateException("Component not initialized: "
 					+ this.getClass().getSimpleName());
 		}
 
 		List<eu.play_project.play_platformservices.jaxb.Query> results = new ArrayList<eu.play_project.play_platformservices.jaxb.Query>();
 
 		Map<String, BdplQuery> queries = dcepManagmentApi
 				.getRegisteredEventPatterns();
 
 		for (String queryId : queries.keySet()) {
 			results.add(new eu.play_project.play_platformservices.jaxb.Query(queries.get(queryId)));
 		}
 
 		return results;
 	}
 
 }
