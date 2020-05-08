 public class Main {
 
     public static void main(String[] args) {
 
         if (args[0] != null && args[1] != null) {
             if (args[0].equalsIgnoreCase("train")) {
                 Trainer trainer = new Trainer(args[1]);
                 Neuron n = new Neuron();
                trainer.train(n, 0.3, 0.0001);
             } else if (args[0].equalsIgnoreCase("test")) {
                 Trainer tester = new Trainer(args[1]);
                 Neuron n = new Neuron("neuron.dat");
                 tester.test(n, true);
             }
         }
 
     }
 
 }
