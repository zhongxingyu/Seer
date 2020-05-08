 package com.flashmath.activity;
 
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.Spannable;
 import android.text.SpannableString;
 import android.text.TextPaint;
 import android.text.method.LinkMovementMethod;
 import android.text.style.URLSpan;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.education.flashmath.R;
 import com.flashmath.fragment.LongFragment;
 import com.flashmath.fragment.LongGraphFragment;
 import com.flashmath.models.Score;
 import com.flashmath.network.FlashMathClient;
 import com.flashmath.util.ColorUtil;
 import com.flashmath.util.ConnectivityUtil;
 import com.jjoe64.graphview.GraphView.GraphViewData;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 public class LongActivity extends Activity {
 	private String subject;
 	private String subjectTitle;
 	private Button btnClear;
 	private Button btnSwap;
 	private TextView tvAttempts;
 	private TextView tvBest;
 	private TextView tvWorst;
 	private TextView tvAverage;
 	private LongFragment lf;
 	private LongGraphFragment lg;
 	private Boolean listOn;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_long);
 		TextView tvSubject = (TextView) findViewById(R.id.tvSubject);
 		TextView tvLink = (TextView) findViewById(R.id.tvLink);
 	    TextView tvStudy = (TextView) findViewById(R.id.tvStudy);
 	    btnClear = (Button) findViewById(R.id.btnClear);
 	    tvAttempts = (TextView) findViewById(R.id.tvAttempts);
 	    tvBest = (TextView) findViewById(R.id.tvBest);
 	    tvWorst = (TextView) findViewById(R.id.tvWorst);
 	    tvAverage = (TextView) findViewById(R.id.tvAverage);
 	    btnClear.setBackground(getResources().getDrawable(R.drawable.btn_red));
 		
 		subject = getIntent().getStringExtra("subject");
 		ActionBar ab = getActionBar();
 		ab.setIcon(ColorUtil.getBarIcon(subject, this));
 	    Button btnClose = (Button) findViewById(R.id.btnClose);
 	    btnClose.setBackground(ColorUtil.getButtonStyle(subject, this));
 	    btnSwap = (Button) findViewById(R.id.btnSwap);
 	    btnSwap.setBackground(ColorUtil.getButtonStyle(subject, this));
 		subjectTitle = Character.toUpperCase(subject.charAt(0))+subject.substring(1);
 		ab.setTitle(subjectTitle + " Details");
 		
 		tvSubject.setText("Score History for "+ subjectTitle);
 		tvSubject.setBackgroundColor(ColorUtil.subjectColorInt(subject));
 		tvSubject.setTextColor(Color.WHITE);
 		tvStudy.setText(subjectTitle);
		tvLink.setText(Html.fromHtml("<a style='text-decoration:none' href=http://en.wikipedia.org/wiki/"+subject+"_(mathematics)>↱"));
 		stripUnderlines(tvLink);
 		tvLink.setMovementMethod(LinkMovementMethod.getInstance());
 		
 		//Graph section
 		
 		FlashMathClient client = FlashMathClient.getClient(this);
 		client.getScores(subject, new JsonHttpResponseHandler() {
 			@SuppressLint("SimpleDateFormat")
 			@Override
 			public void onSuccess(JSONArray jsonScores) {
 			    
 				tvAttempts.setText(jsonScores.length() + " Attempts");
 				GraphViewData[] data = new GraphViewData[jsonScores.length()];
 				ArrayList<Score> scoreList = new ArrayList<Score>(jsonScores.length());
 				
 				int max_score = 0;
 				int min_score = -1;
 				int total = 0;
 				for (int i = jsonScores.length() - 1; i >= 0; i--) {
 					try {
 						int val = jsonScores.getJSONObject(i).getInt("value");
 						max_score = val > max_score ? val : max_score;
 						if (min_score == -1) {
 							min_score = val;
 						} else {
 							min_score = val > min_score ? min_score : val;
 						}
 						total += val;
 						data[i] = (new GraphViewData(i + 1, val));
 						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
 						Date date = dateFormat.parse(jsonScores.getJSONObject(i).getString("created"), new ParsePosition(0));
 						scoreList.add(new Score(jsonScores.length() - i, val, date));
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				}
 				if (min_score == -1) {
 					min_score = 0;
 				}
 				tvBest.setText(String.valueOf(max_score));
 				tvBest.setTextColor(ColorUtil.getScoreColor((float) max_score / 3));
 				tvWorst.setText(String.valueOf(min_score));
 				tvWorst.setTextColor(ColorUtil.getScoreColor((float) min_score / 3));
 				float average = jsonScores.length() == 0 ? 0f : (float) total / jsonScores.length();
 				tvAverage.setText(String.format("%.1f", average));
 				tvAverage.setTextColor(ColorUtil.getScoreColor(average / 3));
 				lg = new LongGraphFragment();
 				lg.setScores(data);
 				lg.setSubject(subject);
 				lf = new LongFragment();
 				lf.setScoreList(scoreList);
 				lf.setSubject(subject);
 				getFragmentManager().beginTransaction().add(R.id.frameStats, lf).commit();
 				listOn = true;
 				findViewById(R.id.pgLoad).setVisibility(View.GONE);
 			}
 		});
 	}
 	
 	//Close button
 	public void onClose(View v){
 		finish();
 	}
 
 	//Close button
 	public void onSwap(View v){
 		if (listOn) {
 			getFragmentManager().beginTransaction()
 				.setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out,
 						 R.animator.card_flip_left_in, R.animator.card_flip_left_out)
 				.replace(R.id.frameStats, lg)
 				.addToBackStack(null)
 				.commit();
 			listOn = false;
 			btnSwap.setText("List");
 			btnSwap.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_list, 0, 0, 0);
 		} else {
 			getFragmentManager().beginTransaction()
 				.setCustomAnimations(R.animator.card_flip_left_in, R.animator.card_flip_left_out,
 						 R.animator.card_flip_right_in, R.animator.card_flip_right_out)
 				.replace(R.id.frameStats, lf)
 				.addToBackStack(null)
 				.commit();
 			listOn = true;
 			btnSwap.setText("Graph");
 			btnSwap.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_line, 0, 0, 0);
 		}
 	}
 	
 	//Clear button
 	public void onClear(View v){
 		tvAttempts.setText("0 Attempts");
 		tvBest.setText("0");
 		tvBest.setTextColor(ColorUtil.getScoreColor(0));
 		tvWorst.setText("0");
 		tvWorst.setTextColor(ColorUtil.getScoreColor(0));
 		tvAverage.setText("0.0");
 		tvAverage.setTextColor(ColorUtil.getScoreColor(0));
 		
 		lf.clearScores();
 		lg.clearScores();
 		
 		if (ConnectivityUtil.isInternetConnectionAvailable(this)) {
 			FlashMathClient client = FlashMathClient.getClient(this);
 			
 			client.clearScores(subject, new JsonHttpResponseHandler() {
 				@Override
 				public void onSuccess(JSONArray jsonScores) {
 	 
 				}
 			});
 		} else {
 			Toast.makeText(this, ConnectivityUtil.INTERNET_CONNECTION_IS_NOT_AVAILABLE, Toast.LENGTH_SHORT).show();
 		}
 		
 	}
 	
 	private void stripUnderlines(TextView textView) {
         Spannable s = (Spannable) new SpannableString(textView.getText());
         URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
         for (URLSpan span: spans) {
             int start = s.getSpanStart(span);
             int end = s.getSpanEnd(span);
             s.removeSpan(span);
             span = new URLSpanNoUnderline(span.getURL());
             s.setSpan(span, start, end, 0);
         }
         textView.setText(s);
     }
 	
 	@Override
 	public void onBackPressed() {
 		Intent i = new Intent(this, SubjectActivity.class);
 		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(i);
 	}
 	
 	private class URLSpanNoUnderline extends URLSpan {
         public URLSpanNoUnderline(String url) {
             super(url);
         }
         @Override public void updateDrawState(TextPaint ds) {
             super.updateDrawState(ds);
             ds.setUnderlineText(false);
         }
     }
 }
