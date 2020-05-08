 package ca.charland.tanita;
 
 import android.content.ContentValues;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import ca.charland.tanita.db.TanitaDataSource;
 import ca.charland.tanita.db.TanitaDataTable;
import ca.charland.tanita.manage.PeopleListActivity;
 
 /**
  * The listener interface for receiving nextButtonOnClick events. The class that is interested in processing a nextButtonOnClick event implements this
  * interface, and the object created with that class is registered with a component using the component's
  * <code>addNextButtonOnClickListener<code> method. When
  * the nextButtonOnClick event occurs, that object's appropriate
  * method is invoked.
  * 
  * @see NextButtonOnClickEvent
  * 
  * @author mcharland
  */
 class NextButtonOnClickListener implements View.OnClickListener {
 
	/** The Constant ID which is the unique identifier of a row in the table.. */
 	static final String ID = "ROW_ID";
 
 	/** The data source. */
 	private final TanitaDataSource datasource;
 
 	/** The activity. */
 	private final AbstractBaseActivity activity;
 
 	/**
 	 * Instantiates a new next button on click listener.
 	 * 
 	 * @param activity
 	 *            the activity
 	 */
 	NextButtonOnClickListener(AbstractBaseActivity activity) {
 		this.activity = activity;
 		datasource = new TanitaDataSource(activity);
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void onClick(View v) {
 		datasource.open();
 		ContentValues values = activity.getValues();
 
 		long id = -1;
		int person = 0;
 		Intent intent = activity.getIntent();
 		if (intent != null) {
 			// Don't know how inject extras in testing.
 			Bundle extras = intent.getExtras();
 			if (extras.containsKey(ID)) {
 				id = extras.getLong(ID);
 			}
			person = extras.getInt(PeopleListActivity.PERSON_ID.toString());
 		}
 
 		// Check to see if you've already inserted the row.
 		if (id == -1) {
 			id = datasource.insert(values);
 		} else {
 			datasource.update(TanitaDataTable.Column.ID.toString(), id, values);
 		}
 
		// Create a new intent which bases the row id and the person id.
 		Intent newIntent = new Intent(activity.getBaseContext(), activity.getNextClass());
 		newIntent.putExtra(ID, id);
		newIntent.putExtra(PeopleListActivity.PERSON_ID.toString(), person);
 
 		activity.startActivity(newIntent);
 		datasource.close();
 	}
 }
