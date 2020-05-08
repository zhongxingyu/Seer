 package plugins.adufour.vars.gui.model;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Variable constraint that only allows to choose from a specified set of values
  * 
  * @author Alexandre Dufour
  * 
  * @param <T>
  */
 public class ValueSelectionModel<T> implements VarEditorModel<T>
 {
     protected final List<T> validValues;
 
     protected final T       defaultValue;
 
     private final boolean   freeInput;
 
     public ValueSelectionModel()
     {
         this(null);
     }
 
     public ValueSelectionModel(T[] validValues)
     {
         this(validValues, 0, validValues == null || validValues.length == 0);
     }
 
     /**
      * 
      * @param validValues
      *            a list of valid values
      * @param defaultValueIndex
      *            the index of the default selected value
      * @param freeInput
      *            set to true if any valid outside the list is valid (no check will be performed)
      */
     public ValueSelectionModel(T[] validValues, int defaultValueIndex, boolean freeInput)
     {
         this.validValues = (validValues == null) ? new ArrayList<T>() : Arrays.asList(validValues);
         this.defaultValue = (this.validValues.size() > defaultValueIndex) ? this.validValues.get(defaultValueIndex) : null;
         this.freeInput = freeInput;
     }
 
     @Override
     public boolean isValid(T value)
     {
         return freeInput || validValues.contains(value);
     }
 
     @Override
     public T getDefaultValue()
     {
         return defaultValue;
     }
 
     public List<T> getValidValues()
 	{
 		return validValues;
 	}
     
     public boolean isFreeInput()
 	{
 		return freeInput;
 	}
 }
