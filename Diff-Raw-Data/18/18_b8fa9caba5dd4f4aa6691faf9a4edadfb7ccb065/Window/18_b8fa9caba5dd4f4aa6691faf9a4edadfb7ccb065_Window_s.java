 package gui;
 
 import character.Input;
 import character.Player;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.filechooser.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.Stack;
 
 /**
  * Created with IntelliJ IDEA.
  * User: brad
  * Date: 4/4/13
  * Time: 2:53 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Window extends JFrame {
     int[][] map;
     ImagePanel imagePanel;
     BufferedImage image;
     Stack<Integer> keyStack;
 
     private JMenuBar menubar;
     private JMenu file;
     private JMenuItem loadMap;
 
     private JFileChooser fileChooser;
 
     public Window() {
         initComponents();
 //        createEntities();
 
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
     }
 
     public void initComponents() {
 
         image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
 
         keyStack = new Stack<Integer>();
 
         menubar = new JMenuBar();
         file = new JMenu("File");
         loadMap = new JMenuItem("Load Map");
 
         fileChooser = new JFileChooser();
 
         fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
 
             @Override
             public boolean accept(File f) {
                 return f.getName().endsWith(".map") || f.isDirectory();
             }
 
             @Override
             public String getDescription() {
                 return "Map files";
             }
         });
 
         menubar.add(file);
         file.add(loadMap);
 
         this.setJMenuBar(menubar);
 
         imagePanel = new ImagePanel(image, 0, 0);
 
         add(imagePanel);
 
         loadMap.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 fileChooser.setCurrentDirectory(new File(getClass().getResource("/maps/").getFile()));
                 int val = fileChooser.showOpenDialog(imagePanel);
                 if (val == JFileChooser.APPROVE_OPTION) {
                     File file = fileChooser.getSelectedFile();
                     try {
                         createMap(file);
                     } catch (IOException e1) {
                         e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                 }
             }
         });
 
         addKeyListener(new KeyListener() {
             @Override
             public void keyTyped(KeyEvent e) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public void keyPressed(KeyEvent e) {
                 keyStack.push(e.getKeyCode());
             }
 
             @Override
             public void keyReleased(KeyEvent e) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         });
 
         pack();
     }
 
     //TODO: clean this up a lot
     public void createEntities() {
         BufferedImage img;
         try {
             img = ImageIO.read(getClass().getResource("/images/abc.jpeg"));
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             return;
         }
         Player player = new Player(img, new Input() {
             @Override
             public int getMovement(int x, int y) {
                 repaint();
                 if (!keyStack.empty()) {
                     switch (keyStack.pop()) {
                         case KeyEvent.VK_LEFT:
                         case KeyEvent.VK_A:
                             if (map[y][x - 1] != 0)
                                 break;
                             return 1;
                         case KeyEvent.VK_UP:
                         case KeyEvent.VK_W:
                             if (map[y - 1][x] != 0)
                                 break;
                             return 2;
                         case KeyEvent.VK_RIGHT:
                         case KeyEvent.VK_D:
                             if (map[y][x + 1] != 0)
                                 break;
                             return 3;
                         case KeyEvent.VK_DOWN:
                         case KeyEvent.VK_S:
                             if (map[y + 1][x] != 0)
                                 break;
                             return 4;
                     }
                 }
                 return 0;
             }
         });
 
         imagePanel.addEntity(player);
 
         Thread t = new Thread(player);
 
         // TODO: need to kill this thread when game reload
         t.start();
     }
 
     public int getColour(int index) {
         switch (index) {
             // White
             case 0:
                 return 0xFFFFFF;
             // Black
             case 1:
                 return 0;
             // Red
             case 2:
                 return 0xFF0000;
             // Green
             case 3:
                 return 0x00FF00;
             // Blue
             case 4:
                 return 0x0000FF;
             // Stupid colour
             case 5:
                 return 0x5EDA9E;
             default:
                 return 0;
         }
     }
 
     public void createMap(File file) throws IOException {
         ArrayList<int[]> mapList = new ArrayList<int[]>();
         Scanner scan = new Scanner(file, "UTF-8");
         while (scan.hasNext()) {
             String str = scan.nextLine();
             Scanner line = new Scanner(str);
             int[] buffer = new int[str.length()];
             int i = 0;
 
             while (line.hasNext()) {
                 buffer[i++] = Integer.parseInt(line.next());
             }
 
             int[] row = new int[i];
 
             System.arraycopy(buffer, 0, row, 0, i);
 
             mapList.add(row);
         }
 
         map = mapList.toArray(new int[0][]);
 
         int[] RGB = new int[image.getHeight() * image.getWidth()];
 
         for (int y = 0; y < image.getHeight(); y++) {
             for (int x = 0; x < image.getWidth(); x++) {
                 double yRatio = (double) y / image.getHeight();
                 double xRatio = (double) x / image.getWidth();
 
                 RGB[y * image.getWidth() + x] = getColour(map[(int) (map.length * yRatio)][(int) (map[0].length * xRatio)]);
             }
         }
 
         image.setRGB(0, 0, image.getWidth(), image.getHeight(), RGB, 0, image.getWidth());
 
         remove(imagePanel);
 
        imagePanel = new ImagePanel(image, 500 / map[0].length, 500 / map.length);
 
         add(imagePanel);
 
         pack();
 
         createEntities();
 
         repaint();
     }
 }
