 package com.df.android.ui;
 
 import android.content.Context;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 
 import com.df.android.R;
 import com.df.android.entity.ItemCategory;
 import com.df.android.entity.Shop;
 import com.df.android.menu.MenuPagerAdapter;
 
 public class MenuView extends LinearLayout {
 	private RadioGroup menuNavigator = null;
 	private ViewPager menuPager = null;
 
 	public MenuView(Context context) {
 		super(context);
 	}
 
 	public MenuView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	private Shop shop;
 
 	public void setShop(final Shop shop) {
 		this.shop = shop;
 		loadMenu();
 	}
 
 	private void loadMenu() {
 		Log.d(getClass().getName(), "Refreshing shop menu");
 		
 		LayoutInflater li = LayoutInflater.from(this.getContext());
 
 		// Add menu navigator
<<<<<<< HEAD
 		if (menuNavigator == null) {
 			menuNavigator = (RadioGroup) li.inflate(R.layout.menunavigator,
 					this, false);
 			this.addView(menuNavigator);
 		} else {
 			menuNavigator.removeAllViews();
 		}
 
 		for (ItemCategory category : shop.getNavigatableMenuItemCategories()) {
			RadioButton rb = (RadioButton) li.inflate(
					R.layout.menunavigatorbutton, menuNavigator, false);
 			rb.setText(getContext().getResources().getString(
 					getContext().getResources().getIdentifier(
 							getContext().getPackageName() + ":string/"
 									+ category.toString(), null, null)));
=======
		final RadioGroup menuNavigator = (RadioGroup) li.inflate(R.layout.menunavigator,
				this, false);
 		
 		int i = 0;
 		for (ItemCategory category : shop.getNavigatableMenuItemCategories()) {
 			RadioButton rb = (RadioButton) li.inflate(R.layout.menunavigatorbutton,
 					menuNavigator, false);
 			rb.setId(i++);
 			rb.setText(getContext().getResources().getString(getContext().getResources().getIdentifier(getContext().getPackageName() + ":string/" + category.toString(), null, null)));
>>>>>>> cb516d741921d0894435723b2b4d2c15d1869182
 			menuNavigator.addView(rb);
 		}
 
 		// Add menu pager
<<<<<<< HEAD
 		if (menuPager == null) {
 			menuPager = (ViewPager) li.inflate(R.layout.menupager, this, false);
 			MenuPagerAdapter menuPagerAdapter = new MenuPagerAdapter(
 					this.getContext(), shop);
 			menuPager.setAdapter(menuPagerAdapter);
 			this.addView(menuPager);
 			menuPager.setOnPageChangeListener(new OnPageChangeListener() {
 
 				@Override
 				public void onPageScrollStateChanged(int position) {
 
 				}
 
 				@Override
 				public void onPageScrolled(int arg0, float arg1, int arg2) {
 
 				}
 
 				@Override
 				public void onPageSelected(int position) {
 					if (menuNavigator.getCheckedRadioButtonId() != position + 1)
 						menuNavigator.check(position + 1);
 				}
 
 			});
 
 			menuNavigator
 					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 						@Override
 						public void onCheckedChanged(RadioGroup view,
 								int position) {
 							if (menuPager.getCurrentItem() != position - 1)
 								menuPager.setCurrentItem(position - 1);
 						}
 
 					});
 
 			menuNavigator.check(1);
 		}
=======
 		final ViewPager menuPager = (ViewPager) li.inflate(R.layout.menupager,
 				this, false);
 		MenuPagerAdapter menuPagerAdapter = new MenuPagerAdapter(this.getContext(), shop);
 		menuPager.setAdapter(menuPagerAdapter);
 
 		menuPager.setOnPageChangeListener(new OnPageChangeListener() {
 
 			@Override
 			public void onPageScrollStateChanged(int position) {
 				
 			}
 
 			@Override
 			public void onPageScrolled(int arg0, float arg1, int arg2) {
 				
 			}
 
 			@Override
 			public void onPageSelected(int position) {
 				if(menuNavigator.getCheckedRadioButtonId() != position)
 					menuNavigator.check(position);
 			}
 			
 		});
 
 		menuNavigator.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(RadioGroup view, int checkedId) {
 				if(menuPager.getCurrentItem() != checkedId)
 					menuPager.setCurrentItem(checkedId);
 			}
 
 		});
 		
 		
 		this.addView(menuNavigator);
 		this.addView(menuPager);
 		
 		menuNavigator.check(0);
>>>>>>> cb516d741921d0894435723b2b4d2c15d1869182
 	}

 }
