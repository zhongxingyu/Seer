 package org.mashti.gauge.reporter;
 
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.mashti.gauge.Counter;
 import org.mashti.gauge.MetricRegistry;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
  * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
  */
 public class CsvReporterTest {
 
     public static final String TEST_METRIC_NAME = "test_metric";
     private MetricRegistry registry;
     private CsvReporter reporter;
     private Path reports_home;
     private Path test_report_path;
 
     @Before
     public void setUp() throws Exception {
 
         registry = new MetricRegistry("test_registry");
         registry.register(TEST_METRIC_NAME, new Counter());
         reports_home = Files.createTempDirectory("test_dir");
         test_report_path = reports_home.resolve(TEST_METRIC_NAME + ".csv");
         reporter = new CsvReporter(registry, reports_home);
     }
 
     @After
     public void tearDown() throws Exception {
         Files.delete(test_report_path);
         Files.delete(reports_home);
         reporter.stop();
     }
 
     @Test
     public void testReport() throws Exception {
 
         assertTrue(Files.exists(reports_home));
         assertFalse(Files.exists(test_report_path));
         reporter.report();
         assertTrue(Files.exists(test_report_path));
         assertEquals(2, Files.readAllLines(test_report_path, StandardCharsets.UTF_8).size());
         reporter.report();
         assertEquals(3, Files.readAllLines(test_report_path, StandardCharsets.UTF_8).size());
     }
 }
