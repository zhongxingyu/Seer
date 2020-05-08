 /***
     Version 7 of reactions class, changing reaction creation to a single mol file, Once working version 8 will continue with changes to rest of code. 
 ***/
 import java.io.*;
 import java.util.*;
 
public class Reactionsv7B {
     /***
 	Molecule representing mol file of all reactants for reaction
     */
     public Molecule reactants;
     /***
 	Molecule representing mol file of all products for reaction
     */
     public Molecule products;
     /***
 	Main method creating reaction and running matching algorithm / signature creation algorithm
     */
     public static void main(String[] args) throws FileNotFoundException {
	Reactionsv7B reactions = new Reactionsv7B();
 	// create reaction by scanning mol file for data to make reactant and product Molecule objects.
 	reactions.createReaction(args);
 	Molecule reactant = reactions.reactants;
 	System.out.print("Reactant atoms: ");
 	for (int reactAtom = 0; reactAtom < reactant.getAtoms().size(); reactAtom ++) {
 	    System.out.print(reactant.getAtoms().get(reactAtom) + " ");
 	}
 	System.out.println();
 	reactant.generateFreq();
 	int [] freq = reactant.getFreqs();
 	System.out.print("# Bonds per Atom: ");
 	for (int fq = 0; fq < freq.length; fq++) {
 	    System.out.print(freq[fq] + " ");
 	}
 	System.out.println();
 	System.out.println("# Molecules in reactant = " + reactant.findNumOfMlcs());
 	// Get and print # of bonds of each type in the reactants
 	HashMap<String, Integer> bondTypes = reactant.listChangedBonds();
 	Set<String> bondPics = bondTypes.keySet();
 	for (String s : bondPics) {
 	    System.out.println(s + " " + bondTypes.get(s));
 	}
 	reactant.buildMatchTrees();
 	int atomNumCount = 1;
 	// Print atom match Trees as strings for each possible path.
 	for (ArrayList<String> atomTree : reactant.getMatchTrees()) {
 	    System.out.print(atomNumCount + ": ");
 	    for (String s : atomTree) {
 		System.out.print(s + ", ");
 	    }
 	    System.out.println();
 	    atomNumCount++;
 	}
 	// end reactant information begin product
 	Molecule product = reactions.products;
 	System.out.print("Product atoms: ");
 	for (int prodAtom = 0; prodAtom < product.getAtoms().size(); prodAtom++) {
 	    System.out.print(product.getAtoms().get(prodAtom) + " ");
 	}
 	System.out.println();
 	product.generateFreq();
 	int [] pfreqs = product.getFreqs();
 	System.out.print("# Bonds per Atom: ");
 	for (int pfq = 0; pfq < pfreqs.length; pfq++) {
 	    System.out.print(pfreqs[pfq] + " ");
 	}
 	System.out.println();
 	System.out.println("# Molecules in product = " + product.findNumOfMlcs());
 	// Get and print # of bonds of each type in products
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
 	    patomNumCount++;
 	}
 	reactions.matchAtoms(reactant, product);
     } // close main method
     /***
 	createReaction method takes a mol file and converts it into reactant and product molecule objects
     */
     public void createReaction(String[] args) throws FileNotFoundException {
 	File ReactionInput;
 	Scanner inputSc;
 	if (args.length != 1) {
 	    System.out.println("Wrong Number of arguments, please enter 1 filename for the reaction");
 	    System.exit(0);
 	}
 	ReactionInput = new File(args[0]);
 	inputSc = new Scanner(ReactionInput);
 	createMolecules(inputSc);
 	// Print data for testing purposes
 	System.out.println("Reactant # of atoms = " + reactants.getNumberOfAtoms());
 	System.out.println("Reactant # of bonds = " + reactants.getNumberOfBonds());
 	System.out.println("Reactant atoms      = " + reactants.getAtoms());
 	System.out.println("Reactant bonds      = " + reactants.getBonds());
 	System.out.println("Product # of atoms = " + products.getNumberOfAtoms());
 	System.out.println("Product # of bonds = " + products.getNumberOfBonds());
 	System.out.println("Product atoms      = " + products.getAtoms());
 	System.out.println("Product bonds      = " + products.getBonds());
     } // close createReaction method
     /***
 	createMolecules method take scanner on input file and makes appropriate reactant and product molecule data for the reaction based on mol files
     */
     public void createMolecules(Scanner molsc) {
 	// Get Reactants
 	System.out.println("In Create Molecules Method");
 	ArrayList<String> rbonds = new ArrayList<String>();
 	ArrayList<String> ratoms = new ArrayList<String>();
 	ArrayList<String> rcharges = new ArrayList<String>();
 	String info = "";
 	String trash = molsc.nextLine();
 	Scanner trashsc = new Scanner(trash);
 	Boolean reactantStart = false;
 	Boolean reactantDataStart = false;
 	while(reactantStart == false) {
 	    String next = "";
 	    if (trashsc.hasNext()) {
 		next = trashsc.next();
 	    }
 	    if (next.equals("$MOL")) {
 		reactantStart = true;
 	    } else {
 		trash = molsc.nextLine();
 		trashsc = new Scanner(trash);
 	    }
 	}
 	while(reactantDataStart == false) {
 	    if (trashsc.hasNextInt()) {
 		info = trash;
 		reactantDataStart = true;
 	    } else {
 		trash = molsc.nextLine();
 		trashsc = new Scanner(trash);
 	    }
 	}
 	System.out.println("Begin Gathering Data");
 	Scanner infosc = new Scanner(info);
 	int numberOfAtoms = infosc.nextInt();
 	int numberOfBonds = infosc.nextInt();
 	String temp = molsc.nextLine();
 	while (temp.charAt(2) == ' ') {
 	    Scanner tempsc = new Scanner(temp);
 	    String atom = tempsc.next();
 	    atom = tempsc.next();
 	    atom = tempsc.next();
 	    atom = tempsc.next();
 	    ratoms.add(atom);
 	    temp = molsc.nextLine();
 	}
 	System.out.println("After Gathering ratoms");
 	Scanner bondsc = new Scanner(temp);
 	while(bondsc.hasNextInt()) {
 	    String bond = "" + bondsc.nextInt() + " " + bondsc.nextInt() + " " + bondsc.nextInt() + " " + bondsc.nextInt();
 	    rbonds.add(bond);
 	    temp = molsc.nextLine();
 	    bondsc = new Scanner(temp);
 	}
 	System.out.println("After Gathering rbonds");
 	Scanner chargeSc = new Scanner(temp);
 	String chargeNext = "";
    	if (chargeSc.hasNext()) {
 	     chargeNext = chargeSc.next();
 	}
 	while(chargeNext.equals("M")) {
 	    String code = "";
 	    code = chargeSc.next();
 	    System.out.println("code = " + code);
 	    if (code.equals("CHG")) {
 		String charge = "" + chargeSc.nextInt() + " " + chargeSc.nextInt() + " " + chargeSc.nextInt();
 		rcharges.add(charge);
 		System.out.println("charge = " + charge);
 	    } else {
 		break;
 	    }
 	    chargeSc = new Scanner(molsc.nextLine());    
 	}
 	System.out.println("After getting rcharges");
 	reactants = new Molecule(ratoms, numberOfBonds, numberOfAtoms, rbonds, rcharges);
 	// Get Products
 	ArrayList<String> pbonds = new ArrayList<String>();
 	ArrayList<String> patoms = new ArrayList<String>();
 	ArrayList<String> pcharges = new ArrayList<String>();
 	info = "";
 	trash = temp;
 	trashsc = new Scanner(trash);
 	Boolean productStart = false;
 	Boolean productDataStart = false;
 	while(productStart == false) {
 	    String next = trashsc.next();
 	    if (next.equals("$MOL")) {
 		productStart = true;
 	    } else {
 		trash = molsc.nextLine();
 		trashsc = new Scanner(trash);
 	    }
 	}
 	while(productDataStart == false) {
 	    if (trashsc.hasNextInt()) {
 		info = trash;
 		productDataStart = true;
 	    } else {
 		trash = molsc.nextLine();
 		trashsc = new Scanner(trash);
 	    }
 	}
 	infosc = new Scanner(info);
 	int pnumberOfAtoms = infosc.nextInt();
 	int pnumberOfBonds = infosc.nextInt();
 	temp = molsc.nextLine();
 	while (temp.charAt(2) == ' ') {
 	    Scanner tempsc = new Scanner(temp);
 	    String atom = tempsc.next();
 	    atom = tempsc.next();
 	    atom = tempsc.next();
 	    atom = tempsc.next();
 	    patoms.add(atom);
 	    temp = molsc.nextLine();
 	}
 	bondsc = new Scanner(temp);
 	while(bondsc.hasNextInt()) {
 	    String bond = "" + bondsc.nextInt() + " " + bondsc.nextInt() + " " + bondsc.nextInt() + " " + bondsc.nextInt();
 	    pbonds.add(bond);
 	    temp = molsc.nextLine();
 	    bondsc = new Scanner(temp);
 	}
 	Scanner pchargeSc = new Scanner(temp);
 	String pchargeNext = "";
    	if (pchargeSc.hasNext()) {
 	     pchargeNext = pchargeSc.next();
 	}
 	while(pchargeNext.equals("M")) {
 	    String code = "";
 	    code = pchargeSc.next();
 	    System.out.println("code = " + code);
 	    if (code.equals("CHG")) {
 		String charge = "" + pchargeSc.nextInt() + " " + pchargeSc.nextInt() + " " + pchargeSc.nextInt();
 		pcharges.add(charge);
 		System.out.println("charge = " + charge);
 	    } else {
 		break;
 	    }
 	    pchargeSc = new Scanner(molsc.nextLine());    
 	}
 	products = new Molecule(patoms, pnumberOfBonds, pnumberOfAtoms, pbonds, pcharges);
     } // close createMolecules method
     /***
 	matchAtoms Method compares reactants and products to find out the most likely bonds made / broken and then uses those to match atom numbers from reactants to products. 
     */
     public void matchAtoms(Molecule m1, Molecule m2) {
 	HashMap<Integer, Integer> mapping = new HashMap<Integer, Integer>();
 	int m1AtomNum = 0;
 	int m2AtomNum = 0;
 	int rootMatches = 0;
 	HashMap<String, Integer> m1BondTypes = m1.listChangedBonds();
 	HashMap<String, Integer> m2BondTypes = m2.listChangedBonds();
 	System.out.println("m1 List Changed Bonds: " + m1.listChangedBonds());
 	System.out.println("m2 List Changed Bonds: " + m2.listChangedBonds());
 	Set<String> m1Keys = m1BondTypes.keySet();
 	HashMap<String, ArrayList<String>> changedBonds = new HashMap<String, ArrayList<String>>();
 	changedBonds.put("+", new ArrayList<String>());
 	changedBonds.put("-", new ArrayList<String>());
 	//Create List Of Bonds Added or Removed
 	boolean firstThrough = false;
 	for (String key : m1Keys) {
 	    if (!m2BondTypes.containsKey(key) && !changedBonds.get("-").contains(key)) {
 		changedBonds.get("-").add(key);
 	    }
 	    for (String key2 : m2BondTypes.keySet()) {
 		if (!m1BondTypes.containsKey(key2) && !changedBonds.get("+").contains(key2)) {
 		    changedBonds.get("+").add(key2);
 		}
 		int key1Freq = m1BondTypes.get(key);
 		int key2Freq = m2BondTypes.get(key2);
 		if (key1Freq - key2Freq > 0) {
 		    changedBonds.get("-").add(key);
 		} else if (key1Freq - key2Freq < 0) {
 		    changedBonds.get("+").add(key2);
 		} 
 	    }
 	}
 	ArrayList<String> rmvedNodes = new ArrayList<String>();
 	ArrayList<String> addedNodes = new ArrayList<String>();
 	System.out.println("+ " + changedBonds.get("+"));
 	System.out.println("- " + changedBonds.get("-"));
 	for (String add : changedBonds.get("+")) {
 	    Scanner bondsc = new Scanner(add);
 	    String parentAtom = bondsc.next();
 	    String bondtype = bondsc.next();
 	    String bondedAtom = bondsc.next();
 	    if (bondtype.equals("-")) {
 		addedNodes.add(bondedAtom + "1");
 		addedNodes.add(parentAtom + "1");
 	    } else if (bondtype.equals("=")) {
 		addedNodes.add(bondedAtom + "2");
 		addedNodes.add(parentAtom + "2");
 	    } else {
 		addedNodes.add(bondedAtom + "3");
 		addedNodes.add(parentAtom + "3");
 	    }
 	}
 	for (String lose : changedBonds.get("-")) {
 	    Scanner bondsc = new Scanner(lose);
 	    String parentAtom = bondsc.next();
 	    String bondtype = bondsc.next();
 	    String bondedAtom = bondsc.next();
 	    if (bondtype.equals("-")) {
 		rmvedNodes.add(bondedAtom + "1");
 		rmvedNodes.add(parentAtom + "1");
 	    } else if (bondtype.equals("=")) {
 		rmvedNodes.add(bondedAtom + "2");
 		rmvedNodes.add(parentAtom + "2");
 	    } else {
 		rmvedNodes.add(bondedAtom + "3");
 		rmvedNodes.add(parentAtom + "3");
 	    }
 	}
 	System.out.println("Added Nodes: " + addedNodes);
 	System.out.println("Lost Nodes: " + rmvedNodes);
 	// Finish added/rmved Node section
 	// Start Comparison for matching
 	HashMap<Integer, Integer> atomMap = new HashMap<Integer, Integer>();
 	ArrayList<matchTreeNode> m1parents = m1.getatomTrees();
 	ArrayList<matchTreeNode> m2parents = m2.getatomTrees();
 	ArrayList<Integer> m2visited = new ArrayList<Integer>();
 	ArrayList<Integer> m1eqMatch = new ArrayList<Integer>();
 	ArrayList<Integer> m2eqMatch = new ArrayList<Integer>();
 	for (int m1lvl1 = 0; m1lvl1 < m1parents.size(); m1lvl1++) {
 	    m2loop:
 	    for (int m2lvl1 = 0; m2lvl1 < m2parents.size(); m2lvl1++) {
 		matchTreeNode m1root = m1parents.get(m1lvl1);
 		matchTreeNode m2root = m2parents.get(m2lvl1);
 		if (m1root.toString().equals(m2root.toString()) && !m2eqMatch.contains(m2lvl1)) {
 		    ArrayList<matchTreeNode> m1children = m1root.getChildren();
 		    ArrayList<matchTreeNode> m2children = m2root.getChildren();
 		    //System.out.println(m1lvl1 + " " + m2lvl1 + " " + m1children + " " + m2children);
 		    ArrayList<matchTreeNode> childrenTest = compareChildren(m1children, m2children);
 		    boolean ctest = childrenTest.size() == 0;
 		    //System.out.println("children test = " + ctest);
 		    if (childrenTest.size() == 0) {
 			ArrayList<Integer> m2childrenMatched = new ArrayList<Integer>();
 			for (int m1lvl2 = 0; m1lvl2 < m1children.size(); m1lvl2++) {
 			    for (int m2lvl2 = 0; m2lvl2 < m2children.size(); m2lvl2++) {
 				matchTreeNode m1lvl2curr = m1children.get(m1lvl2);
 				matchTreeNode m2lvl2curr = m2children.get(m2lvl2);
 				if (m1lvl2curr.toString().equals(m2lvl2curr.toString()) && !m2childrenMatched.contains(m2lvl2)) {
 				    ArrayList<matchTreeNode> m1gchildren = m1lvl2curr.getChildren();
 				    ArrayList<matchTreeNode> m2gchildren = m2lvl2curr.getChildren();
 				    //System.out.println(m1lvl1 + " " + m2lvl1 + " " + m1gchildren + " " + m2gchildren);
 				    ArrayList<matchTreeNode> gchildrenTest = compareChildren(m1gchildren, m2gchildren);
 				    if (gchildrenTest.size() == 0) {
 					if (m1gchildren.size() == 0 && m2gchildren.size() == 0) {
 					    m2childrenMatched.add(m2lvl2);
 					    //System.out.println(m1lvl1 + " " + m2lvl1 + " " + m2childrenMatched.size() + " " + m2children.size());
 					    if (m2childrenMatched.size() == m2children.size()) {
 						int i = m1lvl1 + 1;
 						int i2 = m2lvl1 + 1;
 						mapping.put(i, i2);
 						m1eqMatch.add(m1lvl1);
 						m2eqMatch.add(m2lvl1);
 						break m2loop;
 					    }
 					}
 					ArrayList<Integer> m2gchildrenMatched = new ArrayList<Integer>();
 					for (int m1lvl3 = 0; m1lvl3 < m1gchildren.size(); m1lvl3 ++) {
 					    for (int m2lvl3 = 0; m2lvl3 < m2gchildren.size(); m2lvl3++) {
 						matchTreeNode m1lvl3curr = m1gchildren.get(m1lvl3);
 						matchTreeNode m2lvl3curr = m2gchildren.get(m2lvl3);
 						if (m1lvl3curr.toString().equals(m2lvl3curr.toString()) && !m2gchildrenMatched.contains(m2lvl3)) {
 						    ArrayList<matchTreeNode> m1ggchildren = m1lvl3curr.getChildren();
 						    ArrayList<matchTreeNode> m2ggchildren = m2lvl3curr.getChildren();
 						    ArrayList<matchTreeNode> ggchildrenTest = compareChildren(m1ggchildren, m2ggchildren);
 						    if (ggchildrenTest.size() == 0) {
 							m2gchildrenMatched.add(m2lvl3);
 							if (m2gchildrenMatched.size() == m2gchildren.size()) {
 							    m2childrenMatched.add(m2lvl2);
 							    if (m2childrenMatched.size() == m2children.size()) {
 								int index = m1lvl1 + 1;
 								int index2 = m2lvl1 + 1;
 								mapping.put(index, index2);
 								m1eqMatch.add(m1lvl1);
 								m2eqMatch.add(m2lvl1);
 								break m2loop;
 							    }							    
 							}
 						    } 
 						} 
 					    }
 					} 
 				    }
 				} 
 			    }
 			}
 		    } 
 		}
 	    }
 	} // eq test
 	for (int m1atom : mapping.keySet()) {
 	    System.out.println("reactant atom " + m1atom + " maps to " + mapping.get(m1atom) + " in the product");
 	}
 	System.out.println();
 	if (addedNodes.size() == 0 && rmvedNodes.size() == 0) {
 	    // match testing
 	    for (int m1parIndex = 0; m1parIndex < m1parents.size(); m1parIndex++) {
 		matchTreeNode reactantTest = m1parents.get(m1parIndex);
 		ArrayList<Integer> m1bonds = reactantTest.getAtomBonds();
 		int atomNum = reactantTest.getAtomNumber();
 		//System.out.println("Reactant atom " + atomNum + " is bonded to " + m1bonds);
 		ArrayList<Integer> possible = new ArrayList<Integer>();
 		for (int i=0; i<m1bonds.size(); i++){
 		    if (mapping.keySet().contains(m1bonds.get(i)) && !mapping.keySet().contains(atomNum)) {
 			int bondedAtom = mapping.get(m1bonds.get(i));
 			int atomNum2 = m2parents.get(bondedAtom).getAtomNumber()-1;
 			//System.out.println("Product atom " + atomNum2 + " is bonded to " + m2parents.get(atomNum2-1).getAtomBonds() + " is from " + m1bonds.get(i));
 			ArrayList<Integer> m2bonds = m2parents.get(atomNum2-1).getAtomBonds();
 			for (Integer k : m2bonds) {
 			    possible.add(k);
 			}
 			for (int y = 0; y < possible.size(); y ++) {
 			    int j = possible.get(y);
 			    if (m2eqMatch.contains(j-1)) {
 				//System.out.println(j-1 + " " + m2eqMatch);
 				possible.remove(y);
 				y --;
 			    }
 			}
 			if (possible.size() == 1) {
 			    //System.out.println("match");
 			    mapping.put(atomNum, possible.get(0));
 			    m1eqMatch.add(atomNum-1);
 			    m2eqMatch.add(possible.get(0)-1);
 			} if (possible.size() > 1) {
 			    for (int q = 0; q < possible.size(); q++) {
 				//System.out.println(reactantTest + " " + m2parents.get(possible.get(q)-1) + " " + possible.get(q));
 				if (reactantTest.toString().equals(m2parents.get(possible.get(q)-1).toString())) {
 				    mapping.put(atomNum, possible.get(q));
 				    m1eqMatch.add(atomNum - 1);
 				    m2eqMatch.add(possible.get(q)-1);
 				}
 			    }
 			}
 		    }
 		}
 	    }
 	    /*
 	      System.out.println();
 	      for (int m1atom : mapping.keySet()) {
 	      System.out.println("reactant atom " + m1atom + " maps to " + mapping.get(m1atom) + " in the product");
 	      }
 	      System.out.println(m1eqMatch);
 	      System.out.println(m2eqMatch);
 	    */
 	    for (int m1parIndex = 0; m1parIndex < m1parents.size(); m1parIndex++) {
 		matchTreeNode reactantTest = m1parents.get(m1parIndex);
 		ArrayList<Integer> m1bonds = reactantTest.getAtomBonds();
 		int atomNum = reactantTest.getAtomNumber();
 		
 		//System.out.println("Reactant atom " + atomNum + " is bonded to " + m1bonds);
 		ArrayList<Integer> possible = new ArrayList<Integer>();
 		for (int i=0; i<m1bonds.size(); i++){
 		    
 		    if (mapping.keySet().contains(m1bonds.get(i)) && !mapping.keySet().contains(atomNum)) {
 			int bondedAtom = mapping.get(m1bonds.get(i));
 			int atomNum2 = m2parents.get(bondedAtom).getAtomNumber()-1;
 			//System.out.println("Product atom " + atomNum2 + " is bonded to " + m2parents.get(atomNum2-1).getAtomBonds() + " is from " + m1bonds.get(i));
 			ArrayList<Integer> m2bonds = m2parents.get(atomNum2-1).getAtomBonds();
 			for (Integer k : m2bonds) {
 			    possible.add(k);
 			}
 			for (int y = 0; y < possible.size(); y ++) {
 			    int j = possible.get(y);
 			    if (m2eqMatch.contains(j-1)) {
 				//System.out.println(j-1 + " " + m2eqMatch);
 				possible.remove(y);
 				y --;
 			    }
 			}
 			System.out.println(possible.size() + " " + possible);
 			if (possible.size() == 1) {
 			    int matchNum = possible.get(0);
 			    ArrayList<Integer> matchBonds = m2parents.get(matchNum-1).getAtomBonds();
 			    if (reactantTest.toString().equals(m2parents.get(matchNum-1).toString())) {
 				//System.out.println("match");
 				System.out.println("MATCHING " + atomNum  + " & " + possible.get(0) + "!!!!!");
 				mapping.put(atomNum, possible.get(0));
 				m1eqMatch.add(atomNum-1);
 				m2eqMatch.add(possible.get(0)-1);
 			    }
 			}
 			if (possible.size() > 1) {
 			    for (int q = 0; q < possible.size(); q++) {
 				//System.out.println(reactantTest + " " + m2parents.get(possible.get(q)-1) + " " + possible.get(q));
 				if (reactantTest.toString().equals(m2parents.get(possible.get(q)-1).toString())) {
 				    System.out.println("MATCHING " + atomNum  + " & " + possible.get(q) + "!!!!!");
 				    mapping.put(atomNum, possible.get(q));
 				    m1eqMatch.add(atomNum - 1);
 				    m2eqMatch.add(possible.get(q)-1);
 				    break;
 				}
 			    }
 		    }
 		    }
 		}
 	    }
 	}
 	//System.out.println();
 	//System.out.println();
 	for (int m1atom : mapping.keySet()) {
 	    System.out.println("reactant atom " + m1atom + " maps to " + mapping.get(m1atom) + " in the product");
 	}
 	System.out.println("Start removal testing");
 	if (addedNodes.size() == 0 && rmvedNodes.size() == 0) {
 	    //addedNodes.add("C1");
 	    //rmvedNodes.add("C1");
 	}
 	
 	// compare with removal
 	for (int m1parIndex = 0; m1parIndex < m1parents.size(); m1parIndex++) {
 	    matchTreeNode m1curr = m1parents.get(m1parIndex);
 	    for (int m2parIndex = 0; m2parIndex < m2parents.size(); m2parIndex++) {
 		matchTreeNode m2curr = m2parents.get(m2parIndex);
 		if (m1curr.toString().equals(m2curr.toString()) && !m2visited.contains(m2parIndex) && !m2eqMatch.contains(m2parIndex) && !m1eqMatch.contains(m1parIndex)) {
 		    ArrayList<matchTreeNode> m1children1 = m1curr.getChildren();
 		    ArrayList<matchTreeNode> m2children1 = m2curr.getChildren();
 		    ArrayList<matchTreeNode> match = compareChildren(m1children1, m2children1);
 		    if (match.size() != 0) {
 			ArrayList<Integer> m2ChldrnMatched = new ArrayList<Integer>();
 			// insert removal comparisons
 			ArrayList<Integer> m2lvl1matched = new ArrayList<Integer>();
 			ArrayList<Integer> m1lvl1matched = new ArrayList<Integer>();
 			for (int m1test1 = 0; m1test1 < m1children1.size(); m1test1 ++) {
 			    for (int m2test1 = 0; m2test1 < m2children1.size(); m2test1 ++) {
 				matchTreeNode m1lvl1curr = m1children1.get(m1test1);
 				matchTreeNode m2lvl1curr = m2children1.get(m2test1);
 				if (m1lvl1curr.toString().equals(m2lvl1curr.toString()) && !m2lvl1matched.contains(m2test1)) {
 				    ArrayList<matchTreeNode> m1lvl1children = m1lvl1curr.getChildren();
 				    ArrayList<matchTreeNode> m2lvl1children = m2lvl1curr.getChildren();
 				    //System.out.println(m1parIndex + " " + m1lvl1children + " " + m2lvl1children);
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
 						    //System.out.println(m1lvl2curr.toString() + ": " + m1lvl2children + " " + m2lvl2curr.toString() + ": "  + m2lvl2children);
 						    ArrayList<matchTreeNode> lvl1gchldresults = compareChildren(m1lvl2children, m2lvl2children);
 						    if (lvl1gchldresults.size() == 0) {
 							m2lvl2matched.add(m2test2);
 							m1lvl1matched.add(m1test1);
 							//System.out.println("lvl2matched");
 						    } 
 						} 
 						//System.out.println(m1parIndex + " " + m2lvl1children.size() + " " + m2lvl2matched.size());
 						if (m2lvl2matched.size() == m2lvl1children.size()) {
 						    m2lvl1matched.add(m2test1);	
 						    m1lvl1matched.add(m1test1);
 						    //System.out.println("lvl1matched");
 						    break;
 						}
 					    }						
 					}
 				    }
 				}
 			    }				
 			} // found children to be removed
 			System.out.println(m1parIndex + " " + m2parIndex + " : " + m1children1 + " " + m1lvl1matched + " : " + m2children1 + " " + m2lvl1matched);
 			System.out.println(m1parIndex + " " + m2parIndex + " Children before removal " + m1children1 + " : " + m2children1);
 			// Remove nodes from m1children1 that were rmved to get m2children1
 			ArrayList<matchTreeNode> m1lvl1nodesRmved = new ArrayList<matchTreeNode>();
 			for (int m1rem = 0; m1rem < m1children1.size(); m1rem++) {
 			    matchTreeNode m1remcurr = m1children1.get(m1rem);
 			    if (!m1lvl1matched.contains(m1rem)) {
 				//System.out.println("m1 unmatched tree " + rmvedNodes + " " + m1remcurr.toString());
 				if (rmvedNodes.contains(m1remcurr.toString())) {
 				    m1lvl1nodesRmved.add(m1remcurr);
 				    m1children1.remove(m1remcurr);
 				}
 			    }
 			}
 
 			// Remove nodes from m2children 2 which arent matched in m1children1
 			ArrayList<matchTreeNode> m2lvl1nodesRmved = new ArrayList<matchTreeNode>();
 			for (int m2rem = 0; m2rem < m2children1.size(); m2rem++) {
 			    matchTreeNode m2remcurr = m2children1.get(m2rem);
 			    if (!m2lvl1matched.contains(m2rem)) {
 				if (addedNodes.contains(m2remcurr.toString())) {
 				    m2lvl1nodesRmved.add(m2remcurr);
 				    m2children1.remove(m2remcurr);
 				}
 			    }
 			}
 			System.out.println("Children after removal " + m1children1 + " : " + m2children1 + " removed " + m1lvl1nodesRmved + " " + m2lvl1nodesRmved);
 
 			ArrayList<matchTreeNode> childrenReTest = compareChildren(m1children1, m2children1);
 			System.out.println("retest = " + childrenReTest);
 			for (matchTreeNode mtn : m1lvl1nodesRmved) {
 			    m1children1.add(mtn);
 			}
 			for (matchTreeNode mtn : m2lvl1nodesRmved) {
 			    m2children1.add(mtn);
 			}
 			if (childrenReTest.size() == 0) {
 			    int index = m1parIndex + 1;
 			    int ind = m2parIndex + 1;
 			    mapping.put(index, ind);
 			    m2visited.add(m2parIndex);
 			    
 			    break;
 			}
 			if (m2ChldrnMatched.size() == m2children1.size()) {
 			    //System.out.println("match!!!" + m1parIndex + " " + m2parIndex);
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
 			    //System.out.println(index + " " + ind);
 			    m2visited.add(m2parIndex);
 			    break;
 			} else {
 			    mapping.put(index, ind);
 			    //System.out.println(index + " " + ind);
 			    m2visited.add(m2parIndex);
 			    break;
 			}
 		    }
 		}
 	    }
 	}
 	System.out.println("Post removal pre-doublecheck " + mapping.keySet().size());
 	for (int m1atom : mapping.keySet()) {
 	    System.out.println("reactant atom " + m1atom + " maps to " + mapping.get(m1atom) + " in the product");
 	}
 	//if (addedNodes.size() == 0 && rmvedNodes.size() == 0) {
        	if (addedNodes.size() == 0 && rmvedNodes.size() == 0) {
 
 	    for (int m1parIndex = 0; m1parIndex < m1parents.size(); m1parIndex++) {
 		matchTreeNode reactantTest = m1parents.get(m1parIndex);
 		ArrayList<Integer> m1bonds = reactantTest.getAtomBonds();
 		int atomNum = reactantTest.getAtomNumber();
 		
 		ArrayList<Integer> possible = new ArrayList<Integer>();
 		for (int i=0; i<m1bonds.size(); i++){
 		    if (mapping.keySet().contains(m1bonds.get(i)) && !mapping.keySet().contains(atomNum)) {
 			int bondedAtom = mapping.get(m1bonds.get(i));
 			int atomNum2 = m2parents.get(bondedAtom).getAtomNumber()-1;
 			
 			ArrayList<Integer> m2bonds = m2parents.get(atomNum2-1).getAtomBonds();
 			for (Integer k : m2bonds) {
 			    possible.add(k);
 			}
 		    }
 		}
 		for (int y = 0; y < possible.size(); y ++) {
 		    int j = possible.get(y);
 		    if (m2eqMatch.contains(j-1)) {
 			//System.out.println(j-1 + " " + m2eqMatch);
 			possible.remove(y);
 			y --;
 		    }
 		}
 		if (possible.size() == 1 && !m2visited.contains(possible.get(0)-1)) {
 		    //System.out.println("match");
 		    mapping.put(atomNum, possible.get(0));
 		    m1eqMatch.add(atomNum-1);
 		    m2eqMatch.add(possible.get(0)-1);
 		} if (possible.size() > 1 ) {
 		    for (int q = 0; q < possible.size(); q++) {
 			//System.out.println(reactantTest + " " + m2parents.get(possible.get(q)-1) + " " + possible.get(q));
 			if (reactantTest.toString().equals(m2parents.get(possible.get(q)-1).toString()) && !m2visited.contains(possible.get(q)-1)) {
 			    mapping.put(atomNum, possible.get(q));
 			    m1eqMatch.add(atomNum - 1);
 			    m2eqMatch.add(possible.get(q)-1);
 			}
 		    }
 		}
 		
 		
 	    }
 	}
     	System.out.println(mapping.keySet().size());
 	for (int m1atom : mapping.keySet()) {
 	    System.out.println("reactant atom " + m1atom + " maps to " + mapping.get(m1atom) + " in the product");
 	}
 	// Ask for clarification
 	Scanner userInput = new Scanner(System.in);
 	System.out.println("Is this matching correct? Y or N");
 	String correct = userInput.next();
 	int userMatch = 0;
 	if (correct.equals("N") || correct.equals("n")) {
 	    System.out.println("Please Enter Which Atoms Need Correcting: (enter 0 to end)");
 	    int [] atomsForCorrecting = new int[mapping.keySet().size()];
 	    int i = 0;
 	    while (true) {
 		int input = userInput.nextInt();
 		if (input == 0 || i >= mapping.keySet().size()){
 		    System.out.println("In break if ");
 		    break;
 		}
 		atomsForCorrecting[i] = input;
 		i++;
 	    }
 	    for (int j = 0; j < i; j++) {
 		System.out.println("What atom does " + atomsForCorrecting[j] + " match to? ");
 		int newMatch = userInput.nextInt();
 		mapping.put(atomsForCorrecting[j], newMatch);
 	    } 
 	    
 	} else if (correct.equals("Y") || correct.equals("y")) {
 	    System.out.println("Matching was correct");
 	}
 	System.out.println("Mapping as inputted");
 	for (int m1atom : mapping.keySet()) {
 	    System.out.println("Reactant Atom " + m1atom + " maps to " + mapping.get(m1atom) + " in the product");
 	}
 	generateSignature(m1, m2, mapping);
     } // close matchAtoms method
 
     public void generateSignature(Molecule reactant, Molecule product, HashMap<Integer, Integer> mapping) {
 	HashMap<Integer, Integer> reacNumHydrogens = new HashMap<Integer, Integer>();
 	HashMap<Integer, Integer> prodNumHydrogens = new HashMap<Integer, Integer>();
 	HashMap<String, Integer> atomicNumberTable = new HashMap<String, Integer>();
 	HashMap<String, Integer> numberOfBonds = new HashMap<String, Integer>();
 	// Filling atomicNumberTable
 	atomicNumberTable.put("H", 1);
 	atomicNumberTable.put("Li",3);
 	atomicNumberTable.put("C",6);
 	atomicNumberTable.put("N", 7);
 	atomicNumberTable.put("O", 8);
 	atomicNumberTable.put("F",9);
 	atomicNumberTable.put("Na",11);
 	atomicNumberTable.put("Si",14);
 	atomicNumberTable.put("P",15);
 	atomicNumberTable.put("S",16);
 	atomicNumberTable.put("Cl",17);
 	atomicNumberTable.put("K",19);
 	atomicNumberTable.put("Br",35);
 	atomicNumberTable.put("I",53);
 	// Filling Number of Bonds Table
 	numberOfBonds.put("H", 1);
 	numberOfBonds.put("C", 4);
 	numberOfBonds.put("N", 3);
 	numberOfBonds.put("O", 2);
 	numberOfBonds.put("F", 1);
 	numberOfBonds.put("Cl", 1);
 	numberOfBonds.put("K",1);
 	numberOfBonds.put("Br",1);
 	numberOfBonds.put("I",1);
 	numberOfBonds.put("Na",1);
 	numberOfBonds.put("P", 4);
 	numberOfBonds.put("Si",4);
 	
 	// populate Hydrogen Tables
 	int[] reactFreqs = reactant.getFreqs();
 	int[] prodFreqs = product.getFreqs();
 	ArrayList<String> reactAtoms = reactant.getAtoms();
 	ArrayList<String> prodAtoms = product.getAtoms();
 	for (int i = 0; i< reactAtoms.size(); i++) {
 	    int atomIndex = i + 1;
 	    String atom = reactAtoms.get(i);
 	    int currentBonds = reactFreqs[atomIndex-1];
 	    int totalBonds = numberOfBonds.get(atom);
 	    //System.out.println("R " + atom + " " + atomIndex  + " has " + currentBonds + " but needs " + totalBonds);
 	    reacNumHydrogens.put(atomIndex, totalBonds-currentBonds);
 	}
 	for (int i = 0; i <  prodAtoms.size() ; i++) {
 	    int atomIndex = i + 1;
 	    String atom = prodAtoms.get(i);
 	    int currentBonds = prodFreqs[atomIndex-1];
 	    int totalBonds = numberOfBonds.get(atom);
 	    //System.out.println("P " + atom + " " + atomIndex + " has " + currentBonds + " but needs " + totalBonds);
 	    prodNumHydrogens.put(atomIndex, totalBonds-currentBonds);
 	}
 	for (int key : reacNumHydrogens.keySet()) {
 	    System.out.println("Reactant " + key + " has " + reacNumHydrogens.get(key) + " hydrogens.");
 	}
 	for (int key: prodNumHydrogens.keySet()) {
 	    System.out.println("Product " + key + " has " + prodNumHydrogens.get(key) + " hydrogens.");
 	}
 	// Find which bonds change on which atoms
 	ArrayList<matchTreeNode> reactTrees = reactant.getatomTrees();
 	ArrayList<matchTreeNode> prodTrees = product.getatomTrees();
 	ArrayList<ArrayList<String>> reactAtomBonds = new ArrayList<ArrayList<String>>();
 	ArrayList<ArrayList<String>> prodAtomBonds = new ArrayList<ArrayList<String>>();
 	for (int i = 0; i < reactTrees.size(); i++) {
 	    reactAtomBonds.add(new ArrayList<String>());
 	    prodAtomBonds.add(new ArrayList<String>());
 	}
 	for (int i = 0; i < reactTrees.size(); i++) {
 	    matchTreeNode curr = reactTrees.get(i);
 	    ArrayList<matchTreeNode> children = curr.getChildren();
 	    int temp = i + 1;
 	    System.out.println("Reactant " + temp + " " + children);
 	    for (matchTreeNode mtn : children) {
 		reactAtomBonds.get(i).add(mtn.toString());
 	    }	
 	    int hydrogens = reacNumHydrogens.get(i+1);
 	    for (int j = 0; j < hydrogens; j++) {
 		reactAtomBonds.get(i).add("H1");
 	    }
 	}
 	for (int i = 0; i < prodTrees.size(); i++) {
 	    matchTreeNode curr = prodTrees.get(i);
 	    ArrayList<matchTreeNode> children = curr.getChildren();
 	    int temp = i + 1;
 	    System.out.println("product " + temp + " " + children);
 	    for (matchTreeNode mtn : children) {
 		prodAtomBonds.get(i).add(mtn.toString());
 	    }
 	    int hydrogens = prodNumHydrogens.get(i+1);
 	    for (int j = 0; j < hydrogens; j++) {
 		prodAtomBonds.get(i).add("H1");
 	    }
 	}
 	for (int k = 0; k < reactAtomBonds.size(); k++) {
 	    int j = k + 1;
 	    System.out.println("All of reactant " + j + " bonds are: " + reactAtomBonds.get(k));
 	}
 	for (int k = 0; k < prodAtomBonds.size(); k++) {
 	    int j = k+1;
 	    System.out.println("All of product " + j + " bonds are: " + prodAtomBonds.get(k));
 	}
 	
 	// Find which atoms are part of change
 	ArrayList<Integer> changingAtoms = new ArrayList<Integer>();
 	for (int i = 0; i < reactAtomBonds.size(); i++) {
 	    int atomNum = i + 1;
 	    int atomMatchNum = mapping.get(atomNum);
 	    ArrayList<String> rBonds = reactAtomBonds.get(i);
 	    ArrayList<String> pBonds = prodAtomBonds.get(atomMatchNum - 1);
 	    boolean compare = compareList(rBonds, pBonds);
 	    if (compare == false) {
 		changingAtoms.add(atomNum);
 	    }
 	}
 	System.out.println("The atoms as indexed by reactants which change are: " + changingAtoms);
 	HashMap<String, ArrayList<String>> removedBonds = new HashMap<String, ArrayList<String>>();
 	HashMap<String, ArrayList<String>> addedBonds = new HashMap<String, ArrayList<String>>();
 	for (int i = 0; i < changingAtoms.size(); i++) {
 	    int atomNum = changingAtoms.get(i);
 	    int atomMatchNum = mapping.get(atomNum);
 	    int atomNum2 = atomMatchNum - 1;
 	    ArrayList<String> reac = reactAtomBonds.get(atomNum-1);
 	    ArrayList<String> prod = prodAtomBonds.get(atomNum2);
 	    for (int j = 0; j < reac.size(); j++) {
 		String r = reac.get(j);
 		if (prod.contains(r)) {
 		    reac.remove(r);
 		    prod.remove(r);
 		}
 	    }
 	    System.out.println(atomNum  + " " + atomMatchNum + " has these bonds removed " + reac + " and these bonds added " + prod);
 	}
 	/*ArrayList<String> test1 = new ArrayList<String>();
 	ArrayList<String> test2 = new ArrayList<String>();
 	test1.add("C1");
 	test1.add("C1");
 	test1.add("H1");
 	test2.add("C1");
 	test2.add("H1");
 	test2.add("C1");
 	System.out.println(compareList(test1, test2));
 	*/
 	
     } // close generate Signature method
 
     public boolean compareList(ArrayList<String> l1, ArrayList<String> l2) {
 	if (l1.size() != l2.size()) {
 	    return false;
 	}
 	for (String s : l2) {
 	    if (l1.contains(s)) {
 		l1.remove(s);
 	    }
 	}
 	if (l1.size() == 0) {
 	    return true;
 	}
 	return false;
     }
 
     public ArrayList<matchTreeNode> compareChildren(ArrayList<matchTreeNode> m1, ArrayList<matchTreeNode> m2) {
 	ArrayList<matchTreeNode> unMatchedNodes = new ArrayList<matchTreeNode>();
 	ArrayList<Integer> m2visited = new ArrayList<Integer>();
 	if (m1.size() != m2.size()) {
 	    unMatchedNodes.add(new matchTreeNode("z",4,null,0, new ArrayList<String>()));
 	    return unMatchedNodes;
 	}
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
     
    
 } // close Reactionsv7 class
