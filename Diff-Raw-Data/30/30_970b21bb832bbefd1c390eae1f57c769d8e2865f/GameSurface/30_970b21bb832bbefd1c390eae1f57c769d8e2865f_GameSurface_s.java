 package cs252.lab6.atoms;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.Typeface;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class GameSurface extends SurfaceView
 {
 	private GameThread thread = null;
 	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 	private SurfaceHolder holder;
 	private int size;
 	private int yOffset;
 	private Player[] players;
 	private Atom[][] grid;
 	private Point[][] strays;
 	private int straysPos;
 	private int numStrays;
 	private int turn;
 	private boolean exploding;
 	private boolean gameOver;
	
	public static final int TIMER_TICK = 2;
 	
 	public GameSurface(Context context)
 	{
 		super(context);
 	}
 	
 	public GameSurface(Context context, AttributeSet attrs)
 	{
 		super(context, attrs);
 	}
 	
 	public GameSurface(Context context, AttributeSet attrs, int defStyle)
 	{
 		super(context, attrs, defStyle);
 	}
 	
 	public void onResume()
 	{
 		players = new Player[4];
 		int[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};
 		players[0] = new Player("me", Color.RED, false);
 		
 		for(int i=1;i<4;i++)
 			players[i] = new Player("Player " + (i + 1), colors[i], true);
 		
 		grid = new Atom[5][5];
 		
 		for(int i=0;i<5;i++)
 		{
 			for(int j=0;j<5;j++)
 			{
 				int rings = 3;
 				if(i == 0 || i == 4) rings--;
 				if(j == 0 || j == 4) rings--;
				grid[i][j] = new Atom(rings);
 			}
 		}
 		
 		strays = new Point[1024][];
 		straysPos = 0;
 		numStrays = 0;
 		turn = 0;
 		exploding = false;
 		gameOver = false;
 		holder = getHolder();
 		thread = new GameThread(this, holder);
 		thread.setRunning(true);
 		thread.start();
 	}
 	
 	public void onPause()
 	{
 		boolean retry = true;
 		thread.setRunning(false);
 		while(retry)
 		{
 			try
 			{
 				thread.join();
 				retry = false;
 			}
 			catch(InterruptedException e) { e.printStackTrace(); }
 		}
 	}
 	
 	public void botTurn()
 	{
 		int x;
 		int y;
 		
 		do
 		{
 			x = (int)(Math.random() * 5);
 			y = (int)(Math.random() * 5);
 		}
		while(!grid[x][y].getPlayer().getName().equals("null") && grid[x][y].getPlayer() != players[turn]);
 		
 		explode(x, y);
 	}
 	
 	public void explode(int x, int y)
 	{
 		exploding = true;
 		grid[x][y].setPlayer(players[turn]);
 		grid[x][y].addElectron();
 		checkWin();
 		if(grid[x][y].getRings() < grid[x][y].getNum())
 		{
 			grid[x][y].sendOutElectrons();
 			if(x > 0) sendStray(x, y, x-1, y);
 			if(x < 4) sendStray(x, y, x+1, y);
 			if(y > 0) sendStray(x, y, x, y-1);
 			if(y < 4) sendStray(x, y, x, y+1);
 		}
 		
 		if(numStrays == 0)
 			nextTurn();
 	}
 	
 	public void sendStray(int x, int y, int dx, int dy)
 	{
 		float nucSize = size / 4;
 		float ringInc = size / 16;
 		
 		int elec = grid[x][y].getOutermostElec();
 		double electron = grid[x][y].getElecs()[elec];
 		Point[] points = new Point[2];
 		points[0] = new Point((int)((x + 0.5) * size + (nucSize + ringInc * elec) * Math.cos(electron)), (int)(((y + 0.5) * size + (nucSize + ringInc * elec) * Math.sin(electron)) + yOffset));
 		points[1] = new Point(dx, dy);
 		strays[straysPos++] = points;
 		numStrays++;
 	}
 	
 	public void nextTurn()
 	{
 		straysPos = 0;
 		exploding = false;
 		turn = (turn + 1) % players.length;
 		
 		for(int i=0;i<5;i++)
 			for(int j=0;j<5;j++)
				if(grid[i][j].getPlayer().getName().equals("null") || grid[i][j].getPlayer() == players[turn])
 				{
 					if(players[turn].isBot())
 						botTurn();
 					return;
 				}
 		
 		nextTurn();
 	}
 	
 	public void checkWin()
 	{
 		for(int i=0;i<5;i++)
 			for(int j=0;j<5;j++)
				if(grid[i][j].getPlayer().getName().equals("null") || grid[i][j].getPlayer() != players[turn])
 					return;
 		
 		gameOver = true;
 	}
 	
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		int x = (int)(event.getX() / size);
 		int y = (int)((event.getY() - yOffset) / size);
 		
		if(!gameOver && !exploding && x >= 0 && x < 5 && y >= 0 && y < 5 && (grid[x][y].getPlayer().getName().equals("null") || grid[x][y].getPlayer() == players[turn]))
 			explode(x, y);
 		
 		return false;
 	}
 	
 	public void updateStates()
 	{
 		float nucSize = size / 4;
 		float ringInc = size / 16;
 		float straySpeed = size / 10;
 		
 		for(int i=0;i<5;i++)
 			for(int j=0;j<5;j++)
 				grid[i][j].moveElectrons();
 		
 		for(int i=0;i<strays.length;i++)
 		{
 			if(strays[i] != null)
 			{
 				Point[] elec = strays[i];
 				int outermost = grid[elec[1].x][elec[1].y].getOutermostElec();
 				double electron = grid[elec[1].x][elec[1].y].getElecs()[outermost];
 				double dx = (elec[1].x + 0.5) * size + (nucSize + ringInc * outermost) * Math.cos(electron);
 				double dy = (elec[1].y + 0.5) * size + (nucSize + ringInc * outermost) * Math.sin(electron) + yOffset;
 				double theta = Math.atan2(dy - elec[0].y, dx - elec[0].x);
 				double distX = straySpeed * Math.cos(theta);
 				double distY = straySpeed * Math.sin(theta);
 				if(dx - elec[0].x < straySpeed && dy - elec[0].y < straySpeed)
 				{
 					explode(elec[1].x, elec[1].y);
 					strays[i] = null;
 					if(--numStrays == 0)
 						nextTurn();
 				}
 				else
 				{
 					elec[0].x += distX;
 					elec[0].y += distY;
 				}
 			}
 		}
 	}
 	
 	public void drawAtom(Canvas canvas, float x, float y, Atom atom)
 	{
 		float nucSize = size / 4;
 		float neutSize = size / 6;
 		float proSize = size / 8;
 		float ringInc = size / 16;
 		float elecSize = size / 32;
 		
 		for(int i=0;i<atom.getRings();i++)
 		{
 			double pro = atom.getNuc() + i * ((2*Math.PI) / 3);
 			paint.setColor(atom.getBackColor());
 			paint.setStyle(Paint.Style.FILL);
 			canvas.drawCircle((float)(x + proSize * Math.cos(pro)), (float)(y + proSize * Math.sin(pro)), proSize, paint);
 			paint.setColor(Color.rgb(50, 50, 50));
 			paint.setStyle(Paint.Style.STROKE);
 			canvas.drawCircle((float)(x + proSize * Math.cos(pro)), (float)(y + proSize * Math.sin(pro)), proSize, paint);
 			paint.setColor(Color.WHITE);
 			canvas.drawCircle((float)x, (float)y, nucSize + ringInc * (i + 1), paint);
 		}
 		
 		paint.setColor(atom.getColor());
 		paint.setStyle(Paint.Style.FILL);
 		canvas.drawCircle(x, y, neutSize, paint);
 		paint.setColor(Color.rgb(50, 50, 50));
 		paint.setStyle(Paint.Style.STROKE);
 		canvas.drawCircle(x, y, neutSize, paint);
 		
 		int elecs = atom.getNum() < atom.getRings() ? atom.getNum() : atom.getRings();
 		for(int i=0;i<elecs;i++)
 		{
 			paint.setColor(atom.getColor());
 			paint.setStyle(Paint.Style.FILL);
 			canvas.drawCircle((float)(x + (nucSize + ringInc * (i + 1)) * Math.cos(atom.getElecs()[i])), (float)(y + (nucSize + ringInc * (i + 1)) * Math.sin(atom.getElecs()[i])), elecSize, paint);
 			paint.setColor(Color.rgb(50, 50, 50));
 			paint.setStyle(Paint.Style.STROKE);
 			canvas.drawCircle((float)(x + (nucSize + ringInc * (i + 1)) * Math.cos(atom.getElecs()[i])), (float)(y + (nucSize + ringInc * (i + 1)) * Math.sin(atom.getElecs()[i])), elecSize, paint);
 		}
 	}
 	
 	public void paint(Canvas canvas)
 	{
 		try
 		{
 			float elecSize = size / 32;
 			
 			size = this.getWidth() / 5;
 			yOffset = (int)((this.getHeight() / 2) - (size * 2.5));
 			
 			canvas.drawARGB(255, 48, 74, 97);
 			
 			paint.setTextAlign(Paint.Align.CENTER);
 			paint.setTypeface(Typeface.SANS_SERIF);
 			paint.setStrokeWidth(0);
 			paint.setTextSize((float)(size / 1.5));
 			paint.setColor(Color.RED);
 			canvas.drawText("Atoms!", this.getWidth() / 2, yOffset / 2, paint);
 			paint.setTextSize(size / 3);
 			paint.setColor(players[turn].getColor());
 			if(!gameOver)
 				canvas.drawText(players[turn].getName() + "'s turn", this.getWidth() / 2, this.getHeight() - (yOffset / 2), paint);
 			else
 				canvas.drawText(players[turn].getName() + " has won!", this.getWidth() / 2, this.getHeight() - (yOffset / 2), paint);
 			paint.setStrokeWidth((float)1.5);
 			
 			for(int i=0;i<5;i++)
 				for(int j=0;j<5;j++)
 					drawAtom(canvas, (float)((i + 0.5) * size), (float)(yOffset + (j + 0.5) * size), grid[i][j]);
 			
 			for(int i=0;i<strays.length;i++)
 			{
 				if(strays[i] != null)
 				{
 					Point[] elec = strays[i];
 					paint.setColor(players[turn].getColor());
 					paint.setStyle(Paint.Style.FILL);
 					canvas.drawCircle((float)elec[0].x, (float)elec[0].y, elecSize, paint);
 					paint.setColor(Color.rgb(50, 50, 50));
 					paint.setStyle(Paint.Style.STROKE);
 					canvas.drawCircle((float)elec[0].x, (float)elec[0].y, elecSize, paint);
 				}
 			}
 		}
 		catch(Exception e) {}
 	}
 }
