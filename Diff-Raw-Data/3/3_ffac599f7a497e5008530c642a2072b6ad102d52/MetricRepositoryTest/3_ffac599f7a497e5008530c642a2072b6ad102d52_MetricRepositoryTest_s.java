 /**
  * 
  */
 package com.monstersoftwarellc.graphtastic.repository;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Date;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.monstersoftwarellc.graphtastic.TestApplicationContext;
 import com.monstersoftwarellc.graphtastic.model.Metric;
 import com.monstersoftwarellc.graphtastic.repository.MetricRepository.Count;
 import com.monstersoftwarellc.graphtastic.utility.MetricUtility;
 
 /**
  * Tests functionality provided by the {@link MetricRepository}.
  * @author nicholas
  *
  */
 public class MetricRepositoryTest extends TestApplicationContext {
 	
 	private static final Logger LOG = Logger.getLogger(MetricRepositoryTest.class);
 	
 	@Autowired
 	private MetricRepository metricRepository;
 	
 	@Test
 	@Transactional
 	public void testSaveAndFindByName(){
 		long timestamp = new Date().getTime();
 		metricRepository.save(MetricUtility.buildMetric("LOG", "INFO", timestamp));
 		List<Metric> metrics = metricRepository.findByName("LOG");
 		assertTrue(!metrics.isEmpty());
 		Metric metric = metrics.get(0);
 		assertNotNull(metric.getId());
 		assertEquals("LOG", metric.getName());
 		assertEquals(timestamp, metric.getTimestamp());
 		assertEquals("INFO", metric.getValue());
 	}
 	
 	@Test
 	@Transactional
 	public void testSaveAndFindByNameAndPage(){
		long timestamp = new Date().getTime();
 		saveGroup(100, "LOG");
 		Page<Metric> metrics = metricRepository.findByName("LOG", new PageRequest(0, 10));
 		assertTrue(metrics.hasContent());
 		assertEquals(10, metrics.getSize());
 		Metric metric = metrics.getContent().get(0);
 		assertNotNull(metric.getId());
 		assertEquals("LOG", metric.getName());
		assertEquals(timestamp, metric.getTimestamp());
 		assertEquals("INFO", metric.getValue());
 	}
 	
 	@Test
 	@Transactional
 	public void test100SavesAndFindByNameAndTimestagetMetricsmpGreaterThanAndTimestampLessThan(){
 		long timestamp = new Date().getTime();
 		// save 100 more and save new date every time.
 		saveGroup(100, "LOG");
 		long timestamp2 = new Date().getTime();		
 		List<Metric> metrics = metricRepository.findByNameAndTimestampGreaterThanAndTimestampLessThan("LOG", timestamp, timestamp2);
 		assertTrue(!metrics.isEmpty());
 		assertTrue(metrics.size() == 100);// include zero
 		assertTrue(metricRepository.count() == 100);
 	}
 	
 	@Test
 	@Transactional
 	public void test100SavesAndFindByValueAndTimestampGreaterThanAndTimestampLessThan(){
 		long timestamp = new Date().getTime();
 		// save 100 more and save new date every time.
 		saveGroup(100, "LOG");
 		long timestamp2 = new Date().getTime();		
 		List<Metric> metrics = metricRepository.findByValueAndTimestampGreaterThanAndTimestampLessThan("DEBUG", timestamp, timestamp2);
 		assertTrue(!metrics.isEmpty());
 		if(LOG.isDebugEnabled()){
 			LOG.debug("Size of Find by value : " + metrics.size() );
 		}
 		assertTrue(metrics.size() == 50);// include zero
 		assertTrue(metricRepository.count() == 100);
 	}
 	
 	@Test
 	@Transactional	
 	public void testCountByMetricName() throws InterruptedException{
 		long timestamp = new Date().getTime();
 		saveGroup(100, "TEST");
 		long timestamp2 = new Date().getTime();
 		// ensure we don't end up with the same time
 		Thread.currentThread();
 		Thread.sleep(1000);
 		saveGroup(100, "TEST");
 		List<Count> counts = metricRepository.getCountByMetricNameBetween("TEST", timestamp, timestamp2);
 		if(LOG.isDebugEnabled()){
 			for(Count count : counts){
 				LOG.debug("			time : " + count.getTimestamp() + "     count : " + count.getCount());
 			}
 		}
 		assertTrue(counts.size() <= 100 && counts.size() > 50);
 	}
 
 	/**
 	 * 
 	 */
 	private void saveGroup(int numberOfItems, String... names) {
 		Date start = new Date();
 		for(int i = 0; i < numberOfItems; i++){
 			String name = names.length == 1 ? names[0] : ((i % 2 == 0 ? names[0] : names[1]));
 			String value = (i % 2 == 0 ? "INFO" : "DEBUG");
 			metricRepository.save(MetricUtility.buildMetric(name, value, new Date().getTime()));
 		}
 		Date end = new Date();
 		if(LOG.isDebugEnabled()){
 			LOG.debug("Number saved : " + metricRepository.count() + "  In  " + (end.getTime() - start.getTime()) + "ms");
 		}
 	}
 
 }
