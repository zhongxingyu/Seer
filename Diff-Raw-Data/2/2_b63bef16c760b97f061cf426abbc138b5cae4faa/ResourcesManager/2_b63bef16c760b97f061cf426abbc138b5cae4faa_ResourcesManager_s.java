 package com.codefest2013.game;
 
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
 import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
 import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
 import org.andengine.opengl.texture.bitmap.BitmapTextureFormat;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.ui.activity.BaseGameActivity;
 import org.andengine.util.debug.Debug;
 
 import android.util.DisplayMetrics;
 
 public class ResourcesManager {
 	
     public static float CAMERA_WIDTH;
     public static float CAMERA_HEIGHT;
     public static float WORLD_WIDTH;
     public static float WORLD_HEIGHT;
     
     /**
      * it was calculated by size of room(background) image
      * WORLD_SCALE_CONSTANT = width / height
      * XXX I think that scaling system is not so well implemented right now 
      */
     private static final float PROPORTION_CONSTANT = 2.0686f;
     public static float WORLD_SCALE_CONSTANT;
 
     public ITextureRegion[] goblinLeftWalk = new ITextureRegion[12];
     public ITextureRegion[] goblinRightWalk = new ITextureRegion[12];
     
     /**
      * Parts of background image
      */
     public ITextureRegion bgLB;
     public ITextureRegion bgLT;
     public ITextureRegion bgRB;
     public ITextureRegion bgRT;
     
     /**
      * Fireplace animation
      */
     public ITextureRegion fireplace1;
     public ITextureRegion fireplace2;
     
     /**
      * Tree texture
      */
     public ITextureRegion tree;
     
     /**
      * Clock animation
      */
     public ITextureRegion[] clock = new ITextureRegion[10];
     
     /**
      * Lamp texture
      */
     public ITextureRegion lamp;
     
     /**
      * Stocking texture
      */
     public ITextureRegion stocking;
     
 	private static ResourcesManager mInstance = new ResourcesManager();
 	
 	private ResourcesManager()
 	{ 
 	}
 	
 	public static void init()
 	{
         final DisplayMetrics displayMetrics = MainActivity.getInstance().getResources().getDisplayMetrics();
 
         CAMERA_WIDTH = displayMetrics.widthPixels;
         CAMERA_HEIGHT = displayMetrics.heightPixels;
 
         WORLD_WIDTH = CAMERA_HEIGHT * PROPORTION_CONSTANT;
         WORLD_HEIGHT = CAMERA_HEIGHT;
         
         WORLD_SCALE_CONSTANT = WORLD_HEIGHT;
 	}
 	
 	public void load()
 	{
 		/*
 		final DisplayMetrics displayMetrics = MainActivity.getInstance().getResources().getDisplayMetrics();
         
 		float mScaleFactor = 1;
 	    int deviceDpi = displayMetrics.densityDpi;
 	    switch(deviceDpi){
 		    case DisplayMetrics.DENSITY_LOW:
 			    // Scale factor already set to 1
 			    break;
 		    case DisplayMetrics.DENSITY_MEDIUM:
 			    // Increase scale to a suitable value for mid-size displays
 			    mScaleFactor = 1.5f;
 			    break;
 		    case DisplayMetrics.DENSITY_HIGH:
 			    // Increase scale to a suitable value for larger displays
 			    mScaleFactor = 2;
 			    break;
 		    case DisplayMetrics.DENSITY_XHIGH:
 			    // Increase scale to suitable value for largest displays
 			    mScaleFactor = 2.5f;
 			    break;
 		    default:
 			    // Scale factor already set to 1
 			    break;
 	    }
 	    */
 	    
 		BaseGameActivity instance = MainActivity.getInstance();
 		
     	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
     	BitmapTextureAtlas textureAtlas = null;
     	
     	textureAtlas = new BitmapTextureAtlas(instance.getTextureManager(), 1024, 1024,
         		BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
         bgLB = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, instance, "background/lb.png", 0, 0);
         bgLT = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, instance, "background/lt.png", 0, 496);
         textureAtlas.load();
         
         textureAtlas = new BitmapTextureAtlas(instance.getTextureManager(), 1024, 1024,
         		BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
         bgRB = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, instance, "background/rb.png", 0, 0);
         bgRT = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, instance, "background/rt.png", 0, 496);
         textureAtlas.load();
         
         textureAtlas = new BitmapTextureAtlas(instance.getTextureManager(), 1024, 1024,
         		BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
         fireplace1 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, instance, "background/fireplace1.png", 0, 0);
         fireplace2 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, instance, "background/fireplace2.png", 0, 512);
         textureAtlas.load();
         
         textureAtlas = new BitmapTextureAtlas(instance.getTextureManager(), 1024, 1024,
         		BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
         tree = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, instance, "background/tree.png", 0, 0);
         lamp = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, instance, "background/lamp.png", 500, 0);
        stocking = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, instance, "background/stocking.png", 500, 170);
         textureAtlas.load();
         
         textureAtlas = new BitmapTextureAtlas(instance.getTextureManager(), 1024, 1024,
         		BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
         for(int i=0; i<5; ++i)
         {
         	String name = "background/clock" + (i+1) + ".png";
         	clock[i] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, instance, name, 150*i, 0);
         }
         textureAtlas.load();
         
         textureAtlas = new BitmapTextureAtlas(instance.getTextureManager(), 1024, 1024,
         		BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
         for(int i=5; i<10; ++i)
         {
         	String name = "background/clock" + (i+1) + ".png";
         	clock[i] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, instance, name, 150*(i-5), 0);
         }
         textureAtlas.load();
         
         
         BuildableBitmapTextureAtlas mBuildableBitmapTextureAtlas = new BuildableBitmapTextureAtlas(instance.getTextureManager(), 1024, 1024, 
         		BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
         for(int i=0; i<12; ++i)
         {
         	String name = "goblin/rightWalk/" + i + ".png";
         	goblinRightWalk[i] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, instance, name);
         }
         try {
             mBuildableBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 0));
             mBuildableBitmapTextureAtlas.load();
         } catch (final TextureAtlasBuilderException e) {
             Debug.e(e);
         }
         
         mBuildableBitmapTextureAtlas = new BuildableBitmapTextureAtlas(instance.getTextureManager(), 1024, 1024, 
         		BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
         for(int i=0; i<12; ++i)
         {
         	String name = "goblin/leftWalk/" + i + ".png";
         	goblinLeftWalk[i] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, instance, name);
         }
         try {
             mBuildableBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 0));
             mBuildableBitmapTextureAtlas.load();
         } catch (final TextureAtlasBuilderException e) {
             Debug.e(e);
         }
 	}
 
 	public static ResourcesManager getInstance()
 	{
 		return mInstance;
 	}
 	
 }
