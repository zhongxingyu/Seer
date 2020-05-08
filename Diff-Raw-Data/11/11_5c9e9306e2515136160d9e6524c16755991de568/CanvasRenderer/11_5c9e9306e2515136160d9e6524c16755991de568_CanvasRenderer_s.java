 package com.jfboily.fxgea;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class CanvasRenderer extends SurfaceView implements Runnable
 {
 	private Game game;
 
 	private long startTime;
 	private long endTime;
 	private long deltaTime;
 	private long fpsTime;
 	private int fps;
 	private int tempoFps = 0;
 	
 	private boolean running;
 	
 	private Screen screen;
 	private Input input;
 	
 	private SurfaceHolder holder;
 	
 	private Thread renderThread = null;
 	
 	private Paint debugPaint = new Paint();
 	
 	private Bitmap backBuffer = null;
 	private Canvas backCanvas = null;
 	private Paint backPaint = new Paint();
 	
 	private float scaleX, scaleY;
 	private float backBufferX, backBufferY;
 	
 	private boolean autoScale;
 	
 	// constructeur
 	public CanvasRenderer(Game game, boolean autoScale) 
 	{
 		super(game);
 		this.game = game;	
 		holder = getHolder();
 		
 		this.autoScale = autoScale;
 		
 		backBuffer = Bitmap.createBitmap(game.getWidth(), game.getHeight(), Bitmap.Config.ARGB_8888);
 		backCanvas = new Canvas(backBuffer);
 		
 		scaleX = 1.0f / game.getScaleX();
 		scaleY = 1.0f / game.getScaleY();
 		
 		if(autoScale)
 		{
 			backBufferX = 0.0f;
 			backBufferY = 0.0f;
 		}
 		else
 		{
 			backBufferX = (game.getWidth() * scaleX / 2.0f) - (game.getWidth() / 2.0f);
 			backBufferY = (game.getHeight() * scaleY / 2.0f) - (game.getHeight() / 2.0f);
 		}
 	}
 
 	
 	// THREAD!
 	//@Override
 	public void run() 
 	{		
 		startTime = System.currentTimeMillis();
 		endTime = startTime;
 		deltaTime = 1;
 		fpsTime = startTime;
 		
 		while(running)
 		{
 			startTime = System.currentTimeMillis();
 			
 			//verifie que la surface est valide
 			if(holder.getSurface().isValid())
 			{
 				input = game.getInput();
 				screen = game.getCurrentScreen();
 				
 				// update inputs
 				input.update();
 				
 				// update the screen
 				screen.superUpdate(startTime, deltaTime);
 				
 
 				// rendu du Screen dans le backBuffer
 				screen.render(backCanvas);
 				
 						// draw debug infos
 						debugPaint.setColor(0x55550000);
 						backCanvas.drawRect(0, 0, 100, 32, debugPaint);
 						debugPaint.setColor(0x77ffffff);
 						debugPaint.setTextSize(24);		
 						backCanvas.drawText("FPS : "+fps, 4, 24, debugPaint);
 				
 				// draw le backBuffer sur l'ecran
 				Canvas canvas = holder.lockCanvas();
 				if(autoScale)
 				{
 					canvas.scale(scaleX, scaleY);
 				}
 				// efface tout l'ecran en gris fonce
 				canvas.drawColor(Color.BLACK);
 				// calcule la valeur du fade
 				backPaint.setAlpha(screen.getFadeValue());
 				canvas.drawBitmap(backBuffer, backBufferX, backBufferY, backPaint);
 				holder.unlockCanvasAndPost(canvas);
 			}
 			
 			endTime = System.currentTimeMillis();
 			deltaTime = endTime - startTime;
			long sleepTime = deltaTime < 33 ? 33 - deltaTime: 0;

 			try
 			{
 				Thread.sleep(sleepTime);
 			}catch(Exception e)
 			{
 				Log.e("FXGEA.Screen", "Thread Exception");
 			}
 						
 			if(startTime - fpsTime >= 1000)
 			{
 				fpsTime = endTime;
 				fps = tempoFps;
 				tempoFps = 0;
 			}
 			
 			tempoFps++;
 		}
 		
 	}
 	
 	public void pause()
 	{
 		boolean ok = false;
 		
 		// est-ce que le thread existe??
 		if(renderThread == null)
 			return;
 		
 		// stoppe le thread en quittant la boucle
 		running = false;
 		
 		// attend la fin du rendu de dernier frame
 		while(!ok)
 		{
 			try
 			{
 				renderThread.join();
 				ok = true;
 			}
 			catch(Exception e)
 			{}
 		}
 	}
 	
 	public void resume()
 	{
 		running = true;
 		renderThread = new Thread(this);
 		renderThread.start();
 	}
 	
 	public int getScreenDX()
 	{
 		return (int)backBufferX;
 	}
 	
 	public int getScreenDY()
 	{
 		return (int)backBufferY;
 	}
 	
 	public long getCurrentTime()
 	{
 		return startTime;
 	}
 	
 }
