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
 
 public class Drug implements Comparable {
 	private String code;
 	private String id;
 	private String target;
 	private String targetChain;
 	private ArrayList<ArrayList<Atom>> interfacesBase;
 	private TestCase[] casesBase;
 	private double baseSum;
 	private ArrayList<ArrayList<Atom>> interfacesTest;
 	private ProteinDockPredict pdp;
 	private TestCase[] casesA;
 	private double testSum;
 	private double similarity;
 	public Drug(String code, String id, String target, String targetChain, String sourcepath, String pdbpath, ArrayList<ArrayList<Atom>> interfacesBase, TestCase[] casesBase, double baseSum) throws Exception {
 		this.code = code;
 		this.id = id;
 		this.target = target;
 		this.targetChain = targetChain;
 		this.interfacesBase = interfacesBase;
 		this.casesBase = casesBase;
 		this.baseSum = baseSum;
 		String filesep = System.getProperty("file.separator");
 		pdp = new ProteinDockPredict(sourcepath, pdbpath, "prot" + target + "to" + code, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", target, targetChain, id, "ALL", 4, Constants.DRUGNUMCASES);
 		TestCaseStore<TestCase> cases = pdp.getCases();
 		casesA = cases.toTCArray();
 		Arrays.sort(casesA);
 		interfacesTest = new ArrayList<ArrayList<Atom>>();
 		testSum = 0;
 		for (int i = 0; i < casesA.length; i++) {
 			if (casesA[i].getProbability() > 1) {
 				interfacesTest.add(casesA[i].getInterfacea());
 				testSum += casesA[i].getProbability();
 			}
 		}
 		similarity = similarity();
 	}
 	public double similarity() {
 		double similarity = 0;
 		for (int i = 0; i < interfacesBase.size(); i++) {
 			for (int j = 0; j < interfacesTest.size(); j++) {
 				if (casesA[j].getProbability() < 1) continue;
 				if (casesBase[i].getProbability() < 1) continue;
				similarity += casesBase[i].getProbability()/baseSum * casesA[j].getProbability()/testSum * similarityOne(interfacesTest.get(j), interfacesBase.get(i));
 			}
 		}
 		return similarity;
 	}
 	public double similarityOne(ArrayList<Atom> interfaceOne, ArrayList<Atom> interfaceTwo) {
 		int count = 0;
 		for (int i = 0; i < interfaceOne.size(); i++) {
 			Atom test = interfaceOne.get(i);
 			for (int j = 0; j < interfaceTwo.size(); j++) {
 				if (test.same(interfaceTwo.get(j))) {
 					count++;
 					break;
 				}
 			}
 		}
 		return ((double)count)/interfaceOne.size();
 	}
 	public double getSimilarity() {
 		return similarity;
 	}
 	public int compareTo(Object other) {
 		Drug dother = (Drug)other;
 		return (int)(similarity - dother.getSimilarity());
 	}
 	public String toString() {
 		return code + " " + id + " " + similarity;
 	}
 }
