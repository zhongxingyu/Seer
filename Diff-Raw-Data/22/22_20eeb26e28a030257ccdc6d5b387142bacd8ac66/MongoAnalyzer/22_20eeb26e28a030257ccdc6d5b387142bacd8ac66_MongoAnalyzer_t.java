 package aic.service.analyzer;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.regex.Pattern;
 
 import tsa.classifier.ClassifierBuilder;
 import tsa.classifier.WekaClassifier;
 import tsa.util.Options;
 import weka.classifiers.bayes.NaiveBayes;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
 import com.mongodb.Mongo;
 
 public class MongoAnalyzer implements IAnalyzer {
 
 	private Mongo mongo;
 
 	private DB db;
 
 	private DBCollection tweetsCollection;
 
 	private WekaClassifier wc;
 
 	public MongoAnalyzer(String dbHost, String db) throws UnknownHostException {
 		this.mongo = new Mongo(dbHost);
 		this.db = mongo.getDB(db);
 		this.tweetsCollection = this.db.getCollection("tweets");
 
 		/*ClassifierBuilder cb = new ClassifierBuilder();
 		Options opts = new Options();
 		cb.setOpt(opts);
 		opts.setSelectedFeaturesByFrequency(true);
 		opts.setNumFeatures(20);
 		opts.setRemoveEmoticons(true);
 
 		try {
 			cb.prepareTrain();
 			cb.prepareTest();
 		} catch (IOException e) {
 			System.err.println("Error initializing analyzer.");
 			throw new RuntimeException(e);
 		}
 
 		NaiveBayes nb = new NaiveBayes();
 		wc = null;
 		try {
 			wc = cb.constructClassifier(nb);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		}*/
 	}
 
 	public double analyze(String company) {
 		return analyze(company, 1,1);
 	}
 	
 	public double analyze(String company, int split,int index){
 		BasicDBObject dbo = new BasicDBObject();
 		dbo.put("keywords", company.toLowerCase());
 		//dbo.put("text", Pattern.compile(".*"+company+".*"));
 		/*DBCursor cursor = tweetsCollection.find(dbo);
 		
 		
 		if(split>1 && index<split && index>=0){
 			//propably very inefficient 
 			//since for this the whole db
 			//must be scanned
 			int count=cursor.count();
 			if(split<count){
 				int limit=count/split;
 				cursor=cursor.skip(index*limit);
 				if(index<split-1){
 					cursor=cursor.limit(limit);
 				}
 			}
 		}
 		
 		double rating = 0;
 		int count = 0;
 		while (cursor.hasNext()) {
 			DBObject tweet = cursor.next();
 			try {
 				String text = (String) tweet.get("text");
 				//System.out.println("----------------");
 				//System.out.println(text); System.out.println(r);
 				double r = (Double)tweet.get("sentiment");
 				rating += r;
 				count++;
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		return rating / count;*/
 		
        //try out map/reduce
		String map = "function(){emit(null,{sentiment: this.sentiment});};";
		String reduce = "function(key,values){" + "var result = 0.0;"
				+ "values.forEach(function(value) {"
				+ "  result += value.sentiment;" + "});"
				+ "return { sentiment: result/values.length };" + "};";
		MapReduceOutput out = tweetsCollection.mapReduce(map, reduce, null,
				MapReduceCommand.OutputType.INLINE, dbo);
		return (Double) ((DBObject) out.results().iterator().next()
				.get("value")).get("sentiment");
 	}
 
 }
