 package com.tw.techradar.support;
 
 import android.app.Activity;
 import android.util.DisplayMetrics;
 import android.view.View;
 import com.tw.techradar.model.Radar;
 import com.tw.techradar.model.RadarArc;
 import com.tw.techradar.support.quadrants.*;
 import com.tw.techradar.ui.model.Blip;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 public class RadarView {
 
     private Radar radarData;
     private View mainView;
     private DisplayMetrics displayMetrics;
     private int marginX;
     private int marginY;
     private QuadrantView quadrantView;
     private Map<QuadrantType,QuadrantView> quadrantViews;
 
 
     public RadarView(DisplayMetrics displayMetrics, Radar radarData, View mainView) {
         this.displayMetrics = displayMetrics;
         this.radarData = radarData;
         this.mainView = mainView;
     }
 
     public void drawRadar() {
         determineBoundsAndDimensions();
 
         if (!isQuadrantViewInitialized())
             initializeQuadrants(displayMetrics, mainView, radarData, marginX, marginY);
 
         quadrantView = getQuadrantViewFor(QuadrantType.QUADRANT_ALL);
         quadrantView.render();
     }
 
     private void determineBoundsAndDimensions() {
         this.marginX = displayMetrics.widthPixels - mainView.getMeasuredWidth();
         this.marginY = displayMetrics.heightPixels - mainView.getMeasuredHeight();
         System.out.println(String.format("MarginX %d MarginY %d", this.marginX, this.marginY));
 
         displayMetrics.heightPixels = displayMetrics.heightPixels - marginY;
         displayMetrics.widthPixels = displayMetrics.widthPixels - marginX;
     }
 
     public void switchQuadrant(QuadrantType quadrantType) {
         quadrantView = getQuadrantViewFor(quadrantType);
         quadrantView.render();
     }
 
     public void filterByRadarArc(RadarArc radarArc) {
         for (QuadrantView view : quadrantViews.values()) {
             view.filterWith(radarArc);
         }
         quadrantView.render();
     }
 
     public void filterBySearchText(String filterText) {
         System.out.println("QUADRANT VIEWS:" + quadrantViews);
         for (QuadrantView view : quadrantViews.values()) {
             view.filterWith(filterText);
         }
         quadrantView.render();
     }
 
 
     public QuadrantType getQuadrantClicked(float x, float y) {
         return getQuadrantForPoint((int) x, (int) y).getQuadrantType();
     }
 
     public Blip getBlipClicked(float clickX, float clickY) {
//        float correctedX = clickX - marginX;
//        float correctedY = clickY - marginY;
//        return quadrantView.getClosestBlipForTouchEvent(correctedX, correctedY);

        return quadrantView.getClosestBlipForTouchEvent(clickX, clickY);
 
     }
 
     public QuadrantType getCurrentQuadrantType() {
         return quadrantView.getQuadrantType();
     }
 
     public boolean isZoomed() {
         return getCurrentQuadrantType() != QuadrantType.QUADRANT_ALL;
     }
 
     public void zoomOut() {
         switchQuadrant(QuadrantType.QUADRANT_ALL);
     }
 
     private void initializeQuadrants(DisplayMetrics displayMetrics, View mainView,Radar radarData,int marginX, int marginY){
         quadrantViews = new HashMap<QuadrantType, QuadrantView>();
         //Important: First initialize all individual quadrants. Each individual quadrant will mutate specific sections of RadarData RadarItem objects as per the overlaps / collisions detected.
         //May need cleanup
 
         quadrantViews.put(QuadrantType.QUADRANT_1, new Quadrant1View(displayMetrics,mainView,radarData,marginX,marginY));
         quadrantViews.put(QuadrantType.QUADRANT_2, new Quadrant2View(displayMetrics,mainView,radarData,marginX,marginY));
         quadrantViews.put(QuadrantType.QUADRANT_3, new Quadrant3View(displayMetrics,mainView,radarData,marginX,marginY));
         quadrantViews.put(QuadrantType.QUADRANT_4, new Quadrant4View(displayMetrics,mainView,radarData,marginX,marginY));
 
         //Use the mutated RadarData RadarItems (corrected for collisions / overlaps) for Quadrant 0
 
         quadrantViews.put(QuadrantType.QUADRANT_ALL, new AllQuadrantView(displayMetrics,mainView,radarData,marginX,marginY));
         initializeQuadrantData(quadrantViews.values());
     }
 
     private boolean isQuadrantViewInitialized() {
         return quadrantViews != null;
     }
 
 
     //Initialize each quadrant - also adjust for overlaps and collisions
     private static void initializeQuadrantData(Collection<QuadrantView> quadrantViews) {
         for (QuadrantView quadrantView : quadrantViews) {
             quadrantView.initialize();
         }
     }
 
     private QuadrantView getQuadrantForPoint(int x, int y){
         for (QuadrantView quadrantView : quadrantViews.values()) {
             if (quadrantView.isPointInQuadrant(x,y)){
                 return quadrantView;
             }
         }
         return null;
     }
 
     private QuadrantView getQuadrantViewFor(QuadrantType quadrantType) {
         return quadrantViews.get(quadrantType);
     }
 
 
 }
