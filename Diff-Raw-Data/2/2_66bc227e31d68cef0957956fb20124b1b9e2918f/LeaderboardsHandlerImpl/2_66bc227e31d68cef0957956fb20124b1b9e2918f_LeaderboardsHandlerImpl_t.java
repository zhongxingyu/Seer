 package org.zisist.leaderboard;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.zisist.conf.ConfigurationLoader;
 import org.zisist.leaderboard.inputanalyzer.InputAnalyzer;
 import org.zisist.leaderboard.inputsourcehandler.InputSourceHandler;
 import org.zisist.leaderboard.inputsourcehandler.InputSourceHandlerFactory;
 import org.zisist.leaderboard.merger.Merger;
 import org.zisist.leaderboard.publisher.Publisher;
 import org.zisist.model.Configuration;
 import org.zisist.model.TopPlayer;
 import org.zisist.model.xml.Leaderboard;
 import org.zisist.model.xml.LeaderboardConfiguration;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created by zis.tax@gmail.com on 04/10/2012 at 12:31 AM
  */
 @Service("leaderboardsHandler")
 @SuppressWarnings("unchecked")
 public final class LeaderboardsHandlerImpl implements LeaderboardsHandler {
     private static Logger log = Logger.getLogger(LeaderboardsHandlerImpl.class);
 
     @Autowired
     private ConfigurationLoader configurationLoader;
     @Autowired
     private Map<String, InputAnalyzer> inputAnalyzersMap;
     @Autowired
     private Map<String, Publisher> publishersMap;
     @Autowired
     private Map<String,Merger<TopPlayer>> mergerMap;
     @Autowired
     private InputSourceHandlerFactory inputSourceHandlerFactory;
 
     public void setConfigurationLoader(ConfigurationLoader configurationLoader) {
         this.configurationLoader = configurationLoader;
     }
     public void setInputAnalyzersMap(Map<String, InputAnalyzer> inputAnalyzersMap) {
         this.inputAnalyzersMap = inputAnalyzersMap;
     }
     public void setPublishersMap(Map<String, Publisher> publishersMap) {
         this.publishersMap = publishersMap;
     }
     public void setMergerMap(Map<String, Merger<TopPlayer>> mergerMap) {
         this.mergerMap = mergerMap;
     }
     public void setInputSourceHandlerFactory(InputSourceHandlerFactory inputSourceHandlerFactory) {
         this.inputSourceHandlerFactory = inputSourceHandlerFactory;
     }
 
     @Override
     public final void handleLeaderboards(Configuration configuration) {
         LeaderboardConfiguration leaderboardConfiguration = configurationLoader.getConfiguration(configuration.getConfigurationName());
         log.info(String.format("Loaded configuration %s...", leaderboardConfiguration.getName()));
         Map<String, List<TopPlayer>> topPlayersPerRegion = new HashMap<String, List<TopPlayer>>();
 
         for (Leaderboard board : leaderboardConfiguration.getLeaderboards().getLeaderboard()) {
             // open stream to source
             String inputSourceURI = board.getInputUri();
             InputSourceHandler source = inputSourceHandlerFactory.getInputSourceHandler(inputSourceURI);
             InputStream inputStream = source.openStream(inputSourceURI);
             log.info(String.format("Read input from %s using handler %s", inputSourceURI, source));
             
             // read from source and generate meaningful representation of data
             InputAnalyzer analyzer = inputAnalyzersMap.get(board.getInputAnalyzer());
             List<TopPlayer> topPlayers = analyzer.getTopPlayersFromInputSource(inputStream);
             log.info(String.format("Analyzed input using analyzer %s", board.getInputAnalyzer()));
 
            topPlayersPerRegion.put(board.getInputAnalyzer() + "_" + board.getInputUri().hashCode(), topPlayers);
         }
 
         // handle the data from all sources
         Merger merger = mergerMap.get(leaderboardConfiguration.getMerger());
         List<TopPlayer> finalList = merger.merge(topPlayersPerRegion);
         log.info(String.format("Handled data from all sources using merger %s", leaderboardConfiguration.getMerger()));
 
         // publish the final results
         Publisher publisher = publishersMap.get(leaderboardConfiguration.getPublisher());
         publisher.publish(finalList, configuration);
         log.info(String.format("Published results using publisher %s", leaderboardConfiguration.getPublisher()));
     }
 }
