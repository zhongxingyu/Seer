 package com.solidstategroup.radar.util;
 
 import com.solidstategroup.radar.service.TreatmentManager;
 
 import java.util.Date;
 
 /**
 * Radar Utility
  */
 public class RadarUtility {
 
     /**
     *
      * @param event1Start cannot be null
      * @param event2Start cannot be null
      * @return true if there is an overlap
      */
     public static boolean isEventsOverlapping(Date event1Start, Date event1End, Date event2Start, Date event2End) {
         if (event1End == null) {
             if (event2End == null) {
                 if (event1Start.compareTo(event2Start) == 0) {
                     return true;
                 }
             } else {
                 if (event1Start.compareTo(event2Start) >= 0 &&
                         event1Start.compareTo(event2End) < 1) {
                     return true;
                 }
             }
         } else {
             if (event2Start.compareTo(event1Start) >= 0 &&
                     event2Start.compareTo(event1End) < 1) {
                 return true;
             }
             if (event2End != null) {
                 if (event1Start.compareTo(event2Start) >= 0 && event1Start.compareTo(event2End) < 1 ||
                         event1End.compareTo(event2Start) >= 0 && event1End.compareTo(event2End) < 1 ||
                         event2Start.compareTo(event1Start) >= 0 && event2Start.compareTo(event1End) < 1 ||
                         event2End.compareTo(event1Start) >= 0 && event2End.compareTo(event1End) < 1) {
                     return true;
                 }
             }
         }
         return false;
     }
 }
