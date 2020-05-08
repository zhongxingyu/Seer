 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 
 
 public class Algorithm {
 
 	
 	public ArrayList<PlanRecord> plans;
 	private LogicalAndTerm terms; 
 	
 	public Algorithm(LogicalAndTerm terms) {
 		if (terms == null)
 			throw new IllegalArgumentException("terms cannot be null");
 		if (terms.isEmpty()) 
 			throw new IllegalArgumentException("terms cannot be empty");
 		
 		this.terms = terms;
 		this.plans = new ArrayList<PlanRecord>();
 	}
 	
 	public PlanRecord findOptimialPlan() {
 		generateAllPlans(terms);
 		
 		for(PlanRecord p1 : plans) {
 			LinkedHashSet<BasicTerm> set1 = Util.convertToSet(p1.subset);
 			for(PlanRecord p2 : plans) {
 				LinkedHashSet<BasicTerm> set2 = Util.convertToSet(p2.subset);
 				set1.retainAll(set2); 
				if (set1.size() > 0) {
 					System.out.println("Debug: Skipping Common set: "+ set1);
 					continue; 
 				}
 				
 				BranchingAndPlan p = makeBranchingAndPlan(p1.subset, p2.subset);
 				
 				if (false /* TODO(jervis): C-metric cost check */) {
 					
 				} else if (false /* TODO(jervis: D-metric cost check */) {
 					
 				} else {
 					double combinedCost = Util.planCost(p);
 					int lastIdx = plans.size() - 1;
 					if (combinedCost < plans.get(lastIdx).c) {
 						PlanRecord ans = plans.get(lastIdx); 
 						ans.c = combinedCost; 
 						ans.left = p1.subset.getSubsetNo(); 
 						ans.right = p2.subset.getSubsetNo(); 
 					}
 				}
 			}
 		}
 		int lastIdx = plans.size() - 1;
 		return plans.get(lastIdx);
 	}
 
 	private BranchingAndPlan makeBranchingAndPlan(LogicalAndTerm leftSubset, 
 												   LogicalAndTerm rightSubset) {
 		Plan left = new LogicalAndPlan(null, null, leftSubset.getTerms());
 		// TODO:(jervis): double-check that expression below is OK. 
 		Plan right = new LogicalAndPlan(null,null, rightSubset.getTerms());
 		BranchingAndPlan p = new BranchingAndPlan(left, right, null);
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
 			left = right = 0; 
 			int n = subset.size(); 
 			double p = subset.getSelectivity(); 
 			boolean b = false; 
 			double c = subset.getCost(CostModel.getDefaultCostModel());
 			if (subset.getNoBranchAlgCost() < c) {
 				c = subset.getNoBranchAlgCost(); 
 				b = true;
 			}
 			Plan plan = new LogicalAndPlan(null, null, subset.getTerms());
 			PlanRecord record = new PlanRecord(n,p,b,c,plan,left,right, subset); 
 			plans.add(record);
 		}
 		return plans;
 	}
 	
 	public static void isDisjoint(PlanRecord p1, PlanRecord p2) {
 		
 	}
 	
 }
