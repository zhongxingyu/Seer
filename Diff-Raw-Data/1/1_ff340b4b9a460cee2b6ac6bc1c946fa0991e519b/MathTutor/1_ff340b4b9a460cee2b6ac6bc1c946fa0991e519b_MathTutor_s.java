 package csci422.lwm.mathtutor;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class MathTutor extends Activity
 {
 	public static String DEBUG_TAG = "mathtutorTest";
 	
 	public static int NUM_BANANAS = 4;
 	private MathProblemGenerator problem;
 	private boolean firstRun;
 	private MathView mv;
 	private Paint debugPaint;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setDataMembers();
 		setContentView(mv);
 	}
 	
 	private void setDataMembers()
 	{
 		mv = new MathView(this);
 		problem = new MathProblemGenerator();
 		firstRun = true;
 		
 		debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		debugPaint.setARGB(255, 10, 133, 255);
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu)
 	{
 		if (problem.getDifficulty() == MathProblemGenerator.EASY)
 		{
 			menu.findItem(R.id.difficulty_easy).setEnabled(false);
 			menu.findItem(R.id.difficulty_medium).setEnabled(true);
 			menu.findItem(R.id.difficulty_hard).setEnabled(true);
 		}
 		else if (problem.getDifficulty() == MathProblemGenerator.MEDIUM)
 		{
 			menu.findItem(R.id.difficulty_easy).setEnabled(true);
 			menu.findItem(R.id.difficulty_medium).setEnabled(false);
 			menu.findItem(R.id.difficulty_hard).setEnabled(true);
 		}
 		else if (problem.getDifficulty() == MathProblemGenerator.HARD)
 		{
 			menu.findItem(R.id.difficulty_easy).setEnabled(true);
 			menu.findItem(R.id.difficulty_medium).setEnabled(true);
 			menu.findItem(R.id.difficulty_hard).setEnabled(false);
 		}
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		getMenuInflater().inflate(R.menu.difficulty, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) 
 	{
 		if (item.getItemId() == R.id.difficulty_easy) 
 		{
 			problem.setDifficulty(MathProblemGenerator.EASY);
 			firstRun = true;
 		} 
 		else if (item.getItemId() == R.id.difficulty_medium)
 		{
 			problem.setDifficulty(MathProblemGenerator.MEDIUM);
 			firstRun = true;
 		} 
 		else if (item.getItemId() == R.id.difficulty_hard)
 		{
 			problem.setDifficulty(MathProblemGenerator.HARD);
 			firstRun = true;
 		}
 		mv.setProblem();
 		mv.invalidate();
 		return true;
 	}
 
 	private class MathView extends View
 	{
 		private Banana[] bananas = new Banana[NUM_BANANAS];
 		private Paint problemTextPaint = new Paint();
 		private Paint bananaTextPaint = new Paint();
 		int[] xcoords = {150, 400, 150, 400};
 		int[] ycoords = {100, 100, 300, 300};
 		private boolean bananaSelected, ignoreTouches;
 		private Banana selectedBanana;
 		private Bitmap monkey, tree, result;
 		private int canvasWidth, canvasHeight, monkeyX, monkeyY, origBananaX, origBananaY, resultX, resultY;
 		private Rect scaledTree;
 
 		public MathView(Context context)
 		{
 			super(context);
 			setFocusable(true);
 			
 			setDataMembers();
 		}
 		
 		private void setDataMembers()
 		{
 			monkey = BitmapFactory.decodeResource(getResources(), R.drawable.monkey_question);
 			tree = BitmapFactory.decodeResource(getResources(), R.drawable.tree);
 			result = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder);
 			bananaSelected = false;
 			ignoreTouches = false;
 			scaledTree = new Rect();
 		}
 		
 		@Override
 		protected void onDraw(Canvas canvas)
 		{
 			if (firstRun)
 			{
 				setDrawingCoords(canvas);
 				setProblem();
 				problemTextPaint.setColor(Color.BLACK);
 				problemTextPaint.setTextSize((float) determineMaxSize(problem.getQuestion(), 
 						(canvas.getWidth() / 2)));
 				bananaTextPaint.setColor(Color.BLACK);
 				bananaTextPaint.setTextSize((float)120.0);
 			}
 		
 			canvas.drawBitmap(tree, null, scaledTree, null);
 			canvas.drawBitmap(monkey, monkeyX, monkeyY, null);
 			canvas.drawBitmap(result, resultX, resultY, null);
 			canvas.drawText(problem.getQuestion(),
 					canvasWidth / 2, 
 					canvasHeight / 3, 
 					problemTextPaint);	
 			
 			for (int i = 0; i < NUM_BANANAS; i++)
 			{
 				if (bananas[i].visible)
 				{
 					canvas.drawBitmap(bananas[i].icon, 
 							bananas[i].x - Banana.ICON_HALFWIDTH, 
 							bananas[i].y - Banana.ICON_HALFHEIGHT, 
 							null);
 					canvas.drawText(Integer.toString(bananas[i].getValue()), 
 							bananas[i].x - Banana.ICON_HALFWIDTH, 
 							bananas[i].y , 
 							bananaTextPaint);
 				}
 				//canvas.drawRect(bananas[i].bounds, debugRects);
 			}
 		}
 		
 		// Credit: http://stackoverflow.com/questions/12166476/android-canvas-drawtext-set-font-size-from-width
 		private int determineMaxSize(String str, float maxWidth)
 		{
 		    int size = 0;       
 		    Paint paint = new Paint();
 
 		    do
 		    {
 		    	paint.setTextSize(++size);
 		    } while (paint.measureText(str) < maxWidth);
 
 		    return size;
 		}
 		
 		private void setDrawingCoords(Canvas canvas)
 		{
 			firstRun = false;
 			
 			canvasWidth = canvas.getWidth();
 			canvasHeight = canvas.getHeight();
 			
 			resultX = canvasWidth / 2;
 			resultY = canvasHeight / 2;
 			
 			scaledTree.left = scaledTree.top = 0;
 			scaledTree.right = canvasWidth / 2;
 			scaledTree.bottom = canvasHeight;
 			
 			monkeyX = canvasWidth - monkey.getWidth();
 			monkeyY = canvasHeight - monkey.getHeight();
 
 			for (int i = 0; i < NUM_BANANAS; i++)
 			{
 				bananas[i] = new Banana(xcoords[i], ycoords[i]);
 			}		
 		}
 		
 		public void setProblem() 
 		{
 			problem.generateProblem();
 			ArrayList<Integer> bananaAnswers = problem.getAnswerChoices();
 			for (int i = 0; i < NUM_BANANAS; i++) {
 				bananas[i].setValue(bananaAnswers.get(i));
 			}
 		}
 		
 		private void happyMonkey()
 		{
 			monkey = BitmapFactory.decodeResource(getResources(), R.drawable.monkey_happy);
 			result = BitmapFactory.decodeResource(getResources(), R.drawable.check);
 			selectedBanana.setVisible(false);
 			Handler handler = new Handler();
 			handler.postDelayed(new Runnable()
 			{
 				public void run()
 				{
 					monkey = BitmapFactory.decodeResource(getResources(), R.drawable.monkey_question);
 					result = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder);
 					firstRun = true;
 					ignoreTouches = false;
 					invalidate();
 				}
 			}, 1000);
 		}
 		
 		private void sadMonkey()
 		{
 			monkey = BitmapFactory.decodeResource(getResources(), R.drawable.monkey_sad);
 			result = BitmapFactory.decodeResource(getResources(), R.drawable.ex);
 			selectedBanana.setVisible(false);
 			Handler handler = new Handler();
 			handler.postDelayed(new Runnable()
 			{
 				public void run()
 				{
 					monkey = BitmapFactory.decodeResource(getResources(), R.drawable.monkey_question);
 					result = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder);
 					ignoreTouches = false;
 					invalidate();
 				}
 			}, 1000);
 		}
 		
 		public boolean onTouchEvent(MotionEvent e)
 		{
 			if (ignoreTouches)
 			{
 				return true;
 			}
 			switch (e.getAction())
 			{
 				case MotionEvent.ACTION_DOWN:
 					Log.v("test", "(" + e.getX() + "," + e.getY() + ")");
 					for (int i = 0; i < NUM_BANANAS; i++)
 					{
 						if (bananas[i].visible && bananas[i].bounds.contains((int) e.getX(), (int) e.getY()))
 						{
 							bananaSelected = true;
 							selectedBanana = bananas[i];
 							selectedBanana.setSelected();
 							origBananaX = selectedBanana.x;
 							origBananaY = selectedBanana.y;
 						}
 					}
 					break;
 				case MotionEvent.ACTION_MOVE:
 					if (selectedBanana != null)
 					{
 						selectedBanana.updatePosition(e.getX(), e.getY());
 					}
 					break;
 				case MotionEvent.ACTION_UP:
 					if (bananaSelected)
 					{
 						Log.v("test", "(" + e.getX() + "," + e.getY() + ")");
 						if (e.getX() >= monkeyX && e.getY() >= monkeyY)
 						{
 							ignoreTouches = true;
 							if(selectedBanana.getValue() == problem.getAnswer())
 							{
 								happyMonkey();
 							}
 							else
 							{
 								sadMonkey();
 							}
 						}
 						else
 						{
 							selectedBanana.updatePosition(origBananaX, origBananaY);
 							selectedBanana.setUnselected();
 							bananaSelected = false;
 							selectedBanana = null;
 						}
 					}
 					break;
 			}
 			invalidate();
 			return true;
 		}
 	}
 	
 	private class Banana
 	{
 		private static final int ICON_WIDTH = 152;
 		private static final int ICON_HEIGHT = 195;
 		private static final int ICON_HALFWIDTH = 76;
 		private static final int ICON_HALFHEIGHT = 97;
 		private int value;
 		private Bitmap icon;
 		private int x, y;
 		private Rect bounds;
 		private boolean visible;
 		
 		public Banana(int x, int y)
 		{
 			icon = BitmapFactory.decodeResource(getResources(), R.drawable.banana);
 			this.x = x;
 			this.y = y;	
 			bounds = new Rect(x - ICON_HALFWIDTH, y - ICON_HALFHEIGHT, x + ICON_HALFWIDTH, y + ICON_HALFHEIGHT);
 			visible = true;
 		}
 		
 		private void updatePosition(float newX, float newY)
 		{
 			x = (int) newX;
 			y = (int) newY;
 			bounds.left = x - ICON_HALFWIDTH;
 			bounds.top = y - ICON_HALFHEIGHT;
 			bounds.right = x + ICON_HALFWIDTH;
 			bounds.bottom = y + ICON_HALFHEIGHT;
 		}
 		
 		private void setUnselected()
 		{
 			icon = BitmapFactory.decodeResource(getResources(), R.drawable.banana);
 		}
 		
 		private void setSelected()
 		{
 			icon = BitmapFactory.decodeResource(getResources(), R.drawable.banana_selected);
 		}
 		
 		public int getValue() 
 		{
 			return value;
 		}
 		
 		public void setValue(int newValue) 
 		{
 			value = newValue;
 		}
 		
 		public void setVisible(boolean newVisibility)
 		{
 			visible = newVisibility;
 		}
 		
 	}
 }
