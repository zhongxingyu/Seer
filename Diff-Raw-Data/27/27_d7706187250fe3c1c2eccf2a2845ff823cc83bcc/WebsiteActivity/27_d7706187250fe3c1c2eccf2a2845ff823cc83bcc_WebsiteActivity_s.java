 /**
  * Copyright (C) 2011 Adriano Monteiro Marques
  *
  * Author:  Zubair Nabi <zn.zubairnabi@gmail.com>
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  */
 
 package org.umit.icm.mobile.gui;
 
 import java.io.IOException;
 
 import org.apache.http.HttpException;
 import org.umit.icm.mobile.R;
 import org.umit.icm.mobile.connectivity.WebsiteOpen;
 import org.umit.icm.mobile.process.Constants;
 import org.umit.icm.mobile.proto.MessageProtos.WebsiteReport;
 import org.umit.icm.mobile.utils.SDCardReadWrite;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class WebsiteActivity extends Activity{
     /** Called when the activity is first created. */
 	private ListView listView;
 	private Button backButton;
 	private String website;
 	private ProgressDialog progressDialog;
 	
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         progressDialog = ProgressDialog.show(this, 
         		getString(R.string.loading)	, getString(R.string.retrieving_website)
         		, true, false);
         Bundle bundle = this.getIntent().getExtras();
         website = bundle.getString("websiteclicked");
         
         setContentView(R.layout.websiteactivity);
         listView = (ListView)findViewById(R.id.ListView01);                
         
         backButton = (Button) findViewById(R.id.backButton);
         
         backButton.setOnClickListener(new OnClickListener() { 
 	       	public void onClick(View v) {  	       				            		 
 	       		WebsiteActivity.this.finish();
 	       	}
 
	   	}  );
         
         new UpdateFavicon().execute(website);
     }
     
     private class UpdateFavicon extends AsyncTask<String,String,WebsiteTextBitmapAdapter> {
     	  
     	protected void onPostExecute(WebsiteTextBitmapAdapter websiteData) {
     		listView.setAdapter(websiteData);
     		progressDialog.dismiss();
     	}
          
 		protected WebsiteTextBitmapAdapter doInBackground(String... urls) {
 			Bitmap favicon = null;
 			WebsiteTextBitmapAdapter websiteTextBitmapAdapter 
 			= new WebsiteTextBitmapAdapter(WebsiteActivity.this);
 			Resources resources = getResources(); 
 			try {
 				 favicon = WebsiteOpen.getFavicon(website);
 			} catch (IOException e) { 
 				favicon 
 				= BitmapFactory.decodeResource(resources,
 						R.drawable.tabs_icons);
 				e.printStackTrace();
 			} catch (HttpException e) {
 				favicon 
 				= BitmapFactory.decodeResource(resources,
 						R.drawable.tabs_icons);
 				e.printStackTrace();
 			}
 			Drawable drawable = new BitmapDrawable(favicon);
 			websiteTextBitmapAdapter.addItem(new WebsiteTextBitmap(website, drawable));
 			String websiteFilename 
 			= website.substring(11) + Constants.WEBSITE_FILE;
 			try {
 				if(SDCardReadWrite.fileExists(websiteFilename, Constants.WEBSITES_DIR)) {
 					WebsiteReport websiteReport 
 					= SDCardReadWrite.readWebsiteReport(Constants.WEBSITES_DIR, website);
 					if(websiteReport.getReport().getStatusCode() == 200) {
 						websiteTextBitmapAdapter.addItem(
 								new WebsiteTextBitmap( getString(R.string.normal_status), getResources().getDrawable(R.drawable.greendot)));
 					} else {
 						websiteTextBitmapAdapter.addItem(
 								new WebsiteTextBitmap( getString(R.string.differentation_status), getResources().getDrawable(R.drawable.reddot)));
 					}
 					websiteTextBitmapAdapter.addItem(
 							new WebsiteTextBitmap(getString(R.string.status_code) + " " +
 									Integer.toString(websiteReport.getReport().getStatusCode()), drawable));
 					websiteTextBitmapAdapter.addItem(
 							new WebsiteTextBitmap(getString(R.string.throughput) + " " +
 									Integer.toString(websiteReport.getReport().getBandwidth())
 									 + " " + getString(R.string.throughput_unit), drawable));
 					websiteTextBitmapAdapter.addItem(
 							new WebsiteTextBitmap(getString(R.string.response_time) + " " +
 									Integer.toString(websiteReport.getReport().getResponseTime())
 									+ " " + getString(R.string.response_time_unit), drawable));
 				} else {
 					websiteTextBitmapAdapter.addItem(
 							new WebsiteTextBitmap(getString(R.string.no_scan)
 									, getResources().getDrawable(R.drawable.bluedot)));
 				}
 				
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (RuntimeException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			return websiteTextBitmapAdapter;
 			 						
 		}			
     }
 }
