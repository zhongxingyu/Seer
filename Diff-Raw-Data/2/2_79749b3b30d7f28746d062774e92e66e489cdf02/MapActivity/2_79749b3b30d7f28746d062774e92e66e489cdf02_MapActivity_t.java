 package com.chattermap;
 
 import java.security.spec.MGF1ParameterSpec;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 
 import com.chattermap.entity.Group;
 import com.chattermap.entity.Note;
 import com.orm.androrm.DatabaseAdapter;
 import com.orm.androrm.Model;
 import com.parse.FindCallback;
 import com.parse.GetCallback;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
public class MapActivity extends Activity {
 	Group mCurrentGroup;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.maplayout);
 		setupDB();
 		loadPublicGroup(true);
 	}
 
 	private void saveNote(final String title, final String body, final int lat,
 			final int longit) {
 		Note.create(title, body, lat, longit, mCurrentGroup);
 	}
 
 	private void loadPublicGroup(boolean b) {
 		final ProgressDialog pd = ProgressDialog.show(MapActivity.this,
 				"Loading...", "Working");
 		new AsyncTask<Void, Void, Void>() {
 
 			@Override
 			protected Void doInBackground(Void... params) {
 				try {
 					loadPublicGroup();
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(Void result) {
 				saveNote("Second Note title", "This is our second Note", 0, 0);
 				pd.dismiss();
 			};
 
 		}.execute();
 
 	}
 
 	/**
 	 * Loads the initial public group
 	 * 
 	 * @throws ParseException
 	 */
 	private void loadPublicGroup() throws ParseException {
 		Group mGroup = Group.find("Public", getApplicationContext());
 		if (mGroup == null) {
 			final ParseQuery query = new ParseQuery("Group");
 			query.whereEqualTo("Name", "Public");
 			ParseObject ob = query.getFirst();
 			Log.i("TAG", ob.getObjectId());
 			Log.i("TAG", ob.getString("Name"));
 			Log.i("TAG", ob.getString("Description"));
 
 			Group group = new Group();
 			group.setID(ob.getObjectId());
 			group.setName(ob.getString("Name"));
 			group.setDescription(ob.getString("Description"));
 			// group.save(MapActivity.this);
 			mCurrentGroup = group;
 		}
 
 	}
 
 	/**
 	 * Test for creating public group.
 	 * 
 	 * @throws ParseException
 	 */
 	private void createTestGroup() {
 		// Create public group is not created
 		final ParseQuery query = new ParseQuery("Group");
 		query.whereEqualTo("Name", "Public");
 		query.getFirstInBackground(new GetCallback() {
 
 			@Override
 			public void done(ParseObject object, ParseException e) {
 				if (e == null) {
 
 					Log.i("ChatterMap", "Public Exist");
 				} else {
 					ParseObject publicGroup = new ParseObject("Group");
 					publicGroup.put("Name", "Public");
 					publicGroup.put("Description", "Group For Public Use");
 					publicGroup.saveInBackground();
 					Log.i("ChatterMap", "Public Created");
 				}
 			}
 		});
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.maplayout, menu);
 		return true;
 	}
 
 	/**
 	 * Updates the notes for projects in the databases Call in background only
 	 * 
 	 * @throws ParseException
 	 */
 	public void update() throws ParseException {
 		// Getting the group from the database
 		List<Group> groups = Group.getGroups(this).all().toList();
 
 		for (final Group group : groups) {
 			// Find all notes by the current group
 			final ParseQuery query = new ParseQuery("Note");
 			query.whereEqualTo("objectid", group.getID());
 			query.findInBackground(new FindCallback() {
 
 				@Override
 				public void done(List<ParseObject> objects, ParseException e) {
 					if (e == null) {
 						List<ParseObject> notes = objects;
 						saveNotes(notes, group);
 					}
 				}
 			});
 
 		}
 	}
 
 	/**
 	 * Save notes to a group
 	 * 
 	 * @param notes
 	 *            list of notes
 	 * @param group
 	 *            that the notes belong to
 	 */
 	private void saveNotes(List<ParseObject> notes, Group group) {
 		for (ParseObject po : notes) {
 			Note note = new Note();
 			note.setID(po.getObjectId());
 			note.setGroup(group);
 			note.setTitle(po.getString("Title"));
 			note.setBody(po.getString("Body"));
 		}
 	}
 
 	/**
 	 * Sets up the background database for Groups and Notes
 	 */
 	private void setupDB() {
 		DatabaseAdapter.setDatabaseName("chattermapdb");
 
 		List<Class<? extends Model>> models = new ArrayList<Class<? extends Model>>();
 		models.add(Group.class);
 		models.add(Note.class);
 		DatabaseAdapter adapter = DatabaseAdapter.getInstance(this);
 		adapter.setModels(models);
 	}
 }
