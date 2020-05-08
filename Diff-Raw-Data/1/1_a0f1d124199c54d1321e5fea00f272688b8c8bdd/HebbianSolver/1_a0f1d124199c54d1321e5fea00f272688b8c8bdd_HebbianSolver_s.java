 package org.decomposer.math;
 
 import java.util.List;
 import java.util.Properties;
 import java.util.Random;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.decomposer.math.vector.DiskBufferedDoubleMatrix;
 import org.decomposer.math.vector.DoubleMatrix;
 import org.decomposer.math.vector.HashMapDoubleMatrix;
 import org.decomposer.math.vector.MapVector;
 import org.decomposer.math.vector.ParallelMultiplyingDiskBufferedDoubleMatrix;
 import org.decomposer.math.vector.VectorFactory;
 import org.decomposer.math.vector.array.DenseMapVectorFactory;
 
 public class HebbianSolver
 {
   public static String EIGEN_VECT_DIR = "eigenVectors";
   public static String EIGEN_VALUE_DIR = "eigenValues";
     
   private static final Logger log = Logger.getLogger(HebbianSolver.class.getName());
   protected final EigenUpdater _updater;
   protected final VectorFactory _corpusProjectionsVectorFactory;
   protected final VectorFactory _eigensVectorFactory;
   protected final SingularVectorVerifier _verifier;
   protected final double _convergenceTarget;
   protected final int _maxPassesPerEigen;
   protected final File _baseDir;
   protected final File _eigenVectorDir;
   protected final File _eigenValueDir;
   
   protected int _numPasses = 0;
   protected boolean debug = false;
   
   public HebbianSolver(EigenUpdater updater, 
                        VectorFactory corpusProjectionsVectorFactory, 
                        VectorFactory eigensVectorFactory,
                        SingularVectorVerifier verifier,
                        String baseDir,
                        double convergenceTarget,
                        int maxPassesPerEigen)
   {
     _updater = updater;
     _corpusProjectionsVectorFactory = corpusProjectionsVectorFactory;
     _eigensVectorFactory = eigensVectorFactory;
     _verifier = verifier;
     _convergenceTarget = convergenceTarget;
     _maxPassesPerEigen = maxPassesPerEigen;
     _baseDir = baseDir == null ? null : new File(baseDir);
     _eigenVectorDir = baseDir == null ? null : new File(_baseDir.getPath() + File.separator + EIGEN_VECT_DIR);
     _eigenValueDir = baseDir == null ? null : new File(_baseDir.getPath() + File.separator + EIGEN_VALUE_DIR);
     setupDirectories();
   }
   
   public HebbianSolver(EigenUpdater updater,
                        VectorFactory corpusProjectionsVectorFactory,
                        VectorFactory eigensVectorFactory,
                        SingularVectorVerifier verfier,
                        String baseDir,
                        double convergenceTarget)
   {
     this(updater, 
          corpusProjectionsVectorFactory, 
          eigensVectorFactory, 
          verfier, 
          baseDir,
          convergenceTarget, 
          Integer.MAX_VALUE);
   }
   
   public HebbianSolver(double convergenceTarget, int maxPassesPerEigen, String baseDir)
   {
     this(new HebbianUpdater(),
          new DenseMapVectorFactory(),
          new DenseMapVectorFactory(),
          new MultiThreadedEigenVerifier(),
          baseDir,
          convergenceTarget,
          maxPassesPerEigen);
   }
   
   public HebbianSolver(double convergenceTarget, String baseDir)
   {
     this(convergenceTarget, Integer.MAX_VALUE, baseDir);
   }
   
   public HebbianSolver(int numPassesPerEigen, String baseDir)
   {
     this(0d, numPassesPerEigen, baseDir);
   }
   
   public HebbianSolver(int numPassesPerEigen)
   {
     this(0d, numPassesPerEigen, null);
   }
   
   public TrainingState solve(DoubleMatrix corpus, 
                              int desiredRank)
   {
     DoubleMatrix eigens = new HashMapDoubleMatrix(_eigensVectorFactory);
     List<Double> eigenValues = new ArrayList<Double>();
     log.info("Finding " + desiredRank + " singular vectors of matrix with " + corpus.numRows() + " rows");
     DoubleMatrix corpusProjections = new HashMapDoubleMatrix(_corpusProjectionsVectorFactory); 
     TrainingState state = new TrainingState(_corpusProjectionsVectorFactory, eigens, corpusProjections);
     state.currentEigens = eigens;
     for(int i=0; i<desiredRank; i++)
     {
       MapVector currentEigen = _eigensVectorFactory.zeroVector();
       MapVector previousEigen = null;
       while(hasNotConverged(currentEigen, corpus, state))
       {
         int randomStartingIndex = getRandomStartingIndex(corpus, eigens);
         MapVector initialTrainingVector = corpus.get(randomStartingIndex);
         state.trainingIndex = randomStartingIndex;
         _updater.update(currentEigen, initialTrainingVector, state);
         for(Entry<Integer, MapVector> vectorEntry : corpus)
         {
           state.trainingIndex = vectorEntry.getKey();
           if(vectorEntry.getKey() != randomStartingIndex)
             _updater.update(currentEigen, vectorEntry.getValue(), state);
         }
         state.firstPass = false;
         if(debug)
         {
           if(previousEigen == null)
           {
             previousEigen = currentEigen.clone();
           }
           else
           {
             double dot = currentEigen.dot(previousEigen);
             if(dot > 0) dot /= (currentEigen.norm() * previousEigen.norm());
             log.info("Current pass * previous pass = " + dot);
           }
         }
       }
       // converged!
       double eigenValue = state.statusProgress.get(state.statusProgress.size()-1).getEigenValue();
       currentEigen.scale(1/currentEigen.norm());
       eigens.set(i, currentEigen);
       eigenValues.add(eigenValue);
       state.currentEigenValues = eigenValues;
       if(_eigenVectorDir != null)
       {
         try
         {
           DiskBufferedDoubleMatrix.persistVector(_eigenVectorDir, currentEigen, i);
           MapVector eigensAsVector = _eigensVectorFactory.zeroVector();
           for(int j=0; j<eigenValues.size(); j++) eigensAsVector.set(j, eigenValues.get(j));
           DiskBufferedDoubleMatrix.persistVector(_eigenValueDir, eigensAsVector, 0);
         }
         catch(IOException ioe)
         {
           throw new RuntimeException(ioe);
         }
       }
       state.firstPass = true;
       state.helperVector = _eigensVectorFactory.zeroVector();
       state.activationDenominatorSquared = 0;
       state.activationNumerator = 0;
       _numPasses = 0;
     }
     return state;
   }
   
   private void setupDirectories()
   {
     createDir(_baseDir);
     createDir(_eigenValueDir);
     createDir(_eigenVectorDir);
   }
   
   private void createDir(File dir)
   {
     if(dir != null && !dir.exists() && !dir.mkdir()) throw new RuntimeException("Unable to create: " + dir.getName());
   }
 
   private int getRandomStartingIndex(DoubleMatrix corpus, DoubleMatrix eigens)
   {
     int index;
     MapVector v = null;
     do
     {
       double r = new Random(System.nanoTime()).nextDouble();
       index = (int)(r * corpus.numRows());
       v = corpus.get(index);
     }
     while(v == null || v.norm() == 0 || v.numNonZeroEntries() < 5);
     return index;
   }
   
   protected boolean hasNotConverged(MapVector currentPseudoEigen, 
                                     DoubleMatrix corpus, 
                                     TrainingState state)
   {
     _numPasses++;
     if(state.firstPass)
     {
       log.info("First pass through the corpus, no need to check convergence...");
       return true;
     }
     DoubleMatrix previousEigens = state.currentEigens;
     log.info("Have made " + _numPasses + " passes through the corpus, checking convergence...");
     for(Entry<Integer, MapVector> eigenEntry : previousEigens)
     {
       currentPseudoEigen.plus(eigenEntry.getValue(), -state.helperVector.get(eigenEntry.getKey()));
       state.helperVector.set(eigenEntry.getKey(), 0);
     }
     if(debug && currentPseudoEigen.norm() > 0)
     {
       for(Entry<Integer, MapVector> eigenEntry : previousEigens)
       {
         log.info("dot with previous: " + (eigenEntry.getValue().dot(currentPseudoEigen)) / currentPseudoEigen.norm());
       }
     }
     EigenStatus status = verify(corpus, currentPseudoEigen);
     if(status.getCosAngle() == 0)
     {
       log.info("Verifier not finished, making another pass...");
     }
     else
     {
       log.info("Has 1 - cosAngle: " + (1-status.getCosAngle()) + ", convergence target is: " + _convergenceTarget);
       state.statusProgress.add(status);
     }   
     return (state.statusProgress.size() <= _maxPassesPerEigen && 1 - status.getCosAngle() > _convergenceTarget);
   }
   
   protected EigenStatus verify(DoubleMatrix corpus, MapVector currentPseudoEigen)
   {
     return _verifier.verify(corpus, currentPseudoEigen);
   }
   
   public static void main(String args[]) throws FileNotFoundException, IOException
   {
     Properties props = new Properties();
     String propertiesFile = args.length > 0 ? args[0] : "config/solver.properties";
     props.load(new FileInputStream(propertiesFile));
     
     String corpusDir = props.getProperty("solver.input.dir");
     String outputDir = props.getProperty("solver.output.dir");
     if(corpusDir == null || corpusDir.equals("") || outputDir == null || outputDir.equals("")) 
     {
       log.severe(propertiesFile + " must contain values for solver.input.dir and solver.output.dir");
       System.exit(1);
     }
     int inBufferSize = Integer.parseInt(props.getProperty("solver.input.bufferSize"));
     int rank = Integer.parseInt(props.getProperty("solver.output.desiredRank"));
     double convergence = Double.parseDouble(props.getProperty("solver.convergence"));
     int maxPasses = Integer.parseInt(props.getProperty("solver.maxPasses"));
     int numThreads = Integer.parseInt(props.getProperty("solver.verifier.numThreads"));
 
     HebbianUpdater updater = new HebbianUpdater();
     SingularVectorVerifier verifier = new MultiThreadedEigenVerifier();
     HebbianSolver solver = new HebbianSolver(updater, 
                                              new DenseMapVectorFactory(),
                                              new DenseMapVectorFactory(), 
                                              verifier, 
                                              outputDir, 
                                              convergence, 
                                              maxPasses);
     DoubleMatrix corpus;
     if(numThreads <= 1)
     {
       corpus = new DiskBufferedDoubleMatrix(new File(corpusDir), inBufferSize);
     }
     else
     {
       corpus = new ParallelMultiplyingDiskBufferedDoubleMatrix(new File(corpusDir), inBufferSize, numThreads);
     }
     long now = System.currentTimeMillis();
     TrainingState finalState = solver.solve(corpus, rank);
     long time = (long)((System.currentTimeMillis() - now)/1000);
     log.info("Solved " + finalState.currentEigens.numRows() + " eigenVectors in " + time + " seconds.  Persisted to " + outputDir);
   }
   
 }
