 package com.dj.craigslistapp;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import net.htmlparser.jericho.Source;
 import net.htmlparser.jericho.StartTag;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class ListingContent  extends Activity {
 	private static final String TAG = "ListingContentActivity";
 	private FreebieApp app;
 	TextView tv;
 	ListView lv;
	
     String[] cityNames = new String[50];
     String[] URLS = new String[50];
 	
 	    /** Called when the activity is first created. */
 	    @Override
 	    public void onCreate(Bundle savedInstanceState) {
 	        super.onCreate(savedInstanceState);
 			setContentView(R.layout.listingcontent);
 			
 			// for getting a message if necessary 
 //			state = (String) getIntent().getSerializableExtra("message");
 			
 			// Find Views 
 			app = (FreebieApp)getApplication();
 			tv = (TextView) findViewById(R.id.listingcontenttext);
 //			lv = (ListView) findViewById(R.id.subcitylist);
 	        
 	        // Set properties 
			tv.setMovementMethod(new ScrollingMovementMethod());
 	        tv.setText(app.getState() + " - " + app.getCity() + "\n");
 
//	        ImageView Image01 = new ImageView(this);
	        ImageView Image01 = (ImageView) findViewById(R.id.imageView1);
 	        Source source = getSource();
 
 	        String userbody = source.getElementById("userbody").getContent().toString();
 	        if(!userbody.equals("") || userbody != null)
 	        {
 	        	if(!app.isPic())
 	        		tv.append(userbody.substring(0, userbody.indexOf("<!--")));		//userbody.substring(0,userbody.indexOf(string) ));
 	        	else
 	        	{
 	        		tv.append(userbody.substring(0, userbody.indexOf("<script")));
 	        		
 	    	        List<StartTag> images =  null;
 	    	        ArrayList<ImageView> imagesArray = new ArrayList<ImageView>();
 	    			try{
 	    				images = source.getElementById("userbody").getAllElementsByClass("iw").get(0).getAllStartTags("img");
 	    				Iterator<StartTag> it = images.iterator();
 	    				int i = 0;
 //	    				while (it.hasNext()) {
 //	    					tv.append(it.next().toString() + "\n");      
 	    					String url = it.next().getAttributeValue("src");
 	    					tv.append(url);
 	    					Drawable image =ImageOperations(this,url);
	    					if( image == null)
	    						tv.append("this fucked up");
 	    					Image01.setImageDrawable(image);
	    					
 //	    					imagesArray.add(new ImageView(this));
 //	    					imagesArray.get(i).findViewById(R.id.imageView1);
 //	    					imagesArray.get(i).setImageDrawable(image);
 //	    					i++;
 //	    				}
 	    	        }
 	    	        catch(Exception ex)
 	    	        {
 	    	            ex.printStackTrace();
 	    	        }
 
 	    			int width = 300;
 	    			int height = 300;
//	    	        Image01.setMinimumWidth(width);
//	    	        Image01.setMinimumHeight(height);
//
//	    	        Image01.setMaxWidth(width);
//	    	        Image01.setMaxHeight(height);
 	        	}
 	        		
 	        }
 	        else
 	        	tv.append("error");
 	        
 	        
 //	        
 //			// Listener for city clicked...
 //	        lv.setOnItemClickListener(new OnItemClickListener() {
 //	        	public void onItemClick(AdapterView<?> parent, View view,
 //	        		int position, long id) {
 //	        		app.setCity(app.getCity().concat(" " + cityNames[position]));
 //	        		app.setLocationURL(app.getLocationURL().concat(URLS[position]));
 //	            	Log.d(TAG,"onClicked");
 //	            	Intent i = new Intent(SubCityList.this,FreebiehunterActivity.class);
 //	            	startActivity(i);
 //	        	}
 //	        });
 			
 	    }
 	    
 		private Source getSource() {
 
 			Source source = null;
 			try {
 				source = new Source(new URL(app.getListingURL()));
 			} catch (Exception e){
 				tv.append("\n" + e.getClass().getName());
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				tv.append("\nfail in getting website");
 			}
 			source.fullSequentialParse();
 			return source;
 
 		}
 		
 		public Object fetch(String address) throws MalformedURLException,
 	    IOException {
 	        URL url = new URL(address);
 	        Object content = url.getContent();
 	        return content;
 	    }  
 
 	    private Drawable ImageOperations(Context ctx, String url) {
 	        try {
 	            InputStream is = (InputStream) this.fetch(url);
 	            Drawable d = Drawable.createFromStream(is, "src");
 	            return d;
 	        } catch (MalformedURLException e) {
 	            return null;
 	        } catch (IOException e) {
 	            return null;
 	        }
 	    }
 
 }
