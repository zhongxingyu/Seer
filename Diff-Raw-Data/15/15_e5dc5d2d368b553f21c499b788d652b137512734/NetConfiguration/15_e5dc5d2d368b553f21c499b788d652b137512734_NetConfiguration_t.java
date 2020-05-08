 package neuronalnetwork;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import neuronalnetwork.function.TransferFunction;
 
 public class NetConfiguration {
 
 	private static final String examplesFile = "./TPE4/res/examples.txt";
 
 	public int[] structure;
 	public TransferFunction f;
 	public float eta;
 	public List<TrainItem> training;
 	public List<TrainItem> testing;
 
 	// Procentaje de los ejemplos para usar para training; los demas son para
 	// testing
 	public float p = 0.5f;
 
 	/**
 	 * Lee los ejemplos del archivo examplesFile y los carga en los aprametros
 	 * training y testing de acuerdo a p.
 	 */
 	public void initialize() throws IOException {
 		// FIXME: Hardcoded values!
 		int inputDim = 2;
 		int outputDim = 1;
 		List<TrainItem> allexamples = new LinkedList<TrainItem>();
 		try {
 			DataInputStream in = new DataInputStream(new FileInputStream(
 					examplesFile));
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String strLine;
 			while ((strLine = br.readLine()) != null) {
 				float[] input = new float[inputDim];
 				float[] output = new float[outputDim];
 				parseLine(strLine, input, output);
 				allexamples.add(new TrainItem(input, output));
 			}
 			divideExamples(allexamples);
 			in.close();
 		} catch (IOException e) {
 			System.out.println("File: " + examplesFile
 					+ " could not be loaded. " + e.getMessage());
 			throw e;
 		}
 	}
 
 	private void parseLine(String line, float[] in, float[] out) {
 		StringTokenizer tokenizer = new StringTokenizer(line.trim());
 		for (int i = 0; i < in.length; i++) {
 			in[i] = Float.parseFloat(tokenizer.nextToken());
 		}
 		for (int i = 0; i < out.length; i++) {
 			out[i] = Float.parseFloat(tokenizer.nextToken());
 		}
 	}
 
 	private void divideExamples(List<TrainItem> examples) {
 		int forTraining = (int) (examples.size() * p);
		training = new ArrayList<TrainItem>();
		testing = new ArrayList<TrainItem>();
		int offset = 0;
		for (TrainItem item: examples) {
			if (offset < forTraining) {
				training.add(item);
			} else {
				testing.add(item);
			}
			offset++;
		}
 	}
 }
