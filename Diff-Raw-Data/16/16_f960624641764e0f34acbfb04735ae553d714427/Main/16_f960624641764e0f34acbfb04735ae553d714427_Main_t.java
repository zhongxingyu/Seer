 package fr.univtours.polytech.indexing_engine_mapreduce.main;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import com.typesafe.config.Config;
 import com.typesafe.config.ConfigFactory;
 
 import fr.univtours.polytech.indexing_engine_mapreduce.filter.Filter;
 import fr.univtours.polytech.indexing_engine_mapreduce.indevertedindex.job.IndexJob;
 import fr.univtours.polytech.indexing_engine_mapreduce.query.job.QueryJob;
 import fr.univtours.polytech.indexing_engine_mapreduce.signextractors.SignExtractor;
 
 /**
  * 
  * @author Jérôme Heissler & Francois Senis
  *
  */
 public class Main {
 
 	public static void main(String[] args) {
		if(args.length < 4)	{
			System.out.println("Missing parameter. Correct use it moteurIndexation MODE CONF INPUT OUTPUT [QUERY]");
			return;
		}
		
 		/* load configuration file */
 		Config conf = ConfigFactory.parseFile(new File(args[1]));
 
 		/* load extractor from className */
 		String extractorClassName = conf.getString("extractor");
 		String[] param = extractorClassName.split("-");
 		SignExtractor extract = null;
 		if (param.length == 2) {
 			extractorClassName = param[0];
 			Class<?> clazz;
 			try {
 				clazz = Class.forName(extractorClassName);
 				extract = (SignExtractor) clazz.getConstructor(int.class).newInstance(param[1]);
 			} catch (Exception e) {
 				System.out.println("Unable to load extractor, please check configuration file");
 			}
 		} else {
 			try {
 				extract = (SignExtractor) Class.forName(extractorClassName).newInstance();
 			} catch (Exception e) {
 				System.out.println("Unable to load extractor \""+extractorClassName+"\", please check configuration file");
 			}
 		}
 
 		/* load filter from classname */
 		List<String> filterList = conf.getStringList("filter");
 
 		ArrayList<Filter> fil = new ArrayList<Filter>();
 		for (String className : filterList) {
 			try {
 				fil.add((Filter) Class.forName(className).newInstance());
 			} catch (Exception e) {
 				System.out.println("Unable to load filter \""+className+"\", please check configuration file");
 			}
 		}
		
 		if(extract == null)	{
 			System.out.println("Unable to load an extractor");
 			return;
 		}else if(filterList.size() != fil.size())	{
 			System.out.println("Unable to load all filter");
 			return;
 		}
 		
 		/**
 		 * The job is indexing all file
 		 */
		if(args[0].compareTo("index") == 0)	{		
 			IndexJob.setSignExtractor(extract);
 			IndexJob.setListFilters(fil);
 	
 			IndexJob job = new IndexJob();
 			job.setInputPaths(args[2]);
 			job.setOutputPath(args[3]);
 			try {
 				job.call();
 				job.getRunningJob().waitForCompletion();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 		/**
 		 * The job is answer to a question
 		 */
		}else if(args[0].compareTo("question") == 0)	{
 
 			/* getting the question tf */
 			String queryQuestion = args[4];
 			HashMap<String, Double> mapQuestion = new HashMap<String, Double>();
 			extract.setContent(queryQuestion);
 			String sign = extract.nextToken();
 			while (sign != null) {
 				String signfiltered = "";
 				if (fil.size() == 0) {
 					signfiltered = sign;
 				} else {
 					String res = sign;
 					for (int i = 0; i < fil.size(); ++i) {
 						res = fil.get(i).filter(res);
 						if (res == null) {
 							break;
 						}
 					}
 					signfiltered = res;
 				}
 				if (signfiltered.compareTo("") != 0) {
 					if (mapQuestion.containsKey(signfiltered))
 						mapQuestion.put(signfiltered, mapQuestion.get(signfiltered) + 1);
 					else
 						mapQuestion.put(signfiltered, (double) 1);	
 				}
 				sign = extract.nextToken();
 			}
 			
 			
 			/* init the job */
 			QueryJob query = new QueryJob(mapQuestion);
 			query.setInputPaths(args[2]);
 			query.setOutputPath(args[3]);
 	
 			try {
 				query.call();
 				query.getRunningJob().waitForCompletion();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 	
 		}else	{
 			
 			System.out.println("args[0] ( = Options ) are : index or question");
 			
 		}
 	}
 }
