 package gov.nih.nci.gss.api;
 
 import gov.nih.nci.gss.domain.DataService;
 import gov.nih.nci.gss.domain.DataServiceGroup;
 import gov.nih.nci.gss.domain.DomainClass;
 import gov.nih.nci.gss.domain.DomainModel;
 import gov.nih.nci.gss.domain.GridService;
 import gov.nih.nci.gss.domain.HostingCenter;
 import gov.nih.nci.gss.domain.PointOfContact;
 import gov.nih.nci.gss.domain.SearchExemplar;
 import gov.nih.nci.gss.util.Cab2bTranslator;
 import gov.nih.nci.gss.util.Constants;
 import gov.nih.nci.gss.util.GridServiceDAO;
 import gov.nih.nci.gss.util.StringUtil;
 import gov.nih.nci.system.applicationservice.ApplicationException;
 import gov.nih.nci.system.dao.orm.ORMDAOImpl;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.hibernate.SessionFactory;
 import org.hibernate.classic.Session;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.springframework.util.FileCopyUtils;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 /**
  * Custom JSON API for returning service metadata in bulk and querying services
  * via caB2B. 
  * 
  * @author <a href="mailto:rokickik@mail.nih.gov">Konrad Rokicki</a>
  */
 public class JSONDataService extends HttpServlet {
 
     private static Logger log = Logger.getLogger(JSONDataService.class);
     
     private enum Verb {
     	GET,
     	POST
     };
     
     private enum QueryStatus {
     	RUNNING,
     	EXPIRED,
     	UNKNOWN,
     	FOUND
     };
     
     /** Date format for serializing dates into JSON. 
      * Must match the data format used by the iPhone client */
     public static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
 
     /** JSON string describing the usage of this service */
     private String usage;
     
     /** Hibernate session factory */
     private SessionFactory sessionFactory;
 
     /** Service that manages background queries and results */
     private QueryService queryService;
     
     /** Translates caB2B names to GSS names */
     private Cab2bTranslator cab2bTranslator;
 
     /** Cache for JSON responses */
     private Map cache;
     
     
     @Override
     public void init() throws ServletException {
         
         try {
             WebApplicationContext ctx =  
                 WebApplicationContextUtils.getWebApplicationContext(getServletContext());
             this.sessionFactory = ((ORMDAOImpl)ctx.getBean("ORMDAO")).getHibernateTemplate().getSessionFactory();
             this.cab2bTranslator = new Cab2bTranslator(sessionFactory);
             this.queryService = new QueryService(cab2bTranslator);
             this.cache = (Map)ctx.getBean("memoryCache");
             this.usage = FileCopyUtils.copyToString(new InputStreamReader(
                 JSONDataService.class.getResourceAsStream("/rest_api_usage.js")));
             
         }
         catch (Exception e) {
             throw new ServletException(e);
         }
     }
 
     @Override
     public void doGet(HttpServletRequest request, HttpServletResponse response) 
             throws ServletException, IOException {
     	doREST(Verb.GET, request, response);
     }
     
     @Override
     public void doPost(HttpServletRequest request, HttpServletResponse response) 
             throws ServletException, IOException {
     	doREST(Verb.POST, request, response);
     }
     
     @Override
     public void destroy() {
         queryService.close();
         super.destroy();
     }
 
     /**
      * 
      * @param verb
      * @param request
      * @param response
      * @throws ServletException
      * @throws IOException
      */
     private void doREST(Verb verb, HttpServletRequest request, 
     		HttpServletResponse response) throws ServletException, IOException {
 
         PrintWriter pw = new PrintWriter(response.getOutputStream());
 		try {
 			response.setContentType("application/json");
 
 			String path = request.getPathInfo();
 			if (path == null) {
 				pw.print(getJSONUsage());
 			}
 			else {
 			    String[] pathList = path.split("/");
 
 			    if (pathList.length < 2) {
 			    	pw.print(getJSONUsage());
 			    }
 			    else {
 			        String noun = pathList[1];                
 			        pw.print(getRESTResponse(verb, noun, pathList, request));
 			    }
 			}
 		} 
 		catch (Exception e) {
             log.error("JSON service error",e);
             pw.print(getJSONError(e.getClass().getName(), e.getMessage()));
 		}
 		finally {
 			pw.close();
 		}
     }
     
     /**
      * Process a REST request for a given noun.
      * @param noun
      * @param pathList
      * @param request
      * @return
      */
     private String getRESTResponse(Verb verb, String noun, String[] pathList, 
             HttpServletRequest request) throws Exception {
 
     	// User can specify a delay for debugging purposes.
         String delay = request.getParameter("delay");
         if (delay != null && !"".equals(delay)) {
         	int sleepTime = Integer.parseInt(delay);
         	if (sleepTime < 20000) { // 30 seconds at most
         		Thread.sleep(sleepTime);
         	}
         }
         
         if ("summary".equals(noun)) {
             // Return a summary of data types
             return getSummaryJSON();
         }
         else if ("counts".equals(noun)) {
             // Return a summary of data types
             boolean aggregate = "1".equals(request.getParameter("aggregate"));
             return getCountsJSON(aggregate);
         }
         else if ("service".equals(noun)) {
             // Return details about services, or a single service
             
             String id = null;
             if (pathList.length > 2) {
                 id = pathList[2];
             }
             
             return getServiceJSON(id);
         }
         else if ("host".equals(noun)) {
             // Return details about hosts, or a single hosts
             
             String id = null;
             if (pathList.length > 2) {
                 id = pathList[2];
             }
 
             boolean includeKey = "1".equals(request.getParameter("key"));
             return getHostJSON(id, includeKey);
         }
         else if ("runQuery".equals(noun)) { 
             // Query grid services using caB2B 
 
             String clientId = request.getParameter("clientId");
             String searchString = request.getParameter("searchString");
             String serviceGroup = request.getParameter("serviceGroup");
             String serviceIdConcat = request.getParameter("serviceIds");
             String[] serviceIds = request.getParameterValues("serviceId");
             String[] serviceUrls = request.getParameterValues("serviceUrl");
 
             if (StringUtil.isEmpty(clientId)) {
                 return getJSONError("UsageError", "Specify your clientId.");
             }
 
             if (StringUtil.isEmpty(searchString)) {
                 return getJSONError("UsageError", "Specify a searchString to search on.");
             }
              
             List<String> serviceUrlList = new ArrayList<String>();
             if (serviceUrls != null) {
                 for(String serviceUrl : serviceUrls) {
                     serviceUrlList.add(serviceUrl);
                 }
             }
             
             if (serviceIds == null && serviceIdConcat != null) {
                 serviceIds = serviceIdConcat.split(",");
             }
             
             if (serviceIds != null && serviceIds.length > 0) {
                 Session s = sessionFactory.openSession();
                 
                 for(String serviceId : serviceIds) {
                     
                     List<GridService> services = GridServiceDAO.getServices(serviceId, s);
                     
                     if (services.isEmpty()) {
                         return getJSONError("UsageError", "Unknown service id: "+serviceId);
                     }
                     if (services.size() > 1) {
                         log.error("More than one matching service for service id: "+serviceId);
                     }
                     
                     serviceUrlList.add(services.get(0).getUrl());
                 }
                 
                 s.close();
             }
             
             QueryParams queryParams = null; 
 
             if (!serviceUrlList.isEmpty()) {
                 queryParams = new QueryParams(clientId, searchString, 
                     null, serviceUrlList);
             }
             else if (!StringUtil.isEmpty(serviceGroup)) {
                 if (serviceGroup == null) {
                     return getJSONError("UsageError", "Unrecognized serviceGroup '"+serviceGroup+"'");
                 }
                 
                 queryParams = new QueryParams(clientId, searchString, serviceGroup, null);
             }
             else {
                 return getJSONError("UsageError", "Specify serviceGroup or serviceId or serviceUrl.");
             }
 
             Cab2bQuery query = queryService.executeQuery(queryParams);
 
             log.info("Executing "+queryParams+" as job " +query.getJobId());
             
             JSONObject jsonObj = new JSONObject();
             jsonObj.put("status", QueryStatus.RUNNING);
             jsonObj.put("job_id", query.getJobId());
             return jsonObj.toString();
     	}
         else if ("query".equals(noun)) {
 
             String clientId = request.getParameter("clientId");
             String jobId = request.getParameter("jobId");
             boolean collapse = "1".equals(request.getParameter("collapse"));
 
             if (StringUtil.isEmpty(clientId)) {
                 return getJSONError("UsageError", "Specify your clientId.");
             }
 
             if (StringUtil.isEmpty(jobId)) {
                 return getJSONError("UsageError", "Specify a jobId.");
             }
             
             Cab2bQuery query = queryService.retrieveQuery(jobId);
             
             if (query == null) {
                 return getJSONStatus(QueryStatus.UNKNOWN);
             }
             
             if (!query.getQueryParams().getClientId().equals(clientId)) {
                 return getJSONStatus(QueryStatus.UNKNOWN);
             }
             
             log.info("Got query "+jobId+": "+query.getQueryParams());
             
             // What if the query isn't completed?
             if (!query.isDone()) {
                 
                 // Return nothing and let the client poll
                 if ("1".equals(request.getParameter("async"))) {
                     log.info("Returning asyncronously for query: "+jobId);
                     return getJSONStatus(QueryStatus.RUNNING);	 
                 }
                 
                 // Block until the query is completed
                 synchronized (query) {  
                     try {
                         log.info("Blocking until query is complete: "+jobId);
                         query.wait();
                     }
                     catch (InterruptedException e) {
                         log.error("Interrupted wait",e);
                     }
                 }
             }
 
             return getQueryResultsJSON(query,collapse);
     	}
         
         // If the noun was good we would've returned by now
         return getJSONError("UsageError", "Unrecognized noun '"+noun+"'");
     }
 
 	/**
      * Returns a JSON object representing the basic attributes of a service.
      * @param service
      * @return
      * @throws JSONException
      */
     private JSONObject getJSONObjectForService(GridService service)
             throws JSONException {
 
         HostingCenter host = service.getHostingCenter();
         
         JSONObject jsonService = new JSONObject();
         jsonService.put("id", service.getIdentifier());
         jsonService.put("name", service.getName());
         jsonService.put("version", service.getVersion());
         jsonService.put("class", service.getClass().getSimpleName());
         jsonService.put("simple_name", service.getSimpleName());
         jsonService.put("url", service.getUrl());
         jsonService.put("publish_date", df.format(service.getPublishDate()));
 
         if (service.getHiddenDefault()) {
             jsonService.put("hidden_default", "true");
         }
         
         if (host != null) {
             jsonService.put("host_id", host.getIdentifier());
             jsonService.put("host_short_name", host.getShortName());
         }
         
         if (service instanceof DataService) {
             DataService dataService = (DataService)service;
             DataServiceGroup group = dataService.getGroup();
             if (group != null) jsonService.put("group", group.getName());
             if (dataService.getSearchDefault()) {
                 jsonService.put("search_default", "true");
             }
             if (dataService.getAccessible() != null) {
                 jsonService.put("accessible", dataService.getAccessible().toString());
             }
         }
         
         return jsonService;
     }
 
 	/**
      * Returns a JSON object representing the basic attributes of a service.
      * @param service
      * @return
      * @throws JSONException
      */
     private JSONObject getJSONObjectForHost(HostingCenter host, boolean includeKey)
             throws JSONException {
 
         JSONObject hostObj = new JSONObject();
                         
         // service host details
         
         hostObj.put("id", host.getIdentifier());
         hostObj.put("short_name", host.getShortName());
         hostObj.put("long_name", host.getLongName());
         hostObj.put("country_code", host.getCountryCode());
         hostObj.put("state_province", host.getStateProvince());
         hostObj.put("locality", host.getLocality());
         hostObj.put("postal_code", host.getPostalCode());
         hostObj.put("street", host.getStreet());
 
         if (host.getHiddenDefault()) {
             hostObj.put("hidden_default", "true");
         }
         
         // check if the host has a custom image
         
         String imageName = ImageService.getHostImageName(host);
         String filePath = ImageService.getHostImageFilePath(imageName);
         File file = new File(filePath);
         
         // TODO: optimize this so that the disk is not accessed each time
         if (file.exists()) {
             hostObj.put("image_name", imageName);
         }
 
         if (includeKey) {
         	hostObj.put("assigned_image_name", imageName);
         }
         
         // service host pocs
 
         JSONArray jsonPocs = new JSONArray();
         for (PointOfContact poc : host.getPointOfContacts()) {
             JSONObject jsonPoc = new JSONObject();
             jsonPoc.put("name", poc.getName());
             jsonPoc.put("role", poc.getRole());
             jsonPoc.put("affiliation", poc.getAffiliation());
             jsonPoc.put("email", poc.getEmail());
             jsonPocs.put(jsonPoc);
         }
         hostObj.put("pocs", jsonPocs);
         
         return hostObj;
     }
     
     
     /**
      * Returns a JSON string with data type summary data.
      * @return JSON-formatted String
      * @throws JSONException
      * @throws ApplicationException
      */
     private String getSummaryJSON() throws JSONException, ApplicationException {
 
         if (cache.containsKey(Constants.SUMMARY_CACHE_KEY)) {
             log.info("Returning cached summary JSON");
             return cache.get(Constants.SUMMARY_CACHE_KEY).toString();
         }
         
         JSONObject json = new JSONObject();
 
         JSONArray groupsArray = new JSONArray();
         
         for(DataServiceGroup group : cab2bTranslator.getServiceGroups()) {
         
             JSONObject groupObj = new JSONObject();
             groupObj.put("name",group.getName());
             groupObj.put("label",group.getCab2bName());
             groupObj.put("primaryKeyAttr",group.getDataPrimaryKey());
             groupObj.put("hostAttr",group.getHostPrimaryKey());
             groupObj.put("titleAttr",group.getDataTitle());
             groupObj.put("descriptionAttr",group.getDataDescription());
             
             JSONArray exemplarsArray = new JSONArray();
             
             List<SearchExemplar> exemplars = new ArrayList(
                 group.getExemplarCollection());
             
             // order exemplars by id so that they can be prioritized in the database
             Collections.sort(exemplars, new Comparator<SearchExemplar>() {
                 public int compare(SearchExemplar o1, SearchExemplar o2) {
                     return o1.getId().compareTo(o2.getId());
                 }
             });
             
             for(SearchExemplar se : exemplars) {
                 exemplarsArray.put(se.getSearchString());
             }
             
             groupObj.put("exemplars",exemplarsArray);
             groupsArray.put(groupObj);
         }
         json.put("groups", groupsArray);
         
         String jsonStr = json.toString();
         cache.put(Constants.SUMMARY_CACHE_KEY, jsonStr);
         return jsonStr;
     
     }
 
     /**
      * Returns a JSON string with class count data.
      * @return JSON-formatted String
      * @throws JSONException
      * @throws ApplicationException
      */
     private String getCountsJSON(boolean aggregate) throws JSONException, ApplicationException {
 
         if (aggregate) {
             if (cache.containsKey(Constants.COUNTS_AGGR_CACHE_KEY)) {
                 log.info("Returning cached aggregate counts JSON");
                 return cache.get(Constants.COUNTS_AGGR_CACHE_KEY).toString();
             }
     
             Session s = sessionFactory.openSession();
             
             JSONObject json = new JSONObject();
             JSONObject classesObj = new JSONObject();
     
             try {
                 Map<String,Long> counts = GridServiceDAO.getAggregateClassCounts(s);
                 for(String fullClass : counts.keySet()) {
                     Long count = counts.get(fullClass);
                     if ((count != null) && (count>0)) {
                         classesObj.put(fullClass,count.toString());
                     }
                 }
                 json.put("counts", classesObj);
             }
             finally {
                 s.close();
             }
             
             String jsonStr = json.toString();
             cache.put(Constants.COUNTS_AGGR_CACHE_KEY, jsonStr);
             return jsonStr;
         }
         else {
             if (cache.containsKey(Constants.COUNTS_CACHE_KEY)) {
                 log.info("Returning cached counts JSON");
                 return cache.get(Constants.COUNTS_CACHE_KEY).toString();
             }
     
             Session s = sessionFactory.openSession();
             
             JSONObject json = new JSONObject();
             JSONObject classesObj = new JSONObject();
     
             try {
                 Map<String,Map<String,Long>> counts = GridServiceDAO.getClassCounts(s);
                 for(String fullClass : counts.keySet()) {
                     JSONObject servicesObj = new JSONObject();
                     Map<String,Long> classCounts = counts.get(fullClass);
                     for(String serviceUrl : classCounts.keySet()) {
                         Long count = classCounts.get(serviceUrl);
                         if ((count != null) && (count>0)) {
                             servicesObj.put(serviceUrl,count.toString());
                         }
                     }
                     if (servicesObj.length() > 0) {
                         classesObj.put(fullClass, servicesObj);
                     }
                 }
                 if (classesObj.length() > 0) {
                     json.put("counts", classesObj);
                 }
             }
             finally {
                 s.close();
             }
             
             String jsonStr = json.toString();
             cache.put(Constants.COUNTS_CACHE_KEY, jsonStr);
             return jsonStr;
         }
     
     }
     
     /**
      * Returns a JSON string with all the metadata about a particular service.
      * @return JSON-formatted String
      * @throws JSONException
      * @throws ApplicationException
      */
     private String getServiceJSON(String serviceId) 
             throws JSONException, ApplicationException {
 
     	if (serviceId == null) {
     		if (cache.containsKey(Constants.SERVICE_CACHE_KEY)) {
     			log.info("Returning cached service JSON");
     			return cache.get(Constants.SERVICE_CACHE_KEY).toString();
     		}
     	}
     	
         Session s = sessionFactory.openSession();
         JSONObject json = new JSONObject();
         
         try {
             // Retrieve the list of services
             Collection<GridService> services = GridServiceDAO.getServices(serviceId, s);
             
             JSONArray jsonArray = new JSONArray();
             json.put("services", jsonArray);
             
             for (GridService service : services) {
                 
                 JSONObject jsonService = getJSONObjectForService(service);
                 jsonArray.put(jsonService);
                 
                 // Add service details
                 jsonService.put("description", service.getDescription());
                 jsonService.put("status", service.getLastStatus());
                 
                 // Add service pocs
                 JSONArray jsonPocs = new JSONArray();
                 for (PointOfContact poc : service.getPointOfContacts()) {
                     JSONObject jsonPoc = new JSONObject();
                     jsonPoc.put("name", poc.getName());
                     jsonPoc.put("role", poc.getRole());
                     jsonPoc.put("affiliation", poc.getAffiliation());
                     jsonPoc.put("email", poc.getEmail());
                     jsonPocs.put(jsonPoc);
                 }
                 jsonService.put("pocs", jsonPocs);
                 
                 if (serviceId != null) {
                 	// output more metadata if a service is specified
                 	
                     if (service instanceof DataService) {
                         DomainModel model = ((DataService)service).getDomainModel();
                         
                         // Add domain model
                         JSONObject modelObj = new JSONObject();
                         jsonService.put("domain_model", modelObj);
                         
                         if (model != null) {
                             
                             modelObj.put("long_name", model.getLongName());
                             modelObj.put("version", model.getVersion());
                             modelObj.put("description", model.getDescription());
                             
                             // Add model classes
                             JSONArray jsonClasses = new JSONArray();
                             for (DomainClass dc : model.getClasses()) {
                                 JSONObject jsonClass = new JSONObject();
                                 jsonClass.put("name", dc.getClassName());
                                 jsonClass.put("package", dc.getDomainPackage());
                                 jsonClass.put("description", dc.getDescription());
                                 jsonClasses.put(jsonClass);
                             }
                             modelObj.put("classes", jsonClasses);
                         }
                     }
                 }
             }
 
         }
         finally {
             s.close();
         }
         
         String jsonStr = json.toString();
     	if (serviceId == null) {
     		cache.put(Constants.SERVICE_CACHE_KEY, jsonStr);
     	}
         return jsonStr;
     }
 
     /**
      * Returns a JSON string with all the metadata about a particular host.
      * @return JSON-formatted String
      * @throws JSONException
      * @throws ApplicationException
      */
     private String getHostJSON(String hostId, boolean includeKey) 
             throws JSONException, ApplicationException {
 
     	if (hostId == null) {
     		if (cache.containsKey(Constants.HOST_CACHE_KEY)) {
     			log.info("Returning cached host JSON");
     			return cache.get(Constants.HOST_CACHE_KEY).toString();
     		}
     	}
     	
         Session s = sessionFactory.openSession();
         JSONObject json = new JSONObject();
         
         try {
         	// Get the list of hosts
             Collection<HostingCenter> hosts = GridServiceDAO.getHosts(hostId, s);
             
             JSONArray jsonArray = new JSONArray();
             json.put("hosts", jsonArray);
             
             for (HostingCenter host : hosts) {
                 jsonArray.put(getJSONObjectForHost(host, includeKey));
             }
         }
         finally {
             s.close();
         }
 
         String jsonStr = json.toString();
     	if (hostId == null) {
     		cache.put(Constants.HOST_CACHE_KEY, jsonStr);
     	}
         return jsonStr;
     }
     
     /**
      * Returns a JSON string with the results (or error) produced by the given
      * query. The query should have already finished executing. If not, an
      * IllegalStateException is thrown (not as JSON!).
      * @param query
      * @return
      */
     private String getQueryResultsJSON(Cab2bQuery query, boolean collapse) 
     		throws JSONException {
         
         if (!query.isDone()) {
             throw new IllegalStateException("Query has not completed: "+query);
         }
             
         Exception e = query.getException();
         if (e != null) {
            log.error("Error in caB2B",e);
             return getJSONError(e.getClass().getName(), e.getMessage());
         }
 
     	JSONObject json = new JSONObject(query.getResultJson());
 
         if (json.has("error")) {
             return query.getResultJson();
         }
 
     	String modelGroupName = json.getString("modelGroupName");
         DataServiceGroup serviceGroup = cab2bTranslator.getServiceGroupForModelGroup(modelGroupName);
     	if (modelGroupName != null) {
             json.remove("modelGroupName");
     		json.put("serviceGroup", serviceGroup.getName());
     	}
     	
         if (collapse) {
         	Map<String,Map<String,JSONObject>> servers = 
         	    new LinkedHashMap<String,Map<String,JSONObject>>();
         	
         	// the key to discriminate on for duplicates
         	String primaryKey = serviceGroup.getDataPrimaryKey();
             String hostKey = serviceGroup.getHostPrimaryKey();
         	
         	JSONObject queries = json.getJSONObject("results");
         	for(Iterator i = queries.keys(); i.hasNext(); ) {
         		String queryName = (String)i.next();
         		JSONObject urls = queries.getJSONObject(queryName);
         		for(Iterator j = urls.keys(); j.hasNext(); ) {
             		String url = (String)j.next();
             		JSONArray objs = urls.getJSONArray(url);
 
                     Map<String,JSONObject> serverUnique = servers.get(url);
                         
             		if (serverUnique == null) {
             		    serverUnique = new LinkedHashMap<String,JSONObject>();
             		    servers.put(url, serverUnique);
             		}
 
             		for(int k=0; k<objs.length(); k++) {
             			JSONObject obj = objs.getJSONObject(k);
             			String key = null;
             			try {
             			    key = obj.getString(hostKey)+"~"+obj.getString(primaryKey);
             			}
             			catch (JSONException x) {
             				log.error("Error getting unique key",x);
             				key = obj.toString();
             			}
             			if (!serverUnique.containsKey(key)) {
             			    serverUnique.put(key, obj);
             			}
             		}
         		}
         	}
         	
         	JSONObject jsonUrls = new JSONObject();
         	for(String url : servers.keySet()) {
         	    JSONArray jsonObjs = new JSONArray();
         	    jsonUrls.put(url, jsonObjs);
         	    for (JSONObject obj : servers.get(url).values()) {
         	        jsonObjs.put(obj);
         	    }
         	}
         	json.put("results", jsonUrls);
         	
         }
         
         return json.toString();
     }
     
     /**
      * Returns a JSON string with the given error message.
      * @return JSON-formatted String
      */
     private String getJSONError(String exception, String message) {
         return "{\"error\":"+JSONObject.quote(exception)+
                 ",\"message\":"+JSONObject.quote(message)+"}";
     }
     
     /**
      * Get a JSON string with a simple query status message.
      * @param status
      * @return
      * @throws JSONException
      */
     private String getJSONStatus(Object status) throws JSONException {
         JSONObject jsonObj = new JSONObject();
         jsonObj.put("status", status);
         return jsonObj.toString();
     }
     
     /**
      * Returns a JSON string with usage instructions, or an error if a problem
      * occurs.
      * @return JSON-formatted String
      */
     private String getJSONUsage() {
         return usage;
     }
 }
