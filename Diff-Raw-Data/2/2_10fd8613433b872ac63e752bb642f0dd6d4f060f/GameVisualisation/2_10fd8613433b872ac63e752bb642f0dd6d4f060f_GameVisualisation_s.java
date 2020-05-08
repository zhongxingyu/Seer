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
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.image.BufferedImage;
 import java.awt.image.IndexColorModel;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JToolTip;
 import javax.swing.ToolTipManager;
 import shoddybattleclient.shoddybattle.Pokemon;
 
 /**
  *
  * @author ben
  */
 public class GameVisualisation extends JPanel {
 
     public static class VisualPokemon {
         private String m_species;
         private int m_gender;
         private boolean m_shiny;
         private List<String> m_statuses = new ArrayList<String>();
         private int m_healthN = 100;
         private int m_healthD = 100;
         private int[] m_statLevels = new int[6];
         private double[] m_statMultipliers = new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
         private boolean m_visible = true;
 
         public VisualPokemon(String species, int gender, boolean shiny) {
             m_species = species;
             m_gender = gender;
             m_shiny = shiny;
         }
         public String getSpecies() {
             return m_species;
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
         public void setStatMultiplier(int i, double mult) {
             m_statMultipliers[i] = mult;
         }
         public double getStatMultiplier(int i) {
             return m_statMultipliers[i];
         }
     }
 
     private static final Image m_background;
     private static final Image[] m_pokeball = new Image[3];
     private static final Image[] m_arrows = new Image[2];
     private VisualPokemon[][] m_parties;
     private int m_view;
     private int m_selected = -1;
     private int m_target = Integer.MAX_VALUE;
     private Graphics2D m_mouseInput;
     private static final IndexColorModel m_colours;
     private int m_n;
 
     private int m_tooltipParty = Integer.MAX_VALUE;
     private int m_tooltipPoke = Integer.MAX_VALUE;
 
     public static Image getImageFromResource(String file) {
         return Toolkit.getDefaultToolkit()
                 .createImage(GameVisualisation.class.getResource("resources/" + file));
     }
 
     static {
         m_background = getImageFromResource("background.jpg");
         m_pokeball[0] = getImageFromResource("pokeball.png");
         m_pokeball[1] = getImageFromResource("pokeball2.png");
         m_pokeball[2] = getImageFromResource("pokeball3.png");
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
     
     public GameVisualisation(int view, int n) {
         m_view = view;
         m_parties = new VisualPokemon[2][n];
         m_n = n;
         setBorder(BorderFactory.createLineBorder(Color.BLACK));
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
         m_parties[party][slot].setStatLevel(stat, level);
     }
 
     public void updateStatMultiplier(int party, int slot, int stat, double mult) {
         m_parties[party][slot].setStatMultiplier(stat, mult);
     }
 
     public void setSpriteVisible(int party, int slot, boolean visible) {
         m_parties[party][slot].m_visible = visible;
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
 
     public void setParties(VisualPokemon[] party1, VisualPokemon[] party2) {
         for (int i = 0; i < 2; i++) {
             VisualPokemon[] party = (i == 0) ? party1 : party2;
             for (int j = 0; j < party.length; j++) {
                 m_parties[i][j] = party[j];
             }
         }
     }
 
     public void updateHealth(int party, int slot, int num, int denom) {
         m_parties[party][slot].setHealth(num, denom);
     }
 
     public VisualPokemon getPokemon(int party, int order) {
         return m_parties[party][order];
     }
 
     public void setPokemon(int party, int order, VisualPokemon p) {
         m_parties[party][order] = p;
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
 
     public String[] getPokemonNames() {
         String[] ret = new String[m_n * 2];
         int len = m_n;
         for (int i = 0; i < m_parties.length; i++) {
             for (int j = 0; j < len; j++) {
                 VisualPokemon p = m_parties[i][j];
                 ret[i * len + j] = (p == null) ? null : p.getSpecies();
             }
         }
         return ret;
     }
 
     public String[] getAllyNames() {
         VisualPokemon[] party = m_parties[m_view];
         String[] ret = new String[party.length];
         for (int i = 0; i < party.length; i++) {
             VisualPokemon p = party[i];
             ret[i] = (p == null) ? null : p.getSpecies();
         }
         return ret;
     }
 
     @Override
     public JToolTip createToolTip() {
         VisualPokemon p = m_parties[m_tooltipParty][m_tooltipPoke];
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
                 double mult = p.getStatMultiplier(i);
                 if (mult != 1.0) {
                     stats.append(" (");
                     stats.append(mult);
                     stats.append("x)");
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
         if (m_n <= 2) {
             num = denom = -1;
         } else {
             num = p.getNumerator();
             denom = p.getDenominator();
         }
         VisualToolTip vt = new VisualToolTip(p.getSpecies(), stats.toString(),
                 effects.toString(), num, denom);
         return new JCustomTooltip(this, vt);
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         Graphics2D g2 = (Graphics2D)g.create();
         m_mouseInput.setColor(Color.WHITE);
         m_mouseInput.fillRect(0, 0, getWidth(), getHeight());
         g2.drawImage(m_background, 0, 0, this);
         paintParty(1 - m_view, g2);
         paintParty(m_view, g2);
         g2.dispose();
     }
 
     private void paintParty(int idx, Graphics g) {
         Graphics2D g2 = (Graphics2D)g.create();
         VisualPokemon[] team = m_parties[idx];
         if (team == null) return;
         boolean us = (idx == m_view);
         final int partyBuf = 12;
         MediaTracker tracker = new MediaTracker(this);
         for (int i = m_n - 1; i >= 0; i--) {
             VisualPokemon p = team[i];
             if (p == null) continue;
             Image img = null;
             try {
                img = getSprite(p.getSpecies(), !us, p.getGender() == Pokemon.Gender.GENDER_MALE.getValue(), p.isShiny(), null);
             } catch (IOException e) {
                 String gender = p.getGender() == 0 ? "Male" : "Female";
                 System.out.println(p.getSpecies() + " " + gender + " sprite not found");
             }
             if (img == null) continue;
             tracker.addImage(img, 0);
             try {
                 tracker.waitForAll();
             } catch (InterruptedException e) {
 
             }
             int h = img.getHeight(this);
             int w = img.getWidth(this);
             int x;
             int y = us ? m_background.getHeight(this) - h : 90 - h;
             if (m_n == 1) {
                 x = us ? 30 : 190 - w / 2;
             } else if (m_n == 2) {
                 x = us ? 70 : 210 - w / 2;
                 x -= us ? 70 * i : 50 * i;
             } else {
                 //get ugly
                 x = us ? 45 * (m_n - (i + 1)) - 15 : 220 - 45 * (m_n - (i + 1));
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
             m_mouseInput.setColor(new Color(idx, i, 0));
             m_mouseInput.fillRect(x, y, w, h);
         }
         g2.dispose();
     }
 
     public static Image getSprite(String name, boolean front, boolean male, 
             boolean shiny, String repository) throws IOException {
         String shininess = shiny ? "shiny" : "normal";
         String prefix = front ? "front" : "back";
         String gender = male ? "m" : "f";
         String path = prefix + shininess + "/" + gender + name.replaceAll("[ '\\.]", "").toLowerCase() + ".png";
         //TODO: change storage location
         String qualified = "/Users/ben/sprites/" + path;
         File f = new File(qualified);
         String[] repositories = new String[] {"http://shoddybattle.com/dpsprites/", repository};
         if (!f.exists()) {
             for (int i = 0; i < repositories.length; i++) {
                 URL url = new URL(repositories[i] + path);
                 InputStream input;
                 try {
                     input = url.openStream();
                 } catch (IOException e) {
                     continue;
                 }
                 f.getParentFile().mkdirs();
                 FileOutputStream output = new FileOutputStream(f);
                 byte[] bytes = new byte[255];
                 while (true) {
                     int read = input.read(bytes);
                     if (read == -1)
                         break;
                     output.write(bytes, 0, read);
                 }
                 output.flush();
                 output.close();
                 input.close();
                 break;
             }
         }
         return Toolkit.getDefaultToolkit().createImage(qualified);
     }
 
     public static void main(String[] args) {
         JFrame frame = new JFrame("Visualisation test");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         final GameVisualisation vis = new GameVisualisation(0, 2);
         VisualPokemon[] party1 = new VisualPokemon[] {
             new VisualPokemon("Squirtle", 0, false),
             new VisualPokemon("Wartortle", 1, true)
         };
         VisualPokemon[] party2 = new VisualPokemon[] {
             new VisualPokemon("Blissey", 1, false),
             new VisualPokemon("Chansey", 1, true)
         };
         vis.setSelected(0);
         vis.setTarget(-2);
         vis.setParties(party1, party2);
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
                         if (++i > 3) i = -3;
                     }
                 }
             }
 
         });//.start();
     }
 }
