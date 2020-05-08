 package to.rcpt.quest;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.LoaderManager;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.Loader;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.BaseColumns;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.view.View;
 import android.widget.ImageView;
 
 public class QuestListActivity extends ListActivity implements
 		LoaderManager.LoaderCallbacks<Cursor> {
 	private SimpleCursorAdapter adapter;
 
 	private final SimpleCursorAdapter.ViewBinder viewBinder = new SimpleCursorAdapter.ViewBinder() {
 		@Override
 		public boolean setViewValue(final View view, Cursor cursor,
 				int columnIndex) {
 			if (cursor.isNull(columnIndex)) {
 				return true;
 			}
 			if (view instanceof ImageView) {
 				new ImageLoadingTask(QuestListActivity.this, 256) {
 					@Override
 					protected void onPostExecute(Bitmap bm) {
 						((ImageView) view).setImageBitmap(bm);
 					}
 				}.execute(Uri.parse(cursor.getString(columnIndex)));
 				return true;
 			}
 			return false;
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getLoaderManager().initLoader(0, null, this);
 		adapter = new SimpleCursorAdapter(this, R.layout.list_item_querylist,
 				null, new String[] { Metadata.Images.ORIGINAL,
 						Metadata.Images.LINEARIZED, Metadata.Images.SOLUTION,
 						Metadata.Images.CLUE }, new int[] { R.id.original,
 						R.id.linearized, R.id.solution, R.id.clue }, 0);
 		adapter.setViewBinder(viewBinder);
 		setListAdapter(adapter);
 	}
 
 	public void deleteQuest(View v) {
 		int pos = viewToListPosition(v);
 		long rowId = adapter.getItemId(pos);
 		Cursor c = (Cursor) adapter.getItem(pos);
 		View dialog = getLayoutInflater().inflate(R.layout.dialog_delete_quest,
 				null);
 		dialog.findViewById(R.id.imageView1);
 		new AlertDialog.Builder(this)
 				.setView(dialog)
 				.setPositiveButton("Yes",
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								// TODO Auto-generated method stub
 							}
 						})
 				.setNegativeButton("No", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						// TODO Auto-generated method stub
 					}
 				}).show();
 	}
 
 	public void editImage(View v) {
 		int pos = viewToListPosition(v);
 		long rowId = adapter.getItemId(pos);
 		Cursor c = (Cursor) adapter.getItem(pos);
 		switch (v.getId()) {
 		case R.id.clue:
 			if (editImage(ClueImageActivity.class, c, rowId,
 					Metadata.Images.CLUE)) {
 				return;
 			}
 		case R.id.solution:
 			if (editImage(SolutionImageActivity.class, c, rowId,
 					Metadata.Images.SOLUTION)) {
 				return;
 			}
 		case R.id.linearized:
 			if (editImage(LinearizeActivity.class, c, rowId,
 					Metadata.Images.LINEARIZED)) {
 				return;
 			}
 		case R.id.original:
 			if (!editImage(LinearizeActivity.class, c, rowId,
 					Metadata.Images.ORIGINAL)) {
 				Toaster.s(this, "Couldn't load any images to edit!");
 			}
 		}
 	}
 
 	private int viewToListPosition(View v) {
 		int[] loc = new int[2];
 		v.getLocationInWindow(loc);
 		int pos = getListView().pointToPosition(loc[0], loc[1]);
 		return pos;
 	}
 
 	private boolean editImage(Class<?> cls, Cursor c, long rowId,
 			String columnName) {
 		int columnIndex = c.getColumnIndex(columnName);
		if (columnIndex == -1) {
			Toaster.s(this, "No entry for column", columnName);
			return false;
		}
		c.moveToPosition((int) rowId);
 		if (c.isNull(columnIndex)) {
 			return false;
 		}
 		Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(c
 				.getString(columnIndex)), this, cls);
 		i.putExtra(BaseColumns._ID, rowId);
 		startActivity(i);
 		return true;
 
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
 		return new Metadata.Helper(this).getLoader();
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
 		adapter.changeCursor(arg1);
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> arg0) {
 		adapter.changeCursor(null);
 	}
 }
