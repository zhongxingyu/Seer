 package yang.graphics.defaults;
 
 import java.nio.FloatBuffer;
 import java.nio.ShortBuffer;
 
 import yang.graphics.buffers.IndexedVertexBuffer;
 import yang.graphics.buffers.UniversalVertexBuffer;
 import yang.graphics.camera.CameraProjection;
 import yang.graphics.camera.YangCamera;
 import yang.graphics.defaults.geometrycreators.StripCreator;
 import yang.graphics.font.BitmapFont;
 import yang.graphics.font.DrawableString;
 import yang.graphics.model.FloatColor;
 import yang.graphics.programs.BasicProgram;
 import yang.graphics.textures.TextureCoordinatesQuad;
 import yang.graphics.translator.AbstractGraphics;
 import yang.graphics.translator.GraphicsTranslator;
 import yang.graphics.util.VertexZSort;
 import yang.math.MatrixOps;
 import yang.math.objects.Point3f;
 import yang.math.objects.Quadruple;
 import yang.math.objects.YangMatrix;
 import yang.model.Rect;
 import yang.util.Util;
 
 public abstract class DefaultGraphics<ShaderType extends BasicProgram> extends AbstractGraphics<ShaderType> {
 
 	public static final float[] RECT_POSITIONS = {0,0,0, 1,0,0, 0,1,0, 1,1,0};
 	public static final float[][] DEFAULT_NEUTRAL_ELEMENTS = { { 0, 0, 0 }, { 0, 0 }, { 1, 1, 1, 1 }, { 0, 0, 0, 0 } };
 
 	public static final float[] RECT_TEXTURECOORDS = { 0.0f,1.0f, 1.0f,1.0f, 0.0f,0.0f, 1.0f,0.0f};
 	public static final float[] RECT_TEXTURECOORDS_INV = { 0.0f,0.0f, 1.0f,0.0f, 0.0f,1.0f, 1.0f,1.0f};
 	public static final TextureCoordinatesQuad RECT_TEXQUAD = new TextureCoordinatesQuad().init(0, 0, 1, 1);
 
 	public static final float[] RECT_WHITE = { 1,1,1,1, 1,1,1,1, 1,1,1,1, 1,1,1,1 };
 	public static final float[] RECT_BLACK = { 0,0,0,1, 0,0,0,1, 0,0,0,1, 0,0,0,1 };
 	public static final float[] RECT_ZERO = { 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0 };
 
 	public static final int ID_POSITIONS = 0;
 	public static final int ID_TEXTURES = 1;
 	public static final int ID_COLORS = 2;
 	public static final int ID_SUPPDATA = 3;
 	public static final int ID_NORMALS = 4;
 
 	public static final float DEFAULTBIAS = 0.0018f;
 	public static final float DEFAULTTEXBIAS = 0;
 	public static final float[] NULLVECTOR = { 0, 0, 0, 0 };
 	public static final float[] BLACK = { 0, 0, 0, 1 };
 	public static final float[] WHITE = { 1, 1, 1, 1 };
 
 	public static final float[] FLOAT_ZERO_12 = {0,0,0,0,0,0,0,0,0,0,0,0};
 
 	public StripCreator mDefaultStripCreator;
 
 	protected float[] mInterArray = new float[1024];
 
 	//Properties
 	public BitmapFont mDefaultFont;
 	public boolean mAutoRefreshCameraTransform = true;
 
 	//State
 	public float mCurrentZ;
 	public float[] mColorFactor;
 
 	// Buffers
 	protected DrawableString mInterString = new DrawableString(2048);
 	public int mPositionDimension;
 	public ShortBuffer mIndexBuffer;
 	public FloatBuffer mPositions;
 	public FloatBuffer mTextures;
 	public FloatBuffer mColors;
 	public FloatBuffer mSuppData;
 	public FloatBuffer mNormals;
 
 	//Objects
 	protected CameraProjection mCameraProjection;
 	protected YangMatrix mLeftEyeTranform;
 	protected YangMatrix mRightEyeTranform;
 	protected VertexZSort mVertexSort;
 
 	//Temp
 	private Point3f mTempPnt1 = new Point3f();
 
 	public void shareBuffers(DefaultGraphics<?> graphics) {
 		initDynamicBuffer();
 		mDynamicVertexBuffer.linkBuffer(ID_POSITIONS, graphics.mDynamicVertexBuffer, ID_POSITIONS);
 		mDynamicVertexBuffer.linkBuffer(ID_TEXTURES, graphics.mDynamicVertexBuffer, ID_TEXTURES);
 		mDynamicVertexBuffer.linkBuffer(ID_COLORS, graphics.mDynamicVertexBuffer, ID_COLORS);
 		mDynamicVertexBuffer.linkBuffer(ID_SUPPDATA, graphics.mDynamicVertexBuffer, ID_SUPPDATA);
 	}
 
 	public DefaultGraphics(GraphicsTranslator translator, int positionBytes) {
 		super(translator);
 		mPositionDimension = positionBytes;
 		mDefaultStripCreator = new StripCreator(this);
 		mColorFactor = new float[4];
 	}
 
 	@Override
 	public void restart() {
 		super.restart();
 		setColorFactor(1);
 	}
 
 	@Override
 	protected void derivedInit() {
 		setColorFactor(1);
 		mCameraProjection = new CameraProjection();
 		mViewProjectionTransform = mCameraProjection.getViewProjReference();
 		mInvViewProjectionTransform = mCameraProjection.getUnprojCameraReference();
 	}
 
 	@Override
 	protected int[] getBufferElementSizes() {
 		return new int[] { 3, 2, 4, 4 };
 	}
 
 	@Override
 	protected float[][] getNeutralBufferElements() {
 		return DEFAULT_NEUTRAL_ELEMENTS;
 	}
 
 	public void beginQuad(boolean wireFrames) {
 		mCurrentVertexBuffer.beginQuad(wireFrames);
 	}
 
 	@Override
 	public void bindBuffers() {
 		assert mTranslator.preCheck("bind buffers 2D");
 		mTranslator.setAttributeBuffer(mCurrentProgram.mPositionHandle, DefaultGraphics.ID_POSITIONS);
 		if (mCurrentProgram.mHasTextureCoords)
 			mTranslator.setAttributeBuffer(mCurrentProgram.mTextureHandle, DefaultGraphics.ID_TEXTURES);
 		if (mCurrentProgram.mHasColor)
 			mTranslator.setAttributeBuffer(mCurrentProgram.mColorHandle, DefaultGraphics.ID_COLORS);
 		if (mCurrentProgram.mHasSuppData)
 			mTranslator.setAttributeBuffer(mCurrentProgram.mSuppDataHandle, DefaultGraphics.ID_SUPPDATA);
 		assert mTranslator.checkErrorInst("Bind buffers 2D");
 	}
 
 	@Override
 	public void enableBuffers() {
 		assert mTranslator.checkErrorInst("PRE enable buffers 2D");
 		mTranslator.enableAttributePointer(mCurrentProgram.mPositionHandle);
 		if (mCurrentProgram.mHasTextureCoords)
 			mTranslator.enableAttributePointer(mCurrentProgram.mTextureHandle);
 		if (mCurrentProgram.mHasColor)
 			mTranslator.enableAttributePointer(mCurrentProgram.mColorHandle);
 		if (mCurrentProgram.mHasSuppData)
 			mTranslator.enableAttributePointer(mCurrentProgram.mSuppDataHandle);
 		assert mTranslator.checkErrorInst("Enable buffers 2D");
 		//if(!mTranslator.checkErrorInst("Enable buffers 2D"))
 			//System.err.println("Handles: "+mCurrentProgram.mPositionHandle+" "+mCurrentProgram.mTextureHandle+" "+mCurrentProgram.mColorHandle+" "+mCurrentProgram.mSuppDataHandle);
 	}
 
 	@Override
 	public void disableBuffers() {
 		assert mTranslator.checkErrorInst("PRE disable buffers 2D");
 		mTranslator.disableAttributePointer(mCurrentProgram.mPositionHandle);
 		if (mCurrentProgram.mHasTextureCoords)
 			mTranslator.disableAttributePointer(mCurrentProgram.mTextureHandle);
 		if (mCurrentProgram.mHasColor)
 			mTranslator.disableAttributePointer(mCurrentProgram.mColorHandle);
 		if (mCurrentProgram.mHasSuppData)
 			mTranslator.disableAttributePointer(mCurrentProgram.mSuppDataHandle);
 		assert mTranslator.checkErrorInst("Disable buffers 2D");
 	}
 
 	protected void refreshViewTransform() {
 		mCameraProjectionMatrix.set(mCurViewProjTransform);
 		mCameraProjectionMatrix.postScale(1f/mTranslator.mCurrentSurface.getSurfaceRatioX(),1f/mTranslator.mCurrentSurface.getSurfaceRatioY(),1);
 	}
 
 //	protected void updateCameraProjectionPostTransform() {
 //		if(mTranslator.mCurrentSurface.getViewPostTransform()!=null) {
 //			mCameraProjection.mPostCameraTransform.set(mTranslator.mCurrentSurface.getViewPostTransform());
 //		}else
 //			mCameraProjection.mPostCameraTransform.loadIdentity();
 //	}
 
 	public void setCamera(YangCamera camera) {
 		YangMatrix trafo = mTranslator.mCurrentSurface.getViewPostTransform();
 		mCameraProjection.copyFrom(camera,trafo);
 	}
 
 	public void getUnprojection(YangMatrix target) {
 		refreshViewTransform();
 		target.set(mCurViewProjTransform);
 		if (mWorldTransformEnabled)
 			target.multiplyRight(mWorldTransform);
 //		target.postScale(mTranslator.mRatioX, mTranslator.mRatioY, 1);
 	}
 
 	protected void updateProgramProjection() {
 		assert mTranslator.preCheck("Set program projection");
 		final BasicProgram program = mCurrentProgram;
 		if (program != null) {
 			if(mCurViewProjTransform == mTranslator.mProjScreenTransform) {
 				mCameraProjectionMatrix.set(mTranslator.mProjScreenTransform);
 				YangMatrix trafo = mTranslator.mCurrentSurface.getViewPostTransform();
 				if(trafo!=null) {
 					trafo.getTranslation(mTempPnt1);
 					float f = get2DStereoShiftFactor(mNoCameraTransformStereoDistance);
 					mCameraProjectionMatrix.postTranslate(-mTempPnt1.mX*f,-mTempPnt1.mY*f);
 				}
 			}else
 				refreshViewTransform();
 			if (mWorldTransformEnabled) {
 				if (program.mHasWorldTransform) {
 					program.setWorldTransform(mWorldTransform.mValues);
 					program.setProjection(mCameraProjectionMatrix.mValues);
 				} else {
 					mResultTransformationMatrix.multiply(mCameraProjectionMatrix, mWorldTransform);
 					program.setProjection(mResultTransformationMatrix.mValues);
 				}
 			} else {
 				program.setProjection(mCameraProjectionMatrix.mValues);
 				if (program.mHasWorldTransform)
 					program.setWorldTransform(MatrixOps.IDENTITY);
 			}
 			if(program.mNormalTransformHandle>=0) {
 				refreshNormalTransform();
 				program.setNormalTransform(mNormalTransform);
 			}
 		}
 	}
 
 	@Override
 	public boolean setShaderProgram(ShaderType program) {
 		assert mTranslator.preCheck("Set shader program");
 		if (super.setShaderProgram(program)) {
 			if(program.mHasColorFactor)
 				program.setColorFactor(mColorFactor);
 			program.setTime(mTime);
 			assert mTranslator.checkErrorInst("Set shader program");
 			return true;
 		} else
 			return false;
 	}
 
 	public float readPosition() {
 		return mPositions.get();
 	}
 
 	public void readColor(float[] target) {
 		if (target == null) {
 			mColors.get();
 			mColors.get();
 			mColors.get();
 			mColors.get();
 		} else {
 			target[0] = mColors.get();
 			target[1] = mColors.get();
 			target[2] = mColors.get();
 			target[3] = mColors.get();
 		}
 	}
 
 	// ---RECTS---
 
 	public void drawQuad(YangMatrix worldTransform, YangMatrix textureTransform) {
 		mCurrentVertexBuffer.beginQuad();
 		putTransformedPositionRect(worldTransform);
 		putTransformedTextureRect(textureTransform);
 		putColorRect(mCurColor);
 		putSuppDataRect(mCurSuppData);
 	}
 
 	public void drawQuad(float worldX1,float worldY1,float worldX2,float worldY2,float worldX3,float worldY3,float worldX4,float worldY4, TextureCoordinatesQuad texCoordinates) {
 		mCurrentVertexBuffer.beginQuad();
 		putPosition(worldX1,worldY1);
 		putPosition(worldX2,worldY2);
 		putPosition(worldX3,worldY3);
 		putPosition(worldX4,worldY4);
 		if(texCoordinates!=null)
 			putTextureArray(texCoordinates.mAppliedCoordinates);
 		else
 			putTextureArray(RECT_TEXTURECOORDS);
 		putColorRect(mCurColor);
 		putSuppDataRect(mCurSuppData);
 	}
 
 	public void drawQuad(YangMatrix worldTransform, TextureCoordinatesQuad texCoordinates) {
 		mCurrentVertexBuffer.beginQuad();
 		putTransformedPositionRect(worldTransform);
 		if(texCoordinates!=null)
 			putTextureArray(texCoordinates.mAppliedCoordinates);
 		else
 			putTextureArray(RECT_TEXTURECOORDS);
 		putColorRect(mCurColor);
 		putSuppDataRect(mCurSuppData);
 	}
 
 	public void drawQuad(YangMatrix transform) {
 		drawQuad(transform, mTexIdentity);
 	}
 
 	public void drawRect(float worldX1, float worldY1, float worldX2, float worldY2, float texX1, float texY1, float texX2, float texY2) {
 		mCurrentVertexBuffer.beginQuad();
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,worldX1, worldY1, worldX2, worldY2, mCurrentZ);
 		mCurrentVertexBuffer.putRect2D(ID_TEXTURES,texX1, texY1, texX2, texY2);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 
 	public void drawRect(float worldX1, float worldY1, float worldX2, float worldY2, TextureCoordinatesQuad texCoordinates) {
 		mCurrentVertexBuffer.beginQuad();
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,worldX1, worldY1, worldX2, worldY2, mCurrentZ);
 		mCurrentVertexBuffer.putArray(ID_TEXTURES, texCoordinates.mAppliedCoordinates);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 
 	public void drawRect(float worldX1, float worldY1, float worldX2, float worldY2, YangMatrix textureTransform) {
 		mCurrentVertexBuffer.beginQuad();
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,worldX1, worldY1, worldX2, worldY2, mCurrentZ);
 		putTransformedTextureRect(textureTransform);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 
 	public void drawRect(float worldX1, float worldY1, float worldX2, float worldY2) {
 		mCurrentVertexBuffer.beginQuad();
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,worldX1, worldY1, worldX2, worldY2, mCurrentZ);
 		mCurrentVertexBuffer.putArray(ID_TEXTURES, mTexIdentity.mAppliedCoordinates);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 
 	public void drawLineRect(float worldX1, float worldY1, float worldX2, float worldY2, float width, TextureCoordinatesQuad texCoordinates) {
 		drawLine(worldX1,worldY1, worldX2+width*0.5f,worldY1, width, texCoordinates);
 		drawLine(worldX2,worldY1, worldX2,worldY2+width*0.5f, width, texCoordinates);
 		drawLine(worldX2,worldY2, worldX1-width*0.5f,worldY2, width, texCoordinates);
 		drawLine(worldX1,worldY2, worldX1,worldY1-width*0.5f, width, texCoordinates);
 	}
 
 	public void drawLineRect(float worldX1, float worldY1, float worldX2, float worldY2, float width) {
 		drawLineRect(worldX1,worldY1, worldX2,worldY2, width, null);
 	}
 
 	public void drawRectWH(float left, float bottom, float width, float height, TextureCoordinatesQuad texCoords) {
 		mCurrentVertexBuffer.beginQuad();
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,left, bottom, left+width, bottom+height, mCurrentZ);
 		if(texCoords==null)
 			mCurrentVertexBuffer.putArray(ID_TEXTURES, RECT_TEXTURECOORDS);
 		else
 			mCurrentVertexBuffer.putArray(ID_TEXTURES, texCoords.mAppliedCoordinates);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 
 	public void drawRectCentered(float centerX, float centerY, float width, float height) {
 		mCurrentVertexBuffer.beginQuad();
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,centerX-width*0.5f,centerY-height*0.5f,centerX+width*0.5f,centerY+height*0.5f,mCurrentZ);
 		mCurrentVertexBuffer.putArray(ID_TEXTURES, RECT_TEXTURECOORDS);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 
 	public void drawRectCentered(float centerX, float centerY, float widthAndHeight) {
 		mCurrentVertexBuffer.beginQuad();
 		final float d = widthAndHeight*0.5f;
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,centerX-d,centerY-d,centerX+d,centerY+d,mCurrentZ);
 		mCurrentVertexBuffer.putArray(ID_TEXTURES, RECT_TEXTURECOORDS);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 
 	public void drawRectCentered(float centerX, float centerY, float width, float height, float angle, float texX1, float texY1, float texX2, float texY2) {
 		mCurrentVertexBuffer.beginQuad();
 		mCurrentVertexBuffer.putRotatedRect3D(ID_POSITIONS,width,height,centerX,centerY,mCurrentZ,angle);
 		mCurrentVertexBuffer.putRect2D(ID_TEXTURES,texX1, texY1, texX2, texY2);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 
 	public void drawRectCentered(float centerX, float centerY, float width, float height, float angle, TextureCoordinatesQuad texCoordinates) {
 		mCurrentVertexBuffer.beginQuad();
 		mCurrentVertexBuffer.putRotatedRect3D(ID_POSITIONS,width,height,centerX,centerY,mCurrentZ,angle);
 //		if(mTranslator.mTimer>10) return;
 		mCurrentVertexBuffer.putArray(ID_TEXTURES,texCoordinates.mAppliedCoordinates);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 
 
 	public void drawRectCentered(float centerX, float centerY, float width, float height, float angle) {
 		drawRectCentered(centerX,centerY,width,height,angle,RECT_TEXQUAD);
 	}
 
 	public void drawRectCentered(float centerX, float centerY, float width, float height, float texX1, float texY1, float texX2, float texY2) {
 		mCurrentVertexBuffer.beginQuad();
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,centerX-width*0.5f,centerY-height*0.5f,centerX+width*0.5f,centerY+height*0.5f,mCurrentZ);
 		this.putTextureRect(texX1, texY1, texX2, texY2);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 
 	/**
 	 * width = scale * texCoordRatio
 	 * height = scale
 	 */
 	public void drawRectCentered(float centerX, float centerY, float scale, float angle, TextureCoordinatesQuad texCoordinates) {
 		drawRectCentered(centerX,centerY,scale*texCoordinates.mRatioWidth,scale,angle,texCoordinates);
 	}
 
 	public void drawRectCentered(float centerX, float centerY, float scale, TextureCoordinatesQuad texCoordinates) {
 		drawRectCentered(centerX, centerY, scale, 0, texCoordinates);
 	}
 
 	public void drawLine(float fromX,float fromY, float toX,float toY, float width, YangMatrix textureTransform) {
 		MatrixOps.setLine(mInterTransf1,fromX, fromY, toX, toY, width);
 		drawQuad(mInterTransf1,textureTransform);
 	}
 
 	public void drawLine(float fromX,float fromY, float toX,float toY, float width, TextureCoordinatesQuad texCoordinates) {
 		MatrixOps.setLine(mInterTransf1,fromX, fromY, toX, toY, width);
 		drawQuad(mInterTransf1,texCoordinates);
 	}
 
 	public void drawLine(float fromX,float fromY, float toX,float toY, float width) {
 		drawLine(fromX,fromY,toX,toY,width,mTexIdentity);
 	}
 
 	// ---PUT-POSITIONS---
 
 	public void putTransformedPositionRect(YangMatrix transform) {
 		mCurrentVertexBuffer.putTransformed3D(ID_POSITIONS,0,0,mCurrentZ, transform.mValues);
 		mCurrentVertexBuffer.putTransformed3D(ID_POSITIONS,1,0,mCurrentZ, transform.mValues);
 		mCurrentVertexBuffer.putTransformed3D(ID_POSITIONS,0,1,mCurrentZ, transform.mValues);
 		mCurrentVertexBuffer.putTransformed3D(ID_POSITIONS,1,1,mCurrentZ, transform.mValues);
 	}
 
 	public void putPosition(float x, float y) {
 		mCurrentVertexBuffer.putVec3(ID_POSITIONS, x, y, mCurrentZ);
 	}
 
 	public void putPosition(float x, float y,YangMatrix transform) {
 		mCurrentVertexBuffer.putTransformed3D(ID_POSITIONS, x, y, mCurrentZ, transform.mValues);
 	}
 
 	public void putPosition(float x, float y,float z) {
 		mCurrentVertexBuffer.putVec3(ID_POSITIONS, x, y, z);
 	}
 
 	public void putPosition(float x,float y,float z,YangMatrix transform) {
 		transform.apply3D(x, y, z, mInterArray, 0);
 		if(mInterArray[3]!=1) {
 			final float d = 1f/mInterArray[3];
 			mInterArray[0] *= d;
 			mInterArray[1] *= d;
 			mInterArray[2] *= d;
 		}
 		mPositions.put(mInterArray, 0, 3);
 	}
 
 	public void putPositionArray(float[] positions) {
 		mPositions.put(positions);
 	}
 
 	public void putPositionQuad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
 		mCurrentVertexBuffer.putVec8(ID_POSITIONS, x1, y1, x2, y2, x3, y3, x4, y4);
 	}
 
 	public void putPositionRect(float x1, float y1, float x2, float y2, float bias) {
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,x1+bias,y1+bias,x2-bias,y2-bias,mCurrentZ);
 	}
 
 	public void putPositionRect(float x1, float y1, float x2, float y2) {
 		mCurrentVertexBuffer.putRect3D(ID_POSITIONS,x1,y1,x2,y2,mCurrentZ);
 	}
 
 	public void putNormal(float nx,float ny,float nz) {
 		mCurrentVertexBuffer.putVec3(ID_NORMALS, nx, ny, nz);
 	}
 
 	// ---PUT-TEXTURES---
 
 	public void putTextureArray(float[] texCoords) {
 		mCurrentVertexBuffer.putArray(ID_TEXTURES,texCoords);
 	}
 
 	public void putTextureCoord(float u, float v) {
 		mCurrentVertexBuffer.putVec2(ID_TEXTURES, u,v);
 	}
 
 	public void putTextureCoordPair(float u1, int v1, float u2, float v2) {
 		mCurrentVertexBuffer.putVec4(ID_TEXTURES,u1,v1,u2,v2);
 	}
 
 	public void putTextureRect(float x1, float y1, float x2, float y2, float bias) {
 		mCurrentVertexBuffer.putVec8(ID_TEXTURES,
 				x1 - bias, y1 - bias,
 				x2 + bias, y1 - bias,
 				x1 - bias, y2 + bias,
 				x2 + bias, y2 + bias);
 	}
 
 	public void putTextureRect(float x1, float y1, float x2, float y2) {
 		putTextureRect(x1, y1, x2, y2, DEFAULTTEXBIAS);
 	}
 
 	public void putTextureRect(TextureCoordinatesQuad texCoords) {
 		mCurrentVertexBuffer.putArray(ID_TEXTURES, texCoords.mAppliedCoordinates);
 	}
 
 	public void putTransformedTextureRect(YangMatrix transform) {
 		mCurrentVertexBuffer.putTransformed2D(ID_TEXTURES,0,1, transform.mValues);
 		mCurrentVertexBuffer.putTransformed2D(ID_TEXTURES,1,1, transform.mValues);
 		mCurrentVertexBuffer.putTransformed2D(ID_TEXTURES,0,0, transform.mValues);
 		mCurrentVertexBuffer.putTransformed2D(ID_TEXTURES,1,0, transform.mValues);
 	}
 
 	// ---PUT-COLORS---
 
 	@Override
 	public void setWhite() {
 		setColor(1,1,1,1);
 	}
 
 	public void setFactorWhite() {
 		setColorFactor(1,1,1,1);
 	}
 
 	public void putColor(float r, float g, float b, float a) {
 		mCurrentVertexBuffer.putVec4(ID_COLORS, r, g, b, a);
 	}
 
 	public void putColor(float[] color) {
 		mCurrentVertexBuffer.putArray(ID_COLORS,color);
 	}
 
 	public void putColor(FloatColor color) {
 		mCurrentVertexBuffer.putArray(ID_COLORS,color.mValues);
 	}
 
 	public void putColorRect(float[] color) {
 		if(mCurrentProgram.mHasColor) {
 			mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,color,4);
 		}
 	}
 
 	public void putColorWhite(int amount) {
 		putColor(WHITE,amount);
 	}
 
 	public void putColor(float[] color, int amount) {
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,color, amount);
 	}
 
 	// ---COLOR-FACTOR---
 
 	public void setColorFactor(float r, float g, float b) {
 		flush();
 		mColorFactor[0] = r;
 		mColorFactor[1] = g;
 		mColorFactor[2] = b;
 		mColorFactor[3] = 1;
 		if (mCurrentProgram != null && mCurrentProgram.mHasColorFactor)
 			mCurrentProgram.setColorFactor(r, g, b);
 	}
 
 	public void setColorFactor(float r, float g, float b, float a) {
 		flush();
 		mColorFactor[0] = r;
 		mColorFactor[1] = g;
 		mColorFactor[2] = b;
 		mColorFactor[3] = a;
 		if (mCurrentProgram != null && mCurrentProgram.mHasColorFactor)
 			mCurrentProgram.setColorFactor(r, g, b, a);
 	}
 
 	public void setColorFactor(float brightness) {
 		assert mTranslator.preCheck("Set color factor");
 		flush();
 		mColorFactor[0] = brightness;
 		mColorFactor[1] = brightness;
 		mColorFactor[2] = brightness;
 		mColorFactor[3] = 1;
 		if (mCurrentProgram != null && mCurrentProgram.mHasColorFactor)
 			mCurrentProgram.setColorFactor(brightness, brightness, brightness, 1);
 	}
 
 	public void setColorFactor(FloatColor color) {
 		setColorFactor(color.mValues[0],color.mValues[1],color.mValues[2],color.mValues[3]);
 	}
 
 	public void setColorFactor(float[] color) {
 		setColorFactor(color[0],color[1],color[2],color[3]);
 	}
 
 	// ---PUT-SUPPLEMENTARY-DATA---
 
 	public void putSuppData(float v1, float v2, float v3, float v4) {
 		mSuppData.put(v1);
 		mSuppData.put(v2);
 		mSuppData.put(v3);
 		mSuppData.put(v4);
 	}
 
 	public void putSuppDataZero(int amount) {
 		while (amount > 0) {
 			putSuppData(NULLVECTOR);
 			amount--;
 		}
 	}
 
 	public void putSuppData(float[] data) {
 		mSuppData.put(data);
 	}
 
 	public void putSuppData(Quadruple data) {
 		mSuppData.put(data.mValues);
 	}
 
 	public void putSuppDataRect(float[] data) {
 		putSuppData(data);
 		putSuppData(data);
 		putSuppData(data);
 		putSuppData(data);
 	}
 
 	public void putSuppData(float[] data, int amount) {
 		while (amount > 0) {
 			putSuppData(data);
 			amount--;
 		}
 	}
 
 	public String buffersToString() {
 		String res = "";
 		res += "Indices:\t" + Util.bufferToString(mIndexBuffer) + "\n";
 		res += "Positions:\t" + Util.bufferToString(mPositions) + "\n";
 		res += "Textures:\t" + Util.bufferToString(mTextures) + "\n";
 		res += "Colors:\t\t" + Util.bufferToString(mColors) + "\n";
 		res += "SuppData:\t\t" + Util.bufferToString(mSuppData) + "\n";
 		return res;
 	}
 
 	public boolean checkBufferIndices() {
 		mIndexBuffer.position(0);
 		for (int i = 0; i < mCurrentVertexBuffer.getIndexCount(); i++) {
 			final int index = mIndexBuffer.get();
 			if (index >= mCurrentVertexBuffer.getVertexCount())
 				throw new RuntimeException();
 		}
 		mIndexBuffer.position(0);
 		return true;
 	}
 
 	@Override
 	public void setVertexBuffer(IndexedVertexBuffer vertexBuffer) {
 		super.setVertexBuffer(vertexBuffer);
 		final UniversalVertexBuffer buf = (UniversalVertexBuffer) vertexBuffer;
 		mIndexBuffer = buf.mIndexBuffer;
 		final FloatBuffer[] buffers = buf.mFloatBuffers;
 		mPositions = buffers[ID_POSITIONS];
 		mTextures = buffers[ID_TEXTURES];
 		mColors = buffers[ID_COLORS];
 		mSuppData = buffers[ID_SUPPDATA];
 		if (buffers.length > 4)
 			mNormals = buffers[ID_NORMALS];
 		else
 			mNormals = null;
 	}
 
 	public void drawRect(YangMatrix transform,TextureCoordinatesQuad texCoords) {
 		mCurrentVertexBuffer.beginQuad();
 		mCurrentVertexBuffer.putTransformedArray3D(ID_POSITIONS, RECT_POSITIONS, 4, transform.mValues);
 		mCurrentVertexBuffer.putArray(ID_TEXTURES, texCoords.mAppliedCoordinates);
 		mCurrentVertexBuffer.putArrayMultiple(ID_COLORS,mCurColor,4);
 		mCurrentVertexBuffer.putArrayMultiple(ID_SUPPDATA,mCurSuppData,4);
 	}
 
 	public void drawRect(Rect rect, TextureCoordinatesQuad texCoords) {
 		drawRect(rect.mLeft, rect.mBottom, rect.mRight, rect.mTop, texCoords);
 	}
 
 	public void drawRect(Rect rect) {
 		drawRect(rect.mLeft, rect.mBottom, rect.mRight, rect.mTop);
 	}
 
 	@Override
 	public void onPreDraw() {
 		updateProgramProjection();
 		mCurrentProgram.prepareDraw();
 	}
 
 	public void setTime(float time) {
 		mTime = time;
 		if (mCurrentProgram != null)
 			mCurrentProgram.setTime(time);
 	}
 
 	public void drawString(YangMatrix transform,DrawableString string) {
 		string.draw(transform);
 	}
 
 	public void drawString(float x,float y,float lineHeight,DrawableString string) {
 		string.draw(x,y,lineHeight);
 	}
 
 	public void drawString(float x,float y,float lineHeight,float rotation,DrawableString string) {
 		string.draw(x,y,lineHeight,rotation);
 	}
 
 	public float getCamX() {
 		return mCameraProjection.getX();
 	}
 
 	public float getCamY() {
 		return mCameraProjection.getY();
 	}
 
 	public float getCamZ() {
 		return mCameraProjection.getZ();
 	}
 
 	public Point3f getCamPositionReference() {
 		return mCameraProjection.getPositionReference();
 	}
 
 	public CameraProjection getCameraProjection() {
 		return mCameraProjection;
 	}
 
 	public void setStereoZDistance(float f) {
 		//TODO
 	}
 
 	public boolean rectInScreen2D(float posX, float posY, Rect rect) {
 		if(true)return true;
 		return inScreen2D(posX+rect.mLeft, posY+rect.mBottom, rect.getWidth(), rect.getHeight());
 	}
 
 	public boolean inScreen2D(float posX, float posY, float width, float height) {
 		if(true)return true;
 		if(mWorldTransformEnabled) {
 			posX += mWorldTransform.get(12);
 			posY += mWorldTransform.get(13);
 		}
 		float rx = mTranslator.mCurrentSurface.getSurfaceRatioX();
 		float ry = mTranslator.mCurrentSurface.getSurfaceRatioY();
 		if(mCurViewProjTransform==mViewProjectionTransform)
 			return posX<=mCameraProjection.normToWorld2DX(rx, 0) &&
 					posY<=mCameraProjection.normToWorld2DY(0, ry) &&
 					(posX+width>=mCameraProjection.normToWorld2DX(-rx, 0)) &&
 					(posY+height>=mCameraProjection.normToWorld2DY(0, -ry));
 		else{
 			return posX<=rx && posY<=ry && (posX>=-rx-width) && (posY>=-ry-height);
 		}
 	}
 
 	public void sort(IndexedVertexBuffer buffer,float normShift) {
 		if(mVertexSort==null)
			mVertexSort = new VertexZSort(700000,30000);
 		mVertexSort.sort(buffer.mIndexBuffer, buffer.getFloatBuffer(ID_POSITIONS),buffer.getFloatBuffer(ID_NORMALS),normShift, mCameraProjection,mWorldTransformEnabled?mWorldTransform:null);
 	}
 
 	public void sort() {
 		sort(mCurrentVertexBuffer,0.002f);
 	}
 
 }
