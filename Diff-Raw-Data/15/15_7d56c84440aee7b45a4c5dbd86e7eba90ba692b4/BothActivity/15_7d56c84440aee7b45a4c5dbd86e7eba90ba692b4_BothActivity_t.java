 package com.ocforums.application;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.htmlcleaner.TagNode;
 
 import com.ocforums.application.CustomListView.MyCustomAdapter;
 
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 
 
 public class BothActivity extends Activity{
 	
 
 	
 	
 
 
     private static List<String> foutput2 = new ArrayList<String>();
     List<String> houtput = new ArrayList<String>();	
 	List<List<String>> newold = new ArrayList<List<String>>();
     List<String> Tid = new ArrayList<String>();
 	List<String> newlist = new ArrayList<String>();
     
     CustomListView lv = new CustomListView();
     String hurl;
     List<String> hhrefslist;
 	private ProgressDialog pd;
 	//OCForumsAppActivity getter = new OCForumsAppActivity();
 
 	@Override
 	protected void onStop() {
 	    super.onStop();
 	    findViewById(R.id.listView1).setVisibility(View.GONE);
 	}
 
 	@Override
 	protected void onRestart() {
 	    super.onRestart();
 	    Log.i("size of foupout2",Integer.toString(foutput2.size()));
 	    findViewById(R.id.listView1).setVisibility(View.VISIBLE);
 	    ListView listview = (ListView) findViewById(R.id.listView1);
 	    MyCustomAdapter adapter = new CustomListView.MyCustomAdapter(BothActivity.this, R.layout.row , foutput2);
 	    listview.setAdapter(adapter);
 	}
	
	@Override
	protected void onResume() {
	    super.onRestart();
	    Log.i("size of foupout2",Integer.toString(foutput2.size()));
	    findViewById(R.id.listView1).setVisibility(View.VISIBLE);
	    ListView listview = (ListView) findViewById(R.id.listView1);
	    MyCustomAdapter adapter = new CustomListView.MyCustomAdapter(BothActivity.this, R.layout.row , foutput2);
	    listview.setAdapter(adapter);
	}
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //setContentView(R.layout.main);
         
         String qhurl = CustomListView.gethurl();
         String shurl = "http://www.overclockers.com/forums/"+qhurl+"&styleid=23";
         //Toast.makeText(getApplicationContext(), hurl, Toast.LENGTH_LONG).show();
         pd = ProgressDialog.show(BothActivity.this, "Working...", "request to server", true, false);
         new ParseBoth().execute(shurl);
         Log.i("where ami?","back from execution");
         setContentView(R.layout.main);
         ListView list = (ListView) findViewById(R.id.listView1);
         //list.addFooterView(list);
 
         list.setOnItemClickListener(new OnItemClickListener(){
     	    public void onItemClick(AdapterView<?> parent, View view,
     	            int position, long id) {
     		 //Toast.makeText(getApplicationContext(), String.valueOf(parent), Toast.LENGTH_SHORT).show();  
     		 //Toast.makeText(getApplicationContext(), String.valueOf(view), Toast.LENGTH_SHORT).show();
     		 //Toast.makeText(getApplicationContext(), String.valueOf(id), Toast.LENGTH_SHORT).show();
     		 hhrefslist = CustomListView.gethlist();
     		 hurl = hhrefslist.get((int) id).replace("amp;","");
     	    //Toast.makeText(getApplicationContext(), hurl, Toast.LENGTH_LONG).show();
     	     Log.i("testing",hurl);	
     		 //pd = ProgressDialog.show(OCForumsAppActivity.this, "Working...", "request to server", true, false);
     	     if(hurl.matches("(?i).*forumdisplay.*")){
     	    	 //Toast.makeText(BothActivity.this, hurl, Toast.LENGTH_LONG).show();
     	    	 CustomListView.sethurl(hurl);
     	    	 Intent newActivity = new Intent(getBaseContext(), BothActivity.class);     
                  startActivity(newActivity);
 
     	    	// BothActivity.bothactivity.execute(hurl);
     	     }
     	     else if(hurl.matches("(?i).*showthread.php.*")) {
     	    	 CustomListView.sethurl(hurl);
     	    	 Intent newActivity = new Intent(getBaseContext(), ThreadActivity.class);     
                  startActivity(newActivity);
 
     	     }
     	     else if(hhrefslist == null){
     	    	 Toast.makeText(getApplicationContext(), "Confused?", Toast.LENGTH_LONG).show();
     	    	 //TODO add context menu for quote and reply
     	     }
     	     else{
     	    	 Toast.makeText(getApplicationContext(), "Very Confused", Toast.LENGTH_LONG).show();
     	     }
     		}
 
 			
         });
         
        /* list.setClickable(true);
         list.setOnItemClickListener(new OnItemClickListener(){
     	    public void onItemClick(AdapterView<?> parent, View view,
     	            int position, long id) {
   	    	hurl = hrefslist.get((int) id).replace("amp;","");
     	    Toast.makeText(getApplicationContext(), hurl, Toast.LENGTH_LONG).show();
     	     Log.i("testing",hurl);	
     		 
     	     if(hurl.matches("(?i).*forumdisplay.*")){
     	    	 Toast.makeText(getApplicationContext(), hurl, Toast.LENGTH_LONG).show();
     	    	 //Intent newActivity = new Intent(getBaseContext(), BothActivity.class);     
                  //startActivity(newActivity);
     	    	 OCForumsAppActivity.sethurl(hurl);
     	    	 BothActivity ba = new BothActivity();
     	    	 ba.onCreate(null);
 
     	    	// BothActivity.bothactivity.execute(hurl);
     	     }
     	     else if(hurl.matches("(?i).*showthread.php.*")) {
     	    	 Intent newActivity = new Intent(getBaseContext(), ThreadActivity.class);     
                  startActivity(newActivity);
     	     }
     	     else if(hrefslist == null){
     	    	 Toast.makeText(getApplicationContext(), "Confused?", Toast.LENGTH_LONG).show();
     	    	 //TODO add context menu for quote and reply
     	     }
     	     else{
     	    	 Toast.makeText(getApplicationContext(), "Very Confused", Toast.LENGTH_LONG).show();
     	     }
     		}
         });*/
     }
 		
     class ParseBoth extends AsyncTask<String, Void, List<List<String>>> {
 
         protected List<List<String>> doInBackground(String... arg) {
         	List<List<String>> combined2d = new ArrayList<List<String>>();
             List<String> output = new ArrayList<String>();
             List<String> hrefs = new ArrayList<String>();
             List<String> Toutput = new ArrayList<String>();
             List<String> Threfs = new ArrayList<String>();
            // List<String> ido = new ArrayList<String>();
            // List<String> idn = new ArrayList<String>();
 
             try
             {
                 newold = null;
                 Tid= null;
                 HtmlHelper hh = new HtmlHelper(new String(arg[0]));
                 HtmlHelper hh2 = new HtmlHelper(new String(arg[0]));
                // HtmlHelper hh3 = new HtmlHelper(new String(arg[0]));
                 List<TagNode> links = hh.getLinksByClass("forumtitle");
                 List<TagNode> Tlinks = hh2.getLinksByid("thread_title_");
                // List<TagNode> links2 = hh3.getForumStatus("");
 
                 for (Iterator<TagNode> iterator = links.iterator(); iterator.hasNext();)
                 {
                     TagNode divElement = (TagNode) iterator.next();
                     output.add(divElement.getText().toString());
                     hrefs.add(divElement.getAttributeByName("href").toString());
                 }
 
                 for (Iterator<TagNode> iterator = Tlinks.iterator(); iterator.hasNext();)
                 {
                     TagNode divElement = (TagNode) iterator.next();
                     Toutput.add(divElement.getText().toString());
                     Threfs.add(divElement.getAttributeByName("href").toString());
                     //Tid.add(divElement.getAttributeByName("id").toString().replaceAll("\\D",""));
                 }
                /*for (Iterator<TagNode> iterator = links2.iterator(); iterator.hasNext();)
                 {
                     TagNode divElement = (TagNode) iterator.next();
                     if (divElement.getAttributeByName("src").toString().matches(".*new.*")){
                         ido.add("new");
                         idn.add(divElement.getAttributeByName("id").toString().replaceAll("\\D",""));                   	
                     }
                     else {
                     	ido.add("old");
                         idn.add(divElement.getAttributeByName("id").toString().replaceAll("\\D",""));
                     }
                 }
                 newold.add(ido);
                 newold.add(idn);*/
                 
                 Log.i("size",Integer.toString(output.size()));
                 Log.i("size",Integer.toString(hrefs.size()));
                 Log.i("size",Integer.toString(Toutput.size()));
                 Log.i("size",Integer.toString(Threfs.size()));
                 //Toast.makeText(getApplicationContext(), output.size(), Toast.LENGTH_LONG).show();
                 //Toast.makeText(getApplicationContext(), hrefs.size(), Toast.LENGTH_LONG).show();
                 //Toast.makeText(getApplicationContext(), Toutput.size(), Toast.LENGTH_LONG).show();
                 //Toast.makeText(getApplicationContext(), Threfs.size(), Toast.LENGTH_LONG).show();
                 combined2d.add(output);
                 combined2d.add(hrefs);
                 combined2d.add(Toutput);
                 combined2d.add(Threfs);
 
             }
             catch(Exception e)
             {
                 e.printStackTrace();
             }
 
             return combined2d;
         }
 
         protected void onPostExecute(List<List<String>> output2d) {
             List<String> foutput = new ArrayList<String>();
             //Log.i("null?",String.format("%d",newold.get(1).size()));
     
             pd.dismiss();
             output2d.size();
             //Log.i("size",Integer.toString(output2d.get(0).size()));
             //Log.i("size",Integer.toString(output2d.get(1).size()));
             //Log.i("size",Integer.toString(output2d.get(2).size()));
             //Log.i("size",Integer.toString(output2d.get(3).size()));
             
             foutput.addAll(output2d.get(0));
             foutput.addAll(output2d.get(2));
             
             houtput.addAll(output2d.get(1));
             houtput.addAll(output2d.get(3));
             
             Log.i("size",Integer.toString(foutput.size()));
             //String[] testing = {"test1","test2"};
             
             //setContentView(R.layout.main);
             //ListView list;
             //list = (ListView)findViewById(R.id.listView1);
           
             //ListView listview = (ListView) findViewById(R.id.listView1);
             
            // ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, output);
             //listview.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1 , testing));
             //listview.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.row,R.id.forumlist , testing));
            //lv.makelistview(foutput, BothActivity.this);
             	 
               if(foutput2.size() == 0){
            	   foutput2 = foutput;
               }
                
                 Log.i("size of foutput",Integer.toString(foutput.size()));
                 Log.i("size of foutput2",Integer.toString(foutput2.size()));
            Log.i("where ami?","going");
            ListView listview = (ListView) findViewById(R.id.listView1);
            listview.setAdapter(new CustomListView.MyCustomAdapter(BothActivity.this, R.layout.row, foutput));
            Log.i("where ami?","back-1");
            //CustomListView.MyCustomAdapter adapter = lv.new MyCustomAdapter(BothActivity.this, R.layout.row, foutput);
             //setListAdapter(new CustomListView.MyCustomAdapter(BothActivity.this, R.layout.row2, foutput));
             // listview.setAdapter(adapter);
            // setListAdapter(ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1 , output));
                     
             /* mAdapter = new MyCustomAdapter();
             
                 mAdapter.addItem(output2d);
             
             listview.setAdapter(mAdapter);*/
         
              CustomListView.sethlist(houtput);
         }
         }
     
 }
