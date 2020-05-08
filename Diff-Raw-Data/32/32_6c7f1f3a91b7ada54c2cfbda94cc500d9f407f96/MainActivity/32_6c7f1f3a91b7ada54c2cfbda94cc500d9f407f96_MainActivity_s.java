 package com.levelup.picturecache.samples;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageView;
 
 import com.levelup.picturecache.LifeSpan;
 import com.levelup.picturecache.StorageType;
import com.levelup.picturecache.loaders.ImageViewLoader;
 import com.levelup.picturecache.loaders.ImageViewLoaderDefaultDrawable;
 import com.levelup.picturecache.loaders.ImageViewLoaderDefaultResource;
 
 public class MainActivity extends Activity {
 	
 	private MyPictureCache mCache;
 
 	static final String levelupAvatarURL = "https://si0.twimg.com/profile_images/1584807085/LevelUp-Logo-Avatarv5.png"; // '_normal' / '_bigger' / ''
 	static final String plumeAvatarURL = "https://si0.twimg.com/profile_images/2794557097/a073ce673f72cb8fc1aa189ad7f28fd6.png"; // '_normal' / '_bigger' / ''
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		final int screenDpi = getResources().getDisplayMetrics().densityDpi;
 		
 		mCache = MyPictureCache.getInstance(this);
 
 		/*
 		 * the most basic image loading call
 		 */
 		findViewById(R.id.loadAvatar1).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				ImageView avatar = (ImageView) findViewById(R.id.avatar1);
				ImageViewLoader loader = new ImageViewLoaderDefaultDrawable(avatar, null, null, null);
				mCache.loadPictureWithFixedHeight(loader, levelupAvatarURL, "twitter_levelup", 0,
 						LifeSpan.LONGTERM,
 						(48*screenDpi)/160,
 						StorageType.AUTO);
 
 			}
 		});
 		/*
 		 * unload the picture by loading the null URL
 		 * when the URL is null, a UUID is necessary
 		 */
 		findViewById(R.id.resetAvatar1).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				ImageView avatar = (ImageView) findViewById(R.id.avatar1);
				ImageViewLoader loader = new ImageViewLoaderDefaultDrawable(avatar, null, null, null);
				mCache.loadPictureWithFixedHeight(loader, null, "empty", 0, LifeSpan.LONGTERM, (48*screenDpi)/160, StorageType.AUTO);
 			}
 		});
 
 		/*
 		 * image loading with a custom PictureLoaderHandler
 		 */
 		findViewById(R.id.loadAvatar2).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				ImageView avatar = (ImageView) findViewById(R.id.avatar2);
 				ImageViewLoaderDefaultResource loader = new ImageViewLoaderDefaultResource(avatar, R.drawable.picholder, null, null);
				mCache.loadPictureWithFixedHeight(loader, plumeAvatarURL, "twitter_plume", 0,
 						LifeSpan.LONGTERM,
 						(96*screenDpi)/160,
 						StorageType.AUTO);
 
 			}
 		});
 		/*
 		 * unload the picture by loading the null URL, the default view will be displayed (ie R.drawable.picholder)
 		 * when the URL is null, a UUID is necessary
 		 */
 		findViewById(R.id.resetAvatar2).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				ImageView avatar = (ImageView) findViewById(R.id.avatar2);
 				ImageViewLoaderDefaultResource loader = new ImageViewLoaderDefaultResource(avatar, R.drawable.picholder, null, null);
				mCache.loadPictureWithFixedHeight(loader, null, "empty", 0, LifeSpan.LONGTERM, (96*screenDpi)/160, StorageType.AUTO);
 			}
 		});
 
 		/*
 		 * clear the data in the cache
 		 */
 		findViewById(R.id.clearCache).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				mCache.clear();
 			}
 		});
 
 		/*
 		 * launch the list view sample
 		 */
 		findViewById(R.id.listViewSample).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				startActivity(new Intent(getApplicationContext(), ListViewSample.class));
 			}
 		});
 	}
 }
