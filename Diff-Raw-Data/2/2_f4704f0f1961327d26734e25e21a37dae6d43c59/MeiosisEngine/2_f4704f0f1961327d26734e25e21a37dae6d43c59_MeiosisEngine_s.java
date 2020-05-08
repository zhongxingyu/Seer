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
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this software.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.jax.drakegenetics.server;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.jax.drakegenetics.shareddata.client.Chromosome;
 import org.jax.drakegenetics.shareddata.client.ChromosomeDescription;
 import org.jax.drakegenetics.shareddata.client.CrossoverPoint;
 import org.jax.drakegenetics.shareddata.client.DiploidGenome;
 
 /**
  * Contains algorithm for simulating meiosis.
  * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
  */
 public class MeiosisEngine
 {
     private final Random rand;
     
     /**
      * Constructor
      */
     public MeiosisEngine()
     {
         this(new Random());
     }
     
     /**
      * Constructor
      * @param rand  the random number generator to use
      */
     public MeiosisEngine(Random rand)
     {
         this.rand = rand;
     }
 
     /**
      * Performs meiosis with the given genome and returns the resulting four
      * gametes.
      * @param genome
      *          the genome to perform meiosis on
      * @return
      *          the resulting gametes. This array will always have a length of
      *          four but since the java designers decided they don't care about
      *          tuples I'm returning an array
      */
     @SuppressWarnings("unchecked")
     public List<Chromosome>[] performMeiosis(DiploidGenome genome)
     {
         if(genome.isAneuploid())
         {
             // all aneuploids are infertile
             return null;
         }
         
         Map<String, ChromosomeDescription> chrDescMap =
             genome.getSpeciesGenomeDescription().getChromosomeDescriptions();
         List<Chromosome> maternalHaploid = genome.getMaternalHaploid();
         List<Chromosome> paternalHaploid = genome.getPaternalHaploid();
         
         int chrCount = maternalHaploid.size();
         assert paternalHaploid.size() == chrCount;
         
         List<Chromosome>[] gametes = new ArrayList[] {
                 new ArrayList<Chromosome>(chrCount),
                 new ArrayList<Chromosome>(chrCount),
                 new ArrayList<Chromosome>(chrCount),
                 new ArrayList<Chromosome>(chrCount)};
         
         // TODO special treatment for X and Y and Non-disjunction
         
         // we model crossover as a process which is independent for each
         // chromosome, so we can work on each chromosome separately
         for(int chrIndex = 0; chrIndex < chrCount; chrIndex++)
         {
             for(int i = 0; i < 2; i++)
             {
                 Chromosome maternalChr = new Chromosome(maternalHaploid.get(chrIndex));
                Chromosome paternalChr = new Chromosome(maternalHaploid.get(chrIndex));
                 ChromosomeDescription chrDesc = chrDescMap.get(maternalChr.getChromosomeName());
                 this.maybeCrossover(chrDesc, maternalChr, paternalChr);
                 
                 if(this.rand.nextBoolean())
                 {
                     gametes[0 + i * 2].add(maternalChr);
                     gametes[1 + i * 2].add(paternalChr);
                 }
                 else
                 {
                     gametes[0 + i * 2].add(paternalChr);
                     gametes[1 + i * 2].add(maternalChr);
                 }
             }
         }
         
         return gametes;
     }
     
     /**
      * The crossover algorithm. Each 10cM segment will have a 0.1 chance
      * of a crossover event
      * @param chrDesc   the chromosome description
      * @param chr1      the 1st chromosome
      * @param chr2      the 2nd chromosome
      */
     private void maybeCrossover(
             ChromosomeDescription chrDesc,
             Chromosome chr1,
             Chromosome chr2)
     {
         assert chr1.getChromosomeName().equals(chr2.getChromosomeName());
         
         int[] crossoverIndices = this.getCrossoverIndices(chrDesc);
         for(int i = 0; i < crossoverIndices.length; i++)
         {
             // randomly place the crossover somewhere in the given 10cM segment
             double crossoverLocation = (i + this.rand.nextDouble()) * 10.0;
             
             // split the chromosomes at the crossover point
             List<CrossoverPoint> chr1Crossovers = chr1.getCrossovers();
             int crossoverIndex1 = MeiosisEngine.findCrossoverIndex(
                     chr1Crossovers,
                     crossoverLocation);
             List<CrossoverPoint> newCrossovers1 = new ArrayList<CrossoverPoint>(chr1Crossovers.subList(
                     0,
                     crossoverIndex1));
             List<CrossoverPoint> suffix1 = chr1Crossovers.subList(
                     crossoverIndex1,
                     chr1Crossovers.size());
             
             List<CrossoverPoint> chr2Crossovers = chr2.getCrossovers();
             int crossoverIndex2 = MeiosisEngine.findCrossoverIndex(
                     chr2Crossovers,
                     crossoverLocation);
             List<CrossoverPoint> newCrossovers2 = new ArrayList<CrossoverPoint>(chr2Crossovers.subList(
                     0,
                     crossoverIndex2));
             List<CrossoverPoint> suffix2 = chr2Crossovers.subList(
                     crossoverIndex2,
                     chr2Crossovers.size());
             
             // splice in the new crossover points and append the suffix
             CrossoverPoint last1 = newCrossovers1.get(newCrossovers1.size() - 1);
             CrossoverPoint last2 = newCrossovers2.get(newCrossovers2.size() - 1);
             newCrossovers1.add(new CrossoverPoint(
                     last2.getDistalHaplotypeId(),
                     crossoverLocation));
             newCrossovers2.add(new CrossoverPoint(
                     last1.getDistalHaplotypeId(),
                     crossoverLocation));
             newCrossovers1.addAll(suffix2);
             newCrossovers2.addAll(suffix1);
             
             // keep the centromeres intact
             if(crossoverLocation < chrDesc.getCentromerePositionCm())
             {
                 chr1.setCrossovers(newCrossovers2);
                 chr2.setCrossovers(newCrossovers1);
             }
             else
             {
                 chr1.setCrossovers(newCrossovers1);
                 chr2.setCrossovers(newCrossovers2);
             }
         }
     }
 
     private static int findCrossoverIndex(
             List<CrossoverPoint> crossovers,
             double newCrossover)
     {
         int size = crossovers.size();
         
         // we start at 1 because the new crossover should never be less than
         // 0 and all chromosomes start at 0
         int i = 1;
         for( ; i < size; i++)
         {
             if(newCrossover < crossovers.get(i).getCentimorganPosition())
             {
                 break;
             }
         }
         
         return i;
     }
     
     private int[] getCrossoverIndices(ChromosomeDescription chrDesc)
     {
         int segments = chrDesc.getNumTenCmSegments();
         List<Integer> crossoverIndexList = new ArrayList<Integer>(segments);
         for(int i = 0; i < segments; i++)
         {
             // give each segment a 0.1 chance of crossover
             if(this.rand.nextDouble() < 0.1)
             {
                 crossoverIndexList.add(i);
             }
         }
         
         // shuffle the crossover indices. This is necessary because crossover
         // order will affect the resulting chromosome
         int numCrossovers = crossoverIndexList.size();
         for(int i = numCrossovers; i > 0; i--)
         {
             Integer nextTail = crossoverIndexList.remove(this.rand.nextInt(i));
             crossoverIndexList.add(nextTail);
         }
         
         // we want to return a maximum of 3 crossovers
         int[] crossoverIndices = new int[Math.min(3, numCrossovers)];
         for(int i = 0; i < crossoverIndices.length; i++)
         {
             crossoverIndices[i] = crossoverIndexList.get(i);
         }
         
         return crossoverIndices;
     }
 }
