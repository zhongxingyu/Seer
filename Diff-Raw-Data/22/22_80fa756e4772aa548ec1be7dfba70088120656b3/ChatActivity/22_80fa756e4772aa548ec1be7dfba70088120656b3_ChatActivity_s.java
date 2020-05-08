 package com.example.haw_app;
 
 import java.util.ArrayList;
 
 import com.example.haw_app.socialfeatures.implementation.Chat;
 import com.example.haw_app.socialfeatures.interfaces.IChat;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Spinner;
 
 /**
  * Chat Fenster
  * 
  * @author Aria
  * 
  */
 public class ChatActivity extends Activity implements OnItemSelectedListener {
 
 	private Spinner recipient;
 	private EditText textMessage;
 	private ListView listview;
 	private String selectTo = "";
 	private IChat ci;
 	//private Chat ci;
 	private ArrayList<String> messages = new ArrayList<String>();
 	private ArrayList<String> contactList = new ArrayList<String>();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_sf_chat);
		contactList.add(" "); // Damit man auch keinen Nutzer whlen kann.
 		ci = Chat.getInstance();
 		ci.setChatActivity(this);
 
 		recipient = (Spinner) this.findViewById(R.id.toET);
 		recipient.setOnItemSelectedListener(this);
 		textMessage = (EditText) this.findViewById(R.id.chatET);
 		listview = (ListView) this.findViewById(R.id.listMessages);
 
 		ci.startContact(); // Kontaktdaten holen + aktualisierung
 		ci.showOfflineMessages(); // Offlinemessage holen
 		setContactAdapter(); // Kontaktfenster initialisieren
 
		ci.addUserAutomatic(); // User automatisch adden die in Praktika eingetragen worden sind
 		Button send = (Button) this.findViewById(R.id.sendBtn);
 		send.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
				if (!selectTo.equals(" ")) { // PRFEN OB ES GEHT
 					// String to = recipient.getText().toString();
 					String to = selectTo;
 					String text = textMessage.getText().toString();
 					ci.sendMessage(to, text);
 					setMessage("to " + selectTo + " :");
 					setMessage(text);
 					setListAdapter();
 					textMessage.setText("");//Textfeld wieder leeren
 				}
 			}
 		});
 	}
 
 	/**
 	 * Nachrichtenliste aktualisiern/setzen
 	 */
 	public void setListAdapter() {
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				R.layout.sf_listitem, messages);
 		listview.setAdapter(adapter);
 	}
 
 	/**
 	 * Text die auf der Nachrichtenliste angezeigt werden sollen
 	 * @param text Nachrichtentext
 	 */
 	public void setMessage(String text) {
 		messages.add(text);
 	}
 
 	/**
 	 * Kontaktliste aktualisiern/setzen
 	 */
 	public void setContactAdapter() {
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item, contactList);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		recipient.setAdapter(adapter);
 
 		// adapter.notifyDataSetChanged();
 	}
 
 	/**
 	 * Beim start erstmal alle Kontakte adden die der Nutzer
 	 * in seine Freundesliste hat
 	 * @param text Kontaktname
 	 */
 	public void addContact(String text) {
 		contactList.add(text);
 	}
 
 	/**
 	 * Nutzer geht von Offline zu Online.
 	 * Im text steht Nutzer + Status (z.b. ABC-(Offline))
 	 * @param text Nutzer + Status(Offline)
 	 */
 	public void changeContactOnline(String text) {
 		String textChange = (text + "-(Online)");
 		text = (text + "-(Offline)");
 		contactList.set(contactList.indexOf(text), textChange);
 	}
 
 	/**
 	 * Nutzer geht von Online zu Offline.
 	 * Im text steht Nutzer + Status (z.b. ABC-(Online))
 	 * @param text Nutzer + Status(Online)
 	 */
 	public void changeContactOffline(String text) {
 		String textChange = (text + "-(Offline)");
 		text = (text + "-(Online)");
 		contactList.set(contactList.indexOf(text), textChange);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		ci.chatDisconnect();
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int pos,
 			long id) {
 		//Name muss zurechtgeschnitten werden, da beim senden nicht
		//zustzlich Offline/Online stehen darf.
 		String temp = parent.getItemAtPosition(pos).toString();
 		String[] tempcon = temp.split("-");
 		selectTo = tempcon[0];
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 	}
 
 }
