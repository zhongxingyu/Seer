 package org.pillarone.riskanalytics.core.parameterization;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.pillarone.riskanalytics.core.components.Component;
 import org.pillarone.riskanalytics.core.model.Model;
 
 import java.util.*;
 
 public class ComboBoxTableMultiDimensionalParameter extends TableMultiDimensionalParameter implements IComboBoxBasedMultiDimensionalParameter {
 
     static Log LOG = LogFactory.getLog(ComboBoxTableMultiDimensionalParameter.class);
 
     private Map comboBoxValues = new HashMap();
     private Class markerClass;
 
     public ComboBoxTableMultiDimensionalParameter(List cellValues, List titles, Class markerClass) {
         super(cellValues, titles);
         this.markerClass = markerClass;
     }
 
 
     public void setSimulationModel(Model simulationModel) {
         super.setSimulationModel(simulationModel);
         List<Component> markedComponents = simulationModel.getMarkedComponents(markerClass);
         for (Component c : markedComponents) {
             comboBoxValues.put(normalizeName(c.getName()), c);
         }
         LOG.debug("Marker: " + markerClass + ", comboBoxValues: " + comboBoxValues.values());
     }
 
     public void validateValues() {
         for (List list : values) {
             int i = 0;
             for (Object value : list) {
                 if (!comboBoxValues.keySet().contains(value)) {
                     Object[] validValues = comboBoxValues.keySet().toArray();
                     if (validValues.length > 0) {
                         list.set(i, validValues[0]);
                     }
                 }
                 i++;
             }
         }
     }
 
     public Object getPossibleValues(int row, int column) {
         if (row == 0) {
             return getValueAt(row, column);
         } else {
             List names = new LinkedList();
             for (Object c : comboBoxValues.values()) {
                 names.add(normalizeName(((Component) c).getName()));
             }
             return names;
         }
     }
 
 
     public List getValuesAsObjects() {
         List result = new ArrayList();
         if (valuesConverted) {
             for (Object o : getValues()) {
                 if (!(o instanceof String && ((String) o).length() == 0)) {
                     result.add(comboBoxValues.get(o));
                 }
             }
         } else {
             for (Object o : getValues()) {
                 List l = new ArrayList();
                 for (Object obj : (List) o) {
                     if (!(o instanceof String && ((String) o).length() == 0)) {
                         l.add(comboBoxValues.get(obj));
                     }
 
                 }
                 result.add(l);
             }
         }
         return result;
     }
 
     public Class getMarkerClass() {
         return markerClass;
     }
 
     protected void appendAdditionalConstructorArguments(StringBuffer buffer) {
         super.appendAdditionalConstructorArguments(buffer);
         buffer.append(", ");
         buffer.append(markerClass.getName());
     }
 
     @Override
     public boolean supportsZeroRows() {
         return true;
     }
 
     @Override
     protected Object createDefaultValue(int row, int column, Object object) {
         List list = (List) getPossibleValues(1, 1);
         return list.size() > 0 ? list.get(0) : "";
     }
 
     @Override
     public ComboBoxTableMultiDimensionalParameter clone() throws CloneNotSupportedException {
         final ComboBoxTableMultiDimensionalParameter clone = (ComboBoxTableMultiDimensionalParameter) super.clone();
         clone.markerClass = markerClass;
         if (simulationModel != null) {
             setSimulationModel(simulationModel);
         }
         return clone;
     }
 }
