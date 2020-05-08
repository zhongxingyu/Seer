 package dk.statsbiblioteket.newspaper.metadatachecker.film;
 
 import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
 import dk.statsbiblioteket.newspaper.metadatachecker.checker.FailureType;
 import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
 import dk.statsbiblioteket.util.xml.XPathSelector;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 /**
  * Checks that the editions contained in a film for a batch structure are inside of the start/end dates specified in the
 * film.xml.
  */
 public class FilmDateVsEditionsChecker extends XmlAttributeChecker {
     private final XPathSelector xPathSelector;
     private final Document batchXmlStructure;
 
     public FilmDateVsEditionsChecker(ResultCollector resultCollector, XPathSelector xPathSelector,
                                      Document batchXmlStructure) {
         super(resultCollector, FailureType.METADATA);
         this.xPathSelector = xPathSelector;
         this.batchXmlStructure = batchXmlStructure;
     }
 
     @Override
     public void validate(AttributeParsingEvent event, Document filmMetaData) {
         String filmStartdate = xPathSelector.selectString(filmMetaData, "/avis:reelMetadata/avis:startDate");
         String filmEnddate = xPathSelector.selectString(filmMetaData, "/avis:reelMetadata/avis:endDate");
         String filmID = event.getName()
                              .split("/")[1].replace(".film.xml", "");
         NodeList editions = xPathSelector.selectNodeList(
                 batchXmlStructure,
                 "/node/node[@shortName='" + filmID + "']/node[@shortName!='FILM-ISO-target'][@shortName!='UNMATCHED']");
         for (int index = 0; index < editions.getLength(); index++) {
             verifyEditionDateContainment(
                     filmStartdate,
                     filmEnddate,
                     editions.item(index)
                             .getAttributes()
                             .getNamedItem("shortName")
                             .getNodeValue(),
                     filmID,
                     event);
         }
     }
 
     /**
      * Contain the logic for checking whether a given edition is inside of the film start/end dates. Also
      * handles the registration of check failures.
      * @param filmStartdate The start date to check against.
      * @param filmEnddate The end date to check against.
      * @param editionID The edition date which should be after (or same) the start date and before the end date.
      * @param filmID For failure information.
      * @param event For failure information.
      */
     protected void verifyEditionDateContainment(String filmStartdate, String filmEnddate, String editionID,
                                                 String filmID, AttributeParsingEvent event) {
         FuzzyDate filmStart = new FuzzyDate(filmStartdate);
         FuzzyDate filmEnd = new FuzzyDate(filmEnddate);
         FuzzyDate editionDate = new FuzzyDate(editionID.substring(0, editionID.lastIndexOf('-')));
         if (filmStart.compareTo(editionDate) > 0) {
             addFailure(
                     event,
                     "2E-2: Edition earlier than film start date " + filmStartdate + " in film " + filmID);
         }
         if (filmEnd.compareTo(editionDate) < 0) {
             addFailure(event, "2E-3: Edition later than film end date " + filmEnddate + " in film " + filmID);
         }
     }
 
     @Override
     public boolean shouldCheckEvent(AttributeParsingEvent event) {
         return event.getName().endsWith(".film.xml");
     }
 }
