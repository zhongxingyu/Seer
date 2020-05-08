 package com.googlecode.whatswrong;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 
 /**
  * A DependencyFilterPanel controls a EdgeLabelFilter and a EdgeTokenFilter and updates an NLPCanvas after changes to
  * the filters.
  *
  * @author Sebastian Riedel
  */
 @SuppressWarnings({"MissingMethodJavaDoc"})
 public class DependencyFilterPanel extends ControllerPanel {
 
     /**
      * Creates a new DependencyFilterPanel.
      *
      * @param nlpCanvas       the NLPCanvas to update when the filters are changed through this panel.
      * @param edgeLabelFilter The EdgeLabelFilter to control through this panel.
      * @param edgeTokenFilter The EdgeTokenFilter to control through this panel.
      */
     public DependencyFilterPanel(final NLPCanvas nlpCanvas,
                                  final EdgeLabelFilter edgeLabelFilter,
                                  final EdgeTokenFilter edgeTokenFilter) {
         setLayout(new GridBagLayout());
         //setBorder(new TitledBorder(new EtchedBorder(), "Filter By Token"));
         GridBagConstraints c = new GridBagConstraints();
 
         c.gridx = 0;
         c.weightx = 0.0;
         c.anchor = GridBagConstraints.EAST;
         add(new JLabel("Label:"), c);
 
         //setBorder(new TitledBorder(new EtchedBorder(), "Filter By Label"));
        final JTextField labelField = new FilterTextField("text,text,...");
         labelField.setColumns(10);
         c.weightx = 1.0;
         c.fill = GridBagConstraints.HORIZONTAL;
         //c.anchor = GridBagConstraints.WEST;
         c.gridx = 1;
         add(labelField, c);
         labelField.addKeyListener(new KeyAdapter() {
             public void keyReleased(KeyEvent e) {
                 edgeLabelFilter.clear();
                 String[] split = labelField.getText().split("[,]");
                 for (String label : split)
                     edgeLabelFilter.addAllowedLabel(label);
                 nlpCanvas.updateNLPGraphics();
             }
         });
 
 
         c.gridx = 0;
         c.gridy = 1;
         c.weightx = 0.0;
         c.fill = GridBagConstraints.NONE;
         c.anchor = GridBagConstraints.EAST;
         add(new JLabel("Token:"), c);
 
 
         c.gridx = 1;
         c.weightx = 1.0;
         c.fill = GridBagConstraints.HORIZONTAL;
         c.anchor = GridBagConstraints.WEST;
        final JTextField tokenTextField = new FilterTextField("text1,2-5,...");
        tokenTextField.setColumns(10);
        add(tokenTextField, c);
        tokenTextField.addKeyListener(new KeyAdapter() {
             public void keyReleased(KeyEvent e) {
                 edgeTokenFilter.clear();
                String[] split = tokenTextField.getText().split("[,]");
                 for (String property : split)
                     edgeTokenFilter.addAllowedProperty(property);
                 nlpCanvas.updateNLPGraphics();
             }
         });
         final JCheckBox usePaths = new JCheckBox("Only Paths", edgeTokenFilter.isUsePaths());
         usePaths.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 edgeTokenFilter.setUsePaths(usePaths.isSelected());
                 nlpCanvas.updateNLPGraphics();
             }
         });
 
         c.gridx = 0;
         c.gridy = 2;
         c.weightx = 0.0;
         c.fill = GridBagConstraints.NONE;
         c.anchor = GridBagConstraints.EAST;
         add(new JLabel("Options:"), c);
 
         c.gridx = 1;
         c.gridy = 2;
         c.weightx = 0;
         c.fill = GridBagConstraints.NONE;
         c.anchor = GridBagConstraints.WEST;
         add(usePaths, c);
 
         final JCheckBox collaps = new JCheckBox("Collaps");
         collaps.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 edgeTokenFilter.setCollaps(collaps.isSelected());
                 nlpCanvas.updateNLPGraphics();
             }
         });
         add(collaps, new SimpleGridBagConstraints(3, false));
 
         final JCheckBox wholeWords = new JCheckBox("Whole Words");
         wholeWords.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 edgeTokenFilter.setWholeWords(wholeWords.isSelected());
                 nlpCanvas.updateNLPGraphics();
             }
         });
         add(wholeWords, new SimpleGridBagConstraints(4, false));
 
 //    final JButton onlySelected = new JButton("Hide Unselected");
 //    //onlySelected.setEnabled(!nlpCanvas.getDependencyLayout().getSelected().isEmpty());
 //    onlySelected.addActionListener(new ActionListener() {
 //      public void actionPerformed(ActionEvent e) {
 ////        if (!nlpCanvas.getSpanLayout().getSelected().isEmpty())
 ////          nlpCanvas.getSpanLayout().onlyShow(nlpCanvas.getSpanLayout().getSelected());
 ////        else
 ////          nlpCanvas.getSpanLayout().showAll();
 ////        nlpCanvas.updateNLPGraphics();
 //      }
 //    });
 //    add(onlySelected, new SimpleGridBagConstraints(5, false));
 
         //setPreferredSize(new Dimension(200, (int) getPreferredSize().getHeight()));
 
     }
 }
