 package com.september.tableroids.statics;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.res.AssetManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.util.DisplayMetrics;
 import android.view.MotionEvent;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 import com.september.tableroids.MainActivity;
 import com.september.tableroids.R;
 import com.september.tableroids.consts.Constants;
 import com.september.tableroids.model.Sprite;
 import com.september.tableroids.model.elements.Cloud;
 import com.september.tableroids.model.elements.Cloud.Direction;
 import com.september.tableroids.model.elements.ResponseCointaner;
 import com.september.tableroids.model.elements.SmileContainer;
 import com.september.tableroids.model.elements.Square;
 import com.september.tableroids.model.elements.Square.Fattore;
 import com.september.tableroids.model.elements.SquareResponse;
 import com.september.tableroids.utils.GraphicsUtils;
 import com.september.tableroids.utils.Updater;
 
 public class GameBuilder {
 
 	/**
 	 * @param args
 	 */
 	private static boolean ready = false;
 	private static Typeface typeFace;
 	public final static String tag = GameBuilder.class.getSimpleName();
 	private static Bitmap background;
 	private static Sprite backgroundSprite;
 	
 	private static Bitmap loaderBitmap;
 	private static Sprite loaderSprite;
 	
 	private static DisplayMetrics out;
 	
 	
 	public static boolean isReady() {
 		return ready;
 	}
 
 
 
 	public static void setReady(boolean ready) {
 		GameBuilder.ready = ready;
 	}
 	
 	
 	
 
 	public static DisplayMetrics getOut() {
 		if(out == null) {
 			out = new DisplayMetrics();
 		}
 		return out;
 	}
 
 
 
 	public static void setOut(DisplayMetrics out) {
 		GameBuilder.out = out;
 	}
 
 
 
 	public static Bitmap getLoaderBitmap() {
 		return loaderBitmap;
 	}
 
 
 
 	public static void setLoaderBitmap(Bitmap loaderBitmap) {
 		GameBuilder.loaderBitmap = loaderBitmap;
 	}
 
 
 
 	public static Sprite getLoaderSprite() {
 		return loaderSprite;
 	}
 
 
 
 	public static void setLoaderSprite(Sprite loaderSprite) {
 		GameBuilder.loaderSprite = loaderSprite;
 	}
 
 
 
 	public static Sprite getBackgroundSprite() {
 		return backgroundSprite;
 	}
 
 
 
 	public static void setBackgroundSprite(Sprite backgroundSprite) {
 		GameBuilder.backgroundSprite = backgroundSprite;
 	}
 
 
 
 	public static Bitmap getBackground() {
 		return background;
 	}
 
 
 
 	public static void setBackground(Bitmap background) {
 		GameBuilder.background = background;
 	}
 
 
 
 	protected static void threadedBuild(final Activity activity) {
 		
 		Scorer.reset();
 		Updater.getInstance().setSprites(null);
 		
 		//MOUNTAIN BACK
 		
 		int[] dim = GraphicsUtils.getScreenSize();
 		Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
 		GraphicsUtils.setBitmapResources(Constants.MOUNTAINS, getBackground());
 		Sprite mountain = getBackgroundSprite();
 		mountain.setY(dim[1]-mountain.getResizedHeight());
 		Updater.getInstance().getSprites().add(mountain);
 		
 		//SQUARE
 		
 		Bitmap bmp = Bitmap.createBitmap(GraphicsUtils.ONEPERCENTWIDTH*20, GraphicsUtils.ONEPERCENTWIDTH*20, conf);
 		GraphicsUtils.setBitmapResources(Constants.SQUARED_BITMAP, bmp);
 		Square square = new Square(bmp, (GraphicsUtils.ONEPERCENTWIDTH*30), (GraphicsUtils.ONEPERCENTHEIGHT*20), Constants.FPS, new int[]{1,1});
 		square.setFattore(Fattore.MOLTIPLICANDO);
 		Square square2 = new Square(bmp, (GraphicsUtils.ONEPERCENTWIDTH*70), (GraphicsUtils.ONEPERCENTHEIGHT*30), Constants.FPS, new int[]{1,1});
 		square2.setFattore(Fattore.MOLTIPLICATORE);
 		
 		//SMILECONTAINER
 		
 		Bitmap smileConatinerBmp = Bitmap.createBitmap(dim[0], GraphicsUtils.ONEPERCENTWIDTH*20, conf);
 		
 		Bitmap smileySprites = GraphicsUtils.getBitmapResources(Constants.SMILE_BITMAP,"gfx/smiley_sprites.png", activity);
 		Bitmap sadSprites = GraphicsUtils.getBitmapResources(Constants.SAD_BITMAP,"gfx/smiley_sad_sprites.png", activity);
 		
 		SmileContainer smileContainer = new SmileContainer(smileConatinerBmp, 0, 0, Constants.FPS, new int[]{1,1});
 		Updater.getInstance().getSprites().add(smileContainer);
 		//CLOUD
 		
 		Bitmap cloud = GraphicsUtils.getBitmapResources(Constants.CLOUD,"gfx/cloud.png", activity);
 		
 		Cloud cloud1 = new Cloud(cloud, -20, (GraphicsUtils.ONEPERCENTHEIGHT*30), 20, new int[]{1,1});
 		buildCloud(cloud1, dim[0]/5, Direction.RIGHT, 5, 10, dim, 30);
 	
 		Cloud cloud2 = new Cloud(cloud, dim[0], (GraphicsUtils.ONEPERCENTHEIGHT*32), 20, new int[]{1,1});
 		buildCloud(cloud2, dim[0]/7, Direction.LEFT, 7, 10, dim, 30);
 		
 		Cloud cloud3 = new Cloud(cloud, -20, (GraphicsUtils.ONEPERCENTHEIGHT*28), 20, new int[]{1,1});
 		buildCloud(cloud3, dim[0]/10, Direction.RIGHT, 7, 10, dim, 30);
 		
 		Updater.getInstance().getSprites().add(cloud1);
 		Updater.getInstance().getSprites().add(cloud2);
 		Updater.getInstance().getSprites().add(cloud3);
 		
 //		for(int x = 0; x< 20; x++) {
 //			
 //			int size = dim[0]/20;
 //			Random r = new Random();
 //			 int rx = r.nextInt(5);
 //			 int ry = r.nextInt(3);
 //			
 //			Smile smile = new Smile(smileySprites,x*size,0,1,new int[]{5,3},new int[]{rx,ry});
 //			smile.setScaleWidth(size);
 //			Updater.getInstance().getSprites().add(smile);
 //		}
 //		
 //		for(int x = 0; x< 20; x++) {
 //			
 //			int size = dim[0]/20;
 //			Random r = new Random();
 //			 int rx = r.nextInt(5);
 //			 int ry = r.nextInt(3);
 //			
 //			Smile smile = new Smile(sadSprites,x*size,size,1,new int[]{5,3},new int[]{rx,ry});
 //			smile.setScaleWidth(size);
 //			Updater.getInstance().getSprites().add(smile);
 //		}
 
 		
 		Updater.getInstance().getSprites().add(square);
 		Updater.getInstance().getSprites().add(square2);
 		
 		
 		//RESPONSE
 		
 		Bitmap resBmp = Bitmap.createBitmap(GraphicsUtils.getScreenSize()[0], GraphicsUtils.ONEPERCENTWIDTH*15, conf);
 		
 //		for(int x = 0; x< resBmp.getWidth(); x++) {
 //	    	for(int y = 0; y < resBmp.getHeight(); y++) {
 //	    		if(x%2==0) {
 //	    			resBmp.setPixel(x, y, Color.BLUE);
 //	    		}
 //	    	}
 //	    }
 		
 		ResponseCointaner resContainer = new ResponseCointaner(resBmp, 0, GraphicsUtils.ONEPERCENTHEIGHT*50, Constants.FPS, new int[]{1,1});
 		Bitmap response = Bitmap.createBitmap(GraphicsUtils.ONEPERCENTWIDTH*15, GraphicsUtils.ONEPERCENTWIDTH*15, conf);
 		int fraction = GraphicsUtils.getScreenSize()[0] /6;
 		int correctResponse = Scorer.getR().nextInt(3);
 		
 		SquareResponse response1 = new SquareResponse(response, fraction, 10, Constants.FPS, new int[]{1,1});
 		response1.changeColor();
 		response1.setyRange(15);
 		response1.setValue(correctResponse == 0 ? Scorer.getMoltiplicando() * Scorer.getMoltiplicatore() : Scorer.getR().nextInt(99)+1);
 		SquareResponse response2 = new SquareResponse(response,fraction * 3,18, Constants.FPS, new int[]{1,1});
 		response2.changeColor();
 		response2.setyRange(15);
 		response2.setValue(correctResponse == 1 ? Scorer.getMoltiplicando() * Scorer.getMoltiplicatore() : Scorer.getR().nextInt(99)+1);
 		SquareResponse response3 = new SquareResponse(response,fraction * 5, 15, Constants.FPS, new int[]{1,1});
 		response3.changeColor();
 		response3.setyRange(15);
 		response3.setValue(correctResponse == 2 ? Scorer.getMoltiplicando() * Scorer.getMoltiplicatore() : Scorer.getR().nextInt(99)+1);
 		
 		resContainer.appendChildren(response1);
 		resContainer.appendChildren(response2);
 		resContainer.appendChildren(response3);
 		
 		Updater.getInstance().getSprites().add(resContainer);
 
 //		TextView tv = (TextView) activity.findViewById(R.id.textAdView);
 //		tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 20));
 //		tv.setText("FAKE ADMOB");
 //		tv.setBackgroundColor(Color.MAGENTA);
 //		tv.setTextColor(Color.WHITE);
 		
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				while(!Scorer.isReadyToPlay()) {
 					try {
 						Thread.sleep(200);
 					}
 					catch(Exception e) {
 						android.util.Log.e(GameBuilder.class.getSimpleName(), e.getMessage());
 					}
 				};
 				
 				activity.runOnUiThread(new Runnable() {
 
 					@Override
 					public void run() {
 						
 						
 						((MainActivity)activity).setAdView(new AdView(activity, AdSize.BANNER, "a151dd328171ec6"));
 						LinearLayout layout = (LinearLayout)activity.findViewById(R.id.adViewHolder);
 						layout.addView(((MainActivity)activity).getAdView());
 						((MainActivity)activity).getAdView().loadAd(new AdRequest());
 						
 						//activity.findViewById(R.id.gamellayout).postInvalidate();
 					}
 					
 				});
 				
 			}
 		}).start();
 		
 	}
 	
 	private static void buildCloud(Cloud cloudSprite,int scaleWidth, Direction direction, int speed, int speedRange, int[] sourceDim, int yRange) {
 		cloudSprite.setScaleWidth(scaleWidth);
 		cloudSprite.setDirection(direction);
 		cloudSprite.setSpeed(speed);
 		cloudSprite.setSpeedRange(speedRange);
 		cloudSprite.setSourcedim(sourceDim);
 		cloudSprite.setyRange(yRange);
 	}
 	
 	
 
 	public static Typeface getTypeFace() {
 		return typeFace;
 	}
 
 
 
 	public static void setTypeFace(Typeface typeFace) {
 		GameBuilder.typeFace = typeFace;
 	}
 
 
 
 	public static void build(final Activity activity) {
 		
 		setTypeFace(Typeface.createFromAsset(activity.getAssets(),"fonts/WalterTurncoat.ttf"));
 		AssetManager manager = activity.getAssets();
 		
 		activity.getWindowManager().getDefaultDisplay().getMetrics(getOut());
 		int w = out.widthPixels;
 		int h = out.heightPixels;
 		
 		try {
 			setBackground(BitmapFactory.decodeStream (manager.open("gfx/mountain.png")));
 			
 			
 			
 			Sprite backGround = new Sprite(getBackground(), 0, 0, 1, new int[]{1,1}) {
 				@Override
 				public void onTouch(MotionEvent event) {
 				}
 				@Override
 				public void onCollide() {
 				}
 				@Override
 				protected void doUpdate() {
 				}
 			};
 			
 			backGround.setScaleWidth(w);
 			setBackgroundSprite(backGround);
 			
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			android.util.Log.e(GameBuilder.tag, e1.getMessage());
 		}
 		
 		try {
 			setLoaderBitmap(BitmapFactory.decodeStream (manager.open("gfx/loadSprite.png")));
 			
 			activity.getWindowManager().getDefaultDisplay().getMetrics(getOut());
 			
 			Sprite loaderSprite = new Sprite(getLoaderBitmap(), w/3, h/2, Constants.FPS50, new int[]{1,6}) {
 				@Override
 				public void onTouch(MotionEvent event) {
 				}
 				@Override
 				public void onCollide() {
 				}
 				@Override
 				protected void doUpdate() {
 				}
 			};
 			
 			loaderSprite.setScaleWidth(w/3);
 			setLoaderSprite(loaderSprite);
 			
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			android.util.Log.e(GameBuilder.tag, e1.getMessage());
 		}
 		
 		
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				while(!GameBuilder.isReady()) {
 					try {
 						Thread.sleep(200);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						android.util.Log.e(GameBuilder.tag, e.getMessage());
 					}
 				}
				GameBuilder.setReady(false);
				GameBuilder.threadedBuild(activity);
				GameBuilder.setReady(true);
 			}
 		}).start();
 		
 		
 		
 	}
 
 
 }
