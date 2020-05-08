 package com.example.testappsms;
 
 import java.util.ArrayList;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.telephony.SmsManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	
 	private final int SELECT_CONTACT=1;
 	
 	private ArrayList <Contatto>arrayListContatti;
     private ListView listaContatti ;
     
     private CustomAdapter adapter;
 	
 	private OnClickListener inviaSMSClickListener = new OnClickListener() {
 		
 		//alla pressione del tasto "invia" verifico di aver selezionato
 		//dei contatti prima di poter inviare un messaggio
 		@Override
 		public void onClick(View v) {
 			
 			//invio un messagio a tutti i destinatari
 			if(arrayListContatti.size()  > 0){
 				
 				EditText msgField = (EditText)findViewById(R.id.textBoxSMS);
 				String msgToSend = msgField.getText().toString();
 				
 				if(!msgToSend.isEmpty()){
 
 					SmsManager sms = SmsManager.getDefault();
 
 					for(int i=0; i < arrayListContatti.size() ; i++){
 						//sms.sendTextMessage(arrayListContatti.get(i).getTelefono(), null, msgToSend, null, null);
 					}
 
 					showToast("Messagio inviato!");
 					
 				}else{
 					showToast("Messagio vuoto!");	
 				}
 					
 			}else{
 				showToast("Nessun contatto selezionato!");
 			}
 			
 		}
 	};
 
 	//alla pressione del tasto "seleziona contatti" lancia un nuovo
 	//intent per la selzione di un contatto
 	private OnClickListener selectContattiListener = new OnClickListener(){
 
 		@Override
 		public void onClick(View v) {
 			Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
 			startActivityForResult(intent, SELECT_CONTACT);
 
 		}
 	};
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     	
         
         setContentView(R.layout.activity_main);
         
         //mi collego agli elementi della grafica che mi servono
         Button inviaMsgButton = (Button)findViewById(R.id.inviaSMSButton);
         inviaMsgButton.setOnClickListener(inviaSMSClickListener);
         
         ImageButton slectContact = (ImageButton)findViewById(R.id.selectContattiButton);  
         slectContact.setOnClickListener	(selectContattiListener);
 
         arrayListContatti = new ArrayList <Contatto>();
         
         listaContatti=(ListView)findViewById(R.id.listaContattiSelezionati);
         listaContatti.setCacheColorHint(getResources().getColor(R.color.grayBlack));
 
         adapter = new CustomAdapter(this, R.layout.row, arrayListContatti);


         listaContatti.setAdapter(adapter);
         
        //se l'applicazione è stata ripristinata, allora recupero l'ArrayLi
         if (savedInstanceState != null){
         	ArrayList<String> arrayNomi = savedInstanceState.getStringArrayList( "arrayNomi" );
         	ArrayList<String> arrayNumeri = savedInstanceState.getStringArrayList( "arrayNumeri" );
         	
         	for(int i=0 ; i < arrayNomi.size() ; i++ ){	
         		arrayListContatti.add(new Contatto(arrayNomi.get(i), "", arrayNumeri.get(i)));
         	}
         	
         	adapter.notifyDataSetChanged();
         }
     }
     
     protected void onStart(){
         super.onStart();
     }
     
     private void showToast(String testo){
 		Toast toast=Toast.makeText(getApplicationContext(),testo,Toast.LENGTH_SHORT);
 		toast.show();
 	}
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     
     //metodo usato per ricevere il risultato dell'intent per selezionare i contatti
     @Override
     public void onActivityResult(int reqCode, int resultCode, Intent data) {
     	super.onActivityResult(reqCode, resultCode, data);
 
     	switch (reqCode) {
     	case (SELECT_CONTACT) :
     		
     		//se l'intent ha ritornato un risultato valido
     		if (resultCode == Activity.RESULT_OK) {
     			Uri contactData = data.getData();
     			Cursor cur =  managedQuery(contactData, null, null, null, null);
     			
     			if (cur.moveToFirst()) {
     				//recupero il nome e l'ID del contatto
     				String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
     				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
 
     				if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) 
     				{
     					// Query phone here. Covered next
     					Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,null, null); 
     					
     					//aggiungo tutti i numeri di telefono del contatto all'elenco dei destinatari
     					while (phones.moveToNext()) { 
     						String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
     						
     						Log.d("Number", phoneNumber);
     						
     						addContattoToList(  name , phoneNumber);
     					} 
     					phones.close();
 
     					Log.d("Names", name + " " );
     				}
     			}
     		}
     		break;
     	}
     }
 
 
     //se non ègià presente aggiungo un contatto alla all'ArrayList di contatti
 	private void addContattoToList(String name, String phoneNumber) {
         
         boolean trovato=false;
         
         //verifico che non ci siano doppioni nella lista
         for( int i=0 ; i < arrayListContatti.size() && !trovato; i++ ){
         	
         	Contatto c1 = arrayListContatti.get(i);
         	if(c1.getNome().equals(name) && c1.getTelefono().equals(phoneNumber) )
         		trovato=true;
         }
         
         if(!trovato){
         
         	arrayListContatti.add(new Contatto(name, "", phoneNumber));
         	adapter.notifyDataSetChanged();
 		}else{
 			showToast("Contatto già presente nei contatti slezionati!");
 		}
 	}
 	
 	//metodo invocato quando per salvare lo stato dell'appllicazione prima che avvenga una rotazione
 	//salvo l'ArrayList dei contatti nel Bundle outState, tramite 2 arraylist
 	@Override
 	public void onSaveInstanceState (Bundle outState){
 		ArrayList<String> arrayNomi = new ArrayList<String>(); 
 		ArrayList<String> arrayNumeri = new ArrayList<String>();
 		
 		for( Contatto c1 : arrayListContatti  ){
 			arrayNomi.add(c1.getNome());
 			arrayNumeri.add(c1.getTelefono());	
 		}
 
 		outState.putStringArrayList("arrayNomi", arrayNomi);
 		outState.putStringArrayList("arrayNumeri", arrayNumeri);
 	}
 }
