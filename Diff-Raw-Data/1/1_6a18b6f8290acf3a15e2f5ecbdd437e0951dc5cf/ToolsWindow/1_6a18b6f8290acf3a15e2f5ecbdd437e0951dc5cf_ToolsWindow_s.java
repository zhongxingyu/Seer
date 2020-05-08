 package pointAndPixel;
 
 import javax.swing.*;
 import javax.swing.text.*;
 import javax.swing.event.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 
 import javax.imageio.*;
 
 import java.io.*;
 import java.util.*;
 
 import java.util.regex.*;
 
 public class ToolsWindow extends JFrame implements ActionListener, DocumentListener, FocusListener {
 
   public enum ToolState {
     DRAW,
     DROPPER,
     FILL,
     SELECT
   }
 
   public static final int DEFAULT_PIXEL_SIZE = 20;
   public static final int DEFAULT_WIDTH_PIXELS = 25;
   public static final int DEFAULT_HEIGHT_PIXELS = 25;
 
   private Container container = null;
   private ArrayList<PixelCanvas> allCanvases = null;
   private PixelCanvas canvas = null;
   
   private Map<String, Color> defaultColors = null;
   private Color selectedColor = new Color(0, 0, 0, 255);
   private Color[][] copyBuffer = null;
   private ToolState toolState = ToolState.DRAW;
   
   private JButton btnDrawPixel = null;
   private JButton btnCopyColor = null;
 	private JButton btnFill = null;
 	private JButton btnSelectPixels = null;
   
   private JTextField red = null;
   private JTextField green = null;
   private JTextField blue = null;
   private JTextField alpha = null;
   
   private JCheckBoxMenuItem gridOn = null;
   private JCheckBoxMenuItem gridExportOn = null;
   
   private JPanel colorSample = null;
   
   private File saveFile = null;
   
   private JFileChooser fc = null;
   
   public ToolsWindow() {
     super("Pixel Tools");
     setDefaultCloseOperation(EXIT_ON_CLOSE);
     
     setSize(175, 450);
 		setResizable(false);
     //setAlwaysOnTop(true);
     
     defaultColors = new LinkedHashMap<String, Color>();
     
     defaultColors.put("Black", Color.BLACK);
 		defaultColors.put("Gray", Color.GRAY);
 		defaultColors.put("Red", Color.RED);
 		defaultColors.put("Orange", new Color(240, 120, 0, 255));         // orange
 		defaultColors.put("Dark Green", new Color(0, 110, 0, 255));       // dark green
 		defaultColors.put("Dark Blue", new Color(0, 0, 128, 255));        // dark blue
 		defaultColors.put("Purple", new Color(85, 0, 128, 255));          // purple
 		
 		defaultColors.put("White", Color.WHITE);
 		defaultColors.put("Light Gray", Color.LIGHT_GRAY);
 		defaultColors.put("Magenta", Color.MAGENTA);
 		defaultColors.put("Yellow", Color.YELLOW);
 		defaultColors.put("Green", Color.GREEN);
 		defaultColors.put("Cyan", Color.CYAN);
 		defaultColors.put("Blue", Color.BLUE);
     
     fc = new JFileChooser();
     
 		setupMenu();
     
     container = getContentPane();
 		container.setLayout(new GridLayout(10, 1));
 		
 		btnDrawPixel = setupButton("Draw Pixel");
 		container.add(btnDrawPixel);
 		btnCopyColor = setupButton("Copy Color");
 		container.add(btnCopyColor);
 		btnFill = setupButton("Fill");
 		container.add(btnFill);
 		btnSelectPixels = setupButton("Select Pixels");
 		container.add(btnSelectPixels);
 		
 		selectButton(btnDrawPixel);
 		
 		JPanel pColors = new JPanel(new GridLayout(2, 7));		
 		for (String colorName: defaultColors.keySet()) {
 		  pColors.add(setupColorButton(colorName, defaultColors.get(colorName)));
 		}		
 		container.add(pColors);		
 		
 		JPanel pRed = new JPanel(new GridLayout(1, 2));
 		pRed.add(new JLabel("<html><div align='right'>Red<br/>(0-255)</div></html>", JLabel.RIGHT));
 		red = new JTextField();
 		red.setText("0");
 		red.getDocument().addDocumentListener(this);
 		red.addFocusListener(this);
 		pRed.add(red);
 		container.add(pRed);
 		
 		JPanel pGreen = new JPanel(new GridLayout(1, 2));
 		pGreen.add(new JLabel("<html><div align='right'>Green<br/>(0-255)</div></html>", JLabel.RIGHT));
 		green = new JTextField();
 		green.setText("0");
 		green.getDocument().addDocumentListener(this);
 		green.addFocusListener(this);
 		pGreen.add(green);
 		container.add(pGreen);
 		
 		JPanel pBlue = new JPanel(new GridLayout(1, 2));
 		pBlue.add(new JLabel("<html><div align='right'>Blue<br/>(0-255)</div></html>", JLabel.RIGHT));
 		blue = new JTextField();
 		blue.setText("0");
 		blue.getDocument().addDocumentListener(this);
 		blue.addFocusListener(this);
 		pBlue.add(blue);
 		container.add(pBlue);
 		
 		JPanel pAlpha = new JPanel(new GridLayout(1, 2));
 		pAlpha.add(new JLabel("<html><div align='right'>Alpha<br/>(0-255)</div></html>", JLabel.RIGHT));
 		alpha = new JTextField();
 		alpha.setText("255");
 		alpha.getDocument().addDocumentListener(this);
 		alpha.addFocusListener(this);
 		pAlpha.add(alpha);
 		container.add(pAlpha);
 		
 		colorSample = new JPanel();
 		colorSample.setBackground(selectedColor);
 		container.add(colorSample);
     
     setVisible(true);
     
     allCanvases = new ArrayList<PixelCanvas>();
     newPixelCanvas();
   }
   
   public void save() {
     int returnVal = selectPixelFile(false, saveFile);
       
     if (returnVal == JFileChooser.APPROVE_OPTION) {
       saveFile = fc.getSelectedFile();
       savePixelFile(saveFile);
     }
   }
   
   public void open() {
     int returnVal = selectPixelFile(true, saveFile);
     
     if (returnVal == JFileChooser.APPROVE_OPTION) {
       saveFile = fc.getSelectedFile();
       loadPixelFile(saveFile);
     }
   }
   
   public void importImage() {
     int returnVal = selectImageFile(true, null);
     
     if (returnVal == JFileChooser.APPROVE_OPTION) {
       String response = JOptionPane.showInputDialog(null, "Pixel size", String.valueOf(canvas.getHeightPixels()));
     
       if (response != null) {
         canvas.setPixelSize(Integer.parseInt(response));
         importImage(fc.getSelectedFile());
       }
     }
   }
   
   public void exportImage() {
     String defaultName = "drawing.png";
     if (saveFile != null) {
       defaultName = saveFile.getName();
       defaultName = defaultName.substring(0, defaultName.lastIndexOf(".")) + ".png";
     }
     int returnVal = selectImageFile(false, new File(defaultName));
     
     if (returnVal == JFileChooser.APPROVE_OPTION) {
       exportImage(fc.getSelectedFile());
     }
   }
   
   public void pixelSize() {
     String response = JOptionPane.showInputDialog(null, "Pixel Size", String.valueOf(canvas.getPixelSize()));
     
     if (response != null) {
       canvas.setPixelSize(Integer.parseInt(response));
       
       canvas.resizeWindow();
       
       repaint();
     }
   }
   
   public void height() {
     String response = JOptionPane.showInputDialog(null, "Height in Pixels", String.valueOf(canvas.getHeightPixels()));
     
     if (response != null) {
       canvas.setHeightPixels(Integer.parseInt(response));
       
       canvas.resizeGrid();
       canvas.resizeWindow();
       
       repaint();
     }
   }
   
   public void width() {
     String response = JOptionPane.showInputDialog(null,  "Width in Pixels", String.valueOf(canvas.getWidthPixels()));
     
     if (response != null) {
       canvas.setWidthPixels(Integer.parseInt(response));
       
       canvas.resizeGrid();
       canvas.resizeWindow();
       
       repaint();
     }
   }
   
   public void copy() {
     copyBuffer = canvas.getSelected();
     canvas.clearSelected();
   }
   
   public void paste() {
     canvas.pasteSelected(copyBuffer);
   }
   
   public void actionPerformed(ActionEvent e) {
     if ("Save".equals(e.getActionCommand())) {
       save();
     }
     else if ("Open".equals(e.getActionCommand())) {
       open();
     }
     else if ("New".equals(e.getActionCommand())) {
       newPixelCanvas();
     }
     else if ("Import Image".equals(e.getActionCommand())) {
       importImage();
     }
     else if ("Export Image".equals(e.getActionCommand())) {
       exportImage();
     }
     else if ("Pixel Size".equals(e.getActionCommand())) {
       pixelSize();
     }
     else if ("Canvas Width".equals(e.getActionCommand())) {
       width();
     }
     else if ("Canvas Height".equals(e.getActionCommand())) {
       height();
     }
     else if ("Draw Pixel".equals(e.getActionCommand())) {
       setToolState(ToolState.DRAW);
     }
     else if ("Copy Color".equals(e.getActionCommand())) {
       setToolState(ToolState.DROPPER);
     }
     else if ("Fill".equals(e.getActionCommand())) {
       setToolState(ToolState.FILL);
     }
     else if ("Select Pixels".equals(e.getActionCommand())) {
       setToolState(ToolState.SELECT);
     }
     else if ("Show grid".equals(e.getActionCommand())) {
       canvas.setGridOn(gridOn.isSelected());
     }
     else if ("Show grid in export".equals(e.getActionCommand())) {
       canvas.setGridExportOn(gridExportOn.isSelected());
     }
     else if ("Copy".equals(e.getActionCommand())) {
       copy();
     }
     else if ("Paste".equals(e.getActionCommand())) {
       paste();
     }
     else if ("Undo".equals(e.getActionCommand())) {
       canvas.undo();
     }
     else if (defaultColors.keySet().contains(e.getActionCommand())) {
       setSelectedColor(defaultColors.get(e.getActionCommand()));
     }
     else if ("Quit".equals(e.getActionCommand())) {
       System.exit(0);
     }
     else {
       JOptionPane.showMessageDialog(this, "Unhandled action: " + e.getActionCommand());
     }
   }
   
   public void focusGained(FocusEvent e) {
     ((JTextField)e.getComponent()).selectAll();
   }
 
   public void focusLost(FocusEvent e) {}
   
   public int selectPixelFile(boolean open, File file) {
     fc.resetChoosableFileFilters();
     fc.removeChoosableFileFilter(fc.getAcceptAllFileFilter());
     ImgFilter filter = new ImgFilter();
     filter.addExtension("pixel");
     filter.setDescription("Point and Pixel File (*.pixel)");
     fc.setFileFilter(filter);
     fc.setSelectedFile(file);
     
     if (open) {
       return fc.showOpenDialog(this);
     }
     else {
       return fc.showSaveDialog(this);
     }
   }
   
   public int selectImageFile(boolean open, File file) {
     fc.resetChoosableFileFilters();
     fc.removeChoosableFileFilter(fc.getAcceptAllFileFilter());
     ImgFilter filter = new ImgFilter();
     filter.addExtension("png");
     if (open) {
       filter.addExtension("jpg");
       filter.addExtension("jpeg");
       filter.setDescription("Image File (*.png, *.jpg)");
     }
     else {
       filter.setDescription("PNG File (*.png)");
     }
     fc.setFileFilter(filter);
     fc.setSelectedFile(file);
     
     if (open) {
       return fc.showOpenDialog(this);
     }
     else {
       return fc.showSaveDialog(this);
     }
   }
   
   public void insertUpdate(DocumentEvent e) {
     int[] argb = intToARGB(selectedColor.getRGB());
     
     try {
       argb[1] = Integer.parseInt(red.getText());
     }
     catch (Exception ex) {}
     
     try {
       argb[2] = Integer.parseInt(green.getText());
     }
     catch (Exception ex) {}
     
     try {
       argb[3] = Integer.parseInt(blue.getText());
     }
     catch (Exception ex) {}
     
     try {
       argb[0] = Integer.parseInt(alpha.getText());
     }
     catch (Exception ex) {}
     
     selectedColor = new Color(argb[1], argb[2], argb[3], argb[0]);
     colorSample.setBackground(selectedColor);
   }
     
   public void removeUpdate(DocumentEvent e) {}
   
   public void changedUpdate(DocumentEvent e) {}
   
   public void setupMenu() {
     JMenuBar menuBar = new JMenuBar();
 		
 		JMenu fileMenu = new JMenu("File");
 		menuBar.add(fileMenu);
 				
 		fileMenu.add(setupMenu("New", KeyEvent.VK_N));	
 		fileMenu.add(setupMenu("Open", KeyEvent.VK_O));		
 		fileMenu.add(setupMenu("Save", KeyEvent.VK_S));	
 		fileMenu.addSeparator();		
 		
 		fileMenu.add(setupMenu("Import Image", KeyEvent.VK_I));	
 		fileMenu.add(setupMenu("Export Image", KeyEvent.VK_E));		
 		fileMenu.addSeparator();	
 		
 		fileMenu.add(setupMenu("Quit", KeyEvent.VK_Q));
 		
 		JMenu editMenu = new JMenu("Edit");
 		menuBar.add(editMenu);
 		
 		editMenu.add(setupMenu("Undo", KeyEvent.VK_Z));	
 		editMenu.addSeparator();	
 				
 		editMenu.add(setupMenu("Copy", KeyEvent.VK_C));	
 		editMenu.add(setupMenu("Paste", KeyEvent.VK_V));	
 		
 		JMenu keyMenu = new JMenu("Settings");
 		menuBar.add(keyMenu);
 		
 		keyMenu.add(setupMenu("Pixel Size", KeyEvent.VK_P));
 		keyMenu.add(setupMenu("Canvas Width", KeyEvent.VK_W));	
 		keyMenu.add(setupMenu("Canvas Height", KeyEvent.VK_H));	
 		fileMenu.addSeparator();	
 		
 		gridOn = new JCheckBoxMenuItem("Show grid", true);
 		gridOn.setActionCommand("Show grid");
 		gridOn.addActionListener(this);
 		keyMenu.add(gridOn);
 		
 		gridExportOn = new JCheckBoxMenuItem("Show grid in export", false);
 		gridExportOn.setActionCommand("Show grid in export");
 		gridExportOn.addActionListener(this);
 		keyMenu.add(gridExportOn);
 				
 		setJMenuBar(menuBar);
   }
   
   public int getValue(String json, String valueName) {
     int retVal = -1;
     Pattern pattern = Pattern.compile("\"" + valueName + "\":(\\d+)");
     Matcher matcher = pattern.matcher(json);
     if (matcher.find()) {
       retVal = Integer.parseInt(matcher.group(1));
     }
     return retVal;
   }
   
   public void loadPixelFile(File file) {
     BufferedReader input = null; 
     
     try {      
       input = new BufferedReader(new FileReader(file));
       String line = "";
       String json = "";
       int heightPixels = 0;
       int widthPixels = 0;
       int pixelSize = 0;
       
       while ((line = input.readLine()) != null) {
         json += line;
       }
       
       heightPixels = getValue(json, "height_pixels");
       widthPixels = getValue(json, "width_pixels");
       pixelSize = getValue(json, "pixel_size");
       
       canvas.setPixelSize(pixelSize);
       canvas.setWidthPixels(widthPixels);
       canvas.setHeightPixels(heightPixels);
       canvas.resizeGrid();
       
       canvas.resizeWindow();    
       canvas.newHistorySpot();
       
       Pattern pixels = Pattern.compile("\\{\\S*\\}");
       Matcher matcher = pixels.matcher(json);
       while (matcher.find()) {
         String pixel = matcher.group();
         
         int xVal = getValue(pixel, "x");
         int yVal = getValue(pixel, "y");
         int rVal = getValue(pixel, "r");
         int gVal = getValue(pixel, "g");
         int bVal = getValue(pixel, "b"); 
         int aVal = getValue(pixel, "a");
         
         canvas.colorPixel(xVal, yVal, new Color(rVal, gVal, bVal, aVal));
       }
       
     } catch (IOException e) {
       JOptionPane.showMessageDialog(this, "Unable to load drawing");
     }
     finally {
       if (input != null) {
         try { input.close(); } catch (Exception e) {}
       }
     }
   }
   
   public void savePixelFile(File file) {
     Writer output = null;    
     
     try {      
       Color[][] grid = canvas.getGrid();
       output = new BufferedWriter(new FileWriter(file));
 
       output.write("{\n");
       output.write("  \"pixel_size\":" + canvas.getPixelSize());
       output.write(",\n  \"height_pixels\":" + canvas.getHeightPixels());
       output.write(",\n  \"width_pixels\":" + canvas.getWidthPixels());
       output.write(",\n  \"pixels\":[");
       
       boolean firstPixel = true;
       
       for (int column = 0; column < grid.length; column++) {
         for (int row = 0; row < grid[column].length; row++) {
           if (grid[column][row] != null) {
             Color c = grid[column][row];
             if (!firstPixel) {
               output.write(",");
             }
             firstPixel = false;
             output.write("\n    {\"x\":" + column + ",\"y\":" + row + ",\"r\":" + c.getRed() + ",\"g\":" + c.getGreen() + ",\"b\":" + c.getBlue() + ",\"a\":" + c.getAlpha() + "}");
           }
         }
       }
       
       output.write("\n  ]\n}");
     } catch (IOException e) { 
       JOptionPane.showMessageDialog(this, "Unable to save drawing");
     }
     finally {
       if (output != null) {
         try { output.close(); } catch (Exception e) {}
       }
     }
   }
   
   public void exportImage(File file) {
     BufferedImage bi = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
     Graphics g = bi.createGraphics();
     
     boolean gridOn = canvas.isGridOn();
     canvas.setGridOn(canvas.isGridExportOn());
     
     canvas.setShowSelected(false);
     
     canvas.paint(g);
     
     canvas.setShowSelected(true);
     
     try {
       ImageIO.write(bi, "PNG", file);
     } 
     catch(Exception e) {
       JOptionPane.showMessageDialog(this, "Unable to export image");
     }
     
     canvas.setGridOn(gridOn);
   }
   
   public void importImage(File file) {
     try {
       BufferedImage img = ImageIO.read(file);
       int widthPixels = img.getWidth() / canvas.getPixelSize();
       int heightPixels = img.getHeight() / canvas.getPixelSize();
       
       canvas.setWidthPixels(widthPixels);
       canvas.setHeightPixels(heightPixels);
       canvas.resizeGrid();
       
       for (int column=0; column<widthPixels; column++) {
         for (int row=0; row<heightPixels; row++) {
           canvas.colorPixel(column, row, getPixelColor(img, column, row));
         }
       }
       
       canvas.resizeWindow();
     } 
     catch(Exception e) {
       e.printStackTrace();
       JOptionPane.showMessageDialog(this, "Unable to import image");
     }
   }
   
   public Color getPixelColor(BufferedImage img, int column, int row) {
     int MASK = 0x000000ff;
     int pixelSize = canvas.getPixelSize();
     int count = 0;
     int alphaPart = 0;
     int redPart = 0;
     int greenPart = 0;
     int bluePart = 0;
     
     for (int x = column * pixelSize; x < column * pixelSize + pixelSize; x++) {
       for (int y = row * pixelSize; y < row * pixelSize + pixelSize; y++) {
         int[] argb = intToARGB(img.getRGB(x, y));
         
         alphaPart += argb[0];
         redPart += argb[1];
         greenPart += argb[2];
         bluePart += argb[3];
         
         count++;
       }
     }
     
     return new Color(redPart/count, greenPart/count, bluePart/count, alphaPart/count);
   }
   
   public void newPixelCanvas() {
     CanvasFrame cf = new CanvasFrame(this);
 		JDialog d = new JDialog(cf);
     d.setModalityType(Dialog.ModalityType.MODELESS);
     cf.setLocationRelativeTo(this);
     cf.setLocation(440, 100);
 		cf.setVisible(true);
 		
     canvas = cf.getCanvas();
     allCanvases.add(canvas);
   }
   
   public void select(PixelCanvas canvas) {
     this.canvas = canvas;
     gridOn.setSelected(canvas.isGridOn());
     gridExportOn.setSelected(canvas.isGridExportOn());
   }
   
   public void deleteCanvas(PixelCanvas canvas) {
     allCanvases.remove(canvas);
     
     if (this.canvas == canvas) {
       this.canvas = null;
     }
     
     if (allCanvases.size() == 0) {
       System.exit(0);
     }
   }
   
   public JMenuItem setupMenu(String menuText, int keyEvent) {
 		JMenuItem menuItem = new JMenuItem(menuText, keyEvent);
 		menuItem.setActionCommand(menuText);
 		menuItem.addActionListener(this);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, ActionEvent.CTRL_MASK));
 		return menuItem;
   }
   
   public JButton setupButton(String buttonText) {
 		JButton button = new JButton(buttonText);
 		button.setBackground(Color.LIGHT_GRAY);
 		button.setActionCommand(buttonText);
 		button.addActionListener(this);
 		return button;
   }
   
   public JButton setupColorButton(String action, Color color) {
 		JButton button = new JButton("");
 		button.setBackground(color);
 		button.setActionCommand(action);
 		button.addActionListener(this);
 		return button;
   }
   
   public Color getSelectedColor() {
     return selectedColor;
   }
   
   public void setSelectedColor(Color color) {
     selectedColor = color;    
     
     int MASK = 0x000000ff;
     int[] argb = intToARGB(color.getRGB());
   
     red.setText(String.valueOf(argb[1]));
     green.setText(String.valueOf(argb[2]));
     blue.setText(String.valueOf(argb[3]));
     alpha.setText(String.valueOf(argb[0]));
     
     colorSample.setBackground(selectedColor);
   }
   
   public static int[] intToARGB(int raw) {
     int MASK = 0x000000ff;
     int[] argb = new int[4];
     
     argb[0] = (raw >> 24) & MASK;
     argb[1] = (raw >> 16) & MASK;
     argb[2] = (raw >> 8) & MASK;
     argb[3] = raw & MASK;
     
     return argb;
   }
   
   public void selectButton(JButton button) {
     clearButtonSelections();
     button.setBackground(Color.GRAY);
   }
   
   public void clearButtonSelections() {
     btnDrawPixel.setBackground(Color.LIGHT_GRAY);
     btnCopyColor.setBackground(Color.LIGHT_GRAY);
 	  btnFill.setBackground(Color.LIGHT_GRAY);
 	  btnSelectPixels.setBackground(Color.LIGHT_GRAY);
   }
   
   public ToolState getToolState() {
     return toolState;
   }
   
   public void setToolState(ToolState toolState) {
     canvas.clearSelected();
     this.toolState = toolState;
     
     switch(toolState) {
       case DRAW:
         selectButton(btnDrawPixel);
         break;
       case DROPPER:
         selectButton(btnCopyColor);
         break;
       case FILL:
         selectButton(btnFill);
         break;
       case SELECT:
         selectButton(btnSelectPixels);
         break;
     }
   }
 }
 
