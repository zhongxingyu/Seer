 package de.thatsich.core.javafx;
 
 import java.io.IOException;
 import java.net.URL;
 
 import javafx.scene.Parent;
 
 import com.cathive.fx.guice.GuiceFXMLLoader;
import com.cathive.fx.guice.GuiceFXMLLoader.Result;
 import com.google.inject.Inject;
 
 
 /**
  * all view have a parent object they will load
  * fetches the name from its own child-name
  * @author Tran Minh Do
  */
 public abstract class AFXMLView implements IFXMLView {
 
 	/**
 	 * Parent Object of the FXML
 	 */
 	private Parent pane;
 
 	/**
 	 * String of the FXML File
 	 */
 	private String fxml;
 	/**
 	 * FXML Loader
 	 */
 	@Inject
 	private GuiceFXMLLoader loader;
 	
 	/**
 	 * CTOR
 	 * @param fxml name of the corresponding FXML
 	 */
 	public AFXMLView() {
 		this.fxml = this.getFXMLName();
 	}
 	
 	
 	/*
 	 * ==================================================
 	 * Private Helper Implementation
 	 * ==================================================
 	 */
 	
 	/**
 	 * applyCSSIfPossible
 	 * checks if CSS File is avaiable and applies it
 	 * @param parent Node which CSS is to be applied
 	 */
 	private void applyCSSIfPossible(Parent parent) {
 		final URL url = this.getClass().getResource(this.getCSSName());
 		if (url == null) return;
 		
 		final String uri = url.toExternalForm();
 		parent.getStylesheets().add(uri);
 	}
 	
 	
 	/**
 	 * getFXMLName
 	 * FXMLName derived from SimpleClassName
 	 * @return String FXML File Name
 	 */
 	private String getFXMLName() {
 		return this.getSimpleClassName() + ".fxml";
 	}
 	
 	/**
 	 * getCSSName
 	 * gets the CSS File Name derived from SimpleClassName
 	 * @return String CSS File Name
 	 */
 	private String getCSSName() {
 		return this.getSimpleClassName() + ".css";
 	}
 	
 	/**
 	 * Returns the simple ClassName
 	 * e.g
 	 * - Class: /path/to/ThisIsATest.class
 	 * - Return: ThisIsATest
 	 * @return String Simple Class Name
 	 */
 	private String getSimpleClassName() {
 		return this.getClass().getSimpleName();
 	}
 
 	
 	/*
 	 * ==================================================
 	 * Getter Implementation
 	 * ==================================================
 	 */
 	public Parent getRoot() {
 		
 		// lazy loading
 		if (this.pane == null)
 		{
 			// fetch FXML
 			URL urlFXML = this.getClass().getResource(this.fxml);
 			if (urlFXML == null) throw new IllegalStateException("Could not find " + urlFXML);
 			
 			// load FXML
			Result res = null;
 			try {
				res = this.loader.load( urlFXML );
 			} catch (IOException e) {
 				throw new IllegalStateException("Could not load " + urlFXML, e);
 			}
 			
			this.pane = res.getRoot();  
			
 			// apply CSS if found
 			this.applyCSSIfPossible(this.pane);
 		}
 		
 		return this.pane;
 	}
 }
