 package info.gehrels.diplomarbeit;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Random;
 
 import static java.lang.Math.pow;
 
 public class GraphGenerationBatch {
 
 	private static final Random RANDOM = new Random();
 
 	public static void main(String[] args) throws IOException {
 		for (int i = 3; i < 30; i++) {
 			for (int j = 0; j <= maxFor(i); j++) {
 				int numberOfNodes = (int) Math.pow(2, i);
 				long numberOfEdges = (long) Math.pow(10, j);
 				System.out.println("----------------------------");
 				System.out.println("numberOfNodes = " + numberOfNodes);
 				System.out.println("numberOfEdges = " + numberOfEdges);
 
				double ratio = 2d * (double) numberOfEdges / (double) numberOfNodes;
 				if (ratio > 403.57) {
 					System.out.println("SKIPPED");
 					continue;
 				}
 
				try {
					generateGraph(numberOfNodes, numberOfEdges);
				} catch (OutOfMemoryError e) {
					System.gc();
					System.out.println(e);
				}
 			}
 		}
 	}
 
 	private static int maxFor(int i) {
 		long numberOfNodes = (long) Math.pow(2, i);
 		long maxPossibleNumOfEdges = numberOfNodes * (numberOfNodes + 1) / 2;
 		return (int) Math.floor(Math.log10(maxPossibleNumOfEdges));
 	}
 
 	private static void generateGraph(int numberOfNodes, long numberOfEdges) throws IOException {
 		String fileNameBase = numberOfNodes + "_" + numberOfEdges;
 		String geoffFileName = fileNameBase + ".geoff";
 		deleteGeofFile(geoffFileName);
 		generateAGeoffFile(numberOfNodes, numberOfEdges, geoffFileName);
 	}
 
 	private static void deleteGeofFile(String geoffFileName) {
 		new File(geoffFileName).delete();
 	}
 
 	private static void generateAGeoffFile(int numberOfNodes, long numberOfEdges, String fileName) throws IOException {
 		FileWriter writer = new FileWriter(fileName);
 
 		EdgeWriterFactory edgeWriterFactory = new EdgeWriterFactory(writer);
		Generator generator = new RMATGenerator(edgeWriterFactory, new NodeWriter(writer), numberOfNodes,
		                                        numberOfEdges);
 		generator.generate();
 		writer.close();
 	}
 
 	static class NodeWriter {
 
 		private final FileWriter writer;
 
 		public NodeWriter(FileWriter writer) {
 			this.writer = writer;
 		}
 
 
 		public void createNode(String nodeId) {
 			try {
 				writer.write("(" + nodeId + ") {}\n");
 			} catch (IOException e) {
 				throw new IllegalStateException(e);
 			}
 		}
 	}
 
 	static class EdgeWriterFactory {
 
 		private final FileWriter writer;
 
 		public EdgeWriterFactory(FileWriter writer) {
 			this.writer = writer;
 		}
 
 		public static byte getLabel() {
 			int rndValue = RANDOM.nextInt(1000000000);
 			double sum = 0;
 			for (byte i = 1; i <= 4; i++) {
 				sum += 702439024 * pow(i, -2);
 				if (rndValue < sum) {
 					return i;
 				}
 			}
 			throw new IllegalStateException();
 		}
 
 		public Boolean createEdge(String sourceVertex, String targetVertex) {
 			try {
 				writer.write("(" + sourceVertex + ")-[:L" + getLabel() + "]->(" + targetVertex + ")\n");
 			} catch (IOException e) {
 				throw new IllegalStateException(e);
 			}
 			return true;
 		}
 	}
 }
