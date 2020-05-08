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
 import java.awt.Dimension;
 import java.awt.FileDialog;
 import java.awt.Font;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.swing.ButtonGroup;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 import javax.swing.JToolTip;
 import javax.swing.KeyStroke;
 import javax.swing.Timer;
 import shoddybattleclient.ChatPane.CommandException;
 import shoddybattleclient.GameVisualisation.VisualPokemon;
 import shoddybattleclient.LobbyWindow.Channel;
 import shoddybattleclient.LobbyWindow.UserCellRenderer;
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
 
     private static final Map<String, Color> m_colourMap = new HashMap<String, Color>();
     static {
         m_colourMap.put("Normal", new Color(0xb2a5b2));
         m_colourMap.put("Fire", new Color(0xf9532c));
         m_colourMap.put("Water", new Color(0x4a9cf1));
         m_colourMap.put("Electric", new Color(0xebe96a));
         m_colourMap.put("Grass", new Color(0x2ecf4d));
         m_colourMap.put("Ice", new Color(0x6ce0e4));
         m_colourMap.put("Fighting", new Color(0xa3694d));
         m_colourMap.put("Poison", new Color(0x8956a4));
         m_colourMap.put("Ground", new Color(0xa47c56));
         m_colourMap.put("Flying", new Color(0xeeae52));
         m_colourMap.put("Psychic", new Color(0xe580e1));
         m_colourMap.put("Bug", new Color(0x7bf091));
         m_colourMap.put("Rock", new Color(0x4a2d0e));
         m_colourMap.put("Ghost", new Color(0x7d679a));
         m_colourMap.put("Dragon", new Color(0x1ba8cb));
         m_colourMap.put("Dark", new Color(0x221448));
         m_colourMap.put("Steel", new Color(0x7b7b7b));
         m_colourMap.put("Typeless", new Color(0x2299a7));
     }
 
     /**
      * JToggleButton subclass that has a fancy gradient. This will be used
      * by the move, switch, and target buttons
      */
     private class GradientButton extends JToggleButton {
         //The gradient will be derived from this colour. It should be the
         //colour for the top half of the button
         protected Color m_colour;
 
         //this is the default font colour for a selected button
         protected Color SELECTION_FONT = new Color(0xffe42d);
         //this is the default font colour for an active button
         //e.g. the active pokemon
         protected Color ACTIVE_FONT = new Color(0x78dcee);
 
         public GradientButton() {
             setFocusPainted(false);
         }
         protected void paintComponent(Graphics g) {
             Graphics2D g2 = (Graphics2D)g.create();
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                     RenderingHints.VALUE_ANTIALIAS_ON);
 
             if (!isEnabled()) {
                 g2.setComposite(
                     AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
             }
 
             Color c = m_colour;
             int diff = 20;
             int red = c.getRed() - diff;
             if (red < 0) red = 0;
             int green = c.getGreen() - diff;
             if (green < 0) green = 0;
             int blue = c.getBlue() - diff;
             if (blue < 0) blue = 0;
             Color c2 = new Color(red, green, blue);
             int w = getWidth();
             int h = getHeight();
             if(isSelected())
                 g2.setColor(SELECTION_FONT);
             else
                 g2.setColor(c.darker());
             g2.fillRoundRect(0, 0, w, h, 5, 5);
             g2.setPaint(new GradientPaint(0, h/2, c2, 0, h-2, c2.darker()));
             g2.fillRect(2, h/2, w-4, h/2 - 2);
             g2.setPaint(new GradientPaint(0, 0, c.brighter(), 0, h/2-2, c));
             g2.fillRect(2, 2, w - 4, h/2 - 2);
             if (isSelected()) {
                 g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.20f));
                 g2.setColor(Color.BLACK);
                 g2.fillRect(2, 2, w-4, h-4);
                 g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
             }
             g2.dispose();
         }
     }
 
     private class MoveButton extends GradientButton {
         private int m_i, m_j;
         private PokemonMove m_move = null;
         public void setMove(int i, int j, PokemonMove move) {
             m_i = i;
             m_j = j;
             m_move = move;
             if (move != null)
                 m_colour = m_colourMap.get(move.type);
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
             if (m_move == null) return;
             super.paintComponent(g);
             Graphics2D g2 = (Graphics2D)g.create();
             g2.setFont(g2.getFont().deriveFont(Font.BOLD).deriveFont(17f));
             g2.setColor(Color.GRAY);
             g2.drawString(m_move.name, 11, 26);
             if (isSelected()) {
                 //todo: other defining features
                 g2.setColor(SELECTION_FONT);
             } else {
                 g2.setColor(Color.WHITE);
             }
             g2.drawString(m_move.name, 10, 25);
             
             g2.setFont(g2.getFont().deriveFont(Font.PLAIN).deriveFont(12f));
             int y = getHeight() - g2.getFontMetrics().getHeight() + 4;
             g2.drawString(m_move.type, 10, y);
             String pp = getPp() + "/" + m_maxPp[m_i][m_j];
             int left = getWidth() - g2.getFontMetrics().stringWidth(pp) - 5;
             g2.drawString(pp, left, y);
             g2.dispose();
         }
     }
 
     private class SwitchButton extends GradientButton {
         private int m_index;
         private final Color HEALTH_COLOUR = new Color(0x28ae3b);
         public SwitchButton(int index) {
             m_index = index;
             m_colour = new Color(0x4b5278);
             setToolTipText("asdf");
         }
         public int getRealIndex() {
             return m_sortedPokemon.get(m_index);
         }
         @Override
         public JToolTip createToolTip() {
             return new JCustomTooltip(this, new VisualToolTip(getPokemon(
                     m_participant, getRealIndex()), true));
         }
         protected void paintComponent(Graphics g) {
             VisualPokemon pokemon = getPokemon(m_participant, getRealIndex());
             if (pokemon == null) return;
             super.paintComponent(g);
             Graphics2D g2 = (Graphics2D)g.create();
             if (!isEnabled()) {
                 g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
             }
             Color fontCol = isSelected() ? SELECTION_FONT : Color.WHITE;
             if (pokemon.getSlot() != -1) fontCol = ACTIVE_FONT;
             if (pokemon.isFainted()) {
                 fontCol = Color.DARK_GRAY;
             }
             
             g2.setFont(g2.getFont().deriveFont(Font.BOLD));
             int speciesY = getHeight() / 2 - g2.getFontMetrics().getHeight() / 2 + 4;
             g2.setColor(Color.GRAY);
             g2.drawString(pokemon.getSpecies(), 6, speciesY + 1);
             g2.setColor(fontCol);
             g2.drawString(pokemon.getSpecies(), 5, speciesY);
             
             int n = pokemon.getNumerator();
             int d = pokemon.getDenominator();
             int y = getHeight() / 2 + 3;
             int x = 3;
             int healthW = getWidth() / 2 - x;
             double frac = (double)n / d;
             if (frac < 0) frac = 0;
             g2.setColor(Color.BLACK);
             g2.drawRect(x, y, healthW, 10);
             g2.setColor(HEALTH_COLOUR);
             int barW = (int)(healthW * frac) - 1;
             g2.fillRect(x + 1, y + 1, barW, 9);
             g2.setColor(Color.DARK_GRAY);
             g2.fillRect(x + 1 + barW, y + 1, healthW - barW - 1, 9);
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
 
     public static class TimerLabel extends JLabel {
         private static final int FULL_RADIUS = 6;
         private static final int EMPTY_RADIUS = 3;
         private static final int PAD_X = 3;
         private static final int PAD_Y = 2;
         
         private int m_time = -1;
         private int m_periods = -1;
         private int m_maxPeriods = -1;
         private int m_periodLength = -1;
         public void setPeriodLength(int length) {
             m_periodLength = length;
         }
         public void setMaxPeriods(int periods) {
             m_maxPeriods = periods;
         }
         public void update(int time, int periods) {
             m_time = time;
             m_periods = periods;
             int minutes = time / 60;
             int seconds = time % 60;
             StringBuilder sb = new StringBuilder();
             sb.append(minutes);
             sb.append(":");
             if (seconds < 10) sb.append(0);
             sb.append(seconds);
             setText(sb.toString());
             repaint();
         }
         public void tick() {
             if (m_time < 0) {
                 return;
             }
             int time = m_time;
             int periods = m_periods;
             if (time-- == 0) {
                 time = m_periodLength;
                 periods--;
             }
             if (periods >= 0) update(time, periods);
         }
         @Override
         public Dimension getPreferredSize() {
             Dimension d = super.getPreferredSize();
             int cols = (m_maxPeriods) > 5 ? 5 : m_maxPeriods;
             return new Dimension(d.width + cols * (FULL_RADIUS + PAD_X) + 10, d.height);
         }
 
         @Override
         public void paintComponent(Graphics g) {
             if (m_maxPeriods == 0) return;
             Graphics2D g2 = (Graphics2D)g.create();
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                     RenderingHints.VALUE_ANTIALIAS_ON);
             super.paintComponent(g2);
             int x = super.getPreferredSize().width + 5;
             g2.setColor(Color.BLACK);
             for (int i = 0; i < m_maxPeriods; i++) {
                 int y;
                 int left;
                 if (m_maxPeriods <= 5) {
                     y = 5;
                     left = x + i * (FULL_RADIUS + PAD_X);
                 } else {
                     y = 2 + (i / 5) * (FULL_RADIUS + PAD_Y);
                     left = x + (i % 5) * (FULL_RADIUS + PAD_X);
                 }
                 int radius;
                 if (i < m_periods) {
                     radius = FULL_RADIUS;
                 } else {
                     radius = EMPTY_RADIUS;
                     int diff = (FULL_RADIUS - EMPTY_RADIUS) / 2;
                     y += diff;
                 }
                 
                 g2.fillOval(left, y, radius, radius);
             }
             g2.dispose();
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
     // Your Pokemon mapped from display ID to real ID
     private ArrayList<Integer> m_sortedPokemon;
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
     // if timing is enabled
     private boolean m_timing;
     // Timer that controls the clock display
     private Timer m_timer;
 
     // whether the battle is finished
     private boolean m_finished = false;
 
     // the underlying channel
     private Channel m_channel;
 
     // This hack is required to unclear a button group in Java 5.
     private JToggleButton m_dummySwitchButton;
 
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
             int n, int length, String[] users, int maxPeriods) {
         this(link, fid, n, length, 0, users, null, maxPeriods, -1, null);
     }
 
     /** Creates new form BattleWindow */
     public BattleWindow(ServerLink link, int fid,
             int n,
             int length,
             int participant,
             String[] users,
             Pokemon[] team,
             int maxPeriods,
             int periodLength,
             String uid) {
         initComponents();
 
         String title = users[0] + " v. " + users[1];
         if (uid != null) {
             title += " {" + uid + "}";
         }
         setTitle(title);
 
         m_link = link;
         m_fid = fid;
         m_n = n;
         m_length = length;
         m_participant = participant;
         m_users = users;
         m_pokemon = team;
         m_timing = (maxPeriods != -1) && (periodLength != -1);
         ((TimerLabel)lblClock0).setMaxPeriods(maxPeriods);
         ((TimerLabel)lblClock1).setMaxPeriods(maxPeriods);
         if (m_timing) ((TimerLabel)lblClock0).setPeriodLength(periodLength);
 
         m_channel = m_link.getLobby().getChannel(fid);
         listUsers.setModel(m_channel.getModel());
         listUsers.setCellRenderer(new UserCellRenderer());
 
         mnuLeaveBattle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
               Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         
         if (m_participant == 0) {
             lblPlayer1.setText(users[1]);
             lblPlayer0.setText(users[0]);
         } else {
             lblPlayer1.setText(users[0]);
             lblPlayer0.setText(users[1]);
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
             tabAction.add("Spectator", new JPanel());
 
             // Set all of the health bars to use percents.
             if (m_healthBars != null) {
                 for (HealthBar[] i : m_healthBars) {
                     for (HealthBar j : i) {
                         j.setFraction(false);
                     }
                 }
             }
         }
 
         if (uid != null) {
             addMessage(null, "The unique ID for this battle is {" +
                     uid + "}.");
         }
     }
 
     public VisualPokemon getPokemonForSlot(int party, int slot) {
         return m_visual.getPokemonForSlot(party, slot);
     }
     
     public VisualPokemon getPokemon(int party, int idx) {
         return m_visual.getPokemon(party, idx);
     }
 
     private void setTicking(boolean tick) {
         if (m_timing) {
             if (tick) {
                 m_timer.start();
             } else {
                 m_timer.stop();
             }
         }
     }
 
     private void preparePlayer() {
         if (m_timing) {
             m_timer = new Timer(1000, new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     ((TimerLabel)lblClock0).tick();
                 }
             });
             m_timer.start();
         }
 
         if (m_pokemon.length > m_length) {
             Pokemon[] temp = new Pokemon[m_length];
             for (int i = 0; i < m_length; i++) {
                 temp[i] = m_pokemon[i];
             }
             m_pokemon = temp;
         }
         
         createButtons();
         setMoves(0);
 
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
         m_sortedPokemon = new ArrayList<Integer>();
         for (int i = 0; i < m_pokemon.length; i++) {
             m_sortedPokemon.add(i);
             Pokemon poke = m_pokemon[i];
             m_visual.setPokemon(m_participant, i, poke);
             VisualPokemon p = m_visual.getPokemon(m_participant, i);
             int hp = poke.calculateStat(Pokemon.S_HP, ServerLink.getSpeciesList(), 0);
             p.setHealth(hp, hp);
             for (int j = 0; j < m_pokemon[i].moves.length; j++) {
                 String move = m_pokemon[i].moves[j];
                 for (PokemonMove m : m_moveList) {
                     if (m.name.equalsIgnoreCase(move)) {
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
         setTicking(false);
         m_finished = true;
         String msg;
         if (party == -1) {
             msg = "It's a draw!";
         } else {
             msg = Text.formatTrainer(m_users[party], m_participant, party) + " wins!";
         }
         msg = Text.addClass(msg, "victory");
         addMessage(null, msg, false);
 
         if (m_pokemon != null) {
             btnMove.setEnabled(false);
             btnMoveCancel.setEnabled(false);
             btnSwitch.setEnabled(false);
             btnSwitchCancel.setEnabled(false);
             for (MoveButton button : m_moveButtons) {
                 button.setEnabled(false);
             }
         }
     }
 
     private void createButtons() {
         m_moveButtons = new MoveButton[4];
         panelMoves.setLayout(new GridLayout(2, 2, 3, 3));
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
         panelSwitch.setLayout(new GridLayout(3, 2, 3, 3));
         m_switches = new SwitchButton[m_pokemon.length];
         for (int i = 0; i < m_switches.length; i++) {
             final int idx = i;
             final SwitchButton button = new SwitchButton(idx);
             button.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseClicked(MouseEvent e) {
                     if (!button.isEnabled()) return;
                     if (e.getClickCount() == 2) {
                         sendSwitch(button.getRealIndex());
                     }
                 }
             });
             switchButtons.add(button);
             m_switches[i] = button;
             panelSwitch.add(button);
         }
         m_dummySwitchButton = new JToggleButton();
         switchButtons.add(m_dummySwitchButton);
     }
 
     /**
      * Switches the move panel to show moves
      */
     private void showMoves() {
         if (m_moveButtons[0].isAncestorOf(panelMoves)) return;
         m_targeting = false;
         panelMoves.removeAll();
         panelMoves.setLayout(new GridLayout(2, 2, 3, 3));
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
             for (int j = 0; j < m_n; j++) {
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
             if (j >= m_pokemon[i].moves.length) {
                 setMove(i, j, -1);
             } else {
                 setMove(i, j, PokemonMove.getIdFromName(m_moveList, m_pokemon[i].moves[j]));
             }
         }
     }
 
     private void setupVisual() {
         m_visual = new GameVisualisation(m_participant, m_n, m_length, m_link.getSpeciesList());
         m_visual.setSize(m_visual.getPreferredSize());
         panelVisual.add(m_visual);
         if (m_n > 2) return;
         int p1 = m_participant;
         int p2 = 1 - p1;
         if (m_n == 1) {
             ((GridLayout)panelHealth0.getLayout()).setColumns(1);
             ((GridLayout)panelHealth1.getLayout()).setColumns(1);
             m_healthBars = new HealthBar[2][1];
             m_healthBars[0][0] = new HealthBar(false);
             m_healthBars[1][0] = new HealthBar(true);
             panelHealth0.add(m_healthBars[p1][0]);
             panelHealth1.add(m_healthBars[p2][0]);
         } else if (m_n == 2) {
             ((GridLayout)panelHealth0.getLayout()).setColumns(2);
             ((GridLayout)panelHealth1.getLayout()).setColumns(2);
             m_healthBars = new HealthBar[2][2];
             m_healthBars[0][1] = new HealthBar(false);
             m_healthBars[0][0] = new HealthBar(false);
             m_healthBars[1][1] = new HealthBar(true);
             m_healthBars[1][0] = new HealthBar(true);
             panelHealth0.add(m_healthBars[p1][0]);
             panelHealth0.add(m_healthBars[p1][1]);
             panelHealth1.add(m_healthBars[p2][0]);
             panelHealth1.add(m_healthBars[p2][1]);
         }
         for (int i = 0; i < m_healthBars[p1].length; i++) {
             m_healthBars[p1][i].setFraction(true);
             m_healthBars[p2][i].setFraction(false);
         }
     }
 
     public void setMove(int pokemon, int idx, int id) {
         if ((idx < 0) || (idx >= m_moveButtons.length)) return;
         if (id < 0) {
             m_moveButtons[idx].setMove(pokemon, idx, null);
             return;
         }
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
         m_dummySwitchButton.setSelected(true);
         setTicking(true);
     }
 
     public void requestReplacement() {
         btnMove.setEnabled(false);
         btnMoveCancel.setEnabled(false);
         btnSwitch.setEnabled(true);
         btnSwitchCancel.setEnabled(false);
         tabAction.setSelectedIndex(1);
         for (MoveButton button : m_moveButtons) {
             button.setEnabled(false);
         }
         setTicking(true);
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
         for (MoveButton button : m_moveButtons) {
             button.setEnabled(false);
         }
         setTicking(false);
     }
 
     public void setValidMoves(boolean[] valid) {
         boolean struggle = true;
         for (int i = 0; i < m_moveButtons.length; i++) {
             boolean allowed = (i < valid.length) ? valid[i] : false;
             m_moveButtons[i].setEnabled(allowed);
             if (allowed) struggle = false;
         }
 
         if (m_forced) {
             //todo: Determine which move has been forced
             btnMove.setText("Forced Move");
         } else {
             btnMove.setText("Attack");
         }
     }
 
     public void setValidSwitches(boolean[] valid) {
         boolean canSwitch = false;
         for (int i = 0; i < m_switches.length; i++) {
             for (int j = 0; j < m_switches.length; j++) {
                 if (m_sortedPokemon.get(j) == i) {
                     m_switches[j].setEnabled(valid[i]);
                 }
             }
             if (valid[i]) canSwitch = true;
         }
 
         if (!canSwitch) {
             btnSwitch.setEnabled(false);
             btnSwitchCancel.setEnabled(false);
         }
     }
 
     public void updateHealth(int party, int slot, int total, int denominator) {
         if (m_n <= 2) {
             m_healthBars[party][slot].setRatio(total, denominator);
         }
         m_visual.updateHealth(party, slot, total, denominator);
     }
 
     public void updateSprite(int party, int slot) {
         m_visual.updateSprite(party, slot);
     }
 
     public void faint(int party, int slot) {
         m_visual.faint(party, slot);
     }
 
     public void setSpecies(int party, int slot, String species) {
         if (m_n == 2) {
             m_healthBars[party][slot].setToolTipText(species);
         }
         m_visual.setSpecies(party, slot, species);
     }
 
     public void sendOut(int party, int slot, int index, int speciesId, 
             String species, String name, int gender, int level) {
         m_visual.sendOut(party, slot, index, speciesId, species, name,
                 gender, level);
 
         if (party == m_participant && m_pokemon != null) {
             Collections.sort(m_sortedPokemon, new Comparator<Integer>() {
                 public int compare(Integer lhs, Integer rhs) {
                     VisualPokemon first = getPokemon(m_participant, lhs);
                     VisualPokemon second = getPokemon(m_participant, rhs);
                     int lhsVal = (first.getSlot() == -1) ? m_length + lhs :
                             first.getSlot();
                     int rhsVal = (second.getSlot() == -1) ? m_length + rhs :
                             second.getSlot();
                     return lhsVal - rhsVal;
                 }
             });
             panelSwitch.repaint();
         }
 
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
 
     public void updateStatus(int party, int position, int radius,
             String statusId, String msg, boolean applied) {
         m_visual.updateStatus(party, position, radius, statusId, msg, applied);
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
 
     public void synchroniseClock(int i, int time, int periods) {
         TimerLabel timer = (TimerLabel)((i == m_participant) ? lblClock0 : lblClock1);
         timer.update(time, periods);
         setTicking(true);
     }
 
     public void informTurnStart(int turn) {
         addMessage(null, "<b>===============</b>", false);
         String message = Text.getText(4, 16, new String[] { String.valueOf(turn) });
         addMessage(null, "<b>" + message + "</b>", false);
     }
 
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
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
         panelGame = new javax.swing.JPanel();
         lblPlayer1 = new javax.swing.JLabel();
         lblClock1 = new TimerLabel();
         lblPlayer0 = new javax.swing.JLabel();
         lblClock0 = new TimerLabel();
         panelHealth1 = new javax.swing.JPanel();
         panelVisual = new javax.swing.JPanel();
         panelHealth0 = new javax.swing.JPanel();
         jMenuBar1 = new javax.swing.JMenuBar();
         jMenu1 = new javax.swing.JMenu();
         mnuLeaveBattle = new javax.swing.JMenuItem();
 
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
 
         panelGame.setPreferredSize(new java.awt.Dimension(256, 247));
         panelGame.setLayout(new java.awt.GridBagLayout());
 
         lblPlayer1.setText("Player1");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         panelGame.add(lblPlayer1, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         panelGame.add(lblClock1, gridBagConstraints);
 
         lblPlayer0.setText("Player0");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         panelGame.add(lblPlayer0, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         panelGame.add(lblClock0, gridBagConstraints);
 
         panelHealth1.setPreferredSize(new java.awt.Dimension(256, 30));
         panelHealth1.setLayout(new java.awt.GridLayout(1, 0, 3, 0));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
         panelGame.add(panelHealth1, gridBagConstraints);
 
         panelVisual.setPreferredSize(new java.awt.Dimension(256, 144));
 
         org.jdesktop.layout.GroupLayout panelVisualLayout = new org.jdesktop.layout.GroupLayout(panelVisual);
         panelVisual.setLayout(panelVisualLayout);
         panelVisualLayout.setHorizontalGroup(
             panelVisualLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 256, Short.MAX_VALUE)
         );
         panelVisualLayout.setVerticalGroup(
             panelVisualLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 144, Short.MAX_VALUE)
         );
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = 2;
         panelGame.add(panelVisual, gridBagConstraints);
 
         panelHealth0.setPreferredSize(new java.awt.Dimension(256, 30));
         panelHealth0.setLayout(new java.awt.GridLayout(1, 0, 3, 0));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
         panelGame.add(panelHealth0, gridBagConstraints);
 
         jMenu1.setText("File");
 
         mnuLeaveBattle.setText("Leave Battle");
         mnuLeaveBattle.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuLeaveBattleActionPerformed(evt);
             }
         });
         jMenu1.add(mnuLeaveBattle);
 
         jMenuBar1.add(jMenu1);
 
         setJMenuBar(jMenuBar1);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                     .add(layout.createSequentialGroup()
                         .add(panelGame, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jScrollPane1, 0, 0, Short.MAX_VALUE)
                         .add(7, 7, 7))
                     .add(tabAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 363, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(txtChat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                     .add(scrollChat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .add(20, 20, 20)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                         .add(scrollChat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(txtChat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(layout.createSequentialGroup()
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(panelGame, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
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
         String separator = System.getProperty("line.separator");
         if (separator == null) separator = "\n";
         m_log.append(separator);
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
         if (!m_finished && m_pokemon != null && m_link.isAlive()) {
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
             sendAction(Action.MOVE, -1, -1);
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
                 selected = m_switches[i].getRealIndex();
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
 
     private void mnuLeaveBattleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuLeaveBattleActionPerformed
         formWindowClosing(null);
     }//GEN-LAST:event_mnuLeaveBattleActionPerformed
 
     /**
     * @param args the command line arguments
     */
     public static void main(String args[]) {
         JFrame frame = new JFrame();
         JPanel panel = new JPanel();
         TimerLabel label = new TimerLabel();
         label.setMaxPeriods(3);
         label.update(300, 3);
         panel.add(label);
         frame.add(panel);
         frame.setSize(150, 50);
         frame.setVisible(true);
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnMove;
     private javax.swing.JButton btnMoveCancel;
     private javax.swing.JButton btnSwitch;
     private javax.swing.JButton btnSwitchCancel;
     private javax.swing.JMenu jMenu1;
     private javax.swing.JMenuBar jMenuBar1;
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
     private javax.swing.JMenuItem mnuLeaveBattle;
     private javax.swing.JPanel panelGame;
     private javax.swing.JPanel panelHealth0;
     private javax.swing.JPanel panelHealth1;
     private javax.swing.JPanel panelMoves;
     private javax.swing.JPanel panelSwitch;
     private javax.swing.JPanel panelVisual;
     private javax.swing.JScrollPane scrollChat;
     private javax.swing.JTabbedPane tabAction;
     private javax.swing.JTextField txtChat;
     // End of variables declaration//GEN-END:variables
 
 }
