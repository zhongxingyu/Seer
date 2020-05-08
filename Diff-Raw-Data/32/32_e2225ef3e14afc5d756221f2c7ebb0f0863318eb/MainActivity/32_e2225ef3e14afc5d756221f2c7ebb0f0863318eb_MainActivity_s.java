 package com.hyperactivity.android_app.activities;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;

 import com.hyperactivity.android_app.R;
 import com.hyperactivity.android_app.core.Engine;
 import com.hyperactivity.android_app.forum.ForumEventCallback;
 import com.hyperactivity.android_app.forum.models.Thread;
 
 public class MainActivity extends FragmentActivity implements ForumEventCallback {
     public static final int HOME_FRAGMENT = 0,
             				FORUM_FRAGMENT = 1,
         					DIARY_FRAGMENT = 2,
         					CREATE_THREAD_FRAGMENT = 3,
         					SEARCH_FRAGMENT = 4,
         					VIEW_THREAD_FRAGMENT = 5;
     		
 
     private Fragment[] fragments;
     private int currentFragment;
     private NavigationMenuFragment navigationMenu;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         // Make a link from the navigation menu to this activity
         navigationMenu = (NavigationMenuFragment) getSupportFragmentManager()
                 .findFragmentById(R.id.navigation_menu_fragment);
         navigationMenu.setParentActivity(this);
         
         initializeFragments();
         currentFragment = -1;
         changeFragment(HOME_FRAGMENT);
 
         Engine engine = (Engine) getApplication();
         engine.getPublicForum().setCallback(this);
         engine.getPublicForum().loadCategories(this, false);
     }
 
     private void initializeFragments() {
         fragments = new Fragment[6];
         fragments[HOME_FRAGMENT] = new HomeFragment();
         fragments[FORUM_FRAGMENT] = new ForumFragment();
         fragments[DIARY_FRAGMENT] = new DiaryFragment();
         fragments[CREATE_THREAD_FRAGMENT] = new CreateThreadFragment();
         fragments[SEARCH_FRAGMENT] = new SearchFragment();
         fragments[VIEW_THREAD_FRAGMENT] = new ViewThreadFragment();
     }
     
     public void visitThread(Thread thread) {
     	((ViewThreadFragment)fragments[VIEW_THREAD_FRAGMENT]).setCurrentThread(thread);
     	changeFragment(VIEW_THREAD_FRAGMENT);
     }
 
     public void changeFragment(int fragmentID) {
         if (currentFragment != fragmentID) {
             if(currentFragment == FORUM_FRAGMENT) {
                 ((ForumFragment)fragments[FORUM_FRAGMENT]).test();
             }
             currentFragment = fragmentID;
             FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
             transaction.replace(R.id.main_fragment_container, fragments[fragmentID]);
             transaction.commit();
             navigationMenu.updateNavigationMenu(currentFragment);
         }
     }
 
     @Override
     public void loadingStarted() {
     }
 
     @Override
     public void loadingFailed() {
     }
 
     /**
      * Called when forum have finished loading.
      */
     @Override
     public void loadingFinished() {
     }
 
     @Override
     public void categoriesLoaded() {
 		((ForumFragment)fragments[FORUM_FRAGMENT]).updateCategories();
     }
 
     @Override
     public void threadsLoaded() {
     	if (currentFragment == FORUM_FRAGMENT) {
             ((ForumFragment)fragments[FORUM_FRAGMENT]).updateThreadList();
     	} else if (currentFragment == HOME_FRAGMENT) {
     		// Get latest threads
             ((HomeFragment)fragments[HOME_FRAGMENT]).updateThreadList();
     	}
     }
 
     @Override
     public void repliesLoaded() {
     }
 }
