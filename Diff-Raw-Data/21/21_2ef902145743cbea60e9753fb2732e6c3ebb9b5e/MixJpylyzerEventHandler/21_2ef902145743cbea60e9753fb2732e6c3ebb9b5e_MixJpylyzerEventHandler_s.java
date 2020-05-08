 package dk.statsbiblioteket.newspaper.metadatachecker.mix;
 
 import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
 import dk.statsbiblioteket.newspaper.metadatachecker.checker.FailureType;
 import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
 import org.w3c.dom.Document;
 
 /**
  * This class validates the mix information vs. the jpylyzer information
  */
 public class MixJpylyzerEventHandler extends XmlAttributeChecker {

    private boolean foundMix = false;
     private Integer mixFileSize = null;
     private Integer mixWidth = null;
     private Integer mixHeight = null;
 
 
     /**
      * Constructor for this class.
      *
      * @param resultCollector   the result collector to collect errors in
      *
      *
      */
     public MixJpylyzerEventHandler(ResultCollector resultCollector) {
         super(resultCollector, FailureType.METADATA);
     }
 
     protected void handleJpylyzer(AttributeParsingEvent event, Document doc) {
 
 
         String jpylyzerFileSizeXpath = "/jpylyzer/fileInfo/fileSizeInBytes";
         Integer jpylyzerFileSize = XPATH.selectInteger(doc, jpylyzerFileSizeXpath);
         if (!jpylyzerFileSize.equals(mixFileSize)) {
             addFailure(
                     event,
                     "2K-2: The file size from jpylyzer does not match what is reported in the mix file"
                     );
         }
 
         String jpylyzerHeightXpath = "/jpylyzer/properties/jp2HeaderBox/imageHeaderBox/height";
         Integer jpylyzerHeight = XPATH.selectInteger(doc, jpylyzerHeightXpath);
         if (!jpylyzerHeight.equals(mixHeight)) {
             addFailure(
                     event,
                     "2K-10: The picture height from jpylyzer does not match what is reported in the mix file"
                     );
         }
 
         String jpylyzerWidthXpath = "/jpylyzer/properties/jp2HeaderBox/imageHeaderBox/width";
         Integer jpylyzerWidth = XPATH.selectInteger(doc, jpylyzerWidthXpath);
         if (!jpylyzerWidth.equals(mixWidth)) {
             addFailure(
                     event,
                     "2K-9: The picture width from jpylyzer does not match what is reported in the mix file"
                     );
         }
        foundMix = false;
 
 
     }
 
     protected void handleMix(AttributeParsingEvent event, Document doc) {
        if (foundMix) {
            // TODO: Say what?
            // TODO Please output which for which mix file the corresponding jpylyzer file was not found
            addFailure(event, "2K: We found this two mix files without a jpylyzer file");
         }
 
         final String mixFileSizeXpath = "/mix:mix/mix:BasicDigitalObjectInformation/mix:fileSize";
         this.mixFileSize = XPATH.selectInteger(doc, mixFileSizeXpath);
 
 
         String mixWidthXpath = "/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageWidth";
         this.mixWidth = XPATH.selectInteger(doc, mixWidthXpath);
 
         String mixHeightXpath = "/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageHeight";
         this.mixHeight = XPATH.selectInteger(doc, mixHeightXpath);

        foundMix = true;

     }
 
     @Override
     public void validate(AttributeParsingEvent event, Document doc) {
         if (isMix(event)) {
             handleMix(event, doc);
         }
         if (isJpylyzer(event)) {
             handleJpylyzer(event, doc);
         }
     }
 
     private boolean isJpylyzer(AttributeParsingEvent event) {
         return event.getName()
                     .endsWith(".jpylyzer.xml");
     }
 
     private boolean isMix(AttributeParsingEvent event) {
         return event.getName()
                     .endsWith(".mix.xml");
     }
 
     @Override
     public boolean shouldCheckEvent(AttributeParsingEvent event) {
         return isMix(event) || isJpylyzer(event);
     }
 }
