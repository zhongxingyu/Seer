 package com.cheesymountain.woe;
 /*=============================================================
  * Copyright 2012, Cheesy Mountain Production
  * 
  * This file is part of World of Everbies.
  * 
  * World of Everbies is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * World of Everbies is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with World of Everbies.  If not, see <http://www.gnu.org/licenses/>.
 ================================================================*/
 import android.annotation.SuppressLint;
 import android.app.*;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.text.method.ScrollingMovementMethod;
 import android.view.*;
 import android.widget.*;
 
 import com.cheesymountain.woe.R;
 import com.cheesymountain.woe.combat.*;
 import com.cheesymountain.woe.enemies.*;
 import com.cheesymountain.woe.food.*;
 import com.cheesymountain.woe.interact.*;
 import com.cheesymountain.woe.item.*;
 import com.cheesymountain.woe.training.*;
 import com.cheesymountain.woe.util.SimpleGestureFilter;
 import com.cheesymountain.woe.util.SimpleGestureFilter.SimpleGestureListener;
 import com.cheesymountain.woe.work.*;
 
 @SuppressLint("NewApi")
 /**
  * Main Activity that keeps most of the controlling
  * functionality such as saving and loading game and
  * handles events from OptionsMenu. Moreover it controls
  * the exitGame functionality and what buttons can activate it.
  * 
  * @author CheesyMountain
  * 
  */
 public class MainActivity extends Activity implements SimpleGestureListener{
 
 	private Use use;
 	private SimpleGestureFilter detector;
 	private Enemy enemy;
 	private MediaPlayer bgMusic;
 	private static final int DIALOG_FIGHT_ID = 1;
 	private static final int DIALOG_OI_ID = 10;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.onSwipe(SimpleGestureFilter.SWIPE_RIGHT);
 		bgMusic = MediaPlayer.create(MainActivity.this, R.raw.everbiebgm);
 		use = new Use();
 		detector = new SimpleGestureFilter(this, this);
 
 		Database db = new Database(this);
 		db.open();
 		db.load();
 		db.close();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu){
 		MenuItem feedLabel = ((MenuItem)(menu.findItem(R.id.menuOiFeed)));
 		feedLabel.setTitle("Oi: " + Everbie.getEverbie().getMoney());
 
 		MenuItem storeLabel = ((MenuItem)(menu.findItem(R.id.menuOiStore)));
 		storeLabel.setTitle("Oi: " + Everbie.getEverbie().getMoney());
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public void onBackPressed(){
 		Intent intent = new Intent("com.cheesymountain.woe.STARTSCREENACTIVITY");
 		startActivity(intent);
 		finish();
 	}
 
 	@Override
 	public void onPause(){
 		super.onPause();
 		bgMusic.pause();
 
 		Database db = new Database(this);
 		db.open();
 		db.save(Everbie.getEverbie());
 		db.close();
 		use.stopOccupation();
 		Everbie.getEverbie().kill();
 	}
 
 	@Override
 	public void onResume(){
 		bgMusic.start();
 		bgMusic.setLooping(true);
 		super.onResume();
 		
 		Database db = new Database(this);
 		db.open();
 		db.load();
 		db.close();
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item){
 		switch(item.getItemId()){
 
 		//Food
 		case R.id.BreadAndWater:
 			if(use.activate(new BreadAndWater()) == Use.NOT_ENOUGH_OI){
 				showDialog(DIALOG_OI_ID);
 			}
 			break;
 		case R.id.Melon:
 			if(use.activate(new Melon()) == Use.NOT_ENOUGH_OI){
 				showDialog(DIALOG_OI_ID);
 			}
 			break;
 		case R.id.ribEyeStake:
 			if(use.activate(new RibEyeSteak()) == Use.NOT_ENOUGH_OI){
 				showDialog(DIALOG_OI_ID);
 			}
 			break;
 
 			//Interaction
 		case R.id.chat:
 			use.activate(new Chat());
 			break;
 		case R.id.pamper:
 			use.activate(new Pamper());
 			break;
 		case R.id.snuggle:
 			use.activate(new Snuggle());
 			break;
 		case R.id.playCards:
 			use.activate(new PlayCards());
 			break;
 		case R.id.takeABath:
 			use.activate(new TakeABath());
 			break;
 
 			//Item
 		case R.id.book:
 			if(use.activate(new Book()) == Use.NOT_ENOUGH_OI){
 				showDialog(DIALOG_OI_ID);
 			}
 			break;
 		case R.id.healthPotion:
 			if(use.activate(new HealthPotion()) == Use.NOT_ENOUGH_OI){
 				showDialog(DIALOG_OI_ID);
 			}
 			break;
 		case R.id.kettleBell:
 			if(use.activate(new Kettlebell()) == Use.NOT_ENOUGH_OI){
 				showDialog(DIALOG_OI_ID);
 			}
 			break;
 		case R.id.ribbon:
 			if(use.activate(new Ribbon()) == Use.NOT_ENOUGH_OI){
 				showDialog(DIALOG_OI_ID);
 			}
 			break;
 		case R.id.skippingRope:
 			if(use.activate(new SkippingRope()) == Use.NOT_ENOUGH_OI){
 				showDialog(DIALOG_OI_ID);
 			}
 			break;
 
 			//Training
 		case R.id.chess:
 			use.activate(new Chess());
 			break;
 		case R.id.running:
 			use.activate(new Running());
 			break;
 		case R.id.workingOut:
 			use.activate(new WorkingOut());
 			break;
 		case R.id.squash:
 			use.activate(new Squash());
 			break;
 		case R.id.swimming:
 			use.activate(new Swimming());
 
 			//Work
 		case R.id.consulting:
 			use.activate(new Consulting());
 			break;
 		case R.id.dogWalking:
 			use.activate(new DogWalking());
 			break;
 		case R.id.motelCleaning:
 			use.activate(new MotelCleaning());
 			break;
 		case R.id.plumbing:
 			use.activate(new Plumbing());
 			break;
 		case R.id.sellLemonade:
 			int specificSalary = (int)((Math.abs(Everbie.getEverbie().getCharm() + Everbie.getEverbie().getCuteness())
 					+ (Everbie.getEverbie().getIntelligence()/2))*Math.random() + 42);
 			use.activate(new SellLemonade(specificSalary));
 			break;
 
 			//Fight
 		case R.id.gnome:
 			if(!checkEverbieStatus()){
 				break;
 			}
 			enemy = new GardenGnome();
 			showDialog(DIALOG_FIGHT_ID);
 			break;
 		case R.id.golem:
 			if(!checkEverbieStatus()){
 				break;
 			}
 			enemy = new GarbageGolem();
 			showDialog(DIALOG_FIGHT_ID);
 			break;
 		case R.id.spider:
 			if(!checkEverbieStatus()){
 				break;
 			}
 			enemy = new OversizedSpider();
 			showDialog(DIALOG_FIGHT_ID);
 			break;
 		case R.id.titan:
 			if(!checkEverbieStatus()){
 				break;
 			}
 			enemy = new ScrapMetalTitan();
 			showDialog(DIALOG_FIGHT_ID);
 			break;
 
 			//Default
 		default:
 			return false;
 		}
 		onSwipe(SimpleGestureFilter.SWIPE_RIGHT);
 		return true;
 	}
 	
 	/**
 	 * Checks wheter the everbie is alive and available (i.e. not fainted nor occupied).
 	 * @return <code>true</code> if Everbie is alive and available, <code>false</code> otherwise.
 	 */
 	private boolean checkEverbieStatus(){
 		if(!Everbie.getEverbie().isAlive()){
 			Log.getLog().isDead();
 		}else if(Everbie.getEverbie().isFainted()){
 			Log.getLog().isFainted();
 		}else if(Everbie.getEverbie().isOccupied()){
 			Log.getLog().isBusy();
 		}else{
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public Dialog onCreateDialog(int i){
 		if(i == DIALOG_FIGHT_ID){
 			Builder builder = new Builder(this);
 			builder.setMessage("How do you wish to fight?");
 			builder.setCancelable(true);
 			builder.setPositiveButton("Offensive", new DialogInterface.OnClickListener() {
 
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.cancel();
 					Combat combat = new Combat(enemy, new Offensive());
 					combat.doFight();
 					onSwipe(SimpleGestureFilter.SWIPE_RIGHT);
 				}
 			});
 			builder.setNeutralButton("Defensive", new DialogInterface.OnClickListener() {
 
 				public void onClick(DialogInterface dialog, int which) {
 					Combat combat = new Combat(enemy, new Defensive());
 					combat.doFight();
 					onSwipe(SimpleGestureFilter.SWIPE_RIGHT);
 					dialog.cancel();
 				}
 			});
 			builder.setNegativeButton("Tactical", new DialogInterface.OnClickListener() {
 
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.cancel();
 					Combat combat = new Combat(enemy, new Tactical());
 					combat.doFight();
 					onSwipe(SimpleGestureFilter.SWIPE_RIGHT);
 				}
 			});
 			return builder.create();
 		}else if(i == DIALOG_OI_ID){
 			Builder builder = new Builder(this);
 			builder.setMessage("Ooops, you do not have enough Oi to buy that.");
 			builder.setCancelable(true);
 			builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
 
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.cancel();
 				}
 			});
 			return builder.create();
 		}
 		return null;
 	}
 
 	/**
 	 * Rewrites the TextArea with the current version
 	 * of the Log.
 	 */
 	public void updateLog(){
 		((TextView)findViewById(R.id.log)).setText(Log.getLog().getLogList());
 		((TextView)findViewById(R.id.log)).setMovementMethod(new ScrollingMovementMethod());
 	}
	
	public void menuPressed(View view){
		openOptionsMenu();
	}
	
 
 	public void onSwipe(int direction) {
 		if(direction == SimpleGestureFilter.SWIPE_RIGHT){
 			this.setContentView(R.layout.activity_main);
 			((ImageView)findViewById(R.id.mainImage)).setImageResource(Everbie.getEverbie().getImageId());
 			updateLog();
 		}
 		if(direction == SimpleGestureFilter.SWIPE_LEFT){
 			this.setContentView(R.layout.activity_stats);
 
 			((ImageView)findViewById(R.id.statsImage)).setImageResource(Everbie.getEverbie().getImageId());
 
 			((TextView)findViewById(R.id.nameText)).setText(Everbie.getEverbie().getName() + "");
 			((TextView)findViewById(R.id.moneyText)).setText(Everbie.getEverbie().getMoney() + "");
 			((TextView)findViewById(R.id.charmText)).setText(Everbie.getEverbie().getCharm() + "");
 			((TextView)findViewById(R.id.cuteText)).setText(Everbie.getEverbie().getCuteness() + "");
 			((TextView)findViewById(R.id.levelText)).setText(Everbie.getEverbie().getLevel() + "");
 			((TextView)findViewById(R.id.strengthText)).setText(Everbie.getEverbie().getStrength() + "");
 			((TextView)findViewById(R.id.staminaText)).setText(Everbie.getEverbie().getStamina() + "");
 			((TextView)findViewById(R.id.intelligenceText)).setText(Everbie.getEverbie().getIntelligence() + "");
 
 			((ProgressBar)findViewById(R.id.fullnessBar)).setProgress(Everbie.getEverbie().getFullness());
 			((ProgressBar)findViewById(R.id.happinessBar)).setProgress(Everbie.getEverbie().getHappiness());
 			((ProgressBar)findViewById(R.id.toxicityBar)).setProgress(Everbie.getEverbie().getToxicity());
 			((ProgressBar)findViewById(R.id.healthBar)).setMax(Everbie.getEverbie().getMaxHealth());
 			((ProgressBar)findViewById(R.id.healthBar)).setProgress(Everbie.getEverbie().getHealth());
 		}
 	}
 
 	public void onDoubleTap() {
 		//Do nothing
 	}
 
 	@Override
 	public boolean dispatchTouchEvent(MotionEvent me){
 		detector.onTouchEvent(me);
 		return super.dispatchTouchEvent(me);
 	}
 }
