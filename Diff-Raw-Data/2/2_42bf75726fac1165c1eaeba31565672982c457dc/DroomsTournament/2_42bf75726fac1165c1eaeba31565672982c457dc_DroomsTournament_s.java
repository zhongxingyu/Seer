 package org.drooms.impl;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Timestamp;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Properties;
 import java.util.SortedMap;
 
 import org.drooms.api.Game;
 import org.drooms.api.Player;
 import org.drooms.api.Playground;
 import org.drooms.impl.util.DroomsTournamentResults;
 import org.drooms.impl.util.PlayerAssembly;
 import org.drooms.impl.util.TournamentCLI;
 import org.drooms.impl.util.TournamentResults;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class DroomsTournament {
 
     private static final Logger LOGGER = LoggerFactory
             .getLogger(DroomsTournament.class);
 
     @SuppressWarnings("unchecked")
     private static Class<? extends Game> getGameImpl(final String id) {
         try {
             return (Class<? extends Game>) Class.forName(id);
         } catch (final ClassNotFoundException e) {
             throw new IllegalArgumentException(
                     "Cannot instantiate game class.", e);
         }
     }
 
     private static String getTimestamp() {
         final Date date = new java.util.Date();
         return new Timestamp(date.getTime()).toString();
     }
 
     private static Properties loadFromFile(final File f) {
         try (InputStream is = new FileInputStream(f)) {
             final Properties props = new Properties();
             props.load(is);
             return props;
         } catch (final IOException e) {
             return null;
         }
     }
 
     public static void main(final String[] args) {
         // load the CLI
         final TournamentCLI cli = TournamentCLI.getInstance();
         final File config = cli.process(args);
         if (config == null) {
             cli.printHelp();
             System.exit(-1);
         }
         // load players
         final Properties props = DroomsTournament.loadFromFile(config);
         if (props == null) {
             throw new IllegalStateException(
                     "Failed reading tournament config file.");
         }
         final File playerConfigFile = new File(props.getProperty("players"));
         final Collection<Player> players = new PlayerAssembly(playerConfigFile)
                 .assemblePlayers();
         // load report folder
         final String id = DroomsTournament.getTimestamp();
         final File reports = new File("target/reports/tournaments/" + id);
         if (!reports.exists()) {
             reports.mkdirs();
         }
         // load game class
         final Class<? extends Game> game = DroomsTournament.getGameImpl(props
                 .getProperty("game.class"));
         // prepare a result tracker
         final TournamentResults result = new DroomsTournamentResults(id,
                 players);
         // for each playground...
         final String[] playgroundNames = props.getProperty("playgrounds")
                 .split("\\Q,\\E");
         for (final String playgroundName : playgroundNames) {
             // load playground
             final File playgroundFile = new File("src/main/resources",
                     playgroundName + ".playground");
             Playground p = null;
             try (InputStream is = new FileInputStream(playgroundFile)) {
                 p = DefaultPlayground.read(is);
             } catch (final IOException e) {
                 throw new IllegalStateException("Cannot read playground file "
                         + playgroundFile, e);
             }
             // load game properties
             final File propsFile = new File("src/main/resources",
                     playgroundName + ".cfg");
             final Properties gameProps = DroomsTournament
                     .loadFromFile(propsFile);
             if (gameProps == null) {
                 throw new IllegalStateException(
                         "Failed reading game config file for playgrond: "
                                 + playgroundName);
             }
             // run N games on the playground
             DroomsTournament.LOGGER.info("Starting games on playground {}.",
                     playgroundName);
             final DroomsGame dg = new DroomsGame(game, p, players, gameProps,
                     reports);
            for (int i = 1; i < Integer.valueOf(props.getProperty("runs")); i++) {
                 DroomsTournament.LOGGER.info(
                         "Starting game #{} on playground {}.", i,
                         playgroundName);
                 result.addResults(playgroundName,
                         dg.play(playgroundName + "_" + i));
             }
         }
         System.out.println("Tournament results:");
         int i = 1;
         for (final SortedMap.Entry<Long, Collection<Player>> entry : result
                 .evaluate().entrySet()) {
             System.out.println("#" + i + " with " + entry.getKey()
                     + " points: " + entry.getValue());
             i++;
         }
         try (BufferedWriter w = new BufferedWriter(new FileWriter(new File(
                 reports, "report.html")))) {
             result.write(w);
         } catch (final IOException e) {
             // FIXME do something here
         }
     }
 
 }
