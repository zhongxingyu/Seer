 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package sensor;
 
 import core.PollingSensor;
 import edu.wpi.first.wpilibj.Encoder;
 import event.EncoderEvent;
 import event.EncoderListener;
 import java.util.Vector;
 
 /**
  *
  * @author gerberduffy
  * @author nadia
  */
 public class GRTEncoder extends PollingSensor {
 
     private Encoder rotaryEncoder;
     private double distancePerPulse;
     public static final int KEY_DISTANCE = 0;
     public static final int KEY_DEGREES = 1;
     public static final int KEY_DIRECTION = 2;
     public static final int NUM_DATA = 3;
     private Vector listeners;
 
     public GRTEncoder(int channelA, int channelB, double pulseDistance, int pollTime, String id) {
         super(id, pollTime, NUM_DATA);
         rotaryEncoder = new Encoder(channelA, channelB);
         rotaryEncoder.start();
         
         distancePerPulse = pulseDistance;
         listeners = new Vector();
     }
 
     protected void poll() {
        setState(KEY_DISTANCE, rotaryEncoder.getDistance() * Math.PI * distancePerPulse / 360.0);
        setState(KEY_DEGREES, rotaryEncoder.getDistance());
         setState(KEY_DIRECTION, rotaryEncoder.getDirection() ? TRUE : FALSE);
         
        System.out.println("Degrees: " + getState(KEY_DEGREES));
        System.out.println("Dist : " + getState(KEY_DISTANCE));
     }
 
     protected void notifyListeners(int id, double oldDatum, double newDatum) {
         EncoderEvent e = new EncoderEvent(this, id, newDatum);
         switch (id) {
             case KEY_DEGREES:
                 for (int i = 0; i < listeners.size(); i++) {
                     ((EncoderListener) listeners.elementAt(i)).degreeChanged(e);
                 }
                 break;
             case KEY_DIRECTION:
                 for (int i = 0; i < listeners.size(); i++) {
                     ((EncoderListener) listeners.elementAt(i)).directionChanged(e);
                 }
             case KEY_DISTANCE:
                 for (int i = 0; i < listeners.size(); i++) {
                     ((EncoderListener) listeners.elementAt(i)).distanceChanged(e);
                 }
         }
     }
 
     public void addEncoderListener(EncoderListener l) {
         listeners.addElement(l);
     }
 
     public void removeEncoderListener(EncoderListener l) {
         listeners.removeElement(l);
     }
 }
