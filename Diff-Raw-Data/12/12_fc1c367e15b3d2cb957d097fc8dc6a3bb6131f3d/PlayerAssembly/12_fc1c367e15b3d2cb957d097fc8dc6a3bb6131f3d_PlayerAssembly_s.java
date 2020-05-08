 package org.drooms.impl.util;
 
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.drools.KnowledgeBase;
 import org.drools.builder.KnowledgeBuilder;
 import org.drools.builder.KnowledgeBuilderError;
 import org.drooms.api.Player;
 import org.drooms.api.Strategy;
 import org.drooms.impl.GameController;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A helper class to load {@link Strategy} implementations. for all requested
  * {@link Player}s.
  */
 public class PlayerAssembly {
 
     private static final Logger LOGGER = LoggerFactory
             .getLogger(PlayerAssembly.class);
 
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
      * @param config
      *            Game config as described in
      *            {@link GameController#play(String, Properties, Properties)}.
      */
     public PlayerAssembly(final Properties config) {
         this.config = config;
     }
 
     /**
      * Perform all the strategy resolution and return a list of fully
      * initialized players.
      * 
      * @return The unmodifiable collection of players, in a totally randomized
      *         order.
      */
     public Collection<Player> assemblePlayers() {
         // parse a list of players
         final Map<String, String> playerStrategies = new HashMap<>();
         final Map<String, URI> strategyJars = new HashMap<>();
         for (final String playerName : this.config.stringPropertyNames()) {
             final String strategyDescr = this.config.getProperty(playerName);
             final String[] parts = strategyDescr.split("\\Q@\\E");
             if (parts.length != 2) {
                 throw new IllegalArgumentException(
                         "Invalid strategy descriptor: " + strategyDescr);
             }
             final String strategyClass = parts[0];
             if (strategyClass.startsWith("org.drooms.impl.")) {
                 throw new IllegalStateException(
                         "Strategy musn't belong to the game implementation package.");
             }
             try {
                 final URI strategyJar = new URI(parts[1]);
                 playerStrategies.put(playerName, strategyClass);
                 strategyJars.put(strategyClass, strategyJar);
             } catch (final URISyntaxException e) {
                 throw new IllegalArgumentException(
                         "Invalid URL in the strategy descriptor: "
                                 + strategyDescr, e);
             }
         }
         // load strategies for players
         final List<Player> players = new ArrayList<>();
         for (final Map.Entry<String, String> entry : playerStrategies
                 .entrySet()) {
             final String playerName = entry.getKey();
             final String strategyClass = entry.getValue();
             final URI strategyJar = strategyJars.get(strategyClass);
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
                 players.add(new Player(playerName, kbase));
             } catch (final Exception ex) {
                 for (final KnowledgeBuilderError error : kb.getErrors()) {
                     PlayerAssembly.LOGGER.error(error.toString());
                 }
                 throw new IllegalStateException(
                         "Cannot create knowledge base for strategy: "
                                 + strategy.getName(), ex);
             }
         }
         Collections.shuffle(players, new SecureRandom());
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
             @SuppressWarnings("resource")
             final ClassLoader loader = URLClassLoader.newInstance(
                     new URL[] { PlayerAssembly.uriToUrl(strategyJar) }, this
                             .getClass().getClassLoader());
             this.strategyClassloaders.put(strategyJar, loader);
         }
         return this.strategyClassloaders.get(strategyJar);
     }
 
     private Strategy loadStrategy(final String strategyClass,
             final URI strategyJar) throws Exception {
         if (!this.strategyInstances.containsKey(strategyClass)) {
             final Class<?> clz = Class.forName(strategyClass, true,
                     this.loadJar(strategyJar));
             final Strategy strategy = (Strategy) clz.newInstance();
             this.strategyInstances.put(strategyClass, strategy);
         }
         return this.strategyInstances.get(strategyClass);
     }
 
 }
