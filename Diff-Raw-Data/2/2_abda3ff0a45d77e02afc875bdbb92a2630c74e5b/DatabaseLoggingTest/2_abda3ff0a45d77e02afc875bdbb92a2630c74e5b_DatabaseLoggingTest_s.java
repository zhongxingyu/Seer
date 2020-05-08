 package eu.europeana.uim.logging.database;
 
 import org.osgi.service.log.LogEntry;
 
import eu.europeana.uim.api.AbstractLoggingEngineTest;
 import eu.europeana.uim.logging.LoggingEngine;
 
 /**
  * Tests {@link DatabaseLoggingEngine} and {@link LogEntry} implementations used for it.
  * 
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @since Apr 4, 2011
  */
 public class DatabaseLoggingTest extends AbstractLoggingEngineTest {
     @Override
     protected LoggingEngine<Long> getLoggingEngine() {
         DatabaseLoggingEngine<Long> loggingEngine = new DatabaseLoggingEngine<Long>();
         return loggingEngine;
     }
 }
