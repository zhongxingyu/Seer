 package edu.cmu.cs.diamond.pathfind;
 
 import java.awt.BorderLayout;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import javax.swing.*;
 
 import edu.cmu.cs.diamond.opendiamond.Util;
 
public final class QueryPanel extends JPanel {
     private static final String IJ_DIR = "/coda/coda.cs.cmu.edu/usr/agoode/ImageJ";
 
     private static final String[] IJ_CMD = {
             "/home/agoode/jre1.6.0_04/bin/java", "-jar", "ij.jar" };
 
     public class Macro {
         private final String name;
 
         private final String macroName;
 
         public Macro(String name, String macroName) {
             this.name = name;
             this.macroName = macroName;
         }
 
         @Override
         public String toString() {
             return name;
         }
 
         public double runMacro() {
             // make hourglass
             setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
             File imgFile = null;
             double result = Double.NaN;
 
             try {
                 // grab image
                 BufferedImage img = pf.getSelectionAsImage();
                 // JFrame jf = new JFrame();
                 // jf.add(new JLabel(new ImageIcon(img)));
                 // jf.setVisible(true);
                 // jf.pack();
 
                 // write tmp image
                 BufferedOutputStream out = null;
                 try {
                     imgFile = File.createTempFile("pathfind", ".ppm");
                     imgFile.deleteOnExit();
                     out = new BufferedOutputStream(
                             new FileOutputStream(imgFile));
 
                     out.write("P6\n".getBytes());
                     out.write(Integer.toString(img.getWidth()).getBytes());
                     out.write('\n');
                     out.write(Integer.toString(img.getHeight()).getBytes());
                     out.write("\n255\n".getBytes());
 
                     for (int y = 0; y < img.getHeight(); y++) {
                         for (int x = 0; x < img.getWidth(); x++) {
                             int pixel = img.getRGB(x, y);
                             out.write((pixel >> 16) & 0xFF);
                             out.write((pixel >> 8) & 0xFF);
                             out.write(pixel & 0xFF);
                         }
                     }
                 } catch (IOException e) {
                     e.printStackTrace();
                 } finally {
                     if (out != null) {
                         try {
                             out.close();
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                     }
                 }
 
                 // run macro
                 List<String> processArgs = new ArrayList<String>();
                 processArgs.addAll(Arrays.asList(IJ_CMD));
                 processArgs.add(imgFile.getPath());
                 processArgs.add("-batch");
                 processArgs.add(macroName);
                 ProcessBuilder pb = new ProcessBuilder(processArgs);
                 pb.directory(new File(IJ_DIR));
 
                 try {
                     StringBuilder sb = new StringBuilder();
                     Process p = pb.start();
                     BufferedInputStream pOut = new BufferedInputStream(p
                             .getInputStream());
                     int data;
                     while ((data = pOut.read()) != -1) {
                         sb.append((char) data);
                     }
 
                     String sr = sb.toString();
                     System.out.println(sr);
                     String srr[] = sr.split("\n");
                     result = Double.parseDouble(srr[2]);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             } finally {
                 // delete temp
                 if (imgFile != null) {
                     imgFile.delete();
                 }
 
                 // reset hourglass
                 setCursor(null);
             }
             return result / 10000.0;
         }
     }
 
     private final PathFind pf;
 
     private final JComboBox macroComboBox;
 
     private final JLabel resultField;
 
     private double result = Double.NaN;
 
     private final JButton computeButton;
 
     private final JButton searchButton;
 
     private final JButton stopButton;
 
     private final JSpinner searchBound;
 
     private final Macro macroList[] = createMacroList();
 
     public QueryPanel(PathFind pathFind) {
         setLayout(new BorderLayout());
 
         pf = pathFind;
 
         Box b = Box.createHorizontalBox();
 
         // add macro list
         // TODO: dynamic list
         macroComboBox = new JComboBox(macroList);
         b.add(macroComboBox);
         b.add(Box.createHorizontalStrut(10));
 
         // add result
         resultField = new JLabel();
         resultField.setPreferredSize(new Dimension(100, 1));
         clearResult();
         b.add(resultField);
 
         // add compute button
         computeButton = new JButton("Calculate");
         computeButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 result = macroList[macroComboBox.getSelectedIndex()].runMacro();
                 updateResultField();
             }
         });
         b.add(computeButton);
         b.add(Box.createHorizontalStrut(10));
 
         // add divider
         b.add(new JSeparator(SwingConstants.VERTICAL));
 
         // add search range
         b.add(new JLabel("Search bound: "));
         searchBound = new JSpinner(new SpinnerNumberModel(0.0, 0.0, null, 0.1));
         b.add(searchBound);
         b.add(Box.createHorizontalStrut(10));
 
         // add search button
         searchButton = new JButton("Search");
         searchButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 String m = macroList[macroComboBox.getSelectedIndex()].macroName;
                 File mm = new File(IJ_DIR + "/macros", m + ".txt");
                 byte macroBlob[];
                 try {
                     macroBlob = Util.readFully(new FileInputStream(mm));
                     pf.startSearch(Double.isNaN(result) ? 0.0 : result,
                             macroBlob);
                 } catch (FileNotFoundException e1) {
                     e1.printStackTrace();
                 } catch (IOException e1) {
                     e1.printStackTrace();
                 }
             }
         });
         b.add(searchButton);
 
         // add stop button
         stopButton = new JButton("Stop");
         stopButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 pf.stopSearch();
             }
         });
         b.add(stopButton);
 
         b.add(Box.createHorizontalGlue());
         add(b);
     }
 
     private Macro[] createMacroList() {
         List<Macro> r = new ArrayList<Macro>();
 
         BufferedReader in = new BufferedReader(new InputStreamReader(getClass()
                 .getResourceAsStream("resources/macros.txt")));
 
         try {
             String line;
             while ((line = in.readLine()) != null) {
                 line = line.trim();
                 StringTokenizer t = new StringTokenizer(line, ";");
                 r.add(new Macro(t.nextToken(), t.nextToken()));
             }
 
             in.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return r.toArray(new Macro[0]);
     }
 
     private void updateResultField() {
         resultField.setText("Result: " + Double.toString(result));
     }
 
     public void clearResult() {
         result = Double.NaN;
         updateResultField();
     }
 }
