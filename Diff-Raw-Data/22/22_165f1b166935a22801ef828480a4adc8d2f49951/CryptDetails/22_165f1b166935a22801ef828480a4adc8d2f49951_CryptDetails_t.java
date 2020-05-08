 package name.vampidroid;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.widget.TextView;
 
 public class CryptDetails extends Activity {
 	
	static String QUERY_CRYPT = "select Name, Type, Clan, Disciplines, CardText, Capacity, Artist, _Set from crypt where _id = ";
 	
 	/**
 	 * @see android.app.Activity#onCreate(Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// TODO Put your code here
 		
 		Intent i = getIntent();
 		
 		String deck = i.getStringExtra(VampiDroid.DECK_NAME);
 		long card_id = i.getLongExtra(VampiDroid.CARD_ID, 0);
 		
 		String query;
 		
 		query = QUERY_CRYPT;
 		
 		
 		setContentView(R.layout.cryptcarddetails);
 		
 		SQLiteDatabase db = VampiDroid.getDatabase(this);
 		Cursor c = db.rawQuery(query + String.valueOf(card_id), null );
 		c.moveToFirst();
 		
 		String cardName = c.getString(0);
 		String cardType = c.getString(1);
 		String cardClan = c.getString(2);
 		String cardDisciplines = c.getString(3);
 		String cardText = c.getString(4);
 		String cardCapacity = c.getString(5);
 		String cardArtist = c.getString(6);
		String cardSetRarity = c.getString(7);
 		
 		c.close();
 		db.close();
 		
 		
 		
 		TextView txt = (TextView) findViewById(R.id.txtCardName);
 		txt.setText(cardName);
 		
 		txt = (TextView) findViewById(R.id.txtCardCapacity);
 		txt.setText(cardCapacity);
 
 		txt = (TextView) findViewById(R.id.txtCardType);
 		txt.setText(cardType);
 		
 
 		txt = (TextView) findViewById(R.id.txtCardClan);
 		txt.setText(cardClan);
 		
 
 		txt = (TextView) findViewById(R.id.txtCardDisciplines);
 		txt.setText(cardDisciplines);
 		
 
 		txt = (TextView) findViewById(R.id.txtCardArtist);
 		txt.setText(cardArtist);
 
 		txt = (TextView) findViewById(R.id.txtCardText);
 		txt.setText(cardText);
 		
		txt = (TextView) findViewById(R.id.txtSetRarity);
		txt.setText(cardSetRarity);
		
 	}
 }
