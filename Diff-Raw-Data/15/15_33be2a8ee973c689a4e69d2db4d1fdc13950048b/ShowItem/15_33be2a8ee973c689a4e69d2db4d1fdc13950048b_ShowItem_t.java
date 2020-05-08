 package jim.h.common.android.zxingjar.demo;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.CalendarContract;
 import android.provider.CalendarContract.Events;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ShowItem extends Activity {
 	
 	private Document doc = null;
 	private String text = "";
 	private Element masthead;
 	private String [] list = new String [100];
 	private int m=0;
 	private int dd=0;
 	private String urlen;
 	private String tida;
 	private String sted;
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
     	super.onCreateOptionsMenu(menu);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
             	Intent i = new Intent(getApplicationContext(),HvaSkjer.class);
                 startActivity(i);
                 break;
 
             case R.id.menu_refresh:
                 
 
                 getWindow().getDecorView().postDelayed(
                         new Runnable() {
                             @Override
                             public void run() {
                             	
                             }
                         }, 1000);
                 break;
                 
             case R.id.menu_search:
                 Toast.makeText(this, "Tapped search", Toast.LENGTH_SHORT).show();
                 break;
 
             case R.id.menu_share:
                 Toast.makeText(this, "Tapped share", Toast.LENGTH_SHORT).show();
                 break;
                 
         }
         return super.onOptionsItemSelected(item);
     }
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.show_item);
         
         final TextView txt1 = (TextView)findViewById(R.id.text);
 		final Button btn = (Button)findViewById(R.id.addtoCal);
         /*WebView webView = (WebView) findViewById(R.id.webView);
 	    
 	    webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
 	    webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
 	    
 	    webView.setWebViewClient(new MyWebViewClient());
 	    webView.getSettings().setPluginsEnabled(true);
 	    webView.getSettings().setBuiltInZoomControls(false); 
 	    webView.getSettings().setSupportZoom(false);
 	    webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);   
 	    webView.getSettings().setAllowFileAccess(true); 
 	    webView.getSettings().setDomStorageEnabled(true);*/
 	    
         
 	    Intent i = getIntent();
 	    urlen = i.getStringExtra("url");
 	    tida = i.getStringExtra("tid");
 	    sted = i.getStringExtra("sted");
 	    
 	    //LinearLayout linearLayout = (LinearLayout) findViewById(R.id.info);
 	    
 	    
 	    final Handler handler = new Handler();                     
 		Thread downloadThread = new Thread(new Runnable() {                     
     	    @Override
 			public void run() {
 		    	try {
 			    		doc = Jsoup.connect("http://www.hioa.no/" + urlen).get();
 						String content = "";
 						//String[] list = new String[100];
 						int i = 0;
 						
 			    		Elements bodyText = doc.select("p");
 
 			    		for (Element src : bodyText) {
 			    			//System.out.println(src.text());
 			    			
 			    			content = src.html().toString();
 			    			
 			    			
 			    			content = content.replaceAll("\\<.*?>","");
 			    			content = content.replaceAll("\\&aring;","");
 			    			content = content.replaceAll("\\&Aring;","");
 			    			content = content.replaceAll("\\&oslash;","");
 			    			content = content.replaceAll("\\&Oslash;","");
 			    			content = content.replaceAll("\\&aelig;","");
 			    			content = content.replaceAll("\\&Aelig;","");
 			    			content = content.replaceAll("\\&nbsp;","");
 			    			content = content.replaceAll("\\&laquo;","'");
 			    			content = content.replaceAll("\\&raquo;","'");
 			    			content = content.replaceAll("\\&lt;","<");
 			    			content = content.replaceAll("\\&gt;",">");
 			    			content = content.replaceAll("\\&quot;","'");
 			    			content = content.replaceAll("\\&eacute;","");
 			    			
 			    			
 			    			list[i] = content + "\n\n";
 			    			i++;
 			    			
 			    		}
 			    		
 			    		int j = 0;
 			    		while (j < list.length-1) {
 			    			if (list[j+1] == null) {
 			    				text += "";
 			    				break;
 			    			} else {
 			    				text += list[j];
 			    				System.out.println("html: " + list[j]);
 			    				j++;
 			    				
 			    			}
 			    		}
 			    		
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		    	System.out.println("dato: " + tida);
 		    	System.out.println("sted: " + sted);
 		    	final String daay = tida.substring(3,5);
 		    	final String maaned = tida.substring(0,2);
 		    	final String aar = tida.substring(6,10);
 		    	final String hour = tida.substring(11,13);
 		    	final String min = tida.substring(14, 16);
 		    	final String sec = tida.substring(17, 19);
 		    	
 		    	m = Integer.parseInt(maaned);
 		    	dd = Integer.parseInt(daay);
 		    	
 		    	//System.out.println("tid: " + hour + min + sec );
 		    	
 		    	handler.post(new Runnable() {
 		    		@Override
 		    		public void run() {
 		    			btn.setOnClickListener(new OnClickListener() {
 							
 							@Override
 							public void onClick(View v) {
 								// TODO Auto-generated method stub
 								
 								addToCall(list[0], list[2], Integer.parseInt(maaned), 
 										Integer.parseInt(daay), Integer.parseInt(aar), Integer.parseInt(hour), 
 										Integer.parseInt(min), Integer.parseInt(sec));
 							}
 						});
 		    			
 		    		}
 		    	});
 		    	
 		    	handler.post(new Runnable() {
 					@Override
 					public void run() {
 						txt1.setText(text);
 					}
 		    	});
     	    }
     	});
 		downloadThread.start();
 		
 	    System.out.println("her her" + this.text);
 	    
 	    //webView.loadUrl("http://www.hioa.no/" + urlen);
         
     }
 	
 	public void addToCall (String eventname, String description, int mon, int d, int y, int hour, int min, int sec) {
 		
 		long startMillis = 0;
 	    long endMillis = 0;
 	    
 	    Calendar beginTime = Calendar.getInstance();
	    beginTime.set(y, (d-1), mon, hour, min, sec);
		startMillis = beginTime.getTimeInMillis();
 		
		GregorianCalendar startDate = new GregorianCalendar(y,d,mon,hour,min);
		//startMillis = startDate.getTimeInMillis();
 		
		GregorianCalendar endDate = new GregorianCalendar(y,(d-1),mon,(hour+2),min);
 		endMillis = endDate.getTimeInMillis();
 		
 		//Calendar endTime = Calendar.getInstance();
 	    //endTime.set(y, mon, d, hour+2, min, sec);
 	    //endMillis = endTime.getTimeInMillis();
 	    
 	    System.out.println("Date start :"+startMillis);
 	    System.out.println("Date start :"+endMillis);
 	    
 		
         //cal.setTime(new Date());
		System.out.println("mned: " + d + " " + mon + " " + y + " " + hour + " " + min + " " + sec);
 		
 		
         Intent intent = new Intent(Intent.ACTION_INSERT);
         intent.setType("vnd.android.cursor.item/event");
         intent.setData(CalendarContract.Events.CONTENT_URI); 
          
         
         intent.putExtra(CalendarContract.Events.EVENT_LOCATION, sted);
         intent.putExtra(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Oslo");
         intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis);
         intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis);
         
         intent.putExtra(CalendarContract.Events.TITLE, eventname);
         intent.putExtra(CalendarContract.Events.DESCRIPTION, description);
         
         /*intent.putExtra(
               CalendarContract.EXTRA_EVENT_BEGIN_TIME, 
               cal.getTime().getTime());       
         intent.putExtra(
               CalendarContract.EXTRA_EVENT_END_TIME, 
               cal.getTime().getTime() + 600000); 
         intent.putExtra(
               Intent.EXTRA_EMAIL, 
               "tofiksa@gmail.com"); */
         startActivity(intent); 
 	}
 	
 	public class MyWebViewClient extends WebViewClient {        
 	    /* (non-Java doc)
 	     * @see android.webkit.WebViewClient#shouldOverrideUrlLoading(android.webkit.WebView, java.lang.String)
 	     */
 
 
 	    @Override
 	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
 	        if (url.endsWith(".mp4")) 
 	        {
 	            Intent intent = new Intent(Intent.ACTION_VIEW);
 	            intent.setDataAndType(Uri.parse(url), "video/*");
 
 	            view.getContext().startActivity(intent);
 	            return true;
 	        } 
 	        else {
 	            return super.shouldOverrideUrlLoading(view, url);
 	        }
 	    }
 	}
 }
