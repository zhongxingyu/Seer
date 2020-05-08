 package com.randy.fbsample.fragments;
 
 import java.util.ArrayList;
import java.util.Arrays;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.facebook.HttpMethod;
 import com.facebook.Request;
 import com.facebook.Response;
 import com.facebook.Session;
 import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
 import com.randy.fbsample.FBSampleActivity;
 import com.randy.fbsample.FBSampleApplication;
 import com.randy.fbsample.R;
 import com.randy.fbsample.adapters.PhotoNewsListViewAdapter;
 
 public class ProfileFragment extends Fragment {
 	private static final String TAG = "ProfileFragment";
 	
	private ProfilePictureView mUserProfileImage;
 	private TextView mUserNameTextView;
 	private EditText mStatusEditText;
 	private Button mMyActionButton;
 	private Button mPostStatusButton;
 	private ListView mPhotoStoriesListView;
 	
 	//Data
 	private GraphUser mMeUser;
 	private ArrayList<JSONObject> mNewsFeedPhotoItems;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View profileFragmentView = inflater.inflate(R.layout.fragment_profile, container, false);
 		
		mUserProfileImage = (ProfilePictureView) profileFragmentView.findViewById(R.id.user_profile_pic);
 		mUserNameTextView = (TextView) profileFragmentView.findViewById(R.id.username_text_view);
 		mStatusEditText = (EditText) profileFragmentView.findViewById(R.id.user_status_edit_text);
 		mMyActionButton = (Button) profileFragmentView.findViewById(R.id.custom_action_btn);
 		mPostStatusButton = (Button) profileFragmentView.findViewById(R.id.post_status_btn);
 		mPhotoStoriesListView = (ListView) profileFragmentView.findViewById(R.id.photo_stories_list_view);
 		
 		//setup views
 		mStatusEditText.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void afterTextChanged(Editable s) {}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				if (count > 0) {
 					mPostStatusButton.setEnabled(true);
 				} else {
 					mPostStatusButton.setEnabled(false);
 				}
 			}
 		});
 		
 		mPostStatusButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				postStatus(Session.getActiveSession(), mStatusEditText.getText().toString());
 				
 				mStatusEditText.setText("");
 				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
 				imm.hideSoftInputFromWindow(mStatusEditText.getWindowToken(), 0);
 			}
 		});
 		
 		mMyActionButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				recommendRandy(Session.getActiveSession());
 			}
 		});
 		
 		return profileFragmentView;
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		FBSampleActivity fbActivity = FBSampleApplication.getFBSampleActivity(); //Use to to control my nav
 		
 		if (Session.getActiveSession() == null || Session.getActiveSession().isClosed()) {
 			fbActivity.goToNavFragment(fbActivity.FRAG_LOGIN);
 		} else {
 			if (mMeUser == null) {
 				fetchMeUser(Session.getActiveSession());
 			} else { //Already have data no need to fetch
 				loadMeUser();
 			}
 			
 			if (mNewsFeedPhotoItems == null) {
 				fetchNewsFeedPhotos(Session.getActiveSession());
 			} else {
 				loadNewsFeedPhotos();
 			}
 		}
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 
 	//communicate with FB
 	private void postStatus(final Session session, final String status) {
 		Request request = Request.newStatusUpdateRequest(session, status, new Request.Callback() {
 			@Override
 			public void onCompleted(Response response) {
 				if (response.getError() == null) {
 					Toast confirmToast = Toast.makeText(getActivity(), "Your status was updated.", Toast.LENGTH_LONG);
 					confirmToast.show();
 				} else {
 					Log.e(TAG, "Error in status update response - " + response.getError().getErrorMessage());
 					
 					Toast failToast = Toast.makeText(getActivity(), "Error in status update response - " + response.getError().getErrorMessage(), Toast.LENGTH_LONG);
 					failToast.show();
 				}
 			}
 		});
 		request.executeAsync();
 	}
 	
 	private void recommendRandy(final Session session) {
 		String randyRecommendUrl = "https://fast-coast-7268.herokuapp.com/";
 		
 		Bundle params = new Bundle();
         params.putString("recommendation", randyRecommendUrl);
         
         Request request = new Request(session, "/me/randy_recommends:submit", params, HttpMethod.POST, new Request.Callback(){         
         	public void onCompleted(Response response) {
         		if (response.getError() == null) {
         			Toast confirmToast = Toast.makeText(getActivity(), "Your action was accepted.", Toast.LENGTH_LONG);
 					confirmToast.show();   
 	            } else {
 	            	Log.e(TAG, "Error in recommend response - " + response.getError().getErrorMessage());
 	            	Toast failToast = Toast.makeText(getActivity(), "Error in status update response - " + response.getError().getErrorMessage(), Toast.LENGTH_LONG);
 					failToast.show();
 	            }
         	}                  
         });
         
         request.executeAsync();
 	}
 	
 	private void fetchMeUser(final Session session) {
 	    Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
 	        @Override
 	        public void onCompleted(GraphUser user, Response response) {
 	            if (session == Session.getActiveSession()) {
 	                if (user != null) {
 	                	mMeUser = user;
 	                	loadMeUser();
 	                }
 	            }
 	            if (response.getError() != null) {
 	                Log.e(TAG, "Error in response - " + response.getError().getErrorMessage());
 	            }
 	        }
 	    });
 	    request.executeAsync();
 	}
 	
 	private void loadMeUser() {
 		if (mMeUser != null) {
			mUserProfileImage.setProfileId(mMeUser.getId());
 			mUserNameTextView.setText(mMeUser.getName());
 		}
 	}
 	
 	//get newfeed photos
 	private void fetchNewsFeedPhotos(final Session session) {
 		String fqlQuery = "SELECT post_id, message, attachment FROM stream WHERE filter_key IN (SELECT filter_key FROM stream_filter WHERE uid=me() AND name=\"Photos\")";
 		Bundle params = new Bundle();
         params.putString("q", fqlQuery);
         
         Request request = new Request(session, "/fql", params, HttpMethod.GET, new Request.Callback(){         
         	public void onCompleted(Response response) {
         		if (response.getError() == null) {
         			try {
         				JSONObject responseJson = response.getGraphObject().getInnerJSONObject();
         				JSONArray dataArray = responseJson.getJSONArray("data");
         				
         				mNewsFeedPhotoItems = new ArrayList<JSONObject>();
         				
         				for (int i = 0; i < dataArray.length(); i++) {
         					mNewsFeedPhotoItems.add(dataArray.getJSONObject(i));
         				}
         				
         				loadNewsFeedPhotos();
         			} catch (Exception e) {
         				Log.e(TAG, "Error parsing response JSON - " + Log.getStackTraceString(e));
         			}   
 	            } else {
 	            	Log.e(TAG, "Error in response - " + response.getError().getErrorMessage());
 	            }
         	}                  
         }); 
         Request.executeBatchAsync(request); 
 	}
 	
 	private void loadNewsFeedPhotos() {
 		if (getActivity() != null && mNewsFeedPhotoItems != null) {
 			
 			mPhotoStoriesListView.setAdapter(new PhotoNewsListViewAdapter(getActivity(), mNewsFeedPhotoItems));
 		}
 	}
 }
