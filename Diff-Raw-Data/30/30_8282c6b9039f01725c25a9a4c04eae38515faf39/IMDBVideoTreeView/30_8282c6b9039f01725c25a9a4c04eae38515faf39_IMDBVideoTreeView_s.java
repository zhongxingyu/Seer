 package com.ricex.rpi.server.imbdparser;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SpringLayout;
 
 import com.ricex.rpi.server.player.view.video.VideoTreeView;
 
 /**  IMDB Video Tree View
  * 
  *  Adds movie information from IMDB to the plain Video Tree View
  * 
  * @author Mitchell
  *
  */
 
 public class IMDBVideoTreeView extends VideoTreeView {
 
 	/** The horizontal padding for the spring layout */
 	private static final int HORIZONTAL_PADDING = 5;
 	
 	/** The vertical padding for the spring layout */
 	private static final int VERTICAL_PADDING = 5;
 	
 	/** The side panel that will be used to display the movie information */
 	protected JPanel sidePanel;
 	
 	/** The label to display the name of the movie */
 	protected JLabel labMovieName;
 	
 	/** The table to display the release date of the movie */
 	protected JLabel labMovieReleaseDate;
 	
 	/** The label to display the description of the movie */
 	protected JLabel labMovieDescription;
 	
 	/** Initializes all of the components needed to display the movie information for the selected
 	 *  video
 	 */
 	
 	public IMDBVideoTreeView() {
 		sidePanel = new JPanel();
 		
 		//initialize the labels for displaying the movie information
 		labMovieName = new JLabel("Airplane!");		
		labMovieDescription = new JLabel("<html>An airplane crew takes ill. Surely the only person capable of landing the plane is an ex-pilot afraid to fly. But don't call him Shirley. </html>");		
 		labMovieReleaseDate = new JLabel("2 July 1980 (USA)");
 		
		labMovieDescription.setMaximumSize(new Dimension(getPreferredSize().width - 3* HORIZONTAL_PADDING, 500));
 		
 		System.out.println("Width?L " + getPreferredSize().width);
 		
 		SpringLayout layout = new SpringLayout();
 		
		layout.putConstraint(SpringLayout.WEST, labMovieName, HORIZONTAL_PADDING, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, labMovieName, VERTICAL_PADDING, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, labMovieName, -HORIZONTAL_PADDING, SpringLayout.EAST, this);
 		
		layout.putConstraint(SpringLayout.WEST, labMovieReleaseDate, HORIZONTAL_PADDING, SpringLayout.WEST, this);
 		layout.putConstraint(SpringLayout.NORTH, labMovieReleaseDate, VERTICAL_PADDING, SpringLayout.SOUTH, labMovieName);
 		
		layout.putConstraint(SpringLayout.WEST, labMovieDescription, HORIZONTAL_PADDING, SpringLayout.WEST, this);
 		layout.putConstraint(SpringLayout.NORTH, labMovieDescription, VERTICAL_PADDING , SpringLayout.SOUTH, labMovieReleaseDate);
		layout.putConstraint(SpringLayout.EAST, labMovieDescription, -2 * HORIZONTAL_PADDING , SpringLayout.EAST, this);
 		
 		sidePanel.setLayout(layout);
 		
 		sidePanel.add(labMovieName);
 		sidePanel.add(labMovieDescription);
		sidePanel.add(labMovieReleaseDate);
		
		sidePanel.setPreferredSize(new Dimension(300, 100));
 		
 		add(sidePanel, BorderLayout.EAST);
 	}
 	
 }
