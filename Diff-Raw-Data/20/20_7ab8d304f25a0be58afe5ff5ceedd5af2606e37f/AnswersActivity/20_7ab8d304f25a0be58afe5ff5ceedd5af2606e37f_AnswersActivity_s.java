 package com.sambuo.stackoverlook;
 
 import java.util.List;
 
 import com.sambuo.stackoverlook.entities.Answer;
 import com.sambuo.stackoverlook.repositories.StackOverflowRepository;
 import com.sambuo.stackoverlook.utilities.Utils;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class AnswersActivity extends Activity {
 	
 	public final static String EXTRA_QUESTION_ID = "com.sambuo.stackoverlook.QUESTION_ID";
 	private final StackOverflowRepository repository = StackOverflowRepository.getInstance();
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.answers);
 		
 		Intent intent = getIntent();
 		if(!intent.hasExtra(UsersActivity.EXTRA_USER_ID)) {
 			Log.e(AnswersActivity.class.toString(), "UserId was not given to AnswersActivity");
 		}
 		
 		long selectedUserId = intent.getLongExtra(UsersActivity.EXTRA_USER_ID, 0);
 		Iterable<Answer> answers = this.repository.getLatestAnswersFromUserId(selectedUserId);
 		
 		final AnswerArrayAdapter adapter = new AnswerArrayAdapter(this, Utils.toArrayList(answers));
 		final ListView listView = (ListView) findViewById(R.id.answersListView);
 		listView.setAdapter(adapter);
 		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
 				Intent showAnswersIntent = new Intent(view.getContext(), QuestionActivity.class);
 				showAnswersIntent.putExtra(AnswersActivity.EXTRA_QUESTION_ID, id);
 				startActivity(showAnswersIntent);
 			}
 		});
 	}
 	
 	private class AnswerArrayAdapter extends BaseAdapter {
 		private List<Answer> answers;
 		private LayoutInflater inflater;
 
 		public AnswerArrayAdapter(Context context, List<Answer> answers) {
 			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         	this.answers = answers;
 		}
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View row = convertView;
 			if (row == null) {
 				row = inflater.inflate(R.layout.answer_row, parent, false);
 			}
 			
 			//TODO: Use ViewHolder pattern
 			TextView title = (TextView) row.findViewById(R.id.title);
 			TextView score = (TextView) row.findViewById(R.id.score);
 			
 			Answer answer = this.answers.get(position);
 			
			title.setText(answer.getTitle());
 			score.setText(String.format("%d", answer.getScore()));
 			if (answer.isAccepted()) {
 				score.setBackgroundResource(android.R.color.holo_green_dark);
 			}
 			
 			return row;
 		}
 		
 		@Override
 		public int getCount() {
 			return this.answers.size();
 		}
 
 		@Override
 		public Object getItem(int i) {
 			return this.answers.get(i);
 		}
 
 		@Override
 		public long getItemId(int i) {
 			return this.answers.get(i).getQuestionId();
 		}
 	}
 }
