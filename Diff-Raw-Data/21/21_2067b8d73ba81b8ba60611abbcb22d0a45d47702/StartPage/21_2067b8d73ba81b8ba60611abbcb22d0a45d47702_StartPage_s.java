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
 import android.app.ProgressDialog;
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
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class StartPage extends Activity {
 	/** Members */
 	private View startPage;
 	private View splash;
 	
 	private ListView stagesView;
 	private ArrayList<HashMap<String, Object>> stageList = new ArrayList<HashMap<String, Object>>();
 	private SimpleAdapter adapter;
 	
 	private int cursor = -1;
 	private ProgressDialog progressDialog;
 	
 	private StageManager manager;
 	
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		
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
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle("Hello");
 			builder.setIcon(android.R.drawable.ic_dialog_info);
 			builder.setMessage("This is your first play,\nCheck out the introduction !");
 			
 			builder.setPositiveButton("Help", new DialogInterface.OnClickListener(){
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					Intent help = new Intent();
 					help.setClass(StartPage.this, HelpPage.class);
 					startActivity(help);
 				}
 				
 			});
 			
 			builder.setCancelable(false);
 			builder.show();
 			
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
 		progressDialog = ProgressDialog.show(StartPage.this, "Loading....", "Please Wait", true, false);
 		
 		Thread build = new Thread(){
 			@Override
 			public void run(){
				stageList.clear();
 				
 				for(int i=0;i<manager.numOfStages();i++){
 					HashMap<String,Object> item = new HashMap<String,Object>();
 					item.put("Name", manager.getName(i));
 					item.put("Description", manager.getDescription(i));
 					item.put("Icon", (manager.getIfClear(i)?R.drawable.spir2:R.drawable.spir1));
 					stageList.add(item);
 				}
				
 				StartPage.this.runOnUiThread(new Runnable(){
 
 					@Override
 					public void run() {
 						// TODO Auto-generated method stub
 						adapter = new SimpleAdapter(StartPage.this, stageList,
 								R.layout.stageitem, new String[] { "Name",
 										"Description", "Icon" }, new int[] { R.id.Name,
 										R.id.Description, R.id.claerIcon });
 
 						stagesView.setAdapter(adapter);

 						stagesView.setOnItemClickListener(new OnItemClickListener() {
 
 							@Override
 							public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 									long arg3) {
 								// TODO Auto-generated method stub
 								cursor = arg2;
 								AlertDialog.Builder builder = new AlertDialog.Builder(StartPage.this);
 								builder.setTitle(stageList.get(cursor).get("Name")+"");
 								builder.setIcon(android.R.drawable.ic_dialog_info);
 								builder.setMessage(stageList.get(cursor).get("Description")+"");
 								
 								builder.setPositiveButton("Play", new DialogInterface.OnClickListener(){
 
 									@Override
 									public void onClick(DialogInterface dialog, int which) {
 										play();
 									}
 									
 								});
 								if(!manager.getFileName(cursor).startsWith("000")){
									builder.setNegativeButton("Delete", new DialogInterface.OnClickListener(){
 									
 										@Override
 										public void onClick(DialogInterface dialog, int which) {
 											delete();
 										}
 										
 									});
 								}
								builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener(){
 
 									@Override
 									public void onClick(DialogInterface dialog, int which) {
 										
 									}
 									
 								});
 								
 								builder.show();
 							}
 
 						});
 						
 						progressDialog.dismiss();
 					}
 					
 				});
 			}
 		};
 		build.start();
 		
 	}
 
 	private void saveStageList() {
 		// store list
 		manager.commit();
 		stageList.clear();
 		
 	}
 	
 	private void waitSplash(){
 		setContentView(splash);
 		Log.e("splash","splash put to screen");
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
 							Log.e("splash","stage list loaded");
 							
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
 	
 	
 	private void play() {
 		
 			Thread load = new Thread(){
 				 @Override
 			     public void run(){
 					 StartPage.this.runOnUiThread(new Runnable(){
 
 							@Override
 							public void run() {
 								// TODO Auto-generated method stub
 								progressDialog = ProgressDialog.show(StartPage.this, "Loading....", "Remember to turn on GPS", true, false);
 							}
 							
 						});
 					 Stage stage = manager.getStage(cursor);
 					 Stage.buildBitmap(StartPage.this);
 					 Backpack backpack = new Backpack(manager.getFileName(cursor),StartPage.this,stage.getItemList());
 
 					 ContainerBox.currentStage = stage;
 					 ContainerBox.backback = backpack;
 					 
 					 StartPage.this.runOnUiThread(new Runnable(){
 
 						@Override
 						public void run() {
 							// TODO Auto-generated method stub
 							Intent playStage = new Intent();
 							playStage.setClass(StartPage.this, MapMode.class);
 							
 							int which = (int)(Math.random()*DrawableIndex.TOTAL);
 							
 							BackgroundMusic.setBGM(which);
 							BackgroundMusic.init(StartPage.this);
 							
 							cursor = -1;
 							startActivityForResult(playStage,0);
 							progressDialog.dismiss();
 						}
 						
 					});
 					
 					
 				 }
 			};
 			load.start();
 			
 		
 		
 	}
 	
 
 	private void delete() {
 		manager.deleteStage(cursor);
 		reBuildStageList();
 	}
 
 	/** Button onClick listeners */
 	public void addClicked(View view) {
 		Intent net = new Intent();
 		net.setClass(this, DownLoadPageActivity.class);
 		startActivity(net);
 	}
 	
 	public void helpClicked(View view) {
 		Intent help = new Intent();
 		help.setClass(this, HelpPage.class);
 		startActivity(help);
 	}
 	
 	public void aboutClicked(View view) {
 		Intent about = new Intent();
 		about.setClass(this, AboutPage.class);
 		startActivity(about);
 	}
 
 }
