 import java.util.*;
 import java.io.*;
 
 class Distribution extends ArrayList<Distribution.Entry> {
     class Entry {
         double value;
         double prob;
 
         public Entry (double value, double prob) {
             this.value = value;
             this.prob = prob;
         }
     }
 
     public void add (double value, double prob) {
         add (new Entry (value, prob));
     }
 
     public double sample () {
         double r = Math.random();
         double c = 0;
         for (Entry e: this) {
             c += e.prob;
             if (r <= c) {
                 return e.value;
             }
         }
         throw new RuntimeException ("Wrong distribution");
     }
 }
 
 class Generator {
     int nargs;
     Distribution argProb;
     Distribution outdegProb;
     Distribution defProb;
 
     public Generator (int nargs, Distribution argProb, Distribution outdegProb, Distribution defProb) {
         this.nargs = nargs;
         this.argProb = argProb;
         this.outdegProb = outdegProb;
         this.defProb = defProb;
     }
 
     public PAF generateTree () {
         PAF paf = new PAF();
         // gen args
         int argsGenerated = 0;
         argsGenerated:
         for (char a='a'; a <= 'z'; a++) {
             paf.addArg(""+a, argProb.sample ());
             argsGenerated++;
             if (argsGenerated == nargs) {
                 break argsGenerated;
             }
         }
         if (argsGenerated < nargs) {
             throw new RuntimeException ("argsGenerated < nargs");
         }
         // gen defeats
         for (String a: paf.argsList) {
             int numdef = (int)outdegProb.sample ();
             for (int i=0; i < numdef; i++) {
                 String b;
                 do {
                     int j = (int) (Math.random()*nargs);
                     b = paf.argsList.get(j);
                 } while (paf.getDefeats(a).get(b) != null); // FIXME
 		paf.addDefeat (a, b, defProb.sample ());
             }
         }
         return paf;
     }
 
     public static ArgSet findPossibleSet (Semantics semantic, PAF paf) {
 	PrintWriter pw = null;
 	try {
 	    pw = new PrintWriter (new FileOutputStream ("genset.mod"), true);
 	} catch (IOException e) {
             e.printStackTrace ();
 	}
 	pw.print ("set N := {");
 	for (int i=0; i < paf.argsList.size(); i++) {
 	    pw.print ("'"+paf.argsList.get(i)+"'");
 	    if (i < paf.argsList.size()-1) {
 		pw.print (", ");
 	    }
 	}
 	pw.print ("};\n");
 	pw.print ("var x{N} binary;\n"+
 		  "var s{N} binary;\n"+
 		  "param p{N} > 0, <= 1;\n"+
 		  "param def{N, N} >= 0, < 1;\n"+
 		  "maximize cost: sum{i in N} s[i]*p[i] + sum{i in N, j in N} -3*s[i]*def[j,i]*p[j] + sum{i in N} -(1-s[i])*p[i];\n"+
 		  "certain{i in N: p[i]=1.0}: s[i] = 1;\n"+
 		  "solve;\n"+
 		  "printf{i in N: s[i]}: '%s ', i;\n"+
 		  "printf '\\n';\n"+
 		  "data;\n");
 	pw.print ("param p := ");
 	for (int i=0; i < paf.argsList.size(); i++) {
 	    String a = paf.argsList.get(i);
 	    pw.print ("["+a+"] "+paf.args.get(a));
 	    if (i < paf.argsList.size()-1) {
 		pw.print (", ");
 	    }
 	}
 	pw.print (";\n");
 	pw.print ("param def : ");
 	for (String a: paf.argsList) {
 	    pw.print ("'"+a+"' ");
 	}
 	pw.print (":=\n");
 	for (String a: paf.argsList) {
 	    pw.print ("'"+a+"' ");
 	    for (String b: paf.argsList) {
 		double p = paf.getDefeats(a).get(b) != null ? paf.getDefeats(a).get(b) : 0;
 		pw.print (p+" ");
 	    }
 	    pw.print ("\n");
 	}
 	pw.print (";");
 	pw.close ();
 	try {
 	    Runtime.getRuntime().exec ("glpsol --model genset.mod --display out").waitFor ();
 	    BufferedReader r = new BufferedReader (new FileReader ("out"));
 	    String line = r.readLine ();
 	    ArgSet s = new ArgSet();
 	    for (String a: line.split(" ")) {
 		s.add (a.trim());
 	    }
 	    ArrayList<ArgSet> l = new ArrayList<ArgSet> ();
 	    l.add (s);
 	    return hillClimbing (semantic, paf, l);
 	} catch (Exception e) {
 	    e.printStackTrace ();
 	}
 	return new ArgSet();
     }
 
     static ArgSet hillClimbing (Semantics semantic, PAF paf, Collection<ArgSet> cfsets) {
 	ArgSet best = new ArgSet();
 	double pbest = 0;
         Montecarlo m = new MontecarloIter (3000);
 	for (ArgSet s: cfsets) {
 	    System.out.println ("Hill from "+s);
 	    ArgSet cur = s;
 	    double ccur = semantic.conditional(paf, cur);
 	    double pcur = ccur*m.run(semantic, paf, cur).toDouble();
 	    while (true) {
 		ArgSet next = null;
 		double cnext = ccur;
 		double pnext = pcur;
 		for (String a: paf.argsList) {
 		    if (!cur.contains (a)) {
 			cur.add (a);
 			double c = semantic.conditional(paf, cur);
 			if ((cnext < 0.6 && c > cnext) || (c >= 0.6 && c < 1)) {
 			    double p = c*m.run(semantic, paf, cur).toDouble();
 			    if (cnext < 0.6 || p > pnext) {
 				next = new ArgSet (cur);
 				cnext = c;
 				pnext = p;
 			    }
 			}
 			cur.remove (a);
 		    } else if (paf.args.get(a) < 1) {
 			// remove
 			cur.remove (a);
 			double c = semantic.conditional(paf, cur);
 			if (cnext < 0.6 || pnext == 0) {
 			    double p = c*m.run(semantic, paf, cur).toDouble();
 			    if (p > pnext) {
 				next = new ArgSet (cur);
 				cnext = c;
 				pnext = p;
 			    }
 			}
 			// substitute
 			for (String b: paf.argsList) {
 			    if (!cur.contains (b)) {
 				cur.add (b);
 				c = semantic.conditional(paf, cur);
 				if ((cnext < 0.6 && c > cnext) || (c >= 0.6 && c < 1)) {
 				    double p = c*m.run(semantic, paf, cur).toDouble();
 				    if (cnext < 0.6 || p > pnext) {
 					next = new ArgSet (cur);
 					cnext = c;
 					pnext = p;
 				    }
 				}
 				cur.remove (b);
 			    }
 			}
 			cur.add (a);
 		    }
 		}
 		if (next == null) {
 		    break;
 		}
 		cur = next;
 		ccur = cnext;
 		pcur = pnext;
 		System.out.println (cur+" "+ccur+" "+pcur);
 	    }
 	    if (pcur > pbest) {
 		best = cur;
 		pbest = pcur;
 	    }
 	}
         return best;
     }
 
     static Distribution readDistribution (Properties props, String key) {
 	String dstring = props.getProperty (key);
 	Distribution d = new Distribution ();
 	for (String entry: dstring.split(",")) {
 	    String[] kv = entry.split(":");
 	    if (kv.length == 2) {
 		d.add (Double.parseDouble (kv[0].trim()), Double.parseDouble (kv[1].trim()));
 	    }
 	}
 	return d;
     }
 
    static void generateInstance20 (Properties props) throws IOException {
 	Distribution argProb = readDistribution (props, "generate.argument_probability");
 	Distribution outdegProb = readDistribution (props, "generate.argument_outdegree");
 	Distribution defProb = readDistribution (props, "generate.defeat_probability");
 	String outfile = props.getProperty ("generate.output");
 	int iterations = Integer.parseInt (props.getProperty ("generate.iterations"));
 	int nargs = Integer.parseInt (props.getProperty ("generate.arguments"));
 	Semantics sem = Semantics.fromName (props.getProperty ("generate.maximize"));
 
         PAF best = null;
         ArgSet sbest = null;
         double cfbest = 0;
         double sembest = 0;
         Montecarlo m = new MontecarloError (1.96, 0.0005);
         for (int i=0; i < 1000; i++) {
             Generator g = new Generator (nargs, argProb, outdegProb, defProb);
             PAF paf = g.generateTree ();
             System.out.println("Structures: "+paf.args.size()+" "+paf.defeatsList.size());
             System.out.println("Search space: "+(Math.pow(2, paf.args.size())*Math.pow(2, paf.defeatsList.size())));
             ArgSet set = findPossibleSet (sem, paf);
             System.out.println(set);
             double cf = sem.conditional (paf, set);
             System.out.println("Conflict free: "+cf);
             if (cf > 0.6 && cf < 1) {
                 double c = cf*m.run (sem, paf, set).toDouble();
                 System.out.println ("P(semantic): "+c);
                 if (c > sembest) {
                     best = paf;
                     sbest = set;
                     cfbest = cf;
                     sembest = c;
                     new Instance(paf, set).writeInstance (outfile);
                 }
             }
         }
     }
 }
