 /**
 *   SSPEA.java
 *
 *   Copyright (c) 2013 Vladimir Brigant
 *   This software is distributed under the terms of the GNU General Public License.
 */
 
 package cassp.ea;
 
 import java.io.*;
 import java.util.*;
 
 import org.jgap.*;
 import org.jgap.data.*;
 import org.jgap.impl.*;
 import org.jgap.xml.*;
 import org.jgap.event.*;
 import org.jgap.util.*;
 import org.w3c.dom.*;
 import org.apache.log4j.*;
 
 import cassp.*;
 import cassp.ca.*;
 import cassp.data.*;
 import cassp.utils.*;
 import cassp.config.*;
 import cassp.ca.rules.*;
 import cassp.ea.stats.*;
 
 
 
 /*
 Notes:
 - default mutation rate - 1/12 (1/X)
 - default crossover rate - 35 % from pop size
 - http://vyuka.martinpilat.com/2011/11/09/eva-5-cviceni-realna-funkce-ii-operatory/
 */
 
 
 
 /**
 * Secondary Structure Prediction Evolutionary Algorithm.
 */
 public class SSPEA {
 
     static Logger logger = Logger.getLogger(SSPEA.class);
 
     private Data data;
     private SimConfig config;
     private EAStats stats;
 
     public SSPEA(SimConfig config, Data data){
         this.data = data;
         this.config = config;
         this.stats = new EAStats(config.getNoChangeEAEnd());
     }
 
     public CARule evolve() throws Exception{
         // EA configuration
         Configuration.reset();
         Configuration conf = new Configuration("conf");
         conf.setBreeder(new GABreeder());
 
         RandomGenerator rg = new GaussianRndGenerator(this.config.getMutDev());
         conf.setRandomGenerator(rg);
         conf.setEventManager(new EventManager());
 
         BestChromosomesSelector bestChromsSelector = new BestChromosomesSelector(conf, 0.90d);
         bestChromsSelector.setDoubletteChromosomesAllowed(true);
         conf.addNaturalSelector(bestChromsSelector, false);
         conf.addNaturalSelector(new WeightedRouletteSelector(conf), false);
 
         conf.setMinimumPopSizePercent(0);
         conf.setSelectFromPrevGen(1.0d);
         conf.setKeepPopulationSizeConstant(true);
         conf.setFitnessEvaluator(new DefaultFitnessEvaluator());
         conf.setChromosomePool(new ChromosomePool());
         conf.addGeneticOperator(new CrossoverOperator(conf, this.config.getCrossProb()));
         conf.addGeneticOperator(new GaussianMutationOperator(conf, this.config.getMutDev()));
         conf.setPreservFittestIndividual(true);
 
         FitnessFunction ff = new SSPFF(this.data, this.config);
         conf.setFitnessFunction(ff);
         CARule rule = this.setRule();
         //conf.addGeneticOperator(new MutationOperator(conf, rule.getSize()/2));
 
         IChromosome sampleChromosome = rule.initChromosome(conf, this.config.getMaxSteps());
 
         conf.setSampleChromosome(sampleChromosome);
         conf.setPopulationSize(this.config.getPop());
 
         //genotype = Genotype.randomInitialGenotype(conf);
         Genotype genotype = this.initGenotype(conf);
 
 
         // population evolving
         for (int i = 0; i < this.config.getMaxGen(); i++) {
             genotype.evolve();
 
             // filling GenStats object
             GenStats gs = new GenStats();
             gs.setMax(Utils.getMax(genotype.getPopulation()));
             gs.setMin(Utils.getMin(genotype.getPopulation()));
             gs.setMean(Utils.getMean(genotype.getPopulation()));
             gs.setGeneration(i);
             this.stats.addGenStats(gs);
 
            logger.info("##  " + i + ". generation: " + genotype.getFittestChromosome().getFitnessValue());
             if (this.stats.isConverged())
                 break;
          }
 
         CARule bestRule = rule.fromChromosome(genotype.getFittestChromosome());
         bestRule.computeMaxPropsDiff(new double[]{this.data.getMaxCF(), this.data.getMaxCC()});
         return rule;
     }
 
 
     /**
     * Initialize EA genotype.
     */
     private Genotype initGenotype(Configuration conf){
         Genotype genotype = null;
 
         if (this.config.getRule() == SimConfig.RULE_SIMPLE)
             genotype = CASimpleRule.initGenotypeGauss(conf);
         else if (this.config.getRule() == SimConfig.RULE_CONFORM)
             genotype = CAConformRule.initGenotypeGauss(conf);
         return genotype;
     }
 
 
     /* Getters & setters */
 
     private CARule setRule(){
         if (this.config.getRule() == SimConfig.RULE_SIMPLE)
             return new CASimpleRule(this.config.getNeigh(), this.data.getAminoAcids());
         else if (this.config.getRule() == SimConfig.RULE_CONFORM)
             return new CAConformRule(this.config.getNeigh(), this.data.getAminoAcids());
         else
             return new CASimpleRule(this.config.getNeigh(), this.data.getAminoAcids());
     }
 
     public EAStats getStats(){
         return this.stats;
     }
 }
