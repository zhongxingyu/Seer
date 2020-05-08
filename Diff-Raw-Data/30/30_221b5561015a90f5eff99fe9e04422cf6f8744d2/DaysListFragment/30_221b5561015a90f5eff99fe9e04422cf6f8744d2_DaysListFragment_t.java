 package com.hasgeek.fragment;
 
 import android.app.ListFragment;
 import android.app.LoaderManager;
 import android.content.Intent;
 import android.content.Loader;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.hasgeek.R;
 import com.hasgeek.activity.SessionDetailActivity;
 import com.hasgeek.misc.EventSession;
 import com.hasgeek.misc.SessionsListLoader;
 
 import java.util.List;
 
 
 public class DaysListFragment extends ListFragment
         implements LoaderManager.LoaderCallbacks<List<EventSession>> {
 
     public static final int BOOKMARKED_SESSIONS = 198263;
     public static final int All_SESSIONS = 198264;
 
    private SessionsListAdapter mAdapter;
     private Button mToggleBookmarksButton;
     private static final int REQUEST_SESSION_DETAIL = 4201;
    private List<EventSession> mSessionsList;
     private int mListMode;
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mListMode = All_SESSIONS;
        mAdapter = new SessionsListAdapter();
         getLoaderManager().initLoader(0, null, this);
     }
 
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.fragment_sessionslist, container, false);
         mToggleBookmarksButton = (Button) v.findViewById(R.id.btn_toggle_bookmarked);
         mToggleBookmarksButton.setText(R.string.show_only_bookmarked);
         mToggleBookmarksButton.setOnClickListener(toggleBookmarksButtonClickListener);
         return v;
     }
 
 
     @Override
     public Loader<List<EventSession>> onCreateLoader(int i, Bundle bundle) {
         if (mListMode == All_SESSIONS) {
             return new SessionsListLoader(getActivity(), All_SESSIONS);
         } else if (mListMode == BOOKMARKED_SESSIONS) {
             return new SessionsListLoader(getActivity(), BOOKMARKED_SESSIONS);
         } else {
             throw new RuntimeException("List mode missing");
         }
     }
 
 
     @Override
     public void onLoadFinished(Loader<List<EventSession>> listLoader, List<EventSession> eventSessions) {
        mSessionsList = eventSessions;
        if (getListView().getAdapter() == null) {
            setListAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
     }
 
 
     @Override
     public void onLoaderReset(Loader<List<EventSession>> listLoader) {
     }
 
 
     private class SessionsListAdapter extends BaseAdapter {
 
         @Override
         public int getCount() {
            return mSessionsList.size();
         }
 
 
         @Override
         public EventSession getItem(int i) {
            return mSessionsList.get(i);
         }
 
 
         @Override
         public long getItemId(int i) {
             return i;
         }
 
 
         @Override
         public View getView(int position, View convertView, ViewGroup viewGroup) {
             if (convertView == null) {
                 convertView = LayoutInflater.from(getActivity()).inflate(R.layout.row_session, viewGroup, false);
             }
 
             TextView title = (TextView) convertView.findViewById(R.id.tv_session_title);
            title.setText(mSessionsList.get(position).getTitle());
 
             TextView speaker = (TextView) convertView.findViewById(R.id.tv_session_speaker);
            speaker.setText(mSessionsList.get(position).getSpeaker());
 
             return convertView;
         }
     }
 
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
         Intent i = new Intent(getActivity(), SessionDetailActivity.class);
        i.putExtra("session", mSessionsList.get(position));
         startActivityForResult(i, REQUEST_SESSION_DETAIL);
     }
 
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
             case REQUEST_SESSION_DETAIL:
                 getLoaderManager().restartLoader(0, null, this);
         }
     }
 
 
     private View.OnClickListener toggleBookmarksButtonClickListener = new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             if (mListMode == All_SESSIONS) {
                 mListMode = BOOKMARKED_SESSIONS;
                 mToggleBookmarksButton.setText(R.string.show_all_sessions);
             } else {
                 mListMode = All_SESSIONS;
                 mToggleBookmarksButton.setText(R.string.show_only_bookmarked);
             }
             getLoaderManager().restartLoader(0, null, DaysListFragment.this);
         }
     };
 }
