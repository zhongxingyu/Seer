 package fr.ybo.ybotv.android.activity;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.text.Html;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.analytics.tracking.android.EasyTracker;
 import fr.ybo.ybotv.android.R;
 import fr.ybo.ybotv.android.YboTvApplication;
 import fr.ybo.ybotv.android.adapter.ProgrammeAdapter;
 import fr.ybo.ybotv.android.adapter.ProgrammeViewFlowAdapter;
 import fr.ybo.ybotv.android.exception.YboTvErreurReseau;
 import fr.ybo.ybotv.android.exception.YboTvException;
 import fr.ybo.ybotv.android.lasylist.RatingLoader;
 import fr.ybo.ybotv.android.lasylist.ImageLoader;
 import fr.ybo.ybotv.android.modele.Channel;
 import fr.ybo.ybotv.android.modele.Programme;
 import fr.ybo.ybotv.android.receiver.AlertReceiver;
 import fr.ybo.ybotv.android.service.YouTubeService;
 import fr.ybo.ybotv.android.util.AdMobUtil;
 import fr.ybo.ybotv.android.util.CalendarUtil;
 import fr.ybo.ybotv.android.util.GetView;
 import fr.ybo.ybotv.android.util.TacheAvecGestionErreurReseau;
 import org.taptwo.android.widget.TitleFlowIndicator;
 import org.taptwo.android.widget.ViewFlow;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 public class ProgrammeActivity extends SherlockActivity implements GetView {
 
     private Programme programme;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         overridePendingTransition(R.anim.fadein, R.anim.fadeout);
         programme = getIntent().getParcelableExtra("programme");
         getSupportActionBar().setTitle(programme.getTitle());
 
         if (((YboTvApplication) getApplication()).isTablet()) {
             createViewForTablet(programme);
         } else {
             createViewForPhone(programme);
         }
 
         AdMobUtil.manageAds(this);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         MenuItem itemAgenda = menu.add(Menu.NONE, R.id.menu_calendar, Menu.NONE, R.string.menu_calendar);
         itemAgenda.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
         itemAgenda.setIcon(android.R.drawable.ic_menu_agenda);
         MenuItem itemShare = menu.add(Menu.NONE, R.id.menu_share, Menu.NONE, R.string.menu_share);
         itemShare.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
         itemShare.setIcon(android.R.drawable.ic_menu_share);
         String currentDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
         if (currentDate.compareTo(programme.getStart()) < 0) {
             MenuItem item = menu.add(Menu.NONE, R.id.menu_alert, Menu.NONE, R.string.menu_alert);
             item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
             setMenuAlertIcon(item);
         }
         return true;
     }
 
     private void setMenuAlertIcon(MenuItem item) {
         if (programme.hasAlert(getApplication())) {
             item.setIcon(R.drawable.ic_menu_alert_on);
         } else {
             item.setIcon(R.drawable.ic_menu_alert_off);
         }
     }
 
     private Channel channel = null;
 
     private Channel getChannel() {
         if (channel == null) {
             Channel channelSelect = new Channel();
             channelSelect.setId(programme.getChannel());
             channel = ((YboTvApplication) getApplication()).getDatabase().selectSingle(channelSelect);
         }
         return channel;
     }
 
     private void createNotification() {
         long timeToNotif;
         try {
             // The notification is 3 minutes before programme start.
             timeToNotif = new SimpleDateFormat("yyyyMMddHHmmss").parse(programme.getStart()).getTime() - (3 * 60 * 1000);
         } catch (ParseException e) {
             throw new YboTvException(e);
         }
 
 
         Intent alert = new Intent(this, AlertReceiver.class);
         alert.putExtra("programme", (Parcelable) programme);
         alert.putExtra("channel", (Parcelable) getChannel());
         int notificationId = Integer.parseInt(programme.getStart().substring(8));
 
         PendingIntent pendingAlert = PendingIntent.getBroadcast(this, notificationId, alert, PendingIntent.FLAG_CANCEL_CURRENT);
         AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         alarms.set(AlarmManager.RTC_WAKEUP, timeToNotif, pendingAlert);
     }
 
     private void cancelNotification() {
         Intent alert = new Intent(this, AlertReceiver.class);
         alert.putExtra("programme", (Parcelable) programme);
         alert.putExtra("channel", (Parcelable) getChannel());
         int notificationId = Integer.parseInt(programme.getStart().substring(8));
         PendingIntent pendingAlert = PendingIntent.getBroadcast(this, notificationId, alert, PendingIntent.FLAG_CANCEL_CURRENT);
 
         AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         alarms.cancel(pendingAlert);
     }
 
     private void addProgrammeToAgenda() {
         Map<Integer, String> calendars = CalendarUtil.getCalendars(getContentResolver());
         if (calendars.size() > 1) {
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             final int[] calenderIds = new int[calendars.size()];
             CharSequence[] calenderNames = new String[calendars.size()];
             int index = 0;
             for (Map.Entry<Integer, String> entry : calendars.entrySet()) {
                 calenderIds[index] = entry.getKey();
                 calenderNames[index] = entry.getValue();
                 index++;
             }
 
             builder.setItems(calenderNames, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     addInCalendar(calenderIds[which]);
                 }
             });
             builder.show();
         } else if (calendars.size() == 1) {
             addInCalendar(calendars.entrySet().iterator().next().getKey());
         } else {
             Toast.makeText(this, R.string.noCalendarError, Toast.LENGTH_LONG).show();
         }
     }
 
     private void addInCalendar(int calendarId) {
         try {
             SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
             long startTime = sdf.parse(programme.getStart()).getTime();
             long stopTime = sdf.parse(programme.getStop()).getTime();
             CalendarUtil.addToCalendar(this, getChannel().getDisplayName() + " - " + programme.getTitle(), programme.getDesc(), calendarId, startTime, stopTime);
             Toast.makeText(this, R.string.eventAdded, Toast.LENGTH_SHORT).show();
         } catch (ParseException e) {
             throw new YboTvException(e);
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.id.menu_calendar) {
             addProgrammeToAgenda();
         } else if (item.getItemId() == R.id.menu_share) {
             Intent intent = new Intent(Intent.ACTION_SEND);
             intent.setType("text/plain");
             intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.shareSubject, programme.getTitle()));
             intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareText, programme.getTitle()));
             startActivity(Intent.createChooser(intent, getString(R.string.app_name)));
         } else if (item.getItemId() == R.id.menu_alert) {
             programme.setHasAlert(getApplication(), !programme.hasAlert(getApplication()));
             if (programme.hasAlert(getApplication())) {
                 createNotification();
             } else {
                 cancelNotification();
             }
             setMenuAlertIcon(item);
         }
         return super.onOptionsItemSelected(item);
     }
 
     private void createViewForPhone(Programme programme) {
         setContentView(R.layout.flow);
         ViewFlow viewFlow = (ViewFlow) findViewById(R.id.viewflow);
         ProgrammeViewFlowAdapter adapter = new ProgrammeViewFlowAdapter(this, programme);
         viewFlow.setAdapter(adapter);
         TitleFlowIndicator indicator = (TitleFlowIndicator) findViewById(R.id.viewflowindic);
         indicator.setTitleProvider(adapter);
         viewFlow.setFlowIndicator(indicator);
     }
 
     private void createViewForTablet(Programme programme) {
         setContentView(R.layout.programme_for_tablet);
         contructResumeView(this, this, programme);
         contructDetailView(this, this, programme);
         if (programme.isMovie()) {
             contructTrailerView(this, this, programme);
         } else {
             findViewById(R.id.programme_trailer_loading).setVisibility(View.GONE);
         }
     }
 
     public static void contructTrailerView(final Context context, GetView getView, final Programme programme) {
         if (programme == null || programme.getTitle() == null) {
             return;
         }
 
         final Button trailerButton = (Button) getView.findViewById(R.id.programme_trailer_button);
         final TextView trailerLoading = (TextView) getView.findViewById(R.id.programme_trailer_loading);
         trailerButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 String videoId = (String) v.getTag();
                 Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
                 intent.putExtra("VIDEO_ID", videoId);
                 try {
                     context.startActivity(intent);
                 } catch (ActivityNotFoundException ignore) {
                     Toast.makeText(context, R.string.youtubeNotFound, Toast.LENGTH_LONG).show();
                 }
             }
         });
 
         new TacheAvecGestionErreurReseau(context) {
             private String videoId;
             @Override
             protected void myDoBackground() throws YboTvErreurReseau {
                 videoId = YouTubeService.getInstance().getFirstResult("Bande annonce " + programme.getTitle());
             }
 
             @Override
             protected void onPostExecute(Void result) {
                 super.onPostExecute(result);
                 if (videoId == null) {
                     trailerLoading.setText(R.string.no_trailer_found);
                 } else {
                     trailerLoading.setVisibility(View.INVISIBLE);
                     trailerButton.setVisibility(View.VISIBLE);
                     trailerButton.setTag(videoId);
                 }
             }
         }.execute();
     }
 
     public static void contructDetailView(Context context, GetView getView, Programme programme) {
         TextView duree = (TextView) getView.findViewById(R.id.programme_detail_duree);
         TextView date = (TextView) getView.findViewById(R.id.programme_detail_date);
         TextView credits = (TextView) getView.findViewById(R.id.programme_detail_credits);
 
         duree.setText(context.getString(R.string.duree, programme.getDuree()));
 
         if (programme.getDate() == null) {
             date.setVisibility(View.GONE);
         } else {
             date.setVisibility(View.VISIBLE);
             date.setText(context.getString(R.string.date, programme.getDate()));
         }
 
         StringBuilder builderCredits = new StringBuilder();
 
         for (String director : programme.getDirectors()) {
             builderCredits.append(context.getString(R.string.director, director));
             builderCredits.append('\n');
         }
         for (String actor : programme.getActors()) {
             builderCredits.append(context.getString(R.string.actor, actor));
             builderCredits.append('\n');
         }
         for (String writer : programme.getWriters()) {
             builderCredits.append(context.getString(R.string.writer, writer));
             builderCredits.append('\n');
         }
         for (String presenter : programme.getPresenters()) {
             builderCredits.append(context.getString(R.string.presenter, presenter));
             builderCredits.append('\n');
         }
 
         credits.setText(builderCredits.toString());
         credits.setMovementMethod(new ScrollingMovementMethod());
     }
 
     private static String formatterMots(String motsAFormatter) {
         StringBuilder motsFormattes = new StringBuilder();
         motsFormattes.append(motsAFormatter.substring(0, 1).toUpperCase());
         motsFormattes.append(motsAFormatter.substring(1));
         return motsFormattes.toString();
     }
 
     private final static Map<String, Integer> mapOfCsaRatings = new HashMap<String, Integer>() {{
         put("-18", R.drawable.moins18);
         put("-16", R.drawable.moins16);
         put("-12", R.drawable.moins12);
         put("-10", R.drawable.moins10);
     }};
 
     public static void contructResumeView(Activity context, GetView getView, Programme programme) {
         ImageLoader imageLoader = new ImageLoader(context.getApplicationContext());
 
         ImageView icon = (ImageView) getView.findViewById(R.id.programme_resume_icon);
         ImageView rating = (ImageView) getView.findViewById(R.id.programme_resume_rating);
         ImageView csaRating = (ImageView) getView.findViewById(R.id.programme_resume_csa_rating);
 
         TextView categories = (TextView) getView.findViewById(R.id.programme_resume_categories);
         TextView description = (TextView) getView.findViewById(R.id.programme_resume_description);
 
        if (programme.getIcon() != null && !programme.getIcon().isEmpty()) {
             imageLoader.DisplayImage(programme.getIcon(), icon);
             icon.setVisibility(View.VISIBLE);
         } else {
             icon.setVisibility(View.GONE);
         }
         if (programme.isMovie() || programme.isTvShow()) {
             if (programme.getRatingResource() == null) {
                 new Thread(new RatingLoader(programme, new RunChangeRatingImageOnUiThread(context, rating, programme))).start();
             } else {
                 rating.setImageResource(programme.getRatingResource());
                 rating.setVisibility(View.VISIBLE);
             }
         } else {
             rating.setVisibility(View.GONE);
         }
         Log.d(YboTvApplication.TAG, "CsaRating : " + programme.getCsaRating());
         if (programme.getCsaRating() != null
                 && mapOfCsaRatings.containsKey(programme.getCsaRating())) {
             csaRating.setImageResource(mapOfCsaRatings.get(programme.getCsaRating()));
             csaRating.setVisibility(View.VISIBLE);
         } else {
             csaRating.setVisibility(View.GONE);
         }
 
         if (programme.getCategories().isEmpty()) {
             categories.setVisibility(View.GONE);
         } else {
             categories.setVisibility(View.VISIBLE);
             String categorie = null;
             for (String oneCategorie : programme.getCategories()) {
                 categorie = oneCategorie;
             }
             categories.setText(formatterMots(categorie));
         }
 
         if (programme.getDesc() != null) {
             description.setText(Html.fromHtml(programme.getDesc()));
             description.setVisibility(View.VISIBLE);
         } else {
             description.setVisibility(View.GONE);
         }
 
         description.setMovementMethod(new ScrollingMovementMethod());
     }
 
     @Override
     public void onStart() {
         super.onStart();
         EasyTracker.getInstance().activityStart(this);
     }
 
     @Override
     public void onStop() {
         super.onStop();
         EasyTracker.getInstance().activityStop(this);
     }
 
     private static class ChangeRatingImage implements Runnable {
 
         private ImageView rating;
         private Programme programme;
 
         private ChangeRatingImage(ImageView rating, Programme programme) {
 
             this.rating = rating;
             this.programme = programme;
         }
 
         @Override
         public void run() {
             if (programme.getRatingResource() != null) {
                 rating.setImageResource(programme.getRatingResource());
                 rating.setVisibility(View.VISIBLE);
             }
         }
     }
 
     private static class RunChangeRatingImageOnUiThread implements Runnable {
 
         private Activity context;
         private ImageView rating;
         private Programme programme;
 
         private RunChangeRatingImageOnUiThread(Activity context, ImageView rating, Programme programme) {
             this.context = context;
             this.rating = rating;
             this.programme = programme;
         }
 
         @Override
         public void run() {
             context.runOnUiThread(new ChangeRatingImage(rating, programme));
         }
     }
 
 }
 
