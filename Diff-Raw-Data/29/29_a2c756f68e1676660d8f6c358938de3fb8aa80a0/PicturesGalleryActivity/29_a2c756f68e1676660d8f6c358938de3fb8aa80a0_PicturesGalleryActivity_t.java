 /*
  * Copyright (c) 2012 Roman Truba Permission is hereby granted, free of charge, to any person obtaining a copy of this
  * software and associated documentation files (the "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
  * Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The
  * above copyright notice and this permission notice shall be included in all copies or substantial portions of the
  * Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
  * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
  * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package com.corising.weddingalbum;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import ru.truba.touchgallery.GalleryWidget.BasePagerAdapter;
 import ru.truba.touchgallery.GalleryWidget.BasePagerAdapter.OnItemChangeListener;
 import ru.truba.touchgallery.GalleryWidget.GalleryViewPager;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.novoda.imageloader.core.ImageManager;
import com.novoda.imageloader.core.exception.ImageCopyException;
 import com.novoda.imageloader.core.file.FileManager;
import com.novoda.imageloader.core.file.util.FileUtil;
 
 public class PicturesGalleryActivity extends SherlockActivity implements OnItemChangeListener
 {
 
 	private static final String	URL_SPECIAL_SIZE	= "=s1024";
 	private GalleryViewPager	mViewPager;
 	private ArrayList<Picture>	pictures;
 	private Album				album;
	private static final String	title				= "%1$s(%2$s/%3$s)";
	private static final String	TAG					= PicturesGalleryActivity.class.getName();
 
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.album_pictures_gallery);
 
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 
 		album = getIntent().getParcelableExtra("album");
 		pictures = getIntent().getParcelableArrayListExtra("pictures");
 		int position = getIntent().getIntExtra("position", 0);
 		getSupportActionBar().setTitle(
 				String.format(title, album.getName(), String.valueOf(position + 1), String.valueOf(pictures.size())));
 
 		List<String> images = new ArrayList<String>();
 		for (Picture picture : pictures)
 		{
 			String url = picture.getUrl() + URL_SPECIAL_SIZE;
 			images.add(url);
 		}
 
 		BasePagerAdapter pagerAdapter = new LoaderImagePagerAdapter(this, images);
 		pagerAdapter.setOnItemChangeListener(this);
 
 		mViewPager = (GalleryViewPager) findViewById(R.id.viewer);
 		mViewPager.setOffscreenPageLimit(3);
 		mViewPager.setAdapter(pagerAdapter);
 		mViewPager.setCurrentItem(position);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		getSupportMenuInflater().inflate(R.menu.actions_gallery, menu);
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item)
 	{
 		FileManager pictureGalleryFileManager;
 		switch (item.getItemId())
 		{
 		case android.R.id.home:
 			onBackPressed();
 			return true;
 		case R.id.menu_share:
 			Intent share = new Intent(Intent.ACTION_SEND);
 			int position = mViewPager.getCurrentItem();
 			Picture picture = pictures.get(position);
 			ImageManager imageManager = AlbumApplication.getImageLoader();
 			pictureGalleryFileManager = imageManager.getFileManager();
 			File file = pictureGalleryFileManager.getFile(picture.getUrl() + URL_SPECIAL_SIZE);
			FileUtil fileUtil = new FileUtil();
			File sharedFile = new File(file.getParentFile(), picture.getName());
			try
			{
				fileUtil.copy(file, sharedFile);
				Log.i(TAG, "file name = " + sharedFile.getName());
				Log.i(TAG, "file = " + sharedFile.getAbsolutePath());
				share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(sharedFile));
				share.setType("image/*");
				startActivity(Intent.createChooser(share, getString(R.string.pictures_gallery_share_image)));
			}
			catch (ImageCopyException e)
			{
				Log.e(TAG, e.getMessage(), e);
			}
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onItemChange(int currentPosition)
 	{
 		getSupportActionBar().setTitle(
 				String.format(title, album.getName(), String.valueOf(currentPosition + 1),
 						String.valueOf(pictures.size())));
 	}
 
 }
