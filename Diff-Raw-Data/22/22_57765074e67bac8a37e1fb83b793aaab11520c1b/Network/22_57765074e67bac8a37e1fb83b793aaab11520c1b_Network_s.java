 package pl.eiti.idsnn.model;
 
 import java.util.ArrayList;
 import java.util.List;
 import pl.eiti.idsnn.UnsuitableDataException;
 
 /**
  * A neural network class.
  */
 public class Network {
   List<Layer> layers = new ArrayList<Layer>();
   double nuFactor;
 
   public Network() {}
 
   /**
    * Method responsible for adding new Layer to Network.
    */
   public void addLayer(Layer newLayer) {
     layers.add(newLayer);
     if(layers.size() == 1) return;
     Layer previousLayer = layers.get(layers.size() - 2);
     previousLayer.addNextLayer(newLayer);
     newLayer.addPrevLayer(previousLayer);
   }
 
   /**
    * Method responsible for calculating the results from initial values.
    * @return results of the forward propagation - vector of values from last Layer.
    * @throws Exception 
    */
   public List<Double> forwardPropagate(final double[] inputValues) throws UnsuitableDataException {
 
     if(inputValues.length != layers.get(0).getNeurons().size())
       throw new UnsuitableDataException();
 
     //feed data to first layer
     int i = 0;
     for(Neuron neuron : layers.get(0).getNeurons()){
       neuron.setCurrentValue(inputValues[i]);
       i++;
     }
 
     for(Layer layer : layers)
       layer.propagateForward();
 
     //return results
     List<Double> results = new ArrayList<Double>();
     for(Neuron neuron : layers.get(layers.size()-1).getNeurons())
       results.add(neuron.getCurrentValue());
 
     return results;
   }
 
  public void backPropagate(double templateOutput){
    updateDeltaInOutputLayer(templateOutput);
     updateDeltasInHiddenLayers();
     correctWeights();
   }
   
  private void updateDeltaInOutputLayer(double templateOutput) {
     int numOfLayers = layers.size()-1;
     Neuron outputNeuron = (layers.get(numOfLayers).getNeurons()).get(0);
 
     double outputValue, delta, error;
     outputValue = outputNeuron.getCurrentValue();
     error = templateOutput - outputValue;
     
     // error times derivative of activation function
     delta = error * (outputValue * (1 - outputValue)); 
     outputNeuron.setDelta(delta);
   }
 
   private void updateDeltasInHiddenLayers() {
    // for each hidden layer
    for (int i = layers.size()-2; i>=0; --i) {
       for(Neuron neuron : layers.get(i).getNeurons()) {
         neuron.updateDelta();
       }
     }
   }
   
   private void correctWeights() {
    for(Layer layer : layers) {
       for(Neuron neuron: layer.getNeurons()) {
         neuron.updateWeights(nuFactor);
       }
     }
   }
 }
