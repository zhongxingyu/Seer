 package de.fhb.mobile.ToDoListAndroidApp;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import de.fhb.mobile.ToDoListAndroidApp.commons.AndroidContactsHelper;
 import de.fhb.mobile.ToDoListAndroidApp.communication.IServerCommunicationREST;
 import de.fhb.mobile.ToDoListAndroidApp.communication.ServerCommunicationREST;
 import de.fhb.mobile.ToDoListAndroidApp.models.Contact;
 import de.fhb.mobile.ToDoListAndroidApp.models.Todo;
 import de.fhb.mobile.ToDoListAndroidApp.persistance.TodoDatabase;
 import de.fhb.mobile.ToDoListAndroidApp.persistance.TodoTable;
 
 /**
  * The Class ToDoListActivity.
  */
 public class ToDoListActivity extends ListActivity {
 
 	/** The Constant REQUEST_CODE_TODODETAILS. */
 	public static final int REQUEST_CODE_TODODETAILS = 1;
 
 	/** The Constant REQUEST_CODE_ALLCONTACTS. */
 	public static final int REQUEST_CODE_ALLCONTACTS = 2;
 
 	/** The Constant SORTING_FAVORITE_DATE. */
 	private static final int SORTING_FAVORITE_DATE = 20;
 
 	/** The Constant SORTING_DATE_FAVORITE. */
 	private static final int SORTING_DATE_FAVORITE = 30;
 
 	/** The Constant MODE_ALLTODOS. */
 	private static final int MODE_ALLTODOS = 1000;
 
 	/** The Constant MODE_ALLTODOS_FOR_CONTACT. */
 	private static final int MODE_ALLTODOS_FOR_CONTACT = 2000;
 
 	/** The Constant ARG_TODO_ID. */
 	public static final String ARG_TODO_ID = "todoObjectID";
 
 	/** The db. */
 	private TodoDatabase db;
 	
 	private IServerCommunicationREST server;
 
 	/** The sorting. */
 	private int sorting;
 
 	/** The mode. */
 	private int mode;
 
 	/** The contact. */
 	private Contact contact;
 
 	// View
 	/** The spinner_sortby. */
 	private Spinner spinner_sortby;
 
 	/** The headline. */
 	private TextView headline;
 
 	/**
 	 * Called when the activity is first created.
 	 * 
 	 * @param savedInstanceState
 	 *            the saved instance state
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.todolist);
 		Log.i(this.getClass().toString(), "onCreate");
 		
 		boolean serverConnection = getIntent().getBooleanExtra(LoginActivity.ARG_SERVER_CONNECTION, true);
 		
 		mode = MODE_ALLTODOS;
 		headline = (TextView) findViewById(R.id.alltodos_activityname);
 		initSortSpinner();
 		db = new TodoDatabase(this);
 		db.open();
 		
 		if(serverConnection){
 			Log.i(this.getClass().toString(), "Synchronize");
 			server = new ServerCommunicationREST();
 			// TODO sync meldung an user ausgeben
			server.synchronize();
 			
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onPause()
 	 */
 	@Override
 	public void onPause() {
 		Log.i(this.getClass().toString(), "onPause");
 		super.onPause();
 		db.close();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onStop()
 	 */
 	@Override
 	public void onStop() {
 		Log.i(this.getClass().toString(), "onStop");
 		super.onStop();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onResume()
 	 */
 	@Override
 	public void onResume() {
 		Log.i(this.getClass().toString(), "onResume");
 		super.onResume();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onRestart()
 	 */
 	@Override
 	public void onRestart() {
 		Log.i(this.getClass().toString(), "onRestart");
 		super.onRestart();
 		db.open();
 		initListAdapter(sorting);
 	}
 
 	/**
 	 * Inits the sort spinner.
 	 */
 	private void initSortSpinner() {
 		Log.i(this.getClass().toString(), "initSortSpinner()");
 		spinner_sortby = (Spinner) findViewById(R.id.spinner_orderby);
 		CharSequence[] objects = { "sort by important + date",
 				"sort by date + important" };
 		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
 				this, android.R.layout.simple_spinner_item, objects);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner_sortby.setAdapter(adapter);
 		spinner_sortby
 				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 
 					@Override
 					public void onItemSelected(AdapterView<?> parent,
 							View view, int pos, long id) {
 						Log.i(this.getClass().toString(),
 								"Sortspinner select: pos= " + pos);
 						switch (pos) {
 						case 0:
 							initListAdapter(SORTING_FAVORITE_DATE);
 							break;
 						case 1:
 							initListAdapter(SORTING_DATE_FAVORITE);
 							break;
 						}
 					}
 
 					@Override
 					public void onNothingSelected(AdapterView<?> parent) {
 					}
 				});
 	}
 
 	/**
 	 * Inits the list adapter.
 	 * 
 	 * @param sorting
 	 *            the sorting
 	 */
 	private void initListAdapter(int sorting) {
 		Log.i(this.getClass().toString(), "initListAdapter()");
 		this.sorting = sorting;
 
 		switch (mode) {
 		case MODE_ALLTODOS:
 			initListAdapterForAllTodos();
 			break;
 		case MODE_ALLTODOS_FOR_CONTACT:
 			initListAdapterForAllTodosForContact();
 			break;
 		}
 
 	}
 
 	/**
 	 * Inits the list adapter for all todos for contact.
 	 */
 	private void initListAdapterForAllTodosForContact() {
 		Log.i(this.getClass().toString(),
 				"initListAdapterForAllTodosForContact() - "
 						+ contact.getDisplayName());
 		List<Todo> list = new ArrayList<Todo>(0);
 
 		headline.setText("All Todos for " + contact.getDisplayName());
 
 		switch (sorting) {
 		case SORTING_FAVORITE_DATE:
 			list = db.getAllTodosForContact(TodoTable.KEY_FINISHED + " DESC, "
 					+ TodoTable.KEY_FAVORITE + " DESC, "
 					+ TodoTable.KEY_EXPIREDATE + " ASC", contact.getId());
 			break;
 		case SORTING_DATE_FAVORITE:
 			list = db.getAllTodosForContact(TodoTable.KEY_FINISHED + " DESC, "
 					+ TodoTable.KEY_EXPIREDATE + " ASC, "
 					+ TodoTable.KEY_FAVORITE + " DESC", contact.getId());
 			break;
 		default:
 			list = db.getAllTodosForContact(TodoTable.KEY_FINISHED + " DESC",
 					contact.getId());
 		}
 		ArrayAdapter<Todo> sadapter = new TodoAdapter(this,
 				R.layout.listelement, list);
 		// setze den Adapter auf die Listenansicht
 		this.setListAdapter(sadapter);
 	}
 
 	/**
 	 * Inits the list adapter for all todos.
 	 */
 	private void initListAdapterForAllTodos() {
 		Log.i(this.getClass().toString(), "initListAdapterForAllTodos()");
 		List<Todo> list = new ArrayList<Todo>(0);
 
 		headline.setText("All Todos");
 
 		switch (sorting) {
 		case SORTING_FAVORITE_DATE:
 			list = db.getAllTodos(TodoTable.KEY_FINISHED + " DESC, "
 					+ TodoTable.KEY_FAVORITE + " DESC, "
 					+ TodoTable.KEY_EXPIREDATE + " ASC");
 			break;
 		case SORTING_DATE_FAVORITE:
 			list = db.getAllTodos(TodoTable.KEY_FINISHED + " DESC, "
 					+ TodoTable.KEY_EXPIREDATE + " ASC, "
 					+ TodoTable.KEY_FAVORITE + " DESC");
 			break;
 		default:
 			list = db.getAllTodos(TodoTable.KEY_FINISHED + " DESC");
 		}
 		ArrayAdapter<Todo> sadapter = new TodoAdapter(this,
 				R.layout.listelement, list);
 		// setze den Adapter auf die Listenansicht
 		this.setListAdapter(sadapter);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView,
 	 * android.view.View, int, long)
 	 */
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		Log.i(this.getClass().toString(), "onListItemClick: " + position);
 		Intent intent = new Intent(ToDoListActivity.this,
 				ToDoDetailsActivity.class);
 		Todo todo = (Todo) l.getItemAtPosition(position);
 		Log.i(this.getClass().toString(),
 				"onListItemClick: Todo_id " + todo.getId());
 		// pass the item to the intent
 		intent.putExtra(ARG_TODO_ID, todo.getId());
 		// start the details activity with the intent
 		startActivityForResult(intent, REQUEST_CODE_TODODETAILS);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		Log.i(this.getClass().toString(), "onCreateOptionsMenu");
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onActivityResult(int, int,
 	 * android.content.Intent)
 	 */
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.i(this.getClass().toString(), "onActivityResult");
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (requestCode) {
 		case (REQUEST_CODE_ALLCONTACTS):
 			if (resultCode == Activity.RESULT_OK) {
 				long contactId = data.getLongExtra(
 						ShowContactsActivity.ARG_CONTACT_ID, -1);
 				mode = MODE_ALLTODOS_FOR_CONTACT;
 				contact = AndroidContactsHelper.getContact(
 						getContentResolver(), contactId);
 			}
 			break;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		Log.i(this.getClass().toString(), "onPrepareOptionsMenu");
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.menu_item_create_new_todo:
 			startCreateActivity();
 			return true;
 		case R.id.menu_item_all_todos:
 			initListAdapterForAllTodos();
 			return true;
 		case R.id.menu_item_all_contacts_with_todos:
 			startAllContactActivity();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/**
 	 * Start all contact activity.
 	 */
 	private void startAllContactActivity() {
 		Log.i(this.getClass().toString(), "startAllContactActivity()");
 		Intent intent = new Intent(ToDoListActivity.this,
 				ShowContactsActivity.class);
 		startActivityForResult(intent, REQUEST_CODE_ALLCONTACTS);
 	}
 
 	/**
 	 * Start create activity.
 	 */
 	private void startCreateActivity() {
 		Log.i(this.getClass().toString(), "startCreateActivity()");
 		Intent intent = new Intent(ToDoListActivity.this,
 				ToDoDetailsActivity.class);
 		// start the details activity with the intent
 		startActivityForResult(intent, REQUEST_CODE_TODODETAILS);
 	}
 
 	/**
 	 * The Class TodoAdapter.
 	 */
 	private class TodoAdapter extends ArrayAdapter<Todo> {
 
 		/** The m context. */
 		private final Context mContext;
 
 		/** The m layout. */
 		private final int mLayout;
 
 		/** The m layout inflater. */
 		private final LayoutInflater mLayoutInflater;
 
 		/** The list. */
 		private List<Todo> list;
 
 		/**
 		 * The Class ViewHolder.
 		 */
 		private final class ViewHolder {
 
 			/** The hidden id. */
 			public TextView hiddenID;
 
 			/** The finished check box. */
 			public CheckBox finishedCheckBox;
 
 			/** The favorite check box. */
 			public CheckBox favoriteCheckBox;
 
 			/** The todo name. */
 			public TextView todoName;
 
 			/** The expiredate. */
 			public TextView expiredate;
 
 			/** The delete button. */
 			public ImageButton deleteButton;
 		}
 
 		/**
 		 * Instantiates a new todo adapter.
 		 * 
 		 * @param context
 		 *            the context
 		 * @param layout
 		 *            the layout
 		 * @param list
 		 *            the list
 		 */
 		public TodoAdapter(Context context, int layout, List<Todo> list) {
 			super(context, layout, list);
 			this.mContext = context;
 			this.mLayout = layout;
 			this.list = list;
 			this.mLayoutInflater = LayoutInflater.from(mContext);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
 		 * android.view.ViewGroup)
 		 */
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			Log.i(this.getClass().toString(), "getView postion: " + position);
 			ViewHolder viewHolder;
 			Todo todo = list.get(position);
 			if (convertView == null) {
 				convertView = mLayoutInflater.inflate(mLayout, null);
 
 				viewHolder = new ViewHolder();
 				viewHolder.hiddenID = (TextView) convertView
 						.findViewById(R.id.list_hiddenid);
 				viewHolder.finishedCheckBox = (CheckBox) convertView
 						.findViewById(R.id.list_finished);
 				viewHolder.favoriteCheckBox = (CheckBox) convertView
 						.findViewById(R.id.list_favorite);
 				viewHolder.todoName = (TextView) convertView
 						.findViewById(R.id.list_todoname);
 				viewHolder.expiredate = (TextView) convertView
 						.findViewById(R.id.list_expiredate);
 				viewHolder.deleteButton = (ImageButton) convertView
 						.findViewById(R.id.list_delete_button);
 				convertView.setTag(viewHolder);
 			} else {
 				viewHolder = (ViewHolder) convertView.getTag();
 			}
 			long id = todo.getId();
 			boolean finished = todo.isFinished();
 			boolean favorite = todo.isFavorite();
 			String name = todo.getName();
 			Calendar expiredate = todo.getExpireDate();
 			if (expiredate != null) {
 				Calendar calNow = Calendar.getInstance();
 				if (expiredate.compareTo(calNow) == -1) {
 					viewHolder.expiredate.setBackgroundColor(Color.rgb(255,
 							255, 0));
 					viewHolder.expiredate.setTextColor(Color.BLACK);
 				} else {
 					viewHolder.expiredate.setTextColor(Color.LTGRAY);
 					viewHolder.expiredate
 							.setBackgroundColor(android.R.color.transparent);
 				}
 				viewHolder.expiredate.setText(todo.getExpireDateAsString());
 			} else {
 				viewHolder.expiredate.setText("");
 			}
 			viewHolder.hiddenID.setText(String.valueOf(id));
 			viewHolder.finishedCheckBox.setOnCheckedChangeListener(null);
 			viewHolder.finishedCheckBox.setChecked(finished);
 			viewHolder.finishedCheckBox
 					.setOnCheckedChangeListener(new UpdateDBOnCheckedChangeListener(
 							id, TodoTable.KEY_FINISHED));
 
 			viewHolder.favoriteCheckBox.setOnCheckedChangeListener(null);
 			viewHolder.favoriteCheckBox.setChecked(favorite);
 			viewHolder.favoriteCheckBox
 					.setOnCheckedChangeListener(new UpdateDBOnCheckedChangeListener(
 							id, TodoTable.KEY_FAVORITE));
 
 			viewHolder.todoName.setText(name);
 
 			viewHolder.deleteButton.setOnClickListener(null);
 			viewHolder.deleteButton
 					.setOnClickListener(new DeleteOnClickListener(id));
 			viewHolder.deleteButton.setFocusable(false);
 			return convertView;
 		}
 	}
 
 	/**
 	 * The listener interface for receiving updateDBOnCheckedChange events. The
 	 * class that is interested in processing a updateDBOnCheckedChange event
 	 * implements this interface, and the object created with that class is
 	 * registered with a component using the component's
 	 * <code>addUpdateDBOnCheckedChangeListener<code> method. When
 	 * the updateDBOnCheckedChange event occurs, that object's appropriate
 	 * method is invoked.
 	 * 
 	 * @see UpdateDBOnCheckedChangeEvent
 	 */
 	private class UpdateDBOnCheckedChangeListener implements
 			CompoundButton.OnCheckedChangeListener {
 
 		/** The id. */
 		private long id;
 
 		/** The column. */
 		private String column;
 
 		/**
 		 * Instantiates a new update db on checked change listener.
 		 * 
 		 * @param id
 		 *            the id
 		 * @param column
 		 *            the column
 		 */
 		public UpdateDBOnCheckedChangeListener(long id, String column) {
 			this.id = id;
 			this.column = column;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged
 		 * (android.widget.CompoundButton, boolean)
 		 */
 		@Override
 		public void onCheckedChanged(CompoundButton buttonView,
 				boolean isChecked) {
 			Log.i(this.getClass().toString(),
 					"UpdateDBOnCheckedChanged "
 							+ db.updateBooleanColumns(id, column, isChecked));
 		}
 	}
 
 	/**
 	 * The listener interface for receiving deleteOnClick events. The class that
 	 * is interested in processing a deleteOnClick event implements this
 	 * interface, and the object created with that class is registered with a
 	 * component using the component's
 	 * <code>addDeleteOnClickListener<code> method. When
 	 * the deleteOnClick event occurs, that object's appropriate
 	 * method is invoked.
 	 * 
 	 * @see DeleteOnClickEvent
 	 */
 	private class DeleteOnClickListener implements OnClickListener {
 
 		/** The id. */
 		long id;
 
 		/**
 		 * Instantiates a new delete on click listener.
 		 * 
 		 * @param id
 		 *            the id
 		 */
 		public DeleteOnClickListener(long id) {
 			this.id = id;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see android.view.View.OnClickListener#onClick(android.view.View)
 		 */
 		@Override
 		public void onClick(View v) {
 			db.deleteTodo(id);
 			initListAdapter(sorting);
 		}
 
 	}
 }
