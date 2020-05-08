 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package model;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.bounding.BoundingBox;
 import com.jme3.bullet.PhysicsSpace;
 import com.jme3.bullet.collision.shapes.BoxCollisionShape;
 import com.jme3.bullet.collision.shapes.CollisionShape;
 import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
 import com.jme3.bullet.control.VehicleControl;
 import com.jme3.bullet.util.CollisionShapeFactory;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Matrix3f;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.Camera;
 import com.jme3.scene.CameraNode;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.control.CameraControl;
 
 /**
 *
 * @author Sergi
 */
 public class VehicleProtagonista {
 
     private float mass;
     private VehicleControl vehicle;
     private MaterialsVehicle materials;
     private Geometry chasis1;
     private Geometry wheel1;
     private Geometry wheel3;
     private Geometry wheel2;
     private Geometry wheel4;
     private float wheelRadius;
     private AssetManager assetManager;
     private CameraNode camNode;
     private Node vehicleNode;
     private PhysicsSpace physicsSpace;
     private Camera cam;
     //Propiedades del coche
     private float steeringValue = 0;
     private float accelerationValue = 0;
     private final float accelerationForce = 1000.0f;
     private final float brakeForce = 100.0f;
     //Factores para disminuir y aumentar la acceleracion y la frenadas
     private int accelerationFactor = 2; //Factor multiplicativo
     private int brakeForceFactor = 1;   //Factor de division
     private double reverseFactor = 1.5;    //Factor de multiplicacio
     //Variable per saber si estas en mode normal o marcha atras.
     private boolean reverseMode = false;
     private boolean handBrakeMode  = false;
     private boolean forwardMode = false;
     
     private Audio accelerate_sound;
     private Audio decelerate_sound;
     private Audio max_velocity_sound;
     private Audio brake_sound;
     private Audio idling_car_sound;
     
     private int maxAccelerateVelocity = 120;
     private int maxReverseVelocity = -50;
 
     public VehicleProtagonista(AssetManager asset, PhysicsSpace phy, Camera cam) {
         assetManager = asset;
         physicsSpace = phy;
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
         
         buildCar();
     }
 
     public void buildCar() {
         mass = 400;
         
         //create a compound shape and attach the BoxCollisionShape for the car body at 0,1,0
         //this shifts the effective center of mass of the BoxCollisionShape to 0,-1,0
         //CompoundCollisionShape compoundShape = new CompoundCollisionShape();
 
         Node meshNode = (Node) assetManager.loadModel("Models/tempCar/Car.scene");
 
         chasis1 = findGeom(meshNode, "Car");
         chasis1.rotate(0, 3.135f, 0);
         chasis1.setMaterial(materials.getMatChasis());
         
         CollisionShape carHull = CollisionShapeFactory.createDynamicMeshShape(chasis1);
         BoundingBox box = (BoundingBox) chasis1.getModelBound();
         //BoxCollisionShape box = new BoxCollisionShape(new Vector3f(1.2f, 0.5f, 2.4f));
         //compoundShape.addChildShape(box, new Vector3f(0, 1, 0));
 
         //create vehicle node
         vehicleNode = new Node("vehicleNode");
         vehicle = new VehicleControl(carHull, mass);
         vehicleNode.addControl(vehicle);
 
 
         //Geometry glass = findGeom(meshNode, "Cube2");
 
         //Spatial chasis = (Spatial)assetManager.loadModel("Models/Cube.mesh.xml");
         vehicleNode.attachChild(chasis1);
         //vehicleNode.attachChild(glass);
 
 
         //setting suspension values for wheels, this can be a bit tricky
         //see also https://docs.google.com/Doc?docid=0AXVUZ5xw6XpKZGNuZG56a3FfMzU0Z2NyZnF4Zmo&hl=en
         float stiffness = 200.0f;//200=f1 car
         float compValue = .2f; //(should be lower than damp)
         float dampValue = .3f;
         vehicle.setSuspensionCompression(compValue * 2.0f * FastMath.sqrt(stiffness));
         vehicle.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
         vehicle.setSuspensionStiffness(stiffness);
         vehicle.setMaxSuspensionForce(10000.0f);
 
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
 
         //rootNode.attachChild(vehicleNode);
 
         physicsSpace.add(vehicle);
         
         initAudio();
         //set forward camera node that follows the character
         //camNode = new CameraNode("CamNode", cam);
         //camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
         //camNode.setLocalTranslation(new Vector3f(0, 4, -15));
         //camNode.setLocalTranslation(new Vector3f(-15, 15, -15));
         //camNode.setLocalTranslation(new Vector3f(-15, 15, -15));
         //camNode.lookAt(vehicleNode.getLocalTranslation(), Vector3f.UNIT_Y);
         //vehicleNode.attachChild(camNode);
 
 
 
     }
      public void initAudio() {
         accelerate_sound = new Audio(vehicleNode, assetManager, "accelerate_sound.wav");
         decelerate_sound = new Audio(vehicleNode, assetManager, "decelerate_sound.wav");
         max_velocity_sound = new Audio(vehicleNode, assetManager, "max_velocity_sound.wav", true);
         brake_sound = new Audio(vehicleNode, assetManager, "brake_sound.wav");
         idling_car_sound = new Audio(vehicleNode, assetManager, "idling_car_sound.wav", true);
     }
     
     
     public VehicleControl getVehicle() {
         return vehicle;
     }
 
     public Spatial getSpatial() {
         return (Spatial) vehicleNode;
     }
 
     public void turnLeft(boolean value) {
         if (value) {
             steeringValue += .5f;
         } else {
             steeringValue += -.5f;
         }
         vehicle.steer(steeringValue);
     }
 
     public void turnRight(boolean value) {
         if (value) {
             steeringValue += -.5f;
         } else {
             steeringValue += .5f;
         }
         vehicle.steer(steeringValue);
     }
 
     public void forward(boolean value) {
         soundForward(value);
         accelerationValue = 0;
         if (value) {
             forwardMode = true;
             if(!handBrakeMode){
                 reverseMode = false;
                 accelerationValue += (accelerationForce * accelerationFactor);                
             }
             vehicle.accelerate(accelerationValue);
         } else {
             if(!handBrakeMode && accelerationValue!=0){
                 accelerationValue -= (accelerationForce * accelerationFactor);
             } 
             vehicle.accelerate(accelerationValue);
             forwardMode = false;
         }
     }
     
     public void soundForward(boolean value) {
         float speed = getSpeed();
         if (value && speed > 190) {
             decelerate_sound.stop();
             accelerate_sound.play(10.5f);
             max_velocity_sound.play();
         }
         if (value && speed > -5) {
             decelerate_sound.stop();
            accelerate_sound.play(speed/20.0f);
         }
         else if (!value) {
             accelerate_sound.stop();
            decelerate_sound.play(10.0f - speed/20.0f);
         }
     }
 
     public void back(boolean value) {
         brake_sound.play();
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
     }
 
     public void reset(boolean value, Vector3f pos, Quaternion rot) {
         if (value) {
             vehicle.setPhysicsLocation(pos);
             vehicle.setPhysicsRotation(rot);
             vehicle.setLinearVelocity(Vector3f.ZERO);
             vehicle.setAngularVelocity(Vector3f.ZERO);
             vehicle.resetSuspension();
             accelerationValue = 0;
             steeringValue = 0;
             reverseMode = false;
             handBrakeMode = false;
             forwardMode = false;
             vehicle.accelerate(0f);
         } else {
         }
     }
 
     public void reverse() {
         idling_car_sound.play();
         float valueBrake;
         if (getSpeed() > 5) {
             valueBrake = brakeForce / brakeForceFactor;
             brake(valueBrake);
         }
         reverseMode = true;
         accelerationValue -= (accelerationForce * reverseFactor);
         vehicle.accelerate(accelerationValue);
     }
 
     public void brake(float valueBrake) {
         vehicle.brake(valueBrake);
     }
     
     public void handBrake(boolean value){
         brake_sound.play();
         float valueBrake;
         if(!reverseMode){
             if(value){
                 handBrakeMode = true;
                 if(forwardMode && accelerationValue!=0){
                     accelerationValue -= (accelerationForce * accelerationFactor);
                     vehicle.accelerate(accelerationValue); 
                 }
                 //valueBrake = brakeForce;
                 //brake(valueBrake);
                 //vehicle.brake(vehicle.getWheel(0)., brakeForce*100);
                 vehicle.brake(0, brakeForce*5);
                 vehicle.brake(1, brakeForce*5);
             } else {;
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
                 accelerationValue += (accelerationForce * reverseFactor);
                 vehicle.accelerate(accelerationValue);
                 valueBrake = brakeForce;
                 brake(valueBrake);
             } else {
                 brake(0f);
                 back(true);
             }
         } 
     }
 
     public void setReverseMode(boolean value) {
         reverseMode = value;
     }
 
     public boolean getReverseMode() {
         return reverseMode;
     }
 
     public float getSpeed(){
         //return vehicle.getLinearVelocity().length();
         //System.out.println("Speed "+vehicle.getCurrentVehicleSpeedKmHours());
         return vehicle.getCurrentVehicleSpeedKmHour();
         //return (float)Math.sqrt((Math.pow(vehicle.getLinearVelocity().x,2)+Math.pow(vehicle.getLinearVelocity().z,2)+Math.pow(vehicle.getLinearVelocity().y,2)));
     }
     
     public void upDateMaxSpeed(){
         float speed;
         speed = getSpeed();
         if((reverseMode) && (speed < maxReverseVelocity)){
             Vector3f vec = vehicle.getLinearVelocity();
             //vec.x = vec.x - 1;
             vehicle.setLinearVelocity(vec);
         }else if((!reverseMode) && (speed > maxAccelerateVelocity) && (forwardMode)){
             Vector3f vec = vehicle.getLinearVelocity();
             //vec.x = vec.x - 1;
             vehicle.setLinearVelocity(vec);
         }
     }
 }
