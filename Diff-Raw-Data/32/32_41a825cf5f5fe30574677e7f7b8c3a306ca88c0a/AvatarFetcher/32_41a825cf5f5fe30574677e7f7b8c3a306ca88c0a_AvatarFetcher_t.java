 package com.connectsy.data;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.apache.http.util.ByteArrayBuffer;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Handler;
 import android.util.Log;
 import android.widget.ImageView;
 
 import com.connectsy.R;
 import com.connectsy.settings.Settings;
 
 public class AvatarFetcher{
 	@SuppressWarnings("unused")
 	private final static String TAG = "AvatarFetcher";
 	
     // singleton!
     private static AvatarFetcher instance = new AvatarFetcher();
 	private AvatarFetcher(){
 		for (int i=0;i<threadPool;i++) threadsAvailable[i] = true;
		File f = new File(avatarPath);
		if (!f.exists()) f.mkdir();
 	}
 
 	private final Handler handler = new Handler();
     private final int threadPool = 2;
     private final DownloadThread[] threads = new DownloadThread[threadPool];
     private final boolean[] threadsAvailable = new boolean[threadPool];
     private final int expiry = 1000 * 60 * 60; // 1 hour
     
     private static final String avatarPath = 
		"/data/data/com.connectsy/avatars/";
     private HashMap<String, ArrayList<ImageView>> q = 
     	new HashMap<String, ArrayList<ImageView>>();
     private HashMap<String, Integer> usernameOnThread = 
     	new HashMap<String, Integer>();
     
 	public static void download(String username, final ImageView view, 
 			boolean force){
 		File f = new File(avatarPath+username+".png");
 		if ((f.exists() && 
 				(new Date().getTime() - f.lastModified()) < instance.expiry) 
 				&& !force){
 			Log.d(TAG, username+" loading cached");
 			instance.useFile(username, view);
 		}else{
 			Log.d(TAG, username+" qed up");
 			if (!instance.q.containsKey(username))
 				instance.q.put(username, new ArrayList<ImageView>());
 			instance.q.get(username).add(view);
 			instance.runIfReady();
 		}
 	}
 	
 	private class DownloadThread extends Thread{
 		private String username;
 		
 		public DownloadThread(String username){
 			this.username = username;
 		}
 
 		public void run() {
 			Log.d(TAG, username+" downloading");
 			try {
 				
 				URL url = new URL(
 						Settings.API_DOMAIN+"/users/"+username+"/avatar/");
 				URLConnection con = url.openConnection();
 				InputStream in;
 				try {
 					in = con.getInputStream();
 				} catch (FileNotFoundException e){
 					image404();
 					return;
 				}
 				BufferedInputStream bis = new BufferedInputStream(in);
 				ByteArrayBuffer baf = new ByteArrayBuffer(50);
 				int current = 0;
 	            while ((current = bis.read()) != -1)
                     baf.append((byte) current);
 	            
 	            if (baf.isEmpty()){
 					image404();
 					return;
 	            }
 	            
 				File file = new File(avatarPath+username+".png");
 				if (file.exists()) file.delete();
 				file.createNewFile();
 				
 	            FileOutputStream fos = new FileOutputStream(file);
 	            fos.write(baf.toByteArray());
 	            fos.close();
 	            in.close();
 			
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			handler.post(new Runnable() {
 		        public void run() { 
 		        	instance.onDownloadFinish(username);
 	        	} 
 		    });
         }
 		
 		private void image404(){
 			handler.post(new Runnable() {
 		        public void run() { 
 		        	instance.on404(username);
 	        	} 
 		    });
 		}
 	};
 
 	private void runIfReady() {
 		if (q.size() == 0) return;
 		for (int i=0;i<threadPool;i++){
 			if (threads[i] == null){
 				String username = (String) q.keySet().toArray()[0];
 				usernameOnThread.put(username, i);
 				threads[i] = new DownloadThread(username);
 				threads[i].start();
 				break;
 			}
 		}
 	}
 
 	private void onDownloadFinish(String username) {
 		Log.d(TAG, username+" dl finished");
 		if (q.containsKey(username)){
 			for (ImageView v: q.get(username)){
 				useFile(username, v);
 			}
 			q.remove(username);
 			threads[usernameOnThread.get(username)] = null;
 		}
 		runIfReady();
 	}
 
 	private void on404(String username) {
 		Log.d(TAG, username+" 404");
 		if (q.containsKey(username)){
 			for (ImageView v: q.get(username)){
 				v.setImageResource(R.drawable.avatar_default);
 			}
 			q.remove(username);
 			threads[usernameOnThread.get(username)] = null;
 		}
 		runIfReady();
 	}
 	
 	private void useFile(String username, ImageView v){
 		if (v != null){
 			Log.d(TAG, username+" loading into view");
 			Bitmap b = BitmapFactory.decodeFile(avatarPath+username+".png");
 			v.setImageDrawable(new BitmapDrawable(b));
 		}
 	}
 }
