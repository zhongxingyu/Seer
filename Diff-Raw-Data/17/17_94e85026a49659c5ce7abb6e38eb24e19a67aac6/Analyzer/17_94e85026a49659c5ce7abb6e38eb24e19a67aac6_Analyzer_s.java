 package edu.gmu.linguistics;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Arrays;
 import java.util.Stack;
 import java.util.ArrayList;
 
 public class Analyzer {
 	
 	// Create the alhpabet;
     Alphabet phonetic; 
    
     // Variables that control certain processing rules
 	Double cskip;
 	Double csub;
 	Double cexp;
 	Double epsilon;
 	
 	// class variables to track psps
 	public static Map psp_map;
 	
 	
 	public void clear_psp_count() {
 		this.psp_map = new HashMap();    // hash table
 	    this.psp_map = new TreeMap();        // sorted map
 	    
 	}
 	
 	private void increment_psp_count(String psp_type, int count) {
 		
 		if (this.psp_map.containsKey(psp_type)) {
 		 	Integer temp_value;
 			temp_value = (Integer) this.psp_map.get(psp_type);
 			this.psp_map.put(psp_type, temp_value + count);
 		}
 		else {
 			this.psp_map.put(psp_type, count);
 		}
 
 	}
 
 	public void print_psp_count() {
 		Iterator it = psp_map.entrySet().iterator();
 	    while (it.hasNext()) {
 	        Map.Entry pairs = (Map.Entry)it.next();
 	        System.out.println(pairs.getKey() + ", " + pairs.getValue());
 	    }
 		
 	}
 
 	
     public Analyzer(Alphabet newalphabet) {
     	this.clear_psp_count();
     	phonetic = newalphabet;
 		//populate initial settings for weighting factors
 		cskip = -10.0;
 		csub = 35.0;
 		cexp = 45.0;
 		epsilon = 0.0;
 		
 	}
 	
     public Double exp(String p, String q1, String q2){
 		Double maxvowel;
 		if (phonetic.vowel(q1) >= phonetic.vowel(q2)){
 			maxvowel = (Double) phonetic.vowel(q1);
 		}
 		else {
 			maxvowel = (Double) phonetic.vowel(q2);
 		}
 		
 		return cexp - phonetic.totalDiff(p, q1) - phonetic.totalDiff(p, q2) - phonetic.vowel(p) - maxvowel;
 	}
 	
     public Double sub(String p, String q){
     	return csub - phonetic.totalDiff(p, q) - phonetic.vowel(p) - phonetic.vowel(q);
     }
 	
     public Double skip(String p) {
     	return cskip;
     }
 	
 	public double alignment(String x, String y) {
 		int lengthx, lengthy;
 		lengthx = x.length();
 		lengthy = y.length();
 		
 		int i = 0;
 		Double[][] matrix = new Double[lengthx+1][lengthy+1];
 		while (i < lengthx+1) {
 			Arrays.fill(matrix[i],0.0);
 			i = i + 1;
 		}
 		
 		Double [] temp;
 		i = 1;
 		int j;
 		while (i < (lengthx + 1)) {
 			j = 1;
 			while (j < (lengthy + 1)) {
 				temp = new Double[6];
 				
 				// inner processing for alignment
 				// the heavy lifting starts
 				
 				if (i-1 < 0 || j-1 < 0) {
 					temp[0] = -9999999.0 + this.skip(x.substring(i-1,i));
 				}
 				else {
 					temp[0] = matrix[i-1][j] + this.skip(x.substring(i-1,i));
 				}
 				
 				////////////////
 				
 				if (i-1 < 0|| j-1 < 0) {
 					temp[1] = -9999999.0 + this.skip(y.substring(j-1,j));	
 				}
 				else {
 					temp[1] = matrix[i][j-1] + this.skip(y.substring(j-1,j));
 				}
 				
 				////////////////
 				
 				if (i < 0|| j < 0) {
 					temp[2] = -9999999.0 + this.sub(x.substring(i-1,i),y.substring(j-1,j));	
 				}
 				else {
 					temp[2] = matrix[i-1][j-1] + this.sub(x.substring(i-1,i),y.substring(j-1,j));
 				}
 				
 				////////////////
 
 				if (i-1 < 0|| j-2 < 0) {
 					temp[3] = -9999999.0;// + this.exp(x.substring(i-1,i),y.substring(j-2,j-1),y.substring(j-1,j));	
 				}
 				else {
 					temp[3] = matrix[i-1][j-2] + this.exp(x.substring(i-1,i),y.substring(j-2,j-1),y.substring(j-1,j));
 				}
 				
 				////////////////
 
 				if (i-2 < 0|| j-1 < 0) {
 					temp[4] = -9999999.0;// + this.exp(x.substring(i-2,i-1),x.substring(i-1,i),y.substring(j-2,j));	
 				}
 				else {
 					temp[4] = matrix[i-2][j-1] + this.exp(x.substring(i-2,i-1),x.substring(i-1,i),y.substring(j-1,j));
 				}
 				
 				////////////////
 		
 
 				
 				temp[5] = 0.0;
 
 				Arrays.sort(temp);
 				
 				
 				matrix[i][j] = temp[5];
 				
 				// closing inner loop
 				j = j + 1;
 			}
 			//closing outer loop
 			i = i + 1;
 		}
 		// loops closed
 		
 		
 		// Start retrieval
 		Double endscore, stack;
 		// set stack value to initial placeholder value...
 		stack = 0.0;
 		
 		
 		
 		endscore = (1.0 - epsilon) * matrix[lengthx][lengthy];
 		
 		Stack<String> answerstack = new Stack<String>();
 		Stack<String> alignment = new Stack<String>();
 		
 	
 		i = 0;
 		while (i <= lengthx ) {
 			j = 0;
 			while (j <= lengthy) {
 				if (matrix[i][j] >= endscore) {
 					answerstack = new Stack<String>();
					alignment = this.retrieve(x, y, i, j, 0.0, matrix, endscore, answerstack);
 				}
 				j += 1;
 			}
 			i += 1;
 		}
 			
 		String line = "";
 		
 		
 		while (!answerstack.empty()) {
 			line += answerstack.pop() + "\n";
 		}
 		
 		//System.out.println("should now print alignment...");
 		System.out.println(line);
 		
 		return endscore;
 	}
 
 	public double alignment(ArrayList x, ArrayList y) {
 		int lengthx, lengthy;
 		lengthx = x.size();
 		lengthy = y.size();
 		
 		int i = 0;
 		Double[][] matrix = new Double[lengthx+1][lengthy+1];
 		while (i < lengthx+1) {
 			Arrays.fill(matrix[i],0.0);
 			i = i + 1;
 		}
 		
 		Double [] temp;
 		i = 1;
 		int j;
 		while (i < (lengthx + 1)) {
 			j = 1;
 			while (j < (lengthy + 1)) {
 				temp = new Double[6];
 				
 				// inner processing for alignment
 				// the heavy lifting starts
 				
 				if (i-1 < 0 || j-1 < 0) {
 					temp[0] = -9999999.0 + this.skip((String)x.get(i-1));
 				}
 				else {
 					temp[0] = matrix[i-1][j] + this.skip((String)x.get(i-1));
 				}
 				
 				////////////////
 				
 				if (i-1 < 0|| j-1 < 0) {
 					temp[1] = -9999999.0 + this.skip((String)y.get(j-1));	
 				}
 				else {
 					
 					temp[1] = matrix[i][j-1] + this.skip((String)y.get(j-1));
 				}
 				
 				////////////////
 				
 				if (i < 0|| j < 0) {
 					temp[2] = -9999999.0 + this.sub((String)x.get(i-1),(String)y.get(j-1));	
 				}
 				else {
 					temp[2] = matrix[i-1][j-1] + this.sub((String)x.get(i-1),(String)y.get(j-1));
 				}
 				
 				////////////////
 
 				if (i-1 < 0|| j-2 < 0) {
 					temp[3] = -9999999.0;// + this.exp(x.substring(i-1,i),y.substring(j-2,j-1),y.substring(j-1,j));	
 				}
 				else {
 					temp[3] = matrix[i-1][j-2] + this.exp((String)x.get(i-1),(String)y.get(j-2),(String)y.get(j-1));
 				}
 				
 				////////////////
 
 				if (i-2 < 0|| j-1 < 0) {
 					temp[4] = -9999999.0;// + this.exp(x.substring(i-2,i-1),x.substring(i-1,i),y.substring(j-2,j));	
 				}
 				else {
 					temp[4] = matrix[i-2][j-1] + this.exp((String)x.get(i-2),(String)x.get(i-1),(String)y.get(j-1));
 				}
 				
 				////////////////
 		
 
 				
 				temp[5] = 0.0;
 
 				Arrays.sort(temp);
 				
 				
 				matrix[i][j] = temp[5];
 				
 				// closing inner loop
 				j = j + 1;
 			}
 			//closing outer loop
 			i = i + 1;
 		}
 		// loops closed
 		
 		
 		// Start retrieval
 		Double endscore, stack;
 		// set stack value to initial placeholder value...
 		stack = 0.0;
 		
 		
 		
 		endscore = (1.0 - epsilon) * matrix[lengthx][lengthy];
 		
 		Stack<String> answerstack = new Stack<String>();
 		Stack<String> alignment = new Stack<String>();
 		
 		Map psptrack = new HashMap();    // hash table
 	    psptrack = new TreeMap();        // sorted map
 	    
 
 
 
 		System.out.println("Prepout");
 		
 		i = 0;
 		while (i <= lengthx) {
 			j = 0;
 			while (j <= lengthy) {
 				if (matrix[i][j] >= endscore) {
 					answerstack = new Stack<String>();
 					alignment = this.retrieve(x, y, i, j, 0.0, matrix, endscore, answerstack);
 				}
 				j += 1;
 			}
 			i += 1;
 		}
 			
 		String line = "";
 		while (!answerstack.empty()) {
 			line += answerstack.pop() + "\n";
 		}
 
 	
 		
 		System.out.println(line);
 		
 		System.out.println("printing alignment: " + alignment.toString());
 		
 		System.out.println(endscore);
 		
 		
 		return endscore;
 	}
 
 
 	public String alignment_word(ArrayList x, ArrayList y) {
 		int lengthx, lengthy;
 		lengthx = x.size();
 		lengthy = y.size();
 		
 		int i = 0;
 		Double[][] matrix = new Double[lengthx+1][lengthy+1];
 		while (i < lengthx+1) {
 			Arrays.fill(matrix[i],0.0);
 			i = i + 1;
 		}
 		
 		Double [] temp;
 		i = 1;
 		int j;
 		while (i < (lengthx + 1)) {
 			j = 1;
 			while (j < (lengthy + 1)) {
 				temp = new Double[6];
 				
 				// inner processing for alignment
 				// the heavy lifting starts
 				
 				if (i-1 < 0 || j-1 < 0) {
 					temp[0] = -9999999.0 + this.skip((String)x.get(i-1));
 				}
 				else {
 					temp[0] = matrix[i-1][j] + this.skip((String)x.get(i-1));
 				}
 				
 				////////////////
 				
 				if (i-1 < 0|| j-1 < 0) {
 					temp[1] = -9999999.0 + this.skip((String)y.get(j-1));	
 				}
 				else {
 					
 					temp[1] = matrix[i][j-1] + this.skip((String)y.get(j-1));
 				}
 				
 				////////////////
 				
 				if (i < 0|| j < 0) {
 					temp[2] = -9999999.0 + this.sub((String)x.get(i-1),(String)y.get(j-1));	
 				}
 				else {
 					temp[2] = matrix[i-1][j-1] + this.sub((String)x.get(i-1),(String)y.get(j-1));
 				}
 				
 				////////////////
 
 				if (i-1 < 0|| j-2 < 0) {
 					temp[3] = -9999999.0;// + this.exp(x.substring(i-1,i),y.substring(j-2,j-1),y.substring(j-1,j));	
 				}
 				else {
 					temp[3] = matrix[i-1][j-2] + this.exp((String)x.get(i-1),(String)y.get(j-2),(String)y.get(j-1));
 				}
 				
 				////////////////
 
 				if (i-2 < 0|| j-1 < 0) {
 					temp[4] = -9999999.0;// + this.exp(x.substring(i-2,i-1),x.substring(i-1,i),y.substring(j-2,j));	
 				}
 				else {
 					temp[4] = matrix[i-2][j-1] + this.exp((String)x.get(i-2),(String)x.get(i-1),(String)y.get(j-1));
 				}
 				
 				////////////////
 		
 
 				
 				temp[5] = 0.0;
 
 				Arrays.sort(temp);
 				
 				
 				matrix[i][j] = temp[5];
 				
 				// closing inner loop
 				j = j + 1;
 			}
 			//closing outer loop
 			i = i + 1;
 		}
 		// loops closed
 		
 		
 		// Start retrieval
 		Double endscore, stack;
 		// set stack value to initial placeholder value...
 		stack = 0.0;
 		
 		
 		
 		endscore = (1.0 - epsilon) * matrix[lengthx][lengthy];
 		
 		Stack<String> answerstack = new Stack<String>();
 		Stack<String> alignment = new Stack<String>();
 		
 
 
 		System.out.println("-------asfasf----");
 		System.out.println(x.size());
 		System.out.println("-------asfasf----");
 		
 		i = 0;
 		while (i <= lengthx) {
 			j = 0;
 			while (j <= lengthy) {
 				if (matrix[i][j] >= endscore) {
 					answerstack = new Stack<String>();
 					alignment = this.retrieve(x, y, i, j, 0.0, matrix, endscore, answerstack);
 				}
 				j += 1;
 			}
 			i += 1;
 		}
 			
 		String line = "";
 		while (!answerstack.empty()) {
 			line += answerstack.pop() + "\n";
 		}
 
 	
 		
 		return line;
 	}
 	
 	
 	private Stack<String> retrieve2(String x, String y, int i, int j, Double score, Double [][] matrix, Double t, Stack<String> answerstack){
 		Double tempscore = 0.0;
 		String alignstring = "";
 		
 		System.out.println("starting inner retrieve focus");
 		
 		if (matrix[i][j] == 0.0) {
 			//return 0.0; 
 			System.out.println("base condition");
 
 			return answerstack;
 		}
 		else {
 			
 			// start
 			if (i-1 < 0 || j-1 < 0) {
 				tempscore = -9999999.0;
 			}
 			else {
 				tempscore = matrix[i-1][j-1];
 			
 				if ((tempscore + this.sub(x.substring(i-1,i), y.substring(j-1,j)) + score) >= t){
 					//alignstring = "Align " + x.substring(i-1,i) + " with " + y.substring(j-1,j);
 					alignstring = x.substring(i-1,i) + " --- " + y.substring(j-1,j);
 					
 					answerstack.push(alignstring);
					return this.retrieve(x,y,i-1,j-1,score + this.sub(x.substring(i-1,i), y.substring(j-1,j)), matrix, t, answerstack);
 				}
 			
 			
 			}
 			
 			
 			
 			
 			if (i < 0 || (j - 1) < 0) {
 				tempscore = -9999999.0;
 			}
 			else {
 				tempscore = matrix[i][j-1];				
 
 				if ((tempscore + this.skip(y.substring(j-1,j)) + score) >= t) {
 					//alignstring = "Align null with " + y.substring(j-1,j);
 					alignstring = " --- " + y.substring(j-1,j);
 					
 					answerstack.push(alignstring);
					return this.retrieve(x, y, i, j-1, score + this.skip(y.substring(j-1,j)), matrix, t, answerstack );			
 				}
 
 			
 			}
 			
 			if ((i - 1) < 0 || (j - 2) < 0) {
 				tempscore = -9999999.0;
 			}
 			else {
 				tempscore = matrix[i-1][j-2];				
 
 				if ((tempscore + this.exp(x.substring(i-1,i), y.substring(j-1-1,j-1), y.substring(j-1,j)) + score) >= t) {
 					//alignstring = "Align " + x.substring(i-1,i) + " with " + y.substring(j-1-1,j-1) + " " + y.substring(j-1,j);
 					alignstring = x.substring(i-1,i) + " --- " + y.substring(j-1-1,j-1) + " " + y.substring(j-1,j);
 					
 					answerstack.push(alignstring);
					return this.retrieve(x, y, i-1, j-2, score + this.exp(x.substring(i-1,i), y.substring(j-1-1,j-1), y.substring(j-1,j)), matrix, t, answerstack );			
 				}
 
 				
 			}
 			
 				
 			if ((i - 1) < 0 || j < 0) {
 				tempscore = -9999999.0;
 			}
 			else {
 				tempscore = matrix[i-1][j];				
 
 				if ((tempscore + this.skip(x.substring(i-1,i)) + score) >= t) {
 					//alignstring = "Align " + x.substring(i-1,i) + " with null";
 					alignstring = x.substring(i-1,i) + " --- ";
 					answerstack.push(alignstring);
					return this.retrieve(x, y, i-1, j, score + this.skip(x.substring(i-1,i)), matrix, t, answerstack );			
 				}
 
 			
 			}
 		    
 			if ((i - 2) < 0 || (j - 1) < 0) {
 				tempscore = -9999999.0;
 			}
 			else {
 				tempscore = matrix[i-2][j-1];				
 
 				if ((tempscore + this.exp(y.substring(j-1,j), x.substring(i-1-1, i-1), x.substring(i-1, i)) + score) >= t) {
 					//alignstring = "Align " + x.substring(i-1-1,i-1) + " " + x.substring(i-1,i) + " with " + y.substring(j-1,j);
 					alignstring = x.substring(i-1-1,i-1) + " " + x.substring(i-1,i) + " --- " + y.substring(j-1,j);
 					answerstack.push(alignstring);
					return this.retrieve(x, y, i-2, j-1, score + this.exp(y.substring(j-1,j), x.substring(i-1-1, i-1), x.substring(i-1, i)), matrix, t, answerstack );			
 				}
 
 			
 			}
 			
 			
 		}
 		return answerstack;
 	}
 
 	
 	private Stack<String> retrieve(ArrayList x, ArrayList y, int i, int j, Double score, Double [][] matrix, Double t, Stack<String> answerstack){
 		Double tempscore = 0.0;
 		String alignstring = "";
 		
 		// this is the base condition to end recursion
 		if (matrix[i][j] == 0.0) {
 			//return 0.0; 
 			return answerstack;
 		}
 		else {
 			// start
 			if (i-1 < 0 || j-1 < 0) {
 				tempscore = -9999999.0;
 			}
 			else {
 
 				tempscore = matrix[i-1][j-1];
 				
 				if ((tempscore + this.sub((String)x.get(i-1), (String)y.get(j-1)) + score) >= t){
 					//alignstring = "Align " + x.get(i-1) + " with " + y.get(j-1);
 					alignstring = x.get(i-1) + " --- " + y.get(j-1);
 					
 					alignstring += " --- " + this.apply_rules((String)x.get(i-1), (String)y.get(j-1));
 					
 					if (x.size() == i && y.size() == j) {
 						alignstring += " " + this.apply_word_final_rules((String)x.get(i-1), (String)y.get(j-1));
 					}
 					answerstack.push(alignstring);
 					return this.retrieve(x,y,i-1,j-1,score + this.sub((String)x.get(i-1), (String)y.get(j-1)), matrix, t, answerstack);
 				}
 			
 			
 			}
 			
 			
 			if (i < 0 || (j - 1) < 0) {
 				tempscore = -9999999.0;
 			}
 			else {
 				tempscore = matrix[i][j-1];				
 
 				if ((tempscore + this.skip((String)y.get(j-1)) + score) >= t) {
 					//alignstring = "Align null with " + y.get(j-1);
 					alignstring = " --- " + y.get(j-1);
 					
 					alignstring += " --- " + this.apply_deletion_rules(null, (String)y.get(j-1));
 					
 					
 					answerstack.push(alignstring);
 					return this.retrieve(x, y, i, j-1, score + this.skip((String)y.get(j-1)), matrix, t, answerstack );			
 				}
 
 			
 			}
 			
 			if ((i - 1) < 0 || (j - 2) < 0) {
 				tempscore = -9999999.0;
 			}
 			else {
 				tempscore = matrix[i-1][j-2];				
 
 				if ((tempscore + this.exp((String)x.get(i-1), (String)y.get(j-2), (String)y.get(j-1)) + score) >= t) {
 					//alignstring = "Align " + x.get(i-1) + " with " + y.get(j-2) + " " + y.get(j-1);
 					alignstring = x.get(i-1) + " --- " + y.get(j-2) + " " + y.get(j-1);
 					
 					answerstack.push(alignstring);
 					return this.retrieve(x, y, i-1, j-2, score + this.exp((String)x.get(i-1), (String)y.get(j-2), (String)y.get(j-1)), matrix, t, answerstack );			
 				}
 
 				
 			}
 			
 				
 			if ((i - 1) < 0 || j < 0) {
 				tempscore = -9999999.0;
 			}
 			else {
 				tempscore = matrix[i-1][j];				
 
 				if ((tempscore + this.skip((String)x.get(i-1)) + score) >= t) {
 					//alignstring = "Align " + x.get(i-1) + " with null";
 					alignstring = x.get(i-1) + " --- ";
 					alignstring += " --- " + this.apply_deletion_rules((String)x.get(i-1), null);
 					answerstack.push(alignstring);
 					return this.retrieve(x, y, i-1, j, score + this.skip((String)x.get(i-1)), matrix, t, answerstack );			
 				}
 
 			
 			}
 		    
 			if ((i - 2) < 0 || (j - 1) < 0) {
 				tempscore = -9999999.0;
 			}
 			else {
 				tempscore = matrix[i-2][j-1];				
 
 				if ((tempscore + this.exp((String)y.get(j-1), (String)x.get(i-2), (String)x.get(i-1)) + score) >= t) {
 					//alignstring = "Align " + x.get(i-2) + " " + x.get(i-1) + " with " + y.get(j-1);
 					alignstring = x.get(i-2) + " " + x.get(i-1) + " --- " + y.get(j-1);
 					answerstack.push(alignstring);
 					return this.retrieve(x, y, i-2, j-1, score + this.exp((String)y.get(j-1), (String)x.get(i-2),(String) x.get(i-1)), matrix, t, answerstack );			
 				}
 
 			
 			}
 			
 			
 		}
 		return answerstack;
 	}
 
 	
 	public String apply_rules(String left, String right) {
 		String core_left = phonetic.core_sound(left);
 		String core_right = phonetic.core_sound(right);
 		
 		String ruleReturn = "";
 		
 		// sound specific rules
 		if (core_left.equals("ɹ")) {
 			if (phonetic.sound_map(right).get("manner") == "trill") {
 				ruleReturn = "r to trill";
 				this.increment_psp_count("r to trill", 1);
 				
 			}
 			else if (core_left.equals("l")) {
 				ruleReturn = "r to l";
 				this.increment_psp_count("r to l", 1);
 
 			}
 			else if (phonetic.sound_map(right).get("manner") == "fricative" && phonetic.sound_map(right).get("place") == "uvular") {
 				ruleReturn = "r to uvular fricative";
 				this.increment_psp_count("r to uvular fricative", 1);
 
 			}
 		}
 		
 		
 		if (phonetic.sound_map(left).get("manner") == "fricative" && phonetic.sound_map(left).get("place") == "dental") {
 			if (phonetic.sound_map(right).get("manner") == "stop" && phonetic.sound_map(right).get("place") == "alveolar") {
 				ruleReturn = "interdental fricative to alveolar stop";
 				this.increment_psp_count("interdental fricative to alveolar stop", 1);
 
 			}
 			else if (phonetic.sound_map(right).get("manner") == "fricative" && (phonetic.sound_map(right).get("place") == "labial" || phonetic.sound_map(right).get("place") == "labiodental")) {
 				ruleReturn = "interdental fricative to labial fricative";
 				this.increment_psp_count("interdental fricative to labial fricative", 1);
 
 			}
 			else if (phonetic.sound_map(right).get("manner") == "fricative" && phonetic.sound_map(right).get("place") == "alveolar") {
 				ruleReturn = "interdental fricative to alveolar fricative";
 				this.increment_psp_count("interdental fricative to alveolar fricative", 1);
 
 			}
 			else if (phonetic.sound_map(right).get("manner") == "stop") {
 				ruleReturn = "interdental fricative to stop (non alveolar)";
 				this.increment_psp_count("interdental fricative to stop (non alveolar)", 1);
 
 			}
 
 		}
 		if (core_left.equals("ʃ")  && core_right.equals("s")) {
 			ruleReturn = "ʃ to s";
 			this.increment_psp_count("ʃ to s", 1);
 		}
 		/*if (core_left.equals("p")  && core_right.equals("b")) {
 			ruleReturn = "consonant voicing";
 		}
 		if (core_left.equals("s")  && core_right.equals("z")) {
 			ruleReturn = "consonant voicing";
 		} */
 
 		
 		
 		if (core_left.equals("h")  && phonetic.sound_map(right).get("manner") == "fricative" && phonetic.sound_map(right).get("place") == "velar") {
 			ruleReturn = "h to velar fricative";
 			this.increment_psp_count("h to velar fricative", 1);
 		}
 		else if (core_left.equals("h")  && core_right.equals("x")) {
 			ruleReturn = "h to velar fricative";
 			this.increment_psp_count("h to velar fricative", 1);
 		}
 
 		if (core_left.equals("w")){
 			if (core_right.equals("v")) {
 				ruleReturn = "w to v";
 				this.increment_psp_count("w to v", 1);
 			}
 			else if (phonetic.sound_map(right).get("manner") == "fricative") {
 				ruleReturn = "w to fricative";
 				this.increment_psp_count("w to fricative", 1);
 			}
 			else if (phonetic.sound_map(right).get("place") == "bilabial") {
 				ruleReturn = "w to bilabial";
 				this.increment_psp_count("w to bilabial", 1);
 			}
 			
 		}		
 		
 		// consonant specific rules
 		if (phonetic.sound_type(left) == 2 && phonetic.sound_type(right) == 2) {
 			if ((Double)phonetic.sound_map(left).get("voice") == 0.0 && (Double)phonetic.sound_map(right).get("voice") == 1.0) {
 				ruleReturn += " consonant voicing";
 				this.increment_psp_count("consonant voicing", 1);
 			}
 			
 			if ((Double)phonetic.sound_map(left).get("retroflex") < (Double)phonetic.sound_map(right).get("retroflex")) {
 				ruleReturn += " retroflexing";
 				this.increment_psp_count("retroflexing", 1);
 			}
 			
 			
 			if ((Double)phonetic.sound_map(left).get("voice") == 1.0 && (Double)phonetic.sound_map(right).get("voice") == 0.0) {
 				ruleReturn += " consonant devoicing";
 				this.increment_psp_count("consonant devoicing", 1);
 			}
 			
 			if (phonetic.sound_map(left).get("manner") == "stop" && phonetic.sound_map(right).get("manner") == "fricative") {
 				ruleReturn += " stop to fricative";
 				this.increment_psp_count("stop to fricative", 1);
 			}
 			
 			if (phonetic.sound_map(left).get("place") != "dental" && phonetic.sound_map(right).get("place") == "dental") {
 				ruleReturn += " dentalization";
 				this.increment_psp_count("dentalization", 1);
 			}
 			
 			if (phonetic.sound_map(left).get("place") != "palatal" && phonetic.sound_map(right).get("place") == "palatal") {
 				ruleReturn += " paltalization";
 				this.increment_psp_count("palatalization", 1);
 			}
 		}
 		
 		
 		// vowel specific rules
 		if (phonetic.sound_type(left) == 1 && phonetic.sound_type(right) == 1) {
 			if ((phonetic.sound_map(left).get("manner") == "hvowel" &&  phonetic.sound_map(right).get("manner") == "mvowel")
 				||
 				(phonetic.sound_map(left).get("manner") == "mvowel" &&  phonetic.sound_map(right).get("manner") == "lvowel")) {
 				ruleReturn = "vowel lowering";
 				this.increment_psp_count("vowel lowering", 1);
 			}
 			else if ((phonetic.sound_map(left).get("manner") == "lvowel" &&  phonetic.sound_map(right).get("manner") == "mvowel")
 					||
 					(phonetic.sound_map(left).get("manner") == "mvowel" &&  phonetic.sound_map(right).get("manner") == "hvowel")) {
 					ruleReturn = "vowel raising ";
 					this.increment_psp_count("vowel raising", 1);
 			}		
 			
 			
 			if ((Double)phonetic.sound_map(left).get("long") > (Double)phonetic.sound_map(right).get("long")) {
 				ruleReturn += "vowel shortening";
 				this.increment_psp_count("vowel shortening", 1);
 			}
 		}
 		
 		// general rules
 		if (phonetic.sound_map(left).get("manner") == "stop" && phonetic.sound_map(right).get("manner") == "fricative") {
 			ruleReturn = "stop to fricative";
 			this.increment_psp_count("stop to fricative", 1);
 		}
 		
 		
 		return ruleReturn;
 		
 	}
 	
 	public String apply_deletion_rules(String left, String right) {
 		String ruleReturn = "";
 		
 		if (left != null) {
 			String core_left = phonetic.core_sound(left);
 			if (phonetic.sound_type(left) == 1) {
 				ruleReturn = "vowel deletion";
 				this.increment_psp_count("vowel deletion", 1);
 			} 
 		
 		}
 		if (right != null) {
 			String core_right = phonetic.core_sound(right);
 			if (phonetic.sound_type(right) == 1) {
 				ruleReturn = "vowel insertion";
 				this.increment_psp_count("vowel insertion", 1);
 			}
 			
 		}
 		
 		
 		return ruleReturn;
 	}
 	
 	public String apply_word_final_rules(String left, String right) {
 		String core_left = phonetic.core_sound(left);
 		String core_right = phonetic.core_sound(right);
 		
 		String ruleReturn = "";
 		
 		// general rules
 		if (phonetic.sound_type(left) == 2 && phonetic.sound_type(right) == 2) {
 			if ((Double)phonetic.sound_map(left).get("voice") == 1.0 && (Double)phonetic.sound_map(right).get("voice") == 0.0) {
 				ruleReturn = "final obstruent devoicing";
 				this.increment_psp_count("final obstruent devoicing", 1);
 			}
 		}
 		return ruleReturn;
 		
 	}
 	
 }
