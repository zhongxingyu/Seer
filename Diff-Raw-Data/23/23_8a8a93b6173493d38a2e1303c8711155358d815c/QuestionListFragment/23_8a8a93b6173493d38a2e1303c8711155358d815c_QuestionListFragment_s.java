 package net.fiive.kotoba.activities.questionList;
 
 import android.database.Cursor;
 import net.fiive.kotoba.R;
 import net.fiive.kotoba.activities.questionEdit.QuestionEditActivity;
 import net.fiive.kotoba.data.dao.DataService;
 import net.fiive.kotoba.data.table.QuestionTable;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 import com.google.common.base.Preconditions;
 
 public class QuestionListFragment extends ListFragment {
 
 	private Cursor findAllCursor;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		initializeFragment();
 		this.setHasOptionsMenu(true);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		super.onCreateView(inflater, container, savedInstanceState);
 		return inflater.inflate(R.layout.default_view_for_list_fragment, container);
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		Preconditions.checkNotNull(id, "Id should not be null");
 		super.onListItemClick(l, v, position, id);
 
 		String uriValue = String.format("kotoba://kotoba.fiive.net/question/%d", id);
 		Intent editQuestionIntent = new Intent(QuestionEditActivity.EDIT_QUESTION_ACTION, Uri.parse(uriValue));
 		startActivity(editQuestionIntent);
 	}
 
 	@Override
	public void onResume() {
		super.onResume();
		if( findAllCursor.isClosed() ) {
			initializeFragment();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if( !findAllCursor.isClosed() ) {
			findAllCursor.close();
		}
 	}
 
 	private void initializeFragment() {
 		DataService dataService = new DataService(getActivity().getApplicationContext());
 		findAllCursor = dataService.cursorForFindAll();
 		CursorAdapter questionListAdapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.question_list_item, findAllCursor,
 			new String[] {QuestionTable.Columns.VALUE}, new int[] {R.id.questionListItemValue });
 		this.setListAdapter(questionListAdapter);
 	}
 
 }
