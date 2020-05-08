 package net.wesley.gitadroia;
 
 import java.io.BufferedOutputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.InputStreamEntity;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ContentBody;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.CoreProtocolPNames;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.HorizontalScrollView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class SearchActivity extends Activity {
 	
 	ArrayList<String> maybeList =new ArrayList<String>();
 	ArrayList<String> confirmList =new ArrayList<String>();
 	
 	ArrayList<HashMap<String,String>> remoteList =new ArrayList<HashMap<String,String>>();
 	
 	ProgressDialog progressDialog;
 	
 	
 	class DownloadAsync extends  AsyncTask<String,Integer,String>{
 		@Override
 		protected String doInBackground(String... arg0) {
 			try {
 				HttpClient httpclient = new DefaultHttpClient();
 
 				HttpContext localContext = new BasicHttpContext();
 				HttpGet httpPost = new HttpGet("http://183.129.206.109/getgita.php?id="+arg0[0]);
 
 				HttpResponse response = httpclient.execute(httpPost, localContext);
 				
 				HttpEntity resEntity = response.getEntity();
 
 				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(getApplicationContext().getFilesDir(),arg0[0]+".gita")),10*1024*1024);
 				InputStream in=resEntity.getContent();
 				int read=0;
 				byte[] bytes = new byte[1024];
 				int downloadedSize = 0;
 				long totalSize=resEntity.getContentLength();
 				while((read = in.read(bytes))!= -1){
 					out.write(bytes, 0, read);
 					downloadedSize+=read;
 					if (totalSize<=0){
 						 publishProgress(-1);
 					}
 					else{
 						 publishProgress((int) ((downloadedSize*100)/totalSize));
 					}
 				}
 				in.close();
 				out.flush();
 				out.close();
 
 				if (resEntity != null) {
 					resEntity.consumeContent();
 				}
 
 				httpclient.getConnectionManager().shutdown();
 				return "ok"; 
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return "";
 		}
 		
 		@Override
 		protected void onProgressUpdate(Integer... progress){
 			if (progress[0]>=0){
 				progressDialog.setProgress(progress[0]);
 			}
 			else{ 
 				progressDialog.setIndeterminate(true);
 			}
			Log.e("", ""+progress[0]);
 		}
 		
 		@Override 
 		protected void onPostExecute(String result){
 			if (result.length()>0){
 				Toast.makeText(SearchActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
 				progressDialog.dismiss();
 				finish();
 			}
 		}
 	}
 	
 	class RemoteSearchAsyncTask extends AsyncTask<String,Void,String>{
 		@Override
 		protected String doInBackground(String... arg0) {
 			try {
 				return getUrl("http://183.129.206.109/searchgita.php?song="+arg0[0]);
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			return "";
 		}
 		@Override 
 		protected void onPostExecute(String result){
 			if (result.length()>0){
 				JSONObject p=null;
 				try{
 					p=new JSONObject(result);
 					String resp=(String) p.get("response");
 					if (!resp.equals("ok")){
 						Toast.makeText(SearchActivity.this, resp, Toast.LENGTH_SHORT).show();
 					}
 					else{
 						JSONArray songs = p.getJSONArray("songs");
 						remoteList.clear();
 						for (int i=0;i<songs.length();i++){
 							JSONObject song=songs.getJSONObject(i);
 							HashMap<String,String> map=new HashMap<String,String>();
 							map.put("id", song.getString("id"));
 							map.put("song", song.getString("song"));
 							map.put("singer", song.getString("singer"));
 							remoteList.add(map);
 						}
 						updateRemoteListView();
 					}
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 		}		
 	}
 	
 	class MyPostTask extends AsyncTask<String,Void,String>{
 		@Override
 		protected String doInBackground(String... arg0) {
 			try {
 				return post(arg0[0],arg0[1],arg0[2],"http://183.129.206.109/uploadgita.php");
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			return "Error on client side!";
 		}
 		
 		@Override 
 		protected void onPostExecute(String result){
 			if (result.length()>0){
 				File file=new File(getApplicationContext().getCacheDir(),"preview.jita");
 				file.renameTo(new File(getApplicationContext().getFilesDir(),result+".gita"));
 				Toast.makeText(SearchActivity.this, "创建完成!", Toast.LENGTH_SHORT).show();
 				finish();
 			}
 		}		
 	}
 	
 	@SuppressLint("SetJavaScriptEnabled")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.searchlayout);
 		progressDialog = new ProgressDialog(this);
 		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 		
 		if (savedInstanceState!=null){
 			confirmList.clear();
 			String[] values = savedInstanceState.getStringArray("confirm");
 			if (values!=null){
 				for (String v:values){
 					confirmList.add(v);
 				}
 			}
 		}
 		
 		try{
 			File file=new File(getApplicationContext().getCacheDir(),"preview.jita");
 			file.delete();
 		}
 		catch (Exception e){
 			
 		}
 
 		WebView web=(WebView) findViewById(R.id.webview);
 		web.getSettings().setJavaScriptEnabled(true);  
 		web.setWebViewClient(new WebViewClient());
 		
 		
 		((Button)findViewById(R.id.createinsearch)).setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				TextView song=(TextView) findViewById(R.id.songinsearch);
 				TextView singer=(TextView) findViewById(R.id.singerinsearch);
 				File file=new File(getApplicationContext().getCacheDir(),"preview.jita");
 				MyPostTask task=new MyPostTask();
 				task.execute(file.getAbsolutePath(),song.getText().toString(),singer.getText().toString());
 			}
 		});		
 		
 		((Button)findViewById(R.id.searchinsearch)).setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				TextView text=(TextView) findViewById(R.id.textinsearch);
 
 				TextView song=(TextView) findViewById(R.id.songinsearch);
 				if (song.getText().toString().length()==0){
 					song.setText(text.getText().toString());
 				};
 				
 				RemoteSearchAsyncTask task=new RemoteSearchAsyncTask();
 				task.execute(text.getText().toString());
 				
 				String url="http://www.baidu.com/s?wd=吉他谱+"+text.getText().toString();
 				WebView web=(WebView) findViewById(R.id.webview);
 				clearCacheFolder(getApplicationContext().getCacheDir());
 				web.clearCache(true);
 				web.loadUrl(url);
 
 				InputMethodManager imm = (InputMethodManager)getSystemService(
 						Context.INPUT_METHOD_SERVICE);
 				imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
 
 			}
 		});
 
 		((Button)findViewById(R.id.previewinsearch)).setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				TextView song=(TextView) findViewById(R.id.songinsearch);
 				TextView singer=(TextView) findViewById(R.id.singerinsearch);
 				
 				if (song.getText().toString().trim().length()<=0){
 					new AlertDialog.Builder(SearchActivity.this).setTitle("错误!")
 						.setMessage("歌曲名字不能为空!")
 						.setPositiveButton("确定", null).create().show();
 					return;
 				}
 
 				if (singer.getText().toString().trim().length()<=0){
 					new AlertDialog.Builder(SearchActivity.this).setTitle("错误!")
 						.setMessage("演唱者的名字不能为空!")
 						.setPositiveButton("确定", null).create().show();
 					return;
 				}
 				
 				
 				File file=new File(getApplicationContext().getCacheDir(),"preview.jita");
 				//File file=new File("/sdcard/preview.jita");
 				try {
 					DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
 					out.writeUTF(song.getText().toString());
 					out.writeUTF(singer.getText().toString());
 					byte[] buf = new byte[1024];
 					int len;
 					for (String path:confirmList){
 						InputStream in = new FileInputStream(path);
 						out.writeInt(in.available());
 						while ((len = in.read(buf)) > 0){
 							out.write(buf, 0, len);
 						}
 						in.close();
 					}
 					out.close();
 					
 					Intent i=new Intent(SearchActivity.this,ShowActivity.class);
 					i.putExtra("path", file.getAbsolutePath());
 					startActivity(i);
 					
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 
 		
 		((Button)findViewById(R.id.cacheinsearch)).setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				listCacheFile();
 			}
 		});
 		
 		
 		ListView lv=(ListView) findViewById(R.id.listinsearch);
 		lv.setOnItemClickListener(new OnItemClickListener(){
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				String song=remoteList.get(arg2).get("song");
 				String[] choices={"下载吉他谱:"+song}; 
 				final int position=arg2;
 				new AlertDialog.Builder(SearchActivity.this).setItems(choices, new DialogInterface.OnClickListener(){
 					@Override
 					public void onClick(DialogInterface dialog, int which) { 
 						DownloadAsync task=new DownloadAsync();
 						task.execute(remoteList.get(position).get("id"));
 						progressDialog.setTitle("提示");
 						progressDialog.setMessage("正在下载...");
 						progressDialog.setProgress(100);
 						progressDialog.setIndeterminate(false);
 						progressDialog.setCancelable(false);
 						progressDialog.show();
 					}
 					
 				})
 				.create().show();
 			}
 		});
 		
 	}
 	
 	
 	public void updateRemoteListView() {
 		ListView lv=(ListView) findViewById(R.id.listinsearch);
 		lv.setAdapter(new SimpleAdapter(this,
 				remoteList, R.layout.songrow,
 				new String[] { "song", "singer"}, new int[] {
 						R.id.listrow_name, R.id.listrow_artist}));
 	}
 
 	public String getUrl(String urlServer) throws ClientProtocolException, IOException, JSONException {
 		String json="";
 		HttpClient httpclient = new DefaultHttpClient();
 
 		HttpContext localContext = new BasicHttpContext();
 		HttpGet httpPost = new HttpGet(urlServer);
 
 		HttpResponse response = httpclient.execute(httpPost, localContext);
 		
 		HttpEntity resEntity = response.getEntity();
 
 		System.out.println(response.getStatusLine());//通信Ok
 		if (resEntity != null) {
 			//System.out.println(EntityUtils.toString(resEntity,"utf-8"));
 			json=EntityUtils.toString(resEntity,"utf-8");
 		}
 		if (resEntity != null) {
 			resEntity.consumeContent();
 		}
 
 		httpclient.getConnectionManager().shutdown();
 		return json;
 	} 
 
 	
 	public String post(String pathToOurFile,String song,String singer,String urlServer) throws ClientProtocolException, IOException, JSONException {
 		HttpClient httpclient = new DefaultHttpClient();
 
 		HttpContext localContext = new BasicHttpContext();
 		HttpPost httpPost = new HttpPost(urlServer);
 
 		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 
 		entity.addPart("file", new FileBody(new File (pathToOurFile)));
 		entity.addPart("song", new StringBody(song,Charset.forName("UTF-8")));
 		entity.addPart("singer", new StringBody(singer,Charset.forName("UTF-8")));
 
 		httpPost.setEntity(entity);
 
 		HttpResponse response = httpclient.execute(httpPost, localContext);
 		
 		HttpEntity resEntity = response.getEntity();
 
 		System.out.println(response.getStatusLine());//通信Ok
 		String json="";
 		String path="";
 		if (resEntity != null) {
 			//System.out.println(EntityUtils.toString(resEntity,"utf-8"));
 			json=EntityUtils.toString(resEntity,"utf-8");
 			Log.e("", "http post return:"+json);
 			JSONObject p=null;
 			try{
 				p=new JSONObject(json);
 				path=(String) p.get("id");
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 		if (resEntity != null) {
 			resEntity.consumeContent();
 		}
 
 		httpclient.getConnectionManager().shutdown();
 		return path;
 	} 
 
 	@Override 
 	public void onRestoreInstanceState(Bundle savedInstanceState) { 
 		super.onRestoreInstanceState(savedInstanceState);
 		confirmList.clear();
 		String[] values = savedInstanceState.getStringArray("confirm");
 		if (values!=null){
 			for (String v:values){
 				confirmList.add(v);
 			}
 		}
 	}
 	
 	@Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
 		savedInstanceState.putStringArrayList("confirmlist", confirmList);
 	}
 	
 	protected void listCacheFile() {
 		maybeList.clear();
 		File base=this.getApplicationContext().getCacheDir();
 		listdir(base);
 		if (maybeList.size()<0){
 			Toast.makeText(this, "没有找到任何可能谱子图片！", Toast.LENGTH_LONG).show();
 		}
 		else{
 			showChooseDialog();
 		}
 	}
 	
 	static int clearCacheFolder(final File dir) {
 
 	    int deletedFiles = 0;
 	    if (dir!= null && dir.isDirectory()) {
 	        try {
 	            for (File child:dir.listFiles()) {
 
 	                //first delete subdirectories recursively
 	                if (child.isDirectory()) {
 	                    deletedFiles += clearCacheFolder(child);
 	                }
 
 	                //then delete the files and subdirectories in this dir
 	                //only empty directories can be deleted, so subdirs have been done first
 	                if (child.delete()) {
 	                	deletedFiles++;
 	                }
 	            }
 	        }
 	        catch(Exception e) {
 	            Log.e("", String.format("Failed to clean the cache, error %s", e.getMessage()));
 	        }
 	    }
 	    return deletedFiles;
 	}
 
 	private void showChooseDialog() {
 		
 		confirmList.clear();
 		
 		HorizontalScrollView hsv=new HorizontalScrollView(this);
 		LinearLayout l=new LinearLayout(this);
 		l.setOrientation(LinearLayout.HORIZONTAL);
 		
 		final AlertDialog dialog = new AlertDialog.Builder(this)
 		.setTitle("请依次选择正确的图片：")
 		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface arg0, int arg1) {
				updateConfirmView();
 			}
 		})
 		.setNegativeButton("取消", null)
 		.setView(hsv)
 		.create();
 		
 		for (String path:maybeList){
 			ImageView iv=new ImageView(this);
 			iv.setImageBitmap(BitmapFactory.decodeFile(path));
 			iv.setTag(path);
 			iv.setOnClickListener(new OnClickListener(){
 				@Override
 				public void onClick(View arg0) {
 					LinearLayout l= (LinearLayout) arg0.getParent();
 					l.removeView(arg0);
 					confirmList.add((String) arg0.getTag());
 					if (l.getChildCount()==0){
 						dialog.dismiss();
						updateConfirmView();
 					}
 				}
 			});
 			l.addView(iv);
 		}
 		hsv.addView(l);
 		
 		dialog.show();
 			
 	}
 
 	protected void updateConfirmView() {
 		LinearLayout l=(LinearLayout) findViewById(R.id.gallerylayoutinsearch);
 		l.removeAllViews();
 		for (String path:confirmList){
 			ImageView iv=new ImageView(this);
 			iv.setTag(path);
 			iv.setOnClickListener(new OnClickListener(){
 				@Override
 				public void onClick(View v) {
 					String path=(String) v.getTag();
 					ImageView iv=new ImageView(SearchActivity.this);
 					iv.setImageBitmap(BitmapFactory.decodeFile(path));
 					new AlertDialog.Builder(SearchActivity.this)
 						.setView(iv)
 						.setPositiveButton("确定", null)
 						.create().show();
 				}
 			});
 			iv.setImageBitmap(BitmapFactory.decodeFile(path));
 			l.addView(iv);
 		}
	}
 
 	private void listdir(File base) {
 		Log.e("", "list dir:"+base.getAbsolutePath());
 		File[] files = base.listFiles();
 		for (File f:files){
 			if (f.isDirectory()){
 				listdir(f);
 			}
 			else{
 				Log.e("", "list file:"+f.getAbsolutePath());
 				try{
 					Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
 					if ((bmp.getWidth()>400) && (bmp.getHeight()>300)){
 						Log.e("", "we may find something good: width:"+bmp.getWidth()+" height:"+bmp.getHeight());
 					}
 					maybeList.add(f.getAbsolutePath());
 					bmp.recycle();
 				}
 				catch (Exception e){
 					
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onBackPressed(){
 		WebView web=(WebView) findViewById(R.id.webview);
 		if (web.canGoBack()){
 			web.goBack();
 		}
 		else{
 			finish();
 		}
 	}
 
 	
 }
