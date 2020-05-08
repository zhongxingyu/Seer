 package mobisocial.tictactoe;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import mobisocial.socialkit.SocialKit.Dungbeetle;
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class TicTacToeActivity extends Activity {
     private static final String TAG = "ttt";
 
     Dungbeetle mDungBeetle;
     private Board mBoard;
     private String mToken;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         if (!Dungbeetle.isDungbeetleIntent(getIntent())) {
             Toast.makeText(this, "Please launch with 2-players!", Toast.LENGTH_SHORT).show();
             finish();
             return;
         }
         // All app code is in Board.
         mDungBeetle = Dungbeetle.getInstance(getIntent());
         mBoard = new Board();
         mBoard.parse(mDungBeetle.getThread().getApplicationState());
         mToken = (1 == mDungBeetle.getThread().getMemberNumber()) ? "O" : "X";
 
         // Other demos
         /*
         if (Dungbeetle.isDungbeetleIntent(getIntent())) {
             mDungBeetle = Dungbeetle.getInstance(getIntent());
 
             mDungBeetle.getThread().getMembers(); // List of all known people
             mDungBeetle.getThread().getJunction(); // Message-passing without persistence.
 
             // sendMessage(...);
             // synState(...);
         }*/
     }
 
     class Board implements View.OnClickListener {
         private final List<Button> mmSquares = new ArrayList<Button>();
 
         public Board() {
             // TODO: It is more efficient to bind each individual
             // view object to the SocialKit. Can just give root view?
             mmSquares.add((Button)findViewById(R.id.s0));
             mmSquares.add((Button)findViewById(R.id.s1));
             mmSquares.add((Button)findViewById(R.id.s2));
             mmSquares.add((Button)findViewById(R.id.s3));
             mmSquares.add((Button)findViewById(R.id.s4));
             mmSquares.add((Button)findViewById(R.id.s5));
             mmSquares.add((Button)findViewById(R.id.s6));
             mmSquares.add((Button)findViewById(R.id.s7));
             mmSquares.add((Button)findViewById(R.id.s8));
             for (int i = 0; i < 9; i++) {
                 mmSquares.get(i).setOnClickListener(this);
                 mmSquares.get(i).setTag(R.id.s0, i);
             }
         }
 
         private void parse(JSONObject state) {
            if (!state.has("s")) {
                 return; // empty board initialized.
             }
 
             JSONArray s = state.optJSONArray("s");
             for (int i = 0; i < 9; i++) {
                 mmSquares.get(i).setText(s.optString(i));
             }
         }
 
         private JSONObject getApplicationState() {
             JSONObject o = new JSONObject();
             JSONArray s = new JSONArray();
             try {
                 for (Button b : mmSquares) {
                     s.put(b.getText());
                 }
                 o.put("s", s);
             } catch (JSONException e) {
                 Log.wtf(TAG, "Failed to get board state", e);
             }
             return o;
         }
 
         @Override
         public void onClick(View v) {
             mmSquares.get((Integer)v.getTag(R.id.s0)).setText(mToken);
             mDungBeetle.getThread().setApplicationState(getApplicationState());
         }
     }
 }
