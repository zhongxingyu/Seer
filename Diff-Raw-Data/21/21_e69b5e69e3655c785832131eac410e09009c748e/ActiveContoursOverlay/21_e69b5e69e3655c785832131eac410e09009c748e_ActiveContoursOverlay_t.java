 package plugins.adufour.activecontours;
 
 import icy.canvas.IcyCanvas;
 import icy.painter.Overlay;
 import icy.sequence.Sequence;
 import icy.util.GraphicsUtil;
 
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import plugins.fab.trackmanager.TrackGroup;
 import plugins.fab.trackmanager.TrackSegment;
 import plugins.nchenouard.spot.Detection;
 
 public class ActiveContoursOverlay extends Overlay
 {
     private HashMap<Integer, ArrayList<ActiveContour>> contoursMap;
     
     private TrackGroup                                 trackPool;
     
     public ActiveContoursOverlay(HashMap<Integer, ArrayList<ActiveContour>> contours)
     {
         super("Active contours");
         contoursMap = contours;
     }
     
     public ActiveContoursOverlay(TrackGroup trackPool)
     {
         super("Active contours");
         this.trackPool = trackPool;
     }
     
     @Override
     public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
     {
         int t = canvas.getPositionT();
         
         if (trackPool == null)
         {
             if (contoursMap.containsKey(t)) for (ActiveContour contour : contoursMap.get(t))
                 contour.paint(g, sequence, canvas);
         }
         else
         {
            ArrayList<TrackSegment> segments = new ArrayList<TrackSegment>(trackPool.getTrackSegmentList());
             
            int id = 1;
            for (TrackSegment segment : segments)
             {
                 ArrayList<Detection> detections = segment.getDetectionList();
                
                for (Detection det : detections)
                 {
                     if (det.getT() == canvas.getPositionT())
                     {
                         ((ActiveContour) det).paint(g, sequence, canvas);
                        GraphicsUtil.drawCenteredString(g, "" + id, (int) det.getX(), (int) det.getY(), false);
                     }
                 }
                
                id++;
             }
         }
     }
 }
