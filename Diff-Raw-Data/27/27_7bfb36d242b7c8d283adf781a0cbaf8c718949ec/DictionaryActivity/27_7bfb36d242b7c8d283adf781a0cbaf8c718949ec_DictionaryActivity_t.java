 package cz.havlena.dictionary.gui;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 import cz.havlena.dictionary.DictionaryElement;
 import cz.havlena.dictionary.DictionaryService;
 import cz.havlena.dictionary.IDictionaryService;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.Editable;
 import android.text.Html;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class DictionaryActivity extends Activity {
 	
 	private static final String TAG = "DictionaryActivity";
 	private static final String DICTIONARY_FILE = "gcide-entries.xml";
 	private static final int FORMAT = DictionaryService.FORMAT_HTML;
 	
 	private static final int SEARCHING_FOUND = 1;
 	private static final int SEARCHING_COMPLETED = 2;
 	private static final int SEARCHING_STARTED = 3;
 	private static final int SEARCHING_ERROR = -1;
 	
 	private DictionaryService mService;
 	
 	private TextView txtResult;
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         txtResult = (TextView) findViewById(R.id.txtResult);
         EditText edtSearch = (EditText) findViewById(R.id.edtSearch);
         edtSearch.addTextChangedListener(new SearchHandler());
     }
     
     private Handler mHandler = new Handler() {
     	private boolean xmlError = false;
     	
     	@Override
     	public void handleMessage(Message msg) {
     		switch(msg.what) {
     		case SEARCHING_STARTED:
     			Log.w(TAG, "Searching started");
     			txtResult.setText("");
     			break;
     			
     		case SEARCHING_FOUND:
     			switch(FORMAT) {
     			case DictionaryService.FORMAT_HTML:
 	    			String element = (String) msg.obj;
 	    			if(element != null) {
 		    			if(!xmlError)
 		    				element = Html.fromHtml(element).toString();
 		   
 		    				txtResult.setText(txtResult.getText() + element + "\n" +
 		    						"----------------------------------------------" + "\n");
 		    		}
 	    			break;
 	    			
     			case DictionaryService.FORMAT_ELEMENT:
     				DictionaryElement el = (DictionaryElement) msg.obj;
 	    			if(el != null) {
 	    				txtResult.setText(txtResult.getText() + el.key + "\n"/* + el.definition + "\n" + 
 	    						el.expression + "\n" + el.senses + "\n" +
 	    						"----------------------------------------------" + "\n"*/);
 	        			//txtResult.invalidate();
     				}
     				break;
     			}
     			
     			xmlError = false;
     			break;
     			
     		case SEARCHING_COMPLETED:
     			Log.w(TAG, "Searching stopped");
     			if(txtResult.getText().length() == 0) {
     				txtResult.setText("no word found");
     			}
     			break;
     			
     		case SEARCHING_ERROR:
     			Exception ex = (Exception) msg.obj;
     			if((ex instanceof ParserConfigurationException) ||
     					(ex instanceof SAXException)) {
     				xmlError = true;
     			}
 	    		else {
 	    			txtResult.setText("Error: " + ex.getMessage());
     			}
     			break;
     		}
     	}
     };
     
     private class DictionaryHandler implements IDictionaryService {
 
 		public void onFoundElement(Object element) {
 			Message msg = mHandler.obtainMessage(SEARCHING_FOUND);
 			msg.obj = element;
 			mHandler.sendMessage(msg);
 		}
 
 		public void onSearchStop() {
 			Message msg = mHandler.obtainMessage(SEARCHING_COMPLETED);
 			mHandler.sendMessage(msg);
 		}
 
 		public void onSearchStart() {
 			Message msg = mHandler.obtainMessage(SEARCHING_STARTED);
 			mHandler.sendMessage(msg);
 		}
 
 		public void onError(Exception ex) {
			Message msg = mHandler.obtainMessage(SEARCHING_ERROR);
 			msg.obj = ex;
 			mHandler.sendMessage(msg);
 		}
     	
     }
     
     private class SearchHandler implements TextWatcher {
 
 		public void afterTextChanged(final Editable arg0) {
 			if(arg0.length() == 0) {
 				txtResult.setText("");
 				return;
 			}
 		
 			mService.searchAsync(arg0.toString(), 10, FORMAT);
 		}
 
 		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
 			if(mService == null) {
 				Log.w(TAG, "Initzializing dictionary service");
 				
 				mService = new DictionaryService();
 				mService.setListener(new DictionaryHandler());
 				try {
 					mService.init(new File("/sdcard/" + DICTIONARY_FILE));
 					Log.w(TAG, "Dictionary service initzialized");
 				} catch (FileNotFoundException e) {
 					Log.e(TAG, "Error: " + e.getMessage());
 					txtResult.setText("ERR: " + e.getMessage());
 				}
 			}
 		}
 
 		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
 				int arg3) {}
     	
     }
 }
