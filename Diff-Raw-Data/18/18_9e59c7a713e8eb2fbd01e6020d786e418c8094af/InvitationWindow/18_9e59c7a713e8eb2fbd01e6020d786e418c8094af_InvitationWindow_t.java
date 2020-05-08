 package eecs.berkeley.edu.cs294;
 
 import java.util.Date;
 import java.util.List;
 
 import eecs.berkeley.edu.cs294.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.AnalogClock;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TabHost;
 import android.widget.TableRow;
 import android.widget.TabHost.TabSpec;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TableLayout;
 import android.widget.TextView;
 
 public class InvitationWindow extends Activity {
 	/** Called when the activity is first created. */
 	TextView tv_sender, tv_recipient, tv_group, tv_description;
 	Button accept, reject;
 	TabHost th_invitation;
 	TableLayout tl_outgoing, tl_incoming;
 	
 	EditText et_title, et_place, et_note;
 	AutoCompleteTextView actv_tag;
 	Spinner s_group, s_status, s_priority;
 	Button b_submit;
 	TabHost th_add;
 	TableLayout tl_todo_lists;
 	
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.invitations);
 		
 		th_invitation = (TabHost) findViewById(R.id.th_invitation);
 		th_invitation.setup();
	
		tl_outgoing = (TableLayout) findViewById(R.id.tl_invitation);
		tl_incoming = (TableLayout) findViewById(R.id.tl_invitation);
			
 		TabSpec in_req_tab = th_invitation.newTabSpec("in_req_btn_tab");
 		in_req_tab.setContent(R.id.tl_invitation);
 		in_req_tab.setIndicator("Incoming");
 		populateIncomingRequests();
 		th_invitation.addTab(in_req_tab);
 		
 		TabSpec out_req_tab = th_invitation.newTabSpec("out_req_btn_tab");
 		out_req_tab.setContent(R.id.tl_invitation);
 		out_req_tab.setIndicator("Outgoing");
 		populateOutgoingRequests();
 		th_invitation.addTab(out_req_tab);
 		
 	}
 	
 	public void populateIncomingRequests(){
 		List<String> senders = ToDo_Replica.dh.select_all_recv_invitations("sender");
 		List<String> groups = ToDo_Replica.dh.select_all_recv_invitations("groupz");
 
 		String sender, group;
 		for (int i=0; i<senders.size(); i++) {
 			sender = senders.get(i);
 			group = groups.get(i);
 			TableRow row = new TableRow(this);		
 			row.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 
 			TextView tv_sender = new TextView(this);
 			tv_sender.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));			
 			tv_sender.setText(sender);
 			tv_sender.setTextSize(20);
 			
 			TextView tv_group = new TextView(this);
 			tv_group.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			tv_group.setText(group);
 			tv_group.setTextSize(20);
 
 			row.addView(tv_sender);
 			row.addView(tv_group);
 			row.setContentDescription(sender);
 			row.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Intent intent = new Intent(v.getContext(), InvitationConfirmation.class);
 					intent.putExtra("sender_select", v.getContentDescription());
 					startActivityForResult(intent, 3);
 				}
 			});
 			registerForContextMenu(row);
 			tl_incoming.addView(row);
 		}
 	}
 	
 	public void populateOutgoingRequests(){
 		List<String> recipients = ToDo_Replica.dh.select_all_sent_invitations("recipient");
 		List<String> groups = ToDo_Replica.dh.select_all_sent_invitations("groupz");
 		List<String> statuses = ToDo_Replica.dh.select_all_sent_invitations("status");
 
 		String recipient, group, status;
 		for (int i=0; i<recipients.size(); i++) {
 			recipient = recipients.get(i);
 			group = groups.get(i);
 			status = statuses.get(i);
 			
 			TableRow row = new TableRow(this);		
 			row.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 
 			TextView tv_recipient = new TextView(this);
 			tv_recipient.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));			
 			tv_recipient.setText(recipient);
 			tv_recipient.setTextSize(20);
 			
 			TextView tv_group = new TextView(this);
 			tv_group.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			tv_group.setText(group);
 			tv_group.setTextSize(20);
 
 			TextView tv_status = new TextView(this);
 			tv_status.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			tv_status.setText(status);
 			tv_status.setTextSize(20);
 			
 			row.addView(tv_recipient);
 			row.addView(tv_group);
 			row.setContentDescription(recipient);
 			row.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Intent intent = new Intent(v.getContext(), InvitationConfirmation.class);
 					intent.putExtra("recipient_select", v.getContentDescription());
 					startActivityForResult(intent, 3);
 				}
 			});
 			registerForContextMenu(row);
 			tl_outgoing.addView(row);
 		}
 	}
 }
