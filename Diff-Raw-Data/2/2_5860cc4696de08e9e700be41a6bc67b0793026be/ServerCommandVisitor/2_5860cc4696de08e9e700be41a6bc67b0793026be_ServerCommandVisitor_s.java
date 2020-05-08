 package risk.protocol;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Random;
 
 import risk.common.Logger;
 import risk.game.*;
 import risk.game.server.*;
 import risk.protocol.command.*;
 
 public class ServerCommandVisitor implements CommandVisitor {
     private ClientHandler clientHandler;
     private CommandSender cmdSender;
     private GameView gameView;
     private GameController gameCtrl;
     private ArrayList<Color> colors;
 
     public ServerCommandVisitor(CommandSender cmdSender, GameView gameView, GameController gameCtrl, ClientHandler clientHandler) {
         this.gameView = gameView;
         this.gameCtrl = gameCtrl;
         this.cmdSender = cmdSender;
         this.clientHandler = clientHandler;
         
         colors = new ArrayList<Color>();
         colors.add(new Color(200, 0, 0)); // Red
         colors.add(new Color(0, 0, 200)); // Blue
         colors.add(new Color(0, 200, 0)); // Green
         colors.add(new Color(200, 200, 0)); // Yellow
         colors.add(new Color(200, 0, 200)); // Magenta
         colors.add(new Color(0, 200, 200)); // Cyan
     }
 
     @Override
     public void visit(HelloCmd cmd) {
         Logger.logdebug("Got HelloCmd from " + cmd.getName());
 
         if (gameView.getPlayer(cmd.getName()) != null) {
             // A player with this name is connectected
             Logger.logwarn("Name is already in use");
             sendError(ErrorCmd.NAME_ALREADY_USED);
             return;
         }
         
         Player newPlayer = new Player(cmd.getName(), colors.get(gameView.getPlayers().size()));
         clientHandler.setPlayer(newPlayer);
         
         // Get current players and send them to the new player
         Iterable<Player> players = gameView.getPlayers();
         for (Player p : players) {
             clientHandler.queuePlayerJoinedForSend(p);
         }
 
         // Create new player and send it to everyone
         
         gameCtrl.addPlayer(newPlayer);
         cmdSender.sendPlayerJoinedCmd(newPlayer);
         
         // Check if all the players are here
         if (gameView.getPlayers().size() == 2) {
             startGame();
         }
     }
 
     private void sendError(int errorCode) {
         clientHandler.queueForSend(new ErrorCmd(errorCode));
     }
 
     @Override
     public void visit(PlayerJoinedCmd cmd) {
         WrongCommand(cmd);
     }
 
     @Override
     public void visit(GameStartedCmd cmd) {
         WrongCommand(cmd);
     }
 
     @Override
     public void visit(NextRoundCmd cmd) {
         WrongCommand(cmd);
     }
 
     @Override
     public void visit(CountyInitCmd cmd) {
         WrongCommand(cmd);
     }
 
     @Override
     public void visit(GameEndedCmd cmd) {
         WrongCommand(cmd);
     }
 
     private void WrongCommand(Command cmd) {
         Logger.logwarn("Server shouldn't receive " + cmd.toString());
     }
 
     private void startGame() {
         // Send game started notification
         cmdSender.sendCmd(new GameStartedCmd(), null);
         
         // Randomly give countries to players
         ArrayList<Country> countries = new ArrayList<Country>(gameView.getCountries());
         Collection<Player> players = gameView.getPlayers();
         
         Random r = new Random();
         Iterator<Player> playerIterator = players.iterator();
         while (!countries.isEmpty()) {
             Logger.logdebug("Countries: " + countries.size());
             // Generate random number
             int rand = r.nextInt(countries.size());
             // Set random country to be owned by the next player
             Country c = countries.get(rand);
             c.setOwner(playerIterator.next());
             Logger.logdebug("Country name: " + c.getName() + " to " + c.getOwner().getName());
             c.setTroops(1);
             cmdSender.sendCmd(new CountyInitCmd(c), null);
             // Remove Country from the 'unowned' countries list
             countries.remove(rand);
             // If the iterator has ended, reset it to start.
             if (!playerIterator.hasNext()) {
                 playerIterator = players.iterator();
             }
         }
         
         initNextRound();
     }
     
     private void initNextRound() {
         Logger.logdebug("Initing next round");
         gameCtrl.setRoundPlayers(gameView.getPlayers());
         gameCtrl.setRoundNumber(gameView.getRoundNumber() + 1);
         LinkedList<RoundPhase> phases = new LinkedList<RoundPhase>();
         switch (gameView.getRoundNumber()) {
         case 1:
             phases.add(RoundPhase.REINFORCEMENT);
             break;
         case 2:
             phases.add(RoundPhase.REGROUP);
             break;
         default:
             phases.add(RoundPhase.REINFORCEMENT);
             phases.add(RoundPhase.ATTACK);
             phases.add(RoundPhase.REGROUP);
             break;
         }
         gameCtrl.setRoundPhases(phases);
         cmdSender.sendCmd(new NextRoundCmd(gameView.getPlayers(), gameView.getRoundPhases()), null);
         gotoNextPlayer();
     }
     
     private void initNextPhase() {
         Logger.logdebug("Initing next phase");
         if (!gameCtrl.swicthToNextPhase()) {
             Logger.logdebug("After last phase");
             gotoNextPlayer();
             return;
         }
         RoundPhase phase = gameView.getRoundPhase();
         switch (phase) {
         case REINFORCEMENT:
             Player p = gameView.getCurrentPlayer();
             int reinforcement = getReinforcement(p);
             gameCtrl.setAvailableReinforcement(reinforcement);
             cmdSender.sendCmd(new NextPhaseCmd(reinforcement), null);
             break;
         case ATTACK:
             cmdSender.sendCmd(new NextPhaseCmd(0), null);
             break;
         case REGROUP:
             cmdSender.sendCmd(new NextPhaseCmd(0), null);
             break;
         default:
             Logger.logerror("INVALID round phase!!");
             break;
         }
     }
     
     private void gotoNextPlayer() {
         Logger.logdebug("Initing next player");
         if (!gameCtrl.switchToNextPlayer()) {
             Logger.logdebug("After last player");
             initNextRound();
             return;
         }
         gameCtrl.resetPhases();
         cmdSender.sendCmd(new NextPlayerCmd(), null);
         initNextPhase();
     }
     
     private int getReinforcement(Player p) {
         if (gameView.getRoundNumber() == 1) {
             int playerno = gameView.getPlayers().size() - 2;
             if (playerno > 4) {
                 playerno = 4;
             }
             return 40 - playerno*5;
         } else {
             Collection<Country> countries = gameView.getCountries();
             int countryno = 0;
             // Count owned territorry
             for (Country c : countries) {
                 if (c.getOwner() == gameView.getCurrentPlayer()) {
                     countryno++;
                 }
             }
             int reinforcement = countryno / 3;
             
             // Count continents
             Collection<Continent> continents = gameView.getContinents();
             Player myPlayer = gameView.getMyPlayer();
             for (Continent continent : continents) {
                 boolean ownedEveryCountry = true;
                 Collection<Country> contCountries = continent.getCountries();
                 for (Country c : contCountries) {
                     if (c.getOwner() != myPlayer) {
                         ownedEveryCountry = false;
                         break;
                     }
                 }
                 if (ownedEveryCountry) {
                     reinforcement += getContinentReinforcement(continent);
                 }
             }
             if (reinforcement < 3) {
                 reinforcement = 3;
             }
             return reinforcement;
         }
     }
 
     private int getContinentReinforcement(Continent continent) {
         if (continent.getName() == Continent.ASIA) {
             return 7;
         } else if (continent.getName() == Continent.AFRICA) {
             return 3;
         } else if (continent.getName() == Continent.EUROPE) {
             return 5;
         } else if (continent.getName() == Continent.AUSTRALIA) {
             return 2;
         } else if (continent.getName() == Continent.NORTHAMERICA) {
             return 5;
         } else if (continent.getName() == Continent.SOUTHAMERICA) {
             return 2;
         } 
         return 0;
     }
 
     @Override
     public void visit(PlaceReinforcementCmd cmd) {
         Country country = gameView.getCountry(cmd.getCountry().getName());
         Logger.logdebug("Got PlaceReinforcement command to " + country.getName() + " (" + cmd.getTroops() + ")");
         if (clientHandler.getPlayer() != gameView.getCurrentPlayer()) {
             Logger.logwarn(clientHandler.getPlayer().getName() + " is trying to place reinforcement in someone else's turn " + gameView.getCurrentPlayer().getName());
             sendError(ErrorCmd.ILLEGAL_ARGUMENT);
             return;
         }
         
         if (cmd.getTroops() < 0 || cmd.getTroops() > gameView.getAvailableReinforcement()) {
             Logger.logwarn(clientHandler.getPlayer().getName() + " is trying to place " + cmd.getTroops() + " reinforcement, available is " + gameView.getAvailableReinforcement());
             sendError(ErrorCmd.ILLEGAL_ARGUMENT);
             return;
         }
         
         if (gameView.getCountry(cmd.getCountry().getName()).getOwner() != clientHandler.getPlayer()) {
             Logger.logwarn(clientHandler.getPlayer().getName() + " is trying to place reinforcement to " + country.getName() + " (" + country.getOwner().getName() + ")");
             sendError(ErrorCmd.ILLEGAL_ARGUMENT);
             return;
         }
         
         gameCtrl.setAvailableReinforcement(gameView.getAvailableReinforcement() - cmd.getTroops());
         gameCtrl.addTroopsToCountry(country, cmd.getTroops());
         cmdSender.sendCmd(new PlaceReinforcementCmd(country, cmd.getTroops(), gameView.getCurrentPlayer()), null);
         
         if (gameView.getAvailableReinforcement() == 0) {
             initNextPhase();
         }
     }
 
     @Override
     public void visit(NextPhaseCmd cmd) {
         WrongCommand(cmd);
     }
 
     @Override
     public void visit(NextPlayerCmd cmd) {
         WrongCommand(cmd);
     }
 
     @Override
     public void visit(RegroupCmd cmd) {
         Country from = gameView.getCountry(cmd.getCountryPair().From.getName());
         Country to = gameView.getCountry(cmd.getCountryPair().To.getName());
         Logger.logdebug("Got regroup command " + from.getName() + " -> " + to.getName() + " : " + cmd.getTroops());
         if (from.getOwner() != to.getOwner() || from.getOwner() != clientHandler.getPlayer()) {
             Logger.logerror("Invalid regroup command, owners do not match");
             sendError(ErrorCmd.ILLEGAL_ARGUMENT);
             return;
         }
         if (!((gameView.getRoundPhase() == RoundPhase.ATTACK && gameView.getNextRoundPhase() == RoundPhase.REGROUP) ||
                 gameView.getRoundPhase() == RoundPhase.REGROUP))
         {
             Logger.logerror("Regroup in wrong game phase!");
             sendError(ErrorCmd.INVALID_PHASE);
             return;
         }
         Attack lastAttack = gameView.getLastAttack();
        if ((from != lastAttack.getCountryPair().From || to != lastAttack.getCountryPair().To) &&
                 gameView.getRoundPhase() != RoundPhase.REGROUP)
         {
             initNextPhase();
         }
         CountryPair cp = new CountryPair(from, to);
         gameCtrl.regroup(cp, cmd.getTroops());
         cmdSender.sendCmd(new RegroupCmd(cp, cmd.getTroops()), null);
     }
 
     @Override
     public void visit(AttackStartCmd cmd) {
         Country from = gameView.getCountry(cmd.getCountryPair().From.getName());
         Country to = gameView.getCountry(cmd.getCountryPair().To.getName());
         
         Logger.logdebug("Got AttackStart command " + from.getName() + " -> " + to.getName());
         
         if (from.getOwner() != clientHandler.getPlayer() || to.getOwner() == clientHandler.getPlayer()) {
             Logger.logdebug("The owner of From or To countries are wrong");
             sendError(ErrorCmd.ILLEGAL_ARGUMENT);
             return ;
         }
         
         if (gameView.getRoundPhase() != RoundPhase.ATTACK) {
             Logger.logerror("Attack start in wrong phase! (" + gameView.getRoundPhase() + ")");
             sendError(ErrorCmd.INVALID_PHASE);
             return;
         }
         
         CountryPair cp = new CountryPair(from, to);
         gameCtrl.setAttack(new Attack(cp));
         cmdSender.sendCmd(new AttackStartCmd(cp), null);
     }
 
     @Override
     public void visit(AttackSetADiceCmd cmd) {
         int dice = cmd.getADice();
         Logger.logdebug("Got attackSetADiceCmd: " + dice);
         Country from = gameView.getAttack().getCountryPair().From;
         if (!(dice >= 1 && dice <= 3 && dice < from.getTroops())) {
             Logger.logdebug("Too many (" + dice + ") adice! (troops: " + from.getTroops() + ")");
             sendError(ErrorCmd.ILLEGAL_ARGUMENT);
             return;
         }
         gameCtrl.setAttackADice(dice);
         cmdSender.sendCmd(new AttackSetADiceCmd(dice), null);
     }
 
     @Override
     public void visit(AttackSetDDiceCmd cmd) {
         int dice = cmd.getDDice();
         Logger.logdebug("Got attackSetDDiceCmd: " + dice);
         
         if (gameView.getAttack().getAttackerDice() < 1) {
             Logger.logerror("Defender cannot choose dice number before attacker does!");
             sendError(ErrorCmd.INVALID_PHASE);
             return;
         }
         
         Country to = gameView.getAttack().getCountryPair().To;
         if (!(dice >= 1 && dice <=2 && dice <= to.getTroops())) {
             Logger.logdebug("Too many (" + dice + ") ddice! (troops: " + to.getTroops() + ")");
             sendError(ErrorCmd.ILLEGAL_ARGUMENT);
             return;
         }
         gameCtrl.setAttackDDice(dice);
         cmdSender.sendCmd(new AttackSetDDiceCmd(dice), null);
         doAttack();
     }
 
     private void doAttack() {
         Logger.logdebug("Doing attack");
         Attack attack = gameView.getAttack();
         Random r = new Random();
         
         ArrayList<Integer> aDice = new ArrayList<Integer>();
         for (int i = 0; i < attack.getAttackerDice(); i++) {
             Integer rand = r.nextInt(6) + 1; 
             aDice.add(rand);
             Logger.logdebug("Attacker thrown " + rand);
         }
         
         ArrayList<Integer> dDice = new ArrayList<Integer>();
         for (int i = 0; i < attack.getDefenderDice(); i++) {
             Integer rand = r.nextInt(6) + 1; 
             dDice.add(rand);
             Logger.logdebug("Defender thrown " + rand);
         }
         
         gameCtrl.setAttackRoundResults(aDice, dDice);
         int[] losses = attack.calcLosses();
         Logger.logdebug("Attacker lost: " + losses[0] + " defender lost: " + losses[1]);
         cmdSender.sendCmd(new AttackRoundResultCmd(aDice, dDice), null);
     }
 
     @Override
     public void visit(AttackRoundResultCmd cmd) {
         WrongCommand(cmd);
     }
 
     @Override
     public void visit(AttackRetreatCmd cmd) {
         Logger.logdebug("Got AttackRetreat command");
         gameCtrl.clearAttack();
         cmdSender.sendCmd(new AttackRetreatCmd(), null);
     }
 
     @Override
     public void visit(EndTurnCmd cmd) {
         gotoNextPlayer();
     }
 
     @Override
     public void visit(ErrorCmd cmd) {
         // Client errors ignored yet
         WrongCommand(cmd);
     }
 }
