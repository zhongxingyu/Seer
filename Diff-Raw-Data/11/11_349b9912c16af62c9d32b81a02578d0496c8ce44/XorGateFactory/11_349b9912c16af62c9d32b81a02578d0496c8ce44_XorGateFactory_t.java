 package model.gates.basic;
 
 import model.gates.AbstractGate;
 
 public class XorGateFactory implements GateFactory {
    public final static XorGateFactory INSTANCE = new XorGateFactory();
    
     @Override
     public AbstractGate newGate() {
	return new Xor(2);
     }
 
     @Override
     public AbstractGate newGate(int numInputs) {
	return new Xor(numInputs);
     }
 
     private class Xor extends AbstractGate {
 
 	public Xor(int numInputs) {
 	    super(numInputs, 1);
 	}
 
 	/**
 	 * Recalculate the output array from the inputs. We use the generally
 	 * implemented definition of n-input XOR: a modulo 2 adder.
 	 */
 	@Override
 	protected void computeOutput() {
 	    // We count the number of on-signals, and return its parity
 	    int numOns = 0;
 	    for (int i = 0; i < inputs.length; i++)
 		numOns += inputs[i] ? 1 : 0;
 	    
 	    outputs[0] = (numOns % 2) == 1;
 	}
 
 	@Override
 	public AbstractGate clone() {
 	    Xor newXor = new Xor(inputs.length);
 	    newXor.setInputs(inputs.clone());
 	    return newXor;
 	}
     }
 }
