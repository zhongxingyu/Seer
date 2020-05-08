 package de.thatsich.bachelor;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.List;
 
import org.apache.hama.ml.perception.MultiLayerPerceptron;
import org.apache.hama.ml.perception.SmallMultiLayerPerceptron;

 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.scene.image.Image;
 import javafx.stage.Stage;
 
 import com.cathive.fx.guice.GuiceApplication;
 import com.google.inject.Inject;
 import com.google.inject.Module;
 
 import de.thatsich.bachelor.gridoverview.api.guice.GridCommandModule;
 import de.thatsich.bachelor.gridoverview.api.guice.GridWiringModule;
 import de.thatsich.bachelor.gridoverview.restricted.view.DisplayView;
 import de.thatsich.core.guice.LoggerModule;
 import de.thatsich.core.guice.PostInitModule;
 import de.thatsich.core.opencv.OpenCVLoader;
 
 
 // TODO need Threshold
 // TODO bind erfolgsrate precision recall
 // ODO low priority - Random Feature Extractor
 // TODO CNBC
 // ODO halted - AANN
 // ODO halted - evtl in Commander Klassen aufteilen sodass Handler alle am
 // selben Platz sind und dass die Controller weniger Responsibility haben
 /**
  * Main Execution Class
  * 
  * @author Tran Minh Do
  */
 public class Main extends GuiceApplication
 {
 
 	/**
 	 * First instantiated view.
 	 */
 	@Inject
 	private DisplayView	view;
 
 	/**
 	 * 
 	 * @param args
 	 */
 	public static void main( String[] args )
 	{
		String modelPath = "/tmp/sampleModel-testWriteReadMLP.data";
		double learningRate = 0.3;
		double regularization = 0.0; // no regularization
		double momentum = 0; // no momentum
		String squashingFunctionName = "Sigmoid";
		String costFunctionName = "SquaredError";

		int[] layerSizeArray = new int[] { 3, 2, 2, 3 };

		MultiLayerPerceptron mlp = new SmallMultiLayerPerceptron(learningRate,
	        regularization, momentum, squashingFunctionName, costFunctionName,
	        layerSizeArray);
		
//		mlp.train( null, null )
		
		System.out.println("Finished");
		
 		// Loading OpenCV Bindings
//		OpenCVLoader.loadLibrary();
 
//		Main.launch( args );
 	}
 
 	/*
 	 * ==================================================
 	 * GuiceApplication Implementation
 	 * ==================================================
 	 */
 	/**
 	 * Initialize the Application with the first scene on stage.
 	 * Called automatically by Super Class.
 	 * 
 	 * @param primaryStage
 	 *            Main Stage to be drawn on
 	 */
 	@Override
 	public void start( Stage primaryStage ) throws IOException
 	{
 		if ( this.view == null ) throw new NullPointerException( "View not instantiated." );
 
 		final Parent root = this.view.getRoot();
 		if ( root == null ) throw new NullPointerException( "Root damaged." );
 
 		final URL url = this.getClass().getResource( "opencv.png" );
 		if ( url == null ) throw new NullPointerException( "Icon not found." );
 		final Image icon = new Image( url.toString() );
 		if ( icon.isError() ) throw new IllegalStateException( "Icon could not be loaded" );
 
 		// Put the loaded user interface onto the primary stage.
 		final Scene scene = new Scene( root );
 
 		// Show the primary stage
 		primaryStage.setTitle( "Sample" );
 		primaryStage.getIcons().add( icon );
 		primaryStage.setScene( scene );
 		primaryStage.setResizable( false );
 		primaryStage.show();
 	}
 
 	/**
 	 * Initialize all added modules
 	 * Automatically called by super class
 	 * 
 	 * @param modules
 	 *            Empty Module-List
 	 */
 	@Override
 	public void init( List<Module> modules )
 	{
 		modules.add( new LoggerModule() );
 		modules.add( new PostInitModule() );
 
 		modules.add( new GridWiringModule() );
 		modules.add( new GridCommandModule() );
 	}
 }
