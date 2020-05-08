 package yang.graphics.camera;
 
 import yang.math.objects.Vector3f;
 import yang.math.objects.YangMatrix;
 
 public class DefaultCamera extends YangCamera {
 
	protected YangMatrix mProjectionTransform = new YangMatrix();
	protected YangMatrix mInvProjectionTransform = new YangMatrix();
	protected YangMatrix mViewTransform = new YangMatrix();

 	protected boolean mProjectionUpdated = true;
 
 	float[] mTempMat = new float[16];
 
 	@Override
 	public void calcTransformations() {
 //		if(target.mPostCameraTransform!=null) {
 //			target.mPostCameraTransform.getTranslation(target.mPosition);
 //			target.mPosition.add(mPosition);
 //			target.mPostCameraTransform.asInverted(mTempMat);
 //			target.mViewTransform.multiply(mTempMat,mViewTransform.mValues);
 //		}else{
 //			target.mPosition.set(mPosition);
 //			target.mViewTransform.set(mViewTransform);
 //		}
 //		target.mViewProjectTransform.multiply(mProjectionTransform,target.mViewTransform);
 //
 //		target.mViewTransform.asInverted(target.mCameraTransform.mValues);
 //		target.mViewProjectTransform.asInverted(target.mUnprojectCameraTransform.mValues);
 
 		mViewProjectTransform.multiply(mProjectionTransform,mViewTransform);
 		mViewProjectTransform.asInverted(mUnprojectCameraTransform.mValues);
 		mViewTransform.asInverted(mCameraTransform.mValues);
 		//mCameraTransform.getTranslation(mPosition);
 	}
 
 	@Override
 	public void getRightVector(Vector3f target) {
 		final float[] mat = mViewTransform.mValues;
 		target.mX = mat[0];
 		target.mY = mat[4];
 		target.mZ = mat[8];
 	}
 
 	@Override
 	public void getUpVector(Vector3f target) {
 		final float[] mat = mViewTransform.mValues;
 		target.mX = mat[1];
 		target.mY = mat[5];
 		target.mZ = mat[9];
 	}
 
 	@Override
 	public void getForwardVector(Vector3f target) {
 		final float[] mat = mViewTransform.mValues;
 		target.mX = mat[2];
 		target.mY = mat[6];
 		target.mZ = mat[10];
 	}
 
 	public void setProjectionUpdated() {
 		mProjectionUpdated = true;
 	}
 
 	public YangMatrix getProjectionTransformReference() {
 		return mProjectionTransform;
 	}
 
 	public YangMatrix getInvProjection() {
 		if(mProjectionUpdated)
 			mProjectionTransform.asInverted(mInvProjectionTransform.mValues);
 		return mInvProjectionTransform;
 	}
 
 	@Override
 	public YangMatrix getViewTransformReference() {
 		return mViewTransform;
 	}
 
 }
