 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.juhokall.telesina.game.action;
 
 import com.juhokall.telesina.model.Player;
 import com.juhokall.telesina.model.Situation;
 import com.juhokall.telesina.model.Solution;
 import com.juhokall.telesina.model.core.Telesina;
 
 /**
  *
  * @author juho
  */
 public class Bet implements TelesinaGameAction {
 
 	@Override
 	public Boolean solve(Situation situation, Solution solution) {
 		int betSize = solution.getSolutionSize();
 		if (betSize >= Telesina.DEFAULT_ANTE && betSize <= situation.getPotSize()) {
 			Player player = situation.getActivePlayer();
 			player.removeFromStack(betSize);
			situation.decreasePlayersLeft();
 			situation.addToPot(betSize);
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
