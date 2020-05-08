 package levelDesigner;
 
 /*
  * Add in different combo boxes based on object
  * 		terrain
  * 		object
  * 		living thing
  * 
  * Add in option to change rows/columns
  * 
  * Add in ability to place objects on top of others
  */
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Scanner;
 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 @SuppressWarnings("serial")
 public class LevelDesigner extends JPanel implements ButtonListener{
 	private final int SIZE = 12;
 	private Grid[][] theView;
 	private ButtonPanel bp;
 	public static int val;
 	private int rows, cols;
 	private JPanel gridPanel;
 	private JFrame frame;
 	private ImageIcon optionPaneIcon;
 	String[] selectedTilesToPaint = {"dirt.png", "grass.png", "stoneWall.png", "Void.png", "woodFloorDark.png",
             "woodFloorLight.png", "woodFloorMedium.png", "woodFloorRed.png", "bed.png", "bedWithCat.png",
             "chairLeftFacing.png", "chairRightFacing.png", "characterArmor.png", "characterNoArmor.png",
             "characterSwordAndShield.png", "Chest.png", "doorInsideToInside.png", "doorInsideToOutside.png",
            "doorOutsideToInside.png", "Enemy.png", "epicCrawlerIcon.png", "EpicCrawlTitle.png", "Girl.png",
             "Portal.png", "rock.png", "table.png", "tableLong.png", "Tablewithfood.png", "tree.png",
            "treeAndShrubNorSBorder", "++Save Level++"};
     private int paintBrushSize = 0; // 0 Indicates one tile to be painted
 
 	public LevelDesigner(int r, int c){
 		frame = new JFrame();
 		setVisible(true);
 		setLayout(new BorderLayout());
 
 		BufferedImage imageToPaint = null;
 		InputStream input = this.getClass().getClassLoader().getResourceAsStream("Images/64x64EpicCrawler.png");
 		try{
 			imageToPaint = ImageIO.read(input);
 		}
 		catch(IOException e){System.err.println("Failed to load main screen image");}
 		optionPaneIcon = new ImageIcon(imageToPaint);
 
 		rows = r;
 		cols = c;
 		theView = new Grid[r][c];   
 		
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.setColumns(cols);
 		gridLayout.setRows(rows);
 
 		gridPanel = new JPanel();
 		gridPanel.setVisible(true);
 		gridPanel.setBackground(Color.BLACK);
 		gridPanel.setPreferredSize(getMaximumSize());
 		gridPanel.setLayout(gridLayout);
 
 		for(int i = 0; i < r; i++){
 			for(int j = 0; j < c; j++){
 				theView[i][j] = new Grid(i, j, SIZE, selectedTilesToPaint);
 				gridPanel.add(theView[i][j]);
 			}
 		}
 
 		bp = new ButtonPanel(selectedTilesToPaint, this);
 
 		this.add(bp, BorderLayout.SOUTH);
 		this.add(gridPanel, BorderLayout.CENTER);
 
 		frame.addMouseListener(new MouseAdapter(){
 			public void mousePressed(MouseEvent me) {
 
 				for(int i = 0; i < rows; ++i){
 					for(int j = 0; j < cols; ++j){
 						Point pointOfClick = SwingUtilities.convertPoint(frame, me.getPoint(), theView[i][j]);  //Top left of square
 						if (theView[i][j].contains(pointOfClick)){
                             for(int colClick = 0; colClick <= paintBrushSize; colClick++){
 							    for(int rowClick = 0; rowClick <= paintBrushSize; rowClick++){
                                     if((i+rowClick < rows) && (j+colClick < cols)){
                                         theView[i+rowClick][j+colClick].actionPerformed(null);
                                     }
                                 }
                             }
 							return;
 						}
 					}
 				}
 			} 
 		});
 
 		frame.addMouseMotionListener(new MouseMotionAdapter(){
 			public void mouseDragged(MouseEvent me) {
 
 				for(int i = 0; i < rows; ++i){
 					for(int j = 0; j < cols; ++j){
 						Point pointOfClickDrag = SwingUtilities.convertPoint(frame, me.getPoint(), theView[i][j]);
 
 						if (theView[i][j].contains(pointOfClickDrag)){
                             for(int colClick = 0; colClick <= paintBrushSize; colClick++){
                                 for(int rowClick = 0; rowClick <= paintBrushSize; rowClick++){
                                     if((i+rowClick < rows) && (j+colClick < cols)){
                                         theView[i+rowClick][j+colClick].actionPerformed(null);
                                     }
                                 }
                             }
                             return;
 						}
 					}
 				}
 			} 
 		});
 
 		Dimension dim = new Dimension((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(), 
 				(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 30);
 		frame.setPreferredSize(dim);
 		frame.setVisible(true);
 		frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
 		frame.setTitle("Epic Crawl - Main Menu");
 		frame.pack();
 		frame.setLocationRelativeTo(null);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close on exit
 		frame.repaint();
 		frame.add(this);
 	}
 	
 
     //Increases the paintBrushSize by 1
     //Resetting it to 1, if it gets bigger than 10...
     public void increasePaintBrushSize(){
         this.paintBrushSize += 1;
 
         if(this.getPaintBrushSize() > 10){
             this.paintBrushSize = 0;
         }
     }
     
 
     public int getPaintBrushSize(){
         return this.paintBrushSize;
     }
     
 
 	public String toString(){
 		String s = "";
 		for(int r = 0; r < rows; r++){
 			for(int c = 0; c < cols; c++){
 				for(String imageName: theView[r][c].gridImageNames){
 					if(imageName.equals("null"))
 						s = s + "Void.png,";
 					else
 						s = s + imageName + ",";
 				}
 				
 				s = s.substring(0, s.length() - 1) + " ";
 			}
 
 			s = s + "\n";
 		}
 
 		return s;
 	}
 	
 
 	public void saveGame(){
 		String levelName = (String)JOptionPane.showInputDialog(this, 
 				"Enter level name.", "Save level", JOptionPane.INFORMATION_MESSAGE, optionPaneIcon, null, null);
 
 		levelName += ".txt";
 
 		PrintWriter p = null;
 		try{
 			p = new PrintWriter(new File(levelName));
 		} catch (FileNotFoundException e1){}
 		p.println( this );
 		p.close();   
 
 		try{
 			BufferedWriter bufWriter = new BufferedWriter(new FileWriter("LevelsList.txt", true));
 			bufWriter.write("\n" + levelName);
 			bufWriter.newLine();
 			bufWriter.flush();
 			bufWriter.close();
 		}
 		catch(IOException e){System.out.println("Error saving level to LevelsList.txt");}
 	}
 
 	public void loadGame(){
 		ArrayList<String> savedLevelsList = new ArrayList<String>();
 		try{
 			BufferedReader bufReader = new BufferedReader(new FileReader("LevelsList.txt"));
 			String curLine = "";
 
 			while((curLine = bufReader.readLine()) != null){
 				if(!curLine.equals(""))
 					savedLevelsList.add(curLine);
 			}
 
 			bufReader.close();
 		}
 		catch(Exception e){
 			System.err.println("Can't open file to load levels, " + e);
 		}
 
 		String[]savedLevels = new String[savedLevelsList.size()];
 		for(int i = 0; i < savedLevelsList.size(); ++i){
 			savedLevels[i] = savedLevelsList.get(i);
 		}
 
 		String levelFileName = (String)JOptionPane.showInputDialog(this,
 				"Select level to load", "Load Level", JOptionPane.INFORMATION_MESSAGE, optionPaneIcon, savedLevels, null);
 		
 		FileReader fileReader = null;
 		try{
 			fileReader = new FileReader(levelFileName);
 		} 
 		catch(FileNotFoundException e1){}
 
 		Scanner lineScanner = new Scanner(fileReader);
 		Scanner wordScanner = null;
 		int curRow = 0, curCol = 0;
 		String curString = "";
 
 		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 		
 		while(lineScanner.hasNextLine()){
 			wordScanner = new Scanner(lineScanner.nextLine());
 
 			while(wordScanner.hasNext()){
 				curString = wordScanner.next();
 
 				ArrayList<String> wordList = new ArrayList<String>(Arrays.asList(curString.split(",")));
 
 				for(String cur: wordList){
 
 					InputStream input = this.getClass().getClassLoader().getResourceAsStream("Images/" + cur);
 					try{
 						theView[curRow][curCol].gridImages.add(ImageIO.read(input));
 						theView[curRow][curCol].gridImageNames.add(cur);
 					}
 					catch(Exception e){System.err.println("Failed to load image");}
 
 				}
 
 				++curCol;
 			}
 			
 			curCol = 0;
 			++curRow;
 		}
 
 		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 
 		lineScanner.close();
 		wordScanner.close();
 		repaint();
 	}
 
     //Increases the length of the square paint brush size by 1
     //If The size of the brush will reset back to 0, once it hits 10
     public int changePaintBrushSize(){
         increasePaintBrushSize();
         return this.getPaintBrushSize();
     }
 
 	public void buttonPressed(int id){
 		if(id == -1) // Save
 			saveGame();
 		else if(id == -2) // Load
 			loadGame();
         else if(id == -3) //Change paint brush size
             changePaintBrushSize();
 		else
 			val = id;
 	}
 
 	@SuppressWarnings("unused")
 	public static void main(String arg[]){ 
 		LevelDesigner levelDesigner = new LevelDesigner(50, 50);
 	}
 }
