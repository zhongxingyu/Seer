 package fu.berlin.de.webdatabrowser.ui;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import fu.berlin.de.webdatabrowser.R;
 import fu.berlin.de.webdatabrowser.deep.rdf.DeebResource;
 import fu.berlin.de.webdatabrowser.deep.rdf.RdfStore;
 import fu.berlin.de.webdatabrowser.ui.widgets.MenuItem;
 import fu.berlin.de.webdatabrowser.util.Debug;
 
 public class HistoryBrowserActivity extends Activity {
     protected static final String[] PRESET_QUERIES             = new String[] { "SELECT ?subject ?predicate ?object WHERE { ?subject ?predicate ?object }",
                                                               "SELECT ?subject WHERE { ?subject <http://Schema.org/author> ?object }" };
     private static final String[]   PRESET_QUERIES_DESCRIPTION = new String[] { "everything (file)",
                                                                "authors of something" };
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_historybrowser);
         ((MenuItem) findViewById(R.id.historybrowser_menuitem_tohistorybrowser)).setHighlighted(true);
         final RdfStore rdfStore = RdfStore.getInstance();
         final WebView webView = (WebView) findViewById(R.id.historybrowser_webview);
         final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, PRESET_QUERIES_DESCRIPTION);
         final Spinner spinner = (Spinner) findViewById(R.id.historybrowser_spinner);
         spinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
         spinner.setAdapter(spinnerAdapter);
         spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 
             @Override
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 if(position == 0) {
                     Debug.writeFileToExternalStorage(rdfStore.getQueryFormattedResult(PRESET_QUERIES[0]),
                             "rdfstore_dump.txt");
                     return;
                 }
 
                 List<DeebResource> resources = rdfStore.performQuery(PRESET_QUERIES[position]);
                 String source = "<!DOCTYPE html><html>";
 
                 if(resources.isEmpty()) {
                     source += "Nothing useful found.";
                 }
                 else {
                     source += resources.get(0).getHeaderHtml();
                 }
 
                 for(DeebResource resource : resources)
                     source += resource.getHtml() + "<p/>";
 
                 source += "</html>";
                 webView.loadDataWithBaseURL(null, source, "text/html", "UTF-8", null);
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> parent) {
             }
         });
     }
 
     public void toHistoryBrowser(View view) {
     }
 
     public void toWebBrowser(View view) {
         startActivity(new Intent(this, WebBrowserActivity.class));
         finish();
     }
 }
