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
 
 package evopaint.pixel.rulebased.targeting.qualifiers;
 
 import evopaint.Configuration;
 import evopaint.pixel.Pixel;
 import evopaint.pixel.rulebased.targeting.Qualifier;
 import evopaint.util.mapping.RelativeCoordinate;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 
 /**
  *
  * @author Markus Echterhoff <tam@edu.uni-klu.ac.at>
  */
 public class EnergyQualifier extends Qualifier {
 
     private boolean isLeast;
 
     public EnergyQualifier(boolean isLeast) {
         this.isLeast = isLeast;
     }
 
     public EnergyQualifier() {
     }
 
     public boolean isLeast() {
         return isLeast;
     }
 
     public void setLeast(boolean isLeast) {
         this.isLeast = isLeast;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final EnergyQualifier other = (EnergyQualifier) obj;
         if (this.isLeast != other.isLeast) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 97 * hash + (this.isLeast ? 1 : 0);
         return hash;
     }
 
     public String getName() {
         return "energy";
     }
 
     @Override
     public String toString() {
         if (isLeast) {
             return "has the least Energy";
         }
         return "has the most Energy";
     }
 
     @Override
     public String toHTML() {
         return toString();
     }
 
     public List<RelativeCoordinate> getCandidates(Pixel origin, List<RelativeCoordinate> directions, Configuration configuration) {
         List<RelativeCoordinate> ret = new ArrayList(1);
         if (isLeast) {
             int minEnergy = Integer.MAX_VALUE;
             for (RelativeCoordinate direction : directions) {
                 Pixel target = configuration.world.get(origin.getLocation(), direction);
                 if (target == null) {
                     continue;
                 }
                if (ret.size() > 0 && target.getEnergy() < minEnergy) {
                     minEnergy = target.getEnergy();
                     ret.clear();
                 }
                ret.add(direction);
             }
         } else {
             int maxEnergy = 0;
             for (RelativeCoordinate direction : directions) {
                 Pixel target = configuration.world.get(origin.getLocation(), direction);
                 if (target == null) {
                     continue;
                 }
                if (ret.size() > 0 && target.getEnergy() > maxEnergy) {
                     maxEnergy = target.getEnergy();
                     ret.clear();
                 }
                ret.add(direction);
             }
         }
         return ret;
     }
 
     @Override
     public LinkedHashMap<String, JComponent> addParametersGUI(LinkedHashMap<String, JComponent> parametersMap) {
         parametersMap = super.addParametersGUI(parametersMap);
 
         JComboBox comparisonComboBox = new JComboBox();
         comparisonComboBox.setModel(new DefaultComboBoxModel(new String [] {"the least", "the most"}));
         comparisonComboBox.setSelectedItem(isLeast ? "the least" : "the most");
         comparisonComboBox.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 String chosen = (String)((JComboBox) (e.getSource())).getSelectedItem();
                 if (chosen.equals("the least")) {
                     setLeast(true);
                 } else {
                     setLeast(false);
                 }
             }
         });
         parametersMap.put("Comparison", comparisonComboBox);
 
         return parametersMap;
     }
 
 }
