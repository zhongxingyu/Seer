 package ui.scoreboard;
 import model.*;
 
 
 
 import java.awt.event.*;
 
 
 class ScoreBoardKeyListener extends KeyAdapter {
 
     private enum State {
         Entry,
         WaitingForAchievement,
         WaitingForColorToEdit,
         EditingPlayer,
         WaitingForColorToDelete,
         WaitingForColorToAdd
     }
 
     private Game game;
     private State state;
     private ScoreBoard board;
     private Achievement achievementToSet;
     private PlayerColor playerEditing;
     private ScoreBoardHelper helper; // Sorry
 
     public ScoreBoardKeyListener(Game game, ScoreBoard board) {
         this.game = game;
         this.board = board;
         this.state = State.Entry;
         this.helper = new ScoreBoardHelper();
     }
 
     public void keyTyped(KeyEvent e) {
         switch (state) {
             case WaitingForAchievement:
                 try {
                     if (e.getKeyChar() == '0') {
                 	Player p = game.getAchievement(achievementToSet);
                 	if (p != null)
                 	    p.remove(achievementToSet);
                     }
                     else {
                 	Player p = game.getPlayer(helper.getPlayerColor(Character.toLowerCase(e.getKeyChar())));
                 	if (p != null)
                 	    p.add(achievementToSet);
                     }
                 }
                 catch (Exception ex) {
 
                 }
                 state = State.Entry;
                 board.setState(ScoreBoard.ScoreBoardState.DEFAULT);
             break;
             case WaitingForColorToEdit:
                 playerEditing = helper.getPlayerColor(e.getKeyChar());
                 if (playerEditing != null) {
                     state = State.EditingPlayer;
                     board.setStateEditing(playerEditing);
                 }
                 else {
                     state = State.Entry;
                     board.setState(ScoreBoard.ScoreBoardState.DEFAULT);
                 }
 
             break;
             case EditingPlayer:
                 if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                     state = State.Entry;
                 }
                 else if (Character.isLetterOrDigit(e.getKeyChar())) {
                     Player toRename = game.getPlayer(playerEditing);
                     toRename.setName(toRename.getName() + e.getKeyChar());
                 }
             break;
             case WaitingForColorToAdd:
                 {
                 	try {
                     PlayerColor pc = helper.getPlayerColor(e.getKeyChar());
                     if (pc != null)
                         game.addPlayer(pc);
                     state = State.Entry;
                     board.setState(ScoreBoard.ScoreBoardState.DEFAULT);
                 	}
                 	catch (RulesBrokenException ex) {
                 		// not really worth doing anything here.
                 	}
                 }
             break;
             case WaitingForColorToDelete:
                 {
                     PlayerColor pc = helper.getPlayerColor(e.getKeyChar());
                     if (pc != null) {
                 	Player p = game.getPlayer(pc);
                 	if (p != null) {
                 	    game.removePlayer(p);
                 	}
                     }
                         
                     state = State.Entry;
                     board.setState(ScoreBoard.ScoreBoardState.DEFAULT);
                 }
             break;
             case Entry:
                 char lowerChar = Character.toLowerCase(e.getKeyChar());
                 if (lowerChar == 'a') {
                     state = State.WaitingForAchievement;
                     achievementToSet = Achievement.LargestArmy;
                     board.setStateAchievement(Achievement.LargestArmy);
                 }
                 else if (lowerChar == 'l') {
                     state = State.WaitingForAchievement;
                     achievementToSet = Achievement.LongestRoad;
                     board.setStateAchievement(Achievement.LongestRoad);
                 }
                 else if (lowerChar == 'e') {
                     state = State.WaitingForColorToEdit;
                     board.setState(ScoreBoard.ScoreBoardState.SELECT_PLAYER_TO_EDIT);
                 }
                 else if (e.getKeyChar() == '+') {
                     state = State.WaitingForColorToAdd;
                     board.setState(ScoreBoard.ScoreBoardState.SELECT_PLAYER_TO_ADD);
                 }
                 else if (e.getKeyChar() == '-') {
                     state = State.WaitingForColorToDelete;
                     board.setState(ScoreBoard.ScoreBoardState.SELECT_PLAYER_TO_DELETE);
                 }
                 else if (e.getKeyChar() == 'h') {
                     state = State.WaitingForAchievement;
                     achievementToSet = Achievement.HarbourMaster;
                     board.setStateAchievement(Achievement.HarbourMaster);
                     //board.setShowHelp(!board.getShowHelp());
                 }
                 else if (e.getKeyChar() == 'c') {
                     board.setShowColorHelp(!board.getShowColorHelp());
                 }
                 else if (e.getKeyChar() == 'f') {
                 //    board.toggleFullScreen();
                 }
                 else if (e.getKeyChar() == 's') {
                     board.stop();
                 }
                 else {
                     try {
                         // Lower case = +1 VP, Upper Case = -1 VP
                         int updateAmount = Character.isLowerCase(e.getKeyChar()) ? 1 : -1;
 
                         // This will throw if e.getKeyChar isn't a character which maps to a color
                         Player p = game.getPlayer(helper.getPlayerColor(e.getKeyChar()));
 
                         if (game.getWinner() == null || game.getWinner() == p) {
                            p.setVP(p.getVP() + updateAmount);
                         }
                     }
                     catch (Exception ex) {
                     }
                 }
             break;
         }
 
 
     }
 
     public void keyPressed(KeyEvent e) {
         switch (state) {
             case EditingPlayer:
                 if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                     if (game.getPlayer(playerEditing).getName().isEmpty()) {
                         game.getPlayer(playerEditing).resetName();
                     }
                     state = State.Entry;
                     board.setState(ScoreBoard.ScoreBoardState.DEFAULT);
                 }
                 else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                     String old_name = game.getPlayer(playerEditing).getName();
                     if (old_name.length() > 0) {
                         game.getPlayer(playerEditing).setName(old_name.substring(0, old_name.length() -1));
                     }
                 }
         }
     }
 }
