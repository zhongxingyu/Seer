 /**
  * Copyright (c) 2011. Physion Consulting LLC
  * All rights reserved.
  */
 package com.physion.ebuilder;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.swing.BorderFactory;
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.FocusManager;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.Document;
 
 import com.lavantech.gui.comp.DateTimePicker;
 import com.lavantech.gui.comp.TimePanel;
 import com.physion.ebuilder.datamodel.DataModel;
 import com.physion.ebuilder.datamodel.RowData;
 import com.physion.ebuilder.datamodel.RowDataEvent;
 import com.physion.ebuilder.datamodel.RowDataListener;
 import com.physion.ebuilder.datatypes.Attribute;
 import com.physion.ebuilder.datatypes.Cardinality;
 import com.physion.ebuilder.datatypes.ClassDescription;
 import com.physion.ebuilder.datatypes.CollectionOperator;
 import com.physion.ebuilder.datatypes.Operator;
 import com.physion.ebuilder.datatypes.Type;
 
 
 /**
  * This class creates all the widgets that are used to display and
  * edit one row.
  *
  * We create all the widgets we will need in our constructor, and
  * thereafter we simply add or remove them from the JPanel based on
  * the RowData value for that row.
  *
  * Please note that I haved added the line:
  *
  *   @SuppressWarnings("unchecked")
  *
  * in order to stop IDEs complaining about the fact that I have
  * not specified a type for the items that are in a JComboBox or
  * its ComboBoxModel.
  *
  * TODO:  Possibly change code so the assorted "special" comboBoxes and
  * spinners are created lazily.  Not sure if that would actually save
  * much time.
  */
 class RowPanel
     extends JPanel
     implements ActionListener, DocumentListener, ChangeListener,
         RowDataListener {
 
 	/**
 	 * We never serialize this class, so this declaration is
 	 * just to stop the compiler warning.
 	 * You can suppress the warning instead if you want using
 	 * @SuppressWarnings("serial")
 	 */
 	private static final long serialVersionUID = 1L;
 
     /**
      * A comboBox dropdown list will dispaly at least this many
      * items before it starts to use a scrollbar.
      */
     private static final int MAX_ROWS_IN_COMBOBOX_DROPDOWN = 30;
 
     /**
      * Minimum width of a text field.  Please note, the actual
      * number of characters that will fit depend on the font
      * used and the characters in the field.  E.g. 'W' is wider
      * than 'l' in most fonts.
      */
     private static final int MIN_TEXT_COLUMNS = 8;
     private static final int MIN_SPINNER_COLUMNS = 8;
 
     /**
      * Inset in pixels between Components in a row.
      * We typically use this on the left side of a component.
      */
     private static final int INSET = 7;
     private static final Insets LEFT_INSETS = new Insets(0,INSET,0,0);
 
     /**
      * We set the background color of a RowPanel to this color.
      * When it contains an illegal value, we make the panel "opaque"
      * so the background color is shown.  If the row is legal, we
      * set opaque to false, so it is not drawn.
      *
      * Please note, I've just hardcoded a subtle color.
      * (The user will have to look at this often, so we don't
      * want a fire engine red.)
      * If you want to choose a different color, use one of
      * the many online color choosers such as:  www.colorpicker.com
      * We could get more clever and dynamically create the
      * color based on the default background color.
      */
     private static final Color ILLEGAL_BACKGROUND_COLOR =
         new Color(214, 203, 214);  // light gray-purple
         //new Color(224, 206, 224);  // light Easter egg purple
         //new Color(204, 143, 204);  // med purple
         //new Color(186, 222, 222);  // light blue
 
     private InvisibleButton deleteRowButton;
     private InvisibleButton createCompoundRowButton;
     private InvisibleButton createAttributeRowButton;
 
     /**
      * This holds the attribute JComboBoxes we use in this row.
      * Note that a user can, theoretically, have an infinitely
      * long path of attributes.  For example,
      *
      *      nextEpoch.prevEpoch.nextEpoch.prevEpoch...
      *
      * Because of this, we generate the JComboBoxes as we need them.
      *
      * Please note, the first comboBox in this array is also used
      * if this is a root row as to hold the Class Under Qualification
      * selection.
      */
     @SuppressWarnings("unchecked")
 	private ArrayList<JComboBox> comboBoxes = new ArrayList<JComboBox>();
 
     /**
      * This text field is used to enter the value of a "primitive"
      * attribute that is a string or a float.
      */
     private JTextField valueTextField;
 
     /**
      * This spinner is used to enter the value of a "primitive"
      * attribute that is an INT_16.
      *
      * Please note, although we create this JSpinner using
      * Short values passed to the constructor, the values
      * that a JSpinner returns are of type "Number".  I.e.
      * we are not going to get back Short values from it.
      */
     private JSpinner valueSpinnerInt16;
 
     /**
      * This spinner is used to enter the value of a "primitive"
      * attribute that is an INT_32.  It is also used for other
      * integer values in a row.
      */
     private JSpinner valueSpinnerInt32;
 
     /**
      * This spinner is used to enter a Count value.
      * It only holds numbers >= 0.
      */
     private JSpinner countSpinnerInt32;
 
     /**
      * This text field is used to enter the user created "key" for the
      * "properties" attribute.
      */
     private JTextField propNameTextField;
 
     /**
      * This comboBox allows the user to select what the type of the
      * "keyed" "properties" attribute will be.
      * E.g. int, string, boolean, time.
      */
     @SuppressWarnings("unchecked")
 	private JComboBox propTypeComboBox;
 
     /**
      * This comboBox will be used for an Attribute Row that
      * ends in a "primitive" value, and also for a row that
      * contains a "keyed" "properties" definition.
      * It will contain values like:  ==, !=, >, is null.
      */
     @SuppressWarnings("unchecked")
 	private JComboBox operatorComboBox;
 
     /**
      * This comboBox will be used to hold collection
      * operators:  Any, All, None, Count.
      */
     @SuppressWarnings("unchecked")
 	private JComboBox collectionOperatorComboBox;
 
     /**
      * This comboBox will be used for an Attribute Row
      * that ends with an Attribute that has a TO_MANY
      * relationship and whose collectionOperator is
      * set to Any/All/None, (but not Count).
      * This comboBox can contain only Any/All/None.
      * It cannot contain Count.
      */
     @SuppressWarnings("unchecked")
 	private JComboBox collectionOperator2ComboBox;
 
     /**
      * This is the widget that is displayed to let a user select
      * a date and time.
      */
     private DateTimePicker dateTimePicker;
 
     /**
      * This is simply a "spacer" that we put on the left side of
      * the row to indent the widgets to the right of it.
      * Currently, I am simply using a JLabel
      * filled with an adustible number of spaces to do this.
      * Simple, and it works.  There are many other ways to do
      * this though:  A JPanel with a minimum size, A JLabel or
      * JPanel that is empty, but has an EmptyBorder whose size
      * you change.
      */
     private JLabel indentWidget = new JLabel();
 
     /**
      * This holds all the buttons on the right side of the row.
      * I.e. the +/++/- buttons.
      */
     private JPanel buttonPanel;
 
     /**
      * This label never changes.
      */
     private final JLabel ofTheFollowingLabel = new JLabel(" of the following");
 
     /**
      * This label never changes.
      */
     private final JLabel haveLabel = new JLabel("have");
 
     /**
      * This is the RowData object that this RowPanel is displaying/editing.
      */
     private RowData rowData;
 
     /**
      * This flag is used to signal that I am programmatically making
      * changes to things and I do NOT want the GUI to notify anyone
      * or make change to the RowData object that this RowPanel is
      * manipulating.  For example, if the user makes a selection in
      * a comboBox that will require many changes to the associated
      * RowData object and GUI, we don't want that to happen until we
      * are done making all the necessary changes to the RowData object.
      * Please note, I think the code should be restructured
      * so this flag is not needed.
      */
     private boolean inProcess = false;
 
     /**
      * This index incremented as the assorted layout code in this
      * class lays out from left to right the components that this
      * RowPanel contains.
      */
     private int gridx;
 
     /**
      * This is set to true when we are doing our layout if there
      * is a widget that will use up the empty space on the right
      * side of a row.  If there isn't such a widget in the RowPanel,
      * then the panel holding the buttons will do that.
      */
     private boolean someWidgetFillingEmptySpace;
 
 
     /**
      * Create whatever components this renderer will need.
      * Other methods add or remove the components depending on
      * what RowData this row is displaying/editing.
      */
     @SuppressWarnings("unchecked")
 	public RowPanel(RowData rowData) {
 
         this.rowData = rowData;
 
         rowData.addRowDataListener(this);
 
         setBorder(BorderFactory.createEmptyBorder(4,10,4,10));
 
         /**
          * We will make this panel opaque, (showing this
          * background color), if this row contains an
          * illegal value.
          */
         setBackground(ILLEGAL_BACKGROUND_COLOR);
 
         GridBagLayout layout = new GridBagLayout();
         setLayout(layout);
 
         /**
          * Make sure the JLabels are not part of the focus
          * traversal.
          */
         indentWidget.setFocusable(false);
         ofTheFollowingLabel.setFocusable(false);
         haveLabel.setFocusable(false);
 
         deleteRowButton = new InvisibleButton("-");
         deleteRowButton.addActionListener(this);
         Util.setupAutoScrolling(deleteRowButton);
 
         createAttributeRowButton = new InvisibleButton("+");
         createAttributeRowButton.addActionListener(this);
         Util.setupAutoScrolling(createAttributeRowButton);
 
         createCompoundRowButton = new InvisibleButton("++");
         createCompoundRowButton.addActionListener(this);
         Util.setupAutoScrolling(createCompoundRowButton);
 
         valueTextField = new JTextField();
         valueTextField.setColumns(MIN_TEXT_COLUMNS);
         valueTextField.getDocument().addDocumentListener(this);
         Util.setupAutoScrolling(valueTextField);
 
         valueSpinnerInt16 = new JSpinner(new SpinnerNumberModel(
             0, Short.MIN_VALUE, Short.MAX_VALUE, 1));
         valueSpinnerInt16.addChangeListener(this);
         ((JSpinner.NumberEditor)valueSpinnerInt16.getEditor()).getTextField().
             setColumns(MIN_SPINNER_COLUMNS);
         Util.setupAutoScrolling(valueSpinnerInt16);
 
         valueSpinnerInt32 = new JSpinner(new SpinnerNumberModel(
             0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
         valueSpinnerInt32.addChangeListener(this);
         ((JSpinner.NumberEditor)valueSpinnerInt32.getEditor()).getTextField().
             setColumns(MIN_SPINNER_COLUMNS);
         Util.setupAutoScrolling(valueSpinnerInt32);
 
         /**
          * This spinner is used when we are setting the Count for
          * an attribute that has a to-many relationship.
          */
         countSpinnerInt32 = new JSpinner(new SpinnerNumberModel(
             0, 0, Integer.MAX_VALUE, 1));
         countSpinnerInt32.addChangeListener(this);
         ((JSpinner.NumberEditor)countSpinnerInt32.getEditor()).getTextField().
             setColumns(MIN_SPINNER_COLUMNS);
         Util.setupAutoScrolling(countSpinnerInt32);
 
         /**
          * This text field is used to display/edit the name of
          * a "keyed" property.
          */
         propNameTextField = new JTextField();
         propNameTextField.setColumns(MIN_TEXT_COLUMNS);
         propNameTextField.getDocument().addDocumentListener(this);
         Util.setupAutoScrolling(propNameTextField);
 
         dateTimePicker = new DateTimePicker();
         TimePanel timePanel = dateTimePicker.getTimePanel();
         timePanel.setSecDisplayed(false);
         dateTimePicker.addActionListener(this);
         Util.setupAutoScrolling(dateTimePicker);
 
         /**
          * Create the comboBox used to choose the type of a "keyed"
          * property.  For example, an attribute like one of these:
          *
          *      nextEpoch.properties.animalID(int) <= 123
          *      protocolParameters.stimulusName(string) == "caffeine"
          *
          * The model, (i.e. the selectable items), for this comboBox
          * never changes, so we can set the value of the model now.
          */
         propTypeComboBox = createComboBox(new DefaultComboBoxModel(
             DataModel.PROP_TYPES));
 
         /**
          * Create the comboBox used to choose the operator for
          * a "keyed" property value.  The operator changes depending
          * on the type of the property.  For example, an int/float
          * value has operators like ==,!=,<,>,<=,>=, while a string
          * value has those operators plus ~==, and ~~==.
          */
         operatorComboBox = createComboBox(null);
 
         /**
          * Create the first collection operator comboBox.
          * It can contain Any, All, None, Count.
          */
         collectionOperatorComboBox = createComboBox(new DefaultComboBoxModel(
             CollectionOperator.values()));
 
         /**
          * Create the second collection operator comboBox.
          * It can only contain Any, All, None, but not Count.
          */
         collectionOperator2ComboBox = createComboBox(new DefaultComboBoxModel(
             CollectionOperator.getCompoundCollectionOperators()));
 
         buttonPanel = new JPanel(new GridBagLayout());
         buttonPanel.setOpaque(false);
         GridBagConstraints gc;
         gc = new GridBagConstraints();
         gc.gridx = 0;
         gc.fill = GridBagConstraints.VERTICAL;
         //gc.insets = LEFT_INSETS;
         gc.insets = new Insets(0,INSET*3,0,0);
         buttonPanel.add(createAttributeRowButton, gc);
 
         gc = new GridBagConstraints();
         gc.gridx = 1;
         gc.fill = GridBagConstraints.VERTICAL;
         gc.insets = LEFT_INSETS;
         buttonPanel.add(createCompoundRowButton, gc);
 
         gc = new GridBagConstraints();
         gc.gridx = 2;
         gc.fill = GridBagConstraints.VERTICAL;
         gc.insets = LEFT_INSETS;
         buttonPanel.add(deleteRowButton, gc);
 
         initializeComponents();
     }
 
 
     /**
      * Get the RowData object that this RowPanel is displaying/editing.
      */
     RowData getRowData() {
         return(rowData);
     }
 
 
     /**
      * Get the comboBox at the specified index.  Create it if it
      * does not already exist.
      */
     @SuppressWarnings("unchecked")
 	private JComboBox getComboBox(int index) {
 
         while (index >= comboBoxes.size())
             comboBoxes.add(createComboBox(null));
 
         return(comboBoxes.get(index));
     }
 
 
     /**
      * Create a JComboBox that uses the passed in model.
      * Pass null if you just want a default model that you
      * later change to something else.
      */
     @SuppressWarnings("unchecked")
 	private JComboBox createComboBox(ComboBoxModel model) {
 
         JComboBox comboBox;
         if (model == null)
             comboBox = new JComboBox();
         else
             comboBox = new JComboBox(model);
 
         comboBox.setEditable(false);
         Util.setupAutoScrolling(comboBox);
 
         /**
          * Set the number of items that the dropdown will
          * display before it adds a scrollbar.
          */
         comboBox.setMaximumRowCount(MAX_ROWS_IN_COMBOBOX_DROPDOWN);
         comboBox.addActionListener(this);
 
         /**
          * Change default keyboard behavior of the comboBox so
          * it does NOT select the value when using the keyboard
          * to traverse values in the list.
          * Details about this behavior can be googled.
          * For example:  http://tinyurl.com/cbkeyboardtrav
          */
         comboBox.putClientProperty("JComboBox.isTableCellEditor",
                                    Boolean.TRUE);
         return(comboBox);
     }
 
 
     /**
      * All this method does is set a flag that can be used to
      * ignore GUI events while the GUI is being setup.
      * The initializeComponentsProtected() method, that this
      * method calls, does the real work.
      */
     private void initializeComponents() {
 
         if (inProcess)
             return;
 
         inProcess = true;
         initializeComponentsProtected();
         inProcess = false;
     }
 
 
     /**
      * This method lays out the components, (e.g. comboBoxes),
      * that will be needed to display this RowPanel's current
      * rowData value.
      * It also sets the data models the comboBoxes will use
      * and sets the selected value in the comboBoxes to the
      * corresponding values in this RowPanel's rowData value.
      */
     private void initializeComponentsProtected() {
 
         GridBagConstraints gc;
 
 
         /**
          * First save which component has the focus before we start
          * removing components from this RowPanel.
          */
         Component componentWithFocus = FocusManager.getCurrentManager().
             getFocusOwner();
 
         /**
          * Now remove ALL the components that are in this RowPanel.
          */
         removeAll();
 
         /**
          * Add back in the components that are needed to
          * display and edit our current rowData value.
          *
          * Start our "gridx" counter that is incremented every time
          * we add another widget to this row.  This counter is always
          * set to the gridx location of the next widget to be placed
          * in this RowPanel's GridBagLayout.
          *
          * The indentWidget we use to indent rows is always the first/leftmost
          * widget.
          */
         gridx = 0;
 
         /**
          * If a row is filled with widgets that do
          * not stretch, we need to have the cell that
          * holds the buttons on the right side of the
          * row use the empty space.
          *
          * This flag gets set to true if some other widget
          * uses the extra space.  E.g. the valueTextField
          * will use the extra space if it exists in this row.
          */
         someWidgetFillingEmptySpace = false;
 
         gc = new GridBagConstraints();
         gc.gridx = gridx++;
         add(indentWidget, gc);
         if (rowData != null)
             indentWidget.setText(rowData.getIndentString());
         else
             indentWidget.setText("");
 
         /**
          * Now add the components that are needed.
          */
 
         if (rowData.isRootRow()) {
             layoutRootRow();
         }
         else if (rowData.isSimpleCompoundRow()) {
             /**
              * A "simple" compound row is a row that
              * only contains a Collection Operator comboBox,
              * (i.e. Any/All/None), and the -, +, ++ buttons
              * on the right side.
              */
             layoutSimpleCompoundRow();
         }
         else {
             /**
              * An attribute row.  For example:
              *
              *      epochGroup.epochs.label == "Test 21"
              */
             layoutAttributeRow();
         }
 
         /**
          * Show/hide the +,++,- buttons.
          */
         layoutButtons();
 
         /**
          * Set the color of the row based on whether it
          * currently contains legal values.
          * Note that this also makes a call to repaint().
          */
         adjustBackgroundColor();
 
         /**
          * Set the focus back to the component that had it before
          * we removed all the components and put some components
          * back.
          *
          * If the same component that had the focus before is
          * no longer being used, (i.e. it is not in this RowPanel
          * any more), then set the focus to whatever the first
          * focusable component is in this RowPanel.
          */
         if (componentWithFocus != null) {
             if (componentWithFocus.getParent() != null)
                 componentWithFocus.requestFocusInWindow();
             else
                 setFocusToFirstFocusableComponent();
         }
     }
 
 
     /**
      * Set the model for the passed in comboBox to be the attributes
      * of the passed in classDescription.
      *
     * In addition, we will (optionally) prepend the special
      * Attribute.SELECT_ATTRIBUTE attribute, and we will append
     * the special Attribute.IS_NULL and Attribute.IS_NOT_NULL,
     * and we will append the special Attribute.MY_PROPERTY and
     * Attribute.ANY_PROPERTY.
     *
     * Any attributes that are of type Type.PER_USER_OR_CUSTOM_REFERENCE_OPERATOR will cause
     * two entries to be added to the comboBox model.  One entry
     * will be prepended with the string "My" and the other with
     * the string "All".  For example, if the attribute was called
     * "analysisRecords", instead of inserting an entry called
     * "analysisRecords" into the comboBox, we will insert two
     * entries:  "My analysisRecords" and "All analysisRecords".
      *
      * @param comboBox The JComboBox whose model and selectedItem
      * we will set.
      *
      * @param classDescription We will set the comboBox's model to
      * be the list of attributes of this ClassDescription.  (We also
      * might add a few more special values to the list.)
      *
      * @param appendNulls If this is true, we will append the
      * special Attribute.IS_NULL and IS_NOT_NULL to the end of the
      * list of the choices.
      *
      * @param selectedItem After setting the model, this method sets
      * the selected item to this value.  Pass null if you do not want to
      * set the selected item.
      */
     @SuppressWarnings("unchecked")
 	private void setComboBoxModel(JComboBox comboBox,
                                   ClassDescription classDescription,
                                   boolean appendNulls,
                                   Object selectedItem) {
 
         ArrayList<Attribute> attributes;
         if ((classDescription != null) &&
             (classDescription.getAllAttributes() != null)) {
             attributes = classDescription.getAllAttributes();
         }
         else {
             attributes = new ArrayList<Attribute>();
         }
 
         ArrayList<Attribute> copy = new ArrayList<Attribute>();
 
         /**
          * First, prepend the special "Select Attribute" attribute.
          */
         copy.add(Attribute.SELECT_ATTRIBUTE);
 
         /**
          * Go through the list of attributes adding them to our
          * copy of the ArrayList.
          */
         for (Attribute attribute : attributes) {
             copy.add(attribute);
         }
 
         if (appendNulls) {
             copy.add(Attribute.IS_NULL);
             copy.add(Attribute.IS_NOT_NULL);
         }
 
         /**
          * All the work with the list of Attributes is
          * finished, so create a DefaultComboBoxModel out of the list
          * Attributes and install it in the comboBox.
          */
         Attribute[] values;
         values = copy.toArray(new Attribute[0]);
         setComboBoxModel(comboBox, values, selectedItem);
     }
 
 
     /**
      * @param selectedItem After setting the model, this method sets
      * the selected item to this value.  Pass null if you do not want to
      * set the selected item.
      */
     @SuppressWarnings("unchecked")
 	private void setComboBoxModel(JComboBox comboBox, Object[] items,
                                   Object selectedItem) {
 
         DefaultComboBoxModel model = new DefaultComboBoxModel(items);
         comboBox.setModel(model);
         if (selectedItem != null)
             comboBox.setSelectedItem(selectedItem);
 
         if (((DefaultComboBoxModel)(comboBox.getModel())).
             getIndexOf(selectedItem) < 0) {
 
             String s = "Desired selectedItem not found in "+
                 "list.\nselectedItem = "+selectedItem;
             if (selectedItem instanceof Attribute) {
                 s += " ("+((Attribute)selectedItem).toStringDebug()+")";
             }
             s += "\nItems in list:";
             for (Object item : items) {
                 s += "\n  "+item;
                 if (item instanceof Attribute) {
                     s += " ("+((Attribute)item).toStringDebug()+")";
                 }
             }
             (new Exception(s)).printStackTrace();
         }
     }
 
 
     /**
      * This method is called when the RowData we are displaying
      * changes.  Make the row a different color when it contains
      * an illegal value.
      */
     @Override
     public void rowDataChanged(RowDataEvent event) {
 
         if (event.getTiming() == RowDataEvent.TIMING_AFTER) {
             adjustBackgroundColor();
         }
     }
 
 
     /**
      * Make the row a different color when it contains
      * an illegal value.
      */
     private void adjustBackgroundColor() {
 
         if (rowData.containsLegalValue()) {
             /**
              * Row is legal.  Don't draw background color.
              */
             setOpaque(false);
         }
         else {
             /**
              * Row is not legal.  Draw the background color, which
              * is a color different from the default JPanel background
              * color.
              */
             setOpaque(true);
         }
         repaint();
     }
 
 
     /**
      * This method is called when the user clicks on a +,++,- button
      * on the right side of a row, or when the user sets a time/date
      * value using the dateTimePicker.
      */
     @SuppressWarnings("unchecked")
 	@Override
     public void actionPerformed(ActionEvent e) {
 
         //System.out.println("Enter actionPerformed on: "+
         //                   e.getSource().getClass());
 
         if (e.getSource() == createCompoundRowButton) {
             rowData.createCompoundRow();
         }
         else if (e.getSource() == createAttributeRowButton) {
             rowData.createAttributeRow();
         }
         else if (e.getSource() == deleteRowButton) {
             rowData.removeFromParent();
         }
         else if (e.getSource() instanceof DateTimePicker) {
             dateTimeChanged();
         }
         else if (e.getSource() instanceof JComboBox) {
             comboBoxChanged((JComboBox)e.getSource());
         }
         else {
             System.err.println("ERROR: RowPanel.actionPerformed() does not "+
                 "handle events from this widget.  event = "+e);
         }
     }
 
 
     /**
      * This method is called when the user picks a new date or time
      * value in the dateTimePicker.
      */
     private void dateTimeChanged() {
 
         /**
          * If we are in the process of updating the RowData due
          * to a programmatic change, ignore this.
          */
         if (inProcess)
             return;
 
         Date date = dateTimePicker.getDate();
         rowData.setAttributeValue(date);
     }
 
 
     /**
      * This method is called when the user changes the selected
      * item in a comboBox.  For example, the user changes the
      * selected attribute or collection operator.
      */
     @SuppressWarnings("unchecked")
 	private void comboBoxChanged(JComboBox comboBox) {
 
         /**
          * If we are in the process of updating the RowData due
          * to a comboBox change, ignore this change.
          */
         if (inProcess)
             return;
 
         /**
          * Change the appropriate value in this row's RowData
          * object.
          */
 
         if (rowData.isRootRow()) {
             /**
              * The root row is being changed.
              */
             handleRootRowChange(comboBox);
         }
         else {
             /**
              * User is editing a row other than the first row.
              */
             handleChildRowChange(comboBox);
         }
     }
 
 
     /**
      * This method is called when the user changes a comboBox
      * in a row other than the first row.  I.e. the user is
      * changing a comboBox in a row that is NOT the root row.
      */
     @SuppressWarnings("unchecked")
 	private void handleChildRowChange(JComboBox comboBox) {
 
         /**
          * First, save the index of the comboBox that
          * currently has the focus.  I.e. the comboBox
          * in which the user just selected something.
          * By "index" in this case, I don't mean the
          * index of the comboBox in the comboBoxes
          * ArrayList.  I mean the index of the comboBox
          * in the parent Container's list of Components.
          * (Note, the parent Container is this RowPanel.)
          */
         int focusedComboBoxIndex = -1;
         Container parent = comboBox.getParent();
         for (int index = 0; index < parent.getComponentCount(); index++) {
 
             //System.out.println("parent.getComponent("+index+") = "+
             //    parent.getComponent(index).getClass());
             if (comboBox == parent.getComponent(index)) {
                 focusedComboBoxIndex = index;
                 break;
             }
         }
 
         /**
          * Now, depending upon which comboBox was changed,
          * set the appropriate value in the RowData object
          * this RowPanel is displaying/editing.
          */
 
         Object selectedItem = comboBox.getSelectedItem();
         if (comboBox == propTypeComboBox) {
             /**
              * User has changed the type of a "keyed" "properties"
              * attribute in a "My/Any Property" row.
              */
             rowData.setPropType((Type)selectedItem);
         }
         else if (comboBox == operatorComboBox) {
             /**
              * User has changed the operator for a "keyed" property.
              */
             rowData.setAttributeOperator((Operator)selectedItem);
         }
         else if (comboBox == collectionOperatorComboBox) {
             /**
              * User has changed the first collection operator
              * of a row that ends with an Attribute that has
              * a TO_MANY relationship.
              *
              * For example, the change the value of the "Any"
              * comboBox in the example below:
              *
              *      epochGroup.epochs Any have All of the following
              */
             rowData.setCollectionOperator((CollectionOperator)selectedItem);
         }
         else if (comboBox == collectionOperator2ComboBox) {
             if (rowData.isSimpleCompoundRow()) {
                 /**
                  * User has changed the one and only collection operator
                  * comboBox in this row.
                  *
                  * For example, the change the value of the "All"
                  * comboBox in the example below:
                  *
                  *      Any of the following
                  *
                  * So, set the collection operator for this row.
                  */
                 rowData.setCollectionOperator((CollectionOperator)selectedItem);
             }
             else {
                 /**
                  * User has changed the second collection operator
                  * of a row that ends with an Attribute that has
                  * a TO_MANY relationship.  (And the first collection
                  * operator was not set to Count.)
                  *
                  * For example, the change the value of the "All"
                  * comboBox in the example below:
                  *
                  *      epochGroup.epochs Any have All of the following
                  */
                 rowData.setCollectionOperator2((CollectionOperator)
                     selectedItem);
             }
         }
         else if (selectedItem instanceof Attribute) {
             /**
              * One of the comboBoxes in our list of comboBoxes we
              * use for attributes, the attribute operator, or
              * collection operators was changed.
              *
              * User selected an Attribute in a comboBox dropdown.
              * Set the values "to the right" of the comboBox to
              * appropriate values.
              */
             handleAttributeSelected(comboBox, (Attribute)selectedItem);
         }
 
         /*
         System.out.println("rowData's new value: "+
                            rowData.getRowString(false, ""));
         System.out.println("Debug Version: "+rowData.getRowString());
         */
 
         initializeComponents();
 
         /**
          * Move the focus to the component to the right of the
          * comboBox in which the user just selected a value.
          *
          * TODO:  Ask Physion if they want this feature.
          * People that use the keyboard alot might like it.
          * It shouldn't bother people who use the mouse.
          */
 
         /**
          * Get the list of components we contain.
          * (Note, the parent Container is this RowPanel in
          * current implementation.)
          */
         //Component[] components = parent.getComponents();
         Component[] components = getComponents();
 
         /**
          * A quick review of the components in this RowPanel:
          *
          * The first component (at index 0) is the indentWidget.
          * Next component will be a comboBox.
          * The components after the comboBox will be more comboBoxes,
          * text fields, etc.
          * The last component we contain is the JPanel that holds
          * the +/++/- buttons.
          *
          * So, make sure there is a component after the one that
          * had the focus, but we don't want to move the focus to
          * the JPanel that holds the buttons.
          */
         if ((focusedComboBoxIndex + 1) < (components.length - 1))
             focusedComboBoxIndex++;
 
         for (int index = focusedComboBoxIndex;
              index < components.length; index++) {
 
             Component component = components[index];
             if (component.isFocusable()) {
 
                 if (!(component == buttonPanel)) {
                     /**
                      * Set the focus to "component".
                      * Note we have to handle the DateTimePicker
                      * specially to get the focus into one of its
                      * sub-components.
                      */
                     if (component == dateTimePicker) {
                         dateTimePicker.getRenderer().requestFocusInWindow();
                     }
                     else {
                         /**
                          * It is an ordinary Component, so we
                          * can simply call requestFocusInWindow()
                          * to set the focus to it.
                          */
                         component.requestFocusInWindow();
                     }
                 }
                 else {
                     /**
                      * The next focusable component is the JPanel we use
                      * to hold the +/++/- buttons.  So we've come to the
                      * end of the editable widgets in this row, so don't
                      * automatically progress the focus any further.
                      */
                  }
                 break;
             }
         }
     }
 
 
     /**
      * This method is called when the user selected an
      * Attribute in a comboBox dropdown.
      * Figure out which comboBox was changed and then
      * set the corresponding Attribute in the row's
      * attributePath.
      *
      * @param comboBox The comboBox in which the Attribute was selected.
      *
      * @param selectedAttribute The selected Attribute.
      */
     @SuppressWarnings("unchecked")
 	private void handleAttributeSelected(JComboBox comboBox,
                                          Attribute selectedAttribute) {
 
         int comboBoxIndex = comboBoxes.indexOf(comboBox);
         if (comboBoxIndex < 0) {
             System.err.println("ERROR:  In comboBoxChanged.  "+
                 "comboBoxIndex = "+comboBoxIndex+
                 ".  This should never happen.");
         }
 
         rowData.setAttribute(comboBoxIndex, selectedAttribute);
     }
 
 
     /**
      * This is called when the first/topmost row is being edited.
      * So we need to adjust the value of the "root" row, (also known
      * as the Class Under Qualification.)
      *
      * The first row has only two comboBoxes.  The first comboBox,
      * which is the first comboBox in our comboBox array,
      * is used to select the Class Under Qualification.
      *
      * The second comboBox, which is the collectionOperator2ComboBox
      * that is set up to only hold Any/All/None, is used to select
      * the Collection Operator.
      */
     @SuppressWarnings("unchecked")
 	private void handleRootRowChange(JComboBox comboBox) {
 
         if (comboBox == comboBoxes.get(0)) {
             /**
              * User is changing the value of the Class Under Qualification.
              */
             ClassDescription classDescription =
                 (ClassDescription)comboBox.getSelectedItem();
             //System.out.println("classDescription: "+classDescription);
             if (classDescription != null) {
                 /**
                  * The user changed the Class Under Qualification,
                  * so set the new value.
                  */
                 rowData.setClassUnderQualification(classDescription);
             }
         }
         else if (comboBox == collectionOperator2ComboBox) {
             /**
              * User is changing the value of the Collection Operator.
              */
             CollectionOperator collectionOperator =
                 (CollectionOperator)comboBox.getSelectedItem();
             if (collectionOperator != null) {
                 rowData.setCollectionOperator(collectionOperator);
             }
         }
         else {
             /**
              * This should never happen.
              */
         }
     }
 
 
     /**
      * Arrange the buttons +/++/- buttons that are in this row.
      * Note that those three buttons are in their own JPanel
      * called buttonPanel.
      */
     private void layoutButtons() {
 
         /**
          * The very first row cannot be deleted.
          * All other rows can always be deleted.
          */
         if (rowData.isRootRow()) {
             deleteRowButton.setDraw(false);
         }
         else {
             deleteRowButton.setDraw(true);
         }
 
         /**
          * See if this row can have child rows.
          * Based on that, show/hide the +, ++ buttons.
          */
         if ((rowData != null) && rowData.isCompoundRow()) {
             createCompoundRowButton.setDraw(true);
             createAttributeRowButton.setDraw(true);
         }
         else {
             createCompoundRowButton.setDraw(false);
             createAttributeRowButton.setDraw(false);
         }
 
         /**
          * Add the panel that holds the -/+/++ buttons to
          * the far right side of this row.
          * If there is no other widget that will fill
          * the extra space in the row, tell the GridBagLayout
          * manager that the buttonPanel will do it.
          */
         GridBagConstraints gc = new GridBagConstraints();
         gc.gridx = gridx++;
         gc.anchor = GridBagConstraints.EAST;
         if (someWidgetFillingEmptySpace == false)
             gc.weightx = 1;
         add(buttonPanel, gc);
     }
 
 
     /**
      * Layout, and set the values of, the components in the first
      * row.  I.e. the "root" row of the expression tree.
      *
      * The first/topmost row always has the
      * Class Under Qualification comboBox and the
      * Collection Operator comboBox, (and only those comboBoxes).
      *
      * The leftmost comboBox contains the list of possible choices
      * for the Class Under Qualification.  The comboBox on the
      * right contains the Any/All/None CollectionOperator.
      */
     private void layoutRootRow() {
 
         GridBagConstraints gc;
 
         gc = new GridBagConstraints();
         gc.gridx = gridx++;
         gc.insets = LEFT_INSETS;
         add(getComboBox(0), gc);
 
         gc = new GridBagConstraints();
         gc.gridx = gridx++;
         gc.insets = LEFT_INSETS;
         add(collectionOperator2ComboBox, gc);
 
         gc = new GridBagConstraints();
         gc.gridx = gridx++;
         gc.weightx = 1;
         someWidgetFillingEmptySpace = true;
         gc.anchor = GridBagConstraints.WEST;
         add(ofTheFollowingLabel, gc);
 
         /**
          * Get the list of possible classes that can be used
          * as a "Class Under Qualification" and set that list
          * as the first comboBox's model, and set the currently
          * selected value to be this row's value.
          */
 
         ClassDescription[] values =
             DataModel.getPossibleCUQs().
             toArray(new ClassDescription[0]);
 
         setComboBoxModel(getComboBox(0), values,
                          rowData.getClassUnderQualification());
 
         /**
          * Now set the selected value of the Collection Operator combobox.
          * Note that we are using the collectionOperator2ComboBox because
          * it has Any/All/None (not Count) as selections.
          */
         collectionOperator2ComboBox.setSelectedItem(
             rowData.getRootRow().getCollectionOperator());
     }
 
 
     /**
      * A "simple" compound row is a row that
      * only contains a Collection Operator comboBox,
      * and the -, +, ++ buttons on the right side.
      */
     private void layoutSimpleCompoundRow() {
 
         GridBagConstraints gc;
 
 
         gc = new GridBagConstraints();
         gc.gridx = gridx++;
         gc.insets = LEFT_INSETS;
         add(collectionOperator2ComboBox, gc);
 
         gc = new GridBagConstraints();
         gc.gridx = gridx++;
         gc.weightx = 1;
         someWidgetFillingEmptySpace = true;
         gc.anchor = GridBagConstraints.WEST;
         add(ofTheFollowingLabel, gc);
 
         /**
          * This is a "simple" Compound Row.  I.e. it only has
          * the Collection Operator comboBox in it.
          * Set the comboBox model.
          */
         setComboBoxModel(collectionOperator2ComboBox,
                          CollectionOperator.getCompoundCollectionOperators(),
                          rowData.getCollectionOperator());
     }
 
 
     /**
      * This is an Attribute Row that contains one
      * or more comboBoxes for selecting attributes,
      * possibly a Collection Operator comboBox,
      * possibly a true/false comboBox, possibly
      * an Attribute Operator (==, !=, <, >, ...) comboBox,
      * or any number of other widgets.  It also contains
      * the +, ++, - buttons.
      *
      * A row like one of these, for example:
      *
      *      protocolID == "Test 27"
      *      My Property.animalName string == "Spot"
      *      epochGroup.source.My Property.animalID string == X123
      *      protocolParameters.stimulusFrequency int == 5
      *
      * TODO: Perhaps we want to break this long method up into
      * multiple methods where each method handles one "type" of
      * attribute row?  I.e. make each if/else block that is
      * part of the if-statement that begins with:
      *
      *   if (childmostAttribute.isPrimitive()) {
      *
      * into a separate method.  It won't reduce the amount
      * of code, but might make it a bit easier to understand.
      */
     @SuppressWarnings("unchecked")
 	private void layoutAttributeRow() {
 
         GridBagConstraints gc;
 
         //System.out.println("Add comboBoxes for: "+rowData.getRowString());
 
         /**
          * We are an Attribute Row, so the widgets we contain
          * are based on the values in this row's RowData object.
          *
          * Add one comboBox for every Attribute on this row's
          * attributePath.  For example, this row would
          * cause this loop to add two comboBoxes:
          *
          *      epochGroup.source isNull
          *
          * The leftmost comboBox holds "epochGroup" and the next
          * one holds "source".
          */
         int index;
         for (index = 0; index < rowData.getAttributeCount(); index++) {
 
             if (Attribute.IS_NULL.equals(rowData.getAttribute(index)) ||
                 Attribute.IS_NOT_NULL.equals(rowData.getAttribute(index))) {
                 /**
                  * We have hit a special Attribute.IS_NULL or
                  * Attribute.IS_NOT_NULL that is not really an Attribute.
                  * So, ignore it.  (No other Attributes should be after
                  * this one, so we can just break out of this for-loop.)
                  */
                 break;
             }
 
             //System.out.println("Adding comboBox at gridx "+gridx);
             gc = new GridBagConstraints();
             gc.gridx = gridx++;
             gc.insets = LEFT_INSETS;
             add(getComboBox(index), gc);
 
             if (index == 0) {
                 /**
                  * Set the model and selected item of the leftmost
                  * comboBox.  The leftmost comboBox is filled with
                  * the attributes of the parentClass.  I.e. the
                  * class of its parent row.
                  * Also set the selected item in the comboBox.
                  */
                 ClassDescription parentClass = rowData.getParentClass();
                 if (parentClass == null) {
                     System.err.println("ERROR: layoutAttributeRow encountered "+
                         "parentClass == null.\n"+
                         "This should never happen.");
                     return;
                 }
                 setComboBoxModel(getComboBox(index), parentClass,
                                  false, rowData.getAttribute(index));
             }
             else {
 
                 /**
                  * This is NOT the leftmost comboBox.
                  * Each comboBox is filled with the attributes of
                  * the class of the comboBox to its left.
                  * Also set the selected item in the comboBox.
                  */
                 Attribute att = rowData.getAttribute(index-1);
                 setComboBoxModel(getComboBox(index),
                                  att.getClassDescription(), true,
                                  rowData.getAttribute(index));
             }
         }
 
         /**
          * We have inserted comboBoxes for every Attribute on this
          * RowData's attributePath.  Now insert any other widgets
          * that are needed based on what the childmost (i.e. rightmost)
          * attribute is in this row.
          */
 
         /**
          * Get the childmost/rightmost Attribute.
          * If the childmost Attribute is the "special" Attribute.IS_NULL
          * or Attribute.IS_NOT_NULL, get the Attribute to the left
          * of that special Attribute.
          */
         Attribute childmostAttribute = rowData.getChildmostAttribute();
         if (Attribute.IS_NULL.equals(childmostAttribute) ||
             Attribute.IS_NOT_NULL.equals(childmostAttribute)) {
 
             int indexToLeft = rowData.getAttributeCount()-2;
             if (indexToLeft >= 0) {
                 childmostAttribute = rowData.getAttribute(indexToLeft);
             }
             else {
                 /**
                  * This should never happen.
                  */
             }
         }
 
         //System.out.println("childmostAttribute = "+childmostAttribute);
         if (childmostAttribute == null) {
             System.err.println("ERROR: childmostAttribute == null\n"+
                 "This probably means the rowData for this row was not\n"+
                 "properly set up or initialized.  If this is the very\n"+
                 "first row in the tree, you probably should be calling\n"+
                 "RowData.createRootRow() to create it.");
             return;
         }
 
         if (childmostAttribute.isPrimitive()) {
 
             /**
              * The rightmost Attribute is a primitive Attribute
              * such as an int, float, string, date/time, so now
              * place the comboBox that will hold operators such
              * as ==, !=, >, is true.
              */
             gc = new GridBagConstraints();
             gc.gridx = gridx++;
             gc.insets = LEFT_INSETS;
             add(operatorComboBox, gc);
 
             /**
              * Set the comboBox model to hold operators appropriate
              * for the Type (int, string, float, boolean) of the
              * Attribute.
              */
             setOperatorComboBoxModel(childmostAttribute.getType());
 
             /**
              * Now add the widget the user can use to edit the
              * value.  E.g. a text field, integer spinner, or
              * a time/date picker.
              */
 
             if ((childmostAttribute.getType() == Type.DATE_TIME) &&
                 (rowData.getAttributeOperator() != Operator.IS_NULL) &&
                 (rowData.getAttributeOperator() != Operator.IS_NOT_NULL)) {
                 /**
                  * We only display the dateTimePicker if the
                  * operator is not "is null" or "is not null".
                  */
                 gc = new GridBagConstraints();
                 gc.gridx = gridx++;
                 gc.fill = GridBagConstraints.BOTH;
                 gc.insets = LEFT_INSETS;
                 add(dateTimePicker, gc);
             }
             else if (childmostAttribute.getType() == Type.INT_16) {
                 gc = new GridBagConstraints();
                 gc.gridx = gridx++;
                 gc.weightx = 0.1;
                 gc.fill = GridBagConstraints.BOTH;
                 gc.insets = LEFT_INSETS;
                 add(valueSpinnerInt16, gc);
             }
             else if (childmostAttribute.getType() == Type.INT_32) {
                 gc = new GridBagConstraints();
                 gc.gridx = gridx++;
                 gc.weightx = 0.1;
                 gc.fill = GridBagConstraints.BOTH;
                 gc.insets = LEFT_INSETS;
                 add(valueSpinnerInt32, gc);
             }
             else if ((childmostAttribute.getType() == Type.UTF_8_STRING) ||
                      (childmostAttribute.getType() == Type.FLOAT_64)) {
                 /**
                  * Place a text field into which the user can enter an
                  * attribute value using a string.
                  */
                 gc = new GridBagConstraints();
                 gc.gridx = gridx++;
                 gc.weightx = 1;
                 someWidgetFillingEmptySpace = true;
                 gc.fill = GridBagConstraints.BOTH;
                 gc.insets = LEFT_INSETS;
                 add(valueTextField, gc);
             }
             else if (childmostAttribute.getType() == Type.BOOLEAN) {
                 /**
                  * There is no "value" field to add because the
                  * operator comboBox values of "is true" and "is false"
                  * specify all that needs to be specified.
                  */
             }
 
             /**
              * Set the selected operator and (probably) the
              * attribute value also.
              */
 
             if (childmostAttribute.getType() == Type.BOOLEAN) {
                 operatorComboBox.setSelectedItem(
                     rowData.getAttributeOperator());
             }
             else if (childmostAttribute.getType() == Type.DATE_TIME) {
 
                 operatorComboBox.setSelectedItem(
                     rowData.getAttributeOperator());
                 if ((rowData.getAttributeOperator() != Operator.IS_NULL) &&
                     (rowData.getAttributeOperator() != Operator.IS_NOT_NULL)) {
                     dateTimePicker.setDate((Date)rowData.getAttributeValue());
                 }
             }
             else if (childmostAttribute.getType() == Type.INT_16) {
                 operatorComboBox.setSelectedItem(
                     rowData.getAttributeOperator());
                 Object attributeValue = rowData.getAttributeValue();
                 if ((attributeValue == null) ||
                     attributeValue.toString().isEmpty()) {
                     attributeValue = new Integer((short)0);
                 }
                 else {
                     attributeValue = new Integer(Short.parseShort(
                         attributeValue.toString()));
                 }
                 valueSpinnerInt16.setValue(attributeValue);
             }
             else if (childmostAttribute.getType() == Type.INT_32) {
                 operatorComboBox.setSelectedItem(
                     rowData.getAttributeOperator());
                 Object attributeValue = rowData.getAttributeValue();
                 if ((attributeValue == null) ||
                     attributeValue.toString().isEmpty()) {
                     attributeValue = new Integer(0);
                 }
                 else {
                     attributeValue = new Integer(attributeValue.toString());
                 }
                 valueSpinnerInt32.setValue(attributeValue);
             }
             else {
                 operatorComboBox.setSelectedItem(
                     rowData.getAttributeOperator());
                 Object attributeValue = rowData.getAttributeValue();
                 if (attributeValue == null)
                     attributeValue = "";
                 valueTextField.setText(attributeValue.toString());
             }
         }
         else if ((childmostAttribute.getType() ==
                   Type.PER_USER_PARAMETERS_MAP) ||
                  (childmostAttribute.getType() == Type.PARAMETERS_MAP)) {
 
             /**
              * The rightmost attribute is either "My Property" or
              * "Any Property", or is of Type.PARAMETERS_MAP,
              * so add the widgets that are to the right of that
              * attribute.  For example, for a row that looks like this:
              *
              *      epochGroup.source.My Property.animalID string == X123
              *
              * or this:
              *
              *      protocolParameters.stimulusFrequency int == 5
              *
              * For the first example, we would need to add a text
              * field where the user can enter "animalID",
              * a comboBox where the user can select the type of the value
              * (e.g. int, string, float, boolean),
              * a comboBox to select the operator (e.g. ==, !=, >),
              * and a text field where the user can enter a value.
              * Note that for boolean types, we would not have a
              * value text field, but instead the operator comboBox would
              * let the user choose, "is true" or "is false".
              */
 
             /**
              * Add the propNameTextField where the user can enter
              * the "keyed" property name.  "animalID" in the example
              * in the comments above.
              */
             gc = new GridBagConstraints();
             gc.gridx = gridx++;
             gc.weightx = 1;
             gc.fill = GridBagConstraints.BOTH;
             someWidgetFillingEmptySpace = true;
             gc.insets = LEFT_INSETS;
             add(propNameTextField, gc);
 
             /**
              * Add the propTypeComboBox where the user can select the
              * the type of the "keyed" property.  "string" or "int" in
              * the example in the comments above.
              */
             gc = new GridBagConstraints();
             gc.gridx = gridx++;
             gc.insets = LEFT_INSETS;
             add(propTypeComboBox, gc);
 
             /**
              * Add the attributeOperatorComboBox where the user can
              * select the operator for the "keyed" property.  "==" in the
              * example in the comments above.
              *
              * Later, other code will set the model of this comboBox
              * depending on the selected value in the propTypeComboBox.
              */
             gc = new GridBagConstraints();
             gc.gridx = gridx++;
             gc.insets = LEFT_INSETS;
             add(operatorComboBox, gc);
 
             /**
              * Set the model of the operatorComboBox depending on the
              * selected value in the propTypeComboBox.
              */
             setOperatorComboBoxModel(rowData.getPropType());
 
             if (rowData.getPropType() == Type.DATE_TIME) {
 
                 /**
                  * Add the dateTimePicker where the user can enter the
                  * the value of the "keyed" property.   But, only if
                  * the operator is not "is null" and "is not null".
                  */
                 if ((rowData.getAttributeOperator() != Operator.IS_NULL) &&
                     (rowData.getAttributeOperator() != Operator.IS_NOT_NULL)) {
 
                     gc = new GridBagConstraints();
                     gc.gridx = gridx++;
                     gc.fill = GridBagConstraints.BOTH;
                     gc.insets = LEFT_INSETS;
                     add(dateTimePicker, gc);
                 }
             }
             else if (rowData.getPropType() == Type.INT_32) {
                 gc = new GridBagConstraints();
                 gc.gridx = gridx++;
                 gc.weightx = 0.1;
                 gc.fill = GridBagConstraints.BOTH;
                 gc.insets = LEFT_INSETS;
                 add(valueSpinnerInt32, gc);
             }
             else if (rowData.getPropType() != Type.BOOLEAN) {
 
                 /**
                  * Add the valueTextField where the user can enter the
                  * the value of the "keyed" property.  "x123" in the
                  * example in the comments above.
                  */
                 gc = new GridBagConstraints();
                 gc.gridx = gridx++;
                 gc.weightx = 1;
                 gc.fill = GridBagConstraints.BOTH;
                 gc.insets = LEFT_INSETS;
                 add(valueTextField, gc);
             }
 
             /**
              * Set the property name, type, and operator.
              */
             propNameTextField.setText(rowData.getPropName());
             propTypeComboBox.setSelectedItem(rowData.getPropType());
             operatorComboBox.setSelectedItem(rowData.getAttributeOperator());
 
             /**
              * Now based on the type, set the value of the
              * attribute.
              */
             if (rowData.getPropType() == Type.INT_16) {
                 short value = 0;
                 if ((rowData.getAttributeValue() != null) &&
                     (rowData.getAttributeValue() instanceof Number)) {
                     value = ((Number)rowData.getAttributeValue()).shortValue();
                 }
 
                 valueSpinnerInt16.setValue(value);
             }
             else if (rowData.getPropType() == Type.INT_32) {
                 int value = 0;
                 if ((rowData.getAttributeValue() != null) &&
                     (rowData.getAttributeValue() instanceof Number)) {
                     value = ((Number)rowData.getAttributeValue()).intValue();
                 }
 
                 valueSpinnerInt32.setValue(value);
             }
             else if ((rowData.getPropType() == Type.FLOAT_64) ||
                      (rowData.getPropType() == Type.UTF_8_STRING)) {
 
                 if (rowData.getAttributeValue() != null)
                     valueTextField.setText(
                         rowData.getAttributeValue().toString());
                 else
                     valueTextField.setText("");
             }
             else if (rowData.getPropType() == Type.DATE_TIME) {
                 /**
                  * The property is a date/time type, so set
                  * the value of the dateTimePicker if the
                  * operator is not "is null" and "is not null".
                  */
                 if ((rowData.getAttributeOperator() != Operator.IS_NULL) &&
                     (rowData.getAttributeOperator() != Operator.IS_NOT_NULL)) {
 
                     if (rowData.getAttributeValue() instanceof Date) {
                         dateTimePicker.setDate(
                             (Date)rowData.getAttributeValue());
                     }
                     else {
                         System.err.println("attributeValue not an instance "+
                             "of a Date.  This should never happen.");
                     }
                 }
             }
             else if (rowData.getPropType() == Type.BOOLEAN) {
                 /**
                  * No valueTextField is displayed in this case because
                  * the operatorComboBox serves that function.
                  * I.e.  "is true" and "is false" is both an operator
                  * and a "value".
                  */
             }
         }
         else if (rowData.getCollectionOperator() != null) {
 
             /**
              * Add comboBox for the Collection Operator which
              * will display the value Any, All, None, or Count.
              */
             gc = new GridBagConstraints();
             gc.gridx = gridx++;
             gc.insets = LEFT_INSETS;
             add(collectionOperatorComboBox, gc);
 
             if (rowData.getCollectionOperator() == CollectionOperator.COUNT) {
 
                 /**
                  * This row says something like:
                  *
                  *      epochGroups.epochs Count == 5
                  */
 
                 /**
                  * Add comboBox for the Attribute Operator which
                  * will display a value like ==,!=, <, etc.
                  */
                 gc = new GridBagConstraints();
                 gc.gridx = gridx++;
                 gc.insets = LEFT_INSETS;
                 add(operatorComboBox, gc);
 
                 /**
                  * Add count spinner.
                  */
                 gc = new GridBagConstraints();
                 gc.gridx = gridx++;
                 gc.fill = GridBagConstraints.BOTH;
                 gc.insets = LEFT_INSETS;
                 add(countSpinnerInt32, gc);
             }
             else {
                 /**
                  * This row has a collection operator set to
                  * Any, All, or None.  (But not Count.)
                  *
                  * This row says something like:
                  *
                  *      epochGroups.epochs Any have All of the following
                  */
                 if (rowData.getCollectionOperator2() != null) {
 
                     /**
                      * Add the "have" label.
                      */
                     gc = new GridBagConstraints();
                     gc.gridx = gridx++;
                     gc.insets = LEFT_INSETS;
                     add(haveLabel, gc);
 
                     /**
                      * Add comboBox for the second Collection Operator.
                      */
                     gc = new GridBagConstraints();
                     gc.gridx = gridx++;
                     gc.insets = LEFT_INSETS;
                     add(collectionOperator2ComboBox, gc);
                 }
                 else {
                     /**
                      * This should never happen.
                      */
                 }
             }
         }
         else {
             /**
              * The last Attribute on the right is a primitive
              * or it is the special "Select Attribute" Attribute.
              * So we don't need any more comboBoxes to the right
              * of the last one in this row.
              */
         }
 
         /**
          * Add the "of the following" label to the end
          * of the row if this is a compound row.
          * (I.e. the row ends with Any, All, or None.)
          */
         if (rowData.isCompoundRow()) {
 
             gc = new GridBagConstraints();
             gc.gridx = gridx++;
             gc.weightx = 1;
             someWidgetFillingEmptySpace = true;
             gc.anchor = GridBagConstraints.WEST;
             add(ofTheFollowingLabel, gc);
         }
 
         /**
          * By this point, all the models and values of the comboBoxes
          * that correspond to Attributes on this RowData's attributePath
          * have been set if the row does not end in an Attribute
          * that is of a TO_MANY relationship.  E.g. if the row does not
          * look like one of these examples:
          *
          *      epochGroup.epochs Count == 5
          *      epochGroup.epochs All have Any of the following
          *
          * If it does end in a TO_MANY relationship, initialize
          * the last one or two comboBox and value(s).
          */
         childmostAttribute = rowData.getChildmostAttribute();
         if (childmostAttribute.getCardinality() == Cardinality.TO_MANY) {
 
             /**
              * The item selected in the "childmost" (i.e. last)
              * Attribute in this RowData's attributePath is an
              * Attribute that has a to-many relationship with the
              * class that contains it.  For example, a row like
              * one of these:
              *
              *      epochGroup.epochs Count == 5
              *      resources Count == 27
              *      resources Any
              *      resources None
              *
              * Set the value of the Collection Operator comboBox
              * to be this row's value.
              */
             collectionOperatorComboBox.setSelectedItem(
                 rowData.getCollectionOperator());
 
             /**
              * Set the value of the operatorComboBox and spinner if the
              * collection operator is currently set to Count.
              */
             if (rowData.getCollectionOperator() == CollectionOperator.COUNT) {
 
                 /**
                  * This row is something like:
                  *
                  *      epochGroup.epochs Count == 5
                  *
                  * Set the operator that is used for the Count.
                  * E.g. ==, >, <=
                  */
                 operatorComboBox.setModel(new DefaultComboBoxModel(
                     Operator.OPERATORS_ARITHMATIC));
                 operatorComboBox.setSelectedItem(
                     rowData.getAttributeOperator());
 
                 /**
                  * Set the value in the spinner.
                  */
                 Object attributeValue = rowData.getAttributeValue();
                 int intValue = 0;
                 if ((attributeValue != null) &&
                     (attributeValue instanceof Integer)) {
                     intValue = ((Integer)attributeValue).intValue();
                 }
                 countSpinnerInt32.setValue(intValue);
             }
             else {
                 /**
                  * The first collection operator is set to something
                  * besides Count, so there is a second collection
                  * operator comboBox displayed.  Set the value of the
                  * second Collection Operator comboBox to be this
                  * row's value.
                  */
                 collectionOperator2ComboBox.setSelectedItem(
                     rowData.getCollectionOperator2());
             }
         }
     }
 
 
     /**
      * Set the operatorComboBox's model based on the passed
      * in type.  For example, if the type is Type.BOOLEAN,
      * then the operators are: "is true", "is false".
      * If the type is Type.UTF_8_STRING, then the operators
      * are: ==, !=, >, <, ~~=, etc.
      */
 	@SuppressWarnings("unchecked")
 	private void setOperatorComboBoxModel(Type type) {
 
         switch(type) {
 
             case BOOLEAN:
                 operatorComboBox.setModel(
                     new DefaultComboBoxModel(Operator.OPERATORS_BOOLEAN));
             break;
 
             case UTF_8_STRING:
                 operatorComboBox.setModel(new DefaultComboBoxModel(
                     Operator.OPERATORS_STRING));
             break;
 
             case DATE_TIME:
                 operatorComboBox.setModel(new DefaultComboBoxModel(
                     Operator.OPERATORS_DATE_TIME));
             break;
 
             default:  // INT_16, INT_32, FLOAT_64
                 operatorComboBox.setModel(new DefaultComboBoxModel(
                     Operator.OPERATORS_ARITHMATIC));
         }
     }
 
 
     /**
      * Set the focus to the first component we want to have
      * the focus.  Currently, I am simply setting the focus
      * to the first focusable component we contain, which
      * is always the first comboBox.  We might instead want
      * to set the focus to the last component this row
      * contains.  Once we get some user feedback, we can
      * work on this.
      *
      * Eventually, we probably want to have some sort of focus
      * traversal policy to handle this stuff, but this is
      * all we need for right now.
      */
     void setFocusToFirstFocusableComponent() {
 
         /**
          * We could do something as simple
          * as this if we wanted.
          */
         //comboBoxes.get(0).requestFocusInWindow();
 
         /**
          * But this is safer in case the layout
          * and/or components used in a RowPanel
          * get changed in the future.
          */
         for (Component component : getComponents()) {
             if (component.isFocusable()) {
                 component.requestFocusInWindow();
                 return;
             }
         }
     }
 
 
     /**
      * Set the focus to the deleteRowButton in this RowPanel.
      */
     void setFocusToDeleteButton() {
         deleteRowButton.requestFocusInWindow();
     }
 
 
     /**
      * Set the focus to the createAttributeRowButton in this RowPanel.
      */
     void setFocusToCreateAttributeRowButton() {
         createAttributeRowButton.requestFocusInWindow();
     }
 
 
     /**
      * We are required to implement this method because we are
      * a DocumentListener.
      */
     @Override
     public void insertUpdate(DocumentEvent event) {
         textFieldChanged(event.getDocument());
     }
 
 
     /**
      * We are required to implement this method because we are
      * a DocumentListener.
      */
     @Override
     public void removeUpdate(DocumentEvent event) {
         textFieldChanged(event.getDocument());
     }
 
 
     /**
      * We are required to implement this method because we are
      * a DocumentListener.
      */
     @Override
     public void changedUpdate(DocumentEvent event) {
     }
 
 
     /**
      * This is called when the value in any of our JSpinners change.
      */
     @Override
     public void stateChanged(ChangeEvent event) {
 
         /**
          * If we are in the process of updating the RowData due
          * to a programmatic change, ignore this.
          */
         if (inProcess)
             return;
 
         Object value = ((JSpinner)event.getSource()).getValue();
         rowData.setAttributeValue(value);
     }
 
 
     /**
      * This is called when the text field changes.  E.g. as the user
      * types into it.
      */
     private void textFieldChanged(Document document) {
 
         /**
          * If we are in the process of updating the RowData due
          * to a programmatic change, ignore this.
          */
         if (inProcess)
             return;
 
         if (document == valueTextField.getDocument())
             rowData.setAttributeValueUsingString(valueTextField.getText());
         else if (document == propNameTextField.getDocument())
             rowData.setPropName(propNameTextField.getText());
     }
 
 
     /**
      * This is a quick and dirty way to put a line between rows.
      * Perhaps a more proper, but more complicated, solution is
      * to create a Border subclass that draws the line.
      *
      * All this method does is call our superclass's normal paint()
      * method and then draws a line at the bottom of this panel.
      *
      * If/when we decide whether we want to have lines between rows
      * and/or do "zebra" striping of rows, possibly remove this
      * method.
      */
     @Override
     public void paint(Graphics g) {
         super.paint(g);
         g.drawLine(0, getHeight()-1, getWidth()-1, getHeight()-1);
     }
 }
