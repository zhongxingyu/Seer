 /*Copyright (C) 2012 Lars Andersen, Tormund S. Haus.
 larsan@stud.ntnu.no
 tormunds@ntnu.no
 
 EASY is free software: you can redistribute it and/or modify it
 under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
  
 EASY is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.
  
 You should have received a copy of the GNU General Public License
     along with EASY.  If not, see <http://www.gnu.org/licenses/>.*/
 package edu.ntnu.EASY;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 
 import org.apache.commons.cli.BasicParser;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 import java.io.File;
 
 import edu.ntnu.EASY.blotto.Blotto;
 import edu.ntnu.EASY.blotto.BlottoReport;
 import edu.ntnu.EASY.incubator.BitvectorIncubator;
 import edu.ntnu.EASY.incubator.BitvectorReplicator;
 import edu.ntnu.EASY.incubator.Incubator;
 import edu.ntnu.EASY.selection.adult.AdultSelector;
 import edu.ntnu.EASY.selection.adult.FullGenerationalReplacement;
 import edu.ntnu.EASY.selection.adult.GenerationalMixing;
 import edu.ntnu.EASY.selection.adult.Overproduction;
 import edu.ntnu.EASY.selection.parent.BoltzmanSelector;
 import edu.ntnu.EASY.selection.parent.FitnessProportionateSelector;
 import edu.ntnu.EASY.selection.parent.ParentSelector;
 import edu.ntnu.EASY.selection.parent.RankSelector;
 import edu.ntnu.EASY.selection.parent.SigmaScaledSelector;
 import edu.ntnu.EASY.selection.parent.TournamentSelector;
 
 public class Main {
 
 	private static final Options options = new Options();
 	private static final String USAGE = "java -jar easy.jar [pgtmckoePA]\n" +
 											"ParentSelectors: Boltzman, FitnessProportionate, Rank, SigmaScaled, Tournament \n" +
 											"AdultSelectors: FullGenerationalReplacement, GenerationalMixing, Overproducion.";
 	
 	static {
 		options.addOption("p","population",true,"The population size of the system")
 				.addOption("g","generations",true,"How many generations to simulate")
 				.addOption("t","threshold",true,"The fitness threshold to stop at")
 				.addOption("m","mutation",true,"The mutation rate of the system.")
 				.addOption("c","crossover",true,"The crossover rate of the system.")
 				.addOption("b","children",true,"Number of children produced every cycle.")
 				.addOption("f","parents",true,"Number of parents selected for mating")
 				.addOption("e","elitsm",true,"How many of the fittest individuals that skip selection")
 				.addOption("l","length",true,"Length of the genome, number of bits")
 				.addOption("P","parent-select",true,"Which parent selection strategy to use.")
 				.addOption("A","adult-select",true,"Which adult selection strategy to use.")
 				.addOption("?",false,"Print help")
 				.addOption("h","help",false,"Print help")
 				.addOption("B","blotto",false,"Run blotto, no other options needed :I")
 				.addOption("o","output-file",true,"Name of the outputfile")
 				.addOption("r","rank",true,"The rank used is selection.");
 	}
 	
     public static void main(String[] args) {
 
     	CommandLine cl = null;
     	CommandLineParser clp = new BasicParser();
     	HelpFormatter hf = new HelpFormatter();
     	try {
     		cl = clp.parse(options,args);
     	} catch (ParseException e) {
     		e.printStackTrace();
     		hf.printHelp(USAGE,options);
     		System.exit(1);
     	}
 
     	if(cl.hasOption('h') || cl.hasOption('?')){
     		hf.printHelp(USAGE,options);
     		System.exit(0);
     	}
     	
     	if(cl.hasOption('B')){
     		blotto();
     		System.exit(0);
     	}
     	
     	Environment env = new Environment();
     	
 		env.populationSize = Integer.parseInt(cl.getOptionValue('p',"60"));
 		env.maxGenerations = Integer.parseInt(cl.getOptionValue('g',"100"));
 		env.fitnessThreshold = Double.parseDouble(cl.getOptionValue('t',"1.1"));
 		env.mutationRate = Double.parseDouble(cl.getOptionValue('m',"0.01"));
 		env.crossoverRate = Double.parseDouble(cl.getOptionValue('c',"0.01"));
 		env.numChildren = Integer.parseInt(cl.getOptionValue('b',"57"));
 		env.numParents = Integer.parseInt(cl.getOptionValue('f',"29"));
 		env.rank = Integer.parseInt(cl.getOptionValue('r',"10"));
 		env.elitism = Integer.parseInt(cl.getOptionValue('e',"3"));
     	
 		int length = Integer.parseInt(cl.getOptionValue('l',"40"));
 		
 		String parentSelect = cl.getOptionValue('P',"FitnessProportionate");
 		ParentSelector<int[]> parentSelector = null;
 		if(parentSelect.equalsIgnoreCase("Boltzman")){
 			parentSelector = new BoltzmanSelector<int[]>(env.numParents);
 		} else if(parentSelect.equalsIgnoreCase("FitnessProportionate")){
 			parentSelector = new FitnessProportionateSelector<int[]>(env.numParents);
 		} else if(parentSelect.equalsIgnoreCase("Rank")){
 			parentSelector = new RankSelector<int[]>(env.rank);
 		} else if(parentSelect.equalsIgnoreCase("SigmaScaled")){
 			parentSelector = new SigmaScaledSelector<int[]>(env.numParents);
 		} else if(parentSelect.equalsIgnoreCase("Tournament")){
 			parentSelector = new TournamentSelector<int[]>(env.rank, env.numParents);
 		} else {
 			System.out.printf("No such parent selector: %s%n",parentSelect);
 			hf.printHelp(USAGE,options);
 			System.exit(1);
 		}
 		
 		String adultSelect = cl.getOptionValue('A',"FullGenerationalReplacement");
 		AdultSelector<int[]> adultSelector = null;
 		if(adultSelect.equalsIgnoreCase("FullGenerationalReplacement")){
 			adultSelector = new FullGenerationalReplacement<int[]>(env.elitism);
 		} else if (adultSelect.equalsIgnoreCase("GenerationalMixing")){
 			adultSelector = new GenerationalMixing<int[]>(env.numAdults);
 		} else if (adultSelect.equalsIgnoreCase("Overproduction")){
			adultSelector = new Overproduction<int[]>(env.numAdults);
 		} else {
 			System.out.printf("No such parent selector: %s%n",adultSelect);
 			hf.printHelp(USAGE,options);
 			System.exit(1);
 		}
 		
 		FitnessCalculator<int[]> fitCalc = IntegerArrayFitnessCalculators.ONE_MAX_FITNESS;
 		
 		Incubator<int[],int[]> incubator = new BitvectorIncubator(new BitvectorReplicator(length, env.mutationRate,env.crossoverRate), env.numChildren);	
 		Evolution<int[],int[]> evo = new Evolution<int[], int[]>(fitCalc, adultSelector, parentSelector, incubator);
 		
 		String filename = cl.getOptionValue('o',"one-max.plot");
 		
 		Report<int[],int[]> report = new BasicReport(filename);
 		evo.runEvolution(env, report);
     }
     
     public static void blotto(){
     	Blotto blotto = new Blotto();
     	
     	int[] Bs = {5,20};
     	double[] Rfs = {1.0,0.5,0.0};
     	double[] Lfs = {1.0,0.5,0.0};
 
     	for(int B : Bs){
     		for(double Rf : Rfs){
     			for(double Lf : Lfs){
     				double start = System.currentTimeMillis();
     				BlottoReport report = blotto.runBlottoEvolution(B,Rf,Lf);
     				double stop = System.currentTimeMillis();
     				String filename = String.format("log/blotto-%d-%.1f-%.1f",B,Rf,Lf);
     				try {
     					PrintStream log = new PrintStream(new FileOutputStream(filename + ".log"));
     					log.printf("# Blotto run, B: %d, Rf: %.2f, Lf: %.2f - %.2f sec %n",B,Rf,Lf,(stop-start)/1000);
     					report.writeToStream(log);
     					File plotfile = new File(filename + ".plot");
     					PrintStream plotStream = new PrintStream(new FileOutputStream(plotfile));
     					report.writePlot(plotStream);
     					Output.plotBlotto(plotfile,B,Rf,Lf);
     				} catch (FileNotFoundException e) {
     					System.err.printf("File not found: %s%n",filename);
     				}
     			}
     		}
     	}
     }
 }
