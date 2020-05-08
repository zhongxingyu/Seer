 package random;
 
 /**
  * Copyright (C) <2011>
  * 
  * @author Jonas Ruef & Felix Langenegger <cis.unibe.ch@gmail.com>
  * @license GPLv3, for more informations see Readme.mdown
  */
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Random;
 
 import org.junit.Test;
 
 import play.test.UnitTest;
 import algorithms.cna.CNAException;
 import datastructures.cna.CNAList;
 import datastructures.mt.MinimalTheory;
 import datastructures.mt.MinimalTheorySet;
 import datastructures.random.RandomMTGeneratorHelper;
 import datastructures.random.RandomMTSetGenerator;
 
 /** Run Test 500 times because random stuff is going on */
 public class RandomMTSetGeneratorTest extends UnitTest {
     private RandomMTGeneratorHelper input;
 
     public void generateRandomInput() throws CNAException {
 	ArrayList<ArrayList<Integer>> bundleSizes = new ArrayList<ArrayList<Integer>>();
 	int levels = 3;
 	for (int i = 0; i < levels; i++) {
 	    ArrayList<Integer> level = new ArrayList<Integer>();
 	    int bundlesPerLevel = 2;
 	    for (int j = 0; j < bundlesPerLevel; j++) {
		level.add((int) (Math.random() * 2));
 	    }
 	    bundleSizes.add(level);
 	}
 
 	ArrayList<Integer> alterFactor = new ArrayList<Integer>();
 	for (int j = 0; j < levels; j++) {
	    alterFactor.add((int) (Math.random() * 2));
 	}
 
 	input = new RandomMTGeneratorHelper(bundleSizes, alterFactor, false);
     }
 
     @Test
     public void shouldGenerateFactorNames() {
 	RandomMTSetGenerator generator = new RandomMTSetGenerator();
 	generator.generateFactorNames();
 
 	CNAList shouldBeA = generator.getNamesOriginal().get(0);
 	CNAList shouldBeW = generator.getNamesOriginal().get(
 		generator.getNamesOriginal().size() - 1);
 	assertEquals("A ¬A ", shouldBeA.toString());
 	assertEquals("W ¬W ", shouldBeW.toString());
 	assertEquals(23, generator.getNamesOriginal().size());
     }
 
     @Test
     public void shouldMakeChain() throws CNAException {
 	for (int i = 0; i < 500; i++) {
 	    generateRandomInput();
 	    RandomMTSetGenerator generator = new RandomMTSetGenerator(
 		    input.getCompleteList(), false);
 	    MinimalTheorySet set = generator.getMTSet();
 
 	    for (int j = 0; j < set.size(); j++) {
 		String effectPrev = set.get(j).getEffect();
 		if (j < set.size() - 1) {
 		    assertEquals(effectPrev, set.get(j + 1).getFactors()
 			    .getLastElement());
 		}
 	    }
 	}
     }
 
     @Test
     public void shouldNotHaveDuplicatedFactorsInMTSet() throws CNAException {
 	for (int i = 0; i < 500; i++) {
 	    generateRandomInput();
 	    RandomMTSetGenerator generator = new RandomMTSetGenerator(
 		    input.getCompleteList(), false);
 	    MinimalTheorySet set = generator.getMTSet();
 	    CNAList factors = set.getAllFactors();
 
 	    HashSet map = new HashSet();
 	    for (int j = 0; j < factors.size() - 1; j++) {
 		map.add(factors.get(j));
 		assertFalse(map.contains(factors.get(j + 1)));
 	    }
 	}
 
     }
 
     @Test
     public void shouldCreateRandomNumbers() {
 	RandomMTSetGenerator generator = new RandomMTSetGenerator();
 
 	for (int i = 0; i < 500; i++) {
 	    int randomIndex = generator.randomNegativePositiv();
 	    assertTrue(randomIndex == 0 || randomIndex == 1);
 	}
 	for (int j = 0; j < 50 * 10; j++) {
 	    int randomIndex = generator.randomIndex(50);
 	    assertTrue(randomIndex >= 0 && randomIndex < 50);
 	}
     }
 
     @Test
     public void shouldNotHaveSameFactorsAsCauseAndEffectInTheory()
 	    throws CNAException {
 	for (int i = 0; i < 500; i++) {
 	    generateRandomInput();
 	    RandomMTSetGenerator generator = new RandomMTSetGenerator(
 		    input.getCompleteList(), false);
 	    MinimalTheorySet set = generator.getMTSet();
 
 	    for (MinimalTheory theory : set) {
 		CNAList factors = theory.getFactors();
 		for (String factor : factors) {
 		    assertFalse(theory.getEffect().equals(factor));
 		}
 	    }
 	}
     }
 
     public void generateRandomEpiInput() throws CNAException {
 	ArrayList<ArrayList<Integer>> bundleSizes = new ArrayList<ArrayList<Integer>>();
 	Random random = new Random();
 	boolean twoLevelBundles = random.nextBoolean();
 	boolean twoLevelAlter;
 	if (!twoLevelBundles) {
 	    twoLevelAlter = true;
 	} else {
 	    Random random1 = new Random();
 	    twoLevelAlter = random1.nextBoolean();
 	}
 	int levels = 3;
 	for (int i = 0; i < levels; i++) {
 	    ArrayList<Integer> level = new ArrayList<Integer>();
 	    int bundlesPerLevel = 2;
 	    if (twoLevelBundles) {
 		for (int j = 0; j < bundlesPerLevel; j++) {
 		    level.add((int) (Math.random() * 2) + 1);
 		}
		level.set((int) (Math.random() * 2), 0);
 		bundleSizes.add(level);
 	    } else {
 		for (int j = 0; j < bundlesPerLevel; j++) {
		    level.add((int) (Math.random() * 2));
 		}
 		bundleSizes.add(level);
 	    }
 	}
 
 	ArrayList<Integer> alterFactor = new ArrayList<Integer>();
 	if (twoLevelAlter) {
 	    for (int j = 0; j < levels; j++) {
 		alterFactor.add((int) (Math.random() * 2) + 1);
 	    }
	    alterFactor.set((int) (Math.random() * 2), 0);
 	} else {
 	    for (int j = 0; j < levels; j++) {
		alterFactor.add((int) (Math.random() * 2));
 	    }
 	}
 	input = new RandomMTGeneratorHelper(bundleSizes, alterFactor, false);
     }
 
     @Test
     public void shouldMakeEpiphenomenon() throws CNAException {
 	for (int j = 0; j < 500; j++) {
 	    generateRandomEpiInput();
 	    RandomMTSetGenerator generator = new RandomMTSetGenerator(
 		    input.getCompleteList(), true);
 	    MinimalTheorySet set = generator.getMTSet();
 
 	    assertEquals(set.get(set.size() - 2).getFactors().get(0),
 		    set.get(set.size() - 1).getFactors().get(0));
 	}
     }
 
     @Test
     public void shouldMakeEpiAndChain() throws CNAException {
 	for (int i = 0; i < 500; i++) {
 	    generateRandomEpiInput();
 	    RandomMTSetGenerator generator = new RandomMTSetGenerator(
 		    input.getCompleteList(), true);
 	    MinimalTheorySet set = generator.getMTSet();
 
 	    // Check if chain was made.
 	    if (set.size() > 2) {
 		assertEquals(set.get(0).getEffect(), set.get(1).getFactors()
 			.get(set.get(1).getFactors().size() - 1));
 	    }
 
 	    // Check if Epi was made.
 	    assertEquals(set.get(set.size() - 2).getFactors().get(0),
 		    set.get(set.size() - 1).getFactors().get(0));
 	}
     }
 
 }
