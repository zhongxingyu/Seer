 package controlador;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.PhysicsSpace;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.input.KeyInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.CameraNode;
 import com.jme3.scene.control.CameraControl;
 import model.Audio;
 import model.Rival;
 import model.VehicleProtagonista;
 import model.WorldCreator;
 import vista.Display;
 
 
 public class Main extends SimpleApplication implements ActionListener {
 
     private BulletAppState bulletAppState;   
     private VehicleProtagonista car;
     private Rival rival;
     private WorldCreator world;
     private CameraNode camNode;    
     private MenuController menu;
     private Display display;
     private boolean gameStarted = false; 
     private RigidBodyControl landscape;
     private Vector3f initialPos;
     private Quaternion initialRot;
     
     /*Variables per a moure el rival per a fer el crcuit. Cal moure-ho en mesura del que es pugui 
     * a dins de la classe Rival*/
     int estado=1;
     public Vector3f direccioCar;
     public Vector3f direccioRival;
     public Vector2f r = new Vector2f(1.0f,0.1f);
     float angle; 
     
     public static void main(String[] args) {
         Main app = new Main();
         app.start();
     }
     
     private void setupKeys() {
         inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_LEFT));
         inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_RIGHT));
         inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_UP));
         inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_DOWN));
         inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
         inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));
         inputManager.addListener(this, "Lefts");
         inputManager.addListener(this, "Rights");
         inputManager.addListener(this, "Ups");
         inputManager.addListener(this, "Downs");
         inputManager.addListener(this, "Space");
         inputManager.addListener(this, "Reset");
     }
 
     @Override
     public void simpleInitApp() {
         bulletAppState = new BulletAppState();
         stateManager.attach(bulletAppState);
 
         /*if (settings.getRenderer().startsWith("LWJGL")) {
             BasicShadowRenderer bsr = new BasicShadowRenderer(assetManager, 512);
             bsr.setDirection(new Vector3f(-0.5f, -0.3f, -0.3f).normalizeLocal());
             viewPort.addProcessor(bsr);
         }
         cam.setFrustumFar(150f);
          * 
          */             
         
         display = new Display(assetManager,settings,guiNode,this.timer);        
         menu = new MenuController(settings,stateManager,assetManager,rootNode,guiViewPort,inputManager,audioRenderer,this,false,1,0,5,2,1,10,1,0,1,0,0,0,0);
     }
     
     private PhysicsSpace getPhysicsSpace() {
         return bulletAppState.getPhysicsSpace();
     }
 
     public void onAction(String binding, boolean value, float tpf) {
         if (binding.equals("Lefts")) {
             car.turnLeft(value);
         } else if (binding.equals("Rights")) {
             car.turnRight(value);
         } else if (binding.equals("Ups")) {
             car.forward(value);
         } else if (binding.equals("Downs")) {
             car.back(value);
         } else if (binding.equals("Reset")) {
             car.reset(value, initialPos, initialRot);
         }else if (binding.equals("Space")) {
             car.handBrake(value);
         }
         
     }
     
     
     /*Metode per comprovar que el cotxe protagonista esta en moviment*/
     public boolean comprovaMoviment (){
         if (car.getVehicle().getLinearVelocity().length()>=5) {
             return true;
         } else {
             return false;
         }
     }
     @Override
     public void simpleUpdate(float tpf) {
         
         flyCam.setEnabled(false);
         
         if(menu.isMenuFinished() && !gameStarted){            
             addWorld();            
             addProtagonista();
             addRival();
             addDisplay();
             gameStarted = true;
             setupKeys();
            audioGameStarted();
         }
         
         if(gameStarted){
             camNode.lookAt(car.getSpatial().getWorldTranslation(), Vector3f.UNIT_Y);
             
             camNode.setLocalTranslation(car.getSpatial().localToWorld( new Vector3f( 0, 4, -15), null));
             //System.out.println(car.getVehicle().getPhysicsLocation().getX());
             /*Codi per a moure el rival, cal moure-ho d'aqui*/
             switch (estado) {
                 case 1:
                     if(comprovaMoviment()==true) {
                         estado = 2;
                     }
                     break;
                 case 2:
 
                     if (rival.getVehicle().getPhysicsLocation().getZ()>=30) {
                         estado = 3;
                     }
                     if (rival.velocitat == 0) {
                         rival.moureEndavant();
                     }
                     break;
 
                 case 3:
                     r.setX(rival.getVehicle().getLinearVelocity().getX());
                     r.setY(rival.getVehicle().getLinearVelocity().getZ());
                     r = r.normalize();
                     if (r.getX()<-0.9f && r.getY()<-0.2f) {
                         rival.getVehicle().steer(0);
                         estado = 4;
 
                     } else {
                         rival.girarCurva1();
                     }
                     break;
                 case 4:
                     if (rival.getVehicle().getPhysicsLocation().getX()<=-40.f) {
                          estado = 5;
                     }                
                     rival.moureEndavant();
                     break;
                 case 5:
                     r.setX(rival.getVehicle().getLinearVelocity().getX());
                     r.setY(rival.getVehicle().getLinearVelocity().getZ());
                     r = r.normalize();
 
 
                     if (r.getX()>+.2f && r.getY()<-.9f) {
                         System.out.println(r);
                         System.out.println("recta 3");
                         estado = 6;
                         rival.getVehicle().steer(0);
                     } else {
                         rival.girarCurva1();
                     }
                     break;
                 case 6:
                     if (rival.getVehicle().getPhysicsLocation().getZ()<=-54.f) {
                         estado = 7;
                         System.out.println("curva 3");
                     }
                     rival.moureEndavant();
                     break;
                 case 7:
                     r.setX(rival.getVehicle().getLinearVelocity().getX());
                     r.setY(rival.getVehicle().getLinearVelocity().getZ());
                     r = r.normalize();
                     /*System.out.println("eeee");
                     System.out.println(r);*/
                     if (r.getX()>+.9f && r.getY()>+0.f) {
                         estado = 8;
                         rival.getVehicle().steer(0);
                     } else {
                         rival.girarCurva1();
                     }
                     break;
                 case 8:
                     if (rival.getVehicle().getPhysicsLocation().getX()>=0.f) {
                         estado = 9;
                         System.out.println("curva 4");
                     }
                     rival.moureEndavant();
                     break;
                 case 9:
                     r.setX(rival.getVehicle().getLinearVelocity().getX());
                     r.setY(rival.getVehicle().getLinearVelocity().getZ());
                     r = r.normalize();
                     /*System.out.println("eeee");
                     System.out.println(r);*/
                     if (r.getX()<-.2f && r.getY()>+.9f) {
 
                         estado = 2;
                         rival.getVehicle().steer(0);
                     } else {
                         rival.girarCurva1();
                     }
                     break;    
                 default:
             }
             world.updateMusic();
             display.updateDisplay(car.getSpeed(),1);      
             display.updateMirror(car.getSpatial().localToWorld(new Vector3f(0,3,-15), null),car.getSpatial().localToWorld( new Vector3f( 0, 3, 0), null));
         }
 
     }
     
     // Añadir aqui los gets necesarios que cada uno necesite para su constructor
     // ej : menu.getCarColorRGBA()
     
     private void addWorld(){
         //Cargamos la escena
         world = new WorldCreator(rootNode, assetManager, bulletAppState, this.viewPort, menu);
         initialPos = world.getInitialPos();
         initialRot = world.getInitialRot();
     }
     
     private void addDisplay(){        
         float minDimension = Math.min(settings.getWidth(),settings.getHeight());
         display.addDisplay((int)(settings.getWidth()-(minDimension/2.5f)/2),(int)((minDimension/2.5f)/2),2.5f,(int)(settings.getWidth()-(minDimension/40f)-(minDimension/11.42f)-10),(int)(settings.getHeight()*0.975f),40,(int)(settings.getWidth()-(minDimension/9.23f)-10),(int)(settings.getHeight()*0.95f),9.23f,(int)(settings.getWidth()-(minDimension/11.42f)-10),(int)(settings.getHeight()*0.85f),11.42f);        
         display.addMirror(settings.getWidth()/2, (int)(settings.getHeight()*0.88f), 3f,renderManager,cam,car.getSpatial().localToWorld(new Vector3f(0,3,-15), null),rootNode);        
     }        
     
     private void addProtagonista(){
         car = new VehicleProtagonista(getAssetManager(), getPhysicsSpace(), cam);
         car.setCocheProtagonista(menu.getIdCar(), menu.getCarColorNameENG());
         
         car.getVehicle().setPhysicsLocation(initialPos);
         car.getVehicle().setPhysicsRotation(initialRot);
         
         //Añadimos el coche protagonista
         rootNode.attachChild(car.getSpatial());
         
         //Settejem la camera
         camNode = new CameraNode("CamNode", cam);
         camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
         camNode.setLocalTranslation(new Vector3f(0, 4, -15));
         //camNode.setLocalTranslation(new Vector3f(-15, 15, -15));
         //camNode.setLocalTranslation(new Vector3f(-15, 15, -15));
         camNode.lookAt(car.getSpatial().getLocalTranslation(), Vector3f.UNIT_Y);
         
         rootNode.attachChild(camNode);
     }
     
     
     private void addRival(){
         //Aqui creem la classe rival i la afegim al rootNode
         rival = new Rival(getAssetManager(), getPhysicsSpace());
         rival.buildCar();
         rival.getVehicle().setPhysicsLocation(new Vector3f(5.f,-4.f,0.f));
         //Añadimos Rival
         rootNode.attachChild(rival.getSpatial());
     }
 }
