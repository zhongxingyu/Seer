 package com.mikedougandnixon.scorekeeper;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class PlayerArrayAdapter extends ArrayAdapter {
 	private List<Player> playerList;
 	private Context myContext;
 	private int viewId;
 	private int holeNumber;
 
 	public PlayerArrayAdapter(Context c, int viewId, List<Player> p, int holeNumber) {
 		super(c, viewId);
 		playerList = p;
 		myContext = c;
 		this.viewId = viewId;
 		this.holeNumber = holeNumber;
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		
 		LayoutInflater li = ((Activity) myContext).getLayoutInflater();
 		View v = li.inflate(viewId, parent);
 
 		TextView totalScore = (TextView) v.findViewById(R.id.totalScoreTextView);
 		TextView holeScore = (TextView) v.findViewById(R.id.holeScoreTextView);
 		TextView playerName = (TextView) v.findViewById(R.id.playerNameTextView);
 		Button scoreUpButton = (Button) v.findViewById(R.id.scoreUpButton);
 		Button scoreDownButton = (Button) v.findViewById(R.id.scoreDownButton);
 
 		Player p = playerList.get(position);
 
 		scoreUpButton.setOnClickListener(new PlayerButtonOnClickListener(totalScore, holeScore, p, 1, holeNumber));
 		scoreDownButton.setOnClickListener(new PlayerButtonOnClickListener(totalScore, holeScore, p, -1, holeNumber));
 		
 		totalScore.setText(p.getTotalScore());
 		holeScore.setText(p.getScore(holeNumber));
 		playerName.setText(p.getName());
 
 		return v;
 	}
 
 	private class PlayerButtonOnClickListener implements OnClickListener {
 
 		TextView totalScore;
 		TextView holeScore;
 		Player p;
 		int delta;
 		int holeNumber;
 
 		public PlayerButtonOnClickListener(TextView totalScore, TextView holeScore, Player p, int delta, int holeNumber) {
 			this.totalScore = totalScore;
 			this.holeScore = holeScore;
 			this.p = p;
 			this.delta = delta;
 			this.holeNumber = holeNumber;
 		}
 
 		public void onClick(View arg0) {
 			int newScore = p.getScore(holeNumber) + delta;
 			p.setScore(holeNumber, newScore);
 			holeScore.setText("" + newScore);
 			totalScore.setText("" + p.getTotalScore());
 		}
 	}
 
 }
