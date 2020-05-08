 import java.io.*;
 import java.util.*;
 /***
 Version 2 of Reactions, trying to clean up code so debugging is easier
 */
 public class Reactionsv2 {
     public Molecule reactants;
     public Molecule products;
 
     public static void main(String[] args) throws FileNotFoundException {
 	Reactionsv2 reactions = new Reactionsv2();
 	reactions.createReaction(args);
 	Molecule reactant = reactions.reactants;
 	for (int i = 0; i < reactant.getAtoms().size(); i++) {
 	    System.out.print(reactant.getAtoms().get(i) + " ");
 	}
 	System.out.println();
 	reactant.generateFreq();
 	int [] freq = reactant.getFreqs();
 	for (int i=0; i<freq.length; i++) {
 	    System.out.print(freq[i] + " ");
 	}
 	System.out.println();
 	System.out.println("molecules = " + reactant.findNumOfMlcs());
 	HashMap<String, Integer> bondTypes = reactant.listChangedBonds();
 	Set<String> bondPics = bondTypes.keySet();
 	for (String s : bondPics) {
 	    System.out.println(s + " " + bondTypes.get(s));
 	}
 	reactant.buildMatchTrees();
 	int atomNumCount = 1;
 	for (ArrayList<String> atomTree : reactant.getMatchTrees()) {
 	    System.out.print(atomNumCount + ": ");
 	    for (String s : atomTree) {
 		System.out.print(s + ", ");
 	    }
 	    System.out.println();
 	    atomNumCount++;
 	}
 	Molecule product = reactions.products;
 	product.generateFreq();
 	int [] pfreq = product.getFreqs();
 	for (int i=0; i<pfreq.length; i++) {
 	    System.out.print(pfreq[i] + " ");
 	}
 	System.out.println();
 	System.out.println("molecules = " + product.findNumOfMlcs());
 	HashMap<String, Integer> pbondTypes = product.listChangedBonds();
 	Set<String> pbondPics = pbondTypes.keySet();
 	for (String s : pbondPics) {
 	    System.out.println(s + " " + pbondTypes.get(s));
 	}
 	product.buildMatchTrees();
 	int patomNumCount = 1;
 	for (ArrayList<String> patomTree : product.getMatchTrees()) {
 	    System.out.print(patomNumCount + ": ");
 	    for (String s : patomTree) {
 		System.out.print(s + ", ");
 	    }
 	    System.out.println();
 	    patomNumCount ++;
 	}
 	reactions.matchAtoms(reactant, product);
     }
     
     public void createReaction(String[] args) throws FileNotFoundException {
 	File reactant;
 	Scanner reactantSc;
 	Molecule rmolecule;
 	File product;
 	Scanner productSc;
 	Molecule pmolecule;
 	if (args.length != 2) {
 	    System.out.println("Wrong number  of arguments, enter 2 arguments: reactant product");
 	    System.exit(0);
 	}
 	reactant = new File (args[0]);
 	reactantSc = new Scanner(reactant);
 	rmolecule = createMolecule(reactantSc);
 	reactants = rmolecule;
 	product = new File(args[1]);
 	productSc = new Scanner(product);
 	pmolecule = createMolecule(productSc);
 	products = pmolecule;
 	System.out.println("Reactant # of atoms = " + reactants.getNumberOfAtoms());
 	System.out.println("Reactant # of bonds = " + reactants.getNumberOfBonds());
 	System.out.println("Reactant atoms      = " + reactants.getAtoms());
 	System.out.println("Reactant bonds      = " + reactants.getBonds());
 	System.out.println("Product # of atoms = " + products.getNumberOfAtoms());
 	System.out.println("Product # of bonds = " + products.getNumberOfBonds());
 	System.out.println("Product atoms      = " + products.getAtoms());
 	System.out.println("Product bonds      = " + products.getBonds());
     }
     
     public Molecule createMolecule(Scanner molsc) {
 	ArrayList<String> bonds = new ArrayList<String>();
 	ArrayList<String> atoms = new ArrayList<String>();
 	String info = "";
 	String trash = molsc.nextLine();
 	Scanner trashsc = new Scanner(trash);
 	Boolean molFileStart = false;
 	while (molFileStart == false) {
 	    if (trashsc.hasNextInt()) {
 		info = trash;
 		molFileStart = true;
 	    } else {
 		trash = molsc.nextLine();
 		trashsc = new Scanner(trash);
 	    }
 	}
 	Scanner infosc = new Scanner (info);
 	int numberOfAtoms = infosc.nextInt();
 	int numberOfBonds = infosc.nextInt();
 	String temp = molsc.nextLine();
 	while (temp.charAt(2) == ' ') {
 	    Scanner tempsc = new Scanner(temp);
 	    String atom = tempsc.next();
 	    atom = tempsc.next();
 	    atom = tempsc.next();
 	    atom = tempsc.next();
 	    atoms.add(atom);
 	    temp = molsc.nextLine();
 	}
 	Scanner bondsc = new Scanner(temp);
 	while (bondsc.hasNextInt()) {
 	    String bond = "" + bondsc.nextInt() + " " + bondsc.nextInt() + " " + bondsc.nextInt();
 	    bonds.add(bond);
 	    temp = molsc.nextLine();
 	    bondsc = new Scanner(temp);
 	}
 	Molecule molecule = new Molecule(atoms, numberOfBonds, numberOfAtoms, bonds);
 	return molecule;
     }
 
     public void matchAtoms(Molecule m1, Molecule m2) {
 	Map <Integer, Integer> mapping = new HashMap<Integer, Integer>();
 	int m1AtomNum = 0;
 	int m2AtomNum = 0;
 	int rootMatches = 0;
 	HashMap<String, Integer> m1BondTypes = m1.listChangedBonds();
 	HashMap<String, Integer> m2BondTypes = m2.listChangedBonds();
 	Set<String> m1Keys = m1BondTypes.keySet();
 	HashMap<String, ArrayList<String>> changedBonds = new HashMap<String, ArrayList<String>>();
 	changedBonds.put("+", new ArrayList<String>());
 	changedBonds.put("-", new ArrayList<String>());
 	boolean firstThrough = false;
 	for (String key : m1Keys) {
 	    if (!m2BondTypes.containsKey(key)) {
 		changedBonds.get("-").add(key);
 	    } else {
 		for (String key2 : m2BondTypes.keySet()) {
 		    if (!m1BondTypes.containsKey(key2) && firstThrough == false) {
 			changedBonds.get("+").add(key2);
 		    } else if (!m1BondTypes.containsKey(key2) && firstThrough == true) {
 			continue;
 		    } else {
 			int key1Freq = m1BondTypes.get(key);
 			int key2Freq = m2BondTypes.get(key);
 			if (key1Freq - key2Freq > 0) {
 			    changedBonds.get("-").add(key);
 			} else if (key1Freq - key2Freq < 0) {
 			    changedBonds.get("+").add(key);
 			}
 		    }
 		} // close key2 for loop
 	    }
 	} // close key for loop
 	ArrayList<String> rmvedNodes = new ArrayList<String>();
 	ArrayList<String> addedNodes = new ArrayList<String>();
 	for (String add : changedBonds.get("+")) {
 	    //System.out.println("+ " + add);
 	    Scanner bondsc = new Scanner(add);
 	    String parentAtom = bondsc.next();
 	    String bondtype = bondsc.next();
 	    String bondedAtom = bondsc.next();	   
 	    if (bondtype.equals("-")) {
 		addedNodes.add(bondedAtom + "1");
 		addedNodes.add(parentAtom + "1");
 	    } else if (bondtype.equals("=")) {
 		addedNodes.add(bondedAtom + "2");
 	    } else {
 		addedNodes.add(bondedAtom + "3");
 	    }
 	} // close add for loop
 	for (String lose : changedBonds.get("-")) {
 	    //System.out.println("- " + lose);
 	    Scanner bondsc = new Scanner(lose);
 	    String  parentAtom = bondsc.next();
 	    String bondtype = bondsc.next();
 	    String bondedAtom = bondsc.next();	    
 	    if (bondtype.equals("-")) {
 	        rmvedNodes.add(bondedAtom + "1");
 	    } else if (bondtype.equals("=")) {
 		rmvedNodes.add(bondedAtom + "2");
 	    } else {
 		rmvedNodes.add(bondedAtom + "3");
 	    }
 	} // close lose for loop
 	System.out.println("Added Nodes: " + addedNodes);
 	System.out.println("Lost Nodes: " + rmvedNodes);
 	
 	ArrayList<Integer> m1atomsMapped = new ArrayList<Integer>();
 	ArrayList<Integer> m2atomsMapped = new ArrayList<Integer>();
 	HashMap<Integer, Integer> atomMap = new HashMap<Integer, Integer>();
 	ArrayList<matchTreeNode> m1parents = m1.getatomTrees();
 	ArrayList<matchTreeNode> m2parents = m2.getatomTrees();
 	ArrayList<Integer> m2visited = new ArrayList<Integer>();
 
 	for (int m1parIndex = 0; m1parIndex < m1parents.size(); m1parIndex++) {
 	    matchTreeNode m1curr = m1parents.get(m1parIndex);
 	    for (int m2parIndex = 0; m2parIndex < m2parents.size(); m2parIndex++) {
 		matchTreeNode m2curr = m2parents.get(m2parIndex);
 		if (m1curr.toString().equals(m2curr.toString()) && !m2visited.contains(m2parIndex)) {
 		    ArrayList<matchTreeNode> m1children1 = m1curr.getChildren();
 		    ArrayList<matchTreeNode> m2children1 = m2curr.getChildren();
 		    ArrayList<matchTreeNode> match = compareChildren(m1children1, m2children1);
 		    if (match.size() != 0) {
 			ArrayList<Integer> m2ChldrnMatched = new ArrayList<Integer>();
 			// insert removal comparisons
 			if (m1children1.size() < m2children1.size()) {
 			    ArrayList<Integer> m2lvl1matched = new ArrayList<Integer>();
 			    for (int m1test1 = 0; m1test1 < m1children1.size(); m1test1 ++) {
 				for (int m2test1 = 0; m2test1 < m2children1.size(); m2test1 ++) {
 				    matchTreeNode m1lvl1curr = m1children1.get(m1test1);
 				    matchTreeNode m2lvl1curr = m2children1.get(m2test1);
 				    if (m1lvl1curr.toString().equals(m2lvl1curr.toString()) && !m2lvl1matched.contains(m2test1)) {
 					ArrayList<matchTreeNode> m1lvl1children = m1lvl1curr.getChildren();
 					ArrayList<matchTreeNode> m2lvl1children = m2lvl1curr.getChildren();
 					System.out.println(m1parIndex + " " + m1lvl1children + " " + m2lvl1children);
 					ArrayList<matchTreeNode> lvl1chldresults = compareChildren(m1lvl1children, m2lvl1children);
 					if (lvl1chldresults.size() == 0) {
 					    ArrayList<Integer> m2lvl2matched = new ArrayList<Integer>();
 					    for (int m1test2 = 0; m1test2 < m1lvl1children.size(); m1test2 ++) {
 						for (int m2test2 = 0; m2test2 < m2lvl1children.size(); m2test2 ++) {
 						    matchTreeNode m1lvl2curr = m1lvl1children.get(m1test2);
 						    matchTreeNode m2lvl2curr = m2lvl1children.get(m2test2);
 						    if (m1lvl2curr.toString().equals(m2lvl2curr.toString()) && !m2lvl2matched.contains(m2test2)) {
 							ArrayList<matchTreeNode> m1lvl2children = m1lvl2curr.getChildren();
 							ArrayList<matchTreeNode> m2lvl2children = m2lvl2curr.getChildren();
 							System.out.println(m1lvl2curr.toString() + ": " + m1lvl2children + " " + m2lvl2curr.toString() + ": "  + m2lvl2children);
 							ArrayList<matchTreeNode> lvl1gchldresults = compareChildren(m1lvl2children, m2lvl2children);
 							if (lvl1gchldresults.size() == 0) {
 							    m2lvl2matched.add(m2test2);
 							    System.out.println("lvl2matched");
 							} 
 						    } 
 						    //System.out.println(m1parIndex + " " + m2lvl1children.size() + " " + m2lvl2matched.size());
 						    if (m2lvl2matched.size() == m2lvl1children.size()) {
 							m2lvl1matched.add(m2test1);	
 							System.out.println("lvl1matched");
 							break;
 						    }
 						}						
 					    }
 					}
 				    }
 				}				
 			    } // found children to be removed
 			    for (int m2scan = 0; m2scan < m2children1.size(); m2scan ++) {
 				System.out.println("m2scan: " + m2scan);
 				if (!m2lvl1matched.contains(m2scan)) {
 				    matchTreeNode temp = m2children1.get(m2scan);
 				    System.out.println("temp " + temp + " " + temp.tested + " " + m2scan);
 				    //System.out.println(m1parIndex + " " + temp);
 				    if (addedNodes.contains(temp.toString()) && temp.tested == false) {
 					//System.out.println("temp in added nodes " + m1children1 + " " + m2children1);
 					m2children1.remove(m2scan);
 					//System.out.println(m1parIndex + " " + m2parIndex + " removed " + temp + " from m2children1 " + m2children1);
 					ArrayList<matchTreeNode> lvl1retest = compareChildren(m1children1, m2children1);
 					//System.out.println(lvl1retest);
 					if (lvl1retest.size() == 0) {
 					    temp.tested = true;
 					    m2ChldrnMatched.add(m2scan);
 					    m2children1.add(temp);
 					    m2scan--;
 					    System.out.println("m2ChldrnMatched add up to " + m2ChldrnMatched.size());
 					    //break;
 					} else {
 					    temp.tested = true;
 					    m2children1.add(temp);
 					    m2ChldrnMatched.add(m2scan);
 					    System.out.println("m2ChldrnMatched add up to " + m2ChldrnMatched.size());
 					    m2scan--;
 					    System.out.println("fixed " + m2children1);
 					}
 				    }
 				} else if (m2children1.get(m2scan).tested == false) {
 				    m2ChldrnMatched.add(m2scan);
 				}
 			    }
 			} else if (m1children1.size() > m2children1.size()) {
 			    ArrayList<Integer> m2lvl1matched = new ArrayList<Integer>();
 			    ArrayList<Integer> m1lvl1matched = new ArrayList<Integer>();
 			    for (int m1test1 = 0; m1test1 < m1children1.size(); m1test1 ++) {
 				for (int m2test1 = 0; m2test1 < m2children1.size(); m2test1 ++) {
 				    matchTreeNode m1lvl1curr = m1children1.get(m1test1);
 				    matchTreeNode m2lvl1curr = m2children1.get(m2test1);
 				    if (m1lvl1curr.toString().equals(m2lvl1curr.toString()) && !m2lvl1matched.contains(m2test1)) {
 					ArrayList<matchTreeNode> m1lvl1children = m1lvl1curr.getChildren();
 					ArrayList<matchTreeNode> m2lvl1children = m2lvl1curr.getChildren();
 					ArrayList<matchTreeNode> lvl1chldresults = compareChildren(m1lvl1children, m2lvl1children);
 					if (lvl1chldresults.size() == 0) {
 					    ArrayList<Integer> m2lvl2matched = new ArrayList<Integer>();
 					    for (int m1test2 = 0; m1test2 < m1lvl1children.size(); m1test2 ++) {
 						for (int m2test2 = 0; m2test2 < m2lvl1children.size(); m2test2 ++) {
 						    matchTreeNode m1lvl2curr = m1lvl1children.get(m1test2);
 						    matchTreeNode m2lvl2curr = m2lvl1children.get(m2test2);
 						    if (m1lvl2curr.toString().equals(m2lvl2curr.toString()) && !m2lvl2matched.contains(m2test2)) {
 							ArrayList<matchTreeNode> m1lvl2children = m1lvl2curr.getChildren();
 							ArrayList<matchTreeNode> m2lvl2children = m2lvl2curr.getChildren();
 							ArrayList<matchTreeNode> lvl1gchldresults = compareChildren(m1lvl2children, m2lvl2children);
 							if (lvl1gchldresults.size() == 0) {
 							    m2lvl2matched.add(m2test2);
 							} else if (m2lvl2matched.size() == m2lvl2children.size()) {
 							    m2lvl1matched.add(m2test1);
 							    m1lvl1matched.add(m1test1);
 							    break;
 							}
 						    }
 						}
 					    }
 					}
 				    }
 				}				
 			    } // found children to be removed
 			    for (int m1scan = 0; m1scan < m1children1.size(); m1scan ++) {
 				if (!m1lvl1matched.contains(m1scan)) {
 				    matchTreeNode temp = m1children1.get(m1scan);
 				    if (rmvedNodes.contains(temp)) {
 					m1children1.remove(m1scan);
 					ArrayList<matchTreeNode> lvl1retest = compareChildren(m1children1, m2children1);
 					if (lvl1retest.size() == 0) {
 					    m2ChldrnMatched.add(m1scan);
 					} else {
 					    m1children1.add(temp);
 					}
 				    }
 				}
 			    }
 			} else if (m1children1.size() == m2children1.size()){
 			    ArrayList<Integer> m1lvl2matched = new ArrayList<Integer>();
 			    ArrayList<Integer> m2lvl2matched = new ArrayList<Integer>();
 			    ArrayList<Integer> m2lvl3matched = new ArrayList<Integer>();
 			    for (int m1lvl2 = 0; m1lvl2 < m1children1.size(); m1lvl2++) {
 				for (int m2lvl2 = 0; m2lvl2 < m2children1.size(); m2lvl2++) {
 				    matchTreeNode m1lvl2curr = m1children1.get(m1lvl2);
 				    matchTreeNode m2lvl2curr = m2children1.get(m2lvl2);
 				    if (m1lvl2curr.toString().equals(m2lvl2curr.toString()) && !m2lvl2matched.contains(m2lvl2)) {
 					ArrayList<matchTreeNode> m1lvl2children = m1lvl2curr.getChildren();
 					ArrayList<matchTreeNode> m2lvl2children = m2lvl2curr.getChildren();
 					ArrayList<matchTreeNode> lvl2compare = compareChildren(m1lvl2children, m2lvl2children);
 					if (lvl2compare.size() == 0) {
 					    for (int m1lvl3 = 0; m1lvl3 < m1lvl2children.size(); m1lvl3 ++) {
 						for (int m2lvl3 = 0; m2lvl3 < m2lvl2children.size(); m2lvl3 ++) {
 						    matchTreeNode m1lvl3curr = m1lvl2children.get(m1lvl3);
 						    matchTreeNode m2lvl3curr = m2lvl2children.get(m2lvl3);
 						    if (m1lvl3curr.toString().equals(m2lvl3curr.toString()) && !m2lvl3matched.contains(m2lvl3)) {
 							ArrayList<matchTreeNode> m1lvl3children = m1lvl3curr.getChildren();
 							ArrayList<matchTreeNode> m2lvl3children = m2lvl3curr.getChildren();
 							ArrayList<matchTreeNode> lvl3compare = compareChildren(m1lvl3children, m2lvl3children);
 							if (lvl3compare.size() == 0) {
 							    m2lvl3matched.add(m2lvl3);
 							} 
 							if (m2lvl3matched.size() == m2lvl2children.size()) {
 							    m1lvl2matched.add(m1lvl2);
 							    m2lvl2matched.add(m2lvl2);
 							}
 						    }
 						}
 					    }
 					}
 				    }
 				}
 			    } // matched children that can be matched
 			    System.out.println("m1: " + m1children1.size() + " m2: " + m2children1.size());
 			    matchTreeNode m1remove = null;
 			    matchTreeNode m2remove = null;
 			    for (int m1sc = 0; m1sc < m1children1.size(); m1sc++) {
 				if (!m1lvl2matched.contains(m1sc)) {
 				    //System.out.println("IN M1 REMOVE SECTION");
 				    m1remove = m1children1.get(m1sc);
 				    // System.out.println(rmvedNodes.contains(m1remove.toString()));
 				    if (rmvedNodes.contains(m1remove.toString())) {
 					m1children1.remove(m1remove);
 					System.out.println(" removed m1: " + m1children1.size() + " m2: " + m2children1.size());
 				    }
 				}
 			    }
 			    
 			    for (int m2sc = 0; m2sc < m2children1.size(); m2sc++) {
 				if (!m2lvl2matched.contains(m2sc)) {
 				    m2remove = m2children1.get(m2sc);
 				    if (addedNodes.contains(m2remove.toString())) {
 					m2children1.remove(m2sc);
					System.out.println("removed m1: " + m1children1.size() + " removed m2: " + m2children1.size());
 					ArrayList<matchTreeNode> retest = compareChildren(m1children1, m2children1);
 					if (retest.size() == 0) {
 					    m2ChldrnMatched.add(m2sc);
 					    System.out.println("m2ChldrnMatched added to");
 					    m1children1.add(m1remove);
 					    m2children1.add(m2remove);
 					} else {
					    m1children1.add(m1remove);
 					    m2children1.add(m2remove);
 					    System.out.println("fixed m1: " + m1children1.size() + " fixed m2: " + m2children1.size());
 					}
 				    } 
 				} else {
 				    m2ChldrnMatched.add(m2sc);
 				}
 			    }
 			  
 			}
 			System.out.println(m1parIndex + " " + m2parIndex + " " + m2ChldrnMatched.size() + " " + m2children1.size());
 			if (m2ChldrnMatched.size() == m2children1.size()) {
 			    System.out.println("match!!!");
 			    int index = m1parIndex + 1;
 			    int ind = m2parIndex + 1;
 			    mapping.put(index, ind);
 			    m2visited.add(m2parIndex);
 			    break;
 			}
 			
 		    } else {
 			int index = m1parIndex + 1;
 			int ind = m2parIndex + 1;
 			boolean treematch = false;
 			ArrayList<Integer> m2childrenmatched = new ArrayList<Integer>();
 			for (int lvl2 = 0; lvl2 < m1children1.size(); lvl2++) {
 			    for (int m2lvl2 = 0; m2lvl2 < m2children1.size(); m2lvl2++) {
 				if (!m2childrenmatched.contains(m2lvl2)) {
 				    ArrayList<matchTreeNode> lvl2results = compareChildren(m1children1.get(lvl2).getChildren(), m2children1.get(m2lvl2).getChildren());
 				    if (lvl2results.size() != 0) {
 					// compare with removal tests
 					ArrayList<matchTreeNode> m1level2 = m1children1.get(lvl2).getChildren();
 					ArrayList<matchTreeNode> m2level2 = m2children1.get(m2lvl2).getChildren();
 					if (m1level2.size() < m2level2.size()) {
 					    ArrayList<Integer> m2lvl2matched = new ArrayList<Integer>();
 					    for (int m1test2 = 0; m1test2 < m1level2.size(); m1test2++) {
 						for (int m2test2 = 0; m2test2 < m2level2.size(); m2test2++) {
 						    matchTreeNode m1lvl2curr = m1level2.get(m1test2);
 						    matchTreeNode m2lvl2curr = m2level2.get(m2test2);
 						    if (m1lvl2curr.toString().equals(m2lvl2curr.toString()) && !m2lvl2matched.contains(m2test2)) {
 							ArrayList<matchTreeNode> m1lvl2currChildren = m1lvl2curr.getChildren();
 							ArrayList<matchTreeNode> m2lvl2currChildren = m2lvl2curr.getChildren();
 							ArrayList<matchTreeNode> lvl2chldresults = compareChildren(m1lvl2currChildren, m2lvl2currChildren);
 							if (lvl2chldresults.size() == 0) {
 							    m2lvl2matched.add(m2test2);
 							    break;
 							}
 						    }
 						}
 					    } // found children to remove
 					    for (int m2scan = 0; m2scan < m2level2.size(); m2scan++) {
 						if (!m2lvl2matched.contains(m2scan)) {
 						    matchTreeNode temp = m2level2.get(m2scan);
 						    if (addedNodes.contains(temp)) {
 							m2level2.remove(m2scan);
 							ArrayList<matchTreeNode> lvl2restest = compareChildren(m1level2, m2level2);
 							if (lvl2restest.size() == 0) {
 							    m2childrenmatched.add(m2lvl2);
 							} else {
 							    m2level2.add(temp);
 							}
 						    }
 						}
 					    } // tried removing children
 					} else if (m1level2.size() > m2level2.size()) {
 					    ArrayList<Integer> m1lvl2matched = new ArrayList<Integer>();
 					    ArrayList<Integer> m2lvl2matched = new ArrayList<Integer>();
 					    for (int m1test2 = 0; m1test2 < m1level2.size(); m1test2++) {
 						for (int m2test2 = 0; m2test2 < m2level2.size(); m2test2++) {
 						    matchTreeNode m1lvl2curr = m1level2.get(m1test2);
 						    matchTreeNode m2lvl2curr = m2level2.get(m2test2);
 						    if (m1lvl2curr.toString().equals(m2lvl2curr.toString()) && !m2lvl2matched.contains(m2test2)) {
 							ArrayList<matchTreeNode> m1lvl2currChildren = m1lvl2curr.getChildren();
 							ArrayList<matchTreeNode> m2lvl2currChildren = m2lvl2curr.getChildren();
 							ArrayList<matchTreeNode> lvl2chldresults = compareChildren(m1lvl2currChildren, m2lvl2currChildren);
 							if (lvl2chldresults.size() == 0) {
 							    m2lvl2matched.add(m2test2);
 							    m1lvl2matched.add(m1test2);
 							    break;
 							}
 						    }
 						}
 					    } // found children to remove
 					    for (int m1scan = 0; m1scan < m1level2.size(); m1scan++) {
 						if (!m1lvl2matched.contains(m1scan)) {
 						    matchTreeNode temp = m1level2.get(m1scan);
 						    if (rmvedNodes.contains(temp)) {
 							m1level2.remove(m1scan);
 							ArrayList<matchTreeNode> lvl2retest = compareChildren(m1level2, m2level2);
 							if (lvl2retest.size() == 0) {
 							    m2childrenmatched.add(m2lvl2);
 							} else {
 							    m1level2.add(temp);
 							}
 						    }
 						}
 					    }
 					} // tried removing children
 				    } else if (lvl2results.size() == 0) {
 					ArrayList<matchTreeNode> m1level3 = m1children1.get(lvl2).getChildren();
 					ArrayList<matchTreeNode> m2level3 = m2children1.get(m2lvl2).getChildren();
 					ArrayList<matchTreeNode> lvl3results = compareChildren(m1level3, m2level3);
 					if (lvl3results.size() == 0) {
 					    m2childrenmatched.add(m2lvl2);
 					} else if (lvl3results.size() != 0) {
 					    // compare with removal test
 					    if (m1level3.size() < m2level3.size()) {
 						ArrayList<Integer> m2lvl3matched = new ArrayList<Integer>();
 						for (int lvl3 = 0; lvl3 < m1level3.size(); lvl3++) {
 						    for (int m2lvl3 = 0; m2lvl3 < m2level3.size(); m2lvl3++) {
 							matchTreeNode m1lvl3curr = m1level3.get(lvl3);
 							matchTreeNode m2lvl3curr = m2level3.get(m2lvl3);
 							if (m1lvl3curr.toString().equals(m2lvl3curr.toString()) && !m2lvl3matched.contains(m2lvl3)) {
 							    m2lvl3matched.add(m2lvl3);
 							    break;
 							}
 						    }
 						}
 						for (int m2test = 0; m2test < m2level3.size(); m2test++) {
 						    if (!m2lvl3matched.contains(m2test)) {
 							matchTreeNode temp = m2level3.get(m2test);
 							if (addedNodes.contains(temp)) {
 							    m2level3.remove(m2test);
 							    ArrayList<matchTreeNode> lvl3retest = compareChildren(m1level3, m2level3);
 							    if (lvl3retest.size() == 0) {
 								m2childrenmatched.add(m2lvl2);
 							    } else {
 								m2level3.add(temp);
 							    }
 							}
 						    }
 						}
 					    } else if (m1level3.size() > m2level3.size()) {
 						ArrayList<Integer> m1lvl3matched = new ArrayList<Integer>();
 						ArrayList<Integer> m2lvl3matched = new ArrayList<Integer>();
 						for (int lvl3 = 0; lvl3 < m1level3.size(); lvl3++) {
 						    for (int m2lvl3 = 0; m2lvl3 < m2level3.size(); m2lvl3++) {
 							matchTreeNode m1lvl3curr = m1level3.get(lvl3);
 							matchTreeNode m2lvl3curr = m2level3.get(m2lvl3);
 							if (m1lvl3curr.toString().equals(m2lvl3curr.toString()) && !m2lvl3matched.contains(m2lvl3)) {
 							    m1lvl3matched.add(lvl3);
 							    m2lvl3matched.add(m2lvl3);
 							    break;
 							}
 						    }
 						}
 						for (int m1test = 0; m1test < m1level3.size(); m1test++) {
 						    if (!m1lvl3matched.contains(m1test)) {
 							matchTreeNode temp = m1level3.get(m1test);
 							if (rmvedNodes.contains(temp)) {
 							    m1level3.remove(m1test);
 							    ArrayList<matchTreeNode> lvl3retest = compareChildren(m1level3, m2level3);
 							    if (lvl3retest.size() == 0) {
 								m2childrenmatched.add(m2lvl2);
 							    } else {
 								m1level3.add(temp);
 							    }
 							}
 						    }
 						}
 					    }
 					}
 				    } else if (m2childrenmatched.size() == m2children1.size()) {
 					treematch = true;
 				    } else {
 					continue;
 				    }
 				}
 			    } 
 			    
 			} if (treematch == true) {
 			    mapping.put(index, ind);
 			    m2visited.add(m2parIndex);
 			    break;
 			} else {
 			    mapping.put(index, ind);
 			    m2visited.add(m2parIndex);
 			    break;
 			}
 		    }
 		}
 	    }
 	}
 	System.out.println(mapping.keySet().size());
 	for (int m1atom : mapping.keySet()) {
 	    System.out.println("reactant atom " + m1atom + " maps to " + mapping.get(m1atom) + " in the product");
 	}
 	
     } // close matchAtoms method
 
    
 
     public ArrayList<matchTreeNode> compareChildren(ArrayList<matchTreeNode> m1, ArrayList<matchTreeNode> m2) {
 	ArrayList<matchTreeNode> unMatchedNodes = new ArrayList<matchTreeNode>();
 	ArrayList<Integer> m2visited = new ArrayList<Integer>();
 	
 	for (int i=0; i<m1.size(); i++) {
 	    String temp = m1.get(i).toString();
 	    for (int j=0; j<m2.size(); j++) {
 		if (temp.equals(m2.get(j).toString()) && !m2visited.contains(j)) {
 		    m2visited.add(j);
 		    break;
 		} else {
 		    continue;
 		}
 	    }
 	}
 	for (int k=0; k<m2.size(); k++) {
 	    if (!m2visited.contains(k)) {
 		unMatchedNodes.add(m2.get(k));
 	    }
 	}
 	return unMatchedNodes;
     } // close compareChildren method
 }
