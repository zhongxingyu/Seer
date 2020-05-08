 package org.pticlic;
 
 import org.pticlic.exception.PtiClicException;
import org.pticlic.games.BaseGame;
 import org.pticlic.model.Constant;
 import org.pticlic.model.DownloadedBaseGame;
 import org.pticlic.model.Match;
 import org.pticlic.model.Network;
 import org.pticlic.model.Network.Mode;
 import org.pticlic.model.Network.ScoreResponse;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 /**
  * @author John CHARRON
  * 
  * Permet l'affichage du score obtenu par le joueur lors de sa partie.
  */
 public class BaseScore extends Activity implements OnClickListener{
 
 	private Match           gamePlayed;
 	private ScoreResponse   sr = null;
 	
 	private void networkStuff() {
 		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
 		String id = sp.getString(Constant.USER_ID, "joueur");
 		String passwd = sp.getString(Constant.USER_PASSWD, "");
 		String serverURL = sp.getString(Constant.SERVER_URL, Constant.SERVER);
 		Mode mode = null;
 
 		if (getIntent().getExtras() != null) {
 			// GamePlayed contient toutes les infos sur la partie jouee
 			this.gamePlayed = (Match) getIntent().getExtras().get(Constant.SCORE_GAMEPLAYED);
 			mode = (Mode) getIntent().getExtras().get(Constant.SCORE_MODE);
 		}
 
 		// TODO : factoriser le serverUrl dans Network
 		sp.edit().remove(Constant.NEW_BASE_GAME).commit();
 		Network network = new Network(serverURL, mode, id, passwd);
 		try {
 			sr = network.sendBaseGame(gamePlayed);
 			sp.edit().putString(Constant.NEW_BASE_GAME, sr.getNewGame()).commit();
 		} catch (PtiClicException e) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle(getString(R.string.app_name))
 			.setIcon(android.R.drawable.ic_dialog_alert)
 			.setMessage(e.getMessage())
 			.setCancelable(false)
 			.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					dialog.cancel();
 					finish();
 				}
 			});
 			AlertDialog alert = builder.create();
 			alert.show();
 		} catch (Exception e) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle(getString(R.string.app_name))
 			.setIcon(android.R.drawable.ic_dialog_alert)
 			.setMessage(R.string.server_down)
 			.setCancelable(false)
 			.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					dialog.cancel();
 					finish();
 				}
 			});
 			AlertDialog alert = builder.create();
 			alert.show();
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.score);
 
 		this.networkStuff();
 
 		// TODO : Attention, le cast en (BaseGame) n'est pas sûr !
 		DownloadedBaseGame bg = (DownloadedBaseGame)gamePlayed.getGame();
 		((TextView)findViewById(R.id.total)).setText(String.valueOf(sr.getScoreTotal()));
 		((TextView)findViewById(R.id.scoreRel1)).setText(bg.getCatString(1));
 		((TextView)findViewById(R.id.scoreRel2)).setText(bg.getCatString(2));
 		((TextView)findViewById(R.id.scoreRel3)).setText(bg.getCatString(3));
 		((TextView)findViewById(R.id.scoreRel4)).setText(bg.getCatString(4));
 		
 		String res;
 		res = "";
 		for (int i : gamePlayed.getRelation1()) {
 			res += bg.getWordInCloud(i).getName();
 			res += " (" + String.valueOf(sr.getScoreOfWord(i)) + "), ";
 		}
 		((TextView)findViewById(R.id.scoreWords1)).setText(res);
 		
 		res = "";
 		for (int i : gamePlayed.getRelation2()) {
 			res += bg.getWordInCloud(i).getName();
 			res += " (" + String.valueOf(sr.getScoreOfWord(i)) + "), ";
 		}
 		((TextView)findViewById(R.id.scoreWords2)).setText(res);
 		
 		res = "";
 		for (int i : gamePlayed.getRelation3()) {
 			res += bg.getWordInCloud(i).getName();
 			res += " (" + String.valueOf(sr.getScoreOfWord(i)) + "), ";
 		}
 		((TextView)findViewById(R.id.scoreWords3)).setText(res);
 		
 		res = "";
 		for (int i : gamePlayed.getRelation4()) {
 			res += bg.getWordInCloud(i).getName();
 			res += " (" + String.valueOf(sr.getScoreOfWord(i)) + "), ";
 		}
 		((TextView)findViewById(R.id.scoreWords4)).setText(res);
 		
 		((Button)findViewById(R.id.saw)).setOnClickListener(this);
 	}
 
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 
 		finish();
 	}
 
 	protected double calculateTotal(){
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v.getId()==R.id.saw) {
 			finish();
 		}
 	}
 }
