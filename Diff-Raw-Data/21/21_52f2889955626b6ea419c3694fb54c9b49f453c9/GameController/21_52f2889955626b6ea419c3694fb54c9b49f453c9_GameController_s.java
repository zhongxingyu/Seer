 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JButton;
 
 public class GameController {
 	
 	private LifeSimulation gameLogic;
 	private GameUI userInterface;
 	private Timer timer;
 	private TimerTask task;
 	private boolean isRunning;
 	
 	public GameController(LifeSimulation logic, GameUI uInterface) {
 		gameLogic = logic;
 		userInterface = uInterface;
 		isRunning = false;
 		timer = new Timer();
 		
 		setListeners();
 	}
 	
 	private ActionListener menuItemLoadClicked = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			if(! isRunning) {
 				PropertyFileIO properties = new PropertyFileIO();
 				gameLogic = properties.readLifeSim(userInterface.fileBrowser());
 				//ReadWriteXML ioXML = new ReadWriteXML();
 				//gameLogic = ioXML.readLifeSim(userInterface.fileBrowser());
 				//BinaryFileInputOutput binaryFile = new BinaryFileInputOutput();
 				//gameLogic = binaryFile.readLifeSim(userInterface.fileBrowser());//, gameLogic);
 				
 				userInterface.initializeGrid(gameLogic);
 				userInterface.addGridButtonListener(gridButtonClicked);
 			}
 			else {
 				userInterface.warningMessage();
 			}
 		}
 	};
 	
 	private ActionListener menuItemStoreClicked = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			if(! isRunning) {
 				PropertyFileIO properties = new PropertyFileIO();
 				properties.writeLifeSim(userInterface.getFileName(), gameLogic);
 				//ReadWriteXML ioXML = new ReadWriteXML();
 				//ioXML.writeLifeSim(userInterface.getFileName(), gameLogic);
 				//BinaryFileInputOutput binaryFile = new BinaryFileInputOutput();
 				//binaryFile.writeLifeSim(userInterface.getFileName(), gameLogic);
 			}
 			else {
 				userInterface.warningMessage();
 			}
 		}
 	};
 	
 	private ActionListener gridButtonClicked = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			JButton cell = ((JButton)e.getSource());
 			userInterface.setLifeFromGrid(cell, gameLogic);
 		}
 	};
 
 	private ActionListener clearClicked = new ActionListener() {
 		public void actionPerformed(ActionEvent event) {
 			stopTimer(timer);
 			userInterface.clearGrid(gameLogic);
 			userInterface.enableOrDisableTextFields(true);
 		}
 	};
 
 	private ActionListener stepClicked = new ActionListener() {
 		public void actionPerformed(ActionEvent event) {
 			stopTimer(timer);
 			evolve();
 			userInterface.enableOrDisableTextFields(true);
 		}
 	};
 
 	private ActionListener startClicked = new ActionListener() {
 		public void actionPerformed(ActionEvent event) {
 			startTask();
 		}
 	};
 	
 	private ActionListener changeGridSizeClicked = new ActionListener() {
 		public void actionPerformed(ActionEvent event) {
 			if (! isRunning) {
 				gameLogic = userInterface.changeGridSize();
 				userInterface.addGridButtonListener(gridButtonClicked);
 			}
 			else {
 				userInterface.warningMessage();
 			}
 		}
 	};
 	
 	private void startTask() {
 		if (!isRunning) {
 			timer = new Timer();
 			task = new TimerTask() {
 				public void run() {
 					evolve();
 				}
 			};
			timer.schedule(task, 500, 500);
 			isRunning = true;
 			userInterface.enableOrDisableTextFields(false);
 		}
 	}
 	
 	private void stopTimer(Timer timer) {
 		try {
 			timer.cancel();
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		}
 		isRunning = false;
 	}
 	
 	private void evolve() {
 		gameLogic.evolve();
 		userInterface.setLifeInGrid(gameLogic);
 	}
 	
 	private void setListeners() {
 		userInterface.addBtnStartActionListener(startClicked);
 		userInterface.addBtnStepActionListener(stepClicked);
 		userInterface.addBtnClearActionListener(clearClicked);
 		userInterface.addGridButtonListener(gridButtonClicked);
 		userInterface.addMenuItemLoadListener(menuItemLoadClicked);
 		userInterface.addMenuItemStoreListener(menuItemStoreClicked);
 		userInterface.addChangeGridSizeButtonListener(changeGridSizeClicked);
 	}
 }
