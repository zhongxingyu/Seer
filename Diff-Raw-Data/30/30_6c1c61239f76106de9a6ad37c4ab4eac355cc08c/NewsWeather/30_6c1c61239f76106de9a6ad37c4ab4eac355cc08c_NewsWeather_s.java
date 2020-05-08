 package com.newsweather;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.util.Log;
 import android.util.Xml;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.View.OnTouchListener;
 import android.webkit.WebView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.HorizontalScrollView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class NewsWeather extends Activity implements OnTouchListener,OnClickListener,OnLongClickListener {
 	
 	//宣告最上面的5個新聞標題按鈕
 	private Button button;
 	private ListView llv1,llv2,llv3,llv4;//先初始化4個ListView
 	private HorizontalScrollView slv;//為了讓下面的新聞欄可以左右滑動，需把HorizontalScrollView宣告出來
 	double getend,getstart=0;//用來存放手勢的第1個值和最後一個值，好比較是往前或往後滑
 	private List<News> li1,li2,li3,li4 = new ArrayList<News>();//容器
 	String Encode;  //判斷xml文件編碼並儲存在Encode
 	String bufferb;  //bufferb用來存放從xml複製下來，每一行從BIG5轉成UTF-8的String空間
 //	private HashMap<String,String> path;//存放網址路徑的容器
 	public ProgressDialog myDialog;  //資料載入中的等待視窗
 	File file;//用來檢查資料庫在不在
 	
 	
 	String path1="http://tw.news.yahoo.com/rss/realtime";//雅虎UTF-8	
 	String path2="http://www.cw.com.tw/RSS/cw_content.xml";//天下雜誌BIG5	
 	String path3="http://rss.chinatimes.com/rss/focus-u.rss"; //中時UTF-8
 //	String path4="http://www.zdnet.com.tw/rss/news_daily.htm";  ////中時BIG5
 //	String path="http://tw.nextmedia.com/rss/create/type/1077";//蘋果utf8
 //	String path="http://inews.mingpao.com/rss/INews/gb.xml";//明報BIG5
 //	String path="http://www.lib.ntu.edu.tw/rss/newsrss.xml";//台灣大學圖書館UTF8
 //	String path="http://www.ait.org.tw/zh/press-releases.rss";//美國在台協會UTF8→有編碼的問題(來源檔有亂碼)
 //	String path="http://acq.lib.nttu.edu.tw/RSS/RSS_NB.asp";//台東大學圖書館BIG5
 	String path4="http://www.thb.gov.tw/tm/Menus/Menu04/Trss/rss1_xml.aspx";//交通部公路總局UTF8
	int newscount;//這個值用來計算總共有幾篇新聞
 	int frequently;
 	int nowview=1;
 	private Handler handler,handler2;
 	private DB myDB;
 	private Cursor cursor;
 	String name,path;//將資料庫的name,path,int存到hashmap用的變數
 	int id;
 	
 	LinearLayout up_layout,down_layout;
 	private HashMap<Integer,String> namelist;
 	
 	
 	private void getDefaultData(){
 		myDB = new DB(this);
 	      myDB.insert("yahoo", "http://tw.news.yahoo.com/rss/realtime",false);
 	      myDB.insert("cw", "http://www.cw.com.tw/RSS/cw_content.xml",true);
 	      myDB.insert("chinatimes", "http://rss.chinatimes.com/rss/focus-u.rss",true);
 	      myDB.insert("thb", "http://www.thb.gov.tw/tm/Menus/Menu04/Trss/rss1_xml.aspx",true);
 		myDB.close();
 		
 	}
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         Log.i("startProgress", "start");
       
         //如果沒有資料庫，才建立預設資料
         File file = new File("/data/data/com.newsweather/databases/database.db");
         if(!file.exists())getDefaultData();//取得預設的新聞資料
         Log.i("checkdatabase", "pass");
 
 //        progressDialog();
         
         
       /*  
         Thread brother = new Thread(){
         	public void run(){
         		
         		
         	path=new HashMap<String,String>();
         	path.put("path1", "http://tw.news.yahoo.com/rss/realtime");
         	path.put("path2", "http://www.cw.com.tw/RSS/cw_content.xml");
         	path.put("path3", "http://rss.chinatimes.com/rss/focus-u.rss");
         	path.put("path4", "http://www.thb.gov.tw/tm/Menus/Menu04/Trss/rss1_xml.aspx");
         
         	Log.i("loadpath", "startToLoadData");
 //        	try{	
 	            for(int i =1; i<5;i++){
 	            	Log.i("foreach", String.valueOf(i));
 	            	frequently=i;
 	            		Log.i("intoforloop", "this is " +frequently+" time to do this.");
 	            		checkEncode(path.get("path"+frequently));  //先判斷xml的編碼格式
 	                    encodeTransfer(path.get("path"+frequently));  //進行big5→utf-8轉碼
 	            	
 	                    switch(frequently){
 	                    	case 1:
 	                    		li1 = getRss();  //將複製並轉碼完的檔案做解析，並存放到li容器裡   
 	                    		break;
 	                    	case 2:
 	                    		li2 = getRss();  //將複製並轉碼完的檔案做解析，並存放到li容器裡   
 	                    		break;
 	                    	case 3:
 	                    		li3 = getRss();  //將複製並轉碼完的檔案做解析，並存放到li容器裡   
 	                    		break;
 	                    	case 4:
 	                    		li4 = getRss();  //將複製並轉碼完的檔案做解析，並存放到li容器裡   
 	                    		break;
 	                    }
 	            }
 //            }catch(Exception e){
 //            	new AlertDialog.Builder(NewsWeather.this)
 //				.setMessage("請確認您的網路連線...")
 //				.setTitle("出錯了")
 //				
 //				.setPositiveButton("確認", new DialogInterface.OnClickListener() {
 //					
 //					@Override
 //					public void onClick(DialogInterface dialog, int which) {
 //						NewsWeather.this.finish();	
 //						
 //					}
 //				})
 //				.show();
 //            	Log.i("Exception",e.getMessage());
             	
             	
 //            }
 */                           
             
         	    	
      /*   button_foucs = (Button) findViewById(R.id.button_focus);
         button_tech = (Button) findViewById(R.id.button_tech);
         button_sports = (Button) findViewById(R.id.button_sports);
         button_relax = (Button) findViewById(R.id.button_relax);
        
         llv1 = (ListView) findViewById(R.id.list);
         llv2 = (ListView) findViewById(R.id.list2);
         llv3 = (ListView) findViewById(R.id.list3);
         llv4 = (ListView) findViewById(R.id.list4);
         slv = (HorizontalScrollView) findViewById(R.id.hsv);*/
       /* 
         //寄簡訊請主執行緒的Handler繼續處理事件
         Message message = handler.obtainMessage(1);
         handler.sendMessage(message);
         Log.i("handlestatus", "endsendmess");
         	}
     };
     Log.i("startbrother", "start");
     brother.start();*/
    /* 
     handler = new Handler(){
     	
 	    @Override 
 	    public void handleMessage(Message msg){ 
 	    	switch(msg.what){
 	    		
 	    		case 1:*/
 	    	/*slv.setOnTouchListener(NewsWeather.this);*/
 	        //設定ListView的樣版和文字來源
 	      /*  llv1.setAdapter(new NewsAdapter(NewsWeather.this,li1));
 	        llv2.setAdapter(new NewsAdapter(NewsWeather.this,li2));
 	        llv3.setAdapter(new NewsAdapter(NewsWeather.this,li3));
 	        llv4.setAdapter(new NewsAdapter(NewsWeather.this,li4));*/
 	        
 	        //讓列表有點選的功能
 	       /* llv1.setOnItemClickListener(listtener);
 	        llv2.setOnItemClickListener(listtener);
 	        llv3.setOnItemClickListener(listtener);
 	        llv4.setOnItemClickListener(listtener);
 	        
 	        
 	        button_foucs.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	slv.smoothScrollTo(0,0);
 	            	nowview=1;
 	            }
 	        });
 	        button_tech.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	slv.smoothScrollTo(800,0);
 	            	nowview=2;
 	            }
 	        });
 	        button_sports.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	slv.smoothScrollTo(1600,0);
 	            	nowview=3;
 	            }
 	        });
 	        button_relax.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	slv.smoothScrollTo(2400,0);
 	            	nowview=4;
 	            }
 	        });*/
 	        
 	    /*
 	        //把ProgressDialog關掉
 	        myDialog.dismiss();
 	        Log.i("prepareToClosePD", "closed");
 	    	break;*/
 	    	
 	/*    	}
 	    
 	    }
 	    
       };*/
     
    
     }
     
     @Override
 	protected void onResume() {
 	Log.i("onResum", "into");
 	super.onResume();
 	
     up_layout =(LinearLayout) findViewById(R.id.up_layout);//找出主畫面上方的水平scrollbar的id位置
     down_layout = (LinearLayout) findViewById(R.id.down_layout);//找出主畫面下方的水平scrollbar的id位置
     
 	//先建立資料庫，若沒建立直接使用myDB.getTruePath()會出現NullPointerException	
 	myDB = new DB(this);
 	cursor =myDB.getTruePath();
 	
 	//一開始先清空所有的view，避免每次都重覆創建子view
 	up_layout.removeAllViews();
 	down_layout.removeAllViews();
 	
	//初始化新聞加總newscount值為0
	newscount=1;
 	
 	//專門用來放每一筆的name，好讓刪除視窗出現時，能對應到
 	namelist = new HashMap();
 	
 		while(cursor.moveToNext()){
 			
 			//將資料庫內的內容取出放到Button上
 			name=cursor.getString(cursor.getColumnIndex("_name"));
 			path=cursor.getString(cursor.getColumnIndex("_path"));
 			id = cursor.getInt(cursor.getColumnIndex("_id"));
 			
 			//動態新增按鈕
 			button = new Button(NewsWeather.this);
             button.setText(name);
             LinearLayout.LayoutParams param =new LinearLayout.LayoutParams(110,65);
             up_layout.addView(button,param);
             button.setOnLongClickListener(this);
             button.setId(id);
             button.setOnClickListener(this);

             
             
             //動態新增ListView
             ListView newlv = new ListView(NewsWeather.this);
             LinearLayout.LayoutParams param2 =new LinearLayout.LayoutParams(800,460);
             down_layout.addView(newlv,param2);
             
             
             
             //將取出來的name存入容器
             namelist.put(id,name);
             
            
 		}
         
 			//滑動選單的初始設定
 	        slv = (HorizontalScrollView) findViewById(R.id.hsv);
 	        slv.setOnTouchListener(NewsWeather.this);
 	        
 //	        llv4.setOnItemClickListener(listtener);
 	        
 	        
 	  
 
 		
 	}
 
 	//ProgressDialog對話框
     private void progressDialog(){
     	final CharSequence strDialogTitle = getString(R.string.str_dialog_title);
     	final CharSequence strDialogBody = getString(R.string.str_dialog_body);
     	
     	//顯示Progress對話方塊
     	myDialog = ProgressDialog.show(NewsWeather.this, strDialogTitle,strDialogBody);
     	 Log.i("startProgressThread", "start");
     	 
     }
     
 	
     
     
     private void checkEncode(String path){//判斷此xml的格式
     	URL url = null;
     	String encode="";
     	int a,b;
     	 try {
     		   Log.i("intoCeckEncode: ",frequently+ "pass");
 			   url = new URL(path);
 
 			   InputStream is = url.openConnection().getInputStream();
 			   InputStreamReader isr = new InputStreamReader(is);
 			   BufferedReader br = new BufferedReader(isr);
 			   String buffera = br.readLine();
 			   br.close();
 			   a=buffera.indexOf("\"", 25)+1;
 			   b=buffera.indexOf("\"", a+1);
 			   encode = buffera.substring(a, b);
 			   Log.i("test", "test");
     	 }catch (Exception e) {
 				Log.i("Exception+", e.getMessage());
 				
 				new AlertDialog.Builder(NewsWeather.this)
 				.setTitle("很抱歉！")
 				.setMessage("由於目前沒有針測到網路，暫不提供新聞服務...")
 				.setPositiveButton("確認", new DialogInterface.OnClickListener() {
 											@Override
 											public void onClick(DialogInterface dialog, int which) {
 												NewsWeather.this.finish();
 											}
 										}).show();
 				
  		} 
     	 
 	    	   if(encode.equals("big5")|encode.equals("BIG5")){
 	    		 Encode ="BIG5";  
 			   }else if(encode.equals("utf-8")|encode.equals("UTF-8")|encode.equals("Utf-8")){
 				 Encode ="UTF-8";
 			   }	
 	    	   
     }
     
     
     
     /*因XML無法解析BIG5，會出現paraexception(not-well formed(invalid tocken))
       所以只要網址一進來，一定存到utf-8的buffxml.xml檔裡*/
     private void encodeTransfer(String path) {
     	
     	  URL url = null;
     	  String buffera="";
 			   try {
 				   Log.i("intoencodeTransfer:"+frequently, "pass");
 				   url = new URL(path);
 				   InputStream is = url.openConnection().getInputStream();
 				   InputStreamReader isr = new InputStreamReader(is,Encode);
 				   BufferedReader br = new BufferedReader(isr);
 				   FileOutputStream fos = openFileOutput("buffxml"+frequently+".xml", Context.MODE_PRIVATE);
 				   
 				   
 				   do{
 					   buffera = br.readLine();
 					   if(buffera!=null){
 					   bufferb = new String(buffera.getBytes(),"UTF-8");
 					   fos.write(bufferb.getBytes());
 					   fos.write('\r');
 					   }else{/*else這段避免XML原文最下面有一行空白行，卻還要for.write(b.getBytes())給出值的冏境
 					       導致造成NullPointerException*/
 					   bufferb="";
 				   }
 					   
 				   } while(buffera !=null);
 					  
 				   
 				    fos.flush();			    
 				    fos.close();
 				    Log.i("fos.close()+", "pass");
 				}  catch (UnsupportedEncodingException e) {
 				Log.i("unsupportex", e.getMessage());
 			} catch (FileNotFoundException e) {
 				Log.i("FileNotFoundException", e.getMessage());
 			}   catch (IOException e) {
 				Log.i("IOException+", e.getMessage());
 		} 
 				
 			Log.i("big52utf8()+", "pass");
 	}
 
 	//按下列表所要觸發的事件
     OnItemClickListener listtener = new OnItemClickListener(){
 
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			switch(nowview){
 				case 1:
 					String temp1 = li1.get(position).getLink();
 					Intent browserIntent1 = new Intent("android.intent.action.VIEW", Uri.parse(temp1));
 					startActivity(browserIntent1);
 					break;
 				case 2:
 					String temp2 = li2.get(position).getLink();
 					Intent browserIntent2 = new Intent("android.intent.action.VIEW", Uri.parse(temp2));
 					startActivity(browserIntent2);
 					break;
 				case 3:
 					String temp3 = li3.get(position).getLink();
 					Intent browserIntent3 = new Intent("android.intent.action.VIEW", Uri.parse(temp3));
 					startActivity(browserIntent3);
 					break;
 				case 4:
 					String temp4 = li4.get(position).getLink();
 					Intent browserIntent4 = new Intent("android.intent.action.VIEW", Uri.parse(temp4));
 					startActivity(browserIntent4);
 					break;
 					
 			}
 			
 
 
 		}
     	
     };
     
     
 
 	@Override//滑動手勢指令換頁
 	public boolean onTouch(View v, MotionEvent event) {
 		
 		
 		if(getstart==0){
 			getstart = event.getX();
 		}else if(event.getAction()!=event.ACTION_MOVE){
 			getend =event.getX();
 			
 			if (getstart-getend >0){
 				slv.smoothScrollBy(800, 0);
 				
					if(nowview<=4){
 						nowview++;
 					}else{
						nowview=4;
 					}
 				
 			}else{
 				slv.smoothScrollBy(-800, 0);
 				if(nowview>1)nowview--;
 			}
 			
 			getstart=0;
 		}		
 		return true;
 	}
     
 	//使用XML解析器
 	private List<News> getRss(){
 
 		List<News> data = new ArrayList<News>();
 //		URL url = null;//編碼為UTF-8直接解析的寫法		
 
 		try{
 //            url = new URL(path);//編碼為UTF-8直接解析的寫法				
 			
 			/*//使用sax解析
 			SAXParserFactory spf = SAXParserFactory.newInstance();
 			SAXParser sp = spf.newSAXParser();
 			XMLReader xr = sp.getXMLReader();
 			MyHandler myHandler = new MyHandler();
 			xr.setContentHandler(myHandler);
 			FileInputStream fis = openFileInput("buffxml.xml");
 			InputSource A =new InputSource(path);
 		
 			String encode =A.getEncoding();//返回n u l l 
 			xr.parse(A);*/
 			
 			//使用android解析器
 			MyHandler myHandler = new MyHandler();
 			Log.i("myHandler", "pass");
 			
 			/*//編碼為UTF-8直接解析的寫法
 //			android.util.Xml.parse(url.openConnection().getInputStream(), Xml.Encoding.UTF_8, myHandler);*/
 			
 			
 	
 			FileInputStream fis = openFileInput("buffxml"+frequently+".xml");
 			android.util.Xml.parse(fis, Xml.Encoding.UTF_8, myHandler);
 			Log.i("parse", "pass");
 			//取得RSS標題與內容列表
 			data = myHandler.getParasedData();
 			Log.i("getParasedData", "pass");
 		}catch(Exception e){
 			Log.i("tag", "wrong! "+e.getMessage());
 		}
 		return data;
 	}
 	
 	//建立Menu清單
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// TODO Auto-generated method stub
 		
 		menu.add(0, 0, 0, "設定");
 		menu.add(0, 1, 1, "關於");
 		menu.getItem(0).setIcon(R.drawable.setting);
 		menu.getItem(1).setIcon(R.drawable.about);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	//建立Menu清單的觸發事件
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		switch(item.getItemId()){
 			case 0:
 				Intent intent = new Intent();
 				intent.setClass(NewsWeather.this, Setting.class);
 				startActivity(intent);
 
 				break;
 			case 1:
 				new AlertDialog.Builder(NewsWeather.this)
 				.setMessage("作者：Camangi Corporation")
 				.setTitle("關於")
 				
 				.setPositiveButton("確認", new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 		
 					}
 				})
 				.show();
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	
 	@Override//Button的觸發事件
 	public void onClick(View v) {

    	slv.smoothScrollTo((v.getId()-1)*800,0);
     	
 //		v.setBackgroundResource(R.color.brown);//試圖改變背景顏色，結果...	]
     	
     
 	}
 	
 	//按鈕長按事件
 	@Override
 	public boolean onLongClick(final View v) {
 		
 		try{
 		myDB=new DB(this);
 		
 		String a =namelist.get(v.getId()).toString();
 		new AlertDialog.Builder(NewsWeather.this)
 		
 		.setMessage("刪除頻道"+namelist.get(v.getId())+"？")
 		.setTitle("注意！")
 		
 		.setPositiveButton("確認", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				myDB.delete(v.getId());
 				onResume();
 			}
 		})
 		
 		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				onResume();
 			}
 		})
 		
		.show();
 		
 		}catch(Exception e){
 			new AlertDialog.Builder(NewsWeather.this)
 		
 		
		.setMessage("已經剩1筆了，不要刪了吧！")
 		.setTitle("注意！")
 		
 		.setPositiveButton("確認", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				onResume();
 			}
 		})
 		.show();
 		}
 		return false;
 	}
 	
 
 
 }
