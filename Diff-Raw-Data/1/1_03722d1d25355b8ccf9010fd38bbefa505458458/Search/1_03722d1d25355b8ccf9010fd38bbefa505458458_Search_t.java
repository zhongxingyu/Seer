 package ca.projectkarma.gradetrackr.activity;
 
 import android.app.Activity;
 import android.app.LoaderManager;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.CursorLoader;
 import android.content.Intent;
 import android.content.Loader;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.TextView;
 
 import ca.projectkarma.gradetrackr.EditMode;
 import ca.projectkarma.gradetrackr.adapter.SearchCursorAdapter;
 import ca.projectkarma.gradetrackr.db.adapter.CategoriesDBAdapter;
 import ca.projectkarma.gradetrackr.db.adapter.CoursesDBAdapter;
 import ca.projectkarma.gradetrackr.db.adapter.EvaluationsDBAdapter;
 import ca.projectkarma.gradetrackr.db.adapter.TaskDBAdapter;
 
 import ca.projectkarma.gradetrackr.R;
 
 public class Search extends Activity implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {
 	
 	private SearchCursorAdapter coursesSearchCursorAdapter;
 	private SearchCursorAdapter catsSearchCursorAdapter;
 	private SearchCursorAdapter evalsSearchCursorAdapter;
 	private SearchCursorAdapter taskSearchCursorAdapter;
 	
 	private SearchView searchView;
 	
 	private ListView coursesListView;
 	private ListView catsListView;
 	private ListView evalsListView;
 	private ListView taskListView;
 	
 	private TextView coursesTextView;
 	private TextView catsTextView;
 	private TextView evalsTextView;
 	private TextView taskTextView;
 	
 	private static final int COURSES_LOADER_ID = 0;
 	private static final int CATS_LOADER_ID = 1;
 	private static final int EVALS_LOADER_ID = 2;
 	private static final int TASK_LOADER_ID = 3;
 	
 	private String[] coursesFrom = new String[] { CoursesDBAdapter.COURSE_TITLE };
 	private String[] catsFrom = new String[] { CategoriesDBAdapter.CAT_TITLE };
 	private String[] evalsFrom = new String[] { EvaluationsDBAdapter.EVAL_TITLE };
 	private String[] taskFrom = new String[] { TaskDBAdapter.TASK_TITLE };
 	private int[] to = new int[] { R.id.activity_search_listitem_text };
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_search);
 		
         // up button in action bar
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.linen));
        getActionBar().setTitle("");
         
         coursesSearchCursorAdapter = new SearchCursorAdapter(this, R.layout.activity_search_listitem, null, coursesFrom, to, 0);
         catsSearchCursorAdapter = new SearchCursorAdapter(this, R.layout.activity_search_listitem, null, catsFrom, to, 0);
         evalsSearchCursorAdapter = new SearchCursorAdapter(this, R.layout.activity_search_listitem, null, evalsFrom, to, 0);
         taskSearchCursorAdapter = new SearchCursorAdapter(this, R.layout.activity_search_listitem, null, taskFrom, to, 0);
         
         coursesListView = (ListView) findViewById(R.id.activity_search_courses_listview);
         catsListView = (ListView) findViewById(R.id.activity_search_categories_listview);
         evalsListView = (ListView) findViewById(R.id.activity_search_evaluations_listview);
         taskListView = (ListView) findViewById(R.id.activity_search_task_listview);
         
         coursesTextView = (TextView) findViewById(R.id.activity_search_courses_textview);
         catsTextView = (TextView) findViewById(R.id.activity_search_categories_textview);
         evalsTextView = (TextView) findViewById(R.id.activity_search_evaluations_textview);
         taskTextView = (TextView) findViewById(R.id.activity_search_task_textview);
         
         coursesListView.setAdapter(coursesSearchCursorAdapter);
         catsListView.setAdapter(catsSearchCursorAdapter);
         evalsListView.setAdapter(evalsSearchCursorAdapter);
         taskListView.setAdapter(taskSearchCursorAdapter);
 
 	}
 	
     @Override
     protected void onNewIntent(Intent intent) {
         setIntent(intent);
         handleIntent(intent);
     }
 
     private void handleIntent(Intent intent) {
         if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
           String query = intent.getStringExtra(SearchManager.QUERY);
           System.out.println("Query is " + query);
         }
     }
     
     @Override
     public void onRestart() {
     	super.onRestart();
 		getLoaderManager().restartLoader(COURSES_LOADER_ID, null, this);
 		getLoaderManager().restartLoader(CATS_LOADER_ID, null, this);
 		getLoaderManager().restartLoader(EVALS_LOADER_ID, null, this);
 		getLoaderManager().restartLoader(TASK_LOADER_ID, null, this);
     }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_search, menu);
 		
         // Get the SearchView and set the searchable configuration
         SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
         // Assumes current activity is the searchable activity
         searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
         searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
         searchView.setFocusable(true);
         searchView.requestFocusFromTouch();
 
         getLoaderManager().initLoader(COURSES_LOADER_ID, null, this);
         getLoaderManager().initLoader(CATS_LOADER_ID, null, this);
         getLoaderManager().initLoader(EVALS_LOADER_ID, null, this);
         getLoaderManager().initLoader(TASK_LOADER_ID, null, this);
 
         searchView.setOnQueryTextListener(this);
 		
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId())
         {
             case android.R.id.home:
             	finish(); //Simply pop Search activity off call stack
         }
         return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		if (id == COURSES_LOADER_ID) {
 			
 			Uri uri = Uri.withAppendedPath(CoursesDBAdapter.SUGGEST_URI, searchView.getQuery().toString());
 			return new CursorLoader(this, uri, null, null, null, null);
 			
 		} else if (id == CATS_LOADER_ID) {
 			
 			Uri uri = Uri.withAppendedPath(CategoriesDBAdapter.SUGGEST_URI, searchView.getQuery().toString());			
 			return new CursorLoader(this, uri, null, null, null, null);
 			
 		} else if (id == EVALS_LOADER_ID) {
 			
 			Uri uri = Uri.withAppendedPath(EvaluationsDBAdapter.SUGGEST_URI, searchView.getQuery().toString());			
 			return new CursorLoader(this, uri, null, null, null, null);
 			
 		} else if (id == TASK_LOADER_ID) {
 			
 			Uri uri = Uri.withAppendedPath(TaskDBAdapter.SUGGEST_URI, searchView.getQuery().toString());			
 			return new CursorLoader(this, uri, null, null, null, null);
 			
 		}
 		return null;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
 		if (loader.getId() == COURSES_LOADER_ID) {
 			coursesSearchCursorAdapter.swapCursor(cursor);
 			if (cursor != null && cursor.getCount() > 0) {
 				coursesTextView.setVisibility(View.VISIBLE);
 				coursesListView.setOnItemClickListener(new OnItemClickListener() {
 
 					@Override
 					public void onItemClick(AdapterView<?> parent, View view,
 							int position, long id) {
 						if (cursor.moveToPosition(position)) {
 							Intent intent = new Intent(Search.this, Categories.class);
 							intent.putExtra("refID_Term", cursor.getInt(cursor.getColumnIndex(CoursesDBAdapter.TERM_REFERENCE)));
 							intent.putExtra("refID_Course", cursor.getInt(cursor.getColumnIndex(CoursesDBAdapter.ROW_ID)));
 							startActivity(intent);	
 						}
 					}
 					
 				});
 			}
 		}
 		else if (loader.getId() == CATS_LOADER_ID) {
 			catsSearchCursorAdapter.swapCursor(cursor);
 			if (cursor != null && cursor.getCount() > 0) {
 				catsTextView.setVisibility(View.VISIBLE);
 				catsListView.setOnItemClickListener(new OnItemClickListener() {
 
 					@Override
 					public void onItemClick(AdapterView<?> parent, View view,
 							int position, long id) {
 						if (cursor.moveToPosition(position)) {
 							Intent intent = new Intent(Search.this, Evaluations.class);
 							intent.putExtra("refID_Term", cursor.getInt(cursor.getColumnIndex(CategoriesDBAdapter.TERM_REFERENCE)));
 							intent.putExtra("refID_Course", cursor.getInt(cursor.getColumnIndex(CategoriesDBAdapter.COURSE_REFERENCE)));
 							intent.putExtra("refID_Category", cursor.getInt(cursor.getColumnIndex(CategoriesDBAdapter.ROW_ID)));
 							startActivity(intent);	
 						}
 					}
 					
 				});
 			}
 		}
 		else if (loader.getId() == EVALS_LOADER_ID) {
 			evalsSearchCursorAdapter.swapCursor(cursor);
 			if (cursor != null && cursor.getCount() > 0) {
 				evalsTextView.setVisibility(View.VISIBLE);
 				evalsListView.setOnItemClickListener(new OnItemClickListener() {
 
 					@Override
 					public void onItemClick(AdapterView<?> parent, View view,
 							int position, long id) {
 						if (cursor.moveToPosition(position)) {
 							Intent intent = new Intent(Search.this, AddEval.class);
 							intent.putExtra("refID_Term", cursor.getInt(cursor.getColumnIndex(EvaluationsDBAdapter.TERM_REFERENCE)));
 							intent.putExtra("refID_Course", cursor.getInt(cursor.getColumnIndex(EvaluationsDBAdapter.COURSE_REFERENCE)));
 							intent.putExtra("refID_Category", cursor.getInt(cursor.getColumnIndex(EvaluationsDBAdapter.CATEGORY_REFERENCE)));
 							intent.putExtra("idEdit_Item", cursor.getInt(cursor.getColumnIndex(EvaluationsDBAdapter.ROW_ID)));
 							intent.putExtra("id_Mode", EditMode.EDIT_MODE);
 							startActivity(intent);	
 						}
 					}
 					
 				});
 			}
 		}
 		else if (loader.getId() == TASK_LOADER_ID) {
 			taskSearchCursorAdapter.swapCursor(cursor);
 			if (cursor != null && cursor.getCount() > 0) {
 				taskTextView.setVisibility(View.VISIBLE);
 				taskListView.setOnItemClickListener(new OnItemClickListener() {
 
 					@Override
 					public void onItemClick(AdapterView<?> parent, View view,
 							int position, long id) {
 						if (cursor.moveToPosition(position)) {
 							Intent intent = new Intent(Search.this, AddTask.class);
 							intent.putExtra("idEdit_Item", cursor.getInt(cursor.getColumnIndex(CoursesDBAdapter.ROW_ID)));
 							intent.putExtra("id_Mode", EditMode.EDIT_MODE);
 							startActivity(intent);	
 						}
 					}
 					
 				});
 			}
 		}
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 		if (loader.getId() == COURSES_LOADER_ID) {
 			coursesSearchCursorAdapter.swapCursor(null);
 			coursesListView.setOnItemClickListener(null);
 		}
 		else if (loader.getId() == CATS_LOADER_ID) {
 			catsSearchCursorAdapter.swapCursor(null);
 			catsListView.setOnItemClickListener(null);
 		}
 		else if (loader.getId() == EVALS_LOADER_ID) {
 			evalsSearchCursorAdapter.swapCursor(null);
 			evalsListView.setOnItemClickListener(null);
 		}
 		else if (loader.getId() == TASK_LOADER_ID) {
 			taskSearchCursorAdapter.swapCursor(null);
 			taskListView.setOnItemClickListener(null);
 		}
 	}
 
 	@Override
 	public boolean onQueryTextChange(String newText) {
 		getLoaderManager().restartLoader(COURSES_LOADER_ID, null, this);
 		getLoaderManager().restartLoader(CATS_LOADER_ID, null, this);
 		getLoaderManager().restartLoader(EVALS_LOADER_ID, null, this);
 		getLoaderManager().restartLoader(TASK_LOADER_ID, null, this);
 		
 		coursesTextView.setVisibility(View.GONE);
 		catsTextView.setVisibility(View.GONE);
 		evalsTextView.setVisibility(View.GONE);
 		taskTextView.setVisibility(View.GONE);
 		return true;
 	}
 
 	@Override
 	public boolean onQueryTextSubmit(String query) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
