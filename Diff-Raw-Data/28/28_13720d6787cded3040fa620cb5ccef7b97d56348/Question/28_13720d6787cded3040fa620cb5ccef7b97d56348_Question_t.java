 package org.geobricks.survey.questions;
 
 import java.util.UUID;
 
 import org.geobricks.survey.R;
 import org.geobricks.survey.bean.QuestionBean;
 import org.geobricks.survey.constants.QUESTIONTYPE;
 import org.geobricks.survey.questions.types.QuestionDate;
 import org.geobricks.survey.questions.types.QuestionEditText;
 import org.geobricks.survey.questions.types.QuestionListView;
 import org.geobricks.survey.questions.types.QuestionMultiselection;
 import org.geobricks.survey.questions.types.QuestionSpinner;
 import org.geobricks.survey.questions.types.QuestionValue;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.Html;
 import android.text.InputType;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ScrollView;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class Question extends Fragment {
 	
 	QuestionBean qb;
 	
 //	ScrollView scrollView;
 	
 	ScrollView s;
 	
 	LinearLayout panel;
 	
 	QuestionValue questionValue;
 	
 // 	int spinner_layout = android.R.layout.simple_spinner_item;
 
 	
 	public static Question newInstance(QuestionBean bean) {
 		Question f = new Question();
 		Log.i("newInstance", "newInstance");
 		f.setQuestionBean(bean);
         return f;
 	}
 	
 	public void setQuestionBean(QuestionBean bean) {
        this.qb = bean;
     }
 	
 	public QuestionBean getQuestionBean() {
 		return qb;
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		Log.i("onResume", "onResume");
 //		createUI();
 	}
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.i("onCreate", "ok");
 //		createUI();
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		Log.i("onpause", "pause");
 //		createUI();
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		QuestionBean qb = getQuestionBean();
 		String text = qb.getNumber() + ") " + qb.getText();
 
 		Log.i("onCreateView", String.valueOf(getQuestionBean().getText()));
 
 		if (container == null) {
 			// We have different layouts, and in one of them this
 			// fragment's containing frame doesn't exist. The fragment
 			// may still be created from its saved state, but there is
 			// no reason to try to create its view hierarchy because it
 			// won't be displayed. Note this is not needed -- we could
 			// just run the code below, where we would create and return
 			// the view hierarchy; it would just never be used.
 			return null;
 		}
 
 //		ScrollView scrollView = new ScrollView(getActivity());
 		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getActivity().getResources().getDisplayMetrics());
 //		scrollView.setPadding(padding, padding, padding, padding);
 		panel = new LinearLayout(getActivity());
 		
 		panel.setOrientation(LinearLayout.VERTICAL);
 		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT);
 		llp.setMargins(5, 5, 5, 5);
 		llp.weight = 1.0f;
 //		scrollView.setLayoutParams(llp);
 		panel.setLayoutParams(llp);
 		 
 //		scrollView.addView(panel);
 
 		panel.setBackgroundColor(Color.WHITE);
 //		scrollView.setBackgroundColor(Color.WHITE);
 
 		LinearLayout h = new LinearLayout(getActivity());	
 		if( qb.getInfo() != null ) {
 			h.addView(addinfo(qb.getInfo()));
 		}
 		
 		h.setOrientation(LinearLayout.HORIZONTAL);
 		TextView t = new TextView(getActivity());
 		t.setPadding(padding, padding, padding, padding);
 		h.addView(t);
 		t.setTextAppearance(getActivity(), R.style.QuestionTitle);
 		t.setText(text);
 		
 
 		
 		panel.addView(h);
 		
 //		TextView uuid = new TextView(getActivity());
 //		uuid.setPadding(padding, padding, padding, padding);
 //		panel.addView(uuid);
 //		uuid.setText(String.valueOf(UUID.randomUUID()));
 
 		panel.addView(createForm(qb));
 		// container.removeAllViews();
 
 		// return inflater.inflate(R.layout.question, container, false);
 		return panel;
 	}
 	
 	private ImageView addinfo(String info) {
 		ImageView i = new ImageView(getActivity());
 		i.setImageResource(R.drawable.ic_menu_info_details);
 		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		builder.setMessage(Html.fromHtml(info)).setPositiveButton(R.string.ok, null);
 		i.setOnClickListener(new View.OnClickListener() {	
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				builder.show();
 			}
 		});
 		return i;
 	}
 
 	private LinearLayout createForm(QuestionBean qb) {
 		Log.i("QUESTIONTYPE", String.valueOf(qb.getQuestionType()));
 
 		LinearLayout panel = new LinearLayout(getActivity());
 		panel.setOrientation(LinearLayout.VERTICAL);
 		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT);
 		llp.setMargins(5, 5, 5, 5);
 		switch (qb.getQuestionType()) {
 		case SINGLE_VALUE_NUMBER:
 			questionValue = new QuestionEditText(getActivity());
 			((QuestionEditText) questionValue).build(InputType.TYPE_CLASS_NUMBER);
 			break;
 		case SINGLE_VALUE_TEXT:
 			questionValue = new QuestionEditText(getActivity());
 			((QuestionEditText) questionValue).build(null);
 			break;
 		case FREE_TEXT:
 			questionValue = new QuestionEditText(getActivity());
 			((QuestionEditText) questionValue).build(null);
 			break;
 		case SINGLE_VALUE_DATE:
 			questionValue = new QuestionDate(getActivity());
 			((QuestionDate) questionValue).build();
 			break;
 		case SINGLE_CHOICE:
 			questionValue = new QuestionSpinner(getActivity());
 			((QuestionSpinner) questionValue).build(null, qb.getQuestionChoices().getChoices());
 			break;
 		case MULTIPLE_CHOICE:
 			questionValue = new QuestionMultiselection(getActivity());
 			((QuestionMultiselection) questionValue).build();
 			break;
 
 		default: break;
 		}
 		try {
 			panel.addView(questionValue.getPanel(), llp);
 		}catch(Exception e ){}
 		return panel;
 	}
 	
 
 	// public void build() {
 	//
 	// View text =
 	// this.container.addView(child);
 	// }
 	// //
 	// public LinearLayout getPanel() {
 	// return panel;
 	// }
 	//
 	// public void setPanel(LinearLayout panel) {
 	// this.panel = panel;
 	// }
 
 }
