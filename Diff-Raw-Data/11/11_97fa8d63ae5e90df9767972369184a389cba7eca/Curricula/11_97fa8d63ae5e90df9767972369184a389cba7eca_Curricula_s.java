 package de.tum.in.newtumcampus;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.SlidingDrawer;
 import android.widget.TextView;
 import de.tum.in.newtumcampus.common.FileUtils;
 import de.tum.in.newtumcampus.common.Utils;
 
 /**
  * Activity to fetch and display the curricula of different programs.
  * 
  * @author Vincenz Doelle
  * @review Daniel G. Mayr
  * @review Thomas Behrens
  */
 public class Curricula extends Activity {
 
 	/** Key for the shared preference */
 	private static final String PREF_CURRICULUM_SETTING_KEY = "preferred_curriculum";
 
 	/** Http client to fetch the curricula data */
 	private DefaultHttpClient httpClient;
 
 	/** List of curricula displayed in the slider drawer */
 	private ListView lvCurricula;
 
 	/** Web view to show the fetched curriculum */
 	private WebView webView;
 
 	/** Slider to list the curricula options */
 	private SlidingDrawer slider;
 
 	/** Different curricula available (name -> url) */
 	Hashtable<String, String> options;
 
 	/** Preferred (last opened) curriculum stored in the shared preferences */
 	private String prefCurriculum;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.curricula);
 
 		httpClient = new DefaultHttpClient();
 
 		prefCurriculum = Utils.getSetting(this, PREF_CURRICULUM_SETTING_KEY);
 
 		webView = (WebView) findViewById(R.id.webView);
 		webView.getSettings().setBuiltInZoomControls(true);
 		webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
 
 		slider = (SlidingDrawer) findViewById(R.id.slider);
 
 		// Initialize the curricula options that should be displayed in the list
 		initOptions();
 
 		// sort curricula options and attach them to the list
 		Vector<String> sortedOptions = new Vector<String>(options.keySet());
 		Collections.sort(sortedOptions);
 
 		String[] optionsArray = sortedOptions.toArray(new String[0]);
 
 		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_custom,
 				optionsArray);
 
 		lvCurricula = (ListView) findViewById(R.id.lstCurricula);
 		lvCurricula.setAdapter(arrayAdapter);
 
 		lvCurricula.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				String curriculum = ((TextView) view).getText().toString();
 				getCurriculum(curriculum, options.get(curriculum));
 			}
 		});
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 
 		File prefCurriculumFile = new File(prefCurriculum);
 
 		// check if a specific curriculum exists and was opened last time
 		// if yes, consider this as preferred and open it as default
 		if (prefCurriculum == null || prefCurriculum.equals("") || !prefCurriculumFile.exists()) {
 			slider.animateOpen();
 		} else {
 			openFile(prefCurriculumFile);
 		}
 
 	}
 
 	/**
 	 * Downloads the curricula data, parses the relevant content, adds the corresponding css information and creates a
 	 * new html document.
 	 * 
 	 * @param name The name of the curriculum as displayed in the list.
 	 * @param url The url of the curriculum to be downloaded.
 	 */
 	private void getCurriculum(String name, final String url) {
 
 		String filename = FileUtils.getFilename(name, ".html");
 
 		File file = null;
 		try {
 			file = FileUtils.getFileOnSD(Const.CURRICULA, filename);
 		} catch (Exception e) {
 			Utils.showLongCenteredToast(this, getString(R.string.no_sd_card));
 			Log.d("EXCEPTION", e.getMessage());
 		}
 
 		if (file == null) {
 			return; // cannot work without target file
 		}
 
 		// if file does not exist download it again
 		if (!file.exists()) {
 			if (!Utils.isConnected(this)) {
 				Utils.showLongCenteredToast(this, getString(R.string.no_internet_connection));
 				return;
 			}
 
 			// start the progress dialog
 			final ProgressDialog progressDialog = ProgressDialog.show(this, "", getString(R.string.fetching_document));
 			progressDialog.setCancelable(true);
 
 			// fetch information in a background task and show progress dialog in meantime
 			final AsyncTask<Object, Void, File> backgroundTask = new AsyncTask<Object, Void, File>() {
 
 				@Override
 				protected File doInBackground(Object... params) {
 					fetchCurriculum((String) params[0], (File) params[1]);
 					return (File) params[1];
 				}
 
 				@Override
 				protected void onPostExecute(File result) {
 					progressDialog.dismiss();
 					openFile(result);
 				}
 			};
 
 			// terminate background task if running
 			progressDialog.setOnCancelListener(new OnCancelListener() {
 				@Override
 				public void onCancel(DialogInterface dialog) {
 					backgroundTask.cancel(true);
 				}
 			});
 
 			backgroundTask.execute(url, file);
 
 		} else {
 			openFile(file);
 		}
 
 	}
 
 	/**
 	 * Fetches the curriculum document and extracts all relevant information.
 	 * 
 	 * @param url URL of the curriculum document
 	 * @param targetFile Target where the results should be written to
 	 */
 	private void fetchCurriculum(String url, File targetFile) {
 		String text = Utils.buildHTMLDocument(
 				FileUtils.sendGetRequest(httpClient, "http://www.in.tum.de/fileadmin/_src/add.css"),
 				"<div id=\"maincontent\"><div class=\"inner\">" + extractResultsFromURL(url) + "</div></div>");
 
 		text = text.replace("href=\"fuer-studierende-der-tum", "href=\"http://www.in.tum.de/fuer-studierende-der-tum");
 
 		FileUtils.writeFile(targetFile, text);
 	}
 
 	/**
 	 * Extract the results from a document fetched from the given URL.
 	 * 
 	 * @param url URL pointing to a document where the results are extracted from.
 	 * @return The results.
 	 */
 	private String extractResultsFromURL(String url) {
 		String text = FileUtils.sendGetRequest(httpClient, url);
 
 		if (text == null) {
 			return getString(R.string.something_wrong);
 		}
 		return Utils.cutText(text, "<!--TYPO3SEARCH_begin-->", "<!--TYPO3SEARCH_end-->");
 	}
 
 	/**
 	 * Opens a local file.
 	 * 
 	 * @param file File to be opened.
 	 */
 	private void openFile(File file) {
 		if (file == null) {
 			return;
 		}
 
 		// save as preferred curriculum to open automatically later
 		Utils.setSetting(this, PREF_CURRICULUM_SETTING_KEY, file.getPath());
 
 		webView.loadUrl("file://" + file.getPath());
 
 		if (slider.isOpened()) {
 			slider.animateClose();
 		}
 	}
 
 	/** Initializes the curricula options available in the list. */
 	private void initOptions() {
 		options = new Hashtable<String, String>();
 		options.put(
 				getString(R.string.informatics_bachelor),
 				"http://www.in.tum.de/fuer-studierende-der-tum/bachelor-studiengaenge/informatik/studienplan/studienbeginn-ab-ws-20072008.html");
 		options.put(
 				getString(R.string.business_informatics_bachelor_0809),
 				"http://www.in.tum.de/fuer-studierende-der-tum/bachelor-studiengaenge/wirtschaftsinformatik/studienplan/studienbeginn-ab-ws-20112012.html");
 		options.put(
 				getString(R.string.business_informatics_bachelor_1112),
 				"http://www.in.tum.de/fuer-studierende-der-tum/bachelor-studiengaenge/wirtschaftsinformatik/studienplan/studienbeginn-ab-ws-20082009.html");
 		options.put(getString(R.string.bioinformatics_bachelor),
 				"http://www.in.tum.de/fuer-studierende-der-tum/bachelor-studiengaenge/bioinformatik/studienplan/ws-20072008.html");
 		options.put(
 				getString(R.string.games_engineering_bachelor),
 				"http://www.in.tum.de/fuer-studierende-der-tum/bachelor-studiengaenge/informatik-games-engineering/studienplan-games.html");
 
 		options.put(getString(R.string.informatics_master),
 				"http://www.in.tum.de/fuer-studierende-der-tum/master-studiengaenge/informatik/studienplan/studienplan-fpo-2007.html");
 		options.put(
 				getString(R.string.business_informatics_master),
				"http://www.in.tum.de/fuer-studierende-der-tum/master-studiengaenge/wirtschaftsinformatik/studienplan/studienplan-fpo-2008.html");
 		options.put(getString(R.string.bioinformatics_master),
 				"http://www.in.tum.de/fuer-studierende-der-tum/master-studiengaenge/bioinformatik/studienplan/ws-20072008.html");
		options.put(getString(R.string.automotive_master),
				"http://www.in.tum.de/fuer-studierende-der-tum/master-studiengaenge/automotive-software-engineering/stundenplaene.html");
 		options.put(
 				getString(R.string.computational_science_master),
				"http://www.in.tum.de/fuer-studierende-der-tum/master-studiengaenge/computational-science-and-engineering/course/list-of-courses.html");
 	}
 }
