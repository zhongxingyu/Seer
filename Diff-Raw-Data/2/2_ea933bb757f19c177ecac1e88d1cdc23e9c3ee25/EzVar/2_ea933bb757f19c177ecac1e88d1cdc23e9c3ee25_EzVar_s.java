 package plugins.adufour.ezplug;
 
 import java.awt.Container;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 
 import plugins.adufour.vars.gui.ComboBox;
 import plugins.adufour.vars.gui.VarEditor;
 import plugins.adufour.vars.gui.VarEditorFactory;
 import plugins.adufour.vars.lang.Constraint;
 import plugins.adufour.vars.lang.ConstraintByValue;
 import plugins.adufour.vars.lang.Var;
 import plugins.adufour.vars.lang.VarListener;
 
 /**
  * Class defining a variable for use within an EzPlug.<br>
  * <br>
  * EzVar objects are powerful components that bind each parameter type to a specific graphical
  * component that can be used to receive input from the user interface. It is designed to be used by
  * plug-in developers in a simple and fast manner with zero knowledge in graphical interface
  * programming.<br>
  * A wide-range of EzVar subclasses are available, depending on the type of data to handle
  * (numerical value, boolean flags, image sequences, file arrays, etc.). Advanced developers may
  * also contribute to the EzVar class hierarchy by implementing additional variable types to fit
  * their needs and that of others.<br>
  * <br>
  * EzVar objects are always instantiated using the subclass corresponding to the parameter type to
  * use. Once created, the variable can be added to the graphical interface of the EzPlug via the
  * {@link plugins.adufour.ezplug.EzPlug#addEzComponent(EzComponent)} method (see sample code in the
  * {@link plugins.adufour.ezplug.EzPlug} class documentation).<br>
  * 
  * @author Alexandre Dufour
  */
 public abstract class EzVar<T> extends EzComponent implements VarListener<T>
 {
     final Var<T>                              variable;
 
     private final JLabel                      jLabelName;
 
     private final VarEditor<T>                varEditor;
 
     private final HashMap<EzComponent, T[]>   visibilityTriggers = new HashMap<EzComponent, T[]>();
 
     private final ArrayList<EzVarListener<T>> listeners          = new ArrayList<EzVarListener<T>>();
 
     /**
      * Constructs a new variable
      * 
      * @param variable
      *            The variable to attach to this object
      * @param constraint
      *            the constraint to apply on the variable when receiving input, or null if a default
      *            constraint should be applied
      */
     EzVar(final Var<T> variable, Constraint<T> constraint)
     {
         super(variable.getName());
         this.variable = variable;
         variable.setConstraint(constraint);
 
         jLabelName = new JLabel(variable.getName());
         varEditor = VarEditorFactory.createEditor(variable);
     }
 
     /**
      * Creates a new variable with a JComboBox as default graphical component
      * 
      * @param varName
      *            the variable name
      * @param defaultValues
      *            the list of values to store in the combo box
      * @param defaultValueIndex
      *            the index of the default selected item
      * @param freeInput
      *            true to allow user manual input, false to restrict the selection to the given list
      */
     EzVar(Var<T> variable, T[] defaultValues, int defaultValueIndex, boolean freeInput)
     {
         this(variable, new ConstraintByValue<T>(defaultValues, defaultValueIndex, freeInput));
     }
 
     /**
      * Adds a new listener that will be notified is this variable changes
      * 
      * @param listener
      *            the listener to add
      */
     public void addVarChangeListener(EzVarListener<T> listener)
     {
         listeners.add(listener);
     }
 
     /**
      * Sets a visibility trigger on the target EzComponent. The visibility state of the target
      * component is set to true whenever this variable is visible and takes any of the trigger
      * values, and false otherwise.
      * 
      * @param targetComponent
      *            the component to hide or show
      * @param values
      *            the list of values which will set the visibility of the target component to true
      */
     public void addVisibilityTriggerTo(EzComponent targetComponent, T... values)
     {
         visibilityTriggers.put(targetComponent, values);
 
         updateVisibilityChain();
     }
 
     @Override
     protected void addTo(Container container)
     {
         GridBagConstraints gbc = new GridBagConstraints();
 
         gbc.insets = new Insets(2, 10, 2, 5);
         gbc.fill = GridBagConstraints.HORIZONTAL;
         // gbc.weighty = 0;
         container.add(jLabelName, gbc);
 
         gbc.weightx = 1;
         // gbc.weighty = 0;
         gbc.gridwidth = GridBagConstraints.REMAINDER;
         container.add(getVarEditor().editorComponent, gbc);
     }
 
     protected void dispose()
     {
         visibilityTriggers.clear();
 
         varEditor.dispose();
 
         // unregister the internal listener
         variable.removeListener(this);
 
         super.dispose();
     }
 
     /**
      * Privileged access to fire listeners from inside the EzPlug package. This method is called
      * after the GUI was created in order to trigger listeners declared in the
      * {@link EzPlug#initialize()} method
      */
     final void fireVariableChangedInternal()
     {
         fireVariableChanged(variable.getValue());
     }
 
     protected final void fireVariableChanged(T value)
     {
         for (EzVarListener<T> l : listeners)
             l.variableChanged(this, value);
 
         if (getUI() != null && varEditor != null)
         {
             updateVisibilityChain();
             getUI().repack(true);
         }
     }
 
     /**
      * Retrieves the default values into the destination array, or returns a new one if dest is not
      * big enough.
      * 
      * @param dest
      *            the array to fill with the values. the array is left untouched if it is not big
      *            enough
      * @throws UnsupportedOperationException
      *             if the user input component is not a combo box
      */
     @SuppressWarnings("unchecked")
     public T[] getDefaultValues(T[] dest)
     {
         if (getVarEditor() instanceof ComboBox)
         {
             JComboBox combo = (JComboBox) getVarEditor().editorComponent;
 
             ArrayList<T> items = new ArrayList<T>(combo.getItemCount());
             for (int i = 0; i < combo.getItemCount(); i++)
                 items.add((T) combo.getItemAt(i));
             return items.toArray(dest);
         }
 
         throw new UnsupportedOperationException("The input component is not a list of values");
     }
 
     protected VarEditor<T> getVarEditor()
     {
         return varEditor;
     }
 
     /**
      * Returns an EzPlug-wide unique identifier for this variable (used to save/load parameters)
      * 
      * @return a String identifier that is unique within the owner plug
      */
     String getID()
     {
         String id = variable.getName();
 
         EzGroup group = getGroup();
 
         while (group != null)
         {
             id = group.name + "." + id;
             group = group.getGroup();
         }
 
         return id;
     }
 
     /**
      * Returns the variable value. By default, null is considered a valid value. In order to show an
      * error message (or throw an exception in head-less mode), use the {@link #getValue(boolean)}
      * method instead
      * 
      * @return The variable value
      */
     public T getValue()
     {
         return getValue(false);
     }
 
     /**
      * Returns the variable value (or fails if the variable is null).
      * 
      * @param forbidNull
      *            set to true to display an error message (or to throw an exception in head-less
      *            mode)
      * @return the variable value
      * @throws EzException
      *             if the variable value is null and forbidNull is true
      */
     public T getValue(boolean forbidNull) throws EzException
     {
         T val = variable.getValue();
         if (val == null && forbidNull) throw new EzException("Variable " + variable.getName() + " has not been set", true);
         return val;
     }
 
     public Var<T> getVariable()
     {
         return variable;
     }
 
     /**
      * Removes the given listener from the list
      * 
      * @param listener
      *            the listener to remove
      */
     public void removeVarChangeListener(EzVarListener<T> listener)
     {
         listeners.remove(listener);
     }
 
     /**
      * Removes all change listeners for this variable
      */
     public void removeAllVarChangeListeners()
     {
         variable.removeListeners();
     }
 
     /**
      * Replaces the list of values available in the combo box of this variable<br>
      * NOTE: this method has no effect if the user component is not already a combo box
      * 
      * @param values
      * @param defaultValueIndex
      * @param allowUserInput
      */
     public void setDefaultValues(T[] values, int defaultValueIndex, boolean allowUserInput)
     {
         if (getVarEditor() instanceof ComboBox)
         {
             ((ComboBox<T>) getVarEditor()).setDefaultValues(values, defaultValueIndex, allowUserInput);
         }
     }
 
     /**
      * Sets whether the input component is enabled or not in the interface
      * 
      * @param enabled
      *            the enabled state
      */
     public void setEnabled(boolean enabled)
     {
         jLabelName.setEnabled(enabled);
         getVarEditor().editorComponent.setEnabled(enabled);
     }
 
     /**
      * Sets the new value of this variable
      * 
      * @param value
      *            the new value
      * @throws UnsupportedOperationException
      *             thrown if changing the variable value from code is not supported (or not yet
      *             implemented)
      */
     public void setValue(final T value) throws UnsupportedOperationException
     {
         variable.setValue(value);
     }
 
     /**
      * Assigns a tool-tip text to the variable, which pops up when the user hovers the mouse on it.
      * 
      * @param text
      *            the text to display (usually no more than 20 words)
      */
     public void setToolTipText(String text)
     {
         jLabelName.setToolTipText(text);
         getVarEditor().editorComponent.setToolTipText(text);
     }
 
     /**
      * Sets the visibility state of this variable, and updates the chain of visibility states
      * (components hiding other components)
      * 
      * @param newVisibleState
      *            the new visibility state
      */
     public void setVisible(boolean newVisibleState)
     {
         super.setVisible(newVisibleState);
 
         updateVisibilityChain();
     }
 
     public String toString()
     {
         return variable.getName() + " = " + variable.toString();
     }
 
     protected void updateVisibilityChain()
     {
         Set<EzComponent> componentsToUpdate = visibilityTriggers.keySet();
 
         // first, hide everything in the chain
         for (EzComponent component : componentsToUpdate)
             component.setVisible(false);
 
         // if "this" is not visible, do anything else
         if (!this.isVisible()) return;
 
         // otherwise, one by one, show the components w.r.t. the triggers
         component: for (EzComponent component : componentsToUpdate)
         {
             T[] componentTriggerValues = visibilityTriggers.get(component);
 
             for (T triggerValue : componentTriggerValues)
             {
                if (this.getValue().equals(triggerValue))
                 {
                     // this call will be recursive in case of a EzVar object
                     component.setVisible(true);
                     continue component;
                 }
             }
         }
     }
 
     @Override
     public void variableChanged(Var<T> source, T oldValue, T newValue)
     {
         fireVariableChanged(newValue);
     }
 
     @Override
     public void referenceChanged(Var<T> source, Var<? extends T> oldReference, Var<? extends T> newReference)
     {
 
     }
 }
