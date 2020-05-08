 package twoverse.util;
 
 import processing.core.PApplet;
 
 public class Camera {
     private float mEyeX; // position of the camera in space
     private float mEyeY;
     private float mEyeZ;
     private float mCenterX; // where the eye is looking (center of screen)
     private float mCenterY;
     private float mCenterZ;
    private float mScale;
     private PApplet mParent;
 
     public Camera(PApplet parent, float eyeX, float eyeY, float eyeZ,
            float centerX, float centerY, int centerZ, float scale) {
         mEyeX = eyeX;
         mEyeY = eyeY;
         mEyeZ = eyeZ;
         mCenterX = centerX;
         mCenterY = centerY;
         mCenterZ = centerZ;
        mScale = scale;
         mParent = parent;
     }
 
     public void setCamera() {
         mParent.camera(mEyeX,
                 mEyeY,
                 mEyeZ,
                 (float) (mParent.width / 2.0),
                 (float) (mParent.height / 2.0),
                 0,
                 0,
                 1,
                 0);

        mParent.translate(mParent.width / 2,
                mParent.height / 2);
        mParent.scale(mScale);
        mParent.translate(-mParent.width / 2, -mParent.height / 2);
         mParent.translate(mCenterX, mCenterY);
        
     }
 
     public void moveEye(float differenceX, float differenceY, float differenceZ) {
         mEyeX += differenceX;
         mEyeY += differenceY;
         mEyeZ += differenceZ;
     }
 
    public void zoom(float difference) {
        mScale += difference;
    }

     public void moveCenter(float differenceX, float differenceY,
             float differenceZ) {
         mCenterX += differenceX;
         mCenterY += differenceY;
         mCenterZ += differenceZ;
         mCenterZ = PApplet.constrain(mCenterZ, 0, (float) 1.0);
     }
 }
