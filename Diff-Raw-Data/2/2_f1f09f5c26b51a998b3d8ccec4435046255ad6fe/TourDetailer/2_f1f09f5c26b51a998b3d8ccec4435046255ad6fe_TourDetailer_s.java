 package edu.vanderbilt.vm.guide.ui;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import edu.vanderbilt.vm.guide.R;
 import edu.vanderbilt.vm.guide.container.Agenda;
 import edu.vanderbilt.vm.guide.db.GuideDBConstants;
 import edu.vanderbilt.vm.guide.db.GuideDBOpenHelper;
 import edu.vanderbilt.vm.guide.ui.adapter.AgendaAdapter;
 import edu.vanderbilt.vm.guide.ui.listener.PlaceListClickListener;
 import edu.vanderbilt.vm.guide.util.DBUtils;
 import edu.vanderbilt.vm.guide.util.GlobalState;
 import edu.vanderbilt.vm.guide.util.GuideConstants;
 
 public class TourDetailer extends Activity {
 
 	public static final String TOUR_ID_EXTRA = "tourId";
 	private static final long NO_ID = -1;
 	private static final Logger logger = LoggerFactory
 			.getLogger("ui.TourDetailer");
 
 	private ActionBar mAction;
 	private Cursor mCursor;
 	private GuideDBOpenHelper mHelper;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_tour_detail);
 
 		// Set up ActionBar
 		mAction = getActionBar();
 		mAction.setTitle("Tour Details");
 		mAction.setDisplayHomeAsUpEnabled(true);
 		mAction.setBackgroundDrawable(GuideConstants.ACTION_BAR_BG);
 
 		mHelper = new GuideDBOpenHelper(this);
 		long tourId = getIntent().getExtras().getLong(TOUR_ID_EXTRA, NO_ID);
 
 		if (tourId == NO_ID) {
 			logger.error("Got an intent with no tour ID");
 			return;
 		}
 
 		mCursor = mHelper.getReadableDatabase().query(
 				GuideDBConstants.TourTable.TOUR_TABLE_NAME, null,
 				GuideDBConstants.TourTable.ID_COL + " = " + tourId, null, null,
 				null, null);
 
 		if (!mCursor.moveToFirst()) {
 			logger.error(
 					"Got an empty cursor for selection of tour with id {}",
 					tourId);
 			return;
 		}
 
 		fillViews(mCursor);
 
 	}
 
 	/**
 	 * Fill in all of the text for the TextViews in the layout and set up the
 	 * ListView
 	 * 
 	 * @param cursor
 	 *            The cursor to get the text from
 	 */
 	private void fillViews(Cursor cursor) {
 		int index = cursor.getColumnIndex(GuideDBConstants.TourTable.NAME_COL);
 		if (index != -1) {
 			String tourName = cursor.getString(index);
 			TextView tourNameView = (TextView) findViewById(R.id.tour_detail_tour_name_tv);
 			tourNameView.setText(tourName);
 		} else {
 			logger.warn("Cursor for tour id {} didn't have a tour name");
 		}
 
 		index = cursor
 				.getColumnIndex(GuideDBConstants.TourTable.DESCRIPTION_COL);
 		if (index != -1) {
 			String tourDesc = cursor.getString(index);
 			TextView tourDescView = (TextView) findViewById(R.id.tour_detail_tour_desc_tv);
 			tourDescView.append(tourDesc);
 		} else {
 			logger.warn("Cursor for tour id {} didn't have a tour description");
 		}
 
 		index = cursor
 				.getColumnIndex(GuideDBConstants.TourTable.TIME_REQUIRED_COL);
 		if (index != -1) {
 			String tourTime = cursor.getString(index);
 			TextView tourTimeView = (TextView) findViewById(R.id.tour_detail_tour_time_tv);
 			tourTimeView.append(tourTime);
 		} else {
 			logger.warn("Cursor for tour id {} didn't have a tour time required");
 		}
 
 		index = cursor.getColumnIndex(GuideDBConstants.TourTable.DISTANCE_COL);
 		if (index != -1) {
 			String tourDist = cursor.getString(index);
 			TextView tourDistView = (TextView) findViewById(R.id.tour_detail_tour_distance_tv);
 			tourDistView.append(tourDist);
 		} else {
 			logger.warn("Cursor for tour id {} didn't have a tour distance");
 		}
 
 		index = cursor
 				.getColumnIndex(GuideDBConstants.TourTable.PLACES_ON_TOUR_COL);
 		if (index != -1) {
 			String placeIds = cursor.getString(index);
 			Agenda agenda = DBUtils.getAgendaFromIds(placeIds,
 					mHelper.getReadableDatabase());
 			ListView listView = (ListView) findViewById(R.id.tour_detail_place_list);
 			listView.setAdapter(new AgendaAdapter(this, agenda));
 			listView.setOnItemClickListener(new PlaceListClickListener(this));
 		} else {
 			logger.warn("Cursor for tour id {} didn't have the places on the tour");
 		}
 	}
 
 	/**
 	 * Use this method to open the Details page
 	 * 
 	 * @param ctx
 	 *            The starting Activity
 	 * @param tourId
 	 *            The Id of the tour that you want to detail
 	 */
 	public static void open(Context ctx, long tourId) {
 		Intent i = new Intent(ctx, TourDetailer.class);
 		i.putExtra(TOUR_ID_EXTRA, tourId);
 		ctx.startActivity(i);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.place_detail_activity, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_add_tour_to_agenda:
 			int index = mCursor
 					.getColumnIndex(GuideDBConstants.TourTable.PLACES_ON_TOUR_COL);
 			if (index == -1) {
 				logger.error("Cursor does not have a places on tour column");
 			} else {
 				String placeIds = mCursor.getString(index);
 				SQLiteDatabase db = mHelper.getReadableDatabase();
 				Agenda tourAgenda = DBUtils.getAgendaFromIds(placeIds, db);
 				GlobalState.getUserAgenda().coalesce(tourAgenda);
 				Toast.makeText(this,
 						"Added all of the places on this tour to your agenda",
 						Toast.LENGTH_LONG).show();
 			}
 			return true;
 		default:
 			logger.warn("Clicked menu item does not match any known ids");
 			return false;
 		}
 	}
 
 }
