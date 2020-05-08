 package algorithms.cna;
 
 /** 
  * Copyright (C) <2011> 
  * @author Jonas Ruef & Felix Langenegger <cis.unibe.ch@gmail.com>
  * @license GPLv3, for more informations see Readme.mdown 
  */
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.concurrent.CompletionService;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorCompletionService;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import datastructures.cna.CNAList;
 import datastructures.cna.CNAListComparator;
 import datastructures.cna.CNATable;
 import datastructures.mt.MinimalTheory;
 import datastructures.mt.MinimalTheorySet;
 import datastructures.tree.CNATreeNode;
 import datastructures.tree.MnecTree;
 import datastructures.tree.MsufTree;
 
 public class CNAlgorithm {
     private final CNATable originalTable;
     private CNAList effects;
     private CNATable sufTable;
     private CNATable msufTable;
     private CNAList nec;
     private final ArrayList<MinimalTheorySet> sets;
     private CNATable mnecTable;
     private final ArrayList<MinimalTheory> allTheories;
     private CNAList notEffectsList;
 
     public CNAlgorithm(CNATable table) throws CNAException,
 	    InterruptedException, ExecutionException {
 	originalTable = table;
 	sets = new ArrayList<MinimalTheorySet>();
 	allTheories = new ArrayList<MinimalTheory>();
 
 	identifyPE(originalTable);
     }
 
     public CNAlgorithm(CNATable table, CNAList notEffectsList)
 	    throws CNAException, InterruptedException, ExecutionException {
 	originalTable = table;
 	sets = new ArrayList<MinimalTheorySet>();
 	allTheories = new ArrayList<MinimalTheory>();
 	this.notEffectsList = notEffectsList;
 
 	identifyPE(originalTable);
     }
 
     /**
      * Step 0
      * 
      * @param originalTable
      * @throws CNAException
      * @throws InterruptedException
      * @throws ExecutionException
      */
     private void identifyPE(CNATable originalTable) throws CNAException,
 	    InterruptedException, ExecutionException {
 	effects = new CNAList();
 	CNATable table = originalTable.clone();
 	CNAListComparator comparator = new CNAListComparator();
 
 	ArrayList<Integer> indexes = new ArrayList<Integer>();
 	for (int row = 1; row < table.size(); row++) {
 	    for (int row2 = 1; row2 < table.size(); row2++) {
 		int cr = comparator.compare(table.get(row2), table.get(row));
 		if (cr != -1) {
 		    indexes.add(cr);
 		}
 	    }
 	}
 	HashSet duplicate = new HashSet(indexes);
 	indexes.clear();
 	indexes.addAll(duplicate);
 	effects = table.get(0);
 	for (int i = indexes.size() - 1; i >= 0; i--) {
 	    effects.remove(indexes.get(i));
 	}
 
 	for (int i = 0; i < effects.size(); i++) {
 	    String cur = effects.get(i);
 	    if (cur.contains("¬")) {
 		effects.remove(i);
 		i--;
 	    }
 
 	}
 	if (effects.size() < 1) {
 	    throw new CNAException("No effects could be identified.");
 	}
 	if (notEffectsList != null) {
 	    removeNotEffects();
 	}
 
 	run(effects, originalTable);
     }
 
     /**
      * Step 1
      * 
      * @param effects
      * @throws CNAException
      * @throws InterruptedException
      * @throws ExecutionException
      */
     private void run(CNAList effects, CNATable originalTable)
 	    throws CNAException, InterruptedException, ExecutionException {
 	ArrayList<Integer> indexes = new ArrayList<Integer>();
 	for (int col = 0; col < originalTable.get(0).size(); col++) {
 	    for (int i = 0; i < effects.size(); i++) {
 		String curEffect = effects.get(i);
 		if (originalTable.get(0).get(col).equals(curEffect)) {
 		    indexes.add(col);
 		}
 	    }
 	}
 	System.out.println("Table: " + originalTable);
 	identifySUF(originalTable, indexes);
     }
 
     /**
      * Step 2
      * 
      * @param table
      * @throws CNAException
      * @throws InterruptedException
      * @throws ExecutionException
      **/
     private void identifySUF(CNATable originalTable, ArrayList<Integer> indexes)
 	    throws CNAException, InterruptedException, ExecutionException {
 	CNATable newOrderedOrigTable = new CNATable();
 	for (int i = 0; i < indexes.size(); i++) {
 	    newOrderedOrigTable = originalTable.clone();
 	    newOrderedOrigTable.swap(indexes.get(i), originalTable.get(0)
 		    .size() - 1);
 	    System.out.println("**********************");
 	    System.out.println("Effect: "
 		    + newOrderedOrigTable.get(0).getLastElement());
 
 	    sufTable = newOrderedOrigTable.clone();
 	    sufTable.removeZeroEffects();
 	    if (sufTable.size() <= 1) {
 		if (i == indexes.size() - 1) {
 		    throw new CNAException(
 			    "You entered a coincidence table where no line with a instantiated effect exists or which is not according to minimal diversity condition.");
 		}
 	    } else {
 		System.out.println("SufTable\n" + sufTable);
 		indentifyMSUF(newOrderedOrigTable, sufTable);
 	    }
 	}
     }
 
     private void indentifyMSUF(CNATable originalTable, CNATable sufTable)
 	    throws CNAException, InterruptedException, ExecutionException {
 	MsufTree msufTree;
 	msufTable = new CNATable();
 
 	// Controls how many threads run at the same time
 	ExecutorService threadPool = Executors.newFixedThreadPool(10);
 	CompletionService<CNATable> pool = new ExecutorCompletionService<CNATable>(
 		threadPool);
 
 	// i = 1 because first line holds factor names.
 	for (int i = 1; i < sufTable.size(); i++) {
 	    CNATable msufTableThread = new CNATable();
 
 	    /** None Threading stuff */
 	    CNAList list = (CNAList) sufTable.get(i).clone();
 	    boolean stopWalk = false;
 	    list.remove(list.size() - 1);
 	    CNATreeNode root = new CNATreeNode(list);
 	    /** None Threading stuff */
 
 	    msufTree = new MsufTree(root, originalTable, msufTableThread,
 		    stopWalk);
 	    msufTree.fillUpTree(root);
 	    pool.submit(msufTree);
 	    // System.out.println("Run: " + i);
 	}
 
 	for (int i = 1; i < sufTable.size(); i++) {
 	    CNATable curTable;
 	    curTable = pool.take().get();
 	    for (CNAList list : curTable) {
 		msufTable.add(list);
 	    }
 	}
 
 	threadPool.shutdown();
 	msufTable.removeDuplicated();
 	System.out.println("MsufTable\n" + msufTable);
 
 	identifyNEC(msufTable, originalTable);
     }
 
     private void identifyNEC(CNATable msufTable, CNATable originalTable)
 	    throws CNAException {
 	CNATable bundleTable = msufTable.summarizeBundles(msufTable,
 		originalTable);
 
 	nec = msufTable.getNec();
 	nec.invert();
 	ArrayList<CNAList> negations = nec.negate(nec);
 
 	for (CNAList negatedNec : negations) {
 	    for (CNAList list : bundleTable) {
 		if (list.equals(negatedNec)) {
 		    throw new CNAException(
 			    "No necessary condition could be identified.");
 		}
 	    }
 	}
 
 	this.nec = msufTable.getNec();
	System.out.println("NecTable\n" + nec);
 	identifyMNEC(nec, bundleTable, originalTable);
     }
 
     private void identifyMNEC(CNAList necList, CNATable bundleTable,
 	    CNATable originalTable) {
 
 	necList.invert();
 	MnecTree mnecTree;
 	mnecTable = new CNATable();
 	CNATreeNode root = new CNATreeNode(necList);
	System.out.println("rootNodes: " + root.toString());
 	mnecTree = new MnecTree(root);
 
 	mnecTree.fillUpTree(root);
 	boolean stopWalk = false;
 	mnecTree.walk(root, bundleTable, mnecTable, stopWalk);
 	mnecTable.removeDuplicated();
 
 	ArrayList<MinimalTheory> mtList = new ArrayList<MinimalTheory>();
 	for (CNAList list : mnecTable) {
 	    CNAList mtNames = new CNAList();
 	    for (int i = 0; i < list.size(); i++) {
 		if (!list.get(i).equals("$")) {
 		    // When factor is negative
 		    if (list.get(i).length() > 1) {
 			String bundleCoinc = list.get(i);
 			String bundle = new String();
 			for (int j = 0; j < bundleCoinc.length(); j++) {
 			    if (bundleCoinc.charAt(j) == '1') {
 				bundle += "¬" + bundleTable.get(0).get(i).charAt(j);
 			    } else {
 				bundle += bundleTable.get(0).get(i).charAt(j);
 			    }
 			}
 			mtNames.add(bundle);
 		    } else {
 			if (list.get(i).equals("1")) {
 			    mtNames.add("¬" + bundleTable.get(0).get(i));
 			} else {
 			    mtNames.add(bundleTable.get(0).get(i));
 			}
 		    }
 
 		}
 	    }
 	    MinimalTheory theory = new MinimalTheory(mtNames, originalTable
 		    .get(0).getLastElement());
 	    mtList.add(theory);
 	}
 	System.out.println("Bundle Table\n" + bundleTable);
 	allTheories.addAll(mtList);
 	System.out.println("MnecTable\n" + mnecTable);
 	System.out.println("mtList\n" + mtList);
 	System.out.println("**********************");
 	createMTSets(mtList);
     }
 
     private void createMTSets(ArrayList<MinimalTheory> mtList) {
 	if (sets.size() == 0) {
 	    for (MinimalTheory theory : mtList) {
 		MinimalTheorySet set = new MinimalTheorySet();
 		set.add(theory);
 		sets.add(set);
 	    }
 	} else {
 	    ArrayList<MinimalTheorySet> temp = new ArrayList<MinimalTheorySet>();
 	    for (MinimalTheorySet set : sets) {
 		for (MinimalTheory theory : mtList) {
 		    MinimalTheorySet clone = set.duplicate(set);
 		    clone.add(theory);
 		    temp.add(clone);
 		}
 	    }
 	    sets.clear();
 	    sets.addAll(temp);
 	}
 
     }
 
     /** Helpers */
     private void removeNotEffects() {
 	for (int i = effects.size() - 1; i >= 0; i--) {
 	    String effect = effects.get(i);
 	    for (int j = 0; j < notEffectsList.size(); j++) {
 		String notEffect = notEffectsList.get(j);
 		notEffect = notEffect.replace(' ', '_');
 		if (effect.equals(notEffect)) {
 		    effects.remove(i);
 		    break;
 		}
 	    }
 	}
     }
 
     /** Getter and Setters */
 
     public CNATable getOriginalTable() {
 	return originalTable;
     }
 
     public CNATable getSufTable() {
 	return sufTable;
     }
 
     public CNATable getMsufTable() {
 	return msufTable;
     }
 
     public CNATable getMnecTable() {
 	return mnecTable;
     }
 
     public CNAList getNecList() {
 	return nec;
     }
 
     public CNAList getEffects() {
 	return effects;
     }
 
     public ArrayList<MinimalTheorySet> getSets() {
 	return sets;
     }
 
     public ArrayList<MinimalTheory> getAllTheories() {
 	return allTheories;
     }
 }
