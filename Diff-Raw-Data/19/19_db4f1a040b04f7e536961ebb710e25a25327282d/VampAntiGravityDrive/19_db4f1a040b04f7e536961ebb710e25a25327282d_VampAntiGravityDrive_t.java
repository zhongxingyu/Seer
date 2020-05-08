 package teamlg.drive.antiGrav;
 
 import java.util.HashSet;
 import static teamlg.drive.antiGrav.AntiGravityDrive.REPULSE_FACTOR;
 import xander.core.Resources;
 import xander.core.track.Snapshot;
 
 /**
  *
  * @author FHEMERY
  */
 public class VampAntiGravityDrive extends AntiGravityDrive{
 
     private static final String ROBOVAMP_STR = "Robovamp";
     
     public VampAntiGravityDrive(double mapXlength, double mapYLength) {
         super(mapXlength,mapYLength);
     }
     
     @Override
     protected void ComputeRobotThreat(GravityPoint p) {
         if (p.name.contains(ROBOVAMP_STR))
         {
             boolean isStrongest = true;
             double maxHealth = robot.getEnergy();
             HashSet<String> aRobotList = Resources.getOtherRobots().getRobotList(); 
             for (String rName: aRobotList)
             {
                 if (rName.contains(ROBOVAMP_STR))
                     continue;
                 Snapshot sn = Resources.getSnapshotHistory().getSnapshot(rName);
                 if (sn != null && maxHealth < sn.getEnergy())
                 {
                     isStrongest = false;
                     break;
                 }
             }
             if (isStrongest)
             {
                 double d2 = Math.pow(p.x - myX, 2) + Math.pow(p.y - myY, 2);
                 targetX += REPULSE_FACTOR * p.power * 1.5 * (1 / Math.pow(d2, 1.5)) * (p.x - myX);
                 targetY += REPULSE_FACTOR * p.power * 1.5 * (1 / Math.pow(d2, 1.5)) * (p.y - myY);
             }
             else
             {
                 double d2 = Math.pow(p.x - myX, 2) + Math.pow(p.y - myY, 2);
                 if (d2 > 2500)
                 {
                     targetX += REPULSE_FACTOR * p.power * -1 * (1 / Math.pow(d2, 2)) * (p.x - myX);
                     targetY += REPULSE_FACTOR * p.power * -1 * (1 / Math.pow(d2, 2)) * (p.y - myY);
                 }
             }
         }
         else
         {
             double d2 = Math.pow(p.x - myX, 2) + Math.pow(p.y - myY, 2);
             targetX += REPULSE_FACTOR * p.power * (1 / Math.pow(d2, 1.5)) * (p.x - myX);
             targetY += REPULSE_FACTOR * p.power * (1 / Math.pow(d2, 1.5)) * (p.y - myY);
         }
        
     }
 }
