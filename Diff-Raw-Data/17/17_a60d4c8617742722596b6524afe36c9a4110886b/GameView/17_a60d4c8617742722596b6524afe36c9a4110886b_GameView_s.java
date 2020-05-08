 package edu.brandeis.tjhickey.fireflies;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.PointF;
 import android.graphics.Paint.Style;
 import android.view.SurfaceHolder;
 
 /**
  * this is an implementation of the firefly game in which you use your finger
  * to move a net which captures fireflies. This class creates the GameModel
  * and starts a GameLoop.  It also contains code for the redraw() method.
  * Since we are using SurfaceView, there is much less use of synchronized methods or statements
  * @author tim
  *
  */
 public class GameView {
 	
 	public GameModel gm;
 	private Paint mPaint;
 	private Paint fPaint; // fireflies
 	private Paint wPaint; // wasps
 	private Paint aPaint; // avatars
 	private Paint tempPaint;
 	private PointF tempPoint = new PointF(0f, 0f);
 	
 	private SurfaceHolder surfaceHolder;
 	boolean readyToDraw = false;
 	
 	private float width,height;
 
 
 	public GameView(SurfaceHolder sh){
 
 		this.surfaceHolder = sh;
 		
 		initFireflyCanvas();
 		
 		//create GameModel and start the GameLoop
 		gm = new GameModel(100,200);
 		GameLoop gl = new GameLoop(gm,this);
 		Thread t = new Thread(gl);
 		t.start();
 	}
 	
 	
 	
 	public void start(){
 		this.readyToDraw = true;
 		gm.start();
 	}
 	
 	public void stop(){
 		this.readyToDraw = false;
 		gm.stop();
 	}
 	
 	public void setGameModel(GameModel gm) {
 		this.gm = gm;
 	}
 
 	public void moveAvatar(PointF dp){
 	  gm.moveAvatar(toModelCoords(dp));
 	}
 	
 	public void changeDimensions(float w, float h) {
 		this.width = w;
 		this.height = h;
 	}
 	private void initFireflyCanvas(){
 
 		// Set up default Paint values
 		mPaint = new Paint();
 		mPaint.setAntiAlias(true);
 		mPaint.setStyle(Style.FILL);
 		mPaint.setColor(Color.BLUE);
 
 		fPaint = new Paint();
 		fPaint.setAntiAlias(true);
 		fPaint.setStyle(Style.FILL);
 		fPaint.setColor(Color.GREEN);
 
 		wPaint = new Paint();
 		wPaint.setAntiAlias(true);
 		wPaint.setStyle(Style.FILL);
 		wPaint.setColor(Color.RED);
 		wPaint.setTextSize(50f);
 
 		aPaint = new Paint();
 		aPaint.setAntiAlias(true);
 		aPaint.setStyle(Style.STROKE);
 		aPaint.setColor(Color.WHITE);
 
 	}
 	
 	
 	public void redraw(){
 		if (!readyToDraw) return;
 		
 		Canvas canvas=surfaceHolder.lockCanvas();
 		
 		clearBackground(canvas);
 		drawActors(canvas);
 
 		drawActor(canvas, gm.avatar);
 		drawActor(canvas, gm.hole);
 		//canvas.drawText("" + System.nanoTime(), 50, 50, wPaint);
		canvas.drawText("Score: " + (gm.score - gm.minusScore), 50, 50, wPaint);
 		if (gm.gameOver){
			if ((gm.score - gm.minusScore) > 0) {
				canvas.drawText("You Win!", 200, 200, wPaint);
			} else {
				canvas.drawText("Sorry, You Lose", 200, 200, wPaint);
			}
 		}
 		
 		
 		surfaceHolder.unlockCanvasAndPost(canvas);
 	}
 
 	private void drawActors(Canvas canvas) {
 		try{
 		for (GameActor a : gm.actors) {
 			if(!a.scored) {
 				drawActor(canvas, a);
 			}
 		}
 		} catch(Exception e){
 			System.out.println("error in drawActors:"+e);
 		}
 	}
 
 	private void drawActor(Canvas canvas, GameActor a) {
 		this.tempPoint.x = (float) a.x;
 		this.tempPoint.y = (float) a.y;
 		Point r = this.toViewCoords(tempPoint);
 		
 		tempPaint = getPaint(a);
 
 		canvas.drawCircle(r.x, r.y, this.toViewCoords(a.radius), tempPaint);
 	}
 
 	private Paint getPaint(GameActor a) {
 		if (a.species == Species.firefly) {
 			return fPaint;
 		} 
 		if (a.species == Species.wasp) {
 			return wPaint;
 		}
 		
 		else {
 			return aPaint;
 		}
 	}
 
 	private void clearBackground(Canvas canvas) {
 		canvas.drawRect(0, 0, width, height, mPaint);
 	}
 
 	/**
 	 * toViewCoords(x) converts from model coordinates to pixels on the screen
 	 * so that objects can be drawn to scale, i.e. as the screen is resized the
 	 * objects change size proportionately.
 	 * 
 	 * @param x
 	 *            the unit in model coordinates
 	 * @return the corresponding value in pixel based on window-size
 	 */
 	public float toViewCoords(float x) {
 		float viewSize = (width < height) ? width : height;
 		return x / gm.size * viewSize;
 	}
 
 	public Point toViewCoords(PointF p) {
 		Point q = new Point(0, 0);
 		float viewSize = (width < height) ? width : height;
 		q.x = (int) Math.round(p.x / gm.size * viewSize);
 		q.y = (int) Math.round(p.y / gm.size * viewSize);
 		return q;
 	}
 
 	/**
 	 * toModelCoords(x) is used to convert mouse locations to positions in the
 	 * model so that the avatar position in the model can be changed correctly
 	 * 
 	 * @param x
 	 *            position in pixels in view
 	 * @return position in model coordinates
 	 */
 	public float toModelCoords(float x) {
 		float viewSize = (width < height) ? width : height;
 		return x * gm.size / viewSize;
 	}
 
 	public PointF toModelCoords(PointF p) {
 		PointF q = new PointF(0f, 0f);
 		float viewSize = (width < height) ? width : height;
 		q.x = (float) (p.x * gm.size / viewSize);
 		q.y = (float) (p.y * gm.size / viewSize);
 		return q;
 	}
 
 }
