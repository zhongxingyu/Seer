 /* @license
  * This file is part of the Game Closure SDK.
  *
  * The Game Closure SDK is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * The Game Closure SDK is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with the Game Closure SDK.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.tealeaf;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.nio.IntBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 
 import android.opengl.GLES20;
 import android.os.Build;
 import android.os.Bundle;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.view.Display;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 
 import com.tealeaf.event.ImageLoadedEvent;
 import com.tealeaf.event.ResumeEvent;
 import com.tealeaf.event.RedrawOffscreenBuffersEvent;
 
 public class TeaLeafGLSurfaceView extends com.tealeaf.GLSurfaceView {
 	private TeaLeaf context;
 	private Renderer renderer;
	private boolean started = false;
 	public boolean sendResumeEvent = false;
 	private ArrayList<TextureData> loadedImages = new ArrayList<TextureData>();
 	protected boolean saveTextures = false;
 	protected Object lastFrame = new Object();
 	protected long glThreadId = 0;
 	public boolean queuePause = false;
 
 	public TeaLeafOptions getOptions() {
 		return context.getOptions();
 	}
 
 	public boolean isGLThread() {
 		return Thread.currentThread().getId() == glThreadId;
 	}
 
 	public TeaLeafGLSurfaceView(TeaLeaf context) {
 		super(context);
 
 		// Create an OpenGL ES 2.0 context.
 		setEGLContextClientVersion(2);
 		setPreserveEGLContextOnPause(true);
 
 		this.context = context;
 		renderer = new Renderer(this);
 		this.setOnTouchListener(renderer);
 	}
 
 	public void queueResumeEvent() {
 		synchronized (this) {
 			sendResumeEvent = true;
 		}
 	}
 
 	public boolean isResumeEventQueued() {
 		boolean queued = false;
 		synchronized (this) {
 			queued = sendResumeEvent;
 		}
 		return queued;
 	}
 
 	public void checkResumeEvent() {
 		boolean send = false;
 		synchronized (this) {
 			if (sendResumeEvent) {
 				send = true;
 				sendResumeEvent = false;
 			}
 		}
 		if (send) {
 			renderer.state = renderer.READY;
 
 			String[] events = { new ResumeEvent().pack() };
 			NativeShim.dispatchEvents(events);
 		}
 	}
 
 	public void setRendererStateReloading() {
 		renderer.state = renderer.RELOADING;
 	}
 
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		if (started) {
 			super.onWindowFocusChanged(hasFocus);
 		}
 	}
 
 	@Override
 	protected void onDetachedFromWindow() {
 		if (started) {
 			super.onDetachedFromWindow();
 		}
 	}
 
 	@Override
 	public void onPause() {
 		logger.log("{gl} Pause");
 
 		if (started) {
 			saveTextures = true;
 			this.queuePause = true;
 		}
 	}
 
 	@Override
 	public void onResume() {
 		logger.log("{gl} Resume");
 		if (started) {
 			renderer.onResume();
 			super.onResume();
 		}
 	}
 
 	public void destroy() {
 		logger.log("{gl} Destroy");
 		renderer.destroy();
 	}
 
 	public boolean running() {
 		boolean running = false;
 		if (renderer != null && renderer.state == renderer.READY) {
 			running = true;
 		}
 		return running;
 	}
 
 	public void start() {
 		started = true;
 		setRenderer(renderer);
 	}
 
 	public void restart() {
 		renderer.restart();
 	}
 
 	public void startCrossPromo(String appid) {
 		renderer.setCrossPromoTarget(appid);
 	}
 
 	// FIXME This functionality should probably be in its own class.
 	public void pushLoadedImage(TextureData textureData) {
 		synchronized (loadedImages) {
 			loadedImages.add(textureData);
 		}
 	}
 
 	// FIXME separate this with pushLaodedImage
 	protected ArrayList<TextureData> getLoadedImages() {
 		ArrayList<TextureData> ret;
 		synchronized (loadedImages) {
 			ret = new ArrayList<TextureData>(loadedImages);
 			loadedImages.clear();
 		}
 		return ret;
 	}
 
 	// Clear the loadedImage list and the texture load queue in textureLoader
 	public void clearLoadedImageQueue() {
 		renderer.textureLoader.clearTextureLoadQueue();
 
 		synchronized (loadedImages) {
 			loadedImages.clear();
 		}
 
 		logger.log("{gl} Clearing the in-flight loads");
 	}
 
 	// FIXME separate this with the above 2 functions
 	protected void finishLoadingImages() {
 		for (TextureData td : getLoadedImages()) {
 			if (td.loaded) {
 				long then = new Date().getTime();
 				renderer.textureLoader.finishLoadingTexture(td);
 				long now = new Date().getTime();
 				logger.log("{gl} Finish loading texture took", now - then, "ms");
 				if (!td.url.startsWith("@TEXT")) {
 					EventQueue.pushEvent((new ImageLoadedEvent(td.url,
 							td.width, td.height, td.originalWidth,
 							td.originalHeight, td.name)));
 				}
 				// Number of channels (last argument) is always 4 for now
 				// (RGBA8888)
 				NativeShim.onTextureLoaded(td.url, td.name, td.width,
 						td.height, td.originalWidth, td.originalHeight, 4);
 			} else {
 				NativeShim.onTextureFailedToLoad(td.url);
 			}
 		}
 	}
 
 	public void clearTextures() {
 		NativeShim nativeShim = renderer.getNativeShim();
 		if (nativeShim != null) {
 			nativeShim.clearTextureData();
 		}
 	}
 
 	public TextureLoader getTextureLoader() {
 		return renderer.getTextureLoader();
 	}
 
 	@Override
 	public boolean dispatchTouchEvent(MotionEvent ev) {
 		// if we get to the GL surface with a touch, then we should clear the
 		// focus on the textbox layer
 		if (context.hasTextInputView()) {
 			context.getTextInputView().defocus();
 		}
 		return super.dispatchTouchEvent(ev);
 	}
 
 	public static class Renderer implements GLSurfaceView.Renderer,
 			OnTouchListener {
 		private TeaLeafGLSurfaceView view;
 
 		private NativeShim nativeShim;
 		private boolean shouldReloadTextures = true;
 		private boolean runCrossPromo = false;
 		private boolean initJS = true;
 		private String crossPromoTarget = "";
 
 		private ResourceManager resourceManager;
 		private ServiceWrapper service = null;
 
 		public Renderer(TeaLeafGLSurfaceView view) {
 			logger.log("{gl} Created renderer");
 			this.view = view;
 			resourceManager = this.view.context.getResourceManager();
 			// FIXME is this really where we should create the service wrapper?
 			// no, but we need to create it arbitrarily early to give it time to
 			// spin up before the game starts
 			service = new ServiceWrapper(this.view.context);
 		}
 
 		public void destroy() {
 			service.unbind();
 			if (nativeShim != null) {
 				NativeShim.destroy();
 				nativeShim = null;
 			}
 		}
 
 		public void setCrossPromoTarget(String appid) {
 			runCrossPromo = true;
 			crossPromoTarget = appid;
 		}
 
 		public void startCrossPromo() {
 			runCrossPromo = false;
 			view.context.runOnUiThread(new Runnable() {
 				public void run() {
 					view.destroy();
 					Intent startGame = view.context.getPackageManager()
 							.getLaunchIntentForPackage(
 									view.context.getPackageName());
 					startGame.putExtra("appid", crossPromoTarget);
 					view.context.overridePendingTransition(0, 0);
 					view.context.finish();
 					view.context.startActivity(startGame);
 				}
 			});
 		}
 
 		public TextureLoader getTextureLoader() {
 			return textureLoader;
 		}
 
 		public void onPause() {
 			if (nativeShim != null && state == READY) {
 				nativeShim.onPause();
 			}
 		}
 
 		public void onResume() {
 			if (nativeShim != null && state == READY) {
 				shouldReloadTextures = true;
 				nativeShim.onResume();
 			}
 		}
 
 		// FIXME the renderer probably doesn't also need to be the onTouch
 		// listener. We should move this to a dedicated class
 		public boolean onTouch(View v, MotionEvent event) {
 			int activePointer = event.getActionIndex();
 			int id = event.getPointerId(activePointer);
 			float x = Math.min(Math.max(0, event.getX(activePointer)), width);
 			float y = Math.min(Math.max(0, event.getY(activePointer)), height);
 			int type;
 			int eventType = event.getAction();
 
 			switch (eventType & MotionEvent.ACTION_MASK) {
 			case MotionEvent.ACTION_DOWN:
 			case MotionEvent.ACTION_POINTER_1_DOWN:
 				type = 1;
 				break;
 			case MotionEvent.ACTION_MOVE:
 				type = 2;
 				break;
 			case MotionEvent.ACTION_UP:
 			case MotionEvent.ACTION_POINTER_1_UP:
 			case MotionEvent.ACTION_CANCEL:
 				type = 3;
 				break;
 			default:
 				type = -1;
 			}
 
 			synchronized (InputEvents.lock) {
 				InputEvents.push(id, type, (int) x, (int) y);
 			}
 
 			for (int i = 0; i < event.getPointerCount(); i++) {
 				if (i == activePointer) {
 					continue;
 				}
 				id = event.getPointerId(i);
 				x = Math.min(Math.max(0, event.getX(i)), width);
 				y = Math.min(Math.max(0, event.getY(i)), height);
 				type = 2;
 				synchronized (InputEvents.lock) {
 					InputEvents.push(id, type, (int) x, (int) y);
 				}
 			}
 			return true;
 		}
 
 		// FIXME this should be moved into its own file and maybe started
 		// earlier
 		class JSInitializer implements Runnable {
 			public void run() {
 				if (!NativeShim.initIsolate()) {
 					state = FIRST_INIT_FAIL;
 					logger.log("{js} ERROR: Unable to initialize isolate");
 				} else {
 					if (NativeShim.initJS(view.context.getLaunchUri(),
 							view.context.getOptions().getAndroidHash())) {
 						state = FIRST_LOAD;
 					} else {
 						state = FIRST_INIT_FAIL;
 						logger.log("{js} ERROR: Unable to retrieve native.js.mp3");
 					}
 				}
 			}
 		}
 
 		private boolean beginJSInitialization() {
 			state = 0;
 
 			Thread thread = new Thread(new JSInitializer());
 			thread.setName("JS Thread");
 			thread.start();
 
 			return true;
 		}
 
 		private void handleInitFail(final String title, final String prompt) {
 			if (view.context.getOptions().isDevelop()) {
 				// Popup on every failed attempt, and try forever
 				view.context.runOnUiThread(new Runnable() {
 					public void run() {
 						String[] buttons = { "Retry", "Cancel" };
 						Runnable[] cbs = { new Runnable() {
 							public void run() {
 								Renderer.this.state = FIRST_RUN;
 							}
 						}, new Runnable() {
 							public void run() {
 								view.context.reset();
 							}
 						} };
 						JSDialog.showDialog(view.context, null, title, prompt,
 								buttons, cbs);
 					}
 				});
 			}
 		}
 
 		// Initialization states
 		private static final int FIRST_RUN = 1;
 		private static final int FIRST_LOAD = 2;
 		private static final int FIRST_INIT_FAIL = 3;
 		private static final int WAIT_FOR_RETRY = 4;
 		private static final int READY = 5;
 		private static final int RELOADING = 6;
 
 		private int state = FIRST_RUN;
 
 		public void onDrawFrame(GL10 gl) {
 
 			switch (state) {
 			case FIRST_RUN:
 				if (!beginJSInitialization()) {
 					logger.log("{js} Retrying initialization of JavaScript VM...");
 					state = WAIT_FOR_RETRY;
 					handleInitFail("Initialization error",
 							"Unable to initialize JavaScript VM");
 				}
 				break;
 			case FIRST_INIT_FAIL:
 				logger.log("{js} Retrying native.js.mp3 download...");
 				state = WAIT_FOR_RETRY;
 				handleInitFail("Connect error",
 						"Unable to contact server to download native.js.mp3");
 				break;
 			case WAIT_FOR_RETRY:
 				break;
 			case FIRST_LOAD:
 				runJS();
 				state = READY;
 				break;
 			// fall-thru
 			case RELOADING:
 				// fall-thru
 				NativeShim.reloadCanvases();
 				state = READY;
 			case READY:
 				view.checkResumeEvent();
 				handleInputEvents();
 				handleGameEvents();
 				break;
 			}
 
 			if (state != FIRST_RUN) {
 				step();
 			}
 
 			if (view.queuePause) {
 				view.queuePause = false;
 				//save textures
 				if (view.saveTextures) {
 					view.saveTextures = false;
 					NativeShim.saveTextures();
 				}
 				//pause the renderer and glview
 				this.onPause();
 			}
 		}
 
 		private void runJS() {
 			this.view.glThreadId = Thread.currentThread().getId();
 			NativeShim.resizeScreen(width, height);
 			NativeShim.run();
 			state = READY;
 		}
 
 		static class InputEvents {
 			public static final int MAX_EVENTS = 32;
 			public static int[] ids = new int[MAX_EVENTS];
 			public static int[] types = new int[MAX_EVENTS];
 			public static int[] xs = new int[MAX_EVENTS];
 			public static int[] ys = new int[MAX_EVENTS];
 
 			private static int index = 0;
 
 			public static Object lock = new Object();
 
 			public static void push(int id, int type, int x, int y) {
 				if (index < MAX_EVENTS) {
 					ids[index] = id;
 					types[index] = type;
 					xs[index] = x;
 					ys[index] = y;
 					index++;
 				}
 			}
 
 			public static void clear() {
 				index = 0;
 			}
 		}
 
 		private void handleInputEvents() {
 			synchronized (InputEvents.lock) {
 				if (InputEvents.index > 0) {
 					NativeShim.dispatchInputEvents(InputEvents.ids,
 							InputEvents.types, InputEvents.xs, InputEvents.ys,
 							InputEvents.index);
 					InputEvents.clear();
 				}
 			}
 		}
 
 		private void handleGameEvents() {
 			EventQueue.dispatchEvents();
 			if (view.context.hasOverlay()
 					&& view.context.getOverlay().getProgress() == 100) {
 				view.context.getOverlay().ready();
 			}
 			if (runCrossPromo) {
 				startCrossPromo();
 			}
 		}
 
 		private long lastMS = System.currentTimeMillis();
 
 		private void step() {
 			long now = System.currentTimeMillis();
 			view.finishLoadingImages();
 			NativeShim.step((int) (now - lastMS));
 			lastMS = now;
 		}
 
 		private int width, height;
 		private TextureLoader textureLoader;
 
 		private boolean trySplashImage(String fileName) {
 			try {
 				TeaLeaf tealeaf = this.view.context;
 				if (Arrays.asList(tealeaf.getResources().getAssets().list("resources")).contains(fileName)) {
 					tealeaf.getOptions().setSplash(fileName);
 					return true;
 				}
 			} catch (Exception e) {
 			}
 			return false;
 		}
 
 		private void selectSplashScreen() {
 			// Get screen width and height
 			int sw = 0, sh = 0;
 			Point size = new Point();
 			TeaLeaf tealeaf = this.view.context;
 			WindowManager w = tealeaf.getWindowManager();
 
 			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 				w.getDefaultDisplay().getSize(size);
 				sw = size.x;
 				sh = size.y;
 			} else {
 				Display d = w.getDefaultDisplay();
 				sw = d.getWidth();
 				sh = d.getHeight();
 			}
 
 			// Calculate longer screen side
 			int longerScreenSide = sw;
 			if (longerScreenSide < sh) {
 				longerScreenSide = sh;
 			}
 
 			boolean success = false;
 			
 			if (longerScreenSide > 1024) {
 				success = trySplashImage("splash-2048.png");
 			}
 			
 			if (!success && longerScreenSide > 512) {
 				success = trySplashImage("splash-1024.png");
 			} 
 			
 			if (!success) {
 				success = trySplashImage("splash-512.png");
 			}
 
 			if (!success) {
 				logger.log("{core} WARNING: Unable to find a suitable splash image");
 			}
 
 			logger.log("{core} Device screen (", sw, ",", sh, "), using splash '", tealeaf.getOptions().getSplash(), "'");
 		}
 		
 		public void onSurfaceChanged(GL10 gl, int width, int height) {
 
 			if (this.view.context.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
 					&& height > width) {
 				int tempWidth = width;
 				width = height;
 				height = tempWidth;
 			} else if (this.view.context.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
 					&& width > height) {
 				int tempWidth = width;
 				width = height;
 				height = tempWidth;
 			}
 
 			this.width = width;
 			this.height = height;
 			if (initJS) {
 				TeaLeafOptions options = view.context.getOptions();
 				logger.log("{js} Initializing JS config");
 
 				this.selectSplashScreen();
 
 				NativeShim.init(nativeShim, view.context.getCodeHost(),
 						options.getTcpHost(), options.getCodePort(),
 						options.getTcpPort(), options.getEntryPoint(),
 						options.getSourceDir(), width, height,
 						options.isRemoteLoading(), options.getSplash(),
 						options.getSimulateID());
 
 				// set halfsized textures
 				Settings settings = view.context.getSettings();
 				boolean useHalfsizedTextures = settings.getBoolean(
 						"@__use_halfsized_textures__", false);
 				NativeShim.setHalfsizedTextures(useHalfsizedTextures);
 
 				initJS = false;
 			}
 			NativeShim.resizeScreen(width, height);
 		}
 
 		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 			logger.log("{gl} OpenGL version",
 					GLES20.glGetString(GLES20.GL_VERSION), "surface created");
 
 			if (nativeShim == null) {
 				// TODO: make the text manager earlier
 				TextManager textManager = new TextManager(view.context);
 				ContactList contactList = view.context.getContactList();
 
 				// TODO: make the texture loader earlier
 				textureLoader = new TextureLoader(view.context,
 						resourceManager, textManager, contactList);
 				nativeShim = new NativeShim(textManager, textureLoader,
 						this.view.context.getSoundQueue(),
 						this.view.context.getLocalStorage(), contactList,
 						new LocationManager(view.context), service,
 						resourceManager, view.context);
 			}
 
 			if (shouldReloadTextures) {
 				NativeShim.initGL(0);
 
 				// Clear out anything that is currently being loaded
 				view.clearLoadedImageQueue();
 
 				NativeShim.reloadTextures();
 			}
 
 			EventQueue.pushEvent(new RedrawOffscreenBuffersEvent());
 
 		}
 
 		public Bitmap getScreenshot(GL10 gl) {
 			logger.log("{screenshot} Taking screenshot");
 			int[] buf = new int[width * height];
 			int[] buf2 = new int[width * height];
 
 			IntBuffer ib = IntBuffer.wrap(buf);
 			ib.position(0);
 			gl.glReadPixels(0, 0, width, height, GL10.GL_RGBA,
 					GL10.GL_UNSIGNED_BYTE, ib);
 
 			for (int i = 0, k = 0; i < height; i++, k++) {
 				for (int j = 0; j < width; j++) {
 					int pix = buf[i * width + j];
 					int pb = (pix >> 16) & 0xff;
 					int pr = (pix << 16) & 0x00ff0000;
 					int pix1 = (pix & 0xff00ff00) | pr | pb;
 					buf2[(height - k - 1) * width + j] = pix1;
 				}
 			}
 			return Bitmap.createBitmap(buf2, width, height,
 					Bitmap.Config.ARGB_8888);
 		}
 
 		/*
 		 * @Override public void onSurfaceLost() {
 		 * logger.log("{gl} Surface lost"); shouldReloadTextures = true; }
 		 */
 
 		public NativeShim getNativeShim() {
 			return nativeShim;
 		}
 
 		public void restart() {
 			destroy();
 			state = FIRST_RUN;
 		}
 	}
 }
