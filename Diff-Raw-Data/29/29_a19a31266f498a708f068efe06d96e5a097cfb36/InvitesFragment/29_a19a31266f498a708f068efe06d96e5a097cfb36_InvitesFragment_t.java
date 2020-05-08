 package com.ese2013.mub;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.ese2013.mub.model.Day;
 import com.ese2013.mub.model.Mensa;
 import com.ese2013.mub.model.Model;
 import com.ese2013.mub.social.Invitation;
 import com.ese2013.mub.social.LoginService;
 import com.ese2013.mub.social.SocialManager;
 import com.ese2013.mub.util.Observer;
 
 public class InvitesFragment extends Fragment {
 
 	private ListView invitedList;
 	private InvitesListAdapter adapter;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		adapter = new InvitesListAdapter();
 		View view = inflater.inflate(R.layout.fragment_invites, null);
 		invitedList = (ListView) view.findViewById(R.id.invites_list);
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
 			inflater.inflate(R.menu.invites_menu, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.new_invite_button:
 			((DrawerMenuActivity) getActivity()).createInvitation();
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	class InvitesListAdapter extends BaseAdapter implements Observer {
 
 		private LayoutInflater inflater;
 		private List<Invitation> invitations = new ArrayList<Invitation>();
 
 		public InvitesListAdapter() {
 			SocialManager.getInstance().addObserver(this);
 			SocialManager.getInstance().loadSentInvites();
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View view = convertView;
 			if (view == null)
 				inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			view = inflater.inflate(R.layout.invites_entry_layout, null);
 			Invitation invite = invitations.get(position);
 
 			setUpWhereTextView(view, invite);
 			setUpWhenTextView(view, invite);
 
 			setUpShowInvitedButton(view, invite);
 
 			return view;
 		}
 
 		private void setUpShowInvitedButton(View view, Invitation invite) {
 			ImageButton showInvitedButton = (ImageButton) view.findViewById(R.id.show_invited);
 			showInvitedButton.setOnClickListener(new ShowInvitedListener(invite));
 		}
 
 		private void setUpWhenTextView(View view, Invitation invite) {
 			TextView whenTextView = (TextView) view.findViewById(R.id.when_text_field);
 			Day day = new Day(invite.getTime());
 			
 			DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
 			DateFormat dateFormat2 = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
 			if (day.equals(Day.today()))
 				whenTextView.setText(dateFormat.format(invite.getTime()));
 			else
 				whenTextView.setText(dateFormat2.format(invite.getTime()));
 		}
 
 		private void setUpWhereTextView(View view, Invitation invite) {
 			TextView whereTextView = (TextView) view.findViewById(R.id.where_text_field);
 			Mensa mensa = null;
 			for (Mensa m : Model.getInstance().getMensas()) {
 				if (m.getId() == invite.getMensa())
 					mensa = m;
 			}
 			whereTextView.setText(mensa.getName());
 		}
 
 		@Override
 		public int getCount() {
 			return invitations.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return invitations.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public void onNotifyChanges() {
 			this.invitations = SocialManager.getInstance().getSentInvitations();
 			notifyDataSetChanged();
 		}
 	}
 }
