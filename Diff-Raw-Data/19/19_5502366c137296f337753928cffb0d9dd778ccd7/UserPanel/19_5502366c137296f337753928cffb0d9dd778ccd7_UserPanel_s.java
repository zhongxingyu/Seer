 /*
  * UserPanel.java
  *
  * Created on May 12, 2009, 1:48:11 PM
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
 
 package shoddybattleclient.forms;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FileDialog;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.MediaTracker;
 import javax.swing.BorderFactory;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import shoddybattleclient.GameVisualisation;
 import shoddybattleclient.network.ServerLink;
 import shoddybattleclient.network.ServerLink.ChallengeMediator;
 import shoddybattleclient.network.ServerLink.MessageListener;
 import shoddybattleclient.network.ServerLink.TimerOptions;
 import shoddybattleclient.shoddybattle.Generation;
 import shoddybattleclient.shoddybattle.Generation.Metagame;
 import shoddybattleclient.shoddybattle.Generation.RuleSet;
 import shoddybattleclient.shoddybattle.Pokemon;
 import shoddybattleclient.shoddybattle.Pokemon.Gender;
 import shoddybattleclient.shoddybattle.PokemonSpecies;
 import shoddybattleclient.utils.ClauseList;
 import shoddybattleclient.utils.ClauseList.ClauseListModel;
 import shoddybattleclient.utils.CloseableTabbedPane.CloseableTab;
 import shoddybattleclient.utils.HTMLPane;
 import shoddybattleclient.utils.TeamFileParser;
 import shoddybattleclient.utils.Text;
 
 /**
  *
  * @author ben
  */
 public class UserPanel extends javax.swing.JPanel implements CloseableTab, MessageListener {
 
     public static class TeamBox extends JPanel {
         private int m_teamLength;
         private Pokemon[] m_team;
         private Generation m_generation;
 
         public TeamBox() {
             this(6);
         }
 
         public TeamBox(int teamLength) {
             super.setLayout(new GridLayout(2, (int)Math.ceil(teamLength/2)));
             setTeamLength(teamLength);
         }
 
         public void reset() {
             this.removeAll();
             for (int i = 0; i < m_teamLength; i++) {
                 this.add(new SpritePanel(null, -1, null, false));
             }
             if (m_teamLength > 1 && m_teamLength % 2 == 1)
                 this.add(new JPanel());
 
             revalidate();
             repaint();
         }
 
         public void setTeamLength(int teamLength) {
             m_teamLength = teamLength;
 
             GridLayout layout = (GridLayout)this.getLayout();
             if (m_teamLength == 1)
                 layout.setRows(1);
             else
                 layout.setRows(2);
             
             if (m_team != null)
                 loadTeam();
             else
                 reset();
         }
 
         private void loadTeam() {
             this.removeAll();
             for (int i = 0; i < m_teamLength; i++) {
                 if (i < m_team.length) {
                     Pokemon p = m_team[i];
                     int id = PokemonSpecies.getIdFromName(m_generation,
                             p.species);
                     SpritePanel panel = new SpritePanel(p.species, id,
                             p.gender, p.shiny);
                     this.add(panel);
                 } else {
                     this.add(new SpritePanel(null, -1, null, false));
                 }
             }
             if (m_teamLength > 1 && m_teamLength % 2 == 1)
                 this.add(new JPanel());
 
             revalidate();
             repaint();
         }
 
         /**
          * Initialises the sprites from a team file and returns the team
          */
         public Pokemon[] loadFromTeam(String file, Generation generation) {
             TeamFileParser tfp = new TeamFileParser();
             Pokemon[] team = null;
             try {
                 team = tfp.parseTeam(file, ServerLink.getGeneration());
                 m_team = team;
             } catch (Exception e) {
                 return null;
             }
             m_generation = generation;
 
             if (m_team != null) {
                 loadTeam();
             }
             return team;
         }
     }
 
     private static class SpritePanel extends JPanel {
         private Image m_image;
         public SpritePanel(String species, int speciesId, Gender g, boolean shiny) {
             setBorder(BorderFactory.createEtchedBorder());
             if (speciesId < 0) return;
             m_image = GameVisualisation.getSprite(speciesId, true,
                     !Gender.GENDER_FEMALE.equals(g), shiny);
             if (m_image == null) return;
             MediaTracker tracker = new MediaTracker(this);
             tracker.addImage(m_image, 0);
             try {
                 tracker.waitForAll();
             } catch (Exception e) {
 
             }
             setToolTipText(species);
         }
         @Override
         public Dimension getPreferredSize() {
             return new Dimension(40, 40);
         }
         @Override
         protected void paintComponent(Graphics g) {
             super.paintComponent(g);
             g.setColor(Color.WHITE);
             g.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
             if (m_image != null)
                 g.drawImage(m_image, 0, 0, this);
         }
     }
 
     private final ServerLink m_link;
     private String m_opponent;
     private int m_idx;
     private Pokemon[] m_team = null;
     private boolean m_incoming = false;
     private int m_n;
     private int m_teamLength;
     private int m_generation;
     private int[] m_clauses;
     private TimerOptions m_timerOps;
     private int m_metagame;
     private boolean m_waiting = false;
 
     /** Creates new form UserPanel */
     public UserPanel(String name, ServerLink link, int index) {
         initComponents();
         lblName.setText(name);
         m_opponent = name;
         m_link = link;
         m_idx = index;
         m_link.addMessageListener(this);
         m_link.requestUserMessage(name);
         listClauses.setModel(new ClauseListModel(m_link.getClauseList()));
 
         Generation[] generations = m_link.getGenerations();
         DefaultComboBoxModel model = new DefaultComboBoxModel(generations);
         cmbGen.setModel(model);
         cmbGen.setSelectedIndex(0);
     }
 
     public void setPersonalMessage(String msg) {
         if ("".equals(msg)) {
             msg = Text.getText(27, 5);
         }
         msg = "<html>" + HTMLPane.htmlEntityEncode(msg) + "</html>";
         lblMessage.setText(msg);
     }
 
     public void setIncoming() {
         btnChallenge.setText("Accept");
         cmbGen.setEnabled(false);
         cmbRules.setEnabled(false);
         cmbTeamLength.setEnabled(false);
         cmbN.setEnabled(false);
         m_incoming = true;
     }
 
     public void setOptions(int n, int teamLength, int generation, int metagame,
             RuleSet rules) {
         if (m_waiting) return;
 
         m_n = n;
         m_teamLength = teamLength;
         m_generation = generation;
         cmbGen.setSelectedIndex(generation);
         cmbN.setSelectedIndex(n - 1);
         cmbTeamLength.setSelectedIndex(teamLength - 1);
 
         if (metagame < 0) {
             cmbRules.setSelectedIndex(cmbRules.getItemCount()-1);
             tabSettings.setSelectedIndex(0);
         } else {
             cmbRules.setSelectedIndex(metagame);
         }
 
         m_clauses = rules.getClauses(m_link.getClauseList());
         ((ClauseListModel)listClauses.getModel()).setSelected(m_clauses);
         TimerOptions ops = rules.getTimerOptions();
         m_timerOps = ops;
         enableCustomFields(false);
         if (ops == null) {
             chkTimed.setSelected(false);
             txtTimerLength.setText(null);
             txtTimerPeriods.setText(null);
             txtTimerPool.setText(null);
         } else {
             chkTimed.setSelected(true);
             txtTimerLength.setText(String.valueOf(ops.periodLength));
             txtTimerPeriods.setText(String.valueOf(ops.periods));
             txtTimerPool.setText(String.valueOf(ops.pool));
         }
     }
 
     public ChallengeMediator getMediator() {
         return new ChallengeMediator() {
             public Pokemon[] getTeam() {
                 return m_team;
             }
             public void informResolved(boolean accepted) {
                 close();
             }
             public String getOpponent() {
                 return m_opponent;
             }
             public int getGeneration() {
                 return m_generation;
             }
             public int getActivePartySize() {
                 return m_n;
             }
             public int getMaxTeamLength() {
                 return m_teamLength;
             }
             public int[] getClauses() {
                 return m_clauses;
             }
             public TimerOptions getTimerOptions() {
                 return m_timerOps;
             }
             public int getMetagame() {
                 return m_metagame;
             }
         };
     }
 
     public void close() {
         m_link.getLobby().closeTab(m_idx);
     }
 
     public boolean informClosed() {
         boolean close = true;
         if (m_waiting && !m_incoming) {
             close = JOptionPane.showConfirmDialog(this,
                     "Are you sure you wish to cancel your challenge?", "Cancel Challenge",
                      JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
             if (close) m_link.withdrawChallenge(m_opponent);
         } else if (m_incoming) {
             m_link.resolveChallenge(m_opponent, false, null);
             close = true;
         }
         if (!close) return false;
         m_link.removeMessageListener(this);
         return true;
     }
 
     public String getOpponent() {
         return m_opponent;
     }
 
     @Override
     public void informMessageRecevied(String user, String msg) {
         if (!user.equals(m_opponent)) return;
         setPersonalMessage(msg);
     }
 
     public void enableCustomFields(boolean enable) {
         chkTimed.setEnabled(enable);
         txtTimerLength.setEnabled(enable);
         txtTimerPeriods.setEnabled(enable);
         txtTimerPool.setEnabled(enable);
         listClauses.setEnabled(enable);
     }
 
     private boolean validateFields() {
         try {
             Integer.parseInt(txtTimerLength.getText());
             Integer.parseInt(txtTimerPool.getText());
             Integer.parseInt(txtTimerPeriods.getText());
         } catch (NumberFormatException e) {
             JOptionPane.showMessageDialog(this, "Invalid timer options - please" +
                     " enter only numbers");
             return false;
         }
         return true;
     }
 
     public void unsetTeam() {
         ((TeamBox)panelSprites).reset();
         btnChallenge.setEnabled(false);
         btnLoad.setEnabled(true);
         m_waiting = true;
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jPanel1 = new javax.swing.JPanel();
         lblName = new javax.swing.JLabel();
         lblMessage = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         tabSettings = new javax.swing.JTabbedPane();
         jPanel2 = new javax.swing.JPanel();
         panelSprites = new TeamBox();
         btnLoad = new javax.swing.JButton();
         btnChallenge = new javax.swing.JButton();
         jPanel4 = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         cmbGen = new javax.swing.JComboBox();
         jLabel2 = new javax.swing.JLabel();
         cmbRules = new javax.swing.JComboBox();
         jLabel4 = new javax.swing.JLabel();
         cmbN = new javax.swing.JComboBox();
         jLabel8 = new javax.swing.JLabel();
         cmbTeamLength = new javax.swing.JComboBox();
         jPanel3 = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         listClauses = new ClauseList();
         chkTimed = new javax.swing.JCheckBox();
         jLabel5 = new javax.swing.JLabel();
         txtTimerPool = new javax.swing.JTextField();
         jLabel6 = new javax.swing.JLabel();
         txtTimerPeriods = new javax.swing.JTextField();
         jLabel7 = new javax.swing.JLabel();
         txtTimerLength = new javax.swing.JTextField();
 
         setOpaque(false);
 
         jPanel1.setOpaque(false);
 
         lblName.setFont(new java.awt.Font("Lucida Grande", 1, 16));
         lblName.setText("bearzly");
 
         lblMessage.setFont(new java.awt.Font("Lucida Grande", 0, 11));
         lblMessage.setVerticalAlignment(javax.swing.SwingConstants.TOP);
 
         jLabel3.setFont(new java.awt.Font("Lucida Grande", 1, 13));
         jLabel3.setText("Rankings:");
 
         org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1Layout.createSequentialGroup()
                 .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(lblName)
                     .add(jLabel3)
                     .add(lblMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1Layout.createSequentialGroup()
                 .add(lblName)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(lblMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 135, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jLabel3)
                .addContainerGap(147, Short.MAX_VALUE))
         );
 
         jPanel2.setOpaque(false);
 
         panelSprites.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         panelSprites.setMaximumSize(new java.awt.Dimension(32767, 200));
         panelSprites.setOpaque(false);
         panelSprites.setPreferredSize(new java.awt.Dimension(4, 200));
         panelSprites.setLayout(new java.awt.GridLayout(2, 3));
 
         btnLoad.setText("Load");
         btnLoad.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnLoadActionPerformed(evt);
             }
         });
 
         btnChallenge.setText("Challenge");
         btnChallenge.setEnabled(false);
         btnChallenge.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnChallengeActionPerformed(evt);
             }
         });
 
         jPanel4.setOpaque(false);
         jPanel4.setLayout(new java.awt.GridLayout(4, 2, 5, 7));
 
        jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
         jLabel1.setText("Generation:");
         jPanel4.add(jLabel1);
 
         cmbGen.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Generation 4", "Generation 5" }));
         cmbGen.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmbGenActionPerformed(evt);
             }
         });
         jPanel4.add(cmbGen);
 
         jLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 13));
         jLabel2.setText("Rules:");
         jPanel4.add(jLabel2);
 
         cmbRules.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Standard", "Ubers" }));
         cmbRules.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmbRulesActionPerformed(evt);
             }
         });
         jPanel4.add(cmbRules);
 
         jLabel4.setFont(new java.awt.Font("Lucida Grande", 1, 13));
         jLabel4.setText("Mode:");
         jPanel4.add(jLabel4);
 
         cmbN.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Singles", "Doubles", "3v3", "4v4", "5v5", "6v6" }));
         jPanel4.add(cmbN);
 
         jLabel8.setFont(new java.awt.Font("Lucida Grande", 1, 13));
         jLabel8.setText("Team Length:");
         jPanel4.add(jLabel8);
 
         cmbTeamLength.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6" }));
         cmbTeamLength.setSelectedIndex(5);
         cmbTeamLength.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmbTeamLengthActionPerformed(evt);
             }
         });
         jPanel4.add(cmbTeamLength);
 
         org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(panelSprites, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                     .add(jPanel2Layout.createSequentialGroup()
                         .add(btnLoad, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 78, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .add(18, 18, 18)
                        .add(btnChallenge, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
                    .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(panelSprites, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 91, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(btnLoad)
                     .add(btnChallenge))
                 .addContainerGap())
         );
 
         tabSettings.addTab("Basic", jPanel2);
 
         jPanel3.setOpaque(false);
 
         listClauses.setEnabled(false);
         jScrollPane1.setViewportView(listClauses);
 
         chkTimed.setSelected(true);
         chkTimed.setText("Timed Battle?");
         chkTimed.setEnabled(false);
         chkTimed.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chkTimedActionPerformed(evt);
             }
         });
 
         jLabel5.setText("Initial Pool:");
 
         txtTimerPool.setText("300");
         txtTimerPool.setEnabled(false);
 
         jLabel6.setText("Periods:");
 
         txtTimerPeriods.setText("3");
         txtTimerPeriods.setEnabled(false);
 
         jLabel7.setText("Period Length:");
 
         txtTimerLength.setText("30");
         txtTimerLength.setEnabled(false);
 
         org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                 .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, chkTimed)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel3Layout.createSequentialGroup()
                         .add(8, 8, 8)
                         .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
                             .add(jPanel3Layout.createSequentialGroup()
                                 .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                     .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel3Layout.createSequentialGroup()
                                         .add(jLabel7)
                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                         .add(txtTimerLength))
                                     .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel3Layout.createSequentialGroup()
                                         .add(jLabel5)
                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                         .add(txtTimerPool, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                 .add(jLabel6)
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                 .add(txtTimerPeriods, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                 .addContainerGap())
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel3Layout.createSequentialGroup()
                 .add(chkTimed)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel5)
                     .add(txtTimerPool, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jLabel6)
                     .add(txtTimerPeriods, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel7)
                     .add(txtTimerLength, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         tabSettings.addTab("Advanced", jPanel3);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .add(41, 41, 41)
                 .add(tabSettings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 262, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, tabSettings, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnChallengeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChallengeActionPerformed
         if (m_team == null) return;
         if (m_incoming) {
             m_link.resolveChallenge(m_opponent, true, m_team);
             btnChallenge.setEnabled(false);
             btnLoad.setEnabled(false);
         } else {
             if (m_waiting) {
                 m_link.postChallengeTeam(m_opponent, m_team);
                 m_waiting = false;
                 return;
             }
             if (!validateFields()) return;
             m_link.postChallenge(new ChallengeMediator() {
                 public Pokemon[] getTeam() {
                     return m_team;
                 }
                 public void informResolved(boolean accepted) {
                     if (accepted) {
                         m_link.postChallengeTeam(m_opponent, m_team);
                     } else {
                         // todo: internationalisation
                         JOptionPane.showMessageDialog(UserPanel.this,
                                 m_opponent + " rejected the challenge.");
                         m_waiting = false;
                         close();
                     }
                     m_waiting = false;
                 }
                 public String getOpponent() {
                     return m_opponent;
                 }
                 public int getGeneration() {
                     return cmbGen.getSelectedIndex();
                 }
                 public int getActivePartySize() {
                     return cmbN.getSelectedIndex() + 1;
                 }
                 public int getMaxTeamLength() {
                     return Integer.parseInt((String)cmbTeamLength.getSelectedItem());
                 }
                 public int[] getClauses() {
                     return ((ClauseListModel)listClauses.getModel()).getSelected();
                 }
                 public TimerOptions getTimerOptions() {
                     if (!chkTimed.isSelected()) return null;
                     int pool = Integer.parseInt(txtTimerPool.getText());
                     int periods = Integer.parseInt(txtTimerPeriods.getText());
                     int periodLength = Integer.parseInt(txtTimerLength.getText());
                     return new TimerOptions(pool, periods, periodLength);
                 }
                 public int getMetagame() {
                     int genId = cmbGen.getSelectedIndex();
                     Generation gen = m_link.getGenerations()[genId];
 
                     int idx = cmbRules.getSelectedIndex();
                     if (idx >= gen.getMetagames().size()) {
                         return -1;
                     }
                     return idx;
                 }
             });
             btnChallenge.setEnabled(false);
             btnLoad.setEnabled(false);
             cmbGen.setEnabled(false);
             cmbRules.setEnabled(false);
             cmbTeamLength.setEnabled(false);
             cmbN.setEnabled(false);
             enableCustomFields(false);
             m_waiting = true;
         }
     }//GEN-LAST:event_btnChallengeActionPerformed
 
     private void btnLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadActionPerformed
         FileDialog fd = new FileDialog(m_link.getLobby(), "Choose a team to load", FileDialog.LOAD);
         fd.setVisible(true);
         if (fd.getFile() == null) return;
         String file = fd.getDirectory() + fd.getFile();
         TeamBox box = (TeamBox)panelSprites;
         Pokemon[] team = box.loadFromTeam(file, m_link.getGeneration());
         if (team != null) {
             m_team = team;
             btnChallenge.setEnabled(true);
         } else {
             JOptionPane.showMessageDialog(this, "Selected team file could not be loaded");
             if (m_team == null) btnChallenge.setEnabled(false);
         }
     }//GEN-LAST:event_btnLoadActionPerformed
 
     private void cmbGenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbGenActionPerformed
         Generation gen = (Generation)cmbGen.getSelectedItem();
 
         DefaultComboBoxModel model = new DefaultComboBoxModel();
         
         for (Metagame meta : gen.getMetagames()) {
             model.addElement(meta);
         }
         model.addElement("Custom...");
         cmbRules.setModel(model);
         cmbRules.setSelectedIndex(0);
     }//GEN-LAST:event_cmbGenActionPerformed
 
     private void cmbTeamLengthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTeamLengthActionPerformed
         int teamLength = Integer.parseInt(cmbTeamLength.getSelectedItem().toString());
         ((TeamBox)panelSprites).setTeamLength(teamLength);
     }//GEN-LAST:event_cmbTeamLengthActionPerformed
 
     private void chkTimedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkTimedActionPerformed
         boolean enabled = chkTimed.isSelected();
         txtTimerPool.setEnabled(enabled);
         txtTimerPeriods.setEnabled(enabled);
         txtTimerLength.setEnabled(enabled);
     }//GEN-LAST:event_chkTimedActionPerformed
 
     private void cmbRulesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbRulesActionPerformed
         String val = cmbRules.getSelectedItem().toString();
         if ("Custom...".equals(val)) {
             enableCustomFields(true);
             tabSettings.setSelectedIndex(1);
         } else {
             Metagame meta = (Metagame)cmbRules.getSelectedItem();
             cmbN.setSelectedIndex(meta.getPartySize()-1);
             cmbTeamLength.setSelectedIndex(meta.getMaxTeamLength()-1);
             m_clauses = meta.getClauses(m_link.getClauseList());
             ((ClauseListModel)listClauses.getModel()).setSelected(m_clauses);
 
             enableCustomFields(false);
         }
     }//GEN-LAST:event_cmbRulesActionPerformed
 
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnChallenge;
     private javax.swing.JButton btnLoad;
     private javax.swing.JCheckBox chkTimed;
     private javax.swing.JComboBox cmbGen;
     private javax.swing.JComboBox cmbN;
     private javax.swing.JComboBox cmbRules;
     private javax.swing.JComboBox cmbTeamLength;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JLabel lblMessage;
     private javax.swing.JLabel lblName;
     private javax.swing.JList listClauses;
     private javax.swing.JPanel panelSprites;
     private javax.swing.JTabbedPane tabSettings;
     private javax.swing.JTextField txtTimerLength;
     private javax.swing.JTextField txtTimerPeriods;
     private javax.swing.JTextField txtTimerPool;
     // End of variables declaration//GEN-END:variables
 
 }
