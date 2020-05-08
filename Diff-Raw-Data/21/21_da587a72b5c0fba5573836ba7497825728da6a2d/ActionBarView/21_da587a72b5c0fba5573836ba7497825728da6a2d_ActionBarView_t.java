 /**
  * 
  */
 package fr.nikokode.helloandy4;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.view.View.OnClickListener;;
 
 /**
  * @author STAGIAIRE
  *
  */
public class ActionBarView extends LinearLayout 
implements OnClickListener, OnDispatchClickListener {
 
 	private View mConvertView;
 	private ImageButton mButtonSearch;
 	private ImageButton mButtonComment;
 	private ImageButton mButtonHome;
 	private ProgressBar mProgressBar;
 	private TextView mTitle;
 	private OnDispatchClickListener mListenerClick;
 	
	private boolean loading = false;
	
 	public ActionBarView(Context context, AttributeSet attrs) {
 		super(context,attrs);
 		mConvertView = LayoutInflater.from(context).inflate(R.layout.action_bar_view, this);
 		mButtonSearch = (ImageButton)mConvertView.findViewById(R.id.ab_search);
 		mButtonComment =
 				(ImageButton)mConvertView.findViewById(R.id.ab_comment);
 		mButtonHome =
 				(ImageButton)mConvertView.findViewById(R.id.ab_home);
 		mProgressBar = (ProgressBar)mConvertView.findViewById(R.id.ab_loading);
 		mTitle = (TextView)mConvertView.findViewById(R.id.ab_title);
 		mButtonHome.setOnClickListener(this);
 		mButtonSearch.setOnClickListener(this);
 		mButtonComment.setOnClickListener(this);
 		mProgressBar.setOnClickListener(this);
		setOnDispatchClickListener(this);
 	}
 	
 	public void showButtonSearch() {
 		mButtonSearch.setVisibility(View.VISIBLE);
 	}
 
 	public void showButtonComment() {
 		mButtonComment.setVisibility(View.VISIBLE);
 	}
 	
 	public void setTitle(String _t) {
 		mTitle.setText(_t);
 	}
 	
 	public void loading() {
 		mProgressBar.setVisibility(View.VISIBLE);
 	}
 	
 	public void loaded() {
 		mProgressBar.setVisibility(View.GONE);
 	}
 	
 	public void onClick(View v) {
 		mListenerClick.onDispatchClick(v.getId());
 	}
 	
 	public void setOnDispatchClickListener(OnDispatchClickListener v) {
 		mListenerClick = v;
 	}

	public void onDispatchClick(int id) {
		if (loading) {
			loaded();
			setTitle("Done!");
		} else {
			setTitle("Loading...");
			loading();
		}
		loading = !loading;		
	}
	
 }
