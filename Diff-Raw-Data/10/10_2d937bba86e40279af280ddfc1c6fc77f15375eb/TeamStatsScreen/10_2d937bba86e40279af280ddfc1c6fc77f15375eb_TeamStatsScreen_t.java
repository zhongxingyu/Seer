 package com.naqtscoresheet;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.TextView;
 
 public class TeamStatsScreen extends Activity {
 	private Game game;
 	
 	private void updateTeamNames() {
 		TextView teamALabel = (TextView)findViewById(R.id.teamastatslabel);
 		TextView teamBLabel = (TextView)findViewById(R.id.teambstatslabel);
 		teamALabel.setText(game.getTeamA().getName());
 		teamBLabel.setText(game.getTeamB().getName());
 	}
 	
 	private void updateTeamStats() {
 		updateScores();
 		updatePowers();
 		updateNegs();
 		updatePointsPerBonus();
 		updateTossupsHeard();
 		updatePointsPerTossup();
 		updatePointsPer20TossupsHeard();
 	}
 	
 	private int getNumNegs(Team team) {
 		Tossup currTossup = game.getNthTossup(1);
 		int i = 1;
 		int numNegs = 0;
 		while (currTossup != null) {
 			if (currTossup.getLoserPoints() == -5 && currTossup.getLoserTeam().equals(team)) {
 				numNegs += 1;
 			}
 			i += 1;
 			currTossup = game.getNthTossup(i);
 		}
 		return numNegs;
 	}
 	
 	private void updateNegs() {
 		TextView teamANegsValue = (TextView)findViewById(R.id.teamanegsvalue);
 		TextView teamBNegsValue = (TextView)findViewById(R.id.teambnegsvalue);
 		teamANegsValue.setText(Integer.toString(this.getNumNegs(game.getTeamA())));
 		teamBNegsValue.setText(Integer.toString(this.getNumNegs(game.getTeamB())));
 	}
 	
 	private double getPointsPerBonus(Team team) {
 		Tossup currTossup = game.getNthTossup(1);
 		int i = 1;
		int bonusesHeard = 0;
 		double totalBonusPoints = 0.0;
 		while (currTossup != null) {
 			if (currTossup.getWinnerTeam().equals(team)) {
 				totalBonusPoints += currTossup.getBonus().getPoints();
				bonusesHeard += 1;
 			}
 			i += 1;
 			currTossup = game.getNthTossup(i);
 		}
		if (bonusesHeard == 0) {
 			return 0.0;
 		}
		return totalBonusPoints / (double)bonusesHeard;
 	}
 	
 	private double roundToNPlaces(double d, int n) {
 		double multFactor = Math.pow(10.0, n);
 		return Math.round(d * multFactor) / multFactor;
 	}
 	
 	private void updatePointsPerBonus() {
 		TextView teamAPPBValue = (TextView)findViewById(R.id.teamappbvalue);
 		TextView teamBPPBValue = (TextView)findViewById(R.id.teambppbvalue);
 		double teamAppb = this.roundToNPlaces(this.getPointsPerBonus(game.getTeamA()), 3);
 		double teamBppb = this.roundToNPlaces(this.getPointsPerBonus(game.getTeamB()), 3);
 		teamAPPBValue.setText(Double.toString(teamAppb));
 		teamBPPBValue.setText(Double.toString(teamBppb));
 	}
 	
 	private double getPointsPerTossup(Team team) {
 		Tossup currTossup = game.getNthTossup(1);
 		int i = 1;
 		double totalTossupPoints = 0.0;
 		while (currTossup != null) {
 			if (currTossup.getWinnerTeam().equals(team)) {
 				totalTossupPoints += currTossup.getWinnerPoints() + currTossup.getBonus().getPoints();
 			}
 			else {
 				totalTossupPoints += currTossup.getLoserPoints();
 			}
 			i += 1;
 			currTossup = game.getNthTossup(i);
 		}
 		double tossupsHeard = this.game.tossupsHeard();
 		if (this.game.tossupsHeard() == 0) {
 			return 0.0;
 		}
 		return totalTossupPoints / tossupsHeard;
 	}
 	
 	private void updatePointsPerTossup() {
 		TextView teamAPPTValue = (TextView)findViewById(R.id.teamapptvalue);
 		TextView teamBPPTValue = (TextView)findViewById(R.id.teambpptvalue);
 		double teamAppt = this.roundToNPlaces(this.getPointsPerTossup(game.getTeamA()), 3);
 		double teamBppt = this.roundToNPlaces(this.getPointsPerTossup(game.getTeamB()), 3);
 		teamAPPTValue.setText(Double.toString(teamAppt));
 		teamBPPTValue.setText(Double.toString(teamBppt));
 	}
 	
 	private double getPointsPer20TossupsHeard(Team team) {
 		return getPointsPerTossup(team) * 20.0;
 	}
 	
 	private void updatePointsPer20TossupsHeard() {
 		TextView teamAPP20THValue = (TextView)findViewById(R.id.teamapp20thvalue);
 		TextView teamBPP20THValue = (TextView)findViewById(R.id.teambpp20thvalue);
 		double teamApp20th = this.roundToNPlaces(this.getPointsPer20TossupsHeard(game.getTeamA()), 3);
 		double teamBpp20th = this.roundToNPlaces(this.getPointsPer20TossupsHeard(game.getTeamB()), 3);
 		teamAPP20THValue.setText(Double.toString(teamApp20th));
 		teamBPP20THValue.setText(Double.toString(teamBpp20th));
 	}
 	
 	private void updateTossupsHeard() {
 		TextView teamATossupsHeardValue = (TextView)findViewById(R.id.teamatossupsheardvalue);
 		TextView teamBTossupsHeardValue = (TextView)findViewById(R.id.teambtossupsheardvalue);
 		teamATossupsHeardValue.setText(Integer.toString(game.tossupsHeard()));
 		teamBTossupsHeardValue.setText(Integer.toString(game.tossupsHeard()));
 	}
 	
 	private void updateScores() {
 		TextView teamAScoreValue = (TextView)findViewById(R.id.teamascorevalue);
 		TextView teamBScoreValue = (TextView)findViewById(R.id.teambscorevalue);
 		teamAScoreValue.setText(Integer.toString(game.getTeamA().getScore()));
 		teamBScoreValue.setText(Integer.toString(game.getTeamB().getScore()));
 	}
 	
 	private int getNumPowers(Team team) {
 		Tossup currTossup = game.getNthTossup(1);
 		int i = 1;
 		int numPowers = 0;
 		while (currTossup != null) {
 			if (currTossup.getWinnerPoints() == 15 && currTossup.getWinnerTeam().equals(team)) {
 				numPowers += 1;
 			}
 			i += 1;
 			currTossup = game.getNthTossup(i);
 		}
 		return numPowers;
 	}
 	
 	private void updatePowers() {
 		TextView teamAPowersValue = (TextView)findViewById(R.id.teamapowersvalue);
 		TextView teamBPowersValue = (TextView)findViewById(R.id.teambpowersvalue);
 		teamAPowersValue.setText(Integer.toString(getNumPowers(game.getTeamA())));
 		teamBPowersValue.setText(Integer.toString(getNumPowers(game.getTeamB())));
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.team_stats);
 		
 		Bundle bun = getIntent().getExtras();
 		this.game = (Game)bun.getSerializable("game");
 				
 		updateTeamNames();
 		updateTeamStats();
 	}
 }
