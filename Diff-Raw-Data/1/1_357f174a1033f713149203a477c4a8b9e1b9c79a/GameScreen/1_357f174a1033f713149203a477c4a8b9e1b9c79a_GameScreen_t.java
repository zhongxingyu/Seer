 package com.luzi82.chitanda.game.ui;
 
 import java.util.Arrays;
 import java.util.TreeMap;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Mesh;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.VertexAttribute;
 import com.badlogic.gdx.graphics.VertexAttributes;
 import com.badlogic.gdx.utils.Disposable;
 import com.luzi82.chitanda.ChitandaGame;
 import com.luzi82.chitanda.game.logic.Board;
 import com.luzi82.gdx.GrScreen;
 
 public class GameScreen extends GrScreen<ChitandaGame> {
 
 	public static final float PHI = (float) (1 + Math.sqrt(5)) / 2;
 
 	private static final float LINE_ALPHA = 1f / 16;
 	private static final float LINE_WIDTH = 1f / 16;
 
 	private Board mBoard;
 
 	private OrthographicCamera mCamera;
 
 	// private Pixmap mBasePixmap0;
 	// private Pixmap mBasePixmap1;
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
 
 	private Mesh mBlockGroupMesh;
 
 	private CameraCalc mCameraManager;
 	private CameraControl mCameraTouchLogic;
 
 	// screen density
 	private float mBlockPerPixelBorder;
 
 	public GameScreen(ChitandaGame aParent) {
 		super(aParent);
 	}
 
 	@Override
 	protected void onScreenShow() {
 	}
 
 	@Override
 	protected void onScreenLoad() {
 		GL10 gl = Gdx.graphics.getGL10();
 
 		mBoard = new Board();
 		mBoard.setAll(true);
 
 		// create a hole in center
 		int cx = Board.WIDTH / 2;
 		int cy = Board.HEIGHT / 2;
 		int r = 800;
 		int rr = r * r;
 		for (int i = cx - r; i < cx + r; ++i) {
 			int dx = i - cx;
 			dx *= dx;
 			for (int j = cy - r; j < cy + r; ++j) {
 				int dy = j - cy;
 				dy *= dy;
 				int dd = dx + dy;
 				mBoard.set(i, j, dd > rr);
 			}
 		}
 
 		mCamera = new OrthographicCamera();
 
 		Pixmap tmpPixmap;
 		tmpPixmap = new Pixmap(Gdx.files.internal("data/chitanda0.png"));
 		mBaseTexture0 = new Texture(tmpPixmap, true);
 		tmpPixmap.dispose();
 		tmpPixmap = null;
 		tmpPixmap = new Pixmap(Gdx.files.internal("data/chitanda1.png"));
 		mBaseTexture1 = new Texture(tmpPixmap, true);
 		tmpPixmap.dispose();
 		tmpPixmap = null;
 
 		VertexAttributes va;
 		va = new VertexAttributes( //
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
 
 		mCellTexturePixmap = new Pixmap(CELLTEXTURE_SIZE, CELLTEXTURE_SIZE, Pixmap.Format.RGBA8888);
 
 		va = new VertexAttributes( //
 				new VertexAttribute(VertexAttributes.Usage.Position, 3, "position"),//
 				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "texturecoordinates")//
 		);
 
 		mBlockGroupMesh = new Mesh(true, 4, 4, va);
 		mBlockGroupMesh.setVertices(new float[] { //
 				0f, 1f, 0f, 0f, 1f,//
 						1f, 1f, 0f, 1f, 1f,//
 						0f, 0f, 0f, 0f, 0f,//
 						1f, 0f, 0f, 1f, 0f,//
 				});
 		mBlockGroupMesh.setIndices(new short[] { 0, 1, 2, 3 });
 
 		gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
 		gl.glDisable(GL10.GL_DEPTH_TEST);
 
 		mCameraManager = new CameraCalc();
 		mCameraTouchLogic = new CameraControl(mCameraManager);
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
 		mCameraTouchLogic.update(aDelta);
 
 		mCamera.zoom = mCameraManager.iCameraZoom;
 		mCamera.position.x = mCameraManager.iCameraBX;
 		mCamera.position.y = mCameraManager.iCameraBY;
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
 
 		if (mCameraManager.iCameraZoom < mCameraManager.mZoomMin * PHI * PHI) {
 			float a = mCameraManager.iCameraZoom / (mCameraManager.mZoomMin * PHI);
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
 
 				min = (int) Math.floor(mCameraManager.viewBY0Min());
 				max = (int) Math.ceil(mCameraManager.viewBY0Max());
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
 
 				min = (int) Math.floor(mCameraManager.viewBX0Min());
 				max = (int) Math.ceil(mCameraManager.viewBX0Max());
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
 		int minSide = Math.min(mScreenWidth, mScreenHeight);
 		float blockPerPixel = mCameraManager.iCameraZoom / minSide;
 		if (blockPerPixel > mBlockPerPixelBorder) {
 			int layer = (blockPerPixel > 4) ? 2 //
 					: (blockPerPixel > 1) ? 1 //
 							: 0;
 			aGl.glEnable(GL10.GL_BLEND);
 			aGl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 			aGl.glEnable(GL10.GL_TEXTURE_2D);
 			aGl.glColor4f(1f, 1f, 1f, 1f);
 			int minBX = (int) Math.floor(mCameraManager.screenToBoardX(0));
 			int maxBX = (int) Math.ceil(mCameraManager.screenToBoardX(mScreenWidth));
 			int minBY = (int) Math.floor(mCameraManager.screenToBoardY(mScreenHeight));
 			int maxBY = (int) Math.ceil(mCameraManager.screenToBoardY(0));
 			minBX = minMax(0, minBX, Board.WIDTH);
 			maxBX = minMax(0, maxBX, Board.WIDTH);
 			minBY = minMax(0, minBY, Board.HEIGHT);
 			maxBY = minMax(0, maxBY, Board.HEIGHT);
 			updateCellContent(minBX, maxBX, minBY, maxBY, layer);
 			drawCellTextureV(aGl, minBX, maxBX, minBY, maxBY, layer);
 			// aGl.glColor4f(1f, 1f, 1f, 1f);
 		} else {
 			aGl.glDisable(GL10.GL_BLEND);
 			aGl.glBlendFunc(GL10.GL_ONE, GL10.GL_ZERO);
 			aGl.glDisable(GL10.GL_TEXTURE_2D);
 			int minX = (int) Math.floor(mCameraManager.screenToBoardX(0));
 			int maxX = (int) Math.ceil(mCameraManager.screenToBoardX(mScreenWidth));
 			int minY = (int) Math.floor(mCameraManager.screenToBoardY(mScreenHeight));
 			int maxY = (int) Math.ceil(mCameraManager.screenToBoardY(0));
 			minX = minMax(0, minX, Board.WIDTH);
 			maxX = minMax(0, maxX, Board.WIDTH);
 			minY = minMax(0, minY, Board.HEIGHT);
 			maxY = minMax(0, maxY, Board.HEIGHT);
 			for (int x = minX; x < maxX; ++x) {
 				aGl.glPushMatrix();
 				aGl.glTranslatef(x, 0, 0);
 				for (int y = minY; y < maxY; ++y) {
 					if (mBoard.get0(x, y)) {
 						aGl.glPushMatrix();
 						aGl.glTranslatef(0, y, 0);
 						if ((x + y) % 2 == 0) {
 							aGl.glColor4f(0f, 0f, 0f, 1f);
 						} else {
 							aGl.glColor4f(0.1f, 0.1f, 0.1f, 1f);
 						}
 						mBlockMesh.render(GL10.GL_TRIANGLE_STRIP);
 						aGl.glPopMatrix();
 					}
 				}
 				aGl.glPopMatrix();
 			}
 			aGl.glColor4f(1f, 1f, 1f, 1f);
 		}
 	}
 
 	private int minMax(int aMin, int aV, int aMax) {
 		aV = Math.max(aMin, aV);
 		aV = Math.min(aMax, aV);
 		return aV;
 	}
 
 	@Override
 	public void onScreenResize() {
 		mCameraManager.onScreenResize(mScreenWidth, mScreenHeight);
 		mCamera.viewportWidth = mCameraManager.mViewPortW;
 		mCamera.viewportHeight = mCameraManager.mViewPortH;
 		mBlockPerPixelBorder = 10f / 6 / Gdx.graphics.getPpcX();
 		updateCellTextureV();
 	}
 
 	@Override
 	public boolean touchDown(int x, int y, int pointer, int button) {
 		// iLogger.debug("touchDown");
 		mCameraTouchLogic.touchDown(x, y, pointer, button);
 
 		int minSide = Math.min(mScreenWidth, mScreenHeight);
 		float blockPerPixel = mCameraManager.iCameraZoom / minSide;
 		if (blockPerPixel <= mBlockPerPixelBorder) {
 			int bx = (int) mCameraManager.screenToBoardX(x);
 			int by = (int) mCameraManager.screenToBoardY(y);
 			boolean good = true;
 			good = good && (bx >= 0);
 			good = good && (bx < Board.WIDTH);
 			good = good && (by >= 0);
 			good = good && (by < Board.HEIGHT);
 			if (good) {
 				if (mBoard.get0(bx, by)) {
 					mBoard.set(bx, by, false);
 					int tx = bx / CELLTEXTURE_SIZE;
 					int ty = by / CELLTEXTURE_SIZE;
 					for (int layer = 0; layer < LAYER_COUNT; ++layer) {
 						CellTexture ct = mCellTextureM[layer].remove((tx << 16) | ty);
 						if (ct != null) {
 							ct.mUpdate = false;
 						}
 						tx >>= 2;
 						ty >>= 2;
 					}
 				}
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean touchUp(int x, int y, int pointer, int button) {
 		// iLogger.debug("touchUp");
 		mCameraTouchLogic.touchUp(x, y, pointer, button);
 		return true;
 	}
 
 	@Override
 	public boolean touchDragged(int x, int y, int pointer) {
 		// iLogger.debug("touchDragged");
 		// mTouching[pointer] = true;
 		// mNewTouchEvent = ((mTouchX[pointer] != x) || (mTouchY[pointer] !=
 		// y));
 		// mTouchX[pointer] = x;
 		// mTouchY[pointer] = y;
 		mCameraTouchLogic.touchDragged(x, y, pointer);
 		return true;
 	}
 
 	@Override
 	public boolean touchMoved(int x, int y) {
 		// iLogger.debug("touchMoved");
 		// mMouseOverX = x;
 		// mMouseOverY = y;
 		mCameraTouchLogic.touchMoved(x, y);
 		return true;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		mCameraTouchLogic.scrolled(amount);
 		return true;
 	}
 
 	static final private int CELLTEXTURE_SIZE = 64;
 
 	private class CellTexture implements Disposable, Comparable<CellTexture> {
 		int mCX = -1;
 		int mCY = -1;
 		int mIdx = -1;
 		int mLayer = -1;
 		// int mVersion = -1;
 		boolean mUpdate = false;
 		float mDistanceSq = 0;
 		Texture mTexture = new Texture(CELLTEXTURE_SIZE, CELLTEXTURE_SIZE, Pixmap.Format.RGBA8888);
 
 		@Override
 		public void dispose() {
 			if (mTexture != null)
 				mTexture.dispose();
 			mTexture = null;
 		}
 
 		@Override
 		public int compareTo(CellTexture o) {
 			// reverse sort mDistanceSq, farest get first
 			return Float.compare(o.mDistanceSq, mDistanceSq);
 		}
 
 		public void calcDistance(float aMinBX, float aMaxBX, float aMinBY, float aMaxBY) {
 			if ((!mUpdate) || (mCX == -1) || (mCY == -1) || (mLayer == -1)) {
 				mDistanceSq = Float.MAX_VALUE;
 				return;
 			}
 			float bx = mCX * CELLTEXTURE_SIZE << (mLayer * 2);
 			float by = mCY * CELLTEXTURE_SIZE << (mLayer * 2);
 			float t;
 			aMinBX -= CELLTEXTURE_SIZE << (mLayer * 2);
 			aMinBY -= CELLTEXTURE_SIZE << (mLayer * 2);
 			mDistanceSq = 0;
 			if (bx < aMinBX) {
 				t = aMinBX - bx;
 				mDistanceSq += t * t;
 			}
 			if (aMaxBX < bx) {
 				t = bx - aMaxBX;
 				mDistanceSq += t * t;
 			}
 			if (by < aMinBY) {
 				t = aMinBY - by;
 				mDistanceSq += t * t;
 			}
 			if (aMaxBY < by) {
 				t = by - aMaxBY;
 				mDistanceSq += t * t;
 			}
 		}
 
 		public void update(int aCX, int aCY, int aIdx, int aLayer) {
 			mCX = aCX;
 			mCY = aCY;
 			mIdx = aIdx;
 			mLayer = aLayer;
 			switch (aLayer) {
 			case 0:
 				mBoard.writePixmap0(mCellTexturePixmap, mCX, mCY);
 				break;
 			case 1:
 				mBoard.writePixmap1(mCellTexturePixmap, mCX, mCY);
 				break;
 			case 2:
 				mBoard.writePixmap2(mCellTexturePixmap, mCX, mCY);
 				break;
 			default:
 				throw new IllegalStateException();
 			}
 			mTexture.draw(mCellTexturePixmap, 0, 0);
 			mUpdate = true;
 		}
 	}
 
 	private static final int LAYER_COUNT = 3;
 	private CellTexture[][] mCellTextureV;
 	private TreeMap<Integer, CellTexture>[] mCellTextureM;
 	private Pixmap mCellTexturePixmap;
 
 	@SuppressWarnings("unchecked")
 	private void updateCellTextureV() {
 		iLogger.debug("updateCellTextureV");
 
 		if (mCellTextureV == null)
 			mCellTextureV = new CellTexture[LAYER_COUNT][];
 		if (mCellTextureM == null)
 			mCellTextureM = new TreeMap[LAYER_COUNT];
 
 		// TODO object reuse
 		int ctvw = ((mScreenWidth + (CELLTEXTURE_SIZE - 1)) / CELLTEXTURE_SIZE) + 1;
 		int ctvh = ((mScreenHeight + (CELLTEXTURE_SIZE - 1)) / CELLTEXTURE_SIZE) + 1;
 		int len = ctvw * ctvh;
 		for (int layer = 0; layer < LAYER_COUNT; ++layer) {
 			if (mCellTextureV[layer] != null) {
 				if (mCellTextureV[layer].length == len)
 					continue;
 				for (int i = 0; i < mCellTextureV[layer].length; ++i) {
 					CellTexture ct = mCellTextureV[layer][i];
 					if (ct != null)
 						ct.dispose();
 					mCellTextureV[layer][i] = null;
 				}
 				mCellTextureV[layer] = null;
 			}
 			mCellTextureM[layer] = new TreeMap<Integer, CellTexture>();
 			mCellTextureV[layer] = new CellTexture[len];
 			for (int i = 0; i < len; ++i) {
 				mCellTextureV[layer][i] = new CellTexture();
 			}
 		}
 	}
 
 	private void updateCellContent(float aMinBX, float aMaxBX, float aMinBY, float aMaxBY, int aLayer) {
 		int layerShift = 2 * aLayer;
 		int minCX = ((int) Math.floor(aMinBX / (CELLTEXTURE_SIZE << layerShift)));
 		int maxCX = ((int) Math.ceil(aMaxBX / (CELLTEXTURE_SIZE << layerShift)));
 		int minCY = ((int) Math.floor(aMinBY / (CELLTEXTURE_SIZE << layerShift)));
 		int maxCY = ((int) Math.ceil(aMaxBY / (CELLTEXTURE_SIZE << layerShift)));
 		boolean good = true;
 		fullTest: for (int cx = minCX; cx < maxCX; ++cx) {
 			for (int cy = minCY; cy < maxCY; ++cy) {
 				int idx = (cx << 16) | cy;
 				if (!mCellTextureM[aLayer].containsKey(idx)) {
 					good = false;
 					break fullTest;
 				}
 			}
 		}
 		if (!good) {
 			// iLogger.debug("!good");
 			int offset = 0;
 			sortCellTextureV(aMinBX, aMaxBX, aMinBY, aMaxBY, aLayer);
 			for (int cx = minCX; cx < maxCX; ++cx) {
 				for (int cy = minCY; cy < maxCY; ++cy) {
 					int idx = (cx << 16) | cy;
 					if (!mCellTextureM[aLayer].containsKey(idx)) {
 						CellTexture ct = mCellTextureV[aLayer][offset++];
 						if (ct.mDistanceSq <= 0) {
 							throw new AssertionError();
 						}
 						// iLogger.debug(String.format("remove %08x", ct.mIdx));
 						if (ct.mUpdate) {
 							mCellTextureM[aLayer].remove(ct.mIdx);
 						}
 						ct.update(cx, cy, idx, aLayer);
 						mCellTextureM[aLayer].put(idx, ct);
 					}
 				}
 			}
 		}
 
 //		for (int i = 0; i < mCellTextureV[aLayer].length; ++i) {
 //			mCellTextureV[aLayer][i].calcDistance(aMinBX, aMaxBX, aMinBY, aMaxBY);
 //		}
 //		int c = 0;
 //		for (CellTexture ct : mCellTextureV[aLayer]) {
 //			if (ct.mDistanceSq <= 0) {
 //				++c;
 //			}
 //		}
 //		iLogger.debug("c " + c);
 	}
 
 	private void sortCellTextureV(float aMinBX, float aMaxBX, float aMinBY, float aMaxBY, int aLayer) {
 		for (int i = 0; i < mCellTextureV[aLayer].length; ++i) {
 			mCellTextureV[aLayer][i].calcDistance(aMinBX, aMaxBX, aMinBY, aMaxBY);
 		}
 		Arrays.sort(mCellTextureV[aLayer]);
 	}
 
 	private void drawCellTextureV(GL10 aGl, float aMinBX, float aMaxBX, float aMinBY, float aMaxBY, int aLayer) {
 		int layerShift = 2 * aLayer;
 		int minCX = ((int) Math.floor(aMinBX / (CELLTEXTURE_SIZE << layerShift)));
 		int maxCX = ((int) Math.ceil(aMaxBX / (CELLTEXTURE_SIZE << layerShift)));
 		int minCY = ((int) Math.floor(aMinBY / (CELLTEXTURE_SIZE << layerShift)));
 		int maxCY = ((int) Math.ceil(aMaxBY / (CELLTEXTURE_SIZE << layerShift)));
 		// iLogger.debug("minCX " + minCX);
 		aGl.glPushMatrix();
 		aGl.glScalef(CELLTEXTURE_SIZE << layerShift, CELLTEXTURE_SIZE << layerShift, 1);
 		int idx = -1;
 		for (int cx = minCX; cx < maxCX; ++cx) {
 			aGl.glPushMatrix();
 			aGl.glTranslatef(cx, 0, 0);
 			for (int cy = minCY; cy < maxCY; ++cy) {
 				try {
 					aGl.glPushMatrix();
 					aGl.glTranslatef(0, cy, 0);
 					idx = (cx << 16) | cy;
 					CellTexture ct;
 					ct = mCellTextureM[aLayer].get(idx);
 					ct.mTexture.bind();
 					mBlockGroupMesh.render(GL10.GL_TRIANGLE_STRIP);
 					// mBlockMesh.render(GL10.GL_TRIANGLE_STRIP);
 					aGl.glPopMatrix();
 				} catch (NullPointerException npe) {
 					iLogger.debug(String.format("npe %08x %d", idx, mBoard.getVersion()));
 					throw npe;
 				}
 			}
 			aGl.glPopMatrix();
 		}
 		aGl.glPopMatrix();
 	}
 
 }
