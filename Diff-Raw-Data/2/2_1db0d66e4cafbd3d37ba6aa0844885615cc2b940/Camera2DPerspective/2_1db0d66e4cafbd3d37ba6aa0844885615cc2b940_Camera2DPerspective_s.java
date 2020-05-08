 package yang.graphics.camera;
 
 import yang.graphics.camera.projection.PerspectiveProjection;
 import yang.math.objects.YangMatrix;
 
 public class Camera2DPerspective extends DefaultCamera {
 
 	private float mRatio = 1;
 
 	public void setPerspectiveProjection(float fovy, float near, float far,float stretchX) {
 		mNear = near;
 		mFar = far;
 		mRatio = near/PerspectiveProjection.getTransformFovy(mProjectionTransform,fovy, stretchX,1, near, far);
 	}
 
 	public void setPerspectiveProjection(float fovy, float near, float far) {
 		setPerspectiveProjection(fovy,near,far,1);
 	}
 
 	public void setPerspectiveProjection(float fovy) {
 		setPerspectiveProjection(fovy,PerspectiveProjection.DEFAULT_NEAR,PerspectiveProjection.DEFAULT_FAR);
 	}
 
 	public void set(Camera2D camera2D) {
 		//TODO invert
 //		YangMatrix temp = new YangMatrix();
 //		temp.translate(camera2D.getPositionReference());
 //		temp.rot
		float zoom = camera2D.mPosition.mZ;
 		mPosition.mX = camera2D.mPosition.mX;
 		mPosition.mY = camera2D.mPosition.mY;
 		mPosition.mZ = mRatio*zoom;
 
 //		mViewTransform.setTranslationNegative(mPosition);
 		mCameraTransform.setTranslation(mPosition);
 		if(camera2D.mRotation!=0) {
 			mCameraTransform.rotateZ(camera2D.mRotation);
 		}
 
 		if(mPostUnprojection==null)
 			mPostUnprojection = new YangMatrix();
 
 		float ratio = 2f/(mFar-mNear);
 		mPostUnprojection.loadIdentity();
 //		mPostUnprojection.translate(0,0,-1);
 //		mPostUnprojection.scale(0,0,ratio);
 //		mPostUnprojection.translate(0,0,-mNear);
 //		mPostUnprojection.translate(0,0,mPosition.mZ);
 //		mPostUnprojection.translate(0,0,0.95f);
 		mProjectionUpdated = true;
 	}
 
 }
