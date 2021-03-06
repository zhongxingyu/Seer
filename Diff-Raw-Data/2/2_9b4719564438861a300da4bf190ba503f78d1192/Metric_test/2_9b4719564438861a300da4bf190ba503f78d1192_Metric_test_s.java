 package org.hyperic.hq.hqapi1.test;
 
 import org.hyperic.hq.hqapi1.MetricApi;
 import org.hyperic.hq.hqapi1.types.DisableMetricResponse;
 import org.hyperic.hq.hqapi1.types.EnableMetricResponse;
 import org.hyperic.hq.hqapi1.types.GetMetricResponse;
 import org.hyperic.hq.hqapi1.types.ListMetricResponse;
 import org.hyperic.hq.hqapi1.types.Metric;
 import org.hyperic.hq.hqapi1.types.Resource;
 import org.hyperic.hq.hqapi1.types.SetMetricIntervalResponse;
 
 public class Metric_test extends MetricTestBase {
 
     public Metric_test(String name) {
         super(name);
     }
 
     protected void validateMetric(Metric m) throws Exception {
         assertNotNull(m);
         assertTrue(m.getId() > 0);
         assertNotNull(m.getInterval());
         assertNotNull(m.isEnabled());
         assertTrue(m.getName().length() > 0);
         assertNotNull(m.isDefaultOn());
        assertNotNull(m.isIndictor());
         assertTrue(m.getCollectionType().length() > 0);
     }
         
     public void testMetricList() throws Exception {
 
         Resource r = getResource();
         if (r == null) {
             getLog().error("Unable to find the local platform, skipping test");
             return;
         }
 
         MetricApi api = getApi().getMetricApi();
         ListMetricResponse resp = api.listMetrics(r);
         hqAssertSuccess(resp);
 
         for (Metric m : resp.getMetric()) {
             validateMetric(m);
         }
     }
 
     public void testMetricById() throws Exception {
 
         Resource r = getResource();
         if (r == null) {
             getLog().error("Unable to find the local platform, skipping test");
             return;
         }
 
         MetricApi api = getApi().getMetricApi();
         ListMetricResponse resp = api.listMetrics(r);
         hqAssertSuccess(resp);
 
         assertFalse("Resource " + r.getName() + " has no metrics",
                    resp.getMetric().size() == 0);
 
         for (Metric m : resp.getMetric()) {
             GetMetricResponse metricResponse = api.getMetric(m.getId());
             hqAssertSuccess(metricResponse);
             validateMetric(metricResponse.getMetric());  
         }
     }
 
     public void testMetricDisableEnable() throws Exception {
         Resource r = getResource();
         if (r == null) {
             getLog().error("Unable to find the local platform, skipping test");
             return;
         }
 
         MetricApi api = getApi().getMetricApi();
         ListMetricResponse resp = api.listMetrics(r);
         hqAssertSuccess(resp);
 
         assertFalse("Resource " + r.getName() + " has no metrics",
                    resp.getMetric().size() == 0);
 
         Metric m = resp.getMetric().get(0);
 
         // Disable
         DisableMetricResponse disableMetricResponse = api.disableMetric(m);
         hqAssertSuccess(disableMetricResponse);
 
         // Verify
         GetMetricResponse metricResponse = api.getMetric(m.getId());
         hqAssertSuccess(metricResponse);
         assertFalse("Metric id " + m.getId() + " not disabled",
                      metricResponse.getMetric().isEnabled());
 
         // Enable
         EnableMetricResponse enableResponse =
                 api.enableMetric(m, m.getMetricTemplate().getDefaultInterval());
         hqAssertSuccess(enableResponse);
 
         // Verify
         metricResponse = api.getMetric(m.getId());
         hqAssertSuccess(metricResponse);
         assertTrue("Metric id " + m.getId() + " not enabled",
                    metricResponse.getMetric().isEnabled());
     }
 
     public void testMetricSetInterval() throws Exception {
 
         Resource r = getResource();
         if (r == null) {
             getLog().error("Unable to find the local platform, skipping test");
             return;
         }
 
         MetricApi api = getApi().getMetricApi();
         ListMetricResponse resp = api.listMetrics(r);
         hqAssertSuccess(resp);
         assertFalse("Resource " + r.getName() + " has no metrics",
                    resp.getMetric().size() == 0);
 
         Metric m = resp.getMetric().get(0);
 
         final long INTERVAL = 60000;
 
         // Set new interval
         SetMetricIntervalResponse intervalResp = api.setInterval(m, INTERVAL);
         hqAssertSuccess(intervalResp);
 
 
         // Validate
         GetMetricResponse metricResponse = api.getMetric(m.getId());
         hqAssertSuccess(metricResponse);
         assertEquals(INTERVAL, metricResponse.getMetric().getInterval());
 
         // Reset
         intervalResp = api.setInterval(m, m.getMetricTemplate().getDefaultInterval());
         hqAssertSuccess(intervalResp);
 
         // Validate
         metricResponse = api.getMetric(m.getId());
         hqAssertSuccess(metricResponse);
         assertEquals(metricResponse.getMetric().getInterval(),
                      metricResponse.getMetric().getMetricTemplate().getDefaultInterval());
     }
 
     public void testMetricSetInvalidInterval() throws Exception {
 
         Resource r = getResource();
         if (r == null) {
             getLog().error("Unable to find the local platform, skipping test");
             return;
         }
 
         MetricApi api = getApi().getMetricApi();
         ListMetricResponse resp = api.listMetrics(r);
         hqAssertSuccess(resp);
         assertFalse("Resource " + r.getName() + " has no metrics",
                    resp.getMetric().size() == 0);
         
         Metric m = resp.getMetric().get(0);
 
         final long[] BAD_INTERVALS = { -1, 0, 1, 60, 60001 };
 
         for (long interval : BAD_INTERVALS) {
             SetMetricIntervalResponse intervalResponse =
                     api.setInterval(m, interval);
             hqAssertFailureInvalidParameters(intervalResponse);
         }
     }
 }
