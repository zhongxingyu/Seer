 package imgMain;
 
 /**
 *@author Koro
 *@date 07/03/2012
 */
 
 import imgMain.controls.JImage;
 import imgMain.controls.JxMenuBar;
 
 import java.awt.BorderLayout;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.prefs.Preferences;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.DisplayMode;
 import java.awt.FileDialog;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.TransferHandler;
 
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 
 import listeners.Mouse;
 
 public class JxReader extends TransferHandler {
 
   private static final long serialVersionUID = 1L;
   public static final double VERSION = 1.0;
   public JScrollPane pane = new JScrollPane();
   public static JPanel panel = new JPanel(new BorderLayout());
  private static JLabel pnlImage = new JLabel();
   
   private static final String[] supportedExtensions = {".png", ".jpg", ".gif", "jpeg", ".psd", ".bmp", ".wbmp", ".webp"};
   
   private JxMenuBar jmb = new JxMenuBar(this);
 
   private ArrayList<JImage> jj = new ArrayList<JImage>();
   private static Insets dim2;
   private static int VerticalHeight, HorizontalWidth;
   private int intX, intY, count = 0;
   private static boolean boolFullscreen, OnTop;
   
   private static int sideInsets = 0, topInsets = 0, OSDistro, currentScreen = 0; //0 = Linux, 1 = Windows, 2 = Mac, etc.;
   
   private static GraphicsDevice[] gs = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
   private JFrame frame;
   private static Archiver archiver;
   private static StringHandler stringHandler;
   private static String curdir;
   public static Cursor blankCursor;
   public static Cursor defaultCursor;
   
   
   private static final String PREFERENCE_FILE = "jxreader/prefs/preferences";
   private Preferences fPrefs = Preferences.userRoot().node(PREFERENCE_FILE);
   
   	/**
   	 * TODO: Change this class into a JFrame? Use icon.png as default image? Add to menu bar?
   	 * 		 Jump to page button; isNumber, check against 0 and jj.size(); disable until images loaded
   	 * 		 About tab
   	 * 		 Always on top mode
   	 * 		 Commandline arguments
   	 * 		 Standard fullscreen mode
   	 * TODO: Windows read from folder
   	 * 		 Resize from fullscreen error
   	 * TODO: Proper escape from true fullscreen mode
   	 * TODO: Add .tar/.gz support
   	 * 		 Add BufferedImage resize
   	 * TODO: ctrl hotkeys
   	 * TODO: Generic read from disk mode
   	 * TODO: Scroll speed
   	 * TODO: Background color
   	 * 		 Hide menu bar
   	 * 		 Cursor timeout
   	 * 		 Wait cursor
   	 * 		 Borderless mode
   	 * 		 Onclick event
   	 * 		 Save settings (%userdir%)
   	 * 		 Zoom; combine with bufferedimage algorithm
   	 * TODO: Other image formats //webp? ico, tif/tiff, mng, etc.
   	 * TODO: JR icon (make new one)
   	 * */
   
 	public JxReader(){
 		
 		frame = newFrame(false);
 		OSDistro = distro();
 		archiver = new Archiver();
 		stringHandler = new StringHandler();
 		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
 		blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
 			    cursorImg, new Point(0, 0), "blank cursor");
 		defaultCursor = frame.getCursor();
 		
 		dim2 = Toolkit.getDefaultToolkit().getScreenInsets(gs[0].getDefaultConfiguration());
 		intX = gs[0].getDisplayMode().getWidth() - dim2.left - dim2.right;
 		intY = gs[0].getDisplayMode().getHeight() - dim2.bottom - dim2.top;
 		//setScreen(0);
 		
 		
 		newFrame(frame);
 		//frame.setIconImage(new ImageIcon(getClass().getResource("/images/icon.png")).getImage());
 		
 	    pane.setTransferHandler(this);
 		pane.getVerticalScrollBar().setUnitIncrement(120);
 		pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
 		pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		pane.getHorizontalScrollBar().setUnitIncrement(120);
 		pane.setLocation(0, 0);
 		pane.setDoubleBuffered(true);
 		panel.setDoubleBuffered(true);
 		pnlImage.setDoubleBuffered(true);
 		pane.setViewportView(panel);
 		
 		pnlImage.addMouseListener(new Mouse(this));
 		
 		pane.setBorder(BorderFactory.createEmptyBorder());
 		pane.setViewportBorder(BorderFactory.createEmptyBorder());
 		
 		
 		panel.setLocation(0, 0);
 		panel.setBackground(Color.black);
 		panel.add(pnlImage);
 		pnlImage.setLocation(0, 0);
 		pnlImage.setLayout(new BorderLayout());
 		pnlImage.setVisible(true);
 		pnlImage.setName("pnlImage");
 		pnlImage.addMouseMotionListener(new Mouse(this));
 
 		try{
 			loadPreferences();
 		}catch (Exception ex){
 			System.out.println(ex);
 		}
 		
 		//panel.setVisible(true);
 		//pane.setVisible(true);
 		frame.setVisible(true);
 
   }
   
 	public void exit(){
 		savePreferences();
 		System.exit(0);
 	}
 	
 	private void savePreferences(){
 	    System.out.println("Updating preferences.");
 	    fPrefs.putInt("screen", currentScreen);
 	    fPrefs.putInt("x", frame.getX());
 	    fPrefs.putInt("y", frame.getY());
 	    fPrefs.putBoolean("fullscreen", boolFullscreen);
 	    fPrefs.putBoolean("disk", !jmb.isRAMMode());
 	}
 	
 	private void loadPreferences() throws Exception{
 		if (gs.length > 1){
 			setScreen(fPrefs.getInt("screen", 0));
 			frame.setLocation(fPrefs.getInt("x", 0), fPrefs.getInt("y", 0));			
 		}
 		boolFullscreen = fPrefs.getBoolean("fullscreen", false);
 		if (boolFullscreen){
 			boolFullscreen = false;
 			fullScreen();
 		}
 		boolean disk = fPrefs.getBoolean("disk", false);
 		jmb.setRAMSelected(!disk);
 	}
 	
 	private static byte distro(){
 		if (System.getProperty("os.name").contains("Linux")){
 	  		return 0;
 	  	}else if(System.getProperty("os.name").contains("Windows")){
 	  		return 1;
 	  	}else if(System.getProperty("os.name").contains("Mac")){
 	  		return 2;
 	  	}else{
 	  		return 3;
 	  	}
   }
 	
 
 	
 	private void newFrame(JFrame frame){
 		frame.setFocusable(true);
 		frame.addKeyListener(new Hotkeys(this));
 		frame.setResizable(false);
 		frame.setUndecorated(true);
 		frame.setContentPane(pane);
 	    frame.setSize(intX / 2, intY / 2);
 	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setLocation(0,0);
 		frame.setMaximumSize(new Dimension(intX, intY));
 		frame.setMinimumSize(new Dimension(500, 400));
 		frame.setJMenuBar(jmb);
   }
   
 	private static JFrame newFrame(boolean fullScreen){
 		JFrame jFrame = new JFrame(gs[currentScreen].getDefaultConfiguration());
 		jFrame.setTitle("JxReader");
 		jFrame.createBufferStrategy(1);
 		//if (fullScreen){
 			jFrame.setUndecorated(true);
 			
 		//}
 		jFrame.setLayout(new BorderLayout());
 		return jFrame;
   }
   
 	public void setScreen(int i){
 		if (currentScreen == i){return;}
 		currentScreen = i;
 		dim2 = Toolkit.getDefaultToolkit().getScreenInsets(gs[i].getDefaultConfiguration());
 		intX = gs[i].getDisplayMode().getWidth() - dim2.left - dim2.right;
 		intY = gs[i].getDisplayMode().getHeight() - dim2.bottom - dim2.top;
 		boolFullscreen = true;
 		fullScreen();
 		boolean secondary = i == 1;
 		int curWidth = secondary ? gs[0].getDisplayMode().getWidth() : 0;
 		frame.setLocation(curWidth, 0);
 		jmb.setScreenPrimary(!secondary);
 	}
   
 	public void pageRouter(int i){
 		int size = jj.size();
 	  if (size > 0){
 		if (i == 1){
 			if (count < size - 1){count++;}
 			else{count = 0;}
 		} //Next
 		else if (i == 2){
 			if (0 < count){count--;}
 			else{count = size - 1;}
 		} //Previous
 		else if (i == 3){count = 0;} //First
 		else if (i == 4){count = size - 1;} //Last
 
 		flickCount();
 	  }
   }
 	
 	public void InfoBox(Object message, String title){
 		
 		frame.setAlwaysOnTop(false);
 		JOptionPane.showMessageDialog(frame, message, title, 1);
 		frame.setAlwaysOnTop(OnTop);
 		/**TODO: Add Icon*/
 	}
 	
 	private Object InputBox(Object message, String title){
 		frame.setAlwaysOnTop(false);
 		Object obj = JOptionPane.showInputDialog(frame, message, title, 1);
 		frame.setAlwaysOnTop(OnTop);
 		return obj;
 		/**TODO: Add Icon*/
 	}
 	
 	private void ErrorBox(Object message, String title){
 		frame.setAlwaysOnTop(false);
 		System.err.println(message);
 		JOptionPane.showMessageDialog(frame, message, title, 2);
 		frame.setAlwaysOnTop(OnTop);
 	}
 	
 	public void flickCount(){
 		
 		try{
 			pnlImage.removeAll();	
 			pane.getHorizontalScrollBar().setValue(0);
 			pane.getVerticalScrollBar().setValue(0);
 			setJRSize();
 			frame.validate();
 			
 			JLabel tempImage = zoom();
 			tempImage.validate();
 			tempImage.updateUI();
 			
 			pnlImage.add(tempImage);
 			pnlImage.validate();
 
 			
 			pnlImage.validate();
 			panel.updateUI();
 			if (boolFullscreen) {pnlImage.getComponent(0).setLocation((intX - jj.get(count).getWidth()) / 2, 0);}
 			else {pnlImage.getComponent(0).setLocation(0, 0);}
 			jmb.setLabelText("   Page " + (count + 1) + " of " + jj.size() + " - " + jj.get(count).getName());
 
 		}catch (Exception ex){
 			//InfoBox(ex.toString(), "flickCount error");
 			ErrorBox(ex.toString(), "flickCount error");
 		}
 		//frame.validate();
 		//Runtime runtime = Runtime.getRuntime();
 		//long usedMB = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
 		//MsgBox(usedMB);
 	}
   
 	public boolean canImport(JComponent dest, DataFlavor[] flavors) {return true;}
   
 	public boolean importData(JComponent src, Transferable transferable) {
 		if (jj.size() > 0){
 		  pnlImage.removeAll();
 		    jj.clear();
 		    count = 0;
 		}
 
 		DataFlavor[] flavors = transferable.getTransferDataFlavors();
 		DataFlavor bestTextFlavor = DataFlavor.selectBestTextFlavor(flavors);
 		DataFlavor listFlavor = null;
 		int lastFlavor = flavors.length - 1;
 	    
 	    try {
 	    	
 	    	if (OSDistro != 1){
 	    		
 	    		BufferedReader br = new BufferedReader(bestTextFlavor.getReaderForText(transferable)); //Set UTF-16?
 	    		String line;
 	    		File file;
 	    		frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 	    		pane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 	    		do{	    			
 		            try{
 		            	line = stringHandler.UnixPath(br.readLine());
 		            	file = new File(line);
 		            }catch (Exception ex){
 		            	break;
 		            }
 	    			
 		            processCommand(line, file);
 	            }while (line != null);
 	    	}else {
 	    		readWindowsImage(lastFlavor, flavors, listFlavor, transferable);
 	    	}
 	    	finishProcessing();
 	    }catch (Exception e) {
 	    	//InfoBox(e.toString(), "Load error");
 			ErrorBox(e.toString(), "Load error");
 	    	return false;
 	    }finally {
 	    	frame.setCursor(JxReader.defaultCursor);
 	    	pane.setCursor(JxReader.defaultCursor);
 		}
 	    return true;
 	  }
 	
 	private void finishProcessing(){
 		if (jj.size() == 0){return;}
     	sideInsets = 0;
     	topInsets = 0;
 
     	sort(jj);
     	flickCount();
     	pane.validate();
 	}
 	
 	private void readWindowsImage(int lastFlavor, DataFlavor[] flavors, DataFlavor listFlavor, Transferable transferable) throws IOException, UnsupportedFlavorException{
 		try{
     	    for (int f = 0; f <= lastFlavor; f++) {
     			if (flavors[f].isFlavorJavaFileListType()) {
     		    	listFlavor = flavors[f];
     			}
     		}
     	}catch (Exception e){
     		//InfoBox(e.toString(), "Windows load error");
     		ErrorBox(e.toString(), "Windows load error");
     	}
 
     	java.util.List<?> list = (java.util.List<?>)transferable.getTransferData(listFlavor);
 
     	int size = list.size();
     	for (int i = 0; i < size; i++){
 
     		String s = list.get(i).toString();
     		File f = new File(s);
 
     		processCommand(s, f);
     	}
 	}
   
 	private void parseFolder(String path, String[] listOfFiles) throws IOException{
 		if (OSDistro != 1){
 	    	FolderParser parser = new FolderParser(path, listOfFiles);
 	    	jj = parser.parseFolder();
 		}
 	}
   
 	private void calcInsets(){
 		
   	HorizontalWidth = jj.get(count).getHeight() > intY - topInsets ? 16 : 0;
   	VerticalHeight = jj.get(count).getWidth() > intX - sideInsets ? 16 : 0; //Different than 16 on Windows?
   	VerticalHeight += jmb.isVisible() ? jmb.getHeight() : 0;
   	sideInsets = frame.getInsets().left + frame.getInsets().right + HorizontalWidth;
   	topInsets = frame.getInsets().top + frame.getInsets().bottom + VerticalHeight;
 
   }
   
 	private void sort(List<JImage> list){
 	  for (int x = 0; x < list.size(); x++){
 		  int index_of_min = x;
 		  for (int y = x; y < list.size(); y++){
 			  if (list.get(index_of_min).getName().compareTo(list.get(y).getName()) > 0){
 				  index_of_min = y;
 			  }
 		  }
 		  JImage temp = list.get(x);
 		  list.set(x, list.get(index_of_min));
 		  list.set(index_of_min, temp);
 	  }
   }
   
 	public void setJRSize(){
 
 		if (boolFullscreen){return;}
 		
 		if (jj.size() != 0){
 			calcInsets();
 			int width = jj.get(count).getWidth() + sideInsets < intX ? jj.get(count).getWidth() + sideInsets + 2 : intX;
 			int height = jj.get(count).getHeight() < intY - topInsets ? jj.get(count).getHeight() + topInsets + 2 : intY;
 			frame.setSize(width, height);
 		}else {
 			frame.setSize((int)(intX / 2), (int)(intY / 2));
 		}
 	}
   
 	public void fullScreen(){
 		JFrame jframe = null;
 		//frame.dispose();
 		try{
 			jframe = newFrame(!boolFullscreen);
 			newFrame(jframe);
 						
 			jframe.setAlwaysOnTop(!boolFullscreen);
 			OnTop = !boolFullscreen;
 			boolFullscreen = !boolFullscreen;
 		}catch (Exception ex){
 			//InfoBox(ex, "Fullscreen error 1");
 			ErrorBox(ex, "Fullscreen error 1");
 		}
 		if (boolFullscreen){
 			try{
 				jframe.toFront();
 				//GraphicsDevice vc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
 				if (!gs[currentScreen].isDisplayChangeSupported()){
 					System.out.println("True fullscreen not supported on screen: " + (currentScreen + 1));
 				}else{
 					DisplayMode dm = findFirstCompatibleMode();
 					gs[currentScreen].setDisplayMode(dm);
 				}
 
 				jframe.setPreferredSize(new Dimension(intX, intY));
 				jframe.setSize(intX, intY);
 				jframe.validate();
 				jframe.repaint();
 				
 				//gs[currentScreen].setFullScreenWindow(jframe);
 			}catch (Exception ex){
 				ErrorBox(ex, "Fullscreen error 2");
 			}
 
 			if (jj.size() != 0){flickCount();}
 			Thread curThread = new Thread(new curHide());
 			curThread.start();
 		}else{			
 			if (jj.size() != 0){
 				flickCount();
 				pane.validate();
 				calcInsets();
 			}
 			jframe.getContentPane().setCursor(defaultCursor);
 			//jframe.getContentPane().setCursor(Cursor.WAIT_CURSOR);
 		}
 		frame.dispose();
 		frame = jframe;
 		setJRSize();
 		if (currentScreen == 1){
 			frame.setLocation(gs[0].getDisplayMode().getWidth(), 0);
 		}else {
 
 			frame.setLocation(0, 0);
 		}
 		System.out.println(frame.getLocation().x);
 		//gs[0].setFullScreenWindow(frame);
 		frame.setVisible(true);
 	}
   
   public class curHide implements Runnable{
 
 		@Override
 		public void run() {
 			while (boolFullscreen){
 				try {
 					Thread.sleep(3000);
 					if (boolFullscreen){
 						if (frame.getContentPane().getCursor() == defaultCursor){
 							frame.getContentPane().setCursor(blankCursor);
 						}
 					}
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 				} catch (Exception e){
 					e.printStackTrace();
 				}
 			}
 			
 		}
   }
   
   private void Zarchiver(File file){
 	  jj = !jmb.isRAMMode() ? archiver.oldZarchiver(file) : archiver.Zarchiver(file);
   }
 
   private void rarchiver(File file) throws IOException{
 	  jj = !jmb.isRAMMode() ? archiver.oldRarchiver(file) : archiver.rarchiver(file);
   }
   
   @SuppressWarnings("unused")
   private JImage ParseImageFromDisk(String dir){
 	  
 	  File file;
 	  JImage jimg = null;
 	  BufferedImage img;
 	  ImageIcon icon;
 	  
 	  try{
 		  file = new File(dir);
 		  if (file.isFile() && isSupported(dir)){
 			  processCommand(dir);
 			  curdir = dir;
 		  }else{
 			  ErrorBox("Couldn't parse Image", "Disk parse error 1");
 			  //InfoBox("Couldn't parse Image", "Disk parse error 1");
 			  if (curdir != null){
 				  file = new File(curdir);
 				  img = ImageIO.read(file);
 				  jimg = new JImage(file.getAbsolutePath());
 			  }else{
 				  jimg = null;
 			  }			  			  
 		  }
 		  
 	  }catch (Exception ex){
 		  //InfoBox(ex.toString(), "Disk parse error 2");
 		  ErrorBox(ex.toString(), "Disk parse error 2");
 	  }
 	  return jimg;
   }
   
   private boolean isSupported(String extension){
 	  for (String s : supportedExtensions){
 		  if (extension.toLowerCase().endsWith(s)){return true;}
 	  }
 	  return false;
   }
   
   public void archiveChooser(){
 	  
 	  int returnVal = 0;
 	  File file = null;
 	  frame.setAlwaysOnTop(false);
 		
 	  if (OSDistro == 2){
 			@SuppressWarnings("unused")
 			FileDialog fd = new FileDialog(frame, "Test", FileDialog.LOAD);
 			//returnVal = fd.show();  
 			//file = fd.getSelectedFile();
 	  }else{
 		  JFileChooser fc = new JFileChooser();
 			fc.setAcceptAllFileFilterUsed(false);
 			fc.setFileFilter(new ArchiveFilter());
 			returnVal = fc.showOpenDialog(frame);
 			file = fc.getSelectedFile();
 	  }
 	  frame.setAlwaysOnTop(OnTop);
 		
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
           
           String line = file.getPath();
           if(line.endsWith(".zip") || line.endsWith(".cbz")){
         	  Zarchiver(file);
           }else if(line.endsWith(".rar") || line.endsWith(".cbr")){
           	try {
 				rarchiver(file);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
           }else if(line.endsWith(".tar")){
         	  jj = archiver.Tarchiver(file);
           }else if(line.endsWith(".ar") || line.endsWith(".cba")){
         	  jj = archiver.ArArchiver(file);
           }
 
 	    	sideInsets = panel.getInsets().left + panel.getInsets().right + pane.getInsets().left + 
 	    			pane.getInsets().right + frame.getInsets().left + frame.getInsets().right;
 	    	topInsets = panel.getInsets().bottom + panel.getInsets().top + pane.getInsets().bottom + 
 	    			pane.getInsets().top + jmb.getHeight() + frame.getInsets().top + frame.getInsets().bottom;
 
 	    	sort(jj);
 	    	flickCount();
 	    	pane.validate();
 
 	    	calcInsets();
       }
   }
   
   public void folderChooser(){
 	  
 	  int returnVal = 0;
 	  File file = null;
 
 	  frame.setAlwaysOnTop(false);
 	  if (OSDistro == 2){
 			@SuppressWarnings("unused")
 			FileDialog fd = new FileDialog(frame, "Test", FileDialog.LOAD);
 	  }else{
 		  JFileChooser fc = new JFileChooser();
 			fc.setAcceptAllFileFilterUsed(false);
 			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 			returnVal = fc.showOpenDialog(frame);
 			file = fc.getSelectedFile();
 	  }
 	  frame.setAlwaysOnTop(OnTop);
 		
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
           
 			try {
 				parseFolder(file.getAbsolutePath(), file.list());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
           
 	    	sideInsets = panel.getInsets().left + panel.getInsets().right + pane.getInsets().left + 
 	    			pane.getInsets().right + frame.getInsets().left + frame.getInsets().right;
 	    	topInsets = panel.getInsets().bottom + panel.getInsets().top + pane.getInsets().bottom + 
 	    			pane.getInsets().top + jmb.getHeight() + frame.getInsets().top + frame.getInsets().bottom;
 
 	    	sort(jj);
 	    	flickCount();
 	    	pane.validate();
 
 	    	calcInsets();
       }
   }
   
   public void AlwaysOnTop(){
 	  if (!boolFullscreen){
 		frame.setAlwaysOnTop(!OnTop);
 		OnTop = !OnTop;
 	  }
   }
   
   public void HideMenu(){
 	  jmb.setVisible(!jmb.isVisible());
   }
   
   public void Jump(){
 	  if (jj.size() > 1){
 		  Object resultObject = InputBox("Select a page between 1 and " + jj.size() + " to jump to.", "Jump to page");
 		  try{
 			  int page = Integer.parseInt(resultObject.toString());
 			  if ((page < 1 || page > jj.size())){
 				  ErrorBox("Invalid page number", "Minor error");
 				  return;
 			  }
 			  count = page - 1;
 			  flickCount();
 		  }catch(Exception ex){
 			  ErrorBox("Invalid page number", "Minor error");
 		  }
 	  }
   }
   
   public void commandLine(String[] args){
 	for (int i = 0; i < args.length; i++){
 		try {
 			processCommand(args[i]);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	finishProcessing();
   }
   
   private void processCommand(String line) throws IOException{
 	  processCommand(line, new File(line));
   }
   
   private void processCommand(String line, File file) throws IOException{
 	  if (isSupported(line)){
 		  jj.add(new JImage(line));
       }else if(file.isDirectory()){
       	parseFolder(line, file.list());
       }else if(line.endsWith(".zip") || line.endsWith(".cbz")){
       	Zarchiver(file);
       }else if(line.endsWith(".rar") || line.endsWith(".cbr")){
       	rarchiver(file);	            	   
       }else if(line.endsWith(".tar")){
     	  jj = archiver.Tarchiver(file);
       }else if(line.endsWith(".ar") || line.endsWith(".cba")){
     	  jj = archiver.ArArchiver(file);
       }
   }
 	
 	public void repaint(){
 		//panel.repaint();
 		panel.updateUI();
 		//pane.repaint();
 		pane.updateUI();
 		//pnlImage.repaint();
 		pnlImage.updateUI();
 		//jmb.repaint();
 		jmb.updateUI();
 	}
 
 	public void scroll(int scrollLength) {
 		pane.getVerticalScrollBar().setValue(pane.getVerticalScrollBar().getValue() + scrollLength);
 	}
 
 	public JLabel zoom(){
 		JLabel tempImage = null;
 		double ratio = jmb.getZoom();
 		
 		if (ratio == 1 || ratio == -1){
 			if (jj.get(count).getWidth() < intX || ratio == 1){
 				tempImage = jj.get(count).getLabel();
 				pnlImage.setPreferredSize(new Dimension(jj.get(count).getWidth(), jj.get(count).getHeight())); //works
 			}else{
 				tempImage = jj.get(count).getResizedImage(1, intX, sideInsets);
 				pnlImage.setPreferredSize(new Dimension(intX - sideInsets - 2, tempImage.getHeight() + topInsets)); //works
 			}
 		}else{
 			tempImage = jj.get(count).getResizedImage(ratio, intX, sideInsets);
 			pnlImage.setPreferredSize(new Dimension(tempImage.getWidth() + sideInsets + 2, tempImage.getHeight())); //add option to wrap to image? probably in flickcount
 		}
 		
 		return tempImage;
 	}
 
 	public DisplayMode findFirstCompatibleMode(){
 		
 		DisplayMode[] goodModes = gs[0].getDisplayModes();
 				
 		return goodModes[0];
 		
 	}
 		
 	public JFrame getFrame(){
 		return this.frame;
 	}
 	
 } //980 //881
