 package jp.gr.java_conf.neko_daisuki.android.nexec.client;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.JsonWriter;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View.OnClickListener;
 import android.view.View;
 import android.widget.Adapter;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
     private class PrivatePagerAdapter extends PagerAdapter {
 
         private abstract class PageCreator {
 
             public View create(ViewPager pager, int position) {
                 View view = mInflater.inflate(getResourceId(), pager, false);
                 initializeView(view);
                pager.addView(view, position);
                 return view;
             }
 
             protected abstract int getResourceId();
             protected abstract void initializeView(View view);
 
             protected TextView getTextView(View view, int id) {
                 return (TextView)view.findViewById(id);
             }
 
             protected AdapterView getAdapterView(View view, int id) {
                 return (AdapterView)view.findViewById(id);
             }
         }
 
         private class HostPageCreator extends PageCreator {
 
             protected int getResourceId() {
                 return R.layout.page_host;
             }
 
             protected void initializeView(View view) {
                 setStringText(view, R.id.host, mHost);
                 setIntText(view, R.id.port, mPort);
             }
 
             private void setIntText(View view, int id, int n) {
                 getTextView(view, id).setText(Integer.toString(n));
             }
 
             private void setStringText(View view, int id, String s) {
                 getTextView(view, id).setText(s);
             }
         }
 
         private class CommandPageCreator extends PageCreator {
 
             protected int getResourceId() {
                 return R.layout.page_command;
             }
 
             protected void initializeView(View view) {
                 setStringArrayText(view, R.id.args, mArgs);
             }
 
             private void setStringArrayText(View view, int id, String[] sa) {
                 StringBuffer buffer = new StringBuffer(sa[0]);
                 for (int i = 1; i < sa.length; i++) {
                     buffer.append(String.format(" %s", sa[i]));
                 }
                 getTextView(view, id).setText(buffer.toString());
             }
         }
 
         private class LinkPageCreator extends PageCreator {
 
             protected int getResourceId() {
                 return R.layout.page_link;
             }
 
             protected void initializeView(View view) {
                 AdapterView listView = getAdapterView(view, R.id.link_list);
 
                 int id = android.R.layout.simple_list_item_1;
 
                 List<String> list = new ArrayList<String>();
                 for (Link link: mLinks) {
                     list.add(String.format("%s -> %s", link.src, link.dest));
                 }
 
                 Adapter adapter = new ArrayAdapter(MainActivity.this, id, list);
                 listView.setAdapter(adapter);
             }
         }
 
         private class PermissionPageCreator extends PageCreator {
 
             protected int getResourceId() {
                 return R.layout.page_permission;
             }
 
             protected void initializeView(View view) {
                 setPermissionList(view, mFiles);
             }
 
             private void setPermissionList(View view, String[] files) {
                 int listId = R.id.permission_list;
                 AdapterView listView = getAdapterView(view, listId);
 
                 int id = android.R.layout.simple_list_item_1;
 
                 List<String> list = new ArrayList<String>();
                 for (String file: files) {
                     list.add(file);
                 }
 
                 Adapter adapter = new ArrayAdapter(MainActivity.this, id, list);
                 listView.setAdapter(adapter);
             }
         }
 
         private class Page {
 
             private PageCreator mCreator;
             private String mTitle;
 
             public Page(PageCreator creator, String title) {
                 mCreator = creator;
                 mTitle = title;
             }
 
             public PageCreator getCreator() {
                 return mCreator;
             }
 
             public String getTitle() {
                 return mTitle;
             }
         }
 
         private String mHost;
         private int mPort;
         private String[] mArgs;
         private String[] mFiles;
         private Link[] mLinks;
 
         private LayoutInflater mInflater;
         private Page[] mPages;
 
         public PrivatePagerAdapter(String host, int port, String[] args, String[] files, Link[] links) {
             mHost = host;
             mPort = port;
             mArgs = args;
             mFiles = files;
             mLinks = links;
 
             String key = Context.LAYOUT_INFLATER_SERVICE;
             mInflater = (LayoutInflater)getSystemService(key);
             mPages = new Page[] {
                 new Page(new HostPageCreator(), "Host"),
                 new Page(new CommandPageCreator(), "Command"),
                 new Page(new PermissionPageCreator(), "Permission"),
                 new Page(new LinkPageCreator(), "Redirection") };
         }
 
         @Override
         public void destroyItem(View collection, int position, Object view) {
             ViewPager pager = (ViewPager)collection;
             View v = (View)view;
             pager.removeView(v);
         }
 
         @Override
         public void finishUpdate(View collection) {
         }
 
         @Override
         public int getCount() {
             return mPages.length;
         }
 
         @Override
         public Object instantiateItem(View collection, int position) {
             ViewPager pager = (ViewPager)collection;
             return mPages[position].getCreator().create(pager, position);
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             return mPages[position].getTitle();
         }
 
         @Override
         public boolean isViewFromObject(View view, Object object) {
             return view == (View)object;
         }
 
         @Override
         public void restoreState(Parcelable parcel, ClassLoader classLoader) {
         }
 
         @Override
         public Parcelable saveState() {
             return null;
         }
 
         @Override
         public void startUpdate(View collection) {
         }
     }
 
     private class OkButtonOnClickListener implements OnClickListener {
 
         private String mHost;
         private int mPort;
         private String[] mArgs;
         private String[] mFiles;
         private Link[] mLinks;
 
         public OkButtonOnClickListener(String host, int port, String[] args, String[] files, Link[] links) {
             mHost = host;
             mPort = port;
             mArgs = args;
             mFiles = files;
             mLinks = links;
         }
 
         public void onClick(View view) {
             String sessionId;
             try {
                 sessionId = createSessionId();
             }
             catch (NoSuchAlgorithmException e) {
                 showException("algorithm not found", e);
                 return;
             }
             try {
                 saveSession(sessionId);
             }
             catch (IOException e) {
                 showException("failed to save session", e);
                 return;
             }
 
             Intent intent = getIntent();
             intent.putExtra("SESSION_ID", sessionId);
             setResult(RESULT_OK, intent);
             finish();
         }
 
         private void showException(String message, Throwable e) {
             showToast(String.format("%s: %s", message, e.getMessage()));
             e.printStackTrace();
         }
 
         private String createSessionId() throws NoSuchAlgorithmException {
             MessageDigest md = MessageDigest.getInstance("SHA-256");
             md.update(mHost.getBytes());
             for (int width: new int[] { 0, 8, 16, 24 }) {
                 md.update((byte)((mPort >>> width) & 0xff));
             }
             for (String a: mArgs) {
                 md.update(a.getBytes());
             }
             long now = System.currentTimeMillis();
             for (int width: new int[] { 0, 8, 16, 24, 32, 40, 48, 56 }) {
                 md.update((byte)((now >>> width) & 0xff));
             }
             byte[] bytes = new byte[32];
             mRandom.nextBytes(bytes);
             md.update(bytes);
             return md.toString();
         }
 
         private void writeLinkArray(JsonWriter writer, String name, Link[] links) throws IOException {
             writer.name(name);
             writer.beginArray();
             for (Link link: links) {
                 writer.beginObject();
                 writer.name("dest").value(link.dest);
                 writer.name("src").value(link.src);
                 writer.endObject();
             }
             writer.endArray();
         }
 
         private void writeStringArray(JsonWriter writer, String name, String[] sa) throws IOException {
             writer.name(name);
             writer.beginArray();
             for (String a: sa) {
                 writer.value(a);
             }
             writer.endArray();
         }
 
         private void saveSession(String sessionId) throws IOException {
             OutputStream out = openFileOutput(sessionId, 0);
             JsonWriter writer = new JsonWriter(
                     new BufferedWriter(new OutputStreamWriter(out, "UTF-8")));
             try {
                 writer.beginObject();
                 writer.name("host").value(mHost);
                 writer.name("port").value(mPort);
                 writeStringArray(writer, "args", mArgs);
                 writeStringArray(writer, "files", mFiles);
                 writeLinkArray(writer, "links", mLinks);
                 writer.endObject();
             }
             finally {
                 writer.close();
             }
         }
     }
 
     private class CancelButtonOnClickListener implements OnClickListener {
 
         public void onClick(View view) {
             setResult(RESULT_CANCELED, getIntent());
             finish();
         }
     }
 
     private static class Link {
 
         public String dest;
         public String src;
 
         public Link(String dest, String src) {
             this.dest = dest;
             this.src = src;
         }
     }
 
     private Random mRandom = new Random();
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         Intent intent = getIntent();
         String host = intent.getStringExtra("HOST");
         int port = intent.getIntExtra("PORT", 57005);
         String[] args = intent.getStringArrayExtra("ARGS");
         String[] files = intent.getStringArrayExtra("FILES");
         Link[] links = parseLinks(intent.getStringArrayExtra("LINKS"));
 
         ViewPager pager = (ViewPager)findViewById(R.id.view_pager);
         pager.setAdapter(
                 new PrivatePagerAdapter(host, port, args, files, links));
 
         Button okButton = (Button)findViewById(R.id.ok_button);
         okButton.setOnClickListener(
                 new OkButtonOnClickListener(host, port, args, files, links));
         Button cancelButton = (Button)findViewById(R.id.cancel_button);
         cancelButton.setOnClickListener(new CancelButtonOnClickListener());
     }
 
     private Link parseLink(String s) {
         StringBuilder dest = new StringBuilder();
         int i;
         for (i = 0; s.charAt(i) != ':'; i++) {
             i += s.charAt(i) == '\\' ? 1 : 0;
             dest.append(s.charAt(i));
         }
 
         StringBuilder src = new StringBuilder();
         int len = s.length();
         for (i = i + 1; i < len; i++) {
             i += s.charAt(i) == '\\' ? 1 : 0;
             src.append(s.charAt(i));
         }
 
         return new Link(dest.toString(), src.toString());
     }
 
     private Link[] parseLinks(String[] links) {
         List<Link> l = new LinkedList<Link>();
         for (String link: links) {
             l.add(parseLink(link));
         }
         return l.toArray(new Link[0]);
     }
 
     private void showToast(String message) {
         Toast.makeText(this, message, Toast.LENGTH_LONG).show();
     }
 }
 
 // vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
