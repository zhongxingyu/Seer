 package com.wxk.jokeandroidapp.ui;
 
 import java.util.List;
 
 import com.wxk.jokeandroidapp.Constant;
 import com.wxk.jokeandroidapp.R;
 import com.wxk.jokeandroidapp.bean.JokeBean;
 import com.wxk.jokeandroidapp.db.JokeDb;
 import com.wxk.jokeandroidapp.ui.util.ImageCache;
 import com.wxk.jokeandroidapp.ui.util.ImageFetcher;
 import com.wxk.util.GsonUtils;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.DisplayMetrics;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class DetailActivity extends BaseActivity implements OnClickListener {
 
 	private ViewPager mPager;
 	private JokePagerAdapter mAdapter;
 	public static final String EXTRA_JOKE_ID = "extra_joke_id";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.joke_detail_pager);
 		//
 		final DisplayMetrics displayMetrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
 		final int height = displayMetrics.heightPixels;
 		final int width = displayMetrics.widthPixels;
 
		// final int longest = (height > width ? height : width);// / 2;
 
 		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(
 				this, Constant.IMAGE_CACHE_DIR);
 		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
 													// app memory
 		//
		mImageFetcher = new ImageFetcher(this, width, height);
 		mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
 		mImageFetcher.setImageFadeIn(false);
 		mImageFetcher.setFullImageShow(true);
 		//
 		mAdapter = new JokePagerAdapter(getSupportFragmentManager());
 		mPager = (ViewPager) findViewById(R.id.pager);
 		mPager.setAdapter(mAdapter);
 		mPager.setOffscreenPageLimit(2);
 
 		// final int extraCurrentItem = getIntent().getIntExtra(EXTRA_JOKE, -1);
 		final int extraCurrentItem = (int) db
 				.getCount(
 						"id>?",
 						new String[] { ""
 								+ getIntent().getIntExtra(EXTRA_JOKE_ID, -1) });
 		if (extraCurrentItem != -1) {
 			mPager.setCurrentItem(extraCurrentItem);
 		}
 	}
 
 	private JokeDb db = new JokeDb();
 
 	private class JokePagerAdapter extends FragmentStatePagerAdapter {
 		public JokePagerAdapter(FragmentManager fm) {
 			super(fm);
 			mSize = (int) db.getCount();
 		}
 
 		private final int mSize;
 
 		@Override
 		public int getCount() {
 			return mSize;
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			JokeBean jokeBean = null;
 			List<JokeBean> list = db.getList(position + 1, 1);
 			jokeBean = list.get(0);
 			return JokeDetailFragment.newInstance(GsonUtils.toJson(jokeBean));
 		}
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public ImageFetcher getImageFetcher() {
 		return mImageFetcher;
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		mImageFetcher.setExitTasksEarly(false);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		mImageFetcher.setExitTasksEarly(true);
 		mImageFetcher.flushCache();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		mImageFetcher.closeCache();
 	}
 }
