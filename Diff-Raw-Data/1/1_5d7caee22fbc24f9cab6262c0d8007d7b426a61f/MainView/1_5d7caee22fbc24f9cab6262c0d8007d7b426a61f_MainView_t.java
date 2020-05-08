 // TODO: Log, character sheet.
 // Turn UI pink on charm, to make it obvious why we need to wait for network?
 // Victory/defeat screen with stats.
 // Psych spell conflict.
 // Resize event.
 // Clean up get_ready() nonsense.
 // Stop handlers on init.
 package com.gmail.benlynn.spelltap;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.TextView;
 import android.os.Handler;
 import android.os.Message;
 
 import android.graphics.Typeface;
 import android.text.SpannableString;
 import android.text.Spanned;
 import android.text.style.StyleSpan;
 
 import com.gmail.benlynn.spelltap.SpellTap.Wisdom;
 
 public class MainView extends View {
   static Gesture[] gesture;
   static String msg;
   static float x0, y0, x1, y1;
   static Tutorial tut;
   static int main_state;
 
   // If 0 or 1, represents left or right spell icon, otherwise represents
   // controlled monster.
   static int drag_i;
 
   static boolean is_animating;
   static BeingPosition being_pos[];
 
   static final int STATE_VOID = -1;
   static final int STATE_NORMAL = 0;
   // Special states for tutorials.
   static final int STATE_GESTURE_ONLY = 1;
   static final int STATE_ON_END_ROUND = 2;
 
   static int choice[];  // Gesture choice.
   static int lastchoice[];
   static History hist, opphist;
   static final int ylower = 128 + 144 + 4 * 4;
   static final int ystatus = ylower + 32 + 2 * 50 + 16 - 4;
   static final int yicon = 64 + 48 + 2 * 4;
   static String spell_text[];
   static int ready_spell_count[];
   static Spell[][] ready_spell;
   static Spell[] spell_list;
   static int spell_list_count;
   static int spell_choice[];
   static int spell_target[];
   static int being_list_count;
   static Being being_list[];
   static int winner;
   static MonsterAttack monatt[];
 
   static Board board;
   static ArrowView arrow_view;
   void set_board(Board a) {
     board = a;
   }
   void set_arrow_view(ArrowView a) {
     arrow_view = a;
   }
 
   void run() { tut.run(); }
 
   void set_state_dummytutorial() {
     tut = new DummyTutorial();
   }
   void set_state_knifetutorial() {
     tut = new KnifeTutorial();
   }
   void set_state_palmtutorial() {
     tut = new PalmTutorial();
   }
   void set_state_win_to_advance(Agent a) {
     tut = new WinToAdvance(a);
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
   void set_state_netduel() {
     tut = new NetDuel();
   }
 
   static SpellTapMove oppmove;
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
 
   void jack_says(int string_constant) {
     spelltap.jack_says(string_constant);
   }
 
   void set_main_state(int new_state) {
     main_state = new_state;
   }
 
   abstract class Tutorial {
     abstract void run();
   }
 
   void set_gesture_knowledge(int level) {
     for (int i = 0; i < 9; i++) {
       Gesture g = gesture[i];
       if (null != g) g.learned = false;
     }
     // Exploit fall-through.
     switch(level) {
       case Wisdom.ALL_GESTURES:
         gesture[Gesture.CLAP].learned = true;
       case Wisdom.ALL_BUT_C:
         gesture[Gesture.FINGERS].learned = true;
       case Wisdom.ALL_BUT_FC:
         gesture[Gesture.WAVE].learned = true;
       case Wisdom.DKPS:
         gesture[Gesture.DIGIT].learned = true;
       case Wisdom.KPS:
         gesture[Gesture.SNAP].learned = true;
       case Wisdom.KNIFE_AND_PALM:
         gesture[Gesture.PALM].learned = true;
       case Wisdom.KNIFE_ONLY:
         gesture[Gesture.KNIFE].learned = true;
       case Wisdom.NONE:
     }
   }
 
   void learn(Spell sp) {
     sp.learned = true;
   }
 
   void set_spell_knowledge(int level) {
     for (int i = 0; i < spell_list_count; i++) {
       Spell sp = spell_list[i];
       sp.learned = false;
     }
     // Exploits fall-through.
     switch(level) {
       case Wisdom.ALL_LEVEL_1:
         learn(spellAtGesture("WWP"));
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
       count = 0;
     }
     void run() {
       for(;;) switch(state) {
 	case 0:
 	  clear_choices();
 	  set_gesture_knowledge(Wisdom.KNIFE_ONLY);
 	  set_spell_knowledge(Wisdom.STAB);
 	  board.setVisibility(View.GONE);
 	  arrow_view.setVisibility(View.GONE);
 	  jack_says(R.string.welcome);
 	  state = 100;
 	  return;
 	case 100:
 	  jack_says(R.string.howtoknife);
 	  state = 1;
 	  return;
 	case 1:
 	  jack_says(R.string.howtoknife2);
 	  state = 2;
 	  return;
 	case 2:
 	  clear_choices();
 	  invalidate();
 	  set_main_state(STATE_GESTURE_ONLY);
 	  state = 3;
 	  return;
 	case 3:
 	  if (choice[0] == Gesture.KNIFE || choice[1] == Gesture.KNIFE) {
 	    count++;
 	    switch(count) {
 	    case 3:
 	      jack_says(R.string.howtoknifepass3);
 	      state = 4;
 	      break;
 	    case 2:
 	      jack_says(R.string.howtoknifepass2);
 	      state = 2;
 	      break;
 	    case 1:
 	      jack_says(R.string.howtoknifepass1);
 	      state = 2;
 	      break;
 	    }
 	  } else {
 	    state = 1;
 	    break;
 	  }
 	  return;
 	case 4:
 	  set_main_state(STATE_NORMAL);
 	  spelltap.next_state();
 	  spelltap.goto_town();
 	  return;
       }
     }
     int state;
     int count;
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
         init_opponent(Agent.getDummy());
 	being_list[1].start_life(3);
 	reset_game();
 	jack_says(R.string.dummytut);
 	board.setVisibility(View.VISIBLE);
 	arrow_view.setVisibility(View.VISIBLE);
 	invalidate();
 	state = 2;
         return;
       case 2:
 	get_ready();
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
 
   // Now we're talking! Two goblins and a dummy. Since the dummy is the
   // default target, the player is must retarget their stabs if they are
   // to win.
   class TargetTutorial extends Tutorial {
     TargetTutorial() {
       state = 0;
     }
     void run() {
       for(;;) switch(state) {
       case 0:
         init_opponent(Agent.getDummy());
 	being_list[1].start_life(3);
 	reset_game();
 	// Two goblins.
 	being_list[2] = new Being("Porsap", R.drawable.goblin, 1);
 	being_list[2].start_life(1);
 	being_list[2].target = 0;
 
 	being_list[3] = new Being("Dedmeet", R.drawable.goblin, 1);
 	being_list[3].start_life(1);
 	being_list[3].target = 0;
 	being_list_count = 4;
 
 	jack_says(R.string.targettut);
 	board.setVisibility(View.VISIBLE);
 	arrow_view.setVisibility(View.VISIBLE);
 	state = 1;
 	invalidate();
         return;
       case 1:
 	set_main_state(STATE_NORMAL);
         get_ready();
 	state = 2;
         return;
       case 2:
 	switch(winner) {
 	case 0:
 	  jack_says(R.string.targettutwin);
 	  state = 3;
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
 	reset_being_pos();
         return;
       case 3:
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
       count = 0;
     }
     void run() {
       for(;;) switch(state) {
 	case 0:
 	  set_gesture_knowledge(Wisdom.KNIFE_AND_PALM);
 	  reset_game();
 	  board.setVisibility(View.GONE);
 	  arrow_view.setVisibility(View.GONE);
 	  jack_says(R.string.palmtut);
 	  state = 100;
 	  return;
 	case 100:
 	  jack_says(R.string.palmtut1);
 	  state = 1;
 	  return;
 	case 1:
 	  clear_choices();
 	  set_main_state(STATE_GESTURE_ONLY);
 	  invalidate();
 	  state = 2;
 	  return;
 	case 2:
 	  if (choice[0] == Gesture.PALM || choice[1] == Gesture.PALM) {
 	    count++;
 	    switch(count) {
 	    case 3:
 	      jack_says(R.string.palmtutpass3);
 	      state = 3;
 	      break;
 	    case 2:
 	      jack_says(R.string.palmtutpass2);
 	      state = 1;
 	      break;
 	    case 1:
 	      jack_says(R.string.palmtutpass1);
 	      state = 1;
 	      break;
 	    }
 	  } else {
 	    state = 100;
 	    break;
 	  }
 	  return;
 	case 3:
 	  jack_says(R.string.palmtutpass4);
 	  state = 4;
 	  return;
 	case 4:
 	  jack_says(R.string.palmtutpass5);
 	  state = 5;
 	  return;
 	case 5:
 	  set_main_state(STATE_NORMAL);
 	  spelltap.next_state();
 	  spelltap.goto_town();
 	  return;
       }
     }
     int state;
     int count;
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
 	  init_opponent(Agent.getDummy());
 	  being_list[1].start_life(dummyhp);
 	  reset_game();
 	  state = 1;
 	  board.setVisibility(View.VISIBLE);
 	  arrow_view.setVisibility(View.VISIBLE);
 	  get_ready();
 	  invalidate();
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
 	init_opponent(agent);
 	reset_game();
 	board.setVisibility(View.VISIBLE);
 	arrow_view.setVisibility(View.VISIBLE);
 	get_ready();
 	invalidate();
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
 
   // Introduce SD.
   class SDTutorial extends Tutorial {
     SDTutorial() {
       state = -1;
       hand = 0;
     }
     void run() {
       for(;;) switch(state) {
 	case -1:
 	  set_gesture_knowledge(Wisdom.KPS);
 	  init_opponent(Agent.getDummy());
 	  reset_game();
 	  board.setVisibility(View.VISIBLE);
 	  arrow_view.setVisibility(View.VISIBLE);
 	  jack_says(R.string.SDtut0);
 	  state = 0;
 	  return;
 	case 0:
 	  // Restore life in case player has been messing around.
 	  being_list[0].start_life(5);
 	  being_list[1].start_life(5);
 	  hist.reset();
 	  opphist.reset();
 	  jack_says(R.string.SDtut);
 	  state = 1;
 	  return;
 	case 1:
 	  clear_choices();
 	  get_ready();
 	  set_main_state(STATE_ON_END_ROUND);
 	  state = 2;
 	  invalidate();
 	  return;
 	case 2:
 	  if (hist.gest[0][0] == Gesture.SNAP &&
 	      hist.gest[0][1] == Gesture.SNAP) {
 	    jack_says(R.string.SDtutpass1);
 	    set_spell_knowledge(Wisdom.UP_TO_MISSILE);
 	    state = 3;
 	  } else {
 	    jack_says(R.string.SDtutwrong);
 	    state = 0;
 	  }
 	  return;
 	case 3:
 	  set_gesture_knowledge(Wisdom.DKPS);
 	  get_ready();
 	  state = 4;
 	  invalidate();
 	  return;
 	case 4:
 	  if (hist.gest[1][0] == Gesture.DIGIT &&
 	      hist.gest[1][1] == Gesture.DIGIT) {
 	    jack_says(R.string.SDtutpass2);
 	    state = 5;
 	  } else {
 	    jack_says(R.string.SDtutwrong);
 	    state = 0;
 	  }
 	  return;
 	case 5:
 	  jack_says(R.string.SDtutpass3);
 	  state = 6;
 	  return;
 	case 6:
 	  jack_says(R.string.SDtutpass4);
 	  state = 7;
 	  return;
 	case 7:
 	  set_main_state(STATE_NORMAL);
 	  get_ready();
 	  state = 8;
 	  return;
 	case 8:
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
 	  init_opponent(Agent.getDummy());
 	  being_list[1].start_life(4);
 	  reset_game();
 	  board.setVisibility(View.VISIBLE);
 	  arrow_view.setVisibility(View.VISIBLE);
 	  jack_says(R.string.wavetut);
 	  state = 1;
 	  return;
 	case 1:
 	  clear_choices();
 	  get_ready();
 	  set_main_state(STATE_ON_END_ROUND);
 	  state = 2;
 	  invalidate();
 	  return;
 	case 2:
 	  if (hist.gest[0][0] == Gesture.WAVE &&
 	      hist.gest[0][1] == Gesture.WAVE) {
 	    jack_says(R.string.fingerstut);
 	    state = 3;
 	  } else {
 	    jack_says(R.string.fingerstutfail);
 	    state = 0;
 	  }
 	  return;
 	case 3:
 	  set_gesture_knowledge(Wisdom.ALL_BUT_C);
 	  get_ready();
 	  state = 4;
 	  invalidate();
 	  return;
 	case 4:
 	  if (hist.gest[1][0] == Gesture.FINGERS &&
 	      hist.gest[1][1] == Gesture.FINGERS) {
 	    jack_says(R.string.fingerstutpass1);
 	    set_spell_knowledge(Wisdom.UP_TO_WFP);
 	    state = 5;
 	  } else {
 	    jack_says(R.string.fingerstutfail);
 	    state = 0;
 	  }
 	  return;
 	case 5:
 	  get_ready();
 	  invalidate();
 	  state = 6;
 	  return;
 	case 6:
 	  if (hist.gest[2][0] == Gesture.PALM &&
 	      hist.gest[2][1] == Gesture.PALM && 0 == winner) {
 	    jack_says(R.string.fingerstutpass2);
 	    state = 7;
 	  } else {
 	    jack_says(R.string.fingerstutfail);
 	    state = 0;
 	  }
 	  return;
 	case 7:
 	  jack_says(R.string.fingerstutpass3);
 	  state = 8;
 	  return;
 	case 8:
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
     if (target <= 1) {  // Player, opponent or thin air.
       return (char) ('A' + target);
     } else {
       return (char) ('A' + 2 + being_list[target].id);
     }
   }
 
   private int decode_target(char c) {
     int raw = c - 'A';
     if (-1 == raw) return -1;
     if (0 == raw || 1 == raw) return 1 - raw;
     raw -= 2;
     if (0 == (raw & 1)) raw++;
     else raw--;
     for (int i = 2; i < being_list_count; i++) {
       if (raw == being_list[i].id) return i;
     }
     return -1;
   }
 
   void net_move(SpellTapMove turn) {
     String s = "";
     // Encode gestures.
     s += (char) (choice[0] + '0');
     s += (char) (choice[1] + '0');
     // Encode spells and targets.
     for (int h = 0; h < 2; h++) {
       if (-1 != spell_choice[h]) {
 	s += (char) (ready_spell[spell_choice[h]][h].index + 'A');
 	s += encode_target(spell_target[h]);
       } else {
 	s += (char) ('A' - 1);
 	s += (char) ('A' - 1);
       }
     }
     // Encode monster attacks.
     String s2 = "";
     int count = 0;
     for (int i = 2; i < being_list_count; i++) {
       Being b = being_list[i];
       if (0 == b.controller) {
 	s2 += encode_target(i);
 	s2 += encode_target(b.target);
 	count++;
       }
     }
     s += (char) ('0' + count);
     s += s2;
 
     opp_ready = false;
     print("Waiting for opponent...");
     invalidate();
     // Spawn a thread to send move over the network, so we can keep handling
     // user input.
     net_state = NET_DECODE;
     Tubes.send_move(s);
   }
   static int net_state;
   static final int NET_DECODE = 0;
   static final int NET_CHARM_CHOSEN = 1;
   static final int NET_GET_CHARM_HAND = 2;
   static final int NET_GET_CHARM_GESTURE = 3;
 
   void net_set_charm(int hand, int gesture) {
     opp_ready = false;
     print("Informing server...");
     invalidate();
     net_state = NET_CHARM_CHOSEN;
     Tubes.send_set_charm(hand, gesture);
   }
 
   int net_get_charm_hand() {
     opp_ready = false;
     print("Waiting for opponent to choose charmed hand...");
     invalidate();
     net_state = NET_GET_CHARM_HAND;
     Tubes.send_get_charm_hand();
     return -1;
   }
 
   int net_get_charm_gesture() {
     opp_ready = false;
     print("Waiting for server...");
     invalidate();
     net_state = NET_GET_CHARM_GESTURE;
     Tubes.send_get_charm_gesture();
     return -1;
   }
 
   static NetHandler net_handler;
   class NetHandler extends Handler {
     @Override
     public void handleMessage(Message msg) {
       // Valid reply received from network.
       try {
 	Tubes.net_thread.join();
       } catch (InterruptedException e) {
       }
       Log.i("Reply", Tubes.reply);
       switch(net_state) {
 	case NET_DECODE:
 	  decode_move(oppmove, Tubes.reply);
 	  break;
 	case NET_CHARM_CHOSEN:
 	  opp_ready = true;
 	  is_waiting = false;
 	  note_charm_chosen();
 	  break;
 	case NET_GET_CHARM_HAND:
 	  switch(Tubes.reply.charAt(0)) {
 	    case '0':
 	      charmed_hand = 0;
 	      break;
 	    case '1':
 	      charmed_hand = 1;
 	      break;
 	  }
 	  if (-1 == charmed_hand) {
 	    Log.e("TODO", "Handle bad messages");
 	  }
 	  opp_ready = true;
 	  is_waiting = false;
 	  new_round_post_charm();
 	  break;
 	case NET_GET_CHARM_GESTURE:
 	  {
 	    int g = Tubes.reply.charAt(0) - '0';
 	    if (g >= 0 && g <= 8 && g != Gesture.NONE && gesture[g] != null) {
 	      choice[charmed_hand] = g;
 	    } else {
 	      Log.e("TODO", "Handle bad messages");
 	      break;
 	    }
 	    opp_ready = true;
 	    is_waiting = false;
 	    apply_charm();
 	    break;
 	  }
       }
     }
   }
 
   void decode_move(SpellTapMove turn, String r) {
     if (null == r || r.length() < 7) {
       // TODO: Do something for invalid messages!
       return;
     }
     // TODO: Check message is valid!
     // Decode gestures, spells and spell targets.
     for (int h = 0; h < 2; h++) {
       turn.gest[h] = r.charAt(h) - '0';
       turn.spell[h] = r.charAt(2 + 2 * h) - 'A';
       turn.spell_target[h] = decode_target(r.charAt(3 + 2 * h));
     }
     // Decode monster attacks.
     turn.attack_count = r.charAt(6) - '0';
     for (int n = 0; n < turn.attack_count; n++) {
       turn.attack_source[n] = decode_target(r.charAt(7 + 2 * n));
       turn.attack_target[n] = decode_target(r.charAt(7 + 2 * n + 1));
     }
     opp_ready = true;
     is_waiting = false;
     resolve();
   }
 
   class NetAgent extends Agent {
     String name() { return "Opponent"; }
     String name_full() { return "Opponent"; }
     int life() { return 5; }
     int bitmap_id() { return R.drawable.wiz; }
     void move(SpellTapMove turn) {
       net_move(turn);
     }
     void set_charm(int hand, int gesture) {
       net_set_charm(hand, gesture);
     }
     int get_charm_hand() {
       return net_get_charm_hand();
     }
     int get_charm_gesture() {
       return net_get_charm_gesture();
     }
   }
 
   class NetDuel extends Tutorial {
     NetDuel() {
       state = 0;
     }
     void run() {
       for(;;) switch(state) {
 	case 0:
 	  if (0 != Tubes.newgame()) {
 	    main_state = STATE_VOID;
 	    board.setVisibility(View.GONE);
 	    arrow_view.setVisibility(View.GONE);
 	    spelltap.narrate(R.string.servererror);
 	    state = 1;
 	    return;
 	  }
 	  init_opponent(new NetAgent());
 	  reset_game();
 	  board.setVisibility(View.VISIBLE);
 	  arrow_view.setVisibility(View.VISIBLE);
           state = 1;
 	  get_ready();
 	  invalidate();
 	  return;
 	case 1:
 	  spelltap.goto_town();
           state = 0;
 	  return;
       }
     }
     int state;
   }
 
   void clear_choices() {
     choice[1] = choice[0] = Gesture.NONE;
     lastchoice[0] = lastchoice[1] = choice[0];
     ready_spell_count[0] = ready_spell_count[1] = 0;
     spell_choice[0] = spell_choice[1] = -1;
     spell_text[0] = spell_text[1] = "";
     arrow_view.bmspell[0] = arrow_view.bmspell[1] = null;
   }
 
   class History {
     History() {
       gest = new int[128][2];
       start = new int[2];
       reset();
     }
     void reset() {
       cur = 0;
       start[0] = start[1] = 0;
     }
     boolean is_doubleP() {
       if (cur == 0) Log.e("History", "is_doubleP called with no history");
       return gest[cur - 1][0] == Gesture.PALM && gest[cur - 1][1] == Gesture.PALM;
     }
     void add(int g[]) {
       gest[cur][0] = g[0];
       gest[cur][1] = g[1];
       // Stabs and null gestures break combos.
       if (cur > 0) {
 	if (gest[cur - 1][0] == Gesture.KNIFE) start[0] = cur;
 	if (gest[cur - 1][1] == Gesture.KNIFE) start[1] = cur;
       }
       if (g[0] == Gesture.KNIFE) start[0] = cur;
       if (g[1] == Gesture.KNIFE) start[1] = cur;
       cur++;
       if (g[0] == Gesture.NONE) start[0] = cur;
       if (g[1] == Gesture.NONE) start[1] = cur;
       // No spell needs more than 7 turns.
       if (cur > start[0] + 6) start[0]++;
       if (cur > start[1] + 6) start[1]++;
     }
     int[][] gest;
     int cur;
     int[] start;
   }
 
   private void put_gest(String s, int x, int y) {
     int n = Gesture.flattenxy(x, y);
     Gesture g = gesture[n] = new Gesture(s, x, y);;
     g.name = s;
     g.x = x;
     g.y = y;
   }
 
   void init_opponent(Agent a) {
     being_list[1] = new Being(a.name(), a.bitmap_id(), -2);
     being_list[1].start_life(a.life());
     agent = a;
   }
 
   // Constructor.
   public MainView(Context context, AttributeSet attrs) {
     super(context, attrs);
     con = context;
     drag_i = -1;
     is_animating = false;
     choice = new int[2];
     lastchoice = new int[2];
     hist = new History();
     opphist = new History();
     ready_spell_count = new int[2];
     ready_spell = new Spell[4][2];
     spell_choice = new int[2];
     spell_choice[0] = spell_choice[1] = 0;
     spell_text = new String[2];
     spell_text[0] = spell_text[1] = "";
     spell_list = new Spell[64];
     spell_list_count = 0;
     stab_spell = new StabSpell();
     add_spell(stab_spell, 65);
     add_spell(new ShieldSpell(), 8);
     add_spell(new MissileSpell(), 64);
     add_spell(new CauseLightWoundsSpell(), 63);
     add_spell(new ConfusionSpell(), 28);
     add_spell(new CureLightWoundsSpell(), 128);
     add_spell(new SummonGoblinSpell(), 17);
     add_spell(new ProtectionSpell(), 7);
     add_spell(new CharmPersonSpell(), 31);
 
     being_list = new Being[16];
     summon_count = new int[2];
     summon_count[0] = summon_count[1] = 0;
     init_being_pos();
 
     being_list[0] = new Being("Player", R.drawable.wiz, -1);
     being_list[0].start_life(5);
     init_opponent(Agent.getDummy());
     being_list[1].start_life(3);
     being_list_count = 2;
 
     spell_target = new int[2];
     exec_queue = new SpellCast[16];
 
     monatt = new MonsterAttack[5];
     for (int i = 1; i <= 4; i++) {
       monatt[i] = new MonsterAttack(i);
      monatt[i].priority = 66;
     }
 
     msg = "";
     bmcorpse = BitmapFactory.decodeResource(getResources(), R.drawable.corpse);
     oppmove = new SpellTapMove();
 
     gesture = new Gesture[9];
     put_gest("Snap", -1, -1);
     put_gest("Knife", 0, -1);
     put_gest("Digit", 1, -1);
     put_gest("Clap", 1, 0);
     put_gest("Wave", -1, 1);
     put_gest("Palm", 0, 1);
     put_gest("Fingers", 1, 1);
 
     arrow_view = null;
     tilt_state = TILT_AWAIT_UP;
     charmed_hand = -1;
     freeze_gesture = false;
     choosing_charm = false;
     net_handler = new NetHandler();
   }
   static String emptyleftmsg;
   static String emptyrightmsg;
 
   public void get_ready() {
     print("Draw gestures, and tap here to confirm.");
   }
 
   public void add_spell(Spell sp, int priority) {
     spell_list[spell_list_count] = sp;
     sp.index = spell_list_count;
     sp.priority = priority;
     spell_list_count++;
   }
 
   static StabSpell stab_spell;
 
   @Override
   public void onDraw(Canvas canvas) {
     super.onDraw(canvas);
     int x, y;
 
     // Board class handles avatars and status line.
 
     // Opponent history.
     y = 16 - 4;
     String s = "";
     for (int i = opphist.start[0]; i < opphist.cur; i++) {
       s += " " + gesture[opphist.gest[i][0]].abbr;
     }
     canvas.drawText(s, 0, y, Easel.grey_text);
     s = "";
     for (int i = opphist.start[1]; i < opphist.cur; i++) {
       s += " " + gesture[opphist.gest[i][1]].abbr;
     }
     canvas.drawText(s, 320, y, Easel.grey_rtext);
 
     // Player history.
     y = ylower - 4;
     s = "";
     for (int i = hist.start[0]; i < hist.cur; i++) {
       s += " " + gesture[hist.gest[i][0]].abbr;
     }
     canvas.drawText(s, 0, y, Easel.grey_text);
     s = "";
     for (int i = hist.start[1]; i < hist.cur; i++) {
       s += " " + gesture[hist.gest[i][1]].abbr;
     }
     canvas.drawText(s, 320, y, Easel.grey_rtext);
 
     // Gesture area.
     y = ylower;
     canvas.drawRect(0, y, 320, 480, Easel.octarine);
 
     // Status line highlight.
     canvas.drawRect(0, ystatus, 320, 480, Easel.status_paint);
 
     // Gesture and spell text.
     // TODO: Rethink Charm presentation.
     y = ylower + 16 - 4;
     if (0 == charmed_hand) {
       if (freeze_gesture) {
 	Gesture g = gesture[choice[0]];
 	canvas.drawText(g.statusname, 0, y, Easel.charm_text);
       } else {
 	canvas.drawText("[Charmed]", 0, y, Easel.charm_text);
       }
     } else {
       Gesture g = gesture[choice[0]];
       if (null == g) {
 	canvas.drawText(emptyleftmsg, 0, y, Easel.grey_text);
       } else {
 	canvas.drawText(g.statusname, 0, y, Easel.white_text);
       }
     }
     if (1 == charmed_hand) {
       if (freeze_gesture) {
 	Gesture g = gesture[choice[1]];
 	canvas.drawText(g.statusname, 320, y, Easel.charm_rtext);
       } else {
 	canvas.drawText("[Charmed]", 320, y, Easel.charm_rtext);
       }
     } else {
       Gesture g = gesture[choice[1]];
       if (null == g) {
 	canvas.drawText(emptyrightmsg, 320, y, Easel.grey_rtext);
       } else {
 	canvas.drawText(g.statusname, 320, y, Easel.white_rtext);
       }
     }
 
     canvas.drawText(spell_text[0], 0, y + 16, Easel.white_text);
     canvas.drawText(spell_text[1], 320, y + 16, Easel.white_rtext);
 
     // Spell choice row 1
     x = 0;
     y = ylower + 32;
     for (int h = 0; h < 2; h++) {
       for (int i = 0; i < ready_spell_count[h]; i++) {
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
     /*
     y += 50;
     canvas.drawRect(0, y, 50 - 1, y + 50 - 1, Easel.paint);
     */
   }
 
   static boolean okstate;
 
   private void choose_spell(int h, int i) {
     // Assumes i is a valid choice for hand h.
     if (i == spell_choice[h]) return;
     spell_choice[h] = i;
     spell_target[h] = ready_spell[i][h].target;
 
     if (-1 != spell_choice[h]) {
       Spell sp = ready_spell[spell_choice[h]][h];
       arrow_view.bmspell[h] = sp.bitmap;
       spell_text[h] = sp.name;
     } else {
       arrow_view.bmspell[h] = null;
     }
     invalidate();
   }
 
   @Override
   public boolean onTouchEvent(MotionEvent event) {
     switch (event.getAction()) {
       case MotionEvent.ACTION_DOWN:
 	if (is_animating) return false;
 	if (is_waiting) return false;
 	x0 = event.getX();
 	y0 = event.getY();
 	if (y0 < ylower) {
 	  if (STATE_GESTURE_ONLY == main_state) {
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
 	    for (int i = 2; i < being_list_count; i++) {
 	      Being b = being_list[i];
 	      // It might not matter if you can retarget a corpse's
 	      // attack, but it doesn't seem useful.
 	      if (b.dead || 0 != b.controller) continue;
 	      if (b.contains(x0, y0)) {
 		drag_i = i;
 		break;
 	      }
 	    }
 	  }
 	  if (-1 != drag_i) {
 	    print("Drag to target.");
 	    // TODO: Only invalidate status bar.
 	    invalidate();
 	    return true;
 	  }
 	  return false;
 	}
 	okstate = y0 >= ystatus;
 	return true;
       case MotionEvent.ACTION_UP:
 	if (is_animating) return false;
 	if (is_waiting) return false;
 	x1 = event.getX();
 	y1 = event.getY();
 	if (drag_i != -1) {
 	  get_ready();
 	  int target;
 	  for(target = being_list_count - 1; target >= 0; target--) {
 	    Being b = being_list[target];
 	    if (b.contains(x1, y1)) break;
 	  }
 	  // drag_i = 0, 1 means player is targeting spell.
 	  if (drag_i <= 1) {
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
 	    }
 	  } else {
 	    Being b = being_list[drag_i];
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
 	  if (STATE_GESTURE_ONLY == main_state) {
 	    run();
 	    return true;
 	  }
 	  if (okstate && y1 >= ystatus) {
 	    confirm_move();
 	    return true;
 	  }
 	  if (y1 >= ylower + 32 && y1 < ylower + 32 + 50) {
 	    // Could be choosing a ready spell.
 	    for (int h = 0; h < 2; h++) {
 	      int i;
 	      if (h == 1) {
 		if (x1 < 160) break;
 		i = (320 - (int) x1) / 50;
 	      } else {
 		i = ((int) x1) / 50;
 	      }
 	      if (i >= 0 && i < ready_spell_count[h]) {
 		choose_spell(h, i);
 		return true;
 	      }
 	    }
 	  }
 	} else {
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
 	  if (h == charmed_hand || freeze_gesture) {
 	    return true;
 	  }
 	  choice[h] = Gesture.flattenxy(dirx, diry);
 	  if (null == gesture[choice[h]] || !gesture[choice[h]].learned) {
 	    choice[h] = Gesture.NONE;
 	  }
 	  if (choice[h] != lastchoice[h]) {
 	    handle_new_choice(h);
 	  }
 	  if (STATE_GESTURE_ONLY == main_state) run();
 	  return true;
 	}
     }
     return false;
   }
 
   static int tilt_state;
   void tilt_up() {
     if (TILT_AWAIT_UP != tilt_state) return;
     if (STATE_NORMAL != main_state) return;
     // Tilt only works when there are gestures in both hands not counting
     // charmed hands, or when the player has chosen a gesture for the opponent.
     if ((choice[0] != Gesture.NONE && choice[1] != Gesture.NONE) ||
         (-1 != charmed_hand && choice[1 - charmed_hand] != Gesture.NONE) ||
 	(choosing_charm && (choice[0] != Gesture.NONE || choice[1] != Gesture.NONE))) {
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
     SpellCast(Spell init_spell, int init_source, int init_target) {
       spell = init_spell;
       source = init_source;
       target = init_target;
     }
     Spell spell;
     int target;
     int source;
   }
   static int exec_queue_count;
   static SpellCast[] exec_queue;
 
   // TODO: Move to Agent class. Also, introduce charm_hand and charm_gesture
   // variables there, so network code in this class can avoid touching
   // charmed_hand and choice[].
   static boolean opp_ready;
 
   private void confirm_move() {
     tilt_state = TILT_DISABLED;
     board.animation_reset();  // Stop tilt animation if it's still going.
     // If Charm Person has been cast on opponent, must first send
     // chosen gesture before normal turn.
     if (choosing_charm) {
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
 	  is_waiting = true;
 	}
       }
       return;
     }
     if (-1 != charmed_hand) {
       if (!freeze_gesture) {
 	tilt_state = TILT_AWAIT_UP;
 	opp_ready = true;
 	choice[charmed_hand] = agent.get_charm_gesture();
 	if (opp_ready) {
 	  apply_charm();
 	} else  {
 	  is_waiting = true;
 	  // Network code calls apply_charm().
 	}
 	return;
       }
       freeze_gesture = false;
     }
     get_opp_moves();
   }
 
   void apply_charm() {
     handle_new_choice(charmed_hand);
     freeze_gesture = true;
     print("Charm takes effect. Confirm spells and targets.");
   }
 
   void get_opp_moves() {
     arrow_view.setVisibility(View.GONE);
     hist.add(choice);
 
     opp_ready = true;
     agent.move(oppmove);
     // In local duels, opp_ready should still be true.
     if (opp_ready) {
       resolve();
       return;
     }
     // Otherwise we wait for network code to call resolve().
     is_waiting = true;
   }
 
   void note_charm_chosen() {
     choosing_charm = false;
     clear_choices();
     get_ready();
     invalidate();
     new_round2();
   }
 
   static boolean is_waiting;
 
   // Insert spells by priority.
   private void insert_spell(SpellCast sc) {
     int i;
     for (i = 0; i < exec_queue_count; i++) {
       if (exec_queue[i].spell.priority > sc.spell.priority) break;
     }
     for (int j = exec_queue_count; j > i; j--) {
       exec_queue[j] = exec_queue[j - 1];
     }
     exec_queue[i] = sc;
     exec_queue_count++;
   }
 
   private void resolve() {
     is_animating = true;
     opphist.add(oppmove.gest);
 
     // Expire status effects.
     for (int i = 0; i < being_list_count; i++) {
       Being b = being_list[i];
       b.status = Status.OK;
     }
 
     exec_queue_count = 0;
     // Insert player spells and targets into execution queue.
     for (int h = 0; h < 2; h++) {
       if (-1 == spell_choice[h]) continue;
       SpellCast sc = new SpellCast(
 	  ready_spell[spell_choice[h]][h], 0, spell_target[h]);
       insert_spell(sc);
     }
     // Insert opponent spells and targets.
     for (int h = 0; h < 2; h++) {
       if (-1 == oppmove.spell[h]) continue;
       Spell sp = spell_list[oppmove.spell[h]];
       SpellCast sc = new SpellCast(sp, 1, oppmove.spell_target[h]);
       insert_spell(sc);
     }
     // Retarget monsters controlled by oppponent.
     for (int i = 0; i < oppmove.attack_count; i++) {
       Being b = being_list[oppmove.attack_source[i]];
       b.target = oppmove.attack_target[i];
     }
     // Insert monster attacks.
     for (int i = 2; i < being_list_count; i++) {
       Being b = being_list[i];
       if (b.dead) continue;
       if (-1 != b.target) {
 	SpellCast sc = new SpellCast(monatt[b.life_max], i, b.target);
 	insert_spell(sc);
       }
     }
 
     clear_choices();
     exec_cursor = 0;
     // TODO: Print message and delay if there are no spells.
     // Or maybe flash the screen and make a sound unconditionally to get
     // attention; in multiplayer, there can be a delay while waiting for
     // opponent.
     next_spell();
   }
 
   public void print(String s) {
     msg = s;
   }
 
   static int exec_cursor;
   public void next_spell() {
     if (exec_cursor < exec_queue_count) {
       SpellCast sc = exec_queue[exec_cursor];
       String s = "";
       String srcname = being_list[sc.source].name;
       String tgtname = null;
       if (sc.target != -1) {
 	tgtname = being_list[sc.target].name;
       }
       if (sc.source >= 2) {
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
       if (0 == sc.target) {
 	if (0 == sc.source) {
 	  s += "yourself.";
 	} else {
 	  s += "you.";
 	}
       } else if (-1 == sc.target) {
 	s += "thin air!";
       } else {
 	s += tgtname + ".";
       }
       print(s);
       Log.i("MV", s);
       sc.spell.execute(sc.source, sc.target);
       exec_cursor++;
     } else {
       end_round();
     }
   }
 
   // End of round. Check for death, shield expiration, etc.
   void end_round() {
     boolean gameover = false;
     for(int i = being_list_count - 1; i >= 0; i--) {
       Being b = being_list[i];
       if (b.shield > 0) b.shield--;
       // TODO: Shield off animation.
       if (b.life <= 0) {
 	if (i >= 2) b.bitmap = bmcorpse;
 	b.die();
       }
     }
 
     is_animating = false;
     tilt_state = TILT_AWAIT_UP;
     arrow_view.setVisibility(View.VISIBLE);
     int sid = R.string.bug;
     winner = -1;
     if (being_list[1].dead) {
       gameover = true;
       winner = 0;
       sid = R.string.win;
       if (being_list[0].dead) {
 	winner = 2;
 	sid = R.string.draw;
       }
     } else if (being_list[0].dead) {
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
       print("");
       spelltap.narrate(sid);
       // run() is called once the player has tapped through the
       // victory screen.
     } else if (STATE_ON_END_ROUND == main_state) {
       run();
     } else {
       new_round();
     }
   }
 
   boolean player_charmed() {
     return Status.CHARMED == being_list[0].status;
   }
 
   boolean opp_charmed() {
     return Status.CHARMED == being_list[1].status;
   }
 
   // Start new round.
   void new_round() {
     // Thus begins the Charm Person spaghetti. The most complex case is
     // when Charm Person has simultaneously been cast on both players. Then:
     //  1. I pick a hand and gesture for my opponent.
     //  2. I ask which hand of mine was chosen by my opponent.
     //  3. I choose a gesture with my other hand.
     //  4. I ask which gesture the opponent chose for my charmed hand.
     //  5. I choose spells and targets from the results.
     // Non-blocking network communication complicates the code further.
     if (opp_charmed()) {
       choosing_charm = true;
       print("Charm: Pick gesture for opponent.");
       return;
     }
     new_round2();
   }
 
   void reset_game() {
     clear_choices();
     hist.reset();
     opphist.reset();
     being_list_count = 2;
     being_list[0].status = Status.OK;
     being_list[1].status = Status.OK;
     being_list[0].shield = 0;
     being_list[1].shield = 0;
     being_list[0].heal_full();
     being_list[1].heal_full();
     set_main_state(STATE_NORMAL);
     is_waiting = false;
   }
 
   void new_round2() {
     // Handle charm on player.
     if (player_charmed()) {
       // Get charmed hand from opponent.
       opp_ready = true;
       charmed_hand = agent.get_charm_hand();
       if (!opp_ready) {
 	// Network game. Handler will call new_round_post_charm once
 	// a valid reply is received.
 	is_waiting = true;
 	return;
       }
     } else {
       charmed_hand = -1;
     }
     new_round_post_charm();
   }
 
   void new_round_post_charm() {
     // Handle confused monsters.
     for (int i = 2; i < being_list_count; i++) {
       Being b = being_list[i];
       if (Status.CONFUSED == b.status) {
 	b.target = b.controller;
       }
     }
     get_ready();
     invalidate();
   }
 
   private void handle_new_choice(int h) {
     if (choosing_charm) {
       if (Gesture.NONE != choice[h]) {
 	choice[1 - h] = Gesture.NONE;
       }
       invalidate();
       return;
     }
     spell_search(h);
     if (Status.CONFUSED == being_list[0].status) {
       if (choice[1 - h] != choice[h]) {
 	choice[1 - h] = choice[h];
 	spell_search(1 - h);
       }
     }
     invalidate();
   }
 
   private void spell_search(int h) {
     ready_spell_count[h] = 0;
     spell_choice[h] = -1;
     if (choice[h] == Gesture.KNIFE) {
       if (choice[1 - h] == Gesture.KNIFE) {
 	spell_text[h] = "(only one knife)";
 	return;
       } else {
 	add_ready_spell(h, stab_spell);
 	spell_text[h] = "";
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
 	  String g = spell_list[i].gesture;
 	  int k = g.length();
 	  if (k > hist.cur - hist.start[h] + 1) continue;
 	  k--;
 	  if (g.charAt(k) != gesture[choice[h]].abbr) continue;
 	  k--;
 	  int k2 = hist.cur - 1;
 	  while (k >= 0) {
 	    if (g.charAt(k) != gesture[hist.gest[k2][h]].abbr) {
 	      break;
 	    }
 	    k2--;
 	    k--;
 	  }
 	  if (0 > k) {
 	    // At last we have a match.
 	    add_ready_spell(h, spell_list[i]);
 	  }
 	}
       }
     }
     if (ready_spell_count[h] > 0) {
       choose_spell(h, ready_spell_count[h] - 1);
     } else {
       arrow_view.bmspell[h] = null;
     }
     lastchoice[0] = choice[0];
     lastchoice[1] = choice[1];
   }
 
   public void add_ready_spell(int h, Spell sp) {
     ready_spell[ready_spell_count[h]][h] = sp;
     ready_spell_count[h]++;
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
       bitmap = BitmapFactory.decodeResource(getResources(), bitmapid);
       target = def_target;
       learned = false;
     }
 
     abstract public void cast(int init_source, int init_target);
     Bitmap bitmap;
     String name;
     String gesture;
     int index;
     int target;
     int state;
     int description;
     SpannableString purty;
     boolean learned;
     boolean is_finished;  // Set this to true before calling last animation.
                           // Or call finish_spell() [it's slower].
     int cast_source, cast_target;
 
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
 	  if (target != -1) {
 	    Being b = being_list[target];
 	    if (0 == b.shield) b.shield = 1;
 	  }
 	  finish_spell();
 	  return;
       }
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
 	  if (target != -1) {
 	    Being b = being_list[target];
 	    if (0 == b.shield) {
 	      b.get_hurt(1);
 	      board.animate_move_damage(target, 1);
 	    } else {
 	      Log.i("TODO", "block animation");
 	      board.animate_move_damage(target, 0);
 	    }
 	  } else {
 	    board.animate_delay();
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
 	  if (target != -1) {
 	    Being b = being_list[target];
 	    if (0 == b.shield) {
 	      b.get_hurt(1);
 	      board.animate_damage(target, 1);
 	    } else {
 	      Log.i("TODO", "block animation");
 	      board.animate_damage(target, 0);
 	    }
 	  } else {
 	    board.animate_delay();
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
 	  if (-1 != target) {
 	    being_list[target].get_hurt(2);
 	    print("Cause Light Wounds deals 2 damage.");
 	  }
 	  board.animate_damage(target, 2);
 	  return;
       }
     }
   }
 
   public class SummonGoblinSpell extends Spell {
     SummonGoblinSpell() {
       init("Summon Goblin", "SFW", R.drawable.summon1, R.string.SFWdesc, 0);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  is_finished = true;
 	  if (-1 != target) {
 	    int k = being_list[target].controller;
 	    Being b = being_list[being_list_count] = new Being("Goblin", R.drawable.goblin, k);
 	    being_list_count++;
 	    b.start_life(1);
 	    b.target = 1 - k;
 	    Log.i("TODO", "fade in goblin");
 	    board.animate_spell(target, bitmap);
 	  } else {
 	    board.animate_delay();
 	  }
 	  return;
       }
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
 	  if (-1 != target) {
 	    being_list[target].heal(1);
 	  }
 	  board.animate_spell(target, bitmap);
 	  return;
       }
     }
   }
 
   public class ConfusionSpell extends Spell {
     ConfusionSpell() {
       init("Confusion", "DSF", R.drawable.confusion, R.string.DSFdesc, 1);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  is_finished = true;
 	  if (-1 != target) {
 	    being_list[target].status = Status.CONFUSED;
 	  }
 	  board.animate_spell(target, bitmap);
 	  return;
       }
     }
   }
 
   public class CharmPersonSpell extends Spell {
     CharmPersonSpell() {
       init("Charm Person", "PSDF", R.drawable.confusion, R.string.PSDFdesc, 1);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  is_finished = true;
 	  if (-1 != target) {
 	    // Only works on opponent.
 	    if (1 - source == target) {
 	      being_list[target].status = Status.CHARMED;
 	    }
 	  }
 	  board.animate_spell(target, bitmap);
 	  return;
       }
     }
   }
 
   public class ProtectionSpell extends Spell {
     ProtectionSpell() {
       init("Protection From Evil", "WWP", R.drawable.shield, R.string.WWPdesc, 0);
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  board.animate_shield(target);
 	  return;
 	case 1:
 	  if (target != -1) {
 	    Being b = being_list[target];
 	    b.shield = 4;
 	  }
 	  finish_spell();
 	  return;
       }
     }
   }
 
   public class MonsterAttack extends Spell {
     MonsterAttack(int n) {
       init("", "", R.drawable.goblin, R.string.bug, 1);
       level = n;
     }
     public void cast(int source, int target) {
       switch(state) {
 	case 0:
 	  board.animate_move(source, target);
 	  return;
 	case 1:
 	  if (target != -1) {
 	    Being b = being_list[target];
 	    if (0 == b.shield) {
 	      b.get_hurt(1);
 	      board.animate_move_damage(target, 1);
 	    } else {
 	      Log.i("TODO", "block animation");
 	      board.animate_move_damage(target, 0);
 	    }
 	  } else {
 	    board.animate_delay();
 	  }
 	  return;
 	case 2:
 	  is_finished = true;
 	  board.animate_move_back();
 	  return;
       }
     }
     int level;
   }
 
   static public class Status {
     static public final int OK = 0;
     static public final int CONFUSED = 1;
     static public final int CHARMED = 2;
   }
 
   static int[] summon_count;
   static Bitmap bmcorpse;
   public class Being {
     public Being(String init_name, int bitmapid, int owner) {
       switch(owner) {
 	case -1:  // This being is the player.
 	  y = ylower - 64;
 	  index = -1;
 	  controller = 0;
 	  break;
 	case -2:  // This being is the opponent.
 	  y = 0;
 	  index = -2;
 	  controller = 1;
 	  break;
         case 0:  // Player controls this being.
 	  for (index = 0; index < 16; index++) {
 	    if (null == being_pos[index].being) break;
 	  }
 	  controller = 0;
 	  break;
         case 1:  // Opponent controls this being.
 	  for (index = 16 - 1; index >= 0; index--) {
 	    if (null == being_pos[index].being) break;
 	  }
 	  controller = 1;
 	  break;
 	default:
 	  Log.e("Being", "Ctor given bad owner.");
 	  break;
       }
       if (owner < 0) {
 	x = 160 - 32;
 	set_size_64();
       } else {
 	if (index < 0 || index >= 16) Log.e("Being", "index out of range! Summon should never have been successful?");
 	x = being_pos[index].x;
 	y = being_pos[index].y;
 	being_pos[index].being = this;
 	set_size_48();
 	id = controller + 2 * summon_count[controller];
 	summon_count[controller]++;
       }
       status = Status.OK;
       shield = 0;
       dead = false;
       setup(init_name, bitmapid, 0);
     }
     void setup(String init_name, int bitmapid, int life) {
       name = init_name;
       bitmap = BitmapFactory.decodeResource(getResources(), bitmapid);
       start_life(life);
     }
     void heal(int amount) {
       if (!dead) {
 	life += amount;
 	if (life > life_max) life = life_max;
 	lifeline = Integer.toString(life) + "/" + Integer.toString(life_max);
       }
     }
     void get_hurt(int amount) {
       if (!dead) {
 	life -= amount;
 	lifeline = Integer.toString(life) + "/" + Integer.toString(life_max);
       }
     }
     void set_size_48() {
       w = h = 48;
       midw = midh = 24;
     }
     void set_size_64() {
       w = h = 64;
       midw = midh = 32;
     }
     void start_life(int n) {
       life_max = n;
       dead = false;
       heal_full();
     }
     void heal_full() {
       life = life_max;
       lifeline = Integer.toString(life) + "/" + Integer.toString(life);
     }
     void die() {
       dead = true;
       lifeline = "Dead";
     }
     boolean contains(float xf, float yf) {
       int x0 = (int) xf;
       int y0 = (int) yf;
       return x0 + SLOP >= x && x0 < x + w + SLOP &&
 	  y0 + SLOP >= y && y0 < y + h + SLOP;
     }
 
     Bitmap bitmap;
     String name;
     String lifeline;
     int index;
     int x, y;
     int life;
     int life_max;
     int status;
     int target;
     int shield;
     int w, h;
     int midw, midh;
     // For monsters, the player that controls it.
     // In future, if ever we support more than two players, for players it
     // could represent the source of a Charm Person spell. For now we know
     // it must be the other player.
     short controller;
     boolean dead;
     // ID of monster consistent amongst all players in a net game.
     int id;
   }
 
   class BeingPosition {
     BeingPosition(int init_x, int init_y) {
       x = init_x;
       y = init_y;
       being = null;
     }
     int x, y;
     Being being;
   }
 
   // Summoned creatures should appear close to their owner, hence this mess.
   void init_being_pos() {
     int x, y;
     x = 160 - 32;
     y = ylower - 64;
     being_pos = new BeingPosition[16];
     being_pos[0] = new BeingPosition(x - 48 - 10, y);
     being_pos[1] = new BeingPosition(x + 64 + 10, y);
     being_pos[2] = new BeingPosition(x - 48 - 10, y - 48 - 4);
     being_pos[3] = new BeingPosition(x + 64 + 10, y - 48 - 4);
     being_pos[4] = new BeingPosition(x - 2 * 48 - 2 * 10, y);
     being_pos[5] = new BeingPosition(x + 64 + 48 + 2 * 10, y);
     being_pos[6] = new BeingPosition(x - 2 * 48 - 2 * 10, y - 48 - 4);
     being_pos[7] = new BeingPosition(x + 64 + 48 + 2 * 10, y - 48 - 4);
     for (int i = 0; i < 8; i++) {
       being_pos[8 + i] = new BeingPosition(
 	  being_pos[8 - i - 1].x, ylower - 64 - being_pos[8 - i - 1].y + 16);
     }
   }
   void reset_being_pos() {
     for (int i = 0; i < 16; i++) being_pos[i].being = null;
   }
 
   static SpellTap spelltap;
   static int charmed_hand;
   static boolean freeze_gesture;
   static boolean choosing_charm;
   static final int SLOP = 4;
   static Agent agent;
   static final int TILT_AWAIT_UP = 0;
   static final int TILT_AWAIT_DOWN = 1;
   static final int TILT_DISABLED = 2;
 }
