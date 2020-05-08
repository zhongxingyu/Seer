 package dk.aau.cs.giraf.cars;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.view.Menu;
 import android.view.View;
 
 public class HelpActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_help);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.help, menu);
 		return true;
 	}
 
 	public void showMicHelp(View v) {
 
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Mikrofon");
		alertDialogBuilder.setMessage("For at indstille mikrofonen skal du ude p startskrmen trykke p indstillinger og derefter p knappen Start til hjre for teksten Mikrofon Indstillinger. Nr knappen trykkes bliver din stemme testet ved at optage dig snakke med en dyb stemme og derefter en hj stemme. Til sidst kan du se hvordan din stemme nu pvirker figuren der flytter sig op og ned nr du snakker. Hvis resultatet ikke er tilfredsstillende kan testen tages igen, fr du begynder at spille.");
 
		alertDialogBuilder.setPositiveButton("Okay",new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog,int id) {
 				// Dismiss dialog
 			}
 		});
 
 		AlertDialog alertDialog = alertDialogBuilder.create();
 		alertDialog.show();
 	}
 
 	public void showSettingsHelp(View v) {
 
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Indstillinger");
		alertDialogBuilder.setMessage("For at ndre indstillinger i spillet skal du ude p startskrmen trykke p Indstillinger hvorefter et nyt vindue vil vise den nuvrendes profils indstillinger. I hjre side kan du ndre dine nskede indstillinger og derefter tryk p knappen Gem. I venstre side er der en liste over af de profile den nuvrende bruger kan tilg. Hvis der trykkes p et navn vises den profils indstillinger.");
 
		alertDialogBuilder.setPositiveButton("Okay",new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog,int id) {
 				// Dismiss dialog
 			}
 		});
 
 		AlertDialog alertDialog = alertDialogBuilder.create();
 		alertDialog.show();
 	}
 
 
 	public void showGameHelp(View v) {
 
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Spil Hjlp");
		alertDialogBuilder.setMessage("Spillet gr ud p at styre bilen ind i garagerne der er placeret i hjre side af skrmen. Der er forhindringer p krebanen som skal undges. Hvis et objekt bliver pkrt er bilen delagt og du skal starte forfra. Du kan styre bilen ned ved at lave en dyb lyd og ligeledes op ved at lave en hj lyd. Hvis alle biler kres ind i deres garage har du vundet spillet");
 
		alertDialogBuilder.setPositiveButton("Okay",new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog,int id) {
 				// Dismiss dialog
 			}
 		});
 
 		AlertDialog alertDialog = alertDialogBuilder.create();
 		alertDialog.show();
 	}
 }
