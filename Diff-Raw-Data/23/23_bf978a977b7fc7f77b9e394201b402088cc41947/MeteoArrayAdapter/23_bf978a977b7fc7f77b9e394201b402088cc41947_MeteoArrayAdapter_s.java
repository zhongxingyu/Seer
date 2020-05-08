 package com.storassa.android.scuolasci;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class MeteoArrayAdapter extends ArrayAdapter<MeteoItem> {
    int resource;
    int day;
    int currentHour;
    boolean daily;
 
    public MeteoArrayAdapter(Context context, int _resource,
          List<MeteoItem> items, boolean _daily, int _day, int _currentHour) {
       super(context, _resource, items);
       resource = _resource;
       day = _day;
       daily = _daily;
       currentHour = _currentHour;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       // Create and inflate the View to display
       LinearLayout newView;
       if (convertView == null) {
          // Inflate a new view if this is not an update.
          newView = new LinearLayout(getContext());
          String inflater = Context.LAYOUT_INFLATER_SERVICE;
          LayoutInflater li;
          li = (LayoutInflater) getContext().getSystemService(inflater);
          li.inflate(resource, newView, true);
       } else {
          // Otherwise we'll update the existing View
          newView = (LinearLayout) convertView;
       }
       MeteoItem item = getItem(position);
 
       int iconResource = item.getIconResourceId();
       int minTemp = (int) item.getMinTemp();
       int maxTemp = (int) item.getMaxTemp();
       int humidity = (int) (item.getHumidity() * 100);
       int precipit = (int) (item.getPrecipit() * 100);
 
       Date date = new Date();
       Calendar c = Calendar.getInstance();
 
       try {
          c.setTime(date);
       } catch (Exception e) {
          throw new RuntimeException(e.toString());
       }
 
       // add the day to the current date; as Calendar consider Sunday as first
       // day
       // you need to decrease the day
       if (daily) {
          c.add(Calendar.DATE, position - 1); // number of days to add
       } else {
          c.add(Calendar.DATE, day - 1);
       }
 
       DayOfWeek dayOfWeek = DayOfWeek.values()[c.get(Calendar.DAY_OF_WEEK) - 1];
       String dayOfWeekString = dayOfWeek.getLabel(parent.getContext());
 
       Month month = Month.values()[c.get(Calendar.MONTH)];
       String monthString = month.getLabel(parent.getContext());
       int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
       int year = c.get(Calendar.YEAR);
 
       String dayString = String.valueOf(dayOfMonth);
       dayString += " " + monthString + " " + String.valueOf(year);
 
       ImageView meteoImageView = (ImageView) newView
             .findViewById(R.id.meteo_list_image);
       TextView minTempView = (TextView) newView
             .findViewById(R.id.meteo_list_min_temp_text);
       TextView maxTempView = (TextView) newView
             .findViewById(R.id.meteo_list_max_temp_text);
       TextView humidityView = (TextView) newView
             .findViewById(R.id.meteo_list_humidity_text);
       TextView precipitView = (TextView) newView
             .findViewById(R.id.meteo_list_precipit_text);
       TextView meteoDayOfWeek = (TextView) newView
             .findViewById(R.id.meteo_day_of_week);
       TextView meteoDay = (TextView) newView.findViewById(R.id.meteo_day);
       TextView meteoHour = (TextView) newView.findViewById(R.id.meteo_hour);
 
       meteoImageView.setImageResource(iconResource);
       minTempView.setText(" " + String.valueOf(minTemp));
       maxTempView.setText(" " + String.valueOf(maxTemp));
       humidityView.setText(" " + String.valueOf(humidity) + "%");
       precipitView.setText(" " + String.valueOf(precipit) + "%");
       meteoDayOfWeek.setText(dayOfWeekString);
       meteoDay.setText(dayString);
       if (!daily)
         if (day != 0)
             meteoHour.setText(position + ":00");
          else
             meteoHour.setText(position + currentHour + ":00");
       return newView;
    }
 }
