 /*
  * 
  */
 package testjnn;
 
 import beans.DayBean;
 import java.io.FileNotFoundException;
 import java.io.IOException;
import java.util.Arrays;
 import java.util.List;
import java.util.Vector;
 import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
 import org.neuroph.core.NeuralNetwork;
 import org.neuroph.core.learning.SupervisedTrainingElement;
 import org.neuroph.core.learning.TrainingElement;
 import org.neuroph.core.learning.TrainingSet;
 import org.neuroph.nnet.MultiLayerPerceptron;
 import org.neuroph.nnet.learning.LMS;
 import org.neuroph.nnet.learning.MomentumBackpropagation;
 import utils.CSVHandler;
 import utils.Normalize;
 
 /**
  *
  * @author Marco Tinacci
  */
 public class Main {
    static private final int N_LEARN = 1500;
    static private final int N_TEST = 350;
 
    // dimensioni rete
    static private final int NNET_INPUT_LAYER = 30;
    static private final int NNET_HIDDEN_LAYER = 30;
    static private final int NNET_OUTPUT_LAYER = 1;
 
   //
    static private final double MIN_RANGE = 0.1;
    static private final double MAX_RANGE = 0.9;
 
   static private final String INDEX_FILE = "SP500";
 
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
       Main main = new Main();
       List<DayBean> list = CSVHandler.readAll("data/" + INDEX_FILE + ".csv");
       //System.out.println(list.toString().replace("], [", "]\n["));
       main.testNN(list);
 
    }
 
    public void testNN(List<DayBean> days) throws FileNotFoundException, IOException {
       // creazione della rete neurale
       NeuralNetwork nnet = new MultiLayerPerceptron(NNET_INPUT_LAYER,
               NNET_HIDDEN_LAYER, NNET_OUTPUT_LAYER);
      nnet.setLearningRule(new MomentumBackpropagation());
      //nnet.setLearningRule(new TDPBackPropagation());
      ((MomentumBackpropagation) nnet.getLearningRule()).setMomentum(0);
       ((LMS) nnet.getLearningRule()).setLearningRate(0.1);
       ((LMS) nnet.getLearningRule()).setMaxError(0.0001);
       ((LMS) nnet.getLearningRule()).setMaxIterations(1000);
 
       double max = 0;
       double min = Double.MAX_VALUE;
       for(int i = 0; i < N_LEARN; i++){
          double x = days.get(i).getClose();
          if(x < min){
             min = x;
          }
          if(x > max){
             max = x;
          }
       }
 
       
       // creazione dell'insieme di dati di addestramento
       TrainingSet trainSet = new TrainingSet();
       // ciclo su training sets
       for(int i = 0; i < N_LEARN; i++){
          double v[] = new double[NNET_INPUT_LAYER];
          // ciclo sul singolo input
          for(int j = 0; j < NNET_INPUT_LAYER; j++){
             v[j] = Normalize.normalize(days.get(i+j).getClose(), min, max,
                     MIN_RANGE, MAX_RANGE);
          }
          // aggiungi l'input e l'output
          trainSet.addElement(new SupervisedTrainingElement(
             v, new double[]{
                   Normalize.normalize(days.get(i+NNET_INPUT_LAYER).getClose(), 
                           min, max, MIN_RANGE, MAX_RANGE)})
          );
       }
 
       // addestramento della rete
       nnet.learnInSameThread(trainSet);
 
       // test errore
       TrainingSet testSet = new TrainingSet();
       for(int i = 0; i < N_TEST; i++){
          double v[] = new double[NNET_INPUT_LAYER];
          for(int j = 0; j < NNET_INPUT_LAYER; j++){
             v[j] = Normalize.normalize(days.get(N_LEARN+i+j).getClose(), min,
                     max, MIN_RANGE, MAX_RANGE);
          }
          testSet.addElement(new TrainingElement(v));
       }
 
       double errors[] = new double[testSet.trainingElements().size()];
       double targets[] = new double[testSet.trainingElements().size()];
       for(int i = 0; i < testSet.trainingElements().size(); i++){
          nnet.setInput(testSet.trainingElements().get(i).getInput());
          nnet.calculate();
          double[] networkOutput = nnet.getOutput();
          double v1 = days.get(N_LEARN+NNET_INPUT_LAYER+i).getClose();
          double v2 = Normalize.denormalize(networkOutput[0], min, max,
                     MIN_RANGE, MAX_RANGE);
          errors[i] = (Math.abs(v1 - v2) / v1 );
          targets[i] = v1;
       }
       DescriptiveStatistics ds = new DescriptiveStatistics(errors);
       System.out.println("Errore medio: "+ ds.getMean());
       ds = new DescriptiveStatistics(targets);
       double stdDeviation = ds.getStandardDeviation();
       System.out.println("Deviazione standard: "+ stdDeviation);
 
       // test profitto
       double stocks = 0;
       double seed = 1000000;
       int currentSign = -1; // {-1, +1}
       int nextSign;
       double todayVal = 0;
 
       double v_days[] = new double[260];
       double v_values[] = new double[260];
       double v_targets[] = new double[260];
 
       for(int i = 0; i < 260; i++){
          
          nnet.setInput(testSet.trainingElements().get(i).getInput());
          nnet.calculate();
          double[] networkOutput = nnet.getOutput();
          todayVal = testSet.trainingElements().get(i)
                  .getInput()[NNET_INPUT_LAYER-1];
          // TODO segno uguale a zero??
          // calcolo della previsione
          double diff = Normalize.denormalize(networkOutput[0], min, max,
                  MIN_RANGE, MAX_RANGE) - Normalize.denormalize(todayVal, min,
                  max, MIN_RANGE, MAX_RANGE);
 //         if(Math.abs(diff)/Normalize.denormalize(todayVal, min,
   //               max, MIN_RANGE, MAX_RANGE) > 0.01){
             nextSign = (int)Math.signum(diff);
             // decisione
             if(currentSign == -1 && nextSign == 1){
                stocks = seed / Normalize.denormalize(todayVal, min, max,
                        MIN_RANGE, MAX_RANGE);
                //System.out.println("giorno: "+ (N_LEARN+NNET_INPUT_LAYER+i) +
                //        " compra "+ stocks +" azioni per "+ seed + "$.");
 
                seed = 0;
                currentSign = 1;
             }else if(currentSign == 1 && nextSign == -1){
                seed = stocks * Normalize.denormalize(todayVal, min, max,
                        MIN_RANGE, MAX_RANGE);
                System.out.println("giorno: "+ (N_LEARN+NNET_INPUT_LAYER+i) +
                        " vendi "+ stocks +" azioni per "+ seed + "$.");
                stocks = 0;
                currentSign = -1;
             }
          v_days[i] = N_LEARN + NNET_INPUT_LAYER + i;
          v_values[i] = seed;
          v_targets[i] = Normalize.denormalize(networkOutput[0], min, max, MIN_RANGE, MAX_RANGE);
 //         }
       }
 
       double v[][] = new double[3][];
       v[0] = v_days;
       v[1] = v_values;
       v[2] = v_targets;
      CSVHandler.writeArray(v, "data/result_"+INDEX_FILE+".csv");
 
       System.out.println("Ricavo finale: " + (seed + stocks *
               Normalize.denormalize(todayVal, min, max, MIN_RANGE, MAX_RANGE)));
 //      System.out.println("c1: "+((TDPBackPropagation)nnet.getLearningRule()).c1);
 //      System.out.println("c2: "+((TDPBackPropagation)nnet.getLearningRule()).c2);
 //      System.out.println("c3: "+((TDPBackPropagation)nnet.getLearningRule()).c3);
 //      System.out.println("c4: "+((TDPBackPropagation)nnet.getLearningRule()).c4);
 //      System.out.println("cplus: "+((TDPBackPropagation)nnet.getLearningRule()).cplus);
    }
 }
