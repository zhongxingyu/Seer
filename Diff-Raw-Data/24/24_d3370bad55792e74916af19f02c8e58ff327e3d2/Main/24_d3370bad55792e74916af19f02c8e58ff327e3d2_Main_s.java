 package main.nl.marktplaats;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import main.nl.marktplaats.algorithm.Diversification;
 import main.nl.marktplaats.algorithm.ExtendQuery;
 import main.nl.marktplaats.algorithm.LogLikelihoodRatioCalculator;
 import main.nl.marktplaats.objects.Experiment;
 import main.nl.marktplaats.objects.IndexedField;
 import main.nl.marktplaats.objects.Indri;
 import main.nl.marktplaats.objects.LLRDisciminativeTermsL1;
 import main.nl.marktplaats.objects.LLRDisciminativeTermsL2;
 import main.nl.marktplaats.objects.PseudoL1Extention;
 import main.nl.marktplaats.objects.PseudoL2Extention;
 import main.nl.marktplaats.objects.Voyager;
 import main.nl.marktplaats.objects.analyticsL1Extention;
 import main.nl.marktplaats.objects.analyticsL2Extention;
 import main.nl.marktplaats.utils.AobMethod;
 import main.nl.marktplaats.utils.Configuration;
 import main.nl.marktplaats.utils.MMRMethod;
 import main.nl.marktplaats.utils.SearchEngine;
 import main.nl.marktplaats.utils.SqlCommands;
 
 public class Main {
 
 	/**
 	 * @param args
 	 * @throws Exception 
 	 */
 	private static Configuration createConfiguration() throws Exception {
 		Configuration configuration = new Configuration();
 		String pathToDisk = "/media/Data/Coen/";
 		configuration.setLocalPathToExternalDisk(pathToDisk);
 		configuration.setDB("algorithms");
 		configuration.setExperiment(Experiment.VoyagerScores);
 		configuration.setParameterFilesDirectory(pathToDisk+"ParameterFiles/tests");
 		configuration.setReadQueriesFromSGML(true);
 		configuration.setSGMLFolder(pathToDisk+"sgml/DataSet/VIP/Unstemmed/Entire");
 		configuration.setQueryChoice(1);
 		configuration.setIndriPath("/home/varvara/workspace/tools/indri-5.4/");
 		configuration.setIndriResultsFolder(pathToDisk +"Results/Indri");
 		configuration.setIndriScoresInputTable("indriScores");
 		configuration.setReadTable("queries");
 		configuration.setQueryEnvRepository(pathToDisk+"repositories/repositoriesL1/");
 		configuration.setSearchEngine(SearchEngine.Voyager);
 		configuration.setStatisticsTable("statistics");
 		configuration.setVoyagerQueriesTable("voyRequests");
 		configuration.setTrecInputFolder(pathToDisk+"trecResults/tests");
 		configuration.setVoyagerResultsTable("voyScores");
 		configuration.setVoyagerResultsFolder(pathToDisk+"Results/similarItems/");
 		configuration.setIndexField(IndexedField.Title);
/*
 * 		
		configuration.setSearchEngine(SearchEngine.IndriOkapi);
		configuration.setStatisticsTable("voyStatistics");
 		configuration.setVoyagerRequest("http://10.249.123.123:4242/query?Qy=");
 		configuration.setPostFixVoyagerRequest("&Fl=AD_ID&Rk=1&Nr=1000&Sk=0&Hx=no");
 		configuration.setMMRTable("MMR");
 		configuration.setAobMethod(AobMethod.AnalyticsL1);
 		configuration.setMmrMethod(MMRMethod.simpleMMR);
 		
  */
 		
 		return configuration;
 	}	
 	
 	public static void main(String[] args) throws Exception {
 		Configuration configuration = createConfiguration();
 		if(configuration.checkConfiguration())
 			runExperiment(configuration);
 	}
 	private static void runExperiment(Configuration configuration) throws Exception {
 		Experiment experiment = configuration.getExperiment();
 		switch (experiment) {
 		case Aob:
 			boolean checkAob = configuration.checkAobConfiguration();
 			if(checkAob)
 				aob(configuration);
 			break;
 		case VoyagerScores:
 		{
 			boolean checkVoy = configuration.checkVoyagerConfiguration();
 			if(checkVoy)
 				voyagerExperiments(configuration);
 		}
 		break;
 		case Diversification:
 		{
 			boolean checkMMR = configuration.checkMMRConfiguration();
 			if(checkMMR)
 				diversificationExperiment(configuration);
 			break;
 		}
 		case Synonyms:
 			boolean checkSynonyms = configuration.checkSynonymsConfiguration();
 			if(checkSynonyms)
 				synonymsExperiment(configuration);
 			break;
 		case IndriScores:
 			boolean checkIndri = configuration.checkIndriConfiguration();
 			if(checkIndri)
 				indriExperiments(configuration);
 			break;
 		default:
 			System.out.println("Please provide valid experiment");
 			break;
 		}
 		
 	}
 	private static void indriExperiments(Configuration configuration) throws IOException {
 		
 		Indri indri = new Indri(configuration);
 		HashMap<String,String> queries = new HashMap<String, String>();
 		if(configuration.isReadQueriesFromSGML())
 			queries = indri.createQueryFromSGMLIndriQueryLanguage();
 		else 
 			queries = indri.createQueryFromTableIndriQueryLanguage();
 		indri.createParameterFiles(queries);
 		indri.runIndriQueries();
 		indri.saveIndriResultsToTable();
 		indri.gatherStatistics();		
 	}
 
 	private static void synonymsExperiment(Configuration configuration) throws Exception {
 		llr(configuration);		
 	}
 
 	private static void diversificationExperiment(Configuration configuration) throws Exception {
 		Diversification diversification = new Diversification(configuration);
 		MMRMethod mmrMethod = configuration.getMmrMethod();
 		switch (mmrMethod) {
 		case simpleMMR:
 			diversification.diversificationSimple();
 			break;
 		case altAllMMR:
 			diversification.alternativeDiversification();
 			break;
 		case altLastOneMMR:
 			diversification.alternativeDiversification2();
 			break;
 		case altLastFourMMR:
 			diversification.alternativeDiversificationLast4();
 			break;
 		case altLastFourTenNextMMR:
 			diversification.alternativeDiversificationLast4With10Next();
 			break;
 		case altMMRwithFine:
 			diversification.alternativeDiversificationLast4With10NextAndFine();
 			break;
 		default:
 			break;
 		}
 		diversification.alternativeDiversification();
 	}
 
 	private static void voyagerExperiments(Configuration configuration) throws IOException {
 		Voyager voyager = new Voyager(configuration);
 		voyager.createQueryVoyagerQueryLanguage();
 		//voyager.runVoyagerQueries();
 		voyager.saveVoyagerResultsToTable();
		//voyager.createInputFiles();
 		voyager.gatherStatistics();
 	}
 
 	private static void llr(Configuration configuration) throws Exception {
 		SqlCommands sql = new SqlCommands();
 		HashMap<Long, String> docsQueries = sql.selectHashMapLongStringQuery("select doc,query from queries",configuration.getDb());
 		LogLikelihoodRatioCalculator llr = new LogLikelihoodRatioCalculator();
 		for(Entry<Long,String> docQuery : docsQueries.entrySet())
 		{
 			String results = llr.calculateLLRForDoc(docQuery, configuration.getQueryEnvRepository());
 			System.out.println("QUERY:"+docQuery.getValue()+" \n NEW QUERY: "+results);
 		}
 		System.out.println("What Do you want to with the new Queries?");
 	}
 
 	private static void aob(Configuration configuration) throws Exception {
 			SqlCommands sql = new SqlCommands();
 			HashMap<Long,String> queries = sql.selectHashMapQuery("select doc,query from "+configuration.getReadTable()+";",configuration.getDb());
 			for(Entry<Long, String> query: queries.entrySet())
 			{	
 				ExtendQuery newQuery = getExtendQueryType(configuration, query.getKey(), query.getValue());
 				newQuery.saveResults(configuration);
 			}
 	}
 
 	private static ExtendQuery getExtendQueryType(Configuration configuration, Long id, String qString) throws Exception {
 		ExtendQuery newQuery;
 		if(configuration.getAobMethod()==null)
 		{
 			System.out.println("Please, give me Aob method for extention :D !!!!!");
 			return null;
 		}
 		switch (configuration.getAobMethod()) {
 		case AnalyticsL1:
 			newQuery = new analyticsL1Extention(id, qString, configuration);				
 			break;
 		case AnalyticsL2:
 			newQuery = new analyticsL2Extention(id, qString, configuration);
 			break;
 		case PseudoL1:
 			newQuery = new PseudoL1Extention(id, qString, configuration);
 			break;
 		case PseudoL2:
 			newQuery = new PseudoL2Extention(id, qString, configuration);
 			break;
 		case llrL1:
 			newQuery = new LLRDisciminativeTermsL1(id, qString, configuration);
 			break;
 		case llrL2:
 			newQuery = new LLRDisciminativeTermsL2(id, qString, configuration);
 			break;
 		default:
 			newQuery = new ExtendQuery() {};
 			System.out.println("Please, give me a correct Aob method for extention :D !!!!!");
 			break;
 		}
 		
 		return newQuery;
 	}
 	
 
 }
