 package org.cagrid.sdkquery43.extension.processor;
 
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.cagrid.data.InitializationException;
 import gov.nih.nci.cagrid.data.MalformedQueryException;
 import gov.nih.nci.cagrid.data.QueryProcessingException;
 import gov.nih.nci.cagrid.data.cql2.CQL2QueryProcessor;
 import gov.nih.nci.system.applicationservice.ApplicationService;
 import gov.nih.nci.system.client.ApplicationServiceProvider;
 import gov.nih.nci.system.client.util.xml.XMLUtility;
 import gov.nih.nci.system.client.util.xml.XMLUtilityException;
 import gov.nih.nci.system.query.hibernate.HQLCriteria;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Properties;
 
 import javax.xml.namespace.QName;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.cagrid.cql.utilities.AnyNodeHelper;
 import org.cagrid.cql.utilities.CQL2SerializationUtil;
 import org.cagrid.cql2.Aggregation;
 import org.cagrid.cql2.CQLQuery;
 import org.cagrid.cql2.CQLQueryModifier;
 import org.cagrid.cql2.NamedAttribute;
 import org.cagrid.cql2.results.CQLAggregateResult;
 import org.cagrid.cql2.results.CQLAttributeResult;
 import org.cagrid.cql2.results.CQLObjectResult;
 import org.cagrid.cql2.results.CQLQueryResults;
 import org.cagrid.cql2.results.CQLResult;
 import org.cagrid.cql2.results.TargetAttribute;
 import org.cagrid.iso21090.sdkquery.translator.ConstantValueResolver;
 import org.cagrid.iso21090.sdkquery.translator.IsoDatatypesConstantValueResolver;
 import org.cagrid.iso21090.sdkquery.translator.ParameterizedHqlQuery;
 //import org.cagrid.iso21090.sdkquery.translator.cql2.CQL2ToParameterizedHQL;
 import org.cagrid.iso21090.sdkquery.translator.cql2.Cql2TypesInformationResolver;
 import org.cagrid.iso21090.sdkquery.translator.cql2.HibernateConfigCql2TypesInformationResolver;
 import org.exolab.castor.types.AnyNode;
 import org.globus.wsrf.security.SecurityManager;
 import org.hibernate.cfg.Configuration;
 
 import org.cagrid.sdkquery43.extension.translator.ExtendedCQL2ToParameterizedHQL;
 
 /**
  * CustomQueryProcessor
  * CQL 2 Query processor for caCORE SDK 4.3 with ISO 21090 data types 
  * Added Extensions 
  * 
  * @author David W. Ervin
  */
 public class CustomQueryProcessor extends CQL2QueryProcessor {
     
     private static Log LOG = LogFactory.getLog(CustomQueryProcessor.class);
     
     // general configuration options
     public static final String PROPERTY_APPLICATION_NAME = "applicationName";
     public static final String PROPERTY_USE_LOCAL_API = "useLocalApiFlag";
     
     // remote service configuration properties
     public static final String PROPERTY_HOST_NAME = "applicationHostName";
     public static final String PROPERTY_HOST_PORT = "applicationHostPort";
     public static final String PROPERTY_HOST_HTTPS = "useHttpsUrl";
     
     // security configuration properties
     public static final String PROPERTY_USE_GRID_IDENTITY_LOGIN = "useGridIdentityLogin";
     public static final String PROPERTY_USE_STATIC_LOGIN = "useStaticLogin";
     public static final String PROPERTY_STATIC_LOGIN_USER = "staticLoginUser";
     public static final String PROPERTY_STATIC_LOGIN_PASS = "staticLoginPass";
     
     // default values for properties
     public static final String DEFAULT_USE_LOCAL_API = String.valueOf(false);
     public static final String DEFAULT_HOST_HTTPS = String.valueOf(false);
     public static final String DEFAULT_USE_GRID_IDENTITY_LOGIN = String.valueOf(false);
     public static final String DEFAULT_USE_STATIC_LOGIN = String.valueOf(false);
     
     
     private ExtendedCQL2ToParameterizedHQL cqlTranslator = null;
     private QNameResolver qnameResolver = null;
     private byte[] wsddBytes = null;
     
     public CustomQueryProcessor() {
         super();
     }
     
     
     public Properties getRequiredParameters() {
         Properties props = super.getRequiredParameters();
         props.setProperty(PROPERTY_APPLICATION_NAME, "");
         props.setProperty(PROPERTY_USE_LOCAL_API, DEFAULT_USE_LOCAL_API);
         props.setProperty(PROPERTY_HOST_NAME, "");
         props.setProperty(PROPERTY_HOST_PORT, "");
         props.setProperty(PROPERTY_HOST_HTTPS, DEFAULT_HOST_HTTPS);
         props.setProperty(PROPERTY_USE_GRID_IDENTITY_LOGIN, DEFAULT_USE_GRID_IDENTITY_LOGIN);
         props.setProperty(PROPERTY_USE_STATIC_LOGIN, DEFAULT_USE_STATIC_LOGIN);
         props.setProperty(PROPERTY_STATIC_LOGIN_USER, "");
         props.setProperty(PROPERTY_STATIC_LOGIN_PASS, "");
         return props;
     }
     
     
     public void initialize() throws InitializationException {
         // verify that if we're using grid identity login, we're also using the Local API
         if (isUseGridIdentLogin() && !isUseLocalApi()) {
             throw new InitializationException("Grid identity + CSM authentication can only be used with the local API!");
         }
         // verify we have a URL for the remote API
         if (!isUseLocalApi()) {
             try {
                 new URL(getRemoteApplicationUrl());
             } catch (MalformedURLException ex) {
                 throw new InitializationException("Could not determine a remote API url: " + ex.getMessage(), ex);
             }
         }
     }
     
 
     public CQLQueryResults processQuery(CQLQuery cqlQuery) throws MalformedQueryException, QueryProcessingException {
         LOG.debug("Starting process query");
         Iterator<CQLResult> resultIter = processQueryAndIterate(cqlQuery);
         LOG.debug("Converting iterated results to static results");
         CQLQueryResults results = new CQLQueryResults();
         results.setTargetClassname(cqlQuery.getCQLTargetObject().getClassName());
         List<CQLObjectResult> objectResults = new LinkedList<CQLObjectResult>();
         List<CQLAttributeResult> attributeResults = new LinkedList<CQLAttributeResult>();
         CQLAggregateResult aggregateResult = null;
         while (resultIter.hasNext()) {
             CQLResult result = resultIter.next();
             if (result instanceof CQLObjectResult) {
                 objectResults.add((CQLObjectResult) result);
             } else if (result instanceof CQLAttributeResult) {
                 attributeResults.add((CQLAttributeResult) result);
             } else if (result instanceof CQLAggregateResult) {
                 aggregateResult = (CQLAggregateResult) result;
             }
         }
         if (objectResults.size() != 0) {
             results.setObjectResult(objectResults.toArray(new CQLObjectResult[0]));
         } else if (attributeResults.size() != 0) {
             results.setAttributeResult(attributeResults.toArray(new CQLAttributeResult[0]));
         } else {
             results.setAggregationResult(aggregateResult);
         }
         return results;
     }
     
     
     public Iterator<CQLResult> processQueryAndIterate(CQLQuery query) throws QueryProcessingException {
         LOG.debug("Starting process query and iterate results");
         CQLQuery runQuery = query;
         if (runQuery.getCQLQueryModifier() != null && runQuery.getCQLQueryModifier().getNamedAttribute() != null) {
             LOG.debug("Cloning query and editing modifier for unique result tuples");
             // HQL will return distinct tuples of attribute names, so we need to include
             // the id attribute in those tuples to get a 1:1 correspondence with
             // actual data instances in the database
             try {
                 runQuery = CQL2SerializationUtil.cloneQueryBean(query);
                 NamedAttribute[] namedAttributes = runQuery.getCQLQueryModifier().getNamedAttribute();
                 NamedAttribute idAttribute = new NamedAttribute("id");
                 namedAttributes = (NamedAttribute[]) Utils.appendToArray(namedAttributes, idAttribute);
                 runQuery.getCQLQueryModifier().setNamedAttribute(namedAttributes);
             } catch (Exception ex) {
                 String message = "Error pre-processing query modifier attribute names: " + ex.getMessage();
                 LOG.error(message, ex);
                 throw new QueryProcessingException(message, ex);
             }
         }
         // get an instance of the caCORE SDK ApplicationService
         ApplicationService sdkService = getApplicationService();
 
         // empty results object
         CQLQueryResults queryResults = new CQLQueryResults();
         queryResults.setTargetClassname(query.getCQLTargetObject().getClassName());
 
         // convert the CQL to HQL
         LOG.debug("Converting CQL query to HQL");
         ParameterizedHqlQuery hql = null;
         HashMap<String, Object> queryMap;
         try {
         	this.cqlTranslator = getCqlTranslator();
         	queryMap = cqlTranslator.convertToHql(runQuery);
             
            hql  = (ParameterizedHqlQuery) queryMap.get("hql");
             
             LOG.debug("Created HQL: " + hql.toString());
         } catch (QueryProcessingException ex) {
             throw ex;
         } catch (Exception ex) {
             throw new QueryProcessingException("Error processing query: " + ex.getMessage(), ex);
         }
 
         // Process the Limit Extension if it exists
          HQLCriteria criteria = null;
          if (queryMap.containsKey("firstRow")) {
          		Integer firstRow = (Integer) queryMap.get("firstRow");
          		
         		if (queryMap.containsKey("numberOfRows")) {
         			Integer numberOfRows = (Integer) queryMap.get("numberOfRows");
             		criteria = new HQLCriteria(hql.getHql(), hql.getParameters(), firstRow.intValue(), numberOfRows.intValue());
         		} else {
         			criteria = new HQLCriteria(hql.getHql(), hql.getParameters(), firstRow.intValue());
         		}
         	}
         	else {
          		criteria = new HQLCriteria(hql.getHql(), hql.getParameters());
           }
 
         // query the SDK
         LOG.debug("Querying application service");
         List<Object> rawResults = null;
         try {
             rawResults = sdkService.query(criteria);
         } catch (Exception ex) {
             String message = "Error querying caCORE service: " + ex.getMessage();
             LOG.error(message, ex);
             throw new QueryProcessingException(message, ex);
         }
         LOG.debug("Results obtained from application service");
         
         Iterator<CQLResult> cqlResultsIter = null;
         // see if there is further processing to be done
         if (runQuery.getCQLQueryModifier() != null) {
             LOG.debug("Post-processing query modifiers");
             CQLQueryModifier mods = runQuery.getCQLQueryModifier();
             if (mods.getNamedAttribute() != null) {
                 LOG.debug("Detected named attribute results");
                 // trim off the extra id attribute we added earlier
                 String[] attributeNames = new String[mods.getNamedAttribute().length - 1];
                 for (int i = 0; i < attributeNames.length; i++) {
                     attributeNames[i] = mods.getNamedAttribute(i).getAttributeName();
                 }
                 // this will happily ignore the last value which is the extra ID attribute
                 cqlResultsIter = wrapAttributeResult(attributeNames, rawResults);
             } else if (mods.getCountOnly() != null && mods.getCountOnly().booleanValue()) {
                 LOG.debug("Detected count-only aggregate results");
                 Object resultValue = rawResults.size() != 0 ? rawResults.get(0) : null;
                 String valueAsString = attributeValueAsString(resultValue);
                 cqlResultsIter = wrapAggregateResult(Aggregation.COUNT, "id", valueAsString);
             } else if (mods.getDistinctAttribute() != null) {
                 LOG.debug("Detected distinct attribute results");
                 if (mods.getDistinctAttribute().getAggregation() != null) {
                     LOG.debug("Detected aggregate results");
                     Aggregation agg = mods.getDistinctAttribute().getAggregation();
                     Object resultValue = rawResults.size() != 0 ? rawResults.get(0) : null;
                     String valueAsString = attributeValueAsString(resultValue);
                     cqlResultsIter = wrapAggregateResult(agg, mods.getDistinctAttribute().getAttributeName(), valueAsString);
                 } else {
                     // standard attribute name / value pairs
                     cqlResultsIter = wrapAttributeResult(
                         new String[]{mods.getDistinctAttribute().getAttributeName()}, rawResults);
                 }
             } else if (mods.getModifierExtension() != null) {
             	LOG.debug("Detected Modifier Extension");
                 QName targetQName = null;
                 try {
                     targetQName = getQNameResolver().getQName(query.getCQLTargetObject().getClassName());
                 } catch (Exception ex) {
                     throw new QueryProcessingException("Error obtaining QName for target data type: " + ex.getMessage(), ex);
                 }
                 cqlResultsIter = wrapObjectResults(rawResults, targetQName);
             }
         } else {
             LOG.debug("Detected object results");
             QName targetQName = null;
             try {
                 targetQName = getQNameResolver().getQName(query.getCQLTargetObject().getClassName());
             } catch (Exception ex) {
                 throw new QueryProcessingException("Error obtaining QName for target data type: " + ex.getMessage(), ex);
             }
             cqlResultsIter = wrapObjectResults(rawResults, targetQName);
         }
 
         return cqlResultsIter;
     }
     
     
     private Iterator<CQLResult> wrapObjectResults(List<Object> rawObjects, final QName targetQName) {
         final Iterator<Object> rawObjectIter = rawObjects.iterator();
         Iterator<CQLResult> objectIter = new Iterator<CQLResult>() {
             public boolean hasNext() {
                 return rawObjectIter.hasNext();
             }
 
 
             public CQLResult next() {
                 CQLObjectResult obj = new CQLObjectResult();
                 Object rawObject = rawObjectIter.next();
                 LOG.debug("Stripping proxy impl from result instance");
                 try {
                     // TODO: should the association flag be false / true?
                     rawObject = XMLUtility.convertFromProxy(rawObject, false);
                 } catch (XMLUtilityException ex) {
                     LOG.error("Error stripping proxy from result instance: " + ex.getMessage(), ex);
                     throw new NoSuchElementException("Error stripping proxy from result instance: " + ex.getMessage());
                 }
                 StringWriter writer = new StringWriter();
                 try {
                     InputStream wsdd = getDisposableWsdd();
                     Utils.serializeObject(rawObject, targetQName, writer, wsdd);
                 } catch (Exception ex) {
                     String message = "Error pre-serializing object result: " + ex.getMessage();
                     LOG.error(message, ex);
                     NoSuchElementException nse = new NoSuchElementException(message);
                     nse.initCause(ex);
                     throw nse;
                 }
                 AnyNode node = null;
                 try {
                     node = AnyNodeHelper.convertStringToAnyNode(
                         writer.getBuffer().toString());
                 } catch (Exception ex) {
                     String message = "Error creating AnyNode for object results: " + ex.getMessage();
                     LOG.error(message, ex);
                     NoSuchElementException nse = new NoSuchElementException(message);
                     nse.initCause(ex);
                     throw nse;
                 }
                 obj.set_any(node);
                 return obj;
             }
 
 
             public void remove() {
                 throw new UnsupportedOperationException("remove() is not supported");
             }
         };
         return objectIter;
     }
 
 
     private Iterator<CQLResult> wrapAggregateResult(Aggregation agg, String attributeName, String value) {
         final CQLAggregateResult result = new CQLAggregateResult();
         result.setAggregation(agg);
         result.setAttributeName(attributeName);
         result.setValue(value);
         
         Iterator<CQLResult> iter = new Iterator<CQLResult>() {
             boolean hasBeenReturned = false;
             
             public boolean hasNext() {
                 return !hasBeenReturned;
             }
 
             
             public synchronized CQLResult next() {
                 if (!hasNext()) {
                     throw new NoSuchElementException();
                 }
                 hasBeenReturned = true;
                 return result;
             }
 
             
             public void remove() {
                 throw new UnsupportedOperationException("remove() is not supported");
             }
         };
         
         return iter;
     }
 
 
     private Iterator<CQLResult> wrapAttributeResult(final String[] attributeNames, List<Object> attributeValues) {
         final Iterator<Object> rawValueIter = attributeValues.iterator();
         
         Iterator<CQLResult> iter = new Iterator<CQLResult>() {
             public boolean hasNext() {
                 return rawValueIter.hasNext();
             }
 
             
             public synchronized CQLResult next() {
                 Object[] values = arrayify(rawValueIter.next());
                 CQLAttributeResult result = new CQLAttributeResult();
                 TargetAttribute[] ta = new TargetAttribute[attributeNames.length];
                 for (int i = 0; i < attributeNames.length; i++) {
                     Object val = values[i];
                     ta[i] = new TargetAttribute(attributeNames[i], attributeValueAsString(val));
                 }
                 result.setAttribute(ta);
                 return result;
             }
             
 
             public void remove() {
                 throw new UnsupportedOperationException("remove() is not supported");
             }
             
             
             private Object[] arrayify(Object o) {
                 if (o != null && o.getClass().isArray()) {
                     return (Object[]) o;
                 }
                 return new Object[] {o};
             }
         };
         return iter;
     }
     
     
     private String attributeValueAsString(Object val) {
         String valAsString = null;
         if (val != null) {
             if (val instanceof Date) {
                 valAsString = DateFormat.getDateTimeInstance().format((Date) val);
             } else {
                 valAsString = String.valueOf(val);
             }
         }
         return valAsString;
     }
 
     
     
     private ExtendedCQL2ToParameterizedHQL getCqlTranslator() throws Exception {
         if (cqlTranslator == null) {
             LOG.debug("Locating Hibernate configuration");
             InputStream configStream = getClass().getResourceAsStream("/hibernate.cfg.xml");
             Configuration config = new Configuration();
             config.addInputStream(configStream);
             config.configure();
             configStream.close();
             LOG.debug("Initializing types information resolver");
             Cql2TypesInformationResolver typesInfoResolver = new HibernateConfigCql2TypesInformationResolver(config, true);
             LOG.debug("Loading ISO constant resolver");
             ConstantValueResolver constantResolver = new IsoDatatypesConstantValueResolver();
             LOG.debug("Initializing CQL 2 to HQL translator");
             cqlTranslator = new ExtendedCQL2ToParameterizedHQL(typesInfoResolver, constantResolver, false);
         }
         return cqlTranslator;
     }
     
     
     private boolean isUseLocalApi() {
         boolean useLocal = Boolean.parseBoolean(DEFAULT_USE_LOCAL_API);
         String useLocalApiValue = getConfiguredParameters().getProperty(PROPERTY_USE_LOCAL_API);
         try {
             useLocal = Boolean.parseBoolean(useLocalApiValue);
         } catch (Exception ex) {
             LOG.error("Error parsing property " + PROPERTY_USE_LOCAL_API 
                 + ".  Value was " + useLocalApiValue, ex);
         }
         return useLocal;
     }
     
     
     private boolean isUseGridIdentLogin() {
         boolean useGridIdent = Boolean.parseBoolean(DEFAULT_USE_GRID_IDENTITY_LOGIN);
         String useGridIdentValue = getConfiguredParameters().getProperty(PROPERTY_USE_GRID_IDENTITY_LOGIN);
         try {
             useGridIdent = Boolean.parseBoolean(useGridIdentValue);
         } catch (Exception ex) {
             LOG.error("Error parsing property " + PROPERTY_USE_GRID_IDENTITY_LOGIN
                 + ".  Value was " + useGridIdentValue, ex);
         }
         return useGridIdent;
     }
     
     
     private boolean isUseStaticLogin() {
         boolean useStatic = false;
         String useStaticValue = getConfiguredParameters().getProperty(PROPERTY_USE_STATIC_LOGIN);
         try {
             useStatic = Boolean.parseBoolean(useStaticValue);
         } catch (Exception ex) {
             LOG.error("Error parsing property " + PROPERTY_USE_STATIC_LOGIN
                 + ".  Value was " + useStaticValue, ex);
         }
         return useStatic;
     }
     
     
     private boolean isUseHttps() {
         boolean useHttps = Boolean.parseBoolean(DEFAULT_HOST_HTTPS);
         String useHttpsValue = getConfiguredParameters().getProperty(PROPERTY_HOST_HTTPS);
         try {
             useHttps = Boolean.parseBoolean(useHttpsValue);
         } catch (Exception ex) {
             LOG.error("Error parsing property " + PROPERTY_HOST_HTTPS
                 + ".  Value was " + useHttpsValue, ex);
         }
         return useHttps;
     }
     
     
     private String getStaticLoginUser() {
         return getConfiguredParameters().getProperty(PROPERTY_STATIC_LOGIN_USER);
     }
     
     
     private String getStaticLoginPass() {
         return getConfiguredParameters().getProperty(PROPERTY_STATIC_LOGIN_PASS);
     }
     
     
     private String getRemoteApplicationUrl() {
         StringBuffer url = new StringBuffer();
         if (isUseHttps()) {
             url.append("https://");
         } else {
             url.append("http://");
         }
         url.append(getConfiguredParameters().getProperty(PROPERTY_HOST_NAME));
         url.append(":");
         url.append(getConfiguredParameters().getProperty(PROPERTY_HOST_PORT));
         url.append("/");
         url.append(getConfiguredParameters().getProperty(PROPERTY_APPLICATION_NAME));
         String completedUrl = url.toString();
         LOG.debug("Application Service remote URL determined to be: " + completedUrl);
         return completedUrl;
     }
     
     
     private ApplicationService getApplicationService() throws QueryProcessingException {
         LOG.debug("Getting caCORE SDK application service instance");
         ApplicationService service = null;
         try {
             if (isUseLocalApi()) {
                 LOG.debug("Using Local API");
                 if (isUseGridIdentLogin()) {
                     LOG.debug("Using grid identity login");
                     SecurityManager securityManager = SecurityManager.getManager();
                     String username = securityManager.getCaller();
                     LOG.trace("Identity determined to be " + username);
                     service = ApplicationServiceProvider.getApplicationServiceForUser(username);
                 } else {
                     service = ApplicationServiceProvider.getApplicationService();
                 }
             } else {
                 LOG.debug("Using Remote API");
                 String url = getRemoteApplicationUrl();
                 if (isUseStaticLogin()) {
                     LOG.debug("Using static login");
                     String username = getStaticLoginUser();
                     String password = getStaticLoginPass();
                     service = ApplicationServiceProvider.getApplicationServiceFromUrl(url, username, password);
                 } else {
                     service = ApplicationServiceProvider.getApplicationServiceFromUrl(url);
                 }
             }
         } catch (Exception ex) {
             throw new QueryProcessingException("Error obtaining application service: " + ex.getMessage(), ex);
         }
         return service;
     }
     
     
     private QNameResolver getQNameResolver() throws Exception {
         if (qnameResolver == null) {
             qnameResolver = new MappingFileQNameResolver(getClassToQnameMappings());
         }
         return qnameResolver;
     }
     
     
     private InputStream getDisposableWsdd() throws IOException {
         if (wsddBytes == null) {
             wsddBytes = Utils.inputStreamToByteArray(getConfiguredWsddStream());
         }
         return new ByteArrayInputStream(wsddBytes);
     }
 }
