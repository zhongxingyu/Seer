 /*
  * Example.java
  *
  * Created on October 25, 2005, 2:45 PM
  *
  * To change this template, choose Tools | Options and locate the template under
  * the Source Creation and Management node. Right-click the template and choose
  * Open. You can then make changes to the template in the Source Editor.
  */
 package de.cismet.tools.gui.autocomplete;
 
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.TimeZone;
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListCellRenderer;
 
 /**
  *
  */
 public class Example {
 
     /** Creates a new instance of Example */
     public Example() {
         JFrame frame = new JFrame();
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.getContentPane().add(_createPanel());
         frame.setBounds(100, 100, 450, 350);
         frame.setVisible(true);
     }
 
     private JPanel _createPanel() {
         _tf = new CompleterTextField(TimeZone.getAvailableIDs(), false);
 
         final JCheckBox caseCheck = new JCheckBox(org.openide.util.NbBundle.getMessage(Example.class, "Example.caseCheck.text"));  //NOI18N
         caseCheck.setSelected(_tf.isCaseSensitive());
 
         final JCheckBox correctCheck = new JCheckBox(org.openide.util.NbBundle.getMessage(Example.class, "Example.correctCheck.text"));  //NOI18N
         correctCheck.setSelected(_tf.isCorrectingCase());
         correctCheck.setEnabled(!caseCheck.isSelected());
         correctCheck.setToolTipText(org.openide.util.NbBundle.getMessage(Example.class, "Example.correctCheck.tooltip"));  //NOI18N
 
         caseCheck.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent arg0) {
                 _tf.setCaseSensitive(caseCheck.isSelected());
                 correctCheck.setEnabled(!caseCheck.isSelected());
                 if (caseCheck.isSelected()) {
                     correctCheck.setSelected(false);
                     _tf.setCorrectCase(false);
                 }
             }
         });
 
         correctCheck.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent arg0) {
                 _tf.setCorrectCase(correctCheck.isSelected());
             }
         });
 
         JPanel panel = new JPanel();
         panel.setLayout(new FlowLayout());
         panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         panel.add(new JLabel(org.openide.util.NbBundle.getMessage(Example.class, "Example._createPanel().panel.JLabel_anon1.text")));  //NOI18N
         panel.add(_tf);
 
         panel.add(Box.createGlue());
         panel.add(caseCheck);
 
         panel.add(Box.createGlue());
         panel.add(correctCheck);
 
         panel.add(Box.createVerticalStrut(20));
         panel.add(Box.createVerticalStrut(20));
 
         _tfww = new CompleterTextField(TimeZone.getAvailableIDs(), true);
         final JCheckBox caseCheck2 = new JCheckBox(org.openide.util.NbBundle.getMessage(Example.class, "Example.caseCheck2.text"));  //NOI18N
         caseCheck2.setSelected(_tfww.isCaseSensitive());
 
         final JCheckBox correctCheck2 = new JCheckBox(org.openide.util.NbBundle.getMessage(Example.class, "Example.correctCheck2.text"));  //NOI18N
         correctCheck2.setSelected(_tfww.isCorrectingCase());
         correctCheck2.setEnabled(!caseCheck2.isSelected());
        correctCheck2.setToolTipText(org.openide.util.NbBundle.getMessage(Example.class, "Example.correctCheck2.tooltip"));  //NOI18N
 
         caseCheck2.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent arg0) {
                 _tfww.setCaseSensitive(caseCheck2.isSelected());
                 correctCheck2.setEnabled(!caseCheck2.isSelected());
                 if (caseCheck2.isSelected()) {
                     correctCheck2.setSelected(false);
                     _tfww.setCorrectCase(false);
                 }
             }
         });
 
         correctCheck2.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent arg0) {
                 _tfww.setCorrectCase(correctCheck2.isSelected());
             }
         });
 
         panel.add(new JLabel(org.openide.util.NbBundle.getMessage(Example.class, "Example._createPanel().panel.JLabel_anon2.text")));  //NOI18N
         panel.add(_tfww);
 
         panel.add(Box.createGlue());
         panel.add(caseCheck2);
 
         panel.add(Box.createGlue());
         panel.add(correctCheck2);
 
         panel.add(Box.createVerticalStrut(20));
         panel.add(Box.createVerticalStrut(20));
         final JButton tbn = new JButton(new AbstractAction() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 System.out.println(_combo.getSelectedItem() + " " + _combo.getSelectedIndex() + " " + _combo.getModel());   //NOI18N
             }
         });
         tbn.setText(org.openide.util.NbBundle.getMessage(Example.class,"Example._createPanel().tbn.text"));  //NOI18N
         panel.add(tbn);
 //    _combo = new CompleterComboBox(new String[]{""," ","aa","Aa","aA","AA","Spielplatz"});
         final DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
         _combo = new JComboBox(new TT[]{
             new TT(org.openide.util.NbBundle.getMessage(Example.class,"Example._createPanel().dlcr.TT_anon1.bdy")),  //NOI18N
             new TT(org.openide.util.NbBundle.getMessage(Example.class,"Example._createPanel().dlcr.TT_anon2.bdy")),  //NOI18N
             new TT(org.openide.util.NbBundle.getMessage(Example.class,"Example._createPanel().dlcr.TT_anon3.bdy")),  //NOI18N
             new TT(org.openide.util.NbBundle.getMessage(Example.class,"Example._createPanel().dlcr.TT_anon4.bdy")),  //NOI18N
             new TT(org.openide.util.NbBundle.getMessage(Example.class,"Example._createPanel().dlcr.TT_anon5.bdy")),  //NOI18N
             null});
         _combo.setRenderer(new ListCellRenderer() {
 
             @Override
             public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                 Component ret = dlcr.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                 if (value == null) {
                     ((JLabel) ret).setText("nix");  //NOI18N
                 }
                 return ret;
             }
         });
         final ComboCompleterFilter filter = ComboCompleterFilter.addCompletionMechanism(_combo);
 //    _combo = new CompleterComboBox(new String[]{"x"," ","aa","Aa","aA","AA","Spielplatz"});
         filter.setStrict(false);
         filter.setNullRespresentation("nix");       //NOI18N
 //    _combo = new CompleterComboBox(TimeZone.getAvailableIDs());
         final JCheckBox caseCheck3 = new JCheckBox(org.openide.util.NbBundle.getMessage(Example.class, "Example.caseCheck3.text"));  //NOI18N
         caseCheck3.setSelected(filter.isCaseSensitive());
 
         final JCheckBox correctCheck3 = new JCheckBox(org.openide.util.NbBundle.getMessage(Example.class, "Example.correctCheck3.text"));  //NOI18N
         correctCheck3.setSelected(filter.isCorrectingCase());
         correctCheck3.setEnabled(!caseCheck3.isSelected());
         correctCheck3.setToolTipText(org.openide.util.NbBundle.getMessage(Example.class, "Example.correctCheck3.tooltip"));  //NOI18N
 
         caseCheck3.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent arg0) {
                 filter.setCaseSensitive(caseCheck3.isSelected());
                 correctCheck3.setEnabled(!caseCheck3.isSelected());
                 if (caseCheck3.isSelected()) {
                     correctCheck3.setSelected(false);
                     filter.setCorrectCase(false);
                 }
             }
         });
 
         correctCheck3.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent arg0) {
                 filter.setCorrectCase(correctCheck3.isSelected());
             }
         });
 
         panel.add(new JLabel(org.openide.util.NbBundle.getMessage(Example.class,"Example._createPanel().panel.JLabel_anon3.text")));  //NOI18N
         panel.add(_combo);
 
         panel.add(Box.createGlue());
         panel.add(caseCheck3);
 
         panel.add(Box.createGlue());
         panel.add(correctCheck3);
 
         return panel;
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         new Example();
     }
     private CompleterTextField _tf;
     private CompleterTextField _tfww;
     private JComboBox _combo;
 }
 
 class TT {
 
     public TT(String bdy) {
         this.bdy = bdy;
     }
     final String bdy;
 
     @Override
     public String toString() {
         return bdy;
     }
 }
