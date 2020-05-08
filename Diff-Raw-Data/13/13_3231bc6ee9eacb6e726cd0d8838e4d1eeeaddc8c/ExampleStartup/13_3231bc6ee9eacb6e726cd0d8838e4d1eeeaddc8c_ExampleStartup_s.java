 package com.ceejii;
 
 import java.awt.FlowLayout;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 
 import javax.swing.JFrame;
 
 import com.ceejii.gui.ExampleSearchSuggestionListener;
 import com.ceejii.gui.component.SearchSuggestionComponent;
 import com.ceejii.search.ExampleSearchProvider;
 
 public class ExampleStartup {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		//Create a main windows and fill it with a SearchSuggestionComponent.
 		final JFrame window = new JFrame("Skfrslag");
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		window.setLayout(new FlowLayout());
 		SearchSuggestionComponent searchSuggestionComponent = new SearchSuggestionComponent(new ExampleSearchProvider(), window);
 		searchSuggestionComponent.setSupportsHoveringDescription("Do something on hover");
 		window.getContentPane().add(searchSuggestionComponent);
 		window.pack();
 		window.setVisible(true);
 		searchSuggestionComponent.addComponentListener(new ComponentListener(){
 			public void componentHidden(ComponentEvent arg0) {
 			}
 			public void componentMoved(ComponentEvent arg0) {
 			}
 			public void componentResized(ComponentEvent arg0) {
 				window.pack();
 			}
 			public void componentShown(ComponentEvent arg0) {
 			}
 		});
 //		searchSuggestionComponent.addSearchSuggestionListener(new ExampleSearchSuggestionListener());
 	}
 
 }
