 package ru.RssReader;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.Html;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathExpressionException;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class RssReaderActivity extends ListActivity
 {
     public class RssAdapter extends BaseAdapter {
         private ArrayList <Entry> entryList = new ArrayList<Entry> ();
         private Context context;
 
         public RssAdapter (ArrayList <Entry> el, Context c) {
             entryList = el;
             context = c;
         }
 
         public int getCount () {
             return entryList.size ();
         }
 
         public Object getItem (int i) {
             return entryList.get (i);
         }
 
         public long getItemId (int i) {
             return i;
         }
 
         private String fromHtml (String html) {
             return Html.fromHtml (html).toString ();
         }
 
         public View getView (int i, View view, ViewGroup viewGroup) {
            if (null == view) {
                View v = LayoutInflater.from (context).inflate (R.layout.entry, null);
                TextView title = (TextView) v.findViewById (R.id.title);
                TextView description = (TextView) v.findViewById (R.id.description);
                TextView pubDate = (TextView) v.findViewById (R.id.pubDate);
 
                Entry entry = (Entry) getItem (i);
 
                title.setText (fromHtml (entry.getTitle ()));
                description.setText (fromHtml (entry.getDescription ()));
                pubDate.setText (fromHtml (entry.getPubDate ()));
 
                return v;
            }
            else return view;
         }
     }
 
 
 
     @Override
     protected void onListItemClick (ListView l, View v, int position, long id) {
         String link = ((Entry) getListAdapter ().getItem (position)).getLink ();
 
         Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse (link));
         startActivity (browserIntent);
     }
 
 
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate (Bundle savedInstanceState)
     {
         String rssFeed = "http://www.opennet.ru/opennews/opennews_all.rss";
 
         super.onCreate (savedInstanceState);
         setContentView (R.layout.main);
 
         ProgressDialog progDialog = new ProgressDialog (this);
         progDialog.setMessage ("Please wait for few seconds...");
         progDialog.setTitle ("Yet another rss reader");
 
         Loader loader = new Loader ();
         try {
             progDialog.show ();
             ArrayList <Entry> entryList = loader.load (rssFeed);
 
             setListAdapter (new RssAdapter (entryList, this));
             progDialog.dismiss ();
        } catch (Exception e) {
             e.printStackTrace ();
 
             Log.e ("RssReaderActivity", e.getMessage ());
             throw new RuntimeException (e.getMessage (), e);
         }
     }
 }
