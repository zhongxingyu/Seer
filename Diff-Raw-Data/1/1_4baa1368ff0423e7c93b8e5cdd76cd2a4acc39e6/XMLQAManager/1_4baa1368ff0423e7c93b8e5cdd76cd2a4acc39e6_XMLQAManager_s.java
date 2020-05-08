 package com.t2.mtbi.activity.qa;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Stack;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.XmlResourceParser;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 import com.t2.mtbi.activity.ABSNavigationActivity;
 
 public abstract class XMLQAManager extends ABSNavigationActivity {
 	public static final String EXTRA_XML_RESID = "xmlResourceID";
 	private static final String TAG = XMLQAManager.class.getSimpleName();
 
 	protected Questionare questionare = new Questionare();
 	protected LinkedHashMap<String,Question> questions = new LinkedHashMap<String,Question>();
 	protected LinkedHashMap<String,Answer> answers = new LinkedHashMap<String,Answer>();
 	protected LinkedHashMap<String,Answer[]> selectdAnswers = new LinkedHashMap<String,Answer[]>();
 	private Question currentQuestion;
 
 	private static final int QUESTION_ACTIVITY = 309;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		int xmlResId = getIntent().getIntExtra(EXTRA_XML_RESID, -1);
 		if(xmlResId == -1 || !loadData(xmlResId)) {
 			this.finish();
 			return;
 		}
 	}
 
 	protected void startQuestionare() {
 		Question nextQuestion = getNextQuestion(this.currentQuestion);
 		this.currentQuestion = nextQuestion;
 		showQuestion(this.currentQuestion);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if(requestCode == QUESTION_ACTIVITY) {
 			if(resultCode == RESULT_OK) {
 				if(data != null) {
 					Parcelable[] parcels = data.getParcelableArrayExtra(XMLQAQuestion.EXTRA_ANSWERS);
 					Answer[] qAnswers = new Answer[parcels.length];
 					for(int i = 0; i < qAnswers.length; ++i) {
 						qAnswers[i] = (Answer)parcels[i];
 					}
					Log.v(TAG, "ANSARR:"+qAnswers.length);
 					this.addSelectedAnswer(
 							(Question)data.getParcelableExtra(XMLQAQuestion.EXTRA_QUESTION),
 							qAnswers
 					);
 				}
 
 
 				Question nextQuestion = getNextQuestion(this.currentQuestion);
 
 				// this was the last question.
 				if(nextQuestion == null) {
 					onAllQuestionsAnswered();
 					return;
 
 				// load the next question
 				} else {
 					this.currentQuestion = nextQuestion;
 					showQuestion(this.currentQuestion);
 				}
 
 			} else if(resultCode == RESULT_BACK) {
 				onQuestionCancelled();
 				return;
 			}
 		}
 	}
 
 	private void addSelectedAnswer(Question q, Answer[] ans) {
 		selectdAnswers.put(
 				q.id,
 				ans
 		);
 	}
 
 	private boolean loadData(int xmlResourceId) {
 		Stack<String> tagsStack = new Stack<String>();
 		tagsStack.push("");
 		try {
 			XmlResourceParser parser = this.getResources().getXml(xmlResourceId);
 
 			int eventType = parser.getEventType();
 			while(eventType != XmlPullParser.END_DOCUMENT) {
 				String prevTag = tagsStack.peek();
 				String tag = parser.getName();
 
 				if(eventType == XmlPullParser.START_TAG) {
 					tagsStack.push(tag);
 
 					if(prevTag.equals("questionare")) {
 						if(tag.equals("title")) {
 							eventType = parser.next();
 							if(eventType == XmlPullParser.TEXT) {
 								questionare.title = parser.getText();
 							}
 							continue;
 						} else if(tag.equals("desc")) {
 							eventType = parser.next();
 							if(eventType == XmlPullParser.TEXT) {
 								questionare.desc = parser.getText();
 							}
 							continue;
 						} else if(tag.equals("content")) {
 							eventType = parser.next();
 							if(eventType == XmlPullParser.TEXT) {
 								questionare.content = parser.getText();
 							}
 							continue;
 						}
 					} else if(prevTag.equals("questions")) {
 						if(tag.equals("question")) {
 							Question q = new Question();
 							q.id = parser.getAttributeValue(null, "id");
 							q.desc = parser.getAttributeValue(null, "desc");
 							q.title = parser.getAttributeValue(null, "title");
 							q.answerIds = parser.getAttributeValue(null, "answerIds").split(",");
 							this.questions.put(q.id, q);
 						}
 					} else if(prevTag.equals("answers")) {
 						if(tag.equals("answer")) {
 							Answer a = new Answer();
 							a.id = parser.getAttributeValue(null, "id");
 							a.title = parser.getAttributeValue(null, "title");
 							a.desc = parser.getAttributeValue(null, "desc");
 							a.value = parser.getAttributeIntValue(null, "value", 0);
 							this.answers.put(a.id, a);
 						}
 					}
 
 				} else if(eventType == XmlPullParser.END_TAG) {
 					tagsStack.pop();
 				}
 
 				eventType = parser.next();
 			}
 		} catch (XmlPullParserException e) {
 			e.printStackTrace();
 			return false;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 
 		return true;
 	}
 
 	protected void onQuestionCancelled() {
 		this.finish();
 	}
 
 	protected abstract double getTotalScore();
 	protected abstract void onAllQuestionsAnswered();
 
 	protected Question getNextQuestion(Question previousQuestion) {
 		boolean loadNext = (previousQuestion == null);
 		for(Question q: questions.values()) {
 			if(loadNext) {
 				return q;
 			}
 
 			if(q == previousQuestion) {
 				loadNext = true;
 			}
 		}
 		return null;
 	}
 
 	private void showQuestion(Question q) {
 		Answer[] ans = q.getAnswers(this.answers);
 
 		int questionCount = questions.size();
 		int questionIndex = indexOfQuestion(q);
 
 		Intent intent = getQuestionIntent(
 				q,
 				ans,
 				questionCount,
 				questionIndex
 		);
 
 		intent.putExtra(XMLQAQuestion.EXTRA_TOTAL_QUESIONS, questionCount);
 		intent.putExtra(XMLQAQuestion.EXTRA_QUESTION_INDEX, questionIndex);
 		intent.putExtra(XMLQAQuestion.EXTRA_QUESTION, q);
 		intent.putExtra(XMLQAQuestion.EXTRA_ANSWERS, ans);
 
 		startActivityForResult(intent, QUESTION_ACTIVITY);
 	}
 
 	protected abstract Intent getQuestionIntent(Question question, Answer[] answers, int totalQuestions, int questionIndex);
 
 	protected int indexOfQuestion(Question question) {
 		int i = 0;
 		for(Question q: questions.values()) {
 			if(q == question) {
 				return i;
 			}
 			++i;
 		}
 		return -1;
 	}
 
 	public static class Questionare {
 		public String title;
 		public String desc;
 		public String content;
 	}
 
 	public static class Question implements Parcelable {
 		public String id;
 		public String title;
 		public String desc;
 		public String[] answerIds = new String[]{};
 
 		public static final Parcelable.Creator<Question> CREATOR
 			= new Parcelable.Creator<XMLQAManager.Question>() {
 				@Override
 				public Question createFromParcel(Parcel source) {
 					return new Question(source);
 				}
 
 				@Override
 				public Question[] newArray(int size) {
 					return new Question[size];
 				}
 			};
 
 		public Question() {
 
 		}
 
 		private Question(Parcel in) {
 			this.id = in.readString();
 			this.title = in.readString();
 			this.desc = in.readString();
 			this.answerIds = new String[in.readInt()];
 			in.readStringArray(this.answerIds);
 		}
 
 		@Override
 		public int describeContents() {
 			return 0;
 		}
 
 		@Override
 		public void writeToParcel(Parcel dest, int flags) {
 			dest.writeString(this.id);
 			dest.writeString(this.title);
 			dest.writeString(this.desc);
 			dest.writeInt(this.answerIds.length);
 			dest.writeStringArray(this.answerIds);
 		}
 
 		private Answer[] getAnswers(HashMap<String,Answer> answers) {
 			ArrayList<Answer> ans = new ArrayList<Answer>();
 			for(int i = 0; i < answerIds.length; ++i) {
 				Answer answer = answers.get(answerIds[i]);
 				if(answer != null) {
 					ans.add(answer);
 				}
 			}
 			return ans.toArray(new Answer[ans.size()]);
 		}
 	}
 
 	public static class Answer implements Parcelable {
 		public String id;
 		public String title;
 		public String desc;
 		public int value;
 
 		public static final Parcelable.Creator<Answer> CREATOR
 			= new Parcelable.Creator<XMLQAManager.Answer>() {
 				@Override
 				public Answer createFromParcel(Parcel source) {
 					return new Answer(source);
 				}
 
 				@Override
 				public Answer[] newArray(int size) {
 					return new Answer[size];
 				}
 			};
 
 		public Answer() {
 
 		}
 
 		private Answer(Parcel in) {
 			this.id = in.readString();
 			this.title = in.readString();
 			this.desc = in.readString();
 			this.value = in.readInt();
 		}
 
 		@Override
 		public int describeContents() {
 			return 0;
 		}
 
 		@Override
 		public void writeToParcel(Parcel dest, int flags) {
 			dest.writeString(this.id);
 			dest.writeString(this.title);
 			dest.writeString(this.desc);
 			dest.writeInt(this.value);
 		}
 	}
 
 
 	public static class QAAdapter extends BaseAdapter {
 		private int layoutResId;
 		private LayoutInflater layoutInflater;
 		private LinkedHashMap<String, Question> questions;
 		private LinkedHashMap<String, Answer[]> answers;
 		private ArrayList<Question> questionsList;
 
 		public QAAdapter(Context c, int layoutResId, LinkedHashMap<String,Question> questions, LinkedHashMap<String,Answer[]> answers) {
 			this.layoutResId = layoutResId;
 			this.questions = questions;
 			this.answers = answers;
 			this.layoutInflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 			this.questionsList = new ArrayList<Question>(this.questions.values());
 		}
 
 		@Override
 		public int getCount() {
 			return this.questions.size();
 		}
 
 		@Override
 		public Question getItem(int arg0) {
 			return this.questionsList.get(arg0);
 		}
 
 		public Answer[] getAnswers(int arg0) {
 			//return this.answersList.get(arg0);
 			return this.answers.get(this.getItem(arg0).id);
 		}
 
 		@Override
 		public long getItemId(int arg0) {
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			Question question = getItem(position);
 			Answer[] answers = getAnswers(position);
 
 			if(convertView == null) {
 				convertView = this.layoutInflater.inflate(layoutResId, null);
 			}
 
 			View view1 = convertView.findViewById(android.R.id.text1);
 			View view2 = convertView.findViewById(android.R.id.text2);
 			if(view1 != null) {
 				((TextView)view1).setText(question.title);
 			}
 			if(view2 != null) {
 				String answerValue = "";
 				if(answers != null && answers.length > 0) {
 					answerValue = ((int)answers[0].value)+"";
 				}
 				((TextView)view2).setText(answerValue);
 			}
 
 			return convertView;
 		}
 	}
 }
