 package interdroid.vdb.avro.view;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.avro.Schema;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import interdroid.vdb.R;
 import interdroid.vdb.avro.control.AvroController;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class AvroBaseEditor extends Activity {
 	private static final Logger logger = LoggerFactory.getLogger(AvroBaseEditor.class);
 
 	public static final String ACTION_EDIT_SCHEMA = "interdroid.vdb.sm.action.EDIT_SCHEMA";
 
 	// Enums we may need dialogs for
 	Map<Integer, Schema> enums = new HashMap<Integer, Schema>();
 
 	// Menu options.
 	private static final int REVERT_ID = Menu.FIRST;
 	private static final int DISCARD_ID = Menu.FIRST + 1;
 	private static final int DELETE_ID = Menu.FIRST + 2;
 
 	public static final String SCHEMA = "schema";
 
 	public static final int REQUEST_RECORD_SELECTION = 1;
 
 	private AvroController mController;
 
 	private AsyncTask<Object, Void, Void> mInit;
 
 	public AvroBaseEditor() {
		logger.debug("Constructed AvroBaseEditor: " + this + ":" + mController);
 	}
 
 	protected AvroBaseEditor(Schema schema) {
 		this(schema, null);
 	}
 
 	public AvroBaseEditor(Schema schema, Uri defaultUri) {
 		this();
 		if (defaultUri == null) {
 			logger.debug("Using default URI.");
 			defaultUri = Uri.parse("content://" + schema.getNamespace() + "/branches/master/"+ schema.getName());
 		}
 		mController = new AvroController(this, schema.getName(), defaultUri, schema);
 		logger.debug("Set controller for schema: " + schema.getName() + " : " + defaultUri + " : " + schema);
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		logger.debug("onCreate: " + this);
 
 		mInit = new InitTask().execute(this, savedInstanceState);
 	}
 
 	private class LoadTask extends AsyncTask<Object, Void, Void> {
 		private ProgressDialog d;
 
 		protected void onPreExecute() {
 			try {
 				logger.debug("Waiting for Controller...");
 				AvroBaseEditor.this.mInit.get();
 				logger.debug("Controller built.");
 			} catch (Exception e) {
				// Ignored
			}
 
 			d = ProgressDialog.show(AvroBaseEditor.this, "Loading..", "Loading...", true, false);
 		}
 
 		protected void onPostExecute(Void v) {
 
 			// Modify our overall title depending on the mode we are running in.
 			if (mController.getState() == AvroController.STATE_EDIT) {
 				AvroBaseEditor.this.setTitle(getText(R.string.title_edit) + " " + mController.getTypeName());
 			} else if (mController.getState() == AvroController.STATE_INSERT) {
 				AvroBaseEditor.this.setTitle(getText(R.string.title_create) + " " + mController.getTypeName());
 			}
 
 			d.dismiss();
 		}
 
 		@Override
 		protected Void doInBackground(Object... params) {
 			if (AvroBaseEditor.this.mInit != null) {
 				try {
 					logger.debug("Waiting for UI to finish loading");
 
 					AvroBaseEditor.this.mInit.get();
 					AvroBaseEditor.this.mInit = null;
 				//	d.dismiss();
 					logger.debug("UI Loaded");
 				} catch (Exception e) {
 					logger.error("Error with init task", e);
 					finish();
 				}
 			}
 
 			mController.loadData();
 			return null;
 		}
 
 	}
 
 	private class InitTask extends AsyncTask<Object, Void, Void> {
 		private ProgressDialog d;
 
 		protected void onPreExecute() {
 			d = ProgressDialog.show(AvroBaseEditor.this, "Loading..", "Building User Interface...", true, false);
 		}
 
 		protected void onPostExecute(Void v) {
 			d.dismiss();
 		}
 
 		@Override
 		protected Void doInBackground(Object... params) {
 
 			final Intent intent = getIntent();
 			final AvroBaseEditor baseEditor = ((AvroBaseEditor)params[0]);
 
 			// If we don't have a controller, then create one...
 			if (mController == null) {
 				Uri defaultUri = intent.getData();
 				if (defaultUri == null) {
 					throw new IllegalArgumentException("A Uri is required.");
 				}
 				String schemaJson = intent.getStringExtra(SCHEMA);
 				if (schemaJson == null) {
 					throw new IllegalArgumentException("A Schema is required.");
 				}
 				Schema schema = Schema.parse(schemaJson);
 				logger.debug("Building controller for: " + schema.getName() + " : " + defaultUri);
 
 				mController = new AvroController(baseEditor, schema.getName(), defaultUri, schema);
 				logger.debug("Controller built: {}", mController);
 			}
 
 			final Uri editUri = baseEditor.mController.setup(intent, (Bundle) params[1]);
 
 			if (editUri == null) {
 				logger.debug("No edit URI built.");
 				baseEditor.finish();
 			} else {
 				// Everything was setup properly so assume the result will work.
 				setResult(RESULT_OK, (new Intent()).setAction(editUri.toString()));
 			}
 
 			return null;
 		}
 
 	}
 
 	protected void onPause() {
 		super.onPause();
 
 		logger.debug("onPause");
 
 		mController.handleSave();
 	}
 
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		logger.debug("onResume");
 
 		// Are we come backing from a for result task?
 		logger.debug("Loading Data");
 
 		new LoadTask().execute(mController);
 
 		logger.debug("Ready");
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		logger.debug("onSaveInstanceState");
 		mController.saveState(outState);
 	}
 
 	public void onStop() {
 		super.onStop();
 		logger.debug("onStop");
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		logger.debug("onDestroy");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 
 		// Build the menus that are shown when editing.
 		if (mController.getState() == AvroController.STATE_EDIT) {
 			menu.add(0, REVERT_ID, 0, R.string.menu_revert)
 			.setShortcut('0', 'r')
 			.setIcon(android.R.drawable.ic_menu_revert);
 			menu.add(0, DELETE_ID, 0, R.string.menu_delete)
 			.setShortcut('1', 'd')
 			.setIcon(android.R.drawable.ic_menu_delete);
 			// Build the menus that are shown when inserting.
 		} else {
 			menu.add(0, DISCARD_ID, 0, R.string.menu_discard)
 			.setShortcut('0', 'd')
 			.setIcon(android.R.drawable.ic_menu_delete);
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle all of the possible menu actions.
 		switch (item.getItemId()) {
 		case DELETE_ID:
 			mController.handleDelete();
 			finish();
 			break;
 		case DISCARD_ID:
 		case REVERT_ID:
 			mController.handleCancel();
 			setResult(RESULT_CANCELED);
 			finish();
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void registerEnum(int hashCode, Schema schema) {
 		enums.put(hashCode, schema);
 	}
 
 	public void launchIntent(Intent editIntent) {
 		editIntent.addCategory(Intent.CATEGORY_DEFAULT);
 		editIntent.setClassName(this.getPackageName(), AvroBaseEditor.class.getName());
 		startActivity(editIntent);
 	}
 }
