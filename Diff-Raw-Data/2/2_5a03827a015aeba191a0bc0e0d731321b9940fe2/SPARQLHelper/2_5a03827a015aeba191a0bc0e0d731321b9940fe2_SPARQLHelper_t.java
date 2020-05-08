 package de.konrad.commons.sparql;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 
 import com.hp.hpl.jena.query.ARQ;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.query.Syntax;
 import com.hp.hpl.jena.vocabulary.OWL;
 
 //
 //import java.io.File;
 //import java.io.IOException;
 //import java.util.Collection;
 //import java.util.HashMap;
 //import java.util.LinkedList;
 //import java.util.List;
 //import java.util.Map;
 //import java.util.Scanner;
 //import java.util.concurrent.TimeoutException;
 //
 //import net.saim.sparql.Restriction;
 //
 //import org.aksw.commons.collections.random.RandomUtils;
 //import org.aksw.commons.jena.ExtendedQueryEngineHTTP;
 //import org.apache.commons.collections15.MultiMap;
 //import org.apache.commons.collections15.multimap.MultiHashMap;
 //import org.apache.commons.io.FileUtils;
 //
 //import com.hp.hpl.jena.query.ARQ;
 //import com.hp.hpl.jena.query.Query;
 //import com.hp.hpl.jena.query.QueryExecution;
 //import com.hp.hpl.jena.query.QueryExecutionFactory;
 //import com.hp.hpl.jena.query.QueryFactory;
 //import com.hp.hpl.jena.query.QuerySolution;
 //import com.hp.hpl.jena.query.ResultSet;
 //import com.hp.hpl.jena.query.Syntax;
 //import com.hp.hpl.jena.rdf.model.RDFNode;
 //import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
 //
 //import de.uni_leipzig.simba.data.Instance;
 //import de.uni_leipzig.simba.io.KBInfo;
 //import java.util.logging.*;
 //;
 //// TODO: move all sparql stuff into aksw commons
 public class SPARQLHelper
 {
 	//	protected static final Logger log = Logger.getLogger(SPARQLHelper.class.toString());
 	//	public static final String GEONAMES_ENDPOINT_INTERNAL = "http://lgd.aksw.org:8900/sparql";
 	public static final String DBPEDIA_ENDPOINT_OFFICIAL = "http://dbpedia.org/sparql";
 	public static final String DBPEDIA_ENDPOINT_LIVE = "http://live.dbpedia.org/sparql";
 	public static final String DBPEDIA_ENDPOINT = DBPEDIA_ENDPOINT_OFFICIAL;
 	//
 	//	public static final String DBPEDIA_ENDPOINT = DBPEDIA_ENDPOINT_OFFICIAL; 
 	//
 	//	public static final String LGD_ENDPOINT = "http://linkedgeodata.org/sparql/";
 	//	//public static int TIMEOUT = 10000;
 	//
 	//	/**
 	//	 * @param text a string in two-row csv format.
 	//	 * @return a map with an entry for each line where the first row is the key and the second row the value
 	//	 */
 	//	public static Map<String, String> textToMap(String text)
 	//	{
 	//		HashMap<String,String> prefixes = new HashMap<String,String>();
 	//		Scanner in = new Scanner(text);
 	//		while(in.hasNext())
 	//		{
 	//			String[] tokens = in.nextLine().split("\t");
 	//			if(tokens.length==2) prefixes.put(tokens[0],tokens[1]);
 	//		}
 	//		return prefixes;
 	//	}
 	//
 	//	public static Map<String,String> getDefaultPrefixes()
 	//	{
 	//		try
 	//		{
 	//			return textToMap(FileUtils.readFileToString(new File("config/default_prefixes.csv")));
 	//		} catch (IOException e)
 	//		{
 	//			e.printStackTrace();
 	//			return new HashMap<String, String>();	
 	//		}
 	//	}
 	//
 		
 	/** @return the last part of a RDF resource url, e.g. http://dbpedia.org/ontology/City -> City,
 	 * http://example.org/ontology#something -> something*/
 	public static String lastPartOfURL(String url)
 	{
 		return url.substring(Math.max(url.lastIndexOf('#'),url.lastIndexOf('/'))+1);
 	}
 
 	public static List<String> subclassesOf(String clazz,String endpoint, String graph)
 	{
 		final int MAX_CHILDREN = 100;
 		String query = "SELECT distinct(?class) WHERE { ?class rdfs:subClassOf "+wrapIfNecessary(clazz)+". } LIMIT "+MAX_CHILDREN;
 		query = PrefixHelper.addPrefixes(query); // in case rdfs and owl prefixes are not known
 		return resultSetToList(querySelect(query,endpoint,graph));
 	}
 
 	/** returns the root classes of a SPARQL endpoint's ontology ({owl:Thing} normally).  */
 	public static List<String> rootClasses(String endpoint, String graph)
 	{
 		{
 			// if owl:Thing exists and has at least one subclass, so use owl:Thing
 			String queryForOWLThing = "SELECT ?class WHERE {?class rdfs:subClassOf owl:Thing} limit 1";
 			if(!resultSetToList(querySelect(PrefixHelper.addPrefixes(queryForOWLThing),endpoint,graph)).isEmpty())
 			{return Collections.singletonList(OWL.Thing.toString());}
 		}
 //		System.err.println("no owl:Thing found for endpoint "+endpoint+", using fallback.");
 		// bad endpoint, use fallback: classes (instances of owl:Class) which don't have superclasses
 		{
 			String queryForParentlessClasses =
					"SELECT distinct(?class) WHERE {{?class a owl:class} UNION {?class a rdfs:class}. OPTIONAL {?class rdfs:subClassOf ?superClass.} FILTER (!BOUND(?superClass))}";
 
 			List<String> classes = resultSetToList(querySelect(PrefixHelper.addPrefixes(queryForParentlessClasses), endpoint, graph));
 
 			if(!classes.isEmpty()) {return classes;}
 		}
 //		System.err.println("no root owl:Class instance for endpoint "+endpoint+", using fallback fallback.");
 		// very bad endpoint, use fallback fallback: objects of type property which don't have superclasses
 		{
 		String query =
 				"SELECT distinct(?class) WHERE {?x a ?class. OPTIONAL {?class rdfs:subClassOf ?superClass.} FILTER (!BOUND(?superClass))}";
 		List<String> classes = resultSetToList(querySelect(PrefixHelper.addPrefixes(query), endpoint, graph));
 		
 		// we only want classes of instances
 		classes.remove("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property");
 		classes.remove("http://www.w3.org/2000/01/rdf-schema#Class");
 		classes.remove("http://www.w3.org/2002/07/owl#DatatypeProperty");
 		classes.remove("http://www.w3.org/2002/07/owl#DatatypeProperty");
 		
 		return classes;
 		}
 	}
 
 	public static String wrapIfNecessary(String uriString)
 	{
 		if(uriString.startsWith("http://")) return "<"+uriString+">";
 		return uriString;
 	}
 	
 	/**
 	 * Get all Properties of the given knowledge base
 	 * @param endpoint
 	 * @param graph
 	 * @return
 	 */
 	public static List<String> properties(String endpoint, String graph, String className) {
 		Logger logger = Logger.getLogger("SAIM");
 		String query1 = "SELECT DISTINCT ?s \n" +
 				"WHERE { ?s rdf:type "+wrapIfNecessary(className)+".\n"+
 				"?s ?p ?o. }\n"+
 				"LIMIT 5";
 		
 		List<String> subList = resultSetToList(querySelect(PrefixHelper.addPrefixes(query1), endpoint, graph));
 		logger.info("Got "+subList.size()+" subjects of type "+className+" from "+endpoint);
 		String subQuery = "SELECT DISTINCT ?p WHERE {\n";
 		int i = 0;
 		for(String s : subList) {
 			subQuery += wrapIfNecessary(s)+" ?p ?o"+i+".\n";
 			i++;
 		}
 		subQuery+="}";
 		logger.info("May execute: "+subQuery);
 		if(subList.size()>0) {return resultSetToList(querySelect(PrefixHelper.addPrefixes(subQuery), endpoint, graph));}	
 		else {
 			String query = "\nSELECT ?s ?p "+//(COUNT(?s) AS ?count)\n"+
 			//		"FROM "+wrapIfNecessary(graph)+"\n"+
 					"WHERE { ?s rdf:type "+wrapIfNecessary(className)+".\n"+
 				    "	?s ?p ?o\n"+
 				//	"} GROUP BY ?p \n"+
 				//    "ORDER BY DESC(?count)";
 				"} LIMIT 30";
 			Logger.getLogger("SAIM").info("Query "+endpoint+" with query:\n"+query);
 			ResultSet rs = querySelect(PrefixHelper.addPrefixes(query), endpoint, graph);
 			List<String> props = new Vector<String>();
 			while(rs.hasNext()) {
 				QuerySolution qS = rs.next();
 				if(!props.contains(qS.get("?p").toString()))
 					props.add(qS.get("?p").toString());
 			}
 			return props;
 		}
 //		return resultSetToList(querySelect(PrefixHelper.addPrefixes(query), endpoint, graph));		
 	}
 	
 	//
 	//	public static ResultSet query(String endpoint, String graph, String query)
 	//	{
 	//		log.info("Querying \""+query+"\" at endpoint \""+endpoint+"\" and graph "+(graph!=null?'"'+graph+'"':" no graph")+".");	
 	//		try
 	//		{
 	//			QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint, query);
 	//			if(graph!=null)	{queryExecution.addDefaultGraph(graph);}
 	//			ResultSet rs = queryExecution.execSelect(); 
 	//			return rs;
 	//		}
 	//		catch(Throwable e)
 	//		{
 	//			throw new 
 	//			RuntimeException("Error with query \""+query+"\" at endpoint \""+endpoint+"\" and graph "+(graph!=null?'"'+graph+'"':" no graph")+".",e);
 	//		}
 	//	}
 	//
 	//	@SuppressWarnings("deprecation")
 	//	public static ResultSet query(String sparqlEndpoint, String graph, String query, int timeout)
 	//	{
 	//		ExtendedQueryEngineHTTP queryExecution = new ExtendedQueryEngineHTTP(sparqlEndpoint, query);
 	//		queryExecution.setTimeOut(timeout);
 	//		if(graph!=null)	{queryExecution.addDefaultGraph(graph);}
 	//		return queryExecution.execSelect();
 	//	}
 	//
 	//	public static Instance[] getMockSample(KBInfo kb, int n)
 	//	{
 	//		List<Instance> instances = new LinkedList<Instance>();
 	//		for(int i=0;i<n;i++)
 	//		{		
 	//			Instance instance = new Instance("http://someurl"+i);		
 	//			instance.addProperty("rdfs:label", "some label"+i);
 	//			instance.addProperty("blubb:name", "some name"+i);
 	//			instance.addProperty("dc:title", "some title"+i);
 	//			instances.add(instance);
 	//		}
 	//		return instances.toArray(new Instance[0]);
 	//	}
 	//
 	//	public static Instance[] getSample(KBInfo kb, int n, int timeout) throws Exception
 	//	{
 	//		StringBuilder query = new StringBuilder();	
 	//		query.append("SELECT DISTINCT * where {?"+kb.var+" ?p ?o. ");
 	//		query.append('\n');
 	//		// limited restriction subquery 
 	//		query.append("{select ?"+kb.var+" where "+Restriction.restrictionUnion(kb.restrictions, kb.var)+" limit "+n+"}}");
 	//
 	//		List<Instance> instances = new LinkedList<Instance>();
 	//		//try
 	//		{
 	//			ResultSet rs = SPARQLHelper.query(kb.endpoint, null, query.toString(), timeout);
 	//			MultiMap<String,QuerySolution> urlToSolution = new MultiHashMap<String,QuerySolution>();
 	//			while(rs.hasNext())
 	//			{			
 	//				QuerySolution qs = rs.next();			
 	//				urlToSolution.put(qs.getResource(kb.var).toString(), qs);
 	//			}
 	//			for(String url : urlToSolution.keySet())
 	//			{
 	//				Instance instance = new Instance(url);
 	//				instances.add(instance);
 	//				Collection<QuerySolution> querySolutions = urlToSolution.get(url);
 	//				for(QuerySolution solution : querySolutions)
 	//				{
 	//					instance.addProperty(solution.getResource("p").toString(), solution.get("o").toString());
 	//				}				
 	//			}
 	//			return instances.toArray(new Instance[0]);
 	//		}		
 	//		//catch (TimeoutException e) {throw new Exception(e);}
 	//		//{return new Instance[]{new Instance("error: timeout ("+TIMEOUT+") or other sparql error for sparql query\n \""+query+"\",\nmessage: "+e.getMessage())};}
 	//	}
 	//
 	//
 	//	public static String dataType(final String literal)
 	//	{
 	//		int index = literal.indexOf("^^");
 	//		if(index==-1) return "";
 	//		return literal.substring(index+2);
 	//	}
 	//	public static String languageTag(final String literal)
 	//	{
 	//		int index = literal.indexOf("@");
 	//		if(index==-1) return "";
 	//		return literal.substring(index+1);
 	//	}
 	//
 	//	public static String lexicalForm(final String literal)
 	//	{
 	//		String lexicalForm = literal;
 	//		// remove data type		
 	//		int index = lexicalForm.indexOf("^^");
 	//		if(index>-1) {lexicalForm = lexicalForm.substring(0, index);}
 	//		// remove language tag
 	//		index = lexicalForm.indexOf("@"); 				
 	//		if(index>-1) {lexicalForm = lexicalForm.substring(0, index);}
 	//		return lexicalForm;
 	//	}
 	//
 	public static QueryExecution queryExecution(String query,String graph, String endpoint)
 	{
 		ARQ.setNormalMode();
 		Query sparqlQuery = QueryFactory.create(query,Syntax.syntaxARQ);		
 		QueryExecution qexec;
 
 		// take care of graph issues. Only takes one graph. Seems like some sparql endpoint do
 		// not like the FROM option.
 		// it is important to
 		if (graph != null)
 		{
 			qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery, graph);
 		} //
 		else
 		{
 			qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery);
 		}
 		return qexec;
 	}
 	//
 	//	public static QueryExecution queryExecutionDirect(String query,String graph, String endpoint)
 	//	{
 	//		QueryExecution qexec = new QueryEngineHTTP(endpoint, query);
 	//		return qexec;
 	//	}
 	////
 	//	public static boolean hasNext(ResultSet rs)
 	//	{
 	//		try
 	//		{
 	//			return rs.hasNext();
 	//		}
 	//		catch(Exception e)
 	//		{
 	//			return false;
 	//		}
 	//	}
 	//
 
 	public static List<String> resultSetToList(ResultSet rs)
 	{
 		List<String> list = new Vector<String>();
 		while(rs.hasNext())
 		{
 			QuerySolution qs = rs.nextSolution();
 			list.add(qs.get(qs.varNames().next()).toString());
 		}
 		return list;
 	}
 
 	public static ResultSet querySelect(String query, String endpoint, String graph)
 	{		
 		try
 		{
 			//QueryExecution qexec = queryExecutionDirect(query,graph,endpoint);
 			ResultSet results = queryExecution(query,graph,endpoint).execSelect();
 			return results;
 		}
 		catch(RuntimeException e)
 		{
 			throw new RuntimeException("Error with query \""+query+"\" at endpoint \""+endpoint+"\" and graph \""+graph+"\"",e);
 		}
 	}
 
 	//	public static ResultSet querySelect(String query, KBInfo kb)
 	//	{		
 	//		String wholeQuery = formatPrefixes(kb.prefixes)+'\n'+query;
 	//		// end workaround
 	//		//System.out.println(wholeQuery);
 	//		try
 	//		{
 	//			QueryExecution qexec = queryExecutionDirect(wholeQuery,kb.graph,kb.endpoint);
 	//			ResultSet results = qexec.execSelect();
 	//			return results;
 	//		}
 	//		catch(RuntimeException e)
 	//		{
 	//			throw new RuntimeException("Error with query \""+query+"\"",e);
 	//		}
 	//	}
 	//
 	//	//	public static ResultSet querySelect(String query, KBInfo kb,int pageSize)
 	//	//	{		
 	//	//		String wholeQuery = formatPrefixes(kb.prefixes)+'\n'+query;
 	//	//		QueryExecution qexec = queryExecution(wholeQuery,kb.graph,kb.endpoint);
 	//	//		ResultSet results = qexec.execSelect();
 	//	//		return results;
 	//	//	}
 	//
 	//	//	public static ResultSet querySelect
 	//	//	(
 	//	//			String query, String graph, String endpoint, Integer limit,
 	//	//			int offset,Integer pageSize, Map<String,String> prefixes
 	//	//	)
 	//	//	{		
 	//	//		String wholeQuery = formatPrefixes(prefixes)+'\n'+query;
 	//	//		do
 	//	//		{
 	//	//			QueryExecution qexec = queryExecution(wholeQuery,graph,endpoint);
 	//	//			ResultSet results = qexec.execSelect();
 	//	//			results.
 	//	//		} while(true);
 	//	//		return results;
 	//	//	}
 	//
 	//	public static String formatPrefixes(Map<String,String> prefixes)
 	//	{
 	//		if(prefixes.isEmpty()) return "";
 	//		StringBuffer prefixSPARQLString = new StringBuffer();
 	//		for(String key:	prefixes.keySet())
 	//		{
 	//			prefixSPARQLString.append("PREFIX "+key+": <"+prefixes.get(key)+">"+'\n');
 	//		}
 	//		return prefixSPARQLString.substring(0, prefixSPARQLString.length()-1);
 	//	}
 	//
 	//
 	//	/**
 	//	 * @param rs all solutions need to contain bindings for the variables ?s, ?p and ?o 
 	//	 * @return the resulting instances
 	//	 */
 	//	public static Instance[] resultSetToInstances(ResultSet rs)
 	//	{
 	//		MultiMap<String,QuerySolution> urlToSolution = new MultiHashMap<String,QuerySolution>();
 	//		List<Instance> instances = new LinkedList<Instance>();
 	//
 	//		while(rs.hasNext())
 	//		{			
 	//			QuerySolution qs = rs.next();			
 	//			urlToSolution.put(qs.getResource("s").toString(), qs);
 	//		}
 	//		for(String url : urlToSolution.keySet())
 	//		{
 	//			Instance instance = new Instance(url);
 	//			instances.add(instance);
 	//			Collection<QuerySolution> querySolutions = urlToSolution.get(url);
 	//			for(QuerySolution solution : querySolutions)
 	//			{
 	//				//System.out.println(solution);
 	//				instance.addProperty(solution.getResource("p").toString(), solution.get("o").toString());
 	//			}				
 	//		}
 	//		return instances.toArray(new Instance[0]);			
 	//	}
 	//
 	//	/** Generates a random sample from a knowledge base.
 	//	 * @param kb
 	//	 * @param n
 	//	 * @param timeout
 	//	 * @return
 	//	 * @throws TimeoutException 
 	//	 * @throws Exception
 	//	 */
 	//	public static Instance[] getRandomSample(KBInfo kb, int n, int timeout)
 	//	{
 	//		{
 	//			// get size of the knowledge base
 	//			String countQuery = "SELECT DISTINCT count(?s) as ?count where {"+Restriction.restrictionUnion(kb.restrictions, "s")+"}";
 	//			QuerySolution qs;
 	//			System.out.println(countQuery);
 	//			try
 	//			{
 	//				qs = SPARQLHelper.query(kb.endpoint, null, countQuery, timeout).next();
 	//			}
 	//			catch(Exception e)
 	//			{
 	//				System.err.println("Error in SPARQLHelper.getRandomSample() with query "+countQuery+" at endpoint "+kb.endpoint);
 	//				throw new RuntimeException(e);
 	//			}
 	//			RDFNode node = qs.get("count");
 	//			int count = node.asLiteral().getInt();
 	//
 	//			if(n>=count)
 	//			{
 	//				// no random sample needed, just retrieve all
 	//				String query = "select ?s ?p ?o where {?s ?p ?o."+Restriction.restrictionUnion(kb.restrictions, "s")+"}";
 	//				ResultSet rs = SPARQLHelper.query(kb.endpoint, null, query, timeout);				
 	//				return resultSetToInstances(rs);
 	//			}
 	//			double p = (double)n / count;
 	//			// standard deviation of normal distribution
 	//			//			double sigma = Math.sqrt(count*p*(1-p));
 	//			// simplified
 	//			double sigma = Math.sqrt(n*(1-p));
 	//			//level l	   percentage of values within l standard deviations of the mean  
 	//			//			1 	31% 
 	//			//			2 	69% 
 	//			//			3 	93.3% 
 	//			//			4 	99.38% 
 	//			//			5 	99.977% 
 	//			//			6 	99.99966% 
 	//			//			7 	99.9999981% 
 	//			// increasing the sigma level increases the probability that we get at least n elements, however
 	//			// it increases the statistical skew towards the elements at the beginning of the ordering used by 
 	//			// the SPARQL endpoint
 	//			final double SIGMA_LEVEL = 3;
 	//			double safetyIncrement = SIGMA_LEVEL*sigma/count; 
 	//			// this formula is an approximation from below
 	//			// as increasing the probability also increases the variance
 	//			double decimationFactor = 1/(p+safetyIncrement);
 	//
 	//			String query =
 	//					"SELECT * WHERE {?s ?p ?o ."+ 
 	//							"{select ?s where {?s ?p ?o. "+Restriction.restrictionUnion(kb.restrictions, "s")+
 	//							"FILTER ( 1>  <SHORT_OR_LONG::bif:rnd>  ("+decimationFactor+", ?s))} limit "+n+" }}";
 	//			try
 	//			{
 	//				ResultSet rs = SPARQLHelper.querySelect(query, kb);
 	//				Instance[] instances = resultSetToInstances(rs); 
 	//				return RandomUtils.<Instance>randomSample(instances,n);
 	//			}
 	//			catch(Exception e)
 	//			{
 	//				System.err.println("Error in SPARQLHelper.getRandomSample() with query "+query);
 	//				throw new RuntimeException(e);
 	//			}
 	//		}
 	//		//		StringBuilder query = new StringBuilder();	
 	//		//
 	//		//		query.append("SELECT DISTINCT * where {?"+kb.var+" ?p ?o. ");
 	//		//		query.append('\n');
 	//		//		// limited restriction subquery 
 	//		//		query.append("{select ?"+kb.var+" where "+Restriction.restrictionUnion(kb.restrictions, kb.var)+" limit "+n+"}}");
 	//
 	//
 	//
 	//	}		
 	//
 	//	//{return new Instance[]{new Instance("error: timeout ("+TIMEOUT+") or other sparql error for sparql query\n \""+query+"\",\nmessage: "+e.getMessage())};}
 	//
 }
