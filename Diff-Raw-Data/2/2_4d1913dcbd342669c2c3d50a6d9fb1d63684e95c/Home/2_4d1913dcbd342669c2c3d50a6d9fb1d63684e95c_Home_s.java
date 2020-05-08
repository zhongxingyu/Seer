 package com.abhi.rpsls;
 
 import com.abhi.rpsls.model.Referee;
 import com.abhi.rpsls.model.Result;
 import com.abhi.rpsls.model.Robot;
 import com.abhi.rpsls.model.Result.Outcome;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class Home extends Activity 
 {
 	private Referee ref = new Referee();
 	private CharSequence selectionA =null;
 	private CharSequence selectionB =null;
 	private Robot robot = null;
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         final ListView view = (ListView) findViewById(R.id.mainList);
         robot = new Robot();
 		
 		view.setOnItemClickListener(new OnItemClickListener() 
 		{
 
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
 			{
 					TextView tv = (TextView) arg1;
 					CharSequence newSelection = tv.getText();
 					TextView selectionView = (TextView) findViewById(R.id.selection);
 					String display;
 					
 					selectionView.setText("");
 					
 					selectionA = newSelection;
 					display = selectionA + " vs. ";
 					
 					selectionView.setText(display);
 					
 					selectionB = robot.Choice();
 					
 					selectionView.append(selectionB.toString());
 			
 					final Result res = ref.determineWinner(selectionA.toString(), selectionB.toString());
 						
 					if(Outcome.TIE == res.getOutcome())
 					{
						selectionView.append( selectionA + " ties with " + selectionB);
 					}
 					else if(Outcome.WIN == res.getOutcome())
 					{
 						selectionView.append(". Winner is " + res.getWinner() + ".");
 					}
 					else
 					{
 						selectionView.append("Invalid input.");
 					}
 			}
 			
 		});
     }
 }
