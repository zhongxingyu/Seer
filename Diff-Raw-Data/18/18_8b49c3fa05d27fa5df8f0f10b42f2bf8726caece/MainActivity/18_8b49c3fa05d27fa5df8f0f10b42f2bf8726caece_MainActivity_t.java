 package net.wesley.kalaplayer;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.SystemClock;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 //import android.widget.VideoView;
 import android.widget.VideoView;
 
 public class MainActivity extends Activity {
 	MediaPlayer mPlayer;
 	public DatagramSocket mSocket;
 	private byte[] bytes=new byte[1024];
 	private DatagramPacket mPacket=new DatagramPacket(bytes, bytes.length);
 	private ArrayList<HashMap<String,String>> playlist=new ArrayList<HashMap<String,String>>();
 	private boolean playing=false;
 	private boolean downloading=false;
 	public Integer lastpercent=-1;
 	private boolean isKalaOK=true;
 	HttpConnection currconn;
 	private HashMap<String,String> currplaysong=null;
 	private Handler syncHandler=new Handler();
 	ArrayList<String> localsearchleft=new ArrayList<String>();
 	private Runnable syncRunnable=new Runnable(){
 		@Override
 		public void run() {
 			try{
 				VideoView v=(VideoView) findViewById(R.id.idvideoview);
 				if (v.isPlaying()){
 					long vp = v.getCurrentPosition();
 					long ap = mPlayer.getCurrentPosition();
 					if (Math.abs(ap-vp)>100){
 						Toast.makeText(MainActivity.this, "同步音视频", Toast.LENGTH_LONG).show();
 						v.pause();
 						mPlayer.seekTo((int) v.getCurrentPosition());
 						v.start();
 					}
 				}
 			}
 			catch (Exception e){
 				
 			}
 			syncHandler.postDelayed(syncRunnable, 5000);
 		}
 	};
 	public Object lastCmd="";
 	private HashMap<String, String> currDownloadMap;
 
 	static class GetSongMVHandler extends Handler {
 	    private final WeakReference<MainActivity> mActivity;
 		private String prefix;
 		private String songurl;
 		private String musicurl; 
 
 		GetSongMVHandler(MainActivity service, String songurl, String musicurl, String prefix) {
 			((TextView)service.findViewById(R.id.idprogress)).setText("下载 "+prefix+" 视频");
 	    	mActivity = new WeakReference<MainActivity>(service);
 	    	this.prefix=prefix;
 	    	this.songurl=songurl;
 	    	this.musicurl=musicurl;
 	    }
 	    @Override
 	    public void handleMessage(Message msg)
 	    {
 	    	MainActivity service = mActivity.get();
 	         if (service != null) {
 	              service.handleGetSongMv(msg,songurl,musicurl,prefix);
 	         }
 	    }
 	}
 
 	static class GetSongSongHandler extends Handler {
 	    private final WeakReference<MainActivity> mActivity;
 		private String prefix;
 		private String musicurl;
 		private String mvpath; 
 
 		GetSongSongHandler(MainActivity service, String mvpath, String musicurl, String prefix) {
 			((TextView)service.findViewById(R.id.idprogress)).setText("下载 "+prefix+" 原唱");
 	    	mActivity = new WeakReference<MainActivity>(service);
 	    	this.prefix=prefix;
 	    	this.musicurl=musicurl;
 	    	this.mvpath=mvpath;
 	    }
 	    @Override
 	    public void handleMessage(Message msg)
 	    {
 	    	MainActivity service = mActivity.get();
 	         if (service != null) {
 	              service.handleGetSongSong(msg,mvpath,musicurl,prefix);
 	         }
 	    }
 	}
 
 	static class GetSongMusicHandler extends Handler {
 	    private final WeakReference<MainActivity> mActivity;
 		private String prefix;
 		private String mvpath;
 		private String songpath;
 
 		GetSongMusicHandler(MainActivity service, String mvpath,String songpath, String prefix) {
 			((TextView)service.findViewById(R.id.idprogress)).setText("下载 "+prefix+" 伴奏");
 	    	mActivity = new WeakReference<MainActivity>(service);
 	    	this.prefix=prefix;
 	    	this.mvpath=mvpath;
 	    	this.songpath=songpath;
 	    }
 	    @Override
 	    public void handleMessage(Message msg)
 	    {
 	    	MainActivity service = mActivity.get();
 	         if (service != null) {
 	              service.handleGetSongMusic(msg,mvpath,songpath,prefix);
 	         }
 	    }
 	}
 	
 	static class GetSongHandler extends Handler {
 	    private final WeakReference<MainActivity> mActivity;
 		private String prefix; 
 
 	    GetSongHandler(MainActivity service, String prefix) {
 	    	mActivity = new WeakReference<MainActivity>(service);
 	    	this.prefix=prefix;
 	    }
 	    @Override
 	    public void handleMessage(Message msg)
 	    {
 	    	MainActivity service = mActivity.get();
 	         if (service != null) {
 	              service.handleGetSong(msg,prefix);
 	         }
 	    }
 	}
 
 	
 	class UDPServerTask extends AsyncTask<Integer,String,Void> {
 
 		@Override
 		protected Void doInBackground(Integer... ports) {
 			try {
 				mSocket = new DatagramSocket(ports[0]);
 				mSocket.setReuseAddress(true);
 				while (true){ 
 					try {
 						mSocket.receive(mPacket);
 						String curPeer = mPacket.getAddress().toString().substring(1);
 						String cnt=new String(mPacket.getData(),0, mPacket.getLength(),"UTF-8");
 						if (cnt.equals(lastCmd)){
 							continue;
 						}
 						lastCmd=cnt;
 						publishProgress(curPeer, ""+mPacket.getPort(),cnt);
 					} catch (Exception e) {
 						Log.d("myown", "receive thread exit because of exception");
 						e.printStackTrace();
 						break;
 					}
 				}
 
 			} catch (SocketException e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 		
 		@Override     
         protected void onCancelled() {     
                 super.onCancelled();     
         }     
         @Override     
         protected void onPostExecute(Void result) {  
         	//Toast.makeText(MainActivity.this, "播放器任务已经退出，原因不明！", Toast.LENGTH_LONG).show();
         }     
         
         @Override     
         protected void onPreExecute() {     
         }
         
         @Override     
         protected void onProgressUpdate(String... values) {  
         	if (values.length!=3) return;
         	processControlCmd(values[0], Integer.parseInt(values[1]), values[2]);
         }     
         
 	}
 	
 	private void enableSocketOnMainUI() {
 		if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) >= 9) {
 		    try {
 		        // StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);
 		           Class<?> strictModeClass = Class.forName("android.os.StrictMode", true, Thread.currentThread()
 		                        .getContextClassLoader());
 		           Class<?> threadPolicyClass = Class.forName("android.os.StrictMode$ThreadPolicy", true, Thread.currentThread()
 		                        .getContextClassLoader());
 		           Field laxField = threadPolicyClass.getField("LAX");
 		           Method setThreadPolicyMethod = strictModeClass.getMethod("setThreadPolicy", threadPolicyClass);
 		                setThreadPolicyMethod.invoke(strictModeClass, laxField.get(null));
 		    } 
 		    catch (Exception e) { }
 		}
 	}
 
 
 	
 	public void handleGetSongMusic(Message message, String mvpath,String songpath, String prefix) {
 		switch (message.what) {
 		case HttpConnection.DID_ERROR:
 			Log.e("", "Song Music download Error",(Exception)message.obj);
 			Toast.makeText(this, ""+message.obj, Toast.LENGTH_LONG).show();
 			break;
 		case HttpConnection.DID_PROGRESS:
 			int percent=(Integer)message.obj;
 			if (percent!=lastpercent){
 				lastpercent=percent;
 				((TextView)findViewById(R.id.idprogress)).setText("下载 "+prefix+" 3/3:"+percent+"%");
 			}
 			break;
 		case HttpConnection.DID_SUCCEED: {
 			((TextView)findViewById(R.id.idprogress)).setText("");
 			String musicpath=(String) message.obj;
 			Log.d("myown", "get music response:"+musicpath);
 			saveToFile(getFinishTagLocalPath(prefix),mvpath+"\n"+songpath+"\n"+musicpath+"\n");
 			if (!playing) startPlay();
 			downloading=false;
 			startDownload();
 		}
 		}
 	}
 
 	private void saveToFile(String finishTagLocalPath, String string) {
 		try {
 			File outputFile = new File(finishTagLocalPath);
 			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
 			writer.write(string);
 			writer.close();        }
 		catch (IOException e) {
 			Log.e("", "File write failed: " + e.toString());
 		} 
 	}
 
 
 
 	private String getFinishTagLocalPath(String prefix) {
 		return getBase()+prefix+"_3.tag";
 	}
 
 
 
 	@SuppressLint("SdCardPath")
 	private String getBase() {
 		String base="/mnt/sdcard2/kala/";
 		if (new File(base).exists()){
 			return base;
 		}
 		else{
 			return "/sdcard/kala/";
 		}
 	}
 
 
 
 	public void handleGetSongSong(Message message, String mvpath, String musicurl, String prefix) {
 		switch (message.what) {
 		case HttpConnection.DID_ERROR:
 			Log.e("", "Song Song download Error",(Exception)message.obj);
 			Toast.makeText(this, ""+message.obj, Toast.LENGTH_LONG).show();
 			break;
 		case HttpConnection.DID_PROGRESS:
 			int percent=(Integer)message.obj;
 			if (percent!=lastpercent){
 				lastpercent=percent;
 				((TextView)findViewById(R.id.idprogress)).setText("下载 "+prefix+" 2/3:"+percent+"%");
 			}
 			break;
 		case HttpConnection.DID_SUCCEED: {
 			String songpath=(String) message.obj;
 			Log.d("myown", "get song response:"+songpath);
 			currconn=new HttpConnection(this,new GetSongMusicHandler(this,mvpath,songpath,prefix));
 			currconn.file(musicurl, getMusicLocalPath(musicurl,prefix));
 		}
 		}
 	}
 
 
 
 	public void handleGetSongMv(Message message, String songurl, String musicurl,
 			String prefix) {
 		switch (message.what) {
 		case HttpConnection.DID_ERROR:
 			Log.e("", "MV download Error",(Exception)message.obj);
 			Toast.makeText(this, ""+message.obj, Toast.LENGTH_LONG).show();
 			break;
 		case HttpConnection.DID_PROGRESS:
 			int percent=(Integer)message.obj;
 			if (percent!=lastpercent){
 				lastpercent=percent;
 				((TextView)findViewById(R.id.idprogress)).setText("下载 "+prefix+" 1/3:"+percent+"%");
 			}
 			break;
 		case HttpConnection.DID_SUCCEED: {
 			String mvpath=(String) message.obj;
 			Log.d("myown", "get mv response:"+mvpath);
 			currconn=new HttpConnection(this,new GetSongSongHandler(this,mvpath,musicurl,prefix));
 			currconn.file(songurl, getSongLocalPath(songurl,prefix));
 		}
 		}
 	}
 
 
 
 	private String getSongLocalPath(String url, String prefix) {
 		String[] parts = url.split("\\.");
 		return getBase()+prefix+"_1."+parts[parts.length-1];
 	}
 
 	private String getMusicLocalPath(String url, String prefix) {
 		String[] parts = url.split("\\.");
 		return getBase()+prefix+"_2."+parts[parts.length-1];
 	}
 
 	public void handleGetSong(Message message, String prefix) {
 		switch (message.what) {
 		case HttpConnection.DID_SUCCEED: {
 			String body=(String) message.obj;
 			Log.d("myown", "response:"+body);
 			try {  
 			    JSONTokener jsonParser = new JSONTokener(body);  
 			    JSONObject result = (JSONObject) jsonParser.nextValue(); 
 			    if (result.getInt("result")==0){
 			    	Toast.makeText(this, result.getString("text"), Toast.LENGTH_LONG).show();
 			    }
 			    else{
			    	if (!result.has("mv")){
 			    		removeCurrDownload("没有MV视频，跳过播放！");
 			    		return;
 			    	}
			    	String mv=result.getString("mv");
 			    	String song=result.getString("song");
 			    	String music=result.getString("music");
 			    	if (new File(getMusicLocalPath(music,prefix)).exists()){
 			    		currconn=new HttpConnection(this,new GetSongMusicHandler(this,
 				    			getMvLocalPath(mv,prefix),
 				    			getSongLocalPath(song,prefix),
 				    			prefix));
 			    		currconn.file(music, getMusicLocalPath(music,prefix));
 			    	}
 			    	else if (new File(getSongLocalPath(song,prefix)).exists()){
 			    		currconn=new HttpConnection(this,new GetSongSongHandler(this,
 				    			getMvLocalPath(mv,prefix),
 				    			music,prefix));
 			    		currconn.file(song, getSongLocalPath(song,prefix));
 			    	}
 			    	else{
 			    		currconn=new HttpConnection(this,new GetSongMVHandler(this,song,music,prefix));
 			    		currconn.file(mv, getMvLocalPath(mv,prefix));
 			    	}
 			    }
 			} catch (JSONException ex) {  
 			}  
 		}
 		}
 	}
 
 
 
 	private void removeCurrDownload(String string) {
 		if (playlist.indexOf(currDownloadMap)>=0){
 			Toast.makeText(this, "无法下载"+currDownloadMap.get("name")+":"+string, Toast.LENGTH_LONG).show();
 			playlist.remove(currDownloadMap);
 			downloading=false;
 			startDownload();
 		}
 		
 	}
 
 
 
 	private String getMvLocalPath(String url,String prefix) {
 		String[] parts = url.split("\\.");
 		return getBase()+prefix+"_0."+parts[parts.length-1];
 	}
 
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		 mPlayer = new MediaPlayer();
 		setContentView(R.layout.activity_main);
 		enableSocketOnMainUI();
 		startControllerAsyncTask();
 	}
 	
 	@Override 
 	public void onBackPressed(){
 		VideoView v=(VideoView) findViewById(R.id.idvideoview);
 		try{v.stopPlayback();}catch (Exception e){}
 		try{mPlayer.stop();}catch (Exception e){}
 		try{currconn.cancelDownload();}catch (Exception e){}
 		try{mSocket.close();}catch (Exception e){}
 		super.onBackPressed();
 		syncHandler.removeCallbacks(syncRunnable);
 	}
 
 	protected void sendString(String dst, int port, String string) {
 		byte[] bytes=string.getBytes();
 		try {
 			InetAddress addr = InetAddress.getByName(dst);
 			DatagramPacket dPacket = new DatagramPacket(bytes, 
 					bytes.length,addr, port); 
 			mSocket.send(dPacket);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	public void processControlCmd(String peer, int port, String cnt) {
 		String[] cmds = cnt.split(" ",2);
 		if (cmds[0].equalsIgnoreCase("add")){
 			processAddReq(cmds[1]);
 			sendString(peer,port,"OK");
 		}
 		else if (cmds[0].equalsIgnoreCase("list")){
 			String response="";
 			for (int i=0;i<playlist.size();i++){
 				response+=String.format("%s %s %s;", playlist.get(i).get("id"),playlist.get(i).get("name"),playlist.get(i).get("artist"));
 			}
 			sendString(peer,port,response);
 		}
 		else if (cmds[0].equalsIgnoreCase("pause")){
 			sendString(peer,port,"OK");
 			Toast.makeText(this, "切换播放/暂停", Toast.LENGTH_LONG).show();
 			VideoView v=(VideoView) this.findViewById(R.id.idvideoview);
 			if (v.isPlaying()){
 				v.pause();
 				mPlayer.pause();
 			}
 			else{
 				mPlayer.seekTo((int) v.getCurrentPosition());
 				v.start();
 				mPlayer.start();
 			}
 		}		
 		else if (cmds[0].equalsIgnoreCase("kalaok")){
 			sendString(peer,port,"OK");
 			Toast.makeText(this, "切换原唱/伴奏", Toast.LENGTH_LONG).show();
 			if (playing){
 				changeSoundChannel(currplaysong);
 			}
 		}		
 		else if (cmds[0].equalsIgnoreCase("delete")){
 			String songid=cmds[1].split(" ")[0];
 			Toast.makeText(this, "删除歌曲"+songid, Toast.LENGTH_LONG).show();
 			File f = new File(getBase());
 			File[] files = f.listFiles();
 			for (File inFile : files) {
 				if (inFile.getName().endsWith("_3.tag")){
 					if (inFile.getName().startsWith(songid+"_")){
 						inFile.delete();
 					}
 				}
 			}
 			sendString(peer,port,"OK");
 
 		}		
 		else if (cmds[0].equalsIgnoreCase("quit")){
 			sendString(peer,port,"OK");
 			Toast.makeText(this, "退出", Toast.LENGTH_LONG).show();
 			this.onBackPressed();
 		}		
 		else if (cmds[0].equalsIgnoreCase("searchnext")){
 			int page=Integer.parseInt(cmds[1]);
 			String result=""+page+" ";
 			int total=0;
 			for (int i=0;i<10;i++) {
 				if (page*10+i>=localsearchleft.size()) break;
 				String fname=localsearchleft.get(page*10+i);
 				String[] infos = fname.split("_",4);
 				if (infos.length==4){
 					total+=1;
 					result+=String.format("%s %s %s;", infos[0],infos[1],infos[2]);
 					if (total>10) break;
 				}
 			}
 			sendString(peer,port,result);
 		}
 		else if (cmds[0].equalsIgnoreCase("search")){
 			Toast.makeText(this, "本地搜索", Toast.LENGTH_LONG).show();
 			File f = new File(getBase());
 			File[] files = f.listFiles();
 			String result="";
 			int total=0;
 			localsearchleft.clear();
 			String keyword=cmds[1].trim().toLowerCase();
 			if (keyword.startsWith("empty")) keyword="";
 			for (File inFile : files) {
 				if (inFile.getName().endsWith("_3.tag")){
 					String[] infos = inFile.getName().split("_",4);
 					if (infos.length==4){
 						if ((keyword.length()<=0) || (inFile.getName().contains(keyword))){
 							total+=1;
 							if (total>=10){
 								localsearchleft.add(inFile.getName());
 							}
 							else{
 								result+=String.format("%s %s %s;", infos[0],infos[1],infos[2]);
 							}
 						}
 					}
 				}
 			}
 			sendString(peer,port,result);
 		}
 		else if (cmds[0].equalsIgnoreCase("next")){
 			sendString(peer,port,"OK");
 			Toast.makeText(this, "切歌", Toast.LENGTH_LONG).show();
 			if (playing){
 				VideoView v=(VideoView) this.findViewById(R.id.idvideoview);
 				v.stopPlayback();
 				playNext();
 			}
 		}		
 	}
 
 	private void playNext() {
 		mPlayer.stop();
 		mPlayer.reset();
 		VideoView v=(VideoView) this.findViewById(R.id.idvideoview);
 		if (currplaysong!=null) playlist.remove(currplaysong);
 		playing=false;
 		LinearLayout l=(LinearLayout) findViewById(R.id.idcover);
 		l.setVisibility(View.VISIBLE);
 		v.setVisibility(View.INVISIBLE);
 		startPlay();
 	}
 
 
 
 	private void processAddReq(String string) {
 		String[] songs = string.split(";");
 		for (String song:songs){
 			String[] infos = song.trim().split(" ");
 			Toast.makeText(this, ""+infos.length+" "+infos[0], Toast.LENGTH_LONG).show();
 			if (infos.length==3){
 				HashMap<String,String>map=new HashMap<String,String>();
 				map.put("id", infos[0]);
 				map.put("name", infos[1]);
 				map.put("artist", infos[2]);
 				playlist.add(map);
 			}
 		}
 		
 		if (!downloading) startDownload();
 		if (!playing) startPlay();
 		
 	}
 
 
 
 	private void startDownload() {
 		for (HashMap<String,String>map:playlist){
 			if (!fileReady(getPrefixFromMap(map))){
 				downloading=true;
 				currDownloadMap=map;
 				download(map.get("id"),getPrefixFromMap(map));
 				break;
 			}
 		}
 	}
 
 
 
 	private String getPrefixFromMap(HashMap<String, String> map) {
 		return ""+map.get("id")+"_"+map.get("name")+"_"+map.get("artist");
 	}
 
 
 
 	private void startPlay() {
 		for (HashMap<String,String>map:playlist){
 			if (fileReady(getPrefixFromMap(map))){
 				playing=true;
 				currplaysong=map;
 				play(map);
 				break;
 			}
 		}
 	}
 
 	public static String md5(String string) {
 	    byte[] hash;
 	    try {
 	        hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
 	    } catch (NoSuchAlgorithmException e) {
 	        throw new RuntimeException("Huh, MD5 should be supported?", e);
 	    } catch (UnsupportedEncodingException e) {
 	        throw new RuntimeException("Huh, UTF-8 should be supported?", e);
 	    }
 
 	    StringBuilder hex = new StringBuilder(hash.length * 2);
 	    for (byte b : hash) {
 	        if ((b & 0xFF) < 0x10) hex.append("0");
 	        hex.append(Integer.toHexString(b & 0xFF));
 	    }
 	    return hex.toString();
 	}
 
 
 	private void download(String id, String prefix) {
 		((TextView)findViewById(R.id.idprogress)).setText("下载 "+prefix);
 		String request;
 		try {
 			request = Base64.encodeBytes(("{\"type\":1,\"songId\":\""+id+"\",\"common\":{\"clientversion\":\"1.9.3\",\"model\":\"sdk\",\"imei\":\"000000000000000\",\"userid\":0,\"resolution\":\"1196X720\",\"apiversion\":\"1.9.3\",\"product\":\"KALAOK\",\"clienttype\":\"Android\",\"nettype\":\"epc.tmobile.com\",\"updatechannel\":\"37\",\"login\":0,\"language\":1,\"imsi\":\"89014103211118510720\",\"systemversion\":\"17\",\"channel\":\"YYH\"},\"primeId\":\"20727\"}").getBytes("UTF-8"));
 			String sign=md5(request+"1731c73ef747457e8ac6f2ddb7de9227087e337ee96b4545b71edd50ea79d367");
 			String url="http://sns.audiocn.org/tlcysns/content/getSongUrl.action?request="+request+"&sign="+sign+"&type=52f78ffbda1e416e";
 			currconn=new HttpConnection(this,new GetSongHandler(this,prefix));
 			currconn.post(url, "");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 
 
 	private boolean fileReady(String prefix) {
 		return new File(getFinishTagLocalPath(prefix)).exists();
 	}
 
 
 
 	private void startControllerAsyncTask() {
 		UDPServerTask task=new UDPServerTask();
 		task.execute(4004);
 	}
 
 	protected void play(HashMap<String, String> map) {
 		LinearLayout l=(LinearLayout) findViewById(R.id.idcover);
 		l.setVisibility(View.INVISIBLE);
 		String[] paths=getLocalPath(map);
 		VideoView v=(VideoView) this.findViewById(R.id.idvideoview);
 		v.setVisibility(View.VISIBLE);
 		v.setOnPreparedListener(new OnPreparedListener(){
 			@Override
 			public void onPrepared(MediaPlayer mp) {
 			}
 		});
 		
 		/*
 		String path_adjusted=paths[0]+"_FFMPEG.mp4";
 		if (new File(path_adjusted).exists()){
 			v.setVideoPath(path_adjusted);
 		}
 		else{
 			naMain(this,paths[0],path_adjusted);
 			if (new File(path_adjusted).exists()){
 				v.setVideoPath(path_adjusted);
 			}
 			else{
 				v.setVideoPath(paths[0]);
 			}
 		}*/
 		v.setVideoPath(paths[0]);
 		
 		v.setOnCompletionListener(new OnCompletionListener(){
 			@Override
 			public void onCompletion(MediaPlayer arg0) {
 				Toast.makeText(MainActivity.this, "播放完毕", Toast.LENGTH_LONG).show();
 				playNext();
 			}
 		});
 		mPlayer.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener(){
 			@Override
 			public void onCompletion(MediaPlayer mp) {
 				Log.e("", "Audio play finished!");
 			}
 		});
 		isKalaOK=true;
 		try {
 			mPlayer.setDataSource(paths[2]);
 			mPlayer.prepare();
 	        mPlayer.start();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		v.start();		
 	}
 
 	private String[] getLocalPath(HashMap<String, String> map) {
 		FileInputStream fin;
 		try {
 			fin = new FileInputStream(this.getFinishTagLocalPath(this.getPrefixFromMap(map)));
 			BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
 		    String[] res=new String[3];
 		    res[0]= reader.readLine();		
 		    res[1]= reader.readLine();		
 		    res[2]= reader.readLine();
 		    //fix the path error when tp mini update
 		    res[0]=getBase()+getLastPart(res[0].split("/"));
 		    res[1]=getBase()+getLastPart(res[1].split("/"));
 		    res[2]=getBase()+getLastPart(res[2].split("/"));
 		    return res;
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 
 	private String getLastPart(String[] split) {
 		return split[split.length-1];
 	}
 
 
 
 	protected void changeSoundChannel(HashMap<String, String> map) {
 		mPlayer.stop();
 		mPlayer.reset();
 		String[] paths=getLocalPath(map);
 		try {
 			isKalaOK=!isKalaOK;
 			if (isKalaOK){
 				mPlayer.setDataSource(paths[2]);
 			}
 			else{
 				mPlayer.setDataSource(paths[1]);
 			}
 			VideoView v=(VideoView) findViewById(R.id.idvideoview);
 			mPlayer.prepare();
 			v.pause();
 			SystemClock.sleep(500);
 			mPlayer.seekTo((int) v.getCurrentPosition());
 			v.start();
 			mPlayer.start();
 			
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 }
