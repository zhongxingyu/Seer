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
 package de.fhb.twitalyse.bolt.status.text;
 
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import backtype.storm.task.OutputCollector;
 import backtype.storm.task.TopologyContext;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.tuple.Fields;
 import backtype.storm.tuple.Tuple;
 import backtype.storm.tuple.Values;
 import de.fhb.twitalyse.bolt.redis.BaseRedisBolt;
 
 /**
  * This Bolt analyses the given Twitter Status Text.
  *
  * @author Michael Koppen <koppen@fh-brandenburg.de>
  */
 public class SplitStatusTextBolt extends BaseRedisBolt {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7734590864277387631L;
 
 	private final static Logger LOGGER = Logger.getLogger(SplitStatusTextBolt.class.getName());
 
 	private OutputCollector collector;
 	private List<String> ignoreWords;
 
 	public SplitStatusTextBolt(List<String> ignoreWords, String host, int port) {
 		super(host, port);
 		this.ignoreWords = ignoreWords;
 	}
 
 	@Override
 	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
 		super.prepare(stormConf, context, collector);
 		this.collector = collector;
 	}
 
 	@Override
 	public void execute(Tuple input) {
 		long id = input.getLong(0);
 		LOGGER.log(Level.INFO, "AnalyseStatusTextBolt Status ID: {0}", id);
 		String text = input.getString(1);
 		LOGGER.log(Level.INFO, "AnalyseStatusTextBolt Text: {0}", text);
 
 		text = text.toLowerCase();
 		//Clean up text
 		for (String wordToIgnore : ignoreWords) {
			wordToIgnore = " " +wordToIgnore +" ";
 			text = text.replaceAll(wordToIgnore, "");
 		}
 		LOGGER.log(Level.INFO, "AnalyseStatusTextBolt filtered Text: {0}", text);
 
 		//Split text
 		text = text.trim();
 		List<String> splittedText = Arrays.asList(text.split(" "));
 
 
 		Date today = new Date();
 		SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");
 
 		for (String word : splittedText) {
 
 			word = word.trim();
 			if (!word.equals("") && word.length() >= 3) {
 
 				// Saves # of all words
 				this.incr("#words_full");
 				// Saves # of words of today
 				this.incr("#words_full_" + sdf.format(today));
 				
 				collector.emit(input, new Values(id, word));
 			}
 		}
 		collector.ack(input);
 	}
 
 	@Override
 	public void declareOutputFields(OutputFieldsDeclarer declarer) {
 		super.declareOutputFields(declarer);
 		declarer.declare(new Fields("id", "word"));
 	}
 }
