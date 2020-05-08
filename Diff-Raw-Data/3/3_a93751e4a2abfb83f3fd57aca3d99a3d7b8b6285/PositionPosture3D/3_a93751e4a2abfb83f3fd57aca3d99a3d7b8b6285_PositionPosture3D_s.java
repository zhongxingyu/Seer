 package yang.graphics.skeletons.pose;
 
 import yang.model.DebugYang;
 import yang.physics.massaggregation.MassAggregation;
 import yang.physics.massaggregation.elements.Joint;
 import yang.util.Util;
 
 public class PositionPosture3D extends Posture<PositionPosture3D,MassAggregation> {
 
 	public boolean mRelative;
 
 	public PositionPosture3D(float[] data,boolean relative) {
 		super(data);
 		mRelative = relative;
 	}
 
 	public PositionPosture3D(float[] data) {
 		this(data,false);
 	}
 
 	public PositionPosture3D(MassAggregation skeleton,boolean relative) {
 		this(new float[skeleton.calcAnimatedJointCount()*3],relative);
 		for(int i=0;i<mData.length;i++) {
 			mData[i] = Float.MAX_VALUE;
 		}
 	}
 
 	public PositionPosture3D(MassAggregation skeleton) {
 		this(skeleton,false);
 	}
 
 	@Override
 	public void applyPosture(MassAggregation skeleton, PositionPosture3D interpolationPose, float weight) {
 		int c = 0;
 		final float dWeight = 1-weight;
 		for(final Joint joint:skeleton.mJoints) {
 			if(joint.mAnimate) {
 				if(mData[c]!=Float.MAX_VALUE || joint.mParent!=null) {
 					if(joint.mAnimDisabled || mData[c]==Float.MAX_VALUE) {
						if(mRelative && joint.mParent!=null) {DebugYang.stateString(joint.mParentSpatial);
 							joint.mX = joint.mParent.mX + joint.mParentSpatial.mX;
 							joint.mY = joint.mParent.mY + joint.mParentSpatial.mY;
 							joint.mZ = joint.mParent.mZ + joint.mParentSpatial.mZ;
 						}
 						c += 3;
 					}else{
 						if(weight==1 || interpolationPose==null) {
 							joint.mX = mData[c++];
 							joint.mY = mData[c++];
 							joint.mZ = mData[c++];
 						}else{
 							joint.mX = mData[c]*weight + interpolationPose.mData[c]*dWeight;
 							joint.mY = mData[c+1]*weight + interpolationPose.mData[c+1]*dWeight;
 							joint.mZ = mData[c+2]*weight + interpolationPose.mData[c+2]*dWeight;
 							c += 3;
 						}
 						if(mRelative && joint.mParent!=null) {
 							joint.add(joint.mParent);
 						}
 					}
 				}else
 					c += 3;
 			}
 		}
 	}
 
 	@Override
 	public void copyFromSkeleton(MassAggregation skeleton) {
 		int c=0;
 		for(final Joint joint:skeleton.mJoints) {
 			if(joint.mAnimate) {
 				mData[c++] = joint.mX;
 				mData[c++] = joint.mY;
 				mData[c++] = joint.mZ;
 			}
 		}
 	}
 
 	@Override
 	public String toSourceCode() {
 		return "new PositionPose3D(new float[]{"+Util.arrayToString(mData,",",0)+"})";
 	}
 
 	@Override
 	public void clear() {
 		for(int c=0;c<mData.length;c++)
 			mData[c] = Float.MAX_VALUE;
 	}
 
 	@Override
 	public void clear(int id) {
 		mData[id*3] = Float.MAX_VALUE;
 	}
 
 }
