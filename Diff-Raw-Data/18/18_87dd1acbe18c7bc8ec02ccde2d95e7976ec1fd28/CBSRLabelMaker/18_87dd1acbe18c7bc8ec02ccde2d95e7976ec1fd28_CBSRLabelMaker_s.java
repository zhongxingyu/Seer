 package edu.ualberta.med.biobank.barcodegenerator.template.presets.cbsr;
 
 import java.awt.Font;
 import java.io.ByteArrayInputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.JAXBException;
 
 import edu.ualberta.med.biobank.barcodegenerator.template.Template;
 import edu.ualberta.med.biobank.barcodegenerator.template.configuration.Configuration;
 import edu.ualberta.med.biobank.barcodegenerator.template.configuration.Rectangle;
 import edu.ualberta.med.biobank.barcodegenerator.template.jasper.JasperFiller;
 import edu.ualberta.med.biobank.barcodegenerator.template.jasper.JasperOutline;
 import edu.ualberta.med.biobank.barcodegenerator.template.jasper.containers.BarcodeImage;
 import edu.ualberta.med.biobank.barcodegenerator.template.jasper.containers.PatientInfo;
 import edu.ualberta.med.biobank.barcodegenerator.template.jasper.element.FieldGenerator;
 import edu.ualberta.med.biobank.barcodegenerator.template.jasper.element.barcodes.Barcode1D;
 import edu.ualberta.med.biobank.barcodegenerator.template.jasper.element.barcodes.Barcode2D;
 import edu.ualberta.med.biobank.barcodegenerator.template.jasper.element.text.Text;
 import edu.ualberta.med.biobank.barcodegenerator.template.jasper.exceptions.ElementCreationException;
 import edu.ualberta.med.biobank.barcodegenerator.template.jasper.exceptions.JasperFillException;
 import edu.ualberta.med.biobank.barcodegenerator.template.presets.cbsr.exceptions.CBSRPdfGenException;
 
 /**
  * 
  * Used for setting up the default configuration key-value settings. Creating a
  * jasper outline from a specific jasper-file layout. This class also has
  * supporting functions to create a pdf and print to printer. Essientially it
  * provides the relationship between the gui and the jasper filler.
  * 
  * 
  * @author Thomas Polasek 2011
  * 
  */
 public class CBSRLabelMaker {
 
     private static final int BARCODE_COUNT = 32;
 
    private static final long serialVersionUID = -6346822010546940605L;

     /**
      * Generates a jasper outline and creates a pdf file byte array.
      * 
      * @param cbsrData
      * @param barcodeStrings
      * @return
      * @throws CBSRPdfGenException
      * @throws JAXBException
      */
     public static byte[] generatePdfCBSR(CBSRData cbsrData,
         List<String> barcodeStrings) throws CBSRPdfGenException, JAXBException {
 
         Configuration configDataStr = cbsrData.template.getConfiguration();
 
         if (!verifyConfiguration(configDataStr)) {
             throw new CBSRPdfGenException(
                 "Configuration data is invalid. Template is corrupt.");
         }
 
         JasperOutline jo = generateJasperOutline(cbsrData, barcodeStrings);
 
         byte[] pdfData = null;
         try {
             JasperFiller tm = new JasperFiller(jo);
             pdfData = tm.generatePdfData();
         } catch (JasperFillException e) {
             throw new CBSRPdfGenException(
                 "Failed to fill configuration data into jasper template.\n"
                     + e.getError());
         }
         return pdfData;
     }
 
     /**
      * Generates a jasper outline and prints to a specified printer.
      * 
      * @param cbsrData
      * @param barcodeStrings
      * @throws CBSRPdfGenException
      */
     public static void printLabelsCBSR(CBSRData cbsrData,
         List<String> barcodeStrings) throws CBSRPdfGenException {
 
         JasperOutline jo = generateJasperOutline(cbsrData, barcodeStrings);
 
         try {
             JasperFiller tm = new JasperFiller(jo);
             tm.printJasperToPrinter(cbsrData.printerNameStr);
         } catch (JasperFillException e) {
 
             throw new CBSRPdfGenException(
                 "Failed to fill configuration data into jasper template for prining.\n"
                     + e.getError());
         }
     }
 
     private static JasperOutline generateJasperOutline(CBSRData cbsrData,
         List<String> barcodeStrings) throws CBSRPdfGenException {
 
         Template tplt = cbsrData.template;
 
         if (cbsrData.projectTileStr == null) {
             throw new CBSRPdfGenException("Cannot have a null project title");
         }
 
         if (barcodeStrings == null || barcodeStrings.size() == 0) {
             throw new CBSRPdfGenException(
                 "Require a valid amount of barcode strings");
         }
 
         // -------branding------------a
         JasperOutline.Branding branding = new JasperOutline.Branding(
             cbsrData.projectTileStr, cbsrData.logoStream);
 
         Font baseFont = new Font(cbsrData.fontName, Font.PLAIN, 22);
 
         // Point position, Dimension size, String message,String label, String
         // value, Font font, boolean printBarcode
         // -------patient info------------
         PatientInfo patientInfo = new PatientInfo();
 
         try {
             patientInfo.getElements().addAll(
                 FieldGenerator.generateElements(
                     tplt.getKey("Patient Info.Top Field.Field Text"),
                     cbsrData.label1Str, cbsrData.value1Str,
                     baseFont.deriveFont(23),
                     tplt.getKey("Patient Info.Top Field.1D Barcode"),
                     cbsrData.barcode1Print));
 
             patientInfo.getElements().addAll(
                 FieldGenerator.generateElements(
                     tplt.getKey("Patient Info.Middle Field.Field Text"),
                     cbsrData.label2Str, cbsrData.value2Str,
                     baseFont.deriveFont(23),
                     tplt.getKey("Patient Info.Middle Field.1D Barcode"),
                     cbsrData.barcode2Print));
 
             patientInfo.getElements().addAll(
                 FieldGenerator.generateElements(
                     tplt.getKey("Patient Info.Bottom Field.Field Text"),
                     cbsrData.label3Str, cbsrData.value3Str,
                     baseFont.deriveFont(23),
                     tplt.getKey("Patient Info.Bottom Field.1D Barcode"),
                     cbsrData.barcode3Print));
 
             patientInfo.getElements().add(
                 new Barcode1D(
                     tplt.getKey("Patient Info.Patient ID.1D Barcode"),
                     cbsrData.patientIdStr, baseFont.deriveFont(22)));
 
         } catch (ElementCreationException eee) {
             throw new CBSRPdfGenException(
                 "Failed to create element in patient info box : "
                     + eee.getError());
         } catch (JAXBException ee) {
             throw new CBSRPdfGenException(
                 "Failed to load configuration setting " + ee.getMessage());
         }
         // -------barcode info------------
         JasperOutline.PatientBarcodeInformation pbi = new JasperOutline.PatientBarcodeInformation();
 
         try {
             int i = 0;
             for (String rStrArray : barcodeStrings) {
 
                 BarcodeImage bi = new BarcodeImage();
 
                 i++;
                 // 1d barcode
                 if (cbsrData.patientIdStr != null
                     && cbsrData.patientIdStr.length() > 0) {
 
                     Rectangle master = tplt.getKey("Barcodes.All.Barcode 1D");
                     Rectangle barcode = tplt.getKey(String.format(
                         "Barcodes.Individual.Barcode %03d.Barcode 1D", i));
 
                     Rectangle r = new Rectangle(master.getX() + barcode.getX(),
                         master.getY() + barcode.getY(), master.getWidth()
                             + barcode.getWidth(), master.getHeight()
                             + barcode.getHeight());
 
                     Barcode1D item1D = new Barcode1D(r, cbsrData.patientIdStr,
                         baseFont.deriveFont(22));
                     bi.getElements().add(item1D);
                 } else {
                     throw new CBSRPdfGenException(
                         "Empty or null barcode string was specified.");
                 }
 
                 // 2d barcode;
                 if (rStrArray != null && rStrArray.length() > 0
                     && rStrArray.replaceAll("[^a-zA-Z0-9 ]", "").length() == 12) {
 
                     Rectangle master = tplt.getKey("Barcodes.All.Barcode 2D");
                     Rectangle barcode = tplt.getKey(String.format(
                         "Barcodes.Individual.Barcode %03d.Barcode 2D", i));
 
                     Rectangle r = new Rectangle(master.getX() + barcode.getX(),
                         master.getY() + barcode.getY(), master.getWidth()
                             + barcode.getWidth(), master.getHeight()
                             + barcode.getHeight());
 
                     Barcode2D item2D = new Barcode2D(r, rStrArray);
                     bi.getElements().add(item2D);
                 } else {
                     throw new CBSRPdfGenException(
                         "Barcode ID must be a 12 character alphanumeric string.");
                 }
 
                 if (cbsrData.specimenTypeStr != null
                     && cbsrData.specimenTypeStr.length() > 0) {
 
                    Rectangle master = tplt.getKey("Barcodes.All.Specimen Text");
 
                     Rectangle barcode = tplt.getKey(String.format(
                         "Barcodes.Individual.Barcode %03d.Specimen Text", i));
 
                     Rectangle rectdim = new Rectangle(master.getX()
                         + barcode.getX(), master.getY() + barcode.getY(),
                         master.getWidth() + barcode.getWidth(),
                         master.getHeight() + barcode.getHeight());
 
                     Text itemText = new Text(rectdim, cbsrData.specimenTypeStr,
                         baseFont.deriveFont(22));
                     bi.getElements().add(itemText);
                 }
                 pbi.getLayout().add(bi);
             }
         } catch (ElementCreationException e2) {
             throw new CBSRPdfGenException(
                 "Failed to create element in PatientBarcodeInformation box : "
                     + e2.getError());
         } catch (JAXBException e1) {
             throw new CBSRPdfGenException(
                 "Failed to load configuration setting " + e1.getMessage());
         }
 
         if (!tplt.jasperTemplateExists()) {
             throw new CBSRPdfGenException("A valid jasper file is required.");
         }
 
         try {
             ByteArrayInputStream inputStream = new ByteArrayInputStream(tplt
                 .getJasperTemplateXML().getBytes());
 
             JasperOutline jo = new JasperOutline();
             jo.setOutline(branding, patientInfo, pbi, inputStream);
             return jo;
         } catch (Exception e5) {
             throw new CBSRPdfGenException(
                 "Failed to create element in patient info box : "
                     + e5.getMessage());
         }
     }
 
     /**
      * 
      * Set of default configurations. This class is required to be called upon
      * the creation of a new jasper template. This configuration class uses
      * these key values to plot the appropiate tree for editing configuration
      * settings. Please be cautious when changing any key value names. Ensure
      * that all references to these key values are updated.
      * 
      * @return
      */
     public static Configuration getDefaultConfiguration() {
         Configuration config = new Configuration();
 
         config.setSetting("Patient Info.Top Field.Field Text", new Rectangle(1,
             4, 0, 0));
         config.setSetting("Patient Info.Top Field.1D Barcode", new Rectangle(
             38, 1, 29, 8));
         config.setSetting("Patient Info.Middle Field.Field Text",
             new Rectangle(1, 13, 0, 0));
         config.setSetting("Patient Info.Middle Field.1D Barcode",
             new Rectangle(38, 13, 29, 8));
         config.setSetting("Patient Info.Bottom Field.Field Text",
             new Rectangle(1, 25, 0, 0));
         config.setSetting("Patient Info.Bottom Field.1D Barcode",
             new Rectangle(38, 25, 29, 8));
         config.setSetting("Patient Info.Patient ID.1D Barcode", new Rectangle(
             1, 33, 29, 8));
         config
             .setSetting("Barcodes.All.Barcode 1D", new Rectangle(8, 7, 29, 8));
         config
             .setSetting("Barcodes.All.Barcode 2D", new Rectangle(40, 7, 6, 6));
        config
            .setSetting("Barcodes.All.Specimen Text", new Rectangle(8, 2, 0, 0));
 
         for (int i = 1; i <= BARCODE_COUNT; i++) {
             config
                 .setSetting(String.format(
                     "Barcodes.Individual.Barcode %03d.Barcode 1D", i),
                     new Rectangle(0, 0, 0, 0));
             config
                 .setSetting(String.format(
                     "Barcodes.Individual.Barcode %03d.Barcode 2D", i),
                     new Rectangle(0, 0, 0, 0));
             config.setSetting(String.format(
                 "Barcodes.Individual.Barcode %03d.Specimen Text", i),
                 new Rectangle(0, 0, 0, 0));
         }
         return config;
     }
 
     private static List<String> getConfigurationKeyList() {
         String[] configKeyList = new String[] {
             "Patient Info.Top Field.Field Text",
             "Patient Info.Top Field.1D Barcode",
             "Patient Info.Middle Field.Field Text",
             "Patient Info.Middle Field.1D Barcode",
             "Patient Info.Bottom Field.Field Text",
             "Patient Info.Bottom Field.1D Barcode",
             "Patient Info.Patient ID.1D Barcode", "Barcodes.All.Barcode 1D",
             "Barcodes.All.Barcode 2D", "Barcodes.All.Specimen Text" };
 
         List<String> output = new ArrayList<String>();
         for (String ckl : configKeyList)
             output.add(ckl);
 
         for (int i = 1; i <= BARCODE_COUNT; i++) {
             output.add(String.format(
                 "Barcodes.Individual.Barcode %03d.Barcode 1D", i));
             output.add(String.format(
                 "Barcodes.Individual.Barcode %03d.Barcode 2D", i));
             output.add(String.format(
                 "Barcodes.Individual.Barcode %03d.Specimen Text", i));
         }
         return output;
 
     }
 
     private static boolean verifyConfiguration(Configuration c) {
         for (String k : c.getSettings().keySet()) {
             boolean found = false;
 
             for (String ckl : getConfigurationKeyList()) {
                 if (k.equals(ckl)) {
                     found = true;
                     break;
                 }
             }
             if (!found) {
                 System.out.println(k);
                 return false;
             }
         }
         return true;
     }
 
 }
