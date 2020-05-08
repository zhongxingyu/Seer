 /*
  * Copyright (C) 2012 Michael Koppen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.fhb.twitalyse;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.FileHandler;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 import com.google.common.collect.Sets;
 
 import backtype.storm.Config;
 import backtype.storm.LocalCluster;
 import backtype.storm.StormSubmitter;
 import backtype.storm.generated.AlreadyAliveException;
 import backtype.storm.generated.InvalidTopologyException;
 import backtype.storm.topology.TopologyBuilder;
 import de.fhb.twitalyse.bolt.redis.CountLanguageBolt;
 import de.fhb.twitalyse.bolt.redis.CountSourceBolt;
 import de.fhb.twitalyse.bolt.redis.CountWordsBolt;
 import de.fhb.twitalyse.bolt.redis.CountWordsInCircleBolt;
 import de.fhb.twitalyse.bolt.status.coords.FilterCoordsBolt;
 import de.fhb.twitalyse.bolt.status.coords.GetCoordsBolt;
 import de.fhb.twitalyse.bolt.status.source.GetStatusSourceBolt;
 import de.fhb.twitalyse.bolt.status.text.GetStatusTextBolt;
 import de.fhb.twitalyse.bolt.status.text.SplitStatusTextBolt;
 import de.fhb.twitalyse.bolt.status.user.GetLanguageBolt;
 import de.fhb.twitalyse.spout.TwitterStreamSpout;
 import de.fhb.twitalyse.utils.Point;
 
 /**
  * This Topology analyses Twitter Stati posted on the Twitter Public Channel.
  * 
  * @author Christoph Ott <ott@fh-brandenburg.de>
  */
 public class TwitalyseTopology {
 	private final static Logger LOGGER = Logger.getLogger(TwitalyseTopology.class.getName());
 
 	private static final String TWITTERSPOUT = "twitterSpout";
 	private TopologyBuilder builder;
 	private String consumerKey;
 	private String consumerKeySecure;
 	private final int DEFAULT_NUMBEROFWORKERS = 4;
 	private Collection<String> stopWords;
 	private String redisHost;
 	private int redisPort;
 	private String token;
 	private String tokenSecret;
 
 	public TwitalyseTopology() throws IOException {
 		initLogger();
 		initProperties();
 		initBuilder();
 	}
 
 	private void initBuilder() {
 		builder = new TopologyBuilder();
 		initTwitterSpout();
 		initSourceCount();
 		initWordCount();
 		initLanguageCount();
 		initGetCoordsInCircle();
 	}
 	
 	private void initGetCoordsInCircle() {
 		
 		
 		// New York
 		Point centerPoint =  new Point(40.712134, -74.004988);
 		// Mitte EU
 //		Point centerPoint = new Point(49.124219, 5.882080);
 		double radius = 3000;
 		GetCoordsBolt coords = new GetCoordsBolt();
 		FilterCoordsBolt filterCoords = new FilterCoordsBolt(centerPoint, radius, redisHost, redisPort);
 		SplitStatusTextBolt splitText = new SplitStatusTextBolt(stopWords, redisHost, redisPort);
 		CountWordsInCircleBolt count = new CountWordsInCircleBolt(redisHost, redisPort);
 		
 
 		builder.setBolt("1_1 getCoords", coords).allGrouping(TWITTERSPOUT);
 		builder.setBolt("1_2 filterCoords", filterCoords).shuffleGrouping("1_1 getCoords");
 		builder.setBolt("1_3 splitText", splitText).shuffleGrouping("1_2 filterCoords");
 		builder.setBolt("1_4 countWordsInCircle", count).shuffleGrouping("1_3 splitText");
 
 	}
 	
 	private void initLogger() {
 		Level consoleHandlerLevel = Level.SEVERE;
 		Level fileHandlerLevel = Level.INFO;
 		Date today = new Date();
 		SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");
 
 		//setting up ConsoleHandler
 		Logger rootLogger = Logger.getLogger("");
 
 		Handler[] handlers = rootLogger.getHandlers();
 
 		ConsoleHandler chandler = null;
 
 		for (int i = 0; i < handlers.length; i++) {
 			if (handlers[i] instanceof ConsoleHandler) {
 				chandler = (ConsoleHandler) handlers[i];
 			}
 		}
 
 		if (chandler != null) {
 			chandler.setLevel(consoleHandlerLevel);
 		} else {
 			LOGGER.log(Level.SEVERE, "No ConsoleHandler there.");
 		}
 
 		//setting up FileHandler
 		FileHandler fh = null;
 		try {
 			fh = new FileHandler("log/log_" + sdf.format(today) + ".log");
 			fh.setFormatter(new SimpleFormatter());
 			fh.setLevel(fileHandlerLevel);
 		} catch (IOException ex) {
 			new File("log").mkdir();
 			try {
 				fh = new FileHandler("log/log_" + sdf.format(today) + ".log");
 				fh.setFormatter(new SimpleFormatter());
 				fh.setLevel(fileHandlerLevel);
 			} catch (IOException ex1) {
 				System.err.println("Input-output-error while creating the initial log.");
 				LOGGER.log(Level.SEVERE, null, ex1);
 			} catch (SecurityException ex1) {
 				LOGGER.log(Level.SEVERE, null, ex1);
 			}
 			LOGGER.log(Level.SEVERE, null, ex);
 
 		} catch (SecurityException ex) {
 			System.err.println("Cannot open/access Log-Folder so I will not log anything.");
 			LOGGER.log(Level.SEVERE, null, ex);
 		}
 
 		if (fh != null) {
 			rootLogger.addHandler(fh);
 		}
 	}
 
 	private void initLanguageCount() {
 		GetLanguageBolt getLanguageBolt = new GetLanguageBolt();
 		CountLanguageBolt countLanguageBolt = new CountLanguageBolt(redisHost,
 				redisPort);
 
 		builder.setBolt("2_1 getLanguageBolt", getLanguageBolt).allGrouping(
 				TWITTERSPOUT);
 		builder.setBolt("2_2 countLanguageBolt", countLanguageBolt)
 				.shuffleGrouping("2_1 getLanguageBolt");
 	}
 
 	private void initProperties() throws IOException {
 		PropertyLoader propLoader = new PropertyLoader();
 
 		Properties twitterProps = propLoader
 				.loadSystemProperty("twitterProps.properties");
 
 		consumerKey = twitterProps.getProperty("consumerKey");
 		consumerKeySecure = twitterProps.getProperty("consumerKeySecure");
 		token = twitterProps.getProperty("token");
 		tokenSecret = twitterProps.getProperty("tokenSecret");
 		
 		Enumeration<Object> enumOfStopWords = propLoader.loadSystemProperty("ignoreWords.properties").elements();
		stopWords = new HashSet<String>();
 		while (enumOfStopWords.hasMoreElements()) {
 			String stopWordsLang = (String) enumOfStopWords.nextElement();
 			stopWords.addAll(Sets.newHashSet(stopWordsLang.split(";")));
 		}
 				
 		Properties redisProps = propLoader
 				.loadSystemProperty("redisProps.properties");
 		redisHost = redisProps.getProperty("host");
 		redisPort = Integer.valueOf(redisProps.getProperty("port"));
 	}
 
 	private void initSourceCount() {
 		GetStatusSourceBolt getStatusSourceBolt = new GetStatusSourceBolt();
 		CountSourceBolt countSourceBolt = new CountSourceBolt(redisHost,
 				redisPort);
 
 		builder.setBolt("3_1 getStatusSourceBolt", getStatusSourceBolt)
 				.allGrouping(TWITTERSPOUT);
 		builder.setBolt("3_2 countSourceBolt", countSourceBolt).shuffleGrouping(
 				"3_1 getStatusSourceBolt");
 	}
 
 	private void initTwitterSpout() {
 		TwitterStreamSpout twitterStreamSpout = new TwitterStreamSpout(
 				consumerKey, consumerKeySecure, token, tokenSecret, redisHost,
 				redisPort);
 		builder.setSpout(TWITTERSPOUT, twitterStreamSpout, 1);
 	}
 
 	private void initWordCount() {
 		GetStatusTextBolt getTextBolt = new GetStatusTextBolt();
 		SplitStatusTextBolt splitStatusTextBolt = new SplitStatusTextBolt(
 				stopWords, redisHost, redisPort);
 		CountWordsBolt countWordsBolt = new CountWordsBolt(redisHost, redisPort);
 
 		builder.setBolt("4_1 getTextBolt", getTextBolt).allGrouping(
 				TWITTERSPOUT);
 		builder.setBolt("4_2 splitStatusTextBolt", splitStatusTextBolt)
 				.shuffleGrouping("4_1 getTextBolt");
 		builder.setBolt("4_3 countWordsBolt", countWordsBolt).shuffleGrouping(
 				"4_2 splitStatusTextBolt");
 	}
 
 	/**
 	 * (args.length == 0) LocalCluster <br>
 	 * args[0] - Name of Topology for Storm ui (String)<br>
 	 * args[1] - Number of workers (int)
 	 * 
 	 * @param args
 	 * @throws AlreadyAliveException
 	 * @throws InvalidTopologyException
 	 * @throws InterruptedException
 	 */
 	public void startTopology(String[] args) throws AlreadyAliveException,
 			InvalidTopologyException, InterruptedException {
 		Config conf = new Config();
 		conf.setDebug(false);
 
 		if (args != null && args.length > 0) {
 			if (args.length > 1) {
 				conf.setNumWorkers(Integer.parseInt(args[1]));
 			} else {
 				conf.setNumWorkers(DEFAULT_NUMBEROFWORKERS);
 			}
 			LOGGER.log(Level.SEVERE,"Starting Cluster......");
 			StormSubmitter.submitTopology(args[0], conf,
 					builder.createTopology());
 		} else {
 			conf.setMaxTaskParallelism(4);
 			conf.setNumWorkers(DEFAULT_NUMBEROFWORKERS);
 			LocalCluster cluster = new LocalCluster();
 			LOGGER.log(Level.SEVERE,"Starting Cluster......");
 			cluster.submitTopology("twitalyse", conf, builder.createTopology());
 			
 //			Thread.sleep(20000);
 
 //			cluster.shutdown();
 		}
 	}
 
 	public static void main(String[] args) throws IOException {
 		TwitalyseTopology topology = new TwitalyseTopology();
 		try {
 			topology.startTopology(args);
 		} catch (AlreadyAliveException e) {
 			LOGGER.log(Level.SEVERE, "{0}\n{1}", new Object[]{e, e.getMessage()});
 		} catch (InvalidTopologyException e) {
 			LOGGER.log(Level.SEVERE, "{0}\n{1}", new Object[]{e, e.getMessage()});
 		} catch (InterruptedException e) {
 			LOGGER.log(Level.SEVERE, "{0}\n{1}", new Object[]{e, e.getMessage()});
 		}
 	}
 }
