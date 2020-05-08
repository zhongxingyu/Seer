 package storm2013.subsystems;
 
 import edu.wpi.first.wpilibj.command.Subsystem;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 /**
  *
  * @author ginto
  */
 public class Vision extends Subsystem {
     public static final class Distance {
         public static final Distance NEAR          = new Distance("Near");
         public static final Distance CENTER        = new Distance("Center line");
         public static final Distance OPPONENT_AUTO = new Distance("Opponent Auto");
         public static final Distance FEEDER        = new Distance("Feeder");
         
         private String _value;
         
         private Distance(String value) {
             _value = value;
         }
         
         public String getValue() {
             return _value;
         }
     }
     
     private static final Distance[] _distances = {
         Distance.NEAR,
         Distance.CENTER,
         Distance.OPPONENT_AUTO,
         Distance.FEEDER
     };
     
     private int _currIndex = 0;
     
     public Vision() {
         _setDistance(0);
     }
     
 
     protected void initDefaultCommand() {}
     
     public void setDistance(Distance distance) {
         for(int i=0;i<_distances.length;++i) {
             if(_distances[i] == distance) {
                 _setDistance(i);
                 return;
             }
         }
     }
     
     private void _setDistance(int index) {
        index = (index%_distances.length + _distances.length)%_distances.length;
         _currIndex = index;
         SmartDashboard.putString("Distance", _distances[index].getValue());
     }
     
     public void nextDistance() {
         _setDistance(_currIndex+1);
     }
     
     public void prevDistance() {
         _setDistance(_currIndex-1);
     }
 }
