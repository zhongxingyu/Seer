 package com.panicgame.core;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import com.panicgame.playeractions.CalmDown;
 import com.panicgame.playeractions.DropPeople;
 import com.panicgame.playeractions.HealPeople;
 import com.panicgame.playeractions.PlayerAction;
 import com.panicgame.popupwindow.*;
 import com.panicgame.popupwindow.QuickAction.OnActionItemClickListener;
 
 import com.panicgame.actions.Action;
 import com.panicgame.actions.BroadcastMessage;
 import com.panicgame.actions.CalmDownPeople;
 import com.panicgame.actions.FixEvent;
 import com.panicgame.actions.GetMap;
 import com.panicgame.actions.ImDone;
 import com.panicgame.actions.JoineGame;
 import com.panicgame.actions.MovePeople;
 import com.panicgame.actions.PickedEquipment;
 import com.panicgame.actions.PlayerLeave;
 import com.panicgame.actions.SetPosition;
 import com.panicgame.actions.UseMedic;
 import com.panicgame.creators.MapCreator;
 import com.panicgame.creators.SetLocationCreator;
 import com.panicgame.listeners.EventListener;
 import com.panicgame.listeners.GameListener;
 import com.panicgame.listeners.MapListener;
 import com.panicgame.listeners.SectorListener;
 import com.panicgame.managers.InputManager;
 import com.panicgame.models.Civilian;
 import com.panicgame.models.Game;
 import com.panicgame.models.Map;
 import com.panicgame.models.Player;
 import com.panicgame.models.ProgressObject;
 import com.panicgame.models.Sector;
 import com.panicgame.models.Team;
 import com.panicgame.tools.Equipment;
 import com.panicgame.tools.Extinguisher;
 import com.panicgame.tools.Hammer;
 import com.panicgame.utils.Util;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.GradientDrawable.Orientation;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.os.PowerManager;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewFlipper;
 import android.widget.ViewSwitcher;
 
 public class GameActivity extends SuperActivity implements GameListener, EventListener, MapListener, SectorListener {
 
 	private int actionsLeft = 3;
 	private int movementLeft = 2;
 	private boolean yourturn = false;
 
 	private ViewFlipper switcher;
 	private static Button doneBtn, backToMap;
 	private Animation movePageLeft, movePageRight, movePageToRight, movePageToLeft;
 	private static TextView panicTimer, playerName, your_turn_clock_value, role_name;
 	private PanicWaveCounter panicTimeCounter;
 	private YourTurnClock yourTimeRemainingCounter;
 	private RelativeLayout panicScreen;
 	private Handler h;
 	private static QuickAction equipmentQuickAction;
 	private static QuickAction actionAction;
 	private static QuickAction locationAction;
 
 	private static Button actions;
 	private static Button location;
 
 	public static int team_position;
 	public static int game_position;
 	protected PowerManager.WakeLock mWakeLock;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.game_status);
 
 		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
 		this.mWakeLock.acquire();
 
 		getSupportActionBar().hide();
 		h = new Handler();
 
 		panicScreen = (RelativeLayout) findViewById(R.id.panic_screen);
 		showProgressDialog("Loading game..");
 
 		game_position = getIntent().getIntExtra(Util.GAME_POSITION, -1);
 		team_position = getIntent().getIntExtra(Util.TEAM_POSITION, -1);
 
 		if (game_position > -1 && team_position >= 0) {
 			app.current_game = app.teams.get(team_position).getGames().get(game_position);
 		}
 
 		actions = (Button) findViewById(R.id.btn_actions);
 		location = (Button) findViewById(R.id.btn_location);
 		doneBtn = (Button) findViewById(R.id.done_btn);
 		backToMap = (Button) findViewById(R.id.back_to_map_btn);
 
 		initSwitchView();
 		initTextViews();
 
 		InputManager.getInstance(this).addListener(this);
 		InputManager.getInstance(this).addEventListener(this);
 		SetLocationCreator.getInstance(this).addMapListener(this);
 
 		Action action = new JoineGame();
 		action.addParam("name", app.me.getName());
 		action.addParam("role", app.me.getRole());
 		action.addParam("game_name", app.teams.get(team_position).getGames().get(game_position).getName());
 		actionsLeft = app.me.actionsLeft;
 		movementLeft = app.me.movementsLeft;
 		
 		ImageView player_image = (ImageView)findViewById(R.id.player_image);
 		player_image.setBackgroundResource(getLayoutIdFromName(app.me.getRole(), "drawable"));
 		
 		app.client.write(action.getAction());
 
 		initEquipmentAction();
 		initActionQuickAction();
 		initLocationQuickAction();
 
 		h.postDelayed(new Runnable() {
 
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				Intent i = new Intent(GameActivity.this, ShowDebreaf.class);
 				String debrief = app.teams.get(team_position).getGames().get(game_position).getDebreaf();
 
 				if (debrief != null && !debrief.isEmpty()) {
 					i.putExtra("debrief", debrief);
 					startActivity(i);
 				}
 			}
 		}, 400);
 	}
 
 	private void initEquipmentAction() {
 		if (equipmentQuickAction != null) {
 			equipmentQuickAction.dropActions();
 			equipmentQuickAction.setOnActionItemClickListener(null);
 			equipmentQuickAction.removeListener();
 		}
 
 		equipmentQuickAction = new QuickAction(this, QuickAction.VERTICAL);
 
 		for (Equipment item : app.me.getEquipments()) {
 			equipmentQuickAction.addActionItem(item.getActionItem());
 		}
 
 		equipmentQuickAction.setOnActionItemClickListener(new OnActionItemClickListener() {
 			@Override
 			public void onItemClick(com.panicgame.popupwindow.QuickAction source, int pos, int actionId) {
 				Log.i("Equipment", "Pressed at index: " + pos);
 				if (!yourturn)
 					return;
 				if (switcher.getCurrentView().getId() == R.id.layout_map_table)
 					return;
 
 				Sector sector = app.map.getSector(app.me.getId());
 				Equipment equipment = app.me.getEquipments().get(pos);
 
 				Action fixEventAction = new FixEvent();
 				fixEventAction.addParam("sector_id", String.valueOf(sector.getId()));
 
 				boolean fixed = false;
 
 				String message = equipment.getMessage();
 				int duration = equipment.getDuration();
 
 				switch (actionId) {
 
 				case Util.ID_EXTINGUISHER:
 					if (equipment.canFixEvent(sector.getEvent())) {
 						sector.setEvent("none");
 						fixed = true;
 						app.me.getEquipments().remove(equipment);
 					}
 					break;
 				case Util.ID_HAMMER:
 					if (equipment.canFixEvent(sector.getEvent())) {
 						sector.setEvent("none");
 						fixed = true;
 					}
 					break;
 				}
 
 				if (fixed) {
 					ProgressObject progress = new ProgressObject(fixEventAction, duration, message, sector);
 					new startProgress().execute(progress);
 
 				}
 
 			}
 		});
 	}
 
 	private void startAnProgress(String message, int millis, final Sector sector) {
 		showProgressDialog(message);
 		h.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				stopProgressDialog();
 				MapCreator.getInstance(GameActivity.this).createRoom(sector);
 			}
 		}, millis);
 	}
 
 	private void initActionQuickAction() {
 		/*
 		if (actionAction != null) {
 			actionAction.dropActions();
 			actionAction.setOnActionItemClickListener(null);
 			actionAction.removeListener();
 		}
 		*/
 		
 		actionAction = new QuickAction(this, QuickAction.VERTICAL);
 		final ActionItem dropCivils = new ActionItem(Util.ID_ACTION_DROP_PEOPLE, "Drop people", getResources().getDrawable(R.drawable.peoples));
 
 		app.me.removeActions();
 		app.me.initActions(GameActivity.this);
 
 		for (PlayerAction action : app.me.getActions())
 			actionAction.addActionItem(action.getActionItem());
 
 		actionAction.setOnActionItemClickListener(new OnActionItemClickListener() {
 			@Override
 			public void onItemClick(com.panicgame.popupwindow.QuickAction source, int pos, int actionId) {
 
 				final ArrayList<Integer> selectedCivilians = app.me.getCheckedCivilians();
 				final Sector sector = app.getSector(app.me.getPostion_id());
 				final PlayerAction player_action = app.me.getActions().get(pos);
 				boolean prepareDrop = false;
 				boolean needProgress = false;
 				String message = "";
 				Action action = null;
 				int duration = 0;
 
 				switch (actionId) {
 				case Util.ID_ACTION_MOVE_PEOPLE:
 
 					if (app.me.getCheckedCivilians().isEmpty())
 						return;
 
 					boolean peopleCanBeMoved = true;
 					for (Civilian c : sector.getCivilians()) {
 						if (app.me.isCivilianChecked(c.getId())) {
 							if (c.getCivilianStatus() == Civilian.BAD) {
 								peopleCanBeMoved = false;
 							}
 						}
 					}
 					if (!peopleCanBeMoved) {
 						String msg = "'Red' civilians cannot be moved.";
 						showDialog("Ops!", msg);
 						return;
 					}
 					
 					actionAction.dropActions();
 					actionAction.addActionItem(dropCivils);
 					
 
 					// Attache civils to player´
 					for (Civilian civ : sector.getCivilians()) {
 						if (app.me.isCivilianChecked(civ.getId())) {
 							app.me.attacheCivil(civ);
 						}
 					}
 					// Remove players from sector
 					for (Civilian c : app.me.getAttachedCivilians())
 						sector.getCivilians().remove(c);
 
					increaseActions();
 					showToast(app.me.getAttachedCivilians().size() + " civilians are following you");
 					MapCreator.getInstance(GameActivity.this).createRoom(sector);
 					break;
 
 				case Util.ID_ACTION_CALM_PEOPLE:
 					if (selectedCivilians.size() == 0)
 						return;
 
 					needProgress = true;
 					message = "Calming down civilian";
 					action = new CalmDownPeople();
 					for (int i = 0; i < selectedCivilians.size(); i++) {
 						duration += player_action.getDuration();
 						action.addParam("civil" + (i + 1), "" + selectedCivilians.get(i));
 					}
 
 					app.me.dropCivilians();
 					break;
 				case Util.ID_ACTION_DROP_PEOPLE:
 					ArrayList<Civilian> attachedCivilians = app.me.getAttachedCivilians();
 					if (attachedCivilians.size() == 0)
 						return;
 
 					Action movePeopleAction = new MovePeople();
 
 					actionAction.dropActions();
 					//app.me.removeActions();
 					
 					//TEST This work okey. No crash yet. One crash on movePeople. Two crash.
 					//app.me.initActions(GameActivity.this);
 					for(PlayerAction a : app.me.getActions()){
 						actionAction.addActionItem(a.getActionItem());
 					}
 					
 					//TEST
 					
 					//app.me.initActions(GameActivity.this); //Old solution. This will crash some times. Not optimal.
 					/*
 					for (PlayerAction pla : app.me.getActions())
 						actionAction.addActionItem(pla.getActionItem());
 					*/
 					// Drop civilian in sector
 					for (int i = 0; i < attachedCivilians.size(); i++) {
 						sector.getCivilians().add(attachedCivilians.get(i));
 						movePeopleAction.addParam("civil" + (i + 1), "" + attachedCivilians.get(i).getId());
 					}
 					if (sector.isSafe())movePeopleAction.addParam("rescued", "true");
 					
 					app.me.dropCivilians();
 					app.client.write(movePeopleAction.getAction());
 
 					increaseActions();
 					MapCreator.getInstance(GameActivity.this).createRoom(sector);
 
 					break;
 				case Util.ID_MEDIC:
 					if (app.me.getCheckedCivilians().isEmpty())
 						return;
 					HealPeople heal_action = (HealPeople) app.me.getActions().get(pos);
 
 					needProgress = true;
 					action = new UseMedic();
 					message = "Healing civilian (" + heal_action.getProcent() + " %)";
 
 					for (int i = 0; i < selectedCivilians.size(); i++) {
 						action.addParam("civil" + (i + 1), "" + selectedCivilians.get(i));
 						action.addParam("heal_value", ""+heal_action.getProcent());
 						duration += player_action.getDuration();
 					}
 
 					break;
 
 				case Util.ID_ACTION_BROADCAST:
 
 					final CharSequence[] items = { "I need help", "Locate team", "Need firefighter!", "Need medic!" };
 
 					AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
 					builder.setTitle("Communication");
 					builder.setIcon(R.drawable.walkie_talkie);
 					builder.setItems(items, new DialogInterface.OnClickListener() {
 
 						String message = "";
 						BroadcastMessage broadcast = null;
 						int duration = 5000;
 
 						public void onClick(DialogInterface dialog, int item) {
 							switch (item) {
 							case 0:
 								message = "Broadcasting: I need help!";
 								broadcast = new BroadcastMessage();
 								broadcast.addParam("type", "" + item);
 								break;
 							case 1:
 								message = "Broadcasting: Exploring team members location.";
 								broadcast = new BroadcastMessage();
 								broadcast.addParam("type", "" + item);
 								broadcast.setType(1);
 								duration = 10000;
 								break;
 							case 2:
 								message = "Broadcasting: Need firefigher!";
 								broadcast = new BroadcastMessage();
 								broadcast.addParam("type", "" + item);
 								break;
 							case 3:
 								message = "Broadcasting: Need Medic!";
 								broadcast = new BroadcastMessage();
 								broadcast.addParam("type", ""+item);
 							}
 							ProgressObject obj = new ProgressObject(broadcast, duration, message, sector);
 							new startProgress().execute(obj);
 						}
 
 					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							dialog.cancel();
 						}
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 
 					break;
 				}
 
 				if (needProgress) {
 					ProgressObject obj = new ProgressObject(action, duration, message, sector);
 					new startProgress().execute(obj);
 				}
 
 			}
 		});
 	}
 
 	public void refreshMap() {
 		MapCreator.getInstance(GameActivity.this).createMap();
 	}
 
 	public void increaseLocation() {
 
 		if (movementLeft == 1) {
 			movementLeft--;
 			location.setText("Location(" + movementLeft + ")");
 			showToast("You have no more moves left");
 			checkDone();
 		} else {
 			if(movementLeft>0)movementLeft--;
 			location.setText("Location(" + movementLeft + ")");
 		}
 	}
 
 	public void increaseActions() {
 		if (actionsLeft == 1) {
 			actionsLeft--;
 			actions.setText("Actions(" + actionsLeft + ")");
 			showToast("No more actions");
 			checkDone();
 		} else {
 			actionsLeft--;
 			actions.setText("Actions(" + actionsLeft + ")");
 
 		}
 	}
 
 	private void checkDone() {
 		if (actionsLeft == 0 && movementLeft == 0) {
 			showToast("You are done");
 			done();
 		}
 	}
 
 	private void initLocationQuickAction() {
 		locationAction = new QuickAction(this, QuickAction.VERTICAL);
 		ActionItem newLocation = new ActionItem(Util.ID_ACTION_MOVE_PEOPLE, "New location", getResources().getDrawable(R.drawable.map));
 		locationAction.addActionItem(newLocation);
 
 		locationAction.setOnActionItemClickListener(new OnActionItemClickListener() {
 			@Override
 			public void onItemClick(com.panicgame.popupwindow.QuickAction source, int pos, int actionId) {
 				if (!yourturn || movementLeft == 0)
 					return;
 
 				SetLocationCreator.getInstance(GameActivity.this).showSetLocationDialog();
 
 			}
 		});
 	}
 
 	private void initTextViews() {
 		panicTimer = (TextView) findViewById(R.id.panic_wave_time);
 		playerName = (TextView) findViewById(R.id.role_name_value);
 		role_name = (TextView) findViewById(R.id.role_role_value);
 		your_turn_clock_value = (TextView) findViewById(R.id.your_turn_clock);
 		playerName.setText(app.me.getName());
 		role_name.setText(app.me.getRole());
 	}
 
 	private void initSwitchView() {
 		movePageToRight = AnimationUtils.loadAnimation(this, R.anim.movepagetoright);
 		movePageLeft = AnimationUtils.loadAnimation(this, R.anim.movepageleft);
 		movePageToLeft = AnimationUtils.loadAnimation(this, R.anim.movepagetoleft);
 		movePageRight = AnimationUtils.loadAnimation(this, R.anim.movepageright);
 		switcher = (ViewFlipper) findViewById(R.id.view_switcher);
 
 	}
 
 	private void reverceSwitchView() {
 		switcher.setInAnimation(movePageRight);
 		switcher.setOutAnimation(movePageToLeft);
 	}
 
 	private void resetSwitchView() {
 		switcher.setInAnimation(movePageLeft);
 		switcher.setOutAnimation(movePageToRight);
 	}
 
 	public void onClick(View view) {
 
 		if (view.getId() == R.id.back_to_map_btn) {
 			resetSwitchView();
 			switcher.showPrevious();
 			backToMap.setVisibility(View.GONE);
 			app.me.checkedCivilians.clear();
 			if (yourturn)
 				doneBtn.setVisibility(View.VISIBLE);
 		} else {
 			if (!yourturn) {
 				showToast("Its not your turn");
 			} else {
 				if (view.getId() == R.id.btn_actions) {
 					actionAction.show(view);
 				} else if (view.getId() == R.id.btn_location) {
 					locationAction.show(view);
 				} else if (view.getId() == R.id.btn_tools) {
 					equipmentQuickAction.show(view);
 				} else if (view.getId() == R.id.done_btn) {
 					endTurn();
 				}
 			}
 		}
 
 	}
 
 	private void done() {
 		if (!yourturn)
 			return;
 
 		if (!app.me.getAttachedCivilians().isEmpty()) {
 			ArrayList<Civilian> attachedCivilians = app.me.getAttachedCivilians();
 			Sector sector = app.map.getSector(app.me.getId());
 
 			for (int i = 0; i < attachedCivilians.size(); i++) {
 				sector.getCivilians().add(attachedCivilians.get(i));
 			}
 		}
 		app.me.dropCivilians();
 		initActionQuickAction();
 
 		yourTimeRemainingCounter.setViewCounterVisible(false);
 		yourTimeRemainingCounter.cancel();
 		app.client.write(new ImDone().getAction());
 		actionsLeft = 3;
 		movementLeft = 2;
 		showDialog("End of turn", "Your time is up.");
 		yourturn = false;
 		doneBtn.setVisibility(View.GONE);
 
 	}
 
 	/**
 	 * Create map from Server map object
 	 */
 
 	private void createMap() {
 		MapCreator.getInstance(this).createMap();
 		MapCreator.getInstance(this).createRoom(app.getSector(app.me.getPostion_id()));
 	}
 
 	private void removeListeners() {
 		InputManager.getInstance(GameActivity.this).removeEventListener(GameActivity.this);
 		InputManager.getInstance(GameActivity.this).removeListener(GameActivity.this);
 		MapCreator.getInstance(GameActivity.this).removeListener(GameActivity.this);
 		MapCreator.getInstance(GameActivity.this).reset();
 		SetLocationCreator.getInstance(GameActivity.this).removeMapListener(GameActivity.this);
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (switcher.getCurrentView().getId() == R.id.map_container) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle(app.current_game.getName());
 			builder.setMessage("Are you sure you want to exit?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 
 					yourturn = false;
 
 					removeListeners();
 
 					app.client.write(new PlayerLeave().getAction());
 
 					if (yourTimeRemainingCounter != null)
 						yourTimeRemainingCounter.cancel();
 					if (panicTimeCounter != null)
 						panicTimeCounter.cancel();
 
 					finish();
 				}
 			}).setNegativeButton("No", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					dialog.cancel();
 				}
 			});
 			builder.create().show();
 		} else {
 			resetSwitchView();
 			switcher.showPrevious();
 			backToMap.setVisibility(View.GONE);
 			app.me.checkedCivilians.clear();
 		}
 	}
 
 	public void endTurn() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("End turn");
 		builder.setMessage("Are you sure you want to end your turn ?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.cancel();
 				done();
 			}
 		}).setNegativeButton("No", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();
 
 			}
 		});
 		builder.create().show();
 	}
 
 	@Override
 	public void connected() {
 	}
 
 	@Override
 	public void playerJoined(Player player, Team team, Game game) {
 		showToast(player.getName() + " joined");
 
 	}
 
 	@Override
 	public void playerLeft(Player player) {
 		showToast(player.getName() + " left game");
 
 	}
 
 	@Override
 	public void disconnected() {
 		showToast("Error. Disconnected from server. Restart game.");
 	}
 
 	@Override
 	public void teamsReceived() {
 	}
 
 	@Override
 	public void mapUpdate() {
 		Log.i("GameActivity", "mapUpdate called");
 		h.post(new Runnable() {
 			@Override
 			public void run() {
 				createMap();
 				stopProgressDialog();
 			}
 		});
 	}
 
 	// countdowntimer is an abstract class, so extend it and fill in methods
 	private class PanicWaveCounter extends CountDownTimer {
 		public PanicWaveCounter(long millisInFuture, long countDownInterval) {
 			super(millisInFuture, countDownInterval);
 		}
 
 		@Override
 		public void onFinish() {
 			panicTimer.setText("00:00");
 		}
 
 		@Override
 		public void onTick(long millisUntilFinished) {
 			int seconds = (int) (millisUntilFinished / 1000) % 60;
 			int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
 			panicTimer.setText((minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds));
 			// millisUntilFinished/1000);
 		}
 	}
 
 	// countdowntimer is an abstract class, so extend it and fill in methods
 	private class YourTurnClock extends CountDownTimer {
 		LinearLayout time_remaining;
 		ProgressBar progressBar;
 
 		public YourTurnClock(long millisInFuture, long countDownInterval) {
 			super(millisInFuture, countDownInterval);
 			time_remaining = (LinearLayout) findViewById(R.id.remaining_time_layout);
 			progressBar = (ProgressBar) findViewById(R.id.progressBar1);
 			progressBar.setMax((int) millisInFuture / 1000);
 		}
 
 		public void setViewCounterVisible(final boolean visible) {
 			h.post(new Runnable() {
 				@Override
 				public void run() {
 					if (visible)
 						time_remaining.setVisibility(View.VISIBLE);
 					else
 						time_remaining.setVisibility(View.GONE);
 				}
 			});
 		}
 
 		@Override
 		public void onFinish() {
 			your_turn_clock_value.setText("00");
 			time_remaining.setVisibility(View.GONE);
 			done();
 		}
 
 		@Override
 		public void onTick(long millisUntilFinished) {
 			int seconds = (int) (millisUntilFinished / 1000) % 60;
 			your_turn_clock_value.setText("" + (seconds < 10 ? "0" + seconds : seconds));
 			progressBar.setProgress(seconds);
 		}
 
 	}
 
 	@Override
 	public void yourTurn(final int roundtime) {
 		yourturn = true;
 		showToast("Your turn!");
 		vibrate(300);
 		h.post(new Runnable() {
 			@Override
 			public void run() {
 				doneBtn.setVisibility(View.VISIBLE);
 				if (yourTimeRemainingCounter != null)
 					yourTimeRemainingCounter.cancel();
 				yourTimeRemainingCounter = new YourTurnClock(roundtime, 1000);
 				yourTimeRemainingCounter.setViewCounterVisible(true);
 				yourTimeRemainingCounter.start();
 				actions.setText("Actions(" + actionsLeft + ")");
 				location.setText("Location(" + movementLeft + ")");
 
 			}
 		});
 	}
 
 	@Override
 	public void gameStarted(final int panicInterval) {
 		showToast("Game started!");
 		h.post(new Runnable() {
 			@Override
 			public void run() {
 				createMap();
 				panicTimeCounter = new PanicWaveCounter(panicInterval, 1000);
 				panicTimeCounter.start();
 			}
 		});
 
 	}
 
 	@Override
 	public void panicIncreased(final int newinterval) {
 		h.post(new Runnable() {
 			@Override
 			public void run() {
 				panicScreen.setVisibility(View.VISIBLE);
 				createMap();
 				panicTimeCounter = new PanicWaveCounter(newinterval, 1000);
 				panicTimeCounter.start();
 			}
 		});
 		h.postDelayed(new Runnable() {
 
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				panicScreen.setVisibility(View.INVISIBLE);
 			}
 		}, 5000);
 
 	}
 
 	@Override
 	public void sectorClicked(int viewId) {
 
 		Sector s = app.getSector(viewId);
 		if (s.getId() != app.me.getPostion_id()) {
 			showToast("You are not at this location.");
 			return;
 		}
 		MapCreator.getInstance(this).createRoom(s);
 		reverceSwitchView();
 		switcher.showNext();
 
 		doneBtn.setVisibility(View.VISIBLE);
 		backToMap.setVisibility(View.VISIBLE);
 
 	}
 
 	@Override
 	public void checkedInAtSector(int sectorId) {
 		final Sector s = app.getSector(sectorId);
 
 		h.postDelayed(new Runnable() {
 
 			@Override
 			public void run() {
 				if (switcher.getCurrentView().getId() == R.id.map_container) {
 					MapCreator.getInstance(GameActivity.this).createRoom(s);
 					reverceSwitchView();
 					switcher.showNext();
 					backToMap.setVisibility(View.VISIBLE);
 
 				} else {
 					resetSwitchView();
 					switcher.showPrevious();
 					backToMap.setVisibility(View.GONE);
 				}
 			}
 		}, 100);
 
 		Action action = new SetPosition();
 		action.addParam("to_pos", "" + s.getId());
 
 		app.me.exploredSectors.add(sectorId);
 		// IS THIS THE REASON THE APPLICATION FAILED?
 		//app.map.getSector(app.me.getId()).removePlayer(app.me.getId());
 		app.getSector(app.me.getPostion_id()).removePlayer(app.me.getId());
 		//Set new position id
 		app.me.setPostion_id(sectorId);
 		
 		// app.getSector(app.me.getPostion_id()).removePlayer(app.me.getId());
 		
 		s.addPlayer(app.me);
 
 		MapCreator.getInstance(GameActivity.this).createMap();
 		app.client.write(action.getAction());
 
 		increaseLocation();
 
 		showToast("You are now at " + s.getName());
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		try {
 			removeListeners();
 			Action exitAction = new PlayerLeave();
 			app.client.write(exitAction.getAction());
 			this.mWakeLock.release();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void stopGame() {
 		yourturn = false;
 		if (yourTimeRemainingCounter != null)
 			yourTimeRemainingCounter.cancel();
 		if (panicTimeCounter != null)
 			panicTimeCounter.cancel();
 		removeListeners();
 		app.teams.get(team_position).getGames().remove(game_position);
 
 	}
 
 	@Override
 	public void gameOver(String score) {
 		stopGame();
 		showToast("Game is over");
 		Intent i = new Intent(this, GameOver.class);
 		i.putExtra("score", score);
 		startActivityForResult(i, 1);
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 
 		this.finish();
 	}
 
 	private class startProgress extends AsyncTask<ProgressObject, Integer, ProgressObject> {
 		ProgressDialog dialog;
 		ProgressObject obj;
 
 		@Override
 		protected ProgressObject doInBackground(ProgressObject... params) {
 
 			obj = params[0];
 			dialog.setTitle(obj.getMessage());
 			int i = 0;
 			while (i <= 100) {
 				try {
 					Thread.sleep(obj.getDuration() / 100);
 					publishProgress(i);
 					i++;
 				} catch (Exception e) {
 				}
 			}
 
 			return obj;
 		}
 
 		@Override
 		protected void onPostExecute(ProgressObject obj) {
 			dialog.cancel();
 			boolean send = true;
 
 			if (obj.getAction() instanceof CalmDownPeople) {
 				increaseActions();
 			} else if (obj.getAction() instanceof FixEvent) {
 				initEquipmentAction();
 			} else if (obj.getAction() instanceof BroadcastMessage) {
 
 				if (((BroadcastMessage) obj.getAction()).getType() == 1) {
 					send = false;
 					for (ArrayList<Sector> sector_list : app.map.getSectors()) {
 						for (Sector s : sector_list) {
 							for (Player p : s.getPlayers()) {
 								if (!p.getId().equals(app.me.getId())) {
 									app.me.exploredSectors.add(s.getId());
 									break;
 								}
 							}
 						}
 					}
 				}
 				increaseActions();
 			} else if (obj.getAction() instanceof UseMedic) {
 				app.me.dropCivilians();
 				increaseActions();
 			}
 
 			MapCreator.getInstance(GameActivity.this).createRoom(obj.getSector());
 			refreshMap();
 
 			if (send)
 				app.client.write(obj.getAction().getAction());
 
 		}
 
 		@Override
 		protected void onPreExecute() {
 			dialog = new ProgressDialog(GameActivity.this);
 			dialog.setTitle("Processing");
 			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			dialog.show();
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			dialog.setProgress(values[0]);
 		}
 	}
 
 	@Override
 	public void equipmentPickedUp(int equipmentId) {
 		Equipment equipment = null;
 		String message = "You picked up";
 		String type = "";
 		switch (equipmentId) {
 		case Util.ID_EXTINGUISHER:
 			type = " an extinguisher";
 			message += type;
 			equipment = new Extinguisher(10000, GameActivity.this);
 			break;
 		case Util.ID_HAMMER:
 			equipment = new Hammer(10000, GameActivity.this);
 			type = " a hammer";
 			message += type;
 			break;
 		}
 		if (app.me.hasEquipment(equipmentId)) {
 			showToast("You allready have " + type);
 			return;
 		}
 		Sector s = app.map.getSector(app.me.getId());
 		s.removeEquipment(equipmentId);
 
 		app.me.getEquipments().add(equipment);
 
 		initEquipmentAction();
 
 		Action equipmentPickedEquipment = new PickedEquipment();
 		equipmentPickedEquipment.addParam("equipment_type", "" + equipmentId);
 		equipmentPickedEquipment.addParam("sector_id", "" + app.map.getSector(app.me.getId()).getId());
 
 		app.client.write(equipmentPickedEquipment.getAction());
 
 		createMap();
 		showDialog("Equipment", message);
 
 	}
 
 	@Override
 	public void broadcastMessageReceived(String title, String message) {
 		showDialog(title, message);
 
 	}
 
 	@Override
 	public void negotiationStarted(final int timer) {
 		h.post(new Runnable() {
 			@Override
 			public void run() {
 				int seconds = timer/1000;
 				Toast.makeText(GameActivity.this, "The game will start within "+seconds+" seconds!", Toast.LENGTH_LONG).show();
 			}
 		});
 		
 	}
 
 }
