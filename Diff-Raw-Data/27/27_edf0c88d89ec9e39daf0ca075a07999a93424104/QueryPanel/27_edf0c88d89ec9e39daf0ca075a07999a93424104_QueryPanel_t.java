 /*
  *  PathFind -- a Diamond system for pathology
  *
  *  Copyright (c) 2008-2009 Carnegie Mellon University
  *  All rights reserved.
  *
  *  PathFind is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, version 2.
  *
  *  PathFind is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with PathFind. If not, see <http://www.gnu.org/licenses/>.
  *
  *  Linking PathFind statically or dynamically with other modules is
  *  making a combined work based on PathFind. Thus, the terms and
  *  conditions of the GNU General Public License cover the whole
  *  combination.
  *
  *  In addition, as a special exception, the copyright holders of
  *  PathFind give you permission to combine PathFind with free software
  *  programs or libraries that are released under the GNU LGPL or the
  *  Eclipse Public License 1.0. You may copy and distribute such a system
  *  following the terms of the GNU GPL for PathFind and the licenses of
  *  the other code concerned, provided that you include the source code of
  *  that other code when and as the GNU GPL requires distribution of source
  *  code.
  *
  *  Note that people who make modified versions of PathFind are not
  *  obligated to grant this special exception for their modified versions;
  *  it is their choice whether to do so. The GNU General Public License
  *  gives permission to release a modified version without this exception;
  *  this exception also makes it possible to release a modified version
  *  which carries forward this exception.
  */
 
 package edu.cmu.cs.diamond.pathfind;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.*;
 
 import edu.cmu.cs.diamond.opendiamond.Util;
 
 public final class QueryPanel extends JPanel {
     public final String[] ijCmd;
 
     public final File ijDir;
 
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
                BufferedImage img = pf.getSelectionAsImage(0);
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
                 processArgs.addAll(Arrays.asList(ijCmd));
                 processArgs.add(imgFile.getPath());
                 processArgs.add("-batch");
                 processArgs.add(macroName);
                 ProcessBuilder pb = new ProcessBuilder(processArgs);
                 pb.directory(ijDir);
 
                 try {
                     StringBuilder sb = new StringBuilder();
                     Process p = pb.start();
                     BufferedInputStream pOut = new BufferedInputStream(p
                             .getInputStream());
                     int data;
                     while ((data = pOut.read()) != -1) {
                         sb.append((char) data);
                     }
 
                     pOut.close();
 
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
 
         public String getMacroName() {
             return macroName;
         }
     }
 
     private final PathFind pf;
 
     private final JComboBox macroComboBox;
 
     private final JLabel resultField;
 
     private double result = Double.NaN;
 
     private final JButton computeButton;
 
     private final JButton searchButton;
 
     private final JButton stopButton;
 
     private final JSpinner threshold;
 
     private final DefaultComboBoxModel macroListModel = new DefaultComboBoxModel();
 
     private final File extraPluginsDir;
 
     private final JButton editButton;
 
     private final File macrosDir;
 
     public QueryPanel(PathFind pathFind, File ijDir, File macrosDir,
             File extraPluginsDir, File jreDir) {
         this.ijDir = ijDir;
         this.macrosDir = macrosDir;
         this.extraPluginsDir = extraPluginsDir;
 
         macrosDir.mkdir();
 
         System.out.printf("macrosDir: %s\nextraPluginsDir: %s\n", macrosDir,
                 extraPluginsDir);
 
         populateMacroListModel();
 
         this.ijCmd = new String[] {
                 new File(jreDir + File.separator + "bin" + File.separator
                         + "java").getAbsolutePath(), "-jar", "ij.jar" };
 
         setLayout(new BorderLayout());
 
         pf = pathFind;
 
         Box b = Box.createHorizontalBox();
 
         // add macro list
         macroComboBox = new JComboBox(macroListModel);
         macroComboBox.setRenderer(new DefaultListCellRenderer() {
             @Override
             public Component getListCellRendererComponent(JList list,
                     Object value, int index, boolean isSelected,
                     boolean cellHasFocus) {
                 JLabel r = (JLabel) super.getListCellRendererComponent(list,
                         value, index, isSelected, cellHasFocus);
 
                 String name = ((File) value).getName().replace("_", " ")
                         .replaceAll("\\.txt$", "");
                 r.setText(name);
 
                 return r;
             }
         });
         b.add(macroComboBox);
 
         // edit
         editButton = new JButton("Edit");
         editButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 File f = (File) macroComboBox.getSelectedItem();
                 try {
                     pf.editMacro(f);
                 } catch (IOException e1) {
                     e1.printStackTrace();
                 }
             }
         });
         b.add(editButton);
 
         b.add(Box.createHorizontalStrut(10));
 
         // add compute button
         computeButton = new JButton("Calculate");
         computeButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 File f = (File) macroComboBox.getSelectedItem();
                 Macro m = new Macro(f.getName(), f.getAbsolutePath());
                 result = m.runMacro();
                 updateResultField();
             }
         });
         b.add(computeButton);
 
         b.add(Box.createHorizontalStrut(10));
 
         // add result
         resultField = new JLabel();
         resultField.setPreferredSize(new Dimension(100, 1));
         clearResult();
         b.add(resultField);
 
         // add divider
         b.add(new JSeparator(SwingConstants.VERTICAL));
 
         // add search range
         b.add(new JLabel("Threshold: "));
         threshold = new JSpinner(new SpinnerNumberModel(0, 0,
                 Integer.MAX_VALUE, 1));
         b.add(threshold);
         b.add(Box.createHorizontalStrut(10));
 
         // add search button
         searchButton = new JButton("Search");
         searchButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 try {
                     File f = (File) macroComboBox.getSelectedItem();
                     String name = f.getName().replaceAll("\\.txt$", "");
                     runRemoteMacro(name);
                 } catch (InterruptedException e1) {
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
                 try {
                     pf.stopSearch();
                 } catch (InterruptedException e1) {
                     Thread.currentThread().interrupt();
                 }
             }
         });
         b.add(stopButton);
 
         b.add(Box.createHorizontalGlue());
         add(b);
     }
 
     void populateMacroListModel() {
         macroListModel.removeAllElements();
 
         File[] files = macrosDir.listFiles(new FilenameFilter() {
             @Override
             public boolean accept(File dir, String name) {
                 return name.toLowerCase().endsWith(".txt");
             }
         });
 
         Collections.sort(Arrays.asList(files));
 
         for (File f : files) {
             macroListModel.addElement(f);
         }
     }
 
     private void updateResultField() {
         resultField.setText("Result: " + Double.toString(result));
     }
 
     public void clearResult() {
         result = Double.NaN;
         updateResultField();
     }
 
     private void runRemoteMacro(String macroName) throws InterruptedException,
             IOException {
         File mm = new File(QueryPanel.this.ijDir + "/macros", macroName
                 + ".txt");
         byte blob1[] = Util.quickTar(extraPluginsDir);
         byte blob2[] = Util.quickTar(new File[] { mm });
         byte macroBlob[] = new byte[blob1.length + blob2.length];
         System.arraycopy(blob1, 0, macroBlob, 0, blob1.length);
         System.arraycopy(blob2, 0, macroBlob, blob1.length, blob2.length);
         pf.startSearch((int) ((Number) threshold.getValue()).doubleValue(),
                 macroBlob, macroName);
     }
 }
