 package net.wesley.gitadroia;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class ShowActivity extends Activity {
 	
 	class TimePosition{
 		public long deltatime;
 		public float deltay;
 		public TimePosition(long deltatime, float deltay) {
 			this.deltatime=deltatime;
 			this.deltay=deltay;
 		}
 		
 	}
 
 	protected static final long RECORD_CHECK_DELTA = 100;
 	
 	ArrayList<TimePosition> poses=new ArrayList<TimePosition>();
 	
 	protected Toast starthint;
 	protected int startLeft;
 
 	protected Handler playhandler=new Handler();
 	protected Runnable playrunnable=new Runnable(){
 		@Override
 		public void run() {
 			boolean found=false;
 			long delta = SystemClock.elapsedRealtime()-playstartat;
 			for (int i=0;i<poses.size();i++){
 				TimePosition tp=poses.get(i);
 				if (tp.deltatime>delta){
 					int idx=i-1;
 					if (idx<0) idx=0;
 					tp=poses.get(idx);
 					found=true;
 					WebView web=(WebView) findViewById(R.id.webviewinshow);
 					float webviewsize = web.getContentHeight() - web.getTop();
                     float positionInWV = webviewsize * tp.deltay;
                     int positionY = Math.round(web.getTop() + positionInWV);
                     if (Math.abs(web.getScaleY()-positionY)>10){
                     	web.scrollTo(0, positionY);
                     }
 					break;
 				}
 			}
 			
 			if (found){
 				playhandler.postDelayed(playrunnable, RECORD_CHECK_DELTA);
 			}			
 			else{
 				stopPlay();
 			}
 			
 		}
 	};
 
 	protected Handler rechandler=new Handler();
 	protected Runnable recrunnable=new Runnable(){
 		@Override
 		public void run() {
 			WebView web=(WebView) findViewById(R.id.webviewinshow);
 			long deltatime = SystemClock.elapsedRealtime()-recstartat;
 		   float deltay = ((float)(web.getScrollY()-web.getTop()))/web.getContentHeight();
 			if (poses.size()==0){ 
 				poses.add(new TimePosition(deltatime,deltay));
 			}
 			else{
 				TimePosition pos=poses.get(poses.size()-1);
 				if (pos.deltay!=deltay){
 					poses.add(new TimePosition(deltatime,deltay));
 				}
 			}
 			rechandler.postDelayed(recrunnable, RECORD_CHECK_DELTA);
 		}
 	};
 
 	protected Handler playbchandler=new Handler();
 	protected Runnable playbcrunnable=new Runnable(){
 		@Override
 		public void run() {
 			startPlayLeft-=1;
 			if (startPlayLeft>=0){
 				if (startPlayLeft>0){
 					startplayhint.setText("将在"+startPlayLeft+"秒后开始自动滚屏...");
 				}
 				else{
 					startplayhint.cancel();
 					playstartat=SystemClock.elapsedRealtime();
 					playhandler.post(playrunnable);
 					Button btn=(Button)findViewById(R.id.btnplayinplay);
 					btn.setText("停止播放");
 				}
 				playbchandler.postDelayed(playbcrunnable, 1000);
 			}
 		}
 	};
 
 	
 	protected Handler bchandler=new Handler();
 	protected long recstartat;
 	protected Runnable bcrunnable=new Runnable(){
 		@Override
 		public void run() {
 			startLeft-=1;
 			if (startLeft>=0){
 				if (startLeft>0){
 					starthint.setText("将在"+startLeft+"秒后开始录制滚屏...");
 				}
 				else{
 					starthint.cancel();
 					poses.clear();
 					Button btn=(Button)findViewById(R.id.btnrecordinplay);
 					btn.setText("停止录制");
 					btn.setEnabled(true);
 					rechandler.postDelayed(recrunnable, 500);
 					recstartat=SystemClock.elapsedRealtime();
 				}
 				bchandler.postDelayed(bcrunnable, 1000);
 			}
 		}
 	};
 	protected boolean recording=false;
 
 	protected boolean playing=false;
 
 	protected long playstartat;
 
 	protected Toast startplayhint;
 
 	protected int startPlayLeft;
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.showlayout);
 		
 		WebView web=(WebView) findViewById(R.id.webviewinshow);
 		web.getSettings().setBuiltInZoomControls(true);
 		web.getSettings().setSupportZoom(true);
 		web.getSettings().setDisplayZoomControls(false);
 
 		((Button)findViewById(R.id.btnplayinplay)).setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				Button btn=(Button)arg0;
 				playing=!playing;
 				if (playing){
 					if (poses.size()<=0){
 						Toast.makeText(ShowActivity.this, "似乎你需要先录制过了才能播放!", Toast.LENGTH_SHORT).show();
 						playing=false;
 						return;
 					}
 					((Button)findViewById(R.id.btnrecordinplay)).setEnabled(false);
 					
 					startplayhint=Toast.makeText(ShowActivity.this, "将在3秒后开始自动滚屏...", Toast.LENGTH_LONG);
 					startplayhint.show();
 					startPlayLeft=3;
 					playbchandler.postDelayed(playbcrunnable, 1000);					
 				}
 				else{
 					stopPlay();
 				}
 			}
 		});
 		
 		((Button)findViewById(R.id.btnrecordinplay)).setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				recording=!recording;
 				Button btn=(Button)arg0;
 				if (recording){
 					arg0.setEnabled(false);
 					((Button)findViewById(R.id.btnplayinplay)).setEnabled(false);
 					starthint=Toast.makeText(ShowActivity.this, "将在3秒后开始录制滚屏...", Toast.LENGTH_LONG);
 					starthint.show();
 					startLeft=3;
 					bchandler.postDelayed(bcrunnable, 1000);
 				}
 				else{
 					btn.setText("开始录制");
 					((Button)findViewById(R.id.btnplayinplay)).setEnabled(true);
 					rechandler.removeCallbacks(recrunnable);
 					if (poses.size()>0){
 						saveRecordToFile();
 					}
 				}
 			}
 		});
 		
 		String path=getIntent().getStringExtra("path");
 		String cachepath=getApplicationContext().getCacheDir().getAbsolutePath();
 		((Button)findViewById(R.id.btnplayinplay)).setEnabled(false);
 		if (path.startsWith(cachepath)){
 			((Button)findViewById(R.id.btnrecordinplay)).setEnabled(false);
 		}
 		else{
 			File recf=new File(path+".rec");
 			if (recf.exists()){
 				poses.clear();
 				((Button)findViewById(R.id.btnplayinplay)).setEnabled(true);
 				try {
 					DataInputStream in = new DataInputStream(new FileInputStream(recf));
 					while (in.available()>0){
 						poses.add(new TimePosition(in.readLong(),in.readFloat()));
 					}
 				}
 				catch (Exception e){
 					Log.e("", "exception",e);
 				}
 
 			}
 			
 		}
 		File file=new File(path);
 		try {
 			DataInputStream in = new DataInputStream(new FileInputStream(file));
 			String song=in.readUTF();
 			String singer=in.readUTF();
 			int idx=0;
 			String cnt="";
 			while (in.available()>0){
 				int len=in.readInt();
 				byte[] bs=new byte[len];
 				in.read(bs);
 				Bitmap bmp=BitmapFactory.decodeByteArray(bs, 0, len);
 				try {
 					File tmp=new File(getApplicationContext().getCacheDir(),"img"+idx+".png");
 					cnt+="<tr><td><img width='100%' src='file://"+tmp.getAbsolutePath()+"'></td></tr>";
 					idx++;
 					FileOutputStream out = new FileOutputStream(tmp);
 					bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
 				} catch (Exception e) {
 					Log.e("", "",e);
 				}			
 			}
 			String body="<html><table width='100%' border='0'>"+cnt+"</table></html>";
 			Log.e("", body);
 			web.loadDataWithBaseURL(null,body, "text/html", "utf-8",null);
 		}
 		catch (Exception e){
 			Log.e("", "exception",e);
 		}
 				
 	}
 
 	protected void saveRecordToFile() {
 		String path=getIntent().getStringExtra("path");
 		String recpath=path+".rec";
 		File file=new File(recpath);
 		try {
 			DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
 			for (TimePosition tp:poses){
 				out.writeLong(tp.deltatime);
 				out.writeFloat(tp.deltay);
 			}
 			out.close();			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}		
 	}
 
 	@Override
 	protected void onPause(){
 		super.onPause();
 		playhandler.removeCallbacks(playrunnable);
 		rechandler.removeCallbacks(recrunnable);
 		playbchandler.removeCallbacks(playbcrunnable);
 		bchandler.removeCallbacks(bcrunnable);
 	}
 	
 	protected void stopPlay() {
 		((Button)findViewById(R.id.btnrecordinplay)).setEnabled(true);
 		playhandler.removeCallbacks(playrunnable);
 		((Button)findViewById(R.id.btnplayinplay)).setText("开始播放");
 	}
 }
