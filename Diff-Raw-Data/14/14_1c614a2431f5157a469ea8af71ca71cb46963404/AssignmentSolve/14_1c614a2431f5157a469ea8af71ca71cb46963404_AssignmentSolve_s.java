 package cz.kinst.jakub.coursemanager;
 
 import java.io.File;
 import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnMultiChoiceClickListener;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.text.Editable;
 import android.text.InputType;
 import android.text.TextWatcher;
import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 import cz.kinst.jakub.coursemanager.utils.QuestionTag;
 import cz.kinst.jakub.coursemanager.utils.Utils;
 
 public class AssignmentSolve extends CMActivity {
 
 	/**
 	 * UID for serialization
 	 */
 	private static final long serialVersionUID = -5079492776147483408L;
 	protected static final int SELECT_FILE = 0;
 	private int aid;
 	ArrayList<QuestionTag> tags = new ArrayList<QuestionTag>();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.aid = getIntent().getExtras().getInt("aid");
 		setContentView(R.layout.assignment_solve);
 		reload();
 	}
 
 	@Override
 	protected JSONObject reloadWork() throws JSONException {
 		ArrayList<NameValuePair> args = new ArrayList<NameValuePair>();
 		args.add(new BasicNameValuePair("aid", String.valueOf(this.aid)));
 		return courseManagerCon.getAction("assignment", "solve", args,
 				new ArrayList<NameValuePair>());
 	}
 
 	@Override
 	public void gotData(JSONObject data) throws JSONException {
 		ArrayList<JSONObject> questions = Utils.getJSONObjectArray(data
 				.getJSONArray("questions"));
 		Date realEndTime = Utils.getDateFromDBString(data
 				.getString("realEndTime"));
 
 		JSONObject assg = data.getJSONObject("assignment");
 		TextView name = (TextView) findViewById(R.id.name);
 		final TextView date = (TextView) findViewById(R.id.date);
 		TextView description = (TextView) findViewById(R.id.description);
 
 		name.setText(getText(R.string.solving) + " " + assg.getString("name"));
 		description.setText(assg.getString("description"));
 
 		Date now = new Date();
 		date.setText("");
 		new CountDownTimer(realEndTime.getTime() - now.getTime(), 1000) {
 			@Override
 			public void onTick(long millisUntilFinished) {
 				long time = millisUntilFinished / 1000;
 				String seconds = Integer.toString((int) (time % 60));
 				String minutes = Integer.toString((int) ((time % 3600) / 60));
 				String hours = Integer.toString((int) (time / 3600));
 				for (int i = 0; i < 2; i++) {
 					if (seconds.length() < 2) {
 						seconds = "0" + seconds;
 					}
 					if (minutes.length() < 2) {
 						minutes = "0" + minutes;
 					}
 					if (hours.length() < 2) {
 						hours = "0" + hours;
 					}
 				}
 
 				date.setText(getText(R.string.remaining_time) + ": " + hours
 						+ ":" + minutes + ":" + seconds);
 			}
 
 			@Override
 			public void onFinish() {
 				submitForm();
 			}
 		}.start();
 
 		JSONObject currentAnswers = data.getJSONObject("currentAnswers");
 
 		final LinearLayout assignment = (LinearLayout) findViewById(R.id.assignment);
 		assignment.removeAllViews();
 
 		for (final JSONObject question : questions) {
 			String type = question.getString("type");
 			final String id = question.getString("id");
 
 			if (type.equals("text")) {
 				TextView label = new TextView(this);
 				label.setText(question.getString("label"));
 				EditText edit = new EditText(this);
 				final QuestionTag tag = new QuestionTag(id, type, "");
 				tags.add(tag);
 				edit.setText(currentAnswers.getString(id));
 				edit.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
 						LayoutParams.WRAP_CONTENT));
 				edit.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
 				edit.setSingleLine(false);
 				edit.addTextChangedListener(new TextWatcher() {
 					@Override
 					public void onTextChanged(CharSequence s, int start,
 							int before, int count) {
 					}
 
 					@Override
 					public void beforeTextChanged(CharSequence s, int start,
 							int count, int after) {
 					}
 
 					@Override
 					public void afterTextChanged(Editable s) {
 						tag.setValue(s.toString());
 					}
 				});
 				assignment.addView(label);
 				assignment.addView(edit);
 			}
 			if (type.equals("textarea")) {
 				TextView label = new TextView(this);
 				label.setText(question.getString("label"));
 				EditText edit = new EditText(this);
 				final QuestionTag tag = new QuestionTag(id, type, "");
 				tags.add(tag);
 				edit.setText(currentAnswers.getString(id));
 				edit.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
 						LayoutParams.WRAP_CONTENT));
 				edit.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
 				edit.setSingleLine(false);
 				edit.addTextChangedListener(new TextWatcher() {
 					@Override
 					public void onTextChanged(CharSequence s, int start,
 							int before, int count) {
 					}
 
 					@Override
 					public void beforeTextChanged(CharSequence s, int start,
 							int count, int after) {
 					}
 
 					@Override
 					public void afterTextChanged(Editable s) {
 						tag.setValue(s.toString());
 					}
 				});
 				assignment.addView(label);
 				assignment.addView(edit);
 			}
 			if (type.equals("radio")) {
 				TextView label = new TextView(this);
 				label.setText(question.getString("label"));
 				RadioGroup radioGroup = new RadioGroup(this);
 				final QuestionTag tag = new QuestionTag(id, type, "");
 				tags.add(tag);
 				int currentPos = (currentAnswers.getInt(id));
 				final String[] choices = question.getString("choices").split(
 						"#");
 				int i = 0;
 				for (String choice : choices) {
 					RadioButton radio = new RadioButton(this);
 					radio.setText(choice);
 					radio.setTag(i + "");
 					if (i == currentPos) {
 						radio.setChecked(true);
 					}
 					radioGroup.addView(radio);
 					i++;
 				}
 				radioGroup
 						.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 							@Override
 							public void onCheckedChanged(RadioGroup group,
 									int checkedId) {
 								String t = (String) ((RadioButton) group
 										.findViewById(checkedId)).getTag();
 								tag.setValue(t);
 							}
 						});
 				assignment.addView(label);
 				assignment.addView(radioGroup);
 			}
 			if (type.equals("multi")) {
 				final String[] choices = question.getString("choices").split(
 						"#");
 				TextView label = new TextView(this);
 				label.setText(question.getString("label"));
 
 				final boolean[] selected = new boolean[choices.length];
 				JSONArray curAnswers = currentAnswers.getJSONArray(id);
 				for (@SuppressWarnings("unused")
 				boolean b : selected) {
 					b = false;
 				}
 				for (int i = 0; i < curAnswers.length(); i++) {
 					selected[curAnswers.getInt(i)] = true;
 				}
 
 				final Button select = new Button(this);
 				select.setText(getText(R.string.selected) + ": "
 						+ String.valueOf(countSelected(selected)));
 				final QuestionTag tag = new QuestionTag(id, type, new String[0]);
 				tags.add(tag);
 
 				final OnMultiChoiceClickListener listener = new OnMultiChoiceClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which,
 							boolean isChecked) {
 						selected[which] = isChecked;
 						tag.setValues(Utils.getTruePositions(selected));
 						select.setText(getText(R.string.selected) + ": "
 								+ String.valueOf(countSelected(selected)));
 					}
 				};
 
 				select.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						AlertDialog.Builder builder = new AlertDialog.Builder(
 								AssignmentSolve.this);
 						try {
 							builder.setTitle(question.getString("label"));
 							builder.setMultiChoiceItems(choices, selected,
 									listener);
 						} catch (JSONException e) {
 							e.printStackTrace();
 						}
 						AlertDialog alert = builder.create();
 
 						alert.show();
 					}
 				});
 				assignment.addView(label);
 				assignment.addView(select);
 			}
 			if (type.equals("file")) {
 				TextView label = new TextView(this);
 				label.setText(question.getString("label"));
 				final QuestionTag tag = new QuestionTag(id, type, "");
 				tags.add(tag);
 				Button pickButton = new Button(this);
 				pickButton.setText(R.string.select_file);
 				pickButton.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
 						intent.setType("file/*");
 						try {
							startActivityForResult(intent, Integer.parseInt(id));							
 						} catch (Exception e) {
							Toast.makeText(AssignmentSolve.this, R.string.no_file_manager, Toast.LENGTH_LONG).show();
 						}
 					}
 				});
 				assignment.addView(label);
 				assignment.addView(pickButton);
 			}
 		}
 		Button submitButton = new Button(this);
 		submitButton.setText(R.string.submit);
 		submitButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				submitForm();
 			}
 		});
 		assignment.addView(submitButton);
 
 	}
 
 	private void submitForm() {
 		final ArrayList<NameValuePair> postArgs = new ArrayList<NameValuePair>();
 		final ArrayList<NameValuePair> getArgs = new ArrayList<NameValuePair>();
 		getArgs.add(new BasicNameValuePair("aid", String.valueOf(aid)));
 		final HashMap<String, File> files = new HashMap<String, File>();
 
 		for (QuestionTag tag : tags) {
 			if (tag.getType().equals("text")
 					|| tag.getType().equals("textarea")
 					|| tag.getType().equals("radio")) {
 				postArgs.add(new BasicNameValuePair(tag.getId() + "_", tag
 						.getValue()));
 			}
 			if (tag.getType().equals("file")) {
 				if (!tag.getValue().equals("")) {
 					files.put(tag.getId() + "_", new File(tag.getValue()));
 				} else {
 					try {
 						files.put(tag.getId() + "_",
 								File.createTempFile("no-file", "file"));
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			if (tag.getType().equals("multi")) {
 				int i = 0;
 				for (String val : tag.getValues()) {
 					postArgs.add(new BasicNameValuePair(tag.getId() + "_[" + i
 							+ "]", val));
 					i++;
 				}
 			}
 		}
 
 		new AsyncTask<Void, Void, Void>() {
 			@Override
 			protected void onPreExecute() {
 				setProgressBarIndeterminateVisibility(true);
 			};
 
 			@Override
 			protected Void doInBackground(Void... params) {
 				courseManagerCon.sendForm("assignment", "solve", "solveForm",
 						getArgs, postArgs, files);
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(Void result) {
 				setProgressBarIndeterminateVisibility(false);
 				courseManagerCon.toastFlashes();
 				finish();
 			};
 		}.execute();
 	}
 
 	public String getSelectedRadioValue(RadioGroup g) {
 
 		// Returns an integer which represents the selected radio button's ID
 		int selected = g.getCheckedRadioButtonId();
 
 		// Gets a reference to our "selected" radio button
 		RadioButton b = (RadioButton) findViewById(selected);
 
 		// Now you can get the text or whatever you want from the "selected"
 		// radio button
 		return b.getText().toString();
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (resultCode == Activity.RESULT_OK) {
 			Uri filePath = data.getData();
			Log.e("test",Utils.getRealPathFromURI(filePath, this));
 			for (QuestionTag tag : tags) {
 				if (tag.getId().equals(String.valueOf(requestCode))) {
 					tag.setValue(Utils.getRealPathFromURI(filePath, this));
 				}
 			}
 		}
 	}
 
 	public int countSelected(boolean[] array) {
 		int n = 0;
 		for (boolean b : array) {
 			if (b) {
 				n++;
 			}
 		}
 		return n;
 	}
 
 }
