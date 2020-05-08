 package gui;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.swing.JMenuBar;
 import util.Util;
 import java.util.ArrayList;
 
 public class Explorer {	
 	
 	/**
 	 * Used to disable/enable our menus
 	 */
 	JMenuBar menu;
 	
 	private static BufferedWriter output;
 	
 	/**
 	 * Write to logfile if true
 	 */
 	boolean export = false;
 	
 	/**
 	 * Data from accepted trials should be stored if this is true
 	 */
 	boolean retainData;
 	
 	/**
 	 * Holds vector data from each accepted trial.
 	 * Data format is: 
 	 * [panel0.iA][panel0.iB]...[conc.iA][conc.iB][verbNum]
 	 */
 	ArrayList<int[]> points = new ArrayList<int[]>();
 	
 	/**
 	 * Filename to export to
 	 */
 	String exportFile;
 	
 	/**
 	 * The container panel object
 	 */
 	ContainerPanel cPanel;
 	
 	/**
 	 * If false, stop the explore process
 	 */
 	boolean continueExplore;
 	
 	/**
 	 * Our ContainerPanel's panels
 	 */
 	InteractivePanel panels[];
 	
 	/**
 	 * Our ContainerPanel's constrain array
 	 */
 	ConstrainPanel constrain[];
 
 	/**
 	 * Our ContainerPanel's conclusion
 	 */
 	ConclusionPanel conclusion;
 	
 	/**
 	 * These will keep track of the number of each verb found
 	 */
 	long numCauses = 0;
 	long numAllows = 0;
 	long numHelps = 0;
 	long numPrevents = 0;
 	long numDespites = 0;
 	long numInvalids = 0;
 					
 	/**
 	 * Number of Panels
 	 */
 	int total;
 	
 	/**
 	 * Total number of accepted trials
 	 */
 	long num_accepted;
 	
 	/**
 	 * Total number of attempted trials
 	 */
 	long attempts;
 	
 	/**
 	 * Predicted number of attempted trials
 	 * Determined by the number of free vectors.
 	 */
 	double predictedAttempts;
 
 	/**
 	 * The length of panel zero's endstate vector.
 	 * Determines how long the explorer runs.
 	 */
 	public int eZero = -1;
 	
 	/**
 	 * Points to the hierVal of the vector which is currently being varied
 	 * by the explorer
 	 */
 	int currPointer;
 	
 	/**
 	 * Current percentage complete for total explore
 	 */
 	int aPercDone;
 	
 	/**
 	 * Last percentage complete for total explore
 	 */
 	int oldPercDone;
 	
 	/**
 	 * Stores written state of premises
 	 */
 	String premises;
 	
 	/**
 	 * Holds the last known working configuration
 	 * [iA][iB] <-- panel x
 	 */
 	int restore[][];
 	
 	/**
 	 * Holds the verbs of the initial configuration
 	 */
 	String[] words;
 	
 	String overallAffector;
 	String overallPatient;
 	
 	/**
 	 * Stores the current values of all panels iA and iB
 	 * panel[i].iA = hierVals[2*i]
 	 * panel[i].iB = hierVals[2*i + 1]
 	 */
 	int hierVals[];
 	
 	/**
 	 * Locked state of the panels
 	 * panel[i].rALocked = hierLocked[2*i]
 	 * panel[i].rBLocked = hierLocked[2*i + 1]
 	 */
 	boolean hierLocked[];
 	
 	/**
 	 * Initial Values of the panels
 	 * panel[i].iA = hierInitVals[2*i]
 	 * panel[i].iB = hierInitVals[2*i + 1]
 	 */
 	int hierInitVals[];
 	
 	/**
 	 * [hier#][from][from]
 	 * Stores the information of which panels to update each from.
 	 */
 	int updateFrom[][];
 	
 	/**
 	 * Total number of locked vectors
 	 */
 	int totalLocked;
 	
 	/**
 	 * Start time of the explore
 	 */
 	long timeStart;
 	
 	String timeTaken;
 	String timeRemaining;
 	
 	
 //---------------------------------------------------------------------------------------
 //	PUBLIC METHODS
 	
 	/**
 	 * Creates a new Explorer. Explorer needs the container panel
 	 * in order to manipulate the model. It needs the MenuBar in order
 	 * to lock down the menus.
 	 */
 	public Explorer (ContainerPanel cp, JMenuBar JMenu)
 	{
 		cPanel = cp;
 		menu = JMenu;
 		
 		panels = cp.panels;
 		constrain = cp.constrain;
 		conclusion = cp.conclusion;
 	}
 	
 	/**
 	 * Set to true before starting explore if it is desired to
 	 * export the explore to a file.
 	 * @param exportExplore
 	 */
 	public void setExport (boolean exportExplore)
 	{
 		export = exportExplore;
 	}
 	
 	/**
 	 * Set to true before explore if it is desire that data be 
 	 * retained normally for the benefit of the grapher.
 	 * @param _retainData
 	 */
 	public void setRetainData (boolean _retainData)
 	{
 		retainData = _retainData;
 	}
 	
 
 	/**
 	 * Creates a new Buffered Writer for the explorer
 	 * @param fileName The name of the file to write to
 	 * @throws IOException
 	 */
 	public void initializeBufferedWriter (String fileName) throws IOException
 	{
 		output = new BufferedWriter(new FileWriter(fileName));
 	}
 	
 	/**
 	 * Called when the user clicks the stop explore button
 	 */
 	public void stopExplore ()
 	{
 		continueExplore = false;
 	}
 	
 	/**
 	 * Explores the given space and optionally writes to output CSV file
 	 */
 	public void explore() throws IOException {
 		// Initialize Variables
 		initVars();
 		
 		// Disable Menus
 		menu.setEnabled(false);
 						
 		// Create a record of our original premises
 		premises = getPremises();	
 		
 		// Record the initial state of the panels
 		saveInitialState();
 		
 		// Find update dependencies within the panels
 		findDependencies();
 		
 		//This is main exploration loop
 		while(Math.abs(hierVals[0]) <= eZero && continueExplore) {
 			attempts++;
 			updatePercComplete();
 			
 			// Increment the current vector
 			if(hierInitVals[currPointer] > 0)
 				hierVals[currPointer]++;
 			else
 				hierVals[currPointer]--;
 				
 			// Update all locked vectors
 			for(int j = 0; j < totalLocked; j++) 
 			{
 				hierVals[updateFrom[0][j]] = hierVals[updateFrom[1][j]] + hierVals[updateFrom[2][j]];
 				if (updateFrom[3][j] != Integer.MAX_VALUE)
 					hierVals[updateFrom[0][j]] += hierVals[updateFrom[3][j]];
 			}
 			
 			// Check if the change is valid
 			boolean accepted = checkConsistent();
 			
 			// Update variables if necessary
 			handleTrial(accepted);
 			
 			// Find a new current pointer
 			for (int j = hierVals.length - 1; j >= 0; j--) {
 				if(j == 0)
 					currPointer = j;
 				
 				if(!hierLocked[j] && Math.abs(hierVals[j]) < eZero) 
 				{ 
 					currPointer = j; break; 
 				}
 			}
 
 			// Reset all values below current pointer
 			for(int j = currPointer; j < hierVals.length - 1; j++) 
 			{
 				if (hierInitVals[j+1] > 0) 
 					hierVals[j+1] = 1;
 				else 
 					hierVals[j+1] = -1;
 			}
 		}
 		
 		timeTaken = Util.parseTime(System.currentTimeMillis() - timeStart);
 		
 		unlockRestore();
 		
 		if (export) 
 			output.close();
 	}
 	
 	public ArrayList<int[]> getRawData ()
 	{
 		return points;
 	}
 	
 	public void setEMag (int eMag)
 	{
 		eZero = eMag;
 	}
 	
 //---------------------------------------------------------------------------------------
 //	PRIVATE METHODS	
 	
 	/**
 	 * 	Gets the list of original premises for display in the final explore window
 	 */
 	private String getPremises () throws IOException
 	{
 		String premise = "";
 		int total = panels.length;
 		for (int t = 0; t < total; t++)
 		{
 			if (export) {
 				String out = panels[t].getAWord();
 				if (panels[t].getASubscript() != null)
 					out += "("+panels[t].getASubscript().toLowerCase()+")";
 				out += ",";
 				output.write(out);
 				out = panels[t].getBWord();
 				if (panels[t].getBSubscript() != null)
 					out += "("+panels[t].getBSubscript().toLowerCase()+")";
 				out += ",";
 				output.write(out);
 			}
 			if (panels[t].aNegated && !panels[t].verb.equals("Invalid")) {
 				premise += "N_";
 			} if (panels[t].verb.equals("Allows")) {
 				premise += "ALLOW";
 			} else if (panels[t].verb.equals("Helps")) {
 				premise += "HELP";
 			} else if (panels[t].verb.equals("Causes")) {
 				premise += "CAUSE";
 			} else if (panels[t].verb.equals("Despite")) {
 				premise += "DESPITE";
 			} else if (panels[t].verb.equals("Prevents")) {
 				premise += "PREVENT";
 			}
 			if (panels[t].eNegated && !panels[t].verb.equals("Invalid"))
 				premise += "_N";
 			if (t < total - 1) {
 				if(constrain[t].constrained) {
 					String s = constrain[t].verb.toLowerCase().substring(0,1);
 					if (s.equals("h")) s = "a";
 					premise += " -"+s+"- ";
 				} else
 					premise += " / ";
 			}
 		}
 		if (export) output.write("Accepted\n");
 
 		return premise;
 	}
 	
 	private void initVars ()
 	{
 		numCauses = 0;
 		numAllows = 0;
 		numHelps = 0;
 		numPrevents = 0;
 		numDespites = 0;
 		numInvalids = 0;
 		num_accepted = 0;
 		attempts = 0;
 
 		total = panels.length;
 
 		restore = new int[2][total];
 		
 		words = new String[total];
 		
 		continueExplore = true;
 		
 		overallAffector = conclusion.getAWord();
 		overallPatient = conclusion.eNegated ? Util.NEG_CHAR + conclusion.getBWord() : conclusion.getBWord();
 		
 		hierVals = new int[panels.length * 2];
 		hierLocked = new boolean[panels.length * 2];
 		hierInitVals = new int[panels.length * 2];
 		totalLocked = 0;
 		
 		aPercDone = oldPercDone = 0;
 		currPointer = total * 2 - 1;
 		
 		// Start the timer
 		timeStart = System.currentTimeMillis();
 	}
 	
 	/**
 	 * Collects and stores information about the initial state of the panels.
 	 * Also predicts the total number of attempts.
 	 */
 	private void saveInitialState ()
 	{
 		for (int i = 0; i < total; i++) 
 		{
 			panels[i].setLockDown(true);
 			words[i] = panels[i].verb;
 			if (panels[i].iA > 0) hierVals[2*i] = 1; //Don't include 0
 			else hierVals[2*i] = -1;
 			hierLocked[2*i] = panels[i].rALocked;
 			if(panels[i].rALocked) totalLocked++;
 			hierInitVals[2*i] = panels[i].iA;
 			
 			if (panels[i].iB > 0) hierVals[2*i+1] = 1; //Don't include 0
 			else hierVals[2*i+1] = -1;
 			hierLocked[2*i+1] = panels[i].rBLocked;
 			if(panels[i].rBLocked) totalLocked++;
 			hierInitVals[2*i+1] = panels[i].iB;
 		}
 		
 		int totalFree = 2 * total - totalLocked;
 		
 		if (eZero == -1)
 			eZero = Math.abs(panels[0].iE);
 		
 		predictedAttempts = Math.pow(eZero+1,totalFree);
 	}
 	
 	/**
 	 * Finds which vectors can be updated safely from which other vectors
 	 */
 	private void findDependencies ()
 	{
 		//[hier#][from][from]
 		// Stores the information of which panels to update each from.
 		updateFrom = new int[4][totalLocked];
 		int curr = 0;
 		
 		while(curr < totalLocked) {
 			for(int i=0; i<panels.length; i++) {
 				if(panels[i].rALocked) {
 					if(!panels[i-1].rALocked && !panels[i-1].rBLocked) { //We can derive from parent
 							updateFrom[0][curr] = i*2;
 							updateFrom[1][curr] = i*2 - 1;
 							updateFrom[2][curr] = i*2 - 2;
 							updateFrom[3][curr] = Integer.MAX_VALUE;
 							panels[i].rALocked = false;
 							/*if (panels[i-1].constrainedAbove) {
 								updateFrom[1][curr] = i*2 - 1;
 								updateFrom[2][curr] = i*2 - 3;
 								updateFrom[3][curr] = i*2 - 4;
 							}*/
 							if (panels[i-1].constrainedAbove) {
 								updateFrom[1][curr] = i*2 - 1; //parent.ib
 								updateFrom[2][curr] = i*2 - 2; //parent.ia
 								updateFrom[3][curr] = i*2 - 4; //parent.parent.ia
 							}
 							curr++;
 					}
 				}
 				if(panels[i].rBLocked) {
 					if(!panels[i+1].rALocked && !panels[i+1].rBLocked) {
 							updateFrom[0][curr] = i*2 + 1;
 							updateFrom[1][curr] = i*2 + 2;
 							updateFrom[2][curr] = i*2 + 3;
 							updateFrom[3][curr++] = Integer.MAX_VALUE;
 							panels[i].rBLocked = false;
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Checks the percentage complete and updates the window if necessary
 	 */
 	private void updatePercComplete ()
 	{
 		aPercDone = (int)(((double)attempts/predictedAttempts) * 100.0);
 		if(aPercDone != oldPercDone) 
 		{
 			long elapsed = System.currentTimeMillis() - timeStart;
 			long remaining = 0;
 			if (aPercDone > 0) remaining = (100 - aPercDone) * (elapsed / aPercDone);
 			timeTaken = Util.parseTime(elapsed);
 			timeRemaining = Util.parseTime(remaining);
 			oldPercDone = aPercDone;
 		}
 	}
 	
 	/**
 	 * Updates the model and checks to make sure all vectors have
 	 * their original verbs and all constraints are consistent
 	 * @return true if consistent
 	 */
 	private boolean checkConsistent ()
 	{
 		boolean valid = true;
 		for(int i=0; i<panels.length; i++) {
 			panels[i].iA = hierVals[i*2];
 			panels[i].iB = hierVals[i*2+1];
 			panels[i].iR = hierVals[i*2] + hierVals[i*2+1];
 			panels[i].updateValues();
 			if(!panels[i].verb.equals(words[i])) {
 				valid = false;
 			}
 			if(i > 0 && !constrain[i-1].inCompliance()) {
 				valid = false;
 			}
 		}
 		return valid;
 	}
 	
 	/**
 	 * Updates variables in the case of an accepted trial and 
 	 * writes to the logfile
 	 */
 	private void handleTrial (boolean accepted) throws IOException
 	{
 		if (accepted)
 		{
 			conclusion.update();
 			if(conclusion.verb.equals("Causes")) {
 				numCauses++;
 			}
 			else if(conclusion.verb.equals("Allows")) { 
 				numAllows++;
 			}
 			else if(conclusion.verb.equals("Helps")) {
 				numHelps++;
 			}
 			else if(conclusion.verb.equals("Prevents")) {
 				numPrevents++;
 			}
 			else if(conclusion.verb.equals("Despite")) {
 				numDespites++;
 			}
 			else if(conclusion.verb.equals("Invalid")) {
 				numInvalids++;
 			}
 			num_accepted++;
 		}
 		
 		if (export) {
 			for (int i = 0; i < panels.length; i++)
 				output.write(panels[i].iA + "," + panels[i].iB + ",");
 			if (accepted) output.write("Y\n");
 			else output.write("N\n");
 		}
 		
 		// Create a point for graph3d to display
 		if (retainData && accepted) {
 			int[] tuple = new int[total * 2 + 3];
 			int tupleIndex = 0;
 			
 			for (int i = 0; i < total; i++)			
 			{
 				tuple[tupleIndex++] = panels[i].iA;
 				tuple[tupleIndex++] = panels[i].iB;
 			}
 			tuple[tupleIndex++] = conclusion.iA;
 			tuple[tupleIndex++] = conclusion.iB;
 			
 			tuple[tupleIndex++] = Util.getVerbNum(conclusion.verb);
 			
 			points.add(tuple);
 		}
 	}
 	
 	/**
 	 * Restores the initial state of the panels and unlocks the menus
 	 */
 	private void unlockRestore ()
 	{
 		// Unlock the panels and restore the values
 		for (int i = 0; i < total; i++) {
 			panels[i].setLockDown(false);
 			panels[i].iA = hierInitVals[2*i];
 			panels[i].iB = hierInitVals[2*i + 1];
			panels[i].verb = words[i];
			panels[i].rALocked = hierLocked[2*i];
			panels[i].rBLocked = hierLocked[2*i+1];
 		}
 		cPanel.update();
 		cPanel.repaintAll();
 		menu.setEnabled(true);
 		aPercDone = 100;
 	}
 }
