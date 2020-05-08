 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package demo_v04;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 
 /**
  *
  * @author Chorinca-Notebook
  */
 public class Demo_v04 {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
 
         Function[]      __populationInit;
         Function[]      __populationSelect;
         Function[]      __populationRecombination;
         Function[]      __populationMutation;
 
         final int       __sizePopulationInit;
         final int       __sizePopulationSelect;
         final double    __beginDomainX1;
         final double    __beginDomainX2;
         final double    __endDomainX1;
         final double    __endDomainX2;
         final String    __scriptFitness;
         int             __numberGenerationsMax;
         double          __bestFitnessMax;
         
         __beginDomainX1         = -3.0;
         __beginDomainX2         = 4.1;
         __endDomainX1           = 12.1;
         __endDomainX2           = 5.8;
         __scriptFitness         = "21.5+x1*Math.sin(4*Math.PI*x1)+x2*Math.sin(20*Math.PI*x2)";
         
         __sizePopulationInit    = 10;
         __sizePopulationSelect  = 10;
         __populationInit        = new Function[__sizePopulationInit];
         
         // inicializar a população de function
         for (int __indexIndividual = 0; __indexIndividual < __sizePopulationInit; __indexIndividual++) {
             __populationInit[__indexIndividual] = new Function(__beginDomainX1, __endDomainX1, __beginDomainX2, __endDomainX2, __scriptFitness);
         }
         
        __numberGenerationsMax  = 200;
         __bestFitnessMax        = 40.0;
         
         while(((__numberGenerationsMax--) > 0) || _returnBestFitness(__populationInit) >= __bestFitnessMax) {
             __populationSelect          = roulette(__populationInit, __sizePopulationSelect);
             __populationRecombination   = recombination(__populationSelect);
            __populationMutation        = mutation(__populationRecombination, 0.025);
             __populationInit            = truncation(__populationInit, __populationMutation, __populationInit.length);
 
             System.out.println("Generation CountDown: " + __numberGenerationsMax);
             _writeIndividualToConsole(__populationInit);
             System.out.println("");
         }
 
         System.out.println();
         _writeIndividualToConsoleWithXs(__populationInit);
     }
     
     private static void _writeIndividualToConsole(Function[] population) {
         for (int __indexIndividual = 0; __indexIndividual < population.length; __indexIndividual++) {
             System.out.println(population[__indexIndividual].toString() + " -> " + population[__indexIndividual].Fitness());
         }
     }
     
     private static void _writeIndividualToConsoleWithXs(Function[] population) {
         for (int __indexIndividual = 0; __indexIndividual < population.length; __indexIndividual++) {
             System.out.println(population[__indexIndividual].toString() + " -> " + population[__indexIndividual].Fitness() + " -> " + "X1 = " + population[__indexIndividual].getX1() + " X2 = " + population[__indexIndividual].getX2());
         }
     }
     
     private static double _returnBestFitness(Function[] population) {
         double __bestFiteness;
         double __fitnessActual;
         __bestFiteness = 0;
         
         for (int __indexIndividual = 0; __indexIndividual < population.length; __indexIndividual++) {
             __fitnessActual = population[__indexIndividual].Fitness();
             
             if(__bestFiteness < __fitnessActual)
                 __bestFiteness = __fitnessActual;
         }
         
         return __bestFiteness;
     }
     
     
     public static Function[] roulette(Function[] population, int sizeSelection){
         Function[]  __populationSelect;
         double      __point;
         
         __populationSelect = new Function[sizeSelection];
         
         for (int __indexIndividual = 0; __indexIndividual < sizeSelection; __indexIndividual++) {
             //ponteiro que vai apontar para os individuos, inicialização com ponto aleatorio
             __point = randomPoint(getFitnessTotal(population));
             //acrecenta um individou para a nova população
             __populationSelect[__indexIndividual] = returnIndividualByPoint(__point, population);
         }
         
         return __populationSelect;
     }
     
     public static double getFitnessTotal(Function[] population) {
         double __totalFitness = 0;
 
         for (Function __individual : population) {
             __totalFitness += __individual.Fitness();
         }
 
         return __totalFitness;
     }
     
     public static Function returnIndividualByPoint(double point, Function[] population){
         double __totalFitness = 0;
         //corre a população para cada individuo
         for (Function __individual : population) {
             //incrementa o total fitness
             __totalFitness += __individual.Fitness();
             
             //escolhe o individuo onde o ponteiro aponta 
             if(point <= __totalFitness) {
                 //clone para criar um novo individuo e não ser individuo da pop original
                 return new Function(__individual);
             }
         }
         //Caso não existam individuos na população recebida, devolve null
         return null;
     }
     
     public static double randomPoint(double totalFitnessPopulation){
         return Function.RANDOM_GENERATOR.nextDouble() * totalFitnessPopulation;
     }
 
     
     public static Function[] recombination(Function[] population) {
         final int SON       = 0;
         final int DAUGHTER  = 1;
         final int X1        = 0;
         final int X2        = 1;
         
         Function[] __descendents;
         Function[] __descendentsPopulation;
         
         __descendentsPopulation = new Function[population.length];
         
         for (int __indexIndividual = 0; __indexIndividual < population.length; __indexIndividual++) {
             
             // Caso a população seja impar e um indivu
             if((__indexIndividual+1) >= population.length) {
                 __descendentsPopulation[__indexIndividual] = new Function(population[__indexIndividual]);
                 break;
             }
             
             __descendents       = new Function[2];
             __descendents[0]    = new Function(population[__indexIndividual]);
             __descendents[1]    = new Function(population[__indexIndividual + 1]); 
 
            //if(Function.RANDOM_GENERATOR.nextDouble() < 0.65) {
                 final int __sizeChromosomeX1;            
                 final int __sizeChromosomeX2;
                 final int __pointCutX1;
                 final int __pointCutX2;            
 
                 __sizeChromosomeX1  = population[__indexIndividual].getChromossome(X1).length();            
                 __sizeChromosomeX2  = population[__indexIndividual].getChromossome(X2).length();
                 __pointCutX1        = pointCutRandom(__sizeChromosomeX1);
                 __pointCutX2        = pointCutRandom(__sizeChromosomeX2);
 
                 __descendents       = crossover(__descendents[SON], __descendents[DAUGHTER], __pointCutX1, __pointCutX2);
            //}
             
             __descendentsPopulation[__indexIndividual]      = __descendents[0];
             __descendentsPopulation[__indexIndividual + 1]  = __descendents[1];            
         }
 
         return __descendentsPopulation;
     }
     
     protected static int pointCutRandom(int size) {
         return Function.RANDOM_GENERATOR.nextInt(size - 2) + 1;
     }
     
     protected static Function[] crossover(Function father, Function mother, int pointCutX1, int pointCutX2) {
         final int SON       = 0;
         final int DAUGHTER  = 1;
         final int X1        = 0;
         final int X2        = 1;
         
         final Function[] __descendents;
         final int __sizeChromosomeSonX1;            
         final int __sizeChromosomeSonX2;
             
         __descendents           = new Function[2];
         __descendents[0]        = new Function(father);
         __descendents[1]        = new Function(mother); 
         __sizeChromosomeSonX1   = father.getChromossome(X1).length();            
         __sizeChromosomeSonX2   = father.getChromossome(X2).length();
 
         for (int __indexX1 = 0; __indexX1 < __sizeChromosomeSonX1; __indexX1++) {
             if(__indexX1 >= pointCutX1) {
                 __descendents[SON].setGene(X1, __indexX1, mother.getGene(X1, __indexX1));                   
                 __descendents[DAUGHTER].setGene(X1, __indexX1, father.getGene(X1, __indexX1));
             }
         }
 
         for (int __indexX2 = 0; __indexX2 < __sizeChromosomeSonX2; __indexX2++) {
             if(__indexX2 >= pointCutX2) {
                 __descendents[SON].setGene(X2, __indexX2, mother.getGene(X2, __indexX2));                   
                 __descendents[DAUGHTER].setGene(X2, __indexX2, father.getGene(X2, __indexX2));
             }
         }
 
         return __descendents;
     }
 
     
     public static Function[] mutation(Function[] population, double probability) {
         Function[]  __descendentsPopulation;
         Function    __descendentsMutate;
         
         __descendentsPopulation = new Function[population.length];
         
         for (int __indexIndividual = 0; __indexIndividual < population.length; __indexIndividual++) {
             __descendentsMutate = new Function(population[__indexIndividual]);
             
             if(probability > Function.RANDOM_GENERATOR.nextDouble()) {
                 mutateGenes(__descendentsMutate);
             }
             
             __descendentsPopulation[__indexIndividual] = __descendentsMutate;
         }
         
         return __descendentsPopulation;
     }
     
     protected static void mutateGenes(Function individual) {
         int __indexGeneMutate;
         
         // Mutação para X1
         __indexGeneMutate = Function.RANDOM_GENERATOR.nextInt(individual.getChromossome(0).length());
        individual.setGene(0, __indexGeneMutate, !individual.getGene(0, __indexGeneMutate));
         
         // Mutação para X2
         __indexGeneMutate = Function.RANDOM_GENERATOR.nextInt(individual.getChromossome(1).length());
        individual.setGene(1, __indexGeneMutate, !individual.getGene(1, __indexGeneMutate));
     }
 
 
     private static Function[] truncation(Function[] progenitors, Function[] descendants, int numberOfIndividualsForNewGeneration) {
         ArrayList<Function> __newGeneration;
         __newGeneration = new ArrayList<>();
         
         __newGeneration.addAll(Arrays.asList(progenitors));
         __newGeneration.addAll(Arrays.asList(descendants));
         
         Collections.sort(__newGeneration, Collections.reverseOrder(new Comparator<Function>() {
 
             @Override
             public int compare(Function individual1, Function individual2) {
                 double __fitnessIndividual1;
                 double __fitnessIndividual2;
 
                 __fitnessIndividual1 = individual1.Fitness();
                 __fitnessIndividual2 = individual2.Fitness();
 
                 if(__fitnessIndividual1 > __fitnessIndividual2)
                     return 1;
                 
                 if(__fitnessIndividual1 < __fitnessIndividual2)
                     return -1;
                 
                 return 0;
             }
             
         }));
 
         return (new ArrayList<>(__newGeneration.subList(0, numberOfIndividualsForNewGeneration))).toArray(descendants);
     }
 }
