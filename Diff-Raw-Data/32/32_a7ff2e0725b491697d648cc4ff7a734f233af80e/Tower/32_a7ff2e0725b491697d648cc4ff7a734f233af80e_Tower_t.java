 package com.matthewtole.androidrise.game.pieces;
 
 import com.matthewtole.androidrise.game.SpriteManager;
 import com.matthewtole.androidrise.game.enums.GamePlayer;
 import com.matthewtole.androidrise.lib.Utils;
 
public class Tower extends CompositePiece {
 	
 	private int level = 0;
 
 	public Tower(SpriteManager sprites, GamePlayer player) {
 		super(sprites);
		this.addPiece(new BitmapPiece(sprites, "error"));
		this.hidePiece(0);
		this.addPiece(new BitmapPiece(sprites, (Utils.playerString(player) + "_tower1")));
		this.addPiece(new BitmapPiece(sprites, (Utils.playerString(player) + "_tower2")));
		this.addPiece(new BitmapPiece(sprites, (Utils.playerString(player) + "_tower3")));
		this.hidePiece(1);
		this.hidePiece(2);
		this.hidePiece(3);
 	}
 
 	public void removeLevel() {
		this.hidePiece(this.level);
 		level -= 1;
		if (this.level < 0) { this.level = 0; }
 	}
 
 	public void addLevel() {
 		level += 1;
		if (this.level > 3) { this.level = 3; }
		this.showPiece(this.level);
 	}
 }
