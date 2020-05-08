 package com.greentaperacing.olearyp.n4lwp;
 
 import java.util.Random;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.opengl.Matrix;
 import android.service.wallpaper.WallpaperService;
 import android.util.FloatMath;
 import android.view.SurfaceHolder;
 
 public class N4WallpaperService extends WallpaperService {
 
 	@Override
 	public Engine onCreateEngine() {
 		return new N4WallpaperEngine();
 	}
 
 	private class N4WallpaperEngine extends Engine implements SensorEventListener {
 
 		private final SensorManager mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
 		private final Sensor mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
 		private float[] rotation = {0.0f, 0.0f, 0.0f};
 		
		private final Random rng = new Random(0);
		
 		private final Bitmap[] dots = {
 				BitmapFactory.decodeResource(getResources(),R.drawable.dot1),
 				BitmapFactory.decodeResource(getResources(),R.drawable.dot2),
 				BitmapFactory.decodeResource(getResources(),R.drawable.dot3),
 				BitmapFactory.decodeResource(getResources(),R.drawable.dot4),
 		};
 		
 		@Override
 		public void onVisibilityChanged(boolean visible) {
 			if (visible) {
 				mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_NORMAL);
 				draw();
 			} else {
 				mSensorManager.unregisterListener(this);
 			}
 		}
 
 		@Override
 		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 			draw();
 		}
 
 		@Override
 		public void onSurfaceDestroyed(SurfaceHolder holder) {
 			super.onSurfaceDestroyed(holder);
 		}
 
 		@Override
 		public void onDestroy() {
 			super.onDestroy();
 			mSensorManager.unregisterListener(this);
 		}
 
 		private void draw() {
 			SurfaceHolder holder = getSurfaceHolder();
 			Canvas c = null;
 
 			try {
 				c = holder.lockCanvas();
 				if (c != null) {
 					float[] R_dw = new float[16];
 					SensorManager.getRotationMatrixFromVector(R_dw, rotation);
 
 					// Compute vector normal to screen in world coordinates
 					final float[] n_d = {0.0f, 0.0f, 1.0f, 1.0f};
 					float[] n_w = new float[4];
 					Matrix.multiplyMV(n_w, 0, R_dw, 0, n_d, 0);
 					
 					// cos(theta) = n_w[3]
 					double theta = Math.acos(n_w[2]);
 					
 					// Compute screen up vector in world coordinates
 					final float[] u_d = {0.0f, 1.0f, 0.0f, 1.0f};
 					float[] u_w = new float[4];
 					Matrix.multiplyMV(u_w, 0, R_dw, 0, u_d, 0);
 					
 					// Compute projection of world up vector onto screen, expressed in world
 					float[] proj_wu_w = {-n_w[2]*n_w[0], -n_w[2]*n_w[1], 1.0f-n_w[2]*n_w[2]};
 					
 					// cos(psi) = dot(u_w, proj_wu_w)/norm(proj_wu_w)
 					double psi = Math.acos((u_w[0]*proj_wu_w[0] + u_w[1]*proj_wu_w[1] + u_w[2]*proj_wu_w[2])/norm(proj_wu_w));
 					
					// Compute psi for each dot rotation
 					double[] psi_rotations = {psi-Math.PI/5.0, psi-2.0*Math.PI/5.0, psi-3.0*Math.PI/5.0, psi-4.0*Math.PI/5.0};
 					
 					int[] intensity = new int[4];
 					for(int ii = 0; ii < intensity.length; ii++) {
						intensity[ii] = (int) Math.round(180*Math.abs(Math.sin(theta*2.0) * Math.sin(psi_rotations[ii])));
 					}
 					
 					c.drawColor(Color.BLACK);
 					Paint p = new Paint();
 					p.setColor(Color.WHITE);
 					p.setAntiAlias(true);
 
 					int hMax = c.getHeight();
 					int wMax = c.getWidth();
 					int r = 10;
					rng.setSeed(0);
 					for(int ii = 0; ii*2*r < wMax; ii++) {
 						for(int jj = 0; jj*2*r < hMax; jj++) {
 							int dot = rng.nextInt(dots.length);
 							p.setAlpha((int) intensity[dot]);
 							c.drawBitmap(dots[dot], ii*2*r, jj*2*r, p);
 						}
 					}
 				}
 			} finally {
 				if (c != null)
 					holder.unlockCanvasAndPost(c);
 			}
 		}
 
 		@Override
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {
 			// pass
 		}
 
 		@Override
 		public void onSensorChanged(SensorEvent event) {
 			rotation  = event.values;
 			draw();
 		}
 	}
 
 	public static float norm(float[] vector) {
 		return FloatMath.sqrt(vector[0] * vector[0] + vector[1] * vector[1]
 				+ vector[2] * vector[2]);
 	}
 }
