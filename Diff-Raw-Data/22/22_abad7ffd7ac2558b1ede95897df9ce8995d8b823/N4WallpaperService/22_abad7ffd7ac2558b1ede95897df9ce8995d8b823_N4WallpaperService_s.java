 /*
 Copyright (c) 2012, Patrick O'Leary
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 
 
 1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution. 
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.greentaperacing.olearyp.n4lwp;
 
 import java.util.Random;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuffXfermode;
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
 		private final Sensor mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
 		private final float illumMax = 700f;
 		private float[] rotation = {0.0f, 0.0f, 0.0f};
 		private float illum = illumMax;
 		
 		private final Random rng = new Random(0);
 		
 		private final float[] dotAngles = new float[20];
 		private final Bitmap[] dots = new Bitmap[dotAngles.length];
 		
 		@Override
 		public void onCreate(SurfaceHolder surfaceHolder) {
 			super.onCreate(surfaceHolder);
 			for(int ii = 0; ii < dotAngles.length; ii++) {
 				dotAngles[ii] = (float) (ii * Math.PI / dotAngles.length);
 				dots[ii] = makeDot(16, 14, dotAngles[ii]);
 			}
 		}
 
 		@Override
 		public void onVisibilityChanged(boolean visible) {
 			if (visible) {
 				mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_NORMAL);
 				if(mLight != null) {
 					mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
 				}
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
					
 					float illum_adj = 1.0f;
 					if(illum < illumMax) {
 						illum_adj = (illum + 100)/(illumMax + 100);
 					} 
 					
 					int[] intensity = new int[dotAngles.length];
 					for(int ii = 0; ii < intensity.length; ii++) {
						intensity[ii] = (int) Math.round(180*Math.abs(Math.sin(theta*2.0) * Math.sin(psi + dotAngles[ii] + Math.PI/2)) * illum_adj + 5);
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
 			if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
 				rotation  = event.values;
 				draw();
 			} else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
 				illum = event.values[0];
 			}
 		}
 
 		public float norm(float[] vector) {
 			return FloatMath.sqrt(vector[0] * vector[0] + vector[1] * vector[1]
 					+ vector[2] * vector[2]);
 		}
 
 		public Bitmap makeDot(int gridSize, int dotSize, float angle) {
 			Bitmap b = Bitmap.createBitmap(gridSize, gridSize, Bitmap.Config.ARGB_8888);
 			Canvas c = new Canvas(b);
 			Paint p = new Paint();
 			p.setColor(Color.WHITE);
 			p.setAntiAlias(true);
 			
 			c.drawCircle(gridSize/2.0f, gridSize/2.0f, dotSize/2.0f, p);
 			
 			p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
 			p.setStrokeWidth(dotSize/8);
 			
 			android.graphics.Matrix R_if = new android.graphics.Matrix();
 			R_if.setRotate(angle*180f/(float) Math.PI, gridSize/2.0f, gridSize/2.0f);
 			float[] lineEnds = {
 					gridSize/2.0f, 0,
 					gridSize/2.0f, gridSize,
 					gridSize/4.0f, 0,
 					gridSize/4.0f, gridSize,
 					3.0f*gridSize/4.0f, 0,
 					3.0f*gridSize/4.0f, gridSize,
 			};
 			R_if.mapPoints(lineEnds);
 			c.drawLines(lineEnds, p);
 						
 			return b;
 		}
 	}
 
 }
