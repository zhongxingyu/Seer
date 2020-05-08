 package eu.dm2e.ws.services.publish;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import javax.ws.rs.Path;
 
 import org.joda.time.DateTime;
 
 import eu.dm2e.ws.Config;
 import eu.dm2e.ws.ConfigProp;
 import eu.dm2e.ws.api.IWebservice;
 import eu.dm2e.ws.api.JobPojo;
 import eu.dm2e.ws.api.VersionedDatasetPojo;
 import eu.dm2e.ws.api.WebserviceConfigPojo;
 import eu.dm2e.ws.grafeo.Grafeo;
 import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
 import eu.dm2e.ws.services.AbstractTransformationService;
 //import java.util.Date;
 
 /**
  * Service to publish files as versioned datasets to a RDF endpoint.
  */
 @Path("/publish")
 public class PublishService extends AbstractTransformationService {
 	
 	public static final String PARAM_ENDPOINT_SELECT = "endpoint-select";
 	public static final String PARAM_ENDPOINT_UPDATE = "endpoint-update";
     public static final String PARAM_PROVIDER_ID = "provider-id";
 	public static final String PARAM_COMMENT = "comment";
     public static final String PARAM_TO_PUBLISH = "to-publish";
 	public static final String PARAM_LABEL = "label";
 	public static final String PARAM_DATASET_ID = "dataset-id";
 	public static final String PARAM_RESULT_DATASET_ID = "result-dataset-id";
 
     public PublishService() {
         IWebservice ws = getWebServicePojo();
         ws.setLabel("Publish");
         ws.addInputParameter(PARAM_TO_PUBLISH).setIsRequired(true);
         ws.addInputParameter(PARAM_DATASET_ID).setIsRequired(true);
         ws.addInputParameter(PARAM_LABEL).setIsRequired(true);
         ws.addInputParameter(PARAM_PROVIDER_ID).setIsRequired(true);
         ws.addInputParameter(PARAM_COMMENT);
         ws.addInputParameter(PARAM_ENDPOINT_UPDATE);
         ws.addInputParameter(PARAM_ENDPOINT_SELECT);
         ws.addOutputParameter(PARAM_RESULT_DATASET_ID);
     }
 
     @Override
     public void run() {
     	JobPojo jobPojo = getJobPojo();
         try {
             WebserviceConfigPojo wsConf = jobPojo.getWebserviceConfig();
             jobPojo.debug("wsConf: " + wsConf);
 
             String input = wsConf.getParameterValueByName(PARAM_TO_PUBLISH);
             String dataset = wsConf.getParameterValueByName(PARAM_DATASET_ID);
             String label = wsConf.getParameterValueByName(PARAM_LABEL);
             String provider = wsConf.getParameterValueByName(PARAM_PROVIDER_ID);
             String comment = wsConf.getParameterValueByName(PARAM_COMMENT);
             String endpoint = wsConf.getParameterValueByName(PARAM_ENDPOINT_UPDATE);
             String endpointSelect = wsConf.getParameterValueByName(PARAM_ENDPOINT_SELECT);
             if (null == endpoint) endpoint = Config.get(ConfigProp.ENDPOINT_UPDATE);
             if (null == endpointSelect) endpointSelect = Config.get(ConfigProp.ENDPOINT_QUERY);
 
             jobPojo.debug("Dataset: " + dataset);
             jobPojo.debug("Input file: " + input);
             jobPojo.debug("Label: " + label);
             jobPojo.debug("Comment: " + comment);
             jobPojo.debug("Endpoint: " + endpoint);
 
             jobPojo.setStarted();
 
 //            if (null == dataset) {
 //            	dataset = createUniqueStr();
 //            }
             String datasetURI = dataset;
             if (!dataset.startsWith("http")) datasetURI = Config.get(ConfigProp.PUBLISH_GRAPH_PREFIX) + provider + "/" + dataset;
 
             String versionedURI = datasetURI;
             if (!versionedURI.endsWith("/")) versionedURI = versionedURI + "/";
             versionedURI = versionedURI + DateTime.now().getMillis();
 
             VersionedDatasetPojo ds = new VersionedDatasetPojo();
             ds.setId(versionedURI);
             ds.setLabel(label != null ? label : dataset);
             ds.setComment(comment);
             ds.setTimestamp(DateTime.now());
             try {
                 ds.setJobURI(new URI(jobPojo.getId()));
                 ds.setDatasetID(new URI(datasetURI));
             } catch (URISyntaxException e) {
                 throw new RuntimeException("Error in URI: " + e, e);
             }
             ds.findLatest(endpointSelect);
 
 
             Grafeo g = new GrafeoImpl();
             g.loadWithoutContentNegotiation(input);
             g.getObjectMapper().addObject(ds);
             // TODO Logging this can be very expensive (dozens of MB of output for FFM)
 //            log.info("Published graph: " + g.getTurtle());
 //            jobPojo.debug("Published graph: " + g.getTurtle());
             log.info("Write to endpoint: " + endpoint + " / Graph: " + versionedURI);
             g.postToEndpoint(endpoint, versionedURI);
             jobPojo.addOutputParameterAssignment(PARAM_RESULT_DATASET_ID, versionedURI);
 
         } catch (Throwable t) {
             log.error("Exception during publishing: " + t, t);
             jobPojo.fatal(t);
             jobPojo.setFailed();
             throw t;
        }
        jobPojo.setFinished();
     }


}
