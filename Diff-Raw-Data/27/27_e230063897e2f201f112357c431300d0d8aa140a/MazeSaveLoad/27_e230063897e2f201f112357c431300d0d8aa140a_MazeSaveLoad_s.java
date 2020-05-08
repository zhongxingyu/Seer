 package mazeEscapeApp;
 
 import java.util.*;
 import java.awt.Color;
 import java.io.*;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 
 import org.jhotdraw.framework.FigureAttributeConstant;
 
 import mazeEscapeUtils.MazeFileFilter;
 
 public class MazeSaveLoad {
 	private ArrayList<String> mazeFile;                 //The ArrayList that holds the initial maze numberings
 	private ArrayList<GCellCoordinate> saveCells;       //The modified cells since the game began
 	private final JFileChooser fc = new JFileChooser(); //The file chooser for save/load purposes
 	private GCellArea currentlySelected;
 	private boolean isFirstClick;
 	private boolean reachedEndCell;
 	private boolean reset;
 	private boolean on;
 	private int levelPoints;
 	private int timePassed;
 	private int timeScore;
 	private int minSteps;
 	private int stepsTaken;
 	private String time;
 	private String count;
 	private GCellCoordinate currentlySelectedCoord;
 	private String difficulty;
 	
 	public MazeSaveLoad(ArrayList<String> maze)
 	{
 		//Instantiations
 		mazeFile = maze;
 		saveCells = new ArrayList<GCellCoordinate>();
 		
 		//Configure the file chooser
 		MazeFileFilter filter = new MazeFileFilter();
 		fc.addChoosableFileFilter(filter);
 		fc.setAcceptAllFileFilterUsed(false);
 		fc.setFileFilter(filter);
 	}
 	
 	public MazeSaveLoad()
 	{
 		//Instantiations
 		mazeFile = null;
 		saveCells = new ArrayList<GCellCoordinate>();
 		
 		//Configure the file chooser
 		MazeFileFilter filter = new MazeFileFilter();
 		fc.addChoosableFileFilter(filter);
 		fc.setAcceptAllFileFilterUsed(false);
 		fc.setFileFilter(filter);
 	}
 	
 	public ArrayList<GCellCoordinate> getCellArray()
 	{
 		return saveCells;
 	}
 	
 	public void setMazeFile(ArrayList<String> m)
 	{
 		mazeFile = m;
 	}
 	
 	public void saveGame(MazeEscape m)
 	{
 		currentlySelected=m.getCurrentlySelected();
 		isFirstClick=m.isFirstClick();
 		reachedEndCell=m.isReachedEndCell();
 		reset=m.getReset();
 		on=m.isOn();
 		levelPoints=m.getLevelPoints();
 		timePassed=m.getTimePassed();
 		timeScore=m.getTimeScore();
 		minSteps=m.getMinSteps();
 		stepsTaken=m.getStepsTaken();
 		time=m.getTime();
 		count=m.getCount();
 		difficulty=m.getDifficulty();
 		
 		saveGame(currentlySelected, isFirstClick, reachedEndCell, reset, on,
 				levelPoints, timePassed,timeScore, minSteps, stepsTaken, time, count, difficulty);
 	}
 	private void saveGame(GCellArea cS, boolean iFC, boolean rEC, boolean reset,
 			boolean on, int lvlPts, int tP, int tS, int mS, int sT, String t, String c, String diff)
 	{
 		//Have the user select the save location
 		fc.setSelectedFile(new File("MazeSave"));
 		int returnVal = fc.showSaveDialog(null);
 		
 		
 		if(returnVal == JFileChooser.APPROVE_OPTION)
 		{
 			//Add the .maze extension to the file
 			File file = fc.getSelectedFile();
			file = new File(file.getPath() + ".maze");
 			try {
 				//Write mazeFile and saveCells to the file
 				PrintWriter out = new PrintWriter(file);
 				
 				//Header
 				out.println("MazeEscape1 Save");
 				
 				//MazeFile
 				for(String s:mazeFile)
 				{
 					out.println(s);
 				}
 				
 				//Separator token
 				out.println("***===***");
 				
 				//SaveCells
 				for(GCellCoordinate gc:saveCells)
 				{
 					out.println(gc);
 				}
 				
 				//Separator
 				out.println("***===***");
 				
 				//Currently Selected
				out.println(new GCellCoordinate(cS));
 				
 				//time
 				out.println(t);
 				
 				//count
 				out.println(c);
 				
 				//difficulty
 				out.println(diff);
 				
 				//isFirstClick
 				out.println(iFC);
 				
 				//reachedEndCell
 				out.println(rEC);
 				
 				//reset
 				out.println(reset);
 				
 				//on
 				out.println(on);
 				
 				//levelPoints
 				out.println(lvlPts);
 				
 				//timePassed
 				out.println(tP);
 				
 				//timeScore
 				out.println(tS);
 				
 				//minSteps
 				out.println(mS);
 				
 				//stepsTaken
 				out.println(sT);
 				
 				
 				
 				out.close();
 			} catch (FileNotFoundException e) {
 				JOptionPane.showMessageDialog(null, "Could not write to that location. Please try again.");
 			}
 		}
 	}
 
 	public void loadGame(MazeEscape m)
 	{
 		System.out.println("=== Start Load Method ===");
 		
 		mazeFile=new ArrayList<String>();
 		saveCells=new ArrayList<GCellCoordinate>();
 		
 		int returnVal = fc.showOpenDialog(null);
 		
 		if(returnVal == JFileChooser.APPROVE_OPTION)
 		{
 			File file = fc.getSelectedFile();
 			
 			try
 			{
 				Scanner scan = new Scanner(file);
 				
 				if(scan.nextLine().equals("MazeEscape1 Save"))
 				{
 					boolean readingMazeFile = true;
 					boolean readingCoordinateFile=false;
 					
 					System.out.println("Import Maze Info");
 					//Import maze info
 					while(readingMazeFile && scan.hasNext())
 					{
 						String s = scan.nextLine();
 						if(s.equals("***===***"))
 						{
 							readingCoordinateFile=true;
 							readingMazeFile=false;
 						}
 						else
 						{
 							System.out.println(s);
 							mazeFile.add(s);
 						}
 					}
 					
 					//Import modified cells info
 					System.out.println("\nImport Modified Cells Info");
 					while(readingCoordinateFile && scan.hasNext())
 					{
 						String s = scan.nextLine();
 						if(s.equals("***===***"))
 						{
 							readingCoordinateFile=false;
 						}
 						else
 						{
 							System.out.println(s);
 							saveCells.add(new GCellCoordinate(s));
 						}
 					}
 					
 					//Import variable state info
 					System.out.println("\nStart Variable Import");
					currentlySelectedCoord = new GCellCoordinate(scan.nextLine());
 					time = scan.nextLine();
 					count = scan.nextLine();
 					difficulty = scan.nextLine();
 					isFirstClick = scan.nextBoolean();
 					reachedEndCell=scan.nextBoolean();
 					reset = scan.nextBoolean();
 					on = scan.nextBoolean();
 					levelPoints = scan.nextInt();
 					timePassed = scan.nextInt();
 					timeScore = scan.nextInt();
 					minSteps = scan.nextInt();
 					stepsTaken = scan.nextInt();
 					System.out.println("End Variable Import");
 					
 					scan.close();
 					
 					//updateMaze(m, gd);
 					
 					
 				}
 			}catch(FileNotFoundException e)
 			{
 				//Implement me!
 			}
 		}
 	}
 	
 	public void updateMaze(MazeEscape m, GUIDrawer gd)
 	{
 		System.out.println("Updating maze...");
 		gd.loadMaze(mazeFile, difficulty);
 		
 		System.out.println("Updating modified cells");
 		for(GCellCoordinate c:saveCells)
 		{
 			m.getgCellClickableArea()[c.getRow()][c.getCol()].setAttribute(FigureAttributeConstant.FILL_COLOR, Color.CYAN);
 		}
 		
 		System.out.println("Updating variables");
		currentlySelected = m.getgCellClickableArea()[currentlySelectedCoord.getRow()][currentlySelectedCoord.getCol()];
 		m.setCurrentlySelected(currentlySelected);
 		m.setFirstClick(isFirstClick);
 		m.setReachedEndCell(reachedEndCell);
 		m.setReset(reset);
 
 		m.displayTimeAndSteps();
 		m.setTimePassed(timePassed);
 		m.setLevelPoints(levelPoints);
 		m.setMinSteps(minSteps);
 		m.setStepsTaken(stepsTaken);
 		m.setTime(time);
 		m.setCount(count);
 		currentlySelected.drawLarry();
 		m.setOn(on);
 		
 		
 		m.view().repairDamage();
 	}
 	
 	public void resetMSL()
 	{
 		mazeFile = new ArrayList<String>();
 		saveCells = new ArrayList<GCellCoordinate>();
 	}
 }
