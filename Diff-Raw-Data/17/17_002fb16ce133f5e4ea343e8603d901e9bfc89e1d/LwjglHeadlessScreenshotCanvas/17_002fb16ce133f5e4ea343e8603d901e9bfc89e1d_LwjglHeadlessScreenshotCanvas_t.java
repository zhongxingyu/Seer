 package com.github.neothemachine.ardor3d.screenshot;
 
 import java.awt.image.BufferedImage;
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.concurrent.Callable;
 
 import javax.management.RuntimeErrorException;
 
 import org.lwjgl.LWJGLException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.ardor3d.framework.Canvas;
 import com.ardor3d.framework.DisplaySettings;
 import com.ardor3d.framework.Scene;
 import com.ardor3d.intersection.PickResults;
 import com.ardor3d.math.Ray3;
 import com.ardor3d.renderer.Renderer;
 import com.ardor3d.renderer.TextureRendererFactory;
 import com.ardor3d.renderer.lwjgl.LwjglTextureRendererProvider;
 import com.ardor3d.scenegraph.Node;
 import com.ardor3d.util.ContextGarbageCollector;
 import com.ardor3d.util.GameTaskQueue;
 import com.ardor3d.util.GameTaskQueueManager;
 import com.ardor3d.util.screen.ScreenExporter;
 
 /**
  * Work in progress
  * 
  * 
  * @author maik
  * 
  */
 public class LwjglHeadlessScreenshotCanvas implements ScreenshotCanvas, Scene,
 		Runnable {
 
 	private static final Logger log = LoggerFactory
 			.getLogger(LwjglHeadlessScreenshotCanvas.class);
 
 	private final Collection<UncaughtExceptionHandler> uncaughtExceptionHandlers = new LinkedList<UncaughtExceptionHandler>();
 
 	private final IntDimension size;
 	private LwjglHeadlessCanvas canvas;
 	private Renderer renderer;
 
 	private Canvas canvasWrapper;
 
 	private final Node root = new Node();
 
 	private final ScreenShotBufferExporter screenShotExp = new ScreenShotBufferExporter();
 
 	private Throwable lastUncaughtException = null;
 
 	private boolean isExitRequested = false;
 	private boolean isExitDone = false;
 	private final Object exitDoneMonitor = new Object();
 
 	private boolean isShotRequested = false;
 	private final Object shotRequestedMonitor = new Object();
 	private final Object shotFinishedMonitor = new Object();
 
 	public LwjglHeadlessScreenshotCanvas(IntDimension size) {
 
 		this.size = size;
 				
 		GameTaskQueueManager.getManager(this).getQueue(GameTaskQueue.UPDATE)
 				.setExecuteMultiple(true);
 		GameTaskQueueManager.getManager(this).getQueue(GameTaskQueue.RENDER)
 				.setExecuteMultiple(true);
 
 		// Don't know if this is necessary, probably not, but it doesn't hurt.
 		// For our own queues, we need it because we only want to render exactly
 		// two frames
 		// and don't wait until all queued actions are executed.
 		// The internal queue here is only used internally when disposing the
 		// canvas and
 		// deleting textures etc., and at least the javadoc says that only ONE
 		// frame
 		// needs to be rendered, so they probably don't enqueue more than one
 		// action.
 
 		// TODO doesn't work as our wrapper is too stupid
 		// this.queueCanvasUpdate(new CanvasUpdate() {
 		// @Override
 		// public void update(Canvas canvas) {
 		// GameTaskQueueManager.getManager(canvas.getCanvasRenderer().getRenderContext()).
 		// getQueue(GameTaskQueue.RENDER).setExecuteMultiple(true);
 		// }
 		// });
 
 		TextureRendererFactory.INSTANCE
 				.setProvider(new LwjglTextureRendererProvider());
 
 		Thread renderThread = new Thread(this);
 		renderThread
 				.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
 					@Override
 					public void uncaughtException(Thread t, Throwable e) {
 						log.error(e.getMessage(), e);
						isShotRequested = false;
 						lastUncaughtException = e;
 						// wake up takeShot() on error
 						synchronized (shotFinishedMonitor) {
 							shotFinishedMonitor.notifyAll();
 						}
 						for (UncaughtExceptionHandler eh : uncaughtExceptionHandlers) {
 							eh.uncaughtException(t, e);
 						}
 						dispose();
 					}
 				});
 		renderThread.start();
 
 	}
 
 	@Override
 	public void queueSceneUpdate(final SceneGraphUpdate update) {
 		GameTaskQueueManager.getManager(this).update(new Callable<Void>() {
 			@Override
 			public Void call() throws Exception {
 				update.update(root);
 				root.updateGeometricState(0);
 				return null;
 			}
 		});
 	}
 
 	@Override
 	public void queueCanvasUpdate(final CanvasUpdate update) {
 		GameTaskQueueManager.getManager(this).render(new Callable<Void>() {
 			@Override
 			public Void call() throws Exception {
 				update.update(canvasWrapper);
 				return null;
 			}
 		});
 	}
 
 	@Override
 	public IntDimension getSize() {
 		return this.size;
 	}
 
 	@Override
 	public BufferedImage takeShot() {
 		synchronized (shotRequestedMonitor) {
 			if (isShotRequested) {
 				throw new IllegalStateException(
 						"Another screenshot is already in progress");
 			}
 			// canvas.getCanvasRenderer().makeCurrentContext();
 			// ^ not necessary, as canvas.draw() already does that 
 			isShotRequested = true;
 			shotRequestedMonitor.notifyAll();
 		}
 		synchronized (shotFinishedMonitor) {
 			while (isShotRequested) {
 				try {
 					shotFinishedMonitor.wait();
 				} catch (InterruptedException e) {
 				}
			}
			if (lastUncaughtException != null) {
				throw new Ardor3DRenderException(lastUncaughtException);
 			}
 		}
 
 		return screenShotExp.getLastImage();
 	}
 
 	@Override
 	public void addUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
 		this.uncaughtExceptionHandlers.add(eh);
 	}
 
 	@Override
 	public boolean renderUnto(Renderer renderer) {
		
 		GameTaskQueueManager.getManager(this).getQueue(GameTaskQueue.UPDATE)
 				.execute();
 
 		GameTaskQueueManager.getManager(this).getQueue(GameTaskQueue.RENDER)
 				.execute(renderer);
 
 		// necessary because internal ardor3d code relies on this queue
 		// it happens after our own queue so that dispose() works correctly
 		// see
 		// http://ardor3d.com/forums/viewtopic.php?f=13&t=1020&p=16253#p16253
 		// GameTaskQueueManager.getManager(canvas.getRenderer().getRenderContext()).
 		// getQueue(GameTaskQueue.RENDER).execute(renderer);
 
 		// Clean up card garbage such as textures, vbos, etc.
 		ContextGarbageCollector.doRuntimeCleanup(renderer);
 
 		root.draw(renderer);
		
 		if (isShotRequested) {
 			// force any waiting scene elements to be rendered.
 			renderer.renderBuckets();
 			ScreenExporter.exportCurrentScreen(canvasWrapper
 					.getCanvasRenderer().getRenderer(), screenShotExp);
 			synchronized (shotFinishedMonitor) {
 				isShotRequested = false;
 				this.shotFinishedMonitor.notifyAll();
 			}
 		}
 		return true;
 
 	}
 
 	@Override
 	public void run() {
 		
 		int aaSamples = 0;
 		
 		final DisplaySettings settings = new DisplaySettings(size.getWidth(),
 				size.getHeight(), 24, 1, 8, 8, 0, aaSamples, false, false);
 
 		this.canvas = new LwjglHeadlessCanvas(settings, this);
 		this.canvasWrapper = new LwjglHeadlessCanvasWrapper(this.canvas);
 		this.renderer = this.canvas.getRenderer();
 		
 		while (!isExitRequested) {
 			synchronized (shotRequestedMonitor) {
 				while (!isShotRequested && !isExitRequested) {
 					try {
 						// this should be a new monitor for the invariant
 						// !isShotRequested && !isExitRequested
 						// but anyway.. we just notifyAll shotRequestedMonitor
 						// in exit()
 						shotRequestedMonitor.wait();
 					} catch (InterruptedException e) {
 					}
 				}
 			}
 			isShotRequested = false;
 			this.canvas.draw();
 			isShotRequested = true;
 			this.canvas.draw();
 		}
 		this.doDispose();
 	}
 
 	@Override
 	public void dispose() {
 		if (lastUncaughtException != null) {
 			return;
 		}
 		isExitRequested = true;
 		synchronized (exitDoneMonitor) {
 			synchronized (shotRequestedMonitor) {
 				shotRequestedMonitor.notifyAll(); // this is a hack, see run()
 			}
 
 			while (!isExitDone) {
 				try {
 					exitDoneMonitor.wait();
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 	}
 
 	private void doDispose() {
 		try {
 			this.canvas.cleanup();
 			isExitDone = true;
 			synchronized (exitDoneMonitor) {
 				this.exitDoneMonitor.notify();
 			}
 		} catch (Exception e) {
 			log.error("Error disposing canvas resources", e);
 		}
 	}
 
 	@Override
 	public PickResults doPick(Ray3 pickRay) {
 		throw new UnsupportedOperationException();
 	}
 
 }
