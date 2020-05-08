 import java.io.FileWriter;
 import java.io.IOException;
 
 
 public class Sentence {
 
 	public int number;
 	public String stns;
 	public StringBuffer buf;
 	public static String VP = "VP";
 	public static String NP = "NP";
 	FileWriter flog;
 	//public static VPrules VPinst = new VPrules();
 	
 	
 	Sentence(int num, String sentence) throws IOException {
 		number = num;
 		stns = sentence;
 		buf = new StringBuffer(stns); 
 		
 	}
 	
 	public void setstns(String str) {
 		stns = str;
 	}
 	
 	
 	public void rules (int left) throws IOException {
 		if (stns.charAt(left) != '(') {
 			System.out.println("Wrong reference in Sentence.rules()\n");
 			System.exit(0);
 		}
 		String label;
 		String substring;
 		int right;
 		int [] range;
 		int j;
 		
 		//System.out.println(match(left));
 		for (int i = left; i < match(left); i++) {
 			
 			//System.out.println("i "+i);
 			if (stns.charAt(i) == '(') {
 				right = match(i) + 1;
 				substring = stns.substring(i, right);
 				label = getlabel(stns.substring(i));
 				//System.out.println("label = "+label);
 				switch (label) {
 				case "VP":
 					//System.out.println("Here is a VP");
 					VPrules VPruleinst = new VPrules(number, stns.substring(i, right));
 					VPruleinst.VPrulesfunc();
 					substring = VPruleinst.stns.stns;
 					break;
 				case "NP":
 					NPrules NPruleinst = new NPrules(number, stns.substring(i, right));
 					NPruleinst.NPrulesfunc();
 					substring = NPruleinst.stns.stns;
 				default:
 					;
 				}		
 				if (i > 0) {
 					buf.replace(i, right, substring);
 					stns = buf.toString();
 				}
 					//stns = stns.substring(0, i) + substring + stns.substring(right);
 				j = 1;
 				while (true) {
 					//System.out.println("j = "+j);
 					if ((range = childrange(i, j)) != null)
 						rules(range[0]);
 					else
 						break;
 					++j;					
 				}
 				//System.out.println("get to an element");
 				i = match(i);
 				//System.out.println("i = "+i);
 			}
 		}
 		
 		
 	}
 	
 	public String getlabel (String str) {
 		int right = str.indexOf(" ");
 		//System.out.println(right);
 		return str.substring(1, right);
 	}
 
 	
 	public String childlabel(int left, int num) {
 		if (stns.charAt(left) != '(') {
 			System.out.println("Wrong reference in Sentence.childlabel()\n");
 			System.exit(0);
 		}
 		int right = match(left);
 		for(int i = left+1; i < right; i++) {
 			if (stns.charAt(i) == '(') {
 				num--;
 				if (num == 0) {
 					return getlabel(stns.substring(i));
 				}
 				i = match(i);
 			}
 		}
 		return null;
 	}
 	
 	public String child(int left, int num) {
 	//	System.out.println("This is child(). left = "+left);
 		if (stns.charAt(left) != '(') {
 			System.out.println("Wrong reference in Sentence.child()\n");
 			System.exit(0);
 		}
 		int right = match(left);
 		for(int i = left+1; i < right; i++) {
 			if (stns.charAt(i) == '(') {
 				num--;
 				if (num == 0) {
 					return stns.substring(i, match(i));
 				}
 				i = match(i);
 			}
 		}
 		return null;
 	}
 
 	public int [] childrange(int left, int num) {
 		if (stns.charAt(left) != '(') {
 			System.out.println("Wrong reference in Sentence.childrange()\n");
 			System.exit(0);
 		}
 		//System.out.println("This is childrange(). left = "+left);
 		int right = match(left);
 		for(int i = left+1; i < right; i++) {
 			//System.out.println("This is loop in childrange(). i = "+i);
 			if (stns.charAt(i) == '(') {
 				num--;
 				if (num == 0) {
 					//System.out.println("This is child(). i = "+i+" match(i) = "+match(i));
 					return new int[]{i, match(i)};
 				}
 				i = match(i);
 			}
 		}
 		return null;
 	}
 	
 	public int match (int left) {
 		if (stns.charAt(left) != '(') {
 			System.out.println("Wrong reference in Sentence.match()\n");
 			System.exit(0);
 		}
 		int len = stns.length();
 		int label = 0;
 		//System.out.println(stns.charAt(left));
 		for(int i = left; i < len; i++) {
 			if(stns.charAt(i) == '('){
 				label ++;
 			}
 			if(stns.charAt(i) == ')') {
 				label --;
 			}
 			if(label == 0) {
 				//System.out.println(stns.substring(left, i+1));
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	public boolean isElement(int left) {
 		if (stns.charAt(left) != '(') {
 			System.out.println("Wrong reference in Sentence.isElement()\n");
 			//System.out.println(stns);
 			System.exit(0);
 		}
 		int right = match(left);
 		int ext = stns.indexOf("(", left + 1);
 		if (ext < right && ext > 0) {
 			//System.out.println(ext + " < " + right);
 			return false;
 		}
 		return true;
 		
 	/*	int firstspace = stns.indexOf(" ", left);
 		int lastspace = stns.lastIndexOf(" ", right);
 		if(firstspace == lastspace && firstspace > 0) {
 			System.out.println(firstspace);
 			return true;
 		}*/
 		//return false;
 	}
 	
 	public int[] word(int left) {
 		if (stns.charAt(left) != '(') {
 			System.out.println("Wrong reference in Sentence.word()\n");
 			System.exit(0);
 		}
 		int right = match(left);
 		if (!isElement(left)) {
 			//System.out.println("No word added here");
 			return null;
 		}
 		int space = stns.indexOf(" ", left);		
 		return new int[]{space + 1, right};		
 	}
 	
 	public void swap(int a, int b) throws IOException {
 		if (a < 0 || b < 0 ) {
 			System.out.println("Wrong reference in Sentence.swap()\n");
 			//System.out.println(stns);
 			System.exit(0);
 		}
 		int [] range1 = childrange(0, a);
 		int [] range2 = childrange(0, b);
		String substring1 = stns.substring(range1[0],range1[1]+1);
		String substring2 = stns.substring(range2[0],range2[1]+1);
		flog = new FileWriter("change.log", true);
 		flog.write("Swap:\n" + stns.substring(range1[0], range1[1]+1) + "\nAnd:\n" + stns.substring(range2[0], range2[1]+1) + "\n\n");
		flog.close();
 		stns = stns.substring(0, range1[0]) + substring2 + stns.substring(range1[1], range2[0]) + substring1 + stns.substring(range2[1]);		
 	}
 	
 	public int exist(String label) {
 		int j = 1;
 		while (true) {
 			if (childrange(0, j) != null) {
 				if (childlabel(0, j).equals(label))
 					return j;
 			}
 			else
 				return -1;
 			++j;
 		}
 	}
 	
 	public int last(String label) {
 		int j = 1;
 		int i = -1;
 		while (true) {
 			if (childrange(0, j) != null) {
 				if (childlabel(0, j).equals(label))
 					i = j;
 			}
 			else
 				return i;
 			++j;
 		}
 	}
 }
