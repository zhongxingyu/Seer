 package com.app.wordservant;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckedTextView;
 import android.widget.GridView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.app.wordservant.Bible.BibleBook;
 
 public class SelectScripture extends Activity {
 	private class WaitForBibleLoad extends AsyncTask<Void, Void, Void>{
 
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			// TODO Auto-generated method stub
 			while(Bible.getInstance().books.size()<66){
 				if(this.isCancelled())
 					return null;
 			}
 			return null;
 		}
 
 		protected void onPostExecute(Void bible){
 			ProgressBar pBar = (ProgressBar) findViewById(R.id.progressBar1);
 			pBar.setProgress(100);
 			pBar.setVisibility(ProgressBar.GONE);
 			TextView loadingMessage = (TextView) findViewById(R.id.loadingText);
 			loadingMessage.setVisibility(TextView.GONE);
 			ListView lView = (ListView) findViewById(R.id.listView1);
 			lView.setVisibility(ListView.VISIBLE);
 			lView.setOnItemClickListener(new OnItemClickListener(){
 
 				@Override
 				public void onItemClick(AdapterView<?> aView, View view,
 						int position, long id) {
 					// TODO Auto-generated method stub
 					TabHost tHost = (TabHost) findViewById(R.id.tabhost);
 					tHost.setCurrentTab(1);
 					final BibleBook b = Bible.getInstance().getBook(position);
 					GridView gView = (GridView) findViewById(R.id.gridView1);
 					gView.setOnItemClickListener(new OnItemClickListener(){
 
 						@Override
 						public void onItemClick(AdapterView<?> aView, View view,
 								int position, long id) {
 							// TODO Auto-generated method stub
 							TabHost tHost = (TabHost) findViewById(R.id.tabhost);
 							tHost.setCurrentTab(2);
 							final GridView gView = (GridView) findViewById(R.id.gridView2);
 							final Bible.BibleChapter chapter = b.getChapter(position);
 							final ArrayList<Integer> checkedCheckBoxes = new ArrayList<Integer>();
 							gView.setOnItemClickListener(new OnItemClickListener(){
 
 								@Override
 								public void onItemClick(AdapterView<?> aView,
 										View view, int position, long id) {
 									// Get scriptures and send them to the displayed scripture screen.
 									CheckedTextView textView = (CheckedTextView) view;
 
 									if(Build.VERSION.SDK_INT<11){
 										textView.setChecked(!textView.isChecked());
 									}
 
 									if((Build.VERSION.SDK_INT<11 && textView.isChecked()) || (Build.VERSION.SDK_INT>=11 && !textView.isChecked())){
 										checkedCheckBoxes.add(Integer.valueOf((String) textView.getText()));
 									}
 									else{
 										checkedCheckBoxes.remove(checkedCheckBoxes.indexOf((Integer) position+1));
 									}
 
 
 									Button displayScriptures = (Button) findViewById(R.id.displayScriptures);
 
 									if(checkedCheckBoxes.size()>0){
 										displayScriptures.setEnabled(true);
 									}
 									else{
 										displayScriptures.setEnabled(false);
 									}
 								}
 
 							});
 							gView.setOnItemSelectedListener(new OnItemSelectedListener(){
 
 								@Override
 								public void onItemSelected(AdapterView<?> arg0,
 										View arg1, int arg2, long arg3) {
 									// TODO Auto-generated method stub
 									Toast.makeText(getApplicationContext(), "yay", Toast.LENGTH_SHORT).show();
 								}
 
 								@Override
 								public void onNothingSelected(
 										AdapterView<?> arg0) {
 									// TODO Auto-generated method stub
 
 								}
 
 							});
 							Button displayScripture = (Button) findViewById(R.id.displayScriptures);
 							displayScripture.setOnClickListener(new OnClickListener(){
 
 								@Override
 								public void onClick(View view) {
 									// TODO Auto-generated method stub
 									Intent intent = new Intent(SelectScripture.this,DisplaySelectedScripture.class);
 									intent.putExtra("book_name", b.bookName);
 									intent.putExtra("chapter_number", Integer.valueOf(chapter.chapterNumber).intValue());
 									Bundle bundle = new Bundle();
 									Collections.sort(checkedCheckBoxes);
 									bundle.putIntegerArrayList("verses", checkedCheckBoxes);
 									intent.putExtra("bundle", bundle);
 
 									startActivityForResult(intent, 0);
 								}
 
 							});
 							Button previewChapter = (Button) findViewById(R.id.previewChapter);
 							previewChapter.setOnClickListener(new OnClickListener(){
 
 								@Override
 								public void onClick(View v) {
 									// TODO Auto-generated method stub
 									Intent intent = new Intent(SelectScripture.this,DisplaySelectedScripture.class);
 									intent.putExtra("book_name", b.bookName);
 									intent.putExtra("chapter_number", Integer.valueOf(chapter.chapterNumber).intValue());
 									Bundle bundle = new Bundle();
 									ArrayList<Integer> allCheckBoxes = new ArrayList<Integer>();
 									//Add all to the array list
 									for(int i=1;i<=chapter.getVersesArray().length;i++){
 										allCheckBoxes.add(i);
 									}
 									Collections.sort(allCheckBoxes);
 									bundle.putIntegerArrayList("verses", allCheckBoxes);
 									intent.putExtra("bundle", bundle);
 
 									startActivityForResult(intent, 0);
 								}
 								
 							});
 							ArrayAdapter<String> adapter;
 							adapter = new ArrayAdapter<String>(SelectScripture.this, android.R.layout.simple_list_item_checked, chapter.getVersesArray()){
 								public View getView(int position, View convertView, ViewGroup parent){
									CheckedTextView textView;
									if(convertView == null)
										textView = (CheckedTextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_checked, null);
									else
										textView = (CheckedTextView) convertView;
 									textView.setText(String.valueOf(position+1));
 									if(checkedCheckBoxes.contains(position+1))
 										textView.setChecked(true);
 									return textView;
 								}
 							};
 							gView.setAdapter(adapter);
 						}
 
 					});
 					ArrayAdapter<String> adapter;
 					adapter = new ArrayAdapter<String>(SelectScripture.this, android.R.layout.simple_list_item_1, Bible.getInstance().getBook(position).getChaptersArray());
 					gView.setAdapter(adapter);
 				}
 
 			});
 
 			ArrayAdapter<String> adapter;
 			adapter = new ArrayAdapter<String>(SelectScripture.this, android.R.layout.simple_list_item_1, Bible.getInstance().getBookNames());
 			lView.setAdapter(adapter);
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.layout_select_scripture);
 
 		new WaitForBibleLoad().execute();
 
 		TabHost tHost = (TabHost) findViewById(R.id.tabhost);
 		tHost.setup();
 		TabSpec tabSpec = tHost.newTabSpec("tabs");
 		tabSpec.setIndicator("Books");
 		tabSpec.setContent(R.id.tab1);
 		tHost.addTab(tabSpec);
 
 		tabSpec = tHost.newTabSpec("tabs");
 		tabSpec.setIndicator("Chapters");
 		tabSpec.setContent(R.id.tab2);
 		tHost.addTab(tabSpec);
 
 		tabSpec = tHost.newTabSpec("tabs");
 		tabSpec.setIndicator("Verses");
 		tabSpec.setContent(R.id.tab3);
 		tHost.addTab(tabSpec);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		//getMenuInflater().inflate(R.menu.tag_preview, menu);
 		return true;
 	}
 
 	protected void onActivityResult (int requestCode, int resultCode, Intent data){
 		if(resultCode==0){
 			finish();
 		}
 	}
 }
