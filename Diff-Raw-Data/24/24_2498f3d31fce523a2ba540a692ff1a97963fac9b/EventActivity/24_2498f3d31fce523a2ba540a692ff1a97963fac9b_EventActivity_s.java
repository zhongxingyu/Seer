 package dani.leahele.EkspSysApp.Calender;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import dani.leahele.EkspSysApp.Constants;
 import dani.leahele.EkspSysApp.FunActivity;
 import dani.leahele.EkspSysApp.MainActivity;
 import dani.leahele.EkspSysApp.R;
 import dani.leahele.EkspSysApp.Event.Event;
 
 public class EventActivity extends Activity {
 	private Event event;
 
 	private boolean isRegistered = false;
 	private boolean isMaybe = false;
 	private String owner = Constants.OWNER;
 
 	private LinearLayout registered;
 	private LinearLayout maybes;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_event);
 
 		Intent intent = getIntent();
 		event = (Event) intent
 				.getSerializableExtra("dani.leahele.EkspSysApp.event");
 
 		TextView title = (TextView) findViewById(R.id.event_title);
 		TextView date = (TextView) findViewById(R.id.event_date);
 		TextView time = (TextView) findViewById(R.id.event_time);
 		TextView signup = (TextView) findViewById(R.id.event_frist);
 		TextView info = (TextView) findViewById(R.id.event_info);
 
 		title.setText(event.title);
 		date.setText(event.date);
 		time.setText(event.time);
 		signup.setText(event.singupDate);
 		info.setText(event.info);
 
 		registered = (LinearLayout) findViewById(R.id.event_registered);
 		maybes = (LinearLayout) findViewById(R.id.event_registered_maybe);
 
 		addTextViews(registered, event.getRegistered());
 		addTextViews(maybes, event.getMaybeRegistered());
 		
 		isMaybe = event.getRegistered().contains(Constants.OWNER);
 		isRegistered = event.getRegistered().contains(Constants.OWNER);
 		
 		Button b = (Button) findViewById(R.id.event_signup);
 		if(isRegistered){
 			b.setText("Afmeld");
 		}
 		
 	}
 
 	private void addTextViews(LinearLayout ll, List<String> items) {
 		ll.removeAllViews();
 		for (String s : items) {
 			TextView tv = new TextView(this);
 			tv.setText(s);
 			ll.addView(tv);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.event, menu);
 		return true;
 	}
 
 	public void gotoHome(View view) {
 		saveEvent();
 		Intent intent = new Intent(this, MainActivity.class);
 		startActivity(intent);
 	}
 
 	public void gotoCalender(View view) {
 		saveEvent();
 		Intent intent = new Intent(this, CalenderActivity.class);
 		startActivity(intent);
 	}
 
 	public void gotoFun(View view) {
 		saveEvent();
 		Intent intent = new Intent(this, FunActivity.class);
 		startActivity(intent);
 	}
 
 	public void signup(View view) {
 		Button b = (Button) findViewById(R.id.event_signup);
 		if (isRegistered) {
 			if (event.price == 0) {
 				event.unRegister(owner);
 				addTextViews(registered, event.getRegistered());
 				b.setText("Tilmeld");
 			}
 		} else {
 			if (isMaybe) {
 				event.unMaybeRegister(owner);
 				addTextViews(maybes, event.getMaybeRegistered());
 				isMaybe = !isMaybe;
 			}
 			event.register(owner);
 			addTextViews(registered, event.getRegistered());
 			b.setText("Afmeld");
 		}
 		isRegistered = !isRegistered;
 		
 		event.isMaybe = isMaybe;
 		event.isRegistered = isRegistered;
 		saveEvent();
 	}
 
 	public void signupMaybe(View view) {
 		Button r = (Button) findViewById(R.id.event_signup);
 		if (isMaybe) {
 			event.unMaybeRegister(owner);
 			addTextViews(maybes, event.getMaybeRegistered());
 		} else {
 			if (!isRegistered) {
 				event.maybeRegister(owner);
 				addTextViews(maybes, event.getMaybeRegistered());
 			} else if (event.price == 0) {
 				event.unRegister(owner);
 				addTextViews(registered, event.getRegistered());
 				r.setText("Tilmeld");
 				isRegistered = !isRegistered;
 
 				event.maybeRegister(owner);
 				addTextViews(maybes, event.getMaybeRegistered());
 			}
 		}
 		isMaybe = !isMaybe;
 
 		event.isMaybe = isMaybe;
 		event.isRegistered = isRegistered;
 		saveEvent();
 	}
 
 	private void saveEvent() {
 		System.out.println("!!!!! Event " + event.title + ": " + event.getRegistered());
 		
 		// Create file
 		File fileDir = getFilesDir();
 		
 		System.out.println("!!!!! files : " + fileDir.listFiles().length);
 		
 		File[] files = fileDir.listFiles(new FilenameFilter() {
 			
 			@Override
 			public boolean accept(File dir, String filename) {
 				return filename.equals(event.title);
 			}
 		});
 		if (files.length != 0) {
 			System.out.println("!!!! No such file");
 			for(File f : files){
 				f.delete();
 			}
 		}
 		
 		File file = new File(fileDir, event.title);
 
 		// Save recipe in file
 		FileOutputStream fout;
 		try {
 			fout = new FileOutputStream(file);
 			ObjectOutputStream out = new ObjectOutputStream(fout);
 			out.writeObject(event);
 			out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
