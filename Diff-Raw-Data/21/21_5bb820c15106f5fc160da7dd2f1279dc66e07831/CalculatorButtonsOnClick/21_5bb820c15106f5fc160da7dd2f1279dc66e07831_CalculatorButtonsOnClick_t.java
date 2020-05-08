 package com.Centaurii.app.RatingCalculator.listeners;
 
 import com.Centaurii.app.RatingCalculator.R;
 import com.Centaurii.app.RatingCalculator.model.Profile;
 import com.Centaurii.app.RatingCalculator.util.Calculator;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.graphics.Color;
 import android.support.v4.app.FragmentActivity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class CalculatorButtonsOnClick implements OnClickListener
 {
     FragmentActivity act;
     ListView players;
     ArrayAdapter<Profile> playersAdapter;
     
     public CalculatorButtonsOnClick(FragmentActivity act, ListView players, ArrayAdapter<Profile> playersAdapter)
     {
         this.act = act;
         this.players = players;
         this.playersAdapter = playersAdapter;
     }
 
     @Override
     public void onClick(View view)
     {
         int winnerPosition = players.getCheckedItemPosition();
         int totalPlayers = players.getCount() - 1;
         if(winnerPosition == AbsListView.INVALID_POSITION)
         {
             Toast.makeText(act, "Please select a winner", Toast.LENGTH_SHORT).show();
         }
         else if(totalPlayers < 2)
         {
             Toast.makeText(act, "You don't have enough players", Toast.LENGTH_SHORT).show();
         }
         else
         {
             Profile[] calculateArr = new Profile[totalPlayers];
             
             for(int i = 0; i < totalPlayers; i++)
             {
                 calculateArr[i] = (Profile) players.getItemAtPosition(i);
             }
             
             int[] scores = Calculator.calculateRating(winnerPosition, false, calculateArr);
             
             View dialogView = buildView(totalPlayers, calculateArr, scores, winnerPosition);
             
             AlertDialog.Builder builder = new AlertDialog.Builder(act);
             
             builder.setView(dialogView)
                    .setPositiveButton("Save", new SaveListener(winnerPosition, calculateArr))
                    .setNegativeButton("Don't Save", null)
                    .create()
                    .show();
         }
     }
     
     private View buildView(int totalPlayers, Profile[] calculateArr, int[] scores, int winner)
     {
         LayoutInflater inflater = act.getLayoutInflater();
         ViewGroup masterView = (ViewGroup) inflater.inflate(R.layout.score_reveal_dialog, null, false);
         
         for(int i = 0; i < totalPlayers; i++)
         {
             View slaveView = inflater.inflate(R.layout.score_reveal_dialog_slave_view, null, false);
             
             TextView name = (TextView) slaveView.findViewById(R.id.player_name);
             TextView newScore = (TextView) slaveView.findViewById(R.id.player_new_score);
             
             name.setText(calculateArr[i].getName() + ":");
            if(scores[i] > 99999999)
            {
                Toast.makeText(act, calculateArr[i].getName() + "'s rating can go no higher", Toast.LENGTH_SHORT).show();
                scores[i] = 99999999;
            }
             newScore.setText("" + scores[i]);
             if(i == winner)
             {
                 newScore.setTextColor(Color.GREEN);
             }
             
             masterView.addView(slaveView);
         }
         
         return masterView;
     }
     
     private class SaveListener implements DialogInterface.OnClickListener
     {
         int winnerPosition;
         Profile[] scored;
         
         public SaveListener(int winnerPosition, Profile[] scored)
         {
             this.winnerPosition = winnerPosition;
             this.scored = scored;
         }
         
         @Override
         public void onClick(DialogInterface dialog, int which)
         {
             Calculator.calculateRating(winnerPosition, true, scored);
             playersAdapter.notifyDataSetChanged();
         }
         
     }
 
 }
