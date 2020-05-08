 package palganthony.mtg.prices.ui;
 
 import palganthony.mtg.prices.CardInformationActivity;
 import palganthony.mtg.prices.DataBaseAdapter;
 import palganthony.mtg.prices.R;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.CursorAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import 	android.support.v4.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 /**
  * 
  * @author John Palgut likes men
  *
  * A @Fragment object to do card lookup.
  * 
  */
 public class LookUpCardFragment extends Fragment implements TextWatcher, OnItemClickListener{
 
 
 	final static int[] TO = new int[] { R.id.TextViewCardName, R.id.TextViewExpansionName};
     final static String[] FROM = new String[] { DataBaseAdapter.CARDNAME, DataBaseAdapter.CARDSET };
 	
 	private boolean mDualPane;
 	private EditText mCardNameEditText;
 	private ListView mSearchResultListView;
 	private DataBaseAdapter mDbHelper;
 	private LookUpCardCursorAdapter mAdapter;
 	
 	@Override
 	public void onActivityCreated (Bundle savedInstanceState)
 	{
 		super.onActivityCreated(savedInstanceState);
 		
 		mDbHelper = new DataBaseAdapter(getActivity());
		
		//give the screen a blank adapter to start
		mAdapter = new LookUpCardCursorAdapter(getActivity(), null, 0);
 
 		
 		mCardNameEditText = (EditText) getActivity().findViewById(R.id.EditTextCardName);
 		mSearchResultListView = (ListView) getActivity().findViewById(R.id.ListViewSearchResults);
 		mSearchResultListView.setAdapter(mAdapter);
 		mSearchResultListView.setOnItemClickListener(this);
 		
 		mCardNameEditText.addTextChangedListener(this);
 		
 		View placeholderFrame = getActivity().findViewById(R.id.FrameLayoutPlaceholder);
 		
 		mDualPane = placeholderFrame != null && placeholderFrame.getVisibility() == View.VISIBLE;
 		
 		/*if(mDualPane)
 			mSearchResultListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);*/
 	}
 	
 	/**
 	 * A helper method to decide if we can just populate a @Fragment, or if we have to call a new @Activity
 	 * 
 	 * @param pSelectedCardId The ID of the selected card
 	 */
 	private void showCardInfo(String pSelectedCardName, String pSetName)
 	{
 		if(!mDualPane) { //We need to call the other activity
 			Intent myIntent = new Intent(getActivity(), CardInformationActivity.class);
         	myIntent.putExtra(getString(R.string.extra_card_name), pSelectedCardName);
         	myIntent.putExtra(getString(R.string.extra_set_name), pSetName);
             startActivity(myIntent);
 		} else {
 			// Check what fragment is currently shown, replace if needed.
             CardInfoFragment cardInfo = (CardInfoFragment)getFragmentManager().findFragmentById(R.id.FrameLayoutPlaceholder);
             if (cardInfo == null || !cardInfo.getCardName().equals(pSelectedCardName)) {
                 // Make new fragment to show this selection.
             	cardInfo = CardInfoFragment.newInstance(getActivity(), pSelectedCardName, pSetName);
             	
                 // Execute a transaction, replacing any existing fragment
                 // with this one inside the frame.
                 FragmentTransaction ft = getFragmentManager().beginTransaction();
                 ft.replace(R.id.FrameLayoutPlaceholder, cardInfo);
                 ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                 ft.commit();
             }
 		}
 	}
 	
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		
         // Inflate the layout for this fragment
         return inflater.inflate(R.layout.lookup_fragment, container, false);
     }
 	
 	@Override
 	public void afterTextChanged(Editable s) {
 		final String text = s.toString(); 
 		
		//don't query any string less than 2 characters (there shouldn't be any cards with just 2 letters for a name), too many cards in the current listview will slow down the user experience
		if(text.length() > 2)
			mAdapter.changeCursor(mDbHelper.queryForMatchingCards(text));
 	}
 
 	@Override
 	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 	
 	@Override
 	public void onTextChanged(CharSequence s, int start, int before, int count) {}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		TextView cardName = (TextView) arg1.findViewById(R.id.TextViewCardName);
 		TextView setName = (TextView) arg1.findViewById(R.id.TextViewExpansionName);
 		
 		if(cardName != null && setName != null)
 		{
 			showCardInfo(cardName.getText().toString(), setName.getText().toString());
 			
 			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
 			imm.hideSoftInputFromWindow(mCardNameEditText.getWindowToken(), 0);
 		}
 	}
 }
