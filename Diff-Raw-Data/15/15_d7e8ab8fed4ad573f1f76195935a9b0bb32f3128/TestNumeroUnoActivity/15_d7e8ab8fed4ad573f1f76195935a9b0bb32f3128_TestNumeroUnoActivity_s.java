 package rinxter.First;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class TestNumeroUnoActivity extends Activity {
 	int season;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(rinxter.First.R.layout.main);
         
         /* Obtain Current Year */
         Calendar c = Calendar.getInstance();
         season = c.get(Calendar.YEAR);
         TextView seasonTxt = (TextView) findViewById(R.id.seasonTV);
         seasonTxt.setText(String.valueOf(season) + " Season");
         
         Button bout_btn = (Button) findViewById(rinxter.First.R.id.bout_btn);        
         bout_btn.setOnClickListener(new View.OnClickListener() {			
 			@Override
 			public void onClick(View v) {
 				URI bouts = null;
 				try {
 					bouts = new URI("http://rinxter.net/wftda/ds?type=boutList&leagueId=1&season=" + String.valueOf(season));
 				} catch (URISyntaxException e) {
 					e.printStackTrace();
 				}
 				Intent i =  new Intent(getApplicationContext(), SecondScreenActivity.class);
 				i.putExtra("URI", bouts);
 				i.putExtra("page", "bouts");
 				startActivity(i);
 			}
         });
         
         //retrieves data from each team
         Button team_btn = (Button) findViewById(R.id.team_btn);
         team_btn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				URI teams = null;
 				try {
 					teams = new URI("http://rinxter.net/wftda/ds?type=teamList&leagueId=1&season=" +  String.valueOf(season));
 				} catch (URISyntaxException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				Intent i = new Intent(getApplicationContext(), SecondScreenActivity.class);
 				i.putExtra("URI", teams);
 				i.putExtra("page", "teams");
 				startActivity(i);
 			}
 		});
     }
 	
     public boolean onCreateOptionsMenu(Menu menu)
     {
     	menu.add(0, 0, 0, "Change Year");
     	menu.add(0, 1, 1, "Quit");
 		return true;
     }
 
     public boolean onOptionsItemSelected(MenuItem item)
     {
 		switch(item.getItemId())
 		{
 			case 0:
 				chooseSeason();
 				break;
 			case 1: 
 				finish();
 				break;
 		}
     	return true;
     }

 	private void chooseSeason() {
 		AlertDialog.Builder alb = new AlertDialog.Builder(this);
 		alb.setTitle("Select Season");
		int size = season - 1990;
 		final String choices[] = new String[size+1];
		for(int i = 1990; i <= season; i++)
 		{
			choices[i - 1990] = String.valueOf(i);
 		}
 		alb.setItems(choices, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				TextView tv = (TextView) findViewById(R.id.seasonTV);
 				tv.setText(choices[which] + " Season");
 				season = Integer.parseInt(choices[which]);
 			}
 		});
 		alb.show();
 	}
 }
