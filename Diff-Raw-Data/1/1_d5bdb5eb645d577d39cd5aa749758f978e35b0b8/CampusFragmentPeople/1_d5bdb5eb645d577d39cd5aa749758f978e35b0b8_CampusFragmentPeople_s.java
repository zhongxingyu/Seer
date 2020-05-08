 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.cm.fragments.campus;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragment;
 
 import eu.trentorise.smartcampus.android.common.SCAsyncTask;
 import eu.trentorise.smartcampus.cm.HomeActivity;
 import eu.trentorise.smartcampus.cm.R;
 import eu.trentorise.smartcampus.cm.custom.AbstractAsyncTaskProcessor;
 import eu.trentorise.smartcampus.cm.custom.UsersPictureProfileAdapter;
 import eu.trentorise.smartcampus.cm.custom.UsersPictureProfileAdapter.UserOptionsHandler;
 import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
 import eu.trentorise.smartcampus.cm.model.PictureProfile;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
 import eu.trentorise.smartcampus.social.model.Group;
 
 public class CampusFragmentPeople extends SherlockFragment {
 
 	private static final String ARG_GROUP = "ARG_GROUP";
 	ArrayAdapter<PictureProfile> usersListAdapter;
 	List<PictureProfile> usersList = new ArrayList<PictureProfile>();
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		if (getSherlockActivity().getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
 			getSherlockActivity().getSupportActionBar().setNavigationMode(
 					ActionBar.NAVIGATION_MODE_STANDARD);
 		}
 		return inflater.inflate(R.layout.people, container, false);
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
 		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
 		getSherlockActivity().getSupportActionBar().setTitle(R.string.campus_title);
 
 		ImageButton search = (ImageButton) getView().findViewById(R.id.people_search_img);
 		search.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				new SCAsyncTask<String, Void, List<PictureProfile>>(getActivity(), new LoadUserProcessor(getActivity())).execute(((EditText)getView().findViewById(R.id.people_search)).getText().toString());
 			}
 		});
 		
 		ListView usersListView = (ListView) getView().findViewById(R.id.people_listview);
 		Set<String> initGroups = null;
 		if (getArguments() != null && getArguments().containsKey(ARG_GROUP)) {
 			initGroups = Collections.singleton(getArguments().getString(ARG_GROUP));
 		}
 		usersListAdapter = new UsersPictureProfileAdapter(getActivity(), R.layout.user_mp, new PeopleUserOptionsHandler(), initGroups);
 		usersListView.setAdapter(usersListAdapter);
 		super.onStart();
 	}
 
 	private class LoadUserProcessor extends AbstractAsyncTaskProcessor<String, List<PictureProfile>> {
 
 		public LoadUserProcessor(Activity activity) {
 			super(activity);
 		}
 
 		@Override
 		public List<PictureProfile> performAction(String... params) throws SecurityException, Exception {
 			return CMHelper.getPeople(params[0]);
 		}
 
 		@Override
 		public void handleResult(List<PictureProfile> result) {
 			usersListAdapter.clear();
 			if (result != null){
 				for (PictureProfile mp : result) {
 					if(mp.getId().equals(HomeActivity.picP.getId())){
 						usersListAdapter.remove(mp);;
 					}
 					else
 					usersListAdapter.add(mp);
 				}
 			}
 			usersListAdapter.notifyDataSetChanged();
 			
 			eu.trentorise.smartcampus.cm.custom.ViewHelper.removeEmptyListView((LinearLayout)getView().findViewById(R.id.layout_people));
 			if (result == null || result.isEmpty()) {
 				eu.trentorise.smartcampus.cm.custom.ViewHelper.addEmptyListView((LinearLayout)getView().findViewById(R.id.layout_people), R.string.people_list_empty);
 			}
 		}
 
 	}
 	
 	private class PeopleUserOptionsHandler implements UserOptionsHandler {
 
 		@Override
 		public void assignUserToGroups(PictureProfile user, Collection<Group> groups) {
 			new SCAsyncTask<Object, Void, PictureProfile>(getActivity(), new AssignToGroups(getActivity())).execute(user,groups);
 		}
 		
 	}
 
 	private class AssignToGroups extends AbstractAsyncTaskProcessor<Object, PictureProfile> {
 
 		public AssignToGroups(Activity activity) {
 			super(activity);
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		public PictureProfile performAction(Object... params) throws SecurityException, Exception {
 			if (CMHelper.assignToGroups((PictureProfile)params[0], (Collection<Group>)params[1]))  {
 				return (PictureProfile)params[0];
 			}
 			return null;
 		}
 
 		@Override
 		public void handleResult(PictureProfile result) {
 			if (result != null) {
 				usersListAdapter.notifyDataSetChanged();
 			}
 		}
 	}
 	
 	public static Bundle prepareArgs(String socialId) {
 		Bundle b = new Bundle();
 		b.putString(ARG_GROUP, socialId);
 		return b;
 	}
 }
