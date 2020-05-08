 /*
  * MacroPanel.java: GameTable is in the Public Domain.
  */
 
 
 package com.galactanet.gametable;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.*;
 import javax.swing.border.CompoundBorder;
 import javax.swing.border.MatteBorder;
 
 
 
 /**
  * Panel listing macros.
  * 
  * @author Iffy
  */
 public class MacroPanel extends JPanel
 {
     // --- Types -----------------------------------------------------------------------------------------------------
 
     /**
      * TODO: comment
      * 
      * @author Iffy
      */
     private class MacroEntryPanel extends JPanel
     {
         private DiceMacro macro;
 
         /**
          * This is the default constructor
          */
         public MacroEntryPanel(DiceMacro mac)
         {
             macro = mac;
             initialize();
         }
 
         public DiceMacro getMacro()
         {
             return macro;
         }
 
         /**
          * This method initializes this
          * 
          * @return void
          */
         private void initialize()
         {
             // setBackground(Color.LIGHT_GRAY);
 
             // There has to be better ways to organize all this without the constants?
 
             setMaximumSize(new Dimension(32768, 31));
 
             SpringLayout layout = new SpringLayout();
             setLayout(layout);
 
             JLabel nameLabel = new JLabel(macro.getName());
             nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
             nameLabel.setToolTipText(macro.toString());
 
             JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 3));
             panel.add(nameLabel);
             add(panel);
 
             Image rollImage = UtilityFunctions.getCachedImage("assets/roll.png");
 
             JButton rollButton = new JButton(new ImageIcon(rollImage));
             rollButton.setMargin(new Insets(0, 0, 0, 0));
             rollButton.setFocusable(false);
             rollButton.setToolTipText("Roll " + macro.toString());
 
             rollButton.addActionListener(new MacroActionListener(macro));
             add(rollButton);
 
             Image editImage = UtilityFunctions.getCachedImage("assets/edit.png");
 
             JButton editButton = new JButton(new ImageIcon(editImage));
             editButton.setMargin(new Insets(0, 0, 0, 0));
             editButton.setFocusable(false);
             editButton.setToolTipText("Edit " + macro.getName());
             add(editButton);
 
             Image deleteImage = UtilityFunctions.getCachedImage("assets/delete.png");
 
             JButton deleteButton = new JButton(new ImageIcon(deleteImage));
             deleteButton.setMargin(new Insets(0, 0, 0, 0));
             deleteButton.setFocusable(false);
             deleteButton.setToolTipText("Delete " + macro.getName());
             deleteButton.addActionListener(new DeleteMacroActionListener(macro));
             add(deleteButton);
 
             layout.getConstraints(panel).setX(Spring.constant(2));
             layout.getConstraints(panel).setY(Spring.constant(2));
             layout.putConstraint(SpringLayout.NORTH, panel, 2, SpringLayout.NORTH, this);
             layout.putConstraint(SpringLayout.SOUTH, panel, -2, SpringLayout.SOUTH, this);
 
             layout.putConstraint(SpringLayout.EAST, deleteButton, -2, SpringLayout.EAST, this);
             layout.putConstraint(SpringLayout.NORTH, deleteButton, 2, SpringLayout.NORTH, this);
             layout.putConstraint(SpringLayout.SOUTH, deleteButton, -2, SpringLayout.SOUTH, this);
 
             layout.putConstraint(SpringLayout.EAST, editButton, -2, SpringLayout.WEST, deleteButton);
             layout.putConstraint(SpringLayout.NORTH, editButton, 2, SpringLayout.NORTH, this);
             layout.putConstraint(SpringLayout.SOUTH, editButton, -2, SpringLayout.SOUTH, this);
 
             layout.putConstraint(SpringLayout.EAST, rollButton, -2, SpringLayout.WEST, editButton);
             layout.putConstraint(SpringLayout.NORTH, rollButton, 2, SpringLayout.NORTH, this);
             layout.putConstraint(SpringLayout.SOUTH, rollButton, -2, SpringLayout.SOUTH, this);
 
             setBorder(new CompoundBorder(new MatteBorder(2, 2, 2, 2, Color.WHITE), new MatteBorder(1, 1, 1, 1,
                 Color.BLACK)));
         }
     }
 
     /**
      * Action for macro buttons.
      * 
      * @author Iffy
      */
     private static class MacroActionListener implements ActionListener
     {
         private DiceMacro macro;
 
         public MacroActionListener(DiceMacro mac)
         {
             macro = mac;
         }
 
         /*
          * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
          */
         public void actionPerformed(ActionEvent e)
         {
             GametableFrame.getGametableFrame().postMessage(macro.doMacro());
         }
     }
 
     /**
      * Action for macro buttons.
      * 
      * @author Iffy
      */
     private static class DeleteMacroActionListener implements ActionListener
     {
         private DiceMacro macro;
 
         public DeleteMacroActionListener(DiceMacro mac)
         {
             macro = mac;
         }
 
         /*
          * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
          */
         public void actionPerformed(ActionEvent e)
         {
             GametableFrame.getGametableFrame().removeMacro(macro);
         }
     }
 
     // --- Members ---------------------------------------------------------------------------------------------------
 
     private JScrollPane scrollPane      = null;
     private JPanel      macroPanel      = null;
     private Set         displayedMacros = new HashSet();
 
     // --- Constructors ----------------------------------------------------------------------------------------------
 
     /**
      * This is the default constructor
      */
     public MacroPanel()
     {
         initialize();
         refreshMacroList();
     }
 
     // --- Methods ---------------------------------------------------------------------------------------------------
 
     /**
      * Updates the macro list from the latest data.
      */
     public void refreshMacroList()
     {
         List macros = GametableFrame.getGametableFrame().getMacros();
         Set toDelete = new HashSet(displayedMacros);
         toDelete.removeAll(macros);
         
         Set toAdd = new HashSet(macros);
         toAdd.removeAll(displayedMacros);
         
         for (Iterator iterator = toDelete.iterator(); iterator.hasNext();)
         {
             DiceMacro macro = (DiceMacro)iterator.next();
             Component component = getMacroComponent(macro);
             if (component != null)
             {
                macroPanel.remove(component);
             }
             displayedMacros.remove(macro);
         }
         
         for (Iterator iterator = toAdd.iterator(); iterator.hasNext();)
         {
             DiceMacro macro = (DiceMacro)iterator.next();
             MacroEntryPanel panel = new MacroEntryPanel(macro);
             macroPanel.add(panel);
             displayedMacros.add(macro);
         }
        macroPanel.validate();
     }
 
     // --- Private Methods ---
 
     private Component getMacroComponent(DiceMacro macro)
     {
         for (int i = 0, size = macroPanel.getComponentCount(); i < size; ++i)
         {
             MacroEntryPanel entry = (MacroEntryPanel)macroPanel.getComponent(i);
             if (entry.getMacro() == macro)
             {
                 return entry;
             }
         }
 
         return null;
     }
 
     /**
      * This method initializes this
      * 
      * @return void
      */
     private void initialize()
     {
         setLayout(new BorderLayout());
         add(getScrollPane(), BorderLayout.CENTER);
     }
 
     /**
      * This method initializes scrollPane
      * 
      * @return javax.swing.JScrollPane
      */
     private JScrollPane getScrollPane()
     {
         if (scrollPane == null)
         {
             scrollPane = new JScrollPane(getMacroPanel(), ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         }
         return scrollPane;
     }
 
     /**
      * This method initializes macroPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getMacroPanel()
     {
         if (macroPanel == null)
         {
             macroPanel = new JPanel();
             macroPanel.setLayout(new BoxLayout(macroPanel, BoxLayout.Y_AXIS));
             macroPanel.setBackground(Color.WHITE);
         }
         return macroPanel;
     }
 
 }
