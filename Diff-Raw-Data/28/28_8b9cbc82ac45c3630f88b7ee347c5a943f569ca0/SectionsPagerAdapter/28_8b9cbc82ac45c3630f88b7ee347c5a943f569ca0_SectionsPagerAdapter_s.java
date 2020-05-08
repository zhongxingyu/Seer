 package com.mick88.superbrain.main;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 
 import com.mick88.superbrain.quizzes.QuizManager;
 
 /**
  * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
  * of the sections/tabs/pages.
  */
 public class SectionsPagerAdapter extends FragmentPagerAdapter
 {
 	String [] categories;
 	final QuizManager quizManager;
 
 	public SectionsPagerAdapter(FragmentManager fm, QuizManager quizManager) {
 		super(fm);
 		this.quizManager = quizManager;
 		categories = quizManager.getCategories();
 	}
 
 	@Override
 	public Fragment getItem(int position)
 	{
 		String category = categories[position];
 		
 		Bundle args = new Bundle();
 		args.putString(SectionFragment.EXTRA_CATEGORY, category);
 		SectionFragment section = new SectionFragment();
 		section.setArguments(args);
		section.setCategoryName(category);
 		
 		return section;
 	}
 
 
 	@Override
 	public int getCount()
 	{
 		if (categories == null) return 0;
 		else return categories.length;
 	}
 	
 	@Override
 	public int getItemPosition(Object object)
 	{
 		// Hack for refreshing pages
 		return POSITION_NONE;
 	}
 	
 	@Override
 	public void notifyDataSetChanged()
 	{
 		categories = quizManager.getCategories();
 		super.notifyDataSetChanged();
 	}
 
 	@Override
 	public CharSequence getPageTitle(int position)
 	{
 		return categories[position];
 	}
 }
