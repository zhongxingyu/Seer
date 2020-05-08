 package NerualNet;
 
 import java.util.Random;
 
 public class Neuron {
 	protected int numInputs;
 	protected int activation;
 	protected double delta;
 	protected double output;
 	protected double derivative;
 	protected double[] weight;
 	protected Random rnd;
 	
 	Neuron(int numInputs, int activation){
 		this.rnd = new Random();
 		this.activation = activation;
 		this.delta = 0.0;
 		this.output = 0.0;
 		this.derivative = 0.0;
 		this.numInputs = numInputs;
 		this.weight = new double[this.numInputs];
 		
 		for (int i = 0; i < this.numInputs; i++){
			this.weight[i] = (this.rnd.nextDouble() * 0.5) - 0.25;
 		}
	}
	
	Neuron(int numInputs){
		this.rnd = new Random();
		this.delta = 0.0;
		this.output = 0.0;
		this.derivative = 0.0;
		this.numInputs = numInputs;
		this.weight = new double[this.numInputs];
		
		for (int i = 0; i < this.numInputs; i++){
			this.weight[i] = (this.rnd.nextDouble() * 3.0) - 1.5;
		}
	}
	
 	
 	protected void activate(double[] inputs){
 		double sum = 0.0;
 		for(int i = 0; i < this.numInputs; i++){
 			sum += this.weight[i]*inputs[i]; 
 		}
 		if(activation == 2){
 			this.output = this.sigmoidFunc(sum);
 			this.derivative = this.sigDerivative(sum);
 		}
 		else{
 			this.output = sum;
 			this.derivative = 1.0;
 		}
 	}
 	
 	protected double getOutput(){
 		return this.output;
 	}
 	
 	protected double getDerivative(){
 		return this.derivative;
 	}
 	
 	protected double[] getWeight(){
 		return this.weight;
 	}
 	
 	protected void setWeight(int index, double wPrime){
 		this.weight[index] = wPrime;
 	}
 	
 	protected double getDelta(){
 		return this.delta;
 	}
 	
 	protected void setDelta(double delta){
 		this.delta = delta;
 	}
 	
 	private double sigmoidFunc(double x){
 		return Math.tanh(x);
 	}
 	
 	private double sigDerivative(double x){
 		return (1.0 - Math.pow(Math.tanh(x), 2.0));
 	}
 }
