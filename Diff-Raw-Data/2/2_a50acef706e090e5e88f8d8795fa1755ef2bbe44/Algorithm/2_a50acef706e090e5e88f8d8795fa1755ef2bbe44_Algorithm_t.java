 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 
 /**
  *
  * @author Jervis
  * @author Varun
  *
  * Contains the Algorithm 4.11 to Optimize Cost of Branch
  * Mis-prediction From the Paper 'Selection conditions in main memory'
  * By Kenneth Ross.
  */
 public class Algorithm {
 
 
 	public ArrayList<PlanRecord> plans;
 	public static final boolean DEBUG = false;
 	private LogicalAndTerm terms;
 
 	public Algorithm(LogicalAndTerm terms) {
 		if (terms == null)
 			throw new IllegalArgumentException("terms cannot be null");
 		if (terms.isEmpty())
 			throw new IllegalArgumentException("terms cannot be empty");
 
 		this.terms = terms;
 		this.plans = new ArrayList<PlanRecord>();
 	}
 
 
 	private void d(String msg) {
 		if (DEBUG)
 			System.out.print(msg);
 	}
 
 	private void dln(String msg) {
 		if (DEBUG)
 			System.out.println(msg);
 	}
 
 	public PlanRecord findOptimalPlan(CostModel cm) {
 
 		if (cm == null)
 			throw new IllegalArgumentException("Cost Model cannot be null");
 
 
 		generateAllPlans(terms, cm);
 
 		// p1 is S
 		// p2 is S'
 
 		for(PlanRecord p1 : plans) {
 			LinkedHashSet<BasicTerm> set1 = Util.convertToSet(p1.subset);
 			for(PlanRecord p2 : plans) {
 				LinkedHashSet<BasicTerm> set2 = Util.convertToSet(p2.subset);
 
 				if (!Util.isDisjointSets(set1, set2)) {
 					// dln(". Debug: Skipping Common sets");
 					continue;
 				}
 
 				BranchingAndPlan p = makeBranchingAndPlan(p1, p2);
 
 				LogicalAndTerm lat = p1.getLeftMostLogicalAndTerm(plans);

 				Pair s1CMetric = lat.getCMetric(cm);
 				Pair s2CMetric = p2.subset.getCMetric(cm);
 
 				Pair s1DMetric = p1.subset.getDMetric(cm);
 				Pair s2DMetric = p2.subset.getDMetric(cm);
 
 				boolean isDMetricOfSPrimeDominated = false;
 
 				if(p2.p <= 0.5) {
 					// Check each set in S to see if s' is dominated by it
 					for(PlanRecord p3 : plans) {
 						if(!Util.isLogicalAndTerm(p3))
 							continue;
 
 						Pair s3DMetric = p3.subset.getDMetric(cm);
 
 						if(s2DMetric.y > s3DMetric.y &&
 						   s2DMetric.x > s3DMetric.x){
 							isDMetricOfSPrimeDominated = true;
 							break;
 						}
 					}
 				}
 
 
 				d("s1 = ");
 				d(Util.printSubset(set1, false));
 				d("s2 = ");
 				d(Util.printSubset(set2, false));
 				dln("");
 				d("s1.c = " + s1CMetric);
 				d("s2.c = " + s2CMetric);
 				d("s1.d = " + s1DMetric);
 				d("s2.d = " + s2DMetric);
 				dln("");
 
 				if (s2CMetric.x > s1CMetric.x && s2CMetric.y >= s1CMetric.y) {
 					continue;
 				} else if (p2.p <= 0.5 && isDMetricOfSPrimeDominated) {
 					continue;
 				} else {
 					double combinedCost = Util.getAndPlanCost(p2, p1, plans, cm);
 					LogicalAndTerm union = Util.getUnionTerm(p2.subset,
 															 p1.subset);
 					int idx = Util.getIndexOfSubset(plans, union);
 					double currentCost = plans.get(idx).c;
 
 					// System.out.println("We NOT doing && ");
 					// System.out.print("s1 = ");
 					// System.out.print(Util.printSubset(set1, false));
 					// System.out.print("s2 = ");
 					// System.out.print(Util.printSubset(set2, false));
 					// System.out.println("\n======\n");
 
 					if (combinedCost < currentCost) {
 						// System.out.println("We doing && coz its better");
 						// System.out.print("s1 = ");
 						// System.out.print(Util.printSubset(set1, false));
 						// System.out.print("s2 = ");
 						// System.out.print(Util.printSubset(set2, false));
 						// System.out.println("\n======\n");
 
 						PlanRecord ans = plans.get(idx);
 						ans.c = combinedCost;
 						ans.left = p2.subset.getSubsetNo();
 						ans.right = p1.subset.getSubsetNo();
 					}
 				}
 			}
 		}
 		int lastIdx = plans.size() - 1;
 		return plans.get(lastIdx);
 	}
 
 
 
 	private BranchingAndPlan makeBranchingAndPlan(PlanRecord left,
 												  PlanRecord right) {
 
 		// TODO:(jervis): double-check that expression below is OK.
 		BranchingAndPlan p = new BranchingAndPlan(left.plan, right.plan, null);
 		return p;
 	}
 
 	public ArrayList<PlanRecord> generateAllPlans(LogicalAndTerm terms, CostModel cm) {
 		plans = createPlanRecordsArray(terms, cm);
 		return plans;
 	}
 
 	/**
 	 * Creates an array of 2^k possible plan records from the given terms.
 	 * This corresponds to the 'Array A' in the 'Selection Conditions in Main
 	 * Memory' paper by Ken Ross.
 	 * @param terms
 	 */
 	public static ArrayList<PlanRecord> createPlanRecordsArray(LogicalAndTerm
 																terms, CostModel cm) {
 		ArrayList<LogicalAndTerm> subsets = Util.getAllSubsets(terms);
 		subsets = Util.removeEmptySubset(subsets);
 		Util.numberSubsets(subsets);
 		ArrayList<PlanRecord> plans = new ArrayList<PlanRecord>();
 		for (LogicalAndTerm subset : subsets) {
 			long left, right;
 			left = right = -1;
 			int n = subset.size();
 			double p = subset.getSelectivity();
 			boolean doNoBranch = false;
 			double c = subset.getCost(cm);
 			if (subset.getNoBranchAlgCost(cm) < c) {
 				c = subset.getNoBranchAlgCost(cm);
 				doNoBranch = true;
 			}
 			Plan plan = new LogicalAndPlan(null, null, subset.getTerms());
 			PlanRecord record = new PlanRecord(n,p,doNoBranch,c,plan,left,right, subset);
 			plans.add(record);
 		}
 		return plans;
 	}
 
 }
