 package NerualNet;
 
 public class MLPNet implements Network{
 	private int numInputs;
 	private int numLayers;
 	private int numOutputs;
 	private int numNodes;
 	private char classifier;
 	
 	private double rate;
 	
 	private Neuron[] output;
 	private Neuron[][] hidden;
 	
 	MLPNet(int inputs, int layers, int outputs, double rate, char classifier){	
 		this.numInputs = inputs;
 		this.numLayers = layers;
 		this.numOutputs = outputs;
		this.numNodes = 2 * inputs;
 		this.rate = rate;
 		this.classifier = classifier;
 		
 		this.hidden = new Neuron[this.numNodes][this.numLayers];
 		this.output = new Neuron[this.numOutputs];
 				
 		for (int i = 0; i < this.numLayers; i++){
 			for (int j = 0; j < this.numNodes; j++){
 				if(i == 0)
 					this.hidden[j][i] = new Neuron(numInputs, 2);
 				else
 					this.hidden[j][i] = new Neuron(numNodes, 2);
 			}
 		}
 		for (int j = 0; j < this.numOutputs; j++){
 			this.output[j] = new Neuron(numNodes, 1);
 		}
 	}
 
 	public void train(double[][] trainSet, int numSamples, int epochs, char[] classes){
 		double expected;
 		double[] firstIn = new double[this.numInputs];
 		double[] inputs = new double[this.numNodes];
 		double[] outputs = new double[this.numNodes];
 		double[] finalOut = new double[this.numOutputs];
 		double sum;
 		
 		int a = 0;
 		
 		while (a < epochs){
 			for (int i = 0; i < numSamples; i++){
 				this.rate = (this.rate / 1.000001);
 				
 				for (int j = 0; j < this.numInputs; j++){
 					firstIn[j] = trainSet[j][i];
 				}
 				
 				if(this.classifier == classes[i])
 					expected = 1.0;
 				else
 					expected = -1.0;
 				
 				// Hidden layers
 				for (int j = 0; j < this.numLayers; j++){
 					for (int k = 0; k < this.numNodes; k++){
 						if(j == 0)
 							this.hidden[k][j].activate(firstIn);
 						else
 							this.hidden[k][j].activate(inputs);
 						outputs[k] = this.hidden[k][j].getOutput();
 					}						
 					for (int k = 0; k < this.numNodes; k++){
 						inputs[k] = outputs[k];
 					}
 				}
 				
 				// Output layer (and calculate output delta)
 				for (int j = 0; j < this.numOutputs; j++){
 					this.output[j].activate(inputs);
 					finalOut[j] = this.output[j].getOutput();
 					this.output[j].setDelta(finalOut[j] - expected);
 				}
 				
 				// Begin back-propagation of delta					
 				for (int j = 0; j < this.numNodes; j++){
 					sum = 0.0;
 					for (int k = 0; k < this.numOutputs; k++){
 						sum += this.output[k].getDelta() * this.output[k].getWeight()[j];
 					}					
 					this.hidden[j][this.numLayers - 1].setDelta(this.hidden[j][this.numLayers - 1].getDerivative() * sum);
 				}				
 							
 				for (int j = this.numLayers - 2; j >= 0; j--){
 					for(int k = 0; k < this.numNodes; k++){
 						sum = 0.0;
 						if(j > 0){
 							for (int l = 0; l < this.numNodes; l++){
 								sum += this.hidden[l][j + 1].getDelta() * this.hidden[l][j + 1].getWeight()[k]; 
 							}
 						}
 						else{
 							for (int l = 0; l < this.numInputs; l++){
 								sum += this.hidden[l][j + 1].getDelta() * this.hidden[l][j + 1].getWeight()[k]; 
 							}
 						}
 						this.hidden[k][j].setDelta(sum);
 					}
 				}
 				
 				// Begin forward propagation of weights
 				for (int j = 0; j < this.numInputs; j++){	
 					firstIn[j] = trainSet[j][i];
 				}
 				
 				Neuron n;
 				double wPrime;				
 				for (int j = 0; j < this.numLayers; j++){
 					if(j == 0){
 						for (int k = 0; k < this.numInputs; k++){
 							n = this.hidden[k][j];						
 							for (int l = 0; l < this.numInputs; l++){
 								wPrime = n.getWeight()[l] - this.rate * n.getDelta() * n.getDerivative() * firstIn[l];
 								n.setWeight(l, wPrime);
 							}
 						}
 						for (int k = 0; k < this.numInputs; k++){
 							inputs[k] = this.hidden[k][j].getOutput();
 						}
 					}
 					else{
 						for (int k = 0; k < this.numNodes; k++){
 							n = this.hidden[k][j];						
 							for (int l = 0; l < this.numNodes; l++){
 								wPrime = n.getWeight()[l] - this.rate * n.getDelta() * n.getDerivative() * inputs[l];
 								n.setWeight(l, wPrime);
 							}
 						}
 						for (int k = 0; k < this.numNodes; k++){
 							inputs[k] = this.hidden[k][j].getOutput();
 						}
 					}
 				}
 				
 				for (int j = 0; j < this.numOutputs; j++){
 					n = this.output[j];
 					for (int k = 0; k < this.numNodes; k++){
 						wPrime = n.getWeight()[k] - this.rate * n.getDelta() * n.getDerivative() * inputs[k];
 						n.setWeight(k, wPrime);
 					}
 				}
 				
 				a++;
 				if(a == epochs)
 					break;
 			}
 		}
 	}
 	
 	public char process(double[][] set, int index, char classes){
 		double[] firstIn = new double[this.numInputs];
 		double[] inputs = new double[this.numNodes];
 		double[] outputs = new double[this.numNodes];
 		double[] finalOut = new double[this.numOutputs];
 		double expected;
 		double out = 0.0;
 		
 		if(this.classifier == classes)
 			expected = 1.0;
 		else
 			expected = -1.0;
 		
 		for (int j = 0; j < this.numInputs; j++){
 			firstIn[j] = set[j][index];
 		}
 		
 		// Hidden layers
 		for (int j = 0; j < this.numLayers; j++){
 			for (int k = 0; k < this.numNodes; k++){
 				if(j == 0)
 					this.hidden[k][j].activate(firstIn);
 				else
 					this.hidden[k][j].activate(inputs);
 				outputs[k] = this.hidden[k][j].getOutput();
 			}						
 			for (int k = 0; k < this.numNodes; k++){
 				inputs[k] = outputs[k];
 			}
 		}
 		
 		// Output layer (and calculate output delta)
 		for (int j = 0; j < this.numOutputs; j++){
 			this.output[j].activate(inputs);
 			finalOut[j] = this.output[j].getOutput();
 			if(finalOut[j] > 0.0)
 				out = 1.0;
 			else
 				out = -1.0;					
 		}
 	
 		if(out == 1.0)
 			return (this.classifier);
 		else
 			return '!';
 	}
 	
 }
