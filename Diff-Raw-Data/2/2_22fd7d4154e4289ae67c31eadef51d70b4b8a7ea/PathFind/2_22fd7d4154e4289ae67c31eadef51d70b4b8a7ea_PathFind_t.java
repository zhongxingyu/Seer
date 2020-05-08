 /*
  *  PathFind -- a Diamond system for pathology
  *
  *  Copyright (c) 2008 Carnegie Mellon University
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
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Shape;
 import java.awt.event.ActionEvent;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.List;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import edu.cmu.cs.diamond.opendiamond.*;
 import edu.cmu.cs.wholeslide.Wholeslide;
 import edu.cmu.cs.wholeslide.gui.WholeslideView;
 
 public class PathFind extends JFrame {
 
     private final SearchPanel searchPanel;
 
     private final JPanel selectionPanel;
 
     private final JList savedSelections;
 
     private final Scope scope;
 
     private DefaultListModel ssModel;
 
     private final QueryPanel qp;
 
     private final PairedSlideView psv = new PairedSlideView();
 
     public PathFind(String filename, String ijDir, String extraPluginsDir,
             String jreDir, String trestleDir, String sqlHost) {
         super("PathFind");
         setSize(1000, 750);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         // slides in middle
         add(psv);
 
         // query bar at bottom
         qp = new QueryPanel(this, ijDir, extraPluginsDir, jreDir);
         add(qp, BorderLayout.SOUTH);
 
         // search results at top
         searchPanel = new SearchPanel(this, trestleDir, sqlHost);
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
                 psv.getLeftSlide().setSelection(selection);
                 psv.getLeftSlide().centerOnSelection();
             }
         });
         add(selectionPanel, BorderLayout.WEST);
 
         setLeftSlide(new Wholeslide(new File(filename)), filename);
 
         // load scope
         ScopeSource.commitScope();
         List<Scope> scopes = ScopeSource.getPredefinedScopeList();
         this.scope = scopes.get(0);
     }
 
     public void startSearch(double threshold, byte[] macroBlob) {
         System.out.println("start search");
 
         Search search = Search.getSharedInstance();
         // TODO fill in search parameters
         search.setScope(scope);
         search.setSearchlet(prepareSearchlet(threshold, macroBlob));
 
         searchPanel.beginSearch(search);
     }
 
     public void stopSearch() {
         searchPanel.endSearch();
     }
 
     private Searchlet prepareSearchlet(double threshold, byte[] macroBlob) {
         Filter imagej = null;
         try {
             FilterCode c = new FilterCode(new FileInputStream(
                     "/usr/share/imagejfind/filter/fil_imagej_exec.so"));
             imagej = new Filter("imagej", c, "f_eval_imagej_exec",
                     "f_init_imagej_exec", "f_fini_imagej_exec",
                     (int) (threshold * 10000), new String[] {},
                     new String[] {}, 400, macroBlob);
             System.out.println(imagej);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         // init diamond
         Search search = Search.getSharedInstance();
         search.setScope(scope);
 
         // make a new searchlet
         Searchlet searchlet = new Searchlet();
         searchlet.addFilter(imagej);
         searchlet.setApplicationDependencies(new String[] { "imagej" });
 
         return searchlet;
     }
 
     void setLeftSlide(Wholeslide wholeslide, String title) {
         final WholeslideView wv = createNewView(wholeslide, title, true);
 
         psv.setLeftSlide(wv);
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
     }
 
     void setRightSlide(Wholeslide wholeslide, String title) {
         if (wholeslide == null) {
             psv.setRightSlide(null);
         } else {
             psv.setRightSlide(createNewView(wholeslide, title, false));
         }
     }
 
     protected void saveSelection(WholeslideView wv) {
         Shape s = wv.getSelection();
         if (s != null) {
             ssModel.addElement(s);
         }
     }
 
     private WholeslideView createNewView(Wholeslide wholeslide, String title,
             boolean zoomToFit) {
         WholeslideView wv = new WholeslideView(wholeslide, zoomToFit);
         wv.setBorder(BorderFactory.createTitledBorder(title));
         return wv;
     }
 
     public static void main(String[] args) {
        if (args.length != 6) {
             System.out
                     .println("usage: "
                             + PathFind.class.getName()
                             + " filename ij_dir extra_plugins_dir jre_dir trestle-20x_dir sql_host");
             return;
         }
 
         String filename = args[0];
         String ijDir = args[1];
         String extraPluginsDir = args[2];
         String jreDir = args[3];
         String trestleDir = args[4];
         String sqlHost = args[5];
 
         PathFind pf = new PathFind(filename, ijDir, extraPluginsDir, jreDir,
                 trestleDir, sqlHost);
         pf.setVisible(true);
     }
 
     public BufferedImage getSelectionAsImage() {
         Shape s = psv.getLeftSlide().getSelection();
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
         psv.getLeftSlide().getWholeslide().paintRegion(g, 0, 0,
                 (int) bb.getX(), (int) bb.getY(), img.getWidth(),
                 img.getHeight(), 1.0);
         g.dispose();
 
         return img;
     }
 
     public WholeslideView getLeftSlide() {
         return psv.getLeftSlide();
     }
 
     public WholeslideView getRightSlide() {
         return psv.getRightSlide();
     }
 }
