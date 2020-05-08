 package ecprac.era270;
 
 import java.lang.Math;
 import java.util.Random;
 
 interface Component {
     double getOutput();
 }
 
class Synapse implements Component {
     Neuron source, target;
     double weight;
 
     private static final Random r = new Random();
 
     Synapse(Neuron s, Neuron t) {
         source = s;
         target = t;
         weight = r.nextGaussian();
     }
 
     public double getOutput() {
         return weight * source.getOutput();
     }
 
     public String toString() {
         return "Synapses with source " + source + ", target " + target + " and weight " + weight;
     }
 }
 
class Neuron implements Component {
     Synapse[] inputSynapses = null;
 
     int id, layer;
     double value;
 
     Neuron(int id, int layer, double value) {
         this.id = id;
         this.layer = layer;
         this.value = value;
     }
 
     public double getOutput() {
         if (layer == 0)
             return value;
             
         double inputs = 0;
         for (Synapse s: inputSynapses)
             inputs += s.getOutput();
 
         return 1.0 / (1.0 + Math.exp(inputs * -1.0 * 0.4));
     }
 
     public String toString() {
         return "Neuron " + id + ", layer " + layer + " with " + inputSynapses.length + " synapses";
     }
 }
 
public class FeedForward {
 
     Neuron[] inputLayer, hiddenLayer, outputLayer; 
 
     private Neuron[] initLayer(Neuron[] sourceLayer, Neuron[] destLayer) {
         for(int i=0; i<destLayer.length; i++) {
             destLayer[i].inputSynapses = new Synapse[sourceLayer.length];
 
             for(int j=0; j<sourceLayer.length; j++) 
                 destLayer[i].inputSynapses[j] = new Synapse(sourceLayer[j], destLayer[i]);
         }
         return destLayer;
     }
 
     public FeedForward(double[] values, int wantedOutputs) {
         inputLayer = new Neuron[values.length];
         hiddenLayer = new Neuron[values.length];
         outputLayer = new Neuron[wantedOutputs];
 
         for(int i=0; i<inputLayer.length; i++)
             inputLayer[i] = new Neuron(i, 0, values[i]);
 
         for(int i=0; i<hiddenLayer.length; i++)
             hiddenLayer[i] = new Neuron(i, 1, 0);
 
         for(int i=0; i<outputLayer.length; i++) 
             outputLayer[i] = new Neuron(i, 2, 0);
 
         hiddenLayer = initLayer(inputLayer, hiddenLayer);
         outputLayer = initLayer(hiddenLayer, outputLayer);
     }
 
     public void changeInputs(double[] values) {
         for(int i=0; i<inputLayer.length; i++)
             inputLayer[i].value = values[i];
     }
 
     public int numberOfSynapses() {
         int syn = 0;
 
         for (Neuron n: hiddenLayer)
             syn += n.inputSynapses.length;
 
         for (Neuron n: outputLayer)
             syn += n.inputSynapses.length;
 
         return syn;
     }
 
     public double[] getWeights() {
         int cur = 0;
         double weights[] = new double[numberOfSynapses()];
 
         for (Neuron n: hiddenLayer)
             for (Synapse s: n.inputSynapses) {
                 weights[cur] = s.weight;
                 cur++;
             }
 
         for (Neuron n: outputLayer)
             for (Synapse s: n.inputSynapses) {
                 weights[cur] = s.weight;
                 cur++;
             }
 
         return weights;
     }
 
     public void changeWeights(double[] weights) {
         int cur = 0;
 
         for (int i=0; i<hiddenLayer.length; i++)
             for (int j=0; j<hiddenLayer[i].inputSynapses.length; j++) {
                 hiddenLayer[i].inputSynapses[j].weight = weights[cur];
                 cur++;
             }
 
         for (int i=0; i<outputLayer.length; i++)
             for (int j=0; j<outputLayer[i].inputSynapses.length; j++) {
                 outputLayer[i].inputSynapses[j].weight = weights[cur];
                 cur++;
             }
     }
 
     public double[] getOutput() {
         double[] results = new double[outputLayer.length];
 
         for(int i=0; i<outputLayer.length; i++)
             results[i] = outputLayer[i].getOutput();
 
         return results;
     }
 
     public static void main(String[] args) {
         double[] inputs = new double[4];
         inputs[0] = 10;
         inputs[1] = 42;
         inputs[2] = 150;
         inputs[3] = 2;
 
         FeedForward network = new FeedForward(inputs, 1);
 
         for (double res: network.getOutput())
             System.out.println(res);
 
         double[] weights = network.getWeights();
         weights[3] = 1.0;
         network.changeWeights(weights);
 
         for (double res: network.getOutput())
             System.out.println(res);
     }
 }
