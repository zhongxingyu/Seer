 package com.mitrejcevski.widget.adapter;
 
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 
 import com.mitrejcevski.widget.fragment.TaskListFragment;
 import com.mitrejcevski.widget.model.Group;
 
 import java.util.ArrayList;
 
 /**
  * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding to
  * one of the primary sections of the application.
  */
 public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
 
 	private ArrayList<Group> mGroups = new ArrayList<Group>();
 	private final ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
 
 	/**
 	 * Set groups to the adapter.
 	 * 
 	 * @param groups
 	 */
 	public void setGroups(ArrayList<Group> groups) {
 		mGroups = groups;
 		notifyDataSetChanged();
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param fragmentManager
 	 */
 	public SectionsPagerAdapter(FragmentManager fragmentManager) {
 		super(fragmentManager);
 	}
 
 	@Override
 	public Fragment getItem(int i) {
 		Fragment fragment = new TaskListFragment();
 		((TaskListFragment) fragment).setCurrentGroup(mGroups.get(i));
 		mFragments.add(fragment);
 		return fragment;
 	}
 
 	/**
 	 * Returns the fragment for specific position.
 	 * 
 	 * @param position
 	 * @return
 	 */
 	public Fragment getFragmentItem(int position) {
 		return mFragments.get(position);
 	}
 
 	/**
 	 * Returns a count of the fragments.
 	 * 
 	 * @return
 	 */
 	public int getFragmentsSize() {
 		return mFragments.size();
 	}
 
 	@Override
 	public int getCount() {
 		return mGroups.size();
 	}
 
 	/**
 	 * Get the group at position.
 	 * 
 	 * @param postition
 	 * @return
 	 */
 	public Group getGroupItem(int postition) {
 		return mGroups.get(postition);
 	}
 
 	@Override
 	public CharSequence getPageTitle(int position) {
 		return mGroups.get(position).getGroupTitle().toUpperCase();
 	}
 }
