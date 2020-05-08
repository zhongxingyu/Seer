 package com.tw.techradar.support.quadrants;
 
 import android.graphics.*;
 import android.graphics.drawable.PictureDrawable;
 import android.graphics.drawable.shapes.PathShape;
 import android.graphics.drawable.shapes.Shape;
 import android.util.DisplayMetrics;
 import android.util.FloatMath;
 import android.view.View;
 import com.tw.techradar.constants.SizeConstants;
 import com.tw.techradar.model.Radar;
 import com.tw.techradar.model.RadarArc;
 import com.tw.techradar.model.RadarItem;
 import com.tw.techradar.ui.model.Blip;
 
 import java.util.*;
 
 public abstract class QuadrantView {
 
     protected DisplayMetrics displayMetrics;
     protected Radar radarData;
     protected int screenOriginX;
     protected int screenOriginY;
     protected int maxRadius;
     protected int marginFromRight;
     protected int marginFromBottom;
 
     private float scalingFactor;
     private View mainView;
     private final int marginX;
     private final int marginY;
     private List<Blip> blips;
     private RadarArc radarArcFilter;
     private String radarTextFilter;
 
     private static final int RETRY_LIMIT = 100;
     private List<Blip> renderedBlips;
     private int fingerTipRadius;
 
     public QuadrantView(DisplayMetrics displayMetrics, View mainView,Radar radarData,int marginX, int marginY){
 
         this.displayMetrics = displayMetrics;
         this.mainView = mainView;
         this.radarData = radarData;
         this.marginX = marginX;
         this.marginY = marginY;
         this.fingerTipRadius = Math.round(SizeConstants.FINGER_TIP_DETECTION_RADIUS_INCH * displayMetrics.xdpi);
         determineMaxRadiusAndOrigins();
         determineScalingFactor();
         determineQuadrantCaptionMargins();
     }
 
     public QuadrantView filterWith(String textFilter){
         this.radarTextFilter = textFilter;
         return this;
     }
 
     public QuadrantView filterWith(RadarArc arcFilter){
         this.radarArcFilter = arcFilter;
         return this;
     }
 
     public QuadrantView clearFilter(){
         this.radarArcFilter = null;
         this.radarTextFilter = null;
         return this;
     }
 
     public void render(){
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
 
     protected Paint getQuadrantTextPaint(Paint.Align textAlign, int textSize) {
         Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
         p.setTextSize(textSize);
         p.setColor(Color.rgb(37, 170, 225));
         p.setTextAlign(textAlign);
         return p;
     }
 
     public boolean isPointInQuadrant(int x, int y) {
        int correctedX = x ;//- marginX;
        int correctedY = y ;//- marginY;
         return (correctedX >= getStartX()) && (correctedX <= getEndX()) && (correctedY >= getStartY()) && (correctedY <= getEndY());
     }
 
     public Blip getClosestBlipForTouchEvent(float x, float y){
         Blip blipTouched = null;
         double minDistance = Double.MAX_VALUE;
 
 
         for (Blip radarBlip : renderedBlips) {
             if (radarBlip.isPointInBlip(x,y)){
                 //If exact touch then return immediately
                 blipTouched = radarBlip;
                 break;
             }
 
             double distanceMeasure = radarBlip.getDistanceFromPoint(x, y);
             float toleranceRadius = radarBlip.getRadius() + fingerTipRadius;
 
             if (distanceMeasure <= toleranceRadius){
                 //Blip lies within touch radius
                 if (distanceMeasure < minDistance){
                     //Keep track of closest blip
                     minDistance = distanceMeasure;
                     blipTouched = radarBlip;
                 }
             }
 
         }
 
         return blipTouched;
 
     }
 
     public abstract QuadrantType getQuadrantType();
 
     protected abstract int getEndY();
 
     protected abstract int getStartY();
 
     protected abstract int getEndX();
 
     protected abstract int getStartX();
 
     protected abstract String getQuadrantName();
 
     protected abstract void renderQuadrantCaption(Canvas canvas);
 
     protected abstract void determineMaxRadiusAndOrigins();
 
     protected abstract int getQuadrantStartTheta();
 
     protected abstract int getQuadrantEndTheta();
 
     protected abstract int getThetaAdjustmentForOverlap(Blip blip);
 
     private void determineScalingFactor(){
         scalingFactor = (float) maxRadius / getRadiusOfOutermostArc(radarData.getRadarArcs());
     }
 
     public void initialize() {
         List<RadarItem> radarItems = Collections.<RadarItem>unmodifiableList(radarData.getItems());
         this.blips = new ArrayList<Blip>(radarItems.size());
         for (RadarItem radarItem : radarItems) {
             if (isRadarItemInQuadrant(radarItem)){
                 Blip blip = getBlipItem(radarItem);
                 blips.add(blip);
             }
         }
         fixQuadrantOverlapsForCurrentQuadrant();
         adjustForCollisions();
     }
 
     private void fixQuadrantOverlapsForCurrentQuadrant(){
         for (Blip blip : blips) {
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
         while (!isBlipWithinZoomedQuadrantBoundaries(blip)) {
             thetaAdj = getThetaAdjustmentForOverlap(blip);
             totalAdjustment = totalAdjustment + thetaAdj;
             adjustTheta(blip, totalAdjustment);
         }
         return totalAdjustment;
     }
 
 
     /**
      * Adjusts for collisions - WIP code
      */
     private void adjustForCollisions() {
         if (getQuadrantType() == QuadrantType.QUADRANT_ALL) return;
         int retries = 0;
         int collisionCount = 0;
         boolean collisionFlag = false;
         do{
         retries++;
         collisionFlag = false;
         for (Blip referenceBlip : blips) {
             for (Blip blip : blips) {
                 if (blip == referenceBlip)  continue;
                 boolean isCollisionPresent = checkAndResolveCollision(referenceBlip, blip);
                 collisionFlag = collisionFlag | isCollisionPresent;
                 if (isCollisionPresent)
                     collisionCount++;
             }
         }
         }while(collisionFlag && (retries < RETRY_LIMIT));
         System.out.println("Collisions detected for Quadrant[" + getQuadrantType() + "]:" + collisionCount);
     }
 
     private boolean checkAndResolveCollision(Blip referenceBlip, Blip blip) {
         boolean collisionFlag=false;
         while (chkIfCollision(referenceBlip, blip)) {
 
             int blipThetaAdj;
             int refBlipThetaAdj;
 
             blipThetaAdj = (referenceBlip.getRadarItem().getTheta() > blip.getRadarItem().getTheta()) ? -1 : 1;
             refBlipThetaAdj = -blipThetaAdj;
 
             if (!blip.isFrozen())
                 adjustTheta(blip, blipThetaAdj);
 
             if (!referenceBlip.isFrozen())
                 adjustTheta(referenceBlip, refBlipThetaAdj);
 
             if (!isBlipWithinZoomedQuadrantBoundaries(blip) && !blip.isFrozen()){
                 System.out.println("Collision - boundary condition reached for blip:" + blip.getRadarItem().getName() + ".Reverting theta and ignoring collision for now");
                 adjustTheta(blip, -blipThetaAdj);
                 blip.freeze();
 
             }
 
             if (!isBlipWithinZoomedQuadrantBoundaries(referenceBlip) && !referenceBlip.isFrozen()){
                 System.out.println("Collision - boundary condition reached for blip:"  + referenceBlip.getRadarItem().getName() + ".Reverting theta and ignoring collision for now");
                 adjustTheta(referenceBlip, - refBlipThetaAdj);
                 referenceBlip.freeze();
             }
 
             if (referenceBlip.isFrozen() && blip.isFrozen()){
                 System.out.println("Collision - unresolveable:" + referenceBlip.getRadarItem().getName() + " and " + blip.getRadarItem().getName());
                 //Collision cannot be solved
                 return false;
             }
 
             collisionFlag = true;
         }
         if (collisionFlag){
             System.out.println("Collision between " + referenceBlip.getRadarItem().getName() + " and " + blip.getRadarItem().getName());
         }
         return collisionFlag;
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
 
 
     protected boolean isBlipWithinZoomedQuadrantBoundaries(Blip blip) {
         Rect dimensions = blip.getDimensionsWithText();
         int maxX = displayMetrics.widthPixels;
         int maxY = displayMetrics.heightPixels;
 
         return (dimensions.right <= maxX) && (dimensions.bottom <= maxY) && (dimensions.left >= 0) && (dimensions.top >= 0);
     }
 
 
     private boolean isRadarItemInQuadrant(RadarItem radarItem) {
         return (radarItem.getTheta() >= getQuadrantStartTheta()) && (radarItem.getTheta() <= getQuadrantEndTheta());
     }
 
     private Blip getBlipItem(RadarItem radarItem) {
         float xCoordinate = getXCoordinate(radarItem);
         float yCoordinate = getYCoordinate(radarItem);
         return Blip.getBlipForRadarItem(radarItem, xCoordinate, yCoordinate, displayMetrics.xdpi);
     }
 
     private float getXCoordinate(RadarItem radarItem) {
 
         float xCoord = radarItem.getRadius() * scalingFactor * FloatMath.cos((float) Math.toRadians(radarItem.getTheta()));
         return translateXCoordinate(xCoord);
     }
 
     private float translateXCoordinate(float xCoord) {
         float transaltedXCoord = xCoord - screenOriginX;
         return transaltedXCoord;
     }
 
 
     private float getYCoordinate(RadarItem radarItem) {
         float yCoord = radarItem.getRadius() * scalingFactor * FloatMath.sin((float) Math.toRadians(radarItem.getTheta()));
         return translateYCoordinate(yCoord);
     }
 
     private float translateYCoordinate(float yCoord) {
         float transaltedYCoord = yCoord - screenOriginY;
         return -transaltedYCoord;
     }
 
     private void drawBackground(Canvas canvas) {
         Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
         paint.setColor(Color.rgb(213,213,213));
         paint.setStyle(Paint.Style.FILL);
 
         canvas.drawRect(0,0, displayMetrics.widthPixels,displayMetrics.heightPixels,paint);
     }
 
     private void drawRadarQuadrants(int screenWidth, int screenHeight,
                                     int centerX, int centerY, Canvas canvas, Paint paint) {
         canvas.drawLine((float) 0, (float) centerY, (float) screenWidth, (float) centerY, paint);
         canvas.drawLine((float) centerX, (float) 0, (float) centerX, (float) screenHeight, paint);
         renderQuadrantCaption(canvas);
     }
 
     private void drawRadarCircles(int centerX, int centerY, Canvas canvas, Paint circlePaint) {
         for (RadarArc radarArc : radarData.getRadarArcs()) {
             float circleRadius = scalingFactor * radarArc.getRadius();
             drawRadarCircleWithTitle(centerX, centerY, circleRadius, canvas, circlePaint, radarArc);
         }
     }
 
     private void drawRadarCircleWithTitle(float centerX, float centerY, float circleRadius, Canvas canvas, Paint circlePaint, RadarArc radarArc) {
         Path circle = new Path();
         circle.addCircle(centerX, centerY, circleRadius, Path.Direction.CCW);
         drawCircle(canvas, circle, circlePaint);
         drawCircleTitle(canvas, circle, circleRadius, radarArc.getName(), circlePaint);
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
 
     private Rect getTextBounds(Paint paint, String text) {
         Rect bounds = new Rect();
         paint.getTextBounds(text, 0, text.length(), bounds);
         return bounds;
     }
 
     private void setPaintForCircleTitles(Paint circlePaint) {
         circlePaint.setTextSize(20);
         circlePaint.setColor(Color.GRAY);
         circlePaint.setStyle(Paint.Style.FILL);
         circlePaint.setTextAlign(Paint.Align.CENTER);
     }
 
     private void restorePaintSettingsForDrawingOtherthanTitles(Paint circlePaint) {
         circlePaint.setStyle(Paint.Style.STROKE);
         circlePaint.setColor(Color.WHITE);
     }
 
     private void drawRadarBlips(Canvas canvas) {
         this.renderedBlips = filterBlipsByText(filterBlipsByArc(this.blips));
         for (Blip blip : renderedBlips) {
             blip.render(canvas, getQuadrantType().getQuadrantNo());
         }
     }
 
     private List<Blip> filterBlipsByArc(List<Blip> inputBlips) {
         if (this.radarArcFilter == null){
             return inputBlips;
         }
 
         List<Blip> filteredBlips = new ArrayList<Blip>();
         for (Blip blip : inputBlips) {
             if (blip.isBlipInArc(radarArcFilter)){
                 filteredBlips.add(blip);
             }
         }
 
         return filteredBlips;
     }
 
     private List<Blip> filterBlipsByText(List<Blip> inputBlips){
         if(radarTextFilter== null || radarTextFilter.trim().length() == 0) {
             return inputBlips;
         }
 
         radarTextFilter = radarTextFilter.trim();
 
         List<Blip> filteredBlips = new ArrayList<Blip>();
         for (Blip blip : inputBlips) {
             if (blip.getRadarItem().getName().toLowerCase().contains(radarTextFilter.toLowerCase()) || blip.getRadarItem().getDescription().toLowerCase().contains(radarTextFilter.toLowerCase())){
                 filteredBlips.add(blip);
             }
         }
 
         return filteredBlips;
 
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
 
     private void determineQuadrantCaptionMargins(){
         marginFromRight = displayMetrics.widthPixels - SizeConstants.MARGIN_PADDING_PIXELS;
         marginFromBottom = displayMetrics.heightPixels - SizeConstants.MARGIN_PADDING_PIXELS;
     }
 
     private boolean chkIfCollision(Blip blipA, Blip blipB) {
         return blipA.getTextDimensions().intersect(blipB.getTextDimensions()) || blipA.getIconDimensions().intersect(blipB.getIconDimensions()) || blipA.getIconDimensions().intersect(blipB.getTextDimensions()) || blipB.getIconDimensions().intersect(blipA.getTextDimensions());
     }
 
 
 }
