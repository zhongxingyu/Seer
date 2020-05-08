 package jp.example.myapp.base;
 
 import jp.example.myapp.MyApp;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.googlecode.androidannotations.annotations.App;
 import com.googlecode.androidannotations.annotations.EFragment;
 
 @EFragment
 public class MyAppListFragment extends SherlockListFragment {
 	
 	@App
 	protected MyApp app;
 	
 }
