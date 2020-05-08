 package no.nith.pg3200.a02.utils;
 
 import android.content.Context;
 import android.util.SparseIntArray;
 import java.util.ArrayList;
 import no.nith.pg3200.a02.R;
 import no.nith.pg3200.a02.db.WeatherDataDao;
 import no.nith.pg3200.a02.domain.WeatherData;
 import org.jetbrains.annotations.NotNull;
 
 /**
  * @author Simen Bekkhus
  */
 public final class Utils {
     private static final SparseIntArray mapIcons = new SparseIntArray() {{
         put(1, R.drawable.img_1);
         put(2, R.drawable.img_2);
         put(3, R.drawable.img_3);
         put(4, R.drawable.img_4);
         put(5, R.drawable.img_5);
         put(6, R.drawable.img_6);
         put(7, R.drawable.img_7);
         put(8, R.drawable.img_8);
         put(9, R.drawable.img_9);
         put(10, R.drawable.img_10);
         put(11, R.drawable.img_11);
         put(12, R.drawable.img_12);
         put(13, R.drawable.img_13);
         put(14, R.drawable.img_14);
         put(15, R.drawable.img_15);
         put(16, R.drawable.img_16);
         put(17, R.drawable.img_17);
         put(18, R.drawable.img_18);
         put(19, R.drawable.img_19);
         put(20, R.drawable.img_20);
         put(21, R.drawable.img_21);
         put(22, R.drawable.img_22);
         put(23, R.drawable.img_23);
     }};
     private static ArrayList<WeatherData> weatherDataArray;
     @NotNull
     private static WeatherDataDao dao;
 
     private Utils() {
     }
 
     public static void initUtils(final Context context) {
         dao = new WeatherDataDao(context);
     }
 
     public static ArrayList<WeatherData> getWeatherDataArray() {
         return weatherDataArray;
     }
 
     public static WeatherDataDao getDao() {
         return dao;
     }
 
     public static void fetchWeatherData() {
         weatherDataArray = dao.getAllWeatherData();
     }
 
     public static int getIcon(final int index) {
         return mapIcons.get(index);
     }
 
     public static void addWeatherData(final WeatherData weatherData) {
         weatherDataArray.add(weatherData);
 
         dao.insertWeatherData(weatherData);
     }
 }
