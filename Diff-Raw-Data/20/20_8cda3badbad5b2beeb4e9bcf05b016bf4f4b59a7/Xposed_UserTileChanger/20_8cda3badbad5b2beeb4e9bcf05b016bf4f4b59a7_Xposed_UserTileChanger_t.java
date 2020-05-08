 /*
  *      HarshJelly Tweaker - An app to Tweak HarshJelly ROM
  *      Author : Harsh Panchal <panchal.harsh18@gmail.com, mr.harsh@xda-developers.com>
  */
 package com.harsh.romtool;
 
 import java.io.File;
 
 import android.content.res.XResources;
 import android.graphics.drawable.Drawable;
 import de.robv.android.xposed.IXposedHookInitPackageResources;
 import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
 
 public class Xposed_UserTileChanger implements IXposedHookInitPackageResources {
 
 	@Override
 	public void handleInitPackageResources(InitPackageResourcesParam resparam)
 			throws Throwable {
 		File HarshJellyResPath = new File("/data/HarshJelly");
 		boolean ret = HarshJellyResPath.mkdir();
		Runtime.getRuntime().exec("chmod 777 /data/HarshJelly");
 		if (!resparam.packageName.equals("com.android.systemui"))
 			return;
 		File PROF_NORM = new File(HarshJellyResPath+"/my_prof_normal.png");
 		File PROF_PRESSED = new File(HarshJellyResPath+"/my_prof_pressed.png");
 		if (PROF_PRESSED.exists()) {
 			final Drawable myDrawable = Drawable.createFromPath(HarshJellyResPath+"/my_prof_pressed.png");
 			resparam.res.setReplacement("com.android.systemui", "drawable", "my_prof_pressed", new XResources.DrawableLoader() {
 				@Override
 				public Drawable newDrawable(XResources res, int id) throws Throwable {
 					return myDrawable;
 				}
 			});
 		}
 		if (PROF_NORM.exists()) {
 			final Drawable myDrawable = Drawable.createFromPath(HarshJellyResPath+"/my_prof_normal.png");
 			resparam.res.setReplacement("com.android.systemui", "drawable", "my_prof_normal", new XResources.DrawableLoader() {
 				@Override
 				public Drawable newDrawable(XResources res, int id) throws Throwable {
 					return myDrawable;
 				}
 			});
 		}
 	}
 
 }
