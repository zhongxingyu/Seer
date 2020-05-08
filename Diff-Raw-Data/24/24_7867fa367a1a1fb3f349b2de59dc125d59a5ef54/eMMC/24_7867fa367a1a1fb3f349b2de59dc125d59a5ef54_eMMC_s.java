 package com.tware.anexter;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.StatFs;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class eMMC extends Activity {
 	private Button bPass;
 	private Button bFail;
 	private Button bNa;
 	private String LOG;
 	
 	private String [] eMMC_strSplit =  new String[20];
 	private Float eMMC_Total = (float)0;
 	private int platId = 0;
 	 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.emmc);
         this.setTitle("aNexter - eMMC信息检查");
         
         if (util.isRk()){
         	platId = 1;
         }
         
         bPass = (Button)findViewById(R.id.btn_pass);       
         bPass.setOnClickListener(new OnClickListener(){
         	@Override
 			public void onClick(View v)
         	{
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + String.format("%.0fGB", eMMC_Total) +"|");
         		i.setClass(eMMC.this , compass.class);
         		startActivity(i);
         		eMMC.this.finish();
         	}
         });
         
         TextView emmc = (TextView)findViewById(R.id.view_emmc);
         
 	    File path = Environment.getDataDirectory();   
 	    StatFs stat = new StatFs(path.getPath());   
 	       	
 	    long blockSize = stat.getBlockSize();   
 	    long aBlocks = stat.getAvailableBlocks(); 
 	       	
 	    float aSize = (float)blockSize * aBlocks/(1024*1024*1024);
 	    try
 	    {		       	
	       	FileReader emmc_fr = new FileReader("/proc/partitions");
	       	BufferedReader emmc_br = new BufferedReader(emmc_fr);
 	       	String eMMC_str = emmc_br.readLine();
 		    switch(platId)
 		    {	
 		    	case 0:
 		    		eMMC_str = emmc_br.readLine();
 		       		eMMC_str = emmc_br.readLine();  /** start at 3th line */
 		       		eMMC_strSplit = eMMC_str.split(" ");
 
 		       		while (eMMC_strSplit == null || 
 		       				Integer.parseInt(eMMC_strSplit[eMMC_strSplit.length-2]) < 10000)
 		       		{
 		       			eMMC_str = emmc_br.readLine();
 		       			eMMC_strSplit = eMMC_str.split(" ");
 		       		}
 
 		       		eMMC_Total = (float) (Float.parseFloat(
 		       					eMMC_strSplit[eMMC_strSplit.length-2])/(1000*1000)*1.024*1.024*1.024);
		       				break;
 		       				
 		       	case 1:
 		       		eMMC_Total = (float) 0;
 		       		while(eMMC_str != null)
 		       		{
 		       			if (eMMC_str.contains("mtdblock") && (eMMC_strSplit = eMMC_str.split(" "))!=null)
 		       			{
		       				eMMC_Total = eMMC_Total + (Float.parseFloat(
	   		    			eMMC_strSplit[eMMC_strSplit.length-2]));
		      		
 		       			}
 		       			eMMC_str = emmc_br.readLine();
 		       		}
 		       		eMMC_Total = (float) (eMMC_Total/(1000*1000)*1.024*1.024*1.024);
 		       		break;
 		    }
        	}catch (FileNotFoundException e){
        		eMMC_Total = (float)-1;
        	} catch (IOException e) {
        		eMMC_Total = (float)-2;
        	}
         emmc.setTextSize(38);
         emmc.setText(String.format("全部容量: %.0f GB \n\n可用容量: %.2f GB"
 	       			, eMMC_Total, aSize));
         
         bFail = (Button)findViewById(R.id.btn_fail);
         bFail.setOnClickListener(new OnClickListener(){
         	@Override
     		public void onClick(View v)
         	{
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + "FAIL|");
         		i.setClass(eMMC.this , getResults.class);
         		startActivity(i);
         		eMMC.this.finish();
         	}
         });
         
         bNa = (Button)findViewById(R.id.btn_na);
         bNa.setVisibility(View.INVISIBLE);
         
         LOG = this.getIntent().getStringExtra("LOG");
         Toast.makeText(getApplicationContext(), LOG, Toast.LENGTH_SHORT).show();
      
     }
     
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 }
