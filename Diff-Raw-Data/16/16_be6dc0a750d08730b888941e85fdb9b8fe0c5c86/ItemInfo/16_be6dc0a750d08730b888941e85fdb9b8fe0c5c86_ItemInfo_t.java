 package com.youpony.amuse;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ItemInfo extends Activity {
 
 	TextView t1,t2,t3,t4,titolo1,titolo2,titolo3,titolo4;	TextView title;
 	ImageView v;
 	int pos;
 	Item item;
 	ImageDownloader downloader;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_qr_result);
 		Bundle b = getIntent().getExtras();
 		pos = b.getInt("pos");
 		item = new Item();
 		titolo1= new TextView(this);
 		titolo2= new TextView(this);
 		titolo3= new TextView(this);
 		titolo4= new TextView(this);
 		
 		t1 = new TextView(this);
 		t2 = new TextView(this);
 		t3 = new TextView(this);
 		t4 = new TextView(this);
 		v = new ImageView(this);
 		try{
 			item = PageViewer.values.get(pos);
 		}
 		catch (Exception e){
 			Log.i("orrudebug", "can't find object with position " + pos);
 		}
 		titolo1 = (TextView) findViewById(R.id.autoreTitle);
 		t1 = (TextView) findViewById(R.id.autoreText);
 		titolo2 = (TextView) findViewById(R.id.annoTitle);
 		t2 = (TextView) findViewById(R.id.annoText);
 		titolo3 = (TextView) findViewById(R.id.descTitle);
 		t3 = (TextView) findViewById(R.id.descText);
 		titolo4 = (TextView) findViewById(R.id.mostraTitle);
 		t4 = (TextView) findViewById(R.id.mostraText);
 		v = (ImageView) findViewById(R.id.imageView);
 		title = (TextView) findViewById(R.id.title);
 		
 		//display object infos
 		
 		if(item.author!="ignoto") {
 			t1.setText(item.author);
 		} else {
 			titolo1.setVisibility(2);
 		}
 		
 		if(item.year!="0") {
 			t2.setText(item.year);
 		} else{
 			titolo2.setVisibility(2);
 		}
 		
 		t3.setText(item.description);
 		t4.setText(item.mostra);
 			
 		//add scroll listener on Text
 		t3.setMovementMethod(new ScrollingMovementMethod());
 		
 		
 		
 		title = (TextView) findViewById(R.id.title);
 		//set item title
 		title.setText(item.name);
 		
 		downloader = new ImageDownloader();
 		if( item.url != null){
 			item.url = downloader.download(item.url, v);
 		}
 		
 		//manage Delete button action
 		Button delete = (Button) findViewById(R.id.confirm);
 		delete.setText("Delete");
 		delete.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(View v) {
 				
 				PageViewer.values.remove(pos);
 				PageViewer.pinterestItems.remove(pos);
 				Story.pinterestAdapter.notifyDataSetChanged();
 				if(PageViewer.values.size() == 0){
 					Log.i("orrudebug", "cancellata la lista");
					Story.start = true;
 				}
 				close();
 			}
 		});
 		
 		//manage Cancel button action
 		Button cancel = (Button) findViewById(R.id.cancel);
 		cancel.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				close();
 				
 			}
 		});
 			
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.item_info, menu);
 		return true;
 	}
 	
 	void close(){
 		ItemInfo.this.finish();
 		PageViewer.mViewPager.setCurrentItem(1);
 	}
 	
 
 }
