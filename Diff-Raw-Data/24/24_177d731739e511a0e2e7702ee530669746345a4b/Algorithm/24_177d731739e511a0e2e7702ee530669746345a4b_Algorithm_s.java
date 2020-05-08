 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 
 /**
  *
  * @author Jervis
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
 
	public PlanRecord findOptimialPlan(CostModel cm) {
 
 		if (cm == null)
 			throw new IllegalArgumentException("Cost Model cannot be null");
 
 
 		generateAllPlans(terms);
 
 		for(PlanRecord p1 : plans) {
 			LinkedHashSet<BasicTerm> set1 = Util.convertToSet(p1.subset);
 			for(PlanRecord p2 : plans) {
 				LinkedHashSet<BasicTerm> set2 = Util.convertToSet(p2.subset);
 
 				// debug, delete me later
 				d("s1 = ");
 				d(Util.printSubset(set1, false));
 				d("s2 = ");
 				d(Util.printSubset(set2, false));
 				d(" my common size  = " + Util.getCommonElementsSize(set1, set2));
 				d(" ## ");
 
 				d(String.valueOf(p1.subset.getSubsetNo()));
 				d(" | ");
 				d(String.valueOf(p2.subset.getSubsetNo()));
 
 				if (!Util.isDisjointSets(set1, set2)) {
 					dln(". Debug: Skipping Common sets");
 					continue;
 				}
 				dln("");
 
 				BranchingAndPlan p = makeBranchingAndPlan(p1, p2);
 
 				/**
 				 * TODO(jervis): double check again to make sure I do the
 				 * checks correctly for both metrics. (esp. wrt to condition
 				 * "leftmost &-term in set2 (aka 's' in paper)")
 				 */
 				LogicalAndTerm lat = p1.getLeftMostLogicalAndTerm(plans);
 				Pair s1CMetric = lat.getCMetric(cm);
 				Pair s2CMetric = p2.subset.getCMetric(cm);
 
 
 				Pair s1DMetric = p1.subset.getDMetric(cm);
 				Pair s2DMetric = p2.subset.getDMetric(cm);
 				if (s2CMetric.x < s1CMetric.x && s2CMetric.y <= s1CMetric.y) {
 					continue;
 				} else if (p2.p <= 0.5 &&
 						   s2DMetric.y < s1DMetric.y &&
 						   s2DMetric.x < s1DMetric.x) {
 					continue;
 				} else {
 					double combinedCost = Util.getAndPlanCost(p1, p2, plans, cm);
 					LogicalAndTerm union = Util.getUnionTerm(p1.subset,
 															 p2.subset);
 					int idx = Util.getIndexOfSubset(plans, union);
 					if (combinedCost < plans.get(idx).c) {
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
 
 	public ArrayList<PlanRecord> generateAllPlans(LogicalAndTerm terms) {
 		plans = createPlanRecordsArray(terms);
 		return plans;
 	}
 
 	/**
 	 * Creates an array of 2^k possible plan records from the given terms.
 	 * This corresponds to the 'Array A' in the 'Selection Conditions in Main
 	 * Memory' paper by Ken Ross.
 	 * @param terms
 	 */
 	public static ArrayList<PlanRecord> createPlanRecordsArray(LogicalAndTerm
 																terms) {
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
 			// TODO: change getDefaultCostModel to model from config file
 			CostModel cm = CostModel.getDefaultCostModel();
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
