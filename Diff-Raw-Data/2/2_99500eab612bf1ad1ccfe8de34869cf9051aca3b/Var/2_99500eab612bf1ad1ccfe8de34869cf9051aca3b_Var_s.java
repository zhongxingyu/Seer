 package plugins.adufour.vars.lang;
 
 import icy.file.xml.XMLPersistent;
 import icy.util.XMLUtil;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import plugins.adufour.vars.gui.VarEditor;
 import plugins.adufour.vars.gui.model.ValueSelectionModel;
 import plugins.adufour.vars.gui.model.VarEditorModel;
 import plugins.adufour.vars.gui.swing.ComboBox;
 import plugins.adufour.vars.gui.swing.Label;
 import plugins.adufour.vars.util.VarException;
 import plugins.adufour.vars.util.VarListener;
 
 /**
  * <p>
  * Top-level class defining a generic variable. A {@link Var} object embeds an inner value of
  * generic type T, and offers a number of methods to get/set this value, hide the inner value with a
  * virtual reference to another compatible {@link Var} object, and listen to value or reference
  * changes via the {@link VarListener} interface.
  * </p>
  * While many subclasses are available for most common data types (see the direct sub-classes in
  * this package), it is possible to create custom variable types by extending these classes. The
  * most important methods to override are the following:<br/>
  * <ul>
  * <li>The empty {@link #parse(String)} method should be overridden to provide the ability to read
  * text input (either from a graphical interface or when saving/reading data from XML files).</li>
  * <li>The {@link #getValueAsString()} method returns a pretty-printed version of the variable's
  * value, and is used to display and store the value into XML files. Overriding implementations
  * should make sure that the result of this method is compatible with the {@link #parse(String)}
  * method to ensure proper reloading from XML files.</li>
  * <li>The {@link #isAssignableFrom(Var)} indicates which types of variables can be used as
  * reference variables. The default implementation relies on the native
  * {@link Class#isAssignableFrom(Class)} method, but can be overridden to fine-tune the type of
  * supported data (see the {@link VarArray#isAssignableFrom(Var)} method for an example).</li>
  * <li>The {@link #createVarEditor()} method is responsible for generating a {@link VarEditor}
  * object that will handle user interaction in a graphical user interface. For each variable type, a
  * default {@link VarEditor} is provided, but overriding implementation can provide their own
  * component.</li>
  * </ul>
  * 
  * @see VarEditor
  * 
  * @author Alexandre Dufour
  * 
  * @param <T>
  *            The type of the inner (boxed) value
  */
 public class Var<T> implements XMLPersistent, VarListener<T>
 {
     /**
      * Attribute key defining the unique identifier of a variable. This key is used by the
      * {@link XMLPersistent} mechanism to save/load the variable value to/from XML files.
      */
     public static final String         XML_KEY_ID    = "ID";
     
     /**
      * Attribute key defining the value of a variable. This key is used by the {@link XMLPersistent}
      * mechanism to save/load the variable value to/from XML files.
      */
     static final String                XML_KEY_VALUE = "value";
     
     private final String               name;
     
     /**
      * The {@link Class} definition describing the type of the variable value
      */
     protected Class<T>                 type;
     
     private final T                    defaultValue;
     
     private T                          value;
     
     private Var<? extends T>           reference;
     
     private VarEditorModel<T>          defaultEditorModel;
     
     private final List<VarListener<T>> listeners     = new ArrayList<VarListener<T>>();
     
     /**
      * Creates a new {@link Var}iable with given name and non-null default value.
      * 
      * @param name
      *            the name of this variable
      * @param defaultValue
      *            the non-null default value of this variable
      * @throws NullPointerException
      *             if {@link #defaultValue} is null
      */
     @SuppressWarnings("unchecked")
     public Var(String name, T defaultValue) throws NullPointerException
     {
         this(name, (Class<T>) defaultValue.getClass(), defaultValue);
     }
     
     /**
      * Creates a new {@link Var}iable with specified name and inner value type (the default value is
      * set to null).
      * 
      * @param name
      *            the name of this variable
      * @param type
      *            the type of this variable
      */
     public Var(String name, Class<T> type)
     {
         this(name, type, null);
     }
     
     /**
      * Creates a new {@link Var}iable with the specified name, and inner value type and default
      * value (may be null).
      * 
      * @param name
      *            the name of this variable
      * @param type
      *            the type of this variable
      */
     public Var(String name, Class<T> type, T defaultValue)
     {
         this.name = name;
         this.type = type;
         this.defaultValue = defaultValue;
         this.value = defaultValue;
     }
     
     /**
      * Adds the specified listener to the list of registered listeners
      * 
      * @param listener
      *            the listener to register
      */
     public void addListener(VarListener<T> listener)
     {
         synchronized (listeners)
         {
             if (!listeners.contains(listener)) listeners.add(listener);
         }
     }
     
     /**
      * Creates a new {@link VarEditor} object that allows the user to graphically adjust the value
      * of this variable. By default this editor is an empty label for generic types, but this method
      * can be overridden to provide a custom editor
      * 
      * @return the variable editor embarking the graphical component
      */
     public VarEditor<T> createVarEditor()
     {
         if (getDefaultEditorModel() instanceof ValueSelectionModel) return new ComboBox<T>(this);
         
         if (getDefaultEditorModel() == null) return new Label<T>(this);
         
         throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support editor models of type " + getDefaultEditorModel().getClass().getSimpleName());
     }
     
     protected void fireVariableChanged(Var<? extends T> oldRef, Var<? extends T> newRef)
     {
         final ArrayList<VarListener<T>> listenersCopy;
         
         synchronized (listeners)
         {
             listenersCopy = new ArrayList<VarListener<T>>(listeners);
         }
         
         for (VarListener<T> l : listenersCopy)
             l.referenceChanged(this, oldRef, newRef);
     }
     
     protected void fireVariableChanged(T oldValue, T newValue)
     {
         final ArrayList<VarListener<T>> listenersCopy;
         
         synchronized (listeners)
         {
             listenersCopy = new ArrayList<VarListener<T>>(listeners);
         }
         
         for (VarListener<T> l : listenersCopy)
             l.valueChanged(this, oldValue, newValue);
     }
     
     /**
      * @return the default editor model
      */
     public VarEditorModel<T> getDefaultEditorModel()
     {
         return defaultEditorModel;
     }
     
     public T getDefaultValue()
     {
         return defaultValue;
     }
     
     public String getName()
     {
         return name;
     }
     
     /**
      * @return The variable referenced by the current variable (or null if no variable is
      *         referenced)
      * @see #getValue()
      */
     public Var<? extends T> getReference()
     {
         return reference;
     }
     
     /**
      * @return a {@link Class} object defining the type of the variable's value.
      */
     public Class<T> getType()
     {
         return this.type;
     }
     
     /**
      * @return a user-friendly text describing the type of this variable. By default the returned
      *         string is the simple class name of this variable's value (as defined by the
      *         {@link Class#getSimpleName()} method).
      */
     public String getTypeAsString()
     {
         Class<?> type = getType();
         return type == null ? "null" : getType().getSimpleName();
     }
     
     /**
      * @return the value stored in this variable, or the value in the referenced variable if the
      *         reference is not null. Note that this method may return null. To forbid null values,
      *         use the {@link #getValue(boolean)} method instead.
      * @see #getReference()
      */
     public T getValue()
     {
         return getValue(false);
     }
     
     /**
      * @return the value stored in this variable, or the value in the referenced variable if the
      *         reference is not null. Note that this method will throw a {@link VarException} if the
      *         {@code forbidNull} argument is true
      * @param true to throw an exception if the underlying value is null
      * @see #getReference()
      */
     public T getValue(boolean forbidNull) throws VarException
     {
         T returnValue = reference == null ? value : reference.getValue();
         
        if (returnValue == null && forbidNull) throw new VarException("Parameter " + name + " has not been set.");
         
         return returnValue;
     }
     
     /**
      * @return a pretty-printed text representation of the variable's value. This text is used to
      *         display the value (e.g. in a graphical interface) or store the value into XML files.
      *         Overriding implementations should make sure that the result of this method is
      *         compatible with the {@link #parse(String)} method to ensure proper reloading from XML
      *         files.
      * @param followReference
      *            set to true to return the String representation of the reference variable (if any)
      */
     public String getValueAsString(boolean followReference)
     {
         return reference != null && followReference ? reference.getValueAsString(followReference) : getValueAsString();
     }
     
     /**
      * @return a pretty-printed text representation of the variable's local value (referenced
      *         variables are <b>not</b> followed). This text is used to display the value (e.g. in a
      *         graphical interface) or store the value into XML files. Overriding implementations
      *         should make sure that the result of this method is compatible with the
      *         {@link #parse(String)} method to ensure proper reloading from XML files.
      */
     public String getValueAsString()
     {
         return value == null ? "null" : value.toString();
     }
     
     /**
      * Checks whether the type of the given variable is equal or extends this variable's type.<br>
      * If the result is true, then the given variable can become a link source for this variable
      * 
      * @param source
      * @return true if the source type is equal or wider than this variable's type, false otherwise
      *         (including if source has null type)
      */
     public boolean isAssignableFrom(Var<?> source)
     {
         if (source.getType() == null) return false;
         
         return getType().isAssignableFrom(source.getType());
     }
     
     @Override
     public boolean loadFromXML(Node node)
     {
         try
         {
             T xmlValue = parse(XMLUtil.getAttributeValue((Element) node, XML_KEY_VALUE, null));
             setValue(xmlValue);
             return true;
         }
         catch (UnsupportedOperationException e)
         {
             if (node.getNodeValue() != null)
             {
                 System.err.println("Warning: unable to parse " + node.getNodeValue() + " into a " + getClass().getSimpleName());
             }
             
             setValue(null);
             return true;
         }
         catch (NullPointerException npE)
         {
             return false;
         }
     }
     
     /**
      * Parses the given String into the current type
      * 
      * @param text
      * @return The variable value corresponding to the given string
      * @throws UnsupportedOperationException
      *             if the parser has not been implemented for the current type
      */
     public T parse(String text)
     {
         throw new UnsupportedOperationException("Parsing operation not supported for type " + getClass().getSimpleName());
     }
     
     /**
      * Removes the specified listener from the list of registered listeners.
      * 
      * @param listener
      *            the listener to remove
      */
     public void removeListener(VarListener<T> listener)
     {
         synchronized (listeners)
         {
             listeners.remove(listener);
         }
     }
     
     /**
      * Removes all listeners currently registered to this variable.
      */
     public void removeListeners()
     {
         synchronized (listeners)
         {
             listeners.clear();
         }
     }
     
     /**
      * Saves the current variable to the specified node
      * 
      * @throws UnsupportedOperationException
      *             if the functionality is not supported by the current variable type
      */
     @Override
     public boolean saveToXML(Node node) throws UnsupportedOperationException
     {
         XMLUtil.setAttributeValue((Element) node, Var.XML_KEY_VALUE, value == null ? "" : getValueAsString());
         
         return true;
     }
     
     /**
      * Sets a default {@link VarEditorModel} object which can be used to generate a graphical editor
      * for this variable. This default model is used by the {@link #createVarEditor()} method is
      * overridden to provide a custom editor
      * 
      * @param model
      *            the model used by the {@link #createVarEditor()} method to generate the
      *            appropriate graphical component
      * @see VarEditorModel
      */
     public void setDefaultEditorModel(VarEditorModel<T> model)
     {
         this.defaultEditorModel = model;
         if (model != null) setValue(model.getDefaultValue());
     }
     
     /**
      * Sets the current variable to reference the specified variable (or null to release the
      * reference). If the reference is non-null, the {@link #getValue()} method will disregard the
      * local value and return the value of the referenced variable
      * 
      * @param variable
      *            the variable to reference
      * @throws ClassCastException
      *             if the two arguments are incompatible and cannot be linked
      */
     public void setReference(Var<T> variable) throws ClassCastException
     {
         if (variable != null && !isAssignableFrom(variable) && !(variable instanceof VarObject))
         {
             throw new ClassCastException(this + " cannot point to " + variable);
         }
         
         boolean change = (this.reference != variable);
         
         if (change)
         {
             @SuppressWarnings("unchecked")
             Var<T> oldRef = (Var<T>) this.reference;
             
             if (oldRef != null) oldRef.removeListener(this);
             this.reference = variable;
             if (this.reference != null) variable.addListener(this);
             
             fireVariableChanged(oldRef, reference);
         }
     }
     
     /**
      * Sets the value of this variable and notify the listeners
      * 
      * @param newValue
      * @throws IllegalAccessError
      *             if this variable is already linked to another one
      */
     public void setValue(T newValue) throws IllegalAccessError
     {
         if (this.reference != null) throw new IllegalAccessError("Cannot assign the value of a linked variable.");
         
         T oldValue = this.value;
         this.value = newValue;
         
         if (oldValue != newValue) fireVariableChanged(oldValue, newValue);
     }
     
     @Override
     public String toString()
     {
         String s = getName() + " (" + getClass().getSimpleName() + ")";
         
         s += (reference == null ? " = " + getValueAsString() : " => " + reference.toString());
         
         return s;
     }
     
     /**
      * Called when this variable has a non-null reference and receives a value-changed event from
      * this reference
      * 
      * @param source
      *            the variable sending the event. This should be equal to the result of
      *            {@link #getReference()}
      * @param oldValue
      *            the old variable value
      * @param newValue
      *            the new variable value
      */
     @Override
     public void valueChanged(Var<T> source, T oldValue, T newValue)
     {
         fireVariableChanged(oldValue, newValue);
     }
     
     @Override
     public void referenceChanged(Var<T> source, Var<? extends T> oldReference, Var<? extends T> newReference)
     {
     }
 }
