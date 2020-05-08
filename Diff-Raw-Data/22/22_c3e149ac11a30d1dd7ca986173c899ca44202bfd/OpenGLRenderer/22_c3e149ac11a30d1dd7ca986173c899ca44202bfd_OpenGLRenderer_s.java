 package com.bunkerdev.chooser;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.PointF;
 import android.opengl.GLSurfaceView;
 import android.opengl.GLSurfaceView.Renderer;
 import android.os.CountDownTimer;
 import android.view.MotionEvent;
 
 public class OpenGLRenderer implements Renderer {
 
 	private GLSurfaceView view;
 	private LinkedList<Token> tokens = new LinkedList<Token>();
 	private ArrayList<Choice> choices = new ArrayList<Choice>();
 	private GLBitmap needle;
 	private GLBitmap circle;
 	private GLBitmap lastNearest;
 	private Bitmap tokenBmp;
 	private Bitmap bgBmp;
 	private Bitmap needleBmp;
 	private WeightedRandom random;
 	private int width;
 	private int height;
 	public static double[] CENTER;
 	private PointF pointDown;
 	private long timeDown;
 	private Timer timer;
 	private TimerTask updateTask;
 	private double remainingDegrees;
 
 	private int lastFrameDraw = 0;
 	private int frameSampleTime = 0;
 	private int frameSamplesCollected = 0;
 	private int fps = 0;
 	private long now = 0l;
 
 	public OpenGLRenderer(GLSurfaceView v, Bitmap t, Bitmap n, Bitmap bg) {
 		view = v;
 		tokenBmp = t;
 		bgBmp = bg.copy(Config.RGB_565, false);
 		needleBmp = n.copy(Config.ARGB_4444, false);
 		random = new WeightedRandom(choices);
 
 		CENTER = new double[2];
 	}
 
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		// Set the background color to black ( rgba ).
 		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
 		// Enable Smooth Shading, default not really needed.
 		gl.glShadeModel(GL10.GL_SMOOTH);
 		// Depth buffer setup.
 		gl.glClearDepthf(1.0f);
 		// Enables depth testing.
 		gl.glEnable(GL10.GL_DEPTH_TEST);
 		// The type of depth testing to do.
 		gl.glDepthFunc(GL10.GL_LEQUAL);
 		// Really nice perspective calculations.
 		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
 	}
 
 	public void onSurfaceChanged(GL10 gl, int w, int h) {
 		width = w;
 		height = h;
 		CENTER[0] = (double) width / 2;
 		CENTER[1] = (double) height / 2;
 		//TODO Radio: minimo(w, h)
 		circle = new GLBitmap(bgBmp, width / 2, height / 2, width, width);
 		needle = new GLBitmap(needleBmp, width / 2, height / 2, width, width);
 		needle.setAngle(0);
 		// needle.setRotAngle(10);
 		// Sets the current view port to the new size.
 		gl.glViewport(0, 0, width, height);
 		// Select the projection matrix
 		gl.glMatrixMode(GL10.GL_PROJECTION);
 		// Reset the projection matrix
 		gl.glLoadIdentity();
 		// Poniendolo en ortogonal
 		// left, right, bottom, top, near, far
 		gl.glOrthof(0, w, -0, h, -1, 1);
 		// Calculate the aspect ratio of the window
 		// GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f,
 		// 100.0f);
 		// Select the modelview matrix
 		gl.glMatrixMode(GL10.GL_MODELVIEW);
 		// Reset the modelview matrix
 		gl.glLoadIdentity();
 
 	}
 
 	public void onDrawFrame(GL10 gl) {
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 		gl.glLoadIdentity();
 		circle.draw(gl);
 		needle.draw(gl);
 		synchronized (tokens) {
 			for (Token d : tokens) {
 				d.draw(gl);
 			}
 		}
 		// long now = System.currentTimeMillis();
 		// if (lastFrameDraw != 0) {
 		// int time = (int) (now - lastFrameDraw);
 		// frameSampleTime += time;
 		// frameSamplesCollected++;
 		// if (frameSamplesCollected == 10) {
 		// fps = (int) (10000 / frameSampleTime);
 		// Main.debug("fps: %d   size: %d", fps, tokens.size());
 		// frameSampleTime = 0;
 		// frameSamplesCollected = 0;
 		// }
 		// }
 		// lastFrameDraw = (int) now;
 	}
 
 	// public void addDrawable(GLDrawable d) {
 	// synchronized (drawables) {
 	// drawables.add(d);
 	// }
 	// }
 
 	public void addToken(float x, float y) {
 		// x e y calculado porque android y opengl tienen distinto eje de
 		// coordenadas
 		Choice c = new Choice("");
 		choices.add(c);
 		synchronized (tokens) {
 			tokens.add(new Token(c, tokenBmp, x, height - y));
 		}
 	}
 
 	public void actionDown(PointF p) {
 		pointDown = p;
 		timeDown = System.currentTimeMillis();
 	}
 
 	public void actionUp(MotionEvent event) {
 		float xDelta = (pointDown.x - event.getX())
 				* (pointDown.x - event.getX());
 		float yDelta = (pointDown.y - event.getY())
 				* (pointDown.y - event.getY());
 		float distance = (float) Math.sqrt(xDelta + yDelta);
 		if (distance < 10.0) {
 			addToken(event.getX(), event.getY());
 			view.requestRender();
 		} else {
 			if (!tokens.isEmpty()) {
 				random = new WeightedRandom(choices);
 				Token chosen = choose();
 				
 				float speed = distance
 						/ (System.currentTimeMillis() - timeDown);
 				int turns = (int) Math.round(2.857142857 * speed + 2);
 
 				remainingDegrees = 360 * turns;
 				double angle1 = Math.abs(needle.getAngle() - chosen.getAngle());
 				double angle2 = 360 - (Math.abs(needle.getAngle() - chosen.getAngle()));
 
 				if(Math.abs((needle.getAngle()+angle1) - chosen.getAngle())<0.01)
 					remainingDegrees += angle1;
 				else
 					remainingDegrees += angle2;
 				
				initializeTimer();
				timer.schedule(updateTask, 0, 50);
 			}
 		}
 	}
 
 	private void update() {
 		if (!tokens.isEmpty()) {
 			double step = -0.0202 * Math.pow(remainingDegrees / 360, 3.0)
 					+ 0.1096 * Math.pow(remainingDegrees / 360, 2.0) + 2.8465
 					* remainingDegrees / 360 + 2;
 			if(remainingDegrees < 2.0)
 				step = remainingDegrees;
 			
 			needle.incrementAngle(step);
 			remainingDegrees -= step;
 	
 			GLBitmap actual = getNearestToken().getGLBitmap();
 			if (lastNearest != null && !actual.equals(lastNearest)) {
 				lastNearest.setHeight(50);
 				lastNearest.setWidth(50);
 			}
 			actual.setHeight(80);
 			actual.setWidth(80);
 			lastNearest = actual;
 			view.requestRender();
 		}
 	}
 
 	private void initializeTimer() {
		timer = new Timer();
 		updateTask = new TimerTask() {
 
 			@Override
 			public void run() {
				if (remainingDegrees <= 0)
					timer.cancel();
 				update();
 			}
 		};
 	}
 
 	private Token getNearestToken() {
 		Token nearest = null;
 		double min = Double.MAX_VALUE;
 		double actual = 0.0;
 		for (Token t : tokens) {
 			actual = Math.min(Math.abs(needle.getAngle() - t.getAngle()),
 					360 - (Math.abs(needle.getAngle() - t.getAngle())));
 			// Main.debug("distancia a %s: %f",
 			// choices.get(tokens.indexOf(t)).getName(), actual);
 			if (actual < min) {
 				min = actual;
 				nearest = t;
 			}
 		}
 		// Main.debug("nearest: %s, distancia: %f, aguja: %f",
 		// choices.get(tokens.indexOf(nearest)).getName(), min,
 		// needle.getAngle());
 		return nearest;
 	}
 
 	private Token choose() {
 		Choice c = random.getChoice(1).get(0);
 		return tokens.get(choices.indexOf(c));
 	}
 
 }
