 
 
 package fermanext.jgap;
 
 import java.util.*;
 import java.io.*;
 import fermanext.system.*;
 import fermanext.trussunit.*;
 import fermanext.logging.log4cxx.*;
 
 import org.jgap.*;
 import org.jgap.data.*;
 import org.jgap.impl.*;
 import org.jgap.xml.*;
 import org.jgap.event.EventManager;
 
 final class GAOptimizationFitnessFunction extends FitnessFunction
 {
     /** Plugin logger */
     private static Logger logger = 
         Logger.getLogger("java.plugin.GAOptimizationPlugin");
 
     private GAOptimizationPlugin gaPlugin = null;
     private Plugin fitnessPlugin = null;
     private TrussUnit truss = null;
 
     public GAOptimizationFitnessFunction ( GAOptimizationPlugin gaPlugin,
                                            Plugin fitnessPlugin,
                                            TrussUnit truss )
     {
         this.gaPlugin = gaPlugin;
         this.fitnessPlugin = fitnessPlugin;
         this.truss = truss;        
     }
 
     private StringUUIDObject chromosomeToXml ( IChromosome a_chromosome )
     {
         int numGenes = a_chromosome.size();
 
         String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<GAGenomes>\n";
 
         for ( int i = 0; i < numGenes; ++i ) { 
             DoubleGene gene = (DoubleGene)a_chromosome.getGene(i);
             xml += "<GAGenome floatValue=\"" + gene.doubleValue() + "\"/>\n";
         }
         xml += "</GAGenomes>\n";
         
         return new StringUUIDObject( xml );
     }
 
     private double resultToFitness ( Plugin.ExecutionResult res )
     {
         logger.debug( "resultToFitness: fitness value " + res.data );
 
         if ( res.status != Plugin.Status.OkStatus ) {
             logger.warn( "Execution results of fitness plugin have NOT OK" +
                          " status." );
             return Double.MAX_VALUE;
         }
     
         try { 
             double fitness = Double.valueOf(res.data).doubleValue(); 
             logger.info( "Fitness value = " + fitness );
             return fitness;
         }
         catch ( NumberFormatException e ) {
             logger.warn( "Execution results of fitness plugin does not " +
                          "have correct float fitness value." );
             return Double.MAX_VALUE;
         }
     }
 
     public double evaluate ( IChromosome a_chromosome ) 
     {
         int numGenes = a_chromosome.size();
 
         StringUUIDObject chromosomeXml = chromosomeToXml( a_chromosome );
 
         UUIDObject[] args = new UUIDObject[]{ truss, chromosomeXml };
 
         try { 
             // Execute fitness plugin with specified chromosome
             Plugin.ExecutionResult fitnessXml = fitnessPlugin.execute( args );
             return resultToFitness( fitnessXml );
 
         } catch ( Plugin.ContextIsNotValidException e ) {
             logger.error( "Plugin.ContextIsNotValidException exception" );
             gaPlugin.contextException = e;
             throw new RuntimeException();
         }
         catch ( Plugin.ParamsAreNotAcceptedException e ) {
             logger.error( "Plugin.ParamsAreNotAcceptedException exception" );
             gaPlugin.paramsException = e;
             throw new RuntimeException();
         }
         catch ( Plugin.DependenciesAreNotResolvedException e ) {
             logger.error( "Plugin.DependenciesAreNotResolvedException " +
                           "exception" );
             gaPlugin.dependException = e;
             throw new RuntimeException();
 
         }
         catch ( Plugin.WrongExecutionArgsException e ) {
             gaPlugin.wrongArgsException = e;
             throw new RuntimeException();
         }
     }
 }
 
 public final class GAOptimizationPlugin extends JavaPlugin
 {
 // Members     
 
     /** Demo plugin logger */
     private static Logger logger = Logger.getLogger(
                                "java.plugin.GAOptimizationPlugin");
 
     /** Execution argument types */
     private static Class[] m_execArgumentTypes = 
         new Class[]{ TrussUnit.class };
 
     public Plugin.ContextIsNotValidException contextException = null;
     public Plugin.ParamsAreNotAcceptedException paramsException = null;
     public Plugin.DependenciesAreNotResolvedException dependException = null;
     public Plugin.WrongExecutionArgsException wrongArgsException = null;
 
 // Methods
     public GAOptimizationPlugin ( PluginManager pluginMng, String pluginPath )
     { super(pluginMng, pluginPath); }
     
     protected Class<?>[] executionArgumentTypes ()
     { return m_execArgumentTypes; }
 
     protected ExecutionResult specificExecute ( 
                                            PluginExecutionParams params,
                                            UUIDObject[] args,
                                            Hashtable<String, Plugin[]> deps  )
         throws ContextIsNotValidException,
                ParamsAreNotAcceptedException,
                DependenciesAreNotResolvedException,
                WrongExecutionArgsException
     {        
         logger.debug( "specificExecute" );
 
         if ( args == null || args.length != 1 ) {
             logger.error("args == null or args.length != 1");
             throw new WrongExecutionArgsException(getUUID());
         }
 
         
         TrussUnit truss = null;
         try { truss = (TrussUnit)args[0]; }
         catch ( Exception e ) {
             logger.error("Argument is not a truss unit instance");
             throw new WrongExecutionArgsException(getUUID());
         }
 
         if ( deps.values().size() != 1 ) {
             logger.error("Dependencies are not resolved");
             DependenciesAreNotResolvedException e =
                 new DependenciesAreNotResolvedException(getUUID());
             for ( String type : requiredPluginTypes() )                 
                 e.unresolvedTypes.add( type );
             throw e;
         }
 
         Iterator<Plugin[]> it = deps.values().iterator();
         Plugin[] plgArr = it.next();
         if ( plgArr == null || plgArr.length != 1 ) {
             logger.error("Dependencies are not resolved");
             DependenciesAreNotResolvedException e =
                 new DependenciesAreNotResolvedException(getUUID());
             for ( String type : requiredPluginTypes() )                 
                 e.unresolvedTypes.add( type );
             throw e;
         }
 
         Plugin fitnessPlugin = plgArr[0];
         
         Configuration conf;
         try {
             Configuration.reset();
             conf = //new DefaultConfiguration();
                 new Configuration();
         }
         catch ( Exception e ) {
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             e.printStackTrace(new PrintStream(out));
             String excpMsg = out.toString();
             logger.error(excpMsg);
 
             return new ExecutionResult( Status.InternalErrStatus, excpMsg );
         }
 
         Genotype population;
 
         Map<String, Variant> hashParams = params.getParams();
         
         try {
             // set up a sample chromosome
             int genesNum = hashParams.get("genes").getInt();
             double minValue = hashParams.get("minValue").getDouble();
             double maxValue = hashParams.get("maxValue").getDouble();
             Gene[] sampleGenes = new Gene[genesNum];
             for ( int i =0; i < genesNum; ++i )
                 sampleGenes[i] = new DoubleGene(conf, minValue, maxValue);
 
             IChromosome sampleChromosome = new Chromosome(conf, sampleGenes);
             FitnessFunction fitFunc = new GAOptimizationFitnessFunction( 
                                                   this, fitnessPlugin, truss );
 
             conf.setFitnessFunction(fitFunc);
             // Do minimization
             conf.setFitnessEvaluator(new DeltaFitnessEvaluator());
             conf.setSampleChromosome(sampleChromosome);
             BestChromosomesSelector bestChromsSelector = new
                 BestChromosomesSelector(conf, 1.0d);
             bestChromsSelector.setDoubletteChromosomesAllowed(false);
             conf.addNaturalSelector(bestChromsSelector, true);
             conf.setRandomGenerator(new StockRandomGenerator());
             conf.setEventManager(new EventManager());
             conf.addGeneticOperator(new CrossoverOperator(conf));
             conf.addGeneticOperator(new MutationOperator(conf, 15));
             conf.setPopulationSize( hashParams.get("populations").getInt() );
             population = Genotype.randomInitialGenotype(conf);
         }
         catch ( InvalidConfigurationException e ) {
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             e.printStackTrace(new PrintStream(out));
             String excpMsg = out.toString();
             logger.error(excpMsg);
             
             return new ExecutionResult( Status.InternalErrStatus, excpMsg );
         }
         // We expect the rest of the config parameter, for example 
         // the population size, to be set via the config file
 
         // Clean exceptions
         contextException = null;
         paramsException = null;
         dependException = null;
         wrongArgsException = null;        
         
         // Evolve the population
         try { population.evolve(); }
         catch ( RuntimeException e ) {
             if ( contextException != null )
                 throw contextException;
             else if ( paramsException != null )
                 throw paramsException;
             else if ( dependException != null )
                 throw dependException;
             else if ( wrongArgsException != null )
                 throw wrongArgsException;
 
             // Unexpected exception
             throw e;
         }
 
         IChromosome bestSolutionSoFar = population.getFittestChromosome();
         System.out.println("The best solution has a fitness value of " +
                            bestSolutionSoFar.getFitnessValue());
         Double aVal = (Double) bestSolutionSoFar.getGene(0).getAllele();
         Double bVal = (Double) bestSolutionSoFar.getGene(1).getAllele();
         System.out.println("a = " + aVal.doubleValue());
         System.out.println("b = " + bVal.doubleValue());
        
         return new ExecutionResult( Status.OkStatus, "Nothing from Java" );
     }
 
     public PluginInfo pluginInfo ()
     { 
         return new PluginInfo( 
                   "Optimization.GA.JGAP", 
                   "Genetic Java optimization algorithm.<br>" +
                   "(with support of JGAP, http://jgap.sourceforge.net/ )",
                   "optimization.ga.algorithm.java" );
     }
                              
     public Status pluginStatusCode ()
     { return Status.OkStatus; }
 
     public String pluginStatusMsg ()
     { return new String(); }
 
     public void tryToAcceptParams ( PluginExecutionParams params )
         throws ParamsAreNotAcceptedException
     {
         Map<String, Variant> hashParams = params.getParams();
 
         ParamsAreNotAcceptedException e = 
             new ParamsAreNotAcceptedException(getUUID());
         
         // Check populations
         if ( ! hashParams.containsKey("populations") )
             e.problemList.put( "populations", "Does not exist" );
         
        if ( hashParams.get("populations").getType() != VariantTypes.INT )
             e.problemList.put( "populations",  "Is not integer" );
         
        if ( hashParams.get("populations").getInt() < 50 || 
             hashParams.get("populations").getInt() > 1000 )
             e.problemList.put( "populations", "Should be [50, 1000]" );
         
         
         // Check genes
         if ( ! hashParams.containsKey("genes") )
             e.problemList.put( "genes", "Does not exist" );
         
        if ( hashParams.get("genes").getType() != VariantTypes.INT )
             e.problemList.put( "genes",  "Is not integer" );
     
        if ( hashParams.get("genes").getInt() < 2 || 
             hashParams.get("genes").getInt() > 100 )
             e.problemList.put( "genes", "Should be [2, 100]" );
 
         
         // Check minValue
         if ( ! hashParams.containsKey("minValue") )
             e.problemList.put( "minValue", "Does not exist" );
         
        if ( hashParams.get("minValue").getType() != VariantTypes.DOUBLE )
             e.problemList.put( "minValue",  "Is not double" );
         
        if ( hashParams.get("minValue").getDouble() < 0.0 || 
             hashParams.get("minValue").getDouble() > 1000.0 )
             e.problemList.put( "minValue", "Should be [0.0, 1000.0]" );
 
 
         // Check maxValue
         if ( ! hashParams.containsKey("maxValue") )
             e.problemList.put( "maxValue", "Does not exist" );
 
        if ( hashParams.get("maxValue").getType() != VariantTypes.DOUBLE )
             e.problemList.put( "maxValue",  "Is not double" );
 
        if ( hashParams.get("maxValue").getDouble() < 0.0 || 
              hashParams.get("maxValue").getDouble() > 1000.0 )
             e.problemList.put( "maxValue", "Should be [0.0, 1000.0]" );
 
         
         if ( e.problemList.size() != 0 )
             throw e;
     }
 
 
     public DependenceMode dependenceMode ()
     { return DependenceMode.MultiDependent; }
 
     public String[] requiredPluginTypes ()
     { return new String[] { "optimization.ga.fitness.function.*" }; }
 
     public boolean isUserExecutable ()
     { return true; }    
 }
