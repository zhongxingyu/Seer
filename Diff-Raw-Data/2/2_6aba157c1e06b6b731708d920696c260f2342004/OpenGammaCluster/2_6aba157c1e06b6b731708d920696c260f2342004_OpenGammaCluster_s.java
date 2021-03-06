 package io.cloudsoft.opengamma;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import io.cloudsoft.amp.entities.BasicStartable;
 import io.cloudsoft.amp.entities.BasicStartable.LocationsFilter;
 import io.cloudsoft.amp.entities.DynamicRegionsFabric;
 import io.cloudsoft.amp.policies.ServiceFailureDetector;
 import io.cloudsoft.amp.policies.ServiceReplacer;
 import io.cloudsoft.amp.policies.ServiceRestarter;
 import io.cloudsoft.opengamma.server.OpenGammaMonitoringAggregation;
 import io.cloudsoft.opengamma.server.OpenGammaServer;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import brooklyn.catalog.CatalogConfig;
 import brooklyn.config.ConfigKey;
 import brooklyn.config.StringConfigMap;
 import brooklyn.enricher.HttpLatencyDetector;
 import brooklyn.enricher.basic.SensorPropagatingEnricher;
 import brooklyn.enricher.basic.SensorTransformingEnricher;
 import brooklyn.entity.Entity;
 import brooklyn.entity.basic.AbstractApplication;
 import brooklyn.entity.basic.Attributes;
 import brooklyn.entity.basic.Entities;
 import brooklyn.entity.basic.EntityFactory;
 import brooklyn.entity.basic.EntityLocal;
 import brooklyn.entity.basic.SoftwareProcess;
 import brooklyn.entity.basic.StartableApplication;
 import brooklyn.entity.database.postgresql.PostgreSqlNode;
 import brooklyn.entity.dns.geoscaling.GeoscalingDnsService;
 import brooklyn.entity.group.DynamicCluster;
 import brooklyn.entity.group.DynamicFabric;
 import brooklyn.entity.messaging.activemq.ActiveMQBroker;
 import brooklyn.entity.proxy.AbstractController;
 import brooklyn.entity.proxying.EntitySpecs;
 import brooklyn.entity.trait.Changeable;
 import brooklyn.entity.webapp.ControlledDynamicWebAppCluster;
 import brooklyn.entity.webapp.WebAppService;
 import brooklyn.entity.webapp.WebAppServiceConstants;
 import brooklyn.event.SensorEvent;
 import brooklyn.event.SensorEventListener;
 import brooklyn.event.basic.BasicConfigKey;
 import brooklyn.launcher.BrooklynLauncher;
 import brooklyn.location.basic.PortRanges;
 import brooklyn.policy.autoscaling.AutoScalerPolicy;
 import brooklyn.util.CommandLineUtil;
 import brooklyn.util.MutableMap;
 
 import com.google.common.base.Functions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 
 public class OpenGammaCluster extends AbstractApplication implements StartableApplication {
     
     private static final long serialVersionUID = 997984655016594134L;
     public static final Logger LOG = LoggerFactory.getLogger(OpenGammaCluster.class);
     
     public static final String DEFAULT_LOCATION = "localhost";
 
     @CatalogConfig(label="Debug Mode", priority=2)
     public static final ConfigKey<Boolean> DEBUG_MODE = OpenGammaServer.DEBUG_MODE;
 
     @CatalogConfig(label="Multi-Region", priority=1)
     public static final ConfigKey<Boolean> SUPPORT_MULTIREGION = new BasicConfigKey<Boolean>(Boolean.class,
             "opengamma.multiregion", "Whether to run multi-region", true);
 
     /** build the application */
     @Override
     public void init() {
         StringConfigMap config = getManagementContext().getConfig();
         
         // First define the stock service entities (message bus broker and database server) for OG
         
         BasicStartable backend = addChild(EntitySpecs.spec(BasicStartable.class)
                 .displayName("OpenGamma Back-End")
                 .configure(BasicStartable.LOCATIONS_FILTER, LocationsFilter.USE_FIRST_LOCATION));
         final ActiveMQBroker broker = backend.addChild(EntitySpecs.spec(ActiveMQBroker.class));
         final PostgreSqlNode database = backend.addChild(EntitySpecs.spec(PostgreSqlNode.class)
                 .configure(PostgreSqlNode.CREATION_SCRIPT_URL, "classpath:/io/cloudsoft/opengamma/config/create-brooklyn-db.sql"));
 
         // Now add the server tier, either multi-region (fabric) or fixed single-region (cluster)
 
         // factory for creating the OG server cluster, passed to fabric, or used directly here to make a cluster   
         EntityFactory<Entity> ogWebClusterFactory = new EntityFactory<Entity>() {
             @Override
             public Entity newEntity(@SuppressWarnings("rawtypes") Map flags, Entity parent) {
                 ControlledDynamicWebAppCluster ogWebCluster = parent.addChild(EntitySpecs.spec(ControlledDynamicWebAppCluster.class)
                         .displayName("Load-Balanced Cluster") 
                         .configure(ControlledDynamicWebAppCluster.INITIAL_SIZE, 2)
                         .configure(ControlledDynamicWebAppCluster.MEMBER_SPEC, 
                             EntitySpecs.spec(OpenGammaServer.class).displayName("OpenGamma Server")
                             .configure(OpenGammaServer.BROKER, broker)
                             .configure(OpenGammaServer.DATABASE, database)) );
                 
                 initAggregatingMetrics(ogWebCluster);
                 initResilience(ogWebCluster);
                 initElasticity(ogWebCluster);
                 return ogWebCluster;
             }
         };
 
         // use fabric by default, unless no password for geoscaling is set
         String geoscalingPassword = config.getFirst("brooklyn.geoscaling.password");
         
         if (getConfig(SUPPORT_MULTIREGION) && geoscalingPassword!=null) {
             log.info("GeoScaling support detected. Running in multi-cloud mode.");
             
             GeoscalingDnsService geoDns = addChild(EntitySpecs.spec(GeoscalingDnsService.class)
                     .displayName("GeoScaling DNS")
                     .configure("username", checkNotNull(config.getFirst("brooklyn.geoscaling.username"), "username"))
                     .configure("password", geoscalingPassword)
                     .configure("primaryDomainName", checkNotNull(config.getFirst("brooklyn.geoscaling.primaryDomain"), "primaryDomain")) 
                     .configure("smartSubdomainName", "brooklyn"));
 
             DynamicRegionsFabric webFabric = addChild(EntitySpecs.spec(DynamicRegionsFabric.class)
                     .displayName("Dynamic Regions Fabric")
                     .configure(DynamicFabric.FACTORY, ogWebClusterFactory)
                     .configure(AbstractController.PROXY_HTTP_PORT, PortRanges.fromCollection(ImmutableList.of(80,"8000+"))) );
 
             // tell GeoDNS what to monitor
             geoDns.setTargetEntityProvider(webFabric);
 
             // bubble up sensors (kpi's and access info), from WebFabric and GeoDNS
             OpenGammaMonitoringAggregation.aggregateOpenGammaClusterSensors(webFabric);
             OpenGammaMonitoringAggregation.promoteKpis(this, webFabric);
             addEnricher(new SensorTransformingEnricher<Integer,Integer>(webFabric, Changeable.GROUP_SIZE, 
                     OpenGammaMonitoringAggregation.REGIONS_COUNT, Functions.<Integer>identity()));
             addEnricher(new SensorTransformingEnricher<String,String>(geoDns, Attributes.HOSTNAME, WebAppServiceConstants.ROOT_URL, 
                     OpenGammaMonitoringAggregation.surround("http://","/")));
         } else {
            if (!getConfig(SUPPORT_MULTIREGION))
                 log.warn("No password set for GeoScaling. Creating "+this+" in single-cluster mode.");
             else
                 log.info("Configured not to have multi-region support. Creating "+this+" in single-cluster mode.");
             
             Entity ogWebCluster = ogWebClusterFactory.newEntity(MutableMap.of(), this);
             
             // bubble up sensors (kpi's and access info) - in single-cluster mode it all comes from cluster (or is hard-coded)
             OpenGammaMonitoringAggregation.promoteKpis(this, ogWebCluster);
             setAttribute(OpenGammaMonitoringAggregation.REGIONS_COUNT, 1);
             addEnricher(SensorPropagatingEnricher.newInstanceListeningTo(ogWebCluster,  
                     WebAppServiceConstants.ROOT_URL));
         }
     }
 
     /** aggregate metrics and selected KPI's */
     protected void initAggregatingMetrics(ControlledDynamicWebAppCluster web) {
         web.addEnricher(HttpLatencyDetector.builder().
                 url(WebAppService.ROOT_URL).
                 rollup(10, TimeUnit.SECONDS).
                 build());
         OpenGammaMonitoringAggregation.aggregateOpenGammaServerSensors(web.getCluster());
     }
 
 
     /** this attaches a policy at each OG Server listening for ENTITY_FAILED,
      * attempting to _restart_ the process, and 
      * failing that attempting to _replace_ the entity (e.g. a new VM), and 
      * failing that setting the cluster "on-fire" */
     protected void initResilience(ControlledDynamicWebAppCluster web) {
         ((EntityLocal)web).subscribe(web.getCluster(), DynamicCluster.MEMBER_ADDED, new SensorEventListener<Entity>() {
             @Override
             public void onEvent(SensorEvent<Entity> addition) {
                 initSoftwareProcess((SoftwareProcess)addition.getValue());
             }
         });
         web.getCluster().addPolicy(new ServiceReplacer(ServiceRestarter.ENTITY_RESTART_FAILED));
     }
 
     /** invoked whenever a new OpenGamma server is added (the server may not be started yet) */
     protected void initSoftwareProcess(SoftwareProcess p) {
         p.addPolicy(new ServiceFailureDetector());
         p.addPolicy(new ServiceRestarter(ServiceFailureDetector.ENTITY_FAILED));
     }
 
     /** configures scale-out and scale-back; in this case based on number of view processes active,
      * allowing an (artificially low) max of 1.2 per node, 
      * so as soon as you have 3 view processes a scale-out is forced */
     protected void initElasticity(ControlledDynamicWebAppCluster web) {
         web.getCluster().addPolicy(AutoScalerPolicy.builder().
                 metric(OpenGammaMonitoringAggregation.VIEW_PROCESSES_COUNT_PER_NODE).
                 metricRange(0.8, 1.2).
                 sizeRange(2, 5).
                 build());
     }
 
     public static void main(String[] argv) {
         List<String> args = Lists.newArrayList(argv);
         String port =  CommandLineUtil.getCommandLineOption(args, "--port", "8081+");
         
         List<String> locations = new ArrayList<String>();
         while (true) {
             String l = CommandLineUtil.getCommandLineOption(args, "--location", null);
             if (l!=null) locations.add(l);
             else break;
         }
         if (locations.isEmpty()) locations.add(DEFAULT_LOCATION);
 
         BrooklynLauncher launcher = BrooklynLauncher.newInstance()
                  .application(EntitySpecs.appSpec(OpenGammaCluster.class)
                          .displayName("OpenGamma Elastic Multi-Region"))
                  .webconsolePort(port)
                  .locations(locations)
                  .start();
              
         Entities.dumpInfo(launcher.getApplications());
     }
 }
