 package org.sonar.plugins.qi;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import org.junit.Test;
 
 public class QIMetricsTest {
 
   @Test
   public void testGetMetrics() {
     QIMetrics metrics = new QIMetrics();
     assertThat(metrics.getMetrics().size(), is(10));
   }
 }
