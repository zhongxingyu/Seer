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
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import edu.cmu.cs.diamond.opendiamond.*;
 import edu.cmu.cs.openslide.OpenSlide;
 import edu.cmu.cs.openslide.gui.OpenSlideView;
 
 public class PathFind extends JFrame {
 
     private class OpenCaseAction extends AbstractAction {
         public OpenCaseAction() {
             super("Open Case...");
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             int returnVal = jfc.showDialog(PathFind.this, "Open");
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 File slide = jfc.getSelectedFile();
                 setSlide(slide);
             }
         }
     }
 
     private class DefineScopeAction extends AbstractAction {
         public DefineScopeAction() {
             super("Define Scope");
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             try {
                 cookieMap = CookieMap.createDefaultCookieMap();
             } catch (IOException e1) {
                 e1.printStackTrace();
             }
         }
     }
 
     private class CreateMacroAction extends AbstractAction {
 
         public CreateMacroAction() {
             super("New Macro...");
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             String enteredName = JOptionPane.showInputDialog(PathFind.this,
                     "Enter the name of the new macro:");
             if (enteredName == null) {
                 return;
             }
 
             String newName = enteredName.replace(" ", "_") + ".txt";
 
             try {
                 File newFile = new File(macrosDir, newName);
                 createNewMacro(newFile);
                 editMacro(newFile);
             } catch (IOException e1) {
                 // TODO Auto-generated catch block
                 e1.printStackTrace();
             }
 
             qp.populateMacroListModel();
         }
     }
 
     private static class ExitAction extends AbstractAction {
         public ExitAction() {
             super("Exit");
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             System.exit(0);
         }
     }
 
     private final SearchPanel searchPanel;
 
     private final JPanel selectionPanel;
 
     private final JList savedSelections;
 
     private final File macrosDir;
 
     private DefaultListModel ssModel;
 
     private final QueryPanel qp;
 
     private final PairedSlideView psv = new PairedSlideView();
 
     private CookieMap cookieMap;
 
     private final JFileChooser jfc = new JFileChooser();
 
     public PathFind(String ijDir, String extraPluginsDir, String jreDir,
             File slide) throws IOException {
         super("PathFind");
         setSize(1000, 750);
         setMinimumSize(new Dimension(1000, 500));
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         jfc.setAcceptAllFileFilterUsed(false);
         jfc.setFileFilter(OpenSlide.getFileFilter());
 
         cookieMap = CookieMap.createDefaultCookieMap();
 
         // slides in middle
         add(psv);
 
         // query bar at bottom
         macrosDir = new File(ijDir, "macros");
         qp = new QueryPanel(this, new File(ijDir), macrosDir, new File(
                 extraPluginsDir), new File(jreDir));
         add(qp, BorderLayout.SOUTH);
 
         // menubar
         setJMenuBar(createMenuBar());
 
         // search results at top
         searchPanel = new SearchPanel(this);
         searchPanel.setVisible(false);
         add(searchPanel, BorderLayout.NORTH);
 
         // save selections at left
         selectionPanel = new JPanel(new BorderLayout());
         savedSelections = new JList();
         savedSelections.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         savedSelections.setLayoutOrientation(JList.VERTICAL);
         selectionPanel.add(new JScrollPane(savedSelections,
                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
         selectionPanel.setBorder(BorderFactory
                 .createTitledBorder("Saved Selections"));
         selectionPanel.setPreferredSize(new Dimension(280, 100));
         savedSelections.addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent e) {
                 Shape selection = (Shape) savedSelections.getSelectedValue();
                 psv.getSlide().addSelection(selection);
                psv.getSlide().centerOnSelection();
             }
         });
         add(selectionPanel, BorderLayout.WEST);
 
         if (slide != null) {
             setSlide(slide);
         }
     }
 
     private void setSlide(File slide) {
         OpenSlide os = new OpenSlide(slide);
         setSlide(os, slide.getName());
     }
 
     void editMacro(final File macro) throws IOException {
         // read in macro
         FileInputStream in = new FileInputStream(macro);
         String text;
         try {
             text = new String(Util.readFully(in), "UTF-8");
         } finally {
             try {
                 in.close();
             } catch (IOException e) {
             }
         }
 
         // editor
         final JTextArea textArea = new JTextArea(text, 25, 80);
         textArea.setEditable(true);
         textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
 
         JScrollPane textPane = new JScrollPane(textArea);
         textPane
                 .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         textPane.setMinimumSize(new Dimension(10, 10));
 
         // top panel
         JPanel top = new JPanel();
         top.setLayout(new FlowLayout());
 
         // save
         JButton saveButton = new JButton("Save");
         top.add(saveButton);
 
         // delete
         JButton deleteButton = new JButton("Delete");
         top.add(deleteButton);
 
         // frame
         final JFrame editorFrame = new JFrame(macro.getName());
         editorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         editorFrame.add(textPane);
         editorFrame.add(top, BorderLayout.NORTH);
         editorFrame.pack();
         editorFrame.setVisible(true);
 
         // actions
         deleteButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (JOptionPane.showConfirmDialog(editorFrame,
                         "Really delete macro “" + macro.getName() + "”?",
                         "Confirm Deletion", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
                     editorFrame.dispose();
                     macro.delete();
                     qp.populateMacroListModel();
                 }
             }
         });
 
         saveButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 String text = textArea.getText();
                 try {
                     File tmp = File.createTempFile("pathfind", ".tmp",
                             macrosDir);
                     tmp.deleteOnExit();
 
                     // write out
                     FileWriter out = new FileWriter(tmp);
                     try {
                         out.write(text);
                     } finally {
                         try {
                             out.close();
                         } catch (IOException e1) {
                         }
                     }
                     tmp.renameTo(macro);
                     editorFrame.dispose();
                 } catch (IOException e1) {
                     e1.printStackTrace();
                 }
             }
         });
     }
 
     void createNewMacro(File newFile) throws IOException {
         // create a blank file if it doesn't exist
         if (!newFile.createNewFile()) {
             JOptionPane.showMessageDialog(PathFind.this, "Macro “"
                     + newFile.getName() + "” already exists.");
         }
     }
 
     private JMenuBar createMenuBar() {
         JMenuBar mb = new JMenuBar();
 
         JMenu m = new JMenu("PathFind");
         mb.add(m);
 
         m.add(new OpenCaseAction());
         m.add(new DefineScopeAction());
         m.add(new CreateMacroAction());
 
         m.add(new JSeparator());
         m.add(new ExitAction());
 
         return mb;
     }
 
     public void startSearch(int threshold, byte[] macroBlob, String macroName)
             throws IOException, InterruptedException {
         System.out.println("start search");
 
         SearchFactory factory = createFactory(threshold, macroBlob, macroName);
 
         searchPanel.beginSearch(factory.createSearch(null), factory);
     }
 
     public void stopSearch() throws InterruptedException {
         searchPanel.endSearch();
     }
 
     private SearchFactory createFactory(int threshold, byte[] macroBlob,
             String macroName) throws IOException {
         List<Filter> filters = new ArrayList<Filter>();
         String macroName2 = macroName.replace(' ', '_');
 
         InputStream in = null;
 
         // imagej
         try {
             in = new FileInputStream("/opt/snapfind/lib/fil_imagej_exec.so");
             FilterCode c = new FilterCode(in);
             List<String> dependencies = Collections.emptyList();
             List<String> arguments = Arrays.asList(new String[] { macroName2 });
             Filter imagej = new Filter("imagej", c, "f_eval_imagej_exec",
                     "f_init_imagej_exec", "f_fini_imagej_exec", threshold,
                     dependencies, arguments, macroBlob);
             filters.add(imagej);
         } finally {
             try {
                 in.close();
             } catch (IOException e) {
             }
         }
 
         try {
             in = new FileInputStream("/opt/snapfind/lib/fil_rgb.so");
             FilterCode c = new FilterCode(in);
             List<String> dependencies = Collections.emptyList();
             List<String> arguments = Collections.emptyList();
             Filter rgb = new Filter("RGB", c, "f_eval_img2rgb",
                     "f_init_img2rgb", "f_fini_img2rgb", 1, dependencies,
                     arguments);
             filters.add(rgb);
         } finally {
             try {
                 in.close();
             } catch (IOException e) {
             }
         }
 
         try {
             in = new FileInputStream("/opt/snapfind/lib/fil_thumb.so");
             FilterCode c = new FilterCode(in);
             List<String> dependencies = Arrays.asList(new String[] { "RGB" });
             List<String> arguments = Arrays
                     .asList(new String[] { "200", "150" });
             Filter thumb = new Filter("thumbnail", c, "f_eval_thumbnailer",
                     "f_init_thumbnailer", "f_fini_thumbnailer", 1,
                     dependencies, arguments, macroBlob);
             filters.add(thumb);
         } finally {
             try {
                 in.close();
             } catch (IOException e) {
             }
         }
 
         // make a new factory
         SearchFactory factory = new SearchFactory(filters, Arrays
                 .asList(new String[] { "RGB" }), cookieMap);
         return factory;
     }
 
     void setSlide(OpenSlide openslide, String title) {
         final OpenSlideView wv = createNewView(openslide, title, true);
 
         psv.setSlide(wv);
         wv.getInputMap()
                 .put(KeyStroke.getKeyStroke("INSERT"), "save selection");
         wv.getActionMap().put("save selection", new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 saveSelection(wv);
             }
         });
         ssModel = new SavedSelectionModel(wv);
         savedSelections.setModel(ssModel);
         savedSelections.setCellRenderer(new SavedSelectionCellRenderer(wv));
         psv.revalidate();
         psv.repaint();
     }
 
     void setResult(Icon result, String title) {
         psv.setResult(result);
     }
 
     private OpenSlideView createNewView(OpenSlide openslide, String title,
             boolean zoomToFit) {
         OpenSlideView wv = new OpenSlideView(openslide, zoomToFit);
         wv.setBorder(BorderFactory.createTitledBorder(title));
         return wv;
     }
 
     public static void main(String[] args) {
         if (args.length != 3 && args.length != 4) {
             System.out.println("usage: " + PathFind.class.getName()
                     + " ij_dir extra_plugins_dir jre_dir");
             return;
         }
 
         final String ijDir = args[0];
         final String extraPluginsDir = args[1];
         final String jreDir = args[2];
 
         final File slide;
         if (args.length == 4) {
             slide = new File(args[3]);
         } else {
             slide = null;
         }
 
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 PathFind pf;
                 try {
                     pf = new PathFind(ijDir, extraPluginsDir, jreDir, slide);
                     pf.setVisible(true);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         });
     }
 
     public BufferedImage getSelectionAsImage(int selection) {
        Shape s = psv.getSlide().getSelection();
         if (s == null) {
             return null;
         }
 
         Rectangle2D bb = s.getBounds2D();
         if (bb.getWidth() * bb.getHeight() > 6000 * 6000) {
             throw new SelectionTooBigException();
         }
 
         // move selection
         AffineTransform at = AffineTransform.getTranslateInstance(-bb.getX(),
                 -bb.getY());
         s = at.createTransformedShape(s);
 
         BufferedImage img = new BufferedImage((int) bb.getWidth(), (int) bb
                 .getHeight(), BufferedImage.TYPE_INT_RGB);
 
         Graphics2D g = img.createGraphics();
         g.setBackground(Color.WHITE);
         g.clearRect(0, 0, img.getWidth(), img.getHeight());
         g.clip(s);
         psv.getSlide().getOpenSlide().paintRegion(g, 0, 0, (int) bb.getX(),
                 (int) bb.getY(), img.getWidth(), img.getHeight(), 1.0);
         g.dispose();
 
         return img;
     }
 
     public OpenSlideView getSlide() {
         return psv.getSlide();
     }
 }
