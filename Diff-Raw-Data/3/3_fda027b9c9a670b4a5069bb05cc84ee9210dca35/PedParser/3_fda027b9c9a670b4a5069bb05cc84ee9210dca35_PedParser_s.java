 package edu.mit.wi.pedparser;
 
 import org._3pq.jgrapht.graph.SimpleGraph;
 import org._3pq.jgrapht.Graph;
 import org._3pq.jgrapht.UndirectedGraph;
 import org._3pq.jgrapht.alg.ConnectivityInspector;
 
 import java.util.*;
 
public class PedParser {
 
     double TRIO_SCORE = 2.001;
     double TRIO_DIFFERENTIAL = TRIO_SCORE - 1;
 
     public SimpleGraph buildGraph(Vector pedFileInds, double missingThresh) throws PedigreeException{
         SimpleGraph theGraph = new SimpleGraph();
 
         Hashtable indsByID = new Hashtable();
         Hashtable momsByID = new Hashtable();
         Hashtable dadsByID = new Hashtable();
 
         for (int i = 0; i < pedFileInds.size(); i++) {
             edu.mit.wi.pedfile.Individual ind = (edu.mit.wi.pedfile.Individual) pedFileInds.elementAt(i);
             String id = ind.getIndividualID();
             String dad = ind.getDadID();
             String mom = ind.getMomID();
             int gender = ind.getGender();
             double genopc = ind.getGenoPC();
             boolean missing = false;
             if (genopc < 1-missingThresh){
                 missing = true;
             }
             Individual newInd = new Individual(id,missing,gender,genopc);
             indsByID.put(id,newInd);
             momsByID.put(id,mom);
             dadsByID.put(id,dad);
         }
 
 
         Iterator itr = indsByID.values().iterator();
         while (itr.hasNext()){
             Individual ind = (Individual) itr.next();
             Individual mom = (Individual) indsByID.get(momsByID.get(ind.id));
             Individual dad = (Individual) indsByID.get(dadsByID.get(ind.id));
             if (mom != null ^ dad != null){
                 throw new PedigreeException("Individual " + ind +" has only one parent.");
             }
             if (dad != null && mom != null){
                 ind.addDad(dad);
                 dad.addKid(ind);
 
                 ind.addMom(mom);
                 mom.addKid(ind);
 
                 mom.addSpouse(dad);
                 dad.addSpouse(mom);
             }
         }
 
         Hashtable nodesByKids = new Hashtable();
         Hashtable nodesByParents = new Hashtable();
         HashSet usedPeople = new HashSet();
         itr = indsByID.values().iterator();
         while (itr.hasNext()){
             Individual ind = (Individual) itr.next();
             Vector parentsToAdd = new Vector();
             Vector kidsToAdd = new Vector();
             if(!nodesByKids.containsKey(ind) && (ind.dad != null || ind.mom != null)) {
                 PedTreeNode ptn = new PedTreeNode();
                 kidsToAdd.add(ind);
                 if(ind.mom != null) {
                     parentsToAdd.add(ind.mom);
                 }
                 if(ind.dad != null) {
                     parentsToAdd.add(ind.dad);
                 }
               
                 while(parentsToAdd.size() > 0) {
                     Individual next = (Individual) parentsToAdd.remove(parentsToAdd.size()-1);
                     Iterator sitr = next.spouses.iterator();
                     while(sitr.hasNext()) {
                         Individual s = (Individual) sitr.next();
                         if(!(ptn.parents.contains(s))) {
                             parentsToAdd.add(s);
                         }
                     }
                     ptn.addParent(next);
                     usedPeople.add(next);
                     nodesByParents.put(next,ptn);
                     kidsToAdd.addAll(next.kids);
                 }
 
                 ptn.addChildren(kidsToAdd);
 
                 for(int i=0;i<kidsToAdd.size();i++) {
                     nodesByKids.put(kidsToAdd.get(i),ptn);
                 }
 
                 usedPeople.addAll(kidsToAdd);
                 theGraph.addVertex(ptn);
             }
         }
 
         itr = indsByID.values().iterator();
 
         while(itr.hasNext()) {
             Individual ind = (Individual) itr.next();
             if(nodesByKids.containsKey(ind) && nodesByParents.containsKey(ind)) {
                 PedEdge theEdge = new PedEdge(nodesByKids.get(ind), nodesByParents.get(ind),ind);
                 theGraph.addEdge(theEdge);
             }
         }
 
         //now add any floating singletons
         itr = indsByID.values().iterator();
         while (itr.hasNext()){
             Individual ind = (Individual) itr.next();
             if (!usedPeople.contains(ind)){
                 theGraph.addVertex(ind);
             }
         }
 
 
 
         return theGraph;
     }
 
     public Vector parsePed(UndirectedGraph g) throws PedigreeException{
         //takes any graph and submits all connected subgraphs to parseConnectedGraph
         //returns the sum of those results
         Vector retVec = new Vector();
         ConnectivityInspector inspectorGadget = new ConnectivityInspector(g);
         Iterator itr = inspectorGadget.connectedSets().iterator();
         while (itr.hasNext()){
             SimpleGraph subGraph = new SimpleGraph();
             Iterator vitr = ((Set)itr.next()).iterator();
             HashSet edgeSet = new HashSet();
             while (vitr.hasNext()){
                 Object o = vitr.next();
                 subGraph.addVertex(o);
                 edgeSet.addAll(g.edgesOf(o));
             }
             subGraph.addAllEdges(edgeSet);
             retVec.addAll(parseConnectedGraph(subGraph));
         }
 
         return retVec;
     }
 
     private Vector parseConnectedGraph(UndirectedGraph g) throws PedigreeException{
         Iterator itr;
 
         ConnectivityInspector inspectorGadget = new ConnectivityInspector(g);
         if (!inspectorGadget.isGraphConnected()){
             throw new PedigreeException("Graph not connected.");
         }
 
         //this deals with floating singletons
         if (g.vertexSet().size() == 1){
             itr = g.vertexSet().iterator();
             Object o = itr.next();
             if (o instanceof Individual){
                 Vector v = new Vector();
                 if (!((Individual)o).missing){
                     v.add(((Individual)o).id);
                 }
                 return v;
             }
         }
 
         itr = g.vertexSet().iterator();
         while (itr.hasNext()){
             PedTreeNode starter = (PedTreeNode) itr.next();
             List edges = g.edgesOf(starter);
             if (starter.parents.size() + starter.kids.size() == edges.size()){
                 //we can't use this node because all its individuals are edges.
                 continue;
             }
             PedTreeNode fakeVertex = new PedTreeNode();
             Iterator litr = edges.iterator();
             HashSet edgeIndividuals = new HashSet();
             while (litr.hasNext()){
                 edgeIndividuals.add(((PedEdge)litr.next()).getInd());
             }
             HashSet allIndividuals = new HashSet();
             allIndividuals.addAll(starter.parents);
             allIndividuals.addAll(starter.kids);
             allIndividuals.removeAll(edgeIndividuals);
             Iterator fitr = allIndividuals.iterator();
             if (fitr.hasNext()){
                 Individual i = (Individual) fitr.next();
                 //if we select this person to be our fake edge he must not be an edge.
                 PedEdge e = new PedEdge(fakeVertex, starter, i);
                 ScoreData sd = scoreEdge(e,fakeVertex,g);
                 Vector retVec = new Vector();
                 Iterator sitr = sd.lineageSingletons.iterator();
                 while (sitr.hasNext()){
                     retVec.add(((Individual)sitr.next()).id);
                 }
                 Iterator titr = sd.lineageTrios.iterator();
                 while (titr.hasNext()){
                     Trio t = (Trio) titr.next();
                     retVec.add(t.mom.id);
                     retVec.add(t.dad.id);
                     retVec.add(t.kid.id);
                 }
                 return retVec;
             }
         }
         return null;
     }
 
     ScoreData scoreEdge(PedEdge theEdge, PedTreeNode source, Graph g) throws PedigreeException{
         Hashtable scoresByEdges = new Hashtable();
         Hashtable edgesByInds = new Hashtable();
 
         PedTreeNode thisVertex = (PedTreeNode) theEdge.oppositeVertex(source);
         Individual edgeInd = theEdge.getInd();
         List edges = g.edgesOf(thisVertex);
         Iterator eitr = edges.iterator();
         while (eitr.hasNext()){
             PedEdge ne = (PedEdge) eitr.next();
             if (ne != theEdge){
                 scoresByEdges.put(ne, scoreEdge(ne, thisVertex, g));
                 edgesByInds.put(ne.getInd(),ne);
             }
         }
 
         //this HashSet contains the Individuals who aren't available to use
         //in this node, since we choose to use the 2-score of their edge
         //HashSet lineageEdges = resolveEdges(thisVertex,scoresByEdges,edgesByInds,new Vector());
 
         Vector outgoingLineagePeople = new Vector();
         if (thisVertex.parents.contains(edgeInd)){
             outgoingLineagePeople.addAll(edgeInd.kids);
         }else{
             outgoingLineagePeople.add(edgeInd);
             if(edgeInd.dad != null) {
                 outgoingLineagePeople.add(edgeInd.dad);
                 outgoingLineagePeople.addAll(edgeInd.dad.kids);
             }
             if(edgeInd.mom != null) {
                 outgoingLineagePeople.addAll(edgeInd.mom.kids);
                 outgoingLineagePeople.add(edgeInd.mom);
             }
 
         }
 
         //start scoring
         //first, build the zeroscore by parsing the node with only people unrelated to edgeInd
 
         ScoreData sd0 = new ScoreData();
         if (thisVertex.parents.contains(edgeInd)){
             HashSet kids = (HashSet)thisVertex.kids.clone();
             HashSet parents = (HashSet)thisVertex.parents.clone();
             parents.remove(edgeInd);
             kids.removeAll(edgeInd.kids);
             Iterator itr = edgeInd.spouses.iterator();
             while (itr.hasNext()){
                 Individual s = (Individual) itr.next();
                 sd0.merge(scoreNodeRemainder(s,kids,parents,scoresByEdges,edgesByInds,edgeInd),true,false);
             }
         } else{
             HashSet kids = (HashSet)thisVertex.kids.clone();
             HashSet parents = (HashSet)thisVertex.parents.clone();
             kids.removeAll(edgeInd.mom.kids);
             kids.removeAll(edgeInd.dad.kids);
             parents.remove(edgeInd.mom);
             parents.remove(edgeInd.dad);
             Iterator itr = edgeInd.mom.spouses.iterator();
             while (itr.hasNext()){
                 Individual s = (Individual) itr.next();
                 if (s != edgeInd.dad){
                     sd0.merge(scoreNodeRemainder(s,kids,parents,scoresByEdges,edgesByInds,edgeInd),true,false);
                 }
             }
             itr = edgeInd.dad.spouses.iterator();
             while (itr.hasNext()){
                 Individual s = (Individual) itr.next();
                 if (s != edgeInd.mom){
                     sd0.merge(scoreNodeRemainder(s,kids,parents,scoresByEdges,edgesByInds,edgeInd),true,false);
                 }
             }
         }
 
         //now, parse the node allowing all members
         Individual starter = null;
 
 
         if(thisVertex.parents.contains(edgeInd)) {
             starter = edgeInd;
         } else {
             starter = edgeInd.mom;
         }
 
         HashSet kids = (HashSet)thisVertex.kids.clone();
         HashSet parents = (HashSet)thisVertex.parents.clone();
         ScoreData sd2 = scoreNodeRemainder(starter,kids,parents,scoresByEdges,edgesByInds,edgeInd);
 
         sd2.indyTrios = sd0.indyTrios;
         sd2.indySingletons = sd0.indySingletons;
         Iterator itr = thisVertex.getRelatedMembers(edgeInd).iterator();
         while (itr.hasNext()){
             Individual i = (Individual) itr.next();
             if (i != edgeInd){
                 if (edgesByInds.containsKey(i)){
                     ScoreData sd = (ScoreData)scoresByEdges.get(edgesByInds.get(i));
                     sd2.indyTrios.addAll(sd.indyTrios);
                     sd2.indySingletons.addAll(sd.indySingletons);
                 }
             }
         }
 
         return sd2;
     }
 
     ScoreData scoreNodeRemainder (Individual seed, HashSet kids, HashSet parents,
                                   Hashtable scoresByEdges, Hashtable edgesByInds, Individual edgeInd){
 
         Hashtable scores = new Hashtable();
         Trio curBestTrio = null;
         Individual curBestSingleton = null;
         boolean lookInNode = true;
 
         Iterator sitr = seed.spouses.iterator();
         while (sitr.hasNext()){
             Individual currentSpouse = (Individual) sitr.next();
             if (parents.contains(currentSpouse)){
                 HashSet p = (HashSet) parents.clone();
                 HashSet k = (HashSet) kids.clone();
                 p.remove(seed);
                 k.removeAll(seed.kids);
                 //we first score the spouse's stuff in the rest of the node
                 scores.put(currentSpouse, scoreNodeRemainder(currentSpouse,k,p,scoresByEdges,edgesByInds,edgeInd));
             }
         }
 
         if (edgesByInds.containsKey(seed)){
             scores.put(seed, (ScoreData) scoresByEdges.get(edgesByInds.get(seed)));
             //if seed's edge is better than a trio
             if (((ScoreData)scores.get(seed)).getScoreDifference() > TRIO_DIFFERENTIAL){
                 curBestSingleton = seed;
                 lookInNode = false;
             }
         }else{
             HashSet tmp = new HashSet();
             if (!seed.missing){
                 tmp.add(seed);
             }
             scores.put(seed,new ScoreData(new NodeScore(new Vector(), new HashSet()),
                     new NodeScore(new Vector(), tmp)));
         }
         Iterator kitr = seed.kids.iterator();
         while (kitr.hasNext()){
             Individual kid = (Individual) kitr.next();
             if (kids.contains(kid)){
                 if (edgesByInds.containsKey(kid)){
                     scores.put(kid, (ScoreData) scoresByEdges.get(edgesByInds.get(kid)));
                 }else{
                     HashSet tmp = new HashSet();
                     if (!kid.missing){
                         tmp.add(kid);
                     }
                     scores.put(kid,new ScoreData(new NodeScore(new Vector(), new HashSet()),
                             new NodeScore(new Vector(), tmp)));
                 }
             }
         }
 
         if(lookInNode) {
             //find the best usage of this node.
             ScoreData seScore = (ScoreData) scores.get(seed);
             sitr = seed.spouses.iterator();
             while (sitr.hasNext()){
                 Individual currentSpouse = (Individual) sitr.next();
                 if(parents.contains(currentSpouse)) {
                     ScoreData spScore = (ScoreData) scores.get(currentSpouse);
                     kitr = seed.kids.iterator();
                     while (kitr.hasNext()){
                         Individual curKid = (Individual) kitr.next();
                         if ((curKid.mom == currentSpouse || curKid.dad == currentSpouse) &&
                                 !curKid.missing && !curKid.mom.missing && !curKid.dad.missing){
                             Trio curTrio = new Trio(curKid, curKid.mom, curKid.dad);
                             boolean addTrio = false;
                             if (spScore.getScoreDifference() < TRIO_DIFFERENTIAL &&
                                     seScore.getScoreDifference() < TRIO_DIFFERENTIAL){
                                 addTrio = true;
                             }else if (spScore.getScoreDifference() == TRIO_DIFFERENTIAL){
                                 double compScore = curTrio.scaledGenoSum;
                                 if (curTrio.dad == seed){
                                     compScore -= curTrio.dad.genotypePercent;
                                 }else{
                                     compScore -= curTrio.mom.genotypePercent;
                                 }
                                 if (compScore > spScore.getGenoSumDifference()){
                                     addTrio = true;
                                 }
                             }else if (seScore.getScoreDifference() == TRIO_DIFFERENTIAL){
                                 double compScore = curTrio.scaledGenoSum;
                                 if (curTrio.dad == seed){
                                     compScore -= curTrio.mom.genotypePercent;
                                 }else{
                                     compScore -= curTrio.dad.genotypePercent;
                                 }
                                 if (compScore > seScore.getGenoSumDifference()){
                                     addTrio = true;
                                 }
                             }
                             if(addTrio){
                                 if (curBestTrio != null){
                                     ScoreData nssScore = null;
                                     if (curBestTrio.dad == seed){
                                         nssScore = (ScoreData)scores.get(curBestTrio.mom);
                                     }else{
                                         nssScore = (ScoreData)scores.get(curBestTrio.dad);
                                     }
                                     if(nssScore.getScoreDifference() > spScore.getScoreDifference()){
                                         curBestTrio = curTrio;
                                     }else if (nssScore.getScoreDifference() == spScore.getScoreDifference() &&
                                             curTrio.scaledGenoSum > curBestTrio.scaledGenoSum) {
                                         curBestTrio = curTrio;
                                     }
                                 } else {
                                     curBestTrio = curTrio;
                                 }
                             }
                         }
                     }
                 }
             }
 
 
             //best kid of a missing non-seed parent
             Individual curBestKid = null;
             Individual curBestMPKid = null;
             ScoreData curBestSD = new ScoreData();
             double curBestGSDiff = 0;
             ScoreData curBestMPSD = new ScoreData();
 
             ScoreData seedScore = (ScoreData) scores.get(seed);
             Iterator itr = seed.spouses.iterator();
             while(itr.hasNext()) {
                 Individual curSpouse = (Individual) itr.next();
                 if(parents.contains(curSpouse)) {
                     ScoreData curSpouseScore = (ScoreData) scores.get(curSpouse);
                     kitr = curSpouse.kids.iterator();
                     while(kitr.hasNext()) {
                         Individual curKid = (Individual) kitr.next();
                         if (curKid.dad == seed || curKid.mom == seed){
                             ScoreData ckScore = (ScoreData)scores.get(curKid);
                             if (curSpouseScore.getScoreDifference() == 0){
                                 //kids with a missing non-seed parent are preferred
                                 if (ckScore.getScoreDifference() > curBestMPSD.getScoreDifference()){
                                     curBestMPKid = curKid;
                                     curBestMPSD = ckScore;
                                 }else if (ckScore.getScoreDifference() == curBestMPSD.getScoreDifference()){
                                     if (ckScore.getGenoSumDifference() > curBestMPSD.getGenoSumDifference()){
                                         curBestMPKid = curKid;
                                         curBestMPSD = ckScore;
                                     }
                                 }
                             }else{
                                 //otherwise find the best general kid who's score is better than the sum of his
                                 //parents or is equal to his parents but better genotyped.
                                 if ((ckScore.getScoreDifference() > curSpouseScore.getScoreDifference() + seedScore.getScoreDifference()) ||
                                         (ckScore.getScoreDifference() == curSpouseScore.getScoreDifference() + seedScore.getScoreDifference() &&
                                         ckScore.getGenoSumDifference() > curSpouseScore.getGenoSumDifference())){
                                     if (ckScore.getScoreDifference() > curBestSD.getScoreDifference()){
                                         curBestKid = curKid;
                                         curBestSD = ckScore;
                                         curBestGSDiff = ckScore.getGenoSumDifference() - curSpouseScore.getGenoSumDifference();
                                     }else if(ckScore.getScoreDifference() == curBestSD.getScoreDifference()){
                                         if(ckScore.getGenoSumDifference() - curSpouseScore.getGenoSumDifference() >
                                                 curBestGSDiff) {
                                             //only get in here if ck is better in both score diff from non seed parent
                                             //and in genoPC
                                             curBestKid = curKid;
                                             curBestSD = ckScore;
                                             curBestGSDiff = ckScore.getGenoSumDifference() - curSpouseScore.getGenoSumDifference();
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
             //set curBestKid to be the best all-around kid found above
             if (curBestMPKid != null){
                 if (curBestKid != null){
                     if (curBestMPSD.getScoreDifference() >= curBestSD.getScoreDifference()){
                         curBestSingleton = curBestMPKid;
                     }else{
                         curBestSingleton = curBestKid;
                     }
                 }else{
                     curBestSingleton = curBestMPKid;
                 }
             }else{
                 curBestSingleton = curBestKid;
             }
             //if seed is better than the best kid use seed instead.
             if (curBestSingleton != null){
                 if (seedScore.getScoreDifference() > ((ScoreData)scores.get(curBestSingleton)).getScoreDifference()){
                     curBestSingleton = seed;
                 }else if (seedScore.getScoreDifference() == ((ScoreData)scores.get(curBestSingleton)).getScoreDifference()){
                     if (seedScore.getGenoSumDifference() > ((ScoreData)scores.get(curBestSingleton)).getGenoSumDifference()){
                         curBestSingleton = seed;
                     }
                 }
             }else{
                 curBestSingleton = seed;
             }
 
             if (curBestSingleton != null && curBestTrio != null &&
                     ((ScoreData)scores.get(curBestSingleton)).getScoreDifference() > 1){
                 //if the best singleton has a trio sized edge score compare and take based on genopc
                 double compScore = curBestTrio.scaledGenoSum;
                 if (curBestTrio.dad == seed){
                     compScore -= curBestTrio.mom.genotypePercent;
                 }else{
                     compScore -= curBestTrio.dad.genotypePercent;
                 }
                 if (compScore < ((ScoreData)scores.get(curBestSingleton)).getGenoSumDifference()){
                     curBestTrio = null;
                 }
             }
         }
 
         NodeScore zeroScore = new NodeScore(new Vector(), new HashSet());
         Iterator itr = scores.keySet().iterator();
         while (itr.hasNext()){
             Individual s = (Individual) itr.next();
             ScoreData sd = (ScoreData) scores.get(s);
             if (parents.contains(s) && s != seed){
                 if (s == edgeInd.mom || s == edgeInd.dad){
                     zeroScore.trios.addAll(sd.indyTrios);
                     zeroScore.singletons.addAll(sd.indySingletons);
                 }else{
                     zeroScore.trios.addAll(sd.lineageTrios);
                     zeroScore.singletons.addAll(sd.lineageSingletons);
                 }
             }else{
                 zeroScore.trios.addAll(sd.indyTrios);
                 zeroScore.singletons.addAll(sd.indySingletons);
             }
         }
         NodeScore twoScore = new NodeScore(new Vector(), new HashSet());
         twoScore.trios.addAll(zeroScore.trios);
         twoScore.singletons.addAll(zeroScore.singletons);
 
         if (curBestTrio != null){
             //use the trio in the returned score.
             if(curBestTrio.mom == edgeInd.mom && curBestTrio.dad == edgeInd.dad) {
                 //if edgeInd or his full sib is the kid in this trio, we remove non-seed parents
                 //0-score (so it gets added below)
                 ScoreData sdata = null;
                 if(seed == edgeInd.mom) {
                     sdata = ((ScoreData)scores.get(edgeInd.dad));
                 } else {
                     sdata = ((ScoreData)scores.get(edgeInd.mom));
                 }
                 twoScore.trios.removeAll(sdata.indyTrios);
                 twoScore.singletons.removeAll(sdata.indySingletons);
             } else if(curBestTrio.mom == edgeInd.mom || curBestTrio.dad == edgeInd.dad) {
                 ScoreData sdata = null;
                 if(seed == edgeInd.mom && parents.contains(edgeInd.dad)) {
                     sdata = ((ScoreData)scores.get(edgeInd.dad));
                 } else if(parents.contains(edgeInd.mom)){
                     sdata = ((ScoreData)scores.get(edgeInd.mom));
                 }
                 if(sdata != null) {
                     twoScore.trios.removeAll(sdata.indyTrios);
                     twoScore.singletons.removeAll(sdata.indySingletons);
                     twoScore.trios.addAll(sdata.lineageTrios);
                     twoScore.singletons.addAll(sdata.lineageSingletons);
                 }
             }
 
 
             twoScore.trios.add(curBestTrio);
             ScoreData sd = null;
             if (curBestTrio.mom == seed){
                 sd = (ScoreData) scores.get(curBestTrio.dad);
             }else {
                 sd = (ScoreData) scores.get(curBestTrio.mom);
             }
             if (sd != null){
                 twoScore.trios.removeAll(sd.lineageTrios);
                 twoScore.trios.addAll(sd.indyTrios);
                 twoScore.singletons.removeAll(sd.lineageSingletons);
                 twoScore.singletons.addAll(sd.indySingletons);
             }
 
 
 
         }else if (curBestSingleton != null){
             ScoreData sd = (ScoreData) scores.get(curBestSingleton);
             twoScore.trios.removeAll(sd.indyTrios);
             twoScore.singletons.removeAll(sd.indySingletons);
             twoScore.trios.addAll(sd.lineageTrios);
             twoScore.singletons.addAll(sd.lineageSingletons);
 
             if (curBestSingleton != seed){
 
                 if (!(curBestSingleton.mom == edgeInd.mom && curBestSingleton.dad == edgeInd.dad)){
                     if (curBestSingleton.mom == seed){
                         sd = (ScoreData) scores.get(curBestSingleton.dad);
                     }else{
                         sd = (ScoreData) scores.get(curBestSingleton.mom);
                     }
                     twoScore.trios.removeAll(sd.lineageTrios);
                     twoScore.trios.addAll(sd.indyTrios);
                     twoScore.singletons.removeAll(sd.lineageSingletons);
                     twoScore.singletons.addAll(sd.indySingletons);
 
                     ScoreData sdata = null;
                     if(seed == edgeInd.mom && parents.contains(edgeInd.dad)) {
                         sdata = (ScoreData) scores.get(edgeInd.dad);
                     } else if(seed == edgeInd.dad && parents.contains(edgeInd.mom)) {
                         sdata = (ScoreData) scores.get(edgeInd.mom);
                     }
                     if(sdata != null) {
                         twoScore.trios.removeAll(sdata.indyTrios);
                         twoScore.trios.addAll(sdata.lineageTrios);
                         twoScore.singletons.removeAll(sdata.indySingletons);
                         twoScore.singletons.addAll(sdata.lineageSingletons);
                     }
                 }
             } else {
                 ScoreData sdata = null;
                 if(seed == edgeInd.mom && parents.contains(edgeInd.dad)) {
                     sdata = (ScoreData) scores.get(edgeInd.dad);
                 } else if(seed == edgeInd.dad && parents.contains(edgeInd.mom)) {
                     sdata = (ScoreData) scores.get(edgeInd.mom);
                 }
                 if(sdata != null) {
                     twoScore.trios.removeAll(sdata.indyTrios);
                     twoScore.trios.addAll(sdata.lineageTrios);
                     twoScore.singletons.removeAll(sdata.indySingletons);
                     twoScore.singletons.addAll(sdata.lineageSingletons);
                 }
             }
         }
 
         return new ScoreData(zeroScore,twoScore);
     }
 
     class Trio{
         Individual kid, mom, dad;
         double scaledGenoSum;
 
         Trio (Individual k, Individual m, Individual d){
             kid = k;
             mom = m;
             dad = d;
             scaledGenoSum = 2*((k.genotypePercent + m.genotypePercent + d.genotypePercent)/3);
         }
 
         public String toString(){
             return "k: " + kid + " m: " + mom + " d: " + dad;
         }
     }
 
     class NodeScore{
         Vector trios;
         HashSet singletons;
         NodeScore (Vector t, HashSet s){
             trios = t;
             singletons = s;
         }
     }
 
     class ScoreData {
 
         Vector lineageTrios, indyTrios;
         HashSet lineageSingletons, indySingletons;
 
         double getLineageScore(){
             return ((lineageTrios.size()) * 2.001) + lineageSingletons.size();
         }
 
         double getIndyScore(){
             return ((indyTrios.size())*2.001) + indySingletons.size();
         }
 
         double getScoreDifference(){
             return getLineageScore() - getIndyScore();
         }
 
         public String toString(){
             return "lin: " + getLineageScore() + " indy: " + getIndyScore();
         }
 
         public String membersToString(){
             StringBuffer retStr = new StringBuffer();
             if (lineageTrios.size() > 0){
                 retStr.append("LT: ");
                 for (int i = 0; i < lineageTrios.size(); i++) {
                     Trio trio = (Trio) lineageTrios.elementAt(i);
                     retStr.append("[");
                     retStr.append(trio);
                     retStr.append("] ");
                 }
                 retStr.append("\n");
             }
             if (lineageSingletons.size() > 0){
                 retStr.append("LS: ");
                 for (Iterator iterator = lineageSingletons.iterator(); iterator.hasNext();) {
                     Object o = (Object) iterator.next();
                     retStr.append(o);
                     retStr.append(" ");
                 }
                 retStr.append("\n");
             }
 
             if (indyTrios.size() > 0){
                 retStr.append("IT: ");
                 for (int i = 0; i < indyTrios.size(); i++) {
                     Trio trio = (Trio) indyTrios.elementAt(i);
                     retStr.append("[");
                     retStr.append(trio);
                     retStr.append("] ");
                 }
                 retStr.append("\n");
             }
 
             if (indySingletons.size() > 0){
                 retStr.append("IS: ");
                 for (Iterator iterator = indySingletons.iterator(); iterator.hasNext();) {
                     Object o = (Object) iterator.next();
                     retStr.append(o);
                     retStr.append(" ");
                 }
                 retStr.append("\n");
             }
 
             return retStr.toString();
         }
 
         public boolean hasSameLineageScore(ScoreData comp){
             SdComparator sdc = new SdComparator();
             int z = sdc.compare(this,comp);
             if (z == 0){
                 return true;
             }else{
                 return false;
             }
         }
 
         ScoreData(NodeScore us0, NodeScore us2){
             lineageTrios = us2.trios;
             lineageSingletons = us2.singletons;
 
             indyTrios = us0.trios;
             indySingletons = us0.singletons;
         }
 
         ScoreData(){
             lineageTrios = new Vector();
             indyTrios = new Vector();
 
             lineageSingletons = new HashSet();
             indySingletons = new HashSet();
         }
 
         public void merge(ScoreData data, boolean fromLineage, boolean toLineage) {
             if (fromLineage && toLineage){
                 lineageTrios.addAll(data.lineageTrios);
                 lineageSingletons.addAll(data.lineageSingletons);
             }else if (fromLineage && !toLineage){
                 indyTrios.addAll(data.lineageTrios);
                 indySingletons.addAll(data.lineageSingletons);
             }else if (!fromLineage && toLineage){
                 lineageTrios.addAll(data.indyTrios);
                 lineageSingletons.addAll(data.indySingletons);
             }else{
                 indyTrios.addAll(data.indyTrios);
                 indySingletons.addAll(data.indySingletons);
             }
         }
 
         public double getLineageGenoSum() {
             double total = 0;
 
             Iterator itr = lineageSingletons.iterator();
             while(itr.hasNext()) {
                 total += ((Individual)itr.next()).genotypePercent;
             }
 
             itr = lineageTrios.iterator();
             while(itr.hasNext()) {
                 total += (((Trio)itr.next()).scaledGenoSum);
             }
 
             return total;
         }
 
         public double getIndyGenoSum() {
             double total = 0;
 
             Iterator itr = indySingletons.iterator();
             while(itr.hasNext()) {
                 total += ((Individual)itr.next()).genotypePercent;
             }
 
             itr = indyTrios.iterator();
             while(itr.hasNext()) {
                 total += (((Trio)itr.next()).scaledGenoSum);
             }
 
             return total;
         }
 
         public double getGenoSumDifference() {
             return getLineageGenoSum() - getIndyGenoSum();
         }
 
         class SdComparator implements Comparator{
             public int compare(Object o1, Object o2) {
                 ScoreData sd1  = (ScoreData) o1;
                 ScoreData sd2 = (ScoreData) o2;
 
                 if (sd1.lineageSingletons.size() != sd2.lineageSingletons.size()){
                     return -1;
                 }
                 Iterator itr = sd1.lineageSingletons.iterator();
                 while (itr.hasNext()){
                     if (!sd2.lineageSingletons.contains(itr.next())){
                         return -1;
                     }
                 }
 
                 if (sd1.lineageTrios.size() != sd2.lineageTrios.size()){
                     return -1;
                 }
                 for (int x = 0; x < sd1.lineageTrios.size(); x++){
                     Trio t1 = (Trio) sd1.lineageTrios.elementAt(x);
                     boolean found = false;
                     for (int y = 0; y < sd2.lineageTrios.size(); y++){
                         Trio t2 = (Trio) sd2.lineageTrios.elementAt(y);
                         if (t1.mom == t2.mom && t1.dad == t2.dad && t1.kid == t2.kid){
                             found = true;
                             break;
                         }
                     }
                     if (!found){
                         return -1;
                     }
                 }
                 return 0;
             }
         }
     }
 }
