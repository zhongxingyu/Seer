 package com.fonenet.fonemarket.adapter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * @author chenzheng_java
  * @description ĲʵģSimpleAdapter
  */
 public class MyAdapter extends BaseAdapter {
 
 
 	private ArrayList<HashMap<String, Object>> data;
 	/**
 	 * LayoutInflater ǴʵлȡļҪʽ LayoutInflater layoutInflater =
 	 * LayoutInflater.from(context); View convertView =
 	 * layoutInflater.inflate();
 	 * LayoutInflaterʹ,ʵʿLayoutInflater໹Ƿǳõ, findViewById(),
 	 * ͬLayoutInflaterlayoutxmlļʵ findViewById()Ҿxmlµľ
 	 * widgetؼ(:Button,TextView)
 	 */
 	private LayoutInflater layoutInflater;
 	private Context context;
 
 	public MyAdapter(Context context, ArrayList<HashMap<String, Object>> data) {
 
 		this.context = context;
 		this.data = data;
 		this.layoutInflater = LayoutInflater.from(context);
 	}
 
 	/**
 	 * ȡ
 	 */
 	public int getCount() {
 		return data.size();
 	}
 
 	/**
 	 * ȡĳһλõ
 	 */
 	public Object getItem(int position) {
 		return data.get(position);
 	}
 
 	/**
 	 * ȡΨһʶ
 	 */
 	public long getItemId(int position) {
 		return position;
 	}
 
 	/**
 	 * androidÿһеʱ򣬶
 	 */
 	public View getView(final int position, View convertView, ViewGroup parent) {
 		ZuJian zuJian = null;
 		if (convertView == null) {
 			zuJian = new ZuJian();
 			// ȡ
 			convertView = layoutInflater.inflate(R.layout.activity_tab1, null);
 			zuJian.imageView = (ImageView) convertView.findViewById(R.id.image);
 			zuJian.titleView = (TextView) convertView.findViewById(R.id.title);
 			zuJian.infoView = (TextView) convertView.findViewById(R.id.info);
 			zuJian.button = (Button) convertView.findViewById(R.id.view_btn);
 			zuJian.url = (String) data.get(position).get("url");
 			// Ҫע⣬ʹõtag洢ݵġ
 			convertView.setTag(zuJian);
 		} else {
 			zuJian = (ZuJian) convertView.getTag();
 		}
 		// ݡԼ¼
 
 		zuJian.imageView.setBackgroundResource((Integer) data.get(position)
 				.get("image"));
 		zuJian.titleView.setText((String) data.get(position).get("title"));
 		zuJian.infoView.setText((String) data.get(position).get("info"));
 		/*
 		 * zuJian.button.setOnClickListener(new OnClickListener(){
 		 * 
 		 * public void onClick(View v) { int p = position; String z =
 		 * (String)data.get(position).get("title");
 		 * 
 		 * 
 		 * showInfo(p);
 		 * 
 		 * // download file new Thread(){ public void run(){ //try {
 		 * http://192.168.7.76:8080/glxt/ HttpDownloader downloader = new
 		 * HttpDownloader(uiHandler); int lrc =
 		 * downloader.downFile("http://192.168.7.66/Market3.apk"
 		 * ,"test/","",FoneConstValue.FILE_TYPE_STORE_APP); // int lrc =
 		 * downloader.downFile(
 		 * "http://192.168.7.76:8080/glxt/interface/usstore.jsp?projectid=76&type=3&date=0&version=1&imsi=9001010123456789"
 		 * ,"test/","",FoneConstValue.FILE_TYPE_STORE_APP);
 		 * System.out.println(lrc); //ļһURLڶ· // } catch
 		 * (ClientProtocolException e) { // TODO Auto-generated catch block //
 		 * e.printStackTrace(); // } catch (IOException e) { // TODO
 		 * Auto-generated catch block // e.printStackTrace(); // } } }.start();
 		 * {
 		 * 
 		 * String urlstr = "http://192.168.7.66/Market4.apk"; // String
 		 * localfile = SD_PATH + "test/okdown.apk"; // ߳Ϊ4 int threadcount
 		 * = 1; // ʼһdownloader // Downloader downloader =
 		 * downloaders.get(urlstr); // if (downloader == null) { Downloader
 		 * downloader = new Downloader(urlstr, "/mnt/sdcard/test/oktest.apk", 1,
 		 * context,uiHandler); // downloaders.put(urlstr, downloader); // } if
 		 * (downloader.isdownloading()) return; // õϢĸɼ LoadInfo
 		 * loadInfo = downloader.getDownloaderInfors(); // ʾ //
 		 * showProgress(loadInfo, urlstr, v); // ÷ʼ downloader.download();
 		 * }
 		 * 
 		 * //
 		 * installApk(Environment.getExternalStorageDirectory()+"/test/Market.apk"
 		 * ); }
 		 * 
 		 * });
 		 */
 		return convertView;
 	}
 
 	/**
 	 * ûťʱ¼ᵯһȷ϶Ի
 	 */
 	public void showInfo(int pos) {
 
 		new AlertDialog.Builder(context)
 
 		.setTitle("ҵlistview")
 
 		.setMessage("..." + pos)
 
 		.setPositiveButton("ȷ", new DialogInterface.OnClickListener() {
 
 			public void onClick(DialogInterface dialog, int which) {
 
 			}
 
 		})
 
 		.show();
 
 	}
 
 }
