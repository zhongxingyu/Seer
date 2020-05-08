 package fr.eurecom.util;
 
 import android.content.Context;
 import android.graphics.Point;
 import android.util.Log;
 import android.view.Display;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.WindowManager;
 import android.widget.ImageView;
 
 public class CardView extends ImageView implements OnTouchListener{
 	
 	public static int height = 208;
 	public static int width = 150;
 	//TODO: fix height/width
 	private long lastDown;
 	private Context context;
 	private Point anchorPoint = new Point();
 	private CardPlayerHand playerHand;
 	private Point screenSize;
 	private Card card;
 	
 	public CardView(Context context) {
 		super(context);
 	}
 	
 	public CardView(Context context, Card card) {
 		super(context);
 		this.context = context;
 		this.card = card;
 		
 		this.setLayoutParams(new LayoutParams(width, height));
 		this.setOnTouchListener(this);
 		this.updateGraphics();
 		
 		//TODO: static ScreenSize in game?
 		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
 		Display display = wm.getDefaultDisplay();
 		screenSize = new Point();
 		display.getSize(screenSize);
 	}
 	
 	//TODO: when taking turned cards from table, the text is shown...
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		final int action = event.getAction();
 		
 		switch(action) {
 			case MotionEvent.ACTION_DOWN:
 				if(lastDown != 0 && (System.currentTimeMillis() - lastDown) <= 200) {
 					card.setTurned(card.getTurned() ? false : true);
 					updateGraphics();
 					playerHand.turnCard(this);
 				}
 				lastDown = System.currentTimeMillis();
 				
 				anchorPoint.x = (int) (event.getRawX() - v.getX());
 				anchorPoint.y = (int) (event.getRawY() - v.getY());
 				
 				v.setAlpha((float)0.5);
 				v.bringToFront();
 				playerHand.takeCard(this);
 				
 		    	return true;
 		    	
 			case MotionEvent.ACTION_MOVE:
 				
                 int x = (int) event.getRawX();
                 int y = (int) event.getRawY();
                 
                 float posX = x-anchorPoint.x;
                 float posY = y-anchorPoint.y;
                 
                 if(posX < 0 || (posX+width) > screenSize.x) {
                 	if(posY > 0 && (posY+height) < screenSize.y) {
                 		v.setY(y-(anchorPoint.y));
                 		playerHand.moveCard(this);
                 	}
                 } else if(posY < 0 || (posY+height) > screenSize.y) {
                 	if(posX > 0 && (posX+width) < screenSize.x) {
                 		v.setX(x-(anchorPoint.x));
                 		playerHand.moveCard(this);
                 	}
                 } else {
                 	v.setX(x-(anchorPoint.x));
                 	v.setY(y-(anchorPoint.y));
                 	playerHand.moveCard(this);
                 }
 				
                 return true;
 				
 			case MotionEvent.ACTION_UP:
 				
 				v.setAlpha(1);
 				playerHand.dropCard(this);
 				return true;
 
 			default:
 				Log.e("TOUCH", "OTHER TOUCH EVENT NO."+action);
 				return false;
 		}
 	}
 	
 	public void setOwner(CardPlayerHand owner) {
 		this.playerHand = owner;
 	}
 	
 	public Card getCard() {
 		return this.card;
 	}
 	
 	private String getResourceString() {
		return card.getTurned() ? "drawable/back_blue" : "drawable/cards"+card.getSuit()+card.getFace();
 		//TODO: May have to scale cards on big screens
 	}
 	
 	private static int getImageResource(Context context, String string) {
 		return context.getResources().getIdentifier(string, null, context.getPackageName());
 	}
 	
 	public void updateGraphics() {
 		this.setImageResource(getImageResource(context, getResourceString()));
 	}
 
 }
