 package ru.ratadubna.dubnabus;
 
import java.io.UnsupportedEncodingException;
 import java.net.URL;
import java.net.URLEncoder;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Toast;
 
 abstract public class AbstractContentFragment extends WebViewFragment {
 	abstract String getPage();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setRetainInstance(true);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View result = super.onCreateView(inflater, container,
 				savedInstanceState);
 		getWebView().getSettings().setJavaScriptEnabled(true);
 		getWebView().getSettings().setSupportZoom(true);
 		getWebView().getSettings().setBuiltInZoomControls(true);
 		String content = getPage();
 		if (content.contains("parse:")) {
 			GetTaxiPhonesTask taxiTask = new GetTaxiPhonesTask(
 					content.substring(6));
 			ModelFragment.executeAsyncTask(taxiTask, getActivity()
 					.getApplicationContext());
 		} else
 			getWebView().loadUrl(content);
 		return (result);
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		setUserVisibleHint(true);
 	}
 
 	private class GetTaxiPhonesTask extends AsyncTask<Context, Void, Void> {
 		private Exception e = null;
 		String page = "";
 
 		GetTaxiPhonesTask(String page) {
 			this.page = page;
 		}
 
 		@Override
 		protected Void doInBackground(Context... ctxt) {
 			try {
 				page = ModelFragment.loadPage(new URL(page));
 				if (page.equals("\n"))
 					throw new Exception("Connection problem");
 			} catch (Exception e) {
 				Log.e(getClass().getSimpleName(),
 						"Exception retrieving taxi phones content", e);
 			}
 			return (null);
 		}
 
 		@Override
 		public void onPostExecute(Void arg0) {
 			String result = parseRoutes(page);
			if (e == null) {
				String data = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
						+ "<style type=\"text/css\">td {white-space: nowrap;}</style>"
						+ result;
				try {
					getWebView().loadData(
							URLEncoder.encode(data, "UTF-8").replaceAll("\\+",
									" "), "text/html; charset=UTF-8", null);
				} catch (UnsupportedEncodingException e1) {
					Log.e(getClass().getSimpleName(),
							"Exception loading taxi phones content", e);
				}
			}
 		}
 	}
 
 	private String parseRoutes(String page) {
 		Pattern pattern = Pattern
 				.compile("<table border=0 width=\"100%\">([\\s\\S]+?)</table>"), pattern2 = Pattern
 				.compile("(8[\\d\\(\\)\\s-]{14,15})");
 		Matcher matcher = pattern.matcher(page);
 		if (matcher.find()) {
 			String phone, result = matcher.group()
 					.replaceAll("(21[\\d-]{7}<br[\\s>/]+)", "")
 					.replaceAll("(\\(49621\\)[\\d\\s-]+<br[\\s>/]+)", "")
 					.replaceAll("\\(9", "8(9").replaceAll("[\\(\\)]\\s*", "-")
 					.replaceAll("<tr>\\s+<td colspan=5><hr></td>\\s+</tr>", "")
 					.replace("lightgrey", "lightblue");
 			matcher = pattern2.matcher(result);
 			while (matcher.find()) {
 				phone = matcher.group();
 				result = result.replaceAll(phone, "<a href=\"tel:" + phone
 						+ "\">" + phone + "</a>");
 			}
 			return result;
 		} else {
 			Toast.makeText(getActivity(),
 					DubnaBusActivity.getCtxt().getString(R.string.problem),
 					Toast.LENGTH_LONG).show();
 			return DubnaBusActivity.getCtxt().getString(
 					R.string.connection_problem);
 		}
 	}
 }
