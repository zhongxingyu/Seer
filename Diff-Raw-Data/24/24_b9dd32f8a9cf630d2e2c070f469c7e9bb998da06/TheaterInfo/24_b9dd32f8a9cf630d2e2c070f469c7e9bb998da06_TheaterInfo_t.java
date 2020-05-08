 package ua.in.leopard.androidCoocooAfisha;
 
import android.app.AlertDialog;
import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 
 public class TheaterInfo extends MainActivity implements OnClickListener {
 	
 	private TheaterDB theater_main = null;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.theater_info);
         
         Bundle extras = getIntent().getExtras();
         int theater_id = 0;
         if(extras != null) {
         	theater_id = extras.getInt("theater_id", 0);
         }
         
         if (theater_id != 0){
         	DatabaseHelper DatabaseHelperObject = new DatabaseHelper(this);
         	theater_main = DatabaseHelperObject.getTheater(theater_id);
         	if (theater_main != null){
         		setTitle(theater_main.getTitle());
         		initTwoButtonsBar();
         		
         		TextView theater_address = (TextView)findViewById(R.id.theater_address);
         		theater_address.setText(Html.fromHtml(theater_main.getAddress()));
         		TextView theater_phone = (TextView)findViewById(R.id.theater_phone);
         		theater_phone.setText(Html.fromHtml(theater_main.getPhone()));
         		TextView theater_link = (TextView)findViewById(R.id.theater_link);
         		theater_link.setText(theater_main.getLink());
         	}
         }
 	}
 	
 	private void initTwoButtonsBar(){
 		View two_buttons_bar = findViewById(R.id.two_buttons_bar);
 		if (two_buttons_bar != null){
 			View two_buttons_bar_button_one = two_buttons_bar.findViewById(R.id.two_buttons_bar_button_one);
 			if (two_buttons_bar_button_one != null){
 				two_buttons_bar_button_one.setOnClickListener(this);
 				TextView button_one_text = (TextView)two_buttons_bar_button_one.findViewById(R.id.two_buttons_button_label);
 				if (button_one_text != null){
 					button_one_text.setText(R.string.theater_call_phone);
 				}
 				ImageView button_img = (ImageView)two_buttons_bar_button_one.findViewById(R.id.two_buttons_button_image);
 				button_img.setImageResource(R.drawable.booking_button);
 			}
 			View two_buttons_bar_button_second = two_buttons_bar.findViewById(R.id.two_buttons_bar_button_second);
 			if (two_buttons_bar_button_second != null){
 				two_buttons_bar_button_second.setOnClickListener(this);
 				TextView button_second_text = (TextView)two_buttons_bar_button_second.findViewById(R.id.two_buttons_button_label);
 				if (button_second_text != null){
 					button_second_text.setText(R.string.theater_map_location);
 				}
 				ImageView button_img = (ImageView)two_buttons_bar_button_second.findViewById(R.id.two_buttons_button_image);
 				button_img.setImageResource(R.drawable.map_button);
 			}
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 			case R.id.two_buttons_bar_button_one:
 				String toDial="tel:" + theater_main.getCallPhone();
 				startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(toDial)));
 		        break;
 			case R.id.two_buttons_bar_button_second:
				if (isOnline()){
					Intent intent_map = new Intent(this, MainTheatersMap.class);
					Bundle bundle_map = new Bundle();
					bundle_map.putInt("theater_id", theater_main.getId());
					intent_map.putExtras(bundle_map);
					startActivity(intent_map);
				} else {
					new AlertDialog.Builder(this)
					.setTitle(getString(R.string.offline_error_title))
					.setMessage(getString(R.string.offline_error_message))
					.setNeutralButton(getString(R.string.offline_error_button), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dlg, int sumthin) {
							// do nothing – it will close on its own
						}
					}).show();
				}
 				break;
 		}
 	}
 	
 }
