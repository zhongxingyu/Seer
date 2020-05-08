 package edu.cui.wineapp;
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 public class WineNotesFragment extends ListFragment {
 	public static final String ARG_WINE = "currWine";
 	public static DetailedWine dWine;
 	public int currentCommentCount = 0;
 	public int prevCommentCount = -1;
 
 	public View onCreateView(LayoutInflater inflater,
 			ViewGroup container, Bundle savedInstanceState){
 
 		View rootView = inflater.inflate(R.layout.fragment_wine_notes, container, false);
 		final EditText commentBox = (EditText) rootView.findViewById(R.id.comment);
 
 		dWine = (DetailedWine) getArguments().getSerializable(ARG_WINE);
 
 		updateComments();
 
 		commentBox.setOnEditorActionListener(new OnEditorActionListener(){
 
 			@Override
 			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event){
				
 				boolean handled = false;
 				
 				if (actionId == EditorInfo.IME_ACTION_SEND){
 					
 					UserManager.getUserManager(getActivity()).setComment(
 							dWine.getCode(), "USER_ID_GOES_HERE", commentBox.getText().toString());
					
 					commentBox.setText("");
 					handled = true;
 				}
 				
 				prevCommentCount = currentCommentCount;
				do updateComments(); while(prevCommentCount == currentCommentCount);
 				return handled;
 			}});
 		
 		return rootView;
 	}
 
 	protected void updateComments(){
 				
 		ArrayList<Comment>commentList = (UserManager.getUserManager(getActivity()).getComment(
 				"WHERE winecode = '"+dWine.getCode()+"' ORDER BY date DESC LIMIT 10"));
 		
 		currentCommentCount = commentList.size();
 		
 		ArrayList<String> commentStrings = new ArrayList<String>();
 
 		for(Comment c:commentList)
 			commentStrings.add(c.getComment());
 
 		setListAdapter(new ArrayAdapter<String>(
 				getActivity().getApplicationContext(), R.layout.list_item_black, commentStrings));
 	}
 }
