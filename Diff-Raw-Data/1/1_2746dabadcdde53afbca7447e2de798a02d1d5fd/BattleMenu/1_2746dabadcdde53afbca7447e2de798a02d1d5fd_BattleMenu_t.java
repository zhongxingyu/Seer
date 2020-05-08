 package rpisdd.rpgme.activities;
 
 import rpisdd.rpgme.R;
 import rpisdd.rpgme.gamelogic.dungeon.model.Monster;
 import rpisdd.rpgme.gamelogic.dungeon.viewcontrol.BattleSurfaceView;
 import rpisdd.rpgme.gamelogic.player.Player;
 import rpisdd.rpgme.gamelogic.player.StatType;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 
 public class BattleMenu extends Fragment implements OnClickListener {
 
 	Button strengthAtk;
 	Button spiritAtk;
 	Button willAtk;
 	Button intelAtk;
 
 	Button runAway;
 	BattleSurfaceView battleView;
 
 	Monster realMonster;
 	Monster battleMonster;
 
 	public BattleMenu() {
 	}
 
 	public void setMonster(Monster monster) {
 		this.realMonster = monster;
 		this.battleMonster = new Monster(realMonster);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		View v = inflater.inflate(R.layout.battle_menu, container, false);
 
 		battleView = (BattleSurfaceView) v.findViewById(R.id.battleSurfaceView);
 		battleView.battleMenu = this;
 		battleView.setMonster(battleMonster);
 
 		strengthAtk = (Button) v.findViewById(R.id.strengthAtkButton);
 		spiritAtk = (Button) v.findViewById(R.id.spiritAtkButton);
 		willAtk = (Button) v.findViewById(R.id.willAtkButton);
 		intelAtk = (Button) v.findViewById(R.id.intelAtkButton);
 		runAway = (Button) v.findViewById(R.id.runAwayButton);
 
 		strengthAtk.setOnClickListener(this);
 		spiritAtk.setOnClickListener(this);
 		willAtk.setOnClickListener(this);
 		intelAtk.setOnClickListener(this);
 		runAway.setOnClickListener(this);
 
 		return v;
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.strengthAtkButton: {
 			battleView.setPlayerAttack(StatType.STRENGTH);
 			setButtonsEnabled(false);
 			break;
 		}
 		case R.id.spiritAtkButton: {
 			battleView.setPlayerAttack(StatType.SPIRIT);
 			setButtonsEnabled(false);
 			break;
 		}
 		case R.id.willAtkButton: {
 			battleView.setPlayerAttack(StatType.WILL);
 			setButtonsEnabled(false);
 			break;
 		}
 		case R.id.intelAtkButton: {
 			battleView.setPlayerAttack(StatType.INTELLIGENCE);
 			setButtonsEnabled(false);
 			break;
 		}
 		case R.id.runAwayButton: {
 			setButtonsEnabled(false);
 			returnToDungeon(false);
 			break;
 		}
 		default:
 			break;
 		}
 	}
 
 	public void setButtonsEnabled(boolean enable) {
 		// ((MainActivity)getActivity()).blockMenuAccess();
 		strengthAtk.setEnabled(enable);
 		spiritAtk.setEnabled(enable);
 		willAtk.setEnabled(enable);
 		intelAtk.setEnabled(enable);
 		runAway.setEnabled(enable);
 	}
 
 	// Updates the monster in the dungeon to keep damage dealt,
 	// even if player is knocked unconscious or runs
 	public void updateMonsterDamage() {
 		int damageDone = realMonster.getEnergy() - battleMonster.getEnergy();
 		realMonster.RecieveDamage(damageDone);
 
 		if (realMonster.isDead()) {
 			// remove monster from map
 			realMonster.die();
 		}
 	}
 
 	public void returnToDungeon(boolean isVictory) {
 		// update damage even if player runs away
 		updateMonsterDamage();
 
 		((MainActivity) getActivity()).getDrawerToggle()
 				.setDrawerIndicatorEnabled(true);
 
 		if (!isVictory) {
 			System.out.println(Player.getPlayer().getLastRooms());
 			Player.getPlayer().goToLastRoom();
 		}
 
 		TransitionFragment trans = new TransitionFragment();
 		trans.setValues(new DungeonMenu(), true);
 		((MainActivity) getActivity()).changeFragment(trans);
 	}
 
 	public void goToUnconsciousScreen() {
 		// update damage even if player runs away
 		updateMonsterDamage();
 
 		((MainActivity) getActivity()).getDrawerToggle()
 				.setDrawerIndicatorEnabled(true);
 
		Player.getPlayer().goToLastRoom();
 		TransitionFragment trans = new TransitionFragment();
 		trans.setValues(new UnconsciousWarning(), true);
 		((MainActivity) getActivity()).changeFragment(trans);
 	}
 
 	public void redirectToStats() {
 		// update damage even if player died
 		updateMonsterDamage();
 
 		((MainActivity) getActivity()).getDrawerToggle()
 				.setDrawerIndicatorEnabled(true);
 
 		Player.getPlayer().goToLastRoom();
 		TransitionFragment trans = new TransitionFragment();
 		trans.setValues(new StatsMenu(), true);
 		((MainActivity) getActivity()).changeFragment(trans);
 
 	}
 
 }
