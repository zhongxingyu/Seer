 package eu.whrl.lsystemwallpaper;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.service.wallpaper.WallpaperService;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 
 class DrawingPosition {
 	float x;
 	float y;
 	float angle;
 	public DrawingPosition() {
 		x = 0;
 		y = 0;
 		angle = 0;
 	}
 	public DrawingPosition(float x, float y, float a) {
 		this.x = x;
 		this.y = y;
 		this.angle = a;
 	}
 	public DrawingPosition copy() {
 		return new DrawingPosition(x, y, angle);
 	}
 }
 
 public class LSystemDrawingService extends WallpaperService {
 
 	@Override
 	public Engine onCreateEngine() {
 		return new LSystemDrawingEngine();
 	}
 	
 	public enum DrawingState {
 		CALCULATE,
 		ERROR,
 		DRAW,
 		FADE
 	}
 	
 	private class LSystemDrawingEngine extends Engine {
 		
 		private final Handler handler = new Handler();
 		private final Runnable drawRunner = new Runnable() {
 			@Override
 			public void run() {
 				draw();
 			}
 
 		};
 		
 		private boolean visible = true;
 
 		private LSystem lsystem = null;
 		private int currentCommand = 0;
 		
 		private DrawingPosition drawPos = new DrawingPosition(50.0f, 200.0f, 0.0f);
 		private List<DrawingPosition> drawPosStack = null;
 		
 		private DrawingPosition originDrawPos = drawPos.copy();
 		
 		private List<Path> tailLines;
 		private Path currentTailLine;
 		
 		DrawingState state = DrawingState.CALCULATE;
 		private int calculationCount = 0;
 		
 		private Paint tailPaint = new Paint();
 		private Paint headPaint = new Paint();
 		
 		class LSystemGenerator extends AsyncTask<LSystemDescription,Void,LSystem> {
 
 			@Override
 			protected LSystem doInBackground(LSystemDescription... params) {
 				if (params.length == 1) { 
 					LSystemDescription d = params[0];
 					LSystem lsystem = new LSystem(d.iterations, 
 							d.turnAngle, 
 							d.startState, 
 							d.functions);
 					return lsystem;
 				}
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(LSystem l) {
 				if (l != null) {
 					lsystem = l;
 					state = DrawingState.DRAW;
 				} else {
 					state = DrawingState.ERROR;
 				}
 			}
 		}
 		
 		public LSystemDrawingEngine() {		
 			
 			drawPosStack = new LinkedList<DrawingPosition>();
 			
 			tailPaint.setAntiAlias(true);
 			tailPaint.setColor(Color.GRAY);
 			tailPaint.setStyle(Paint.Style.STROKE);
 			tailPaint.setStrokeWidth(4f);
 			
 			headPaint.setAntiAlias(true);
 			headPaint.setColor(Color.MAGENTA);
 			headPaint.setStyle(Paint.Style.STROKE);
 			headPaint.setStrokeWidth(5f);
 			
 			LSystemDescription lsDesc = new LSystemDescription();
 			lsDesc.name = "hilbert";
 			lsDesc.functions = new String[3];
 			lsDesc.functions[0] = "f::20";
 			lsDesc.functions[1] = "l:+rf-lfl-fr+:0";
 			lsDesc.functions[2] = "r:-lf+rfr+fl-:0";
 			lsDesc.startState = "l";
 			lsDesc.iterations = 5;
 			lsDesc.turnAngle = 90.0f;
 			
 			/*
 			LSystemDescription lsDesc = new LSystemDescription();
 			lsDesc.name = "tree";
 			lsDesc.functions = new String[2];
 			lsDesc.functions[0] = "f:g[-f][+f][gf]:10";
 			lsDesc.functions[1] = "g:gg:10";
 			lsDesc.startState = "f";
 			lsDesc.iterations = 5;
 			lsDesc.turnAngle = 45.0f;
 			*/
 			
 			new LSystemGenerator().execute(lsDesc);
 			
 			currentTailLine = new Path();
 			currentTailLine.moveTo(drawPos.x, drawPos.y);
 			
 			tailLines = new LinkedList<Path>();
 			tailLines.add(currentTailLine);
 			
 			handler.post(drawRunner);
 		}
 
 		@Override
 		public void onVisibilityChanged(boolean visible) {
 			this.visible = visible;
 			if (visible) {
 				handler.post(drawRunner);
 			} else {
 				handler.removeCallbacks(drawRunner);
 			}
 		}
 
 		@Override
 		public void onSurfaceDestroyed(SurfaceHolder holder) {
 			super.onSurfaceDestroyed(holder);
 			this.visible = false;
 			handler.removeCallbacks(drawRunner);
 		}
 
 		@Override
 		public void onSurfaceChanged(SurfaceHolder holder, int format,
 				int width, int height) {
 			super.onSurfaceChanged(holder, format, width, height);
 		}
 
 		@Override
 		public void onTouchEvent(MotionEvent event) {
 			
 		}
 
 		private void draw() {
 			SurfaceHolder holder = getSurfaceHolder();
 			Canvas canvas = null;
 			try {
 				canvas = holder.lockCanvas();
 				if (canvas != null) {
 					canvas.drawColor(Color.BLACK);
 					if (state == DrawingState.CALCULATE) {
 						canvas.drawLine(50, 200, 50, 200 + calculationCount, headPaint);
 						calculationCount++;
 					} else if (state == DrawingState.DRAW) {
 						drawLSystem(canvas);
 					} else if (state == DrawingState.FADE) {
 						fadeLSystem(canvas);
 					}
 				}
 			} finally {
 				if (canvas != null)
 					holder.unlockCanvasAndPost(canvas);
 			}
 			handler.removeCallbacks(drawRunner);
 			if (visible) {
 				handler.postDelayed(drawRunner, 200);
 			}
 		}
 		
 		private void drawOlderLines(Canvas canvas) {
 			for (Path tailLine : tailLines) {
 				canvas.drawPath(tailLine, tailPaint);
 			}
 		}
 		
 		private void fadeLSystem(Canvas canvas) {
 			int newAlpha = tailPaint.getAlpha() - 16;
 			if (newAlpha < 0) {
 				changeToDraw();
 				return;
 			}
 			tailPaint.setAlpha(newAlpha);
 			drawOlderLines(canvas);
 		}
 		
 		private void drawLSystem(Canvas canvas) {
 			
 			drawOlderLines(canvas);
 			
 			float newX = drawPos.x;
 			float newY = drawPos.y;
 			
 			LSystem.Command cmd = lsystem.commands[currentCommand];
 			
 			boolean found = false;
 			
 			// Find the next command that is a move that isn't distance 0.
 			while (!found) {
 				if ((cmd instanceof LSystem.Move)) {
 					if (((LSystem.Move)cmd).dist > 0.0f) {
 						found = true;
 					}
 				}
 				
 				if (!found) {
 					if (cmd instanceof LSystem.Turn) {
 						drawPos.angle += ((LSystem.Turn)cmd).angle;
 					}
 					
 					if (cmd instanceof LSystem.BranchStart) {
 						drawPosStack.add(drawPos.copy());
 					}
 					
 					if (cmd instanceof LSystem.BranchEnd) {
 						if (drawPosStack.size() > 0) {
 							drawPos = drawPosStack.remove(drawPosStack.size()-1);
 							currentTailLine = new Path();
 							currentTailLine.moveTo(drawPos.x, drawPos.y);
 							tailLines.add(currentTailLine);
 						} else {
 							Log.w("LSystem", "Encountered branch end with no matching branch start, skipping.");
 						}
 					}
 
 					currentCommand++;
 					if (currentCommand == lsystem.commands.length) {
 						changeToFade();
 						return;
 					}
 					cmd = lsystem.commands[currentCommand];
 				}
 			}
 			
 			// Calculate the destination of the move.
 			float distance = ((LSystem.Move)cmd).dist;
 			double radians = Math.toRadians(drawPos.angle);
 			newX = drawPos.x + (float) (Math.cos(radians)*distance);
 			newY = drawPos.y + (float) (Math.sin(radians)*distance);
 			
 			// Draw our new line
 			canvas.drawLine(drawPos.x, drawPos.y, newX, newY, headPaint);
 			
 			currentTailLine.lineTo(newX, newY);
 			
 			// Update our position
 			drawPos.x = newX;
 			drawPos.y = newY;
 			
 			// Move onto the next command.
 			currentCommand++;
 			if (currentCommand == lsystem.commands.length) {
 				changeToFade();
 			}
 		}
 		
 		private void changeToFade() {
 			currentCommand = 0;
 			state = DrawingState.FADE;
 		}
 		
 		private void changeToDraw() {
 			tailLines.clear();
 			currentTailLine = new Path();
			currentTailLine.moveTo(originDrawPos.x, originDrawPos.y);
 			tailLines.add(currentTailLine);
 			drawPos = originDrawPos.copy();
 			tailPaint.setAlpha(255);
 			state = DrawingState.DRAW;
 		}
 	} 
 }
