 package com.mick88.superbrain.main;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.mick88.superbrain.R;
 import com.mick88.superbrain.SuperBrainApplication;
 import com.mick88.superbrain.quiz.QuizActivity;
 import com.mick88.superbrain.quiz_creator.QuizCreatorActivity;
 import com.mick88.superbrain.quizzes.Quiz;
 import com.mick88.superbrain.quizzes.QuizManager;
 
 /**
  * A dummy fragment representing a section of the app, but that simply displays
  * dummy text.
  */
 public class SectionFragment extends Fragment implements OnItemLongClickListener
 {
 	public static final String EXTRA_CATEGORY = "quiz_category";
 	public static final int REQUEST_ID_EDIT_QUIZ = 1;
 	String categoryName;
 	List<Quiz> quizzes;
 	QuizManager quizManager;
 	private ListView listView;
 	
	public void setCategoryName(String categoryName)
	{
		this.categoryName = categoryName;
	}
	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
		String category = getArguments().getString(EXTRA_CATEGORY);
		this.quizzes = quizManager.getQuizzes(category);
 	}
 	
 	@Override
 	public void onAttach(Activity activity)
 	{
 		super.onAttach(activity);
 		SuperBrainApplication application = (SuperBrainApplication) activity.getApplication();
 		this.quizManager = application.getQuizManager();
 	}
 	
 	void populateList()
 	{
 		ArrayAdapter<Quiz> adapter = new ArrayAdapter<Quiz>(getActivity(), android.R.layout.simple_list_item_1, quizzes);
 		listView.setAdapter(adapter);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState)
 	{
 		
 		View rootView = inflater.inflate(R.layout.fragment_list_quizes,
 				container, false);
		
 		listView = (ListView) rootView
 				.findViewById(R.id.listOfQuizzes);
 			
 			listView.setOnItemClickListener(new OnItemClickListener()
 			{
 
 				@Override
 				public void onItemClick(AdapterView<?> arg0, View arg1,
 						int arg2, long id)
 				{
 					Quiz quiz = (Quiz) arg0.getItemAtPosition(arg2);
 					if (quiz.getId() == null)
 						throw new NullPointerException("Quiz id is null.");
 					Intent intent = new Intent(getActivity(), QuizActivity.class);
 					intent.putExtra(QuizActivity.EXTRA_ID, quiz.getId());
 					
 					getActivity().startActivity(intent);
 					
 				}
 			});
 			listView.setOnItemLongClickListener(this);
 			populateList();
 
 		/*
 		 * dummyTextView.setText(Integer.toString(getArguments().getInt(
 		 * ARG_SECTION_NUMBER)));
 		 */
 		return rootView;
 	}
 	@Override
 	public void onDestroyView()
 	{
 		super.onDestroyView();
 		this.listView = null;
 	}
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == REQUEST_ID_EDIT_QUIZ)
 		{
 			if (resultCode == Activity.RESULT_OK)
 			{
				this.quizzes = quizManager.getQuizzes(categoryName);
 				populateList();
 			}
 		}
 	}
 
 	@Override
 	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
 			long arg3)
 	{
 		Quiz quiz = (Quiz) arg0.getItemAtPosition(arg2);
 		Intent intent = new Intent(getActivity(), QuizCreatorActivity.class);
 		intent.putExtra(QuizCreatorActivity.EXTRA_QUIZ_ID, quiz.getId());
 		startActivityForResult(intent, REQUEST_ID_EDIT_QUIZ);
 		return true;
 	}
 }
