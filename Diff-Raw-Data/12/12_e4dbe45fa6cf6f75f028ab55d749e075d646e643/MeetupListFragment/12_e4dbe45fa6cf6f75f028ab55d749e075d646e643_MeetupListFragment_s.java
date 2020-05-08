 package com.codepath.apps.locateme.fragments;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 
 import com.codepath.apps.locateme.MeetupsAdapter;
 import com.codepath.apps.locateme.R;
 import com.codepath.apps.locateme.activities.MeetupStatusActivity;
 import com.codepath.apps.locateme.models.Meetup;
 import com.codepath.apps.locateme.models.UserMeetupState;
 
 import eu.erikw.PullToRefreshListView;
 import eu.erikw.PullToRefreshListView.OnRefreshListener;
 
 public class MeetupListFragment extends Fragment {
 	private long userId;
 	PullToRefreshListView lvMeetups;
 	MeetupsAdapter adapter;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		userId = getArguments().getLong("userId");
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_meetuplist, container, false);
 		return view;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		setupViews();
 	}
 
 	@Override
 	public void onResume() {
 		loadData();
 		super.onResume();
 	}
 
 	private void setupViews() {
 		lvMeetups = (PullToRefreshListView) getActivity().findViewById(R.id.lvMeetups);
 		lvMeetups.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				Meetup meetup = adapter.getItem(position);
 				Intent i = new Intent(getActivity(), MeetupStatusActivity.class);
 				i.putExtra("meetupId", meetup.getId());
 				startActivity(i);
 			}
 		});
 		lvMeetups.setOnRefreshListener(new OnRefreshListener() {
 			@Override
 			public void onRefresh() {
 				loadData();
 				lvMeetups.onRefreshComplete();
 			}
 		});
 	}
 
 	private void loadData() {
 		List<Meetup> meetups = new ArrayList<Meetup>();
 		List<UserMeetupState> states = UserMeetupState.byUserId(userId);
 		for (UserMeetupState state : states) {
 			meetups.add(Meetup.byId(state.meetupId));
 		}
		if (adapter == null) {
			adapter = new MeetupsAdapter(getActivity(), meetups, userId);
			lvMeetups.setAdapter(adapter);
		} else {
			adapter.clear();
			adapter.addAll(meetups);
		}
 	}
 }
