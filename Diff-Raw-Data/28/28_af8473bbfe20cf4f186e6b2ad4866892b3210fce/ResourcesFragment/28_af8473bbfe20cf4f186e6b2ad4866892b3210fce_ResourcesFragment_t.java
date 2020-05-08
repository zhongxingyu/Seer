 package phillykeyspots.frpapp;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 import android.app.ProgressDialog;
import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebView;
import android.widget.Button;
 
 /**
  * Loads the Information on the Resource Page off the web site.
  * 
  * @author btopportaldev
  *
  */
 
 public class ResourcesFragment extends Fragment {
 
 	private ResourcesLoader loader;
 	private WebView view;
 	private ProgressDialog progress;
 	
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, 
         Bundle savedInstanceState) {
         // Inflate the layout for this fragment
         return inflater.inflate(R.layout.fragment_resources, container, false);
     }
 	public void onStart(){
 		super.onStart();
 		view = (WebView)getActivity().findViewById(R.id.resources_webview);
 	}
 	
 	/**
 	 * Starts the Loading overlay.
 	 * Gathers information from the web site and loads it on the page.
 	 */
 	
 	public void onResume(){
 		super.onResume();
 		progress = ProgressDialog.show(getActivity(), "", "Loading...");
 		loader = new ResourcesLoader();
 		loader.execute();
 	}
 	
 	/**
 	 * Tells the view to insert HTML.
 	 * Style is added here.
 	 */
 	
 	private void loadOurData(String data){
 		String style = "<style type='text/css'>" +
 				"h3{background-color:#479CD1;color:#ffffff;padding:3px;}" +
 				"a{color:#f89939;font-weight:700;}" +
 				"dd{color:#479cd1;}" + 
 				"</style>";
 		view.loadData(style+data, "text/html", null);
 	}
 	
 	/**
 	 * Loads different information based on Button pushed.
	 * Changes colors of tabs.
	 * Calls loadOurData twice because doesnt always work when just called once for some reason. 
 	 * @param ID - the ID of the button pushed.
 	 */
 	
 	public void switchTab(int ID){
		((Button)getActivity().findViewById(ID)).setTextColor(getResources().getColor(R.color.orange1645C));
 		switch(ID){
 		case (R.id.resources_career):
 			loadOurData(loader.career);
			loadOurData(loader.career);
			((Button)getActivity().findViewById(R.id.resources_computer_training)).setTextColor(Color.BLACK);
			((Button)getActivity().findViewById(R.id.resources_public_services)).setTextColor(Color.BLACK);
 			break;
 		case (R.id.resources_computer_training):
 			loadOurData(loader.computer_training);
			loadOurData(loader.computer_training);
			((Button)getActivity().findViewById(R.id.resources_career)).setTextColor(Color.BLACK);
			((Button)getActivity().findViewById(R.id.resources_public_services)).setTextColor(Color.BLACK);
 			break;
 		case (R.id.resources_public_services):
 			loadOurData(loader.public_services);
			loadOurData(loader.public_services);
			((Button)getActivity().findViewById(R.id.resources_career)).setTextColor(Color.BLACK);
			((Button)getActivity().findViewById(R.id.resources_computer_training)).setTextColor(Color.BLACK);
 			break;
 		}
 	}
 	
 	/**
 	 * Gathers the information on the website as HTML.
 	 * 
 	 * @author btopportaldev
 	 *
 	 */
 	
 	public class ResourcesLoader extends AsyncTask<String, Void, String>{
 
 		public String career, computer_training, public_services;
 		private Document doc;
 		
 		/** 
 		 * Loads the web site into a Document.
 		 * @param url
 		 */
 		
 		private void Load(String url){
 			try{
 				doc = Jsoup.connect(url).get();	
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		protected String doInBackground(String... params) {
 			if(doc == null){
 				Load("https://www.phillykeyspots.org/resources");
 			}
 			return null;
 		}
 		
 		/**
 		 * Breaks the Document up into the pieces that are desired.
 		 * Displays the initial page.
 		 * Dismisses Progress overlay.
 		 */
 		
 		protected void onPostExecute(String result){
 			career = doc.select("div#career_list").get(0).html();
 			computer_training = doc.select("div#computer_training_list").get(0).html();
 			public_services = doc.select("div#public_services_list").get(0).html();
 			loadOurData(career);
 			progress.dismiss();
 		}
 	}
 }
