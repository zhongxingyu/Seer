 package com.bigtheta.ragedice;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 
 import com.bigtheta.ragedice.R.drawable;
 
 
 public class DiceDisplayFragment extends Fragment  {
     DiceDisplayListener m_callback;
 
     public interface DiceDisplayListener {
         public void onDiceSelected(int position);
         public View findViewById(int id);
        public void initializeGame(boolean display);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                          Bundle savedInstanceState) {
         return inflater.inflate(R.layout.dice_layout, container, false);
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         try {
             m_callback = (DiceDisplayListener) activity;
         } catch (ClassCastException e) {
             throw new ClassCastException(activity.toString()
                     + " must implement DiceDisplayListener");
         }
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         //refreshStatusText();
         super.onActivityCreated(savedInstanceState);
     }
 
     @Override
     public void onStart() {
         super.onStart();
     }
 
     @Override
     public void onResume() {
         displayDiceRoll(DiceRoll.getLastDiceRoll(MainActivity.getGame().getId()));
         super.onResume();
     }
 
     @Override
     public void onPause() {
         super.onPause();
     }
 
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
         super.onSaveInstanceState(savedInstanceState);
     }
 
     @Override
     public void onStop() {
         super.onStop();
     }
 
     @Override
     public void onDestroyView() {
         super.onDestroyView();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
     }
 
     @Override
     public void onDetach() {
         super.onDetach();
     }
     public void rollTest() {
         m_callback.onDiceSelected(0);
     }
 
     public void displayDiceRoll(DiceRoll dr) {
         //TextView tv = (TextView) m_callback.findViewById(R.id.player_number);
         //Player currentPlayer = Player.retrieve(dr.getPlayerId());
         //tv.setText(currentPlayer.getPlayerName());
         if (dr == null) {
             Player initPlayer = Player.getNextPlayer(MainActivity.getGame().getId());
             dr = new DiceRoll(initPlayer);
             displayDiceRoll(dr);
             dr.delete();
         }else {
             Class<drawable> res = R.drawable.class;
             for (DieResult result : DieResult.getDieResults(dr)) {
                 DieDescription dd = DieDescription.retrieve(result.getDieDescriptionId());
                 try {
                    ImageView iv = (ImageView)m_callback.findViewById(dd.getImageViewResource());
                     iv.setImageResource(result.getImageResource());
                     //getResources().getColor(result.getImageColorResource());
                     iv.setBackgroundColor(getResources().getColor(result.getImageColorResource()));
                } catch (ClassCastException err) {
                    m_callback.initializeGame(false);
                 } catch (Exception err) {
                 }
                
             }
         }
         //refreshStatusText();
     }
 
     private void refreshStatusText() {
         return;
         /*
         TextView tv = (TextView) m_callback.findViewById(R.id.whose_turn_text);
         Game g = MainActivity.getGame();
         String currentPlayerName = Player.getLastPlayer(g.getId()).getPlayerName();
         String nextPlayerName = Player.getNextPlayer(g.getId()).getPlayerName();
         if (tv != null) {
             if (Player.getNumPlayers() > 1) {
                 tv.setText(currentPlayerName + "'s turn.\n" +
                            nextPlayerName + " is next.");
             } else {
                 tv.setText("");
             }
         }
         */
     }
 }
 
