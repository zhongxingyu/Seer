 package gpxsplitter;
 
 import java.io.File;
 import java.io.IOException;
 import org.jdom.Document;
 import org.jdom.Element;
 
 /**
  *
  * @author anc6
  */
 public class GpxTrackFileBuilder extends GpxFileBuilder
 {
 
     public GpxTrackFileBuilder(Gpx gpx)
     {
         super(gpx);
     }
 
     /**
      * This method will build a set of GPX files given the file name, number of
      * GPX instructions per file and the number of files to be built.
      * TODO: split this method so it returns a testable gpx document.
      * TODO: do we need to pass in the num of instructions
      * @param file
      * @param instNum
      * @param filesNum
      * @throws IOException
      */
     public void build(File file, int totalInstructionsNum, int filesNum) throws IOException
     {
         int fileNum = 1;
         int instrNum = 1;
         while (fileNum <= filesNum)
         {
             Document newGpxDocument = createNewGpx();
             //RteType newRte = newGpxDocument.getRootElement();
 
            Element rteSeg = new Element("trkseg");
             for(WayPoint wpt : gpx.getIntructions())
             {
                Element rtePt = new Element("trkpt");
                rtePt.setAttribute("lat", wpt.getLatitude()+"");
                rtePt.setAttribute("lon", wpt.getLongitude()+"");
                 Element ele = new Element("ele");
                 ele.setText(wpt.getElement());
                rtePt.setContent(ele);
                rteSeg.setContent(rtePt);
             }
            newGpxDocument.setContent(rteSeg);
 
             saveFile(new File(file + "-" + fileNum + GPX_FORMAT), newGpxDocument);
             instrNum--; //The second route will start with the last wpt of the first
             fileNum++; // Proceed to next file
         }
 
     }
 }
