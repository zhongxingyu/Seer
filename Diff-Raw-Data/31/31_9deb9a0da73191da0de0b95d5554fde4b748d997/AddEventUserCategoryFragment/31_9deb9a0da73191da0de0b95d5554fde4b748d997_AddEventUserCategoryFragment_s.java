 package com.samportnow.bato.addthought;
 
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.haarman.listviewanimations.swinginadapters.prepared.SwingRightInAnimationAdapter;
 import com.samportnow.bato.R;
 
 public class AddEventUserCategoryFragment extends Fragment
 {
 	private String[] mCategoryTitles = null;
 	private String[] mCategoryDescriptions = null;
 	private ArrayAdapter<String> mCategoryAdapter = null;
 
 	private ListView mCategoryListView = null;
 	private TextView mDescriptionTextView = null;
 	private Button mNextButton = null;	
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);	   
 
 		View view = inflater.inflate(R.layout.fragment_add_event_user_category, null);
 
 		mCategoryTitles = getResources().getStringArray(R.array.add_event_user_category_titles);
 		mCategoryDescriptions = getResources().getStringArray(R.array.add_event_user_category_descriptions);
 
 		mCategoryAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_single_choice, mCategoryTitles);	
 
 		mCategoryListView = (ListView) view.findViewById(R.id.user_category_choices);
 		mCategoryListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 
 		SwingRightInAnimationAdapter animationAdapter = new SwingRightInAnimationAdapter(mCategoryAdapter);
 		animationAdapter.setAbsListView(mCategoryListView);	
 		
 		mCategoryListView.setAdapter(animationAdapter);
 		mCategoryListView.setOnItemClickListener(new OnItemClickListener()
 		{
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
 			{
 				mDescriptionTextView.setText(mCategoryDescriptions[position]);
 				mNextButton.setEnabled(true);
 			}
 		});
 
 		mDescriptionTextView = (TextView) view.findViewById(R.id.user_category_description);
 		mDescriptionTextView.setText(R.string.add_event_user_category_instruction);	  
 		
 		mNextButton = (Button) view.findViewById(R.id.next_fragment);
 		mNextButton.setEnabled(false);
 		mNextButton.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
 				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
 				
 				int position = mCategoryListView.getCheckedItemPosition();
 
 				if (position != ListView.INVALID_POSITION)
 				{
 					Bundle eventBundle = getArguments();
 					eventBundle.putInt("negative_type", position);
 				}
 
 				createUseCopingDialog().show();
 			}
 		});
 
 		return view;
 	}
 	
 	private AlertDialog createUseCopingDialog()
 	{
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		
 		builder.setTitle(R.string.use_coping_strategy_dialog_title);
 		builder.setMessage(R.string.use_coping_strategy_dialog_message);
 		
 		builder.setNegativeButton(R.string.use_coping_strategy_dialog_no, new DialogInterface.OnClickListener()
 		{
 			public void onClick(DialogInterface dialog, int which)
 			{
 				// TODO: convert cast to use an interface.
 				((AddEventActivity) getActivity()).createNewEvent();
 			}
 		});
 		
 		builder.setPositiveButton(R.string.use_coping_strategy_dialog_yes, new DialogInterface.OnClickListener()
 		{
 			public void onClick(DialogInterface dialog, int which)
 			{	
 				Fragment fragment = new AddThoughtCopingStrategyFragment();
 				fragment.setArguments(getArguments());
 
 				getFragmentManager()
 					.beginTransaction()
 					.replace(R.id.fragment_container, fragment)
 					.commit();
 			}
 		});
 		
 		return builder.create();
 	}
 }
