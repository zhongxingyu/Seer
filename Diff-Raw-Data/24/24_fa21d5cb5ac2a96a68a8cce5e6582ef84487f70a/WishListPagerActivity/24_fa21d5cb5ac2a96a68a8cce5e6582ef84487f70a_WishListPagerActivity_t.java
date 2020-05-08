 package com.salemethis.app;
 
 import java.util.ArrayList;
 import java.util.UUID;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 
 import com.salemethis.app.dummy_data.DummyItem;
 import com.salemethis.app.dummy_data.DummyItemGenerator;
 import com.salemethis.app.product.ProductFragment;
 
 public class WishListPagerActivity extends FragmentActivity {
 
 	private ViewPager mViewPager;
 	private ArrayList<DummyItem> mItems;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mViewPager = new ViewPager(this);
 		mViewPager.setId(R.id.viewPager);
 		setContentView(mViewPager);
 		
 		mItems = DummyItemGenerator.get(this).getDummyItems();
 		
 		FragmentManager fm = getSupportFragmentManager();
 		mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
 
 			@Override
 			public Fragment getItem(int pos) {
 				DummyItem item = mItems.get(pos);
 				
 				return ProductFragment.newInstance(item.getId());
 			}
 
 			@Override
 			public int getCount() {
 				return mItems.size();
 			}
 		});
 		
 		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
 
 			@Override
 			public void onPageScrollStateChanged(int arg0) {}
 
 			@Override
 			public void onPageScrolled(int arg0, float arg1, int arg2) {}
 
 			@Override
 			public void onPageSelected(int pos) {
 				DummyItem item = mItems.get(pos);
 				if (item != null) {
 					setTitle(item.getName());
 				}
 			}
 		});
		
		UUID itemId = (UUID)getIntent().getSerializableExtra(ProductFragment.EXTRA_ITEM_ID);
		for (int i = 0; i < mItems.size(); i++) {
			if (mItems.get(i).getId().equals(itemId)) {
				mViewPager.setCurrentItem(i);
				break;
			}
		}
 	}
 }
