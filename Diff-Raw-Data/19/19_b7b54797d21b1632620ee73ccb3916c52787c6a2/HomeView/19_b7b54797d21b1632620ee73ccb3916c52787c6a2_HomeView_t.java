 package com.example.views;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.example.phonepet.R;
 import com.example.utils.DatabaseHelper;
 import com.example.utils.Point;
 import com.example.vos.Ball;
 import com.example.vos.Food;
 import com.example.vos.Poop;
 
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.os.Environment;
 import android.text.format.Time;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 import android.widget.LinearLayout;
  
 public class HomeView extends View {
 
 	private Bitmap mBackground, mPet, mCloud, mPoop, mFood, mSponge, mTrash, mDirt, mBall, mBubbleLeft, mBubbleRight, mFrownyFace, mZZ;
 	private String fileName = "preferences", petName;
 	private Bitmap mDirt_1,mDirt_2,mDirt_3;
 	private SharedPreferences sharedPref;
 	private int backgroundWidth, backgroundHeight, petWidth, petHeight, cloud1X, cloud2X, nameX, petDirtAmt;
 	private float spongeX, spongeY;
 	private boolean poopExists, foodExists, trashcanNeeded, cleaningPet, ballExists;
 	private boolean showUnhappyThought=false, showHungryThought=false, showSleepingThought=false;
 	public boolean ballInPlay;
 	private Point petPoint = null;
 	private List<Poop> myList = null;
 	private Food currentFood = null;
 	private Paint paint;
 	private Ball currentBall = null;
 	
 	public HomeView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		
 		petPoint = new Point(0,0);
 		
 		// Get preferences file
 		sharedPref = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
 		
 		// Get constant values from preference file
 		backgroundWidth = sharedPref.getInt("backgroundWidth", 0);
 		backgroundHeight = sharedPref.getInt("backgroundHeight", 0);
 		
 		petWidth = sharedPref.getInt("petWidth", 0);
 		petHeight = sharedPref.getInt("petHeight", 0);
 		
 		// Set the cloud positions
 		cloud1X = backgroundWidth / 2;
 		cloud2X = 10;
 		poopExists = false;
 		foodExists = false;
 		ballExists = false;
		ballInPlay = false;
 		cleaningPet = false;
 		trashcanNeeded = false;
 		
 		// Load pet name and format it
 		paint = new Paint();
 		paint.setColor(Color.BLACK);
 		paint.setTextSize(20);
 		//Typeface tf = Typeface.create("Mistral", Typeface.BOLD);
 		//paint.setTypeface(tf);	
 		petName = sharedPref.getString("petName", " ");
 		nameX = getXCoordOfName(petName, paint);
 				
 		// Load bitmaps
 		loadBitmaps();
 		
 		// Start a thread that simulates cloud movement.
 		Thread moveThread = new MoveClouds();
 		moveThread.start();
 
 	}
 
 	@SuppressLint("DrawAllocation")
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		
 		// Set the layout parameters
 		this.setLayoutParams(new LinearLayout.LayoutParams(backgroundWidth, backgroundHeight));
 		
 		// Draw environment
 		canvas.drawBitmap(mBackground, 0, 0, null);
 
 		// Draw clouds.
 		canvas.drawBitmap(mCloud, cloud1X, 10, null);
 		canvas.drawBitmap(mCloud, cloud2X, 30, null);
 		
 		// Draw pet name
 		canvas.drawText(petName, nameX, backgroundHeight/2, paint);
 				
 		// Draw pet
 		canvas.drawBitmap(mPet, petPoint.x, petPoint.y, null);
 		
 		if(petDirtAmt == 1){
 			canvas.drawBitmap(mDirt_1, petPoint.x, petPoint.y, null);
 		}
 
 		if(petDirtAmt == 2){
 			canvas.drawBitmap(mDirt_2, petPoint.x, petPoint.y, null);
 		}
 
 		if(petDirtAmt == 3){
 			canvas.drawBitmap(mDirt_3, petPoint.x, petPoint.y, null);
 		}
 
 		//canvas.drawBitmap(mPoop,e.getX(), e.getY(), null);
 		if(poopExists) {
 			for(Poop e: myList) {
 				canvas.drawBitmap(mPoop, e.getX(), e.getY(), null);
 			}
 		}
 		
 		// Draw food on the screen if there is food.
 		if(foodExists)
 		{
 			canvas.drawBitmap(mFood, currentFood.getX(), currentFood.getY(), null);
 		}
 		
 		if(ballExists)
 		{
 			canvas.drawBitmap(mBall, currentBall.getX(), currentBall.getY(), null);
 		}
 		
 		if(cleaningPet){
 			//canvas.drawBitmap(R.drawable.sponge, 100.0, 100.0, null);
 			//Log.v("currently cleaning pet", Boolean.toString(cleaningPet));
 			canvas.drawBitmap(mSponge, spongeX, spongeY, null);
 		}
 		
 		if(trashcanNeeded) {
 			canvas.drawBitmap(mTrash, backgroundWidth/10, backgroundHeight/6 + backgroundHeight/3, null);
 		}
 		// Draw thought bubble if necessary
 				if (showUnhappyThought || showHungryThought || showSleepingThought) {
 					// Determine where thought bubble should be placed.
 					Point bubblePoint = getBubblePoint();
 					
 					// Draw bubble
 					if (bubblePoint.x < petPoint.x) {
 						// Using bubble_left
 						canvas.drawBitmap(mBubbleLeft, bubblePoint.x, bubblePoint.y, null);
 					}
 					else {
 						// Using bubble right
 						canvas.drawBitmap(mBubbleRight, bubblePoint.x, bubblePoint.y, null);
 					}
 					
 					
 					// Draw necessary thought in center of bubble
 					if (showUnhappyThought)
 						canvas.drawBitmap(mFrownyFace, bubblePoint.x + mBubbleLeft.getWidth()/2 - mFrownyFace.getWidth()/2,
 														bubblePoint.y + mBubbleLeft.getHeight()/2 - mFrownyFace.getHeight()/2,
 														null);
 					else if (showHungryThought)
 						canvas.drawBitmap(mFood, bubblePoint.x + mBubbleLeft.getWidth()/2 - mFood.getWidth()/2,
 								bubblePoint.y + mBubbleLeft.getHeight()/2 - mFood.getHeight()/2,
 								null);
 					else
 						canvas.drawBitmap(mZZ, bubblePoint.x + mBubbleLeft.getWidth()/2 - mZZ.getWidth()/2,
 								bubblePoint.y + mBubbleLeft.getHeight()/2 - mZZ.getHeight()/2,
 								null);
 				}
 	}
 	
 	private Point getBubblePoint() {
 		Point bubblePoint = new Point(0,0);
 		// Size of thought bubble is (bgWidth/8) x (bgHeight/8).
 		
 		if (petPoint.x < backgroundWidth/8) {
 			// Use bubble_right image. Put bubble point at top-right corner of pet.
 			bubblePoint.x = petPoint.x + petWidth;
 			bubblePoint.y = petPoint.y - backgroundHeight/8;
 		}
 		else {
 			// Use bubble_left image. Put bubble point at top-left corner of pet.
 			bubblePoint.x = petPoint.x - backgroundWidth/8;
 			bubblePoint.y = petPoint.y - backgroundHeight/8;
 		}
 		
 		return bubblePoint;
 	}
 	
 	/**
 	 * Handles cloud movement at a time interval.
 	 * If a cloud moves off screen it will eventually be redrawn on the opposite side.
 	 * 
 	 * @author Michael
 	 *
 	 */
 	private class MoveClouds extends Thread {
 		@Override
 		public void run() {
 			while (true) {
 				// Cloud 1 just went off screen
 				if (cloud1X > backgroundWidth + backgroundWidth/20)
 					// Reset the cloud's position to the left off-screen only if the other cloud is visible (x > 0).
 					// This prevents clouds from being drawn on top of each other which creates ugly bitmap problems.
 					if (cloud2X > 0)
 						// Place cloud somewhere randomly to the left offscreen.
 						cloud1X = -1 * (backgroundWidth/4 + (int)(Math.random() * ((backgroundWidth/1.5 - backgroundWidth/4) + 1)));
 
 				// Cloud 2 just went off screen
 				if (cloud2X > backgroundWidth - backgroundWidth/20)
 					if (cloud1X > 0)
 						cloud2X = -1 * (backgroundWidth/4 + (int)(Math.random() * ((backgroundWidth/1.5 - backgroundWidth/4) + 1)));
 				
 				// Move clouds 2px to the left. 
 				cloud1X += 2;
 				cloud2X += 2;
 				
 				// Sleep thread for 2 seconds.
 				try {
 					sleep(2000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	// Draw everything on the bitmap
 	public void drawPet(int x, int y) {
 		petPoint.x = x;
 		petPoint.y = y;
 		
 		// Calls onDraw; redraws the board and then deletes the old one.
 		this.invalidate();
 	}
 
 	public float petX(){
 		return petPoint.x;
 	}
 
 	public float petY(){
 		return petPoint.y;
 	}
 
 	// Update the pet on the bitmap if it's moved.
 	public void dragPet(int x, int y) {
 		petPoint.x = x;
 		petPoint.y = y;
 				
 		this.invalidate();
 	}
 	
 	public void drawSponge(float x, float y){
 		spongeX = x;
 		spongeY = y;
 
 		this.invalidate();
 	}
 
 	// Returns the x coordinate at which the pet's name should be placed on the screen.
 	// Props to whoever understands this math ;)
 	public int getXCoordOfName(String text, Paint paint) {
 		// First determine width of Painted name
 		Rect bounds = new Rect();
 		paint.getTextBounds(text, 0, text.length(), bounds);
 		int nameWidth = bounds.left + bounds.width();
 		
 		// Min x coord of pet sign placement is (38/65)bg width.
 		// Max x coord of pet sign placement is (53/65)bg width.
 		int minX = (int)(backgroundWidth * (38f/65f));
 		int maxX = (int)(backgroundWidth * (53f/65f));
 		int signWidth = maxX - minX;
 		
 		return (minX + ((signWidth - nameWidth))/2);
 	}
 	
 	// Load pet bitmap from file
 	public void loadBitmaps() {
 		// Get pet image from sd card.
 		String imageInSD = Environment.getExternalStorageDirectory() + "/PhonePet/petBitmap/pet";
 		mPet = BitmapFactory.decodeFile(imageInSD);
 
 		// Get poop image.
 		mPoop = BitmapFactory.decodeResource(getResources(), R.drawable.poop);
 		//get trashcan image
 		mTrash = BitmapFactory.decodeResource(getResources(), R.drawable.trashcan);
 		// Get food image.
 		mFood = BitmapFactory.decodeResource(getResources(), R.drawable.scooby_snack);
 
 		mBall = BitmapFactory.decodeResource(getResources(), R.drawable.tennisball2);
 
 		// Default mBoard to the background image.
 		mBackground = BitmapFactory.decodeResource(getResources(), R.drawable.templatebackground);
 				
 		// Default mCloud to cloud image
 		mCloud = BitmapFactory.decodeResource(getResources(), R.drawable.cloud);
 		mSponge = BitmapFactory.decodeResource(getResources(), R.drawable.sponge);
 		mDirt_1 = BitmapFactory.decodeResource(getResources(), R.drawable.dirt_1);
 		mDirt_2 = BitmapFactory.decodeResource(getResources(), R.drawable.dirt_2);
 		mDirt_3 = BitmapFactory.decodeResource(getResources(), R.drawable.dirt_3);
 		
 
 		// Get thought bubble images
 		mBubbleLeft = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_left);
 		mBubbleRight = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_right);
 		mFrownyFace = BitmapFactory.decodeResource(getResources(), R.drawable.frowny_face);
 		mZZ = BitmapFactory.decodeResource(getResources(), R.drawable.zz);
 		
 		// Scale the bitmaps
 		mBackground = Bitmap.createScaledBitmap(mBackground, backgroundWidth, backgroundHeight, true); // Environment
 		mCloud = Bitmap.createScaledBitmap(mCloud, backgroundWidth/4, backgroundHeight/10, true); // Cloud
 		mPet = Bitmap.createScaledBitmap(mPet, petWidth, petHeight, true); // Pet
 		mPoop = Bitmap.createScaledBitmap(mPoop, petWidth/3, petHeight/3, true);
 		mTrash = Bitmap.createScaledBitmap(mTrash, petWidth, petHeight, true);
 		mBubbleLeft = Bitmap.createScaledBitmap(mBubbleLeft, backgroundWidth/8, backgroundHeight/8, true);
 		mBubbleRight = Bitmap.createScaledBitmap(mBubbleRight, backgroundWidth/8, backgroundHeight/8, true);
 		mFrownyFace = Bitmap.createScaledBitmap(mFrownyFace, mBubbleLeft.getWidth()/2, mBubbleLeft.getHeight()/2, true);
 		mZZ = Bitmap.createScaledBitmap(mZZ, mBubbleLeft.getWidth()/2, mBubbleLeft.getHeight()/2, true);
 	}
 	
 
 	public void drawPoop(List<Poop> list) {
 		
 		myList = list;
 		for (Poop cn : myList) {
             String log = "Id: "+cn.getID()+" ,x: " + cn.getX() + " ,y: " + cn.getY();
         }	
 		        
 		poopExists = true;		
 		
 		this.invalidate();
 	}
 		
 	// Food functionality
 	public void drawFood(Food f)
 	{
 		// Only draw if the old food was eaten.
 		if(!foodExists)
 		{
 			currentFood = f;
 			foodExists = true;
 			this.invalidate();
 		}
 	}
 	
 	//Draws a ball
 	public void drawBall(Ball b)
 	{
 		Ball tempBall = b;
 		float x = (b.getX() - 237)/30, y = (b.getY() - 636)/30;
 		
 		if(ballInPlay)
 		{
 			currentBall = new Ball(237, 636);
 			ballExists = true;
 			
 			/*while(!currentBall.CloseEnoughTo(b))
 			{
 				currentBall.addPos(x,y);
 				this.invalidate();
 			}*/
 			
 			this.invalidate();
 		}
 		
 	}
 	
 	public void removeBall()
 	{
 		ballExists = false;
 		this.invalidate();
 	}
 
 	public void removeFood()
 	{	
 		foodExists = false;
 		this.invalidate();	
 	}
 	// End food functionality
 	
 
 
 	public int getBackgroundWidth() {
 		return backgroundWidth;
 	}
 	
 	public int getBackgroundHeight() {
 		return backgroundHeight;
 	}
 	
 	public int getPetWidth() {
 		return petWidth;
 	}
 	
 	public int getPetHeight() {
 		return petHeight;
 	}
 
 	public void dragPoop(List<Poop> list) {
 		
 		myList = list;
 		this.invalidate();
 		
 	}
 
 	public void setTrash(boolean need) {
 		trashcanNeeded = need;
 		this.invalidate();
 	}
 
 	public void setDirtAmt(int amt){
 		petDirtAmt = amt;
 	}
 	
 	public void cleaning(){
 		cleaningPet = true;
 		spongeX = (float) (backgroundWidth/2.0);
 		spongeY = (float) (backgroundHeight/2.0);			
 	}
 	public void notCleaning(){
 
 		cleaningPet = false;
 	}
 	
 	public void showThought(int which) {
 		if (which == 1) {
 			// Show sleeping thought
 			showUnhappyThought = false;
 			showHungryThought = false;
 			showSleepingThought = true;
 		}
 		else if (which == 2) {
 			// Show hungry thought
 			showUnhappyThought = false;
 			showHungryThought = true;
 			showSleepingThought = false;
 		}
 		else if (which == 3) {
 			// Show unhappy thought
 			showUnhappyThought = true;
 			showHungryThought = false;
 			showSleepingThought = false;
 			
 		}
 		else {
 			// Show no thought
 			showUnhappyThought = false;
 			showHungryThought = false;
 			showSleepingThought = false;
 		}
 	}
 
 }
