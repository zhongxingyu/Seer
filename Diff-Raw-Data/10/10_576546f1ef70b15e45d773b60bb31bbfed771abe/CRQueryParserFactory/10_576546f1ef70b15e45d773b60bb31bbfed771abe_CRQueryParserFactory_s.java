 package com.gentics.cr.lucene.search.query;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.util.Version;
 
 import com.gentics.cr.CRConfig;
 import com.gentics.cr.CRRequest;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.lucene.LuceneVersion;
 
 public class CRQueryParserFactory {
 
 	protected static Logger log = Logger.getLogger(CRQueryParserFactory.class);
 	
 	private static String MAX_CLAUSES_KEY="maxqueryclauses";
 	
 	private static String QUERY_PARSER_CLASS="class";
 	
 	private static String QUERY_PARSER_CONFIG="queryparser";
 	
 	/***
 	 * Generates a prepared and configured QueryParser
 	 * @param searchedAttributes
 	 * @param analyzer
 	 * @param request
 	 * @param config
 	 * @return
 	 */
 	public static QueryParser getConfiguredParser(String[] searchedAttributes, Analyzer analyzer, CRRequest request, CRConfig config)
 	{
 		  QueryParser parser = null;
 		  
 		  
 		  Object subconfig = config.get(QUERY_PARSER_CONFIG);
 		  if(subconfig!=null && subconfig instanceof GenericConfiguration)
 		  {
 			  GenericConfiguration pconfig = (GenericConfiguration)subconfig;
 			  
 			  String parserClass = pconfig.getString(QUERY_PARSER_CLASS);
 			  if(parserClass!=null)
 			  {
				try {
					parser = (QueryParser) Class.forName(parserClass).getConstructor(new Class[] {Version.class, String[].class, Analyzer.class, CRRequest.class}).newInstance(LuceneVersion.getVersion(), searchedAttributes, analyzer, request);
				} catch (Exception e) {
					log.error("Could not create QueryParser from class "+parserClass+", will use default QueryParser", e);
				} 
 			  }
 		  }
 		  
 		  if(parser == null)
 		  {
 			  //USE DEFAULT QUERY PARSER
 			  parser = new QueryParser(LuceneVersion.getVersion(),searchedAttributes[0],analyzer);
 		  }
 		  
 		  //CONFIGURE MAX CLAUSES
 	      String maxQueryClausesString = config.getString(QUERY_PARSER_CONFIG+"."+MAX_CLAUSES_KEY);
 	      if(maxQueryClausesString!=null && !"".equals(maxQueryClausesString))
 	      {
 	    	  BooleanQuery.setMaxClauseCount(Integer.parseInt(maxQueryClausesString));
 	      }
 		  
 		  //ADD SUPPORT FOR LEADING WILDCARDS
 		  parser.setAllowLeadingWildcard(true);
 	      parser.setMultiTermRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
 	      
 		  return parser;
 	}
 }
