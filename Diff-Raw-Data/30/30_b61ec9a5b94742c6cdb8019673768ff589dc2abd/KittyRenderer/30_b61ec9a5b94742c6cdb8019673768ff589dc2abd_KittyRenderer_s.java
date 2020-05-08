 /*
    Copyright 2012 Harri Smatt
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 
 package fi.harism.wallpaper.hellokitty;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.SystemClock;
 import android.widget.Toast;
 
 /**
  * Renderer class.
  */
 public final class KittyRenderer implements GLSurfaceView.Renderer {
 
 	// Bezier curve split count.
 	private static final int BEZIER_VERTEX_COUNT = 20;
 	// Background fill color.
 	private static final float[] COLOR_BG = { .2f, .5f, .8f };
 	// Render states.
 	private static final int STATE_BLINK_EYE_BOTH = 0;
 	private static final int STATE_BLINK_EYE_LEFT = 1;
 	private static final int STATE_BLINK_EYE_RIGHT = 2;
 	private static final int STATE_CLEAR = 3;
 	private static final int STATE_MOVE_BOW = 4;
 	private static final int STATE_MOVE_PAW_LEFT = 5;
 	private static final int STATE_MOVE_PAW_RIGHT = 6;
 	private static final int STATE_NONE = 7;
 	private static final int STATE_RENDERKITTY = 100;
 	// View aspect ratio.
 	private final float mAspectRatio[] = new float[2];
 	// Vertex buffers.
 	private FloatBuffer mBufferBezier;
 	private ByteBuffer mBufferScreen;
 	// Clear layer.
 	private KittyLayer mClearLayer = new KittyLayer("clear",
 			new float[] { 0, 0 }, 1);
 	private final Handler mDelayedHandler = new Handler(Looper.getMainLooper());
 	private Runnable mDelayedRunnable;
 	// Owner surface view.
 	private GLSurfaceView mGLSurfaceView;
 	private final KittyFbo mKittyFbo = new KittyFbo();
 	private final KittySvg mKittySvg = new KittySvg();
 	// Shader variables.
 	private final KittyShader mShaderBezier = new KittyShader();
 	private final boolean[] mShaderCompilerSupport = new boolean[1];
 	private final KittyShader mShaderCopy = new KittyShader();
 	private int mState;
 	private long mTimeStart, mTimeLast = -1;
 	// View width and height.
 	private int mWidth, mHeight;
 
 	/**
 	 * Default constructor.
 	 */
 	public KittyRenderer(GLSurfaceView glSurfaceView) {
 		mGLSurfaceView = glSurfaceView;
 
 		// Screen sized coordinates.
 		final byte SCREEN_COORDS[] = { -1, 1, -1, -1, 1, 1, 1, -1 };
 		mBufferScreen = ByteBuffer.allocateDirect(2 * 4);
 		mBufferScreen.put(SCREEN_COORDS).position(0);
 
 		// Spline float buffer generation.
 		ByteBuffer buf = ByteBuffer.allocateDirect(4 * 4 * BEZIER_VERTEX_COUNT);
 		mBufferBezier = buf.order(ByteOrder.nativeOrder()).asFloatBuffer();
 		for (int i = 0; i < BEZIER_VERTEX_COUNT; ++i) {
 			float t = (float) i / (BEZIER_VERTEX_COUNT - 1);
 			mBufferBezier.put(t).put(-1);
 			mBufferBezier.put(t).put(1);
 		}
 		mBufferBezier.position(0);
 
 		// Initialize clear layer.
 		for (int i = 0; i < 5; ++i) {
 			KittyBezier bezier = new KittyBezier(COLOR_BG, 0, 0);
 			mClearLayer.mBeziers.add(bezier);
 		}
 
 		// Load kitty svg..
 		try {
 			InputStream is = mGLSurfaceView.getContext().getResources()
 					.openRawResource(R.raw.kitty_svg);
 			mKittySvg.read(is);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			showError(ex.getMessage());
 		}
 
 		// Random event Runnable for delayed scheduling.
 		mDelayedRunnable = new Runnable() {
 			@Override
 			public void run() {
 				mTimeLast = -1;
 				mTimeStart = -1;
 
 				double rand = Math.random();
 				if (rand < 0.45) {
 					rand = Math.random();
 					if (rand < 0.4)
 						mState = STATE_BLINK_EYE_LEFT;
 					else if (rand < 0.8)
 						mState = STATE_BLINK_EYE_RIGHT;
 					else
 						mState = STATE_BLINK_EYE_BOTH;
 				} else if (rand < 0.9) {
 					rand = Math.random();
 					if (rand < 0.333)
 						mState = STATE_MOVE_PAW_LEFT;
 					else if (rand < 0.667)
 						mState = STATE_MOVE_PAW_RIGHT;
 					else
 						mState = STATE_MOVE_BOW;
 				} else {
 					mState = STATE_CLEAR;
 				}
 
 				mGLSurfaceView.requestRender();
 			}
 		};
 	}
 
 	/**
 	 * Loads String from raw resources with given id.
 	 */
 	private String loadRawString(int rawId) throws Exception {
 		InputStream is = mGLSurfaceView.getContext().getResources()
 				.openRawResource(rawId);
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		byte[] buf = new byte[1024];
 		int len;
 		while ((len = is.read(buf)) != -1) {
 			baos.write(buf, 0, len);
 		}
 		return baos.toString();
 	}
 
 	@Override
 	public void onDrawFrame(GL10 unused) {
 
 		// If shader compiler is not supported.
 		if (mShaderCompilerSupport[0] == false) {
 			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
 			GLES20.glViewport(0, 0, mWidth, mHeight);
 			GLES20.glClearColor(COLOR_BG[0], COLOR_BG[1], COLOR_BG[2], 1f);
 			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
 			return;
 		}
 
 		// Default settings.
 		GLES20.glDisable(GLES20.GL_BLEND);
 		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
 		GLES20.glDisable(GLES20.GL_CULL_FACE);
 
 		// Render to FBO.
 		mKittyFbo.bind();
 		mKittyFbo.bindTexture(0);
 		boolean requestRender = false;
 		switch (mState) {
 		case STATE_RENDERKITTY:
 			requestRender = renderKitty();
 			break;
 		case STATE_BLINK_EYE_LEFT:
 			requestRender = renderBlinkEye("eye_left_bg", "eye_left");
 			break;
 		case STATE_BLINK_EYE_RIGHT:
 			requestRender = renderBlinkEye("eye_right_bg", "eye_right");
 			break;
 		case STATE_BLINK_EYE_BOTH:
 			requestRender = renderBlinkEye("eye_left_bg", "eye_left",
 					"eye_right_bg", "eye_right");
 			break;
 		case STATE_MOVE_BOW:
 			requestRender = renderMoveLayer("bow");
 			break;
 		case STATE_MOVE_PAW_LEFT:
 			requestRender = renderMoveLayer("paw_left");
 			break;
 		case STATE_MOVE_PAW_RIGHT:
 			requestRender = renderMoveLayer("paw_right");
 			break;
 		case STATE_CLEAR:
 			requestRender = renderClear();
 			break;
 		}
 		if (requestRender) {
 			mGLSurfaceView.requestRender();
 		} else {
 			mState = STATE_NONE;
 			mDelayedHandler.postDelayed(mDelayedRunnable, 5000);
 		}
 
 		// Bind screen buffer.
 		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
 		GLES20.glViewport(0, 0, mWidth, mHeight);
 		// Copy FBO to screen.
 		mShaderCopy.useProgram();
 		int aPosition = mShaderCopy.getHandle("aPosition");
 		GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_BYTE, false, 0,
 				mBufferScreen);
 		GLES20.glEnableVertexAttribArray(aPosition);
 		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
 	}
 
 	@Override
 	public void onSurfaceChanged(GL10 unused, int width, int height) {
 		mWidth = width;
 		mHeight = height;
 
 		mAspectRatio[0] = (float) Math.min(mWidth, mHeight) / mWidth;
 		mAspectRatio[1] = (float) Math.min(mWidth, mHeight) / mHeight;
 
 		mKittyFbo.init(mWidth, mHeight, 1);
 		mTimeStart = mTimeLast = -1;
 		mState = STATE_RENDERKITTY;
 	}
 
 	@Override
 	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
 		// Check if shader compiler is supported.
 		GLES20.glGetBooleanv(GLES20.GL_SHADER_COMPILER, mShaderCompilerSupport,
 				0);
 
 		// If not, show user an error message and return immediately.
 		if (mShaderCompilerSupport[0] == false) {
 			String msg = mGLSurfaceView.getContext().getString(
 					R.string.error_shader_compiler);
 			showError(msg);
 			return;
 		}
 
 		// Try to load shaders.
 		try {
 			String vertexSource, fragmentSource;
 			vertexSource = loadRawString(R.raw.copy_vs);
 			fragmentSource = loadRawString(R.raw.copy_fs);
 			mShaderCopy.setProgram(vertexSource, fragmentSource);
 			vertexSource = loadRawString(R.raw.bezier_vs);
 			fragmentSource = loadRawString(R.raw.bezier_fs);
 			mShaderBezier.setProgram(vertexSource, fragmentSource);
 		} catch (Exception ex) {
 			mShaderCompilerSupport[0] = false;
 			showError(ex.getMessage());
 		}
 	}
 
 	/**
 	 * Renders bezier fill onto current buffer. tStart and tEnd are values
 	 * between [0, 1].
 	 */
 	private void renderBezier(KittyBezier bezier, float[] translate,
 			float scale, float tStart, float tEnd) {
 
 		final float[] ctrlPts = new float[16];
 		for (int i = 0; i < 8; i += 2) {
 			float x1 = bezier.mCtrlPts0[i];
 			float y1 = bezier.mCtrlPts0[i + 1];
 			ctrlPts[i] = x1 * scale + translate[0];
 			ctrlPts[i + 1] = y1 * scale + translate[1];
 
 			float x2 = bezier.mCtrlPts1[i];
 			float y2 = bezier.mCtrlPts1[i + 1];
 			ctrlPts[i + 8] = x2 * scale + translate[0];
 			ctrlPts[i + 9] = y2 * scale + translate[1];
 
 		}
 
 		mShaderBezier.useProgram();
 		int uAspectRatio = mShaderBezier.getHandle("uAspectRatio");
 		int uLimitsT = mShaderBezier.getHandle("uLimitsT");
 		int uControlPts = mShaderBezier.getHandle("uControlPts");
 		int uColor = mShaderBezier.getHandle("uColor");
 		int aBezierPos = mShaderBezier.getHandle("aBezierPos");
 
 		GLES20.glUniform2fv(uAspectRatio, 1, mAspectRatio, 0);
 		GLES20.glUniform2f(uLimitsT, tStart, tEnd);
 		GLES20.glUniform2fv(uControlPts, 8, ctrlPts, 0);
 		GLES20.glUniform3fv(uColor, 1, bezier.mColor, 0);
 
 		GLES20.glVertexAttribPointer(aBezierPos, 2, GLES20.GL_FLOAT, false, 0,
 				mBufferBezier);
 		GLES20.glEnableVertexAttribArray(aBezierPos);
 
 		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0,
 				2 * BEZIER_VERTEX_COUNT);
 
 	}
 
 	private boolean renderBlinkEye(String... layerIds) {
 		long timeCurrent = SystemClock.uptimeMillis();
 		if (mTimeLast < 0) {
 			mTimeStart = mTimeLast = timeCurrent;
 		}
 
 		long diffLast = mTimeLast - mTimeStart;
 		long diffCurrent = timeCurrent - mTimeStart;
 
 		float tStart = diffLast / 500f;
 		float tEnd = diffCurrent / 500f;
 
 		for (int i = 0; i < layerIds.length; ++i) {
 			KittyLayer layer = mKittySvg.getLayer(layerIds[i]);
 			for (KittyBezier bezier : layer.mBeziers) {
 				renderBezier(bezier, layer.mTranslate, layer.mScale,
 						i % 2 == 0 ? tStart : 2 - tEnd, i % 2 == 0 ? tEnd
 								: 2 - tStart);
 			}
 		}
 
 		mTimeLast = timeCurrent;
 		return diffCurrent < 1000;
 	}
 
 	private boolean renderClear() {
 		long timeCurrent = SystemClock.uptimeMillis();
 		if (mTimeLast < 0) {
 			for (KittyBezier bezier : mClearLayer.mBeziers) {
 				float x = (float) (Math.random() * 2 - 1);
 				float y = (float) (Math.random() * 2 - 1);
 				bezier.mCtrlPts0 = new float[] { x, y - 1, x - 1.5f, y - 1.0f,
 						x - 1.5f, y + 1.0f, x, y + 1 };
 				bezier.mCtrlPts1 = new float[] { x, y - 1, x + 1.5f, y - 1.0f,
 						x + 1.5f, y + 1.0f, x, y + 1 };
 			}
 			mTimeStart = mTimeLast = timeCurrent;
 		}
 
 		long diffCurrent = timeCurrent - mTimeStart;
 		float scale = diffCurrent / 3000f;
		// scale = scale * scale * (3 - 2 * scale);
 		for (KittyBezier bezier : mClearLayer.mBeziers) {
 			renderBezier(bezier, mClearLayer.mTranslate, scale * 3, 0f, 1f);
 		}
 
 		mTimeLast = timeCurrent;
 		if (diffCurrent >= 3000) {
 			mTimeLast = mTimeStart = -1;
 			mState = STATE_RENDERKITTY;
 		}
 
 		return true;
 	}
 
 	/**
 	 * 
 	 */
 	private boolean renderKitty() {
 		long timeCurrent = SystemClock.uptimeMillis();
 		if (mTimeStart < 0) {
 			GLES20.glClearColor(COLOR_BG[0], COLOR_BG[1], COLOR_BG[2], 1f);
 			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
 			mTimeStart = mTimeLast = timeCurrent;
 		}
 
 		boolean requestRender = false;
 		for (KittyLayer layer : mKittySvg.getLayers()) {
 			for (KittyBezier bezier : layer.mBeziers) {
 				long diffLast = mTimeLast - mTimeStart;
 				long diffCurrent = timeCurrent - mTimeStart;
 				long diffEnd = bezier.mTimeStart + bezier.mTimeDuration;
 
 				if (diffLast <= bezier.mTimeStart && diffCurrent >= diffEnd) {
 					renderBezier(bezier, layer.mTranslate, layer.mScale, 0f, 1f);
 				} else if ((diffLast >= bezier.mTimeStart && diffLast <= diffEnd)
 						|| (diffCurrent >= bezier.mTimeStart && diffCurrent <= diffEnd)) {
 					float tStart = (float) (diffLast - bezier.mTimeStart)
 							/ bezier.mTimeDuration;
 					float tEnd = (float) (diffCurrent - bezier.mTimeStart)
 							/ bezier.mTimeDuration;
 
 					renderBezier(bezier, layer.mTranslate, layer.mScale,
 							tStart >= 0 ? tStart : 0, tEnd <= 1 ? tEnd : 1);
 				}
 
 				if (diffEnd > diffCurrent) {
 					requestRender = true;
 				}
 			}
 		}
 
 		mTimeLast = timeCurrent;
 		return requestRender;
 	}
 
 	private boolean renderMoveLayer(String layerId) {
 		long timeCurrent = SystemClock.uptimeMillis();
 		if (mTimeStart < 0) {
 			mTimeStart = timeCurrent;
 			mTimeLast = (long) (Math.random() * 1440 - 720);
 		}
 
 		GLES20.glClearColor(COLOR_BG[0], COLOR_BG[1], COLOR_BG[2], 1f);
 		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
 
 		for (KittyLayer layer : mKittySvg.getLayers()) {
 			if (layer.mName.equals(layerId)) {
 				continue;
 			}
 			for (KittyBezier bezier : layer.mBeziers) {
 				renderBezier(bezier, layer.mTranslate, layer.mScale, 0, 1);
 			}
 		}
 
 		long diffCurrent = timeCurrent - mTimeStart;
 		if (diffCurrent > 2880) {
 			diffCurrent = 2880;
 		}
 
 		float t = (float) Math.sin(diffCurrent * Math.PI / 2880);
 		t = t * t * t * (3 - 2 * t) * 0.05f;
 		float dx = (float) Math.sin((diffCurrent + mTimeLast) * Math.PI / 720);
 		float dy = (float) Math.cos((diffCurrent + mTimeLast) * Math.PI / 720);
 		if (mTimeLast < 0) {
 			float tmp = dx;
 			dx = dy;
 			dy = tmp;
 		}
 
 		KittyLayer layer = mKittySvg.getLayer(layerId);
 		final float[] translate = { 0, 0 };
 		translate[0] = layer.mTranslate[0] + t * dx;
 		translate[1] = layer.mTranslate[1] + t * dy;
 		for (KittyBezier bezier : layer.mBeziers) {
 			renderBezier(bezier, translate, layer.mScale, 0, 1);
 		}
 
 		return diffCurrent < 2880;
 	}
 
 	/**
 	 * Shows Toast on screen with given message.
 	 */
 	private void showError(final String errorMsg) {
 		mDelayedHandler.post(new Runnable() {
 			@Override
 			public void run() {
 				Toast.makeText(mGLSurfaceView.getContext(), errorMsg,
 						Toast.LENGTH_LONG).show();
 			}
 		});
 	}
 
 }
