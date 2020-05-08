 package tatoo.view.armyBuilder;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.LayoutManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.Popup;
 import javax.swing.SwingConstants;
 import javax.swing.border.LineBorder;
 
 import tatoo.model.ArmyBuilderEntityModel;
 import tatoo.model.conditions.ConditionParseException;
 import tatoo.model.entities.AbstractEntity.EntityType;
 import tatoo.view.TatooPanel;
 
 /**
  * Panel to edit Entities.
  * 
  * @author mkortz
  */
 @SuppressWarnings ("serial")
 public class EntityEditPane extends ArmyBuilderEditPanel implements ActionListener {
 
     private JLabel     title;
     private JTextField count;
     private JTextField points;
     private JTextField minCount;
     private JTextField maxCount;
     private JComboBox  type;
     Popup              popup;
     private boolean    textFieldModified = false;
 
     public EntityEditPane (ArmyBuilderEntityModel entityModel) {
         super (entityModel);
         showValues ();
     }
 
     /**
      * arrange Components on Criteria builder.
      */
     @Override
     public void buildPanel () {
 
         if (title == null) {
             title = new JLabel ();
             title.setPreferredSize (new Dimension (width, 35));
             title.setBorder (new LineBorder (Color.black));
             title.setHorizontalAlignment (SwingConstants.CENTER);
         }
         if (count == null)
             count = new JTextField ();
         if (points == null)
             points = new JTextField ();
         if (minCount == null)
             minCount = new JTextField ();
         if (maxCount == null)
             maxCount = new JTextField ();
         if (type == null) {
             if (model.getSource () == null)
                 type = new JComboBox ();
             else {
                 EntityType[] possibleNodeTypes = model.getPossibleNodeTypes ();
                 String[] nodeStrings = new String[possibleNodeTypes.length];
                 for (int i = 0; i < possibleNodeTypes.length; i++ )
                     nodeStrings[i] = possibleNodeTypes[i].name ();
                 type = new JComboBox (nodeStrings);
                 if (possibleNodeTypes.length < 2)
                     type.setEditable (false);
                 else type.setEditable (true);
             }
         }
 
         JPanel countPanel = buildAttribPanel ("Count", count);
         count.getDocument ().addDocumentListener (new TextFieldChangeHandler (count) {
 
             @Override
             public void setValue (String val) {
                 if ( !textFieldModified) {
                     textFieldModified = true;
                     try {
                         model.setCount (val);
                         textFieldModified = false;
                         count.setBackground (Color.WHITE);
                     }
                     catch (ConditionParseException cpe) {
                         count.setBackground (new Color (255, 170, 170));
                     }
                 }
             };
         });
         JPanel pointsPanel = buildAttribPanel ("Points", points);
         points.getDocument ().addDocumentListener (new TextFieldChangeHandler (points) {
 
             @Override
             public void setValue (String val) {
                 if ( !textFieldModified) {
                     textFieldModified = true;
                     try {
                         model.setPrice (val);
                         textFieldModified = false;
                         points.setBackground (Color.WHITE);
                     }
                     catch (ConditionParseException cpe) {
                         points.setBackground (new Color (255, 170, 170));
                     }
                 }
             };
         });
         JPanel minCountPanel = buildAttribPanel ("Min Count", minCount);
         minCount.getDocument ().addDocumentListener (new TextFieldChangeHandler (minCount) {
 
             @Override
             public void setValue (String val) {
                 if ( !textFieldModified) {
                     textFieldModified = true;
                     try {
                         model.setMinCount (val);
                         textFieldModified = false;
                         minCount.setBackground (Color.WHITE);
                     }
                     catch (ConditionParseException cpe) {
                         minCount.setBackground (new Color (255, 170, 170));
                     }
                 }
             };
         });
         JPanel maxCountPanel = buildAttribPanel ("Max Count", maxCount);
         maxCount.getDocument ().addDocumentListener (new TextFieldChangeHandler (maxCount) {
 
             @Override
             public void setValue (String val) {
                 if ( !textFieldModified) {
                     textFieldModified = true;
                     try {
                         model.setMaxCount (val);
                         textFieldModified = false;
                         maxCount.setBackground (Color.WHITE);
                     }
                     catch (ConditionParseException cpe) {
                         maxCount.setBackground (new Color (255, 170, 170));
                        textFieldModified = false;
                     }
                 }
             };
         });
         JPanel typePanel = buildAttribPanel ("Node Type", type);
         type.addActionListener (new ActionListener () {
 
             @Override
             public void actionPerformed (ActionEvent e) {
                 if ( !textFieldModified) {
                     JComboBox cb = (JComboBox) e.getSource ();
                     String val = (String) cb.getSelectedItem ();
                     if ( !textFieldModified) {
                         textFieldModified = true;
                         model.setType (val);
                         textFieldModified = false;
                     }
 
                 }
             }
         });
 
         JPanel editPane = new TatooPanel ();
         LayoutManager editPaneLayout = new BoxLayout (editPane, BoxLayout.Y_AXIS);
         editPane.setLayout (editPaneLayout);
         editPane.add (typePanel);
         editPane.add (countPanel);
         editPane.add (pointsPanel);
         editPane.add (minCountPanel);
         editPane.add (maxCountPanel);
         editPane.add (Box.createVerticalGlue ());
 
         setLayout (new BorderLayout ());
         setBackground (Color.white);
         add (title, BorderLayout.NORTH);
         add (editPane, BorderLayout.CENTER);
 
     }
 
     private JPanel buildAttribPanel (String title, JComponent component) {
         JLabel titleLabel = new JLabel (title);
 
         // final JButton conditionBuilderButton = new JButton ();
         // conditionBuilderButton.addActionListener (new ActionListener () {
         //
         // @Override
         // public void actionPerformed (ActionEvent e) {
         // // condBuilder = new ConditionBuilder(new PriceSetter(), model);
         // // condBuilder.addActionListener(thisPane);
         // // PopupFactory factory = PopupFactory.getSharedInstance();
         // // if (popup != null)
         // // popup.hide();
         // // popup = factory.getPopup(
         // // thisPane, condBuilder,
         // // conditionBuilderButton.getLocationOnScreen().x -150,
         // // conditionBuilderButton.getLocationOnScreen().y + 20);
         // // popup.show();
         // }
         // });
 
         JPanel attribPanel = new groupPane (title);
         attribPanel.setLayout (new BorderLayout ());
         attribPanel.add (BorderLayout.NORTH, titleLabel);
         attribPanel.add (BorderLayout.CENTER, component);
         // attribPanel.add (BorderLayout.EAST, conditionBuilderButton);
 
         return attribPanel;
     }
 
     /**
      * get Values from model and update EntityEditPanel.
      */
     @Override
     void showValues () {
         if (model == null || model.getSource () == null) {
             title.setEnabled (false);
             count.setEnabled (false);
             points.setEnabled (false);
             minCount.setEnabled (false);
             maxCount.setEnabled (false);
             type.setEnabled (false);
         }
         else {
             title.setEnabled (true);
             count.setEnabled (true);
             points.setEnabled (true);
             minCount.setEnabled (true);
             maxCount.setEnabled (true);
             type.setEnabled (true);
             if ( !textFieldModified) {
                 textFieldModified = true;
                 title.setText (model.getName ());
 
                 count.setText (model.getCount ());
                 points.setText (model.getPrice ());
                 minCount.setText (model.getMinCount ());
                 maxCount.setText (model.getMaxCount ());
 
                 // die Combobox für den Typ neu setzen
                 type.removeAllItems ();
                 EntityType[] possibleNodeTypes = model.getPossibleNodeTypes ();
                 for (int i = 0; i < possibleNodeTypes.length; i++ )
                     type.addItem (possibleNodeTypes[i].name ());
                 if (possibleNodeTypes.length < 2)
                     type.setEditable (false);
                 else type.setEditable (true);
                 type.setSelectedItem (model.getSourceType ().name ());
                 textFieldModified = false;
             }
         }
 
     }
 
     /**
      * Hides the performing popup and refreshes the Values in EditPanel.
      * EntitiyEditPanel listens on Popup Windows. If an Action is performed from
      * them this Method is called.
      */
     @Override
     public void actionPerformed (ActionEvent e) {
         popup.hide ();
         showValues ();
     }
 
 }
