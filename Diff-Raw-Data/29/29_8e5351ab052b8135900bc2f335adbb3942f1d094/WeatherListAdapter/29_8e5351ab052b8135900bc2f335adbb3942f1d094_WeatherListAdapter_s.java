 package com.mobileappdevelopersclub.shellp.adapters;
 
 import java.util.List;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.mobileappdevelopersclub.shellp.R;
 import com.mobileappdevelopersclub.shellp.models.Weather;
 
 public class WeatherListAdapter extends ArrayAdapter<Weather>{
 
 	private Context context;
 	private LayoutInflater mInflater;
 	private List<Weather> mWeatherList;
 
 	public WeatherListAdapter(Context context, int textViewResourceId,
 			List<Weather> objects ) {
 		super(context, textViewResourceId, objects);
 		this.context = context;
 		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		this.mWeatherList = objects;
 		//		imageLoader = new ImageLoader();
 
 	}



 
 
 	@Override
 	public int getCount() {
 		// TODO Auto-generated method stub
 		return (mWeatherList.size() > 0) ? mWeatherList.size() : 0;
 	}
 
 
 	private class WeatherViewHolder {
 		RelativeLayout itemBackground;
 		TextView header;
 		TextView tempMax;
 		TextView tempMin;
 		TextView weatherDesc;
 		TextView date;
 	}
 
 
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		// TODO Auto-generated method stub
 
 		WeatherViewHolder mViewHolder;
 
 		if(convertView == null) {
 			mViewHolder = new WeatherViewHolder();
 			convertView = View.inflate(context, R.layout.weather_item, null);
 			mViewHolder.header = (TextView) convertView.findViewById(R.id.header);
 			mViewHolder.itemBackground = (RelativeLayout)convertView.findViewById(R.id.weather_item_layout);
 			mViewHolder.tempMax = (TextView) convertView.findViewById(R.id.tempMax);
 			mViewHolder.tempMin = (TextView) convertView.findViewById(R.id.tempMin);
 			mViewHolder.weatherDesc = (TextView) convertView.findViewById(R.id.weatherDescription);
 			mViewHolder.date = (TextView) convertView.findViewById(R.id.date);
 
 
 			convertView.setTag(mViewHolder);
 		}
 		else {
 			mViewHolder = (WeatherViewHolder) convertView.getTag();
 		}
 		
 		Weather weather = mWeatherList.get(position);
 		
 		setWeatherBackground(weather, mViewHolder.itemBackground);
 		
 		mViewHolder.tempMax.setText(weather.getTempMax());
 		mViewHolder.tempMin.setText(weather.getTempMin());
 		mViewHolder.weatherDesc.setText(weather.getDesctiptions().get(0).getValue());
 		mViewHolder.date.setText(formatDate(weather.getDate()));
 		
 		if(position == 0) {
 			mViewHolder.header.setVisibility(View.VISIBLE);
 		}
 
 		return convertView;
 	}
 	
 	private void setWeatherBackground(Weather weather, RelativeLayout layout) {
 		int temp = Integer.parseInt(weather.getTempMax());
 		
 		if(temp < 30 ) {
 			layout.setBackgroundColor(context.getResources().getColor(R.color.cold_blue));
 		} else if( temp < 55) {
 			layout.setBackgroundColor(context.getResources().getColor(R.color.mid_cool));
 		} else if (temp < 75){
 			layout.setBackgroundColor(context.getResources().getColor(R.color.mid_heat));
 		} else  {
 			layout.setBackgroundColor(context.getResources().getColor(R.color.hot_red));
 		}
 	}
 	
 	private String formatDate(String date) {
 		String monthVal = date.substring(5, 7);
 		int month = Integer.parseInt(monthVal);
 		String monthString = "";
 		switch (month) {
         case 1:  monthString = "January";       break;
         case 2:  monthString = "February";      break;
         case 3:  monthString = "March";         break;
         case 4:  monthString = "April";         break;
         case 5:  monthString = "May";           break;
         case 6:  monthString = "June";          break;
         case 7:  monthString = "July";          break;
         case 8:  monthString = "August";        break;
         case 9:  monthString = "September";     break;
         case 10: monthString = "October";       break;
         case 11: monthString = "November";      break;
         case 12: monthString = "December";      break;
         default: monthString = "Invalid month"; break;
     }
 		return monthString + " " + date.substring(8, date.length() );
 		
 	}
 }
