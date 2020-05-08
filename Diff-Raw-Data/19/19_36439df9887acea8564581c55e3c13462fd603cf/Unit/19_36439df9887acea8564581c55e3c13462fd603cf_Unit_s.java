    package org.ashpro.temporal.units;
 
    import com.threed.jpct.*;
    import java.io.*;
 
 
    public class Unit extends AbstractEntity {
       private float yRot=0;
       private static char c = File.separatorChar;
       private Object3D leg1 = null, leg2 = null;
       private boolean shieldOn = false;
       static
       {
          Texture body=new Texture("resrc"+c+"textures"+c+"skin.jpg");
          Texture shield=new Texture("resrc"+c+"textures"+c+"shield.jpg");
          WaterTextureEffect wte = new WaterTextureEffect(20);
          shield.setEffect(wte);
          TextureManager.getInstance().addTexture("magician", body);
          TextureManager.getInstance().addTexture("shield", shield);
       }
       public Unit(){
          super(Primitives.getBox(5, 5f));
          leg1 = Primitives.getSphere(20, 4);
          leg2 = Primitives.getSphere(20, 4);
          addChild(leg1);
          addChild(leg2);
          setTexture("magician");
          leg1.setTexture("shield");
          leg2.setTexture("shield");
          leg1.setCollisionMode(Object3D.COLLISION_CHECK_NONE);
          leg2.setCollisionMode(Object3D.COLLISION_CHECK_NONE);
          setEnvmapped(Object3D.ENVMAP_ENABLED);
          leg1.setEnvmapped(Object3D.ENVMAP_ENABLED);
          leg2.setEnvmapped(Object3D.ENVMAP_ENABLED);
         translate(new SimpleVector(0,4,0));
         leg1.translate(new SimpleVector(-1, 4, 0));
         leg2.translate(new SimpleVector(1, 4, 0));
          translateMesh();
          leg1.translateMesh();
          leg2.translateMesh();
          setTranslationMatrix(new Matrix());
          leg1.setTranslationMatrix(new Matrix());
          leg2.setTranslationMatrix(new Matrix());
          build();
          leg1.build();
          leg2.build();
       }
       public boolean place(Object3D ground) {
          SimpleVector dropDown=new SimpleVector(0, 1, 0);
          Matrix rotMat=getRotationMatrix();
          rotMat.setIdentity();
          setRotationMatrix(rotMat);
       
          rotateY(yRot);
          translate(0, -10, 0);
       
          float rightFrontHeight=ground.calcMinDistance(leg1.getTransformedCenter(), dropDown, 4*30);
          float leftFrontHeight=ground.calcMinDistance(leg2.getTransformedCenter(), dropDown, 4*30);
       
          translate(0, 10, 0);
       
          rotMat=getRotationMatrix();
          rotMat.setIdentity();
          setRotationMatrix(rotMat);
       
          if (rightFrontHeight!=Object3D.COLLISION_NONE&&
           leftFrontHeight!=Object3D.COLLISION_NONE) {
          
             rightFrontHeight-=10;
             leftFrontHeight-=10;
             double angleFront=rightFrontHeight-leftFrontHeight;
             double as=(angleFront/(16d));
             angleFront=Math.atan(as);
          
             float rot=(float) ((angleFront*2)/2d);
             rotateZ(rot);
          
             float down=rightFrontHeight;
             if (leftFrontHeight<down) {
                down=leftFrontHeight;
             }
             dropDown.scalarMul(down-4);
             translate(dropDown);
          
          } 
          else {
             return false;
          }
          rotateY(yRot);
          return true;
       }
    
       public void addToWorld(World world) {
          super.addToWorld(world);
          world.addObject(leg1);
          world.addObject(leg2);
       }
       public void turnLeft() {
          yRot-=0.075f;
       }
       public void turnRight() {
          yRot+=0.075f;
       }
       public float getDirection() {
          return yRot;
       }
       public boolean toggleShield() {
          if(shieldOn)
          {
             shieldOn = false;
             leg1.scale(1/14.0F);
             return false;
          }
          else
          {
             shieldOn = true;
             leg1.scale(14.0F);
             return true;
          }
       }
       public boolean isShieldOn() {
          return shieldOn;
       }
    }
