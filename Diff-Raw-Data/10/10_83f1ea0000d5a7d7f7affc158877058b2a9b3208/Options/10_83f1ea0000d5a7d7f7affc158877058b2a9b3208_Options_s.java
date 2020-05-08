 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package leocarbon.cu;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.EtchedBorder;
 import javax.swing.event.ChangeEvent;
 import static leocarbon.cu.ColorUtility.*;
 import org.apache.log4j.Logger;
 
 public class Options extends JFrame implements ActionListener {
     JCheckBox texttoggle, RGBtoggle, Hextoggle, aHextoggle;
     private class Visibility extends JPanel { private Visibility() {
         Border controlborder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), RB.getString("Options.v.title"));
         setBorder(controlborder);
         
         setLayout(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
         
         JPanel evpanel = new JPanel(new GridBagLayout()); {
             c.fill = GridBagConstraints.NONE;
             evpanel.add(CU.evtoggle,c);
 
             texttoggle = new JCheckBox("Show Color Values", true);
             texttoggle.addActionListener(Options.this);
             texttoggle.setActionCommand("Ev");
             c.gridy = 1;
             evpanel.add(texttoggle,c);
 
             JPanel Evtoggle = new JPanel(new GridBagLayout()); {
                 RGBtoggle = new JCheckBox("RGB", true);
                 RGBtoggle.setActionCommand(RB.getString("Options.v.ev.rgb"));
                 RGBtoggle.addActionListener(Options.this);
                 c.gridx = 0; c.gridy = 0;
                 Evtoggle.add(RGBtoggle,c);
                 Hextoggle = new JCheckBox(RB.getString("Options.v.ev.hex"), true);
                 Hextoggle.setActionCommand("Evhex");
                 Hextoggle.addActionListener(Options.this);
                 c.gridx = 0; c.gridy = 1;
                 Evtoggle.add(Hextoggle,c);
                 aHextoggle = new JCheckBox(RB.getString("Options.v.ev.ahex"), true);
                 aHextoggle.setActionCommand("Evahex");
                 aHextoggle.addActionListener(Options.this);
                 c.gridx = 0; c.gridy = 2;
                 Evtoggle.add(aHextoggle,c);
             }
             c.gridy = 2;
             evpanel.add(Evtoggle,c);
         }
         evpanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK), new EmptyBorder(4, 4, 2, 4)));
         c.gridx = 0; c.gridy = 0;
         c.fill = GridBagConstraints.BOTH;
         add(evpanel,c);
 
         JPanel cmpanel = new JPanel();
         cmpanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK), new EmptyBorder(2, 4, 2, 4)));
         cmpanel.add(CU.cmtoggle);
         c.gridx = 0; c.gridy = 1;
         add(cmpanel,c);
         
         JPanel ccmpanel = new JPanel();
         ccmpanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 4, 4));
         ccmpanel.add(CU.ccmtoggle);
         c.gridx = 0; c.gridy = 2;
         add(ccmpanel,c);
     }}
     JCheckBox logging;
     private class Logging extends JPanel { public Logging() {
         setLayout(new GridLayout(1,1));
         logging = new JCheckBox(RB.getString("Options.l.logging"),false);
         logging.setActionCommand("logging");
         logging.addActionListener(Options.this);
         add(logging);
         setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), RB.getString("Options.l.logging")));
     }}
     private class Background extends JPanel {
         /* Funciton by 843806(https://community.oracle.com/people/843806?customTheme=otn): https://community.oracle.com/thread/1354255?start=0&tstart=0
          * I modified it a bit.
          */ private void changeBackGround(Container iC, Color Background) {
             iC.setBackground(Background);
             for(Component C : iC.getComponents()){
                 if(C instanceof Container
                 &&!(C instanceof javax.swing.JTextField)
                 &&!(C instanceof javax.swing.JSpinner)) changeBackGround((Container)C, Background);
             }
         } public Background() {
             setLayout(new GridBagLayout());
 
             GridBagConstraints c = new GridBagConstraints();
             //c.fill = GridBagConstraints.BOTH;
 
             final JLabel Displaybrightness = new JLabel("Current brightness: "+UIManager.getColor("Panel.background").getRed());
             c.gridx = 0;
             c.gridy = 2;
             c.gridwidth = 1;
             c.gridheight = 1;
             add(Displaybrightness,c);
             final JSlider brightness = new JSlider(91,255,UIManager.getColor("Panel.background").getRed());
             brightness.setMajorTickSpacing(41);
             brightness.setPaintTicks(true);
             brightness.setPaintLabels(true);
             brightness.addChangeListener((ChangeEvent CE) -> {
                 Displaybrightness.setText("Current brightness: " + brightness.getValue());
                 Color Background1 = new Color(brightness.getValue(),brightness.getValue(),brightness.getValue());
                 CU.getContentPane().setBackground(Background1);
                 changeBackGround(cc.getParent(), Background1);
                 changeBackGround(ccc.getParent(), Background1);
                 if(!brightness.getValueIsAdjusting() && dologging) Logger.getLogger(Options.class).trace("Changed brightness to: " + brightness.getValue());
             });
             c.gridx = 0;
             c.gridy = 1;
             c.gridwidth = 2;
             c.gridheight = 1;
             add(brightness,c);
 
             JButton brightnessReset = new JButton("Reset background color");
             brightnessReset.addActionListener((ActionEvent AE) -> {
                 brightness.setValue(UIManager.getColor("Panel.background").getRed());
             });
             c.gridx = 1;
             c.gridy = 2;
             c.gridwidth = 1;
             c.gridheight = 1;
             add(brightnessReset,c);
 
             c.gridx = 0;
             c.gridy = 0;
             c.gridwidth = 2;
             c.gridheight = 1;
             //add(new JLabel("<html><font color = 'red'><b>Note: </b></font><i>This function might not work for some Look and Feels.</i></html>"),c);
             Border brightnessBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Background");
             setBorder(brightnessBorder);
         }
     }
     private static int currentLAF = 0;
     private static class LookAndFeel extends JPanel { public LookAndFeel() {
         setLayout(new GridLayout(1,1));
 
         Border plafBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), RB.getString("Options.laf.title"));
         setBorder(plafBorder);
 
         final UIManager.LookAndFeelInfo[] ILAFs = UIManager.getInstalledLookAndFeels();
         String[] ILAFNames = new String[ILAFs.length];
         
         for(int a = 0; a < ILAFs.length; ++a){
             ILAFNames[a] = ILAFs[a].getName();
             if(ILAFs[a].getName().equals(UIManager.getLookAndFeel().getName())) currentLAF = a;
         }
         final JComboBox LAFJC = new JComboBox(ILAFNames);
         LAFJC.setSelectedIndex(currentLAF);
         add(LAFJC);
         
         LAFJC.addActionListener((ActionEvent AE) -> {
             int index = LAFJC.getSelectedIndex();
             try {
                 UIManager.setLookAndFeel(ILAFs[index].getClassName());
                 SwingUtilities.updateComponentTreeUI(CU);
                 
                 boolean Aboutwasopened = (A != null);
                 Color wasColor = cc.getColor();
                 CU.setVisible(false);
                 setVisible(false);
                 if(Aboutwasopened) A.setVisible(false);
                 CU.dispose();
                 O.dispose();
                 if(Aboutwasopened) A.dispose();
                 CU = null;
                 O = null;
                 if(Aboutwasopened) A = null;
                 System.gc();
                 
                 OptionsLAFChange = true;
                 CU = new ColorUtility();
                 CU.pack();
                 cc.setColor(wasColor);
                 
                 EvD = new Dimension(Ev.getSize());
                 EvP = new Point(Ev.getLocationOnScreen());
                 CUD = new Dimension(CU.getSize());
                 
                 O = new Options();
                 O.pack();
                 
                 if(Aboutwasopened) A = new About();
                 
                 if(UIManager.getLookAndFeel().getClass().getName().equals(ILAFs[index].getClassName()) && dologging) Logger.getLogger(ColorUtility.class.getName()).info("Changed Look and Feel to: " + UIManager.getLookAndFeel());
             } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException E){
                 if(dologging){
                     Logger.getLogger(ColorUtility.class.getName()).error(E);
                     Logger.getLogger(ColorUtility.class.getName()).trace("Could not set Look and Feel to "+ILAFs[index].getClassName());
                 }
             }
         });
     }}
     
     
     public static boolean isEasyViewTextVisible = true, RGBv = true, Hexv = true, aHexv = true;
     @Override
     public void actionPerformed(ActionEvent AE) {
         switch(AE.getActionCommand()){
             case "Ev":
                 if(texttoggle.isSelected()){
                     isEasyViewTextVisible = true;
                     Ev.update(cc.getColor());
                     RGBtoggle.setEnabled(true);
                     Hextoggle.setEnabled(true);
                     aHextoggle.setEnabled(true);
                 } else {
                     isEasyViewTextVisible = false;
                     Ev.update(cc.getColor());
                     RGBtoggle.setEnabled(false);
                     Hextoggle.setEnabled(false);
                     aHextoggle.setEnabled(false);
                 }
                 break;
             case "EvRGB":
                 if(RGBtoggle.isSelected()){
                     RGBv = true;
                     Ev.update(cc.getColor());
                 } else {
                     RGBv = false;
                     Ev.update(cc.getColor());
                 }
                 break;
             case "Evhex":
                 if(Hextoggle.isSelected()){
                     Hexv = true;
                     Ev.update(cc.getColor());
                 } else {
                     Hexv = false;
                     Ev.update(cc.getColor());
                 }
                 break;
             case "Evahex":
                 if(aHextoggle.isSelected()){
                     aHexv = true;
                     Ev.update(cc.getColor());
                 } else {
                     aHexv = false;
                     Ev.update(cc.getColor());
                 }
                 break;
             case "logging":
                 dologging = logging.isSelected();
         }
     }
     
     JPanel Background;
     public Options() {
         setTitle(RB.getString("Options.Title"));
         Background = new JPanel(new GridBagLayout());
         
         GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.BOTH;
         
         c.gridx = 0;
         c.gridy = 0;
         Background.add(new Visibility(),c);
         
         c.gridy = 1;
         Background.add(new LookAndFeel(),c);
         
         c.gridy = 2;
         Background.add(new Logging(),c);
 
         Background.setBorder(new EmptyBorder(4,4,4,4));
         
         add(Background);
         
         setAlwaysOnTop(true);
         setSize(cc.getWidth(),cc.getHeight()+ccc.getHeight());
         pack();
         setLocation(EvP.x, EvP.y+EvD.height);
         setVisible(true);
         setResizable(false);
     }
 }
