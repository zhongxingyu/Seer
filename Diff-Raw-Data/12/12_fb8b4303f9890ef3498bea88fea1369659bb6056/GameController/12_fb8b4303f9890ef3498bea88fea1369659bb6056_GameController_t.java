 package org.drooms.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import org.drools.KnowledgeBase;
 import org.drools.builder.KnowledgeBuilder;
 import org.drools.builder.KnowledgeBuilderError;
 import org.drooms.api.Collectible;
 import org.drooms.api.Game;
 import org.drooms.api.GameReport;
 import org.drooms.api.Move;
 import org.drooms.api.Player;
 import org.drooms.api.Strategy;
 import org.drooms.impl.logic.CommandDistributor;
 import org.drooms.impl.logic.commands.AddCollectibleCommand;
 import org.drooms.impl.logic.commands.CollectCollectibleCommand;
 import org.drooms.impl.logic.commands.Command;
 import org.drooms.impl.logic.commands.CrashPlayerCommand;
 import org.drooms.impl.logic.commands.DeactivatePlayerCommand;
 import org.drooms.impl.logic.commands.MovePlayerCommand;
 import org.drooms.impl.logic.commands.RemoveCollectibleCommand;
 import org.drooms.impl.logic.commands.RewardSurvivalCommand;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public abstract class GameController implements
         Game<DefaultPlayground, DefaultNode, DefaultEdge> {
 
     protected enum CollectibleType {
 
         CHEAP("cheap"), GOOD("good"), EXTREME("extreme");
 
         private static final String COMMON_PREFIX = "collectible.";
         private static final String PROBABILITY_PREFIX = CollectibleType.COMMON_PREFIX
                 + "probability.";
         private static final String PRICE_PREFIX = CollectibleType.COMMON_PREFIX
                 + "price.";
         private static final String EXPIRATION_PREFIX = CollectibleType.COMMON_PREFIX
                 + "expiration.";
         private final String collectibleName;
 
         CollectibleType(final String propertyName) {
             this.collectibleName = propertyName;
         }
 
         public int getExpiration(final Properties config) {
             final String price = config.getProperty(
                     CollectibleType.EXPIRATION_PREFIX + this.collectibleName,
                     "1");
             return Integer.valueOf(price);
         }
 
         public int getPoints(final Properties config) {
             final String price = config.getProperty(
                     CollectibleType.PRICE_PREFIX + this.collectibleName, "1");
             return Integer.valueOf(price);
         }
 
         public BigDecimal getProbabilityOfAppearance(final Properties config) {
             final String probability = config.getProperty(
                     CollectibleType.PROBABILITY_PREFIX + this.collectibleName,
                     "0.1");
             return new BigDecimal(probability);
         }
     }
 
     private static final Logger LOGGER = LoggerFactory
             .getLogger(GameController.class);
 
     protected static final SecureRandom RANDOM = new SecureRandom();
 
     private final Map<Player, Integer> playerPoints = new HashMap<Player, Integer>();
     private final Map<URL, ClassLoader> strategyClassloaders = new HashMap<URL, ClassLoader>();
 
     private final Map<String, Strategy> strategyInstances = new HashMap<String, Strategy>();
 
     private final Map<Player, Integer> lengths = new HashMap<Player, Integer>();
 
     private final Map<Player, Deque<DefaultNode>> positions = new HashMap<Player, Deque<DefaultNode>>();
 
     private final Map<Collectible, DefaultNode> nodesByCollectible = new HashMap<Collectible, DefaultNode>();
 
     private final Map<DefaultNode, Collectible> collectiblesByNode = new HashMap<DefaultNode, Collectible>();
 
     private final Map<Player, SortedMap<Integer, Move>> decisionRecord = new HashMap<Player, SortedMap<Integer, Move>>();
 
     private void addCollectible(final Collectible c, final DefaultNode n) {
         this.collectiblesByNode.put(n, c);
         this.nodesByCollectible.put(c, n);
     }
 
     private void addDecision(final Player p, final Move m, final int turnNumber) {
         if (!this.decisionRecord.containsKey(p)) {
             this.decisionRecord.put(p, new TreeMap<Integer, Move>());
         }
         this.decisionRecord.get(p).put(turnNumber, m);
     }
 
     private List<Player> constructPlayers(final Properties config,
             final Properties playerConfig) {
         // parse a list of players
         final Map<String, String> playerStrategies = new HashMap<String, String>();
         final Map<String, URL> strategyJars = new HashMap<String, URL>();
         for (final String playerName : playerConfig.stringPropertyNames()) {
             final String strategyDescr = playerConfig.getProperty(playerName);
             final String[] parts = strategyDescr.split("\\Q@\\E");
             if (parts.length != 2) {
                 throw new IllegalArgumentException(
                         "Invalid strategy descriptor: " + strategyDescr);
             }
             final String strategyClass = parts[0];
             URL strategyJar;
             try {
                 strategyJar = new URL(parts[1]);
             } catch (final MalformedURLException e) {
                 throw new IllegalArgumentException(
                         "Invalid URL in the strategy descriptor: "
                                 + strategyDescr, e);
             }
             playerStrategies.put(playerName, strategyClass);
             strategyJars.put(strategyClass, strategyJar);
         }
         // load strategies for players
         final List<Player> players = new ArrayList<Player>();
         int playerNum = 0;
         for (final Map.Entry<String, String> entry : playerStrategies
                 .entrySet()) {
             final String playerName = entry.getKey();
             final String strategyClass = entry.getValue();
             final URL strategyJar = strategyJars.get(strategyClass);
             Strategy strategy;
             try {
                 strategy = this.loadStrategy(strategyClass, strategyJar);
             } catch (final Exception e) {
                 throw new IllegalArgumentException("Failed loading: "
                         + strategyClass, e);
             }
             final KnowledgeBuilder kb = strategy.getKnowledgeBuilder(this
                     .loadJar(strategyJar));
             try {
                 final KnowledgeBase kbase = kb.newKnowledgeBase();
                 players.add(new DefaultPlayer(playerName, this
                         .getCharPerNumber(playerNum), kbase));
                 playerNum++;
             } catch (final Exception ex) {
                 for (final KnowledgeBuilderError error : kb.getErrors()) {
                     GameController.LOGGER.error(error.toString());
                 }
                 throw new IllegalStateException(
                         "Cannot create knowledge base for strategy: "
                                 + strategy.getName(), ex);
             }
         }
         return players;
     }
 
     protected Collectible getCollectible(final DefaultNode n) {
         return this.collectiblesByNode.get(n);
     }
 
     protected Deque<Move> getDecisionRecord(final Player p) {
         final LinkedList<Move> moves = new LinkedList<Move>();
         for (final SortedMap.Entry<Integer, Move> entry : this.decisionRecord
                 .get(p).entrySet()) {
             moves.add(entry.getKey(), entry.getValue());
         }
         return moves;
     }
 
     private char getCharPerNumber(final int number) {
         if (number >= 0 && number < 10) {
             // for first 10 players, we have numbers 0 - 9
             return (char) (48 + number);
         } else if (number > 9 && number < 36) {
             // for next 25 players, we have capital letters
             return (char) (55 + number);
         } else {
             throw new IllegalArgumentException("Invalid number of a player: "
                     + number);
         }
     }
 
     protected int getPlayerLength(final Player p) {
         if (!this.lengths.containsKey(p)) {
             throw new IllegalStateException(
                     "Player doesn't have any length assigned: " + p);
         }
         return this.lengths.get(p);
     }
 
     protected Deque<DefaultNode> getPlayerPosition(final Player p) {
         if (!this.positions.containsKey(p)) {
             throw new IllegalStateException(
                     "Player doesn't have any position assigned: " + p);
         }
         return this.positions.get(p);
     }
 
     private ClassLoader loadJar(final URL strategyJar) {
         if (!this.strategyClassloaders.containsKey(strategyJar)) {
             final ClassLoader loader = URLClassLoader
                     .newInstance(new URL[] { strategyJar }, this.getClass()
                             .getClassLoader());
             this.strategyClassloaders.put(strategyJar, loader);
         }
         return this.strategyClassloaders.get(strategyJar);
     }
 
     private Strategy loadStrategy(final String strategyClass,
             final URL strategyJar) throws Exception {
         if (!this.strategyInstances.containsKey(strategyClass)) {
             final Class<?> clz = Class.forName(strategyClass, true,
                     this.loadJar(strategyJar));
             final Strategy strategy = (Strategy) clz.newInstance();
             this.strategyInstances.put(strategyClass, strategy);
         }
         return this.strategyInstances.get(strategyClass);
     }
 
     protected abstract Map<Collectible, Player> performCollectibleCollection(
             final Collection<Player> players);
 
     protected abstract Map<Collectible, DefaultNode> performCollectibleDistribution(
             final Properties gameConfig, final DefaultPlayground playground,
             final Collection<Player> players, final int currentTurnNumber);
 
     protected abstract Set<Player> performCollisionDetection(
             final DefaultPlayground playground,
             final Collection<Player> currentPlayers);
 
     protected abstract Set<Player> performInactivityDetection(
             final Collection<Player> currentPlayers,
             final int currentTurnNumber, final int allowedInactiveTurns);
 
     protected abstract Deque<DefaultNode> performPlayerMove(
             final Player player, final Move decision);
 
     @Override
     public GameReport<DefaultPlayground, DefaultNode, DefaultEdge> play(
             final Properties gameConfig, final Properties playerConfig) {
         // prepare the playground
         DefaultPlayground playground;
         try {
             playground = DefaultPlayground.read(new File(gameConfig
                     .getProperty("playground.file")));
         } catch (final IOException e) {
             throw new IllegalArgumentException(
                     "Playground file cannot be read!", e);
         }
         // prepare the players
         final List<Player> players = this.constructPlayers(gameConfig,
                 playerConfig);
         final int wormLength = Integer.valueOf(gameConfig.getProperty(
                 "worm.length.start", "1"));
         final int allowedInactiveTurns = Integer.valueOf(gameConfig
                 .getProperty("worm.max.inactive.turns", "3"));
         final int wormSurvivalBonus = Integer.valueOf(gameConfig.getProperty(
                 "worm.survival.bonus", "1"));
         // prepare starting positions
         final List<DefaultNode> startingPositions = playground
                 .getStartingPositions();
         final int playersSupported = startingPositions.size();
         final int playersAvailable = players.size();
         if (playersSupported < playersAvailable) {
             throw new IllegalArgumentException(
                     "The playground doesn't support " + playersAvailable
                             + " players, only " + playersSupported + "! ");
         }
         int i = 0;
         for (final Player player : players) {
             final Deque<DefaultNode> pos = new LinkedList<DefaultNode>();
             pos.push(startingPositions.get(i));
             this.setPlayerPosition(player, pos);
             this.setPlayerLength(player, wormLength);
             GameController.LOGGER.info("Player {} assigned position {}.",
                     player.getName(), i);
             i++;
         }
         // prepare situation
        final CommandDistributor playerControl = new CommandDistributor(
                playground, players);
         final Set<Player> currentPlayers = new HashSet<Player>(players);
         Map<Player, Move> decisions = new HashMap<Player, Move>();
         for (final Player p : currentPlayers) { // initialize players
             decisions.put(p, Move.STAY);
         }
         // start the game
         int turnNumber = 0;
         do {
             GameController.LOGGER.info("--- Starting turn no. {}.", turnNumber);
             final List<Command<DefaultPlayground, DefaultNode, DefaultEdge>> commands = new LinkedList<Command<DefaultPlayground, DefaultNode, DefaultEdge>>();
             // remove inactive worms
             for (final Player player : this.performInactivityDetection(
                     currentPlayers, turnNumber, allowedInactiveTurns)) {
                 currentPlayers.remove(player);
                 commands.add(new DeactivatePlayerCommand(player));
             }
             // move the worms
             for (final Player p : currentPlayers) {
                 final Move m = decisions.get(p);
                 this.addDecision(p, m, turnNumber);
                 final Deque<DefaultNode> newPosition = this.performPlayerMove(
                         p, m);
                 this.setPlayerPosition(p, newPosition);
                 commands.add(new MovePlayerCommand(p, m, newPosition));
             }
             // resolve worms colliding
             for (final Player player : this.performCollisionDetection(
                     playground, currentPlayers)) {
                 currentPlayers.remove(player);
                 commands.add(new CrashPlayerCommand(player));
             }
             if (turnNumber > 0) {
                 // reward surviving worms; but not in the first round
                 for (final Player p : currentPlayers) {
                     this.reward(p, 1);
                     commands.add(new RewardSurvivalCommand(p, wormSurvivalBonus));
                 }
             }
             // expire uncollected collectibles
             final Set<Collectible> removeCollectibles = new HashSet<Collectible>();
            for (final Collectible c : this.collectiblesByNode.values()) {
                if (c.expires() && turnNumber >= c.expiresInTurn()) {
                     removeCollectibles.add(c);
                 }
             }
             for (final Collectible c : removeCollectibles) {
                 this.removeCollectible(c);
                 commands.add(new RemoveCollectibleCommand(c));
             }
             // add points for collected collectibles
             for (final Map.Entry<Collectible, Player> entry : this
                     .performCollectibleCollection(currentPlayers).entrySet()) {
                 final Collectible c = entry.getKey();
                 final Player p = entry.getValue();
                 this.reward(p, c.getPoints());
                 this.removeCollectible(c);
                 this.setPlayerLength(p, this.getPlayerLength(p) + 1);
                 commands.add(new CollectCollectibleCommand(c, p));
             }
             // distribute new collectibles
             for (final Map.Entry<Collectible, DefaultNode> entry : this
                     .performCollectibleDistribution(gameConfig, playground,
                             currentPlayers, turnNumber).entrySet()) {
                 final Collectible c = entry.getKey();
                 final DefaultNode n = entry.getValue();
                 this.addCollectible(c, n);
                 commands.add(new AddCollectibleCommand(c, n));
             }
             // make the move decision
             decisions = playerControl.execute(commands);
             turnNumber++;
         } while (currentPlayers.size() > 1);
         // output player status
         GameController.LOGGER.info("--- Game over.");
         for (final Map.Entry<Player, Integer> entry : this.playerPoints
                 .entrySet()) {
             GameController.LOGGER.info("Player {} earned {} points.", entry
                     .getKey().getName(), entry.getValue());
         }
         return null;
     }
 
     private void removeCollectible(final Collectible c) {
         final DefaultNode n = this.nodesByCollectible.remove(c);
         this.collectiblesByNode.remove(n);
     }
 
     private void reward(final Player p, final int points) {
         if (!this.playerPoints.containsKey(p)) {
             this.playerPoints.put(p, 0);
         }
         this.playerPoints.put(p, this.playerPoints.get(p) + points);
     }
 
     private void setPlayerLength(final Player p, final int length) {
         this.lengths.put(p, length);
     }
 
     private void setPlayerPosition(final Player p,
             final Deque<DefaultNode> position) {
         this.positions.put(p, position);
     }
 
 }
