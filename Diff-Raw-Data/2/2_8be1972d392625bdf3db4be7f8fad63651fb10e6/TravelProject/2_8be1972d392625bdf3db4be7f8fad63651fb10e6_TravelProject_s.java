 package travelPlanner;
 
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 /**
  * The class TravelProject manages data for travelprojects in the application 
  * "TravelPlanner" and create GUI-components from this data. 
  * for the user to view and use. 
  * @author Joakim Candefors
  *
  */
 
 public class TravelProject extends Slide{
 
 	private String[] projectInfo; 				//serialized
 
 	private final int hometown = 1;
 	private String[] labels;
	private final String PROJECTTITLE = "Resenär: ", EDITPROJECTBUTTONTEXT = "Redigera reseprojekt", CREATEBUTTONTEXT = "Skapa destination",
 	DESTINATIONLABELTEXT = "Destinationer:";
 	
 	/**
 	 * Constructor of class TravelProject. firstTime = true should only be called with when user is created.
 	 * @param layoutHandler The layoutHandler used for laying out components in the frame.
 	 * @param title	The title of this project.
 	 * @param firstTime true if new user and project to be created. Else false.
 	 */
 	public TravelProject(LayoutHandler layoutHandler, String userName, boolean firstTime) {
 		super(layoutHandler, userName);
 		super.title = userName;		
 		labels = new String[]{"Reseprojekt:","Hemstad:", "Startdatum:","Slutdatum:"};
 		if(firstTime){
 			new File("data/"+userName).mkdir();
 			projectInfo = new String[labels.length];
 			super.destinations = new ArrayList<String>();
 			editTravelProject();
 			saveDestinations();
 		}else{
 			loadDestinations();
 			loadProjectInfo();
 			prepareLayout();
 		}
 	}	
 	
 	/**
 	 * Calls the layout help methods to create and send components to layoutHandler for layout.
 	 */
 	public void prepareLayout(){
 		layoutHandler.clearAll();
 		generalProjectLayout();
 		destinationLayout();
 		mainLayout();
 	}
 
 	/**
 	 * Creates general components common for every TravelProject.
 	 */
 	public void generalProjectLayout(){
 		layoutHandler.updateTitle(PROJECTTITLE + title);
 		
 		layoutHandler.addToMenuUp(Box.createRigidArea(new Dimension(20, 10)));
 		layoutHandler.addToMenuUp(new ProjectButton(EDITPROJECTBUTTONTEXT, 1));
 		layoutHandler.addToMenuUp(new DestinationButton(CREATEBUTTONTEXT, 2));
 		layoutHandler.addToMap(new MapLabel(projectInfo[hometown],destinations));
 	}
 
 
 	/**
 	 * Creates components (DestinationButton) for all the destinations in the TravelProject.
 	 */
 	public void destinationLayout(){
 		layoutHandler.addToMenuLow(Box.createRigidArea(new Dimension(20, 10)));
 		JLabel menuLabel = new JLabel(DESTINATIONLABELTEXT);
 		menuLabel.setFont(new Font("Tahoma", Font.BOLD, 16));			
 		layoutHandler.addToMenuLow(menuLabel);		
 		Iterator<String> iterator = destinations.iterator();
 		while (iterator.hasNext()){
 			layoutHandler.addToMenuLow(new DestinationButton(iterator.next(), DestinationButton.OPEN));
 		}
 	}
 	
 	/**
 	 * Creates components from mainInfo and labels and adds them to the frame.
 	 */
 	public void mainLayout(){
 		JPanel panel = new JPanel(new GridLayout(0,2,20,8));
 		panel.setOpaque(false);
 		for(int index = 0; index < labels.length; index++){
 			
 			JLabel tempLabel1 = new JLabel(labels[index]);
 			tempLabel1.setFont(new Font("Tahoma", Font.BOLD, 16));
 			panel.add(tempLabel1);
 			JLabel tempLabel2 = new JLabel(projectInfo[index]);
 			tempLabel2.setFont(new Font("Tahoma", Font.PLAIN, 16));
 			panel.add(tempLabel2);
 		}
 			layoutHandler.addToMain(panel);
 		
 	}
 	
 	/**
 	 * Saves the project information data to disk.
 	 */
 	public void saveProjectInfo(){
 		try{															
 			ObjectIO.saveObject(projectInfo, userName, title);
 		}catch (IOException e){
 			ErrorHandler.printError(e, this.getClass().toString());
 		}
 	}
 	
 	/**
 	 * Takes the edited information and applies it to current project or destination and saves to disk. 
 	 * @param editedProjectInfo The array with the main information edited in EditTravelProject or EditDestination.
 	 */
 	public void updateProjectInfo(String[] editedProjectInfo){		
 		projectInfo = editedProjectInfo;
 		saveProjectInfo();
 	}
 
 	
 	/**
 	 * Creates a new EditTravelProject and then updates the data in the layout.
 	 */
 	public void editTravelProject(){
 		new EditTravelProject(this, projectInfo, labels);	//vilken skapar popup och kallar vid "spara" p� updateMainInfo();	
 	}
 	
 	/**
 	 * 
 	 */
 	public void loadProjectInfo(){
 		try{
 			projectInfo = (String[]) ObjectIO.loadObject(userName, title);
 			}catch (ClassNotFoundException e){
 			ErrorHandler.printError(e, this.getClass().toString());
 		}catch (IOException e){
 			ErrorHandler.printError(e, this.getClass().toString());
 		}
 	}
 
 
 	/**
 	 * ProjectButton is a JButton specifically for TravelProjects.
 	 */
 	public class ProjectButton extends JButton implements ActionListener{
 		private int actionType;
 		private final String EDITTOOLTIPTEXT = "Redigera Reseprojekt";
 
 		/**
 		 * Constructor of a project button.
 		 * @param text	The text of the ProjectButton.
 		 * @param actionType
 		 */
 		public ProjectButton(String text, int actionType){
 			super(text);
 			super.setCursor(new Cursor(Cursor.HAND_CURSOR));
 			super.setBorderPainted(false);
 			this.actionType = actionType;
 			if(actionType == 1){
 				super.setToolTipText(EDITTOOLTIPTEXT);
 			}
 			super.addActionListener(this); 
 		}
 		/**
 		 * 		
 		 * @param e Action when button fired.
 		 */
 		public void actionPerformed(ActionEvent e) {
 			if(this.actionType == 1){			
 				editTravelProject();
 			}
 		}
 	}
 }
