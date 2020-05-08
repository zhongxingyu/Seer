 package twizansk.hivemind.drone.data;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.typesafe.config.Config;
 
 import twizansk.hivemind.api.data.TrainingSample;
 import twizansk.hivemind.api.data.TrainingSet;
 
 /**
  * A simple training set that reads a CSV file and stores the set in memory. If
  * the end of the set is reached, the points are shuffled in pseudo-random manor
  * and the iteration cycles back to the beginning. This training set
  * implementation is not meant for production use.
  * 
  * @author Tommer Wizansky
  * 
  */
 public class InMemoryCyclicCSVTrainingSet implements TrainingSet {
 
 	private InMemoryCyclicCSVTrainingSetConfig config;
 	private BufferedReader reader;
 	private List<double[]> X;
 	private List<Double> y;
 	private List<Integer> indexes;
 	private Integer t;
 
 	@Override
 	public void init(Config config) {
 		this.config = new InMemoryCyclicCSVTrainingSetConfig(config);
 		this.reset();
 	}
 
 	@Override
 	public TrainingSample getNext() {
 		// If we've reached the end of the set, initialize the time stamp,
 		// shuffle the indexes randomly and start over.
 		if (t == indexes.size()) {
 			this.t = 0;
 			Collections.shuffle(indexes);
 		}
		int i = indexes.get(t);
 		return new TrainingSample(X.get(i), y.get(i));
 	}
 
 	@Override
 	public void reset() {
 		try {
 			// Initialize the reader.
 			if (reader != null) {
 				reader.close();
 			}
 			URI uri = ClassLoader.getSystemResource(config.path).toURI();
 			Charset charset = Charset.forName("US-ASCII");
 			Path file = Paths.get(uri.getPath());
 			this.reader = Files.newBufferedReader(file, charset);
 
 			// Read the entire training set into memory.
 			String line;
 			this.X = new ArrayList<double[]>();
 			this.y = new ArrayList<Double>();
 			this.indexes = new ArrayList<>();
 			int n = 0;
 			while ((line = reader.readLine()) != null) {
 				String[] arr = line.split(",\\s*");
 				double[] rowData = new double[arr.length - 1];
 				for (int i = 0; i < arr.length - 1; i++) {
 					rowData[i] = Double.parseDouble(arr[i]);
 				}
 				this.X.add(rowData);
 				this.y.add(Double.parseDouble(arr[arr.length - 1]));
 				this.indexes.add(n++);
 			}
 
 			// Reset the counter
 			this.t = 0;
 		} catch (URISyntaxException | IOException e) {
 			throw new RuntimeException(e);
 		}
 
 	}
 
 }
