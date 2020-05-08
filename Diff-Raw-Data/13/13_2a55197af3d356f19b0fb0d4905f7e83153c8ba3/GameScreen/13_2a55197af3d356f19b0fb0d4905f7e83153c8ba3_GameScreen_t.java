 package com.luzi82.chitanda.game.ui;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Mesh;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.VertexAttribute;
 import com.badlogic.gdx.graphics.VertexAttributes;
 import com.luzi82.chitanda.ChitandaGame;
 import com.luzi82.chitanda.game.logic.Board;
 import com.luzi82.gdx.GrScreen;
 
 public class GameScreen extends GrScreen<ChitandaGame> {
 
 	public static final int TOUCH_MAX = 16;
 	public static final float SMOOTH_REDUCE = 1f / 256;
 	public static final float DIV_LN_SMOOTH_REDUCE = (float) (1 / Math.log(SMOOTH_REDUCE));
 	public static final float PHI = (float) (1 + Math.sqrt(5)) / 2;
 
 	private static final float LINE_ALPHA = 1f / 16;
 	private static final float LINE_WIDTH = 1f / 16;
 
 	private Board mBoard;
 
 	private OrthographicCamera mCamera;
 
 	private Pixmap mBasePixmap0;
 	private Pixmap mBasePixmap1;
 	private Texture mBaseTexture0;
 	private Texture mBaseTexture1;
 	private Mesh mBaseMesh0;
 	private Mesh mBaseMesh1;
 
 	private Mesh mLineMeshH;
 	private float[] mLineMeshHF;
 	private final int[] LINE_MESH_HA = {//
 	1 * 7 - 1,//
 			2 * 7 - 1,//
 			7 * 7 - 1,//
 			8 * 7 - 1,//
 	};
 
 	private Mesh mBlockMesh;
 
 	private float mViewPortWidth;
 	private float mViewPortHeight;
 
 	private float iCameraZoom;
 	private float iCameraX;
 	private float iCameraY;
 	private float mCameraZoomD;
 	private float mCameraXD;
 	private float mCameraYD;
 
 	private boolean mNewTouch;
 	private boolean mNewTouchEvent;
 	private boolean[] mTouching;
 	private int[] mTouchX;
 	private int[] mTouchY;
 	// private boolean[] mTouching;
 	// private boolean[] mMoved;
 	private float mTouchStartCameraX;
 	private float mTouchStartCameraY;
 	private float mTouchStartDiff;
 	private float mTouchStartCameraZoom;
 
 	// desktop handle
 	private int mMouseOverX;
 	private int mMouseOverY;
 	private int mMouseScrolled;
 
 	// private boolean mMoveEnabled;
 
 	public GameScreen(ChitandaGame aParent) {
 		super(aParent);
 	}
 
 	@Override
 	protected void onScreenShow() {
 		iCameraZoom = Math.min(Board.WIDTH, Board.HEIGHT);
 		iCameraX = Board.WIDTH / 2;
 		iCameraY = Board.HEIGHT / 2;
 	}
 
 	@Override
 	protected void onScreenLoad() {
 		GL10 gl = Gdx.graphics.getGL10();
 
 		mBoard = new Board();
 		mBoard.setAll(true);
 
 		mCamera = new OrthographicCamera();
 
 		mBasePixmap0 = new Pixmap(Gdx.files.internal("data/chitanda0.png"));
 		mBasePixmap1 = new Pixmap(Gdx.files.internal("data/chitanda1.png"));
 		mBaseTexture0 = new Texture(mBasePixmap0, true);
 		mBaseTexture1 = new Texture(mBasePixmap1, true);
 
 		VertexAttributes va = new VertexAttributes( //
 				new VertexAttribute(VertexAttributes.Usage.Position, 3, "position"),//
 				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "texturecoordinates")//
 		);
 
 		mBaseMesh0 = new Mesh(true, 4, 4, va);
 		mBaseMesh0.setVertices(new float[] { //
 				0f, 1024f * 4, 0f, 0f, 0f,//
 						1024f * 4, 1024f * 4, 0f, 1f, 0f,//
 						0f, 0f, 0f, 0f, 1f,//
 						1024f * 4, 0f, 0f, 1f, 1f,//
 				});
 		mBaseMesh0.setIndices(new short[] { 0, 1, 2, 3 });
 		mBaseMesh1 = new Mesh(true, 4, 4, va);
 		mBaseMesh1.setVertices(new float[] { //
 				1024f * 4, 1024f * 4, 0f, 0f, 0f,//
 						1024f * 8, 1024f * 4, 0f, 1f, 0f,//
 						1024f * 4, 0f, 0f, 0f, 1f,//
 						1024f * 8, 0f, 0f, 1f, 1f,//
 				});
 		mBaseMesh1.setIndices(new short[] { 0, 1, 2, 3 });
 
 		va = new VertexAttributes( //
 				new VertexAttribute(VertexAttributes.Usage.Position, 3, "position"),//
 				new VertexAttribute(VertexAttributes.Usage.Color, 4, "color")//
 		);
 
 		mLineMeshH = new Mesh(false, 8, 12, va);
 		mLineMeshHF = new float[] { //
 		1f, 0f, 0f, 0f, 0f, 0f, LINE_ALPHA,//
 				0f, 0f, 0f, 0f, 0f, 0f, LINE_ALPHA,//
 				1f, LINE_WIDTH, 0f, 0f, 0f, 0f, 0f,//
 				0f, LINE_WIDTH, 0f, 0f, 0f, 0f, 0f,//
 
 				1f, 1 - LINE_WIDTH, 0f, 0f, 0f, 0f, 0f,//
 				0f, 1 - LINE_WIDTH, 0f, 0f, 0f, 0f, 0f,//
 				1f, 1f, 0f, 0f, 0f, 0f, LINE_ALPHA,//
 				0f, 1f, 0f, 0f, 0f, 0f, LINE_ALPHA,//
 		};
		mLineMeshH.setIndices(new short[] { //
				0, 1, 2, //
						1, 2, 3, //
						4, 5, 6, //
						5, 6, 7 //
				});
 
 		va = new VertexAttributes( //
 				new VertexAttribute(VertexAttributes.Usage.Position, 3, "position") //
 		);
 
 		mBlockMesh = new Mesh(true, 4, 4, va);
 		mBlockMesh.setVertices(new float[] {//
 				0f, 1f, 0f,//
 						1f, 1f, 0f,//
 						0f, 0f, 0f,//
 						1f, 0f, 0f,//
 				});
 		mBlockMesh.setIndices(new short[] { 0, 1, 2, 3 });
 
 		gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
 		gl.glDisable(GL10.GL_DEPTH_TEST);
 
 		// mTouching = new boolean[TOUCH_MAX];
 		// mLastX = new int[TOUCH_MAX];
 		// mLastY = new int[TOUCH_MAX];
 		// mSumX = new int[TOUCH_MAX];
 		// mSumY = new int[TOUCH_MAX];
 		mNewTouch = true;
 		mTouching = new boolean[TOUCH_MAX];
 		mTouchX = new int[TOUCH_MAX];
 		mTouchY = new int[TOUCH_MAX];
 
 		mCameraZoomD = 0;
 		mCameraXD = 0;
 		mCameraYD = 0;
 	}
 
 	@Override
 	public void onScreenRender(float aDelta) {
 		GL10 gl = Gdx.graphics.getGL10();
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		updateCamera(aDelta, gl);
 		drawImage(gl);
 		drawLine(gl);
 		drawBlock(gl);
 	}
 
 	private void updateCamera(float aDelta, GL10 aGl) {
 		int i;
 
 		float reduce = (float) Math.pow(SMOOTH_REDUCE, aDelta);
 		float intReduce = (reduce - 1) * DIV_LN_SMOOTH_REDUCE;
 
 		float touchXAvg = 0;
 		float touchYAvg = 0;
 		float touchDiff = 0;
 		int touchCount = 0;
 
 		for (i = 0; i < TOUCH_MAX; ++i) {
 			if (!mTouching[i])
 				continue;
 			touchXAvg += mTouchX[i];
 			touchYAvg += mTouchY[i];
 			++touchCount;
 		}
 		if (touchCount > 0) {
 			touchXAvg /= touchCount;
 			touchYAvg /= touchCount;
 			if (touchCount > 1) {
 				for (i = 0; i < TOUCH_MAX; ++i) {
 					if (!mTouching[i])
 						continue;
 					float d = 0, dd = 0;
 					dd = mTouchX[i] - touchXAvg;
 					dd *= dd;
 					d += dd;
 					dd = mTouchY[i] - touchYAvg;
 					dd *= dd;
 					d += dd;
 					touchDiff += (float) Math.sqrt(d);
 				}
 			}
 			touchDiff /= touchCount;
 			if (mNewTouch) {
 				mTouchStartCameraX = screenToBoardX(touchXAvg);
 				mTouchStartCameraY = screenToBoardY(touchYAvg);
 				if (touchCount > 1) {
 					mTouchStartDiff = touchDiff;
 					mTouchStartCameraZoom = iCameraZoom;
 				}
 				mNewTouch = false;
 			} else if (mNewTouchEvent) {
 				if (touchCount > 1) {
 					float newZoom = mTouchStartCameraZoom * mTouchStartDiff / touchDiff;
 					mCameraZoomD = ((float) Math.log(newZoom / iCameraZoom)) / aDelta;
 					iCameraZoom = newZoom;
 				} else {
 					smoothZoom(aDelta, reduce, intReduce);
 				}
 				float newX = screenBoardToCameraX(touchXAvg, mTouchStartCameraX);
 				float newY = screenBoardToCameraY(touchYAvg, mTouchStartCameraY);
 				mCameraXD = (newX - iCameraX) / aDelta;
 				mCameraYD = (newY - iCameraY) / aDelta;
 				iCameraX = newX;
 				iCameraY = newY;
 			}
 		} else if (mMouseScrolled != 0) {
 			float x = screenToBoardX(mMouseOverX);
 			float y = screenToBoardY(mMouseOverY);
 			mCameraZoomD -= mMouseScrolled * PHI;
 			smoothZoom(aDelta, reduce, intReduce);
 			float newX = screenBoardToCameraX(mMouseOverX, x);
 			float newY = screenBoardToCameraY(mMouseOverY, y);
 			mCameraXD = (newX - iCameraX) / aDelta;
 			mCameraYD = (newY - iCameraY) / aDelta;
 			iCameraX = newX;
 			iCameraY = newY;
 			mMouseScrolled = 0;
 		} else {
 			smoothZoom(aDelta, reduce, intReduce);
 			smoothXY(aDelta, reduce, intReduce);
 		}
 		mNewTouchEvent = false;
 
 		mCamera.zoom = iCameraZoom;
 		mCamera.position.x = iCameraX;
 		mCamera.position.y = iCameraY;
 		mCamera.update();
 		mCamera.apply(aGl);
 	}
 
 	private void drawImage(GL10 aGl) {
 		aGl.glDisable(GL10.GL_BLEND);
 		aGl.glBlendFunc(GL10.GL_ONE, GL10.GL_ZERO);
 		aGl.glEnable(GL10.GL_TEXTURE_2D);
 		mBaseTexture0.bind();
 		mBaseMesh0.render(GL10.GL_TRIANGLE_STRIP);
 		mBaseTexture1.bind();
 		mBaseMesh1.render(GL10.GL_TRIANGLE_STRIP);
 	}
 
 	private void drawLine(GL10 aGl) {
 		int i;
 
 		aGl.glEnable(GL10.GL_BLEND);
 		aGl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		aGl.glDisable(GL10.GL_TEXTURE_2D);
 
 		if (iCameraZoom < ZOOM_MIN * PHI * PHI) {
 			float a = iCameraZoom / (ZOOM_MIN * PHI);
 			a = (float) Math.log(a);
 			a /= (float) Math.log(PHI);
 			a = 1 - a;
 			if (a > 0) {
 				if (a > 1)
 					a = 1f;
 				a *= LINE_ALPHA;
 				for (int ai : LINE_MESH_HA) {
 					mLineMeshHF[ai] = a;
 				}
 				mLineMeshH.setVertices(mLineMeshHF);
 
 				int min, max;
 
 				min = (int) Math.floor(iCameraY - iCameraZoom * mViewPortHeight / 2);
 				max = (int) Math.ceil(iCameraY + iCameraZoom * mViewPortHeight / 2);
 				if (min < 0)
 					min = 0;
 				if (max > Board.HEIGHT)
 					max = Board.HEIGHT;
 				for (i = min; i < max; ++i) {
 					aGl.glPushMatrix();
 					aGl.glTranslatef(0, i, 0);
 					aGl.glScalef(Board.WIDTH, 1, 1);
 					mLineMeshH.render(GL10.GL_TRIANGLES);
 					aGl.glPopMatrix();
 				}
 
 				min = (int) Math.floor(iCameraX - iCameraZoom * mViewPortWidth / 2);
 				max = (int) Math.ceil(iCameraX + iCameraZoom * mViewPortWidth / 2);
 				if (min < 0)
 					min = 0;
 				if (max > Board.WIDTH)
 					max = Board.WIDTH;
 				aGl.glPushMatrix();
 				aGl.glRotatef(90, 0, 0, 1);
 				aGl.glScalef(1, -1, 1);
 				for (i = min; i < max; ++i) {
 					aGl.glPushMatrix();
 					aGl.glTranslatef(0, i, 0);
 					aGl.glScalef(Board.HEIGHT, 1, 1);
 					mLineMeshH.render(GL10.GL_TRIANGLES);
 					aGl.glPopMatrix();
 				}
 				aGl.glPopMatrix();
 			}
 		}
 	}
 
 	private void drawBlock(GL10 aGl) {
 		aGl.glDisable(GL10.GL_BLEND);
 		aGl.glBlendFunc(GL10.GL_ONE, GL10.GL_ZERO);
 		aGl.glDisable(GL10.GL_TEXTURE_2D);
 		aGl.glColor4f(0f, 0f, 0f, 1f);
 		if (iCameraZoom < ZOOM_MIN * PHI * PHI) {
 			int minX = (int) Math.floor(screenToBoardX(0));
 			int maxX = (int) Math.ceil(screenToBoardX(mScreenWidth));
 			int minY = (int) Math.floor(screenToBoardY(mScreenHeight));
 			int maxY = (int) Math.ceil(screenToBoardY(0));
 			for (int x = minX; x < maxX; ++x) {
 				aGl.glPushMatrix();
 				aGl.glTranslatef(x, 0, 0);
 				for (int y = minY; y < maxY; ++y) {
 					if (mBoard.get(x, y)) {
 						aGl.glPushMatrix();
 						aGl.glTranslatef(0, y, 0);
 						mBlockMesh.render(GL10.GL_TRIANGLE_STRIP);
 						aGl.glPopMatrix();
 					}
 				}
 				aGl.glPopMatrix();
 			}
 		}
 		aGl.glColor4f(1f, 1f, 1f, 1f);
 	}
 
 	private float screenToBoardX(float aX) {
 		return iCameraX + (iCameraZoom * mViewPortWidth * (aX / mScreenWidth - 0.5f));
 	}
 
 	private float screenToBoardY(float aY) {
 		return iCameraY + (iCameraZoom * mViewPortHeight * (1 - (aY / mScreenHeight) - 0.5f));
 	}
 
 	private float screenBoardToCameraX(float aScreenX, float aBoardX) {
 		return (iCameraZoom * mViewPortWidth) * (0.5f - aScreenX / mScreenWidth) + aBoardX;
 	}
 
 	private float screenBoardToCameraY(float aScreenY, float aBoardY) {
 		return (iCameraZoom * mViewPortHeight) * (0.5f + aScreenY / mScreenHeight - 1) + aBoardY;
 	}
 
 	@Override
 	public void onScreenResize() {
 		mViewPortWidth = (mScreenWidth > mScreenHeight) ? (((float) mScreenWidth) / mScreenHeight) : 1;
 		mViewPortHeight = (mScreenWidth > mScreenHeight) ? 1 : (((float) mScreenHeight) / mScreenWidth);
 		mCamera.viewportWidth = mViewPortWidth;
 		mCamera.viewportHeight = mViewPortHeight;
 	}
 
 	@Override
 	public boolean touchDown(int x, int y, int pointer, int button) {
 		mNewTouch = true;
 		mNewTouchEvent = true;
 		mTouching[pointer] = true;
 		mTouchX[pointer] = x;
 		mTouchY[pointer] = y;
 		return true;
 	}
 
 	@Override
 	public boolean touchUp(int x, int y, int pointer, int button) {
 		mNewTouch = true;
 		mNewTouchEvent = true;
 		mTouching[pointer] = false;
 		return true;
 	}
 
 	@Override
 	public boolean touchDragged(int x, int y, int pointer) {
 		mNewTouchEvent = true;
 		mTouching[pointer] = true;
 		mTouchX[pointer] = x;
 		mTouchY[pointer] = y;
 		return true;
 	}
 
 	@Override
 	public boolean touchMoved(int x, int y) {
 		mMouseOverX = x;
 		mMouseOverY = y;
 		return true;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		mMouseScrolled += amount;
 		return true;
 	}
 
 	static private float ZOOM_MIN = PHI * 4;
 	static private float ZOOM_MAX = 4 * 1024 * PHI;
 	static private float LOG_ZOOM_MIN = (float) Math.log(ZOOM_MIN);
 	static private float LOG_ZOOM_MAX = (float) Math.log(ZOOM_MAX);
 
 	private void smoothZoom(float aDelta, float aReduce, float aIntReduce) {
 		// iCameraZoom *= (float) Math.pow(Math.E, mCameraZoomD * aIntReduce);
 		float logCameraZoom = (float) Math.log(iCameraZoom);
 		logCameraZoom = smooth(aDelta, aReduce, aIntReduce, logCameraZoom, mCameraZoomD, LOG_ZOOM_MIN, LOG_ZOOM_MAX);
 		iCameraZoom = (float) Math.pow(Math.E, logCameraZoom);
 		mCameraZoomD *= aReduce;
 	}
 
 	private void smoothXY(float aDelta, float aReduce, float aIntReduce) {
 		// iCameraX += mCameraXD * aIntReduce;
 		// iCameraY += mCameraYD * aIntReduce;
 		iCameraX = smooth(aDelta, aReduce, aIntReduce, iCameraX, mCameraXD, 0, Board.WIDTH);
 		iCameraY = smooth(aDelta, aReduce, aIntReduce, iCameraY, mCameraYD, 0, Board.HEIGHT);
 		mCameraXD *= aReduce;
 		mCameraYD *= aReduce;
 	}
 
 	private float smooth(float aDelta, float aReduce, float aIntReduce, float aS0, float aV, float aMin, float aMax) {
 		if ((aMin <= aS0) && (aS0 <= aMax)) {
 			float s1 = aS0 + aV * aIntReduce;
 			// TODO mid border
 			return s1;
 		} else {
 			float border = (aS0 < aMin) ? aMin : aMax;
 			float dm = aV * aIntReduce;
 			float db = (aReduce - 1) * (2 * aS0 - 2 * border + dm) / 2;
 			float s1 = aS0 + dm + db;
 			// TODO mid border
 			return s1;
 		}
 
 	}
 
 }
