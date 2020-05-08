 package com.geekytheory.miguelcatalandev.developerdays;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 
 public class Main_Fragment_Timetable extends Fragment implements
 		OnClickListener {
 
 	/***
 	 * Horrible way of managing data throw an Android App, but in this case we
 	 * are only working with static data, so implementing a dynamic way
 	 * (DataBase/Adapter/Objects) is a waste of time and effort.
 	 */
 
 	Button button_1, button_2, button_3, button_4, button_5, button_6,
 			button_7, button_8, button_9, button_10, button_11, button_12,
			button_13, button_14, button_15,button_16;
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		initialize();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.main_fragment_timetable,
 				container, false);
 		return view;
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 	}
 
 	private void initialize() {
 		button_1 = (Button) getView().findViewById(R.id.timetable_button_sl);
 		button_1.setOnClickListener(this);
 
 		button_2 = (Button) getView().findViewById(R.id.timetable_button_linux);
 		button_2.setOnClickListener(this);
 
 		button_3 = (Button) getView().findViewById(R.id.timetable_button_wp);
 		button_3.setOnClickListener(this);
 
 		button_4 = (Button) getView().findViewById(R.id.timetable_button_otp);
 		button_4.setOnClickListener(this);
 
 		button_5 = (Button) getView()
 				.findViewById(R.id.timetable_button_opencv);
 		button_5.setOnClickListener(this);
 
 		button_6 = (Button) getView().findViewById(R.id.timetable_button_wp2);
 		button_6.setOnClickListener(this);
 
 		button_7 = (Button) getView().findViewById(
 				R.id.timetable_button_arduino);
 		button_7.setOnClickListener(this);
 
 		button_8 = (Button) getView().findViewById(R.id.timetable_button_node);
 		button_8.setOnClickListener(this);
 
 		button_9 = (Button) getView().findViewById(R.id.timetable_button_money);
 		button_9.setOnClickListener(this);
 
 		button_10 = (Button) getView().findViewById(R.id.timetable_button_ieee);
 		button_10.setOnClickListener(this);
 
 		button_11 = (Button) getView().findViewById(
 				R.id.timetable_button_intelec);
 		button_11.setOnClickListener(this);
 
 		button_12 = (Button) getView().findViewById(R.id.timetable_button_chat);
 		button_12.setOnClickListener(this);
 
 		button_13 = (Button) getView().findViewById(
 				R.id.timetable_button_microsoft);
 		button_13.setOnClickListener(this);
 
 		button_14 = (Button) getView()
 				.findViewById(R.id.timetable_button_aisoy);
 		button_14.setOnClickListener(this);
 
 		button_15 = (Button) getView().findViewById(
 				R.id.timetable_button_yuliop);
 		button_15.setOnClickListener(this);
		
		button_16 = (Button) getView().findViewById(
				R.id.timetable_button_arduino2);
		button_16.setOnClickListener(this);
 	}
 
 	@Override
 	public void onClick(View v) {
 		Intent intent = new Intent(getActivity(), Detail_activity_Main.class);
 		switch (v.getId()) {
 		case R.id.timetable_button_arduino:
 			intent.putExtra("name", getString(R.string.event_arduino_name));
 			intent.putExtra("image", R.drawable.mario_perez_geekytheory);
 			intent.putExtra("buis", getString(R.string.event_arduino_buis));
 			intent.putExtra("des", getString(R.string.event_arduino_des));
 			intent.putExtra("loc", getString(R.string.event_arduino_loc));
 			intent.putExtra("time", getString(R.string.event_arduino_time));
 			intent.putExtra("title", getString(R.string.event_arduino_title));
 			intent.putExtra("url", getString(R.string.event_arduino_url));
 			break;
		case R.id.timetable_button_arduino2:
			intent.putExtra("name", getString(R.string.event_arduino_name));
			intent.putExtra("image", R.drawable.mario_perez_geekytheory);
			intent.putExtra("buis", getString(R.string.event_arduino_buis));
			intent.putExtra("des", getString(R.string.event_arduino_des));
			intent.putExtra("loc", getString(R.string.event_arduino_loc2));
			intent.putExtra("time", getString(R.string.event_arduino_time));
			intent.putExtra("title", getString(R.string.event_arduino_title));
			intent.putExtra("url", getString(R.string.event_arduino_url));
			break;
 		case R.id.timetable_button_chat:
 			intent.putExtra("name", getString(R.string.event_chat_name));
 			intent.putExtra("image", R.drawable.miguel_herrera_yuilop);
 			intent.putExtra("buis", getString(R.string.event_chat_buis));
 			intent.putExtra("des", getString(R.string.event_chat_des));
 			intent.putExtra("loc", getString(R.string.event_chat_loc));
 			intent.putExtra("time", getString(R.string.event_chat_time));
 			intent.putExtra("title", getString(R.string.event_chat_title));
 			intent.putExtra("url", getString(R.string.event_chat_url));
 			break;
 		case R.id.timetable_button_ieee:
 			intent.putExtra("name", getString(R.string.event_ieee_name));
 			intent.putExtra("buis", getString(R.string.event_ieee_buis));
 			intent.putExtra("des", getString(R.string.event_ieee_des));
 			intent.putExtra("loc", getString(R.string.event_ieee_loc));
 			intent.putExtra("time", getString(R.string.event_ieee_time));
 			intent.putExtra("title", getString(R.string.event_ieee_title));
 			intent.putExtra("url", getString(R.string.event_ieee_url));
 			break;
 		case R.id.timetable_button_intelec:
 			intent.putExtra("name", getString(R.string.event_intelec_name));
 			intent.putExtra("buis", getString(R.string.event_intelec_buis));
 			intent.putExtra("des", getString(R.string.event_intelec_des));
 			intent.putExtra("loc", getString(R.string.event_intelec_loc));
 			intent.putExtra("time", getString(R.string.event_intelec_time));
 			intent.putExtra("title", getString(R.string.event_intelec_title));
 			intent.putExtra("url", getString(R.string.event_intelec_url));
 			break;
 		case R.id.timetable_button_linux:
 			intent.putExtra("name", getString(R.string.event_linux_name));
 			intent.putExtra("image", R.drawable.david_ubeda_umh);
 			intent.putExtra("buis", getString(R.string.event_linux_buis));
 			intent.putExtra("des", getString(R.string.event_linux_des));
 			intent.putExtra("loc", getString(R.string.event_linux_loc));
 			intent.putExtra("time", getString(R.string.event_linux_time));
 			intent.putExtra("title", getString(R.string.event_linux_title));
 			intent.putExtra("url", getString(R.string.event_linux_url));
 			break;
 		case R.id.timetable_button_microsoft:
 			intent.putExtra("name", getString(R.string.event_microsoft_name));
 			intent.putExtra("buis", getString(R.string.event_microsoft_buis));
 			intent.putExtra("des", getString(R.string.event_microsoft_des));
 			intent.putExtra("loc", getString(R.string.event_microsoft_loc));
 			intent.putExtra("time", getString(R.string.event_microsoft_time));
 			intent.putExtra("title", getString(R.string.event_microsoft_title));
 			intent.putExtra("url", getString(R.string.event_microsoft_url));
 			break;
 		case R.id.timetable_button_money:
 			intent.putExtra("name", getString(R.string.event_money_name));
 			intent.putExtra("image", R.drawable.asun_vicente_umh);
 			intent.putExtra("buis", getString(R.string.event_money_buis));
 			intent.putExtra("des", getString(R.string.event_money_des));
 			intent.putExtra("loc", getString(R.string.event_money_loc));
 			intent.putExtra("time", getString(R.string.event_money_time));
 			intent.putExtra("title", getString(R.string.event_money_title));
 			intent.putExtra("url", getString(R.string.event_money_url));
 			break;
 		case R.id.timetable_button_node:
 			intent.putExtra("name", getString(R.string.event_node_name));
 			intent.putExtra("buis", getString(R.string.event_node_buis));
 			intent.putExtra("des", getString(R.string.event_node_des));
 			intent.putExtra("loc", getString(R.string.event_node_loc));
 			intent.putExtra("time", getString(R.string.event_node_time));
 			intent.putExtra("title", getString(R.string.event_node_title));
 			intent.putExtra("url", getString(R.string.event_node_url));
 			break;
 		case R.id.timetable_button_opencv:
 			intent.putExtra("name", getString(R.string.event_opencv_name));
 			intent.putExtra("image", R.drawable.jesualdo_ros_geekytheory);
 			intent.putExtra("buis", getString(R.string.event_opencv_buis));
 			intent.putExtra("des", getString(R.string.event_opencv_des));
 			intent.putExtra("loc", getString(R.string.event_opencv_loc));
 			intent.putExtra("time", getString(R.string.event_opencv_time));
 			intent.putExtra("title", getString(R.string.event_opencv_title));
 			intent.putExtra("url", getString(R.string.event_opencv_url));
 			break;
 		case R.id.timetable_button_otp:
 			intent.putExtra("name", getString(R.string.event_otp_name));
 			intent.putExtra("buis", getString(R.string.event_otp_buis));
 			intent.putExtra("des", getString(R.string.event_otp_des));
 			intent.putExtra("loc", getString(R.string.event_otp_loc));
 			intent.putExtra("time", getString(R.string.event_otp_time));
 			intent.putExtra("title", getString(R.string.event_otp_title));
 			intent.putExtra("url", getString(R.string.event_otp_url));
 			break;
 		case R.id.timetable_button_sl:
 			intent.putExtra("name", getString(R.string.event_SL_name));
 			intent.putExtra("image", R.drawable.isidro_perez);
 			intent.putExtra("buis", getString(R.string.event_SL_buis));
 			intent.putExtra("des", getString(R.string.event_SL_des));
 			intent.putExtra("loc", getString(R.string.event_SL_loc));
 			intent.putExtra("time", getString(R.string.event_SL_time));
 			intent.putExtra("title", getString(R.string.event_SL_title));
 			intent.putExtra("url", getString(R.string.event_SL_url));
 			break;
 		case R.id.timetable_button_aisoy:
 			intent.putExtra("name", getString(R.string.event_aisoy_name));
 			intent.putExtra("image", R.drawable.diego_garcia_aisoy);
 			intent.putExtra("buis", getString(R.string.event_aisoy_buis));
 			intent.putExtra("des", getString(R.string.event_aisoy_des));
 			intent.putExtra("loc", getString(R.string.event_aisoy_loc));
 			intent.putExtra("time", getString(R.string.event_aisoy_time));
 			intent.putExtra("title", getString(R.string.event_aisoy_title));
 			intent.putExtra("url", getString(R.string.event_aisoy_url));
 			break;
 		case R.id.timetable_button_yuliop:
 			intent.putExtra("name", getString(R.string.event_yuilop_name));
 			intent.putExtra("buis", getString(R.string.event_yuilop_buis));
 			intent.putExtra("des", getString(R.string.event_yuilop_des));
 			intent.putExtra("loc", getString(R.string.event_yuilop_loc));
 			intent.putExtra("time", getString(R.string.event_yuilop_time));
 			intent.putExtra("title", getString(R.string.event_yuilop_title));
 			intent.putExtra("url", getString(R.string.event_yuilop_url));
 			break;
 		case R.id.timetable_button_wp:
 			intent.putExtra("name", getString(R.string.event_wp_name));
 			intent.putExtra("image", R.drawable.dachi_geekytheory);
 			intent.putExtra("buis", getString(R.string.event_wp_buis));
 			intent.putExtra("des", getString(R.string.event_wp_des));
 			intent.putExtra("loc", getString(R.string.event_wp_loc));
 			intent.putExtra("time", getString(R.string.event_wp_time));
 			intent.putExtra("title", getString(R.string.event_wp_title));
 			intent.putExtra("url", getString(R.string.event_wp_url));
 			break;
 		case R.id.timetable_button_wp2:
 			intent.putExtra("name", getString(R.string.event_wp2_name));
 			intent.putExtra("buis", getString(R.string.event_wp2_buis));
 			intent.putExtra("des", getString(R.string.event_wp2_des));
 			intent.putExtra("loc", getString(R.string.event_wp2_loc));
 			intent.putExtra("time", getString(R.string.event_wp2_time));
 			intent.putExtra("title", getString(R.string.event_wp2_title));
 			intent.putExtra("url", getString(R.string.event_wp2_url));
 			break;
 		}
 		startActivity(intent);
 	}
 }
