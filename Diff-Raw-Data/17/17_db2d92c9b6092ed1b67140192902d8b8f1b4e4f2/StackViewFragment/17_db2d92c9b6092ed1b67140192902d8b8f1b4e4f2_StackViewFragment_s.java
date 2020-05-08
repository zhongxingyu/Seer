 package uk.co.ataulmunim.android.stacks.fragment;
 
 import android.view.*;
 import android.widget.*;
 import uk.co.ataulmunim.android.stacks.Stack;
 import uk.co.ataulmunim.android.stacks.activity.StacksActivity;
 import uk.co.ataulmunim.android.stacks.adapter.StacksCursorAdapter;
 import uk.co.ataulmunim.android.stacks.contentprovider.Stacks;
 import android.app.Activity;
 import android.app.ListFragment;
 
 
 import android.content.ContentUris;
 import android.content.CursorLoader;
 import android.content.Intent;
 import android.content.Loader;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.LoaderManager;
 import android.util.Log;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.nicedistractions.shortstacks.R;
 import uk.co.ataulmunim.android.widget.CroutonEx;
 
 
 public class StackViewFragment extends ListFragment
 	implements LoaderManager.LoaderCallbacks<Cursor>, Stack.OnStackChangedListener, OnEditorActionListener,
         OnItemClickListener, View.OnClickListener, PopupMenu.OnMenuItemClickListener {
 	
 	public static final String TAG = "StackViewFragment";
 	public static final String HEADER_TAG = "header";
     public static final String FOOTER_TAG= "footer";
 	
 	public static final String[] STACKS_PROJECTION = {
 		Stacks._ID,	Stacks.SHORTCODE, Stacks.ACTION_ITEMS, Stacks.NOTES
 	};
 
     private TextView shortCodeView;
     private TextView notesView;
     private ImageView showMoreNotes;
     private ImageView showLessNotes;
 
     /**
      * When user presses the context menu overflow button, the list item position is stored here
      */
     private int itemClickedForPopupMenu;
 
 	/**
 	 * Determines whether or not to close the soft input keyboard when adding Stacks to the list.
 	 * A value of TRUE will leave it open, but this can be set in shared preferences.
 	 */
 	private boolean quickAddMode;
 
 	/**
 	 * Determines whether or not to scroll to the end of the list after the
 	 * data set has been updated. Initially false so it doesn't scroll when the
 	 * activity is first opened, but true after that.
 	 */
 	private boolean scrollToEnd;
 
 	private StacksCursorAdapter adapter;
 	private int stackId = Stacks.ROOT_STACK_ID; // id of the current stack in the Stacks table
 
 	// Indicates whether the Stack Info view (header) is expanded
 	private boolean isNotesViewUnlimited;
 
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.fragment_stack_view, container, false);
     }
 
 
 	/**
 	 * Called after onCreateView(), after the parent activity is created
 	 */
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		
 		// Inflate the stackInfoView and footer views
         LayoutInflater inflater = getActivity().getLayoutInflater();
 
         RelativeLayout stackInfoView = (RelativeLayout) inflater.inflate(R.layout.fragment_stack_view_header, null);
         stackInfoView.setTag(HEADER_TAG);
 
         findViewsInHeader(stackInfoView);
         updateHeaderView();
 
        EditText newStackInput = (EditText) inflater.inflate(R.layout.fragment_stack_view_footer, null);
 
         getListView().addHeaderView(stackInfoView, null, true);
         getListView().addFooterView(newStackInput, null, true);
 
         getListView().setSelector(R.drawable.list_selector); // TODO: we don't want header view to show selected
 
 
         stackId = ((StacksActivity) getActivity()).getStackId();
 		
 		// Create an empty adapter we will use to display the loaded data.
 		adapter = new StacksCursorAdapter(
                 getActivity(),
                 R.layout.list_item_stacks,
                 null,
                 new String[] {Stacks.SHORTCODE},
                 new int[] { R.id.listitem_shortcode},
                 this
 	    );
         setListAdapter(adapter);
         getListView().setOnItemClickListener(this);
         getListView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
         
         // Prepare the loader.  Either re-connect with an existing one, or start a new one.
         getActivity().getLoaderManager().initLoader(StacksActivity.STACKS_LOADER, null, this);
 
         ((StacksActivity) getActivity()).getStack().addOnStackChangedListener(this);
 
         quickAddMode = true;
         newStackInput.setOnEditorActionListener(this);
 
         registerForContextMenu(getListView());
 	}
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getActivity().getMenuInflater();
         inflater.inflate(R.menu.menu_stackview_listitem, menu);
     }
 
     private void findViewsInHeader(View stackInfoView) {
         shortCodeView = (TextView) stackInfoView.findViewById(R.id.short_code);
         notesView = (TextView) stackInfoView.findViewById(R.id.notes);
         showMoreNotes = (ImageView) stackInfoView.findViewById(R.id.show_more_indicator);
         showLessNotes = (ImageView) stackInfoView.findViewById(R.id.show_less_indicator);
     }
 
 
 
 
     public void updateHeaderView() {
         Stack stack = ((StacksActivity) getActivity()).getStack();
         String notes = stack.getNotes();
         String shortCode = stack.getShortCode();
         int limitedLines = getResources().getInteger(R.integer.notes_line_height);
 
         shortCodeView.setText(shortCode);
 
         if (notes.length() <= 0) {
             notesView.setVisibility(View.GONE);
             showMoreNotes.setVisibility(View.GONE);
             showLessNotes.setVisibility(View.GONE);
             return;
         }
 
         if (notesView.getVisibility() != View.VISIBLE) notesView.setVisibility(View.VISIBLE);
 
         notesView.setText(stack.getNotes());
         notesView.setMaxLines(limitedLines);
         isNotesViewUnlimited = false;
 
         if (notesView.getLineCount() > limitedLines) {
             showMoreNotes.setVisibility(View.VISIBLE);
             showLessNotes.setVisibility(View.GONE);
         }
     }
 
 
     private void toggleStackInfoExpanded() {
         int limitedLines = getResources().getInteger(R.integer.notes_line_height);
 
         if (!isNotesViewUnlimited) {
             notesView.setMaxLines(Integer.MAX_VALUE);
             showMoreNotes.setVisibility(View.INVISIBLE);
             showLessNotes.setVisibility(View.VISIBLE);
             isNotesViewUnlimited = true;
         } else {
             notesView.setMaxLines(limitedLines);
             showMoreNotes.setVisibility(View.VISIBLE);
             showLessNotes.setVisibility(View.INVISIBLE);
             isNotesViewUnlimited = false;
         }
     }
 
 
 
     /**
 	 * Opens the clicked Stack in a new StacksActivity, or toggles the expanded
 	 * property of the Stack's header view.
 	 */
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		Log.d(TAG, "List item clicked, stackId: " + id);
 		
 		if (view.getTag().equals(HEADER_TAG)) {
             toggleStackInfoExpanded();
 		} else {
 			final Uri stack = ContentUris.withAppendedId(Stacks.CONTENT_URI, id);
 	        final Intent viewStack = new Intent(Intent.ACTION_VIEW, stack);
 	        startActivity(viewStack);
 		}
 	}
 
 
     /**
 	 * Adds a stack as a child to the current stack.
 	 */
 	@Override
 	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 		final String name = v.getText().toString().trim();
 		if (name.length() == 0) return true;
 		
 		Log.d(TAG, "Adding " + name);
         Stack.add(getActivity(), name, stackId);
         v.setText("");
 		
 		if (!quickAddMode) {
 			Log.d(TAG, "quickAddMode false, hiding keyboard.");
 			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
 					Activity.INPUT_METHOD_SERVICE);
 			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
 		}
 	    return true;
 	}
 	
 	// Loaders ////////////////////////////////////////////////////////////////////////////////////
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		CursorLoader cursorLoader = null;
 		
 		if (id == StacksActivity.STACKS_LOADER) {
 			Log.d(TAG, "Loading stacks under stack " + stackId);
 			
 			final String where = Stacks.PARENT + "=" + stackId +
 					" AND " + Stacks.DELETED + "<> 1" +
 					" AND " + Stacks._ID + "<>" + Stacks.ROOT_STACK_ID;
 			
 			cursorLoader = new CursorLoader(
 					getActivity(),
 					Stacks.CONTENT_URI,
 					STACKS_PROJECTION,
 					where,
 					null,
 					Stacks.LOCAL_SORT
 			);
 		}
 		
 		return cursorLoader;
 	}
 	
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
 		if (loader.getId() == StacksActivity.STACKS_LOADER) {
 			Log.d(TAG, "Stacks loaded, swapping cursor, scrolling to end.");
 			
 			adapter.swapCursor(data);
 			if (scrollToEnd) {
 				getListView().smoothScrollToPosition(adapter.getCount());
 			} else {
 				scrollToEnd = true;
 			}
 		}
 	}	
 	
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 		if (loader.getId() == StacksActivity.STACKS_LOADER) {
 			Log.d(TAG, "Closing last Stacks cursor, so setting adapter cursor to null.");
 			adapter.swapCursor(null);
 		}
 	}
 	
 	// Loaders end ////////////////////////////////////////////////////////////////////////////////
 
 
     @Override
     public void onStackChanged(Stack stack) {
         Log.d(TAG, "onStackChanged() called");
         updateHeaderView();
     }
 
     @Override
     public void onClick(View v) {
         Log.d(TAG, "mini overflow clicked");
         itemClickedForPopupMenu = (Integer) v.getTag(R.id.tag_stack_position);
 
         PopupMenu popup = new PopupMenu(getActivity(), v);
         popup.setOnMenuItemClickListener(this);
         popup.inflate(R.menu.menu_stackview_listitem);
         popup.show();
     }
 
 
 
     @Override
     public boolean onMenuItemClick(MenuItem item) {
         // getListAdapter().getItem(itemClickedForPopupMenu);
 
         switch (item.getItemId()) {
             case R.id.edit:
                 showCrouton("Edit pressed");
                 return true;
             case R.id.delete:
                 showCrouton("Delete pressed");
                 return true;
             case R.id.move:
                 showCrouton("Move pressed");
                 return true;
             default:
                 return false;
         }
     }
 
     private void showCrouton(String message) {
         CroutonEx.makeText(getActivity(), message, CroutonEx.INFO).show();
     }
 }
