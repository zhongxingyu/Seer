 package it.polimi.deib.streams.oracle.repository;
 
 import it.polimi.deib.streams.oracle.Config;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Set;
 
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.NumericLiteralImpl;
 import org.openrdf.query.BindingSet;
 import org.openrdf.query.MalformedQueryException;
 import org.openrdf.query.QueryEvaluationException;
 import org.openrdf.query.QueryLanguage;
 import org.openrdf.query.TupleQuery;
 import org.openrdf.query.TupleQueryResult;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFParseException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class SRBenchImporter extends StreamImporter{
 	private final static Logger logger = LoggerFactory.getLogger(SRBenchImporter.class);
 	private int max=0;
 	private long interval=0;
 	private Set<String> exclude=null;
 	
 	public SRBenchImporter(){
 		max=Config.getInstance().getInputStreamMaxTime();
 		interval=Config.getInstance().getInputStreamInterval();
 		exclude=Config.getInstance().getInputStreamHoles();
 	}
 	
 	public void addTimestampedData(File f, long timestamp) throws RDFParseException, IOException, RepositoryException{
 		URI graph = BenchmarkVocab.getGraphURI(timestamp);
 		repo.getConnection().begin();
 		if(!existsGraph(graph)){
 			repo.getConnection().add(graph, BenchmarkVocab.hasTimestamp, new NumericLiteralImpl(timestamp), BenchmarkVocab.graphList);
 		}
 		repo.getConnection().add(f,"",RDFFormat.TURTLE, graph);
 		repo.getConnection().commit();
 	}
 	
 	public void importData(int time) throws RDFParseException, IOException, RepositoryException{
		addTimestampedData(new File("data/data_"+(time < 10 ? "0" + time : time)+".ttl"), time*interval);
 	}
 	
 	public void importAllData() throws RDFParseException, RepositoryException, IOException{
 		for (int i=0;i<=max;i++){
 			if (!exclude.contains(""+i))
 			  importData(i);
 		}
 			
 	}
 	
 	public void  getGraphs() throws RepositoryException, MalformedQueryException, QueryEvaluationException{
 		RepositoryConnection conn=getRepository().getConnection();
 		String qg="SELECT ?g " +
 				"FROM <"+BenchmarkVocab.graphList+"> " +
 				"WHERE{" +
 				"?g <" + BenchmarkVocab.hasTimestamp + "> ?timestamp. " +
 				//"FILTER(?timestamp >= "+range.getFrom() + " && ?timestamp < "+range.getTo()+") " +
 				"}";
 		TupleQuery q=conn.prepareTupleQuery(QueryLanguage.SPARQL, qg);
 		TupleQueryResult tqr=q.evaluate();		
 		while (tqr.hasNext()){
 			BindingSet bind=tqr.next();
 	    	logger.info(bind.getValue("g").stringValue());
 		}
 	}
 	/*
 	public Graph read(String file){
 		RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
 		Graph myGraph = new TreeModel();
 		StatementCollector collector = new StatementCollector(myGraph);
 		rdfParser.setRDFHandler(collector);
 		
 		try {
 			rdfParser.parse(new FileInputStream(file), "http://example.com/");
 		} catch (RDFParseException | RDFHandlerException
 				 | IOException e) {
 			logger.error("Error while parsing the file "+ file, e);
 		}
 		logger.debug("Finished parse");
 		return myGraph;
 	}*/
 	public static void main(String[] args)   {
 		SRBenchImporter sr=new SRBenchImporter();
 		logger.debug("clean repository");
 		try {
 			sr.clearRepository();
 		logger.debug("importing the data");	
 	        sr.importAllData();
 		logger.debug("finished import");
 	
 			sr.getGraphs();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		
 		}
 	}
 }
