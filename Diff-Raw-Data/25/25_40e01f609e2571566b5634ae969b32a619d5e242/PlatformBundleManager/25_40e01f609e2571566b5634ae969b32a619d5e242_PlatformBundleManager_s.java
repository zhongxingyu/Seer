 package com.twam.boostrunner.manager;
 
 import com.twam.boostrunner.data.PlatformBundleData;
 import com.twam.boostrunner.entity.Platform;
 import com.twam.boostrunner.entity.PlatformBundle;
 import com.twam.boostrunner.entity.StaticObstacle;
 import com.twam.boostrunner.pool.PlatformBundlePool;
 import com.twam.boostrunner.pool.PlatformPool;
 import com.twam.boostrunner.pool.StaticObstaclePool;
 
 import org.andengine.entity.IEntity;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 /**
  * Created by parsia on 7/15/13.
  */
 public class PlatformBundleManager {
 
     //============================================================
     // constants
     //============================================================
     private final static float platformBundleY = 30;
     private final int platformBundleDataCount = 10;
     //============================================================
     // fields
     //============================================================
     private static PlatformBundleManager instance;
     private PlatformPool platformPool;
     private PlatformBundlePool platformBundlePool;
     private StaticObstaclePool staticObstaclePool;
     private PlatformBundle lastPlatformBundle;
     private ArrayList<PlatformBundle> platformBundlesArray;
     private PlatformBundleData[] platformBundleDatas;
 
     //============================================================
     // constructor
     //============================================================
 
     /**
      * private constructor for singleton pattern implementation
      * */
     private PlatformBundleManager() {
         platformBundlesArray = new ArrayList<PlatformBundle>();
         platformBundleDatas = new PlatformBundleData[platformBundleDataCount];
     }
 
 
     //============================================================
     // getter & setter
     //============================================================
 
     public ArrayList<PlatformBundle> getPlatformBundlesArray() {
         return platformBundlesArray;
     }
 
     public PlatformBundleData[] getPlatformBundleDatas() {
         return platformBundleDatas;
     }
 
     //============================================================
     // methods
     //============================================================
 
     /**
      *getInstance for singleton pattern implementation
      * */
     public static PlatformBundleManager getInstance() {
         if(PlatformBundleManager.instance == null)
             instance = new PlatformBundleManager();
         return instance;
     }
 
     /**
      * initials the pools ( PlatformPool & PlatformBundlePool )
      * */
     public void initPools(ITextureRegion platformTexture, ITextureRegion staticobstacleTexture, VertexBufferObjectManager vertexBufferObjectManager){
         this.platformPool = new PlatformPool(platformTexture, vertexBufferObjectManager);
         this.staticObstaclePool = new StaticObstaclePool(staticobstacleTexture, vertexBufferObjectManager);
         this.platformBundlePool = new PlatformBundlePool();
     }
 
     /**
      * reset platform bundle manager
      * */
     public void reset(){
         stopPlatformBundleManager();
         lastPlatformBundle = null;
         for(int i=0; i<3; i++){
             makeNextPlatformBundle();
         }
     }
 
     public void onPause(){
         lastPlatformBundle = null;
     }
     /**
      * make a platform bundle
      * */
     public PlatformBundle makePlatformBundle(float x,float y, PlatformBundleData platformBundleData){
         PlatformBundle platformBundle = platformBundlePool.obtainPoolItem(x, y);
         platformBundle.setPlatformCount(platformBundleData.getNumberOfPlatform());
         platformBundle.setStaticObstacleCount(platformBundleData.getNumberOfStaticObstacle());
         for (int i=0; i<platformBundle.getPlatformeCount(); i++){
             Platform platform = platformPool.obtainPoolItem(0,0);
             platformBundle.addPlatform(platform);
         }
         for (int i=0; i<platformBundle.getStaticObstacleCount(); i++){
             StaticObstacle staticObstacle = staticObstaclePool.obtainPoolItem(0,0);
             platformBundle.addStaticObstacle(staticObstacle,platformBundleData.getStaticObstacleX()[i]*Platform.WIDTH + StaticObstacle.WIDTH,platformBundleData.getStaticObstacleY()[i]*StaticObstacle.HEIGHT + Platform.HEIGHT);
         }
         platformBundle.updateBodiesPosition();
         return platformBundle;
     }
 
     /**
      * recycling a bundle and it's platforms
      * */
     public void recyclePlatformBundle(PlatformBundle platformBundle){
         while(platformBundle.getChildCount() > 0){
             IEntity entity = platformBundle.getLastChild();
             if (entity instanceof Platform) {
                 Platform platform = platformBundle.deleteLastPlatform();
                 platformPool.recyclePoolItem(platform);
             }
             else if (entity instanceof StaticObstacle) {
                 StaticObstacle staticObstacle = platformBundle.deleteLastStaticObstacle();
                 staticObstaclePool.recyclePoolItem(staticObstacle);
             }
         }
         platformBundlePool.recyclePoolItem(platformBundle);
     }
 
     /**
      * making the next platform Bundle corresponding to ...
      * */
     public PlatformBundle makeNextPlatformBundle(){
         float platformBundleX = 100f;
         if(lastPlatformBundle != null){
              platformBundleX = this.lastPlatformBundle.getRightBoundary() + generateGapLength();
         }
         PlatformBundle platformBundle = makePlatformBundle(platformBundleX, platformBundleY,getNextPlatformBundleData());
         platformBundlesArray.add(platformBundle);
         this.lastPlatformBundle = platformBundle;
         return platformBundle;
     }
 
     public int generatePlatformCount(){
         return 9;
 //        Random random = new Random();
 //        return random.nextInt(10)+ 6;
     }
 
     public int generateStaticObstacleCount(){
         Random random = new Random();
         return 1;
     }
 
     public PlatformBundleData getNextPlatformBundleData()
     {
         Random random = new Random();
         return platformBundleDatas[random.nextInt(10)];
     }
 
     public float generateGapLength(){
         return 200;
 //        Random random = new Random();
 //        if(!random.nextBoolean())
 //            return  0;
 //        else{
 //            return random.nextFloat()*100 + 100;
 //        }
     }
 
     public void stopPlatformBundleManager() {
         for(PlatformBundle platformBundle : platformBundlesArray) {
             recyclePlatformBundle(platformBundle);
         }
         platformBundlesArray.removeAll(platformBundlesArray);
     }
 
 }
