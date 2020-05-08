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
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import backtype.storm.task.OutputCollector;
 import backtype.storm.task.TopologyContext;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.topology.base.BaseRichBolt;
 import backtype.storm.tuple.Fields;
 import backtype.storm.tuple.Tuple;
 import backtype.storm.tuple.Values;
import redis.clients.jedis.Jedis;
 
 /**
  * This Bolt analyses the given Twitter Status Text.
  * 
  * @author Michael Koppen <koppen@fh-brandenburg.de>
  */
 public class SplitStatusTextBolt extends BaseRichBolt {
 
 	private OutputCollector collector;
 	private List<String> ignoreWords;
 	private String host;
 	private int port;
 
 	public SplitStatusTextBolt(List<String> ignoreWords, String host, int port) {
 		this.ignoreWords = ignoreWords;
 		this.host = host;
 		this.port = port;
 	}
 
 	
 	
 	@Override
 	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
 		this.collector = collector;
 	}
 
 	@Override
 	public void execute(Tuple input) {
 		long id = input.getLong(0);
 		System.out.println("AnalyseStatusTextBolt Status ID: "+id);
 		String text = input.getString(1);
 		System.out.println("AnalyseStatusTextBolt Text: "+text);
 		
 		text = text.toLowerCase();
 		//Clean up text
 		for (String wordToIgnore : ignoreWords) {
 			text = text.replaceAll(wordToIgnore, "");
 		}
 		System.out.println("AnalyseStatusTextBolt filtered Text: "+text);
 		
 		//Split text
 		text = text.trim();
 		List<String> splittedText = Arrays.asList(text.split(" "));
 		
		Jedis jedis = new Jedis(host, port);
		jedis.getClient().setTimeout(9999);
		
 		for (String word : splittedText) {
 			
 			word = word.trim();
 			if (!word.equals("") && word.length()>=3) {
 				
				jedis.incr("#words_full");
 				collector.emit(input, new Values(id, word));
 			}
 		}
 		
 		collector.ack(input);
		jedis.disconnect();
 	}
 
 	@Override
 	public void declareOutputFields(OutputFieldsDeclarer declarer) {
 		declarer.declare(new Fields("id","word"));
 	}	
 }
