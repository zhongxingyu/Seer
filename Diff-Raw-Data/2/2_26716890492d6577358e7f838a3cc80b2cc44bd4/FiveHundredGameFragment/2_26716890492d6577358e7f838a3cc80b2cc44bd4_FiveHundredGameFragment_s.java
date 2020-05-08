 package com.austin.cardcounter;
 
 import com.austin.cardcounter.MainActivity.ImgAdapter;
 
 import android.support.v4.app.Fragment;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class FiveHundredGameFragment extends Fragment implements
 		OnClickListener {
 
 	Integer mTeam1Score = 0;
 	Integer mTeam2Score = 0;
 	int[] mSavedTeam1Scores;
 	int[] mSavedTeam2Scores;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fragment_500_game,
 				container, false);
 		
 		// click listener
         Button addBtn = (Button) rootView.findViewById(R.id.add_scores);
         addBtn.setOnClickListener(this);
         Button undoBtn = (Button) rootView.findViewById(R.id.undo);
         undoBtn.setOnClickListener(this);
         Button clearBtn = (Button) rootView.findViewById(R.id.clear);
         clearBtn.setOnClickListener(this);
         
         EditText editText = (EditText) rootView.findViewById(R.id.team1_tricks);
         editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 			@Override
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 				boolean handled = false;
 		        if (actionId == EditorInfo.IME_ACTION_NEXT) {
 		            int val = Integer.parseInt(v.getText().toString());
 		            EditText tricks2 = (EditText) getActivity().findViewById(R.id.team2_tricks);
 		            tricks2.setText(Integer.toString(10 - val));
 		            //handled = true;
 		        }
 		        return handled;
 			}
         });
         
 		Spinner mySpinner = (Spinner) rootView.findViewById(R.id.trump_spinner);
 		mySpinner.setAdapter(((MainActivity)getActivity()).getSuitImagesAdapter(true));
              
 		return rootView;
 	}
 	
 
 	@Override
 	public void onViewStateRestored(Bundle savedInstanceState) {
 		super.onViewStateRestored(savedInstanceState);
 		
 
 //        if(((MainActivity)getActivity()).mTeam1Scores != null && ((MainActivity)getActivity()).mTeam2Scores != null) {
 //		    for(int score : ((MainActivity)getActivity()).mTeam1Scores) {
 //		    	addTeam1Score(score);
 //		    }
 //		    for(int score : ((MainActivity)getActivity()).mTeam2Scores) {
 //		    	addTeam2Score(score);
 //		    }
 //        }
 	}
 
 	@Override
 	public void onClick(View view) {
 		switch(view.getId()) {
 		case R.id.add_scores:
 			addScores(view);
 			break;
 		case R.id.undo:
 			undo(view);
 			break;
 		case R.id.clear:
 			clear(view);
 		default:
 			break;
 		}
 	}
 	
 	// add to total scores based on values entered
 		public void addScores(View view) {
 			/*
 			 * lots of error checking
 			 * 		tricks have numbers 0 < x < 10 (fail = do nothing)
 			 * 		tricks add to 10 (fail = do nothing)
 			 * 		bid value entered (fail = do nothing)
 			 * 
 			 * add trick scores to each team's scroll, recalc total score
 			 * if game over, announce win, ask about archiving game
 			 * 
 			 */
 			
 			if(mTeam1Score > 500 || mTeam2Score > 500) {
 				errorToast("Game already over. You may undo to add scores.");
 				return;
 			}
 			
 			EditText name1 = (EditText)getActivity().findViewById(R.id.team1name);
 			String name1val;
 			if(name1.getText().length() < 1) {
 				name1val = "Team 1";
 			}
 			else {
 				name1val = name1.getText().toString();
 			}
 			
 			EditText name2 = (EditText)getActivity().findViewById(R.id.team2name);
 			String name2val;
 			if(name2.getText().length() < 1) {
 				name2val = "Team 2";
 			}
 			else {
 				name2val = name2.getText().toString();
 			}
 
 			EditText trick1 = (EditText)getActivity().findViewById(R.id.team1_tricks);
 			if(trick1.getText().length() < 1) {
 				// error
 				errorToast("Invalid team 1 trick amount");
 				return;
 			}
 			int trick1val = Integer.parseInt(trick1.getText().toString());
 
 			EditText trick2 = (EditText)getActivity().findViewById(R.id.team2_tricks);
 			if(trick2.getText().length() < 1) {
 				// error
 				errorToast("Invalid team 2 trick amount");
 				return;
 			}
 			int trick2val = Integer.parseInt(trick2.getText().toString());
 
 			RadioButton measleBtn = (RadioButton)getActivity().findViewById(R.id.measle);
 			Boolean measle = measleBtn.isChecked();
 			RadioButton doubleMeasleBtn = (RadioButton)getActivity().findViewById(R.id.double_measle);
 			Boolean doubleMeasle = doubleMeasleBtn.isChecked();
 
 			EditText bid = (EditText)getActivity().findViewById(R.id.bid);
 			if(bid.getText().length() < 1 && !measle && !doubleMeasle) {
 				// error
 				errorToast("Invalid bid amount");
 				return;
 			}
 			int bidval = 0;
 			if(!measle && !doubleMeasle) {
 				bidval = Integer.parseInt(bid.getText().toString());
 				if(bidval < 6) {
 					// error
 					errorToast("Bid value below 6");
 					return;
 				}
 			}
 			
 			ToggleButton team = (ToggleButton)getActivity().findViewById(R.id.bidding_team);
 			Boolean team1 = team.isChecked();
 			
 			Spinner trumpSpinner = (Spinner)getActivity().findViewById(R.id.trump_spinner);
 			int suit = trumpSpinner.getSelectedItemPosition();
 			
 			
 			if(trick1val + trick2val != 10) {
 				// error
 				errorToast("Tricks taken do not add to 10");
 				return;
 			}
 			
 			if(team1) {
 				// 500 points logic for team 1
 				/*
 				 * if measle
 				 * 		took 0 = 250, 0
 				 * 		otherwise = -250, 10*trick1val
 				 * if double measle
 				 * 		took 0 = 500, 0
 				 * 		otherwise = -500, 10*trick1val
 				 * otherwise
 				 * 		if taken > bid
 				 * 			team 1 pts = (bidval - 6) * 100 + (2 + suit) * 40
 				 * 			if taken == 10 & team 1 pts < 250, team 1 pts = 250
 				 * 			team2 = 10 * tricks
 				 * 		otherwise
 				 * 			team 1 pts = -bid
 				 * 			team 2 = 10 * tricks
 				 * 
 				 */
 				if(measle) {
 					if(trick1val == 0) {
 						addTeam1Score(250);
 						addTeam2Score(0);
 					}
 					else {
 						addTeam1Score(-250);
 						addTeam2Score(10 * trick1val);
 					}
 				}
 				else if(doubleMeasle) {
 					if(trick1val == 0) {
 						addTeam1Score(500);
 						addTeam2Score(0);
 					}
 					else {
 						addTeam1Score(-500);
 						addTeam2Score(10 * trick1val);
 					}					
 				}
 				else {
 					if(trick1val >= bidval) {
 						int points = (bidval - 6) * 100 + (2 + suit) * 20;
 						if(trick1val == 10 && points < 250) {
 							points = 250;
 						}
 						addTeam1Score(points);
 						addTeam2Score(10 * trick2val);
 					}
 					else {
 						int points = (bidval - 6) * 100 + (2 + suit) * 20;
 						addTeam1Score(-1 * points);
 						if(trick2val == 10) {
 							addTeam2Score(250);
 						}
 						else {
							addTeam2Score(10 * trick1val);
 						}
 					}
 				}
 			}
 			else {
 				// points logic for team 2 bidding
 				if(measle) {
 					if(trick2val == 0) {
 						addTeam2Score(250);
 						addTeam1Score(0);
 					}
 					else {
 						addTeam2Score(-250);
 						addTeam1Score(10 * trick2val);
 					}
 				}
 				else if(doubleMeasle) {
 					if(trick2val == 0) {
 						addTeam2Score(500);
 						addTeam1Score(0);
 					}
 					else {
 						addTeam2Score(-500);
 						addTeam1Score(10 * trick2val);
 					}					
 				}
 				else {
 					if(trick2val >= bidval) {
 						int points = (bidval - 6) * 100 + (2 + suit) * 20;
 						if(trick2val == 10 && points < 250) {
 							points = 250;
 						}
 						addTeam2Score(points);
 						addTeam1Score(10 * trick1val);
 					}
 					else {
 						int points = (bidval - 6) * 100 + (2 + suit) * 20;
 						addTeam2Score(-1 * points);
 						if(trick1val == 10) {
 							addTeam1Score(250);
 						}
 						else {
 							addTeam1Score(10 * trick1val);
 						}
 					}
 				}
 			}
 
 			
 			// zero out various entries
 			trick1.setText("");
 			trick2.setText("");
 			bid.setText("");
 			
 			// check if game has ended
 			if(mTeam1Score >= 500 && mTeam1Score > mTeam2Score) {
 				// go out, biggest
 				
 				HistoryDbHelper mDbHelper = new HistoryDbHelper(getActivity());
 				SQLiteDatabase db = mDbHelper.getWritableDatabase();
 				
 				// Create a new map of values, where column names are the keys
 				ContentValues values = new ContentValues();
 				values.put(FiveHundredHistoryFragment.COLUMN_NAME_WINNING_TEAM, name1val);
 				values.put(FiveHundredHistoryFragment.COLUMN_NAME_WINNING_SCORE, mTeam1Score);
 				values.put(FiveHundredHistoryFragment.COLUMN_NAME_LOSING_TEAM, name2val);
 				values.put(FiveHundredHistoryFragment.COLUMN_NAME_LOSING_SCORE, mTeam2Score);
 				
 				// Insert the new row, returning the primary key value of the new row
 				long newRowId;
 				newRowId = db.insert(
 						FiveHundredHistoryFragment.TABLE_NAME,
 				        null,
 				        values);
 				
 				errorToast("Team 1 wins! Game saved to History");
 			}
 			else if(mTeam2Score >= 500) {
 				// other team didn't win, and we went out, so we win
 				
 				HistoryDbHelper mDbHelper = new HistoryDbHelper(getActivity());
 				SQLiteDatabase db = mDbHelper.getWritableDatabase();
 				
 				// Create a new map of values, where column names are the keys
 				ContentValues values = new ContentValues();
 				values.put(FiveHundredHistoryFragment.COLUMN_NAME_WINNING_TEAM, name2val);
 				values.put(FiveHundredHistoryFragment.COLUMN_NAME_WINNING_SCORE, mTeam2Score);
 				values.put(FiveHundredHistoryFragment.COLUMN_NAME_LOSING_TEAM, name1val);
 				values.put(FiveHundredHistoryFragment.COLUMN_NAME_LOSING_SCORE, mTeam1Score);
 				
 				// Insert the new row, returning the primary key value of the new row
 				long newRowId;
 				newRowId = db.insert(
 						 FiveHundredHistoryFragment.TABLE_NAME,
 				         null,
 				         values);
 				
 				errorToast("Team 2 wins! Game saved to History");
 			}
 		}
 	
 	private void addTeam1Score(Integer value) {
 		mTeam1Score += value;
 		LinearLayout scrollArea = (LinearLayout)getActivity().findViewById(R.id.team1_list);
 		// add textview
 		TextView newScore = new TextView(getActivity());
 		newScore.setText(value.toString());
 		
 		scrollArea.addView(newScore);
 		
 		// update total scores
 		TextView score1 = (TextView)getActivity().findViewById(R.id.team1_score);
 		score1.setText(mTeam1Score.toString());
 	}
 	
 	private void addTeam2Score(Integer value) {
 		mTeam2Score += value;
 		LinearLayout scrollArea = (LinearLayout)getActivity().findViewById(R.id.team2_list);
 		// add textview
 		TextView newScore = new TextView(getActivity());
 		newScore.setText(value.toString());
 		
 		scrollArea.addView(newScore);
 		
 		TextView score2 = (TextView)getActivity().findViewById(R.id.team2_score);
 		score2.setText(mTeam2Score.toString());
 	}
 	
 	// reset scores page entirely
 		public void clear(View view) {
 			mTeam1Score = 0;
 			mTeam2Score = 0;
 			LinearLayout scrollArea1 = (LinearLayout)getActivity().findViewById(R.id.team1_list);
 			LinearLayout scrollArea2 = (LinearLayout)getActivity().findViewById(R.id.team2_list);
 			scrollArea1.removeAllViews();
 			scrollArea2.removeAllViews();
 			scrollArea1.invalidate();
 			scrollArea2.invalidate();
 
 
 			// update total scores
 			TextView score1 = (TextView)getActivity().findViewById(R.id.team1_score);
 			score1.setText(mTeam1Score.toString());
 			
 			TextView score2 = (TextView)getActivity().findViewById(R.id.team2_score);
 			score2.setText(mTeam2Score.toString());
 			
 			// zero out various entries
 			EditText trick1 = (EditText)getActivity().findViewById(R.id.team1_tricks);
 			EditText trick2 = (EditText)getActivity().findViewById(R.id.team2_tricks);
 			EditText bid = (EditText)getActivity().findViewById(R.id.bid);
 			trick1.setText("");
 			trick2.setText("");
 			bid.setText("");
 		}
 	
 	
 	// remove last set of scores entered
 		public void undo(View view) {
 			// make sure we have something to undo
 			LinearLayout scrollArea1 = (LinearLayout)getActivity().findViewById(R.id.team1_list);
 			LinearLayout scrollArea2 = (LinearLayout)getActivity().findViewById(R.id.team2_list);
 			if(scrollArea1.getChildCount() < 1 || scrollArea2.getChildCount() < 1) {
 				return;
 			}
 			
 			// get IDs of the last text box in each LinearLayout
 			TextView v1 = (TextView) scrollArea1.getChildAt(scrollArea1.getChildCount() - 1);
 			Integer toSubtract = Integer.parseInt(v1.getText().toString());
 			mTeam1Score -= toSubtract;
 			
 			// remove text box
 			scrollArea1.removeViewAt(scrollArea1.getChildCount() - 1);
 
 			// figure out scores to subtract
 			TextView total1 = (TextView)getActivity().findViewById(R.id.team1_score);
 			Integer newTotal = Integer.parseInt(total1.getText().toString()) - toSubtract;
 			total1.setText(newTotal.toString());
 			scrollArea1.invalidate();
 
 			// ID of last text box for team 2
 			TextView v2 = (TextView) scrollArea2.getChildAt(scrollArea2.getChildCount() - 1);
 			toSubtract = Integer.parseInt(v2.getText().toString());
 			mTeam2Score -= toSubtract;
 			
 			// remove text box
 			scrollArea2.removeViewAt(scrollArea2.getChildCount() - 1);
 
 			// figure out scores to subtract
 			TextView total2 = (TextView)getActivity().findViewById(R.id.team2_score);
 			newTotal = Integer.parseInt(total2.getText().toString()) - toSubtract;
 			total2.setText(newTotal.toString());
 			scrollArea2.invalidate();
 		}
 
 	
 	public void errorToast(String errorMsg) {
 		Context context = getActivity().getApplicationContext();
 		int duration = Toast.LENGTH_SHORT;
 		
 		Toast toast = Toast.makeText(context, errorMsg, duration);
 		toast.show();
 	}
 }
