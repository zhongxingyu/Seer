 /**
  *
  * @author David Findley (ThinksInBits)
  * 
  * The source for this application may be found in its entirety at 
  * https://github.com/ThinksInBits/OU-Mobile-App
  * 
  * This application is published on the Google Play Store under
  * the title: OU Mobile Alpha:
  * https://play.google.com/store/apps/details?id=com.geared.ou
  * 
  * If you want to follow the official development of this application
  * then check out my Trello board for the project at:
  * https://trello.com/board/ou-app/4f1f697a28390abb75008a97
  * 
  * Please email me at: thefindley@gmail.com with questions.
  * 
  */
 
 package com.geared.ou;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.slidingmenu.lib.SlidingMenu;
 import com.slidingmenu.lib.app.SlidingFragmentActivity;
 
 /**
  *
  * This is a top level activity that displays News that is read from oudaily.com
  * to the user. Currently this is just a framework, and none of the real
  * functionality has been implemented.
  * 
  */
 public class NewsActivity extends SlidingFragmentActivity {
 	
     private FragmentManager fragmentManager;
     private OUApplication app;
     
     private TextView whoAmI;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         app = (OUApplication)getApplication();
         
         setContentView(R.layout.news);
         setBehindLeftContentView(R.layout.side_nav);
         
         /* SideMenu User Button. */
         whoAmI = (TextView)findViewById(R.id.whoAmI);
         if (!app.getUser().isEmpty())
         {
         	whoAmI.setText(getResources().getString(R.string.loggedInAsText)+" "+app.getUser());
         }
         else
         {
         	whoAmI.setText(R.string.loginButtonText);
         }
         
         SlidingMenu sm = getSlidingMenu();
         sm.setBehindWidth(350, SlidingMenu.LEFT);
         
         fragmentManager = getSupportFragmentManager();
         
         if (icicle == null)
         {
         	switch(app.getCurrentFragment())
         	{
         		case OUApplication.FRAGMENT_NEWS:
         			NewsFragment newsFragment = new NewsFragment();
         			FragmentTransaction fragNewsTrans = fragmentManager.beginTransaction();
         			fragNewsTrans.add(R.id.top_level_container, newsFragment, "main_fragment");
         			fragNewsTrans.commit();
         			break;
         		case OUApplication.FRAGMENT_CLASSES:
         			ClassesFragment classesFragment = new ClassesFragment();
         			FragmentTransaction fragClassesTrans = fragmentManager.beginTransaction();
         			fragClassesTrans.add(R.id.top_level_container, classesFragment, "main_fragment");
         			fragClassesTrans.commit();
         			break;
         		case OUApplication.FRAGMENT_CLASS:
         			ClassHomeFragment classHomeFragment = new ClassHomeFragment();
         			FragmentTransaction fragClassTrans = fragmentManager.beginTransaction();
         			fragClassTrans.add(R.id.top_level_container, classHomeFragment, "main_fragment");
         			fragClassTrans.commit();
         		case OUApplication.FRAGMENT_CONTENT:
         			ContentFragment contentFragment = new ContentFragment();
         			FragmentTransaction fragContentTrans = fragmentManager.beginTransaction();
         			fragContentTrans.add(R.id.top_level_container, contentFragment, "main_fragment");
         			fragContentTrans.commit();
         		case OUApplication.FRAGMENT_PREFS:
         			PrefsFragment prefsFragment = new PrefsFragment();
         			FragmentTransaction fragPrefsTrans = fragmentManager.beginTransaction();
         			fragPrefsTrans.add(R.id.top_level_container, prefsFragment, "main_fragment");
         			fragPrefsTrans.commit();
         			break;
         		case OUApplication.FRAGMENT_ABOUT:
         			AboutFragment aboutFragment = new AboutFragment();
         			FragmentTransaction fragAboutTrans = fragmentManager.beginTransaction();
         			fragAboutTrans.add(R.id.top_level_container, aboutFragment, "main_fragment");
         			fragAboutTrans.commit();
         			break;
     			default:
     				NewsFragment newsFragmentD = new NewsFragment();
     				FragmentTransaction fragDefaultTrans = fragmentManager.beginTransaction();
     				fragDefaultTrans.add(R.id.top_level_container, newsFragmentD, "main_fragment");
     				fragDefaultTrans.commit();
     				break;
         	}
         }
     }
     
     @Override
 	public void onBackPressed() {
     	int f = app.getCurrentFragment();
 		if (f == OUApplication.FRAGMENT_CLASS || f == OUApplication.FRAGMENT_CONTENT || f == OUApplication.FRAGMENT_GRADES || f == OUApplication.FRAGMENT_ROSTER)
 		{
 			ClassesFragment classesFragment = new ClassesFragment();
 			FragmentTransaction fragClassesTrans = fragmentManager.beginTransaction();
 			fragClassesTrans.replace(R.id.top_level_container, classesFragment, "main_fragment");
 			fragClassesTrans.commit();
 		}
 		else if (f == OUApplication.FRAGMENT_PREFS || f == OUApplication.FRAGMENT_ABOUT)
 		{
 			NewsFragment newsFragment = new NewsFragment();
 			FragmentTransaction fragNewsTrans = fragmentManager.beginTransaction();
 			fragNewsTrans.replace(R.id.top_level_container, newsFragment, "main_fragment");
 			fragNewsTrans.commit();
 		}
 		else
 			super.onBackPressed();
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
         return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()) {
         	case android.R.id.home:
         		toggle(SlidingMenu.LEFT);
         		break;
         	case R.id.itemPrefs:
         		PrefsFragment prefsFragment = new PrefsFragment();
     			FragmentTransaction fragPrefsTrans = fragmentManager.beginTransaction();
     			fragPrefsTrans.replace(R.id.top_level_container, prefsFragment, "main_fragment");
     			fragPrefsTrans.commit();
             default:
             	break;
         }
         return super.onOptionsItemSelected(item);
     }
     
     public void sideNavItemSelected(View v)
     {
     	/* Update the sidebar. */
     	if (app.getUser().isEmpty())
     		whoAmI.setText(R.string.loginButtonText);
     	else
     		whoAmI.setText(getResources().getString(R.string.loggedInAsText)+" "+app.getUser());
     	
     	/* Switch over view ID. */
     	switch(v.getId())
     	{
     		case R.id.news_button:
     			NewsFragment newsFragment = new NewsFragment();
     			FragmentTransaction fragNewsTrans = fragmentManager.beginTransaction();
     			fragNewsTrans.replace(R.id.top_level_container, newsFragment, "main_fragment");
     			fragNewsTrans.commit();
     			break;
     		case R.id.classes_button:
     			ClassesFragment classesFragment = new ClassesFragment();
     			FragmentTransaction fragClassesTrans = fragmentManager.beginTransaction();
     			fragClassesTrans.replace(R.id.top_level_container, classesFragment, "main_fragment");
     			fragClassesTrans.commit();
     			break;
     		case R.id.map_button:
     			startActivity(new Intent(this, CampusMapActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
     			break;
 			default:
 				break;
     	}
     	toggle(SlidingMenu.LEFT);
     }
 
     public void userSideMenuButton(View v)
     {
     	if (app.getUser().isEmpty())
     	{
     		PrefsFragment prefsFragment = new PrefsFragment();
 			FragmentTransaction fragPrefsTrans = fragmentManager.beginTransaction();
 			fragPrefsTrans.replace(R.id.top_level_container, prefsFragment, "main_fragment");
 			fragPrefsTrans.commit();
 			toggle(SlidingMenu.LEFT);
     	}
     	else
     	{
     		showDialog();
     	}
     }
     
     public void aboutButton(View v)
     {
     	AboutFragment aboutFragment = new AboutFragment();
 		FragmentTransaction fragAboutTrans = fragmentManager.beginTransaction();
 		fragAboutTrans.replace(R.id.top_level_container, aboutFragment, "main_fragment");
 		fragAboutTrans.commit();
 		toggle(SlidingMenu.LEFT);
     }
     
     public void goToClassesFragment(View v)
     {
         app.getClasses().forceNextUpdate();
         ClassesFragment classesFragment = new ClassesFragment();
 		FragmentTransaction fragClassesTrans = fragmentManager.beginTransaction();
 		fragClassesTrans.replace(R.id.top_level_container, classesFragment, "main_fragment");
 		fragClassesTrans.commit();
 		login();
     }
     
     public void logout()
     {
     	whoAmI.setText(R.string.loginButtonText);
     }
     
     public void login()
     {
 		whoAmI.setText(getResources().getString(R.string.loggedInAsText)+" "+app.getUser());
     }
     
     private void showDialog() {
         // DialogFragment.show() will take care of adding the fragment
         // in a transaction.  We also want to remove any currently showing
         // dialog, so make our own transaction and take care of that here.
         FragmentTransaction ft = fragmentManager.beginTransaction();
         Fragment prev = fragmentManager.findFragmentByTag("dialog");
         if (prev != null) {
             ft.remove(prev);
         }
         ft.addToBackStack(null);
 
         // Create and show the dialog.
         LoginDialogFragment newFragment = new LoginDialogFragment();
         newFragment.show(ft, "dialog");
     }
 
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		
 	}
 }
