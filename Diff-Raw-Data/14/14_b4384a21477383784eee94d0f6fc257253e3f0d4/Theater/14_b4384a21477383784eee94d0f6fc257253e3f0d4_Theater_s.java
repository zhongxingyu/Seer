 package ua.in.leopard.androidCoocooAfisha;
 
 import java.util.List;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TabHost;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.TextView;
 
 public class Theater extends MainActivity implements OnItemClickListener,OnClickListener {
 	private final String AFISHA_TODAY_TAB = "afisha_today_tag";
 	private final String AFISHA_TOMORROW_TAB = "afisha_tomorrow_tag";
 	private View tab_change_button;
 	private TabHost tabs;
 	private SeanceAdapter adapter_today, adapter_tomorrow;
 	private TheaterDB theater_main = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.theater);
         
         initTabHost();
         initTwoButtonsBar();
         
         Bundle extras = getIntent().getExtras();
         int theater_id = 0;
         if(extras != null) {
         	theater_id = extras.getInt("theater_id", 0);
         }
         
         if (theater_id != 0){
         	DatabaseHelper DatabaseHelperObject = new DatabaseHelper(this);
         	theater_main = DatabaseHelperObject.getTheater(theater_id);
         	if (theater_main != null){
         		setTitle(Html.fromHtml(theater_main.getTitle()));
         		
         		/*
         		Button theater_call_phone = (Button)findViewById(R.id.theater_call_phone);
         		theater_call_phone.setOnClickListener(this);
         		Button theater_map_location = (Button)findViewById(R.id.theater_map_location);
         		theater_map_location.setOnClickListener(this);
         		*/
         		
         		ListView seanceTodayList = (ListView)findViewById(R.id.afisha_today_list);
         		List<CinemaDB> cinemas_today = DatabaseHelperObject.getTodayOrTomorrowByTheater(theater_main, true);
         		adapter_today = new SeanceAdapter(this, cinemas_today);
         		seanceTodayList.setAdapter(adapter_today);
         		seanceTodayList.setOnItemClickListener(this);
         		
         		ListView seanceTomorrowList = (ListView)findViewById(R.id.afisha_tomorrow_list);
         		List<CinemaDB> cinemas_tomorrow = DatabaseHelperObject.getTodayOrTomorrowByTheater(theater_main, false);
         		adapter_tomorrow = new SeanceAdapter(this, cinemas_tomorrow);
         		seanceTomorrowList.setAdapter(adapter_tomorrow);
         		seanceTomorrowList.setOnItemClickListener(this);
         	}
         }
 	}
 	
 	private void initTabHost(){
 		tabs = (TabHost)findViewById(R.id.tabhost);
         tabs.setup();
         TabHost.TabSpec spec = tabs.newTabSpec(AFISHA_TODAY_TAB);
         spec.setContent(R.id.afisha_today_list);
         spec.setIndicator(getString(R.string.afisha_today));
         tabs.addTab(spec);
         spec = tabs.newTabSpec(AFISHA_TOMORROW_TAB);
         spec.setContent(R.id.afisha_tomorrow_list);
         spec.setIndicator(getString(R.string.afisha_tomorrow));
         tabs.addTab(spec);
         tabs.setCurrentTab(0);
 	}
 	
 	private void initTwoButtonsBar(){
 		View two_buttons_bar = findViewById(R.id.two_buttons_bar);
 		if (two_buttons_bar != null){
 			tab_change_button = two_buttons_bar.findViewById(R.id.two_buttons_bar_button_one);
 			tabButtonChanged();
 			
 			View two_buttons_bar_button_second = two_buttons_bar.findViewById(R.id.two_buttons_bar_button_second);
 			if (two_buttons_bar_button_second != null){
 				two_buttons_bar_button_second.setOnClickListener(this);
 				TextView button_second_text = (TextView)two_buttons_bar_button_second.findViewById(R.id.two_buttons_button_label);
				button_second_text.setText(R.string.theater_more_info);
 				ImageView button_img = (ImageView)two_buttons_bar_button_second.findViewById(R.id.two_buttons_button_image);
 				button_img.setImageResource(R.drawable.info_button);
 			}
 		}
 	}
 	
 	private void tabButtonChanged(){
 		if (tab_change_button != null){
 			tab_change_button.setOnClickListener(this);
 			ImageView button_img = (ImageView)tab_change_button.findViewById(R.id.two_buttons_button_image);
 			TextView button_text = (TextView)tab_change_button.findViewById(R.id.two_buttons_button_label);
 			if (AFISHA_TODAY_TAB == tabs.getCurrentTabTag()){
				button_text.setText(R.string.afisha_today);
 				button_img.setImageResource(R.drawable.today_button);
 			} else {
				button_text.setText(R.string.afisha_tomorrow);
 				button_img.setImageResource(R.drawable.tomorrow_button);
 			}
 			
 		}
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 		SeanceAdapter s_adapter = (SeanceAdapter)parent.getAdapter();
 		CinemaDB cinema_obj = (CinemaDB)s_adapter.getItem(position);
 		Intent intent = new Intent(this, Cinema.class);
 		Bundle bundle = new Bundle();
 		bundle.putInt("cinema_id", cinema_obj.getId());
 		intent.putExtras(bundle);
 		startActivity(intent);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		  case R.id.two_buttons_bar_button_second:
 			Intent intent_info = new Intent(this, TheaterInfo.class);
 			Bundle bundle_info = new Bundle();
 			bundle_info.putInt("theater_id", theater_main.getId());
 			intent_info.putExtras(bundle_info);
 			startActivity(intent_info);
 	        break; 
 		  case R.id.two_buttons_bar_button_one:
 			if (AFISHA_TODAY_TAB == tabs.getCurrentTabTag()){
 				tabs.setCurrentTabByTag(AFISHA_TOMORROW_TAB);
 			} else {
 				tabs.setCurrentTabByTag(AFISHA_TODAY_TAB);
 			}
 			tabButtonChanged();
 			break;
 		  /*
 		  case R.id.theater_call_phone:
 				String toDial="tel:" + theater_main.getCallPhone();
 				startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(toDial)));
 		        break;
 		  case R.id.theater_map_location:
 				Intent intent_map = new Intent(this, MainTheatersMap.class);
 				Bundle bundle_map = new Bundle();
 				bundle_map.putInt("theater_id", theater_main.getId());
 				intent_map.putExtras(bundle_map);
 				startActivity(intent_map);
 				break;
 		  */
 	   }
 	}
 }
