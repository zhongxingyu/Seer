 import java.util.ArrayList;
 
 
 /**
  * 
  * Represents a Collection of BasicTerms
  * @author Jervis
  */
 public class LogicalAndTerm {
 
 	private ArrayList<BasicTerm> data; 
 	/**
 	 * We use to label in what array index this subset is being stored in. 
 	 * It helps us to do easy look ups. 
 	 */
 	private int subset_no; 
 	public LogicalAndTerm(BasicTerm term) {
 		this.data  = new ArrayList<BasicTerm>();
 		data.add(term);		
 	}
 	public LogicalAndTerm() {
 		this.data  = new ArrayList<BasicTerm>();		
 	}
 	
 	public void add(BasicTerm term) {
 		data.add(term); 
 	}
 	
 	public ArrayList<BasicTerm> getTerms() {
 		return data; 
 	}
 	
 	public BasicTerm get(int i ) {
 		return data.get(i);
 	}
 	
 	public void setSubsetNo(int n) {
 		if (n < 0) {
 			throw new IllegalArgumentException(
 					"subset no can't be negative: " + n);
 		}
 		this.subset_no = n; 
 	}
 	
 	public int getSubsetNo() {
 		return this.subset_no; 
 	}
 	
 	
 	public double getNoBranchAlgCost() {
 		return Util.computeNoBranchCost(data, CostModel.getDefaultCostModel());
 	}
 	
 	public double getCost(CostModel cost) {
 		/**
 		 * General Cost Formula is as follows: 
 		 * 
 		 * kr * (k-1)*l + (f1 + ... + fk) + t + mq + (p1*...*pk)*a
 		 * Where: 
 		 * k = # of Basic terms
 		 * l = cost of performing a 'logical' "and" (i.e. & op)
 		 * f1 ... fk = Cost of doing function evals on all K functions. 
 		 * t = Cost of an if test evaluation
 		 * m = Cost of branch misprediction
 		 * p1 *...* pk = Compound / combined selectivity of all K basic terms
 		 * r = Cost of accessing an array element
 		 * q = p1*...*pk if p1*...*pk <= 0.5 ; else q = 1 - (p1*...*pk)
 		 * a = cost of writing an answer to the answer array. 
 		 */
 		double total = 0; 
 		double selectivities = getSelectivity(); 
 		double k = size();
 		int l = cost.l;
 		int t = cost.t;
 		int m = cost.m;
 		int r = cost.r; 
 		int f = cost.f; 
 		double q = selectivities <= 0.5 ? selectivities : 1 - selectivities;
 		int a = cost.a; 
 		
 		// Note: we assume each function has equal cost that's wy
 		// we multiply it (f) by # of terms (k)	
 		return k*r + (k-1)*l + k*f + t + m*q + selectivities*a;
 	}
 	
 	public int size() {
 		return data.size(); 
 	}
 	
 	/**
 	 * Computes the fixed cost for this &-term. 
 	 * Fixed cost is the cost that does not vary with the selectivity
 	 * of the terms.  
 	 * @return
 	 */
	public double getFixedCosst(CostModel cm) {
 		/**
 		 * fixed cost = k*r + (k-1)*l + f1 + ... + fk + t
 		 * where :
 		 *    k = # of basic terms
 		 *    r = cost of accessing an array element
 		 *    l = cost of performing a logical and op
 		 *    f1 + ... fk = cost of applying function 1 through k to its arg
 		 *    t = cost of performing the if test 
 		 */
 		
 		if (cm == null)
 			throw new IllegalArgumentException("Cannot have null cost model");
 		
 		
 		int k = data.size();
 		
		
		
 	}
 	
 	/**
 	 * Computes the compound selectivity of all the basic terms
 	 * that we have. 
 	 * @return
 	 */
 	public double getSelectivity() {
 		if (isEmpty())
 			return 0; 
 		double start = data.get(0).selectivity;
 		double result = start; 
 		for (int i = 1; i < data.size(); ++i) {
 			result *= data.get(i).selectivity;
 		}
 		return result; 
 	}
 	
 	public boolean isEmpty() {
 		return size() == 0; 
 	}
 	
 	public String toString() {
 		StringBuffer sb = new StringBuffer();
 		for (BasicTerm t : data) {
 			sb.append(t.toString());
 			sb.append(" & "); 
 			sb.append("\n");
 		}
 		return sb.toString(); 
 	}
 }
