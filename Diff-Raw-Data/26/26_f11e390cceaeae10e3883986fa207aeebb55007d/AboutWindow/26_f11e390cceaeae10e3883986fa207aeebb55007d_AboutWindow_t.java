 /*
  * This file is part of GenSim.
  *
  * GenSim is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * GenSim is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with GenSim.  If not, see <http://www.gnu.org/licenses/>.
  */
 package gensim;
 
 import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.Toolkit;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URISyntaxException;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 /**
  *
  * @author Andrew Vitkus
  */
 public class AboutWindow extends JFrame implements Runnable {
 
     public AboutWindow() {
         super("About GenSim");
     }
 
     @Override
     public void run() {
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         try {
             UIManager.setLookAndFeel(new WindowsLookAndFeel());
         } catch (UnsupportedLookAndFeelException ex) {
         }
         setSize(400, 400);
         buildWindow();
 
         setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/GenSim icon large.png")));
         setLocationByPlatform(true);
         setResizable(false);
         setVisible(true);
     }
 
     private void buildWindow() {
         JTabbedPane tabPane = new JTabbedPane();
 
         JPanel mainTab = new JPanel();
 
         JEditorPane aboutText = new JEditorPane();
         try {
             aboutText.setPage(getClass().getResource("/about.html"));
         } catch (IOException ex) {
         }
         aboutText.setEditable(false);
         aboutText.setBorder(null);
         aboutText.setBackground(null);
         aboutText.addHyperlinkListener(new HyperlinkListener() {
             @Override
             public void hyperlinkUpdate(HyperlinkEvent e) {
                 if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                     try {
                         Desktop.getDesktop().browse(e.getURL().toURI());
                     } catch (IOException | URISyntaxException ex) {
                     }
                 }
             }
         });
         /*
         JLabel versionLabel = new JLabel("GenSim v 1.2");
         JLabel codedLabel = new JLabel("Coded by: Andrew Vitkus");
         JLabel logoArtLabel = new JLabel("Logo art by: Andrew Vitkus");
         JLabel chickenArtLabel = new JLabel("Chicken art by: Fred Hurteau");
         JLabel updatedLabel = new JLabel("Last Updated: 7/2013");
         JLabel copyrightLabel = new JLabel("Copyright 2013");
         JLabel websiteLabel = new JLabel("Visit our site to check for updates");
         JLabel addressLabel = new JLabel("http://sourceforge.net/projects/gen-sim/");
 
         addComponent(mainTab, versionLabel, GridBagConstraints.WEST, 1, 0, 0);
         addComponent(mainTab, codedLabel, GridBagConstraints.WEST, 1, 0, 2);
         addComponent(mainTab, logoArtLabel, GridBagConstraints.WEST, 1, 0, 3);
         addComponent(mainTab, chickenArtLabel, GridBagConstraints.WEST, 1, 0, 4);
         addComponent(mainTab, updatedLabel, GridBagConstraints.WEST, 1, 0, 6);
         addComponent(mainTab, copyrightLabel, GridBagConstraints.WEST, 1, 0, 8);*/
 
         mainTab.add(aboutText);
         JTabbedPane licenseTabs = new JTabbedPane();
 
         JPanel gnugplv3Tab = new JPanel();
         gnugplv3Tab.add(new JLabel("Program licensed under the GNU General Public License version 3"), BorderLayout.NORTH);
 
 
         StringBuilder gplv3Text = new StringBuilder();
         try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResource("/gpl.txt").openStream(), "ISO-8859-1"))) {
             while (br.ready()) {
                 gplv3Text.append(br.readLine()).append("\n");
             }
         } catch (IOException ex) {
         }
 
         JTextArea gplv3 = new JTextArea(gplv3Text.toString());
         gplv3.setEditable(false);
         gplv3.setLineWrap(true);
         gplv3.setWrapStyleWord(true);
         gplv3.setFont(new Font("Serif", Font.PLAIN, 11));
 
         JScrollPane gplv3Scroll = new JScrollPane(gplv3);
         gplv3Scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
         gplv3Scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         gplv3Scroll.setPreferredSize(new Dimension(382, 292));
         gnugplv3Tab.add(gplv3Scroll, BorderLayout.CENTER);
 
         JPanel ccTab = new JPanel();
         ccTab.add(new JLabel("Images licensed under a Creative Commons"), BorderLayout.NORTH);
         ccTab.add(new JLabel("Attribution-NonCommercial-ShareAlike 3.0 Unported License"), BorderLayout.NORTH);
 
         StringBuilder ccText = new StringBuilder();
         try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResource("/cc-by-nc-sa.txt").openStream(), "ISO-8859-1"))) {
             while (br.ready()) {
                 ccText.append(br.readLine()).append("\n");
             }
         } catch (IOException ex) {
         }
 
         JTextArea cc = new JTextArea(ccText.toString());
 
         cc.setEditable(false);
         cc.setLineWrap(true);
         cc.setWrapStyleWord(true);
         cc.setFont(new Font("Serif", Font.PLAIN, 11));
 
         JScrollPane ccScroll = new JScrollPane(cc);
         ccScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
         ccScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         ccScroll.setPreferredSize(new Dimension(382, 273));
         ccTab.add(ccScroll, BorderLayout.CENTER);
 
         licenseTabs.addTab("Program", null, gnugplv3Tab, "Program license");
         licenseTabs.addTab("Art", null, ccTab, "Art license");
 
         tabPane.addTab("General", null, mainTab, "General information");
         tabPane.addTab("Licenses", null, licenseTabs, "View Licenses");
 
         add(tabPane);
     }
 
     private void addComponent(Container dest, Component component, int fill, int width, int x, int y) {
         GridBagConstraints c = new GridBagConstraints();
 
         c.fill = fill;
         c.weightx = 1;
         c.weighty = 1;
         c.gridwidth = width;
         c.gridx = x;
         c.gridy = y;
 
         dest.add(component, c);
     }
 }
