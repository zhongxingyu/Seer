 
 package cz.cuni.mff.odcleanstore.engine.outputws;
 
 import cz.cuni.mff.odcleanstore.configuration.ConfigLoader;
 import cz.cuni.mff.odcleanstore.configuration.OutputWSConfig;
 import cz.cuni.mff.odcleanstore.conflictresolution.AggregationSpec;
 import cz.cuni.mff.odcleanstore.conflictresolution.EnumAggregationErrorStrategy;
 import cz.cuni.mff.odcleanstore.conflictresolution.EnumAggregationType;
 import cz.cuni.mff.odcleanstore.engine.common.FormatHelper;
 import cz.cuni.mff.odcleanstore.engine.outputws.output.HTMLFormatter;
 import cz.cuni.mff.odcleanstore.engine.outputws.output.QueryResultFormatter;
 import cz.cuni.mff.odcleanstore.engine.outputws.output.TriGFormatter;
 import cz.cuni.mff.odcleanstore.queryexecution.QueryExecution;
 import cz.cuni.mff.odcleanstore.queryexecution.QueryExecutionException;
 import cz.cuni.mff.odcleanstore.shared.Utils;
 import cz.cuni.mff.odcleanstore.transformer.TransformerException;
 
 import org.restlet.data.Form;
 import org.restlet.data.Reference;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.resource.Get;
 import org.restlet.resource.Post;
 import org.restlet.resource.ServerResource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  *  @author Petr Jerman
  */
 public abstract class QueryExecutorResourceBase extends ServerResource {
 
     private static final Logger LOG = LoggerFactory.getLogger(QueryExecutorResourceBase.class);
 
     private static final String DEFAULT_AGGREGATION_PARAM = "aggr";
     private static final String DEFAULT_MULTIVALUE_PARAM = "multivalue";
     private static final String ERROR_STRATEGY_PARAM = "es";
     private static final String FORMAT_PARAM = "format";
     private static final String PROPERTY_AGGREGATION_PARAM = "paggr[";
     private static final String PROPERTY_MULTIVALUE_PARAM = "pmultivalue[";
 
     private static final String TRUE_LITERAL = "1";
     
     /**
      * Class implementing the "initialize-on-demand holder class" idiom.
      * Used to lazily initialize QueryExecution (when the config is already loaded).
      * @see http://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
      */
     private static class QueryExecutionHolder {
         /**
          * Cached instance of QueryExecution.
          * Query Execution is thread safe and should be maintained between requests because 
          * it can keep cached data of its own.
          */
         static final QueryExecution QUERY_EXECUTION = new QueryExecution(
                 ConfigLoader.getConfig().getBackendGroup().getCleanDBJDBCConnectionCredentials(),
                 ConfigLoader.getConfig());
     }
 
     /**
      * Returns the singleton instance of {@link QueryExecution}.
      * @return QueryExecution instance
      */
     protected static QueryExecution getQueryExecution() {
         return QueryExecutionHolder.QUERY_EXECUTION;
     }
     
     private Form form;
     
     protected String getFormValue(String key) {
         return form.getFirstValue(key);
     }
 
     protected String[] getFormValuesArray(String key) {
         return form.getValuesArray(key);
     }
 
     protected Map<String, String> getValuesMap() {
         return form.getValuesMap();
     }
 
     protected Reference getRequestReference() {
         return this.getRequest().getResourceRef();
     }
 
     @Get
     public Representation executeGet() {
         return parseRequest(false, null) ? executeInternal() : null;
     }
 
     @Post
     public Representation executePost(Representation entity) {
         return parseRequest(true, entity) ? executeInternal() : null;
     }
 
     private boolean parseRequest(boolean isPost, Representation entity) {
         try {
             if (isPost) {
                 form = new Form(entity);
             } else {
                 form = this.getQuery();
             }
             return true;
         } catch (Exception e) {
             LOG.warn(FormatHelper.formatExceptionForLog(e, "Client error bad request"));
             getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
             return false;
         }
     }
 
     private Representation executeInternal() {
         try {
             if (((Root) getApplication()).canServeRequest()) {
                 return execute();
             }
             getResponse().setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
         } catch (ResultEmptyException e) {
             LOG.warn("Response has no content");
             getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
         } catch (QueryExecutionException e) {
             switch (e.getErrorType()) {
             case QUERY_TOO_LONG:
             case INVALID_QUERY_FORMAT:
             case AGGREGATION_SETTINGS_INVALID:
             case UNKNOWN_PREFIX:
                LOG.warn(FormatHelper.formatExceptionForLog(e, "Client error bad request"));
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
                 break;
             case DATABASE_ERROR:
             case DEFAULT_AGGREGATION_SETTINGS_INVALID:
             case QUERY_EXECUTION_SETTINGS_INVALID:
             case CONFLICT_RESOLUTION_ERROR:
             default:
                LOG.error(FormatHelper.formatExceptionForLog(e, "Server error internal"));
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
             }
         } catch (Exception e) {
             LOG.error(FormatHelper.formatExceptionForLog(e, "Server error internal"));
             getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
         }
         return null;
     }
 
     /**
      * Serve the current request.
      * @return response to the current request
      * @throws QueryExecutionException exception
      * @throws ResultEmptyException exception
      * @throws TransformerException exception
      */
     protected abstract Representation execute()
             throws QueryExecutionException, ResultEmptyException, TransformerException;
 
     /**
      * Returns an appropriate formatter of the result.
      * @param outputWSConfig Configuration of the output webservice from the global configuration file.
      * @return formatter of query result
      *         TODO: accept also content-negotiation (Accept request header)
      */
     protected QueryResultFormatter getFormatter(OutputWSConfig outputWSConfig) {
         String formatName = getFormValue(FORMAT_PARAM);
 
         if (formatName != null && !formatName.isEmpty()) {
             if (formatName.equalsIgnoreCase("trig")) {
                 return new TriGFormatter(outputWSConfig);
             }
         }
         return new HTMLFormatter(outputWSConfig);
     }
 
     /**
      * Reads parameters given to the output webservice and build an AggregationSpec object according to it.
      * @return aggregation settings given to the output webservice
      */
     protected AggregationSpec getAggregationSpec() {
         AggregationSpec aggregationSpec = new AggregationSpec();
         Map<String, EnumAggregationType> propertyAggregations = new TreeMap<String, EnumAggregationType>();
         Map<String, Boolean> propertyMultivalue = new TreeMap<String, Boolean>();
 
         String defaultAggregation = getFormValue(DEFAULT_AGGREGATION_PARAM);
         if (defaultAggregation != null && !defaultAggregation.isEmpty()) {
             aggregationSpec.setDefaultAggregation(EnumAggregationType.valueOf(defaultAggregation));
         }
         String defaultMultivalue = getFormValue(DEFAULT_MULTIVALUE_PARAM);
         if (defaultMultivalue != null && !defaultMultivalue.isEmpty()) {
             aggregationSpec.setDefaultMultivalue(TRUE_LITERAL.equals(defaultMultivalue));
         }
         String errorStrategy = getFormValue(ERROR_STRATEGY_PARAM);
         if (errorStrategy != null && !errorStrategy.isEmpty()) {
             aggregationSpec.setErrorStrategy(EnumAggregationErrorStrategy.valueOf(errorStrategy));
         }
 
         Map<String, String> valuesMap = getValuesMap();
         for (String param : valuesMap.keySet()) {
             if (param.startsWith(PROPERTY_AGGREGATION_PARAM) && param.endsWith("]")) {
                 String property = param.substring(PROPERTY_AGGREGATION_PARAM.length(), param.length() - 1);
                 String aggregationString = valuesMap.get(param);
                 if (!Utils.isNullOrEmpty(aggregationString) && !property.isEmpty()) {
                     EnumAggregationType aggregation = EnumAggregationType.valueOf(aggregationString); // TODO: error handling
                     propertyAggregations.put(property, aggregation);
                 }
             } else if (param.startsWith(PROPERTY_MULTIVALUE_PARAM) && param.endsWith("]")) {
                 String property = param.substring(PROPERTY_MULTIVALUE_PARAM.length(), param.length() - 1);
                 String multivalueString = valuesMap.get(param);
                 if (!Utils.isNullOrEmpty(multivalueString) && !property.isEmpty()) {
                     propertyMultivalue.put(property, TRUE_LITERAL.equals(multivalueString));
                 }
             }
         }
         aggregationSpec.setPropertyAggregations(propertyAggregations);
         aggregationSpec.setPropertyMultivalue(propertyMultivalue);
         return aggregationSpec;
     }
 }
