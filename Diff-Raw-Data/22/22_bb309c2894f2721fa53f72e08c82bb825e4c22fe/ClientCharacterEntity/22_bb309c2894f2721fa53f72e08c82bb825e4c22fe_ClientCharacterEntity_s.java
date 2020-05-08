 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package mygame;
 
 import com.jme3.animation.AnimControl;
 import com.jme3.animation.Bone;
 import com.jme3.math.FastMath;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Node;
 
 /**
  *
  * @author blah
  */
 public class ClientCharacterEntity extends CharacterEntity {
     protected Vector3f prevUpArmAngle;
     long timeOfLastUpdate=0;
 
     public ClientCharacterEntity(Node model){
         super(model);
     }
 
     @Override
     public void onRemoteUpdate(float latencyDelta){
 
         if(upperArmAngle!=null){
        //     setTransforms(upperArmAngle.z);
             float extrapolatedSelfAngle,angleDiff,extrapolatedForeignAngle,newUpperArmAngle;
             if(prevUpArmAngle!=null){
                extrapolatedSelfAngle=extrapolateAngles(prevUpArmAngle.z,((float)(System.currentTimeMillis()-timeOfLastUpdate)/1000));
      //          System.out.println("extrapolatedAngle:"+extrapolatedAngle+",actualAngle:"+upperArmAngle.z);
                 extrapolatedForeignAngle=extrapolateAngles(upperArmAngle.z,latencyDelta);
                 angleDiff=upperArmAngle.z-extrapolatedSelfAngle;
 
                if(Math.abs(angleDiff%360)<1){
                     newUpperArmAngle=extrapolatedSelfAngle;
                 }
                 else{
                     newUpperArmAngle=extrapolatedForeignAngle;
                 }
 
                 upperArmAngle=new Vector3f(0,0,newUpperArmAngle);
             }
 
             
             prevUpArmAngle=upperArmAngle.clone();
    
 
             
 
             upperArmAngle=new Vector3f(0,0,extrapolateAngles(upperArmAngle.z,latencyDelta));
             timeOfLastUpdate=System.currentTimeMillis();
         }
      //   System.out.println("onRemoteUpdate");
     }
 
     private void setTransforms(float upArmAngle){
     //   System.out.println("upArmAngles is "+this.currentUpArmAngle[0]+","+this.currentUpArmAngle[1]+","+this.currentUpArmAngle[2]);
         Quaternion q = new Quaternion();
         q.fromAngles(0, upArmAngle, 0);
     //    System.out.println("in remote update");
         Bone bone=this.model.getControl(AnimControl.class).getSkeleton().getBone("UpArmL");
         bone.setUserControl(true);
         bone.setUserTransforms(Vector3f.ZERO, q, Vector3f.UNIT_XYZ);
     }
 
     private float interpolateAngles(float angle1,float angle2,float blendAmount){
         return angle1+blendAmount*(angle2-angle1);
     }
 
     @Override
     public void onLocalUpdate(){
         if(prevUpArmAngle!=null){
             setTransforms(extrapolateAngles(prevUpArmAngle.z,((float)(System.currentTimeMillis()-timeOfLastUpdate)/1000)));
         }
 //       System.out.println("delta:"+delta);
     }
 
     @Override
     public void interpolate(float blendAmount){
         if (prevUpArmAngle != null && upperArmAngle != null) {
             float tempUpArmAngle[] = {0, 0, 0};
 
             tempUpArmAngle[2] = interpolateAngles(prevUpArmAngle.z, upperArmAngle.z, blendAmount);
 
             setTransforms(tempUpArmAngle[2]);
         }
     }
 
     @Override
     public void extrapolate(float tpf){
         if(prevUpArmAngle!=null){
             setTransforms(extrapolateAngles(prevUpArmAngle.z,tpf));
         }
     }
 
     public float extrapolateAngles(float currentAngle,float tpf){
        
         float tempUpArmAngle[]={0,0,0};
         tempUpArmAngle[2]=currentAngle+(FastMath.HALF_PI / 2f) * tpf * 10f * upperArmVelocity.z;
         
         return tempUpArmAngle[2];
     }
 }
