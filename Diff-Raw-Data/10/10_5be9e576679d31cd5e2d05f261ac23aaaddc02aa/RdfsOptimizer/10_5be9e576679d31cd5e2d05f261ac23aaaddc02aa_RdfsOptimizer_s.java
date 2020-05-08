 /**
  * 
  */
 package eu.larkc.iris.rules.stratification;
 
 import java.awt.HeadlessException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.deri.iris.api.basics.ILiteral;
 import org.deri.iris.api.basics.IPredicate;
 import org.deri.iris.api.basics.IRule;
 import org.deri.iris.api.basics.ITuple;
 import org.deri.iris.api.factory.IBasicFactory;
 import org.deri.iris.api.factory.ITermFactory;
 import org.deri.iris.api.terms.ITerm;
 import org.deri.iris.basics.Rule;
 import org.deri.iris.compiler.Parser;
 import org.deri.iris.compiler.ParserException;
 import org.deri.iris.factory.Factory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Florian Fischer, fisf
  */
 public class RdfsOptimizer implements IPostStratificationOptimization,
 		IPreStratificationOptimization {
 
 	private static final Logger logger = LoggerFactory.getLogger(RdfsOptimizer.class);
 	
 	/**
 	 * Factory for rule elements
 	 */
 	private IBasicFactory factory = Factory.BASIC;
 	
 	private ITermFactory termFactory = Factory.TERM;
 	
 	/**
 	 * Rules to be ignored for stratification
 	 */
 	private List<IRule> delayRules = new ArrayList<IRule>();
 	
 	/**
 	 * Constructor
 	 * @throws ParserException 
 	 */
 	public RdfsOptimizer() {		
 		
 		//rdfs2
 		//"http://www.w3.org/1999/02/22-rdf-syntax-ns#type$2(?uuu, ?xxx) :- RIF_HAS_VALUE$3(?aaa, ?uuu, ?vvv), http://www.w3.org/2000/01/rdf-schema#domain$2(?aaa, ?xxx).";
 		
 		ITerm term1 = termFactory.createVariable("uuu");
 		ITerm term2 = termFactory.createVariable("xxx");
 		ITerm term3 = termFactory.createVariable("vvv");
 		ITerm term4 = termFactory.createVariable("aaa");		
 		ITuple tuple1 = factory.createTuple(term1, term2);
 		ITuple tuple2 = factory.createTuple(term4, term1, term3);
 		ITuple tuple3 = factory.createTuple(term4, term2);		
 	
 		
 		IPredicate head2Predicate = factory.createPredicate("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 2);
 		ILiteral head2literal = factory.createLiteral(true, head2Predicate, tuple1);
 		
 		IPredicate body2Predicate1 = factory.createPredicate("RIF_HAS_VALUE", 3);
 		ILiteral body2literal2 = factory.createLiteral(true, body2Predicate1, tuple2);
 		
 		IPredicate body2Predicate2 = factory.createPredicate("http://www.w3.org/2000/01/rdf-schema#domain", 2);
 		ILiteral body2literal1 = factory.createLiteral(true, body2Predicate2, tuple3);
 		
 		List<ILiteral> bodyRDFS2 = new ArrayList<ILiteral>();
 		bodyRDFS2.add(body2literal1);
 		bodyRDFS2.add(body2literal2);
 		
 		List<ILiteral> headRDFS2 = new ArrayList<ILiteral>();
 		headRDFS2.add(head2literal);
 		
 		IRule rdfs2 = factory.createRule(headRDFS2, bodyRDFS2);
 		
 		//rdfs7
 		//"RIF_HAS_VALUE$3(?bbb, ?uuu, ?yyy) :- RIF_HAS_VALUE$3(?aaa, ?uuu, ?yyy), http://www.w3.org/2000/01/rdf-schema#subPropertyOf$2(?aaa, ?bbb),";
 		ITerm term5 = termFactory.createVariable("bbb");
 		ITerm term6 = termFactory.createVariable("yyy");
 		
 		ITuple tuple4 = factory.createTuple(term5, term1, term6);		
 		ITuple tuple5 = factory.createTuple(term4, term1, term6);
 		ITuple tuple6 = factory.createTuple(term4, term5);
 		
 		IPredicate body7predicate2 = factory.createPredicate("http://www.w3.org/2000/01/rdf-schema#subPropertyOf", 2);
 		
 		ILiteral head7literal = factory.createLiteral(true, body2Predicate1, tuple4);
 		ILiteral body7literal2 = factory.createLiteral(true, body2Predicate1, tuple5);
 		ILiteral body7literal1 = factory.createLiteral(true, body7predicate2, tuple6);
 		
 		List<ILiteral> bodyRDFS7 = new ArrayList<ILiteral>();		
 		bodyRDFS7.add(body7literal1);
 		bodyRDFS7.add(body7literal2);
 		
 		List<ILiteral> headRDFS7 = new ArrayList<ILiteral>();
 		headRDFS7.add(head7literal);
 				
 		IRule rdfs7 = factory.createRule(headRDFS7, bodyRDFS7);
 		
 		//rdfs3
 		ITuple tuple7 = factory.createTuple(term4, term1, term3);
 		ITuple tuple8 = factory.createTuple(term1, term2);
 		IPredicate body3Predicate1 = factory.createPredicate("http://www.w3.org/2000/01/rdf-schema#range", 2);
 		ILiteral body3literal1= factory.createLiteral(true, body3Predicate1, tuple3);
 		ILiteral body3literal2= factory.createLiteral(true, body2Predicate1, tuple7);
 		
 		List<ILiteral> bodyRDFS3 = new ArrayList<ILiteral>();
 		bodyRDFS3.add(body3literal1);
 		bodyRDFS3.add(body3literal2);
 		
 		ILiteral head3literal = factory.createLiteral(true, head2Predicate, tuple8);
 		List<ILiteral> headRDFS3 = new ArrayList<ILiteral>();
 		headRDFS3.add(head3literal);
 		
 		IRule rdfs3 = factory.createRule(headRDFS3, bodyRDFS3);
 		
 		//end		
 		
 		delayRules.add(rdfs2);
 		delayRules.add(rdfs7);
 		delayRules.add(rdfs3);
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.larkc.iris.rules.stratification.IPostStratificationOptimization#doPostProcessing(java.util.List)
 	 */
 	@Override
 	public List<List<IRule>> doPostProcessing(List<List<IRule>> rules) {
 		
 		//append at end
 		for (IRule rule : delayRules) {
 			ArrayList<IRule> toAdd = new ArrayList<IRule>();
 			toAdd.add(rule);
 			rules.add(toAdd);
 		}
 		return rules;
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.larkc.iris.rules.stratification.IPreStratificationOptimization#doPreProcessing(java.util.List)
 	 */
 	@Override
 	public List<IRule> doPreProcessing(List<IRule> rules) {
 		
 		for (IRule iRule : rules) {		
			if(delayRules.contains(iRule)) {
				rules.remove(iRule);
 			}
 		}
 
		return rules;
 	}
 
 }
