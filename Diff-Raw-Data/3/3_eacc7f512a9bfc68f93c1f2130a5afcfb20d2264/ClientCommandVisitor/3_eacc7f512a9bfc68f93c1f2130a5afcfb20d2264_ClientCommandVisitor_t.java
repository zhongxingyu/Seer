 package risk.protocol;
 
 import risk.common.Logger;
 import risk.game.Attack;
 import risk.game.Country;
 import risk.game.CountryPair;
 import risk.game.GameController;
 import risk.game.GameView;
 import risk.game.Player;
 import risk.game.RoundPhase;
 import risk.protocol.command.*;
 
 public class ClientCommandVisitor implements CommandVisitor {
     private GameView gameView;
     private GameController gameCtrl;
 
     public ClientCommandVisitor(GameView gameView, GameController gameCtrl) {
         super();
         this.gameView = gameView;
         this.gameCtrl = gameCtrl;
     }
 
     @Override
     public void visit(HelloCmd cmd) {
         Logger.logwarn("Client shouldn't receive HelloCmd");
     }
 
     @Override
     public void visit(PlayerJoinedCmd cmd) {
         Logger.logdebug("Got PlayerJoinedCmd: " + cmd.getPlayer().getName() + (cmd.isControlledByMe() ? " It's me!" : ""));
         gameCtrl.addPlayer(cmd.getPlayer());
         if (cmd.isControlledByMe()) {
             gameCtrl.setMyPlayer(cmd.getPlayer());
         }
         
     }
 
     @Override
     public void visit(GameStartedCmd cmd) {
         Logger.logdebug("Game started");
     }
 
     @Override
     public void visit(NextRoundCmd cmd) {
         Logger.logdebug("New round started");
         gameCtrl.setRoundPlayers(cmd.getPlayers());
         gameCtrl.setRoundPhases(cmd.getRoundPhases());
     }
     
     @Override
     public void visit(NextPhaseCmd cmd) {
         if (!gameCtrl.swicthToNextPhase()) {
             // TODO: error handling
             Logger.logerror("Cannot switch phase!");
             return;
         }
         RoundPhase phase = gameView.getRoundPhase();
         Logger.logdebug("Switched to phase: " + phase.toString());
         if (phase == RoundPhase.REINFORCEMENT) {
             gameCtrl.setAvailableReinforcement(cmd.getReinforcement());
             Logger.logdebug("Got " + cmd.getReinforcement() + "reinforcement");
         }
     }
 
     @Override
     public void visit(CountyInitCmd cmd) {
         Logger.logdebug("Got CountryInitCmd: " + cmd.getCountry().getName() + " to " + cmd.getCountry().getOwner().getName());
         Country c = cmd.getCountry();
         gameCtrl.initCountry(c.getName(), c.getOwner().getName(), c.getTroops());
     }
 
     @Override
     public void visit(GameEndedCmd cmd) {
         String reason = cmd.getReason() == GameEndedCmd.WIN ? "won" : "quit";
         Logger.logdebug("Got GameEnded command: " + cmd.getPlayer().getName() + " has " + reason);
     }
 
     @Override
     public void visit(DoAttackCmd cmd) {
         WrongCommand(cmd);
         
     }
 
     @Override
     public void visit(PlaceReinforcementCmd cmd) {
         Country c = gameView.getCountry(cmd.getCountry().getName());
         Logger.logdebug("Player " + cmd.getPlayer().getName() + " placed " + cmd.getTroops() + " units to country " + cmd.getCountry().getName());
         gameCtrl.setAvailableReinforcement(gameView.getAvailableReinforcement() - cmd.getTroops());
         gameCtrl.addTroopsToCountry(c, cmd.getTroops());
     }
     
     private void WrongCommand(Command cmd) {
         Logger.logwarn("Clienet shouldn't receive " + cmd.toString());
     }
 
     @Override
     public void visit(NextPlayerCmd nextPlayerCmd) {
         Logger.logdebug("Got NextPlayerCmd!");
         if (!gameCtrl.switchToNextPlayer()) {
             Logger.logerror("No more player in NextPlayerCmd handler!");
         }
         Logger.logdebug("Current player is: " + gameView.getCurrentPlayer().getName());
         gameCtrl.resetPhases();
     }
 
     @Override
     public void visit(RegroupCmd cmd) {
         Country from = gameView.getCountry(cmd.getCountryPair().From.getName());
         Country to = gameView.getCountry(cmd.getCountryPair().To.getName());
         Logger.logdebug("Got regroup command " + from.getName() + " -> " + to.getName() + " : " + cmd.getTroops());
         CountryPair cp = new CountryPair(from, to);
         gameCtrl.regroup(cp, cmd.getTroops());
     }
 
     @Override
     public void visit(AttackStartCmd cmd) {
         Country from = gameView.getCountry(cmd.getCountryPair().From.getName());
         Country to = gameView.getCountry(cmd.getCountryPair().To.getName());
         Logger.logdebug("Got AttackStartCmd " + from.getName() + " -> " + to.getName());
         gameCtrl.setAttack(new Attack(new CountryPair(from, to)));
     }
 
     @Override
     public void visit(AttackSetADiceCmd cmd) {
         int dice = cmd.getADice();
         Logger.logdebug("Got attackSetADiceCmd: " + dice);
         gameCtrl.setAttackADice(dice);
     }
 
     @Override
     public void visit(AttackSetDDiceCmd cmd) {
         int dice = cmd.getDDice();
         Logger.logdebug("Got attackSetDDiceCmd: " + dice);
         gameCtrl.setAttackDDice(dice);        
     }
 
     @Override
     public void visit(AttackRoundResultCmd cmd) {
         Logger.logdebug("Got AttackResultCmd");
         gameCtrl.setAttackRoundResults(cmd.getADiceResults(), cmd.getDDiceResults());
        Attack attack = gameView.getAttack();
        int[] losses = attack.calcLosses();
        gameCtrl.accountAttackLosses(losses[0], losses[1]);
     }
 
     @Override
     public void visit(AttackRetreatCmd cmd) {
         gameCtrl.clearAttack();
     }
 
     @Override
     public void visit(EndTurnCmd cmd) {
         WrongCommand(cmd);
     }
 }
