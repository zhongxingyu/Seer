 /*
  * TeamBuilder.java
  *
  * Created on Apr 4, 2009, 3:53:56 PM
  *
  * This file is a part of Shoddy Battle.
  * Copyright (C) 2009  Catherine Fitzpatrick and Benjamin Gwin
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, visit the Free Software Foundation, Inc.
  * online at http://gnu.org.
  */
 
 package shoddybattleclient;
 
 import java.awt.Component;
 import java.awt.FileDialog;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.awt.event.ItemEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeWillExpandListener;
 import javax.swing.tree.ExpandVetoException;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 import shoddybattleclient.shoddybattle.*;
 import shoddybattleclient.shoddybattle.PokemonBox.PokemonWrapper;
 import shoddybattleclient.shoddybattle.Pokemon.Gender;
 import shoddybattleclient.utils.*;
 
 /**
  *
  * @author ben
  */
 public class TeamBuilder extends javax.swing.JFrame {
 
     private class SpritePanel extends JPanel {
         private int m_species = 0;
         private boolean m_shiny = false;
         private boolean m_front = true;
         private Image m_img = null;
         private Image m_background =
                 GameVisualisation.getImageFromResource("backgrounds/background2.png");
         private Image m_boxBackground =
                 GameVisualisation.getImageFromResource("backgrounds/background23.png");
         public SpritePanel() {
             MediaTracker tracker = new MediaTracker(null);
             tracker.addImage(m_background, 0);
             try {
                 tracker.waitForAll();
             } catch (Exception e) {
                 
             }
             addMouseListener(new MouseAdapter() {
                 public void mouseClicked(MouseEvent e) {
                     m_front = !m_front;
                     setSpecies(m_species, m_shiny, m_front);
                 }
             });
         }
         public void setSpecies(int species, boolean shiny, boolean front) {
             m_species = species;
             m_shiny = shiny;
             m_front = front;
             MediaTracker tracker = new MediaTracker(this);
             m_img = GameVisualisation.getSprite(species, m_front, true, m_shiny);
             tracker.addImage(m_img, WIDTH);
             try {
                 tracker.waitForAll();
             } catch (Exception e) {
                 
             }
             repaint();
         }
         public void setShiny(boolean shiny) {
             m_shiny = shiny;
             setSpecies(m_species, m_shiny, m_front);
         }
         @Override
         public void paintComponent(Graphics g) {
             super.paintComponent(g);
             if (tabForms.getSelectedComponent() instanceof BoxForm) {
                 g.drawImage(m_boxBackground, -130, -17, this);
             } else if(m_front) {
                 g.drawImage(m_background, -130, -17, this);
                 if (m_img == null) return;
                 g.drawImage(m_img, this.getWidth() / 2 - m_img.getWidth(this) / 2,
                         this.getHeight() - 10 - m_img.getHeight(this), this);
             } else {
                 g.drawImage(m_background, 0, getHeight() - m_background.getHeight(this), this);
                 if (m_img == null) return;
                 g.drawImage(m_img, 30, getHeight() - m_img.getHeight(this), this);
             }
         }
     }
 
     private List<TeamBuilderForm> m_forms = new ArrayList<TeamBuilderForm>();
     private Generation m_generation;
     private File m_save;
 
     private DefaultComboBoxModel m_speciesModel;
     
     // A hacky fix similar to TeamBuilderForm's hpProgramSelect
     // Set to true if we don't want cmbSpeciesItemStateChange clearing
     // EVs/IVs and other data on a tab switch
     private boolean speciesProgramSelect = false;
 
     /** Creates new form TeamBuilder */
     public TeamBuilder(Generation mod) {
         initComponents();
         m_generation = mod;
 
         ArrayList<PokemonSpecies> species = m_generation.getSpecies();
         m_speciesModel = new DefaultComboBoxModel(species.toArray(new PokemonSpecies[species.size()]));
         cmbSpecies.setModel(m_speciesModel);
         
         treeBox.setModel(new BoxTreeModel());
         treeBox.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
         treeBox.addTreeWillExpandListener(new TreeWillExpandListener() {
             public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
                 String root = (String)e.getPath().getPathComponent(1);
                 if (BoxTreeModel.isTeamRoot(root)) {
                     loadPokemonFromTeams();
                 } else if (BoxTreeModel.isBoxRoot(root)) {
                     loadPokemonFromBoxes();
                 }
             }
             public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException { }
         });
 
         addDefaultTeam();
     }
 
     private void addDefaultTeam() {
         m_forms.clear();
         tabForms.removeAll();
         for (int i = 0; i < 6; i++) {
             addDefaultForm();
         }
         setSpecies("Bulbasaur");
     }
     private void addDefaultForm() {
         TeamBuilderForm tbf = new TeamBuilderForm(this, m_forms.size());
         m_forms.add(tbf);
         ((CloseableTabbedPane)tabForms).addTab("", tbf, false);
         tbf.setPokemon(new Pokemon("Bulbasaur", "", false, Gender.GENDER_MALE, 100, 255,
             "", "", "", new String[] {null, null, null, null}, new int[] {3,3,3,3},
             new int[] {31,31,31,31,31,31}, new int[] {0,0,0,0,0,0}), true);
     }
 
     public PokemonSpecies getSpecies(String species) {
         for (PokemonSpecies sp : m_generation.getSpecies()) {
             if (sp.getName().equals(species)) {
                 return sp;
             }
         }
         return null;
     }
 
     public List<PokemonSpecies> getSpeciesList() {
         return m_generation.getSpecies();
     }
 
     public void updateTitle(int index, String title) {
         tabForms.setTitleAt(index, title);
     }
 
     public Generation getGeneration() {
         return m_generation;
     }
 
     private void saveTeam() {
         FileDialog choose = new FileDialog(this, "Save Team", FileDialog.SAVE);
         choose.setVisible(true);
         if (choose.getFile() == null) return;
         String file = choose.getDirectory() + choose.getFile();
         saveTeam(file);
         m_save = new File(file);
     }
 
     private void saveTeam(String location) {
         int dot = location.lastIndexOf('.');
         int slash = location.lastIndexOf(File.separatorChar);
         if (slash > dot) {
             // no extension - so supply the default one
             location += ".sbt";
         }
 
         int length = m_forms.size();
         StringBuffer buf = new StringBuffer();
         buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<shoddybattle>\n\n");
         for (int i = 0; i < length; i++) {
             Pokemon p = m_forms.get(i).getPokemon();
             buf.append(p.toXML());
             buf.append("\n");
         }
         buf.append("</shoddybattle>");
 
         try {
             Writer output = new PrintWriter(new FileWriter(location));
             output.write(new String(buf));
             output.flush();
             output.close();
 
             JOptionPane.showMessageDialog(null, "Team saved successfully",
                     "", JOptionPane.INFORMATION_MESSAGE);
         } catch (IOException e) {
             System.out.println("Failed to write team to file");
         }
     }
 
     private void setSpecies(String name) {
         // Boxes may interfere with setSpecies, so we prepare it beforehand
         scrTreeBox.setViewportView(treeBox);
         cmbSpecies.setModel(m_speciesModel);
         cmbSpecies.setEnabled(true);
 
         speciesProgramSelect = true;
         PokemonSpecies species = m_generation.getSpeciesByName(name);
         if (species != null) {
             ((SpritePanel)panelSprite).setSpecies(species.getId(), false, true);
 
             if (!cmbSpecies.getSelectedItem().equals(species)) {
                 cmbSpecies.setSelectedItem(species);
                 updateTree();
             }
         }
         speciesProgramSelect = false;
     }
 
     public void setSpriteShiny(boolean shiny) {
         String tab = m_forms.get(tabForms.getSelectedIndex()).getPokemon().toString();
         String current = ((PokemonSpecies)cmbSpecies.getSelectedItem()).getName();
         if (!tab.equals(current)) return;
         ((SpritePanel)panelSprite).setShiny(shiny);
     }
 
     public Pokemon getSelectedPokemon() {
         return m_forms.get(tabForms.getSelectedIndex()).getPokemon();
     }
 
     public void setSelectedPokemon(Pokemon poke) {
         setPokemonAt(tabForms.getSelectedIndex(), poke);
     }
 
     public void setPokemonAt(int index, Pokemon poke) {
         if (index < 0) return;
         if (index == tabForms.getSelectedIndex()) {
             if (!poke.toString().equals(getSelectedPokemon().toString()))
                 setSpecies(poke.toString());
             setSpriteShiny(poke.shiny);
         }
         m_forms.get(index).setPokemon(poke.clone(), true);
         updateTitle(index, poke.toString());
     }
 
     //updates the Tree by looking through our teams for any of the same pokemon
     private void loadPokemonFromTeams() {
         String species = ((PokemonSpecies)cmbSpecies.getSelectedItem()).getName();
         File dir = Preference.getTeamDirectory();
         FilenameFilter filter = new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.endsWith(".sbt");
             }
         };
         File[] teams = dir.listFiles(filter);
         TeamFileParser parser = new TeamFileParser();
         for (int i = 0; i < teams.length; i++) {
             Pokemon[] team = parser.parseTeam(teams[i].toString(), m_generation.getSpecies());
             if (team == null) continue;
             for (Pokemon p : team) {
                 if (p.species.equalsIgnoreCase(species)) {
                     ((BoxTreeModel)treeBox.getModel()).addTeamPokemon(p);
                 }
             }
         }
     }
 
     //updates the Tree by looking through the boxes for matching pokemon
     private void loadPokemonFromBoxes() {
         String species = ((PokemonSpecies)cmbSpecies.getSelectedItem()).getName();
         File dir = new File(Preference.getBoxLocation());
         if (!dir.exists()) return;
 
         //We need to look through all the boxes for matches of the same species
         for (File boxFolder : dir.listFiles()) {
             if (!boxFolder.isDirectory()) continue;
             try {
                 PokemonBox box = new PokemonBox(
                         boxFolder.getName(), species, m_generation.getSpecies());
                 if (box.getSize() > 0)
                     ((BoxTreeModel)treeBox.getModel()).addBox(box);
             } catch (Exception ex) {}
         }
     }
 
     private void updateBoxes(boolean updateTree) {
         Component last = tabForms.getComponentAt(tabForms.getTabCount() - 1);
         if (last instanceof BoxForm) {
             ((BoxForm)last).updateBoxes();
         }
 
         if (updateTree) {
             updateTree();
         }
     }
 
     private void updateTree() {
         if (!(treeBox.getModel() instanceof BoxTreeModel)) {
             treeBox.setModel(new BoxTreeModel());
             return;
         }
         
         BoxTreeModel model = (BoxTreeModel)treeBox.getModel();
         boolean teamRootCollapsed = treeBox.isCollapsed(model.getTeamPath());
         boolean boxRootCollapsed = treeBox.isCollapsed(model.getBoxPath());
         TreePath treePath = treeBox.getSelectionPath();
 
         treeBox.setModel(new BoxTreeModel());
         if (treePath != null && treePath.getPathCount() > 1) {
             Object[] path = treePath.getPath();
             treeBox.setSelectionPath(
                     new TreePath(model.getRoot()).pathByAddingChild(path[1]));
         }
 
         if (!teamRootCollapsed) treeBox.expandPath(model.getTeamPath());
         if (!boxRootCollapsed) treeBox.expandPath(model.getBoxPath());
     }
 
     // This will ask the user for input, returns null on fail, or the resultant pokemon if it succeeded
     public PokemonWrapper addPokemonToBox(PokemonBox box, Pokemon poke) {
         String name = JOptionPane.showInputDialog(this, "New Pokemon's name:");
         if (name == null || name.trim().equals("")) {
             return null;
         }
 
         if (box.getPokemon(name) != null) {
             int confirm = JOptionPane.showConfirmDialog(this, "This Pokemon already exists, are " +
                     "you sure you want to replace it?", "", JOptionPane.YES_NO_OPTION);
             if (confirm != JOptionPane.YES_OPTION) {
                 return null;
             }
         }
 
         try {
             name = name.trim();
             box.removePokemon(name); //May be a rename
             box.addPokemon(name, poke);
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(this, "Error adding pokemon", "Error",
                 JOptionPane.ERROR_MESSAGE);
         }
 
         return box.getPokemon(name);
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         tabForms = new CloseableTabbedPane();
         cmbSpecies = new javax.swing.JComboBox();
         scrTreeBox = new javax.swing.JScrollPane();
         treeBox = new javax.swing.JTree();
         btnLoadFromBox = new javax.swing.JButton();
         btnSaveToBox = new javax.swing.JButton();
         panelSprite = new SpritePanel();
         jMenuBar1 = new javax.swing.JMenuBar();
         jMenu1 = new javax.swing.JMenu();
         menuNew = new javax.swing.JMenuItem();
         menuLoad = new javax.swing.JMenuItem();
         menuSave = new javax.swing.JMenuItem();
         menuSaveAs = new javax.swing.JMenuItem();
         jSeparator1 = new javax.swing.JSeparator();
         menuImportBox = new javax.swing.JMenuItem();
         menuExportBox = new javax.swing.JMenuItem();
         menuDeleteBox = new javax.swing.JMenuItem();
         jSeparator2 = new javax.swing.JSeparator();
         menuExport = new javax.swing.JMenuItem();
         jMenu3 = new javax.swing.JMenu();
         mnuHappiness = new javax.swing.JMenuItem();
         menuChangeSize = new javax.swing.JMenuItem();
         jMenu2 = new javax.swing.JMenu();
         menuFront = new javax.swing.JMenuItem();
         menuRandomise = new javax.swing.JMenuItem();
         menuBox = new javax.swing.JMenuItem();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
         setTitle("Shoddy Battle - Team Builder");
         setLocationByPlatform(true);
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 formWindowClosing(evt);
             }
         });
 
         tabForms.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 tabFormsStateChanged(evt);
             }
         });
 
         cmbSpecies.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 cmbSpeciesItemStateChanged(evt);
             }
         });
 
         javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
         treeBox.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
         treeBox.setRootVisible(false);
         scrTreeBox.setViewportView(treeBox);
 
         btnLoadFromBox.setText("Load >>");
         btnLoadFromBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnLoadFromBoxActionPerformed(evt);
             }
         });
 
         btnSaveToBox.setText("Save <<");
         btnSaveToBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSaveToBoxActionPerformed(evt);
             }
         });
 
         panelSprite.setBackground(new java.awt.Color(255, 255, 255));
         panelSprite.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));
         panelSprite.setMaximumSize(new java.awt.Dimension(80, 80));
 
         org.jdesktop.layout.GroupLayout panelSpriteLayout = new org.jdesktop.layout.GroupLayout(panelSprite);
         panelSprite.setLayout(panelSpriteLayout);
         panelSpriteLayout.setHorizontalGroup(
             panelSpriteLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 117, Short.MAX_VALUE)
         );
         panelSpriteLayout.setVerticalGroup(
             panelSpriteLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 81, Short.MAX_VALUE)
         );
 
         jMenu1.setText("File");
 
         menuNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
         menuNew.setText("New Team");
         menuNew.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuNewActionPerformed(evt);
             }
         });
         jMenu1.add(menuNew);
 
         menuLoad.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
         menuLoad.setText("Load Team");
         menuLoad.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuLoadActionPerformed(evt);
             }
         });
         jMenu1.add(menuLoad);
 
         menuSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
         menuSave.setText("Save");
         menuSave.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuSaveActionPerformed(evt);
             }
         });
         jMenu1.add(menuSave);
 
         menuSaveAs.setText("Save As...");
         menuSaveAs.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuSaveAsActionPerformed(evt);
             }
         });
         jMenu1.add(menuSaveAs);
         jMenu1.add(jSeparator1);
 
         menuImportBox.setText("Import Boxes");
         menuImportBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuImportBoxActionPerformed(evt);
             }
         });
         jMenu1.add(menuImportBox);
 
         menuExportBox.setText("Export Boxes");
         menuExportBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuExportBoxActionPerformed(evt);
             }
         });
         jMenu1.add(menuExportBox);
 
         menuDeleteBox.setText("Delete Boxes");
         menuDeleteBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuDeleteBoxActionPerformed(evt);
             }
         });
         jMenu1.add(menuDeleteBox);
         jMenu1.add(jSeparator2);
 
         menuExport.setText("Export to Text");
         menuExport.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuExportActionPerformed(evt);
             }
         });
         jMenu1.add(menuExport);
 
         jMenuBar1.add(jMenu1);
 
         jMenu3.setText("Edit");
 
         mnuHappiness.setText("Set Happiness");
         mnuHappiness.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuHappinessActionPerformed(evt);
             }
         });
         jMenu3.add(mnuHappiness);
 
         menuChangeSize.setText("Change Team Size");
         menuChangeSize.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuChangeSizeActionPerformed(evt);
             }
         });
         jMenu3.add(menuChangeSize);
 
         jMenuBar1.add(jMenu3);
 
         jMenu2.setText("Tools");
 
         menuFront.setText("Move to Front");
         menuFront.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuFrontActionPerformed(evt);
             }
         });
         jMenu2.add(menuFront);
 
         menuRandomise.setText("Randomise Team");
         jMenu2.add(menuRandomise);
 
         menuBox.setText("Open Box");
         menuBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuBoxActionPerformed(evt);
             }
         });
         jMenu2.add(menuBox);
 
         jMenuBar1.add(jMenu2);
 
         setJMenuBar(jMenuBar1);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(layout.createSequentialGroup()
                         .add(14, 14, 14)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(org.jdesktop.layout.GroupLayout.TRAILING, btnLoadFromBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 121, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(org.jdesktop.layout.GroupLayout.TRAILING, scrTreeBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 121, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(org.jdesktop.layout.GroupLayout.TRAILING, btnSaveToBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 121, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(panelSprite, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                     .add(layout.createSequentialGroup()
                         .addContainerGap()
                         .add(cmbSpecies, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 129, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(tabForms, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 728, Short.MAX_VALUE)
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(tabForms, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
                     .add(layout.createSequentialGroup()
                         .add(cmbSpecies, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(panelSprite, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                         .add(scrTreeBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(btnLoadFromBox)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(btnSaveToBox)
                         .add(88, 88, 88)))
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
         this.dispose();
     }//GEN-LAST:event_formWindowClosing
 
     private void menuLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuLoadActionPerformed
         FileDialog choose = new FileDialog(this, "Load Team", FileDialog.LOAD);
         choose.setVisible(true);
         if (choose.getFile() == null) return;
         String file = choose.getDirectory() + choose.getFile();
         if (file == null || !(new File(file).exists())) return;
 
         TeamFileParser tfp = new TeamFileParser();
         Pokemon[] team = tfp.parseTeam(file, m_generation.getSpecies());
 
         if (team == null) {
             JOptionPane.showMessageDialog(null, "Error reading file",
                 "Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
         
         m_forms.clear();
         tabForms.removeAll();
 
         int nPokemon = Math.min(team.length, 24);
         for (int i = 0; i < nPokemon; i++) {
             m_forms.add(new TeamBuilderForm(this, i));
             ((CloseableTabbedPane)tabForms).addTab("", m_forms.get(i), false);
             m_forms.get(i).setPokemon(team[i], true);
         }
         setSpecies(team[0].species);
         setSpriteShiny(team[0].shiny);
 
         m_save = new File(file);
 }//GEN-LAST:event_menuLoadActionPerformed
 
     private void menuSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveActionPerformed
         if (m_save == null) {
             saveTeam();
         } else {
             saveTeam(m_save.getAbsolutePath());
         }
 }//GEN-LAST:event_menuSaveActionPerformed
 
     private void menuSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveAsActionPerformed
         saveTeam();
     }//GEN-LAST:event_menuSaveAsActionPerformed
 
     private void menuChangeSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuChangeSizeActionPerformed
         String str = JOptionPane.showInputDialog(this, "Enter a new size for this team", m_forms.size());
         int size = 0;
         try {
             size = Integer.parseInt(str.trim());
         } catch (Exception e) {
             return;
         }
 
         if (size < 1) {
             return;
         } else if (size > 24) {
             JOptionPane.showMessageDialog(null, "Cannot use a size larger than 24 pokemon.",
                     "Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         //If the last tab is a box, pick it up and move it to the end
         Component last = tabForms.getComponentAt(tabForms.getTabCount() - 1);
 
         if (size > m_forms.size()) {
             if (last instanceof BoxForm) {
                 tabForms.remove(tabForms.getTabCount() - 1);
             }
 
             while (size > m_forms.size()) {
                 addDefaultForm();
             }
 
             if (last instanceof BoxForm) {
                 tabForms.addTab("Boxes", last);
             }
         } else {
             while (m_forms.size() > size) {
                 int idx = m_forms.size() - 1;
                 m_forms.remove(idx);
                 tabForms.remove(idx);
             }
         }
     }//GEN-LAST:event_menuChangeSizeActionPerformed
 
     private void cmbSpeciesItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbSpeciesItemStateChanged
         if (evt.getStateChange() != ItemEvent.SELECTED) return;
         if (speciesProgramSelect) return;
         PokemonSpecies sp = (PokemonSpecies)cmbSpecies.getSelectedItem();
         if (sp == null) return;
         ((SpritePanel)panelSprite).setSpecies(sp.getId(), false, true);
 
         //If the species has no gender or is only female, GENDER_MALE is ignored
         TeamBuilderForm tbf = (TeamBuilderForm)tabForms.getSelectedComponent();
         tbf.setPokemon(new Pokemon(sp.getName(), "", false, Gender.GENDER_MALE, 100, 255,
             "", "", "", new String[] {null, null, null, null}, new int[] {3,3,3,3},
             new int[] {31,31,31,31,31,31}, new int[] {0,0,0,0,0,0}), false);
 
         updateTree();
     }//GEN-LAST:event_cmbSpeciesItemStateChanged
 
     private void btnLoadFromBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadFromBoxActionPerformed
         Component selectedTab = tabForms.getSelectedComponent();
         if (selectedTab instanceof BoxForm) {
             BoxForm boxForm = (BoxForm)selectedTab;
             JList teamList = (JList)scrTreeBox.getViewport().getView();
             
             PokemonBox box = boxForm.getSelectedBox();
             Object selected = teamList.getSelectedValue();
 
             if (box == null || selected == null) {
                 return;
             }
 
             Pokemon poke = (Pokemon)selected;
             addPokemonToBox(box, poke);
             updateBoxes(false);
 
         } else {
             Object obj = treeBox.getLastSelectedPathComponent();
             if (obj == null) return;
             if (BoxTreeModel.isDefaultNode(obj)) {
                 PokemonSpecies sp = (PokemonSpecies)cmbSpecies.getSelectedItem();
                 ((TeamBuilderForm)tabForms.getSelectedComponent()).setPokemon(new Pokemon(
                     sp.getName(), "", false, Gender.GENDER_MALE, 100, 255, "", "", "",
                     new String[] {null, null, null, null}, new int[] {3,3,3,3},
                     new int[] {31,31,31,31,31,31}, new int[] {0,0,0,0,0,0}), false);
             } else if (obj instanceof Pokemon) {
                 setSelectedPokemon((Pokemon)obj);
             } else if (obj instanceof PokemonWrapper) {
                 setSelectedPokemon(((PokemonWrapper)obj).pokemon);
             }
         }
     }//GEN-LAST:event_btnLoadFromBoxActionPerformed
 
     private void mnuHappinessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuHappinessActionPerformed
         int selected = tabForms.getSelectedIndex();
         if (selected >= m_forms.size()) return;
 
         int happiness = ((TeamBuilderForm)tabForms.getSelectedComponent()).getHappiness();
         String resp = JOptionPane.showInputDialog(this, "Enter a new value in [0,255]", happiness);
         try {
             happiness = Integer.valueOf(resp);
             if ((happiness >= 0) && (happiness <= 255)) {
                 ((TeamBuilderForm)tabForms.getSelectedComponent()).setHappiness(happiness);
             }
         } catch (NumberFormatException e) {
             
         }
     }//GEN-LAST:event_mnuHappinessActionPerformed
 
     private void tabFormsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabFormsStateChanged
         int idx = tabForms.getSelectedIndex();
         if (idx < 0) return;
 
         if (idx < m_forms.size()) {
             Pokemon poke = m_forms.get(idx).getPokemon();
             if (poke == null) return;
             setSpecies(poke.toString());
         } else {
             cmbSpecies.setModel(new DefaultComboBoxModel());
             cmbSpecies.setEnabled(false);
             
             BoxForm form = (BoxForm)tabForms.getSelectedComponent();
             scrTreeBox.setViewportView(form.getTeamList(m_forms));
 
             panelSprite.repaint();
         }
 
     }//GEN-LAST:event_tabFormsStateChanged
 
     private void menuNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuNewActionPerformed
         int result = JOptionPane.showOptionDialog(null,
                  "This team may have unsaved changes, create new anyways?",
                  "Unsaved Changes", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
         if (result == JOptionPane.OK_OPTION) {
             addDefaultTeam();
            m_save = null;
         }
     }//GEN-LAST:event_menuNewActionPerformed
 
     private void menuExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuExportActionPerformed
         StringBuilder buf = new StringBuilder();
         for (int i = 0; i < m_forms.size(); i++) {
             Pokemon p = m_forms.get(i).getPokemon();
             buf.append(p.toTeamText());
             buf.append("\n---\n");
         }
         new TextDialog(this, buf.toString());
     }//GEN-LAST:event_menuExportActionPerformed
 
     private void menuFrontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuFrontActionPerformed
         int selected = tabForms.getSelectedIndex();
         if (selected < 1 || selected >= m_forms.size()) {
             return;
         }
 
         TeamBuilderForm temp = m_forms.get(selected);
         m_forms.set(selected, m_forms.get(0));
         m_forms.set(0, temp);
 
         Component last = tabForms.getComponentAt(tabForms.getTabCount() - 1);
         tabForms.removeAll();
         for (TeamBuilderForm tab : m_forms) {
             ((CloseableTabbedPane)tabForms).addTab(tab.getPokemon().toString(), tab, false);
         }
 
         if (last instanceof BoxForm) {
             tabForms.add("Boxes", last);
         }
     }//GEN-LAST:event_menuFrontActionPerformed
 
     private void menuBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuBoxActionPerformed
         Component last = tabForms.getComponentAt(tabForms.getTabCount() - 1);
         if (!(last instanceof BoxForm))
             tabForms.addTab("Boxes", new BoxForm(this));
         tabForms.setSelectedIndex(tabForms.getTabCount() - 1);
     }//GEN-LAST:event_menuBoxActionPerformed
 
     private void btnSaveToBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveToBoxActionPerformed
         Component selectedTab = tabForms.getSelectedComponent();
         if (selectedTab instanceof BoxForm) {
             // Boxes is selected
             BoxForm boxForm = (BoxForm)selectedTab;
             JList teamList = (JList)scrTreeBox.getViewport().getView();
 
             Pokemon poke = boxForm.getSelectedPokemon();
             int selectedTeamIndex = teamList.getSelectedIndex();
 
             if (poke == null || selectedTeamIndex < 0 || selectedTeamIndex >= m_forms.size()) {
                 return;
             }
 
             setPokemonAt(selectedTeamIndex, poke.clone());
             ((DefaultListModel)teamList.getModel()).set(selectedTeamIndex, poke);
 
         } else {
             // A TeamBuilderForm is selected
             BoxTreeModel treeModel = (BoxTreeModel)treeBox.getModel();
             TreePath path = treeBox.getSelectionPath();
 
             if (path == null || BoxTreeModel.isBoxRoot(path.getLastPathComponent())) {
                 ArrayList<String> boxes = new ArrayList<String>();
                 String newBox = "<html><i>New Box</i> ";
                 for (File boxFile : new File(Preference.getBoxLocation()).listFiles()) {
                     if (boxFile.isDirectory())
                         boxes.add(boxFile.getName());
                 }
                 Collections.sort(boxes);
                 boxes.add(0, newBox);
 
                 Object selection = JOptionPane.showInputDialog(this, "Select a box", "Save to Box",
                         JOptionPane.PLAIN_MESSAGE, null, boxes.toArray(), boxes.get(0));
                 if (selection == null) return;
 
                 if (selection.equals(newBox)) { //No system allows a foldername like newBox's
                     String boxName = JOptionPane.showInputDialog(this, "New box name:");
                     if (boxName == null) return;
                     if (new File(Preference.getBoxLocation() + File.separatorChar + boxName).exists()) {
                         JOptionPane.showMessageDialog(this, "This box already exists", "Error",
                                 JOptionPane.ERROR_MESSAGE);
                         return;
                     }
                     selection = boxName;
                 }
                 
                 PokemonBox box = new PokemonBox((String)selection, 
                         getSelectedPokemon().toString(), m_generation.getSpecies());
                 PokemonWrapper poke = addPokemonToBox(box, getSelectedPokemon());
                 if (poke != null) {
                     treeModel.addBoxPokemon(poke);
                 }
             } else if (path.getLastPathComponent() instanceof PokemonWrapper) {
                 PokemonWrapper wrapper = (PokemonWrapper)path.getLastPathComponent();
                 String name = wrapper.name;
                 PokemonBox box = wrapper.getParent();
                 int confirm = JOptionPane.showConfirmDialog(this, "Are you sure "
                         + "you want to replace " + name + "?", "", JOptionPane.YES_NO_OPTION);
                 if (confirm != JOptionPane.YES_OPTION) {
                     return;
                 }
                 try {
                     box.removePokemon(name);
                     box.addPokemon(name, getSelectedPokemon());
                     treeModel.addBoxPokemon(box.getPokemon(name));
 
                     //This is needed or immediately clicking Load afterwards creates a bug
                     treeBox.setSelectionPath(path.getParentPath().pathByAddingChild(box.getPokemon(name)));
                 } catch (Exception ex) {
                     JOptionPane.showMessageDialog(this, "Error adding pokemon", "Error",
                         JOptionPane.ERROR_MESSAGE);
                 }
             } else {
                 return;
             }
 
             updateBoxes(false);
         }
     }//GEN-LAST:event_btnSaveToBoxActionPerformed
 
     private void menuImportBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuImportBoxActionPerformed
         FileDialog choose = new FileDialog(this, "Import Boxes", FileDialog.LOAD);
         // todo: make it obvious these are zips with better file choosers
         choose.setVisible(true);
         if (choose.getFile() == null) return;
         File inFile = new File(choose.getDirectory() + choose.getFile());
         if (!inFile.exists()) return;
 
         // These are for overwriting pokemon.
         boolean asked = false; // Has the user been asked if overriding pokemon is ok?
         boolean okOverwrite = false; // Is it ok to overwrite?
 
         try {
             ZipFile file = new ZipFile(inFile);
 
             Enumeration<? extends ZipEntry> entries = file.entries();
             while(entries.hasMoreElements()) {
                 ZipEntry entry = entries.nextElement();
                 String[] path = entry.toString().split("[/\\\\]");
                 if (path.length > 2) {
                     continue;
                 }
 
                 if (path.length == 1 && entry.isDirectory()) {
                     //Its a box
                     File box = new File(Preference.getBoxLocation() + "/" + entry.toString());
                     if (!box.exists())
                         box.mkdirs();
                 } else if (path.length == 2 && !entry.isDirectory()) {
                     //Its a pokemon
                     File pokemon = new File(Preference.getBoxLocation() + "/" + entry.toString());
 
                     if (pokemon.exists()) {
                         if (!asked) {
                             int result = JOptionPane.showConfirmDialog(this,
                                     "There are some pokemon conflicts. Ok to overwrite them?",
                                     "", JOptionPane.YES_NO_OPTION);
                             if (result == JOptionPane.YES_OPTION)
                                 okOverwrite = true;
                             asked = true;
                         }
                         
                         if (asked && !okOverwrite) {
                             continue;
                         }
                     }
 
                     InputStream in = file.getInputStream(entry);
                     byte[] data = new byte[in.available()];
                     in.read(data);
                     in.close();
 
                     FileOutputStream out = new FileOutputStream(pokemon, false);
                     out.write(data);
                     out.flush();
                     out.close();
                 }
             }
             
             file.close();
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(this, "Error importing boxes",
                                             "Error", JOptionPane.ERROR_MESSAGE);
         }
 
         updateBoxes(true);
     }//GEN-LAST:event_menuImportBoxActionPerformed
 
     private void menuExportBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuExportBoxActionPerformed
         FileDialog choose = new FileDialog(this, "Export Boxes", FileDialog.SAVE);
         choose.setVisible(true);
         if (choose.getFile() == null) return;
         File outFile = new File(choose.getDirectory() + choose.getFile());
 
         try {
             ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFile));
             File boxFolder = new File(Preference.getBoxLocation());
             for (File box : boxFolder.listFiles()) {
                 if (!box.isDirectory()) continue;
 
                 // Write the box folder
                 out.putNextEntry(new ZipEntry(box.getName() + "/"));
                 out.closeEntry();
 
                 // Write the pokemon
                 for (File pokemon : box.listFiles()) {
                     if (pokemon.isDirectory()) return;
                     
                     FileInputStream in = new FileInputStream(pokemon);
                     byte[] data = new byte[in.available()];
                     in.read(data);
                     in.close();
 
                     out.putNextEntry(new ZipEntry(box.getName() + "/" + pokemon.getName()));
                     out.write(data);
                     out.closeEntry();
                 }
             }
 
             out.finish();
             out.close();
 
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(this, "Error exporting boxes",
                                             "Error", JOptionPane.ERROR_MESSAGE);
         }
     }//GEN-LAST:event_menuExportBoxActionPerformed
 
     private void menuDeleteBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuDeleteBoxActionPerformed
         int result = JOptionPane.showConfirmDialog(this,
                 "Once you delete boxes they're gone forever, are you sure you want to continue?",
                 "", JOptionPane.YES_NO_OPTION);
 
         if (result == JOptionPane.NO_OPTION) return;
 
         // Variables to give an indication of a serious problem
         boolean badFormat = false;
         boolean deleteFailed = false;
 
         File boxFolder = new File(Preference.getBoxLocation());
         for (File box : boxFolder.listFiles()) {
             if (!box.isDirectory()) {
                 continue;
             }
 
             for (File pokemon : box.listFiles()) {
                 if (pokemon.isDirectory()) {
                     if (!pokemon.delete()) {
                         badFormat = true;
                     }
                 } else if (!pokemon.delete()) {
                     deleteFailed = true;
                 }
             }
 
             if (!box.delete()) {
                 deleteFailed = true;
             }
         }
 
         if (badFormat) {
             JOptionPane.showMessageDialog(this,
                     "The boxes are badly formatted, so some files couldn't be deleted",
                     "Error", JOptionPane.ERROR_MESSAGE);
         } else if (deleteFailed) {
             JOptionPane.showMessageDialog(this, "Some files couldn't be deleted",
                     "Error", JOptionPane.ERROR_MESSAGE);
         } else {
             JOptionPane.showMessageDialog(this, "Boxes deleted");
         }
 
         updateBoxes(true);
     }//GEN-LAST:event_menuDeleteBoxActionPerformed
 
     /**
     * @param args the command line arguments
     */
     public static void main(String args[]) {
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 try {
                     UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                 } catch (Exception e) {}
                 
                 long t1 = System.currentTimeMillis();
                 MoveListParser mlp = new MoveListParser();
                 ArrayList<PokemonMove> moves = mlp.parseDocument(
                         TeamBuilder.class.getResource("resources/moves.xml").toString());
                 long t2 = System.currentTimeMillis();
                 SpeciesListParser parser = new SpeciesListParser();
                 ArrayList<PokemonSpecies> species = parser.parseDocument(
                         TeamBuilder.class.getResource("resources/species.xml").toString());
                 long t3 = System.currentTimeMillis();
                 ArrayList<String> items = new ArrayList<String>();
                 items.add("Leftovers");
                 Generation mod = new Generation(species, moves, items);
                 System.out.println("Loaded moves in " + (t2-t1) + " milliseconds");
                 System.out.println("Loaded species in " + (t3-t2) + " milliseconds");
 
                 new TeamBuilder(mod).setVisible(true);
             }
         });
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnLoadFromBox;
     private javax.swing.JButton btnSaveToBox;
     private javax.swing.JComboBox cmbSpecies;
     private javax.swing.JMenu jMenu1;
     private javax.swing.JMenu jMenu2;
     private javax.swing.JMenu jMenu3;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JSeparator jSeparator2;
     private javax.swing.JMenuItem menuBox;
     private javax.swing.JMenuItem menuChangeSize;
     private javax.swing.JMenuItem menuDeleteBox;
     private javax.swing.JMenuItem menuExport;
     private javax.swing.JMenuItem menuExportBox;
     private javax.swing.JMenuItem menuFront;
     private javax.swing.JMenuItem menuImportBox;
     private javax.swing.JMenuItem menuLoad;
     private javax.swing.JMenuItem menuNew;
     private javax.swing.JMenuItem menuRandomise;
     private javax.swing.JMenuItem menuSave;
     private javax.swing.JMenuItem menuSaveAs;
     private javax.swing.JMenuItem mnuHappiness;
     private javax.swing.JPanel panelSprite;
     private javax.swing.JScrollPane scrTreeBox;
     private javax.swing.JTabbedPane tabForms;
     private javax.swing.JTree treeBox;
     // End of variables declaration//GEN-END:variables
 
 }
