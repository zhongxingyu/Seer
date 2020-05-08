 package com.samportnow.bato.addthought;
 
 import android.app.Fragment;
 import android.content.Context;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 
 import com.haarman.listviewanimations.swinginadapters.prepared.SwingRightInAnimationAdapter;
 import com.samportnow.bato.R;
 import com.samportnow.bato.database.ThoughtsDataSource;
 
 public class AddEventUserThoughtFragment extends Fragment
 {
 	private ArrayAdapter<String> mHistoryAdapter = null;
 
 	private EditText mThoughtEditText = null;
 	private ListView mHistoryListView = null;
 	private RadioGroup mIsNegativeRadioGroup = null;
 	private Button mNextButton = null;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		View view = inflater.inflate(R.layout.fragment_add_event_user_thought, null);
 		
		ThoughtsDataSource dataSource = new ThoughtsDataSource(getActivity()).open();
 
 		mHistoryAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
 		mHistoryAdapter.addAll(dataSource.getThoughts());
 		
 		dataSource.close();
 
 		mHistoryListView = (ListView) view.findViewById(R.id.user_thought_history);
 		
 		SwingRightInAnimationAdapter animationAdapter = new SwingRightInAnimationAdapter(mHistoryAdapter);
 		animationAdapter.setAbsListView(mHistoryListView);		
 		
 		mHistoryListView.setAdapter(mHistoryAdapter);
 		mHistoryListView.setOnItemClickListener(new OnItemClickListener()
 		{
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
 			{
 				String item = (String) mHistoryListView.getItemAtPosition(position);
 
 				mThoughtEditText.setText(item);
 				mThoughtEditText.setSelection(item.length());
 			}
 		});
 
 		mThoughtEditText = (EditText) view.findViewById(R.id.user_thought);
 		mThoughtEditText.addTextChangedListener(new TextWatcher()
 		{
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count)
 			{
 				mHistoryAdapter.getFilter().filter(s);
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count, int after)
 			{
 
 			}
 
 			@Override
 			public void afterTextChanged(Editable s)
 			{
 				updateNextButtonState();
 			}
 		});
 
 		mIsNegativeRadioGroup = (RadioGroup) view.findViewById(R.id.user_thought_is_negative);
 		mIsNegativeRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
 		{
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId)
 			{
 				updateNextButtonState();
 			}
 		});
 
 		mNextButton = (Button) view.findViewById(R.id.next_fragment);
 		mNextButton.setEnabled(false);
 		mNextButton.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
 				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
 
 				Bundle eventBundle = getArguments();
 				eventBundle.putString("user_thought", mThoughtEditText.getText().toString());
 
 				if (mIsNegativeRadioGroup.getCheckedRadioButtonId() == R.id.radio_positive)
 				{
 					Fragment fragment = new AddEventUserCategoryFragment();
 					fragment.setArguments(eventBundle);
 
 					getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
 				}
 				else
 				{
 					((AddEventActivity) getActivity()).createNewEvent();
 				}
 			}
 		});
 
 		return view;
 	}
 
 	private void updateNextButtonState()
 	{
 		mNextButton.setEnabled((mThoughtEditText.getText().length() > 0) && (mIsNegativeRadioGroup.getCheckedRadioButtonId() != -1));
 	}
 }
