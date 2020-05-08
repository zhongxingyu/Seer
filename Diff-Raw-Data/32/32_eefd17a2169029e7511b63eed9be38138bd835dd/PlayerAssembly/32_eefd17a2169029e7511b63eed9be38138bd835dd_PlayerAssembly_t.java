 package org.drooms.impl.util;
 
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Properties;
 
 import org.drooms.api.Player;
 import org.drooms.impl.GameController;
 import org.kie.api.KieServices;
 import org.kie.api.builder.ReleaseId;
 
 /**
 * A helper class to load Strategy implementations for all requested {@link Player}s.
  */
 public class PlayerAssembly {
 
     private final Properties config;
 
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
         final List<Player> players = new ArrayList<>();
         for (final String playerName : this.config.stringPropertyNames()) {
             final String gav = this.config.getProperty(playerName);
             final String[] gavParts = gav.split("\\Q:\\E");
             if (gavParts.length != 3) {
                 throw new IllegalStateException("Player " + playerName + " has a wrong Maven GAV " + gav + ".");
             }
             final ReleaseId id = KieServices.Factory.get().newReleaseId(gavParts[0], gavParts[1], gavParts[2]);
             players.add(new Player(playerName, id));
         }
         return Collections.unmodifiableList(players);
     }
 
 }
