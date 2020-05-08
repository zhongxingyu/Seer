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
 import android.app.Activity;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.method.ScrollingMovementMethod;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import com.cheesymountain.woe.SimpleGestureFilter.SimpleGestureListener;
 
 import com.cheesymountain.woe.combat.*;
 import com.cheesymountain.woe.enemies.*;
 import com.cheesymountain.woe.food.*;
 import com.cheesymountain.woe.interact.*;
 import com.cheesymountain.woe.item.*;
 import com.cheesymountain.woe.training.*;
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
 	private static final int DIALOG_EXIT_APP_ID = 1;
 	private static final int DIALOG_FIGHT_ID = 10;
 	private static final int DIALOG_OI_ID = 100;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	this.onSwipe(SimpleGestureFilter.SWIPE_RIGHT);
         use = new Use();
         detector = new SimpleGestureFilter(this, this);
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
     public void onSaveInstanceState(Bundle savedInstanceState){
     	super.onSaveInstanceState(savedInstanceState);
     	savedInstanceState.putString("Name", Everbie.getEverbie().getName());
     	savedInstanceState.putInt("maxHealth", Everbie.getEverbie().getMaxHealth());
     	savedInstanceState.putInt("health", Everbie.getEverbie().getHealth());
     	savedInstanceState.putInt("Strength", Everbie.getEverbie().getStrength());
     	savedInstanceState.putInt("Intelligence", Everbie.getEverbie().getIntelligence());
     	savedInstanceState.putInt("stamina", Everbie.getEverbie().getStamina());
     	savedInstanceState.putInt("charm", Everbie.getEverbie().getCharm());
     	savedInstanceState.putInt("fullness", Everbie.getEverbie().getFullness());
     	savedInstanceState.putInt("happiness", Everbie.getEverbie().getHappiness());
     	savedInstanceState.putInt("toxicity", Everbie.getEverbie().getToxicity());
     	savedInstanceState.putInt("cuteness", Everbie.getEverbie().getCuteness());
     	savedInstanceState.putInt("money", Everbie.getEverbie().getMoney());
     	savedInstanceState.putBoolean("Alive", Everbie.getEverbie().isAlive());
     	savedInstanceState.putInt("imagePath", Everbie.getEverbie().getImageId());
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item){
 		switch(item.getItemId()){
 			case R.id.BreadAndWater:
 				use.activate(new BreadAndWater());
 	    		break;
 			case R.id.Melon:
 				use.activate(new Melon());
 	    		break;
 			case R.id.ribEyeStake:
 				use.activate(new RibEyeSteak());
 	    		break;
 			case R.id.chat:
 				use.activate(new Chat());
 	    		break;
 			case R.id.comb:
 				use.activate(new Comb());
 	    		break;
 			case R.id.snuggle:
 				use.activate(new Snuggle());
 	    		break;
 			case R.id.book:
 				use.activate(new Book());
 	    		break;
 			case R.id.healthPotion:
 				use.activate(new HealthPotion());
 	    		break;
 			case R.id.kettleBell:
 				use.activate(new Kettlebell());
 	    		break;
 			case R.id.ribbon:
 				use.activate(new Ribbon());
 	    		break;
 			case R.id.skippingRope:
 				use.activate(new SkippingRope());
 	    		break;
 			case R.id.chess:
 				use.activate(new Chess());
 	    		break;
 			case R.id.running:
 				use.activate(new Running());
 	    		break;
 			case R.id.workingOut:
 				use.activate(new WorkingOut());
 	    		break;
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
 	    		use.activate(new SellLemonade());
 	    		break;
 	    	case R.id.gnome:
 	    		enemy = new GardenGnome();
 	    		onCreateDialog(DIALOG_FIGHT_ID);
 	    		break;
 	    	case R.id.golem:
 	    		enemy = new GarbageGolem();
 	    		onCreateDialog(DIALOG_FIGHT_ID);
 	    		break;
 	    	case R.id.spider:
 	    		enemy = new OversizedSpider();
 	    		onCreateDialog(DIALOG_FIGHT_ID);
 	    		break;
 	    	case R.id.titan:
 	    		enemy = new ScrapMetalTitan();
 	    		onCreateDialog(DIALOG_FIGHT_ID);
 	    		break;
 	    	default:
 	    		return false;
 		}
 		onSwipe(SimpleGestureFilter.SWIPE_RIGHT);
 		return true;
     }
     
     @Override
     public Dialog onCreateDialog(int i){
     	if(i==DIALOG_EXIT_APP_ID){
     		Builder builder = new Builder(this);
     		builder.setMessage("Are you sure you want to Exit World of Everbies?");
     		builder.setCancelable(true);
     		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
     			public void onClick(DialogInterface dialog, int which) {
     				finish();
     			}
     		});
     		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
     			public void onClick(DialogInterface dialog, int which) {
     				dialog.cancel();
     			}
     		});
     		return builder.create();
     	}else if(i == DIALOG_FIGHT_ID){
     		Builder builder = new Builder(this);
     		builder.setMessage("How do you wish to fight?");
     		builder.setCancelable(false);
     		builder.setPositiveButton("Offensive", new DialogInterface.OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					new Combat(enemy, new Offensive());
 					dialog.cancel();
 				}
 			});
 			builder.setNeutralButton("Defensive", new DialogInterface.OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					new Combat(enemy, new Defensive());
 					dialog.cancel();
 				}
 			});
 			builder.setNegativeButton("Tactical", new DialogInterface.OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					new Combat(enemy, new Tactical());
 					dialog.cancel();
 				}
 			});
     	}else if(i == DIALOG_OI_ID){
     		Builder builder = new Builder(this);
     		builder.setMessage("Ooops, you do not have enough Oi to buy that.");
     		builder.setCancelable(true);
     		builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.cancel();
 				}
 			});
     	}
     	return null;
     }
     
     @Override
     public void onOptionsMenuClosed(Menu menu) {
     	super.onOptionsMenuClosed(menu);
     }
 
     
     /**
      * Rewrites the TextArea with the current version
      * of the Log.
      */
     public void updateLog(){
     	((TextView)findViewById(R.id.log)).setText(Log.getLog().getLogList());
     	((TextView)findViewById(R.id.log)).setMovementMethod(new ScrollingMovementMethod());
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
