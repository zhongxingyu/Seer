 package dummy.MahjonggCalc.activity;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import com.google.inject.Inject;
 import dummy.MahjonggCalc.R;
 import dummy.MahjonggCalc.db.model.Person;
 import dummy.MahjonggCalc.db.model.PlayerRound;
 import dummy.MahjonggCalc.db.model.Round;
 import dummy.MahjonggCalc.db.service.PersonService;
 import dummy.MahjonggCalc.db.service.RoundService;
 import roboguice.activity.GuiceActivity;
 import roboguice.inject.InjectView;
 
 import java.util.List;
 
 public class GameSessionActivity extends GuiceActivity {
 	private static final String TAG = "GameSessionActivity";
     public static final String EXTRA_PARTICIPANT_1 = "participant_1";
     public static final String EXTRA_PARTICIPANT_2 = "participant_2";
     public static final String EXTRA_PARTICIPANT_3 = "participant_3";
     public static final String EXTRA_PARTICIPANT_4 = "participant_4";
 
     @InjectView(R.id.ListView01)
     ListView list;
 
     @InjectView(R.id.GraphView01)
     GraphView graph;
 
     @Inject
     private PersonService personService;
 
     @Inject
     private RoundService roundService;
 
     private Person[] players;
     private long[] personIds;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.game_session_activity);
         list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			 public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                  onListItemClick(a, v, position, id);
 			 }});
     }
 
     @Override
     protected void onResume() {
         Log.d(TAG, "onResume()");
         super.onResume();
 
         Intent intent = getIntent();
         personIds = new long[] {
                 intent.getLongExtra(EXTRA_PARTICIPANT_1, Person.ID_NOBODY),
                 intent.getLongExtra(EXTRA_PARTICIPANT_2, Person.ID_NOBODY),
                 intent.getLongExtra(EXTRA_PARTICIPANT_3, Person.ID_NOBODY),
                 intent.getLongExtra(EXTRA_PARTICIPANT_4, Person.ID_NOBODY),
         };
         List<Round> rounds = roundService.findAllByPlayers(personIds, getResources());
         Round[] roundsArray = rounds.toArray(new Round[]{});
         RoundListAdapter adapter = new RoundListAdapter(this,
                 R.layout.game_session_listitem, R.id.TextView01,
                 roundsArray);
         list.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
         list.setStackFromBottom(true);
         list.setAdapter(adapter);
 
         int sums[] = new int[4];
         graph.setCountDataLines(sums.length);
         graph.clearValues();
         for (Round item : rounds) {
             sums[0] += item.findPlayerRoundByPlayerId(personIds[0]).getAmount();
             sums[1] += item.findPlayerRoundByPlayerId(personIds[1]).getAmount();
             sums[2] += item.findPlayerRoundByPlayerId(personIds[2]).getAmount();
             sums[3] += item.findPlayerRoundByPlayerId(personIds[3]).getAmount();
             graph.addValues(new Integer[]{sums[0], sums[1], sums[2], sums[3]});
         }
         View summaryView = findViewById(R.id.summaryListItem);
         ActivityTools.setLabel((TextView)summaryView.findViewById(
                 R.id.TextView01), sums[0]);
         ActivityTools.setLabel((TextView)summaryView.findViewById(
                 R.id.TextView02), sums[1]);
         ActivityTools.setLabel((TextView)summaryView.findViewById(
                 R.id.TextView03), sums[2]);
         ActivityTools.setLabel((TextView)summaryView.findViewById(
                 R.id.TextView04), sums[3]);
 
         setAvatar(R.id.player1, EXTRA_PARTICIPANT_1);
         setAvatar(R.id.player2, EXTRA_PARTICIPANT_2);
         setAvatar(R.id.player3, EXTRA_PARTICIPANT_3);
         setAvatar(R.id.player4, EXTRA_PARTICIPANT_4);
 
         players = new Person[] {
             personService.findById(intent.getLongExtra(EXTRA_PARTICIPANT_1, Person.ID_NOBODY)),
             personService.findById(intent.getLongExtra(EXTRA_PARTICIPANT_2, Person.ID_NOBODY)),
             personService.findById(intent.getLongExtra(EXTRA_PARTICIPANT_3, Person.ID_NOBODY)),
             personService.findById(intent.getLongExtra(EXTRA_PARTICIPANT_4, Person.ID_NOBODY)),
         };
     }
 
     private void setAvatar(int layout, String extra) {
         Long personId = getIntent().getLongExtra(extra, 0);
         if (personId <= 0) return;
 
         Person person = personService.findById(personId);
         ImageView imageView = (ImageView)findViewById(layout).findViewById(R.id.ImageView01);
         imageView.setImageBitmap(person.getImage());
 
         TextView textView = (TextView)findViewById(layout).findViewById(R.id.txtName1);
         textView.setText(person.getName());
     }
 
     public class RoundListAdapter extends ArrayAdapter<Round> {
         private Context mContext;
         private Round[] mItems;
 
         public RoundListAdapter(Context context, int resource,
                                 int textViewResourceId, Round[] objects) {
 			
             super(context, resource, textViewResourceId, objects);
             mContext = context;
             mItems = objects;
         }
 
         public View getView(int position, View ConvertView, ViewGroup parent) {
             Log.d(TAG, "getView(" + position + ")");
             LayoutInflater inflater = (LayoutInflater)
                 mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
             Round round = mItems[position];
             View row = inflater.inflate(R.layout.game_session_listitem, null);
 
             setColumn(round, row, personIds[0], R.id.TextView01);
             setColumn(round, row, personIds[1], R.id.TextView02);
             setColumn(round, row, personIds[2], R.id.TextView03);
             setColumn(round, row, personIds[3], R.id.TextView04);
             return row;
         }
 
         private void setColumn(Round round, View row, long playerId, int textViewId) {
             PlayerRound playerRound = round.findPlayerRoundByPlayerId(playerId);
             ActivityTools.setLabel((TextView) row.findViewById(textViewId),
                     String.format("%s(%s) %s",
                             playerRound.getPersonId().equals(round.getWinner()) ? "*" : "",
                             playerRound.getWind("-").charAt(0),
                             playerRound.getAmount("-")
                     ),
                     playerRound.getAmount());
         }
 
         private void setLabel(int layoutId, int index, View row) {
 
         }
     }
     
     public void onAddScoresClick(View view) {
     	Log.d(TAG, "onAddScoresClick");
         Intent intent = new Intent(this, AddScoreActivity.class);
 
         long playerIds[] = new long[players.length];
         for (int index = 0; index < players.length; index++) {
             if (players[index] == null) {
                 playerIds[index] = 0;
             } else {
                 playerIds[index] = players[index].getId();
             }
 
         }
         intent.putExtra(AddScoreActivity.EXTRA_PLAYER_IDS, playerIds);
         startActivity(intent);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
     }
 
 
     public void onListItemClick(AdapterView<?> a, View v, int position, long id) {
         Intent intent = new Intent(this, AddScoreActivity.class);
         Round listItem = (Round)list.getItemAtPosition(position);
         Log.d(TAG, "listItem.getId(): " + listItem.getId());
         intent.putExtra(AddScoreActivity.EXTRA_ROUND_ID, listItem.getId());
         startActivity(intent);
     }
    
    public void onSelectPersonClick(View view) {
        Log.d(TAG, "onSelectPersonClick");
    }
 }
