 /*
  *  Copyright (C) 2010 Markus Echterhoff <tam@edu.uni-klu.ac.at>
  * 
  *  This file is part of EvoPaint.
  * 
  *  EvoPaint is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with EvoPaint.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package evopaint.gui.rulesetmanager;
 
 import evopaint.gui.rulesetmanager.util.JRangeSlider;
 import evopaint.pixel.rulebased.targeting.ActionMetaTarget;
 import evopaint.pixel.rulebased.targeting.IConditionTarget;
 import evopaint.pixel.rulebased.targeting.ConditionMetaTarget;
 import evopaint.pixel.rulebased.targeting.ConditionTarget;
 import evopaint.pixel.rulebased.targeting.ITarget;
 import evopaint.pixel.rulebased.targeting.MetaTarget;
 import evopaint.pixel.rulebased.targeting.SingleTarget;
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 /**
  *
  * @author Markus Echterhoff <tam@edu.uni-klu.ac.at>
  */
 public class JConditionTargetPanel extends JPanel {
     private JRangeSlider jRangeSlider;
     private JLabel lowValueLabel;
     private JLabel highValueLabel;
     private JTarget jTarget;
 
     public JConditionTargetPanel(ITarget target) {
         setLayout(new GridBagLayout());
 
         GridBagConstraints c = new GridBagConstraints();
         c.insets = new Insets(5, 5, 5, 5);
 
         final JPanel rangePanel = new JPanel();
         rangePanel.setLayout(new BorderLayout());
 
         JPanel lowAlignmentPanel = new JPanel();
         lowAlignmentPanel.setLayout(new BorderLayout());
         JPanel lowPanel = new JPanel();
         final JLabel lowTextLabel = new JLabel("Min:");
         lowPanel.add(lowTextLabel);
         lowValueLabel = new JLabel();
         lowPanel.add(lowValueLabel);
         lowAlignmentPanel.add(lowPanel, BorderLayout.SOUTH);
         rangePanel.add(lowAlignmentPanel, BorderLayout.WEST);
 
         jRangeSlider = new JRangeSlider(0, 0, 0, 0, JRangeSlider.VERTICAL,
                 JRangeSlider.RIGHTLEFT_BOTTOMTOP);
 
         if (target instanceof ConditionMetaTarget) {
             jRangeSlider.setMinimum(0);
            jRangeSlider.setMaximum(((ConditionMetaTarget)target).getMax());
             jRangeSlider.setLowValue(((ConditionMetaTarget)target).getMin());
             jRangeSlider.setHighValue(((ConditionMetaTarget)target).getMax());
         } else {
             if (((SingleTarget)target).getDirection() == null) {
                 jRangeSlider.setMinimum(0);
                 jRangeSlider.setMaximum(0);
                 jRangeSlider.setLowValue(0);
                 jRangeSlider.setHighValue(0);
             } else {
                 jRangeSlider.setMinimum(0);
                 jRangeSlider.setMaximum(1);
                 jRangeSlider.setLowValue(1);
                 jRangeSlider.setHighValue(1);
             }
         }
 
         lowValueLabel.setText(Integer.toString(jRangeSlider.getLowValue()));
         jRangeSlider.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 lowValueLabel.setText(Integer.toString(jRangeSlider.getLowValue()));
                 highValueLabel.setText(Integer.toString(jRangeSlider.getHighValue()));
             }
         });
         
         rangePanel.add(jRangeSlider, BorderLayout.CENTER);
 
         JPanel highPanel = new JPanel();
         final JLabel highTextLabel = new JLabel("Max:");
         highPanel.add(highTextLabel);
         highValueLabel = new JLabel();
         
         highPanel.add(highValueLabel);
         rangePanel.add(highPanel, BorderLayout.EAST);
 
         add(rangePanel, c);
 
         final JLabel ofLabel = new JLabel("of");
         c.gridx = 1;
         add(ofLabel, c);
         
         jTarget = new JTarget(target, new ActionListener(){
             public void actionPerformed(ActionEvent e) {
                 int currentMax = (Integer)(jRangeSlider.getMaximum());
                 int newMax = ((JToggleButton)e.getSource()).isSelected() ? currentMax + 1 : currentMax - 1;
                 jRangeSlider.setMaximum(newMax);
                 if (jRangeSlider.getHighValue() == currentMax) {
                     jRangeSlider.setHighValue(newMax);
                 }
                 if (jRangeSlider.getLowValue() == currentMax) {
                     jRangeSlider.setLowValue(newMax);
                 }
                 if (newMax > 0) {
                     jRangeSlider.setEmpty(false);
                     lowValueLabel.setEnabled(true);
                     highValueLabel.setEnabled(true);
                     lowTextLabel.setEnabled(true);
                     highTextLabel.setEnabled(true);
                     ofLabel.setEnabled(true);
                 } else {
                     jRangeSlider.setEmpty(true);
                     lowValueLabel.setEnabled(false);
                     highValueLabel.setEnabled(false);
                     lowTextLabel.setEnabled(false);
                     highTextLabel.setEnabled(false);
                     ofLabel.setEnabled(false);
                 }
             }
         });
         c.gridx = 2;
         add(jTarget, c);
 
         lowValueLabel.setText(Integer.toString(jRangeSlider.getLowValue()));
         highValueLabel.setText(Integer.toString(jRangeSlider.getHighValue()));
 
         if (jRangeSlider.getMaximum() > 0) {
             jRangeSlider.setEmpty(false);
             lowValueLabel.setEnabled(true);
             highValueLabel.setEnabled(true);
             lowTextLabel.setEnabled(true);
             highTextLabel.setEnabled(true);
             ofLabel.setEnabled(true);
         } else {
             jRangeSlider.setEmpty(true);
             lowValueLabel.setEnabled(false);
             highValueLabel.setEnabled(false);
             lowTextLabel.setEnabled(false);
             highTextLabel.setEnabled(false);
             ofLabel.setEnabled(false);
         }
     }
 
     public IConditionTarget createConditionTarget() {
         
         ITarget target = jTarget.getTarget();
 
         if (target == null) {
             return new ConditionTarget();
         }
 
         if (target instanceof MetaTarget) {
             return new ConditionMetaTarget(
                     ((MetaTarget)target).getDirections(), jRangeSlider.getLowValue(),
                     jRangeSlider.getHighValue());
         }
 
         return new ConditionTarget(((SingleTarget)target).getDirection());
     }
 
     public ActionMetaTarget createQuantifiedActionTarget() {
         assert (false);
         return null;
     }
 
 }
