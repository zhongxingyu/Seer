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
 	Float[] dMetric;
 	//String elementsString;
 	
 	Term() {
 	}
 	
 	Term(Integer[] givenRep){
 		this.rep = givenRep;
 		numVars();
 	}
 	
 	public float calcProductivites(float[] select){
 		float answer = 1.0f;
 		for (int i = 0; i < this.rep.length; i++) {
 			if(this.rep[i] == 1){
 				answer = answer * select[i];
 			}
 		}
 		return answer;
 	}
 	
 	public boolean biggerD(Float[] d1, Float[] comp){
 		boolean answer = false;
 		if (d1[0] < comp[0] && d1[1] < comp[1]){
 			answer = true;
 		}
 		return answer;
 	}
 	
 	public Float[] largestDMetric() {
 		Float[] max = new Float[2];
 		max[0] = this.dMetric[0];
 		max[1] = this.dMetric[1];
 		Term term = this;
 		ArrayList<Float[]> compares = new ArrayList<Float[]>();
 		ArrayList<Term> stackArrayList = new ArrayList<Term>();
 		stackArrayList.add(term);
 		boolean keepLooping = true;
 		while(keepLooping){
 			if(stackArrayList.get(0).leftSeq != null){
 				compares.add(stackArrayList.get(0).leftSeq.dMetric);
 				stackArrayList.add(stackArrayList.get(0).leftSeq);
 			}
 			if (stackArrayList.get(0).rightSeq != null) {
 				compares.add(stackArrayList.get(0).rightSeq.dMetric);
 				stackArrayList.add(stackArrayList.get(0).rightSeq);
 			}
 			stackArrayList.remove(0);
 			if(stackArrayList.size() == 0){
 				keepLooping = false;
 			}
 		}
 		for (Float[] comp : compares) {
 			if (biggerD(max, comp)){
 				max = comp;
 			}
 		}
 		return max;
 	}
 
 	public boolean compareDMetrics(Term t){
 		Float[] toComp = t.largestDMetric();
 		boolean answer = biggerD(this.dMetric, toComp);
 		return answer;
 	}
 	
 	public boolean compareCMetrics(Term t){
 		boolean answer = false;
 		t = t.getLeftMostTerm(t);
 		boolean d1 = false;
 		if (this.cMetric[0] < t.cMetric[0]){
 			d1 = true;
 		}
 		boolean d2 = false;
 		if (this.cMetric[1] < t.cMetric[1]){
 			d2 = true;
 		}
 		if (d1 && d2){
 			answer = true;
 		}
 		return answer;
 	}
 	
 	public Term getLeftMostTerm(Term t){
 		if(hasLeftChild()){
 			return t.leftSeq;
 		}
 		return t;
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
 			if (t1.rep[i] == 1 || t2.rep[i] == 1) {
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
 		//System.out.println(Term.repToString(combo) + " = " + val);
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
 		//System.out.println("For " + t.repToString() + " NoBranch is : "+ noBranch + " and sAnd is: " + sAnd + " So we choose: "+ t.algoName[t.costAlgo]);
 		t.calcCMetric(props, select);
 		t.calcDMetric(props, select);
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
 		Float[] tuple = new Float[2];
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
 	
 	//public String[] writeNode(Term t)
 
 	protected String termOut(Integer i){
 		return "t" + i + "[o" + i + "[i]]";
 	}
 	
 	public String[] determinePlanArrangement(){
 		String[] output = new String[2];
 		String ifConditional = "";
 		String jPlusAssignment = "";
 		
 		if (costAlgo < 2){ // not a && Term
 			String allTogether = "";
 			// Find out which functions this Term covers
 			for (int i = 0; i < this.rep.length; i++) {
 				if (this.rep[i] == 1){
 					if (allTogether.length() > 0){ allTogether += " & "; }
 					allTogether += termOut(i+1);
 				}
 			}
 			
 			if (costAlgo == 0) { // an & Term
 				ifConditional = allTogether;
 			}
 			else if (costAlgo == 1) { // a No-Branch Term
 				jPlusAssignment = allTogether;
 			}
 		}
 		else { // is a && Term, and so recurse the children
 			String[] outputChildL = this.leftSeq.determinePlanArrangement();
 			String[] outputChildR = this.rightSeq.determinePlanArrangement();
 			
 			jPlusAssignment = outputChildL[1];
 			if (jPlusAssignment.length() > 0 && outputChildR[1].length() > 0) { jPlusAssignment += " & "; }
 			jPlusAssignment += outputChildR[1];
 			
 			ifConditional = outputChildL[0];
 			if (ifConditional.length() > 0 && outputChildR[0].length() > 0) { ifConditional += " && "; }
 			ifConditional += outputChildR[0];
 		}
 		
		if (ifConditional.length() > 0 && varsPresent > 1) {
 			ifConditional = "(" + ifConditional + ")";
 		}
 		output[0] = ifConditional;
 		output[1] = jPlusAssignment;
 		return output;
 	}
 	
 	public void printCodeOutput(float[] probs){
 		System.out.println("======================================");
 		for (int i = 0; i < probs.length; i++) {
 			System.out.print(probs[i] + " ");
 		}
 		System.out.println();
 		System.out.println("--------------------------------------");
 		boolean[] output = {false,false,false,false};
 		String[] outputStrings = new String[4];
 		String s0 = "if";
 		String s1 = "answer[j] = i;";
 		String s2 = "j += 1";
 		String s3 = "}";
 		String indent = "";
 
 		String[] plan = this.determinePlanArrangement();
 		if (plan[0].length() > 0) {
 			output[0] = true;
 			s0 += plan[0] + " {";
 			indent = "    ";
 			output[3] = true;
 		}
 		
 		output[1] = true;
 		s1 = indent + s1;
 		output[2] = true;
 		s2 = indent + s2;
 		
 		if (plan[1].length() > 0) {
 			s2 = indent + "j += (" + plan[1] + ");";
 		}
 		
 		outputStrings[0] = s0;
 		outputStrings[1] = s1;
 		outputStrings[2] = s2;
 		outputStrings[3] = s3;
 		
 		/*
 		if(!this.hasChildren()){
 			if(this.costAlgo == 1){
 				output[1] = true;
 				output[2] = true;
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
 		*/
 		//print the output
 		for (int i = 0; i < 4; i++) {
 			if (output[i]) {
 				System.out.println(outputStrings[i]);
 			}
 		}
 		System.out.println("--------------------------------------");
 		System.out.println("cost = " + this.cost);
 	}
 	
 }
 
 
