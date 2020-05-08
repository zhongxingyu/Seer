 package com.github.kenji0717.a3cs;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.util.*;
 import java.util.prefs.Preferences;
 import javax.swing.JOptionPane;
 import javax.vecmath.Vector3d;
 
 class CarRaceImpl implements Runnable, CollisionListener, CarSim {
     PhysicalWorld pw;
     Preferences prefs;
     RaceCarBase car;
     String carClasspath;
     String workDir;
     String workDirURL;
     ArrayList<ActiveObject> activeObjects = new ArrayList<ActiveObject>();
     boolean battleRunning = false;//一時停止中でもtrue
     boolean simRunning = false;//一時停止中はfalse
     URLClassLoader classLoader;
     CarRaceGUI gui;
     int NUM=12;
     MyCheckPoint cps[];
     Deque<MyCheckPoint> checkPointStack;
 
     CarRaceImpl(String args[]) {
         prefs = Preferences.userNodeForPackage(this.getClass());
         String carClass = prefs.get("carClass","test.TestCar01");
         if (args.length>=1) {
             carClass = args[0];
             prefs.put("carClass",carClass);
         }
         carClasspath = prefs.get("carClasspath","System");
         workDir = prefs.get("workDir",null);
         workDirURL = prefs.get("workDirURL",null);
         checkPointStack = new ArrayDeque<MyCheckPoint>();
 
         pw = new PhysicalWorld();
         pw.pause();
         simRunning = false;
         pw.addTask(this);
         pw.addCollisionListener(this);
 
         gui = new CarRaceGUI(this,carClass);
         gui.pack();
         gui.setVisible(true);
 
         pw.setMainCanvas(gui.mainCanvas);
         pw.addSubCanvas(gui.carCanvas);
     }
     //clearの処理
     void clearBattle() {
         if (simRunning)
             throw new IllegalStateException();
         if (battleRunning)
             throw new IllegalStateException();
         pw.clear();
         activeObjects.clear();
         car = null;
         classLoader = null;
         System.gc();
         gui.clearCamera();
         gui.clearTA();
         checkPointStack.clear();
     }
     void initBattle() {
         if (simRunning)
             throw new IllegalStateException();
         if (battleRunning)
             throw new IllegalStateException();
         MyGround2 g = new MyGround2(pw);
         pw.add(g);
         //MyGround g = new MyGround(pw);
         //pw.add(g);
 
         //CheckPoint配置
         cps = new MyCheckPoint[NUM];
         Vector3d loc = new Vector3d();
         Vector3d rot = new Vector3d();
         loc.set(  0,  4,-25);rot.set(0,0,0);
         cps[ 0] = new MyCheckPoint(loc,rot,pw);cps[ 0].a3.setUserData("cp00");
         loc.set(-37,  4,-56);rot.set(0,1.57,0);
         cps[ 1] = new MyCheckPoint(loc,rot,pw);cps[ 1].a3.setUserData("cp01");
         loc.set(-76,  4,  9);rot.set(0,0.78,0);
         cps[ 2] = new MyCheckPoint(loc,rot,pw);cps[ 2].a3.setUserData("cp02");
         loc.set(-21,  4, 20);rot.set(0,0,0);
         cps[ 3] = new MyCheckPoint(loc,rot,pw);cps[ 3].a3.setUserData("cp03");
         loc.set(-54,  4, 35);rot.set(0,0,0);
         cps[ 4] = new MyCheckPoint(loc,rot,pw);cps[ 4].a3.setUserData("cp04");
         loc.set(-16,  4, 35);rot.set(0,-0.78,0);
         cps[ 5] = new MyCheckPoint(loc,rot,pw);cps[ 5].a3.setUserData("cp05");
         loc.set(-23,  4,-39);rot.set(0,1.57,0);
         cps[ 6] = new MyCheckPoint(loc,rot,pw);cps[ 6].a3.setUserData("cp06");
         loc.set(-43,  4,  0);rot.set(0,1.57,0);
         cps[ 7] = new MyCheckPoint(loc,rot,pw);cps[ 7].a3.setUserData("cp07");
         loc.set(-53,  4,-38);rot.set(0,1.57,0);
         cps[ 8] = new MyCheckPoint(loc,rot,pw);cps[ 8].a3.setUserData("cp08");
         loc.set(-65,  9, 11);rot.set(0,0,0);
         cps[ 9] = new MyCheckPoint(loc,rot,pw);cps[ 9].a3.setUserData("cp09");
         loc.set(-40,  4, 55);rot.set(0,1.57,0);
         cps[10] = new MyCheckPoint(loc,rot,pw);cps[10].a3.setUserData("cp10");
         loc.set(  0,  4, 10);rot.set(0,0,0);
         cps[11] = new MyCheckPoint(loc,rot,pw);cps[11].a3.setUserData("cp11");
 
         for (int i=0;i<NUM;i++)
             pw.add(cps[i]);
 
         classLoader = makeClassLoader(carClasspath);
 
         String carClass = gui.carClassTF.getText();
         prefs.put("carClass",carClass);
         //try{prefs.flush();}catch(Exception e){;}
         try {
             Class<?> theClass = classLoader.loadClass(carClass);
             Class<? extends RaceCarBase> tClass = theClass.asSubclass(RaceCarBase.class);
             car = tClass.newInstance();
         } catch(Exception e) {
             System.out.println("Class Load Error!!!");
             e.printStackTrace();
         }
 
         //下の行の3.1という数値は本来PI=3.141592...ジンバルロック対策でわざと誤差を入れた
         car.init(new Vector3d( 0,0.8,-1),new Vector3d(0,3.1,0),"x-res:///res/stk_tux.a3",pw,this);
 
         pw.add(car.car);
         gui.setCar(car);
         activeObjects.add(car);
         gui.updateCarInfo(car);
         gui.a3csController.init();
     }
     
     URLClassLoader makeClassLoader(String s) {
         try {
             URLClassLoader cl = null;
             URL urls[] = null;
             if (s==null) {
                 urls = new URL[0];
             } else if (s.equals("System")) {
                 urls = new URL[0];
             } else if (s.equals("IDE")) {
                 if (workDirURL==null) {
                     urls = new URL[0];
                 } else {
                     urls = new URL[]{new URL(workDirURL)};
                 }
             } else {
                 urls = new URL[]{new URL(s)};
             }
             final URL urlsF[] = urls;
             final ClassLoader pCL = CarBase.class.getClassLoader();
             cl = AccessController.doPrivilegedWithCombiner(new PrivilegedAction<URLClassLoader>() {
                 public URLClassLoader run() {
                     return new URLClassLoader(urlsF,pCL);
                 }
             });
 
             return cl;
         } catch (MalformedURLException e) {
             return null;
         } catch (Error e) {
             e.printStackTrace();
             return null;
         }
     }
     void startBattle() {
         if (simRunning) {
             return;
             //throw new IllegalStateException();
         }
         if (battleRunning) {
             pw.resume();
             simRunning = true;
         } else {
             clearBattle();
             initBattle();
             pw.resume();
             simRunning = true;
             gui.setParamEditable(false);
             battleRunning = true;
         }
     }
     void pauseBattle() {
         if (!battleRunning)
             return;
         if (simRunning) {
             pw.pause();
             simRunning = false;
         } else {
             pw.resume();
             simRunning = true;
         }
     }
     void stopBattle() {
         battleRunning = false;
         //clearBattle();
         pw.pause();
         simRunning = false;
         gui.setParamEditable(true);
     }
     @Override
     public ArrayList<CarBase> getAllCar() {
         ArrayList<CarBase> ret = new ArrayList<CarBase>();
         ret.add(car);
         return ret;
     }
 
     @Override
     public void addActiveObject(ActiveObject o) {
         synchronized (activeObjects) {
             activeObjects.add(o);
         }
     }
 
     @Override
     public void delActiveObject(ActiveObject o) {
         synchronized (activeObjects) {
             activeObjects.remove(o);
         }
     }
 
     @Override
     public void collided(A3CollisionObject a, A3CollisionObject b) {
         if ((a instanceof MyCheckPoint)||(b instanceof MyCheckPoint)) {
             MyCheckPoint cp = null;
             A3CollisionObject other = null;
             if (a instanceof MyCheckPoint) {
                 cp = (MyCheckPoint)a;
                 other = b;
             } else {
                 cp = (MyCheckPoint)b;
                 other = a;
             }
             if (other instanceof MyCar) {
                 if (checkPointStack.peek()!=cp) {
                     checkPointStack.push(cp);
                     String t = String.format("%4.2f",pw.getTime());
                     System.out.println(cp.a3.getUserData()+":"+t);
                 }
                 if (cp==cps[NUM-1]) {
                     finishBattle();
                 }
             } else if (other instanceof MyGround2){
                 ;
             } else if (other instanceof MyBullet){
                 ;
             } else if (other instanceof MyCheckPoint){
                 ;
             } else {
             }
         }
         //System.out.println("gaha a:"+a.a3.getUserData()+" b:"+b.a3.getUserData());
     }
 
     @Override
     public void run() {
         ArrayList<ActiveObject> tmp = new ArrayList<ActiveObject>();
         synchronized (activeObjects) {
             tmp.clear();
             tmp.addAll(activeObjects);
         }
         for (ActiveObject o: tmp) {
            o.beforeExec();
             o.exec();
         }
         gui.updateCarInfo(car);
         gui.updateTime(pw.getTime());
     }
 
     void finishBattle() {
         pw.pause();
         simRunning = false;
         boolean goal=true;
         for (int i=0;i<NUM;i++) {
             if (checkPointStack.isEmpty()){
                 goal=false;
                 break;
             }
             MyCheckPoint cp = checkPointStack.removeLast();
             if (cps[i]!=cp) {
                 goal=false;
                 break;
             }
         }
         String message = goal ? "goal":"fail";
         message = message+"\ntime:"+String.format("%4.2f",pw.getTime());
         JOptionPane.showMessageDialog(gui,message);
 
         stopBattle();
     }
     void changeCP(String cp) {
         carClasspath = cp;
         prefs.put("carClasspath",carClasspath);
     }
     void setWorkDirURL(String wdu) {
         workDirURL = wdu;
         if (workDirURL!=null)
             prefs.put("workDirURL",workDirURL);
     }
     void setWorkDir(String wd) {
         workDir = wd;
         if (workDir!=null)
             prefs.put("workDir",workDir);
     }
     void fastForward(boolean b) {
         pw.fastForward(b);
     }
 }
