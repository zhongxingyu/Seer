 package com.app.sentinelsecurity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.app.sentinelsecurity.domain.Question;
 
 public class QuestionsAdapter extends ArrayAdapter<Question> {
 	Context context;
 	List<Question> questions = new ArrayList<Question>();
 	QuestionHolder holder = null;
 	ListView list;
 
 	public QuestionsAdapter(ListActivity activity, Context context, List<Question> questions) {
 		super(context, R.layout.row_questions, questions);
 		this.questions = questions;
 		this.context = context;
 		list = activity.getListView();
 	}
 
 	@Override
 	public View getView(int position, final View convertView, ViewGroup parent) {
 		View row = convertView;
 
 		if (row == null) {
 			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			row = inflater.inflate(R.layout.row_questions, parent, false);
 			holder = new QuestionHolder();
 			holder.question = (TextView) row.findViewById(R.id.question);
 			holder.yes = (CheckBox) row.findViewById(R.id.yes);
 			holder.no = (CheckBox) row.findViewById(R.id.no);
 			row.setTag(holder);
 		} else {
 			holder = (QuestionHolder) row.getTag();
 		}
 		Question question = questions.get(position);
 		holder.question.setText(question.getQuestion());
 		holder.yes.setTag(getItem(position));
 		holder.no.setTag(getItem(position));
 		holder.yes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				final int position = list.getPositionForView(buttonView);
 				LinearLayout l = (LinearLayout) buttonView.getParent();
 				CheckBox c1 = (CheckBox) l.getChildAt(0);
 				CheckBox c2 = (CheckBox) l.getChildAt(1);
 				if (isChecked) {
 					c2.setChecked(false);
 				}
 			}
 		});
 		holder.no.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				final int position = list.getPositionForView(buttonView);
 				LinearLayout l = (LinearLayout) buttonView.getParent();
 				CheckBox c1 = (CheckBox) l.getChildAt(0);
 				CheckBox c2 = (CheckBox) l.getChildAt(1);
 				if (isChecked) {
 					c1.setChecked(false);
 				}
 			}
 		});
 		return row;
 	}
 
 	public class QuestionHolder {
 		TextView question;
 		CheckBox yes;
 		CheckBox no;
 	}
 
 	// @Override
 	// public void onCheckedChanged(CompoundButton buttonView, boolean
 	// isChecked) {
 	//
 	// LinearLayout l = (LinearLayout) buttonView.getParent();
 	// CheckBox c1 = (CheckBox) l.getChildAt(0);
 	// CheckBox c2 = (CheckBox) l.getChildAt(1);
 	//
 	// }
 }
