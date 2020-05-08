 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class Trainer {
 
     ArrayList<Example> data;
 
     public Trainer(String filePath) {
 
         data = new ArrayList<Example>();
 
         //read in data from file
         try {
             BufferedReader reader = new BufferedReader(new FileReader(filePath));
 
             //skip first line
             reader.readLine();
 
             String line;
             while ((line = reader.readLine()) != null) {
                 Example e = new Example(line);
                 data.add(e);
             }
         } catch (FileNotFoundException e) {
             System.out.println("error: " + e);
         } catch (IOException e) {
             System.out.println("error: " + e);
         }
     }
 
     public void train(Neuron n, double learnrate, double acceptableError) {
         double error = 99999999.0;
         int i = 0, examples = 0, iterations = 0;
 
         while (error > acceptableError || iterations == 0) {
             if (i == data.size()) {
                 i = 0;
                 iterations++;
                System.out.println(error);
             }
             Example e = data.get(i);
             n.train(e.getAttributes(), e.getTarget(), learnrate);
             error = this.test(n, false);
             i++;
             examples++;
         }
 
         System.out.println("Weights Learned: " + n.getWeights() +
                 "\nIn " + iterations + " iterations (" + (examples - 1) + " examples).");
         n.save();
     }
 
     public double test(Neuron n, boolean output) {
         int correct = 0, total = 0;
         double error = 0.0;
 
         for (Example e : data) {
             double expected = e.getTarget();
             double actual = n.activate(e.getAttributes());
             if (output)
                 System.out.println(e + " Prediction:" + (int) Math.round(actual));
             if ((int) Math.round(expected) == (int) Math.round(actual))
                 correct++;
            else
                error++;
             total++;
         }
 
         if (output)
             System.out.println("Got " + correct + "/" + total + " examples correct. (" +
                     (double) correct / (double) total * 100 + "%)");
        return (error / total);
     }
 
 }
