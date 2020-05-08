 package prototype;
 
 import java.util.ArrayList;
 import java.util.Random;
 import javax.vecmath.Vector3d;
 import jp.sourceforge.acerola3d.a3.A3Background;
 import jp.sourceforge.acerola3d.a3.A3CanvasInterface;
 import jp.sourceforge.acerola3d.a3.Action3D;
 import jp.sourceforge.acerola3d.a3.VRML;
 import com.github.hiuprocon.pve.core.*;
 import com.github.hiuprocon.pve.obj.*;
 
 public class Simulator2 implements CollisionListener {
     PVEWorld w;
     PVEObject ground;
     Elevator elevator;
     PVEObject slope1;
     PVEObject slope2;
     PVEObject switch1;
     PVEObject switch2;
     PVEObject goal1;
     PVEObject goal2;
     PVEObject floor1;
     PVEObject floor2;
     CarInterface car1;
     CarInterface car2;
     ArrayList<Jewel> jewels = new ArrayList<Jewel>();
     Simulator2GUI gui;
     int waitTime = 33;
     Random random;
 
     public Simulator2() {
         w = new PVEWorld(PVEWorld.A3CANVAS,PVEWorld.MANUAL_STEP);
         //w = new PVEWorld(PVEWorld.A3CANVAS,PVEWorld.AUTO_STEP);
         gui = new Simulator2GUI(this);
         w.addCollisionListener(this);
         initWorld();
     }
     void initWorld() {
         noOfActivated = 3; //simulator + cars
         noOfWaiting = 0;
 
         w.resume();
         synchronized(waitingRoom) {
             waitingRoom.notifyAll();
         }
         try{Thread.sleep(1000);}catch(Exception e) {;}
 
         if (car1!=null) {
             car1.dispose();
             car2.dispose();
             try{Thread.sleep(1000);}catch(Exception e) {;}
         }
 
         w.pause();
         w.clear();
 
         ground = new BoxObj(Type.STATIC, 0, new Vector3d(250, 1, 250),
                 "x-res:///res/prototype/Ground2.wrl");
         ground.setUserData("ground");
         ground.setLocRev(0, -0.5, 0, 0, 0, 0);
         w.add(ground);
 
         elevator = new Elevator();
         elevator.setUserData("elevator1");
         elevator.setLocRev(0, 0, 0, 0, 90, 0);
         w.add(elevator);
 
         slope1 = new SlopeObj(10, 1, 10);
         slope1.setUserData("slope1");
         slope1.setLocRev(-10.0, 0.5, 0, 0, -90, 0);
         w.add(slope1);
 
         slope2 = new SlopeObj(10, 1, 10);
         slope2.setUserData("slope2");
         slope2.setLocRev(10.0, 0.5, 0, 0, 90, 0);
         w.add(slope2);
 
         switch1 = new BoxObj(Type.GHOST, 1, new Vector3d(10, 1, 10),
                 "x-res:///res/prototype/Switch.wrl");
         switch1.setUserData("switch1");
         switch1.setLocRev(0.0, 0.5, 11, 0, 0, 0);
         w.add(switch1);
 
         switch2 = new BoxObj(Type.GHOST, 1, new Vector3d(10, 1, 10),
                 "x-res:///res/prototype/Switch.wrl");
         switch2.setUserData("switch2");
         switch2.setLocRev(0.0, 0.5, -11, 0, 0, 0);
         w.add(switch2);
 
         goal1 = new BoxObj(Type.GHOST, 1, new Vector3d(10, 1, 10),
                 "x-res:///res/prototype/Goal.wrl");
         goal1.setUserData("goal1");
         goal1.setLocRev(-10, 15.5, 0, 0, 0, 0);
         w.add(goal1);
 
         goal2 = new BoxObj(Type.GHOST, 1, new Vector3d(10, 1, 10),
                 "x-res:///res/prototype/Goal.wrl");
         goal2.setUserData("goal2");
         goal2.setLocRev( 10, 15.5, 0, 0, 0, 0);
         w.add(goal2);
 
         floor1 = new BoxObj(Type.STATIC,0.0,new Vector3d(10,1,10),"x-res:///res/ClearBox.wrl");
         floor1.setUserData("floor1");
         floor1.setLocRev(-10, 14.5,0,0,0,0);
         w.add(floor1);
 
         floor2 = new BoxObj(Type.STATIC,0.0,new Vector3d(10,1,10),"x-res:///res/ClearBox.wrl");
         floor2.setUserData("floor1");
         floor2.setLocRev( 10, 14.5,0,0,0,0);
         w.add(floor2);
 
         car1 = new CarD(this, 10000);
         car1.setUserData("car1");
         car1.setLocRev(-80, 0, 0, 0, 90, 0);
         w.add((PVEObject)car1);
         gui.setCar1(car1);
 
         car2 = new CarD(this, 20000);
         car2.setUserData("car2");
         car2.setLocRev(80, 0, 0, 0, -90, 0);
         w.add((PVEObject)car2);
         gui.setCar2(car2);
 
         ((CarD)car1).setAnotherCar((CarD)car2);
         ((CarD)car2).setAnotherCar((CarD)car1);
 
         String seedS = gui.getSeed();
         if (seedS==null||seedS.equals("")) {
             random = new Random();
         } else {
             long seed = 0;
             try {
                 seed = Long.parseLong(seedS);
             } catch (NumberFormatException e) {
                 e.printStackTrace();
                 gui.setSeed(0);
             }
             random = new Random(seed);
         }
         for (int i = 0; i < 10; i++) {
             Jewel j = new Jewel();
             j.setUserData("jA1." + i);
             double x = 60 * random.nextDouble() - 30 - 50;
             double z = 80 * random.nextDouble() - 40;
             j.setLocRev(x, 2, z, 0, 0, 0);
             w.add(j);
             jewels.add(j);
         }
         for (int i = 0; i < 10; i++) {
             Jewel j = new Jewel();
             j.setUserData("jA2." + i);
             double x = 60 * random.nextDouble() - 30 + 50;
             double z = 80 * random.nextDouble() - 40;
             j.setLocRev(x, 2, z, 0, 0, 0);
             w.add(j);
             jewels.add(j);
         }
         w.resume();
         w.stepForward();
 
         A3CanvasInterface mainCanvas = w.getMainCanvas();
         mainCanvas.setHeadLightEnable(false);
         Action3D light = null;
         Action3D grid1 = null;
         Action3D grid2 = null;
         Action3D grid3 = null;
         VRML jewelField1 = null;
         VRML jewelField2 = null;
         try {
             light = new Action3D("x-res:///res/DirectionalLightSet.a3");
             grid1 = new Action3D("x-res:///res/prototype/PCGrid.a3");
             grid2 = new Action3D("x-res:///res/prototype/PCGrid.a3");
             grid3 = new Action3D("x-res:///res/prototype/PCGrid.a3");
             jewelField1 = new VRML("x-res:///res/prototype/JewelField.wrl");
             jewelField2 = new VRML("x-res:///res/prototype/JewelField.wrl");
         } catch (Exception e) {
             e.printStackTrace();
         }
         light.change("dl1.0");
         light.setLoc(0,10,0);
         mainCanvas.add(light);
         grid1.setRev(-90,0,0);
         grid1.setLoc(0,-2,0);
         mainCanvas.add(grid1);
         grid2.setLoc(0,0,-150);
         mainCanvas.add(grid2);
         grid3.setRev(0,90,0);
         grid3.setLoc(-150,0,0);
         mainCanvas.add(grid3);
         jewelField1.setScaleX(60);jewelField1.setScaleY(0.1);jewelField1.setScaleZ(80);
         jewelField1.setLoc(50,0,0);
         mainCanvas.add(jewelField1);
         jewelField2.setScaleX(60);jewelField2.setScaleY(0.1);jewelField2.setScaleZ(80);
         jewelField2.setLoc(-50,0,0);
         mainCanvas.add(jewelField2);
         mainCanvas.setBackground(new A3Background(0.1f, 0.3f, 0.5f));
     }
 
     public void start() {
         //A3CanvasInterface mainCanvas = w.getMainCanvas();
         waitTime = 33;
         while (true) {
             stepForward();
             if (w.getTime()>=5000.0)
                 timeUp();
             //mainCanvas.waitForUpdate(waitTime * 2);
             //Thread.sleep(waitTime/2);// 微妙
             gui.updateTime(w.getTime());
             try {Thread.sleep(waitTime);}catch(Exception e) {;}
         }
     }
     Object waitingRoom = new Object();
     int noOfActivated = 3; //simulator + cars
     volatile int noOfWaiting = 0;
     //ArrayList<Object> waitings = new ArrayList<Object>();
     void deactivateOneCar() {
         noOfActivated=2;
         noOfWaiting = 0;
         synchronized(waitingRoom) {
             waitingRoom.notifyAll();
         }
     }
     void activateTwoCars() {
         noOfActivated=3;
         noOfWaiting = 0;
         synchronized(waitingRoom) {
             waitingRoom.notifyAll();
         }
     }
     void stepForward() {
         synchronized (waitingRoom) {
             noOfWaiting++;
             if (noOfWaiting==noOfActivated) {
                 w.stepForward();
                 noOfWaiting = 0;
                 waitingRoom.notifyAll();
             } else {
                 try {
                     waitingRoom.wait();
                 } catch(Exception e) {
                     ;
                 }
             }
         }
     }
 
     @Override
     public void collided(PVEPart a, PVEPart b) {
         PVEObject aa = a.getObject();
         PVEObject bb = b.getObject();
         if (aa == ground || bb == ground)
             return;
         // System.out.println("collided "+(String)(aa.getUserData())+":"+
         // ((String)bb.getUserData()));
         ArrayList<Jewel> alTmp = new ArrayList<Jewel>();
         synchronized (jewels) {
             alTmp.addAll(jewels);
         }
         int jewelsCount=0;
         for (Jewel j:alTmp) {
             Vector3d jLoc = j.getLoc();
             if (jLoc.x<5.0 && jLoc.x>-5.0)
                 if (jLoc.z<5.0 && jLoc.z>-5.0)
                     jewelsCount++;
         }
         if ((aa == switch1 || aa == switch2) && (bb instanceof CarInterface) && (jewelsCount<=5))
             elevator.setUp();
         if ((aa instanceof CarInterface) && (bb == switch1 || bb == switch2) && (jewelsCount<=5))
             elevator.setUp();
         if ((aa == goal1 || aa == goal2) && bb instanceof Jewel) {
             goal((Jewel)bb);
         }
         if (aa instanceof Jewel && (bb == goal1 || bb == goal2)) {
             goal((Jewel)aa);
         }
 
     }
     void goal(Jewel j) {
         gui.appendText("goal! "+j.getUserData());
         w.del(j);
         synchronized(jewels) {
             jewels.remove(j);
         }
         if (jewels.size()==0) {
             w.pause();
             try{Thread.sleep(1000);}catch(Exception e){;}
             gui.updateTime(w.getTime());
             gui.appendText(String.format("clear!!!!!  time=%9.2f",w.getTime()));
         }
     }
     void timeUp() {
         w.pause();
         try{Thread.sleep(1000);}catch(Exception e){;}
         gui.updateTime(5000.0);
         gui.appendText(String.format("time up!!!!!  time=%9.2f",5000.0));
     }
     String searchJewels() {
         synchronized(jewels) {
             String s = ""+jewels.size();
             for (Jewel j:jewels) {
                 Vector3d v = j.getLoc();
                 s = s +" "+j.getUserData().toString()+" "+v.x+" "+v.y+" "+v.z;
             }
             return s;
         }
     }
     void setWaitTime(int t) {
         waitTime=t;
     }
     void pause() {
         w.pause();
     }
     void resume() {
         w.resume();
     }
 
     public static void main(String args[]) {
         Simulator2 s2 = new Simulator2();
         s2.start();
     }
 }
