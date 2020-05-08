 package yang.graphics.skeletons.defaults;
 
 import yang.graphics.buffers.IndexedVertexBuffer;
 import yang.graphics.defaults.DefaultGraphics;
 import yang.graphics.defaults.geometrycreators.grids.GridCreator;
 import yang.graphics.model.FloatColor;
 import yang.graphics.textures.TextureCoordinatesQuad;
 import yang.math.objects.YangMatrix;
 import yang.physics.massaggregation.MassAggregation;
 import yang.physics.massaggregation.constraints.ColliderConstraint;
 import yang.physics.massaggregation.elements.Joint;
 import yang.physics.massaggregation.elements.JointConnection;
 
 public class JointGridCreator {
 
 	public float mJointMass = 1.5f;
 	public MassAggregation mMassAggregation;
 	public String mJointNamePrefix = "grid_";
 	public String mBoneNamePrefix = "grid_";
 	public Joint[][] mJoints;
 	public float mStrength = 40;
 	public float mFriction = 0.98f;
 	public float mJointRadius = 0.1f;
 
 	//Drawing
 	public GridCreator<?> mGridDrawer;
 
 	private int mColCount,mRowCount;
 	private float mRatio;
 
 	public MassAggregation create(int countX,int countY,YangMatrix transform) {
 		if(transform==null)
 			transform = YangMatrix.IDENTITY;
 		mColCount = countX;
 		mRowCount = countY;
 		mJoints = new Joint[countY][countX];
 		if(countX<2 || countY<2)
 			throw new RuntimeException("countX and countY must be larger or equal 2.");
 
 		if(mMassAggregation==null) {
 			mMassAggregation = new MassAggregation();
 			mMassAggregation.mLowerLimit = -128;
 		}
 
		mRatio = (float)(countX-1)/(countY-1);
 
 		for(int j=0;j<countY;j++) {
 			float y = (float)j/(countY-1);
 			Joint prevJoint = null;
 			for(int i=0;i<countX;i++) {
 				Joint newJoint = new Joint(mJointNamePrefix+j+"-"+i);
 				float x = (float)i/(countX-1) * mRatio;
 				transform.apply3D(x,y,0, newJoint);
 				newJoint.setInitialValues();
 				mJoints[j][i] = newJoint;
 				newJoint.mRadius = mJointRadius;
 				mMassAggregation.addJoint(newJoint);
 				JointConnection boneX = null;
 				JointConnection boneY = null;
 				if(i>0)
 					boneX = mMassAggregation.addSpringBone(new JointConnection(mBoneNamePrefix+j+"-"+(i-1)+"_"+j+"-"+i, prevJoint,newJoint), mStrength);
 				if(j>0)
 					boneY = mMassAggregation.addSpringBone(new JointConnection(mBoneNamePrefix+(j-1)+"-"+i+"_"+j+"-"+i, mJoints[j-1][i],newJoint), mStrength);
 				if(i>0 && j>0) {
 //					GridConstraint gridConstraint = new GridConstraint(boneX,boneY);
 //					mMassAggregation.addConstraint(gridConstraint);
 					mMassAggregation.addSpringBone(new JointConnection(mBoneNamePrefix+(j-1)+"-"+(i-1)+"_"+j+"-"+i, mJoints[j-1][i-1],newJoint), mStrength);
 					mMassAggregation.addSpringBone(new JointConnection(mBoneNamePrefix+(j-1)+"-"+i+"_"+j+"-"+(i-1), mJoints[j-1][i],mJoints[j][i-1]), mStrength);
 				}
 				prevJoint = newJoint;
 				newJoint.mFriction = mFriction;
 			}
 		}
 
 		return mMassAggregation;
 	}
 
 	public Joint getJoint(int indexX,int indexY) {
 		return mJoints[indexY][indexX];
 	}
 
 	public int getColumnCount() {
 		return mColCount;
 	}
 
 	public int getRowCount() {
 		return mRowCount;
 	}
 
 	public void addCollider(Joint collJoint) {
 		for(Joint[] row:mJoints) {
 			for(Joint joint:row) {
 				mMassAggregation.addConstraint(new ColliderConstraint(collJoint,joint));
 			}
 		}
 	}
 
 	public void setRowFixed(float row,boolean fixed) {
 		int rowId = normToRow(row);
 		Joint[] jointRow = mJoints[rowId];
 		for(Joint joint:jointRow) {
 			joint.mFixed = fixed;
 		}
 	}
 
 	public void setColumnFixed(float column,boolean fixed) {
 		int colId = normToColumn(column);
 		for(int i=0;i<mRowCount;i++) {
 			mJoints[i][colId].mFixed = fixed;
 		}
 	}
 
 	public int normToRow(float normRow) {
 		return (int)(normRow*(mRowCount-1)+0.5f);
 	}
 
 	public int normToColumn(float normColumn) {
 		return (int)(normColumn*(mColCount-1)+0.5f);
 	}
 
 	public float rowToNorm(int rowIndex) {
 		return (float)rowIndex/(mColCount-1);
 	}
 
 	public float columnToNorm(int columnIndex) {
 		return (float)columnIndex/(mColCount-1)*mRatio;
 	}
 
 	public float getRatio() {
 		return mRatio;
 	}
 
 	//--------DRAWING--------
 
 	public int getVertexCount() {
 		return mColCount*mRowCount;
 	}
 
 	public void initGraphics(DefaultGraphics<?> graphics) {
 		mGridDrawer = new GridCreator<DefaultGraphics<?>>(graphics);
 		mGridDrawer.init(mColCount,mRowCount, 1,1);
 	}
 
 	public void putPositions() {
 		if(mGridDrawer==null)
 			throw new RuntimeException("Graphics not initialized");
 		IndexedVertexBuffer vBuffer = mGridDrawer.mGraphics.mCurrentVertexBuffer;
 		for(Joint[] row:mJoints) {
 			for(Joint joint:row) {
 				vBuffer.putVec3(DefaultGraphics.ID_POSITIONS, joint.mWorldPosition);
 			}
 		}
 	}
 
 	public void drawDefault(FloatColor color,TextureCoordinatesQuad texCoords) {
 		if(texCoords==null)
 			texCoords = TextureCoordinatesQuad.FULL_TEXTURE;
 		mGridDrawer.beginDraw();
 		mGridDrawer.putIndices();
 		putPositions();
 		mGridDrawer.putGridColor(color.mValues);
 		mGridDrawer.putGridSuppData(FloatColor.BLACK.mValues);
 		mGridDrawer.putGridTextureRect(texCoords);
 		mGridDrawer.putNormals();
 	}
 
 	public void drawDefault() {
 		drawDefault(FloatColor.WHITE,TextureCoordinatesQuad.FULL_TEXTURE);
 	}
 
 	public Joint pickJoint(float normColumn,float normRow) {
 		return mJoints[normToRow(normRow)][normToColumn(normColumn)];
 	}
 
 }
