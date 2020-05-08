 package com.ocforums.application;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.htmlcleaner.TagNode;
 
 import com.ocforums.application.R;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 
 
 public class ThreadActivity extends Activity{
 
     List<String> foutput = new ArrayList<String>();
     List<List<String>> foutput2 = new ArrayList<List<String>>();
     List<String> houtput = new ArrayList<String>();
     Integer lpage;
     Integer cpage;
     String turl;
     
     CustomListView lv = new CustomListView();
     String hurl;
     List<String> hhrefslist;
 	private ProgressDialog pd;
 	//OCForumsAppActivity getter = new OCForumsAppActivity();
 
 	@Override
 	protected void onStop() {
 	    super.onStop();
 	    findViewById(R.id.listView3).setVisibility(View.GONE);
 	}
 
 	@Override
 	protected void onRestart() {
 	    super.onRestart();
 	    ListView listview = (ListView) findViewById(R.id.listView3);
 	    listview.setAdapter(new CustomListView.MyCustomAdapter2(ThreadActivity.this, R.layout.row2, foutput2));
 	    findViewById(R.id.listView3).setVisibility(View.VISIBLE);
 	}
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
         
         String qhurl = CustomListView.gethurl();
         String shurl = "http://www.overclockers.com/forums/"+qhurl.replaceFirst("p=", "styleid=31&p=");
         Log.i("url", shurl);
         pd = ProgressDialog.show(ThreadActivity.this, "Working...", "request to server", true, false);
         new ParseThread().execute(shurl);
         Log.i("where ami?","back from execution");
         setContentView(R.layout.header);
         
         
         //addListenerOnButton();
 
 
         Log.i("Adding","Button Listeners");
 		Log.i("view id lpage",Integer.toString(R.id.LastPage));
 		Log.i("view id fpage",Integer.toString(R.id.FirstPage));
 		Log.i("view id npage",Integer.toString(R.id.NextPage));
 		Log.i("view id ppage",Integer.toString(R.id.PreviousPage));
         Button fbutton = (Button) findViewById(R.id.FirstPage);
         //fbutton.setOnClickListener(onclick);
         Button lbutton = (Button) findViewById(R.id.LastPage);
         //lbutton.setOnClickListener(this);
         Button pbutton = (Button) findViewById(R.id.NextPage);
         //pbutton.setOnClickListener(this);
         Button nbutton = (Button) findViewById(R.id.PreviousPage);
         //nbutton.setOnClickListener(this);
 
   
         
         fbutton.setOnClickListener(new OnClickListener(){
     	    public void onClick(View view) {
 				String shurl = "http://www.overclockers.com/forums/"+turl+"&styleid=31"+"&page=1";
 		        Log.i("url", shurl);
 		        pd = ProgressDialog.show(ThreadActivity.this, "Working...", "request to server", true, false);
 		        new ParseThread().execute(shurl);
     	    }
 			
         });
         
         lbutton.setOnClickListener(new OnClickListener(){
     	    public void onClick(View view) {
 		        String shurl = "http://www.overclockers.com/forums/"+turl+"&styleid=31"+"&page="+Integer.toString(lpage);
 		        Log.i("url", shurl);
 		        pd = ProgressDialog.show(ThreadActivity.this, "Working...", "request to server", true, false);
 		        new ParseThread().execute(shurl);
     	    }
 			
         });
         
         pbutton.setOnClickListener(new OnClickListener(){
     	    public void onClick(View view) {
 				if(cpage != 1){
 					   String shurl4 = "http://www.overclockers.com/forums/"+turl+"&styleid=31"+"&page="+Integer.toString(cpage-1);
 			           Log.i("url", shurl4);
 			           pd = ProgressDialog.show(ThreadActivity.this, "Working...", "request to server", true, false);
 			           new ParseThread().execute(shurl4);
 			
 					}
     	    }
 			
         });
         
         nbutton.setOnClickListener(new OnClickListener(){
     	    public void onClick(View view) {
 				if(cpage != lpage){
 					   String shurl3 = "http://www.overclockers.com/forums/"+turl+"&styleid=31"+"&page="+Integer.toString(cpage+1);
 			           Log.i("url", shurl3);
 			           pd = ProgressDialog.show(ThreadActivity.this, "Working...", "request to server", true, false);
 			           new ParseThread().execute(shurl3);
 			
 					}
     	    }
 			
         });
          
     }
     
 
     
     
     @Override  
     public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
     super.onCreateContextMenu(menu, v, menuInfo);  
      menu.setHeaderTitle("Post Options");  
      menu.add(0, v.getId(), 0, "Quote");  
      menu.add(0, v.getId(), 0, "Reply to Thread");
      menu.add(0, v.getId(), 0, "Report post");
  }  
     
     @Override  
     public boolean onContextItemSelected(MenuItem item) {  
             if(item.getTitle()=="Quote"){Quote(item.getItemId());}  
         else if(item.getTitle()=="Reply to Thread"){ReplyToThread(item.getItemId());}
         else if(item.getTitle()=="Report post"){ReportPost(item.getItemId());}
         else {return false;}  
     return true;  
     }
     
     public void ReplyToThread(int id){
     	Toast.makeText(getApplicationContext(), id, Toast.LENGTH_LONG).show();
     	
     }
     
     public void Quote(int id){
     	Toast.makeText(getApplicationContext(), id, Toast.LENGTH_LONG).show();
     	
     }
     
     public void ReportPost(int id){
     	Toast.makeText(getApplicationContext(), id, Toast.LENGTH_LONG).show();
     	
     }
     
     public class ParseThread extends AsyncTask<String, Void, List<List<String>>> {
    	List<List<String>> combined2d = new ArrayList<List<String>>();
         protected List<List<String>> doInBackground(String... arg) {

             List<String> usernamepost = new ArrayList<String>();
             List<String> usernamecolor = new ArrayList<String>();
             List<String> datepost = new ArrayList<String>();
             List<String> messagepost = new ArrayList<String>();
             List<String> hrefs = new ArrayList<String>();
             String fontss = new String();
 
             try
             {
                 HtmlHelper hh = new HtmlHelper(new String(arg[0]));
                 List<TagNode> links = hh.getPost("post");
                 List<TagNode> linksp = hh.getPage("font-weight:normal");
                 List<TagNode> linkstu = hh.getThreadUrl();
                 Log.i("links",Integer.toString(links.size()));
                 for (Iterator<TagNode> iterator = links.iterator(); iterator.hasNext();)
                 {
                   TagNode divElement = (TagNode) iterator.next();
                   TagNode StrongElements[] = divElement.getElementsByName("strong", true);
                   Log.i("StrongElements",Integer.toString(StrongElements.length));
                   Log.i("Checking for:","name and color");
                   for (int j = 0; StrongElements != null && j < StrongElements.length; j++)
                   {
                     TagNode linkElements[] = StrongElements[j].getElementsByName("a", true);
                     Log.i("LinkElements",Integer.toString(linkElements.length));
                     for (int i = 0; linkElements != null && linkElements.length > 0 && i < linkElements.length; i++)
                     {
                         String classType = linkElements[i].getAttributeByName("class"); 
                         Log.i("class",linkElements[i].getAttributeByName("class"));
                         if (classType != null && classType.equals("bigusername"))
                         {
                         	Log.i("Find Any usernames?","MAYBE!");
                         	usernamepost.add(linkElements[i].getText().toString());
                         
                         TagNode fonts[] = linkElements[i].getElementsByName("font", true);
                         Log.i("fonts",Integer.toString(fonts.length));
                         if(fonts.length > 0){
                         for (int k = 0; fonts != null && k < fonts.length; k++){
                         	fontss = fonts[k].getAttributeByName("color");
                         
                         	if(fontss != null){
                         usernamecolor.add(fonts[k].getAttributeByName("color").toString());
                         	}
                         	else{
                         usernamecolor.add("#FFFFFF");		
                         	}
                         }
                     }
                     	else{
                     usernamecolor.add("#FFFFFF");		
                     	}
                       }
                         else{
                         Log.i("Find Any usernames?","Nope");	
                         }
                       
                 }
                }
                     //output.add(divElement.getText().toString());
                     //hrefs.add(divElement.getAttributeByName("href").toString());
                     TagNode linkElements2[] = divElement.getElementsByName("td", true);
                     Log.i("Checking for:","date");
                     //for (int i = 0; linkElements2 != null && i < linkElements2.length; i++)
                     {
                         String classType = linkElements2[0].getAttributeByName("class");
                         
                         if (classType != null && classType.matches("thead"))
                         {
                         	//if(linkElements2[0].getText().toString().matches(".*:.*")){
                            Log.i("Date:","FOUND!");
                            datepost.add(linkElements2[0].getText().toString().replaceAll("\\n", ""));
                            Log.i("date found:",linkElements2[0].getText().toString().replaceAll("\\n", ""));
                         	//}
                         }
                     }
                     TagNode linkElements3[] = divElement.getElementsByName("div", true);
                     Log.i("Checking for:","message");
                     for (int i = 0; linkElements3 != null && i < linkElements3.length; i++)
                     {
                         String classType = linkElements3[i].getAttributeByName("class");
                         
                         if (classType != null && classType.matches("post_message"))
                         {
                             messagepost.add(linkElements3[i].getText().toString());
                             
                         }
                     }
                     
                     
                 }
                 for (Iterator<TagNode> iterator = linkstu.iterator(); iterator.hasNext();)
                 {
                 	TagNode divElement = (TagNode) iterator.next();
                 	Log.i("turl",divElement.getAttributeByName("href").toString()); 
 					hrefs.add(divElement.getAttributeByName("href").toString());	
                 	
                 }

                if(linksp.size() > 1){
                    String tpage = linksp.get(0).getText().toString().replaceAll("Page ", "");
                    String[] values = tpage.split(" of ");
                    cpage = Integer.parseInt(values[0]);
                    lpage = Integer.parseInt(values[1]);
                }
                else{
                	cpage = 1;
                	lpage = 1;
                }
                 turl = hrefs.get(0).replaceAll("http://www.overclockers.com/forums/","");
                 
                 Log.i("current page #",Integer.toString(cpage));
                 Log.i("current page #",Integer.toString(lpage));
                 Log.i("Thread url",turl);
                 
                 combined2d.add(usernamepost);
                 combined2d.add(usernamecolor);
                 combined2d.add(datepost);
                 combined2d.add(messagepost);
                 
 
             }
             catch(Exception e)
             {
                 e.printStackTrace();
             }
 
             //return combined2d;
             return combined2d;
         }
 
         protected void onPostExecute(List<List<String>> output) {
 
             pd.dismiss();
            
             //output2d.size();
            setContentView(R.layout.main);
            Log.i("size",Integer.toString(output.size()));
            Log.i("size",Integer.toString(combined2d.size()));
             Log.i("Adding:","Header");
             //LayoutInflater inflater = LayoutInflater.from(ThreadActivity.this);
             ListView listview = (ListView) findViewById(R.id.listView3);
             //ViewGroup header = (ViewGroup)inflater.inflate(R.layout.header, listview, false);
             //View header = (View)getLayoutInflater().inflate(R.layout.header,listview);
             LayoutInflater inflater = getLayoutInflater();
             ViewGroup header = (ViewGroup)inflater.inflate(R.layout.header, listview, false);
             listview.addHeaderView(header, null, false);
 
 
             Log.i("Adding:","listview");
             //listview.addHeaderView(header);
             listview.setAdapter(new CustomListView.MyCustomAdapter2(ThreadActivity.this, R.layout.row2, output));
             //listview.setAdapter(new ArrayAdapter<String>(ThreadActivity.this, R.layout.row2 , output));
            /* mAdapter = new MyCustomAdapter();
             
                 mAdapter.addItem(output2d);
             
             listview.setAdapter(mAdapter);*/
         
         //hrefslist = output2d.get(1);
             foutput2 = output;
         }
         }
 
 	public void onClick(View v) {
 		Log.i("view clicked",Integer.toString(v.getId()));
 		Log.i("view id lpage",Integer.toString(R.id.LastPage));
 		Log.i("view id fpage",Integer.toString(R.id.FirstPage));
 		Log.i("view id npage",Integer.toString(R.id.NextPage));
 		Log.i("view id ppage",Integer.toString(R.id.PreviousPage));
 		
     	  switch(v.getId()){
 
     	  case R.id.LastPage: 
 		        String shurl = "http://www.overclockers.com/forums/"+turl+"&styleid=31"+"&page="+Integer.toString(lpage);
 		        Log.i("url", shurl);
 		        pd = ProgressDialog.show(ThreadActivity.this, "Working...", "request to server", true, false);
 		        new ParseThread().execute(shurl);
 
     	       break;
 
     	  case R.id.FirstPage: 
 				String shurl2 = "http://www.overclockers.com/forums/"+turl+"&styleid=31"+"&page=1";
 		        Log.i("url", shurl2);
 		        pd = ProgressDialog.show(ThreadActivity.this, "Working...", "request to server", true, false);
 		        new ParseThread().execute(shurl2);
     	       break;
     	  case R.id.NextPage:
 				if(cpage != lpage){
 					   String shurl3 = "http://www.overclockers.com/forums/"+turl+"&styleid=31"+"&page="+Integer.toString(cpage+1);
 			           Log.i("url", shurl3);
 			           pd = ProgressDialog.show(ThreadActivity.this, "Working...", "request to server", true, false);
 			           new ParseThread().execute(shurl3);
 			
 					}
     		  break;
     	  case R.id.PreviousPage:
 				if(cpage != 1){
 					   String shurl4 = "http://www.overclockers.com/forums/"+turl+"&styleid=31"+"&page="+Integer.toString(cpage-1);
 			           Log.i("url", shurl4);
 			           pd = ProgressDialog.show(ThreadActivity.this, "Working...", "request to server", true, false);
 			           new ParseThread().execute(shurl4);
 			
 					}
     		  break;
     	  }
 		        
 
 	
 		  
 	}
 }
 
