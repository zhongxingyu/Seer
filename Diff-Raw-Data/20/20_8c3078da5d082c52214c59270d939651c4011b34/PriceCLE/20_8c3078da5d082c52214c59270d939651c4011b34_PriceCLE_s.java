 package pkgPriceCLE;
 
 import java.awt.Color;
 import java.awt.Dialog;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.border.BevelBorder;
 
 //====================================================================
 /** Price Chinese Learning Experiment
  *  <P>Purpose: This application was designed and written for use
  *  as the primary experimental software for an experiment in 
  *  learning conducted by Jodi Price.
  *  @author Dr. Rick Coleman, Modified By Terry Meacham
  *  @version 1.0
  *  Date: January, 2009
  */
 //===================================================================
 public class PriceCLE extends JFrame
 {
 	/** Main panel */
 	private JPanel m_MainPanel;
 	
 	/** Instructions text area */
 	private PCLE_InstructionsPanel m_InstructionsPan;
 	
 	/** Continue button */
 	private JButton m_ContinueButton;
 	
 	/** Image panel */
 	private PCLE_ImagePanel m_ImagePanel;
 	
 	/** Name of the experiment definition file */
 	private String m_sExpFileName = "Instructions/Experiment.txt";
 	
 	/** Vector of states defining the experiment */
 	private Vector m_vExpStates;
 	
 	/** Index of the current state */
 	private int m_iCurStateIdx;
 	
 	/** Reference to the current state */
 	private PCLE_State m_CurState;
 	
 	/** Reference to the last state */
 	private PCLE_State m_LastState;
 	
 	/** Reference to the image factory */
 	private PCLE_ImageFactory m_ImageFactory;
 	
 	/** ID of this subject */
 	private String m_sSubjectID;
 	
 	/** Experiment time string as HH:MM:SS*/
 	private String m_sExpTimeStr;
 	
 	/** Experiment date string as DayOfWeek, mm, dd, yyyy */
 	private String m_sExpDateStr;
 
 	/** Experiment Condition */
 	private int m_iCondition;
 	
 	/** Current trial number */
 	private int m_iTrialNumber;
 	
 	/** Array of image indices for Trial 1, group 1 */
 	private int[] m_iTrial1Group1;
 	
 	/** Array of image indices for Trial 1, group 2 */
 	private int[] m_iTrial1Group2;
 	
 	/** Array of image indices for Trial 2, group 1 */
 	private int[] m_iTrial2Group1;
 	
 	/** Array of image indices for Trial 2, group 2 */
 	private int[] m_iTrial2Group2;
 
 	/** Array of all indices in both tests for checking EOL */
 	private int[] m_iEOLOrder;
 	
 	/** Array of indices (based on 18) for order of testing trial 1 */
 //	private int[] m_iTestOrder1;
 	
 	/** Array of indices (based on 18) for order of testing trial 1 */
 //	private int[] m_iTestOrder2;
 
 	/** User estimates: 0,3 = pretrial, 1,4 = posttrial, 2,5 = postdict */
 	private int[] m_iUserEstimates;
 
 	/** User estimates: 0 = trial 1 points, 1 = trial 2 points */
 	private int[] m_iUserPoints;
 	
 	/** File to write report to */
 	private File m_ReportFile;
 	
 	/** The file writer encapsulating the report file */
 	private FileWriter m_FileWriter;
 	
 	/** The buffered writer encapsulating the file writer */
 	private BufferedWriter m_BufWriter;
 	
 	/** The calendar used in timing the experiment */
 	private Calendar m_ExpCalendar;
 	
 	/** Start time in milliseconds */
 	private long m_lStartTimeMillis;
 	
 	/** Number of study times for each character to be written to file */
 	private int m_iMaxStudyTimes;
 	
 	/** Title font used for large labels */
 	public static final Font SysTitleFontB = new Font("SansSerif", Font.BOLD, 28);
 	
 	/** Font used in the lists */
 	public static final Font SysButtonFontB = new Font("SansSerif", Font.BOLD, 17);
 
 	/** Label font used for most labels */
 	public static final Font SysLabelFontB = new Font("SansSerif", Font.BOLD, 21);
 
 	/** Label font used for most labels */
 	public static final Font SysInstructionFont = new Font("Courier", Font.PLAIN, 18);
 
 	/** Label font used for most labels */
 	public static final Font SysLabelFontP = new Font("SansSerif", Font.PLAIN, 20);
 
 	/** Small font used for instructions and labels on text widgets */
 	public static final Font SysSmallFont = new Font("SansSerif", Font.PLAIN, 18);
 	
 	//-----------------------------------------------
 	// Default constructor
 	//-----------------------------------------------
 	public PriceCLE()
 	{
 		this.setSize(1024, 768);
 		this.setLocation(50, 50);
 		this.setTitle("Price Chinese Learning Experiment");
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		
 		// Create the main panel
 		m_MainPanel = new JPanel();
 		m_MainPanel.setSize(1024, 768);
 		m_MainPanel.setLocation(0, 0);
 		m_MainPanel.setLayout(null);
 		m_MainPanel.setBackground(Color.LIGHT_GRAY);
 		m_MainPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
 		this.getContentPane().add(m_MainPanel);
 		
 		// Create and show the instructions panel
 		m_InstructionsPan = new PCLE_InstructionsPanel(1010, 686, this);
 		m_InstructionsPan.setLocation(-1, -1);
 		m_MainPanel.add(m_InstructionsPan);
 		
 		// Create the image panel
 		m_ImagePanel = new PCLE_ImagePanel(this, 1010, 728);
 		m_ImagePanel.setLocation(-1, -1);
 		m_MainPanel.add(m_ImagePanel);
 		m_ImagePanel.setVisible(false);
 		
 		// Create and add the Continue button
 		m_ContinueButton = new JButton("Continue");
 		m_ContinueButton.setSize(100, 20);
 		m_ContinueButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
 		m_ContinueButton.setLocation(462, 692);
 		m_ContinueButton.addActionListener(
 			new ActionListener()
 			{
 				public void actionPerformed(ActionEvent e)
 				{
 					if(m_CurState.getChangeStateAction() == PCLE_State.CLICK_CONTINUE) {
 						setNextState();
 					}
 				}
 			}
 		);
 
 		m_MainPanel.add(m_ContinueButton);
 		
 		// Create the image factory
 		m_ImageFactory = new PCLE_ImageFactory();
 		m_InstructionsPan.setImageFactory(m_ImageFactory);
 		m_ImagePanel.setImageFactory(m_ImageFactory);
 
 		// Set up the experiment arrays
 		m_iTrial1Group1 = new int[9];
 		m_iTrial1Group2 = new int[9];
 		m_iTrial2Group1 = new int[9];
 		m_iTrial2Group2 = new int[9];
 
 		//Ease of Learning - Values rated by the user at the start of the program
 		m_iEOLOrder = new int[36];
 
 		m_iUserEstimates = new int[6];
 		m_iUserPoints = new int[2];
 		
 		m_iTrialNumber = 0;
 		m_iMaxStudyTimes = 5;  // Set default to 5 if not read from experiment.txt
 
 		// Get the subject ID first
 		m_sSubjectID = JOptionPane.showInputDialog("Please enter the subject's ID.");
 		if (m_sSubjectID == null)
 			System.exit(0);
 
 		String sCondition;
 		do {
 			sCondition = JOptionPane.showInputDialog("Please enter the experimental Condition\n(This should be given by the experimenter).");
 			if (sCondition == null) {
 				System.exit(0);
 			}
 		} while (!setCondition(sCondition));
 		
 		// Set up the experiment states
 		m_vExpStates = new Vector();
 		if(!initExperiment())	// Oops! Couldn't read the file
 			System.exit(0);
 		
 		m_iCurStateIdx = -1;
 		m_LastState = null;
 		m_CurState = null;
 		
 		// Show the window
 		this.setVisible(true);
 		
 		// Set the initial state
 		this.setNextState();
 	}
 
 	//-----------------------------------------------
 	/** Set the condition; return false 
 	 * if the string is invalid 
 	 */	
 	//-----------------------------------------------
 	public boolean setCondition(String condition)
 	{
 		/* --Disallow numbers for conditions
 		 * try {
 		 *
 			m_iCondition = Integer.parseInt(condition);
 		}
 		catch(Exception e) {*/
 			if (condition.length() == 2) {
 				char cond = condition.charAt(0);
 				if (cond == 'A' || cond == 'a')
 					m_iCondition = 1;
 				else if (cond == 'B' || cond == 'b')
 					m_iCondition = 2;
 				else if (cond == 'C' || cond == 'c')
 					m_iCondition = 3;
 				else if (cond == 'D' || cond == 'd')
 					m_iCondition = 4;
 				else return false;
 				cond = condition.charAt(1);
 				if (cond == 'N' || cond == 'n')
 					m_iCondition += 0; //No feedback condition
 				else if (cond == 'F' || cond == 'f')
 					m_iCondition += 4; //Feedback condition
 				else return false;
 			} else 
 				return false;
 		//}		
 		return (m_iCondition > 0 && m_iCondition < 9);
 	}
 	
 	//-----------------------------------------------
 	/** Set up to run the experiment */
 	//-----------------------------------------------
 	public boolean initExperiment()
 	{
 		// Open the experiment file
 		FileReader		instFile;
 		BufferedReader	bufReader = null;
 		String 			line;
 		PCLE_State 		thisState = null;
 		
 		// Open the file
 		try
 		{
 			instFile = new FileReader(m_sExpFileName);
 		}
 		catch(FileNotFoundException e1) // If we failed to opened it
 		{
 			JOptionPane.showMessageDialog(this, 
 					"Error: Unable to open " + m_sExpFileName, 
 					"Error Opening Experiment File", JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 		// Read the text strings and add them to the text area
 		try
 		{
 			bufReader = new BufferedReader(instFile);
 			while((line = bufReader.readLine()) != null)
 			{
 				// See if we need to create a new State
 				if((line.contains("STATE")) && (!(line.contains("/STATE"))))
 				{
 					thisState = new PCLE_State();
 				}
 				// Check for options in "Show Instructions"
 				else if(line.contains("Show Instructions"))
 				{
 					thisState.setState(PCLE_State.SHOW_INSTRUCTIONS);
 				}
 				// Check for options in "Show Instructions"
 				else if(line.contains("Show Point Notification"))
 				{
 					thisState.setState(PCLE_State.SHOW_POINT_NOTIFICATION);
 				}
 				else if(line.contains("FILE"))
 				{
 					String insFile = line.substring(line.indexOf("\""), line.lastIndexOf("\""));
 					thisState.setInstructionFile(insFile);
 				}
 				else if(line.contains("CONDITIONS"))
 				{
 					String valConditions = line.substring(line.indexOf("\""), line.lastIndexOf("\""));
 					thisState.setValidConditions(valConditions);

 				}
 				else if(line.contains("END"))
 				{
 					if(line.contains("On Click Continue"))
 					{
 						thisState.setChangeStateAction(PCLE_State.CLICK_CONTINUE);
 					}
 				
 				}
 				// Check for options in "Rate Ease of Learning"
 				else if(line.contains("Rate Ease of Learning"))
 				{
 					thisState.setState(PCLE_State.RATE_EASE_OF_LEARNING);
 				}
 				// Check for options in "Estimate Number to Remember Pretrial"
 				else if(line.contains("Estimate Number to Remember Pretrial"))
 				{
 					thisState.setState(PCLE_State.ESTIMATE_NUMBER_TO_REMEMBER_PRE);
 				}
 				// Check for options in "Estimate Number to Remember Posttrial"
 				else if(line.contains("Estimate Number to Remember Posttrial"))
 				{
 					thisState.setState(PCLE_State.ESTIMATE_NUMBER_TO_REMEMBER_POST);
 				}
 				// Check for options in "Estimate Number Correct"
 				else if(line.contains("Estimate Number Correct"))
 				{
 					thisState.setState(PCLE_State.ESTIMATE_NUMBER_CORRECT);
 				}
 				// Check for options in "Learning Phase"
 				else if(line.contains("Learning Phase"))
 				{
 					thisState.setState(PCLE_State.LEARNING);
 				}
 				// Check for options in "Testing Phase"
 				else if(line.contains("Testing Phase"))
 				{
 					thisState.setState(PCLE_State.TESTING);
 				}
 				else if(line.contains("/STATE"))
 				{
 					// Add this one to the vector
 					m_vExpStates.add(thisState);
 				}
 				else if((line.contains("STUDY_TIMES")) && (!line.contains("/")))
 				{
 					// Get the number of study times to write out to report
 					line = bufReader.readLine().trim();
 					m_iMaxStudyTimes = Integer.parseInt(line);
 				}
 
 			}
 		}
 		catch(IOException e)
 		{
 			JOptionPane.showMessageDialog(this, 
 					"Error: Unable to read " + m_sExpFileName, 
 					"Error Reading Experiment File", JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 		// Build vectors of images
 		//    Images  1-12 --- Easy
 		//    Images 13-24 --- Medium
 		//    Images 25-36 --- Hard
 		// Each trial has 18 images in 2 groups of 9
 		// Each group has 3 from easy, 3 from medium, 3 from hard
 		//    in a random arrangement on the grid
 		// 1. Init 3 arrays of ints
 		//	 	1  2  3  4  5  6  7  8  9  10 11 12
 		//      13 14 15 16 17 18 19 20 21 22 23 24
 		//		25 26 27 28 29 30 31 32 33 34 35 36
 		// 2. For each of 2 sets of nine
 		//     a. Randomly select 3 easy indices, 3 medium indices, and 3 hard indices
 		//     b. Move all indices over to remove the one selected and decrement # in the array
 		//
 		
 		// Create arrays of images for easy, medium, hard
 		int[] easy = new int[12];
 		int[] medium = new int[12];
 		int[] hard = new int[12];
 		int[] value = new int[9];
 		int relativeCondition = m_iCondition;
 		if(m_iCondition>4) relativeCondition -= 4;
 		
 		// Set the indices in each
 		for(int i=0; i<12; i++)
 		{
 			easy[i] = i;
 			medium[i] = i + 12;
 			hard[i] = i + 24;
 		}
 		
 		int countE = 12; // Count of indices left in the array
 		int countM = 12; // Count of indices left in the array
 		int countH = 12; // Count of indices left in the array
 		
 		int E1, E2, E3, M1, M2, M3, H1, H2, H3;
 		int idx;
 		
 		// For each of six sets
 		for(int i=0; i<4; i++)
 		{
 			// Pick the array for this iteration
 			int[] theArray = null;
 			switch(i)
 			{
 				case 0 : theArray = m_iTrial1Group1; break;
 				case 1 : theArray = m_iTrial1Group2; break;
 				case 2 : theArray = m_iTrial2Group1; break;
 				case 3 : theArray = m_iTrial2Group2; break;
 			}
 			// Pick 3 from easy group
 			idx = (int)(Math.floor(Math.random() * countE));
 			E1 = easy[idx];
 			countE--;
 			for(int e=idx; e<countE; e++) easy[e] = easy[e+1];
 			idx = (int)(Math.floor(Math.random() * countE));
 			E2 = easy[idx];
 			countE--;
 			for(int e=idx; e<countE; e++) easy[e] = easy[e+1];
 			idx = (int)(Math.floor(Math.random() * countE));
 			E3 = easy[idx];
 			countE--;
 			for(int e=idx; e<countE; e++) easy[e] = easy[e+1];
 			
 			// Pick 2 from medium group
 			idx = (int)(Math.floor(Math.random() * countM));
 			M1 = medium[idx];
 			countM--;
 			for(int m=idx; m<countM; m++) medium[m] = medium[m+1];
 			idx = (int)(Math.floor(Math.random() * countM));
 			M2 = medium[idx];
 			countM--;
 			for(int m=idx; m<countM; m++) medium[m] = medium[m+1];
 			idx = (int)(Math.floor(Math.random() * countM));
 			M3 = medium[idx];
 			countM--;
 			for(int m=idx; m<countM; m++) medium[m] = medium[m+1];
 
 			// Pick 2 from hard group
 			idx = (int)(Math.floor(Math.random() * countH));
 			H1 = hard[idx];
 			countH--;
 			for(int h=idx; h<countH; h++) hard[h] = hard[h+1];
 			
 			idx = (int)(Math.floor(Math.random() * countH));
 			H2 = hard[idx];
 			countH--;
 			for(int h=idx; h<countH; h++) hard[h] = hard[h+1];
 			
 			idx = (int)(Math.floor(Math.random() * countH));
 			H3 = hard[idx];
 			countH--;
 			for(int h=idx; h<countH; h++) hard[h] = hard[h+1];
 			
 			// We'll arrange the images as follows:
 			//	Conditions 1&3				Conditions 2&4
 			//    Even # :  E M H     Odd # : H M E
 			//              E M H             H M E
 			//				E M H			  H M E
 			//	Conditions 3&4				Conditions 2&1 
 			//				8 4 2 			  2 4 8
 			//				8 4 2			  2 4 8
 			//				8 4 2			  2 4 8
 			
 			value[1] = value[4] = value[7] = 4;
 		
 			int lval = (relativeCondition<3)?2:8;
 			int rval = (relativeCondition<3)?8:2;
 
 			value[0] = value[3] = value[6] = lval;
 			value[2] = value[5] = value[8] = rval;
 			
 			if(((relativeCondition-1) % 2) == 0)
 			{
 				// Even number
 				theArray[0] = E1;
 				theArray[1] = M1;
 				theArray[2] = H1;
 				theArray[3] = E2;
 				theArray[4] = M2;
 				theArray[5] = H2;
 				theArray[6] = E3;
 				theArray[7] = M3;
 				theArray[8] = H3;
 			}
 			else
 			{
 				// Odd number
 				theArray[0] = H1;
 				theArray[1] = M1;
 				theArray[2] = E1;
 				theArray[3] = H2;
 				theArray[4] = M2;
 				theArray[5] = E2;
 				theArray[6] = H3;
 				theArray[7] = M3;
 				theArray[8] = E3;
 			}
 			
 //			System.out.println("Array " + i);
 //			System.out.println("\t" + theArray[0] + " " + theArray[1] + " " + theArray[2]);
 //			System.out.println("\t" + theArray[3] + " " + theArray[4] + " " + theArray[5]);
 		}
 		
 		
 		// Create a unique report file name using the time
 		// Note: Calendar months are numbered from 0 so add 1 to Calendar.MONTH
 		m_ExpCalendar = Calendar.getInstance();
 		int hr = m_ExpCalendar.get(Calendar.HOUR_OF_DAY);
 		int min = m_ExpCalendar.get(Calendar.MINUTE);
 		int sec = m_ExpCalendar.get(Calendar.SECOND);
 		int day = m_ExpCalendar.get(Calendar.DAY_OF_MONTH);
 		int month = m_ExpCalendar.get(Calendar.MONTH)+1; // Calendar months are numbered from 0 
 		int year = m_ExpCalendar.get(Calendar.YEAR);
 		String condition = "UNK";
 		
 		switch (m_iCondition) {
 		case 1:
 			condition = "AN";
 			break;
 		case 2:
 			condition = "BN";
 			break;
 		case 3:
 			condition = "CN";
 			break;
 		case 4:
 			condition = "DN";
 			break;
 		case 5:
 			condition = "AF";
 			break;
 		case 6:
 			condition = "BF";
 			break;
 		case 7:
 			condition = "CF";
 			break;
 		case 8:
 			condition = "DF";
 			break;
 		}
 		
 		// Create an image file name
 		String fileName = new String("PCLE_Subject_Report_"
 				+ condition + "_SID" + m_sSubjectID
                 + "_" + ((day<10)?("0"+String.valueOf(day)):String.valueOf(day))
                 + ((month<10)?("0"+String.valueOf(month)):String.valueOf(month))
                 + year + ".txt");
 		try
 		{
 			// Open the report file
 			m_ReportFile = new File(fileName);
 			// Place it in the FileWriter
 			m_FileWriter = new FileWriter(m_ReportFile);
 			// Place that in the BufferedWriter
 			m_BufWriter = new BufferedWriter(m_FileWriter);
 			// Get the experiment start time in milliseconds
 			m_lStartTimeMillis = System.currentTimeMillis();
 			
 			// Save and write some initial time data
 			// Create time string as HH:MM:SS
 			m_sExpTimeStr = 
 				((hr<10)?("0"+String.valueOf(hr)):String.valueOf(hr)) + ":" +
 				((min<10)?("0"+String.valueOf(min)):String.valueOf(min)) + ":" +
 				((sec<10)?("0"+String.valueOf(sec)):String.valueOf(sec));
 			// Create date string as DOW Month Date Year "EEEE MMMM dd yyyy"
 			SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.FULL);
 			sdf.applyPattern("EEEE MMMM dd yyyy");
 			m_sExpDateStr = sdf.format(m_ExpCalendar.getTime());
 			postStatusMessage("Subject Test Time: " + 
 					m_ExpCalendar.getTime().toString() + "\n", false);
 		}
 		catch(IOException ex)
 		{
 			JOptionPane.showMessageDialog(this, 
 					"Error: Unable to write to report file.", 
 					"Error Writing Report File", JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 		
 		// Record the setup for this experiment
 		postStatusMessage("Setup for this run: ", false);
 		postStatusMessage("\tDisplay arrangement for each trial will be top row (l to r), bottom row (l to r) ", false);
 		Vector iVec = this.m_ImageFactory.getImagesVector();
 		// For each of six sets
 		for(int i=0; i<4; i++)
 		{
 			// Pick the array for this iteration
 			int[] theArray = null;
 			switch(i)
 			{
 				case 0 : 
 					theArray = m_iTrial1Group1; 
 					postStatusMessage("\tTrial 1, Set 1", false);
 					break;
 				case 1 : 
 					theArray = m_iTrial1Group2; 
 					postStatusMessage("\tTrial 1, Set 2", false);
 					break;
 				case 2 : 
 					theArray = m_iTrial2Group1; 
 					postStatusMessage("\tTrial 2, Set 1", false);
 					break;
 				case 3 : 
 					theArray = m_iTrial2Group2; 
 					postStatusMessage("\tTrial 2, Set 2", false);
 					break;
 			}
 			line = "\t\t";
 			
 			for(int j=0; j<9; j++)
 			{
 				
 				PCLE_Image theImg = (PCLE_Image)iVec.elementAt(theArray[j]);
 				if(j < 8)
 					line = line.concat(theImg.getReferenceName() + ", ");
 				else
 					line = line.concat(theImg.getReferenceName());
 				
 				// Set the presentation order and location of this image
 				theImg.setEOLPresentationOrder((i*9) + j);
 				// Save this so we do them in this order
 				m_iEOLOrder[(i*9) + j] = theArray[j];
 				theImg.setTrial((i / 2) + 1);
 				theImg.setGridLocation(((i % 2) + 1), ((j / 3) + 1), ((j % 3) + 1));
 				theImg.setImageValue(value[j]);
 			}
 			postStatusMessage(line, false);
 		}		
 		postStatusMessage("\n", false);
 		line = "Max number of times to study each character: " +
 				String.valueOf(m_iMaxStudyTimes);
 		postStatusMessage(line, false);
 		postStatusMessage("\n", false);
 		
 		// Give this presentation order for EOL to the Image panel
 		m_ImagePanel.setEOLPresentationOrder(m_iEOLOrder);
 		
 		// Set to record experiment events
 		postStatusMessage("Exp.Time - Experiment Event", false);
 		postStatusMessage("--------------------------------------------------------------", false);
 		return true;
 	}
 	
 	//--------------------------------------------------------------------
 	/** Post a message to the report file
 	 *  @param msg - String to write out to file
 	 *  @param giveTime - Boolean indicating if time should be output also
 	 */
 	//--------------------------------------------------------------------
 	public void postStatusMessage(String msg, boolean giveTime)
 	{
 		String line;
 		try
 		{
 			if(giveTime)
 			{
 				// Add time hack in front of string
 				line = timeToString() + msg + "\n";
 			}
 			else
 			{
 				line = msg + "\n";
 			}
 			m_BufWriter.write(line, 0, line.length());
 			m_BufWriter.flush();
 		}
 		catch(IOException ex)
 		{
 			JOptionPane.showMessageDialog(this, 
 					"Error: Unable to write to report file.", 
 					"Error Writing Report File", JOptionPane.ERROR_MESSAGE);
 		}
 		
 	}
 	
 	//-----------------------------------------------
 	/** Set the next state */
 	//-----------------------------------------------
 	public void setNextState()
 	{
 		String sCondition;
 		switch (m_iCondition) {
 		case 1:
 			sCondition = "AN";
 			break;
 		case 2:
 			sCondition = "BN";
 			break;
 		case 3:
 			sCondition = "CN";
 			break;
 		case 4:
 			sCondition = "DN";
 			break;
 		case 5:
 			sCondition = "AF";
 			break;
 		case 6:
 			sCondition = "BF";
 			break;
 		case 7:
 			sCondition = "CF";
 			break;
 		case 8:
 			sCondition = "DF";
 			break;
 		default:
 			sCondition = "UnSet";
 			break;
 		}
 		m_iCurStateIdx++;
 		
 		// See if we are done and if so write out all the data in 
 		// comma separated format.
 		if(m_iCurStateIdx >= m_vExpStates.size())
 		{
 			try
 			{
 				String line;
 				// Post headings for first section
 				line = "\n\nSubject ID,Condition,Run Date,Run Time,Total Exp Time," +
 					   "Test 1 Pretrial,Test 1 Posttrial,Test 1 Postdict," +
 				   "Test 2 Pretrial,Test 2 Posttrial,Test 2 Postdict";
 				postStatusMessage(line, false);
 				String estStr = "";
 				// Build the estimates string
 
 				for(int i=0; i<6; i++)
 				{
 					estStr = estStr.concat("," + String.valueOf(m_iUserEstimates[i]));
 				}
 				
 				// Get the total experiment time
 				line = 	m_sSubjectID + "," + 
 						sCondition + "," +
 						m_sExpDateStr  + "," +
 						m_sExpTimeStr  + "," +
 						timeToString() + 
 						estStr;
 				postStatusMessage(line, false);
 				// Post headings for second section
 				line = "\nSID,Word number,English,EOL Order," +
 					"EOL Rate,Test Order,User Answer,Correct(T/F),Answer Value,Trial,Set,Grid Row," +
 					"Grid Column,Times Studied,Total Study Time";
 				String stStr = "";
 				for(int i=0; i<m_iMaxStudyTimes; i++)
 				{
 					stStr = stStr.concat(",Study Time " + String.valueOf(i+1) +
 							",Study Order " + String.valueOf(i+1));
 				}
 				line = line.concat(stStr);
 				postStatusMessage(line, false);
 				// Write out all the information on this run
 				// postStatusMessage for the column headings
 				Vector vec = this.m_ImageFactory.getImagesVector();
 				for(int i=0; i<36; i++) // Only do the exp images
 				{
 					PCLE_Image img = (PCLE_Image)vec.elementAt(i);
 					// Build the study string (up to 5 times allowed)
 					Vector stVec = img.getStudyTimeVector();
 					Vector soVec = img.getStudyOrderVector();
 					String studyStr = "";
 					for(int j=0; j<m_iMaxStudyTimes; j++)
 					{
 						if(j < stVec.size())
 						{
 							Integer tInt = (Integer)(stVec.elementAt(j));
 							Integer oInt = (Integer)(soVec.elementAt(j));
 							studyStr = studyStr.concat("," + String.valueOf(tInt.intValue()) + 
 									 "," + String.valueOf(oInt.intValue()));
 						}
 						else
 						{
 							studyStr = studyStr.concat(",0,0");
 						}
 					}
 					postStatusMessage(m_sSubjectID + "," +
 							String.valueOf(i) + "," +
 							img.getReferenceName() + "," +
 							String.valueOf(img.getEOLPresentationOrder()) + "," +
 							String.valueOf(img.getEOLRate()) + "," +
 							String.valueOf(img.getTestPresentationOrder()) + "," +
 							img.getUserAnswer() + "," +
 							String.valueOf(img.getUserAnswerCorrect()) + "," +
 							String.valueOf(img.getImageValue()) + "," +
 							String.valueOf(img.getTrial()) + "," +
 							String.valueOf(img.getGrid()) + "," +
 							String.valueOf(img.getRow()) + "," +
 							String.valueOf(img.getColumn()) + "," +
 							String.valueOf(img.getTimesStudied()) + "," +
 							String.valueOf(img.getTotalStudyTime()) +
 							studyStr
 							, false);
 				}
 				postStatusMessage("\n\n", false);
 				postStatusMessage(" - End of experiment.", true);
 				m_BufWriter.close(); // Close the file
 			}
 			catch(Exception e)
 			{}
 			// Terminate the experiment
 			System.exit(0);
 		}
 		m_LastState = m_CurState;
 		m_CurState = (PCLE_State)m_vExpStates.elementAt(m_iCurStateIdx);
 		if (!m_CurState.validCondition(sCondition)) {
 			this.setNextState();
 			return;
 		}
 		
		if (m_LastState.getState() == PCLE_State.SHOW_POINT_NOTIFICATION) {
 			int numCorrect = -1;
 			int actualNumCorrect = getUserPoints(m_iTrialNumber);
 			
 			do {
 				JOptionPane.showMessageDialog(this, "You have earned " + actualNumCorrect + " point(s)!",
 						"Points Earned", JOptionPane.INFORMATION_MESSAGE);
 				String userInput = JOptionPane.showInputDialog("Please enter the number of points earned:");
 				
 				if (userInput == null || userInput == "")
 					continue;
 				
 				try {
 					numCorrect = Integer.parseInt(userInput);
 				} catch(Exception ex) { continue; }
 			}
 			while(numCorrect != actualNumCorrect);
 		}
 		
 		switch(m_CurState.getState())
 		{
 			case PCLE_State.SHOW_INSTRUCTIONS :
 			case PCLE_State.SHOW_POINT_NOTIFICATION :
 				if((m_LastState != null) && 
 					(m_LastState.getState() != PCLE_State.SHOW_INSTRUCTIONS &&
 					 m_LastState.getState() != PCLE_State.SHOW_POINT_NOTIFICATION))
 				{
 					m_InstructionsPan.setVisible(true);
 					m_ContinueButton.setVisible(true);
 					m_ImagePanel.setVisible(false);
 				}
 				postStatusMessage(" - Displaying instructions: " + m_CurState.getInstructionFile(), true);
 				//This must be done to get around glitch which causes the first page
 				//to duplicate itself... several times...
 				m_InstructionsPan.setVisible(false);
 				m_InstructionsPan.showInstructions(m_CurState.getInstructionFile());
 				m_InstructionsPan.setVisible(true);
 				break;
 			case PCLE_State.RATE_EASE_OF_LEARNING :
 				m_InstructionsPan.setVisible(false);
 				m_ContinueButton.setVisible(false);
 				m_ImagePanel.setVisible(true);
 				paint(getGraphics());
 				postStatusMessage(" - Rating ease of learning.", true);
 				m_ImagePanel.rateEaseOfLearning();
 //				m_InstructionsPan.setVisible(true);
 //				m_ContinueButton.setVisible(true);
 //				m_ImagePanel.setVisible(false);
 				break;
 			case PCLE_State.ESTIMATE_NUMBER_TO_REMEMBER_PRE :
 				m_InstructionsPan.setVisible(false);
 				m_ContinueButton.setVisible(false);
 				m_ImagePanel.setVisible(true);
 				paint(getGraphics());
 				postStatusMessage(" - Estimating number to remember pretrial.", true);
 				m_ImagePanel.estimateNumber(m_CurState.getInstructionFile(),
 						PCLE_State.ESTIMATE_NUMBER_TO_REMEMBER_PRE);
 				break;
 			case PCLE_State.ESTIMATE_NUMBER_TO_REMEMBER_POST :
 				m_InstructionsPan.setVisible(false);
 				m_ContinueButton.setVisible(false);
 				m_ImagePanel.setVisible(true);
 				paint(getGraphics());
 				postStatusMessage(" - Estimating number to remember posttrial.", true);
 				m_ImagePanel.estimateNumber(m_CurState.getInstructionFile(),
 						PCLE_State.ESTIMATE_NUMBER_TO_REMEMBER_POST);
 				break;
 			case PCLE_State.ESTIMATE_NUMBER_CORRECT :
 				m_InstructionsPan.setVisible(false);
 				m_ContinueButton.setVisible(false);
 				m_ImagePanel.setVisible(true);
 				paint(getGraphics());
 				postStatusMessage(" - Estimating number correct.", true);
 				m_ImagePanel.estimateNumber(m_CurState.getInstructionFile(),
 						PCLE_State.ESTIMATE_NUMBER_CORRECT);
 				break;
 			case PCLE_State.LEARNING :
 				m_iTrialNumber++;
 				m_InstructionsPan.setVisible(false);
 				m_ContinueButton.setVisible(false);
 				m_ImagePanel.setVisible(true);
 				paint(getGraphics());
 				postStatusMessage(" - Begin study phase.", true);
 				if(m_iTrialNumber == 1)
 					m_ImagePanel.doLearning(m_iTrial1Group1, m_iTrial1Group2);
 				else
 					m_ImagePanel.doLearning(m_iTrial2Group1, m_iTrial2Group2);
 				break;
 			case PCLE_State.TESTING :
 				//set up panels
 				m_InstructionsPan.setVisible(false);
 				m_ContinueButton.setVisible(false);
 				m_ImagePanel.setVisible(true);
 				paint(getGraphics());
 				postStatusMessage(" - Begin testing phase.", true);
 				if(m_iTrialNumber == 1)
 					m_ImagePanel.doTesting(1, m_iTrial1Group1, m_iTrial1Group2);
 				else
 					m_ImagePanel.doTesting(2, m_iTrial2Group1, m_iTrial2Group2);
 				break;
 		}
 	}
 
 
 	//----------------------------------------------------------
 	/** Set a user estimate.
 	 *  @param trial = trial number (1 or 2)
 	 *  @param guessAt = estimate for pre(0), posttrial(1), postdict(2)
 	 *  @param guess = what the estimate is
 	 */
 	//----------------------------------------------------------
 	public void saveUserEstimate(int trial, int guessAt, int guess)
 	{
 		int idx;
 		if(trial == 1) idx = 0;
 		else idx = 3;
 		if(guessAt == 1) idx++; // increment to second position
 		if(guessAt == 2) idx+=2; // increment to third position
 		// Save the estimate
 		m_iUserEstimates[idx] = guess;
 	}
 	
 	//----------------------------------------------------------
 	/** Set a user points.
 	 *  @param trial = trial number (1 or 2)
 	 *  @param points = # points
 	 */
 	//----------------------------------------------------------
 	public void saveUserPoints(int trial, int points)
 	{
 		if (trial > 2 || trial < 1)
 			return;
 		// Save the estimate
 		m_iUserPoints[trial-1] = points;
 	}
 
 	//----------------------------------------------------------
 	/** Get a user points.
 	 *  @param trial = trial number (1 or 2)
 	 *  @return points
 	 */
 	//----------------------------------------------------------
 	public int getUserPoints(int trial)
 	{
 		if (trial > 2 || trial < 1)
 			return -1;
 		// Save the estimate
 		return m_iUserPoints[trial-1];
 	}
 	
 
 	//----------------------------------------------------------
 	/** Return the current condition */
 	//----------------------------------------------------------
 	public int getCondition() {
 		return m_iCondition;
 	}
 	
 	//----------------------------------------------------------
 	/** Convert a time in milliseconds to a string giving
 	 *  the time since the start of the experiment
 	 */
 	//----------------------------------------------------------
 	private String timeToString()
 	{
 		long currentTimeMillis = System.currentTimeMillis();
 		double tDiffSec = (double)(currentTimeMillis - m_lStartTimeMillis) / 1000.0;
 		double hours = tDiffSec / 3600.0;
 		double minutes = (hours - Math.floor(hours)) * 60.0;
 		double seconds = (minutes - Math.floor(minutes)) * 60.0;
 
 		int h, m, s;
 		h = (int)(Math.floor(hours));
 		m = (int)(Math.floor(minutes));
 		s = (int)(Math.round(seconds));
 
 		String tStr = new String("");
 		if(h < 10)
 			tStr = tStr.concat("0" + String.valueOf(h) + ":");
 		else
 			tStr = tStr.concat(String.valueOf(h) + ":");
 		if(m < 10)
 			tStr = tStr.concat("0" + String.valueOf(m) + ":");
 		else
 			tStr = tStr.concat(String.valueOf(m) + ":");
 		if(s < 10)
 			tStr = tStr.concat("0" + String.valueOf(s));
 		else
 			tStr = tStr.concat(String.valueOf(s));
 
 		return tStr;
 	}
 	
 	//-----------------------------------------------
 	/** Everything states in main */
 	//-----------------------------------------------
 	public static void main(String[] args)
 	{
 		PriceCLE theApp = new PriceCLE();
 	}
 
 }
