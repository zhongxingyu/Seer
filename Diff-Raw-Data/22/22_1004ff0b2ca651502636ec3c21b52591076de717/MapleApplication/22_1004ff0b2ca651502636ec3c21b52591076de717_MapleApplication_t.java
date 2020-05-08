 package com.example.maple_android;
 
 import java.util.ArrayList;
 
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
 
 import android.app.Application;
 import android.graphics.Bitmap;
 
 /**
  * This class exists to store application global variables
  * that are accessible from any context.
  * 
  * You can access this from any activity by calling
  * getApplication()
  * 
  * We should be careful with how much stuff we keep
  * here, and try to keep it very clean.
  *
  */
 
 public class MapleApplication extends Application{
 	/* The company that the current ad is tagged with */
 	private String companyTag = null;
 	/* Available logos for the current company */
 	private ArrayList<Bitmap> companyLogos;
 	
 	@Override
 	public void onCreate()
 	{
 		super.onCreate();
 
 		// Initialize the singletons so their instances
 		// are bound to the application process.
 		initUniversalImageLoader();
 	}
 	
 	/** Set what company the current ad is tagged with
 	 * 
 	 * @param companyTag The company name the current ad is tagged with
 	 * 
 	 */
 	public void setCurrentCompany(String companyTag){
 		// also start loading company logos from server
		if(companyTag != null) companyLogos = CompanyList.getCompanyLogosFromServer(companyTag);
 		
 		this.companyTag = companyTag;
 	}
 	
 	/** Get the currently tagged company
 	 * 
 	 * @return The name of the last tagged company
 	 * 
 	 */	
 	public String getCurrentCompany(){
 		return companyTag;
 	}
 	
 	/** Get available logos for the current company
 	 * 
 	 * @return All available logos for the currently tagged company
 	 */
 	public ArrayList<Bitmap> getCurrentCompanyLogos(){
 		return companyLogos;
 	}
 	
 	
 
 	/** Universal Image Loader is a library
 	 * that helps with loading images from the web.
 	 * It is asynchronous and multithreaded, and offers
 	 * caching (plus a bunch of other features). 
 	 * 
 	 * It needs to
 	 * be configured once, and then can be used by calling 
 	 * ImageLoader.getInstance()
 	 * 
 	 * Documentation and code at:
 	 * https://github.com/nostra13/Android-Universal-Image-Loader
 	 */
 	private void initUniversalImageLoader() {
 		// Create global configuration and initialize ImageLoader with this configuration
         ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
             // insert other configuration details here
             .build();
         ImageLoader.getInstance().init(config);
 		
 	}
 
 	
 
 	
 }
