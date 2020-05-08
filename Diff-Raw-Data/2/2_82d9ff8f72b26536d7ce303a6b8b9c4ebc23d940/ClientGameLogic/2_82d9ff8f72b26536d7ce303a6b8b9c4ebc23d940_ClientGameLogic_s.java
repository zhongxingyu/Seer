 package risk.game.client;
 
 import risk.common.Logger;
 import risk.game.*;
 import risk.network.QueuedSender;
 import risk.protocol.command.*;
 
 public class ClientGameLogic implements Controller {
     private GameView gameView;
     private GameController gameCtrl;
     private View view;
     private QueuedSender sender;
 
     public ClientGameLogic(GameView gameView, GameController gameCtrl) {
         super();
         this.gameView = gameView;
         this.gameCtrl = gameCtrl;
     }
 
     public void setSender(QueuedSender sender) {
         this.sender = sender;
     }
 
     @Override
     public void onCountryClick(String country) {
         if (gameView.getCurrentPlayer() != gameView.getMyPlayer()) {
             return;
         }
         
         // Get some data to work eith
         Country c = gameView.getCountry(country);
         RoundPhase phase = gameView.getRoundPhase();
         RoundPhase nextPhase = gameView.getNextRoundPhase();
 
         Logger.logdebug("Country \"" + c.getName() + "\" clicked");
         
         // If in reinforcement phase, no second click needed
         if (phase == RoundPhase.REINFORCEMENT) {
             view.showReinforcementDialog(c, gameView.getAvailableReinforcement());
         } else {
             Country selected;
             // If this is a second click, on a selected country, unselect it
             if (c.isSelected()) {
                 Logger.logdebug("Unselecting country " + c.getName());
                 gameCtrl.cancelCountrySelection(c);
             } else if ((selected = gameView.getSelectedCountryNeighbour(c)) != null) {
                 // If this is a second click on a neighbour country, then action is needed
                 Logger.logdebug("Action needed, current round phase: " + phase.toString());
                 // If the selected country is enemy, then attack
                 if (phase == RoundPhase.ATTACK && c.getOwner() != gameView.getMyPlayer()) {
                     if (gameView.getAttack() != null) {
                         Logger.logerror("CurrentAttack not null, when initiating new attack!");
                     }
                     CountryPair cp = new CountryPair(selected, c);
                     Logger.logdebug("Attacking from " + selected.getName() + " to " + c.getName());
                     sender.queueForSend(new AttackStartCmd(cp));
                 } else if ((phase == RoundPhase.REGROUP || nextPhase == RoundPhase.REGROUP) && c.getOwner() == gameView.getMyPlayer()) {
                     // If second click is on my country, then regroup
                     Logger.logdebug("Regrouping from " + selected.getName() + " to " + c.getName());
                     view.showRegroupDialog(new CountryPair(selected, c));
                 } else {
                     // Something went wrong
                     Logger.logwarn("Invalid game phase!");
                 }
                 // After action unselect all countries.
                 gameCtrl.cancelCountryNeighbourSelection(c);
             } else {
                 // Otherwise select country
                 Logger.logdebug("Selecting country " + c.getName());
                 gameCtrl.selectCountry(c);
             }
         }
     }
 
     @Override
     public GameView getGameView() {
         return gameView;
     }
 
     public void setView(View view) {
         this.view = view;
     }
 
     @Override
     public boolean onReinforcementDialogOK(Country c, int troops) {
         int availableReinforcement = gameView.getAvailableReinforcement();
         if (availableReinforcement >= troops) {
             sender.queueForSend(new PlaceReinforcementCmd(c, troops, null));
             return true;
         }
         return false;
     }
 
     @Override
     public boolean onRegroupDialogOk(CountryPair cp, int troops) {
         if (troops > 0 && troops < cp.From.getTroops()) {
             sender.queueForSend(new RegroupCmd(cp, troops));
             return false;
         }
         return true;
     }
 
     @Override
     public void onEndTurnClick() {
         if (gameView.getCurrentPlayer() != gameView.getMyPlayer() || gameView.getAvailableReinforcement() > 0) {
             // TODO: error handling
             Logger.logdebug("Cannot end turn right now");
             return;
         }
         sender.queueForSend(new EndTurnCmd());
     }
 
     @Override
     public boolean onAttackRetreat() {
         Attack attack = gameView.getAttack();
         if (attack.getCountryPair().From.getOwner() != gameView.getMyPlayer() || attack.getAttackerDice() > 0) {
             // TODO error handling
             Logger.logdebug("Attack etreat not possible");
             return true;
         }
         sender.queueForSend(new AttackRetreatCmd());
         return false;
     }
 
     @Override
     public boolean onAttack_AttackerChose(int attackerDice) {
         Attack attack = gameView.getAttack();
         Country from = attack.getCountryPair().From;
         if (from.getOwner() != gameView.getMyPlayer() || from.getTroops() <= attackerDice ||
                 attackerDice > 3 || attackerDice < 1)
         {
             // TODO error handling
             Logger.logdebug("Attack etreat not possible");
             return true;
         }
         sender.queueForSend(new AttackSetADiceCmd(attackerDice));
         return false;
     }
 
     @Override
     public boolean onAttack_DefenderChose(int defenderDice) {
         Attack attack = gameView.getAttack();
         Country to = attack.getCountryPair().To;
         if (to.getOwner() != gameView.getMyPlayer() || to.getTroops() < defenderDice ||
                 defenderDice > 2 || defenderDice < 1)
         {
             // TODO error handling
             Logger.logdebug("Attack etreat not possible");
             return true;
         }
        sender.queueForSend(new AttackSetADiceCmd(defenderDice));
         return false;
     }
 }
