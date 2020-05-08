 package de.unikassel.ann.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.junit.Test;
 
 import de.unikassel.ann.config.NetConfig;
 import de.unikassel.ann.factory.NetworkFactory;
 import de.unikassel.ann.model.func.SigmoidFunction;
 
 public class SynapseTest {
 
 	@Test
 	public void randomTest() {
 		// no sense
 		Neuron n = new Neuron("SigmoidFunction", false);
 		List<Double> list = new ArrayList<Double>();
 		for (int i = 0; i < 20; i++) {
 			Synapse s = new Synapse(n, n);
 			list.add(s.getWeight());
 		}
 
 		Collections.sort(list);
 		for (Double d : list) {
 			System.out.println(d);
 		}
 	}
 
 	/**
 	 * Synapse connections (matrix) looks like this<br>
	 * for connection 00 -> 12 / 0 -> 4  there is no space remaining
 	 * 
 	 * <pre>
 	 * 00 01 02
 	 *   \| X |
 	 * 10 11 12
 	 *   \| /
 	 *    20
 	 * </pre>
 	 * or
 	 * <pre>
 	 * 0 1 2
 	 *  \|X|
 	 * 3 4 5
 	 *  \|/
 	 *   6
 	 * </pre>
 	 */
 	@Test
 	public void testBigSynapseMatrix() {
 
 		NetConfig netConfig = NetworkFactory.createSimpleNet(2,	new int[] { 2 }, 1, true, new SigmoidFunction());
 		Network net = netConfig.getNetwork();
 
 
 		System.out.println("synapse matrix before (random)");
 		Double[][][][] randomMatrix = net.getSynapseMatrix().getBigWeightMatrix();
 		System.out.println(buildBigMatrix(randomMatrix));
 
 		// new matrix
 		Double[][][][] x = new Double[3][3][3][3];
		x[0][0][1][1] = 1.0;
		x[0][0][1][2] = 2.0;
		x[0][1][1][1] = 3.0;
		x[0][1][1][2] = 4.0;
		x[0][2][1][1] = 5.0;
		x[0][2][1][2] = 6.0;
		x[1][0][2][0] = 7.0;
		x[1][1][2][0] = 8.0;
		x[1][2][2][0] = 9.0;
 
 		net.getSynapseMatrix().setBigWeightMatrix(x);
 
 		System.out.println("synapse matrix sorted asc");
 		Double[][][][] sortedMatrix = net.getSynapseMatrix().getBigWeightMatrix();
 		System.out.println(buildBigMatrix(sortedMatrix));
 		
 		Double[][] m = new Double[7][7];
 		m[0][4] = 9.0;
 		m[0][5] = 8.0;
 		m[1][4] = 7.0;
 		m[1][5] = 6.0;
 		m[2][4] = 5.0;
 		m[2][5] = 4.0;
 		m[3][6] = 3.0;
 		m[4][6] = 2.0;
 		m[5][6] = 1.0;
 		
 		net.getSynapseMatrix().setWeightMatrix(m);
 
 		System.out.println("synapse matrix sorted desc");
 
 		System.out.println(buildBigMatrix(net.getSynapseMatrix().getBigWeightMatrix()));
 		System.out.println(buildSmallMatrix(net.getSynapseMatrix().getSynapses()));
 		
 		net.printSynapses();
 		
 	}
 
 	private String buildBigMatrix(Double[][][][] m) {
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < m.length; i++) {
 			for (int j = 0; j < m[i].length; j++) {
 				for (int k = 0; k < m[i][j].length; k++) {
 					for (int l = 0; l < m[i][j][k].length; l++) {
 						if (m[i][j][k][l] != null) {
 							sb.append("[");
 							sb.append(i);
 							sb.append("]");
 							sb.append("[");
 							sb.append(j);
 							sb.append("] -> ");
 							sb.append("[");
 							sb.append(k);
 							sb.append("]");
 							sb.append("[");
 							sb.append(l);
 							sb.append("]");
 							sb.append(" = ");
 							sb.append(m[i][j][k][l]);
 							sb.append("\n");
 						}
 					}
 				}
 			}
 		}
 		return sb.toString();
 	}
 
 	private String buildSmallMatrix(Synapse[][] m) {
 		StringBuilder sb = new StringBuilder();
 		for (int from = 0; from < m.length; from++) {
 			for (int to = 0; to < m[from].length; to++) {
 				if (m[from][to] != null) {
 					Double weight = m[from][to].getWeight();
 					sb.append("{");
 					sb.append(from);
 					sb.append("} -> ");
 					sb.append("{");
 					sb.append(to);
 					sb.append("}");
 					sb.append(" = ");
 					sb.append(weight);
 					sb.append("\n");
 				}
 			}
 		}
 		return sb.toString();
 	}
 	
 }
