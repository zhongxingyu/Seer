 package eu.serscis;
 
 import java.util.HashMap;
 import java.util.LinkedList;
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
 	public static void main(String[] args) throws Exception {
 		if (args.length != 1) {
 			throw new Exception("usage: Eval scenario.dl");
 		}
 		String filename = args[0];
 
 		Configuration configuration = KnowledgeBaseFactory.getDefaultConfiguration();
 
 		List<IRule> rules = new LinkedList<IRule>();
 		Map<IPredicate,IRelation> facts = new HashMap<IPredicate,IRelation>();
 
 		Parser parser = new Parser();
 		parse(parser, rules, facts, new File("scenario.dl"));
 		List<IQuery> queries = parser.getQueries();
 		parse(parser, rules, facts, new File("base.dl"));
 
 		IKnowledgeBase initialKnowledgeBase = KnowledgeBaseFactory.createKnowledgeBase( facts, rules, configuration );
 		graph(initialKnowledgeBase, new File("initial.dot"));
 		checkForErrors(initialKnowledgeBase);
 
 		parse(parser, rules, facts, new File("behaviour.dl"));
 		parse(parser, rules, facts, new File("system.dl"));
 
 		IKnowledgeBase finalKnowledgeBase = KnowledgeBaseFactory.createKnowledgeBase( facts, rules, configuration );
 		graph(finalKnowledgeBase, new File("access.dot"));
 		doQueries(finalKnowledgeBase, queries);
 		checkForErrors(finalKnowledgeBase);
 	}
 
 	static private void doQueries(IKnowledgeBase knowledgeBase, List<IQuery> queries) throws Exception {
 		List<IVariable> variableBindings = new ArrayList<IVariable>();
 
 		for (IQuery query : queries) {
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
 	}
 
 	static private void graph(IKnowledgeBase knowledgeBase, File outputDotFile) throws Exception {
 		ITuple xAndY = BASIC.createTuple(TERM.createVariable("X"), TERM.createVariable("Y"));
 
 		IPredicate graphObjectsPredicate = BASIC.createPredicate("graphObjects", 2);
 		ILiteral graphObjectsLiteral = BASIC.createLiteral(true, graphObjectsPredicate, xAndY);
 		IQuery graphObjectsQuery = BASIC.createQuery(graphObjectsLiteral);
 		IRelation graphObjectsResults = knowledgeBase.execute(graphObjectsQuery);
 
 		IPredicate graphInvocablePredicate = BASIC.createPredicate("graphInvocable", 2);
 		ILiteral graphInvocableLiteral = BASIC.createLiteral(true, graphInvocablePredicate, xAndY);
 		IQuery graphInvocableQuery = BASIC.createQuery(graphInvocableLiteral);
 		IRelation graphInvocableResults = knowledgeBase.execute(graphInvocableQuery);
 
 		graph(graphObjectsResults, graphInvocableResults, outputDotFile);
 	}
 
 	static private void checkForErrors(IKnowledgeBase knowledgeBase) throws Exception {
		List<ITerm> terms = new LinkedList<ITerm>();

		for (int i = 0; i < 5; i++) {
			IPredicate errorPredicate = BASIC.createPredicate("error", i);
			ILiteral errorLiteral = BASIC.createLiteral(true, errorPredicate, BASIC.createTuple(terms));
			IQuery errorQuery = BASIC.createQuery(errorLiteral);
			IRelation errorResults = knowledgeBase.execute(errorQuery);
			if (errorResults.size() != 0) {
				System.out.println("\n=== Errors detected ===\n");
				formatResults(errorResults);
				//System.exit(1);
			}
 
			ITerm newTerm = TERM.createVariable("t" + i);
			terms.add(newTerm);
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
 
 	/* (skips self references and uses a single double-headed arrow for two-way relations) */
 	static private void graphRelation(FileWriter writer, IRelation relation) throws Exception {
 		for (int t = 0; t < relation.size(); t++) {
 			ITuple tuple = relation.get(t);
 			ITerm a = tuple.get(0);
 			ITerm b = tuple.get(1);
 			if (relation.contains(BASIC.createTuple(b, a))) {
 				// both ways (or a == b)
 				if (a.toString().compareTo(b.toString()) > 0) {
 					writer.write(format(a) + " -> " + format(b) + " [dir=both];\n");
 				}
 			} else {
 				// one-way
 				writer.write(format(a) + " -> " + format(b) + ";\n");
 			}
 		}
 	}
 
 	static private void graph(IRelation objects, IRelation invocable, File dotFile) throws Exception {
 		FileWriter writer = new FileWriter(dotFile);
 		writer.write("digraph a {\n");
 		//writer.write("  concentrate=true;\n");
 
 		graphRelation(writer, objects);
 
 		writer.write("  edge [color=green];\n");
 		graphRelation(writer, invocable);
 
 		writer.write("}\n");
 		writer.close();
 	}
 
 	/* Extend rules and facts with information from source. */
 	static private void parse(Parser parser, List<IRule> rules, Map<IPredicate,IRelation> facts, File source) throws Exception {
 		FileReader reader = new FileReader(source);
 		try {
 			parser.parse(reader);
 		} finally {
 			reader.close();
 		}
 
 		Map<IPredicate,IRelation> newFacts = parser.getFacts();
 		List<IRule> newRules = parser.getRules();
 
 		rules.addAll(newRules);
 
 		for (Map.Entry<IPredicate,IRelation> entry : newFacts.entrySet()) {
 			IRelation existing = facts.get(entry.getKey());
 			if (existing == null) {
 				facts.put(entry.getKey(), entry.getValue());
 			} else {
 				existing.addAll(entry.getValue());
 			}
 		}
 	}
 }
