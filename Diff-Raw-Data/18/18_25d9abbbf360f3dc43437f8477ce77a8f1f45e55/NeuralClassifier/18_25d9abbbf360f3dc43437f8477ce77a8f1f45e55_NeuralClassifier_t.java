 package pl.edu.agh.sa.pw;
 
 import org.neuroph.core.NeuralNetwork;
 import org.neuroph.core.learning.SupervisedTrainingElement;
 import org.neuroph.core.learning.TrainingSet;
 import org.neuroph.nnet.Perceptron;
 
 import java.io.File;
 import java.util.Arrays;
 
 public class NeuralClassifier {
 
     public static double[] analyze(String networkPath, double [] input){
         NeuralNetwork loadedPerceptron = NeuralNetwork
 				.load(networkPath);
 
         loadedPerceptron.setInput(input);
         loadedPerceptron.calculate();
         return loadedPerceptron.getOutput();
 
     }
 
     public static void loadLearnAndSave(String networkPath, double[] learningSet, double[] desiredOutput){
         NeuralNetwork perceptron;
        if((new File(networkPath)).exists()){
          perceptron = NeuralNetwork
 				.load(networkPath);
        }else{
           perceptron = new Perceptron(3, 2);
        }
        TrainingSet<SupervisedTrainingElement> trainingSet = new TrainingSet<SupervisedTrainingElement>(
 				3, 2);
        trainingSet.addElement(new SupervisedTrainingElement(learningSet, desiredOutput));
 
        perceptron.learn(trainingSet);
        perceptron.save(networkPath);
     }
 
 	public static void main(String args[]) {
         loadLearnAndSave("myN",new double[] {1,0,0},new double[]{1,0});
         System.out.print(analyze("myN",new double[] {1,0,0})[0]+ " "+analyze("myN",new double[] {1,0,0})[1]);
 
 	}
 
 }
