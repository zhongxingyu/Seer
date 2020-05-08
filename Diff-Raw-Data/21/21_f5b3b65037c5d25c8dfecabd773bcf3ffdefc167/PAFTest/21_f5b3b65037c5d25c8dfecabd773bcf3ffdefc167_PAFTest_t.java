 import java.util.*;
 import java.io.*;
 
 public class PAFTest {
     public static void main (String[] args) throws IOException {
 	Properties props = new Properties ();
 	if (args.length > 0) {
 	    props.load (new FileInputStream (args[0]));
 	} else {
 	    props.load (new FileInputStream ("paftest.txt"));
 	}
 	new File(props.getProperty("csvdir")).mkdirs ();
 	new File(props.getProperty("dotdir")).mkdirs ();
 
	String mode = props.getProperty("mode").trim();
 	if (mode.equals ("compare")) {
 	    runCompare (props);
 	} else if (mode.equals ("describe")) {
 	    runDescribe (props);
	} else if (mode.equals ("generate")) {
	    Generator.generateInstance (props);
 	}
 
 	//optTestArgsSameError ("Complete", "complete_same_error_01.csv", 1.96, 0.01, new CompleteSemantics(), new CompleteGivenConflictFreeSemantics());
 	// optTestArgsSameError ("Complete", "complete_same_error_005.csv", 1.96, 0.005, new CompleteSemantics(), new CompleteGivenConflictFreeSemantics());
 	//optTestArgsSameError ("Grounded", "grounded_same_error_01.csv", 1.96, 0.01, new GroundedSemantics(), new GroundedGivenConflictFreeSemantics());
 	//optTestArgsSameError ("Grounded", "grounded_same_error_005.csv", 1.96, 0.005, new GroundedSemantics(), new GroundedGivenConflictFreeSemantics());
 
 	//testAdmissible ();
 	//profOptimization ();
 	//paperExamples ();
 	//profExamples ();
 
         //testGenerator ();
         //testGraph ();
 	//describeInstances ();
     }
 
     static void runCompare (Properties props) throws IOException {
 	String compare = props.getProperty ("compare");
 	for (String type: compare.split(" ")) {
 	    if (type.trim().length() > 0) {
 		runSingleCompare (props, type);
 	    }
 	}
     }
 
     static void runSingleCompare (Properties props, String type) throws IOException {
 	String prefix = "compare."+type+".";
 	String csvdir = props.getProperty ("csvdir");
 	String inputdir = props.getProperty ("inputdir");
 
 	Semantics first = Semantics.fromName (props.getProperty (prefix+"first"));
 	Semantics second = Semantics.fromName (props.getProperty (prefix+"second"));
	Semantics exact = Semantics.fromName (props.getProperty (prefix+"exact"));
 	Montecarlo m = Montecarlo.fromString (props.getProperty (prefix+"mode"));
	Montecarlo mex = null;
 
 	PrintWriter pw = new PrintWriter (new FileOutputStream (csvdir+"/"+type+".csv"), true);
 	if (exact != null) {
 	    pw.println ("Args,Exact,"+Stats.headerString("Naive ")+","+Stats.headerString("Optim "));
	    mex = Montecarlo.fromString (props.getProperty (prefix+"exactmode"));
 	} else {
 	    pw.println ("Args,"+Stats.headerString("Naive ")+","+Stats.headerString("Optim "));
 	}
 	for (int args=10; args <= 20; args++) {
 	    Instance instance = new Instance (inputdir+"/instance"+args+".txt");
 	    PAF paf = instance.paf;
 	    ArgSet set = instance.set;
 	    // some debug
 	    System.out.println ("Args "+args);
 	    System.out.println("Nodes: "+paf.args.size()+" Defeats: "+paf.defeatsList.size());
 	    System.out.println("Search space: "+(Math.pow(2, paf.args.size())*Math.pow(2, paf.defeatsList.size())));
 	    System.out.println("Set: "+set);
 	    System.out.println("First conditional: "+first.conditional (paf, set));
 	    System.out.println("Second conditional: "+second.conditional (paf, set));
 
 	    Stats s1 = m.runs (first, paf, set, 20);
 	    Stats s2 = m.runs (second, paf, set, 20);
 	    double exactValue = 0;
 	    if (exact != null) {
 		exactValue = exact.conditional(paf, set)*mex.run (exact, paf, set).toDouble();
 	    }
 	    if (exact != null) {
 		pw.println (args+","+exactValue+","+s1.toString()+","+s2.toString());
 	    } else {
 		pw.println (args+","+s1.toString()+","+s2.toString());
 	    }
 	}
 	pw.close ();
     }
 
     static void runDescribe (Properties props) throws IOException {
 	String inputdir = props.getProperty ("inputdir");
 	String csvdir = props.getProperty ("csvdir");
 	String dotdir = props.getProperty ("dotdir");
 	int from = Integer.parseInt (props.getProperty ("describe.from"));
 	int to = Integer.parseInt (props.getProperty ("describe.to"));
 
 	PrintWriter pw = new PrintWriter (new FileOutputStream ("instance_descriptions.csv"), true);
 	pw.println ("Args,Defeats,Set size,CF,Admissible,Complete,Preferred,Grounded");
 	for (int i=from; i <= to; i++) {
 	    System.out.println ("Instance: "+i);
 	    Instance pair = new Instance (inputdir+"/instance"+i+".txt");
 	    PAF paf = pair.paf;
 	    ArgSet set = pair.set;
 	    pair.writeDot (dotdir+"/instance"+i+".dot");
 
 	    Montecarlo m = new MontecarloError (1.96, 0.005);
 	    double cf = paf.conflictFree(set);
 	    double admissible = paf.admissible(set);
 	    double complete = cf*m.run (new CompleteGivenConflictFreeSemantics(), paf, set).toDouble();
 	    double preferred = cf*m.run (new PreferredGivenConflictFreeSemantics(), paf, set).toDouble();
 	    double grounded = cf*m.run (new GroundedGivenConflictFreeSemantics(), paf, set).toDouble();
 	    pw.println(i+","+paf.defeatsList.size()+","+set.size()+","+cf+","+admissible+","+complete+","+preferred+","+grounded);
 	}
 	pw.close ();
     }
 
     static void testAdmissible () {
 	PAF paf = new PAF();
 	paf.addArgs ("a", 1.0,
 		     "b", 2/3.0,
 		     "c", 2/3.0,
 		     "d", 1.0);
 	paf.addDefeats ("a", "b", 2/3.0,
 			"b", "c", 2/3.0);
 	ArgSet s = new ArgSet ("a", "c", "d");
 	Montecarlo m = new MontecarloError (1.96, 0.005);
 	Semantics compl = new CompleteSemantics();
 	Semantics complGA = new CompleteGivenAdmissibleSemantics();
 	System.out.println("exact cf: "+paf.conflictFree (s));
 	System.out.println("exact compl: "+paf.depthFirst (compl, s));
 	System.out.println("exact adm: "+paf.depthFirst (new AdmissibleSemantics(), s));
 	System.out.println("exact compl|A: "+paf.depthFirst (complGA, s));
 	System.out.println("mc compl: "+m.run(compl, paf, s).toDouble());
 	double p = m.run(complGA, paf, s).toDouble();
 	System.out.println("mc compl|A: "+p);
 	System.out.println("mc compl|A*A: "+complGA.conditional(paf, s)*p);
     }
 
     /*
     static void profOptimization () {
 	PAF paf = new PAF();
 	paf.addArgs ("a", 1.0,
 		     "b", 2/3.0,
 		     "c", 2/3.0,
 		     "d", 1.0);
 	paf.addDefeats ("a", "b", 2/3.0,
 			"b", "c", 2/3.0);
 	ArgSet s = new ArgSet ("a", "c", "d");
         //compareOptimization (paf, s);
     }
 
     public static void profExamples () {
 	PAF paf = new PAF();
 	paf.addArgs ("a", 1.0,
 		     "b", 2/3.0,
 		     "c", 2/3.0,
 		     "d", 1.0);
 	paf.addDefeats ("a", "b", 2/3.0,
 			"b", "c", 2/3.0);
 	ArgSet s = new ArgSet ("c");
 	System.out.println (paf.montecarlo (new AdmissibleSemantics(), s, 1.96, 0.01));
 	System.out.println (paf.depthFirst (new AdmissibleSemantics(), s)); // 0.37
         System.out.println (paf.admissible (s));
         System.out.println ();
 
 	s = new ArgSet ("c", "d");
 	System.out.println (paf.montecarlo (new AdmissibleSemantics(), s, 1.96, 0.01));
 	System.out.println (paf.depthFirst (new AdmissibleSemantics(), s)); // 0.37
         System.out.println (paf.admissible (s));
         System.out.println ();
 
 	s = new ArgSet ("a", "c", "d");
 	System.out.println (paf.montecarlo (new AdmissibleSemantics(), s, 1.96, 0.01));
 	System.out.println (paf.depthFirst (new AdmissibleSemantics(), s)); // 0.5679
         System.out.println (paf.admissible (s));
         System.out.println ();
 
 	DAF daf = new DAF();
 	daf.addArgs ("a", "b");
 	daf.addDefeats ("a", "b",
 			"b", "a");
 	System.out.println (daf.complete (new ArgSet())+" "+daf.preferred (new ArgSet ()));
 	System.out.println (daf.complete (new ArgSet("a"))+" "+daf.preferred (new ArgSet ("a")));
 	System.out.println (daf.complete (new ArgSet("a", "b"))+" "+daf.preferred (new ArgSet ("a", "b")));
     }
 
     public static void paperExamples () {
 	PAF paf = new PAF();
 	paf.addArgs ("a", 1.0,
 		     "b", 1.0,
 		     "c", 0.7,
 		     "d", 0.3);
 	paf.addDefeats ("a", "c", 1.0,
 			"d", "a", 1.0);
 	ArgSet s = new ArgSet ("a", "b");
 	System.out.println (paf.depthFirst (new CompleteSemantics(), s)); // 0.7
 	System.out.println (paf.depthFirst (new GroundedSemantics(), s)); // 0.7
     }
     */
 }
