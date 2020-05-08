 package risk.game;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import risk.common.Logger;
 
 public class Game implements GameView, GameController {
     /**
      * The map that represent the current state of the world.
      */
     private Map map = new Map();
 
     /**
      * A list of players.
      */
     private LinkedList<Player> players = new LinkedList<Player>();
     private Iterator<Player> currentPlayerIterator;
     private LinkedList<Player> roundPlayers;
 
     /**
      * A list of observers for the Observer design pattern.
      */
     private ArrayList<Observer> observers=new ArrayList<Observer>();
 
     private LinkedList<MessageListener> msgListeners = new LinkedList<MessageListener>();
     
     /**
      * Indicates in which phase the current round is. May be null.
      * Eg. reinforcement phase, attack phase, regroup phase
      */
     private RoundPhase currentRoundPhase;
     
     /**
      * Indicates the next RoundPhase. May be null.
      */
     private RoundPhase nextRoundPhase;
     
     /**
      * The number of troops that can be assigned to countries at the beginning
      * of a new round as reinforcement.
      */
     private int availableReinforcement;
 
     /**
      * The player controlled by this client
      */
     private Player myPlayer;
     
     /**
      * The player currently acting. 
      */
     private Player currentPlayer;
     
     /**
      * The current round number
      */
     private int roundNumber;
     
     /**
      * The round phases in this current round
      */
     private LinkedList<RoundPhase> roundPhases;
     private Iterator<RoundPhase> roundPhasesIterator;
     
     private Attack attack;
     private Attack lastAttack;
 
     private boolean gameStarted = false;
 
     public void addPlayer(Player p) {
         players.add(p);
 
         modelChanged();
     }
     
     @Override
     public void setMyPlayer(Player p) {
         myPlayer = p;
         modelChanged();
     }
 
     @Override
     public Player getMyPlayer() {
         return myPlayer;
     }
 
     public Collection<Player> getPlayers() {
         return players;
     }
     
     public Player getPlayer(String Name) {
         for (Player p : players) {
             if (p.getName().compareTo(Name) == 0) {
                 return p;
             }
         }
         return null;
     }
 
     @Override
     public void setRoundPlayers(Collection<Player> players) {
         this.currentPlayerIterator = null;
         this.roundPlayers = new LinkedList<Player>();
         for (Player p : players) {
             this.roundPlayers.add(getPlayer(p.getName()));
         }
     }
     
     @Override
     public Country getCountry(String countryName) {      
         return map.getCountry(countryName);
     }
 
     @Override
     public Collection<Continent> getContinents() {
         return map.getContinents();
     }
     
     @Override
     public Collection<Country> getCountries() {
         ArrayList<Country> countries = new ArrayList<Country>();
         Collection<Continent> continets = getContinents();
         for (Continent c : continets) {
             countries.addAll(c.getCountries());
         }
         return countries;
     }
 
     @Override
     public void initCountry(String countryName, String ownerName, int troops) {
         Country c = getCountry(countryName);
         Player newOwner = getPlayer(ownerName);
         c.setOwner(newOwner);
         c.setTroops(troops);
         
         modelChanged();
     }
     
     @Override
     public void registerObserver(Observer o) {
         observers.add(o);
         modelChanged();
     }
 
     private void modelChanged(){
         for(Observer o : observers){
             o.refresh(this);
         }
     }
 
     @Override
     public void registerMsgListener(MessageListener listener) {
         msgListeners.add(listener);
     }
     
     @Override
     public Collection<MessageListener> getMessageListeners() {
         return msgListeners;
     }
     
     @Override
     public Color getCountryColor(String countryName) {
         Country c = getCountry(countryName);
         if (c != null && c.getOwner() != null) {
             return c.getOwner().getColor();
         }
         return null;
     }
 
     @Override
     public int getCountryTroops(String countryName) {
         Country c = getCountry(countryName);
         if (c != null) {
             return c.getTroops();
         }
         return 0;
     }
 
     @Override
     public void cancelCountrySelection(Country c) {
         map.cancelCountrySelection(c);
         modelChanged();
     }
 
     @Override
     public void selectCountry(Country c) {
         map.selectCountry(c);
         modelChanged();
     }
 
     @Override
     public boolean isCountrySelected(String name) {
         Country c = getCountry(name);
         return isCountrySelected(c);
     }
 
     @Override
     public boolean isCountryNeighbourSelected(String name) {
         Country c = getCountry(name);
         return getSelectedCountryNeighbour(c) != null;
     }
     
     @Override
     public boolean isCountrySelected(Country c) {
         return map.isCountrySelected(c);
     }
 
     @Override
     public Country getSelectedCountryNeighbour(Country c) {
         return map.getSelectedCountryNeighbour(c);
     }
 
     @Override
     public void cancelCountryNeighbourSelection(Country c) {
         map.cancelCountryNeighbourSelection(c);
     }
 
     @Override
     public RoundPhase getRoundPhase() {
         return currentRoundPhase;
     }
     
     @Override
     public RoundPhase getNextRoundPhase() {
         return nextRoundPhase;
     }
 
     @Override
     public int getAvailableReinforcement() {
         return availableReinforcement;
     }
 
     @Override
     public void setAvailableReinforcement(int availableReinforcement) {
         this.availableReinforcement = availableReinforcement;
         modelChanged();
     }
 
     @Override
     public void addTroopsToCountry(Country country, int troops) {
         country.setTroops(country.getTroops() + troops);
         modelChanged();
     }
 
     @Override
     public boolean switchToNextPlayer() {
         if (currentPlayerIterator == null) {
             currentPlayerIterator = roundPlayers.iterator();
         }
         if (currentPlayerIterator.hasNext()) {
             currentPlayer = currentPlayerIterator.next();
         } else {
             currentPlayer = null;
         }
         modelChanged();
         return currentPlayer != null;
     }
 
     @Override
     public Player getCurrentPlayer() {
         return currentPlayer;
     }
 
     @Override
     public int getRoundNumber() {
         return roundNumber;
     }
 
     @Override
     public void setRoundNumber(int roundNumber) {
         this.roundNumber = roundNumber;
         modelChanged();
     }
     
     @Override
     public Collection<RoundPhase> getRoundPhases() {
         return roundPhases;
     }
 
     @Override
     public void setRoundPhases(Collection<RoundPhase> roundPhases) {
         this.roundPhases = new LinkedList<RoundPhase>(roundPhases);
         this.currentRoundPhase = null;
         this.roundPhasesIterator = null;
         modelChanged();
     }
     
     @Override
     public void resetPhases() {
         roundPhasesIterator = null;
     }
     
     @Override
     public boolean swicthToNextPhase() {
         if (roundPhasesIterator == null) {
             roundPhasesIterator = roundPhases.iterator();
             if (roundPhasesIterator.hasNext()) {
                 nextRoundPhase = roundPhasesIterator.next();
             } else {
                 nextRoundPhase = null;
             }
         }
         currentRoundPhase = nextRoundPhase;
         if (roundPhasesIterator.hasNext()) {
             nextRoundPhase = roundPhasesIterator.next();
         } else {
             nextRoundPhase = null;
         }
         modelChanged();
         return currentRoundPhase != null;
     }
 
     @Override
     public void regroup(CountryPair countryPair, int troops) {
         Country from = countryPair.From;
         Country to = countryPair.To;
         from.setTroops(from.getTroops() - troops);
         to.setTroops(to.getTroops() + troops);
         modelChanged();
     }
 
     @Override
     public void setAttack(Attack attack) {
         this.attack = attack;
         modelChanged();
     }
     
     @Override
     public Attack getAttack() {
         return attack;
     }
     
     @Override
     public Attack getLastAttack() {
         return lastAttack;
     }
 
     private void accountAttackLosses() {
         if (attack == null) {
             Logger.logerror("Accounting attack losses when no attack is in progress");
             return;
         }
         Country from = attack.getCountryPair().From;
         Country to = attack.getCountryPair().To;
 
         int[] losses = attack.calcLosses();
         int aLosses = losses[0];
         int dLosses = losses[1];
         
         from.setTroops(from.getTroops() - aLosses);
         to.setTroops(to.getTroops() - dLosses);
         
         if (to.getTroops() < 1) {
             to.setOwner(from.getOwner());
             to.setTroops(attack.getAttackerDice());
             clearAttack();
         }
         if (from.getTroops() < 2) {
             clearAttack();
         }
     }
 
     @Override
     public void setAttackDDice(int dice) {
         if (attack == null) {
             Logger.logerror("Adding adice when no attack is in progress");
             return;
         }
         attack.setDefenderDice(dice);
     }
 
     @Override
     public void setAttackADice(int dice) {
         if (attack == null) {
             Logger.logerror("Adding ddice when no attack is in progress");
             return;
         }
         attack.setAttackerDice(dice);
     }
 
     @Override
     public void clearAttack() {
         lastAttack = attack;
         attack = null;
         modelChanged();
     }
 
     @Override
     public void setAttackRoundResults(ArrayList<Integer> aDiceResults, ArrayList<Integer> dDiceResults) {
         if (attack == null) {
             Logger.logerror("Adding dice results when no attack is in progress");
             return;
         }
         attack.setaDiceResults(aDiceResults);
         attack.setdDiceResults(dDiceResults);
         accountAttackLosses();
         if (attack != null) {
             attack.resetDice();
         }
         modelChanged();
     }
 
     @Override
     public boolean isGameStarted() {
         return gameStarted;
     }
     
     public void setGameStarted(boolean gameStarted) {
         this.gameStarted = gameStarted;
     }

 }
