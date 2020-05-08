 package dk.statsbiblioteket.newspaper.metadatachecker;
 
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

 import dk.statsbiblioteket.medieplatform.autonomous.Batch;
 import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
 import dk.statsbiblioteket.newspaper.metadatachecker.film.FuzzyDate;
 import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
 import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperDateRange;
 import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperEntity;
 import dk.statsbiblioteket.util.xml.DOM;
 import dk.statsbiblioteket.util.xml.XPathSelector;
 import org.w3c.dom.Document;
 
 /**
  * This class uses xpath to validate metadata requirements for edition xml (mods) files that do no fit into
  * the schematron paradigm.
  */
 public class EditionModsEventHandler extends DefaultTreeEventHandler {
     private static final String YYYY_MM_DD = "yyyy-MM-dd";
     private ResultCollector resultCollector;
     private MfPakDAO mfPakDAO;
     private Batch batch;
 
     /**
      * Constructor for this class.
      *
      * @param resultCollector the result collector to collect errors in
      * @param mfPakDAO        a DAO object from which one can read relevant external properties of a batch.
      * @param batch           a batch object representing the batch being analysed.
      */
     public EditionModsEventHandler(ResultCollector resultCollector, MfPakDAO mfPakDAO, Batch batch) {
         this.resultCollector = resultCollector;
         this.mfPakDAO = mfPakDAO;
         this.batch = batch;
     }
 
     @Override
     public void handleAttribute(AttributeParsingEvent event) {
         if (event.getName()
                  .endsWith("edition.xml")) {
             doValidate(event);
         }
     }
 
     private void doValidate(AttributeParsingEvent event) {
         XPathSelector xpath = DOM.createXPathSelector("mods", "http://www.loc.gov/mods/v3");
         Document doc;
         try {
             doc = DOM.streamToDOM(event.getData(), true);
             if (doc == null) {
                 addFailure(event, "Could not parse xml from " + event.getName());
                 return;
             }
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         try {
 
             NewspaperEntity entity = getNewspaperEntity(event);
             if (entity == null){
                 addFailure(event,"Failed to resolve a newspaper entity in MFPak");
                 return;
             }
             check2D_1(event, xpath, doc, entity);
             check2D_2(event, xpath, doc, entity);
             check2D_3(event, xpath, doc, entity);
             check2D_4(event, xpath, doc, entity);
             check2D_9(event, xpath, doc);
 
         } catch (SQLException e) {
             addFailure(event, "Problem with communication with MFPak");
         }
 
 
     }
 
     private boolean matchDates(NewspaperDateRange newspaperDateRange, FuzzyDate editionDate)  {
         if (editionDate.compareTo(newspaperDateRange.getFromDate()) < 0 ){
             return false;
         }
         if (editionDate.compareTo(newspaperDateRange.getToDate()) > 0){
             return false;
         }
         return true;
     }
 
     /**
      * 2D-9 Check the edition number defined in the edition.xml with the edition node number.
      */
     private void check2D_9(AttributeParsingEvent event, XPathSelector xpath, Document doc) {
 
         final String xpathForEditionNumberXPath =
                 "/mods:mods/mods:relatedItem[@type='host']/mods:part/mods:detail[@type='edition']/mods:number";
 
         String xpathForEditionNumber = xpath.selectString(doc, xpathForEditionNumberXPath);
         String editionIDFromNode = event.getName().split("/")[2];
         // Extracts the edition number from a editionID: yyyy-MM-dd-editionNumber.
         String nodeEditionNumber = editionIDFromNode.split("-")[3];
         try {
             if (Integer.parseInt(xpathForEditionNumber) != Integer.parseInt(nodeEditionNumber) ) {
                 addFailure(event, "2D_9: Edition number (" + xpathForEditionNumber + ") in edition xml doesn't " +
                         "correspond to node edition number: " + editionIDFromNode);
             }
         } catch (NumberFormatException nfe) {
             addFailure(event, "2D_9: Unable to compare (" + xpathForEditionNumber + ") in edition xml " +
                     "to node edition number: " + editionIDFromNode + ", they can't be converted to numbers.");
         }
     }
 
     private NewspaperEntity getNewspaperEntity(AttributeParsingEvent event) throws SQLException {
 
         FuzzyDate editionDate = null;
         try {
             editionDate = getDateFromName(event.getName());
         } catch (ParseException e) {
             addFailure(event, "2D-2: No edition date found in this file");
             return null;
         }
         if (editionDate == null) {
             addFailure(event, "2D-2: No edition date found in this file");
             return null;
         } else {
 
            List<NewspaperEntity> titles = mfPakDAO.getBatchNewspaperEntities(batch.getBatchID());
             NewspaperEntity selected = null;
            for (NewspaperEntity title : titles) {
                    if (matchDates(title.getNewspaperDateRange(),editionDate)){
                    selected = title;
                     break;
                 }
             }
             return selected;
         }
     }
 
 
         /** 2D-4 Check Issue Date against event.getName() (file structure), */
 
     private void check2D_4(AttributeParsingEvent event, XPathSelector xpath, Document doc,
                            NewspaperEntity newspaperEntity) {
         String dateIssuedString = xpath.selectString(doc, "/mods:mods/mods:originInfo/mods:dateIssued");
         try {
             FuzzyDate dateIssued = new FuzzyDate(dateIssuedString);
             if (!dateIssued.equals(getDateFromName(event.getName()))) {
                 addFailure(event, "2D-4: Date issued from file does not correspond to date in filename");
             }
         } catch (ParseException e) {
             addFailure(event, "2D-4: Date issued from file is not of the form '" + YYYY_MM_DD + "'");
 
         }
 
 
     }
 
     private void addFailure(AttributeParsingEvent event, String description) {
         resultCollector.addFailure(
                 event.getName(), "metadata", getClass().getName(), description);
     }
 
     /**
      * 2D-3 Check Publication Location against MFpak
      *
      * @param event           the parsing event
      * @param xpath           the xpath selector
      * @param doc             the edition mods document
      * @param newspaperEntity the mfpak information about this newspaper
      *
      * @throws SQLException if communication with MFPak failed
      */
     private void check2D_3(AttributeParsingEvent event, XPathSelector xpath, Document doc,
                            NewspaperEntity newspaperEntity) throws SQLException {
 
 
         String editionLocation = xpath.selectString(doc, "/mods:mods/mods:originInfo/mods:place/mods:placeTerm");
         if (editionLocation == null) {
             addFailure(event, "2D-3: Failed to resolve publication location");
         }
         if (!newspaperEntity.getPublicationLocation()
                             .equals(editionLocation)) {
 
             addFailure(
                     event,
                     "2D-3: Publication location '" + editionLocation + "' does not match value '" + newspaperEntity.getPublicationLocation() + "' from MFPak");
         }
 
     }
 
     /**
      * 2D-2
      * Check edition date against MFPak.
      * Check that the full title matches the value from MFPak
      */
     private void check2D_2(AttributeParsingEvent event, XPathSelector xpath, Document doc,
                            NewspaperEntity selected) throws SQLException {
 
         String avisTitle = xpath.selectString(doc, "/mods:mods/mods:titleInfo/mods:title");
         if (avisTitle == null) {
             addFailure(event, "2D-2: Title should exist");
         } else {
             if (!avisTitle.equals(selected.getNewspaperTitle())) {
                 addFailure(
                         event,
                         "2D-2: title " + avisTitle + " does not match title in MFPak '" + selected.getNewspaperTitle() + "'");
             }
         }
     }
 
     /**
      * 2D-1
      * Check avisID (The unique ID for the newspaper concerned provided by the State and University Library)
      * mods:mods/mods:titleInfo/mods:title [@type=”uniform” authority=”Statens Avissamling”]
      * matches the ID in the file structure (event.getName()).
      * Check avisID matches the avisID in MFPak.
      */
     private void check2D_1(AttributeParsingEvent event, XPathSelector xpath, Document doc,
                              NewspaperEntity entity) throws SQLException {
         final String xpath2D1
                 = "/mods:mods/mods:titleInfo[@type='uniform'][@authority='Statens Avissamling']/mods:title";
         String avisID = xpath.selectString(doc, xpath2D1);
         if (avisID == null) {
             addFailure(event, "2D-1: No avisID found in this file");
             return;
         }
         String avisIDfromFileStructure = getAvisIDfromName(event.getName());
         String avisIDfromMFPak;
 
         avisIDfromMFPak = entity.getNewspaperID();
 
 
         if (!avisID.equals(avisIDfromFileStructure)) {
             addFailure(
                     event,
                     "2D-1: avisID " + avisID + " does not match avisID in file structure '" + avisIDfromFileStructure + "'");
         }
         if (!avisID.equals(avisIDfromMFPak)) {
             addFailure(event, "2D-1: avisID " + avisID + " does not match avisID in MFPak '" + avisIDfromMFPak + "'");
         }
     }
 
     /**
      * Get the edition date from the name of a edition.xml file
      *
      * @param name the name of the edition xml file
      *
      * @return the edition date
      * @throws ParseException
      */
     private FuzzyDate getDateFromName(String name) throws ParseException {
 
         //B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01.edition.xml
         String[] splits = name.split("/");
         String file = splits[2];
         String date = file.replaceAll("-[0-9]{2}$", "");
         return new FuzzyDate(date);
     }
 
     /**
      * Get the avisID from the edition.xml name
      *
      * @param name the edition.xml file name
      *
      * @return the avisID
      */
     private String getAvisIDfromName(String name) {
         //B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01.edition.xml
         String[] splits = name.split("/");
         String file = splits[splits.length - 1];
         String[] parts = file.split("-");
         return parts[0];
     }
 }
 
 
