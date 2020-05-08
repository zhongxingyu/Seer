 /*
  * BattleWindow.java
  *
  * Created on Apr 7, 2009, 11:51:16 PM
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
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.FileDialog;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.RenderingHints;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import javax.swing.ButtonGroup;
 import javax.swing.JOptionPane;
 import javax.swing.JToggleButton;
 import shoddybattleclient.ChatPane.CommandException;
 import shoddybattleclient.GameVisualisation.VisualPokemon;
 import shoddybattleclient.LobbyWindow.Channel;
 import shoddybattleclient.Preference.LogOption;
 import shoddybattleclient.network.ServerLink;
 import shoddybattleclient.shoddybattle.*;
 import shoddybattleclient.utils.*;
 
 /**
  *
  * @author ben
  */
 public class BattleWindow extends javax.swing.JFrame implements BattleField {
 
     private static enum Action {
         MOVE,
         SWITCH
     }
 
     private static enum TargetClass {
         NON_USER (true, true, false),
         ALLY (false, true, false),
         USER_OR_ALLY (false, true, true),
         ENEMY (true, false, false);
         // If opponents are targetable
         private boolean m_opp;
         // If allies are targetable
         private boolean m_ally;
         // If self is targetable
         private boolean m_self;
         private TargetClass(boolean opp, boolean ally, boolean self) {
             m_opp = opp;
             m_ally = ally;
             m_self = self;
         }
     }
 
     private static class Target {
         private String m_name;
         private int m_party;
         private int m_slot;
         private boolean m_enabled = true;
         public Target(String name, int party, int slot) {
             m_name = name;
             m_party = party;
             m_slot = slot;
         }
         public String toString() {
             return m_name + " " + m_party + " " + m_slot + " " + m_enabled;
         }
     }
 
     private class MoveButton extends JToggleButton {
         private int m_i, m_j;
         private PokemonMove m_move = null;
         public MoveButton() {
             setFocusPainted(false);
         }
         public void setMove(int i, int j, PokemonMove move) {
             m_i = i;
             m_j = j;
             m_move = move;
             repaint();
         }
         public PokemonMove getMove() {
             return m_move;
         }
         private int getPp() {
             int pp = m_pp[m_i][m_j];
             if (pp != -1) {
                 return pp;
             }
             return m_move.pp;
         }
         protected void paintComponent(Graphics g) {
             super.paintComponent(g);
             if (m_move == null) return;
             Graphics2D g2 = (Graphics2D)g.create();
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             if (!isEnabled()) {
                 g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
             }
             g2.setFont(g2.getFont().deriveFont(Font.BOLD).deriveFont(17f));
             g2.drawString(m_move.name, 10, 25);
             g2.setColor(Color.DARK_GRAY);
             g2.setFont(g2.getFont().deriveFont(Font.PLAIN).deriveFont(12f));
             int y = getHeight() - g2.getFontMetrics().getHeight();
             g2.drawString(m_move.type, 10, y);
             String pp = getPp() + "/" + m_maxPp[m_i][m_j];
             int left = getWidth() - g2.getFontMetrics().stringWidth(pp) - 5;
             g2.drawString(pp, left, y);
             g2.dispose();
         }
     }
 
     private static class SwitchButton extends JToggleButton {
         private Pokemon m_pokemon = null;
         public SwitchButton() {
             this.setFocusPainted(false);
         }
         public void setPokemon(Pokemon pokemon) {
             m_pokemon = pokemon;
             setText((m_pokemon == null) ? null : m_pokemon.species);
         }
         protected void paintComponent(Graphics g) {
             Graphics2D g2 = (Graphics2D)g.create();
             super.paintComponent(g2);
             if (m_pokemon == null) return;
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             if (!isEnabled()) {
                 g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
             }
             g2.setFont(g2.getFont().deriveFont(Font.BOLD));
             //g2.drawString(m_pokemon.species, 5, getHeight() / 2 - g2.getFontMetrics().getHeight() / 2 + 7);
             g2.dispose();
         }
     }
 
     private static class TargetButton extends JToggleButton {
         private final int m_target;
         public TargetButton(String text, int target) {
             super(text);
             m_target = target;
             setFocusPainted(false);
         }
         public int getTarget() {
             return m_target;
         }
     }
 
     private ServerLink m_link;
     private MoveButton[] m_moveButtons;
     private SwitchButton[] m_switches;
     private TargetButton[] m_targets = null;
     private GameVisualisation m_visual;
     private HealthBar[][] m_healthBars;
     private HTMLPane m_chat;
     private List<PokemonMove> m_moveList;
     private StringBuilder m_log = new StringBuilder();
     private int[][] m_maxPp;
     // Your Pokemon in this match
     private Pokemon[] m_pokemon;
     // Users in this match
     private String[] m_users;
     // Pp of moves
     private int[][] m_pp;
     // Your participant number in this battle
     private int m_participant;
     // This battle's field ID
     private int m_fid;
     // if we are forced to make a certain move
     private boolean m_forced = false;
     // if we are in the process of targeting
     private boolean m_targeting = false;
     // the number of pokemon on each team
     private int m_n;
     // the maximum team length
     private int m_length;
     // the pokemon we are currently selecting for
     private int m_current;
     // the move that we are targeting for
     private int m_selectedMove = -1;
 
     // whether the battle is finished
     private boolean m_finished = false;
 
     // the underlying channel
     private Channel m_channel;
 
     public int getPartySize() {
         return m_n;
     }
 
     public int getParticipant() {
         return m_participant;
     }
 
     /**
      * Constructor for spectators.
      */
     public BattleWindow(ServerLink link, int fid,
             int n, int length, String[] users) {
         this(link, fid, n, length, 0, users, null);
     }
 
     /** Creates new form BattleWindow */
     public BattleWindow(ServerLink link, int fid,
             int n,
             int length,
             int participant,
             String[] users,
             Pokemon[] team) {
         initComponents();
 
         setTitle(users[0] + " v. " + users[1] + " - Shoddy Battle");
 
         m_link = link;
         m_fid = fid;
         m_n = n;
         m_length = length;
         m_participant = participant;
         m_users = users;
         m_pokemon = team;
 
         m_channel = m_link.getLobby().getChannel(fid);
         listUsers.setModel(m_channel.getModel());
         listUsers.setCellRenderer(m_channel.getRenderer());
         
         if (m_participant == 0) {
             lblPlayer0.setText(users[0]);
             lblPlayer1.setText(users[1]);
         } else {
             lblPlayer0.setText(users[1]);
             lblPlayer1.setText(users[0]);
         }
         m_chat = new HTMLPane();
         m_chat.setTimeStampsEnabled(false);
         scrollChat.add(m_chat);
         scrollChat.setViewportView(m_chat);
 
         m_moveList = m_link.getMoveList();
 
         setupVisual();
 
         if (m_pokemon != null) {
             preparePlayer();
         } else {
             // We are spectating.
 
             // Hide the whole action area for now. Maybe we can come up with
             // a better use of this space later.
             tabAction.removeAll();
 
             // Set all of the health bars to use percents.
             if (m_healthBars != null) {
                 for (HealthBar[] i : m_healthBars) {
                     for (HealthBar j : i) {
                         j.setFraction(false);
                     }
                 }
             }
         }
     }
 
     public VisualPokemon getPokemon(int party, int idx) {
         return m_visual.getPokemon(party, idx);
     }
 
     private void preparePlayer() {
         createButtons();
         setMoves(0);
         updateSwitches();
 
         btnSwitch.setEnabled(false);
         btnSwitchCancel.setEnabled(false);
         btnMove.setEnabled(false);
         btnMoveCancel.setEnabled(false);
 
         m_pp = new int[m_pokemon.length][Pokemon.MOVE_COUNT];
         for (int i = 0; i < m_pp.length; ++i) {
             for (int j = 0; j < Pokemon.MOVE_COUNT; ++j) {
                 m_pp[i][j] = -1;
             }
         }
 
         m_maxPp = new int[m_pokemon.length][Pokemon.MOVE_COUNT];
         for (int i = 0; i < m_pokemon.length; i++) {
             Pokemon poke = m_pokemon[i];
             m_visual.setPokemon(m_participant, i, poke);
             VisualPokemon p = m_visual.getPokemon(m_participant, i);
             int hp = poke.calculateStat(Pokemon.S_HP, m_link.getSpeciesList());
             p.setHealth(hp, hp);
             for (int j = 0; j < m_pokemon[i].moves.length; j++) {
                 String move = m_pokemon[i].moves[j];
                 for (PokemonMove m : m_moveList) {
                     if (m.name.equals(move)) {
                         m_maxPp[i][j] = m.maxPp * (5 + m_pokemon[i].ppUps[j]) / 5;
                         m.pp = m_maxPp[i][j];
                         break;
                     }
                 }
             }
         }
     }
 
     public void refreshUsers() {
         listUsers.setModel(m_channel.getModel());
         listUsers.setCellRenderer(m_channel.getRenderer());
     }
 
     public String getTrainer(int party) {
         return m_users[party];
     }
 
     public void setPp(int pokemon, int move, int pp) {
         m_pp[pokemon][move] = pp;
     }
 
     public void setPokemonMove(int i, int j, int move, int pp, int maxPp) {
         String name = PokemonMove.getNameFromId(m_moveList, move);
         m_pokemon[i].moves[j] = name;
         m_pp[i][j] = pp;
         m_maxPp[i][j] = maxPp;
     }
 
     public void informVictory(int party) {
         // todo: improve this
         m_finished = true;
         String msg;
         if (party == -1) {
             msg = "It's a draw!";
         } else {
             msg = Text.formatTrainer(m_users[party], m_participant, party) + " wins!";
         }
         msg = Text.addClass(msg, "victory");
         addMessage(null, msg, false);
     }
 
     private void createButtons() {
         m_moveButtons = new MoveButton[4];
         panelMoves.setLayout(new GridLayout(2, 2));
         ButtonGroup moveButtons = new ButtonGroup();
         for (int i = 0; i < m_moveButtons.length; i++) {
             final int idx = i;
             final MoveButton button = new MoveButton();
             button.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseClicked(MouseEvent e) {
                     if (!button.isEnabled()) return;
                     if (e.getClickCount() == 2) {
                         sendMove(idx);
                     }
                 }
             });
             moveButtons.add(button);
             m_moveButtons[i] = button;
             panelMoves.add(button);
         }
 
         ButtonGroup switchButtons = new ButtonGroup();
         panelSwitch.setLayout(new GridLayout(3, 2));
         m_switches = new SwitchButton[m_pokemon.length];
         for (int i = 0; i < m_switches.length; i++) {
             final int idx = i;
             final SwitchButton button = new SwitchButton();
             button.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseClicked(MouseEvent e) {
                     if (!button.isEnabled()) return;
                     if (e.getClickCount() == 2) {
                         sendSwitch(idx);
                     }
                 }
             });
             switchButtons.add(button);
             m_switches[i] = button;
             panelSwitch.add(button);
         }
     }
 
     /**
      * Switches the move panel to show moves
      */
     private void showMoves() {
         if (m_moveButtons[0].isAncestorOf(panelMoves)) return;
         m_targeting = false;
         panelMoves.removeAll();
         panelMoves.setLayout(new GridLayout(2, 2));
         for (int i = 0; i < m_moveButtons.length; i++) {
             panelMoves.add(m_moveButtons[i]);
         }
         panelMoves.repaint();
     }
 
     /**
      * Switches the move panel to show targets
      * @param mode Some constant representing the kind of targeting this move has
      */
     private void showTargets(TargetClass mode) {
         m_targeting = true;
         panelMoves.removeAll();
         panelMoves.setLayout(new GridLayout(2, m_n));
         Target[][] targets = new Target[2][m_n];
         for (int i = 0; i < targets.length; i++) {
             boolean us = (i == m_participant);
             for (int j = 0; j < m_n; j++) {
                 Target t = new Target(m_visual.getPokemonForSlot(i, j).getName(), i, j);
                 if ((!us && !mode.m_opp) || 
                         (us && !mode.m_ally) ||
                         (us && (j == m_current) && !mode.m_self && (m_visual.getActiveCount(m_participant) > 1)) ||
                         (m_visual.getPokemonForSlot(i, j).isFainted())) {
                     t.m_enabled = false;
                 }
                 targets[i][j] = t;
             }
         }
         targets = (1 == m_participant) ? targets : new Target[][] {targets[1], targets[0]};
 
         m_targets = new TargetButton[m_n * 2];
         ButtonGroup bg = new ButtonGroup();
         for (int i = 0; i < targets.length; i++) {
             for (int j = j = m_n - 1; j >= 0; j--) {
                 Target t = targets[i][j];
                 final int idx = t.m_party * m_n + t.m_slot;
                 final TargetButton button = new TargetButton(t.m_name, idx);
                 button.addMouseListener(new MouseAdapter() {
                     @Override
                     public void mouseClicked(MouseEvent e) {
                         if (!button.isEnabled()) return;
                         m_visual.setTarget(idx);
                         if (e.getClickCount() >= 2) {
                             btnMoveActionPerformed(null);
                         }
                     }
                 });
                 button.setEnabled(t.m_enabled);
                 m_targets[idx] = button;
                 bg.add(button);
                 panelMoves.add(button);
             }
         }
         panelMoves.repaint();
     }
 
     private void setMoves(int i) {
         for (int j = 0; j < m_moveButtons.length; j++) {
             setMove(i, j, PokemonMove.getIdFromName(m_moveList, m_pokemon[i].moves[j]));
         }
     }
 
     private void setupVisual() {
         m_visual = new GameVisualisation(m_participant, m_n, m_length, m_link.getSpeciesList());
         m_visual.setSize(m_visual.getPreferredSize());
         int base = 20;
         int buffer = 5;
         int healthHeight = 35;
         int x = 10;
         m_visual.setLocation(x, base + healthHeight + buffer);
         int p1 = 1 - m_participant;
         int p2 = m_participant;
         
         if (m_n == 1) {
             m_healthBars = new HealthBar[2][1];
             m_healthBars[p1][0] = new HealthBar(false);
             m_healthBars[p1][0].setLocation(x, base);
             m_healthBars[p1][0].setSize(m_visual.getWidth(), healthHeight);
             m_healthBars[p2][0] = new HealthBar(true);
             m_healthBars[p2][0].setLocation(x, base + healthHeight + (2 * buffer) + m_visual.getHeight());
             m_healthBars[p2][0].setSize(m_visual.getWidth(), healthHeight);
             add(m_healthBars[0][0]);
             add(m_healthBars[1][0]);
         } else if (m_n == 2) {
             final int w = m_visual.getWidth() / 2 - 5;
             m_healthBars = new HealthBar[2][2];
             m_healthBars[p1][1] = new HealthBar(false);
             m_healthBars[p1][1].setLocation(x, base);
             m_healthBars[p1][1].setSize(w, healthHeight);
             m_healthBars[p1][0] = new HealthBar(false);
             m_healthBars[p1][0].setLocation(x + w + 10, base);
             m_healthBars[p1][0].setSize(w, healthHeight);
             m_healthBars[p2][1] = new HealthBar(true);
             m_healthBars[p2][1].setLocation(x, base + healthHeight + (2 * buffer) + m_visual.getHeight());
             m_healthBars[p2][1].setSize(w, healthHeight);
             m_healthBars[p2][0] = new HealthBar(true);
             m_healthBars[p2][0].setLocation(x + w + 10, base + healthHeight + (2 * buffer) + m_visual.getHeight());
             m_healthBars[p2][0].setSize(w, healthHeight);
             add(m_healthBars[0][0]);
             add(m_healthBars[0][1]);
             add(m_healthBars[1][0]);
             add(m_healthBars[1][1]);
         }
         add(m_visual);
     }
 
     public void setMove(int pokemon, int idx, int id) {
         if ((idx < 0) || (idx >= m_moveButtons.length)) return;
         for (PokemonMove move : m_moveList) {
             if (move.id == id) {
                 m_moveButtons[idx].setMove(pokemon, idx, move);
                 break;
             }
         }
     }
 
     /**
      * Request an action for a pokemon
      * @param idx the index of the pokemon
      */
     public void requestAction(int idx, int slot) {
         m_current = slot;
         showMoves();
         setMoves(idx);
         btnMove.setEnabled(true);
         btnSwitch.setEnabled(true);
         btnMoveCancel.setEnabled(false);
         btnSwitchCancel.setEnabled(false);
         tabAction.setSelectedIndex(0);
         if (m_n > 1) m_visual.setSelected(slot);
     }
 
     public void requestReplacement() {
         btnMove.setEnabled(false);
         btnMoveCancel.setEnabled(false);
         btnSwitch.setEnabled(true);
         btnSwitchCancel.setEnabled(false);
         tabAction.setSelectedIndex(1);
     }
 
     private void requestTarget(TargetClass mode) {
         showTargets(mode);
         btnMove.setEnabled(true);
         btnMoveCancel.setEnabled(false);
     }
 
     private void sendMove(int idx) {
         if (!btnMove.isEnabled()) return;
         m_selectedMove = idx;
        int defaultTarget = 1 - m_participant;
         String target = m_moveButtons[idx].getMove().target;
         if (m_n == 1) {
             sendAction(Action.MOVE, idx, defaultTarget);
         } else if ("Non-user".equalsIgnoreCase(target)) {
             requestTarget(TargetClass.NON_USER);
         } else if ("Ally".equalsIgnoreCase(target)) {
             requestTarget(TargetClass.ALLY);
         } else if ("User or ally".equalsIgnoreCase(target)) {
             requestTarget(TargetClass.USER_OR_ALLY);
         } else if ("Enemy".equalsIgnoreCase(target)) {
             requestTarget(TargetClass.ENEMY);
         } else {
             sendAction(Action.MOVE, idx, defaultTarget);
         }
     }
 
     private void sendSwitch(int idx) {
         if (!btnSwitch.isEnabled()) return;
         sendAction(Action.SWITCH, idx, 0);
         btnSwitch.setEnabled(false);
         btnSwitchCancel.setEnabled(true);
     }
 
     private void sendAction(Action action, int idx, int target) {
         if (action == Action.MOVE) {
             m_link.sendMoveAction(m_fid, idx, target);
         } else {
             m_link.sendSwitchAction(m_fid, idx);
         }
         btnMove.setEnabled(false);
         btnMoveCancel.setEnabled(false);
         btnSwitch.setEnabled(false);
         btnSwitchCancel.setEnabled(false);
     }
 
     public void setValidMoves(boolean[] valid) {
         boolean struggle = true;
         for (int i = 0; i < m_moveButtons.length; i++) {
             m_moveButtons[i].setEnabled(valid[i]);
             if (valid[i]) struggle = false;
         }
         if (struggle && !m_forced) {
             btnMove.setText("Struggle");
         }
     }
 
     public void setValidSwitches(boolean[] valid) {
         for (int i = 0; i < m_switches.length; i++) {
             m_switches[i].setEnabled(valid[i]);
         }
     }
 
     public void updateHealth(int party, int slot, int total, int denominator) {
         if (m_n <= 2) {
             m_healthBars[party][slot].setRatio(total, denominator);
         }
         m_visual.updateHealth(party, slot, total, denominator);
     }
 
     public void updateStatLevel(int party, int slot, int stat, int level) {
         m_visual.updateStatLevel(party, slot, stat, level);
     }
 
     public void faint(int party, int slot) {
         m_visual.faint(party, slot);
     }
 
     private void updateSwitches() {
         for (int i = 0; i < m_pokemon.length; i++) {
             Pokemon p = m_pokemon[i];
             m_switches[i].setPokemon(p);
             StringBuilder builder = new StringBuilder();
             builder.append("<html>");
             builder.append(p.species);
             builder.append("<br>");
             builder.append(p.ability);
             builder.append("<br>");
             builder.append(p.item);
             builder.append("<br><br>");
             for (int j = 0; j < p.moves.length; j++) {
                 builder.append("-");
                 builder.append(p.moves[j]);
                 builder.append("<br>");
             }
             builder.append("</html>");
             m_switches[i].setToolTipText(builder.toString());
         }
     }
 
     public void setSpecies(int party, int slot, String species) {
         if (m_n == 2) {
             m_healthBars[party][slot].setToolTipText(species);
         }
         m_visual.setSpecies(party, slot, species);
     }
 
     public void setPokemon(VisualPokemon[] p1, VisualPokemon[] p2) {
         m_visual.setActive(p1, p2);
     }
 
     public void sendOut(int party, int slot, int index, String name) {
         m_visual.sendOut(party, slot, index, name);
         if (m_n <= 2) {
             HealthBar bar = m_healthBars[party][slot];
             VisualPokemon p = m_visual.getPokemonForSlot(party, slot);
             bar.setRatio(p.getNumerator(), p.getDenominator(), false);
         }
     }
 
     public void setForced(boolean forced) {
         m_forced = forced;
         if (forced) {
             setValidMoves(new boolean[] { false, false, false, false });
         }
     }
 
     public void setSpriteVisible(int party, int slot, boolean visible) {
         m_visual.setSpriteVisible(party, slot, visible);
     }
 
     public String getNameForSlot(int party, int slot) {
         VisualPokemon p = m_visual.getPokemonForSlot(party, slot);
         return (p != null) ? p.getName() : null;
     }
 
     public String getName(int party, int index) {
         VisualPokemon p = m_visual.getPokemon(party, index);
         return (p == null) ? null : p.getName();
     }
 
     public int getParty() {
         return m_participant;
     }
 
     public String getLadder() {
         //todo: implement this
         return "Standard";
     }
 
     private File getDefaultLogPath() {
         String path = Preference.getLogDirectory();
         path += "battles" + File.separator + getLadder() + File.separator;
         String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
         path += date + File.separator;
         path += m_users[0] + " vs " + m_users[1];
         File f = new File(path + ".txt");
         int num = 1;
         while (f.exists()) {
             f = new File(path + " " + num + ".txt");
         }
         return f;
     }
 
     private void saveLog(String path) {
         File f = new File(path);
         f.getParentFile().mkdirs();
         try {
             PrintWriter writer = new PrintWriter(new FileWriter(f));
             writer.print(m_log.toString());
             writer.flush();
             writer.close();
         } catch (Exception e) {
             JOptionPane.showMessageDialog(this, "Failed to save log file " + path);
         }
     }
 
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         txtChat = new javax.swing.JTextField();
         tabAction = new javax.swing.JTabbedPane();
         jPanel4 = new javax.swing.JPanel();
         panelMoves = new javax.swing.JPanel();
         jPanel1 = new javax.swing.JPanel();
         btnMoveCancel = new javax.swing.JButton();
         btnMove = new javax.swing.JButton();
         jPanel3 = new javax.swing.JPanel();
         panelSwitch = new javax.swing.JPanel();
         jPanel2 = new javax.swing.JPanel();
         btnSwitchCancel = new javax.swing.JButton();
         btnSwitch = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         listUsers = new javax.swing.JList();
         scrollChat = new javax.swing.JScrollPane();
         lblPlayer0 = new javax.swing.JLabel();
         lblPlayer1 = new javax.swing.JLabel();
         lblClock0 = new javax.swing.JLabel();
         lblClock1 = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
         setLocationByPlatform(true);
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 formWindowClosing(evt);
             }
         });
 
         txtChat.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 txtChatFocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 txtChatFocusLost(evt);
             }
         });
         txtChat.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 txtChatKeyReleased(evt);
             }
         });
 
         jPanel4.setOpaque(false);
 
         panelMoves.setOpaque(false);
 
         org.jdesktop.layout.GroupLayout panelMovesLayout = new org.jdesktop.layout.GroupLayout(panelMoves);
         panelMoves.setLayout(panelMovesLayout);
         panelMovesLayout.setHorizontalGroup(
             panelMovesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 342, Short.MAX_VALUE)
         );
         panelMovesLayout.setVerticalGroup(
             panelMovesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 120, Short.MAX_VALUE)
         );
 
         jPanel1.setOpaque(false);
         jPanel1.setLayout(new java.awt.GridLayout(1, 2));
 
         btnMoveCancel.setText("Cancel");
         btnMoveCancel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnMoveCancelActionPerformed(evt);
             }
         });
         jPanel1.add(btnMoveCancel);
 
         btnMove.setText("Attack");
         btnMove.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnMoveActionPerformed(evt);
             }
         });
         jPanel1.add(btnMove);
 
         org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(panelMoves, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                 .add(panelMoves, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .add(7, 7, 7)
                 .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         tabAction.addTab("Move", jPanel4);
 
         jPanel3.setOpaque(false);
 
         panelSwitch.setOpaque(false);
 
         org.jdesktop.layout.GroupLayout panelSwitchLayout = new org.jdesktop.layout.GroupLayout(panelSwitch);
         panelSwitch.setLayout(panelSwitchLayout);
         panelSwitchLayout.setHorizontalGroup(
             panelSwitchLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 342, Short.MAX_VALUE)
         );
         panelSwitchLayout.setVerticalGroup(
             panelSwitchLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 120, Short.MAX_VALUE)
         );
 
         jPanel2.setOpaque(false);
         jPanel2.setLayout(new java.awt.GridLayout(1, 2));
 
         btnSwitchCancel.setText("Cancel");
         btnSwitchCancel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSwitchCancelActionPerformed(evt);
             }
         });
         jPanel2.add(btnSwitchCancel);
 
         btnSwitch.setText("Switch");
         btnSwitch.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSwitchActionPerformed(evt);
             }
         });
         jPanel2.add(btnSwitch);
 
         org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(panelSwitch, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                 .add(panelSwitch, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .add(7, 7, 7)
                 .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         tabAction.addTab("Switch", jPanel3);
 
         jScrollPane1.setViewportView(listUsers);
 
         lblPlayer0.setText("Player 1");
 
         lblPlayer1.setText("Player 2");
 
         lblClock0.setText("20:00:00");
 
         lblClock1.setText("20:00:00");
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                     .add(layout.createSequentialGroup()
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(layout.createSequentialGroup()
                                 .add(lblPlayer1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 80, Short.MAX_VALUE)
                                 .add(lblClock1))
                             .add(layout.createSequentialGroup()
                                 .add(lblPlayer0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                                 .add(80, 80, 80)
                                 .add(lblClock0)))
                         .add(32, 32, 32)
                         .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .add(7, 7, 7))
                     .add(tabAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 363, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(txtChat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                     .add(scrollChat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE))
                 .addContainerGap())
         );
 
         layout.linkSize(new java.awt.Component[] {lblPlayer0, lblPlayer1}, org.jdesktop.layout.GroupLayout.HORIZONTAL);
 
         layout.linkSize(new java.awt.Component[] {lblClock0, lblClock1}, org.jdesktop.layout.GroupLayout.HORIZONTAL);
 
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(lblPlayer1)
                     .add(lblClock1))
                 .add(4, 4, 4)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                         .add(scrollChat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(txtChat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(layout.createSequentialGroup()
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                             .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                 .add(lblPlayer0)
                                 .add(lblClock0))
                             .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 244, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(tabAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 222, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void txtChatFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtChatFocusGained
 
 }//GEN-LAST:event_txtChatFocusGained
 
     private void txtChatFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtChatFocusLost
 
     }//GEN-LAST:event_txtChatFocusLost
 
     public void addMessage(String user, String message) {
         m_chat.addMessage(user, message);
     }
 
     public void addMessage(String user, String message, boolean encode) {
         m_chat.addMessage(user, message, encode);
 
         if (!encode) message = Text.stripTags(message);
         String str = message;
         if (user != null) {
             user = Text.stripTags(user);
             str = user + ": " + message;
         }
         m_log.append(str);
         m_log.append("\n");
     }
 
     private void txtChatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtChatKeyReleased
         if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
             try {
                 m_link.sendBattleMessage(m_fid, txtChat.getText());
             } catch (CommandException e) {
                 addMessage(null, e.getMessage());
             }
             txtChat.setText("");
         }
     }//GEN-LAST:event_txtChatKeyReleased
 
     private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
         int result = -1;
         if (!m_finished && (m_pokemon != null)) {
             result = JOptionPane.showConfirmDialog(this, "Leaving will cause you " +
                 "to forfeit this battle. Are you sure you want to leave?",
                 "Leaving Battle", JOptionPane.YES_NO_OPTION);
         }
         if ((result == -1) || (result == JOptionPane.YES_OPTION)) {
             LogOption opt = Preference.getBattleLogOption();
             boolean save = (LogOption.ALWAYS_SAVE.equals(opt));
             File path = getDefaultLogPath();
             if (!save && (LogOption.PROMPT.equals(opt))) {
                 String options[] = new String[] {"Yes, to default", "Yes, to...", "No"};
                 int option = JOptionPane.showOptionDialog(this, "Would you like " +
                         "to save this battle log?", "Save Log?", JOptionPane.YES_NO_OPTION,
                         JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                 save = (option < 2);
                 if (option == 1) {
                     FileDialog fd = new FileDialog(this, "Save log to file...", FileDialog.SAVE);
                     fd.setDirectory(path.getParent());
                     fd.setFile(path.getName());
                     fd.setVisible(true);
                     String file = fd.getDirectory() + fd.getFile();
                     if (file != null) {
                         path = new File(file);
                     } else {
                         save = false;
                     }
                 }
             }
             if (save) {
                 saveLog(path.toString());
             }
             m_link.partChannel(m_channel.getId());
             dispose();
         }
     }//GEN-LAST:event_formWindowClosing
 
     private void btnMoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveActionPerformed
         int selected = -1;
         if (m_targeting) {
             for (int i = 0; i < m_targets.length; i++) {
                 if (m_targets[i].isSelected() &&
                         m_targets[i].isEnabled()) {
                     selected = m_targets[i].getTarget();
                     m_visual.setTarget(Integer.MAX_VALUE);
                     break;
                 }
             }
             if (selected != -1) {
                 sendAction(Action.MOVE, m_selectedMove, selected);
             }
         } else if (m_forced) {
             // Forced move.
             m_link.sendMoveAction(m_fid, -1, -1);
         } else {
             for (int i = 0; i < m_moveButtons.length; i++) {
                 if (m_moveButtons[i].isSelected() &&
                         m_moveButtons[i].isEnabled()) {
                     selected = i;
                     break;
                 }
             }
             if (selected != -1) {
                 sendMove(selected);
             }
         }
     }//GEN-LAST:event_btnMoveActionPerformed
 
     private void btnSwitchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSwitchActionPerformed
         int selected = -1;
         for (int i = 0; i < m_switches.length; i++) {
             if (m_switches[i].isSelected()) {
                 selected = i;
                 break;
             }
         }
         if (selected == -1) return;
         sendSwitch(selected);
     }//GEN-LAST:event_btnSwitchActionPerformed
 
     private void btnMoveCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveCancelActionPerformed
         btnMove.setEnabled(true);
         btnMoveCancel.setEnabled(false);
     }//GEN-LAST:event_btnMoveCancelActionPerformed
 
     private void btnSwitchCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSwitchCancelActionPerformed
         btnSwitch.setEnabled(true);
         btnSwitchCancel.setEnabled(false);
     }//GEN-LAST:event_btnSwitchCancelActionPerformed
 
     /**
     * @param args the command line arguments
     */
     public static void main(String args[]) {
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 try {
                     //javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                 } catch (Exception e) {
                     
                 }
                 TeamFileParser tfp = new TeamFileParser();
                 //Pokemon[] pokemon = tfp.parseTeam("/Users/ben/team1.sbt");
                 BattleWindow battle = new BattleWindow(null, 0, 1, 6, new String[] { "bearzly", "Catherine" });
                 battle.setPokemon(new VisualPokemon[] {new VisualPokemon("Wartortle", 1, false)},
                         new VisualPokemon[] {new VisualPokemon("Groudon", 0, true)});
                 battle.setVisible(true);
                 battle.requestAction(2, 0);
                 battle.updateHealth(0, 0, 38, 48);
                 battle.updateStatLevel(0, 0, 2, -3);
                 battle.setSpriteVisible(0, 0, false);
             }
         });
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnMove;
     private javax.swing.JButton btnMoveCancel;
     private javax.swing.JButton btnSwitch;
     private javax.swing.JButton btnSwitchCancel;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JLabel lblClock0;
     private javax.swing.JLabel lblClock1;
     private javax.swing.JLabel lblPlayer0;
     private javax.swing.JLabel lblPlayer1;
     private javax.swing.JList listUsers;
     private javax.swing.JPanel panelMoves;
     private javax.swing.JPanel panelSwitch;
     private javax.swing.JScrollPane scrollChat;
     private javax.swing.JTabbedPane tabAction;
     private javax.swing.JTextField txtChat;
     // End of variables declaration//GEN-END:variables
 
 }
