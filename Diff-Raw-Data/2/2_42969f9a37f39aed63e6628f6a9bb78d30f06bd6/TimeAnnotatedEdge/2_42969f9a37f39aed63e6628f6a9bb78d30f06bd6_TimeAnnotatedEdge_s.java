 package uk.ac.kcl.informatics.opmbuild;
 
 import java.util.Date;
 
 public abstract class TimeAnnotatedEdge extends Edge {
     private Time _time;
 
     public TimeAnnotatedEdge (Node effect, Node cause) {
         super (effect, cause);
     }
 
     public TimeAnnotatedEdge (Node effect, Node cause, Time time) {
         super (effect, cause);
         setTime (time);
     }
 
     public Time getTime () {
         return _time;
     }
 
     public void setTime (Date instant) {
         setTime (new Time (instant));
     }
 
     public void setTime (Date noEarlierThan, Date noLaterThan) {
         setTime (new Time (noEarlierThan, noLaterThan));
     }
 
     public void setTime (Time time) {
        setTime (time);
     }
 }
