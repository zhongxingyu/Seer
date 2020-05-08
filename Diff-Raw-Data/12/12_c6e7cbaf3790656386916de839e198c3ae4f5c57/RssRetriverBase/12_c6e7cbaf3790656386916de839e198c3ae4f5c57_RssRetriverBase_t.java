 package com.derek.cshome.util;
 
 import com.derek.cshome.MainActivity.ELEMENT_ID;
 import com.derek.cshome.R;
 import com.derek.cshome.R.id;
 import com.derek.cshome.R.layout;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 
 import com.derek.cshome.util.MyContentHandler;
 import com.derek.cshome.util.MyContentHandler.Item;
 
 import android.R.integer;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class RssRetriverBase<T> extends Fragment {
 	private static String TAG = "RssRetriverBase";
 
 	public static final String HOME_IMAGE_URL = "http://redsox.tcs.auckland.ac.nz/CSS/CSService.svc/home_image";
 	public static final String NEWS_RSS_URL = "http://www.cs.auckland.ac.nz/uoa/home/template/news_feed.rss?category=science_cs_news";
 	public static final String COURSES_RSS_URL = "http://redsox.tcs.auckland.ac.nz/CSS/CSService.svc/courses";
 	public static final String EVENT_RSS_URL = "http://www.cs.auckland.ac.nz/uoa/home/template/events_feed.rss?category=other_events";
 	public static final String SEMINARS_RSS_URL = "http://www.cs.auckland.ac.nz/uoa/home/template/events_feed.rss?category=seminars";
 	public static final String ERROR_RSS_URL = "http://192.168.0.12/cs335/test.rss";
 	private String url;
 
 	protected List<HashMap<String, String>> itemListMap = new ArrayList<HashMap<String, String>>();
 	protected SAXParserFactory saxPF;
 	protected XMLReader xmlReader;
 	protected View rootView;
 	protected RssService rssService;
 	protected ListView listView;
 	private ELEMENT_ID retriver_id;
 
 	public RssRetriverBase() {
 		super();
 	}
 
 	public static RssRetriverBase newInstance(ELEMENT_ID retriver_id) {
 		RssRetriverBase fragment = new RssRetriverBase();
 		Bundle bundle = new Bundle();
 		bundle.putInt("retriver_id", retriver_id.getValue());
 		fragment.setArguments(bundle);
 		Log.d(TAG, "sending (int) retriver_id: " + retriver_id.getValue());
 		return fragment;
 	}
 
 	public ELEMENT_ID getRetriverId() {
 		return retriver_id;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if (getArguments() == null) {
 			Log.e(TAG, "getArguments() == null");
 		} else {
 			Log.d(TAG,
 					"received (int) retriver_id: "
 							+ getArguments().getInt("retriver_id"));
 			retriver_id = ELEMENT_ID.formValue(getArguments().getInt(
 					"retriver_id"));
 		}
 		TAG = "RssRetriverBase-" + retriver_id.toString();
 		Log.i(TAG, "onCreate called");
 		switch (retriver_id) {
 		case NEWS_RSS_ID:
 			url = NEWS_RSS_URL;
 			break;
 		case COURSES_RSS_ID:
 			url = COURSES_RSS_URL;
 			break;
 		case EVENTS_RSS_ID:
			url = EVENT_RSS_URL;//ERROR_RSS_URL;//EVENT_RSS_URL;
 			break;
 		case SEMINORS_RSS_ID:
 			url = SEMINARS_RSS_URL;
 			break;
 		default:
 			url = ERROR_RSS_URL;
 			break;
 		}
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		Log.i(TAG, "onCreateView called");
 		Integer progress = 0;
 		rootView = inflater.inflate(R.layout.fragment_view, container, false);
 		listView = (ListView) rootView.findViewById(R.id.newsListView);
 		rssService = new RssService(this.getActivity(), progress, listView);
 		listView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// TODO Auto-generated method stub
 
 				HashMap<String, String> newItem = (HashMap<String, String>) arg0
 						.getAdapter().getItem(arg2);// RetriveNewsActivity.getLink(arg2);
 				String url;//
 				if ((url =  newItem.get("link")) != null){
 					Log.d(TAG, "in listView setOnItemClickListener, get url: "
 							+ url);
 					// newItem.size()=" + newItem.size() + " " +
 					// newItem.keySet().toString());
 
 					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
 							.parse(url));
 					startActivity(browserIntent);
 				}
 			}
 		});
 		refresh();
 		return rootView;
 	}
 
 	public void refresh() {
 		rssService.execute(url);
 	}
 	
 
 	public class RssService extends AsyncTask<String, Integer, List<HashMap<String, String>>> {
 		private List<T> itemList;
 		private Integer progress;
 		private ListView listView;
 		private Activity ac;
 
 		public RssService() {
 			super();
 			itemList = new ArrayList<T>();
 			itemListMap = new ArrayList<HashMap<String, String>>();
 		}
 
 		public RssService(Activity ac, Integer progress, ListView listView) {
 			this();
 			this.progress = progress;
 			this.listView = listView;
 			this.ac = ac;
 		}
 
 		// moved to contentHandler
 		// protected List<HashMap<String, String>> getItemListMap(List<T>
 		// iteList);
 /*		ErrorHandler myErrorHandler = new ErrorHandler()
 		{
 		    public void fatalError(SAXParseException exception)
 		    throws SAXException
 		    {
 		        System.err.println("fatalError: " + exception);
 		    }
 		    
 		    public void error(SAXParseException exception)
 		    throws SAXException
 		    {
 		        System.err.println("error: " + exception);
 		    }
 
 		    public void warning(SAXParseException exception)
 		    throws SAXException
 		    {
 		        System.err.println("warning: " + exception);
 		    }
 		};*/
 
 //		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 //		dbf.setNamespaceAware(true);
 //		dbf.setValidating(true);
 //		DocumentBuilder db = dbf.newDocumentBuilder();
 //		db.setErrorHandler(myErrorHandler);
 		
 		@Override
 		protected List<HashMap<String, String>> doInBackground(String... params) {
 			Log.d(TAG, "do in background");
 			saxPF = SAXParserFactory.newInstance();
 			MyContentHandler myContentHandler;
 
 			try {
 //				xmlReader = saxPF.newSAXParser().getXMLReader();
 				xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
 				
 				// MyContentHandler myContentHandler = new MyContentHandler(
 				// params[0], retriver_id);
 				myContentHandler = (retriver_id == ELEMENT_ID.COURSES_RSS_ID ? new MyCoursesContentHandler(
 						params[0], retriver_id) : new MyContentHandler(
 						params[0], retriver_id));
 				xmlReader.setContentHandler(myContentHandler);
 				xmlReader
 						.parse(new InputSource(new URL(params[0]).openStream()));
 				itemList = myContentHandler.getItemList();
 				return myContentHandler.getItemListMap();
 			} /*
 			 * catch (MalformedURLException e) { e.printStackTrace(); } catch
 			 * (IOException e) { e.printStackTrace(); } catch (SAXException e) {
 			 * e.printStackTrace(); }
 			 */catch (Exception e) {
 				e.printStackTrace();
 				Log.e(TAG, "" + e.toString());
 			}
 			return null;
 		}
 
 		@Override
 		protected void onCancelled() {
 			// TODO Auto-generated method stub
 			super.onCancelled();
 		}
 
 		// protected abstract void onPostExecute(List<HashMap<String, String>>
 		// result) ;
 		@Override
 		protected void onPostExecute(List<HashMap<String, String>> result){
 			// TODO Auto-generated method stub
 			super.onPostExecute(result);
 			if (result == null){
 				try {
 					throw new Exception("Result null Exception");
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				result = new ArrayList<HashMap<String, String>>();
 			}
 			Log.d(TAG, "RssService-onPostExecute: itemListMap.size(): "
 					+ result.size());
 
 			/*
 			 * ac.runOnUiThread(new Runnable() {
 			 * 
 			 * @Override public void run() { // TODO Auto-generated method stub
 			 * 
 			 * } });
 			 */
 
 			if (result == null) {
 				Log.e(TAG, "in onPostExecute, newsListMap == null");
 			} else {
 				Log.d(TAG,
 						"in onPostExecute, newsListMap.size() = "
 								+ result.size());
 			}
 			SimpleAdapter sp;
 			if (retriver_id == ELEMENT_ID.COURSES_RSS_ID) {
 				// listView.setLayoutParams(new LayoutParams(ac, R.id.));
 				// this.listView = (ListView) getActivity().findViewById(R.id.);
 				listView.setClickable(false);
 				sp = new SimpleAdapter(ac, result, R.layout.courses_row_layout,
 						new String[] { "codeField", "semesterField",
 								"titleField" }, new int[] {
 								R.id.courseCodeField, R.id.courseSemesterField,
 								R.id.coursesTitleField });
 			} else {
 				listView.setClickable(true);
 				sp = new SimpleAdapter(ac, result, R.layout.news_row_layout,
 						new String[] { "title", "description", "pubDate",
 								"endDate" }, new int[] {
 								R.id.newsRowLayoutTitle,
 								R.id.newsRowLayoutDescription,
 								R.id.newsRowLayoutPubDate,
 								R.id.newsRowLayoutEndDate });
 			}
 
 			this.listView.setAdapter(sp);
 			sp.notifyDataSetChanged();
 		}
 
 		@Override
 		protected void onPreExecute() {
 			// TODO Auto-generated method stub
 			super.onPreExecute();
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			// TODO Auto-generated method stub
 			super.onProgressUpdate(values);
 		}
 
 	}
 }
