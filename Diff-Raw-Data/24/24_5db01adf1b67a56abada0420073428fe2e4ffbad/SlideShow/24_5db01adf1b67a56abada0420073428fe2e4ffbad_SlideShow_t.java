 package kc.utils;
 
 import java.io.File;
 
 import javafx.animation.FadeTransition;
 import javafx.animation.PauseTransition;
 import javafx.animation.SequentialTransition;
 import javafx.application.Application;
 import javafx.event.EventHandler;
 import javafx.scene.Scene;
 import javafx.scene.image.Image;
 import javafx.scene.image.ImageView;
 import javafx.scene.input.KeyCode;
 import javafx.scene.input.KeyEvent;
 import javafx.scene.layout.StackPane;
 import javafx.stage.Stage;
 import javafx.util.Duration;
 
 public class SlideShow {
 
	
 	ImageView[] slides;
 
 
 	// The method I am running in my class
 	public void start(File outputFolder, Stage stage) {
 
 		final Stage sliderStage = new Stage();
		StackPane root = new StackPane();
		Scene scene = new Scene(root);
 		sliderStage.setScene(scene);
 		sliderStage.setFullScreen(true);
 		sliderStage.initOwner(stage);
 		sliderStage.show();
 		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
 	        public void handle(KeyEvent ke) {
 	            if (ke.getCode() == KeyCode.ESCAPE) {
	                
 	                sliderStage.close();
 	            }
 	        }
 	    });
 		
 		
 		
 		
 
 		slides = new ImageView[outputFolder.listFiles().length-1];
 		File[] listOfFiles = PhotoliciousUtils
 				.filterJPEGImagesFromFolder(outputFolder.listFiles());
 		slides = new ImageView[listOfFiles.length];
 		int length = 0;
 		for (File file : listOfFiles) {
 			Image image = new Image("file:" + file.getPath());
			ImageView imageView = new ImageView(image);
			imageView.setFitWidth(scene.getWidth());
			imageView.setPreserveRatio(true);
			imageView.setSmooth(true);
			imageView.setCache(true);
			slides[length]= imageView;
 			length++;
 		}
 
 		SequentialTransition slideshow = new SequentialTransition();
 
 		for (ImageView slide : slides) {
 
 			SequentialTransition sequentialTransition = new SequentialTransition();
 
 			FadeTransition fadeIn = getFadeTransition(slide, 0.0, 1.0, 2000);
 			PauseTransition stayOn = new PauseTransition(Duration.millis(2000));
 			FadeTransition fadeOut = getFadeTransition(slide, 1.0, 0.0, 2000);
 
 			sequentialTransition.getChildren().addAll(fadeIn, stayOn, fadeOut);
 			slide.setOpacity(0);
			root.getChildren().add(slide);
 			slideshow.getChildren().add(sequentialTransition);
 
 		}
 		slideshow.play();
 	}
 
 	// the method in the Transition helper class:
 
 	public FadeTransition getFadeTransition(ImageView imageView,
 			double fromValue, double toValue, int durationInMilliseconds) {
 
 		FadeTransition ft = new FadeTransition(
 				Duration.millis(durationInMilliseconds), imageView);
 		ft.setFromValue(fromValue);
 		ft.setToValue(toValue);
 
 		return ft;
 
 	}
 
 }
