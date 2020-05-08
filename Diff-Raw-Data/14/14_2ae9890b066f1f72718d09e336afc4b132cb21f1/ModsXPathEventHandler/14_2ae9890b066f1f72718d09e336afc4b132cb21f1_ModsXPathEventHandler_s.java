 package dk.statsbiblioteket.newspaper.metadatachecker;
 
 import dk.statsbiblioteket.medieplatform.autonomous.Batch;
 import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.FileAttributeParsingEvent;
import dk.statsbiblioteket.newspaper.mfpakintegration.configuration.MfPakConfiguration;
 import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
 import dk.statsbiblioteket.util.xml.DOM;
 import dk.statsbiblioteket.util.xml.XPathSelector;
 import org.w3c.dom.Document;
 
import javax.rmi.CORBA.Util;
 import java.io.IOException;
 import java.sql.SQLException;
 
 import static dk.statsbiblioteket.util.Strings.getStackTrace;
 
 /**
  * This class uses xpath to validate metadata requirements for mods files that do no otherwise fit into the schematron
  * paradigm.
  */
 public class ModsXPathEventHandler extends DefaultTreeEventHandler {
 
     private ResultCollector resultCollector;
     private MfPakDAO mfPakDAO;
     private Batch batch;
 
     /**
      * Constructor for this class.
      * @param resultCollector the result collector to collect errors in
      * @param mfPakDAO a DAO object from which one can read relevant external properties of a batch.
      * @param batch a batch object representing the batch being analysed.
      */
     public ModsXPathEventHandler(ResultCollector resultCollector, MfPakDAO mfPakDAO, Batch batch) {
         this.resultCollector = resultCollector;
         this.mfPakDAO = mfPakDAO;
         this.batch = batch;
     }
 
     @Override
     public void handleAttribute(AttributeParsingEvent event) {
         if (event.getName().endsWith("mods.xml")) {
             try {
                 doValidate(event);
             } catch (Exception e) {    //Fault Barrier
                 resultCollector.addFailure(
                         event.getName(),
                         "metadata",
                         getClass().getName(),
                         "Error processing page-MODS metadata.",
                         getStackTrace(e)
                 );
             }
         }
     }
 
     private void doValidate(AttributeParsingEvent event) {
         XPathSelector xpath = DOM.createXPathSelector("mods", "http://www.loc.gov/mods/v3");
         Document doc;
         try {
             doc = DOM.streamToDOM(event.getData());
             if (doc == null) {
                 resultCollector.addFailure(
                         event.getName(),
                         "metadata",
                         getClass().getName(),
                         "Could not parse xml from " + event.getName(),
                         event.getName()
                 );
                 return;
             }
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         //2C-4
         final String xpath2C4 = "mods:mods/mods:relatedItem[@type='original']/mods:identifier[@type='reel number']";
         String reelNumber = xpath.selectString(doc, xpath2C4);
        String reelNumberPatternString = "^" + batch.getBatchID() + "-" + "[0-9]{2}$";
         if (reelNumber == null || !reelNumber.matches(reelNumberPatternString)) {
               resultCollector.addFailure(event.getName(),
                                     "metadata",
                                     getClass().getName(),
                                    "2C-4: reel number " + reelNumber + " does not match expected pattern " + reelNumberPatternString,
                                     xpath2C4
                                     );
         }
 
         //2C-5
         final String xpath2C5 = "mods:mods/mods:relatedItem[@type='original']/mods:identifier[@type='reel sequence number']";
         String sequenceNumber = xpath.selectString(doc, xpath2C5);
         String namePattern = ".*-[0]*" + sequenceNumber + ".mods.xml";
         if (sequenceNumber == null || !(event.getName().matches(namePattern))) {
             resultCollector.addFailure(event.getName(),
                                     "metadata",
                                     getClass().getName(),
                                     "2C-5: " + sequenceNumber + " not found in file name. Should match " + namePattern + ".",
                                     xpath2C5
                                     );
         }
         //2C-11
         final String xpath2C11 = "mods:mods/mods:relatedItem/mods:titleInfo[@type='uniform' and @authority='Statens Avissamling']/mods:title";
         String avisId = null;
         try {
             avisId = mfPakDAO.getNewspaperID(batch.getBatchID());
             String modsAvisId = xpath.selectString(doc, xpath2C11);
             if (modsAvisId == null || avisId == null || !modsAvisId.equals(avisId)) {
                 resultCollector.addFailure(event.getName(),
                         "metadata",
                         getClass().getName(),
                         "2C-11: avisId mismatch. Document gives " + modsAvisId + " but mfpak gives " + avisId,
                         xpath2C11
                 );
             }
         } catch (SQLException e) {
             resultCollector.addFailure(event.getName(),
                                     "metadata",
                                     getClass().getName(),
                                     "2C-11: Couldn't read avisId from mfpak.",
                                     getStackTrace(e)
                                     );
         }
     }
 }
