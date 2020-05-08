 package com.zuehlke.reuters.storm.bolt;
 
 import java.io.IOException;
 import java.util.Map;
 
 import org.apache.mahout.math.Vector;
 
 import backtype.storm.task.OutputCollector;
 import backtype.storm.task.TopologyContext;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.topology.base.BaseRichBolt;
 import backtype.storm.tuple.Fields;
 import backtype.storm.tuple.Tuple;
 import backtype.storm.tuple.Values;
 
 import com.zuehlke.reuters.mahout.classifier.Classifier;
 import com.zuehlke.reuters.mahout.classifier.LogisticRegression;
 import com.zuehlke.reuters.mahout.features.FeatureCollector;
 
 public class ClassifierBolt extends BaseRichBolt {
 	private static final long serialVersionUID = 5727406562716802109L;
 	private OutputCollector collector;
 	private Classifier classifier;
 	
 	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context, OutputCollector collector) {
 		this.collector = collector;
 		try {
 			classifier = LogisticRegression.loadFromFile("/home/cloudera/workspace/reuters/reutersMahout/models");
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 		
 	}
 
 	public void execute(Tuple input) {
 		String text = input.getString(1);
		FeatureCollector featureCollector = new FeatureCollector();
 		Vector featureVector = featureCollector.extractFeatures(text);
 		String textClass = classifier.classify(featureVector);
 		collector.emit(new Values(input.getString(0), textClass, input.getString(2)));
 		collector.ack(input);
 	}
 
 	public void declareOutputFields(OutputFieldsDeclarer declarer) {
 		declarer.declare(new Fields("name", "class", "realClass"));
 	}
 
 }
