 package za.co.yellowfire.carat.metrics;
 
 import com.codahale.metrics.JmxReporter;
 import com.codahale.metrics.MetricRegistry;
 import com.codahale.metrics.health.HealthCheckRegistry;
 import com.codahale.metrics.servlets.AdminServletContextListener;
 
 import javax.enterprise.inject.Produces;
 
 public class MetricsAdminServletContextListener extends AdminServletContextListener {
 
     protected final static MetricRegistry metricRegistry = new MetricRegistry();
     private final static JmxReporter reporter = JmxReporter.forRegistry(metricRegistry).build();
     private final static HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
 
     @Override
     //@Produces @Metrics
     protected MetricRegistry getMetricRegistry() {
         return metricRegistry;
     }
 
     @Override
     //@Produces @Metrics
     protected HealthCheckRegistry getHealthCheckRegistry() {
         return healthCheckRegistry;
     }
 
    // @Produces @Metrics
     protected JmxReporter getJmxReporter() {
         return reporter;
     }
 }
