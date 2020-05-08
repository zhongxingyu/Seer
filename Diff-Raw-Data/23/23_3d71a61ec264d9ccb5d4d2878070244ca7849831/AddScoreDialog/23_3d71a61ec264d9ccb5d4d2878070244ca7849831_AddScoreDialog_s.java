 package org.homelinux.murray.scorekeep;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class AddScoreDialog extends Dialog implements View.OnClickListener,TextView.OnEditorActionListener {
 	private static final String DEBUG_TAG = "ScoreKeep:AddScoreDialog";
 	private final TextView scoreEdit;
 	private final TextView contextEdit;
 
 	private final PlayerData playerData;
 	private final MathEval mathEval = new MathEval();
 
 	public AddScoreDialog(Context parent, PlayerData playerData) {
 		super(parent);
 		this.playerData = playerData;
 		//TODO support alternate dialogs
 		setContentView(R.layout.add_score_generic);
 		setTitle(parent.getText(R.string.add_score));
 		
 		//generic elements
 		scoreEdit = (TextView) findViewById(R.id.score_edit);
 		contextEdit = (TextView) findViewById(R.id.context_edit);
 		Button minusFive = (Button) findViewById(R.id.minus_5);
 		Button minusOne = (Button) findViewById(R.id.minus_1);
 		Button plusOne = (Button) findViewById(R.id.plus_1);
 		Button plusFive = (Button) findViewById(R.id.plus_5);
 		Button ok = (Button) findViewById(R.id.ok);
 		Button cancel = (Button) findViewById(R.id.cancel);
 		
 		scoreEdit.setOnEditorActionListener(this);
 		contextEdit.setOnEditorActionListener(this);
 		
 		minusFive.setOnClickListener(this);
 		minusOne.setOnClickListener(this);
 		plusOne.setOnClickListener(this);
 		plusFive.setOnClickListener(this);
 		
 		ok.setOnClickListener(this);
 		cancel.setOnClickListener(this);
 	}
 
 
 	public void onClick(View v) {
 		int amount = 0;
 		switch(v.getId()) {
 		case R.id.minus_5:
 			amount = -5;
 			break;
 		case R.id.minus_1:
 			amount = -1;
 			break;
 		case R.id.plus_1:
 			amount = 1;
 			break;
 		case R.id.plus_5:
 			amount = 5;
 			break;
 		case R.id.ok:
 			finish();
 			return;
 		case R.id.cancel:
 			dismiss();
 		default:
 			return;
 		}
 		if(amount != 0) {
 			String startExp = scoreEdit.getText().toString();
 			if(startExp.isEmpty()) {
 				scoreEdit.setText(Integer.toString(amount));
 			} else {
 				Long curNum = Long.getLong(startExp);
 				if(curNum == null) {
 					try {
 						curNum = Math.round(mathEval.evaluate(scoreEdit.getText().toString()));
 					} catch(Exception e) {
 						Log.d(DEBUG_TAG, "Math Eval failed: "+e);
 						return;
 					}
 				}
 				scoreEdit.setText(Long.toString(curNum + amount));
 			}
 			return;
 		}
 		
 	}
 
 
 	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 		if(actionId == EditorInfo.IME_ACTION_DONE || actionId == KeyEvent.KEYCODE_ENTER) {
 			finish();
 			return true;
 		}
 		return false;
 	}
 	
 	private void finish() {
 		String startExp = scoreEdit.getText().toString();
 		Long addScore = Long.getLong(startExp);
		if(addScore == null) {
 			try {
 				addScore = Math.round(mathEval.evaluate(startExp));
 			} catch(Exception e) {
 				Log.d(DEBUG_TAG, "Failed to get a valid value "+e);
 				Toast.makeText(this.getContext(), R.string.number_parse_failed, Toast.LENGTH_LONG).show();
 				return; //failed, we can't add a score.
 			}
 		}

 		playerData.addScoreAndContext(addScore, contextEdit.getText().toString());
 		dismiss();
 	}
 }
