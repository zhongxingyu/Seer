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
 
 import java.awt.FileDialog;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.awt.event.ItemEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeWillExpandListener;
 import javax.swing.tree.ExpandVetoException;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 import shoddybattleclient.shoddybattle.*;
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
             if (m_front) {
                 g.drawImage(m_background, -130, -17, this);
                 if (m_img == null) return;
                 g.drawImage(m_img, panelSprite.getWidth() / 2 - m_img.getWidth(this) / 2,
                         panelSprite.getHeight() - 10 - m_img.getHeight(this), this);
             } else {
                 g.drawImage(m_background, 0, getHeight() - m_background.getHeight(this), this);
                 if (m_img == null) return;
                 g.drawImage(m_img, 30, getHeight() - m_img.getHeight(this), this);
             }
         }
     }
 
     private List<TeamBuilderForm> m_forms = new ArrayList<TeamBuilderForm>();
     private ArrayList<PokemonSpecies> m_species;
     private ArrayList<PokemonMove> m_moves;
     private File m_save;
 
     //A hacky fix similar to TeamBuilderForm's hpProgramSelect
     //Set to true if we don't want cmbSpeciesItemStateChange from clearing data
     //on tab switch
     private boolean speciesProgramSelect = false;
 
     /** Creates new form TeamBuilder */
     public TeamBuilder() {
         initComponents();
         long t1 = System.currentTimeMillis();
         MoveListParser mlp = new MoveListParser();
         m_moves = mlp.parseDocument(TeamBuilder.class.getResource("resources/moves.xml").toString());
         long t2 = System.currentTimeMillis();
         mlp = null;
         SpeciesListParser parser = new SpeciesListParser();
         m_species = parser.parseDocument(TeamBuilder.class.getResource("resources/species.xml").toString());
         parser = null;
         long t3 = System.currentTimeMillis();
         Collections.sort(m_species, new Comparator<PokemonSpecies>() {
             public int compare(PokemonSpecies arg0, PokemonSpecies arg1) {
                 return arg0.getName().compareToIgnoreCase(arg1.getName());
             }
         });
         long t4 = System.currentTimeMillis();
         System.out.println("Loaded moves in " + (t2-t1) + " milliseconds");
         System.out.println("Loaded species in " + (t3-t2) + " milliseconds");
         System.out.println("Sorted species in " + (t4-t3) + " milliseconds");
         cmbSpecies.setModel(new DefaultComboBoxModel(m_species.toArray(new PokemonSpecies[m_species.size()])));
         addDefaultTeam();
         treeBox.setModel(new BoxTreeModel());
         treeBox.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
         treeBox.addTreeWillExpandListener(new TreeWillExpandListener() {
             public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
                 TreePath path = e.getPath();
                 String root = (String)path.getPathComponent(1);
                 if (BoxTreeModel.isTeamRoot(root)) {
                     loadPokemonFromTeams();
                 } else if (BoxTreeModel.isBoxRoot(root)) {
                     loadPokemonFromBoxes();
                 }
             }
             public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException { }
         });
     }
 
     private void addDefaultTeam() {
         m_forms.clear();
         tabForms.removeAll();
         for (int i = 0; i < 6; i++) {
             addDefaultForm();
         }
         setSpecies(tabForms.getTitleAt(0));
     }
     private void addDefaultForm() {
         TeamBuilderForm tbf = new TeamBuilderForm(this, m_forms.size());
         m_forms.add(tbf);
         tabForms.addTab("", tbf);
         tbf.setPokemon(new Pokemon("Bulbasaur", "", false, Gender.GENDER_MALE, 100, 255,
             "", "", "", new String[] {null, null, null, null}, new int[] {3,3,3,3},
             new int[] {31,31,31,31,31,31}, new int[] {0,0,0,0,0,0}), true);
     }
 
     public PokemonSpecies getSpecies(String species) {
         for (PokemonSpecies sp : m_species) {
             if (sp.getName().equals(species)) {
                 return sp;
             }
         }
         return null;
     }
 
     public String[] getSpeciesList() {
         String[] ret = new String[m_species.size()];
         int i = 0;
         for (PokemonSpecies ps : m_species) {
             ret[i++] = ps.getName();
         }
         return ret;
     }
 
     public void updateTitle(int index, String title) {
         tabForms.setTitleAt(index, title);
     }
 
     public ArrayList<PokemonMove> getMoveList() {
         return m_moves;
     }
 
     public void setSpriteShiny(boolean shiny) {
         String tab = tabForms.getTitleAt(tabForms.getSelectedIndex());
         String current = ((PokemonSpecies)cmbSpecies.getSelectedItem()).getName();
         if (!tab.equals(current)) return;
         ((SpritePanel)panelSprite).setShiny(shiny);
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
         speciesProgramSelect = true;
         PokemonSpecies species = null;
         for (PokemonSpecies s : m_species) {
             if (s.getName().equals(name)) {
                 species = s;
 
                 int id = PokemonSpecies.getIdFromName(m_species, species.getName());
                 ((SpritePanel)panelSprite).setSpecies(id, false, true);
                 treeBox.setModel(new BoxTreeModel());
                 treeBox.setSelectionRow(0);
 
                 cmbSpecies.setSelectedItem(species);
 
                 break;
             }
         }
         speciesProgramSelect = false;
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
             Pokemon[] team = parser.parseTeam(teams[i].toString());
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
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         tabForms = new javax.swing.JTabbedPane();
         cmbSpecies = new javax.swing.JComboBox();
         jScrollPane1 = new javax.swing.JScrollPane();
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
         menuChangeSize = new javax.swing.JMenuItem();
         jSeparator2 = new javax.swing.JSeparator();
         menuExport = new javax.swing.JMenuItem();
         jMenu3 = new javax.swing.JMenu();
         mnuHappiness = new javax.swing.JMenuItem();
         jMenu2 = new javax.swing.JMenu();
         menuRandomise = new javax.swing.JMenuItem();
         menuBox = new javax.swing.JMenuItem();
         jMenuItem6 = new javax.swing.JMenuItem();
 
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
         jScrollPane1.setViewportView(treeBox);
 
         btnLoadFromBox.setText("Load >>");
         btnLoadFromBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnLoadFromBoxActionPerformed(evt);
             }
         });
 
         btnSaveToBox.setText("Save <<");
 
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
 
         menuChangeSize.setText("Change Team Size");
         menuChangeSize.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuChangeSizeActionPerformed(evt);
             }
         });
         jMenu1.add(menuChangeSize);
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
 
         jMenuBar1.add(jMenu3);
 
         jMenu2.setText("Tools");
 
         menuRandomise.setText("Move to Front");
         jMenu2.add(menuRandomise);
 
         menuBox.setText("Randomise Team");
         jMenu2.add(menuBox);
 
         jMenuItem6.setText("Open Box");
         jMenu2.add(jMenuItem6);
 
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
                             .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 121, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
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
                     .add(tabForms, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                     .add(layout.createSequentialGroup()
                         .add(cmbSpecies, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(panelSprite, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                         .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 163, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(btnLoadFromBox)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(btnSaveToBox)
                         .add(0, 55, Short.MAX_VALUE)))
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
 
         try {
             TeamFileParser tfp = new TeamFileParser();
             Pokemon[] team = tfp.parseTeam(file);
 
             m_forms.clear();
             tabForms.removeAll();
 
             int nPokemon = Math.min(team.length, 24);
             for (int i = 0; i < nPokemon; i++) {
                 m_forms.add(new TeamBuilderForm(this, i));
                 tabForms.add("", m_forms.get(i));
                 m_forms.get(i).setPokemon(team[i], true);
             }
             setSpecies(team[0].species);
             setSpriteShiny(team[0].shiny);
 
             m_save = new File(file);
         }
         catch (Exception ex) {
             ex.printStackTrace();
             addDefaultTeam();
             JOptionPane.showMessageDialog(null, "Error reading file",
                     "Error", JOptionPane.ERROR_MESSAGE);
         }
 }//GEN-LAST:event_menuLoadActionPerformed
 
     private void menuSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveActionPerformed
         if(m_save == null)
             saveTeam();
         else
             saveTeam(m_save.getAbsolutePath());
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
 
         if (size < 1)
             return;
 
         if (size > 24) {
             JOptionPane.showMessageDialog(null, "Cannot use a size larger than 24 pokemon.",
                     "Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         if (size > m_forms.size()) {
             while (size > m_forms.size()) {
                 addDefaultForm();
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
         int id = PokemonSpecies.getIdFromName(m_species, sp.getName());
         ((SpritePanel)panelSprite).setSpecies(id, false, true);
         treeBox.setModel(new BoxTreeModel());
         treeBox.setSelectionRow(0);
 
         //If the species has no gender or is only female, GENDER_MALE is ignored
         TeamBuilderForm tbf = (TeamBuilderForm)tabForms.getSelectedComponent();
         tbf.setPokemon(new Pokemon(sp.getName(), "", false, Gender.GENDER_MALE, 100, 255,
             "", "", "", new String[] {null, null, null, null}, new int[] {3,3,3,3},
             new int[] {31,31,31,31,31,31}, new int[] {0,0,0,0,0,0}), false);
     }//GEN-LAST:event_cmbSpeciesItemStateChanged
 
     private void btnLoadFromBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadFromBoxActionPerformed
         Object obj = treeBox.getLastSelectedPathComponent();
         if (obj == null) return;
         if (BoxTreeModel.isDefaultNode(obj.toString())) {
             PokemonSpecies sp = (PokemonSpecies)cmbSpecies.getSelectedItem();
             ((TeamBuilderForm)tabForms.getSelectedComponent()).setPokemon(new Pokemon(
                 sp.getName(), "", false, Gender.GENDER_MALE, 100, 255, null, null, null,
                 new String[] {null, null, null, null}, new int[] {3,3,3,3},
                 new int[] {31,31,31,31,31,31}, new int[] {0,0,0,0,0,0}), false);
         } else if (obj instanceof Pokemon) {
             m_forms.get(tabForms.getSelectedIndex()).setPokemon((Pokemon)obj, true);
         }
     }//GEN-LAST:event_btnLoadFromBoxActionPerformed
 
     private void mnuHappinessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuHappinessActionPerformed
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
         String name = tabForms.getTitleAt(idx);
         if ((name == null) || name.equals("")) return;
         setSpecies(name);
     }//GEN-LAST:event_tabFormsStateChanged
 
     private void menuNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuNewActionPerformed
         int result = JOptionPane.showOptionDialog(null,
                  "This team may have unsaved changes, create new anyways?",
                  "Unsaved Changes", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
         if(result == JOptionPane.OK_OPTION)
         addDefaultTeam();
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
 
     /**
     * @param args the command line arguments
     */
     public static void main(String args[]) {
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 try {
                     UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                 } catch (Exception e) {
                     
                 }
                 new TeamBuilder().setVisible(true);
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
     private javax.swing.JMenuItem jMenuItem6;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JSeparator jSeparator2;
     private javax.swing.JMenuItem menuBox;
     private javax.swing.JMenuItem menuChangeSize;
     private javax.swing.JMenuItem menuExport;
     private javax.swing.JMenuItem menuLoad;
     private javax.swing.JMenuItem menuNew;
     private javax.swing.JMenuItem menuRandomise;
     private javax.swing.JMenuItem menuSave;
     private javax.swing.JMenuItem menuSaveAs;
     private javax.swing.JMenuItem mnuHappiness;
     private javax.swing.JPanel panelSprite;
     private javax.swing.JTabbedPane tabForms;
     private javax.swing.JTree treeBox;
     // End of variables declaration//GEN-END:variables
 
 }
