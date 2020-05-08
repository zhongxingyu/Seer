 package com.df.android.ui;
 
 import android.content.Context;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 
 import com.df.android.R;
 import com.df.android.entity.ItemCategory;
 import com.df.android.entity.Shop;
 import com.df.android.menu.MenuPagerAdapter;
 
 public class MenuView extends LinearLayout{
 	
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
 		LayoutInflater li = LayoutInflater.from(this.getContext());
 		
 		// Add menu navigator
 		final RadioGroup menuNavigator = (RadioGroup) li.inflate(R.layout.menunavigator,
 				this, false);
 		
 		for (ItemCategory category : shop.getNavigatableMenuItemCategories()) {
 			RadioButton rb = (RadioButton) li.inflate(R.layout.menunavigatorbutton,
 					menuNavigator, false);
 			rb.setText(getContext().getResources().getString(getContext().getResources().getIdentifier(getContext().getPackageName() + ":string/" + category.toString(), null, null)));
 			menuNavigator.addView(rb);
 		}
 
 		// Add menu pager
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
				if(menuNavigator.getCheckedRadioButtonId() != position + 1)
					menuNavigator.check(position + 1);
 			}
 			
 		});
 
 		menuNavigator.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			@Override
			public void onCheckedChanged(RadioGroup view, int position) {
				if(menuPager.getCurrentItem() != position - 1)
					menuPager.setCurrentItem(position - 1);
 			}
 
 		});
 		
 		
 		this.addView(menuNavigator);
 		this.addView(menuPager);
 		
		menuNavigator.check(1);
 	}
 }
