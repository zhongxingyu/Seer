 package com.ese2013.mub.social;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.ese2013.mub.DrawerMenuActivity;
 import com.ese2013.mub.R;
 import com.ese2013.mub.social.model.LoginService;
 import com.ese2013.mub.social.model.SocialManager;
 import com.ese2013.mub.util.Observer;
 
 /**
  * Page of the {@link SocialFragment}, shows where you are invited or
  * the sent invitations.
  */
 public class InvitationsFragment extends Fragment implements Observer {
 
 	private ListView invitedList;
 	private InvitationsBaseAdapter adapter;
 	private MenuItem menuItem;
 
 	/**
 	 * Creates a new instance of InvitedFragment using the given adapter.
 	 * 
 	 * @param adapter
 	 *            The InvitationsBaseAdapter to be used. Must not be null.
 	 * @return the newly created InvitedFragment.
 	 */
 	public static Fragment newInstance(InvitationsBaseAdapter adapter) {
 		InvitationsFragment frag = new InvitationsFragment();
 		frag.setAdapter(adapter);
 		return frag;
 	}
 
 	private void setAdapter(InvitationsBaseAdapter adapter) {
 		SocialManager.getInstance().addObserver(adapter);
 		this.adapter = adapter;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		SocialManager.getInstance().addObserver(this);
 		adapter.setContext(getActivity());
 		View view = inflater.inflate(R.layout.fragment_invited, null);
 		invitedList = (ListView) view.findViewById(R.id.invited_list);
 
 		TextView showMessage = (TextView) view.findViewById(R.id.show_message);
 		if (LoginService.isLoggedIn())
 			showMessage.setText(R.string.no_invites);
 		else
 			showMessage.setText(R.string.not_loged_in);
 
 		invitedList.setEmptyView(showMessage);
 		invitedList.setAdapter(adapter);
 		setHasOptionsMenu(true);
 		return view;
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		if (LoginService.isLoggedIn())
 			inflater.inflate(R.menu.invitations_menu, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.new_invite_button:
 			((DrawerMenuActivity) getActivity()).createInvitation();
 			return true;
 		case R.id.refresh:
 			SocialManager.getInstance().loadInvites();
 			menuItem = item;
 			menuItem.setActionView(R.layout.progress_bar);
 			menuItem.expandActionView();
 			Toast.makeText(getActivity(), R.string.toast_refreshing_msg, Toast.LENGTH_SHORT).show();
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		SocialManager socialMgr = SocialManager.getInstance();
 		socialMgr.removeObserver(this);
 		socialMgr.removeObserver(adapter);
 	}
 
 	@Override
 	public void onNotifyChanges(Object... message) {
 		// the social manager does callbacks only in such cases, so no need to
 		// handle any other case (at the moment),
 		loadingFinished();
 	}
 
 	/**
 	 * changes the progressbar in the ActionBar back to not loading
 	 */
 	public void loadingFinished() {
 		if (menuItem != null) {
 			menuItem.collapseActionView();
 			menuItem.setActionView(null);
 		}
 	}
 
 }
