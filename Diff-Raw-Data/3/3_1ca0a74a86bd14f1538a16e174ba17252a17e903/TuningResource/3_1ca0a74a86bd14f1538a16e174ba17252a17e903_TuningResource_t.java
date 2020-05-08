 package eu.cloudtm.autonomicManager.RESTServer.resources;
 
 import com.google.gson.Gson;
 import eu.cloudtm.autonomicManager.AutonomicManager;
 import eu.cloudtm.autonomicManager.commons.Forecaster;
 import eu.cloudtm.autonomicManager.commons.InstanceConfig;
 import eu.cloudtm.autonomicManager.commons.PlatformConfiguration;
 import eu.cloudtm.autonomicManager.commons.ReplicationProtocol;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response;
 
 @Singleton
 @Path("/tuning")
 public class TuningResource extends AbstractResource {
 
    private static Log log = LogFactory.getLog(TuningResource.class);
    private Gson gson = new Gson();
 
    @Inject
    private AutonomicManager autonomicManager;
 
 
    @POST
    @Path("/forecaster")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public synchronized Response setForecaster(
          @DefaultValue("ANALYTICAL") @FormParam("forecaster") Forecaster forecaster
    ) {
 
       try {
          log.info("updating forecaster");
          autonomicManager.updateForecaster(forecaster);
          //String json = gson.toJson(ControllerOld.getInstance().getState());
 
          String json = "{ \"result\" : \"done\" }";
          Response.ResponseBuilder builder = Response.ok(json);
          return makeCORS(builder);
 
       } catch (Exception e) {
          e.printStackTrace();
       }
 
       String json = "{ \"result\" : \"fail\" }";
       Response.ResponseBuilder builder = Response.ok(json);
       return makeCORS(builder);
    }
 
    @GET
    @Path("/updateAll")
    @Produces("application/json")
    public synchronized Response updateAllForecasters() {
 
       log.info("updating all forecasters");
       PlatformConfiguration platformConfiguration = autonomicManager.forecast();
       //String json = gson.toJson(ControllerOld.getInstance().getState());
 
       if (platformConfiguration != null) {
          log.info("platformConfiguration not null");
          String json = gson.toJson(platformConfiguration);
          Response.ResponseBuilder builder = Response.ok(json);
          return makeCORS(builder);
       }
 
       log.info("platformConfiguration null");
 
       String json = "{ \"result\" : \"fail\" }";
       Response.ResponseBuilder builder = Response.ok(json);
       return makeCORS(builder);
    }
 
    @POST
    @Path("/scale")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public synchronized Response setScale(
          @DefaultValue("TRUE") @FormParam("scale_tuning") Boolean autotuning,
          @DefaultValue("-1") @FormParam("scale_size") int size,
          @DefaultValue("MEDIUM") @FormParam("instance_type") InstanceConfig instanceType
    ) {
 
       try {
          if (autotuning) {
             log.info("AutoScale req");
             autonomicManager.updateScale(true, -1, InstanceConfig.MEDIUM);
          } else {
             log.info("!AutoScale req");
             if (!(size >= 0 && instanceType != null)) {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
             }
             autonomicManager.updateScale(false, size, instanceType);
          }
 
          String json = "{ \"todo\" : \"da impl\" }";
          Response.ResponseBuilder builder = Response.ok(json);
          return makeCORS(builder);
       } catch (Exception e) {
          e.printStackTrace();
       }
       String json = "{ \"result\" : \"fail\" }";
       Response.ResponseBuilder builder = Response.ok(json);
       return makeCORS(builder);
    }
 
 
    @POST
    @Path("/degree")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public synchronized Response setDegree(
          @DefaultValue("TRUE") @FormParam("rep_degree_tuning") Boolean autotuning,
          @DefaultValue("-1") @FormParam("rep_degree_size") int degree
    ) {
 
       try {
          if (autotuning) {
             log.info("AutoDegree req");
             autonomicManager.updateDegree(true, -1);
          } else {
             log.info("!AutoDegree req");
             if (degree < 0) {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
             }
             autonomicManager.updateDegree(false, degree);
          }
 
          String json = "{ \"todo\" : \"done\" }";
          Response.ResponseBuilder builder = Response.ok(json);
          return makeCORS(builder);
       } catch (Exception e) {
          e.printStackTrace();
       }
       String json = "{ \"result\" : \"fail\" }";
       Response.ResponseBuilder builder = Response.ok(json);
       return makeCORS(builder);
    }
 
    @POST
    @Path("/protocol")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public synchronized Response setProtocol(
          @DefaultValue("TRUE") @FormParam("rep_protocol_tuning") Boolean autotuning,
          @DefaultValue("TWOPC") @FormParam("rep_protocol") ReplicationProtocol protocol
    ) {
 
       try {
          if (autotuning) {
             log.info("AutoProtocol req");
             autonomicManager.updateProtocol(true, null);
          } else {
             log.info("!AutoProtocol req");
             if (protocol == null) {
               log.error("Trying to set a null protocol!!");
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
             }
             autonomicManager.updateProtocol(false, protocol);
          }
 
          String json = "{ \"todo\" : \"done\" }";
          Response.ResponseBuilder builder = Response.ok(json);
          return makeCORS(builder);
       } catch (Exception e) {
          e.printStackTrace();
         log.trace(e.getMessage());
       }
       String json = "{ \"result\" : \"fail\" }";
       Response.ResponseBuilder builder = Response.ok(json);
       return makeCORS(builder);
    }
 }
