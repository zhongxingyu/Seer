 package ussr.samples.odin;
 
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.jme.math.Quaternion;
 
 import ussr.robotbuildingblocks.ModuleConnection;
 import ussr.robotbuildingblocks.ModulePosition;
 import ussr.robotbuildingblocks.RotationDescription;
 import ussr.robotbuildingblocks.VectorDescription;
 import ussr.robotbuildingblocks.WorldDescription;
 
 public class OdinBuilder {
     private ArrayList<ModulePosition> ballPos = new ArrayList<ModulePosition>();
     private ArrayList<ModulePosition> modulePos = new ArrayList<ModulePosition>();
     private ArrayList<ModulePosition> allPos = new ArrayList<ModulePosition>();
     private static final float unit = (float)Math.sqrt((0.18f*0.18f)/2);
     private static final float pi = (float)Math.PI;
 
     @Deprecated
     public ArrayList<ModulePosition> buildSpecificBlob(int nBalls, int xMax, int yMax, int zMax) {
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
                     modulePos.add(new ModulePosition(Integer.toString(index),"OdinMuscle", pos, rot));
                     index++;
                 }
             }
         }
         allPos.addAll(modulePos);
         allPos.addAll(ballPos);
         return allPos;
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
         else if(y1-y2<0&&z1-z2<0) return new RotationDescription(0,pi/4,-pi/2);
         else if(y1-y2<0&&z1-z2>0) return new RotationDescription(0,-pi/4,-pi/2);
         System.out.println("("+(x1-x2)+","+(y1-y2)+","+(z1-z2)+")");
         return new RotationDescription(0,0,0);
     }
     public static boolean isConnectable(ModulePosition ball, ModulePosition module) {
         float dist = ball.getPosition().distance(module.getPosition());
         return dist==(float)Math.sqrt(2*unit*unit)/2;
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
 
 }
