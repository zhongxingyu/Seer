 package yang.graphics.defaults.meshes.loaders;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import yang.graphics.defaults.meshes.scenes.LimbObject;
 import yang.graphics.defaults.meshes.scenes.MeshDeformer;
 import yang.graphics.defaults.meshes.scenes.MeshObject;
 import yang.graphics.defaults.meshes.scenes.SceneObject;
 import yang.graphics.translator.AbstractGraphics;
 import yang.math.MathConst;
 import yang.math.objects.Point3f;
 import yang.math.objects.Quaternion;
 import yang.math.objects.Vector3f;
 import yang.math.objects.matrix.YangMatrix;
 import yang.physics.massaggregation.MassAggregation;
 import yang.physics.massaggregation.elements.Joint;
 import yang.physics.massaggregation.elements.JointConnection;
 import yang.util.YangList;
 import yang.util.filereader.TokenReader;
 import yang.util.filereader.exceptions.ParseException;
 import yang.util.filereader.exceptions.UnexpectedTokenException;
 import yang.util.filereader.exceptions.UnknownIdentifierException;
 
 public class FBXLoader extends YangSceneLoader {
 
 	public static final int OBJ_NONE = 0;
 	public static final int OBJ_MESH = 1;
 	public static final int OBJ_LIMB = 2;
 
 	private static float[] tempFloats;
 	private static int[] tempInts;
 
 	public YangList<MeshDeformer> mDeformers = new YangList<MeshDeformer>();
 //	public YangList<MassAggregation> mSkeletons = new YangList<MassAggregation>();
 
 	public YangList<SceneObject> mObjects = new YangList<SceneObject>();
 	public YangList<LimbObject> mLimbObjects = new YangList<LimbObject>();
 	public YangList<MeshObject> mMeshObjects = new YangList<MeshObject>();
 	public SceneObject mRootObject;
 
 	public AbstractGraphics<?> mGraphics;
 	public MeshMaterialHandles mHandles;
 
 	private TokenReader mReader;
 	private float mDefaultJointRadius = Joint.DEFAULT_RADIUS * 0.7f;
 
 	//TEMPS
 	private Vector3f tempVec = new Vector3f();
 	private YangMatrix tempMat = new YangMatrix();
 
 	//private targetObject mTempProperties = new targetObject();
 
 	public FBXLoader(AbstractGraphics<?> graphics, MeshMaterialHandles handles) {
 		super(graphics,handles);
 		if(tempFloats==null) {
 			tempFloats = new float[MAX_VERTICES];
 			tempInts = new int[MAX_VERTICES];
 		}
 		mGraphics = graphics;
 		mHandles = handles;
 		tempMat.initStack(128);
 	}
 
 	private void readPoint3f(Point3f target) throws IOException {
 		mReader.readPoint3f(target);
 	}
 
 	private void readProperties(SceneObject targetObject) throws IOException, UnexpectedTokenException {
 		while(!mReader.eof()) {
 			mReader.nextWord(true);
 			if(mReader.isChar('}'))
 				break;
 			if(!mReader.isWord("Property"))
 				throw new UnexpectedTokenException(mReader, "Property");
 			mReader.nextWord(true);
 			if(mReader.isWord("Lcl Translation")) {
 				mReader.skipWords(2);
 				readPoint3f(targetObject.mTranslation);
 			}else if(mReader.isWord("Lcl Rotation")) {
 				mReader.skipWords(2);
 				readPoint3f(tempVec);
 				tempVec.scale(MathConst.PI/180);
 
 //				targetObject.mOrientation.setFromEuler(tempVec.mX,tempVec.mY,tempVec.mZ);
 				Quaternion quat = targetObject.mOrientation;
 				quat.setIdentity();
 				quat.rotateX(tempVec.mX);
 				quat.rotateY(tempVec.mY);
 				quat.rotateZ(tempVec.mZ);
 			}else if((targetObject instanceof LimbObject) && limbProperty((LimbObject)targetObject)) {
 			//}else if((targetObject instanceof MeshObject) && meshProperty((MeshObject)targetObject)) {
 
 			}else
 				mReader.toLineEnd();
 		}
 	}
 
 	private void skipBracketContent() throws IOException {
 		mReader.toLineEnd();
 		int depth = 1;
 		while(!mReader.eof()) {
 			mReader.nextWord(true);
 			if(mReader.isChar('{')) {
 				depth++;
 			}else if(mReader.isChar('}')) {
 				depth--;
 				if(depth<=0)
 					return;
 			}
 		}
 	}
 
 	private boolean limbProperty(LimbObject limbObj) throws IOException {
 		if(mReader.isWord("LimbLength")) {
 			mReader.skipWords(2);
 			limbObj.mLimbLength = mReader.readFloat(true);
 		}else
 			return false;
 
 		return true;
 	}
 
 	private boolean meshKeyword(MeshObject meshObj) throws IOException,ParseException {
 		if(mReader.isWord("Vertices")) {
 			posId = mReader.readArray(workingPositions,posId);
 			mVertexCount = posId/3;
 		}else if(mReader.isWord("PolygonVertexIndex")) {
 			int baseIndex = -1;
 			int lstIndex = -1;
 			int c = 0;
 			while(!mReader.eof()) {
 				mReader.nextWord(true);
 				int val = mReader.wordToInt();
 				if(val==TokenReader.ERROR_INT)
 					break;
 				c++;
 				if(baseIndex<0) {
 					baseIndex = val;
 				}else{
 					boolean polyEnd = val<0;
 					if(lstIndex>=0) {
 
 						workingIndices[mIndexId++] = (short)(baseIndex);
 						workingIndices[mIndexId++] = (short)(lstIndex);
 
 						if(polyEnd) {
 							baseIndex = -1;
 							lstIndex = -1;
 							val = -val-1;
 							c = 0;
 						}
 						workingIndices[mIndexId++] = (short)(val);
 
 						//}
 					}
 					if(!polyEnd && baseIndex>=0)
 						lstIndex = val;
 				}
 
 			}
 
 		}else if(mReader.isWord("LayerElementUV")) {
 			mReader.nextWord(true);
 			mReader.expect("{");
 			while(!mReader.eof()) {
 				mReader.nextWord(true);
 				if(mReader.isWord("}"))
 					break;
 				if(mReader.isWord("UV")) {
 					texId = mReader.readArray(workingTexCoords,texId);
 				}else
 					mReader.toLineEnd();
 			}
 		}else
 			return false;
 		return true;
 	}
 
 	private void readObjects() throws IOException, ParseException {
 		while(!mReader.eof()) {
 			mReader.nextWord(true);
 			if(mReader.isWord("}"))
 				break;
 			else if(mReader.isWord("Model")) {
 				//NEW MODEL
 				mReader.nextWord(true);
 				String name = mReader.wordToString();
 				//mReader.expect(",");
 				mReader.nextWord(true);
 				int objType = OBJ_NONE;
 				if(mReader.isWord("Limb"))
 					objType = OBJ_LIMB;
 				if(mReader.isWord("Mesh"))
 					objType = OBJ_MESH;
 				//mReader.expect("{");
 
 				SceneObject newObj;
 				MeshObject meshObj = null;
 				LimbObject limbObj = null;
 				if(objType==OBJ_LIMB) {
 //					Joint joint = new Joint(name.split("::")[1],null,mTempProperties.mTranslation,mDefaultJointRadius,mMassAggregation);
 //					mMassAggregation.addJoint(joint);
 //					System.out.println(joint);
 					newObj = new LimbObject();
 					limbObj = (LimbObject)newObj;
 					mLimbObjects.add(limbObj);
 				}else if(objType==OBJ_MESH) {
 					newObj = new MeshObject();
 					meshObj = (MeshObject)newObj;
 					meshObj.mMesh = startLoadingMesh();
 					mMeshObjects.add(meshObj);
 				}else
 					newObj = new SceneObject();
 				mObjects.add(newObj);
 				newObj.mName = name.substring(7);
 
 				while(!mReader.eof()) {
 					mReader.nextWord(true);
 					if(mReader.isWord("}")) {
 						break;
 					}else if(objType==OBJ_MESH && meshKeyword(meshObj)) {
 					//}else if(objType==OBJ_LIMB && limbProperty(limbObj)) {
 					}else if(mReader.isWord("Properties60")) {
 						mReader.expect("{");
 						readProperties(newObj);
 					}else if(mReader.startsWith("Layer")){
 						skipBracketContent();
 					}else
 						mReader.toLineEnd();
 				}
 				if(objType==OBJ_MESH) {
 					finishLoadingMesh(false,false);
 				}
 			}else if(mReader.isWord("Deformer")) {
 				mReader.skipSpace(true);
 				mReader.nextWord(true);
 				if(mReader.isWord("SubDeformer")) {
 					mReader.skipChar();
 					mReader.nextWord(true);
 					if(mReader.isWord("Cluster")) {
 						mReader.nextWord(true);
 						SceneObject obj1 = findObject(0);
 						mReader.nextWord(true);
 						SceneObject obj2 = findObject(0);
 						if((obj1 instanceof MeshObject) && (obj2 instanceof LimbObject)) {
 							MeshObject meshObj = (MeshObject)obj1;
 							LimbObject limbObj = (LimbObject)obj2;
 
 							mReader.expect("Cluster");
 							mReader.expect("{");
 							MeshDeformer deformer = limbObj.addDeformer(meshObj);
 							int count = 0;
 							while(!mReader.eof()) {
 								mReader.nextWord(true);
 								if(mReader.isWord("}")) {
 									break;
 								}
 								if(mReader.isWord("Properties60")) {
 									skipBracketContent();
 								}else if(mReader.isWord("Indexes")) {
 									count = mReader.readArray(tempInts,0);
 								}else if(mReader.isWord("Weights")) {
 									count = mReader.readArray(tempFloats,0);
 								}else
 									mReader.toLineEnd();
 							}
 							deformer.init(count);
 							deformer.copyFrom(tempInts,tempFloats);
 							mDeformers.add(deformer);
 						}else{
 							throw new ParseException(mReader,"Only Mesh-Bone deformers allowed");
 						}
 					}
 				}
 			}else if(mReader.isWord("Material")) {
 				skipBracketContent();
 			}else if(mReader.isWord("Pose")) {
 				skipBracketContent();
 			}else if(mReader.isWord("GlobalSettings")) {
 				skipBracketContent();
 			}else{
 				skipBracketContent();
 			}
 
 		}
 	}
 
 	private SceneObject findObject(int startAt) {
 		int l = mReader.mWordLength-startAt;
 		for(SceneObject obj:mObjects) {
 			if(l==obj.mName.length() && mReader.endsWith(obj.mName)) {
 				return obj;
 			}
 		}
 		return null;
 	}
 
 	private void readConnections() throws IOException, ParseException {
 		while(!mReader.eof()) {
 			mReader.nextWord(true);
 			if(mReader.isWord("}"))
 				break;
 			if(mReader.isWord("Connect")) {
 				mReader.expect("OO");
 				mReader.nextWord(true);
 				if(mReader.startsWith("Model")) {
 					SceneObject obj1 = findObject(7);
 					if(obj1==null)
 						throw new UnknownIdentifierException(mReader,mReader.wordToString().substring(7));
 					mReader.nextWord(true);
 					if(mReader.startsWith("Model")) {
 						SceneObject obj2 = findObject(7);
 						if(obj2==null)
 							throw new UnknownIdentifierException(mReader,mReader.wordToString().substring(7));
 						obj1.setParent(obj2);
 					}
 				}
 			}
 		}
 	}
 
 	public boolean load(String filename) throws IOException, ParseException {
 	//	mGFXLoader = gfxLoader;
 		InputStream stream = mGFXLoader.mResources.getAssetInputStream(filename);
 		if(stream==null)
 			return false;
 		mReader = new TokenReader(stream);
 	//	mChars = mReader.mCharBuffer;
 		mReader.setLineCommentChars(";");
 		mReader.mAutoSkipComments = true;
 		mReader.mWordBreakers[':'] = true;
 		mReader.mWordBreakers[','] = true;
 		mReader.mWordBreakers['"'] = true;
 		mReader.mWhiteSpaces[','] = true;
 
 		mMeshObjects.clear();
 		mDeformers.clear();
 
 		mRootObject = new SceneObject();
 		mRootObject.mName = "Scene";
 		mObjects.add(mRootObject);
 
 		while(!mReader.eof()) {
 			mReader.nextWord(true);
 			if(mReader.isWord("Objects")) {
 				mReader.expect("{");
 				readObjects();
 			}else if(mReader.isWord("Connections")) {
 				mReader.expect("{");
 				readConnections();
 			}else if(mReader.isWord("Takes")) {
 				skipBracketContent();
 			}else if(mReader.startsWith("Version")) {
 				skipBracketContent();
 			}else if(mReader.startsWith("Relations")) {
 				skipBracketContent();
 			}
 
 		}
 
 		refreshGlobalTransforms();
 
 		return true;
 	}
 
 	private float getRadius(float limbLength) {
 		return Math.min(limbLength*0.35f,mDefaultJointRadius);
 	}
 
 	public void subSkel(MassAggregation targetSkeleton,LimbObject baseObj,YangMatrix transform,Joint parentJoint) {
 		if(parentJoint==null) {
 			parentJoint = new Joint("root_"+baseObj.mName);
 			parentJoint.set(baseObj.mTranslation);
 			parentJoint.applyTransform(transform);
 			targetSkeleton.addJoint(parentJoint);
 			parentJoint.setRadius(getRadius(baseObj.mLimbLength));
 		}
 		transform.stackPush();
 		baseObj.multTransform(transform);
		YangMatrix transf = new YangMatrix();
		transf.set(baseObj.mGlobalTransform);
		transf.translate(baseObj.mLimbLength,0,0);
 		transform.translate(baseObj.mLimbLength,0,0);
 		Joint joint = new Joint(baseObj.mName);
		joint.applyTransform(transf);
 		targetSkeleton.addJoint(joint);
 		joint.setRadius(getRadius(baseObj.getMinAdjescentLimbLength()));
 		joint.setParent(parentJoint);
 		transform.translate(-baseObj.mLimbLength,0,0);
 		baseObj.setDeformerIndex(joint.mId);
 
 		targetSkeleton.addSpringBone(new JointConnection(baseObj.mName,joint,parentJoint));
 
 //		transform.stackPop();
 //		transform.stackPush();
 //		transform.translate(baseObj.mTranslation);
 //		transform.translate(baseObj.mBoneLength,0,0);
 
 		for(SceneObject obj:baseObj.getChildren()) {
 			if(obj instanceof LimbObject) {
 				LimbObject limbObj = (LimbObject)obj;
 				subSkel(targetSkeleton,limbObj,transform,joint);
 			}
 		}
 		transform.stackPop();
 	}
 
 	public void refreshGlobalTransform(SceneObject object,YangMatrix parentTransform) {
 		parentTransform.stackPush();
 		object.multTransform(parentTransform);
 		object.mGlobalTransform.set(parentTransform);
 		for(SceneObject child:object.getChildren()) {
 			refreshGlobalTransform(child,parentTransform);
 		}
 		parentTransform.stackPop();
 	}
 
 	public void refreshGlobalTransforms(YangMatrix initialTransform) {
		initialTransform.stackPush();
 		for(SceneObject obj:mObjects) {
			if(obj.mParent==null)
				refreshGlobalTransform(obj,initialTransform);
 		}
		initialTransform.stackPop();
 	}
 
 	public void refreshGlobalTransforms() {
 		tempMat.loadIdentity();
 		refreshGlobalTransforms(tempMat);
 	}
 
 	public void createSkeleton(MassAggregation targetSkeleton) {
 		YangMatrix matrix = new YangMatrix();
 		matrix.initStack(64);
 
 		for(SceneObject obj:mRootObject.getChildren()) {
 			if(obj instanceof LimbObject) {
 				obj.multTransform(matrix);
 				for(SceneObject boneObj:obj.getChildren()) {
 					if(boneObj instanceof LimbObject) {
 						subSkel(targetSkeleton,(LimbObject)boneObj,matrix,null);
 					}
 				}
 			}
 		}
 
 //		matrix.loadIdentity();
 //		matrix.swapLines(0,1);
 //		matrix.rotateY(MathConst.PI);
 //		matrix.rotateZ(MathConst.PI/2);
 //		targetSkeleton.transformJointPositions(matrix);
 	}
 
 	public void createSkinWeights() {
 		for(MeshDeformer deformer:mDeformers) {
 			YangMesh mesh = deformer.mMesh.mMesh;
 			if(!mesh.hasArmatureWeights()) {
 				//mesh.initArmatureWeights();
 				//mesh.createNeutralArmatureWeights();
 				mesh.setZeroArmatureWeights();
 			}
 			int l = deformer.getVertexCount();
 			for(int i=0;i<l;i++) {
 				int vertId = deformer.mIndices[i];
 				float weight = deformer.mWeights[i];
 				mesh.addArmatureWeight(vertId,deformer.mMapIndex,weight);
 			}
 
 		}
 
 		for(MeshObject meshObj:mMeshObjects) {
 			meshObj.mMesh.normalizeArmatureWeights();
 		}
 	}
 
 	public MassAggregation createSkeleton() {
 		MassAggregation skel = new MassAggregation();
 		createSkeleton(skel);
 		return skel;
 	}
 
 //	public void applyTransformations() {
 //		tempMat.loadIdentity();
 //		for(SceneObject obj:mObjects) {
 //
 //		}
 //	}
 
 	@Override
 	public String toString() {
 		StringBuilder result = new StringBuilder();
 		boolean first = true;
 		for(SceneObject obj:mObjects) {
 			if(!first)
 				result.append(',');
 			else
 				first = false;
 			result.append(obj.hierarchyToString());
 		}
 		return result.toString();
 	}
 
 }
