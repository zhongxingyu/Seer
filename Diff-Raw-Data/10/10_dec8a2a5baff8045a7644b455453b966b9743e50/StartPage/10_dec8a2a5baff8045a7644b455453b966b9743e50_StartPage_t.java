 package second.prototype;
 
 import item.Backpack;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.tang.DownLoadPage.DownLoadPageActivity;
 
 
 import control.appearance.BackgroundMusic;
 import control.appearance.DrawableIndex;
 import control.stage.Stage;
 import control.stage.StageManager;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class StartPage extends Activity {
 	/** Members */
 	private View startPage;
 	private View splash;
 	
 	private ListView stagesView;
 	private ArrayList<HashMap<String, String>> stageList = new ArrayList<HashMap<String, String>>();
 	private SimpleAdapter adapter;
 	
 	private int cursor = -1;
 	
 	private StageManager manager;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		LayoutInflater infla = LayoutInflater.from(this);
 		startPage = infla.inflate(R.layout.stagescreen, null);
 		splash = infla.inflate(R.layout.splash, null);
 		
 		waitSplash();
 		
 		ContainerBox.isTab = (Build.VERSION.SDK_INT > 10);
 		
 		stagesView = (ListView) startPage.findViewById(R.id.stagelist);
 		
 		manager = new StageManager(this);
 		if(manager.firstPlay()){
 			StageManager.initFileSettings(this);
 			manager = new StageManager(this);
 		}
 		
 		ContainerBox.stageManager = manager;
 		
 	}
 
 	public void onPause() {
 		super.onPause();
 		saveStageList();
 	}
 	
 	public void onResume() {
 		super.onResume();
 		reBuildStageList();
 	}
 	
 	@Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         BackgroundMusic.stop();
 	}
 	
 	/** Utilities */
 	private void reBuildStageList() {
 		
 		stageList.clear();
 		
 		for(int i=0;i<manager.numOfStages();i++){
 			Stage stage = manager.getStage(i);
 			
 			HashMap<String,String> item = new HashMap<String,String>();
 			item.put("Name", stage.getName());
 			item.put("Description", stage.getDescription());
 			stageList.add(item);
 		}
 		
 		adapter = new SimpleAdapter(this, stageList,
 				R.layout.stageitem, new String[] { "Name",
 						"Description" }, new int[] { R.id.Name,
 						R.id.Description });
 
 		stagesView.setAdapter(adapter);
 
 		stagesView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// TODO Auto-generated method stub
 				cursor = arg2;
 				StartPage.this.setTitle("Selected Stage "
 						+ stageList.get(cursor).get("Name"));
 			}
 
 		});
 		
 	}
 
 	private void saveStageList() {
 		// store list
 		manager.commit();
 		stageList.clear();
 		
 	}
 	
 	private void waitSplash(){
 		setContentView(splash);
 		Thread thread = new Thread(){
     		@Override
     		public void run(){
     			try {
 					sleep(2000);
 					StartPage.this.runOnUiThread(new Runnable(){
 
 						@Override
 						public void run() {
 							// TODO Auto-generated method stub
 							splash.startAnimation(AnimationUtils.loadAnimation(StartPage.this,android.R.anim.fade_out));
 							startPage.startAnimation(AnimationUtils.loadAnimation(StartPage.this,android.R.anim.fade_in));
 							setContentView(startPage);
 							
 						}
 						
 					});
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
     		}
     	};
     	
     	thread.start();
 	}
 	
 	/** Menu Control 
 	 *  These are Programmer tasks ...*/
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, 0, 0, "Restore Deault Data").setIcon(android.R.drawable.ic_menu_upload);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		switch(item.getItemId()){
 		case 0:
 			StageManager.initFileSettings(this);
 			manager = new StageManager(this);
 			ContainerBox.stageManager = manager;
 			reBuildStageList();
 			break;
 		default :		
 		}
 		return true;
 	}
 	
 	/** Button onClick listeners */
 	public void playClicked(View view) {
 		if (cursor < 0) {
 			Toast.makeText(this, "What Stage to play ?", Toast.LENGTH_SHORT)
 					.show();
 		} else {
 			Stage stage = manager.getStage(cursor);
 			Backpack backpack = new Backpack(manager.getFileName(cursor),this,stage.getItemList());
 			
 			ContainerBox.currentStage = stage;
 			ContainerBox.backback = backpack;
 			
 			Intent playStage = new Intent();
 			playStage.setClass(this, MapMode.class);
 			
 			int which = (int)(Math.random()*DrawableIndex.TOTAL);
 			
 			DrawableIndex.setDrawables(which);
 			BackgroundMusic.setBGM(which);
 			BackgroundMusic.init(this);
 			
 			startActivityForResult(playStage,0);
 		}
 		cursor = -1;
 	}
 
 	public void addClicked(View view) {
 		Intent net = new Intent();
 		net.setClass(this, DownLoadPageActivity.class);
 		startActivity(net);
 		cursor = -1;
 	}
 
 	public void deleteClicked(View view) {
 		if (cursor < 0) {
 			Toast.makeText(this, "Select a stage first", Toast.LENGTH_SHORT)
 					.show();
 		} else {
 			manager.deleteStage(cursor);
 			reBuildStageList();
 		}
 		cursor = -1;
 	}
 
 }
