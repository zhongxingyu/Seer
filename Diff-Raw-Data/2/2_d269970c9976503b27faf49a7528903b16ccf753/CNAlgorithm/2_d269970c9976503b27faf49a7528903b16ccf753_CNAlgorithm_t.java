 package algorithms.cna;
 
 /** Copyright 2011 (C) Felix Langenegger & Jonas Ruef */
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import datastructures.cna.CNAList;
 import datastructures.cna.CNAListComparator;
 import datastructures.cna.CNATable;
 import datastructures.mt.MinimalTheory;
 import datastructures.mt.MinimalTheorySet;
 import datastructures.tree.CNATreeNode;
 import datastructures.tree.MnecTree;
 import datastructures.tree.MsufTree;
 
 public class CNAlgorithm {
     private CNATable originalTable;
     private CNAList effects;
     private CNATable sufTable;
     private CNATable msufTable;
     private CNAList necList;
     private ArrayList<MinimalTheorySet> sets;
     private CNATable mnecTable;
 
     public CNAlgorithm(CNATable table) throws NecException {
 	originalTable = table;
 	sets = new ArrayList<MinimalTheorySet>();
 	identifyPE(originalTable);
     }
 
     /**
      * Step 0
      * 
      * @param originalTable
      * @throws NecException
      */
     private void identifyPE(CNATable originalTable) throws NecException {
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
 	run(effects, originalTable);
     }
 
     /**
      * Step 1
      * 
      * @param effects
      * @throws NecException
      */
     private void run(CNAList effects, CNATable originalTable)
 	    throws NecException {
 	CNATable table;
 	ArrayList<Integer> indexes = new ArrayList<Integer>();
 	for (int col = 0; col < originalTable.get(0).size(); col++) {
 	    for (int i = 0; i < effects.size(); i++) {
 		if (originalTable.get(0).get(col).equals(effects.get(i))) {
 		    indexes.add(col);
 		}
 	    }
 	}
 	for (int i = 0; i < indexes.size(); i++) {
 	    table = originalTable.clone();
 	    table.swap(indexes.get(i), originalTable.get(0).size() - 1);
 	    System.out.println("Effect: " + table.get(0).getLastElement());
 	    identifySUF(table);
 	}
     }
 
     /**
      * Step 2
      * 
      * @param table
      * @throws NecException
      **/
     private void identifySUF(CNATable originalTable) throws NecException {
 	sufTable = originalTable.clone();
 	sufTable.removeZeroEffects();
 	System.out.println("SufTable:\n" + sufTable);
 	// TODO Determine how big at min it must be.
	if (sufTable.size() <= 3) {
 	    System.out.println("SUF Table too small!");
 	    throw new NecException("SUF Table too small!");
 	}
 	indentifyMSUF(originalTable, sufTable);
     }
 
     private void indentifyMSUF(CNATable originalTable, CNATable sufTable)
 	    throws NecException {
 	MsufTree msufTree;
 	msufTable = new CNATable();
 	// i = 1 because first line holds factor names.
 	for (int i = 1; i < sufTable.size(); i++) {
 	    CNAList list = (CNAList) sufTable.get(i).clone();
 
 	    // IMPORTANT: Remove effect column of suffLine, if not tree we not
 	    // correctly be built.
 	    list.removeLastElement();
 
 	    CNATreeNode root = new CNATreeNode(list);
 	    msufTree = new MsufTree(root);
 	    msufTree.fillUpTree(root);
 	    msufTree.walk(root, originalTable, msufTable);
 	    msufTable.removeDuplicated();
 	}
 	System.out.println("MsufTable\n" + msufTable);
 	identifyNEC(msufTable, originalTable);
     }
 
     private void identifyNEC(CNATable msufTable, CNATable originalTable)
 	    throws NecException {
 	CNATable bundleTable = msufTable.summarizeBundles(msufTable,
 		originalTable);
 
 	necList = msufTable.getNecList();
 	necList.negate();
 	// Add effect column
 	necList.add("1");
 
 	for (CNAList list : bundleTable) {
 	    if (list.equals(necList)) {
 		throw new NecException();
 	    }
 	}
 
 	this.necList = msufTable.getNecList();
 	System.out.println("NEC: " + necList);
 	identifyMNEC(necList, bundleTable, originalTable);
     }
 
     private void identifyMNEC(CNAList necList, CNATable bundleTable,
 	    CNATable originalTable) {
 
 	necList.negate();
 	System.out.println("NEC Negated: " + necList);
 	MnecTree mnecTree;
 	mnecTable = new CNATable();
 	CNATreeNode root = new CNATreeNode(necList);
 	mnecTree = new MnecTree(root);
 
 	mnecTree.fillUpTree(root);
 	mnecTree.walk(root, bundleTable, mnecTable);
 	mnecTable.removeDuplicated();
 
 	if (mnecTable.size() == 0) {
 	    mnecTable.add(necList);
 	}
 
 	ArrayList<MinimalTheory> mtList = new ArrayList<MinimalTheory>();
 	for (CNAList list : mnecTable) {
 	    CNAList mtNames = new CNAList();
 	    for (int i = 0; i < list.size(); i++) {
 		if (!list.get(i).equals("$")) {
 		    mtNames.add(bundleTable.get(0).get(i));
 		}
 	    }
 	    MinimalTheory theory = new MinimalTheory(mtNames, originalTable
 		    .get(0).getLastElement());
 	    mtList.add(theory);
 	}
 
 	createMTSets(mtList);
 	System.out.println("All MTSets:\n" + sets);
     }
 
     // Getters and Setters
 
     private void createMTSets(ArrayList<MinimalTheory> mtList) {
 	if (sets.size() == 0) {
 	    for (MinimalTheory theory : mtList) {
 		MinimalTheorySet set = new MinimalTheorySet();
 		set.add(theory);
 		sets.add(set);
 	    }
 	} else {
 	    if (sets.size() < mtList.size()) {
 		fillUpSets(mtList.size());
 	    }
 	    for (int i = 0; i < sets.size(); i++) {
 		MinimalTheory theory = mtList.get(i);
 		MinimalTheorySet set = sets.get(i);
 		set.add(theory);
 	    }
 	}
 
     }
 
     private void fillUpSets(int size) {
 	while (sets.size() < size) {
 	    MinimalTheorySet set = sets.get(0);
 	    MinimalTheorySet newSet = new MinimalTheorySet();
 	    for (MinimalTheory theory : set) {
 		newSet.add(theory);
 	    }
 	    sets.add(newSet);
 	}
     }
 
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
 	return necList;
     }
 
     public CNAList getEffects() {
 	return effects;
     }
 
     public ArrayList<MinimalTheorySet> getSets() {
 	return sets;
     }
 }
