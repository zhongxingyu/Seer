 package dit.panels;
 
 import dit.DEIDGUI;
 import dit.DefaceTask;
 import dit.DefaceTaskinWindows;
 import dit.DeidData;
 import dit.DemographicTableModel;
 import dit.FileUtils;
 import dit.IDefaceTask;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.util.Map.Entry;
 import java.util.*;
 import javax.imageio.ImageIO;
 import niftijlib.Nifti1Dataset;
 import org.dcm4che2.data.DicomElement;
 import org.dcm4che2.data.DicomObject;
 import org.dcm4che2.io.DicomInputStream;
 import org.dcm4che2.io.DicomOutputStream;
 import org.dcm4che2.util.StringUtils;
 import org.dcm4che2.util.TagUtils;
 
 /**
  *
  * @author christianprescott & angelo
  */
 public class DeidentifyProgressPanel extends javax.swing.JPanel implements WizardPanel {
     
     private String outputPath = DeidData.outputPath;
     private boolean doDeface;
     
     /**
      * Creates new form DeidentifyProgressPanel
      */
     public DeidentifyProgressPanel(boolean doDeface) {
         initComponents();
         DEIDGUI.helpButton.setEnabled(false);
         DEIDGUI.continueButton.setEnabled(false);
         DEIDGUI.backButton.setEnabled(false);
         this.doDeface = doDeface;
         DeidData.IdTable = new Hashtable<String, String>();
         DEIDGUI.log("DeidentifyProgressPanel initialized");
         startDeidentification();
     }
     
     private void startDeidentification() {
         new Thread(new Runnable() {
             
             @Override
             public void run() {
                 randomizeIds();
                 DeidData.deidentifiedFiles.clear();
                 if (doDeface) {
                     jLabel2.setText("<html><p>Defacing images...</p><p>&nbsp;</p></html>");
                     defaceImages();
                 } else {
                     // Set all images to their un-defaced sources
                     DeidData.deidentifiedFiles.addAll(DeidData.niftiFiles);
                 }
                 jLabel2.setText("<html><p>Deidentifying demographic file...</p><p>&nbsp;</p></html>");
                 if(DeidData.demographicData != DemographicTableModel.dummyModel)
                     createDemographicFile();
                 if(DeidData.NiftiConversionSourceTable.size() > 0){
                     jLabel2.setText("<html><p>Deidentifying DICOM header data...</p><p>&nbsp;</p></html>");
                     createHeaderDataFiles();
                 }
                 /*  if (doDeface){
                  * jLabel2.setText("<html><p>Creating image montage...</p><p>&nbsp;</p></html>");
                  * createMontage();
                  * }*/
                 
                 DEIDGUI.advance();
             }
         }).start();
     }
     
     /* Dr. Eckert's Image ID Cipher
      * image id = <site initials as numeric string>_<name initials as numeric string>_<random 4 digits>
      * i.e. 1234_5678_2301 or 1234_5678_4598
      * Utilizes an alphabet with consonants swapped around vowels and then the
      * transposed alphabet is numbered 01-26, and then used with the users site
      * and initials information */
     private final char[] CipherAlphabet = "bacfedgjihklmponqrsvutwxyz".toCharArray();
     private void randomizeIds() {
         // Only randomize if the ID column is selected
         
         
         // Build the base ID using the cipher
         String baseId = "";
         String[] institutionBits = DeidData.UserInstitution.split("\\s");
         for (String s : institutionBits){
             if(s.length() > 0){
                 // The alphabet is not sorted, so a search will not work.
                 char firstLetter = s.toLowerCase().charAt(0);
                 for(int ndx = 0; ndx < CipherAlphabet.length; ndx++){
                     if(CipherAlphabet[ndx] == firstLetter){
                         baseId += String.format("%02d", ndx);
                     }
                 }
             }
         }
         baseId += "_";
         String[] names = DeidData.UserFullName.split("\\s");
         for (String s : names){
             if(s.length() > 0){
                 // The alphabet is not sorted, so a search will not work.
                 char firstLetter = s.toLowerCase().charAt(0);
                 for(int ndx = 0; ndx < CipherAlphabet.length; ndx++){
                     if(CipherAlphabet[ndx] == firstLetter){
                         baseId += String.format("%02d", ndx);
                     }
                 }
             }
         }
         baseId += "_";
         
         if(DeidData.demographicData == DemographicTableModel.dummyModel)
         {
             for(File file: DeidData.inputFiles)
             {
                 String original=file.getName();
                 String newId=(System.currentTimeMillis()/1000)%10000+"";
                 DeidData.IdTable.put(original, newId);
             }            
         }
         else
         {
             String idIdentifier = "Filename and "
                     + DeidData.demographicData.getColumnName(DeidData.IdColumn);
             String[] omissions = DeidData.selectedIdentifyingFields;
             Arrays.sort(omissions);
             boolean randomizeFilename = (Arrays.binarySearch(
                     omissions, idIdentifier) >= 0);
             
             
             Object[] idCol = DeidData.demographicData.getColumn(DeidData.IdColumn);
             String[] ids = Arrays.copyOf(idCol, idCol.length, String[].class);
             for (int ndx = 0; ndx < ids.length; ndx++) {
                 String original = ids[ndx];
                 String newId = "";
                 // int countID = 0;
                 if (randomizeFilename) {
                     boolean IdCreated = false;
                     while (IdCreated == false) {
                         newId = baseId;
                         for (int strNdx = 0; strNdx < 4; strNdx++) {
                             newId += Integer.toString(new Random().nextInt(9));
                         }
                         // Iterator   it   =   DeidData.IdFilename.entrySet().iterator();
                         
                         // while(it.hasNext()){
                         //     Map.Entry   entry   =   (Map.Entry)   it.next();
                         //    if (entry.getValue() == original) countID++;
                         // }
                         // Ensure IDs are unique (at least to this dataset). It could happen!
                         if (!DeidData.IdTable.containsValue(newId)) {
                             IdCreated = true;
                         }
                     }
                 } else {
                     newId = original;
                 }
                 //System.out.println(original+"##"+newId);
                 /* String tmpnewId = newId;
                  * String tmporiginal = original;
                  * for(int i = 1; i <= countID; i++)
                  * {
                  * if (countID > 1)
                  * {
                  *
                  * tmpnewId += "_" + Integer.toString(i);
                  * tmporiginal += "_" + Integer.toString(i);
                  * }
                  * DeidData.IdTable.put(tmporiginal, tmpnewId);
                  * tmpnewId = newId;
                  * tmporiginal = original;
                  * }*/
                 DeidData.IdTable.put(original, newId);
                 //System.out.println(DeidData.IdTable.get(original));
             }
         }
         DEIDGUI.log("Randomized file IDs");
     }
     
     private void defaceImages() {
         Iterator<File> it = DeidData.niftiFiles.iterator();
         
         try {
             IDefaceTask defaceTask = null;
             if(FileUtils.OS.isWindows())
             {
                 System.out.println("Windows Deface:");
                 defaceTask=new DefaceTaskinWindows();
             }
             else
             {
                 defaceTask=new DefaceTask();
             }
             defaceTask.setProgressBar(jProgressBar1);
             defaceTask.setTextfield(txtDetail);
             while (it.hasNext()) {
                 File curImage = it.next();
                 defaceTask.addInputImage(curImage);
             }
             
             synchronized (defaceTask) {
                 new Thread((Runnable) defaceTask).start();
                 try {
                     defaceTask.wait();
                 } catch (InterruptedException ex) {
                     DEIDGUI.log("bet was interrupted, the defacing result may "
                             + "be incorrect", DEIDGUI.LOG_LEVEL.WARNING);
                 }
             }
             
             DEIDGUI.log("Defaced images");
         } catch (RuntimeException e) {
             e.printStackTrace();
             DEIDGUI.log("Defacing couldn't be started: " + e.getMessage(),
                     DEIDGUI.LOG_LEVEL.ERROR);
         }
     }
     
     private void createDemographicFile() {
         String[] headings = DeidData.demographicData.getDataFieldNames();
         String[] omissions = DeidData.selectedIdentifyingFields;
         Arrays.sort(omissions);
         boolean[] omit = new boolean[headings.length];
         
         // Find set of identifying columns to omit
         int omitCount = 0;
         for (int ndx = 0; ndx < headings.length; ndx++) {
             String fieldName = headings[ndx];
             if(ndx == DeidData.IdColumn){
                 fieldName = "Filename and "
                         + DeidData.demographicData.getColumnName(DeidData.IdColumn);
             }
             if (Arrays.binarySearch(omissions, fieldName) >= 0) {
                 omit[ndx] = true;
                 omitCount++;
             } else {
                 omit[ndx] = false;
             }
         }
         
         if (omitCount != DeidData.selectedIdentifyingFields.length) {
             DEIDGUI.log("Some identifying fields weren't found (" + omitCount
                     + "/" + DeidData.selectedIdentifyingFields.length + "). "
                     + "The demographic data may not be deidentified properly",
                     DEIDGUI.LOG_LEVEL.WARNING);
         }
         
         File newDemoFile = new File(outputPath + "Demographics_Behavioral.txt");
         BufferedWriter writer = null;
         try {
             newDemoFile.createNewFile();
             writer = new BufferedWriter(new FileWriter(newDemoFile, false));
             
             // Write headings
             for (int colNdx = 0; colNdx < headings.length; colNdx++) {
                 if (!omit[colNdx] || colNdx == DeidData.IdColumn) {
                     // The ID is a special case - heading must be included even
                     // when omitted (in randomized form)
                     writer.write(headings[colNdx] + "\t");
                 }
             }
             writer.newLine();
             for (int ndx = 0; ndx < DeidData.demographicData.getRowCount(); ndx++) {
                 Object[] row = DeidData.demographicData.getRow(ndx);
                 String[] rowS = Arrays.copyOf(row, row.length, String[].class);
                 for (int colNdx = 0; colNdx < row.length; colNdx++) {
                     if (!omit[colNdx]) {
                         writer.write(rowS[colNdx] + "\t");
                     } else if (colNdx == DeidData.IdColumn) {
                         // The ID is a special case - it must be included in its
                         // randomized form.
                         writer.write(DeidData.IdTable.get(rowS[colNdx]) + "\t");
                     }
                 }
                 writer.newLine();
             }
         } catch (IOException ex) {
             DEIDGUI.log("Couldn't write deidentified demographic file: " +
                     ex.getMessage(), DEIDGUI.LOG_LEVEL.WARNING);
         } finally {
             if (writer != null) {
                 try {
                     writer.close();
                 } catch (IOException ex) {
                 }
             }
         }
         DeidData.deidentifiedDemoFile = newDemoFile;
         
         DEIDGUI.log("Created anonymized demographic file");
     }
     
     private void createHeaderDataFiles() {
         Iterator<Entry<File, File>> it = DeidData.NiftiConversionSourceTable.entrySet().iterator();
         String newline = System.getProperty("line.separator");
         DEIDGUI.log("Creating " + DeidData.NiftiConversionSourceTable.size() + " header data files");
         while (it.hasNext()) {
             File hdrFile = null;
             try {
                 Entry<File, File> curSet = it.next();
                 // Create new file or overwrite existing
                 String imageName = FileUtils.getName(curSet.getKey());
                 if (DeidData.IdTable.containsKey(imageName)) {
                     imageName = DeidData.IdTable.get(imageName);
                 }
                 hdrFile = new File(outputPath + imageName + "_hdr_varNames.txt");
                 hdrFile.createNewFile();
                 DeidData.ConvertedDicomHeaderTable.put(curSet.getKey(), hdrFile);
                 BufferedWriter writer = null;
                 String[][] metadata = readDicomMetadata(curSet.getValue(), false);
                 try {
                     writer = new BufferedWriter(new FileWriter(hdrFile, false));
                     for (int ndx = 0; ndx < metadata.length; ndx++) {
                         int tagNdx = Arrays.binarySearch(DeidData.dicomVarIds, metadata[ndx][0]);
                         if (tagNdx >= 0) {
                             String[] element = metadata[ndx];
                             String[] tagHalves = StringUtils.split(element[0], ',');
                             String formattedTag = "\"" + tagHalves[0] + ",\"\t" + tagHalves[1];
                             // element[1] contains the name as read from the
                             // DICOM. The name as provided by Dr. Mark Eckert
                             // is preferred.
                             String name = DeidData.dicomVarNames[tagNdx],
                                     vr = element[2],
                                     value = element[3];
                             String tab = "\t";
                             writer.write(formattedTag + tab + name + tab
                                     + vr + tab + value + newline);
                         }
                     }
                 } catch (IOException e) {
                     DEIDGUI.log("Couldn't write header data to " +
                             hdrFile.getAbsolutePath(), DEIDGUI.LOG_LEVEL.WARNING);
                 } finally {
                     if (writer != null) {
                         try {
                             writer.close();
                         } catch (IOException ex) {
                         }
                     }
                 }
             } catch (IOException ex) {
                 if (hdrFile != null) {
                     DEIDGUI.log("Couldn't write header data to " +
                             hdrFile.getAbsolutePath(), DEIDGUI.LOG_LEVEL.WARNING);
                 }
             }
         }
         DEIDGUI.log("Created header files");
     }
     
     private void createMontage(){
         int imageNdx = 0;
         int imageHeight = 64, imageWidth = 64, textHeight = 12,
                 rowHeight = imageHeight + textHeight;
         
         if(DeidData.deidentifiedFiles.isEmpty()){
             return;
         }
         BufferedImage i = new BufferedImage(imageWidth * 16, rowHeight * DeidData.deidentifiedFiles.size(), BufferedImage.TYPE_INT_RGB);
         for(File image : DeidData.deidentifiedFiles){
             Nifti1Dataset set = new Nifti1Dataset(image.getAbsolutePath());
             if (set.exists()) {
                 try {
                     set.readHeader();
                     
                     // TODO: maybe making assignment to the same double[][][]
                     // instead of creating new ones is more memory efficient?
                     double[][][] data;
                     short[] dims = new short[]{set.ZDIM, set.YDIM, set.XDIM};
                     float calMax = set.cal_max;
                     if(calMax <= 0){
                         calMax = 255f;
                     }
                     float calMin = set.cal_min;
                     if(calMin < 0){
                         calMin = 0;
                     }
                     float calMax2 = set.cal_max;
                     float calMin2 = set.cal_min;
                     float sform = set.sform_code;
                     try {
                         short ttt = 0;
                         data = set.readDoubleVol(ttt);
                         if (calMax2 - calMin2 == 0) {
                             float max = 0;
                             for(int ii = 0; ii< dims[2]; ii++)
                                 for (int j = 0; j < dims[1]; j++)
                                     for (int k = 0; k < dims[0]; k++)
                                     {
                                         if (data[k][j][ii] > max) {
                                             max = (float)data[k][j][ii];
                                         }
                                     }
                             calMax2 = max;
                             calMin2 = 0;
                         }
                         if (sform == 4.0 || sform == 0.0) {
                             for(int x = 0; x < 16; x++){
                                 int realX = x * dims[2] / 16;
                                 for (int y = 0; y < imageWidth; y++) {
                                     int realY = y * dims[1] / imageWidth;
                                     for (int z = 0; z < imageHeight; z++) {
                                         int realZ = z * dims[0] / imageHeight;
                                         float colorFactor = Math.min(((float)data[realZ][realY][realX] - calMin2)/ (calMax2 - calMin2), 1f);
                                         int argb = new Color(colorFactor, colorFactor, colorFactor).getRGB();
                                         i.setRGB(x * imageWidth + y, ((imageNdx + 1) * rowHeight) - 1 - z, argb);
                                     }
                                 }
                             }
                         }
                         else if (sform == 1.0  || sform ==2.0 || sform == 3.0){
                             for(int x = 0; x < 16; x++){
                                 int realX = x * dims[2] / 16;
                                 for (int z = 0; z < imageHeight; z++) {
                                     int realZ = z * dims[0] / imageHeight;
                                     for (int y = 0; y < imageWidth; y++) {
                                         int realY = y * dims[1] / imageWidth;
                                         float colorFactor = Math.min(((float)data[realZ][realY][realX] - calMin2)/ (calMax2 - calMin2), 1f);
                                         int argb = new Color(colorFactor, colorFactor, colorFactor).getRGB();
                                         i.setRGB(x * imageWidth + z, ((imageNdx + 1) * rowHeight) - 1 - y, argb);
                                     }
                                 }
                             }
                             
                             
                         }
                         // Write image name to the image
                         Font f = new Font(Font.MONOSPACED, Font.PLAIN, 12);
                         Graphics2D g = i.createGraphics();
                         g.setColor(Color.WHITE);
                         g.setFont(f);
                         //System.out.println(DeidData.IdTable.get("12"));
                         g.drawString(DeidData.IdTable.get(DeidData.IdFilename.get(FileUtils.getName(image))) + ".nii(" +
                                 DeidData.IdFilename.get(FileUtils.getName(image)) + ")",
                                 0, imageNdx * rowHeight + textHeight);
                         
                         imageNdx++;
                     } catch (IOException ex) {
                         DEIDGUI.log("Unable to render image, data could not be read: "
                                 + ex.getMessage(), DEIDGUI.LOG_LEVEL.ERROR);
                     } catch (OutOfMemoryError ex){
                         DEIDGUI.log("Out of memory, image could not be displayed. "
                                 + "Increase memory available to DeID with the -Xmx "
                                 + "option. -Xmx256m is recommended",
                                 DEIDGUI.LOG_LEVEL.ERROR);
                     }
                 } catch (FileNotFoundException ex) {
                     DEIDGUI.log("Unable to render image, file does not exist: "
                             + ex.getMessage(), DEIDGUI.LOG_LEVEL.ERROR);
                 } catch (IOException ex) {
                     DEIDGUI.log("Unable to render image, file read error: "
                             + ex.getMessage(), DEIDGUI.LOG_LEVEL.ERROR);
                 }
             } else {
                 DEIDGUI.log("No image data found in "+ image.getAbsolutePath(),
                         DEIDGUI.LOG_LEVEL.ERROR);
             }
         }
         try {
             ImageIO.write(i, "jpg", new File(DeidData.outputPath + "montage.jpg"));
         } catch (IOException ex) {
             DEIDGUI.log("Unable to write montage.png",
                     DEIDGUI.LOG_LEVEL.ERROR);
         }
     }
     
     
     /**
      * USE OF DCM4CHE HAS RUNTIME DEPENDENCIES ON log4j, slf4j-api,
      * slf4j-log4j12, and dcm4che-core.
      *
      * @param dicomFile source of metadata
      * @return A two-dimensional String array of metadata elements of
      * the form [tag, name, data type, value]
      */
     private String[][] readDicomMetadata(File dicomFile, boolean anonymizeFile) {
         DicomInputStream dis = null;
         ArrayList<String[]> metadataList = new ArrayList<String[]>();
         try {
             // Open and read the DICOM image
             dis = new DicomInputStream(dicomFile);
             DicomObject dicomObject = dis.readDicomObject();
             
             // Iterate over metadata elements
             Iterator<DicomElement> metadataIt = dicomObject.iterator();
             while (metadataIt.hasNext()) {
                 // Read element data
                 DicomElement elem = metadataIt.next();
                 String elemName = dicomObject.nameOf(elem.tag()),
                         elemTag = TagUtils.toString(elem.tag()),
                         elemVR = dicomObject.vrOf(elem.tag()).toString(),
                         elemValue = "";
                 try {
                     if (dicomObject.vm(elem.tag()) != 1) {
                         String[] elemValues = dicomObject.getStrings(elem.tag());
                         elemValue = StringUtils.join(elemValues, '\\');
                     } else {
                         elemValue = dicomObject.getString(elem.tag());
                     }
                 } catch (UnsupportedOperationException e) {
                     // Only alert if the element is one that we want to keep
                     if (Arrays.binarySearch(DeidData.dicomVarIds, elemTag) >= 0){
                         DEIDGUI.log("Couldn't get value of desired DICOM element "
                                 + elemTag + " \"" + elemName + "\". The image "
                                 + "header file may be incomplete.", DEIDGUI.LOG_LEVEL.WARNING);
                     }
                 }
                 
                 metadataList.add(new String[]{
                     elemTag,
                     elemName,
                     elemVR,
                     elemValue});
                 if(anonymizeFile && Arrays.binarySearch(DeidData.dicomVarIds, elemTag) < 0){
                     // Anonymize other elements
                     dicomObject.remove(elem.tag());
                 }
             }
             
             if (anonymizeFile) {
                 // Save anonymized file
                 // TODO: Save this file in a temporary location - the original
                 // file should be left intact.
                 // TODO: Missing (0002,0010) Transfer Syntax UID, unable to
                 // save file. Find out which data are required to save a DICOM.
                 DicomOutputStream dos = new DicomOutputStream(new File("/Users/christianprescott/Desktop/anon.dcm"));//dicomFile);
                 dos.writeDicomFile(dicomObject);
             }
         } catch (IOException ex) {
             DEIDGUI.log("Couldn't read DICOM object " + dicomFile.getAbsolutePath()
                     + ". The image header file may be incomplete.", DEIDGUI.LOG_LEVEL.WARNING);
         } finally {
             try {
                 if (dis != null) {
                     dis.close();
                 }
             } catch (IOException ex) {
             }
         }
         
         String[][] metadataArray = new String[metadataList.size()][metadataList.get(0).length];
         metadataList.toArray(metadataArray);
         return metadataArray;
     }
     
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel2 = new javax.swing.JLabel();
         jProgressBar1 = new javax.swing.JProgressBar();
         txtDetail = new javax.swing.JTextField();
 
         jLabel2.setText("<html><p>Deidentifying image IDs...</p><p>&nbsp;</p></html>");
 
        txtDetail.setText("This process may takes several minutes.");
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(txtDetail)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, jProgressBar1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                         .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .add(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jProgressBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(18, 18, 18)
                 .add(txtDetail, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 188, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(30, Short.MAX_VALUE))
         );
     }// </editor-fold>//GEN-END:initComponents
     // Variables declaration - do not modify//GEN-BEGIN:variables
     public javax.swing.JLabel jLabel2;
     private javax.swing.JProgressBar jProgressBar1;
     private javax.swing.JTextField txtDetail;
     // End of variables declaration//GEN-END:variables
 
     @Override
     public WizardPanel getNextPanel() {
         return new AuditPanel();
     }
 
     @Override
     public WizardPanel getPreviousPanel() {
          if(DeidData.demographicData != DemographicTableModel.dummyModel)
              return new LoadDemoPanel();
         return new DeIdentifyPanel();
     }
 }
