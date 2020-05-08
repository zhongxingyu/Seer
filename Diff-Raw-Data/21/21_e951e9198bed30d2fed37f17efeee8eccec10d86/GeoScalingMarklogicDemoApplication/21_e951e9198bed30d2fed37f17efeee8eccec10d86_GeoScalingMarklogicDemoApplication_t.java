 package io.cloudsoft.marklogic.brooklynapplications;
 
 import brooklyn.config.StringConfigMap;
 import brooklyn.entity.basic.AbstractApplication;
 import brooklyn.entity.dns.geoscaling.GeoscalingDnsService;
 import brooklyn.entity.group.DynamicFabric;
 import brooklyn.entity.proxy.AbstractController;
 import brooklyn.entity.proxy.nginx.NginxController;
 import brooklyn.entity.proxying.BasicEntitySpec;
 import brooklyn.entity.proxying.EntitySpecs;
 import brooklyn.entity.webapp.ControlledDynamicWebAppCluster;
 import brooklyn.entity.webapp.ElasticJavaWebAppService;
 import brooklyn.entity.webapp.JavaWebAppService;
 import brooklyn.entity.webapp.WebAppService;
 import brooklyn.entity.webapp.jboss.JBoss7Server;
 import brooklyn.location.basic.PortRanges;
 import com.google.common.collect.ImmutableList;
 import io.cloudsoft.marklogic.clusters.MarkLogicCluster;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.List;
 
 import static brooklyn.entity.java.JavaEntityMethods.javaSysProp;
 import static brooklyn.entity.proxying.EntitySpecs.spec;
 import static brooklyn.event.basic.DependentConfiguration.attributeWhenReady;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 public class GeoScalingMarklogicDemoApplication extends AbstractApplication {
     public static final Logger log = LoggerFactory.getLogger(GeoScalingMarklogicDemoApplication.class);
 
     private String appServiceName = "DemoService";
     private int appServicePort = 8011;
     private String databaseName = "DemoDatabase";
     private String password = "hap00p";
     private String username = "admin";
 
     @Override
     public void init() {
         StringConfigMap config = getManagementContext().getConfig();
 
         GeoscalingDnsService webGeoDns = addChild(EntitySpecs.spec(GeoscalingDnsService.class)
                 .displayName("Web GeoScaling DNS")
                 .configure("username", checkNotNull(config.getFirst("brooklyn.geoscaling.username"), "username"))
                 .configure("password", checkNotNull(config.getFirst("brooklyn.geoscaling.password"), "password"))
                 .configure("primaryDomainName", checkNotNull(config.getFirst("brooklyn.geoscaling.primaryDomain"), "primaryDomain"))
                 .configure("smartSubdomainName", "brooklyn"));
 
         GeoscalingDnsService marklogicGeoDns = addChild(EntitySpecs.spec(GeoscalingDnsService.class)
                 .displayName("Marklogic GeoScaling DNS")
                 .configure("username", checkNotNull(config.getFirst("brooklyn.geoscaling.username"), "username"))
                 .configure("password", checkNotNull(config.getFirst("brooklyn.geoscaling.password"), "password"))
                 .configure("primaryDomainName", checkNotNull(config.getFirst("brooklyn.geoscaling.primaryDomain"), "primaryDomain"))
                 .configure("smartSubdomainName", "brooklyn"));
 
         DynamicFabric markLogicFabric = addChild(EntitySpecs.spec(DynamicFabric.class)
                 .displayName("MarkLogic Fabric")
                 .configure(AbstractController.PROXY_HTTP_PORT, PortRanges.fromInteger(8011))
                 .configure(DynamicFabric.MEMBER_SPEC, spec(MarkLogicCluster.class)
                         .displayName("MarkLogic Cluster")
                         .configure(MarkLogicCluster.INITIAL_D_NODES_SIZE, 1)
                         .configure(MarkLogicCluster.INITIAL_E_NODES_SIZE, 1)));
         marklogicGeoDns.setTargetEntityProvider(markLogicFabric);
 
         DynamicFabric webFabric = addChild(EntitySpecs.spec(DynamicFabric.class)
                 .displayName("Web Fabric")
                 .configure(DynamicFabric.MEMBER_SPEC, BasicEntitySpec.newInstance(ControlledDynamicWebAppCluster.class)
                         .displayName("WebApp cluster")
                         .configure("initialSize", 1)
                         .configure(ControlledDynamicWebAppCluster.CONTROLLER_SPEC, spec(NginxController.class)
                                 .displayName("WebAppCluster Nginx")
                                 .configure("port", 8080)
                                 .configure("portNumberSensor", WebAppService.HTTP_PORT))
                         .configure(ControlledDynamicWebAppCluster.MEMBER_SPEC, spec(JBoss7Server.class)
                                 .configure("initialSize", 1)
                                 .configure("httpPort", 8080)
                                .configure(javaSysProp("marklogic.host"), attributeWhenReady(marklogicGeoDns, GeoscalingDnsService.HOSTNAME))
                                .configure(javaSysProp("marklogic.port"), "" + appServicePort)
                                .configure(javaSysProp("marklogic.password"), password)
                                .configure(javaSysProp("marklogic.user"), username)
                                 .configure(JavaWebAppService.ROOT_WAR, "classpath:/demo-war-0.1.0-SNAPSHOT.war"))
         ));
         webGeoDns.setTargetEntityProvider(webFabric);
     }
 
 }
