 package com.xenon.greenup;
 
 import java.util.ArrayList;
 
 import com.xenon.greenup.api.APIServerInterface;
 import com.xenon.greenup.api.Comment;
 import com.xenon.greenup.api.CommentPage;
 
 import android.support.v4.app.ListFragment;
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 public class FeedSectionFragment extends ListFragment {
 	private int lastPageLoaded = 1;
 	private ArrayList<Comment> comments = new ArrayList<Comment>(60); // Default to having enough space for 3 spaces
 	private EditText editText;
 	
 	public FeedSectionFragment(){
 	}
 	
 	public void onCreate(Bundle bundle){
 		super.onCreate(bundle);
 		CommentPage cp = APIServerInterface.getComments(null,lastPageLoaded );
 		this.comments = cp.getCommentsList();
 		//Set the adapter
 		Activity currentActivity = getActivity();
 		//If we have no internet then we will get nothing back from the api
 		if(this.comments == null)
 			this.comments = new ArrayList<Comment>(60);
 		
 		//Do feed rendering async or else it will take orders of magnitude longer to render
 		new AsyncCommentLoadTask(this,currentActivity,this.comments).execute();
 	}
 	
     @Override
     /**
      * Called when the android needs to create the view, simply inflates the layout
      */
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     	View rootView = inflater.inflate(R.layout.feed, container, false);
     	editText =  (EditText)rootView.findViewById(R.id.text_entry_comments);
     	editText.setOnEditorActionListener(new OnEditorActionListener() {
     	    @Override
     	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
     	        boolean handled = false;
     	        if (actionId == EditorInfo.IME_ACTION_SEND) {
    	            Log.i("text","i send!");
     	            APIServerInterface.submitComments("forum", editText.getText().toString(), 0);
     	            handled = true;
     	        }
     	        return handled;
     	    }
     	});
        	return rootView;
     }
     
 	private class AsyncCommentLoadTask extends AsyncTask<Void,Void,Void>{
 		
 		private final Activity act;
 		private final ArrayList<Comment> cmts;
 		private final FeedSectionFragment fsf;
 		
 		/**
 		 * AsyncCommentLoadTask is the default constructor to create an asynchronous task
 		 * to load the arrayadapter as well as render the view for the comment feed. 
 		 * @param fsf An instance of the FeedSectionFragment that the adapter to load from belongs to
 		 * @param a The activity which is running the FeedSectionFragment instance
 		 * @param c The list of comments which we will bind to an arrayadapter that will populate the feeds view
 		 */
 		public AsyncCommentLoadTask(FeedSectionFragment fsf, Activity a, ArrayList<Comment> c) {
 			//apparently I've jumped back to 1982 when variable length matters
 			this.act = a;
 			this.cmts = c;
 			this.fsf = fsf;
 		}
 		
 		@Override
 		/**
 		 * The actual task to execute when this class is created and execute() is called. 
 		 * Sets the list adapter of the FeedSectionFragment to be a CommentAdapter loaded with
 		 * the activity stored within the class at construction time with the comments
 		 * passed at construction time.
 		 */
 		protected Void doInBackground(Void...voids) {
 			this.fsf.setListAdapter(new CommentAdapter(this.act,this.cmts));
 			//Java makes no sense. It requires the capital version of Void because there simply
 			//must be something returned and you have to java's bastard children, the wrapper 
 			//types instead of primitives because it's an async task. But yet, the primitive
 			//keyword null is apparent a Void type (although returning void is wrong). 
 			//sense, this makes none.
 			return null;
 		}
 	}
 
 }
