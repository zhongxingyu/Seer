 package edu.cmu.pandaa.stream;
 
 import edu.cmu.pandaa.header.GeometryHeader;
 import edu.cmu.pandaa.header.StreamHeader;
 import edu.cmu.pandaa.header.GeometryHeader.GeometryFrame;
 import edu.cmu.pandaa.header.StreamHeader.StreamFrame;
 
 import java.io.File;
 
 public class GeometryFileStream extends FileStream {
   private GeometryHeader header;
   boolean useMultipleFiles = false;
 
   public GeometryFileStream(String filename) throws Exception {
     super(filename);
     int dot = filename.lastIndexOf('.');
     useMultipleFiles = new File(filename.substring(0, dot)).isDirectory();
   }
 
   public GeometryFileStream(String filename, boolean overwrite) throws Exception {
     this(filename, overwrite, false);
   }
 
   public GeometryFileStream(String filename, boolean overwrite, boolean multiple) throws Exception {
     super(filename, overwrite);
     useMultipleFiles = multiple;
   }
 
   public void setHeader(StreamHeader h) throws Exception {
     GeometryHeader header = (GeometryHeader) h;
     String tempId="";
     for(int i=0;i<header.deviceIds.length;i++){
       tempId += header.deviceIds[i];
       tempId+=",";
     }
     tempId = tempId.substring(0,tempId.length()-1);
     writeString(tempId + " " + header.startTime + " " + header.frameTime + " " + header.rows + " " + header.cols);
   }
 
   public void sendFrame(StreamFrame f) throws Exception {
     GeometryFrame frame = (GeometryFrame) f;
     if (useMultipleFiles)
       nextFile();
     int rows = frame.geometry == null ? 0 : frame.geometry.length;
     int cols = rows == 0 ? rows : frame.geometry[0].length;
     String msg = "";
     for (int j = 0; j < cols; j++) {
       for (int i = 0;i < rows; i++) {
         double val = frame.geometry[i][j];
         // simple way to keep the numbers reasonable (not too much precision)
         // really ony to make it visually look better...
         val = Math.floor(val*100.0)/100.0;
         msg += " " + val;
       }
       msg += useMultipleFiles ? "\n" : "   ";
     }
     writeString(msg.trim());
   }
 
   public GeometryHeader getHeader() throws Exception {
     String line = readLine();
     String[] parts = line.split(" ");
     String[] deviceIds = parts[0].split(","); //extract the array of deviceIds
     header = new GeometryHeader(deviceIds,
             Long.parseLong(parts[1]),
             Integer.parseInt(parts[2]),
             Integer.parseInt(parts[3]),
             Integer.parseInt(parts[4]));
     return header;
   }
 
   public GeometryFrame recvFrame() throws Exception {
     String line = readLine();
     if (line == null)
       return null;
    String[] parts = line.split(" ");
     int rows = header.rows;
     int cols = header.cols;
    double[][] geometry = new double[rows][cols];
     int pos = 0;
    for (int j = 0; j < cols; j++) {
      for (int i = 0;i < rows;i++) {
        while (pos < parts.length && parts[pos].trim().equals(""))
           pos++;
         geometry[i][j] = Double.parseDouble(parts[pos++]);
       }
     }
     return header.makeFrame(geometry);
   }
 
   public static void main(String args[]) throws Exception {
     String[] dids = { "test" };
     double[][] geometry = { { 1.0 } };
     GeometryHeader gh = new GeometryHeader(dids, 0, 100, 1, 1);
     GeometryFileStream gstream = new GeometryFileStream("gtest1.txt", true, false);
     gstream.setHeader(gh);
     gstream.sendFrame(gh.makeFrame(geometry));
     gstream.close();
     gstream = new GeometryFileStream("gtest2.txt", true, true);
     gstream.setHeader(gh);
     gstream.sendFrame(gh.makeFrame(geometry));
     gstream.close();
 
     gstream = new GeometryFileStream("gtest1.txt");
     if (gstream.useMultipleFiles)
       System.out.println("Should not be using multiple files!");
     gstream.close();
     gstream = new GeometryFileStream("gtest2.txt");
     if (!gstream.useMultipleFiles)
       System.out.println("Should be using multiple files!");
     gstream.close();
   }
 }
