 package info.twiceyuan.weather.view;
 
 import android.app.Activity;
 import android.os.Handler;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import info.twiceyuan.weather.R;
 import info.twiceyuan.weather.controller.MainController;
 import info.twiceyuan.weather.domain.Weather;
 import info.twiceyuan.weather.util.DatabaseTools;
 import info.twiceyuan.weather.util.WeatherIconGetter;
 import info.twiceyuan.weather.util.WeekConverter;
 
 public class CardViewTools {
 
     private Activity activity;
     private Weather weather;
     private RelativeLayout cardView;
     private Button upButton;
     private Button downButton;
     private Button deleteButton;
     private Handler handler;
 
     private DatabaseTools dbTools;
 
     private String cityName;
 
    public CardViewTools(Activity activity, Weather weather, Handler handler) {
 
         this.activity = activity;
         this.weather = weather;
         this.handler = handler;
         this.cityName = weather.getCity();
     }
 
     public RelativeLayout getCardView() {
 
         TextView cityNameView;
         ImageView weatherImageView;
         TextView weatherView;
         TextView tempView;
         TextView[] weekViews = new TextView[4];
         ImageView[] weekWeatherIconView = new ImageView[4];
         TextView[] weekUps = new TextView[4];
         TextView[] weekDowns = new TextView[4];
 
         int iweek = WeekConverter.getWeekint(weather.getWeek());
 
         cardView = (RelativeLayout) RelativeLayout.inflate(
                 activity, R.layout.cardview, null);
 
         RelativeLayout toplayout = (RelativeLayout) cardView.getChildAt(0);
         cityNameView = (TextView) toplayout.getChildAt(0);
 
         dbTools = new DatabaseTools(activity);
 
         deleteButton = (Button) toplayout.getChildAt(1);
 
         upButton = (Button) toplayout.getChildAt(2);
 
         downButton = (Button) toplayout.getChildAt(3);
 
         deleteButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
 
                if (dbTools.deleteCity(weather.getCityid()).equals("success")) {
                     handler.sendEmptyMessage(MainController.REFRESH); // 通知主控制器刷新
                 }
             }
         });
 
         upButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                if (dbTools.modifyOrder(weather.getCityid(), 1).equals("up")) {
                     handler.sendEmptyMessage(MainController.REFRESH); // 通知主控制器刷新
                 }
             }
         });
 
         downButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                if (dbTools.modifyOrder(weather.getCityid(), 2).equals("down")) {
                     handler.sendEmptyMessage(MainController.REFRESH); // 通知主控制器刷新
                 }
             }
         });
 
         RelativeLayout todayLayout = (RelativeLayout) cardView.getChildAt(1);
         weatherImageView = (ImageView) todayLayout.getChildAt(0);
         tempView = (TextView) todayLayout.getChildAt(1);
         weatherView = (TextView) todayLayout.getChildAt(2);
 
         LinearLayout weeklayout = (LinearLayout) cardView.getChildAt(2);
         for (int i = 0; i < 4; i++) {
             LinearLayout aday = (LinearLayout) weeklayout.getChildAt(i);
             weekViews[i] = (TextView) aday.getChildAt(0);
             weekWeatherIconView[i] = (ImageView) aday.getChildAt(1);
             weekUps[i] = (TextView) aday.getChildAt(2);
             weekDowns[i] = (TextView) aday.getChildAt(3);
         }
 
         cityNameView.setText(cityName);
         weatherView.setText(weather.getWeather()[0]);
         weatherImageView.setImageDrawable(WeatherIconGetter.getWatherIcon(activity, weather.getWeathericon()[0]));
         tempView.setText(weather.getTemp()[0]);
 
        for (int i = 0; i < 4; i++) {
             weekViews[i].setText(WeekConverter.getWeek(iweek + i + 1));
            weekWeatherIconView[i].setImageDrawable(WeatherIconGetter.getWatherIcon(activity, weather.getWeathericon()[i + 1]));
            weekUps[i].setText(weather.getTemp()[i + 1].split("~")[0]);
            weekDowns[i].setText(weather.getTemp()[i + 1].split("~")[1]);
         }
 
         return this.cardView;
     }
 
 
 }
