 /*
  * Copyright (c) 2011-2012 Vikram Sundar.
  * All Rights Reserved.
  */
 package org.vikramdock;
 
 import java.io.*;
 import java.util.*;
 import java.util.zip.*;
 import java.lang.*;
 import java.lang.reflect.*;
 
 public class ProteinStruct {
 	private ArrayList<Atom> structurea;
 	private ImmutableArrayList<Atom> structure;
 	private ArrayList<Atom> surface;
 	private ArrayList<Bond> bonds;
 	private ArrayList<Bond> surfacebonds;
 	private ArrayList<Atom> backbone;
 	private ArrayList<Atom> surfacebackbone;
 	private ArrayList<Bond> backbonebonds;
 	private ArrayList<Bond> surfacebackbonebonds;
 	private HashMap<Character,Character> chaintranslator;
 	private HashMap<Character,Character> reversechaintrans;
 	private HashMap<Character,Character> chaintranslator2;
 	private HashMap<Character,Character> reversechaintrans2;
 	private HashMap<String,Integer> translator1;
 	private HashMap<String,String> translator2;
 	private HashMap<String,Integer> translator3;
 	private HashMap<String,String> translator4;
 	private String filepath;
 	private PrintWriter out;
 	private int chaincount;
 	private double xcoordcent;
 	private double ycoordcent;
 	private double zcoordcent;
 	private boolean rotated;
 	private double size = 0;
 	private double[][][] newsizes = new double[(int)(2*Math.PI/Constants.ALPHAINC)][(int)(2*Math.PI/Constants.BETAINC)][(int)(2*Math.PI/Constants.GAMMAINC)];
 	private double[][][][] potentials;
 	private double[][][][] solvpotentials;
 	private double solvEModel;
	public ProteinStruct(String filepath, PrintWriter out, HashMap<Character,Character> chaintranslator2, HashMap<Character,Character>  reversechaintrans2) throws Exception {
 		chaintranslator = new HashMap<Character,Character>();
 		structurea = new ArrayList<Atom>();
 		surface = new ArrayList<Atom>();
 		bonds = new ArrayList<Bond>();
 		surfacebonds = new ArrayList<Bond>();
 		backbone = new ArrayList<Atom>();
 		surfacebackbone = new ArrayList<Atom>();
 		backbonebonds = new ArrayList<Bond>();
 		surfacebackbonebonds = new ArrayList<Bond>();
 		translator1 = new HashMap<String,Integer>();
 		translator2 = new HashMap<String,String>();
 		translator3 = new HashMap<String,Integer>();
 		translator4 = new HashMap<String,String>();
 		reversechaintrans = new HashMap<Character,Character>();
 		translator1.put("ALA",0);
 		translator1.put("CYS",1);
 		translator1.put("ASP",2);
 		translator1.put("GLU",3);
 		translator1.put("PHE",4);
 		translator1.put("GLY",5);
 		translator1.put("HIS",6);
 		translator1.put("ILE",7);
 		translator1.put("LYS",8);
 		translator1.put("LEU",9);
 		translator1.put("MET",10);
 		translator1.put("ASN",11);
 		translator1.put("PRO",12);
 		translator1.put("GLN",13);
 		translator1.put("ARG",14);
 		translator1.put("SER",15);
 		translator1.put("THR",16);
 		translator1.put("VAL",17);
 		translator1.put("TRP",18);
 		translator1.put("TYR",19);
 		translator2.put("ALA","A");
 		translator2.put("CYS","C");
 		translator2.put("ASP","D");
 		translator2.put("GLU","E");
 		translator2.put("PHE","F");
 		translator2.put("GLY","G");
 		translator2.put("HIS","H");
 		translator2.put("ILE","I");
 		translator2.put("LYS","K");
 		translator2.put("LEU","L");
 		translator2.put("MET","M");
 		translator2.put("ASN","N");
 		translator2.put("PRO","P");
 		translator2.put("GLN","Q");
 		translator2.put("ARG","R");
 		translator2.put("SER","S");
 		translator2.put("THR","T");
 		translator2.put("VAL","V");
 		translator2.put("TRP","W");
 		translator2.put("TYR","Y");
 		translator3.put("A",0);
 		translator3.put("C",1);
 		translator3.put("D",2);
 		translator3.put("E",3);
 		translator3.put("F",4);
 		translator3.put("G",5);
 		translator3.put("H",6);
 		translator3.put("I",7);
 		translator3.put("K",8);
 		translator3.put("L",9);
 		translator3.put("M",10);
 		translator3.put("N",11);
 		translator3.put("P",12);
 		translator3.put("Q",13);
 		translator3.put("R",14);
 		translator3.put("S",15);
 		translator3.put("T",16);
 		translator3.put("V",17);
 		translator3.put("W",18);
 		translator3.put("Y",19);
 		translator4.put("A","ALA");
 		translator4.put("C","CYS");
 		translator4.put("D","ASP");
 		translator4.put("E","GLU");
 		translator4.put("F","PHE");
 		translator4.put("G","GLY");
 		translator4.put("H","HIS");
 		translator4.put("I","ILE");
 		translator4.put("K","LYS");
 		translator4.put("L","LEU");
 		translator4.put("M","MET");
 		translator4.put("N","ASN");
 		translator4.put("P","PRO");
 		translator4.put("Q","GLN");
 		translator4.put("R","ARG");
 		translator4.put("S","SER");                                 
 		translator4.put("T","THR");                                                             
 		translator4.put("V","VAL");
 		translator4.put("W","TRP");
 		translator4.put("Y","TYR");
 		this.filepath = filepath;
 		this.out = out;
 		this.chaintranslator2 = chaintranslator2;
 		this.reversechaintrans2 = reversechaintrans2;
 		rotated = false;
 		parseStructure(filepath);
 		structure = new ImmutableArrayList<Atom>(structurea);
 		long beforeS = System.currentTimeMillis(); 
 		determineSurfaceNew();
 		long afterS = System.currentTimeMillis();
 		out.println("SURFACE TIME TOTAL " + (afterS - beforeS));
 		out.println("SURFACE SIZE " + surface.size());
 		potentials = new double[(int)((2*Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE + 2*Constants.VDWDISTTHRESHOLD)/Constants.GRIDGRAINSIZE)][(int)((2*Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE + 2*Constants.VDWDISTTHRESHOLD)/Constants.GRIDGRAINSIZE)][(int)((2*Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE + 2*Constants.VDWDISTTHRESHOLD)/Constants.GRIDGRAINSIZE)][5];
 		solvpotentials = new double[(int)((2*Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE + 2*Constants.VDWDISTTHRESHOLD)/Constants.GRIDGRAINSIZE)][(int)((2*Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE + 2*Constants.VDWDISTTHRESHOLD)/Constants.GRIDGRAINSIZE)][(int)((2*Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE + 2*Constants.VDWDISTTHRESHOLD)/Constants.GRIDGRAINSIZE)][17];
 		detBondsBackbone();
 		long beforeP = System.currentTimeMillis();
 		detPotentials();
 		long afterP = System.currentTimeMillis();
 		out.println("POTENTIALS TIME TOTAL " + (afterP - beforeP));
 		out.flush();
 	}
 	public ProteinStruct(ArrayList<Atom> surface, ArrayList<Bond> bonds, ArrayList<Atom> backbone, ArrayList<Bond> backbonebonds, double size, double[][][] newsizes, double[][][][] potentials, double[][][][] solvpotentials, double solvEModel, int chaincount, double xcoordcent, double ycoordcent, double zcoordcent) throws Exception  {
 		this.structurea = surface;
 		this.structure = new ImmutableArrayList<Atom>(this.structurea);
 		this.surface = surface;
 		this.bonds = bonds;
 		this.surfacebonds = bonds;
 		this.backbone = backbone;
 		this.surfacebackbone = backbone;
 		this.backbonebonds = backbonebonds;
 		this.surfacebackbonebonds = backbonebonds;
 		this.size = size;
 		this.newsizes = newsizes;
 		this.potentials = potentials;
 		this.solvpotentials = solvpotentials;
 		this.solvEModel = solvEModel;
 		rotated = true;
 		translator1 = new HashMap<String,Integer>();
 		translator2 = new HashMap<String,String>();
 		translator3 = new HashMap<String,Integer>();
 		translator4 = new HashMap<String,String>();
 		translator1.put("ALA",0);
 		translator1.put("CYS",1);
 		translator1.put("ASP",2);
 		translator1.put("GLU",3);
 		translator1.put("PHE",4);
 		translator1.put("GLY",5);
 		translator1.put("HIS",6);
 		translator1.put("ILE",7);
 		translator1.put("LYS",8);
 		translator1.put("LEU",9);
 		translator1.put("MET",10);
 		translator1.put("ASN",11);
 		translator1.put("PRO",12);
 		translator1.put("GLN",13);
 		translator1.put("ARG",14);
 		translator1.put("SER",15);
 		translator1.put("THR",16);
 		translator1.put("VAL",17);
 		translator1.put("TRP",18);
 		translator1.put("TYR",19);
 		translator2.put("ALA","A");
 		translator2.put("CYS","C");
 		translator2.put("ASP","D");
 		translator2.put("GLU","E");
 		translator2.put("PHE","F");
 		translator2.put("GLY","G");
 		translator2.put("HIS","H");
 		translator2.put("ILE","I");
 		translator2.put("LYS","K");
 		translator2.put("LEU","L");
 		translator2.put("MET","M");
 		translator2.put("ASN","N");
 		translator2.put("PRO","P");
 		translator2.put("GLN","Q");
 		translator2.put("ARG","R");
 		translator2.put("SER","S");
 		translator2.put("THR","T");
 		translator2.put("VAL","V");
 		translator2.put("TRP","W");
 		translator2.put("TYR","Y");
 		translator3.put("A",0);
 		translator3.put("C",1);
 		translator3.put("D",2);
 		translator3.put("E",3);
 		translator3.put("F",4);
 		translator3.put("G",5);
 		translator3.put("H",6);
 		translator3.put("I",7);
 		translator3.put("K",8);
 		translator3.put("L",9);
 		translator3.put("M",10);
 		translator3.put("N",11);
 		translator3.put("P",12);
 		translator3.put("Q",13);
 		translator3.put("R",14);
 		translator3.put("S",15);
 		translator3.put("T",16);
 		translator3.put("V",17);
 		translator3.put("W",18);
 		translator3.put("Y",19);
 		translator4.put("A","ALA");
 		translator4.put("C","CYS");
 		translator4.put("D","ASP");
 		translator4.put("E","GLU");
 		translator4.put("F","PHE");
 		translator4.put("G","GLY");
 		translator4.put("H","HIS");
 		translator4.put("I","ILE");
 		translator4.put("K","LYS");
 		translator4.put("L","LEU");
 		translator4.put("M","MET");
 		translator4.put("N","ASN");
 		translator4.put("P","PRO");
 		translator4.put("Q","GLN");
 		translator4.put("R","ARG");
 		translator4.put("S","SER");
 		translator4.put("T","THR");
 		translator4.put("V","VAL");
 		translator4.put("W","TRP");
 		translator4.put("Y","TYR");
 		filepath = "INVALID";
 		this.chaincount = chaincount;
 		this.xcoordcent = xcoordcent;
 		this.ycoordcent = ycoordcent;
 		this.zcoordcent = zcoordcent;
 	}
 	public ProteinStruct(ProteinStruct clone) throws Exception {
 		this.filepath = clone.getFilepath();
 		structurea = new ArrayList<Atom>();
 		surface = new ArrayList<Atom>();
 		bonds = new ArrayList<Bond>();
 		surfacebonds = new ArrayList<Bond>();
 		backbone = new ArrayList<Atom>();
 		surfacebackbone = new ArrayList<Atom>();
 		backbonebonds = new ArrayList<Bond>();
 		surfacebackbonebonds = new ArrayList<Bond>();
 		translator1 = new HashMap<String,Integer>();
 		translator2 = new HashMap<String,String>();
 		translator3 = new HashMap<String,Integer>();
 		translator4 = new HashMap<String,String>();
 		chaintranslator = new HashMap<Character,Character>();
 		reversechaintrans = new HashMap<Character,Character>();
 		chaintranslator2 = new HashMap<Character,Character>();
 		reversechaintrans2 = new HashMap<Character,Character>();
 		structure = new ImmutableArrayList<Atom>(clone.getStructure());
 		ArrayList<Atom> clonestructa = clone.getSurface();
 		for (int i = 0; i < clonestructa.size(); i++) {
 			Atom current = (Atom)clonestructa.get(i);
 			this.surface.add(new Atom(current));
 		}
 		clonestructa = clone.getBackbone();
 		for (int i = 0; i < clonestructa.size(); i++) {
 			Atom current = (Atom)clonestructa.get(i);
 			this.backbone.add(new Atom(current));
 		}
 		clonestructa = clone.getSurfaceBackbone();
 		for (int i = 0; i < clonestructa.size(); i++) {
 			Atom current = (Atom)clonestructa.get(i);
 			this.surfacebackbone.add(new Atom(current));
 		}
 		ArrayList<Bond> clonebonds = clone.getBonds();
 		for (int i = 0; i < clonebonds.size(); i++) {
 			Bond current = (Bond)clonebonds.get(i);
 			this.bonds.add(new Bond(current));
 		}
 		clonebonds = clone.getSurfaceBonds();
 		for (int i = 0; i < clonebonds.size(); i++) {
 			Bond current = (Bond)clonebonds.get(i);
 			this.surfacebonds.add(new Bond(current));
 		}
 		clonebonds = clone.getBackboneBonds();
 		for (int i = 0; i < clonebonds.size(); i++) {
 			Bond current = (Bond)clonebonds.get(i);
 			this.backbonebonds.add(new Bond(current));
 		}
 		clonebonds = clone.getSurfaceBackboneBonds();
 		for (int i = 0; i < clonebonds.size(); i++) {
 			Bond current = (Bond)clonebonds.get(i);
 			this.surfacebackbonebonds.add(new Bond(current));
 		}
 		translator1.put("ALA",0);
 		translator1.put("CYS",1);
 		translator1.put("ASP",2);
 		translator1.put("GLU",3);
 		translator1.put("PHE",4);
 		translator1.put("GLY",5);
 		translator1.put("HIS",6);
 		translator1.put("ILE",7);
 		translator1.put("LYS",8);
 		translator1.put("LEU",9);
 		translator1.put("MET",10);
 		translator1.put("ASN",11);
 		translator1.put("PRO",12);
 		translator1.put("GLN",13);
 		translator1.put("ARG",14);
 		translator1.put("SER",15);
 		translator1.put("THR",16);
 		translator1.put("VAL",17);
 		translator1.put("TRP",18);
 		translator1.put("TYR",19);
 		translator2.put("ALA","A");
 		translator2.put("CYS","C");
 		translator2.put("ASP","D");
 		translator2.put("GLU","E");
 		translator2.put("PHE","F");
 		translator2.put("GLY","G");
 		translator2.put("HIS","H");
 		translator2.put("ILE","I");
 		translator2.put("LYS","K");
 		translator2.put("LEU","L");
 		translator2.put("MET","M");
 		translator2.put("ASN","N");
 		translator2.put("PRO","P");
 		translator2.put("GLN","Q");
 		translator2.put("ARG","R");
 		translator2.put("SER","S");
 		translator2.put("THR","T");
 		translator2.put("VAL","V");
 		translator2.put("TRP","W");
 		translator2.put("TYR","Y");
 		translator3.put("A",0);
 		translator3.put("C",1);
 		translator3.put("D",2);
 		translator3.put("E",3);
 		translator3.put("F",4);
 		translator3.put("G",5);
 		translator3.put("H",6);
 		translator3.put("I",7);
 		translator3.put("K",8);
 		translator3.put("L",9);
 		translator3.put("M",10);
 		translator3.put("N",11);
 		translator3.put("P",12);
 		translator3.put("Q",13);
 		translator3.put("R",14);
 		translator3.put("S",15);
 		translator3.put("T",16);
 		translator3.put("V",17);
 		translator3.put("W",18);
 		translator3.put("Y",19);
 		translator4.put("A","ALA");
 		translator4.put("C","CYS");
 		translator4.put("D","ASP");
 		translator4.put("E","GLU");
 		translator4.put("F","PHE");
 		translator4.put("G","GLY");
 		translator4.put("H","HIS");
 		translator4.put("I","ILE");
 		translator4.put("K","LYS");
 		translator4.put("L","LEU");
 		translator4.put("M","MET");
 		translator4.put("N","ASN");
 		translator4.put("P","PRO");
 		translator4.put("Q","GLN");
 		translator4.put("R","ARG");
 		translator4.put("S","SER");
 		translator4.put("T","THR");
 		translator4.put("V","VAL");
 		translator4.put("W","TRP");
 		translator4.put("Y","TYR");
 		this.chaincount = clone.getChaincount();
 		this.xcoordcent = clone.getXCoordCent();
 		this.ycoordcent = clone.getYCoordCent();
 		this.zcoordcent = clone.getZCoordCent();
 		this.rotated = clone.getRotated();
 		this.size = clone.getSize();
 		this.newsizes = clone.getNewSizes();
 		this.potentials = clone.getPotentials();
 		this.solvpotentials = clone.getSolvPotentials();
 		this.solvEModel = clone.getSolvEModel();
 	}
 	public void parseStructure(String filepath) throws Exception {
 		try {
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)));
 			ArrayList<String> structraw = new ArrayList<String>();
 			int structcount = 0;
 			while(true) {
 				String s = br.readLine();
 				if(s != null) {
 					String[] ssplit = new String[20];
 					ssplit = s.split(" ");
 					if (ssplit[0].equals("ATOM")) {
 						structraw.add(s);
 						structcount++;
 					}
 				} else {
 					break;
 				}
 			}
 			double xcoordsum = 0;
 			double ycoordsum = 0;
 			double zcoordsum = 0;
 			int atomcount = 1;
 			int rescount = 0;
 			int pastres = -1;
 			Iterator it = structraw.iterator();
 			for (int i = 0; i < structcount; i++) {
 				String catom = (String)it.next();
 				double xcoord = Double.parseDouble(removeSpace(catom.substring(30,38)));
 				double ycoord = Double.parseDouble(removeSpace(catom.substring(38,46)));
 				double zcoord = Double.parseDouble(removeSpace(catom.substring(46,54)));
 				char element = ' ';
 				if (catom.length() > 77) {
 					element = catom.charAt(77);
 				}
 				int resnum = Integer.parseInt(removeSpace(catom.substring(22,26)));
 				int atomnum = Integer.parseInt(removeSpace(catom.substring(6,11)));
				char chainnum = chaintranslator2.get(catom.charAt(21)).toString().charAt(0);
 				String eType = removeSpace(catom.substring(12,16));
 				String AA = removeSpace(catom.substring(17,20));
 				if (element == ' ') {
 					element = eType.charAt(0);
 				}
 				if (pastres != resnum) {
 					rescount++;
 				}
 				Atom next = new Atom(xcoord, ycoord, zcoord, element, rescount, atomcount, chainnum, eType, AA);
 				if (Double.isNaN(xcoord) || Double.isNaN(ycoord) || Double.isNaN(zcoord)) {
 					System.err.println("NaN found");
 					next.printAtomErr();
 				}
 				structurea.add(next);
 				xcoordsum += xcoord;
 				ycoordsum += ycoord;
 				zcoordsum += zcoord;
 				pastres = resnum;
 				atomcount++;
 			}
 			xcoordcent = xcoordsum/structcount;
 			ycoordcent = ycoordsum/structcount;
 			zcoordcent = zcoordsum/structcount;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			throw ex;
 		}
 	}
 	public void determineSurfaceNew() throws Exception {
 		for (int i = 0; i < (int)(2*Math.PI/Constants.ALPHAINC); i++) {
 			for (int j = 0; j < (int)(2*Math.PI/Constants.BETAINC); j++) {
 				for (int k = 0; k < (int)(2*Math.PI/Constants.GAMMAINC); k++) {
 					newsizes[i][j][k] = Double.POSITIVE_INFINITY;
 				}
 			}
 		}
 		ArrayList<Atom> newstructure = new ArrayList<Atom>();
 		for (int i = 0; i < structure.size(); i++) {
 			Atom current = (Atom)structure.get(i);
 			Atom trcurrent = current.transAtom(-xcoordcent, -ycoordcent, -zcoordcent);
 			trcurrent.setSpherical();
 			size = Math.max(size, trcurrent.getXcoord());
 			trcurrent.setCartesian();
 			newstructure.add(trcurrent);
 		}
 		ArrayList<Atom> done = new ArrayList<Atom>();
 		for (double i = Constants.ALPHAINC/2; i < 2*Math.PI; i += Constants.ALPHAINC) {
 			for (double j = Constants.BETAINC/2; j < 2*Math.PI; j += Constants.BETAINC) {
 				for (double k = Constants.GAMMAINC/2; k < 2*Math.PI; k += Constants.GAMMAINC) {
 					double maxnum = 0;
 					ArrayList<Atom> worked = new ArrayList<Atom>();
 					for (int l = 0; l < newstructure.size(); l++) {
 						Atom current = newstructure.get(l);
 						Atom test = current.rotateAtomNew(0, 0, 0, i, j, k);
 						test.setSpherical();
 						if (Math.abs(test.getYcoord()) <= Constants.ALPHAINC/2 && Math.abs(Math.PI/2 - test.getZcoord()) <= Constants.BETAINC/2) {
 							worked.add(current);
 							maxnum = Math.max(test.getXcoord(), maxnum);
 						}
 					}
 					newsizes[(int)((i-Constants.ALPHAINC/2)*(1/Constants.ALPHAINC))][(int)((j-Constants.BETAINC/2)*(1/Constants.BETAINC))][(int)((k-Constants.GAMMAINC/2)*(1/Constants.GAMMAINC))] = maxnum;
 					if (maxnum/size >= Constants.SURFACETHRESHOLD) {
 						for (int l = 0; l < worked.size(); l++) {
 							Atom thisa = (Atom)worked.get(l);
 							Atom current = thisa.transAtom(xcoordcent, ycoordcent, zcoordcent);
 							thisa.setSpherical();
 							if (maxnum - thisa.getXcoord() <= 2*Constants.SURFACESIZE && !done.contains(thisa)) {
 								thisa.setCartesian();
 								surface.add(current);
 								done.add(thisa);
 							}
 						}
 					}
 				}
 			}
 		}
 		out.println("DONE WITH DETERMINING SURFACE");
 	}
 	public void detBondsBackbone() throws Exception {
 		try { 
 			Atom first = (Atom)structure.get(0);
 			ArrayList<Atom> AminoAcid = new ArrayList<Atom>();
 			int resnum = first.getResnum();
 			int counter = 0;
 			for (int i = 0; i < structure.size(); i++) {
 				Atom current = (Atom)structure.get(i);
 				if (current.getResnum() == first.getResnum()) {
 					AminoAcid.add(current);
 				} else {
 					counter = i;
 					break;
 				}
 			}
 			Atom Catom = null;
 			Atom CAatom = null;
 			Atom Natom = null;
 			Atom Oatom = null;
 			Atom CatomLast = null;
 			Bond newb1 = null;
 			Bond newb2 = null;
 			Bond newb3 = null;
 			char restype = ' ';
 			for (int i = 0; i < AminoAcid.size(); i++) {
 				Atom current = (Atom)AminoAcid.get(i);
 				if (current.getEtype().equals("C")) {
 					Catom = current;
 				} else if (current.getEtype().equals("CA")) {
 					CAatom = current;
 				} else if (current.getEtype().equals("N")) {
 					Natom = current;
 				} else if (current.getEtype().equals("O")) {
 					Oatom = current;
 				}
 			}
 			if (Catom != null && CAatom != null && Natom != null && Oatom != null) {
 				backbone.add(Catom);
 				backbone.add(CAatom);
 				backbone.add(Natom);
 				newb1 = new Bond(Natom, CAatom, Constants.BONDCONST);
 				newb2 = new Bond(CAatom, Catom, Constants.BONDCONST);
 				newb3 = new Bond(Catom, Oatom, Constants.BONDCONST);
 				bonds.add(newb1);
 				bonds.add(newb2);
 				bonds.add(newb3);
 				backbonebonds.add(newb1);
 				backbonebonds.add(newb2);
 				CatomLast = Catom;
				restype = translator2.get(Catom.getAA()).toString().charAt(0);
 				AminoAcid.remove(Catom);
 				AminoAcid.remove(CAatom);
 				AminoAcid.remove(Natom);
 				AminoAcid.remove(Oatom);
 				Catom.setAtomType(0);
 				CAatom.setAtomType(1);
 				Natom.setAtomType(2);
 				Oatom.setAtomType(1);
 				if (restype == 'A') {
 					Atom CBatom = AminoAcid.get(0);
 					Bond newb4 = new Bond(CBatom, CAatom, Constants.BONDCONST); 
 					bonds.add(newb4);
 					CBatom.setAtomType(4);
 				} else if (restype == 'C') {
 					Atom CBatom = null;
 					Atom SGatom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype().equals("CB")) {
 							CBatom = current;
 						} else if (current.getEtype().equals("SG")) {
 							SGatom = current;
 						}
 					}
 					Bond newb4 = new Bond(CBatom, CAatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, SGatom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					CBatom.setAtomType(3);
 					SGatom.setAtomType(1);
 				} else if (restype == 'D') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom OD1atom = null;
 					Atom OD2atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype().equals("CB")) {
 							CBatom = current;
 						} else if (current.getEtype().equals("CG")) {
 							CGatom = current;
 						} else if (current.getEtype().equals("OD1")) {
 							OD1atom = current;
 						} else if (current.getEtype().equals("OD2")) {
 							OD2atom = current;
 						}
 					}
 					Bond newb7 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb4 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CGatom, OD1atom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, OD2atom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					CBatom.setAtomType(3);
 					CGatom.setAtomType(0);
 					OD1atom.setAtomType(1);
 					OD2atom.setAtomType(0);
 				} else if (restype == 'E') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom CDatom = null;
 					Atom OE1atom = null;
 					Atom OE2atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype().equals("CB")) {
 							CBatom = current;
 						} else if (current.getEtype().equals("CG")) {
 							CGatom = current;
 						} else if (current.getEtype().equals("CD")) {
 							CDatom = current;
 						} else if (current.getEtype().equals("OE1")) {
 							OE1atom = current;
 						} else if (current.getEtype().equals("OE2")) {
 							OE2atom = current;
 						}
 					}
 					Bond newb4 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CDatom, OE1atom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CDatom, OE2atom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 					Bond newb8 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					bonds.add(newb8);
 					CBatom.setAtomType(3);
 					CGatom.setAtomType(3);
 					CDatom.setAtomType(0);
 					OE1atom.setAtomType(1);
 					OE2atom.setAtomType(0);
 				} else if (restype == 'F') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom CD1atom = null;
 					Atom CD2atom = null;
 					Atom CE1atom = null;
 					Atom CE2atom = null;
 					Atom CZatom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype().equals("CB")) {
 							CBatom = current;
 						} else if (current.getEtype().equals("CG")) {
 							CGatom = current;
 						} else if (current.getEtype().equals("CD1")) {
 							CD1atom = current;
 						} else if (current.getEtype().equals("CD2")) {
 							CD2atom = current;
 						} else if (current.getEtype().equals("CE1")) {
 							CE1atom = current;
 						} else if (current.getEtype().equals("CE2")) {
 							CE2atom = current;
 						} else if (current.getEtype().equals("CZ")) {
 							CZatom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, CD1atom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CD1atom, CD2atom, Constants.BONDCONST);
 					Bond newb8 = new Bond(CD2atom, CZatom, Constants.BONDCONST);
 					Bond newb9 = new Bond(CZatom, CE1atom, Constants.BONDCONST);
 					Bond newb10 = new Bond(CE1atom, CE2atom, Constants.BONDCONST);
 					Bond newb11 = new Bond(CE2atom, CGatom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					bonds.add(newb8);
 					bonds.add(newb9);
 					bonds.add(newb10);
 					bonds.add(newb11);
 					CBatom.setAtomType(3);
 					CGatom.setAtomType(1);
 					CD1atom.setAtomType(5);
 					CD2atom.setAtomType(5);
 					CZatom.setAtomType(5);
 					CE1atom.setAtomType(5);
 					CE2atom.setAtomType(5);
 				} else if (restype == 'G') {
 					//This is intentionally left blank.
 				} else if (restype == 'H') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom ND1atom = null;
 					Atom CD2atom = null;
 					Atom CE1atom = null;
 					Atom NE2atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype().equals("CB")) {
 							CBatom = current;
 						} else if (current.getEtype().equals("CG")) {
 							CGatom = current;
 						} else if (current.getEtype().equals("ND1")) {
 							ND1atom = current;
 						} else if (current.getEtype().equals("CD2")) {
 							CD2atom = current;
 						} else if (current.getEtype().equals("CE1")) {
 							CE1atom = current;
 						} else if (current.getEtype().equals("NE2")) {
 							NE2atom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, ND1atom, Constants.BONDCONST);
 					Bond newb7 = new Bond(ND1atom, CD2atom, Constants.BONDCONST);
 					Bond newb8 = new Bond(CE1atom, NE2atom, Constants.BONDCONST);
 					Bond newb9 = new Bond(NE2atom, CGatom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					bonds.add(newb8);
 					bonds.add(newb9);
 					CBatom.setAtomType(3);
 					CGatom.setAtomType(2);
 					ND1atom.setAtomType(0);
 					CD2atom.setAtomType(4);
 					CE1atom.setAtomType(4);
 					NE2atom.setAtomType(0);
 				} else if (restype == 'I') {
 					Atom CBatom = null;
 					Atom CG1atom = null;
 					Atom CG2atom = null;
 					Atom CD1atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype().equals("CB")) {
 							CBatom = current;
 						} else if (current.getEtype().equals("CG1")) {
 							CG1atom = current;
 						} else if (current.getEtype().equals("CG2")) {
 							CG2atom = current;
 						} else if (current.getEtype().equals("CD1")) {
 							CD1atom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CG1atom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CG1atom, CG2atom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CBatom, CD1atom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					CBatom.setAtomType(3);
 					CG1atom.setAtomType(3);
 					CG2atom.setAtomType(4);
 					CD1atom.setAtomType(4);
 				} else if (restype == 'K') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom CDatom = null;
 					Atom CEatom = null;
 					Atom NZatom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype().equals("CB")) {
 							CBatom = current;
 						} else if (current.getEtype().equals("CG")) {
 							CGatom = current;
 						} else if (current.getEtype().equals("CD")) {
 							CDatom = current;
 						} else if (current.getEtype().equals("CE")) {
 							CEatom = current;
 						} else if (current.getEtype().equals("NZ")) {
 							NZatom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CDatom, CEatom, Constants.BONDCONST);
 					Bond newb8 = new Bond(CEatom, NZatom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					bonds.add(newb8);
 					CBatom.setAtomType(3);
 					CGatom.setAtomType(3);
 					CDatom.setAtomType(3);
 					CEatom.setAtomType(3);
 					NZatom.setAtomType(2);
 				} else if (restype == 'L') {
 					Atom CBatom = null;
 					Atom CG1atom = null;
 					Atom CD2atom = null;
 					Atom CD1atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype().equals("CB")) {
 							CBatom = current;
 						} else if (current.getEtype().equals("CG1")) {
 							CG1atom = current;
 						} else if (current.getEtype().equals("CD2")) {
 							CD2atom = current;
 						} else if (current.getEtype().equals("CD1")) {
 							CD1atom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CG1atom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CG1atom, CD2atom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CG1atom, CD1atom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					CBatom.setAtomType(3);
 					CG1atom.setAtomType(2);
 					CD2atom.setAtomType(4);
 					CD1atom.setAtomType(4);
 				} else if (restype == 'M') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom SDatom = null;
 					Atom CEatom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype().equals("CB")) {
 							CBatom = current;
 						} else if (current.getEtype().equals("CG")) {
 							CGatom = current;
 						} else if (current.getEtype().equals("SD")) {
 							SDatom = current;
 						} else if (current.getEtype().equals("CE")) {
 							CEatom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, SDatom, Constants.BONDCONST);
 					Bond newb7 = new Bond(SDatom, CEatom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					CBatom.setAtomType(3);
 					CGatom.setAtomType(3);
 					SDatom.setAtomType(0);
 					CEatom.setAtomType(4);
 				} else if (restype == 'N') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom OD1atom = null;
 					Atom ND2atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype().equals("CB")) {
 							CBatom = current;
 						} else if (current.getEtype().equals("CG")) {
 							CGatom = current;
 						} else if (current.getEtype().equals("OD1")) {
 							OD1atom = current;
 						} else if (current.getEtype().equals("ND2")) {
 							ND2atom = current;
 						}
 					}
 					Bond newb7 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb4 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CGatom, OD1atom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, ND2atom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					CBatom.setAtomType(3);
 					CGatom.setAtomType(0);
 					OD1atom.setAtomType(1);
 					ND2atom.setAtomType(2);
 				} else if (restype == 'P') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom CDatom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype().equals("CB")) {
 							CBatom = current;
 						} else if (current.getEtype().equals("CG")) {
 							CGatom = current;
 						} else if (current.getEtype().equals("CD")) {
 							CDatom = current;
 						} 
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CDatom, Natom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					Natom.setAtomType(5);
 					CBatom.setAtomType(3);
 					CGatom.setAtomType(3);
 					CDatom.setAtomType(3);
 				} else if (restype == 'Q') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom CDatom = null;
 					Atom OE1atom = null;
 					Atom NE2atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype().equals("CB")) {
 							CBatom = current;
 						} else if (current.getEtype().equals("CG")) {
 							CGatom = current;
 						} else if (current.getEtype().equals("CD")) {
 							CDatom = current;
 						} else if (current.getEtype().equals("OE1")) {
 							OE1atom = current;
 						} else if (current.getEtype().equals("NE2")) {
 							NE2atom = current;
 						}
 					}
 					Bond newb4 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CDatom, OE1atom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CDatom, NE2atom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 					Bond newb8 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					bonds.add(newb8);
 					CBatom.setAtomType(3);
 					CGatom.setAtomType(3);
 					CDatom.setAtomType(0);
 					OE1atom.setAtomType(1);
 					NE2atom.setAtomType(2);
 				} else if (restype == 'R') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom CDatom = null;
 					Atom NEatom = null;
 					Atom CZatom = null;
 					Atom NH1atom = null;
 					Atom NH2atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype().equals("CB")) {
 							CBatom = current;
 						} else if (current.getEtype().equals("CG")) {
 							CGatom = current;
 						} else if (current.getEtype().equals("CD")) {
 							CDatom = current;
 						} else if (current.getEtype().equals("NE")) {
 							NEatom = current;
 						} else if (current.getEtype().equals("CZ")) {
 							CZatom = current;
 						} else if (current.getEtype().equals("NH1")) {
 							NH1atom = current;
 						} else if (current.getEtype().equals("NH2")) {
 							NH2atom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CDatom, NEatom, Constants.BONDCONST);
 					Bond newb8 = new Bond(NEatom, CZatom, Constants.BONDCONST);
 					Bond newb9 = new Bond(CZatom, NH1atom, Constants.BONDCONST);
 					Bond newb10 = new Bond(CZatom, NH2atom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					bonds.add(newb8);
 					bonds.add(newb9);
 					bonds.add(newb10);
 					CBatom.setAtomType(3);
 					CGatom.setAtomType(3);
 					CDatom.setAtomType(3);
 					NEatom.setAtomType(0);
 					CZatom.setAtomType(2);
 					NH1atom.setAtomType(2);
 					NH2atom.setAtomType(4);
 				} else if (restype == 'S') {
 					Atom CBatom = null;
 					Atom OGatom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype().equals("CB")) {
 							CBatom = current;
 						} else if (current.getEtype().equals("OG")) {
 							OGatom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, OGatom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					CBatom.setAtomType(3);
 					OGatom.setAtomType(0);
 				} else if (restype == 'T') {
 					Atom CBatom = null;
 					Atom CG2atom = null;
 					Atom OG1atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype().equals("CB")) {
 							CBatom = current;
 						} else if (current.getEtype().equals("CG2")) {
 							CG2atom = current;
 						} else if (current.getEtype().equals("OG1")) {
 							OG1atom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CG2atom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CBatom, OG1atom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					CBatom.setAtomType(2);
 					CG2atom.setAtomType(4);
 					OG1atom.setAtomType(0);
 				} else if (restype == 'V') {
 					Atom CBatom = null;
 					Atom CG1atom = null;
 					Atom CG2atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype().equals("CB")) {
 							CBatom = current;
 						} else if (current.getEtype().equals("CG1")) {
 							CG1atom = current;
 						} else if (current.getEtype().equals("CG2")) {
 							CG2atom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CG1atom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CBatom, CG2atom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					CBatom.setAtomType(2);
 					CG1atom.setAtomType(4);
 					CG2atom.setAtomType(4);
 				} else if (restype == 'W') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom CD1atom = null;
 					Atom CD2atom = null;
 					Atom NE1atom = null;
 					Atom CE2atom = null;
 					Atom CE3atom = null;
 					Atom CZ2atom = null;
 					Atom CZ3atom = null;
 					Atom CH2atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype().equals("CB")) {
 							CBatom = current;
 						} else if (current.getEtype().equals("CG")) {
 							CGatom = current;
 						} else if (current.getEtype().equals("CD1")) {
 							CD1atom = current;
 						} else if (current.getEtype().equals("CD2")) {
 							CD2atom = current;
 						} else if (current.getEtype().equals("NE1")) {
 							NE1atom = current;
 						} else if (current.getEtype().equals("CE2")) {
 							CE2atom = current;
 						} else if (current.getEtype().equals("CE3")) {
 								CE3atom = current;
 						} else if (current.getEtype().equals("CZ2")) {
 							CZ2atom = current;
 						} else if (current.getEtype().equals("CZ3")) {
 							CZ3atom = current;
 						} else if (current.getEtype().equals("CH2")) {
 							CH2atom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, CD1atom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CGatom, CD2atom, Constants.BONDCONST);
 					Bond newb8 = new Bond(CD1atom, NE1atom, Constants.BONDCONST);
 					Bond newb9 = new Bond(CD2atom, CE2atom, Constants.BONDCONST);
 					Bond newb10 = new Bond(CD2atom, CE3atom, Constants.BONDCONST);
 					Bond newb11 = new Bond(CE2atom, CZ2atom, Constants.BONDCONST);
 					Bond newb12 = new Bond(CE3atom, CZ3atom, Constants.BONDCONST);
 					Bond newb13 = new Bond(CZ2atom, CH2atom, Constants.BONDCONST);
 					Bond newb14 = new Bond(CZ3atom, CH2atom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					bonds.add(newb8);
 					bonds.add(newb9);
 					bonds.add(newb10);
 					bonds.add(newb11);	
 					bonds.add(newb12);
 					bonds.add(newb13);
 					bonds.add(newb14);
 					CBatom.setAtomType(3);
 					CGatom.setAtomType(1);
 					CD1atom.setAtomType(5);
 					CD2atom.setAtomType(1);
 					NE1atom.setAtomType(0);
 					CE2atom.setAtomType(1);
 					CE3atom.setAtomType(5);
 					CZ2atom.setAtomType(5);
 					CZ3atom.setAtomType(5);
 					CH2atom.setAtomType(5);
 				} else if (restype == 'Y') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom CD1atom = null;
 					Atom CD2atom = null;
 					Atom CE1atom = null;
 					Atom CE2atom = null;
 					Atom CZatom = null;
 					Atom OHatom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype().equals("CB")) {
 							CBatom = current;
 						} else if (current.getEtype().equals("CG")) {
 							CGatom = current;
 						} else if (current.getEtype().equals("CD1")) {
 							CD1atom = current;
 						} else if (current.getEtype().equals("CD2")) {
 							CD2atom = current;
 						} else if (current.getEtype().equals("CE1")) {
 							CE1atom = current;
 						} else if (current.getEtype().equals("CE2")) {
 							CE2atom = current;
 						} else if (current.getEtype().equals("CZ")) {
 							CZatom = current;
 						} else if (current.getEtype().equals("OH")) {
 							OHatom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, CD1atom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CD1atom, CD2atom, Constants.BONDCONST);
 					Bond newb8 = new Bond(CD2atom, CZatom, Constants.BONDCONST);
 					Bond newb9 = new Bond(CZatom, CE1atom, Constants.BONDCONST);
 					Bond newb10 = new Bond(CE1atom, CE2atom, Constants.BONDCONST);
 					Bond newb11 = new Bond(CE2atom, CGatom, Constants.BONDCONST);
 					Bond newb12 = new Bond(CZatom, OHatom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					bonds.add(newb8);
 					bonds.add(newb9);
 					bonds.add(newb10);
 					bonds.add(newb11);	
 					bonds.add(newb12);
 					CBatom.setAtomType(3);
 					CGatom.setAtomType(1);
 					CD1atom.setAtomType(5);
 					CD2atom.setAtomType(5);
 					CE1atom.setAtomType(5);
 					CE2atom.setAtomType(5);
 					CZatom.setAtomType(1);
 					OHatom.setAtomType(0);
 				} else {
 					System.err.println("UNKNOWN RES TYPE " + restype);
 				}
 			}
 			while (true) {
 				AminoAcid.clear();
 				first = (Atom)structure.get(counter);
 				resnum = first.getResnum();
 				if (counter >= structure.size() - 1) {
 					break;
 				}
 				for (int i = counter; i < structure.size(); i++) {
 					Atom current = (Atom)structure.get(i);
 					if (current.getResnum() == first.getResnum()) {
 						AminoAcid.add(current);
 						counter = i;
 					} else {
 						counter = i;
 						break;
 					}
 				}
 				Catom = null;
 				CAatom = null;
 				Natom = null;
 				Oatom = null;
 				for (int i = 0; i < AminoAcid.size(); i++) {
 					Atom current = (Atom)AminoAcid.get(i);
 					if (current.getEtype().equals("C")) {
 						Catom = current;
 					} else if (current.getEtype().equals("CA")) {
 						CAatom = current;
 					} else if (current.getEtype().equals("N")) {
 						Natom = current;
 					} else if (current.getEtype().equals("O")) {
 						Oatom = current;
 					}
 				}
 				if (Catom != null && CAatom != null && Natom != null && Oatom != null && CatomLast != null) {
 					backbone.add(Catom);
 					backbone.add(CAatom);
 					backbone.add(Natom);
 					if (CatomLast.getChainnum() == Natom.getChainnum()) {
 						Bond newb0 = new Bond(CatomLast, Natom, Constants.BONDCONST);
 						bonds.add(newb0);
 						backbonebonds.add(newb0);
 					}
 					newb1 = new Bond(Natom, CAatom, Constants.BONDCONST);
 					newb2 = new Bond(CAatom, Catom, Constants.BONDCONST);
 					newb3 = new Bond(Catom, Oatom, Constants.BONDCONST);
 					bonds.add(newb1);
 					bonds.add(newb2);
 					bonds.add(newb3);
 					backbonebonds.add(newb1);
 					backbonebonds.add(newb2);
 					CatomLast = Catom;
					restype = translator2.get(Catom.getAA()).toString().charAt(0);
 					AminoAcid.remove(Catom);
 					AminoAcid.remove(CAatom);
 					AminoAcid.remove(Natom);
 					AminoAcid.remove(Oatom);
 					Catom.setAtomType(0);
 					CAatom.setAtomType(1);
 					Natom.setAtomType(2);
 					Oatom.setAtomType(1);
 					if (restype == 'A') {
 						Atom CBatom = AminoAcid.get(0);
 						Bond newb4 = new Bond(CBatom, CAatom, Constants.BONDCONST); 
 						bonds.add(newb4);
 						CBatom.setAtomType(4);
 					} else if (restype == 'C') {
 						Atom CBatom = null;
 						Atom SGatom = null;
 						for (int i = 0; i < AminoAcid.size(); i++) {
 							Atom current = (Atom)AminoAcid.get(i);
 							if (current.getEtype().equals("CB")) {
 								CBatom = current;
 							} else if (current.getEtype().equals("SG")) {
 								SGatom = current;
 							}
 						}
 						Bond newb4 = new Bond(CBatom, CAatom, Constants.BONDCONST);
 						Bond newb5 = new Bond(CBatom, SGatom, Constants.BONDCONST);
 						bonds.add(newb4);
 						bonds.add(newb5);
 						CBatom.setAtomType(3);
 						SGatom.setAtomType(1);
 					} else if (restype == 'D') {
 						Atom CBatom = null;
 						Atom CGatom = null;
 						Atom OD1atom = null;
 						Atom OD2atom = null;
 						for (int i = 0; i < AminoAcid.size(); i++) {
 							Atom current = (Atom)AminoAcid.get(i);
 							if (current.getEtype().equals("CB")) {
 								CBatom = current;
 							} else if (current.getEtype().equals("CG")) {
 								CGatom = current;
 							} else if (current.getEtype().equals("OD1")) {
 								OD1atom = current;
 							} else if (current.getEtype().equals("OD2")) {
 								OD2atom = current;
 							}
 						}
 						Bond newb7 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 						Bond newb4 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 						Bond newb5 = new Bond(CGatom, OD1atom, Constants.BONDCONST);
 						Bond newb6 = new Bond(CGatom, OD2atom, Constants.BONDCONST);
 						bonds.add(newb4);
 						bonds.add(newb5);
 						bonds.add(newb6);
 						bonds.add(newb7);
 						CBatom.setAtomType(3);
 						CGatom.setAtomType(0);
 						OD1atom.setAtomType(1);
 						OD2atom.setAtomType(0);
 					} else if (restype == 'E') {
 						Atom CBatom = null;
 						Atom CGatom = null;
 						Atom CDatom = null;
 						Atom OE1atom = null;
 						Atom OE2atom = null;
 						for (int i = 0; i < AminoAcid.size(); i++) {
 							Atom current = (Atom)AminoAcid.get(i);
 							if (current.getEtype().equals("CB")) {
 								CBatom = current;
 							} else if (current.getEtype().equals("CG")) {
 								CGatom = current;
 							} else if (current.getEtype().equals("CD")) {
 								CDatom = current;
 							} else if (current.getEtype().equals("OE1")) {
 								OE1atom = current;
 							} else if (current.getEtype().equals("OE2")) {
 								OE2atom = current;
 							}
 						}
 						Bond newb4 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 						Bond newb5 = new Bond(CDatom, OE1atom, Constants.BONDCONST);
 						Bond newb6 = new Bond(CDatom, OE2atom, Constants.BONDCONST);
 						Bond newb7 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 						Bond newb8 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 						bonds.add(newb4);
 						bonds.add(newb5);
 						bonds.add(newb6);
 						bonds.add(newb7);
 						bonds.add(newb8);
 						CBatom.setAtomType(3);
 						CGatom.setAtomType(3);
 						CDatom.setAtomType(0);
 						OE1atom.setAtomType(1);
 						OE2atom.setAtomType(0);
 					} else if (restype == 'F') {
 						Atom CBatom = null;
 						Atom CGatom = null;
 						Atom CD1atom = null;
 						Atom CD2atom = null;
 						Atom CE1atom = null;
 						Atom CE2atom = null;
 						Atom CZatom = null;
 						for (int i = 0; i < AminoAcid.size(); i++) {
 							Atom current = (Atom)AminoAcid.get(i);
 							if (current.getEtype().equals("CB")) {
 								CBatom = current;
 							} else if (current.getEtype().equals("CG")) {
 								CGatom = current;
 							} else if (current.getEtype().equals("CD1")) {
 								CD1atom = current;
 							} else if (current.getEtype().equals("CD2")) {
 								CD2atom = current;
 							} else if (current.getEtype().equals("CE1")) {
 								CE1atom = current;
 							} else if (current.getEtype().equals("CE2")) {
 								CE2atom = current;
 							} else if (current.getEtype().equals("CZ")) {
 								CZatom = current;
 							}
 						}
 						Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 						Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 						Bond newb6 = new Bond(CGatom, CD1atom, Constants.BONDCONST);
 						Bond newb7 = new Bond(CD1atom, CD2atom, Constants.BONDCONST);
 						Bond newb8 = new Bond(CD2atom, CZatom, Constants.BONDCONST);
 						Bond newb9 = new Bond(CZatom, CE1atom, Constants.BONDCONST);
 						Bond newb10 = new Bond(CE1atom, CE2atom, Constants.BONDCONST);
 						Bond newb11 = new Bond(CE2atom, CGatom, Constants.BONDCONST);
 						bonds.add(newb4);
 						bonds.add(newb5);
 						bonds.add(newb6);
 						bonds.add(newb7);
 						bonds.add(newb8);
 						bonds.add(newb9);
 						bonds.add(newb10);
 						bonds.add(newb11);
 						CBatom.setAtomType(3);
 						CGatom.setAtomType(1);
 						CD1atom.setAtomType(5);
 						CD2atom.setAtomType(5);
 						CZatom.setAtomType(5);
 						CE1atom.setAtomType(5);
 						CE2atom.setAtomType(5);
 					} else if (restype == 'G') {
 						//This is intentionally left blank.
 					} else if (restype == 'H') {
 						Atom CBatom = null;
 						Atom CGatom = null;
 						Atom ND1atom = null;
 						Atom CD2atom = null;
 						Atom CE1atom = null;
 						Atom NE2atom = null;
 						for (int i = 0; i < AminoAcid.size(); i++) {
 							Atom current = (Atom)AminoAcid.get(i);
 							if (current.getEtype().equals("CB")) {
 								CBatom = current;
 							} else if (current.getEtype().equals("CG")) {
 								CGatom = current;
 							} else if (current.getEtype().equals("ND1")) {
 								ND1atom = current;
 							} else if (current.getEtype().equals("CD2")) {
 								CD2atom = current;
 							} else if (current.getEtype().equals("CE1")) {
 								CE1atom = current;
 							} else if (current.getEtype().equals("NE2")) {
 								NE2atom = current;
 							}
 						}
 						Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 						Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 						Bond newb6 = new Bond(CGatom, ND1atom, Constants.BONDCONST);
 						Bond newb7 = new Bond(ND1atom, CD2atom, Constants.BONDCONST);
 						Bond newb8 = new Bond(CE1atom, NE2atom, Constants.BONDCONST);
 						Bond newb9 = new Bond(NE2atom, CGatom, Constants.BONDCONST);
 						bonds.add(newb4);
 						bonds.add(newb5);
 						bonds.add(newb6);
 						bonds.add(newb7);
 						bonds.add(newb8);
 						bonds.add(newb9);
 						CBatom.setAtomType(3);
 						CGatom.setAtomType(2);
 						ND1atom.setAtomType(0);
 						CD2atom.setAtomType(4);
 						CE1atom.setAtomType(4);
 						NE2atom.setAtomType(0);
 					} else if (restype == 'I') {
 						Atom CBatom = null;
 						Atom CG1atom = null;
 						Atom CG2atom = null;
 						Atom CD1atom = null;
 						for (int i = 0; i < AminoAcid.size(); i++) {
 							Atom current = (Atom)AminoAcid.get(i);
 							if (current.getEtype().equals("CB")) {
 								CBatom = current;
 							} else if (current.getEtype().equals("CG1")) {
 								CG1atom = current;
 							} else if (current.getEtype().equals("CG2")) {
 								CG2atom = current;
 							} else if (current.getEtype().equals("CD1")) {
 								CD1atom = current;
 							}
 						}
 						Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 						Bond newb5 = new Bond(CBatom, CG1atom, Constants.BONDCONST);
 						Bond newb6 = new Bond(CG1atom, CG2atom, Constants.BONDCONST);
 						Bond newb7 = new Bond(CBatom, CD1atom, Constants.BONDCONST);
 						bonds.add(newb4);
 						bonds.add(newb5);
 						bonds.add(newb6);
 						bonds.add(newb7);
 						CBatom.setAtomType(3);
 						CG1atom.setAtomType(3);
 						CG2atom.setAtomType(4);
 						CD1atom.setAtomType(4);
 					} else if (restype == 'K') {
 						Atom CBatom = null;
 						Atom CGatom = null;
 						Atom CDatom = null;
 						Atom CEatom = null;
 						Atom NZatom = null;
 						for (int i = 0; i < AminoAcid.size(); i++) {
 							Atom current = (Atom)AminoAcid.get(i);
 							if (current.getEtype().equals("CB")) {
 								CBatom = current;
 							} else if (current.getEtype().equals("CG")) {
 								CGatom = current;
 							} else if (current.getEtype().equals("CD")) {
 								CDatom = current;
 							} else if (current.getEtype().equals("CE")) {
 								CEatom = current;
 							} else if (current.getEtype().equals("NZ")) {
 								NZatom = current;
 							}
 						}
 						Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 						Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 						Bond newb6 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 						Bond newb7 = new Bond(CDatom, CEatom, Constants.BONDCONST);
 						Bond newb8 = new Bond(CEatom, NZatom, Constants.BONDCONST);
 						bonds.add(newb4);
 						bonds.add(newb5);
 						bonds.add(newb6);
 						bonds.add(newb7);
 						bonds.add(newb8);
 						CBatom.setAtomType(3);
 						CGatom.setAtomType(3);
 						CDatom.setAtomType(3);
 						CEatom.setAtomType(3);
 						NZatom.setAtomType(2);
 					} else if (restype == 'L') {
 						Atom CBatom = null;
 						Atom CGatom = null;
 						Atom CD2atom = null;
 						Atom CD1atom = null;
 						for (int i = 0; i < AminoAcid.size(); i++) {
 							Atom current = (Atom)AminoAcid.get(i);
 							if (current.getEtype().equals("CB")) {
 								CBatom = current;
 							} else if (current.getEtype().equals("CG")) {
 								CGatom = current;
 							} else if (current.getEtype().equals("CD2")) {
 								CD2atom = current;
 							} else if (current.getEtype().equals("CD1")) {
 								CD1atom = current;
 							}
 						}
 						Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 						Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 						Bond newb6 = new Bond(CGatom, CD2atom, Constants.BONDCONST);
 						Bond newb7 = new Bond(CGatom, CD1atom, Constants.BONDCONST);
 						bonds.add(newb4);
 						bonds.add(newb5);
 						bonds.add(newb6);
 						bonds.add(newb7);
 						CBatom.setAtomType(3);
 						CGatom.setAtomType(2);
 						CD2atom.setAtomType(4);
 						CD1atom.setAtomType(4);
 					} else if (restype == 'M') {
 						Atom CBatom = null;
 						Atom CGatom = null;
 						Atom SDatom = null;
 						Atom CEatom = null;
 						for (int i = 0; i < AminoAcid.size(); i++) {
 							Atom current = (Atom)AminoAcid.get(i);
 							if (current.getEtype().equals("CB")) {
 								CBatom = current;
 							} else if (current.getEtype().equals("CG")) {
 								CGatom = current;
 							} else if (current.getEtype().equals("SD")) {
 								SDatom = current;
 							} else if (current.getEtype().equals("CE")) {
 								CEatom = current;
 							}
 						}
 						Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 						Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 						Bond newb6 = new Bond(CGatom, SDatom, Constants.BONDCONST);
 						Bond newb7 = new Bond(SDatom, CEatom, Constants.BONDCONST);
 						bonds.add(newb4);
 						bonds.add(newb5);
 						bonds.add(newb6);
 						bonds.add(newb7);
 						CBatom.setAtomType(3);
 						CGatom.setAtomType(3);
 						SDatom.setAtomType(0);
 						CEatom.setAtomType(4);
 					} else if (restype == 'N') {
 						Atom CBatom = null;
 						Atom CGatom = null;
 						Atom OD1atom = null;
 						Atom ND2atom = null;
 						for (int i = 0; i < AminoAcid.size(); i++) {
 							Atom current = (Atom)AminoAcid.get(i);
 							if (current.getEtype().equals("CB")) {
 								CBatom = current;
 							} else if (current.getEtype().equals("CG")) {
 								CGatom = current;
 							} else if (current.getEtype().equals("OD1")) {
 								OD1atom = current;
 							} else if (current.getEtype().equals("ND2")) {
 								ND2atom = current;
 							}
 						}
 						Bond newb7 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 						Bond newb4 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 						Bond newb5 = new Bond(CGatom, OD1atom, Constants.BONDCONST);
 						Bond newb6 = new Bond(CGatom, ND2atom, Constants.BONDCONST);
 						bonds.add(newb4);
 						bonds.add(newb5);
 						bonds.add(newb6);
 						bonds.add(newb7);
 						CBatom.setAtomType(3);
 						CGatom.setAtomType(0);
 						OD1atom.setAtomType(1);
 						ND2atom.setAtomType(2);
 					} else if (restype == 'P') {
 						Atom CBatom = null;
 						Atom CGatom = null;
 						Atom CDatom = null;
 						for (int i = 0; i < AminoAcid.size(); i++) {
 							Atom current = (Atom)AminoAcid.get(i);
 							if (current.getEtype().equals("CB")) {
 								CBatom = current;
 							} else if (current.getEtype().equals("CG")) {
 								CGatom = current;
 							} else if (current.getEtype().equals("CD")) {
 								CDatom = current;
 							} 
 						}
 						Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 						Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 						Bond newb6 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 						Bond newb7 = new Bond(CDatom, Natom, Constants.BONDCONST);
 						bonds.add(newb4);
 						bonds.add(newb5);
 						bonds.add(newb6);
 						bonds.add(newb7);
 						Natom.setAtomType(5);
 						CBatom.setAtomType(3);
 						CGatom.setAtomType(3);
 						CDatom.setAtomType(3);
 					} else if (restype == 'Q') {
 						Atom CBatom = null;
 						Atom CGatom = null;
 						Atom CDatom = null;
 						Atom OE1atom = null;
 						Atom NE2atom = null;
 						for (int i = 0; i < AminoAcid.size(); i++) {
 							Atom current = (Atom)AminoAcid.get(i);
 							if (current.getEtype().equals("CB")) {
 								CBatom = current;
 							} else if (current.getEtype().equals("CG")) {
 								CGatom = current;
 							} else if (current.getEtype().equals("CD")) {
 								CDatom = current;
 							} else if (current.getEtype().equals("OE1")) {
 								OE1atom = current;
 							} else if (current.getEtype().equals("NE2")) {
 								NE2atom = current;
 							}
 						}
 						Bond newb4 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 						Bond newb5 = new Bond(CDatom, OE1atom, Constants.BONDCONST);
 						Bond newb6 = new Bond(CDatom, NE2atom, Constants.BONDCONST);
 						Bond newb7 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 						Bond newb8 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 						bonds.add(newb4);
 						bonds.add(newb5);
 						bonds.add(newb6);
 						bonds.add(newb7);
 						bonds.add(newb8);
 						CBatom.setAtomType(3);
 						CGatom.setAtomType(3);
 						CDatom.setAtomType(0);
 						OE1atom.setAtomType(1);
 						NE2atom.setAtomType(2);
 					} else if (restype == 'R') {
 						Atom CBatom = null;
 						Atom CGatom = null;
 						Atom CDatom = null;
 						Atom NEatom = null;
 						Atom CZatom = null;
 						Atom NH1atom = null;
 						Atom NH2atom = null;
 						for (int i = 0; i < AminoAcid.size(); i++) {
 							Atom current = (Atom)AminoAcid.get(i);
 							if (current.getEtype().equals("CB")) {
 								CBatom = current;
 							} else if (current.getEtype().equals("CG")) {
 								CGatom = current;
 							} else if (current.getEtype().equals("CD")) {
 								CDatom = current;
 							} else if (current.getEtype().equals("NE")) {
 								NEatom = current;
 							} else if (current.getEtype().equals("CZ")) {
 								CZatom = current;
 							} else if (current.getEtype().equals("NH1")) {
 								NH1atom = current;
 							} else if (current.getEtype().equals("NH2")) {
 								NH2atom = current;
 							}
 						}
 						Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 						Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 						Bond newb6 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 						Bond newb7 = new Bond(CDatom, NEatom, Constants.BONDCONST);
 						Bond newb8 = new Bond(NEatom, CZatom, Constants.BONDCONST);
 						Bond newb9 = new Bond(CZatom, NH1atom, Constants.BONDCONST);
 						Bond newb10 = new Bond(CZatom, NH2atom, Constants.BONDCONST);
 						bonds.add(newb4);
 						bonds.add(newb5);
 						bonds.add(newb6);
 						bonds.add(newb7);
 						bonds.add(newb8);
 						bonds.add(newb9);
 						bonds.add(newb10);
 						CBatom.setAtomType(3);
 						CGatom.setAtomType(3);
 						CDatom.setAtomType(3);
 						NEatom.setAtomType(0);
 						CZatom.setAtomType(2);
 						NH1atom.setAtomType(2);
 						NH2atom.setAtomType(4);
 					} else if (restype == 'S') {
 						Atom CBatom = null;
 						Atom OGatom = null;
 						for (int i = 0; i < AminoAcid.size(); i++) {
 							Atom current = (Atom)AminoAcid.get(i);
 							if (current.getEtype().equals("CB")) {
 								CBatom = current;
 							} else if (current.getEtype().equals("OG")) {
 								OGatom = current;
 							}
 						}
 						Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 						Bond newb5 = new Bond(CBatom, OGatom, Constants.BONDCONST);
 						bonds.add(newb4);
 						bonds.add(newb5);
 						CBatom.setAtomType(3);
 						OGatom.setAtomType(0);
 					} else if (restype == 'T') {
 						Atom CBatom = null;
 						Atom CG2atom = null;
 						Atom OG1atom = null;
 						for (int i = 0; i < AminoAcid.size(); i++) {
 							Atom current = (Atom)AminoAcid.get(i);
 							if (current.getEtype().equals("CB")) {
 								CBatom = current;
 							} else if (current.getEtype().equals("CG2")) {
 								CG2atom = current;
 							} else if (current.getEtype().equals("OG1")) {
 								OG1atom = current;
 							}
 						}
 						Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 						Bond newb5 = new Bond(CBatom, CG2atom, Constants.BONDCONST);
 						Bond newb6 = new Bond(CBatom, OG1atom, Constants.BONDCONST);
 						bonds.add(newb4);
 						bonds.add(newb5);
 						bonds.add(newb6);
 						CBatom.setAtomType(2);
 						CG2atom.setAtomType(4);
 						OG1atom.setAtomType(0);
 					} else if (restype == 'V') {
 						Atom CBatom = null;
 						Atom CG1atom = null;
 						Atom CG2atom = null;
 						for (int i = 0; i < AminoAcid.size(); i++) {
 							Atom current = (Atom)AminoAcid.get(i);
 							if (current.getEtype().equals("CB")) {
 								CBatom = current;
 							} else if (current.getEtype().equals("CG1")) {
 								CG1atom = current;
 							} else if (current.getEtype().equals("CG2")) {
 								CG2atom = current;
 							}
 						}
 						Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 						Bond newb5 = new Bond(CBatom, CG1atom, Constants.BONDCONST);
 						Bond newb6 = new Bond(CBatom, CG2atom, Constants.BONDCONST);
 						bonds.add(newb4);
 						bonds.add(newb5);
 						bonds.add(newb6);
 						CBatom.setAtomType(2);
 						CG1atom.setAtomType(4);
 						CG2atom.setAtomType(4);
 					} else if (restype == 'W') {
 						Atom CBatom = null;
 						Atom CGatom = null;
 						Atom CD1atom = null;
 						Atom CD2atom = null;
 						Atom NE1atom = null;
 						Atom CE2atom = null;
 						Atom CE3atom = null;
 						Atom CZ2atom = null;
 						Atom CZ3atom = null;
 						Atom CH2atom = null;
 						for (int i = 0; i < AminoAcid.size(); i++) {
 							Atom current = (Atom)AminoAcid.get(i);
 							if (current.getEtype().equals("CB")) {
 								CBatom = current;
 							} else if (current.getEtype().equals("CG")) {
 								CGatom = current;
 							} else if (current.getEtype().equals("CD1")) {
 								CD1atom = current;
 							} else if (current.getEtype().equals("CD2")) {
 								CD2atom = current;
 							} else if (current.getEtype().equals("NE1")) {
 								NE1atom = current;
 							} else if (current.getEtype().equals("CE2")) {
 								CE2atom = current;
 							} else if (current.getEtype().equals("CE3")) {
 								CE3atom = current;
 							} else if (current.getEtype().equals("CZ2")) {
 								CZ2atom = current;
 							} else if (current.getEtype().equals("CZ3")) {
 								CZ3atom = current;
 							} else if (current.getEtype().equals("CH2")) {
 								CH2atom = current;
 							}
 						}
 						Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 						Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 						Bond newb6 = new Bond(CGatom, CD1atom, Constants.BONDCONST);
 						Bond newb7 = new Bond(CGatom, CD2atom, Constants.BONDCONST);
 						Bond newb8 = new Bond(CD1atom, NE1atom, Constants.BONDCONST);
 						Bond newb9 = new Bond(CD2atom, CE2atom, Constants.BONDCONST);
 						Bond newb10 = new Bond(CD2atom, CE3atom, Constants.BONDCONST);
 						Bond newb11 = new Bond(CE2atom, CZ2atom, Constants.BONDCONST);
 						Bond newb12 = new Bond(CE3atom, CZ3atom, Constants.BONDCONST);
 						Bond newb13 = new Bond(CZ2atom, CH2atom, Constants.BONDCONST);
 						Bond newb14 = new Bond(CZ3atom, CH2atom, Constants.BONDCONST);
 						bonds.add(newb4);
 						bonds.add(newb5);
 						bonds.add(newb6);
 						bonds.add(newb7);
 						bonds.add(newb8);
 						bonds.add(newb9);
 						bonds.add(newb10);
 						bonds.add(newb11);	
 						bonds.add(newb12);
 						bonds.add(newb13);
 						bonds.add(newb14);
 						CBatom.setAtomType(3);
 						CGatom.setAtomType(1);
 						CD1atom.setAtomType(5);
 						CD2atom.setAtomType(1);
 						NE1atom.setAtomType(0);
 						CE2atom.setAtomType(1);
 						CE3atom.setAtomType(5);
 						CZ2atom.setAtomType(5);
 						CZ3atom.setAtomType(5);
 						CH2atom.setAtomType(5);
 					} else if (restype == 'Y') {
 						Atom CBatom = null;
 						Atom CGatom = null;
 						Atom CD1atom = null;
 						Atom CD2atom = null;
 						Atom CE1atom = null;
 						Atom CE2atom = null;
 						Atom CZatom = null;
 						Atom OHatom = null;
 						for (int i = 0; i < AminoAcid.size(); i++) {
 							Atom current = (Atom)AminoAcid.get(i);
 							if (current.getEtype().equals("CB")) {
 								CBatom = current;
 							} else if (current.getEtype().equals("CG")) {
 								CGatom = current;
 							} else if (current.getEtype().equals("CD1")) {
 								CD1atom = current;
 							} else if (current.getEtype().equals("CD2")) {
 								CD2atom = current;
 							} else if (current.getEtype().equals("CE1")) {
 								CE1atom = current;
 							} else if (current.getEtype().equals("CE2")) {
 								CE2atom = current;
 							} else if (current.getEtype().equals("CZ")) {
 								CZatom = current;
 							} else if (current.getEtype().equals("OH")) {
 								OHatom = current;
 							}
 						}
 						Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 						Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 						Bond newb6 = new Bond(CGatom, CD1atom, Constants.BONDCONST);
 						Bond newb7 = new Bond(CD1atom, CD2atom, Constants.BONDCONST);
 						Bond newb8 = new Bond(CD2atom, CZatom, Constants.BONDCONST);
 						Bond newb9 = new Bond(CZatom, CE1atom, Constants.BONDCONST);
 						Bond newb10 = new Bond(CE1atom, CE2atom, Constants.BONDCONST);
 						Bond newb11 = new Bond(CE2atom, CGatom, Constants.BONDCONST);
 						Bond newb12 = new Bond(CZatom, OHatom, Constants.BONDCONST);
 						bonds.add(newb4);
 						bonds.add(newb5);
 						bonds.add(newb6);
 						bonds.add(newb7);
 						bonds.add(newb8);
 						bonds.add(newb9);
 						bonds.add(newb10);
 						bonds.add(newb11);	
 						bonds.add(newb12);
 						CBatom.setAtomType(3);
 						CGatom.setAtomType(1);
 						CD1atom.setAtomType(5);
 						CD2atom.setAtomType(5);
 						CE1atom.setAtomType(5);
 						CE2atom.setAtomType(5);
 						CZatom.setAtomType(1);
 						OHatom.setAtomType(0);
 					} else {
 						System.err.println("UNKNOWN RES TYPE " + restype);
 					}
 				}
 			}
 			detSurfaceBonds();
 			detSurfaceBackbone();
 			detSurfaceBackboneBonds();
 			detSolvEModel();
 			out.println("DONE WITH DETERMINING BONDS AND BACKBONE");
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	public void detSurfaceBonds() throws Exception {
 		for (int i = 0; i < bonds.size(); i++) {
 			Bond current = (Bond)bonds.get(i);
 			Atom first = current.getFirst();
 			Atom second = current.getSecond();
 			if (isAlreadyIn(surface, first) && isAlreadyIn(surface, second)) {
 				surfacebonds.add(current);
 			}
 		}
 	}
 	public void detSurfaceBackbone() throws Exception {
 		for (int i = 0; i < backbone.size(); i++) {
 			Atom current = (Atom)backbone.get(i);
 			if (surface.contains(current)) {
 				surfacebackbone.add(current);
 			}
 		}
 	}
 	public void detSurfaceBackboneBonds() throws Exception {
 		for (int i = 0; i < backbonebonds.size(); i++) {
 			Bond current = (Bond)backbonebonds.get(i);
 			Atom first = current.getFirst();
 			Atom second = current.getSecond();
 			if (isAlreadyIn(surface, first) && isAlreadyIn(surface, second)) {
 				surfacebackbonebonds.add(current);
 			}
 		}
 	}
 	public void detSolvEModel() throws Exception {
 		for (int i = 0; i < surface.size(); i++) {
 			Atom current = (Atom)surface.get(i);
 			char cel = current.getElement();
 			int ctype = current.getAtomType();
 			if (cel == 'H') {
 				continue;
 			}
 			if (cel == 'C' && ctype == 0) {
 				solvEModel += Constants.Solv_Gref_C;
 				continue;
 			} else if (cel == 'C' && ctype == 1) {
 				solvEModel += Constants.Solv_Gref_CR;
 				continue;
 			} else if (cel == 'C' && ctype == 2) {
 				solvEModel += Constants.Solv_Gref_CH1E;
 				continue;
 			} else if (cel == 'C' && ctype == 3) {
 				solvEModel += Constants.Solv_Gref_CH2E;
 				continue;
 			} else if (cel == 'C' && ctype == 4) {
 				solvEModel += Constants.Solv_Gref_CH3E;
 				continue;
 			} else if (cel == 'C' && ctype == 5) {
 				solvEModel += Constants.Solv_Gref_CR1E;
 				continue;
 			} else if (cel == 'N' && ctype == 0) {
 				solvEModel += Constants.Solv_Gref_NH1;
 				continue;
 			} else if (cel == 'N' && ctype == 1) {
 				solvEModel += Constants.Solv_Gref_NR;
 				continue;
 			} else if (cel == 'N' && ctype == 2) {
 				solvEModel += Constants.Solv_Gref_NH2;
 				continue;
 			} else if (cel == 'N' && ctype == 3) {
 				solvEModel += Constants.Solv_Gref_NH3;
 				continue;
 			} else if (cel == 'N' && ctype == 4) {
 				solvEModel += Constants.Solv_Gref_NC2;
 				continue;
 			} else if (cel == 'N' && ctype == 5) {
 				solvEModel += Constants.Solv_Gref_N;
 				continue;
 			} else if (cel == 'O' && ctype == 0) {
 				solvEModel += Constants.Solv_Gref_OH1;
 				continue;
 			} else if (cel == 'O' && ctype == 1) {
 				solvEModel += Constants.Solv_Gref_O;
 				continue;
 			} else if (cel == 'O' && ctype == 2) {
 				solvEModel += Constants.Solv_Gref_OC;
 				continue;
 			} else if (cel == 'S' && ctype == 0) {
 				solvEModel += Constants.Solv_Gref_S;
 				continue;
 			} else if (cel == 'S' && ctype == 1) {
 				solvEModel += Constants.Solv_Gref_SH1E;
 			}
 		}
 	}
 	public void detPotentials() throws Exception{
 		for (int i = 0; i < surface.size(); i++) {
 			Atom current = (Atom)surface.get(i);
 			double cx = current.getXcoord();
 			double cy = current.getYcoord();
 			double cz = current.getZcoord();
 			char cel = current.getElement();
 			int ctype = current.getAtomType();
 			for (double j = -round(size, Constants.GRIDGRAINSIZE) - Constants.VDWDISTTHRESHOLD + round(xcoordcent, Constants.GRIDGRAINSIZE); j < round(size, Constants.GRIDGRAINSIZE) - Constants.VDWDISTTHRESHOLD + round(xcoordcent, Constants.GRIDGRAINSIZE); j += Constants.GRIDGRAINSIZE) {
 				for (double k = -round(size, Constants.GRIDGRAINSIZE) - Constants.VDWDISTTHRESHOLD + round(ycoordcent, Constants.GRIDGRAINSIZE); k < round(size, Constants.GRIDGRAINSIZE) - Constants.VDWDISTTHRESHOLD + round(ycoordcent, Constants.GRIDGRAINSIZE); k += Constants.GRIDGRAINSIZE) {
 					for (double l = -round(size, Constants.GRIDGRAINSIZE) - Constants.VDWDISTTHRESHOLD + round(zcoordcent, Constants.GRIDGRAINSIZE); l < round(size, Constants.GRIDGRAINSIZE) - Constants.VDWDISTTHRESHOLD + round(zcoordcent, Constants.GRIDGRAINSIZE); l += Constants.GRIDGRAINSIZE) {
 						double distance = Math.sqrt(Math.pow(cx - j, 2) + Math.pow(cy - k, 2) + Math.pow(cz - l, 2));
 						if (distance < Constants.VDWDISTTHRESHOLD) {
 							double EC = 0.;
 							double EN = 0.;
 							double EO = 0.;
 							double ES = 0.;
 							double EH = 0.;
 							if (cel == 'C') {
 								EC = Constants.C12_C_C/Math.pow(distance, 12.) - Constants.C6_C_C/Math.pow(distance, 6.);
 								EN = Constants.C12_C_N/Math.pow(distance, 12.) - Constants.C6_C_N/Math.pow(distance, 6.);
 								EO = Constants.C12_C_O/Math.pow(distance, 12.) - Constants.C6_C_O/Math.pow(distance, 6.);
 								ES = Constants.C12_C_S/Math.pow(distance, 12.) - Constants.C6_C_S/Math.pow(distance, 6.);
 								EH = Constants.C12_C_H/Math.pow(distance, 12.) - Constants.C6_C_H/Math.pow(distance, 6.);
 							} else if (cel == 'N') {
 								EC = Constants.C12_C_N/Math.pow(distance, 12.) - Constants.C6_C_N/Math.pow(distance, 6.);
 								EN = Constants.C12_N_N/Math.pow(distance, 12.) - Constants.C6_N_N/Math.pow(distance, 6.);
 								EO = Constants.C12_N_O/Math.pow(distance, 12.) - Constants.C6_N_O/Math.pow(distance, 6.);
 								ES = Constants.C12_N_S/Math.pow(distance, 12.) - Constants.C6_N_S/Math.pow(distance, 6.);
 								EH = Constants.C12_N_H/Math.pow(distance, 12.) - Constants.C6_N_H/Math.pow(distance, 6.);
 							} else if (cel == 'O') {
 								EC = Constants.C12_C_O/Math.pow(distance, 12.) - Constants.C6_C_O/Math.pow(distance, 6.);
 								EN = Constants.C12_N_O/Math.pow(distance, 12.) - Constants.C6_N_O/Math.pow(distance, 6.);
 								EO = Constants.C12_O_O/Math.pow(distance, 12.) - Constants.C6_O_O/Math.pow(distance, 6.);
 								ES = Constants.C12_O_S/Math.pow(distance, 12.) - Constants.C6_O_S/Math.pow(distance, 6.);
 								EH = Constants.C12_O_H/Math.pow(distance, 12.) - Constants.C6_O_H/Math.pow(distance, 6.);
 							} else if (cel == 'S') {
 								EC = Constants.C12_C_S/Math.pow(distance, 12.) - Constants.C6_C_S/Math.pow(distance, 6.);
 								EN = Constants.C12_N_S/Math.pow(distance, 12.) - Constants.C6_N_S/Math.pow(distance, 6.);
 								EO = Constants.C12_O_S/Math.pow(distance, 12.) - Constants.C6_O_S/Math.pow(distance, 6.);
 								ES = Constants.C12_S_S/Math.pow(distance, 12.) - Constants.C6_S_S/Math.pow(distance, 6.);
 								EH = Constants.C12_S_H/Math.pow(distance, 12.) - Constants.C6_S_H/Math.pow(distance, 6.);
 							} else if (cel == 'H') {
 								EC = Constants.C12_C_H/Math.pow(distance, 12.) - Constants.C6_C_H/Math.pow(distance, 6.);
 								EN = Constants.C12_N_H/Math.pow(distance, 12.) - Constants.C6_N_H/Math.pow(distance, 6.);
 								EO = Constants.C12_O_H/Math.pow(distance, 12.) - Constants.C6_O_H/Math.pow(distance, 6.);
 								ES = Constants.C12_S_H/Math.pow(distance, 12.) - Constants.C6_S_H/Math.pow(distance, 6.);
 								EH = Constants.C12_H_H/Math.pow(distance, 12.) - Constants.C6_H_H/Math.pow(distance, 6.);
 							}
 							potentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][0] += EC;
 							potentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][1] += EN;
 							potentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][2] += EO;
 							potentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][3] += ES;
 							potentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][4] += EH;
 							if (cel == 'H') {
 								continue;
 							}
 							double eDens = 0;
 							if (cel == 'C' && ctype == 0) {
 								eDens = 2.*Constants.Solv_Gfree_C/(Math.sqrt(Math.PI)*Constants.Solv_Corr)*Math.pow(Math.E, -Math.pow((distance - Constants.Vdw_Rad_C)/Constants.Solv_Corr, 2.))/(4.*Math.PI*Math.pow(distance, 2.));
 							} else if (cel == 'C' && ctype == 1) {
 								eDens = 2.*Constants.Solv_Gfree_CR/(Math.sqrt(Math.PI)*Constants.Solv_Corr)*Math.pow(Math.E, -Math.pow((distance - Constants.Vdw_Rad_C)/Constants.Solv_Corr, 2.))/(4.*Math.PI*Math.pow(distance, 2.));
 							} else if (cel == 'C' && ctype == 2) {
 								eDens = 2.*Constants.Solv_Gfree_CH1E/(Math.sqrt(Math.PI)*Constants.Solv_Corr)*Math.pow(Math.E, -Math.pow((distance - Constants.Vdw_Rad_C)/Constants.Solv_Corr, 2.))/(4.*Math.PI*Math.pow(distance, 2.));
 							} else if (cel == 'C' && ctype == 3) {
 								eDens = 2.*Constants.Solv_Gfree_CH2E/(Math.sqrt(Math.PI)*Constants.Solv_Corr)*Math.pow(Math.E, -Math.pow((distance - Constants.Vdw_Rad_C)/Constants.Solv_Corr, 2.))/(4.*Math.PI*Math.pow(distance, 2.));
 							} else if (cel == 'C' && ctype == 4) {
 								eDens = 2.*Constants.Solv_Gfree_CH3E/(Math.sqrt(Math.PI)*Constants.Solv_Corr)*Math.pow(Math.E, -Math.pow((distance - Constants.Vdw_Rad_C)/Constants.Solv_Corr, 2.))/(4.*Math.PI*Math.pow(distance, 2.));
 							} else if (cel == 'C' && ctype == 5) {
 								eDens = 2.*Constants.Solv_Gfree_CR1E/(Math.sqrt(Math.PI)*Constants.Solv_Corr)*Math.pow(Math.E, -Math.pow((distance - Constants.Vdw_Rad_C)/Constants.Solv_Corr, 2.))/(4.*Math.PI*Math.pow(distance, 2.));
 							} else if (cel == 'N' && ctype == 0) {
 								eDens = 2.*Constants.Solv_Gfree_NH1/(Math.sqrt(Math.PI)*Constants.Solv_Corr)*Math.pow(Math.E, -Math.pow((distance - Constants.Vdw_Rad_N)/Constants.Solv_Corr, 2.))/(4.*Math.PI*Math.pow(distance, 2.));
 							} else if (cel == 'N' && ctype == 1) {
 								eDens = 2.*Constants.Solv_Gfree_NR/(Math.sqrt(Math.PI)*Constants.Solv_Corr)*Math.pow(Math.E, -Math.pow((distance - Constants.Vdw_Rad_N)/Constants.Solv_Corr, 2.))/(4.*Math.PI*Math.pow(distance, 2.));
 							} else if (cel == 'N' && ctype == 2) {
 								eDens = 2.*Constants.Solv_Gfree_NH2/(Math.sqrt(Math.PI)*Constants.Solv_Corr)*Math.pow(Math.E, -Math.pow((distance - Constants.Vdw_Rad_N)/Constants.Solv_Corr, 2.))/(4.*Math.PI*Math.pow(distance, 2.));
 							} else if (cel == 'N' && ctype == 3) {
 								eDens = 2.*Constants.Solv_Gfree_NH3/(Math.sqrt(Math.PI)*Constants.Solv_Corr)*Math.pow(Math.E, -Math.pow((distance - Constants.Vdw_Rad_N)/Constants.Solv_Corr, 2.))/(4.*Math.PI*Math.pow(distance, 2.));
 							} else if (cel == 'N' && ctype == 4) {
 								eDens = 2.*Constants.Solv_Gfree_NC2/(Math.sqrt(Math.PI)*Constants.Solv_Corr)*Math.pow(Math.E, -Math.pow((distance - Constants.Vdw_Rad_N)/Constants.Solv_Corr, 2.))/(4.*Math.PI*Math.pow(distance, 2.));
 							} else if (cel == 'N' && ctype == 5) {
 								eDens = 2.*Constants.Solv_Gfree_N/(Math.sqrt(Math.PI)*Constants.Solv_Corr)*Math.pow(Math.E, -Math.pow((distance - Constants.Vdw_Rad_N)/Constants.Solv_Corr, 2.))/(4.*Math.PI*Math.pow(distance, 2.));
 							} else if (cel == 'O' && ctype == 0) {
 								eDens = 2.*Constants.Solv_Gfree_OH1/(Math.sqrt(Math.PI)*Constants.Solv_Corr)*Math.pow(Math.E, -Math.pow((distance - Constants.Vdw_Rad_O)/Constants.Solv_Corr, 2.))/(4.*Math.PI*Math.pow(distance, 2.));
 							} else if (cel == 'O' && ctype == 1) {
 								eDens = 2.*Constants.Solv_Gfree_O/(Math.sqrt(Math.PI)*Constants.Solv_Corr)*Math.pow(Math.E, -Math.pow((distance - Constants.Vdw_Rad_O)/Constants.Solv_Corr, 2.))/(4.*Math.PI*Math.pow(distance, 2.));
 							} else if (cel == 'O' && ctype == 2) {
 								eDens = 2.*Constants.Solv_Gfree_OC/(Math.sqrt(Math.PI)*Constants.Solv_Corr)*Math.pow(Math.E, -Math.pow((distance - Constants.Vdw_Rad_O)/Constants.Solv_Corr, 2.))/(4.*Math.PI*Math.pow(distance, 2.));
 							} else if (cel == 'S' && ctype == 0) {
 								eDens = 2.*Constants.Solv_Gfree_S/(Math.sqrt(Math.PI)*Constants.Solv_Corr)*Math.pow(Math.E, -Math.pow((distance - Constants.Vdw_Rad_S)/Constants.Solv_Corr, 2.))/(4.*Math.PI*Math.pow(distance, 2.));
 							} else if (cel == 'S' && ctype == 1) {
 								eDens = 2.*Constants.Solv_Gfree_SH1E/(Math.sqrt(Math.PI)*Constants.Solv_Corr)*Math.pow(Math.E, -Math.pow((distance - Constants.Vdw_Rad_S)/Constants.Solv_Corr, 2.))/(4.*Math.PI*Math.pow(distance, 2.));
 							}
 							solvpotentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][0] += eDens*Constants.Solv_V_C;
 							solvpotentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][1] += eDens*Constants.Solv_V_CR;
 							solvpotentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][2] += eDens*Constants.Solv_V_CH1E;
 							solvpotentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][3] += eDens*Constants.Solv_V_CH2E;
 							solvpotentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][4] += eDens*Constants.Solv_V_CH3E;
 							solvpotentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][5] += eDens*Constants.Solv_V_CR1E;
 							solvpotentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][6] += eDens*Constants.Solv_V_NH1;
 							solvpotentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][7] += eDens*Constants.Solv_V_NR;
 							solvpotentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][8] += eDens*Constants.Solv_V_NH2;
 							solvpotentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][9] += eDens*Constants.Solv_V_NH3;
 							solvpotentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][10] += eDens*Constants.Solv_V_NC2;
 							solvpotentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][11] += eDens*Constants.Solv_V_N;
 							solvpotentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][12] += eDens*Constants.Solv_V_OH1;
 							solvpotentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][13] += eDens*Constants.Solv_V_O;
 							solvpotentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][14] += eDens*Constants.Solv_V_OC;
 							solvpotentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][15] += eDens*Constants.Solv_V_S;
 							solvpotentials[(int)((j+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(xcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((k+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(ycoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][(int)((l+round(size, Constants.GRIDGRAINSIZE)+Constants.VDWDISTTHRESHOLD - round(zcoordcent, Constants.GRIDGRAINSIZE))/Constants.GRIDGRAINSIZE)][16] += eDens*Constants.Solv_V_SH1E;
 						}
 					}
 				}
 			}
 		}
 		out.println("DONE WITH DETERMINING POTENTIALS");
 	}
 	public String removeSpace(String test) {
 		String answer = "";
 		for (int i = 0; i < test.length(); i++) {
 			if (test.charAt(i) != ' ') {
 				answer = answer + test.charAt(i);
 			}
 		}
 		return answer;
 	}
 	public String getFilepath() {
 		return filepath;
 	}
 	public ArrayList<Atom> getStructurea() {
 		return structurea;
 	}
 	public ImmutableArrayList<Atom> getStructure() {
 		return structure;
 	}
 	public ArrayList<Atom> getSurface() {
 		return surface;
 	}
 	public ArrayList<Bond> getBonds() {
 		return bonds;
 	}
 	public ArrayList<Bond> getSurfaceBonds() {
 		return surfacebonds;
 	}
 	public ArrayList<Atom> getBackbone() {
 		return backbone;
 	}
 	public ArrayList<Atom> getSurfaceBackbone() {
 		return surfacebackbone;
 	}
 	public ArrayList<Bond> getBackboneBonds() {
 		return backbonebonds;
 	}
 	public ArrayList<Bond> getSurfaceBackboneBonds() {
 		return surfacebackbonebonds;
 	}
 	public double getXCoordCent() {
 		return xcoordcent;
 	}
 	public double getYCoordCent() {
 		return ycoordcent;
 	}
 	public double getZCoordCent() {
 		return zcoordcent;
 	}
 	public int getChaincount() {
 		return chaincount;
 	}
 	public boolean getRotated() {
 		return rotated;
 	}
 	public double getSize() {
 		return size;
 	}
 	public double[] getCent() {
 		double[] centcoords = new double[3];
 		centcoords[0] = xcoordcent;
 		centcoords[1] = ycoordcent;
 		centcoords[2] = zcoordcent;
 		return centcoords;
 	}
 	public double[][][] getNewSizes() {
 		return newsizes;
 	}
 	public double[][][][] getPotentials() {
 		return potentials;
 	}
 	public double[][][][] getSolvPotentials() {
 		return solvpotentials;
 	}
 	public double getSolvEModel() {
 		return solvEModel;
 	}
 	public double round(double number, double roundTo) {
 		return (double)(Math.round(number/roundTo)*roundTo);
 	}
 	public ProteinStruct trans(double xmov, double ymov, double zmov) throws Exception {
 		ArrayList<Atom> newstruct = new ArrayList<Atom>();
 		for (int i = 0; i < surface.size(); i++) {
 			Atom current = (Atom)surface.get(i);
 			Atom trcurrent = current.transAtom(xmov, ymov, zmov);
 			newstruct.add(trcurrent);
 		}
 		ArrayList<Atom> newbackbone = new ArrayList<Atom>();
 		for (int i = 0; i < surfacebackbone.size(); i++) {
 			Atom current = (Atom)surfacebackbone.get(i);
 			Atom trcurrent = current.transAtom(xmov, ymov, zmov);
 			newbackbone.add(trcurrent);
 		}
 		ProteinStruct answer = new ProteinStruct(newstruct, surfacebonds, newbackbone, surfacebackbonebonds, size, newsizes, potentials, solvpotentials, solvEModel, chaincount, xcoordcent + xmov, ycoordcent + ymov, zcoordcent + zmov);
 		return answer;
 	} 
 	public ProteinStruct transall(double xmov, double ymov, double zmov) throws Exception {
 		ArrayList<Atom> newstruct = new ArrayList<Atom>();
 		for (int i = 0; i < structure.size(); i++) {
 			Atom current = (Atom)structure.get(i);
 			Atom trcurrent = current.transAtom(xmov, ymov, zmov);
 			newstruct.add(trcurrent);
 		}
 		ArrayList<Atom> newbackbone = new ArrayList<Atom>();
 		for (int i = 0; i < backbone.size(); i++) {
 			Atom current = (Atom)backbone.get(i);
 			Atom trcurrent = current.transAtom(xmov, ymov, zmov);
 			newbackbone.add(trcurrent);
 		}
 		ProteinStruct answer = new ProteinStruct(newstruct, bonds, newbackbone, backbonebonds, size, newsizes, potentials, solvpotentials, solvEModel, chaincount, xcoordcent + xmov, ycoordcent + ymov, zcoordcent + zmov);
 		return answer;
 	}
 	public ProteinStruct transrotnew(double xmov, double ymov, double zmov, double alpha, double beta, double gamma) throws Exception {
 		ArrayList<Atom> newstruct = new ArrayList<Atom>();
 		for (int i = 0; i < surface.size(); i++) {
 			Atom current = (Atom)surface.get(i);
 			Atom trcurrent = current.transAtom(xmov, ymov, zmov).rotateAtomNew(xcoordcent + xmov, ycoordcent + ymov, zcoordcent + zmov, alpha, beta, gamma);
 			newstruct.add(trcurrent);
 		}
 		ArrayList<Atom> newbackbone = new ArrayList<Atom>();
 		for (int i = 0; i < surfacebackbone.size(); i++) {
 			Atom current = (Atom)surfacebackbone.get(i);
 			Atom trcurrent = current.transAtom(xmov, ymov, zmov).rotateAtomNew(xcoordcent + xmov, ycoordcent + ymov, zcoordcent + zmov, alpha, beta, gamma);
 			newbackbone.add(trcurrent);
 		}
 		ProteinStruct answer = new ProteinStruct(newstruct, surfacebonds, newbackbone, surfacebackbonebonds, size, newsizes, potentials, solvpotentials, solvEModel, chaincount, xcoordcent + xmov, ycoordcent + ymov, zcoordcent + zmov);
 		return answer;
 	}
 	public ProteinStruct transrotnewall(double xmov, double ymov, double zmov, double alpha, double beta, double gamma) throws Exception {
 		ArrayList<Atom> newstruct = new ArrayList<Atom>();
 		for (int i = 0; i < structure.size(); i++) {
 			Atom current = (Atom)structure.get(i);
 			Atom trcurrent = current.transAtom(xmov, ymov, zmov).rotateAtomNew(xcoordcent + xmov, ycoordcent + ymov, zcoordcent + zmov, alpha, beta, gamma);
 			newstruct.add(trcurrent);
 		}
 		ArrayList<Atom> newbackbone = new ArrayList<Atom>();
 		for (int i = 0; i < backbone.size(); i++) {
 			Atom current = (Atom)backbone.get(i);
 			Atom trcurrent = current.transAtom(xmov, ymov, zmov).rotateAtomNew(xcoordcent + xmov, ycoordcent + ymov, zcoordcent + zmov, alpha, beta, gamma);
 			newbackbone.add(trcurrent);
 		}
 		ProteinStruct answer = new ProteinStruct(newstruct, surfacebonds, newbackbone, surfacebackbonebonds, size, newsizes, potentials, solvpotentials, solvEModel, chaincount, xcoordcent + xmov, ycoordcent + ymov, zcoordcent + zmov);
 		return answer;
 	}
 	public void printStructure() {
 		out.println("BEGIN STRUCTURE OF " + filepath);
 		for (int i = 0; i < structure.size(); i++) {
 			Atom current = (Atom)structure.get(i);
 			current.printAtom();
 		}
 		out.println("END STRUCTURE OF " + filepath);
 	}
 	public void printSurface() {
 		for (int i = 0; i < surface.size(); i++) {
 			Atom current = (Atom)surface.get(i);
 			current.printAtomPDB(out);
 		}
 	}
 	public void printStructurePDB(PrintWriter out) {
 		char currentchain = ' ';
 		Atom last = null;
 		for (int i = 0; i < structure.size(); i++) {
 			Atom current = (Atom)structure.get(i);
 			if (currentchain != current.getChainnum() && i != 0) {
 				String toBePrinted = "TER   ";
 				int length = Integer.toString(last.getAtomnum()+1).length();
 				int atomnum = last.getAtomnum()+1;
 				if (length == 5) { 
 					toBePrinted = toBePrinted.concat(Integer.toString(atomnum));
 				} else if (length == 4) {
 					toBePrinted = toBePrinted.concat(" ").concat(Integer.toString(atomnum));
 				} else if (length == 3) {
 					toBePrinted = toBePrinted.concat("  ").concat(Integer.toString(atomnum));
 				} else if (length == 2) {
 					toBePrinted = toBePrinted.concat("   ").concat(Integer.toString(atomnum));
 				} else if (length == 1) {
 					toBePrinted = toBePrinted.concat("    ").concat(Integer.toString(atomnum));
 				}
 				toBePrinted = toBePrinted.concat("      ");
 				toBePrinted = toBePrinted.concat(last.getAA());
 				toBePrinted = toBePrinted.concat(" ").concat(Character.toString(last.getChainnum()));
 				String resnum = Integer.toString(last.getResnum());
 				if (resnum.length() == 4) {
 					toBePrinted = toBePrinted.concat(resnum);
 				} else if (resnum.length() == 3) {
 					toBePrinted = toBePrinted.concat(" ").concat(resnum);
 				} else if (resnum.length() == 2) {
 					toBePrinted = toBePrinted.concat("  ").concat(resnum);
 				} else if (resnum.length() == 1) {
 					toBePrinted = toBePrinted.concat("   ").concat(resnum);
 				}
 				out.println(toBePrinted);
 			}
 			currentchain = current.getChainnum();
 			current.printAtomPDB(out);
 			last = current;
 		}
 		String toBePrinted = "TER   ";
 		int length = Integer.toString(last.getAtomnum()+1).length();
 		int atomnum = last.getAtomnum()+1;
 		if (length == 5) { 
 			toBePrinted = toBePrinted.concat(Integer.toString(atomnum));
 		} else if (length == 4) {
 			toBePrinted = toBePrinted.concat(" ").concat(Integer.toString(atomnum));
 		} else if (length == 3) {
 			toBePrinted = toBePrinted.concat("  ").concat(Integer.toString(atomnum));
 		} else if (length == 2) {
 			toBePrinted = toBePrinted.concat("   ").concat(Integer.toString(atomnum));
 		} else if (length == 1) {
 			toBePrinted = toBePrinted.concat("    ").concat(Integer.toString(atomnum));
 		}
 		toBePrinted = toBePrinted.concat("      ");
 		toBePrinted = toBePrinted.concat(last.getAA());
 		toBePrinted = toBePrinted.concat(" ").concat(Character.toString(last.getChainnum()));
 		String resnum = Integer.toString(last.getResnum());
 		if (resnum.length() == 4) {
 			toBePrinted = toBePrinted.concat(resnum);
 		} else if (resnum.length() == 3) {
 			toBePrinted = toBePrinted.concat(" ").concat(resnum);
 		} else if (resnum.length() == 2) {
 			toBePrinted = toBePrinted.concat("  ").concat(resnum);
 		} else if (resnum.length() == 1) {
 			toBePrinted = toBePrinted.concat("   ").concat(resnum);
 		}
 		out.println(toBePrinted);
 		out.flush();
 	}
 	public Atom getAtomByNum(int atomnum) {
 		for (int i = 0; i < surface.size(); i++) {
 			Atom current = (Atom)surface.get(i);
 			if (current.getAtomnum() == atomnum) {
 				return current;
 			}
 		}
 		return null;
 	}
 	public boolean isAlreadyIn(ArrayList<Atom> surface, Atom test) {
 		double cx = test.getXcoord();
 		double cy = test.getYcoord();
 		double cz = test.getZcoord();
 		for (int i = 0; i < surface.size(); i++) {
 			Atom trial = (Atom)surface.get(i);
 			double tx = trial.getXcoord();
 			double ty = trial.getYcoord();
 			double tz = trial.getZcoord();
 			if (Math.abs(tx - cx) <= Constants.FPPRECISION && Math.abs(ty - cy) <= Constants.FPPRECISION && Math.abs(tz - cz) <= Constants.FPPRECISION && trial.getEtype().equals(test.getEtype()) && test.getAtomnum() == trial.getAtomnum()) {
 				return true;
 			}
 		}
 		return false;
 	}
 }
