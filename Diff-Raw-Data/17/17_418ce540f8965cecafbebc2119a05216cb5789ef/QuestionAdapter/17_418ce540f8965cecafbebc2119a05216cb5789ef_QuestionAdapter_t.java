 package com.kalidu.codeblue.activities.listQuestionActivity;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 import com.kalidu.codeblue.R;
 import com.kalidu.codeblue.models.Question;
 
 /**
  * Allows a List of Questions to be adapted to a ListView.
  * @author tgarv
  *
  */
 public class QuestionAdapter extends BaseAdapter {
 	
 	private LayoutInflater mInflater;
 	private int layout;
 	private static ArrayList<Question> questions;	// The List of Questions held by this view
 	
 	public QuestionAdapter(Context context, ArrayList<Question> questions){
 		this.mInflater = LayoutInflater.from(context);
 		QuestionAdapter.questions = questions;
 		this.layout = R.layout.question_layout;
 	}
 	
 	/**
 	 * Gets the view for a particular Question in the List of Questions.
 	 * 
 	 * @param position the index in the list for which we want the view.
 	 * @param convertView the old View to reuse, if possible. See the documentation for BaseAdapter.getView
 	 * @param parent the parent View that this View is attached to.
 	 */
 	public View getView(int position, View convertView, ViewGroup parent){
 		Question question = questions.get(position);
 		View questionView = mInflater.inflate(layout, parent, false);
 		
 		TextView author = (TextView) questionView.findViewById(R.id.question_author);
		author.setText(question.getUsername());
 		
 		TextView date = (TextView) questionView.findViewById(R.id.question_time);
		date.setText("TODO");
 		
 		TextView title = (TextView) questionView.findViewById(R.id.question_title);
 		title.setText(question.getTitle());
 		
 		TextView text = (TextView) questionView.findViewById(R.id.question_text);
 		text.setText(question.getText());
 		
 		return questionView;
 	}
 
 	/**
 	 * Gets the number of Questions in the List
 	 * @return the number of Questions in the List
 	 */
 	public int getCount() {
 		return questions.size();
 	}
 
 	/**
 	 * Gets the Question at a particular index
 	 * 
 	 * @param index the index for the desired Question
 	 * @return the Question at that index
 	 */
 	public Question getItem(int index) {
 		return questions.get(index);
 	}
 
 	/**
 	 * Gets the ID for the Question at index
 	 * 
 	 * @param index the index for the Question for which we want the ID
 	 * @return the ID at the given index
 	 */
 	public long getItemId(int index) {
 		// TODO Auto-generated method stub
 		return index;
 	}
 
 }
