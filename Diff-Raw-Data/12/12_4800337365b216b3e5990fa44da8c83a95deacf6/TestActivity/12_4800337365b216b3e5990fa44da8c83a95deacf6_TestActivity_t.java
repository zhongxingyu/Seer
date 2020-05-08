 package fm.moe.android.activity;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import fm.moe.android.R;
 import fm.moe.android.util.Utils;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import moefou4j.Moefou;
 import moefou4j.conf.Configuration;
 import moefou4j.http.HttpResponse;
 
 public class TestActivity extends Activity implements TextWatcher, View.OnClickListener {
 
 	private TextView mResultView;
 	private EditText mEditUrl;
 	private View mGoButton;
 	
 	private Moefou mMoefou;
 	private RequestTask mTask;
 
 	public void afterTextChanged(Editable s) {
 
 	}
 	
 	public void beforeTextChanged(CharSequence s, int length, int start, int end) {
 
 	}
 
 	public void onContentChanged() {
 		super.onContentChanged();
 		mEditUrl = (EditText) findViewById(R.id.url);
 		mResultView = (TextView) findViewById(R.id.result);
 		mGoButton = findViewById(R.id.go);
 		mMoefou = Utils.getMoefouInstance(this);
 	}
 
 	public void onClick(View v) {
 		final String url = mEditUrl.getText().toString();
 		if (mTask != null) {
 			mTask.cancel(true);
 		}
 		mTask = new RequestTask(url, mMoefou, mResultView);
 		mTask.execute();
 	}
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.test);
 		mEditUrl.addTextChangedListener(this);
 		mGoButton.setOnClickListener(this);
 	}
 
 	public void onTextChanged(CharSequence s, int length, int start, int end) {
 		final String string = s.toString();
 		final Configuration conf = mMoefou.getConfiguration();
 		mGoButton.setEnabled(string.startsWith(conf.getMoefouBaseURL()) || string.startsWith(conf.getMoeFMBaseURL()));
 	}
 	
 	static final class RequestTask extends AsyncTask<Void, Void, Object> {
 
 		private final String url;
 		private final Moefou moefou;
 		private final TextView view;
 		RequestTask(final String url, final Moefou moefou, final TextView view) {
 			this.url = url;
 			this.moefou = moefou;
 			this.view = view;
 		}
 		
 		protected Object doInBackground(Void... args) {
 			try {
 				final HttpResponse resp = moefou.rawGet(url);
 				final String resp_string = resp.asJSONObject().toString(1);
 				return "Response: " + resp.getStatusCode() + "\n" + resp_string;
 			} catch (final Throwable t) {
 				return t;
 			}
 		}
 		
 		protected void onPreExecute() {
 			view.setText(null);
 			view.setText("Making request to " + url + "\n");
 		}
 		
 		protected void onPostExecute(Object result) {
			view.append("------------------------------------\n");
 			if (result instanceof Throwable) {
 				final StringWriter sw = new StringWriter();
 				final PrintWriter pw = new PrintWriter(sw);
 				((Throwable)result).printStackTrace(pw);
 				view.append("Stack trace:\n");
 				view.append(sw.toString());
 			} else {
 				view.append(String.valueOf(result));
 			}
 		}
 		
 		
 	}
 }
