 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Collections;
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Iterator;
 import java.util.Map;
 import java.io.InputStream;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.text.NumberFormat;
 import java.math.RoundingMode;
 
 class Pipair {
 
     int tSupport = 3;
     float tConfidence = 0.65f;
 
     class Pair {
         private String _aName;
         private String _bName;
         private int _support = 0;
         private float _aWithoutB = 0;
 
         public Pair(String aName, String bName) {
             _aName = aName;
             _bName = bName;
         }
 
         public String getSource() {
             return _aName;
         }
 
         public String getTarget() {
             return _bName;
         }
 
         public void strengthen() {
             _support++;
         }
 
         public void weaken() {
             _aWithoutB++;
         }
 
         public int getSupport() {
             return _support;
         }
 
         public float getConfidence() {
             return (float)_support / _aWithoutB;
         }
 
         public String toString() {
             NumberFormat numf = NumberFormat.getNumberInstance();
             numf.setMaximumFractionDigits(2);
             numf.setRoundingMode(RoundingMode.HALF_EVEN);
 
             return "pair: (" +
                 getSource() + " " + getTarget() + "), support: " +
                 getSupport() + ", confidence: " +
                 numf.format(getConfidence() * 100.0) + "%";
         }
     }
 
     class Violation {
         private String _caller;
         private Pair _pair;
 
         public Violation(String caller, Pair pair) {
             _caller = caller;
             _pair = pair;
         }
 
         public String toString() {
             return "bug: " + _pair.getSource() + " in " + _caller + ", " +
                 _pair.toString();
         }
     }
 
     public Hashtable<String,ArrayList<String>> parseFile(String fileName) {
         Runtime rt = Runtime.getRuntime();
         Hashtable<String, ArrayList<String>> table = new Hashtable<String,ArrayList<String>>();
         try {
            Process pr = rt.exec("opt -print-callgraph -disable-output " + fileName);
             InputStream st = pr.getErrorStream();
             BufferedReader in = new BufferedReader(new InputStreamReader(st));
             String line = null;
 
 
             int state = 0; //0 - Empty Line, 1 - Call graph
             String current = null;
             while ((line = in.readLine()) != null) { 
 
               //System.out.println(line + " " + line.length());
 
               switch (state) {
                 case(1):
                   if (line.matches("(.*)CS<0x[0-9a-f]*> calls function(.*)")) {
                     String[] slist = line.split("\'");
                     String func = slist[1];
                     ArrayList<String> curList = table.get(current);
                     curList.add(func);
                     //System.out.println(func);
                     break;
                   }
                 case(0):
                   if (line.startsWith("Call graph node for function")) {
                     
                     String[] slist = line.split("\'");
                     current = slist[1];
                     ArrayList<String> nlist = new ArrayList<String>();
                     table.put(current,nlist);
                     state = 1;
                     //System.out.println(current);
                     break;
                   }
                 default:
                   if (line.length() == 0) { 
                     state = 0;
                     //System.out.println("");
                   }
                   break;
               }
 
             }
 
         } catch (IOException e) {
         }
         return table;
     }
 
     public Hashtable<String,Hashtable<String,Pair>>
         getInvariantPairs(Hashtable<String,ArrayList<String>> cg) {
 
         Hashtable<String,Hashtable<String,Pair>> pairs = getAllInvariantPairs(cg);
         rejectWeakPairs(pairs);
         return pairs;
     }
 
     private Hashtable<String,Hashtable<String,Pair>> getAllInvariantPairs(Hashtable<String,ArrayList<String>> cg) {
         Hashtable<String,Hashtable<String,Pair>> pairs =
             new Hashtable<String,Hashtable<String,Pair>>();
 
         Enumeration funcs = cg.elements();
         while (funcs.hasMoreElements()) {
             ArrayList<String> calls = (ArrayList<String>)funcs.nextElement();
             calls = removeDuplicateCalls(calls);
 
             for (int i = 0; i < calls.size(); i++) {
                 for (int j = i + 1; j < calls.size(); j++) {
                     createOrStrengthenPair(pairs, calls.get(i), calls.get(j));
                     createOrStrengthenPair(pairs, calls.get(j), calls.get(i));
                 }
 
                 Hashtable<String,Pair> existingPairs = pairs.get(calls.get(i));
                 if (existingPairs != null) {
                     Enumeration e = existingPairs.elements();
                     while (e.hasMoreElements()) {
                         Pair p = (Pair)e.nextElement();
                         p.weaken();
                     }
                 }
             }
         }
 
         return pairs;
     }
 
     private void
         rejectWeakPairs(Hashtable<String,Hashtable<String,Pair>> pairs) {
 
         Enumeration<Hashtable<String,Pair>> pairLists = pairs.elements();
         while (pairLists.hasMoreElements()) {
             Hashtable<String,Pair> pairList = (Hashtable<String,Pair>)pairLists.nextElement();
 
             Enumeration<String> bNames = pairList.keys();
             while (bNames.hasMoreElements()) {
                 String bName = bNames.nextElement();
                 Pair p = (Pair)pairList.get(bName);
                 if (p.getSupport() < tSupport ||
                     p.getConfidence() < tConfidence) {
                     pairList.remove(bName);
                 }
             }
         }
     }
 
     private void createOrStrengthenPair(Hashtable<String,Hashtable<String,Pair>>
                                        pairs,
                                        String f1, String f2) {
         Hashtable<String,Pair> funcPairs = pairs.get(f1);
         if (funcPairs == null) {
             funcPairs = new Hashtable<String,Pair>();
             pairs.put(f1, funcPairs);
         }
         Pair p = funcPairs.get(f2);
         if (p == null) {
             p = new Pair(f1, f2);
             funcPairs.put(f2, p);
         }
         p.strengthen();
     }
 
     private ArrayList<String> removeDuplicateCalls(ArrayList<String> calls) {
         HashSet<String> callSet = new HashSet<String>(calls);
         calls = new ArrayList<String>(callSet);
         return calls;
     }
 
     public ArrayList<Violation>
         getViolations(Hashtable<String,ArrayList<String>> cg,
                       Hashtable<String,Hashtable<String,Pair>> invariants) {
 
         ArrayList<Violation> violations = new ArrayList<Violation>();
 
         Set<Map.Entry<String,ArrayList<String>>> cgSet = cg.entrySet();
         Iterator functions = cgSet.iterator();
         while (functions.hasNext()) {
             Map.Entry<String,ArrayList<String>> entry = (Map.Entry<String,ArrayList<String>>)functions.next();
             String functionName = (String)entry.getKey();
             ArrayList<String> callsL = (ArrayList<String>)entry.getValue();
             HashSet<String> calls = new HashSet<String>(callsL);
 
             Iterator i = calls.iterator();
             while (i.hasNext()) {
                 Hashtable<String,Pair> invariantsForCall = invariants.get(i.next());
                 if (invariantsForCall == null) {
                     continue;
                 }
                 Enumeration pairs = invariantsForCall.elements();
                 while (pairs.hasMoreElements()) {
                     Pair invariant = (Pair)pairs.nextElement();
                     if (!calls.contains(invariant.getTarget())) {
                         violations.add(new Violation(functionName,
                                                       invariant));
                     }
                 }
             }
         }
 
         return violations;
     }
 
     public void run(String cgFile) {
         Hashtable<String,ArrayList<String>> cg = parseFile(cgFile);
         Hashtable<String,Hashtable<String,Pair>> invariants =
             getInvariantPairs(cg);
         ArrayList<Violation> violations = getViolations(cg, invariants);
         printViolations(violations);
     }
 
     public void printViolations(ArrayList<Violation> violations) {
         Enumeration e = Collections.enumeration(violations);
         while (e.hasMoreElements()) {
             Violation v = (Violation)e.nextElement();
             System.out.println(v);
         }
     }
 
     public static void main(String[] args) {
         if (args.length < 1) {
             System.out.println("Usage: ./pipair <bitcode file> <T SUPPORT> <T CONFIDENCE>,");
             System.exit(0);
         }
 
         Pipair prog = new Pipair();
         if (args.length >= 2) {
             prog.tSupport = Integer.parseInt(args[1]);
         }
         if (args.length >= 3) {
             prog.tConfidence = Float.parseFloat(args[2]);
         }
         prog.run(args[0]);
     }
 }
