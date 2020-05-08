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
 
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 
 import android.app.ProgressDialog;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.SpannableString;
 import android.text.style.StyleSpan;
 import android.util.Log;
 import android.view.View;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.normalexception.forum.rx8club.R;
 import com.normalexception.forum.rx8club.handler.ForumImageHandler;
 import com.normalexception.forum.rx8club.task.PmTask;
 import com.normalexception.forum.rx8club.utils.HtmlFormUtils;
 import com.normalexception.forum.rx8club.utils.PreferenceHelper;
 import com.normalexception.forum.rx8club.utils.VBForumFactory;
 
 public class PrivateMessageViewActivity extends ForumBaseActivity {
 
 	private final String TAG = "PrivateMessageViewActivity";
 	
 	private String postUser = null;
 	private String postText = null;
 	private String securityToken = null;
 	private String recipients = null;
 	private String pmid = null;
 	private String title = null;
 	
 	/*
 	 * (non-Javadoc)
 	 * @see com.normalexception.forum.rx8club.activities.ForumBaseActivity#onSaveInstanceState(android.os.Bundle)
 	 */
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putString("postUser", postUser);
 		outState.putString("securitytoken", securityToken);
 		outState.putString("postText", postText);
 		outState.putString("recipients", recipients);
 		outState.putString("pmid", pmid);
 		outState.putString("title", title);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see com.normalexception.forum.rx8club.activities.ForumBaseActivity#onRestoreInstanceState(android.os.Bundle)
 	 */
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		try {
 			if(savedInstanceState != null) {
 				postText = savedInstanceState.getString("postText");
 				securityToken = savedInstanceState.getString("securitytoken");
 				postUser = savedInstanceState.getString("postUser");
 				recipients = savedInstanceState.getString("recipients");
 				pmid = savedInstanceState.getString("pmid");
 				title = savedInstanceState.getString("title");
 			}				
 		} catch (Exception e) {
 			Log.e(TAG, "Error Restoring Contents: " + e.getMessage());
 			BugSenseHandler.sendException(e);
 		}
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
 	 */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         super.setTitle("RX8Club.com Forums");
         setContentView(R.layout.activity_private_message_view);
         
         this.registerGuiButtons();
         
         findViewById(R.id.submitButton).setOnClickListener(this);
         
         Log.v(TAG, "PM View Activity Started");
         
         if(savedInstanceState == null)
         	constructView();
         else {
         	updateView();
         }
     }
     
     /**
      * Construct the view elements
      */
     private void constructView() {
     	loadingDialog = ProgressDialog.show(this, "Loading", "Please wait...", true);
         
         updaterThread = new Thread("PrivateMessageThread") {
 			public void run() {
 				String link = 
 		        		(String) getIntent().getStringExtra("link");
 				Document doc = 
 						VBForumFactory.getInstance().get(
 								VBForumFactory.getRootAddress() + "/" + link);
 				securityToken =
 						HtmlFormUtils.getInputElementValue(doc, "securitytoken");
 				
 				pmid =
 						HtmlFormUtils.getInputElementValue(doc, "pmid");
 				
 				title =
 						HtmlFormUtils.getInputElementValue(doc, "title");
 				
 				Elements postMenu = doc.select("div[id=postmenu_]");
 				
 				postUser = postMenu.text();
 				
 				Elements postMessage = doc.select("div[id=post_message_]");
 				
 				postText = postMessage.html();
 		    	
 				loadingDialog.dismiss();	
 				
 				updateView();
 			}
         };
         updaterThread.start();
     }
     
     /**
      * Update the view information with the saved class objects
      */
     private void updateView() {
     	runOnUiThread(new Runnable() {
     		public void run() {
     			tl = (TableLayout)findViewById(R.id.myTableLayoutPM);
     			
     			addRow(Color.DKGRAY, false, postUser);
     			addRow(Color.GRAY, true, reformatQuotes(postText));
     		}
     	});
     }
     
     /**
      * Add a row to the view table
      * @param clr	The color of the background
      * @param texts	The text array that will be added to each row
      */
     private void addRow(int clr, boolean html, String... texts) {
     	/* Create a new row to be added. */
     	TableRow tr_head = new TableRow(this);
     	tr_head.setId(31);
     	
     	int style = Typeface.NORMAL;
     	int index = 0;
     	for(String text : texts) {
 	    	TextView b = new TextView(this);
 	    	b.setId(32);
 	    	b.setTextColor(Color.WHITE);
 	    	b.setTextSize((float) PreferenceHelper.getFontSize(this));
 	    	b.setPadding(5,5,5,5);
 	    	tr_head.setBackgroundColor(clr);
 			SpannableString spanString = null;
 			
 			if(!html) {
 				spanString = new SpannableString("Private Message From " + text);
 				spanString.setSpan(new StyleSpan(style), 0, text.length(), 0);
 			}
 			
 			ForumImageHandler imageHandler = new ForumImageHandler(b, this);
 			b.setText(html? Html.fromHtml(text + "<br><br><br>", imageHandler, null) : spanString);
 			b.setOnClickListener(this);
 			
 			/* Add Button to row. */
 	        TableRow.LayoutParams params = new TableRow.LayoutParams();
 	        if(index == 0) params.weight = 1f;
 	        tr_head.addView(b,params);
 	        index++;
     	}
         
         /* Add row to TableLayout. */
         tl.addView(tr_head, tl.getChildCount() - 3);
     }
     
     /*
    	 * (non-Javadoc)
    	 * @see android.view.View.OnClickListener#onClick(android.view.View)
    	 */
    	@Override
    	public void onClick(View arg0) {
    		super.onClick(arg0);
    		
    		switch(arg0.getId()) {	
    		case R.id.submitButton:
    			Log.v(TAG, "PM Submit Clicked");
    			String toPost = 
 					((TextView)findViewById(R.id.postBox)).getText().toString();
 			PmTask sTask = 
					new PmTask(this, this.securityToken, this.title, toPost, this.postUser, this.pmid);
 			sTask.execute();
    			break;
    		}
    	}
 
     /*
      * (non-Javadoc)
      * @see com.normalexception.forum.rx8club.activities.ForumBaseActivity#enforceVariants(int, int)
      */
 	@Override
 	protected void enforceVariants(int currentPage, int lastPage) {
 		// TODO Auto-generated method stub
 	}
 }
