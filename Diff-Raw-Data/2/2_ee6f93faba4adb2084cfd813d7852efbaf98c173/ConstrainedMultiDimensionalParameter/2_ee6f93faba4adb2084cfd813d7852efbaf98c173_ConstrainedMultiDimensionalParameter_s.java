 package org.pillarone.riskanalytics.core.parameterization;
 
 import org.pillarone.riskanalytics.core.components.Component;
 import org.pillarone.riskanalytics.core.components.IComponentMarker;
 import org.pillarone.riskanalytics.core.model.Model;
 import org.pillarone.riskanalytics.core.util.GroovyUtils;
 
 import java.math.BigDecimal;
 import java.util.*;
 
 public class ConstrainedMultiDimensionalParameter extends TableMultiDimensionalParameter {
 
     private IMultiDimensionalConstraints constraints;
     private Map<Integer, Map<String, Component>> comboBoxValues = new HashMap<Integer, Map<String, Component>>();
 
     public ConstrainedMultiDimensionalParameter(List cellValues, List titles, IMultiDimensionalConstraints constraints) {
         super(cellValues, titles);
         this.constraints = constraints;
     }
 
 
     @Override
     public void setValueAt(Object value, int row, int column) {
         if (constraints.matches(row, column, value)) {
             super.setValueAt(value, row, column);
         } else {
             throw new IllegalArgumentException("Value does not pass constraints");
         }
     }
 
     public IMultiDimensionalConstraints getConstraints() {
         return constraints;
     }
 
     public void setSimulationModel(Model simulationModel) {
         this.simulationModel = simulationModel;
         for (int i = 0; i < getValueColumnCount(); i++) {
             final Class columnType = constraints.getColumnType(i);
             if (IComponentMarker.class.isAssignableFrom(columnType)) {
                 Map<String, Component> result = new HashMap<String, Component>();
                 List<Component> componentsOfType = simulationModel.getMarkedComponents(columnType);
                 for (Component component : componentsOfType) {
                     result.put(normalizeName(component.getName()), component);
                 }
                 comboBoxValues.put(i, result);
             }
         }
     }
 
     public void validateValues() {
         // column 0 for the index 0,1,2,3,...
         int col = 0;
         for (List list : values) {
             int row = 0;
             for (Object value : list) {
                 Object possibleValues = getPossibleValues(row + 1, col);
                 if (possibleValues instanceof List) {
                     List<String> validValues = (List<String>) possibleValues;
                     if (!validValues.contains(value)) {
                         if (validValues.size() > 0) {
                             list.set(row, validValues.get(0));
                         }
                     }
                 }
                 row++;
             }
             col++;
         }
     }
 
     public List getValuesAsObjects(int column) {
         final Class columnType = constraints.getColumnType(column);
         List result = new LinkedList();
         if (IComponentMarker.class.isAssignableFrom(columnType)) {
             Map<String, Component> componentsOfType = comboBoxValues.get(column);
             List<String> selectedValues = values.get(column);
             for (String selectedValue : selectedValues) {
                 result.add(componentsOfType.get(selectedValue));
             }
         } else {
             result.addAll(values.get(column));
         }
 
         return result;
     }
 
     @Override
     public Object getPossibleValues(int row, int column) {
         if (row == 0) {
             return new Object();
         }
         Class columnClass = constraints.getColumnType(column);
         if (IComponentMarker.class.isAssignableFrom(columnClass)) {
             List<String> names = new ArrayList<String>();
             List components = simulationModel.getMarkedComponents(columnClass);
             for (Object component : components) {
                 names.add(normalizeName(((Component) component).getName()));
             }
             return names;
         } else if (columnClass.isEnum()) {
             return GroovyUtils.getEnumValuesFromClass(columnClass);
         } else {
             return new Object();
         }
     }
 
     protected void appendAdditionalConstructorArguments(StringBuffer buffer) {
         super.appendAdditionalConstructorArguments(buffer);
         buffer.append(", ");
         buffer.append("org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory.getConstraints('").append(constraints.getName()).append("')");
     }
 
     @Override
     public boolean supportsZeroRows() {
         return true;
     }
 
     @Override
     protected Object createDefaultValue(int row, int column, Object object) {
         Object result = "";
         Class columnClass = constraints.getColumnType(column);
         if (IComponentMarker.class.isAssignableFrom(columnClass)) {
             List list = (List) getPossibleValues(row + 1, column);
             if (list != null && list.size() > 0)
                 result = list.get(0);
         } else if (columnClass == BigDecimal.class) {
             result = new BigDecimal(0);
         } else if (columnClass == Double.class) {
             result = 0d;
         } else if (columnClass == Integer.class) {
             result = 0;
         } else {
             try {
                 result = columnClass.newInstance();
             } catch (Exception e) {
                 throw new RuntimeException(columnClass.getSimpleName() + " not supported as column type", e);
             }
         }
         return result;
     }
 
     @Override
     public ConstrainedMultiDimensionalParameter clone() throws CloneNotSupportedException {
         final ConstrainedMultiDimensionalParameter clone = (ConstrainedMultiDimensionalParameter) super.clone();
         clone.comboBoxValues = new HashMap<Integer, Map<String, Component>>();
         clone.constraints = constraints;
         return clone;
     }
 
     @Override
     public int getValueColumnCount() {
         return titles.size();
     }
 }
