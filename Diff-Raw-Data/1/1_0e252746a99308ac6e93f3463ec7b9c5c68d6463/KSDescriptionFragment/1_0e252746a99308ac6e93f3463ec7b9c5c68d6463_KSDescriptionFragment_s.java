 package com.bigtheta.ragedice;
 
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 public class KSDescriptionFragment extends Fragment {
 	KSDescriptionListener mCallback;
 	
 	public interface KSDescriptionListener {
 		public void onKSDescriptionSelected(int position);
 		public View findViewById(int id);
 	}
 	
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, 
                          Bundle savedInstanceState) {
         return inflater.inflate(R.layout.ksdescription_layout, container, false);
     }
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     }
     
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         try {
         	mCallback = (KSDescriptionListener) activity;
         } catch (ClassCastException e) {
         	throw new ClassCastException(activity.toString()
         			+ " must implement KSDescriptionListener");
         }
     }
     
     public void displayInfo(long gameId) {
     	TextView tv = (TextView) mCallback.findViewById(R.id.ksdescription_view);
         String info = "";
         info += "numDiceRolls: " + Integer.toString(DiceRoll.getNumDiceRolls());
         HashMap<Integer, Double> pmf = DieDescription.getPMF(gameId);
         for (Integer observation : pmf.keySet()) {
             info += "\nObservation: " + Integer.toString(observation)
                   + " Probability: " + pmf.get(observation);
         }
         HashMap<Integer, Integer> dist = DiceRoll.getObservedRolls(gameId);
         for (Integer observation : dist.keySet()) {
             info += "\nObservation: " + Integer.toString(observation)
                   + " Count: " + dist.get(observation);
         }
         double stat = DiceRoll.calculateKSTestStatistic(gameId);
         info += "\nKS statistic: " + Double.toString(stat);
         // Probability that these are different distributions.
         info += "\nKS probability: " + Double.toString(DiceRoll.calculateKSPValue(gameId));
         info += "\nCentral Limit Theorem probability: " + Double.toString(DiceRoll.calculateCentralLimitProbabilityPValue(gameId));
 
         info += "\n=====\n";
         info += DieDescription.getKSDescription(gameId);
         info += "\n=====\n";
         info += DieDescription.getCLTDescription(gameId);
         tv.setText(info);
     	
     }
 }
