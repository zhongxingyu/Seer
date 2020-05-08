 /*
  * TeamBuilderForm.java
  *
  * Created on Apr 4, 2009, 4:35:47 PM
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
 import java.awt.Color;
 import java.awt.event.ItemEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import shoddybattleclient.shoddybattle.*;
 import shoddybattleclient.shoddybattle.Pokemon.Gender;
 import shoddybattleclient.utils.*;
 /**
  *
  * @author ben
  */
 
 public class TeamBuilderForm extends javax.swing.JPanel {
 
     private TeamBuilder m_parent;
     //The current species
     private PokemonSpecies m_species = null;
     //index of this tab
     private int m_idx;
     //pokemon represented by this form
     private Pokemon m_pokemon = null;
 
     private JTextField[] m_ivs;
     private JTextField[] m_evs;
     private JLabel[] m_bases;
     private JLabel[] m_totals;
     private JComboBox[] m_ppUps;
 
     private JButtonTable tblMoves;
 
     /** Creates new form TeamBuilderForm */
     public TeamBuilderForm(TeamBuilder parent, String[] species, int idx) {
         initComponents();
         tblMoves = new JButtonTable();
         tblMoves.setModel(new MoveTableModel(null, new String[0]));
         scrollMoves.add(tblMoves);
         scrollMoves.setViewportView(tblMoves);
 
         m_parent = parent;
         cmbPokemon.setModel(new DefaultComboBoxModel(species));
         m_idx = idx;
 
         cmbNature.setModel(new DefaultComboBoxModel(PokemonNature.getNatureNames()));
 
         m_ivs = new JTextField[] {
             ivHp,
             ivAtk,
             ivDef,
             ivSpd,
             ivSpAtk,
             ivSpDef
         };
         m_evs = new JTextField[] {
             evHp,
             evAtk,
             evDef,
             evSpd,
             evSpAtk,
             evSpDef
         };
         m_bases = new JLabel[] {
             baseHp,
             baseAtk,
             baseDef,
             baseSpd,
             baseSpAtk,
             baseSpDef
         };
         m_totals = new JLabel[] {
             totalHp,
             totalAtk,
             totalDef,
             totalSpd,
             totalSpAtk,
             totalSpDef
         };
         m_ppUps = new JComboBox[] {
             cmbPp0,
             cmbPp1,
             cmbPp2,
             cmbPp3,
         };
 
         for (int i = 0; i < m_evs.length; i++) {
             final int index = i;
             KeyListener kl = new KeyListener() {
                 public void keyTyped(KeyEvent e) {
                 }
                 public void keyPressed(KeyEvent e) {
                 }
                 public void keyReleased(KeyEvent e) {
                     updateStat(index);
                 }
             };
             m_ivs[i].addKeyListener(kl);
             m_evs[i].addKeyListener(kl);
         }
         
     }
 
     /**
      * Set the Pokemon shown by this form
      * @param p The pokemon to set
      * @param loading whether we are loading from a team or selected from the list
      */
     public void setPokemon(Pokemon p, boolean loading) {
         m_pokemon = p;
         m_species = m_parent.getSpecies(p.species);
         
         if (loading) {
             cmbPokemon.setSelectedItem(p.species);
         }
 
         Gender g = m_species.getGenders();
         if (g.equals(Gender.GENDER_MALE)) {
             cmbGender.setModel(new DefaultComboBoxModel(new Gender[] {Gender.GENDER_MALE}));
            cmbGender.setSelectedIndex(0);
         } else if (g.equals(Gender.GENDER_FEMALE)) {
             cmbGender.setModel(new DefaultComboBoxModel(new Gender[] {Gender.GENDER_FEMALE}));
         } else if (g.equals(Gender.GENDER_BOTH)) {
             cmbGender.setModel(new DefaultComboBoxModel(new Gender[] {
                 Gender.GENDER_MALE,
                 Gender.GENDER_FEMALE
             }));
            cmbGender.setSelectedIndex(0);
         } else {
             cmbGender.setModel(new DefaultComboBoxModel(new Gender[] {Gender.GENDER_NONE}));
            cmbGender.setSelectedIndex(0);
         }
 
         txtNickname.setText(p.nickname);
         if (loading) {
             cmbItem.setSelectedItem(p.item);
             cmbNature.setSelectedItem(p.nature);
         }
         txtLevel.setText(String.valueOf(p.level));
         chkShiny.setSelected(p.shiny);
         cmbAbility.setSelectedItem(p.ability);
        System.out.println(p.gender.ordinal());
        if (g.equals(Gender.GENDER_BOTH)) {
            cmbGender.setSelectedIndex(p.gender.ordinal());
        }
         for (int i = 0; i < Pokemon.STAT_COUNT; i++) {
             m_ivs[i].setText(String.valueOf(p.ivs[i]));
             m_evs[i].setText(String.valueOf(p.evs[i]));
         }
         for (int i = 0; i < p.moves.length; i++) {
             m_ppUps[i].setSelectedIndex(p.ppUps[i]);
         }
         for (int i = 0; i < Pokemon.STAT_COUNT; i++) {
             m_bases[i].setText(String.valueOf(m_species.getBase(i)));
             m_totals[i].setText(String.valueOf(calculateStat(i)));
         }
 
         MoveTableModel mtm = new MoveTableModel(m_parent.getMoveList(), m_species.getMoves());
         mtm.setSelectedMoves(p.moves);
         tblMoves.setModel(mtm);
         //name column should be wider
         tblMoves.getColumnModel().getColumn(1).setPreferredWidth(160);
         
         cmbAbility.setModel(new DefaultComboBoxModel(m_species.getAbilities()));
         
         m_parent.updateTitle(m_idx, m_species.getName());
     }
 
     public int calculateStat(int i)  {
         int common =
                 (int)((int)(((2.0 * m_species.getBase(i))
                 + m_pokemon.ivs[i]
                 + (m_pokemon.evs[i] / 4.0)))
                 * (m_pokemon.level / 100.0));
         if (i == Pokemon.S_HP) {
             if (m_species.getName().equals("Shedinja")) {
                 // Shedinja always has 1 hp.
                 return 1;
             } else {
                 return common + 10 + m_pokemon.level;
             }
         }
         PokemonNature n = PokemonNature.getNature((String)cmbNature.getSelectedItem());
         double effect = (n == null) ? 1.0 : n.getEffect(i);
         return (int)((common + 5) * effect);
     }
 
     private void updateStat(int idx) {
         if (m_pokemon == null) return;
         if ((idx < 0) || (idx > m_totals.length)) return;
         try {
             m_pokemon.ivs[idx] = Integer.parseInt(m_ivs[idx].getText());
             m_pokemon.evs[idx] = Integer.parseInt(m_evs[idx].getText());
             m_totals[idx].setText(String.valueOf(calculateStat(idx)));
             int total = 0;
             for (int i = 0; i < m_evs.length; i++) {
                 total += Integer.parseInt(m_evs[i].getText());
             }
             Color c = (total > 510) ? Color.RED : Color.BLACK;
             evsTotal.setText("(" + total + "/510)");
             evsTotal.setForeground(c);
         } catch (NumberFormatException e) {
 
         }
     }
 
     public Pokemon getPokemon() {
         m_pokemon.species = (String)cmbPokemon.getSelectedItem();
         m_pokemon.nickname = txtNickname.getText();
         try {
             m_pokemon.level = Integer.parseInt(txtLevel.getText());
         } catch (NumberFormatException e) {
             m_pokemon.level = 100;
         }
         m_pokemon.gender = (Gender)cmbGender.getSelectedItem();
         m_pokemon.nature = (String)cmbNature.getSelectedItem();
         m_pokemon.item = (String)cmbItem.getSelectedItem();
         m_pokemon.ability = (String)cmbAbility.getSelectedItem();
         MoveTableModel m = (MoveTableModel)tblMoves.getModel();
         m_pokemon.moves = m.getSelectedMoves();
         for (int i = 0; i < m_ppUps.length; i++) {
             try {
                 m_pokemon.ppUps[i] = Integer.parseInt((String)m_ppUps[i].getSelectedItem());
             } catch (NumberFormatException e) {
                 m_pokemon.ppUps[i] = 3;
             }
         }
         return m_pokemon;
     }
 
      public static void main(String args[]) {
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new TeamBuilder().setVisible(true);
             }
         });
     }
 
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         cmbPokemon = new javax.swing.JComboBox();
         txtNickname = new javax.swing.JTextField();
         jLabel24 = new javax.swing.JLabel();
         jLabel28 = new javax.swing.JLabel();
         scrollMoves = new javax.swing.JScrollPane();
         cmbPp0 = new javax.swing.JComboBox();
         cmbPp1 = new javax.swing.JComboBox();
         cmbPp2 = new javax.swing.JComboBox();
         cmbPp3 = new javax.swing.JComboBox();
         txtLevel = new javax.swing.JTextField();
         cmbGender = new javax.swing.JComboBox();
         chkShiny = new javax.swing.JCheckBox();
         jPanel2 = new javax.swing.JPanel();
         ivDef = new javax.swing.JTextField();
         ivHp = new javax.swing.JTextField();
         baseHp = new javax.swing.JLabel();
         totalHp = new javax.swing.JLabel();
         evSpAtk = new javax.swing.JTextField();
         ivSpAtk = new javax.swing.JTextField();
         jLabel3 = new javax.swing.JLabel();
         ivSpDef = new javax.swing.JTextField();
         ivSpd = new javax.swing.JTextField();
         jLabel6 = new javax.swing.JLabel();
         totalAtk = new javax.swing.JLabel();
         totalSpAtk = new javax.swing.JLabel();
         ivAtk = new javax.swing.JTextField();
         totalSpd = new javax.swing.JLabel();
         jLabel21 = new javax.swing.JLabel();
         evsTotal = new javax.swing.JLabel();
         evSpDef = new javax.swing.JTextField();
         baseDef = new javax.swing.JLabel();
         baseSpd = new javax.swing.JLabel();
         totalDef = new javax.swing.JLabel();
         jLabel12 = new javax.swing.JLabel();
         evHp = new javax.swing.JTextField();
         baseSpAtk = new javax.swing.JLabel();
         evAtk = new javax.swing.JTextField();
         evDef = new javax.swing.JTextField();
         totalSpDef = new javax.swing.JLabel();
         jLabel18 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         jLabel1 = new javax.swing.JLabel();
         jLabel15 = new javax.swing.JLabel();
         baseAtk = new javax.swing.JLabel();
         jLabel9 = new javax.swing.JLabel();
         evSpd = new javax.swing.JTextField();
         baseSpDef = new javax.swing.JLabel();
         jLabel27 = new javax.swing.JLabel();
         jLabel26 = new javax.swing.JLabel();
         cmbNature = new javax.swing.JComboBox();
         cmbAbility = new javax.swing.JComboBox();
         jLabel25 = new javax.swing.JLabel();
         txtHiddenPower = new javax.swing.JTextField();
         cmbHiddenPower = new javax.swing.JComboBox();
         cmbItem = new javax.swing.JComboBox();
 
         setOpaque(false);
 
         cmbPokemon.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Bulbasaur", "Ivysaur" }));
         cmbPokemon.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 cmbPokemonItemStateChanged(evt);
             }
         });
 
         jLabel24.setText("Level:");
 
         jLabel28.setFont(new java.awt.Font("Lucida Grande", 1, 13));
         jLabel28.setText("PP UPs:");
 
         cmbPp0.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3" }));
         cmbPp0.setSelectedIndex(3);
 
         cmbPp1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3" }));
         cmbPp1.setSelectedIndex(3);
 
         cmbPp2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3" }));
         cmbPp2.setSelectedIndex(3);
 
         cmbPp3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3" }));
         cmbPp3.setSelectedIndex(3);
 
         txtLevel.setText("100");
 
         chkShiny.setText("Shiny?");
         chkShiny.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
         chkShiny.setOpaque(false);
 
         jPanel2.setOpaque(false);
 
         ivDef.setText("31");
 
         ivHp.setText("31");
 
         baseHp.setText("000");
 
         totalHp.setText("000");
 
         evSpAtk.setText("000");
 
         ivSpAtk.setText("31");
 
         jLabel3.setFont(new java.awt.Font("Lucida Grande", 1, 13));
         jLabel3.setText("Base");
 
         ivSpDef.setText("31");
 
         ivSpd.setText("31");
 
         jLabel6.setText("HP");
 
         totalAtk.setText("000");
 
         totalSpAtk.setText("000");
 
         ivAtk.setText("31");
 
         totalSpd.setText("000");
 
         jLabel21.setText("Sp. Defense");
 
         evsTotal.setFont(new java.awt.Font("Lucida Grande", 1, 11));
         evsTotal.setText("(000/510)");
         evsTotal.setMaximumSize(new java.awt.Dimension(50, 16));
 
         evSpDef.setText("000");
 
         baseDef.setText("000");
 
         baseSpd.setText("000");
 
         totalDef.setText("000");
 
         jLabel12.setText("Defense");
 
         evHp.setText("000");
 
         baseSpAtk.setText("000");
 
         evAtk.setText("000");
 
         evDef.setText("000");
 
         totalSpDef.setText("000");
 
         jLabel18.setText("Sp. Attack");
 
         jLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 13));
         jLabel2.setText("Total");
 
         jLabel4.setFont(new java.awt.Font("Lucida Grande", 1, 13));
         jLabel4.setText("IVs");
 
         jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 13));
         jLabel1.setText("Stat");
 
         jLabel15.setText("Speed");
 
         baseAtk.setText("000");
 
         jLabel9.setText("Attack");
 
         evSpd.setText("000");
 
         baseSpDef.setText("000");
 
         jLabel27.setText("Ability:");
 
         jLabel26.setText("Nature:");
 
         cmbNature.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Jolly", "Hasty" }));
         cmbNature.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 cmbNatureItemStateChanged(evt);
             }
         });
 
         cmbAbility.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Overgrow", "Truant" }));
 
         jLabel25.setText("Hidden Power:");
 
         txtHiddenPower.setEditable(false);
         txtHiddenPower.setText("70");
 
         cmbHiddenPower.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Dark" }));
 
         org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel2Layout.createSequentialGroup()
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jPanel2Layout.createSequentialGroup()
                         .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(jLabel1)
                             .add(jLabel6)
                             .add(jLabel9)
                             .add(jLabel12)
                             .add(jLabel15)
                             .add(jLabel18)
                             .add(jLabel21))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                             .add(jLabel2)
                             .add(totalSpDef, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(totalSpAtk, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(totalSpd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(totalDef, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(totalAtk, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(totalHp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                         .add(18, 18, 18)
                         .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(jLabel3)
                             .add(baseHp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(baseAtk, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(baseDef, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(baseSpd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(baseSpAtk, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(baseSpDef, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                             .add(jPanel2Layout.createSequentialGroup()
                                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                     .add(ivHp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                     .add(ivAtk, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                     .add(ivDef, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                     .add(ivSpd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                     .add(ivSpAtk, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                     .add(ivSpDef, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                 .add(18, 18, 18)
                                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                     .add(evSpd, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                                     .add(evSpAtk, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                                     .add(evSpDef, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                                     .add(evHp, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                                     .add(evAtk, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                                     .add(evDef, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)))
                             .add(jPanel2Layout.createSequentialGroup()
                                 .add(jLabel4)
                                 .add(18, 18, 18)
                                 .add(evsTotal, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                     .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                         .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2Layout.createSequentialGroup()
                             .add(jLabel25)
                             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                             .add(cmbHiddenPower, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                             .add(txtHiddenPower, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                         .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2Layout.createSequentialGroup()
                             .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                 .add(jLabel26)
                                 .add(jLabel27))
                             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                             .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                 .add(cmbNature, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 211, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                 .add(cmbAbility, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 210, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                 .addContainerGap(29, Short.MAX_VALUE))
         );
 
         jPanel2Layout.linkSize(new java.awt.Component[] {evAtk, evDef, evHp, evSpAtk, evSpDef, evSpd}, org.jdesktop.layout.GroupLayout.HORIZONTAL);
 
         jPanel2Layout.linkSize(new java.awt.Component[] {cmbAbility, cmbNature}, org.jdesktop.layout.GroupLayout.HORIZONTAL);
 
         jPanel2Layout.linkSize(new java.awt.Component[] {ivAtk, ivDef, ivHp, ivSpAtk, ivSpDef, ivSpd}, org.jdesktop.layout.GroupLayout.HORIZONTAL);
 
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel1)
                     .add(jLabel4)
                     .add(jLabel3)
                     .add(jLabel2)
                     .add(evsTotal, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(jPanel2Layout.createSequentialGroup()
                         .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(jLabel6)
                             .add(baseHp)
                             .add(ivHp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(totalHp))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(jLabel9)
                             .add(baseAtk)
                             .add(ivAtk, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(totalAtk))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(jLabel12)
                             .add(baseDef)
                             .add(ivDef, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(totalDef))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(jLabel15)
                             .add(baseSpd)
                             .add(ivSpd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(totalSpd))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(jLabel18)
                             .add(baseSpAtk)
                             .add(ivSpAtk, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(totalSpAtk))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(jLabel21)
                             .add(baseSpDef)
                             .add(ivSpDef, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(totalSpDef)))
                     .add(jPanel2Layout.createSequentialGroup()
                         .add(evHp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(evAtk, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(evDef, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(evSpd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(evSpAtk, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(evSpDef, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel25)
                     .add(txtHiddenPower, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(cmbHiddenPower, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel26)
                     .add(cmbNature, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel27)
                     .add(cmbAbility, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
         );
 
         cmbItem.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Item", "Leftovers" }));
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(layout.createSequentialGroup()
                         .add(cmbPokemon, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(txtNickname, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 136, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(layout.createSequentialGroup()
                         .add(7, 7, 7)
                         .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(scrollMoves, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                         .add(cmbItem, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jLabel24)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(txtLevel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(cmbGender, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 128, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(chkShiny)
                         .add(0, 0, Short.MAX_VALUE))
                     .add(layout.createSequentialGroup()
                         .add(jLabel28)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(cmbPp0, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(cmbPp1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(cmbPp2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(cmbPp3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .add(24, 24, 24)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(cmbPokemon, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(txtNickname, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(cmbItem, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(cmbGender, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(txtLevel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jLabel24)
                     .add(chkShiny))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                         .add(scrollMoves, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(jLabel28)
                             .add(cmbPp1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(cmbPp2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(cmbPp3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(cmbPp0, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                     .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void cmbPokemonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbPokemonItemStateChanged
         if (!(evt.getStateChange() == ItemEvent.SELECTED)) return;
         int idx = cmbPokemon.getSelectedIndex();
         if ((idx == -1) || (evt.getStateChange() == ItemEvent.DESELECTED)) return;
         setPokemon(new Pokemon((String)cmbPokemon.getSelectedItem(), "", false,
                 Gender.GENDER_MALE, 100, null, null, null, new String[] {null, null, null, null},
                 new int[] {3,3,3,3}, new int[] {31,31,31,31,31,31}, new int[] {0,0,0,0,0,0}), false);
     }//GEN-LAST:event_cmbPokemonItemStateChanged
 
     private void cmbNatureItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbNatureItemStateChanged
         if (!(evt.getStateChange() == ItemEvent.SELECTED)) return;
         for (int i = 0; i < m_totals.length; i++) {
             updateStat(i);
         }
     }//GEN-LAST:event_cmbNatureItemStateChanged
 
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel baseAtk;
     private javax.swing.JLabel baseDef;
     private javax.swing.JLabel baseHp;
     private javax.swing.JLabel baseSpAtk;
     private javax.swing.JLabel baseSpDef;
     private javax.swing.JLabel baseSpd;
     private javax.swing.JCheckBox chkShiny;
     private javax.swing.JComboBox cmbAbility;
     private javax.swing.JComboBox cmbGender;
     private javax.swing.JComboBox cmbHiddenPower;
     private javax.swing.JComboBox cmbItem;
     private javax.swing.JComboBox cmbNature;
     private javax.swing.JComboBox cmbPokemon;
     private javax.swing.JComboBox cmbPp0;
     private javax.swing.JComboBox cmbPp1;
     private javax.swing.JComboBox cmbPp2;
     private javax.swing.JComboBox cmbPp3;
     private javax.swing.JTextField evAtk;
     private javax.swing.JTextField evDef;
     private javax.swing.JTextField evHp;
     private javax.swing.JTextField evSpAtk;
     private javax.swing.JTextField evSpDef;
     private javax.swing.JTextField evSpd;
     private javax.swing.JLabel evsTotal;
     private javax.swing.JTextField ivAtk;
     private javax.swing.JTextField ivDef;
     private javax.swing.JTextField ivHp;
     private javax.swing.JTextField ivSpAtk;
     private javax.swing.JTextField ivSpDef;
     private javax.swing.JTextField ivSpd;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel15;
     private javax.swing.JLabel jLabel18;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel21;
     private javax.swing.JLabel jLabel24;
     private javax.swing.JLabel jLabel25;
     private javax.swing.JLabel jLabel26;
     private javax.swing.JLabel jLabel27;
     private javax.swing.JLabel jLabel28;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JScrollPane scrollMoves;
     private javax.swing.JLabel totalAtk;
     private javax.swing.JLabel totalDef;
     private javax.swing.JLabel totalHp;
     private javax.swing.JLabel totalSpAtk;
     private javax.swing.JLabel totalSpDef;
     private javax.swing.JLabel totalSpd;
     private javax.swing.JTextField txtHiddenPower;
     private javax.swing.JTextField txtLevel;
     private javax.swing.JTextField txtNickname;
     // End of variables declaration//GEN-END:variables
 
 }
