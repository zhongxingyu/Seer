 package ch.hsr.sa.radiotour.activities;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.TreeSet;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TableRow;
 import android.widget.TextView;
 import ch.hsr.sa.radiotour.R;
 import ch.hsr.sa.radiotour.adapter.VirtualRankingAdapter;
 import ch.hsr.sa.radiotour.application.RadioTour;
 import ch.hsr.sa.radiotour.domain.BicycleRider;
 import ch.hsr.sa.radiotour.domain.Group;
 import ch.hsr.sa.radiotour.domain.PointOfRace;
 import ch.hsr.sa.radiotour.domain.RiderState;
 import ch.hsr.sa.radiotour.domain.Stage;
 import ch.hsr.sa.radiotour.domain.Team;
 import ch.hsr.sa.radiotour.fragments.AdminFragment;
 import ch.hsr.sa.radiotour.fragments.RaceFragment;
import ch.hsr.sa.radiotour.fragments.SpecialRakingFragment;
 import ch.hsr.sa.radiotour.fragments.VirtualRankingFragment;
 import ch.hsr.sa.radiotour.fragments.interfaces.TimePickerIF;
 import ch.hsr.sa.radiotour.technicalservices.database.DatabaseHelper;
 import ch.hsr.sa.radiotour.technicalservices.importer.CSVReader;
 import ch.hsr.sa.radiotour.views.EditRiderDialog;
 import ch.hsr.sa.radiotour.views.FragmentDialog;
 import ch.hsr.sa.radiotour.views.MarchTableDialog;
 
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 
 public class RadioTourActivity extends Activity implements Observer,
 		OnClickListener {
 
 	private final TreeSet<Integer> checkedIntegers = new TreeSet<Integer>();
 	private final HashSet<TextView> checkedViews = new HashSet<TextView>();
 	private RaceFragment raceFragment;
 	private VirtualRankingFragment rankingFragment;
 	private AdminFragment adminFragment;
	private SpecialRakingFragment specialRankingFragment;
 
 	public TreeSet<Integer> getCheckedIntegers() {
 		return checkedIntegers;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		importDriverandTeams();
 		setContentView(R.layout.base_activity);
 
 		raceFragment = new RaceFragment();
 		FragmentTransaction fragmentTransaction = getFragmentManager()
 				.beginTransaction();
 		fragmentTransaction.add(R.id.changeLayout, raceFragment);
 		fragmentTransaction.commit();
 	}
 
 	public void importDriverandTeams() {
 		RadioTour application = (RadioTour) getApplication();
 		application.getRiders().clear();
 		application.getGroups().clear();
 		application.getTeams().clear();
 
 		for (BicycleRider rider : getHelper().getBicycleRiderDao()
 				.queryForAll()) {
 			((RadioTour) getApplication()).add(rider);
 		}
 		((RadioTour) getApplication()).getGroups().addAll(
 				databaseHelper.getGroupDao().queryForAll());
 		Collections.sort(((RadioTour) getApplication()).getGroups());
 		if (((RadioTour) getApplication()).getRiders().size() <= 0) {
 			CSVReader reader = new CSVReader(getResources().openRawResource(
 					R.raw.startlistebern));
 			Group gr = new Group();
 			gr.setField(true);
 			gr.setOrderNumber(0);
 
 			for (String[] riderAsString : reader.readFile()) {
 				BicycleRider temp = convertStringArrayToRider(riderAsString);
 				((RadioTour) getApplication()).add(temp);
 				gr.getDriverNumbers().add(temp.getStartNr());
 			}
 			databaseHelper.getGroupDao().create(gr);
 
 			reader = new CSVReader(getResources().openRawResource(
 					R.raw.marschtabellebern));
 			Stage stage = new Stage("Lyss", "Lyss");
 			databaseHelper.getStageDao().create(stage);
 			for (String[] pointAsString : reader.readFile()) {
 				PointOfRace point = convertStringArrayToPoint(pointAsString);
 				point.setStage(stage);
 				databaseHelper.getPointOfRaceDao().create(point);
 			}
 		}
 		for (Team team : ((RadioTour) getApplication()).getTeams()) {
 			databaseHelper.getTeamDao().create(team);
 		}
 	}
 
 	private PointOfRace convertStringArrayToPoint(String[] pointAsString) {
 		int altitude = Integer.valueOf(pointAsString[0]);
 		String name = pointAsString[1];
 		double distance = Double.valueOf(pointAsString[2]);
 		Date date = null;
 		try {
 			date = new SimpleDateFormat("HH:mm").parse(pointAsString[3]);
 		} catch (ParseException e) {
 			Log.e(getClass().getSimpleName(), e.getMessage());
 		}
 
 		return new PointOfRace(altitude, distance, name, date);
 	}
 
 	private BicycleRider convertStringArrayToRider(String[] riderAsString) {
 		try {
 			// Berner Rundfahrt Startliste
 			int startNr = Integer.valueOf(riderAsString[0]);
 			String name = riderAsString[1] + " " + riderAsString[2];
 			String team = riderAsString[3];
 			String country = riderAsString[4].substring(0, 3);
 			Date birthday = null;
 			try {
 				birthday = new SimpleDateFormat("yyyyMMdd")
 						.parse(riderAsString[4].substring(3));
 			} catch (ParseException e) {
 				Log.e(getClass().getSimpleName(), e.getMessage());
 			}
 
 			BicycleRider bicycleRider = new BicycleRider();
 			bicycleRider.setStartNr(startNr);
 			bicycleRider.setName(name);
 			bicycleRider.setTeam(team);
 			bicycleRider.setCountry(country);
 			bicycleRider.setBirthday(birthday);
 			bicycleRider.setRiderState(RiderState.ACTIV);
 
 			// BicycleRider bicycleRider = new BicycleRider(
 			// Integer.valueOf(riderAsString[0]), riderAsString[1],
 			// riderAsString[2], riderAsString[3], "");
 
 			databaseHelper.getBicycleRiderDao().create(bicycleRider);
 			return bicycleRider;
 		} catch (NumberFormatException e) {
 			Log.e(getClass().getSimpleName(), e.getMessage());
 		}
 		return null;
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v instanceof TextView) {
 			final TextView temp = (TextView) v;
 			Integer checkedID;
 			try {
 				checkedID = Integer.valueOf(temp.getText().toString());
 			} catch (NumberFormatException e) {
 				Integer indexSpace = temp.getText().toString().indexOf(" ");
 				checkedID = Integer.valueOf(temp.getText().toString()
 						.substring(0, indexSpace));
 			}
 			if (checkedIntegers.contains(checkedID)) {
 				checkedIntegers.remove(checkedID);
 				checkedViews.remove(temp);
 				temp.setBackgroundColor(Color.TRANSPARENT);
 				temp.setTextColor(Color.WHITE);
 
 			} else {
 				checkedIntegers.add(checkedID);
 				checkedViews.add(temp);
 				temp.setBackgroundColor(Color.LTGRAY);
 				temp.setTextColor(Color.BLACK);
 			}
 		}
 	}
 
 	public void onRowLayoutClick(View tableRow, TreeSet<Integer> dragObject) {
 		raceFragment.getGroupFragment().moveDriverNr(tableRow, dragObject);
 		clearCheckedIntegers();
 	}
 
 	public void clearCheckedIntegersOnclick(View v) {
 		clearCheckedIntegers();
 	}
 
 	private DatabaseHelper databaseHelper = null;
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if (databaseHelper != null) {
 			OpenHelperManager.releaseHelper();
 			databaseHelper = null;
 		}
 	}
 
 	public DatabaseHelper getHelper() {
 		if (databaseHelper == null) {
 			databaseHelper = OpenHelperManager.getHelper(this,
 					DatabaseHelper.class);
 		}
 		return databaseHelper;
 	}
 
 	public void ontestButtonClick(View v) {
 		if (!raceFragment.isAdded()) {
 			FragmentTransaction fragmentTransaction = getFragmentManager()
 					.beginTransaction();
 			fragmentTransaction.replace(R.id.changeLayout, raceFragment);
 			fragmentTransaction.commit();
 		}
 	}
 
 	public void ontestButtonClick1(View v) {
 		clearCheckedIntegers();
 		rankingFragment = rankingFragment == null ? new VirtualRankingFragment()
 				: rankingFragment;
 		if (!rankingFragment.isAdded()) {
 			FragmentTransaction fragmentTransaction = getFragmentManager()
 					.beginTransaction();
 			fragmentTransaction.replace(R.id.changeLayout,
 					new VirtualRankingFragment());
 			fragmentTransaction.commit();
 		}
 	}
 
 	public void onAdminButtonClick(View v) {
 		adminFragment = new AdminFragment();
 		FragmentTransaction fragmentTransaction = getFragmentManager()
 				.beginTransaction();
 		fragmentTransaction.replace(R.id.changeLayout, adminFragment);
 		fragmentTransaction.commit();
 
 	}
 
	public void onSpecialButtonClick(View v) {
		specialRankingFragment = new SpecialRakingFragment();
		FragmentTransaction fragmentTransaction = getFragmentManager()
				.beginTransaction();
		fragmentTransaction.replace(R.id.changeLayout, specialRankingFragment);
		fragmentTransaction.commit();

	}

 	private void clearCheckedIntegers() {
 		checkedIntegers.clear();
 		for (TextView v : checkedViews) {
 			v.setBackgroundColor(Color.TRANSPARENT);
 			v.setTextColor(Color.WHITE);
 
 		}
 		checkedViews.clear();
 	}
 
 	public void showTimeDialog(TimePickerIF timePickerIF) {
 		FragmentTransaction ft = getFragmentManager().beginTransaction();
 		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
 		if (prev != null) {
 			ft.remove(prev);
 		}
 		ft.addToBackStack(null);
 
 		FragmentDialog newFragment = new FragmentDialog(timePickerIF);
 		newFragment.show(ft, "dialog");
 
 	}
 
 	public void showRiderDialog(BicycleRider rider,
 			VirtualRankingAdapter adapter) {
 		FragmentTransaction ft = getFragmentManager().beginTransaction();
 		Fragment prev = getFragmentManager().findFragmentByTag("riderDialog");
 		if (prev != null) {
 			ft.remove(prev);
 		}
 		ft.addToBackStack(null);
 
 		EditRiderDialog newFragment = new EditRiderDialog(rider, adapter);
 		newFragment.show(ft, "riderDialog");
 	}
 
 	public void showMarchTableDialog() {
 		FragmentTransaction ft = getFragmentManager().beginTransaction();
 		Fragment prev = getFragmentManager().findFragmentByTag("marchDialog");
 		if (prev != null) {
 			ft.remove(prev);
 		}
 		ft.addToBackStack(null);
 
 		MarchTableDialog newFragment = new MarchTableDialog();
 		newFragment.show(ft, "marchDialog");
 	}
 
 	@Override
 	public void update(Observable observable, Object data) {
 		onRowLayoutClick((TableRow) data, checkedIntegers);
 	}
 
 }
