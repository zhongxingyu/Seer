 package ua.in.leopard.androidCoocooAfisha;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TabHost;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class Cinema extends Activity implements OnClickListener, OnItemClickListener {
 	private TheaterAdapter adapter_today, adapter_tomorrow;
 	private CinemaDB cinema_main;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.cinema);
         
         TabHost tabs=(TabHost)findViewById(R.id.tabhost);
         tabs.setup();
         TabHost.TabSpec spec=tabs.newTabSpec("afisha_today_tag");
         spec.setContent(R.id.afisha_today_list);
         spec.setIndicator(getString(R.string.afisha_today), getResources().getDrawable(R.drawable.today_icon));
         tabs.addTab(spec);
         spec=tabs.newTabSpec("afisha_tomorrow_tag");
         spec.setContent(R.id.afisha_tomorrow_list);
         spec.setIndicator(getString(R.string.afisha_tomorrow), getResources().getDrawable(R.drawable.tomorrow_icon));
         tabs.addTab(spec);
         tabs.setCurrentTab(0);
         
         Bundle extras = getIntent().getExtras();
         int cinema_id = 0;
         if(extras != null) {
         	cinema_id = extras.getInt("cinema_id", 0);
         }
         
         if (cinema_id != 0){
         	DatabaseHelper DatabaseHelperObject = new DatabaseHelper(this);
         	CinemaDB cinema_main = DatabaseHelperObject.getCinema(cinema_id);
         	if (cinema_main != null){
         		this.cinema_main = cinema_main;
         		TextView cinema_title = (TextView)findViewById(R.id.cinema_title);
         		cinema_title.setText(cinema_main.getTitle());
         		ImageView cinemaPoster = (ImageView)findViewById(R.id.cinema_poster);
         		if (EditPreferences.isNoPosters(this)){
         			cinemaPoster.setImageResource(R.drawable.no_poster);
         		} else {
         			Bitmap poster = cinema_main.getPosterImg();
             		if (poster != null){
             			cinemaPoster.setImageBitmap(poster);
             		} else {
             			cinemaPoster.setImageResource(R.drawable.poster);
             		}
         		}
         		
         		TextView cinema_orig_title = (TextView)findViewById(R.id.cinema_orig_title);
         		String org_title = cinema_main.getOrigTitle();
         		if (cinema_main.getYear() != null && Integer.parseInt(cinema_main.getYear()) != 0){
        			org_title = org_title + " (" + getString(R.string.cinema_year) + ": " + 
         			cinema_main.getYear() + ")";
         		}
         		cinema_orig_title.setText(Html.fromHtml(org_title));
         		
         		View descriptionButton = findViewById(R.id.cinema_description_button);
         		descriptionButton.setOnClickListener(this);
         		
         		
         		ListView afishaTodayList = (ListView)findViewById(R.id.afisha_today_list);
                 List<TheaterDB> theaters_today = DatabaseHelperObject.getTodayOrTomorrowByCinema(cinema_main, true);
                 adapter_today = new TheaterAdapter(this, theaters_today);
                 afishaTodayList.setAdapter(adapter_today);
                 afishaTodayList.setOnItemClickListener(this);
                 
                 ListView afishaTomorrowList = (ListView)findViewById(R.id.afisha_tomorrow_list);
                 List<TheaterDB> theaters_tomorrow = DatabaseHelperObject.getTodayOrTomorrowByCinema(cinema_main, false);
                 adapter_tomorrow = new TheaterAdapter(this, theaters_tomorrow);
                 afishaTomorrowList.setAdapter(adapter_tomorrow);
                 afishaTomorrowList.setOnItemClickListener(this);
         	}
         }
 
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 		TheaterAdapter s_adapter = (TheaterAdapter)parent.getAdapter();
 		TheaterDB theater_obj = (TheaterDB)s_adapter.getItem(position);
 		Intent intent = new Intent(this, Theater.class);
 		Bundle bundle = new Bundle();
 		bundle.putInt("theater_id", theater_obj.getId());
 		intent.putExtras(bundle);
 		startActivity(intent);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		  case R.id.cinema_description_button:
 			Intent intent = new Intent(this, CinemaDescription.class);
 			Bundle bundle = new Bundle();
 			bundle.putString("cinema_title", this.cinema_main.getTitle());
 			bundle.putString("cinema_content", this.cinema_main.getDescription());
 			intent.putExtras(bundle);
 			startActivity(intent);
 	        break;        
 	    }		
 	}
 
 }
