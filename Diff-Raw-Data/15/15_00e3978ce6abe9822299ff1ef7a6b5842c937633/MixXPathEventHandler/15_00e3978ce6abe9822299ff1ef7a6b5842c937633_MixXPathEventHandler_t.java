 package dk.statsbiblioteket.newspaper.metadatachecker;
 
 import dk.statsbiblioteket.medieplatform.autonomous.Batch;
 import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
 import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
 import dk.statsbiblioteket.util.xml.DOM;
 import dk.statsbiblioteket.util.xml.XPathSelector;
 
 import org.w3c.dom.Document;
 
 import java.io.IOException;
 import java.util.Date;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import static dk.statsbiblioteket.util.Strings.getStackTrace;
 
 /**
  *  Class to handle MIX crosschecks
  *  Checks that:
  *  1. The scanned date is after the date that it was shipped from SB.
  */
 public class MixXPathEventHandler extends DefaultTreeEventHandler {
 
     private ResultCollector resultCollector;
     private MfPakDAO mfPakDAO;
     private Batch batch;
 
     /**
      * Constructor for this class.
      * @param resultCollector the result collector to collect errors in
      * @param mfPakDAO a DAO object from which one can read relevant external properties of a batch.
      * @param batch a batch object representing the batch being analysed.
      */
     public MixXPathEventHandler(ResultCollector resultCollector, MfPakDAO mfPakDAO, Batch batch) {
         this.resultCollector = resultCollector;
         this.mfPakDAO = mfPakDAO;
         this.batch = batch;
     }
 
    /**
     * Hooks on
     * @param event
     */
     @Override
     public void handleAttribute(AttributeParsingEvent event) {
         if (event.getName().endsWith(".mix.xml")) {
             try {
                 doValidate(event);
             } catch (Exception e) {    //Fault Barrier
                 addFailure(event.getName(), "Error processing MIX metadata.", getStackTrace(e));
             }
         }
     }
 
     private void doValidate(AttributeParsingEvent event) {
         XPathSelector xpath = DOM.createXPathSelector("mix", "http://www.loc.gov/mix/v20");
         Document doc;
         try {
             doc = DOM.streamToDOM(event.getData(), true);
             if (doc == null) {
                 addFailure(event.getName(), 
                         "Could not parse xml from " + event.getName(),
                         event.getName());
                 return;
             }
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         
         validateScannedDate(doc, xpath, event);
         validateAgainstFilepath(doc, xpath, event);
 
     }
     
     /**
      * Validates the scanned date is after the batch was sent from SB. 
      */
     private void validateScannedDate(Document doc, XPathSelector xpath, AttributeParsingEvent event) {
         Date shipmentDate = null;
         try {
             shipmentDate = mfPakDAO.getBatchShipmentDate(batch.getBatchID());
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
         final String xpath2K1 = "/mix:mix/mix:ImageCaptureMetadata/mix:GeneralCaptureInformation/mix:dateTimeCreated";
         final String mixDateFormat = "yyyy-MM-dd'T'HH:mm:ss";
         final SimpleDateFormat formatter = new SimpleDateFormat(mixDateFormat);
         String scannedDateInMix = xpath.selectString(doc, xpath2K1);
         if(scannedDateInMix == null) {
             addFailure(event.getName(),
                     "2K-1: Could not find scanned date in MIX file with XPath: '" + xpath2K1 + "'",
                     event.getName());
         }
         
         Date scannedDate = null;
         
         try {
             scannedDate = formatter.parse(scannedDateInMix);
         } catch (ParseException e) {
             addFailure(event.getName(),
                     "2K-1: Could not parse the scanned date '" + scannedDateInMix + "' found in the MIX file."
                             + "Expected the form '" + mixDateFormat + "'.",
                     event.getName());
         }
         
         if(scannedDate.before(shipmentDate)) {
             addFailure(event.getName(),
                     "2K-1: The scanned '" + scannedDate + "' is before "
                             + "the batch was shipped from SB '" + shipmentDate + "'.",
                     event.getName());
         }
     }
     
     /**
      * Validate that the objectIdentifier is of the form [Film-id]-[Billed-id] 
      */
     private void validateAgainstFilepath(Document doc, XPathSelector xpath, AttributeParsingEvent event) {
         final String xpath2K2 = "/mix:mix/mix:BasicDigitalObjectInformation/mix:ObjectIdentifier"
                 + "[mix:objectIdentifierType='Image Unique ID']/mix:objectIdentifierValue";
         
         final String xpath2K3 = "/mix:mix/mix:ImageCaptureMetadata/mix:SourceInformation/mix:SourceID"
                + "[mix:sourceIDType='Microfilm reel barcode #']/mix:sourceIDValue";
         
         final String xpath2K4 = "/mix:mix/mix:ImageCaptureMetadata/mix:SourceInformation/mix:SourceID"
                 + "[mix:sourceIDType='Location on microfilm']/mix:sourceIDValue";
         
         String objectIdentifier = xpath.selectString(doc, xpath2K2);
         String mixFilmID = xpath.selectString(doc, xpath2K3);
         String mixBilledeID = xpath.selectString(doc, xpath2K4);
         String filmID = getFilmIDFromEvent(event);
         String billedID = getBilledIDFromEvent(event);
         String identifierFromPath = filmID + "-" + billedID;
        //TODO here it fails
         if(!objectIdentifier.equals(identifierFromPath)) {
             addFailure(event.getName(),
                     "2K-2: ObjectIdentifier does not match the location in the tree. "
                     + "Expected '" + objectIdentifier + "' got '" + identifierFromPath + "'.",
                     event.getName());
         }
         
         if(!mixFilmID.equals(filmID)) {
             addFailure(event.getName(),
                    "2K-3: FilmID does not match the location in the tree. "
                     + "Expected '" + mixFilmID + "' got '" + filmID + "'.",
                     event.getName());
         }
         
         if(!mixBilledeID.equals(billedID)) {
             addFailure(event.getName(),
                    "2K-4: Location on film does not match the location in the tree. "
                     + "Expected '" + mixBilledeID + "' got '" + billedID + "'.",
                     event.getName());
         }
         
         
         
     }
     
     private String getBilledIDFromEvent(AttributeParsingEvent event) {
         return event.getName().substring(event.getName().lastIndexOf("-")+1).split(".mix.xml")[0];
     }
     
     private String getFilmIDFromEvent(AttributeParsingEvent event) {
         String filmID = null;
         if(event.getName().contains("WORKSHIFT-ISO-TARGET")) {
             /* WORKSHIFT-ISO-TARGET is special in the sense that it does not belong to a film. 
                The 'filmID' here is based on the running number that is in the filename just
                before the 'billedID' */
             String filename = event.getName().substring(event.getName().lastIndexOf("/"));
             filmID = filename.split("-")[1];
         } else {
             /* The event.getName should return something like: /batchID/filmID/dir/pagedir/page.xml
                We want the filmID part, which should be the 2. (index 1) part */
             String[] pathParts = event.getName().split("/");
             filmID = pathParts[1];
         }
         return filmID;
     }
     
     private void addFailure(String reference, String description, String details) {
         resultCollector.addFailure(reference, "metadata", getClass().getName(), description, details);
     }
 
 }
