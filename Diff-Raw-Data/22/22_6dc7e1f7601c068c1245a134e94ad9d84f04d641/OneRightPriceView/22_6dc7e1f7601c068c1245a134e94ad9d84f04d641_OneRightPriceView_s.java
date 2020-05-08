 package edu.upenn.cis350;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Random;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.Activity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.view.Display;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 
 
 /**
  * Class to implement the view of the One Right Price game
  * @author Ashley Baldwin
  * @version 1.0
  */
 public class OneRightPriceView extends View {
 
 
 
 	public ArrayList<FoodItem> correctOrder;
 	public ArrayList<FoodItem> displayOrder;
 	
 	private int dispHeight;
 	private int dispWidth;
 	
 	public int numAttempts;
 	public int tries;
 	public ScreenSquare calorieNumberSquare;
 	
 	public ScreenSquare food0Square;
 	public ScreenSquare food1Square;
 	
 	public boolean food0Occupied;
 	public boolean food1Occupied;
 
 	
 	public ScreenSquare start0Square;
 	public ScreenSquare start1Square;
 	
 	public ScreenSquare submitSquare;
 	public ScreenSquare quitSquare;
 	
 	public ScreenSquare touchedSquare;
 	
 	public FoodItem food0;
 	public FoodItem food1;
 	public int calorieNumber;
 	
 	private Context mContext;
 	private String username;
 	private int score;
 
 	public OneRightPriceView(Context c, AttributeSet a) {
 		super(c, a);
 		setUpFoodItems();
 		setUpDisplayItems();
 		this.mContext = c;
 		username = ((OneRightPriceActivity)c).getUsername();
 
 		if(!IOBasic.getShownHelp(username, IOBasic.OneRightPrice)) {
 			showDialog(START_DIALOG);
 			IOBasic.setShownHelp(username, IOBasic.OneRightPrice);
 		}
 		
 		score = IOBasic.getPoints(username);
 		
 	}
 	public OneRightPriceView(Context c) {
 		super(c);
 		setUpFoodItems();
 		setUpDisplayItems();
 		this.mContext = c;
 		username = ((OneRightPriceActivity)c).getUsername();
 		
 		if(!IOBasic.getShownHelp(username, IOBasic.OneRightPrice)) {
 			showDialog(START_DIALOG);
 			IOBasic.setShownHelp(username, IOBasic.OneRightPrice);
 		}
 		
 		score = IOBasic.getPoints(username);
 
 	}
 	
 	/**
 	 * Performs a basic setup, aquiring two different foods
 	 * and randomly assigning one as the calorie guess. 
 	 */
 	
 	private void setUpFoodItems() {
 		Random number = new Random();
 		FoodGenerator foods = new FoodGenerator(this.getResources());
 		correctOrder = new ArrayList<FoodItem>();
 		displayOrder = new ArrayList<FoodItem>();
 		food0 = foods.nextFood();
 		displayOrder.add(food0);
 		food1 = foods.nextFood();
 		displayOrder.add(food1);
 		if(number.nextInt() % 2 ==0)
 			calorieNumber = food1.getCalorieCount();
 		
 		else
 			calorieNumber = food0.getCalorieCount();
 		Collections.sort(correctOrder);
 		numAttempts = 0;
 	}
 	
 	/**
 	 * Sets up the initial state of all static and dynamic display items
 	 */
 	
 	private void setUpDisplayItems() {
 		WindowManager wm = (WindowManager)this.getContext().getSystemService(Context.WINDOW_SERVICE);
 		Display display = wm.getDefaultDisplay();
 		this.dispHeight = display.getHeight();
 		this.dispWidth = display.getWidth();
 		
 		// Use these rectangles to automatically resize source image to our screen
 		Rect food0Loc = new Rect(2*dispWidth/26, dispHeight/26, (2*dispWidth + 8*dispHeight)/26, 6*dispHeight/26);
 		Rect food1Loc = new Rect(2*dispWidth/26, 7*dispHeight/26, (2*dispWidth + 8*dispHeight)/26, 12*dispHeight/26);
 		
 		calorieNumberSquare = new ScreenSquare(1*dispWidth/26, 20*dispHeight/26, 6*dispWidth/26, 3*dispWidth/26, Color.GREEN, Integer.toString(calorieNumber));
 		start0Square = new ScreenSquare(calorieNumberSquare, Color.GRAY, this.getContext().getResources().getString(R.string.rankOtherButton));
 		start1Square = new ScreenSquare(calorieNumberSquare, Color.GRAY, this.getContext().getResources().getString(R.string.rankOtherButton));
 		
 		food0Square = new ScreenSquare(19*dispWidth/26, 3*dispHeight/26, 6*dispWidth/26, 3*dispWidth/26, Color.GRAY, this.getContext().getResources().getString(R.string.rankOtherButton));
 		food1Square = new ScreenSquare(19*dispWidth/26, 9*dispHeight/26, 6*dispWidth/26, 3*dispWidth/26, Color.GRAY, this.getContext().getResources().getString(R.string.rankOtherButton));
 		
 		submitSquare = new ScreenSquare(16*dispWidth/26, 20*dispHeight/26, 9*dispWidth/26, 3*dispWidth/26, Color.RED, this.getContext().getResources().getString(R.string.rankSubmitButton), Color.WHITE);
 		quitSquare = new ScreenSquare(dispWidth - dispWidth/13, 0, dispWidth/13, dispWidth/13, Color.RED, this.getContext().getResources().getString(R.string.rankXButton), Color.BLACK);
 		
 		checkOccupancy();
 		
 		displayOrder.get(0).getImage().setBounds(food0Loc);
 		displayOrder.get(1).getImage().setBounds(food1Loc);
 	}
 	
 	protected void onDraw(Canvas canvas) {
 		Paint paint = new Paint();
 		displayOrder.get(0).getImage().draw(canvas);
 		displayOrder.get(1).getImage().draw(canvas);
 		paint.setTextSize(dispHeight/40);
 		paint.setColor(Color.WHITE);
 		canvas.drawText(displayOrder.get(0).getName(), 2*dispWidth/26, 27*dispHeight/104, paint);
 		canvas.drawText(displayOrder.get(1).getName(), 2*dispWidth/26, 51*dispHeight/104, paint);
 		quitSquare.draw(canvas, paint);
 		submitSquare.setColor(food0Occupied || food1Occupied ? Color.GREEN : Color.RED);
 		submitSquare.draw(canvas, paint);
 		start0Square.draw(canvas, paint);
 		start1Square.draw(canvas, paint);
 		food0Square.draw(canvas, paint);
 		food1Square.draw(canvas, paint);
 		calorieNumberSquare.draw(canvas, paint);
 		// Draw touchedSquare last so that it appears on top of everything else
 		if(touchedSquare != null) {
 			touchedSquare.draw(canvas, paint);
 		}
 		
 	}
 	
 	/**
 	 * Method that generates the desired motion event and sends it to onTouchEvent.  This method
 	 * serves no purpose outside of a tester, as it will not be invoked by the system.
 	 * 
 	 * This method is apparently necessary because the testing application runs in a different
 	 * thread than this View class, and therefore cannot call onTouchEvent directly.
 	 * 
 	 * @param X X-coordinate of the event
 	 * @param Y Y-coordinate of the event
 	 * @param action Action of the event
 	 * @return the result of calling onTouchEvent with this action
 	 */
 	public boolean fakeTouchEvent(int action, int X, int Y) {
 		MotionEvent e = MotionEvent.obtain(0, 0, action, X, Y, 0 );
 		return onTouchEvent(e);
 	}
 	
 	/**
 	 * Handle user input events.
 	 * - On ACTION_DOWN, pick up a square if one was touched.
 	 * - On ACTION_UP, drop (snapping to another square if needed) the picked up square
 	 * - On ACTION_MOVE, center the currently picked up square at the touched location
 	 */
 	public boolean onTouchEvent(MotionEvent event) {
 		if(event.getAction() == MotionEvent.ACTION_DOWN) {
 			int touchX = (int)event.getX();
 			int touchY = (int)event.getY();
 			
 			if(submitSquare.containsPoint(touchX, touchY) && (food0Occupied || food1Occupied)) {
 				numAttempts++;
 				if(checkCorrect()) {
 					showDialog(CORRECT_DIALOG);
 				} else {
 					showDialog(WRONG_DIALOG);
 				}
 			}
 			
 			if(quitSquare.containsPoint(touchX, touchY)) {
 				showDialog(QUIT_DIALOG);
 			}
 
 			checkOccupancy();
 			
 			touchedSquare = calorieNumberSquare.containsPoint(touchX, touchY) ? calorieNumberSquare : null;
 			// If no square is touched, ignore event
 			if(touchedSquare == null) {
 				return false;
 			}
 			touchedSquare.centerAt(touchX, touchY);
 			unmarkOverlapping(touchedSquare);
 			invalidate();
 			return true;
 		}
 		else if(event.getAction() == MotionEvent.ACTION_MOVE) {
 			// If no square was touched on the down action, ignore event
 			if(touchedSquare != null) {
 				touchedSquare.centerAt((int)event.getX(), (int)event.getY());
 				invalidate();
 				return true;
 			}
 			return false;
 		}
 		else if(event.getAction() == MotionEvent.ACTION_UP) {
 			// If no square was touched on the down action, ignore event
 			if(touchedSquare != null) {
 				// If we are currently overlapping a "destination" square, reset "starting" position
 				ScreenSquare overlapping = 	touchedSquare.overlapping(food0Square) && !food0Occupied ? food0Square :
 										  	touchedSquare.overlapping(food1Square) && !food1Occupied ? food1Square :
 											null;
 				
 				if(overlapping == null) {
 					touchedSquare.resetPosition();
 				} 
 				else {
 					touchedSquare.moveTo(overlapping);
 					checkOccupancy();
 					//touchedSquare.setStartPosition();
 				}
 				touchedSquare = null;
 				invalidate();
 				return true;
 			}
 			return false;
 		}
 		return false;
 	}
 	
 	/**
 	 * Updates all occupancy variables depending on the position of the movable squares
 	 */
 	public void checkOccupancy() {
 		food0Occupied = false;
 		food1Occupied = false;
 		markOverlapping(calorieNumberSquare);
 	}
 	
 	/**
 	 * Sets the occupancy variable for the position of the square that was just released
 	 * @param square The square that was just released
 	 */
 	private void markOverlapping(ScreenSquare square) {
 		if(square.overlapping(food0Square)) food0Occupied = true;
 		if(square.overlapping(food1Square)) food1Occupied = true;
 	}
 	
 
 	private void unmarkOverlapping(ScreenSquare square) {
 		if(square.overlapping(food0Square)) food0Occupied = false;
 		if(square.overlapping(food1Square)) food1Occupied = false;
 	}
 
 
 	public boolean checkCorrect() {
 		if(food0Occupied && food0.getCalorieCount() == calorieNumber){
			if(tries == 0){
				score++;
 				IOBasic.setPoints(username, score);
 		        IOBasic.setGameWins(username, IOBasic.OneRightPrice,
 		        		IOBasic.getGameWins(username, IOBasic.OneRightPrice) + 1);
			}
 
 	        IOBasic.setGameAttempts(username, IOBasic.OneRightPrice,
 	        		IOBasic.getGameAttempts(username, IOBasic.OneRightPrice) + 1);
 	        
 			return true;
 		}
 		else if(food1Occupied && food1.getCalorieCount() == calorieNumber){
 			if(tries == 0){
 				score++;
 				IOBasic.setPoints(username, score);
 		        IOBasic.setGameWins(username, IOBasic.OneRightPrice,
 		        		IOBasic.getGameWins(username, IOBasic.OneRightPrice) + 1);
 			}
 			
 	        IOBasic.setGameAttempts(username, IOBasic.OneRightPrice,
 	        		IOBasic.getGameAttempts(username, IOBasic.OneRightPrice) + 1);
 			
 			return true;
 		}
 		else{
 			tries = 1;
 			return false;
 		}
 		
 	}
 	
 	private static final int START_DIALOG = 1;
 	private static final int START2_DIALOG = 2;
 	private static final int CORRECT_DIALOG = 3;
 	private static final int WRONG_DIALOG = 4;
 	private static final int QUIT_DIALOG = 5;
 	
 	/**
 	 * Creates a Dialog of the specified ID and shows it
 	 * @param id ID number of the dialog to create
 	 */
 	public void showDialog(int id) {
 		createDialog(id).show();
 	}
 
 	/**
 	 * Creates an instance of the desired Dialog
 	 * @param id ID number of the dialog to create
 	 * @return a Dialog instance ready to be displayed
 	 */
 	public Dialog createDialog(int id) {
     	if(id == START_DIALOG) {
     		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
     		builder.setMessage(getResources().getString(R.string.oneWelcomeMessage));
     		builder.setPositiveButton(R.string.rankNextButton,
     				new DialogInterface.OnClickListener() {
     			public void onClick(DialogInterface dialog, int id) {
     				dialog.cancel();
     				showDialog(START2_DIALOG);
     			}
     		});
     		return builder.create();
     	}
     	else if(id == START2_DIALOG) {
     		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
     		builder.setMessage(getResources().getString(R.string.oneWelcomeMessage2));
     		builder.setPositiveButton(R.string.rankStartButton,
     				new DialogInterface.OnClickListener() {
     			public void onClick(DialogInterface dialog, int id) {
     				dialog.cancel();
     			}
     		});
     		return builder.create();
     	}
     	else if(id == CORRECT_DIALOG) {
     		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
     		String returnMessage = getResources().getString(R.string.rankCorrectMessage);
    		if(numAttempts != 1) {
     			returnMessage = returnMessage + getResources().getString(R.string.rankCorrectMessage2)
    										+ Integer.toString(numAttempts)
    										+ getResources().getString(R.string.rankCorrectTries);
    		}
     		builder.setMessage(returnMessage);
     		builder.setPositiveButton(R.string.rankExitButton,
     				new DialogInterface.OnClickListener() {
     			public void onClick(DialogInterface dialog, int id) {
     				dialog.dismiss();
     				((Activity)mContext).finish();
     			}
     		});
     		return builder.create();
     	}
     	else if(id == WRONG_DIALOG) {
     		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
     		builder.setMessage(getResources().getString(R.string.rankWrongMessage));
     		builder.setPositiveButton(R.string.rankRetryButton,
     				new DialogInterface.OnClickListener() {
     			public void onClick(DialogInterface dialog, int id) {
     				dialog.cancel();
     			}
     		});
     		return builder.create();
     	}
     	else if(id == QUIT_DIALOG) {
     		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
     		builder.setMessage(getResources().getString(R.string.rankQuitMessage));
     		builder.setNegativeButton(R.string.rankNoButton,
     				new DialogInterface.OnClickListener() {
     			public void onClick(DialogInterface dialog, int id) {
     				dialog.cancel();
     			}
     		});
     		builder.setPositiveButton(R.string.rankYesButton,
     				new DialogInterface.OnClickListener() {
     			public void onClick(DialogInterface dialog, int id) {
     				dialog.dismiss();
     				((Activity)mContext).finish();
     			}
     		});
     		return builder.create();
     	}
     	return null;
     }
 	
 
 }
