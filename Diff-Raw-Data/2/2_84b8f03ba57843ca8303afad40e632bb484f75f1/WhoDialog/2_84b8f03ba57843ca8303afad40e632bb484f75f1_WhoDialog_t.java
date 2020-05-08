 package no.whg.whirc.dialogs;
 
 import no.whg.whirc.R;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class WhoDialog extends DialogFragment {
 	private LinearLayout channelsView;
 	private LinearLayout nickView;
 	private LinearLayout nameView;
 	private LinearLayout serverView;
 	private LinearLayout serverInfoView;
 	private LinearLayout signedOnView;
 	private LinearLayout idleView;
 	private LinearLayout awayView;
 	
 	private TextView channelsContent;
 	private TextView nickContent;
 	private TextView nameContent;
 	private TextView serverContent;
 	private TextView serverInfoContent;
 	private TextView signedOnContent;
 	
 	private String channels = "";
 	private String nick = "";
 	private String name = "";
 	private String server = "";
 	private String serverInfo = "";
 	private String signedOn = "";
 	private boolean idle;
 	private boolean away;
 	
 	public WhoDialog(String[] channels, String nick, String name, 
 			String server, String serverInfo, String signedOn, boolean idle, boolean away) {
 		for (int i = 0; i < channels.length; i++) {
 			this.channels += ("@#" + channels[i] + " ");
 		}
 		this.nick = nick;
 		this.name = name;
 		this.server = server;
 		this.serverInfo = serverInfo;
 		this.signedOn = signedOn;
 		this.idle = idle;
 		this.away = away;
 	}
 
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {		
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		LayoutInflater inflater = getActivity().getLayoutInflater();
 		
 		// Retrieve main element in dialog_who.xml
 		View mainView = inflater.inflate(R.layout.dialog_who, null);
 		
 		setLayouts(mainView);
 		
 		// Setup onclick listeners for both buttons in the dialog
 		builder.setView(mainView)
 			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 				}
 			});
 		
		return builder.create();
 	}
 	
 	/**
 	 * Separate function for dealing with view elements in the layout.
 	 * Purely here to make it more readable
 	 * 
 	 * @param mainView
 	 */
 	private void setLayouts(View mainView) {		
 		/*
 		 * Retrieve LinearLayouts in mainView and hides those who have no content
 		 * Also sets content where applicable
 		 */
 		// CHANNELS
 		channelsView = (LinearLayout)mainView.findViewById(R.id.dialog_who_channels);	
 		if (channels != "") {		
 			channelsContent = (TextView)mainView.findViewById(R.id.dialog_who_channels_content);
 			channelsContent.setText(channels);
 		} else 
 			channelsView.setVisibility(View.INVISIBLE);
 		
 		// NICK
 		nickView = (LinearLayout)mainView.findViewById(R.id.dialog_who_nick);
 		if (nick != "") {
 			nickContent = (TextView)mainView.findViewById(R.id.dialog_who_nick_content);
 			nickContent.setText(nick);
 		} else
 			nickView.setVisibility(View.INVISIBLE);
 		
 		// NAME
 		nameView = (LinearLayout)mainView.findViewById(R.id.dialog_who_name);
 		if (name != "") {
 			nameContent = (TextView)mainView.findViewById(R.id.dialog_who_name_content);
 			nameContent.setText(name);
 		} else
 			nameView.setVisibility(View.INVISIBLE);
 		
 		// SERVER
 		serverView = (LinearLayout)mainView.findViewById(R.id.dialog_who_server);
 		if (server != "") {
 			serverContent = (TextView)mainView.findViewById(R.id.dialog_who_server_content);
 			serverContent.setText(server);
 		} else
 			serverView.setVisibility(View.INVISIBLE);
 		
 		// SERVERINFO
 		serverInfoView = (LinearLayout)mainView.findViewById(R.id.dialog_who_serverInfo);
 		if (serverInfo != "") {
 			serverInfoContent = (TextView)mainView.findViewById(R.id.dialog_who_serverInfo_content);
 			serverInfoContent.setText(serverInfo);
 		} else
 			serverInfoView.setVisibility(View.INVISIBLE);
 		
 		// SIGNEDON
 		signedOnView = (LinearLayout)mainView.findViewById(R.id.dialog_who_signedOn);
 		if (signedOn != "") {
 			signedOnContent = (TextView)mainView.findViewById(R.id.dialog_who_signedOn_content);
 			signedOnContent.setText(signedOn);
 		} else
 			signedOnView.setVisibility(View.INVISIBLE);
 		
 		// IDLE
 		if (!idle) {
 			idleView = (LinearLayout)mainView.findViewById(R.id.dialog_who_idle);
 			idleView.setVisibility(View.INVISIBLE);
 		}
 		
 		// AWAY
 		if (!away) {
 			awayView = (LinearLayout)mainView.findViewById(R.id.dialog_who_away);
 			awayView.setVisibility(View.INVISIBLE);
 		}
 		
 	}
 }
