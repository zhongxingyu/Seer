 package org.jetbrains.plugins.xml.searchandreplace.ui.view;
 
 import javax.swing.*;
 import java.awt.*;
 import java.util.Arrays;
 
 public class PatternView extends JPanel {
 
   public PatternView() {
    LayoutManager lm = new BoxLayout(this, BoxLayout.PAGE_AXIS);
     setLayout(lm);
   }
 
   public void addPredicateView(PredicatePanel panel, PredicatePanel parent) {
     int index = Arrays.asList(getComponents()).indexOf(parent);
     add(panel, index+1);
     panel.updateUI();
   }
 
   public void removePredicateView(PredicatePanel view) {
     remove(view);
     updateUI();
   }
 }
