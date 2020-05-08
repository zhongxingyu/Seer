 package dk.nsi.sdm4.core.parser;
 
 import dk.nsi.sdm4.core.persistence.recordpersister.RecordPersister;
 import dk.nsi.sdm4.core.status.ImportStatusRepository;
 import dk.sdsd.nsp.slalog.api.SLALogItem;
 import dk.sdsd.nsp.slalog.api.SLALogger;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.io.File;
 
 /**
  * Ansvarlig for at foretage jævnlige kørsler af én importer samt at skaffe den inddata.
  */
 public class ParserExecutor {
 	@Autowired
 	Parser parser;
 
 	@Autowired
 	Inbox inbox;
 
 	@Autowired
 	ImportStatusRepository importStatusRepo;
 
 	@Autowired
 	RecordPersister recordPersister;
 
 	@Autowired
 	private SLALogger slaLogger;
 
 	private static final Logger logger = Logger.getLogger(ParserExecutor.class);
 
 	@Scheduled(fixedDelay = 1000)
 	@Transactional
 	/**
 	 * Metode, der når inbox ikke er låst og indeholder et dataset, kalder sin importer.
 	 * Håndterer SLA-logning for hele importen og etablerer en transaktion, importer kører i.
 	 */
 	public void run() {
 		String parserIdentifier = parser.getHome();
 		SLALogItem slaLogItem = slaLogger.createLogItem("ParserExecutor", "Executing parser " + parserIdentifier);
 
 		try {
 			if (!inbox.isLocked()) {
 				if (logger.isDebugEnabled()) {
 					logger.debug("Running parser " + parser.getHome());
 				}
 
 				inbox.update();
 				File dataSet = inbox.top();
 
 				if (dataSet != null) {
 					recordPersister.resetTransactionTime();
 					importStatusRepo.importStartedAt(new DateTime());
 					parser.process(dataSet);
 
 					// Once the import is complete
					// we can remove the data set
 					// from the inbox.
 					inbox.advance();
 
 
 					slaLogItem.setCallResultOk();
 					slaLogItem.store();
 
 					importStatusRepo.importEndedWithSuccess(new DateTime());
 				} // if there is no data and no error, we never call store on the log item, which is okay
 			} else {
 				if (logger.isDebugEnabled()) {
 					logger.debug(inbox + " for parser " + parser.getHome() + " is locked");
 				}
 			}
 		} catch (Exception e) {
 			try {
 				inbox.lock();
 			} catch (RuntimeException lockExc) {
 				logger.error("Unable to lock " + inbox, lockExc);
 			}
 
 			slaLogItem.setCallResultError("Parser " + parserIdentifier + " failed - Cause: " + e.getMessage());
 			slaLogItem.store();
 
 			importStatusRepo.importEndedWithFailure(new DateTime());
 
 			throw new RuntimeException("runParserOnInbox on parser " + parserIdentifier + " failed", e); // to make sure the transaction rolls back
 		}
 	}
 }
