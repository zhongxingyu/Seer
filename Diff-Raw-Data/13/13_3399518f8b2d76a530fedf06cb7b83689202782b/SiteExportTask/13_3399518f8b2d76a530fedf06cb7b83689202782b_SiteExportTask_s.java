 package eionet.eunis.rdfexporter;
 
 import java.io.ByteArrayOutputStream;
 import java.nio.ByteBuffer;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.log4j.Logger;
 import org.dozer.DozerBeanMapperSingletonWrapper;
 import org.dozer.Mapper;
 import org.simpleframework.xml.core.Persister;
 import org.simpleframework.xml.stream.Format;
 
 import ro.finsiel.eunis.factsheet.sites.SiteFactsheet;
 import eionet.eunis.dto.SiteFactsheetDto;
 
 /**
  * Site export task.
  * 
  * @author Aleksandr Ivanov
  * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
  */
 public class SiteExportTask implements Runnable {
 	private static final Logger logger = Logger.getLogger(SiteExportTask.class);
 	
 	private static AtomicInteger sitesExported = new AtomicInteger();
 	
 	private String idsite;
 	private QueuedFileWriter fileWriter;
 	
 
 	public SiteExportTask(String idsite, QueuedFileWriter fileWriter) {
 		this.idsite = idsite;
 		this.fileWriter = fileWriter;
 	}
 
 	public void run() {
 		SiteFactsheet factsheet = new SiteFactsheet(idsite);
 		if (factsheet.exists()) {
 			Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();
 			SiteFactsheetDto dto = mapper
 					.map(factsheet, SiteFactsheetDto.class);
 			if (dto.getIdDc() != null && !"-1".equals(dto.getIdDc().getId())) {
 				dto.getIdDc().setPrefix("http://eunis.eea.europa.eu/documents/");
 			} else {
 				dto.setIdDc(null);
 			}
			if (dto.getIdDesignation() != null) {
 				dto.getIdDesignation().setPrefix("http://eunis.eea.europa.eu/designations/");
			}
			if (dto.getIdGeoscope() != null) {
				dto.getIdGeoscope().setPrefix("http://eunis.eea.europa.eu/geoscope/");
 			}
 			Persister persister = new Persister(new Format(4));
 			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
 			try {
 				persister.write(dto, buffer, "UTF-8");
 				buffer.write("\n".getBytes("UTF-8"));
 				ByteBuffer writeTask = ByteBuffer.wrap(buffer.toByteArray());
 				buffer.close();
 				
 				fileWriter.addTaskToWrite(writeTask);
 				
 				sitesExported.incrementAndGet();
 				
 				if(logger.isDebugEnabled()) {
 					logger.debug("Exporting site #" + sitesExported.get() + " (ID_SITE = " + idsite + ")" );
 				}
 			} catch (Exception e) {
 				logger.error(e);
 			}
 		} else {
 			logger.error("Site " + idsite + " not found");
 		}
 	}
 	
 	public static int getNumberOfExportedSites() {
 		return sitesExported.get();
 	}
 
 }
