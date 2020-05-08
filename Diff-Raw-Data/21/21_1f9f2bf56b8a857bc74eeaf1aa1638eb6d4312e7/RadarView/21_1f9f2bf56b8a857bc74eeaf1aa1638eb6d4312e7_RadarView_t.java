 package com.tw.techradar.support;
 
 import android.app.Activity;
 import android.graphics.*;
 import android.graphics.drawable.PictureDrawable;
 import android.graphics.drawable.shapes.PathShape;
 import android.graphics.drawable.shapes.Shape;
 import android.util.DisplayMetrics;
 import android.util.FloatMath;
 import android.view.View;
 import com.tw.techradar.model.Radar;
 import com.tw.techradar.model.RadarArc;
 import com.tw.techradar.model.RadarItem;
 import com.tw.techradar.ui.model.Blip;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class RadarView {
 
     private int currentQuadrant;
     private Radar radarData;
     private View mainView;
     private DisplayMetrics displayMetrics;
     private Activity parentContext;
     private int marginX;
     private int marginY;
     private int screenOriginY;
     private int screenOriginX;
     private int maxRadius;
     private List<Blip> blips;
     private TolerantTouchDetector tolerantTouchDetector;
     private RadarArc radarArcFilter;
     private CharSequence searchText;
     private float multiplier;
     private boolean initializedFlag = false;
 
     public RadarView(int currentQuadrant, Radar radarData, View mainView,Activity parentContext) {
         this.currentQuadrant = currentQuadrant;
         this.radarData = radarData;
         this.mainView = mainView;
         this.parentContext = parentContext;
     }
 
     public void drawRadar() {
         fixAllQuadrantOverlaps();
         determineBoundsAndDimensions();
         initializeDataForCurrentQuadrant();
         tolerantTouchDetector = new TolerantTouchDetector(displayMetrics.xdpi, this.blips);
         // Add the radar to the RadarRL
         Picture picture = new Picture();
         Canvas canvas = picture.beginRecording(displayMetrics.widthPixels, displayMetrics.heightPixels);
         // Draw on the canvas
 
         int screenWidth = displayMetrics.widthPixels;
         int screenHeight = displayMetrics.heightPixels;
 
         Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
         paint.setColor(Color.WHITE);
         paint.setStyle(Paint.Style.STROKE);
         paint.setStrokeWidth(1);
         int centerX = displayMetrics.widthPixels - Math.abs(screenOriginX);
         int centerY = displayMetrics.heightPixels - Math.abs(screenOriginY);
 
         drawBackground(canvas);
         drawRadarQuadrants(screenWidth, screenHeight, centerX, centerY, canvas,
                 paint);
         drawRadarCircles(Math.abs(screenOriginX), Math.abs(screenOriginY),canvas, paint);
         drawRadarBlips(canvas);
 
         picture.endRecording();
         PictureDrawable drawable = new PictureDrawable(picture);
         mainView.setBackgroundDrawable(drawable);
     }
 
     private void initializeDataForCurrentQuadrant() {
         multiplier = (float) maxRadius / getRadiusOfOutermostArc(radarData.getRadarArcs());
         this.blips = getBlipsForRadarData();
     }
 
     private void determineBoundsAndDimensions() {
         determineBoundsForView(mainView);
         determineScreenDimensions();
         determineOrigins(currentQuadrant);
     }
 
 
     /**
      * Adjusts for collisions - lots of issues here
      * @param blips
      */
     private void adjustForCollisions(List<Blip> blips) {
         if (currentQuadrant==0) return;
         int retries = 0;
         int retryLimit = 50;
         int collisionCount = 0;
         boolean collisionFlag = false;
         do{
         retries++;
         collisionFlag = false;
         for (Blip referenceBlip : blips) {
             if (!isRadarItemRenderable(referenceBlip.getRadarItem())) continue;
             for (Blip blip : blips) {
                 if ((blip == referenceBlip) || (!isRadarItemRenderable(blip.getRadarItem()))) continue;
                 boolean isCollisionPresent = checkAndResolveCollision(referenceBlip, blip);
                 collisionFlag = collisionFlag | isCollisionPresent;
                 if (isCollisionPresent)
                     collisionCount++;
 //                if (collisionFlag) break;
             }
 //            if (collisionFlag) break;
         }
         }while(collisionFlag && (retries < retryLimit));
         System.out.println("Collision resolution retry count for Quadrant[" + currentQuadrant + "]:" + retries);
         System.out.println("Collisions detected for Quadrant[" + currentQuadrant + "]:" + collisionCount);
     }
 
     private boolean checkAndResolveCollision(Blip referenceBlip, Blip blip) {
         boolean collisionFlag=false;
         while (chkIfCollision(referenceBlip.getDimensionsWithText(), blip.getDimensionsWithText())) {
             System.out.println("Collision between " + referenceBlip.getRadarItem().getName() + " and " + blip.getRadarItem().getName());
 
             int blipThetaAdj;
             if (blip.isFrozen()) {
                 //There may be a collision, but we can't do anything immediately
                 return false;
             }
 
            blipThetaAdj = (referenceBlip.getRadarItem().getTheta() > blip.getRadarItem().getTheta()) ? -1 : 1;
             adjustTheta(blip, blipThetaAdj);
 
             if (!isBlipWithinQuadrantBoundariesForZoomedView(blip)){
                 System.out.println("Collision - boundary condition reached for blip:"  + blip.getRadarItem().getName() + ".Reverting theta and ignoring collision for now");
                adjustTheta(blip, - blipThetaAdj);
                 blip.freeze();
                 return false;
             }
 
             collisionFlag = true;
         }
         return collisionFlag;
     }
 
     /**
      * Fixes quadrant overlaps for all quadrants
      * Need to cleanup
      */
 
     public void fixAllQuadrantOverlaps(){
         if (initializedFlag){
             return;
         }
         for (int quadrantToFix = 1; quadrantToFix <= 4 ; quadrantToFix ++){
             this.currentQuadrant = quadrantToFix;
             determineBoundsAndDimensions();
             initializeDataForCurrentQuadrant();
             fixQuadrantOverlapsForCurrentQuadrant();
         }
         this.currentQuadrant = 0;
         initializedFlag = true;
     }
 
     /**
      * Fixes quadrant overlaps for current quadrant
      */
     private void fixQuadrantOverlapsForCurrentQuadrant(){
         if ((currentQuadrant == 0) || (initializedFlag)) return;
         for (Blip blip : blips) {
             if (blip.getRadarItem().getQuadrant() != currentQuadrant) continue;
             int adj = fixQuadrantOverlapIfPresentForBlip(blip);
             if (adj != 0){
                 System.out.println("Collision - Quadrant[" + blip.getRadarItem().getQuadrant() + "] Item - " + blip.getRadarItem().getName() + " ThetaAdj[" + adj + "] deg");
                 blip.freeze();
             }
         }
     }
 
     private int fixQuadrantOverlapIfPresentForBlip(Blip blip) {
         int thetaAdj = 0;
         int totalAdjustment = 0;
         while (!isBlipWithinQuadrantBoundariesForZoomedView(blip)) {
             if (((blip.getRadarItem().getQuadrant() == 1) && (blip.getDimensionsWithText().bottom > displayMetrics.heightPixels)) ||
                     ((blip.getRadarItem().getQuadrant() == 2) && (blip.getDimensionsWithText().right > displayMetrics.widthPixels)) ||
                     ((blip.getRadarItem().getQuadrant() == 3) && (blip.getDimensionsWithText().top < 0)) ||
                     ((blip.getRadarItem().getQuadrant() == 4) && (blip.getDimensionsWithText().left < 0)))
 
             {
                 thetaAdj = 1;
             } else if (((blip.getRadarItem().getQuadrant() == 1) && (blip.getDimensionsWithText().left < 0)) ||
                     ((blip.getRadarItem().getQuadrant() == 2) && (blip.getDimensionsWithText().bottom > displayMetrics.heightPixels)) ||
                     ((blip.getRadarItem().getQuadrant() == 3) && (blip.getDimensionsWithText().right > displayMetrics.widthPixels)) ||
                     ((blip.getRadarItem().getQuadrant() == 4) && (blip.getDimensionsWithText().top < 0))) {
                 thetaAdj = -1;
             }
             totalAdjustment = totalAdjustment + thetaAdj;
             blip.getRadarItem().setTheta(blip.getRadarItem().getTheta() + thetaAdj);
             adjustCoOrdsForTheta(blip);
         }
         return totalAdjustment;
     }
 
     private void adjustTheta(Blip blip, int adjTheta) {
         RadarItem radarItem = blip.getRadarItem();
         radarItem.setTheta(radarItem.getTheta() + adjTheta);
         adjustCoOrdsForTheta(blip);
     }
 
     private void adjustCoOrdsForTheta(Blip blip) {
         RadarItem radarItem = blip.getRadarItem();
         float newXCoordinate = getXCoordinate(radarItem);
         float newYCoordinate = getYCoordinate(radarItem);
         blip.shiftCoOrdinates(newXCoordinate, newYCoordinate);
     }
 
     private boolean isBlipWithinQuadrantBoundariesForZoomedView(Blip blip) {
         Rect dimensions = blip.getDimensionsWithText();
         int maxX = displayMetrics.widthPixels;
         int maxY = displayMetrics.heightPixels;
 
         return (dimensions.right <= maxX) && (dimensions.bottom <= maxY) && (dimensions.left >= 0) && (dimensions.top >= 0);
     }
 
     private boolean chkIfCollision(Rect rectA, Rect rectB){
         return rectA.intersect(rectB);
     }
 
     public void switchQuadrant(int quadrant) {
         this.currentQuadrant = quadrant;
         drawRadar();
     }
 
     public void filterByRadarArc(RadarArc radarArc){
         this.radarArcFilter = radarArc;
         drawRadar();
     }
 
     public int getQuadrantClicked(float x, float y) {
 
         x = x - marginX;
         y = y - marginY;
 
         int midpointX = displayMetrics.widthPixels / 2;
         int midpointY = displayMetrics.heightPixels / 2;
 
         int quadrant = 0;
 
         if (x >= midpointX) {
             if (y <= midpointY) {
                 quadrant = 1;
             } else
                 quadrant = 4;
         } else {
             if (y <= midpointY) {
                 quadrant = 2;
             } else
                 quadrant = 3;
 
         }
         return quadrant;
 
     }
 
     public Blip getBlipClicked(float clickX, float clickY) {
         float correctedX = clickX - marginX;
         float correctedY = clickY - marginY;
 
         return tolerantTouchDetector.getClosestBlipForTouchEvent(correctedX,correctedY);
     }
 
     public int getCurrentQuadrant() {
         return currentQuadrant;
     }
 
     private void determineScreenDimensions() {
         displayMetrics = new DisplayMetrics();
         parentContext.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
         displayMetrics.heightPixels = displayMetrics.heightPixels - marginY;
         displayMetrics.widthPixels = displayMetrics.widthPixels - marginX;
     }
 
     private void determineBoundsForView(View mainView) {
         int bounds[] = new int[2];
         mainView.getLocationOnScreen(bounds);
         this.marginX = bounds[0];
         this.marginY = bounds[1];
         System.out.println(String.format("MarginX %d MarginY %d", this.marginX, this.marginY));
     }
 
     private float getXCoordinate(RadarItem radarItem) {
 
         float xCoord = radarItem.getRadius() * multiplier * FloatMath.cos((float) Math.toRadians(radarItem.getTheta()));
         return translateXCoordinate(xCoord);
     }
 
     private float translateXCoordinate(float xCoord) {
         float transaltedXCoord = xCoord - screenOriginX;
         return transaltedXCoord;
     }
 
 
     private float getYCoordinate(RadarItem radarItem) {
         float yCoord = radarItem.getRadius() * multiplier * FloatMath.sin((float) Math.toRadians(radarItem.getTheta()));
         return translateYCoordinate(yCoord);
     }
 
     private float translateYCoordinate(float yCoord) {
         float transaltedYCoord = yCoord - screenOriginY;
         return -transaltedYCoord;
     }
 
     //TODO: Spiked code.. Need to clean up and better encapsulate stuff
     private void determineOrigins(int quadrant) {
         switch (quadrant) {
             case 1:
                 screenOriginY = displayMetrics.heightPixels;
                 screenOriginX = 0;
                 maxRadius = displayMetrics.widthPixels - 10;
                 break;
 
             case 2:
                 screenOriginY = displayMetrics.heightPixels;
                 screenOriginX = -displayMetrics.widthPixels;
                 maxRadius = displayMetrics.widthPixels - 10;
                 break;
 
             case 3:
                 screenOriginY = 0;
                 screenOriginX = -displayMetrics.widthPixels;
                 maxRadius = displayMetrics.widthPixels - 10;
 
                 break;
 
             case 4:
                 screenOriginY = 0;
                 screenOriginX = 0;
                 maxRadius = displayMetrics.widthPixels - 10;
 
                 break;
 
             default:
                 screenOriginY = displayMetrics.heightPixels / 2;
                 screenOriginX = -displayMetrics.widthPixels / 2;
                 maxRadius = (displayMetrics.widthPixels / 2) - 10;
 
         }
     }
 
 
     private void drawRadarQuadrants(int screenWidth, int screenHeight,
                                     int centerX, int centerY, Canvas canvas, Paint paint) {
         canvas.drawLine((float) 0, (float) centerY, (float) screenWidth, (float) centerY, paint);
         canvas.drawLine((float) centerX, (float) 0, (float) centerX, (float) screenHeight, paint);
         drawQuadrantNames(screenHeight, screenWidth, canvas);
     }
 
     private void drawQuadrantNames(int screenHeight, int screenWidth, Canvas canvas) {
         Paint.Align textAlign = Paint.Align.LEFT;
         // Quadrant Mappings 1=2 2=1 3=3 4=4
         int quadrantTextSize = 20;
         int zoomedQuadrantTextSize = 25;
         int marginFromRight = screenWidth - 30;
         int marginFromBottom = screenHeight - 30;
         int marginFromLeft = 30;
         int marginFromTop = 30;
         String quadrant1Name = radarData.getQuadrants().get(1).getName();
         String quadrant2Name = radarData.getQuadrants().get(0).getName();
         String quadrant3Name = radarData.getQuadrants().get(2).getName();
         String quadrant4Name = radarData.getQuadrants().get(3).getName();
         switch(currentQuadrant){
             case 0:
                 canvas.drawText(quadrant2Name, marginFromLeft, marginFromTop, getQuadrantTextPaint(textAlign, quadrantTextSize));
                 canvas.drawText(quadrant1Name, marginFromRight, marginFromTop, getQuadrantTextPaint(Paint.Align.RIGHT, quadrantTextSize));
                 canvas.drawText(quadrant3Name, marginFromLeft, marginFromBottom, getQuadrantTextPaint(textAlign, quadrantTextSize));
                 canvas.drawText(quadrant4Name, marginFromRight, marginFromBottom, getQuadrantTextPaint(Paint.Align.RIGHT, quadrantTextSize));
                 break;
             case 1:
                 canvas.drawText(quadrant1Name, marginFromRight, marginFromTop, getQuadrantTextPaint(Paint.Align.RIGHT, zoomedQuadrantTextSize));
                 break;
             case 2:
                 canvas.drawText(quadrant2Name, marginFromLeft, marginFromTop, getQuadrantTextPaint(Paint.Align.LEFT, zoomedQuadrantTextSize));
                 break;
             case 3:
                 canvas.drawText(quadrant3Name, marginFromLeft, marginFromBottom, getQuadrantTextPaint(Paint.Align.LEFT, zoomedQuadrantTextSize));
                 break;
             case 4:
                 canvas.drawText(quadrant4Name, marginFromRight, marginFromBottom, getQuadrantTextPaint(Paint.Align.RIGHT, zoomedQuadrantTextSize));
                 break;
         }
     }
 
     private Paint getQuadrantTextPaint(Paint.Align textAlign, int textSize) {
         Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
         p.setTextSize(textSize);
         p.setColor(Color.rgb(37, 170, 225));
         p.setTextAlign(textAlign);
         return p;
     }
 
     private void drawRadarCircles(int centerX, int centerY, Canvas canvas, Paint circlePaint) {
         for (RadarArc radarArc : radarData.getRadarArcs()) {
             float circleRadius = multiplier * radarArc.getRadius();
             drawRadarCircleWithTitle(centerX, centerY, circleRadius, canvas, circlePaint, radarArc);
         }
     }
 
 
     private float getRadiusOfOutermostArc(List<RadarArc> radarArcs) {
         float maxRadius = 0.0f;
         for (RadarArc arc : radarArcs) {
             if (arc.getRadius() > maxRadius) {
                 maxRadius = arc.getRadius();
             }
         }
         return maxRadius;
     }
 
     private List<Blip> getBlipsForRadarData() {
         List<RadarItem> radarItems = (radarArcFilter == null)? radarData.getItems() : radarData.getItemsForArc(radarArcFilter);
         if(searchText!= null && searchText.length() > 0) {
             radarItems = radarData.getItemWithText(radarItems, searchText);
         }
         List<Blip> blips = new ArrayList<Blip>(radarItems.size());
         for (RadarItem radarItem : radarItems) {
             Blip blip = getBlipItem(radarItem);
             blips.add(blip);
         }
 
         return blips;
     }
 
     private Blip getBlipItem(RadarItem radarItem) {
         float xCoordinate = getXCoordinate(radarItem);
         float yCoordinate = getYCoordinate(radarItem);
         return Blip.getBlipForRadarItem(radarItem, xCoordinate, yCoordinate, displayMetrics.xdpi);
     }
 
     private void drawRadarBlips(Canvas canvas) {
         for (Blip blip : blips) {
             blip.render(canvas, currentQuadrant);
         }
     }
 
     private void drawBackground(Canvas canvas) {
         Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
         paint.setColor(Color.rgb(213,213,213));
         paint.setStyle(Paint.Style.FILL);
 
         canvas.drawRect(mainView.getLeft(),mainView.getTop(), mainView.getRight(),mainView.getBottom(),paint);
     }
 
 
     public void filterSearchText(CharSequence charSequence) {
         this.searchText = charSequence;
         drawRadar();
     }
 
     private void drawRadarCircleWithTitle(float centerX, float centerY, float circleRadius, Canvas canvas, Paint circlePaint, RadarArc radarArc) {
         Path circle = new Path();
         circle.addCircle(centerX, centerY, circleRadius, Path.Direction.CCW);
         drawCircle(canvas, circle, circlePaint);
         drawCircleTitle(canvas, circle, circleRadius, radarArc.getName(), circlePaint);
     }
 
     private Rect getTextBounds(Paint paint, String text) {
         Rect bounds = new Rect();
         paint.getTextBounds(text, 0, text.length(), bounds);
         return bounds;
     }
 
     private void drawCircle(Canvas canvas, Path circle, Paint circlePaint) {
         Shape shape = new PathShape(circle, 1, 1);
         shape.resize(1, 1);
         shape.draw(canvas, circlePaint);
     }
 
     private void drawCircleTitle(Canvas canvas, Path circle, float circleRadius, String name, Paint circlePaint) {
         float hOffset =  circleRadius*2.25f;
         final float vOffset = getTextBounds(circlePaint, name).height() / 2;
 
         setPaintForCircleTitles(circlePaint);
 
         canvas.drawTextOnPath(name, circle, hOffset, vOffset, circlePaint);
 
         restorePaintSettingsForDrawingOtherthanTitles(circlePaint);
     }
 
     private void restorePaintSettingsForDrawingOtherthanTitles(Paint circlePaint) {
         circlePaint.setStyle(Paint.Style.STROKE);
         circlePaint.setColor(Color.WHITE);
     }
 
     private void setPaintForCircleTitles(Paint circlePaint) {
         circlePaint.setTextSize(20);
         circlePaint.setColor(Color.GRAY);
         circlePaint.setStyle(Paint.Style.FILL);
         circlePaint.setTextAlign(Paint.Align.CENTER);
     }
 
     private boolean isRadarItemRenderable(RadarItem radarItem){
         return (currentQuadrant == 0) || (currentQuadrant == radarItem.getQuadrant());
     }
 
 
 }
