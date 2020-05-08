 package com.kukung.app;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URL;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.kukung.app.model.TOC;
 import com.kukung.app.model.TOCItem;
 import com.kukung.app.model.TocAdapter;
 
 public class ViewTocActivity extends Activity implements OnItemClickListener {
 	private static Context mainContext;
 	private static List<TOCItem> tocItems;
 	private static int backpress = 0;
 	private ProgressDialog progressDialog;
 	private String notice;
 
 	private Handler handler = new Handler() { 
 		public void handleMessage(Message msg) { 
 			progressDialog.dismiss();
 			if(msg != null && msg.getData() != null) {
 				String fileId = msg.getData().getString("fileId");
 				if (fileId != null) {
 					showNotePages(fileId);
 				} else {
					Toast.makeText(ViewTocActivity.mainContext, "선택된 파일 정보가 없습니다.",
 							Toast.LENGTH_LONG).show();
 				}
 			} else {
				Toast.makeText(ViewTocActivity.mainContext, "예상되지 않은 상황입니다.", 
 						Toast.LENGTH_LONG).show();
 			}
 		} 
 	};
 	
 	@Override
 	public void onBackPressed(){
 		backpress = (backpress + 1);
 		if (backpress == 1) {
			Toast.makeText(getApplicationContext(), "Back 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
 		} else {
 			if (backpress > 1) {         
 				this.finish();     
 			}
 			backpress = 0;
 		}
 	} 
 	
 	private void showNotePages(String fileId) {
 		Intent wannaViewContents = new Intent(this, PageImageViewActivity.class);
 		wannaViewContents.putExtra(PageImageViewActivity.EXTRA_KEY_PAGE_ID, fileId);
 		startActivity(wannaViewContents);
 	}
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mainContext = this;
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         
         if (tocItems == null) {
         	tocItems = TOC.buildTOC(this);
         }
         
         notice = getNoticeFromServer();
         if (notice.equals(getLastNotice())) {
         	notice = "";
         }
 	}
     
     private String getNoticeFromServer() {
     	StringBuffer noticeBuffer = new StringBuffer();
     	BufferedReader in = null;
     	try {
 			HttpClient client = new DefaultHttpClient();
 			HttpGet request = new HttpGet();
 			request.setURI(new URI("http://unja66.woobi.co.kr/pk111/notice.txt"));
 			HttpResponse response = client.execute(request);
 			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 			StringBuffer sb = new StringBuffer("");
 			String line = "";
 			String NL = System.getProperty("line.separator");
 			while ((line = in.readLine()) != null) {
 				if (line.trim().equals("") == false) {
 					noticeBuffer.append(line);
 				}
 			}
 			in.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
     	
     	return noticeBuffer.toString();
 	}
 
 	private void updateNoticeReadHistory() {
 		SharedPreferences pref = getSharedPreferences("noticePref", Activity.MODE_PRIVATE);
     	SharedPreferences.Editor editor = pref.edit();
     	editor.putString("notice", notice);
     	editor.commit();
 	}
 	
 	private String getLastNotice() {
 		SharedPreferences prefs = getSharedPreferences("noticePref", Activity.MODE_PRIVATE);
 		String notice = prefs.getString("notice", "");
 		return notice;
 	}
 
 	private void setupTOCListView() {
     	TocAdapter listItemAdapter = new TocAdapter(this, R.layout.toc_item, tocItems);
     	ListView tocListView = (ListView)this.findViewById(R.id.tocListView);
     	tocListView.setAdapter(listItemAdapter);
     	tocListView.setOnItemClickListener(this);
 	}
 
 	@Override
     public void onResume() {
     	super.onResume();
     	backpress = 0;
     	setContentView(R.layout.toc);
         setupTOCListView();
         showNotice();
     }
 
 	private void showNotice() {
 		if (notice == null || notice.trim().equals("")) {
 			return;
 		}
 		
 		new AlertDialog.Builder(this)
			.setTitle("알림")
 			.setMessage(notice)
			.setPositiveButton("확인", new DialogInterface.OnClickListener() {
 		        public void onClick(DialogInterface dialog, int which) { 
 		        	dialog.dismiss();
 		        	updateNoticeReadHistory();
 		        }
 		     }).show();
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parentView, final View view, int position, long id) {
 		final TOCItem clickedItem = tocItems.get(position); 
 		
 		if (clickedItem.hasPageImage()) {
 			((TextView)view.findViewById(R.id.tocTitle)).setBackgroundColor(Color.MAGENTA);
			progressDialog = ProgressDialog.show(this, "", "이미지를 읽어들입니다.", true);
 			Thread thread = new Thread(new Runnable() { 
 	            public void run() {
 	            	String fileId = clickedItem.getFileId();
 	            	URL imageUrl = clickedItem.getImagUrl();
 	            	
 	            	if (TOC.loadImage(fileId, imageUrl)) {
 	            		Message message = new Message();
 	            		Bundle bundle = new Bundle();
 	            		bundle.putString("fileId", fileId);
 	            		message.setData(bundle);
 	            		handler.sendMessage(message); 
 	            	} else {
 	            		handler.sendEmptyMessage(0);
 	            	}
 	            } 
 	        }); 
 	        thread.start(); 
 		} 
 	}
 	
 }
