 package org.pillarone.riskanalytics.core.parameterization;
 
 import org.pillarone.riskanalytics.core.components.Component;
 import org.pillarone.riskanalytics.core.model.Model;
 
 import java.util.*;
 
 public class ComboBoxMatrixMultiDimensionalParameter extends MatrixMultiDimensionalParameter implements IComboBoxBasedMultiDimensionalParameter {
 
     private Map<String, Component> comboBoxValues = new HashMap<String, Component>();
     private Class markerClass;
 
     public ComboBoxMatrixMultiDimensionalParameter(List cellValues, List columnTitles, Class markerClass) {
         super(cellValues, columnTitles, columnTitles);
         this.markerClass = markerClass;
     }
 
     public void moveColumnTo(int from, int to) {
         Collections.swap(values, from, to);
     }
 
     public void setValueAt(Object value, int rowIndex, int columnIndex) {
         if (rowIndex > 0 && columnIndex > 0) {
             super.setValueAt(value, rowIndex, columnIndex);
             super.setValueAt(value, columnIndex, rowIndex);
         } else if (!(rowIndex == 0 && columnIndex == 0)) {
             columnTitles.set(rowIndex - 1, value);
             rowTitles.set(rowIndex - 1, value);
         }
     }
 
     protected void columnsAdded(int i) {
         for (int j = 0; j < i; j++) {
             columnTitles.add(comboBoxValues.keySet().toArray()[0]);
         }
     }
 
     protected void rowsAdded(int i) {
         for (int j = 0; j < i; j++) {
             rowTitles.add(comboBoxValues.keySet().toArray()[0]);
         }
     }
 
     public boolean isCellEditable(int row, int column) {
         return !(row == 0 || column == 0);
     }
 
     protected void appendAdditionalConstructorArguments(StringBuffer buffer) {
         buffer.append(",");
         appendList(buffer, columnTitles);
         buffer.append(",");
         buffer.append(markerClass.getName());
     }
 
     public void setSimulationModel(Model simulationModel) {
         super.setSimulationModel(simulationModel);
         List<Component> components = simulationModel.getMarkedComponents(markerClass);
         for (Component component : components) {
             comboBoxValues.put(component.getNormalizedName(), component);
         }
     }
 
     public void validateValues() {
         int i = 0;
         for (Object value : columnTitles) {
             if (!comboBoxValues.keySet().contains(value)) {
                 Object[] validValues = comboBoxValues.keySet().toArray();
                 if (validValues.length > 0) {
                     columnTitles.set(i, validValues[0]);
                 }
             }
             i++;
         }
     }
 
     public Object getPossibleValues(int row, int column) {
         if (row == 0 && column == 0) {
             return "";
         }
 
         if (row == 0 || column == 0) {
             List names = new LinkedList();
             for (String c : comboBoxValues.keySet()) {
                 names.add(c);
             }
             return names;
         } else {
             return getValueAt(row, column);
         }
     }
 
     public Class getMarkerClass() {
         return markerClass;
     }
 
     public boolean rowCountChangeable() {
         return true;
     }
 
     public boolean columnCountChangeable() {
         return true;
     }
 
     @Override
     public ComboBoxMatrixMultiDimensionalParameter clone() throws CloneNotSupportedException {
         final ComboBoxMatrixMultiDimensionalParameter clone = (ComboBoxMatrixMultiDimensionalParameter) super.clone();
        clone.comboBoxValues = new HashMap<String, Component>();
         clone.markerClass = markerClass;
         if (simulationModel != null) {
             setSimulationModel(simulationModel);
         }
         return clone;
     }
 }
