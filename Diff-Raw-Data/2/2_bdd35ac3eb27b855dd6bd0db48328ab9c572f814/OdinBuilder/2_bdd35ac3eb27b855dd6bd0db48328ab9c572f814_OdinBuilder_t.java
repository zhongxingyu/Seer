 /**
  * Unified Simulator for Self-Reconfigurable Robots (USSR)
  * (C) University of Southern Denmark 2008
  * This software is distributed under the BSD open-source license.
  * For licensing see the file LICENCE.txt included in the root of the USSR distribution.
  */
 package ussr.samples.odin;
 
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.jme.math.Quaternion;
 
 import ussr.description.geometry.RotationDescription;
 import ussr.description.geometry.VectorDescription;
 import ussr.description.setup.ModuleConnection;
 import ussr.description.setup.ModulePosition;
 import ussr.description.setup.WorldDescription;
 
 /**
  * Helper class for building structures of Odin modules
  * 
  * @author ups
  * @author Konstantinas (modified for builder). In particular added setter
  * methods called "setBallPos(ArrayList<ModulePosition> ballPos)" and "setModulePos(ArrayList<ModulePosition> modulePos)".
  * Moreover modified method called "isConnectable(ModulePosition ball, ModulePosition module)" to look into
  * the interval of connection distance.
  * 
  */
 public class OdinBuilder {
     private ArrayList<ModulePosition> ballPos = new ArrayList<ModulePosition>();
 	private ArrayList<ModulePosition> modulePos = new ArrayList<ModulePosition>();
 	private ArrayList<ModulePosition> allPos = new ArrayList<ModulePosition>();
     private static final float unit = (float)Math.sqrt((0.18f*0.18f)/2);
     private static final float pi = (float)Math.PI;
 
     public ArrayList<ModulePosition> buildDenseBlob(int nBalls, int xMax, int yMax, int zMax) {
         int index=0;
         for(int x=0;x<xMax;x++) {
             for(int y=0;y<yMax;y++) {
                 for(int z=0;z<zMax;z++) {
                     if((x+y+z)%2==0) {
                         VectorDescription pos = new VectorDescription(x*unit,y*unit,z*unit);
                         if(index<nBalls) {
                             ballPos.add(new ModulePosition(Integer.toString(index),"OdinBall", pos, new RotationDescription(0,0,0)));
                         }
                         index++;
                     }
                 }
             }
         }
         for(int i=0;i<ballPos.size();i++) {
             for(int j=i+1;j<ballPos.size();j++) {
                 if(isNeighorBalls(ballPos.get(i),ballPos.get(j))) {
                     VectorDescription pos = posFromBalls(ballPos.get(i),ballPos.get(j));
                     RotationDescription rot = rotFromBalls(ballPos.get(i),ballPos.get(j));
                     System.out.println("Module created: pos="+pos+" rot="+rot);
                     modulePos.add(new ModulePosition(Integer.toString(index),"OdinMuscle", pos, rot));
                     index++;
                 }
             }
         }
         allPos.addAll(modulePos);
         allPos.addAll(ballPos);
         return allPos;
     }
     public void addBall(int x, int y, int z, int index) {
         if((x+y+z)%2==0) {
         	VectorDescription pos = new VectorDescription(x*unit,y*unit,z*unit);
         	ModulePosition ballPosition = new ModulePosition(Integer.toString(index),"OdinBall", pos, new RotationDescription(0,0,0));
         	ballPos.add(ballPosition);
         	allPos.add(ballPosition);
         	return;
         }
         throw new RuntimeException("Odin ball placed outside lattice");
     }
     
     public void addModule(int ballIndex1, int ballIndex2, String moduleType, int index) {
         if(isNeighorBalls(ballPos.get(ballIndex1),ballPos.get(ballIndex2))) {
             VectorDescription pos = posFromBalls(ballPos.get(ballIndex1),ballPos.get(ballIndex2));
             RotationDescription rot = rotFromBalls(ballPos.get(ballIndex1),ballPos.get(ballIndex2));
             ModulePosition modulePosition = new ModulePosition(Integer.toString(index),"OdinMuscle", pos, rot);
             modulePos.add(modulePosition);
             allPos.add(modulePosition);
             return;
         }
         throw new RuntimeException("Odin module placed outside lattice");
     }
     
     public ArrayList<ModuleConnection> allConnections() {
         ArrayList<ModuleConnection> connections = new ArrayList<ModuleConnection>();
         for(int i=0;i<ballPos.size();i++) {
             for(int j=0;j<modulePos.size();j++) {
                 if(isConnectable(ballPos.get(i), modulePos.get(j))) {
                     connections.add(new ModuleConnection(ballPos.get(i).getName(),modulePos.get(j).getName()));
                 }
             }
         }
         return connections;
     }
     private static VectorDescription posFromBalls(ModulePosition p1, ModulePosition p2) {
         VectorDescription pos = new VectorDescription((p1.getPosition().getX()+p2.getPosition().getX())/2,(p1.getPosition().getY()+p2.getPosition().getY())/2,(p1.getPosition().getZ()+p2.getPosition().getZ())/2);
         return pos;
     }
     
     private static RotationDescription rotFromBalls(ModulePosition p1, ModulePosition p2) {
         float x1 = p1.getPosition().getX();
         float y1 = p1.getPosition().getY();
         float z1 = p1.getPosition().getZ();
         float x2 = p2.getPosition().getX();
         float y2 = p2.getPosition().getY();
         float z2 = p2.getPosition().getZ();
         if(x1-x2<0&&z1-z2<0) return new RotationDescription(0,-pi/4,0);
         else if(x1-x2<0&&z1-z2>0) return new RotationDescription(0,pi/4,0);
         else if(x1-x2<0&&y1-y2<0) return new RotationDescription(0,0,pi/4);
         else if(x1-x2<0&&y1-y2>0) return new RotationDescription(0,0,-pi/4);
         else if(y1-y2<0&&z1-z2<0) return new RotationDescription(0,pi/2,-pi/4); // changed when JME 1.0->2.0 from (0,pi/4,-pi/2)
        else if(y1-y2<0&&z1-z2>0) return new RotationDescription(0,-pi/2,-pi/4);//changed as well
         System.out.println("("+(x1-x2)+","+(y1-y2)+","+(z1-z2)+")");
         return new RotationDescription(0,0,0);
     }
     public static boolean isConnectable(ModulePosition ball, ModulePosition module) {
         float dist = ball.getPosition().distance(module.getPosition());
         float connectionDistance = (float)Math.sqrt(2*unit*unit)/2;
         float tolerance = 1.001f;
         if (dist==connectionDistance||dist<tolerance*connectionDistance && dist>connectionDistance){
         	return true;
         }
         return false;
     }
     public static boolean isNeighorBalls(ModulePosition ball1, ModulePosition ball2) {
         float dist = ball1.getPosition().distance(ball2.getPosition());
         return dist==(float)Math.sqrt(2*unit*unit);
     }
 
     public List<ModulePosition> buildHingePyramid() {
         int index=0;
         int nBalls=6, xMax=6;
         
         for(int x=0;x<xMax;x++) {
             VectorDescription pos = new VectorDescription(x*unit,-0.48f,x*unit);
             if(index<nBalls) {
                 ballPos.add(new ModulePosition(Integer.toString(index),"OdinBall", pos, new RotationDescription(0,0,0)));
             }
             index++;
         }
         for(int i=0;i<ballPos.size();i++) {
             for(int j=i+1;j<ballPos.size();j++) {
                 if(isNeighorBalls(ballPos.get(i),ballPos.get(j))) {
                     VectorDescription pos = posFromBalls(ballPos.get(i),ballPos.get(j));
                     RotationDescription rot = rotFromBalls(ballPos.get(i),ballPos.get(j));
                     if(index%2==0) {    //rotate every other module 
                         Quaternion q = new Quaternion();
                         q.fromAngles(pi/2, 0, 0);
                         rot.setRotation(rot.getRotation().mult(q));
                     }
                     modulePos.add(new ModulePosition(Integer.toString(index),"OdinHinge", pos, rot));
                     index++;
                 }
             }
         }
 
         allPos.addAll(modulePos);
         allPos.addAll(ballPos);
         return allPos;
     }
 
     public List<ModulePosition> getModulePositions() {
         return allPos;
     }
 
     public void report(PrintStream out) {
         out.println("#Balls Placed  = "+ballPos.size());
         out.println("#Module Placed = "+modulePos.size());
         out.println("#Total         = "+modulePos.size());
     }
 
     /**
      * Function for designating what type of modules to insert into the structure
      * 
      * @author ups
      */
     public interface ModuleDesignator {
         public String selectModule(int index);
     }
     
     public ArrayList<ModulePosition> buildHingeBlob(int nBalls, int xMax, int yMax, int zMax) {
         return buildWhateverBlob(new VectorDescription(), nBalls,xMax,yMax,zMax,new ModuleDesignator() {
             public String selectModule(int index) { return "OdinHinge"; }
         });
     }
     
     public ArrayList<ModulePosition> buildWheelBlob(VectorDescription offset, int nBalls, int xMax, int yMax, int zMax) {
         return buildWhateverBlob(offset,nBalls,xMax,yMax,zMax,new ModuleDesignator() {
             public String selectModule(int index) {
                 if(index%2==0) return "OdinBattery";
                 else return "OdinWheel";
             }
         });
     }
     
     public ArrayList<ModulePosition> buildMuscleBlob(int nBalls, int xMax, int yMax, int zMax) {
         return buildWhateverBlob(new VectorDescription(), nBalls,xMax,yMax,zMax,new ModuleDesignator() {
             public String selectModule(int index) {
                 if(index%2==0) return "OdinMuscle";
                 else return null;
             }
         });
     }
     
     public ArrayList<ModulePosition> buildWhateverBlob(VectorDescription offset, int nBalls, int xMax, int yMax, int zMax, ModuleDesignator designator) {
         int index=0;
         for(int x=0;x<xMax;x++) {
             for(int y=0;y<yMax;y++) {
                 for(int z=0;z<zMax;z++) {
                     if((x+y+z)%2==0) {
                         VectorDescription pos = new VectorDescription(x*unit,y*unit,z*unit).add(offset);;
                         if(index<nBalls) {
                             ballPos.add(new ModulePosition(Integer.toString(index),"OdinBall", pos, new RotationDescription(0,0,0)));
                         }
                         index++;
                     }
                 }
             }
         }
         for(int i=0;i<ballPos.size();i++) {
             for(int j=i+1;j<ballPos.size();j++) {
                 if(isNeighorBalls(ballPos.get(i),ballPos.get(j))) {
                     VectorDescription pos = posFromBalls(ballPos.get(i),ballPos.get(j));
                     RotationDescription rot = rotFromBalls(ballPos.get(i),ballPos.get(j));
                     if(index%2==0) {
                         Quaternion q = new Quaternion();
                         q.fromAngles(pi/2, 0, 0);
                         rot.setRotation(rot.getRotation().mult(q));
                         
                     }
                     String moduleType = designator.selectModule(index);
                     if(moduleType!=null) modulePos.add(new ModulePosition(Integer.toString(index),moduleType,pos,rot));
                     index++;
                 }
             }
         }
         
         allPos.addAll(modulePos);
         allPos.addAll(ballPos);
 
         return allPos;
     }
     
     /**
      * Sets the positions of OdinBalls in simulation.
      * @param ballPos, the positions of OdinBalls.
      */
     public void setBallPos(ArrayList<ModulePosition> ballPos) {
 		this.ballPos = ballPos;
 	}
     
     /**
      * Sets the positions of Odin modules in simulation except OdinBalls.
      * @param modulePos, the positions of Odin modules except OdinBalls.
      */
     public void setModulePos(ArrayList<ModulePosition> modulePos) {
 		this.modulePos = modulePos;
 	}
 
 }
