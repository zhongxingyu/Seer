 package org.put.hd.dmarf.algorithms.factories;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.put.hd.dmarf.algorithms.IAlgorithm;
 import org.put.hd.dmarf.algorithms.WekaAlgorithm;
 import org.put.hd.dmarf.algorithms.apriori.binary.BinaryApriori;
 import org.put.hd.dmarf.algorithms.apriori.binary.BinaryRuleEngine;
 import org.put.hd.dmarf.algorithms.apriori.binary.BinarySetsEngine;
 import org.put.hd.dmarf.algorithms.apriori.binary.InjectableSetsEngine;
 import org.put.hd.dmarf.algorithms.apriori.binary.JOCLSetsEngine;
 import org.put.hd.dmarf.algorithms.apriori.nst.AprioriNST;
 
 /**
  * Production factory for code. NOTE: each algorithm in this factory will be
  * tested at integration level by maven.
  * 
  * @author Piotr
  * 
  */
 public class ProductionAlgorithmFactory implements IAlgorithmFactory {
 
 	private List<IAlgorithm> algorithms;
 
 	public ProductionAlgorithmFactory() {
 		algorithms = new LinkedList<IAlgorithm>();
 
 		// Weka algorithm is the first one to be implemented in production
 		// phase.
 		algorithms.add(new WekaAlgorithm());
 
 		// Standard non optimized algorithm for apriori with quite inefficient
 		// implementation
 		algorithms.add(new AprioriNST());
 
 		// Standard CPU implementation of binary apriori
 		algorithms.add(new BinaryApriori(new BinaryRuleEngine(),new BinarySetsEngine(),1));
 
 		// CPU based apriori with support mining with OpenCL (bit align to 64 bits vectors)
 		InjectableSetsEngine setsEngine = new InjectableSetsEngine(
 				new BinarySetsEngine(), new JOCLSetsEngine());
 		
 		algorithms.add(new BinaryApriori(new BinaryRuleEngine(), setsEngine,4));
 
 		// CPU based apriori with support mining with OpenCL (bit align to 64 bits vectors)
		algorithms.add(new BinaryApriori(new BinaryRuleEngine(), setsEngine, 64));
 		
 	}
 
 	public int getNumberOfAlgorithms() {
 		return algorithms.size();
 	}
 
 	public IAlgorithm getAlgorithm(int number) {
 		return algorithms.get(number);
 	}
 
 }
