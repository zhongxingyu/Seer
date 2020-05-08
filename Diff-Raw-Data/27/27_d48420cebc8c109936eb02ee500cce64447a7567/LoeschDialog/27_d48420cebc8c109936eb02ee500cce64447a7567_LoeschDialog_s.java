 package com.dhbw.dvst.helper;
 
 import android.app.Activity;
 
 import com.dhbw.dvst.model.Control;
 import com.dhbw.dvst.model.Spiel;
 import com.dhbw.dvst.model.Spieler;
 
 public class LoeschDialog extends PopupService {
 	private Spiel spiel = Control.getInstance();
 	private Spieler spieler;
 
	public LoeschDialog(Activity activity, String fehler, Spieler spieler) {
 		super(activity, fehler, spieler);
 		this.spieler = spieler;
 	}
 	
 	protected void klickePositivenButton() {
 		spiel.spielerLoeschen(LoeschDialog.this.spieler);
 	}
 }
