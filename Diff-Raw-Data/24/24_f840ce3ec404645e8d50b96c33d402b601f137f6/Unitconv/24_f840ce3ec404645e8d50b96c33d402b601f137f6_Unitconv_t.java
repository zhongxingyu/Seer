 package ee.ioc.phon.android.unitconv;
 
 import android.os.Bundle;
 import android.speech.RecognizerIntent;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.inputmethod.EditorInfo;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.TextView.OnEditorActionListener;
 
 import android.app.ProgressDialog;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.grammaticalframework.Linearizer;
 import org.grammaticalframework.PGF;
 import org.grammaticalframework.PGFBuilder;
 import org.grammaticalframework.Parser;
 import org.grammaticalframework.parser.ParseState;
 import org.grammaticalframework.Trees.Absyn.Tree;
 
 public class Unitconv extends AbstractRecognizerActivity {
 
 	// Set of non-standard extras that RecognizerIntentActivity supports
 	public static final String EXTRA_GRAMMAR_JSGF = "EXTRA_GRAMMAR_JSGF";
 
 	// These are the concrete languages that we expect to find in the PGF
 	private static final String P_LANG = "UnitconvEst";
 	private static final String L_LANG = "UnitconvApp";
 
 	private ListView mListView;
 	private EditText mEt;
 	private PGF mPGF;
 	private Intent mIntent;
 	private ImageButton speakButton;
 	private Context mContext;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.main);
 
 		mEt = (EditText) findViewById(R.id.edittext);
 
 		speakButton = (ImageButton) findViewById(R.id.buttonMicrophone);
 
 		if (getRecognizers().size() == 0) {
 			speakButton.setEnabled(false);
 			toast(getString(R.string.errorRecognizerNotPresent));
 		}
 
 		new LoadPGFTask().execute();
 
 		mListView = (ListView) findViewById(R.id.list);
 
 		mEt.setOnEditorActionListener(new OnEditorActionListener() {
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 				if (actionId == EditorInfo.IME_ACTION_GO) {
 					new TranslateTask().execute(mEt.getText().toString());
 				}
 				return true;
 			}
 		});
 
 		mListView.setClickable(true);
 
 		mListView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				Object o = mListView.getItemAtPosition(position);
 				// TODO: Why does Eclipse underline it?
 				Map<String, String> map = (Map<String, String>) o;
 				Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
 				search.putExtra(SearchManager.QUERY, map.get("in"));
 				startActivity(search);
 			}
 		});
 
 		mIntent = createRecognizerIntent(getString(R.string.defaultGrammar));
 		mContext = this;
 	}
 
 
 	public void recognize(View v) {
 		launchRecognizerIntent(mIntent);
 	}
 
 
 	@Override
 	protected void onSuccess(List<String> matches) {
 		if (matches.isEmpty()) {
 			toast("ERROR: empty list was returned not an error message.");
 		} else {
 			// TODO: support multiple results
 			String result = matches.iterator().next();
 			mEt.setText(result);
 			new TranslateTask().execute(result);
 		}
 	}
 
 
 	private static Intent createRecognizerIntent(String grammar) {
 		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 		intent.putExtra(EXTRA_GRAMMAR_JSGF, grammar);
 		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
 		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say e.g.: kaks meetrit jalgades");
 		return intent;
 	}
 
 
 	private class LoadPGFTask extends AsyncTask<Void, Void, PGF> {
 
 		private ProgressDialog mProgress;
 
 		protected void onPreExecute() {
			mProgress = ProgressDialog.show(Unitconv.this, "", getString(R.string.progressLoadingGrammar), true);
 		}
 
 		protected PGF doInBackground(Void... a) {
 			int pgf_res = R.raw.unitconv;
 			InputStream is = getResources().openRawResource(pgf_res);
 			try {
 				PGF pgf = PGFBuilder.fromInputStream(is, new String[] {P_LANG, L_LANG});
 				return pgf;
 			} catch (Exception e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 		protected void onPostExecute(PGF result) {
 			mPGF = result;
 			if (mProgress != null) {
 				mProgress.dismiss();
 			}
 		}
 	}
 
 
 	private class TranslateTask extends AsyncTask<String, Void, List<Map<String, String>>> {
 
 		private ProgressDialog mProgress;
 
 		protected void onPreExecute() {
			mProgress = ProgressDialog.show(Unitconv.this, "", getString(R.string.progressExecuting), true);
 		}
 
 		protected List<Map<String, String>> doInBackground(String... s) {
 			try {
 				// Creating a Parser object for the P_LANG concrete grammar
 				Parser mParser = new Parser(mPGF, P_LANG);
 				// Simple tokenization
 				String[] tokens = s[0].split(" ");
 				// Parsing the tokens
 				ParseState mParseState = mParser.parse(tokens);
 				Tree[] trees = (Tree[]) mParseState.getTrees();
 
 				int numberOfTrees = trees.length;
 				List<Map<String, String>> translations = new ArrayList<Map<String, String>>();
 
 				if (numberOfTrees == 0) {
 				} else {
 					// Creating a Linearizer object for the L_LANG concrete grammar
 					// Linearizing all the trees (i.e. the ambiguity)
 					Linearizer mLinearizer = new Linearizer(mPGF, L_LANG);
 					for (int i = 0; i < numberOfTrees; i++) {
 						Map<String, String> map = new HashMap<String, String>();
 						Converter conv = null;
 						try {
 							String t = mLinearizer.linearizeString(trees[i]);
 							conv = new Converter(t);
 							map.put("in", conv.getIn());
 							map.put("out", conv.getOut());
 						} catch (Exception e) {
 							if (conv == null) {
 								map.put("in", e.getMessage());
 							} else {
 								map.put("in", conv.getIn());
 								map.put("message", e.getMessage());
 							}
 							map.put("out", getString(R.string.error));
 						}
 						translations.add(map);
 					}
 				}
 				return translations;
 			} catch (Exception e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 
 		protected void onPostExecute(List<Map<String, String>> result) {
 			if (mProgress != null) {
 				mProgress.dismiss();
 			}
 			if (result.isEmpty()) {
 				toast(getString(R.string.warningParserInputNotSupported));
 			} else {
 				mListView.setAdapter(new SimpleAdapter(
 						mContext,
 						result,
 						R.layout.list_item_unitconv_result,
 						new String[] { "in", "out", "message" },
 						new int[] { R.id.list_item_in, R.id.list_item_out, R.id.list_item_message}
 				)
 				);
 
 			}
 		}
 	}
}
