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
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 
 public class Locker {
 
     public Locker(Config config) throws Exception {
         lockerPath = new File(config.getPath(Resource.locker)).toPath();
     }
 
     /**
      * Creates folder for storing scans related with
      * this test paper or for suggestion scans
     * @return TODO
      */
     public Path makeRoom(String quizId, String testpaperId) throws Exception {
         Path dirPath = lockerPath.resolve(
                 String.format("%s-%s", quizId, testpaperId));
         if (!Files.exists(dirPath)) {
             Files.createDirectory(dirPath);
         }
         return dirPath;
     }
     
     
     public ManifestType save(File[] scans) throws Exception {
 
         ManifestType manifest = new ManifestType();
         manifest.setRoot(lockerPath.toString());
 
         String scanId = null, base36ScanId = null;
         boolean rotate = false;
         String[] tokens = null;
         for (File scan : scans) {
             
             //scanId(base36)_scanId(base10)_orientation
             tokens = scan.getName().split("_");
             base36ScanId = tokens[0];
             scanId = tokens[1];
             rotate = tokens[2].equals("1") ? true : false;
             
             //scanId(base10)=quizId-testpaperId-studentId-pageNo
             //scanId(suggestion)=0-[signature]-teacherId-1
             String[] subTokens = scanId.split("-");            
             boolean  isSuggestion = subTokens[0].equals("0") ;
             String   subpath = null ;
 
             if (isSuggestion) {
                 subpath = String.format("0-%s/%s", subTokens[2],
                         subTokens[1]);
             } else {
                 subpath = String.format("%s-%s/%s", subTokens[0],
                     subTokens[1], base36ScanId);
             }
             
             Path stored = lockerPath.resolve(subpath);            
             if (!Files.exists(stored)) {
                 if (convert(scan.toPath(), stored, SCAN_SIZE, rotate) != 0) {
                     throw new Exception("Error running convert utility on "
                             + scan.getName());
                 }
                 EntryType image = new EntryType() ;
                 String value = stored.getFileName().toString(); 
 
                 image.setId(scanId);
                 image.setValue(value) ; 
                 manifest.addImage(image);
             }
             
             if (!scan.delete()) {
                 throw new Exception("SaveScan error: could not delete"
                         + scan.getName());
             }
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
 
     /**
      * Expects pairs of diagonally opposite points
      * 
      * @param scanId
      * @throws Exception
      */
     public ManifestType annotateRect(String scanId, PointType[] points)
             throws Exception {
 
         ManifestType manifest = new ManifestType();
         manifest.setRoot(scanId);
 
         File imageFile = lockerPath.resolve(scanId).toFile();
         BufferedImage image = ImageIO.read(imageFile);
         Graphics2D graphics = (Graphics2D) image.getGraphics();
         graphics.setStroke(new BasicStroke(1.0f));
         graphics.setColor(new Color(0xffa500));
 
         PointType topLeft = new PointType();
         PointType firstPoint = null;
         for (PointType point : points) {
 
             if (firstPoint != null) {
                 // the pairs of points are diagonally opposite,
                 // all we need to figure out is which one is which
                 if (firstPoint.getX() < point.getX()) {
                     if (firstPoint.getY() < point.getY()) {
                         // firstPoint is topLeft,
                         topLeft = firstPoint;
                     } else {
                         // firstPoint is bottomLeft,
                         topLeft.setX(firstPoint.getX());
                         topLeft.setY(point.getY());
                     }
                 } else {
                     if (firstPoint.getY() < point.getY()) {
                         // firstPoint is topRight
                         topLeft.setX(point.getX());
                         topLeft.setY(firstPoint.getY());
                     } else {
                         // firstPoint is bottomRight
                         topLeft = point;
                     }
                 }
                 graphics.drawRoundRect(topLeft.getX(), topLeft.getY(),
                         Math.abs(firstPoint.getX() - point.getX()),
                         Math.abs(firstPoint.getY() - point.getY()), ARC_SIZE,
                         ARC_SIZE);
                 firstPoint = null;
             } else {
                 firstPoint = point;
             }
         }
 
         ImageIO.write(image, FORMAT, imageFile);
         return manifest;
     }
 
     /**
      * Expects pairs of diagonally opposite points
      * 
      * @param scanId
      * @throws Exception
      */
     public ManifestType annotateLine(String scanId, PointType[] points)
             throws Exception {
 
         ManifestType manifest = new ManifestType();
         manifest.setRoot(scanId);
 
         File imageFile = lockerPath.resolve(scanId).toFile();
         BufferedImage image = ImageIO.read(imageFile);
         Graphics2D graphics = (Graphics2D) image.getGraphics();
         BasicStroke s = new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_ROUND, 
                 BasicStroke.JOIN_ROUND);
         graphics.setStroke(s);
 
         ArrayList<PointType> curve = new ArrayList<PointType>();
         for (PointType point : points) {
             
             curve.add(point);
             if (point.isCodeSpecified()) {
                 switch(point.getCode()) {
                     case 0:
                         graphics.setColor(new Color(WRONG));
                         break;
                     case 1:
                         graphics.setColor(new Color(RIGHT));
                         break;
                     default:
                         graphics.setColor(new Color(WHAT));
                 }
                 
                 int nPoints = curve.size();
                 int[] xPoints = new int[nPoints], yPoints = new int[nPoints];
                 for (int i = 0; i < nPoints; i++) {
                     
                     xPoints[i] = curve.get(i).getX();
                     yPoints[i] = curve.get(i).getY();                    
                 }
                 graphics.drawPolyline(xPoints, yPoints, nPoints);                
                 curve.clear();
             }
         }
 
         ImageIO.write(image, FORMAT, imageFile);
         return manifest;
     }
     
     private int convert(Path src, Path target, String size, boolean rotate)
             throws Exception {
         ProcessBuilder pb = new ProcessBuilder();
         pb.command("convert", src.toString(), "-resize", size, "-type",
                 "TrueColor");
         if (rotate) {
             pb.command().add("-rotate");
             pb.command().add("180");
         }
         pb.command().add(target.toString());
 
         pb.directory(lockerPath.toFile());
         pb.redirectErrorStream(true);
 
         Process process = pb.start();
         BufferedReader messages = new BufferedReader(new InputStreamReader(
                 process.getInputStream()));
         String line = null;
 
         while ((line = messages.readLine()) != null) {
             System.out.println(line);
         }
 
         return process.waitFor();
     }
 
     private Path lockerPath;
 
     private final String SCAN_SIZE = "600x800";
     private final float  STROKE_WIDTH = 3f;
     private final int    ARC_SIZE  = 3;
     private final String FORMAT    = "JPG";
     private final int    RIGHT = 0x4b9630, WRONG = 0xea5115, WHAT = 0xf6bd13;
 }
