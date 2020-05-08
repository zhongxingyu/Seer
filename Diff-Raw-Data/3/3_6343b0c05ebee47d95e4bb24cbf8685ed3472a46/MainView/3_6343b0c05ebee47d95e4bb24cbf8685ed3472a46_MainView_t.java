 // Spell precedence:
 //
 //   Dispel Magic
 //   Counter-spell, Magic Mirror
 //   Summon (cannot cast on future monsters), including Elementals.
 //   Counter-spell, Magic Mirror on fresh monsters.
 //   Shield, Protection From Evil
 //   Enchantments
 //   Remove Enchantment
 //   Storms
 //   Damaging spells, attacks
 //   Cures, Raise Dead
 //
 // This follows Andrew Plotkin's implementation in that Invisibility/Blindness
 // spells still causes targeting to fail in the same turn they are removed
 // by Disenchant.
 //
 // Fire/ice effects can be complex. Some examples:
 //
 // 1. Fire Elemental, Resist Heat, Fireball, Ice Storm:
 //   The Fire Elemental and Ice Storm destroy each other, even though Resist
 //   Heat alone is enough to kill the Fire Elemental. Since the Ice Storm did
 //   not prevail, the Fireball damages its target.
 //
 // 2. Fire Elemental, Ice Elemental, Ice Storm, Fireball:
 //   Elementals and Storm cancel, thus Fireball does damage.
 //
 // 3. Ice Elemental with 1 remaining hit point, Stab or non-fire damage spell:
 //   The Ice Elemental still attacks despite being destroyed this turn.
 //
 // 4. Ice Elemental, Resist Fire/Fire Storm/Fireball
 //   The Ice Elemental is destroyed and does not attack.
 //
 // Elementals replace any existing elementals, but will be hit by spells
 // targeted at the old and new incarnations.
 //
 // Invisibility is no defence against Elementals, though Shield is.
 //
 // Numbering of targets is relative: 0 is us, 1 is them, -1 is nobody,
 // -2 our left summoning circle, -3 our right summoning circle
 // -4 their left summoning circle, -5 their right summoning circle
 // 2, 4, 6, ... our monsters, in order summoned
 // 3, 5, 7, ... their monsters, in order summoned
 
 package com.benlynn.spelltapper;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.TextView;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 
 import android.graphics.Typeface;
 import android.media.MediaPlayer;
 import android.text.SpannableString;
 import android.text.Spanned;
 import android.text.style.StyleSpan;
 
 import com.benlynn.spelltapper.SpellTap.Wisdom;
 import com.benlynn.spelltapper.Being.Status;
 
 public class MainView extends View {
   static float x0, y0, x1, y1;
   static Tutorial tut;
   static int main_state;
 
   // If 0 or 1, represents left or right spell icon, otherwise represents
   // controlled monster.
   static int drag_i;
   static boolean okstate;
 
   static boolean is_animating;
 
   static final int STATE_VOID = -1;
   static final int STATE_NORMAL = 0;
   // Special states for tutorials.
   static final int STATE_GESTURE_TEACH = 1;
   static final int STATE_TARGET_TEACH = 2;
   // TODO: Use freeze_gesture flag instead of checking for the following state?
   static final int STATE_ON_CONFIRM = 3;
   static final int STATE_ON_END_ROUND = 4;
 
   static int choice[];  // Gesture choice.
   static int lastchoice[];
   static History hist, opphist, history[];
   static final int ylower = 128 + 144 + 4 * 4;
   static final int yspellrow = ylower + 24;
   static final int ystatus = yspellrow + 2 * 50;
   static final int yicon = 64 + 48 + 2 * 4;
   static String spell_text[];
   static boolean spell_is_twohanded;
   static int ready_spell_count[];
   static Spell[][] ready_spell;
   static Spell[] spell_list;
   static int spell_list_count;
   static int spell_choice[];
   static int spell_target[];
   static int fut_choice[];  // Orders for future/charmed/raised monsters.
 
   // Holds these orders after confirmation, and also holds the opponent's
   // choices.
   static int fut_confirm[][];
 
   static int winner;
   static MonsterAttack monatt[];
 
   static Board board;
   static ArrowView arrow_view;
   void set_board(Board i_board) {
     board = i_board;
     board.bmsumcirc = get_bitmap(R.drawable.summoncircle);
   }
   void set_arrow_view(ArrowView a) {
     arrow_view = a;
   }
 
   void run() { tut.run(); }
   void go_back() {
     // TODO: Confirm player wants to leave.
     agent.disconnect();
     winner = 1;
     if (!is_animating) {
       help_arrow_off();
       spelltap.goto_town();
       spelltap.narrate(R.string.chicken);
     }
   }
 
   static boolean is_simplified() {
     return !has_circles;
   }
   static boolean has_circles;
 
   void set_state_knifetutorial() {
     tut = new KnifeTutorial();
   }
   void set_state_palmtutorial() {
     tut = new PalmTutorial();
   }
   void set_state_win_to_advance(Agent a) {
     tut = new WinToAdvance(a);
   }
   void set_state_exhibition_match(Agent a) {
     tut = new ExhibitionMatch(a);
   }
   void set_state_practicemode(int hp) {
     dummyhp = hp;
     tut = new PracticeMode();
   }
   void set_state_missilelesson() {
     tut = new SDTutorial();
   }
   void set_state_wfplesson() {
     tut = new WFPTutorial();
   }
   static void set_tutorial(int i) {
     tut_index = i;
     tut = machines[i];
     tut.set_state(0);
   }
 
   static SpellTapMove oppturn;
   static class SpellTapMove {
     SpellTapMove() {
       gest = new int[2];
       spell = new int[2];
       spell_target = new int[2];
       attack_source = new int[16];
       attack_target = new int[16];
     }
     int gest[];
     int spell[];
     int spell_target[];
     int attack_count;
     int attack_source[];
     int attack_target[];
   }
 
   void tip_off() {
     spelltap.tip_off();
   }
   void jack_tip(int string_constant) {
     spelltap.jack_tip(string_constant);
   }
   void jack_says(int string_constant) {
     spelltap.jack_says(string_constant);
   }
   void set_main_state(int new_state) {
     main_state = new_state;
     if (STATE_TARGET_TEACH == main_state) is_confirmable = false;
     else if (STATE_ON_CONFIRM == main_state) is_confirmable = true;
   }
 
   void new_game_solo() {
     Being.list[0].start_life(Player.life[Player.level]);
     clear_choices();
     board.setVisibility(View.GONE);
     arrow_view.setVisibility(View.GONE);
   }
 
   void new_game(Agent a) {
     Being.list[0].start_life(Player.life[Player.level]);
     init_opponent(a);
     reset_game();
     board.setVisibility(View.VISIBLE);
     arrow_view.setVisibility(View.VISIBLE);
   }
 
   abstract class Tutorial {
     abstract void run();
     void set_state(int i) {}
   }
   static void set_gesture_knowledge(int level) {
     for (int i = 0; i < 9; i++) {
       Gesture g = Gesture.list[i];
       if (null != g) g.learned = false;
     }
     // Exploit fall-through.
     switch(level) {
       case Wisdom.ALL_GESTURES:
         Gesture.list[Gesture.CLAP].learned = true;
       case Wisdom.ALL_BUT_C:
         Gesture.list[Gesture.FINGERS].learned = true;
       case Wisdom.ALL_BUT_FC:
         Gesture.list[Gesture.WAVE].learned = true;
       case Wisdom.DKPS:
         Gesture.list[Gesture.DIGIT].learned = true;
       case Wisdom.KPS:
         Gesture.list[Gesture.SNAP].learned = true;
       case Wisdom.KNIFE_AND_PALM:
         Gesture.list[Gesture.PALM].learned = true;
       case Wisdom.KNIFE_ONLY:
         Gesture.list[Gesture.KNIFE].learned = true;
       case Wisdom.NONE:
     }
   }
 
   static void learn(Spell sp) {
     sp.learned = true;
   }
 
   static void set_spell_knowledge(int level) {
     if (Wisdom.ALL_LEVEL_0 < level) {
       level -= Wisdom.ALL_LEVEL_0;
       for (int i = 0; i < spell_list_count; i++) {
 	Spell sp = spell_list[i];
 	sp.learned = level >= sp.level ? true : false;
       }
       return;
     }
 
     for (int i = 0; i < spell_list_count; i++) spell_list[i].learned = false;
     // Exploits fall-through.
     switch(level) {
       case Wisdom.UP_TO_DFW:
         learn(spellAtGesture("DFW"));
         learn(spellAtGesture("SFW"));
       case Wisdom.UP_TO_DSF:
         learn(spellAtGesture("DSF"));
       case Wisdom.UP_TO_WFP:
         learn(spellAtGesture("WFP"));
       case Wisdom.UP_TO_MISSILE:
         learn(spellAtGesture("SD"));
       case Wisdom.STABNSHIELD:
         learn(spellAtGesture("P"));
       case Wisdom.STAB:
         learn(stab_spell);
     }
   }
 
   // To pass this tutorial, the player merely has to drag their finger up three
   // times, starting from the lower part of the screen.
   class KnifeTutorial extends Tutorial {
     KnifeTutorial() {
       state = 0;
     }
     void run() {
       for(;;) switch(state) {
 	case 0:
 	  set_gesture_knowledge(Wisdom.KNIFE_ONLY);
 	  set_spell_knowledge(Wisdom.STAB);
 	  new_game_solo();
 	  jack_says(R.string.welcome);
 	  state = 100;
 	  return;
 	case 100:
 	  jack_says(R.string.howtoknife);
 	  state = 1;
 	  is_confirmable = false;
 	  return;
 	case 1:
 	  arr_x0 = 75;
 	  arr_y0 = ystatus - 8;
 	  arr_x1 = 75;
 	  arr_y1 = ylower + 32;
 	  help_arrow_on();
 	  jack_tip(R.string.howtoknife2);
 	  set_main_state(STATE_GESTURE_TEACH);
 	  state = 2;
 	  return;
 	case 2:
 	  if (Gesture.KNIFE == choice[0]) {
 	    help_arrow_off();
 	    arr_x0 = 320 - 75;
 	    arr_y0 = ystatus - 8;
 	    arr_x1 = 320 - 75;
 	    arr_y1 = ylower + 32;
 	    help_arrow_on();
 	    jack_tip(R.string.howtoknifepass1);
 	    state = 3;
 	  } else {
 	    clear_choices();
 	  }
 	  return;
 	case 3:
 	  help_arrow_off();
 	  if (Gesture.KNIFE == choice[1]) {
 	    choice[0] = Gesture.NONE;
 	    handle_new_choice(0);
 	    jack_says(R.string.howtoknifepass2);
 	    state = 4;
 	    return;
 	  } else {
 	    clear_choices();
 	  }
 	  return;
 	case 4:
 	  arr_x0 = 75;
 	  arr_y0 = ystatus - 8;
 	  arr_x1 = 75;
 	  arr_y1 = ylower + 32;
 	  help_arrow_on();
 	  jack_tip(R.string.howtoknifeonemore);
 	  state = 5;
 	  return;
 	case 5:
 	  if (choice[0] == Gesture.KNIFE) {
 	    choice[1] = Gesture.NONE;
 	    handle_new_choice(1);
 	    help_arrow_off();
 	    jack_says(R.string.howtoknifepass3);
 	    state = 6;
 	  }
 	  return;
 	case 6:
 	  set_main_state(STATE_NORMAL);
 	  spelltap.next_state();
 	  spelltap.goto_town();
 	  return;
       }
     }
     int state;
   }
 
   // Defeat a pacifist wooden dummy with 3 hitpoints to pass this one.
   // Actually, as long as the battle ends, the player passes. The only way
   // to lose is to stab yourself, which requires a player who knows what
   // they're doing.
   class DummyTutorial extends Tutorial {
     DummyTutorial() {
       state = 0;
     }
     void run() {
       for(;;) switch(state) {
       case 0:
         new_game(Agent.getDummy());
 	Being.list[1].start_life(3);
 	jack_says(R.string.dummytut);
 	state = 1;
         return;
       case 1:
 	jack_tip(R.string.dummytut1);
 	set_main_state(STATE_GESTURE_TEACH);
 	state = 100;
 	return;
       case 100:
 	if (Gesture.KNIFE == choice[0] || Gesture.KNIFE == choice[1]) {
 	  jack_tip(R.string.dummytut2);
 	  set_main_state(STATE_ON_CONFIRM);
 	  state = 101;
 	}
 	return;
       case 101:
 	tip_off();
 	set_main_state(STATE_ON_END_ROUND);
 	state = 102;
 	return;
       case 102:
 	jack_says(R.string.dummytut3);
 	state = 2;
 	return;
       case 2:
 	state = 3;
         return;
       case 3:
 	switch(winner) {
 	case 0:
 	  jack_says(R.string.dummytutwin);
 	  tut = new TargetTutorial();
 	  break;
 	case 1:
 	  jack_says(R.string.dummytutlose);
 	  state = 123;
 	  break;
 	case 2:
 	  jack_says(R.string.dummytutdraw);
 	  state = 123;
 	  break;
 	}
         return;
       case 123:
 	jack_says(R.string.dummytutskip);
 	state = 124;
 	return;
       case 124:
 	spelltap.next_state();
 	spelltap.goto_town();
         return;
       }
     }
     int state;
   }
 
   // Two goblins and a dummy. Since the dummy is the default target, the player
   // is must retarget their stabs if they are to win.
   class TargetTutorial extends Tutorial {
     TargetTutorial() {
       state = 0;
     }
     void run() {
       for(;;) switch(state) {
       case 0:
         new_game(Agent.getDummy());
 	Being.list[1].start_life(3);
 	// Two goblins.
 	Being.list[2] = new Being("Porsap", bmgoblin, 1);
 	Being.list[2].bitmap_dead = bmcorpse;
 	Being.list[2].start_life(1);
 	Being.list[2].target = 0;
 
 	Being.list[3] = new Being("Dedmeet", bmgoblin, 1);
 	Being.list[3].bitmap_dead = bmcorpse;
 	Being.list[3].start_life(1);
 	Being.list[3].target = 0;
 	Being.list_count = 4;
 
 	jack_tip(R.string.targettut);
 	set_main_state(STATE_GESTURE_TEACH);
 	state = 1;
 	invalidate();
         return;
       case 1:
 	{
 	  int h = -1;
 	  if (Gesture.KNIFE == choice[0]) {
 	    h = 0;
 	    arr_x0 = 24;
 	    spell_target[0] = -1;
 	  }
 	  else if (Gesture.KNIFE == choice[1]) {
 	    h = 1;
 	    arr_x0 = 320 - 24;
 	    spell_target[1] = -1;
 	  }
 	  if (-1 == h) return;
 	  tip_off();
 	  arr_y0 = MainView.yicon + 24;
 	  Being b = Being.list[2 + h];
 	  arr_x1 = b.x + b.midw;
 	  arr_y1 = b.y + b.midh;
 	  help_arrow_on();
 	  jack_says(R.string.targettut1);
 	  state = 2;
 	}
 	return;
       case 2:
 	set_main_state(STATE_TARGET_TEACH);
 	state = 3;
 	return;
       case 3:
 	help_arrow_off();
 	jack_tip(R.string.targettut2);
 	set_main_state(STATE_ON_CONFIRM);
 	state = 4;
 	return;
       case 4:
 	tip_off();
 	set_main_state(STATE_ON_END_ROUND);
 	state = 5;
         return;
       case 5:
 	jack_says(R.string.targettut3);
 	set_main_state(STATE_NORMAL);
 	state = 6;
         return;
       case 6:
 	switch(winner) {
 	case 0:
 	  jack_says(R.string.targettutwin);
 	  state = 7;
 	  break;
 	case 1:
 	  jack_says(R.string.targettutlose);
 	  state = 0;
 	  break;
 	case 2:
 	  jack_says(R.string.targettutlose);
 	  state = 0;
 	  break;
 	}
 	Being.reset();
         return;
       case 7:
 	spelltap.next_state();
 	spelltap.goto_town();
 	return;
       }
     }
     int state;
   }
 
   // The Palm version of the Knife tutorial
   // times, starting from the lower part of the screen.
   class PalmTutorial extends Tutorial {
     PalmTutorial() {
       state = 0;
     }
     void run() {
       for(;;) switch(state) {
 	case 0:
 	  set_gesture_knowledge(Wisdom.KNIFE_AND_PALM);
 	  set_spell_knowledge(Wisdom.STABNSHIELD);
 	  new_game_solo();
 	  jack_says(R.string.palmtut);
 	  state = 1;
 	  return;
 	case 1:
 	  arr_x0 = 75;
 	  arr_y0 = ylower + 32;
 	  arr_x1 = 75;
 	  arr_y1 = ystatus - 8;
 	  help_arrow_on();
 	  jack_tip(R.string.palmtut1);
 	  set_main_state(STATE_GESTURE_TEACH);
 	  state = 2;
 	  return;
 	case 2:
 	  if (Gesture.PALM == choice[0]) {
 	    help_arrow_off();
 	    arr_x0 = 320 - 75;
 	    arr_y0 = ylower + 32;
 	    arr_x1 = 320 - 75;
 	    arr_y1 = ystatus - 8;
 	    help_arrow_on();
 	    jack_tip(R.string.palmtutpass1);
 	    state = 3;
 	  } else {
 	    clear_choices();
 	  }
 	  return;
 	case 3:
 	  if (Gesture.PALM == choice[1]) {
 	    help_arrow_off();
 	    choice[0] = Gesture.NONE;
 	    handle_new_choice(0);
 	    jack_tip(R.string.palmtutpass2);
 	    state = 4;
 	  } else {
 	    clear_choices();
 	  }
 	  return;
 	case 4:
 	  if (Gesture.PALM == choice[0]) {
 	    choice[1] = Gesture.NONE;
 	    handle_new_choice(1);
 	    jack_says(R.string.palmtutpass3);
 	    state = 5;
 	  } else {
 	    clear_choices();
 	  }
 	  return;
 	case 5:
 	  jack_says(R.string.palmtutpass4);
 	  state = 6;
 	  return;
 	case 6:
 	  jack_says(R.string.palmtutpass5);
 	  state = 7;
 	  return;
 	case 7:
 	  set_main_state(STATE_NORMAL);
 	  spelltap.next_state();
 	  spelltap.goto_town();
 	  return;
       }
     }
     int state;
   }
 
   static int dummyhp;
   // Practice Mode. A defenceless dummy.
   class PracticeMode extends Tutorial {
     PracticeMode() {
       state = 0;
     }
     void run() {
       for(;;) switch(state) {
 	case 0:
 	  new_game(Agent.getDummy());
 	  Being.list[1].start_life(dummyhp);
 	  state = 1;
 	  return;
 	case 1:
 	  state = 0;
 	  spelltap.goto_town();
 	  return;
       }
     }
     int state;
   }
 
   // Beat given opponent to advance main game state.
   class WinToAdvance extends Tutorial {
     WinToAdvance(Agent a) {
       state = 0;
       agent = a;
     }
     void run() {
       for(;;) switch(state) {
       case 0:
 	new_game(agent);
 	state = 1;
 	return;
       case 1:
 	if (winner == 0) spelltap.next_state();
         spelltap.goto_town();
 	return;
       }
     }
     int state;
     Agent agent;
   }
 
   // Exhibition Match.
   class ExhibitionMatch extends Tutorial {
     ExhibitionMatch(Agent a) {
       state = 0;
       agent = a;
     }
     void run() {
       for(;;) switch(state) {
       case 0:
 	new_game(agent);
 	state = 1;
 	return;
       case 1:
         spelltap.goto_town();
 	state = 0;
 	return;
       }
     }
     int state;
     Agent agent;
   }
 
   // Introduce SD.
   class SDTutorial extends Tutorial {
     SDTutorial() {
       state = 0;
       hand = 0;
     }
     void run() {
       for(;;) switch(state) {
 	case 0:
 	  set_gesture_knowledge(Wisdom.KPS);
 	  new_game(Agent.getDummy());
 	  Being.list[1].start_life(5);
 	  jack_says(R.string.SDtut0);
 	  state = 1;
 	  return;
 	case 1:
 	  jack_says(R.string.SDtut1);
 	  state = 2;
 	  return;
 	case 2:
 	  arr_x1 = 0;
 	  arr_y0 = ystatus - 8;
 	  arr_y1 = ylower + 32;
 	  arr_x0 = arr_x1 - (arr_y1 - arr_y0);
 	  help_arrow_on_symmetric();
 	  jack_tip(R.string.SDtut2);
 	  set_main_state(STATE_GESTURE_TEACH);
 	  state = 200;
 	  return;
 	case 200:
 	  if (Gesture.SNAP == choice[0] && Gesture.SNAP == choice[1]) {
 	    help_arrow_off();
 	    jack_tip(R.string.SDtut3);
 	    set_main_state(STATE_ON_CONFIRM);
 	    state = 3;
 	  }
 	  return;
 	case 3:
 	  jack_says(R.string.SDtutpass1);
 	  state = 300;
 	  return;
 	case 300:
 	  arr_x0 = 0;
 	  arr_y0 = ystatus - 8;
 	  arr_y1 = ylower + 32;
 	  arr_x1 = arr_x0 - (arr_y1 - arr_y0);
 	  help_arrow_on_symmetric();
 	  jack_tip(R.string.SDtutpass100);
 	  set_spell_knowledge(Wisdom.UP_TO_MISSILE);
 	  set_gesture_knowledge(Wisdom.DKPS);
 	  set_main_state(STATE_GESTURE_TEACH);
 	  state = 4;
 	  return;
 	case 4:
 	  if (Gesture.DIGIT == choice[0] && Gesture.DIGIT == choice[1]) {
 	    help_arrow_off();
 	    jack_tip(R.string.SDtut3);
 	    set_main_state(STATE_ON_CONFIRM);
 	    state = 5;
 	  }
 	  return;
 	case 5:
 	  tip_off();
 	  set_main_state(STATE_ON_END_ROUND);
 	  state = 6;
 	  return;
 	case 6:
 	  jack_says(R.string.SDtutpass2);
 	  state = 7;
 	  return;
 	case 7:
 	  jack_says(R.string.SDtutpass3);
 	  state = 8;
 	  return;
 	case 8:
 	  jack_says(R.string.SDtutpass4);
 	  state = 9;
 	  return;
 	case 9:
 	  jack_says(R.string.SDtutpass5);
 	  state = 10;
 	  return;
 	case 10:
 	  set_main_state(STATE_NORMAL);
 	  state = 11;
 	  return;
 	case 11:
 	  spelltap.next_state();
 	  spelltap.goto_town();
 	  return;
       }
     }
     int hand;
     int state;
   }
 
   // Introduces WFP.
   class WFPTutorial extends Tutorial {
     WFPTutorial() {
       state = 0;
       hand = 0;
     }
     void run() {
       for(;;) switch(state) {
 	case 0:
 	  set_gesture_knowledge(Wisdom.ALL_BUT_FC);
 	  new_game(Agent.getDummy());
 	  Being.list[1].start_life(4);
 	  jack_says(R.string.wavetut);
 	  state = 1;
 	  return;
 	case 1:
 	  jack_tip(R.string.wavetut1);
 	  arr_x1 = 0;
 	  arr_y0 = ylower + 32;
 	  arr_y1 = ystatus - 8;
 	  arr_x0 = arr_x1 + (arr_y1 - arr_y0);
 	  help_arrow_on_symmetric();
 	  set_main_state(STATE_GESTURE_TEACH);
 	  state = 2;
 	  return;
 	case 2:
 	  if (Gesture.WAVE == choice[0] && Gesture.WAVE == choice[1]) {
 	    help_arrow_off();
 	    jack_tip(R.string.wavetut2);
 	    set_main_state(STATE_ON_CONFIRM);
 	    state = 3;
 	  }
 	  return;
 	case 3:
 	  jack_tip(R.string.fingerstut);
 	  arr_x0 = 0;
 	  arr_y0 = ylower + 32;
 	  arr_y1 = ystatus - 8;
 	  arr_x1 = arr_x0 + (arr_y1 - arr_y0);
 	  help_arrow_on_symmetric();
 	  set_gesture_knowledge(Wisdom.ALL_BUT_C);
 	  set_main_state(STATE_GESTURE_TEACH);
 	  state = 4;
 	  return;
 	case 4:
 	  if (Gesture.FINGERS == choice[0] && Gesture.FINGERS == choice[1]) {
 	    help_arrow_off();
 	    jack_tip(R.string.wavetut2);
 	    set_main_state(STATE_ON_CONFIRM);
 	    set_spell_knowledge(Wisdom.UP_TO_WFP);
 	    state = 5;
 	  }
 	  return;
 	case 5:
 	  jack_tip(R.string.fingerstut2);
 	  set_main_state(STATE_GESTURE_TEACH);
 	  state = 6;
 	  return;
 	case 6:
 	  if (Gesture.PALM == choice[0] && Gesture.PALM == choice[1]) {
 	    jack_tip(R.string.fingerstut3);
 	    set_main_state(STATE_ON_CONFIRM);
 	    state = 7;
 	  }
 	  return;
 	case 7:
 	  tip_off();
 	  set_main_state(STATE_NORMAL);
 	  state = 8;
 	  return;
 	case 8:
 	  jack_says(R.string.fingerstutpass2);
 	  state = 9;
 	  return;
 	case 9:
 	  jack_says(R.string.fingerstutpass3);
 	  state = 10;
 	  return;
 	case 10:
 	  spelltap.next_state();
 	  spelltap.goto_town();
 	  return;
       }
     }
     int hand;
     int state;
   }
 
   static int indexOfSpell(String name) {
     for(int i = 0; i < spell_list_count; i++) {
       if (name == spell_list[i].name) return i;
     }
     return -1;
   }
 
   static int indexOfSpellGesture(String gesture) {
     for(int i = 0; i < spell_list_count; i++) {
       if (gesture == spell_list[i].gesture) return i;
     }
     return -1;
   }
 
   static Spell spellAtName(String name) {
     for(int i = 0; i < spell_list_count; i++) {
       if (name == spell_list[i].name) return spell_list[i];
     }
     return null;
   }
 
   static Spell spellAtGesture(String combo) {
     for(int i = 0; i < spell_list_count; i++) {
       if (combo == spell_list[i].gesture) return spell_list[i];
     }
     return null;
   }
 
   private char encode_target(int target) {
     if (target <= 1) {  // Player, opponent, thin air, or fresh monster.
       return (char) ('H' + target);
     } else {
       return (char) ('H' + 2 + Being.list[target].id);
     }
   }
 
   private int decode_target(char c) {
     int raw = c - 'H';
     // Just as "me" and "you" are relative to the speaker, so are 0 and 1,
     // -2 and -4, -3 and -5, and the odd monster IDs and even monster IDs.
     switch(raw) {
       case -5:
 	return -3;
       case -4:
 	return -2;
       case -3:
 	return -5;
       case -2:
 	return -4;
       case -1:
 	return -1;
       case 0:
         return 1;
       case 1:
         return 0;
     }
     raw -= 2;
     // Switch odd monster IDs to even IDs and vice versa.
     if (0 == (raw & 1)) raw++;
     else raw--;
     for (int i = 2; i < Being.list_count; i++) {
       if (raw == Being.list[i].id) return i;
     }
     return -1;
   }
 
   // Encode an integer (like the index of a spell) to a single character
   // suitable for a URL request parameter.
   char url_encode(int i) {
     if (i < 0) Log.e("MainView", "Bug! Cannot encode negative integers.");
     if (i < 26) return (char) ('A' + i);
     if (i < 52) return (char) ('a' + i - 26);
     if (i < 62) return (char) ('0' + i - 52);
     Log.e("MainView", "Bug! Too big to encode.");
     return '!';
   }
 
   int url_decode(char c) {
     if (c == '@') return -1;
     if (c >= 'A' && c <= 'Z') return c - 'A';
     if (c >= 'a' && c <= 'z') return 26 + c - 'a';
     if (c >= '0' && c <= '9') return 52 + c - '0';
     Log.e("MainView", "Bug! Don't know how to decode '" + c + "'.");
     return -1;
   }
 
   void net_move(SpellTapMove turn) {
     String s = "";
     // Encode gestures.
     s += (char) (choice[0] + '0');
     s += (char) (choice[1] + '0');
     // Encode spells and targets.
     if (spell_is_twohanded) {
       s += url_encode(ready_spell[spell_choice[0]][2].index);
       s += encode_target(spell_target[0]);
       s += (char) ('A' - 1);
       s += (char) ('A' - 1);
     } else for (int h = 0; h < 2; h++) {
       if (-1 != spell_choice[h]) {
 	s += url_encode(ready_spell[spell_choice[h]][h].index);
 	s += encode_target(spell_target[h]);
       } else {
 	s += (char) ('A' - 1);
 	s += (char) ('A' - 1);
       }
     }
     // Encode monster attacks.
     String s2 = "";
     int count = 0;
     for (int i = 2; i < Being.list_count; i++) {
       Being b = Being.list[i];
       if (0 == b.controller) {
 	s2 += encode_target(i);
 	s2 += encode_target(b.target);
 	count++;
       }
     }
     for (int i = 0; i < 2; i++) {
       if (fut_choice[i] != -1) {
 	s2 += encode_target(-2 - i);
 	s2 += encode_target(fut_choice[i]);
 	count++;
       }
     }
     s += (char) ('0' + count);
     s += s2;
 
     opp_ready = false;
     // Spawn a thread to send move over the network, so we can keep handling
     // user input.
     handler_state = HANDLER_GETMOVE;
     Tubes.send_move(s);
   }
   static int handler_state;
   static final int HANDLER_IDLE = 0;
   static final int HANDLER_GETMOVE = 1;
   static final int HANDLER_DECODE = 2;
   static final int HANDLER_CHARM_CHOSEN = 3;
   static final int HANDLER_GET_CHARM_HAND = 4;
   static final int HANDLER_GET_CHARM_GESTURE = 5;
   static final int HANDLER_SET_PARA = 6;
   static final int HANDLER_GET_PARA = 7;
   static final int HANDLER_NEW_GAME = 8;
   static final int HANDLER_START_GAME = 9;
   static final int HANDLER_FINISH_GAME = 10;
 
   void net_set_para(int target, int hand) {
     opp_ready = false;
     invalidate();
     handler_state = HANDLER_SET_PARA;
     Tubes.send_set_para(target, hand);
   }
 
   void net_get_para(int target) {
     opp_ready = false;
     invalidate();
     handler_state = HANDLER_GET_PARA;
     Tubes.send_get_para(target);
   }
 
   void net_set_charm(int hand, int gesture) {
     opp_ready = false;
     invalidate();
     handler_state = HANDLER_CHARM_CHOSEN;
     Tubes.send_set_charm(hand, gesture);
   }
 
   void net_get_charm_hand() {
     opp_ready = false;
     invalidate();
     handler_state = HANDLER_GET_CHARM_HAND;
     Tubes.send_get_charm_hand();
   }
 
   void net_get_charm_gesture() {
     opp_ready = false;
     invalidate();
     handler_state = HANDLER_GET_CHARM_GESTURE;
     Tubes.send_get_charm_gesture();
   }
 
   static void ready_noise() {
     // TODO: Add option for this feature before turning this on.
     //ready_mp.seekTo(0);
     //ready_mp.start();
   }
 
   static NetHandler net_handler;
   class NetHandler extends Handler {
     @Override
     public void handleMessage(Message msg) {
       // Valid reply received from network.
       try {
 	if (null == Tubes.net_thread) {
 	  // TODO: It crashed here once. Work out why.
 	  Log.e("NetHandler", "Bug! net_thread == null.");
 	} else {
 	  Tubes.net_thread.join();
 	  Tubes.net_thread = null;
 	}
       } catch (InterruptedException e) {
 	Log.e("MainView", "Interrupted exception.");
       }
       //Log.i("Reply", Tubes.reply);
       switch(handler_state) {
 	case HANDLER_NEW_GAME:
 	  Tubes.handle_new_game();
 	  handler_state = HANDLER_START_GAME;
 	  break;
 	case HANDLER_START_GAME:
 	  tip_off();
 	  Player.set_level(Tubes.reply.charAt(0) - '0');
 	  spelltap.netconfig.setVisibility(View.GONE);
 	  set_tutorial(MACHINE_NET);
 	  spelltap.goto_mainframe();
 	  break;
 	case HANDLER_FINISH_GAME:
 	  break;
 	case HANDLER_GETMOVE:
 	  handler_state = HANDLER_DECODE;
 	  Tubes.send_getmove();
 	  break;
 	case HANDLER_DECODE:
 	  if (Tubes.reply.equals("CHICKEN")) {
 	    spelltap.narrate(R.string.winbychicken);
 	    winner = 0;
 	    break;
 	  }
 	  decode_move(oppturn, Tubes.reply);
 	  break;
 	case HANDLER_CHARM_CHOSEN:
 	  opp_ready = true;
 	  net_state = NET_IDLE;
 	  note_charm_chosen();
 	  break;
 	case HANDLER_GET_CHARM_HAND:
 	  switch(Tubes.reply.charAt(0)) {
 	    case '0':
 	      agent.reply_hand = 0;
 	      break;
 	    case '1':
 	      agent.reply_hand = 1;
 	      break;
 	  }
 	  if (-1 == agent.reply_hand) {
 	    Log.e("TODO", "Handle bad messages");
 	  }
 	  opp_ready = true;
 	  ready_noise();
 	  net_state = NET_IDLE;
 	  new_round_post_charm();
 	  break;
 	case HANDLER_GET_CHARM_GESTURE:
 	  {
 	    int g = Tubes.reply.charAt(0) - '0';
 	    if (g >= 0 && g <= 8 && g != Gesture.NONE && Gesture.list[g] != null) {
 	      agent.reply_gesture = g;
 	    } else {
 	      Log.e("TODO", "Handle bad messages");
 	      break;
 	    }
 	    opp_ready = true;
 	    net_state = NET_IDLE;
 	    apply_charm();
 	    break;
 	  }
 	case HANDLER_SET_PARA:
 	  opp_ready = true;
 	  net_state = NET_IDLE;
 	  note_para_chosen();
 	  break;
 	case HANDLER_GET_PARA:
 	  switch(Tubes.reply.charAt(0)) {
 	    case '0':
 	      agent.reply_hand = 0;
 	      break;
 	    case '1':
 	      agent.reply_hand = 1;
 	      break;
 	  }
 	  if (-1 == agent.reply_hand) {
 	    Log.e("TODO", "Handle bad messages");
 	  }
 	  opp_ready = true;
 	  ready_noise();
 	  net_state = NET_IDLE;
 	  set_para_hand();
 	  break;
 	case HANDLER_IDLE:
 	  break;
       }
     }
   }
 
   void decode_move(SpellTapMove turn, String r) {
     if (null == r || r.length() < 7) {
       // TODO: Do something for invalid messages!
       Log.e("TODO", "Invalid server reply.");
       return;
     }
     // TODO: Check message is valid!
     // Decode gestures, spells and spell targets.
     for (int h = 0; h < 2; h++) {
       turn.gest[h] = r.charAt(h) - '0';
       turn.spell[h] = url_decode(r.charAt(2 + 2 * h));
       turn.spell_target[h] = decode_target(r.charAt(3 + 2 * h));
     }
     // Decode monster attacks.
     turn.attack_count = r.charAt(6) - '0';
     for (int n = 0; n < turn.attack_count; n++) {
       turn.attack_source[n] = decode_target(r.charAt(7 + 2 * n));
       turn.attack_target[n] = decode_target(r.charAt(7 + 2 * n + 1));
     }
     opp_ready = true;
     ready_noise();
     net_state = NET_REPLY;
     invalidate();
   }
 
   class NetAgent extends Agent {
     String name() { return "Opponent"; }
     String name_full() { return "Opponent"; }
     int life() { return Player.life[Player.level]; }
     int bitmap_id() { return R.drawable.wiz; }
     void move(SpellTapMove turn) {
       net_move(turn);
     }
     void set_para(int target, int hand) {
       net_set_para(target, hand);
     }
     void get_para(int target) {
       net_get_para(target);
     }
     void set_charm(int hand, int gesture) {
       net_set_charm(hand, gesture);
     }
     void get_charm_hand() {
       net_get_charm_hand();
     }
     void get_charm_gesture() {
       net_get_charm_gesture();
     }
     void disconnect() {
       handler_state = HANDLER_IDLE;
       Tubes.stop_net_thread();
       Tubes.send_disconnect();
     }
   }
 
   class NetDuel extends Tutorial {
     NetDuel() {}
     void set_state(int i) { state = i; }
     void run() {
       for(;;) switch(state) {
 	case 0:
 	  new_game(new NetAgent());
           state = 1;
 	  return;
 	case 1:
 	  handler_state = HANDLER_FINISH_GAME;
 	  Tubes.send_finish();
 	  spelltap.next_state();
 	  spelltap.goto_town();
 	  return;
       }
     }
     int state;
   }
 
   static final String ICE_MAIN_MACHINE = "game-main-machine";
   static void load_bundle(Bundle bun) {
     int i = bun.getInt(ICE_MAIN_MACHINE);
     if (-1 != i) set_tutorial(i);
   }
   static void save_bundle(Bundle bun) {
     bun.putInt(ICE_MAIN_MACHINE, tut_index);
   }
 
   void clear_choices() {
     choice[1] = choice[0] = Gesture.NONE;
     lastchoice[0] = lastchoice[1] = choice[0];
     fut_choice[0] = fut_choice[1] = -1;
     fresh_monster[0][0] = fresh_monster[0][1] = -1;
     fresh_monster[1][0] = fresh_monster[1][1] = -1;
     ready_spell_count[0] = ready_spell_count[1] = 0;
     ready_spell_count[2] = 0;
     spell_choice[0] = spell_choice[1] = -1;
     spell_text[0] = spell_text[1] = "";
     arrow_view.bmspell[0] = arrow_view.bmspell[1] = null;
     is_confirmable = false;
     freeze_gesture = false;
     charmed_hand = -1;
     spell_is_twohanded = false;
   }
 
   void init_opponent(Agent a) {
     Being.list[1] = new Being(a.name(), get_bitmap(a.bitmap_id()), -2);
     Being.list[1].start_life(a.life());
     agent = a;
     a.reset();
   }
 
   // Constructor.
   public MainView(Context context, AttributeSet attrs) {
     super(context, attrs);
     con = context;
     drag_i = -1;
     is_animating = false;
     choice = new int[2];
     lastchoice = new int[2];
     fut_choice = new int[2];
     fut_confirm = new int[2][2];
     fresh_monster = new int[2][2];
     history = new History[2];
     history[0] = hist = new History();
     history[1] = opphist = new History();
     ready_spell_count = new int[3];
     ready_spell = new Spell[5][3];
     spell_choice = new int[2];
     spell_choice[0] = spell_choice[1] = 0;
     spell_text = new String[2];
     spell_text[0] = spell_text[1] = "";
     spell_list = new Spell[64];
     spell_list_count = 0;
     spell_level = 0;
     stab_spell = new StabSpell();
     add_spell(stab_spell, 65);
     spell_level = 1;
     add_spell(new ShieldSpell(), 10);
     add_spell(new MissileSpell(), 64);
     add_spell(new CauseLightWoundsSpell(), 63);
     add_spell(new ConfusionSpell(), 28);
     add_spell(new CureLightWoundsSpell(), 128);
     add_spell(new SummonGoblinSpell(), 6);
     add_spell(new ProtectionSpell(), 11);
     add_spell(new CharmPersonSpell(), 31);
     spell_level = 2;
     add_spell(new AmnesiaSpell(), 29);
     add_spell(new FearSpell(), 30);
     add_spell(new AntiSpellSpell(), 27);
     add_spell(new CounterSpellSpell(), 1);
     add_spell(new CounterSpellAltSpell(), 1);
     add_spell(new RemoveEnchantmentSpell(), 32);
     add_spell(new SummonOgreSpell(), 5);
     add_spell(new LightningSpell(), 61);
     add_spell(new FireballSpell(), 59);
     spell_level = 3;
     add_spell(new SummonTrollSpell(), 4);
     magic_mirror_spell = new MagicMirrorSpell();
     add_spell(magic_mirror_spell, 2);
     add_spell(new ParalysisSpell(), 30);
     add_spell(lightning_clap = new LightningClapSpell(), 61);
     add_spell(dispel_spell = new DispelMagicSpell(), 0);
     add_spell(new CauseHeavyWoundsSpell(), 62);
     add_spell(new CureHeavyWoundsSpell(), 129);
     add_spell(new DiseaseSpell(), 25);
     add_spell(new InvisibilitySpell(), 21);
     spell_level = 4;
     add_spell(new SummonGiantSpell(), 3);
     add_spell(new CharmMonsterSpell(), 31);
     add_spell(new PoisonSpell(), 24);
     add_spell(new ResistHeatSpell(), 23);
     add_spell(new ResistColdSpell(), 22);
     add_spell(new FireStormSpell(), 58);
     add_spell(new IceStormSpell(), 57);
     spell_level = 5;
     add_spell(new FingerOfDeathSpell(), 50);
     raise_dead_spell = new RaiseDeadSpell();
     add_spell(raise_dead_spell, 130);
 
     bmcorpse = get_bitmap(R.drawable.corpse);
     bmgoblin = get_bitmap(R.drawable.goblin);
     bmwizard = get_bitmap(R.drawable.wiz);
 
     Gesture.init();
     Being.init();
 
     Being.list[0] = new Being("Player", bmwizard, -1);
     Being.list[0].start_life(5);
     Being.list_count = 1;
 
     spell_target = new int[2];
     exec_queue = new SpellCast[16];
 
     monster_resolve = new SpellCast(-1, new SentinelSpell(66), -1, -1);
     monatt = new MonsterAttack[5];
     for (int i = 1; i <= 4; i++) {
       monatt[i] = new MonsterAttack(i);
       monatt[i].priority = 66;
     }
 
     fireice_resolve = new SpellCast(-1, new SentinelSpell(60), -1, -1);
 
     comment_i = 0;
     comments = new String[4];
     oppturn = new SpellTapMove();
 
     net_handler = new NetHandler();
     arrow_view = null;
     tilt_state = TILT_AWAIT_UP;
     charmed_hand = -1;
     para_source = new int[2];
     para_target = new int[2];
     para_count = 0;
     choosing_para = false;
     choosing_charm = false;
     is_help_arrow_on = false;
     has_circles = true;
     xsumcirc = new int[2];
     ysumcirc = new int[2];
     xsumcirc[0] = 160 - 32  - 2 * 48 - 2 * 10;
     xsumcirc[1] = 160 + 32 + 48 + 2 * 10;
     ysumcirc[0] = MainView.ylower - 64 - 48 - 4;
     ysumcirc[1] = 64 + 4;
 
     machines = new Tutorial[MACHINE_COUNT];
     machines[MACHINE_DUMMY] = new DummyTutorial();
     machines[MACHINE_NET] = new NetDuel();
     tut_index = -1;
     gesture_help = -1;
     ready_mp = MediaPlayer.create(con, R.raw.ready);
   }
   static String emptyleftmsg;
   static String emptyrightmsg;
 
   static int spell_level;
   public void add_spell(Spell sp, int priority) {
     spell_list[spell_list_count] = sp;
     sp.index = spell_list_count;
     sp.priority = priority;
     sp.level = spell_level;
     spell_list_count++;
   }
 
   @Override
   public void onDraw(Canvas canvas) {
     super.onDraw(canvas);
     int x, y;
 
     // Board class handles avatars and status line.
 
     // Opponent history.
     String s = "";
     Paint pa;
     if (Being.list_count > 1) {
       y = 16;
       for (int i = opphist.start[0]; i < opphist.cur; i++) {
 	int g = opphist.gest[i][0];
 	if (Gesture.HIDDEN == g) {
 	  s += " ?";
 	} else {
 	  s += " " + Gesture.abbr(g);
 	}
       }
       if (Status.PARALYZED == Being.list[1].status &&
 	  0 == Being.list[1].para_hand) pa = Easel.para_text;
       else pa = Easel.history_text;
 
       canvas.drawText(s, 0, y, pa);
       s = "";
       for (int i = opphist.start[1]; i < opphist.cur; i++) {
 	int g = opphist.gest[i][1];
 	if (Gesture.HIDDEN == g) {
 	  s += "? ";
 	} else {
 	  s += Gesture.abbr(g) + " ";
 	}
       }
       if (Status.PARALYZED == Being.list[1].status &&
 	  1 == Being.list[1].para_hand) pa = Easel.para_rtext;
       else pa = Easel.history_rtext;
       canvas.drawText(s, 320, y, pa);
     }
 
     // Player history.
     y = ylower - 2;
     s = "";
     for (int i = hist.start[0]; i < hist.cur; i++) {
       s += " " + Gesture.abbr(hist.gest[i][0]);
     }
     if (Status.PARALYZED == Being.list[0].status &&
         0 == Being.list[0].para_hand) pa = Easel.para_text;
     else pa = Easel.history_text;
     canvas.drawText(s, 0, y, pa);
     s = "";
     for (int i = hist.start[1]; i < hist.cur; i++) {
       s += Gesture.abbr(hist.gest[i][1]) + " ";
     }
     if (Status.PARALYZED == Being.list[0].status &&
         1 == Being.list[0].para_hand) pa = Easel.para_rtext;
     else pa = Easel.history_rtext;
     canvas.drawText(s, 320, y, pa);
 
     // Gesture area.
     y = ylower;
     if (Gesture.PALM == choice[0] && Gesture.PALM == choice[1]) {
       canvas.drawRect(0, y, 320, 480, Easel.dark_red_paint);
     } else {
       canvas.drawRect(0, y, 320, 480, Easel.octarine);
     }
 
     if (STATE_TARGET_TEACH == main_state) {
 	canvas.drawText("Drag from Stab icon to goblin", 160, ystatus + 36, Easel.tap_ctext);
     }
 
     // Bottom line.
     switch(net_state) {
       case NET_WAIT: 
 	canvas.drawRect(0, ystatus, 320, 480, Easel.wait_paint);
 	canvas.drawText("Waiting for opponent...", 160, ystatus + 36, Easel.tap_ctext);
 	break;
       case NET_REPLY:
 	canvas.drawRect(0, ystatus, 320, 480, Easel.reply_paint);
 	canvas.drawText("Opponent has moved. TAP!", 160, ystatus + 36, Easel.white_ctext);
 	break;
       case NET_IDLE:
 	if (is_confirmable) {
 	  if (choosing_charm) {
 	    canvas.drawRect(0, ystatus, 320, 480, Easel.reply_paint);
 	    canvas.drawText("Charm", 160, ystatus + 36, Easel.tap_ctext);
 	  } else if (choosing_para) {
 	    canvas.drawRect(0, ystatus, 320, 480, Easel.reply_paint);
 	    canvas.drawText("Paralyze", 160, ystatus + 36, Easel.tap_ctext);
 	  } else if (Gesture.PALM == choice[0] && Gesture.PALM == choice[1]) {
 	    canvas.drawRect(0, ystatus, 320, 480, Easel.surrender_paint);
 	    canvas.drawText("SURRENDER!", 160, ystatus + 36, Easel.tap_ctext);
 	  } else switch(Being.list[0].status) {
 	    case Status.CHARMED:
 	      canvas.drawRect(0, ystatus, 320, 480, Easel.status_paint);
 	      canvas.drawText(
 		  (freeze_gesture ? "CHARMED! (2)" : "CHARMED! (1)"),
 		  160, ystatus + 36, Easel.tap_ctext);
 	      break;
 	    case Status.CONFUSED:
 	      canvas.drawRect(0, ystatus, 320, 480, Easel.status_paint);
 	      canvas.drawText("CONFUSED!", 160, ystatus + 36, Easel.tap_ctext);
 	      break;
 	    case Status.PARALYZED:
 	      canvas.drawRect(0, ystatus, 320, 480, Easel.status_paint);
 	      canvas.drawText("PARALYZED!", 160, ystatus + 36, Easel.tap_ctext);
 	      break;
 	    case Status.AMNESIA:
 	      canvas.drawRect(0, ystatus, 320, 480, Easel.status_paint);
 	      canvas.drawText("AMNESIA!", 160, ystatus + 36, Easel.tap_ctext);
 	      break;
 	    default:
 	      canvas.drawRect(0, ystatus, 320, 480, Easel.reply_paint);
 	      canvas.drawText("TAP!", 160, ystatus + 36, Easel.tap_ctext);
 	      break;
 	  }
 	}
 	break;
     }
 
     // Gesture and spell text.
     y = yspellrow - 8;
     if (choosing_para) {
       int h;
       for (h = 0; h < 2 && Gesture.NONE == choice[h]; h++);
       if (h < 2) {
 	int g = Gesture.paralyze(
 	    history[para_target[para_i]].last_gesture(h));
 	canvas.drawText("Paralyze: " +
 	    (Gesture.NONE == g ? "(empty)" : Gesture.abbr(g)),
 	    h == 0 ? 0 : 320, y,
 	    h == 0 ? Easel.charm_text : Easel.charm_rtext);
       }
     } else {
       if (0 == charmed_hand) {
 	if (freeze_gesture) {
 	  Gesture g = Gesture.list[choice[0]];
 	  canvas.drawText(g.statusname, 0, y, Easel.charm_text);
 	} else {
 	  canvas.drawText("[Charmed]", 0, y, Easel.charm_text);
 	}
       } else {
 	Gesture g = Gesture.list[choice[0]];
 	if (null == g) {
 	  canvas.drawText(emptyleftmsg, 0, y, Easel.grey_text);
 	} else {
 	  canvas.drawText(g.statusname, 0, y, Easel.white_text);
 	}
       }
       if (1 == charmed_hand) {
 	if (freeze_gesture) {
 	  Gesture g = Gesture.list[choice[1]];
 	  canvas.drawText(g.statusname, 320, y, Easel.charm_rtext);
 	} else {
 	  canvas.drawText("[Charmed]", 320, y, Easel.charm_rtext);
 	}
       } else {
 	Gesture g = Gesture.list[choice[1]];
 	if (null == g) {
 	  canvas.drawText(emptyrightmsg, 320, y, Easel.grey_rtext);
 	} else {
 	  canvas.drawText(g.statusname, 320, y, Easel.white_rtext);
 	}
       }
     }
 
     canvas.drawText(spell_text[0], 0, ystatus + 16, Easel.spell_text);
     canvas.drawText(spell_text[1], 320, ystatus + 16, Easel.spell_rtext);
 
     // Spell choice row 1
     x = 0;
     y = yspellrow;
 
     if (is_animating) {
       y += 25 - 6;
       int i = comment_i;
       do {
 	if (null != comments[i]) {
 	  canvas.drawText(comments[i], x, y, Easel.comment_text);
 	  y += 25;
 	}
 	i++;
 	if (i == 4) i = 0;
       } while (i != comment_i);
     }
 
     if (choosing_charm) {
       canvas.drawText("Charm Person: Choose gesture...",
           x, y + 50, Easel.white_text);
     } else if (choosing_para) {
       canvas.drawText("Paralyze " +
 	  (para_target[para_i] == 0 ? "yourself" : "opponent") +
 	  ": Choose side...", x, y + 50, Easel.white_text);
     }
     for (int h = 0; h < 2; h++) {
       for (int i = 0; i < ready_spell_count[h]; i++) {
 	if (i == 3) {
 	  x = h == 0 ? 0 : 320 - 50;
 	  y += 50;
 	}
 	if (i == spell_choice[h]) {
 	  canvas.drawRect(x, y, x + 50, y + 50, Easel.sel_paint);
 	}
 	canvas.drawBitmap(ready_spell[i][h].bitmap, x + 1, y + 1, Easel.paint);
 	if (h == 0) x += 50;
 	else x -= 50;
       }
       x = 320 - 50;
     }
 
     // Spell choice row 2
     y += 50;
     x = 160 - 50;
     for (int i = 0; i < ready_spell_count[2]; i++) {
       if (i == spell_choice[0]) {
 	canvas.drawRect(x, y, x + 50, y + 50, Easel.sel_paint);
       }
       canvas.drawBitmap(ready_spell[i][2].bitmap, x + 1, y + 1, Easel.paint);
       x += 50;
     }
 
     if (is_help_arrow_on) {
       canvas.drawLine(arr_x0, arr_y0, arr_x2, arr_y2, Easel.arrow_paint);
       if (is_symmetric_help_arrow) {
 	canvas.drawLine(320 - arr_x0, arr_y0,
 	    320 - arr_x2, arr_y2, Easel.arrow_paint);
       }
     }
   }
 
   // Drives animation for helper arrows in tutorials.
   private RefreshHandler anim_handler = new RefreshHandler();
   class RefreshHandler extends Handler {
     @Override
     public void handleMessage(Message msg) {
       MainView.this.updateHelpArrow();
       MainView.this.invalidate();
     }
 
     public void sleep(long delayMillis) {
       this.removeMessages(0);
       if (is_help_arrow_on) {
 	sendEmptyMessageDelayed(0, delayMillis);
       }
     }
   }
   void updateHelpArrow() {
     if (frame <= 16) {
       arr_x2 = arr_x0 + (arr_x1 - arr_x0) * frame / 16;
       arr_y2 = arr_y0 + (arr_y1 - arr_y0) * frame / 16;
     }
     frame++;
     if (frame > 32) frame = 0;
     anim_handler.sleep(64);
   }
   void help_arrow_on() {
     is_symmetric_help_arrow = false;
     is_help_arrow_on = true;
     frame = 1;
     updateHelpArrow();
   }
   void help_arrow_on_symmetric() {
     is_symmetric_help_arrow = true;
     is_help_arrow_on = true;
     frame = 1;
     updateHelpArrow();
   }
   void help_arrow_off() {
     is_help_arrow_on = false;
   }
 
   // Choose spell with one-handed finishing gesture.
   private void choose_spell(int h, int i) {
     if (spell_is_twohanded) {
       spell_is_twohanded = false;
       if (0 == ready_spell_count[1 - h]) {
 	spell_choice[1 - h] = -1;
 	spell_text[1 - h] = "";
 	arrow_view.bmspell[1 - h] = null;
       } else {
 	choose_spell(1 - h, ready_spell_count[1 - h] - 1);
       }
     }
     // Assumes i is a valid choice for hand h.
     if (i == spell_choice[h]) return;
     spell_choice[h] = i;
     Spell sp = ready_spell[i][h];
     spell_target[h] = sp.target;
 
     if (!is_simplified()) {
       if (sp.is_monstrous) {
 	fut_choice[h] = 1;
       } else {
 	fut_choice[h] = -1;
       }
     }
 
     arrow_view.bmspell[h] = sp.bitmap;
     spell_text[h] = sp.name;
     invalidate();
   }
 
   // Choose spell with two-handed finishing gesture.
   private void choose_twohanded_spell(int i) {
     spell_is_twohanded = true;
     if (i == spell_choice[0]) return;
     spell_choice[0] = i;
     spell_choice[1] = i;
     spell_target[0] = ready_spell[i][2].target;
     spell_target[1] = ready_spell[i][2].target;
 
     Spell sp = ready_spell[spell_choice[0]][2];
     arrow_view.bmspell[0] = sp.bitmap;
     arrow_view.bmspell[1] = sp.bitmap;
     spell_text[0] = sp.name;
     spell_text[1] = "(two-handed spell)";
     invalidate();
   }
 
   @Override
   public boolean onTouchEvent(MotionEvent event) {
     switch (event.getAction()) {
       case MotionEvent.ACTION_DOWN:
 	if (NET_REPLY == net_state) {
 	  net_state = NET_IDLE;
 	  resolve();
 	  return false;
 	}
 	if (NET_WAIT == net_state) return false;
 	if (is_animating) return false;
 	x0 = event.getX();
 	y0 = event.getY();
 	if (y0 < ylower) {
 	  if (STATE_GESTURE_TEACH == main_state) {
 	    run();
 	    return false;
 	  }
 	  // Check for spell retargeting drag.
 	  if (y0 >= yicon - SLOP && y0 < yicon + 48 + SLOP) {
 	    if (x0 < 48 + SLOP) {
 	      drag_i = 0;
 	    } else if (x0 >= 320 - 48 - SLOP) {
 	      drag_i = 1;
 	    }
 	  } else {
 	    // Check for monster retargeting drag.
 	    for (int i = 2; i < Being.list_count; i++) {
 	      Being b = Being.list[i];
 	      // TODO: Allow corpse retargeting at Level 5 (for Raise Dead).
 	      if (b.dead || 0 != b.controller) continue;
 	      if (b.contains(x0, y0)) {
 		drag_i = i;
 		break;
 	      }
 	    }
 	    if (-1 == drag_i && !is_simplified()) {
 	      // Check for summon circle retarget.
 	      if (y0 >= ysumcirc[0] && y0 <= ysumcirc[0] + 48) {
 		if (x0 >= xsumcirc[0] && x0 <= xsumcirc[0] + 48) {
 		  drag_i = -2;
 		}
 		else if (x0 >= xsumcirc[1] && x0 <= xsumcirc[1] + 48) {
 		  drag_i = -3;
 		}
 	      }
 	    }
 	  }
 	  if (-1 != drag_i) {
 	    return true;
 	  }
 	  return false;
 	}
 	if (y0 >= ystatus) {
 	  okstate = true;
 	  return true;
 	}
 	return true;
       case MotionEvent.ACTION_MOVE:
         if ((event.getEventTime() - event.getDownTime()) > 256 && y0 >= ylower) {
 	  if (x0 < 160 - BUFFERZONE) {
 	    gesture_help = 0;
 	    arrow_view.invalidate();
 	  } else if (x0 >= 160 + BUFFERZONE) {
 	    gesture_help = 1;
 	    arrow_view.invalidate();
 	  }
 	}
         return true;
       case MotionEvent.ACTION_UP:
 	if (-1 != gesture_help) {
 	  gesture_help = -1;
 	  arrow_view.invalidate();
 	}
 	x1 = event.getX();
 	y1 = event.getY();
 	if (drag_i != -1) {
 	  int target;
 	  for(target = Being.list_count - 1; target >= 0; target--) {
 	    Being b = Being.list[target];
 	    if (b.contains(x1, y1)) break;
 	  }
 	  if (!is_simplified()) {
 	    // Check for summon circle retarget.
 	    if (y1 >= ysumcirc[0] && y1 <= ysumcirc[0] + 48) {
 	      if (x1 >= xsumcirc[0] && x1 <= xsumcirc[0] + 48) {
 		target = -2;
 	      }
 	      else if (x1 >= xsumcirc[1] && x1 <= xsumcirc[1] + 48) {
 		target = -3;
 	      }
 	    }
 	    if (y1 >= ysumcirc[1] && y1 <= ysumcirc[1] + 48) {
 	      if (x1 >= xsumcirc[0] && x1 <= xsumcirc[0] + 48) {
 		target = -4;
 	      }
 	      else if (x1 >= xsumcirc[1] && x1 <= xsumcirc[1] + 48) {
 		target = -5;
 	      }
 	    }
 	  }
 	  if (drag_i < -1) {
 	    // Summon circle retarget.
 	    fut_choice[-2 - drag_i] = target;
 	  } else if (drag_i <= 1) {
 	    // drag_i = 0, 1 means player is targeting spell.
 	    if (target == -1) {
 	      // Doesn't count if still in spell icon.
 	      if (y1 >= yicon - SLOP && y1 < yicon + 48 + SLOP) {
 		if (x1 < 48 + SLOP) {
 		  // In the future I plan to add a meaning for dragging
 		  // one spell icon to the other.
 		} else if (x1 >= 320 - 48 - SLOP) {
 		} else {
 		  spell_target[drag_i] = -1;
 		}
 	      } else {
 		spell_target[drag_i] = -1;
 	      }
 	    } else {
 	      spell_target[drag_i] = target;
 	      if (STATE_TARGET_TEACH == main_state && target > 1) run();
 	    }
 	    if (spell_is_twohanded) {
 	      spell_target[1 - drag_i] = spell_target[drag_i];
 	    }
 	  } else {
 	    Being b = Being.list[drag_i];
 	    if (Status.OK == b.status) {
 	      b.target = target;
 	    }
 	  }
 	  drag_i = -1;
 	  invalidate();
 	  return true;
 	}
 	float dx = x1 - x0;
 	float dy = y1 - y0;
 	if (dx * dx + dy * dy < 32 * 32) {
 	  if (STATE_GESTURE_TEACH == main_state) {
 	    run();
 	    return true;
 	  }
 	  if (okstate && y1 >= ystatus) {
 	    confirm_move();
 	    return true;
 	  }
 	  if (STATE_ON_CONFIRM == main_state) return false;
 	  if (y1 >= yspellrow && y1 < yspellrow + 2 * 50) {
 	    // Could be choosing a ready spell.
 	    int i = 0, h = 0;  // Initial values suppress bogus warnings.
 	    if (y1 > yspellrow + 50) {
 	      // Handle up to two-handed spells.
 	      if (x1 >= 160 - 50 && x1 < 160 + 50) {
 		i = x1 < 160 ? 0 : 1;
 		if (i < ready_spell_count[2]) choose_twohanded_spell(i);
 		return true;
 	      } else {
 		i = 3;
 	      }
 	    }
 	    // Handle one-handed spells.
 	    if (x1 < 3 * 50) {
 	      i += ((int) x1) / 50;
 	      h = 0;
 	    } else if (x1 > 320 - 3 * 50) {
 	      i += (320 - (int) x1) / 50;
 	      h = 1;
 	    }
 	    if (i < ready_spell_count[h]) {
 	      choose_spell(h, i);
 	      return true;
 	    }
 	  }
 	} else if (x0 >= 160 - BUFFERZONE && x0 < 160 + BUFFERZONE) {
 	  return false;
 	} else {
 	  if (STATE_ON_CONFIRM == main_state ||
 	      freeze_gesture ||
 	      TILT_AWAIT_DOWN == tilt_state ||
 	      STATE_TARGET_TEACH == main_state) {
 	    return false;
 	  }
 	  int dirx, diry;
 	  int h;
 	  dirx = dx > 0 ? 1 : -1;
 	  diry = dy > 0 ? 1 : -1;
 	  if (Math.abs(dy) > Math.abs(dx) * 2) {
 	    dirx = 0;
 	  } else if (Math.abs(dx) > Math.abs(dy) * 2) {
 	    diry = 0;
 	  }
 	  if (x0 < 160) {
 	    h = 0;
 	  } else {
 	    h = 1;
 	    dirx *= -1;
 	  }
 	  if (!choosing_para && !choosing_charm) {
 	    if (h == charmed_hand) return true;
 	    if (Status.PARALYZED == Being.list[0].status &&
 		h == Being.list[0].para_hand) return true;
 	  }
 	  choice[h] = Gesture.flattenxy(dirx, diry);
 	  if (null == Gesture.list[choice[h]] || !Gesture.list[choice[h]].learned) {
 	    choice[h] = Gesture.NONE;
 	  }
 	  if (!choosing_para && !choosing_charm) {
 	    if (Status.FEAR == Being.list[0].status &&
 		choice[h] != Gesture.PALM &&
 		choice[h] != Gesture.WAVE &&
 		choice[h] != Gesture.KNIFE) choice[h] = Gesture.NONE;
 	  }
 	  if (choice[h] != lastchoice[h]) {
 	    handle_new_choice(h);
 	  }
 	  if (STATE_GESTURE_TEACH == main_state) run();
 	  return true;
 	}
     }
     return false;
   }
 
   static int tilt_state;
   void tilt_up() {
     if (TILT_AWAIT_UP != tilt_state) return;
     if (STATE_VOID == main_state) return;
     if (STATE_GESTURE_TEACH == main_state) return;
     if (is_confirmable) {
       tilt_state = TILT_AWAIT_DOWN;
       board.set_notify_me(tilt_done_handler);
       board.animate_tilt();
     }
   }
   void tilt_down() {
     if (TILT_AWAIT_DOWN != tilt_state) return;
     confirm_move();
   }
   private TiltDoneHandler tilt_done_handler = new TiltDoneHandler();
   class TiltDoneHandler extends Handler {
     @Override
     public void handleMessage(Message msg) {
       if (TILT_DISABLED != tilt_state) tilt_state = TILT_AWAIT_UP;
     }
   }
 
   class SpellCast {
     SpellCast(int i_hand, Spell i_spell, int i_source, int i_target) {
       hand = i_hand;
       spell = i_spell;
       source = i_source;
       target = i_target;
     }
     void run() {
       spell.execute(source, target);
     }
     int hand;
     Spell spell;
     int target;
     int source;
   }
   static int exec_queue_count;
   static SpellCast[] exec_queue;
 
   // TODO: Use boolean return values instead of opp_ready flag?
   // TODO: Add charm_gesture int in Agent (maybe call it reply_gesture,
   // or reuse reply_hand), so network code in this class can avoid touching
   // choice[].
   static boolean opp_ready;
 
   void wait_on_net() {
     net_state = NET_WAIT;
     invalidate();
   }
 
   private void confirm_move() {
     if (!is_confirmable) return;
     if (STATE_ON_CONFIRM == main_state) run();
     tilt_state = TILT_DISABLED;
     board.animation_reset();  // Stop tilt animation if it's still going.
     if (choosing_para) {
       tilt_state = TILT_AWAIT_UP;
       int h;
       for (h = 0; h < 2 && Gesture.NONE == choice[h]; h++);
       if (h < 2) {
 	Being.list[para_target[para_i]].para_hand = h;
 	opp_ready = true;
 	agent.set_para(para_target[para_i], h);
 	if (opp_ready) {
 	  note_para_chosen();
 	} else  {
 	  // In network games, network code eventually calls
 	  // note_para_chosen().
 	  wait_on_net();
 	}
       }
       return;
     } else if (choosing_charm) {
       // If Charm Person has been cast on opponent, must first send
       // chosen gesture before normal turn.
       tilt_state = TILT_AWAIT_UP;
       int h;
       for (h = 0; h < 2 && Gesture.NONE == choice[h]; h++);
       if (h < 2) {
 	opp_ready = true;
 	agent.set_charm(h, choice[h]);
 	if (opp_ready) {
 	  note_charm_chosen();
 	} else  {
 	  // In network games, network code eventually calls
 	  // note_charm_chosen().
 	  wait_on_net();
 	}
       }
       return;
     }
     if (-1 != charmed_hand) {
       if (!freeze_gesture) {
 	tilt_state = TILT_AWAIT_UP;
 	opp_ready = true;
 	agent.get_charm_gesture();
 	if (opp_ready) {
 	  apply_charm();
 	} else  {
 	  wait_on_net();
 	  // Network code calls apply_charm().
 	}
 	return;
       }
     }
     get_opp_moves();
   }
 
   void apply_charm() {
     choice[charmed_hand] = agent.reply_gesture;
     handle_new_choice(charmed_hand);
     freeze_gesture = true;
   }
 
   void get_opp_moves() {
     hist.add(choice);
 
     opp_ready = true;
     agent.move(oppturn);
     // In local duels, opp_ready should still be true.
     if (opp_ready) {
       resolve();
       return;
     }
     // Otherwise we wait for network code to call resolve().
     wait_on_net();
   }
 
   void note_para_chosen() {
     choosing_para = false;
     clear_choices();
     invalidate();
     new_round_para1();
   }
 
   void note_charm_chosen() {
     choosing_charm = false;
     clear_choices();
     invalidate();
     new_round_charm2();
   }
 
   // Awful hack to increase priorities of Counter-spells and Magic Mirrors
   // cast on freshly summoned monsters so they won't end up being cast on
   // thin air.
   private int adjusted_priority(SpellCast sc) {
     int pr = sc.spell.priority;
     if ((1 == pr || 2 == pr) && sc.target < -1) {
       pr += 8;
     }
     return pr;
   }
 
   // Insert spells by priority.
   private void insert_spell(SpellCast sc) {
     int i;
     int pr = adjusted_priority(sc);
 
     for (i = 0; i < exec_queue_count; i++) {
       if (adjusted_priority(exec_queue[i]) > pr) break;
     }
     for (int j = exec_queue_count; j > i; j--) {
       exec_queue[j] = exec_queue[j - 1];
     }
     exec_queue[i] = sc;
     exec_queue_count++;
   }
 
   private void resolve() {
     is_animating = true;
     for (int i = 0; i < 4; i++) comments[i] = null;
     para_count = 0;
     // TODO: Invisiblility and Blindness should be handled earlier than here,
     // otherwise it's possible to cheat.
     if (Being.list[1].invisibility > 0) {
       opphist.add(Gesture.both_hidden);
     } else {
       opphist.add(oppturn.gest);
     }
 
     global_ice_storm = false;
     global_fire_storm = false;
     fireice_i = 0;
     // Expire status effects.
     for (int i = 0; i < Being.list_count; i++) {
       Being b = Being.list[i];
       b.status = Status.OK;
       b.psych = 0;
       b.is_fireballed = false;
       b.raisedead = false;
     }
 
     exec_queue_count = 0;
     // Insert player spells and targets into execution queue.
     fut_confirm[0][0] = fut_choice[0];
     fut_confirm[0][1] = fut_choice[1];
     if (spell_is_twohanded) {
       if (-1 != spell_choice[0]) {
 	if (lightning_clap == ready_spell[spell_choice[0]][2]) {
 	  if (WDDc_cast) {
 	    Log.e("MV", "Bug! WDDc already cast.");
 	  }
 	  WDDc_cast = true;
 	}
 	SpellCast sc = new SpellCast(
 	    0, ready_spell[spell_choice[0]][2], 0, spell_target[0]);
 	insert_spell(sc);
       }
     } else {
       for (int h = 0; h < 2; h++) {
 	if (-1 == spell_choice[h]) continue;
 	SpellCast sc = new SpellCast(
 	    h, ready_spell[spell_choice[h]][h], 0, spell_target[h]);
 	insert_spell(sc);
       }
     }
     // Insert opponent spells and targets.
     for (int h = 0; h < 2; h++) {
       if (-1 == oppturn.spell[h]) continue;
       Spell sp = spell_list[oppturn.spell[h]];
       SpellCast sc = new SpellCast(h, sp, 1, oppturn.spell_target[h]);
       insert_spell(sc);
     }
     // Retarget monsters controlled by oppponent.
     fut_confirm[1][0] = fut_confirm[1][1] = -1;
     for (int i = 0; i < oppturn.attack_count; i++) {
       int source = oppturn.attack_source[i];
       if (source >= 0) {
 	Being b = Being.list[source];
 	b.target = oppturn.attack_target[i];
       } else if (-4 == source || -5 == source) {
 	fut_confirm[1][-4 - source] = oppturn.attack_target[i];
       } else {
 	Log.e("MV", "Bug! Unknown source!");
       }
     }
     // Insert monster attacks.
     if (is_simplified()) {
       for (int i = 2; i < Being.list_count; i++) {
 	Being b = Being.list[i];
 	if (b.dead) continue;
 	if (-1 != b.target) {
 	  SpellCast sc = new SpellCast(-1, monatt[b.life_max], i, b.target);
 	  insert_spell(sc);
 	}
       }
     } else {
       insert_spell(monster_resolve);
     }
     insert_spell(fireice_resolve);
 
     clear_choices();
     arrow_view.setVisibility(View.GONE);
     invalidate();
 
     exec_cursor = 0;
     mirror_sc = null;
     cur_sc = null;
     if (exec_queue_count == 0) {
       // TODO: Delay?
       end_round();
     } else {
       next_spell();
     }
   }
 
   public void print(String s) {
     comments[comment_i] = s;
     comment_i++;
     if (comment_i == 4) comment_i = 0;
   }
 
   static int exec_cursor;
   static SpellCast mirror_sc, cur_sc;
   int cur_cast_hand() {
     return cur_sc.hand;
   }
   int map_target(int target) {
     if (target < -1) {
       int h = -2 - target;
       int i = 0;
       if (h >= 2) {
 	h -= 2;
 	i++;
       }
       target = fresh_monster[i][h];
     }
     return target;
   }
   public void next_spell() {
     // Magic Mirror interrupts standard order of spells.
     if (null != mirror_sc) {
       cur_sc = mirror_sc;
     } else if (exec_cursor < exec_queue_count) {
       cur_sc = exec_queue[exec_cursor];
     } else {
       end_round();
       return;
     }
 
     // Insert monster attacks at the appropriate moment.
     if (monster_resolve == cur_sc) {
       for (int i = 0; i < 2; i++) for (int h = 0; h < 2; h++) {
 	int j = fresh_monster[i][h];
 	if (-1 != j) {
 	  Being b = Being.list[j];
 	  if (b.controller == 0) {
 	    b.target = fut_confirm[0][h];
 	  } else {
 	    // TODO: Corner case: if player casts Summon spell on opponent,
 	    // then the opponent controls the monster. They may be casting
 	    // their own Summon spell simultaneously, and assigning a target
 	    // for the corresponding monster. Thus both monsters
 	    // attack the assigned target. Should allow different choices
 	    // of targets, perhaps by targeting opponent's Summoning circles.
 	    b.target = fut_confirm[1][h];
 	  }
 	}
       }
       for (int i = 2; i < Being.list_count; i++) {
 	Being b = Being.list[i];
 	b.target = map_target(b.target);
 	if (!b.dead) {
 	  insert_spell(new SpellCast(-1, monatt[b.life_max], i, b.target));
 	}
       }
       exec_cursor++;
       next_spell();
       return;
     }
 
     // Handle fire and ice spells at the appropriate moment
     if (fireice_resolve == cur_sc) {
       if (0 == fireice_i) {
 	// Resolve global spell conflicts.
 	if (global_fire_storm && global_fire_elemental) {
 	  global_fire_elemental = false;
 	  print("The Fire Storm absorbs the Fire Elemental.");
 	}
 	if (global_ice_storm && global_ice_elemental) {
 	  global_ice_elemental = false;
 	  print("The Ice Storm absorbs the Ice Elemental.");
 	}
 	boolean canceled = false;
 	if (global_fire_elemental && global_ice_elemental) {
 	  global_fire_elemental = false;
 	  global_ice_elemental = false;
 	  print("The Fire and Ice Elementals cancel.");
 	  canceled = true;
 	}
 	if (global_fire_elemental && global_ice_storm) {
 	  global_fire_elemental = false;
 	  print("The Fire Elemental and Ice Storm cancel.");
 	  canceled = true;
 	}
 	if (global_ice_elemental && global_fire_storm) {
 	  global_ice_elemental = false;
 	  print("The Ice Elemental and Fire Storm cancel.");
 	  canceled = true;
 	}
 	if (global_fire_storm && global_ice_storm) {
 	  print("The Fire and Ice Storms cancel.");
 	  canceled = true;
 	}
 	if (!global_fire_storm && !global_ice_storm &&
 	    !global_fire_elemental && !global_ice_elemental) {
 	  canceled = true;
 	}
 	if (canceled) {
 	  exec_cursor++;
 	  next_spell();
 	  return;
 	}
       }
       if (fireice_i < Being.list_count) {
 	Being b = Being.list[fireice_i];
 	if (global_ice_storm) {
 	  if (b.is_fireballed ||
 	      b.resist_cold || b.counterspell) {
 	    board.animate_damage(fireice_i, 0);
 	  } else {
 	    b.get_hurt(5);
 	    board.animate_damage(fireice_i, 5);
 	  }
 	} else if (global_fire_storm) {
 	  if (b.resist_heat || b.counterspell) {
 	    board.animate_damage(fireice_i, 0);
 	  } else {
 	    b.get_hurt(5);
 	    board.animate_damage(fireice_i, 5);
 	  }
 	}
 	fireice_i++;
       } else {
 	exec_cursor++;
 	next_spell();
       }
       return;
     }
 
     SpellCast sc = cur_sc;
 
     String s = "";
     String srcname = Being.list[sc.source].name;
     String tgtname = null;
     // If summon spells are targeted at summoning circles, they have no effect.
     if (sc.spell.is_summon) {
       if (sc.target < -1) sc.target = -1;
     } else {
       sc.target = map_target(sc.target);
     }
     int target = sc.target;
     if (target != -1) {
       tgtname = Being.list[target].name;
     }
     if (mirror_sc == sc) {
       s += "Mirror reflects " + sc.spell.name + " at ";
     } else if (sc.source >= 2) {
       s += srcname + " attacks ";
     } else if (sc.spell == stab_spell) {
       if (0 == sc.source) {
 	s += "You stab ";
       } else {
 	s += srcname + " stabs ";
       }
     } else {
       if (0 == sc.source) {
 	s += "You cast ";
       } else {
 	s += srcname + " casts ";
       }
       s += sc.spell.name + " on ";
     }
     if (0 == target) {
       if (0 == sc.source) {
 	s += "yourself.";
       } else {
 	s += "you.";
       }
     } else if (-1 == target) {
       s += "thin air!";
     } else {
       s += tgtname + ".";
     }
     print(s);
     Log.i("MV", s);
     if (sc.spell == dispel_spell) {
       // Dispel Magic always works.
       sc.run();
     } else if (is_dispel_cast && sc.spell.level > 0) {
       print("Dispel Magic negates the spell.");
       sc.spell.just_wait();
     } else if (sc.spell.is_global) {
       // Global spells always work. TODO: Compare with Andrew Plotkin's
       // implementation: what if you cast, say, Fire Storm on a Counter-spelled
       // target?
       sc.run();
     } else if (-1 == target) {
       sc.spell.just_wait();
     } else {
       Being b = Being.list[target];
       if (b.dead && raise_dead_spell != sc.spell) {
 	print("Dead target. Nothing happens.");
 	sc.spell.just_wait();
       } else if (b.invisibility > 0 && sc.target != sc.source) {
 	print("Missed! Target is invisible.");
 	sc.spell.just_wait();
       } else if (b.counterspell && sc.spell.level > 0) {
 	print("Counter-spell blocks the spell.");
 	sc.spell.fizzle(target);
       } else if (b.mirror && sc.source != target && sc.spell.level > 0) {
 	if (sc.spell == magic_mirror_spell) {
 	  sc.run();
 	} else if (null != mirror_sc) {
 	  print("The spell bounces between the mirrors and dissipates."); 
 	  sc.spell.fizzle(target);
 	} else {
 	  mirror_sc = new SpellCast(sc.hand, sc.spell, target, sc.source);
 	  sc.spell.fizzle(target);
 	  // Complicated logic: after creating a new SpellCast representing
 	  // the mirrored spell, we return to avoid incrementing exec_cursor.
 	  return;
 	}
       } else if (sc.spell.is_psych) {
 	// Must compute psychological conflicts on the fly because of
 	// mirrored spells.
 	b.psych++;
 	if (1 < b.psych) {
 	  print("Psychological spell conflict.");
 	  b.status = Status.OK;
 	  sc.spell.fizzle(target);
 	} else {
 	  sc.run();
 	}
       } else {
 	sc.run();
       }
     }
     if (null != mirror_sc) mirror_sc = null;
     exec_cursor++;
   }
 
   // End of round. Check for death, shield expiration, etc.
   // TODO: Move some of this to Being class?
   void end_round() {
     boolean gameover = false;
     for(int i = Being.list_count - 1; i >= 0; i--) {
       Being b = Being.list[i];
       b.counterspell = false;
       b.mirror = false;
       if (b.shield > 0) b.shield--;
       // TODO: Shield off animation.
       if (b.invisibility > 0) b.invisibility--;
       if (b.cast_invisibility) {
 	b.cast_invisibility = false;
 	b.invisibility = 3;
       }
       if (b.disease > 0) {
 	b.disease++;
 	if (b.disease > 6) b.die();
       }
       if (b.poison > 0) {
 	b.poison++;
 	if (b.poison > 6) b.die();
       }
       if (b.life <= 0 || b.doomed) {
 	b.doomed = false;
 	b.die();
       }
       if (b.unsummoned) b.unsummon(i);
     }
     is_dispel_cast = false;
 
     for(int i = 0; i < 2; i++) {
       if (Being.list[i].antispell) {
 	history[i].fast_forward();
 	Being.list[i].antispell = false;
       }
     }
 
     for(int i = Being.list_count - 1; i >= 0; i--) {
       Being b = Being.list[i];
       if (b.raisedead) {
 	if (b.dead) {
 	  b.remove_enchantments();
 	  b.heal_full();
 	} else {
 	  b.heal(5);
 	}
       }
     }
 
     // For player convenience, retarget attacks on dead targets.
     for (int i = 2; i < Being.list_count; i++) {
       Being b = Being.list[i];
       if (Status.OK == b.status && 0 == b.controller && -1 != b.target &&
    	  null != Being.list[b.target] && Being.list[b.target].dead) {
 	b.target = 1;
       }
     }
 
     is_animating = false;
     tilt_state = TILT_AWAIT_UP;
     arrow_view.setVisibility(View.VISIBLE);
     int sid = R.string.bug;
     winner = -1;
     if (Being.list[1].dead) {
       gameover = true;
       winner = 0;
       sid = R.string.win;
       if (Being.list[0].dead) {
 	winner = 2;
 	sid = R.string.draw;
       }
     } else if (Being.list[0].dead) {
       winner = 1;
       gameover = true;
       sid = R.string.lose;
     }
 
     if (!gameover) {
       if (hist.is_doubleP()) {
 	if (opphist.is_doubleP()) {
 	  winner = 2;
 	  gameover = true;
 	  sid = R.string.surrenderdraw;
 	} else {
 	  winner = 1;
 	  gameover = true;
 	  sid = R.string.surrenderlose;
 	}
       } else if (opphist.is_doubleP()) {
 	winner = 0;
 	gameover = true;
 	sid = R.string.surrenderwin;
       }
     }
 
     invalidate();
 
     if (gameover) {
       spelltap.narrate(sid);
       // run() is called once the player has tapped through the
       // victory screen.
     } else {
       if (STATE_ON_END_ROUND == main_state) run();
       new_round();
     }
   }
 
   boolean player_charmed() {
     return Status.CHARMED == Being.list[0].status;
   }
 
   boolean opp_charmed() {
     return Status.CHARMED == Being.list[1].status;
   }
 
   void reset_game() {
     clear_choices();
     hist.reset();
     opphist.reset();
     Being.list_count = 2;
     Being.list[0].remove_enchantments();
     Being.list[1].remove_enchantments();
     Being.list[0].heal_full();
     Being.list[1].heal_full();
     Being.reset();
     set_main_state(STATE_NORMAL);
     net_state = NET_IDLE;
     tilt_state = TILT_AWAIT_UP;
     WDDc_cast = false;
     global_ice_elemental = false;
     global_fire_elemental = false;
   }
 
   // Start new round.
   void new_round() {
     fut_choice[0] = fut_choice[1] = -1;
     // Thus begins the Charm Person spaghetti. The most complex case is
     // when Charm Person has simultaneously been cast on both players. Then:
     //  1. I pick a hand and gesture for my opponent.
     //  2. I ask which hand of mine was chosen by my opponent.
     //  3. I choose a gesture with my other hand. I can also choose
     //  spells and targets and confirm.
     //  4. I ask which gesture the opponent chose for my charmed hand.
     //  5. I finalize spells and targets and confirm.
     // Non-blocking network communication complicates the code further.
     if (opp_charmed()) {
       choosing_charm = true;
       return;
     }
     para_i = -1;
     new_round_charm2();
   }
 
   void new_round_charm2() {
     if (choosing_charm) Log.e("MV", "Bug! Should have chosen charm by now.");
     // Handle charm on player.
     if (player_charmed()) {
       // Get charmed hand from opponent.
       opp_ready = true;
       agent.get_charm_hand();
       if (!opp_ready) {
 	// Network game. Handler will call new_round_post_charm() once
 	// a valid reply is received.
 	wait_on_net();
       } else {
 	new_round_post_charm();
       }
       return;
     } else {
       charmed_hand = -1;
     }
     new_round_para1();
   }
 
   void new_round_post_charm() {
     charmed_hand = agent.reply_hand;
     new_round_para1();
   }
 
   static int para_i;
   void new_round_para1() {
     // Now the paralyze spaghetti. When cast on wizards, the caster
     // chooses the side to be paralyzed if the victim is not already
     // paralyzed. Complications arise because one could cast paralyze on
     // the opponent as well as oneself.
     //
     // Handle player Paralyze spells cast at wizards.
     para_i++;
     if (para_i < para_count) {
       if (0 == para_source[para_i]) {
 	Being b = Being.list[para_target[para_i]];
 	// Psychological conflicts mean that we could have canceled spells
 	// on the para_target and para_source arrays.
 	if (Status.PARALYZED == b.status && -1 == b.para_hand) {
 	  choosing_para = true;
 	  return;
 	}
       }
       new_round_para1();
     } else {
       para_i = -1;
       new_round_para2();
     }
   }
 
   void new_round_para2() {
     if (choosing_para) Log.e("MV", "Bug! Should have chosen para by now.");
     // Handle opponent Paralyze spells cast at wizards.
     para_i++;
     if (para_i < para_count) {
       if (1 == para_source[para_i]) {
 	Being b = Being.list[para_target[para_i]];
 	// Psychological conflicts mean that we could have canceled spells
 	// on the para_target and para_source arrays.
 	if (Status.PARALYZED == b.status && -1 == b.para_hand) {
 	  // Get paralyzed hand from opponent.
 	  opp_ready = true;
 	  agent.get_para(para_target[para_i]);
 	  if (!opp_ready) {
 	    // Network game. Handler will call set_para_hand() once
 	    // a valid reply is received.
 	    wait_on_net();
 	  } else {
 	    set_para_hand();
 	  }
 	  return;
 	}
       }
       new_round_para2();
     } else new_round_post_para();
   }
 
   void set_para_hand() {
     Being.list[para_target[para_i]].para_hand = agent.reply_hand;
     new_round_para2();
   }
 
   void new_round_post_para() {
     if (Status.PARALYZED != Being.list[1].status) Being.list[1].para_hand = -1;
     if (Status.PARALYZED != Being.list[0].status) Being.list[0].para_hand = -1;
     else {
       int h = Being.list[0].para_hand;
       if (-1 == h) Log.e("MV", "Bug! Paralyzed hand not chosen!");
       int lastg = hist.last_gesture(h);
       choice[h] = Gesture.paralyze(lastg);
       spell_search(h);
     }
 
     // Handle amnesia.
     if (Status.AMNESIA == Being.list[0].status) {
       choice[0] = hist.last_gesture(0);
       handle_new_choice(0);
       choice[1] = hist.last_gesture(1);
       handle_new_choice(1);
       freeze_gesture = true;
     }
 
     // Handle confused and paralyzed monsters.
     for (int i = 2; i < Being.list_count; i++) {
       Being b = Being.list[i];
       if (Status.CONFUSED == b.status) {
 	b.target = b.controller;
       } else if (Status.PARALYZED == b.status) {
 	b.target = -1;
       }
     }
     invalidate();
   }
 
   private void handle_new_choice(int h) {
     if (choosing_charm) {
       if (Gesture.NONE != choice[h]) {
 	choice[1 - h] = Gesture.NONE;
 	spell_text[1 - h] = "";
 	spell_text[h] = "(charm opponent)";
 	is_confirmable = true;
       } else if (Gesture.NONE == choice[1 - h]) {
 	spell_text[h] = "";
 	is_confirmable = false;
       }
       invalidate();
       return;
     } else if (choosing_para) {
       if (Gesture.NONE != choice[h]) {
 	choice[1 - h] = Gesture.NONE;
 	spell_text[1 - h] = "";
 	spell_text[h] = "(paralyze)";
 	is_confirmable = true;
       } else if (Gesture.NONE == choice[1 - h]) {
 	spell_text[h] = "";
 	is_confirmable = false;
       }
       invalidate();
       return;
     }
     if (spell_is_twohanded) {
       // Changing one hand invalidates two-handed spells so we must search from
       // scratch.
       spell_search(0);
       spell_search(1);
     } else {
       spell_search(h);
     }
     if (Status.CONFUSED == Being.list[0].status) {
       if (choice[1 - h] != choice[h]) {
 	choice[1 - h] = choice[h];
 	spell_search(1 - h);
       }
     }
     if (-1 != charmed_hand) {
       is_confirmable = Gesture.NONE != choice[1 - charmed_hand];
     } else if (STATE_GESTURE_TEACH == main_state) {
       is_confirmable = false;
     } else if (spelltap.allow_confirm_one) {
       is_confirmable = Gesture.NONE != choice[h] ||
 	  Gesture.NONE != choice[1 - h];
     } else {
       is_confirmable = Gesture.NONE != choice[h] &&
 	  Gesture.NONE != choice[1 - h];
     }
     invalidate();
   }
 
   private void spell_search(int h) {
     spell_is_twohanded = false;
     ready_spell_count[h] = 0;
     spell_choice[h] = -1;
     ready_spell_count[2] = 0;
     if (choice[h] == Gesture.KNIFE) {
       if (choice[1 - h] == Gesture.KNIFE) {
 	spell_text[h] = "(only one knife)";
       } else {
 	add_ready_spell(h, stab_spell);
 	spell_text[h] = "(no spell)";
       }
     } else {
       if (lastchoice[h] == Gesture.KNIFE && choice[1 - h] == Gesture.KNIFE) {
 	ready_spell_count[1 - h] = 0;
 	add_ready_spell(1 - h, stab_spell);
 	choose_spell(1 - h, 0);
       }
       spell_text[h] = "";
       if (choice[h] != Gesture.NONE) {
 	for (int i = 0; i < spell_list_count; i++) {
 	  Spell sp = spell_list[i];
 	  if (!sp.learned) continue;
 	  if (lightning_clap == sp && WDDc_cast) continue;
 	  String g = sp.gesture;
 	  int k = g.length() - 1;
 	  char ch = g.charAt(k);
 	  boolean twohanded = false;
 	  int hand = h;
 	  if (Character.isLowerCase(ch)) {
 	    twohanded = true;
 	    ch = Character.toUpperCase(ch);
 	    if (choice[0] != choice[1] ||
 		ch != Gesture.abbr(choice[0])) continue;
 	  // TODO: Could use the length check for the two-handed case too.
 	  } else if (k > hist.cur - hist.start[h] ||
 	             ch != Gesture.abbr(choice[h])) continue;
 	  // Complicated logic: if the last gesture is two-handed we
 	  // need to search both histories for this spell. Otherwise we
 	  // only search the hand that changed.
 	  for(;;) {
 	    k = g.length() - 2;
 	    int k2 = hist.cur - 1;
 	    while (k >= 0) {
 	      ch = g.charAt(k);
 	      if (Character.isLowerCase(ch)) {
 		ch = Character.toUpperCase(ch);
 		if (k2 < hist.start[0] || k2 < hist.start[1]) break;
 		int old0 = hist.gest[k2][0];
 		int old1 = hist.gest[k2][1];
 		if (old0 != old1 || ch != Gesture.abbr(old0)) break;
 	      } else if (k2 < hist.start[hand] ||
 		  ch != Gesture.abbr(hist.gest[k2][hand])) break;
 	      k2--;
 	      k--;
 	    }
 	    if (0 > k) {
 	      // At last we have a match.
 	      if (twohanded) {
 		add_ready_spell(2, sp);
 	      } else {
 		add_ready_spell(hand, sp);
 	      }
 	    }
 	    if (twohanded) {
 	      if (hand != h) break;
 	      hand = 1 - h;
 	    } else break;
 	  }
 	}
       }
     }
     if (ready_spell_count[h] > 0) {
       choose_spell(h, ready_spell_count[h] - 1);
     } else if (ready_spell_count[2] > 0) {
       choose_twohanded_spell(ready_spell_count[2] - 1);
     } else {
       arrow_view.bmspell[h] = null;
     }
     lastchoice[0] = choice[0];
     lastchoice[1] = choice[1];
   }
 
   public void add_ready_spell(int h, Spell sp) {
     if (h == 2) {
       // Ignore duplicates. This can happen if both hands gesture
       // DSFFFC for example.
       for (int i = 0; i < ready_spell_count[2]; i++) {
 	if (ready_spell[i][2] == sp) return;
       }
     }
     ready_spell[ready_spell_count[h]][h] = sp;
     ready_spell_count[h]++;
   }
 
   static void search_complete(History hi, Agent.SearchResult res,
       int fingest[]) {
     hi.gest[hi.cur][0] = fingest[0];
     hi.gest[hi.cur][1] = fingest[1];
     hi.cur++;
 
     // TODO: Reuse code.
     for (int h = 0; h < 2; h++) {
       res.count[h] = 0;
       for (int i = 0; i < spell_list_count; i++) {
 	Spell sp = spell_list[i];
 	if (!sp.learned) continue;
 	int splen = sp.gesture.length();
 	if (1 == splen) continue;
 	int len = hi.cur - hi.start[h];
 	if (len < splen) continue;
 	int k;
 	for (k = 1; k <= splen; k++) {
 	  if (Gesture.abbr(hi.gest[hi.cur - k][h]) !=
 	      sp.gesture.charAt(splen - k)) break;
 	}
 	if (k > splen) {
 	  res.spell[res.count[h]][h] = sp.gesture;
 	  res.count[h]++;
 	  break;
 	}
       }
     }
     hi.cur--;
   }
 
   static void search_partial(History hi, Agent.SearchResult res) {
     for (int h = 0; h < 2; h++) {
       res.count[h] = 0;
       for (int i = 0; i < spell_list_count; i++) {
 	Spell sp = spell_list[i];
 	if (!sp.learned) continue;
 	int splen = sp.gesture.length();
 	if (1 == splen) continue;
 	int len = hi.cur - hi.start[h];
 	// Look for longest spell match.
 	int progress = len < splen ? len : splen;
 	for(; progress >= 1; progress--) {
 	  int k;
 	  for (k = 1; k <= progress; k++) {
 	    if (Gesture.abbr(hi.gest[hi.cur - k][h]) !=
 		sp.gesture.charAt(progress - k)) break;
 	  }
 	  if (k > progress) {
 	    res.spell[res.count[h]][h] = sp.gesture;
 	    res.progress[res.count[h]][h] = progress;
 	    res.remain[res.count[h]][h] = splen - progress;
 	    res.count[h]++;
 	    break;
 	  }
 	}
       }
     }
   }
 
   static Context con;  // Need this for styling strings.
   abstract public class Spell {
     public void init(String init_name, String init_gest, int bitmapid,
         int descid, int def_target) {
       name = init_name;
       description = descid;
       gesture = init_gest;
       purty = new SpannableString(gesture + " " + name + ": " + con.getText(description) + "\n");
       int n = gesture.length();
       purty.setSpan(new StyleSpan(Typeface.BOLD), 0, n, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
       purty.setSpan(new StyleSpan(Typeface.ITALIC), n + 1, n + name.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
       bitmap = get_bitmap(bitmapid);
       target = def_target;
       learned = false;
       is_psych = false;
       is_global = false;
       is_monstrous = false;
       is_summon = false;
       level = 0;
     }
 
     abstract public void cast(int init_source, int init_target);
     void set_is_psych() { is_psych = true; }
     void set_is_global() { is_global = true; }
     void set_is_monstrous() { is_monstrous = true; }
 
     public void fizzle(int init_target) {
       state = 0;
       is_finished = true;
       board.set_notify_me(done_handler);
       board.animate_spell(init_target, bitmap);
     }
     public void just_wait() {
       state = 0;
       is_finished = true;
       board.set_notify_me(done_handler);
       board.animate_delay();
     }
     public void execute(int init_source, int init_target) {
       state = 0;
       is_finished = false;
       board.set_notify_me(done_handler);
       cast_source = init_source;
       cast_target = init_target;
       cast(cast_source, cast_target);
     }
 
     public void finish_spell() {
       is_finished = true;
       done_handler.sendEmptyMessage(0);
     }
     private DoneHandler done_handler = new DoneHandler();
     class DoneHandler extends Handler {
       @Override
       public void handleMessage(Message msg) {
 	if (is_finished) {
 	  MainView.this.next_spell();
 	} else {
 	  state++;
 	  cast(cast_source, cast_target);
 	}
       }
     }
     int priority;
     int level;
     Bitmap bitmap;
     String name;
     String gesture;
     int index;
     int target;
     int state;
     int description;
     SpannableString purty;
     boolean learned;
     boolean is_psych;
     boolean is_global;
     boolean is_summon;
     boolean is_finished;  // Set this to true before calling last animation.
                           // Or call finish_spell() [it's slower].
     int cast_source, cast_target;
 
     // True if implies another monster will be under player's control on
     // successful casting of the spell.
     boolean is_monstrous;
     // Monstrous spells set this to the monster now under player control.
   }
 
   public class ShieldSpell extends Spell {
     ShieldSpell() {
       init("Shield", "P", R.drawable.shield, R.string.Pdesc, 0);
     }
     public void cast(int source, int target) {
       switch(state) {
         case 0:
           board.animate_shield(target);
           return;
         case 1:
           Being b = Being.list[target];
           if (0 == b.shield) b.shield = 1;
           finish_spell();
           return;
       }
     }
   }
 
   public class CounterSpellSpell extends Spell {
     CounterSpellSpell() {
       init("Counter-spell", "WPP", R.drawable.counter, R.string.WPPdesc, 0);
     }
     public void cast(int source, int target) {
       switch(state) {
         case 0:
           board.animate_shield(target);
           return;
         case 1:
           Being b = Being.list[target];
           if (0 == b.shield) b.shield = 1;
 	  b.counterspell = true;
           finish_spell();
           return;
       }
     }
   }
 
   // Alternate gesture sequence for counter-spell.
   public class CounterSpellAltSpell extends CounterSpellSpell {
     CounterSpellAltSpell() {
       init("Counter-spell", "WWS", R.drawable.counter, R.string.WWSdesc, 0);
     }
   }
 
   public class StabSpell extends Spell {
     StabSpell() {
       init("Stab", "K", R.drawable.stab, R.string.stabdesc, 1);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  board.animate_move(source, target);
 	  return;
 	case 1:
 	  // TODO: Remove duplicated code.
 	  Being b = Being.list[target];
 	  if (0 == b.shield) {
 	    b.get_hurt(1);
 	    board.animate_move_damage(target, 1);
 	  } else {
 	    //Log.i("TODO", "block animation");
 	    board.animate_move_damage(target, 0);
 	  }
 	  return;
 	case 2:
 	  is_finished = true;
 	  board.animate_move_back();
 	  return;
       }
     }
   }
 
   public class MissileSpell extends Spell {
     MissileSpell() {
       init("Missile", "SD", R.drawable.missile, R.string.SDdesc, 1);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  board.animate_bullet(source, target);
 	  return;
 	case 1:
 	  is_finished = true;
 	  Being b = Being.list[target];
 	  if (0 == b.shield) {
 	    b.get_hurt(1);
 	    board.animate_damage(target, 1);
 	  } else {
 	    //Log.i("TODO", "block animation");
 	    board.animate_damage(target, 0);
 	  }
 	  return;
       }
     }
   }
 
   public class FireStormSpell extends Spell {
     FireStormSpell() {
       init("Fire Storm", "SWWc", R.drawable.missile, R.string.SWWcdesc, -1);
       set_is_global();
     }
     public void cast(int source, int target) {
       is_finished = true;
       //Log.i("TODO", "fire storm animation");
       board.animate_delay();
       if (global_fire_storm) print("Fire Storm merges with Fire Storm");
       global_fire_storm = true;
     }
   }
 
   public class IceStormSpell extends Spell {
     IceStormSpell() {
       init("Ice Storm", "WSSc", R.drawable.icestorm, R.string.WSScdesc, -1);
       set_is_global();
     }
     public void cast(int source, int target) {
       is_finished = true;
       //Log.i("TODO", "ice storm animation");
       board.animate_delay();
       if (global_ice_storm) print("Ice Storm merges with Ice Storm");
       global_ice_storm = true;
     }
   }
 
   public class FireballSpell extends Spell {
     FireballSpell() {
       init("Fireball", "FSSDD", R.drawable.fireball, R.string.FSSDDdesc, 1);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  board.animate_bullet(source, target);
 	  return;
 	case 1:
 	  is_finished = true;
 	  Being b = Being.list[target];
 	  b.is_fireballed = true;
 	  if (global_ice_storm) {
 	    //Log.i("TODO", "Ice Storm + Fireball animation");
 	    board.animate_damage(target, 0);
 	  } else if (!b.resist_heat) {
 	    b.get_hurt(5);
 	    board.animate_damage(target, 5);
 	  } else {
 	    //Log.i("TODO", "resist heat animation");
 	    board.animate_damage(target, 0);
 	  }
 	  return;
       }
     }
   }
 
   public class CauseLightWoundsSpell extends Spell {
     CauseLightWoundsSpell() {
       init("Cause Light Wounds", "WFP", R.drawable.wound, R.string.WFPdesc, 1);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  board.animate_spell(target, bitmap);
 	  return;
 	case 1:
 	  is_finished = true;
 	  Being.list[target].get_hurt(2);
 	  board.animate_damage(target, 2);
 	  return;
       }
     }
   }
 
   public class CauseHeavyWoundsSpell extends Spell {
     CauseHeavyWoundsSpell() {
       init("Cause Heavy Wounds", "WPFD", R.drawable.wound, R.string.WPFDdesc, 1);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  board.animate_spell(target, bitmap);
 	  return;
 	case 1:
 	  is_finished = true;
 	  Being.list[target].get_hurt(3);
 	  board.animate_damage(target, 3);
 	  return;
       }
     }
   }
 
   public class LightningSpell extends Spell {
     LightningSpell() {
       init("Lightning", "DFFDD", R.drawable.lightning, R.string.DFFDDdesc, 1);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  board.animate_spell(target, bitmap);
 	  return;
 	case 1:
 	  is_finished = true;
 	  Being.list[target].get_hurt(5);
 	  board.animate_damage(target, 5);
 	  return;
       }
     }
   }
 
   public class LightningClapSpell extends Spell {
     LightningClapSpell() {
       init("Lightning", "WDDc", R.drawable.lightningclap, R.string.WDDcdesc, 1);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  board.animate_spell(target, bitmap);
 	  return;
 	case 1:
 	  is_finished = true;
 	  Being.list[target].get_hurt(5);
 	  board.animate_damage(target, 5);
 	  return;
       }
     }
   }
 
   public class FingerOfDeathSpell extends Spell {
     FingerOfDeathSpell() {
       init("Finger of Death", "PWPFSSSD", R.drawable.fod, R.string.PWPFSSSDdesc, 1);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  is_finished = true;
 	  board.animate_spell(target, bitmap);
 	  Being.list[target].doomed = true;
 	  return;
 	/* TODO: Ominous animation.
 	case 0:
 	  board.animate_spell(target, bitmap);
 	  return;
 	case 1:
 	  is_finished = true;
 	  Being.list[target].doomed = true;
 	  board.animate_damage(target, 2);
 	  return;
 	  */
       }
     }
   }
 
   public class IceElementalSpell extends Spell {
     IceElementalSpell() {
       init("Ice Elemental", "cSWWS", R.drawable.summon1, R.string.cSWWSdesc, 1);
       set_is_global();
     }
     public void cast(int source, int target) {
       is_finished = true;
       board.animate_delay();
       if (global_ice_elemental) print("The Ice Elementals merge into one.");
       global_ice_elemental = true;
     }
   }
 
   public class SummonSpell extends Spell {
     void init_summon(String i_name, String gesture,
 	int bitmapid, int description,
 	int i_alive_bmid, int i_dead_bmid, int i_level) {
       alive_bmid = i_alive_bmid;
       dead_bmid = i_dead_bmid;
       level = i_level;
       name = i_name;
       init("Summon " + name, gesture, bitmapid, description, 0);
       set_is_monstrous();
       is_summon = true;
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  board.animate_spell(target, bitmap);
 	  return;
 	case 1:
 	  is_finished = true;
 	  int k = Being.list[target].controller;
 	  Being b = Being.list[Being.list_count] =
 	      new Being(name, get_bitmap(alive_bmid), k);
 	  b.bitmap_dead = get_bitmap(dead_bmid);
 	  int i = source;
 	  // Corner case: if a summon spell is mirrored, then the original
 	  // caster is the target, not the source.
 	  if (cur_sc == mirror_sc) i = target;
 	  fresh_monster[i][cur_cast_hand()] = Being.list_count;
 	  Being.list_count++;
 	  b.start_life(level);
 	  if (is_simplified()) {
 	    b.target = 1 - k;
 	  }
 	  board.animate_summon(source, cur_cast_hand(), b);
 	  return;
       }
     }
     int level;
     String name;
     int dead_bmid;
     int alive_bmid;
   }
 
   public class SummonGoblinSpell extends SummonSpell {
     SummonGoblinSpell() {
       init_summon("Goblin", "SFW", R.drawable.summon1, R.string.SFWdesc,
 	  R.drawable.goblin, R.drawable.corpse, 1);
     }
   }
 
   public class SummonOgreSpell extends SummonSpell {
     SummonOgreSpell() {
       init_summon("Ogre", "PSFW", R.drawable.summon2, R.string.PSFWdesc,
 	  R.drawable.goblin, R.drawable.corpse, 2);
     }
   }
 
   public class SummonTrollSpell extends SummonSpell {
     SummonTrollSpell() {
       init_summon("Troll", "FPSFW", R.drawable.summon2, R.string.FPSFWdesc,
 	  R.drawable.goblin, R.drawable.corpse, 3);
     }
   }
 
   public class SummonGiantSpell extends SummonSpell {
     SummonGiantSpell() {
       init_summon("Giant", "WFPSFW", R.drawable.summon4, R.string.WFPSFWdesc,
 	  R.drawable.goblin, R.drawable.corpse, 4);
     }
   }
 
   public class CureLightWoundsSpell extends Spell {
     CureLightWoundsSpell() {
       init("Cure Light Wounds", "DFW", R.drawable.curelight, R.string.DFWdesc, 0);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  is_finished = true;
 	  Being.list[target].heal(1);
 	  board.animate_spell(target, bitmap);
 	  return;
       }
     }
   }
 
   public class CureHeavyWoundsSpell extends Spell {
     CureHeavyWoundsSpell() {
       init("Cure Heavy Wounds", "DFPW", R.drawable.cureheavy, R.string.DFPWdesc, 0);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  is_finished = true;
 	  Being b = Being.list[target];
 	  b.heal(2);
 	  b.disease = 0;
 	  board.animate_spell(target, bitmap);
 	  return;
       }
     }
   }
 
   public class ResistColdSpell extends Spell {
     ResistColdSpell() {
       init("Resist Cold", "SSFP", R.drawable.shield, R.string.SSFPdesc, 0);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  is_finished = true;
 	  Being b = Being.list[target];
 	  b.resist_cold = true;
 	  board.animate_spell(target, bitmap);
 	  return;
       }
     }
   }
 
   public class ResistHeatSpell extends Spell {
     ResistHeatSpell() {
       init("Resist Heat", "WWFP", R.drawable.shield, R.string.WWFPdesc, 0);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  is_finished = true;
 	  Being b = Being.list[target];
 	  b.resist_heat = true;
 	  board.animate_spell(target, bitmap);
 	  return;
       }
     }
   }
 
   public class DiseaseSpell extends Spell {
     DiseaseSpell() {
       init("Disease", "DSFFFc", R.drawable.disease, R.string.DSFFFcdesc, 1);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  is_finished = true;
 	  Being b = Being.list[target];
 	  if (0 == b.disease) b.disease = 1;
 	  board.animate_spell(target, bitmap);
 	  return;
       }
     }
   }
 
   public class PoisonSpell extends Spell {
     PoisonSpell() {
       init("Poison", "DWWFWD", R.drawable.poison, R.string.DWWFWDdesc, 1);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  is_finished = true;
 	  Being b = Being.list[target];
 	  if (0 == b.poison) b.poison = 1;
 	  board.animate_spell(target, bitmap);
 	  return;
       }
     }
   }
 
   public class ConfusionSpell extends Spell {
     ConfusionSpell() {
       init("Confusion", "DSF", R.drawable.confusion, R.string.DSFdesc, 1);
       set_is_psych();
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  is_finished = true;
 	  Being.list[target].status = Status.CONFUSED;
 	  board.animate_spell(target, bitmap);
 	  return;
       }
     }
   }
 
   public class CharmPersonSpell extends Spell {
     CharmPersonSpell() {
       init("Charm Person", "PSDF", R.drawable.charm, R.string.PSDFdesc, 1);
       set_is_psych();
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  is_finished = true;
 	  // Only works on opponent.
 	  if (1 - source == target) {
 	    Being.list[target].status = Status.CHARMED;
 	  }
 	  board.animate_spell(target, bitmap);
 	  return;
       }
     }
   }
 
   public class CharmMonsterSpell extends Spell {
     CharmMonsterSpell() {
       init("Charm Monster", "PSDD", R.drawable.charm, R.string.PSDDdesc, 1);
       set_is_psych();
     }
     public void cast(int source, int target) {
 	is_finished = true;
 	// Only works on monsters.
 	if (1 < target) {
 	  Being.list[target].controller = 0;
 	  // The newly controlled monster takes orders from the summoning
 	  // circle.
 	  fresh_monster[0][cur_cast_hand()] = target;
 	}
 	board.animate_spell(target, bitmap);
     }
   }
 
   public class FearSpell extends Spell {
     FearSpell() {
       init("Fear", "SWD", R.drawable.fear, R.string.SWDdesc, 1);
       set_is_psych();
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  is_finished = true;
 	  // Only affects humans.
 	  if (target == 1 || target == 0) {
 	    Being.list[target].status = Status.FEAR;
 	  }
 	  board.animate_spell(target, bitmap);
 	  return;
       }
     }
   }
 
   public class AmnesiaSpell extends Spell {
     AmnesiaSpell() {
       init("Amnesia", "DPP", R.drawable.amnesia, R.string.DPPdesc, 1);
       set_is_psych();
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  is_finished = true;
 	  Being.list[target].status = Status.AMNESIA;
 	  board.animate_spell(target, bitmap);
 	  return;
       }
     }
   }
 
   public class RaiseDeadSpell extends Spell {
     RaiseDeadSpell() {
       init("Raise Dead", "DWWFWc", R.drawable.raise, R.string.DWWFWcdesc, 0);
     }
     public void cast(int source, int target) {
       is_finished = true;
       Being.list[target].raisedead = true;
       board.animate_spell(target, bitmap);
     }
   }
 
   public class ParalysisSpell extends Spell {
     ParalysisSpell() {
       init("Paralysis", "FFF", R.drawable.para, R.string.FFFdesc, 1);
       set_is_psych();
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  is_finished = true;
 	  Being.list[target].status = Status.PARALYZED;
 	  if (0 == target || 1 == target) {
 	    para_source[para_count] = source;
 	    para_target[para_count] = target;
 	    para_count++;
 	  }
 	  board.animate_spell(target, bitmap);
 	  return;
       }
     }
   }
 
   public class AntiSpellSpell extends Spell {
     AntiSpellSpell() {
       init("Anti-spell", "SPFP", R.drawable.antispell, R.string.SPFPdesc, 1);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  is_finished = true;
 	  // Only affects humans.
 	  if (target == 1 || target == 0) {
 	    Being.list[target].antispell = true;
 	  }
 	  board.animate_spell(target, bitmap);
 	  return;
       }
     }
   }
 
   public class RemoveEnchantmentSpell extends Spell {
     RemoveEnchantmentSpell() {
       init("Disenchant", "PDWP", R.drawable.shield, R.string.PDWPdesc, 0);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  Being b = Being.list[target];
 	  b.remove_enchantments();
 	  if (target > 1) {
 	    // Destroy monster.
 	    b.unsummoned = true;
 	  }
 	  is_finished = true;
 	  board.animate_spell(target, bitmap);
 	  return;
       }
     }
   }
 
   public class DispelMagicSpell extends Spell {
     DispelMagicSpell() {
       init("Dispel Magic", "cDPW", R.drawable.protection, R.string.cDPWdesc, 0);
       set_is_global();
     }
     public void cast(int source, int target) {
       is_finished = true;
       if (!is_dispel_cast) {
 	is_dispel_cast = true;
 	for (int i = 0; i < Being.list_count; i++) {
 	  Being b = Being.list[i];
 	  if (i < 2) {
 	    b.remove_enchantments();
 	  } else {
 	    b.unsummoned = true;
 	  }
 	}
       }
       if (target != -1) {
 	Being b = Being.list[target];
 	if (0 == b.invisibility || source == target) {
 	  b.shield = 1;
 	  board.animate_shield(target);
 	} else {
 	  print("Dispel Magic's Shield misses.");
 	}
       } else {
 	board.animate_delay();
       }
       return;
     }
   }
 
   public class MagicMirrorSpell extends Spell {
     MagicMirrorSpell() {
       init("Magic Mirror", "cw", R.drawable.mirror, R.string.cwdesc, 0);
     }
     public void cast(int source, int target) {
       is_finished = true;
       if (Being.list[target].mirror) {
 	print("Magic Mirror merges with existing mirror.");
       }
       Being.list[target].mirror = true;
       board.animate_shield(target);
     }
   }
 
   public class ProtectionSpell extends Spell {
     ProtectionSpell() {
       init("Protection From Evil", "WWP", R.drawable.protection, R.string.WWPdesc, 0);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  board.animate_shield(target);
 	  return;
 	case 1:
 	  Being b = Being.list[target];
 	  b.shield = 4;
 	  finish_spell();
 	  return;
       }
     }
   }
 
   public class InvisibilitySpell extends Spell {
     InvisibilitySpell() {
       init("Invisibility", "PPws", R.drawable.protection, R.string.PPwsdesc, 0);
     }
     public void cast(int source, int target) {
       is_finished = true;
       if (target > 1) {
 	// Instantly destroy monster.
 	Being.list[target].unsummon(target);
       } else {
 	Being.list[target].cast_invisibility = true;
       }
       board.animate_spell(target, bitmap);
     }
   }
 
   public class MonsterAttack extends Spell {
     MonsterAttack(int n) {
       init("", "", R.drawable.goblin, R.string.bug, 1);
       power = n;
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  board.animate_move(source, target);
 	  return;
 	case 1:
 	  Being b = Being.list[target];
 	  if (0 == b.shield) {
 	    b.get_hurt(power);
 	    board.animate_move_damage(target, power);
 	  } else {
 	    //Log.i("TODO", "block animation");
 	    board.animate_move_damage(target, 0);
 	  }
 	  return;
 	case 2:
 	  is_finished = true;
 	  board.animate_move_back();
 	  return;
       }
     }
     int power;
   }
 
   // Placeholder spell so we know when to handle monster attacks,
   // and when to resolve fire and ice spells.
   public class SentinelSpell extends Spell {
     SentinelSpell(int p) {
       init("", "", R.drawable.protection, R.string.bug, 0);
       priority = p;
     }
     public void cast(int source, int target) {
     }
   }
 
   static Bitmap bmcorpse;
 
   Bitmap get_bitmap(int id) {
     return BitmapFactory.decodeResource(getResources(), id);
   }
 
   void set_spelltap(SpellTap i_spelltap) {
     spelltap = i_spelltap;
     stmach = new Kludge(spelltap);
   }
   class Kludge extends SpellTapMachine {
     Kludge(SpellTap st) { super(st); }
     void run() { MainView.this.run(); }
     void go_back() { MainView.this.go_back(); }
   }
   static SpellTapMachine stmach;
 
   static SpellTap spelltap;
   static int charmed_hand;
   static boolean freeze_gesture;
   static boolean choosing_charm;
   static boolean choosing_para;
   static int[] para_source, para_target;
   static int para_count;
   static final int SLOP = 4;
   static final int BUFFERZONE = 32;
   static Agent agent;
   static final int TILT_AWAIT_UP = 0;
   static final int TILT_AWAIT_DOWN = 1;
   static final int TILT_DISABLED = 2;
   static boolean is_confirmable;
   static boolean is_help_arrow_on;
   static boolean is_symmetric_help_arrow;
   static float arr_x0, arr_y0, arr_x1, arr_y1, frame;
   static float arr_x2, arr_y2;
   // For net game turns, net_state goes from IDLE to WAIT while waiting for
   // the opponent's moves, and then REPLY when they are received so we can
   // print a message and wait for a tap before animation. For spells such as
   // charm, the state only goes between IDLE and WAIT.
   static int net_state;
   static final int NET_IDLE = 0;
   static final int NET_WAIT = 1;
   static final int NET_REPLY = 2;
   static boolean is_dispel_cast;
   static StabSpell stab_spell;
   static LightningClapSpell lightning_clap;
   static DispelMagicSpell dispel_spell;
   static MagicMirrorSpell magic_mirror_spell;
   static RaiseDeadSpell raise_dead_spell;
   static String comments[];
   static int comment_i;
   static int xsumcirc[];
   static int ysumcirc[];
   // Index of monster that is the target of a monstrous spell.
   static int fresh_monster[][];
   static Bitmap bmgoblin;
   static Bitmap bmwizard;
 
   static int tut_index;
   static final int MACHINE_DUMMY = 0;
   static final int MACHINE_NET = 1;
   static final int MACHINE_COUNT = 2;
   static Tutorial machines[];
 
   static boolean global_fire_storm;
   static boolean global_ice_storm;
   static boolean global_fire_elemental;
   static boolean global_ice_elemental;
 
   static int fireice_i;
   static SpellCast monster_resolve, fireice_resolve;
   static int gesture_help;
   static boolean WDDc_cast;
 
   static MediaPlayer ready_mp;
 }
