 package gov.usgs.cida.data.grib;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import gov.usgs.cida.ncetl.jpa.ArchiveConfigManager;
 import gov.usgs.cida.ncetl.jpa.ArchiveConfig;
 
 import org.junit.Test;
 import org.springframework.context.ConfigurableApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.integration.endpoint.EventDrivenConsumer;
 
 public class IntegrationTest {
 
 	
 	@Test
 	public void testLoad() {
 		 ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("spring-integration-context.xml");
 		
 		 try {
 			 EventDrivenConsumer enricher = context.getBean("rfc-spec-enricher", EventDrivenConsumer.class);
 		 
 			 System.out.printf("enricher %s\n", enricher);
 		 } finally {
 			 context.close();
 		 }
 	}
 	
 	@Test
 	public void testEnrich() throws InterruptedException {
 		 ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("enricher-test-integration-context.xml");
 		
 		 try {
 			 EventDrivenConsumer enricher = context.getBean("rfc-spec-enricher", EventDrivenConsumer.class);
		 			 
 			 System.out.printf("enricher %s\n", enricher);
 			 
 			 Thread.sleep(10 * 1000);
 		 } finally {
 			 context.close();
 		 }
 	}
 
 	@Test
 	public void testJPA() throws InterruptedException {
 		 ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("jpa-context.xml");
 		
 		 try {
 			 ArchiveConfigManager acm = context.getBean("archive-manager", ArchiveConfigManager.class);
 		 
 			 List<ArchiveConfig> ll = acm.all();
 			 
 			 for (ArchiveConfig a : ll) {
				 System.out.printf("ArchiveConfig %d: rfc %d, input_dir %s, file_regex %s\n", 
						 a.getId(), a.getRfcCode(), a.getInputDir(), a.getFileRegex());
 			 }
			 
			 assertFalse("",ll.isEmpty());
 		 } finally {
 			 context.close();
 		 }
 	}
 
 }
