 package dhbw.stundenplan;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Looper;
 import android.view.Menu;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.Toast;
 import dhbw.stundenplan.database.ResultsDBAdapter;
 import dhbw.stundenplan.database.UserDBAdapter;
 
 /**
  * Zeigt provisorisch die Prfungsergebnisse an
  * 
  * @author DH10HAH
  */
 public class Noten extends OptionActivity
 {
 	final int layout = R.layout.noten;
 	public ProgressDialog progressDialog;
 	Context context;
 	WebView wv;
 
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(layout);
 		// tv = (TextView)findViewById(R.id.textView2);
 		wv = (WebView) findViewById(R.id.webView1);
 		context = this;
 		schreibeErgebnisse();
 	}
 
 	/**
 	 * Wird von Button aufgerufen und startet das Aktualisieren von Vorlesungen
 	 * 
 	 * @param view
 	 */
 	public void aktualisieren(View view)
 	{
 		progressDialog = new ProgressDialog(context);
 		// progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		progressDialog.setMessage("Aktualisiere Noten");
 		progressDialog.setCancelable(false);
 		progressDialog.show();
 		new DownloadResults().execute("");
 	}
 
 	/**
 	 * Vorlufig um die Ergebnisse anzuzeigen
 	 */
 	public void schreibeErgebnisse()
 	{
 		ResultsDBAdapter resultsDBAdapter = new ResultsDBAdapter(context);
 		Cursor c = resultsDBAdapter.fetchAll();
 		if (c.moveToFirst())
 		{
 			StringBuilder stringBuilder = new StringBuilder();
 
 			while (!c.isLast())
 			{
 				stringBuilder.append("<tr>" + "<td>" + c.getString(0) + "</td><td>" + c.getString(1) + "</td><td>" + c.getString(4) + "</td><td>" + c.getString(5) + "</td><td>" + c.getString(6) + "</td>" + "</tr>");
 				c.moveToNext();
 			}
 			String data = "<table border=\"1\"><th>Nr.</th><th>Vorlesung</th><th>Note</th><th>ECTS</th><th>Status</th>" + stringBuilder + "</tbale>";
 
 			wv.loadDataWithBaseURL(null, data, "text/html", "utf-8", null);
 		}
 		else
 		{
 			wv.loadDataWithBaseURL(null, "Bitte erst die Datenbank Aktualisieren", "text/html", "utf-8", null);
 		}
 		c.close();
 		resultsDBAdapter.close();
 
 	}
 
 	/**
 	 * Startet einen Ladedialog, und ld die Ergebnisse herunter
 	 */
 	private class DownloadResults extends AsyncTask<String, Integer, Object>
 	{
 		boolean internetConnection;
 
 		@Override
 		protected Object doInBackground(String... arg)
 		{
 
 			Looper.prepare();
 			if (checkInternetConnection())
 			{
 				UserDBAdapter userDBAdapter = new UserDBAdapter(context);
 				final String password = userDBAdapter.getPassword();
 				final String username = userDBAdapter.getUsername();
 				userDBAdapter.close();
 
 				Online online = new Online();
 				// publishProgress(50);
 				online.ladeResultsInDB(username, password, context);
 				// publishProgress(100);
 				internetConnection = true;
 			}
 			else
 			{
 				internetConnection = false;
 			}
 			return null;
 		}
 
 		public void onPostExecute(Object result)
 		{
 			if (internetConnection)
 			{
 				schreibeErgebnisse();
				progressDialog.dismiss();

 			}
 			else
 			{
				progressDialog.dismiss();
 				Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		return true;
 	}
 }
