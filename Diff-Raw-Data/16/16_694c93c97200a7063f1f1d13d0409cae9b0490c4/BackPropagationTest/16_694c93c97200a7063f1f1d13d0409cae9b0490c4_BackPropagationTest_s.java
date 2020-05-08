 package de.unikassel.ann.model.algo;
 
 import org.junit.Test;
 
 import de.unikassel.ann.algo.BackPropagation;
 import de.unikassel.ann.config.NetConfig;
 import de.unikassel.ann.factory.NetworkFactory;
 import de.unikassel.ann.model.DataPair;
 import de.unikassel.ann.model.DataPairSet;
 import de.unikassel.ann.model.Network;
 import de.unikassel.ann.model.func.SigmoidFunction;
 
 public class BackPropagationTest {
 	
 	@Test
 	public void testForwardPass() {
 		
		NetConfig netConfig = NetworkFactory.createXorNet(4*1000);
 		Network net = netConfig.getNetwork();
 		
 		DataPairSet trainSet = new DataPairSet();
 		trainSet.addPair(new DataPair(new Double[] {0.0, 0.0}, new Double[] {0.0}));
 		trainSet.addPair(new DataPair(new Double[] {0.0, 1.0}, new Double[] {1.0}));
 		trainSet.addPair(new DataPair(new Double[] {1.0, 0.0}, new Double[] {1.0}));
 		trainSet.addPair(new DataPair(new Double[] {1.0, 1.0}, new Double[] {0.0}));
 		
 		netConfig.getTrainingModule().train(trainSet);
 		
 		DataPairSet testSet = new DataPairSet();
 		testSet.addPair(new DataPair(new Double[] {0.0, 0.0}, new Double[] {Double.NaN}));
 		testSet.addPair(new DataPair(new Double[] {0.0, 1.0}, new Double[] {Double.NaN}));
 		testSet.addPair(new DataPair(new Double[] {1.0, 0.0}, new Double[] {Double.NaN}));
 		testSet.addPair(new DataPair(new Double[] {1.0, 1.0}, new Double[] {Double.NaN}));
 		
 		netConfig.getWorkingModule().work(net, testSet);
 		
 		netConfig.printStats();
 		System.out.println(testSet);
 
 	}
 	
 	@Test
 	public void testForwardStepWithSynapseWeights() {
 		
 		NetConfig netConfig = NetworkFactory.createSimpleNet(2, new int[] {2}, 1, true, new SigmoidFunction());
 		Network net = netConfig.getNetwork();
 		
 		Double[][][] x = new Double[3][3][3];
 		
 		x[0][0][1] = 6.145;
 		x[0][0][2] = 1.858;
 		x[0][1][1] = -4.171;
 		x[0][1][2] = -4.930;
 		x[0][2][1] = -4.187;
 		x[0][2][2] = -4.925;
 		x[1][0][0] = -3.078;
 		x[1][1][0] = 6.443;
 		x[1][2][0] = -7.144;
 		
 		net.setSynapseMatrix(x);
 		
 		DataPairSet testSet = new DataPairSet();
 		testSet.addPair(new DataPair(new Double[] {0.0, 0.0}, new Double[] {Double.NaN}));
 		testSet.addPair(new DataPair(new Double[] {0.0, 1.0}, new Double[] {Double.NaN}));
 		testSet.addPair(new DataPair(new Double[] {1.0, 0.0}, new Double[] {Double.NaN}));
 		testSet.addPair(new DataPair(new Double[] {1.0, 1.0}, new Double[] {Double.NaN}));
 		
 		netConfig.getWorkingModule().work(net, testSet);
 		
 		netConfig.printStats();
 		System.out.println(testSet);
 	}
 
 }
