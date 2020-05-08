 /**
  *
  * @author Kyle Lusk
  */
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import streamoutlining.ImageUtils;
 import streamoutlining.ControlPoint;
 import streamoutlining.SquareCorner;
 
 public class StreamGUI extends JFrame
 {
     private BufferedWriter miscInfo;
     private BufferedWriter outline;
     private BufferedWriter controlPoints;
     private BufferedWriter squareCorners;
     private BufferedImage pointsImage;
     private BufferedImage outlineImage;
     private final int SIDE_LENGTH = 8;
     private final int fieldWidth = 15;
     private int clickCount = 0;
     private int redPointsCount = 1;
     private int yellowPointsCount = 1;
     private int magentaOutlineCount = 1;
     
     private ArrayList<Point>yellowPoints;
     private ArrayList<Point>redPoints;
     private ArrayList<Point>magentaOutline;
     
     private File myfile;
     
     private JFileChooser chooser = new JFileChooser();
     
     private JFrame framePoints;
     private JFrame frameOutline;
     private JPanel bottom;
     
     private JLabel pictureLabel;
     
     private JLabel pointsLabel;
     private JTextField pointsText;
     private JButton pointsBrowse;
     
     private JLabel outlineLabel;
     private JTextField outlineText;
     private JButton outlineBrowse;
    
     private JLabel cornerAXLabel;
     private JTextField cornerAXText;
     private JLabel cornerAYLabel;
     private JTextField cornerAYText;
     
     private JLabel cornerBXLabel;
     private JTextField cornerBXText;
     private JLabel cornerBYLabel;
     private JTextField cornerBYText;
     
     private JLabel cornerCXLabel;
     private JTextField cornerCXText;
     private JLabel cornerCYLabel;
     private JTextField cornerCYText;
     
     private JLabel cornerDXLabel;
     private JTextField cornerDXText;
     private JLabel cornerDYLabel;
     private JTextField cornerDYText;
     
     private JLabel sideLengthLabel;
     private JTextField sideLengthText;
     
     private JLabel hfovLabel;
     private JTextField hfovText;
     
     private JButton done;
     
     public StreamGUI() throws IOException
     {
         miscInfo = new BufferedWriter(new FileWriter("E:/miscInfo.txt"));
         outline = new BufferedWriter(new FileWriter("E:/outline(MAGENTA).txt"));
         controlPoints = new BufferedWriter(new FileWriter("E:/controlPoints(RED).txt"));
         squareCorners = new BufferedWriter(new FileWriter("E:/squareCorners(YELLOW).txt"));
         
 
         bottom = new JPanel();
 
         //CREATES TEXT FIELD AND LABEL for POINTS BROWSE
         pointsLabel = new JLabel("Points: ");
         pointsText = new JTextField(fieldWidth);
         
         //CREATES BROWSE BUTTON
         pointsBrowse = new JButton("Browse");
         
         class pointsBrowseListener implements ActionListener
         {
             @Override
             public void actionPerformed(ActionEvent event)
             {
                 if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                 {
                     try 
                     {
                         pointsImage = ImageIO.read(chooser.getSelectedFile());
                         myfile = chooser.getSelectedFile();
                         pointsText.setText(myfile.getName());
                         yellowPoints = ImageUtils.getPixelListForColor(pointsImage, Color.yellow);
                         redPoints = ImageUtils.getPixelListForColor(pointsImage, Color.red);
                         addSqPtsCtrlPts();
                     } catch (IOException ex) 
                     {
                         System.out.println(ex);
                     }
                 }
             }
         }
         ActionListener fileName = new pointsBrowseListener();
         pointsBrowse.addActionListener(fileName);
         
         //CREATES TEXT FIELD AND LABEL for OUTLINE BROWSE 
         outlineLabel = new JLabel("Outline: ");
         outlineText = new JTextField(fieldWidth);
         
         //CREATES BROWSE BUTTON
         outlineBrowse = new JButton("Browse");
         
         class outlineBrowseListener implements ActionListener
         {
             @Override
             public void actionPerformed(ActionEvent event)
             {
                 if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                 {
                     try 
                     {
                         outlineImage = ImageIO.read(chooser.getSelectedFile());
                         myfile = chooser.getSelectedFile();
                         outlineText.setText(myfile.getName());
                         magentaOutline = ImageUtils.getPixelListForColor(outlineImage, Color.magenta);
                         addOutline();
                     } catch (IOException ex) 
                     {
                         System.out.println(ex);
                     }
                 }
             }
         }
         ActionListener fileName1 = new outlineBrowseListener();
         outlineBrowse.addActionListener(fileName1);
         
         //CREATES TEXT FIELDS AND LABELS FOR CORNER A-D, SIDELENGTH, AND HFOV
         cornerAXLabel = new JLabel("Corner A (X): ");
         cornerAXText = new JTextField(5);
         
         cornerAYLabel = new JLabel("Corner A (Y): ");
         cornerAYText = new JTextField(5);
         
         cornerBXLabel = new JLabel("Corner B (X): ");
         cornerBXText = new JTextField(5);
         
         cornerBYLabel = new JLabel("Corner B (Y): ");
         cornerBYText = new JTextField(5);
         
         cornerCXLabel = new JLabel("Corner C (X): ");
         cornerCXText = new JTextField(5);
         
         cornerCYLabel = new JLabel("Corner C (Y): ");
         cornerCYText = new JTextField(5);
         
         cornerDXLabel = new JLabel("Corner D (X): ");
         cornerDXText = new JTextField(5);
         
         cornerDYLabel = new JLabel("Corner D (Y): ");
         cornerDYText = new JTextField(5);
         
         sideLengthLabel = new JLabel("Square Side Length: ");
         sideLengthText = new JTextField(5);
         String strSide = String.valueOf(SIDE_LENGTH);
         sideLengthText.setText(strSide);
         
         hfovLabel = new JLabel("HFOV: ");
         hfovText = new JTextField(5);
         
         //CREATES DONE BUTTON
         done = new JButton("DONE");
         class doneListener implements ActionListener
         {
             @Override
             public void actionPerformed(ActionEvent event)
             {
                 try 
                 {
                     onDoneClick();
                 } catch (IOException ex) 
                 {
                     System.out.println(ex);
                     ex.printStackTrace();
                 }
             }
         }
         ActionListener doneButton = new doneListener();
         done.addActionListener(doneButton);
 
         //CREATES PANEL
         bottom.add(pointsLabel);
         bottom.add(pointsText);
         bottom.add(pointsBrowse);
         bottom.add(outlineLabel);
         bottom.add(outlineText);
         bottom.add(outlineBrowse);
         bottom.add(cornerAXLabel);
         bottom.add(cornerAXText);
         bottom.add(cornerAYLabel);
         bottom.add(cornerAYText);
         bottom.add(cornerBXLabel);
         bottom.add(cornerBXText);
         bottom.add(cornerBYLabel);
         bottom.add(cornerBYText);
         bottom.add(cornerCXLabel);
         bottom.add(cornerCXText);
         bottom.add(cornerCYLabel);
         bottom.add(cornerCYText);
         bottom.add(cornerDXLabel);
         bottom.add(cornerDXText);
         bottom.add(cornerDYLabel);
         bottom.add(cornerDYText);
         bottom.add(sideLengthLabel);
         bottom.add(sideLengthText);
         bottom.add(hfovLabel);
         bottom.add(hfovText);
         bottom.add(done);
         this.add(bottom);
     }
     
     private void addOutline()
     {
         pictureLabel = new JLabel(new ImageIcon(outlineImage));
         frameOutline = new JFrame();
         frameOutline.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frameOutline.getContentPane().add(pictureLabel);
         frameOutline.pack();
         JScrollPane scrollBar=new JScrollPane(pictureLabel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                 JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
         frameOutline.add(scrollBar);
         pictureLabel.setBackground(Color.black);
         pictureLabel.setOpaque(true);
         frameOutline.setTitle("Stream Outline");
         frameOutline.setLocation(350,0);
         frameOutline.setSize(900,700);
         frameOutline.setVisible(true);
     }
         
     private void addSqPtsCtrlPts()
     {
         pictureLabel = new JLabel(new ImageIcon(pointsImage));
         framePoints = new JFrame();
         MouseListener listener = new MouseListener() 
         { 
             @Override
             public void mousePressed(MouseEvent event) 
             {
                 String inputValue = JOptionPane.showInputDialog("Please enter either:"
                         + " (control, or corner).");
                 if (inputValue.equals("control"))
                 {
                     String controlName = JOptionPane.showInputDialog("Please enter "
                             + "the control point number.");
                     int i = Integer.parseInt(controlName);
                     Point p = (event.getPoint());
                     Point pClosest = ImageUtils.getNearestPointFromList(p, redPoints);
                     ControlPoint cp = new ControlPoint(i, pClosest);
                     //NEED TO FIGURE OUT WHAT TO DO / HOW TO ADD cp
                 }else if(inputValue.equals("corner"))
                 {
                     String cornerLetter = JOptionPane.showInputDialog("Please enter"
                             + " the letter of the corner.");
                     Point p = (event.getPoint());
                     Point pClosest = ImageUtils.getNearestPointFromList(p, yellowPoints);
                     SquareCorner sq = new SquareCorner(cornerLetter, pClosest);
                     //NEED TO FIGURE OUT WHAT TO DO / HOW TO ADD sq
                    if(cornerLetter.equalsIgnoreCase("a"))
                     {
                         cornerAXText.setText(String.valueOf(pClosest.x));
                        cornerAYText.setText(String.valueOf(pClosest.y));
                     }
                    if(cornerLetter.equalsIgnoreCase("b"))
                     {
                         cornerBXText.setText(String.valueOf(pClosest.x));
                        cornerBYText.setText(String.valueOf(pClosest.y));
                     }
                    if(cornerLetter.equalsIgnoreCase("c"))
                     {
                         cornerCXText.setText(String.valueOf(pClosest.x));
                        cornerCYText.setText(String.valueOf(pClosest.y));
                     }
                    if(cornerLetter.equalsIgnoreCase("d"))
                     {
                         cornerDXText.setText(String.valueOf(pClosest.x));
                        cornerDYText.setText(String.valueOf(pClosest.y));
                     }   
                 }
             }
             @Override
             public void mouseClicked(MouseEvent event) {}
             @Override
             public void mouseReleased(MouseEvent event) {}
             @Override
             public void mouseEntered(MouseEvent event) {}
             @Override
             public void mouseExited(MouseEvent event) {}
         };
         pictureLabel.addMouseListener(listener);
         framePoints.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         framePoints.getContentPane().add(pictureLabel);
         framePoints.pack();
         pictureLabel.setBackground(Color.black);
         pictureLabel.setOpaque(true);
         JScrollPane scrollBar=new JScrollPane(pictureLabel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                 JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
         framePoints.add(scrollBar);
         JOptionPane.showMessageDialog(framePoints, "Click on the four corners of the "
                 + "same square to fill in points A-D.", "Information", JOptionPane.INFORMATION_MESSAGE);
         framePoints.setTitle("Square Points and Control Points");
         framePoints.setLocation(350,0);
         framePoints.setSize(900,700);
         framePoints.setVisible(true);
     }
     
     private void onDoneClick() throws IOException
     {
         miscInfo.write("Points Filename: " + pointsText.getText());
         miscInfo.newLine();
         miscInfo.write("Image Height, Width: " + pointsImage.getHeight() + ", " 
                 + pointsImage.getWidth());
         miscInfo.newLine();
         miscInfo.write("CornerA (X,Y): " + Double.parseDouble(cornerAXText.getText())
                 + ", " + Double.parseDouble(cornerAYText.getText()));
         miscInfo.newLine();
         miscInfo.write("CornerB (X,Y): " + Double.parseDouble(cornerBXText.getText())
                 + ", " + Double.parseDouble(cornerBYText.getText()));
         miscInfo.newLine();
         miscInfo.write("CornerC (X,Y): " + Double.parseDouble(cornerCXText.getText())
                 + ", " + Double.parseDouble(cornerCYText.getText()));
         miscInfo.newLine();
         miscInfo.write("CornerD (X,Y): " + Double.parseDouble(cornerDXText.getText())
                 + ", " + Double.parseDouble(cornerDYText.getText()));
         miscInfo.newLine();
         miscInfo.write("Square Side Length: " + Double.parseDouble(sideLengthText.getText()));
         miscInfo.newLine();
         miscInfo.write("HFOV: " + Double.parseDouble(hfovText.getText()));
         miscInfo.newLine();
         miscInfo.write("Outline Filename: " + outlineText.getText());
         miscInfo.newLine();
         miscInfo.write("Image Height, Width: " + outlineImage.getHeight() + ", " 
                 + outlineImage.getWidth());
         miscInfo.newLine();
         
         //PIXEL LOCATIONS HERE (RED, YELLOW)
         for (Point p: redPoints)
         {
             controlPoints.write(p.x + ", " + p.y);
             controlPoints.newLine();
             redPointsCount++;
         }
         
         for (Point p: yellowPoints)
         {
             squareCorners.write(p.x + ", " + p.y);
             squareCorners.newLine();
             yellowPointsCount++;
         }
 
         //PIXEL LOCATIONS HERE (MAGENTA)
         for (Point p: magentaOutline)
         {
             outline.write(p.x + ", " + p.y);
             outline.newLine();
             magentaOutlineCount++;
         }
         
         miscInfo.close();
         controlPoints.close();
         squareCorners.close();
         outline.close();
         this.dispose();
         framePoints.dispose();
         frameOutline.dispose();
     }
 
     public static void main(String[] args) throws IOException
     {
         StreamGUI frameStreamGUI = new StreamGUI();
         frameStreamGUI.setTitle("StreamGUI");
         frameStreamGUI.setSize(350,260);
         frameStreamGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frameStreamGUI.setVisible(true);
     }
 }
