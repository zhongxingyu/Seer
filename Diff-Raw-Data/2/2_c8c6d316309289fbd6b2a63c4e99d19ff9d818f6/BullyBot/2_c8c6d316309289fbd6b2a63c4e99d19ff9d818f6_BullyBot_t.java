 import java.util.*;
 /* A bit smarter kind of bot, who searches for its strongest planet and then attacks the weakest planet.
  The score is computed based on the number of ships and the inverse of the growth rate.
  */
 
 
public class BullyBot {
     public static void DoTurn(PlanetWars pw) {
 
 	// (1) Find my strongest planet.
 	Planet source = null;
 	double sourceScore = Double.MIN_VALUE;
 	for (Planet p : pw.MyPlanets()) {
 	    double score = (double)p.NumShips() / (1 + p.GrowthRate());
 	    if (score > sourceScore) {
 		sourceScore = score;
 		source = p;
 	    }
 	}
 	// (2) Find the weakest enemy or neutral planet.
 	Planet dest = null;
 	double destScore = Double.MIN_VALUE;
 	for (Planet p : pw.NotMyPlanets()) {
 	    double score = (double)(1 + p.GrowthRate()) / p.NumShips();
 	    if (score > destScore) {
 		destScore = score;
 		dest = p;
 	    }
 	}
 	// (3) Attack!
 	if (source != null && dest != null) {
 	    pw.IssueOrder(source, dest);
 	}
     }
 
     public static void main(String[] args) {
 	String line = "";
 	String message = "";
 	int c;
 	try {
 	    while ((c = System.in.read()) >= 0) {
 		switch (c) {
 		case '\n':
 		    if (line.equals("go")) {
 			PlanetWars pw = new PlanetWars(message);
 			DoTurn(pw);
 		        pw.FinishTurn();
 			message = "";
 		    } else {
 			message += line + "\n";
 		    }
 		    line = "";
 		    break;
 		default:
 		    line += (char)c;
 		    break;
 		}
 	    }
 	} catch (Exception e) {
 	    // Owned.
 	}
     }
 }
 
