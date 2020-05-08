 package ca.ualberta.CMPUT301F13T02.chooseyouradventure;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 public class ViewPageActivity extends Activity {
     
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.view_page_activity);
     }
 	
 	public void jumpEdit(View view) {
 		Intent intent = new Intent(this, EditStoryActivity.class);
 		startActivity(intent);
 	}
 	
 	@Override
 	public void onResume() {
 		//MyApplication app = (MyApplication) getApplication();
 		//displayPage(app);
 	}
 	
 	//private void displayPage(MyApplication app) {
 		//Page page = app.getPage();
 		//
 		//for (Tile tile : page.getTilesList()) {
 		//	
 		//}
 	//}
 }
