 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package model;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.bounding.BoundingBox;
 import com.jme3.bullet.PhysicsSpace;
 import com.jme3.bullet.collision.PhysicsCollisionEvent;
 import com.jme3.bullet.collision.PhysicsCollisionListener;
 import com.jme3.bullet.collision.shapes.CollisionShape;
 import com.jme3.bullet.control.VehicleControl;
 import com.jme3.bullet.util.CollisionShapeFactory;
 import com.jme3.math.FastMath;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.Camera;
 import com.jme3.scene.CameraNode;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 
 
 /**
 *
 * @author Sergi
 */
 public class VehicleProtagonista implements PhysicsCollisionListener{
 
     private VehicleControl vehicle;
     private MaterialsVehicle materials;
     private Geometry chasis1;
     private Geometry chasis12;
     private Geometry wheel1;
     private Geometry wheel3;
     private Geometry wheel2;
     private Geometry wheel4;
     private Geometry wheel12;
     private Geometry wheel32;
     private Geometry wheel22;
     private Geometry wheel42;
     private float wheelRadius;
     private AssetManager assetManager;
     private CameraNode camNode;
     private Node vehicleNode;
     private PhysicsSpace physicsSpace;
     private Camera cam;
     
     //Numero de vueltas del vehiculo protagonista
     private int numVueltas = 0;
 
     private boolean reverseMode = false;
     private boolean handBrakeMode  = false;
     private boolean forwardMode = false;
     
     private Audio starting_car_sound;
     private Audio accelerate_sound;
     private Audio decelerate_sound;
     private Audio max_velocity_sound;
     private Audio idling_car_sound;
     private LlistaReproduccio brake_sounds;
     private boolean effects = true;
     private boolean paused = false;
     
     private String puntsVoltaData;
     private int numVoltes;
     private int numPuntsControlVolta;
     private int estat;
     private int estatControlVolta;
     private int posicioCarrera;
     private Vector3f puntControlVolta;
     
     private Node meshNodeDef11;
     private Node meshNodeDef12;
     private Node meshNodeDef13;
     
     private Node meshNodeDef21;
     private Node meshNodeDef22;
     private Node meshNodeDef23;
     
     
     //Objeto que encapsula la configuracion del coche
     private CarSettings carSettings;
     
     //Initial position and initial rotation of the car
     public Vector3f initialPos = new Vector3f(0f,0f,0f);
     public Quaternion initialRot = new Quaternion();
     //float accelerationValue = 0;
     //    float accelerationForce = 1000;
     //    float accelerationFactor = 2;
     private boolean prevCollisioned = false;
     private int idModelCar;
     private int collisionCounter;
     
     private CheckPoints checkPoints;
 
     public VehicleProtagonista(AssetManager asset, PhysicsSpace phy, Camera cam, int idCircuit) {
         assetManager = asset;
         physicsSpace = phy;
         numVoltes=0;
         posicioCarrera=1;
         checkPoints = new CheckPoints(idCircuit);
         
         canviaEstatControlVolta(1);
     }  
 
     private Geometry findGeom(Spatial spatial, String name) {
         if (spatial instanceof Node) {
             Node node = (Node) spatial;
             for (int i = 0; i < node.getQuantity(); i++) {
                 Spatial child = node.getChild(i);
                 Geometry result = findGeom(child, name);
                 if (result != null) {
                     return result;
                 }
             }
         } else if (spatial instanceof Geometry) {
             if (spatial.getName().startsWith(name)) {
                 return (Geometry) spatial;
             }
         }
         return null;
     }
     
     public void setCocheProtagonista(int idModel, String idColor){
         materials = new MaterialsVehicle(assetManager, idColor);
         materials.initMaterials();
         carSettings = new CarSettings();
         carSettings.readXml();
         carSettings.loadAtributes(idModel);
         prevCollisioned = false;
         idModelCar = idModel;
         collisionCounter = 0;
         
         loadDeformedModels();
         
         if(idModel==1){
             buildVitoFerrari();
         }else if(idModel == 2){
             buildGolf();
         }
     }
 
     public void canviaEstatControlVolta(int estatFutur) {
         estatControlVolta = estatFutur;
         if(estatControlVolta == checkPoints.getLlistaControlVolta().size()+1){
             estatControlVolta = 1;
             numVoltes++;
         }
         puntControlVolta = checkPoints.buscaPuntControlVolta(estatControlVolta);
     }
     
     private void buildGolf(){
         Node meshNode1 = (Node) assetManager.loadModel("Models/Cars/golfCar/Car.scene");    
         Node meshNode = (Node) assetManager.loadModel("Models/Cars/tempCar/Car.scene");
 
         chasis1 = findGeom(meshNode, "Car");
         chasis12 = findGeom(meshNode1, "Car");
         chasis12.setLocalTranslation(0, 1.1f, 0);
         chasis12.setLocalScale(chasis12.getWorldScale().mult(0.3f));
         chasis1.rotate(0, 3.135f, 0);
         chasis12.rotate(0, 3.135f, 0);
         
         
         chasis12.setMaterial(materials.getMatChasis());
         
         CollisionShape carHull = CollisionShapeFactory.createDynamicMeshShape(chasis1);
         BoundingBox carbox = (BoundingBox) chasis1.getModelBound();
 
         //create vehicle node
         vehicleNode = new Node("vehicleProtaNode");
         vehicle = new VehicleControl(carHull, 400);
         vehicleNode.addControl(vehicle);
         vehicleNode.attachChild(chasis12);
         
         Geometry cristal = findGeom(meshNode1, "cristal");
         cristal.setLocalTranslation(cristal.getWorldTranslation().x*0.3f,
                                     cristal.getWorldTranslation().y*0.3f+1.1f,
                                     cristal.getWorldTranslation().z*0.3f);
         cristal.setLocalScale(cristal.getWorldScale().mult(0.3f));
         cristal.rotate(0, 3.135f, 0);
         vehicleNode.attachChild(cristal);
         
         vehicle.setSuspensionCompression(carSettings.getCompValue() * 2.0f * FastMath.sqrt(carSettings.getStiffness()));
         vehicle.setSuspensionDamping(carSettings.getDampValue() * 2.0f * FastMath.sqrt(carSettings.getStiffness()));
         vehicle.setSuspensionStiffness(carSettings.getStiffness());
         vehicle.setMaxSuspensionForce(carSettings.getMaxSuspensionForce());
 
         //Create four wheels and add them at their locations
         Vector3f wheelDirection = new Vector3f(0, -1, 0); // was 0, -1, 0
         Vector3f wheelAxle = new Vector3f(-1, 0, 0); // was -1, 0, 0
         float radius = 0.5f;
         float restLength = 0.3f;
         float yOff = 0.5f;
         float xOff = 1f;
         float zOff = 2f;
 
 
         Node node1 = new Node("wheel 1 node");
         wheel1 = findGeom(meshNode, "WheelFrontLeft");
         wheel12 = findGeom(meshNode1, "WheelFrontLeft");
         Vector3f wheelPos = wheel12.getWorldTranslation();
         wheel1.setMaterial(materials.getMatWheels());
         node1.attachChild(wheel1);
         wheel1.center();
         BoundingBox box = (BoundingBox) wheel1.getModelBound();
         wheelRadius = box.getYExtent();
         float back_wheel_h = (wheelRadius * 1.7f) - 1f;
         float front_wheel_h = (wheelRadius * 1.9f) - 1f;
         vehicle.addWheel(wheel1.getParent(), carbox.getCenter().add(wheelPos.x*0.3f, -back_wheel_h-0.6f, wheelPos.z*0.3f),
                 wheelDirection, wheelAxle, 0.2f, wheelRadius, false);
 
         Node node2 = new Node("wheel 2 node");
         wheel2 = findGeom(meshNode, "WheelFrontRight");
         wheel22 = findGeom(meshNode1, "WheelFrontRight");
         wheelPos = wheel22.getWorldTranslation();
         node2.attachChild(wheel2);
         wheel2.setMaterial(materials.getMatWheels());
         wheel2.center();
         box = (BoundingBox) wheel2.getModelBound();
         vehicle.addWheel(wheel2.getParent(), carbox.getCenter().add(wheelPos.x*0.3f, -back_wheel_h-0.6f, wheelPos.z*0.3f),
                 wheelDirection, wheelAxle, 0.2f, wheelRadius, false);
 
         Node node3 = new Node("wheel 3 node");
         wheel3 = findGeom(meshNode, "WheelBackLeft");
         wheel32 = findGeom(meshNode1, "WheelBackLeft");
         wheelPos = wheel32.getWorldTranslation();
         wheel3.setMaterial(materials.getMatWheels());
         node3.attachChild(wheel3);
         wheel3.center();
         box = (BoundingBox) wheel3.getModelBound();
         vehicle.addWheel(wheel3.getParent(), carbox.getCenter().add(wheelPos.x*0.3f, -front_wheel_h-0.38f, wheelPos.z*0.3f),
                 wheelDirection, wheelAxle, 0.2f, wheelRadius, true);
 
         Node node4 = new Node("wheel 4 node");
         wheel4 = findGeom(meshNode, "WheelBackRight");
         wheel42 = findGeom(meshNode1, "WheelBackRight");
         wheelPos = wheel42.getWorldTranslation();
         wheel4.setMaterial(materials.getMatWheels());
         node4.attachChild(wheel4);
         wheel4.center();
         box = (BoundingBox) wheel4.getModelBound();
         vehicle.addWheel(wheel4.getParent(), carbox.getCenter().add(wheelPos.x*0.3f, -front_wheel_h-0.38f, wheelPos.z*0.3f),
                 wheelDirection, wheelAxle, 0.2f, wheelRadius, true);
 
         vehicleNode.attachChild(node1);
         vehicleNode.attachChild(node2);
         vehicleNode.attachChild(node3);
         vehicleNode.attachChild(node4);
 
         vehicle.getWheel(0).setFrictionSlip(9.8f);
         vehicle.getWheel(1).setFrictionSlip(9.8f);
         physicsSpace.add(vehicle);
         
         if (effects) {
             initAudio();
         }
         
         vehicle.getPhysicsSpace().addCollisionListener(this);
     }
     
     private void buildVitoFerrari(){
         Node meshNode1 = (Node) assetManager.loadModel("Models/Cars/ferrari/Car.scene");        
         Node meshNode = (Node) assetManager.loadModel("Models/Cars/tempCar/Car.scene");
 
         chasis1 = findGeom(meshNode, "Car");
         chasis12 = findGeom(meshNode1, "Car");
         chasis12.setLocalTranslation(chasis12.getWorldTranslation().x*0.3f, 
                                      chasis12.getWorldTranslation().y*0.3f+1.2f, 
                                      chasis12.getWorldTranslation().z*0.3f);
         chasis12.setLocalScale(chasis12.getWorldScale().mult(0.3f));
         chasis1.rotate(0, 3.135f, 0);
         chasis12.rotate(0, 3.135f, 0);
         
         
         chasis12.setMaterial(materials.getMatChasis());
         
         CollisionShape carHull = CollisionShapeFactory.createDynamicMeshShape(chasis1);
         BoundingBox carbox = (BoundingBox) chasis1.getModelBound();
 
         //create vehicle node
         vehicleNode = new Node("vehicleProtaNode");
         vehicle = new VehicleControl(carHull, 400);
         vehicleNode.addControl(vehicle);
         vehicleNode.attachChild(chasis12);
         
         Geometry cristal = findGeom(meshNode1, "cristal");
         cristal.setLocalTranslation(cristal.getWorldTranslation().x*0.3f,
                                     cristal.getWorldTranslation().y*0.3f+1.2f,
                                     cristal.getWorldTranslation().z*0.3f+1.2f);
         cristal.setLocalScale(cristal.getWorldScale().mult(0.3f));
         cristal.rotate(0, 3.135f, 0);
         vehicleNode.attachChild(cristal);
         
         vehicle.setSuspensionCompression(carSettings.getCompValue() * 2.0f * FastMath.sqrt(carSettings.getStiffness()));
         vehicle.setSuspensionDamping(carSettings.getDampValue() * 2.0f * FastMath.sqrt(carSettings.getStiffness()));
         vehicle.setSuspensionStiffness(carSettings.getStiffness());
         vehicle.setMaxSuspensionForce(carSettings.getMaxSuspensionForce());
 
         //Create four wheels and add them at their locations
         Vector3f wheelDirection = new Vector3f(0, -1, 0); // was 0, -1, 0
         Vector3f wheelAxle = new Vector3f(-1, 0, 0); // was -1, 0, 0
         float radius = 0.5f;
         float restLength = 0.3f;
         float yOff = 0.5f;
         float xOff = 1f;
         float zOff = 2f;
 
 
         Node node1 = new Node("wheel 1 node");
         wheel1 = findGeom(meshNode, "WheelFrontLeft");
         wheel12 = findGeom(meshNode1, "WheelFrontLeft");
         Vector3f wheelPos = wheel12.getWorldTranslation();
         wheel1.setMaterial(materials.getMatWheels());
         node1.attachChild(wheel1);
         wheel1.center();
         BoundingBox box = (BoundingBox) wheel1.getModelBound();
         wheelRadius = box.getYExtent();
         float back_wheel_h = (wheelRadius * 1.7f) - 1f;
         float front_wheel_h = (wheelRadius * 1.9f) - 1f;
         vehicle.addWheel(wheel1.getParent(), carbox.getCenter().add(wheelPos.x*0.3f, -back_wheel_h-0.6f, wheelPos.z*0.3f-0.4f),
                 wheelDirection, wheelAxle, 0.2f, wheelRadius, false);
 
         Node node2 = new Node("wheel 2 node");
         wheel2 = findGeom(meshNode, "WheelFrontRight");
         wheel22 = findGeom(meshNode1, "WheelFrontRight");
         wheelPos = wheel22.getWorldTranslation();
         node2.attachChild(wheel2);
         wheel2.setMaterial(materials.getMatWheels());
         wheel2.center();
         box = (BoundingBox) wheel2.getModelBound();
         vehicle.addWheel(wheel2.getParent(), carbox.getCenter().add(wheelPos.x*0.3f, -back_wheel_h-0.6f, wheelPos.z*0.3f-0.4f),
                 wheelDirection, wheelAxle, 0.2f, wheelRadius, false);
 
         Node node3 = new Node("wheel 3 node");
         wheel3 = findGeom(meshNode, "WheelBackLeft");
         wheel32 = findGeom(meshNode1, "WheelBackLeft");
         wheelPos = wheel32.getWorldTranslation();
         wheel3.setMaterial(materials.getMatWheels());
         node3.attachChild(wheel3);
         wheel3.center();
         box = (BoundingBox) wheel3.getModelBound();
         vehicle.addWheel(wheel3.getParent(), carbox.getCenter().add(wheelPos.x*0.3f, -front_wheel_h-0.38f, wheelPos.z*0.3f-0.4f),
                 wheelDirection, wheelAxle, 0.2f, wheelRadius, true);
 
         Node node4 = new Node("wheel 4 node");
         wheel4 = findGeom(meshNode, "WheelBackRight");
         wheel42 = findGeom(meshNode1, "WheelBackRight");
         wheelPos = wheel42.getWorldTranslation();
         wheel4.setMaterial(materials.getMatWheels());
         node4.attachChild(wheel4);
         wheel4.center();
         box = (BoundingBox) wheel4.getModelBound();
         vehicle.addWheel(wheel4.getParent(), carbox.getCenter().add(wheelPos.x*0.3f, -front_wheel_h-0.38f, wheelPos.z*0.3f-0.4f),
                 wheelDirection, wheelAxle, 0.2f, wheelRadius, true);
 
         vehicleNode.attachChild(node1);
         vehicleNode.attachChild(node2);
         vehicleNode.attachChild(node3);
         vehicleNode.attachChild(node4);
 
         vehicle.getWheel(0).setFrictionSlip(12.0f);
         vehicle.getWheel(1).setFrictionSlip(12.0f);
         physicsSpace.add(vehicle);
         
         if (effects) {
             initAudio();
         }
         
         vehicle.getPhysicsSpace().addCollisionListener(this);
     }
     
     private void buildFerrari() {
         Node meshNode = (Node) assetManager.loadModel("Models/tempCar/Car.scene");
 
         chasis1 = findGeom(meshNode, "Car");
         chasis1.rotate(0, 3.135f, 0);
         chasis1.setMaterial(materials.getMatChasis());
         
         CollisionShape carHull = CollisionShapeFactory.createDynamicMeshShape(chasis1);
         BoundingBox box = (BoundingBox) chasis1.getModelBound();
 
         //create vehicle node
         vehicleNode = new Node("vehicleNode");
         vehicle = new VehicleControl(carHull, 400);
         vehicleNode.addControl(vehicle);
         vehicleNode.attachChild(chasis1);
         
         vehicle.setSuspensionCompression(carSettings.getCompValue() * 2.0f * FastMath.sqrt(carSettings.getStiffness()));
         vehicle.setSuspensionDamping(carSettings.getDampValue() * 2.0f * FastMath.sqrt(carSettings.getStiffness()));
         vehicle.setSuspensionStiffness(carSettings.getStiffness());
         vehicle.setMaxSuspensionForce(carSettings.getMaxSuspensionForce());
 
         //Create four wheels and add them at their locations
         Vector3f wheelDirection = new Vector3f(0, -1, 0); // was 0, -1, 0
         Vector3f wheelAxle = new Vector3f(-1, 0, 0); // was -1, 0, 0
         float radius = 0.5f;
         float restLength = 0.3f;
         float yOff = 0.5f;
         float xOff = 1f;
         float zOff = 2f;
 
 
         Node node1 = new Node("wheel 1 node");
         wheel1 = findGeom(meshNode, "WheelFrontLeft");
         wheel1.setMaterial(materials.getMatWheels());
         node1.attachChild(wheel1);
         wheel1.center();
         box = (BoundingBox) wheel1.getModelBound();
         wheelRadius = box.getYExtent();
         float back_wheel_h = (wheelRadius * 1.7f) - 1f;
         float front_wheel_h = (wheelRadius * 1.9f) - 1f;
         vehicle.addWheel(wheel1.getParent(), box.getCenter().add(0, -back_wheel_h, -0.5f),
                 wheelDirection, wheelAxle, 0.2f, wheelRadius, false);
 
         Node node2 = new Node("wheel 2 node");
         wheel2 = findGeom(meshNode, "WheelFrontRight");
         node2.attachChild(wheel2);
         wheel2.setMaterial(materials.getMatWheels());
         wheel2.center();
         box = (BoundingBox) wheel2.getModelBound();
         vehicle.addWheel(wheel2.getParent(), box.getCenter().add(0, -back_wheel_h, -0.5f),
                 wheelDirection, wheelAxle, 0.2f, wheelRadius, false);
 
         Node node3 = new Node("wheel 3 node");
         wheel3 = findGeom(meshNode, "WheelBackLeft");
         wheel3.setMaterial(materials.getMatWheels());
         node3.attachChild(wheel3);
         wheel3.center();
         box = (BoundingBox) wheel3.getModelBound();
         vehicle.addWheel(wheel3.getParent(), box.getCenter().add(0, -front_wheel_h, -0.4f),
                 wheelDirection, wheelAxle, 0.2f, wheelRadius, true);
 
         Node node4 = new Node("wheel 4 node");
         wheel4 = findGeom(meshNode, "WheelBackRight");
         wheel4.setMaterial(materials.getMatWheels());
         node4.attachChild(wheel4);
         wheel4.center();
         box = (BoundingBox) wheel4.getModelBound();
         vehicle.addWheel(wheel4.getParent(), box.getCenter().add(0, -front_wheel_h, -0.4f),
                 wheelDirection, wheelAxle, 0.2f, wheelRadius, true);
 
         vehicleNode.attachChild(node1);
         vehicleNode.attachChild(node2);
         vehicleNode.attachChild(node3);
         vehicleNode.attachChild(node4);
 
         vehicle.getWheel(0).setFrictionSlip(9.8f);
         vehicle.getWheel(1).setFrictionSlip(9.8f);
         physicsSpace.add(vehicle);
         
         if (effects) {
             initAudio();
         }
     }
         
     
     private void initAudio() {
         starting_car_sound = new Audio(vehicleNode, assetManager, "starting_car.wav");
         accelerate_sound = new Audio(vehicleNode, assetManager, "accelerate_sound.wav");
         decelerate_sound = new Audio(vehicleNode, assetManager, "decelerate_sound.wav");
         max_velocity_sound = new Audio(vehicleNode, assetManager, "max_velocity_sound.wav", true);
         idling_car_sound = new Audio(vehicleNode, assetManager, "idling_car_sound.wav", true);
         String brakes[] = new String[2];
         brakes[0] = "brake_sound.wav";
         brakes[1] = "brake_sound3.wav";
         brake_sounds = new LlistaReproduccio(vehicleNode, assetManager, brakes, true);
         brake_sounds.playNext();
         brake_sounds.stop();
 
         starting_car_sound.play();
     }
     
     public void pauseEffects() {
         paused = true;
         if (effects) {
             if (starting_car_sound.isPlaying()) {
                 starting_car_sound.pause();
             }
             if (accelerate_sound.isPlaying()) {
                 accelerate_sound.pause();
             }
             if (decelerate_sound.isPlaying()) {
                 decelerate_sound.pause();
             }
             if (max_velocity_sound.isPlaying()) {
                 max_velocity_sound.pause();
             }
             if (idling_car_sound.isPlaying()) {
                 idling_car_sound.pause();
             }
             if (brake_sounds.isPlaying()) {
                 brake_sounds.pause();
             }
         }
     }
     
     public void unPauseEffects() {
         paused = false;
         if (effects) {
             if (starting_car_sound.isPaused()) {
                 starting_car_sound.play();
             }
             if (accelerate_sound.isPaused()) {
                 accelerate_sound.play();
             }
             if (decelerate_sound.isPaused()) {
                 decelerate_sound.play();
             }
             if (max_velocity_sound.isPaused()) {
                 max_velocity_sound.play();
             }
             if (idling_car_sound.isPaused()) {
                 idling_car_sound.play();
             }
             if (brake_sounds.isPaused()) {
                 brake_sounds.unPause();
             }
         }
     }
     
     public void setEffects(boolean effects) {
         this.effects = effects;
     }
     
     public Vector3f getPosicio() {
         return this.vehicle.getPhysicsLocation();
     }
     
     public void setPosicioCarrera(int p){
         posicioCarrera=p;
     }
     
     public int getPosicioCarrera(){
         return posicioCarrera;
     }
     
     public int getNumVoltes(){
         return numVoltes;
     }
     public int getEstatControlVolta(){
         return estatControlVolta;
     }
     public float getDistanciaEstatControlVolta() {
         return getDistancia(puntControlVolta);
     }
     
     public float getDistancia (Vector3f punt) { /*busquem la distancia del rival al pto del parametre*/
         float distancia;
         distancia= punt.distance(this.getPosicio());
         return distancia;
     }
     
     public VehicleControl getVehicle() {
         return vehicle;
     }
 
     public Spatial getSpatial() {
         return (Spatial) vehicleNode;
     }
 
     public void turnLeft(boolean value) {
         if (value) {
             carSettings.setSteeringValue(carSettings.getSteeringValue() + .2f);
         } else {
             carSettings.setSteeringValue(carSettings.getSteeringValue() - .2f);
         }
         vehicle.steer(carSettings.getSteeringValue());
     }
 
     public void turnRight(boolean value) {
         if (value) {
             carSettings.setSteeringValue(carSettings.getSteeringValue() - .2f);
         } else {
             carSettings.setSteeringValue(carSettings.getSteeringValue() + .2f);
         }
         vehicle.steer(carSettings.getSteeringValue());
     }
 
     public void forward(boolean value) {
         if (!paused && effects) {
             soundForward(value);
         }
         carSettings.setAccelerationValue(0);
         if (value) {
             forwardMode = true;
             if(!handBrakeMode){
                 reverseMode = false;
                 carSettings.setAccelerationValue(carSettings.getAccelerationValue()+
                         (carSettings.getAccelerationForce() * carSettings.getAccelerationFactor()));
             }
             vehicle.accelerate(carSettings.getAccelerationValue());
         } else {
             if(!handBrakeMode && carSettings.getAccelerationValue()!=0){
                 carSettings.setAccelerationValue(carSettings.getAccelerationValue()-
                         (carSettings.getAccelerationForce() * carSettings.getAccelerationFactor()));
             } 
             vehicle.accelerate(carSettings.getAccelerationValue());
             forwardMode = false;
         }
     }
     
     
     private void soundForward(boolean value) {
         idling_car_sound.stop();
         max_velocity_sound.stop();
         float speed = getSpeed();
         if (value) {
             decelerate_sound.stop();
             if (speed > 190) {
                 accelerate_sound.play(10.5f);
                 max_velocity_sound.play();
             }
             else if (speed > -5) {
                 if (speed < 1) {
                     accelerate_sound.play(0.0f);
                 }
                 else {
                     accelerate_sound.play(speed/19.0f);
                 }
             }
             else {
                 brake_sounds.playNext(); 
             }
         }
         else if (!value) {
             accelerate_sound.stop();
             if (speed > 190) {
                 decelerate_sound.play(0.0f);
             }
             else if (speed < 10) {
                 decelerate_sound.play(10.5f);
                 idling_car_sound.play();
             }
             else {
                 decelerate_sound.play(10.5f - speed/19.0f);
             }
         }
     }
 
     public void back(boolean value) {
         float valueBrake;
         if(!handBrakeMode){
             if (value) {
                 reverse();
             } else {
                 reverseMode = false;
                 vehicle.accelerate(0f);
                 brake(0f);
             }
         }
         if (!paused && effects) {
             soundBack(value);
         }
     }
     
     private void soundBack(boolean value) {
         if (value) {
             brake_sounds.playNext();
             accelerate_sound.stop();
             decelerate_sound.stop();
             idling_car_sound.stop();
         }
         else {
             brake_sounds.stop();
         }
         if (getSpeed() < 5 || (10.5f - getSpeed()/20.0f) < 1.0f) {
             idling_car_sound.play();
         }
         else {
             decelerate_sound.play(10.5f - getSpeed()/20.0f);
         }
     }
 
     public void reset(boolean value, Vector3f pos, Quaternion rot) {
         if (value) {
             vehicle.setPhysicsLocation(pos);
             vehicle.setPhysicsRotation(rot);
             vehicle.setLinearVelocity(Vector3f.ZERO);
             vehicle.setAngularVelocity(Vector3f.ZERO);
             vehicle.resetSuspension();
             carSettings.setAccelerationValue(0);
             carSettings.setSteeringValue(0);
             reverseMode = false;
             handBrakeMode = false;
             forwardMode = false;
             estatControlVolta = 1;
             vehicle.accelerate(0f);
             vehicle.steer(0);
             //tambe resettejem el chasis
             collisionCounter = 0;
             vehicleNode.detachChild(chasis12);
             Node meshNode1 = null;
             if(idModelCar == 1){
                 meshNode1 = (Node) assetManager.loadModel("Models/Cars/ferrari/Car.scene");
             } else if (idModelCar == 2) {
                meshNode1 = (Node) assetManager.loadModel("Models/Cars/golf/Car.scene");
             }
             chasis12 = findGeom(meshNode1, "Car");
             chasis12.setLocalTranslation(chasis12.getWorldTranslation().x*0.3f, 
                                          chasis12.getWorldTranslation().y*0.3f+1.2f, 
                                          chasis12.getWorldTranslation().z*0.3f);
             chasis12.setLocalScale(chasis12.getWorldScale().mult(0.3f));
             chasis1.rotate(0, 3.135f, 0);
             chasis12.rotate(0, 3.135f, 0);
 
 
             chasis12.setMaterial(materials.getMatChasis());
             vehicleNode.attachChild(chasis12);
             
         } else {
         }
     }
 
     public void reverse() {
         if (!paused && effects) {
             idling_car_sound.play();
         }
         float valueBrake;
         if (getSpeed() > 5) {
             valueBrake = carSettings.getBrakeForce() / carSettings.getBrakeForceFactor();
             brake(valueBrake);
         }
         reverseMode = true;
         carSettings.setAccelerationValue((float)(carSettings.getAccelerationValue()-
                         (carSettings.getAccelerationForce() * carSettings.getReverseFactor())));
         vehicle.accelerate(carSettings.getAccelerationValue());
     }
 
     public void brake(float valueBrake) {
         vehicle.brake(valueBrake);
     }
     
     public void handBrake(boolean value){
         float valueBrake;
         if(!reverseMode){
             if(value){
                 handBrakeMode = true;
                 if(forwardMode && carSettings.getAccelerationValue()!=0){
                     carSettings.setAccelerationValue(carSettings.getAccelerationValue()-
                         (carSettings.getAccelerationForce() * carSettings.getAccelerationFactor()));
                     vehicle.accelerate(carSettings.getAccelerationValue()); 
                 }
                 //valueBrake = brakeForce;
                 //brake(valueBrake);
                 //vehicle.brake(vehicle.getWheel(0)., brakeForce*100);
                 vehicle.brake(0, carSettings.getBrakeForce()*5);
                 vehicle.brake(1, carSettings.getBrakeForce()*5);
             } else {
                 handBrakeMode = false; 
                 //brake(0f); 
                 vehicle.brake(0, 0);
                 vehicle.brake(1, 0);
                 if(forwardMode){
                     forward(true);
                 }
             }
         }else{
             if(value){
                 carSettings.setAccelerationValue((float)(carSettings.getAccelerationValue()+
                         (carSettings.getAccelerationForce() * carSettings.getReverseFactor())));
                 vehicle.accelerate(carSettings.getAccelerationValue());
                 valueBrake = carSettings.getBrakeForce();
                 brake(valueBrake);
             } else {
                 brake(0f);
                 back(true);
             }
         }
         if (!paused && effects) {
             soundBrake(value);
         }
     }
     
     private void soundBrake(boolean value) {
         if (value) {
             brake_sounds.playNext();
             accelerate_sound.stop();
             decelerate_sound.stop();
             idling_car_sound.stop();
         }
         else {
             brake_sounds.stop();
         }
         if (getSpeed() < 5 || (10.5f - getSpeed()/20.0f) < 1.0f) {
             idling_car_sound.play();
         }
         else {
             decelerate_sound.play(10.5f - getSpeed()/20.0f);
         }
     }
     
     public float getSpeed(){
         return vehicle.getCurrentVehicleSpeedKmHour();
     }
 
     public Vector3f getInitialPos() {
         return initialPos;
     }
 
     public void setInitialPos(Vector3f initialPos) {
         this.initialPos = initialPos;
     }
 
     public Quaternion getInitialRot() {
         return initialRot;
     }
 
     public void setInitialRot(Quaternion initialRot) {
         this.initialRot = initialRot;
     }
     
     public void upDateMaxSpeed(){
         float speed;
         speed = getSpeed();
         if((reverseMode) && (speed < carSettings.getMaxReverseVelocity())){
             vehicle.accelerate(1f);
         }else if((!reverseMode) && (speed > carSettings.getMaxAccelerateVelocity())){
             vehicle.accelerate(1f);
         } else {
             vehicle.accelerate(carSettings.getAccelerationValue());
             
         }
     }
     
     public Vector3f getPuntControlVolta(){
         return puntControlVolta;
     }
 
     public void collision(PhysicsCollisionEvent event) {
         String colObj = event.getNodeB().getName();
         
         /*if(event.getNodeA().getName().equals("vehicleProtaNode")){
             System.out.println(event.getNodeB().getName());
         }*/
         
         if(getSpeed() > 70.0f && event.getNodeA().getName().equals("vehicleProtaNode") &&
             (colObj.contains("solo_city-scene_node") ||
                 colObj.equals("vehicleNode") ||
                 colObj.equals("World1-scene_node") ||
                 colObj.equals("World3-scene_node") ||
                 colObj.equals("World2-scene_node")))
         
         {
             vehicleNode.detachChild(chasis12);
             Node meshNode1 = null;
             if(idModelCar == 1){
                 if(collisionCounter >= 0 && collisionCounter <= 40){
                     meshNode1 = (Node) assetManager.loadModel("Models/Cars/ferrariDeformed/1/Car.scene");
                 }else if (collisionCounter >= 41 && collisionCounter <= 80){
                     meshNode1 = (Node) assetManager.loadModel("Models/Cars/ferrariDeformed/2/Car.scene");
                 } else if (collisionCounter >= 81){
                     meshNode1 = (Node) assetManager.loadModel("Models/Cars/ferrariDeformed/3/Car.scene");
                 }
             } else if (idModelCar == 2) {
                 if(collisionCounter >= 0 && collisionCounter <= 40){
                     meshNode1 = (Node) assetManager.loadModel("Models/Cars/golfDeformed/1/Car.scene");
                 }else if (collisionCounter >= 41 && collisionCounter <= 80){
                     meshNode1 = (Node) assetManager.loadModel("Models/Cars/golfDeformed/2/Car.scene");
                 } else if (collisionCounter >= 81){
                     meshNode1 = (Node) assetManager.loadModel("Models/Cars/golfDeformed/3/Car.scene");
                 }
             }
             chasis12 = findGeom(meshNode1, "Car");
             chasis12.setLocalTranslation(chasis12.getWorldTranslation().x*0.3f, 
                                          chasis12.getWorldTranslation().y*0.3f+1.2f, 
                                          chasis12.getWorldTranslation().z*0.3f);
             chasis12.setLocalScale(chasis12.getWorldScale().mult(0.3f));
             chasis1.rotate(0, 3.135f, 0);
             chasis12.rotate(0, 3.135f, 0);
 
 
             chasis12.setMaterial(materials.getMatChasis());
             vehicleNode.attachChild(chasis12);
             
             //System.out.println(event.getNodeA().getName()+" --- "+event.getNodeB().getName());
             
             collisionCounter++;
         }
     }
 
     private void loadDeformedModels() {
         meshNodeDef11 = (Node) assetManager.loadModel("Models/Cars/ferrariDeformed/1/Car.scene");
         meshNodeDef12 = (Node) assetManager.loadModel("Models/Cars/ferrariDeformed/2/Car.scene");
         meshNodeDef13 = (Node) assetManager.loadModel("Models/Cars/ferrariDeformed/3/Car.scene");
         meshNodeDef21 = (Node) assetManager.loadModel("Models/Cars/golfDeformed/1/Car.scene");
         meshNodeDef22 = (Node) assetManager.loadModel("Models/Cars/golfDeformed/2/Car.scene");
         meshNodeDef23 = (Node) assetManager.loadModel("Models/Cars/golfDeformed/3/Car.scene");
     }
 }
