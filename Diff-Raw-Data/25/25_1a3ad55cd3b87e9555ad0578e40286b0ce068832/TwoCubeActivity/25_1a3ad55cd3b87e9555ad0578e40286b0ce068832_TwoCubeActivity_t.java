 package com.twocube;
 
import java.io.File;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.simple.JSONValue;
 
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.MemoryCacheAware;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;
 import com.twocube.JSONHandler.OnResponseReceivedListener;
 import com.twocube.entities.Survey;
 import com.twocube.entities.SurveyQuestion;
 
 import android.app.Activity;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.Editable;
 import android.text.InputType;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.DatePicker.OnDateChangedListener;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 public class TwoCubeActivity extends Activity {
 	JSONHandler jHandler,jSubmitHandler;
 	Handler mHandler;
 	String jsonString= "{\"Id\":2,\"surveyTitle\":\"Sample Survey\",\"surveyDescription\":\"A Random Sample Survey. This is just a sample of how json can be used to render a survey.\",\"surveyStatus\":false,\"surveyCreated\":\"\",\"surveyStartDate\":\"\",\"surveyEndDate\":\"\",\"surveyQuestionList\":[{\"Id\":3,\"surveyQuestionType\":0,\"surveyQuestionIsCompulsory\":false,\"surveyQuestionTitle\":\"What is your favourite color?\",\"surveyQuestionOptionList\":[{\"Id\":4,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"White\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":5,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"Yellow\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":6,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"Blue\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":7,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"Green\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":8,\"surveyQuestionOptionType\":2,\"surveyQuestionOptionTitle\":\"Enter another color:\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null}],\"surveyQuestionResponseList\":null},{\"Id\":11,\"surveyQuestionType\":0,\"surveyQuestionIsCompulsory\":false,\"surveyQuestionTitle\":\"Who is the most hardworking person in our team?\",\"surveyQuestionOptionList\":[{\"Id\":12,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"June\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":13,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"Wei Leng\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":14,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"Hong Jing\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":15,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"Xu Ai\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":16,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"Wesley\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null}],\"surveyQuestionResponseList\":null},{\"Id\":19,\"surveyQuestionType\":0,\"surveyQuestionIsCompulsory\":false,\"surveyQuestionTitle\":\"And the best phone is\",\"surveyQuestionOptionList\":[{\"Id\":20,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"Galaxy S 3\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":21,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"Galaxy S 2\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":22,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"Galaxy S\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":23,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"HTC One X\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":24,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"Galaxy Nexus\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":25,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"Motorola Droid Razr Maxx\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null}],\"surveyQuestionResponseList\":null},{\"Id\":28,\"surveyQuestionType\":1,\"surveyQuestionIsCompulsory\":false,\"surveyQuestionTitle\":\"Who is the most hardworking person in our team? (You can choose more than 1 answer)\",\"surveyQuestionOptionList\":[{\"Id\":29,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"June\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":30,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"June\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":31,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"June\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":32,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"June\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":33,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"June\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":34,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"June\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":35,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"June\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":36,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"June\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":37,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"June\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null}],\"surveyQuestionResponseList\":null},{\"Id\":44,\"surveyQuestionType\":2,\"surveyQuestionIsCompulsory\":false,\"surveyQuestionTitle\":\"How hardworking is June?\",\"surveyQuestionOptionList\":[{\"Id\":45,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"Hardworkingness\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null}],\"surveyQuestionResponseList\":null},{\"Id\":48,\"surveyQuestionType\":3,\"surveyQuestionIsCompulsory\":false,\"surveyQuestionTitle\":\"How young is this pretty young star? Numeric input.\",\"surveyQuestionOptionList\":[{\"Id\":49,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"http://graph.facebook.com/weileng.peh/picture?type=large\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null}],\"surveyQuestionResponseList\":null},{\"Id\":52,\"surveyQuestionType\":4,\"surveyQuestionIsCompulsory\":false,\"surveyQuestionTitle\":\"Or the birthday of this talented actor/singer? Date input.\",\"surveyQuestionOptionList\":[{\"Id\":53,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"http://graph.facebook.com/197371292379/picture?type=large\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null}],\"surveyQuestionResponseList\":null},{\"Id\":56,\"surveyQuestionType\":5,\"surveyQuestionIsCompulsory\":false,\"surveyQuestionTitle\":\"Which picture is the odd one out?\",\"surveyQuestionOptionList\":[{\"Id\":57,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"http://graph.facebook.com/alexei.sourin/picture?type=square\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":58,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"http://graph.facebook.com/kuiyu.chang/picture?type=square\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":59,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"http://graph.facebook.com/bengkoon.ng/picture?type=square\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":60,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"http://graph.facebook.com/bingsheng.he/picture?type=square\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null},{\"Id\":61,\"surveyQuestionOptionType\":0,\"surveyQuestionOptionTitle\":\"http://graph.facebook.com/limws.brandon/picture?type=square\",\"surveyQuestionOptionTitleType\":0,\"surveyQuestionOptionRange\":0,\"surveyQuestionOptionMinText\":null,\"surveyQuestionOptionMaxText\":null}],\"surveyQuestionResponseList\":null}],\"respondentList\":null}";
 	String surveyId;
 	LayoutInflater inflater;
 	Survey survey;
	
	ImageLoader imageLoader;// = ImageLoader.getInstance();
	DisplayImageOptions options;
	File cacheDir;
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         Uri data = getIntent().getData();
         //data.getQueryParameter("s").toString();
         inflater = ((Activity) this).getLayoutInflater();
         jHandler = new JSONHandler();
         jSubmitHandler = new JSONHandler();
         mHandler = new Handler();
        
        imageLoader = ImageLoader.getInstance();
		cacheDir = StorageUtils.getCacheDirectory(this);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).defaultDisplayImageOptions(DisplayImageOptions.createSimple()).denyCacheImageMultipleSizesInMemory().offOutOfMemoryHandling().build();
		imageLoader.init(config);
		options = new DisplayImageOptions.Builder().cacheOnDisc().build();
		
		
         jHandler.setOnResponseReceivedListener(new OnResponseReceivedListener() {
 			
 			@Override
 			public void onResponseReceived(final String receivedString, boolean success) {
 				mHandler.post(new Runnable() {
 					
 					@Override
 					public void run() {
 						createSurvey(receivedString);
 						
 					}
 				});
 				
 				
 			}
 			
 			@Override
 			public void onIOException() {
 				// TODO Auto-generated method stub
 				
 			}
 		});
         jSubmitHandler.setOnResponseReceivedListener(new OnResponseReceivedListener() {
 			
 			@Override
 			public void onResponseReceived(String receivedString, boolean success) {
 				mHandler.post(new Runnable() {
 					
 					@Override
 					public void run() {
 						//createSurvey(receivedString);
 						System.exit(0);
 						
 					}
 				});
 				
 			}
 			
 			@Override
 			public void onIOException() {
 				// TODO Auto-generated method stub
 				
 			}
 		});
         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
         if(data!=null){
         	nameValuePairs.add(new BasicNameValuePair("Id", data.getQueryParameter("s")==null?"80":data.getQueryParameter("s")));
         	Log.w("jsonhandler", data.getQueryParameter("s"));
         	surveyId = data.getQueryParameter("s");
         }
         else nameValuePairs.add(new BasicNameValuePair("Id", "80"));
         jHandler.handleJSON(nameValuePairs, "http://twocube1.elasticbeanstalk.com/Services/SurveyControllerService.asmx/getSurveyById");
     }
     
     public void createSurvey(String jsonString){
     	try {
 			JSONObject jsonobj = new JSONObject(jsonString);
 			survey = new Survey();
 			TextView tvSurveyTitle =(TextView)findViewById(R.id.tv_title_main);
 			TextView tvSurveyDescription =(TextView)findViewById(R.id.tv_description_main);
 			tvSurveyTitle.setText(jsonobj.getString("surveyTitle"));
 			tvSurveyDescription.setText(jsonobj.getString("surveyDescription"));
 			JSONArray questionArray = new JSONArray(jsonobj.getString("surveyQuestionList"));
 			for (int i = 0; i < questionArray.length(); i++) {
 				JSONObject tempQuestion = questionArray.getJSONObject(i);
 				View v = inflater.inflate(R.layout.question_layout, null); 
 				LinearLayout llQuestion = (LinearLayout)v.findViewById(R.id.ll_question);
 				((TextView)v.findViewById(R.id.tv_title_question)).setText(tempQuestion.getString("surveyQuestionTitle"));
 				
 				if(tempQuestion.getInt("surveyQuestionType") == 0){
 					JSONArray optionArray = new JSONArray(tempQuestion.getString("surveyQuestionOptionList"));
 					RadioGroup rg = new RadioGroup(this);
 					int pos = i;
 					final SurveyQuestion sq =  new SurveyQuestion(0);
 					survey.getQuestionList().add(sq);
 					for (int j = 0; j < optionArray.length(); j++) {
 						JSONObject tempOption = optionArray.getJSONObject(j);
 						RadioButton rb = new RadioButton(this);
 						rb.setText(tempOption.getString("surveyQuestionOptionTitle"));
 						rg.addView(rb);
 						//CheckBox cb = new CheckBox(this);
 						//cb.setText(tempOption.getString("surveyQuestionOptionTitle"));
 						
 					}
 					rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 						
 						@Override
 						public void onCheckedChanged(RadioGroup group, int checkedId) {
 							if(checkedId == -1){}
 							else sq.setNumAns(group.indexOfChild(findViewById(group.getCheckedRadioButtonId())));
 							
 						}
 					});
 					llQuestion.addView(rg);
 				}else if(tempQuestion.getInt("surveyQuestionType") == 1){
 					JSONArray optionArray = new JSONArray(tempQuestion.getString("surveyQuestionOptionList"));
 					for (int j = 0; j < optionArray.length(); j++) {
 						JSONObject tempOption = optionArray.getJSONObject(j);
 						CheckBox cb = new CheckBox(this);
 						cb.setText(tempOption.getString("surveyQuestionOptionTitle"));
 						llQuestion.addView(cb);
 					}
 				}else if(tempQuestion.getInt("surveyQuestionType") == 2){
 					JSONArray optionArray = new JSONArray(tempQuestion.getString("surveyQuestionOptionList"));
 					final SurveyQuestion sq =  new SurveyQuestion(2);
 					survey.getQuestionList().add(sq);
 					for (int j = 0; j < optionArray.length(); j++) {
 						JSONObject tempOption = optionArray.getJSONObject(j);
 						SeekBar sb = new SeekBar(this);
 						sb.setMax(100);
 						sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 							
 							@Override
 							public void onStopTrackingTouch(SeekBar seekBar) {
 								// TODO Auto-generated method stub
 								
 							}
 							
 							@Override
 							public void onStartTrackingTouch(SeekBar seekBar) {
 								// TODO Auto-generated method stub
 								
 							}
 							
 							@Override
 							public void onProgressChanged(SeekBar seekBar, int progress,
 									boolean fromUser) {
 								sq.setNumAns(progress);
 								
 							}
 						});
 						//cb.setText(tempOption.getString("surveyQuestionOptionTitle"));
 						llQuestion.addView(sb);
 					}
 				}else if(tempQuestion.getInt("surveyQuestionType") == 3){
 					JSONArray optionArray = new JSONArray(tempQuestion.getString("surveyQuestionOptionList"));
 					final SurveyQuestion sq =  new SurveyQuestion(3);
 					survey.getQuestionList().add(sq);
 					for (int j = 0; j < optionArray.length(); j++) {
 						JSONObject tempOption = optionArray.getJSONObject(j);
 						final EditText et = new EditText(this);
 						et.setInputType(InputType.TYPE_CLASS_NUMBER);
 						et.addTextChangedListener(new TextWatcher() {
 							
 							@Override
 							public void onTextChanged(CharSequence s, int start, int before, int count) {
 								sq.setStrAns(et.getEditableText().toString());
 								Log.w("JSON", et.getEditableText().toString());
 								
 							}
 							
 							@Override
 							public void beforeTextChanged(CharSequence s, int start, int count,
 									int after) {
 								// TODO Auto-generated method stub
 								
 							}
 							
 							@Override
 							public void afterTextChanged(Editable s) {
 								// TODO Auto-generated method stub
 								
 							}
 						});
 						//cb.setText(tempOption.getString("surveyQuestionOptionTitle"));
 						llQuestion.addView(et);
 					}
 				}else if(tempQuestion.getInt("surveyQuestionType") == 4){
 					JSONArray optionArray = new JSONArray(tempQuestion.getString("surveyQuestionOptionList"));
 					final SurveyQuestion sq =  new SurveyQuestion(4);
 					survey.getQuestionList().add(sq);
 					for (int j = 0; j < optionArray.length(); j++) {
 						JSONObject tempOption = optionArray.getJSONObject(j);
 						final DatePicker dp = new DatePicker(this);
 						final Calendar c = Calendar.getInstance();
 						dp.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), new OnDateChangedListener() {
 							
 							@Override
 							public void onDateChanged(DatePicker view, int year, int monthOfYear,
 									int dayOfMonth) {
 								sq.setStrAns(Integer.toString(monthOfYear+1)+"/"+Integer.toString(dayOfMonth)+"/"+Integer.toString(year));
 								
 							}
 						});
 						//et.setInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
 						//cb.setText(tempOption.getString("surveyQuestionOptionTitle"));
 						dp.setOnClickListener(new OnClickListener() {
 							
 							@Override
 							public void onClick(View v) {
 								sq.setStrAns(Integer.toString(dp.getMonth()+1)+"/"+Integer.toString(dp.getDayOfMonth())+"/"+Integer.toString(dp.getYear()));
 								Log.w("dp", Integer.toString(dp.getMonth()+1)+"/"+Integer.toString(dp.getDayOfMonth())+"/"+Integer.toString(dp.getYear()));
 							}
 						});
 						llQuestion.addView(dp);
 					}
 				}else if(tempQuestion.getInt("surveyQuestionType") == 6||tempQuestion.getInt("surveyQuestionType") == 7){
 					JSONArray optionArray = new JSONArray(tempQuestion.getString("surveyQuestionOptionList"));
 					final SurveyQuestion sq =  new SurveyQuestion(3);
 					survey.getQuestionList().add(sq);
 					for (int j = 0; j < optionArray.length(); j++) {
 						JSONObject tempOption = optionArray.getJSONObject(j);
 						final EditText et = new EditText(this);
 						//et.setInputType(InputType.TYPE_CLASS_NUMBER);
 						et.addTextChangedListener(new TextWatcher() {
 							
 							@Override
 							public void onTextChanged(CharSequence s, int start, int before, int count) {
 								sq.setStrAns(et.getEditableText().toString());
 								Log.w("JSON", et.getEditableText().toString());
 								
 							}
 							
 							@Override
 							public void beforeTextChanged(CharSequence s, int start, int count,
 									int after) {
 								// TODO Auto-generated method stub
 								
 							}
 							
 							@Override
 							public void afterTextChanged(Editable s) {
 								// TODO Auto-generated method stub
 								
 							}
 						});
 						//cb.setText(tempOption.getString("surveyQuestionOptionTitle"));
 						llQuestion.addView(et);
 					}
 				}
 				
 				((LinearLayout)findViewById(R.id.ll_main)).addView(v);
 				
 			}
 			Button bnSubmit = new Button(this);
 			bnSubmit.setText("Submit");
 			//final 
 
 			bnSubmit.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					Map main=new LinkedHashMap();
 					Map obj=new LinkedHashMap();
 					int i = 0;
 					for(SurveyQuestion question:survey.getQuestionList()){
 						if(question.getQuestionType()==0)
 							obj.put(Integer.toString(i), question.getNumAns());
 						else if(question.getQuestionType()==2)
 							obj.put(Integer.toString(i), question.getNumAns());
 						else if(question.getQuestionType()==3)
 							obj.put(Integer.toString(i), question.getStrAns());
 						else if (question.getQuestionType()==4)
 							obj.put(Integer.toString(i), question.getStrAns());
 						i++;
 					}
 					main.put("s", obj);
 					main.put("surveyId",surveyId);
 					main.put("surveyBrowserBrowser","Mobile App");
 					main.put("surveyBrowserVersion","1");
 					main.put("surveyBrowserOS","Android");
 					main.put("surveyLocaleLang","en");
 					main.put("surveyLocaleCountry","us");
 					main.put("surveyLocationCountryCode","US");
					main.put("surveyTime","34543");
 					StringWriter out = new StringWriter();
 					   try {
 						JSONValue.writeJSONString(main, out);
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					   String jsonText = out.toString();
 					   Log.w("JSON", jsonText);
 					   List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 					   nameValuePairs.add(new BasicNameValuePair("jsonString", jsonText));
 					   jSubmitHandler.handleJSON(nameValuePairs, "http://twocube1.elasticbeanstalk.com/Services/SurveyControllerService.asmx/submitSurvey");
 				}
 			});
 			((LinearLayout)findViewById(R.id.ll_main)).addView(bnSubmit);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
 }
