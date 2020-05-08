 package com.tw.techradar.activity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Picture;
 import android.graphics.drawable.PictureDrawable;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.FloatMath;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.TableLayout;
 import com.tw.techradar.R;
 import com.tw.techradar.controller.RadarController;
 import com.tw.techradar.model.Radar;
 import com.tw.techradar.model.RadarArc;
 import com.tw.techradar.model.RadarItem;
 import com.tw.techradar.ui.model.Blip;
 
 import java.util.ArrayList;
 import java.util.List;
 
 //TODO: Need to cleanup code
 
 
 public class CurrentRadar extends Activity {
 
 	private int marginX;
     private int marginY;
 
     private List<Blip> blips = null;
     private int screenOriginX;
     private int screenOriginY;
     private int maxRadius;
     private DisplayMetrics displayMetrics;
     private int currentQuadrant;
     private Radar radarData;
     private View mainView;
     private TableLayout mainLayout;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_current_radar);
         currentQuadrant = 0;
         radarData = getRadarData();
         mainView = findViewById(R.id.currentRadarLayout);
         mainLayout = (TableLayout) findViewById(R.id.currentRadarLayout);
     }
 
     @Override
     protected void onStart() {
         super.onStart();    //To change body of overridden methods use File | Settings | File Templates.
         drawRadar();
     }
 
     private void drawRadar() {
         determineBoundsForView(mainView);
         determineScreenDimensions();
         determineOrigins(currentQuadrant);
         int screenWidth = displayMetrics.widthPixels;
         int screenHeight = displayMetrics.heightPixels;
 
         float multiplier = (float) maxRadius /getRadiusOfOutermostArc(radarData.getRadarArcs());
         this.blips = getBlipsForRadarData(multiplier, radarData);
         // Add the radar to the RadarRL
         Picture picture = new Picture();
         Canvas canvas = picture.beginRecording(displayMetrics.widthPixels, displayMetrics.heightPixels);
         // Draw on the canvas
 
         Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
         paint.setColor(0xFF000000);
         paint.setStyle(Style.STROKE);
         paint.setStrokeWidth((float) 0.8);
         int centerX = displayMetrics.widthPixels - Math.abs(screenOriginX);
         int centerY = displayMetrics.heightPixels - Math.abs(screenOriginY);
 
         drawRadarQuadrants(screenWidth, screenHeight, centerX, centerY, canvas,
                 paint);
         drawRadarCircles(Math.abs(screenOriginX), Math.abs(screenOriginY), multiplier, canvas, paint, radarData.getRadarArcs());
         drawRadarBlips(canvas);
 
         picture.endRecording();
         PictureDrawable drawable = new PictureDrawable(picture);
         mainLayout.setBackgroundDrawable(drawable);
     }
 
     private void determineScreenDimensions() {
         displayMetrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayMetrics.heightPixels = displayMetrics.heightPixels - marginY;
        displayMetrics.widthPixels = displayMetrics.widthPixels - marginX;
     }
 
     private float getRadiusOfOutermostArc(List<RadarArc> radarArcs) {
         float maxRadius = 0.0f;
         for (RadarArc arc : radarArcs) {
              if (arc.getRadius()> maxRadius){
                  maxRadius = arc.getRadius();
              }
         }
         return maxRadius;
     }
 
     private Radar getRadarData() {
         Radar radarData = null;
         try {
             return new RadarController(getAssets()).getRadarData();
         } catch (Exception e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return radarData;
     }
 
     private List<Blip> getBlipsForRadarData(float multiplier,Radar radarData){
         List<Blip> blips = new ArrayList<Blip>(radarData.getItems().size());
         for (RadarItem radarItem : radarData.getItems()) {
             float xCoordinate = getXCoordinate(radarItem.getRadius() * multiplier, radarItem.getTheta());
             float yCoordinate = getYCoordinate(radarItem.getRadius() * multiplier, radarItem.getTheta());
             Blip blip = Blip.getBlipForRadarItem(radarItem, xCoordinate, yCoordinate);
             blips.add(blip);
         }
 
         return blips;
     }
 
     private void drawRadarBlips(Canvas canvas) {
         for (Blip blip : blips) {
             blip.render(canvas);
         }
     }
 
     private void determineBoundsForView(View mainView) {
 		int bounds[] = new int[2];
 		mainView.getLocationOnScreen(bounds);
 		this.marginX = bounds[0];
 		this.marginY = bounds[1];
 		System.out.println(String.format("MarginX %d MarginY %d", this.marginX, this.marginY));
 		
 	}
 
 
     private float getXCoordinate(float radius, float theta) {
 		
 		float xCoord =  radius*FloatMath.cos((float)Math.toRadians(theta));
 		System.out.println(FloatMath.cos(60));
 		System.out.println(String.format("Converted radius %f and theta %f to %f",radius,theta,xCoord));
 		return translateXCoordinate(xCoord);
 	}
 	
 	private float translateXCoordinate(float xCoord){
 		float transaltedXCoord = xCoord - screenOriginX;
 		return transaltedXCoord;
 	}
 
 
 	private float getYCoordinate(float radius, float theta) {
 		float yCoord = radius*FloatMath.sin((float)Math.toRadians(theta));
 		return translateYCoordinate(yCoord);
 	}
 	
 	private float translateYCoordinate(float yCoord){
 		float transaltedYCoord = yCoord - screenOriginY;
 		return -transaltedYCoord;
 	}
 
     //TODO: Spiked code.. Need to clean up and better encapsulate stuff
     private void determineOrigins(int quadrant){
         switch (quadrant)
         {
             case 1:
                 screenOriginY = displayMetrics.heightPixels ;
                 screenOriginX =  - marginX;
                 maxRadius = displayMetrics.widthPixels - 10;
                 break;
 
             case 2:
                 screenOriginY = displayMetrics.heightPixels ;
                 screenOriginX =  - displayMetrics.widthPixels;
                 maxRadius = displayMetrics.widthPixels - 10;
                 break;
 
             case 3:
                 screenOriginY = 0 ;
                 screenOriginX =  - displayMetrics.widthPixels;
                 maxRadius = displayMetrics.widthPixels - 10;
 
                 break;
 
             case 4:
                 screenOriginY = 0 ;
                 screenOriginX = 0 ;
                 maxRadius = displayMetrics.widthPixels - 10;
 
                 break;
 
             default:
                 screenOriginY = displayMetrics.heightPixels/2;
                 screenOriginX = -displayMetrics.widthPixels/2;
                 maxRadius = ( displayMetrics.widthPixels  /2) - 10;
 
         }   }
 
 
 	private void drawRadarQuadrants(int screenWidth, int screenHeight,
 			int centerX, int centerY, Canvas canvas, Paint paint) {
 		canvas.drawLine((float)0, (float)centerY, (float)screenWidth, (float)centerY, paint);
         canvas.drawLine((float)centerX, (float)0, (float)centerX, (float)screenHeight, paint);
 	}
 
 	private void drawRadarCircles(int centerX, int centerY, float multiplier,
 			Canvas canvas,Paint circlePaint, List<RadarArc> radarArcs) {
         for (RadarArc radarArc : radarArcs) {
             canvas.drawCircle((float) centerX, (float) centerY, (multiplier*radarArc.getRadius()), circlePaint);
         }
 	}
 	
 
     @Override
 	public boolean onTouchEvent(MotionEvent event) {
         if (event.getAction() == MotionEvent.ACTION_DOWN){
             System.out.println("MarginY :" + marginY);
             float correctedX = event.getX() - marginX;
             float correctedY = event.getY() - marginY;
             System.out.println("X:" + correctedX + "  Y:" + correctedY);
             Blip blip = doesLieInABlip(event.getX(), event.getY());
             if (blip != null) {
                 System.out.println("Click lies on a " + blip.getClass() + " Blip");
                 displayItemInfo(blip);
             } else {
                 System.out.println("Click does not lie on a Blip");
                 determineAndChangeQuadrant(correctedX, correctedY);
             }
         }
     	return super.onTouchEvent(event);
 	}
 
     private void determineAndChangeQuadrant(float x, float y) {
         if (currentQuadrant!=0)
             currentQuadrant = 0;
         else{
             currentQuadrant = determineQuadrantClicked(x,y);
         }
         drawRadar();
     }
 
     private int determineQuadrantClicked(float x, float y) {
 
         int midpointX = displayMetrics.widthPixels / 2;
         int midpointY = displayMetrics.heightPixels / 2;
 
         int quadrant = 0;
 
         if (x>=midpointX){
             if (y<=midpointY){
                 quadrant = 1;
             }
             else
                 quadrant = 4;
         }else{
             if (y<=midpointY){
                 quadrant = 2;
             }
             else
                 quadrant = 3;
 
         }
         return quadrant;
 
     }
 
     private void displayItemInfo(Blip blip) {
         Intent intent = new Intent(this, ItemInfoActivity.class);
         intent.putExtra(RadarItem.ITEM_KEY, blip.getRadarItem());
         startActivity(intent);
     }
 
     public Blip doesLieInABlip(float clickX, float clickY){
         View mainView = findViewById(R.id.currentRadarLayout);
         determineBoundsForView(mainView);
         for (Blip blip : blips) {
             if (blip.isPointInBlip(clickX,clickY - marginY))
                 return blip;
         }
         return null;
     }
 
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_current_radar, menu);
         return true;
     }
 
     public void showQuadrant(View view){
         Intent intent = new Intent(this, QuadrantActivity.class);
         startActivity(intent);
 //        AlertDialog.Builder builder;
 //        builder = new AlertDialog.Builder(this);
 //        AlertDialog dialog;
 //        builder.setMessage("Hello World kaise ho").setTitle("Hello World");
 //        dialog = builder.create();
 //        dialog.show();
     }
 }
