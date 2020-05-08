 
 /** 
  * 
  * Algorithm that finds how many nodes we are away
  * This is very bad. Need HUGE code refactoring
  * This is to be done again in Lisp
  * 
  * @author Jon Vlachoyiannis (jon@emotionull.com)
  * Under GPL
  * 01/03/2009
  */
 
 package Algorithm;
 import java.util.*;
 
 public class Algorithm {
    public final String nodes_green[] = {"Πειραιάς", "Φάληρο", "Μόσχατο", "Ταύρος", "Πετράλωνα", "Θησείο", "Μοναστηράκι", "Ομόνοια", "Βικτώρια", "Αττική", "Άγιος Νικόλαος", "Κάτω Πατήσια", "Άγιος Ελευθέριος", "Άνω Πατήσια", "Περισσός", "Πευκάκια", "Νέα Ιωνία", "Ηράκλειο", "Ειρήνη", "Νερατζιώτισσα", "Μαρούσι", "ΚΑΤ", "Κηφισιά"};
     
 	public final String nodes_blue[] = {"Αιγάλεω", "Ελαιώνας", "Κεραμαϊκός", "Μοναστηράκι", "Σύνταγμα", "Ευαγγελισμός", "Μέγαρο Μουσικής", "Αμπελόκηποι", "Πανόρμου", "Κατεχάκη", "Εθνική Άμυνα", "Χαλάνδρι", "Δουκίσσης Πλακεντιάς", "Παλλήνη", "Παιανία-Κάντζα", "Κορωπί", "Αεροδρόμιο"};
     
	public final String nodes_red[] = {"Άγιος Δημήτριος", "Δάφνη", "Άγιος Ιωάννης", "Νέος Κόσμος", "Συγγρού-Φιξ", "Ακρόπολη", "Σύνταγμα", "Πανεπιστήμιο", "Ομόνοια", "Μεταξουργείο", "Σταθμός Λαρίσης", "Αττική", "Σεπόλια", "Άγιος Αντώνιος"};
 
 
 	private Vector results = new Vector();
 
 	public Algorithm() {
 
 	}
 
 	public Vector calc(int src, int dest) {
 		int node_start = 0;
 		int node_color_start = 0;
 		int node_end = 0;
 		int node_color_end = 0;
 
 		int n = 0;
 		int n1 = 0;
 		int n2 = 0;
 
 		final int nd_intsec_green_blue = 6;
 		final int nd_intsec_blue_green = 3;
 		final int nd_intsec_red_blue = 6;
 		final int nd_intsec_blue_red = 4;
		final int nd_intsec_red_green = 11;
		final int nd_intsec_green_red = 9;
 
 		results.removeAllElements();
 
 		if (src < nodes_green.length -1){
 			node_color_start = 0;
 			node_start = src;
 		}
 		
 		else if (src < nodes_green.length -1 + nodes_blue.length -1 ) {
 			node_color_start = 1;
 			node_start = src - nodes_green.length;
 		}
 		
 		else {
 			node_color_start = 2;
 			node_start = src - nodes_green.length - nodes_blue.length;
 
 		}
 
 		if (dest < nodes_green.length -1){
 			node_color_end = 0;
 			node_end = dest;
 		}
 		
 		else if (dest < nodes_green.length -1 + nodes_blue.length -1 ) {
 			node_color_end = 1;
 			node_end = dest - nodes_green.length;
 		}
 		
 		else {
 			node_color_end = 2;
 			node_end = dest - nodes_green.length - nodes_blue.length;
 
 		}
 
 		//System.out.println("DEBUG: " + dest);
 		//System.out.println("DEBUG: " + nodes_green.length);
 		//System.out.println("DEBUG: " + nodes_blue.length);
 
 		//System.out.println("DEBUG: " + node_color_start);
 		//System.out.println("DEBUG: " + node_color_end);
 	
 		// Algorithm
 		if (node_color_start == node_color_end) {
 			n = calcTotalNodes(node_start, node_end, node_color_start);
 		}
 
 		// Green / Blue
 		else if (node_color_start == 0 && node_color_end == 1) {
 			n1 = calcTotalNodes(node_start, nd_intsec_green_blue, 0);
 			n2 = calcTotalNodes(nd_intsec_blue_green, node_end, 1);
 
 			n = math_abs(n1) + math_abs(n2);
 		}
 
 		// Blue / Green
 		else if (node_color_start == 1 && node_color_end == 0) {
 			n1 = calcTotalNodes(node_start, nd_intsec_blue_green, 1);
 			n2 = calcTotalNodes(nd_intsec_green_blue, node_end, 0);
 			n = math_abs(n1) + math_abs(n2);
 		}
 
 		// Green / Red
 		else if (node_color_start == 0 && node_color_end == 2) {
 			n1 = calcTotalNodes(node_start, nd_intsec_green_red, 0);
 			n2 = calcTotalNodes(nd_intsec_red_green, node_end, 2);
 			n = math_abs(n1) + math_abs(n2);
 		}
 
 		// Red / Green
 		else if (node_color_start == 2 && node_color_end == 0) {
 			n1 = calcTotalNodes(node_start, nd_intsec_red_green, 2);
 			n2 = calcTotalNodes(nd_intsec_green_red, node_end, 0);
 			n = math_abs(n1) + math_abs(n2);
 		}
 
 
 		// Blue / Red
 		else if (node_color_start == 1 && node_color_end == 2) {
 			n1 = calcTotalNodes(node_start, nd_intsec_blue_red, 1);
 			n2 = calcTotalNodes(nd_intsec_red_blue, node_end, 2);
 			n = math_abs(n1) + math_abs(n2);
 		}
 
 
 		// Red / Blue
 		else if (node_color_start == 2 && node_color_end == 1) {
 			n1 = calcTotalNodes(node_start, nd_intsec_red_blue, 2);
 			n2 = calcTotalNodes(nd_intsec_blue_red, node_end, 1);
 			n = math_abs(n1) + math_abs(n2);
 		}
 
 
 		// I miss Python's multiple returning system :(
 		// and I refuse to create a struct :D
 
 		if (n1 != 0)
 			results.insertElementAt("Άλλαξε γραμμή μετά από " + String.valueOf(math_abs(n1)) + " στάσεις", 0);
 		else 
 			results.insertElementAt("Δεν χρειάζεται να αλλάξεις γραμμή", 0);
 
 		results.insertElementAt("Σύνολο στάσεων: " + String.valueOf(math_abs(n)),1);
 		results.insertElementAt("Μέσος χρόνος: x λεπτά", 2);
 
 		return results;
 	}
 
 		
 	// this is hideous...
 	private int calcTotalNodes(int nd_st, int nd_en, int line) {
 		String line_color = "ΗΣΑΠ";
 		String[] nodes = nodes_green;
 
 		// lets find the line's color
 		if(line == 0) {
 			line_color = "ΗΣΑΠ";
 			nodes = nodes_green;
 		}
 		else if(line == 1) {
 			line_color = "Μετρο[Μπλε]"; 
 			nodes = nodes_blue;
 
 		}
 		else if(line == 2) {
 			line_color = "Μετρο[Κόκκινο]";
 			nodes = nodes_red;
 		}
 
 		results.addElement("---------------------");
 
 		// lets find the direction
 		if (nd_st < nd_en) {
 			results.addElement("Γραμμή " + line_color + " (προς " +  nodes[nodes.length-1] + " )");
  			results.addElement("---------------------");
 
 			for (int i=nd_st; i < nd_en+1; i++)
 				results.addElement(" + " + nodes[i]);				
 		}
 		
 		else { 
 			results.addElement("Γραμμή " + line_color + " (προς " +  nodes[0] + " )");
 			results.addElement("---------------------");
 
 			for (int i=nd_st; i >= nd_en; i--)
 				results.addElement(" + " + nodes[i]);				
 		}
 
 		return nd_en - nd_st;
 
 	}
 
 	// So we don't need CLDC 1.1
 	private int math_abs(int num) {
 		if (num >= 0)
 			return num;
 		else
 			return num*(-1);
 	}
 
 }
