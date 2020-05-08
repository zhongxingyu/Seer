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
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.umit.icm.mobile.R;
 import org.umit.icm.mobile.connectivity.Website;
 import org.umit.icm.mobile.process.Constants;
 import org.umit.icm.mobile.process.Globals;
 import org.umit.icm.mobile.utils.SDCardReadWrite;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class WebsiteFilterActivity extends Activity{
    	
 	private ListView listView;	
 	Button backButton, selectAllButton, unselectAllButton;
 	private WebsiteTextCheckboxAdapter websiteTextCheckboxAdapter;	
 	private List<WebsiteTextCheckbox> listWebsitesCheckbox;
 	private String currentURL;
 	private Website currentWebsite;
 	private boolean currentCheck;
 		
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.websitefilteractivity);        
         listWebsitesCheckbox 
         = new ArrayList<WebsiteTextCheckbox>();
         Iterator<Website> iterator = Globals.websitesList.iterator();
                         
         backButton = (Button) findViewById(R.id.backButton);        
         backButton.setOnClickListener(new OnClickListener() { 
 	       	public void onClick(View v) {  	   
 	       		Iterator<WebsiteTextCheckbox> iteratorCheck 
 	       		= listWebsitesCheckbox.iterator();	       		
 	       		int i = 0;
 	       		String check = "";
 	       		Website website = new Website();
 	       		WebsiteTextCheckbox websiteTextCheckbox = null;
 	    		while(iteratorCheck.hasNext()){ 
 	    			websiteTextCheckbox = iteratorCheck.next();
 	    			if(websiteTextCheckbox.isCheck() == true) {
 	    				check = "true";	    				
 	    			}    				
 	    			else
 	    				check = "false";
 	    			website = Globals.websitesList.get(i);
 	    			website.setCheck(check);
 	    			Globals.websitesList.set(i, 
 	    					website);						       				    			
 	    			i++;
 	            } 
 	    		try {
 					SDCardReadWrite.writeWebsitesList(Constants.WEBSITES_DIR,
 							Globals.websitesList);
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (RuntimeException e) {
 					e.printStackTrace();
 				}
 	       		WebsiteFilterActivity.this.finish();	       			             
 	       	}
 
 	   	}  );
         
         selectAllButton = (Button) findViewById(R.id.selectAllButton);        
         selectAllButton.setOnClickListener(new OnClickListener() { 
 	       	public void onClick(View v) {  	   
	       		Iterator<WebsiteTextCheckbox> iteratorCheck 
	       		= listWebsitesCheckbox.iterator();	  	       		
 	       		while(iteratorCheck.hasNext()) {
 	       			iteratorCheck.next().setCheck(true);	       			
 	       		}
 	       		websiteTextCheckboxAdapter.setListItems(listWebsitesCheckbox);        	
 	            listView.setAdapter(websiteTextCheckboxAdapter);
 	       	}
 	   	}  );
         
         unselectAllButton = (Button) findViewById(R.id.unselectAllButton);        
         unselectAllButton.setOnClickListener(new OnClickListener() { 
 	       	public void onClick(View v) {  	   
 	       		Iterator<WebsiteTextCheckbox> iteratorCheck 
 	       		= listWebsitesCheckbox.iterator();	  	       		
 	       		while(iteratorCheck.hasNext()) {
 	       			iteratorCheck.next().setCheck(false);	       			
 	       		}
 	       		websiteTextCheckboxAdapter.setListItems(listWebsitesCheckbox);        	
 	            listView.setAdapter(websiteTextCheckboxAdapter);	
 	       	}
 	   	}  );
 
                     
         listView = (ListView)findViewById(R.id.ListView01);
         websiteTextCheckboxAdapter 
         = new WebsiteTextCheckboxAdapter(WebsiteFilterActivity.this);
                 
         while(iterator.hasNext()){               
 			currentWebsite = iterator.next();
 			currentURL = currentWebsite.getUrl();
 			if (currentWebsite.getCheck().equals("true"))
 				currentCheck = true;
 			else 
 				currentCheck = false;			
 			listWebsitesCheckbox.add(new WebsiteTextCheckbox(currentURL, currentCheck));						       			
         }  
         
         websiteTextCheckboxAdapter.setListItems(listWebsitesCheckbox);        	
         listView.setAdapter(websiteTextCheckboxAdapter);
                   		                       
     }
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event)  {
         if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
         	Iterator<WebsiteTextCheckbox> iteratorCheck 
        		= listWebsitesCheckbox.iterator();	       		
        		int i = 0;
        		String check = "";
        		Website website = new Website();
        		WebsiteTextCheckbox websiteTextCheckbox = null;
     		while(iteratorCheck.hasNext()){ 
     			websiteTextCheckbox = iteratorCheck.next();
     			if(websiteTextCheckbox.isCheck() == true) {
     				check = "true";	    				
     			}    				
     			else
     				check = "false";
     			website = Globals.websitesList.get(i);
     			website.setCheck(check);
     			Globals.websitesList.set(i, 
     					website);						       				    			
     			i++;
             } 
     		try {
 				SDCardReadWrite.writeWebsitesList(Constants.WEBSITES_DIR,
 						Globals.websitesList);
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (RuntimeException e) {
 				e.printStackTrace();
 			}
        		WebsiteFilterActivity.this.finish();	 
             return true;
         }
 
         return super.onKeyDown(keyCode, event);
     }
  	          
 }
