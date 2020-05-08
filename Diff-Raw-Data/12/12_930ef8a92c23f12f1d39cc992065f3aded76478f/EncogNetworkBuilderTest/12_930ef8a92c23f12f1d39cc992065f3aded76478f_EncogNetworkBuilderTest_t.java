 package com.dvcs.neuralnetwork;
 
 import org.encog.ml.data.basic.BasicMLData;
 import org.encog.neural.networks.BasicNetwork;
 import org.jblas.DoubleMatrix;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 import com.dvcs.neuralnetwork.NeuralNetworkBuilder.DimensionMismatchException;
 import com.dvcs.neuralnetwork.NeuralNetworkBuilder.InsufficientDataException;
 
 @RunWith(JUnit4.class)
 public class EncogNetworkBuilderTest {
 
 	private EncogNetworkBuilder builder;
 
 	@Before
 	public void setUp() {
 		builder = new EncogNetworkBuilder();
 	}
 
 	private void addExample(NeuralNetworkBuilder builder, double[] x, double[] y)
 			throws DimensionMismatchException {
 		builder.addExample(new Example(x, y));
 	}
 
 	@Test
 	public void testSimpleBuild() throws DimensionMismatchException,
 			InsufficientDataException {
 		double[] input = new double[] { 3 };
 		double[] output = new double[] { 1 };
 
 		addExample(builder, input, output);
 		BasicNetwork net = builder.buildEncogNetwork(new int[] { 1 }, 0.75, 0.6);
 
 		double[] out = net.compute(new BasicMLData(input)).getData();
 		Assert.assertArrayEquals(output, out, 0.1);
 	}
 
 	@Test(expected = InsufficientDataException.class)
 	public void testInsufficientData() throws InsufficientDataException {
 		builder.buildNetwork(new int[] { 1 }, 0);
 	}
 
 	@Test(expected = DimensionMismatchException.class)
 	public void testDimensionMismatchException()
 			throws DimensionMismatchException {
 		addExample(builder, new double[] { 1, 2 }, new double[] { 1 });
 		addExample(builder, new double[] { 3 }, new double[] { 1 });
 	}
 
 }
