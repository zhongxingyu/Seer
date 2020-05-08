 package eu.serscis;
 
 import java.io.FileWriter;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.io.FileReader;
 import java.io.IOException;
 import org.deri.iris.Configuration;
 import org.deri.iris.KnowledgeBaseFactory;
 import org.deri.iris.api.IKnowledgeBase;
 import org.deri.iris.api.basics.IPredicate;
 import org.deri.iris.api.basics.IQuery;
 import org.deri.iris.api.basics.IRule;
 import org.deri.iris.api.basics.ITuple;
 import org.deri.iris.api.basics.ILiteral;
 import org.deri.iris.api.terms.IVariable;
 import org.deri.iris.api.terms.ITerm;
 import org.deri.iris.compiler.Parser;
 import org.deri.iris.storage.IRelation;
 import static org.deri.iris.factory.Factory.*;
 
 // Based on IRIS Demo code
 
 public class Eval {
 	private static final String loadFile( String filename ) throws IOException {
 		FileReader r = new FileReader( filename );
 
 		StringBuilder builder = new StringBuilder();
 
 		int ch = -1;
 		while( ( ch = r.read() ) >= 0 )
 		{
 			builder.append( (char) ch );
 		}
 		return builder.toString();
 	}
 
 	public static void main(String[] args) throws Exception {
 		if (args.length != 1) {
 			throw new Exception("usage: Eval INPUTFILE.dl");
 		}
 		String filename = args[0];
 
 		Configuration configuration = KnowledgeBaseFactory.getDefaultConfiguration();
 		String program = loadFile( filename );
 
 		Parser parser = new Parser();
 		parser.parse( program );
 
 		Map<IPredicate,IRelation> facts = parser.getFacts();
 		List<IRule> rules = parser.getRules();
 		IKnowledgeBase knowledgeBase = KnowledgeBaseFactory.createKnowledgeBase( facts, rules, configuration );
 
 
 		List<IVariable> variableBindings = new ArrayList<IVariable>();
 
 		for( IQuery query : parser.getQueries() )
 		{
 			// Execute the query
 			IRelation results = knowledgeBase.execute( query, variableBindings );
 
 			System.out.println("\n" +  query );
 
 			if( results.size() == 0 ) {
 				System.out.println( "no results" );
 			} else {
 				boolean first = true;
 				for( IVariable variable : variableBindings )
 				{
 					if( first )
 						first = false;
 					else
 						System.out.print( ", " );
 					System.out.print( variable );
 				}
 				System.out.println( );
 
 				formatResults( results );
 			}
 		}
 
 		IPredicate accessPredicate = BASIC.createPredicate("access", 2);
 		ITuple xAndY = BASIC.createTuple(TERM.createVariable("X"), TERM.createVariable("Y"));
 		ILiteral accessLiteral = BASIC.createLiteral(true, accessPredicate, xAndY);
 		IQuery accessQuery = BASIC.createQuery(accessLiteral);
 		IRelation accessResults = knowledgeBase.execute(accessQuery);
 		graph(accessResults, new File("access.dot"));

		IPredicate errorPredicate = BASIC.createPredicate("error", 2);
		ILiteral errorLiteral = BASIC.createLiteral(true, errorPredicate, xAndY);
		IQuery errorQuery = BASIC.createQuery(errorLiteral);
		IRelation errorResults = knowledgeBase.execute(errorQuery);
		if (errorResults.size() != 0) {
			System.out.println("\n=== Errors detected ===\n");
			formatResults(errorResults);
			System.exit(1);
		}
 	}
 
 	static private void formatResults(IRelation m )
 	{
 		for(int t = 0; t < m.size(); ++t )
 		{
 			ITuple tuple = m.get( t );
 			System.out.println( tuple.toString() );
 		}
 	}
 
 	static private String format(ITerm term) {
 		return "\"" + term.getValue().toString() + "\"";
 	}
 
 	static private void graph(IRelation relation, File dotFile) throws Exception {
 		FileWriter writer = new FileWriter(dotFile);
 		writer.write("digraph a {\n");
 
 		for (int t = 0; t < relation.size(); t++)
 		{
 			ITuple tuple = relation.get(t);
 
 			writer.write(format(tuple.get(0)) + " -> " + format(tuple.get(1)) + ";\n");
 		}
 
 		writer.write("}\n");
 		writer.close();
 	}
 }
