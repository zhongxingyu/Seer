 package fi.gekkio.splake.metrics;
 
 import lombok.val;
 
 import org.zkoss.zk.ui.Executions;
 import org.zkoss.zk.ui.WebApp;
 import org.zkoss.zk.ui.util.WebAppCleanup;
 import org.zkoss.zk.ui.util.WebAppInit;
 
 import com.google.common.base.Preconditions;
 import com.yammer.metrics.Metrics;
 import com.yammer.metrics.core.MetricsRegistry;
 
 /**
  * Provides a default implementation of ZK metrics.
  * 
 * Metrics will be exported to JMX with names starting with <strong>fi.jawsy.jawwa.zk.metrics:type=ZkMetrics</strong>.
  * Also manages the lifecycle of the default static {@link MetricsRegistry}, so it is shut down properly with the
  * application.
  */
 public class DefaultZkMetrics extends ZkMetricsImpl implements WebAppInit, WebAppCleanup {
 
     static final String ATTRIBUTE_NAME = DefaultZkMetrics.class.getName();
 
     /**
      * Attempts to lookup a {@link ZkMetricsImpl} instance from the currently running ZK execution.
      * 
      * @return metrics instance or null if not found
      */
     public static ZkMetrics lookupMetrics() {
         val execution = Preconditions.checkNotNull(Executions.getCurrent(), "No current ZK execution");
         return lookupMetrics(execution.getDesktop().getWebApp());
     }
 
     /**
      * Attempts to lookup a {@link ZkMetricsImpl} instance from the specified ZK web app.
      * 
      * @param wapp
      *            webapp instance
      * @return metrics instance or null if not found
      */
     public static ZkMetrics lookupMetrics(WebApp wapp) {
         return (ZkMetrics) wapp.getAttribute(ATTRIBUTE_NAME);
     }
 
     public DefaultZkMetrics() {
         super(Metrics.defaultRegistry());
     }
 
     @Override
     public void cleanup(WebApp wapp) throws Exception {
         wapp.removeAttribute(ATTRIBUTE_NAME);
         Metrics.shutdown();
     }
 
     @Override
     public void init(WebApp wapp) throws Exception {
         if (!wapp.hasAttribute(ATTRIBUTE_NAME))
             wapp.setAttribute(ATTRIBUTE_NAME, this);
     }
 
 }
