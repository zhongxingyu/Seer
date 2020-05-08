 package net.wesley.gitadroia;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 public class LocallistActivity extends Activity {
 	
 	private ArrayList<HashMap<String,String>> locallist=new ArrayList<HashMap<String,String>>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.locallist);
 		((Button)this.findViewById(R.id.btnsearch)).setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				Intent i=new Intent(LocallistActivity.this,SearchActivity.class);
 				//Intent i=new Intent(LocallistActivity.this,ShowActivity.class);
 				//i.putExtra("path", "/sdcard/preview.jita");
 				startActivity(i);
 			}
 		});
 		
 		ListView lv=(ListView) findViewById(R.id.listinlocallist);
 		lv.setOnItemClickListener(new OnItemClickListener(){
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				Intent i=new Intent(LocallistActivity.this,ShowActivity.class);
 				i.putExtra("path", locallist.get(arg2).get("path"));
 				startActivity(i);
 			}
 		});
 	}
 	
 	@Override
 	protected void onResume(){
 		super.onResume();
 		refreshLocalList();
 	}
 
 	private void refreshLocalList() {
 		locallist.clear();
 		File f=getApplicationContext().getFilesDir();
 		File[] files = f.listFiles();
 		for (File fi:files){
 			if (fi.getAbsolutePath().endsWith(".gita")){
 				DataInputStream in;
 				try {
 					in = new DataInputStream(new FileInputStream(fi));
 					String song=in.readUTF();
 					String singer=in.readUTF();
 					HashMap<String,String>map=new HashMap<String,String>();
 					map.put("song", song);
 					map.put("path", fi.getAbsolutePath());
 					map.put("singer", singer);
 					locallist.add(map);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		ListView lv=(ListView) findViewById(R.id.listinlocallist);
 		lv.setAdapter(new SimpleAdapter(this,
 				locallist, R.layout.songrow,
 				new String[] { "song", "singer"}, new int[] {
 						R.id.listrow_name, R.id.listrow_artist}));
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.locallist, menu);
 		return true;
 	}
 
 }
