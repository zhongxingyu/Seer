 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.Properties;
 import com.sun.corba.se.spi.monitoring.StatisticMonitoredAttribute;
 import com.sun.corba.se.spi.orb.StringPair;
 
 
 public class Term {
 	//public BitSet rep;
 	public Integer[] rep;
 	Term leftSeq;
 	Term rightSeq;
 	int varsPresent = 0;
 	String[] algoName = {"Single&", "NoBranch", "&&"};
 	int costAlgo = -1; //0 = single&, 1=NoBranch, 2 = &&
 	float cost;
 	float[] cMetric;
 	float[] dMetric;
 	//String elementsString;
 	
 	Term() {
 	}
 	
 	Term(Integer[] givenRep){
 		this.rep = givenRep;
 		numVars();
 	}
 	
 	public boolean compareCmetric(Term t){
 		return false;
 	}
 	
 	public void numVars(){
 		this.varsPresent = 0;
 		for (int i = 0; i < this.rep.length; i++) {
 			if (this.rep[i] == 1){
 				this.varsPresent++;
 			}
 		}
 	}
 	
 	public boolean hasLeftChild(){
 		boolean ans = (leftSeq != null);
 		return ans;
 	}
 	
 	public boolean hasChildren(){
 		boolean ans = false;
 		if (leftSeq != null || rightSeq !=null){
 			ans = true;
 		}
 		return ans;
 	}
 	
 	public boolean hasSameTerms(Term t){
 		boolean answer = true;
 		for (int i = 0; i < this.rep.length; i++) {
 			if (t.rep[i] != this.rep[i]){
 				answer = false;
 			}
 		}
 		return answer;
 	}
 	
 	public static int calcValue(Term t1, Term t2) {
 		int[] combo = new int[t1.rep.length];
 		for (int i = 0; i < combo.length; i++) {
			if (t1.rep[i] == 1 || t1.rep[i] == 1) {
 				combo[i] = 1;
 			}
 			else combo[i] = 0;
 		}
 		int val = 0;
 		for (int i = 0; i < combo.length; i++) {
 			if (combo[i] == 1) {
 				val += Math.pow(2, i);
 			}
 		}
 		return val;
 	}
 	
 	public static int[] combinedRep(Term t1, Term t2){
 		int[] combo = new int[t1.rep.length];
 		for (int i = 0; i < combo.length; i++) {
 			if (t1.rep[i] == 1 || t1.rep[i] == 1) {
 				combo[i] = 1;
 			}
 			else combo[i] = 0;
 		}
 		return combo;
 	}
 	
 	public boolean canCombine(Term t){
 		boolean answer = true;
 		for (int i = 0; i < this.rep.length; i++) {
 			if (t.rep[i] == 1 && this.rep[i] == 1){
 				answer = false;
 			}
 		}
 		return answer;
 	}
 	
 	public void addLeft(Term lt){
 		this.leftSeq = lt;
 	}
 	
 	public void addRight(Term rt){
 		this.rightSeq = rt;
 	}
 	
 	public static void fillArrayCosts(ArrayList<Term> termsArrayList, Properties props, float[] select){
 		for (Term t : termsArrayList) {
 			calculateCost(t, props, select);
 		}
 	}
 	
 	public static void calculateCost(Term t, Properties props, float[] select){
 		float[] answer = new float[2];
 		float sAnd = calcSAnd(t.rep, props, select);
 		float noBranch = calcNoBranch(t.rep, props);
 		if (noBranch < sAnd){
 			t.cost = noBranch;
 			t.costAlgo = 1;
 		}
 		else {
 			t.cost = sAnd;
 			t.costAlgo = 0;
 		}
 		//System.out.println("NoBranch is : "+ noBranch + " and sAnd is: " + sAnd + " So we choose: "+ t.algoName[t.costAlgo]);
 		t.calcCMetric(props, select);
 	}
 	
 	public String repToString(){
 		String s = "";
 		for (int i = 0; i < this.rep.length; i++) {
 			s += this.rep[i];
 		}
 		return s;
 	}
 	public static String repToString(int[] rep){
 		String s = "";
 		for (int i = 0; i < rep.length; i++) {
 			s += rep[i];
 		}
 		return s;
 	}
 	public static float calcNoBranch(Integer[] vars, Properties props){
 		float k = 0;
 		for (int i = 0; i < vars.length; i++) {
 			if (vars[i] == 1) {
 				k++;
 			}
 		}
 		float cost = 0.0f;
 		cost = k * Float.valueOf(props.getProperty("r"));
 		cost += (k-1) * Float.valueOf(props.getProperty("l"));
 		cost += k * Float.valueOf(props.getProperty("f"));
 		cost += Float.valueOf(props.getProperty("a"));
 		return cost;
 	}
 	
 	public static float calcSAnd(Integer[] vars, Properties props, float[] select){
 		int k = 0;
 		for (int i = 0; i < vars.length; i++) {
 			if (vars[i] == 1) {
 				k++;
 			}
 		}
 		float q = 0;
 		float product = 1.0f;
 		for (int i = 0; i < select.length; i++) {
 			if (vars[i] == 1) {
 				product = product * select[i];
 			}
 		}
 		if (product > 0.5){
 			q = (1.0f - product);
 		}
 		else {
 			q = product;
 		}
 		float cost = 0;
 		cost = k * Float.valueOf(props.getProperty("r"));
 		cost += (k-1) * Float.valueOf(props.getProperty("l"));
 		cost += k * Float.valueOf(props.getProperty("f"));
 		cost += Float.valueOf(props.getProperty("t"));
 		cost += q * Float.valueOf(props.getProperty("m"));
 		cost += product * Float.valueOf(props.getProperty("a"));
 		return cost;
 	}
 	/*
 	public float calcDoubAnd(float costPlan2, Properties props, float[] select){
 		int k = 0;
 		for (int i = 0; i < this.rep.length; i++) {
 			if (this.rep[i] == 1) {
 				k++;
 			}
 		}
 		float fcost = k * Float.valueOf(props.getProperty("r"));
 		fcost += (k-1)*Float.valueOf(props.getProperty("l"));
 		fcost += k*Float.valueOf(props.getProperty("f"));
 		fcost += Float.valueOf(props.getProperty("t"));
 		float q = 0;
 		float product = 1.0f;
 		for (int i = 0; i < select.length; i++) {
 			if (this.rep[i] == 1) {
 				product = product * select[i];
 			}	
 		}
 		if (product > 0.5){
 			q = (1.0f - product);
 		}
 		else {
 			q = product;
 		}
 		float planCost = fcost;
 		planCost += q * Float.valueOf(props.getProperty("m"));
 		planCost += product * costPlan2;
 		return planCost;
 	}
 	*/
 	public float calcDoubAnd(Term t, Properties props, float[] select){
 		int k = 0;
 		this.numVars();
 		k = this.varsPresent;
 		float fcost = k * Float.valueOf(props.getProperty("r")); //fcost = kr
 		fcost += (k-1)*Float.valueOf(props.getProperty("l")); // + (k-1)l
 		fcost += k*Float.valueOf(props.getProperty("f")); // + kf
 		fcost += Float.valueOf(props.getProperty("t")); // + t
 		float q = 0;
 		float product = 1.0f;
 		float prod2 = 10.f;
 		for (int i = 0; i < select.length; i++) {
 			if (this.rep[i] == 1) {
 				product = product * select[i];
 				//System.out.print(select[i] + " * ");
 			}	
 		}
 		//System.out.println("= " + product + " from " + this.repToString() + " and " + t.repToString());
 		q = Math.min(product, (1-product));
 		float planCost = fcost;
 		planCost += q * Float.valueOf(props.getProperty("m")); //cost = fcost + q*m
 		planCost += product * t.cost; // + p*C
 		return planCost;
 	}
 	
 	public void calcCMetric(Properties props, float[] select){
 		float[] tuple = new float[2];
 		int k = 0;
 		for (int i = 0; i < this.rep.length; i++) {
 			if (this.rep[i] == 1) {
 				k++;
 			}
 		}
 		float fcost = k * Float.valueOf(props.getProperty("r"));
 		fcost += (k-1)*Float.valueOf(props.getProperty("l"));
 		fcost += k*Float.valueOf(props.getProperty("f"));
 		fcost += Float.valueOf(props.getProperty("t"));
 		float q = 0;
 		float product = 1.0f;
 		for (int i = 0; i < select.length; i++) {
 			product = product * select[i];
 		}
 		if (product > 0.5){
 			q = (1.0f - product);
 		}
 		else {
 			q = product;
 		}
 		float inter = (product - 1.0f);
 		inter = inter/fcost;
 		tuple[0] = inter;
 		tuple[1] = product;
 		this.cMetric = tuple;
 	}
 	
 	public void calcDMetric(Properties props, float[] select){
 		float[] tuple = new float[2];
 		int k = 0;
 		for (int i = 0; i < this.rep.length; i++) {
 			if (this.rep[i] == 1) {
 				k++;
 			}
 		}
 		float fcost = k * Float.valueOf(props.getProperty("r"));
 		fcost += (k-1)*Float.valueOf(props.getProperty("l"));
 		fcost += k*Float.valueOf(props.getProperty("f"));
 		fcost += Float.valueOf(props.getProperty("t"));
 		float product = 1.0f;
 		for (int i = 0; i < select.length; i++) {
 			product = product * select[i];
 		}
 		tuple[0] = fcost;
 		tuple[1] = product;
 		this.dMetric = tuple;
 	}
 	
 	public static ArrayList<Term> generateTermArray(int card) {
 		ArrayList<Integer[]> seqArr = Term.getTermsArray(card);
 		ArrayList<Term> termArr = new ArrayList<Term>();
 		for(Integer[] elem : seqArr){
 			Term tempTerm = new Term(elem);
 			termArr.add(tempTerm);
 		}
 		//System.out.print("There are " + termArr.size() + " elements");
 		return termArr;
 	}
 	
 	public static ArrayList<Integer[]> getTermsArray(int card){
 	    int numRows = (int)Math.pow(2, card);
 	    ArrayList<Integer[]> varArr = new ArrayList<Integer[]>();
 	    for(int i = 0;i<numRows;i++)
 	    {
 	    	Integer[] vars = new Integer[card];
 	        for(int j = 0; j < card; j++)
 	        {
 	            int val = numRows * j + i;
 	            int ret = (1 & (val >>> j));
 	            if (ret != 0){
 	            	vars[j] = 1;
 	            }
 	            else {
 					vars[j] = 0;
 				}
 	            //System.out.print(bitArrs[i][j]);
 	        }
 	        boolean notAllZeros = false;
 	        for (int j = 0; j < vars.length; j++) {
 				if(vars[j] == 1){
 					notAllZeros = true;
 				}
 			}
 	        if (notAllZeros){
 	        	varArr.add(vars);
 	        }
 	        
 	        //System.out.println();
 	    }
 	    /*//printing code
 	    for (int i = 0; i < varArr.size(); i++) {
 	    	System.out.print(i + ": ");
 	    	if (i < (numRows)){
 				for (int j = 0; j < card; j++) {
 					System.out.print(varArr.get(i)[j]);
 				}
 			}	
 			System.out.println();
 		}
 		*/
 	    return varArr;
 	}
 	
 	public void printCodeOutput(float[] probs){
 		System.out.println("======================================");
 		for (int i = 0; i < probs.length; i++) {
 			System.out.print(probs[i] + " ");
 		}
 		System.out.println();
 		System.out.println("--------------------------------------");
 		int[] output = {0,0,0,0};
 		String[] outputStrings = new String[4];
 		String s1 = "if(";
 		outputStrings[0] = s1;
 		String s2 = "answer[j] = i;";
 		outputStrings[1] = s2;
 		String s3 = "j += (";
 		outputStrings[2] = s3;
 		String s4 = "}";
 		outputStrings[3] = s4;
 		if(!this.hasChildren()){
 			if(this.costAlgo == 1){
 				output[1] = 1;
 				output[2] = 1;
 				for (int i = 1; i < this.rep.length; i++) {
 					String newS = "t" + i + "[o" + i + "[i]] & ";
 					//System.out.println(newS);
 					outputStrings[2] = outputStrings[2].concat(newS);
 					//System.out.println(outputStrings[2]);
 				}
 				int lastInt = this.rep.length;
 				outputStrings[2] = outputStrings[2].concat("t" + lastInt + "[o" + lastInt + "[i]]);");
 			}
 		}
 		//print the output
 		for (int i = 0; i < 4; i++) {
 			if (output[i] == 1) {
 				System.out.println(outputStrings[i]);
 			}
 		}
 		System.out.println("--------------------------------------");
 		System.out.println("cost = " + this.cost);
 	}
 	
 }
 
 
