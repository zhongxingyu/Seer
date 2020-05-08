 package datastructures.random;
 
 /**
  * Copyright (C) <2011>
  * 
  * @author Jonas Ruef & Felix Langenegger <cis.unibe@gmail.com>
  * @license GPLv3, see Readme.mdown
  */
 
 import java.util.ArrayList;
 
 import algorithms.cna.CNAException;
 
 public class RandomMTGeneratorHelper {
     private ArrayList<ArrayList<Object>> completeList = new ArrayList<ArrayList<Object>>();
     private ArrayList<ArrayList<Integer>> bundleSizesLevels;
     private ArrayList<Integer> alterFactors;
     private boolean makeEpi;
 
     public RandomMTGeneratorHelper(
 	    ArrayList<ArrayList<Integer>> bundleSizesLeveled,
 	    ArrayList<Integer> alterFactors, boolean makeEpi)
 	    throws CNAException {
 	this.bundleSizesLevels = bundleSizesLeveled;
 	this.alterFactors = alterFactors;
 	this.makeEpi = makeEpi;
 
 	cleanupNulls();
 	nullToZeros();
 	minimalDiversityInputCheck();
 	if (makeEpi) {
 	    epiInputCheck();
 	}
 	removeZerosInBundles();
 	createLevelsList();
 
     }
 
     // For Testing only.
     public RandomMTGeneratorHelper() {
     }
 
     /** User Input handling */
     private void minimalDiversityInputCheck() throws CNAException {
 	for (int i = 0; i < alterFactors.size(); i++) {
 	    int counter = 0;
 	    for (int j = 0; j < bundleSizesLevels.get(i).size(); j++) {
 		if (bundleSizesLevels.get(i).get(j) != 0) {
 		    counter++;
 		}
 	    }
 	    if (alterFactors.get(i) != 0) {
 		counter += alterFactors.get(i);
 	    }
 	    if (counter < 2 && counter != 0) {
 		throw new CNAException(
 			"Violation of Minimal Diversity pre-condition: Every MT must have at least two bundles, alternate factors, or a bundle and a alternate factor.");
 	    }
 	}
 
     }
 
     /** User Input handling */
     private boolean epiInputCheck() throws CNAException {
 	int counter = 0;
 
 	for (Integer cur : alterFactors) {
 	    if (cur != 0) {
 		counter++;
 	    }
 	}
 	if (counter >= 2) {
 	    return true;
 	}
 
 	counter = 0;
 	for (ArrayList<Integer> list : bundleSizesLevels) {
 	    for (Integer cur : list) {
 		if (cur != 0) {
 		    counter++;
 		    break;
 		}
 	    }
 	}
 	if (counter >= 2) {
 	    return true;
 	}
 	throw new CNAException(
		"Generating an epiphenomenon is only possible with at least two minimal theories.");
     }
 
     private void cleanupNulls() {
 	for (ArrayList<Integer> list : bundleSizesLevels) {
 	    for (int i = 0; i < list.size(); i++) {
 		if (list.get(i) == null)
 		    list.set(i, 0);
 	    }
 	}
 
     }
 
     private void nullToZeros() {
 	for (int i = 0; i < alterFactors.size(); i++) {
 	    if (alterFactors.get(i) == null) {
 		alterFactors.set(i, 0);
 	    }
 	}
     }
 
     public void removeZerosInBundles() {
 	for (int i = bundleSizesLevels.size() - 1; i >= 0; i--) {
 	    ArrayList<Integer> list = bundleSizesLevels.get(i);
 	    for (int j = list.size() - 1; j >= 0; j--) {
 		Integer cur = list.get(j);
 		if (cur == 0) {
 		    list.remove(j);
 		}
 	    }
 	}
     }
 
     public void createLevelsList() {
 	for (int i = 0; i < bundleSizesLevels.size(); i++) {
 	    ArrayList<Object> level = new ArrayList<Object>();
 	    level.add(bundleSizesLevels.get(i));
 	    level.add(alterFactors.get(i));
 	    completeList.add(level);
 	}
     }
 
     public ArrayList<ArrayList<Integer>> getBundleSizesLevels() {
 	return bundleSizesLevels;
     }
 
     public ArrayList<Integer> getAlterFactors() {
 	return alterFactors;
     }
 
     public ArrayList<ArrayList<Object>> getCompleteList() {
 	return completeList;
     }
 
     public void setBundleSizesLevels(
 	    ArrayList<ArrayList<Integer>> bundleSizesLevels) {
 	this.bundleSizesLevels = bundleSizesLevels;
     }
 
     public void setAlterFactors(ArrayList<Integer> alterFactors) {
 	this.alterFactors = alterFactors;
     }
 }
