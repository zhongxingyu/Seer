 package org.drooms.impl.util;
 
 import java.io.File;
 import java.io.FileReader;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.drools.builder.KnowledgeBuilder;
 import org.drooms.api.CustomPathBasedStrategy;
 import org.drooms.api.Edge;
 import org.drooms.api.Node;
 import org.drooms.api.Player;
 import org.drooms.api.Strategy;
 import org.drooms.impl.GameController;
 
 import edu.uci.ics.jung.algorithms.shortestpath.ShortestPath;
 import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
 import edu.uci.ics.jung.graph.Graph;
 
 /**
 * A helper class to load {@link Strategy} implementations for all requested {@link Player}s.
  */
 public class PlayerAssembly {
 
     private static class DefaultPathBasedStrategy implements CustomPathBasedStrategy {
 
         private final Strategy strategy;
 
         public DefaultPathBasedStrategy(final Strategy nonCustomPathBasedStrategy) {
             if (nonCustomPathBasedStrategy == null) {
                 throw new IllegalArgumentException("The strategy may not be null!");
             }
             if (nonCustomPathBasedStrategy instanceof CustomPathBasedStrategy) {
                 throw new IllegalArgumentException("The strategy must not provide its own path-finding algorithm!");
             }
             this.strategy = nonCustomPathBasedStrategy;
         }
 
         @Override
         public boolean enableAudit() {
             return this.strategy.enableAudit();
         }
 
         @Override
         public boolean equals(final Object obj) {
             return this.strategy.equals(obj);
         }
 
         @Override
         public KnowledgeBuilder getKnowledgeBuilder(final ClassLoader cls) {
             return this.strategy.getKnowledgeBuilder(cls);
         }
 
         @Override
         public String getName() {
             return this.strategy.getName();
         }
 
         @Override
         public ShortestPath<Node, Edge> getShortestPathAlgorithm(final Graph<Node, Edge> graph) {
             return new UnweightedShortestPath<>(graph);
         }
 
         @Override
         public int hashCode() {
             return this.strategy.hashCode();
         }
 
         @Override
         public String toString() {
             return this.strategy.toString();
         }
 
     }
 
     private static URL uriToUrl(final URI uri) {
         try {
             return uri.toURL();
         } catch (final MalformedURLException e) {
             throw new IllegalArgumentException("Invalid URL: " + uri, e);
         }
     }
 
     private final Properties config;
 
     private final Map<URI, ClassLoader> strategyClassloaders = new HashMap<>();
 
     private final Map<String, Strategy> strategyInstances = new HashMap<>();
 
     /**
      * Initialize the class.
      * 
      * @param f
     *            Game config as described in{@link GameController#play(org.drooms.api.Playground, Collection, File)}.
      */
     public PlayerAssembly(final File f) {
         try (FileReader fr = new FileReader(f)) {
             this.config = new Properties();
             this.config.load(fr);
         } catch (final Exception e) {
             throw new IllegalArgumentException("Cannot read player config file.", e);
         }
     }
 
     /**
      * Perform all the strategy resolution and return a list of fully
      * initialized players.
      * 
      * @return The unmodifiable collection of players, in the order in which
      *         they come up in the data file.
      */
     public List<Player> assemblePlayers() {
         // parse a list of players
         final Map<String, String> playerStrategies = new HashMap<>();
         final Map<String, URI> strategyJars = new HashMap<>();
         for (final String playerName : this.config.stringPropertyNames()) {
             final String strategyDescr = this.config.getProperty(playerName);
             final String[] parts = strategyDescr.split("\\Q@\\E");
             if (parts.length != 2) {
                 throw new IllegalArgumentException("Invalid strategy descriptor: " + strategyDescr);
             }
             final String strategyClass = parts[0];
             if (strategyClass.startsWith("org.drooms.impl.")) {
                 throw new IllegalStateException("Strategy musn't belong to the game implementation package.");
             }
             try {
                 final URI strategyJar = new URI(parts[1]);
                 playerStrategies.put(playerName, strategyClass);
                 strategyJars.put(strategyClass, strategyJar);
             } catch (final URISyntaxException e) {
                 throw new IllegalArgumentException("Invalid URL in the strategy descriptor: " + strategyDescr, e);
             }
         }
         // load strategies for players
         final List<Player> players = new ArrayList<>();
         for (final Map.Entry<String, String> entry : playerStrategies.entrySet()) {
             final String playerName = entry.getKey();
             final String strategyClass = entry.getValue();
             final URI strategyJar = strategyJars.get(strategyClass);
             CustomPathBasedStrategy strategy;
             try {
                 final Strategy s = this.loadStrategy(strategyClass, strategyJar);
                 if (s instanceof CustomPathBasedStrategy) {
                     strategy = (CustomPathBasedStrategy) s;
                 } else {
                     // if the strategy doesn't care about path, provide the
                     // default one
                     strategy = new DefaultPathBasedStrategy(s);
                 }
             } catch (final Exception e) {
                 throw new IllegalArgumentException("Failed loading: " + strategyClass, e);
             }
             players.add(new Player(playerName, strategy, this.loadJar(strategyJar)));
         }
         return Collections.unmodifiableList(players);
     }
 
     /**
      * Load a strategy JAR file in its own class-loader, effectively isolating
      * the strategies from each other.
      * 
      * @param strategyJar
      *            The JAR coming from the player config.
      * @return The class-loader used to load the strategy jar.
      */
     private ClassLoader loadJar(final URI strategyJar) {
         if (!this.strategyClassloaders.containsKey(strategyJar)) {
            final ClassLoader loader = URLClassLoader.newInstance(new URL[]{PlayerAssembly.uriToUrl(strategyJar)},
                     this.getClass().getClassLoader());
             this.strategyClassloaders.put(strategyJar, loader);
         }
         return this.strategyClassloaders.get(strategyJar);
     }
 
     private Strategy loadStrategy(final String strategyClass, final URI strategyJar) throws Exception {
         if (!this.strategyInstances.containsKey(strategyClass)) {
             final Class<?> clz = Class.forName(strategyClass, true, this.loadJar(strategyJar));
             final Strategy strategy = (Strategy) clz.newInstance();
             this.strategyInstances.put(strategyClass, strategy);
         }
         return this.strategyInstances.get(strategyClass);
     }
 
 }
