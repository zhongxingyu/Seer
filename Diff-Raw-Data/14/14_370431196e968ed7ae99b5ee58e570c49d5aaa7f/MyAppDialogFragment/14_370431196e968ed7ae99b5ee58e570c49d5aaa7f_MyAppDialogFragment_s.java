 package jp.example.myapp.base;
 
 import jp.example.myapp.MyApp;
 
 import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragment;
 import com.googlecode.androidannotations.annotations.App;
 import com.googlecode.androidannotations.annotations.EFragment;
 
 @EFragment
 public class MyAppDialogFragment extends SherlockDialogFragment {
 	
 	@App
 	protected MyApp app;
 	
 }
