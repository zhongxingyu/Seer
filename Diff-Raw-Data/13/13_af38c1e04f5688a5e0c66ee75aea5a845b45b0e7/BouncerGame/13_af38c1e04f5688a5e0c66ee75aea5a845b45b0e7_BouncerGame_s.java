 package bouncer.logic;
 
 
 
 
 
 import java.util.Date;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 import bouncer.sprite.PlatformsDestroyAnimation;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 
 public class BouncerGame extends ArcadeGame {
 
     Random randomGenerator = new Random();
     Random randomGenerator2 = new Random();
 	public static String TAG = BouncerGame.class.getCanonicalName();
 	private Context mContext;
 	private boolean platformHit=false;
 	private int indexPlatformHit[]; 
 	Date base;
 	
 	// For text
 	private Paint mTextPaint = new Paint();
 
 	// For Bitmaps
 	private Paint mBitmapPaint = new Paint();
 	
 	// Game name
 	public static final String NAME = "SpaceBlaster";
 	
 	// Refresh rate (ms)
 	private static final long UPDATE_DELAY = 20; 
 
 	Bitmap[] destroyPlatfromImages;
 	
 	int playerX;
 	int playerY;
 	
 	int ballX;
 	int ballY;
 	/**
 	 * Constructor
 	 * 
 	 * @param context
 	 */
 	public BouncerGame(Context context) {
 		super(context);
 		mContext = context;
 		super.setUpdatePeriod(UPDATE_DELAY);
 	}
 
 	public BouncerGame(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		mContext = context;
 		super.setUpdatePeriod(UPDATE_DELAY);
 	}
 	
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		int tx = (int)event.getX();
 		int ty = (int)event.getY();
 //		System.out.println("tx=" + tx 
 //				+ " ty=" + ty + " ship x=" + x + " y=" + y 
 //				+ " ship wh=" + ship.getWidth() + " " + ship.getHeight()
 //				+ " edt=" + event.getDownTime() +  " ep=" + event.getPressure());
 
 		
 		// Has the ship been touched. if so move it
 		
 		// else start game if not already started
 		
 		if ( tx > playerX + player.getWidth())
 			playerX+=10;
 		else if( tx < playerX)
 			playerX-=10;
 			
 			
 		
 		return true;
 	}
 	
 	
 	
 	
 	/**
 	 * Main Draw Sub
 	 */
 	@Override
 	protected void onDraw(Canvas canvas) 
 	{
 		super.dispatchDraw(canvas);
 		paint(canvas);
 	}
 	
 	boolean ingame = true;
 	public void paint(Canvas g) {
 		if (ingame)
 			playGame(g);
 	//	else
 		//	showIntroScreen(g);
 	}
 	
 	Bitmap[] platforms;
 	int[] nrOfHits;
 	//x,y,direction
 	int[] initPoint;
 	int[] direction;
 	
 	public void playGame(Canvas c) {
 		
 		
 		drawPlayerPlatform(c);
 		c.drawBitmap(platforms[0], initPoint[0], initPoint[1], mBitmapPaint);
 		drawPlatforms(c);
 	    movePlatforms();
 	    drawBall(c);
 	    moveBall();
 	    if(platformHit){
 	    	
 	    	drawPlatformDestroy(c);
 	    }
 	    
 	   // List<int[]> toStr = Arrays.asList(nrOfHits);
 	   // System.out.println(nrOfHits[0] +"  " + nrOfHits[1]  );
 	   
 	}
 	
 	public static int nrOfPlatforms = 8;
 	public void initPlatforms(){
 		int width = getWidth();
 		
 		platforms=new Bitmap[nrOfPlatforms];
 		initPoint=new int[nrOfPlatforms*2];
 		direction=new int[nrOfPlatforms];
 		nrOfHits=new int[nrOfPlatforms];
 		//5 platforms
 		for(int i = 0; i < nrOfPlatforms ; i++){
 		platforms[i] = getImage(R.drawable.platformbluelarger);
 		}
 		
 		for(int i = 0; i < nrOfPlatforms ; i++){
 			nrOfHits[i]=0;
 		}
 		
 		for(int i = 0; i < nrOfPlatforms*2 ; i+=2){
 		
 		//initPoint[i]=0;
 		initPoint[i] = width/2 + i*platforms[0].getWidth()/4;
 		//initPoint[i+1]=0;
 		initPoint[i+1] = 10 + i * platforms[0].getHeight();
 	
 		
 		}
 		for(int i = 0; i < nrOfPlatforms ; i++){
 			direction[i]=i+1;
 		}
 		
 		
 		
 		
 	}
 	
 	public void drawBall(Canvas c){
 		c.drawBitmap(ball, ballX, ballY, mBitmapPaint);
 	}
 	
 
 	
 	int ballSpeed = 8;
 	int ballDirection = -1;
 	
 	int ballSpeedX = 5;
 	int ballDirectionX = 0;
 	
 	boolean first = true;
 	boolean second = false;
 	public void moveBall(){
 		//collision detection
 		int width = getWidth();
 		//1.with platforms
 		
 		
 		for(int i = 0; i < nrOfPlatforms*2; i+=2){
 			
 			
 			if( (ballX > initPoint[i]) && (ballX < initPoint[i] + platforms[0].getWidth())
				&& (ballY <= initPoint[i+1] + platforms[0].getHeight())){
 				if(ballDirection==-1){
 					
 					platformHit=true;
 					indexPlatformHit[i/2]=i;
 					System.out.println("i/2 = " + i/2 + " nrOfHits =  " + nrOfHits[i/2] + " index = " + indexPlatformHit[i/2]);
 					
 					
 					
 //					float timeSince = 0;
 //					if(first){
 //						base=new Date();
 //						first=false;
 //						second=true;
 //						}else if(second){
 //							float before = base.getTime();
 //							Date afterBase=new Date();
 //							float after = afterBase.getTime();
 //							 timeSince = after - before; 
 //						System.out.println(" ->> "  + timeSince);
 //						first=true;
 //						second=false;
 //						}
 //					
 //					if(timeSince > 0.1)
 					nrOfHits[i/2]+=1;
 					
 						
 				}
 				//platformDestroy(i);
 				
 			  	ballDirection=1; 
 			}
 			
 					
 		}
 		
 		for(int i = 0; i < nrOfPlatforms*2; i+=2){
 			
 			
 			//if( (ballX > initPoint[i]) && (ballX < initPoint[i] + platforms[0].getWidth())
 			//	&& (ballY < initPoint[i+1] )){
 				
 			if( (ballX > initPoint[i]) && (ballX < initPoint[i] + platforms[0].getWidth())
 					&& ( initPoint[i+1] >= ballY  ? initPoint[i+1] - ballY <= platforms[0].getHeight() : false )){
 				Log.d(TAG, ballY + " <= " + initPoint[i+1]);
 				//if(initPoint[i]-ballY == 1){ 
 			  	   // Log.d(TAG, "in == 1"); 
 				if(ballDirection==1){
 					platformHit=true;
 					indexPlatformHit[i/2]=i;
 //					float timeSince = 0;
 //					if(first){
 //						base=new Date();
 //						first=false;
 //						second=true;
 //						}else if(second){
 //							float before = base.getTime();
 //							Date afterBase=new Date();
 //							float after = afterBase.getTime();
 //							 timeSince = after - before; 
 //						System.out.println(" ->> "  + timeSince);
 //						first=true;
 //						second=false;
 //						}
 //					
 //					if(timeSince > 0.1)
 					nrOfHits[i/2]+=1;
 					
 				}
 					ballDirection=-1;
 				//}
 			}
 			
 					
 		}
 		
 		
 		
 		//2.with player
 		  if( (ballX > playerX) && (ballX < playerX + player.getWidth())
 				  && (ballY >= playerY - player.getHeight())){
 			  ballCollisionWithPlayer();
 			  ballDirection=-1;
 		  }
 		//3.with up-wall
 		  if(ballY <= 0 + ball.getHeight())
 			  ballDirection=1;
 		  
 		  //4.with left and right wall
 		  if(ballX <= 0)
 			  ballDirectionX=1;
 		  if(ballX >= width-ball.getWidth() )
 			  ballDirectionX=-1;
 		  
 		  int height=getHeight();
 		  //4.with down-wall
 			  if(ballY > height)
 				  gameOver();
 		
 		
 			ballY+=ballSpeed*ballDirection;
 			ballX+=ballSpeedX*ballDirectionX;
 		
 	}
 	
 	Bitmap platformsSprite;
 	private void drawPlatformDestroy( Canvas c) {
 		//perform some animation 
 	//	PlatformsDestroyAnimation pda = new PlatformsDestroyAnimation(platformsSprite,
 	//			initPoint[indexPlatformHit], initPoint[indexPlatformHit+1], platforms[0].getWidth(), platforms[0].getHeight()
 	//			, 4, 5); 
 		
 	//	pda.draw(c);
 		//pda.update(20);
 		
 		for(int j = 0; j < nrOfPlatforms; j++){
 			
 			for(int i = 0; i < nrOfPlatforms; i++){
 			if(nrOfHits[i]==1)
 		     c.drawBitmap(destroyPlatfromImages[0], initPoint[indexPlatformHit[i]],initPoint[indexPlatformHit[i]+1],null);
 			else if(nrOfHits[i]==2)
 		     c.drawBitmap(destroyPlatfromImages[1], initPoint[indexPlatformHit[i]],initPoint[indexPlatformHit[i]+1],null);
 			else if(nrOfHits[i]==3)
 			     c.drawBitmap(destroyPlatfromImages[2], initPoint[indexPlatformHit[i]],initPoint[indexPlatformHit[i]+1],null);
 			else if(nrOfHits[i]==4)
 			     c.drawBitmap(destroyPlatfromImages[3], initPoint[indexPlatformHit[i]],initPoint[indexPlatformHit[i]+1],null);
 			else if(nrOfHits[i]==5)
 			     c.drawBitmap(destroyPlatfromImages[4], initPoint[indexPlatformHit[i]],initPoint[indexPlatformHit[i]+1],null);
//	else if(nrOfHits[i]==6)
		//	initPoint[indexPlatformHit[i]] = -100;
	//		    initPoint[indexPlatformHit[i]+1] = -100;
 			}
 		
 		}
 		//put it outside window
 		//initPoint[indexPlatformHit] = -100;
 		//initPoint[indexPlatformHit+1] = -100;
 		//platformHit=false;
 	}
 	
 /*	private void update(){
 		PlatformsDestroyAnimation pda = new PlatformsDestroyAnimation(platformsSprite,
 				initPoint[indexPlatformHit], initPoint[indexPlatformHit+1], platforms[0].getWidth(), platforms[0].getHeight()
 				, 4, 5); 
 		pda.update(UPDATE_DELAY);
 	}
 	*/
 
 	int divideFragments = 5;
 	int[] xydivFr;
 
 	public void ballCollisionWithPlayer(){
 	
 		
 		
 		//int platformWidth = playerX + player.getWidth();
 		int interval = player.getWidth()/divideFragments;
 		
 		int tempPlayerX=playerX;
 		for(int i = 0; i < divideFragments*2; i+=2){
 			xydivFr[i] = tempPlayerX;
 			xydivFr[i+1] = tempPlayerX+interval;
 			tempPlayerX = tempPlayerX+interval;
 		}
 		Log.d(TAG,""+ xydivFr);
 		
 		//conditions depends on where ball touch player platform
 		if( (ballX > xydivFr[0]) && (ballX < xydivFr[1]) )
 			ballDirectionX=-2;
 		else if( (ballX > xydivFr[2]) && (ballX < xydivFr[3]) )
 			ballDirectionX=-1;
 		else if( (ballX > xydivFr[4]) && (ballX < xydivFr[5]) )
 			ballDirectionX=0;
 		else if( (ballX > xydivFr[6]) && (ballX < xydivFr[7]) )
 			ballDirectionX=1;
 		else if( (ballX > xydivFr[8]) && (ballX < xydivFr[9]) )
 			ballDirectionX=2;
 				
 		
 		
 	}
 	
 	
 	
 	
 	
 	
 	
 	public void drawPlatforms(Canvas c){
 		for(int i = 0; i < nrOfPlatforms ; i++){
 	    int index=i*2;
 		c.drawBitmap(platforms[i], initPoint[index], initPoint[index+1], mBitmapPaint);
 		}
 	}
 
 	//int direction = 1;
 	public void movePlatforms(){
 		
 		int width = getWidth();
 		/*int i = initPoint[0];
 		initPoint[0] = i + direction[0];
 		
 		if(initPoint[0] >= width - platforms[0].getWidth())
 			direction[0] = -1;
 		if(initPoint[0] <= 0)
 			direction[0] = 1;*/
 		
 		for(int index = 0; index < nrOfPlatforms ; index++){
 			int iMul= index * 2;
 			int i = initPoint[iMul];
 			initPoint[iMul] = i + direction[index];
 			
 			if(initPoint[iMul] >= width - platforms[index].getWidth())
 				direction[index] = -1*direction[index];
 			if(initPoint[iMul] <= 0)
 				direction[index] = 1*-(direction[index]);
 		
 		}
 		
 	}
 	
 	@Override
 	protected void updatePhysics() {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	Bitmap player;
 	Bitmap ball;
 	@Override
 	protected void initialize() {
 		Log.d(TAG,"initalize");
 		int n;
 	
 			
 		
 		// Screen size
 		int width = getWidth();
 		int height = getHeight();
 		
 		initPlatforms();
 		
 		mTextPaint.setARGB(255, 255, 255, 255);
 		
 		player = getImage(R.drawable.player_larger);
 		playerX = width/2;
 		playerY = height - player.getHeight();
 		
 		ball = getImage(R.drawable.ball);
 		ballX = playerX + player.getWidth()/2;
 		ballY = playerY - ball.getHeight();
 		xydivFr=new int[divideFragments*2];
 		
 		 //platformsSprite=getImage(R.drawable.sprite_platforms);
 		
 		destroyPlatfromImages=new Bitmap[5];
 		destroyPlatfromImages[0]=getImage(R.drawable.platform_01);
 		destroyPlatfromImages[1]=getImage(R.drawable.platform_02);
 		destroyPlatfromImages[2]=getImage(R.drawable.platform_03);
 		destroyPlatfromImages[3]=getImage(R.drawable.platform_04);
 		destroyPlatfromImages[4]=getImage(R.drawable.platform_05);
 		
 		
 		indexPlatformHit=new int[nrOfPlatforms];
 	}
 	
 	
 	public void drawPlayerPlatform(Canvas c){
 		int height = getHeight();
 		c.drawBitmap(player, playerX, playerY, mBitmapPaint );
 	}
 
 	@Override
 	protected boolean gameOver() {
 		//here should be something logic, now just restart
 		ballDirection=-1;
 		return false;
 	}
 
 	@Override
 	protected long getScore() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 }
