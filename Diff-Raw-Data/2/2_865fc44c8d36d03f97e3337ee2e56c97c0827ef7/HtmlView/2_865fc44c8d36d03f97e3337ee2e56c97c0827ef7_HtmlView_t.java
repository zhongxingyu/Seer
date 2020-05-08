 package lu.albert.android.jsonbackup;
 
 import java.io.IOException;
 import java.io.InputStream;
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.webkit.WebView;
 
 /**
  * This activity displays the license text in HTML which is much easier to read
  * than plain-text
  * 
  * @author exhuma
  */
 public class HtmlView extends Activity {
 
 	/** Key that points to the document which is loaded in the HTML view */
	public static final String KEY_DOC_ID = "lu.albert.android.jsonbackup.html_doc_id";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.html_view);
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			int docId = extras.getInt(KEY_DOC_ID);
 			loadDocument( docId );
 		}
 	}
 
 	private void loadDocument(int docId) {
 		WebView view = (WebView)findViewById(R.id.webview);
 		InputStream license_stream = getResources().openRawResource(docId);
 		StringBuilder content = new StringBuilder();
 		int result;
 		try {
 			result = license_stream.read();
 			while ( result != -1 ){
 				content.append((char)result);
 				result = license_stream.read();
 			}
 		} catch (IOException e) {
 			Log.e(this.getClass().getCanonicalName(), e.getMessage());
 			content.append( "Unable to read the file. Error message was:\n" );
 			content.append( e.getMessage() );
 		}
 		view.loadData(content.toString(), "text/html", "UTF-8");
 		if ( view.getTitle() != null ){
 			setTitle(view.getTitle());
 		} else {
 			setTitle( "unknown doc " );
 		}
 	}
 	
 }
