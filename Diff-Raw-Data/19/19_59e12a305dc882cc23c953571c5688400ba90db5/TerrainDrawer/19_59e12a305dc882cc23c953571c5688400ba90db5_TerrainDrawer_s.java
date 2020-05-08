 package com.sagen.balloonlander.terrain;
 
 import android.graphics.*;
 
 import static android.graphics.Color.GREEN;
 import static android.graphics.Paint.Style.FILL;
 import static com.sagen.balloonlander.ZoomUtil.zoomBoxXPos;
 import static com.sagen.balloonlander.ZoomUtil.zoomBoxYPos;
 
 public class TerrainDrawer {
     private Paint terrainPaint;
     private Paint landingPaint;
     Path zoomedPath;
     Matrix zoomTransformMatrix;
 
     public TerrainDrawer(){
         terrainPaint = new Paint();
         terrainPaint.setStyle(FILL);
         terrainPaint.setColor(GREEN);
         landingPaint = new Paint();
         landingPaint.setColor(Color.BLUE);
         zoomedPath = new Path();
         zoomTransformMatrix = new Matrix();
     }
 
     public void draw(Terrain terrain, Canvas c, int zoomLevel, int xViewPos, int yViewPos) {
         if(zoomLevel == 1){
             c.drawPath(terrain.orgPath, terrainPaint);
             c.drawRect(terrain.landingStart.x, terrain.landingStart.y, terrain.landingEnd.x,
                     terrain.landingStart.y + 5, landingPaint);
         }else{
            int zoomX = zoomBoxXPos(zoomLevel, c.getHeight(), xViewPos);
             int zoomY = zoomBoxYPos(zoomLevel, c.getHeight(), yViewPos);
             zoomTransformMatrix.setScale(zoomLevel, zoomLevel);
             zoomTransformMatrix.postTranslate(-zoomX, -zoomY);
             terrain.orgPath.transform(zoomTransformMatrix, zoomedPath);
             c.drawPath(zoomedPath, terrainPaint);
             c.drawRect(terrain.landingStart.x * zoomLevel - zoomX, terrain.landingStart.y * zoomLevel - zoomY,
                     terrain.landingEnd.x * zoomLevel - zoomX,
                     terrain.landingStart.y * zoomLevel - zoomY + (5 * zoomLevel), landingPaint);
         }
 
     }
 }
