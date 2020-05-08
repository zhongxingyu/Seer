 package plugins.adufour.vars.lang;
 
 import plugins.adufour.vars.gui.model.ValueSelectionModel;
 
 public class VarEnum<T extends Enum<T>> extends Var<T>
 {
     /**
      * 
      * @param name
      * @param defaultValue
      * @throws NullPointerException
      *             if defaultValue is null
      */
     @SuppressWarnings("unchecked")
     public VarEnum(String name, T defaultValue) throws NullPointerException
     {
         super(name, (Class<T>) defaultValue.getClass(), defaultValue);
        setDefaultEditorModel(new ValueSelectionModel<T>((T[]) defaultValue.getClass().getEnumConstants(), defaultValue, false));
     }
 
     @Override
     public String getValueAsString()
     {
         return getValue().name();
     }
     
     /**
      * Parses the given string into an Enumeration value
      */
     @Override
     public T parse(String s)
     {
         return Enum.valueOf(getType(), s);
     }
 }
