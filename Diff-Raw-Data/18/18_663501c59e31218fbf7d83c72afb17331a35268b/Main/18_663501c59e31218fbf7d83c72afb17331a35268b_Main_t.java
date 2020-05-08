 package org.fit.cvut.mvi;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.fit.cvut.mvi.cgp.CGPConfiguration;
 import org.fit.cvut.mvi.cgp.CGPEvolution;
 import org.fit.cvut.mvi.cgp.CGPEvolutionConfiguration;
 import org.fit.cvut.mvi.evaluator.FitnessEvaluator;
 import org.fit.cvut.mvi.model.Genome;
 import org.fit.cvut.mvi.model.functions.Addition;
 import org.fit.cvut.mvi.model.functions.Function;
 import org.fit.cvut.mvi.model.functions.Inputs;
 import org.fit.cvut.mvi.model.functions.Multiplication;
 import org.fit.cvut.mvi.model.functions.Sine;
 import org.fit.cvut.mvi.model.functions.SquareRoot;
 import org.fit.cvut.mvi.model.functions.Subtraction;
 
 public class Main {
 
     public static final String NETLOGO_PATH = "/home/matej/Downloads/netlogo-5.0.2/NetLogo.jar";
     public static final String TEMPLATE_PATH = "sablona.nlogo";
     public static final String SETUP_PATH = "sablona.xml";
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         // Inner functions
         List<Function> functions = getFunctions();
         // Inputs
         List<Function> inputs = getInputs();
 
         // Create CGP configuration
         CGPConfiguration config = new CGPConfiguration.Builder().functions(functions).inputs(inputs).outputs(2).rows(7).columns(14)
                 .levelsBack(7).build();
         CGPEvolutionConfiguration evolutionConfig = new CGPEvolutionConfiguration.Builder().populationSize(4).mutations(3)
                 .generations(1000).build();
 
         // Create fitness evaluator
         FitnessEvaluator evaluator = new FitnessEvaluator(NETLOGO_PATH, TEMPLATE_PATH, SETUP_PATH);
 
         // Evolution
         CGPEvolution evolution = new CGPEvolution(config, evaluator);
         Genome result = evolution.evolve(evolutionConfig);
 
         // Print results
         System.out.println(config);
         System.out.println(result.decode());
     }
 
     public static List<Function> getFunctions() {
         List<Function> functions = new ArrayList<>();
 
         functions.add(new Addition());
         functions.add(new Subtraction());
         functions.add(new Multiplication());
        // functions.add(new Modulo());
         functions.add(new SquareRoot());
         functions.add(new Sine());
 
         return functions;
 
     }
 
     public static List<Function> getInputs() {
         List<Function> inputs = new ArrayList<>();
 
         inputs.add(Inputs.constant(45));
         // Grass patches
         inputs.add(Inputs.patchAt(Inputs.GRASS, Inputs.NORTH));
         inputs.add(Inputs.patchAt(Inputs.GRASS, Inputs.SOUTH));
         inputs.add(Inputs.patchAt(Inputs.GRASS, Inputs.EAST));
         inputs.add(Inputs.patchAt(Inputs.GRASS, Inputs.WEST));
         // Dirt patches
         inputs.add(Inputs.patchAt(Inputs.DIRT, Inputs.NORTH));
         inputs.add(Inputs.patchAt(Inputs.DIRT, Inputs.SOUTH));
         inputs.add(Inputs.patchAt(Inputs.DIRT, Inputs.EAST));
         inputs.add(Inputs.patchAt(Inputs.DIRT, Inputs.WEST));
         // Trap patches
         inputs.add(Inputs.patchAt(Inputs.TRAP, Inputs.NORTH));
         inputs.add(Inputs.patchAt(Inputs.TRAP, Inputs.SOUTH));
         inputs.add(Inputs.patchAt(Inputs.TRAP, Inputs.EAST));
         inputs.add(Inputs.patchAt(Inputs.TRAP, Inputs.WEST));
         inputs.add(Inputs.patchAhead(Inputs.TRAP, 3));
         // Wolves
         inputs.add(Inputs.turtlesAt(Inputs.WOLVES, Inputs.NORTH));
         inputs.add(Inputs.turtlesAt(Inputs.WOLVES, Inputs.SOUTH));
         inputs.add(Inputs.turtlesAt(Inputs.WOLVES, Inputs.EAST));
         inputs.add(Inputs.turtlesAt(Inputs.WOLVES, Inputs.WEST));
         inputs.add(Inputs.turtlesInCone(Inputs.WOLVES, 3, 90));
         // Sheep
         inputs.add(Inputs.turtlesAt(Inputs.SHEEP, Inputs.NORTH));
         inputs.add(Inputs.turtlesAt(Inputs.SHEEP, Inputs.SOUTH));
         inputs.add(Inputs.turtlesAt(Inputs.SHEEP, Inputs.EAST));
         inputs.add(Inputs.turtlesAt(Inputs.SHEEP, Inputs.WEST));
         inputs.add(Inputs.turtlesInCone(Inputs.SHEEP, 3, 90));
 
         return inputs;
     }
 
 }
