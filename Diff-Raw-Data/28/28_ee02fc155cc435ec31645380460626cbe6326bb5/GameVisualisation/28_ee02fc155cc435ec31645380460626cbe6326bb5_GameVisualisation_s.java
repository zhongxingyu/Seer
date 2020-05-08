 /*
  * GameVisualisation.java
  *
  * Created on Apr 10, 2009, 2:13:23 PM
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
 
 import shoddybattleclient.utils.*;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.image.BufferedImage;
 import java.awt.image.IndexColorModel;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JToolTip;
 import javax.swing.Timer;
 import javax.swing.ToolTipManager;
 import shoddybattleclient.shoddybattle.Pokemon;
 import shoddybattleclient.shoddybattle.PokemonSpecies;
 
 /**
  *
  * @author ben
  */
 public class GameVisualisation extends JPanel {
 
     public static class VisualPokemon {
         private String m_species;
         private String m_name;
         private int m_gender;
         private boolean m_shiny;
         private List<String> m_statuses = new ArrayList<String>();
         private int m_healthN = 48;
         private int m_healthD = 48;
         private int[] m_statLevels = new int[6];
         private boolean m_visible = true;
         private int m_slot = -1;
         private boolean m_fainted = false;
         private Pokemon m_pokemon = null;
         private int m_frame = 1;
 
         public VisualPokemon(String species, int gender, boolean shiny) {
             m_species = species;
             m_gender = gender;
             m_shiny = shiny;
         }
         public VisualPokemon() {
             m_species = "???";
             m_gender = 0;
             m_shiny = false;
         }
         public String getSpecies() {
             if (m_pokemon != null) {
                 return m_pokemon.species;
             }
             return m_species;
         }
         public void setName(String name) {
             m_name = name;
         }
         public String getName() {
             return m_name;
         }
         public void setSpecies(String name) {
             m_species = name;
         }
         public int getGender() {
             return m_gender;
         }
         public boolean isShiny() {
             return m_shiny;
         }
         public void addStatus(String status) {
             m_statuses.add(status);
         }
         public void removeStatus(String status) {
             m_statuses.remove(status);
         }
         public List<String> getStatuses() {
             return m_statuses;
         }
         public void setHealth(int num, int denom) {
             m_healthN = num;
             m_healthD = denom;
         }
         public int getNumerator() {
             return m_healthN;
         }
         public int getDenominator() {
             return m_healthD;
         }
         public void setStatLevel(int i, int level) {
             m_statLevels[i] = level;
         }
         public int getStatLevel(int idx) {
             return m_statLevels[idx];
         }
         public void setSlot(int slot) {
             m_slot = slot;
         }
         public int getSlot() {
             return m_slot;
         }
         public void faint() {
             m_fainted = true;
         }
         public boolean isFainted() {
             return m_fainted;
         }
         public int getState() {
             if (m_fainted) {
                 return STATE_FAINTED;
             } else if (m_statuses.size() > 0) {
                 return STATE_STATUSED;
             } else {
                 return STATE_NORMAL;
             }
         }
         public void setPokemon(Pokemon p) {
             m_pokemon = p;
         }
         public void toggleFrame() {
             m_frame = (m_frame == 1) ? 2 : 1;
         }
         public int getFrame() {
             return m_frame;
         }
     }
 
     public static final int STATE_NORMAL = 0;
     public static final int STATE_STATUSED = 1;
     public static final int STATE_FAINTED = 2;
 
     private static final int BACKGROUND_COUNT = 23;
 
     private Image m_background;
     private static final Image[] m_pokeball = new Image[3];
     private static final Image[] m_arrows = new Image[2];
     private VisualPokemon[][] m_active;
     private VisualPokemon[][] m_parties;
     private int m_view;
     private int m_selected = -1;
     private int m_target = Integer.MAX_VALUE;
     private Graphics2D m_mouseInput;
     private static final IndexColorModel m_colours;
     private final List<PokemonSpecies> m_speciesList;
     private int m_n;
     //max team length
     private int m_length;
 
     private int m_tooltipParty = Integer.MAX_VALUE;
     private int m_tooltipPoke = Integer.MAX_VALUE;
 
     public static Image getImageFromResource(String file) {
         return Toolkit.getDefaultToolkit()
                 .createImage(GameVisualisation.class.getResource("resources/" + file));
     }
 
     static {
         m_pokeball[STATE_NORMAL] = getImageFromResource("pokeball.png");
         m_pokeball[STATE_STATUSED] = getImageFromResource("pokeball2.png");
         m_pokeball[STATE_FAINTED] = getImageFromResource("pokeball3.png");
         m_arrows[0] = getImageFromResource("arrow_green.png");
         m_arrows[1] = getImageFromResource("arrow_red.png");
         
         byte[] r = new byte[13];
         byte[] g = new byte[13];
         byte[] b = new byte[13];
         for (byte i = 0; i < 2; ++i) {
             for (byte j = 0; j < 6; ++j) {
                 int k = i * 6 + j;
                 r[k] = i;
                 g[k] = j;
             }
         }
         r[12] = g[12] = b[12] = (byte)255;
         m_colours = new IndexColorModel(4, r.length, r, g, b);
 
         ToolTipManager manager = ToolTipManager.sharedInstance();
         manager.setInitialDelay(0);
         manager.setReshowDelay(0);
     }
     
     public GameVisualisation(int view, int n, int length, List<PokemonSpecies> speciesList) {
         m_view = view;
         m_active = new VisualPokemon[2][n];
         m_parties = new VisualPokemon[2][length];
         m_speciesList = speciesList;
         for (int i = 0; i < m_parties.length; i++) {
             for (int j = 0; j < m_parties[i].length; j++) {
                 m_parties[i][j] = new VisualPokemon();
             }
         }
         m_n = n;
         m_length = length;
         setBorder(BorderFactory.createLineBorder(Color.BLACK));
         int background = new Random().nextInt(BACKGROUND_COUNT) + 1;
         m_background = getImageFromResource("backgrounds/background" + background + ".png");
         MediaTracker tracker = new MediaTracker(this);
         tracker.addImage(m_background, 0);
         tracker.addImage(m_pokeball[0], 1);
         tracker.addImage(m_pokeball[1], 2);
         tracker.addImage(m_pokeball[2], 3);
         tracker.addImage(m_arrows[0], 4);
         tracker.addImage(m_arrows[1], 5);
         try {
             tracker.waitForAll();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
 
         Dimension d = getPreferredSize();
         final BufferedImage image = new BufferedImage((int)d.getWidth(),
                 (int)d.getHeight(),
                 BufferedImage.TYPE_BYTE_BINARY,
                 m_colours);
         m_mouseInput = image.createGraphics();
 
         addMouseMotionListener(new MouseMotionAdapter() {
             @Override
             public void mouseMoved(MouseEvent e) {
                 int x = e.getX();
                 int y = e.getY();
                 if ((x > image.getWidth()) || (y > image.getHeight())) return;
                 Color c = new Color(image.getRGB(x, y));
                 if (c.equals(Color.WHITE)) {
                     setToolTipText(null);
                 } else if (c.getBlue() == 1) {
                     displayInformation(c.getRed(), -1);
                 } else if (c.getBlue() == 2) {
                     displayInformation(-1, -1);
                 } else {
                     displayInformation(c.getRed(), c.getGreen());
                 }
             }
         });
     }
 
     public void updateStatLevel(int party, int slot, int stat, int level) {
         getPokemonForSlot(party, slot).setStatLevel(stat, level);
     }
 
     public void setSpriteVisible(int party, int slot, boolean visible) {
         m_active[party][slot].m_visible = visible;
     }
 
     public void updateHealth(int party, int slot, int total, int denominator) {
         getPokemonForSlot(party, slot).setHealth(total, denominator);
     }
 
     public VisualPokemon getPokemon(int party, int index) {
         return m_parties[party][index];
     }
 
     void faint(int party, int slot) {
         getPokemonForSlot(party, slot).faint();
         repaint();
     }
 
     private void displayInformation(int party, int idx) {
         m_tooltipParty = party;
         m_tooltipPoke = idx;
         String text = party + " " + idx;
         boolean reshow = ((text != null) && !text.equals(getToolTipText()));
         setToolTipText(text);
         if (reshow) {
             ToolTipManager manager = ToolTipManager.sharedInstance();
             manager.setEnabled(false);
             manager.setEnabled(true);
         }
 
     }
 
     public void setActive(VisualPokemon[] party1, VisualPokemon[] party2) {
         for (int i = 0; i < 2; i++) {
             VisualPokemon[] party = (i == 0) ? party1 : party2;
             for (int j = 0; j < party.length; j++) {
                 m_active[i][j] = party[j];
             }
         }
         repaint();
     }
 
     public VisualPokemon getPokemonForSlot(int party, int slot) {
         for (int i = 0; i < m_parties[party].length; i++) {
             if (m_parties[party][i].getSlot() == slot)
                 return m_parties[party][i];
         }
         return null;
     }
 
     public void setSpecies(int party, int slot, String species) {
         VisualPokemon p = getPokemonForSlot(party, slot);
         if (p != null) {
             p.setSpecies(species);
         }
     }
 
     public void sendOut(final int party, final int slot, int index, String name) {
         VisualPokemon p = getPokemonForSlot(party, slot);
         if (p != null) {
             p.setSlot(-1);
         }
         VisualPokemon newPoke = m_parties[party][index];
         newPoke.setSlot(slot);
         newPoke.setName(name);
         Timer t = new Timer(400, new ActionListener() {
             private boolean m_first = true;
             public void actionPerformed(ActionEvent e) {
                 VisualPokemon p = m_active[party][slot];
                 if (p == null) return;
                 Timer t = (Timer)e.getSource();
                 if (m_first) {
                     t.setDelay(300);
                     m_first = false;
                 } else {
                     t.stop();
                     t = null;
                 }
                 p.toggleFrame();
                 repaint();
             }
         });
         t.start();
     }
 
     public void setPokemon(int party, int index, Pokemon p) {
         m_parties[party][index].setPokemon(p);
     }
 
     @Override
     public Dimension getPreferredSize() {
         return new Dimension(m_background.getWidth(this), m_background.getHeight(this));
     }
 
 
     public void setSelected(int i) {
         m_selected = i;
         repaint();
     }
 
     public void setTarget(int i) {
         m_target = i;
         repaint();
     }
 
     @Override
     public JToolTip createToolTip() {
         VisualPokemon p = m_parties[m_tooltipParty][m_tooltipPoke];
         if (p == null) return new JToolTip();
         StringBuilder stats = new StringBuilder();
         stats.append("<html>");
         for (int i = 0; i < 6; i++) {
             //calc stat
             stats.append(Text.getText(2, i));
             if (i > 0) {
                 int level = p.getStatLevel(i);
                 if (level != 0) {
                     if (level > 0) stats.append("+");
                     stats.append(level);
                 }
             }
             stats.append("<br>");
         }
         stats.append("</html>");
         StringBuilder effects = new StringBuilder();
         effects.append("<html>");
         List<String> statuses = p.getStatuses();
         if (statuses.size() == 0) {
             effects.append("No effects");
         } else {
             for (String eff : statuses) {
                 effects.append("-");
                 effects.append(eff);
                 effects.append("<br>");
             }
         }
         effects.append("</html>");
         int num, denom;
         if (false /*m_n <= 2*/) {
             num = denom = -1;
         } else {
             num = p.getNumerator();
             denom = p.getDenominator();
         }
         // TODO: adjust final parameter for spectator support
         VisualToolTip vt = new VisualToolTip(p.getSpecies(), stats.toString(),
                 effects.toString(), num, denom, m_tooltipParty == m_view);
         return new JCustomTooltip(this, vt);
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         Graphics2D g2 = (Graphics2D)g.create();
         m_mouseInput.setColor(Color.WHITE);
         m_mouseInput.fillRect(0, 0, getWidth(), getHeight());
         g2.drawImage(m_background, 0, 0, this);
         for (int i = 0; i < 2; i++) {
             paintParty(i, g2);
             paintPokeballs(i, g2);
         }
         g2.dispose();
     }
 
     private void paintPokeballs(int idx, Graphics g) {
         Graphics2D g2 = (Graphics2D)g.create();
         boolean us = (idx == m_view);
         int w = m_pokeball[0].getWidth(this);
         int h = m_pokeball[0].getHeight(this);
         int baseX = us ? m_background.getWidth(this) - 3 * (w + 3) : 5;
         int baseY = us ? m_background.getHeight(this) - 2 * (h + 3) : 5;
         VisualPokemon[] party = m_parties[idx];
         for (int i = 0; i < party.length; i++) {
             if (i >= 6) break;
             int row = i / 3;
             Image pokeball = m_pokeball[party[i].getState()];
             int x = baseX + (i % 3) * (w + 3);
             int y = baseY + row * (h + 3);
             g2.drawImage(pokeball, x, y, this);
             m_mouseInput.setColor(new Color(idx, i, 0));
             m_mouseInput.fillRect(x, y, w, h);
         }
         g2.dispose();
     }
 
     private void paintParty(int idx, Graphics g) {
         Graphics2D g2 = (Graphics2D)g.create();
         VisualPokemon[] team = m_active[idx];
         if (team == null) return;
         boolean us = (idx == m_view);
         final int partyBuf = 12;
         MediaTracker tracker = new MediaTracker(this);
         for (int i = m_n - 1; i >= 0; i--) {
             VisualPokemon p = team[i];
             if (p == null) continue;
             Image img = getSprite(p.getSpecies(), !us,
                     p.getGender() != Pokemon.Gender.GENDER_FEMALE.getValue(), p.isShiny(), p.getFrame());
             if (img == null) continue;
             tracker.addImage(img, 0);
             try {
                 tracker.waitForAll();
             } catch (InterruptedException e) {
 
             }
             int h = img.getHeight(this);
             int w = img.getWidth(this);
             int x;
             int y = us ? m_background.getHeight(this) - h : 88 - h;
             if (m_n == 1) {
                 x = us ? 64 - (w / 2) : m_background.getWidth(this) - 64 - (w / 2);
             } else if (m_n == 2) {
                 x = us ? 70 : 220 - w / 2;
                 x -= us ? 70 * i : 50 * i;
             } else {
                 //get ugly
                 x = us ? 45 * (m_n - (i + 1)) - 15 : 215 - 45 * i;
             }
             int index = i + idx * m_n;
             if (us && (m_selected == i)) {
                 g2.drawImage(m_arrows[0], x + w / 2, y - m_arrows[0].getHeight(this), this);
             }
             if ((m_target == index) || ((m_target == -1) && !us) || ((m_target == -2) && us)
                     || (m_target == -3)) {
                 g2.drawImage(m_arrows[1], x + w / 2, y - m_arrows[1].getHeight(this), this);
             }
             if (p.m_visible) g2.drawImage(img, x, y, this);
             m_mouseInput.setColor(new Color(idx, 0, 1));
             m_mouseInput.fillRect(x - partyBuf, y - partyBuf, w + partyBuf * 2,
                     h + partyBuf * 2);
             int pos = -1;
             for (int j = 0; j < m_parties[idx].length; j++) {
                 if (m_parties[idx][j].getSlot() == i) {
                     pos = j;
                     break;
                 }
             }
             if (pos != -1) {
                 m_mouseInput.setColor(new Color(idx, pos, 0));
                 m_mouseInput.fillRect(x, y, w, h);
             }
         }
         g2.dispose();
     }
 
     private static String createPath(int number, boolean front, boolean male, boolean shiny,
             String repo, int frame) {
         StringBuilder builder = new StringBuilder(Preference.getSpriteLocation());
         builder.append(repo);
         builder.append("/");
         builder.append(front ? "front" : "back");
         builder.append("/");
         builder.append(shiny ? "shiny" : "normal");
         builder.append(frame == 1 ? "" : String.valueOf(frame));
         builder.append("/");
         builder.append(number);
         builder.append(male ? "" : "f");
         builder.append(".png");
         return builder.toString();
     }
 
     private static String getSpritePath(int number, boolean front, boolean male,
             boolean shiny, String repo, int frame) {
         //look for the correct sprite, then the opposite gender, then the first frames
         File f = new File(createPath(number, front, male, shiny, repo, frame));
         if (f.exists()) return f.toString();
         f = new File(createPath(number, front, !male, shiny, repo, frame));
         if (f.exists()) return f.toString();
         f = new File(createPath(number, front, male, shiny, repo, 1));
         if (f.exists()) return f.toString();
         f = new File(createPath(number, front, !male, shiny, repo, 1));
         if (f.exists()) return f.toString();
         return null;
     }
 
     public static Image getSprite(int number, boolean front, boolean male,
             boolean shiny, int frame) {
         String[] repositories = Preference.getSpriteDirectories();
         String qualified = null;
         for (int i = 0; i < repositories.length; i++) {
             qualified = getSpritePath(number, front, male, shiny, repositories[i], frame);
             if (qualified != null) break;
         }
 
         if (qualified == null) return null;
         return Toolkit.getDefaultToolkit().createImage(qualified);
     }
 
     public static Image getSprite(int number, boolean front, boolean male,
             boolean shiny) {
         return getSprite(number, front, male, shiny, 1);
     }
     public Image getSprite(String name, boolean front, boolean male,
             boolean shiny, int frame) {
         int number = PokemonSpecies.getIdFromName(m_speciesList, name);
         return getSprite(number, front, male, shiny, frame);
     }
 
     public Image getSprite(String name, boolean front, boolean male, boolean shiny) {
         return getSprite(name, front, male, shiny, 1);
     }
 
     public static void main(String[] args) {
         JFrame frame = new JFrame("Visualisation test");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         ArrayList<PokemonSpecies> species = new SpeciesListParser().parseDocument(
                 GameVisualisation.class.getResource("resources/species.xml").toString());
         final GameVisualisation vis = new GameVisualisation(0, 3, 6, species);
         VisualPokemon[] party1 = new VisualPokemon[] {
             new VisualPokemon("Squirtle", Pokemon.Gender.GENDER_MALE.getValue(), false),
             new VisualPokemon("Wartortle", Pokemon.Gender.GENDER_MALE.getValue(), false),
             new VisualPokemon("Blastoise", Pokemon.Gender.GENDER_MALE.getValue(), false)
         };
         VisualPokemon[] party2 = new VisualPokemon[] {
             new VisualPokemon("Blissey", Pokemon.Gender.GENDER_FEMALE.getValue(), false),
             new VisualPokemon("Togekiss", Pokemon.Gender.GENDER_FEMALE.getValue(), false),
             new VisualPokemon("Tyranitar", Pokemon.Gender.GENDER_FEMALE.getValue(), false)
         };
         vis.setSelected(0);
         vis.setTarget(-2);
         vis.setActive(party1, party2);
         Dimension d = vis.getPreferredSize();
         frame.setSize(d.width, d.height + 22);
         vis.setSize(d);
         vis.setLocation(0, 0);
         frame.add(vis);
 
         frame.setVisible(true);
         new Thread(new Runnable() {
 
             public void run() {
                 int i = -3;
                 while (true) {
                     synchronized(this) {
                         final int idx = i;
                         javax.swing.SwingUtilities.invokeLater(new Runnable(){
                             public void run() {
                                 vis.setTarget(idx);
                             }
                         });
                         try {
                             wait(700);
                         } catch (Exception e) {
 
                         }
                         if (++i > 5) i = -3;
                     }
                 }
             }
 
         }).start();
     }
 }
