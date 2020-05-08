 /*
  * GalDroid - a webgallery frontend for android
  * Copyright (C) 2011  Raptor 2101 [raptor2101@gmx.de]
  *		
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
  */
 
 package de.raptor2101.GalDroid.Activities;
 
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 
 import de.raptor2101.GalDroid.Config.GalDroidPreference;
 import de.raptor2101.GalDroid.Config.GalleryConfig;
 import de.raptor2101.GalDroid.WebGallery.GalleryCache;
 import de.raptor2101.GalDroid.WebGallery.GalleryFactory;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;
 import android.app.Activity;
 import android.app.Application;
 import android.net.http.AndroidHttpClient;
 
 public class GalDroidApp extends Application{
 	public static final String INTENT_EXTRA_DISPLAY_GALLERY = ".de.raptor2101.GalDroid.GalleryObject";
 	public static final String INTENT_EXTRA_DISPLAY_INDEX = ".de.raptor2101.GalDroid.DisplayIndex";
 	public static final String INTENT_EXTRA_DISPLAY_OBJECT = ".de.raptor2101.GalDroid.DisplayObject";
 	public static final String INTENT_EXTRA_GALLERY_PROVIDER = ".de.raptor2101.GalDroid.GalleryProvider";
 	public static final String INTENT_EXTRA_SHOW_IMAGE_INFO = ".de.raptor2101.GalDroid.ShowImageInfo";
 	
 	private WebGallery mWebGallery = null;
 	private GalleryCache mGalleryCache = null;
 	private List<GalleryObject> mGalleryChildObjects = null;
 	private GalleryObject mStoredGalleryObject = null;
 	
 	public WebGallery getWebGallery() {
 		return mWebGallery;
 	}
 
 	public GalleryCache getGalleryCache() {
 		return mGalleryCache;
 	}
 
 	public void Initialize(Activity activity) throws NoSuchAlgorithmException {
 		GalDroidPreference.Initialize(this);
 		
 		if(mGalleryCache == null){
 			mGalleryCache = new GalleryCache(activity);
 		}
 		
 		if(mWebGallery == null){
 			try {
 				String galleryName = activity.getIntent().getExtras().getString(INTENT_EXTRA_GALLERY_PROVIDER);
 				if(galleryName != null){
 					GalleryConfig galleryConfig = GalDroidPreference.getSetupByName(galleryName);
 					mWebGallery = GalleryFactory.createFromName(galleryConfig.TypeName, galleryConfig.RootLink, AndroidHttpClient.newInstance("GalDroid"));
 					mWebGallery.setSecurityToken(galleryConfig.SecurityToken);
 				}
 			} catch (NullPointerException e) {
 				mWebGallery = null;
 			}
 		}
 	}
 	
 	public void setWebGallery(WebGallery webGallery) {
 		mWebGallery = webGallery;
		mGalleryChildObjects = null;
		mStoredGalleryObject = null;
 	}
 	
 	public void storeGalleryObjects(GalleryObject parent, List<GalleryObject> childObjects) {
 		if(parent != null) {
 			mStoredGalleryObject = parent;
 			mGalleryChildObjects = childObjects;
 		} else {
 			parent = null;
 			childObjects = null;
 		}
 	}
 	
 	public List<GalleryObject> loadStoredGalleryObjects(GalleryObject parent) {
 		if(parent == null) {
 			return null;
 		}
 		
 		if(!parent.equals(mStoredGalleryObject)) {
 			return null;
 		}
 		
 		return mGalleryChildObjects;
 	}
 }
