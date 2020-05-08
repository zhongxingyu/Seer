 package persistance;
 
 import android.app.Application;
 import android.content.Context;
 import com.thesis.emostatus.R;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 
 public class EmoStatus extends Application{
     private Context bsc;
     private int actualUserMonitorized;
     private List<ThreeDataComponent> usersMonitorized;
     private boolean tutorialEnabled = true;
 
     public EmoStatus(){
         super();
         this.usersMonitorized = new ArrayList<ThreeDataComponent>();
     }
 
     @Override
     public void onCreate(){
         super.onCreate();
         bsc = getBaseContext();
     }
 
     public void setActualUserMonitorized(int actualUserMonitorized) {
         this.actualUserMonitorized = actualUserMonitorized;
     }
 
     public String getActualUserMonitorized() {
         return this.usersMonitorized.get(this.actualUserMonitorized).getTitle();
     }
 
     public List<ThreeDataComponent> getUsersMonitorized() {
         return usersMonitorized;
     }
 
     public void setUsersMonitorized(List<ThreeDataComponent> usersMonitorized) {
         this.usersMonitorized = usersMonitorized;
     }
 
     public List<HistoryDay> getHistoryActualUserMonitorized() {
         switch (actualUserMonitorized){
             case 0:
                 return getHistoryUser1();
             case 1:
                 return getHistoryUser2();
             case 2:
                 return getHistoryUser3();
         }
         return null;
     }
 
     public List<HistoryDay> getHistoryUser1(){
         List<HistoryDay> history = new ArrayList<HistoryDay>();
 
         HistoryDay h1 = new HistoryDay();
         HistoryDay h2 = new HistoryDay();
         HistoryDay h3 = new HistoryDay();
         HistoryDay h4 = new HistoryDay();
         HistoryDay h5 = new HistoryDay();
 
         List<ThreeDataComponent> h1Data = new ArrayList<ThreeDataComponent>();
         List<ThreeDataComponent> h2Data = new ArrayList<ThreeDataComponent>();
         List<ThreeDataComponent> h3Data = new ArrayList<ThreeDataComponent>();
         List<ThreeDataComponent> h4Data = new ArrayList<ThreeDataComponent>();
         List<ThreeDataComponent> h5Data = new ArrayList<ThreeDataComponent>();
 
         Calendar c = Calendar.getInstance();
 
         h1.setDay_date("Hoy");
         h1.setDate(c.getTime());
         h1.setWasSad(true);
         h1Data.add(new ThreeDataComponent("Triste (90%)","10:00 - Grabación", R.drawable.emo_sad));
         h1Data.add(new ThreeDataComponent("Triste (75%)","09:30 - Grabación", R.drawable.emo_sad));
         h1.setHistory(h1Data);
 
         c.roll(Calendar.DAY_OF_YEAR, -1);
         Date yesterday = c.getTime();
 
         c.roll(Calendar.DAY_OF_YEAR, -3);
         Date four_days_ago = c.getTime();
 
         c.roll(Calendar.DAY_OF_YEAR, -5);
         Date one_week_ago = c.getTime();
 
         c.roll(Calendar.DAY_OF_YEAR, -10);
         Date two_weeks_ago = c.getTime();
 
         c.setTime(yesterday);
 
         h2.setDay_date("Ayer");
         h2.setDate(c.getTime());
         h2.setWasSad(true);
         h2Data.add(new ThreeDataComponent("Normal", "19:00 - Skype", R.drawable.emo_normal));
         h2Data.add(new ThreeDataComponent("Normal","18:00 - Grabación", R.drawable.emo_normal));
         h2Data.add(new ThreeDataComponent("Triste (82%)","17:15 - Grabación", R.drawable.emo_sad));
         h2.setHistory(h2Data);
 
         c.setTime(four_days_ago);
 
         h3.setDay_date(getDayWeek(c.get(Calendar.DAY_OF_WEEK)) + " " + c.get(Calendar.DAY_OF_MONTH) + " de "
                 + getMonth(c.get(Calendar.MONTH)));
         h3.setDate(c.getTime());
         h3.setWasSad(false);
         h3Data.add(new ThreeDataComponent("Normal","21:15 - Skype", R.drawable.emo_normal));
         h3Data.add(new ThreeDataComponent("Normal","21:00 - Skype", R.drawable.emo_normal));
         h3.setHistory(h3Data);
 
         c.setTime(one_week_ago);
 
         h4.setDay_date(getDayWeek(c.get(Calendar.DAY_OF_WEEK)) + " " + c.get(Calendar.DAY_OF_MONTH) + " de "
                 + getMonth(c.get(Calendar.MONTH)));
         h4.setDate(c.getTime());
         h4.setWasSad(true);
        h4Data.add(new ThreeDataComponent("Triste (90%)", "19:45 - Grabación", R.drawable.emo_sad));
         h4Data.add(new ThreeDataComponent("Normal","16:30 - Grabación", R.drawable.emo_normal));
         h4Data.add(new ThreeDataComponent("Normal","15:00 - Grabación", R.drawable.emo_normal));
         h4.setHistory(h4Data);
 
         c.setTime(two_weeks_ago);
 
         h5.setDay_date(getDayWeek(c.get(Calendar.DAY_OF_WEEK)) + " " + c.get(Calendar.DAY_OF_MONTH) + " de "
                 + getMonth(c.get(Calendar.MONTH)));
         h5.setDate(c.getTime());
         h5.setWasSad(true);
         h5Data.add(new ThreeDataComponent("Triste (77%)","10:00 - Grabación", R.drawable.emo_sad));
         h5Data.add(new ThreeDataComponent("Normal","08:45 - Skype", R.drawable.emo_normal));
         h5.setHistory(h5Data);
 
         history.add(h1);
         history.add(h2);
         history.add(h3);
         history.add(h4);
         history.add(h5);
 
         return history;
     }
 
     public List<HistoryDay> getHistoryUser2(){
         List<HistoryDay> history = new ArrayList<HistoryDay>();
 
         HistoryDay h1 = new HistoryDay();
         HistoryDay h2 = new HistoryDay();
         HistoryDay h3 = new HistoryDay();
         HistoryDay h4 = new HistoryDay();
         HistoryDay h5 = new HistoryDay();
 
         List<ThreeDataComponent> h1Data = new ArrayList<ThreeDataComponent>();
         List<ThreeDataComponent> h2Data = new ArrayList<ThreeDataComponent>();
         List<ThreeDataComponent> h3Data = new ArrayList<ThreeDataComponent>();
         List<ThreeDataComponent> h4Data = new ArrayList<ThreeDataComponent>();
         List<ThreeDataComponent> h5Data = new ArrayList<ThreeDataComponent>();
 
         Calendar c = Calendar.getInstance();
 
         h1.setDay_date("Hoy");
         h1.setDate(c.getTime());
         h1.setWasSad(true);
         h1Data.add(new ThreeDataComponent("Triste (70%)","10:00 - Grabación", R.drawable.emo_sad));
         h1Data.add(new ThreeDataComponent("Triste (85%)","09:30 - Grabación", R.drawable.emo_sad));
         h1.setHistory(h1Data);
 
         c.roll(Calendar.DAY_OF_YEAR, -4);
         Date yesterday = c.getTime();
 
         c.roll(Calendar.DAY_OF_YEAR, -9);
         Date four_days_ago = c.getTime();
 
         c.roll(Calendar.DAY_OF_YEAR, -2);
         Date one_week_ago = c.getTime();
 
         c.roll(Calendar.DAY_OF_YEAR, -3);
         Date two_weeks_ago = c.getTime();
 
         c.setTime(yesterday);
 
         h2.setDay_date(getDayWeek(c.get(Calendar.DAY_OF_WEEK)) + " " + c.get(Calendar.DAY_OF_MONTH) + " de "
                 + getMonth(c.get(Calendar.MONTH)));
         h2.setDate(c.getTime());
         h2.setWasSad(true);
         h2Data.add(new ThreeDataComponent("Normal", "19:00 - Skype", R.drawable.emo_normal));
         h2Data.add(new ThreeDataComponent("Normal","18:00 - Grabación", R.drawable.emo_normal));
         h2Data.add(new ThreeDataComponent("Triste (82%)","17:15 - Grabación", R.drawable.emo_sad));
         h2.setHistory(h2Data);
 
         c.setTime(four_days_ago);
 
         h3.setDay_date(getDayWeek(c.get(Calendar.DAY_OF_WEEK)) + " " + c.get(Calendar.DAY_OF_MONTH) + " de "
                 + getMonth(c.get(Calendar.MONTH)));
         h3.setDate(c.getTime());
         h3.setWasSad(true);
         h3Data.add(new ThreeDataComponent("Triste (70%)","21:15 - Skype", R.drawable.emo_sad));
         h3Data.add(new ThreeDataComponent("Triste (80%)","21:00 - Skype", R.drawable.emo_sad));
         h3.setHistory(h3Data);
 
         c.setTime(one_week_ago);
 
         h4.setDay_date(getDayWeek(c.get(Calendar.DAY_OF_WEEK)) + " " + c.get(Calendar.DAY_OF_MONTH) + " de "
                 + getMonth(c.get(Calendar.MONTH)));
         h4.setDate(c.getTime());
         h4.setWasSad(true);
        h4Data.add(new ThreeDataComponent("Triste (85%)", "19:45 - Grabación", R.drawable.emo_sad));
         h4Data.add(new ThreeDataComponent("Normal","16:30 - Grabación", R.drawable.emo_normal));
         h4Data.add(new ThreeDataComponent("Normal","15:00 - Grabación", R.drawable.emo_normal));
         h4.setHistory(h4Data);
 
         c.setTime(two_weeks_ago);
 
         h5.setDay_date(getDayWeek(c.get(Calendar.DAY_OF_WEEK)) + " " + c.get(Calendar.DAY_OF_MONTH) + " de "
                 + getMonth(c.get(Calendar.MONTH)));
         h5.setDate(c.getTime());
         h5.setWasSad(true);
         h5Data.add(new ThreeDataComponent("Triste (77%)","10:00 - Grabación", R.drawable.emo_sad));
         h5Data.add(new ThreeDataComponent("Normal","08:45 - Skype", R.drawable.emo_normal));
         h5.setHistory(h5Data);
 
         history.add(h1);
         history.add(h2);
         history.add(h3);
         history.add(h4);
         history.add(h5);
 
         return history;
     }
 
     public List<HistoryDay> getHistoryUser3(){
         List<HistoryDay> history = new ArrayList<HistoryDay>();
 
         HistoryDay h1 = new HistoryDay();
         HistoryDay h2 = new HistoryDay();
         HistoryDay h3 = new HistoryDay();
         HistoryDay h4 = new HistoryDay();
         HistoryDay h5 = new HistoryDay();
 
         List<ThreeDataComponent> h1Data = new ArrayList<ThreeDataComponent>();
         List<ThreeDataComponent> h2Data = new ArrayList<ThreeDataComponent>();
         List<ThreeDataComponent> h3Data = new ArrayList<ThreeDataComponent>();
         List<ThreeDataComponent> h4Data = new ArrayList<ThreeDataComponent>();
         List<ThreeDataComponent> h5Data = new ArrayList<ThreeDataComponent>();
 
         Calendar c = Calendar.getInstance();
 
         h1.setDay_date("Hoy");
         h1.setDate(c.getTime());
         h1.setWasSad(true);
         h1Data.add(new ThreeDataComponent("Normal","10:00 - Grabación", R.drawable.emo_normal));
         h1Data.add(new ThreeDataComponent("Triste (75%)","09:30 - Grabación", R.drawable.emo_sad));
         h1.setHistory(h1Data);
 
         c.roll(Calendar.DAY_OF_YEAR, -2);
         Date yesterday = c.getTime();
 
         c.roll(Calendar.DAY_OF_YEAR, -1);
         Date four_days_ago = c.getTime();
 
         c.roll(Calendar.DAY_OF_YEAR, -1);
         Date one_week_ago = c.getTime();
 
         c.roll(Calendar.DAY_OF_YEAR, -6);
         Date two_weeks_ago = c.getTime();
 
         c.setTime(yesterday);
 
         h2.setDay_date(getDayWeek(c.get(Calendar.DAY_OF_WEEK)) + " " + c.get(Calendar.DAY_OF_MONTH) + " de "
                 + getMonth(c.get(Calendar.MONTH)));
         h2.setDate(c.getTime());
         h2.setWasSad(false);
         h2Data.add(new ThreeDataComponent("Normal", "19:00 - Skype", R.drawable.emo_normal));
         h2Data.add(new ThreeDataComponent("Normal","18:00 - Grabación", R.drawable.emo_normal));
         h2.setHistory(h2Data);
 
         c.setTime(four_days_ago);
 
         h3.setDay_date(getDayWeek(c.get(Calendar.DAY_OF_WEEK)) + " " + c.get(Calendar.DAY_OF_MONTH) + " de "
                 + getMonth(c.get(Calendar.MONTH)));
         h3.setDate(c.getTime());
         h3.setWasSad(true);
         h3Data.add(new ThreeDataComponent("Triste (70%)","21:15 - Skype", R.drawable.emo_sad));
         h3Data.add(new ThreeDataComponent("Triste (80%)","21:00 - Skype", R.drawable.emo_sad));
         h3.setHistory(h3Data);
 
         c.setTime(one_week_ago);
 
         h4.setDay_date(getDayWeek(c.get(Calendar.DAY_OF_WEEK)) + " " + c.get(Calendar.DAY_OF_MONTH) + " de "
                 + getMonth(c.get(Calendar.MONTH)));
         h4.setDate(c.getTime());
         h4.setWasSad(false);
         h4Data.add(new ThreeDataComponent("Normal","16:30 - Grabación", R.drawable.emo_normal));
         h4Data.add(new ThreeDataComponent("Normal","15:00 - Grabación", R.drawable.emo_normal));
         h4.setHistory(h4Data);
 
         c.setTime(two_weeks_ago);
 
         h5.setDay_date(getDayWeek(c.get(Calendar.DAY_OF_WEEK)) + " " + c.get(Calendar.DAY_OF_MONTH) + " de "
                 + getMonth(c.get(Calendar.MONTH)));
         h5.setDate(c.getTime());
         h5.setWasSad(true);
         h5Data.add(new ThreeDataComponent("Triste (77%)","10:00 - Grabación", R.drawable.emo_sad));
         h5Data.add(new ThreeDataComponent("Normal","08:45 - Skype", R.drawable.emo_normal));
         h5.setHistory(h5Data);
 
         history.add(h1);
         history.add(h2);
         history.add(h3);
         history.add(h4);
         history.add(h5);
 
         return history;
     }
 
 
 
     private String getMonth(int month) {
         if(month == 0)
             return "Enero";
         if(month == 1)
             return "Febrero";
         if(month == 2)
             return "Marzo";
         if(month == 3)
             return "Abril";
         if(month == 4)
             return "Mayo";
         if(month == 5)
             return "Junio";
         if(month == 6)
             return "Julio";
         if(month == 7)
             return "Agosto";
         if(month == 8)
             return "Septiembre";
         if(month == 9)
             return "Octubre";
         if(month == 10)
             return "Noviembre";
         if(month == 11)
             return "Diciembre";
 
         return "None";
 
     }
 
     private String getDayWeek(int dayOfWeek) {
         if(dayOfWeek == 1)
             return "Domingo";
         if(dayOfWeek == 2)
             return "Lunes";
         if(dayOfWeek == 3)
             return "Martes";
         if(dayOfWeek == 4)
             return "Miércoles";
         if(dayOfWeek == 5)
             return "Jueves";
         if(dayOfWeek == 6)
             return "Viernes";
         if(dayOfWeek == 7)
             return "Sábado";
 
         return "None";
     }
 
     public boolean isTutorialEnabled() {
         return tutorialEnabled;
     }
 
     public void setTutorialEnabled(boolean tutorialEnabled) {
         this.tutorialEnabled = tutorialEnabled;
     }
 }
