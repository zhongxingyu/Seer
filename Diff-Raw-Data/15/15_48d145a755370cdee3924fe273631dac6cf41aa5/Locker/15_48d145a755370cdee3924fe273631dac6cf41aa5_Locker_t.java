 package gutenberg.workers;
 
 import gutenberg.blocs.EntryType;
 import gutenberg.blocs.ManifestType;
 import gutenberg.blocs.PointType;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.nio.file.DirectoryStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardCopyOption;
 import java.nio.file.StandardOpenOption;
 import java.util.ArrayList;
 import javax.imageio.ImageIO;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 
 import org.apache.commons.codec.binary.Base64;
 import org.scilab.forge.jlatexmath.TeXConstants;
 import org.scilab.forge.jlatexmath.TeXFormula;
 
 
 public class Locker implements ITagLib {
 
     public Locker(Config config) throws Exception {
         scantrayPath = config.getPath(Resource.scantray);
         lockerPath = config.getPath(Resource.locker);
     }
     
     public ManifestType fetchUnresolved(EntryType grader, int max) 
         throws Exception {
         
         ManifestType manifest = new ManifestType();
         manifest.setRoot(scantrayPath.getFileName().toString());
         String gradersExt = "." + grader.getId();
         String undetected = ".md"; //manual detection
         
         DirectoryStream<Path> stream = 
             Files.newDirectoryStream(scantrayPath, "*" + gradersExt);
         for (Path entry: stream) {
             
             if (max == 0) break;
             String filename = entry.getFileName().toString();            
             EntryType scan = new EntryType();
             scan.setId(filename);
             
             manifest.addImage(scan);
             max--;
         }
         
         stream = Files.newDirectoryStream(scantrayPath, "*" + undetected);
         for (Path entry: stream) {
             
             if (max == 0) break;
             String filename = entry.getFileName().toString();
             
             filename = filename.replace(undetected, gradersExt);
             Files.move(entry, entry.resolveSibling(
                 filename.replace(undetected, gradersExt)));
             
             EntryType scan = new EntryType();
             scan.setId(filename);
             
             manifest.addImage(scan);
             max--;
         }        
         
         return manifest;
     }
     
     public ManifestType resolveScan(String source, String target) 
         throws Exception {        
         ManifestType manifest = new ManifestType();
         manifest.setRoot(scantrayPath.getFileName().toString());
         
         Path targetPath = null, sourcePath = scantrayPath.resolve(source);
         if (target != null) {
             targetPath = scantrayPath.resolve(String.format("QR_%s_%s.de", 
                     target, source.split("\\.")[0]));
             Files.move(sourcePath, targetPath);
         } else {
             Files.delete(sourcePath);
         }
         
         return manifest;
     }
 
     public ManifestType uploadSuggestion(String signature, EntryType teacher, 
         String content) throws Exception {
         
         Path teacherDirPath = lockerPath.resolve(
                 String.format("0-%s", teacher.getId()));
         
         String[] tokens = signature.split("\\.");
         String basename = tokens[0];
         String extension = tokens[1];
         
         Path workingDirPath = Files.createDirectories(teacherDirPath.
                 resolve(basename));                
         String original = "suggestion.original";
         String working = "suggestion";
         
         byte[] raw = Base64.decodeBase64(content.getBytes());
         OutputStream ostream = Files.newOutputStream(
                 workingDirPath.resolve(original), 
                 StandardOpenOption.CREATE);
         ostream.write(raw);
         ostream.close();
 
         // {*.doc, *.docx, *.txt} => *.pdf
         if (extension.equals("01")) {
             if (execute(workingDirPath, String.format(libreOfficeCmd, 
                 original)) == 0)
                 //libreoffice forces outputfile extension to be .pdf
                 Files.move(workingDirPath.resolve(working + ".pdf"), 
                     workingDirPath.resolve(working));
             else
                 throw new Exception("libreoffice returned non-zero");
         } else {
             Files.copy(workingDirPath.resolve(original), 
                 workingDirPath.resolve(working));
         }
         
         // {*.pdf, *.tiff, *.png ....} => *.jpg
         execute(workingDirPath, String.format(convertCmd, working));
 
         ManifestType manifest = new ManifestType();
         manifest.setRoot(lockerPath.relativize(workingDirPath).toString());
         
         DirectoryStream<Path> stream = 
                 Files.newDirectoryStream(workingDirPath, "page*");
         for (Path entry: stream) {
             String filename = entry.getFileName().toString();
             EntryType image = new EntryType();
             image.setId(filename);
             manifest.addImage(image);
         }
         stream.close();
         
         Files.delete(workingDirPath.resolve(working));
         return manifest;
     }
         
     public ManifestType annotate(String scanId, PointType[] points) 
         throws Exception {
 
         ManifestType manifest = new ManifestType();
         manifest.setRoot(scanId);
 
         Path imagePath = lockerPath.resolve(scanId);
         File imageFile = imagePath.toFile();
         
         //Create a back up in case we want to revert grading for the page
         Path backupPath = imagePath.resolveSibling("." + imagePath.getFileName());
         if (!Files.exists(backupPath)) Files.copy(imagePath, backupPath);
         
         BufferedImage image = ImageIO.read(imageFile);
         Graphics2D graphics = (Graphics2D) image.getGraphics();
         BasicStroke s = new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_ROUND, 
                 BasicStroke.JOIN_ROUND);
         graphics.setStroke(s);
 
         Icon overlay = null;
         ArrayList<PointType> curve = new ArrayList<PointType>();
         for (PointType point : points) {            
             curve.add(point);
             if (point.isCodeSpecified()) {
                 switch (point.getCode()) {
                 case 0:
                     overlay = new ImageIcon(this.getClass().getClassLoader()
                                 .getResource("META-INF/cross-mark.png"));
                     break;
                 case 1:
                     overlay = new ImageIcon(this.getClass().getClassLoader()
                                 .getResource("META-INF/check-mark.png"));
                     break;
                 case 2:
                     overlay = new ImageIcon(this.getClass().getClassLoader()
                                 .getResource("META-INF/check-mark.png"));
                     break;
                 case 3:
                     TeXFormula formula = new TeXFormula(String.format(TEX, point.getText()));
                     overlay = formula.new TeXIconBuilder().
                             setStyle(TeXConstants.STYLE_TEXT).setSize(FONT_SIZE).build();
                     break;
                 case 4:
                     break;
                 default:
                     throw new Exception("Invalid code " + point.getCode());
                 }
                 
                 int nPoints = curve.size();
                 if (nPoints > 1) {
                     graphics.setColor(new Color(OUTLINE));
                     int[] xPoints = new int[nPoints], yPoints = new int[nPoints];
                     for (int i = 0; i < nPoints; i++) {                    
                         xPoints[i] = curve.get(i).getX();
                         yPoints[i] = curve.get(i).getY();                    
                     }
                     graphics.drawPolyline(xPoints, yPoints, nPoints);
                 } else if (overlay != null){
                     overlay.paintIcon(null, graphics, point.getX()-10, point.getY());
                     overlay = null;
                 } 
                 curve.clear();
             }
         }
         
         ImageIO.write(image, FORMAT, imageFile);
         return manifest;
     }
     
     public ManifestType undoAnnotate(String scanId) throws Exception {
 
         ManifestType manifest = new ManifestType();
         manifest.setRoot(lockerPath.toString());
 
         Path imagePath = lockerPath.resolve(scanId);
         Path backupPath = imagePath.getParent().
                 resolve("." + imagePath.getFileName());
         if (Files.exists(backupPath)) {
             
             Files.copy(backupPath, imagePath, StandardCopyOption.REPLACE_EXISTING);
             
             EntryType image = new EntryType() ;
             String value = scanId;
 
             image.setId(scanId);
             image.setValue(value) ; 
             manifest.addImage(image);
         } else {
             throw new Exception(String.format("%s not found", scanId));
         }
             
         // workaround for de-serialization bug in Savon
         // https://github.com/rubiii/savon/issues/11 (supposedly fixed!)
         if (manifest.getImage() != null && manifest.getImage().length == 1) {
             EntryType dummyEntry = new EntryType();
             dummyEntry.setId("SAVON_BUG_SKIP");
             manifest.addImage(dummyEntry);
         }
 
         return manifest;
     }    
     
     public ManifestType rotate(String scanId) throws Exception {
 
         ManifestType manifest = new ManifestType();
         manifest.setRoot(lockerPath.toString());
 
         Path imageFile = lockerPath.resolve(scanId);
         if (Files.exists(imageFile)) {
             execute(lockerPath, String.format(rotateCmd, imageFile.toString()));
                         
             EntryType image = new EntryType() ;
             String value = imageFile.getFileName().toString(); 
 
             image.setId(scanId);
             image.setValue(value) ; 
             manifest.addImage(image);
         } else {
             throw new Exception(String.format("%s not found", scanId));
         }
             
         // workaround for de-serialization bug in Savon
         // https://github.com/rubiii/savon/issues/11 (supposedly fixed!)
         if (manifest.getImage() != null && manifest.getImage().length == 1) {
             EntryType dummyEntry = new EntryType();
             dummyEntry.setId("SAVON_BUG_SKIP");
             manifest.addImage(dummyEntry);
         }
 
         return manifest;
     }
 
     private int execute(Path workingDirPath, String command) throws Exception {
         
         String[] tokens = command.split(" ");
         System.out.print("[Locker]: execute");
         for (String token : tokens) {
             System.out.print(" " + token);
         }
         System.out.println();
         
         ProcessBuilder pb = new ProcessBuilder();
         pb.command(tokens);
 
         pb.directory(workingDirPath.toFile());
         pb.redirectErrorStream(true);
 
         Process build = pb.start();
         BufferedReader messages = new 
                 BufferedReader(new InputStreamReader(build.getInputStream()));
 
         String line = null;
         while ((line = messages.readLine()) != null) {
             System.out.println(line);
         }
         return build.waitFor();
     }
 
     private Path lockerPath, scantrayPath;
     private final String libreOfficeCmd = "libreoffice --headless --convert-to pdf %s";
     private final String convertCmd = "convert %s -resize 600x800 -scene 1 jpg:page-%%01d.jpeg";
     private final String rotateCmd = "convert %1$s -rotate 180 -type TrueColor %1$s";
     private final float  STROKE_WIDTH = 3f;
     private final String FORMAT    = "JPG";
     private final int    FONT_SIZE = 13;
     private final int    OUTLINE = 0xf6bd13;
     private final String TEX = "\\textcolor{blue}\\textsf{%s}";
     
 }
 
