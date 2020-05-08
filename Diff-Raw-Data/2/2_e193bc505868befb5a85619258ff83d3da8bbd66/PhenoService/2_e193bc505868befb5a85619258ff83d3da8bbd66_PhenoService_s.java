 /*
 * Copyright (c) 2010 The Jackson Laboratory
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 */
 package org.jax.drakegenetics.server;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jax.drakegenetics.shareddata.client.Chromosome;
 import org.jax.drakegenetics.shareddata.client.DiploidGenome;
 import org.jax.drakegenetics.shareddata.client.Gene;
 
 /**
  *
  * @author gbeane
  */
 public class PhenoService {
 
     private final GeneLookup geneLookup;
     private final GenotypeService genotypeService;
     
     /**
      * Constructor
      * @param geneLookup
      * @param genotypeService
      */
     public PhenoService(GeneLookup geneLookup, GenotypeService genotypeService)
     {
         this.geneLookup = geneLookup;
         this.genotypeService = genotypeService;
     }
 
 
     /**
      * Get all phenotypes for this genome.
      * @param genome
      * @return A Map of phenotypes to values. If the genome is inviable 
      * then a single special phenotype of "Lethal" will be returned with value
      * "true"
      */
     public Map<String, String> getPhenome(DiploidGenome genome)
     {
         Map<String, String> phenome = new HashMap<String, String>();
 
         Map<String, List<String>> alleles = getAlleles(genome);
 
         try {
             phenome.put("Metabolic", getMetabolicPhenotype(alleles));
             phenome.put("Eye Color", getEyeColor(alleles));
             phenome.put("Eye Morphology", getEyeMorphology(alleles));
             phenome.put("Scale Color", getScaleColor(alleles));
             phenome.put("Tail Morphology", getTailMorphology(alleles));
             phenome.put("Armor", getArmor(alleles));
             phenome.put("Sex", getSex(genome));
             phenome.put("Sex Reversal", getSexReversal(alleles));
             phenome.put("Scale Color", getScaleColor(alleles));
             phenome.put("Diabetes Predisposition", getDiabetesPredisposition(alleles));
         }
         catch (LethalAlleleCombinationException e) {
             phenome.clear();
             phenome.put("Lethal", "true");
         }
         return phenome;
     }
 
     /**
      * get the alleles for this genome
      * @param genome
      * @return a map of gene symbols to list of alleles for that gene
      */
     private String getDiabetesPredisposition(Map<String, List<String>> alleles)
     {
         List<String> diabetesAlleles = alleles.get("Dia");
 
         if (diabetesAlleles.contains("d")) {
             return "no predisposition for diabetes";
         }
         else {
             return "predisposition for diabetes";
         }
     }
 
     /**
      * get the alleles for this genome
      * @param genome
      * @return a map of gene symbols to list of alleles for that gene
      */
     private Map<String, List<String>> getAlleles(DiploidGenome genome)
     {
         Map<String, List<String>> allAlleles = new HashMap<String, List<String>>();
         List<Chromosome> maternalHaploid = genome.getMaternalHaploid();
         List<Chromosome> paternalHaploid = genome.getPaternalHaploid();
 
         for (Gene gene : geneLookup.getGenes()) {
             List<String> alleles = new ArrayList<String>();
 
 
             for (Chromosome c : maternalHaploid) {
                 if (c.getChromosomeName().equals(gene.getChromosomeName())) {
                     alleles.add(genotypeService.getAllele(
                             c.getHaplotypeAtPosition(gene.getStartPosition()),
                             gene.getSymbol()));
                 }
             }
 
             for (Chromosome c : paternalHaploid) {
                 if (c.getChromosomeName().equals(gene.getChromosomeName())) {
                     alleles.add(genotypeService.getAllele(
                             c.getHaplotypeAtPosition(gene.getStartPosition()),
                             gene.getSymbol()));
                 }
             }
 
             allAlleles.put(gene.getSymbol(), alleles);
 
         }
 
         return allAlleles;
     }
 
     /**
      * get the metabolic phenotype
      * @param alleles
      * @return string describing metabolic phenotype
      */
     private static String getMetabolicPhenotype(Map<String, List<String>> alleles)
     {
 
         List<String> bogBreathAlleles = alleles.get("Otc");
 
         /*
          * Bog/Bog - healthy
          * Bog/bog - healthy
          * bog/bog - bog breath
          * Bog/Y - healthy
          * bog/Y - bog breath
         */
 
         /* if the first allele in the list is "bog" ... */
         if (bogBreathAlleles.get(0).equals("bog")) {
             /* if there is only one copy of the gene, or the second copy is also
              * "bog" then the phenotype is "bog breath"
              */
             if (bogBreathAlleles.size() == 1
                     || (bogBreathAlleles.size() == 2 && bogBreathAlleles.get(1).equals("bog"))) {
                 return "bog breath";
             }
         }
 
         // every other combination is healty
         return "healthy";
     }
 
     /**
      * get the eye color determined by this set of alleles
      * @param alleles all alleles for this genome
      * @return String describing eye color phenotype
      */
     private static String getEyeColor(Map<String, List<String>> alleles)
     {
         List<String> flameAlleles = alleles.get("Xdh");
 
         /*
          * F/F - red eye
          * F/f - gold eye
          * f/f - white eye
          */
 
         if (flameAlleles.get(0).equals("F") && flameAlleles.get(1).equals("F")) {
             // F/F
             return "red";
         }
         else if (flameAlleles.get(0).equals("f") && flameAlleles.get(1).equals("f")) {
             // f/f
             return "white";
         }
         // F/f
         return "gold";
     }
 
     /**
      * Get the eye morphology determined by this set of alleles
      * @param alleles all alleles for this genome
      * @return String describing the eye morphology
      * @throws org.jax.drakegenetics.server.PhenoService.LethalAlleleCombinationException
      */
     private String getEyeMorphology(Map<String, List<String>> alleles) throws LethalAlleleCombinationException
     {
         List<String> nickAlleles = alleles.get("Pax6");
 
         /*
          * N/N - lethal
          * N/n - nicked iris
          * n/n - normal eye
          */
 
         if(nickAlleles.get(0).equals("N") && nickAlleles.get(1).equals("N")) {
                 // N/N
                 throw new LethalAlleleCombinationException();
         }
         else if (nickAlleles.get(0).equals("n") && nickAlleles.get(1).equals("n")) {
                 // n/n
                 return "normal eye";
         }
         
         // N/n
         return "nicked iris";
 
     }
 
     /**
      * get the tail morphology determined by this set of alleles
      * @param alleles all alleles for this genome
      * @return String describing the tail morphology
      */
     private static String getTailMorphology(Map<String, List<String>> alleles)
     {
         List<String> tailAlleles = alleles.get("Dll3");
 
         /*
          * T/T - long tail with barb
          * T/t - long tail with barb
          * t/t - short tail, no barb
          */
 
         if (tailAlleles.get(0).equals("t") && tailAlleles.get(1).equals("t")) {
             return "short no barb";
         }
 
         return "long with barb";
     }
 
     /**
      * Get the armor phenotype determined by this set of alleles
      * @param alleles all alleles for this genome
      * @return String describing the armor phenotype
      */
     private static String getArmor(Map<String, List<String>> alleles) {
         
         List<String> armorAlleles = alleles.get("Eda");
 
         /*
          * A1/A1 - five lateral plates
          * A1/A2 - three lateral plates
          * A2/A2 - one lateral plate
          */
 
         if (armorAlleles.get(0).equals("A1") && armorAlleles.get(1).equals("A1")) {
             return "five lateral plates";
         }
         else if (armorAlleles.get(0).equals("A2") && armorAlleles.get(1).equals("A2")) {
             return "one lateral plate";
         }
 
         return "three lateral plates";
     }
 
     /**
      * Get the sex reversal phenotype
      * @param alleles all alleles for this genome
      * @return String description of the sex reversal phenotype
      */
     private static String getSexReversal(Map<String, List<String>> alleles)
     {
 
         List<String> transformerAlleles = alleles.get("Ar");
 
         /*
          * Tr/Tr - normal female
          * Tr/tr - normal female
          * Tr/Y - normal male
          * tr/Y - sex-reversed male (sterile female)
          */
 
         if (transformerAlleles.size() == 1) {
             if (transformerAlleles.get(0).equals("Tr")) {
                 // Tr/Y
                 return "normal male";
             }
             // tr/Y
             return "Sex-reversed male";
         }
 
         // Tr/Tr Tr/tr,  tr/tr not possible?
         return "normal female";
 
 
     }
 
     /**
      * Get the sex of the individual with this genome
      * @param genome
      * @return String description of the sex: (Scruffy) [male, female]
      * @throws org.jax.drakegenetics.server.PhenoService.LethalAlleleCombinationException
      */
     private String getSex(DiploidGenome genome) throws LethalAlleleCombinationException
     {
         int xCount = 0;
         int yCount = 0;
 
         List<Chromosome> maternalHaploid = genome.getMaternalHaploid();
         List<Chromosome> paternalHaploid = genome.getPaternalHaploid();
 
         for (Chromosome c : maternalHaploid) {
             if (c.getChromosomeName().equals("X")) {
                 xCount++;
             }
         }
 
         for (Chromosome c : paternalHaploid) {
             if (c.getChromosomeName().equals("X")) {
                 xCount++;
             }
             else if (c.getChromosomeName().equals("Y")) {
                 yCount++;
             }
         }
 
         if (xCount == 0 || xCount == 3) {
             // YO or XXX
             throw new LethalAlleleCombinationException();
         }
         else if (xCount == 1 && yCount == 0) {
             // XO
             return "Scruffy female";
         }
         else if (xCount == 2 && yCount == 1) {
             // XXY
             return "Scruffy male";
         }
         else if (xCount ==1 && yCount == 1) {
             // XY
             return "male";
         }
 
         // XY
         return "female";
 
     }
 
     /**
     * get the scale color phenotype
     * @param alleles all alleles for a genome
     * @return String description of scale color
     */
     private String getScaleColor(Map<String, List<String>> alleles) throws LethalAlleleCombinationException
     {
         List<String> colorlessAlleles = alleles.get("Tyr");
         List<String> metalicAlleles = alleles.get("M");
         List<String> diluteAlleles = alleles.get("Myo5a");
         List<String> brownAlleles = alleles.get("Tyrp1");
 
         // We've tried to collapse as many of these rules as possible
         // and also take advantage that combinations like B/Y dl/dl or B/b dl/Y are impossible
 
 
         // if c/c drake is either Frost or inviable
         if (numMatches(colorlessAlleles, "c") == 2) {           
             // lets check for all inviable combinations
             
             // Mt/* 
             if (numMatches(metalicAlleles, "Mt") >= 1) {
 
                 // B/* or B/Y
                 if (numMatches(brownAlleles, "B") >= 1) {
 
                     // dl/dl, dl/Y
                     if (numMatches(diluteAlleles, "dl") == diluteAlleles.size()) {
                         throw new LethalAlleleCombinationException();
                     }
                 }
             }
             else if (numMatches(metalicAlleles, "M") >= 1) { // M/*
 
                 // B/* or B/Y
                 if (numMatches(brownAlleles, "B") >= 1) {
 
                     // dl/dl or dl/Y
                     if (numMatches(diluteAlleles, "dl") == diluteAlleles.size()) {
                         throw new LethalAlleleCombinationException();
                     }
                 }
                 else if (numMatches(brownAlleles, "b") == brownAlleles.size()) { // b/b or b/Y
 
                     // dl/dl or dl/Y
                     if (numMatches(diluteAlleles, "dl") == diluteAlleles.size()) {
                         throw new LethalAlleleCombinationException();
                     }
                 }
 
             }
            else if (numMatches(metalicAlleles, "m") >= 1) { // m/*
 
                 // */* for Brown...
 
                 // dl/dl dl/Y
                 if (numMatches(diluteAlleles, "dl") == diluteAlleles.size()) {
                     throw new LethalAlleleCombinationException();
                 }
 
             }
             else {
                 return "Frost";
             }
         }
         else { // C/*
 
         }
         
             
             
 
         
 
         return "TODO";
     }
 
     /*
      * private exception used to bail out of phenotyping if we find a lethal
      * combination of alleles
      */
     private class LethalAlleleCombinationException extends Exception {
 
     }
 
     private static <E> int numMatches(Collection<E> items, E itemToCheck) {
         int count = 0;
         for(E item : items)
          if(itemToCheck.equals(item))
             count++;
         return count;
     }
 
 
 }
