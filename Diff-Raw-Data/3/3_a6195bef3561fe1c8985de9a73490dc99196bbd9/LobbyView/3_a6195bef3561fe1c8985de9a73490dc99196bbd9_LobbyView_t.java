 package com.benlynn.spelltapper;
 
 import android.util.Log;
 import android.graphics.Canvas;
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.MotionEvent;
 
 public class LobbyView extends View {
   // Constructor.
   public LobbyView(Context context, AttributeSet attrs) {
     super(context, attrs);
     duel = new String[32];
     duel_level = new int[32];
     duel_count = 0;
     username = new String[32];
     userstate = new int[32];
     userarg = new String[32];
     user_count = 0;
     selection = null;
     selection_i = -1;
   }
 
   void set_list(String s) {
     if ('\n' == s.charAt(0)) {
       int j = s.indexOf('\n', 1);
       if (j < 0) return;
       Fry.oppname = s.substring(1, j);
       Fry.duelid = s.substring(j + 1, s.length());
       Lobby.duel0();
       return;
     }
     int i = 0;
     duel_count = 0;
     user_count = 0;
     selection_i = -1;
     for(;;) {
       int j = s.indexOf('\n', i);
       if (j < 0) break;
       username[user_count] = s.substring(i, j);
       i = j + 1;
       j = s.indexOf('\n', i);
       if (j < 0) break;
       userstate[user_count] = s.charAt(i) - '0';
       i = j + 1;
       j = s.indexOf('\n', i);
       if (j < 0) break;
       userarg[user_count] = s.substring(i, j);
       i = j + 1;
       if (1 == userstate[user_count]) {
 	duel[duel_count] = username[user_count];
 	duel_level[duel_count] = userarg[user_count].charAt(0) - '0';
 	if (duel[duel_count].equals(selection)) {
 	  selection_i = duel_count;
 	}
 	duel_count++;
       } else {
 	user_count++;
       }
       if (32 == user_count) break;
     }
     invalidate();
   }
 
   @Override
   public void onDraw(Canvas canvas) {
     super.onDraw(canvas);
     int x, y;
 
     for (int i = 0; i < user_count; i++) {
       canvas.drawText(username[i], 180, 20 + i * 40, Easel.white_text);
       String s = "???";
       switch(userstate[i]) {
 	case 9:
 	  s = "fleeing duel";
 	  break;
 	case 0:
 	  s = "loitering";
 	  break;
 	case 2:
 	case 3:
 	case 4:
 	  s = "dueling " + userarg[i];
 	  break;
       }
       canvas.drawText(s, 180, 40 + i * 40, Easel.grey_text);
     }
 
     x = 0;
     y = 0;
     String s;
 
     for (int i = 0; i < duel_count; i++) {
       if (selection_i == i) {
 	canvas.drawRect(x, y, x + 160, y + 50, Easel.sel_paint);
 	if (selection.equals(Fry.username)) {
 	  canvas.drawRect(0, MainView.ystatus, 320, 480, Easel.wait_paint);
 	  canvas.drawText("(your duel)", 160, MainView.ystatus + 36, Easel.tap_ctext);
 	} else if (duel_level[selection_i] <= Player.true_level) {
 	  canvas.drawRect(0, MainView.ystatus, 320, 480, Easel.reply_paint);
 	  canvas.drawText("Accept duel!", 160, MainView.ystatus + 36, Easel.tap_ctext);
 	} else {
 	  canvas.drawRect(0, MainView.ystatus, 320, 480, Easel.wait_paint);
 	  canvas.drawText("(level too low for this duel)", 160, MainView.ystatus + 36, Easel.tap_ctext);
 	}
       }
       canvas.drawRect(x + 1, y + 1, x + 160 - 1, y + 50 - 1, Easel.octarine);
       canvas.drawText(duel[i], x + 4, y + 20, Easel.white_text);
       s = "Fight me! (level " + duel_level[i] + ")";
       canvas.drawText(s, x + 4, y + 40, Easel.grey_text);
       y += 50;
     }
 
     if (has_created_duel) return;
     if ("" == selection) {
       canvas.drawRect(x, y, x + 160, y + 50, Easel.sel_paint);
 
       canvas.drawRect(0, MainView.ystatus, 320, 480, Easel.reply_paint);
       canvas.drawText("Level:", 0, MainView.ystatus - 25, Easel.white_text);
       for (int i = Player.true_level; i >= 1; i--) {
 	int bx = 8 + i * 50;
 	int by = MainView.ystatus - 64;
 	if (Player.level == i) {
 	  canvas.drawRect(bx, by, bx + 50, by + 50, Easel.sel_paint);
 	}
 	canvas.drawRect(bx + 1, by + 1, bx + 48, by + 48, Easel.octarine);
 	canvas.drawText("" + i, bx + 25, by + 32, Easel.white_ctext);
       }
       canvas.drawText("Issue challenge!", 160, MainView.ystatus + 36, Easel.tap_ctext);
     }
     canvas.drawRect(x + 1, y + 1, x + 160 - 1, y + 50 - 1, Easel.octarine);
     canvas.drawText("Create Duel", x + 80, y + 32, Easel.white_ctext);
   }
 
   @Override
   public boolean onTouchEvent(MotionEvent event) {
     switch (event.getAction()) {
       case MotionEvent.ACTION_DOWN:
 	float x0 = event.getX();
 	float y0 = event.getY();
 	if (y0 > MainView.ystatus) {
 	  is_low_tap = true;
 	  return true;
 	}
 	is_low_tap = false;
 
 	int bx = 8;
 	int by = MainView.ystatus - 64;
 	if (y0 >= by && y0 <= by + 50) {
 	  int n = (int) ((x0 - bx) / 50);
 	  if (n >= 1 && n <= Player.true_level) {
 	    Player.set_level(n);
 	    invalidate();
 	    return true;
 	  }
 	}
 
 	if (x0 <= 160) {
 	  selection_i = (int) (y0 / 50);
 	  if (selection_i < duel_count) {
 	    selection = duel[selection_i];
 	  } else if (selection_i == duel_count) {
 	    selection = "";
 	  }
 	  invalidate();
 	  return true;
 	}
 
 	return true;
       case MotionEvent.ACTION_UP:
 	float x1 = event.getX();
 	float y1 = event.getY();
 	if (is_low_tap && y1 > MainView.ystatus) {
 	  if ("" == selection) {
 	    if (!has_created_duel) {
 	      has_created_duel = true;
 	      Lobby.create_duel(Player.level);
 	      invalidate();
 	    }
	  } else if (null != selection && 
	      !selection.equals(Fry.username) &&
 	      duel_level[selection_i] <= Player.true_level) {
 	    Player.set_level(duel_level[selection_i]);
 	    Lobby.accept_duel(selection);
 	  }
 	}
 	return true;
     }
     return false;
   }
 
   static String[] username;
   static int[] userstate;
   static String[] userarg;
   static int user_count;
   static String[] duel;
   static int[] duel_level;
   static int duel_count;
   static String selection;
   static int selection_i;
   static boolean has_created_duel;
   static boolean is_low_tap;
 }
