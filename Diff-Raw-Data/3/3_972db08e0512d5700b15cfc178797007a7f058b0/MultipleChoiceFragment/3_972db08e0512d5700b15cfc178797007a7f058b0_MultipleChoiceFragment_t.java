 package com.jjm.android.quiz.fragment;
  
 
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 import com.jjm.android.quiz.R;
 import com.jjm.android.quiz.model.Question;
 import com.jjm.android.quiz.view.QuizButton;
 
 
 public class MultipleChoiceFragment extends QuestionFragment {
 	private static final int[] sAnswerButtonIds = new int[]{
 		R.id.answer1, R.id.answer2, R.id.answer3, R.id.answer4
 	}; 
 	
 	@Override
 	protected int getLayoutResourceId() {
 		return mApp.getConfig().fixedLayout() ?
 				R.layout.multiple_choice_fragment_fixed_buttons :
 					R.layout.multiple_choice_fragment_scroll_buttons;
 	}
 
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 		for(int i=0;i<sAnswerButtonIds.length;i++)
 			view.findViewById(sAnswerButtonIds[i]).setOnClickListener(new ButtonListener(i));
 	}
 	
 	@Override
 	protected void onAnswerChanged() {
 		super.onAnswerChanged();
 		View view  = getView();
 		if(view == null) 
 			return;
 		Question question = mQuestion;
 		if(question == null)
 			return;
 		
 		for(int i=0;i<sAnswerButtonIds.length;i++){
 			QuizButton btn = (QuizButton) view.findViewById(sAnswerButtonIds[i]);
 			boolean showingAnswer = mAnswer != NO_ANSWER;
 			
 			if(i < question.getChoices().length){
 				btn.setVisibility(View.VISIBLE);
 				btn.setShowingAnswer(showingAnswer);
 				btn.setCorrect(i == question.getAnswer());
 				btn.setText(mApp.getHtmlCache().getHtml(question.getChoices()[i]));
				btn.setClickable(!showingAnswer);
 			}else{
 				btn.setVisibility(View.GONE);
 			}
 		}
 	} 
 	
 	private class ButtonListener implements OnClickListener {
 		private final int mIndex;
 		public ButtonListener(int index){
 			mIndex = index;
 		}
 		@Override
 		public void onClick(View v) {
			if(mAnswer != NO_ANSWER) return;
 			setAnswer(mIndex);
 		}
 	}
 	
 	public static MultipleChoiceFragment newInstance(Question question, boolean isLastQuestion){
 		MultipleChoiceFragment fragment = new MultipleChoiceFragment();
 		fragment.setArguments(newArguments(question, isLastQuestion));
 		return fragment;
 	}
 }
