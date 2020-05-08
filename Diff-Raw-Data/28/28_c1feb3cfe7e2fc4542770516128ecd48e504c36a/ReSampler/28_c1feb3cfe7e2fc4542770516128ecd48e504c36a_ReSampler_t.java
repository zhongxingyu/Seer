 package plugins.adufour.activecontours;
 
import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.concurrent.Callable;
 
 import plugins.fab.trackmanager.TrackGroup;
 import plugins.fab.trackmanager.TrackSegment;
 
 public class ReSampler implements Callable<Boolean>
 {
     private final TrackGroup             trackGroup;
     
     private final ActiveContour          contour;
     
     private final HashSet<ActiveContour> allContours;
     
     private final HashSet<ActiveContour> evolvingContours;
     
     ReSampler(TrackGroup trackGroup, ActiveContour contour, HashSet<ActiveContour> evolvingContours, HashSet<ActiveContour> allContours)
     {
         this.trackGroup = trackGroup;
         this.contour = contour;
         this.allContours = allContours;
         this.evolvingContours = evolvingContours;
     }
     
     public Boolean call()
     {
         boolean change = false;
         try
         {
             contour.reSample(0.8, 1.4);
         }
         catch (TopologyException e)
         {
             change = true;
             
             // the contour is either dividing or vanishing
             
             // 1) remove it from the list of contours
             
             allContours.remove(contour);
             evolvingContours.remove(contour);
             
             // 2) find the corresponding segment
             
             TrackSegment currentSegment = null;
             
            // this is a thread-safe version of TrackGroup.getTrackSegmentWithDetection(Detection)
            for (TrackSegment segment : new ArrayList<TrackSegment>(trackGroup.getTrackSegmentList()))
             {
                 if (segment.containsDetection(contour))
                 {
                     currentSegment = segment;
                     break;
                 }
             }
             
            if (currentSegment != null)
             {
                currentSegment.removeDetection(contour);
                
                if (currentSegment.getDetectionList().size() == 0)
                {
                    // the current contour is the only detection in this segment
                    // => remove the whole segment
                    trackGroup.removeTrackSegment(currentSegment);
                    currentSegment = null;
                }
             }
             
             // 3) Deal with the children
             
             for (ActiveContour child : e.children)
             {
                 child.setT(contour.getT());
                 allContours.add(child);
                 evolvingContours.add(child);
                 
                 // create the new track segment with the child contour
                 TrackSegment childSegment = new TrackSegment();
                 childSegment.addDetection(child);
                 trackGroup.addTrackSegment(childSegment);
                 
                 if (currentSegment != null) currentSegment.addNext(childSegment);
             }
         }
        
         return change;
     }
 }
