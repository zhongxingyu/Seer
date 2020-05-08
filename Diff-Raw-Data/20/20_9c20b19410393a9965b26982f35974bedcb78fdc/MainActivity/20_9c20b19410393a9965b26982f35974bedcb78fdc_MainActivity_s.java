 package com.normalexception.forum.rx8club.activities;
 
 /************************************************************************
  * NormalException.net Software, and other contributors
  * http://www.normalexception.net
  * 
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  ************************************************************************/
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.normalexception.forum.rx8club.R;
 import com.normalexception.forum.rx8club.utils.LoginFactory;
 import com.normalexception.forum.rx8club.utils.PreferenceHelper;
 import com.normalexception.forum.rx8club.utils.UserProfile;
 import com.normalexception.forum.rx8club.utils.VBForumFactory;
 import com.normalexception.forum.rx8club.view.ViewContents;
 
 /**
  * Main activity for the application.  This is the main forum view that 
  * is displayed after login has been completed.
  * 
  * Required Intent Parameters:
  * none
  */
 public class MainActivity extends ForumBaseActivity implements OnClickListener {
     
     private Map<String,String> linkMap;
     
 	private static final String TAG = "Application";
 	
 	private Map<String, Collection<?>> mainForumContainer;
 	
 	/*
 	 * (non-Javadoc)
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
     @SuppressWarnings("unchecked")
 	@Override
     public void onCreate(Bundle savedInstanceState) {
     	try {
 	    	Log.v(TAG, "Application Started");
 	    	
 	        super.onCreate(savedInstanceState);
 	        super.setTitle("RX8Club.com Forums");
 	        
 	        setContentView(R.layout.activity_main);
 	        
 	     	// Register the titlebar gui buttons
 	        this.registerGuiButtons();
 			    
 	        if(savedInstanceState == null)
 	        	constructView();
 	        else {
 	        	viewContents = (ArrayList<ViewContents>) savedInstanceState.getSerializable("contents");
 	        	updateView(viewContents);
 	        }
     	} catch (Exception e) {
     		Log.e(TAG, "Fatal Error In Main Activity! " + e.getMessage());
     		BugSenseHandler.sendException(e);
     	}
     }
     
     /**
      * Start the application activity
      */
     private void constructView() {
     	loadingDialog = ProgressDialog.show(this, "Loading", "Please wait...", true);
         final Activity thisActivity = this;
         
         /**
          * Thread created to list the contents of the forum into
          * the screen.  The screen contains a table layout, and
          * each category and each forum is inserted as a row
          */
         updaterThread = new Thread("Updater") {
         	public void run() {
         		try {
 	        		Log.v(TAG, "Updater Thread Started");
 	        		
 	        		Document doc = null;
 	        		viewContents = new ArrayList<ViewContents>();
 	        		linkMap = new LinkedHashMap<String,String>();
 	        		
 	        		// User information
 	        		if(!UserProfile.isInitialized()) {
 	        			doc = 
 	        				VBForumFactory.getInstance().get(VBForumFactory.getRootAddress());
 	        			Elements userElement = 
 	        				doc.select("a[href^=http://www.rx8club.com/members/" + UserProfile.getUsername().replace(".", "-") + "]");
 	        			UserProfile.setUserProfileLink(userElement.attr("href"));
 	        		}
 	        		
 	        		doc = getForum();
 	        		
 	                mainForumContainer = getCategories(doc);
 	                
 	                Log.v(TAG, "Document Container Read, size=" + mainForumContainer.size());
 	                
 	                viewContents.add(new ViewContents(Color.BLUE, new String[]{"Forum","Threads","Posts"}, 40, false));
 
 	                Iterator<?> it = mainForumContainer.entrySet().iterator();
 	                while (it.hasNext()) {
 	        			@SuppressWarnings("unchecked")
 						Map.Entry<String, List<String>> pairs = (Map.Entry<String, List<String>>)it.next();
 	                    
 	                    // Always add view objects on a Ui Thread
 	                    viewContents.add(new ViewContents(Color.DKGRAY, pairs.getKey().split(""), 10, true));           	
 	                    
 	                    List<String> vals = pairs.getValue();
 	                    for(final String val : vals) {
 	                    	 viewContents.add(new ViewContents(Color.GRAY, val.split(""), 20, false));  
 	                    }
 	                    
 	                    // avoids a ConcurrentModificationException
 	                    it.remove(); 
 	                }
 	                
 	                updateView(viewContents);
 	                Log.v(TAG, "Dismissing Wait Dialog");
         		} catch(Exception e) {
         			BugSenseHandler.sendException(e);
         			thisActivity.runOnUiThread(new Runnable() {
         				public void run() {
         					Toast.makeText(thisActivity, 
                 					"Sorry, there was an error connecting!", Toast.LENGTH_SHORT).show();
         				}
         			});
         		} finally {
         			if(loadingDialog != null)
         				loadingDialog.dismiss();
         		}
         	}
         };
         updaterThread.start();
     }
     
     /**
      * Update the view contents
      * @param contents	List of view rows
      */
     private void updateView(final ArrayList<ViewContents> contents) {
     	runOnUiThread(new Runnable() {
     		public void run() {
     			tl = (TableLayout)findViewById(R.id.myTableLayout);
     			tl.setColumnStretchable(0, true);
     			for(ViewContents view : contents) {
     				addRow(view.getClr(), view.getTexts(), view.getId(), view.isSpan());
     			}
     		}
     	});
     }
     
     /**
      * Add a row to the view
      * @param clr	The background color of the row
      * @param text	The text for the row
      * @param id	The id of the row
      */
     private void addRow(int clr, String texts[], int id, boolean span) {
     	// Create a new row to be added.
     	TableRow tr_head = new TableRow(this);
     	tr_head.setId(id);
     	tr_head.setBackgroundColor(clr);
 
     	int index = 0;
     	for(String text : texts) {
 	    	// Create a Button to be the row-content.
 	    	TextView b = new TextView(this);
 	    	b.setId(id);
 	    	b.setText(text);
 	    	b.setOnClickListener(this);
 	    	b.setTextSize((float) PreferenceHelper.getFontSize(this));
 	    	b.setTextColor(Color.WHITE);
 	        b.setPadding(5, 5, 5, 5);
 
 	        if(index == 0) {
 	        	// Convert dip to px
 	        	Resources r = getResources();
         		int px = 
         			(int)TypedValue.applyDimension(
         					TypedValue.COMPLEX_UNIT_DIP, 0, r.getDisplayMetrics());
         		b.setWidth(px);
 	        }
 	
 	        TableRow.LayoutParams params = new TableRow.LayoutParams();
 	        params.span = span? 5 :	1;  
 	        if(index == 0) params.weight = 1f;
 	        tr_head.addView(b,params);
 	        index++;
     	}
 
     	// Add row to TableLayout.
         tl.addView(tr_head);
     }
     
     /**
      * Get the forum contents as a Map that links the category names
      * to a list of the forums within each category
      * @param forum	The full forum document
      * @return		A map of the categories to the forums
      */
     private Map<String, Collection<?>> getCategories(Document forum) {
     	Elements tborders = forum.select("table.tborder");
         Element threads = tborders.get(LoginFactory.getInstance().isLoggedIn()? 2 : 5);
         Elements tbodies = threads.getElementsByTag("tbody");
         
         Map< String, Collection<?> > container = new LinkedHashMap< String, Collection<?> >();
         List<String> categories = new ArrayList<String>();
         
         Elements as = null;
         for(Element tbody : tbodies) {
         	Elements tcats = tbody.getElementsByClass("tcat");
     		if(!tcats.isEmpty()) {
 	        	 as = tcats.get(0).getElementsByTag("a");
 	        	 container.put(as.get(1).text(), categories = new ArrayList<String>());
     		} else {
 	        	Elements alts = tbody.getElementsByClass("alt1Active");
 	        	Elements alt1 = tbody.getElementsByClass("alt1");
 	        	Elements alt2 = tbody.getElementsByClass("alt2");
 	        	
 	        	int yindex = 0, zindex = 2;
 	        	for(Element alt : alts) {
 		        	Elements tds = alt.getElementsByTag("strong");
 		        	Elements href = alt.getElementsByTag("a");
 		        	if(!href.isEmpty()) {
 			        	for(Element strong : tds) {
 			        		categories.add(strong.text() + "" + 
 			        				alt1.get(yindex).text() + "" + 
 			        				alt2.get(zindex).text());
 			        		linkMap.put(strong.text(), href.get(0).attr("href"));
 			        	}
 		        	
 			        	yindex++;
 			        	zindex += 3;
 		        	}
 	        	}
     		}
         }
         
         return container;
     }
 
     /**
      * Grab the forum as a jsoup document
      * @return	A jsoup document object that contains the 
      * 			forum contents
      */
     public Document getForum() {    	
 		LoginFactory lf = LoginFactory.getInstance();
 		
 		String output = "";
 		
 		try {
 			VBForumFactory ff = VBForumFactory.getInstance();
 			output = ff.getForumFrontpage(lf);
 		} catch (IOException ioe) {
 			Log.e(TAG, "Error Grabbing Forum Frontpage: " + ioe.getMessage());
 			BugSenseHandler.sendException(ioe);
 		}		
 		
 	   	return Jsoup.parse(output);
     }
 
     /*
      * (non-Javadoc)
      * @see android.view.View.OnClickListener#onClick(android.view.View)
      */
 	@Override
 	public void onClick(View arg0) {
 		super.onClick(arg0);
		
		switch(arg0.getId()) {
			
			default:
				Log.v(TAG, "Category Clicked");
				TextView tv = (TextView)arg0;
				final String link = linkMap.get(tv.getText());
				Log.v(TAG, "User Clicked: " + link);
				
				// Refresh the display
				new Thread("RefreshDisplayList") {
					public void run() {
						Intent intent = 
								new Intent(MainActivity.this, CategoryActivity.class);
						intent.putExtra("link", link);
						startActivity(intent);
					}
				}.start();	
				break;
		}	
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.normalexception.forum.rx8club.activities.ForumBaseActivity#enforceVariants(int, int)
 	 */
 	@Override
 	protected void enforceVariants(int currentPage, int lastPage) {
 		// Do Nothing
 	} 
 }
