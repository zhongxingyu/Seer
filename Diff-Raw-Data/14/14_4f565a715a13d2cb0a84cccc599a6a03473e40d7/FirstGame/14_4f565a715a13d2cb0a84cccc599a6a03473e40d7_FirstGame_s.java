 package org.drooms.impl;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.math.BigDecimal;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.commons.lang3.tuple.Pair;
 import org.drooms.api.Collectible;
 import org.drooms.api.Game;
 import org.drooms.api.GameReport;
 import org.drooms.api.Player;
 import org.drooms.api.Strategy;
 import org.drooms.impl.collectibles.CheapCollectible;
 import org.drooms.impl.collectibles.ExtremeCollectible;
 import org.drooms.impl.collectibles.GoodCollectible;
 
 public class FirstGame implements
         Game<DefaultSituation, DefaultPlayground, DefaultNode, DefaultEdge> {
 
     private enum CollectibleType {
 
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
 
     private static final SecureRandom RANDOM = new SecureRandom();
 
     private final Set<Collectible> collectibles = new HashSet<Collectible>();
 
     private final Map<Player, Integer> playerPoints = new HashMap<Player, Integer>();
     
     private final Map<URL, ClassLoader> strategyClassloaders = new HashMap<URL, ClassLoader>();
     private final Map<String, Strategy> strategyInstances = new HashMap<String, Strategy>();
     
     private ClassLoader loadJar(URL strategyJar) {
         if (!strategyClassloaders.containsKey(strategyJar)) {
             ClassLoader loader = URLClassLoader.newInstance(new URL[] { strategyJar }, getClass().getClassLoader());
             strategyClassloaders.put(strategyJar, loader);
         }
         return strategyClassloaders.get(strategyJar);
     }
     
     private Strategy loadStrategy(String strategyClass, URL strategyJar) throws Exception {
         if (!strategyInstances.containsKey(strategyClass)) {
             Class<?> clz = Class.forName(strategyClass, true, loadJar(strategyJar));
             Strategy strategy = (Strategy)clz.newInstance();
             strategyInstances.put(strategyClass, strategy);
         }
         return strategyInstances.get(strategyClass);
     }
     
     private List<Player> constructPlayers(final Properties config, final Properties playerConfig) {
         // parse a list of players
         Map<String, String> playerStrategies = new HashMap<String, String>();
         Map<String, URL> strategyJars = new HashMap<String, URL>();
         for (String playerName: playerConfig.stringPropertyNames()) {
             String strategyDescr = playerConfig.getProperty(playerName);
             String[] parts = strategyDescr.split("\\Q@\\E");
             if (parts.length != 2) {
                 throw new IllegalArgumentException("Invalid strategy descriptor: " + strategyDescr);
             }
             String strategyClass = parts[0];
             URL strategyJar;
             try {
                 strategyJar = new URL(parts[1]);
             } catch (MalformedURLException e) {
                 throw new IllegalArgumentException("Invalid URL in the strategy descriptor: " + strategyDescr, e);
             }
             playerStrategies.put(playerName, strategyClass);
             strategyJars.put(strategyClass, strategyJar);
         }
         // load strategies for players
         List<Player> players = new ArrayList<Player>();
         for(Map.Entry<String, String> entry: playerStrategies.entrySet()) {
             String playerName = entry.getKey();
             String strategyClass = entry.getValue();
             URL strategyJar = strategyJars.get(strategyClass);
             Strategy strategy;
             try {
                 strategy = loadStrategy(strategyClass, strategyJar);
             } catch (Exception e) {
                 throw new IllegalArgumentException("Failed loading: " + strategyClass, e);
             }
             players.add(new DefaultPlayer(playerName, strategy.getKnowledgeBase(loadJar(strategyJar))));
         }
         return players;
     }
 
     private DefaultNode pickRandomUnusedNode(final DefaultSituation situation,
             final Collection<Player> players) {
         final DefaultPlayground p = situation.getPlayground();
         final List<DefaultNode> nodes = new LinkedList<DefaultNode>();
         // locate available nodes
         for (int x = 0; x < p.getWidth(); x++) {
             for (int y = 0; y < p.getHeight(); y++) {
                 if (p.isAvailable(x, y)) {
                     nodes.add(new DefaultNode(x, y));
                 }
             }
         }
         // exclude nodes where worms are
         for (final Player player : players) {
             nodes.removeAll(situation.getPositions(player));
         }
         // exclude nodes where collectibles are
         final List<DefaultNode> nodesCopy = new LinkedList<DefaultNode>(nodes);
         for (final DefaultNode n : nodesCopy) {
             if (situation.getCollectible(n) != null) {
                 nodes.remove(n);
             }
         }
         if (nodes.size() == 0) {
             return null;
         } else {
             return nodes.get(FirstGame.RANDOM.nextInt(nodes.size()));
         }
     }
 
     @Override
     public GameReport<DefaultSituation, DefaultPlayground, DefaultNode, DefaultEdge> play(
             final Properties gameConfig, final Properties playerConfig) {
         final List<Player> players = this.constructPlayers(gameConfig, playerConfig);
         DefaultPlayground playground;
         try {
             playground = DefaultPlayground.read(new File(gameConfig
                     .getProperty("playground.file")));
         } catch (final IOException e) {
             throw new IllegalArgumentException(
                     "Playground file cannot be read!", e);
         }
         final int wormLength = Integer.valueOf(gameConfig.getProperty(
                 "worm.length", "1"));
         DefaultSituation currentSituation = new DefaultSituation(playground,
                 players, wormLength);
         final Set<Player> currentPlayers = new HashSet<Player>(players);
         do {
             // expire uncollected collectibles
             final Set<Collectible> removeCollectibles = new HashSet<Collectible>();
             for (final Collectible c : this.collectibles) {
                 if (currentSituation.getTurnNumber() >= c.expiresInTurn()) {
                     currentSituation.removeCollectible(c);
                     removeCollectibles.add(c);
                 }
             }
             this.collectibles.removeAll(removeCollectibles);
             // add points for collected collectibles
             for (final Player p : currentPlayers) {
                 final DefaultNode headPosition = currentSituation
                         .getHeadPosition(p);
                 final Collectible c = currentSituation
                         .getCollectible(headPosition);
                 if (c != null) { // successfully collected
                     currentSituation.collectCollectible(c, p);
                     currentSituation.setPlayerLength(p,
                             currentSituation.getPlayerLength(p) + 1);
                     this.reward(p, c.getPoints());
                     this.collectibles.remove(c);
                 }
             }
             // distribute new collectibles
             for (final CollectibleType ct : CollectibleType.values()) {
                 final BigDecimal probability = ct
                         .getProbabilityOfAppearance(gameConfig);
                 final BigDecimal chosen = BigDecimal.valueOf(FirstGame.RANDOM
                         .nextDouble());
                 if (probability.compareTo(chosen) > 0) {
                     final int expiration = currentSituation.getTurnNumber()
                             + ct.getExpiration(gameConfig);
                     final int points = ct.getPoints(gameConfig);
                     Collectible c = null;
                     switch (ct) {
                     case CHEAP:
                         c = new CheapCollectible(points, expiration);
                         break;
                     case GOOD:
                         c = new GoodCollectible(points, expiration);
                         break;
                     case EXTREME:
                         c = new ExtremeCollectible(points, expiration);
                         break;
                     default:
                         throw new IllegalStateException(
                                 "Unknown collectible type!");
                     }
                     this.collectibles.add(c);
                     currentSituation.addCollectible(c, this
                             .pickRandomUnusedNode(currentSituation,
                                     currentPlayers));
                 }
             }
             // make the move
             // FIXME kill the worm when it stays at the same place for a long time
             currentSituation = currentSituation.move();
             // resolve worms colliding
            // FIXME doesn't handle collisions with itself (cycle or return in the same direction)
             final Set<Player> oneCollidedWorms = new HashSet<Player>();
             final Set<Pair<Player, Player>> bothCollidedWorms = new HashSet<Pair<Player, Player>>();
             for (final Player p1 : currentPlayers) {
                 final DefaultNode firstPosition = currentSituation
                         .getHeadPosition(p1);
                 if (!playground.isAvailable(firstPosition)) {
                     oneCollidedWorms.add(p1);
                     continue;
                 }
                 for (final Player p2 : currentPlayers) {
                    if (p1 == p2) {
                        // the same worm
                        continue;
                    }
                     final DefaultNode secondPosition = currentSituation
                             .getHeadPosition(p2);
                     if (firstPosition.equals(secondPosition)) {
                         // head-on-head collision
                         bothCollidedWorms.add(Pair.of(p1, p2));
                     } else if (currentSituation.getPositions(p1).contains(
                             secondPosition)) {
                        // head-on-body collision
                         oneCollidedWorms.add(p2);
                     }
                 }
             }
             for (final Pair<Player, Player> pair : bothCollidedWorms) {
                 if (!currentPlayers.contains(pair.getLeft())
                         && !currentPlayers.contains(pair.getRight())) {
                     // make sure we don't count the same pair twice
                     continue;
                 }
                 currentSituation.collide(pair.getLeft(), pair.getRight());
                 currentPlayers.remove(pair.getLeft());
                 currentPlayers.remove(pair.getRight());
             }
             for (final Player player : oneCollidedWorms) {
                 currentSituation.crash(player);
                 currentPlayers.remove(player);
             }
             // reward surviving worms
             for (final Player p : currentPlayers) {
                 this.reward(p, 1);
                 currentSituation.rewardSurvival(p, Integer.valueOf(gameConfig
                         .getProperty("worm.survival.bonus", "1")));
             }
         } while (currentPlayers.size() > 1);
         return null;
     }
 
     private void reward(final Player p, final int points) {
         if (!this.playerPoints.containsKey(p)) {
             this.playerPoints.put(p, 0);
         }
         this.playerPoints.put(p, this.playerPoints.get(p) + points);
     }
 
     public static void main(final String[] args) {
         try (Reader gameConfigFile = new FileReader(args[0]);
                 Reader playerConfigFile = new FileReader(args[1])) {
             final Properties gameConfig = new Properties();
             gameConfig.load(gameConfigFile);
             final Properties playerConfig = new Properties();
             playerConfig.load(playerConfigFile);
             new FirstGame().play(gameConfig, playerConfig);
         } catch (final IOException e) {
             System.out.println(e);
             System.exit(1);
         }
 
     }
 
 }
