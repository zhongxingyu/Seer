 package com.codingspezis.android.metalonly.player;
 
 import java.text.*;
 import java.util.*;
 import android.annotation.*;
 import android.app.*;
 import android.content.*;
 import android.os.*;
 import android.support.v4.app.*;
 import com.actionbarsherlock.app.*;
 import com.codingspezis.android.metalonly.player.plan.*;
 import com.googlecode.androidannotations.annotations.*;
 import com.googlecode.androidannotations.annotations.res.*;
 
 @EActivity(R.layout.activity_plan)
 @SuppressLint("SimpleDateFormat")
 public class PlanActivity extends SherlockListActivity {
 
 	@StringRes
 	String plan;
 
 	@StringArrayRes
 	String[] days;
 
 	@Extra
 	String site;
 	public static final String KEY_SITE = "site";
 
 	public static final SimpleDateFormat DATE_FORMAT_PARSER = new SimpleDateFormat(
 			"{dd.MM.yy HH:mm"), DATE_FORMAT_TIME = new SimpleDateFormat("HH:mm"),
 			DATE_FORMAT_DATE = new SimpleDateFormat("dd.MM.yy"),
 			DATE_FORMAT_DATE_DAY = new SimpleDateFormat("dd");
 
 	private static final String pattern = "(.*?)_(.*?)_(.*)_(.*)_(.*)";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		getSupportActionBar().setHomeButtonEnabled(true);
 	}
 
 	@AfterInject
 	void afterInject() {
 		setTitle(plan);
 	}
 
 	@AfterViews
 	void afterViews() {
 		ArrayList<PlanData> listEvents = extractEvents(site);
 		ArrayList<Item> listItems = convertToPlan(listEvents);
 		PlanAdapter adapter = new PlanAdapter(this, listItems);
 		getListView().setAdapter(adapter);
 	}
 
 	private ArrayList<Item> convertToPlan(ArrayList<PlanData> listEvents) {
 		ArrayList<Item> listItems = new ArrayList<Item>();
 		Calendar cal = new GregorianCalendar();
 
 		int day = 0;
 
 		SectionItem nextDaySection = new SectionItem(days[day]);
 
 		listItems.add(nextDaySection);
 
 		for (int i = 0; i < listEvents.size(); i++) {
 			PlanData d = listEvents.get(i);
 			listItems.add(new EntryItem(d));
 			if (hasNextListItem(listEvents, i)) {
 				PlanData nextItem = listEvents.get(i + 1);
 				if (notOnSameDay(d, nextItem)) {
 					day++;
					nextDaySection = new SectionItem(days[day]);
 					listItems.add(nextDaySection);
 					int dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7;
 					if (day == dayOfWeek) {
 						final int pos = listItems.size() - 1;
 						getListView().setSelection(pos);
 					}
 				}
 			}
 		}
 		return listItems;
 	}
 
 	private boolean notOnSameDay(PlanData d, PlanData nextItem) {
 		return !d.sameDay(nextItem);
 	}
 
 	private boolean hasNextListItem(ArrayList<PlanData> listEvents, int i) {
 		return i < listEvents.size() - 1;
 	}
 
 	private ArrayList<PlanData> extractEvents(String site) {
 		StringTokenizer tokenizer = new StringTokenizer(site, "}");
 
 		ArrayList<PlanData> listEvents = new ArrayList<PlanData>();
 
 		while (tokenizer.hasMoreTokens()) {
 			String token = tokenizer.nextToken();
 			PlanData planData = convertTokenToPlanEntry(token);
 			if (null != planData) {
 				listEvents.add(planData);
 			}
 		}
 		return listEvents;
 	}
 
 	private PlanData convertTokenToPlanEntry(String token) {
 		try {
 			if (hasModerator(token)) {
 				GregorianCalendar tmpCal = new GregorianCalendar();
 				tmpCal.setTimeInMillis(DATE_FORMAT_PARSER.parse(token.replaceAll(pattern, "$1"))
 						.getTime());
 
 				PlanData planData = new PlanData(token.replaceAll(pattern, "$3"), token.replaceAll(
 						pattern, "$4"), token.replaceAll(pattern, "$5"));
 				planData.setStart(tmpCal);
 				planData.setDuration(Integer.parseInt(token.replaceAll(pattern, "$2")));
 				return planData;
 			}
 		} catch (ParseException e) {
 			// drop entry with wrongly formatted date
 		}
 
 		return null;
 	}
 
 	private boolean hasModerator(String token) {
 		boolean metalHeadIsMod = token.replaceAll(pattern, "$3").equals("MetalHead");
 		boolean hasNoMod = token.replaceAll(pattern, "$3").equals("frei");
 		boolean hasModerator = !(metalHeadIsMod || hasNoMod);
 		return hasModerator;
 	}
 
 	@ItemClick(android.R.id.list)
 	void entryClicked(Object clickedObject) {
		try{
		EntryItem entryItem = (EntryItem) clickedObject;
		PlanData planData = entryItem.getPlanData();
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setItems(R.array.plan_options_array, new PlanEntryClickListener(planData, this));
 		builder.show();
		}
		catch(ClassCastException e){
			// don't need to do stuff
		}
 	}
 
 	@SuppressLint("InlinedApi")
 	@OptionsItem(android.R.id.home)
 	void upButtonClicked() {
 		Intent intent = new Intent(this, MainActivity.class);
 		NavUtils.navigateUpTo(this, intent);
 	}
 
 }
