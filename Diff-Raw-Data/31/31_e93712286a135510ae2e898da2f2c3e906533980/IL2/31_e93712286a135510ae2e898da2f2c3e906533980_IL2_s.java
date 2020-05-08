 package inflib;
 
 /** Import il1 classes. */
 import edu.ucla.belief.*;
 import edu.ucla.belief.inference.*;
 import edu.ucla.belief.io.PropertySuperintendent;
 import edu.ucla.belief.io.NetworkIO;
 
 /** Import classes for loopy belief propagation. */
 import edu.ucla.belief.approx.PropagationEngineGenerator;
 import edu.ucla.belief.approx.BeliefPropagationSettings;
 import edu.ucla.belief.approx.PropagationInferenceEngineImpl;
 import java.math.BigInteger;
 
 
 import edu.ucla.belief.BeliefNetwork;
 import edu.ucla.belief.FiniteVariable;
 import edu.ucla.belief.InferenceEngine;
 import edu.ucla.belief.inference.*;
 
 import il2.inf.JointEngine;
 import il2.inf.PartialDerivativeEngine;
 import il2.inf.structure.EliminationOrders;
 import il2.model.BayesianNetwork;
 import il2.util.IntList;
 import il2.util.IntMap;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.*;
 
 public class IL2{
 
     	BayesianNetwork bn;  // il2 library
 	JointEngine ie2;
 	double[][] tc;
 
 	// CONSTRUCTOR
 
 	public IL2() 
 	{
 	}
 
 	public void loadBayesianNetwork(String filename)
 	{
          	bn = UaiConverter.uaiToBayesianNetwork(filename);
 	}
 
 	public void updateCPTTable(int node, double[] cpt)
 	{
 		// Retrieve CPT from BN
 		il2.model.Table t = bn.cpts()[node];
 		double[] c = t.values();
 
 		// Update values in CPT
 		for (int i=0; i < cpt.length; i++)
 			c[i] = cpt[i];
 
 		// Inform IE that CPT has been modified
 		ie2.setTable(node,t);
 	}
 
 	public void startEngine()
 	{
 		// Load Engine
         	ie2 = startInferenceEngine2(bn);
 	}
 
 	public void updateEvidence(int[] dataset)
 	{
 		// Load evidenee 
 
		IntMap ie2Evidence = new IntMap();
		for (int i=0; i < dataset.length; i++)
		{
			if (dataset[i]!=-1)
 			{
 				ie2Evidence.putAtEnd(i,dataset[i]);
 			}
		}
 
 		// Set evidence
 		
 		ie2.setEvidence(ie2Evidence);
 		
 		double pr = ie2.prEvidence();
 
 
 		// Update table conditional
 		int size = bn.domain().size();
 		tc = new double[size][];
 		for (int i=0; i < size; i++)
 			tc[i] = ie2.tableConditional(i).values();
 	}
 
 	public double[] tableConditional(int node)
 	{
 		return ie2.tableConditional(node).values();
 	}
 
 	public double probability(int node, int cptPos)    //cptPos is in IL2 format
 	{
		return ie2.tableConditional(node).values()[cptPos];
		//return tc[node][cptPos];
 	}
 
 	public void print()
 	{
 		for (int i=0;i<bn.domain().size();i++)
 		{
 			il2.model.Table t = ie2.tableConditional(i);
 			System.out.println(t);
 		}
 	}
 
     /************************************************************
      * INFERENCE ENGINE STUFF
      ************************************************************/
 
     /**
      * get elimination order
      */
     static EliminationOrders.Record 
         getEliminationOrder(Collection<il2.model.Table> domains) {
 
         java.util.Random r = new java.util.Random(0);
         EliminationOrders.Record r_mf, r_ms, record;
 
         r_mf = EliminationOrders.minFill(domains,6,r);
         if (true) return r_mf; // AC: need to re-evalute the following
         r_ms = EliminationOrders.minSize(domains);
 
         int size = r_mf.order.size();
         IntList order = new IntList(size);
         for (int i = 0; i < size; i++) order.add(i);
         record = EliminationOrders.createRecord(domains,order);
 
         // AC: really want to test total size, not max size
         if ( record.size > r_mf.size ) record = r_mf;
         if ( record.size > r_ms.size ) record = r_ms;
         return record;
     }
 
     /**
      * get a jointree
      */
     static EliminationOrders.JT getJoinTree(BayesianNetwork bn) {
         il2.model.Table[] cpts = bn.cpts();
         Collection<il2.model.Table> sd = Arrays.asList(cpts);
         EliminationOrders.Record record = getEliminationOrder(sd);
         
         EliminationOrders.JT jt;
         if (true) // this JT construction has quadratic space complexity
             jt = EliminationOrders.traditionalJoinTree(sd,record.order);
         else      // this JT construction has linear space complexity
             jt = EliminationOrders.bucketerJoinTree(sd,record.order);
         return jt;
     }
 
     /**
      * starts il2 inference engine (Hugin)
      */
     public static JointEngine startInferenceEngine2(BayesianNetwork bn) {
         il2.model.Table[] cpts = bn.cpts();
         EliminationOrders.JT jt = getJoinTree(bn);
         JointEngine ie;
         //ie = il2.inf.jointree.NormalizedZCAlgorithm.create(cpts,jt);
        //ie = il2.inf.jointree.UnindexedZCAlgorithm.create(cpts,jt);
         //ie = il2.inf.jointree.UnindexedSSAlgorithm.create(cpts,jt);
        ie = il2.inf.jointree.UnindexedHuginAlgorithm.create(cpts,jt);
 
         double max = ie.getClusterStats().getNormalizedMax();
 		double total = ie.getClusterStats().getTotal().doubleValue();
 		total = Math.log(total)/Math.log(2);
 
         //System.out.printf("MAX: %.2f\n",max);
         //System.out.printf("TOT: %.2f\n",total);
         return ie;
     }
 }
