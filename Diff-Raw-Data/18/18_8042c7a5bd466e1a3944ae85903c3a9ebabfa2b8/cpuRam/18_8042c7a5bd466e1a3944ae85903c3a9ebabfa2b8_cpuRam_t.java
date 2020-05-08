 package com.tware.anexter;
 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class cpuRam extends Activity {
 	private Button bPass;
 	private Button bFail;
 	private Button bNa;
    	private long memSize = 0;
    	private String strMemSize = "unknown";
    	private String LOG;
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.cpuram);
         this.setTitle("aNexter - CPU & RAM & 显示分辨率 检查");
         
         Display display = getWindowManager().getDefaultDisplay();
         
         bPass = (Button)findViewById(R.id.btn_pass);       
         bPass.setOnClickListener(new OnClickListener(){
         	@Override
 			public void onClick(View v)
         	{
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + strMemSize +"|");
         		i.setClass(cpuRam.this , eMMC.class);
         		startActivity(i);
         		cpuRam.this.finish();
         	}
         });
         
         TextView cpuRam = (TextView)findViewById(R.id.view_cpuram);
         
        	String line = null;
 
        	
         try {
        	BufferedReader r = new BufferedReader(new InputStreamReader( new FileInputStream( new File("/proc/meminfo") )),32);
 				line = r.readLine();
 				 memSize =  Long.parseLong(line.substring(line.indexOf(":")+1,
 									line.lastIndexOf("k")).trim())/1024;
         }
 		catch (IOException e) {
 				return ;
 		}
         
         if (memSize >256 && memSize <=512)
         {
         	strMemSize = "512 MB";
         } else if (memSize > 512 && memSize <= 1024){
         	strMemSize = "1 GB";
         }
         
         cpuRam.setTextSize(38);
         cpuRam.setText("CPU ABI : " +  android.os.Build.CPU_ABI + 
         		"\n内存大小: " + strMemSize +
         		"\n分辨率: " + display.getWidth() + " x " + display.getHeight());
      
         bFail = (Button)findViewById(R.id.btn_fail);
         bFail.setOnClickListener(new OnClickListener(){
         	@Override
     		public void onClick(View v)
         	{
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG +"FAIL|");
         		i.setClass(cpuRam.this , getResults.class);
         		startActivity(i);
         		cpuRam.this.finish();
         	}
         });
         
         bNa = (Button)findViewById(R.id.btn_na);
         bNa.setVisibility(View.INVISIBLE);
         
         LOG = this.getIntent().getStringExtra("LOG");
         Toast.makeText(getApplicationContext(), LOG, Toast.LENGTH_SHORT).show();
         
     }
     
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		// TODO Auto-generated method stub
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 }
