 package plugins.adufour.ezplug;
 
 import plugins.adufour.vars.gui.model.IntegerRangeModel;
 import plugins.adufour.vars.lang.VarInteger;
 
 /**
  * Specialized implementation of {@link plugins.adufour.ezplug.EzVarNumeric} for variables of type integer
  * 
  * @author Alexandre Dufour
  * 
  */
 public class EzVarInteger extends EzVarNumeric<Integer>
 {
     /**
      * Creates a new integer variable with default minimum and maximum values
      */
     public EzVarInteger(String varName)
     {
         this(varName, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
     }
 
     /**
      * Creates a new integer variable with specified minimum and maximum values
      * 
      * @param varName
      *            the name of the variable (as it will appear on the interface)
      * @param min
      *            the minimum allowed value
      * @param max
      *            the maximum allowed value
      * @param step
      *            the step between consecutive values
      */
     public EzVarInteger(String varName, int min, int max, int step)
     {
         this(varName, min, min, max, step);
     }
 
     /**
      * Creates a new integer variable with specified default, minimum and maximum values
      * 
      * @param varName
      *            the name of the variable (as it will appear on the interface)
      * @param value
      *            the default value
      * @param min
      *            the minimum allowed value
      * @param max
      *            the maximum allowed value
      * @param step
      *            the step between consecutive values
      */
     public EzVarInteger(String varName, int value, int min, int max, int step)
     {
         super(new VarInteger(varName, value), new IntegerRangeModel(value, min, max, step));
     }
 
     /**
      * Creates a new integer variable with a given array of possible values
      * 
      * @param varName
      *            the name of the variable (as it will appear on the interface)
      * @param defaultValues
      *            the list of possible values the user may choose from
      * @param allowUserInput
      *            set to true to allow the user to input its own value manually, false otherwise
      * @throws NullPointerException
      *             if the defaultValues parameter is null
      */
     public EzVarInteger(String varName, Integer[] defaultValues, boolean allowUserInput) throws NullPointerException
     {
         this(varName, defaultValues, 0, allowUserInput);
     }
 
     /**
      * Creates a new integer variable with a given array of possible values
      * 
      * @param varName
      *            the name of the variable (as it will appear on the interface)
      * @param defaultValues
      *            the list of possible values the user may choose from
      * @param defaultValueIndex
      *            the index of the default selected value
      * @param allowUserInput
      *            set to true to allow the user to input its own value manually, false otherwise
      * @throws NullPointerException
      *             if the defaultValues parameter is null
      */
     public EzVarInteger(String varName, Integer[] defaultValues, int defaultValueIndex, boolean allowUserInput) throws NullPointerException
     {
        super(new VarInteger(varName, null), defaultValues, defaultValueIndex, allowUserInput);
     }
 }
