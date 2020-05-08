 package rdf;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.Vector;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 
 public class RDFHandler {
 	
 	public static String PLACES_SPARQL = "./input/sparql/places.sparql";
 	
	Vector<QuerySolution> getSparqlOutput(String result, String sparqlQueryFile) throws IOException{
 		//load the rdf
 		Reader reader = new StringReader(result);
 		Model model = ModelFactory.createMemModelMaker().createDefaultModel();
 		model.read(reader, null);		
 		reader.close();
 		
 		//create the query
 		String queryString = this.readTextFile(sparqlQueryFile);
 		Query query = QueryFactory.create(queryString);
 
 		//perform the query
 		QueryExecution qexec = QueryExecutionFactory.create(query, model);
 		try{
 			ResultSet results = qexec.execSelect();
 			Vector<QuerySolution> solutions = new Vector<QuerySolution>();
 			for ( ; results.hasNext() ; ){
 				QuerySolution soln = results.nextSolution() ;
 				solutions.add(soln);
 			}
 			return solutions;
 		} finally { 
 			qexec.close(); 
 		}
 	}
 	
 	String readTextFile(String path) throws IOException{
 		BufferedReader br = new BufferedReader(new FileReader(path));
 		try {
 			StringBuilder sb = new StringBuilder();
 			String line = br.readLine();
 
 			while (line != null) {
 				sb.append(line);
 				sb.append("\n");
 				line = br.readLine();
 			}
 			return sb.toString();
 		} finally {
 			br.close();
 		}		
 	}
 }
