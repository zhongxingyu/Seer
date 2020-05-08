/* $Id: RuleSet.java,v 1.6 2003/09/16 04:45:30 vpapad Exp $
    Colombia -- Java version of the Columbia Database Optimization Framework
 
    Copyright (c)    Dept. of Computer Science , Portland State
    University and Dept. of  Computer Science & Engineering,
    OGI School of Science & Engineering, OHSU. All Rights Reserved.
 
    Permission to use, copy, modify, and distribute this software and
    its documentation is hereby granted, provided that both the
    copyright notice and this permission notice appear in all copies
    of the software, derivative works or modified versions, and any
    portions thereof, and that both notices appear in supporting
    documentation.
 
    THE AUTHORS, THE DEPT. OF COMPUTER SCIENCE DEPT. OF PORTLAND STATE
    UNIVERSITY AND DEPT. OF COMPUTER SCIENCE & ENGINEERING AT OHSU ALLOW
    USE OF THIS SOFTWARE IN ITS "AS IS" CONDITION, AND THEY DISCLAIM ANY
    LIABILITY OF ANY KIND FOR ANY DAMAGES WHATSOEVER RESULTING FROM THE
    USE OF THIS SOFTWARE.
 
    This software was developed with support of NSF grants IRI-9118360,
    IRI-9119446, IRI-9509955, IRI-9610013, IRI-9619977, IIS 0086002,
    and DARPA (ARPA order #8230, CECOM contract DAAB07-91-C-Q518).
 */
 
 package niagara.optimizer.colombia;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /** A RuleSet is a container of rules */
 public class RuleSet {
     private ArrayList rules;
     private SSP ssp;
 
     /** Here we cache the results of topMatch operations for rules */
     private HashMap topMatches;
 
     // XXX vpapad: UNIQ means the opposite of what it meant in Columbia
     // ... although the comment there matches this semantics!
     /** Is this a ruleset that never generates duplicate expressions? */
     private boolean unique;
 
     /** Does this ruleset allow the generation of cartesian products? */
     private boolean allowCartesian = false;
 
     private HashMap name2index;
 
     public RuleSet(ArrayList rules, boolean unique) {
         this.rules = rules;
         this.unique = unique;
         name2index = new HashMap(rules.size());
 
         for (int i = 0; i < rules.size(); i++) {
             // XXX vpapad: Should we copy the rule here?
             Rule r = (Rule) rules.get(i);
             r.setRuleSet(this);
             r.setIndex(i);
             name2index.put(r.getName(), new Integer(i));
         }
 
         // Now initialize the rules
         for (int i = 0; i < rules.size(); i++) {
             Rule r = (Rule) rules.get(i);
             r.initMasks(rules.size());
             r.initialize();
         }
 
         topMatches = new HashMap();
     }
 
     public int size() {
         return rules.size();
     }
 
     public boolean isUnique() {
         return unique;
     }
 
     public void setSSP(SSP ssp) {
         this.ssp = ssp;
     }
 
     public Rule get(int index) {
         return (Rule) rules.get(index);
     }
 
     public int getIndex(String name) {
         Integer i = (Integer) name2index.get(name);
         if (i == null)
             return -1;
         else
             return i.intValue();
     }
 
     public boolean allowCartesian() {
         return allowCartesian;
     }
 
     public ArrayList topMatch(Op op) {
         Class c = op.getClass();
         if (topMatches.containsKey(c))
             return (ArrayList) topMatches.get(c);
         else {
             ArrayList matches = new ArrayList();
             int nrules = size();
             for (int i = 0; i < nrules; i++) {
                 Rule r = get(i);
                if (r.topMatch(c))  
                     matches.add(r);
             }
             topMatches.put(c, matches);
             return matches;
         }
     }
 }
