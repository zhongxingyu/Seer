 package th.in.llun.thorfun;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import th.in.llun.thorfun.api.Thorfun;
 import th.in.llun.thorfun.api.model.CategoryStory;
 import th.in.llun.thorfun.api.model.Story;
 import th.in.llun.thorfun.api.model.ThorfunResult;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
import android.webkit.WebSettings;
 import android.webkit.WebView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class StoryViewContentFragment extends SherlockFragment {
 
 	private CategoryStory mStory;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 	    Bundle savedInstanceState) {
 
 		View rootView = inflater.inflate(R.layout.fragment_story_view_content,
 		    container, false);
 
 		Bundle arguments = getArguments();
 		String rawStory = arguments.getString(StoryView.KEY_STORY);
 		try {
 			mStory = new CategoryStory(new JSONObject(rawStory));
 
 			final WebView webView = (WebView) rootView
 			    .findViewById(R.id.story_view_webcontent);
			WebSettings settings = webView.getSettings();
			settings.setDefaultTextEncodingName("utf-8");

 			Thorfun.getInstance(getActivity()).getStory(mStory.getID(),
 			    new ThorfunResult<Story>() {
 
 				    @Override
 				    public void onResponse(Story response) {
 					    StringBuilder content = new StringBuilder(response
 					        .getStoryDescription().trim());
 
 					    if (content.length() > 0) {
 						    content.append("\n<br /><hr /><br />\n");
 					    }
 
 					    content.append(response.getStoryData());
					    webView.loadDataWithBaseURL(null, content.toString(),
					        "text/html", "utf-8", null);
 				    }
 			    });
 		} catch (JSONException e) {
 			Log.e(Thorfun.LOG_TAG, "Cannot parse JSON string", e);
 		}
 
 		return rootView;
 	}
 }
