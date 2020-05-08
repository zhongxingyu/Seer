 package ltg.heliotablet_android;
 
 import java.util.Collections;
 import java.util.List;
 
 import ltg.heliotablet_android.data.Reason;
 import ltg.heliotablet_android.data.ReasonDBOpenHelper;
 import ltg.heliotablet_android.data.ReasonDataSource;
 import ltg.heliotablet_android.view.CircleView;
 import ltg.heliotablet_android.view.TheoryPlanetView;
 import ltg.heliotablet_android.view.controller.TheoryReasonController;
 
 import org.apache.commons.lang3.StringUtils;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.LoaderManager;
 import android.content.ContentValues;
 import android.content.Loader;
 import android.content.res.TypedArray;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.DragEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnDragListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 
 import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
 import com.google.common.collect.Lists;
 
 public class TheoryViewFragment extends Fragment implements
 		LoaderManager.LoaderCallbacks<Cursor> {
 
 	private String theoryAnchor;
 	private TheoryReasonController theoryController;
 	private ReasonDBOpenHelper db = null;
 	private TheoryPlanetView theoryView;
 	private SQLiteCursorLoader loader = null;
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		theoryView = (TheoryPlanetView) inflater.inflate(R.layout.theory_view,
 				container, false);
 		theoryView.setAnchor(theoryAnchor);
 		theoryView.setOnDragListener(new TargetViewDragListener());
 		db = ReasonDBOpenHelper.getInstance(this.getActivity());
 		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
 				.detectAll().penaltyLog().build());
 		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
 				.detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
 				.penaltyLog().penaltyDeath().build());
 
 		theoryController = TheoryReasonController.getInstance(getActivity());
 		theoryController.add(theoryAnchor, theoryView);
 		setupListeners();
 		setupLoader();
 
 		return theoryView;
 	}
 
 	private void setupLoader() {
 		getLoaderManager().initLoader(ReasonDBOpenHelper.ALL_REASONS_LOADER_ID,
 				null, this);
 		LoaderManager.enableDebugLogging(true);
 	}
 	
 	class TargetViewDragListener implements OnDragListener {
 
 		@Override
 		public boolean onDrag(View targetView, DragEvent event) {
 
 			// Dropped, reassign View to ViewGroup
 			View dragged = (View) event.getLocalState();
 			ViewGroup sourceView = (ViewGroup) dragged.getParent();
 
 			int action = event.getAction();
 			switch (event.getAction()) {
 			case DragEvent.ACTION_DRAG_STARTED:
 				break;
 			case DragEvent.ACTION_DRAG_ENTERED:
 				break;
 			case DragEvent.ACTION_DRAG_EXITED:
 				break;
 			case DragEvent.ACTION_DROP:
 
 				if (targetView.equals(sourceView)) {
 					Log.i("DRAG", "there equal");
 				} else {
 					sourceView.removeView(dragged);
 
 					RelativeLayout rel = (RelativeLayout) dragged;
 
 					CircleView cv = (CircleView) dragged;
 
 					if (targetView instanceof TheoryPlanetView) {
 						TheoryPlanetView tv = (TheoryPlanetView) targetView;
 						
 						Loader<Cursor> insertLoader = TheoryViewFragment.this.getLoaderManager().getLoader(ReasonDBOpenHelper.ALL_REASONS_LOADER_ID);
 						
 						if( insertLoader == null) {
 //							TheoryFragmentWithSQLiteLoaderNestFragments.this.getLoaderManager().initLoader(ReasonDBOpenHelper.ALL_REASONS_LOADER_ID, null, TheoryFragmentWithSQLiteLoaderNestFragments.this);
 							insertLoader = TheoryViewFragment.this.getLoaderManager().getLoader(ReasonDBOpenHelper.ALL_REASONS_LOADER_ID);
 							
 						} 
 
 						Reason reason = new Reason(tv.getAnchor(),cv.getFlag(), Reason.TYPE_THEORY,"tony", false);
 						
 						SQLiteCursorLoader sqlInsertLoader = (SQLiteCursorLoader)insertLoader;
 						
 						ContentValues reasonContentValues = ReasonDataSource.getReasonContentValues(reason);
 						sqlInsertLoader.insert(ReasonDBOpenHelper.TABLE_REASON, null, reasonContentValues);
 					} 
 				}
 				break;
 			case DragEvent.ACTION_DRAG_ENDED:
 			case DragEvent.ACTION_DRAG_LOCATION:
 				View v = (View) event.getLocalState();
 				v.setVisibility(View.VISIBLE);
 				break;
 			default:
 				break;
 			}
 			return true;
 		}
 	}
 
 	private void setupListeners() {
 		// add listeners to all the draggable planetViews
 		// get all the colors and the
 		
 		Button insertButton = new Button(getActivity());
 		insertButton.setLayoutParams(new LayoutParams(
 				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 		insertButton.setText("Insert");
 		insertButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				Reason marsORANGE = new Reason();
 
 				marsORANGE.setAnchor(TheoryViewFragment.this.theoryAnchor);
 				marsORANGE.setFlag(Reason.CONST_GREEN);
 				marsORANGE.setOrigin("tony");
 				marsORANGE.setReasonText("YEAH YEAHdfdfdfdfd ");
 				marsORANGE.setType(Reason.TYPE_THEORY);
				marsORANGE.setReadonly(true);
 
 				ContentValues reasonContentValues = ReasonDataSource
 						.getReasonContentValues(marsORANGE);
 
 				Loader<Cursor> insertLoader = TheoryViewFragment.this
 						.getLoaderManager().getLoader(
 								ReasonDBOpenHelper.ALL_REASONS_LOADER_ID);
 
 				if (insertLoader == null) {
 					TheoryViewFragment.this.getLoaderManager().initLoader(
 							ReasonDBOpenHelper.ALL_REASONS_LOADER_ID, null,
 							TheoryViewFragment.this);
 					insertLoader = TheoryViewFragment.this.getLoaderManager()
 							.getLoader(
 									ReasonDBOpenHelper.ALL_REASONS_LOADER_ID);
 
 				}
 
 				SQLiteCursorLoader sqlInsertLoader = (SQLiteCursorLoader) insertLoader;
 
 				sqlInsertLoader.insert(ReasonDBOpenHelper.TABLE_REASON, null,
 						reasonContentValues);
 				System.out.println("insert click!!");
 
 			}
 		});
 
 		theoryView.addView(insertButton);
 
 		Button deleteButton = new Button(getActivity());
 		deleteButton.setLayoutParams(new LayoutParams(
 				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 		deleteButton.setText("Delete");
 		deleteButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				Loader<Cursor> deleteLoader = TheoryViewFragment.this
 						.getLoaderManager().getLoader(
 								ReasonDBOpenHelper.DELETE_REASON_LOADER_ID);
 				Bundle args = new Bundle();
 				args.putString("ID", String.valueOf(1));
 				if (deleteLoader == null) {
 
 					TheoryViewFragment.this.getLoaderManager().initLoader(
 							ReasonDBOpenHelper.DELETE_REASON_LOADER_ID, args,
 							TheoryViewFragment.this);
 					deleteLoader = TheoryViewFragment.this.getLoaderManager()
 							.getLoader(
 									ReasonDBOpenHelper.DELETE_REASON_LOADER_ID);
 				} else {
 					TheoryViewFragment.this.getLoaderManager().restartLoader(
 							ReasonDBOpenHelper.DELETE_REASON_LOADER_ID, args,
 							TheoryViewFragment.this);
 				}
 
 				SQLiteCursorLoader sqlDeleteLoader = (SQLiteCursorLoader) deleteLoader;
 				String[] ids = { String.valueOf(1) };
 
 				sqlDeleteLoader.delete(db.TABLE_REASON, "_ID=?", ids);
 
 			}
 		});
 
 		theoryView.addView(deleteButton);
 
 		Button updateButton = new Button(getActivity());
 		updateButton.setLayoutParams(new LayoutParams(
 				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 		updateButton.setText("Update");
 		updateButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				Loader<Cursor> updateLoader = TheoryViewFragment.this
 						.getLoaderManager().getLoader(
 								ReasonDBOpenHelper.UPDATE_REASON_LOADER_ID);
 				if (updateLoader == null) {
 					TheoryViewFragment.this.getLoaderManager().initLoader(
 							ReasonDBOpenHelper.DELETE_REASON_LOADER_ID, null,
 							TheoryViewFragment.this);
 					updateLoader = TheoryViewFragment.this.getLoaderManager()
 							.getLoader(
 									ReasonDBOpenHelper.DELETE_REASON_LOADER_ID);
 				} else {
 					TheoryViewFragment.this.getLoaderManager().restartLoader(
 							ReasonDBOpenHelper.DELETE_REASON_LOADER_ID, null,
 							TheoryViewFragment.this);
 				}
 
 				SQLiteCursorLoader sqlDeleteLoader = (SQLiteCursorLoader) updateLoader;
 				String[] ids = { String.valueOf(1) };
 
 				Reason earthRed2 = new Reason();
 
 				earthRed2.setAnchor(Reason.CONST_EARTH);
 				earthRed2.setFlag(Reason.CONST_RED);
 				earthRed2.setOrigin("tony");
 				earthRed2.setReasonText("FUCKING its the biggest");
 				earthRed2.setType(Reason.TYPE_THEORY);
 				earthRed2.setReadonly(false);
 
 				String[] uid = { String.valueOf(2) };
 
 				ContentValues reasonContentValues = ReasonDataSource
 						.getReasonContentValues(earthRed2);
 
 				Bundle args = new Bundle();
 				args.putString("id", "2");
 
 				TheoryViewFragment.this.getLoaderManager().restartLoader(
 						ReasonDBOpenHelper.UPDATE_REASON_LOADER_ID, args,
 						TheoryViewFragment.this);
 
 				// sqlDeleteLoader.update(db.TABLE_REASON, reasonContentValues,
 				// "_id=?", ids);
 
 			}
 		});
 
 		theoryView.addView(updateButton);
 
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 
 		switch (id) {
 		case ReasonDBOpenHelper.ALL_REASONS_LOADER_ID:
 			return new SQLiteCursorLoader(
 					this.getActivity(),
 					db,
 					"SELECT _id, anchor, type, flag, reasonText, isReadOnly, lastTimestamp, origin FROM reason WHERE type = '"
 							+ ReasonDBOpenHelper.TYPE_THEORY
 							+ "' AND anchor = '"
 							+ theoryAnchor
 							+ "' ORDER BY anchor, flag, isReadOnly;", null);
 		}
 
 		return null;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
 
 		switch (loader.getId()) {
 		case ReasonDBOpenHelper.ALL_REASONS_LOADER_ID:
 			updateAllViews(data);
 			break;
 		}
 		Log.d("THEORY FRAGMENT", "onLoadFinished");
 	}
 	
 	private void updateAllViews(Cursor data) {
 		if (data != null && data.getCount() > 0) {
 			List<Reason> allReasons = Lists.newArrayList();
 			allReasons = Collections.synchronizedList(allReasons);
 			data.moveToFirst();
 			while (!data.isAfterLast()) {
 				Reason reason = ReasonDataSource.cursorToReason(data);
 				allReasons.add(reason);
 				data.moveToNext();
 			}
 			quickDump(allReasons);
 			theoryController.updateViews(allReasons, this.theoryAnchor);
 		}
 	}
 
 	private void quickDump(List<Reason> allReasons) {
 		for (Reason reason : allReasons) {
 			System.out.println("REASON: " + reason.toString());
 		}
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 		System.out.println("yo");
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		db.close();
 	}
 
 	@Override
 	public void onInflate(Activity activity, AttributeSet attrs,
 			Bundle savedInstanceState) {
 		super.onInflate(activity, attrs, savedInstanceState);
 
 		TypedArray a = activity.obtainStyledAttributes(attrs,
 				R.styleable.TheoryViewFragment);
 		
 		
 		theoryAnchor = StringUtils.lowerCase((String) a.getText(R.styleable.TheoryViewFragment_android_label));
 		a.recycle();
 
 	}
 
 }
