 package gr.teilar.dionysos;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.nio.charset.Charset;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.xml.sax.SAXException;
 import org.xmlpull.v1.XmlSerializer;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.util.Xml;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Dionysos extends Activity {
 
 	private static final String GRADES_URL = "https://dionysos.teilar.gr/unistudent/stud_CResults.asp?studPg=1&mnuid=mnu3";
 	private static final String LESSONS_URL = "https://dionysos.teilar.gr/unistudent/stud_NewClass.asp?studPg=1&mnuid=diloseis;newDil&";
 	private static final String REQUESTS_URL = "https://dionysos.teilar.gr/unistudent/stud_reqStatus.asp?studPg=1&mnuid=forms;sForm&";
 	private static DefaultHttpClient httpclient;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.downloading);
 
 		final ProgressBar progress = (ProgressBar) findViewById(R.id.downloadingProgressBar);
 		final TextView textview = (TextView) findViewById(R.id.downloadingProgressText);
 
 		String[] urls = { null, GRADES_URL, LESSONS_URL, REQUESTS_URL };
 		Bundle b = this.getIntent().getExtras();
 		int id = 0;
 
 		if (b != null) {
 			id = b.getInt("id");
 		}
 
 		new DownloadData(this, progress, textview, urls[id]).execute();
 	}
 
 	private static boolean connectToDionysos(String username, String password) {
 		httpclient = new DefaultHttpClient();
 		HttpGet httpget = new HttpGet(
 				"https://dionysos.teilar.gr/unistudent/login.asp");
 
 		try {
 			@SuppressWarnings("unused")
 			HttpResponse connectionResponse;
 			connectionResponse = httpclient.execute(httpget);
 
 			HttpPost httpost = new HttpPost(
 					"https://dionysos.teilar.gr/unistudent/login.asp");
 
 			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
 			nvps.add(new BasicNameValuePair("userName", username));
 			nvps.add(new BasicNameValuePair("pwd", password));
 			nvps.add(new BasicNameValuePair("loginTrue", "login"));
 
 			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
 
 			connectionResponse = httpclient.execute(httpost);
 		} catch (ClientProtocolException e) {
 			return false;
 		} catch (IOException e) {
 			return false;
 		}
 		return true;
 	}
 
 	public static String downloadURL(String url) {
 		String html = "";
 		try {
 
 			HttpGet httpget = new HttpGet(url);
 			HttpResponse ratesresponse = httpclient.execute(httpget);
 			html = inputStreamToString(ratesresponse.getEntity().getContent())
 					.toString();
 
 		} catch (IllegalArgumentException e) {
 			return "";
 		} catch (IllegalStateException e) {
 			return "";
 		} catch (IOException e) {
 			return "";
 		}
 		return html;
 	}
 
 	private static Boolean parseAndCreateGradesXML(String html) {
 		Document doc = Jsoup.parse(html);
 
 		Elements gradestable = doc.select("table[cellpadding=4]");
 		Elements grades = gradestable.select("td[colspan=2]");
 
 		Elements trs = grades.select("table > tbody > tr");
 		Elements tds;
 
 		int eksamino = 0;
 
 		XmlSerializer xmlSerializer = Xml.newSerializer();
 		StringWriter writer = new StringWriter();
 		SimpleDateFormat s = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
 
 		try {
 			xmlSerializer.setOutput(writer);
 			xmlSerializer.startDocument("UTF-8", true);
 			xmlSerializer.startTag("", "grades");
 			xmlSerializer.attribute("", "date", s.format(new Date()));
 
 			for (Element tr : trs) {
 				if (tr.attr("height").equals("15")
 						|| tr.className().equals("italicHeader")
 						|| tr.className().equals("subHeaderBack"))
 					continue;
 				else {
 					tds = tr.select("td");
 
 					if (tds.size() == 1) {
 						if (eksamino > 0 && eksamino < 8) {
 							xmlSerializer.endTag("", "eksamino");
 						}
 						eksamino++;
 
 						if (eksamino < 8) {
 							xmlSerializer.startTag("", "eksamino");
 							xmlSerializer.attribute("", "id",
 									Integer.toString(eksamino));
 						}
 					} else if (tds.size() == 8) {
 						xmlSerializer.startTag("", "lesson");
 						xmlSerializer.attribute("", "dm", tds.get(3).text());
 						xmlSerializer.attribute("", "ores", tds.get(4).text());
 						xmlSerializer.attribute("", "vathmos", tds.get(6)
 								.text());
 						xmlSerializer.text(tds.get(1).text()
 								.replaceAll("\\(.*?\\)  ", ""));
 						xmlSerializer.endTag("", "lesson");
 					}
 				}
 			}
 
 			xmlSerializer.endTag("", "grades");
 			xmlSerializer.endDocument();
 
 			if (!writeToFile(Environment.getExternalStorageDirectory()
 					.getPath() + "/egrammatia/grades.xml", writer.toString())) {
 				return false;
 			}
 
 		} catch (IllegalArgumentException e) {
 			return false;
 		} catch (IllegalStateException e) {
 			return false;
 		} catch (IOException e) {
 			return false;
 		} catch (Exception e) {
 			return false;
 		}
 		return true;
 	}
 
 	private static Boolean parseAndCreateLessonsXML(String html) {
 		Document doc = Jsoup.parse(html);
 
 		Element table = doc.select("#mainTable").get(1)
 				.select("table[cellspacing=2]").last();
 
 		Elements trs = table.select("tr[bgcolor=#F1F1F1]");
 		Elements tds;
 
 		XmlSerializer xmlSerializer = Xml.newSerializer();
 		StringWriter writer = new StringWriter();
 		SimpleDateFormat s = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
 
 		try {
 			xmlSerializer.setOutput(writer);
 			xmlSerializer.startDocument("UTF-8", true);
 			xmlSerializer.startTag("", "lessons");
 			xmlSerializer.attribute("", "date", s.format(new Date()));
 
 			for (Element tr : trs) {
 				tds = tr.select("td");
 				xmlSerializer.startTag("", "lesson");
 				xmlSerializer.attribute("", "eksamino", tds.get(3).text());
 				xmlSerializer.attribute("", "ores", tds.get(6).text());
 				xmlSerializer.attribute("", "dm", tds.get(5).text());
 				xmlSerializer.text(tds.get(2).select("span").first().text());
 				xmlSerializer.endTag("", "lesson");
 			}
 
 			xmlSerializer.endTag("", "lessons");
 			xmlSerializer.endDocument();
 
 			File direct = new File(Environment.getExternalStorageDirectory()
 					+ "/egrammatia");
 			if (!direct.exists()) {
 				direct.mkdir();
 			}
 
 			if (!writeToFile(Environment.getExternalStorageDirectory()
 					.getPath() + "/egrammatia/lessons.xml", writer.toString()))
 				return false;
 
 		} catch (IllegalArgumentException e) {
 			return false;
 		} catch (IllegalStateException e) {
 			return false;
 		} catch (IOException e) {
 			return false;
 		} catch (Exception e) {
 			return false;
 		}
 		return true;
 	}
 
 	private static Boolean parseAndCreateRequestsXML(String html) {
 		Document doc = Jsoup.parse(html);
 
 		Element td = doc.select("tr.TableCellBold > td").first();
 		Elements tables = td.select("table");
 
 		XmlSerializer xmlSerializer = Xml.newSerializer();
 		StringWriter writer = new StringWriter();
 		SimpleDateFormat s = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
 
 		try {
 			xmlSerializer.setOutput(writer);
 			xmlSerializer.startDocument("UTF-8", true);
 			xmlSerializer.startTag("", "requests");
 			xmlSerializer.attribute("", "date", s.format(new Date()));
 
 			for (Element table : tables) {
 				xmlSerializer.startTag("", "request");
 				xmlSerializer.attribute("", "date", table.select("td").get(1)
 						.text().replace("-", "/"));
 				xmlSerializer.text(table.select("td").get(2).text());
 				xmlSerializer.endTag("", "request");
 			}
 
 			xmlSerializer.endTag("", "requests");
 			xmlSerializer.endDocument();
 
 			File direct = new File(Environment.getExternalStorageDirectory()
 					+ "/egrammatia");
 			if (!direct.exists()) {
 				direct.mkdir();
 			}
 
 			if (!writeToFile(Environment.getExternalStorageDirectory()
 					.getPath() + "/egrammatia/requests.xml", writer.toString()))
 				return false;
 
 		} catch (IllegalArgumentException e) {
 			return false;
 		} catch (IllegalStateException e) {
 			return false;
 		} catch (IOException e) {
 			return false;
 		} catch (Exception e) {
 			return false;
 		}
 		return true;
 	}
 
 	private static StringBuilder inputStreamToString(InputStream is) {
 		String line = "";
 		StringBuilder total = new StringBuilder();
 
 		// Wrap a BufferedReader around the InputStream
 		BufferedReader rd = new BufferedReader(new InputStreamReader(is,
 				Charset.forName("cp1253")));
 
 		// Read response until the end
 		try {
 			while ((line = rd.readLine()) != null) {
 				total.append(line);
 			}
 		} catch (IOException e) {
 			return total;
 		}
 
 		// Return full string
 		return total;
 	}
 
 	public static boolean writeToFile(String filename, String text) {
 		File direct = new File(Environment.getExternalStorageDirectory()
 				+ "/egrammatia");
 		if (!direct.exists()) {
 			direct.mkdir();
 		}
 		try {
 			File myFile = new File(filename);
 			myFile.createNewFile();
 			FileOutputStream fOut;
 			fOut = new FileOutputStream(myFile);
 			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
 			myOutWriter.append(text);
 			myOutWriter.close();
 			fOut.close();
 		} catch (IOException e) {
 			return false;
 		}
 		return true;
 
 	}
 
 	private Boolean isOnline() {
 		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetworkInfo = connectivityManager
 				.getActiveNetworkInfo();
 		return activeNetworkInfo != null;
 	}
 
 	public static Boolean checkUpdate(Context c, String filename) {
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(c);
 		int minutes = Integer.parseInt(prefs.getString(
 				"updateOldDataPreference", "10"));
 		File file = new File(Environment.getExternalStorageDirectory()
 				.getPath() + "/egrammatia/" + filename + ".xml");
 
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db;
 		org.w3c.dom.Document doc = null;
 
 		try {
 			db = dbf.newDocumentBuilder();
 			doc = db.parse(file);
 		} catch (SAXException e) {
 			return true;
 		} catch (IOException e) {
 			return true;
 		} catch (ParserConfigurationException e1) {
 			return true;
 		}
 
 		doc.getDocumentElement().normalize();
 		String date = ((org.w3c.dom.Element) doc.getElementsByTagName(filename)
 				.item(0)).getAttribute("date");
 
 		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
 		Date reqDate = new Date();
 		try {
 			reqDate = format.parse(date);
 		} catch (ParseException e) {
 			return true;
 		}
 		long now = new Date().getTime();
 		long minDiff = ((now - reqDate.getTime()) / 60000);
 
 		if (minDiff > minutes)
 			return true;
 		return false;
 	}
 	
 	public static int checkHtml(String html){
 		if (html.equals("") || html.indexOf("Συνέβη σφάλμα") != -1
 				|| html.indexOf("You are not authorized") != -1) {
 			return 2;
 		}
 		if (html.indexOf("Λάθος όνομα χρήστη") != -1) {
 			return 11;
 		}
 		if (html.indexOf("Λάθος κωδικός πρόσβασης") != -1) {
 			return 12;
 		}
 		if (html.indexOf("Είσοδος Φοιτητή") != -1) {
 			return 13;
 		}
 		return -1;
 	}
 
 	final class DownloadData extends AsyncTask<Void, Integer, Void> {
 		private final int titles[] = { R.string.connection_check,
 				R.string.connecting, R.string.downloading_grades,
 				R.string.writing_grades, R.string.downloading_lessons,
 				R.string.writing_lessons, R.string.downloading_requests,
 				R.string.writing_requests };
 
 		private final int errors[] = { R.string.connection_error,
 				R.string.connecting_error, R.string.dionysos_error,
				R.string.missing_username, R.string.missing_password,
 				R.string.downloading_grades_error,
 				R.string.writing_grades_error,
 				R.string.downloading_lessons_error,
 				R.string.writing_lessons_error,
 				R.string.downloading_requests_error,
 				R.string.writing_requests_error,
 				R.string.login_fail_username,
 				R.string.login_fail_password,
 				R.string.login_fail};
 
 		private final int progr[] = { 2, 20, 18, 8, 18, 8, 18, 8 };
 
 		private String url;
 		private int index;
 		private int errorCode;
 
 		private final Activity parent;
 		private final ProgressBar progress;
 		private final TextView textview;
 
 		public DownloadData(final Activity parent, final ProgressBar progress,
 				final TextView textview, final String urladdr) {
 			this.parent = parent;
 			this.progress = progress;
 			this.textview = textview;
 			this.url = urladdr;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			int max = 0;
 			for (final int p : progr) {
 				max += p;
 			}
 			progress.setMax(max);
 			index = 0;
 			errorCode = -1;
 			progress.setVisibility(View.VISIBLE);
 		}
 
 		@Override
 		protected Void doInBackground(final Void... params) {
 			if (!isOnline()) {
 				errorCode = 0;
 				return null;
 			}
 
 			publishProgress();
 
 			SharedPreferences prefs = PreferenceManager
 					.getDefaultSharedPreferences(this.parent);
 			String username = prefs.getString("username", "");
 			String password = prefs.getString("password", "");
 			String html;
 
 			if (username.equals("")) {
 				errorCode = 3;
 				return null;
 			}
 			if (password.equals("")) {
 				errorCode = 4;
 				return null;
 			}
 
 			if (!connectToDionysos(username, password)) {
 				errorCode = 0;
 				return null;
 			}
 
 			publishProgress();
 
 			if (url != null) { /* Download specific url */
 
 				html = downloadURL(url);
 				errorCode = checkHtml(html);
 				if (errorCode != -1) return null;
 
 				if (url == GRADES_URL) {
 					publishProgress();
 					if (!parseAndCreateGradesXML(html)) {
 						errorCode = 5;
 						return null;
 					}
 				} else if (url == LESSONS_URL) {
 					index += 2;
 					publishProgress();
 					if (!parseAndCreateLessonsXML(html)) {
 						errorCode = 7;
 						return null;
 					}
 				} else if (url == REQUESTS_URL) {
 					index += 4;
 					publishProgress();
 					if (!parseAndCreateRequestsXML(html)) {
 						errorCode = 9;
 						return null;
 					}
 				}
 				publishProgress();
 			} else { /* Download all urls */
 
 				html = downloadURL(GRADES_URL);
 				errorCode = checkHtml(html);
 				if (errorCode != -1) return null;
 				
 				publishProgress();
 
 				if (!parseAndCreateGradesXML(html)) {
 					errorCode = 5;
 					return null;
 				}
 				publishProgress();
 
 				html = downloadURL(LESSONS_URL);
 				errorCode = checkHtml(html);
 				if (errorCode != -1) return null;
 				
 				publishProgress();
 
 				if (!parseAndCreateLessonsXML(html)) {
 					errorCode = 7;
 					return null;
 				}
 				publishProgress();
 
 				html = downloadURL(REQUESTS_URL);
 				errorCode = checkHtml(html);
 				if (errorCode != -1) return null;
 
 				publishProgress();
 
 				if (!parseAndCreateRequestsXML(html)) {
 					errorCode = 9;
 					return null;
 				}
 				publishProgress();
 			}
 
 			httpclient.getConnectionManager().shutdown();
 			index = 100;
 			return null;
 		}
 
 		@Override
 		protected void onProgressUpdate(final Integer... values) {
 			if (index >= 100) /* download finished or error */
 				return;
 			
 			textview.setText(titles[index]);
 			progress.incrementProgressBy(progr[index]);
 			++index;
 		}
 
 		@Override
 		protected void onPostExecute(final Void result) {
 			progress.setVisibility(View.GONE);
 			if (errorCode != -1) {
 
 				Toast.makeText(parent, errors[errorCode], Toast.LENGTH_LONG)
 						.show();
 				if (errorCode == 3 || errorCode == 4) {
 					parent.finish();
 					Intent i = new Intent(parent,
 							gr.teilar.dionysos.PreferencesScreen.class);
 					startActivity(i);
 				}
 				Toast.makeText(parent, R.string.download_fail,
 						Toast.LENGTH_LONG).show();
 				parent.setResult(RESULT_CANCELED);
 			} else {
 				Toast.makeText(parent, R.string.download_success,
 						Toast.LENGTH_LONG).show();
 				parent.setResult(RESULT_OK);
 			}
 			parent.finish();
 		}
 	}
 
 }
