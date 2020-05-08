 package com.akavrt.csp.tester.ui;
 
 import com.akavrt.csp.solver.genetic.Chromosome;
 import com.akavrt.csp.solver.genetic.Population;
 
 /**
  * User: akavrt
  * Date: 11.04.13
  * Time: 00:35
  */
 public class SeriesData {
     public int age;
     // trim loss
     public double trimBest;
     public double trimAverage;
     public double trimTradeoff;
     // pattern reduction
     public double patternsBest;
     public double patternsAverage;
     public double patternsTradeoffUnique;
     public double patternsTradeoffTotal;
     // product deviation
     public double productionBest;
     public double productionAverage;
     public double productionTradeoff;
     public double productionTradeoffMaxUnderProd;
     public double productionTradeoffMaxOverProd;
     // scalar
     public double scalarBest;
     public double scalarAverage;
 
     public SeriesData(Population population, SeriesMetricProvider provider) {
         process(population, provider);
     }
 
     private void process(Population population, SeriesMetricProvider provider) {
         if (population == null || population.getChromosomes().size() == 0) {
             return;
         }
 
         age = population.getAge();
 
         int i = 0;
         Chromosome bestScalar = null;
         Chromosome bestTrim = null;
         Chromosome bestPatterns = null;
         Chromosome bestProduct = null;
         for (Chromosome chromosome : population.getChromosomes()) {
             trimAverage += provider.getTrimMetric().evaluate(chromosome);
             if (i == 0 || provider.getTrimMetric().compare(chromosome, bestTrim) > 0) {
                 bestTrim = chromosome;
             }
 
             patternsAverage += provider.getPatternsMetric().evaluate(chromosome);
             if (i == 0 || provider.getPatternsMetric().compare(chromosome, bestPatterns) > 0) {
                 bestPatterns = chromosome;
             }
 
             productionAverage += provider.getProductMetric().evaluate(chromosome);
             if (i == 0 || provider.getProductMetric().compare(chromosome, bestProduct) > 0) {
                 bestProduct = chromosome;
             }
 
             scalarAverage += provider.getScalarMetric().evaluate(chromosome);
             if (i == 0 || provider.getScalarMetric().compare(chromosome, bestScalar) > 0) {
                 bestScalar = chromosome;
             }

            i++;
         }
 
         trimBest = provider.getTrimMetric().evaluate(bestTrim);
         trimAverage /= population.getChromosomes().size();
         trimTradeoff = provider.getTrimMetric().evaluate(bestScalar);
 
         patternsBest = provider.getPatternsMetric().evaluate(bestPatterns);
         patternsAverage /= population.getChromosomes().size();
         patternsTradeoffUnique = bestScalar.getMetricProvider().getUniquePatternsCount();
         patternsTradeoffTotal = bestScalar.getMetricProvider().getActivePatternsCount();
 
         productionBest = provider.getProductMetric().evaluate(bestProduct);
         productionAverage /= population.getChromosomes().size();
         productionTradeoff = provider.getProductMetric().evaluate(bestScalar);
         productionTradeoffMaxUnderProd = bestScalar.getMetricProvider().getMaximumUnderProductionRatio();
         productionTradeoffMaxOverProd = bestScalar.getMetricProvider().getMaximumOverProductionRatio();
 
         scalarBest = provider.getScalarMetric().evaluate(bestScalar);
         scalarAverage /= population.getChromosomes().size();
     }
 }
