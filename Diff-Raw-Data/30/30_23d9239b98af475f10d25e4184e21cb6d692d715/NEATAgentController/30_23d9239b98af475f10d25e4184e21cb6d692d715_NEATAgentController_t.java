 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mase.neat;
 
 import mase.AgentController;
 import org.neat4j.neat.core.NEATNeuralNet;
 import org.neat4j.neat.data.core.NetworkInput;
 import org.neat4j.neat.data.core.NetworkOutputSet;
 import org.neat4j.neat.data.csv.CSVInput;
 
 /**
  *
  * @author jorge
  */
 public class NEATAgentController implements AgentController {
 
     private NEATNeuralNet network;
 
     public NEATAgentController(NEATNeuralNet network) {
         this.network = network;
     }
    
    public NEATNeuralNet getNetwork() {
        return network;
    }
 
     @Override
     public double[] processInputs(double[] input) {
         NetworkInput in = new CSVInput(input);
         NetworkOutputSet output = network.execute(in);
         return output.nextOutput().values();
     }
 
     @Override
     public void reset() {
         network.updateNetStructure();
     }
 
     @Override
     public AgentController clone() {
         NEATNeuralNet newNet = new NEATNeuralNet();
         newNet.createNetStructure(network.netDescriptor());
         newNet.updateNetStructure();
         return new NEATAgentController(newNet);
     }
 }
