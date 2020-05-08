 package com.games.spaceman.pal;
 
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 
 import com.games.spaceman.SpaceApp;
 import com.spacemangames.pal.IBitmap;
 
 public class AndroidBitmap implements IBitmap {
    private Drawable         drawable;
    private int              width;
    private int              height;
    private final String     name;
 
     private static Resources mResources;
    private final int        identifier;
    private final boolean    lazyLoading;
     static {
         mResources = SpaceApp.mAppContext.getResources();
     }
 
     public AndroidBitmap(String aResource, boolean lazyLoading) {
         this.lazyLoading = lazyLoading;
         name = aResource;
        identifier = mResources.getIdentifier(name, "drawable", "com.spacemangames.gravisphere");
 
         if (!lazyLoading) {
             loadDrawable();
         }
     }
 
     private void loadDrawable() {
         drawable = mResources.getDrawable(identifier);
 
         width = drawable.getIntrinsicWidth();
         height = drawable.getIntrinsicHeight();
     }
 
     public Drawable getDrawable() {
         if (drawable == null) {
             loadDrawable();
         }
 
         return drawable;
     }
 
     public void releaseLazyMemory() {
         if (lazyLoading) {
             drawable = null;
         }
     }
 
     public int getWidth() {
         return width;
     }
 
     public int getHeight() {
         return height;
     }
 
     public String getName() {
         return name;
     }
 }
