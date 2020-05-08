 package org.encorelab.sail.helioroom;
 
 
 
 import org.encorelab.sail.Event;
 import org.encorelab.sail.android.EventListener;
 import org.encorelab.sail.android.EventResponder;
 import org.encorelab.sail.android.xmpp.XMPPThread;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class InquiryTab extends Activity implements OnClickListener {
 	private String type = "";
 	EditText qTitle = null;
 	EditText qContent = null;
 	EditText dTitle = null;
 	EditText dContent = null;
 	TextView vTitle = null;
 	TextView vNote = null;
 	TextView vComment = null;
 	EditText vEdit = null;
 	private Spinner colourSpinner = null;
 	private Spinner planetSpinner = null;
 	Button submit = null;
 	
 	String groupId = HelioroomLogin.groupId;						//set at login screen
 	//private XmppService service;
 	private XMPPThread xmpp;
 	
 	
 	InquiryAdapter qAdapter = null;
 	DiscussionAdapter dAdapter = null;
 
 	Inquiry currentInq = null;			//used for Inq Disc Viewer
 	int idCounter = 1;					//will this be reset every time this tab is opened (POTENTIAL BUG)
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		Handler xmppHandler = new Handler();
 		EventListener listener = new EventListener(xmppHandler);
 
 		listener.addResponder("inquiry_submitted", new EventResponder() {
 			@Override
 			public void respond(Event ev) {
 				// i.setInqId(some int inqId);
 				Log.d(Helioroom.TAG, "Got inquiry!");
 				Inquiry i = (Inquiry) ev.getPayload(Inquiry.class);
 				
 				if (i.getInqType().equals("question")) {
 					qAdapter.add(i);
 					qAdapter.notifyDataSetChanged();
 				} else if (i.getInqType().equals("discussion")) {
 					dAdapter.add(i);
 					dAdapter.notifyDataSetChanged();
 				}
 				/*
 				 * TODO we need to update the inquiry that is commented on (delete insert),
 				 * also the commenting seems to result in a crashing application.
 				 */
 				else if (false && i.getInqType().equals("question with comments")) {
 					int listPos = 0;
 					int listSize = Helioroom.inqList.size();
 					// iterates through the inq list, checking for an
 					// Inquiry with matching inqId and inqType
 					while (listPos < listSize) {
 						int listInqId = Helioroom.inqList.get(listPos).getInqId();
 						String listInqGroup = Helioroom.inqList.get(listPos).getInqGroup();
 						int rvdInqId = i.getInqId();
 						String rvdInqGroup = i.getInqGroup();
 						
 						if (listInqId == rvdInqId && listInqGroup.equals(rvdInqGroup)) {
 							qAdapter.remove(i);
 							qAdapter.insert(i, listPos);
 						}
 						listPos++;
 					}
 					listPos = 0;
 					qAdapter.notifyDataSetChanged();
 				}
 				/*
 				 * TODO: fix once question with comments handling is fixed
 				 */
 				else if (false && i.getInqType().equals("discussion with comments")) {
 					int listPos = 0;
 					// iterates through the disc list, checking for an
 					// Inquiry with matching inqId and inqType
 					while (listPos < Helioroom.discList.size()) {
 						if ((Helioroom.inqList.get(listPos).getInqId() == i
 								.getInqId())
 								&& (Helioroom.inqList.get(listPos).getInqGroup()
 										.equals(i.getInqGroup()))) {
 							dAdapter.insert(i, listPos);
 						}
 						listPos++;
 					}
 					dAdapter.notifyDataSetChanged();
 				}
 
 			}
 		});
 		
 		Helioroom.xmpp.addEventListener(listener);
 		
 		
 		setContentView(R.layout.inquiry);
 
 		//qTitle = (EditText) findViewById(R.id.inqTitle);
 		qContent = (EditText) findViewById(R.id.inqNote);
 		dTitle = (EditText) findViewById(R.id.discTitle);
 		dContent = (EditText) findViewById(R.id.discNote);
 		vTitle = (TextView) findViewById(R.id.viewerTitle);
 		vNote = (TextView) findViewById(R.id.viewerNote);
 		vComment = (TextView) findViewById(R.id.viewerComment);
 		vEdit = (EditText) findViewById(R.id.viewerEdit);
 		ListView qList = (ListView) findViewById(R.id.inqList);
 		ListView dList = (ListView) findViewById(R.id.discList);
 			
 		submit = (Button) findViewById(R.id.contribButton);
 		submit.setOnClickListener(onInqSubmit);
 		
 		qAdapter = new InquiryAdapter();
 		dAdapter = new DiscussionAdapter();
 		qList.setAdapter(qAdapter);
 		dList.setAdapter(dAdapter);
 
 		qList.setOnItemClickListener(onListClickInq);
 		dList.setOnItemClickListener(onListClickDisc);
 		
 		colourSpinner = (Spinner) findViewById(R.id.colourSpinner);
 	    ArrayAdapter colourAdapter = ArrayAdapter.createFromResource(
 	            this, R.array.colours, android.R.layout.simple_spinner_item);
 	    colourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    colourSpinner.setAdapter(colourAdapter);
 
 	    planetSpinner = (Spinner) findViewById(R.id.planetSpinner);
 	    ArrayAdapter planetAdapter = ArrayAdapter.createFromResource(
 	            this, R.array.planets, android.R.layout.simple_spinner_item);
 	    planetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    planetSpinner.setAdapter(planetAdapter);
 
 		//TODO:
 		//Add a toast to let the idiots know theyve filled too many fields
 		//
 		//XML
 		//Add boxes around the three lists (LOW PRIORITY)
 		//Visible scroll bar (LOW PRIORITY)
 
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 //		HelioroomTab.nt.disconnect();
 //		HelioroomTab.nt.interrupt();
 	}
 	
 
 	// Called when the user clicks contribute button
 	private View.OnClickListener onInqSubmit = new View.OnClickListener() {
 		public void onClick(View v) {
 			
 			// the following if loops exist to force the correct behaviour with the Contribute button
 			// ie only allow a contrib if exactly Title and Note of one column are not null
 			// contrib for Question
 //			if (dTitle.getText().toString().equals("") && dContent.getText().toString().equals("") && vEdit.getText().toString().equals("")
 //				&& !qTitle.getText().toString().equals("") && !qContent.getText().toString().equals("")) {
 			if (dTitle.getText().toString().equals("") && dContent.getText().toString().equals("") && vEdit.getText().toString().equals("")
 				&& !qContent.getText().toString().equals("")) {
 					
 					Inquiry i = new Inquiry();
 					i.setInqId(idCounter);
 					i.setInqType("question");
 					i.setInqGroup(groupId);
 					//i.setInqTitle(qTitle.getText().toString());
 					i.setInqTitle((String) colourSpinner.getSelectedItem() + " is " + (String) planetSpinner.getSelectedItem());
 					i.setInqContent(qContent.getText().toString());
 
 					//qAdapter.add(i);
 					idCounter++;
 
 					Event ev = new Event("inquiry_submitted", i);
 
 					Helioroom.xmpp.sendEvent(ev);
 					
 			}
 			// contrib for Disc
 			else if (qContent.getText().toString().equals("") && vEdit.getText().toString().equals("")
 				&& !dTitle.getText().toString().equals("") && !dContent.getText().toString().equals("")) {
 					
 					Inquiry i = new Inquiry();
 					i.setInqId(idCounter);
 					i.setInqType("discussion");				
 					i.setInqGroup(groupId);
 					i.setInqTitle(dTitle.getText().toString());
 					i.setInqContent(dContent.getText().toString());
 
 					//dAdapter.add(i);
 					idCounter++;
 								
 					Event ev = new Event("inquiry_submitted", i);
 					
 					Helioroom.xmpp.sendEvent(ev);
 			}
 			// contrib for Viewer (god this is ugly)
 			else if (qContent.getText().toString().equals("") &&
 				dTitle.getText().toString().equals("") && dContent.getText().toString().equals("") &&
				!vEdit.getText().toString().equals("") && !vTitle.getText().toString().equals("")) {
 				if (!Helioroom.inqList.isEmpty() || !Helioroom.discList.isEmpty()) {		//locks contrib button if the lists are empty
 
 					
 					// if question title is filled we are sending a comment to a question
 					if (type.equals("question") || type.equals("question with comments")) {
 						currentInq.setInqType("question with comments");
 					}
 					// if discussion title is filled we are sending a comment to a discussion
 					else if (type.equals("discussion") || type.equals("discussion with comments")) {
 						currentInq.setInqType("discussion with comments");
 					}
 					/*FIXME we need to set the group ID here. This will result in some
 					 * work in the event handler. We cannot have groupid being part of the
 					 * identifier.
 					 */
 					currentInq.setInqGroup(groupId);
 					currentInq.addInqComment(vEdit.getText().toString());
 					vComment.setText(currentInq.getInqComments());
 //					Event ev = new Event("inquiry_submitted", i);			//will this just create a new, or overwrite?
 //					ev.toJson();
 					Event ev = new Event("inquiry_submitted", currentInq);
 					
 					Helioroom.xmpp.sendEvent(ev);
 				}
 			}
 
 			else {
 				Toast toast = Toast.makeText(InquiryTab.this, "Please make sure your question or comment contains both a title and a note", Toast.LENGTH_LONG);
 				toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
 				toast.show();
 
 				//qTitle.setText("");
 				qContent.setText("");
 				dTitle.setText("");
 				dContent.setText("");
 				vEdit.setText("");
 			}
 			
 			//qTitle.setText("");
 			qContent.setText("");
 			dTitle.setText("");
 			dContent.setText("");
 			vEdit.setText("");
 		}
 	};
 	
 	
 	// allows you to click on the inqList items to show them in the viewer
 	private AdapterView.OnItemClickListener onListClickInq = new AdapterView.OnItemClickListener() {
 
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 
 			//for (int x=0; x < Helioroom.inqList.size(); x++) {
 			/*for (int x=0; x < parent.getCount(); x++) {
 				parent.getChildAt(x).setBackgroundColor(Color.argb(0,0,0,0));	//turn the background color off
 //				Helioroom.discList(x).setBackgroundColor(Color.argb(0,0,0,0));		//how can I access the 'dom'?
 			}*/
 				
 //			parent.getChildAt(selectedInqPos).setBackgroundColor(Color.argb(0,0,0,0));
 
 			Inquiry i = Helioroom.inqList.get(position);
 			type = i.getInqType();
 			vTitle.setText(i.getInqTitle());
 			vNote.setText(i.getInqContent());
 			vComment.setText(i.getInqComments());
 			currentInq = i;
 			//vTitle.setBackgroundColor(Color.argb(255,200,0,200));
 			//parent.getChildAt(position).setBackgroundColor(Color.argb(255,200,0,200));
 //			selectedInqPos = position;				//var to switch background color off on the next onclick
 		}
 	};
 
 	private AdapterView.OnItemClickListener onListClickDisc = new AdapterView.OnItemClickListener() {
 
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 
 			/*for (int x=0; x < Helioroom.discList.size(); x++) {
 				parent.getChildAt(x).setBackgroundColor(Color.argb(0,0,0,0));	//turn the background color off
 			}*/
 //			parent.getChildAt(selectedInqPos).setBackgroundColor(Color.argb(0,0,0,0));	//turn the background color off
 
 			Inquiry i = Helioroom.discList.get(position);
 			type = i.getInqType();
 			vTitle.setText(i.getInqTitle());
 			vNote.setText(i.getInqContent());
 			vComment.setText(i.getInqComments());
 			currentInq = i;
 			//vTitle.setBackgroundColor(Color.argb(255,200,0,200));
 			//parent.getChildAt(position).setBackgroundColor(Color.argb(255,200,0,200));
 //			selectedInqPos = position;	
 		}
 	};
 	
 	class InquiryAdapter extends ArrayAdapter<Inquiry> {
 		InquiryAdapter() {
 			super(InquiryTab.this, R.layout.row, Helioroom.inqList);
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View row = convertView;
 			InquiryHolder holder = null;
 
 			if (row == null) {
 				LayoutInflater inflater = getLayoutInflater();
 				row = inflater.inflate(R.layout.row, parent, false);
 				holder = new InquiryHolder(row);
 				row.setTag(holder);
 			} else {
 				holder = (InquiryHolder) row.getTag();
 			}
 
 			holder.populateFrom(Helioroom.inqList.get(position));
 
 			return (row);
 			
 		}
 	}
 
 	class DiscussionAdapter extends ArrayAdapter<Inquiry> {
 		DiscussionAdapter() {
 			super(InquiryTab.this, R.layout.row, Helioroom.discList);
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View row = convertView;
 			InquiryHolder holder = null;
 
 			if (row == null) {
 				LayoutInflater inflater = getLayoutInflater();
 				row = inflater.inflate(R.layout.row, parent, false);
 				holder = new InquiryHolder(row);
 				row.setTag(holder);
 			} else {
 				holder = (InquiryHolder) row.getTag();
 			}
 
 			holder.populateFrom(Helioroom.discList.get(position));
 
 			return (row);
 		}
 	}
 
 	
 	static class InquiryHolder {
 		private TextView title = null;
 //		private TextView content = null;
 		private View row = null;
 
 		InquiryHolder(View row) {
 			this.row = row;
 			title = (TextView) row.findViewById(R.id.titleRow);
 //			content = (TextView) row.findViewById(R.id.contentRow);
 		}
 
 		void populateFrom(Inquiry i) {
 			title.setText(i.getInqTitle());
 //			content.setText(i.getInqContent());
 		}
 	}
 
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
