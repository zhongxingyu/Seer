 package geneticAlogrithm;
 
 import java.io.File;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.Semaphore;
 
 import messages.MessageFromPraat;
 import messages.MessageToPraat;
 import monitoring.MonitoringCSV;
 
 import org.uncommons.maths.random.MersenneTwisterRNG;
 import org.uncommons.maths.random.Probability;
 import org.uncommons.watchmaker.framework.EvolutionEngine;
 import org.uncommons.watchmaker.framework.EvolutionObserver;
 import org.uncommons.watchmaker.framework.EvolutionaryOperator;
 import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
 import org.uncommons.watchmaker.framework.PopulationData;
 import org.uncommons.watchmaker.framework.SelectionStrategy;
 import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
 import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
 import org.uncommons.watchmaker.framework.termination.GenerationCount;
 
 
 import org.uncommons.watchmaker.framework.termination.TargetFitness;
 
 import praatGestion.OrderToPraat;
 import praatGestion.Praat;
 import communication.ServerThread;
 import elements.FormantSequence;
 import elements.GlobalAlphabet;
 import elements.Sequence;
 import exceptions.FormantNumberexception;
 import exceptions.PraatScriptException;
 
 /** <p>This is the main Class for the GA.<br/>
  * It use the Genetic algorithm device provide by the watchMaker's API and the class I redefined for it. See the corresponding doc for more information<br/>
  * In order to get a clean code, i have divided the big operation in small functions.<br/>
  * All this methods are called in the function startAlgorithm.</p>
  * 
  * @see Sequence
  * @see SequenceEvaluator
  * @see SequenceCrossOver
  * @see SequenceFactory
  * @see SequenceMutation
  *  
  * @author Pierre-Yves Hervo
  * @version 0.1
  */
 public class GeneticAlgorithmCall{
 	
 	/**
 	 * The length of the sequence we will use. It musn't be modified ones it is set.
 	 * 
 	 */
 	int length;
 	
 	/**
 	 * The alphabet with all the possible values for the box of a sequence.
 	 * 
 	 */
 	private GlobalAlphabet alphabet;
 	
 	/**
 	 * The values of the formant in the target
 	 * 
 	 */
 	private FormantSequence target;
 	
 	/**
 	 * The sequenceFactory used by the GA
 	 * 
 	 */
 	private SequenceFactory mySequenceFactory;
 	
 	/**
 	 * The pipeline that allow to use both mutation qnd cross over in the GA, See the watchMaker doc for more information
 	 * 
 	 */
 	private EvolutionaryOperator<Sequence> pipeline;
 	
 	/**
 	 * The SequenceEvaluation used by the GA
 	 * 
 	 */
 	private SequenceEvaluator mySeqEval;
 	
 	/**
 	 * The SelectionStrategy used by the GA
 	 * 
 	 */
 	private SelectionStrategy<Object> selection;
 	
 	/**
 	 * The Random generator used by the GA
 	 * 
 	 */
 	private Random rng;
 	
 	/**
 	 * The EvolutionEngine used by the GA
 	 * 
 	 */
 	private EvolutionEngine<Sequence> engine;
 	
 	/**
 	 * The message received From Praat by the server. It is a String so it must be cast by the splitChaineToFormantSequence function 
 	 * 
 	 * @see MessageFromPraat#splitChaineToFormantSequence(String)
 	 */
 	private FormantSequence messageFromPraat;
 	
 	/**
 	 * A mutex to protect the value of messageFromPraat and be sure it is not read and modify in the same time
 	 * 
 	 * @see  GeneticAlgorithmCall#messageFromPraat
 	 */
 	private final Semaphore availablevalue = new Semaphore(1, true);
 	/**
 	 * A mutex to protect the execution of the fitness function. The goal is to forbid it to be run while the previous execution isnt finished.
 	 * this is necessary cause of the delay of praat and the GA. So we need to synchronized them this way. 
 	 * 
 	 * @see  GeneticAlgorithmCall#messageFromPraat
 	 */
 	private final Semaphore availableFitnessfunction = new Semaphore(1, true); 
 	
 	/**
 	 * Var to store the best candidate a each generation. 
 	 * This value will be use at he end to write the correct praat script.
 	 */
 	private Sequence finalsequence;
 	
 	/**
 	 * praat object wich indicate the state in wich praat is. It will be use to made praat evolv beetween the different possible state
 	 * and be sure the differnet operations are used at the good time.
 	 * 
 	 */
 	
 	/**
 	 * indicate the time at which we launch the algorithm, it is utse to calculate the execution time.
 	 * initiate in the constructor
 	 */
 	private long start=0;
 	
 	private Praat praatObject;
 	
 	/**
 	* Constructor where we specified the length of the sequence we will use. Initialize the other attribute to null.
 	* 
 	*
 	* @param length
 	* 	the length of the Sequence use in the GA
 	 * @throws FormantNumberexception 
 	* 
 	* @see GeneticAlgorithmCall#length
 	*
 	* @since 0.1
 	*
 	*/
 	public GeneticAlgorithmCall(int length) throws FormantNumberexception {
 		super();
 		this.length=length;
 		this.alphabet=null;
 		this.target=null;
 		this.mySequenceFactory= null;
 		this.pipeline=null;
 		this.mySeqEval=null;
 		this.selection=null;
 		this.rng=null;
 		this.engine=null;
 		this.messageFromPraat=new FormantSequence(2); //it is just an init
 		this.finalsequence=null;
 		this.praatObject=null;
 		this.start=System.currentTimeMillis();
 		//deleting the files in the folder containing the previous results to avoid keeping result that doesnt suit the current run
 		emptyDirectory(new File("C:/Users/phervo/Documents/dossierProjet/results"));
 	}
 	
 	
 	/**
 	 * build the Praat object and lanch it
 	 */
 	public void buildPraatState(){
 		this.praatObject=new Praat();
 		praatObject.launch();
 	}
 	
 	
 	/**
 	* Initialize the target
 	*
 	*
 	* @since 0.1
 	*
 	*/
 	public void buildTarget(){
 		/*its here that we define the target*/
 		this.target=new FormantSequence("e");
 		//this.target.displayFormantSequence();
 	}
 	
 	/**
 	* Initialize the alphabet
 	*
 	*
 	* @since 0.1
 	*
 	*/
 	public void generateAlphabet(){
 		/*It is here that we define all the possible values that can be use by the algorithm to generate a candidate
 		 * it is the list of the unitary values. They will be combined into a sequence to create a candidate by another function*/
 		this.alphabet = new GlobalAlphabet();
 	}
 	
 	/**
 	* Initialize the CandidateFactory
 	*
 	*
 	* @since 0.1
 	*
 	*/
 	public void createCandidateFactory(){
 		/*
 		 * use the alphabet define previously to create a factorycandidate of type SequenceFactory*/
 		mySequenceFactory = new SequenceFactory(this.alphabet, this.length);
 	}
 	
 	/**
 	* Initialize the EvolutionaryOperator
 	*
 	*
 	* @since 0.1
 	*
 	*/
 	public void createEvolutionaryOperator(){
 		List<EvolutionaryOperator<Sequence>> operators
 	    = new LinkedList<EvolutionaryOperator<Sequence>>();
 		operators.add(new SequenceMutation(this.alphabet,new Probability(0.02)));
 		operators.add(new SequenceCrossOver());
 		pipeline = new EvolutionPipeline<Sequence>(operators);
 	}
 	
 	/**
 	* Initialize the FitnessEvalutator
 	*
 	*
 	* @since 0.1
 	*
 	*/
 	public void createFitnessEvalutator(){
 		mySeqEval=new SequenceEvaluator(this.target,this);
 	}
 	
 	/**
 	* Initialize the Selection
 	*
 	*
 	* @since 0.1
 	*
 	*/
 	public void createSelection(){
 		selection=new RouletteWheelSelection();
 	}
 	
 	/**
 	* Initialize the RandomGenerator
 	*
 	*
 	* @since 0.1
 	*
 	*/
 	public void createRandomGenerator(){
 		rng=new MersenneTwisterRNG();
 	}
 	
 	/**
 	* Main method, use all the creating methodes above, create a evolutionObserver and launch the GA
 	*
 	*
 	* @since 0.1
 	*
 	*/
 	public void startAlgorithm(){
 		//load the different elements
 		buildPraatState();
 		this.generateAlphabet();
 		this.buildTarget();
 		this.createCandidateFactory();
 		this.createEvolutionaryOperator();
 		this.createFitnessEvalutator();
 		this.createSelection();
 		this.createRandomGenerator();
 		
 		//start the engine
 		engine = new GenerationalEvolutionEngine<Sequence>(mySequenceFactory, pipeline, mySeqEval, selection, rng);
 		engine.addEvolutionObserver(new MySequenceEvolutionObserver(this));
		engine.evolve(10, 0, new TargetFitness(2,mySeqEval.isNatural()));
 		try {
 			MessageToPraat.writePraatScriptInFile(this.finalsequence,"praatScriptWithCorrectValues");
 		} catch (PraatScriptException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//relaunch praat and display the result we saved during the run
 		praatObject.reLaunch();
 		OrderToPraat.launchAllScripts();
 		
 		//at the end, we launch the monitoring function to display the results
 		MonitoringCSV.displayCSV();
 	}
 
 	/**
 	* Method which return the message from praat store in the instance.
 	* It use a mutex to be sure the resource isn't read and write in the same time.
 	* It lock the resource at the beginning and release it at the end.
 	*
 	* @return the formant sequence where the message from praat is store
 	*
 	* @since 0.1
 	*
 	*/
 	public synchronized FormantSequence getMessageFromPraat() { 
 		FormantSequence temp=null;
 		try {
 			availablevalue.acquire();
 			temp=this.messageFromPraat;
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		availablevalue.release();
 		return temp;
 	}
 
 	/**
 	* Method which store the message from praat  in the instance.
 	* It use a mutex to be sure the resource isn't read and write in the same time.
 	* It lock the resource at the beginning and release it at the end.
 	*
 	*
 	*
 	* @since 0.1
 	*
 	*/
 	public synchronized void setMessageFromPraat(FormantSequence messageFromPraat) {
 		try
 		{
 			availablevalue.acquire();
 			this.messageFromPraat = messageFromPraat;
 		}catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		availablevalue.release();
 	}
 
 	/**
 	* Return the semaphore for the fitness function. It is used in FitnessEvaluator to forbid the function to be launch twice.
 	* 
 	* @return frequency value of the instance
 	*
 	* @since 0.1
 	*
 	*/
 	public Semaphore getMutexFitnessFunction() {
 		return availableFitnessfunction;
 	}
 	
 	/**
 	* Method which set the value of finalSeauence. it is used at each end of generation to store the value of the best candidate.
 	*
 	* @since 0.1
 	*
 	*/
 	public void setSequence(Sequence c){
 		this.finalsequence = c;
 	}
 	
 	public SequenceEvaluator getMySeqEval(){
 		return this.mySeqEval;
 	}
 	
 	
 	public Praat getPraatObject(){
 		return this.praatObject;
 	}
 	
 	public double getStartTime(){
 		return this.start;
 	}
 	
 	/**
 	   * function to delete all the files in th edirectory before using it.
 	   * It avoid to keep file which arent usefull.
 	   * @param folder
 	   */
 	  public static void emptyDirectory(File folder){
 	       for(File file : folder.listFiles()){
 	          if(file.isDirectory()){
 	            emptyDirectory(file);
 	       }
 	       file.delete();
 	     }
 	  }
 }
