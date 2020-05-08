 package com.dvcs.neuralnetwork.driver;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 
 import org.encog.ml.data.MLData;
 import org.encog.ml.data.basic.BasicMLData;
 import org.encog.neural.networks.BasicNetwork;
 import org.jblas.DoubleMatrix;
 
 import com.dvcs.neuralnetwork.EncogNetworkBuilder;
 import com.dvcs.neuralnetwork.Example;
 import com.dvcs.neuralnetwork.NeuralNetwork;
 import com.dvcs.neuralnetwork.NeuralNetworkBuilder.DimensionMismatchException;
 import com.dvcs.neuralnetwork.NeuralNetworkBuilder.InsufficientDataException;
 import com.dvcs.neuralnetwork.driver.DataQueueListener.NewDataCallback;
 
 public class Driver {
 	static final String QUEUE_NAME = "robotData";
 	static final double LEARNING_RATE = 0.75;
 	static final double MOMENTUM = 0.6;
 	static final int HIDDEN_LAYER_UNITS = 100;
 	static final int OUTPUT_LAYER_UNITS = 10;
 
 	private static final Logger LOGGER = Logger
 			.getLogger("NeuralNetworkDriver");
 
 	private DriverGUI gui;
 	private BasicNetwork network;
 	private DataCollector collector;
 	private DataCollector predictor;
 	private EncogNetworkBuilder builder;
 
 	private NewDataCallback dataCollectorCallback = new NewDataCallback() {
 		public void receivedData(byte[] data) {
 			DoubleMatrix m = parseImageData(data);
 
 			double[] x = m.toArray();
 
 			// TODO: y
 			double[] y = new double[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
 
 			Example ex = new Example(x, y);
 			try {
 				builder.addExample(ex);
 			} catch ( DimensionMismatchException e ) {
 				e.printStackTrace();
 			}
 
 			// Display image
 			gui.loadImageMatrix(m);
 		}
 	};
 
 	private NewDataCallback dataPredictorCallback = new NewDataCallback() {
 		public void receivedData(byte[] data) {
 			if ( network == null ) {
 				LOGGER.severe("Driver asked to make predictions before network"
 						+ "was built");
 			}
 
 			DoubleMatrix m = parseImageData(data);
 
 			long start = System.nanoTime();
 
 			double[] output = network.compute(new BasicMLData(m.toArray()))
 					.getData();
 			int predictedClass = NeuralNetwork.maxIndex(output);
 
 			long end = System.nanoTime();
 
 			gui.loadImageMatrix(m);
			gui.displayPropagationResult(output, predictedClass, start
					- end);
 		}
 	};
 
 	public Driver(DriverGUI _gui) {
 		gui = _gui;
 		builder = new EncogNetworkBuilder();
 		collector = new DataCollector(QUEUE_NAME, dataCollectorCallback);
 		predictor = new DataCollector(QUEUE_NAME, dataPredictorCallback);
 	}
 
 	public boolean isCollecting() {
 		return collector.isListening();
 	}
 
 	public void startCollecting() {
 		LOGGER.info("Beginning data collection");
 
 		collector.startQueueListener();
 	}
 
 	public void stopCollecting() {
 		LOGGER.info("Ending data collection");
 
 		collector.stopQueueListener();
 	}
 
 	/**
 	 * @return Whether the builder has enough data to create a neural network
 	 */
 	public boolean hasSufficientData() {
 		return builder.hasSufficientData();
 	}
 
 	/**
 	 * Train the neural network using the given sample data.
 	 */
 	public void trainNeuralNetwork() {
 		try {
 			network = builder.buildEncogNetwork(
 					new int[] { HIDDEN_LAYER_UNITS }, LEARNING_RATE, MOMENTUM);
 		} catch ( InsufficientDataException e ) {
 			e.printStackTrace();
 		}
 	}
 
 	public void startFeedForward() {
 		LOGGER.info("Beginning feedforward processing");
 
 		predictor.startQueueListener();
 	}
 
 	public void stopFeedForward() {
 		LOGGER.info("Ending feedforward processing");
 
 		predictor.stopQueueListener();
 	}
 
 	private DoubleMatrix parseImageData(byte[] data) {
 		BufferedImage im = null;
 		try {
 			im = ImageIO.read(new ByteArrayInputStream(data));
 		} catch ( IOException e ) {
 			e.printStackTrace();
 			return null;
 		}
 
 		if ( im == null ) {
 			LOGGER.severe("Failed to parse data received from queue. "
 					+ "Only valid raw image data should be published on "
 					+ "this queue.");
 			return null;
 		}
 
 		DoubleMatrix m = ImageConverter.convertImageToMatrix(im, true);
 		m = ImageConverter.normalize(m);
 
 		return m;
 	}
 
 	public class OutputProvider {
 
 	}
 }
