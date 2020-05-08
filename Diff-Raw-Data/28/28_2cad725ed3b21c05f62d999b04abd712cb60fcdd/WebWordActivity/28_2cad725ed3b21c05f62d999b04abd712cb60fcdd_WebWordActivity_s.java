 package wikem.chris.wikemv3;
   
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.http.client.ClientProtocolException;
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 import wikem.chris.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.ParseException;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.content.CursorLoader;
 import android.util.AndroidRuntimeException;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 //import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
   
  	 
 	public class WebWordActivity extends  Activity  {
 		
 		private static String loadwbaseurl; // ="http://www.wikem.org";
 		private static final String ORIGINAL_CONTENT = null;
 		public static String keyWord;
 		String noteContent;//for pause and resume?
 		private EditText mBodyText;
 		Cursor cursor;
  		private String mFragment; //for internal links such as when using findtext
 		// public static ArrayList <Integer> linksList;
 		 private ArrayList <Integer> highlightList;
 	        WebView wv;
 	        String summary;
 	        private boolean highlight_text;
 	    	private boolean findNext=false;
 	    	@SuppressWarnings("rawtypes")
 			private Iterator iterator=null;
 	    	private static  String lastUpdated;
 	    	private Uri uri;
 	    	public static CheckUpdatesForPageTask asyncTask ;
 	    	 
 	    	
 	    	
 
 	 	  
 		    	
 		@Override
 		 protected void onCreate(Bundle savedInstanceState) {
 			File appDbDir = getAppDir();
 			loadwbaseurl = "file://" + appDbDir.getAbsolutePath() + "/";
 	Log.d("wwa", "loadwbase url is: " + loadwbaseurl)	;	
 			
 	        super.onCreate(savedInstanceState);
  
 	        String head_css = getString(R.string.head_css);
 	        
 	        setContentView(R.layout.web_relative_layout);
 	        Intent intent = getIntent();
 	        uri=intent.getData();
 	        highlight_text = intent.getBooleanExtra("is_query", false); //deafult false. but true if is
 	        cursor = managedQuery(uri, null, null, null, null);   
 	        startManagingCursor(cursor);
 	        
 	        wv = (WebView) findViewById (R.id.wv1);
 	     //delete   this.registerForContextMenu(wv); // doesn't work 
 	         WebSettings settings = wv.getSettings(); 
 	        settings.setJavaScriptEnabled(true);  //for internal links using onpagefinished //also to get html source
 	        settings.setPluginsEnabled(true); //to get html source
 	        settings.setJavaScriptCanOpenWindowsAutomatically(true); //to get html source
 	        wv.setWebViewClient(new MyWebViewClient());
 	     //   wv.setWebChromeClient(new WebChromeClient()); 
 	     //  bug in android 2.3. dont need   wv.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");	
  
 	        TextView word = (TextView) findViewById(R.id.word);
 	        
 	        if (cursor == null) {
 	            finish();
 	        } else {
 	            cursor.moveToFirst(); //uh. move to first row. there should only be one row...            
 	            //get indexes of columns
 	            int wIndex = cursor.getColumnIndexOrThrow(DictionaryDatabase.KEY_WORD);
 	            keyWord = cursor.getString(wIndex);
 	            int dIndex = cursor.getColumnIndexOrThrow(DictionaryDatabase.KEY_DEFINITION);
 	            
 	            /*
 	             * ok now get lastupdate?
 	             */
 	            int uIndex = cursor.getColumnIndexOrThrow(DictionaryDatabase.LAST_UPDATE);
 	            Log.d("wwact", "last updated" + cursor.getString(uIndex));
 	            lastUpdated = cursor.getString(uIndex);
 	            
 	            
 	            
 	            //set the word/wiki entry name textview layout from the string in the cursor
 	            // word.setText(cursor.getString(wIndex));
 	            //the definition won't be in a webview for now, but simple textview using "Spanned" and fromHtml method
 	            String wikemEntry = cursor.getString(dIndex);
 	            if( highlight_text){
 		    		if (SearchableDictionary.query==null){Log.d("WVA", "watch out!!! the query text being called is null");	}
 		    		else{
 			            wikemEntry = doHighlight(cursor.getString(dIndex), SearchableDictionary.query);
 		    			Log.d("WVA", "highlighting.") ;
 		    		}
 		    		highlight_text=false; //reset it to false
 		    	}
 	            
 	          //  summary =  "<html>" + head_css + "<body>"+ FixHTML.fixImages(wikemEntry)+"</body></html>";
 	            summary =  "<html>" + head_css + "<body>"+ wikemEntry+"</body></html>";
  	            
  	             word.setText(cursor.getString(wIndex));
 	            //wv.loadData(summary, mimeType, encoding); this piece of crap doesn't work ...drove me crazy
  	        
 		        wv.loadDataWithBaseURL(loadwbaseurl, summary,"text/html", "UTF-8", null);
 	         }
 	        mBodyText = (EditText) findViewById(R.id.body);
 	        //hides soft ekyboard until edittext presed?
 	        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);	        
 	        wv.requestFocus();	 
 	        
 	        
 	        /*
 	         * create an asynch task to run in background and check online for updates
 	         * basically using api.php to give us a parsable xml
 	         */
 	        asyncTask = new CheckUpdatesForPageTask();
 	        asyncTask.execute( );
 	        displayNoteIfExists(); 	
 
 	 
 		}
 		private boolean haveNetworkConnection()
 		{
 		    boolean HaveConnectedWifi = false;
 		    boolean HaveConnectedMobile = false;
 
 		    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
 		    for (NetworkInfo ni : netInfo)
 		    {
 		        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
 		            if (ni.isConnected())
 		                HaveConnectedWifi = true;
 		        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
 		            if (ni.isConnected())
 		                HaveConnectedMobile = true;
 		    }
 		    return HaveConnectedWifi || HaveConnectedMobile;
 		}
 		
 		
 		private class CheckUpdatesForPageTask extends AsyncTask<Void, Void, Boolean> {
  			@Override
 			protected Boolean doInBackground(Void... arg0) {
  			//basically check iscancelled several times 
  			 
  				if (!haveNetworkConnection()){return Boolean.valueOf(false);}
  				
  				   try {	WikemAPI w = new WikemAPI();
  					   		long newupdatetime = w.canUpdate();
 							if (newupdatetime== 0)
 							{
 								//do nothing
 								Log.d("wwact", "do nothing zero returned....");
 							}
 							else if ( !isCancelled()){
 								   //download the updated page
 								//if (isCancelled()) break;
 								ContentValues values = new ContentValues();
 					  	        //	values.put(DictionaryDatabase.KEY_WORD, keyWord);
 								if (!isCancelled()){
 									String s = w.updateThisPage();
 											if (s!=null  && !isCancelled()){
 								  	            values.put(DictionaryDatabase.KEY_DEFINITION, w.updateThisPage());
 								  	            values.put(DictionaryDatabase.LAST_UPDATE,  Long.toString(newupdatetime));
 								  	            String[] whereArgs = new String [] { keyWord};
 								  	          	int i = getContentResolver().update(DictionaryProvider.CONTENT_URI, values, DictionaryDatabase.KEY_WORD, whereArgs);
 								  	          				if (i>0 ){
 								  	          					Log.d("wwact", "i think successful update");
 								  	          				return Boolean.valueOf(true);
 								  	          				}
 								  	          				//add toast or something?
 											}								
 								}
 							   }
 							  
 				} catch (java.text.ParseException e) {
 					e.printStackTrace();
 				} catch (ClientProtocolException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				return Boolean.valueOf(false);
 			}
 
  		protected void onPostExecute(Boolean result) {
  /* was getting java.lang.NullPointerException
 at wikem.chris.wikemv3.WebWordActivity$CheckUpdatesForPageTask.onPostExecute(WebWordActivity.java:246) . if result is null, it is null pointer*/
   			if (result != null){			
  				
  				
 	 				//won't be called if cancelled.
 	 				if (result.booleanValue()){
 	    					Log.d("wwact", "onpostexecute");
 	
 	 					//Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
 	 					showUpdateAlert();
 	 				}
 	 				else{ //do nothing i guess
 	  				}
  				}
  			else{ Log.d("wwact", "onpostexecute, result is null. error caught =)");			
  				}
 		     }
  			
  			 @Override
  		    protected void onCancelled() {
  		       //do nothing
  		    }
 		 }
 		
 		
 		/*
 		class MyJavaScriptInterface{
 
 			public void showHTML(String html){
  
 			//	Log.d("webwordact", "html content is "+html);
 				 
 				}
 			}*/
 
 		private class MyWebViewClient extends WebViewClient { //True if the host application wants to leave the current WebView and handle the url itself, otherwise return false.
 		    @Override
 		    public boolean shouldOverrideUrlLoading(WebView view, String url) {
 		    	Log.d("WEBWORDACTVITY", "the url clicked is : " +url);		    	
 			    URI link = null;
 				try {
 					link = new URI(url);
 				} catch (URISyntaxException e1) {
 					e1.printStackTrace();
 				}				
 				if (link!=null )
 				{	
 					if (link.isOpaque()){
 						/*
 						if (link.toString().startsWith("mailto")){
 							// /*later... later  try to send launch email activity on opaques
 			    	    //* Intent intent = new Intent(Intent.ACTION_SEND);
 			    	    */
 						Log.d("WebWordActivity", "opaque link");
 						return false;
 					}					
 					else if (link.isAbsolute()){
 						Log.d("WebWordActivity", "absolute link");
 						// if link is an image don't parse it. do nothing and return true
 						String fileNamespace = "File:"; 
 				
 						if(!url.contains(fileNamespace)){
 							 parseAbsoluteLink(url);
 						}
 					}
 					else{ //dunno why isRelative doesnt work?
 						//relative links
 						Log.d("WebWordActivity", "relative link");
 						return false; //let android do this.
 					}				
 				} 	  			     
 				return true;
 		    }
 	private void parseLinkRecursive(String url){
 		/*dunno why sql not matching some of my links . if first match returrned a null cursor
 		 * calls this with underscores for characters that might be causing problem
 		 * eg. Non-ST-Elevation MI (NSTEMI) -> no match 
 		 * start with Non_ST_Elevatoin__NSTEMI_% (all _ are single wildcard, %is any length wildcard)
 		 * still no result, cut in half
 		 * 	 Non_ST_Elev%
 		 * 
 		 * if that doesn't work, recurse again
 		 * Non_S%
 		 * 
 		 */
 		
 			 try {		CursorLoader cursorLoader = new CursorLoader(getApplicationContext(), DictionaryProvider.ID_LIKE_URI, null, null, new String[] {url}, null);
 					    cursor = cursorLoader.loadInBackground();
 					        	if (cursor!=null){//returns false if empty
 					        		try{
 					        			int i = cursor.getColumnIndexOrThrow(DictionaryDatabase.BASE_STRING);
 					        			String rowID = cursor.getString(i);
 					        			cursor.close();
 					        			Uri data = Uri.withAppendedPath(DictionaryProvider.CONTENT_URI, rowID);
 								        	Log.d("WEBWORD ACTIVIY","the full uri to go to is:" + data.toString()); 	
 							        	Intent wordIntent = new Intent(getApplicationContext(), WebWordActivity.class);
 						        		wordIntent.setData(data);
 						        		startActivity(wordIntent);
 					        		}catch (Exception e){ Log.e("wwact" , "wrong row?!");}
 					        			
 					        	}
 					        	else{ //recursion
 					        		Log.e("wwact" , "cursor is nil?!");  
 					        		int tryShorterQuery = (url.length()/2);
 					        			if(tryShorterQuery < 2 ){ //end the recursive search
 					        			launchAWordSearch(url);
 					        			}
 					        			else{ //recurse
 					        			parseLinkRecursive(url.substring(0,tryShorterQuery));
 					        			}
 					        	}	        			        		
 					        }catch(Exception e){Log.e("WEBWORDACTIVITY", "crap error thrown in parseabsolutelink");
 					        //so search for the ""link" if it returns null that there is no wikem entry that matches
 					  //      					String sub = url.substring(1); //get rid of backslash
 					        					launchAWordSearch(url);
 					        					}
 		
 	}
 	private void parseAbsoluteLink(String url) {
 			//link has the baseurl and comes out as file:///wiki/
         //	Log.d("wwa", "link is: " + url);
 
 	        url=url.substring(13); 
 	        Log.d("wwa", "link is: " + url);
 	      	 //   word = word.replace('-', ' ').trim();
 	        url = url.replace('_', ' ').trim();
 	        try {
 	//        	url = Uri.decode(url);
 	        	Log.d("wwact" , "url is now:" + url);
 	        //Cursor cursor = managedQuery(DictionaryProvider.ID_URI, null, null,
               //        new String[] {url}, null);
 	        	//public final Cursor managedQuery (Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
 	        	//public CursorLoader (Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
 	        	CursorLoader cursorLoader = new CursorLoader(getApplicationContext(), DictionaryProvider.ID_URI, null, null, new String[] {url},
 	        			null);
 	        			cursor = cursorLoader.loadInBackground();
 	        			
 	        //	Log.d("wwact" , "didn't throw error");
 	        	if (cursor!=null){//returns false if empty
 	        		try{
 	        			int i = cursor.getColumnIndexOrThrow(DictionaryDatabase.BASE_STRING);
 	        			String rowID = cursor.getString(i);
 	        			cursor.close();
 	        			Uri data = Uri.withAppendedPath(DictionaryProvider.CONTENT_URI,
 		                        rowID);
 				        	Log.d("WEBWORD ACTIVIY","the full uri to go to is:" + data.toString()); 	
 			        	Intent wordIntent = new Intent(getApplicationContext(), WebWordActivity.class);
 		        		wordIntent.setData(data);
 		        		startActivity(wordIntent);
 	        		}catch (Exception e){ Log.e("wwact" , "wrong row?!");}
 	        			
 	        	}
 	        	else{ //if cursor was empty
 	        		Log.e("wwact" , "cursor is nil?!");      		        		
 	        		//cursor.close unnecessary bc null pointer...
 	        		
 	        		//place underscores (matches any char) so I can LIKE query SQL
 	        			url.replace(' ', '_');
 	        			url.replace('-', '_');
 	        			url.replace('(', '_');
 	        			url.replace(')', '_');
 	        			parseLinkRecursive(url);
 	        			//launchAWordSearch(url); 
 	        	}	        			        		
 	        }catch(Exception e){Log.e("WEBWORDACTIVITY", "crap error thrown in parseabsolutelink");
 	        //so search for the ""link" if it returns null that there is no wikem entry that matches
 	  //      					String sub = url.substring(1); //get rid of backslash
 	        					launchAWordSearch(url);
 	        					}
 					}
 
 			private void launchAWordSearch(String url) {
   				Intent wordSearchIntent = new Intent(getApplicationContext(), WordSearchActivity.class);
 				wordSearchIntent.putExtra("word", url);
 				startActivity(wordSearchIntent);
 		}
 
 			@Override 
 		    public void onPageFinished(final WebView view, final String url) { 
 			      /* This call inject JavaScript into the page which just finished loading. so can get the html source if wanted. not used right now*/  
 	
 			      //  view.loadUrl("javascript:window.HTMLOUT.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
 			 //       view.loadUrl("javascript:window.HTMLOUT.showHTML(document.getElementsByTagName('body')[0].innerHTML);");
 				//make jump to internal link
 				if(mFragment !=null ){
 					view.loadUrl("javascript:location.href=\"#" + mFragment + "\"");
 					mFragment = null; //to avoid infinite loop!
 					view.requestFocus();
 				}
 
 		    	   
 		    } 
 		}
 		
 		
 		
 		 @Override
 		 public boolean onPrepareOptionsMenu(Menu menu) {
 			 //You must return true for the menu to be displayed; if you return false it will not be shown.
 			 /*
 			  * this is called right before the menu is shown, every time it is shown.
 			  */				     
 			 if (findNext==true && iterator.hasNext()){
 		        	menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Find Next!");
 		        	Log.d("WWA", "should have added new menu item");
 		        	findNext=false; //stop creating menus
 		        }			 
 			 if (!displayNoteOrNot()) //ie hide the note
 			 {
 				 menu.setGroupVisible(R.id.remove_edit_text_group, false);
 				 menu.setGroupVisible(R.id.show_edit_text_group, true); 
 			 }
 			 else{
 				 menu.setGroupVisible(R.id.remove_edit_text_group, true);
 				 menu.setGroupVisible(R.id.show_edit_text_group, false);				 
 			 }
 			 return true;
 		 }
 		 private void reloadPage(){
 			 	finish();
 		        Intent intent = new Intent(this, WebWordActivity.class);
                 intent.setData(uri);
 		        startActivity(intent);
 		 }
 		 public void showUpdateAlert() {
 		 		Log.d("WEBWORDACTIVITY", " ok..inside showupdatealert");
 		 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 				alert.setTitle("Update Found!");						
 				alert.setMessage("Would you like to view newer version of '" + keyWord + "'?" );
 		
 				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 				  reloadPage();  }
 				});
 		
 				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				  public void onClick(DialogInterface dialog, int whichButton) {
 				    // Canceled.
 				  }
 				});		
 				alert.show();				
 		}
 
 		@Override
 		 public boolean onCreateOptionsMenu(Menu menu) {
 				if (keyWord.equals("About WikEM")){
 			 		 /*
 					  * put in easter egg. for hidden functionality for beta testing
 					  */
 			 		Log.d("WEBWORDACTIVITY", " ok.. inside about wikem secret menu");
 			 		 MenuInflater inflater = getMenuInflater();
 				     inflater.inflate(R.menu.word_options2, menu);
 				     return true;
 			 	}
 			 	else{
 			        MenuInflater inflater = getMenuInflater();
 			        inflater.inflate(R.menu.word_options2, menu);
 			        return true;
 			 	}
 		    }
 	    @Override
 	    public boolean onOptionsItemSelected(MenuItem item) {
 	        switch (item.getItemId()) {
 	        	case R.id.search:
 	        		onSearchRequested(); 
 	        		return true;
 	            case R.id.find:
 	            //   ?? onSearchRequested(); ??what is this
 	          // startSearch(null,false,null,false);	    
 	            	findTextAlert();
 	                return true;	        
 	            case R.id.favorite:
 	            	favoriteOptionSelect();
 	            	return true;
  	            case R.id.remove_edit_text:
  	            	undisplayNote();
 	            	
 	            	return true;
 	            case R.id.show_edit_text:
 	            	displayNote();
 	            	return true;
 	            case R.id.edit_online:
 	            	editOnline();
 	            	return true;
 	            case R.id.about_this_page:
 	            	displayInfo();
 	            	return true;
 	            	            	
 	            default: return findNext(); //since no unique id of findnext...just use default.
 	            
 	        }
 	    }
 
 	private void displayInfo() {
 		Date ourDbEpoch = new Date(Long.parseLong(lastUpdated) * 1000);
     	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	
     	builder.setMessage("Last update for this page was " + ourDbEpoch.toLocaleString()) 
     			//ourDbEpoch.toGMTString()) 
     	       .setCancelable(false)
     	       .setPositiveButton("Return to WikEM", new DialogInterface.OnClickListener() {
     	           public void onClick(DialogInterface dialog, int id) {
      	        	   dialog.cancel();
     	           }
     	       });    	       
     	AlertDialog alert = builder.create();      
     	alert.show();
 		
 			
 		}
 	private boolean findNext() {
 		if (iterator.hasNext())
 		{
 			mFragment = iterator.next().toString();
 			//wv.loadDataWithBaseURL(null,summary ,"text/html", "UTF-8", null);
 			  wv.loadDataWithBaseURL(loadwbaseurl, summary,"text/html", "UTF-8", null);
 			Log.d("WWA", "inside findnext");
 			return true;
 		}
 			
 			return false;
 		}
 	private void findTextAlert() {
 			AlertDialog.Builder alert = new AlertDialog.Builder(this);
 	
 			alert.setTitle("Find on Page");
 			alert.setMessage("Please enter a term to search for then press ok");
 	
 			// Set an EditText view to get user input 
 			final EditText input = new EditText(this);
 			alert.setView(input);
 	
 			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 			  String value = input.getText().toString();
 			  // Do something with value!
 			  findText(value);
 			  
 			  }
 			});
 	
 			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			  public void onClick(DialogInterface dialog, int whichButton) {
 			    // Canceled.
 			  }
 			});
 	
 			alert.show();			
 		}
 	
 	private void displayNoteIfExists(){
 			SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);
 			
 			
 			if (settings.getBoolean("displayNotePref", false)){ //ie if user wants to show note (by default false)
 				
 			   mBodyText.setVisibility(View.VISIBLE);
 				
 				if(settings.contains("note-" + keyWord)){
 					String note = settings.getString("note-" + keyWord, "none"); //second "keyword" is just default}
 					if(note != "none"){ 
 						if (note!=null){//null protect string body from null string 
 						mBodyText.setTextKeepState(note); Log.d("WWAct", "trying to display note for keyword " + keyWord +". Note is:" + note);}			 	                
 		            }
 					else{ Log.d("WWact", "why is note saying NONE?!!");}
 				}
 			}
 			else { //ie if the displaynote prefs wants to hide the note
 			  mBodyText.setVisibility(View.GONE);
 			  if(settings.contains("note-" + keyWord)){
 				  //ie if note exists, toast
 					Toast.makeText(this, "custom note exists", Toast.LENGTH_SHORT).show();
 				}
 			}
 				
 		
 		}
 	
 	private void commitNote(){
 		// use.. getContentResolver().update(mUri, values, null, null);
 		SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);    			
         SharedPreferences.Editor editor = settings.edit();               
         String note =  mBodyText.getText().toString();
         if (note.length()>=1){ //cheap way to see if not empty
          editor.putString("note-" + keyWord, note);
          editor.commit();
          Log.d("WWAct", "note presumably committed  ") ;}
 
 	}
 	private void findText(String searchTerm){
 		
 		summary = doHighlight(summary, searchTerm);
 		if (highlightList!=null && highlightList.size()>0)//redundant?!
 		{
 		mFragment = Integer.toString(1);// ie. start at the first link
 		findNext=true; //ie add a menu option to find next now
 		Toast.makeText(this, "Found " + Integer.toString(highlightList.size()) + " search items", Toast.LENGTH_SHORT).show();
 		
 		
 		iterator = highlightList.iterator();
 		if( iterator.hasNext()){
 			//this.invalidateOptionsMenu();//not available till api 11
 		}
 		//mFragment = iterator.getClass()
 		
 		}
 		 //  wv.loadDataWithBaseURL(null,summary ,"text/html", "UTF-8", null);
 		  wv.loadDataWithBaseURL(loadwbaseurl, summary,"text/html", "UTF-8", null);
 		
 	}
 
 	private boolean displayNoteOrNot(){ //returns the preference to display..t or f
 		 
 		SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);    
 		return settings.getBoolean("displayNotePref", false);
 	}
 	private void displayNote(){
 		//hideNoteonOptions = true; //ie. show the 'hide note' option again
 		
 		undisplayNote(); //i think undisplaynote should take care of all. since it is written as either/or scenario
 		
 		
 	}
 	private void undisplayNote(){
 		/* so if prefs = hide. then keep hidden. 
 		 * otw, if show note, then keep showing...
 		 */
 		SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);    
 		boolean displayNotePref = settings.getBoolean("displayNotePref", true); //second "true" is just default
 		if(displayNotePref ==true)
 			{
 	        SharedPreferences.Editor editor = settings.edit(); 
 	        editor.putBoolean("displayNotePref", false);
 	        editor.commit();
 	        
 			   mBodyText.setVisibility(View.GONE);
 		//?what is this	   RelativeLayout.LayoutParams layoutParams;
 			   //layoutParams.addRule(RelativeLayout.RIGHT_OF,
 		    	 Log.d("WEBWORDACTIVITY  ", "make text invis..");
 		    //	 hideNoteonOptions=true;
 		    	 
 			}
 		else{ // ie' redisplay the note
 			SharedPreferences.Editor editor = settings.edit(); 
 	        editor.putBoolean("displayNotePref", true);
 	        editor.commit();
 	        displayNoteIfExists();
 	    	// hideNoteonOptions=false;
 
 			
 		     }
 			
 	    }
 	   
 		private void editOnline(){
 	    	
 			
 			
 	    	String baseURL = "http://www.wikem.org/w/index.php?title=";
 	    	String websiteURL= baseURL.concat(keyWord.replace(' ', '_')) + "&action=edit";
 	    	 
 	    	 Log.d("WEBWORDACTIVITY open ", websiteURL.concat(keyWord.replace(' ', '_')));
 	    	 Uri uri2 = Uri.parse( websiteURL);
 	    	 Intent intent = new Intent(Intent.ACTION_VIEW, uri2);
 	    	 startActivity(intent);
 	    }
 
 	
 		private void favoriteOptionSelect() {
 			//first check that not already favorite if not			
 			// replace "favorites" shared preference wiht: favorite1, favorite2, fav3, ...
 			SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);    
 			String favs = settings.getString("favorites", ""); //second "keyword" is just default
            
              if (favs.contains(keyWord)){ //ie already contains this favorite
             	AlertDialog.Builder builder = new AlertDialog.Builder(this);
             	builder.setMessage(keyWord+ " already exists as favorite. Remove?")
             	       .setCancelable(false)
             	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
             	           public void onClick(DialogInterface dialog, int id) {
             	               //since sharedprefs already open just remove here.
              	        	   removeFavorite();
             	           }
             	       })
             	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
             	           public void onClick(DialogInterface dialog, int id) {
             	                dialog.cancel();
             	           }
             	       });
             	AlertDialog alert = builder.create();      
             	alert.show();
             	
             
              
             } 
             //otherwise if the favorite already doesn't exist..
             else{
             	 SharedPreferences.Editor editor = settings.edit();  
             favs = favs.concat(keyWord + ",,");  
             editor.putString("favorites", favs);
 
              // Commit the edits!
              editor.commit();
              
 			if (DictionaryDatabase.addOrRemoveFavorite(keyWord, true)){ 
 				//add fav to the db so to be queried . boolean true is for add...not remove
 				//returned true... toast 
 				Log.d("WEBWORDACt", "favs are:" + favs);
 				Toast toast = Toast.makeText(getApplicationContext(), keyWord + " successfully added to favorites", Toast.LENGTH_SHORT);
 		 		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
 		 		toast.show();
 			}
 			else{
 				//favorite adding db issues..
 			}
             }
 		}
 		
 		
 		protected void removeFavorite() {
 			 
 			if (DictionaryDatabase.addOrRemoveFavorite(keyWord, false)){
 				//false ->remove
 				
 				Toast toast = Toast.makeText(getApplicationContext(), keyWord + " successfully removed from favorites", Toast.LENGTH_SHORT);
 		 		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
 		 		toast.show();
 		 		Log.d("WEBWORDACt", "favorite removed from db");
 		 		
 		 		//now remove from sharedprefs
 		 		SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);    
 				String favs = settings.getString("favorites", ""); //second "keyword" is just default
 		        SharedPreferences.Editor editor = settings.edit();  
 				String favoritesUpdated = favs.replace(keyWord + ",," , new String("") );
 				Log.d("wwact", "tried to remove fav.. new list of favs is:" +favoritesUpdated );
 				editor.putString("favorites", favoritesUpdated);
 		 		editor.commit();
 			}
 			else{
 				//favorite removing db issues...
 			}
 			
 		}
 		@Override
 		public boolean onKeyDown(int keyCode, KeyEvent event) {
 		    if ((keyCode == KeyEvent.KEYCODE_BACK) ) {
 		    	 commitNote();
 		    	asyncTask.cancel(true); //set iscancelled to true in the asynctask thread. otw, it keeps goin
 		    	Log.d("WEBWORD", "back button pressed");
 		         //i still want the default back..just to do work b4
 		    	finish(); //ie. i want the event to be handled by the next receiver
 				 return true;
 		    }
 		   
 		    return false;
 		}
 	
 		 @Override
 		 protected void onPause() {
 		        super.onPause();	
 		        Log.d("WWAct", "on paused " );
 		      //  noteContent = mBodyText.toString();
 		        //commitNote();  
 		    }
 		 @Override
 		 protected void onResume(){
 			 super.onResume();//bc it crashed without it
 				Log.d("WWAct", "on resumed " ); 
 				
 			displayNoteIfExists();		// ? (ck old: 'i think being called too early...keyword needs to reset'
 			//ck 3/26/11: dunno what that last comment means/.
 		 }
 		 @Override
 		    protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		        super.onRestoreInstanceState(savedInstanceState);
 		       // i think the difference here is that on FIRST open this ISNT called. otherwise oncreate and this both called on restore
 			        if (savedInstanceState != null) { //in case wikem change screen orientation or forced closed
 			        	String temp = savedInstanceState.getString(ORIGINAL_CONTENT);
 			        	if (temp!=null){ //maybe it was null ?kept getting weird null edit text android..widget.edittext@ nonsense
 			        		mBodyText.setText(temp);
 			        		
 			        	}
 			        }
 		        ////    //mBodyText.setText(savedInstanceState.getString(ORIGINAL_CONTENT));}	
 		     }
 		 @Override
 	    protected void onSaveInstanceState(Bundle outState) {
 		        // Save away the original text, so we still have it if the activity
 		        // needs to be killed while paused.
 //5/6/11 added
 			   super.onSaveInstanceState(outState);
 //end added//
 		        outState.putString(ORIGINAL_CONTENT, mBodyText.getText().toString());
 		        commitNote(); //what happens if i put the note here instead
 		        /*
 		         * 
 		         * 1) onSaveInstanceState() is intended for the use to save UI states, I have no question about that.
 				2) onPause() is intended for the use to save persistent data (database, files.. etc), in other words.. data that should persist across application sessions.
 
 		         */
 		    }
 		 
 		 private String doHighlight( String bodyText, String searchTerm){
 			  String newText = "";
 			  int i = -1;
 			  String lcSearchTerm = searchTerm.toLowerCase();
 			  String lcBodyText = bodyText.toLowerCase();
 			  //unused highlight tags.. i assume if using css and span stuffs
 			  String highlightStartTag;
 			  String highlightEndTag;
 			//  if ((!highlightStartTag) || (!highlightEndTag)) {
 				    highlightStartTag = "<font style='color:blue; background-color:yellow;'>";
 				    highlightEndTag = "</font>";
 //				  }
 				    int counter = 1; //start counter at 1
 				    highlightList = new ArrayList<Integer>(); 
 				    iterator = null;//reset the iterator to null here. as the list will be changed fosho
 				    
 				    try{
 					  while (bodyText.length() > 0) {
 					    i = lcBodyText.indexOf(lcSearchTerm, i+1);
 					   // Log.d("WWA", "i is:"+ Integer.toString(i));
 					    if (i < 0) {
 					      newText += bodyText;
 					      bodyText = "";
 					    } else {
 		
 					      // skip anything inside an HTML tag
 					      if (bodyText.lastIndexOf(">", i) >= bodyText.lastIndexOf("<", i)) {
 					        // skip anything inside a <script> block
 					      //  if (lcBodyText.lastIndexOf("/script>", i) >= lcBodyText.lastIndexOf("<script", i)) {
 					    	  
 					    	  
 					    	 // )"<a name="+ "\"" + SearchableDictionary.headers[j]+"\"" +"></a>"  highlightList.add(new Integer(counter); counter++;
 					    	  
 					   //       newText += bodyText.substring(0, i) + highlightStartTag + bodyText.substring(i, searchTerm.length()) + highlightEndTag;
 					    	   //	newText = newText.concat(bodyText.substring(0,i) + highlightStartTag);
 					 //just add the link as just an integer number ..so first highlight will just be 1, second ->2.. etc
 					    	  newText = newText.concat(bodyText.substring(0,i) 
 					    			  +"<a name="+ "\"" + Integer.toString(counter)+ "\"" +"></a>" 
 					    			  + highlightStartTag);
 					    	   	highlightList.add(new Integer(counter)); 
 					    	   	counter++;
 					        	newText = newText.concat(bodyText.substring(i, i + searchTerm.length()) + highlightEndTag);
 		        	
 					          bodyText = bodyText.substring(i + searchTerm.length());
 					          lcBodyText = bodyText.toLowerCase();
 					          i = -1;
 					      //  }
 					      }
 					    }
 					  } //end of while
 					  
 				    }catch (IndexOutOfBoundsException e){Log.d("WWA", "highlight error msg!! " + e.getMessage()); return bodyText;}
 			  return newText;
 			}	 
 		 
 		 private File getAppDir(){
 		/*	 int currentapiVersion = android.os.Build.VERSION.SDK_INT;
 			 File d = null;
 	            if (currentapiVersion >= android.os.Build.VERSION_CODES.FROYO){
 	                // Do something for froyo and above versions
 	   			  d = getExternalFilesDir(null);
 
 	            } else {
 	                // do something for phones running an SDK before froyo
 	            	d = new File(
 				            Environment.getExternalStorageDirectory(), DownloaderTest.BACKUP_PATH); //my datapath location
 				       
 				        }
  	     
 	            return d;
 	        */
 			 File d = null;
 				SharedPreferences settings = getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);
 	       		String path = settings.getString("dl-path", "null");
 	       		if (!path.equals("null")){
 	       			d = new File(path);
 	       			if (d.exists()){ 
 	       				Log.d("dictdatab", "path is " + path);
 		       			return d;
 	       			}
 	       		}
 	       		//shouldn't be null. just incase give it something.
 	       		String defaultPath = settings.getString("default-path", "null");		       			
        			d = new File (defaultPath);
        			return d;
 			 
 		 }
 /*
 		 private static class FixHTML {
 
 			 //fix the image tags. no longer used since now just override the url
 			 //eg. mediawiki parses out to <img alt="ADA DKA.gif" src="/w/images/a/a7/ADA_DKA.gif" width="601" height="787" />
 				public static String fixImages(String s){
 					
 					//so find '/w/images/' and add it to www.wikem.org/
 					//FINAL RESULT: link becomes www.wikem.org/w/images/ bla bla ...
 					if (!s.contains("/w/images")){
 						return s;
 					}
 					else{
 						Log.d("webwordact", "in the fixHTML class fixing image tag");
 						return s.replace("/w/images", "http://www.wikem.org/w/images");
  					}
 					//return (topLinks + s);
 				}
 				
 			}
 		 */
 		 
 		 
 		 private class WikemAPI{
 		    public WikemAPI(){
 		    	//constructor
 		    }
 			 
 			 private  File pathFile;
 	 
 			 private  String parseUrl(String s){
 				 //encode url characters
 				  try {
 					return URLEncoder.encode(s, "utf-8");
 					
 				} catch (UnsupportedEncodingException e) {
  					e.printStackTrace(); asyncTask.cancel(true);
 				}
 				Log.e("wwact" , "error URL-encoding the keyword");
  				 return s;
 				  
 			 }
 			 
 			 public  String updateThisPage(){
 				 try {
 					//TODO: doesnt seem to need &format=xml	
 						//http://www.wikem.org/w/index.php?title=Anaphylaxis&action=render
 						connectToWikem( "http://www.wikem.org/w/index.php?title=" + parseUrl(keyWord) + "&action=render", "tempFile");
 						return getNewPageAsString();
 					} catch (Exception e) {
  						e.printStackTrace();
  						asyncTask.cancel(true);
 					}
 					
 					return null;
 			 }
 			 private  String getNewPageAsString() {
 
 				 /* We have to use the openFileInput()-method
 				 * the ActivityContext provides.
 				 * Again for security reasons with
 				 * openFileInput(...) */
  				 FileInputStream fIn;
 				try {
 					fIn = new FileInputStream (pathFile);
 					/*
 					 * The InputStreamReader class is intended to wrap an InputStream, thereby turning the byte based input stream into a character based Reader.
 					 */
 				
 				 InputStreamReader isr = new InputStreamReader(fIn);
 					Reader in = new BufferedReader(isr,  8 * 1024);
  
 				 StringBuffer buffer = new StringBuffer();				 // Fill the Buffer with data from the file
 				 int ch;
 					while ((ch = in.read()) > -1) {
 						buffer.append((char)ch);
 					}
 				 
 				 // Transform the chars to a String
  		//			Log.d("wwact load new pageasstring" , buffer.toString());
  					return buffer.toString();
 				 } catch (FileNotFoundException e) {
  						e.printStackTrace();
 					} catch (IOException e) {
  					e.printStackTrace();
 				}
 					return null;   		
 				}
 			 //canupdate returns the long of the updated timestamp, otherwise zero
 			public  long canUpdate() throws java.text.ParseException, ClientProtocolException, IOException{
 				 
 				 //use todays time and check for newest www.wikem.org/w/api.php?action=query&prop=revisions&titles=Anaphylaxis&rvlimit=1&rvprop=timestamp&rvdir=older&rvstart=
 				 String url = "http://www.wikem.org/w/api.php?action=query&prop=revisions&titles=" + parseUrl(keyWord) + "&rvlimit=1&rvprop=timestamp&rvdir=older&rvstart=";
 				 
 		//		 String url = "http://www.wikem.org/w/api.php?action=query&prop=revisions&titles=" + parseUrl(keyWord) + "&rvlimit=499&rvprop=timestamp&rvdir=newer&rvstart=";
 				 //get the unixtimestamp... eg
 				 Date timeNow = new Date(); //init to current time
 				 
 				 
 				 
 				 //String  epoch = "1081157732";
 				 //Date epochDate = new Date(Long.parseLong(epoch) * 1000);  //used arbitrary number now...and convet ms
 				 SimpleDateFormat simpleDateFormat =
 				        new SimpleDateFormat("yyyyMMddHHmmss");  //i hope this works...
 				//String dateAsString = simpleDateFormat.format(epochDate);
 				 simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));	
 				String dateAsString = simpleDateFormat.format(timeNow);
 				
 				
 				 Log.d("webword act" , "update stuff " + url + dateAsString + "&format=xml");
 				 String dateFromXMLString = ""; //initialize
 				 URL theUrl = null;;
 				try {
 					theUrl = new URL (url + dateAsString +"&format=xml");
 				} catch (MalformedURLException e1) {
  					e1.printStackTrace(); asyncTask.cancel(true);
 				}
 
 				 try {
 					//connectToWikem(url + dateAsString , "temp" );
 					 dateFromXMLString = parsingXMLforLastRevision(theUrl);					 
 						//now parse the date
 						SimpleDateFormat simpleDateFormat2 =
 				        new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss", new Locale("es", "ES"));  //i hope this works...
 						//	simpleDateFormat2.setTimeZone(TimeZone.getDefault());
 						simpleDateFormat2.setTimeZone(TimeZone.getTimeZone("GMT"));	
 						simpleDateFormat2.setLenient(true);
 							if (dateFromXMLString!=null){
 							try {
 								
 								Date dateLastUpdatedOnline = simpleDateFormat2.parse(dateFromXMLString.replaceAll("\\p{Cntrl}", "")); //some hidden ?characters causing date parse failure?
 								Log.d("wwact", "our server page last updated:" + dateLastUpdatedOnline.toString() );
 
 								Date ourDbEpoch = new Date(Long.parseLong(lastUpdated) * 1000);
 								Log.d("wwact", "our client page last updated:" + ourDbEpoch.toString() );
 								if (ourDbEpoch.before(dateLastUpdatedOnline)){
 									
 									return (dateLastUpdatedOnline.getTime()/1000);//convert ms back to seconds
 								}else{
 									return 0;
 								}
 								 
 								//Log.d("webwordact" , "last revision was " + dateFromXMLString);
 								} catch (Exception e) {
  									e.printStackTrace();asyncTask.cancel(true);
 								}
 							}
 						
 						
 						
 					} catch (ParseException e) {
  						e.printStackTrace();asyncTask.cancel(true);
 					}
 				 
 				
 				 return 0;
 			 }
 
 			 
 			 private  void connectToWikem(String url, String newFileName) 
 			 	throws ClientProtocolException, IOException, FileNotFoundException
 			 
 			 {
 				 	URL u = new URL(url);
 				    HttpURLConnection c = (HttpURLConnection) u.openConnection();
 				    try{
 				    c.setRequestMethod("GET");
 				    c.setDoOutput(true);
 				    c.connect();
 				    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
 				           throw new AndroidRuntimeException(
 				               "External storage (SD-Card) not mounted");
 				       } 
 				    
 				    
 				  /*  File appDbDir = new File(
 				            Environment.getExternalStorageDirectory(), DownloaderTest.BACKUP_PATH); //my datapath location
 				        if (!appDbDir.exists()) {
 				    		Log.d("webword act ->wikemapi", "having to make datapath dir?");
 				            appDbDir.mkdirs();
 				        }*/
 				    File appDbDir = getAppDir();
 				    if (!appDbDir.exists()) {
 			    		Log.d("webword act ->wikemapi", "having to make datapath dir?");
 			            appDbDir.mkdirs();}
 				    pathFile = new File(appDbDir, newFileName + ".htm");
 				    FileOutputStream f = new FileOutputStream(pathFile); //make new file with filename of current wikem entry
 
 				    
 				    InputStream in = c.getInputStream();
 
 				    byte[] buffer = new byte[1024];
 				    int len1 = 0;
 				    while ( (len1 = in.read(buffer)) > 0 ) {
 				         f.write(buffer,0, len1);
 				    }
 				    f.close();
 				 }finally {
 				     c.disconnect();
 				   } 
 			 }
  			 
 		 
 			  
 			 private  String parsingXMLforLastRevision (URL url) 
 			 	throws ClientProtocolException, IOException{
 			         
 			                 try {
 			                         /* Create a URL we want to load some xml-data from. */
 			                    //     URL url = new URL("http://www.anddev.org/images/tut/basic/parsingxml/example.xml");
 			  
 			                         /* Get a SAXParser from the SAXPArserFactory. */
 			                         SAXParserFactory spf = SAXParserFactory.newInstance();
 			                         SAXParser sp = spf.newSAXParser();
 			  
 			                         /* Get the XMLReader of the SAXParser we created. */
 			                         XMLReader xr = sp.getXMLReader();
 			                         /* Create a new ContentHandler and apply it to the XML-Reader*/
 			                         XMLHandler myExampleHandler = new XMLHandler();
 			                         xr.setContentHandler(myExampleHandler);
 			                        
 			                         /* Parse the xml-data from our URL. */
 			                         xr.parse(new InputSource(url.openStream()));
 			                         /* Parsing has finished. */
 			  
 			                         /* Our ExampleHandler now provides the parsed data to us. */
 			                    //     ParsedExampleDataSet parsedExampleDataSet =
 			                          return myExampleHandler.getParsedData();
 			  
 			                         /* Set the result to be displayed in our GUI. */
 			                     //    tv.setText(parsedExampleDataSet.toString());
 			                        
 			                 } catch (Exception e) {
 			                         /* Display any Error to the GUI. */
 			                       //  tv.setText("Error: " + e.getMessage());
 			                	 Log.e("webwordact parsingXML" , e.getMessage());
  			                 }
 			                return null;
 			         }
 		  
  		 }
 		 
 
 	}
 	
 	
 	
 	
 	
 	
  
