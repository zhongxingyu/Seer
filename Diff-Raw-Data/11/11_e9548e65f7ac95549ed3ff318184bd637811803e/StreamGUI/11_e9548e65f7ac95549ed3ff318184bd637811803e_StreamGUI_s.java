 /**
  *
  * @author Kyle Lusk
  */
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
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
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import streamoutlining.ImageMarker;
 import streamoutlining.ImageUtils;
 
 public class StreamGUI extends JFrame
 {
     final double SCALE_FACTOR = 5.0;
     
     private BufferedImage JPEGImage;
     private BufferedImage pointsImage;
     private BufferedImage outlineImage;
     BufferedImage allThreeShrunk;
     
     private final int SIDE_LENGTH = 8;
     private final int fieldWidth = 15;
     private String JPEGFileName;
     
     private ArrayList<ImageMarker> cornerAndControlMarkers;
     private ArrayList<ImageMarker> outlineMarkers;
     
     private File myfile;
     
     private JFileChooser chooser;
     
     private JFrame framePoints;
     private JPanel bottom;
     
     private JLabel pictureLabel;
     
     private JLabel JPGLabel;
     private JTextField JPEGText;
     private JButton JPEGBrowse;
     
    
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
         bottom = new JPanel();

         chooser = new JFileChooser();
         chooser.setCurrentDirectory(new File("."));
         FileFilter filter = new FileNameExtensionFilter("JPEG file", "jpg", "jpeg");
         chooser.addChoosableFileFilter(filter);
         //CREATES TEXT FIELD AND LABEL for POINTS BROWSE
         JPGLabel = new JLabel("JPG File: ");
         JPEGText = new JTextField(fieldWidth);
         
         //CREATES BROWSE BUTTON
         JPEGBrowse = new JButton("Browse");
         
         class pointsBrowseListener implements ActionListener
         {
             @Override
             public void actionPerformed(ActionEvent event)
             {
                 if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                 {
                     try 
                     {
                         JPEGImage = ImageIO.read(chooser.getSelectedFile());
                         myfile = chooser.getSelectedFile();
                         JPEGFileName = myfile.getPath();
                         String PNGOutlineFileName = JPEGFileName.replace(".JPG", "outline.png");
                         String PNGPointsFileName = JPEGFileName.replace(".JPG", "points.png");
                         
                         File outline = new File(PNGOutlineFileName);
                         File points = new File(PNGPointsFileName);
                         
                         JPEGText.setText(myfile.getName());
                         pointsImage = ImageIO.read(points);
                         outlineImage = ImageIO.read(outline);
                         
                         cornerAndControlMarkers = ImageUtils.extractColoredMarkers(pointsImage);
                         outlineMarkers = ImageUtils.extractColoredMarkers(outlineImage);
                         setupImageDisplayWindow(); 
                     } catch (IOException ex) 
                     {
                         System.out.println(ex);
                     }
                 }
             }
         }
         ActionListener fileName = new pointsBrowseListener();
         JPEGBrowse.addActionListener(fileName);
         
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
         bottom.add(JPGLabel);
         bottom.add(JPEGText);
         bottom.add(JPEGBrowse);
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
         
     private void setupImageDisplayWindow()
     {
         BufferedImage points = ImageUtils.overlay(pointsImage, outlineImage);
         BufferedImage allThree = ImageUtils.overlay(points, JPEGImage);
         allThreeShrunk = ImageUtils.makeShrunkImageCopy(allThree, SCALE_FACTOR);
         ImageUtils.enlargePoints(allThreeShrunk, cornerAndControlMarkers, 3, SCALE_FACTOR);
         ImageUtils.enlargePoints(allThreeShrunk, outlineMarkers, 0, SCALE_FACTOR);
         pictureLabel = new JLabel(new ImageIcon(allThreeShrunk));
         framePoints = new JFrame();
         MouseListener listener = new MouseListener() 
         { 
             @Override
             public void mousePressed(MouseEvent event) 
             {
                 try
                 {
                     Point pWindow = (event.getPoint());
                     Point pInOriginalImage = new Point((int)(pWindow.x*SCALE_FACTOR),(int)(pWindow.y*SCALE_FACTOR));
                     ImageMarker mClosest = ImageUtils.getNearestMarkerFromList(pInOriginalImage, cornerAndControlMarkers);
                     if(mClosest==null)
                     {
                         JOptionPane.showMessageDialog(framePoints, "Please click closer"
                                 + " to points.");
                     }
                     String markerID="";
                     if (mClosest.getType() == ImageMarker.MarkerType.CONTROL_POINT)
                     {
                         markerID = JOptionPane.showInputDialog("Please enter "
                                 + "the control point number.");
                         if (markerID != null)
                         {
                             for(ImageMarker m : cornerAndControlMarkers)
                             {
                                 if(markerID.equals(m.getID()))
                                 {
                                     markerID = JOptionPane.showInputDialog("Sorry,"
                                             + " control point already specified, enter"
                                             + " another.");
                                 }
                             }
                             mClosest.setID(markerID);
                         }
                     }else if(mClosest.getType() == ImageMarker.MarkerType.CORNER_POINT)
                     {
                         markerID = JOptionPane.showInputDialog("Please enter"
                                 + " the letter of the corner.");
                         if (markerID != null)
                         {
                             markerID = markerID.toUpperCase().trim();
                             for(ImageMarker m : cornerAndControlMarkers)
                             {
                                 if(markerID.equals(m.getID()))
                                 {
                                     markerID = JOptionPane.showInputDialog("Sorry,"
                                             + " corner already specified, enter another.");
                                     markerID = markerID.toUpperCase().trim();
                                 }
                             }
                             mClosest.setID(markerID);
                             if(markerID.equals("A"))
                             {
                                 cornerAXText.setText(String.valueOf(mClosest.getLocation().x + 0.5));
                                 cornerAYText.setText(String.valueOf(mClosest.getLocation().y + 0.5));
                             }
                             if(markerID.equals("B"))
                             {
                                 cornerBXText.setText(String.valueOf(mClosest.getLocation().x + 0.5));
                                 cornerBYText.setText(String.valueOf(mClosest.getLocation().y + 0.5));
                             }
                             if (markerID.equals("C")) 
                             {
                                 cornerCXText.setText(String.valueOf(mClosest.getLocation().x + 0.5));
                                 cornerCYText.setText(String.valueOf(mClosest.getLocation().y + 0.5));
                             }
                             if (markerID.equals("D")) 
                             {
                                 cornerDXText.setText(String.valueOf(mClosest.getLocation().x + 0.5));
                                 cornerDYText.setText(String.valueOf(mClosest.getLocation().y + 0.5));
                             }
                         }
                     }
                     if (markerID != null) 
                     {
                         Graphics2D g = allThreeShrunk.createGraphics();
                         g.setColor(Color.GRAY);
                         int centerx = (int) (mClosest.getLocation().x / SCALE_FACTOR);
                         int centery = (int) (mClosest.getLocation().y / SCALE_FACTOR);
                         g.fillOval(centerx - 2, centery - 2, 5, 5);
                         g.setColor(Color.BLACK);
                         g.drawString(markerID, centerx + 4, centery + 4);
                         g.drawString(markerID, centerx + 6, centery + 4);
                         g.drawString(markerID, centerx + 4, centery + 6);
                         g.drawString(markerID, centerx + 6, centery + 6);
                         g.setColor(Color.WHITE);
                         g.drawString(markerID, centerx + 5, centery + 5);
                         g.dispose();
                         pictureLabel.repaint();
                     }
                 }
                 catch(Exception ex)
                 {
                     System.out.println(ex);
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
         framePoints.setTitle("Square Points and Control Points");
         framePoints.setLocation(350,0);
         framePoints.setSize(945,710);
         framePoints.setVisible(true);
     }
     
     private void onDoneClick() throws IOException
     {
         for (ImageMarker marker: cornerAndControlMarkers)
         {
             if (marker.getID() == null)
             {
                 JOptionPane.showMessageDialog(this, "You can't be done until you've "
                         + "labeled every corner and control point!");
                 return;
             }
         }
         
         PrintWriter miscInfo = new PrintWriter(new FileWriter(JPEGFileName.replace(".JPG", "_miscInfo.txt")));
         PrintWriter outline = new PrintWriter(new FileWriter(JPEGFileName.replace(".JPG", "_outline.txt")));
         PrintWriter controlPoints = new PrintWriter(new FileWriter(JPEGFileName.replace(".JPG", "_controlpoints.txt")));
         PrintWriter squareCorners = new PrintWriter(new FileWriter(JPEGFileName.replace(".JPG", "_corners.txt")));
 
         miscInfo.println("JPG Filename: " + JPEGFileName);
 
         miscInfo.println("Image Height, Width: " + pointsImage.getHeight() + ", " 
                 + pointsImage.getWidth());
         miscInfo.println("CornerA (X,Y): " + cornerAXText.getText()
                 + ", " + cornerAYText.getText());
         miscInfo.println("CornerB (X,Y): " + cornerBXText.getText()
                 + ", " + cornerBYText.getText());
         miscInfo.println("CornerC (X,Y): " + cornerCXText.getText()
                 + ", " + cornerCYText.getText());
         miscInfo.println("CornerD (X,Y): " + cornerDXText.getText()
                 + ", " + cornerDYText.getText());
         
         miscInfo.println("Square Side Length: " + sideLengthText.getText());
         miscInfo.println("HFOV: " + hfovText.getText());
 
         // Output the four corner coordinates.
         squareCorners.println(cornerAXText.getText() + "," + cornerAYText.getText());
         squareCorners.println(cornerBXText.getText() + "," + cornerBYText.getText());
         squareCorners.println(cornerCXText.getText() + "," + cornerCYText.getText());
         squareCorners.println(cornerDXText.getText() + "," + cornerDYText.getText());
 
         // Output the control point IDs and coordinates
         for (ImageMarker marker: cornerAndControlMarkers)
         {
             if (marker.getType() == ImageMarker.MarkerType.CONTROL_POINT)
             {
                 controlPoints.println(marker.getID() + "," + (marker.getLocation().x + 0.5)
                         + "," + (marker.getLocation().y + 0.5));
             }
         }
         
         // Output the stream outline data
         for (ImageMarker marker: outlineMarkers)
         {
             if(marker.getType() == ImageMarker.MarkerType.OUTLINE_POINT)
             {
                 outline.println((marker.getLocation().x + 0.5) + "," + (marker.getLocation().y +0.5));
             }
         }
         miscInfo.close();
         controlPoints.close();
         squareCorners.close();
         outline.close();
         framePoints.setVisible(false);
         framePoints.dispose();
         this.setVisible(false);
         this.dispose();
     }
 
     public static void main(String[] args) throws IOException
     {
         StreamGUI frameStreamGUI = new StreamGUI();
         frameStreamGUI.setTitle("StreamGUI");
         frameStreamGUI.setSize(350,230);
         frameStreamGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frameStreamGUI.setVisible(true);
     }
 }
