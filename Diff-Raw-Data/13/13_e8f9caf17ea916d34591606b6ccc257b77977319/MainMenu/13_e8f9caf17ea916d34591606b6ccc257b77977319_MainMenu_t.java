 package com.gamblore.ld48.twentyfour.worlds;
 
 import net.androidpunk.Entity;
 import net.androidpunk.FP;
 import net.androidpunk.World;
 import net.androidpunk.graphics.atlas.AtlasGraphic;
 import net.androidpunk.graphics.atlas.AtlasText;
 import net.androidpunk.graphics.atlas.GraphicList;
 import net.androidpunk.graphics.atlas.Image;
 import net.androidpunk.utils.Input;
 import android.graphics.Point;
 import android.util.Log;
 
 import com.gamblore.ld48.twentyfour.MainEngine;
 import com.gamblore.ld48.twentyfour.entities.Ant;
 import com.gamblore.ld48.twentyfour.entities.AntGroup;
 
 public class MainMenu extends World {
 	
 	private static final String TAG = "MainMenu";
 	
 	private static final int[][] ANT_POSITIONS = new int[][] { new int[] { 48*2, 48*2 }, new int[] { 3 * 48*2, 48*2}, new int[] { 48*2, 3 * 48*2 }, new int[] {3 * 48*2,3 * 48*2 }};
 	
 	private AtlasText mLogText, mFundsText, mChallengeText;
 	
 	public MainMenu() {
 		super();
 		
 		FP.activity.setOnBackCallback(MainEngine.IN_GAME_BACK_CALLBACK);
 		
 		Entity e = new Entity();
 		
 		Image background = new Image(MainEngine.mAtlas.getSubTexture("mainmenu"));
 		background.scale = 2;
 		
 		mLogText = new AtlasText("Tap on the ant species\nto evolve.\n\nEvolution reduces numbers\nbut modifies traits.", 14, MainEngine.mTypeface);
 		mLogText.x = 18 * 2;
 		mLogText.y = 208 * 2;
 		
 		mFundsText = new AtlasText(String.format("$%d", MainEngine.PLAYER.getFunds()), 14, MainEngine.mTypeface);
 		mFundsText.x = FP.screen.getWidth()/2 - mFundsText.getWidth()/2;
 		mFundsText.y = 4;
 		
		mChallengeText = new AtlasText("Challenge!", 20, MainEngine.mTypeface);
 		mChallengeText.x = FP.screen.getWidth()/2 - mChallengeText.getWidth()/2;
 		mChallengeText.y = 352 * 2;
 		mChallengeText.relative = false;
 		
 		e.setGraphic(new GraphicList(background, mLogText, mFundsText));
 		e.setLayer(10);
 		
 		add(e);
 		
		Entity challengeEntity = new Entity(48*2, 336*2);
		challengeEntity.setGraphic(mChallengeText);
		challengeEntity.setHitbox(3 * 48*2, 48*2);
		challengeEntity.setType("challenge");
		add(challengeEntity);
 		
 		getDisplayAnts();
 	}
 	
 	private void getDisplayAnts() {
 		AntGroup[] antGroups = MainEngine.PLAYER.getAntGroups();
 		for (int i = 0; i < antGroups.length; i++) {
 			Ant a = antGroups[i].getAnt();
 			a.visible = true;
 			if (i % 2 == 1) {
 				((AtlasGraphic)a.getGraphic()).scaleX = -1;
 			}
 			
 			((AtlasGraphic)a.getGraphic()).scale = 3;
 			
 			a.x = ANT_POSITIONS[i][0];
 			a.y = ANT_POSITIONS[i][1];
 			
 			a.setHitbox(a.getWidth(), a.getHeight());
 			a.setType("ant");
 			a.vibrate();
 			
 			add(a);
 		}
 	}
 
 	@Override
 	public void update() {
 		super.update();
 		
 		if (Input.mousePressed) {
 			Point p = Input.getTouches()[0];
 			Entity e;
 			if ((e = collidePoint("ant", p.x, p.y)) != null) {
 				Ant a = (Ant)e;
 				Log.d(TAG, "Touched " + a.getAntGroup().toString());
 				FP.setWorld(new EvolveMenu(a.getAntGroup()));
 			}
 			if ((e = collidePoint("challenge", p.x, p.y)) != null) {
 				Log.d(TAG, "Touched Challenge");
 				FP.setWorld(new ChallengeMenu());
 			}
 		}
 	}
 	
 	
 }
